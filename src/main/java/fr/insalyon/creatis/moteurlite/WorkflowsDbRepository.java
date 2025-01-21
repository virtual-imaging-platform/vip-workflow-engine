package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.*;

import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBException;
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
import fr.insalyon.creatis.moteurlite.boutiques.scheme.OutputFile;

import org.apache.log4j.Logger;

public class WorkflowsDbRepository {

    private static final Logger logger = Logger.getLogger(WorkflowsDbRepository.class);

    private static WorkflowsDbRepository instance;

    private final InputDAO inputDAO;
    private final OutputDAO outputDAO;
    private final ProcessorDAO processorDAO;
    private final WorkflowDAO workflowDAO;

    private WorkflowsDbRepository() throws WorkflowsDBDAOException, WorkflowsDBException {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        this.inputDAO = workflowsDBDAOFactory.getInputDAO();
        this.outputDAO = workflowsDBDAOFactory.getOutputDAO();
        this.processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        this.workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
    }

    public static WorkflowsDbRepository getInstance() throws WorkflowsDBDAOException, WorkflowsDBException {
        if (instance == null) {
            instance = new WorkflowsDbRepository();
        }
        return instance;
    }

    public void persistInputs(
            String workflowId, Map<String, List<String>> inputValues,
            Map<String, fr.insalyon.creatis.moteurlite.boutiques.scheme.Input> boutiquesInputs) throws MoteurLiteException {
        Input input = new Input();
        InputID inputID = new InputID();

        for (Map.Entry<String, List<String>> entry : inputValues.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            for (String value : values) {
                inputID.setWorkflowID(workflowId);
                inputID.setPath(value);
                inputID.setProcessor(key);

                input.setType(getWorkflowsDBType(key, boutiquesInputs));
                input.setInputID(inputID);

                try {
                    inputDAO.add(input);
                } catch (WorkflowsDBDAOException e) {
                    logger.error("Failed to persist input: " + key + " / " + value, e);
                    throw new MoteurLiteException("Failed to persist input: " + key + " / " + value, e);
                }
            }
        }
    }

    private DataType getWorkflowsDBType(String boutiquesInputID, Map<String, fr.insalyon.creatis.moteurlite.boutiques.scheme.Input> boutiquesInputs) {
        if (MoteurLite.RESULTS_DIRECTORY.equals(boutiquesInputID)) {
            return DataType.URI;
        } else {
            return fr.insalyon.creatis.moteurlite.boutiques.scheme.Input.Type.FILE.equals(boutiquesInputs.get(boutiquesInputID).getType()) ? DataType.URI : DataType.String;
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
            Processor existingProcessor = processorDAO.get(workflowId, applicationName);

            if (existingProcessor != null) {
                existingProcessor.setQueued(queued);
                existingProcessor.setCompleted(completed);
                existingProcessor.setFailed(failed);
                processorDAO.update(existingProcessor);
            } else {
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
            WorkflowStatus finalStatus = WorkflowStatus.Unknown;
            if (status.equals(GaswStatus.COMPLETED)) {
                finalStatus = WorkflowStatus.Completed;
            } else if (status.equals(GaswStatus.ERROR)) {
                finalStatus = WorkflowStatus.Failed;
            }

            Workflow workflow = workflowDAO.get(workflowId);

            Date currentDate = new Date();

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
