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
import java.util.logging.Logger;

import thinwire.apps.formcreator.MessageBus.Event;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Divider;
import thinwire.ui.DropDownGridBox;
import thinwire.ui.EditorComponent;
import thinwire.ui.Label;
import thinwire.ui.MaskEditorComponent;
import thinwire.ui.Panel;
import thinwire.ui.Container.ScrollType;
import thinwire.ui.TabSheet;
import thinwire.ui.TextComponent;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.TableLayout;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;

/**
 * @author Joshua J. Gertzen
 */
class PropertySheet extends AbstractSheet {
    private static final Logger log = Logger.getLogger(PropertySheet.class.getName());
    private static final int ROW_HEIGHT = 20;

    private class SheetInfo {
        Map<String, MaskEditorComponent> propToEditor = new HashMap<String, MaskEditorComponent>();
        Panel sheet;
    }
    
    private MessageBus bus;
    private Map<Class, SheetInfo> typeToSheet = new HashMap<Class, SheetInfo>();
    private SheetInfo current;
    private MaskEditorComponent activeEditor;
    private Object activeObject;
    
    PropertySheet(MessageBus bus) {
        super("Properties", "PropertySheet.png");
        this.bus = bus;
        setLayout(new TableLayout(new double[][]{{0},{0}}, 2));
        getStyle().getBackground().setColor(Color.WINDOW);
        getStyle().getBorder().setSize(2);
        getStyle().getBorder().setType(Border.Type.INSET);
        
        bus.addListener(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                setActiveObject(ev.getData());
            }
        });
        
        bus.addListener(MessageBus.Id.SET_PROPERTY_SHEET_VALUE, new MessageBus.Listener() {
            public void eventOccured(Event ev) {
                Object[] nameValue = (Object[])ev.getData();
                if (current == null) return;
                MaskEditorComponent editor = current.propToEditor.get((String)nameValue[0]);
                if (editor != null) editor.setText(AbstractPropertyEditor.toStringValue(nameValue[1]));
            }
        });
    }
    
    private void setActiveObject(Object obj) {
        if (activeObject == obj) return;
        Class type = obj instanceof Component ? Component.class : obj.getClass();
        SheetInfo info = typeToSheet.get(type);
        activeObject = null;
        
        if (info == null) {
            info = new SheetInfo();
            List<PropertyEditor> editors = PropertyEditorFactory.getPropertyEditors(type);
            buildPanel(info, editors);
            typeToSheet.put(type, info);
        }
        
        if (current != null && current != info) getChildren().remove(current.sheet);
        
        for (Component comp : info.sheet.getChildren()) {
            if (comp instanceof MaskEditorComponent) {
                PropertyEditor ped = (PropertyEditor)comp.getUserObject();
                boolean show = ped.isValidFor(obj);
                
                if (show) {
                    ((MaskEditorComponent)comp).setText(String.valueOf(ped.getValue(obj)));
                    //setValueStyle(obj, ped, comp);
                }
                
                TableLayout.Range limit = (TableLayout.Range)comp.getLimit();
                limit.getRow().setVisible(show);
            }
        }
        
        TableLayout layout = (TableLayout)info.sheet.getLayout();
        int height = ((layout.getVisibleRows().size() + layout.getSpacing()) * ROW_HEIGHT) + layout.getMargin() * 2;
        layout = (TableLayout)getLayout();
        height += layout.getMargin() * 2 + ROW_HEIGHT;
        
        //log.info("Row height=" + layout.getRows().get(0).getHeight());
        //p.setVisible(true);
        layout.getRows().get(0).setHeight(height);
        //layout.apply();        
        //log.info("Property Panel Height=" + height + ", panel height=" + p.getHeight() + ", limit=" + p.getLimit() + ", visibleRow.size=" + layout.getVisibleRows().size());
        if (info.sheet.getContainer() == null) getChildren().add(info.sheet);
        current = info;
        activeObject = obj;
    }
    
    private void buildPanel(SheetInfo info, List<PropertyEditor> editors) {
        Panel p = new Panel();
        TableLayout layout = new TableLayout();
        p.setLayout(layout);
        layout.setSpacing(1);
        layout.getColumns().add(new TableLayout.Column(.4));
        layout.getColumns().add(new TableLayout.Column(0));
        
        Set<String> groups = new TreeSet<String>();
        
        for (PropertyEditor ped : editors) {
            groups.add(ped.getGroup());
        }
        
        for (String group : groups) {
            TableLayout.Row row = new TableLayout.Row(ROW_HEIGHT);
            Label lblGroup = new Label(group);
            lblGroup.getStyle().getFont().setBold(true);
            lblGroup.getStyle().getFont().setColor(Color.THREEDDARKSHADOW);
            lblGroup.getStyle().getBackground().setColor(Color.THREEDFACE);
            row.add(lblGroup);
            layout.getRows().add(row);
            TableLayout.Range lmtGroup = (TableLayout.Range)lblGroup.getLimit();
            lmtGroup = new TableLayout.Range(layout, lmtGroup.getColumnIndex(), lmtGroup.getRowIndex(), 2, 1);
            lblGroup.setLimit(lmtGroup);
            
            for (PropertyEditor ped : editors) {
                if (!ped.getGroup().equals(group)) continue;
                MaskEditorComponent editor = ped.newEditor();
                info.propToEditor.put(ped.getName(), editor);
                editor.getStyle().getBorder().setSize(0);
                editor.setUserObject(ped);
                editor.addPropertyChangeListener(Component.PROPERTY_FOCUS, focusChangeListener);
                editor.addPropertyChangeListener(MaskEditorComponent.PROPERTY_TEXT, textChangeListener);
    
                Label lbl = new Label(ped.getName());
                lbl.getStyle().getBackground().setColor(Color.WINDOW);
                lbl.setLabelFor(editor);
                lbl.addActionListener(Label.ACTION_CLICK, propertyLabelClick);
                
                row = new TableLayout.Row(ROW_HEIGHT);
                row.add(lbl);
                row.add(editor);
                layout.getRows().add(row);
            }
        }

        info.sheet = p;
    }
    
    private void setValueStyle(Object obj, PropertyEditor ped, Component comp) {
        Object dValue = ped.getDefaultValue(obj);
        comp.getStyle().getFont().setBold(dValue != null && !dValue.equals(ped.getValue(obj)));
    }
    
    private ActionListener propertyLabelClick = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            MaskEditorComponent editor = (MaskEditorComponent)((Label)ev.getSourceComponent()).getLabelFor();
            selectProperty(editor);
        }
    };
    
    private PropertyChangeListener focusChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getNewValue() == Boolean.TRUE) selectProperty((MaskEditorComponent)ev.getSourceComponent());
        }
    };
    
    private void selectProperty(MaskEditorComponent editor) {
        if (activeEditor != null) {
            Label lbl = activeEditor.getLabel();
            lbl.getStyle().getBackground().setColor(Color.WINDOW);
            lbl.getStyle().getFont().setColor(null);
        }

        Label lbl = editor.getLabel();

        activeEditor = editor;
        lbl.getStyle().getBackground().setColor(Color.HIGHLIGHT);
        lbl.getStyle().getFont().setColor(Color.HIGHLIGHTTEXT);
    }
    
    private PropertyChangeListener textChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
            if (activeObject == null) return;
            MaskEditorComponent comp = (MaskEditorComponent)pce.getSourceComponent();
            PropertyEditor ped = (PropertyEditor)comp.getUserObject();
            Object oldValue = ped.getValue(activeObject);
            
            try {
                ped.setValue(activeObject, comp.getText());
                //setValueStyle(activeObject, ped, comp);
                comp.getStyle().getBackground().setColor(null);
            } catch (Exception e) {
                comp.getStyle().getBackground().setColor(Color.SALMON);
                ped.setValue(activeObject, oldValue);
            }
        }
    };
}
