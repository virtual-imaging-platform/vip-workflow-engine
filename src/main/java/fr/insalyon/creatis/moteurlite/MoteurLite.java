package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.Input;
import fr.insalyon.creatis.moteurlite.inputsParser.InputsFileParser;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategy;


/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {
    private Gasw gasw = Gasw.getInstance();
    private IterationStrategy iterationStrategy = new IterationStrategy();
    private Workflowsdb workflowsdb = new Workflowsdb();

    private int sizeOfInputs;
    private String applicationName;
    private String boutiquesFilePath;
    private String executableName;
    private HashMap<Integer, String> inputBoutiquesId;
    private String workflowId;
    String inputsFilePath;

    private Map<String, String> inputTypes = new HashMap<>();
    private Map<String, String> resultsDirectory;
    private List<Map<String, String>> inputMap;

    private List<Map<String, String>> inputData;
    


    public static void main(String[] args) throws Exception {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws Exception {
        // Verify arguments
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        }
        workflowId = args[0];
        boutiquesFilePath = args[1];
        inputsFilePath = args[2];

        checkArguments(args);

        // Initialize boutiques.json and inputs.xml
        ObjectMapper objectMapper = new ObjectMapper();
        BoutiquesDescriptor descriptor = objectMapper.readValue(new File(boutiquesFilePath), BoutiquesDescriptor.class);

        applicationName =  descriptor.getName();
        executableName = applicationName + ".json";

        Set<String> inputOptional = new HashSet<>();

        for (Input input : descriptor.getInputs()) {
            Input.Type inputType = input.getType();
            if (Boolean.TRUE.equals(input.getOptional())) { 
                inputOptional.add(input.getName());
            }
            
            inputTypes.put(input.getName(), inputType.toString());
        }
        System.out.println("inputTypes: " + inputTypes);
        System.out.println("optinput: " + inputOptional);

        Map<String, Object> customProperties = (descriptor.getCustom() != null && descriptor.getCustom().getAdditionalProperties() != null)
        ? descriptor.getCustom().getAdditionalProperties()
        : new HashMap<>();
        
            
        init(boutiquesFilePath, inputsFilePath);

        // Set workflowsdb
        workflowsdb.persistInputs(workflowId, inputData, inputTypes, resultsDirectory);

        // Set iteration strategy
        List<Map<String, String>> jsonIterations = iterationStrategy.IterationStratergy(inputData, resultsDirectory, customProperties, inputOptional);
        System.out.println("jsonIterations: " + jsonIterations);
        sizeOfInputs = jsonIterations.size();

        // Set gasw monitoring
        GaswMonitor gaswMonitor = new GaswMonitor(workflowId, applicationName, sizeOfInputs, gasw);
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        
        // Create jobs
        createJobs(jsonIterations);
    }


    private void checkArguments(String[] args) {
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
        if ( ! boutiquesFilePath.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid boutiquesFilePath. It should be a JSON file.");
        }

        // Validate inputsFilePath as an XML file
        if ( ! inputsFilePath.endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid inputsFilePath. It should be an XML file.");
        }
    }

    private void init(String boutiquesFilePath, String inputsFilePath) throws Exception {
        // Parse inputs
        InputsFileParser inputsFileParser = new InputsFileParser(inputsFilePath);
        inputData = inputsFileParser.getInputData();
        resultsDirectory = inputsFileParser.getResultDirectory();
        inputMap = inputsFileParser.getInputData();
     }

    public void createJobs(List<Map<String, String>> inputs) throws Exception {
        for (Map<String, String> innerList : inputs) {
            Map<String, String> inputsMap = new HashMap<>();
            Map<String, String> invocation = new HashMap<>();

            for (Map.Entry<String, String> entry : innerList.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().equals("results-directory")) {
                    invocation.put(entry.getKey(), entry.getValue());
                }
            }

             // Extract the value of result-directory from resultsDirectory map
            List<URI> downloads = InputsFileParser.getDownloadFiles(inputsMap);
            URI resultsDirectoryURI = new URI(resultsDirectory.get("results-directory"));

            String invocationString = CreateInvocation.convertMapToJson(invocation, inputTypes);
            String jobId = applicationName + "-" + System.nanoTime() + ".sh";

            // Convert boutiquesFilePath to absolute path
            File boutiquesFile = new File(boutiquesFilePath);
            if (!boutiquesFile.isAbsolute()) {
                boutiquesFilePath = boutiquesFile.getAbsolutePath();
            }
            System.out.println("applicationName: " + applicationName);
            System.out.println("executableName: " + executableName);
            System.out.println("downloads: " + downloads);
            System.out.println("resultsDirectoryURI: " + resultsDirectoryURI);
            System.out.println("invocationString: " + invocationString);
            System.out.println("jobId: " + jobId);
            GaswInput gaswInput = new GaswInput(applicationName, executableName, downloads, resultsDirectoryURI, invocationString, jobId);
            gasw.submit(gaswInput);
        }
    }
}
