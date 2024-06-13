package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.moteurlite.boutiquesParser.BoutiquesEntities;
import fr.insalyon.creatis.moteurlite.boutiquesParser.BoutiquesService;
import fr.insalyon.creatis.moteurlite.inputsParser.ParseInputsFile;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategy;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {
    private Gasw gasw = Gasw.getInstance();
    private BoutiquesService boutiquesService = new BoutiquesService();
    private int sizeOfInputs;
    private IterationStrategy iterationStrategy = new IterationStrategy();
    private Workflowsdb workflowsdb = new Workflowsdb();
    private static ScriptLoader scriptLoader = new ScriptLoader();

    public static void main(String[] args) throws Exception {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws Exception {
        // Verify arguments
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        }

        String workflowId = args[0];
        String boutiquesFilePath = args[1];
        String inputsFilePath = args[2];

        // Check if arguments are not null or empty
        for (String arg : args) {
            if (arg == null || arg.trim().isEmpty()) {
                throw new IllegalArgumentException("Arguments cannot be null or empty.");
            }
        }

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

        // Parse boutiques file
        BoutiquesEntities boutiquesEntities = boutiquesService.parseFile(boutiquesFilePath);
        String executableName = boutiquesService.getNameOfBoutiquesFile();
        String applicationName = boutiquesService.getApplicationName();
        HashMap<Integer, String> inputBoutiquesId = boutiquesService.getInputIdOfBoutiquesFile();
        HashMap<Integer, String> outputBoutiquesId = boutiquesService.getOutputIdOfBoutiquesFile();
        HashMap<String, String> inputBoutiquesType = boutiquesService.getInputTypeOfBoutiquesFile();
        Set<String> crossMap = boutiquesService.getCrossMap();
        Set<String> dotMap = boutiquesService.getDotMap();
        Set<String> inputOptional = boutiquesService.getInputOptionalOfBoutiquesFile();
        
        // Parse inputs
        ParseInputsFile inputsParser = new ParseInputsFile(inputsFilePath);
        List<Map<String, String>> inputData = inputsParser.getInputData();
        Map<String, String> inputType = inputBoutiquesType;
        Map<String, String> resultsDirectory = inputsParser.getResultDirectory();

        // Set workflowsdb
        workflowsdb.persistInputs(workflowId, inputData, inputType, resultsDirectory);

        // Set iteration strategy
        iterationStrategy.IterationStratergy(inputData, resultsDirectory, crossMap, dotMap, inputOptional);
        List<Map<String, String>> jsonIterations = iterationStrategy.getJsonIterations();
        sizeOfInputs = jsonIterations.size();

        // Set gasw monitoring
        GaswMonitor gaswMonitor = new GaswMonitor(workflowId, applicationName, outputBoutiquesId, sizeOfInputs, gasw);
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();

        // Create jobs
        createJobs(jsonIterations, applicationName, boutiquesFilePath, executableName, inputBoutiquesId, outputBoutiquesId, inputType, resultsDirectory, workflowId, gasw, scriptLoader.loadBashScript());
    }

    public static void createJobs(List<Map<String, String>> inputs, String applicationName, String boutiquesFilePath, String executableName, HashMap<Integer, String> inputBoutiquesId, HashMap<Integer, String> outputBoutiquesId, Map<String, String> inputType, Map<String, String> resultsDirectory, String workflowId, Gasw gasw, String bashScript) throws Exception {
        for (Map<String, String> innerList : inputs) {
            Map<String, String> inputsMap = new HashMap<>();
            Map<String, String> invocation = new HashMap<>();

            for (Map.Entry<String, String> entry : innerList.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().equals("results-directory")) {
                    invocation.put(entry.getKey(), entry.getValue());
                }
            }

            List<URI> downloadFiles = ParseInputsFile.getDownloadFiles(inputsMap);
            String outputDirName = "outputDirectoryName(applicationName)";

            GaswParser gaswParser = new GaswParser();
            String invocationString = CreateInvocation.convertMapToJson(invocation, inputType);
            String jobId = applicationName + "-" + System.nanoTime() + ".sh";
            GaswInput gaswInput = gaswParser.getGaswInput(applicationName, inputsMap, boutiquesFilePath, executableName,
                    inputBoutiquesId, outputBoutiquesId, invocationString, resultsDirectory, jobId, scriptLoader.loadBashScript(), downloadFiles, outputDirName);
            gasw.submit(gaswInput);
            System.out.println("Job launched: " + jobId);
        }
    }

    /**
     * Validates the workflowId as a non-empty string.
     * @param workflowId The workflow ID to validate.
     * @return true if the workflowId is valid, false otherwise.
     */
}
