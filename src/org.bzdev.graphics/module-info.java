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
 * constants, and classes representing various types of curves
 * and classes to obtain data about paths.
 */
module org.bzdev.graphics {
    exports org.bzdev.geom;
    exports org.bzdev.gio;
    exports org.bzdev.gio.spi;
    exports org.bzdev.graphs;
    exports org.bzdev.graphs.spi;
    // exports org.bzdev.providers.graphics to org.bzdev.obnaming;
    exports org.bzdev.imageio;
    exports org.bzdev.obnaming.misc;
    opens org.bzdev.geom.lpack;
    opens org.bzdev.gio.lpack;
    opens org.bzdev.graphs.lpack;
    opens org.bzdev.imageio.lpack;
    opens org.bzdev.obnaming.misc.lpack;
    opens org.bzdev.providers.graphics.lpack;
    requires java.base;
    requires java.desktop;
    requires java.scripting;
    requires java.xml;
    requires org.bzdev.base;
    requires org.bzdev.math;
    requires org.bzdev.obnaming;
    uses org.bzdev.graphs.spi.SymbolProvider;
    provides org.bzdev.graphs.spi.SymbolProvider with
	org.bzdev.graphs.symbols.EmptyCircleProvider,
	org.bzdev.graphs.symbols.SolidCircleProvider,
	org.bzdev.graphs.symbols.EmptySquareProvider,
	org.bzdev.graphs.symbols.SolidSquareProvider,
	org.bzdev.graphs.symbols.EmptyHourglassProvider,
	org.bzdev.graphs.symbols.SolidHourglassProvider,
	org.bzdev.graphs.symbols.EmptyBowtieProvider,
	org.bzdev.graphs.symbols.SolidBowtieProvider;
    uses org.bzdev.gio.spi.OSGProvider;
    provides org.bzdev.gio.spi.OSGProvider with
	org.bzdev.gio.OSGImageProvider,
	org.bzdev.gio.OSGPostscriptProvider;
    provides org.bzdev.lang.spi.ONLauncherData with
	org.bzdev.providers.graphics.GraphicsLauncherData;
}

//  LocalWords:  BZDev
