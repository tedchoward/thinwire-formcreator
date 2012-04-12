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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.*;

import thinwire.ui.*;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractPropertyEditor<T> implements PropertyEditor<T> {
    private static final Object NO_DEFAULT = new Object();
    
    private Map<Object, Object> virtualValue;
    private Set<PropertyChangeListener> listeners;
    private Class declaringType;
    private String group;
    private String name;
    private Class type;
    private Object defaultValue;
    
    protected Method getter;
    protected Method setter;

    protected void init(Class declaringType, Class type, String name) {
        init(declaringType, type, name, null);
    }

    protected void init(Class declaringType, Class type, String name, String group) {
        this.declaringType = declaringType;
        this.name = name;
        this.type = type;
        this.group = group;
    }
    
    protected void initStandardAccessors(String name) {
        if (declaringType == Object.class) {
            virtualValue = new WeakHashMap<Object, Object>();
        } else {
            name = name.length() == 1 ? String.valueOf(Character.toUpperCase(name.charAt(0))) : Character.toUpperCase(name.charAt(0)) + name.substring(1);
            
            try {
                getter = declaringType.getMethod((type == boolean.class ? "is" : "get") + name);
                getter.setAccessible(true);
            } catch (NoSuchMethodException e) {
                getter = null;
            }
            
            try {
                setter = declaringType.getMethod("set" + name, type);
                setter.setAccessible(true);
            } catch (NoSuchMethodException e) {
                setter = null;
            }
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (listeners == null) listeners = new HashSet<PropertyChangeListener>();
        listeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (listeners == null) return;
        listeners.remove(listener);
    }
    
    public Class getDeclaringType() {
        return declaringType;
    }
    
    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }
    
    public boolean isValidFor(T obj) {
        return declaringType.isInstance(obj);
    }
    
    public boolean isReadable() {
        return virtualValue != null || getter != null;
    }
    
    public boolean isWritable() {
        return virtualValue != null || setter != null;
    }

    public void setValue(T obj, Object value) {
        setValueForType(obj, value);
    }
    
    public Object getValue(T obj) {
        return getValueForType(obj);
    }

    public Object getDefaultValue(T obj) {
        if (defaultValue == null) {
            Class type = obj.getClass();
            
            try {
                type.getConstructor((Class[])null);
                defaultValue = getValueForType(type.newInstance());
            } catch (NoSuchMethodException e) {
                defaultValue = NO_DEFAULT; 
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return defaultValue == NO_DEFAULT ? null : defaultValue;
    }
    
    public MaskEditorComponent newEditor() {
    	if (type == Date.class) {
    		return new DropDownDateBox();
    	} else if (type.isEnum() || isComplexType()) {
    		return new DropDownGridBox();
    	} else {
    		return new TextField();
    	}
    }

    protected void setValueForType(Object obj, Object value) {
        if (!isWritable()) throw new IllegalArgumentException("property '" + name + "' is not writable");
        value = toTypeValue(type, value);
        Object oldValue = null;
        if (listeners != null) oldValue = getValueForType(obj);

        if (virtualValue == null) {
            try {
                setter.invoke(obj, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            virtualValue.put(obj, value);
        }

        if (listeners != null) {
            if (oldValue == value || (oldValue != null && oldValue.equals(value)) || (value != null && value.equals(oldValue))) return;
            Component comp = obj instanceof Component ? (Component)obj : Application.current().getFrame();
            PropertyChangeEvent pce = new PropertyChangeEvent(getName(), oldValue, value, comp, obj);
            
            for (PropertyChangeListener pcl : listeners.toArray(new PropertyChangeListener[listeners.size()])) {
                pcl.propertyChange(pce);
            }
        }
    }
    
    protected Object getValueForType(Object obj) {
        if (!isReadable()) throw new IllegalArgumentException("property '" + name + "' is not readable");

        if (virtualValue == null) {
            try {
                return getter.invoke(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            Object val = virtualValue.get(obj);
            return val == null && String.class.isAssignableFrom(type) ? "" : val;
        }
    }

    protected Object getDefaultValueForType(Object obj) {
        if (defaultValue == null) {
            Class type = obj.getClass();
            
            try {
                type.getConstructor((Class[])null);
                defaultValue = getValue((T)type.newInstance());
            } catch (NoSuchMethodException e) {
                defaultValue = NO_DEFAULT; 
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return defaultValue == NO_DEFAULT ? null : defaultValue;
    }
    
    public boolean isComplexType() {
        if (type == String.class ||
                type == Boolean.class || type == boolean.class ||
                type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Short.class || type == short.class ||
                type == Byte.class || type == byte.class ||
                type == Float.class || type == float.class ||
                type == Double.class || type == double.class ||
                type == Character.class || type == char.class) {
            return false;
        } else {
            return true;
        }
    }
    
    public static String toStringValue(Object value) {
        if (value == null) return "null";
        Class type = value.getClass();
        
        if (type == String.class) {
            return (String)value;
        } else if (type == Boolean.class) {
            return ((Boolean)value).toString();
        } else if (type == Integer.class) {                            
            return ((Integer)value).toString();
        } else if (type == Long.class) {                            
            return ((Long)value).toString();
        } else if (type == Short.class) {
            return ((Short)value).toString();
        } else if (type == Byte.class) {
            return ((Byte)value).toString();
        } else if (type == Float.class) {
            return ((Float)value).toString();
        } else if (type == Double.class) {
            return ((Double)value).toString();
        } else if (type == Character.class) {                                
            return ((Character)value).toString();
        } else {
            try {
                for (Field f : type.getFields()) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        Object fValue = f.get(null);
                        
                        if (type.isInstance(fValue) && fValue.equals(value)) {
                            return f.getName().toLowerCase().replace('_', '-');
                        }
                    }
                }
                
                Method m = type.getMethod("valueOf", String.class);
                if (m.getReturnType() != type) throw new NoSuchMethodException("public static " + type + " valueOf(String value)");
                return type.toString();
            } catch (NoSuchMethodException e) {
                return null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static Object toTypeValue(Class type, Object value) {
        if (value == null) return null;
        if (type.equals(value.getClass())) return value;        
        String str = value.toString();
        if (str.equals("null")) str = null;
        
        if (type == String.class) {
            value = str;
        } else if (type == boolean.class || type == Boolean.class) {
            value = str.equals("") ? Boolean.FALSE : Boolean.valueOf(str);
        } else if (type == int.class || type == Integer.class) {
            value = str.equals("") ? new Integer(0) : new Integer(Double.valueOf(str).intValue());
        } else if (type == long.class || type == Long.class) {                            
            value = str.equals("") ? new Long(0) : new Long(Double.valueOf(str).longValue());
        } else if (type == short.class || type == Short.class) {
            value = str.equals("") ? new Short((short)0) : new Short(Double.valueOf(str).shortValue());
        } else if (type == byte.class || type == Byte.class) {
            value = str.equals("") ? new Byte((byte)0) : new Byte(Double.valueOf(str).byteValue());
        } else if (type == float.class || type == Float.class) {
            value = str.equals("") ? new Float(0) : new Float(Double.valueOf(str).floatValue());
        } else if (type == double.class || type == Double.class) {
            value = str.equals("") ? new Double(0) : Double.valueOf(str);                                
        } else if (type == char.class || type == Character.class) {                                
            value = new Character(str.charAt(0));
        } else {
            try {
                Field f = type.getField(str.toUpperCase().replace('-', '_'));                        
                value = f.get(null);
            } catch (NoSuchFieldException e2) {
                try {
                    Method m = type.getMethod("valueOf", String.class);
                    if (m.getReturnType() != type) throw new NoSuchMethodException("public static " + type + " valueOf(String value)");
                    value = m.invoke(null, value);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);                
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } catch (IllegalAccessException e2) {
                throw new RuntimeException(e2);
            }
        }
        
        return value;
    }    
}
