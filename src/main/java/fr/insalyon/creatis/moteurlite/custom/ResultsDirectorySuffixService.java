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

public class ResultsDirectorySuffixService {
    public ResultsDirectorySuffixService() {}

    /**
     * <p>
     * Appends a timestamp or a custom subdirectory (possibly empty) to the RESULTS_DIRECTORY input.
     * </p>
     * <pre>
     * "vip:resultsDirectorySuffix":"my/subdir"
     * </pre>
     */
    public void updateInputs(Map<String, List<String>> inputsMap,
                             BoutiquesDescriptor boutiquesDescriptor) {
        String suffix = null;
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom != null) {
            suffix = custom.getResultsDirectorySuffix();
        }
        if (suffix == null) {
            // default behaviour: append a timestamp to all results-directory values
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            suffix = dateFormat.format(System.currentTimeMillis());
        } else if (suffix.isEmpty()) {
            // no suffix, keep results-directory unchanged
            return;
        }
        // append suffix to all values of the results-directory input
        final String inputId = MoteurLiteConstants.RESULTS_DIRECTORY;
        List<String> values = inputsMap.get(inputId);
        List<String> newValues = new ArrayList<>();
        for (String value : values) {
            if (value.startsWith("lfn:") || value.startsWith("file:")) {
                value = String.valueOf(Paths.get(value, suffix));
            }
            newValues.add(value);
        }
        inputsMap.put(inputId, newValues);
    }
}
