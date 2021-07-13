/**
 * Overall module for the BZDev class library.
 *<P>
 * This module provides all the BZDev class library modules needed at
 * runtime.
 *
 * @moduleGraph
 */
module org.bzdev {
    requires transitive org.bzdev.base;
    requires transitive org.bzdev.dmethods;
    requires transitive org.bzdev.esp;
    requires transitive org.bzdev.math;
    requires transitive org.bzdev.obnaming;
    requires transitive org.bzdev.parmproc;
    requires transitive org.bzdev.graphics;
    requires transitive org.bzdev.desktop;
    requires transitive org.bzdev.ejws;
    requires transitive org.bzdev.devqsim;
    requires transitive org.bzdev.drama;
    requires transitive org.bzdev.anim2d;
    requires transitive org.bzdev.p3d;
}

//  LocalWords:  BZDev runtime
