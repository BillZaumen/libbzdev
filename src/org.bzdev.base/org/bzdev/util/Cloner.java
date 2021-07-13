package org.bzdev.util;
import org.bzdev.lang.UnexpectedExceptionError;
import java.lang.reflect.*;
import java.security.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Utility class for cloning objects.
 * The return type for <CODE>clone()</CODE> is <CODE>Object</CODE>,
 * so one may have to cast the type returned by this method in order
 * to use the value returned by it: for example
 * <BLOCKQUOTE><CODE><PRE>
 * Path2D path1 = ...
 * Path2D path2 = (Path2D)path1.clone();
 * </PRE></CODE></BLOCKQUOTE>
 * By contrast, the method {link Cloner#makeClone(Object)} does not
 * require a cast.
 * <BLOCKQUOTE><CODE><PRE>
 * Path2D path1 = ...;
 * Path2D path2 = Cloner.makeClone(path1);
 * </PRE></CODE></BLOCKQUOTE>
 * <P>
 * In a few corner cases, {@link Cloner#makeClone(Object)} will not
 * work.  The class {@link java.awt.geom.Path2D.Double}, for example,
 * declares its <CODE>clone</CODE> method to be final, which implies
 * that cloning a subclass of {@link java.awt.geom.Path2D.Double} will
 * return an object whose type is {@link java.awt.geom.Path2D.Double}.
 * The class {@link org.bzdev.geom.SplinePath2D} is such a subclass
 * (it only adds additional methods, not fields). As a result, the
 * code
 * <BLOCKQUOTE><CODE><PRE>
 * SplinePath2D path1 = ...;
 * Path2D.Double path2 = Cloner.makeClone(path1);
 * </PRE></CODE></BLOCKQUOTE>
 * will fail because {@link Cloner#makeClone(Object)} would cast the
 * cloned object to <CODE>SplinePath2D</CODE>. For this case, one
 * can use the method {@link Cloner#makeCastedClone(Class,Object)}:
 * <BLOCKQUOTE><CODE><PRE>
 * SplinePath2D path1 = ...;
 * Path2D.Double path2 = Cloner.makeCastedClone(Path2D.Double.class, path1);
 * </PRE></CODE></BLOCKQUOTE>
 * The first argument of this method provides the compile-time class
 * for the object that will be returned.
 */
public class Cloner {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    /**
     * Clone an object.
     * @param obj the object to clone
     * @return a cloned object with the same type as that of its argument
     * @exception CloneNotSupportedException the object could not be cloned
     * @exception SecurityException the ReflectPermission target
     *            "suppressAccessChecks" was not granted to Cloner's code base
     */
    @SuppressWarnings("unchecked")
    public static <T> T makeClone(T obj)
	throws CloneNotSupportedException, SecurityException
    {
	final Class<?> clazz = obj.getClass();
	if (obj instanceof Cloneable) {
	    if (clazz.isArray()) {
		Class<?> elementClass = clazz.getComponentType();
		if (elementClass.isPrimitive()) {
		    if (elementClass.equals(Byte.TYPE)) {
			return (T)((byte[])obj).clone();
		    } else if (elementClass.equals(Character.TYPE)) {
			return (T)((char[])obj).clone();
		    } else if (elementClass.equals(Short.TYPE)) {
			return (T)((short[])obj).clone();
		    } else if (elementClass.equals(Integer.TYPE)) {
			return (T)((int[])obj).clone();
		    } else if (elementClass.equals(Long.TYPE)) {
			return (T)((long[])obj).clone();
		    } else if (elementClass.equals(Float.TYPE)) {
			return (T)((float[])obj).clone();
		    } else if (elementClass.equals(Double.TYPE)) {
			return (T)((double[])obj).clone();
		    } else if (elementClass.equals(Boolean.TYPE)) {
			return (T)((boolean[])obj).clone();
		    }
		} else {
		    return (T)((Object[])obj).clone();
		}
	    }
	    try {
		Class<?> c = clazz;
		while (c != null && !Modifier.isPublic(c.getModifiers())) {
		    c = c.getSuperclass();
		}
		if (c == null) {
		    // should never happen as Object is public
		    throw new UnexpectedExceptionError();
		}
		final Class<?> cc = c;
		final Method cloneMethod = AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Method>() {
			public Method run() throws NoSuchMethodException {
			    return cc.getMethod("clone");
			}
		    });

		if (!Modifier.isPublic(cloneMethod.getModifiers())) {
		    throw new CloneNotSupportedException
			(errorMsg("cloneNotPublic"));
		}
		Object object = cloneMethod.invoke(obj);
		if (clazz.isInstance(object)) {
		    return (T) object;
		}
		throw new CloneNotSupportedException
		    (errorMsg("badTypeForClone", object.getClass().getName()));
	    } catch (PrivilegedActionException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflection"));
	    } catch (IllegalAccessException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflectionIA"));
	    } catch (InvocationTargetException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflectionITE"));
	    }
	}
	throw new CloneNotSupportedException(errorMsg("notClonable"));
    }

    /**
     * Partially clone an object.
     * @deprecated The name of this method was confusing. Please
     * use {@link #makeCastedClone(Class,Object)} instead.
     * @param resultClass a superclass or interface of the object that will be
     *        returned
     * @param obj the object to clone
     * @return a cloned object with the same type as that of its argument
     * @exception CloneNotSupportedException the object could not be cloned
     *            or the type of a clone did not match the type requested
     *            by the resultClass argument
     * @exception SecurityException the ReflectPermission target
     *            "suppressAccessChecks" was not granted to Cloner's code base
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static
	<C,T extends C> C makePartialClone(Class<C> resultClass, T obj)
	throws CloneNotSupportedException, SecurityException
    {
	return makeCastedClone(resultClass, obj);
    }

    /**
     * Clone an object and cast it to a specific type.
     * This method calls clone() and return an object with a type that
     * is a supertype or interface of the compile-time type of the
     * object being cloned.
     * <P>
     * Some classes in the standard Java library are clonable but
     * declare a public clone() method to be final, but not while the
     * class itself is not final.  Examples include
     * {@link java.awt.geom.Path2D.Double}: a couple of
     * classes in the org.bzdev.geom package are subclasses of
     * Path2D.Double, but none of the methods of Path2D.Double were
     * overridden. Calling clone() on an instance of a subclass of
     * Path2D.Double will produce an instance of Path2D.Double, not an
     * instance of the subclass.  The class
     * {@link org.bzdev.geom.SplinePath2D} is a good example. The
     * following code will create a clone of an instance of
     * SplinePath2D:
     * <BLOCKQUOTE><CODE><PRE>
     *      SplinePath2D spath = ...;
     *      Path2D path =
     *          Cloner.makeCastedClone(Path2D.Double.class, spath);
     * </PRE></CODE></BLOCKQUOTE>
     * At runtime, this is equivalent to
     * <BLOCKQUOTE><CODE><PRE>
     *      SplinePath2D spath = ...;
     *      Path2D path = (Path2D.Double)(spath.clone());
     * </PRE></CODE></BLOCKQUOTE>
     * but with compile-time tests to ensure that Path2D.Double is in
     * fact a superclass of SplinePath2D. Such a test is not automatic
     * because spath.clone() has a compile-time type of <CODE>Object</CODE>.
     * Note that
     * <BLOCKQUOTE><CODE><PRE>
     *      SplinePath2D spath = ...;
     *      Path2D path =  Cloner.makeClone(Path2D.Double.class, spath);
     * </PRE></CODE></BLOCKQUOTE>
     * would have failed because the compile-type type of the makeClone
     * method would be the compile-time type of spath (i.e., SplinePath2D)
     * and the actual object returned is an instance of Path2D.Double.
     * <P>
     * Note: This method will not clone an array.
     * @param resultClass a superclass or interface of the object that will be
     *        returned
     * @param obj the object to clone
     * @return a cloned object whose compile-time type matches the
     *         resultClass argument
     * @exception CloneNotSupportedException the object could not be cloned
     *            or the type of a clone did not match the type requested
     *            by the resultClass argument
     * @exception SecurityException the ReflectPermission target
     *            "suppressAccessChecks" was not granted to Cloner's code base
     */
    @SuppressWarnings("unchecked")
    public static 
	<C,T extends C> C makeCastedClone(Class<C> resultClass, T obj)
	throws CloneNotSupportedException, SecurityException
    {
	final Class<?> clazz = obj.getClass();
	if (obj instanceof Cloneable) {
	    try {
		Class<?> c = clazz;
		while (c != null && !Modifier.isPublic(c.getModifiers())) {
		    c = c.getSuperclass();
		}
		if (c == null) {
		    // should never happen as Object is public
		    throw new UnexpectedExceptionError();
		}
		final Class<?> cc = c;
		final Method cloneMethod = AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Method>() {
			public Method run() throws NoSuchMethodException {
			    return cc.getMethod("clone");
			}
		    });

		if (!Modifier.isPublic(cloneMethod.getModifiers())) {
		    throw new CloneNotSupportedException
			(errorMsg("cloneNotPublic"));
		}
		Object object = cloneMethod.invoke(obj);
		if (resultClass.isInstance(object)) {
		    return (C) object;
		}
		throw new CloneNotSupportedException
		    (errorMsg("badTypeForClone", object.getClass().getName()));
	    } catch (PrivilegedActionException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflection"));
	    } catch (IllegalAccessException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflectionIA"));
	    } catch (InvocationTargetException e) {
		throw new CloneNotSupportedException
		    (errorMsg("noCloneViaReflectionITE"));
	    }
	}
	throw new CloneNotSupportedException(errorMsg("notClonable"));
    }
}

//  LocalWords:  exbundle BLOCKQUOTE PRE Cloner makeClone SplinePath
//  LocalWords:  makeCastedClone CloneNotSupportedException Cloner's
//  LocalWords:  SecurityException ReflectPermission cloneNotPublic
//  LocalWords:  suppressAccessChecks badTypeForClone notClonable
//  LocalWords:  noCloneViaReflection noCloneViaReflectionIA clonable
//  LocalWords:  noCloneViaReflectionITE resultClass superclass spath
//  LocalWords:  supertype subclasses runtime
