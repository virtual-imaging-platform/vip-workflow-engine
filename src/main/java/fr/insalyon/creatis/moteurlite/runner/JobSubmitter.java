package fr.insalyon.creatis.moteurlite.runner;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.moteurlite.MoteurLite;
import fr.insalyon.creatis.moteurlite.MoteurLiteConstants;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.boutiques.model.Input;

public class JobSubmitter extends Thread {
    private static final Logger logger = Logger.getLogger(MoteurLite.class);

    private final Gasw                      gasw;
    private final String                    applicationName;
    private final List<Map<String, String>> allInvocationsInputs;
    private final Map<String, Input>        boutiquesInputs;

    public JobSubmitter(Gasw gasw, String applicationName, List<Map<String, String>> allInvocationsInputs, Map<String, Input> boutiquesInputs) {
        this.gasw = gasw;
        this.applicationName = applicationName;
        this.allInvocationsInputs = allInvocationsInputs;
        this.boutiquesInputs = boutiquesInputs;
    }

    @Override
    public void run() {
        try {
            createJobs();
        } catch (MoteurLiteException e) {
            logger.error("An exception occured while submitting jobs!", e);
        }
    }

    private void createJobs() throws MoteurLiteException {
        for (Map<String, String> invocationInputs : allInvocationsInputs) {
            if (isInterrupted()) {
                break;
            } else {
                URI resultsDirectoryURI = null;
                List<URI> downloads = new ArrayList<>();
                Map<String, String> finalInvocationInputs = new HashMap<>();
    
                for (String inputId : invocationInputs.keySet()) {
                    String inputValue = invocationInputs.get(inputId);
                    if (MoteurLiteConstants.RESULTS_DIRECTORY.equals(inputId)) {
                        resultsDirectoryURI = getURI(inputValue);
                    } else {
                        if (Input.Type.FILE.equals(boutiquesInputs.get(inputId).getType())) {
                            URI downloadURI = getURI(inputValue);
                            String filename = Paths.get(downloadURI.getPath()).getFileName().toString();
                            downloads.add(downloadURI);
                            inputValue = filename;
                        }
                        finalInvocationInputs.put(inputId, inputValue);
                    }
                }

                String invocationString = convertMapToJson(finalInvocationInputs, boutiquesInputs);
                // jobId can be used for filenames, so normalize it by removing spaces
                String jobId = applicationName.replace(' ', '_') + "-" + System.nanoTime() + ".sh";
    
                submit(new GaswInput(applicationName, applicationName + ".json", downloads, resultsDirectoryURI, invocationString, jobId));
            }
        }
    }

    private void submit(GaswInput gaswInput) throws MoteurLiteException {
        try {
            gasw.submit(gaswInput);
        } catch (GaswException e) {
            logger.error("Error submitting gasw job", e);
            throw new MoteurLiteException("Error submitting gasw job", e);
        }
    }

    private URI getURI(String inputValue) throws MoteurLiteException {
        try {
            return new URI(inputValue);
        } catch (URISyntaxException e) {
            logger.error("Error parsing URI : " + inputValue, e);
            throw new MoteurLiteException("Error parsing URI : " + inputValue, e);
        }
    }

    private String convertMapToJson(Map<String, String> invocationInputs, Map<String, Input> boutiquesInputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();

        for (String inputId : invocationInputs.keySet()) {
            String value = invocationInputs.get(inputId);
            Input input = boutiquesInputs.get(inputId);
            Input.Type type = input.getType();

            if (input.getOptional() != null && input.getOptional() &&
                    value.equals(MoteurLiteConstants.INPUT_WITHOUT_VALUE)) {
                continue; // optional input with no value, skip it
            }
            if (type == Input.Type.NUMBER) {
                if (input.getInteger() != null && input.getInteger()) {
                    jsonNode.put(inputId, Integer.parseInt(value));
                } else {
                    jsonNode.put(inputId, Float.parseFloat(value));
                }
            } else if (type == Input.Type.FLAG) {
                jsonNode.put(inputId, Boolean.parseBoolean(value));
            } else {
                jsonNode.put(inputId, value);
            }
        }
        return jsonNode.toString();
    }
}
