package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesService;
import fr.insalyon.creatis.moteurlite.inputsParser.InputsFileParser;
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
        Map<String, List<String>> inputValues = new InputsFileParser().parse(inputsFilePath);

        // Initialize the database with parsed inputs
        workflowsDbRepo.persistInputs(workflowId, inputValues, boutiquesService.getInputType(descriptor));

        // Compute iteration strategy
        List<Map<String, String>> jobsInputValues = iterationStrategyService.compute(descriptor, inputValues);

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

            // Fetch download files using InputsFileParser
            List<URI> downloads = InputsFileParser.getDownloadFiles(inputsMap);

            // Get the results directory
            URI resultsDirectoryURI = new URI(inputsMap.get("results-directory"));

            // Use BoutiquesService to fetch the input types
            String invocationString = CreateInvocation.convertMapToJson(invocation, boutiquesService.getInputType(descriptor));
            String jobId = descriptor.getName() + "-" + System.nanoTime() + ".sh";

            // Convert boutiquesFilePath to absolute path
            File boutiquesFile = new File(boutiquesFilePath);
            if (!boutiquesFile.isAbsolute()) {
                boutiquesFilePath = boutiquesFile.getAbsolutePath();
            }

            GaswInput gaswInput = new GaswInput(descriptor.getName(), descriptor.getName() + ".json", downloads, resultsDirectoryURI, invocationString, jobId);
            Gasw.getInstance().submit(gaswInput);
        }
    }
}
