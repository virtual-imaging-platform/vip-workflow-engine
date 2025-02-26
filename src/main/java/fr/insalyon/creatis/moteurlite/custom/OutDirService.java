package fr.insalyon.creatis.moteurlite.custom;

import fr.insalyon.creatis.moteurlite.MoteurLiteConstants;
import fr.insalyon.creatis.moteurlite.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.model.Custom;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutDirService {
    public OutDirService() {}

    public Map<String, List<String>> resultsDirectory(Map<String, List<String>> inputsMap,
                                                      BoutiquesDescriptor boutiquesDescriptor) {
        String suffix = null;
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom != null) {
            suffix = custom.getOutDir();
        }
        if (suffix == null) {
            // default behaviour: append a timestamp to all results-directory values
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            suffix = dateFormat.format(System.currentTimeMillis());
        } else if (suffix.isEmpty()) {
            // no suffix, keep results-directory unchanged
            return inputsMap;
        }
        Map<String, List<String>> result = new HashMap<>();
        for (String inputId : inputsMap.keySet()) {
            List<String> values = inputsMap.get(inputId);
            if (MoteurLiteConstants.RESULTS_DIRECTORY.equals(inputId)) {
                List<String> newValues = new ArrayList<>();
                for (String value : values) {
                    if (value.startsWith("lfn:") || value.startsWith("file:")) {
                        value = String.valueOf(Paths.get(value, suffix));
                    }
                    newValues.add(value);
                }
                result.put(inputId, newValues);
            } else { // keep other inputs as is
                result.put(inputId, values);
            }
        }
        return result;
    }
}
