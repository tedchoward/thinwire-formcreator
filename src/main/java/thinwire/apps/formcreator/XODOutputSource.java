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

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import org.xml.sax.InputSource;

/**
 * @author Joshua J. Gertzen
 */
class XODOutputSource extends InputSource {
    private List<Object> rootObjects;
    private Map<Class, String> aliasMap;
    private Map<Object, String> ids;
    private List<String> includes;
    private boolean filterDefaults;
    
    public XODOutputSource(List<Object> rootObjects, Map<Object, String> ids, Map<String, Class> aliasMap, List<String> includes, boolean filterDefaults) {
        this.rootObjects = Collections.unmodifiableList(rootObjects);
        this.aliasMap = new HashMap<Class, String>();
        this.includes = includes == null ? new ArrayList<String>() : includes;
        this.ids = ids;
        
        if (aliasMap != null) {
            for (Map.Entry<String, Class> e : aliasMap.entrySet()) {
                this.aliasMap.put(e.getValue(), e.getKey());
            }
        }
        
        this.filterDefaults = filterDefaults;
    }
    
    public String getObjectId(Object o) {
        return ids.get(o);
    }
    
    public List<String> getIncludes() {
        return includes;
    }
    
    public Map<Class, String> getAliasMap() {
        return aliasMap;
    }
    
    public List<Object> getRootObjects() {
        return rootObjects;
    }
    
    public boolean isFilterDefaults() {
        return filterDefaults;
    }

    @Override
    public InputStream getByteStream() {
        throw new UnsupportedOperationException("XODInputSource must come directly from an object");
    }

    @Override
    public Reader getCharacterStream() {
        throw new UnsupportedOperationException("XODInputSource must come directly from an object");
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        throw new UnsupportedOperationException("XODInputSource must come directly from an object");
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
        throw new UnsupportedOperationException("XODInputSource must come directly from an object");
    }    
}
