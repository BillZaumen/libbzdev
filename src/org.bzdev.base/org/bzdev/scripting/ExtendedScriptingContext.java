package org.bzdev.scripting;
import org.bzdev.lang.ExceptionedCallable;
import java.lang.reflect.*;
import javax.script.*;
import java.util.Properties;
import java.io.InputStream;

//@exbundle org.bzdev.scripting.lpack.Scripting

/**
 * Extended ScriptingContext class.
 * <P>
 * This class provides methods named <code>create</code> that will
 * create a new instance of a class using the class or class name as
 * <code>create</code>'s first argument.  The first argument for these
 * methods should be either the class of the object that is to be
 * created or a string containing the fully-qualified class name of
 * that class. The remaining arguments are those that a constructor
 * can use. Up to 11 arguments are supported.  The original motivation
 * for these methods was a work-around for a bug, but the methods may
 * also be useful when a number of classes have constructors that take
 * the same arguments and the choice of which class to use is made at
 * run time.  The <code>create</code> methods specify <code>Object</code>
 * as the type of the object returned, which works for scripting
 * languages as these typically are not strongly typed.
 * <P>
 * There are two methods named <code>createArray</code> that will
 * create an array of a specified size, and whose first
 * argument is the class of an array's components or the
 * fully-qualified name of the class of the array's components.
 * This can be used by a scripting language to create a Java array
 * conveniently.
 * <P>
 * There are a also series of methods named
 * <code>createAndInitArray</code>.  The first argument of these
 * methods are also either the class of an array's components or the
 * fully-qualified name of the class of the array's components.
 * The remaining arguments are the values used to initialize the
 * array.
 * <P>
 * Several methods aid in the use of Java classes:
 * {@link #importClass(String)}, {@link #importClass(String,String)},
 * {@link #importClasses(String,Object)}, and {@link #finishImport()}.
 * These methods allow Java classes to be imported and bound to
 * variables in the script's name space.  The method
 * {@link #importClasses(String,Object)} will import multiple classes
 * using the scripting language to represent the class names in a
 * succinct form.  After a sequence of calls to importClass and importClasses,
 * the method {@link #finishImport()} must be called.
 * <P>
 * Finally, the <code>scrunner</code> command will run scripts in an
 * environment where a variable names <code>scripting</code> is
 * predefined. This variable is an instance of this class.
 */
public class ExtendedScriptingContext extends ScriptingContext {
   
    static String errorMsg(String key, Object... args) {
	return Scripting.errorMsg(key, args);
    }

    /**
     * Constructor.
     * Unless methods are overridden, the parent scripting context
     * provides the scripting language, script engine, and bindings.
     * This scripting context will be trusted if and only if the parent's
     * scripting context is trusted.
     * @param parent the parent scripting context; null if there is none.
     * @exception SecurityException this subclass of ScriptingContext cannot
     *            be created after a security manager was installed.
     */
    public ExtendedScriptingContext(ScriptingContext parent) {
	super(parent, parent.isTrusted());
	initImporterScript();
    }

    /**
     * Constructor specifying a security mode.
     * Unless methods are overridden, the parent scripting context
     * provides the scripting language, script engine, and bindings.
     * @param parent the parent scripting context; null if there is none.
     * @param trusted true if the script context is trusted; false otherwise
     * @exception SecurityException this subclass of ScriptingContext cannot
     *            be created after a security manager was installed or an
     *            attempt was made to create a trusted subclass of
     *            ScriptingContext from inside a sandbox
     */
    public ExtendedScriptingContext(ScriptingContext parent, boolean trusted) {
	super(parent, trusted);
	initImporterScript();
    }


    /**
     * Create an array given its class.
     * This is a convenience method so that a script does not
     * have to call java.lang.reflect.Array.newInstance(...).
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the name of a class
     * @param n the number of elements in the array
     * @return an array of size n
     * @exception NegativeArraySizeException the array size was negative
     */
    public Object createArray(Class<?> clazz, int n)
	throws NegativeArraySizeException
    {
	return Array.newInstance(clazz, n);
    }

    /**
     * Create an array given the class name of its element type.
     * This is a convenience method so that a script does not
     * have to call java.lang.reflect.Array.newInstance(...).
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the name of a class
     * @param n the number of elements in the array
     * @return an array of size n
     * @exception NegativeArraySizeException the array size was negative
     * @exception ClassNotFoundException the class could not be found
     */
    public Object createArray(String className, int n)
	throws NegativeArraySizeException, ClassNotFoundException
    {
	if (className.equals("double")) {
	    return Array.newInstance(Double.TYPE, n);
	} else if (className.equals("float")) {
	    return Array.newInstance(Float.TYPE, n);
	} else if (className.equals("int")) {
	    return Array.newInstance(Integer.TYPE, n);
	} else if (className.equals("long")) {
	    return Array.newInstance(Long.TYPE, n);
	} else if (className.equals("short")) {
	    return Array.newInstance(Short.TYPE, n);
	} else if (className.equals("char")) {
	    return Array.newInstance(Character.TYPE, n);
	} else if (className.equals("byte")) {
	    return Array.newInstance(Byte.TYPE, n);
	} else {
	    Class<?> clazz =
		ClassLoader.getSystemClassLoader().loadClass(className);
	    return Array.newInstance(clazz, n);
	}
    }


    /**
     * Create a new object given its class name and using a constructor with
     * no arguments.
     * @param cref the fully-qualified class name of object to be created
     * @exception ClassNotFoundException the class specified by the
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref);
    }

    /**
     * Create a new object given its class and using a constructor with
     * no arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of the object to be created
     * @exception ClassNotFoundException the class specified by the
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * one argument.
     * @param cref the fully-qualified class name of object to be created
     * @param arg the argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg);
    }

    /**
     * Create a new object given its class and using a constructor with
     * one argument.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg the argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg);
    }


    /**
     * Create a new object given its class name and using a constructor with
     * two arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2);
    }

    /**
     * Create a new object given its class and using a constructor with
     * two arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of the object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * three arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
          * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3);
    }

    /**
     * Create a new object given its class and using a constructor with
     * three arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3);
    }


    /**
     * Create a new object given its class name and using a constructor with
     * four arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4);
    }

    /**
     * Create a new object given its class and using a constructor with
     * four arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * five arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * Create a new object given its class and using a constructor with
     * five arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * six arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    /**
     * Create a new object given its class and using a constructor with
     * six arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * seven arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    /**
     * Create a new object given its class and using a constructor with
     * seven arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * eight arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8);
    }

    /**
     * Create a new object given its class and using a constructor with
     * eight arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * nine arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9);
    }

    /**
     * Create a new object given its class and using a constructor with
     * nine arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * ten arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @param arg10 the tenth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9, Object arg10) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9, arg10);
    }

    /**
     * Create a new object given its class and using a constructor with
     * ten arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @param arg10 the tenth argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9, Object arg10) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9, arg10);
    }

    /**
     * Create a new object given its class name and using a constructor with
     * eleven arguments.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @param arg10 the tenth argument for the constructor
     * @param arg11 the eleventh argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(String cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9, Object arg10, Object arg11) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9, arg10, arg11);
    }

    /**
     * Create a new object given its class and using a constructor with
     * eleven arguments.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param cref the class of object to be created
     * @param arg1 the first argument for the constructor
     * @param arg2 the second argument for the constructor
     * @param arg3 the third argument for the constructor
     * @param arg4 the forth argument for the constructor
     * @param arg5 the fifth argument for the constructor
     * @param arg6 the sixth argument for the constructor
     * @param arg7 the seventh argument for the constructor
     * @param arg8 the eight argument for the constructor
     * @param arg9 the ninth argument for the constructor
     * @param arg10 the tenth argument for the constructor
     * @param arg11 the eleventh argument for the constructor
     * @exception ClassNotFoundException the class specified by the first
     *            argument cref could not be found
     * @exception NoSuchMethodException a matching constructor could not be
     *            found
     * @exception IllegalArgumentException the class specified by the first
     *            argument cref is an abstract class, an enum, or an annotation,
     *            or a type conversion error occurred during instantiation
     * @exception InvocationTargetException the constructor invoked threw an
     *            exception
     * @exception SecurityException the system class loader did not permit this
     *            operation
     */
    public Object create(Class cref, Object arg1, Object arg2, Object arg3,
			 Object arg4, Object arg5, Object arg6, Object arg7,
			 Object arg8, Object arg9, Object arg10, Object arg11) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	return newInstance(cref, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			   arg8, arg9, arg10, arg11);
    }

    private static boolean classMatch(Class<?> parmClass,
				      Class<?> argClass) {

	if (parmClass.isAssignableFrom(argClass)) return true;

	Class<?> proxyParmClass = parmClass;
	if (parmClass.equals(int.class)) proxyParmClass = Integer.class;
	else if (parmClass.equals(long.class)) proxyParmClass = Long.class;
	else if (parmClass.equals(short.class)) proxyParmClass = Short.class;
	else if (parmClass.equals(byte.class)) proxyParmClass = Byte.class;
	else if (parmClass.equals(char.class)) proxyParmClass = Character.class;
	else if (parmClass.equals(double.class)) proxyParmClass = Double.class;
	else if (parmClass.equals(boolean.class))
	    proxyParmClass = Boolean.class;
	else if (parmClass.equals(float.class)) proxyParmClass = Float.class;

	Class<?> proxyArgClass = argClass;
	if (argClass.equals(int.class)) proxyArgClass = Integer.class;
	else if (argClass.equals(long.class)) proxyArgClass = Long.class;
	else if (argClass.equals(short.class)) proxyArgClass = Short.class;
	else if (argClass.equals(byte.class)) proxyArgClass = Byte.class;
	else if (argClass.equals(char.class)) proxyArgClass = Character.class;
	else if (argClass.equals(double.class)) proxyArgClass = Double.class;
	else if (argClass.equals(boolean.class)) proxyArgClass = Boolean.class;
	else if (argClass.equals(float.class)) proxyArgClass = Float.class;
	
	if (proxyParmClass.isAssignableFrom(proxyArgClass)) {
	    return true;
	} else if(Number.class.isAssignableFrom(proxyParmClass)
	   && Number.class.isAssignableFrom(proxyArgClass)) {
	    return true;
	}
	return false;
    }

    private Object argmap(Class<?> parmClass, Object arg) {
	Class<?> argClass = arg.getClass();
	if (parmClass.equals(argClass)) {
	    return arg;
	} else if (parmClass.equals(Integer.class)
		   || parmClass.equals(int.class)) {
	    if (arg instanceof Number) {
		return (Integer)(((Number)arg).intValue());
	    }
	} else if (parmClass.equals(Double.class)
		   || parmClass.equals(double.class)) {
	    if (arg instanceof Number) {
		return (Double)(((Number)arg).doubleValue());
	    }
	} else if (parmClass.equals(Long.class)
		   || parmClass.equals(long.class)) {
	    if (arg instanceof Number) {
		return (Long)(((Number)arg).longValue());
	    }
	} else if (parmClass.equals(Short.class)
		   || parmClass.equals(short.class)) {
	    if (arg instanceof Number) {
		return (Short)(((Number)arg).shortValue());
	    }
	} else if (parmClass.equals(Float.class)
		   || parmClass.equals(float.class)) {
	    if (arg instanceof Number) {
		return (Float)(((Number)arg).floatValue());
	    }
	} else if (parmClass.equals(Byte.class)
		   || parmClass.equals(byte.class)) {
	    if (arg instanceof Number) {
		return (Byte)(((Number)arg).byteValue());
	    }
	} else if (parmClass.equals(Character.class)
		   || parmClass.equals(char.class)) {
	    if (arg instanceof Character) {
		return arg;
	    } else if (arg instanceof Number) {
		return (Character)(char)(((Number)arg).intValue());
	    }
	} else if (parmClass.equals(Boolean.class)
		   || parmClass.equals(boolean.class)) {
	    return arg;
	}
	return arg;
    }

    /**
     * Create a new object given its class name and the arguments for
     * one of its constructors.
     */
    private Object newInstance(Object cref, Object... args) 
	throws ClassNotFoundException, NoSuchMethodException,
	       IllegalArgumentException, InvocationTargetException,
	       SecurityException
    {
	Class<?> clazz;
	if (cref instanceof Class) {
	    clazz = (Class) cref;
	} else {
	    String name;
	    if (cref instanceof String) {
		name = (String) cref;
	    } else {
		name = cref.toString();
	    }
	    // System.out.println("trying " + name);
	    clazz = ClassLoader.getSystemClassLoader().loadClass(name);
	}
	if (clazz.isEnum() || clazz.isAnnotation())
	    throw new IllegalArgumentException
		(errorMsg("noEnumAnnot", clazz.getName()));
	if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
	    throw new IllegalArgumentException
		(errorMsg("noAbstract", clazz.getName()));
	}
	Constructor<?> constructor = null;
	for (Constructor<?> c: clazz.getConstructors()) {
	    Class<?>[] classes = c.getParameterTypes();
	    if (c.isVarArgs()) {
		int length = classes.length - 1;
		if (length > args.length) continue;
		boolean skip = false;
		for (int i = 0; i < length; i++) {
		    if (!classMatch(classes[i], args[i].getClass())) {
			skip = true;
			break;
		    }
		}
		if (skip) continue;
		for (int i = length; i < args.length; i++) {
		    if (!classMatch(classes[length], args[i].getClass())) {
			skip = true;
			break;
		    }
		}
		if (skip) continue;
	    } else {
		if (classes.length != args.length) continue;
		boolean skip = false;
		for (int i = 0; i < classes.length; i++) {
		    if (!classMatch(classes[i], args[i].getClass())) {
			skip = true;
			break;
		    }
		}
		if (skip) continue;
	    }
	    constructor = c;
	    break;
	}
	if (constructor != null) {
	    Class<?>[] parmClasses = constructor.getParameterTypes();
	    for (int i = 0; i < parmClasses.length; i++) {
		args[i] = argmap(parmClasses[i], args[i]);
	    }
	    try {
		return constructor.newInstance(args);
	    } catch(IllegalAccessException iae) {
		// getConnstructors only returns public constructors
		// but throw an exception to keep javac happy
		String msg = errorMsg("badArg");
		throw new IllegalArgumentException(msg, iae);
	    } catch(InstantiationException ie) {
		// We tested for this above.
		// but throw an exception to keep javac happy
		String msg = errorMsg("badArg");
		throw new IllegalArgumentException(msg, ie);
	    } 
	} else {
	    throw new NoSuchMethodException(errorMsg("noMatch"));
	}
    }

    private Object doCreateAndInitArray(Object result, Object[] args)
	throws IllegalArgumentException, NullPointerException
    {
	int index = 0;
	Class<?> clazz = result.getClass().getComponentType();
	for (Object object: args) {
	    if (classMatch(clazz, object.getClass())) {
		Object obj = argmap(clazz, object);
		Array.set(result, index, obj);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("doCreateAndInitArray", index+1));
	    }
	    index++;
	}
	return result;
    }

    private Object ourCreateAndInitArray(Class<?> clazz, Object... args)
	throws IllegalArgumentException, NullPointerException
    {
	Object result = Array.newInstance(clazz, args.length);
	return doCreateAndInitArray(result, args);
    }

    private  Object ourCreateAndInitArray(String className, Object... args)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object result = createArray(className, args.length);
	return doCreateAndInitArray(result, args);
    }

    /**
     * Create a zero-length array given the class of its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz)
	throws IllegalArgumentException, NullPointerException
    {
	return createArray(clazz, 0);
    }

    /**
     * Create a zero-length array given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	return createArray(className, 0);
    }

    /**
     * Create and initialize an array of length one given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0};
	return ourCreateAndInitArray(clazz, args);
    }

    /**
     * Create and initialize an array of length one given the name of
     * the class of its components
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length two given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create and initialize an array of length two given the name of
     * the class of its components
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length three given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2};
	return ourCreateAndInitArray(clazz, args);
    }

    /**
     * Create an array of length three given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length four given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length four given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length five given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length five given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length six given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length six given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length seven given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length seven given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length eight given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length eight given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length nine given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length nine given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length ten given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @param arg9 the 9th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8, Object arg9)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8, arg9};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length ten given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @param arg9 the 9th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8, Object arg9)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8, arg9};
	return ourCreateAndInitArray(className, args);
    }

    /**
     * Create and initialize an array of length eleven given the class of
     * its components.
     * <P>
     * The classes Byte, Character, Double, Integer, Long, Short, have
     * fields named "TYPE" that provide the class names for the
     * primitive types "byte", "char", "double", "int", "long", and
     * "short" respectively.
     * <P>
     * Note: for Java 1.8 and 1.7, the default scripting engine
     * (ECMAScript) differ in how objects that represent a Java class
     * are treated.  For Java 1.7, which uses the Rhino script engine,
     * the variable used to represent a class is automatically converted
     * to a Java class when this method is called. For Java 1.8, which
     * uses the Nashorn script engine, one must use the "class" property
     * of the object to get the Java class or an error will occur. Due
     * to this incompatibility, one should use the variant of this method
     * that accepts a string rather than a class name if the script will
     * be used with both versions of Java.
     * @param clazz the class of the array components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @param arg9 the 9th component of the array
     * @param arg10 the 10th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception  NullPointerException the first argument was null
     */
    public Object createAndInitArray(Class<?> clazz, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8, Object arg9, Object arg10)
	throws IllegalArgumentException, NullPointerException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8, arg9, arg10};
	return ourCreateAndInitArray(clazz, args);

    }

    /**
     * Create an array of length eleven given the name of the class of its
     * components.
     * <P>
     * For convenience, the names of primitive types can be
     * given as "byte", "char", "double", "int", "long", or "short".
     * @param className the fully-qualified name of the class of the
     *        array's components
     * @param arg0 the 0th component of the array
     * @param arg1 the 1st component of the array
     * @param arg2 the 2nd component of the array
     * @param arg3 the 3rd component of the array
     * @param arg4 the 4th component of the array
     * @param arg5 the 5th component of the array
     * @param arg6 the 6th component of the array
     * @param arg7 the 7th component of the array
     * @param arg8 the 8th component of the array
     * @param arg9 the 9th component of the array
     * @param arg10 the 10th component of the array
     * @return the array
     * @exception IllegalArgumentException an array component had
     *            the wrong type
     * @exception NullPointerException the first argument was null
     * @exception ClassNotFoundException the class corresponding to the
     *            class name could not be found
     */
    public Object createAndInitArray(String className, Object arg0, Object arg1,
				     Object arg2, Object arg3, Object arg4,
				     Object arg5, Object arg6, Object arg7,
				     Object arg8, Object arg9, Object arg10)
	throws IllegalArgumentException, NullPointerException,
	       ClassNotFoundException
    {
	Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
			 arg8, arg9, arg10};
	return ourCreateAndInitArray(className, args);
    }

    private static final String IMPORTER_SCRIPT_RESOURCE =
	"/org/bzdev/scripting/ImporterScript.xml";
    private Object importer = null;

    private void initImporterScript() {
	final Properties importerScriptProperties = new Properties();
	try {
	    java.security.AccessController.doPrivileged
		(new java.security.PrivilegedExceptionAction<Void>() {
		    public Void run() throws java.io.IOException {
			/*
			InputStream is =
			    System.class.getResourceAsStream
			    (IMPORTER_SCRIPT_RESOURCE);
			*/
			InputStream is =
			    ExtendedScriptingContext.class.getResourceAsStream
			    (IMPORTER_SCRIPT_RESOURCE);
			if (is == null) {
			    System.out.println("warning: is == null");
			}
			importerScriptProperties.loadFromXML(is);
			return null;
		    }
		});
	} catch (java.security.PrivilegedActionException e) {
	    String msg = errorMsg("noResource", IMPORTER_SCRIPT_RESOURCE);
	    throw new UnsupportedOperationException();
	}
	String script = (String)
	    importerScriptProperties.get(getScriptLanguage());
	if (script != null) {
	    try {
		importer = evalScript(script);
	    } catch (Exception e) {
		throw new org.bzdev.lang.UnexpectedExceptionError(e);
	    }
	}
    }

    /**
     * Import a class given its fully-qualified name and bind it to a
     * scripting-language variable.
     * The name of the scripting language variable will be the last
     * component of the fully qualified name.
     * <P>
     * Inner classes of the class imported are accessible using the
     * normal dot (".") notation. For example,
     * <blockquote><code><pre>
     *      scripting.importClass("java.awt.geom.Path2D");
     *      scripting.finishImport();
     * </pre></code></blockquote>
     * will create an ECMAScript object stored in a variable named
     * Path2D, and the ECMAScript expression Path2D.Double will refer
     * to the class java.awt.geom.Path2D.Double.  In addition, the
     * ECMASCript <code>new</code>operator can be used to create
     * new instances:
     * <blockquote><code><pre>
     *      scripting.importClass("java.awt.geom.Path2D");
     *      scripting.finishImport();
     *      path = new Path2D.Double();
     * </pre></code></blockquote>
     * <P>
     * As a second example,
     * <P>
     * <blockquote><code><pre>
     *      scripting.importClass("java.util.Locale.Category")
     *      scripting.finishImport();
     * </pre></code></blockquote>
     * will create an ECMAScript variable named Category whose value
     * is the enumeration class java.util.Locale.Category.
     * <P>
     * Note: even for the same scripting language, the type of object
     * created will depended on the specific implementation in use.
     * @param className the fully-qualified name of the class
     * @exception ClassNotfoundException if the class could not be found
     * @exception IllegalArgumentException if the method detected a syntax
     *            error in the package name or class name
     * @exception NullPointerException if the class name was null
     * @exception SecurityException if the required permissions were not
     *            granted (e.g., with the scripting context was created)
     * @exception UnsupportedOperationException this operation is not
     *            supported (could not add a class to the scripting
     *            environment)
     * @exception ScriptExtension an error occurred executing a script
     * @see #finishImport()
     */
    public void importClass(String className)
	throws ClassNotFoundException, IllegalArgumentException,
	       NullPointerException, SecurityException,
	       UnsupportedOperationException, ScriptException
    {
	if (className == null) {
	    throw new NullPointerException(errorMsg("nullPointerArg"));
	}
	String[] comp = className.split("[.]");
	String cn = comp[0];
	String pkg = null;
	final String name1 = cn;
	if (java.security.AccessController.doPrivileged
	    (new java.security.PrivilegedAction<Boolean>() {
		public Boolean run() {
		    ClassLoader cl = ClassLoader.getSystemClassLoader();
		    try {
			cl.loadClass(name1);
			return true;
		    } catch (Exception e) {
			return false;
		    }
		}
	    })) {
	    importClass(pkg, className);
	    return;
	} else {
	    boolean pkgmode = true;
	    for(int i = 1; i < comp.length; i++) {
		if (pkgmode) pkg = cn;
		cn = cn + "." + comp[i];
		final String name2 = cn;
		if (pkgmode && java.security.AccessController.doPrivileged
		    (new java.security.PrivilegedAction<Boolean>() {
			public Boolean run() {
			    ClassLoader cl = ClassLoader.getSystemClassLoader();
			    try {
				cl.loadClass(name2);
				return true;
			    } catch (Exception e) {
				return false;
			    }
			}
		    })) {
		    cn = comp[i];
		    pkgmode = false;
		}
	    }
	    if (pkgmode == false) {
		importClass(pkg, cn);
		return;
	    } else {
		throw new ClassNotFoundException
		    (errorMsg("noClass", className));
	    }
	}
    }

    static private final String[] EMPTY_STRING_ARRAY =
	new String[0];

    /**
     * Import a class given its package and bind it to a
     * scripting-language variable.
     * The name of the scripting language variable will be the last
     * component of the class name.
     * <P>
     * Inner classes of the class imported are accessible using the
     * normal dot (".") notation. For example,
     * <blockquote><code><pre>
     *      scripting.importClass("java.awt.geom", "Path2D")
     *      scripting.finishImport();
     * </pre></code></blockquote>
     * will create an ECMAScript object stored in a variable named
     * Path2D, and the ECMAScript expression Path2D.Double will refer
     * to the class java.awt.geom.Path2D.Double. In addition, the
     * ECMASCript <code>new</code>operator can be used to create
     * new instances:
     * <blockquote><code><pre>
     *      scripting.importClass("java.awt.geom.Path2D");
     *      scripting.finishImport();
     *      path = new Path2D.Double();
     * </pre></code></blockquote>
     * <P>
     * As a second example,
     * <P>
     * <blockquote><code><pre>
     *      scripting.importClass("java.util", "Locale.Category")
     *      scripting.finishImport();
     * </pre></code></blockquote>
     * will create an ECMAScript variable named Category whose value
     * is the enumeration class java.util.Locale.Category.
     * @param packageName the name of the class' package.
     * @param className the class's name, excluding its package name
     * @exception ClassNotfoundException if the class could not be found
     * @exception IllegalArgumentException if the method detected a syntax
     *            error in the package name or class name
     * @exception NullPointerException if the class name was null
     * @exception SecurityException if the required permissions were not
     *            granted (e.g., with the scripting context was created)
     * @exception UnsupportedOperationException this operation is not
     *            supported (could not add a class to the scripting
     *            environment)
     * @exception ScriptExtension an error occurred executing a script
     * @see #finishImport()
     */
    public void importClass(String packageName, String className)
	throws ClassNotFoundException, IllegalArgumentException,
	       NullPointerException, SecurityException,
	       UnsupportedOperationException, ScriptException
    {
	// importClass(packageName, className, false, false);
	if (className == null) {
	    throw new NullPointerException(errorMsg("nullPointer2"));
	}
	if (className.indexOf('.') != -1) {
	    // className = className.replace('.', '$');
	}

	if (!className.matches("[_$\\p{IsLetter}]"
			       + "[_$\\p{IsLetter}\\p{IsDigit}]*"
			       + "([.][_$\\p{IsLetter}]"
			       + "[_$\\p{IsLetter}\\p{IsDigit}]*)*")) {
		throw new IllegalArgumentException
		    (errorMsg("className", className));
	}
	String fullPackageName;
	if (packageName == null) {
	} else if (packageName.length() == 0) {
	    packageName = null;
	} else {
	    for (String s: packageName.split("\\.")) {
		if (!s.matches("[_$\\p{IsLetter}]"
			       + "[_$\\p{IsLetter}\\p{IsDigit}]*")) {
		    throw new IllegalArgumentException
			(errorMsg("packageName", packageName));
		}
	    }
	}
	int ind = className.indexOf('.');
	String name = (ind == -1)? className: className.substring(0, ind);
	ind++;
	String[] rest = (ind == 0)? EMPTY_STRING_ARRAY:
	    className.substring(ind).split("\\.");
	String varname = (ind == 0)? name: rest[rest.length - 1];
	try {
	    callScriptMethod(importer, "doImport", this, varname,
			     packageName, name, rest);
	} catch (NoSuchMethodException e) {
	    throw new org.bzdev.lang.UnexpectedExceptionError(e);
	}
    }

    private Properties importScriptProperties = null;
    private static final String IMPORT_SCRIPT_RESOURCE =
	"/org/bzdev/scripting/ImportScript.xml";

    /**
     * Import classes, specifying the classes to import in a script.
     * This method is provided because the Java scripting interface
     * does not provide a standard way of importing Java classes into
     * a scripting environment. Java-7 used the Rhino ECMAScript
     * implementation that defined a global variable named Packages
     * and the EMCAScript functions importPackage and importClass.
     * Java-8 uses the Nashhorn ECMAScript implementation, which does
     * not contain these two functions, allows them to be defined by
     * calling
     * <blockquote><code><pre>
     *       load("nashorn:mozilla_compat.js");
     * </pre></code></blockquote>
     * but this function call will fail when the Rhino script engine
     * is used as the specified ECMAScript file will not be found.
     * Furthermore, the Rhino script engine will assume that
     * "nashorn:mozilla_compat.js" is a file name and as a result
     * scrunner (unless run in trusted mode) will throw a security
     * exception.
     * <P>
     * The importClasses method and importClass methods are provided
     * to to allow a script to import various classes regardless of
     * the script engine in use and without having to add
     * error-handling to the script.  To use importClasses, the first
     * argument is a package name.  The second argument is
     * scripting-language dependent. For ECMA script, the second
     * argument is an ECMA-script array of class names, excluding the
     * package name.  For inner classes, the names are a sequence of
     * names separated by a period (".") separating a class from the
     * name of an inner class that class contains.  The first
     * component of the name must be the name of a class in the
     * specified package.  In all cases, the inner classes must be
     * declared to be public and static.
     * <P>
     * If the variable <code>scripting</code> is an instance of
     * ExtendedScriptingContext (it is initialized as an instance of
     * this class when scrunner is used), and if it uses ECMAScript,
     * the following script fragment will import several classes from
     * the anim2d package:
     * <blockquote><code><pre>
     *   scripting.inputClasses("org.bzdev.anim2d",
     *                          ["Animation2D",
     *                           "AnimationLayer2D",
     *                           "GraphView]);
     *   scripting.finishImport();
     * </pre></code></blockquote>
     * The classes imported will be the classes <code>Animation2D</code>,
     * <code>AnimationLayer2D</code>, and <code>GraphView</code>, stored
     * using variables with the same names respectively.
     * The ECMAScript expressions that refer to the inner classes are
     * AnimationLayer2D.Type and GraphView.ZoomMode (both are enumerations).
     * Similarly, the statement
     * <blockquote><code><pre>
     *   scripting.inputClasses("org.bzdev.anim2d",
     *                          ["AnimationLayer2D.Type",
     *                           "GraphView.ZoomMode]);
     *   scripting.finishImport();
     * </pre></code></blockquote>
     * will define ECMAScript variables named Type and ZoomMode and
     * their values represent the enumeration classes
     * org.bzdev.anim2d.AnimationLayer2D.Type and
     * org.bzdev.anim2d.GraphView.ZoomMode respectively.
     * <P>
     * This method does not add any functionality beyond that provided by
     * the importClass methods: it's purpose is to shorten the amount of
     * text that must be placed in scripts to import the desired classes.
     * <P>
     * As an aside, the ECMA scripting engine that comes with Java 7 and
     * Java 8 allow one to construct an object named JavaImporter, with
     * packages provided as arguments. For example,
     * <blockquote><code><pre>
     *
     *      classes = new JavaImporter(java.util, java.io);
     *      ...
     *      with (classes) {
     *         var list = new LinkedList();
     *         ...
     *      }
     * </pre></code></blockquote>
     * <P>
     * There is a recommendation to use JavaImporter to minimize the
     * size of the scripting engine's global name space. This
     * recommendation makes sense for long-running applications
     * (servers, actually) that may load large numbers of classes in
     * response to user-generated requests. The result is that unneeded
     * classes will not be garbage-collected.  When using programs
     * such as scrunner, this is typically not an issue: scrunner
     * is intended to perform a single task, after which it exits.
     * Unfortunately, there is no guarantee as to what other
     * implementations of ECMAScript will do to import java classes
     * and specifically whether they will provide the JavaImporter
     * class. For maximal portability, it is possibly better to use
     * the methods {@link #importClasses(String,Object)},
     * {@link #importClass(String,String)} or {@link #importClass(String)}
     * Sequences of calls these methods should be followed by a call
     * to {@link #finishImport()}. For some scripting languages the imports
     * will not occur until {@link #finishImport()} is called.
     * @param packageName the name of the package from which classes
     *        are to be imported
     * @param scriptObject an object in current scripting context's
     *        scripting language that specifies the classes to import
     *        (typically an array or a list of names for the classes
     *        in the specified package)
     * @see #finishImport()
     */
    public void importClasses(String packageName, Object scriptObject)
    {
	if (importScriptProperties == null) {
	    importScriptProperties = new Properties();
	    try {
		java.security.AccessController.doPrivileged
		    (new java.security.PrivilegedExceptionAction<Void>() {
			public Void run() throws java.io.IOException {
			    InputStream is =
				ExtendedScriptingContext.class
				.getResourceAsStream
				(IMPORT_SCRIPT_RESOURCE);
			    importScriptProperties.loadFromXML(is);
			    return null;
			}
		    });
	    } catch (java.security.PrivilegedActionException e) {
		importScriptProperties = null;
		String msg = errorMsg("noResource", IMPORT_SCRIPT_RESOURCE);
		throw new UnsupportedOperationException();
	    }
	}
	try {
	    invokePrivateFunction(importScriptProperties,
				  ScriptingContext.PFMode.SANDBOXED,
				  "importClasses",
				  this, packageName, scriptObject);
	} catch (ScriptException ee) {
	    String msg = errorMsg("illformedScriptObject");
	    Throwable eee = ee;
	    Throwable cause = ee.getCause();
	    if (cause != null && !(cause instanceof ScriptException)) {
		cause = cause.getCause();
	    }
	    throw new IllegalArgumentException(msg, eee);
	}
    }

    private Properties finishImportProperties = null;
    private static final String FINISH_IMPORT_RESOURCE =
	"/org/bzdev/scripting/FinishImportScript.xml";

    /**
     * Complete a series of imports.
     * This method should be called after a sequence of calls to
     * {@link #importClass(String)}, {@link #importClass(String,String)},
     * and/or {@link #importClasses(String,Object)}.  It is optional for
     * some scripting languages but mandatory for others.  For the ESP
     * scripting language, a line containing '###' should be placed after
     * this call so that the import occurs before additional parsing.
     */
    public void finishImport() {
	if (finishImportProperties == null) {
	    finishImportProperties = new Properties();
	    try {
		java.security.AccessController.doPrivileged
		    (new java.security.PrivilegedExceptionAction<Void>() {
			public Void run() throws java.io.IOException {
			    InputStream is =
				ExtendedScriptingContext.class
				.getResourceAsStream(FINISH_IMPORT_RESOURCE);
			    finishImportProperties.loadFromXML(is);
			    return null;
			}
		    });
	    } catch (java.security.PrivilegedActionException e) {
		finishImportProperties = null;
		String msg = errorMsg("noResource", FINISH_IMPORT_RESOURCE);
		throw new UnsupportedOperationException();
	    }
	}
	try {
	    invokePrivateFunction(finishImportProperties,
				  ScriptingContext.PFMode.SANDBOXED,
				  "finishImport");
	} catch (ScriptException ee) {
	    String msg = errorMsg("illformedScriptObject");
	    Throwable eee = ee;
	    Throwable cause = ee.getCause();
	    if (cause != null && !(cause instanceof ScriptException)) {
		cause = cause.getCause();
	    }
	    throw new IllegalArgumentException(msg, eee);
	}
    }

}

//  LocalWords:  exbundle ScriptingContext createArray importClass th
//  LocalWords:  createAndInitArray boolean importClasses scrunner nd
//  LocalWords:  SecurityException clazz NegativeArraySizeException
//  LocalWords:  className ClassNotFoundException cref enum arg javac
//  LocalWords:  NoSuchMethodException IllegalArgumentException pre
//  LocalWords:  InvocationTargetException noEnumAnnot noAbstract io
//  LocalWords:  getConnstructors badArg noMatch doCreateAndInitArray
//  LocalWords:  NullPointerException packageName classname IsLetter
//  LocalWords:  ClassNotfoundException UnsupportedOperationException
//  LocalWords:  importInner nullPointer IsDigit varName ECMAScript
//  LocalWords:  EMCAScript importPackage Nashhorn blockquote ECMA cn
//  LocalWords:  anim inputClasses GraphView AnimationLayer ZoomMode
//  LocalWords:  JavaImporter util LinkedList scriptObject noResource
//  LocalWords:  illformedScriptObject ScriptExtension nullPointerArg
//  LocalWords:  noClass lastInd ClassLoader getSystemClassLoader
//  LocalWords:  loadClass pname ExtendedScriptingContext errorMsg
//  LocalWords:  args initImporterScript newInstance classMatch
//  LocalWords:  parmClass argClass isAssignableFrom proxyParmClass
//  LocalWords:  doImport Nashorn
