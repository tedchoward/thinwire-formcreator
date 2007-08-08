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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import thinwire.render.web.WebApplication;
import thinwire.ui.Application;
import thinwire.ui.Button;
import thinwire.ui.CheckBox;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Dialog;
import thinwire.ui.Divider;
import thinwire.ui.DropDownDateBox;
import thinwire.ui.DropDownGridBox;
import thinwire.ui.FileChooser;
import thinwire.ui.Frame;
import thinwire.ui.GridBox;
import thinwire.ui.Hyperlink;
import thinwire.ui.Image;
import thinwire.ui.Label;
import thinwire.ui.Menu;
import thinwire.ui.MessageBox;
import thinwire.ui.Panel;
import thinwire.ui.ProgressBar;
import thinwire.ui.RadioButton;
import thinwire.ui.Slider;
import thinwire.ui.TabFolder;
import thinwire.ui.TabSheet;
import thinwire.ui.TextArea;
import thinwire.ui.TextField;
import thinwire.ui.Tree;
import thinwire.ui.WebBrowser;
import thinwire.ui.layout.SplitLayout;
import thinwire.ui.style.Background;
import thinwire.ui.style.Color;
import thinwire.util.XOD;

/**
 * @author Joshua J. Gertzen
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
        
    static final String RES_PATH = "class:///" + Main.class.getName() + "/resources/";
    static final String[] SIZE_ARY = new String[]{Component.PROPERTY_WIDTH, Component.PROPERTY_HEIGHT};
    static final String[] BOUNDS_ARY = new String[]{Component.PROPERTY_X, Component.PROPERTY_Y, Component.PROPERTY_WIDTH, Component.PROPERTY_HEIGHT};
    static final int EDITOR_HEIGHT = 20;
    static final int BORDER_SIZE = 2;
    static final int SCROLL_BAR_WIDTH = 18;
    
    static String getSimpleClassName(Class type) {
        String text = type.getName();
        text = text.substring(text.lastIndexOf('.') + 1);
        text = text.replace('$', '.');
        return text;
    }

    static Dialog alignCenter(Dialog d) {
        Frame fr = Application.current().getFrame();
        d.setPosition(fr.getInnerWidth() / 2 - d.getWidth() / 2, fr.getInnerHeight() / 2 - d.getHeight() / 2);
        return d;
    }
    
    static PropertyEditor getIdPropertyEditor() {
        List<PropertyEditor> editors = PropertyEditorFactory.getPropertyEditors(Component.class);
        
        for (PropertyEditor ed : editors) {
            if (ed.getName().equals("id")) {
                return ed;
            }
        }
        
        return null;
    }
    
    static String getTextForObject(Object obj, PropertyEditor idEditor) {
        String className = Main.getSimpleClassName(obj.getClass());
        String text = idEditor.getValue(obj).toString();
        
        if (text.length() > 0) {
            text = "<b>" + text + "</b> : ";
        } else if (obj instanceof Label) {
            Component comp = ((Label)obj).getLabelFor();
            
            if (comp != null) {
                text = idEditor.getValue(comp).toString();
                if (text.length() > 0) text = "<i>" + text + "</i> : ";
            }
        }
        
        text += className;
        return text;
    }
    
    static boolean isPrimaryContainer(Object obj) {
        return obj instanceof Container && !(obj instanceof FileChooser);
    }
    
    static XODWriter getContainerXODWriter(ProjectDefinition project, PropertyEditor idEditor, Container... conts) {
        List<String> includes = new ArrayList<String>();
        includes.add("aliasuiclass.xml");
        
        Map<String, Class> aliasMap = new HashMap<String, Class>();
        //XOD xod = new XOD();
        //File file = new File(new File(project.getFileName()).getParentFile(), "aliasuiclass.xml");
        //xod.execute(file.getAbsolutePath());
        //aliasMap.putAll(xod.getAliasMap());
        aliasMap.put("Dialog", DialogWindow.class);
        aliasMap.put("Button", Button.class);
        aliasMap.put("CheckBox", CheckBox.class);
        aliasMap.put("Container", Container.class);
        aliasMap.put("Dialog", Dialog.class);
        aliasMap.put("Divider", Divider.class);
        aliasMap.put("DropDownGridBox", DropDownGridBox.class);
        aliasMap.put("DropDownDateBox", DropDownDateBox.class);
        aliasMap.put("Frame", Frame.class);
        aliasMap.put("GridBox", GridBox.class);
        aliasMap.put("Hyperlink", Hyperlink.class);
        aliasMap.put("Image", Image.class);
        aliasMap.put("Label", Label.class);
        aliasMap.put("Menu", Menu.class);
        aliasMap.put("Panel", Panel.class);
        aliasMap.put("ProgressBar", ProgressBar.class);
        aliasMap.put("RadioButton", RadioButton.class);
        aliasMap.put("RadioButton.Group", RadioButton.Group.class);
        aliasMap.put("Slider", Slider.class);
        aliasMap.put("TabFolder", TabFolder.class);
        aliasMap.put("TabSheet", TabSheet.class);
        aliasMap.put("TextArea", TextArea.class);
        aliasMap.put("TextField", TextField.class);
        aliasMap.put("Tree", Tree.class);
        aliasMap.put("WebBrowser", WebBrowser.class);
        aliasMap.put("GridBox.Column", GridBox.Column.class);
        aliasMap.put("GridBox.Row", GridBox.Row.class);
        
        

        List<Object> rootObjects = new ArrayList<Object>();
        Map<String, Object> objectMap = new HashMap<String, Object>();

        for (Container cont : conts) {
            rootObjects.add(cont);
            populateIds(rootObjects, objectMap, cont, idEditor);
        }
        
        XODWriter writer = new XODWriter(rootObjects, objectMap, aliasMap, includes);
        return writer;
    }
    
    private static void populateIds(List<Object> rootObjects, Map<String, Object> map, Container<Component> cont, PropertyEditor idEditor) {
        String id = (String)idEditor.getValue(cont);
        if (id != null && id.length() > 0) map.put(id, cont);
        
        for (Component comp : cont.getChildren()) {
            if (comp instanceof Container) {
                populateIds(rootObjects, map, (Container)comp, idEditor);
            } else {
                id = (String)idEditor.getValue(comp);
                if (id != null && id.length() > 0) map.put(id, comp);
                
                if (comp instanceof RadioButton) {
                    RadioButton.Group group = ((RadioButton)comp).getGroup();
                    if (!rootObjects.contains(group)) rootObjects.add(group);
                    id = (String)idEditor.getValue(group);
                    if (id != null && id.length() > 0) map.put(id, group);
                }
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        new Main();
    }

    WebApplication app;
    Widget newWidgetType;
    private MessageBus bus;
    private Frame frame;
    private DesignSheet ds;
    private ProjectSheet ls;
    private PropertySheet ps;
    private Component active;
    private Container designer;
    private Map<Component, String> compToId;
    private Map<String, Component> idToComp;
    boolean loadingFile;
    
    Main() throws Exception {
        bus = initMessageBus();
        compToId = new HashMap<Component, String>();
        idToComp = new HashMap<String, Component>();
        app = (WebApplication)Application.current();
        app.clientSideIncludeFile(ComponentDesigner.CLIENT_SIDE_LIB);
        frame = app.getFrame();
        frame.getStyle().getBackground().setImage("class:///thinwire.apps.formcreator.Main/resources/StyleBlueFadeLTR.png");
        frame.getStyle().getBackground().setRepeat(Background.Repeat.Y);
        frame.setTitle("ThinWire Form Creator v1.0");

        Panel sheets = new Panel();
        sheets.getStyle().getBackground().setColor(Color.TRANSPARENT);
        sheets.setLayout(new SplitLayout(.50, false));

        TabFolder tf1 = new TabFolder();
        tf1.getChildren().add(new ProjectSheet(bus).asTabSheet());
        tf1.getChildren().add(new ToolboxSheet(bus).asTabSheet());
        
        TabFolder tf2 = new TabFolder();
        tf2.getChildren().add(new PropertySheet(bus).asTabSheet());
        
        sheets.getChildren().add(tf1);
        sheets.getChildren().add(tf2);
        
        frame.setLayout(new SplitLayout(.25, true, 4));
        frame.getChildren().add(sheets);
        frame.getChildren().add(new ViewTabFolder(bus));

        /*XOD xod = new XOD();
        String fileName = "C:/TEMP/TestApplication.twpj";
        xod.execute(fileName);
        ProjectDefinition project = (ProjectDefinition)xod.getObjectMap().get("project");
        project.initFileName(fileName);*/
        
        ProjectDefinition project = new ProjectDefinition();
        project.setName("Untitled Project");
        XODFile file = new XODFile();
        file.setFileName("Untitled.xml");
        project.getFiles().add(file);
        bus.fireEvent(new MessageBus.Event(MessageBus.Id.SET_ACTIVE_PROJECT, project));
    }
    
    private MessageBus initMessageBus() {
        MessageBus bus = new MessageBus();
        
        /*bus.addListener(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, new MessageBus.Listener() {
            public void eventOccured(Event e) {
                newWidgetType = (Widget)e.getData();
            }
        });*/
        
        return bus;
    }
    
    Component getComponentById(String id) {
        return idToComp.get(id);
    }
    
    String getComponentId(Component comp) {
        String id = compToId.get(comp);
        if (id == null)  throw new IllegalStateException("!compToId.containsKey(comp)");
        return id;
    }
    
    void setComponentId(Component comp, String id) {
        if (!compToId.containsKey(comp)) throw new IllegalStateException("!compToId.containsKey(comp)");
        compToId.put(comp, id);
        idToComp.put(id, comp);
        //ls.setComponentId(comp, id);
    }
    
    void addComponent(Component comp, String id) {
        //ComponentPropertyEditor.ID.setValue(comp, id);
        compToId.put(comp, id);
        idToComp.put(id, comp);
        //comp.setFocusCapable(false);
        //ds.addComponent(comp, id);
        //ls.addComponent(comp, id);
        setActiveComponent(comp);
    }
    
    void removeComponent(Component comp) {
        if (comp == designer) throw new IllegalStateException("you cannot remove the designer form");
        //ds.removeComponent(comp);
        //ls.removeComponent(comp);        
        //ps.setActiveComponent(null);
        idToComp.remove(compToId.remove(comp));
    }
    
    void clearComponents() {        
        for (Component comp : compToId.keySet().toArray(new Component[compToId.size()])) {
            if (comp != designer) removeComponent(comp);
        }
    }
    
    void setActiveComponent(Component comp) {
        if (loadingFile) return;
        if (active != comp) {
            active = comp;
            //ps.setActiveComponent(comp);
            //ls.setActiveComponent(comp);
            //ds.setFocus(true);
        }
    }
    
    Component getActiveComponent() {
        return active;
    }
}
