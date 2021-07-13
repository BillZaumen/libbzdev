/**
 * Module containing BZDev classes for graphs, geometry, graphics IO, and
 * GUI support.
 * <P>
 * This module provides packages for generating graphs, configuring
 * named-object factories with graphics-specific parameters,
 * characterizing images, and writing graphics to printers and 
 * files (one image at a time). It provides a number of swing
 * components, a bridge between documents and writers, a class
 * to map the names of keyboard keys to the corresponding Java
 * constants.
 *
 * @moduleGraph
 */
module org.bzdev.desktop {
    exports org.bzdev.swing;
    exports org.bzdev.providers.swing to org.bzdev.obnaming;
    exports org.bzdev.swing.io;
    exports org.bzdev.swing.keys;
    exports org.bzdev.swing.proxyconf;
    exports org.bzdev.swing.table;
    exports org.bzdev.swing.text;
    opens org.bzdev.providers.swing.lpack;
    opens org.bzdev.swing.io.lpack;
    opens org.bzdev.swing.keys.lpack;
    opens org.bzdev.swing.lpack;
    opens org.bzdev.swing.proxyconf.lpack;
    opens org.bzdev.swing.table.lpack;
    requires java.base;
    requires java.desktop;
    requires java.prefs;
    requires java.xml;
    requires org.bzdev.base;
    requires org.bzdev.graphics;
    provides org.bzdev.lang.spi.ONLauncherData with
	org.bzdev.providers.swing.DesktopLauncherData;
}

//  LocalWords:  BZDev
