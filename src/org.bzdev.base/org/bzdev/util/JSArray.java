package org.bzdev.util;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.function.Consumer;

//@exbundle org.bzdev.util.lpack.JSUtilities

/**
 * Simplified JavaScript-like array (or list) class.
 * Formats such as JSON are syntactically similar to the source code
 * for JavaScript arrays and JavaScript objects that contain
 * properties but not methods, to the source code for JavaScript
 * arrays, and the source code for numbers, strings, boolean values,
 * and (of course) the value <code>null</code>.  The types of the
 * values that can be inserted into this object are {@link JSObject},
 * {@link JSArray}, {@link Boolean}, {@link Number}, and {@link String}.
 * <P>
 * The class {@link JSUtilities.JSON} can be used to create instances
 * of {@link JSObject} and {@link JSArray}, given a variety of
 * sources, and these instances can then be used as the input for
 * various computation.  <P>

 * This class is based on the {@link java.util.ArrayList} class, but
 * with some run-time type checking. Only a subset of the methods for
 * {@link java.util.ArrayList} are implemented.  In addition to
 * primitive values, entries in the list can be other instances of
 * this class or instances of {@link JSArray}, allowing trees or
 * directed graphs to be constructed. Iterators will list the values
 * in the order of their indices.
 * @see JSArray
 * @see JSUtilities
 */



public class JSArray implements Iterable<Object>, JSOps {

    static String errorMsg(String key, Object... args) {
	return JSUtilities.errorMsg(key, args);
    }

    private final long identity = nextIdentity();

    @Override
    public long identity() {return identity;}


    /**
     * Exception indicating that a JSArray could not be
     * converted to a {@link TemplateProcessor.KeyMapList}.
     */
    public class ConversionException extends IllegalStateException {
	/**
	 * Constructor.
	 * @param msg the exception's message
	 */
	protected ConversionException(String msg) {
	    super(msg);
	}
    }

    ArrayList<Object> list;
    /**
     * Constructor.
     */
    public JSArray() {
	super();
	list = new ArrayList<>();
    }

    /**
     * Constructor sharing the same tables.
     * This is used by {@link org.bzdev.obnaming.NJSArray}.
     * @param base the JSArray whose tables should be used
     */
    protected JSArray(JSArray base) {
	super();
	list = base.list;
    }

    /**
     * Add an instance of {@link JSObject}.
     * @param object the object to append to this array/list
     * @return {@code true} (as specified by {@link java.util.Collection#add}).
     */
    public boolean add(JSObject object) {
	return list.add(object);
    }

    /**
     * Add an instance of {@link JSArray}.
     * @param array the object to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean add(JSArray array) {
	return list.add(array);
    }

    /**
     * Add an instance of {@link Number}.
     * @param value the number to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean add(Number value) {
	return list.add(value);
    }
    
    /**
     * Add an instance of {@link String}.
     * @param string the string to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean add(String string) {
	return list.add(string);
    }

    /**
     * Add an instance of {@link Boolean}.
     * @param value the boolean value to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean add(Boolean value) {
	return list.add(value);
    }

    /**
     * Add any allowed object.
     * The object's type must be assignable
     * @param object the object to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     * @exception IllegalArgumentException if the object has the wrong type
     */
    public boolean add(Object object)
	throws IllegalArgumentException
    {
	if (object == null
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return list.add(object);
	} else {
	    String msg = errorMsg("illegalClass", object.getClass().getName());
	    throw new IllegalArgumentException(msg);
	}
    }

    /**
     * Add any allowed object.
     * This is used by {@link org.bzdev.obnaming.NJSArray}
     * and {@link org.bzdev.obnaming.ObjectNamerLauncher}.
     * @param object the object to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean addObject(Object object) {
	return list.add(object);
    }

    /**
     * Get the value stored in this object for a specified index.
     * @param index the index
     * @return the object stored at the specified index
     */
    public Object get(int index) {
	return list.get(index);
    }

    /**
     * Set any allowed object at a specified index.
     * This is used by {@link org.bzdev.obnaming.NJSArray}.
     * @param index the index
     * @param object the object to insert at the specified index
     * @return the previous object stored at the specified index
     * @exception IndexOutOfBoundsException if index &lt; 0
     *             or index &ge; size()
     * @exception IllegalArgumentException if the object has the wrong type
     */
    public Object set(int index, Object object)
	throws IllegalArgumentException, IndexOutOfBoundsException
       
    {
	if (object == null
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return list.set(index, object);
	} else {
	    String msg = errorMsg("illegalClass", object.getClass().getName());
	    throw new IllegalArgumentException(msg);
	}
    }

    /**
     * Set any object at a specified index.
     * This is used by {@link org.bzdev.obnaming.NJSArray}
     * and {@link org.bzdev.obnaming.ObjectNamerLauncher}.
     * @param index the index
     * @param object the object to insert at the specified index
     * @return the previous object stored at the specified index
     * @exception IndexOutOfBoundsException if index &lt; 0
     *             or index &ge; size()
     */
    public Object setObject(int index, Object object) {
	return list.set(index, object);
    }

    /**
     * Get the number of elements in this array.
     * @return the number of elements
     */
    public int size() {
	return list.size();
    }

    /**
     * Get a sequential stream with this array as its source.
     * @return a stream with this array as its source
     */
    public Stream<Object> stream() {
	return list.stream();
    }

    @Override
    public Iterator<Object> iterator() {
	return list.iterator();
    }

    @Override
    public Spliterator<Object> spliterator() {
	return list.spliterator();
    }

    /**
     * Convert this object to an array.
     * @return the array
     */
    public Object[] toArray() {
	return list.toArray();
    }


    /**
     * Convert this object to an array, storing it in an array with
     * a specified component type.
     * @param clasz the class of the array components
     * @return the array
     * @exception ArrayStoreException if the argument is not a superclass
     *         of the runtime type of this object's elements
     */
    public <T> T[] toArray(Class<T> clasz) {
	int sz = list.size();
	if (sz == 0) return (T[])Array.newInstance(clasz, 0);
	T[] array = (T[])Array.newInstance(clasz, sz);
	return list.toArray(array);
    }

    /**
     * Convert this object to an array of double.
     * @return the array
     * @exception IllegalStateException the conversion failed
     */
    public double[] toDoubleArray() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) return new double[0];
	double[] array = new double[sz];
	for (int i = 0; i < sz; i++) {
	    Object element = list.get(i);
	    if (element instanceof Number) {
		Number number = (Number) element;
		array[i] = number.doubleValue();
	    } else {
		throw new IllegalStateException(errorMsg("notNumber1", i));
	    }
	}
	return array;
    }


    /**
     * Convert this object to an array of long.
     * @return the array
     * @exception IllegalStateException the conversion failed
     */
    public long[] toLongArray() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) return new long[0];
	long[] array = new long[sz];
	for (int i = 0; i < sz; i++) {
	    Object element = list.get(i);
	    if (element instanceof Number) {
		Number number = (Number) element;
		array[i] = number.longValue();
	    } else {
		throw new IllegalStateException(errorMsg("notNumber1", i));
	    }
	}
	return array;
    }

    /**
     * Convert this object to an array of int.
     * @return the array
     * @exception IllegalStateException the conversion failed
     */
    public int[] toIntArray() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) return new int[0];
	int[] array = new int[sz];
	for (int i = 0; i < sz; i++) {
	    Object element = list.get(i);
	    if (element instanceof Number) {
		Number number = (Number) element;
		array[i] = number.intValue();
	    } else {
		throw new IllegalStateException(errorMsg("notNumber1", i));
	    }
	}
	return array;
    }

    /**
     * Convert this object to an array of boolean.
     * @return the array
     * @exception IllegalStateException the conversion failed
     */
    public boolean[] toBooleanArray() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) return new boolean[0];
	boolean[] array = new boolean[sz];
	for (int i = 0; i < sz; i++) {
	    Object element = list.get(i);
	    if (element instanceof Boolean) {
		Boolean b = (Boolean) element;
		array[i] = b.booleanValue();
	    } else {
		throw new IllegalStateException(errorMsg("notBoolean1", i));
	    }
	}
	return array;
    }


    /**
     * Convert this object to a matrix of type double.
     * A matrix is represented by a two-dimensional Java array where each
     * row has the same number of elements.
     * @return the matrix.
     * @exception IllegalStateException the conversion failed
     */
    public Object[][] toMatrix() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return new Object[0][0];
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return new Object[dim1][0];
	    }
	    Object[][] matrix = new Object[dim1][dim2];

	    for (int i = 0; i < dim1; i++) {
		Object object = list.get(i);
		if (object instanceof JSArray) {
		    JSArray row = (JSArray)object;
		    if (row.list.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    for (int j = 0; j < dim2; j++) {
			matrix[i][j] = row.list.get(j);
		    }
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Convert this object to an matrix, storing it in a matrix with
     * a specified component type.
     * The matrix is represented by a two-dimensional Java array and
     * all rows must have the same number of elements.
     * @param clasz the class of the matrix components
     * @return the array
     * @exception IllegalStateException the conversion failed
     */
    public <T> T[][] toMatrix(Class<T> clasz) throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return (T[][])Array.newInstance(clasz, 0, 0);
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return (T[][])Array.newInstance(clasz,dim1, 0);
	    }
	    T[][] matrix = (T[][])Array.newInstance(clasz, dim1, dim2);
	    int i = 0;
	    for (Object obj: list) {
		if (obj instanceof JSArray) {
		    JSArray row = (JSArray) obj;
		    if (row.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    row.list.toArray(matrix[i++]);
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Convert this object to a matrix whose components are of the primitive
     * type double.
     * A matrix is represented by a two-dimensional Java array where each
     * row has the same number of elements.
     * @return the matrix.
     * @exception IllegalStateException the conversion failed
     */
    public double[][] toDoubleMatrix() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return new double[0][0];
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return new double[dim1][0];
	    }
	    double[][] matrix = new double[dim1][dim2];

	    for (int i = 0; i < dim1; i++) {
		Object object = list.get(i);
		if (object instanceof JSArray) {
		    JSArray row = (JSArray)object;
		    if (row.list.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    for (int j = 0; j < dim2; j++) {
			Object element = row.list.get(j);
			if (element instanceof Number) {
			    Number value = (Number)element;
			    matrix[i][j] = value.doubleValue();
			} else {
			    String msg = errorMsg("notNumber2", i, j);
			    throw new IllegalStateException(msg);
			}
		    }
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Convert this object to a matrix whose components are of the primitive
     * type long.
     * A matrix is represented by a two-dimensional Java array where each
     * row has the same number of elements.
     * @return the matrix.
     * @exception IllegalStateException the conversion failed
     */
    public long[][] toLongMatrix() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return new long[0][0];
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return new long[dim1][0];
	    }
	    long[][] matrix = new long[dim1][dim2];

	    for (int i = 0; i < dim1; i++) {
		Object object = list.get(i);
		if (object instanceof JSArray) {
		    JSArray row = (JSArray)object;
		    if (row.list.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    for (int j = 0; j < dim2; j++) {
			Object element = row.list.get(j);
			if (element instanceof Number) {
			    Number value = (Number)element;
			    matrix[i][j] = value.longValue();
			} else {
			    String msg = errorMsg("notNumber2", i, j);
			    throw new IllegalStateException(msg);
			}
		    }
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Convert this object to a matrix whose components are of the primitive
     * type int.
     * A matrix is represented by a two-dimensional Java array where each
     * row has the same number of elements.
     * @return the matrix.
     * @exception IllegalStateException the conversion failed
     */
    public int[][] toIntMatrix() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return new int[0][0];
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return new int[dim1][0];
	    }
	    int[][] matrix = new int[dim1][dim2];

	    for (int i = 0; i < dim1; i++) {
		Object object = list.get(i);
		if (object instanceof JSArray) {
		    JSArray row = (JSArray)object;
		    if (row.list.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    for (int j = 0; j < dim2; j++) {
			Object element = row.list.get(j);
			if (element instanceof Number) {
			    Number value = (Number)element;
			    matrix[i][j] = value.intValue();
			} else {
			    String msg = errorMsg("notNumber2", i, j);
			    throw new IllegalStateException(msg);
			}
		    }
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Convert this object to a matrix whose components are of the primitive
     * type boolean.
     * A matrix is represented by a two-dimensional Java array where each
     * row has the same number of elements.
     * @return the matrix.
     * @exception IllegalStateException the conversion failed
     */
    public boolean[][] toBooleanMatrix() throws IllegalStateException {
	int sz = list.size();
	if (sz == 0) {
	    return new boolean[0][0];
	}
	int dim1 = sz;
	Object first = list.get(0);
	if (first instanceof JSArray) {
	    JSArray array2 = (JSArray) first;
	    int dim2 = array2.size();
	    if (dim2 == 0) {
		return new boolean[dim1][0];
	    }
	    boolean[][] matrix = new boolean[dim1][dim2];

	    for (int i = 0; i < dim1; i++) {
		Object object = list.get(i);
		if (object instanceof JSArray) {
		    JSArray row = (JSArray)object;
		    if (row.list.size() != dim2) {
			String msg = errorMsg("rowSz", i, dim2);
			throw new IllegalStateException(msg);
		    }
		    for (int j = 0; j < dim2; j++) {
			Object element = row.list.get(j);
			if (element instanceof Number) {
			    Boolean value = (Boolean)element;
			    matrix[i][j] = value.booleanValue();
			} else {
			    String msg = errorMsg("notBoolean2", i, j);
			    throw new IllegalStateException(msg);
			}
		    }
		} else {
		    String msg = errorMsg("notRow", i);
		    throw new IllegalStateException(msg);
		}
	    }
	    return matrix;
	} else {
	    String msg = errorMsg("notRow", 0);
	    throw new IllegalStateException(msg);
	}
    }


    /**
     * Get a possibly parallel stream with this array as its source.
     * @return a stream with this array as its source
     */
    public Stream<Object> parallelStream() {
	return list.parallelStream();
    }

    /**
     * Performs the given action for each element of the array until
     * all elements have been processed or the action throws an
     * exception.
     * Actions are performed in the order of iteration, if
     * that order is specified. Exceptions thrown by the action are
     * relayed to the caller.
     * <P>
     * Note: the description is copied from the {@link java.util.ArrayList}
     * documentation.
     * @param action the action to be performed for each element
     */
    public void forEach(Consumer<? super Object> action) {
	list.forEach(action);
    }

    /**
     * Get the value stored in this object for a specified index, cast
     * to a specified type.
     * @param index the index
     * @param clasz the class of the result
     * @return the object stored at the specified index
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int index, Class<T> clasz) {
	Object value = get(index);
	return (T) value;
    }

    /**
     * Convert this JSArray to a {@link TemplateProcessor.KeyMapList}.
     * The elements of this list are processed as follows:
     * <UL>
     *    <LI> if the element is an instance of {@link JSObject}, that
     *         instance is converted to a key map by using the method
     *         {@link JSObject#toKeyMap()}.
     *    <LI> if the element is an instance of {@link JSArray},
     *         the conversion proceeds as follows:
     *         <OL>
     *            <LI> A new key-map list will be created by calling
     *                 the element's method
     *                 <CODE>toKeyMapList(defaultKey)<CODE>,
     *                 where <CODE>defaultKey</CODE> is the argument to
     *                 this method.
     *            <LI> The element will be replaced with a new key map
     *                 that contains a single entry. That entry's key
     *                 is <CODE>defaultKey</CODE> and its value is the
     *                 new key-map list created in step 1.
     *            <LI> The modified element is added to the resulting
     *                 key-map list.
     *         </OL>
     *    <LI> if the element is not an instance of {@link JSObject}
     *         and not an instance of {@link JSArray}, a new key map
     *         will be added and a single entry will be added to that
     *         new key map with a key equal to the default key and its
     *         value equal to the element itself.
     * </UL>
     * There is one constraint: if the {@link JSArray} contains
     * another {@link JSArray}, all the elements that are not instances of
     * {@link JSObject} must be instances of {@link JSArray}.
     * <P>
     * This method is used internally for the case where an element in
     * a {@link JSArray} is another {@link JSArray}.
     * @param defaultKey the key to  use in single-entry key maps
     * @return a key-map list containing those list elements that are
     *         instances of JSObject
     * @exception ConversionException the list contained elements that
     *           are instances of {@link JSArray} and also elements that
     *           are not instances of {@link JSObject}
     * @see JSObject#toKeyMap()
     */
    public TemplateProcessor.KeyMapList toKeyMapList(String defaultKey)
	throws ConversionException
    {
	TemplateProcessor.KeyMapList klist =
	    new TemplateProcessor.KeyMapList();
	int i = 0;
	boolean sawJSArray = false;
	boolean sawPrimitive = false;
	for (Object object: this) {
	    if (object instanceof JSObject) {
		JSObject jso = (JSObject) object;
		klist.add(jso.toKeyMap());
	    } else if (object instanceof JSArray) {
		if (sawPrimitive) {
		    throw new ConversionException(errorMsg("cannotConvert", i));
		}
		JSArray jsa = (JSArray) object;
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		kmap.put(defaultKey, jsa.toKeyMapList(defaultKey));
		klist.add(kmap);
		sawJSArray = true;
	    } else {
		if (sawJSArray) {
		    throw new ConversionException(errorMsg("cannotConvert", i));
		}
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		kmap.put(defaultKey, object);
		klist.add(kmap);
		sawPrimitive = true;
	    }
	    i++;
	}
	return klist;
    }

    /**
     * Convert this JSArray to a {@link TemplateProcessor.KeyMapList}.
     * Elements in the list that are not instances of {@link JSObject}
     * are ignored.
     * @return a key-map list containing those list elements that are
     *         instances of JSObject.
     */
    public TemplateProcessor.KeyMapList toKeyMapList()
    {
	TemplateProcessor.KeyMapList klist =
	    new TemplateProcessor.KeyMapList();
	for (Object object: this) {
	    if (object instanceof JSObject) {
		JSObject jso = (JSObject) object;
		klist.add(jso.toKeyMap());
	    }
	}
	return klist;
    }
}

//  LocalWords:  JSON JSObject JSArray JSUtilities lt ge clasz msg OL
//  LocalWords:  IllegalArgumentException IndexOutOfBoundsException
//  LocalWords:  exbundle boolean TemplateProcessor KeyMapList rowSz
//  LocalWords:  toKeyMap toKeyMapList defaultKey ConversionException
//  LocalWords:  cannotConvert ArrayStoreException superclass runtime
//  LocalWords:  IllegalStateException notNumber notRow
