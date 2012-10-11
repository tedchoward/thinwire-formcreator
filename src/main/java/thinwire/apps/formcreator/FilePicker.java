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

import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.*;

import javax.swing.filechooser.FileSystemView;

import thinwire.ui.*;
import thinwire.ui.event.*;
import thinwire.ui.layout.*;

/**
 * @author Joshua J. Gertzen
 */
class FilePicker extends thinwire.ui.Dialog {
    private DropDownGridBox lookIn;
    private FolderTree tree;
    private FileGrid grid;
    private DropDownGridBox fileName;
    private DropDownGridBox fileType;
    private File selectedFile;
    private File returnFile;
    private boolean saveDialog;
    
    FilePicker(boolean saveDialog, String rootFolder, Map<String, String> fileTypes) {
        setLayout(new TableLayout(new double[][]{{64 ,0, 22, 22, 22}, {20, 0, 20, 20}}, 4, 2));
        this.saveDialog = saveDialog;
        setTitle(saveDialog ? "Save" : "Open");
        setSize(600, 400);
        setResizeAllowed(true);
        Main.alignCenter(this);

        lookIn = getLookInDropDown("Look in:");
        getChildren().add(lookIn.getLabel());
        getChildren().add(lookIn.setLimit("1, 0, 2, 1"));
        
        Button upFolder = new Button(null, Main.RES_PATH + "GoToParentFolder.png");
        getChildren().add(upFolder.setLimit("3, 0"));

        Button newFolder = new Button(null, Main.RES_PATH + "NewFolder.png");
        getChildren().add(newFolder.setLimit("4, 0"));
        
        Panel browser = new Panel();
        browser.setLayout(new SplitLayout(.30, true));
        File root = new File(rootFolder);
        browser.getChildren().add(tree = new FolderTree(root));
        browser.getChildren().add(grid = new FileGrid());
        getChildren().add(browser.setLimit("0, 1, 5, 1"));
        
        fileName = getFileNameDropDown("File name:");
        getChildren().add(fileName.getLabel().setLimit("0, 2"));
        getChildren().add(fileName.setLimit("1, 2"));
        fileName.setFocus(true);

        fileType = getFileTypeDropDown("Files of type:", fileTypes);
        getChildren().add(fileType.getLabel().setLimit("0, 3"));
        getChildren().add(fileType.setLimit("1, 3"));
        
        getChildren().add(getOkButton().setLimit("2, 2, 3, 1"));

        getChildren().add(getCancelButton().setLimit("2, 3, 3, 1"));

        setSelectedFile(root);
        
        tree.addPropertyChangeListener(Tree.Item.PROPERTY_ITEM_SELECTED, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                Tree.Item item = (Tree.Item)pce.getSource();
                File folder = (File)item.getUserObject();
                setSelectedFile(folder);
            }
        });
        
        grid.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_SELECTED, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                GridBox.Row row = (GridBox.Row)pce.getSource();
                File f = (File)row.get("FILE");
                if (!f.isDirectory()) setSelectedFile(f);
            }
        });
    }
    
    File getSelectedFile() {
        return returnFile;
    }

    private void setSelectedFile(File file) {
        File folder = file.isFile() ? file.getParentFile() : file;
        
        lookIn.setText(folder.getAbsolutePath());
        Pattern pattern = (Pattern)fileType.getComponent().getSelectedRow().get("REGEX");
        grid.setFolder(folder, pattern);
        
        if (file.isFile()) fileName.setText(file.getName());
        selectedFile = file;
    }
    
    private Button getOkButton() {
        Button btn = new Button(saveDialog ? "Save" : "Open");
        
        btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent pce) {
                returnFile = selectedFile;
                FilePicker.this.setVisible(false);
            }
        });
        
        return btn;
    }
    
    private Button getCancelButton() {
        Button btn = new Button("Cancel");
        
        btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent pce) {
                FilePicker.this.setVisible(false);
            }
        });
        
        return btn;
    }
    
    private DropDownGridBox getLookInDropDown(String text) {
        final DropDownGridBox ddgb = new DropDownGridBox();
        GridBox gb = ddgb.getComponent();
        gb.setHeight(225);
        gb.getColumns().add(new GridBox.Column());
        
        ddgb.addPropertyChangeListener(DropDownGridBox.PROPERTY_TEXT, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                File f = new File((String)pce.getNewValue());
                
                if (f.exists() && f.isDirectory()) {
                    
                    try {
                        String name = f.getCanonicalPath();
                        GridBox gb = ddgb.getComponent();
                        List<GridBox.Row> rows = gb.getRows();
                        
                        for (GridBox.Row row : rows) {
                            if (name.equals(row.get(0))) {
                                name = null;
                                break;
                            }
                        }
                        
                        if (name != null) {
                            if (rows.size() > 25) rows.remove(rows.size() - 1);
                            rows.add(0, new GridBox.Row(name));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        
        Label lbl = new Label(text);
        lbl.setLabelFor(ddgb);

        return ddgb;
    }
    
    private DropDownGridBox getFileNameDropDown(String text) {
        final DropDownGridBox ddgb = new DropDownGridBox();
        GridBox gb = ddgb.getComponent();
        gb.setHeight(225);
        gb.getColumns().add(new GridBox.Column());
        
        ddgb.addPropertyChangeListener(DropDownGridBox.PROPERTY_TEXT, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                File f = new File((String)pce.getNewValue());
                
                if (!f.exists()) {
                    f = new File(lookIn.getText(), (String)pce.getNewValue());
                }
                
                if (f.exists() && !f.isDirectory()) {
                    try {
                        String name = f.getCanonicalPath();
                        GridBox gb = ddgb.getComponent();
                        List<GridBox.Row> rows = gb.getRows();
                        
                        for (GridBox.Row row : rows) {
                            if (name.equals(row.get(0))) {
                                name = null;
                                break;
                            }
                        }
                        
                        if (name != null) {
                            if (rows.size() > 25) rows.remove(rows.size() - 1);
                            rows.add(0, new GridBox.Row(name));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        
        Label lbl = new Label(text);
        lbl.setLabelFor(ddgb);

        return ddgb;
    }
    
    private DropDownGridBox getFileTypeDropDown(String text, Map<String, String> fileTypes) {
        final DropDownGridBox ddgb = new DropDownGridBox();
        ddgb.setEditAllowed(false);
        GridBox gb = ddgb.getComponent();
        gb.setHeight(80);
        gb.getColumns().add(new GridBox.Column());
        gb.getColumns().add(new GridBox.Column("REGEX", false, (Object[])null));
        
        if (fileTypes == null) {
            fileTypes = new HashMap<String, String>();
            fileTypes.put("*.*", "All Files");
        }
        
        for (Map.Entry<String, String> e : fileTypes.entrySet()) {
            String pattern = e.getKey();
            String regex = pattern.replaceAll("[.]", "[.]").replaceAll("[*]", ".*?");
            GridBox.Row row = new GridBox.Row(e.getValue() + " (" + pattern + ")", Pattern.compile(regex));
            gb.getRows().add(row);
        }
        
        //TODO This shouldn't really be necessary, the text should be updated when the first
        //row is selected.
        ddgb.setText((String)gb.getRows().get(0).get(0));
        
        ddgb.getComponent().addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_SELECTED, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setSelectedFile(selectedFile);
            }
        });
        
        Label lbl = new Label(text);
        lbl.setLabelFor(ddgb);
        return ddgb;
    }

    private static class FolderTree extends Tree {
        FolderTree(File rootFolder) {
            setRootItemVisible(true);
            
            addPropertyChangeListener(Tree.Item.PROPERTY_ITEM_EXPANDED, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    if (pce.getNewValue() == Boolean.TRUE) {
                        Tree.Item item = (Tree.Item)pce.getSource();
                        expandItem(item);
                    }
                }
            });
            
            if (rootFolder != null) setRootFolder(rootFolder);
        }
        
        void setRootFolder(File folder) {
            if (!folder.isDirectory()) throw new IllegalArgumentException("!folder.isDirectory()");
            prepareItem(getRootItem(), folder);
            expandItem(getRootItem());
            getRootItem().setExpanded(true);
        }
        
        private Tree.Item prepareItem(Tree.Item item, File folder) {
            if (item == null) {
                item = new Tree.Item();
            } else {
                item.getChildren().clear();
            }
            
            item.getChildren().add(new Tree.Item("Loading..."));
            String text = folder.getName();
            if (text.trim().equals("")) text = folder.getAbsolutePath();
            item.setText(text);
            item.setImage(Main.RES_PATH + "Folder.png");
            item.setExpanded(false);
            item.setUserObject(folder);
            return item;
        }
        
        private void expandItem(Tree.Item item) {
            if (item.hasChildren()) {
                List<Tree.Item> kids = item.getChildren();

                if (kids.get(0).getImage().equals("")) {
                    kids.clear();
                    File file = (File)item.getUserObject();
                    
                    for (File f : file.listFiles()) {
                        if (f.isDirectory() && !f.isHidden()) kids.add(prepareItem(null, f));
                    }
                }
            }
        }
    }
    
    private static class FileGrid extends GridBox {
        private FileSystemView fsv = FileSystemView.getFileSystemView();
        private NumberFormat sizeFormat = NumberFormat.getIntegerInstance();
        private DateFormat dateFormat = DateFormat.getDateInstance();
        private File currentFolder;
        private Pattern currentPattern;
        
        FileGrid() {
            List<Column> cols = getColumns();
            cols.add(new Column("Name", true, 250, (Object[])null));
            cols.add(new Column("Size", true, 50, AlignTextComponent.AlignX.RIGHT, (Object[])null));
            cols.add(new Column("Type", true, 100, (Object[])null));
            cols.add(new Column("Date modified", true, 70, (Object[])null));
            cols.add(new Column("FILE", false, (Object[])null));
            setVisibleHeader(true);
        }
        
        void setFolder(File folder, Pattern pattern) {
            if (!folder.isDirectory()) throw new IllegalArgumentException("!folder.isDirectory()");
            if (folder.equals(currentFolder) && pattern.equals(currentPattern)) return;
            currentFolder = folder;
            currentPattern = pattern;
            getRows().clear();
            File[] content = fsv.getFiles(folder, false);
            
            for (File f : content) {
                if (f.isDirectory() && !f.isHidden()) addRow(f);
            }

            for (File f : content) {
                if (!f.isDirectory() && !f.isHidden() && pattern.matcher(f.getName()).matches()) addRow(f);
            }
            
        }
        
        private void addRow(File file) {
            Row row = new Row();
            row.add("<img src='" + Main.RES_PATH + (file.isDirectory() ? "Folder" : "File") + ".png'/>  " + fsv.getSystemDisplayName(file));           
            row.add(sizeFormat.format(file.length() / 1024) + "KB");
            row.add(fsv.getSystemTypeDescription(file));
            row.add(dateFormat.format(new Date(file.lastModified())));
            row.add(file);
            getRows().add(row);
        }
    }
}
