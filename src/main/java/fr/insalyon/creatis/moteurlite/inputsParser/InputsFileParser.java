package fr.insalyon.creatis.moteurlite.inputsParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class InputsFileParser {

    private Map<String, List<String>> inputsMap;
    private Map<String, String> inputsTypeMap;

    public InputsFileParser() {
    }

    public static List<URI> getDownloadFiles(Map<String, String> inputsMap) {
        List<URI> downloads = new ArrayList<>();
        for (Map.Entry<String, String> entry : inputsMap.entrySet()) {
            if (InputDownloads.isFileURI(entry.getValue())) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
                try {
                    URI uri = new URI(entry.getValue());
                    if (uri.getScheme() == null) {
                        // Add "lfn://" prefix if URI has no scheme
                        uri = new URI("lfn://" + entry.getValue());
                    }
                    downloads.add(uri);
                } catch (Exception e) {
                    System.err.println("Error parsing URI: " + entry.getValue());
                }
            }
        }
        return downloads;
    }

    // New parse method to return a Map<String, List<String>> directly
    public Map<String, List<String>> parse(String inputsFilePath) {
        inputsMap = InputParser.parseInputData(inputsFilePath);
        return inputsMap; // Return the parsed input data as Map<String, List<String>>
    }

    public Map<String, String> parseInputTypeMap(String inputsFilePath) {
        inputsTypeMap = InputParser.parseInputType(inputsFilePath);
        return inputsTypeMap;
    }

    public Map<String, List<String>> getInputData() {
        return inputsMap;
    }

    public Map<String, String> getInputTypeMap() {
        return inputsTypeMap;
    }
}
