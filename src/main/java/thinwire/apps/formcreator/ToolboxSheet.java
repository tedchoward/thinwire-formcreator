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

import thinwire.apps.formcreator.MessageBus.Event;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.FileChooser;
import thinwire.ui.Label;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.DropEvent;
import thinwire.ui.event.DropListener;
import thinwire.ui.layout.*;
import thinwire.ui.layout.TableLayout.Column;
import thinwire.ui.layout.TableLayout.Row;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;

/**
 * @author Joshua J. Gertzen
 */
public class ToolboxSheet extends AbstractSheet {
    private static final int ROW_HEIGHT = 20;
    
    private MessageBus bus;
    private List<Component> dragNDrop = new ArrayList<Component>();
    private Component currentChoice;
    
    public ToolboxSheet(MessageBus bus) {
        super("Toolbox", "AddComponentSheet.png");
        this.bus = bus;
        
        bus.addListener(MessageBus.Id.ADD_TOOLBOX_DRAG_DROP, new MessageBus.Listener() {
            public void eventOccured(Event ev) {
                Component comp = (Component)ev.getData();
                if (comp != null) addDragNDrop(comp); 
            }
        });

        bus.addListener(MessageBus.Id.REMOVE_TOOLBOX_DRAG_DROP, new MessageBus.Listener() {
            public void eventOccured(Event ev) {
                Component comp = (Component)ev.getData();
                if (comp != null) removeDragNDrop(comp); 
            }
        });
        
        getStyle().getBackground().setColor(Color.WINDOW);
        getStyle().getBorder().setSize(2);
        getStyle().getBorder().setType(Border.Type.INSET);
        buildSheet();
    }
    
    private ActionListener toolDoubleClickListener = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            Component choice = (Component)ev.getSource();
            Widget widget = (Widget)choice.getUserObject();
            selectTool(choice);
            bus.fireEvent(MessageBus.Id.ADD_PROJECT_OBJECT, widget.newInstance());
        }        
    };
    
    private DropListener dropListener = new DropListener() {
        public void dropPerformed(DropEvent ev) {
            Component choice = ev.getDragComponent();
            Widget widget = (Widget)choice.getUserObject();
            selectTool(choice);
            
            Component comp = widget.newInstance();
            comp.setPosition(ev.getSourceComponentX(), ev.getSourceComponentY());
            bus.fireEvent(MessageBus.Id.ADD_PROJECT_OBJECT, comp);
        }
    };
    
    private ActionListener toolClickListener = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            selectTool((Component)ev.getSource());
        }
    };
    
    private void addDragNDrop(Component dest) {
        dest.addDropListener(dragNDrop.get(0), dropListener);
    }
    
    private void removeDragNDrop(Component dest) {
        dest.removeDropListener(dropListener);
    }
    
    private void selectTool(Component choice) {
        if (currentChoice != null) {
            currentChoice.getStyle().getBackground().setColor(null);
            currentChoice.getStyle().getFont().setColor(null);
        }

        choice.getStyle().getBackground().setColor(Color.HIGHLIGHT);
        choice.getStyle().getFont().setColor(Color.HIGHLIGHTTEXT);
        currentChoice = choice;
    }
    
    private void addRow(Component comp) {
        Row row = new TableLayout.Row(ROW_HEIGHT);
        TableLayout layout = (TableLayout)getLayout();
        row.add(comp);
        layout.getRows().add(row);
    }
    
    private Label addRow(Widget widget) {
        String name = Main.getSimpleClassName(widget.getType());
        Label lbl = new Label("<img src='" + Main.RES_PATH + name + ".png'/> " + widget.getDisplayName());
        lbl.setUserObject(widget);
        lbl.addActionListener(Label.ACTION_CLICK, toolClickListener);
        lbl.addActionListener(Label.ACTION_DOUBLE_CLICK, toolDoubleClickListener);
        addRow(lbl);
        return lbl;
    }
    
    private void buildSheet() {
        TableLayout layout = new TableLayout();
        setLayout(layout);
        layout.setAutoApply(false);
        layout.setMargin(2);
        layout.setSpacing(2);
        layout.getColumns().add(new Column(0));

        Label lblGroup = new Label("Containers");
        lblGroup.getStyle().getFont().setBold(true);
        lblGroup.getStyle().getBackground().setColor(Color.THREEDFACE);
        addRow(lblGroup);
        
        for (Widget widget : Widget.values()) {
            if (Container.class.isAssignableFrom(widget.getType()) && widget.getType() != FileChooser.class) addRow(widget);
        }
        
        lblGroup = new Label("Common Controls");
        lblGroup.getStyle().getFont().setBold(true);
        lblGroup.getStyle().getBackground().setColor(Color.THREEDFACE);
        addRow(lblGroup);
        
        for (Widget widget : Widget.values()) {
            if (!Container.class.isAssignableFrom(widget.getType()) || widget.getType() == FileChooser.class) dragNDrop.add(addRow(widget));
        }
    }
}
