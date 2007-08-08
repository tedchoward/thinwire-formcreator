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
import java.io.*;
import java.util.logging.Logger;

import thinwire.ui.*;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.TableLayout;
import thinwire.util.XOD;

/**
 * @author Joshua J. Gertzen
 */
class ProjectSheet extends AbstractSheet {
    private static final Logger log = Logger.getLogger(ProjectSheet.class.getName());
    
    private MessageBus bus;
    private Tree searchTree;
    private Map<Object, Tree.Item> objToItem;
    private ProjectDefinition activeProject;
    private PropertyEditor idEditor;
        
    ProjectSheet(MessageBus bus) {
        super("Project", "Project.png");
        this.bus = bus;
        objToItem = new HashMap<Object, Tree.Item>();
        idEditor = Main.getIdPropertyEditor();
        
        setLayout(new TableLayout(new double[][]{{0},{20, 0}}, 2, 2));

        Toolbar tb = new Toolbar();
        getChildren().add(tb);
        
        //searchTree = new FilterTree();
        searchTree = new Tree();
        searchTree.setRootItemVisible(true);
        searchTree.addPropertyChangeListener(Tree.Item.PROPERTY_ITEM_SELECTED, treeSelectListener);
        searchTree.addPropertyChangeListener(Tree.Item.PROPERTY_ITEM_EXPANDED, treeExpandListener);
        searchTree.addActionListener(Tree.ACTION_DOUBLE_CLICK, treeDoubleClickListener);
        
        getChildren().add(searchTree.setLimit("0, 1"));
        
        bus.addListener(MessageBus.Id.SET_ACTIVE_PROJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                if (ev.getData() instanceof ProjectDefinition) {
                    ProjectDefinition project = (ProjectDefinition)ev.getData();
                    setActiveProject(project);
                }
            }
        });
        
        bus.addListener(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                objToItem.get(ev.getData()).setSelected(true);
            }
        });

        bus.addListener(MessageBus.Id.ADD_PROJECT_OBJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                if (ev.getData() instanceof Component) {
                    Component comp = (Component)ev.getData();
                    Tree.Item selectedItem = searchTree.getSelectedItem();
                    addComponent(selectedItem, comp);
                }
            }
        });
        
        bus.addListener(MessageBus.Id.DELETE_PROJECT_OBJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                Object obj = (Object)ev.getData();
                Tree.Item item = objToItem.get(obj);
                if (item == null) return;
                
                Object parent = item.getParent();
                
                if (parent instanceof Tree.Item) {
                    Object uo = ((Tree.Item)parent).getUserObject();
                    
                    if (uo instanceof XODFile && obj instanceof Container) {
                        ProjectSheet.this.bus.fireEvent(MessageBus.Id.CLOSE_DESIGN_SHEET, (Container)obj);
                    } else if (obj instanceof Component) {
                        Component comp = (Component)obj;
                        if (comp.getContainer() != null) comp.getContainer().getChildren().remove(comp);
                    }
                    
                    removeObjectToItemEntries(item);
                    ((Tree.Item)parent).getChildren().remove(item);
                }
            }
        });
        
        bus.addListener(MessageBus.Id.OPEN_PROJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                Map<String, String> fileTypes = new HashMap<String, String>();
                fileTypes.put("*.twpj", "ThinWire Form Creator Project");
                final FilePicker picker = new FilePicker(false, "C:/", fileTypes);
                picker.setVisible(true);

                picker.addPropertyChangeListener(FilePicker.PROPERTY_VISIBLE, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ev.getNewValue() == Boolean.FALSE) {
                            File file = picker.getSelectedFile();

                            if (file != null) {
                            }
                        }
                    }
                });
            }
        });
        
        bus.addListener(MessageBus.Id.SAVE_PROJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                try {
                    File file = (File)ev.getData();
                    log.info("saving project file=" + file.getAbsolutePath());
    
                    for (Iterator<XODFile> it = activeProject.getFiles().iterator(); it.hasNext();) {
                        XODFile xf = it.next();
                        Tree.Item xfItem = objToItem.get(xf);
                        
                        if (xfItem == null) {
                            it.remove();
                        } else {
                            loadBranch(xfItem);
                            List<Container> lst = new ArrayList<Container>();
                            
                            for (Tree.Item xfChild : xfItem.getChildren()) {
                                Object uo = xfChild.getUserObject();
                                if (uo instanceof Container) lst.add((Container)uo);
                            }
                            
                            XODWriter writer = Main.getContainerXODWriter(activeProject, idEditor, lst.toArray(new Container[lst.size()]));
                            File xodFile = new File(file.getParentFile(), xf.getFileName());
                            if (!xodFile.exists()) xodFile.createNewFile();
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(xodFile));
                            writer.writeTo(os);
                            os.close();
                        }
                    }
                    
                    List<Object> rootObjects = new ArrayList<Object>();
                    rootObjects.add(activeProject);
                    
                    Map<String, Object> objectMap = new HashMap<String, Object>();
                    objectMap.put("project", activeProject);
                    
                    Map<String, Class> aliasMap = new HashMap<String, Class>();
                    //The underscore forces the XODWriter to include an alias link
                    aliasMap.put("_" + Main.getSimpleClassName(ProjectDefinition.class), ProjectDefinition.class);
                    aliasMap.put("_" + Main.getSimpleClassName(XODFile.class), XODFile.class);
                    
                    XODWriter writer = new XODWriter(rootObjects, objectMap, aliasMap, null);
                    
                    if (!file.exists()) file.createNewFile();
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                    writer.writeTo(os);
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        
        bus.addListener(MessageBus.Id.IMPORT_XOD, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                Map<String, String> fileTypes = new HashMap<String, String>();
                fileTypes.put("*.xml", "XML Object Definition");
                final FilePicker picker = new FilePicker(false, "C:/", fileTypes);
                picker.setVisible(true);

                picker.addPropertyChangeListener(FilePicker.PROPERTY_VISIBLE, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ev.getNewValue() == Boolean.FALSE) {
                            File file = picker.getSelectedFile();

                            if (file != null) {
                                //XOD xod = new XOD();
                                
                            }
                        }
                    }
                });
            }
        });
        
        idEditor.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                Tree.Item item = objToItem.get(ev.getSource());
                if (item != null) item.setText(Main.getTextForObject(ev.getSource(), idEditor));                    
            }
        });

        tb.add(new AbstractAction("Open", Main.RES_PATH + "Open.png", "Ctrl-O") {
            public void run(Object obj) {
                ProjectSheet.this.bus.fireEvent(MessageBus.Id.OPEN_PROJECT, null);
            }
        });

        tb.add(new AbstractAction("Save", Main.RES_PATH + "Save.png", "Ctrl-S") {
            public void run(Object obj) {
                File file = new File(activeProject.getFileName());
                ProjectSheet.this.bus.fireEvent(MessageBus.Id.SAVE_PROJECT, file);
            }
        });

        tb.add(new AbstractAction("Save As", Main.RES_PATH + "SaveAs.png", null) {
            public void run(Object obj) {
                Map<String, String> fileTypes = new HashMap<String, String>();
                fileTypes.put("*.twpj", "ThinWire Form Creator Project");
                fileTypes.put("*.*", "All Files");
                final FilePicker picker = new FilePicker(true, "C:/", fileTypes);
                picker.setVisible(true);

                picker.addPropertyChangeListener(FilePicker.PROPERTY_VISIBLE, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ev.getNewValue() == Boolean.FALSE) {
                            File file = picker.getSelectedFile();
                            
                            if (file != null) {
                                if (file.exists()) {
                                    if (MessageBox.confirm(null, null, "Overwrite existing project file?\n" + file.getAbsolutePath(), "OK|Cancel") == 0) {
                                        ProjectSheet.this.bus.fireEvent(MessageBus.Id.SAVE_PROJECT, file);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        tb.addDivider();
        
        /*tb.add(new AbstractAction("Import File", Main.RES_PATH + "Import.png", null) {
            public void run(Object obj) {
                ProjectSheet.this.bus.fireEvent(MessageBus.Id.IMPORT_XOD, null);
            }
        });*/
        
        tb.add(new AbstractAction("Import From Class", Main.RES_PATH + "Import.png", null) {
            public void run(Object obj) {
                //ProjectSheet.this.bus.fireEvent(MessageBus.Id.IMPORT_XOD, null);
                new ImportFromClass(ProjectSheet.this.bus).setVisible(true);
            }
        });
        
        tb.addDivider();        
        
        tb.add(new AbstractAction("Delete", Main.RES_PATH + "Delete.png", "Ctrl-D") {
            public void run(Object obj) {
                ProjectSheet.this.bus.fireEvent(MessageBus.Id.DELETE_PROJECT_OBJECT, searchTree.getSelectedItem().getUserObject());
            }
        });        
    }
    
    private void removeObjectToItemEntries(Tree.Item parent) {
        for (Tree.Item item : parent.getChildren()) {
            if (item.hasChildren()) {
                removeObjectToItemEntries(item);
            } else {
                objToItem.remove(item.getUserObject());
                item.setUserObject(null);
            }
        }

        objToItem.remove(parent.getUserObject());
        parent.setUserObject(null);
    }

    private void alertInvalidAdd(Component comp) {
        MessageBox.confirm("You cannot add a " + Main.getSimpleClassName(comp.getClass()) + " at this level in the Project.");
    }
        
    private Tree.Item addComponentItem(Tree.Item parent, Object obj) {
        String className = Main.getSimpleClassName(obj.getClass());
        Tree.Item item = new Tree.Item(Main.getTextForObject(obj, idEditor), Main.RES_PATH + className + ".png");
        item.setUserObject(obj);
        objToItem.put(obj, item);
        if (Main.isPrimaryContainer(obj) && ((Container)obj).getChildren().size() > 0) item.getChildren().add(new Tree.Item("Loading..."));
        Object parentObject = parent.getUserObject();
        
        if (obj instanceof Component && Main.isPrimaryContainer(parentObject)) {
            Component comp = (Component)obj; 
            if (comp.getContainer() != parentObject) ((Container)parentObject).getChildren().add((Component)obj);
        }
        
        parent.getChildren().add(item);
        parent.setExpanded(true);
        return item;
    }

    private void loadBranch(Tree.Item item) {
        if (!item.hasChildren() || !item.getChildren().get(0).getImage().equals("")) return;
        Object obj = item.getUserObject();
        
        if (obj instanceof XODFile) {
            item.getChildren().clear();
            XOD xod = new XOD();
            File file = new File(new File(activeProject.getFileName()).getParentFile(), ((XODFile)obj).getFileName());
            if (file.exists()) xod.execute(file.getAbsolutePath());
            
            for (Map.Entry<String, Object> e : xod.getObjectMap().entrySet()) {
                idEditor.setValue(e.getValue(), e.getKey());
                
                if (e.getValue() instanceof RadioButton.Group) {
                    log.info("e.getValue()=" + e.getValue() + ",key=" + idEditor.getValue(e.getValue()));
                }
            }
            
            for (Object o : xod.getRootObjects()) {
                if (o instanceof Container) {
                    addComponent(item, (Component)o);
                }
            }
        } else if (Main.isPrimaryContainer(obj)) {
            item.getChildren().clear();
            Container con = (Container)obj;
            
            for (Object o : con.getChildren()) {
                if (o instanceof DesignTimeComponent) continue;
                addComponent(item, (Component)o);
            }
        }
    }
    
    private void addComponent(Tree.Item parent, Component comp) {
        Object obj = parent.getUserObject();
        
        if (obj instanceof XODFile) {
            if (Main.isPrimaryContainer(comp)) {
                if (comp instanceof Dialog) {
                    Container cont = (Container)comp;
                    String id = idEditor.getValue(comp).toString();
                    List<Component> lst = new ArrayList<Component>(cont.getChildren());
                    cont.getChildren().clear();
                    DialogWindow diag = new DialogWindow();
                    diag.setTitle(((Dialog)comp).getTitle());
                    diag.setRepositionAllowed((((Dialog)comp).isRepositionAllowed()));
                    diag.setResizeAllowed((((Dialog)comp).isResizeAllowed()));
                    diag.setResizeAllowed((((Dialog)comp).isWaitForWindow()));
                    diag.setSize(cont.getWidth(), cont.getHeight() - 20);
                    diag.getChildren().addAll(lst);
                    comp = diag;
                    idEditor.setValue(comp, id);
                }
                
                addComponentItem(parent, comp).setSelected(true);
            } else {
                alertInvalidAdd(comp);
            }
        } else if (obj instanceof TabFolder) {
            if (!(comp instanceof TabSheet)) {
                alertInvalidAdd(comp);
            } else {
                addComponentItem(parent, comp).setSelected(true);
            }
        } else if (comp instanceof RadioButton) {
            if (Main.isPrimaryContainer(obj)) {
                RadioButton.Group group = ((RadioButton)comp).getGroup();
                Tree.Item groupItem = objToItem.get(group);
                parent = groupItem == null ? addComponentItem(parent, group) : groupItem;
                obj = parent.getUserObject();
            }
            
            if (obj instanceof RadioButton.Group) {
                addComponentItem(parent, comp);
            } else {
                alertInvalidAdd(comp);
            }
        } else if (Main.isPrimaryContainer(obj)) {
            Tree.Item item = addComponentItem(parent, comp);
            if (Main.isPrimaryContainer(comp)) item.setSelected(true);
        } else {
            alertInvalidAdd(comp);
        }
    }
    
    private PropertyChangeListener treeSelectListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getNewValue() == Boolean.FALSE) return;
            Tree.Item item = (Tree.Item)ev.getSource();
            Object obj = item.getUserObject();
            if (obj == null) log.info("user object is null for item:" + item.getText());
            bus.fireEvent(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, obj);
        }
    };
    
    private PropertyChangeListener treeExpandListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getNewValue() == Boolean.FALSE) return;
            Tree.Item item = (Tree.Item)ev.getSource();
            loadBranch(item);
        }
    };
    
    private ActionListener treeDoubleClickListener = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            log.info("firing double click!");
            Tree.Item item = (Tree.Item)ev.getSource();
            Object obj = item.getUserObject();
            
            if (Main.isPrimaryContainer(obj)) {
                if (((Tree.Item)item.getParent()).getUserObject() instanceof XODFile) {
                    bus.fireEvent(MessageBus.Id.OPEN_DESIGN_SHEET, obj);
                }
            }
        }
    };
    
    private void setActiveProject(ProjectDefinition project) {
        Tree tree = searchTree;
        Tree.Item root = tree.getRootItem();
        objToItem.clear();
        root.getChildren().clear();
        root.setText(project.getName());
        root.setImage(Main.RES_PATH + "Project.png");
        root.setUserObject(project);
        objToItem.put(project, root);
        
        if (activeProject != null) {
            for (XODFile file : activeProject.getFiles()) {
                Tree.Item item = objToItem.get(file); 
            
                for (Tree.Item child : item.getChildren()) {
                    Object obj = child.getUserObject();
                    if (obj instanceof Container) ProjectSheet.this.bus.fireEvent(MessageBus.Id.CLOSE_DESIGN_SHEET, (Container)obj);
                }
            }
        }
        
        activeProject = project;
        
        for (XODFile file : project.getFiles()) {
            Tree.Item item = new Tree.Item(new File(file.getFileName()).getName(), Main.RES_PATH + "File.png");
            item.setUserObject(file);
            objToItem.put(file, item);
            root.getChildren().add(item);
            item.getChildren().add(new Tree.Item("Loading..."));
        }
        
        bus.fireEvent(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, project);
        //root.getChildren().add(new Tree.Item(project.getName()));
    }

    /*void addComponent(Component comp, String id) {
        Tree.Item item;
        
        if (root == null) {
            item = root = searchTree.getTree().getRootItem();
            setItemInfo(item, comp, id);
        } else {
            item = new Tree.Item();
            setItemInfo(item, comp, id);
            root.getChildren().add(item);
        }
        
        compToItem.put(comp, item);
        fc.setActiveComponent(comp);
    }*/
    
    /*void removeComponent(Component comp) {
        Tree.Item item = compToItem.remove(comp);        
        ((Tree.Item)item.getParent()).getChildren().remove(item);        
    }
    
    void setActiveComponent(Component comp) {
        Tree.Item item = compToItem.get(comp);
        if (item != null) item.setSelected(true);
    }
    
    void setComponentId(Component comp, String id) {
        setItemInfo(compToItem.get(comp), comp, id);
    }*/
}
