/**
 * Module providing 3D printing.
 * <P>
 * This package provides a programmatic API for 3D printing.
 * It is not a replacement for CAD software, but can be
 * used effectively for tasks such as creating a 3D graph
 * of a function of two variables. Utility classes make it
 * relatively easy to create some standard shapes.
 * <P>
 * <B>Please see
 * <UL>
 *  <LI> <A HREF="org/bzdev/p3d/doc-files/description.html">the P3D package description</A> for
 *    an extended description of the package provided by this module.
 *  <LI> <A HREF="{@docRoot}/factories-api/org/bzdev/p3d/Model3DViewFactory.html" target="_top"> the Model3DViewFactory  documentation</A>
 *     for configuring the factory provided by the package provided by 
 *     this module.
 * </UL>
 * </B>
 */
module org.bzdev.p3d {
    exports org.bzdev.p3d;
    // exports org.bzdev.providers.p3d to org.bzdev.obnaming;
    opens org.bzdev.p3d.lpack;
    opens org.bzdev.providers.p3d.lpack;
    requires java.base;
    requires java.desktop;
    requires org.bzdev.anim2d;
    requires org.bzdev.base;
    requires org.bzdev.obnaming;
    requires org.bzdev.graphics;
    requires org.bzdev.devqsim;
    provides org.bzdev.obnaming.NamedObjectFactory with
	org.bzdev.p3d.Model3DViewFactory;
    provides org.bzdev.lang.spi.ONLauncherData with
	org.bzdev.providers.p3d.P3DLauncherData;
}

//  LocalWords:  HREF DViewFactory
