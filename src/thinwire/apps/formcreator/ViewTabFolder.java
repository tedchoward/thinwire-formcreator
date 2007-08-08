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
import thinwire.ui.event.*;
import thinwire.ui.layout.*;

/**
 * @author Joshua J. Gertzen
 */
class ViewTabFolder extends TabFolder {
    private ProjectDefinition project;
    private Map<Container, DesignSheet> contToSheet = new HashMap<Container, DesignSheet>();
    
    public ViewTabFolder(final MessageBus bus) {
        getChildren().add(getWelcomeSheet());
        
        bus.addListener(MessageBus.Id.OPEN_DESIGN_SHEET, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                Container cont = (Container)ev.getData();
                DesignSheet ds = contToSheet.get(cont);
                
                if (ds == null) {
                    ds = new DesignSheet(bus, project, cont);
                    getChildren().add(ds);
                    contToSheet.put(cont, ds);
                    setCurrentIndex(getChildren().size() - 1);
                } else {
                    setCurrentIndex(getChildren().indexOf(ds));
                }
            }
        });

        bus.addListener(MessageBus.Id.CLOSE_DESIGN_SHEET, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                Container cont = (Container)ev.getData();
                DesignSheet ds = contToSheet.remove(cont);
                System.out.println("Closing design sheet: ds=" + ds + ",cont=" + cont);
                if (ds != null) {
                    ds.destroy();
                    getChildren().remove(ds);
                }
            }
        });
        
        bus.addListener(MessageBus.Id.SET_ACTIVE_PROJECT, new MessageBus.Listener() {
            public void eventOccured(MessageBus.Event ev) {
                project = (ProjectDefinition)ev.getData();
            }
        });
    }
    
    private TabSheet getWelcomeSheet() {
        TabSheet ts = new TabSheet("Welcome");
        ts.setLayout(new TableLayout(new double[][]{{0},{0}}, 2));
        ts.getChildren().add(new WebBrowser());//("http://www.thinwire.com/blog"));
        return ts;
    }
}
