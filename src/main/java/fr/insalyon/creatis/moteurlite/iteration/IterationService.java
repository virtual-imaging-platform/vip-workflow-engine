package fr.insalyon.creatis.moteurlite.iteration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.insalyon.creatis.boutiques.BoutiquesService;
import fr.insalyon.creatis.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.MoteurLiteConstants;

public class IterationService {
    private final BoutiquesService boutiquesService;
    private final IterationTypes iterationTypes;

    public IterationService(BoutiquesService boutiquesService) {
        this.boutiquesService = boutiquesService;
        this.iterationTypes = new IterationTypes();
    }

    public List<Map<String, String>> compute(Map<String, List<String>> inputsMap, BoutiquesDescriptor boutiquesDescriptor, int maxJobs) throws MoteurLiteException {
        Set<String> crossKeys = boutiquesService.getCrossMap(boutiquesDescriptor);
        Set<String> dotKeys = boutiquesService.getDotMap(boutiquesDescriptor);
        Set<String> allKeys = new HashSet<>(inputsMap.keySet());
        Set<String> optionalKeys = boutiquesService.getInputOptionalOfBoutiquesFile(boutiquesDescriptor);
        Set<String> defaultValueKeys = boutiquesService.getInputDefaultOfBoutiquesFile(boutiquesDescriptor);

        // for removing optional empty inputs from dotKeys to avoid iteration with inputs
        // of different size (that will lead to failure)
        removeEmptyOptionalKeys(dotKeys, inputsMap, optionalKeys);

        allKeys.removeAll(crossKeys);
        allKeys.removeAll(dotKeys);

        dotKeys.retainAll(inputsMap.keySet());
        crossKeys.retainAll(inputsMap.keySet());
        crossKeys.addAll(allKeys);

        Set<String> resultsDirs = new HashSet<>(inputsMap.getOrDefault(MoteurLiteConstants.RESULTS_DIRECTORY, Collections.emptyList()));
        if (resultsDirs.size() > 1) {
            dotKeys.add(MoteurLiteConstants.RESULTS_DIRECTORY);
            crossKeys.remove(MoteurLiteConstants.RESULTS_DIRECTORY);
        }

        List<Map<String, String>> dotCombinations = iterationTypes.dot(getSelectedMap(inputsMap, dotKeys), defaultValueKeys);
        List<Map<String, String>> crossCombinations = iterationTypes.cross(getSelectedMap(inputsMap, crossKeys), maxJobs);
        List<Map<String, String>> resultCombinations = iterationTypes.cross(dotCombinations, crossCombinations, maxJobs);

        return resultCombinations;
    }

    private Map<String, List<String>> getSelectedMap(Map<String, List<String>> inputMap, Set<String> keys) {
        return inputMap.entrySet().stream()
            .filter(entry -> keys.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void removeEmptyOptionalKeys(Set<String> keys, Map<String, List<String>> inputs, Set<String> optionalKeys) {
        for (String key : optionalKeys) {
            if (inputs.get(key) == null || inputs.get(key).isEmpty()) {
                keys.remove(key);
            }
        }
    }
}
