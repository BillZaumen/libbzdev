package org.bzdev.obnaming;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
import java.util.Collection;

/**
 * Simplified JavaScript-like array (or list) class.
 * This class extends {@link JSArray} so that it can
 * store named objects.
 * <P>
 * Objects defined by scripting languages can be used
 * to configure instances of {@link NamedObjectFactory}.
 * For this use, the configuration may include lists
 * of values.
 * The types of the values that can be inserted into
 * this object are {@link NamedObjectOps}, {@link JSObject},
 * {@link JSArray}, {@link Boolean}, {@link Number},
 * and {@link String}.
 * <P>
 * The class {@link NJSUtilities.JSON} can be used to create instances
 * of {@link NJSObject} and {@link NJSArray} by reading from various
 * sources, and instances of these two classes in turn can be used to
 * configure a named-object factory, which can then create a named
 * object.
 * <P>
 * This class is similar to an array list, but with some run-time type
 * checking. Entries in the list can be other instances of {@link JSArray}
 * (instances of {@link NJSArray} are preferred) or instances of
 * {@link JSObject} (instances of {@link NJSObject} are preferred), in
 * addition to strings, numbers, and boolean values, allowing trees or
 * directed graphs to be constructed. Iterators will list the values
 * in the order in which they were inserted.
 * @see NJSObject
 * @see NJSUtilities
 * @see NamedObjectFactory
 */
public class NJSArray extends JSArray {

    /**
     * Constructor.
     */
    public NJSArray() {
	super();
    }

    
    /**
     * Constructor sharing a JSArray's tables.
     * @param jsa the JSArray
     */
    protected NJSArray(JSArray jsa) {
	super(jsa);
    }

    /**
     * Add an instance of {@link NamedObjectOps}.
     * @param object the named object to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    public boolean add(NamedObjectOps object) {
	return super.addObject(object);
    }

    /**
     * Set any allowed object at a specified index.
     * This is used by {@link org.bzdev.obnaming.NJSArray}.
     * @param index the index
     * @param object the object to append to this array/list
     * @exception IndexOutOfBoundsException if index &lt; 0
     *             or index &ge; size()
     * @exception IllegalArgumentException if the object has the wrong type
     */
    public Object set(int index, Object object)
	throws IllegalArgumentException, IndexOutOfBoundsException
    {
	if (object == null
	    || object instanceof NamedObjectOps
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return super.setObject(index, object);
	} else {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Add any allowed object.
     * The object's type must be assignable
     * @param object the object to append to this array/list
     * @return {@code true} (as specified by {@link Collection#add}).
     */
    @Override
    public boolean add(Object object)
	throws IllegalArgumentException
    {
	if (object == null
	    || object instanceof NamedObjectOps
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return addObject(object);
	} else {
	    throw new IllegalArgumentException();
	}
    }
}

//  LocalWords:  JSArray NamedObjectFactory NamedObjectOps JSObject
//  LocalWords:  NJSUtilities JSON NJSObject NJSArray boolean jsa lt
//  LocalWords:  JSArray's IndexOutOfBoundsException ge
//  LocalWords:  IllegalArgumentException
