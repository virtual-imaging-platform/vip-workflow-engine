package fr.insalyon.creatis.moteurlite.custom;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.model.Custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class IntIteratorInputsService {
    public IntIteratorInputsService() {}

    /**
     * <p>
     * Expand a single-valued integer input parameter N>0 into N values from 0 to N-1.
     * </p>
     * <pre>
     * "vip:intIteratorInputs":["input1"]
     * </pre>
     */
    public void updateInputs(Map<String, List<String>> inputsMap,
                             BoutiquesDescriptor boutiquesDescriptor)
            throws MoteurLiteException {
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom == null) {
            return;
        }
        List<String> intIteratorInputs = custom.getIntIteratorInputs();
        if (intIteratorInputs == null || intIteratorInputs.isEmpty()) {
            return;
        }
        for (String inputId : inputsMap.keySet()) {
            List<String> values = inputsMap.get(inputId);
            if (intIteratorInputs.contains(inputId)) { // input is an iterator
                // There must be a single value
                if (values.size() != 1) {
                    throw new MoteurLiteException("vip:intIteratorInputs: multiple values for input " + inputId);
                }
                // Parse: value must be a strictly positive integer.
                // Let parseInt raise NumberFormatException on non-integer strings,
                // and raise our own NumberFormatException on negative integers.
                String strValue = values.getFirst();
                int nSteps;
                try {
                    nSteps = Integer.parseInt(strValue);
                    if (nSteps <= 0) {
                        throw new MoteurLiteException("vip:intIteratorInputs: negative value for input " + inputId);
                    }
                } catch (NumberFormatException e) {
                    throw new MoteurLiteException("vip:intIteratorInputs: invalid value '" + strValue + "' for input " + inputId);
                }
                // Generate values list, from 0 to N-1
                List<String> steps = IntStream.range(0, nSteps).mapToObj(Integer::toString).toList();
                inputsMap.put(inputId, steps);
            }
        }
    }
}
