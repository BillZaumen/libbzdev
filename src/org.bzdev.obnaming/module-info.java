/**
 * Module providing object naming.
 * <P>
 * This module provides pairs of objects: an object namer and a named
 * object. The use of Java generics and annotation processing allows
 * these to be 'spliced' into another class hierarchy.  Uses include
 * the org.bzdev.devqsim package, where a simulation is an object
 * namer that is a subclass of a class that supports scripting.
 *
 * <P><B>Please see
 * <A HREF="org/bzdev/obnaming/doc-files/description.html">the package description</A>
 * for an extended description of the {@link org.bzdev.obnaming} package.</B>
 */
module org.bzdev.obnaming {
    exports org.bzdev.obnaming;
    // exports org.bzdev.providers.obnaming;
    exports org.bzdev.obnaming.spi;
    opens org.bzdev.obnaming.lpack;
    opens org.bzdev.providers.obnaming.lpack;
    requires org.bzdev.base;
    requires org.bzdev.math;
    requires java.base;
    uses org.bzdev.obnaming.NamedObjectFactory;
    uses org.bzdev.obnaming.spi.ONLauncherProvider;
    uses org.bzdev.lang.spi.ONLauncherData;
    provides org.bzdev.obnaming.spi.ONLauncherProvider with
	org.bzdev.providers.obnaming.DefaultLauncherProvider;
}

//  LocalWords:  namer HREF
