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

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Panel;
import thinwire.ui.TabSheet;
import thinwire.ui.layout.TableLayout;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractSheet extends Panel {
    private Container<Component> outerPanel = this;
    private String text;
    private String image;
    
    AbstractSheet(String text) {
        this(text, null);
    }
    
    AbstractSheet(String text, String image) {
        if (text == null) throw new IllegalArgumentException("text == null");
        if (image == null) image = "";
        this.text = text;
        this.image = image;
        setScrollType(ScrollType.AS_NEEDED);
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }
    
    public TabSheet asTabSheet() {
        if (outerPanel == this) {
            if (this.getParent() != null) throw new IllegalStateException("Sheet '" + getText() + "' already returned attached");
        } else {
            throw new IllegalStateException("Sheet '" + getText() + "' already returned as " + outerPanel.getClass());
        }
        
        TabSheet ts = new TabSheet(getText(), Main.RES_PATH + getImage());
        ts.setLayout(new TableLayout(new double[][]{{0},{0}}));
        outerPanel = ts;
        ts.getChildren().add(this);
        return ts;
    }
}
