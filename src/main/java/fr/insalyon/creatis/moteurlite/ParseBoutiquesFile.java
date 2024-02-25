package fr.insalyon.creatis.moteurlite;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gwt.dev.util.collect.HashMap;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.moteurlite.JacksonParser.BoutiquesEntities;
import fr.insalyon.creatis.moteurlite.JacksonParser.InvalidBoutiquesDescriptorException;
import fr.insalyon.creatis.moteurlite.JacksonParser.JacksonParser;
import fr.insalyon.creatis.moteurlite.JacksonParser.JsonValues;
import fr.insalyon.creatis.gasw.parser.GaswParser;


public class ParseBoutiquesFile {
        String NameOfBoutiquesFile;
        String downloadFile;
        java.util.HashMap<Integer, String> InputIdOfBoutiquesFile;
        java.util.HashMap<Integer, String> OutputIdOfBoutiquesFile;
        
    public String ParseBoutiquesFile(String boutiquesDescriptorString) throws FileNotFoundException, IOException,  InvalidBoutiquesDescriptorException, GaswException, ParseException {
        JacksonParser jacksonParser = new JacksonParser();
        NameOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).executableNameJackson+".sh";
        InputIdOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).inputIDList;
        OutputIdOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).outputIDList;
        return NameOfBoutiquesFile;
    }

    public String getDownloadFile() {
        downloadFile = NameOfBoutiquesFile + ".tar.gz";
        return downloadFile;
    }

    public java.util.HashMap<Integer, String> getInputIdOfBoutiquesFile() {
        return InputIdOfBoutiquesFile;
    }

    public java.util.HashMap<Integer, String> getOutputIdOfBoutiquesFile() {
        return OutputIdOfBoutiquesFile;
    }
    
}
