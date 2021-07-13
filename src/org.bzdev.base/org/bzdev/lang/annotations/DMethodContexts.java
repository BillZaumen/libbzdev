package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * List of DMethodContext annotations.
 * This annotation is used to list multiple DMethodContext annotations
 * when more than one is applicable to a class.  One may use
 * DmethodContext in cases where only one dynamic method is being
 * implemented.
 * <P>
 * When modules are used, one will typically create a directory named
 * mods and a subdirectory mods/MODULE where MODULE is the name of the
 * module (e.g., com.foo.bar). One will also create a directory tmpsrc
 * and a corresponding subdirectory tmpsrc/MODULE.  A typical compiler
 * command, assuming the source code is in a directory src, is
 * <BLOCKQUOTE><CODE><PRE>
 *    javac -d mods/MODULE -p /usr/share/bzdev -s tmpsrc/MODULE \
 *          --processor-module-path /usr/share/bzdev \
 *          src/MODULE/module-info.java src/MODULE/DIR/*.java
 * </PRE></CODE></BLOCKQUOTE>
 * where DIR is the directory corresponding to a package name
 * following the usual Java conventions (e.g., com/foo/bar for the
 * package com.foo.bar). Placing generated source files in
 * tmpsrc/MODULE and the source tree in src/MODULE will make it easy
 * to use javadoc to generate documentation: for javadoc the
 * options
 * <BLOCKQUOTE><CODE><PRE>
 *    -d TARGET
 *    --module-path PATH
 *    --module-source-path src:tmpsrc
 *    --add-modules MODULES
 *    --module MODULE
 * </PRE></CODE></BLOCKQUOTE>
 * and possibly
 * <BLOCKQUOTE><CODE><PRE>
 *    --exclude PACKAGES
 * </PRE></CODE></BLOCKQUOTE>
 * are useful. For these options,
 * <UL>
 *   <LI> TARGET is the directory that will hold the API documentation
 *   <LI> PATH is a list of directories separate by the path separator
 *       (":" for Linux or Unix, ';' for Windows), with each containing
 *       modular JAR files.
 *   <LI> MODULES is a comma-separated list of modules that should be
 *        included in the documentation.
 *   <LI> PACKAGES is a colon-separated list of package names listing
 *        packages that should be excluded (Fully qualified package
 *        names are necessary). The BZDev class library has a number
 *        of packages for which the last component of their names is
 *        "lpack". These packages contain Java "properties" files and
 *        are used for localization. As such, they should not be documented.
 * </UL>
 * @see org.bzdev.lang.annotations.DMethodContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DMethodContexts {
    /**
     * DMethodContext table.
     * An array of DMethodContext  annotations is needed because an
     * annotation processor can only look up one annotation with a
     * given name at a time and a class may implement multiple dynamic
     * methods.
     */

    DMethodContext[] value();
}

//  LocalWords:  DMethodContext DmethodContext subdirectory tmpsrc
//  LocalWords:  src BLOCKQUOTE PRE javac javadoc
