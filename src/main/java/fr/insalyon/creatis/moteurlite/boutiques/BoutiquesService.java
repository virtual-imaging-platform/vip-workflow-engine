package fr.insalyon.creatis.moteurlite.boutiques;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.GaswException;

/**
 * Author: Sandesh Patil [https://github.com/sandepat]
 */

public class BoutiquesService {

    public BoutiquesService() {
    }

    // Method to parse the boutiques descriptor file
    public BoutiquesDescriptor parseFile(String boutiquesDescriptorString) throws IOException, GaswException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(boutiquesDescriptorString), BoutiquesDescriptor.class);
    }

    public String getNameOfBoutiquesFile(BoutiquesDescriptor boutiquesDescriptor) {
        return boutiquesDescriptor.getName() + ".json";
    }

    public String getApplicationName(BoutiquesDescriptor boutiquesDescriptor) {
        String nameOfBoutiquesFile = getNameOfBoutiquesFile(boutiquesDescriptor);
        return nameOfBoutiquesFile.substring(0, nameOfBoutiquesFile.lastIndexOf('.'));
    }

    // Method to extract input IDs
    public HashMap<Integer, String> getInputId(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<Integer, String> inputIds = new HashMap<>();
        int index = 0;
        for (Input input : boutiquesDescriptor.getInputs()) {
            inputIds.put(index++, input.getId());
        }
        return inputIds;
    }

    // Method to extract output IDs
    public HashMap<Integer, String> getOutputId(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<Integer, String> outputIds = new HashMap<>();
        int index = 0;
        for (OutputFile output : boutiquesDescriptor.getOutputFiles()) {
            outputIds.put(index++, output.getId());
        }
        return outputIds;
    }

    public HashMap<String, String> getInputType(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<String, String> inputTypes = new HashMap<>();
        for (Input input : boutiquesDescriptor.getInputs()) {
            inputTypes.put(input.getId(), input.getType().toString());
        }
        return inputTypes;
    }

    public HashMap<String, String> getInputValueKey(BoutiquesDescriptor boutiquesDescriptor) {
        HashMap<String, String> inputValueKeys = new HashMap<>();
        for (Input input : boutiquesDescriptor.getInputs()) {
            inputValueKeys.put(input.getId(), input.getValueKey());
        }
        return inputValueKeys;
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
