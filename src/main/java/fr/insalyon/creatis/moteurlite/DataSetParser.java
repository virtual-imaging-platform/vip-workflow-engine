// Source code is decompiled from a .class file using FernFlower decompiler.
package fr.insalyon.creatis.moteurlite;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import java.util.ArrayList; 
import java.util.Arrays; 

public class DataSetParser extends DefaultHandler {
   private boolean parsing = false;
   private String input = null;
   private String type = null;
   private List<String> array = null;
   private int[] indices = null;
   private String item = null;
   private int depth = 0;
   private int maxdepth = 0;
   private HashMap<String, String> itemTags = null;
   private Vector<HashMap<String, String>> arrayTags = new Vector();
   private boolean isvoid = false;
   private boolean parsingItem = false;
   private String currentSourceTag = "";                  
   private int currentItemIndex = 0;
   private Map<String, String> resultsDirectory = new HashMap();
   private ArrayList<Map<String, String>> inputValues = new ArrayList<Map<String,String>>();
   public static final String ANSI_GREEN = "\u001B[32m";
   public static final String ANSI_RESET = "\u001B[0m";
   public static final String ANSI_RED = "\u001B[31m";
   public static final String ANSI_BLUE = "\u001B[34m";
   public static final String ANSI_CYAN = "\u001B[36m";
   List<List<Map<String, String>>> splitArrayList1 = new ArrayList<List<Map<String, String>>>();
   private Map<String, List<String>> inputMapList = new HashMap<>();
   public DataSetParser(String filePath) throws SAXException, IOException {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(this);
      reader.parse(filePath);
   }

   public List<Map<String, String>> getInputValuesCross() {
      List<Map<String, String>> crossCombinations = crossIteration(inputMapList);
      addResultsDir(crossCombinations, resultsDirectory);
      System.out.println(ANSI_GREEN+ crossCombinations+ANSI_RESET);
      return crossCombinations;
   }

   public List<Map<String, String>> getInputValuesDot() {
      List<Map<String, String>> dotCombinations = dotIteration(inputMapList);
      addResultsDir(dotCombinations, resultsDirectory);
      System.out.println(ANSI_BLUE+ dotCombinations + ANSI_RESET);
      return dotCombinations;
   }



   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (!localName.equals("inputdata") && !localName.equals("d:inputdata")) {
         if (!localName.equals("source") && !localName.equals("d:source")) {
            String name;
            if (!localName.equals("item") && !localName.equals("d:item")) {
               if (!localName.equals("tag") && !localName.equals("d:tag")) {
                  if (!localName.equals("array") && !localName.equals("d:array") && !localName.equals("list") && !localName.equals("d:list")) {
                     if (!localName.equals("scalar")) {
                        throw new SAXException("Unknown tag <" + localName + ">");
                     }

                     if (this.array != null) {
                        throw new SAXException("a <scalar> tag is not the only tag in source \"" + this.input + "\"");
                     }

                     this.array = new ArrayList();
                     this.indices = new int[0];
                     this.maxdepth = this.depth = 0;
                  } else {
                     if (this.array == null) {
                        this.array = new ArrayList();
                        this.indices = new int[1];
                        this.indices[0] = 0;
                        this.maxdepth = this.depth = 1;
                     } else {
                        if (this.indices == null) {
                           throw new SAXException("source \"" + this.input + "\" has two root <array> tags");
                        }

                        ++this.depth;
                        if (this.depth > this.maxdepth) {
                           this.maxdepth = this.depth;
                        }

                        int[] ix = new int[this.indices.length + 1];

                        for(int i = 0; i < this.indices.length; ++i) {
                           ix[i] = this.indices[i];
                        }

                        ix[ix.length - 1] = 0;
                        this.indices = ix;
                     }

                     this.arrayTags.add(new HashMap());
                  }
               } else {
                  if (this.array == null || this.indices == null) {
                     throw new SAXException("<tag> tag outside of data array");
                  }

                  name = attributes.getValue("name");
                  
                  if (name == null || name.length() == 0) {
                     throw new SAXException("<tag> tag has no \"name\" attribute.");
                  }

                  String value = attributes.getValue("value");
                  
                  if (value == null || value.length() == 0) {
                     throw new SAXException("<tag> tag has no \"value\" attribute.");
                  }

                  if (this.parsingItem) {
                     if (this.itemTags == null) {
                        this.itemTags = new HashMap();
                     }

                     this.itemTags.put(name, value);
                  } else {
                     ((HashMap)this.arrayTags.get(this.indices.length - 1)).put(name, value);
                  }
               }
            } else {
               if (this.array == null || this.indices == null) {
                  throw new SAXException("<item> tag outside of data array");
               }

               this.parsingItem = true;
               this.item = "";
               this.isvoid = false;
               name = attributes.getValue("special");
               if (name != null) {
                  if (!name.toLowerCase().equals("void")) {
                     throw new SAXException("<item> tag has \"special\" attribute with unknown value \"" + name + "\"");
                  }

                  this.isvoid = true;
               }
            }
         } else {
            if (this.input != null) {
               throw new SAXException("Nested <source> tags.");
            }

            this.input = attributes.getValue("name");
            if (this.input == null || this.input.length() == 0) {
               throw new SAXException("One source tag has no name attribute.");
            }

            this.type = attributes.getValue("type");
            if (this.type == null) {
               throw new SAXException("Unknown type \"" + this.type + "\" for source \"" + this.input + "\"");
            }
         }
      } else {
         if (this.parsing) {
            throw new SAXException("Nested <inputdata> tags.");
         }

         this.parsing = true;
      }

   }

   public void endElement(String uri, String localName, String qName) throws SAXException {
      if (!localName.equals("source") && !localName.equals("d:source")) {
         if (!localName.equals("array") && !localName.equals("d:array") && !localName.equals("list") && !localName.equals("d:list")) {
            if (localName.equals("scalar")) {
               this.indices = null;
            } else if (localName.equals("item") || localName.equals("d:item")) {
               Object value = null;
               if (this.type.equalsIgnoreCase("String")) {
                  value = this.item;
               } else if (this.type.equalsIgnoreCase("URI")) {
                  try {
                     value = new URI(this.item.trim());
                  } catch (URISyntaxException var7) {
                     throw new SAXException(var7.getMessage());
                  }
               } else if (this.type.equalsIgnoreCase("Integer")) {
                  try {
                     value = Integer.parseInt(this.item.trim());
                  } catch (NumberFormatException var6) {
                     throw new SAXException(var6.getMessage());
                  }
               }

               this.array.add(value.toString());
               this.item = null;
               this.parsingItem = false;
               this.itemTags = null;
               if (this.indices.length > 0) {
                  int var10002 = this.indices[this.indices.length - 1]++;
               }
            }
         }
      } else {
         List<String> listOfKeys = new ArrayList<>();
         String values = new String();
         if (this.array != null) {
            if ("results-directory".equals(this.input)) {
               this.resultsDirectory.put(this.input, array.get(0));
            }
            else {
               for (int i=0; i<array.size(); i++) {
                  String key = array.get(i);
                  values = this.input;
                  Map<String, String> pairMap = new HashMap<>();
                  pairMap.put(values, key);
                  inputValues.add(pairMap);
                  listOfKeys.add(key);
            }
        
            inputMapList.put(values, listOfKeys);
            }
            this.array = null;
         }
         this.input = null;
      }
  }
      
   public static List<Map<String, String>> crossIteration(Map<String, List<String>> inputMap) {
      List<Map<String, String>> combinations = new ArrayList<>();

      // Generate cross combinations
      for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
         String key = entry.getKey();
         List<String> values = entry.getValue();
         if (combinations.isEmpty()) {
            for (String value : values) {
                  Map<String, String> combination = new HashMap<>();
                  combination.put(key, value);
                  combinations.add(combination);
            }
         } else {
            List<Map<String, String>> temp = new ArrayList<>(combinations);
            combinations.clear();
            for (Map<String, String> combination : temp) {
                  for (String value : values) {
                     Map<String, String> newCombination = new HashMap<>(combination);
                     newCombination.put(key, value);
                     combinations.add(newCombination);
                  }
            }
         }
      }
      return combinations;
   }

   public static List<Map<String, String>> dotIteration(Map<String, List<String>> inputMap) {
      List<Map<String, String>> combinations = new ArrayList<>();

      // Generate dot combinations
      int maxSize = inputMap.values().stream().mapToInt(List::size).min().orElse(0);
      for (int i = 0; i < maxSize; i++) {
         Map<String, String> combination = new HashMap<>();
         for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) { 
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (i < values.size()) {
                  combination.put(key, values.get(i));
            }
         }
         combinations.add(combination);
      }
      return combinations;
   }

   private static void addResultsDir(List<Map<String, String>> combinations, Map<String, String> resultsDirectory) {
      for (Map<String, String> map : combinations) {
         resultsDirectory.putAll(map);
         map.clear();
         map.putAll(resultsDirectory);
      }
   }
   
   public void characters(char[] ch, int start, int length) {
      if (this.parsingItem) {
         String chars = new String(ch);
         String var10001 = String.valueOf(this.item);
         this.item = var10001 + chars.substring(start, start + length);
      }

   }

   public String toString() {
      boolean var10000 = this.parsing;
      return "DataSetParser [parsing=" + var10000 + ", input=" + this.input + ", type=" + this.type + ", array=" + String.valueOf(this.array) + ", indices=" + Arrays.toString(this.indices) + ", item=" + this.item + ", depth=" + this.depth + ", maxdepth=" + this.maxdepth + ", itemTags=" + String.valueOf(this.itemTags) + ", arrayTags=" + String.valueOf(this.arrayTags) + ", isvoid=" + this.isvoid + ", parsingItem=" + this.parsingItem + ", currentSourceTag=" + this.currentSourceTag + ", currentItemIndex=" + this.currentItemIndex + "]";
   }
}