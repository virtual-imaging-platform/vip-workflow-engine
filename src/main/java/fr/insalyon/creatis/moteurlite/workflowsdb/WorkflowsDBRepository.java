package fr.insalyon.creatis.moteurlite.workflowsdb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

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
import fr.insalyon.creatis.moteurlite.MoteurLiteConstants;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;

import org.apache.log4j.Logger;

public class WorkflowsDBRepository {

    private static final Logger logger = Logger.getLogger(WorkflowsDBRepository.class);

    private static WorkflowsDBRepository instance;

    private final InputDAO inputDAO;
    private final OutputDAO outputDAO;
    private final ProcessorDAO processorDAO;
    private final WorkflowDAO workflowDAO;

    private WorkflowsDBRepository() throws WorkflowsDBDAOException, WorkflowsDBException {
        this(new WorkflowsDBDAOFactory());
    }

    private WorkflowsDBRepository(WorkflowsDBDAOFactory workflowsDBDAOFactory) throws WorkflowsDBDAOException, WorkflowsDBException {
        this(workflowsDBDAOFactory.getInputDAO(),
                workflowsDBDAOFactory.getOutputDAO(),
                workflowsDBDAOFactory.getProcessorDAO(),
                workflowsDBDAOFactory.getWorkflowDAO());
    }

    // not private to allow usage from same package in tests
    WorkflowsDBRepository(InputDAO inputDAO, OutputDAO outputDAO, ProcessorDAO processorDAO, WorkflowDAO workflowDAO) throws WorkflowsDBDAOException, WorkflowsDBException {
        this.inputDAO = inputDAO;
        this.outputDAO = outputDAO;
        this.processorDAO = processorDAO;
        this.workflowDAO = workflowDAO;
    }

    public static WorkflowsDBRepository getInstance() throws WorkflowsDBDAOException, WorkflowsDBException {
        if (instance == null) {
            instance = new WorkflowsDBRepository();
        }
        return instance;
    }

    public void persistInputs(
            String workflowId, Map<String, List<String>> inputValues,
            Map<String, fr.insalyon.creatis.boutiques.model.Input> boutiquesInputs) throws MoteurLiteException {
        Input input = new Input();
        InputID inputID = new InputID();

        for (Map.Entry<String, List<String>> entry : inputValues.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            for (String value : values) {
                DataType type = getWorkflowsDBType(key, boutiquesInputs);
                String path = DataType.URI.equals(type) ? adaptShanoirOrGirderIOValue(value) : value;

                inputID.setWorkflowID(workflowId);
                inputID.setPath(path);
                inputID.setProcessor(key);

                input.setType(type);
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

    private DataType getWorkflowsDBType(String boutiquesInputID, Map<String, fr.insalyon.creatis.boutiques.model.Input> boutiquesInputs) {
        if (MoteurLiteConstants.RESULTS_DIRECTORY.equals(boutiquesInputID)) {
            return DataType.URI;
        } else {
            return fr.insalyon.creatis.boutiques.model.Input.Type.FILE.equals(boutiquesInputs.get(boutiquesInputID).getType()) ? DataType.URI : DataType.String;
        }
    }

    public void persistOutputs(String workflowId, Map<String, URI> uploadMap) throws MoteurLiteException {
        Output output = new Output();
        OutputID outputID = new OutputID();

        for (String outputId : uploadMap.keySet()) {
            String path = adaptShanoirOrGirderIOValue(uploadMap.get(outputId).toString());

            outputID.setWorkflowID(workflowId);
            outputID.setProcessor(outputId);
            outputID.setPath(path);
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

    /*
        Adapt girder and shanoir input or output uri to storable uri, in particular removing the tokens
     */
    private String adaptShanoirOrGirderIOValue(String ioValue) throws MoteurLiteException {
        URI uri;
        try {
             uri = new URI(ioValue);
        } catch (URISyntaxException e) {
            return ioValue;
        }
        if (uri.getScheme() == null) {
            return ioValue;
        }
        if ("shanoir".equals(uri.getScheme())) {
            return adaptShanoirUri(uri);
        } else if ("girder".equals(uri.getScheme())) {
            return adaptGirderUri(uri);
        } else {
            return ioValue;
        }
    }

    private String adaptGirderUri(URI uri) throws MoteurLiteException {
        return selectUriQueries(uri, "apiurl", "fileId");
    }

    private String adaptShanoirUri(URI uri) throws MoteurLiteException {
        return selectUriQueries(uri, "apiUrl", "upload_url", "resourceId", "type", "format", "keycloak_client_id", "converterId");
    }

    private String selectUriQueries(URI uri, String... parametersToKeep) throws MoteurLiteException {
        if (uri.getQuery() == null) {
            return uri.toString();
        }
        List<String> queryWhiteList = Arrays.asList(parametersToKeep);
        String newQuery = Arrays.stream(uri.getQuery().split("&"))
                .filter(q -> {
                    int index = q.indexOf("=");
                    if (index < 0) return false;
                    return queryWhiteList.contains(q.substring(0,index));
                })
                .collect(Collectors.joining("&"));
        // build a new URI
        String newUri = uri.toString();
        newUri = newUri.substring(0, newUri.indexOf("?"));
        return newUri + "?" + newQuery;
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


    public void persistWorkflow(String workflowId, GaswStatus status) throws MoteurLiteException {
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
