package org.bzdev.obnaming.annotations;
import java.lang.annotation.*;

/**
 * Specify the class name of the ParmManager for a named-object
 * factory.
 * This annotation is used by an annotation processor.
 * <P>
 * To compile for the non-modular case, use
 *<pre><code>
 * javac -d CLASSES -s TMPSRC				\
 *       -classpath /usr/share/java/libbzdev.jar:CLASSES	\
 *       FILES...
 * </code></pre>
 * where <code>CLASSES</code> is the name of a directory containing the
 * class files (with subdirectories matching the package
 * hierarchy), <code>TMPSRC</code> is a directory for files created by
 * the annotation processor, and <code>FILES...</code> is a list of source
 * files (with directories matching the package hierarchy).
 * <P>
 * To compile when using Java modules, use
 * <BLOCKQUOTE><CODE><PRE>
 *    javac -d mods/MODULE -p /usr/share/bzdev -s tmpsrc/MODULE	\
 *          --processor-module-path /usr/share/bzdev		\
 *          src/MODULE/module-info.java src/MODULE/DIR/*.java
 * </PRE></CODE></BLOCKQUOTE>
 * where MODULE is the name of the module and DIR is the directory
 * corresponding to a package name following the usual Java conventions
 * (e.g., com/foo/bar for the package com.foo.bar). Placing generated
 * source files in tmpsrc/MODULE and the source tree in src/MODULE will
 * make it easy to use javadoc to generate documentation
 * <P>
 * <B>Please see the
 * <A HREF="doc-files/FactoryParmManager.html">extended description</A>
 * for a detailed description of this class.</B>
 * <P>
 * When this annotation is used, the ParmManager created will use
 * type parameters and these must be provided in the same order as
 * in the class being annotated.  Also, if the stdFactory option is
 * used, at least 1 type parameter is needed and the last type parameter
 * in the list must refer to the type of the named object being
 * created.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface FactoryParmManager {
    /**
     * Class name of the ParmManager for a factory.
     * The class will be one in the factory's package, so the
     * name must be a simple name,  not a fully qualified name.
     * If the class name is an empty string, a ParmManager will not
     * be created. Setting <CODE>value</CODE> to an empty string is
     * useful in some special/unusual cases.
     */
    String value();

    /**
     * The class name for a resource bundle for tips.
     * The name must be either a simple name, a qualified class name
     * starting with "*.", in which the '*' is replaced with the name
     * of the current package to create a fully-qualified class name,
     * or an explicit fully-qualified name.  When the current package
     * is not the unnamed package, the resulting class name must match
     * either the current package or a subpackage whose name is
     * "lpack".  When the current package is the unnamed package, the
     * resource bundle must also be in the unnamed package.
     * If no value is provided or the value is an empty string, a tip
     * resource bundle is not configured.
     * <P>
     * When provided, the key for a tip is a parameter name. The value
     * is a string suitable for use by a tool tip, and will also be
     * used in lsnof documentation for a short summary of a parameter.
     */
    String tipResourceBundle() default "";

    /**
      * The class name for a resource bundle for labels.
     * The name must be either a simple name, a qualified class name
     * starting with "*.", in which the '*' is replaced with the name
     * of the current package to create a fully-qualified class name,
     * or an explicit fully-qualified name.  When the current package
     * is not the unnamed package, the resulting class name must match
     * either the current package or a subpackage whose name is
     * "lpack".  When the current package is the unnamed package, the
     * resource bundle must also be in the unnamed package.
     * If no value is provided or the value is an empty string, a label
     * resource bundle is not configured.
     * <P>
     * When provided, the key for a label is a parameter name. The value
     * is a short string (at most a few words) provided a label for a
     * parameter. The text is suitable for labeling a control in a
     * Swing panel or dialog box, but is also used in lsnof-generated
     * documentation. The label is a title, so most of the words in it
     * should be capitalized.
     */
    String labelResourceBundle() default "";

    /**
     * The class name for a resource bundle for parameter documentation.
     * The name must be either a simple name, a qualified class name
     * starting with "*." or a fully-qualified name whose package
     * component either matches the package of the class being
     * annotated or starts with the package of the class being
     * annotated but followed by a package component named "lpack".
     * The '*." form is meant to suggest a wildcard in which the '*'
     * will be replaced with the package name of the class being annotated.
     * If no value is provided or the value is an empty string, a label
     * resource bundle is not configured.
     * <P>
     * When provided, the key for documentation is a parameter
     * name. The value consists of snippets of HTML code.  How the
     * <CODE>CODE</CODE> element and the special-purpose
     * <CODE>JDOC</CODE> element are handled is described above and in
     * the documentation for
     * {@link org.bzdev.obnaming.NamedObjectFactory#getTemplateKeyMap()}.
     */
    String docResourceBundle() default "";

    /**
     * The simple name for a standard factory.
     * Normally this annotation is used with abstract factory classes.
     * If the corresponding named object is not abstract, there will
     * frequently be a non-abstract, <I>standard</I> factory paired
     * with the abstract factory being annotated. The Java class for this
     * standard factory can be automatically generated by setting this
     * this annotation element to the desired class name. When this
     * element is provided, the class being annotated must use type
     * parameters and the last type parameter in the list must refer to
     * the type of the named object that the factory will create.
     */
    String stdFactory() default "";

    /**
     * The name of the constructor's argument.
     * This allows the name of the constructor's argument to be
     * set to a more mnemonic value than the default as that
     * argument is displayed in javadoc-generated documentatio
     */
    String namerVariable() default "namer";

    /**
     * The documentation string for the constructor's argument.
     * This is the string that follows the name of a constructor's
     * argument in an {@literal @}param statement in a Javadoc comment.
     */
    String namerDocumentation() default "the object namer";

    /**
     * When an IFRAME is used to display the parameter documentation for
     * a standard factory, the IFRAME will be configured using a style.
     * This element allows that style to be changed.
     */
    String iframeStyle() default
	"width:95%;height:500px;border:3px solid steelblue";

    /**
     * Determine if the standard factory's parameter documentation should
     * be shown in an HTML IFRAME in the standard factory's documentation
     */
    boolean showParameterDocumentation() default true;
}

//  LocalWords:  ParmManager NamedObjectFactory CompoundParmType lt
//  LocalWords:  PrimitiveParm KeyedPrimitiveParm CompoundParm html
//  LocalWords:  KeyedCompoundParm lsnof br BZDev subpackage lpack
//  LocalWords:  wildcard BLOCKQUOTE PRE FactoryParmManager initParms
//  LocalWords:  AbstractFooFactoryPM AbstractFooFactory
