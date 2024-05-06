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

public class ParseBoutiquesFile {
    String nameOfBoutiquesFile;
    String applicationName;
    java.util.HashMap<Integer, String> inputIdOfBoutiquesFile;
    java.util.HashMap<Integer, String> outputIdOfBoutiquesFile;
    HashMap<String, String> inputTypeOfBoutiquesFile;
    HashMap<String, String> inputValueKeyOfBoutiquesFile;
    HashMap<String, String> outputPathTemplateOfBoutiquesFile;
    Set<String> crossMap;
    Set<String> dotMap;
    Set<String> inputOptionalOfBoutiquesFile;

    public ParseBoutiquesFile(String boutiquesDescriptorString) throws FileNotFoundException, IOException,
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
    }

    public String getNameOfBoutiquesFile() {
        return nameOfBoutiquesFile;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public java.util.HashMap<Integer, String> getinputIdOfBoutiquesFile() {
        return inputIdOfBoutiquesFile;
    }

    public java.util.HashMap<Integer, String> getoutputIdOfBoutiquesFile() {
        return outputIdOfBoutiquesFile;
    }

    public java.util.HashMap<String, String> getinputTypeOfBoutiquesFile() {
        return inputTypeOfBoutiquesFile;
    }

    public java.util.HashMap<String, String> getinputValueKeyOfBoutiquesFile() {
        return inputValueKeyOfBoutiquesFile;
    }

    public java.util.Set<String> getinputOptionalOfBoutiquesFile() {
        return inputOptionalOfBoutiquesFile;
    }

    public java.util.HashMap<String, String> getoutputPathTemplateOfBoutiquesFile() {
        return outputPathTemplateOfBoutiquesFile;
    }

    public Set<String> getCrossMap() {
        return crossMap;
    }

    public Set<String> getDotMap() {
        return dotMap;
    }
}
