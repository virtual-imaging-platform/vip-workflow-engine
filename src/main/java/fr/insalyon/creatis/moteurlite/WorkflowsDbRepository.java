package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.*;

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
import fr.insalyon.creatis.moteurlite.boutiques.OutputFile;
import org.apache.log4j.Logger;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class WorkflowsDbRepository {

    private static final Logger logger = Logger.getLogger(WorkflowsDbRepository.class);

    private static WorkflowsDbRepository instance;

    private final InputDAO inputDAO;
    private final OutputDAO outputDAO;
    private final ProcessorDAO processorDAO;
    private final WorkflowDAO workflowDAO;

    // Private constructor for Singleton
    private WorkflowsDbRepository() throws WorkflowsDBDAOException {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        this.inputDAO = workflowsDBDAOFactory.getInputDAO();
        this.outputDAO = workflowsDBDAOFactory.getOutputDAO();
        this.processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        this.workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
    }

    // Get the Singleton instance
    public static WorkflowsDbRepository getInstance() throws WorkflowsDBDAOException {
        if (instance == null) {
            instance = new WorkflowsDbRepository();
        }
        return instance;
    }

    public void persistInputs(
            String workflowId, Map<String, List<String>> inputValues,
            Map<String, fr.insalyon.creatis.moteurlite.boutiques.Input> boutiquesInputs) throws MoteurLiteException {
        Input input = new Input();
        InputID inputID = new InputID();

        for (Map.Entry<String, List<String>> entry : inputValues.entrySet()) {
            String key = entry.getKey();  // This is the input key
            List<String> values = entry.getValue();  // List of values associated with the key
            fr.insalyon.creatis.moteurlite.boutiques.Input.Type type = boutiquesInputs.get(key).getType();

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
                try {
                    inputDAO.add(input);
                } catch (WorkflowsDBDAOException e) {
                    logger.error("Failed to persist input: " + key + " / " + value, e);
                    throw new MoteurLiteException("Failed to persist input: " + key + " / " + value, e);
                }
            }
        }
    }


    public void persistOutputs(String workflowId, HashMap<String, OutputFile> boutiquesOutputs, List<URI> uploadList) throws MoteurLiteException {
        Output output = new Output();
        OutputID outputID = new OutputID();

        // at the moment, we do not know which output corresponds to which upload, so we do that randomly

        List<OutputFile> boutiquesOutputList = new ArrayList<>(boutiquesOutputs.values());
        int currentIndex = -1;
        for (URI upload : uploadList) {
            currentIndex++;
            OutputFile outputFile = currentIndex < boutiquesOutputList.size() ?
                    boutiquesOutputList.get(currentIndex) :
                    boutiquesOutputList.get(0);
            outputID.setWorkflowID(workflowId);
            outputID.setProcessor(outputFile.getId());
            outputID.setPath(upload.toString());
            output.setOutputID(outputID);
            output.setType(DataType.URI);

            try {
                outputDAO.add(output);
            } catch (WorkflowsDBDAOException e) {
                logger.error("Failed to persist output: " + outputID.getProcessor() + " / " + outputID.getPath() , e);
                throw new MoteurLiteException("Failed to persist output: " + outputID.getProcessor() + " / " + outputID.getPath() , e);
            }
        }
    }

    public void persistProcessors(String workflowId, String applicationName, Integer queued, Integer completed, Integer failed) throws MoteurLiteException {
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
            logger.error("Failed to persist processor: " + applicationName, e);
            throw new MoteurLiteException("Failed to persist processor: " + applicationName, e);
        }
    }


    public void persistWorkflows(String workflowId, GaswStatus status) throws MoteurLiteException {
        try {
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

            // Update only the status of the Workflow entity
            if (workflow != null) {
                workflow.setStatus(finalStatus);
                workflow.setFinishedTime(currentDate);
                workflowDAO.update(workflow);
            } else {
                logger.error("Workflow with ID " + workflowId + " not found.");
                throw new MoteurLiteException("Workflow with ID " + workflowId + " not found.");
            }
        } catch (WorkflowsDBDAOException e) {
            logger.error("Failed to get or update workflow : " + workflowId, e);
            throw new MoteurLiteException("Failed to get or update workflow : " + workflowId, e);
        }
    }
}
