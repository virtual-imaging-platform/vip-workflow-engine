package fr.insalyon.creatis.moteurlite.inputsParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */
public class InputParser {

    /**
     * This method parses input data from an XML file and returns a Map where each key
     * corresponds to a source name and the value is a List of all items under that source.
     */
    public static Map<String, List<String>> parseInputData(String filePath) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputMap;
    }

    /**
     * This method parses the types of inputs from the XML file and returns a Map where the key is
     * the source name and the value is the type of the input (e.g., String, URI).
     */
    public static Map<String, String> parseInputType(String fileName) {
        Map<String, String> nameTypeMap = new HashMap<>();
        try {
            File inputFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList sourceList = doc.getElementsByTagName("source");
            for (int i = 0; i < sourceList.getLength(); i++) {
                Node sourceNode = sourceList.item(i);
                if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element sourceElement = (Element) sourceNode;
                    String name = sourceElement.getAttribute("name");
                    String type = sourceElement.getAttribute("type");
                    nameTypeMap.put(name, type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nameTypeMap;
    }
}
