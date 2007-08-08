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

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import thinwire.apps.formcreator.Reflector.Property;
import thinwire.ui.Label;

/**
 * @author Joshua J. Gertzen
 */
class XODOutputReader implements XMLReader {
    private static final Logger log = Logger.getLogger(XODOutputReader.class.getName());
    private static final Level LEVEL = Level.INFO;
    
    private static final String NAMESPACE_URI = "";
    private static final String ROOT_ELEMENT = "xod";
    private static final String INCLUDE_ELEMENT = "include";
    private static final String ALIAS_ELEMENT = "alias";
    private static final String REF_ELEMENT = "ref";

    private final Attributes EMPTY_ATTR = new AttributesImpl(); 
    private ContentHandler handler;
    private XODOutputSource source;
    private Map<Class, Object> defaults;
    private Set<Object> objects;
    private List<Object> rootObjects;
    private int level;

    public ContentHandler getContentHandler() {
        return handler;
    }

    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException("XODReader can only process a XODInputSource");
    }
    
    public void parse(InputSource input) throws IOException, SAXException {
        if (!(input instanceof XODOutputSource)) throw new IllegalArgumentException("XODReader can only process a XODInputSource");
        source = (XODOutputSource)input;
        if (source.isFilterDefaults()) defaults = new HashMap<Class, Object>();
        objects = new HashSet<Object>();
        handler.startDocument();
        handler.startElement(NAMESPACE_URI, ROOT_ELEMENT, ROOT_ELEMENT, EMPTY_ATTR);

        for (String include : source.getIncludes()) {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute(NAMESPACE_URI, "file", "file", "CDATA", include);
            handler.startElement(NAMESPACE_URI, INCLUDE_ELEMENT, INCLUDE_ELEMENT, attr);
            handler.endElement(NAMESPACE_URI, INCLUDE_ELEMENT, INCLUDE_ELEMENT);
        }
        
        for (Map.Entry<Class, String> e : source.getAliasMap().entrySet()) {
            String value = e.getValue();
            
            if (value.charAt(0) == '_' && value.length() > 1) {
                value = value.substring(1);
                e.setValue(value);
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute(NAMESPACE_URI, "name", "name", "CDATA", value);
                attr.addAttribute(NAMESPACE_URI, "class", "class", "CDATA", e.getKey().getName());
                handler.startElement(NAMESPACE_URI, ALIAS_ELEMENT, ALIAS_ELEMENT, attr);
                handler.endElement(NAMESPACE_URI, ALIAS_ELEMENT, ALIAS_ELEMENT);
            }
        }
        
        rootObjects = source.getRootObjects();
        
        for (Object top : rootObjects) {
            log.info("Outputing root object:" + top.getClass());
            outputObject(rootObjects, top);
        }
        
        handler.endElement(NAMESPACE_URI, ROOT_ELEMENT, ROOT_ELEMENT);
        handler.endDocument();
        defaults.clear();
        objects.clear();
        defaults = null;
        objects = null;
    }
    
    private void outputObject(Collection parent, Object obj) throws SAXException {
        if (objects.contains(obj) || obj instanceof DesignTimeComponent) return;
        String name = source.getAliasMap().get(obj.getClass());
        if (name == null) name = obj.getClass().getName();
        if (log.isLoggable(LEVEL)) log.log(LEVEL, "writing object '" + name + "'");
        objects.add(obj);
        
        String id = source.getObjectId(obj);
        AttributesImpl attr;
        
        if (id == null) {
            attr = (AttributesImpl)EMPTY_ATTR;
        } else {
            attr = new AttributesImpl();
            attr.addAttribute(NAMESPACE_URI, "id", "id", "CDATA", id);
        }

        handler.startElement(NAMESPACE_URI, name, name, attr);
        
        if (obj instanceof Collection) {
            for (Object item : (Collection)obj) {
                id = source.getObjectId(item);
                
                if (id != null) {
                    writeRefElement(id);
                } else {
                    outputObject(parent, item);                            
                }
            }
        } else {
            outputObjectMembers(parent, obj, false);
        }
        
        handler.endElement(NAMESPACE_URI, name, name);
    }
    
    private int outputObjectMembers(Collection parent, Object obj, boolean countOnly) throws SAXException {
        int count = 0;
        Class clazz = obj.getClass();
        Reflector<? super Object> reflect = new Reflector("General", clazz);
        Object def = null;
        
        if (defaults != null) {
            def = defaults.get(clazz);
            
            if (def == null) {
                try {
                    Constructor con = clazz.getConstructor((Class[])null);
                    if (Modifier.isPublic(con.getModifiers())) defaults.put(clazz, def = con.newInstance((Object[])null));
                } catch (NoSuchMethodException e) {
                    //Do nothing since it's ok if a default object can't be used.
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (log.isLoggable(LEVEL)) log.log(LEVEL, "Reflector found '" + reflect.getProperties().size() + "' properties");
        
        List<Reflector.Property<? super Object>> propBasic = new ArrayList<Reflector.Property<? super Object>>();
        List<Reflector.Property<? super Object>> propCollect = new ArrayList<Reflector.Property<? super Object>>();
        List<Reflector.Property<? super Object>> propComplex = new ArrayList<Reflector.Property<? super Object>>();
        
        for (Reflector.Property<? super Object> prop : reflect.getProperties()) {
            if (!prop.isReadable()) continue;
            
            if (prop.isWritable()) {
                propBasic.add(prop);
            } else if (Collection.class.isAssignableFrom(prop.getType())) {
                propCollect.add(prop);
            } else if (def != null && prop.isComplexType()) {
                propComplex.add(prop);
            }
        }
        
        Comparator<Reflector.Property<? super Object>> compare = new Comparator<Reflector.Property<? super Object>>() {
            public int compare(Property< ? super Object> o1, Property< ? super Object> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
        
        Collections.sort(propBasic, compare);
        Collections.sort(propComplex, compare);
        Collections.sort(propCollect, compare);
        
        for (Reflector.Property<? super Object> prop : propBasic) {
            Object oValue;
            
            try {
                oValue = prop.getValue(obj);
            } catch (Exception e) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "unable to read value for object member '" + prop.getName() + "', exception='" + e.getMessage() + "'"); 
                continue;
            }
            
            if (def != null) {
                Object dValue = prop.getValue(def);
                
                if (oValue == dValue || (oValue != null && oValue.equals(dValue)) || (dValue != null && dValue.equals(oValue))) {
                    continue;
                }
            }

            count++;

            if (!countOnly) {
                if (parent.contains(oValue)) {
                    String name = prop.getName();
                    String id = source.getObjectId(oValue);

                    if (id != null) {
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, "writing reference to '" + id + "' from prop '" + prop.getName() + "'");
                        handler.startElement(NAMESPACE_URI, name, name, EMPTY_ATTR);
                        writeRefElement(id);
                        handler.endElement(NAMESPACE_URI, name, name);
                    }
                } else {
                    String value = AbstractPropertyEditor.toStringValue(oValue);
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, "writing object member '" + prop.getName() + "', value='" + oValue + "'");
                    writeElement(prop.getName(), value);
                }
            }
        }
        
        for (Reflector.Property<? super Object> prop : propComplex) {
            Object oValue;
            
            try {
                oValue = prop.getValue(obj);
            } catch (Exception e) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "unable to read value for object member '" + prop.getName() + "', exception='" + e.getMessage() + "'"); 
                continue;
            }
            
            if (objects.contains(oValue) || rootObjects.contains(oValue) || parent.contains(oValue)) continue;
            
            Object dValue = prop.getValue(def);
            
            //NOTE: For this to work, the objects being compared must report equality properly.
            if (dValue != null && !dValue.equals(oValue)) {
                String name = prop.getName();
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "traversing object '" + prop.getName() + "'");
                
                if (!countOnly) {
                    int innerCount = outputObjectMembers(parent, oValue, true);
                    
                    if (innerCount > 0) {
                        objects.add(oValue);
                        handler.startElement(NAMESPACE_URI, name, name, EMPTY_ATTR);
                        outputObjectMembers(parent, oValue, false);
                        handler.endElement(NAMESPACE_URI, name, name);
                        count += 1 + innerCount;
                    }
                } else {
                    int innerCount = outputObjectMembers(parent, oValue, true);
                    if (innerCount > 0) count += 1 + innerCount;
                }
            }
        }
        
        for (Reflector.Property<? super Object> prop : propCollect) {
            Object oValue;
            
            try {
                oValue = prop.getValue(obj);
            } catch (Exception e) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "unable to read value for object member '" + prop.getName() + "', exception='" + e.getMessage() + "'"); 
                continue;
            }
            
            if (objects.contains(oValue) || rootObjects.contains(oValue)) continue;
            Collection col = (Collection)oValue;

            if (col == null) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "collection for object member '" + prop.getName() + "' is null");
                continue;
            }
                
            if (col.size() > 0) {
                boolean process = false;
                
                for (Object item : col) {
                    if (!objects.contains(item) && !rootObjects.contains(item)) {
                        process = true;
                        break;
                    }
                }
                
                if (process) {
                    String name = prop.getName();
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, "traversing collection '" + name + "', size='" + col.size() + "'");
                    count++;
                    
                    if (!countOnly) {
                        handler.startElement(NAMESPACE_URI, name, name, EMPTY_ATTR);

                        for (Object item : col) {
                            outputObject(col, item);
                        }
                    
                        handler.endElement(NAMESPACE_URI, name, name);
                    }
                }
            }
        }
        
        return count;
    }
    
    private void writeRefElement(String id) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(NAMESPACE_URI, "id", "id", "CDATA", id);
        handler.startElement(NAMESPACE_URI, REF_ELEMENT, REF_ELEMENT, attr);
        handler.endElement(NAMESPACE_URI, REF_ELEMENT, REF_ELEMENT);
    }
    
    private void writeElement(String name, String value) throws SAXException {
        handler.startElement(NAMESPACE_URI, name, name, EMPTY_ATTR);
        if (value == null) value = "";
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement(NAMESPACE_URI, name, name);
    }

    public DTDHandler getDTDHandler() { return null; }
    public EntityResolver getEntityResolver() { return null; }
    public ErrorHandler getErrorHandler() { return null; }
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException { return false; }
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException { return null; }
    public void setDTDHandler(DTDHandler handler) { }
    public void setEntityResolver(EntityResolver resolver) { }
    public void setErrorHandler(ErrorHandler handler) { }
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException { }
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException { }
}
