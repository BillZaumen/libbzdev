package org.bzdev.util;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

//@exbundle org.bzdev.util.lpack.JSUtilities

/**
 * Simplified JavaScript-like object class.
 * Formats such as JSON are syntactically similar to the source code
 * for JavaScript arrays and JavaScript objects that contain
 * properties but not methods, to the source code for JavaScript
 * arrays, and the source code for numbers, strings, boolean values,
 * and (of course) the value <code>null</code>.  The types of the
 * values that can be inserted into this object are {@link JSObject},
 * {@link JSArray}, {@link Boolean}, {@link Number}, and {@link String}.
 * the value <code>null</code>. This class represents an object
 * with values that are associated with keys.
 * <P>
 * The class {@link JSUtilities.JSON} can be used to
 * create instances of {@link JSObject} and {@link JSArray},
 * and instances of these two classes in turn can be used
 * to configure a named-object factory, which can then
 * create a named object.
 * <P>
 * This class is based on {@link java.util.Map} (a subset of that
 * {@link java.util.Map Map's} methods), but with some run-time type
 * checking. In addition to primitive types (boolean numbers, and
 * strings), entries in the map can be other instances of this class or
 * instances of {@link JSArray}, allowing trees or directed graphs to
 * be constructed. Iterators will provide the keys in the order in which
 * they were inserted.
 * @see JSArray
 * @see JSUtilities
 */
public class JSObject implements JSOps {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    private static final AtomicLong identityCounter = new AtomicLong();

    // used by JSOps so should not be used elsewhere
    static long computeNextIdentity() {
	return identityCounter.incrementAndGet();
    }

    private final long identity = nextIdentity();

    @Override
    public long identity() {return identity;}

    LinkedHashMap<String,Object>  map;

    /**
     * Constructor.
     */
    public JSObject() {
	super();
	map = new LinkedHashMap<>();
    }

    /**
     * Constructor sharing the same tables.
     * This is used by {@link org.bzdev.obnaming.NJSObject}.
     * @param base the JSObject whose tables should be used
     */
    protected JSObject(JSObject base) {
	super();
	map = base.map;
    }

    /**
     * Insert an instance of {@link JSObject} into this map.
     * @param key the key
     * @param object another instance of {@link JSObject} providing the
     *        value for the key
     * @return the previous object; null if there was none
     */
    public Object put(String key, JSObject object) {
	return map.put(key, object);
    }

    /**
     * Insert an instance of {@link JSArray} into this map.
     * @param key the key
     * @param array the value
     * @return the previous object; null if there was none
     */
    public Object put(String key, JSArray array) {
	return map.put(key, array);
    }

    /**
     * Insert a number into this map.
     * @param key the key
     * @param value the value
     * @return the previous object; null if there was none
     */
    public Object put(String key, Number value) {
	return map.put(key, value);
    }
    
    /**
     * Insert a string into this map.
     * @param key the key
     * @param string the string
     * @return the previous object; null if there was none
     */
    public Object put(String key, String string) {
	return map.put(key, string);
    }

    /**
     * Insert a boolean value into this map.
     * @param key the key
     * @param value the boolean value (true or false)
     * @return the previous object; null if there was none
     */
    public Object put(String key, Boolean value) {
	return map.put(key, value);
    }

    /**
     * Put an object into this map.
     * The object may be null. If it is not null, it may be an instance
     * of {@link String}, {@link Boolean}, {@link Number}, {@link JSArray},
     * or {@link JSObject}.
     * @param key the key
     * @param object the object
     * @return the previous object; null if there was none
     */
    public Object put(String key, Object object)
	throws IllegalArgumentException
    {
	if (object == null
	    || object instanceof String
	    || object instanceof Boolean
	    || object instanceof Number
	    || object instanceof JSArray
	    || object instanceof JSObject) {
	    return map.put(key, object);
	} else {
	    throw new IllegalArgumentException(errorMsg("unknownType"));
	}
    }

    /**
     * Insert any object.
     * When this method is used, the object may not be suitable
     * for use with YAML or JSON, and may not work properly with
     * {@link #toKeyMap()}.
     * @param key the key for this entry
     * @param object the object to store for the specified key
     * @return the previous object; null if there was none
     */
    public Object putObject(String key, Object object) {
	return map.put(key, object);
    }


    /**
     * Get the number of elements contained in this object.
     * @return the number of elements
     */
    public int size() {
	return map.size();
    }

    /**
     * Get a set of the keys for this object
     * @return a set of keys
     */
    public Set<String> keySet() {
	return map.keySet();
    }


    /**
     * Get an entry set for this object, each containing a key, value pair.
     * @return the entry set
     */
    public Set<Map.Entry<String,Object>> entrySet() {
	return map.entrySet();
    }

    /**
     * Determine if a key exists for this object.
     * @param key the key
     * @return true if the key has a value; false otherwise.
     */
    public boolean containsKey(String key) {
	return map.containsKey(key);
    }

    /**
     * Remove the value associated with a key
     * @param key the key
     * @return the value removed; null if there is none
     */
    public Object remove(String key) {
	return map.remove(key);
    }

    /**
     * Get the object associated with a key for this map.
     * @param key the key
     * @return the value for the specified key
     */
    public Object get(String key) {
	return map.get(key);
    }

    /**
     * Get the object associated with a key for this map, cast to a
     * specified type.
     * @param key the key
     * @param clasz the class of the object that will be
     *        returned.
     * @return the value for the specified key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clasz) {
	Object value = get(key);
	return (T) value;
    }

    /**
     * Create the key map equivalent to this object.
     * This method returns a key map containing the same keyword-value
     * pairs, but with values that are instances of JSArray converted
     * to key-map lists. If an element in the list is not an instance
     * of JSObject, that element is replaced with a key map containing
     * a single entry: one whose key is the key corresponding
     * to the JSArray, and whose value is the  list element.  If the
     * element in the list is a JSObject, that JSObject is converted to
     * a key list (by calling this method).
     * <P>
     * The single-entry maps are inserted in those cases where an
     * entry in a list would be another list.
     * <P>
     * If creating a JSON representation of this object, the key map
     * should be modified to properly quote strings.
     * @return the key map
     * @exception JSArray.ConversionException a {@link JSArray}
     *  contained another {@link JSArray}
     * @see JSArray.toKeyMapList(String)
     */
    public TemplateProcessor.KeyMap toKeyMap()
	throws JSArray.ConversionException
    {
	TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	kmap.putAll(map);
	for (Map.Entry<String,Object> entry: kmap.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value == null) {
		kmap.put(key, new TemplateProcessor.KeyMap());
	    }
	    if (value instanceof JSArray) {
		JSArray array = (JSArray) value;
		kmap.put(key, array.toKeyMapList(key));
	    }
	}
	return kmap;
    }
}


//  LocalWords:  JSON boolean JSObject JSArray JSUtilities clasz
//  LocalWords:  ConversionException toKeyMapList
