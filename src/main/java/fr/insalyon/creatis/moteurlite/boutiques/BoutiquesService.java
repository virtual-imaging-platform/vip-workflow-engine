package fr.insalyon.creatis.moteurlite.boutiques;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;

/**
 * Author: Sandesh Patil [https://github.com/sandepat]
 */

public class BoutiquesService {

    public BoutiquesService() {
    }

    // Method to parse the boutiques descriptor file
    public BoutiquesDescriptor parseFile(String boutiquesDescriptorFile) throws MoteurLiteException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(new File(boutiquesDescriptorFile), BoutiquesDescriptor.class);
        } catch (IOException e) {
            throw new MoteurLiteException("Error parsing boutiques file " + boutiquesDescriptorFile, e);
        }
    }

    public HashMap<String, Input.Type> getInputTypes(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<String, Input.Type> inputTypes = new HashMap<>();
        for (Input input : boutiquesDescriptor.getInputs()) {
            inputTypes.put(input.getId(), input.getType());
        }
        return inputTypes;
    }

    public HashMap<String, Input> getInputsMap(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<String, Input> inputTypes = new HashMap<>();
        for (Input input : boutiquesDescriptor.getInputs()) {
            inputTypes.put(input.getId(), input);
        }
        return inputTypes;
    }

    public HashMap<String, OutputFile> getOutputMap(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<String, OutputFile> outputFiles = new HashMap<>();
        for (OutputFile outputFile : boutiquesDescriptor.getOutputFiles()) {
            outputFiles.put(outputFile.getId(), outputFile);
        }
        return outputFiles;
    }

    // Method to get optional inputs from the BoutiquesDescriptor
    public Set<String> getInputOptionalOfBoutiquesFile(BoutiquesDescriptor boutiquesDescriptor) {
        Set<String> optionalInputs = new HashSet<>();
        for (Input input : boutiquesDescriptor.getInputs()) {
            if (input.getOptional() != null && input.getOptional()) {
                optionalInputs.add(input.getId());
            }
        }
        return optionalInputs;
    }

    // Method to extract "crossSet", "dotSet", and "containerSet" from the custom field of BoutiquesDescriptor
    public Set<String> getCrossMap(BoutiquesDescriptor boutiquesDescriptor) {
        return extractCustomField(boutiquesDescriptor, "VIP:cross");
    }

    public Set<String> getDotMap(BoutiquesDescriptor boutiquesDescriptor) {
        return extractCustomField(boutiquesDescriptor, "VIP:dot-inputs");
    }

    public Set<String> getContainerSet(BoutiquesDescriptor boutiquesDescriptor) {
        return extractCustomField(boutiquesDescriptor, "VIP:imagepath");
    }

    // Helper method to extract the relevant values from the custom field
    private Set<String> extractCustomField(BoutiquesDescriptor boutiquesDescriptor, String key) {
        Set<String> resultSet = new HashSet<>();
        Map<String, Object> customMap = boutiquesDescriptor.getCustom().getAdditionalProperties(); // Assuming getAdditionalProperties() exists for custom

        if (customMap != null && customMap.containsKey(key)) {
            Object value = customMap.get(key);

            if (value instanceof String) {
                resultSet.add((String) value);
            } else if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    resultSet.add(item.toString());
                }
            }
        }

        return resultSet;
    }
}