package fr.insalyon.creatis.moteurlite.JacksonParser;

import java.util.HashMap;

public class JsonValues {
    

public String executableNameJackson;
public HashMap<Integer, String> inputIDList;
public HashMap<Integer, String> outputIDList;
public static final String ANSI_RESET = "\u001B[0m";
public static final String ANSI_RED = "\u001B[31m";

public JsonValues(String executableNameJackson2, java.util.HashMap<Integer, String> inputIDList,
            java.util.HashMap<Integer, String> outputIDList2) {
                this.executableNameJackson = executableNameJackson2;
                this.inputIDList = inputIDList;
                this.outputIDList = outputIDList2;
                System.out.println(ANSI_RED + "Executable File Name:" + executableNameJackson2+ ANSI_RESET);
    }


    
    
}
