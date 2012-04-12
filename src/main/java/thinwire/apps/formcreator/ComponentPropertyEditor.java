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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thinwire.ui.AlignTextComponent;
import thinwire.ui.AlignTextComponent.AlignX;
import thinwire.ui.Button;
import thinwire.ui.CheckBox;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Dialog;
import thinwire.ui.DropDown;
import thinwire.ui.DropDownGridBox;
import thinwire.ui.EditorComponent;
import thinwire.ui.GridBox;
import thinwire.ui.Hyperlink;
import thinwire.ui.ImageComponent;
import thinwire.ui.Label;
import thinwire.ui.MaskEditorComponent;
import thinwire.ui.RadioButton;
import thinwire.ui.RangeComponent;
import thinwire.ui.Container.ScrollType;
import thinwire.ui.TabFolder;
import thinwire.ui.TextComponent;
import thinwire.ui.TextField;
import thinwire.ui.Tree;
import thinwire.ui.WebBrowser;
import thinwire.ui.Window;
import thinwire.ui.style.Background;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.FX;
import thinwire.ui.style.Effect;
import thinwire.ui.style.Font;

/**
 * @author Joshua J. Gertzen
 */
class ComponentPropertyEditor extends AbstractPropertyEditor<Object> {
    private static final String[] TRUE_FALSE = new String[]{"true", "false"};
    
    static final String GROUP_DESIGN = "Design";
    static final String GROUP_STATE = "State";
    static final String GROUP_BEHAVIOR = "Behavior";
    static final String GROUP_BOUNDARY = "Boundary";
    static final String GROUP_STYLE = "Style";
    
    ComponentPropertyEditor(Class objectType, String name, Class type) {
        this(objectType, name, type, GROUP_BEHAVIOR);
    }
    
    ComponentPropertyEditor(Class objectType, String name, Class type, String group) {
        init(objectType, type, name, group);
        if (group.equals(GROUP_STYLE)) name = name.substring(Main.getSimpleClassName(objectType).length());
        initStandardAccessors(name);
    }
    
    public void setValue(Object comp, Object value) {
        Class objectType = getDeclaringType();
        
        if (objectType == Background.class) {
            setValueForType(((Component)comp).getStyle().getBackground(), value);
        } else if (objectType == Font.class) {
            setValueForType(((Component)comp).getStyle().getFont(), value);
        } else if (objectType == Border.class) {
            setValueForType(((Component)comp).getStyle().getBorder(), value);
        } else if (objectType == FX.class) {
            setValueForType(((Component)comp).getStyle().getFX(), value);
        } else {
            super.setValue(comp, value);
        }
    }

    public Object getValue(Object comp) {
        Class objectType = getDeclaringType();
        
        if (objectType == Background.class) {
            return getValueForType(((Component)comp).getStyle().getBackground());
        } else if (objectType == Font.class) {
            return getValueForType(((Component)comp).getStyle().getFont());
        } else if (objectType == Border.class) {
            return getValueForType(((Component)comp).getStyle().getBorder());
        } else if (objectType == FX.class) {
            return getValueForType(((Component)comp).getStyle().getFX());
        } else {            
            return super.getValue(comp);
        }
    }
    
    /*public Object getDefaultValue(Component comp) {
        for (Widget w : Widget.values()) {           
            if (w.getType().isInstance(comp)) {
                return w.getDefaultValue(name);
            }
        }
        
        return null;
    }*/
    
    public boolean isValidFor(Object comp) {        
        if (getDeclaringType().isInstance(comp) || getGroup().equals(GROUP_STYLE)) {
            if (getName().equals(TabFolder.PROPERTY_SCROLL_TYPE) && comp instanceof TabFolder) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public MaskEditorComponent newEditor() {
        String name = getName();
        Class type = getType();
        String[] options = null;
        boolean editAllowed = true;
        String editMask = null;
        int maxLength = -1;
        AlignX alignX = null;
        
        if (type == boolean.class) {
            options = TRUE_FALSE;
            editAllowed = false;
        } else if (type == int.class) {
            editMask = name == Component.PROPERTY_X || name == Component.PROPERTY_Y ? "-####" : "####";
            alignX = AlignX.RIGHT;
        } else if (name == MaskEditorComponent.PROPERTY_EDIT_MASK) {
            options = new String[] {
                    "-###,###,###.##",
                    "###.####",
                    "MM/dd/yyyy",
                    "MM/dd/yy",
                    "hh:mm",
                    "999-99-9999",
                    "99-9999999",
                    "(999) 999-9999",
                    "XXXXXXXX",
                    "AAAAAAAA",
                    "aaaaaaaa",
            };
        } else if (name == ImageComponent.PROPERTY_IMAGE || name == Background.PROPERTY_BACKGROUND_IMAGE) {
            Widget[] values = Widget.values();
            options = new String[values.length + 2];
            options[0] = Main.RES_PATH + "File.png";
            options[1] = Main.RES_PATH + "Folder.png";
            
            for (int i = 0; i < values.length; i++) {
                options[i + 2] = Main.RES_PATH + Main.getSimpleClassName(values[i].getType()) + ".png";
            }
        } else if (name == WebBrowser.PROPERTY_LOCATION || name == Hyperlink.PROPERTY_LOCATION) {
            options = new String[] {
                    "http://www.thinwire.com",
                    "http://www.customcreditsystems.com",
                    "http://www.truecode.org"
            };
        } else if (Enum.class.isAssignableFrom(type)) {            
            try {
                editAllowed = false;
                Enum[] values = (Enum[])type.getMethod("values").invoke(null);                
                options = new String[values.length];
                
                for (int i = 0; i < values.length; i++) {
                    options[i] = values[i].name().toLowerCase();
                }                
            } catch (Exception e) {
                if (e instanceof RuntimeException) throw (RuntimeException)e;
                throw new RuntimeException(e);
            }            
        } else if (type == Color.class) {
            Color[] values = Color.values();
            options = new String[values.length + 2];
            editAllowed = true;
            options[0] = "rgb(255, 128, 0)";
            options[1] = "#5F9EA0";

            for (int i = 0; i < values.length; i++) {
                options[i + 2] = values[i].name().toLowerCase();
            }
        } else if (type == Font.Family.class) {
            Font.Family[] values = Font.Family.values();
            options = new String[values.length + 1];
            editAllowed = true;
            options[0] = "Tahoma, sans-serif";

            for (int i = 0; i < values.length; i++) {
                options[i + 1] = values[i].toString();
            }
        } else if (type == Background.Position.class) {
            Background.Position[] values = Background.Position.values();
            options = new String[values.length];
            editAllowed = true;
            
            for (int i = 0; i < values.length; i++) {
                options[i] = values[i].toString();
            }
        }
        
        MaskEditorComponent editor;
        
        if (options != null) {
            DropDownGridBox dd = new DropDownGridBox();
            dd.getComponent().getColumns().add(new GridBox.Column((Object[])options));
            dd.setEditAllowed(editAllowed);
            editor = dd;
        } else {
            editor = new TextField();
        }
        
        if (editMask != null) {
            editor.setEditMask(editMask);
        } else if (maxLength != -1) {
            editor.setMaxLength(maxLength);
        }
        
        if (alignX != null) editor.setAlignX(alignX);        
        return editor;
    }
}
