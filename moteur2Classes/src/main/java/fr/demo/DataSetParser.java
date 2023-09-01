/* Copyright CNRS-I3S
 *
 * Johan Montagnat
 * johan@i3s.unice.fr
 * http://www.i3s.unice.fr/~johan
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 * 
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or  
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * DataSet file parser.
 */
public class DataSetParser extends DefaultHandler {
    private boolean parsing = false;
    private String input = null;
    private String type = null;
    private List<String> array = null;
    private int [] indices = null;
    private String item = null;
    private int depth = 0, maxdepth = 0;
    private HashMap<String, String> itemTags = null;
    private Vector<HashMap<String, String>> arrayTags = new Vector<HashMap<String, String>>();
    private boolean isvoid = false;
    private boolean parsingItem = false;
    private String currentSourceTag ="";
    private int currentItemIndex = 0;
    private Map<String, String> inputValues = new HashMap<>();
    /**
     * Build data set parser and parse file.
     * @param dataset data set to parse data in
     * @throws org.xml.sax.SAXException file parsing exception
     * @throws java.io.IOException file IO exception
     */
    public DataSetParser(String filePath) throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
        reader.parse(filePath);
    }

    public Map<String, String> getInputValues() {
        return inputValues;
    }

    /**
     * DataSet file parser XML tag start call back.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(localName.equals("inputdata") || localName.equals("d:inputdata")) {
            if(parsing) {
                throw new SAXException("Nested <inputdata> tags.");
            }
            parsing = true;
        }
        else if(localName.equals("source") || localName.equals("d:source")) {
            if(input != null) {
                throw new SAXException("Nested <source> tags.");
            }
            input = attributes.getValue("name");
            if(input == null || input.length() == 0) {
                throw new SAXException("One source tag has no name attribute.");
            }
            type = attributes.getValue("type");
            if(type == null) {
                throw new SAXException("Unknown type \"" + type + "\" for source \"" + input + "\"");
            }
        }
        else if(localName.equals("item") || localName.equals("d:item")) {
            if(array == null || indices == null) {
                throw new SAXException("<item> tag outside of data array");
            }
            parsingItem = true;
            item = "";
            isvoid = false;
            String special = attributes.getValue("special");
            if(special != null) {
                if(special.toLowerCase().equals("void")) {
                    isvoid = true;
                }
                else {
                    throw new SAXException("<item> tag has \"special\" attribute with unknown value \"" + special + "\"");
                }
            }
        }
        else if(localName.equals("tag") || localName.equals("d:tag")) {
            if(array == null || indices == null) {
                throw new SAXException("<tag> tag outside of data array");
            }
            String name = attributes.getValue("name");
            if(name == null || name.length() == 0) {
                throw new SAXException("<tag> tag has no \"name\" attribute.");
            }
            String value = attributes.getValue("value");
            if(value == null || value.length() == 0) {
                throw new SAXException("<tag> tag has no \"value\" attribute.");
            }
            if(parsingItem) {
                if(itemTags == null) {
                    itemTags = new HashMap<String, String>();
                }
                itemTags.put(name, value);
            }
            else {
                arrayTags.get(indices.length - 1).put(name, value);
            }
        }
        else if(localName.equals("array") || localName.equals("d:array") || localName.equals("list") || localName.equals("d:list")) {
            if(array == null) {
                array = new ArrayList<String>();
                indices = new int[1];
                indices[0] = 0;
                maxdepth = depth = 1;
            }
            else {
                if(indices == null) {
                    throw new SAXException("source \"" + input + "\" has two root <array> tags");
                }
                depth++;
                if(depth > maxdepth) {
                    maxdepth = depth;
                }
                int [] ix = new int[indices.length + 1];
                for(int i = 0; i < indices.length; i++) {
                    ix[i] = indices[i];
                }
                ix[ix.length - 1] = 0;
                indices = ix;
            }
            arrayTags.add(new HashMap<String, String>());
        }
        else if(localName.equals("scalar")) {
            if(array != null) {
                throw new SAXException("a <scalar> tag is not the only tag in source \"" + input + "\"");
            }
            array = new ArrayList<String>();
            indices = new int[0];
            maxdepth = depth = 0;
        }
        else {
            throw new SAXException("Unknown tag <" + localName + ">"); //error
        }
    }   
    
    /**
     * DataSet file parser XML tag end call back.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(localName.equals("source") || localName.equals("d:source")) {
            if(array != null) {
                inputValues.put(input, array.get(0));
                array = null;
            }
            input = null;
        }
        else if(localName.equals("array") || localName.equals("d:array") || localName.equals("list") || localName.equals("d:list")) {
           
        }
        else if(localName.equals("scalar")) {
            indices = null;
        }
        else if(localName.equals("item") || localName.equals("d:item")) {

            Object value = null;
            if(type.equalsIgnoreCase("String")) {
                value = item;
            }
            else if(type.equalsIgnoreCase("URI")) {
                try {
                    value = new URI(item.trim());
                }
                catch(URISyntaxException e) {
                    throw new SAXException(e.getMessage());
                }
            }
            else if(type.equalsIgnoreCase("Integer")) {
                try {
                    value = Integer.parseInt(item.trim());
                }
                catch(NumberFormatException e) {
                    throw new SAXException(e.getMessage());
                }
            }
                
            array.add(value.toString());

            item = null;
            parsingItem = false;
            itemTags = null;
            if(indices.length > 0) {
                indices[indices.length - 1]++;                
            }
        }
    }
    
    /**
     * DataSet file parser XML characters reading callback.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if(parsingItem) {
            String chars = new String(ch);
            item += chars.substring(start, start+length);
        }
    }

	@Override
	public String toString() {
		return "DataSetParser [parsing=" + parsing + ", input=" + input + ", type=" + type
				+ ", array=" + array + ", indices=" + Arrays.toString(indices) + ", item=" + item + ", depth=" + depth
				+ ", maxdepth=" + maxdepth + ", itemTags=" + itemTags + ", arrayTags=" + arrayTags + ", isvoid="
				+ isvoid + ", parsingItem=" + parsingItem + ", currentSourceTag=" + currentSourceTag
				+ ", currentItemIndex=" + currentItemIndex + "]";
	}
}