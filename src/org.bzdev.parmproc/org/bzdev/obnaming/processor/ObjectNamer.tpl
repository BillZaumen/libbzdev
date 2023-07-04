$(package)

import org.bzdev.obnaming.*;
import org.bzdev.obnaming.NamedObjectFactory.ConfigException;
import org.bzdev.util.SafeFormatter;
// import javax.annotation.processing.Generated;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.LinkedList;
import java.util.ResourceBundle;
$(configFactory:endCF)
import org.bzdev.scripting.ScriptingContext;
// import org.bzdev.scripting.ScriptingContext.PFMode;
import javax.script.ScriptException;
import java.io.InputStream;
$(endCF)

import java.security.AccessController;
import java.security.PrivilegedAction;

//@exbundle org.bzdev.obnaming.lpack.Obnaming

// @Generated(value="$(generator)", date="$(date)")
class $(helperClass)
      $(extendsHelperSuperclass)$(helperSuperclassTypeParms)
      implements ObjectNamerOps<$(objectClass)>
{
    private static ResourceBundle
	exbundle = ObnamingUtils.getBundle("org.bzdev.obnaming.lpack.Obnaming");

    private static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    Map<Class,Map<String,$(objectClass)>> maps =
        new HashMap<Class,Map<String,$(objectClass)>>();

    private Map<String,$(objectClass)> createMap(Class<?>clazz) {
	Map<String,$(objectClass)> result = new HashMap<String,$(objectClass)>();
	if (clazz != null) {
	    for (Class cls: maps.keySet()) {
		if (clazz.isAssignableFrom(cls)) {
		    result.putAll(maps.get(cls));
		}
	    }
	}
	return result;
    }

    /**
     * Get the class common to all named objects.
     * @return the class
     */
    public Class<$(objectClass)> getNamedObjectClass() {
        return $(objectClass).class;
    }

    $(cloop:endCloop)
    $(helperClass)($(constrArgs))$(constrExceptions) {
        super($(superCall));
	maps.put($(objectClass).class, createMap(null));
    }
    $(endCloop)

    boolean addObject(String name, $(objectHelperClass) object) {
	if (!(object instanceof $(objectClass))) {
	    throw new IllegalArgumentException
		(errorMsg("notInstance", $(objectClass).class.toString()));
	}
	$(objectClass) obj = ($(objectClass)) object;
	Class<?> subclass = object.getClass();

	if (subclass.isInterface() || subclass.isAnnotation()) {
	    throw new IllegalArgumentException(errorMsg("classExpected"));
	}
	while (subclass.isAnonymousClass()) {
	    subclass = subclass.getSuperclass();
	}
        if (!maps.containsKey(subclass)) maps.put(subclass,
						  createMap(subclass));
        
        Map<String,$(objectClass)> map = maps.get($(objectClass).class);
        Map<String,$(objectClass)> map2 = maps.get(subclass);

        if (map/*1*/.containsKey(name)) {
            return false;

        }
        map.put(name, obj);
	while (subclass != /*clazz*/$(objectClass).class) {
	    if (map2 != null) map2.put(name, obj);
	    subclass = subclass.getSuperclass();
	    map2 = maps.get(subclass);
	}

        return true;
    }

    boolean removeObject($(objectHelperClass) object)
    {
	if (!object.isInterned()) {
	    return false;
	}
	Class<?> subclass = object.getClass();
	if (!$(objectClass).class.isAssignableFrom(subclass)) {
	    String scn = subclass.getName();
	    String bcs = $(objectClass).class.getName();
	    throw new IllegalArgumentException
		(errorMsg("notInheritedFrom", scn, bcs));
	}
	String name = object.getName();

        Map<String,$(objectClass)> map = maps.get($(objectClass).class);
        if (!map.containsKey(name)) {
            return false;
        }
        Map<String,$(objectClass)> map2 = maps.get(subclass);
        map.remove(name);
	while (subclass != $(objectClass).class) {
	    if (map2 != null)  {
	        map2.remove(name);
	    }
	    subclass = subclass.getSuperclass();
	    map2 = maps.get(subclass);
	}
        return true;
    }

    LinkedList<ObjectNamerOps<$(objectClass)>> altList =
	new LinkedList<ObjectNamerOps<$(objectClass)>>();

    /**
     * Add an alternative object namer for use by getObject methods.
     * @param altNamer the alternative object namer
     */
    public final void addObjectNamer(ObjectNamerOps<$(objectClass)> altNamer) {
	if (altNamer == this || altList.contains(altNamer)) return;
	if (!altNamer.checkAltList(this))
	    throw new IllegalArgumentException
		(errorMsg("altNameLoop"));
	altList.add(altNamer);
    }

     /**
      * Determine if the argument is equal to this object namer or
      * a member of this object namer's object-namer list. This test
      * is recursive. It is specified in this interface for technical
      * reasons, and is not intended to be called otherwise. It is
      * called by addObjectNamer to ensure that a depth-first search
      * will terminate.
      * @param altNamer the object namer to test
      * @return true if he argument is equal to this object namer or
      * a member of this object namer's object-namer list, tested
      * recursively; false otherwise
      */
    public boolean checkAltList(ObjectNamerOps<$(objectClass)> altNamer) {
	if (altNamer == this) return false;
	for (ObjectNamerOps<$(objectClass)> alt: altList) {
	    if (!alt.checkAltList(altNamer)) return false;
	}
	return true;
    }


    /**
     * Get an object from the object namer's tables.
     * @param name the name of the object
     * @return the object corresponding to the name provided
     */
    public $(objectClass) getObject(String name) {
        Map<String,$(objectClass)> map = maps.get($(objectClass).class);
	$(objectClass) result = map.get(name);
	if (result == null) {
	    for (ObjectNamerOps<$(objectClass)> altNamer: altList) {
		result = altNamer.getObject(name);
		if (result != null) break;
	    }
	}
	return result;
    }

    /**
     * Get all the object names from a object namer's tables.
     * @return a set containing the names of all objects interned in
     *         the object namer's tables
     */
    public Set<String> getObjectNames() {
        return
	    Collections.unmodifiableSet(maps.get($(objectClass).class).keySet());
    }

     /**
     * Get some the object names from a object namer's tables.
     * The object names are those of objects for which either their classes or
     * one of their superclasses match the class passed as an argument.
     * @param clazz the class of the objects whose names are to appear in the
     *        set returned
     * @return a set containing the names of all objects interned in
     *         the object namer's tables such that the class of that object
     *         or the class of a subclass of that object matches the clazz
     *         argument
     *
     */
    public Set<String> getObjectNames(Class<?> clazz) {
        Map<String,$(objectClass)> map = maps.get(clazz);
        if (map == null) {
	    if ($(objectClass).class.isAssignableFrom(clazz)) {
	        map = createMap(clazz);
		maps.put(clazz, map);
	    } else {
		return Collections.emptySet();
	    }
        }
        return Collections.unmodifiableSet(map.keySet());
    }

   /**
     * Get a named object with a particular name, provided that
     * the object is a subclass of a specific class.
     * @param name the name of the object
     * @param clazz the class of the object
     * @return the object or null if the object cannot be found
     */
    @SuppressWarnings("unchecked")
    public<T> T getObject(String name, Class<T> clazz) {
        Map<String,$(objectClass)> map = maps.get($(objectClass).class);
	$(objectClass) result = map.get(name);
	if (result != null &&
	    !(clazz.isAssignableFrom(result.getClass()))) {
	    result = null;
	}
	if (result == null) {
	    for (ObjectNamerOps<$(objectClass)> altNamer: altList) {
		T altResult = altNamer.getObject(name, clazz);
		if (altResult != null) {
		    return altResult;
		}
	    }
	}
	return (T)result;
    }

    /**
     * Get a collection of all the interned named objects assocated
     * with an object namer.
     * @return an unmodifiable collection of the objects interned in
     *         the object namer's tables.
     */
    public Collection<$(objectClass)> getObjects() {
       return Collections.unmodifiableCollection
           (maps.get($(objectClass).class).values());
    }

     /**
     * Get a set of objects from a object namer's tables.
     * The objects are those for which either their classes or
     * one of their superclasses match the class passed as an argument.
     * @param clazz the class of the objects to get.
     * @return a collection containing  all interned objects whose class
     *         or one of its superclasses matches the class clazz passed
     *         as the method's argument
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getObjects(Class<T> clazz) {
        Map<String,$(objectClass)> map = maps.get(clazz);
        if (map == null) {
	    if ($(objectClass).class.isAssignableFrom(clazz)) {
	        map = createMap(clazz);
		maps.put(clazz, map);
	    } else {
		return Collections.emptyList();
	    }
        }
        return Collections.unmodifiableCollection((Collection<T>)map.values());
    }

    /**
     * Get a collection of objects with a class constraint, expressed as a
     *  class name, from a object namer's tables.
     * The objects are those for which either their classes or
     * one of their superclasses match the class passed as an argument.
     * <P>
     * Note: this method is provided because script engines do not
     * provide a standard way of denoting a Java class (the class not
     * an instance of it).  The collection it returns is a collection
     * of the common named object type.  Except for scripting languages
     * that do not provide compile-time type checking, one should nearly
     * always use {@link #getObjects(Class)}.
     * @param className the fully qualified class name of the objects to get.
     * @return an unmodifiable collection containing all interned
     *         objects whose class or one of its superclasses matches
     *         the class clazz passed as the method's argument; an
     *         empty (and unmodifiable) collection if no objects can be found
     */
    @SuppressWarnings({"unchecked", "deprecation", "removal"})
     public Collection<$(objectClass)> getObjects(String className) {
	try {
	    ClassLoader scl = AccessController.doPrivileged
		(new PrivilegedAction<ClassLoader>() {
		    public ClassLoader run() {
			return ClassLoader.getSystemClassLoader();
		    }
		});
	     Class clazz = scl.loadClass(className);
	     return  getObjects(clazz);
	} catch (ClassNotFoundException e1) {
	    return java.util.Collections.emptyList();
	}
    }

  $(configFactory:endCF0)
    private Properties configScriptProperties = null;
  $(endCF0)

  $(createFactory:endCrF0)
    private Properties createScriptProperties = null;
  $(endCrF0)

    /**
     * Determine if the configureFactory method is supported.
     * @return true if configure() is supported; false otherwise
     */
     public boolean configureFactorySupported() {
      $(configFactory:endCF1)
	return true;
      $(endCF1)
      $(noConfigFactory:endNCF1)
	return false;
      $(endNCF1)
     }

    /**
     * Configure a factory.
     * This is an optional operation.
     * @param factory the factory to be configured
     * @param scriptObject an object in a scripting language
     *        representing a specification for how this factory
     *        should be configured
     * @exception UnsupportedOperationException the factory
     *            cannot be configured using a script object
     * @exception IllegalArgumentException the scriptObject is
     *            ill formed
     */
    @SuppressWarnings({"deprecation", "removal"})
    public void configureFactory(NamedObjectFactory factory,
				 Object scriptObject)
	throws UnsupportedOperationException, IllegalArgumentException
    {

      $(configFactory:endCF2)
	if (configScriptProperties == null) {
	    configScriptProperties = new Properties();
	    try {
	      java.security.AccessController.doPrivileged
		(new java.security.PrivilegedExceptionAction<Void>() {
		  public Void run() throws java.io.IOException {
		    if (("$(configScriptResource)").equals
		        ("/org/bzdev/obnaming/FactoryConfigScript.xml")) {
		      ObnamingUtils.setPropertiesForXMLResource
		        (configScriptProperties, "$(configScriptResource)");
		    } else {
		      InputStream is =
		        $(helperClass).class.getResourceAsStream
		        ("$(configScriptResource)");
		      configScriptProperties.loadFromXML(is);
		    }
		    return null;
		  }
		});
	    } catch (java.security.PrivilegedActionException e) {
	        configScriptProperties = null;
		String msg = errorMsg("noResource", "$(configScriptResource)");
		throw new UnsupportedOperationException(msg, e.getException());
	    }
	}
	try {
	    invokePrivateFunction(configScriptProperties,
				  // ScriptingContext.PFMode.SANDBOXED,
				  "configureFactory", factory, scriptObject);
	} catch (ScriptException ee) {
	    String msg = errorMsg("illformedScriptObject");
	    Throwable eee = ee;
	    Throwable cause = ee.getCause();
	    while (cause != null
		   && !(cause instanceof ConfigException
			|| cause instanceof ScriptException)) {
		cause = cause.getCause();
	    }
	    if (cause instanceof ConfigException) {
		eee = cause;
	    }
	    throw new IllegalArgumentException(msg, eee);
	}
      $(endCF2)
      $(noConfigFactory:endNCF2)
	throw new UnsupportedOperationException();
      $(endNCF2)
    }

    @SuppressWarnings({"deprecation", "removal"})
    public void createFactories(String pkg, Object scriptObject)
	throws UnsupportedOperationException, IllegalArgumentException
    {
      $(createFactory:endCrF1)
	if (createScriptProperties == null) {
	    createScriptProperties = new Properties();
	    try {
	      java.security.AccessController.doPrivileged
		(new java.security.PrivilegedExceptionAction<Void>() {
		  public Void run() throws java.io.IOException {
		    if ("$(createScriptResource)".equals
		         ("/org/bzdev/obnaming/FactoryCreateScript.xml")) {
		      ObnamingUtils.setPropertiesForXMLResource
		        (createScriptProperties, "$(createScriptResource)");
		    } else {
		      InputStream is =
		        $(helperClass).class.getResourceAsStream
		        ("$(createScriptResource)");
		      createScriptProperties.loadFromXML(is);
		    }
		    return null;
		  }
		});
	    } catch (java.security.PrivilegedActionException e) {
	        createScriptProperties = null;
		String msg = errorMsg("noResource", "$(createScriptResource)");
		throw new UnsupportedOperationException(msg, e.getException());
	    }
	}
	try {
	    invokePrivateFunction(createScriptProperties,
				  // ScriptingContext.PFMode.SANDBOXED,
				  "createFactories", this, pkg, scriptObject);
	} catch (ScriptException ee) {
	    String msg = errorMsg("illformedScriptObject");
	    Throwable eee = ee;
	    Throwable cause = ee.getCause();
	    while (cause != null
		   && !(cause instanceof IllegalArgumentException
			|| cause instanceof ScriptException)) {
		cause = cause.getCause();
	    }
	    if (cause instanceof IllegalArgumentException) {
		eee = cause;
	    }
	    throw new IllegalArgumentException(msg, eee);
	}
      $(endCrF1)
      $(noCreateFactory:endNCrF2)
	throw new UnsupportedOperationException();
      $(endNCrF2)
    }

     /**
      * Create a factory and store it in a scripting-language variable.
      * The factory must have a single-argument constructor that takes
      * its object namer as its argument.
      * <P>
      * This method is provided for convenience - to reduce the amount
      * of typing when adding factories to a script.  It returns the
      * factory that was created in addition to storing it in a variable
      * in case a user of this method tries to assign the value it returns.
      * @param varName the name of a scripting-language variable
      * @param packageName the name of the package (null or an empty
      *        string for the unnamed package)
      * @param className the  class name of a factory, excluding the
      *        package name
      * @return the factory that was created
      * @exception IllegalArgumentException the factory is not listed in
      *            a META-INF/services/org.bzdev.NamedObjectFactory resource
      *            or the class name does not refer to subclass of
      *            NamedObjectFactory
      * @exception UnsupportedOperationException this object namer does not
      *            support scripting;
      */
    public NamedObjectFactory createFactory(String varName,
			                    String packageName,
                                            String className)
	 throws UnsupportedOperationException, IllegalArgumentException
    {
      $(hasScripting:endScripting1)
        String prefix = (packageName == null || packageName.length() == 0)?
	   "": packageName + ".";
        NamedObjectFactory nof =
	    NamedObjectFactory.newInstance(this, prefix + className);
	putScriptObject(varName, nof);
	return nof;
      $(endScripting1)
      $(doesNotHaveScripting:endScripting2)
	throw new UnsupportedOperationException();
      $(endScripting2)
    }

    /**
     * Create a factory.
     * The factory must have a single-argument constructor that takes
     * its object namer as its argument.
     * @param className the fully-qualified class name of a factory.
     * @exception IllegalArgumentException the factory is not listed in
     *            a META-INF/services/org.bzdev.NamedObjectFactory resource
     *            or the class name does not refer to subclass of
     *            NamedObjectFactory
     */
    public NamedObjectFactory createFactory(String className)
	 throws IllegalArgumentException
    {
	return NamedObjectFactory.newInstance(this, className);
    }

    /**
     * Create a factory given a class.
     * The factory must have a single-argument constructor that takes
     * its object namer as its argument.
     * @param clazz the factory's class
     * @exception IllegalArgumentException the factory is not listed in
     *            a META-INF/services/org.bzdev.NamedObjectFactory resource
     *            or the class name does not refer to subclass of
     *            NamedObjectFactory
     */
    public NamedObjectFactory createFactory(Class clazz)
	 throws IllegalArgumentException
    {
	return NamedObjectFactory.newInstance(this, clazz);
    }
}
