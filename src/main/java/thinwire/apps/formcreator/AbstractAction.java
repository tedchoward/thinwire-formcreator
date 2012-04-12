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

import thinwire.ui.Menu;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractAction {
    private String text;
    private String image;
    private String keyPressCombo;
    
    AbstractAction(String text) {
        this(text, null, null);
    }
    
    AbstractAction(String text, String image) {
        this(text, image, null);
    }
    
    AbstractAction(String text, String image, String keyPressCombo) {
        if (text == null) throw new IllegalArgumentException("text == null");
        if (image == null) image = "";
        if (keyPressCombo == null) keyPressCombo = "";
        this.text = text;
        this.image = image;
        this.keyPressCombo = keyPressCombo;
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }

    public String getKeyPressCombo() {
        return keyPressCombo;
    }
    
    public KeyPressListener asKeyPressListener() {
        return new KeyPressListener() {
            public void keyPress(KeyPressEvent ev) {
                run(null);
            }
        };
    }
    
    public Menu.Item asMenuItem() {
        Menu.Item item = new Menu.Item();
        item.setText(getText());
        item.setImage(getImage());
        item.setKeyPressCombo(getKeyPressCombo());
        item.setUserObject(this);
        return item;
    }
    
    public abstract void run(Object obj);
}
