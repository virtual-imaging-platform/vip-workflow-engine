package fr.insalyon.creatis.moteurlite.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;

public class InputsFileService {

    private static final Logger logger = Logger.getLogger(InputsFileService.class);

    public Map<String, List<String>> parse(String filePath) throws MoteurLiteException {
        String ext = FilenameUtils.getExtension(filePath);
        File file = new File(filePath);

        if ( ! ext.isEmpty()) {
            switch (ext) {
                case "json":
                    return handleJSON(file);
                case "xml":
                    return handleXML(file);
                default:
                    throw new MoteurLiteException("Unsupported input file extension (only json or xml)");
            }
        } else {
            throw new MoteurLiteException("Input file extension missing!");
        }
    }

    public Map<String, List<String>> handleJSON(File file) throws MoteurLiteException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(file, new TypeReference<Map<String, List<String>>>() {});

        } catch (IOException e) {
            logger.error("Cannot transform JSON String to JSON Map!");
            throw new MoteurLiteException(e);
        }
    }

    public Map<String, List<String>> handleXML(File file) throws MoteurLiteException {
        Map<String, List<String>> inputMap = new HashMap<>(); 
        
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("source");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    
                    NodeList itemNodeList = element.getElementsByTagName("item");
                    
                    // Create the list for the items if it's not already in the map
                    if ( ! inputMap.containsKey(name)) {
                        inputMap.put(name, new ArrayList<>());
                    }
                    
                    // Add all item values under this source name
                    for (int j = 0; j < itemNodeList.getLength(); j++) {
                        Node itemNode = itemNodeList.item(j);
                        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                            String value = itemNode.getTextContent().trim();
                            inputMap.get(name).add(value);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Failed to parse input data from XML file: " + file.getPath(), e);
            throw new MoteurLiteException("Failed to parse input data from XML file: " + file.getPath(), e);
        }
        return inputMap;
    }
}
