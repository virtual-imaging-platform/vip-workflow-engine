package fr.insalyon.creatis.moteurlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.moteurlite.boutiquesParser.ParseBoutiquesFile;
import fr.insalyon.creatis.moteurlite.inputsParser.ParseInputsFile;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategy;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {
    private Gasw gasw = Gasw.getInstance();
    private int sizeOfInputs;
    private String bashScript;
    IterationStrategy iterationStrategy = new IterationStrategy();
    Workflowsdb workflowsdb = new Workflowsdb();

    public static void main(String[] args) throws Exception {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws Exception {
        String workflowId = args[0];
        String boutiquesFilePath = args[1];
        String inputsFilePath = args[2];

        //load bash script
        bashScript = ScriptLoader.loadBashScript();

        //parse boutiques file
        ParseBoutiquesFile parseBoutiquesFile = new ParseBoutiquesFile(boutiquesFilePath);
        String executableName = parseBoutiquesFile.getNameOfBoutiquesFile();
        String applicationName = parseBoutiquesFile.getApplicationName();
        HashMap<Integer, String> inputBoutiquesId = parseBoutiquesFile.getinputIdOfBoutiquesFile();
        HashMap<Integer, String> outputBoutiquesId = parseBoutiquesFile.getoutputIdOfBoutiquesFile();
        HashMap<String, String> inputBoutiquesType = parseBoutiquesFile.getinputTypeOfBoutiquesFile();
        Set<String> getCrossMap = parseBoutiquesFile.getCrossMap();
        Set<String> getDotMap = parseBoutiquesFile.getDotMap();
        Set<String> inputOptional = parseBoutiquesFile.getinputOptionalOfBoutiquesFile();
        
        //parse inputs
        ParseInputsFile inputsParser = new ParseInputsFile(inputsFilePath);
        List<Map<String, String>> inputData = inputsParser.getInputData();
        //Map<String, String> inputType = inputsParser.getInputTypeMap();
        Map<String, String> inputType = inputBoutiquesType;
        Map<String, String> resultsDirectory = inputsParser.getResultDirectory();


        //set workflowsdb
        workflowsdb.persistInputs(workflowId, inputData, inputType, resultsDirectory);

        //set iteration strategy
        iterationStrategy.IterationStratergy(inputData, resultsDirectory, getCrossMap, getDotMap, inputOptional);
        List<Map<String, String>> jsonIterations = iterationStrategy.getJsonIterations();
        List<Map<String, String>> inputs = new ArrayList<>();
        inputs = jsonIterations;
        sizeOfInputs = inputs.size();

        //set gasw monitoring
        GaswMonitor gaswMonitor = new GaswMonitor(workflowId, applicationName, outputBoutiquesId, sizeOfInputs, gasw);
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();

        //create jobs
        JobCreator.createJobs(inputs, applicationName, boutiquesFilePath, executableName, inputBoutiquesId, outputBoutiquesId, inputType, resultsDirectory, workflowId, gasw, bashScript);
    }
}