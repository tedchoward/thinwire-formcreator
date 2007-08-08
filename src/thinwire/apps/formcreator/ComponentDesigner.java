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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import thinwire.apps.formcreator.MessageBus.Event;
import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.Application;
import thinwire.ui.Component;
import thinwire.ui.Label;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;

/**
 * @author Joshua J. Gertzen
 */
public class ComponentDesigner extends Label implements DesignTimeComponent {
    /*private static Application.Local<Map<Component, ComponentDesigner>> compToDesigner = new Application.Local<Map<Component, ComponentDesigner>>() {
        public Map<Component, ComponentDesigner> initialValue() {
            return new HashMap<Component, ComponentDesigner>(5);
        }
    };*/
    
    /*static ComponentDesigner getInstance(Component comp) {
        return compToDesigner.get().get(comp);
    }*/
    
    //private static final PropertyEditor xEditor;
    //private static final PropertyEditor yEditor;
    
    //static {
      //  PropertyEditorFactory.getPropertyEditors(Class.class)
    //}
    
    static final String CLIENT_SIDE_LIB = Main.RES_PATH + "ComponentDesigner.js";
    
    private WebApplication app = (WebApplication)WebApplication.current();
    private Component comp;
    private MessageBus bus;
    
    private PropertyChangeListener compBoundsListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
            ComponentDesigner.this.setBounds(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight());
            bus.fireEvent(MessageBus.Id.SET_PROPERTY_SHEET_VALUE, new Object[]{Component.PROPERTY_X, comp.getX()});
            bus.fireEvent(MessageBus.Id.SET_PROPERTY_SHEET_VALUE, new Object[]{Component.PROPERTY_Y, comp.getY()});
            bus.fireEvent(MessageBus.Id.SET_PROPERTY_SHEET_VALUE, new Object[]{Component.PROPERTY_WIDTH, comp.getWidth()});
            bus.fireEvent(MessageBus.Id.SET_PROPERTY_SHEET_VALUE, new Object[]{Component.PROPERTY_HEIGHT, comp.getHeight()});
        }
    }; 
    
    private RenderStateListener compRenderListener = new RenderStateListener() {
        public void renderStateChange(RenderStateEvent ev) {
            comp.getContainer().getChildren().add(ComponentDesigner.this);
            app.removeRenderStateListener(comp, this);
        }
    };
    
    private RenderStateListener renderListener = new RenderStateListener() {
        public void renderStateChange(RenderStateEvent ev) {
            Integer designerId = app.getComponentId(ComponentDesigner.this);
            Integer compId = app.getComponentId(comp);
            app.clientSideMethodCall("tw_ComponentDesigner", "create", designerId, compId);
            getStyle().getBackground().setColor(Color.WINDOW);
            getStyle().getBorder().setType(Border.Type.DASHED);
        }
    };
    
    private MessageBus.Listener activeListener = new MessageBus.Listener() {
        public void eventOccured(Event ev) {
            if (ev.getData() == comp) setActive();
        }
    };
    
    public ComponentDesigner(MessageBus bus, final Component comp) {
        this.bus = bus;
        this.comp = comp;
        this.setBounds(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight());
        getStyle().getBorder().setSize(1);
        getStyle().getBorder().setColor(Color.BLACK);
        getStyle().getBorder().setType(Border.Type.NONE);

        addActionListener(ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                ComponentDesigner.this.bus.fireEvent(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, comp);
            }
        });
        
        bus.addListener(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, activeListener);
        
        comp.addPropertyChangeListener(Main.BOUNDS_ARY, compBoundsListener);
        
        app.addRenderStateListener(this, renderListener);
        app.addRenderStateListener(comp, compRenderListener);
     
        this.setUserObject(comp);
    }
        
    void destroy() {
        bus.removeListener(MessageBus.Id.SET_ACTIVE_PROPERTY_OBJECT, activeListener);
        app.removeRenderStateListener(this, renderListener);
        Integer designerId = app.getComponentId(ComponentDesigner.this);
        app.clientSideMethodCall("tw_ComponentDesigner", "destroy", designerId);
        this.getContainer().getChildren().remove(this);
        comp.removePropertyChangeListener(compBoundsListener);
        comp = null;
    }
    
    void setActive() {
        Integer designerId = app.getComponentId(ComponentDesigner.this);
        app.clientSideMethodCall("tw_ComponentDesigner", "setActive", designerId);
    }
}
