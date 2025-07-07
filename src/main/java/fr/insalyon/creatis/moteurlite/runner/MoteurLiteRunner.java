package fr.insalyon.creatis.moteurlite.runner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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
import fr.insalyon.creatis.moteurlite.gasw.GaswMonitor;
import fr.insalyon.creatis.moteurlite.workflowsdb.WorkflowsDBRepository;
import fr.insalyon.creatis.moteurlite.iteration.IterationService;
import fr.insalyon.creatis.moteurlite.custom.DirectoryInputsService;
import fr.insalyon.creatis.moteurlite.custom.IntIteratorInputsService;
import fr.insalyon.creatis.moteurlite.custom.ResultsDirectorySuffixService;

public class MoteurLiteRunner {
    private static final Logger logger = Logger.getLogger(MoteurLite.class);

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
    private GaswMonitor gaswMonitor;

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
            Map<String, List<String>> allInputs = inputsFileService.parseInputData(inputsFilePath);
            BoutiquesDescriptor descriptor = boutiquesService.parseFile(boutiquesFilePath);
            Map<String, Input> boutiquesInputs = boutiquesService.getInputsMap(descriptor);
            Set<String> optionalInputs = boutiquesService.getInputOptionalOfBoutiquesFile(descriptor);

            // expand vip:directoryInputs
            directoryInputsService.updateInputs(allInputs, descriptor);
            // expand vip:intIteratorInputs
            intIteratorInputsService.updateInputs(allInputs, descriptor);
            // apply vip:resultsDirectorySuffix to results-directory
            resultsDirectorySuffixService.updateInputs(allInputs, descriptor);
            // compute vip:dot and cross combinations
            List<Map<String, String>> invocationsInputs = iterationService.compute(allInputs, optionalInputs, descriptor);

            // check maxJobs limit
            int plannedJobs = invocationsInputs.size(), maxJobs = config.getMaxJobsPerWorkflow();
            if (plannedJobs > maxJobs) {
                throw new MoteurLiteException("Too many jobs (max:" + maxJobs + ", got:" + plannedJobs + ")");
            }

            // store inputs and create processors in workflowsdb
            workflowsDBRepo.persistProcessors(workflowId, descriptor.getName(), 0, 0, 0);
            workflowsDBRepo.persistInputs(workflowId, allInputs, boutiquesInputs);

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
            gaswMonitor = new GaswMonitor(gasw, workflowsDBRepo, workflowId, descriptorName, numberOfInvocations);
            gasw.setNotificationClient(gaswMonitor);
            gaswMonitor.start();

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
                    if ( ! gaswMonitor.isAlive()) {
                        // normal shutdown, not a soft-kill
                        return;
                    }
                    logger.info("Trying to perform a soft-kill!");

                    jobSumitter.interrupt();
                    jobSumitter.join();

                    gaswMonitor.interrupt();
                    gaswMonitor.join();

                    logger.info("Soft-kill have been successfully done!");

                } catch (InterruptedException e) {
                    logger.error("Soft-kill may did not work properly (hard-kill was used instead)!");
                }
            }
        });
    }
}
