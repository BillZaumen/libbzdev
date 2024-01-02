package org.bzdev.obnaming;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.util.*;
import org.bzdev.math.rv.*;
import org.bzdev.util.SafeFormatter;
import org.bzdev.net.WebEncoder;
import org.bzdev.util.ACMatcher;
import org.bzdev.util.ACMatcher.MatchResult;

import java.lang.annotation.Annotation;
import java.util.*;
import java.net.URI;
import java.net.URL;
import java.util.regex.*;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.*;

//@exbundle org.bzdev.obnaming.lpack.NamedObjectFactory

/**
 * Base class for factories for named objects.
 * Factories provide a uniform interface for creating and configuring
 * objects.  Configuration parameters are manipulated by 'set', 'add',
 * and 'clear' methods, and methods can create multiple objects in a
 * single operation. If the object namer associated with the factory
 * supports scripting and if it supports scripting-based configuration,
 * a scripting language can be used to configure the factory.  The
 * default behavior is to use a scripting-language object to configure
 * the factory when the object namer is a subclass of
 * {@link org.bzdev.scripting.ScriptingContext}.  For the Javascript
 * (EMCAScript) case, the object used to configure the factory is
 * either a Javascript object or a Javascript array that is passed to
 * a factory method named "configure".  For an array,
 * each element is processed in order. For a Javascript object, which
 * is a collection of attribute-value pairs, the names of the attribute
 * correspond to the parameter names used in set or add methods. There
 * are several reserved names and these are treated specially.  The
 * reserved attribute names are
 * <ul>
 *    <li> "withKey" - the value is the name of a set/add key.
 *         In this case, the key is used by the current object and
 *         any nested objects (objects indicated by a "config" attribute).
 *         A "withIndex" attribute is not allowed.
 *    <li> "withIndex " - The value of this parameter is a Javascript
 *         array. The attribute "withKey" is not allowed.
 *    <li> "withPrefix " - a prefix is defined. For the value of "config"
 *         or each element of the value of "withIndex", each non-reserved
 *         attribute name NAME is replaced with PREFIX.NAME, where PREFIX
 *         is the value of the "prefix" attribute.  If other "withPrefix"
 *         attributes are available due to nesting, all of them are
 *         concatenated (outer first), with a period separating them.
 *    <li> "config" - this indicates that a nested object or array of
 *         objects should be used as well. Nested objects inherit "withKey"
 *         and "withPrefix" attributes.
 * </ul>
 * Depending on the parameter, factory's are configured using 'set' or
 * 'add' methods. Factory methods allow one to determine which should be
 * used and this choice is handled transparently by the 'configure' method.
 * For the case where 'add' is appropriate. the value associated with a
 * parameter can be an array providing a set of values to add.
 * For example consider a factory with parameters "foo", "bar" and
 * keyed parameters "timeline.foo" and "timeline.bar" that use integer keys.
 * The following code
 * <blockquote><pre>
 *      factory.configure({foo: 10, bar: 20})
 * </pre></blockquote>
 *  is equivalent to
 * <blockquote><pre>
 *      factory.set("foo", 10);
 *      factory.set("bar", 20);
 * </pre></blockquote>
 * The following code
 * <blockquote><pre>
 *      factory.configure({withPrefix: "timeline",
 *                         withKey: "b",
 *                         config: {foo: 10, bar: 20}});
 * </pre></blockquote>
 *  is equivalent to
 * <blockquote><pre>
 *      factory.set("timeline.foo", "b", 10);
 *      factory.set("timeline.bar", "b", 20);
 * </pre></blockquote>
 *  The following code
 * <blockquote><pre>
 *      factory.configure({withPrefix: "timeline",
 *                         withIndex: [{foo: 10}, {foo: 20}]);
 * </pre></blockquote>
 * is equivalent to
 * <blockquote><pre>
 *      factory.set("timeline.foo", 0, 10);
 *      factory.set("timeline.foo", 1, 20);
 * </pre></blockquote>
 * The following code
 * <blockquote><pre>
 *      factory.configure({withPrefix: "timeline",
 *                         withIndex: [{foo: 10}, {foo: 20}]
 * </pre></blockquote>
 * is simply a shorthand notation for
 * <blockquote><pre>
 *      factory.configure([{withKey 0, config: {"timeline.foo": 10}},
 *                        {withKey 1, config: {"timeline.bar": 20}}]);
 * </pre></blockquote>
 * If the factory has a parameter "foobar" that represents a set of
 * integers, the code
 * <blockquote><pre>
 *      factory.configure({foobar: [10, 20, 30]});
 * </pre></blockquote>
 * can be used. This is equivalent to
 * <blockquote><pre>
 *      factory.add("foobar", 10);
 *      factory.add("foobar", 20);
 *      factory.add("foobar", 30);
 * </pre></blockquote>
 * <P>
 * Each supported scripting language will use a syntax appropriate to
 * it. Python uses essentially the same syntax, but all the attribute
 * names must be quoted.
 * <P>
 * To simplify the creation of a large number of objects in which some
 * parameters may differ, a factory can be written to make use of
 * random variables.  In this case, one can provide the factory with
 * either a primitive value (int, long, double, or boolean) or a
 * corresponding random variable, which will be used to obtain a
 * separate value for each object created.
 * <P>
 * Factories are defined by a set of named parameters.  An array of Parm
 * objects and/or a ParmManager defines the parameters, and usually
 * the array or ParmManager is passed to initParms in a constructor
 * for each class in a class hierarchy that defines some of a
 * factory's parameters.  If multiple arrays and ParmManager objects
 * are used, each requires a separate call to initParms.  For each
 * Parm, there will typically be a corresponding field to hold its
 * value.  This is set or cleared (clearing may mean restoring a
 * default value) by using an anonymous class that overrides the
 * appropriate ParmParser methods.  When a ParmManager is used,
 * it will typically have been created by a Java annotation processor,
 * using the following annotations:
 * <ul>
 *  <li>FactoryParmManager. This annotation applies to a factory's class
 *      definition and indicates the name of a ParmManager for the factory
 *      to use.
 *  <li>PrimitiveParm.  This annotation applies to fields defined by a
 *      factory, and provides data needed to generate a Parm instance.
 *      The fields are simple types: int or Integer, long or Long,
 *      boolean or Boolean, double or Double, String, any enumeration,
 *      a named object (which implements NamedObjectOps), or a set of
 *      the above.
 *  <li> KeyedPrimitiveParm: a table with a key that may be an integer,
 *       long, String, or named object and a value suitable for a
 *       a primitive parameter.  The corresponding object must be an
 *       implementation of java.util.Map.
 *  <li> CompoundParmType.  This annotation applies to a class that will
 *       be used to define a parameter with a compound name.  The parameters
 *       the  annotated class defines must be primitive parameters.
 *  <li> CompoundParm. This defines a parameter whose value is an
 *       instance of a class annotated by CompoundParmType and that will
 *       be used as the first component of  compound parameters whose second
 *       components are the names of primitive parameters that a
 *       CompoundParmType's class define.
 *  <li> KeyedCompoundParms provide the same keys as a KeyedPrimitiveParm
 *       but the value type of the map is a class annotated by
 *       CompoundParmType.
 * </ul>
 * While there can be cases that require special treatment, it is usually
 * far simpler to have an annotation processor generate a ParmManager and
 * use that instead of constructing a Parm manually. The subclass of
 * ParmManager created when a FactoryParmManager annotation is used has
 * uses the same generic types as the factory it helps configure. These
 * must be used with the same names and in the same order as in the
 * factory definition, and will generally differ from the type parameters
 * shown for the documentation for this class. As an example,
 * <A ID="pmexample"></A>
 * <BLOCKQUOTE><PRE><CODE>
 *     {@literal @}FactoryParmManager(value="AbstractFooFactoryPM")
 *     public abstract class AbstractFooFactory&lt;OBJ extends Foo&gt; {
 *        ...
 *        AbstractFooFactory&lt;OBJ&gt; pm;
 *        public AbstractFooFactory(Animation2D a2d) {
 *           pm = new AbstractFooFactoryPM&lt;OBJ&gt;(this);
 *           initParms(pm, AbstractFooFactory.class);
 *        }
 *     }
 * </CODE></PRE></BLOCKQUOTE>
 * would be appropriate for a factory for a subclass of some object
 * defined in the <code>anim2d</code> package --- the class created by the
 * annotation processor is defined as follows:
 * <BLOCKQUOTE><PRE><CODE>
 *     class AbstractFooFactoryPM&lt;OBJ extends Foo&gt;
 *        extends ParmManager&lt;AbstractFooFactory&lt;Obj&gt;&gt;
 *     {
 *        ...
 *     }
 * </CODE></PRE></BLOCKQUOTE>
 * If the type for the ParmManager is not expressed exactly as shown
 * above, type-erasure issues will cause a compile-time error.
 * <P>
 * A factory that is not an abstract class must implement the
 * method newObject, which takes a string as an argument. Abstract
 * factories should not implement this method.  Any factory may
 * implement the following methods to initialize named objects (shown in
 * the order in which they will be called):
 * <ul>
 *   <li>startObjectCreation(): called at the start of a sequence of
 *       operations to create one or more objects. The method
 *       newObject will be called after this method has been called.
 *   <li>initObject(OBJ): called to initialize an object after it has
 *       been created.
 *   <li>arrayInit(T[],int,int) where T is a type parameter: called once when
 *       an array of objects has been created and initObject has been called
 *       for each of those objects.
 *   <li>doAfterInits(OBJ): called for each object being created after
 *       initObject and possibly arrayInit has been called.
 *   <li>endObjectCreation(): called at the end of a sequence of operations
 *       to create one or more objects.
 * </ul>
 * These methods generally are expected to invoke the same method on
 * its superclass as the first statement in each method that is
 * implemented.  In the simplest cases, one might only implement initObject,
 * with each (factory) superclass responsible for initializing parameters it
 * has defined.
 * <P>
 * Generally a factory should implement the method clear(), which is
 * should start by calling super.clear().  The rest of this method should
 * clear the existing fields by restoring them to their default values.
 * As a convenience, when a parm manager is used, each parm manager provides
 * a method name setDefaults that takes the current factory as its argument.
 * Calling the setDefaults method will automatically reset the fields
 * associated with parameters defined via annotations for the current
 * class.
 * <P>
 * Any subclass that overrides an "add" or "set" method defined by this
 * class is expected to indicate an error by throwing an instance of
 * {@link org.bzdev.obnaming.NamedObjectFactory.ConfigException}, and
 * can generate this exception by using methods named
 * newConfigExceptionInstance:
 * <ul>
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,IllegalStateException) newConfigExceptionInstance}(String, IllegalStateException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,UnsupportedOperationException) newConfigExceptionInstance}(String, UnsupportedOperationException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IllegalArgumentException) newConfigExceptionInstance}(String, Object, IllegalArgumentException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IllegalStateException) newConfigExceptionInstance}(String, Object, IllegalStateException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,UnsupportedOperationException) newConfigExceptionInstance}(String, Object, UnsupportedOperationException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IndexOutOfBoundsException) newConfigExceptionInstance}(String, Object, IndexOutOfBoundsException).
 * </ul>
 * For these methods, the String argument is the name of a parameter,
 * the Object method (for the 4 methods that take three arguments) is a key or
 * index, and the Exception methods indicate the exceptions that caused the
 * error.  Object-namer classes that are created using the annotations defined
 * in the package org.bzdev.obnaming.annotations will treat instances of
 * {@link org.bzdev.obnaming.NamedObjectFactory.ConfigException} specially.
 * <P>
 * Finally, all factories that are not abstract should be listed in a file
 * name META-INF/services/org.bzdev.obnaming.NamedObjectFactory that should
 * be included in the same JAR file as the factory to facilitate locating
 * factories and listing them.
 * <P>
 * Note: when used with a security manager, this class needs the
 * runtime permission getClassLoader due to using ResourceBundle.getBundle
 * methods that take a class loader as an argument.
 */
abstract public class NamedObjectFactory<
    F extends NamedObjectFactory<F,NMR,NMD,OBJ>,
    NMR extends ObjectNamerOps<NMD>,
    NMD extends NamedObjectOps,
    OBJ extends NMD>
    implements  Cloneable
 {
    /**
     * Interface for classes that can set the values for multiple
     * indexed factory parameters.  Users will call the method
     * {@link IndexedSetter#setIndexed(NamedObjectFactory,int)} to
     * configure a factory, and use the return value to update an index
     * For example,
     * <BLOCKQUOTE><PRE><CODE>
     *       AnimationLayer2DFactory f = ...;
     *       IndexedSetter isetter = ...;
     *       int index = 0;
     *       try {
     *           ...
     *           index = isetter.setIndexed(f, index);
     *       } catch (ConfigException e) {
     *         ...
     *       }
     * </CODE></PRE></BLOCKQUOTE>
     * <P>
     * This interface is used by some utility programs that can
     * generate Java code (e.g. the EPTS graphics editor).
     */
    public interface IndexedSetter {
	/**
	 * @param f a named object factory
	 * @param start the starting index to use
	 * @return the next index to use after this method returns;
	 * @exception ConfigException an error occurred during a factory
	 *            'set' call.
	 */
	int setIndexed(NamedObjectFactory f, int start) throws ConfigException;
    }

    /**
     * Iterator returned by the factory method parmNames().
     * This class is provided due to type-erasure issues.
     */
    public static class ParmNameIterator extends
	EncapsulatingIterator<String,String> {
	/**
	 * Constructor.
	 * @param it the encapsulated iterator
	 */
	public ParmNameIterator(Iterator<String> it) {
	    super(it);
	}
	public String next() {
	    return encapsulatedNext();
	}
    }

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.obnaming.lpack.NamedObjectFactory");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    /**
     * Configuration exception.
     * This class can encapsulate an instance of
     * {@link java.lang.IllegalArgumentException},
     * {@link java.lang.IllegalStateException},
     * {@link java.lang.UnsupportedOperationException}, or
     * {@link java.lang.IndexOutOfBoundsException}.
     * The constructors are accessible only within the org.bzdev.obnaming
     * package, although instances can be created by several protected methods
     * defined by {@link NamedObjectFactory}:
     * <ul>
     *   <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,IllegalArgumentException) newConfigExceptionInstance}(String, IllegalArgumentException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,IllegalStateException) newConfigExceptionInstance}(String, IllegalStateException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,UnsupportedOperationException) newConfigExceptionInstance}(String, UnsupportedOperationException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IllegalArgumentException) newConfigExceptionInstance}(String, Object, IllegalArgumentException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IllegalStateException) newConfigExceptionInstance}(String, Object, IllegalStateException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,UnsupportedOperationException) newConfigExceptionInstance}(String, Object, UnsupportedOperationException).
     * <li>{@link NamedObjectFactory#newConfigExceptionInstance(String,Object,IndexOutOfBoundsException) newConfigExceptionInstance}(String, Object, IndexOutOfBoundsException).
     * </ul>
     * This exception is thrown by methods named "add" or "set" defined by
     * {@link NamedObjectFactory this class}. Any subclass that overrides
     * an "add" or "set" method must ensure that this exception is thrown.
     */
    public static class ConfigException extends IllegalArgumentException {
	static String createMsg(String name) {
	    return NamedObjectFactory.errorMsg("configException", name);
	}

	ConfigException(String name, IllegalArgumentException e) {
	    super(createMsg(name), e);
	}

	ConfigException(String name, IllegalStateException e) {
	    super(createMsg(name), e);
	}

	ConfigException(String name, UnsupportedOperationException e) {
	    super(createMsg(name), e);
	}

	static String createMsg(String name, Object key) {
	    return
		NamedObjectFactory.errorMsg("configKeyedException", name, key);
	}

	ConfigException(String name, Object key, IllegalArgumentException e) {
	    super(createMsg(name, key), e);
	}

	ConfigException(String name, Object key, IllegalStateException e) {
	    super(createMsg(name, key), e);
	}

	ConfigException(String name, Object key,
			UnsupportedOperationException e)
	{
	    super(createMsg(name, key), e);
	}

	ConfigException(String name, Object key, IndexOutOfBoundsException e) {
	    super(createMsg(name, key), e);
	}
    }

    /**
     * Create a ConfigException without a key for an IllegalArgumentException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any IllegalArgumentException and throws an
     * exception created with this method or the variant that specifies a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, IllegalArgumentException e)
    {
	return new ConfigException(name, e);
    }

    /**
     * Create a ConfigException without a key for an IllegalStateException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any IllegalStateException and throws an
     * exception created with this method or the variant that specifies a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, IllegalStateException e)
    {
	return new ConfigException(name, e);
    }

    /**
     * Create a ConfigException without a key for an
     * UnsupportedOperationException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any UnsupportedOperationException and throws an
     * exception created with this method or the variant that does not specify
     * a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, UnsupportedOperationException e)
    {
	return new ConfigException(name, e);
    }


    /**
     * Create a ConfigException with a key for an IllegalArgumentException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any IllegalArgumentException and throws an
     * exception created with this method or the variant that does not specify
     * a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param key the key or index passed to an 'add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, Object key,
				   IllegalArgumentException e)
    {
	return new ConfigException(name, key, e);
    }

    /**
     * Create a ConfigException with a key for an IllegalStateException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any IllegalStateException and throws an
     * exception created with this method or the variant that does not specify
     * a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param key the key or index passed to an 'add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, Object key,
				   IllegalStateException e)
    {
	return new ConfigException(name, key, e);
    }

    /**
     * Create a ConfigException with a key for an UnsuportedOperationException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any UnsupportedOperationException and throws an
     * exception created with this method or the variant that does not specify
     * a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param key the key or index passed to an 'add" or "set" method
     * @param e the exception
     * @return a new exception
     */
    protected ConfigException
	newConfigExceptionInstance(String name, Object key,
				   UnsupportedOperationException e)
    {
	return new ConfigException(name, key, e);
    }

    /**
     * Create a ConfigException with a key for an IndexOutOfBoundsException.
     * Subclasses that override an "add" or "set" method (which should
     * occur only in atypical cases) are expected to start with a 'try'
     * statement that catches any IndexOutOfBoundsException and throws an
     * exception created with this method or the variant that does not specify
     * a key.
     * @param name the parameter name passed to an "add" or "set" method
     * @param key the key or index passed to an 'add" or "set" method
     * @param e the exception
     * @return the new exception
     */
    static protected ConfigException
	newConfigExceptionInstance(String name, Object key,
				   IndexOutOfBoundsException e)
    {
	return new ConfigException(name, key, e);
    }

    // temporary, until we get everything working.
    // static final public  boolean rvmode = true;

    Map<String,Parm> parmMap = new HashMap<String,Parm>();

    /**
     * Determine if a parameter name exists.
     * @param name the name of the parameter
     * @return true if the parameter name exists; false otherwise
     */
    public boolean containsParm(String name) {
	return parmMap.containsKey(name);
    }

    NMR namer = null;
    private Class<NMD> namedObjectClass;

    /**
     * Get the factory's object namer.
     * @return the object namer
     */
    public NMR getObjectNamer() {
	return namer;
    }

    /**
     * Constructor.
     * Subclasses must call this constructor.
     * If the object namer is null, the factory will not be able to
     * create objects, but can be queried to get parameters, etc.
     * @param namer the object namer
     */
    protected NamedObjectFactory (NMR namer) {
	this.namer = namer;
	namedObjectClass = (namer == null)? null: namer.getNamedObjectClass();
    }

    private static Set<String> factoryNames = null;
    private static LinkedList<NamedObjectFactory> factories = null;
    private static Map<String, URL> codebaseMap = new HashMap<String,URL>();
    private static Map<String,Class<? extends NamedObjectFactory>>
	factoryMap = new HashMap<String,Class<? extends NamedObjectFactory>>();
    private static final String SERVICE =
	"META-INF/services/org.bzdev.obnaming.NamedObjectFactory";

    private static boolean identicalFiles(URL url1, URL url2) {
	try {
	    URI uri1 = url1.toURI();
	    URI uri2 = url2.toURI();
	    File f1 = new File(uri1);
	    File f2 = new File(uri2);
	    String p1 = f1.getCanonicalPath();
	    String p2= f2.getCanonicalPath();
	    return p1.equals(p2);
	} catch(Exception e) {
	    return false;
	}
    }

    private static synchronized void loadFactoriesAux() throws Exception {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
		    public Void run() throws Exception {
			factoryNames = new TreeSet<String>();
			factories = null;
			codebaseMap.clear();
			ServiceLoader<NamedObjectFactory>loader =
			    ServiceLoader.load(NamedObjectFactory.class);
			for (ServiceLoader.Provider<NamedObjectFactory> p:
				 loader.stream().collect(Collectors.toList())) {
			    Class<? extends NamedObjectFactory>
				clazz = p.type();
			    String cname = clazz.getName();
			    ProtectionDomain pd = clazz.getProtectionDomain();
			    CodeSource src = (pd == null)? null:
				pd.getCodeSource();
			    URL url = (src == null)? null: src.getLocation();
			    if (factoryMap.containsKey(cname)) {
				Class<? extends NamedObjectFactory>
				    existingClass = factoryMap.get(cname);
				if (!clazz.equals(existingClass)) {
				    // We have two different classes listed
				    // with the same name.
				    System.err.println
					("Warning: "
					 + cname
					 + " listed in multiple"
					 + " class-path entries ...");
				    System.err.println
					("    ... "
					 + codebaseMap.get(cname));
				    System.err.println("    ... "
						       + url.toString());

				}
			    } else {
				codebaseMap.put(cname, url);
				factoryMap.put(cname, clazz);
				factoryNames.add(cname);
			    }
			}
			/*
			codebaseMap.clear();
			Enumeration<URL> serviceClasses;
			serviceClasses =
			    ClassLoader.getSystemResources(SERVICE);
			while (serviceClasses.hasMoreElements()) {
			    URL url = serviceClasses.nextElement();
			    InputStream is = url.openStream();
			    InputStreamReader r =
				new InputStreamReader(is, "UTF-8");
			    BufferedReader reader = new BufferedReader(r);
			    String path = url.getPath();
			    String urlString;
			    if (url.getProtocol().equals("jar")) {
				urlString = path.substring(0,
							   path.indexOf("!/"));
			    } else {
				urlString = url.toString();
				urlString = urlString.substring
				    (0, urlString.lastIndexOf(SERVICE));
			    }
			    url = new URL(urlString);
			    if (url.getProtocol().equals("file")) {
				// resolve symbolic links, etc., to get a
				// unique URL: we don't want a warning if
				// to URLs actually refer to the same file.
				File f = new File(url.toURI());
				url = f.getCanonicalFile().toURI().toURL();
			    }
			    String cname;
			    while ((cname = reader.readLine()) != null) {
				cname = cname.trim();
				URL oldURL = codebaseMap.get(cname);
				if (oldURL == null) {
				    codebaseMap.put(cname, url);
				} else if (!url.equals(oldURL) &&
					   !identicalFiles(url, oldURL)) {
				    System.err.println
					("Warning: "
					 + cname
					 + " listed in multiple"
					 + " class-path entries ...");
				    System.err.println
					("    ... "
					 + codebaseMap.get(cname));
				    System.err.println("    ... "
						       + url.toString());
				}
				factoryNames.add(cname);
			    }
			}
			*/
			return null;
		    }
		});
	}


    private static NamedObjectFactory newInstanceAux(ObjectNamerOps namer,
						     String className,
						     Class<?> clazz)
	throws IllegalArgumentException
    {
	try {
	    if (!NamedObjectFactory.class.isAssignableFrom(clazz)) {
		throw new IllegalArgumentException
		    (errorMsg("notsubclass", className));
		// (className + " is not a subclass of NamedObjectFactory");
	    }
	    Object obj = null;
	    Constructor<?>[] constructors = clazz.getConstructors();
	    for (Constructor<?> constructor: constructors) {
		Class<?>[] fp = constructor.getParameterTypes();
		if (fp.length != 1) continue;
		if (ObjectNamerOps.class.isAssignableFrom(fp[0])) {
		    Object object = constructor.newInstance(namer);
		    NamedObjectFactory factory =
			NamedObjectFactory.class.cast(object);
		    return factory;
		}
	    }
	    throw new NoSuchMethodException
		(errorMsg("constructorNotFound", className));
		// ("constructor not found");
	} catch (IllegalAccessException e1) {
	    // need outer quotes for a test
	    String msg = (errorMsg("classNotAccessible", className));
	    throw new Error(msg, e1);
		// (className + " not accessible", e1);
	} catch (InstantiationException e2) {
	    // need outer quotes for a test
	    String msg = (errorMsg("classNotAccessible", className));
	    throw new Error(msg, e2);
		// (className + " not instantiable", e2);
	} catch (NoSuchMethodException e3) {
	    // need outer quotes for a test
	    String msg = (errorMsg("noReqConstructor", className));
	    throw new Error(msg, e3);
		// (className + " does not have the required constructor", e3);
	} catch (InvocationTargetException e4) {
	    // need outer quotes for a test
	    String msg = (errorMsg("classNotInstantiated", className));
	    throw new Error(msg, e4);
		// (className + " could not be instantiated", e4);
	}
    }

    /**
     * Create a new instance of a NamedObjectFactory given a factory class name.
     * The factory must have a single-argument constructor that takes
     * its object namer as its argument.
     * @param namer the object namer for the factory
     * @param className the fully-qualified class name of a factory.
     * @return the new named object factory
     * @exception IllegalArgumentException the factory is not listed in
     *            a META-INF/services/org.bzdev.NamedObjectFactory resource
     *            or the class name does not refer to subclass of
     *            NamedObjectFactory
     */
    static public NamedObjectFactory newInstance(ObjectNamerOps namer,
						 String className)
	throws IllegalArgumentException
    {
	if (factoryNames == null) {
	    try {
		loadFactoriesAux();
	    } catch (Exception e) {
		String msg =
		    errorMsg("failed1", "NamedObjectFactory.loadFactoriesAux");
		throw new Error(msg, e);
	    }
	}
	if (codebaseMap.get(className) == null)
	    throw new IllegalArgumentException
		(errorMsg("newInstanceNotListed", className));
		// (className + " not a listed" + " factory");

	try {
	    ClassLoader scl = AccessController.doPrivileged
		(new PrivilegedAction<ClassLoader>() {
		    public ClassLoader run() {
			return ClassLoader.getSystemClassLoader();
		    }
		});
	    Class<?> clazz = scl.loadClass(className);
	    return newInstanceAux(namer, className, clazz);
	} catch (ClassNotFoundException e1) {
	    throw new Error(errorMsg("newInstanceNotAvail", className));
		// (className + " is not available", e1);
	}
    }

    /**
     * Create a new instance of a NamedObjectFactory given a factory class.
     * The factory must have a single-argument constructor that takes
     * its object namer as its argument.
     * @param namer the object namer for the factory
     * @param clazz the factory's class
     * @return the new named object factory
     * @exception IllegalArgumentException the factory is not listed in
     *            a META-INF/services/org.bzdev.NamedObjectFactory resource
     *            or the class name does not refer to subclass of
     *            NamedObjectFactory
     */
    static public NamedObjectFactory newInstance(ObjectNamerOps namer,
						 Class clazz)
	throws IllegalArgumentException
    {
	String className = clazz.getName();
	if (factoryNames == null) {
	    try {
		loadFactoriesAux();
	    } catch (Exception e) {
		String msg =
		    errorMsg("failed1", "NamedObjectFactory.loadFactoriesAux");
		throw new Error(msg, e);
	    }
	}
	if (codebaseMap.get(className) == null)
	    throw new IllegalArgumentException(className + " not a listed"
					       + " factory");
	return newInstanceAux(namer, className, clazz);
    }

    private static synchronized void loadFactories() {
	if (factoryNames == null) {
	    try {
		loadFactoriesAux();
	    } catch (Exception e) {
		String msg =
		    errorMsg("failed1", "NamedObjectFactory.loadFactoriesAux");
		throw new Error(msg, e);
	    }
	}
	if (factories != null) return;
	factories = new LinkedList<NamedObjectFactory>();

	for (String cname: factoryNames) {
	    NamedObjectFactory factory = null;
	    Class<? extends NamedObjectFactory> clazz = factoryMap.get(cname);
	    if (clazz == null) {
		continue;
	    }
	    try {
		Constructor<?>[] constructors =
		    clazz.getConstructors();
		for (Constructor<?> constructor: constructors) {
		    Class<?>[] fp =
			constructor.getParameterTypes();
		    if (fp.length != 1) continue;
		    if (ObjectNamerOps.class
			    .isAssignableFrom(fp[0])) {
			    Object[] args = new Object[1];
			    Object obj = constructor.newInstance(args);
			    factory = NamedObjectFactory.class
				.cast(obj);
			    factories.add(factory);
			    break;
		    }
		}
	    } catch (Throwable e) {
		System.err.println("Factory loader could not "
				   + "process " + cname + ": listed in "
				   + codebaseMap.get(cname));
		System.err.println("   ... " + e.getClass().getName()
				   + ": " +  e.getMessage());
		while ((e = e.getCause()) != null) {
		    System.err.println("    ... caused by "
				       + e.getClass().getName()
				       + ": " +  e.getMessage());
		}
		// e.printStackTrace();
	    }
	}

	/*
	ClassLoader scl = AccessController.doPrivileged
	    (new PrivilegedAction<ClassLoader>() {
		public ClassLoader run() {
		    return ClassLoader.getSystemClassLoader();
		}
	    });
	for (String cname: factoryNames) {
	    NamedObjectFactory factory = null;
	    try {
		final Class<?> clazz = scl.loadClass(cname);
		URL location = AccessController.doPrivileged
		    (new PrivilegedExceptionAction<URL>() {
			public URL run() throws Exception {
			    return
			    clazz.getProtectionDomain().getCodeSource()
			    .getLocation();
			}
		    });
		Object obj = null;
		if (NamedObjectFactory.class
		    .isAssignableFrom(clazz)) {
		    Constructor<?>[] constructors =
			clazz.getConstructors();
		    for (Constructor<?> constructor: constructors) {
			Class<?>[] fp =
			    constructor.getParameterTypes();
			if (fp.length > 1) continue;
			if (fp.length == 0) {
			    // obj = clazz.newInstance();
			    obj = clazz.getDeclaredConstructor().newInstance();
			    factory = NamedObjectFactory.class
				.cast(obj);
			    factories.add(factory);
			    break;
			} else {
			    if (ObjectNamerOps.class
				.isAssignableFrom(fp[0])) {
				Object[] args = new Object[1];
				obj = constructor.newInstance(args);
				factory = NamedObjectFactory.class
				    .cast(obj);
				factories.add(factory);
				break;
			    }
			}
		    }
		}
	    } catch (Throwable e) {
		System.err.println("Factory loader could not "
				   + "process " + cname + ": listed in "
				   + codebaseMap.get(cname));
		System.err.println("... " + e.getClass().getName()
				   + ": " +  e.getMessage());
		e.printStackTrace();
	    }
	}
	*/
    }


    /**
     * Get a key map for listing the parameters for all factories specified
     * as factory-listing service providers.
     * These factories must not be abstract classes, and must appear, one
     * per line, in a file named
     * <blockquote>
     * META-INF/services/org.bzdev.obnaming.NamedObjectFactory
     * </blockquote>
     * The service provided is the ability to list factory parameters.
     * <P>
     * The key map contains a single entry named "factories" whose value
     * is a list, each element of which is a key map returned by calling
     * {@link #getTemplateKeyMap() getTemplateKeyMap()} for a given factory.
     * @return the key map
     * @see #getTemplateKeyMap()
     */
    public static TemplateProcessor.KeyMap getTemplateKeyMapForFactories() {
	TemplateProcessor.KeyMap keymap =
	    new TemplateProcessor.KeyMap(64);
	TemplateProcessor.KeyMapList keymaplist =
	    new TemplateProcessor.KeyMapList();
	synchronized (NamedObjectFactory.class) {
	    if (factories == null) {
		loadFactories();
	    }
	}
	for (NamedObjectFactory factory: factories) {
	    keymaplist.add(factory.getTemplateKeyMap());
	}
	keymap.put("factories", keymaplist);
	return keymap;
    }


    /**
     * Get a key map for listing the parameters for all factories specified
     * as factory-listing service providers, restricted to those factories
     * whose fully-qualified class names match a pattern.
     * The pattern is a regular expression in which a '*' indicates an
     * arbitrary number of characters that do not include a period '.',
     * where '|" indicates alternatives, and where a subexpression bracketed
     * by '(' and ')' indicates grouping.  Thus, the pattern
     * <blockquote>
     *       org.bzdev.(anim2d|drama).*
     * </blockquote>
     * will match all listed factories in the packages org.bzdev.anim2d and
     * org.bzdev.drama.
     * These factories must not be abstract classes, and must appear, one
     * per line, in a file named
     * <blockquote>
     * META-INF/services/org.bzdev.obnaming.NamedObjectFactory
     * </blockquote>
     * and each entry in this file must be a subclass of NamedObjectFactory.
     * The service provided is the ability to list factory parameters.
     * <P>
     * The key map contains a single entry named "factories" whose value
     * is a list, each element of which is a key map returned by calling
     * {@link #getTemplateKeyMap() getTemplateKeyMap()} for a given factory.
     * @param pattern the search pattern
     * @return the key map
     * @see #getTemplateKeyMap()
     */
    public static TemplateProcessor.KeyMap
	getTemplateKeyMapForFactories(String pattern)
    {
	if (pattern == null) return new TemplateProcessor.KeyMap();
	pattern = pattern.trim();
	if (pattern.length() == 0) return new TemplateProcessor.KeyMap();
	// pattern = Pattern.quote(pattern);
	// use our own quote in case the JRE implementation changes.
	pattern = "\\Q" + pattern + "\\E";
	pattern = pattern.replaceAll("\\Q*\\E",
				     "\\\\E[^.]*\\\\Q")
	    .replaceAll("\\Q(\\E", "\\\\E(\\\\Q")
	    .replaceAll("\\Q)\\E", "\\\\E)\\\\Q")
	    .replaceAll("\\Q|\\E", "\\\\E|\\\\Q")
	    .replaceAll("\\\\Q\\\\E","");
	Pattern p = Pattern.compile(pattern);
	TemplateProcessor.KeyMap keymap =
	    new TemplateProcessor.KeyMap(64);
	TemplateProcessor.KeyMapList keymaplist =
	    new TemplateProcessor.KeyMapList();
	synchronized (NamedObjectFactory.class) {
	    if (factories == null) {
		loadFactories();
	    }
	}
	for (NamedObjectFactory factory: factories) {
	    Matcher matcher = p.matcher(factory.getClass().getName());
	    if (matcher.matches()) {
		keymaplist.add(factory.getTemplateKeyMap());
	    }
	}
	keymap.put("factories", keymaplist);
	return keymap;
    }
    
    /**
     * Get a template key map for a set of factories.
     * @param factories the set of factories
     * @return the key map
     * @see #getTemplateKeyMap()
     */
    public static TemplateProcessor.KeyMap
	getTemplateKeyMapForFactories(Set<NamedObjectFactory> factories)
    {
	TemplateProcessor.KeyMap keymap =
	    new TemplateProcessor.KeyMap(64);
	TemplateProcessor.KeyMapList keymaplist =
	    new TemplateProcessor.KeyMapList();
	for (NamedObjectFactory factory: factories) {
		keymaplist.add(factory.getTemplateKeyMap());
	}
	keymap.put("factories", keymaplist);
	return keymap;
    }

    /**
     * Get a set of factories that are factory-listing service
     * providers, restricted to those factories whose fully-qualified
     * class names match a pattern.
     * The pattern is a regular expression in which a '*' indicates an
     * arbitrary number of characters that do not include a period
     * '.', where '|" indicates alternatives, and where a
     * subexpression bracketed by '(' and ')' indicates grouping.
     * As a special case, '**' indicates any sequence of characters other
     * than new lines (which never appear in a factory name), and must
     * end the pattern when used.
     * Thus, the pattern
     * <blockquote>
     *       org.bzdev.(anim2d|drama).*
     * </blockquote>
     * will match all listed factories in the packages org.bzdev.anim2d and
     * org.bzdev.drama.* Similarly,
     * <blockquote>
     *       org.bzdev.**
     * </blockquote>
     * will match all factories whose fully qualified names start with
     * "org.bzdev."
     * These factories must not be abstract classes, and must appear, one
     * per line, in a file named
     * <blockquote>
     * META-INF/services/org.bzdev.obnaming.NamedObjectFactory
     * </blockquote>
     * and each entry in this file must be a subclass of NamedObjectFactory.
     * The service provided is the ability to list factory parameters.
     * <P>
     * The key map contains a single entry named "factories" whose value
     * is a list, each element of which is a key map returned by calling
     * {@link #getTemplateKeyMap() getTemplateKeyMap()} for a given factory.
     * @param pattern the search pattern
     * @return a set of factories.
     * @see #getTemplateKeyMap()
     */
    public static Set<NamedObjectFactory> getListedFactories(String pattern)
    {
	Set<NamedObjectFactory> result =
	    new LinkedHashSet<NamedObjectFactory>();
	if (pattern == null) return result;
	pattern = pattern.trim();
	if (pattern.length() == 0) return result;
	// pattern = Pattern.quote(pattern);
	// use our own quote in case the JRE implementation changes.
	boolean wildcard = false;
	if (pattern.endsWith("**")) {
	    wildcard = true;
	    pattern = pattern.substring(0, pattern.lastIndexOf("**"));
	}
	pattern = "\\Q" + pattern + "\\E";
	pattern = pattern.replaceAll("\\Q*\\E",
				     "\\\\E[^.]*\\\\Q")
	    .replaceAll("\\Q(\\E", "\\\\E(\\\\Q")
	    .replaceAll("\\Q)\\E", "\\\\E)\\\\Q")
	    .replaceAll("\\Q|\\E", "\\\\E|\\\\Q")
	    .replaceAll("\\\\Q\\\\E","");
	if (wildcard) {
	    pattern = pattern + ".*";
	}
	Pattern p = Pattern.compile(pattern);
	synchronized (NamedObjectFactory.class) {
	    if (factories == null) {
		loadFactories();
	    }
	}
	for (NamedObjectFactory factory: factories) {
	    Matcher matcher = p.matcher(factory.getClass().getName());
	    if (matcher.matches()) {
		result.add(factory);
	    }
	}
	return result;
    }


    static boolean acceptsParmBounds(Class<?> clasz) {
	if (clasz.equals(int.class)) return true;
	if (clasz.equals(long.class)) return true;
	if (clasz.equals(double.class)) return true;
	if (clasz.equals(short.class)) return true;
	if (clasz.equals(char.class)) return true;
	if (Number.class.isAssignableFrom(clasz)) return true;
	if (IntegerRandomVariable.class.isAssignableFrom(clasz)) return true;
	if (LongRandomVariable.class.isAssignableFrom(clasz)) return true;
	if (DoubleRandomVariable.class.isAssignableFrom(clasz)) return true;
	return false;
    }

    private static Map<String,URL> jdocMap1  = new HashMap<>();
    private static Map<String,String> jdocMap2  = new HashMap<>();


    /**
     * Indicate that a URL refers to an Javadoc API directory.
     * The tables modified by this method are used by the method
     * {@link org.bzdev.obnaming.NamedObjectFactory#getTemplateKeyMap()}.

     * @param apiURL the URL
     * @exception IOException the operation failed.
     */
    public static void addJDoc(final URL apiURL) throws IOException {
	addJDoc(apiURL, apiURL);
    }
    /**
     * Indicate that a URL refers to an Javadoc API directory.
     * The argument <CODE>offlineURL</CODE> is used to read the
     * the element-list or package-list files in an API directory,
     * and is provided for cases where the URL specified by
     * <CODE>apiURL</CODE> either does not yet exist or is not currently
     * accessible.
     * The tables modified by this method are used by the method
     * {@link org.bzdev.obnaming.NamedObjectFactory#getTemplateKeyMap()}.
     * @param apiURL the URL for the Javadoc API directory
     * @param offlineURL the URL for an off-line copy of the Javadoc
     *        API directory
     * @exception IOException the operation failed.
     */
     public static void addJDoc(final URL apiURL,
				final URL offlineURL)
	 throws IOException
     {
	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
			public Void run() throws IOException {
			    URL elements = new URL(offlineURL, "element-list");
			    IOException exception1 = null;
			    IOException exception2 = null;
			    try {
				InputStream is = elements.openStream();
				is = new BufferedInputStream(is);
				LineNumberReader rd = new LineNumberReader
				    (new InputStreamReader(is, "UTF-8"));
				String cmod = null;
				String line;
				while ((line = rd.readLine()) != null) {
				    line = line.trim();
				    if (line.startsWith("module:")) {
					cmod = line.substring(7);
					continue;
				    } else {
					jdocMap1.put(line, apiURL);
					if (cmod != null
					    && cmod.length() != 0) {
					    jdocMap2.put(line, cmod);
					}
				    }
				}
				return (Void)null;
			    } catch (IOException e) {
				exception1 = e;
			    }
			    URL packages = new URL(offlineURL, "packageList");
			    try {
				InputStream is = packages.openStream();
				is = new BufferedInputStream(is);
				LineNumberReader rd = new LineNumberReader
				    (new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = rd.readLine()) != null) {
				    jdocMap1.put(line.trim(), apiURL);
				}
				return (Void)null;
			    } catch (IOException e) {
				exception2 = e;
			    }
			    if (exception1 != null && exception2 != null) {
				String msg =
				    errorMsg("notAPIDir",offlineURL.toString());
				throw new IOException(msg);
			    }
			    return (Void)null;
			}
		    });
	} catch (Throwable e) {
	    throw new IOException(e.getCause());
	}
    }

    private static String baseURL = null;

    /**
     * Set the base URL.
     * The base URL is the common portion of a URL shared by multiple
     * sets of API documentation.  Typically, this will be used when
     * documenation for multiple APIs all have a common parent directory.
     * When possible, relative URLs will be used, simplyfing deployment
     * on multiple servers.
     * The value set by this method is used by the method
     * {@link org.bzdev.obnaming.NamedObjectFactory#getTemplateKeyMap()}.
     * @param url the base URL
     */
    public static void setDocAPIBase(URL url) {
	baseURL = (url == null)? null: url.toString();
    }


    private static String target = null;

    /**
     * Set the target to use in an HTML &lt;A&gt; element.
     * This field is used when a link should be displayed in a
     * specified frame.
     * The value set by this method is used by the method
     * {@link org.bzdev.obnaming.NamedObjectFactory#getTemplateKeyMap()}.
     * @param htmlTarget the target; null if there is none.
     */
    public static void setTarget(String htmlTarget) {
	if (htmlTarget == null) target = null;
	target = WebEncoder.htmlEncode(htmlTarget);
    }

    // find the URL for a Javadoc page for some class.
    private String findJDocURL(final String name) {
	return AccessController.doPrivileged(new PrivilegedAction<String>() {
		public  String run() {
		    try {
			Class<?> clasz = null;
			String n = name;
			for (;;) {
			    try {
				clasz = ClassLoader.getSystemClassLoader()
				    .loadClass(n);
				break;
			    } catch (ClassNotFoundException e) {
				int ind = n.lastIndexOf('.');
				if (ind != -1) {
				    n = n.substring(0, ind)
					+ "$" + n.substring(ind+1);
				    continue;
				} else {
				    return null;
				}
			    }
			}
			Package p = clasz.getPackage();
			String pname = (p == null)? null:  p.getName();
			if (pname != null) {
			    URL url = jdocMap1.get(pname);
			    if (url != null) {
				String urlString = url.toString();
				if (!urlString.endsWith("/")) {
				    urlString = urlString + "/";
				}
				String mod = jdocMap2.get(pname);
				if (mod != null) {
				    urlString = urlString + mod + "/";
				}
				String cname = name.substring(pname.length()+1);
				pname = pname.replaceAll("[.]", "/");
				urlString = urlString + pname + "/"
				    + cname + ".html";
				return urlString;
			    } else {
				return (String)null;
			    }
			}
			return (String) null;
		    } catch (Exception e) {
			return null;
		    }
		}
	    });
    }

    private String findOurJDocBase() {
	final String name = getClass().getName();
	return AccessController.doPrivileged(new PrivilegedAction<String>() {
		public String run() {
		    try {
			Class<?> clasz = null;
			String n = name;
			for (;;) {
			    try {
				clasz = ClassLoader.getSystemClassLoader()
				    .loadClass(n);
				break;
			    } catch (ClassNotFoundException e) {
				int ind = n.lastIndexOf('.');
				if (ind != -1) {
				    n = n.substring(0, ind)
					+ "$" + n.substring(ind+1);
				    continue;
				} else {
				    return null;
				}
			    }
			}
			Package p = clasz.getPackage();
			String pname = (p == null)? null:  p.getName();
			if (pname != null) {
			    URL url = jdocMap1.get(pname);
			    if (url != null) {
				return url.toString();
			    }
			    return (String) null;
			}
			return (String)null;
		    } catch (Exception e) {
			return null;
		    }
		}
	    });
    }

    private String findOurFDocBase() {
	final String name = getClass().getName();
	String result = findOurJDocBase();
	if (result == null) return null;
	if (!result.endsWith("/")) {
	    result = result + "/";
	}
	result = result + "factories-api/";
	return result;
    }

    // fromFactory indicates that the a relative path starts at the
    // factory documentation. When true, it starts at the top-level
    // directory for factory documentation.
    //
    // If fdoc is true, the path is relative to the directory
    // for factory documentation. When false, it is relative to the java
    // documentation.  The BZDev class library is typically configured
    // so that the top-level directory for factory documentation is
    // named "factories" and is located in the api directory for the
    // corresponding javadoc documentation for the BZDev library.
    //
    // Note: only called with the combinations
    //       fromFactory     fdoc
    //       true            false  (used to reference Java API documentation)
    //       false           true   (used only for menus)
    //
    private String makeRelativeIfPossible(String url, boolean fromFactory,
					  boolean fdoc) {
	String ourURLBase = fdoc? findOurFDocBase(): findOurJDocBase();
	if (ourURLBase == null || url == null) {
	    return url;
	}
	if (fromFactory) { 
	    String ourURL = findFDocURL(getClass().getName());
	    if (ourURL == null) {
		return url;
	    }
	    if (ourURL.startsWith(ourURLBase) && url.startsWith(ourURLBase)) {
		int len = ourURLBase.length();
		String prefix = ourURL.substring(len);
		prefix = prefix.replaceAll("[^/]+/", "../");
		int ind = prefix.lastIndexOf('/');
		prefix = prefix.substring(0, ind+1);
		if (!prefix.endsWith("/")) prefix = prefix + "/";
		return prefix + url.substring(len);
	    } else if (baseURL != null
		       && ourURL.startsWith(baseURL)
		       && url.startsWith(baseURL)) {
		int len = baseURL.length();
		String prefix = ourURL.substring(len);
		prefix = prefix.replaceAll("[^/]+/", "../");
		int ind = prefix.lastIndexOf('/');
		prefix = prefix.substring(0, ind+1);
		if (!prefix.endsWith("/")) prefix = prefix + "/";
		return prefix + url.substring(len);
	    }
	} else {
	    if (url.startsWith(ourURLBase)) {
		return url.substring(ourURLBase.length());
	    }
	}
	return url;
    }

    // find the URL for a factory's documentation
    private String findFDocURL(final String name) {
	return AccessController.doPrivileged(new PrivilegedAction<String>() {
		public String run() {
		    try {
			Class<?> clasz = null;
			String n = name;
			for (;;) {
			    try {
				clasz = ClassLoader.getSystemClassLoader()
				    .loadClass(n);
				break;
			    } catch (ClassNotFoundException e) {
				int ind = n.lastIndexOf('.');
				if (ind != -1) {
				    n = n.substring(0, ind)
					+ "$" + n.substring(ind+1);
				    continue;
				} else {
				    return null;
				}
			    }
			}
			Package p = clasz.getPackage();
			String pname = (p == null)? null:  p.getName();
			if (pname != null) {
			    URL url = jdocMap1.get(pname);
			    if (url != null) {
				String urlString = url.toString();
				if (!urlString.endsWith("/")) {
				    urlString = urlString + "/";
				}
				urlString = urlString + "factories-api/";
				String cname = name.substring(pname.length()+1);
				pname = pname.replaceAll("[.]", "/");
				urlString = urlString + pname + "/"
				    + cname + ".html";
				return urlString;
			    } else {
				return (String)null;
			    }
			} else {
			    return (String) null;
			}
		    } catch (Exception e) {
			return null;
		    }
		}
	    });
    }

    private String setupDocLinks(String docParmName, String type, String doc) {
	if (doc.length() == 0) return doc;
	ACMatcher matcher = new ACMatcher("<code>",   // case 0
					  "</code>",  // case 1
					  "<jdoc>",   // case 2
					  "</jdoc>"); // case 3
	int cdepth = 0;
	int adepth = 0;
	ArrayList<Integer> alist = new ArrayList(1 + doc.length()/6);
	String lcdoc = doc.toLowerCase(Locale.ENGLISH);
	for (MatchResult mr: matcher.iterableOver(lcdoc)) {
	    alist.add(mr.getStart());
	    switch(mr.getIndex()) {
	    case 0:
		cdepth++;
		if (adepth > 0) {
		    String cname = getClass().getName();
		    String dpn = docParmName;
		    throw new IllegalArgumentException
			(errorMsg("nesting", "code", dpn, cname , type));
		}
		break;
	    case 1:
		cdepth--;
		if (cdepth < 0) {
		    String cname = getClass().getName();
		    String dpn = docParmName;
		    throw new IllegalArgumentException
			(errorMsg("unbalancedHTML", "code", dpn, cname , type));
		}
		if (adepth > 0) {
		    String cname = getClass().getName();
		    String dpn = docParmName;
		    throw new IllegalArgumentException
			(errorMsg("overlapping", dpn, cname , type));
		}
		break;
	    case 2:
		if (adepth > 0) {
		    String cname = getClass().getName();
		    String dpn = docParmName;
		    throw new IllegalArgumentException
			(errorMsg("nesting", "jdoc", dpn, cname , type));
		}
		adepth++;
		break;
	    case 3:
		adepth--;
		if (adepth < 0) {
		    String cname = getClass().getName();
		    String dpn = docParmName;
		    throw new IllegalArgumentException
			(errorMsg("unbalancedHTML", "jdoc", dpn, cname , type));
		}
		break;
	    }
	}
	if (cdepth != 0) {
	    String cname = getClass().getName();
	    String dpn = docParmName;
	    throw new IllegalArgumentException
		(errorMsg("unbalancedHTML", "code", dpn, cname , type));
	}
	if (adepth != 0) {
	    String cname = getClass().getName();
	    String dpn = docParmName;
	    throw new IllegalArgumentException
		(errorMsg("unbalancedHTML", "jdoc", dpn, cname , type));
	}
	int alistLen = alist.size();
	int[] codeTags = new int[alistLen];
	for (int i = 0; i < alistLen; i++) {
	    codeTags[i] = alist.get(i);
	}
	char[] sequence = lcdoc.toCharArray();
	/*
	SuffixArray.String sa =
	    new SuffixArray.String(doc.toLowerCase(Locale.ENGLISH), 128, true);
	int[] codeStarts = sa.findRange("<code>").toArray();
	int[] codeEnds = sa.findRange("</code>").toArray();
	if (codeStarts.length != codeEnds.length) {
	    String cname = getClass().getName();
	    throw new IllegalArgumentException
		(errorMsg("unbalancedHTML", "code", docParmName, cname , type));
	}
	Arrays.sort(codeStarts);
	Arrays.sort(codeEnds);
	for (int i = 0; i < codeStarts.length; i++) {
	    if (codeStarts[i] >= codeEnds[i]) {
		String cname = getClass().getName();
		String dpn = docParmName;
		new IllegalArgumentException
		    (errorMsg("unbalancedHTML","code", dpn, cname, type));
	    }
	}
	int[] jdocStarts = sa.findRange("<jdoc>").toArray();
	int[] jdocEnds = sa.findRange("</jdoc>").toArray();
	if (jdocStarts.length != jdocEnds.length) {
	    String cname = getClass().getName();
	    throw new IllegalArgumentException
		(errorMsg("unbalancedHTML", "jdoc", docParmName, cname, type));
	}
	Arrays.sort(jdocStarts);
	Arrays.sort(jdocEnds);
	for (int i = 0; i < jdocStarts.length; i++) {
	    if (jdocStarts[i] >= jdocEnds[i]) {
		String cname = getClass().getName();
		String dpn = docParmName;
		new IllegalArgumentException
		    (errorMsg("unbalancedHTML", "jdoc", dpn , cname, type));
	    }
	}
	*/
	StringBuilder sb = new StringBuilder(2*doc.length());
	sb.append(doc);

	/*
	int[] codeTags = ArrayMerger.concat(codeStarts, codeEnds,
					    jdocStarts, jdocEnds);
	Arrays.sort(codeTags);
	char[] sequence = sa.getSequenceArray();
	*/
	for (int i = codeTags.length-2; i > -1; i--) {
	    int tag = codeTags[i];
	    int ntag = codeTags[i+1];
	    Set<String> pns = parmNameSet();
	    if (sequence[tag+1] == 'c'
		&& sequence[ntag+1] == '/'
		&& sequence[ntag+2] == 'c') {
		// found <CODE>.../<CODE> with no intervening CODE tags.
		String name = sb.substring(tag+6, ntag);
		if (pns.contains(name)) {
		    sb.insert(ntag, "</A>");
		    String anchorTag = "<A HREF=\"#" + name
			+ "-" + getClass().getName() + "\">";
		    sb.insert(tag+6, anchorTag);
		}
	    }
	    if (sequence[tag+1] == 'j'
		&& sequence[ntag+1] == '/'
		&& sequence[ntag+2] == 'j') {
		String name = sb.substring(tag+6, ntag).trim();
		int index = name.indexOf('#');
		String text;
		String jdocClassName;
		String fragment;
		if (index == -1) {
		    text = name;
		    jdocClassName = name;
		    fragment = null;
		} else {
		    jdocClassName = name.substring(0, index);
		    text = name.substring(index+1);
		    fragment = text;
		}
		String url = findJDocURL(jdocClassName);
		url = makeRelativeIfPossible(url, true, false);
		if (url != null) {
		    try {
			fragment = (fragment == null)? "":
			    URLEncoder.encode(fragment, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
		    }
		    sb.replace(tag, ntag+7,
			       "<CODE><A href=\"" + url
			       + ((fragment == null)? "":
				  "#" + fragment)
			       + "\">"
			       + text + "</A></CODE>");
		} else {
		    sb.replace(tag, ntag+7,
			       "<CODE>" + text + "</CODE>");
		}
	    }
	}
	return sb.toString();
    }

    /**
     * Get a template-processor key map for a factory's parameters.
     * The keymap contains the following entries:
     * <ul>
     *   <li> <code>factory</code> - the fully qualified class name of
     *        the factory.
     *   <li> <code>factoryAPI</code> - the fully qualified class name of
     *        the factory wrapped by an &lt;A&gt; element that points to
     *        the Javadoc API documentation for the factory. The URL will
     *        begin with a base URL configured by the method
     *        {@link NamedObjectFactory#addJDoc(java.net.URL)}.
     *        or
     *        {@link NamedObjectFactory#addJDoc(java.net.URL,java.net.URL)}.
     *   <li> <code>factoryDoc</code> the fully qualified class name of
     *        the factory wrapped by an &lt;A&gt; element that points to
     *        the parameter documentation for the factory.
     *   <li> <code>parameters</code> - a list of keymaps for the parameters
     *        used by the factory, each element of which contains the
     *        following:
     *        <ul>
     *           <li> <code>factory</code> - the fully qualified
     *                class name of a factory.
     *           <li> <CODE>factoryPackage</CODE> - the name of the package
     *                 in which the factory is defined.
     *           <li> <CODE>nextPackageEntry</CODE> - this element is added
     *                by code in the lsnof command and is not available
     *                elsewhere. It provides a list entry giving the package
     *                name when that has changed from the previous entry.
     *           <li> <code>factoryAPI</code> - the HTML expression for
     *                a link to the API documentation (usually created
     *                by javadoc) for a factory. The fully-qualified
     *                class name will be displayed.  The &lt;A&gt;
     *                element generated may include a target
     *                attribute.
     *           <LI> <CODE>factoryDoc</code> - the HTML expression for
     *                a link to the factory documentation (usually
     *                created by lsnof). From a base url, the value
     *                will be the fully qualified class name for the
     *                factory with each '.' separating a package from
     *                the next component replaced with a '/' and with
     *                ".html" appended at the end.
     *           <li> <code>name</code> - the name of the parameter.
     *           <li> <code>label</code> - the parameter's label.
     *           <li> <code>definingFactoryClass</code> - the factory class
     *                in which the parameter is defined.
     *           <li> <code>type</code> - the type of the parameters (the
     *                string "(none)" if there is none)
     *           <li> <code>typeHTML</code> - the type of the parameters (the
     *                string "(none)" if there is none). When a URL to javadoc
     *                documentation for the class exists a link to that
     *                documentation will be provided by wrapping the name
     *                in an &lt;A&gt; element. This link refers to a location
     *                in the same document in which the link appears.
     *           <li> <code>rvmode</code> - the random-variable mode.
     *           <li> <code>keytype</code> - the type of a key (the
     *                string "(none)" if there is none).
     *           <li> <code>keytypeHTML</code> - the type of a key (the
     *                string "(none)" if there is none). When a URL to javadoc
     *                documentation for the class exists a link to that
     *                documentation will be provided by wrapping the name
     *                in an &lt;A&gt; element.
     *           <li> <code>range</code> - the range of values that the
     *                parameter will accept (an empty string if there is
     *                none).
     *           <li> <code>description</code> - a tool-tip description of
     *                the parameter.
     *           <li> <code>descriptionHTML</code> - a tool-tip description
     *                of the parameter in HTML.
     *           <li> <code>hasDoc</code> - a map that, when present, indicates
     *                 that a doc entry exists.
     *           <li> <code>noDoc</code> - a map that, when present, indicates
     *                 that a doc entry does not exist.
     *           <li> <code>doc</code> - the value provided by a doc resource
     *                bundle for the parameter. This must be an HTML fragment
     *                that can be placed between <CODE>&lt;DIV&gt;</CODE> and
     *                <CODE>&lt;/DIV&gt;</CODE>.
     *        </ul>
     *        In both the <code>description</code> and
     *        <code>descriptionHTML</code> cases, a starting
     *        <CODE>&lt;html&gt;</CODE> and ending
     *        <CODE>&lt;/html&gt;</CODE> indicate that the text is
     *        formatted using HTML.  In this case, any
     *        <CODE>&lt;br&gt;</CODE> directives will be replaced with
     *        whitespace (a newline followed by two tabs). For HTML,
     *        the description will replace <CODE>&lt;JDOC&gt;</CODE>
     *        with <CODE>&lt;CODE&gt;</CODE> and
     *        <CODE>&lt;/JDOC&gt;</CODE> with
     *        <CODE>&lt;/CODE&gt;</CODE>. This is also true for the
     *        descriptionHTML case, but in that case the content of
     *        the JDOC element, which should be a fully-qualified
     *        class name optionally followed by a crosshatch ("#") and
     *        a fragment, will be changed into a link suitable to
     *        Javadoc documentation. The class name and fragment must
     *        follow the same conventions used by javadoc.
     * </ul>
     * @return the template-processor key map for this factory
     */
    public TemplateProcessor.KeyMap getTemplateKeyMap() {
	TemplateProcessor.KeyMap keymap =
	    new TemplateProcessor.KeyMap();
	String factoryName = getClass().getCanonicalName();
	String factorySimpleName = factoryName;
	Package pkg = getClass().getPackage();
	if (pkg != null) {
	    String prefix = pkg.getName();
	    prefix = prefix + ".";
	    // name excludes only packages
	    factorySimpleName = factorySimpleName.substring(prefix.length());
	}
	keymap.put("factory", factoryName);
	String factoryURL = findJDocURL(factoryName);
	factoryURL = makeRelativeIfPossible(factoryURL, true, false);

	if (factoryURL != null && factoryURL.length() > 0) {
	    String factoryAPI = "<A HREF=\"" + factoryURL + "\">"
		+ factoryName + "</A>";
	    keymap.put("factoryAPI", factoryAPI);
	} else {
	    keymap.put("factoryAPI", factoryName);
	}
	factoryURL = findFDocURL(factoryName);
	factoryURL = makeRelativeIfPossible(factoryURL, false, true);
	if (factoryURL != null && factoryURL.length() > 0) {
	    String factoryDoc = "<A HREF=\"" + factoryURL + "\""
		+(target == null? "": " target=\"" + target + "\"")
		+ ">"
		+ factorySimpleName + "</A>";
	    keymap.put("factoryDoc", factoryDoc);
	    if (pkg != null) keymap.put("factoryPackage", pkg.getName());
	} else {
	    keymap.put("factoryDoc", factoryName);
	}
	TemplateProcessor.KeyMapList keymaplist =
	    new TemplateProcessor.KeyMapList();
	for (String name: parmNameSet()) {
	    TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	    map.put("name", name);
	    String label;
	    try {
		label = getLabel(name);
		int length = label.length();
		if (length > 12) {
		    String start =
			label.substring(0,6).toLowerCase(Locale.ENGLISH);
		    String end =
			label.substring(length-7).toLowerCase(Locale.ENGLISH);
		    if (start.equals("<html>") && end.equals("</html>")) {
			label = label.substring(6, label.length()-7);
			label = label.replaceAll("<[bB][rR]>","\n\t\t");
		    } else {
			label = WebEncoder.htmlEncode(label);
		    }
		} else {
		    label = WebEncoder.htmlEncode(label);
		}
	    } catch(MissingResourceException e) {
		label = "";
	    }
	    map.put("label", label);
	    try {
		String doc = getDoc(name);
		doc = setupDocLinks(name, "doc", doc);
		if (doc.length() > 0) {
		    TemplateProcessor.KeyMap docmap =
			new TemplateProcessor.KeyMap();
		    docmap.put("doc", doc);
		    map.put("hasDoc", docmap);
		} else {
		    map.put("noDoc", new TemplateProcessor.KeyMap());
		}
	    } catch (MissingResourceException e) {
		map.put("noDoc", new TemplateProcessor.KeyMap());
	    }
	    Class<?> clasz = getFactoryClass(name);
	    if (clasz != null) {
		String cname = clasz.getName().replace('$','.');
		map.put("definingFactoryClass", cname);
	    }
	    clasz = getType(name);
	    Class<?> keyClass = keyType(name);
	    boolean acceptsBounds = false;
	    boolean isAddable = mustAdd(name);
	    boolean clearOnly = isClearOnly(name);
	    if (clearOnly) {
		String msg = errorMsg("clear");
		map.put("isAddable", msg);
	    } else {
		if (isAddable) {
		    String msg = errorMsg("add");
		    map.put("isAddable", msg);
		} else {
		    String msg = errorMsg("set");
		    map.put("isAddable", msg);
		}
	    }
	    if (clasz != null) {
		String cname = clasz.getName().replace('$','.');
		map.put("type", cname);
		String cnameURL = findJDocURL(cname);
		cnameURL = makeRelativeIfPossible(cnameURL, true, false);
		if (cnameURL != null) {
		    String typeHTML = "<A HREF=\"" + cnameURL + "\">"
			+ cname + "</A>";
		    map.put("typeHTML", typeHTML);
		} else {
		    map.put("typeHTML", cname);
		}
		acceptsBounds = acceptsParmBounds(clasz);
	    } else {
		if (keyClass == null) {
		    String msg = errorMsg("none");
		    map.put("type", msg );
		    map.put("typeHTML",
			    WebEncoder.htmlEncode(errorMsg("none")));
		}
	    }
	    if (getRVMode(name)) {
		String msg = errorMsg("true");
		map.put("rvmode", msg);
	    } else {
		String msg =errorMsg("false");
		map.put("rvmode", msg);
	    }
	    if (keyClass != null) {
		ParmKeyType qn = getParmKeyType(name);
		if (qn != null) {
		    if (isAddable && qn.classArray.length < 3) {
			switch (qn.classArray.length) {
			case 0:
			    {
				String unknown = errorMsg("unknown");
				map.put("type", unknown);
				map.put("keytype", unknown);
				String unknownHTML =
				    WebEncoder.htmlEncode(unknown);
				map.put("typeHTML", unknownHTML);
				map.put("keytypeHTML", unknownHTML);
			    }
			    break;
			case 1:
			    {
				String cname = qn.classArray[0]
				    .getCanonicalName();
				map.put("type", cname);
				String cnameURL = findJDocURL(cname);
				cnameURL = makeRelativeIfPossible(cnameURL,
								  true, false);
				if (cnameURL != null) {
				    String typeHTML = "<A HREF=\""
					+ cnameURL + "\">"
					+ cname + "</A>";
				    map.put("typeHTML", typeHTML);
				} else {
				    map.put("typeHTML", cname);
				}
				map.put("types",
					(String)map.get("typeHTML"));
			    }
			    break;
			case 2:
			    {
				String cname = qn.classArray[0]
				    .getCanonicalName();
				map.put("keytype", cname);
				String cnameURL = findJDocURL(cname);
				cnameURL = makeRelativeIfPossible(cnameURL,
								  true, false);
				if (cnameURL != null) {
				    String keytypeHTML = "<A HREF=\""
					+ cnameURL + "\">"
					+ cname + "</A>";
				    map.put("keytypeHTML", keytypeHTML);
				} else {
				    map.put("keytypeHTML", cname);
				}
				cname = qn.classArray[1]
				    .getCanonicalName();
				map.put("type", cname);
				cnameURL = findJDocURL(cname);
				cnameURL = makeRelativeIfPossible(cnameURL,
								  true, false);
				if (cnameURL != null) {
				    String typeHTML = "<A HREF=\""
					+ cnameURL + "\">"
					+ cname + "</A>";
				    map.put("typeHTML", typeHTML);
				} else {
				    map.put("typeHTML", cname);
				}
				map.put("types", (String)map.get("keytypeHTML")
					+ "<BR>"
					+ (String)map.get("typeHTML"));
			    }
			    break;
			default:
			    break;
			}
		    } else {
			String string;
			if (qn == null) {
			    string = errorMsg("unknown");
			} else {
			    string = qn.description();
			}
			map.put("keytype", string);
			map.put("keytypeHTML", WebEncoder.htmlEncode(string));
		    }
		} else {
		    String cname = keyClass.getName().replace('$','.');
		    map.put("keytype", cname);
		    String cnameURL = findJDocURL(cname);
		    cnameURL = makeRelativeIfPossible(cnameURL, true, false);
		    if (cnameURL != null) {
			String keytypeHTML = "<A HREF=\"" + cnameURL + "\">"
			    + cname + "</A>";
			map.put("keytypeHTML", keytypeHTML);
		    } else {
			map.put("keytypeHTML", cname);
		    }
		    if (clasz != null) {
			map.put("types", (String)map.get("keytypeHTML")
				+ "<BR>"
				+ (String)map.get("typeHTML"));
		    } else {
			map.put("types", (String)map.get("keytypeHTML"));
		    }
		}
	    } else {
		map.put("types", map.get("typeHTML"));
	    }
	    Number min = getGLB(name);
	    boolean minInRange = glbInRange(name);
	    Number max = getLUB(name);
	    boolean maxInRange = lubInRange(name);
	    if (min != null || max != null) {
		String range = minInRange? "[": "(";
		if (min == null) {
		    range = range + "-\u221e, ";
		} else {
		    range = range + min.toString() +", ";
		}
		if (max == null) {
		    range = range + "\u221e";
		} else {
		    range = range + max.toString();
		}
		range = range + (maxInRange? "]": ")");
		map.put("range", range);
	    } else if (acceptsBounds) {
		String delim1 = minInRange? "[": "(";
		String delim2 = maxInRange? "]": ")";
		map.put("range", delim1 + "-\u221e, \u221e" + delim2);
	    } else {
		String msg = errorMsg("NA");
		map.put("range", msg);
	    }
	    String tip = getTip(name);
	    String htmlTip = null;
	    if (tip != null) {
		int length = tip.length();
		if (length > 12) {
		    if (tip.regionMatches(true, 0, "<html>", 0, 6)
			&& tip.regionMatches(true, tip.length()-7, "</html>",
					     0, 7)) {
			tip = tip.substring(6, tip.length()-7);
			tip = tip.replaceAll("<[bB][rR]>","\n\t\t");
			htmlTip = setupDocLinks(name, "tip", tip);
			tip = tip.replaceAll("<[Jj][Dd][Oo][Cc]>", "");
			tip = tip.replaceAll("</[Jj][Dd][Oo][Cc]>", "");
		    } else {
			htmlTip = WebEncoder.htmlEncode(tip);
		    }
		} else {
		    htmlTip = WebEncoder.htmlEncode(tip);
		}
	    }
	    map.put("description", tip);
	    map.put("descriptionHTML", htmlTip);
	    keymaplist.add(map);
	}

	keymap.put("parameters", keymaplist);
	return keymap;
    }

    Map<String,String> labels = new HashMap<>();
    Map<String,String> tips = new HashMap<>();
    Map<String,String> docs = new HashMap<>();
    Map<String,String> prefixMap = new HashMap<>();
    Set<String> prefixSet = new TreeSet<>();

    /**
     * Return a set of prefixes used in compound parameters.
     * @return an unmodifiable set of prefixes
     */
    public Set<String> parmPrefixes() {
	return Collections.unmodifiableSet(prefixSet);
    }

    /**
     * Get a parameter prefix of a compound parameter given the parameter name.
     * @param parmName the name of the parameter
     * @return the parameter's prefix; null if it is not a compound parameter.
     */
    public String getParmPrefix(String parmName) {
	return prefixMap.get(parmName);
    }

    // LinkedList<Properties> defaults = new LinkedList<Properties>();

    private String getFullyQualifiedBaseName(String baseName, Class clazz)
	throws NullPointerException, MissingResourceException
    {
	if (baseName == null)
	    throw new NullPointerException(errorMsg("baseNameNull"));
	    // throw new NullPointerException("baseName was null");
	baseName = baseName.trim();
	if (baseName.length() == 0) {
	    String msg = errorMsg("baseNameEmpty");
	    throw new MissingResourceException(msg, null, null);
	    /*
	    throw new MissingResourceException("baseName not provided",
					       null, null);
	    */
	}
	if (baseName.startsWith("*.")) {
	    baseName = baseName.substring(1);
	    if (baseName.length() == 1) {
		String msg = errorMsg("baseNameEmpty");
		throw new MissingResourceException(msg, null, null);
	    }
	    Package p = clazz.getPackage();
	    if (p == null) {
		baseName = baseName.substring(1);
		if (baseName.contains(".")) {
		    throw new SecurityException
			(errorMsg("baseNameResource", baseName));
		    /*
		    throw new
			SecurityException("Resource " + baseName
					  + " in a named package when defining "
					  + " factory is not");
		    */
		}
		return baseName;
	    } else {
		String pname = p.getName();
		if (pname == null || pname.length() == 0) {
		    baseName = baseName.substring(1);
		    if (baseName.contains(".") &&
			!baseName.startsWith("lpack.")) {
			throw new SecurityException
			    (errorMsg("baseNameWrongPackage", baseName));
			/*
			throw new
			    SecurityException("Resource " + baseName
					  + " in the wrong package");
			*/
		    }
		    return baseName;
		} else {
		    String substring = baseName.substring(1);
		    if (substring.contains(".") &&
			!substring.startsWith("lpack.")) {
			throw new SecurityException
			    (errorMsg("baseNameWrongPackage", substring));
			/*
			throw new
			    SecurityException("Resource " + substring
					  + " in the wrong package");
			*/
		    }
		    return pname + baseName;
		}
	    }
	}
	if (baseName.contains(".")) {
	    Package pc = clazz.getPackage();
	    String pcString = (pc == null)? "": (pc.getName() + ".");
	    if (baseName.startsWith(pcString)) {
		int len = pcString.length();
		int len2 = baseName.length();
		if (len >= len2) {
		    throw new SecurityException
			(errorMsg("baseNameNotInPackage", baseName, pc));
		    /*
		    throw new
			SecurityException("Resource " + baseName
					  + " not in " + pc);
		    */
		}
		String substring = baseName.substring(len);
		if (substring.contains(".") &&
		    !substring.startsWith("lpack.")) {
		    throw new SecurityException
			(errorMsg("baseNameNotInPackage", baseName, pc));
		    /*
		    throw new
			SecurityException("Resource " + baseName
					  + " not in " + pc);
		    */
		}
	    }
	    return baseName;
	} else {
	    Package p = clazz.getPackage();
	    if (p == null) {
		return baseName;
	    } else {
		String pname = p.getName();
		if (pname == null || pname.length() == 0) return baseName;
		return pname + "." + baseName;
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void checkResourceBundleClass(String baseName, Class clazz) {

	if (NamedObjectFactory.class.isAssignableFrom(clazz)) return;
	if (ParmManager.class.isAssignableFrom(clazz)) return;
	if (clazz.isAnnotationPresent(CompoundParmType.class)) {
	    return;
	}
	Package pc = clazz.getPackage();
	if (pc == null) {
	    throw new SecurityException(errorMsg("checkRBClass1", baseName));
	} else {
	    throw new SecurityException
		(errorMsg("checkRBClass2", baseName, pc.toString()));
	}
	/*
	throw new SecurityException("Resource " + baseName + " not in "
				    +((pc == null)? "unnamed package":
				      (pc.toString() + " or a subpackage")));
	*/
    }

    /**
     * Add a resource bundle for labels associated with
     * configuration parameters.
     * The second argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource or property file in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name of clazz, followed by a period,
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package or a subpackage
     *        of clazz's package named "lpack".
     *   <li> an asterisk followed by a period, followed by a
     *        qualified or simple class name.  In this case the
     *        asterisk is replaced with clazz's package name, which
     *        must not be the unnamed package. The resource bundle
     *        must be in the same package as clazz or in a subpackage
     *        named lpack.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addLabelResourceBundle(String baseName, final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    for (String keyName: rb.keySet()) {
		labels.put(keyName, rb.getString(keyName));
	    }
	}

    /**
     * Add a resource bundle for labels associated with
     * keyed configuration parameters.
     * The fourth argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource or property file in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name of clazz, followed by a period,
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package or a subpackage
     *        of clazz's package named "lpack".
     *   <li> an asterisk followed by a period, followed by a
     *        qualified or simple class name.  In this case the
     *        asterisk is replaced with clazz's package name, which
     *        must not be the unnamed package. The resource bundle
     *        must be in the same package as clazz or in a subpackage
     *        named lpack.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addLabelResourceBundle(String keyPrefix,
					  String delimiter,
					  String baseName,
					  final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    prefixSet.add(keyPrefix);
	    for (String keyName: rb.keySet()) {
		labels.put(keyPrefix + delimiter + keyName,
			   rb.getString(keyName));
		prefixMap.put(keyPrefix + delimiter + keyName, keyPrefix);
	    }
	}

    /**
     * Get a label.
     * This will typically be used by a GUI to provide a label for a
     * text field or other parameter.
     * @param name the name of a parameter
     * @return a string providing the label
     */
    public String getLabel(String name) throws IllegalArgumentException {
	if (!containsParm(name)) {
	    throw new IllegalArgumentException(errorMsg("unknownName", name));
	    // throw new IllegalArgumentException("unknown name");
	}
	String label = labels.get(name);
	if (label != null) return label;
	else return "";

    }

    /**
     * Add a resource bundle for tips associated with configuration entries.
     * The second argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource bundle (or property file) in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name if clazz, followed by a period
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package.
     *   <li> an asterisks followed by a period, followed by a qualified
     *        or simple class name.  In this case, clazz's package name is
     *        prepended to baseName when clazz is in a named package. Otherwise
     *        the leading ".*" is removed from baseName.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addTipResourceBundle(String baseName, final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    for (String keyName: rb.keySet()) {
		tips.put(keyName, rb.getString(keyName));
	    }
	}

    /**
     * Add a resource bundle for tips associated with compound configuration
     * entries.
     * The fourth argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource or property file in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name if clazz, followed by a period
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package.
     *   <li> an asterisks followed by a period, followed by a qualified
     *        or simple class name.  In this case, clazz's package name is
     *        prepended to baseName when clazz is in a named package. Otherwise
     *        the leading ".*" is removed from baseName.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addTipResourceBundle(String keyPrefix,
					String delimiter,
					String baseName,
					final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    prefixSet.add(keyPrefix);
	    for (String keyName: rb.keySet()) {
		tips.put(keyPrefix + delimiter + keyName,
			 rb.getString(keyName));
		prefixMap.put(keyPrefix + delimiter + keyName, keyPrefix);
	    }
	}

    /**
     * Get a tip.
     * This will typically be used by a GUI to provide a tip describing  a
     * text field or other parameter.
     * @param name the name of a parameter
     * @return a string providing the tip.
     */
    public String getTip(String name) throws IllegalArgumentException {
	if (!containsParm(name)) {
	    throw new IllegalArgumentException(errorMsg("unknownName", name));
	    // throw new IllegalArgumentException("unknown name");
	}
	String tip = tips.get(name);
	if (tip != null) return tip;
	else return "";
    }

    /**
     * Add a resource bundle for docs associated with configuration entries.
     * The second argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource bundle (or property file) in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name if clazz, followed by a period
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package.
     *   <li> an asterisks followed by a period, followed by a qualified
     *        or simple class name.  In this case, clazz's package name is
     *        prepended to baseName when clazz is in a named package. Otherwise
     *        the leading ".*" is removed from baseName.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addDocResourceBundle(String baseName, final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    for (String keyName: rb.keySet()) {
		docs.put(keyName, rb.getString(keyName));
	    }
	}

    /**
     * Add a resource bundle for docs associated with compound configuration
     * entries.
     * The fourth argument (clazz) is used to determine the class
     * loader for the resource bundle. It can be any class whose class
     * loader can find the resource bundle, but typically one will use
     * a factory class name (or parm-manager class name), with the corresponding
     * resource or property file in the same directory or jar file as
     * the factory class being loaded or initialized.
     * The baseName argument may be one of the following:
     * <ul>
     *   <li> a simple class name. In this case, if clazz is in a named
     *        package, the package name if clazz, followed by a period
     *        will be prepended to baseName.
     *   <li> a fully qualified class name. In this case, baseName is
     *        used as is, but a run time check will ensure that the package
     *        component of the name matches clazz's package.
     *   <li> an asterisks followed by a period, followed by a qualified
     *        or simple class name.  In this case, clazz's package name is
     *        prepended to baseName when clazz is in a named package. Otherwise
     *        the leading ".*" is removed from baseName.
     * </ul>
     * Regardless of how baseName is specified, if the resource bundle is
     * not in the same package as clazz, it must be in a subpackage whose
     * first component is named "lpack" (for language pack).
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the name of a class representing this resource
     * @param clazz the class of the factory, parm manager, or a class
     *        annotated with CompoundParmType, adding the
     *        resource bundle
     * @exception NullPointerException an argument was null
     * @exception MissingResourceException the first argument did not
     *            refer to a resource that could be found
     * @exception SecurityException the packages for clazz and baseName
     *            differ or clazz's package is not the package of
     *            the factory or one of its superclasses
     */
    protected void addDocResourceBundle(String keyPrefix,
					String delimiter,
					String baseName,
					final Class clazz)
	throws NullPointerException, MissingResourceException
	{
	    final String ourBaseName =
		getFullyQualifiedBaseName(baseName, clazz);
	    checkResourceBundleClass(ourBaseName, clazz);
	    ResourceBundle rb =
		AccessController.doPrivileged
		(new PrivilegedAction<ResourceBundle>() {
		    public ResourceBundle run() {
			return ResourceBundle.getBundle(ourBaseName,
							Locale.getDefault(),
							clazz.getClassLoader());
		    }
		});
	    prefixSet.add(keyPrefix);
	    for (String keyName: rb.keySet()) {
		docs.put(keyPrefix + delimiter + keyName,
			 rb.getString(keyName));
		prefixMap.put(keyPrefix + delimiter + keyName, keyPrefix);
	    }
	}

    /**
     * Get a doc.
     * This will typically be used by a GUI to provide a doc describing  a
     * text field or other parameter.
     * @param name the name of a parameter
     * @return a string providing the doc.
     */
    public String getDoc(String name) throws IllegalArgumentException {
	if (!containsParm(name)) {
	    throw new IllegalArgumentException(errorMsg("unknownName", name));
	    // throw new IllegalArgumentException("unknown name");
	}
	String doc = docs.get(name);
	if (doc != null) return doc;
	else return "";
    }


    private String layoutResource = null;

    /**
     * Set the name of a resource specifying how a GUI should
     * configure this factory.
     * @param resource the name for the resource
     */
    protected void setLayoutResource(String resource) {
	layoutResource = resource;
    }

    /**
     * Set the name of a resource specifying how a GUI should
     * configure this factory given its class. The resource name
     * will be the same as the class name but with each '.' replaced
     * with a '/', and with ".xml" added as an extension.
     * @param clazz the class
     * @param extension a resource extension (excluding the '.' separating
     *        it from the rest of the resource name)
     * @exception NullPointerException an argument was null
     * @exception IllegalArgumentException after trimming leading and trailing
     *            whitespace, the extension was an empty string
     */
    protected void setLayoutResource(Class<?> clazz, String extension) {
	if (extension == null)
	    throw new NullPointerException(errorMsg("layoutNullExt"));
	// NullPointerException("null extension");
	extension = extension.trim();
	if (extension.length() == 0) {
	    throw new IllegalArgumentException
		(errorMsg("layoutBadExt", extension));
	    // throw new IllegalArgumentException("Illegal extension");
	}
	layoutResource = clazz.getName().replace('.', '/') + "."
	    + extension;
    }



    /**
     * Return the name of the resource specifying how a GUI should
     * configure this factory.
     * @return the resource; null if there is none
     */
    public String getLayoutResource() {
	return layoutResource;
    }

    /**
     * Initialize parameters table.
     * A subclass must call this method, typically inside a constructor.
     * The clazz argument will normally be the name of the constructor
     * with ".class" appended to it.
     * @param parms an array of parameters
     * @param clazz the class defining a set of parameters, typically
     *        the name of the class whose constructor calls initParms
     */
    protected void initParms(Parm[] parms, Class<?> clazz) {
	for (Parm i: parms) {
	    i.setFactoryClass(clazz);
	    parmMap.put(i.name, i);
	}
    }

    /**
     * Initialize parameters table by adding a single parameter.
     * A subclass must call this method, typically inside a constructor.
     * The clazz argument will normally be the name of the constructor
     * with ".class" appended to it.
     * @param parm the parameter
     * @param clazz the class defining a set of parameters, typically
     *        the name of the class whose constructor calls initParms
     */
    protected void initParm(Parm parm, Class<?> clazz) {
	parm.setFactoryClass(clazz);
	parmMap.put(parm.name, parm);
    }

    /**
     * Initialize parameters specified by a ParmManager.
     * A subclass must call this method, typically inside a constructor.
     * The clazz argument will normally be the name of the constructor
     * with ".class" appended to it.
     * <P>
     * Note: when a parm manager is created by using the
     * annotation {@link org.bzdev.obnaming.annotations.FactoryParmManager},
     * the generic type parameters will match those of the factory being
     * annotated.  These must have the same type-parameter names as in
     * the factory's class definition, as shown
     * <A href="#pmexample">above</A>: subclasses of ParmManager do not
     * necessarily use the same type parameters that ParmManager uses.
     * @param manager a parameter manager specifying parameters
     * @param clazz the class defining this set of parameters, typically
     *        the name of the class whose constructor calls initParms
     */
    protected void initParms(ParmManager<? extends F> manager, Class<?>clazz) {
	for (Parm i: manager.getParmList()) {
	    i.setFactoryClass(clazz);
	    parmMap.put(i.name, i);
	}
    }

    /**
     * Remove parameters previously named.
     * This method is intended for cases in which the parameter
     * names provided by a superclass should not be used by a subclass.
     * A subclass must call this method, typically inside a constructor.
     * @param parmNames the names of the parameters to remove
     */
    protected void removeParms(String[] parmNames) {
	for (String name: parmNames) {
	    parmMap.remove(name);
	}
    }

    /**
     * Remove a single parameter that was previously named.
     * A subclass must call this method, typically inside a constructor.
     * @param name the names of the parameters to remove
     */
    protected void removeParm(String name) {
	parmMap.remove(name);
    }

    /**
     * Get the names of parameters.
     * @return an enumeration of the allowed parameter names
     */
    public ParmNameIterator parmNames() {
	return new ParmNameIterator(parmMap.keySet().iterator());
    }

    /**
     * Get a set of the names of entries.
     * This method produces a snapshot of the parameter names that
     * existed when the method was called. While creating a new set
     * is not particularly efficient, this method is rarely used. It
     * is intended for finding parameter names in lexical order.
     * @return a set of the allowed parameter names
     */
    public Set<String> parmNameSet() {
	return new TreeSet<String>(parmMap.keySet());
    }

    /**
     * Determine the type of a key, if any.
     * @param name the name of the value
     * @return the class representing the key type; null if there is
     *         no key for this parameter
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public Class<?> keyType(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.keyType;
    }

    Parm getParm(String name) {
	return parmMap.get(name);
    }


    /**
     * Get the class for the factory that defined a parameter name
     * @param name the name of the value
     * @return the class of the factory that defined the parameter; null
     *         if unknown
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public Class<?> getFactoryClass(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.factoryClass;
    }

    /**
     * Get the parameter-key-type data for a parameter.
     * @param name the name of a parameter
     * @return the ParmKeyType object describing the key for this
     *         parameter; null if the key is not a compound key
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public ParmKeyType getParmKeyType(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.parmKeyType;
    }

    /**
     * Determine the type of a parameter's value.
     * @param name the name of the value
     * @return a class denoting the parameter's value's type, which
     *         can be int.class, double.class, long.class,
     *         boolean.class, String.class, the class name of a named
     *         object that the object namer accepts (in which case the
     *         type is really String.class as a corresponding value is
     *         a name of an interned named object), or null if there
     *         is no value
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public Class<?> getType(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.type;
    }

    /**
     * Get the random-variable mode.
     * @param name the name of an entry
     * @return true if a random variable used in a call to <code>set</code>
     *         should be considered to be the value used to initialize an
     *         object; false if that random variable is used to generate the
     *         value used to initialize an object
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public boolean getRVMode(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.rvmode;
    }

    /**
     * Get the GLB (Greatest Lower Bound) for the allowed range of values.
     * @param name the name of an entry
     * @return the greatest lower bound; null if none is defined
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public Number getGLB(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.glb;
    }

    /**
     * Determine if the GLB (Greatest Lower Bound) for the range of allowed
     * values is in that set.
     * @param name the name of an entry
     * @return true if the GLB is in the allowed range; false if not; undefined
     *         if there a GLB was not defined.
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public boolean glbInRange(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.lbClosed;
    }

    /**
     * Get the LUB (Least Upper Bound) for the allowed range of values.
     * @param name the name of an entry
     * @return the greatest lower bound; null if none is defined
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public Number getLUB(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.lub;
    }

    /**
     * Determine if the LUB (Least Upper Bound) for the range of allowed
     * values is in that set.
     * @param name the name of an entry
     * @return true if the GLB is in the allowed range; false if not; undefined
     *         if there a GLB was not defined.
     * @exception IllegalArgumentException there is no entry for the
     *            specified name
     */
    public boolean lubInRange(String name)
	throws IllegalArgumentException
    {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	return parm.ubClosed;
    }

    private void setBounds(Parm parm, IntegerRandomVariable rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Integer)
	    && (parm.lub == null || parm.lub instanceof Integer)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Integer)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Integer)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void setBounds(Parm parm, LongRandomVariable rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Long)
	    && (parm.lub == null || parm.lub instanceof Long)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Long)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Long)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void setBounds(Parm parm, DoubleRandomVariable rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Double)
	    && (parm.lub == null || parm.lub instanceof Double)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Double)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Double)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void setBounds(Parm parm, IntegerRandomVariableRV rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Integer)
	    && (parm.lub == null || parm.lub instanceof Integer)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Integer)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Integer)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void setBounds(Parm parm, LongRandomVariableRV rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Long)
	    && (parm.lub == null || parm.lub instanceof Long)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Long)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Long)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void setBounds(Parm parm, DoubleRandomVariableRV rv)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Double)
	    && (parm.lub == null || parm.lub instanceof Double)) {
	    if (parm.glb != null) {
		rv.tightenMinimum((Double)parm.glb, parm.lbClosed);
	    }
	    if (parm.lub != null) {
		rv.tightenMaximum((Double)parm.lub, parm.ubClosed);
	    }
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }


    private void checkBounds(Parm parm, int value)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Integer)
	    && (parm.lub == null || parm.lub instanceof Integer)) {
	    if (parm.glb != null) {
		int glb = (Integer)parm.glb;
		if (parm.lbClosed? value < glb: value <= glb) {
		    throw new IllegalArgumentException
			(errorMsg("argTooSmall", parm.name));
		}
	    }
	    if (parm.lub != null) {
		int lub = (Integer)parm.lub;
		if (parm.ubClosed? value > lub: value >= lub) {
		    throw new IllegalArgumentException
			(errorMsg("argTooLarge", parm.name));
		}
	    }
	} else if ((parm.glb == null || parm.glb instanceof Long)
		   && (parm.lub == null || parm.lub instanceof Long)) {
	    checkBounds(parm,(long)value);
	} else if ((parm.glb == null || parm.glb instanceof Double)
		   && (parm.lub == null || parm.lub instanceof Double)) {
	    checkBounds(parm,(double)value);
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void checkBounds(Parm parm, long value)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Long)
	    && (parm.lub == null || parm.lub instanceof Long)) {
	    if (parm.glb != null) {
		long glb = (Long)parm.glb;
		if (parm.lbClosed? value < glb: value <= glb) {
		    throw new IllegalArgumentException
			(errorMsg("argTooSmall", parm.name));
		}
	    }
	    if (parm.lub != null) {
		long lub = (Long)parm.lub;
		if (parm.ubClosed? value > lub: value >= lub) {
		    throw new IllegalArgumentException
			(errorMsg("argTooLarge", parm.name));
		}
	    }
	} else if ((parm.glb == null || parm.glb instanceof Integer)
		   && (parm.lub == null || parm.lub instanceof Integer)
		   && value <= (long)Integer.MAX_VALUE
		   && value >= (long)Integer.MIN_VALUE) {
	    checkBounds(parm, (int)value);
	} else if ((parm.glb == null || parm.glb instanceof Double)
		   && (parm.lub == null || parm.lub instanceof Double)) {
	    checkBounds(parm, (double)value);
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    private void checkBounds(Parm parm, double value)
	throws IllegalArgumentException
    {
	if ((parm.glb == null || parm.glb instanceof Double)
	    && (parm.lub == null || parm.lub instanceof Double)) {
	    if (parm.glb != null) {
		double glb = (Double)parm.glb;
		if (parm.lbClosed? value < glb: value <= glb) {
		    throw new IllegalArgumentException
			(errorMsg("argTooSmall", parm.name));
		}
	    }
	    if (parm.lub != null) {
		double lub = (Double)parm.lub;
		if (parm.ubClosed? value > lub: value >= lub) {
		    throw new IllegalArgumentException
			(errorMsg("argTooLarge", parm.name));
		}
	    }
	} else if ((parm.glb == null || parm.glb instanceof Integer)
		   && (parm.lub == null || parm.lub instanceof Integer)) {
	    long val = Math.round(value);
	    if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		throw new IllegalArgumentException
		    (errorMsg("argOverflow", parm.name));
	    }
	    if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
		throw new
		    IllegalArgumentException(errorMsg("argNotInt", parm.name));
	    }
	    checkBounds(parm, (int)val);
	} else if ((parm.glb == null || parm.glb instanceof Long)
		   && (parm.lub == null || parm.lub instanceof Long)) {
	    long val = Math.round(value);
	    if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
		throw new
		    IllegalArgumentException(errorMsg("argNotInt", parm.name));
	    }
	    checkBounds(parm, val);
	} else {
	    throw new IllegalArgumentException
		(errorMsg("badRangeType", parm.name));
	}
    }

    /**
     * Clear an entry and restore it to the default value.
     * @param name the name of the entry
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.clear();
	}

    /**
     * Clear all entries and restore to default values.
     * Note: each subclass that implements this method should
     * call <code>super.clear()</code>.  Any subclass that
     * defines parameters should call this method in order to
     * restore the parameters to their default values.  When
     * an annotation processor is used for some parameters,
     * those parameters can be restored to their default value
     * by calling the parm manager's setDefaults method with
     * the factory as its argument.
     */
    public void clear() {return;}


    /**
     * Add a value provided as a string to as set of strings.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, String value)	throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType != null) {
		if (parm.keyType.equals(ParmKeyType.class)) {
		    if (parm.parmKeyType == null
			|| parm.parmKeyType.addable == false) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		    String[] strings = value.split("\\.");
		    Object[] objects = new Object[strings.length];
		    System.arraycopy(strings, 0, objects, 0,
				     strings.length);
		    check(name, objects, parm.keyType, parm.parmKeyType, false);
		    //  parm.parser.parse(value);
		    parm.parser.parse(objects);
		} else {
		    if (parm.keyType.equals(int.class)
			|| parm.keyType.equals(Integer.class)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			parm.parser.parse(val);
		    } else if (namedObjectClass.isAssignableFrom(parm.keyType)){
			NamedObjectOps obj = namer.getObject(value);
			if (obj == null ||
			    !parm.keyType.isAssignableFrom
			    (obj.getClass())) {
			    throw new IllegalArgumentException
				(errorMsg("notAssignable", name));
			}
			parm.parser.parse(obj);
		    } else if (Enum.class
			       .isAssignableFrom(parm.keyType)) {
			try {
			    java.lang.reflect.Method valueOf =
				parm.keyType.getMethod("valueOf",
						       String.class);
			    Enum<?> obj = (Enum<?>) valueOf.invoke(null,
								   value);
			    parm.parser.parse(obj);
			    return;
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e2) {
			} catch (NoSuchMethodException e3 ){
			}
			throw new UnsupportedOperationException
			    (errorMsg("reflectionFailed", name));
			/* ("use of reflection failed");*/
		    } else if (parm.keyType.equals(String.class)) {
			parm.parser.parse(value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a value provided as a named object to a set of named objects.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, NamedObjectOps value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType != null) {
		if (parm.parmKeyType != null) {
		    if (parm.parmKeyType.addable == false) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    } else if (!(parm.parmKeyType.classArray.length == 1
				 && parm.parmKeyType.classArray[0]
				 .isAssignableFrom(value.getClass()))) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (!parm.keyType
			   .isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a value provided as an enumeration to a set of enumerations.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, Enum<?> value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType != null) {
		if (parm.parmKeyType != null) {
		    if (parm.parmKeyType.addable == false) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    } else if (!(parm.parmKeyType.classArray.length == 1
				 && parm.parmKeyType.classArray[0]
				 .isAssignableFrom(value.getClass()))) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (!parm.keyType
			   .isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a value provided as an int to a set of integers.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, int value)  throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType != null) {
		if (parm.parmKeyType != null) {
		    if (parm.parmKeyType.addable == false) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    } else if (!(parm.parmKeyType.classArray.length == 1
				 && (parm.parmKeyType.classArray[0]
				     .equals(int.class)
				     || parm.parmKeyType.classArray[0]
				     .equals(Integer.class)))) {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (!(parm.keyType.equals(int.class)
			     || parm.keyType.equals(Integer.class))) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a value provided as a long to a set of integers.
     * The long-integer argument will be converted to an int as this
     * method is provided to support polymorphism.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, long value) throws ConfigException {
	try {
	    if (value < (long)Integer.MIN_VALUE
		|| value > (long)Integer.MAX_VALUE) {
		throw new IllegalArgumentException
		    (errorMsg("argOverflow", name));
	    }
	    add(name, (int) value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a value provided as a double to a set of integers.
     * The double-precision value will be converted to an int as this
     * method is provided to support polymorphism.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, double value) throws ConfigException {
	try {
	    long val = Math.round(value);
	    if (val > (long)Integer.MAX_VALUE
		|| val < (long)Integer.MIN_VALUE) {
		throw new IllegalArgumentException
		    (errorMsg("argOverflow", name));
	    }
	    if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
		throw new
		    IllegalArgumentException(errorMsg("argNotInt", name));
	    }
	    add(name, (int)val);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Add a compound key provided as two objects.
     * This is handles a special case - a compound key with two
     * components - and is equivalent to
     * <blockquote><pre>
     *    Object compoundKey[] = {key, subkey};
     *    add(name, compoundKey);
     * </pre></blockquote>
     * @param name the name of the entry
     * @param key the first key
     * @param subkey the second key
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void add(String name, Object key, Object subkey)
	throws ConfigException
    {
	Object objects[] = {key, subkey};
	add(name, objects);
    }


    /**
     * Add a value provided as an array of objects representing a compound key.
     * If the array length is larger than 1, the parameter must use a
     * ParmKeyType to describe its keys.
     * @param name the name of the entry
     * @param value an array containing the keys
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     * @see org.bzdev.obnaming.ParmKeyType
     */
    public void add(String name, Object[] value) throws ConfigException {
	try {
	    if (value == null || value.length == 0) {
		throw new IllegalArgumentException
		    (errorMsg("emptyArray", name));
		/* ("array with at least one entry needed");*/
	    }
	    if (value.length == 1) {
		Object val = value[0];
		if (val == null) {
		    throw new IllegalArgumentException
			(errorMsg("nullKey", name));
		    /*("null value in array argument");*/
		}
		Class<?> valType = val.getClass();
		if (val instanceof NamedObjectOps) {
		    add(name, (NamedObjectOps) val);
		} else if (val instanceof Enum<?>) {
		    add(name, (Enum<?>)val);
		} else if (val instanceof Integer) {
		    add(name, ((Integer)val).intValue());
		} else if (val instanceof Long) {
		    long vl = ((Long)val).longValue();
		    if (vl > ((long)Integer.MAX_VALUE) ||
			vl < ((long)Integer.MIN_VALUE)) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
			/*("value  out or range");*/
		    }
		    add(name, (int)vl);
		} else if (val instanceof String) {
		    add(name, (String) val);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		return;
	    }
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType != null) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == false) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		// also resets elements of value, mapping strings
		// representing named objects or enums into their
		// corresponding values
		check(name, value, parm.keyType, parm.parmKeyType, false);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }


    /**
     * Test if a name must be added rather than set.
     * @param name the name of the entry or property
     * @return true if it must be added; false otherwise
     */
    public boolean mustAdd(String name) {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    return false;
	}
	if (parm.type != null) {
	    return false;
	}
	if (parm.keyType != null) {
	    if (parm.parmKeyType != null
		&& parm.parmKeyType.addable == false) {
		return false;
	    }
	} else {
	    return false;
	}
	return true;
    }


    /**
     * Test if a the three-argument form of add can be used.
     * @param name the name of the entry or property
     * @return true if the three-argument form of add can be used;
     *         false otherwise
     */
    public boolean canAdd3(String name) {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    return false;
	}
	if (!mustAdd(name)) return false;
	if (parm.parmKeyType == null || parm.parmKeyType.addable == false)
	    return false;
	if (parm.parmKeyType.classArray.length == 2) return true;
	else return false;
    }

    /**
     * Determine if a parameter is a clear-only parameter.
     * A clear-only parameter does not support <code>set</code>
     * or <code>add</code> methods, just <code>clear</code> methods.
     * A few such methods are provided in order to allow users to
     * conveniently clear a table.
     * @param name the name of the paraemter
     * @return true if the parameter is clear only; otherwise false
     */
     public boolean isClearOnly(String name) {
	 Parm parm = parmMap.get(name);
	 if (parm == null) {
	     return false;
	 }
	 return parm.isClearOnly();
     }

     private NamedRandomVariableOps<?,?>
	 getNamedRV(String name, Parm parm)
     {
	 if (parm.rvmode || RandomVariable.class.isAssignableFrom(parm.type)) {
	     NamedObjectOps obj = namer.getObject(name);
	     if (obj instanceof NamedRandomVariableOps<?,?>) {
		 return (NamedRandomVariableOps<?,?>)obj;
	     } else {
		 return null;
	     }
	 } else {
	     return null;
	 }
     }

     private NamedRandomVariableOps<?,?>
	 getNamedRV(NamedObjectOps obj, Parm parm)
     {
	 if (parm.rvmode || RandomVariable.class.isAssignableFrom(parm.type)) {
	     if (obj instanceof NamedRandomVariableOps<?,?>) {
		 return (NamedRandomVariableOps<?,?>)obj;
	     } else {
		 return null;
	     }
	 } else {
	     return null;
	 }
     }

     // call only when we detect a named object.
     private void setRV(String name, NamedRandomVariableOps<?,?> nrv) {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, (BooleanRandomVariableRV) rv);
	 }
     }

     private void setRV(String name, int key,
			NamedRandomVariableOps<?,?> nrv)
     {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, key, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, key, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, key, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, key, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, key, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, key, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, key, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, key, (BooleanRandomVariableRV) rv);
	 }
     }


     private void setRV(String name,
			NamedObjectOps key,
			NamedRandomVariableOps<?,?> nrv)
     {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, key, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, key, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, key, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, key, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, key, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, key, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, key, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, key, (BooleanRandomVariableRV) rv);
	 }
     }

     private void setRV(String name, Enum<?> key,
			NamedRandomVariableOps<?,?> nrv)
     {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, key, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, key, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, key, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, key, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, key, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, key, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, key, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, key, (BooleanRandomVariableRV) rv);
	 }
     }

     private void setRV(String name, Object[] key,
			NamedRandomVariableOps<?,?> nrv)
     {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, key, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, key, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, key, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, key, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, key, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, key, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, key, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, key, (BooleanRandomVariableRV) rv);
	 }
     }

     private void setRV(String name, String key,
			NamedRandomVariableOps<?,?> nrv)
     {
	 RandomVariable<?> rv = nrv.getRandomVariable();
	 if (rv instanceof IntegerRandomVariable) {
	     set(name, key, (IntegerRandomVariable) rv);
	 } else if (rv instanceof LongRandomVariable) {
	     set(name, key, (LongRandomVariable) rv);
	 } else if (rv instanceof DoubleRandomVariable) {
	     set(name, key, (DoubleRandomVariable) rv);
	 } else if (rv instanceof BooleanRandomVariable) {
	     set(name, key, (BooleanRandomVariable) rv);
	 } else if (rv instanceof IntegerRandomVariableRV) {
	     set(name, key, (IntegerRandomVariableRV) rv);
	 } else if (rv instanceof LongRandomVariableRV) {
	     set(name, key, (LongRandomVariableRV) rv);
	 } else if (rv instanceof DoubleRandomVariableRV) {
	     set(name, key, (DoubleRandomVariableRV) rv);
	 } else if (rv instanceof BooleanRandomVariableRV) {
	     set(name, key, (BooleanRandomVariableRV) rv);
	 }
     }



    /**
     * Set a value provided as a string.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, String value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.equals(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    IntegerRandomVariableRV rv =
				new FixedIntegerRVRV
				(new FixedIntegerRV(val));
			    parm.parser.parse(rv);
			} else {
			    IntegerRandomVariable rv =
				new FixedIntegerRV(val);
			    parm.parser.parse(rv);
			}
			return;
		    } else if (LongRandomVariable.class.equals(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			if(parm.rvmode) {
			    LongRandomVariableRV rv =
				new FixedLongRVRV(new FixedLongRV(val));
			    parm.parser.parse(rv);
			} else {
			    LongRandomVariable rv = new FixedLongRV(val);
			    parm.parser.parse(rv);
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .equals(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			if (parm.rvmode) {
			    DoubleRandomVariableRV rv =
				new FixedDoubleRVRV(new FixedDoubleRV(x));
			    parm.parser.parse(rv);
			} else {
			    DoubleRandomVariable rv = new FixedDoubleRV(x);
			    parm.parser.parse(rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .equals(parm.type)) {
			if (parm.rvmode) {
			    parm.parser
				.parse(new FixedBooleanRVRV
				       (new
					FixedBooleanRV
					(Boolean.parseBoolean(value))));
			} else {
			    parm.parser
				.parse(new FixedBooleanRV
				       (Boolean.parseBoolean(value)));
			}
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)){
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			IntegerRandomVariable rv =
			    new FixedIntegerRV(Integer.parseInt(value));
			parm.parser.parse(rv);
		    } else {
			parm.parser.parse(val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			LongRandomVariable rv =
			    new FixedLongRV(Long.parseLong(value));
			parm.parser.parse(rv);
		    } else {
			parm.parser.parse(val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			parm.parser.parse(rv);
		    } else {
			parm.parser.parse(x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(new FixedBooleanRV(Boolean.parseBoolean(value)));
		    } else {
			parm.parser.parse(Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null ||
			!parm.type.isAssignableFrom(obj.getClass())) {
			throw new IllegalArgumentException
			    (errorMsg("notAssignable", name));
			/*("" +parm.type +" not assignable from "
			  +((obj == null)? "null": obj.getClass()));*/
		    }
		    parm.parser.parse(obj);
		    return;
		} else if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("reflectionFailed", name));
		    /* ("use of reflection failed"); */
		} else {
		    // we can't convert value to its enumeration constant
		    // even using the reflection API, so we have to let
		    // parm.parser do it using a string argument as the
		    // actual type is known at that point.
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a named object.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, NamedObjectOps value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, nrv);
		    return;
		} else if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an enumeration type.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, Enum<?> value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an int.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, int value) throws ConfigException {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new FixedIntegerRV(value));
		    parm.parser.parse(rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    IntegerRandomVariable rv = new FixedIntegerRV(value);
		    parm.parser.parse(rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new FixedDoubleRV((double)value));
		    parm.parser.parse(rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv = new FixedDoubleRV((double)value);
		    parm.parser.parse(rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse((long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse((double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    IntegerRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a long.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, long value) throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv =
			new FixedLongRVRV(new FixedLongRV(value));
		    parm.parser.parse(rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV(value);
		    parm.parser.parse(rv);
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new
			FixedIntegerRVRV(new FixedIntegerRV((int)value));
		    parm.parser.parse(rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse((double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse((int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an long-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception IllegalArgumentException an argument is out of bounds
     *            or the name does not match a parameter
     * @exception UnsupportedOperationException the factory
     *            does not allow this method to be used
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(value.next().longValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a long-random-variable-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a double.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, double value)
	throws 	ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv =
			new FixedDoubleRVRV(new FixedDoubleRV(value));
		    parm.parser.parse(rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv = new FixedDoubleRV(value);
		    parm.parser.parse(rv);
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)val));
		    parm.parser.parse(rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)val));
		    parm.parser.parse(rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV((long)val);
		    parm.parser.parse(rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse((int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse((long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a double-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom (parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a double-random-variable-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as a boolean.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (boolean.class.equals(parm.type)
		       || Boolean.class.equals(parm.type)) {
		parm.parser.parse(value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an boolean-valued random variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(new FixedBooleanRVRV(value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass
			   .isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Set a value provided as an boolean-random-variable-valued random
     * variable.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void set(String name, BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, e);
	}
    }

    /**
     * Unset an entry with an index and restore it to the default value.
     * @param name the name of the entry
     * @param index the index for the entry
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name, int index)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    parm.parser.clear(index);
	}

    /**
     * Unset an entry with a named-object key and restore it to the
     * default value.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name, NamedObjectOps key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    parm.parser.clear(key);
	}

    /**
     * Unset an entry with an enumeration key and restore it to the
     * default value.
     * @param name the name of the entry
     * @param key the key for the entry
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name, Enum<?> key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    parm.parser.clear(key);
	}

    /**
     * Unset an entry with a compound key and restore it to the
     * default value.
     * @param name the name of the entry
     * @param key the compound key for the entry
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name, Object[] key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    parm.parser.clear(key);
	}

    private void check(String name,
		       Object[] key, Class<?> keyType, ParmKeyType parmKeyType,
		       boolean useClearTo)
	throws IllegalArgumentException
    {

	if (key == null)
	    throw new IllegalArgumentException(errorMsg("checkNullKey", name));
	/*
	if (key == null) throw new
			     IllegalArgumentException("key cannot be null");
	*/
	if (namedObjectClass.isAssignableFrom(keyType)){
	    Object obj = (key.length == 0)? null: key[0];
	    if (obj == null) {
		throw new IllegalArgumentException
		    (errorMsg("checkEmptyKey", name));
		// throw new IllegalArgumentException("key cannot be null");
	    }
	    if (obj instanceof String) {
		obj = namer.getObject((String)obj);
		if (obj == null)
		    throw new IllegalArgumentException
			(errorMsg("checkNamedObj", name));
		key[0] = obj;
	    }
	    if (!keyType.isAssignableFrom(obj.getClass())) {
		throw new IllegalArgumentException
		    (errorMsg("checkNamedObjType", name));
	    }
	} else if (Enum.class.isAssignableFrom(keyType)) {
	    try {
		Object obj = (key.length == 0)? null: key[0];
		if (obj == null) {
		    throw new IllegalArgumentException
			(errorMsg("checkEmptyKey", name));
		    // throw new IllegalArgumentException("key cannot be null");
		} else if (obj instanceof String) {
		    java.lang.reflect.Method valueOf =
			keyType.getMethod("valueOf", String.class);
		    // throws IllegalArgumentException if key is not the
		    // name of the enumeration constant.
		    valueOf.invoke(null, key);
		} else if (!keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("checkNamedObjType", name));
		}
	    } catch (IllegalAccessException e1) {
	    } catch (InvocationTargetException e2) {
	    } catch (NoSuchMethodException e3 ){
	    }
	} else if (keyType.equals(ParmKeyType.class)) {
	    // StringTokenizer st = new StringTokenizer(key, ".");
	    // int len = st.countTokens();
	    int ind = 0;
	    int len = key.length;
	    Class<?>[] classArray;
	    if (useClearTo && len < parmKeyType.classArray.length) {
		classArray = new Class<?>[len];
		System.arraycopy(parmKeyType.classArray, 0, classArray, 0, len);
	    } else {
		classArray = parmKeyType.classArray;
	    }
	    for (Class<?> kt: classArray) {
		if (ind < len) {
		    Object obj = key[ind];
		    if (obj == null) {
			throw new IllegalArgumentException
			    (errorMsg("checkNullKeyInd", ind, name));
		    }
		    if (kt.equals(int.class) || kt.equals(Integer.class)) {
			if (obj instanceof String) {
			    if (!((String)obj).matches("-?\\d+")) {
				throw new IllegalArgumentException
				    (errorMsg("checkIntFormatInd", ind, name));
			    }
			    obj = Integer.valueOf((String)obj);
			    key[ind] = obj;
			} else if (obj instanceof Double) {
			    // in case a scripting language casts it as a double
			    double dval = ((Double)obj).doubleValue();
			    long val = Math.round(dval);
			    if (val > Integer.MAX_VALUE ||
				val < Integer.MIN_VALUE) {
				throw new IllegalArgumentException
				    (errorMsg("argOverflowInd", ind, name));
			    }
			    if (dval != 0.0
				&& Math.abs((dval - val) / dval) > 1.0e-10) {
				throw new
				    IllegalArgumentException
				    (errorMsg("argNotIntInd", ind, name));
			    }
			    obj = Integer.valueOf((int) val);
			    key[ind] = obj;
			} else if (obj instanceof Long) {
			    long val = ((Long)obj).longValue();
			    if (val > Integer.MAX_VALUE
				|| val < Integer.MIN_VALUE) {
				throw new IllegalArgumentException
				    (errorMsg("argOverflowInd", ind, name));
			    }
			    obj = Integer.valueOf((int)val);
			    key[ind] = obj;
			} else if (!(obj instanceof Integer)) {
			    throw new IllegalArgumentException
				(errorMsg("argNotIntInd", ind, name));
			}
		    } else if (kt.equals(String.class)) {
			if (!(obj instanceof String)) {
			    throw new IllegalArgumentException
				(errorMsg("checkIsStringInd", ind, name));
			}
			// return;
		    } else if (namedObjectClass.isAssignableFrom(kt)) {
			if (obj instanceof String) {
			    obj = namer.getObject((String)obj);
			    if (obj == null) {
				throw new IllegalArgumentException
				    (errorMsg("checkNamedObjInd", ind, name));
			    }
			    key[ind] = obj;
			}
			if (!kt.isAssignableFrom(obj.getClass())) {
			    throw new IllegalArgumentException
				(errorMsg("checkNamedObjTypeInd", ind,  name));
			    /*
			    throw new IllegalArgumentException
				(kt.getName() + " is not assignable from "
				 + obj.getClass().getName()
				 + " [ ind = " + ind +"]");
			    */
			}
		    } else if (Enum.class.isAssignableFrom(kt)) {
			if (obj instanceof String) {
			    try {
				java.lang.reflect.Method valueOf =
				    kt.getMethod("valueOf", String.class);
				obj = valueOf.invoke(null, (String)obj);
				key[ind] = obj;
			    } catch (IllegalAccessException e1) {
			    } catch (InvocationTargetException e2) {
			    } catch (NoSuchMethodException e3 ){
			    }
			}
			if (!kt.isAssignableFrom(obj.getClass())) {
			    throw new IllegalArgumentException
				(errorMsg("checkEnumTypeInd", ind,  name));
			}
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("checkEnumTypeInd", ind, name));
		    }
		    ind++;
		} else {
		    throw new IllegalArgumentException
			(errorMsg("checkTooManyKeys", name));
		}
	    }
	    if (ind < len) {
		throw new IllegalArgumentException
		    (errorMsg("checkTooFewKeys", name));
	    }
	}
    }

    /**
     * Set a value provided as a string, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, String value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null) throw new UnsupportedOperationException
				       (errorMsg("unsupported1", name));
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, index, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.isAssignableFrom
			(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    IntegerRandomVariableRV rv = new FixedIntegerRVRV
				(new FixedIntegerRV(val));
			    parm.parser.parse(index, rv);
			} else {
			    IntegerRandomVariable rv =
				new FixedIntegerRV(val);
			    parm.parser.parse(index, rv);
			}
			return;
		    } else if (LongRandomVariable.class.
			       isAssignableFrom(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    parm.parser.parse(index,
					      new FixedLongRVRV
					      (new FixedLongRV(val)));
			} else {
			    parm.parser.parse(index, new FixedLongRV(val));
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			if (parm.rvmode) {
			    parm.parser.parse(index, new FixedDoubleRVRV(rv));
			} else {
			    parm.parser.parse(index, rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			BooleanRandomVariable rv =
			    new FixedBooleanRV(Boolean.parseBoolean(value));
			if (parm.rvmode) {
			    parm.parser.parse(index, new FixedBooleanRVRV(rv));
			} else {
			    parm.parser.parse(index, rv);
			}
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)) {
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(index, new FixedIntegerRV(val));
		    } else {
			parm.parser.parse(index, val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(index, new FixedLongRV(val));
		    } else {
			parm.parser.parse(index, val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			parm.parser.parse(index, new FixedDoubleRV(x));
		    } else {
			parm.parser.parse(index, x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(index,
				   new FixedBooleanRV
				   (Boolean.parseBoolean(value)));
		    } else {
			parm.parser.parse(index, Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null
			|| !parm.type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException
			    (errorMsg("argNotAssignableInd", name, index));
		    }
		    parm.parser.parse(index, obj);
		    return;
		} else if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(index, obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1v", name, value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(index, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as a named object, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, NamedObjectOps value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, index, nrv);
		    return;
		} else if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(index, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an enumeration type, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
      * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
    */
    public void set(String name, int index, Enum<?> value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException(errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(index, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an int, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, int value)
	throws 	ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(index,
				      new FixedIntegerRVRV
				      (new FixedIntegerRV(value)));
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(index, new FixedIntegerRV(value));
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(index, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(index, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new FixedDoubleRV((double)value));
		    parm.parser.parse(index, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv = new FixedDoubleRV((double)value);
		    parm.parser.parse(index, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(index, value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse(index, (long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(index, (double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable,
     * given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
      * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
    */
    public void set(String name, int index, IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index,
				      new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom (parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(index, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		&& parm.rvClass.isAssignableFrom(value.getClass())) {
		if (parm.rvmode) {
		    parm.parser.parse(index, value);
		} else {
		    parm.parser.parse(index, value.next());
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }


    /**
     * Set a value provided as a long, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, long value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(index,
				      new FixedLongRVRV
				      (new FixedLongRV(value)));
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(index, new FixedLongRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new FixedIntegerRV((int)value));
		    parm.parser.parse(index, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)value);
		    parm.parser.parse(index, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new FixedDoubleRV((double)value));
		    parm.parser.parse(index, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv = new FixedDoubleRV((double)value);
		    parm.parser.parse(index, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(index, value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(index, (double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw new IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse(index, (int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an long-valued random variable,
     * given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
    */
    public void set(String name, int index, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index,
				      new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(index, value.next().longValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an long-random-variable-valued random variable,
     * given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
    */
    public void set(String name, int index, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(index, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(index, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }


    /**
     * Set a value provided as a double, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, double value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(index,
				      new FixedDoubleRVRV
				      (new FixedDoubleRV(value)));
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(index, new FixedDoubleRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new FixedIntegerRV((int)val));
		    parm.parser.parse(index, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)val);
		    parm.parser.parse(index, rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV(val));
		    parm.parser.parse(index, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV(val);
		    parm.parser.parse(index, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(index, value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(index, (int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(index, (long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an double-valued random variable,
     * given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(index, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an double-random-variable-valued
     * random variable, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(index, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next() ;
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(index, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as a boolean, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(index,
				      new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(index, new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (boolean.class.equals(parm.type)
		       || Boolean.class.equals(parm.type)) {
		parm.parser.parse(index, value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an boolean-valued random variable,
     * given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index,
				      new FixedBooleanRVRV( value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(index, value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(index, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as an boolean-random-variable-valued random
     * variable, given an index.
     * @param name the name of the entry
     * @param index the index
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     */
    public void set(String name, int index, BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(index, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(index, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, index, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, index, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, index, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, index, e);
	}
    }

    /**
     * Set a value provided as a string, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, String value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.isAssignableFrom
			(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    IntegerRandomVariableRV rv
				= new FixedIntegerRVRV
				(new FixedIntegerRV(val));
			    parm.parser.parse(key, rv);
			} else {
			    IntegerRandomVariable rv =
				new FixedIntegerRV(val);
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (LongRandomVariable.class.
			       isAssignableFrom(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedLongRVRV
					      (new FixedLongRV(val)));
			} else {
			    parm.parser.parse(key, new FixedLongRV(val));
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			if (parm.rvmode) {
			    parm.parser.parse(key, new FixedDoubleRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			BooleanRandomVariable rv =
			    new FixedBooleanRV(Boolean.parseBoolean(value));
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedBooleanRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)) {
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedIntegerRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedLongRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedDoubleRV(x));
		    } else {
			parm.parser.parse(key, x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(key,
				   new FixedBooleanRV
				   (Boolean.parseBoolean(value)));
		    } else {
			parm.parser.parse(key, Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null
			|| !parm.type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException
			    (errorMsg("argNotAssignableInd", key, name));
		    }
		    parm.parser.parse(key, obj);
		    return;
		} else if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(key, obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a named object, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, NamedObjectOps value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) throw new UnsupportedOperationException
					  (errorMsg("unsupported1", name));
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an enumeration type, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, Enum<?> value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an int, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, int value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV
				      (new FixedIntegerRV(value)));
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedIntegerRV(value));
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV
			(new FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(key, value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse(key, (long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable,
     * given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key,
		    IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom (parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key,
		    IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		&& parm.rvClass.isAssignableFrom(value.getClass())) {
		if (parm.rvmode) {
		    parm.parser.parse(key, value);
		} else {
		    parm.parser.parse(key, value.next());
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, long value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedLongRVRV
				      (new FixedLongRV(value)));
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedLongRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)value));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(key, value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse(key, (int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-valued random variable,
     * given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next().longValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-random-variable-valued random variable,
     * given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key,rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }


    /**
     * Set a value provided as a double, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, double value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedDoubleRVRV
				      (new FixedDoubleRV(value)));
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedDoubleRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)val));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)val);
		    parm.parser.parse(key, rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV(val));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV(val);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-valued random variable,
     * given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-random-variable-valued
     * random variable, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key,
		    DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next() ;
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a boolean, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (boolean.class.equals(parm.type)
		       || Boolean.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-valued random variable,
     * given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key,
		    BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV( value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-random-variable-valued random
     * variable, given a named-object key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, NamedObjectOps key,
		    BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a string, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, String value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.isAssignableFrom
			(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    IntegerRandomVariableRV rv =
				new FixedIntegerRVRV
				(new FixedIntegerRV(val));
			    parm.parser.parse(key, rv);
			} else {
			    IntegerRandomVariable rv =
				new FixedIntegerRV(val);
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (LongRandomVariable.class.
			       isAssignableFrom(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedLongRVRV
					      (new FixedLongRV(val)));
			} else {
			    parm.parser.parse(key, new FixedLongRV(val));
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			if (parm.rvmode) {
			    parm.parser.parse(key, new FixedDoubleRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			BooleanRandomVariable rv =
			    new FixedBooleanRV(Boolean.parseBoolean(value));
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedBooleanRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)) {
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedIntegerRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedLongRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedDoubleRV(x));
		    } else {
			parm.parser.parse(key, x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(key,
				   new FixedBooleanRV
				   (Boolean.parseBoolean(value)));
		    } else {
			parm.parser.parse(key, Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null
			|| !parm.type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException
			    (errorMsg("argNotAssignableInd", name, key));
		    }
		    parm.parser.parse(key, obj);
		    return;
		} else if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(key, obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a named object, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, NamedObjectOps value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an enumeration type, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, Enum<?> value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an int, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, int value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV
				      (new FixedIntegerRV(value)));
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedIntegerRV(value));
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(key, value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse(key, (long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable,
     * given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom (parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		&& parm.rvClass.isAssignableFrom(value.getClass())) {
		if (parm.rvmode) {
		    parm.parser.parse(key, value);
		} else {
		    parm.parser.parse(key, value.next());
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, long value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedLongRVRV
				      (new FixedLongRV(value)));
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedLongRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)value));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(key,rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(key, value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw new IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse(key, (int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-valued random variable,
     * given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next().longValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-random-variable-valued random variable,
     * given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key,rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }


    /**
     * Set a value provided as a double, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, double value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedDoubleRVRV
				      (new FixedDoubleRV(value)));
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedDoubleRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)val));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)val);
		    parm.parser.parse(key, rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV(val));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV(val);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-valued random variable,
     * given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-random-variable-valued
     * random variable, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next() ;
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }


    /**
     * Set a value provided as a boolean, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (boolean.class.equals(parm.type)
		       || Boolean.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-valued random variable,
     * given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV( value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-random-variable-valued random
     * variable, given an enumeration key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Enum<?> key, BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a string, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, String value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.isAssignableFrom
			(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    IntegerRandomVariableRV rv =
				new FixedIntegerRVRV
				(new FixedIntegerRV(val));
			    parm.parser.parse(key, rv);
			} else {
			    IntegerRandomVariable rv =
				new FixedIntegerRV(val);
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (LongRandomVariable.class.
			       isAssignableFrom(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedLongRVRV
					      (new FixedLongRV(val)));
			} else {
			    parm.parser.parse(key, new FixedLongRV(val));
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			if (parm.rvmode) {
			    parm.parser.parse(key, new FixedDoubleRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			BooleanRandomVariable rv =
			    new FixedBooleanRV(Boolean.parseBoolean(value));
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedBooleanRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)) {
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedIntegerRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedLongRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedDoubleRV(x));
		    } else {
			parm.parser.parse(key, x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(key,
				   new FixedBooleanRV(Boolean.parseBoolean
						      (value)));
		    } else {
			parm.parser.parse(key, Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null
			|| !parm.type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException
			    (errorMsg("argNotAssignableInd", name, key));
		    }
		    parm.parser.parse(key, obj);
		    return;
		} else if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(key, obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a named object, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, NamedObjectOps value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an enumeration type, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, Enum<?> value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.type != null) {
		if (!parm.type.isAssignableFrom(value.getClass())) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an int, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, int value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV
				      (new FixedIntegerRV(value)));
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedIntegerRV(value));
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new
			FixedDoubleRVRV(new FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(key, value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse(key, (long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable,
     * given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom (parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		&& parm.rvClass.isAssignableFrom(value.getClass())) {
		if (parm.rvmode) {
		    parm.parser.parse(key, value);
		} else {
		    parm.parser.parse(key, value.next());
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, long value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedLongRVRV
				      (new FixedLongRV(value)));
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedLongRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)value));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv = new
			FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(key, value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw
			new IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse(key, (int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-valued random variable,
     * given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next().longValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an long-random-variable-valued random variable,
     * given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key,rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a double, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, double value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedDoubleRVRV
				      (new FixedDoubleRV(value)));
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedDoubleRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)val));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)val);
		    parm.parser.parse(key, rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV(val));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV(val);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-valued random variable,
     * given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an double-random-variable-valued
     * random variable, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next() ;
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a boolean, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (boolean.class.equals(parm.type)
		       || Boolean.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-valued random variable,
     * given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV( value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an boolean-random-variable-valued random
     * variable, given a compound key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, Object[] key, BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null)
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    check(name, key, parm.keyType, parm.parmKeyType, false);
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a string, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, String value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null || parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /* ("" +parm.keyType +" not assignable from "
		       +((obj == null)? "null": obj.getClass())); */
		}
		set(name, obj, value);
		return;
	    }
	    if (!parm.type.equals(String.class)) {
		NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
		if (nrv != null) {
		    setRV(name, key, nrv);
		    return;
		} else if (RandomVariable.class.isAssignableFrom(parm.type)) {
		    if (IntegerRandomVariable.class.isAssignableFrom
			(parm.type)) {
			int val = Integer.parseInt(value);
			checkBounds(parm, val);
			IntegerRandomVariable rv = new FixedIntegerRV(val);
			if (parm.rvmode) {
			    parm.parser.parse(key,
					      new FixedIntegerRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (LongRandomVariable.class.
			       isAssignableFrom(parm.type)) {
			long val = Long.parseLong(value);
			checkBounds(parm, val);
			LongRandomVariable rv = new FixedLongRV(val);
			if (parm.rvmode) {
			    parm.parser.parse(key, new FixedLongRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (DoubleRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			double x;
			if (value.equals("POSITIVE_INFINITY")) {
			    x = Double.POSITIVE_INFINITY;
			} else if (value.equals("NEGATIVE_INFINITY")) {
			    x = Double.NEGATIVE_INFINITY;
			} else {
			    x = Double.parseDouble(value);
			}
			checkBounds(parm, x);
			DoubleRandomVariable rv = new FixedDoubleRV(x);
			if (parm.rvmode) {
			    parm.parser.parse(key, new FixedDoubleRVRV(rv));
			} else {
			    parm.parser.parse(key, rv);
			}
			return;
		    } else if (BooleanRandomVariable.class
			       .isAssignableFrom(parm.type)) {
			parm.parser
			    .parse(key,
				   new FixedBooleanRV(Boolean.parseBoolean
						      (value)));
			return;
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else if (parm.type.equals(int.class)
			   || parm.type.equals(Integer.class)) {
		    int val = Integer.parseInt(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedIntegerRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(long.class)
			   || parm.type.equals(Long.class)) {
		    long val = Long.parseLong(value);
		    checkBounds(parm, val);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedLongRV(val));
		    } else {
			parm.parser.parse(key, val);
		    }
		    return;
		} else if (parm.type.equals(double.class)
			   || parm.type.equals(Double.class)) {
		    double x;
		    if (value.equals("POSITIVE_INFINITY")) {
			x = Double.POSITIVE_INFINITY;
		    } else if (value.equals("NEGATIVE_INFINITY")) {
			x = Double.NEGATIVE_INFINITY;
		    } else {
			x = Double.parseDouble(value);
		    }
		    checkBounds(parm, x);
		    if (parm.rvmode) {
			parm.parser.parse(key, new FixedDoubleRV(x));
		    } else {
			parm.parser.parse(key, x);
		    }
		    return;
		} else if (parm.type.equals(boolean.class)
			   || parm.type.equals(Boolean.class)) {
		    if (parm.rvmode) {
			parm.parser
			    .parse(key,
				   new FixedBooleanRV(Boolean.parseBoolean
						      (value)));
		    } else {
			parm.parser.parse(key, Boolean.parseBoolean(value));
		    }
		    return;
		} else if (namedObjectClass.isAssignableFrom(parm.type)) {
		    NamedObjectOps obj = namer.getObject(value);
		    if (obj == null
			|| !parm.type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException
			    (errorMsg("argNotAssignableInd", name, key));
		    }
		    parm.parser.parse(key, obj);
		    return;
		} else  if (Enum.class.isAssignableFrom(parm.type)){
		    try {
			java.lang.reflect.Method valueOf =
			    parm.type.getMethod("valueOf", String.class);
			Enum<?> obj = (Enum<?>) valueOf.invoke(null, value);
			parm.parser.parse(key, obj);
			return;
		    } catch (IllegalAccessException e1) {
		    } catch (InvocationTargetException e2) {
		    } catch (NoSuchMethodException e3 ){
		    }
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	    parm.parser.parse(key, value);
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a named object, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, NamedObjectOps value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/* ("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    NamedRandomVariableOps<?,?> nrv = getNamedRV(value, parm);
	    if (nrv != null) {
		setRV(name, key, nrv);
		return;
	    } else if (parm.type.isAssignableFrom(value.getClass())) {
		parm.parser.parse(key, value);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("notAssignable", name));
		/*
		  ("type of value argument not assignable to " +
		  parm.type.toString());
		*/
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an enumeration, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, Enum<?> value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    if (parm.type.isAssignableFrom(value.getClass())) {
		parm.parser.parse(key, value);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("notAssignable", name));
		/*
		  ("type of value argument not assignable to " +
		  parm.type.toString());
		*/
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an int, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, int value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    if (parm.type == null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedIntegerRVRV
				      (new FixedIntegerRV(value)));
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedIntegerRV(value));
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV((long)value));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    LongRandomVariable rv = new FixedLongRV((long)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new FixedDoubleRVRV(new
					      FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type) ) {
		parm.parser.parse(key, value);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		parm.parser.parse(key, (long)value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-valued random variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, IntegerRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/* ("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));*/
		}
		set(name, obj, value);
		return;
	    }
	    try {
		value = (IntegerRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedIntegerRVRV(value));
		} else if ((int.class.equals(parm.type)
			    || Integer.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (IntegerRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next().intValue());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as an integer-random-variable-valued random
     * variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, IntegerRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/* ("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		if (parm.rvmode) {
		    value = (IntegerRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (IntegerRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    IntegerRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, long value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedLongRVRV
				      (new FixedLongRV(value)));
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedLongRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariableRV rv
			= new
			FixedIntegerRVRV(new FixedIntegerRV((int)value));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    if (value < (long)Integer.MIN_VALUE
			|| value > (long)Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    IntegerRandomVariable rv =
			new FixedIntegerRV((int)value);
		    parm.parser.parse(key, rv);
		} else if (DoubleRandomVariable.class.equals(parm.type)) {
		    DoubleRandomVariableRV rv
			= new
			FixedDoubleRVRV(new FixedDoubleRV((double)value));
		    parm.parser.parse(key, rv);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    DoubleRandomVariable rv =
			new FixedDoubleRV((double)value);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type))  {
		parm.parser.parse(key, value);
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, (double)value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		if (value < (long)Integer.MIN_VALUE
		    || value > (long)Integer.MAX_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		} else {
		    parm.parser.parse(key, (int)value);
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long-valued random variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, LongRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		value = (LongRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedLongRVRV(value));
		} else if ((long.class.equals(parm.type)
			    || Long.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (LongRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a long-random-variable-valued random
     * variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, LongRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		if (parm.rvmode) {
		    value = (LongRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (LongRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    LongRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a double, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, double value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    checkBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedDoubleRVRV
				      (new FixedDoubleRV(value)));
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedDoubleRV(value));
		} else if (IntegerRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariableRV rv
			= new FixedIntegerRVRV(new
					       FixedIntegerRV((int)val));
		    parm.parser.parse(key, rv);
		} else if (int.class.equals(parm.type)
			   || Integer.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (val > Integer.MAX_VALUE
			|| val < Integer.MIN_VALUE) {
			throw new IllegalArgumentException
			    (errorMsg("argOverflow", name));
		    }
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    IntegerRandomVariable rv = new FixedIntegerRV((int)val);
		    parm.parser.parse(key, rv);
		} else if (LongRandomVariable.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariableRV rv
			= new FixedLongRVRV(new FixedLongRV(val));
		    parm.parser.parse(key, rv);
		} else if (long.class.equals(parm.type)
			   || Long.class.equals(parm.type)) {
		    long val = Math.round(value);
		    if (value != 0.0
			&& Math.abs((value - val) / value) > 1.0e-10) {
			throw new
			    IllegalArgumentException
			    (errorMsg("argNotInt", name));
		    }
		    LongRandomVariable rv = new FixedLongRV(val);
		    parm.parser.parse(key, rv);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else if (double.class.equals(parm.type)
		       || Double.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else if (int.class.equals(parm.type)
		       || Integer.class.equals(parm.type)) {
		long val = Math.round(value);
		if (val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
		    throw new
			IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (int)val);
	    } else if (long.class.equals(parm.type)
		       || Long.class.equals(parm.type)) {
		long val = Math.round(value);
		if (value != 0.0
		    && Math.abs((value - val) / value) > 1.0e-10) {
		    throw new
			IllegalArgumentException
			(errorMsg("argNotInt", name));
		}
		parm.parser.parse(key, (long)val);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a double-valued random variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, DoubleRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/* ("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		value = (DoubleRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (parm.rvmode) {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedDoubleRVRV(value));
		} else if ((double.class.equals(parm.type)
			    || Double.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (DoubleRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (double.class.equals(parm.type)
			   || Double.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a double-random-variable-valued random
     * variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, DoubleRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/* ("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		if (parm.rvmode) {
		    value = (DoubleRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    setBounds(parm, value);
	    if (DoubleRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    DoubleRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a boolean, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, boolean value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.equals(parm.type)) {
		    parm.parser.parse(key,
				      new FixedBooleanRVRV
				      (new FixedBooleanRV(value)));
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, new FixedBooleanRV(value));
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else  if (boolean.class.equals(parm.type)
			|| Boolean.class.equals(parm.type)) {
		parm.parser.parse(key, value);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a boolean-valued random variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, BooleanRandomVariable value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		value = (BooleanRandomVariable)(value.clone());
	    } catch (CloneNotSupportedException e) {}
	    if (parm.rvmode) {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, new FixedBooleanRVRV(value));
		} else if ((boolean.class.equals(parm.type)
			    || Boolean.class.equals(parm.type))
			   && parm.rvClass.isAssignableFrom
			   (value.getClass())) {
		    parm.parser.parse(key, value);
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    } else {
		if (BooleanRandomVariable.class.isAssignableFrom(parm.type)
		    && parm.type.isAssignableFrom(value.getClass())) {
		    parm.parser.parse(key, value);
		} else if (boolean.class.equals(parm.type)
			   || Boolean.class.equals(parm.type)) {
		    parm.parser.parse(key, value.next());
		} else {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Set a value provided as a boolean-random-variable-valued
     * random variable, given a key.
     * @param name the name of the entry
     * @param key the key
     * @param value the value of the object
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the key is not in a legal
     *            range
     */
    public void set(String name, String key, BooleanRandomVariableRV value)
	throws ConfigException
    {
	try {
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		set(name, Integer.parseInt(key), value);
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		set(name, objects, value);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    set(name, obj, value);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		/*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		      throw new IllegalArgumentException
		      ("" +parm.keyType +" not assignable from "
		      +((obj == null)? "null": obj.getClass()));
		    */
		}
		set(name, obj, value);
		return;
	    }
	    try {
		if (parm.rvmode) {
		    value = (BooleanRandomVariableRV)(value.clone());
		}
	    } catch (CloneNotSupportedException e) {}
	    if (BooleanRandomVariable.class.isAssignableFrom(parm.type)) {
		if (parm.rvmode) {
		    if (parm.rvClass.isAssignableFrom(value.getClass())) {
			parm.parser.parse(key, value);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		} else {
		    BooleanRandomVariable rv = value.next();
		    if (parm.type.isAssignableFrom(rv.getClass())) {
			parm.parser.parse(key, rv);
		    } else {
			throw new UnsupportedOperationException
			    (errorMsg("unsupported1", name));
		    }
		}
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} catch (IllegalArgumentException e) {
	    throw new ConfigException(name, key, e);
	} catch (IllegalStateException e) {
	    throw new ConfigException(name, key, e);
	} catch (UnsupportedOperationException e) {
	    throw new ConfigException(name, key, e);
	} catch (IndexOutOfBoundsException e) {
	    throw new ConfigException(name, key, e);
	}
    }

    /**
     * Clear keys by removing all keys associated with a parameter name
     * or restore a parameter to its default value.
     * @param name the name of the entry
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void clear(String name) {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException
		(errorMsg("badParmName", name));
	}
	/*
	if (parm.type != null || parm.keyType == null) {
	    throw new UnsupportedOperationException
		(errorMsg("unsupported1", name));
	}
	*/
	parm.parser.clear();
    }


    /**
     * Remove an entry with a key from a set of strings.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void remove(String name, String key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null || parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class)
		|| parm.keyType.equals(Integer.class)) {
		parm.parser.clear(Integer.parseInt(key));
		return;
	    }
	    if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType == null) {
		    throw new UnsupportedOperationException
			(errorMsg("noParmKeyType", name));
		} else if (parm.parmKeyType.addable == false) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] subkeys = new Object[strings.length];
		System.arraycopy(strings, 0, subkeys, 0, strings.length);
		check(name, subkeys, parm.keyType, parm.parmKeyType, true);
		parm.parser.clear(subkeys);
		return;
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		    throw new IllegalArgumentException
			("" +parm.keyType +" not assignable from "
			 +((obj == null)? "null": obj.getClass()));
		    */
		}
		parm.parser.clear(obj);
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    parm.parser.clear(obj);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		    /*("use of reflection failed");*/
	    } else	if (parm.keyType.equals(String.class)) {
		parm.parser.clear(key);
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	}

    /**
     * Remove an entry with a named-object key from a set of named objects.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void remove(String name, NamedObjectOps key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null || parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.isAssignableFrom(key.getClass())) {
		parm.parser.clear(key);
	    } else if (parm.keyType.equals(ParmKeyType.class)
		       && parm.parmKeyType.classArray.length >= 1
		       && parm.parmKeyType.classArray[0]
		       .isAssignableFrom(key.getClass())) {
		Object[] objkey = new Object[1];
		objkey[0] = key;
		parm.parser.clear(objkey);
	    } else {
		throw new
		    IllegalArgumentException
		    (errorMsg("keyNotNamedObj", name));
	    }
	}

    /**
     * Remove an entry with an enumeration key from a set of enumerations.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void remove(String name, Enum<?> key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);
	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.keyType == null || parm.type != null) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(ParmKeyType.class)
		&& parm.parmKeyType.classArray.length >= 1
		&& parm.parmKeyType.classArray[0]
		       .isAssignableFrom(key.getClass())) {
		Object[] objkey = new Object[1];
		objkey[0] = key;
		parm.parser.clear(objkey);
	    } else if (parm.keyType.isAssignableFrom(key.getClass())) {
		parm.parser.clear(key);
	    } else {
		throw new
		    IllegalArgumentException(errorMsg("keyNotEnum", name));
	    }
	}

    /**
     * Remove an entry with an index from a set of integers.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void remove(String name, int key) {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	if (parm.type != null) {
	    throw new UnsupportedOperationException
		(errorMsg("unsupported1", name));
	}
	if (parm.keyType != null) {
	    if (parm.keyType.equals(ParmKeyType.class)
		&& parm.parmKeyType.classArray.length >= 1
		&& (parm.parmKeyType.classArray[0].equals(int.class)
		    || parm.parmKeyType.classArray[0].equals(Integer.class))) {
		Object[] objkey = new Object[1];
		objkey[0] = Integer.valueOf(key);
		check(name, objkey, parm.keyType, parm.parmKeyType, true);
		parm.parser.clear(objkey);
		return;
	    } else if (!(parm.keyType.equals(int.class)
			 || parm.keyType.equals(Integer.class))) {
		throw new UnsupportedOperationException
		    (errorMsg("unsupported1", name));
	    }
	} else {
	    throw new UnsupportedOperationException
		(errorMsg("unsupported1", name));
	}
	parm.parser.clear(key);
    }

    /**
     * Remove a value provided as a long from a set of integers.
     * The long-integer argument will be converted to an int as this
     * method is provided to support polymorphism.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception IllegalArgumentException an argument is out of bounds
     *            or the name does not match a parameter
     * @exception UnsupportedOperationException the factory
     *            does not allow this method to be used
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void remove(String name, long value) {
	if (value < (long)Integer.MIN_VALUE
	    || value > (long)Integer.MAX_VALUE) {
	    throw new IllegalArgumentException(errorMsg("argOverflow", name));
	}
	remove(name, (int) value);
    }

    /**
     * Remove a value provided as a double from a set of integers.
     * The double-precision value will be converted to an int as this
     * method is provided to support polymorphism.
     * @param name the name of the entry
     * @param value the value of the object
     * @exception IllegalArgumentException an argument is out of bounds
     *            or the name does not match a parameter
     * @exception UnsupportedOperationException the factory
     *            does not allow this method to be used
     * @exception IllegalStateException the factory is not in
     *            a state for which this value may be parsed and entered
     */
    public void remove(String name, double value) {
	long val = Math.round(value);
	if (val > (long)Integer.MAX_VALUE || val < (long)Integer.MIN_VALUE) {
	    throw new IllegalArgumentException(errorMsg("argOverflow", name));
	}
	if (value != 0.0 && Math.abs((value - val) / value) > 1.0e-10) {
	    throw new
		IllegalArgumentException(errorMsg("argNotInt", name));
	}
	remove(name, (int)val);
    }


    /**
     * Remove a compound key provided as two objects, undoing the
     * corresponding add operation.
     * This is handles a special case - a compound key with two
     * components - and is equivalent to
     * <blockquote><pre>
     *    Object compoundKey[] = {key, subkey};
     *    remove(name, compoundKey);
     * </pre></blockquote>
     * @param name the name of the entry
     * @param key the first key
     * @param subkey the second key
     * @exception IllegalArgumentException an argument is out of bounds
     *            or the name does not match a parameter
     * @exception UnsupportedOperationException the factory
     *            does not allow this method to be used
     */
    public void remove(String name, Object key, Object subkey) {
	Object objects[] = {key, subkey};
	remove(name, objects);
    }

    /**
     * Remove an entry with a compound key, undoing the
     * corresponding add operation.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void remove(String name, Object[] key) {
	Parm parm = parmMap.get(name);
	if (parm == null) {
	    throw new IllegalArgumentException(errorMsg("badParmName", name));
	}
	if (parm.type != null || parm.keyType == null) {
	    throw new UnsupportedOperationException
		(errorMsg("unsupported1", name));
	}
	if (parm.keyType.equals(ParmKeyType.class)) {
	    if (parm.parmKeyType == null) throw new IllegalStateException
					(errorMsg("illFormedParm", name));
	    if (parm.parmKeyType.addable == false) {
		throw new IllegalArgumentException
		    (errorMsg("notAddable", name));
	    }
	    key = (Object[]) key.clone();
	    check(name, key, parm.keyType, parm.parmKeyType, true);
	    parm.parser.clear(key);
	} else if (key.length == 1) {
	    Object subkey = key[0];
	    if (subkey instanceof Integer) {
		remove(name, ((Integer)subkey).intValue());
	    } else if (subkey instanceof Long) {
		long vl = ((Long)subkey).longValue();
		if (vl > ((long)Integer.MAX_VALUE) ||
		    vl < ((long)Integer.MIN_VALUE)) {
		    throw new IllegalArgumentException
			(errorMsg("argOverflow", name));
		}
		remove(name, (int)vl);
	    } else if (subkey instanceof NamedObjectOps) {
		remove(name, (NamedObjectOps) subkey);
	    } else if (subkey instanceof Enum) {
		remove(name, (Enum<?>) subkey);
	    } else if (subkey instanceof String) {
		remove(name, (String)subkey);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("unknownSubKeyType", name));
	    }
	} else throw new IllegalArgumentException
		   (errorMsg("unknownMultiKey", name));
    }



    /**
     * Unset an entry with a key and restore it to the default value.
     * @param name the name of the entry
     * @param key the key
     * @exception IllegalArgumentException the argument does
     *            not match an entry
     * @exception UnsupportedOperationException the factory
     *            does not allow this entry to be removed
     */
    public void unset(String name, String key)
	throws 	IllegalArgumentException, UnsupportedOperationException
	{
	    Parm parm = parmMap.get(name);

	    if (parm == null) {
		throw new IllegalArgumentException
		    (errorMsg("badParmName", name));
	    }
	    if (parm.type == null || parm.keyType == null) {
		throw new IllegalArgumentException
		    (errorMsg("unsupported1", name));
	    }
	    if (parm.keyType.equals(int.class) ||
		parm.keyType.equals(Integer.class)) {
		unset(name, Integer.parseInt(key));
		return;
	    } else if (parm.keyType.equals(ParmKeyType.class)) {
		if (parm.parmKeyType != null
		    && parm.parmKeyType.addable == true) {
		    throw new UnsupportedOperationException
			(errorMsg("unsupported1", name));
		}
		String[] strings = key.split("\\.");
		Object[] objects = new Object[strings.length];
		System.arraycopy(strings, 0, objects, 0,
				 strings.length);
		unset(name, objects);
		return;
	    } else if (Enum.class.isAssignableFrom(parm.keyType)) {
		try {
		    java.lang.reflect.Method valueOf =
			parm.keyType.getMethod("valueOf", String.class);
		    Enum<?> obj = (Enum<?>) valueOf.invoke(null, key);
		    unset(name, obj);
		    return;
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e2) {
		} catch (NoSuchMethodException e3 ){
		}
		throw new UnsupportedOperationException
		    (errorMsg("reflectionFailed", name));
		    /*("use of reflection failed");*/
	    } else if (namedObjectClass.isAssignableFrom(parm.keyType)) {
		NamedObjectOps obj = namer.getObject(key);
		if (obj == null ||
		    !parm.keyType.isAssignableFrom(obj.getClass())) {
		    throw new IllegalArgumentException
			(errorMsg("notAssignable", name));
		    /*
		    throw new IllegalArgumentException
			("" +parm.keyType +" not assignable from "
			 +((obj == null)? "null": obj.getClass()));
		    */
		}
		unset(name, obj);
		return;
	    }
	    parm.parser.clear(key);
	}


    /**
     * Determine if the configure method is supported by this factory's
     * object namer.
     * @return true if configure(&lt;Configuration Object&gt;) is supported;
     *         false otherwise
     */
    public boolean configureSupported() {
	return namer.configureFactorySupported();
    }

     private void configureFactoryAux(String prefix, Object key,
					Object spec)
	 throws IllegalArgumentException
     {
	 if (spec instanceof JSArray) {
	     JSArray array = (JSArray) spec;
	     for (Object obj: array) {
		 configureFactoryAux(prefix, key, obj);
	     }
	 } else if (spec instanceof JSObject) {
	     JSObject ospec = (JSObject) spec;
	     if (ospec.containsKey("withPrefix")) {
		 prefix = prefix + ospec.get("withPrefix", String.class)
		     + ".";
	     }
	     if (ospec.containsKey("withIndex")) {
		 if (ospec.containsKey("withKey")) {
		     String msg = errorMsg("IndKeyCombo", prefix);
		     throw new IllegalArgumentException(msg);
		 }
		 Object newspec = ospec.get("withIndex");
		 if (newspec instanceof JSArray) {
		     JSArray newarray = (JSArray) newspec;
		     int i = 0;
		     for (Object obj: newarray) {
			 configureFactoryAux(prefix, Integer.valueOf(i++), obj);
		     }
		 } else {
		     configureFactoryAux(prefix, 0, newspec);
		 }
	     }
	     if (ospec.containsKey("withKey")) {
		 key = ospec.get("withKey");
	     }
	     if (ospec.containsKey("config")) {
		 Object newspec = ospec.get("config");
		 configureFactoryAux(prefix, key, newspec);
	     }
	     if (key == null) {
		 for (Map.Entry<String,Object> entry: ospec.entrySet()) {
		     String name = entry.getKey();
		     if (name.equals("withIndex")
			 || name.equals("withKey")
			 || name.equals("withPrefix")
			 || name.equals("config")) {
			 continue;
		     }
		     Object value = entry.getValue();
		     if (value instanceof JSArray) {
			 JSArray aval = (JSArray) value;
			 for (Object oval: aval) {
			     if (value instanceof ObjectParser.Source) {
				 oval = ((ObjectParser.Source) oval).evaluate();
			     }
			     oadd(prefix+name, oval);
			 }
		     } else if (mustAdd(prefix+name)) {
			 if (value instanceof ObjectParser.Source) {
			     value = ((ObjectParser.Source) value).evaluate();
			 }
			 oadd(prefix+name, value);
		     } else {
			 if (value instanceof ObjectParser.Source) {
			     value = ((ObjectParser.Source) value).evaluate();
			 }
			 oset(prefix+name, value);
		     }
		 }
	     } else {
		 for (Map.Entry<String,Object> entry: ospec.entrySet()) {
		     String name = entry.getKey();
		     if (name.equals("withIndex")
			 || name.equals("withKey")
			 || name.equals("withPrefix")
			 || name.equals("config")) {
			 continue;
		     }
		     Object value = entry.getValue();
		     if (value instanceof JSArray) {
			 JSArray aval = (JSArray) value;
			 for (Object oval: aval) {
			     if (value instanceof ObjectParser.Source) {
				 oval = ((ObjectParser.Source) oval).evaluate();
			     }
			     add(prefix+name, key, oval);
			 }
		     } else if (canAdd3(prefix+name)) {
			 if (value instanceof ObjectParser.Source) {
			     value = ((ObjectParser.Source) value).evaluate();
			 }
			 add(prefix+name, key, value);
		     } else {
			 if (value instanceof ObjectParser.Source) {
			     value = ((ObjectParser.Source) value).evaluate();
			 }
			 oset(prefix+name, key, value);
		     }
		 }
	     }
	 } else if (spec == null) {
	     String msg = errorMsg("noSpec", prefix, key);
	     throw new IllegalArgumentException(msg);
	 } else {
	     String msg = errorMsg("wrongSpec", prefix, key, spec.getClass());
	     throw new IllegalArgumentException(msg);
	 }
     }

     // The number of types is constrained by what can appear in
     // a JSObject or JSArray.
     private void oset(String name, Object value) throws ConfigException {
	 if (value == null || value instanceof String) {
	     set(name, (String)value);
	 } else if (value instanceof Integer) {
	     set(name, ((Integer)value).intValue());
	 } else if (value instanceof NamedObjectOps) {
	     set(name, (NamedObjectOps)value);
	 } else if (value instanceof Long) {
	     set(name, ((Long)value).longValue());
	 } else if (value instanceof Double) {
	     set(name, ((Double)value).doubleValue());
	 } else if (value instanceof Boolean) {
	     set(name, ((Boolean)value).booleanValue());
	 } else if (value instanceof Enum<?>) {
	     set(name, (Enum<?>) value);
	 } else if (value instanceof IntegerRandomVariable) {
	     set(name, (IntegerRandomVariable) value);
	 } else if (value instanceof IntegerRandomVariableRV) {
	     set(name, (IntegerRandomVariableRV) value);
	 } else if (value instanceof LongRandomVariable) {
	     set(name, (LongRandomVariable) value);
	 } else if (value instanceof LongRandomVariableRV) {
	     set(name, (LongRandomVariableRV) value);
	 } else if (value instanceof DoubleRandomVariable) {
	     set(name, (DoubleRandomVariable) value);
	 } else if (value instanceof DoubleRandomVariableRV) {
	     set(name, (DoubleRandomVariableRV) value);
	 } else if (value instanceof BooleanRandomVariable) {
	     set(name, (BooleanRandomVariable) value);
	 } else if (value instanceof BooleanRandomVariableRV) {
	     set(name, (BooleanRandomVariableRV) value);
	 } else {
	     String msg = errorMsg("wrongSpec1", name, value.getClass());
	     throw new IllegalArgumentException(msg);
	 }
     }

     private void oadd(String name, Object value) throws ConfigException {
	 if (value == null || value instanceof String) {
	     add(name, (String)value);
	 } else if (value instanceof Integer) {
	     add(name, ((Integer)value).intValue());
	 } else if (value instanceof NamedObjectOps) {
	     add(name, (NamedObjectOps)value);
	 } else if (value instanceof Long) {
	     add(name, ((Long)value).longValue());
	 } else if (value instanceof Double) {
	     add(name, ((Double)value).doubleValue());
	 } else if (value instanceof Enum<?>) {
	     add(name, (Enum<?>)value);
	 } else {
	     String msg = errorMsg("wrongSpec1", name, value.getClass());
	     throw new IllegalArgumentException(msg);
	 }
     }

     private void oset(String name, Object key, Object value)
	 throws ConfigException
     {
	 if (value == null || value instanceof String) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(), (String)value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (String)value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (String)value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (String)value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof Integer) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     ((Integer)value).intValue());
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, ((Integer)value).intValue());
	     } else if (key instanceof String) {
		 set(name, (String) key, ((Integer)value).intValue());
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, ((Integer)value).intValue());
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof NamedObjectOps) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(), (NamedObjectOps)value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (NamedObjectOps)value);
	     } else if (key instanceof String) {
		 set(name, (String) key, (NamedObjectOps)value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (NamedObjectOps)value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof Long) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     ((Long)value).longValue());
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, ((Long)value).longValue());
	     } else if (key instanceof String) {
		 set(name, (String)key, ((Long)value).longValue());
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, ((Long)value).longValue());
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof Double) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     ((Double)value).doubleValue());
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps) key, ((Double)value).doubleValue());
	     } else if (key instanceof String) {
		 set(name, (String)key, ((Double)value).doubleValue());
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, ((Double)value).doubleValue());
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof Boolean) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     ((Boolean)value).booleanValue());
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key,
		     ((Boolean)value).booleanValue());
	     } else if (key instanceof String) {
		 set(name, (String)key, ((Boolean)value).booleanValue());
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, ((Boolean)value).booleanValue() );
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof Enum<?>) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(), (Enum<?>) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (Enum<?>) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (Enum<?>) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key,  (Enum<?>) value);
	     }
	 } else if (value instanceof IntegerRandomVariable) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (IntegerRandomVariable) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (IntegerRandomVariable) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (IntegerRandomVariable) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (IntegerRandomVariable) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof IntegerRandomVariableRV) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (IntegerRandomVariableRV) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key,
		     (IntegerRandomVariableRV) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (IntegerRandomVariableRV) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (IntegerRandomVariableRV) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof LongRandomVariable) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (LongRandomVariable) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (LongRandomVariable) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (LongRandomVariable) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (LongRandomVariable) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof LongRandomVariableRV) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (LongRandomVariableRV) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (LongRandomVariableRV) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (LongRandomVariableRV) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (LongRandomVariableRV) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof DoubleRandomVariable) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (DoubleRandomVariable) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (DoubleRandomVariable) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (DoubleRandomVariable) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (DoubleRandomVariable) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof DoubleRandomVariableRV) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (DoubleRandomVariableRV) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (DoubleRandomVariableRV) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (DoubleRandomVariableRV) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (DoubleRandomVariableRV) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof BooleanRandomVariable) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (BooleanRandomVariable) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key, (BooleanRandomVariable) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (BooleanRandomVariable) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (BooleanRandomVariable) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else if (value instanceof BooleanRandomVariableRV) {
	     if (key instanceof Integer) {
		 set(name, ((Integer)key).intValue(),
		     (BooleanRandomVariableRV) value);
	     } else if (key instanceof NamedObjectOps) {
		 set(name, (NamedObjectOps)key,
		     (BooleanRandomVariableRV) value);
	     } else if (key instanceof String) {
		 set(name, (String)key, (BooleanRandomVariableRV) value);
	     } else if (key instanceof Enum<?>) {
		 set(name, (Enum<?>)key, (BooleanRandomVariableRV) value);
	     } else {
		 String msg =
		     errorMsg("wrongSpec", name, key, value.getClass());
		 throw new IllegalArgumentException(msg);
	     }
	 } else {
	     String msg = errorMsg("wrongSpec", name, key, value.getClass());
	     throw new IllegalArgumentException(msg);
	 }
     }

    /**
     * Configure a factory.
     * This is an optional operation. The default behavior is that
     * the object namer handles the operation and throws an
     * UnsupportedOperationException if it cannot.  The existing
     * configuration will not be cleared.
     * @param scriptObject an object in a scripting language, or instances
     *        of {@link JSObject} or {@link JSArray},
     *        representing a specification for how this factory
     *        should be configured
     * @exception UnsupportedOperationException the factory
     *            cannot be configured using a script object
     * @exception IllegalArgumentException the scriptObject is
     *            ill formed
     */
    public void configure(Object scriptObject)
	throws UnsupportedOperationException, IllegalArgumentException
     {
	 if (scriptObject instanceof JSObject
	     || scriptObject instanceof JSArray) {
	     configureFactoryAux("", null, scriptObject);
	 } else {
	     namer.configureFactory(this, scriptObject);
	 }
     }


    /**
     * Determine if an object is an instance of RandomVariable&lt;?&gt;.
     * This is intended for supporting scripting languages that might
     * not have full visibility into the Java type system.  It just
     * returns (obj instanceof RandomVariable&lt;?&gt;).
     * @param obj the object
     * @return true if it is an instance of RandomVariable&lt;?&gt;  or one
     *         of RandomVariable's subclasses; false otherwise
     */
    public boolean isRandomVariable(Object obj) {
	return (obj instanceof RandomVariable<?>);
    }

    /**
     * Determine if an object is an instance of a named object.
     * This is intended for supporting scripting languages that might
     * not have full visibility into the Java type system.
     * @param obj the object
     * @return true if it is an instance of the base name object for the
     *         object namer associated with this factory, or one of that
     *         named object's subclasses; false otherwise
     */

    public  boolean isNamedObject(Object obj) {
	return namedObjectClass.isAssignableFrom(obj.getClass());
    }



    String nameBase = "_Object_";
    static long nameIndex = 0;

    /**
     * Set the initial part of a name to use when creating objects.
     * The name provided will be prefaced and followed with "_".
     * @param nameRoot the factory-unique part of a name
     */
    public void setNameRoot(String nameRoot) {
	this.nameBase = "_" +nameRoot +"_";
    };


    static synchronized long nextNameIndex() {
	    return nameIndex++;
	}
    /**
     * Generate a named-object name.
     * @return a named-object  name
     */
    protected String getNextName() {
	return nameBase + nextNameIndex();
    }

    /**
     * Generate a unique index to append to a string to generate a
     * name.
     * @return the index
     */
    protected long getNextNameIndex() {
	return nextNameIndex();
    }


    /**
     * Start a sequence of creation of new objects.
     * This method is called at the start of the methods
     * <code>createObject</code> and <code>createObjects</code> unless
     * these methods are overridden.
     * The default method does nothing. Subclasses that override this
     * method must start with the statement
     * "<code>super.startObjectCreation();</code>".
     */
    protected void startObjectCreation() {}

    /**
     * End a sequence of creation of new objects.
     * This method is called at the end of the methods
     * <code>createObject</code> and <code>createObjects</code> unless
     * these methods are overridden,
     * including the case of an abnormal termination.
     * The default method does nothing. Subclasses that override this
     * method must start with the statement
     * "<code>super.endObjectCreation();</code>".
     */
    protected void endObjectCreation() {}


    /**
     * Construct a new object.
     * The object will not be initialized.  This method is called by
     * <code>createObject()</code> and <code>createObjects</code>
     * unless these methods are overridden.  Subclasses should call
     * {@link #willIntern() willIntern()} to determine if the object
     * will interned or not, and {@link #getObjectNamer()
     * getObjectNamer()} to find the object namer.  Some subclasses
     * (e.g., org.devqsim.SimObjectFactory) provide a method that will
     * return the object namer cast to the type needed by
     * constructors.  In the case of SimObjectFactory, this method is
     * named getSimulation(). For a subclass of SimObjectFactory to
     * create a new object of type <code>Foo</code>, <code>newObject</code>
     * will execute the expression
     * <pre><code>
     *      new Foo(getSimulation(), name, willIntern())
     * </CODE></PRE>
     * or an equivalent expression, where <code>name</code>  is the
     * argument passed to <code>newObject</code>.
     * @param name the name of the object to be created
     * @return the new object
     * @see #willIntern()
     * @see #setInterned(boolean)
     * @see #getObjectNamer()
     */
    abstract protected OBJ newObject(String name);

    /**
     * Initialize an object.
     * This method will call the methods for the object necessary to
     * initialize it based on how the factory was configured, and is
     * called by <code>createObject()</code> and
     * <code>createObjects</code> unless these methods are overridden.
     * The default method does nothing. Subclasses that override this
     * method to provide subclass-specific initializations must start
     * with the statement "<code>super.initObject(object);</code>".
     * @param object the object to initialize
     */
    protected void initObject(OBJ object) {}

    /**
     * Apply additional initializations after an array of objects
     * has been created and each initialized by calling
     * <code>initObject(obj)</code>. The default implementation
     * does nothing. This method is called from methods named
     * <code>createObjects</code> after <code>initObject</code> has
     * been called on each of the objects that will be created.
     * Subclasses should that override this method to provide
     * subclass-specific initializations must start with the statement
     * "<code>super.arrayInit(array, offset, n);</code>".
     * @param <T> the type of the objects that are created
     * @param array an array of objects that were created and
     *        initialized
     * @param offset the starting point in the array
     * @param n the number of elements to initialize, starting at the
     *        offset
     *
     */
    protected <T> void arrayInit(T[] array, int offset, int n) {}

    /**
     * Final initialization code.
     * Called on each object being initialized after all other
     * initialization steps have been completed (i.e., after
     * <code>initObject</code> and possibly <code>arrayInit</code>).
     * This will be called the methods named
     * <code>createObject</code> and <code>createObjects</code> unless
     * these methods are overridden.
     * The default method does nothing.  Subclasses that override this
     * method to provide subclass-specific initializations must start
     * with the statement "<code>super.doAfterInits(object);"</code>.
     * @param object the object whose initialization is to be completed
     */
    protected void doAfterInits(OBJ object) {}

    private boolean intern = true;

    /** Specify if objects created will be interned.
     * An object is interned if it appears in an object namer's tables
     * and can thus be looked up by name.
     *  @param value true if the objects will be interned; false if not
     */
    public void setInterned(boolean value) {
	intern = value;
    }

    /**
     * Determine if a created object will be interned.
     * An object is interned if it appears in an object namer's tables
     * and can thus be looked up by name.
     * @return true if it will be interned, false if not
     */
    public boolean willIntern() {return intern;}


    /**
     * Create a named object of the type this factory supports with the
     * object's name generated by the factory.
     * Generally this method should not be overridden as the object is actually
     * created by newObject(). The factory configuration is not altered
     * by a call to this method.
     * @return a named object
     * @see #newObject
     */
    public OBJ createObject() {
	return createObject(getNextName());
    }
    /**
     * Create a named object of the type this factory supports.
     * Generally this method should not be overridden as the object is actually
     * created by newObject().  The factory configuration is not altered
     * by a call to this method.
     * @param name the name of the object
     * @return a named object
     */
    public OBJ createObject(String name) {
	try {
	    startObjectCreation();
	    OBJ object = newObject(name);
	    initObject(object);
	    doAfterInits(object);
	    return object;
	} finally {
	    endObjectCreation();
	}
    }

    /**
     * Create a named object of the type this factory supports, modifying the
     * factory configuration.
     * The existing configuration, if any, will be cleared and then
     * replaced with the configuration specified by the scriptObject
     * argument
     * Generally this method should not be overridden as the object is actually
     * created by newObject().
     * @param name the name of the object
     * @param scriptObject an object in a scripting language
     *        representing a specification for how this factory
     *        should be configured
     * @return a named object
     */
    public OBJ createObject(String name, Object scriptObject) {
	clear();
	if (scriptObject == null) scriptObject = new JSObject();
	configure(scriptObject);
	return createObject(name);
    }

    /**
     * Create a named object of the type this factory supports, setting or
     * modifying the factory configuration.
     * Generally this method should not be overridden as the object is actually
     * created by newObject(). Regardless of whether the existing configuation
     * is cleared, the configuration after the scriptObject argument is
     * processed will not be changed by this method after the object is
     * created.
     * @param name the name of the object
     * @param clearConfig true if the existing configuration should be cleared;
     *        false otherwise
     * @param scriptObject an object in a scripting language
     *        representing a specification for how this factory
     *        should be configured
     * @return a named object
     */
    public OBJ createObject(String name, boolean clearConfig,
			    Object scriptObject) {
	if (clearConfig) clear();
	configure(scriptObject);
	return createObject(name);
    }


    /**
     * Create named objects with the names generated by the factory and
     * the number of objects determined by an array size.
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored if large enough; otherwise a new array of the same
     *        type is allocated
     * @return the objects that were created
     * @throws  ArrayStoreException if the runtime type of the specified array
     *          is not a supertype of the runtime type of every object
     *          created
     * @throws  NullPointerException if the specified array is null
     */
    public <T> T[] createObjects(T[] array) {
	return createObjects(array, array.length);
    }


    /**
     * Create named objects with the names generated by the factory.
     * Generally this method should not be overridden as each object is actually
     * created by newObject().
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored if large enough; otherwise a new array of the same
     *        type is allocated
     * @param n the number of objects to create
     * @return an array containing the newly created objects
     * @throws  ArrayStoreException if the runtime type of the specified array
     *          is not a supertype of the runtime type of every object
     *          created
     * @throws  NullPointerException if the specified array is null
     */
    public <T> T[] createObjects(T[] array, int n) {
	return createObjects(array, getNextName(), n);
    }

    /**
     * Create named objects given an offset with the names generated by
     * the factory.
     * Generally this method should not be overridden as each object is actually
     * created by newObject().
     * This method is intended for cases in which multiple calls to
     * createObjects will be used to add entries to an array.
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored
     * @param offset the offset into the array at which to start
     * @param n the number of objects to create
     * @return an array containing the newly created objects
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every object
     *         created
     * @throws NullPointerException if the specified array is null
     * @throws IllegalArgumentException the array length was too short given
     *         the offset
     */
    public <T> T[] createObjects(T[] array, int offset, int n) {
	return createObjects(array, getNextName(), offset, n);
    }


    /**
     * Create named objects with the number of objects determined by an array.
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored if large enough - otherwise a new array of the same
     *        type is allocated; object names will consist of
     *        a root name, followed by an "_", followed by a number
     * @param root the root of a name
     * @return the objects that were created
     * @throws  ArrayStoreException if the runtime type of the specified array
     *          is not a supertype of the runtime type of every object
     *          created
     * @throws  NullPointerException if the specified array is null
     */
    public <T> T[] createObjects(T[] array, String root) {
	return createObjects(array, root, array.length);
    }

    /**
     * Create named objects.
     * Generally this method should not be overridden as each object is actually
     * created by newObject().
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored if large enough; otherwise a new array of the same
     *        type is allocated; object names will consist of
     *        a root name, followed by an "_", followed by a number
     * @param root the root of a name
     * @param n the number of objects to create
     * @return an array containing the newly created objects
     * @throws  ArrayStoreException if the runtime type of the specified array
     *          is not a supertype of the runtime type of every object
     *          created
     * @throws  NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
	public <T> T[] createObjects(T[] array, String root, int n) {
	try {
	    startObjectCreation();
	    if (array.length < n) {
		array = (T[])java.lang.reflect.Array.newInstance
		    (array.getClass().getComponentType(), n);
	    }
	    for (int i = 0; i < n; i++) {
		OBJ obj = newObject(root + "_" + getNextNameIndex());
		initObject(obj);
		array[i] = (T) obj;
	    }
	    int m = n;
	    while (m < array.length) array[m++] = null;
	    arrayInit(array, 0, n);
	    for (int i = 0; i < n; i++) {
		doAfterInits((OBJ)(array[i]));
	    }
	    return array;
	} finally {
	    endObjectCreation();
	}
    }

    /**
     * Create named objects.
     * Generally this method should not be overridden as each object is actually
     * created by newObject().
     * This method is intended for cases where multiple calls to
     * createObjects will be used to add entries to an array.
     * @param <T> the type of the created objects
     * @param array the array into which the created objects are to be
     *        stored; object names will consist of
     *        a root name, followed by an "_", followed by a number
     * @param root the root of a name
     * @param offset the offset into the array at which to start
     * @param n the number of objects to create
     * @return an array containing the newly created objects at indices
     *         starting with the offset
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every object
     *         created
     * @throws NullPointerException if the specified array is null
     * @throws IllegalArgumentException the array length was too short given
     *         the offset
     */
    @SuppressWarnings("unchecked")
    public <T> T[] createObjects(T[] array, String root, int offset, int n) {
	try {
	    startObjectCreation();
	    if (array.length < n + offset) {
		throw new IllegalArgumentException
		    (errorMsg("arraylenTooShortGivenOffset", n, offset));
	    }
	    int m = offset + n;
	    for (int i = offset; i < m; i++) {
		OBJ obj = newObject(root + "_" + getNextNameIndex());
		initObject(obj);
		array[i] = (T) obj;
	    }
	    arrayInit(array, offset, n);
	    for (int i = offset; i < m; i++) {
		doAfterInits((OBJ)(array[i]));
	    }
	    return array;
	} finally {
	    endObjectCreation();
	}
    }
}

//  LocalWords:  exbundle namer Javascript EMCAScript ul li withKey
//  LocalWords:  config withIndex withPrefix timeline blockquote pre
//  LocalWords:  boolean Parm ParmManager initParms ParmParser  parm
//  LocalWords:  FactoryParmManager PrimitiveParm NamedObjectOps UTF
//  LocalWords:  KeyedPrimitiveParm CompoundParmType CompoundParm JRE
//  LocalWords:  CompoundParmType's KeyedCompoundParms newObject API
//  LocalWords:  startObjectCreation initObject arrayInit superclass
//  LocalWords:  doAfterInits endObjectCreation setDefaults runtime
//  LocalWords:  newConfigExceptionInstance NamedObjectFactory rvmode
//  LocalWords:  IllegalStateException UnsupportedOperationException
//  LocalWords:  IllegalArgumentException IndexOutOfBoundsException
//  LocalWords:  getClassLoader ResourceBundle getBundle parmNames lt
//  LocalWords:  configException configKeyedException notsubclass br
//  LocalWords:  UnsuportedOperationException className instantiable
//  LocalWords:  constructorNotFound classNotAccessible clazz apiURL
//  LocalWords:  noReqConstructor classNotInstantiated subexpression
//  LocalWords:  loadFactoriesAux newInstanceNotListed codebase html
//  LocalWords:  newInstanceNotAvail getTemplateKeyMap unbalancedHTML
//  LocalWords:  jdoc HREF href keymap definingFactoryClass keytype
//  LocalWords:  descriptionHTML hasDoc noDoc whitespace Javadoc bB
//  LocalWords:  javadoc rR Jj Oo LinkedList baseNameNull baseName pc
//  LocalWords:  NullPointerException baseNameEmpty baseNameResource
//  LocalWords:  MissingResourceException SecurityException lpack xml
//  LocalWords:  baseNameWrongPackage substring baseNameNotInPackage
//  LocalWords:  checkRBClass toString subpackage prepended clazz's
//  LocalWords:  superclasses keyPrefix unknownName layoutNullExt GLB
//  LocalWords:  layoutBadExt parms badParmName ParmKeyType LUB enums
//  LocalWords:  badRangeType argTooSmall argTooLarge argOverflow len
//  LocalWords:  argNotInt notAssignable valueOf reflectionFailed url
//  LocalWords:  compoundKey subkey emptyArray nullKey getClass param
//  LocalWords:  checkNullKey checkEmptyKey checkNamedObj countTokens
//  LocalWords:  checkNamedObjType StringTokenizer checkNullKeyInd
//  LocalWords:  checkIntFormatInd argOverflowInd argNotIntInd lsnof
//  LocalWords:  checkIsStringInd checkNamedObjInd getName keyType
//  LocalWords:  checkNamedObjTypeInd checkEnumTypeInd errorMsg anim
//  LocalWords:  checkTooManyKeys checkTooFewKeys argNotAssignableInd
//  LocalWords:  noParmKeyType keyNotNamedObj keyNotEnum notAddable
//  LocalWords:  illFormedParm unknownSubKeyType unknownMultiKey EPTS
//  LocalWords:  scriptObject RandomVariable instanceof nameRoot fdoc
//  LocalWords:  RandomVariable's createObject createObjects namer's
//  LocalWords:  willIntern getObjectNamer SimObjectFactory supertype
//  LocalWords:  getSimulation setInterned clearConfig indices bzdev
//  LocalWords:  ArrayStoreException arraylenTooShortGivenOffset api
//  LocalWords:  htmlTarget Javadocs factoryAPI setJavaAPI addJDoc
//  LocalWords:  factoryDoc addFDoc keymaps typeHTML keytypeHTML
//  LocalWords:  pmexample AbstractFooFactoryPM AbstractFooFactory
//  LocalWords:  factoryName isAddable IndexedSetter setIndexed BZDev
//  LocalWords:  AnimationLayer DFactory isetter Subclasses
//  LocalWords:  fromFactory factoryPackage nextPackageEntry
//  LocalWords:  unmodifiable subclasses polymorphism initializations
