package org.bzdev.obnaming;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
import java.util.LinkedHashMap;


/**
 * Simplified JavaScript-like object class.

 * Objects defined by scripting languages can be used to configure
 * instances of {@link NamedObjectFactory}.  For this use, the objects
 * contain properties but any methods explicitly defined for the
 * objects are ignored.  The types of the values that can be inserted
 * into this object are {@link NamedObjectOps}, {@link JSObject},
 * {@link JSArray},{@link Boolean}, {@link Number}, and {@link String}.
 * <P>
 * The class {@link NJSUtilities.JSON} can be used to create instances
 * of {@link NJSObject} and {@link NJSArray} by reading from various
 * sources, and instances of these two classes in turn can be used to
 * configure a named-object factory, which can then create a named
 * object.
 * <P>
 * This class is similar to a {@link java.util.Map}, but with some
 * run-time type checking. Entries in the map can be other instances
 * of {@link JSObject} (instances of {@link NJSObject} are preferred)
 * or instances of {@link JSArray} (instances of {@link NJSArray} are
 * preferred), allowing trees or directed graphs to be constructed.
 * Iterators will use the keys in the order in which they were
 * inserted.
 * @see NJSArray
 * @see NJSUtilities
 * @see NamedObjectFactory
 */
public class NJSObject extends JSObject {

    /**
     * Constructor.
     */
    public NJSObject() {
	super();
    }

    /**
     * Constructor sharing the same tables.
     * This is used by {@link org.bzdev.obnaming.NJSObject}.
     * @param base the JSObject whose tables should be used
     */
    protected NJSObject(JSObject base) {
	super(base);
    }

    /**
     * Insert a named object into this map.
     * @param key the key
     * @param object the object
     * @return the previous object; null if there isn't one
     */
    public Object put(String key, NamedObjectOps object) {
	return putObject(key, object);
    }

    /**
     * Put an object into this map.
     * The object may be null. If it is not null, it may be
     * an instance of {@link NamedObjectOps}, {@link String},
     * {@link Boolean}, {@link Number}, {@link JSArray}, or
     * {@link JSObject}.
     * @param key the key
     * @param object the object
     */
    public Object put(String key, Object object)
	throws IllegalArgumentException
    {
	if (object == null
	    || object instanceof NamedObjectOps
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return super.putObject(key, object);
	} else {
	    throw new IllegalArgumentException();
	}
    }
}

//  LocalWords:  NamedObjectFactory NamedObjectOps JSObject JSON
//  LocalWords:  NJSUtilities NJSObject NJSArray JSArray
