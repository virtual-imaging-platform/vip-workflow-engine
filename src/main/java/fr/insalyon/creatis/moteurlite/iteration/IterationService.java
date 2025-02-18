package fr.insalyon.creatis.moteurlite.iteration;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.boutiques.BoutiquesService;
import fr.insalyon.creatis.moteurlite.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.model.Custom;

public class IterationService {
    private final BoutiquesService boutiquesService;
    private final IterationTypes iterationTypes;

    public IterationService(BoutiquesService boutiquesService) {
        this.boutiquesService = boutiquesService;
        this.iterationTypes = new IterationTypes();
    }

    private Map<String, List<String>> expand(Map<String, List<String>> inputsMap,
                                             BoutiquesDescriptor boutiquesDescriptor) {
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom == null)
            return inputsMap;
        // "vip:dir":{ "inputs":[ {"name":"input1", "patterns":["*.nii","*.nii.gz"] }, ...]}
        Custom.VipListDir vipdir = custom.vipListDir;
        if (vipdir == null)
            return inputsMap;
        List<Custom.VipListDir.VipListDirInput> inputs = vipdir.inputs;
        if (inputs == null)
            return inputsMap;
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (Custom.VipListDir.VipListDirInput input: inputs) {
            List<String> inputPathsList = inputsMap.get(input.name);
            if (inputPathsList == null || input.patterns == null || input.patterns.isEmpty())
                continue;
            List<String> realFilesList = new ArrayList<String>();
            for (String pathName: inputPathsList) {
                // if pathName is "file:" or "lfn:" and isDirectory(pathName) and patterns are defined
                List<String> dirFiles = new ArrayList<>(); // XXX Grida isdir+ls here
                //  List<GridData> files = client.getFolderData(dirname, true);
                //  for (GridData file : files) {
                //    if (file.getType() == GridData.Type.File) {
                for (String filename: input.patterns) { // XXX compute the list of matchers just once
                    for (String pattern: input.patterns) {
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
                        if (matcher.matches(Paths.get(filename))) {
                            realFilesList.add(filename); // watch for file:/lfn: prefix
                        }
                    }
                }
            }
            inputsMap.put(input.name, inputPathsList);
        }
        return result;
    }

    public List<Map<String, String>> compute(Map<String, List<String>> inputsMap, BoutiquesDescriptor boutiquesDescriptor) throws MoteurLiteException {
        inputsMap = expand(inputsMap, boutiquesDescriptor);
        Set<String> crossKeys = boutiquesService.getCrossMap(boutiquesDescriptor);
        Set<String> dotKeys = boutiquesService.getDotMap(boutiquesDescriptor);
        Set<String> allKeys = new HashSet<>(inputsMap.keySet());

        allKeys.removeAll(crossKeys);
        allKeys.removeAll(dotKeys);

        dotKeys.retainAll(inputsMap.keySet());
        crossKeys.retainAll(inputsMap.keySet());
        crossKeys.addAll(allKeys);

        List<Map<String, String>> dotCombinations = iterationTypes.dot(getSelectedMap(inputsMap, dotKeys));
        List<Map<String, String>> crossCombinations = iterationTypes.cross(getSelectedMap(inputsMap, crossKeys));
        List<Map<String, String>> resultCombinations = iterationTypes.cross(dotCombinations, crossCombinations);

        return resultCombinations;
    }

    private Map<String, List<String>> getSelectedMap(Map<String, List<String>> inputMap, Set<String> keys) {
        return inputMap.entrySet().stream()
            .filter(entry -> keys.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
