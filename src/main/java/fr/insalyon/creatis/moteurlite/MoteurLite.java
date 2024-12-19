package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesService;
import fr.insalyon.creatis.moteurlite.boutiques.Input;
import fr.insalyon.creatis.moteurlite.boutiques.OutputFile;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategyService;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {

    public static final String RESULTS_DIRECTORY = "results-directory";
    private static final Logger logger = Logger.getLogger(MoteurLite.class);

    public static void main(String[] args) throws MoteurLiteException {
        // Verify arguments
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        }
        String workflowId = args[0];
        String boutiquesFilePath = args[1];
        String inputsFilePath = args[2];

        checkArguments(workflowId, boutiquesFilePath, inputsFilePath);

        new MoteurLite().runWorkflow(workflowId, boutiquesFilePath, inputsFilePath);
    }

    private static void checkArguments(String workflowId, String boutiquesFilePath, String inputsFilePath) {
        // Validate workflowId as a non-empty string
        if (workflowId == null || workflowId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid workflowId. It should be a simple non-empty string.");
        }

        // Validate boutiquesFilePath as a JSON file
        if (!boutiquesFilePath.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid boutiquesFilePath. It should be a JSON file.");
        }

        // Validate inputsFilePath as an XML file
        if (!inputsFilePath.endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid inputsFilePath. It should be an XML file.");
        }
    }

    private final WorkflowsDbRepository workflowsDbRepo;
    private final BoutiquesService boutiquesService;
    private final InputsFileService inputsFileService;
    private final IterationStrategyService iterationStrategyService;

    public MoteurLite() throws MoteurLiteException {
        // Build dependencies
        boutiquesService = new BoutiquesService();
        inputsFileService = new InputsFileService();
        iterationStrategyService = new IterationStrategyService(boutiquesService);
        try {
            workflowsDbRepo = WorkflowsDbRepository.getInstance();
        } catch (WorkflowsDBDAOException | WorkflowsDBException e) {
            logger.error("Error creating workflows db repo", e);
            throw new MoteurLiteException("Error creating workflows db repo", e);
        }
    }

    public void runWorkflow(String workflowId, String boutiquesFilePath, String inputsFilePath) throws MoteurLiteException {
        // Parse boutiques and inputs
        Map<String, List<String>> allInputs = inputsFileService.parseInputData(inputsFilePath);
        BoutiquesDescriptor descriptor = boutiquesService.parseFile(boutiquesFilePath);
        HashMap<String, Input> boutiquesInputs = boutiquesService.getInputsMap(descriptor);
        HashMap<String, OutputFile> boutiquesOutputs = boutiquesService.getOutputMap(descriptor);

        // Compute iteration strategy
        List<Map<String, String>> invocationsInputs = iterationStrategyService.compute(descriptor, allInputs);

        // Persist processors before persisting inputs
        workflowsDbRepo.persistProcessors(workflowId, descriptor.getName(), 0, 0, 0);

        // Persist inputs
        workflowsDbRepo.persistInputs(workflowId, allInputs, boutiquesInputs);

        // Initialize Gasw and GaswMonitor
        Gasw gasw = null;
        try {
            gasw = Gasw.getInstance();
            GaswMonitor gaswMonitor = new GaswMonitor(gasw, workflowsDbRepo, workflowId, descriptor.getName(), boutiquesOutputs, invocationsInputs.size());
            gasw.setNotificationClient(gaswMonitor);
            gaswMonitor.start();
        } catch (GaswException e) {
            logger.error("Error launching gasw", e);
            throw new MoteurLiteException("Error launching gasw", e);
        }

        // Launch jobs
        createJobs(gasw, descriptor.getName(), invocationsInputs, boutiquesInputs);

    }

    private void createJobs(Gasw gasw, String applicationName, List<Map<String, String>> allInvocationsInputs, HashMap<String, Input> boutiquesInputs) throws MoteurLiteException {
        for (Map<String, String> invocationInputs : allInvocationsInputs) {

            URI resultsDirectoryURI = null;
            List<URI> downloads = new ArrayList<>();
            Map<String, String> finalInvocationInputs = new HashMap<>();

            for (String inputId : invocationInputs.keySet()) {
                String inputValue = invocationInputs.get(inputId);
                if (RESULTS_DIRECTORY.equals(inputId)) {
                    resultsDirectoryURI = getURI(inputValue);
                } else {
                    if (Input.Type.FILE.equals(boutiquesInputs.get(inputId).getType())) {
                        URI downloadURI = getURI(inputValue);
                        String filename = Paths.get(downloadURI.getPath()).getFileName().toString();
                        downloads.add(downloadURI);
                        inputValue = filename;
                    }
                    finalInvocationInputs.put(inputId, inputValue);
                }
            }

            // Convert invocation map to JSON
            String invocationString = convertMapToJson(finalInvocationInputs, boutiquesInputs);
            String jobId = applicationName + "-" + System.nanoTime() + ".sh";

            GaswInput gaswInput = new GaswInput(applicationName, applicationName + ".json", downloads, resultsDirectoryURI, invocationString, jobId);
            try {
                gasw.submit(gaswInput);
            } catch (GaswException e) {
                logger.error("Error submitting gasw job", e);
                throw new MoteurLiteException("Error submitting gasw job", e);
            }
        }
    }

    private URI getURI(String inputValue) throws MoteurLiteException {
        try {
            return new URI(inputValue);
        } catch (URISyntaxException e) {
            logger.error("Error parsing URI : " + inputValue, e);
            throw new MoteurLiteException("Error parsing URI : " + inputValue, e);
        }
    }

    private String convertMapToJson(Map<String, String> invocationInputs, HashMap<String, Input> boutiquesInputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();

        for (String inputId : invocationInputs.keySet()) {
            String value = invocationInputs.get(inputId);
            Input.Type type = boutiquesInputs.get(inputId).getType();

            if (type == Input.Type.NUMBER ) {
                if (boutiquesInputs.get(inputId).getInteger() != null && boutiquesInputs.get(inputId).getInteger()) {
                    jsonNode.put(inputId, Integer.parseInt(value));
                } else {
                    jsonNode.put(inputId, Float.parseFloat(value));
                }
            } else if (type == Input.Type.FLAG) {
                jsonNode.put(inputId, Boolean.parseBoolean(value));
            } else {
                jsonNode.put(inputId, value);
            }
        }
        return jsonNode.toString();
    }
}