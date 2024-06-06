package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.GaswException;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class BoutiquesService {
    private String nameOfBoutiquesFile;
    private String applicationName;
    private HashMap<Integer, String> inputIdOfBoutiquesFile;
    private HashMap<Integer, String> outputIdOfBoutiquesFile;
    private HashMap<String, String> inputTypeOfBoutiquesFile;
    private HashMap<String, String> inputValueKeyOfBoutiquesFile;
    private HashMap<String, String> outputPathTemplateOfBoutiquesFile;
    private Set<String> crossMap;
    private Set<String> dotMap;
    private Set<String> inputOptionalOfBoutiquesFile;

    public BoutiquesService() {
        // Default constructor
    }

    public BoutiquesEntities parseFile(String boutiquesDescriptorString) throws FileNotFoundException, IOException,
            InvalidBoutiquesDescriptorException, GaswException, ParseException {
        Object object = new JSONParser().parse(new FileReader(boutiquesDescriptorString));
        String objeString = String.valueOf(object);

        ObjectMapper objectMapper = new ObjectMapper();
        BoutiquesEntities boutiquesEntities = objectMapper.readValue(objeString, BoutiquesEntities.class);
        
        this.nameOfBoutiquesFile = boutiquesEntities.getName() + ".json";
        this.applicationName = nameOfBoutiquesFile.substring(0, nameOfBoutiquesFile.lastIndexOf('.'));
        this.inputIdOfBoutiquesFile = boutiquesEntities.getInputId();
        this.outputIdOfBoutiquesFile = boutiquesEntities.getOutputId();
        this.crossMap = boutiquesEntities.getCrossMap();
        this.dotMap = boutiquesEntities.getDotMap();
        this.inputTypeOfBoutiquesFile = boutiquesEntities.getInputTypes();
        this.inputValueKeyOfBoutiquesFile = boutiquesEntities.getInputValueKey();
        this.inputOptionalOfBoutiquesFile = boutiquesEntities.getInputOptional();
        this.outputPathTemplateOfBoutiquesFile = boutiquesEntities.getOutputPathTemplateList();

        return boutiquesEntities;
    }

    public String getNameOfBoutiquesFile() {
        return nameOfBoutiquesFile;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public HashMap<Integer, String> getInputIdOfBoutiquesFile() {
        return inputIdOfBoutiquesFile;
    }

    public HashMap<Integer, String> getOutputIdOfBoutiquesFile() {
        return outputIdOfBoutiquesFile;
    }

    public HashMap<String, String> getInputTypeOfBoutiquesFile() {
        return inputTypeOfBoutiquesFile;
    }

    public HashMap<String, String> getInputValueKeyOfBoutiquesFile() {
        return inputValueKeyOfBoutiquesFile;
    }

    public Set<String> getInputOptionalOfBoutiquesFile() {
        return inputOptionalOfBoutiquesFile;
    }

    public HashMap<String, String> getOutputPathTemplateOfBoutiquesFile() {
        return outputPathTemplateOfBoutiquesFile;
    }

    public Set<String> getCrossMap() {
        return crossMap;
    }

    public Set<String> getDotMap() {
        return dotMap;
    }
}