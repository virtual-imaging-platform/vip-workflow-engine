package fr.insalyon.creatis.moteurlite.iterationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Sandesh Patil [https://github.com/sandepat]
 */

public class IterationStrategy {
    private List<Map<String, String>> crossCombinations = new ArrayList<>();
    private List<Map<String, String>> dotCombinations = new ArrayList<>();
    private List<Map<String, String>> jsonCombinations = new ArrayList<>();

    public List<Map<String, String>> IterationStratergy(Map<String, List<String>> inputsMap, Map<String, Object> customProperties, Set<String> inputOptional) {
        // Extract result-directory from inputsMap
        Map<String, String> resultDir = new HashMap<>();
        if (inputsMap.containsKey("results-directory")) {
            List<String> resultDirs = inputsMap.get("results-directory");
            if (resultDirs != null && !resultDirs.isEmpty()) {
                resultDir.put("results-directory", resultDirs.get(0));  // Assuming there is only one result-directory
            }
        }

        // Create crossJson and dotJson from customProperties
        Set<String> crossJson = extractCustomValues(customProperties, "VIPcross");
        Set<String> dotJson = extractCustomValues(customProperties, "VIPdot");

        crossCombinations = setCrossIteration(inputsMap, resultDir);
        dotCombinations = setDotIteration(inputsMap, resultDir);
        jsonCombinations = setJsonIteration(inputsMap, resultDir, crossJson, dotJson, inputOptional);

        return jsonCombinations;
    }

    private Set<String> extractCustomValues(Map<String, Object> customProperties, String key) {
        Set<String> values = new HashSet<>();
        
        // Check if the custom property contains the key (e.g., "VIPcross" or "VIPdot")
        if (customProperties.containsKey(key)) {
            Object customValue = customProperties.get(key);

            if (customValue instanceof List<?>) {
                for (Object value : (List<?>) customValue) {
                    if (value instanceof String) {
                        values.add((String) value);
                    }
                }
            }
        }
        return values;
    }

    private List<Map<String, String>> setCrossIteration(Map<String, List<String>> inputsMap, Map<String, String> resultDir) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : inputsMap.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            valuesMap.put(key, values);
        }
        List<Map<String, String>> crossCombinations = CrossIteration.crossIteration(valuesMap);
        addResultsDir(crossCombinations, resultDir);
        return crossCombinations;
    }

    private List<Map<String, String>> setDotIteration(Map<String, List<String>> inputsMap, Map<String, String> resultDir) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : inputsMap.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            valuesMap.put(key, values);
        }
        List<Map<String, String>> dotCombinations = DotIteration.dotIteration(valuesMap);
        addResultsDir(dotCombinations, resultDir);
        return dotCombinations;
    }

    private List<Map<String, String>> setJsonIteration(Map<String, List<String>> inputsMap, Map<String, String> resultDir, Set<String> crossJson, Set<String> dotJson, Set<String> inputOptional) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : inputsMap.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            valuesMap.put(key, values);
        }
        List<Map<String, String>> jsonCombinations = JsonIteration.jsonIteration(valuesMap, crossJson, dotJson, inputOptional);
        addResultsDir(jsonCombinations, resultDir);
        return jsonCombinations;
    }

    private static void addResultsDir(List<Map<String, String>> combinations, Map<String, String> resultDir) {
        if (resultDir != null && !resultDir.isEmpty()) {
            for (Map<String, String> map : combinations) {
                map.putAll(resultDir);
            }
        }
    }

    public List<Map<String, String>> getJsonIterations() {
        return jsonCombinations;
    }

    public List<Map<String, String>> getCrossCombinations() {
        return crossCombinations;
    }

    public List<Map<String, String>> getDotCombinations() {
        return dotCombinations;
    }
}
