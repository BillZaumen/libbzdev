package org.bzdev.obnaming;
import org.bzdev.util.SafeFormatter;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
import org.bzdev.util.JSUtilities;

/**
 * The JSUtilities class contains inner classes and no
 * public methods.
 * These inner classes are specific to a particular
 * format (e.g. JSON).
 */
public class NJSUtilities {

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.obnaming.lpack.JSUtilities");

    // so a constructor is never called.
    private NJSUtilities() {}

    /**
     * Methods for parsing and generating JSON.
     * All the public methods of this class are static.
     * An extended syntax is used for parsing (but not for writing):
     * <UL>
     *   <LI> Comments consist of the sequence '//', not within a string,
     *        and contain all characters up to the end of a line.
     *   <LI> For a JSON object in a key/value pair, the quoting the
     *        key is optional when the key is syntactically a Java
     *        identifier.
     * </UL>
     * These extensions are useful when  a file containing JSON-formated
     * values is used as an input to a program. The object returned by the
     * parser can be used to configure a named-object factory (using the
     * syntax described in the documentation for {@link NamedObjectFactory}.
     */
    public static class JSON extends JSUtilities.JSON {

	/**
	 * Constructor.
	 */
	protected JSON() {}
	private static JSON json = new JSON();

	/**
	 * Write a JSON representation of an object.
	 * The type of the object may be {@link NamedObjectOps},
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}. For an instance of
	 * {@link NamedObjectOps}, the name of the object will be
	 * written as a JSON string.
	 * @param w a writer
	 * @param object the object to write
	 * @exception IOException an error occurred while writing
	 */
	public static void writeTo(Writer w, Object object)
	    throws IOException
	{
	    json.write(w, object);
	}

	protected void write(Writer w, Object object) throws IOException {
	    if (object != null && object instanceof NamedObjectOps) {
		super.write(w, ((NamedObjectOps) object).getName());
	    } else {
		super.write(w, object);
	    }
	}

	// Do a depth first traversal and replace each JSArray
	// an JSObject with the corresponding NJSArray and NJSObject.
	// Because this is used internally for parsing a text file,
	// we do not need to check for cycles and using constructor
	// that shares tables is OK because the objects returned by
	// the parser are never exposed unless returned by this
	// method.
	private static Object walk(Object object) {
	    if (object instanceof JSArray) {
		NJSArray jsa = new NJSArray((JSArray) object);
		int n = jsa.size();
		for (int i = 0; i < n; i++) {
		    Object obj = jsa.get(i);
		    if (obj instanceof JSArray
			|| obj instanceof JSObject) {
			jsa.set(i, walk(obj));
		    }
		}
		return jsa;
	    } else if (object instanceof JSObject) {
		NJSObject jso = new NJSObject((JSObject) object);
		for (String key: jso.keySet()) {
		    Object obj = jso.get(key);
		    if (obj instanceof JSArray
			|| obj instanceof JSObject) {
			jso.put(key, walk(obj));
		    }
		}
		return jso;
	    } else {
		return object;
	    }
	}

	/**
	 * JSON parser that reads JSON-formatted text from an
	 * {@link InputStream} given a {@link String} providing the
	 * name of a character set.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a JSON value)
	 * @param charsetName the name of the charset used by the input
	 *        stream
	 * @return the result of parsing
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 * @exception UnsupportedEncodingException the charsetName does
	 *            not match a known charset
	 */
	public static Object parse(InputStream is, String charsetName)
	    throws IOException, UnsupportedEncodingException
	{
	    return walk(JSUtilities.JSON.parse(is, charsetName));
	}

	/**
	 * JSON parser that reads JSON-formatted text from an
	 * {@link InputStream} given a {@link CharsetDecoder}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a JSON value)
	 * @param dec the charset decoder used to turn the
	 *        input stream into a sequence of characters
	 * @return the result of parsing
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, CharsetDecoder dec)
	    throws IOException
	{
	    return walk(JSUtilities.JSON.parse(is, dec));
	}

	/**
	 * JSON parser that reads JSON-formatted text from an
	 * {@link InputStream} given a {@link Charset}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a JSON value)
	 * @param cs the charset for the input stream
	 * @return the result of parsing
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, Charset cs)
	    throws IOException
	{
	    return walk(JSUtilities.JSON.parse(is, cs));
	}

	/**
	 * JSON parser that reads JSON-formated text from a character
	 * array.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param buffer the input character sequence  (containing
	 *        a JSON value)
	 * @return the result of parsing
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(char[] buffer) throws IOException {
	    return walk(JSUtilities.JSON.parse(buffer));
	}
	/**
	 * JSON parser that reads JSON-formated text from a {@link String}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param s the input string (containing a JSON value)
	 * @return the result of parsing
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(String s) throws IOException {
	    return walk(JSUtilities.JSON.parse(s));
	}

	/**
	 * JSON parser that reads JSON-formated text from a {@link Reader}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param r the reader
	 * @return the object.
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(Reader r) throws IOException {
	    return walk(JSUtilities.JSON.parse(r));
	}
    }
}

//  LocalWords:  exbundle JSUtilities JSON NamedObjectOps JSObject
//  LocalWords:  JSArray IOException Cntrl InputStream charsetName
//  LocalWords:  charset UnsupportedEncodingException CharsetDecoder
//  LocalWords:  charsetDecoder formated badComment needsDoubleQuote
//  LocalWords:  missingColon missingComma EOF hexDigit illegalEscape
//  LocalWords:  controlNotAllowed illformedNumber badNumber NJSArray
//  LocalWords:  expectingBoolean expectingNull illegalChar NJSObject
//  LocalWords:  NamedObjectFactory dec
