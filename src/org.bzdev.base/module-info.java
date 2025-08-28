/**
 * Base module for the BZDev class library.
 * <P>
 * This module provides IO classes, dynamic methods, mathematical
 * functions, basic statistical analysis, special-purpose protocols,
 * random variables, scripting support, and various utility classes.
 * <P>
 * The documentation includes a description of
 * <A HREF="org/bzdev/util/doc-files/esp.html">the ESP scripting language<A>,
 * which is implemented using {@link org.bzdev.util.ExpressionParser}.
 */
module org.bzdev.base {
    exports org.bzdev.io;
    exports org.bzdev.lang;
    exports org.bzdev.lang.spi;
    exports org.bzdev.lang.annotations;
    exports org.bzdev.net;
    exports org.bzdev.net.calendar;
    exports org.bzdev.obnaming.annotations;
    exports org.bzdev.protocols;
    exports org.bzdev.protocols.resource to java.base;
    exports org.bzdev.protocols.sresource to java.base;
    exports org.bzdev.util;
    exports org.bzdev.util.units;
    exports org.bzdev.scripting;
    requires java.base;
    opens org.bzdev.io.lpack;
    opens org.bzdev.lang.lpack;
    opens org.bzdev.net.calendar.lpack;
    opens org.bzdev.net.lpack;
    opens org.bzdev.protocols.resource.lpack;
    opens org.bzdev.protocols.sresource.lpack;
    opens org.bzdev.scripting.lpack;
    opens org.bzdev.util.lpack;
    // requires java.desktop;
    requires java.prefs;
    requires java.scripting;
    requires java.xml;
}

//  LocalWords:  BZDev HREF
