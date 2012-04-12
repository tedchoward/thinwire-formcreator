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
var tw_ComponentDesigner = Class.extend({
    _opacity: 35,
    _comp: null,
    _designer: null,
    _drag: null,
    
    construct: function(designerId, compId) {
        var designer = this._designer = tw_Component.instances[designerId];
        var comp = this._comp = tw_Component.instances[compId];
        designer._box.style.cursor = "move";
        designer._box.style.zIndex = "2";
        designer.setOpacity(1);
        designer.compDesign = this;
        
        designer._box.appendChild(this._createSizerButton(0, 0));
        designer._box.appendChild(this._createSizerButton(0, 1));
        designer._box.appendChild(this._createSizerButton(0, 2));
        designer._box.appendChild(this._createSizerButton(1, 0));
        designer._box.appendChild(this._createSizerButton(1, 2));
        designer._box.appendChild(this._createSizerButton(2, 0));
        designer._box.appendChild(this._createSizerButton(2, 1));
        designer._box.appendChild(this._createSizerButton(2, 2));

        this._updateBounds = this._updateBounds.bind(this);        
        this._drag = new tw_DragHandler(this._designer._box, this._dragListener.bind(this));
        this._setActive(true);
    },
        
    _setActive: function(active) {
        if (active) {
            if (tw_ComponentDesigner.active === this) return;
            if (tw_ComponentDesigner.active != null) tw_ComponentDesigner.active._setActive(false);
            this._comp._box.style.zIndex = "1";
            this._designer._box.style.zIndex = "2";
            this._designer.setOpacity(this._opacity);
            active = "block";                           
            tw_ComponentDesigner.active = this;            
        } else {
            this._comp._box.style.zIndex = "0";
            this._designer._box.style.zIndex = "1";
            this._designer.setOpacity(1);
            active = "none";
        }
                
        this._designer._box.style.display = "none";
        
        for (var i = this._designer._box.childNodes.length; --i >= 1;) {
            var button = this._designer._box.childNodes.item(i); 
            button.style.display = active;
        }

        this._designer._box.style.display = "block";
    },
        
    _createSizerButton: function(row, column) {
        var button = document.createElement("div");
        var s = button.style;
        s.position = "absolute";
        s.border = "1px solid black";
        s.width = "5px";
        s.height = "5px";
        s.display = "none";
        s.overflow = "hidden";
        s.backgroundColor = "lightgrey";
        
        var cursor;
        
        if (row == 0) {            
            cursor = "N";
            button.style.top = "0px";
        } else if (row == 1) {
            cursor = "";
            button.style.top = "48%";
        } else {
            cursor = "S";
            button.style.bottom = "0px";
        }
    
        if (column == 0) {
            cursor += "W";
            button.style.left = "0px";
        } else if (column == 1) {
            button.style.left = "49%";
        } else {
            cursor += "E";
            button.style.right = "0px";
        }
        
        cursor += "-resize";
        s.cursor = cursor;
        return button;
    },
    
    _dragListener: function(ev) {
        if (ev.type == 0) {
            this._setActive(true);
        } else if (ev.type == 1) {
            var s = this._designer._box.style;
        
            if (ev.box === this._designer._box) {        
                var x = this._comp._x;
                var y = this._comp._y;            
                x += ev.changeInX;
                y += ev.changeInY;
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                this._comp.setX(x);
                this._comp.setY(y);
                s.left = x + "px";
                s.top = y + "px";
            } else {
                var cursor = ev.box.style.cursor;
                var dir1 = cursor.charAt(0).toUpperCase();
                var dir2 = cursor.charAt(1).toUpperCase();
                var s = this._designer._box.style;
                s.display = "none";
                
                if (dir1 == "N" || dir2 == "N") {
                    var height = this._comp._height - ev.changeInY;
                    
                    if (height >= 10) {
                        var y = this._comp._y + ev.changeInY;                        
                        s.top = y + "px";
                        s.height = height + "px";                        
                        this._comp.setY(y);
                        this._comp.setHeight(height);
                        this._designer.setY(y);
                        this._designer.setHeight(height);
                    }
                } else if (dir1 == "S" || dir2 == "S") {
                    var height = this._comp._height + ev.changeInY;
                    
                    if (height >= 10) {                
                        s.height = height + "px";
                        this._comp.setHeight(height);
                        this._designer.setHeight(height);
                    }
                }
                
                if (dir1 == "W" || dir2 == "W") {
                    var width = this._comp._width - ev.changeInX;
                    
                    if (width >= 10) {
                        var x = this._comp._x + ev.changeInX;
                        s.left = x + "px";
                        s.width = width + "px";
                        this._comp.setX(x);
                        this._comp.setWidth(width);
                        this._designer.setX(x);
                        this._designer.setWidth(width);
                    }
                } else if (dir1 == "E" || dir2 == "E") {
                    var width = this._comp._width + ev.changeInX;
                    
                    if (width >= 10) {
                        s.width = width + "px";
                        this._comp.setWidth(width);
                        this._designer.setWidth(width);
                    }
                }
                
                s.display = "block";
            }
        } else if (ev.type == 2) {
            setTimeout(this._updateBounds, 100);
        }
    },
    
    _updateBounds: function() {
        var msg = this._comp._x + "," + this._comp._y + "," + this._comp._width + "," + this._comp._height;
        tw_em.sendViewStateChanged(this._comp._id, "bounds", msg);
        tw_em.sendViewStateChanged(this._designer._id, "bounds", msg);
    },
    
    destroy: function() {
        this._drag.setBox(null);
        delete this._designer.compDesign;
        this._comp = this._designer = null;
        if (tw_ComponentDesigner.active === this) tw_ComponentDesigner.active = null; 
    }
});

tw_ComponentDesigner.active = null;

tw_ComponentDesigner.create = function(designerId, compId) {
    new tw_ComponentDesigner(designerId, compId);
};

tw_ComponentDesigner.destroy = function(designerId) {
    var designer = tw_Component.instances[designerId];
    designer.compDesign.destroy();
};

tw_ComponentDesigner.setActive = function(designerId) {
    var designer = tw_Component.instances[designerId];
    designer.compDesign._setActive(true);
};