package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.DataType;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.InputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Output;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.OutputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Processor;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.ProcessorID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Workflow;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.WorkflowStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.InputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.OutputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.ProcessorDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class WorkflowsDbRepository {

    private static WorkflowsDbRepository instance;

    private InputDAO inputDAO;
    private OutputDAO outputDAO;
    private ProcessorDAO processorDAO;
    private WorkflowDAO workflowDAO;

    // Private constructor for Singleton
    private WorkflowsDbRepository() throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        this.inputDAO = workflowsDBDAOFactory.getInputDAO();
        this.outputDAO = workflowsDBDAOFactory.getOutputDAO();
        this.processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        this.workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
    }

    // Get the Singleton instance
    public static WorkflowsDbRepository getInstance() throws Exception {
        if (instance == null) {
            instance = new WorkflowsDbRepository();
        }
        return instance;
    }

    public void persistInputs(String workflowId, Map<String, List<String>> inputValues, Map<String, fr.insalyon.creatis.moteurlite.boutiques.Input.Type> inputType) throws Exception {
        Input input = new Input();
        InputID inputID = new InputID();
    
        for (Map.Entry<String, List<String>> entry : inputValues.entrySet()) {
            String key = entry.getKey();  // This is the input key
            List<String> values = entry.getValue();  // List of values associated with the key
            fr.insalyon.creatis.moteurlite.boutiques.Input.Type type = inputType.get(key);
            
            // Iterate over all values in the list
            for (String value : values) {
                // Set InputID
                inputID.setWorkflowID(workflowId);
                inputID.setPath(value);
                inputID.setProcessor(key);

                // Set input properties based on type
                if (type == fr.insalyon.creatis.moteurlite.boutiques.Input.Type.FILE) {
                    input.setType(DataType.URI);
                } else {
                    input.setType(DataType.String);
                }

                input.setInputID(inputID);

                // Add to InputDAO
                inputDAO.add(input);
            }
        }
    }


    public void persistOutputs(String workflowId, HashMap<Integer, String> outputData, List<URI> uploadList) throws Exception {
        Output output = new Output();
        OutputID outputID = new OutputID();
        for (Map.Entry<Integer, String> entry : outputData.entrySet()) {
            outputID.setWorkflowID(workflowId);
            outputID.setProcessor(entry.getValue());
            outputID.setPath("");
            output.setOutputID(outputID);
            output.setType(DataType.String);

            int index = entry.getKey();
            if (index < uploadList.size()) {
                URI uploadURI = uploadList.get(index);
                outputID.setPath(uploadURI.toString());
            }
            outputDAO.add(output);
        }
    }

    public void persistProcessors(String workflowId, String applicationName, Integer queued, Integer completed, Integer failed) throws Exception {
        try {
            // Fetch existing processor entity by workflowId and processor name (applicationName)
            Processor existingProcessor = processorDAO.get(workflowId, applicationName);

            if (existingProcessor != null) {
                // Update the existing processor with new values
                existingProcessor.setQueued(queued);
                existingProcessor.setCompleted(completed);
                existingProcessor.setFailed(failed);
                processorDAO.update(existingProcessor);
            } else {
                // If the processor does not exist, add a new one
                Processor processors = new Processor();
                ProcessorID processorID = new ProcessorID();
                processorID.setWorkflowID(workflowId);
                processorID.setProcessor(applicationName);
                processors.setProcessorID(processorID);
                processors.setQueued(queued);
                processors.setCompleted(completed);
                processors.setFailed(failed);
                processorDAO.add(processors);
            }
        } catch (WorkflowsDBDAOException e) {
            throw new RuntimeException("Failed to persist processor: " + e.getMessage(), e);
        }
    }


    public void persistWorkflows(String workflowId, GaswStatus status) throws Exception {
        // Determine the final status based on GaswStatus
        WorkflowStatus finalStatus = WorkflowStatus.Unknown;
        if (status.equals(GaswStatus.COMPLETED)) {
            finalStatus = WorkflowStatus.Completed;
        } else if (status.equals(GaswStatus.ERROR)) {
            finalStatus = WorkflowStatus.Failed;
        }

        // Fetch the existing Workflow entity by its ID
        Workflow workflow = workflowDAO.get(workflowId);

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = formatter.format(currentDate);
        Date parsedDate = formatter.parse(formattedDateTime);

        // Update only the status of the Workflow entity
        if (workflow != null) {
            workflow.setStatus(finalStatus);
            workflow.setFinishedTime(parsedDate);
            workflowDAO.update(workflow);
        } else {
            throw new Exception("Workflow with ID " + workflowId + " not found.");
        }
    }
}
