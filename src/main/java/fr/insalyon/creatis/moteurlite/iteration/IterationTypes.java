package fr.insalyon.creatis.moteurlite.iteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;

public class IterationTypes {

    public IterationTypes() {}

    /**
     * <p>
     * This generates combinations of inputs by matching elements across their indexes.
     * All sets must have the same length for this operation to succeed. If the input sets
     * have different lengths, an exception will be thrown.
     * </p>
     * 
     * <pre>
     * ex:
     * 
     * Input:
     * {
     *   "color": ["red", "green"],
     *   "size": [1, 2],
     *   "material": ["ice", "iron"]
     * }
     * 
     * Output:
     * [
     *   {"color": "red", "size": 1, "material": "ice"},
     *   {"color": "green", "size": 2, "material": "iron"}
     * ]
     * </pre>
     *
     */
    public List<Map<String, String>> dot(Map<String, List<String>> inputs) throws MoteurLiteException {
        List<Map<String, String>> combinations = new ArrayList<>();

        if (inputs.size() == 0)
            return combinations;

        boolean sameSize = inputs.values().stream().mapToInt(List::size).distinct().count() == 1;
        int size = inputs.values().stream().mapToInt(List::size).min().getAsInt();

        if (!sameSize) {
            throw new MoteurLiteException("Dot iteration is impossible with inputs of different size!");
        }
        for (int i = 0; i < size; i++) {
            Map<String, String> combination = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : inputs.entrySet()) {
                combination.put(entry.getKey(), entry.getValue().get(i));
            }
            combinations.add(combination);
        }
        return combinations;
    }

    /**
     * <p>
     * This performs a cartesian product-like operation. <br>
     * Each element from one set is matched with every element of the other sets to
     * generate all possible combinations.
     * </p>
     * 
     * <pre>
     * ex:
     * 
     * Input:
     * {
     *   "color": ["red", "green", "yellow"],
     *   "size": [1, 2, 3]
     * }
     * 
     * Output:
     * [
     *   {"color": "red", "size": 1},
     *   {"color": "red", "size": 2},
     *   {"color": "red", "size": 3},
     *   {"color": "green", "size": 1},
     *   {"color": "green", "size": 2},
     *   {"color": "green", "size": 3},
     *   {"color": "yellow", "size": 1},
     *   {"color": "yellow", "size": 2},
     *   {"color": "yellow", "size": 3}
     * ]
     * </pre>
     */
    public List<Map<String, String>> cross(Map<String, List<String>> inputs) {
        List<Map<String, String>> combinations = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : inputs.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (combinations.isEmpty()) {
                for (String value : values) {
                    Map<String, String> combination = new HashMap<>();
                    combination.put(key, value);
                    combinations.add(combination);
                }
            } else {
                List<Map<String, String>> temp = new ArrayList<>(combinations);
                combinations.clear();
                for (Map<String, String> combination : temp) {
                    for (String value : values) {
                        Map<String, String> newCombination = new HashMap<>(combination);
                        newCombination.put(key, value);
                        combinations.add(newCombination);
                    }
                }
            }
        }
        return combinations;
    }

    /**
     * <p>
     * This fonction does exactly the same than the previous one, the only difference is the inputs types. 
     * This used to cross existing dot and cross combinations.   
     * </p>
     */
    public List<Map<String, String>> cross(List<Map<String, String>> combinationsA, List<Map<String, String>> combinationsB) {
        List<Map<String, String>> result = new ArrayList<>();

        if (combinationsA.isEmpty()) {
            return combinationsB;
        } else if (combinationsB.isEmpty()) {
            return combinationsA;
        } else {
            for (Map<String, String> mapA : combinationsA) {
                for (Map<String, String> mapB : combinationsB) {
                    Map<String, String> combined = new HashMap<>(mapA);
    
                    combined.putAll(mapB);
                    result.add(combined);
                }
            }
            return result;
        }
    }
}
