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
import thinwire.util.*;
import thinwire.ui.*;
import thinwire.ui.event.*;
import thinwire.ui.layout.*;
import thinwire.ui.style.*;
import static thinwire.ui.layout.TableLayout.*;

/**
 * @author Joshua J. Gertzen
 */
class Toolbar extends Panel {
    private TableLayout layout;
    private boolean showText;
    
    Toolbar() {
        this(false);
    }
    
    Toolbar(boolean showText) {
        layout = new TableLayout(new double[][]{{0},{0}}, 0, 4);
        this.showText = showText; 
        setLayout(layout);
    }
    
    public void add(AbstractAction action) {
        Button btn = new Button(showText ? action.getText() : null, action.getImage());
        btn.setUserObject(action);
        int width = 20;
        if (showText) width += action.getText().length() * 6;
        add(btn, width);
    }
    
    public void addDivider() {
        add(new Divider(), 6);
    }
    
    private void add(Component comp, int width) {
        comp.addActionListener(Component.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                ((AbstractAction)ev.getSourceComponent().getUserObject()).run(null);
            }
        });
        
        comp.getStyle().getBorder().setSize(1);
        int position = layout.getColumns().size() - 1;
        Column col = new Column(width);
        layout.getColumns().add(position, col);
        col.set(0, comp);
    }
}
