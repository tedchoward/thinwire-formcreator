/*
                            ThinWire(R) Form Creator
                   Copyright (C) 2007 Custom Credit Systems

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users interested in finding out more about the ThinWire framework should visit
  the ThinWire framework website at http://www.thinwire.com. For those interested
  in discussing the details of how this application was built, you can contact the 
  developer via email at "Joshua Gertzen" <josh at truecode dot org>.
*/
package thinwire.apps.formcreator;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import java.util.*;

/**
 * @author Joshua J. Gertzen
 */
class XODWriter {
    private List<Object> rootObjects;
    private Map<String, Object> objectMap;
    private Map<Object, String> idMap;
    private Map<String, Class> aliases;
    private List<String> includes;
    
    XODWriter(List<Object> rootObjects, Map<String, Object> objectMap, Map<String, Class> aliases, List<String> includes) {
        this.rootObjects = rootObjects;
        this.objectMap = objectMap;
        this.aliases = aliases;
        this.includes = includes;
    }
    
    private Map<Object, String> getIdMap() {
        if (idMap == null) {
            idMap = new HashMap<Object, String>();

            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                idMap.put(entry.getValue(), entry.getKey());
            }
        }
        
        return idMap;
    }
    
    void writeTo(OutputStream out) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", 4);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");            
            SAXSource source = new SAXSource(new XODOutputReader(), new XODOutputSource(rootObjects, getIdMap(), aliases, includes, true));
            transformer.transform(source, new StreamResult(new OutputStreamWriter(out, "utf-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
