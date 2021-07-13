package org.bzdev.obnaming;
import java.util.Set;
import java.util.Collection;

/**
 * Operations provided by an object namer.

 * Insertion and deletion of objects is handled by named-object
 * classes and hence the operations object namers support involve just
 * lookup operations.  This interface, and the 
 * {@link NamedObjectOps NamedObjectOps} interface, are provided mostly
 * for documentation as javadocs may not show the helper classes an
 * annotation processor generates.
 * @see NamedObjectOps
 */
public interface ObjectNamerOps<TO extends NamedObjectOps> {
    /**
     * Get the class common to all named objects.
     * @return the class
     */
    Class<TO> getNamedObjectClass();

    /**
     * Get an object from the object namer's tables.
     * @param name the name of the object
     * @return the object corresponding to the name provided
     */
    TO getObject(String name);

    /**
     * Get all the object names from a object namer's tables.
     * @return a set containing the names of all objects interned in
     *         the object namer's tables
     */
     Set<String> getObjectNames();

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
     Set<String> getObjectNames(Class<?> clazz);

     /**
     * Get a set of objects from a object namer's tables.
     * The objects are those for which either their classes or
     * one of their superclasses match the class passed as an argument.
     * @param clazz the class of the objects to get.
     * @return a collection containing  all interned objects whose class
     *         or one of its superclasses matches the class clazz passed
     *         as the method's argument
     */
     public <T> Collection<T> getObjects(Class<T> clazz);

    /**
     * Get a collection of all the interned named objects associated
     * with an object namer.
     * @return an unmodifiable collection of the objects interned in
     *         the object namer's tables.
     */
     Collection<TO> getObjects();
   /**
     * Get a named object with a particular name, provided that
     * the object is a subclass of a specific class.
     * @param name the name of the object
     * @param clazz the class of the object
     * @return the object or null if the object cannot be found
     */
     <T> T getObject(String name, Class<T> clazz);

    /**
     * Determine if the configureFactory method is supported.
     * @return true if configure() is supported; false otherwise
     */
     boolean configureFactorySupported();

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
     void configureFactory(NamedObjectFactory factory, Object scriptObject)
	 throws UnsupportedOperationException, IllegalArgumentException;

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
      *            support scripting
      */
     NamedObjectFactory createFactory(String varName,
				      String packageName,
				      String className)
	 throws UnsupportedOperationException, IllegalArgumentException;


    /**
     * Create multiple factories from a package in a single statement.
     * The first argument is the fully qualified name of a package.
     * The second is a scripting-language object specifying the individual
     * factories and how they should be named.  For the object-namer
     * subclass {@link org.bzdev.devqsim.Simulation} and its subclasses
     * ({@link org.bzdev.drama.DramaSimulation} and
     * {@link org.bzdev.anim2d.Animation2D} are defined in this class
     * library), the specification for ECMAScript is an ECMAScript objects
     * whose property names are used as the name of a scripting language
     * variable and whose value is a string containing the name of the
     * factory class (excluding its package name, which is provided by
     * the first argument).
     * For example, with ECMAScript for one of these subclasses,
     * <BLOCKQUOTE><CODE><PRE>
     *    a2d = new Animation2D(scripting, ...);
     *    a2d.createFactories("org.bzdev.anim2d", {
     *       alf: "AnimationLayer2DFactory",
     *       gvf: "GraphViewFactory"
     *    });
     * </PRE></CODE></BLOCKQUOTE>
     * will create two ECMAScript variables whose names are alf and gvf.
     * <P>
     * Unless explicitly implemented, this method will throw an
     * UnsupportedOperationException.
     * @param pkg the fully-qualified package name
     * @param scriptObject a scripting-language object specifying factories
     *        that should be created
      * @exception UnsupportedOperationException this object namer does not
      *            support scripting or creating multiple factories specified
      *            by a scripting object
      * @exception IllegalArgumentException an argument was not suitable
      *            for creating a factory.
     */
    default void createFactories(String pkg, Object scriptObject)
	throws UnsupportedOperationException, IllegalArgumentException {
	throw new UnsupportedOperationException();
    }

     /**
      * Create a factory.
      * The factory must have a single-argument constructor that takes
      * its object namer as its argument.
      * @param className the fully-qualified class name of a factory
      * @exception IllegalArgumentException the factory is not listed in
      *            a META-INF/services/org.bzdev.NamedObjectFactory resource
      *            or the class name does not refer to subclass of
      *            NamedObjectFactory
      */
     NamedObjectFactory createFactory(String className)
	 throws IllegalArgumentException;

     /**
      * Create a factory given its class.
      * The factory must have a single-argument constructor that takes
      * its object namer as its argument.
      * @param clazz the factory's class
      * @exception IllegalArgumentException the factory is not listed in
      *            a META-INF/services/org.bzdev.NamedObjectFactory resource
      *            or the class name does not refer to subclass of
      *            NamedObjectFactory
      */
     NamedObjectFactory createFactory(Class clazz)
	 throws IllegalArgumentException;


    /**
     * Add an alternative object namer for use by getObject methods.
     * When called, the alternate object namer specified in the argument
     * is added to a list of object namers that will be searched depth
     * first when getObject cannot find an object with a given name in
     * the current object namer's tables.
     * @param altNamer the alternative object namer
     */
     void addObjectNamer(ObjectNamerOps<TO> altNamer);

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
    boolean checkAltList(ObjectNamerOps<TO> altNamer);
}

//  LocalWords:  namer namers NamedObjectOps javadocs namer's clazz
//  LocalWords:  superclasses unmodifiable configureFactory varName
//  LocalWords:  scriptObject UnsupportedOperationException className
//  LocalWords:  IllegalArgumentException packageName subclasses PRE
//  LocalWords:  NamedObjectFactory BLOCKQUOTE createFactories alf
//  LocalWords:  AnimationLayer DFactory gvf GraphViewFactory
//  LocalWords:  getObject altNamer addObjectNamer
