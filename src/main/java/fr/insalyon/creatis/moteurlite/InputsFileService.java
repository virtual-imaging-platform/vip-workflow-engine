package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class InputsFileService {

    private static final Logger logger = Logger.getLogger(InputsFileService.class);


    /**
     * This method parses input data from an XML file and returns a Map where each key
     * corresponds to a source name and the value is a List of all items under that source.
     */
    public Map<String, List<String>> parseInputData(String filePath) throws MoteurLiteException {
        Map<String, List<String>> inputMap = new HashMap<>(); 
        
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("source");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    
                    NodeList itemNodeList = element.getElementsByTagName("item");
                    
                    // Create the list for the items if it's not already in the map
                    if (!inputMap.containsKey(name)) {
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
            logger.error("Failed to parse input data from XML file: " + filePath, e);
            throw new MoteurLiteException("Failed to parse input data from XML file: " + filePath, e);
        }
        return inputMap;
    }
}
