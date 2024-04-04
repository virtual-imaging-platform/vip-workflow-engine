package fr.insalyon.creatis.moteurlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.DataType;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.InputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Output;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.OutputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Processor;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.ProcessorID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.InputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.OutputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.ProcessorDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;


public class Workflowsdb {

    public void Inputs(String workflowId, List<Map<String, String>> inputData, Map<String, String> inputType,
            Map<String, String> resultsDirectory, HashMap<Integer, String> outputData, Map<String, String> outputName, String outputDirName)
            throws Exception {
        persistInputs(workflowId, inputData, inputType, resultsDirectory);
        persistOutputs(workflowId, outputData, outputName, outputDirName, resultsDirectory);
        persistProcessors(workflowId, inputType);
    }

    public void persistInputs(String workflowId, List<Map<String, String>> inputData, Map<String, String> inputType,
            Map<String, String> resultsDirectory) throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        InputDAO inputDAO = workflowsDBDAOFactory.getInputDAO();
        Input input = new Input();
        InputID inputID = new InputID();
        for (Map<String, String> item : inputData) {
            String key = item.keySet().iterator().next();
            String value = item.get(key);
            String itemType = inputType.get(key);
            List<String> result = new ArrayList<>();
            result.add(workflowId);
            result.add(key);
            result.add(itemType);
            Map<String, List<String>> outputMap = new HashMap<>();
            outputMap.put(value, result);
            // Add result-input to the output map
            List<String> resultDirectoryList = new ArrayList<>();
            resultDirectoryList.add(workflowId);
            resultDirectoryList.add("result-directory");
            resultDirectoryList.add("String");
            outputMap.put(resultsDirectory.get("results-directory"), resultDirectoryList);
            for (Map.Entry<String, List<String>> entry : outputMap.entrySet()) {
                List<String> valueList = entry.getValue();
                if (valueList.size() > 3 && valueList.get(3).equals(entry.getKey())) {
                    valueList.remove(3); // Remove duplicate value from the list
                }
                inputID.setWorkflowID(valueList.get(0));
                inputID.setPath(entry.getKey());
                inputID.setProcessor(valueList.get(1));
                input.setInputID(inputID);
                if (isURL(entry.getKey())) {
                    input.setType(DataType.URI);
                } else {
                    input.setType(DataType.String);
                }
                inputDAO.add(input);
            }
        }
    }

    public void persistOutputs(String workflowId, HashMap<Integer, String> outputData, Map<String, String> outputName, String outputDirName, Map<String, String> resultsDirectory)
            throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        OutputDAO outputDAO = workflowsDBDAOFactory.getOutputDAO();
        Output output = new Output();
        OutputID outputID = new OutputID();
        String resultDir = resultsDirectory.get("results-directory");
        for (Map.Entry<Integer, String> entry : outputData.entrySet()) {
            outputID.setWorkflowID(workflowId);
            for (Map.Entry<String, String> outputEntry : outputName.entrySet()) {
                String outputKey = outputEntry.getKey();
                String outputPath = outputEntry.getValue();
                if (outputKey.equals(entry.getValue())) {
                    outputID.setPath(resultDir + "/" + outputDirName+ "/" + outputPath);
                    break;
                }
            }
            outputID.setProcessor(entry.getValue());
            output.setOutputID(outputID);
            output.setType(DataType.String);
            outputDAO.add(output);
        }
    }

    public void persistProcessors(String workflowId, Map<String, String> inputType) throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        ProcessorDAO processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        Processor processors = new Processor();
        ProcessorID processorID = new ProcessorID();
        for (Entry<String, String> entry : inputType.entrySet()) {
            processorID.setWorkflowID(workflowId);
            processorID.setProcessor(entry.getKey());
            processors.setProcessorID(processorID);
            processorDAO.add(processors);
        }
    }

    private boolean isURL(String urlString) {
        // Check if the string contains a forward slash '/'
        return urlString.contains("/");
    }
}