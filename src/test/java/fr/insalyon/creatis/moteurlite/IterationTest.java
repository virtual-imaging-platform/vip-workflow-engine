package fr.insalyon.creatis.moteurlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insalyon.creatis.moteurlite.iteration.IterationTypes;

public class IterationTest {

    private Map<String, List<String>> inputsA = new HashMap<>();
    private Map<String, List<String>> inputsB = new HashMap<>();
    private Map<String, List<String>> inputsC = new HashMap<>();

    private IterationTypes types = new IterationTypes();

    private List<Map<String, String>> expectedResult = new ArrayList<>();
    private List<Map<String, String>> result;

    @BeforeEach
    public void initData() {
        inputsA.put("scan", Arrays.asList("pied", "jambe"));
        inputsA.put("color", Arrays.asList("blue", "red"));
        inputsB.put("age", Arrays.asList("1", "2", "3"));
        inputsC.put("type", Arrays.asList("metal", "iron"));
    }

    @Test
    public void crossInput() {
        inputsA.putAll(inputsB);
        result = types.cross(inputsA);

        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "1"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "2"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "3"));
        expectedResult.add(Map.of("scan", "pied", "color", "red", "age", "1"));
        expectedResult.add(Map.of("scan", "pied", "color", "red", "age", "2"));
        expectedResult.add(Map.of("scan", "pied", "color", "red", "age", "3"));
        expectedResult.add(Map.of("scan", "jambe", "color", "blue", "age", "1"));
        expectedResult.add(Map.of("scan", "jambe", "color", "blue", "age", "2"));
        expectedResult.add(Map.of("scan", "jambe", "color", "blue", "age", "3"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "1"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "2"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "3"));
        assertEquals(12, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    public void dotInput() throws MoteurLiteException {
        result = types.dot(inputsA);

        expectedResult.add(Map.of("scan", "pied", "color", "blue"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red"));

        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    public void emptyCrossInput() {
        inputsA = new HashMap<>();
        result = types.cross(inputsA);

        assertEquals(0, result.size());
    }

    @Test
    public void emptyDotInput() throws MoteurLiteException {
        inputsA = new HashMap<>();
        result = types.dot(inputsA);

        assertEquals(0, result.size());
    }

    @Test
    public void wrongSizeDotInput() {
        inputsA.putAll(inputsB);
        assertThrows(MoteurLiteException.class, () -> types.dot(inputsA));
    }

    @Test
    public void crossOnDotAndCross() throws MoteurLiteException{
        inputsB.putAll(inputsC);
        result = types.cross(types.dot(inputsA), types.cross(inputsB));

        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "1", "type", "metal"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "2", "type", "metal"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "3", "type", "metal"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "1", "type", "iron"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "2", "type", "iron"));
        expectedResult.add(Map.of("scan", "pied", "color", "blue", "age", "3", "type", "iron"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "1", "type", "metal"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "2", "type", "metal"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "3", "type", "metal"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "1", "type", "iron"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "2", "type", "iron"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "age", "3", "type", "iron"));
        assertEquals(12, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    public void crossOnEmptyDotAndCross() {
        result = types.cross(new ArrayList<>(), types.cross(inputsA));

        expectedResult.add(Map.of("scan", "pied", "color", "blue"));
        expectedResult.add(Map.of("scan", "pied", "color", "red"));
        expectedResult.add(Map.of("scan", "jambe", "color", "blue"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red"));
        assertEquals(4, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    public void crossOnDotAndEmptyCross() throws MoteurLiteException {
        inputsA.putAll(inputsC);
        result = types.cross(types.dot(inputsA), new ArrayList<>());

        expectedResult.add(Map.of("scan", "pied", "color", "blue", "type", "metal"));
        expectedResult.add(Map.of("scan", "jambe", "color", "red", "type", "iron"));
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedResult));
    }

    @Test
    public void crossOnEmptyDotAndEmptyCross() {
        result = types.cross(new ArrayList<>(), new ArrayList<>());

        assertEquals(0, result.size());
    }
}