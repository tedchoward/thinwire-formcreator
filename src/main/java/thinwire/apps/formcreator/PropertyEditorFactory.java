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

import static thinwire.apps.formcreator.ComponentPropertyEditor.*;

import thinwire.ui.*;
import thinwire.ui.style.*;
import thinwire.ui.Container.ScrollType;
import thinwire.ui.AlignTextComponent.AlignX;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Joshua J. Gertzen
 */
class PropertyEditorFactory {
    private static final Logger log = Logger.getLogger(PropertyEditorFactory.class.getName());
    private static final Map<Class, List<PropertyEditor>> typeToList = new HashMap<Class, List<PropertyEditor>>();
    
    public static List<PropertyEditor> getPropertyEditors(Class type) {
        if (Component.class.isAssignableFrom(type)) {
            type = Component.class;
        } else if (ProjectDefinition.class.isAssignableFrom(type)) {
            type = ProjectDefinition.class;
        } else if (XODFile.class.isAssignableFrom(type)) {
            type = XODFile.class;
        } else if (RadioButton.Group.class.isAssignableFrom(type)) {
            type = RadioButton.Group.class;
        } else {
            throw new IllegalArgumentException("No known property editors for type '" + type + "'");
        }

        List<PropertyEditor> editors = typeToList.get(type);
        
        if (editors == null) {
            editors = new ArrayList<PropertyEditor>();

            if (type == ProjectDefinition.class) {
                editors.add(new ProjectPropertyEditor("name", String.class));
            } else if (type == XODFile.class) {
                editors.add(new FilePropertyEditor("fileName", String.class));
            } else if (type == RadioButton.Group.class) {
                editors.add(Main.getIdPropertyEditor());
            } else if (type == Component.class) {
                editors.add(new ComponentPropertyEditor(Object.class, "id", String.class, GROUP_DESIGN));
                
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_X, int.class, GROUP_BOUNDARY));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_Y, int.class, GROUP_BOUNDARY));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_WIDTH, int.class, GROUP_BOUNDARY));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_HEIGHT, int.class, GROUP_BOUNDARY));
                
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_ENABLED, boolean.class));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_VISIBLE, boolean.class));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_FOCUS_CAPABLE, boolean.class));
                editors.add(new ComponentPropertyEditor(Component.class, Component.PROPERTY_USER_OBJECT, Object.class));
                
                editors.add(new ComponentPropertyEditor(ImageComponent.class, ImageComponent.PROPERTY_IMAGE, String.class, GROUP_STATE));
    
                editors.add(new ComponentPropertyEditor(TextComponent.class, TextComponent.PROPERTY_TEXT, String.class, GROUP_STATE));
                
                editors.add(new ComponentPropertyEditor(AlignTextComponent.class, AlignTextComponent.PROPERTY_ALIGN_X, AlignX.class));
                
                editors.add(new ComponentPropertyEditor(EditorComponent.class, EditorComponent.PROPERTY_MAX_LENGTH, int.class));
                
                editors.add(new ComponentPropertyEditor(MaskEditorComponent.class, MaskEditorComponent.PROPERTY_EDIT_MASK, String.class));            
                editors.add(new ComponentPropertyEditor(MaskEditorComponent.class, MaskEditorComponent.PROPERTY_FORMAT_TEXT, boolean.class));
                
                editors.add(new ComponentPropertyEditor(Window.class, Window.PROPERTY_TITLE, String.class, GROUP_STATE));
    
                editors.add(new ComponentPropertyEditor(RangeComponent.class, RangeComponent.PROPERTY_LENGTH, int.class));
                editors.add(new ComponentPropertyEditor(RangeComponent.class, RangeComponent.PROPERTY_CURRENT_INDEX, int.class, GROUP_STATE));
    
                editors.add(new ComponentPropertyEditor(Button.class, Button.PROPERTY_STANDARD, boolean.class));
                editors.add(new ComponentPropertyEditor(Container.class, Container.PROPERTY_SCROLL_TYPE, ScrollType.class));
                editors.add(new ComponentPropertyEditor(CheckBox.class, CheckBox.PROPERTY_CHECKED, boolean.class, GROUP_STATE));
                editors.add(new ComponentPropertyEditor(DialogWindow.class, Dialog.PROPERTY_TITLE, String.class));
                editors.add(new ComponentPropertyEditor(DialogWindow.class, Dialog.PROPERTY_WAIT_FOR_WINDOW, boolean.class));
                editors.add(new ComponentPropertyEditor(DialogWindow.class, Dialog.PROPERTY_RESIZE_ALLOWED, boolean.class));
                editors.add(new ComponentPropertyEditor(DialogWindow.class, Dialog.PROPERTY_REPOSITION_ALLOWED, boolean.class));
                editors.add(new ComponentPropertyEditor(DropDown.class, DropDown.PROPERTY_EDIT_ALLOWED, boolean.class));
                editors.add(new ComponentPropertyEditor(GridBox.class, GridBox.PROPERTY_FULL_ROW_CHECK_BOX, boolean.class));
                editors.add(new ComponentPropertyEditor(GridBox.class, GridBox.PROPERTY_VISIBLE_CHECK_BOXES, boolean.class));
                editors.add(new ComponentPropertyEditor(GridBox.class, GridBox.PROPERTY_VISIBLE_HEADER, boolean.class));
                editors.add(new ComponentPropertyEditor(Hyperlink.class, Hyperlink.PROPERTY_LOCATION, String.class, GROUP_STATE));
                editors.add(new ComponentPropertyEditor(Label.class, Label.PROPERTY_WRAP_TEXT, boolean.class));
                editors.add(new ComponentPropertyEditor(RadioButton.class, RadioButton.PROPERTY_CHECKED, boolean.class, GROUP_STATE));
                editors.add(new ComponentPropertyEditor(TabFolder.class, TabFolder.PROPERTY_CURRENT_INDEX, int.class, GROUP_STATE));
                editors.add(new ComponentPropertyEditor(TextField.class, TextField.PROPERTY_INPUT_HIDDEN, boolean.class));
                editors.add(new ComponentPropertyEditor(Tree.class, Tree.PROPERTY_ROOT_ITEM_VISIBLE, boolean.class));
                editors.add(new ComponentPropertyEditor(WebBrowser.class, WebBrowser.PROPERTY_LOCATION, String.class, GROUP_STATE));
                
                editors.add(new ComponentPropertyEditor(Background.class, Background.PROPERTY_BACKGROUND_COLOR, Color.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Background.class, Background.PROPERTY_BACKGROUND_IMAGE, String.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Background.class, Background.PROPERTY_BACKGROUND_POSITION, Background.Position.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Background.class, Background.PROPERTY_BACKGROUND_REPEAT, Background.Repeat.class, GROUP_STYLE));
                
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_BOLD, boolean.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_ITALIC, boolean.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_UNDERLINE, boolean.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_FAMILY, Font.Family.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_SIZE, double.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Font.class, Font.PROPERTY_FONT_COLOR, Color.class, GROUP_STYLE));
                
                editors.add(new ComponentPropertyEditor(Border.class, Border.PROPERTY_BORDER_TYPE, Border.Type.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Border.class, Border.PROPERTY_BORDER_SIZE, int.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(Border.class, Border.PROPERTY_BORDER_COLOR, Color.class, GROUP_STYLE));
                
                editors.add(new ComponentPropertyEditor(FX.class, FX.PROPERTY_FX_POSITION_CHANGE, Effect.Motion.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(FX.class, FX.PROPERTY_FX_SIZE_CHANGE, Effect.Motion.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(FX.class, FX.PROPERTY_FX_VISIBLE_CHANGE, Effect.Motion.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(FX.class, FX.PROPERTY_FX_OPACITY_CHANGE, Effect.Motion.class, GROUP_STYLE));
                editors.add(new ComponentPropertyEditor(FX.class, FX.PROPERTY_FX_COLOR_CHANGE, Effect.Motion.class, GROUP_STYLE));
            }
            
            typeToList.put(type, editors);
        }
        
        return editors;
    }
}
