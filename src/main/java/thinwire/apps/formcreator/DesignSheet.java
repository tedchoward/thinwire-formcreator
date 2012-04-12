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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import thinwire.apps.formcreator.MessageBus.Event;
import thinwire.ui.*;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.TableLayout;
import thinwire.ui.style.Color;

/**
 * @author Joshua J. Gertzen
 */
class DesignSheet extends TabSheet {
    private DesignGrid grid;
    private Panel form;
    private Container<Component> cont;
    private Map<Component, ComponentDesigner> compToDesign = new HashMap<Component, ComponentDesigner>();
    private PropertyEditor idEditor;
    private MessageBus bus;
    
    private PropertyChangeListener idEditorListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getSource() == cont) {
                setText(Main.getTextForObject(cont, idEditor));
            }
        }
    };
    
    private PropertyChangeListener containerSizeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals(Component.PROPERTY_WIDTH)) {
                grid.setWidth((Integer)pce.getNewValue());
            } else {
                grid.setHeight((Integer)pce.getNewValue());
            }
        }
    };
    
    private MessageBus.Listener addComponentListener = new MessageBus.Listener() {
        public void eventOccured(Event ev) {
            if (getContainer() != null && ((TabFolder)getContainer()).getCurrentIndex() == ((TabFolder)getContainer()).getChildren().indexOf(DesignSheet.this)) {
                if (ev.getData() instanceof Component) {
                    Component comp = (Component)ev.getData();
                    
                    if (!compToDesign.containsKey(comp)) {
                        compToDesign.put(comp, new ComponentDesigner(bus, comp));
                    }
                }
            }
        }
    };

    private MessageBus.Listener deleteComponentListener = new MessageBus.Listener() {
        public void eventOccured(Event ev) {
            if (getContainer() != null && ((TabFolder)getContainer()).getCurrentIndex() == ((TabFolder)getContainer()).getChildren().indexOf(DesignSheet.this)) {
                if (ev.getData() instanceof Component) {
                    Component comp = (Component)ev.getData();
                    ComponentDesigner designer = compToDesign.get(comp);
                    
                    if (designer != null) {
                        designer.destroy();
                        cont.getChildren().remove(designer);
                    }
                }
            }
        }
    };
    
    DesignSheet(final MessageBus bus, final ProjectDefinition project, final Container<Component> cont) {
        this.bus = bus;
        this.cont = cont;
        idEditor = Main.getIdPropertyEditor();
        setText(Main.getTextForObject(cont, idEditor));
        setImage(Main.RES_PATH + Main.getSimpleClassName(cont.getClass()) + ".png");
        Application app = Application.current();

        bus.addListener(MessageBus.Id.ADD_PROJECT_OBJECT, addComponentListener);
        //bus.addListener(MessageBus.Id.DELETE_PROJECT_OBJECT, deleteComponentListener);
        idEditor.addPropertyChangeListener(idEditorListener);
        
        addActionListener(TabSheet.ACTION_DOUBLE_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                bus.fireEvent(MessageBus.Id.CLOSE_DESIGN_SHEET, cont);
            }
        });
        
        form = new Panel();
        form.setScrollType(ScrollType.ALWAYS);
        form.getStyle().getBackground().setColor(Color.APPWORKSPACE);
        form.getStyle().getBorder().copy(app.getDefaultStyle(GridBox.class).getBorder());
        form.setPosition(Main.BORDER_SIZE, Main.BORDER_SIZE);

        grid = new DesignGrid();
        grid.setSize(cont.getInnerWidth(), cont.getInnerHeight());

        bus.fireEvent(MessageBus.Id.ADD_TOOLBOX_DRAG_DROP, cont);
        
        cont.addPropertyChangeListener(Main.SIZE_ARY, containerSizeListener);
        
        cont.getChildren().add(0, grid);
        form.getChildren().add(cont.setPosition(20, 20));
        
        setLayout(new TableLayout(new double[][]{{0},{22, 0}}, 2, 4));

        final TextArea source = new TextArea();
        source.setEnabled(false);
        source.setVisible(false);
        
        Toolbar tb = new Toolbar(true);
        tb.add(new AbstractAction("Design", Main.RES_PATH + "DesignMode.png") {
            public void run(Object obj) {
                source.setVisible(false);
                form.setVisible(true);
            }
        });

        tb.add(new AbstractAction("Source", Main.RES_PATH + "File.png") {
            public void run(Object obj) {
                form.setVisible(false);
                source.setVisible(true);
                XODWriter writer = Main.getContainerXODWriter(project, idEditor, cont);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                writer.writeTo(baos);
                source.setText(baos.toString());
            }
        });
        
        getChildren().add(tb);
        getChildren().add(form.setLimit("0, 1"));
        getChildren().add(source.setLimit("0, 1"));
        
        for (Component comp : cont.getChildren().toArray(new Component[cont.getChildren().size()])) {
            if (!(comp instanceof DesignTimeComponent)) compToDesign.put(comp, new ComponentDesigner(bus, comp));
        }
        
        /*sourceTab = new TabSheet("Source", Main.RES_PATH + "File.png");
        source = new TextArea();
        source.getStyle().getFont().setFamily(Font.Family.MONOSPACE);
        source.setPosition(Main.BORDER_SIZE, Main.BORDER_SIZE);
        sourceTab.getChildren().add(source);
        sourceTab.addPropertyChangeListener(Main.SIZE_ARY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getPropertyName().equals(Component.PROPERTY_WIDTH)) {
                    source.setWidth(sourceTab.getInnerWidth() - Main.BORDER_SIZE * 2);
                } else {
                    source.setHeight(sourceTab.getInnerHeight() - Main.BORDER_SIZE * 2);
                }
            }
        });*/
        
        /*getChildren().add(layoutTab);
        getChildren().add(sourceTab);
        
        ds = new DesignSheet(this);
        ds.addKeyPressListener(new String[] {"ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight",
                "Ctrl-ArrowUp", "Ctrl-ArrowDown", "Ctrl-ArrowLeft", "Ctrl-ArrowRight"}, positionKPL);
        ds.addKeyPressListener(new String[] {"Shift-ArrowUp", "Shift-ArrowDown", "Shift-ArrowLeft", "Shift-ArrowRight",
                "Ctrl-Shift-ArrowUp", "Ctrl-Shift-ArrowDown", "Ctrl-Shift-ArrowLeft", "Ctrl-Shift-ArrowRight"}, sizeKPL);*/
    }
    
    void destroy() {
        bus.removeListener(MessageBus.Id.ADD_PROJECT_OBJECT, addComponentListener);
        bus.removeListener(MessageBus.Id.DELETE_PROJECT_OBJECT, deleteComponentListener);
        bus.fireEvent(MessageBus.Id.REMOVE_TOOLBOX_DRAG_DROP, cont);

        for (Component comp : cont.getChildren().toArray(new Component[cont.getChildren().size()])) {
            if (comp instanceof DesignTimeComponent) { 
                if (comp instanceof ComponentDesigner) ((ComponentDesigner)comp).destroy();
                cont.getChildren().remove(comp);
            }
        }
        
        form.getChildren().remove(cont);

        idEditor.removePropertyChangeListener(idEditorListener);
        cont.removePropertyChangeListener(containerSizeListener);
        compToDesign.clear();
    }
    
    /*Panel getDesignContainer() {
        return design;
    }*/
    
    /*private KeyPressListener positionKPL = new KeyPressListener() {
        public void keyPress(KeyPressEvent ev) {
            String keyPressCombo = ev.getKeyPressCombo();
            Component comp = getActiveComponent();
            
            if (keyPressCombo.equals("ArrowLeft")) {
                int v = comp.getX() - 1;
                comp.setX(v < 0 ? 0 : v);
            } else if (keyPressCombo.equals("ArrowRight")) {
                int v = comp.getX() + 1;
                comp.setX(v > comp.getContainer().getInnerWidth() ? comp.getContainer().getInnerWidth() : v);
            } else if (keyPressCombo.equals("ArrowUp")) {
                int v = comp.getY() - 1;
                comp.setY(v < 0 ? 0 : v);
            } else if (keyPressCombo.equals("ArrowDown")) {
                int v = comp.getY() + 1;
                comp.setY(v > comp.getContainer().getInnerHeight() ? comp.getContainer().getInnerHeight() : v);
            } else if (keyPressCombo.equals("Ctrl-ArrowLeft")) {
                int v = getGridOffset(comp.getX(), true);
                comp.setX(comp.getX() + v);
            } else if (keyPressCombo.equals("Ctrl-ArrowRight")) {
                int v = getGridOffset(comp.getX(), false);
                comp.setX(comp.getX() + v);
            } else if (keyPressCombo.equals("Ctrl-ArrowUp")) {
                int v = getGridOffset(comp.getY(), true);
                comp.setY(comp.getY() + v);
            } else if (keyPressCombo.equals("Ctrl-ArrowDown")) {
                int v = getGridOffset(comp.getY(), false);
                comp.setY(comp.getY() + v);
            }
        }
    };

    private KeyPressListener sizeKPL = new KeyPressListener() {
        public void keyPress(KeyPressEvent ev) {
            String keyPressCombo = ev.getKeyPressCombo();
            Component comp = getActiveComponent();
            
            if (keyPressCombo.equals("Shift-ArrowLeft")) {
                int v = comp.getWidth() - 1;
                comp.setWidth(v < 0 ? 0 : v);
            } else if (keyPressCombo.equals("Shift-ArrowRight")) {
                int v = comp.getWidth() + 1;
                comp.setWidth(v > comp.getContainer().getInnerWidth() ? comp.getContainer().getInnerWidth() : v);
            } else if (keyPressCombo.equals("Shift-ArrowUp")) {
                int v = comp.getHeight() - 1;
                comp.setHeight(v < 0 ? 0 : v);
            } else if (keyPressCombo.equals("Shift-ArrowDown")) {
                int v = comp.getHeight() + 1;
                comp.setHeight(v > comp.getContainer().getInnerHeight() ? comp.getContainer().getInnerHeight() : v);
            } else if (keyPressCombo.equals("Ctrl-Shift-ArrowLeft")) {
                int v = getGridOffset(comp.getX() + comp.getWidth(), true);
                comp.setWidth(comp.getWidth() + v);
            } else if (keyPressCombo.equals("Ctrl-Shift-ArrowRight")) {
                int v = getGridOffset(comp.getX() + comp.getWidth(), false);
                comp.setWidth(comp.getWidth() + v);
            } else if (keyPressCombo.equals("Ctrl-Shift-ArrowUp")) {
                int v = getGridOffset(comp.getY() + comp.getHeight(), true);
                comp.setHeight(comp.getHeight() + v);
            } else if (keyPressCombo.equals("Ctrl-Shift-ArrowDown")) {
                int v = getGridOffset(comp.getY() + comp.getHeight(), false);
                comp.setHeight(comp.getHeight() + v);
            }
        }
    };
    
    private int getGridOffset(int position, boolean previous) {
        int change = position % 5;
        return previous ? change - 5 : 5 - change;
    }*/
    
    /*
    void addComponent(Component comp, String id) {
        if (comp == design) return;
        design.getChildren().add(comp);
        new ComponentDesigner(fc, comp);
    }
    
    void removeComponent(Component comp) {
        if (comp == design) return;
        ComponentDesigner.getInstance(comp).destroy();
        design.getChildren().remove(comp);        
    }
    
    void setSource(String uri) {
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fc.app.getRelativeFile(uri))));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            source.setText(sb.toString());
        } catch (Exception e) {
            source.setText("");
        }
    }
        
    int getSheetWidth() {
        return getWidth();
    }*/
}
