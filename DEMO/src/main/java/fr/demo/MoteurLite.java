package fr.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;


import org.xml.sax.SAXException;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.parser.GaswParser;

public class MoteurLite {
    public static void main(String[] args) throws GaswException, SAXException, IOException, URISyntaxException {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws GaswException, SAXException, IOException, URISyntaxException {
        String gaswFilePath = args[0];
        String inputsFilePath = args[1];

        GaswParser gaswParser = new GaswParser();
        gaswParser.parse(gaswFilePath);

        Map<String, String> inputsMap = new DataSetParser(inputsFilePath).getInputValues();
        GaswInput gaswInput = gaswParser.getGaswInput(inputsMap);

        Gasw gasw = Gasw.getInstance();
        //gasw.setNotificationClient(this);
        String jobID = gasw.submit(gaswInput);
        System.out.println("job launched : " + jobID);
    }
}