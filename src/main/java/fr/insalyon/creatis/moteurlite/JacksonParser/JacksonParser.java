package fr.insalyon.creatis.moteurlite.JacksonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.parser.GaswParser;

public class JacksonParser {
    String executableNameJackson;
    HashMap<Integer, String> inputIDList;
    HashMap<Integer, String> outputIDList;
    public JsonValues JsonJacksonParser(String JsonFileName) throws FileNotFoundException, IOException, ParseException {
        //String jsonString = JsonFileName; // Replace with your JSON input
        //JSONParser parser = new JSONParser();
        Object object = new JSONParser().parse(new FileReader(JsonFileName)); 
        String objeString = String.valueOf(object);

        ObjectMapper objectMapper = new ObjectMapper();
        BoutiquesEntities boutiquesEntities = objectMapper.readValue(objeString, BoutiquesEntities.class);
        String executableNameJackson= boutiquesEntities.getName();
        inputIDList = boutiquesEntities.getInputid();
        outputIDList = boutiquesEntities.getOutputid();
        //currentIDList.add(currentID);
        //System.out.println("'''''''+"+boutiquesEntities.getBoutiquesInput());
    
        return new JsonValues(executableNameJackson, inputIDList, outputIDList);
        
    }
}


