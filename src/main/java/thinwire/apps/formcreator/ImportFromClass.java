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

import thinwire.ui.*;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.layout.*;
import thinwire.ui.layout.TableLayout.Row;
import thinwire.ui.style.*;

/**
 * @author Joshua J. Gertzen
 */
class ImportFromClass extends Dialog {
    private MessageBus bus;
    private TextField className;
    private EditorComponent containerType;
    private EditorComponent containerWidth;
    private EditorComponent containerHeight;
    private EditorComponent containerScrollType;
    private EditorComponent labelWidth;
    private EditorComponent spacing;
    private int index;
    
    ImportFromClass(MessageBus bus) {
        super("Create Container From Class");
        this.bus = bus;
        index = 0;
        setLayout(new TableLayout(new double[][]{{0, 70, 70},{0, 22}}, 4));
        getChildren().add(createFirstPage().setLimit("0, 0, 3, 1"));
        
        //MUST DO NOW
        //Add: Spacing & Margin settings
        //Add: Auto - mask / max length guessing
        
        //ITEMS TO ADD LATER
        //Add: Uniform field sizing vs. Jagged field sizing
        //Add: TopDown & LeftToRight vs. LeftToRight & TopDown
        //Add: id format generation, either '_' based or camelcased.
        //Add: Label alignment left / right - default to right
        //Add: Number of columns - set to 2 for now
        //Add: RadioButton control support

        //SECOND PAGE:
        //Add: for dropdown's select visible columns, set userObject with list
        //Add: add or remove new property items

        getChildren().add(getCancelButton().setLimit("1, 1"));
        getChildren().add(getOKButton().setLimit("2, 1"));
        setSize(300, 175);
        Main.alignCenter(this);
    }
    
    private Button getCancelButton() {
        Button btn = new Button("Cancel");
        btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                ImportFromClass.this.setVisible(false);
            }
        });
        
        return btn;
    }
    
    private Button getOKButton() {
        Button btn = new Button("OK");
        
        btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
            	if (index == 0) {
            		try {
	                    Class clazz = Class.forName(className.getText());
	                    className.getStyle().getBackground().setColor(null);
	                    setSize(800, 600);
	                    Main.alignCenter(ImportFromClass.this);
	                    ((TableLayout) getLayout()).getRows().get(0).set(0, createSecondPage(clazz).setLimit("0, 0, 3, 1"));
	                    index++;
	                } catch (ClassNotFoundException e) {
	                    className.getStyle().getBackground().setColor(Color.LIGHTSALMON);
	                }
            	} else {
            		
            		TableLayout tl = (TableLayout) ((Panel) ((TableLayout) getLayout()).getRows().get(0).get(0)).getLayout();
            		tl = (TableLayout) ((Panel) tl.getRows().get(0).get(0)).getLayout();
            		List<TableLayout.Row> rows = tl.getRows();
            		List<TextComponent> sequenceCol = new ArrayList(tl.getColumns().get(0));
            		Collections.sort(sequenceCol, new Comparator<TextComponent>() {
						public int compare(TextComponent arg0, TextComponent arg1) {
							if (arg0 instanceof Label) return -1;
							if (arg0 instanceof Label) return 1;
							int index1 = Integer.parseInt(arg0.getText());
							int index2 = Integer.parseInt(arg1.getText());
							if (index1 < index2) {
								return -1;
							} else if (index1 == index2) {
								return 0;
							} else {
								return 1;
							}
						}
            		});
            		int arraySize = Integer.parseInt(sequenceCol.get(sequenceCol.size() - 1).getText());
            		Component[] components = new Component[arraySize + 1];
            		Label[] labels = new Label[arraySize + 1];
            		try {
            			for (int i = 1, cnt = rows.size(); i < cnt; i++) {
            				TableLayout.Row row = rows.get(i);
            				int sequence = Integer.parseInt(((EditorComponent) row.get(0)).getText());
            				String fieldID = ((EditorComponent) row.get(2)).getText();
            				String typeName = ((EditorComponent) row.get(3)).getText();
            				String labelText = ((EditorComponent) row.get(4)).getText();
            				String editMask = ((EditorComponent) row.get(5)).getText();
            				String userObject = ((EditorComponent) row.get(6)).getText();
							MaskEditorComponent c = (MaskEditorComponent) Class.forName("thinwire.ui." + typeName).newInstance();
							Main.getIdPropertyEditor().setValue(c, fieldID);
							
							if (editMask.length() > 0) {
								if ("9#MdyAaXxhmp".indexOf(editMask.charAt(0)) >= 0) {
									c.setEditMask(editMask);
								} else {
									try {
										c.setMaxLength(Integer.parseInt(editMask));
									} catch (NumberFormatException e) {
									}
								}
							}
							
							c.setUserObject(userObject);
							
							Label l = new Label(labelText);
							l.setLabelFor(c);
							l.setAlignX(Label.AlignX.RIGHT);
							labels[sequence] = l;
							components[sequence] = c;
            			}
            			Container cont = createContainer(components, labels);
            			ImportFromClass.this.setVisible(false);
	                    bus.fireEvent(MessageBus.Id.ADD_PROJECT_OBJECT, cont);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
            	}
            }
        });
        
        return btn;
    }
    
    private TextField getClassNameField() {
        TextField fld = new TextField();
        Label lbl = getRightLabel("Class name:");
        lbl.setLabelFor(fld);
        return fld;
    }
    
    private DropDownGridBox getContainerTypeField() {
    	DropDownGridBox ddgb = new DropDownGridBox();
    	ddgb.setEditAllowed(false);
    	List<GridBox.Column> cols = ddgb.getComponent().getColumns();
    	cols.add(new GridBox.Column("Dialog", "Panel"));
    	cols.add(new GridBox.Column(Dialog.class, Panel.class));
    	cols.get(1).setVisible(false);
    	getRightLabel("Container Type:").setLabelFor(ddgb);
    	return ddgb;
    }
    
    private Container createContainer(Component[] components, Label[] labels) {
    	Container cont = null;
    	try {
			cont = ((Class<Container>) ((DropDownGridBox) containerType).getComponent().getSelectedRow().get(1)).newInstance();
			cont.setSize(Integer.parseInt(containerWidth.getText()), Integer.parseInt(containerHeight.getText()));
			cont.setScrollType(ScrollType.valueOf(containerScrollType.getText().toUpperCase()));
	        TableLayout layout = new TableLayout();
	        layout.setSpacing(Integer.parseInt(spacing.getText()));
	        layout.setMargin(5);
	        cont.setLayout(layout);
	        int lblWidth = Integer.parseInt(labelWidth.getText());
	        layout.getColumns().add(new TableLayout.Column(lblWidth));
	        layout.getColumns().add(new TableLayout.Column(0));
	        layout.getColumns().add(new TableLayout.Column(lblWidth));
	        layout.getColumns().add(new TableLayout.Column(0));
	        int column = 0;
	        TableLayout.Row row = null;
	        
	        List<Component> kids = cont.getChildren();
	        
	        for (int i = 0, cnt = components.length; i < cnt; i++) {
	            if (row == null) layout.getRows().add(row = new TableLayout.Row(22));
	            Label lbl = labels[i];
	            int idx = row.getIndex();
	            if (lbl != null) lbl.setLimit(column + ", " + row.getIndex());
	            column++;
	            EditorComponent comp = (EditorComponent) components[i];
	            if (comp != null) {
	            	comp.setLimit(column + ", " + row.getIndex());
	            	kids.add(comp);
		            kids.add(lbl);
	            }
	            column++;
	            
	            if (column == 4) {
	                column = 0;
	                row = null;
	            }
	        }
	        
	        //if (row != null) layout.getRows().add(row);
	        cont.setLayout(null);
    	} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
        return cont;
    }
    
    private Panel createFirstPage() {
    	Panel p = new Panel();
    	p.setLayout(new TableLayout(new double[][]{{80, 25, 50, 25, 0},{22, 22, 22, 22, 22, 0}}, 0, 2));
        className = getClassNameField();
        
        List<Component> kids = p.getChildren();
        
        kids.add(className.getLabel());
        kids.add(className.setLimit("1, 0, 4, 1"));
        containerType = getContainerTypeField();
        kids.add(containerType.getLabel().setLimit("0, 1"));
        kids.add(containerType.setLimit("1, 1, 4, 1"));
        kids.add(getRightLabel("Width:").setLimit("0, 2"));
        kids.add(containerWidth = (EditorComponent) new ComponentPropertyEditor(Container.class, Container.PROPERTY_WIDTH, int.class).newEditor().setLimit("1, 2"));
        kids.add(getRightLabel("Height:").setLimit("2, 2"));
        kids.add(containerHeight = (EditorComponent) new ComponentPropertyEditor(Container.class, Container.PROPERTY_HEIGHT, int.class).newEditor().setLimit("3, 2"));
        kids.add(getRightLabel("Scroll Type:").setLimit("0, 3"));
        kids.add(containerScrollType = (EditorComponent) new ComponentPropertyEditor(Container.class, Container.PROPERTY_SCROLL_TYPE, Container.ScrollType.class).newEditor().setLimit("1, 3, 4, 1"));
        kids.add(getRightLabel("Label Width:").setLimit("0, 4"));
        kids.add(labelWidth = (EditorComponent) new ComponentPropertyEditor(Label.class, Label.PROPERTY_WIDTH, int.class).newEditor().setLimit("1, 4"));
        kids.add(getRightLabel("Spacing:").setLimit("2, 4"));
        kids.add(spacing = (EditorComponent) new ComponentPropertyEditor(TableLayout.class, "spacing", int.class).newEditor().setLimit("3, 4"));
        
        containerType.setText("Panel");
        containerWidth.setText("600");
        containerHeight.setText("400");
        containerScrollType.setText("none");
        labelWidth.setText("100");
        spacing.setText("2");
        
    	return p;
    }
    
    private Panel createSecondPage(Class clazz) {
    	Panel p = new Panel();
    	p.setLayout(new TableLayout(new double[][] {{0, 20, 20}, {0, 20}}));
    	Panel gridPanel = new Panel();
    	gridPanel.getStyle().getBackground().setColor(Color.LIGHTGRAY);
    	gridPanel.getStyle().getBorder().setColor(Color.THREEDFACE);
    	gridPanel.getStyle().getBorder().setSize(2);
    	gridPanel.getStyle().getBorder().setType(Border.Type.INSET);
    	gridPanel.setScrollType(Panel.ScrollType.AS_NEEDED);
    	final TableLayout tl = new TableLayout(new double[][] {{0, 0, 0, 0, 0, 0, 0}, {0}}, 1, 1);
    	List<TableLayout.Row> rows = tl.getRows();
    	gridPanel.setLayout(tl);
    	Reflector<Object> reflector = new Reflector<Object>("General", clazz);
    	int idx = 0;
    	TableLayout.Row headerRow = new TableLayout.Row(20);
    	headerRow.add(getColumnHeader("Sequence"));
    	headerRow.add(getColumnHeader("Data Type"));
    	headerRow.add(getColumnHeader("Field ID"));
    	headerRow.add(getColumnHeader("Field Type"));
    	headerRow.add(getColumnHeader("Label Text"));
    	headerRow.add(getColumnHeader("EditMask / MaxLength"));
    	headerRow.add(getColumnHeader("Valid Columns"));
    	rows.set(0, headerRow);
    	for (final Reflector.Property prop : reflector.getProperties()) {
    		final TableLayout.Row row = new TableLayout.Row(20);
    		row.add(getCellTextField(String.valueOf(idx)));
    		row.add(getCellLabel(prop.getType().getSimpleName()));
    		EditorComponent id = Main.getIdPropertyEditor().newEditor();
    		id.getStyle().getBorder().setSize(0);
    		id.setText(prop.getName());
    		row.add(id);
    		EditorComponent ec;
    		row.add(ec = (EditorComponent) getFieldTypeCell(""));
    		ec.addPropertyChangeListener(EditorComponent.PROPERTY_TEXT, new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent ev) {
					if (ev.getNewValue().equals("DropDownGridBox")) {
						if (prop.isComplexType()) {
							Reflector<Object> propReflect = new Reflector<Object>("General", prop.getType());
							DropDownGridBox ddgb = new DropDownGridBox();
							ddgb.getStyle().getBorder().setSize(0);
							Collection<Reflector.Property<Object>> props = propReflect.getProperties();
							List<GridBox.Row> rows = ddgb.getComponent().getRows();
							for (Reflector.Property p : props) rows.add(new GridBox.Row(p.getName()));
							ddgb.getComponent().setVisibleCheckBoxes(true);
							row.set(6, ddgb);
						} else {
							((EditorComponent) row.get(6)).setEnabled(true);
						}
					} else {
						row.set(6, getCellLabel(""));
					}
				}
    		});
    		row.add(getCellTextField(getLabelText(prop.getName())));
    		row.add(getCellTextField(prop.newEditor().getEditMask()));
    		row.add(getCellLabel(""));
			rows.add(row);
			ec.setText(prop.newEditor().getClass().getSimpleName());
			idx++;
    	}
    	
    	p.getChildren().add(gridPanel.setLimit("0, 0, 3, 1"));
    	/*
    	Button b = new Button("+");
    	b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				TableLayout.Row row = new TableLayout.Row(20);
	    		row.add(getCellTextField(""));
	    		row.add(getCellLabel(""));
	    		EditorComponent id = Main.getIdPropertyEditor().newEditor();
	    		id.getStyle().getBorder().setSize(0);
	    		id.setText("");
	    		row.add(id);
	    		row.add(getFieldTypeCell(""));
	    		row.add(getCellTextField(""));
	    		row.add(getCellTextField(""));
				tl.getRows().add(new TableLayout.Row(20));
			}
    	});
    	p.getChildren().add(b.setLimit("1, 1"));
    	
    	b = new Button("-");
    	b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				tl.getRows().remove(tl.getRows().size() - 1);
			}
    	});
    	p.getChildren().add(b.setLimit("2, 1"));
    	*/
    	return p;
    }
    
    private DropDownGridBox getFieldTypeCell(String text) {
    	DropDownGridBox ddgb = new DropDownGridBox(text);
    	ddgb.getComponent().getColumns().add(new GridBox.Column("DropDownDateBox", "DropDownGridBox", "TextField"));
    	ddgb.setEditAllowed(false);
    	ddgb.getStyle().getBorder().setSize(0);
    	return ddgb;
    }
    
    private Label getColumnHeader(String text) {
    	Label l = new Label(text);
    	l.setAlignX(Label.AlignX.CENTER);
    	l.getStyle().getBackground().setColor(Color.THREEDFACE);
    	l.getStyle().getBorder().setType(Border.Type.OUTSET);
    	l.getStyle().getBorder().setColor(Color.THREEDFACE);
    	l.getStyle().getBorder().setSize(2);
    	return l;
    }
    
    private TextField getCellLabel(String text) {
    	TextField tf = new TextField(text);
    	tf.setEnabled(false);
    	tf.getStyle().getBorder().setSize(0);
    	return tf;
    }
    
    private TextField getCellTextField(String text) {
    	TextField tf = new TextField(text);
    	tf.getStyle().getBorder().setSize(0);
    	return tf;
    }
    
    private Label getRightLabel(String text) {
    	Label l = new Label(text);
    	l.setAlignX(Label.AlignX.RIGHT);
    	return l;
    }
    
    private String getLabelText(String propName) {
    	String s = propName.replaceAll("(.*?)(\\p{Upper})", "$1 $2");
    	s = s.length() > 1 ? String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1) : String.valueOf(s.charAt(0)).toUpperCase();
    	return s + ":";
    }
}
