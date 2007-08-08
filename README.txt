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

===============================================================================
				        About ThinWire(R) Form Creator	
===============================================================================
The Form Creator application, while not 100% complete or function yet, provides
developers with a way to visually layout forms. Here are a list of some of its
features:
 by performing drag and drop
operations and visual resizing and positioning.  Additionally, the Form Creator
provides an extensive properties panel that makes it easy to adjust most of
the settings of any component you are visually laying out.

    * manage multiple forms under a single project (mostly working)
    * load and save the forms to a project file (half done)
    * load and save dialog used for picking local directory to save/load
    * double click to add components to the current form (add a panel first!)
    * drag and drop new components from the tool box onto a form
    * position components by dragging them around on the screen
    * resize components by dragging resize handles
    * remove components by using the hot key (Ctrl-D)
    * modify nearly any property of a component using the property sheet
    * immediate change of properties when changed
    * error highlighting when invalid property values entered
    * view form source as a XOD file (a little verbose, but mostly works)
    * save form source as a XOD file, as part of the project (not working)
    * load existing XOD file into project (works if you select the project)
    * multiple form editing at the same time

It's worth noting that this form creator is absolutely a work in progress.
By providing this to the community in its current state, I hope that others will
jump in and extend what I've done thus far and then contribute it back to this
subproject of ThinWire. With that said, the basic view XOD source feature works
reasonably well and with a little tweaking, this application could be very very
powerful.

===============================================================================
                           Building the Form Creator
===============================================================================
The build process for the demo is defined using the Apache Ant build tool. It
has only been built using Ant 1.6 or greater, but it may build correctly with
earlier releases as well.  You can learn about the Apache Ant project and
download a working version from: http://ant.apache.org/

Once you have Ant installed and added to your system path, you can build the
demo simply by typing 'ant dist' at the command shell from the 'build'
directory.  The following Ant build targets are supported:

 dist        compile the application, create a jar and package
	         it along with other required runtime files into
             a distribution zip.
	
 source      create a source only distribution that contains
             everything necessary to build the application.
