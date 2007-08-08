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

import java.util.*;
import java.lang.reflect.*;

import thinwire.ui.MaskEditorComponent;

/**
 * @author Joshua J. Gertzen
 */
class Reflector<T> {
    static class Property<PT> extends AbstractPropertyEditor<PT> {
        public Property(Class declaringType, Class<PT> type, String name, String group) {
            init(declaringType, type, name, group);
            initStandardAccessors(name);
        }
    }
    
    private Map<String, Property<T>> props = new HashMap<String, Property<T>>();
    private Collection<Property<T>> roProperties;

    public Reflector(String defaultGroup, Class<T> clazz) {
        this(defaultGroup, null, clazz);
    }
    
    public Reflector(String defaultGroup, Map<String, String> propertyToGroup, Class<T> clazz) {
        if (defaultGroup == null) defaultGroup = "";
        
        try {
            for (Method m : clazz.getMethods()) {
                String name = m.getName();
                if (!Modifier.isPublic(m.getModifiers())) continue;
                int len = name.length();
                Class retType = m.getReturnType();
                Class[] argTypes = m.getParameterTypes();
                Class type;
                String propName;
                
                if (((name.startsWith("get") && len > 3) || (name.startsWith("is") && len > 2)) && retType != void.class && argTypes.length == 0) {
                    propName = getPropertyName(name);
                    type = retType;
                } else if (name.startsWith("set") && len > 3 && retType == void.class && argTypes.length == 1) {
                    propName = getPropertyName(name);
                    type = argTypes[0];
                } else {
                    continue;
                }

                Property<T> prop = props.get(propName);
                if (prop != null) continue;
                if (propName.equals("class")) continue;
                String group = propertyToGroup != null ? propertyToGroup.get(propName) : defaultGroup;
                props.put(propName, prop = new Property<T>(m.getDeclaringClass(), type, propName, group));
            }
            
            roProperties = Collections.unmodifiableCollection(props.values());
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }        
    }
    
    private static String getPropertyName(String name) {
        String propName = name.substring(name.charAt(0) == 'i' ? 2 : 3);
        propName = Character.toLowerCase(propName.charAt(0)) + (propName.length() > 1 ? propName.substring(1) : "");
        return propName;
    }
    
    public Collection<Property<T>> getProperties() {
        return roProperties;
    }
}