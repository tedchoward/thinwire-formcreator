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

import thinwire.ui.*;
import thinwire.ui.style.*;

/**
 * @author Joshua J. Gertzen
 */
public class DialogWindow extends Panel {
    private String title = "";
    private boolean resizeAllowed;
    private boolean repositionAllowed;
    private boolean waitForWindow;

    public DialogWindow() {
        getStyle().getBorder().setType(Border.Type.OUTSET);
        getStyle().getBorder().setSize(2);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) title = "";
        this.title = title;
    }

    public boolean isRepositionAllowed() {
        return repositionAllowed;
    }

    public void setRepositionAllowed(boolean repositionAllowed) {
        this.repositionAllowed = repositionAllowed;
    }

    public boolean isResizeAllowed() {
        return resizeAllowed;
    }

    public void setResizeAllowed(boolean resizeAllowed) {
        this.resizeAllowed = resizeAllowed;
    }

    public boolean isWaitForWindow() {
        return waitForWindow;
    }

    public void setWaitForWindow(boolean waitForWindow) {
        this.waitForWindow = waitForWindow;
    }
}
