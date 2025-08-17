/**
 * Module providing 2D animations.
 * <P>
 * This module treats animations as a type of discrete event
 * simulation. The simulations are for the most part animated
 * diagrams.  Objects can, however, be drawn to scale. A view can
 * scroll over a larger area with the view either sliding, zooming, or
 * (if desired) rotating.  Objects can be given a path to follow and
 * and be given an initial velocity and acceleration, either of which
 * can be changed at any point in time.
 * <P>
 * <B>Please see
 * <A HREF="org/bzdev/anim2d/doc-files/description.html"> the org.bzdev.anim2d description</A>
 * for a description of how to use this module.</B>
 * <P>
 * This module also contains an
 * <A HREF="doc-files/startup.html"><B>ESP startup resource</B></A> for use
 * with the <STRONG>scrunner</STRONG> command. As the <STRONG>scrunner</STRONG>
 * manual page indicates, you can add the argument
 * <STRONG>module:org.bzdev.anim2d</STRONG> to the <STRONG>scrunner</STRONG>
 * command line.
 */
module org.bzdev.anim2d {
    exports org.bzdev.anim2d;
    // exports org.bzdev.providers.anim2d to org.bzdev.obnaming;
    opens org.bzdev.anim2d.lpack;
    opens org.bzdev.providers.anim2d.lpack;
    requires java.base;
    requires java.desktop;
    requires org.bzdev.base;
    requires org.bzdev.obnaming;
    requires org.bzdev.devqsim;
    requires org.bzdev.graphics;
    provides org.bzdev.obnaming.spi.ONLauncherProvider with
	org.bzdev.providers.anim2d.Animation2DLauncherProvider;
    provides org.bzdev.obnaming.NamedObjectFactory with
	org.bzdev.anim2d.AnimationLayer2DFactory,
	org.bzdev.anim2d.AnimationPath2DFactory,
	org.bzdev.anim2d.AnimationShape2DFactory,
	org.bzdev.anim2d.CartesianGrid2DFactory,
	org.bzdev.anim2d.ConnectingLine2DFactory,
	org.bzdev.anim2d.GraphViewFactory,
	org.bzdev.anim2d.PolarGridFactory;

}

//  LocalWords:  HREF scrunner
