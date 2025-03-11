package fr.insalyon.creatis.moteurlite.custom;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.model.Custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class IntIteratorService {
    public IntIteratorService() {}

    /**
     * <p>
     * Expand a single-valued integer input parameter N>0 into N values from 0 to N-1.
     * </p>
     * <pre>
     * "vip:intIterator":["input1"]
     * </pre>
     */
    public Map<String, List<String>> iterate(Map<String, List<String>> inputsMap,
                                             BoutiquesDescriptor boutiquesDescriptor)
            throws MoteurLiteException {
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom == null) {
            return inputsMap;
        }
        List<String> intIterators = custom.getIntIterator();
        if (intIterators == null || intIterators.isEmpty()) {
            return inputsMap;
        }
        Map<String, List<String>> result = new HashMap<>();
        for (String inputId : inputsMap.keySet()) {
            List<String> values = inputsMap.get(inputId);
            if (intIterators.contains(inputId)) { // input is an iterator
                // There must be a single value
                if (values.size() != 1) {
                    throw new MoteurLiteException("vip:intIterator: multiple values for input " + inputId);
                }
                // Parse: value must be a strictly positive integer.
                // Let parseInt raise NumberFormatException on non-integer strings,
                // and raise our own NumberFormatException on negative integers.
                String strValue = values.getFirst();
                int nSteps;
                try {
                    nSteps = Integer.parseInt(strValue);
                    if (nSteps <= 0) {
                        throw new MoteurLiteException("vip:intIterator: negative value for input " + inputId);
                    }
                } catch (NumberFormatException e) {
                    throw new MoteurLiteException("vip:intIterator: invalid value '" + strValue + "' for input " + inputId);
                }
                // Generate values list, from 0 to N-1
                List<String> steps = IntStream.range(0, nSteps).mapToObj(Integer::toString).toList();
                result.put(inputId, steps);
            } else { // keep other inputs as is
                result.put(inputId, values);
            }
        }
        return result;
    }
}
