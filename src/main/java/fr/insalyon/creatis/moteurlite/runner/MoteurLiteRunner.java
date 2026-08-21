package fr.insalyon.creatis.moteurlite.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteurlite.MoteurLite;
import fr.insalyon.creatis.moteurlite.MoteurLiteConfiguration;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.boutiques.BoutiquesException;
import fr.insalyon.creatis.boutiques.BoutiquesService;
import fr.insalyon.creatis.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.boutiques.model.Input;
import fr.insalyon.creatis.moteurlite.gasw.Monitor;
import fr.insalyon.creatis.moteurlite.workflowsdb.WorkflowsDBRepository;
import fr.insalyon.creatis.moteurlite.iteration.IterationService;
import fr.insalyon.creatis.moteurlite.custom.DirectoryInputsService;
import fr.insalyon.creatis.moteurlite.custom.IntIteratorInputsService;
import fr.insalyon.creatis.moteurlite.custom.ResultsDirectorySuffixService;

public class MoteurLiteRunner {
    private static final Logger logger = LoggerFactory.getLogger(MoteurLite.class);

    private final MoteurLiteConfiguration config;
    private final WorkflowsDBRepository workflowsDBRepo;
    private final BoutiquesService boutiquesService;
    private final InputsFileService inputsFileService;
    private final IterationService iterationService;
    private final DirectoryInputsService directoryInputsService;
    private final IntIteratorInputsService intIteratorInputsService;
    private final ResultsDirectorySuffixService resultsDirectorySuffixService;

    private Gasw       gasw;
    private JobSubmitter jobSumitter;
    private Monitor monitor;

    public MoteurLiteRunner() throws MoteurLiteException {
        config = new MoteurLiteConfiguration();
        boutiquesService = new BoutiquesService();
        inputsFileService = new InputsFileService();
        iterationService = new IterationService(boutiquesService);
        directoryInputsService = new DirectoryInputsService(config);
        intIteratorInputsService = new IntIteratorInputsService();
        resultsDirectorySuffixService = new ResultsDirectorySuffixService();

        try {
            workflowsDBRepo = WorkflowsDBRepository.getInstance();
        } catch (WorkflowsDBDAOException | WorkflowsDBException e) {
            logger.error("Error creating workflows db repo", e);
            throw new MoteurLiteException("Error creating workflows db repo", e);
        }
    }

    public void run(String workflowId, String boutiquesFilePath, String inputsFilePath) throws MoteurLiteException {
        try {
            List<Map<String, List<String>>> allInputs = inputsFileService.parse(inputsFilePath);
            BoutiquesDescriptor descriptor = boutiquesService.parseFile(boutiquesFilePath);
            Map<String, Input> boutiquesInputs = boutiquesService.getInputsMap(descriptor);
            for (Map<String, List<String>> inputMap : allInputs) {
                // expand vip:directoryInputs
                directoryInputsService.updateInputs(inputMap, descriptor);
                // expand vip:intIteratorInputs
                intIteratorInputsService.updateInputs(inputMap, descriptor);
                // apply vip:resultsDirectorySuffix to results-directory
                resultsDirectorySuffixService.updateInputs(inputMap, descriptor);
            }

            List<Map<String, String>> invocationsInputs;
            // Compute vip:dot and cross combinations only if there is a single input map
            if (allInputs.size() == 1) {
                invocationsInputs = iterationService.compute(allInputs.getFirst(), descriptor, config.getMaxJobsPerWorkflow());
            } else {
                invocationsInputs = new ArrayList<>();
                for (Map<String, List<String>> inputMap : allInputs) {
                    Map<String, String> invocationInputMap = new HashMap<>();
                    // Only keep the first value of each input map, there is not supposed to be multiple ones here
                    for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
                        if (entry.getValue().size() > 1) {
                            throw new MoteurLiteException("Multiple values for input '" + entry.getKey() + "' are not allowed when providing a list of input maps");
                        }

                        invocationInputMap.put(entry.getKey(), entry.getValue().getFirst());
                    }

                    invocationsInputs.add(invocationInputMap);
                }
            }

            // check maxJobs limit
            int plannedJobs = invocationsInputs.size(), maxJobs = config.getMaxJobsPerWorkflow();
            if (plannedJobs > maxJobs) {
                throw new MoteurLiteException("Too many jobs (max:" + maxJobs + ", got:" + plannedJobs + ")");
            }

            // store inputs and create processors in workflowsdb
            workflowsDBRepo.persistProcessors(workflowId, descriptor.getName(), 0, 0, 0);
            // Same input values may be persisted multiple times with dot combinations or multiple input maps.
            // This technically breaks the Inputs primary key (workflowId/path/input), but we use merge() as db operation, which overwrites duplicates safely.
            for (Map<String, List<String>> inputMap : allInputs) {
                workflowsDBRepo.persistInputs(workflowId, inputMap, boutiquesInputs);
            }
            // init gasw
            initGaswAndMonitor(workflowId, descriptor.getName(), invocationsInputs.size());

            // launch jobs
            jobSumitter = new JobSubmitter(gasw, descriptor.getName(), invocationsInputs, boutiquesInputs);
            jobSumitter.start();

            listenSoftKill();
        } catch (BoutiquesException e) {
            throw new MoteurLiteException(e);
        }
    }

    private void initGaswAndMonitor(String workflowId, String descriptorName, int numberOfInvocations) throws MoteurLiteException {
        try {
            gasw = Gasw.getInstance();
            monitor = new Monitor(gasw, workflowsDBRepo, workflowId, descriptorName, numberOfInvocations);
            gasw.setNotificationClient(monitor);
            monitor.start();

        } catch (GaswException e) {
            logger.error("Error launching gasw", e);
            throw new MoteurLiteException("Error launching gasw", e);
        }
    }

    private void listenSoftKill() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if ( ! monitor.isAlive()) {
                        // normal shutdown, not a soft-kill
                        return;
                    }
                    logger.info("Trying to perform a soft-kill!");

                    jobSumitter.interrupt();
                    jobSumitter.join();

                    monitor.interrupt();
                    monitor.join();

                    logger.info("Soft-kill have been successfully done!");

                } catch (InterruptedException e) {
                    logger.error("Soft-kill may did not work properly (hard-kill was used instead)!");
                }
            }
        });
    }
}
