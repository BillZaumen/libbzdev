package org.bzdev.util;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.SafeFormatter;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

//@exbundle org.bzdev.util.lpack.JSUtilities

/**
 * This class provides operations that allow JSObject and JSArray
 * objects to be written to an output stream in JSON format or
 * to be generated from a string or character stream.
 * <P>
 * The JSUtilities class contains inner classes and no
 * public methods.
 * These inner classes are specific to a particular
 * format (e.g. JSON and YAML).
 */
public class JSUtilities {

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.util.lpack.JSUtilities");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static class JSException extends IOException {
	long lineno;

	JSException(long lineno) {
	    super();
	    this.lineno = lineno;
	}

	JSException(long lineno, String message) {
	    super(message);
	    this.lineno = lineno;
	}

	JSException(long lineno, String message,
		    Throwable cause) {
	    super(message, cause);
	    this.lineno = lineno;
	}

	JSException(long lineno, Throwable cause) {
	    super(cause);
	    this.lineno = lineno;

	}

	public String getMessage() {
	    return super.getMessage() + " " + errorMsg("lines", lineno);
	}
    }

    // so a constructor is never called.
    private JSUtilities() {}

    /**
     * Location in a JSON or YAML nested series of arrays and objects.
     * This will typically be used in conjunction with a hash table
     * mapping locations to line and column numbers.
     * <P>
     * Each location is represented a sequence of strings and integers,
     * where a string represents a {@link JSObject} key and an integer
     * represents a {@link JSArray} element, numbered from 1 (instead of
     * from 0). The elements are in the order in which JSObjects and
     * JSArrays are nested, starting from the top-level object.
     * <P>
     * To create a location, use thean instance of {@link Locator}
     * while traversing a tree consisting of nested instance of
     * {@link JSArray} and {@link JSObject}.
     * @see Locator
     */
    public static class Location {
	Object[] objects;

	/**
	 * Constructor.
	 * @param locator a {@link Locator}
	 */
	public Location(Locator locator) {
	    objects = locator.locator();
	}

	@Override
	public boolean  equals(Object o) {
	    if (o instanceof Location) {
		Location other = (Location) o;
		return Arrays.deepEquals(objects, other.objects);
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    return Arrays.deepHashCode(objects);
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[" + getClass().getName() + ": ");
	    boolean first = true;
	    for (Object o: objects) {
		if (first) {
		    first = false;
		} else {
		    sb.append(", ");
		}
		sb.append(o);
	    }
	    sb.append("]");
	    return sb.toString();
	}

    }

    /**
     * Class to represent a line-number, column-number pair.
     * @see Location
     * @see Locator
     * @see JSON.Parser#getLocationMap()
     */
    public static class LocationPair {
	long lineno;
	int column;

	/**
	 * Constructor.
	 * @param lineno the line number
	 * @param column the column number
	 */
	LocationPair(long lineno, int column) {
	    this.lineno = lineno;
	    this.column = column;
	}

	/**
	 * Get the line number.
	 * @return the line number
	 */
	public long getLineNumber() {return lineno;}

	/**
	 * Get the column number.
	 * @return the column number
	 */
	public int getColumn() {return column;}
    }

    /**
     * Determines a location in nested instances of {@link JSObject}
     * and {@link JSArray}.
     * To capture a location, set the locator and then use the
     * constructor {@link Location#Location(Locator)}.
     * <P>
     * When parsing or traversing a tree of nested instances of
     * {@link JSObject} and {@link JSArray}, one will call
     * {@link Locator#pushLevel()} when starting to process an object
     * and {@link Locator#popLevel()} when done. The method
     * {@link Locator#setKey(String)} for each property name (or key) in a
     * {@link JSObject}, and {@link Locator#incrList()} is called whenever
     * moving to the next element in a {@link JSArray}.
     * For a parser, each time the location changes one should use the
     * constructor {@link Location#Location(Locator)} to capture the
     * current location and add a map entry mapping that location to
     * an instance of {@link LocationPair} that contains the corresponding
     * line number and column number.
     * @see Location
     * @see JSON.Parser#getLocationMap()
     */
    public static class Locator {
	int level = -1;
	ArrayList<Integer> tags = new ArrayList<>();
	ArrayList<String> keys = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public Locator() {}

	/**
	 * Get current nesting depth.
	 * The value is the sum of the number of keys and indexes
	 * at the current point in nested instances of {@link JSObject}
	 * and {@link JSArray}.
	 * @return the nesting depth
	 */
	public int size() {
	    return tags.size();
	}

	/**
	 * Add the nest nesting level.
	 */
	public void pushLevel() {
	    level++;
	    tags.add(0);
	    keys.add(null);
	}

	/**
	 * Add remove the deepest nesting level.
	 */
	public void popLevel() {
	    tags.remove(level);
	    keys.remove(level);
	    level--;
	}

	/**
	 * Increment the index at the current nesting level
	 * and remove any key that was set at this level.
	 */
	public void incrList() {
	    tags.set(level, 1+tags.get(level));
	    keys.set(level, null);
	}

	/*
	 * Increment the number of tags at the current nesting level
	 * and remove any key that was set at this level.
	public void incrDecr() {
	    tags.set(level, tags.get(level) - 1);
	    keys.set(level, null);
	}
	 */

	/**
	 * Set the key for the current nesting level.
	 */
	public void setKey(String value) {
	    keys.set(level, value);
	}

	// Used by the Location constructor
	Object[] locator() {
	    Object[] results = new Object[tags.size()];
	    for(int i = 0; i < tags.size(); i++) {
		int index = tags.get(i);
		if (index == 0) {
		    results[i] = keys.get(i);
		} else {
		    results[i] = index;
		}
	    }
	    return results;
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    if (tags.size() == 0) {
		return null;
	    }
	    String msg = errorMsg("location");
	    sb.append(msg + " ");
	    int index = tags.get(0);
	    if (index == 0) {
		sb.append(keys.get(0));
	    } else {
		sb.append(index);
	    }
	    for(int i = 1; i < tags.size(); i++) {
		index = tags.get(i);
		sb.append(", ");
		if (index == 0) {
		    sb.append(keys.get(i));
		} else {
		    sb.append(index);
		}
	    }
	    return sb.toString();
	}
    }



    /**
     * Methods for parsing and generating JSON.
     * All the public methods of this class are static.
     * An extended syntax is used for parsing (but not for writing):
     * <UL>
     *   <LI> Comments consist of the sequence '//', not within a string,
     *        and contain all characters up to the end of a line. Comments
     *        will be silently dropped and are allowed only where whitespace
     *        is allowed.
     *   <LI> For a JSON object in a key/value pair, quoting the
     *        key is optional when the key is syntactically a Java
     *        identifier.
     * </UL>
     * These extensions are useful when  a file containing JSON-formated
     * values is used as an input to a program. The object returned by the
     * parser can be used to configure a named-object factory (using the
     * syntax described in the documentation for
     * {@link org.bzdev.obnaming.NamedObjectFactory}.
     */
    public static class JSON {

	// used as a singleton
	/**
	 *  Constructor.
	 */
	protected JSON() {}
	private static JSON json = new JSON();

	/**
	 * Write a JSON representation of an object.
	 * The type of the object may be {@link JSObject}, {@link JSArray},
	 * {@link String}, {@link Number}, or {@link Boolean}.
	 * @param w a writer
	 * @param object the object to write
	 * @exception IOException an error occurred while writing
	 */
	public static void writeTo(Writer w, Object object)
	    throws IOException
	{
	    json.write(w, object);
	}

	/**
	 * Write an object.
	 * This method dispatches the operation to methods for
	 * specific object types.
	 * @param w the writer
	 * @param object the object
	 */
	protected void write(Writer w, Object object) throws IOException {
	    if (object == null) {
		w.write("null");
	    } else if (object instanceof JSObject) {
		write(w, (JSObject) object);
	    } else if (object instanceof JSArray) {
		write(w, (JSArray) object);
	    } else if (object instanceof Number) {
		write(w, (Number) object);
	    } else if (object instanceof Boolean) {
		write(w, (Boolean) object);
	    } else if (object instanceof String) {
		write(w, (String) object);
	    } else  {
		String msg = errorMsg("noWrite", object.getClass());
		throw new IOException(msg);
	    }
	    w.flush();
	}

	/**
	 * Write an object whose type is {@link JSObject}.
	 * @param w the writer
	 * @param object the object
	 */
	protected  void write(Writer w, JSObject object)
	    throws IOException
	{
	    w.write("{");
	    boolean notFirst = false;
	    for(Map.Entry<String,Object> entry: object.entrySet()) {
		if (notFirst) {
		    w.write(", ");
		}
		String key = entry.getKey();
		Object value = entry.getValue();
		w.write(quote(key) + ": ");
		write(w, value);
		notFirst = true;
	    }
	    w.write("}");
	}

	/**
	 * Write an object whose type is {@link JSArray}.
	 * @param w the writer
	 * @param object the object
	 */
	protected void write(Writer w, JSArray object)
	    throws IOException
	{
	    w.write("[");
	    boolean notFirst = false;
	    for (Object value: object.list) {
		if (notFirst) {
		    w.write(", ");
		}
		write(w, value);
		notFirst = true;
	    }
	    w.write("]");
	}

	/**
	 * Write an object whose type is {@link Boolean}.
	 * @param w the writer
	 * @param object the object
	 */
	protected void write(Writer w, Boolean object)
	    throws IOException
	{
	    w.write((object == true)? "true": "false");
	}

	/**
	 * Write an object whose type is {@link Number}.
	 * @param w the writer
	 * @param object the object
	 */
	protected void write(Writer w, Number object)
	    throws IOException
	{
	    w.write("" + object.toString());
	}

	/**
	 * Write an object whose type is {@link String}.
	 * @param w the writer
	 * @param object the object
	 */
	protected void write(Writer w, String object)
	    throws IOException
	{
	    if (object == null) {
		w.write("null");
	    } else {
		w.write(quote(object));
	    }
	}

	private static final Pattern STRING_PATTERN = Pattern.compile
	    ("([\"/\\p{Cntrl}\\\\])");
    
	/**
	 * Encode a string using JSON escape sequences.
	 * @param s the string to encode
	 * @return the encoded string, including the delimiting
	 *         double quotes; the token {@code null} if the
	 *         argument is null
	 */
	public static String quote(String s) {
	    if (s == null) return "null";
	    Matcher m = STRING_PATTERN.matcher(s);
	    StringBuilder sb = null;
	    int end = 0;
	    int len = s.length();
	    while(m.find()) {
		if (sb == null) {
		    sb = new StringBuilder();
		    sb.append("\"");
		}
		int start = m.start();
		int codepoint = s.charAt(start);
		sb.append(s.substring(end, start));
		end = m.end();
		switch ((char)codepoint) {
		case '"':
		    sb.append("\\\"");
		    break;
		case '\\':
		    sb.append("\\\\");
		    break;
		case '/':
		    sb.append("\\/");
		    break;
		case '\b':
		    sb.append("\\b");
		    break;
		case '\f':
		    sb.append("\\f");
		    break;
		case '\n':
		    sb.append("\\n");
		    break;
		case '\r':
		    sb.append("\\r");
		    break;
		case'\t':
		    sb.append("\\t");
		    break;
		default:
		    sb.append(String.format((Locale)null, "\\u%04x",
					    s.codePointAt(start)));
		}
	    }
	    if (sb != null) {
		sb.append(s.substring(end, len));
		sb.append("\"");
		return sb.toString();
	    } else {
		return "\"" + s + "\"";
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
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 * @exception UnsupportedEncodingException the charsetName does
	 *            not match a known charset
	 */
	public static Object parse(InputStream is, String charsetName)
	    throws IOException, UnsupportedEncodingException
	{
	    Reader r = new InputStreamReader(is, charsetName);
	    Parser parser = new Parser(r);
	    return parser.getResults();
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
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, CharsetDecoder dec)
	    throws IOException
	{
	    Reader r = new InputStreamReader(is, dec);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * JSON parser that reads JSON-formatted text from an
	 * {@link InputStream} given a {@link Charset}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a JSON value)
	 * @param cs the charset for the input stream
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, Charset cs)
	    throws IOException
	{
	    Reader r = new InputStreamReader(is, cs);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * JSON parser that reads JSON-formated text from a character
	 * array.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param buffer the input character sequence  (containing
	 *        a JSON value)
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(char[] buffer) throws IOException {
	    Reader r = new CharArrayReader(buffer);
	    Parser parser = new Parser(r);
	    return parser.getResults();

	}
	/**
	 * JSON parser that reads JSON-formated text from a {@link String}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param s the input string (containing a JSON value)
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(String s) throws IOException {
	    Reader r = new StringReader(s);
	    Parser parser = new Parser(r);
	    return parser.getResults();
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
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * Parser support class for JSON.
	 */
	public static class Parser {
	    Reader r;
	    protected Locator locator = new Locator();
	    HashMap<Location,LocationPair> lmap = new HashMap<>();

	    protected void tagLocation() {
		// System.out.println(locator +" --- line " + lineno);
		lmap.put(new Location(locator),
			 new LocationPair(lineno, column));
	    }

	    /**
	     * Return a map that associates line-number, column-number pairs
	     * with locations.
	     * @return the map
	     */
	    public Map<Location,LocationPair> getLocationMap() {
		return Collections.unmodifiableMap(lmap);
	    }

	    int b;
	    StringBuilder sb = new StringBuilder();
	    protected long lineno = 1;
	    protected int column = -1;

	    protected int getColumn() {return column;}

	    /**
	     * Specify whether or not comments are allowed and what
	     * their syntax is. The parsers prune out comments when
	     * they are skipping over whitespace.
	     */
	    protected enum CommentMode {
		/**
		 * Comments are not allowed.
		 */
		NONE,
		/*
		 * Comments start with '//' and go to the end of the line.
		*/
		JS,
		/*
		 * Comments start with '#' and go to the end of the line.
		 */
		YAML
	    }
	    
	    CommentMode commentMode = CommentMode.JS;

	    /**
	     *  Determine how comments are treated.
	     *  The default is {@link CommentMode#JS}.
	     *  @param mode the comment mode
	     *  
	     */
	    protected void setCommentMode(CommentMode mode) {
		commentMode = mode;
	    }

	    /**
	     * Constructor.
	     * @param r a reader.
	     */
	    public Parser(Reader r) throws IOException {
		this.r = r;
		b = r.read();
		column++;
		if (b == '\r') {
		    b = r.read();
		    column++;
		}
		if (b != '\n') {
		    if (b != -1 && column == -1) {
			lineno++;
			column++;
		    }
		} else {
		    column = -1;
		}
	    }

	    /**
	     * Read a character.
	     */
	    protected int nextChar() throws IOException {
		int prevb = b;
		b = r.read();
		if (commentMode == CommentMode.YAML && b == '\t') {
		    throw new IOException("tabsNotAllowed");
		}
		if (b != '\n') {
		    if (b != -1 && column == -1) lineno++;
		    column++;
		} else {
		    if (prevb == '\n') lineno++;
		    column = -1;
		}
		/*
		String trace = null;
		for (StackTraceElement e:
			 Thread.currentThread().getStackTrace()) {
		    if (e.getClassName().equals("java.lang.Thread")) continue;
		    if (!e.getMethodName().equals("nextChar")) {
			trace = e.getMethodName() + ", line "
			    + e.getLineNumber();
			break;
		    }
		}

		System.out.println("read b = " + b + ", char = '" + (char)b
				   + "', lineno = " + lineno +", col = "
				   + column + ", at " + trace);
		*/
		return b;
	    }

	    /**
	     * Read a string terminated by an EOL sequence,
	     * which is not part of the string.
	     * @return the string
	     */
	    protected String readLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean sawCR = false;
		while (b != '\n' && b != -1) {
		    if (b != '\r') {
			sb.append((char)b);
			sawCR = false;
		    } else {
			if (sawCR) sb.append('\r');
			sawCR = true;
		    }
		    b = r.read();
		    if (b != -1 && column == -1) lineno++;
		    column++;
		}
		if (b == '\n') {
		    column = -1;
		}

		// have not read past the terminating '\n'
		// but any '\r' just before a '\n' will be dropped.
		return sb.toString();
	    }

	    protected boolean hasComment(String s) {
		int index = -1;
		int len = s.length();
		if (len == 0) return false;
		switch(commentMode) {
		case JS:
		    int lenm2 = len - 2;
		    if (lenm2 < 0) return false;
		    index = s.indexOf('/');
		    if (index < 0) {
			return false;
		    } else {
			while (index >= 0 && index <= lenm2
			       && s.charAt(index+1) != '/') {
			    index = s.indexOf('/', index+1);
			}
			if (index >= 0) {
			    return true;
			} else {
			    return false;
			}
		    }
		case YAML:
		    index = s.indexOf('#');
		    if (index < 0) {
			return false;
		    } else {
			return true;
		    }
		default:
		    return false;
		}
	    }
	    
	    /**
	     * Strip a comment from a string.
	     * @param s the string
	     * @return the string with comments removed.
	     */
	    protected String stripComment(String s) {
		if (s == null) return null;
		int index = -1;
		int len = s.length();
		if (len == 0) return s;
		switch(commentMode) {
		case JS:
		    int lenm2 = len - 2;
		    if (lenm2 < 0) return s;
		    index = s.indexOf('/');
		    if (index < 0) {
			return s;
		    } else {
			while (index >= 0 && index <= lenm2
			       && s.charAt(index+1) != '/') {
			    index = s.indexOf('/', index+1);
			}
			if (index >= 0) {
			    return s.substring(index).stripTrailing();
			} else {
			    return s;
			}
		    }
		case YAML:
		    index = s.indexOf('#');
		    if (index < 0) {
			return s.stripTrailing();
		    } else {
			return s.substring(0, index).stripTrailing();
		    }
		default:
		    return s;
		}
	    }

	    /**
	     * Get the object generated by a parser.
	     * @return the object (a Boolean, Integer, Long, Double,
	     *   JSArray, JSObject, or the value null)
	     */
	    public Object getResults() throws IOException {
		return parseValueJS(null);
	    }

	    private int indentation = 0;

	    /**
	     * The  indentation after a new line.
	     * @return the indentation
	     */
	    protected int getLineIndentation() {
		return indentation;
	    }

	    private boolean startsComment(int b) {
		switch(commentMode) {
		case JS:
		    if (b == '/') return true;
		case YAML:
		    if (b == '#') return true;
		}
		return false;
	    }

	    /**
	     * Skip over whitespace.
	     * @exception IOException an error occurred
	     */
	    protected void skipWhitespace() throws IOException {
		boolean inComment = false;
		while (b != -1  & Character.isWhitespace((char)b) ||
		       startsComment(b)) {
		    switch(commentMode) {
		    case JS:
			if (b == '/') {
			    b = r.read();
			    column++;
			    if (b == -1) break;
			    if (b == '/') {
				b = r.read();
				column++;
				while (b != -1 &&  b != '\n') {
				    b = r.read();
				    // if (b != -1 && column == -1) lineno++;
				    column++;
				}
				if (b == '\n') {
				    column = -1;
				    indentation = 0;
				}
				continue;
			    } else {
				String msg = errorMsg("badComment");
				throw new JSException(lineno, msg);
			    }
			}
			break;
		    case YAML:
			if (b == '#') {
			    b = r.read();
			    // if (b != -1 && column == -1) lineno++;
			    column++;
			    if (b == -1) break;
			    while (b != -1 &&  b != '\n') {
				b = r.read();
				// if (b != -1 && column == -1) lineno++;
				column++;
			    }
			    if (b == '\n') {
				column = -1;
				indentation = 0;
			    }
			    continue;
			}
			break;
		    }
		    b = r.read();
		    if (b != -1 && column == -1) lineno++;
		    column++;
		    indentation = column;
		    if (b == '\n') {
			indentation = 0;
			column = -1;
		    }
		}
		/*
		String trace = null;
		for (StackTraceElement e: Thread.currentThread()
			 .getStackTrace()) {
		    if (e.getClassName().equals("java.lang.Thread")) continue;
		    if (!e.getMethodName().equals("skipWhitespace")) {
			trace = e.getMethodName() + ", line "
			    + e.getLineNumber();
			break;
		    }
		}
		System.out.println("*** read b = " + b + ", char = '" + (char)b
				   + "', lineno = " + lineno +", col = "
				   + column + ", at " + trace);
		*/
	    }

	    /**
	     * Skip over whitespace, restricted to a single line
	     * @exception IOException an error occurred
	     */
	    protected void skipWhitespace1() throws IOException {
		boolean inComment = false;
		long old = lineno;
		while (b != -1) {
		    if (b == '\n') {
			if (old != lineno) {
			    return;
			}
			/*
			lineno++;
			indentation = 0;
			column = -1;
			*/
		    }
		    switch(commentMode) {
		    case JS:
			if (b == '/') {
			    b = r.read();
			    if (b != -1 && column == -1) lineno++;
			    column++;
			    if (b == -1) break;
			    if (b == '/') {
				b = r.read();
				while (b != -1 &&  b != '\n') {
				    b = r.read();
				    if (b != -1 && column == -1) lineno++;
				    if (b != -1 && column == -1) lineno++;
				    column++;
				}
				if (old != lineno) {
				    return;
				}
				column = -1;
				indentation = 0;
			    } else {
				String msg = errorMsg("badComment");
				throw new JSException(lineno, msg);
			    }
			}
			break;
		    case YAML:
			if (b == '#') {
			    b = r.read();
			    if (b != -1 && column == -1) lineno++;
			    column++;
			    if (b == -1) break;
			    while (b != -1 &&  b != '\n') {
				b = r.read();
				if (b != -1 && column == -1) lineno++;
				column++;
			    }
			    if (old != lineno) {
				return;
			    }
			    column = -1;
			    indentation = 0;
			}
			break;
		    }
		    if (!Character.isWhitespace(b)) {
			return;
		    }
		    b = r.read();
		    if (b != -1 && column == -1) lineno++;
		    column++;
		    indentation = column;
		    if (b == '\n') {
			column = -1;
		    }
		}
	    }

	    /**
	     * Parse a JSON object.
	     * @exception IOException an error occurred
	     */
	    protected JSObject parseObject(YAML.Parser.YAMLData ydata)
		throws IOException
	    {
		// initial '{'already  read
		//b = r.read();
		locator.pushLevel();
		nextChar();
		skipWhitespace();
		JSObject jsobject = new JSObject();

		if (b == '}') {
		    locator.popLevel();
		    nextChar();
		    return jsobject;
		}
		boolean isIdent = false;
		if (Character.isJavaIdentifierStart((char)b)) {
		    isIdent = true;
		} else if (b != '"') {
		    String msg = errorMsg("needsDoubleQuote");
		    throw new JSException(lineno, msg);
		}
		for (;;) {
		    String key = parseString(isIdent);
		    locator.setKey(key);
		    tagLocation();
		    skipWhitespace();
		    if ( b != ':') {
			String msg = errorMsg("missingColon");
			throw new JSException(lineno, msg);
		    }
		    nextChar();
		    Object value = parseValueJS(ydata);
		    if (jsobject.containsKey(key)) {
			String msg = errorMsg("duplicateKey", key);
			throw new JSException(lineno, msg);
		    }
		    jsobject.putObject(key, value);
		    skipWhitespace();
		    if (b == '}') {
			locator.popLevel();
			nextChar();
			return jsobject;
		    }
		    if (b != ',') {
			String msg = errorMsg("missingComma1");
			throw new JSException(lineno, msg);
		    }
		    nextChar();
		    skipWhitespace();
		    isIdent = false;
		    if (Character.isJavaIdentifierStart(b)) {
			isIdent = true;
		    } else if (b != '"') {
			String msg = errorMsg("needsDoubleQuote");
			throw new JSException(lineno, msg);
		    }
		}
	    }

	    /**
	     * Parse a JSON array.
	     * @exception IOException an error occurred
	     */
	    protected JSArray parseArray(YAML.Parser.YAMLData ydata)
		throws IOException
	    {
		//  initial '[' already read.
		locator.pushLevel();
		nextChar();
		skipWhitespace();
		JSArray array = new JSArray();
		if (b == ']') {
		    locator.popLevel();
		    nextChar();
		    return array;
		}
		for (;;) {
		    locator.incrList();
		    tagLocation();
		    Object value = parseValueJS(ydata);
		    array.addObject(value);
		    skipWhitespace();
		    if (b == ']') {
			locator.popLevel();
			nextChar();
			return array;
		    }
		    if (b != ',') {
			String msg = errorMsg("missingComma2", (char)b);
			throw new JSException(lineno, msg);
		    }
		    nextChar();
		}
	    }

	    /**
	     * Parse a JSON string.
	     */
	    protected String parseString() throws IOException {
		return parseString(false);
	    }

	    /**
	     * Parse a string.
	     * @param isIdent true if the string is an identifier without
	     *        double quotes; false otherwise
	     * @return the string
	     * @exception IOException an error occurred
	     */
	    protected String parseString(boolean isIdent) throws IOException {
		if (isIdent) {
		    sb.setLength(0);
		    sb.append((char) b);
		    nextChar();
		    while (b != -1) {
			if (b == '.') {
			    sb.append((char)b);
			    nextChar();
			} else if (!Character.isJavaIdentifierPart((char)b)) {
			    return sb.toString();
			} else {
			    sb.append((char)b);
			    nextChar();
			}
		    }
		    String msg = errorMsg("EOF");
		    throw new JSException(lineno);
		}
		// initial '"' already read.
		nextChar();
		sb.setLength(0);
		while(b != -1) {
		    switch (b) {
		    case '\\':
			nextChar();
			switch (b) {
			case '"':
			    sb.append('"');
			    break;
			case '\\':
			    sb.append('\\');
			    break;
			case '/':
			    sb.append('/');
			    break;
			case 'b':
			    sb.append("\b");
			    break;
			case 'f':
			    sb.append("\f");
			    break;
			case 'n':
			    sb.append("\n");
			    break;
			case 'r':
			    sb.append("\r");
			    break;
			case 't':
			    sb.append("\t");
			    break;
			case 'u':
			    int codepoint = 0;
			    for (int i = 0; i < 4; i++) {
				nextChar();
				switch(b) {
				case '0':
				    b = 0;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '1':
				    b = 1;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '2':
				    b = 2;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '3':
				    b = 3;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '4':
				    b = 4;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '5':
				    b = 5;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '6':
				    b = 6;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '7':
				    b = 7;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '8':
				    b = 8;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '9':
				    b = 9;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'a':
				case 'A':
				    b = 10;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'b':
				case 'B':
				    b = 11;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'c':
				case 'C':
				    b = 12;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'd':
				case 'D':
				    b = 13;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'e':
				case 'E':
				    b = 14;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'f':
				case 'F':
				    b = 15;
				    codepoint = (codepoint << 4) | b;
				    break;
				default:
				    String msg = errorMsg("hexDigit");
				    throw new
					JSException(lineno, msg);
				}
			    }
			    b = codepoint;
			    sb.append((char)codepoint);
			    break;
			default:
			    String msg =
				errorMsg("illegalEscape", (char)b, b);
			    throw new JSException(lineno, msg);
			}
			break;
		    case '"':
			nextChar();
			return sb.toString();
		    default:
			sb.append((char)b);
		    }
		    if (Character.isISOControl(b)) {
			String msg = errorMsg("controlNotAllowed", b);
			throw new JSException(lineno, msg);
		    }
		    nextChar();
		}
		String msg = errorMsg("EOF");
		throw new JSException(lineno, msg);
	    }

	    /**
	     * Parse a number.
	     * @return the number
	     * @exception IOException an error occurred
	     */
	    protected Number parseNumber() throws IOException {
		boolean floatingPoint = false;
		sb.setLength(0);
		if (b == '-') {
		    sb.append((char)b);
		    nextChar();
		}
		if (b == '0') {
		    sb.append('0');
		    nextChar();
		} else {
		    switch (b) {
		    case '1': case '2': case '3': case '4': case '5':
		    case '6': case '7': case '8': case '9':
			sb.append((char)b);
			nextChar();
			break;
		    default:
			break;
		    }
		    for (;;) {
			boolean done = false;
			switch (b) {
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
			    sb.append((char)b);
			    nextChar();
			    break;
			default:
			    done = true;
			    break;
			}
			if (done) break;
		    }
		}
		if (b == '.') {
		    sb.append('.');
		    floatingPoint = true;
		    boolean done = false;
		    for (;;) {
			nextChar();
			switch (b) {
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
			    sb.append((char)b);
			    break;
			default:
			    done = true;
			    break;
			}
			if (done) break;
		    }
		}
		if (b == 'e' || b == 'E') {
		    sb.append((char)b);
		    floatingPoint = true;
		    boolean done = false;
		    boolean notFirst = false;
		    for (;;) {
			nextChar();
			switch (b) {
			case '-': case '+':
			    if (notFirst) {
				String msg =
				    errorMsg("illformedNumber", (char) b);
				throw new JSException(lineno, msg);
			    }
			    // fall through
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
			    sb.append((char)b);
			    break;
			default:
			    done = true;
			    break;
			}
			if (done) break;
			notFirst= true;
		    }
		    nextChar();
		}
		String s = sb.toString();
		if (floatingPoint) {
		    Double value = Double.valueOf(s);
		    Long longval = Math.round(value);
		    boolean maybeLong = Long.MIN_VALUE <= value
			&& value <= Long.MAX_VALUE;
		    if (maybeLong && ((double) longval) == value) {
			try {
			    return Integer.valueOf(Math.toIntExact(longval));
			} catch (ArithmeticException e) {
			    return Long.valueOf(longval);
			}
		    } else {
			return value;
		    }
		} else {
		    try {
			return Integer.valueOf(s);
		    } catch (NumberFormatException e1) {
			try {
			    return Long.valueOf(s);
			} catch (NumberFormatException e2) {
			    try {
				return Double.valueOf(s);
			    } catch (NumberFormatException e3) {
				String msg = errorMsg("badNumber");
				throw new JSException(lineno, msg);
			    }
			}
		    }
		}
	    }

	    /**
	     * Parse a JSON value.
	     * @exception IOException an error occurred
	     */
	    protected Object parseValue() throws IOException {
		return parseValue(null);
	    }

	    /**
	     * Parse a JSON value with string mapping.
	     * @param ydata a function that maps a string to an
	     *        object
	     * @exception IOException an error occurred
	     */
	    protected Object parseValue(YAML.Parser.YAMLData ydata)
		throws IOException
	    {
		return parseValueJS(ydata);
	    }



	    protected Object parseValueJS(YAML.Parser.YAMLData ydata)
		throws IOException
	    {
		String anchor = null;
		String tag = null;
		for (;;) {
		    skipWhitespace();
		    if (b == -1) {
			String msg = errorMsg("EOF");
			throw new JSException(lineno, msg);
		    }
		    switch (b) {
		    case '{':
			if (ydata != null && anchor != null) {
			    Object result = parseObject(ydata);
			    ydata.setAnchor(anchor, result);
			    return result;
			} else {
			    return parseObject(ydata);
			}
		    case '[':
			if (ydata != null && anchor != null) {
			    Object result = parseArray(ydata);
			    ydata.setAnchor(anchor, result);
			    return result;
			} else {
			    return parseArray(ydata);
			}
		    case '"':
			if (ydata == null) {
			    return parseString();
			} else {
			    Object result = (tag == null)? parseString():
				ydata.toObject(parseString());
			    if (anchor != null) {
				ydata.setAnchor(anchor, result);
			    }
			    return result;
			}
		    case 't':
			if (nextChar() == 'r' && nextChar() == 'u'
			    && nextChar() == 'e') {
			    nextChar();
			    return Boolean.TRUE;
			} else {
			    String msg = errorMsg("expectingBoolean");
			    throw new JSException(lineno, msg);
			}
		    case 'f':
			if (nextChar() == 'a'&& nextChar() == 'l'
			    && nextChar() == 's' && nextChar() == 'e') {
			    nextChar();
			    return Boolean.FALSE;
			} else {
			    String msg = errorMsg("expectingBoolean");
			    throw new JSException(lineno, msg);
			}
		    case 'n':
			if (nextChar() == 'u' && nextChar() == 'l'
			    && nextChar() == 'l') {
			    nextChar();
			    return null;
			} else {
			    String msg = errorMsg("expectingNull");
			    throw new JSException(lineno, msg);
			}
		    case '-':
		    case '0':
		    case '1':
		    case '2':
		    case '3':
		    case '4':
		    case '5':
		    case '6':
		    case '7':
		    case '8':
		    case '9':
			return parseNumber();
		    case '*':
			if (ydata == null) {
			    String msg = errorMsg("illegalChar", (char)b, b);
			    throw new JSException(lineno, msg);
			}
			long clineno = lineno;
			    String alias = ydata.findAlias();
			if (alias == null) {
			    String msg = errorMsg("illegalChar", '*', (int)'*');
			    throw new JSException(clineno, msg);
			}
			return ydata.getAlias(alias);
		    case '!':
		    case '<':
		    case '&':
			if (ydata == null) {
			    String msg = errorMsg("illegalChar", (char)b, b);
			    throw new JSException(lineno, msg);
			}
			anchor = ydata.getAnchorWithTag();
			tag = ydata.getCurrentTag();
			continue;
		    default:
			String msg = errorMsg("illegalChar", (char)b, b);
			throw new JSException(lineno, msg);
		    }
		}
	    }
	}
    }


    /**
     * Class for parsing YAML streams.
     * This class, like the corresponding JSON class, provides static
     * methods for parsing a YAML stream that produces a single object.
     * <P>
     * Unlike JSON, YAML allows a sequence of objects to be read from a
     * single stream, with  objects' YAML representation separated by
     * a line starting with "---" (such streams must start with the
     * line "%YAML VERSION" followed by some optional tag definitions,
     * and the sequence "---" at the start of a line). This class does
     * not support this particular case: instead use
     * {@link JSUtilities.YAML.Parser}.
     * <P>
     * The YAML parser will throw an exception if the input stream
     * contains tabs (the YAML specification does not allow tabs).
     * If one needs to handle files with tabs, the class
     * {@link org.bzdev.io.DetabReader} can be used to turn those
     * tabs into the appropriate number of spaces.
     */
    public static class YAML extends JSON {

	/**
	 * YAML tag for additional data types.
	 * This class is used to configure how YAML will handle
	 * additional data types. When {@link ExpressionParser} is
	 * used, the recommended tag is tag:bzdev.org,2021:esp and
	 * a suitable prefix is !bzdev! so that the prefix and tag
	 * combination is equivalent to the YAML declaration
	 * <BLOCKQUOTE>
	 * %TAG !bzdev! tag:bzdev.org,2021:
	 * </BLOCKQUOTE>
	 * and the string will be processed by {@link ExpressionParser}
	 * when a value starts with the sequence !bzdev!esp
	 * For example
	 * <BLOCKQUOTE>
	 * - expressionList:
	 *    - !bzdev!esp &gt;-
	 *        ...
	 * </BLOCKQUOTE>
	 * If the constructor explicitly provides the prefix, the
	 * %TAG directive is not needed.  For example
	 * <BLOCKQUOTE>
	 *   ExpressionParser ep = new ExpressionParser(...);
	 *   ObjectParser.SourceParser sp =
	 *       new ObjectParser.SourceParser(ep);
	 *   TagSpec ts = new TagSpec("!bzdev!",
	 *                            "tag:bzdev.org,2021:esp",
	 *                            sp);
	 * </BLOCKQUOTE>
	 * will create an {@link ObjectParser.SourceParser} for
	 * an expression parser that will encapsulate the content
	 * so it can be evaluated at a later time (this occurs in
	 * the {@link org.bzdev.obnaming.ObjectNamerLauncher} class
	 * which for implementation reasons has to control the order
	 * of evaluation when anchors are used.
	 */
	public static class TagSpec {
	    String prefix;
	    String tag;
	    ObjectParser objectParser;

	    /**
	     * Constructor.
	     * The prefix is a string, which should start with "!",
	     * that will be replaced with the tag up to an including
	     * its final colon. The tag is a YAML-tag URL that names a
	     * data type (for example, tag:yaml.org,2002:int). The
	     * object parser is an instance of {@link ObjectParser}
	     * that will parse a string and return the appropriate
	     * object.
	     * <P>
	     * With multiple instances, one can provide the prefix for
	     * one and not bother for ones where the mapping to a tag
	     * will be the same.  It can also be null if the prefix
	     * will be defined in a YAML "%TAG" directive.
	     * <P>
	     * A prefix is a string that represents the portion of the
	     * tag up to its final colon, and should start and end with
	     * exclamation points: for example if the prefix i
	     * !bzdev! and the tag is tag:bzdev.org,2021:esp, then
	     * !bzdev! will represent "tag:bzdev.org,2021:", providing a
	     * terser notation.  That in turn will indicate which object
	     * parser to use.  If the prefix is null, it should be defined
	     * previously or in a %TAG directive at the start of a YAML
	     * file.
	     * @param prefix the prefix; null if not used for this entry
	     * @param tag the tag
	     * @param objectParser the object parser for this data type.
	     */
	    public TagSpec(String prefix, String tag,
			   ObjectParser objectParser)
	    {
		this.prefix = prefix;
		this.tag = tag;
		this.objectParser = objectParser;
	    }
	}

	// private constructor as there is nothing to do: all
	// methods are static
	private YAML() {}

	/**
	 * YAML parser that reads YAML-formatted text from an
	 * {@link InputStream} given a {@link String} providing the
	 * name of a character set.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a YAML value)
	 * @param charsetName the name of the charset used by the input
	 *        stream
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 * @exception UnsupportedEncodingException the charsetName does
	 *            not match a known charset
	 */
	public static Object parse(InputStream is, String charsetName)
	    throws IOException, UnsupportedEncodingException
	{
	    Reader r = new InputStreamReader(is, charsetName);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * YAML parser that reads YAML-formatted text from an
	 * {@link InputStream} given a {@link CharsetDecoder}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a YAML value)
	 * @param dec the charset decoder used to turn the
	 *        input stream into a sequence of characters
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, CharsetDecoder dec)
	    throws IOException
	{
	    Reader r = new InputStreamReader(is, dec);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * YAML parser that reads YAML-formatted text from an
	 * {@link InputStream} given a {@link Charset}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param is the input stream (containing a YAML value)
	 * @param cs the charset for the input stream
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(InputStream is, Charset cs)
	    throws IOException
	{
	    Reader r = new InputStreamReader(is, cs);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * YAML parser that reads YAML-formated text from a character
	 * array.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param buffer the input character sequence  (containing
	 *        a YAML value)
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(char[] buffer) throws IOException {
	    Reader r = new CharArrayReader(buffer);
	    Parser parser = new Parser(r);
	    return parser.getResults();

	}
	/**
	 * YAML parser that reads YAML-formated text from a {@link String}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param s the input string (containing a YAML value)
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(String s) throws IOException {
	    Reader r = new StringReader(s);
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}

	/**
	 * YAML parser that reads YAML-formated text from a {@link Reader}.
	 * The result's type, if the result is not null, is either
	 * {@link JSObject}, {@link JSArray}, {@link String},
	 * {@link Number}, or {@link Boolean}.
	 * @param r the reader
	 * @return the object.
	 * @exception IOException an error occurred while reading from
	 *            the input stream
	 */
	public static Object parse(Reader r) throws IOException {
	    Parser parser = new Parser(r);
	    return parser.getResults();
	}


	/**
	 * Parser support class for YAML.
	 * This class is used by the static methods in the class
	 * {@link JSUtilities.YAML}. These static methods create a
	 * suitable {@link Reader} when necessary, pass that to
	 * the constructor for {@link JSUtilities.YAML.Parser}, and
	 * then call the method {@link JSUtilities.YAML.Parser#getResults()}
	 * to obtain the results.
	 * <P>
	 * For YAML streams representing multiple objects, the
	 * constructor is used as described above. The method
	 * {@link JSUtilities.YAML.Parser#getResults()} can be called
	 * as many times as needed, while the method
	 * {@link JSUtilities.YAML.Parser#hasNext()} can be called to
	 * determine if there is another object available.
	 */
	public static class Parser extends JSON.Parser {

	    class YAMLData {

		Object toObject(String string) throws JSException {
		    return Parser.this.toObject(string);
		}

		String getCurrentTag() {
		    return Parser.this.currentTag;
		}
		String getCurrentTagRef() {
		    return Parser.this.currentTagRef;
		}

		void clearTag() {
		    Parser.this.currentTag = null;
		    Parser.this.currentTagRef = null;
		}

		String getAnchorWithTag() throws IOException {
		    Parser.this.getTag(true);
		    if (b == '&') {
			nextChar();
			StringBuilder asb = new StringBuilder();
			while (isAnchorChar(b)) {
			    asb.append((char)b);
			    nextChar();
			}
			String anchor = asb.toString();
			long ln1 = Parser.this.lineno;
			skipWhitespace();
			Parser.this.getTag(true);
			return anchor;
		    } else {
			return null;
		    }
		}
		void setAnchor(String anchor, Object value) {
		    Parser.this.refmap.put(anchor, value);
		}

		String findAlias() throws IOException {
		    if (b == '*') {
			StringBuilder asb = new StringBuilder();
			nextChar();
			while (isAnchorChar(b)) {
			    asb.append((char)b);
			    nextChar();
			}
			return asb.toString();
		    } else {
			return null;
		    }
		}

		Object getAlias(String alias) throws JSException {
		    if (Parser.this.refmap.containsKey(alias)) {
			return Parser.this.refmap.get(alias);
		    } else {
			String msg = errorMsg("missingAnchor", alias);
			throw new JSException(lineno, msg);
		    }
		}
	    }


	    // for YAML anchors
	    HashMap<String,Object>refmap = new HashMap<>();
	    private boolean isAnchorChar(int c) {
		switch (c) {
		case -1:
		case '[':
		case ']':
		case '{':
		case '}':
		case ',':
		case '\'':
		case '"':
		case ':':
		case '#':
		case '!':
		case '-':
		case '?':
		case '&':
		case '|':
		case '>':
		case '%':
		case '@':
		case '`':
		case '*':
		    return false;
		}
		return (c > 0x20 && c < 0xFF)
		    || c == 0x85
		    || (c >= 0xA0 && c <= 0xFFFD);
	    }

	    // used to to help the JSON parser handle YAMA-specific
	    // requirements.
	    YAMLData ydata = new YAMLData();


	    private boolean isTagChar(int c) {
		if (b == -1) return false;
		return Character.isWhitespace((char) c) == false;
	    }

	    private static enum Style {
		FOLDED,
		LITERAL,
	    }

	    private static enum Chomping {
		CLIP,
		STRIP,
		KEEP,
	    }

	    Style style = Style.LITERAL;
	    Chomping chomping = Chomping.STRIP;
	    Object currentObject = null;
	    int currentIndent = -1;
	    int contIndent = -1;
	    String key = null;
	    long keyLineno = -1; // can't have two keys on the same line
	    StringBuffer sb = new StringBuffer();
	    String currentTag = null;
	    String currentTagRef = null; // for error messages
	    boolean sawTag = false;

	    enum Termination {
		NOT_TERMINATED,
		NEXT_OBJECT_AVAILABLE,
		TERMINATED,
		TERMINATED_DELAYED,
		NEXT_OBJECT_AVAILABLE_DELAYED,
		FAILED
	    }

	    Termination termination = Termination.NOT_TERMINATED;

	    /**
	     * Determine if {@link #getResults()} can be called
	     * again to obtain another object.
	     */
	    public boolean hasNext() {
		return termination == Termination.NOT_TERMINATED
		    || termination == Termination.NEXT_OBJECT_AVAILABLE;
	    }

	    /**
	     * Sets the current tag when 'b' has the value '!' or '<'.
	     */
	    private void getTag(boolean skip) throws IOException {
		if (b == '!') {
		    nextChar();
		    StringBuilder asb = new StringBuilder();
		    asb.append('!');
		    while (isTagChar(b)) {
			asb.append((char)b);
			nextChar();
		    }
		    currentTagRef = asb.toString();
		    currentTag = toTag(currentTagRef);
		    if (skip) skipWhitespace();
		} else if (b == '<') {
		    nextChar();
		    StringBuilder asb = new StringBuilder();
		    asb.append('<');
		    while (isTagChar(b)) {
			asb.append((char)b);
			boolean stop = (b == '>');
			nextChar();
			if (stop) break;
		    }
		    currentTagRef = asb.toString();
		    currentTag = toTag(currentTagRef);
		    if (skip) skipWhitespace();
		}
	    }

	    private Object toObject(String string) throws JSException {
		if (string == null) return null;
		// String s = string.trim();
		if (currentTag != null) {
		    ObjectParser<?> parser = pmap.get(currentTag);
		    currentTag = null;
		    if (parser == null) {
			String msg = errorMsg("noParserForTag", currentTagRef);
			currentTagRef = null;
			throw new JSException(lineno, msg);
		    }
		    String s = (parser == ObjectParser.STRING)? string:
			string.trim();
		    try {
			if (parser.matches(string)) {
			    return parser.parse(string);
			} else {
			    String msg = errorMsg("matchFailed", currentTagRef);
			    throw new Exception(msg);
			}
		    } catch (Exception e) {
			    String msg = errorMsg("parseError", currentTagRef);
			    throw new JSException(lineno, msg, e);
		    } finally {
			currentTagRef = null;
		    }
		}
		String trimmed = string.trim();
		for (Map.Entry<String,ObjectParser<?>> entry: pmap.entrySet()) {
		    String tagref = entry.getKey();
		    ObjectParser<?> parser = entry.getValue();
		    String s = (parser == ObjectParser.STRING)? string:
			trimmed;
		    try {
			if (parser.appliesTo(s)) {
			    if (parser.matches(s)) {
				Object value = parser.parse(s);
				return value;
			    } else {
				String msg = errorMsg("parserMatch", tagref);
				throw new Exception(msg);
			    }
			}
		    } catch (Exception e) {
			String msg = errorMsg("parseError", tagref);
			throw new JSException(lineno, msg, e);
		    }
		}
		return null;
	    }

	    private void setScalarOptions(char flag, String s)
		throws JSException
	    {
		s = s.stripTrailing();
		int slen  = s.length();
		if (slen > 3) {
		    // error
		    String msg = errorMsg("illegalStyle");
		    throw new JSException(lineno, msg);
		}
		chomping = Chomping.CLIP;
		contIndent = 0; // indicates automatic
		if (flag == '|') {
		    style = Style.LITERAL;
		} else if (flag == '>') {
		    style = Style.FOLDED;
		} else {
		    java.lang.String
			msg = errorMsg("badStringStyle");
		    throw new IllegalArgumentException(msg);
		}
		for (int i = 1; i < slen; i++) {
		    switch (s.charAt(i)) {
		    case '+':
			chomping = Chomping.KEEP;
			break;
		    case '-':
			chomping = Chomping.STRIP;
			break;
		    case '1': case '2': case '3': case '4': case '5':
		    case '6': case '7': case '8': case '9':
			contIndent = s.charAt(i) - '0';
			break;
		    }
		}
	    }

	    /*
	     * 0 for unknown, -1 for end of input, '-', "'", '"", "|", ">"
	     * for those characters, "o" for other printable characters.
	     */
	    int prevchar = 0;
	    Pattern TAG_PATTERN = Pattern.compile
		("%TAG[ ]+([!][0-9a-zA-Z]*[!]?)[ ]+([\\p{Graph}]+)");

	    HashMap<String,String> tagmap = new HashMap<>();

	    HashMap<String,ObjectParser<?>> pmap = new LinkedHashMap<>();

	    private String toTag(String s) throws JSException {
		int len = s.length();
		// length cannot be zero
		if (len == 1) {
		    return tagmap.get(s);
		} else {
		    if (s.charAt(0) == '<' &&
			s.charAt(len-1) == '>') {
			return s.substring(1, len-1);
		    }
		    int index = s.indexOf('!', 1);
		    if (index > 0) {
			String key = s.substring(0, index+1);
			String s1 = tagmap.get(key);
			if (s1 == null) {
			    String msg = errorMsg("noTag", key);
			    throw new JSException(lineno, msg);
			}
			String s2 = s.substring(index+1, len);
			return s1 + s2;
		    } else {
			String s1 = tagmap.get("!");
			String s2 = s.substring(1);
			if (s1 == null) {
			    String msg = errorMsg("noTag", s1);
			    throw new JSException(lineno, msg);
			}
			return s1 + s2;
		    }
		}
	    }

	    /**
	     * Constructor.
	     * @param r a reader.
	     */
	    public Parser(Reader r) throws IOException {
		super(r);
		init(r);
	    }


	    /**
	     * Constructor.
	     * @param r a reader.
	     */
	    public Parser(Reader r, TagSpec... yamlTags) throws IOException {
		super(r);
		for (TagSpec ytag: yamlTags) {
		    pmap.put(ytag.tag, ytag.objectParser);
		    if (ytag.prefix != null) {
			int index = ytag.tag.lastIndexOf(':');
			if (index >= 0) {
			    String tag = ytag.tag.substring(0, index+1);
			    tagmap.put(ytag.prefix, tag);
			}
		    }
		}
		init(r);
	    }

	    private void init(Reader r) throws IOException {
		pmap.put("tag:yaml.org,2002:int", ObjectParser.INTLONG);
		pmap.put("tag:yaml.org,2002:float", ObjectParser.DOUBLE);
		pmap.put("tag:yaml.org,2002:bool", ObjectParser.BOOLEAN);
		pmap.put("tag:yaml.org,2002:null", ObjectParser.NULL);
		pmap.put("tag:yaml.org,2002:str", ObjectParser.STRING);
		// These two will be ignored because ObjectParser.STRING
		// handles any string it is given.
		pmap.put("tag:yaml.org,2002:map",  ObjectParser.STRING);
		pmap.put("tag:yaml.org,2002:seq",  ObjectParser.STRING);

		setCommentMode(CommentMode.YAML);
		tagmap.put("!!", "tag:yaml.org,2002:");
		StringBuilder sb = new StringBuilder();
		boolean sawYAML = false;
		boolean isList = false;
		boolean isObject = false;
		boolean oneLine = true;
		skipWhitespace();
		while (getLineIndentation() == 0 && b != -1) {
		    if (b == '%') {
			sawTag = true;
			String line = readLine().trim();
			if (line.startsWith("%YAML")) {
			    if (!line.matches("%YAML[ ]+[0-9]+[.][0-9]+")) {
				String msg = errorMsg("badYAMLDirective", line);
				throw new JSException(lineno, msg);
			    } else if (sawYAML) {
				String msg = errorMsg("multipleYAMLDirectives");
				throw new JSException(lineno, msg);
			    }
			    sawYAML = true;
			} else if (line.startsWith("%TAG")) {
			    Matcher matcher = TAG_PATTERN.matcher(line);
			    if (!matcher.matches()) {
				String msg = errorMsg("badYAMLDirective", line);
				throw new JSException(lineno, msg);
			    }
			    String tag = matcher.group(1);
			    String uri = matcher.group(2);
			    tagmap.put(tag, uri);
			}
			nextChar();
		    } else if (b == '-') {
			long cline = lineno;
			currentIndent = 0;
			prevchar = '-';
			nextChar();
			if (b == ' ' || b == '\r' || b == '\n') {
			    if (sawTag) {
				String msg = errorMsg("notDirectiveEndMark");
				throw new JSException(lineno, msg);
			    }
			    skipWhitespace();
			    break;
			} else if (b == '-') {
			    // two dashes
			    prevchar = 0;
			    nextChar();
			    if (b != '-') {
				if (sawTag) {
				    String msg =
					errorMsg("notDirectiveEndMark");
				    throw new JSException(lineno, msg);
				}
				sb.append("--");
				while (b != -1) {
				    String s = readLine();
				    if (hasComment(s)) {
					s = stripComment(s);
					sb.append(s);
					currentObject = toObject(sb.toString());
					return;
				    } else {
					sb.append(s);
					skipWhitespace();
				    }
				}
				currentObject = toObject(sb.toString());
			    } else {
				init3(cline);
				break;
			    }
			} else {
			    if (sawTag) {
				String msg =
				    errorMsg("notDirectiveEndMark");
				throw new JSException(lineno, msg);
			    }
			    // string starting with '-'
			    sb.append("-");
			    while (b != -1) {
				String s = readLine();
				if (hasComment(s)) {
				    s = stripComment(s);
				    sb.append(s);
				    currentObject = toObject(sb.toString());
				    return;
				} else {
				    sb.append(s);
				    skipWhitespace();
				}
			    }
			    currentObject = toObject(sb.toString());
			    return;
			}
		    } else {
			// may be the first property in an object or a
			// primitive.
			if (sawTag) {
			    String msg = errorMsg("notDirectiveEndMark");
			    throw new JSException(lineno, msg);
			}
			skipWhitespace();
			getTag(true);
			currentObject = tryParseObject(-1, 0, true);
			if (currentTag != null) {
			    if (currentObject != null
				&& currentObject instanceof String) {
				currentObject = toObject((String)currentObject);
			    } else {
				currentTag = null;
				currentTagRef = null;
			    }
			}

			if (currentObject == null) return;
		    }
		    skipWhitespace();
		}
	    }

	    private void init3(long cline) throws IOException {
		long ourlineno = lineno;
		// after three dashes seen from start of a line.
		nextChar();
		skipWhitespace();
		getTag(true);
		switch (b) {
		case '-':
		    prevchar = '-';
		    currentIndent = getColumn();
		    nextChar();
		    if (b == ' ' || b ==  '\r' || b == '\n') {
			skipWhitespace();
		    } else {
			// text, not a list
			sb.append("-");
			String s = readLine();
			boolean hc = hasComment(s);
			if (hc) s = stripComment(s);
			s = s.stripTrailing();
			if (s.equals("--")) {
			    currentObject = null;
			    termination =
				Termination.NEXT_OBJECT_AVAILABLE_DELAYED;
			    return;
			}
			sb.append(s);
			String nls = "";
			while (b != -1) {
			    nextChar();
			    s = readLine();
			    boolean hc2 = hasComment(s);
			    s = stripComment(s);
			    s = s.stripTrailing();
			    if (s.equals("...")) {
				termination = Termination.TERMINATED_DELAYED;
				currentObject = sb.toString();
				return;
			    } else if (s.equals("---")) {
				termination =
				    Termination.NEXT_OBJECT_AVAILABLE_DELAYED;
				currentObject = sb.toString();
				return;
			    }
			    s = s.stripLeading();
			    if (s.length() == 0) {
				nls = nls + "\n";
			    } else {
				if (nls.length() == 0) nls = " ";
				sb.append(nls);
				sb.append(s);
				nls = "";
			    }
			    if (hc2) hc = true;
			}
			currentObject =
			    toObject(sb.toString());
		    }
		    break;
		case '{':
		case '[':
		    currentObject = super.parseValue(ydata);
		    return;
		case '"':
		    // nextChar();
		    currentObject = parseString();
		    if (currentTag != null
			&& currentObject != null
			&& currentObject instanceof String) {
			currentObject =
			    toObject((String)currentObject);
		    } else {
			currentTag = null;
			currentTagRef = null;
		    }
		    return;
		case '\'':
		    // nextChar();
		    currentObject = parseString1();
		    if (currentTag != null
			&& currentObject != null
			&& currentObject instanceof String) {
			currentObject =
			    toObject((String)currentObject);
		    } else {
			currentTag = null;
			currentTagRef = null;
		    }
		    return;
		case '|':
		    String s1 = readLine();
		    setScalarOptions('|', s1);
		    sb.setLength(0);
		    currentIndent = 0;
		    currentObject = parseMultiline(0);
		    if (/*currentTag != null
			  && */ currentObject != null
			&& currentObject instanceof String) {
			currentObject =
			    toObject((String)currentObject);
		    } else {
			currentTag = null;
			currentTagRef = null;
		    }
		    return;
		case '>':
		    String s2 = readLine();
		    setScalarOptions('>', s2);
		    sb.setLength(0);
		    currentIndent = 0;
		    currentObject = parseMultiline(0);
		    if (currentObject != null
			&& currentObject instanceof String) {
			currentObject = toObject((String)currentObject);
		    }
		    return;
		default:
		    if (b == '.' && getColumn() == 0) {
			String prefix = null;
			prefix = readLine();
			if (prefix.equals("...")) {
			    termination = Termination.TERMINATED_DELAYED;
			    currentObject = null;
			    return;
			} else if (prefix.equals("---")) {
			    termination =
				Termination.NEXT_OBJECT_AVAILABLE_DELAYED;
			    currentObject = null;
			    return;
			}
			StringBuilder sb = new StringBuilder();
			// https://jsonformatter.org/yaml-validator
			// indicates that you get in trouble with a
			// comment in the middle of a string.
			boolean hc = hasComment(prefix);
			if (hc) {
			    int ind = prefix.indexOf('#');
			    if (ind >= 0) {
				prefix = prefix.substring(0, ind);
			    }
			}
			sb.append(prefix.stripTrailing());
			String nlprefix = "";
			for (;;) {
			    nextChar();
			    if (b == -1) return;
			    prefix = readLine();
			    if (prefix.equals("...")) {
				termination = Termination.TERMINATED_DELAYED;
				currentObject = sb.toString();
				return;
			    } else if (prefix.equals("---")) {
				termination =
				    Termination.NEXT_OBJECT_AVAILABLE_DELAYED;
				currentObject = sb.toString();
				return;
			    } else {
				if (hc) {
				    String msg = errorMsg("badPrefix", prefix);
				    throw new IOException(msg);
				}
			    }
			    hc = hasComment(prefix);
			    if (hc) {
				int ind = prefix.indexOf('#');
				if (ind >= 0) {
				    prefix = prefix.substring(0, ind);
				}
			    }
			    prefix = prefix.stripTrailing();
			    if (prefix.length() == 0) {
				nlprefix = nlprefix + "\n";
			    } else {
				if (nlprefix.length() == 0) nlprefix = " ";
				sb.append(nlprefix);
				sb.append(prefix.stripTrailing());
				nlprefix = "";
			    }
			}
		    }
		    getTag(true);
		    currentObject =
			tryParseObject(-1, 0, true);
		    if (/*currentTag != null
			  &&*/ currentObject != null
			&& currentObject instanceof String) {
			currentObject =
			    toObject((String)currentObject);
		    } else {
			currentTag = null;
			currentTagRef = null;
		    }
		    if (currentObject == null) return;
		}
	    }

	    /*
	    enum Type {
		YARRAY,
		    YOBJECT,
		    PRIMITIVE
	    }
	    */

	    private class StackEntry {
		int indentation;
		
		    
	    }

	    @Override
	    public Object getResults() throws IOException {
		switch (termination) {
		case NOT_TERMINATED:
		    break;
		case NEXT_OBJECT_AVAILABLE:
		    // reset to state we'd have before calling init3()
		    // in init()
		    column = 2;
		    style = Style.LITERAL;
		    chomping = Chomping.STRIP;
		    currentObject = null;
		    currentIndent = -1;
		    contIndent = -1;
		    key = null;
		    keyLineno = -1; // can't have two keys on the same line
		    sb.setLength(0);
		    currentTag = null;
		    currentTagRef = null; // for error messages
		    refmap.clear();
		    prevchar = 0;
		    init3(lineno);
		    skipWhitespace();
		    break;
		case TERMINATED:
		    throw new IOException(errorMsg("parsingTerminated"));
		case FAILED:
		    throw new IOException(errorMsg("parsingFailed"));
		}
		if (termination == Termination.TERMINATED_DELAYED) {
		    termination = Termination.TERMINATED;
		    return currentObject;
		} else if (termination ==
			   Termination.NEXT_OBJECT_AVAILABLE_DELAYED) {
		    termination = Termination.NEXT_OBJECT_AVAILABLE;
		    return currentObject;
		}
		if (currentObject != null) {
		    if (currentObject instanceof StringBuilder) {
			return currentObject.toString();
		    } else {
			return currentObject;
		    }
		} else {
		    if (getColumn() == -1) {
			skipWhitespace();
			prevchar = b;
			currentIndent = getColumn();			
		    }
		    if (prevchar == '-') {
			locator.pushLevel();
			JSArray array = new JSArray();
			int baseIndent = currentIndent;
			while (prevchar == '-') {
			    locator.incrList();
			    tagLocation();
			    int col = getColumn();
			    String anchor = null;
			    prevchar = 0;
			    getTag(true);
			    if (b == '&') {
				long ln1 = -1;
				nextChar();
				StringBuilder asb = new StringBuilder();
				while (isAnchorChar(b)) {
				    asb.append((char)b);
				    nextChar();
				}
				anchor = asb.toString();
				ln1 = lineno;
				skipWhitespace();
				if (lineno != ln1) {
				    col = getColumn();
				}
				getTag(true); // for opposite order
			    }
			    Object value = parseValue(currentIndent, col);
			    if (currentTag != null && value != null
				&& value instanceof String) {
				value = toObject((String)value);
			    } else {
				currentTag = null; currentTagRef = null;
			    }

			    array.addObject(value);
			    if (anchor != null) {
				refmap.put(anchor, value);
			    }
			    skipWhitespace();
			    if (b == '-' && getColumn() == baseIndent) {
				nextChar();
				if (b == '\r') nextChar();
				if (b == ' ' || b == '\n') {
				    skipWhitespace();
				    prevchar = '-';
				}
			    }
			}
			skipWhitespace();
			if (b == -1) {
			    if (sawTag) {
				termination = Termination.FAILED;
				String msg = errorMsg("inputNotTerminated");
				throw new JSException(lineno, msg);
			    }
			    termination = Termination.TERMINATED;
			    locator.popLevel();
			    return array;
			}
			if (b == '-') {
			    int col = getColumn();
			    if (col == 0) {
				nextChar();
				if (b == '-') nextChar();
				if (b == '-' && getColumn() == 2) {
				    termination =
					Termination.NEXT_OBJECT_AVAILABLE;
				    locator.popLevel();
				    return array;
				}
			    } else if (col == 1) {
				nextChar();
				if (b == '-' && getColumn() == 2) {
				    termination =
					Termination.NEXT_OBJECT_AVAILABLE;
				    locator.popLevel();
				    return array;
				}
			    }
			} else if (b == '.') {
			    int col = getColumn();
			    if (col == 0) {
				nextChar();
				if (b == '.') nextChar();
				if (b == '.' && getColumn() == 2) {
				    nextChar();
				    termination = Termination.TERMINATED;
				    locator.popLevel();
				    return array;
				}
			    }
			}
			termination = Termination.FAILED;
			String msg = errorMsg("badTerminationCol", getColumn());
			throw new JSException(lineno, msg);
		    } else {
			IOException ex = null;
			try {
			    // return super.getResults();
			    // super.getResults() just calls parseValue.
			    skipWhitespace();
			    getTag(true);
			    Object value = parseValue();
			    if (currentTag != null && value != null
				&& value instanceof String) {
				value = toObject((String)value);
			    } else {
				currentTag = null;
				currentTagRef = null;
			    }
			    return value;
			} catch (IOException e) {
			    ex = e;
			    return null;
			} finally {
			    skipWhitespace();
			    if (b != -1) {
				boolean badterm = true;
				if (getColumn() == 0) {
				    if (b == '-') {
					nextChar();
					if (b == '-') {
					    nextChar();
					    if (b == '-') {
						termination =
						    Termination
						    .NEXT_OBJECT_AVAILABLE;
						badterm = false;
					    }
					}
				    } else if (b == '.') {
					nextChar();
					if (b == '.') {
					    nextChar();
					    if (b == '.') {
						badterm = false;
						termination =
						    Termination.TERMINATED;
					    }
					}
				    } else {
					if (ex != null) throw ex;
				    }
				} else {
				    if (ex != null) throw ex;
				}
				if (badterm) {
				    termination = Termination.FAILED;
				    String msg = errorMsg("badTermination");
				    throw new JSException(lineno, msg);
				}
			    } else {
				if (sawTag) {
				    termination = Termination.FAILED;
				    String msg = errorMsg("inputNotTerminated");
				    throw new JSException(lineno, msg);
				}
				termination = Termination.TERMINATED;
			    }
			}
		    }
		}
	    }

	    private void parse(JSObject object, String key,
			       int prevIndent, int nextIndent)
		throws IOException
	    {
		String lastkey = null;
		String anchor = null;
		skipWhitespace();
		int col = getColumn();
		// Testing with https://jsonformatter.org/yaml-validator
		// indicates that YAML should accept the following:
		//    sublist:
		//    - a
		//    - b
		// while
		//    sublist:
		//    a
		//    b
		// should fail
		// So, we'll continue parsing as a special case when
		// the following boolean is true.
		boolean listCase = (b == '-' && col == nextIndent);
		getTag(true);
		if (b == '&') {
		    nextChar();
		    StringBuilder asb = new StringBuilder();
		    while (isAnchorChar(b)) {
			asb.append((char)b);
			nextChar();
		    }
		    anchor = asb.toString();
		    long ln1 = lineno;
		    skipWhitespace();
		    if (lineno != ln1) {
			col = getColumn();
		    }
		    getTag(true);
		}
		for (;;) {
		    if (col > nextIndent || listCase) {
			Object value = parseValue(nextIndent, col);
			if (currentTag != null
			    && value != null
			    && value instanceof String) {
			    value = toObject((String)value);
			} else {
			    currentTag = null;
			    currentTagRef = null;
			}
			if (object.containsKey(key)) {
			    String msg = errorMsg("duplicateKey", key);
			    throw new JSException(lineno, msg);
			}
			object.putObject(key, value);
			lastkey = key;
			key = null;
			if (anchor != null) {
			    refmap.put(anchor, value);
			}
		    } else if (col == nextIndent) {
			currentTag = null;
			currentTagRef = null;
			if (object.containsKey(key)) {
			    String msg = errorMsg("duplicateKey", key);
			    throw new JSException(lineno, msg);
			}
			if (object.containsKey(key)) {
			    String msg = errorMsg("duplicateKey", key);
			    throw new JSException(lineno, msg);
			}
			object.putObject(key, (Object) null);
			lastkey = key;
			key = null;
			if (anchor != null) {
			    refmap.put(anchor, null);
			}
		    } else if (col < nextIndent) {
			currentTag = null;
			currentTagRef = null;
			return;
		    }
		    if (b == -1) {
			return;
		    }
		    anchor = null;
		    skipWhitespace();
		    col = getColumn();
		    if (col != nextIndent && col <= prevIndent) {
			return;
		    }
		    if (col == nextIndent) {
			if (col == 0 && sawTag) {
			    int dotcount = 0;
			    if (b == '.') {
				nextChar();
				dotcount++;
				if (b == '.') {
				    nextChar();
				    dotcount++;
				    if (b == '.') {
					nextChar();
					dotcount++;
				    }
				}
			    }
			    if (dotcount > 0) {
				if (dotcount == 3) {
				    skipWhitespace();
				}
				if (dotcount == 3  && b == -1) {
				    return;
				} else {
				    String msg = errorMsg("badTermination");
				    throw new JSException(lineno, msg);
				}
			    }
			}
			if (b == '-') {
			    locator.pushLevel();
			    long ln = lineno;
			    nextChar();
			    if (col == 1 && sawTag && b == '-') {
				// check for '---' at the start of a line
				nextChar();
				if (b == '-') {
				    nextChar();
				    return;
				} else {
				    String msg = errorMsg("badTermination");
				    throw new JSException(lineno, msg);
				}
			    } else if (b == ' ' || b == '\r' || b == '\n') {
				nextChar();
			    }
			    if (ln == keyLineno) {
				String msg = errorMsg("arrayAfterKey");
				throw new JSException(lineno, msg);
			    }
			    // list entries
			    int ncol = col;
			    int flag = '-';
			    JSArray array = new JSArray();
			    StringBuilder asb = new StringBuilder();
			    while (flag == '-' && ncol == col) {
				String anchor1 = null;
				skipWhitespace();
				int ccol = getColumn();
				getTag(true);
				if (b == '&') {
				    nextChar();
				    asb.setLength(0);
				    while (isAnchorChar(b)) {
					asb.append((char)b);
					nextChar();
				    }
				    anchor1 = asb.toString();
				    long ln1 = lineno;
				    skipWhitespace();
				    if (lineno != ln1) {
					ccol = getColumn();
				    }
				    getTag(true);
				}
				if (ccol > ncol) {
				    locator.incrList();
				    tagLocation();
				    Object obj = parseValue(ncol, ccol);
				    if (currentTag != null
					&& obj != null
					&& obj instanceof String) {
					obj = toObject((String)obj);
				    } else {
					currentTag = null;
					currentTagRef = null;
				    }
				    array.addObject(obj);
				    if (anchor != null) {
					refmap.put(anchor, obj);
				    }
				    skipWhitespace();
				    flag = b;
				    ncol = getColumn();
				    if (ncol == col && flag == '-') {
					nextChar();
				    }
				} else {
				    flag = 0;
				}
			    }
			    locator.popLevel();
			    if (ncol > col) {
				String msg =
				    errorMsg("badTerminationCol", ncol);
				throw new JSException(lineno, msg);
			    } else {
				if (object.containsKey(key)) {
				    String msg = errorMsg("duplicateKey", key);
				    throw new JSException(lineno, msg);
				}
				if (key == null) {
				    object.put(lastkey, array);
				} else {
				    object.put(key, array);
				}
				skipWhitespace();
				col = getColumn();
				if (col != nextIndent && col <= prevIndent) {
				    return;
				}

			    }
			}
			sb.setLength(0);
			while (b == '.' ||
			       Character.isJavaIdentifierPart((char)b)) {
			    sb.append((char)b);
			    nextChar();
			}
			if (b == ':') {
			    long ln = lineno;
			    nextChar();
			    if (b == ' ' || b == '\r' || b == '\n') {
				// found a key
				key = sb.toString();
				locator.setKey(key);
				tagLocation();
				if (ln == keyLineno) {
				    String msg = errorMsg("oneKeyPerLine");
				    throw new JSException(lineno, msg);
				}
				skipWhitespace();
				col = getColumn();
				getTag(true);
				if (b == '&') {
				    nextChar();
				    StringBuilder asb = new StringBuilder();
				    while (isAnchorChar(b)) {
					asb.append((char)b);
					nextChar();
				    }
				    anchor = asb.toString();
				    long ln1 = lineno;
				    skipWhitespace();
				    if (lineno != ln1) {
					col = getColumn();
				    }
				    getTag(true);
				}
				continue;
			    }
			} else {
			    return;
			}
		    } else {
			return;
		    }
		}
	    }

	    private Object tryParseObject(int minCont, int startCol,
					  boolean top)
		throws IOException
	    {
		sb.setLength(0);
		while(b != -1
		      && Character.isWhitespace((char)b)) {
		    nextChar();
		    if (b == '#') {
			while (b != '\n' && b != -1) {
			    nextChar();
			}
		    }
		}
		switch (b) {
		case '*':
		    StringBuilder asb = new StringBuilder();
		    nextChar();
		    while (isAnchorChar(b)) {
			asb.append((char)b);
			nextChar();
		    }
		    while (b == ' ' || b == '#') {
			if (b == '#') {
			    while (b != '\n') nextChar();
			}
			nextChar();
		    }
		    String ref = asb.toString();
		    if (refmap.containsKey(ref)) {
			return refmap.get(ref);
		    } else {
			String msg = errorMsg("missingAnchor", ref);
			throw new JSException(lineno, msg);
		    }
		case '"': case '\'': case '|': case '>':
		    return parseValue(startCol, getColumn());
		case '{': case '[':
		    if (top) {
		    return null;
		    } else {
			return parseValue(startCol, getColumn());
		    }
		}
		int cloc = getColumn();
		while (b != -1) {
		    if (b == '.'
			|| Character.isJavaIdentifierPart ((char)b)) {
			sb.append((char)b);
		    } else {
			break;
		    }
		    nextChar();
		}
		if (b == ':') {
		    locator.pushLevel();
		    long ln = lineno;
		    nextChar();
		    if (b == -1) {
			if (ln == keyLineno) {
			    String msg = errorMsg("oneKeyPerLine");
			    throw new JSException(lineno, msg);
			}
			keyLineno = ln;
			JSObject object = new JSObject();
			String key = sb.toString();
			if (object.containsKey(key)) {
			    String msg = errorMsg("duplicateKey", key);
			    throw new JSException(lineno, msg);
			}
			locator.setKey(key);
			tagLocation();
			object.put(key, (Object)null);
			// currentObject = object;
			locator.popLevel();
			return object;
		    }
		    if (b == '\r') nextChar();
		    if (b == ' ' || b == '\n') {
			if (ln == keyLineno) {
			    String msg = errorMsg("oneKeyPerLine");
			    throw new JSException(lineno, msg);
			}
			keyLineno = ln;
			JSObject object  = new JSObject();
			String key = sb.toString();
			locator.setKey(key);
			tagLocation();
			parse(object, key, startCol, cloc);
			// currentObject = object;
			locator.popLevel();
			return object;
		    } else {
			sb.append(':');
		    }
		    locator.popLevel();
		}
		while (b != '\n' && b != -1) {
		    if (b != '\r') {
			sb.append((char)b);
		    }
		    nextChar();
		}
		String s3 = sb.toString().stripTrailing();
		if (hasComment(s3)) {
		    s3 = stripComment(s3);
		}
		sb.setLength(0);
		sb.append(s3);
		long cldiff = 0;
		while (b != -1 && Character.isWhitespace(b)) {
		    nextChar();
		    if (b == '#') {
			while (b != -1 && b != '\n') {
			    nextChar();
			}
		    }
		    if (b == '\n') cldiff++;
		}
		int col3 = getColumn();
		StringBuilder sb2 = new StringBuilder();
		while (col3 > minCont && b != -1) {
		    boolean strip = false;
		    for (;;) {
			if (b != '\r' && b != '\n' && b != -1) {
			    if (b == '#') strip = true;
			    if (strip == false) {
				sb2.append((char)b);
			    }
			} else if (b == '\n' || b == -1) {
			    break;
			}
			nextChar();
		    }
		    // cldiff++;
		    String sb2s = sb2.toString().stripTrailing();
		    if (col3 == 0) {
			if (sb2s.equals("...")) {
			    termination = Termination.TERMINATED_DELAYED;
			    return toObject(sb.toString());
			} else if (sb2s.equals("---")) {
			    termination =
				Termination.NEXT_OBJECT_AVAILABLE_DELAYED;
			    return toObject(sb.toString());
			}
		    }
		    if (col3 >= startCol) {
			int ind = sb2s.indexOf(":");
			if (ind != -1) {
			    ind++;
			    if (ind == sb2s.length() ||
				Character.isWhitespace(sb2s.charAt(ind))) {
				String msg = errorMsg("keyInString", sb2s);
				throw new JSException(lineno, msg);
			    }
			}
		    }
		    if (sb2.length() > 0) {
			if (cldiff <= 1) {
			    sb.append(" ");
			} else {
			    for (long i = 1; i < cldiff; i++) {
				sb.append("\n");
			    }
			}
			sb.append(sb2s.stripLeading());
			sb2.setLength(0);
		    }
		    cldiff = 0;
		    while (b != -1 && Character.isWhitespace(b)) {
			if (b == '\n') cldiff++;
			nextChar();
			if (b == '#') {
			    while (b != -1 && b != '\n') {
				nextChar();
			    }
			}
		    }
		    col3 = getColumn();
		}
		return toObject(sb.toString());
	    }


	    @Override
	    protected Object parseValue() throws IOException {
		skipWhitespace();
		int indent = getLineIndentation();
		return parseValue(indent, indent);
	    }
	    
	    /**
	     * Parse the input from the current character.
	     * For characters to be used, their indentation must be
	     * larger than that provided by prevIndent. When a line's
	     * first character's indentation is less than or equal to
	     * the prevIndent argument, that character is not used in
	     * the results returned by this method and is instead left
	     * to the caller to handle. The nextIndent argument is the
	     * value to use as the prevIndent value in a recursive call.
	     * @param prevIndent the previous line indentation
	     * @param nextIndent the next line indentation
	     *
	     */
	    protected Object parseValue(int prevIndent, int nextIndent)
		throws IOException
	    {
		if (b == -1) {
		    String msg = errorMsg("EOF");
		    throw new JSException(lineno, msg);
		}

		StringBuilder asb = new StringBuilder();
		int col = getColumn();
		switch (b)  {
		case '*':
		    nextChar();
		    while (isAnchorChar(b)) {
			asb.append((char)b);
			nextChar();
		    }
		    while (b == ' ' || b == '#') {
			if (b == '#') {
			    while (b != '\n') nextChar();
			}
			nextChar();
		    }
		    String ref = asb.toString();
		    if (refmap.containsKey(ref)) {
			return refmap.get(ref);
		    } else {
			String msg = errorMsg("missingAnchor", ref);
			throw new JSException(lineno, msg);
		    }
		case '"':
		    return parseString();
		case '[':
		case '{':
		    return super.parseValue(ydata);
		case '\'':
		    // nextChar();
		    return parseString1();
		case '|':
		    String s1 = readLine();
		    setScalarOptions('|', s1);
		    sb.setLength(0);
		    return parseMultiline(prevIndent);
		case '>':
		    String s2 = readLine();
		    setScalarOptions('>', s2);
		    sb.setLength(0);
		    return parseMultiline(prevIndent);
		case '-':
		    long ln = lineno;
		    locator.pushLevel();
		    nextChar();
		    if (b != ' ' && b != '\r' && b != '\n') {
			int cdiff = getColumn() - col - 1;
			locator.popLevel();
			sb.setLength(0);
			sb.append('-');
			sb.append((char)b);
			while (cdiff >= 0) {
			    String s = readLine();
			    if (hasComment(s)) {
				s = stripComment(s);
				sb.append(s);
				return toObject(sb.toString());
			    } else {
				sb.append(s);
				skipWhitespace();
				cdiff = getColumn() - col;
			    }
			}
			return toObject(sb.toString());
		    } else {
			if (ln == keyLineno) {
			    String msg = errorMsg("arrayAfterKey");
			    throw new JSException(lineno, msg);
			}
			// list entries
			int ncol = col;
			int flag = '-';
			JSArray array = new JSArray();
			while (flag == '-' && ncol == col) {
			    String anchor = null;
			    skipWhitespace();
			    int ccol = getColumn();
			    getTag(true);
			    if (b == '&') {
				nextChar();
				asb.setLength(0);
				while (isAnchorChar(b)) {
				    asb.append((char)b);
				    nextChar();
				}
				anchor = asb.toString();
				long ln1 = lineno;
				skipWhitespace();
				if (lineno != ln1) {
				    ccol = getColumn();
				}
				getTag(true);
			    }
			    if (ccol > ncol) {
				locator.incrList();
				tagLocation();
				Object obj = parseValue(ncol, ccol);
				if (currentTag != null
				    && obj != null
				    && obj instanceof String) {
				    obj = toObject((String)obj);
				} else {
				    currentTag = null;
				    currentTagRef = null;
				}
				array.addObject(obj);
				if (anchor != null) {
				    refmap.put(anchor, obj);
				}
				skipWhitespace();
				flag = b;
				ncol = getColumn();
				if (ncol == col && flag == '-') {
				    nextChar();
				}
			    } else {
				flag = 0;
			    }
			}
			if (ncol > col) {
			    String msg = errorMsg("badTerminationCol", ncol);
			    throw new JSException(lineno, msg);
			} else {
			    locator.popLevel();
			    return array;
			}
		    }
		default:
		    sb.setLength(0);
		    Object tpo = tryParseObject(prevIndent, col, false);
		    return tpo;
		}
	    }

	    static final String LINE_SEPARATOR =
		System.getProperty("line.separator");

	    /**
	     * Parse a string.
	     * @param isIdent true if the string is an identifier without
	     *        double quotes; false otherwise
	     * @return the string
	     * throws IOException an error occurred, typically due to the
	     *        input stream being read
	     */
	    protected String parseString(boolean isIdent) throws IOException {
		if (isIdent) {
		    sb.setLength(0);
		    sb.append((char) b);
		    nextChar();
		    while (b != -1) {
			if (b == '.') {
			    sb.append((char)b);
			    nextChar();
			} else if (!Character.isJavaIdentifierPart((char)b)) {
			    return sb.toString();
			} else {
			    sb.append((char)b);
			    nextChar();
			}
		    }
		    String msg = errorMsg("EOF");
		    throw new JSException(lineno, msg);
		}
		// initial '"' already read.
		sb.setLength(0);
		nextChar();
		int nlcount = 0;
		while(b != -1) {
		    switch (b) {
		    case '\r':
			nextChar();
			if (b != '\n') {
			    String msg = errorMsg("badNewLine");
			    throw new JSException(lineno, msg);
			}
			continue;
		    case '\n':
			nlcount = 0;
			while (b != -1) {
			    nextChar();
			    while (Character.isWhitespace(b) && b != '\r'
				   && b != '\n') {
				nextChar();
			    }
			    if (b == '\r') {
				nextChar();
				if (b != '\n') {
				    if (nlcount == 0) {
					sb.append(' ');
				    } else {
					for (int i = 0; i < nlcount; i++) {
					    sb.append(LINE_SEPARATOR);
					}
					break;
				    }
				} else {
				    nlcount++;
				}
			    } else if (b == '\n') {
				nlcount++;
			    } else {
				if (nlcount == 0) {
				    sb.append(' ');
				} else {
				    for (int i = 0; i < nlcount; i++) {
					sb.append(LINE_SEPARATOR);
				    }
				}
				break;
			    }
			}
			if (b == -1) {
			    String msg = errorMsg("EOF");
			    throw new JSException(lineno, msg);
			}
			continue;
		    case '\\':
			nextChar();
			switch (b) {
			case '"':
			    sb.append('"');
			    break;
			case '\\':
			    sb.append('\\');
			    break;
			case '/':
			    sb.append('/');
			    break;
			case 'b':
			    sb.append("\b");
			    break;
			case 'f':
			    sb.append("\f");
			    break;
			case 'n':
			    sb.append("\n");
			    break;
			case 'r':
			    sb.append("\r");
			    break;
			case 't':
			    sb.append("\t");
			    break;
			case 'u':
			    int codepoint = 0;
			    for (int i = 0; i < 4; i++) {
				nextChar();
				switch(b) {
				case -1:
				    String msg = errorMsg("EOF");
				    throw new JSException(lineno, msg);
				case '0':
				    b = 0;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '1':
				    b = 1;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '2':
				    b = 2;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '3':
				    b = 3;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '4':
				    b = 4;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '5':
				    b = 5;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '6':
				    b = 6;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '7':
				    b = 7;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '8':
				    b = 8;
				    codepoint = (codepoint << 4) | b;
				    break;
				case '9':
				    b = 9;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'a':
				case 'A':
				    b = 10;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'b':
				case 'B':
				    b = 11;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'c':
				case 'C':
				    b = 12;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'd':
				case 'D':
				    b = 13;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'e':
				case 'E':
				    b = 14;
				    codepoint = (codepoint << 4) | b;
				    break;
				case 'f':
				case 'F':
				    b = 15;
				    codepoint = (codepoint << 4) | b;
				    break;
				default:
				    msg = errorMsg("hexDigit");
				    throw new
					JSException(lineno, msg);
				}
			    }
			    b = codepoint;
			    sb.append((char)codepoint);
			    break;
			default:
			    String msg = errorMsg("illegalEscape", (char)b, b);
			    throw new JSException(lineno, msg);
			}
			break;
		    case '"':
			nextChar();
			return sb.toString();
		    default:
			sb.append((char)b);
		    }
		    if (Character.isISOControl(b)) {
			String msg = errorMsg("controlNotAllowed", b);
			throw new JSException(lineno, msg);
		    }
		    nextChar();
		}
		String msg = errorMsg("EOF");
		throw new JSException(lineno, msg);
	    }

	    /**
	     * Parse a string whose delimiters are single quotes.
	     * @return the string
	     * throws IOException an error occurred, typically due to the
	     *        input stream being read
	     */
	    protected String parseString1() throws IOException {
		StringBuilder sb = new StringBuilder();
		int nlcount = 0;
		nextChar();	// was looking at "'"
		while (b != -1) {
		    switch (b) {
		    case '\'':
			nextChar();
			if (b == '\'') sb.append('\'');
			else return sb.toString();
			break;
		    case '\r':
			nextChar();
			if (b != -1 && b != '\n') {
			    sb.append('\r');
			    break;
			}
			continue;
		    case '\n':
			nlcount = 0;
			while (b != -1) {
			    nextChar();
			    while (Character.isWhitespace(b) && b != '\r'
				   && b != '\n') {
				nextChar();
			    }
			    if (b == '\r') {
				nextChar();
				if (b != '\n') {
				    if (nlcount == 0) {
					sb.append(' ');
				    } else {
					for (int i = 0; i < nlcount; i++) {
					    sb.append(LINE_SEPARATOR);
					}
				    }
				    sb.append((char)b);
				    break;
				} else {
				    nlcount++;
				}
			    } else if (b == '\n') {
				nlcount++;
			    } else {
				if (nlcount == 0) {
				    sb.append(' ');
				} else {
				    for (int i = 0; i < nlcount; i++) {
					sb.append(LINE_SEPARATOR);
				    }
				}
				sb.append((char)b);
				break;
			    }
			}
			if (b == -1) {
			    String msg = errorMsg("EOF");
			    throw new JSException(lineno, msg);
			}
			break;
		    default:
			sb.append((char)b);
		    }
		    nextChar();
		}
		// We reached the end of the file but without seeing
		// a closing "'", so throw an exception
		String msg = errorMsg("EOF");
		throw new JSException(lineno, msg);
	    }
	    static final String NLPATTERN0 = "([^\n])[\n]([ ])";
	    static final String NLPATTERN1 = "([^\n])[\n]([^\n])";
	    static final String NLPATTERN2 = "[\n]([\n]+)";
	    static final Pattern pattern0 = Pattern.compile(NLPATTERN0);
	    static final Pattern pattern1 = Pattern.compile(NLPATTERN1);
	    static final Pattern pattern2 = Pattern.compile(NLPATTERN2);

	    private String parseMultiline(int startingIndent)
		throws IOException
	    {
		int lastIndent = startingIndent;
		sb.setLength(0);
		skipWhitespace1();
		if (contIndent == 0) {
		    contIndent = getLineIndentation();
		    if (contIndent < startingIndent) {
			String msg = errorMsg("indentation");
			throw new JSException(lineno, msg);
		    }
		} else {
		    contIndent = startingIndent + contIndent - 1;
		}
		int lindent = getLineIndentation();
		while (b != -1 && lindent >= contIndent) {
		    if (lindent > contIndent) {
			for (int i = contIndent; i < lindent; i++) {
			    sb.append(' ');
			}
		    }
		    sb.append(readLine());
		    while (b == '\n') {
			skipWhitespace1();
			sb.append('\n');
		    }
		    lindent = getLineIndentation();
		}
		int ind;
		int len = sb.length();
		switch (style) {
		case LITERAL:
		    switch (chomping) {
		    case KEEP:
			break;
		    case CLIP:
			sb.append('\n');
		    case STRIP:
			for (ind = len-1; ind >= 0; ind--) {
			    if (sb.charAt(ind)  != '\n') {
				ind++;
				break;
			    }
			}
			if (ind < 0) ind = 0;
			if (ind < len) {
			    sb.delete(ind, len);
			}
		    }
		    return sb.toString();
		case FOLDED:
		    switch (chomping) {
		    case KEEP:
			sb.append('\n');
			break;
		    case CLIP:
			sb.append('\n');
		    case STRIP:
			for (ind = len-1; ind >= 0; ind--) {
			    if (sb.charAt(ind)  != '\n') {
				ind++;
				break;
			    }
			}
			if (ind < 0) ind = 0;
			if (ind < len) {
			    sb.delete(ind, len);
			}
		    }
		    String s = sb.toString();
		    Matcher m0 = pattern0.matcher(s);
		    s = m0.replaceAll("$1\n\n$2");
		    Matcher m1 = pattern1.matcher(s);
		    s = m1.replaceAll("$1 $2");
		    Matcher m2 = pattern2.matcher(s);
		    return m2.replaceAll("$1");
		default:
		    throw new UnexpectedExceptionError();
		}
	    }
	}
    }
}

//  LocalWords:  exbundle JSUtilities JSON NamedObjectOps JSObject JS
//  LocalWords:  JSArray IOException Cntrl InputStream charsetName zA
//  LocalWords:  charset UnsupportedEncodingException CharsetDecoder
//  LocalWords:  charsetDecoder formated badComment needsDoubleQuote
//  LocalWords:  missingColon missingComma EOF hexDigit illegalEscape
//  LocalWords:  controlNotAllowed illformedNumber badNumber dec YAML
//  LocalWords:  expectingBoolean expectingNull illegalChar parsers
//  LocalWords:  whitespace CommentMode param EOL isIdent getResults
//  LocalWords:  hasNext noParserForTag parseError parserMatch noTag
//  LocalWords:  illegalStyle bool str ObjectParser badYAMLDirective
//  LocalWords:  multipleYAMLDirectives notDirectiveEndMark YOBJECT
//  LocalWords:  init inputNotTerminated badTerminationCol parseValue
//  LocalWords:  badTermination oneKeyPerLine missingAnchor nextChar
//  LocalWords:  currentObject arrayAfterKey badNewLine
