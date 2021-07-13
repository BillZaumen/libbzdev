package org.bzdev.util;
import java.util.*;
import java.lang.reflect.*;
import java.security.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Sort class arrays in a standard order.
 * The ordering is such that for each array index, a given class will
 * appear before its superclasses and before the interfaces it implements.
 * Similarly interfaces will appear before the interfaces they extend.
 * Arrays that contain the same class for array index 0 will be adjacent
 * in the sorted order.  For an index j, if arrays are selected so that
 * the classes for each index smaller than j match, and if these arrays
 * are kept in the same order, then for these selected arrays, all arrays
 * with the same class at index j will be adjacent.
 */
public class ClassArraySorter {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    static Comparator<Class<?>> classComparator = ClassSorter.getComparator();

    static Comparator<Key> comparator = new Comparator<Key>() {
	public int compare(Key key1, Key key2) {
	    Class<?>[]ca1 = key1.value;
	    Class<?>[]ca2 = key2.value;
	    int length = ca1.length;
	    if (key1.varargs && key2.varargs) {
		if (ca1.length < ca2.length) {
		    // want longest match first
		    return 1;
		} else if (ca1.length > ca2.length) {
		    return -1;
		} else {
		    int lenm1 = length - 1;
		    for (int i = 0; i < lenm1; i++) {
			int result = classComparator.compare(ca1[i], ca2[i]);
			if (result != 0) return result;
		    }
		    return classComparator
			.compare(ca1[lenm1].getComponentType(),
				 ca2[lenm1].getComponentType());
		}
	    } else if (key1.varargs) {
		// fixed number of args before varargs
		return 1;
	    } else if (key2.varargs) {
		return -1;
	    } else if (length != ca2.length) {
		throw new IllegalArgumentException
		    (errorMsg("argArrayLengthsDiffer"));
	    }
	    for (int i = 0; i < length; i++) {
		int result = classComparator.compare(ca1[i], ca2[i]);
		if (result != 0) return result;
	    }
	    return 0;
	}
    };

    /**
     * Get the comparator used to compare class arrays.
     * @return the comparator.
     */
    public static Comparator<Key> getComparator() {
	return comparator;
    }

    PriorityQueue<Key> pq = new PriorityQueue<>(11, comparator);
    
    /**
     * Add a Key.
     * The queue of Keys that were added will be cleared when
     * {@link #createList() createList} is called.
     * @param key the Key to add
     * @exception IllegalArgumentException the argument was invalid (e.g.,
     *            null)
     */
    public void addKey(Key key) {
	if (key == null)
	    throw new IllegalArgumentException(errorMsg("nullArgument"));
	pq.add(key);
    }

    /**
     * Create a list of class arrays ordered so that for a class array ca
     * appearing after a class array ca1, then for all indices i such that
     * ca[j] = ca1[j] when j &lt; i, ca1[i].isAssignableFrom(ca[i]) returns
     * false unless ca[i] = ca1[i].
     * <P>
     * Note: once called, the class arrays added by calling addKey will
     * no longer be remembered.
     * @return the list
     */
    public LinkedList<Key> createList() {
	return createList(false);
    }
    /**
     * Create a list of class arrays ordered so that for a class array ca
     * appearing after a class array ca1, then for all indices i such that
     * ca[j] = ca1[j] when j &lt; i, ca1[i].isAssignableFrom(ca[i]) returns
     * false unless ca[i] = ca1[i], optionally remembering the list.
     * <P>
     * Note: once called, the class arrays added by calling addKey
     * will no longer be remembered if the argument has the value
     * <CODE>false</CODE>.
     * @param remember true if the list should be remembered; false otherwise
     * @return the list
     */
    public LinkedList<Key> createList(boolean remember) {
	LinkedList<Key> list = new LinkedList<>();
	Key element;
	if (remember) {
	    PriorityQueue<Key>newpq = new
		PriorityQueue<>(pq.size()*2 + 11, comparator);
	    while ((element = pq.poll()) != null) {
		list.add(element);
		newpq.add(element);
	    }
	    pq = newpq;
	} else {
	    while ((element = pq.poll()) != null) {
		list.add(element);
	    }
	}
	return list;
    }

    /**
     * Constant used to denote that an argument count array entry should
     * be ignored.
     * An argument-count array is used by the {@link Key} supplied as
     * an argument to {@link Key#isAssignableFrom(Key)} or
     * {@link Key#isAssignableFrom(Key,boolean)}, and is provided by
     * the constructors
     * {@link Key#Key(Class[],boolean,int[])}, and
     * {@link Key#Key(Class[],boolean,int[],ArgCountMap[])}.
     * @see Key#Key(Class[],boolean,int[])
     * @see Key#Key(Class[],boolean,int[],ArgCountMap[])
     * @see Key#isAssignableFrom(Key)
     * @see Key#isAssignableFrom(Key,boolean)
     */
    public static final int NO_ARGCOUNT_TEST = -1;

    /**
     * Constant used to denote that an argument count array entry should
     * match any interface.
     * An argument-count array is used by the {@link Key} supplied as
     * an argument to {@link Key#isAssignableFrom(Key)} or
     * {@link Key#isAssignableFrom(Key,boolean)}, and is provided by
     * the constructors
     * {@link Key#Key(Class[],boolean,int[])}, and.
     * {@link Key#Key(Class[],boolean,int[],ArgCountMap[])}.
     * @see Key#Key(Class[],boolean,int[])
     * @see Key#Key(Class[],boolean,int[],ArgCountMap[])
     * @see Key#isAssignableFrom(Key)
     * @see Key#isAssignableFrom(Key,boolean)
     */
    public static final int INTERFACE_TEST = -2;

    /**
     * Table mapping strings to integers.
     * This class is a trivial extension of the class
     * {@link HashMap HashMap<String,Integer>}, and is provided
     * because of Java-related issues with creating arrays of generic
     * classes.
     */
    public static class ArgCountMap extends HashMap<String,Integer> {
	/**
	 * Constructor.
	 */
	public ArgCountMap() {
	    super();
	}

	/**
	 * Constructor given an initial capacity.
	 * @param initialCapacity the initial capacity of this map
	 */
	public ArgCountMap(int initialCapacity) {
	    super(initialCapacity);
	}

	/**
	 * Constructor given an initial capacity and load factor.
	 * When the number of entries exceeds the product of the
	 * load factor and the initial capacity, the table is
	 * resized.
	 * @param initialCapacity the initial capacity of this map
	 * @param loadFactor the load factor for this map
	 */
	public ArgCountMap(int initialCapacity, float loadFactor) {
	    super(initialCapacity, loadFactor);
	}

    }

    /**
     * Class providing a key representing an array of classes.
     * Keys used for entries in a {@link ClassArraySorter} will
     * typically be created using the constructor
     * {@link ClassArraySorter.Key#Key(Class[])}.  The remaining constructors
     * provide keys that may be matched against those in the sorter.
     */
    public static class Key {
	Class<?>[] value;
	int argcount[] = null;
	boolean varargs = false;
	ArgCountMap objectMaps[] = null;

	/**
	 * Constructor.
	 * @param types an array of classes that the key represents
	 */
	public Key(Class<?>[] types) {
	    value = new Class[types.length];
	    System.arraycopy(types, 0, value, 0, types.length);
	    for (int i = 0; i < types.length; i++) {
	      if (value[i] == null)
		  throw new IllegalArgumentException
		      (errorMsg("nullValueAtIndex", i));
	    }
	}

	/**
	 * Indicate that this key represents a parameter list for
	 * a method with a variable number of arguments.
	 * When called, the key must contain at least one class in its
	 * array and the last element must be an array.
	 * @exception IllegalStateException the key is not compatible
	 *            with a parameter list for a method or constructor
	 *            that takes a variable number of arguments
	 */
	public void varargsMode() throws IllegalStateException {
	    int len = value.length;
	    if (len == 0 || !(value[len-1].isArray())) {
		throw new IllegalStateException(errorMsg("notVarargs"));
	    }
	    varargs = true;
	}

	/**
	 * Constructor with possibly null array entries.
	 * If one of the types is null, that entry is assumed to match
	 * any class when the key is passed as an argument to
	 * {@link #isAssignableFrom(Key)} or
	 * {@link #isAssignableFrom(Key,boolean)}.
	 * @param types an array of classes that the key represents
	 * @param allowNull true if null values are allowed in
	 *        the types array
	 */
	public Key(Class<?>[] types, boolean allowNull) {
	    value = new Class[types.length];
	    System.arraycopy(types, 0, value, 0, types.length);
	    for (int i = 0; i < types.length; i++) {
	      if (allowNull == false && value[i] == null)
		  throw new IllegalArgumentException
		      (errorMsg("nullValueAtIndex", i));
	    }
	}

	/**
	 * Constructor with possibly null array entries and an argument-count
	 * array, but no object-map array.
	 * If one of the types in the types array is null, that entry
	 * is assumed to match any class when the key is passed as an
	 * argument to {@link #isAssignableFrom(Key)} or
	 * {@link #isAssignableFrom(Key,boolean)}. The parameter argcount
	 * is an array with the same length as the array passed in the
	 * types argument. When an element of the argcount array has a
	 * non-negative value and the key is used as an argument for
	 * {@link #isAssignableFrom(Key)} or
	 * {@link #isAssignableFrom(Key,boolean)},
	 * and a match for a specific element in the array would fail,
	 * an alternative test is used.  For the expression
	 * key1.isAssignableFrom(key2) key1.isAssignableFrom(key2, true),
	 * or key1.isAssignableFrom(key2, false), let c1 be an element in
	 * key1's class array and let c2 be the corresponding element in
	 * key2's class array, and let cnt be the corresponding value of key2's
	 * argcount array. If c1.isAssignableFrom(c2) is true, the entries
	 * match.  If c1.isAssignableFrom(c2) is false, there are several
	 * cases:
	 * <UL>
	 *   <LI> if key2's argcount array entry corresponding to e2 has
	 *        a non-negative value, then the
	 *        element also matches if c1 is a functional interface (i.e.,
	 *        has the annotation {@link FunctionalInterface}, and has a
	 *        public instance method that is not a default method
	 *        and whose number of arguments is cnt.
	 *   <LI> if key2's argcount array entry corresponding to c2 has the
	 *        value {@link ClassArraySorter#INTERFACE_TEST}, an error
	 *        will occur with this constructor as an object map is
	 *        required for this case.
	 *   <LI> if key2's argcount array entry corresponding to e2 has the
	 *        value {@link ClassArraySorter#NO_ARGCOUNT_TEST}, then
	 *        the elements do not match.
	 * </UL>
	 * @param types an array of classes that the key represents
	 * @param allowNull true if null values are allowed in
	 *        the types array
	 * @param argcount the argument-count array
	 * @exception IllegalArgumentException an argument was not
	 *            consistent with this constructor.
	 */
	public Key(Class<?>[] types, boolean allowNull, int[] argcount)
	    throws IllegalArgumentException
	{
	    this(types, allowNull, argcount, null);
	}

	/**
	 * Constructor with possibly null array entries, an argument-count
	 * array, and an object-map array.
	 * If one of the types in the types array is null, that entry
	 * is assumed to match any class when the key is passed as an
	 * argument to {@link #isAssignableFrom(Key)} or
	 * {@link #isAssignableFrom(Key,boolean)}. The parameter argcount
	 * is an array with the same length as the array passed in the
	 * types argument. When an element of the argcount array has a
	 * non-negative value and the key is used as an argument for
	 * {@link #isAssignableFrom(Key)} or
	 * {@link #isAssignableFrom(Key,boolean)},
	 * and a match for a specific element in the array would fail,
	 * an alternative test is used.  For the expression
	 * key1.isAssignableFrom(key2) key1.isAssignableFrom(key2, true),
	 * or key1.isAssignableFrom(key2, false), let c1 be and element in
	 * key1's class array and let c2 be an element in key2's class
	 * array, and let cnt be the corresponding value of key2's
	 * argcount array.  If c1.isAssignableFrom(c2) is true, the entries
	 * match.  If c1.isAssignableFrom(c2) is false, there are several
	 * cases:
	 * <UL>
	 *   <LI> if key2's argcount array entry corresponding to e2 has
	 *        a non-negative value, then the
	 *        element also matches if c1 is a functional interface (i.e.,
	 *        has the annotation {@link FunctionalInterface}, and has a
	 *        public instance method that is not a default method
	 *        and whose number of arguments is cnt.
	 *   <LI> if key2's argcount array entry corresponding to c2 has the
	 *        value {@link ClassArraySorter#INTERFACE_TEST}, and c1 is an
	 *        interface and then the supplied map, if any, is tested against
	 *        the interface's public, non-default methods. If the
	 *        value in the map for such a method's name does not matches the
	 *        number of arguments that method has, the test fails.
	 *        Otherwise the test fails.
	 *   <LI> if key2's argcount array entry corresponding to e2 has the
	 *        value {@link ClassArraySorter#NO_ARGCOUNT_TEST}, then
	 *        the elements do not match.
	 * </UL>
	 * @param types an array of classes that the key represents
	 * @param allowNull true if null values are allowed in
	 *        the types array
	 * @param argcount the argument-count array
	 * @param objectMaps the object-map array, each entry of which
	 *        maps the names of methods to the expected argument count
	 *        for that method.
	 * @exception IllegalArgumentException an object map was required
	 *            but not provided or the argument allowNull was false
	 *            while one of the elements in the types argument was
	 *            null
	 */
	public Key(Class<?>[] types, boolean allowNull, int[] argcount,
		   ArgCountMap[] objectMaps)
	    throws IllegalArgumentException
	{

	    value = new Class[types.length];
	    System.arraycopy(types, 0, value, 0, types.length);
	    for (int i = 0; i < types.length; i++) {
		if (argcount != null && argcount[i] == INTERFACE_TEST
		    && objectMaps == null) {
		  throw new IllegalArgumentException
		      (errorMsg("nullObjectMap", i));
		}
	      if (allowNull == false && value[i] == null)
		  throw new IllegalArgumentException
		      (errorMsg("nullValueAtIndex", i));
	    }
	    this.argcount = argcount;
	    this.objectMaps = objectMaps;
	}

	/**
	 * Change primitive types to their corresponding subclasses
	 * of {@link Number}.
	 */
	public void promotePrimitives() {
	    for (int i = 0; i < value.length; i++) {
		if (value[i].equals(double.class)) {
		    value[i] = Number.class;
		} else if (value[i].equals(int.class)) {
		    value[i] = Number.class;
		} else if (value[i].equals(long.class)) {
		    value[i] = Number.class;
		}
	    }
	}

	/**
	 * Get the number of classes in this key.
	 * @return the number of classes in this key
	 */
	public int size() {
	    return value.length;
	}

	/**
	 * Convert this key to an array
	 * @return an array containing classes in the same order in which
	 *         they were entered
	 */
	public Class<?>[] toArray() {
	    return toArray(new Class<?>[value.length]);
	}

	/**
	 * Convert this key to an array given an existing array.
	 * If the existing array is as long as or longer than the
	 * array that is needed, that array will be used.  Otherwise a
	 * new array will be allocated.
	 * @return an array containing classes in the same order in which
	 *         they were entered
	 */
	public Class<?>[] toArray(Class<?>[] array) {
	    if (array.length < value.length) {
		array = new Class<?>[value.length];
	    }
	    System.arraycopy(value, 0, array, 0, value.length);
	    for (int i = value.length; i < array.length; i++) {
		array[i] = null;
	    }
	    return array;
	}

	@Override
	public boolean equals(Object obj) {
	    if (! (obj instanceof Key)) return false;
	    Key key = (Key) obj;
	    if (value.length != key.value.length) {
		return false;
	    }
	    for (int i = 0; i < value.length; i++) {
		if (!value[i].equals(key.value[i])) return false;
	    }
	    return (varargs == key.varargs);
	}

	/**
	 * Check that each class in this Key is assignable from the
	 * corresponding class in the argument Key.
	 * @param key the argument key
	 */
	public boolean isAssignableFrom(Key key) {
	    return isAssignableFrom(key, false);
	}

	private boolean isAssignableTest(Class<?> ifclass,
					 ArgCountMap objectMap)
	{
	    // So we can match the type of a scripting-language object
	    // to an interface by making sure the methods agree - smae
	    // name and same number of arguments.
	    if (!ifclass.isInterface()) return false;
	    try {
		boolean ok = AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Boolean>() {
			    public Boolean run()
				throws SecurityException
			    {
				for (Method m: ifclass.getMethods()) {
				    if (m.isDefault()) continue;
				    if (m.isVarArgs()) continue;
				    if (m.isSynthetic()) continue;
				    String name = m.getName();
				    int acnt = m.getParameterCount();
				    if (!objectMap.containsKey(name)
					|| objectMap.get(name) != acnt) {
					return false;
				    }
				}
				return true;
			    }
			});
		return ok;
	    } catch(PrivilegedActionException ep) {
		Throwable ex = ep.getCause();
		if (ex instanceof RuntimeException) {
		    throw (RuntimeException) ex;
		}
	    }
	    return false;
	}

	/**
	 * Check that each class in this Key is assignable from the
	 * corresponding class in the argument Key after a permissible cast.
	 * If this key represents the parameters for a method with a
	 * variable number of arguments, the argument key should provide
	 * the classes for the arguments passed in a method call.
	 * @param key the argument key
	 * @param cast true if the argument key's numerical types should be
	 *        cast to this key's type.
	 */
	public boolean isAssignableFrom(Key key, boolean cast) {
	    Class<?>[] classArray = key.value;
	    if (varargs) {
		if (key.value.length < value.length - 1) {
		    return false;
		}
	    } else if (value.length != key.value.length) {
		return false;
	    }
	    int imax = value.length - (varargs? 1: 0);
	    for (int i = 0; i < imax; i++) {
		if (key.value[i] == null) {
		    // treat a null class as a null value with the
		    // desired type
		    continue;
		}
		if (!value[i].isAssignableFrom(key.value[i])) {
		    if (key.argcount != null) {
			if (key.argcount[i] == INTERFACE_TEST) {
			    if (value[i].isInterface()) {
				if (isAssignableTest(value[i],
						     key.objectMaps[i])) {
				    continue;
				}
			    }
			    return false;
			} else if (key.argcount[i] >= 0) {
			    if (value[i].getDeclaredAnnotation
				(FunctionalInterface.class) != null) {
				final int ii = i;
				Method mr = null;
				try {
				    mr = AccessController.doPrivileged
				    (new PrivilegedExceptionAction<Method>() {
					    public Method run()
						throws SecurityException
					    {
						for (Method m: value[ii]
							 .getDeclaredMethods())
						{
						    if (m.isVarArgs()) continue;
						    if (m.isDefault()) continue;
						    if (m.isSynthetic())
							continue;
						    int mods = m.getModifiers();
						    if ((mods
							 & Modifier.STATIC) == 0
							&& (mods
							    & Modifier.PUBLIC)
							!= 0
							&& (key.argcount[ii]
							    ==
							    m.getParameterCount
							    ())) {
							return  m;
						    }
						}
						return null;
					    }
					});
				} catch(PrivilegedActionException ep) {
				    Throwable ex = ep.getCause();
				    if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				    }
				}
				/*
				Method mr = null;
				for (Method m: value[i].getDeclaredMethods()) {
				    if (m.isVarArgs()) continue;
				    if (m.isDefault()) continue;
				    if (m.isSynthetic()) continue;
				    int mods = m.getModifiers();
				    if ((mods & Modifier.STATIC) == 0
					&& (mods & Modifier.PUBLIC) != 0
					&& (key.argcount[i]
					    == m.getParameterCount())) {
					mr = m;
					break;
				    }
				}
				*/
				if (mr != null) continue;

			    }
			    return false;
			} else {
			    return false;
			}
		    }
		    if (cast) {
			if (value[i].equals(Double.class)) {
			    if (key.value[i].equals(Long.class)
				|| key.value[i].equals(Float.class)
				|| key.value[i].equals(Integer.class)) {
				continue;
			    }
			} else if (value[i].equals(Float.class)) {
			    if (key.value[i].equals(Integer.class)) {
				continue;
			    }
			} else if (value[i].equals(Long.class)) {
			    if (key.value[i].equals(Integer.class)) {
				continue;
			    }
			}
		    }
		    return false;
		}
	    }
	    if (varargs) {
		int keylen = key.value.length;
		if (keylen == imax) {
		    return true; // case where there are no extra args
		}
		if (keylen == value.length
		    && value[imax].isAssignableFrom(key.value[imax])) {
		    // final arg is an array with the correct type
		    return true;
		}
		Class<?> ctype = value[imax].getComponentType();
		// repeat the previous test by using ctype instead of
		// value[i] for the remainder of the argument key's values
		for (int i = imax; i < key.value.length; i++) {
		    if (key.value[i] == null) {
			// treat a null class as a null value with the
			// desired type
			continue;
		    }
		    if (!ctype.isAssignableFrom(key.value[i])) {
			if (key.argcount != null) {
			    if (key.argcount[i] == INTERFACE_TEST) {
				if (ctype.isInterface()) {
				    continue;
				}
				return false;
			    } else if (key.argcount[i] >= 0) {
				if (ctype.getDeclaredAnnotation
				    (FunctionalInterface.class) != null) {
				    Method mr = null;
				    for (Method m: ctype.getDeclaredMethods()) {
					if (m.isVarArgs()) continue;
					if (m.isDefault()) continue;
					if (m.isSynthetic()) continue;
					int mods = m.getModifiers();
					if ((mods & Modifier.STATIC) == 0
					    && (mods & Modifier.PUBLIC) != 0
					    && (key.argcount[i]
						== m.getParameterCount())) {
					    mr = m;
					    break;
					}
				    }
				    if (mr != null) continue;
				}
				return false;
			    }
			}
			if (cast) {
			    if (ctype.equals(Double.class)
				|| ctype.equals(double.class)) {
				if (key.value[i].equals(Number.class)
				    || key.value[i].equals(Long.class)
				    || key.value[i].equals(Float.class)
				    || key.value[i].equals(Integer.class)) {
				    continue;
				}
			    } else if (ctype.equals(Float.class)
				       || ctype.equals(float.class)) {
				if (key.value[i].equals(Integer.class)) {
				    continue;
				}
			    } else if (ctype.equals(Long.class)
				       || ctype.equals(long.class)) {
				if (key.value[i].equals(Integer.class)) {
				    continue;
				}
			    } else if (ctype.equals(int.class)) {
				if (key.value[i].equals(Integer.class)) {
				    continue;
				}
			    }
			}
			return false;
		    }
		}
	    }
	    return true;
	}

	@Override
	public int hashCode() {
	    return Arrays.hashCode(value);
	}

	public String toString() {
	  StringBuilder sb = new StringBuilder(128);
	  sb.append("[");
	  for (int i = 0; i < value.length; i++) {
	    if (i > 0) sb.append(", ");
	    sb.append(value[i].toString());
	  }
	  sb.append("]");
	  return sb.toString();
	}
    }
}

//  LocalWords:  exbundle superclasses argArrayLengthsDiffer lt cnt
//  LocalWords:  comparator createList IllegalArgumentException
//  LocalWords:  nullArgument isAssignableFrom addKey boolean
//  LocalWords:  nullValueAtIndex allowNull argcount
//  LocalWords:  FunctionalInterface
