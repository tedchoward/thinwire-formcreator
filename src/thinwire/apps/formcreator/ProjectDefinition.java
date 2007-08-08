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

/**
 * @author Joshua J. Gertzen
 */
public class ProjectDefinition {
    private String name;
    private String fileName = "";
    private List<XODFile> files = new ArrayList<XODFile>(); 

    public ProjectDefinition() {
        this(null);
    }
    
    public ProjectDefinition(String name) {
        setName(name);
    }
    
    public void initFileName(String fileName) {
        if (!this.fileName.equals("")) throw new IllegalStateException("this.fileName{" + this.fileName + "} != null");
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) name = "";
        this.name = name;
    }
    
    public List<XODFile> getFiles() {
        return files;
    }
}
