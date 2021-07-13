package org.bzdev.util;
import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Sort classes in an order such that for a class c appearing after a
 * different class c1, c1.isAssignableFrom(c) returns false.  In the
 * list created by this class, a subclass appears before its
 * superclasses and before the interfaces it implements, and an
 * interface appears before the interfaces it extends.
 * <P>
 * Note: when used with a security manager, this class needs the
 * runtime permission getClassLoader.  For a class loader, the only
 * method called is hashCode() and the class loaders are local
 * variables in a comparator.
 */
public class ClassSorter {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    static final int MAX_ENTRIES = (1<<15);

    static HashMap<Class<?>, Integer> classMap
	= new LinkedHashMap<Class<?>, Integer>(1024) {
	protected boolean removeEldestEntry(Map.Entry<Class<?>,Integer> eldest)
	{
	    return (size() > MAX_ENTRIES);
	}
    };
    static {
	classMap.put(Object.class, 0);
    }

    /**
     * Get the class depth - the maximum number of hops to Object or to
     * the furthest interface or superinterface.
     * @param clazz the class whose depth is to be determined
     * @return the class depth
     */
    public static synchronized int getClassDepth(Class<?> clazz) {
	Integer result = classMap.get(clazz);
	if (result == null) {
	    int count = 0;
	    Class<?> c = clazz.getSuperclass();
	    if (c != null) {
		count = 1 + getClassDepth(c);
	    } else {
		// because interfaces can be assigned to Object.
		count = 1;
	    }
	    for (Class<?> cc : clazz.getInterfaces()) {
		int x = 1 + getClassDepth(cc);
		if (count < x) count = x;
	    }
	    classMap.put(clazz, count);
	    result = count;
	}
	return result.intValue();
    }

    static Comparator<Class<?>> comparator = new Comparator<Class<?>>() {
	public int compare(final Class<?> c1, final Class<?> c2) {
	    if (c1.equals(c2)) return 0;
	    // special case - we want Integer to appear before Long
	    // and either before Double. This choice is arbitrary in
	    // terms of the class hierarchy as all are subclasses of
	    // Number.
	    if (c1.equals(Integer.class)) {
		if (c2.equals(Long.class) || c2.equals(Double.class)
		    || c2.equals(Float.class)) {
		    return -1;
		}
	    } else if (c1.equals(Long.class)) {
		if (c2.equals(Integer.class) || c2.equals(Float.class)) {
			return 1;
		} else if (c2.equals(Double.class)) {
		    return -1;
		}
	    } else if (c1.equals(Float.class)) {
		if (c2.equals(Integer.class)) {
		    return 1;
		} else if (c2.equals(Long.class) || c2.equals(Double.class)) {
		    return -1;
		}
	    } else if (c1.equals(Double.class)) {
		if (c2.equals(Integer.class) || c2.equals(Long.class)
		    || c2.equals(Float.class)) {
		    return 1;
		}
	    }
	    int x1 = getClassDepth(c1);
	    int x2 = getClassDepth(c2);
	    // reversed so that a class will be sorted into an order such that
	    // it appears before its superclasses or interfaces, and so that
	    // an interface appears before its superinterfaces.
	    if (x1 > x2) return -1;
	    if (x1 < x2) return 1;
	    // rest is an arbitrary ordering. It would be very unusual
	    // for the hash codes to match.
	    int h1 = c1.hashCode();
	    int h2 = c2.hashCode();
	    if (h1 < h2) return -1;
	    if (h1 > h2) return 1;
	    int result = c1.getName().compareTo(c2.getName());
	    if (result != 0) return result;
	    try {
		// For two classes to be different and for none of the tests
		// to detect the difference is an extremely rare case that
		// may never be seen in practice.  As a result, if the
		// attempt to access a classes class loader fails, we will
		// catch the exception and simply return 0.
		ClassLoader cl1 =
		    AccessController.doPrivileged
		    (new PrivilegedAction<ClassLoader>() {
			public ClassLoader run() {
			    return c1.getClassLoader();
			}
		    });
		ClassLoader cl2 =
		    AccessController.doPrivileged
		    (new PrivilegedAction<ClassLoader>() {
			public ClassLoader run() {
			    return c2.getClassLoader();
			}
		    });
		if (cl1 == cl2) return 0;
		if (cl1 == null) return -1;
		if (cl2 == null) return 1;
		if (cl1.equals(cl2)) return 0;
		h1 = cl1.hashCode();
		h2 = cl2.hashCode();
		if (h1 < h2) return -1;
		if (h1 > h2) return 1;
		Class<?> clc1 = cl1.getClass();
		Class<?> clc2 = cl2.getClass();
		h1 = clc1.hashCode();
		h2 = clc2.hashCode();
		if (h1 < h2) return -1;
		if (h1 > h2) return 1;
		return clc1.getName().compareTo(clc2.getName());
	    } catch (Exception e) {}
	    return 0;
	}
    };

    /**
     * Get the comparator used to compare classes
     * @return the comparator.
     */
    public static Comparator<Class<?>> getComparator() {
	return comparator;
    }

    PriorityQueue<Class<?>> pq = new PriorityQueue<>(11, comparator);
  
    /**
     * Add a class.
     * The queue of classes that were added will be cleared when
     * {@link #createList() createList} is called.
     * @param key the class to add
     * @exception IllegalArgumentException the argument was invalid (e.g.,
     *            null)
     */
    public void addKey(Class<?> key) {
	if (key == null) throw new IllegalArgumentException
			     (errorMsg("nullArgument"));
	pq.add(key);
    }

    /**
     * Create a list of classes ordered so that for a class c
     * appearing after a class c1, c1.isAssignableFrom(c) returns
     * false.
     * <P>
     * Note: once called, the classes added by calling addKey will
     * no longer be remembered.
     * @return the list
     */
    public LinkedList<Class<?>> createList() {
	LinkedList<Class<?>> list = new LinkedList<>();
	Class<?> element;
	while ((element = pq.poll()) != null) {
	    list.add(element);
	}
	return list;
    }
}

//  LocalWords:  exbundle isAssignableFrom superclasses runtime clazz
//  LocalWords:  getClassLoader hashCode comparator superinterface
//  LocalWords:  subclasses superinterfaces createList nullArgument
//  LocalWords:  IllegalArgumentException addKey
