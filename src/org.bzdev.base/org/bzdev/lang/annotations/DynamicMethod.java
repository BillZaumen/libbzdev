package org.bzdev.lang.annotations;
import java.lang.annotation.*;

/**
 * Annotation to mark a dynamic method definition.
 * The <code>DynamicMethod</code> annotation indicates that a method 
 * will be implemented in such so as to dispatch a method call based 
 * on the types of its arguments.  Methods marked with this annotation
 * are expected to be implemented in a particular way documented below.
 * <p>
 * The method annotated must take one or more arguments at least
 * one of which is not an array, enum, or primitive type, and will
 * use the type of such arguments to look up a method to invoke.
 * Because variable argument types are implemented via arrays in
 * Java, dynamic methods cannot be dispatched on a variable-argument
 * list. The methods invoked will usually be one of the methods marked
 * by a DMethodImpl annotation.
 * <p>
 * Usage depends on whether this annotation appears in a class or
 * an interface definition.  In a class definition, the method
 * marked by this annotation must use the following pattern:
 * <pre><code>
 *            &#064;DynamicMethod("Helper")
 *            public void method(Type arg) {
 *                Helper.getHelper().dispatch(this, msg);
 *            }
 * </code></pre>
 * where <code>method</code>, <code>Helper</code>,
 * <code>Type</code>, and <code>arg</code> should be replaced with the 
 * actual method, helper class, argument type, and argument names.
 * The dispatch method will throw a runtime exception
 * <code>
 * {@link org.bzdev.lang.MethodNotPresentException MethodNotPresentException}
 * </code>
 * if a matching method cannot be found
 * A return value may be provided.  in this case, the dynamic method
 * is specified as in the following example
 * <pre><code>
 *            &#064;DynamicMethod("Helper")
 *            public Foo method(Type arg) {
 *                return Helper.getHelper().dispatch(this, msg);
 *            }
 * </code></pre>
 * The helper class name may be fully qualified but if it is not,
 * the helper class is assumed to be in the current package (an
 * annotation processor does not currently handle import statements,
 * so one must use a fully-qualified name in an annotation whenever
 * the name does not refer to a name in the current package.)
 * <p>
 * In cases where exceptions may be thrown, the method tagged by
 * the <code>&#064;DynamicMethod</code> annotation must contain a
 * <code>throws</code> clause.
 * <p>
 * For an interface definition, one must use the following pattern:
 * <pre><code>
 *            &#064;DynamicMethod("Helper")
 *            public void method(Type arg);
 * </code></pre>
 * Then for each of the top-level class that implements the corresponding
 * interface, one must insert the following code
 * <pre><code>
 *            public void method(Type arg) {
 *                Helper.getHelper().dispatch(this, msg);
 *            }
 * </code></pre>
 * As with the previous case, <code>method</code>,
 * <code>Helper</code>, <code>Type</code>, and <code>arg</code>
 * should be replaced with the actual method, type and argument names,
 * and a type may be returned.  In that case, an example of the
 * interface definition is
 * <pre><code>
 *            &#064;DynamicMethod("Helper")
 *            public Foo method(Type arg);
 * </code></pre>
 * and the top level class implementing the interface should contain
 * <pre><code>
 *            public Foo method(Type arg) {
 *                return Helper.getHelper().dispatch(this, msg);
 *            }
 * </code></pre>
 * If exceptions are thrown, these must be listed in <code>throws</code>
 * clauses for the method in the interface and the method in the top-level
 * classes, following the normal rules Java uses in these cases. One
 * should note that the <code>dispatch</code> method is automatically
 * declared to throw the same exceptions as the ones declared for the
 * method annotated by <code>&#064;DynamicMethod</code>.
 * <p>
 * The search order for dynamic methods starts with the current class
 * and sorts methods based on the types of their arguments, considering
 * only those that are not arrays, primitive types, or enumeration types.
 * The sorting is such that the most specific type is tried first - a
 * For a type T1, a Type T2 is less specific if T2 is a superclass of T1
 * or an interface that T1 implements or extends.  If there is not an
 * exact match, the first method whose type is assignable from the
 * actual type of the argument is used.  If no match is found, this is
 * repeated, replacing the current class with its superclass and repeating
 * the search on the superclass' methods.
 * <P>
 * A consequence of these rules is that a subclass can hide methods of
 * its superclass.  In the following example,
 * <pre><code>
 *     {@literal @}DMethodContext(localHelper="SuperclassHelper",
 *                     localHelper="SuperclassLocalHelper")
 *     public class Superclass {
 *         static {
 *            SuperclassLocalHelper.register();
 *         }
 *
 *         {@literal @}DynamicMethod("SuperclassHelper")
 *         public void test(Object obj) {
 *             try {
 *                 SuperclassHelper.getHelper().dispatch(this, obj);
 *             } catch (MethodNotPresentException e) {
 *               // default behavior
 *               ...
 *             }
 *         }
 *         {@literal @}DMethodImpl("SuperclassHelper")
 *         public void doTest(Number number) {
 *           System.out.println("Superclass: doTest(Number)");
 *         }
 *     }
 *
 *     {@literal @}DMethodContext(localHelper="SubclassHelper",
 *                     localHelper="SubclassLocalHelper")
 *     public class Subclass extends Superclass {
 *
 *         {@literal @}DMethodImpl("SuperclassHelper")
 *         public void doTest(Object object) {
 *           System.out.println("Subclass: doTest(Object)");
 *         }
 *         {@literal @}DMethodImpl("SuperclassHelper")
 *         public void doTest(Double x) {
 *           ...
 *         }
 *     }
 *     public class Test {
 *         public static void main(String argv[]) {
 *            Subclass subclass = new Subclass();
 *            subclass.test(new Float(0.0F));
 *         }
 *
 *     }
 * </code></pre>
 * the statement <code>subclass.test(new Float(0.0F)</code> will result in
 * "<code>Subclass: doTest(Object)</code>" being printed because the
 * method search terminates when the doTest method taking an Object
 * as its argument is reached.
 * <P>
 * For methods with multiple argument, the search order is also based
 * on the position of the argument (first argument, second, etc.).
 * The default order varies argument n faster than argument m when
 * n &gt; m; however, the order can be explicitly set by the the
 * <code>DMethodOrder</code> annotation.
 * <pre><code>
 *           &#064;DynamicMethod("FooBarHelper")
 *           public void method(Object x, int i, Object y) {
 *               FooBarHelper.getHelper().dispatch(this, x, i, y);
 *           }
 *           &#064;DMethodImpl("FooBarHelper")
 *           public void doMethod(Foo x, int i, Bar y) {
 *              ...
 *           }
 *           &#064;DMethodImpl("FooBarHelper")
 *           public void doMethod(Bar x, int i, Bar y) {
 *              ...
 *           }
 *           &#064;DMethodImpl("FooBarHelper")
 *           public void doMethod(Bar x, int i, Foo y) {
 *              ...
 *           }
 * </code></pre>
 * the search order when Bar extends Foo is
 * <pre><code>
 *           doMethod(Bar, int, Bar)
 *           doMethod(Bar, int, Foo)
 *	     doMethod(Foo, int, Bar)
 * </code></pre>
 * and the first match will be used.  The search order can be
 * modified by using the <code>DMethodOrder</code> annotation as
 * stated above.
 * <p>
 * Some tuning for particular computational environments is
 * possible.  This is controlled by the <code>DMethodOptions</code>
 * annotation, which should only be present on a method annotated
 * by the <code>DynamicMethod</code>annotation. For example, locks
 * used on the helper class can be removed for single-threaded
 * application. The use of the <code>DMethodOptions</code> 
 * annotation is not mandatory.
 * <p>
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
 * <p>
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
 * <BLOCKQUOTE><PRE><CODE>
 *    javac -d mods/MODULE -p /usr/share/bzdev -s tmpsrc/MODULE \
 *          --processor-module-path /usr/share/bzdev \
 *          src/MODULE/module-info.java src/MODULE/DIR/*.java
 * </CODE></PRE></BLOCKQUOTE>
 * where DIR is the directory corresponding to a package name
 * following the usual Java conventions (e.g., com/foo/bar for the
 * package com.foo.bar). Placing generated source files in
 * tmpsrc/MODULE and the source tree in src/MODULE will make it easy
 * to use javadoc to generate documetation: for javadoc the
 * options
 * <BLOCKQUOTE><PRE><CODE>
 *    -d TARGET
 *    --module-path PATH
 *    --module-source-path src:tmpsrc
 *    --add-modules MODULES
 *    --module MODULE
 * </CODE></PRE></BLOCKQUOTE>
 * and possibly
 * <BLOCKQUOTE><PRE><CODE>
 *    --exclude PACKAGES
 * </CODE></PRE></BLOCKQUOTE>
 * are useful. For these options,
 * <UL>
 *   <LI> TARGET is the directory that will hold the API documentation
 *   <LI> PATH is a list of directories separate by the path separator
 *       (":" for Linux or Unix, ';' for Windows), with each containing
 *       modular JAR files.
 *   <LI> MODULES is a comma-separated list of modules that should be
 *        included in the documentation.
 *   <LI> PACKAGES is a colon-separated list of package names listing
 *        packages that should be excluded (Fully qualitifed package
 *        names are necessary). The BZdev class libarary has a number
 *        of packages for which the last component of their names is
 *        "lpack". These packages contain Java "properties" files and
 *        are used for localization. As such, they should not be documented.
 * </UL>
 * @see org.bzdev.lang.annotations.DMethodOrder
 * @see org.bzdev.lang.annotations.DMethodImpl
 * @see org.bzdev.lang.annotations.DMethodOptions
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DynamicMethod {
    /**
     * The class name of the top-level helper class.
     * This class name is used as a key to distinguish various
     * dynamic methods, and the code for the helper class will
     * be generated automatically. If it is a simple name, the
     * current package is used; otherwise
     * key must be a fully-qualified class name for a (new) class
     * in the current package.
     * @return the class name of the top-level helper class
     */
    String value();
}

//  LocalWords:  DynamicMethod enum DMethodImpl pre arg msg runtime
//  LocalWords:  MethodNotPresentException superclass DMethodContext
//  LocalWords:  localHelper SuperclassHelper SuperclassLocalHelper
//  LocalWords:  doTest SubclassHelper SubclassLocalHelper argv javac
//  LocalWords:  DMethodOrder FooBarHelper doMethod DMethodOptions
//  LocalWords:  Microsystem's TMPSRC classpath subdirectories SRC
//  LocalWords:  blockquote libbzdev subdirectory tmpsrc
