package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * Annotation to provide class names for dynamic-method helpers.
 * This annotation applies to each class definition that contains at least
 * one method with a <code>{@literal @}DMethodImpl</code> annotation.
 * <p>
 * Dynamic methods are implemented using "helper" classes. There is a
 * helper associated with the top-level class or interface type, and
 * also one associated with each class that implements a dynamic
 * method. The top-level class or interface's helper is created
 * via a <code>{@literal @}DynamicMethod</code> annotation, whereas
 * the methods for specific argument types are tagged with
 * <code>{@literal @}DMethodImpl</code> annotations, where
 * the top-level helper class name is used as a key. This key
 * associates the method implementing a specific set of arguments
 * with the dynamic method.  Each class implementing dynamic methods
 * by using <code>{@literal @}DMethodImpl</code> annotations must be
 * tagged with a <code>{@literal @}DMethodContext</code> annotation
 * that associates a top-level helper class with a local helper class.
 * The local helper class must be a class in the current package
 * (an unqualified class name can be used).
 * <p>
 * If more than one key is appropriate, use the
 * <code>{@literal @}DMethodContexts</code> annotation to list each one.
 * Recommended options for the java compiler (we assume the
 * command-line syntax used in Sun Microsystem's implementation)
 * for non-modular jar files are
 * <pre><code>
 *  javac -d CLASSES -s TMPSRC \
 *         -classpath /usr/lib/libbzdev/libbzdev.jar:CLASSES \
 *         FILES...
 * </code></pre>
 * where <code>CLASSES</code> is the name of a directory containing
 * the class files (with subdirectories matching the package
 * hierarchy), <code>TMPSRC</code> is a directory for files created by
 * the annotation processor, and <code>SRC</code> is a directory (or
 * search path) containing source files (again with subdirectories
 * matching the package hierarchy).  Without <code>CLASSES</code> in
 * the class path, you may get a warning about implicitly compiled
 * files. Without the '-s' flag, automatically generated java files
 * will appear in the CLASSES directory.  The default locking mode
 * can be set at compile time by adding a -A flag as described in the
 * documentation for {@literal @}{@link DMethodOptions DMethodOptions}.
 * <P>
 * During compilation, the JAR file containing the org.bzdev.base
 * module must be accessible from the class path, as must the file
 * <blockquote>
 * META-INF/services/javax.annotation.processing.Processor
 * </blockquote>
 * (the libbzdev.jar file contains both).  For separate compilation,
 * the class files corresponding to the helper files are needed.
 * <p>
 * Finally, if none of the files being compiled contain a
 * <code>DynamicMethod</code> or <code>DMethodImpl</code> annotation,
 * nothing special needs to be done.
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
s *        names are necessary). The BZDev class library has a number
 *        of packages for which the last component of their names is
 *        "lpack". These packages contain Java "properties" files and
 *        are used for localization. As such, they should not be documented.
 * </UL>


 * @see org.bzdev.lang.annotations.DMethodImpl
 * @see org.bzdev.lang.annotations.DMethodContexts
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DMethodContext {
    /**
     * Helper class name.
     * This is the fully qualified class name for the helper class
     * used in the matching DMethodImpl annotation for one or more
     * methods in the class marked by this annotation.
     */
    String helper();
    /**
     * Local helper class name.
     * This is the class name of a class in the current package
     * that will be used to implement a dynamic method. The
     * code for that class will be automatically generated. If
     * not fully qualified, the package name will be added to
     * the class name.
     */
    String localHelper();

    // String[] value();
}

//  LocalWords:  DMethodImpl DynamicMethod DMethodContext pre javac
//  LocalWords:  DMethodContexts Microsystem's TMPSRC classpath SRC
//  LocalWords:  subdirectories DMethodOptions blockquote libbzdev
//  LocalWords:  subdirectory tmpsrc src javadoc lpack BZDev
