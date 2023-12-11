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
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        
    public String ParseBoutiquesFile(String boutiquesDescriptorString) throws FileNotFoundException, IOException,  InvalidBoutiquesDescriptorException, GaswException, ParseException {
        System.out.println(ANSI_GREEN + "Inside Parse Boutiques File"+ ANSI_GREEN);
        JacksonParser jacksonParser = new JacksonParser();
        NameOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).executableNameJackson+".sh";
        InputIdOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).inputIDList;
        OutputIdOfBoutiquesFile = jacksonParser.JsonJacksonParser(boutiquesDescriptorString).outputIDList;
        System.out.println(ANSI_RESET + "Executable File Name:" + NameOfBoutiquesFile);
        System.out.println(ANSI_RESET + "Id:" + InputIdOfBoutiquesFile + " " + OutputIdOfBoutiquesFile);
        return NameOfBoutiquesFile;
    }

    public String getDownloadFile() {
        downloadFile = NameOfBoutiquesFile + ".tar.gz";
        System.out.println(ANSI_RESET + "Download File Name:" + downloadFile);
        return downloadFile;
    }

    public java.util.HashMap<Integer, String> getInputIdOfBoutiquesFile() {
        return InputIdOfBoutiquesFile;
    }

    public java.util.HashMap<Integer, String> getOutputIdOfBoutiquesFile() {
        return OutputIdOfBoutiquesFile;
    }
    
}
