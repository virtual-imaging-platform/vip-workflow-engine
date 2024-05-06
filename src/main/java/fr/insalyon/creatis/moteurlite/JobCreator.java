package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.moteurlite.inputsParser.ParseInputsFile;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

class JobCreator {
    public static void createJobs(List<Map<String, String>> inputs, String applicationName, String boutiquesFilePath, String executableName, HashMap<Integer, String> inputBoutiquesId, HashMap<Integer, String> outputBoutiquesId, Map<String, String> inputType, Map<String, String> resultsDirectory, String workflowId, Gasw gasw, String bashScript) throws Exception {
        
        Workflowsdb workflowsdb = new Workflowsdb();
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
                    inputBoutiquesId, outputBoutiquesId, invocationString, resultsDirectory, jobId, bashScript, downloadFiles, outputDirName);
            gasw.submit(gaswInput);
            System.out.println("Job launched: " + jobId);
        }
    }
}







