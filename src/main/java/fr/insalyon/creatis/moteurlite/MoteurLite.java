package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesService;
import fr.insalyon.creatis.moteurlite.boutiques.Input;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategyService;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {

    public static void main(String[] args) throws Exception {
        // Verify arguments
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        }
        String workflowId = args[0];
        String boutiquesFilePath = args[1];
        String inputsFilePath = args[2];

        checkArguments(workflowId, boutiquesFilePath, inputsFilePath);

        // Build dependencies
        WorkflowsDbRepository workflowsDbRepo = WorkflowsDbRepository.getInstance();
        BoutiquesService boutiquesService = new BoutiquesService();
        IterationStrategyService iterationStrategyService = new IterationStrategyService(boutiquesService);

        // Parse boutiques and inputs
        BoutiquesDescriptor descriptor = boutiquesService.parseFile(boutiquesFilePath);
        InputsFileService inputsFileService = new InputsFileService();
        Map<String, List<String>> inputValues = inputsFileService.parse(inputsFilePath);

        // Compute iteration strategy
        List<Map<String, String>> jobsInputValues = iterationStrategyService.compute(descriptor, inputValues);

        // Persist processors before persisting inputs
        workflowsDbRepo.persistProcessors(workflowId, descriptor.getName(), 0, 0, 0);
        workflowsDbRepo.persistInputs(workflowId, inputValues, boutiquesService.getInputType(descriptor));

        // Persist inputs
        workflowsDbRepo.persistInputs(workflowId, inputValues, boutiquesService.getInputType(descriptor));

        // Initialize Gasw and GaswMonitor
        Gasw gasw = Gasw.getInstance();
        GaswMonitor gaswMonitor = new GaswMonitor(workflowId, descriptor.getName(), jobsInputValues.size(), gasw);
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();

        // Launch jobs
        createJobs(descriptor, jobsInputValues, boutiquesFilePath, boutiquesService);
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

    private static void createJobs(BoutiquesDescriptor descriptor, List<Map<String, String>> inputs, String boutiquesFilePath, BoutiquesService boutiquesService) throws Exception {
        for (Map<String, String> inputMap : inputs) {
            Map<String, String> inputsMap = new HashMap<>();
            Map<String, String> invocation = new HashMap<>();

            for (Map.Entry<String, String> entry : inputMap.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().equals("results-directory")) {
                    invocation.put(entry.getKey(), entry.getValue());
                }
            }

            // Extract file inputs using BoutiquesDescriptor
            List<URI> downloads = new ArrayList<>();
            for (Input input : descriptor.getInputs()) {
                if (input.getType() == Input.Type.FILE) {
                    String inputId = input.getId();
                    if (inputsMap.containsKey(inputId)) {
                        try {
                            URI fileUri = new URI(inputsMap.get(inputId));
                            if (fileUri.getScheme() == null) {
                                // Add "lfn://" prefix if URI has no scheme
                                fileUri = new URI("lfn://" + inputsMap.get(inputId));
                            }
                            downloads.add(fileUri);
                        } catch (URISyntaxException e) {
                            System.err.println("Error parsing URI: " + inputsMap.get(inputId));
                        }
                    }
                }
            }

            // Get the results directory
            URI resultsDirectoryURI;
            try {
                resultsDirectoryURI = new URI(inputsMap.get("results-directory"));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid URI for results directory: " + e.getMessage(), e);
            }

            // Convert invocation map to JSON
            String invocationString = convertMapToJson(invocation, boutiquesService.getInputType(descriptor));
            String jobId = descriptor.getName() + "-" + System.nanoTime() + ".sh";

            GaswInput gaswInput = new GaswInput(descriptor.getName(), descriptor.getName() + ".json", downloads, resultsDirectoryURI, invocationString, jobId);
            Gasw.getInstance().submit(gaswInput);
        }
    }

    private static String convertMapToJson(Map<String, String> invocationMap, Map<String, Input.Type> inputTypeMap) throws URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();

        for (Map.Entry<String, String> entry : invocationMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Input.Type type = inputTypeMap.get(key);

            if (type == Input.Type.NUMBER) {
                jsonNode.put(key, Float.parseFloat(value));
            } else if (type == Input.Type.FLAG) {
                jsonNode.put(key, Boolean.parseBoolean(value));
            } else if (type == Input.Type.FILE) {
                value = Paths.get(new URI(value).getPath()).getFileName().toString();
                jsonNode.put(key, value);
            } else {
                jsonNode.put(key, value);
            }
        }
        return jsonNode.toString();
    }
}