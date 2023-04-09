package org.bzdev.obnaming;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.Writer;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.bzdev.io.DetabReader;
import org.bzdev.lang.MathOps;
import org.bzdev.lang.spi.ONLauncherData;
import org.bzdev.obnaming.spi.ONLauncherProvider;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
import  org.bzdev.util.JSOps;
import org.bzdev.util.JSUtilities;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ObjectParser;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.units.MKS;

// for tables - there are very few so we just put the
// entries in manually using addObject methods.

import java.io.OutputStream;
import org.bzdev.io.DirectoryAccessor;
import org.bzdev.io.DelayedFileOutputStream;
import org.bzdev.io.DelayedRandomAccessFile;
import org.bzdev.io.DelayedFileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//@exbundle org.bzdev.obnaming.lpack.Launcher

/**
 * Base class for object-namer launchers.
 * An object-namer launcher allows on the process an instance of
 * {@link JSOps} (e.g., a{@link JSObject} or a {@link JSArray}) to
 * configure an application and run it as an alternative to using a
 * scripting language in cases.  The application is assumed to consist
 * of an object namer, named objects, and named object factories.  The
 * {@link org.bzdev.util.ExpressionParser} class is used to provide an
 * <A HREF="{@docRoot}/org.bzdev.base/org/bzdev/util/doc-files/esp.html">
 * ESP-like scripting environment</A>, but without the ability to
 * explicitly import Java classes.  Examples of classes that have
 * object-namer launchers are the org.bzdev.anim2d package, the
 * org.bzdev.devqsim package, or the org.bzdev.drama package in cases
 * for cases where factories and simple constructors can be used to
 * create objects and where operations are simply sequences of
 * expressions.
 * <P>
 * Please visit <A HREF="doc-files/launcher.html"> the object-namer
 * launcher documentation</A> and the manual page for the
 * <CODE>yrunner</CODE> for additional information.
 */
public abstract class ObjectNamerLauncher
    implements AutoCloseable
{
    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.obnaming.lpack.Launcher");

    static String errorMsg(String key, Object... args)
	throws NullPointerException, MissingResourceException
    {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    private static ThreadLocal<ObjectNamerLauncher> self = new ThreadLocal<>();
    
    ExpressionParser ep;

    private static Class<?> defaultRetTypes[] = {
	Writer.class,
	NamedObjectOps.class,
	ObjectNamerOps.class,
	NamedObjectFactory.class,
	Charset.class
    };
    private static Class<?> defaultArgTypes[] = {
	OutputStream.class,
	InputStream.class,
	Writer.class,
	PrintWriter.class,
	PrintStream.class,
	DirectoryAccessor.class,
	DelayedFileOutputStream.class,
	DelayedRandomAccessFile.class,
	DelayedFileInputStream.class
    };
    private static Class<?> defaultFunctionClasses[] = {
	Writer.class,
	Math.class,
	MathOps.class,
	MKS.class
    };
    private static Class<?> defaultMethodClasses[] = {
	Writer.class,
	PrintWriter.class,
	PrintStream.class,
	DirectoryAccessor.class,
	DelayedFileOutputStream.class,
	DelayedRandomAccessFile.class,
	DelayedFileInputStream.class
    };

    private static void initSeen(Set<Object>seen, String category) {
	seen.clear();
	if (category.equals("returnTypes")) {
	    for (Class<?> c: defaultRetTypes) {
		seen.add(c);
	    }
	} else if (category.equals("argumentTypes")) {
	    for (Class<?> c: defaultArgTypes) {
		seen.add(c);
	    }
	} else if (category.equals("functionClasses")) {
	    for (Class<?> c: defaultFunctionClasses) {
		seen.add(c);
	    }
	} else if (category.equals("methodClasses")) {
	    for (Class<?> c: defaultMethodClasses) {
		seen.add(c);
	    }
	}
    }

    /**
     * Constructor given an expression parser.
     * @param ep the expression parser
     * @exception IllegalStateException an object namer launcher is
     *            currently active in this thread
     */
    protected ObjectNamerLauncher(ExpressionParser ep)
	throws IllegalStateException
    {
	if (self.get() != null) {
	    throw new IllegalStateException(errorMsg("launcherExists"));
	} else {
	    self.set(this);
	}
	this.ep = ep;
	ep.setScriptingMode();
	ep.setPrefixMode();
    }

    private static final String[] inames = {
	"returnTypes",
	"argumentTypes",
	"functionClasses",
	"methodClasses",
	"fieldClasses"
    };

    /**
     * Merge two initializers.
     * This is useful in constructors that combine their default
     * initializer with an initilizer provided as an argument
     * @param initializer1 the first initializer
     * @param initializers the remaining initializers
     * @return the merger of the two initializers
     */
    protected static JSObject combine(JSObject initializer1,
				      JSObject... initializers)
    {
	JSObject result = new JSObject();
	HashSet<Object> seen = new HashSet<>(64);
	for (String key: inames) {
	    JSArray array = new JSArray();
	    initSeen(seen, key); // prune what we manually add
	    if (initializer1 != null) {
		JSArray array1 = (JSArray) initializer1.get(key);
		if (array1 != null) {
		    for (Object obj: array1) {
			if (seen.contains(obj)) continue;
			seen.add(obj);
			array.addObject(obj);
		    }
		}
	    }
	    for (JSObject initializer2: initializers) {
		if (initializer2 != null) {
		    JSArray array2 = (JSArray) initializer2.get(key);
		    if (array2 != null) {
			for (Object obj: array2) {
			    if (seen.contains(obj)) continue;
			    seen.add(obj);
			    array.addObject(obj);
			}
		    }
		}
		result.put(key, array);
	    }
	}
	return result;
    }

    /**
     * Constructor given an initializer.
     * @param initializer the initializer.
     * @exception IllegalStateException an object namer launcher is
     *            currently active in this thread
     * @exception IllegalAccessException if a Constructor object is
     * enforcing Java language access control and the underlying
     * constructor is inaccessible.
     * @exception ClassNotFoundException a class needed to initialize
     *            this object could not be found
     */
    public ObjectNamerLauncher(JSObject initializer)
	throws ClassNotFoundException, IllegalAccessException,
	       IllegalStateException
    {
	this(init(initializer));
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static void loadFromStreamAux
	(JSArray target, Class<?> lclass, JSArray array)
	throws ClassNotFoundException, IllegalAccessException, IOException
    {
	int len = array.size();
	for (int i = 0; i < len; i++) {
	    Object obj = array.get(i);
	    if (obj instanceof String) {
		// class name.
		String nm = (String) obj;
		String n = nm;
		while (true) {
		    try {
			Class<?> c = lclass.forName(n);
			// array.setObject(i, c);
			target.addObject(c);
			break;
		    } catch (ClassNotFoundException e) {
			int index = n.lastIndexOf('.');
			if (index < 0) {
			    ClassNotFoundException ne =
				new ClassNotFoundException(nm);
			    ne.setStackTrace(e.getStackTrace());
			    throw ne;
			}
			n = n.substring(0, index) + "$"
			    + n.substring(index+1);
		    }
		}
	    } else if (obj instanceof JSArray) {
		loadFromStreamAux(target, lclass, (JSArray) obj);
	    }
	}

    }

    /**
     * Load a {@link JSObject} given a stream containing YAML data.
     * The data is assumed to be UTF-8 encoded text. The tabspacing
     * argument will add a filter that handles evenly spaced tabs in
     * case these appear in the input. While 8 is a frequently used
     * value, there are no standards specifying a specific number.
     * @param lclass the class whose class loader will be used to
     *        convert class names to classes
     * @param is the input stream
     * @param tabspacing the tab spacing (0 implies no tab processing)
     * @return the object loaded from the input stream
     * @exception ClassNotFoundException a class needed to initialize
     *            this object could not be found
     * @exception IOException an IO error occurred
     * @exception IllegalAccessException if a Constructor object is
     *            enforcing Java language access control and the underlying
     *            constructor is inaccessible.
     */
    protected static JSObject loadFromStream(Class<?> lclass, InputStream is,
					     int tabspacing)
	throws ClassNotFoundException, IllegalAccessException, IOException
    {
	Reader r = new InputStreamReader(is, UTF8);
	r = (tabspacing < 1)? r: new DetabReader(r, tabspacing);
	JSObject object = (JSObject) JSUtilities.YAML.parse(r);
	if (object != null) {
	    for (String name: inames) {
		JSArray array = (JSArray)object.get(name);
		if (array != null) {
		    JSArray a = new JSArray();
		    loadFromStreamAux(a, lclass, array);
		    object.put(name, a);
		}
	    }
	}
	return object;
    }
				       
    private static ExpressionParser init(JSObject initializer)
	throws ClassNotFoundException, IllegalAccessException
    {
	Class<?>[] returnTypes = null;	
	Class<?>[] argumentTypes = null;	
	Class<?>[] functionClasses = null;	
	Class<?>[] methodClasses = null;	
	Class<?>[] fieldClasses = null;	

	Object obj = initializer.get("returnTypes");
	if (obj == null) {
	    obj = new JSArray();
	}
	if (obj instanceof JSArray) {
	    JSArray array = (JSArray) obj;
	    for (Class<?> c: defaultRetTypes) {
		array.addObject(c);
	    }
	    /*
	    array.addObject(NamedObjectOps.class);
	    array.addObject(ObjectNamerOps.class);
	    array.addObject(NamedObjectFactory.class);
	    array.addObject(Charset.class);
	    */
	    int len = array.size();
	    returnTypes = new Class<?>[len];
	    for (int i = 0; i < len; i++) {
		returnTypes[i] = (Class<?>)array.get(i);
	    }
	}
	obj = initializer.get("argumentTypes");
	if (obj != null && obj instanceof JSArray) {
	    JSArray array = (JSArray) obj;
	    for (Class<?> c: defaultArgTypes) {
		array.addObject(c);
	    }
	    /*
	    array.addObject(OutputStream.class);
	    array.addObject(InputStream.class);
	    array.addObject(PrintWriter.class);
	    array.addObject(PrintStream.class);
	    array.addObject(DirectoryAccessor.class);
	    array.addObject(DelayedFileOutputStream.class);
	    array.addObject(DelayedRandomAccessFile.class);
	    array.addObject(DelayedFileInputStream.class);
	    */
	    int len = array.size();
	    argumentTypes = new Class<?>[len];
	    for (int i = 0; i < len; i++) {
		argumentTypes[i] =  (Class<?>)array.get(i);
	    }
	}
	obj = initializer.get("functionClasses");
	if (obj != null && obj instanceof JSArray) {
	    JSArray array = (JSArray) obj;
	    for (Class<?> c: defaultFunctionClasses) {
		array.addObject(c);
	    }
	    // array.addObject(Math.class);
	    int len = array.size();
	    functionClasses = new Class<?>[len];
	    for (int i = 0; i < len; i++) {
		functionClasses[i] = (Class<?>)array.get(i);
	    }
	}
	obj = initializer.get("methodClasses");
	if (obj != null && obj instanceof JSArray) {
	    JSArray array = (JSArray) obj;
	    for (Class<?> c: defaultMethodClasses) {
		array.addObject(c);
	    }
	    /*
	    array.addObject(PrintWriter.class);
	    array.addObject(PrintStream.class);
	    array.addObject(DirectoryAccessor.class);
	    array.addObject(DelayedFileOutputStream.class);
	    array.addObject(DelayedRandomAccessFile.class);
	    array.addObject(DelayedFileInputStream.class);
	    */
	    int len = array.size();
	    methodClasses = new Class<?>[len];
	    for (int i = 0; i < len; i++) {
		methodClasses[i] = (Class<?>)array.get(i);
	    }
	}
	obj = initializer.get("fieldClasses");
	if (obj != null && obj instanceof JSArray) {
	    JSArray array = (JSArray) obj;
	    int len = array.size();
	    fieldClasses = new Class<?>[len];
	    for (int i = 0; i < len; i++) {
		fieldClasses[i] =  (Class<?>)array.get(i);
	    }
	}
	return new
	    ExpressionParser(returnTypes, argumentTypes, functionClasses,
			     methodClasses, fieldClasses);
    }


    /**
     * Get the current launcher.
     * @return the current launcher; null if there is none
     */
    protected static ObjectNamerLauncher  currentLauncher() {
	return self.get();
    }

    ObjectNamerOps namer = null;


    /**
     * Get the value of a variable stored by the current launcher.
     * @param vname the name of the variable
     * @return the value of the variable
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static Object get(String vname) throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.get(vname);
    }

    /**
     * Determine if a variable is stored in the current launcher.
     * @param name the name of the variable
     * @return true if the variable exists; false otherwise
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static boolean exists(String name) throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.exists(name);
    }


    /**
     * Set the value of a variable stored in the current launcher.
     * @param vname the name of the variable
     * @param value the value of the variable
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static void set(String vname, Object value)
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	launcher.ep.set(vname, value);
    }

    /**
     * Remove a variable from the current launcher.
     * @param name the name of the variable
     * @return the value of the variable being removed
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static Object remove(String name) throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.remove(name);
    }

    /**
     * Get the variables stored in the current launcher.
     * @return a set containing the names of the variables
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static Set<String> variables() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.variables();
    }

    /**
     * Remove all variables from the current launcher and
     * reset it.
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static void clear() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	launcher.ep.clear();
    }

    /*
    private static class Tag {
	int level = -1;
	ArrayList<Integer> tags = new ArrayList<>();
	ArrayList<String> keys = new ArrayList<>();

	Tag() {}

	int size() {
	    return tags.size();
	}

	void pushLevel() {
	    level++;
	    tags.add(0);
	    keys.add(null);
	}

	void popLevel() {
	    tags.remove(level);
	    keys.remove(level);
	    level--;
	}

	void incrList() {
	    tags.set(level, 1+tags.get(level));
	    keys.set(level, null);
	}
	void setKey(String value) {
	    keys.set(level, value);
	}

	String location() {
	    StringBuilder sb = new StringBuilder();
	    if (tags.size() == 0) {
		return null;
	    }
	    String msg = errorMsg("location");
	    sb.append("### " + msg + " ");
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
	    sb.append(LINE_SEP);
	    sb.append("### " + errorMsg("locationDescription"));
	    return sb.toString();
	}
    }
    */
    private static final String LINE_SEP = System.lineSeparator();

    // The 'equals operation for a HashSet is expensive as it
    // checks everything in the set so we use a TreeSet instead
    // after arranging that each JSOps instance has a unique ID.
    TreeSet<JSOps> seenSet = new TreeSet<>((x,y) -> {
	    if (x == y) return 0;
	    long idx = x.identity();
	    long idy = y.identity();
	    long d = (idx - idy);
	    return (d < 0)? -1: (d > 0)? 1: 0;
    });

    private void scan(Object object, boolean ymode, JSUtilities.Locator tag) {
	if (object instanceof JSObject) {
	    scanObject((JSObject) object, ymode, tag);
	} else if (object instanceof JSArray) {
	    scanArray((JSArray)object, ymode, tag);
	} else if (ymode) {
	    if (object instanceof ObjectParser.Source) {
		((ObjectParser.Source) object).evaluate();
	    }
	} else if (object instanceof String) {
	    ObjectNamerLauncher launcher = self.get();
	    String s = (String) object;
	    if (launcher.ep.matches(s)) {
		launcher.ep.parse(s);
	    }
	}
    }

    private void scanObject(JSObject object, boolean ymode, JSUtilities.Locator tag) {
	ObjectNamerLauncher launcher = self.get();
	if(launcher.seenSet.contains(object)) return;
	launcher.seenSet.add(object);
	tag.pushLevel();
	for (Map.Entry<String,Object> entry: object.entrySet()) {
	    String key = entry.getKey();
	    tag.setKey(key);
	    Object value = entry.getValue();
	    if (value != null) {
		if (ymode) {
		    if (value instanceof ObjectParser.Source) {
			((ObjectParser.Source) value).evaluate();
		    } else if (value instanceof JSObject) {
			scanObject((JSObject) value, ymode, tag);
		    } else if (value instanceof JSArray) {
			scanArray((JSArray) value, ymode, tag);
		    }
		} else if (value instanceof String) {
		    String s = (String) value;
		    if (launcher.ep.matches(s)) {
			object.putObject(key, launcher.ep.parse(s));
		    }
		} else if (value instanceof JSObject) {
		    scanObject((JSObject) value, ymode, tag);
		} else if (value instanceof JSArray) {
		    scanArray((JSArray) value, ymode, tag);
		}
	    }
	}
	tag.popLevel();
    }

    private void scanArray(JSArray array, boolean ymode, JSUtilities.Locator tag) {
	ObjectNamerLauncher launcher = self.get();
	if(launcher.seenSet.contains(array)) return;
	launcher.seenSet.add(array);
	tag.pushLevel();
	int index = 0;
	for (Object value: array) {
	    tag.incrList();
	    if (value != null) {
		if (ymode) {
		    if (value instanceof ObjectParser.Source) {
			((ObjectParser.Source) value).evaluate();
		    } else if (value instanceof JSObject) {
			scanObject((JSObject) value, ymode, tag);
		    } else if (value instanceof JSArray) {
			scanArray((JSArray) value, ymode, tag);
		    }
		} else if (value instanceof String) {
		    String s = (String) value;
		    if (launcher.ep.matches(s)) {
			array.setObject(index, launcher.ep.parse(s));
		    }
		} else if (value instanceof JSObject) {
		    scanObject((JSObject) value, ymode, tag);
		} else if (value instanceof JSArray) {
		    scanArray((JSArray) value, ymode, tag);
		}
	    }
	    index++;
	}
	tag.popLevel();
    }

    /**
     * Process a JSON or YAML string.
     * @param s the string to process
     * @param ymode true if YAML syntax is being parse; false for JSON
     * @exception Exception an error occurred
     */
    public static void process(String s, boolean ymode) throws Exception {
	process(null, s, ymode);
    }

    /**
     * Process a JSON or YAML file.
     * @param r a reader for the text stored in the file
     * @param ymode true if YAML syntax is being parse; false for JSON
     * @exception Exception an error occurred
     */
    public static void process(Reader r, boolean ymode) throws Exception {
	process(null, r, ymode);
    }


    /**
     * Process a JSON or YAML string, supplying a file name.
     * The file name is used when printing error messages.
     * @param filename a file-name like identifier to assign to the input
     *        string
     * @param s the string to process
     * @param ymode true if YAML syntax is being parse; false for JSON
     * @exception Exception an error occurred
     */
    public static void process(String filename, String s, boolean ymode)
	throws Exception
    {
	process(filename, new StringReader(s), ymode);
    }


    /**
     * Process a JSON or YAML file, supplying a file name.
     * The file name is used when printing error messages.
     * @param filename the input file name; null if not known
     * @param r a reader for the text stored in the file
     * @param ymode true if YAML syntax is being parse; false for JSON
     * @exception Exception an error occurred
     */
    public static void process(String filename, Reader r, boolean ymode)
	throws Exception
    {
	ObjectNamerLauncher launcher = self.get();
	Object object = null;
	Map<JSUtilities.Location,JSUtilities.LocationPair> map = null;
	if (ymode) {
	    ObjectParser.SourceParser sp =
		new ObjectParser.SourceParser(launcher.ep);
	    JSUtilities.YAML.Parser yparser = new JSUtilities.YAML
		.Parser(r, new
			JSUtilities.YAML.TagSpec("!bzdev!",
						 "tag:bzdev.org,2021:esp",
						 sp));
	    object = yparser.getResults();
	    map = yparser.getLocationMap();
	}  else {
	    // object = JSUtilities.JSON.parse(r);
	    JSUtilities.JSON.Parser jparser = new JSUtilities.JSON.Parser(r);
	    object = jparser.getResults();
	    map = jparser.getLocationMap();
	}
	if (object instanceof JSOps) {
	    process(filename, (JSOps) object, ymode, map);
	}
    }

    /**
     * Process a JSObject or  JSArray.
     * The argument is one that would be created as specified in the
     * documentation for {@link ObjectNamerLauncher this class}. While
     * the BZDev class library can generate the argument from JSON or
     * YAML inputs, other libraries may provide additional formats.
     * <P>
     * How to construct the map argument is described in the documentation
     * for {@link org.bzdev.util.JSUtilities.Locator}.
     * @param filename the input file name; null if not known
     * @param object the object specifying a configuration or series of
     *        actions
     * @param ymode true if the object was obtained by parsing a YAML file;
     *        false otherwise
     * @param map the mapping from parser locations to line/column numbers;
     *        null if not available
     * @exception java.lang.Exception an error occurred
     * @see org.bzdev.util.JSUtilities.Location
     * @see org.bzdev.util.JSUtilities.LocationPair
     * @see org.bzdev.util.JSUtilities.Locator
     */
    public static void process
	(String filename, JSOps object, boolean ymode,
	 Map<JSUtilities.Location,JSUtilities.LocationPair> map)
	throws Exception
    {
	JSUtilities.Locator tag = new JSUtilities.Locator();
	try {
	    process (object, ymode, tag);
	} catch (java.lang.Exception e) {
	    String emsg = e.getMessage();
	    String msg;
	    if (map == null) {
		msg = emsg + LINE_SEP
		    + "### " + ((filename == null)? "": filename + ": ")
		    + tag.toString();
	    } else {
		JSUtilities.LocationPair pair =
		    map.get(new JSUtilities.Location(tag));
		if (pair == null) {
		    msg = emsg + LINE_SEP
			+ "### " + ((filename == null)? "": filename + ": ")
			+ tag.toString();
		} else {
		    long lineno = pair.getLineNumber();
		    int col = pair.getColumn();
		    if (filename == null) {
			msg = errorMsg("error1", lineno, col, emsg);
		    } else {
			msg = errorMsg("error2", filename,  lineno, col, emsg);
		    }
		}
	    }
	    if (e instanceof ObjectParser.Exception) {
		ObjectParser.Exception oe = (ObjectParser.Exception) e;
		String input = oe.getInput();
		input = input.replaceAll("[\r\n]", " ");
		int offset = oe.getOffset();
		if (offset > 36) {
		    int start = offset - 36;
		    input = input.substring(start);
		    offset = 36;
		}
		if (input.length() > 72) {
		    input = input.substring(0, 72);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msg);
		sb.append(LINE_SEP);
		sb.append("### ");
		sb.append(input);
		sb.append(LINE_SEP);
		sb.append("### ");
		for (int i = 0; i < offset; i++) {
		    sb.append(" ");
		}
		sb.append("^");
		msg = sb.toString();
	    }
	    java.lang.Exception ex = new java.lang.Exception(msg, e);
	    ex.setStackTrace(e.getStackTrace());
	    throw ex;
	} finally {
	    ObjectNamerLauncher launcher = self.get();
	    launcher.seenSet.clear();
	}
    }

    private static void process(JSOps object, boolean ymode,
				JSUtilities.Locator tag)
	throws Exception
    {
	if (object instanceof JSArray) {
	    process((JSArray) object, ymode, tag);
	} else if (object instanceof JSObject) {
	    process((JSObject) object, ymode, tag);
	} else {
	    String msg = errorMsg("unknownType", object.getClass());
	    throw new IllegalArgumentException(msg);
	}
    }

    /**
     * Get the fully qualified names for enumeration constants and fields
     * provided by the current launcher for use in expressions.
     * <P>
     * This method is provided for applications that can list the available
     * constants as part of a 'help' or documentation subsystem.
     * @return the constants
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static List<String> getConstants() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getConstants();
    }




    /**
     * Get the classes for objects that can be returned by in expressions
     * processed by the current launcher.
     * The values returned do not include primitive types.
     * <P>
     * This method is provided for applications that can list the
     * types of objects that can be returned as part of a 'help' or
     * documentation subsystem.
     * @return the classes
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static ArrayList<Class<?>> getReturnClasses()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getArgumentClasses();
    }

    /**
     * Get the classes for objects that can be uses as arguments of
     * functions or methods in expressions processed by the current launcher.
     * The values returned do not include primitive types or the type
     * String.
     * <P>
     * This method is provided for applications that can list the
     * types of objects that can be used as arguments as part of a
     * 'help' or documentation subsystem.
     * @return the classes
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static ArrayList<Class<?>> getArgumentClasses()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getArgumentClasses();
    }

    /**
     * Get the constructors supported by the current launcher for use
     * in expressions.
     * Each element in the returned list consists of a fully qualified
     * class name followed by an open parenthesis, a comma-separated list
     * of the class names for the arguments, and a closing parenthesis.
     * <P>
     * This method is provided for applications that can list the
     * constructors and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the constructors
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static List<String> getConstructors() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getConstructors();
    }

    /**
     * Get the functions supported by the current launcher for use in
     * expressions.
     * Each element in the returned list consists of a fully qualified
     * class name followed the function name that is in turn followed
     * by an open parenthesis, a comma-separated list of the class
     * names for the arguments, and a closing parenthesis.
     * <P>
     * This method is provided for applications that can list
     * functions and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the functions
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static List<String> getFunctions() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getFunctions();
    }

    /**
     * Get the methods supported by the current launcher for use in
     * expressions.
     * Each element in the returned list consists of a fully qualified
     * class name followed by a colon, a space, and the method name.
     * The method  name is in turn followed by an open parenthesis, a
     * comma-separated list of the class names for the arguments, and
     * a closing parenthesis.
     * <P>
     * This method is provided for applications that can list
     * methods and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the methods
     * @exception IllegalStateException a parser has not been created
     *            for the current thread
     */
    public static List<String> getMethods() throws IllegalStateException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.getMethods();
    }

    /**
     * Create a table mapping package names to the location containing
     * the HTML files for that package's documentation.
     * For the javadoc directories provided by the JDK, the name of the
     * final component in the path is "api" and that directory will
     * contain a file named either element-list or package-list.
     * @param apis a list of the URLS for javadoc directories
     * @exception IOException an IO error occured
     */
    public static void createAPIMap(List<URL> apis) throws IOException {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	launcher.ep.createAPIMap(apis);
    }

    /**
     * Get a key list for generating a table of constants.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified name of a constant
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForConstants()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForConstants();
    }

    /**
     * Get a key list for generating a table of classes, instances of
     * which can be returned by an expression.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified class name of the class
     *         an expression can return
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForReturnClasses()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForReturnClasses();
    }

    /**
     * Get a key list for generating a table of .
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified class name of a class for
     *        an argument to a constructor, function or method recognized
     *        by this expression parser
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForArgumentClasses()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForArgumentClasses();
    }

    /**
     * Get a key list for generating a table of constructors.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the object
     *        a constructor will create.
     *   <LI> <B>item</B> - the simple class name followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        constructor's arguments, followed by a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForConstructors()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForConstructors();
    }

    /**
     * Get a key list for generating a table of functions.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for a class,
     *        one of whose static methods implements the function.
     *   <LI> <B>item</B> - the name of the static method representing
     *        the function followed by an open parenthesis, a
     *        comma-separated list of the types of the function's
     *        arguments, followed by a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForFunctions()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForFunctions();
    }

    /**
     * Get a key list for generating a table of instance methods,
     * excluding methods for {@link String}.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the
     *         class declaring a method.
     *   <LI> <B>item</B> - the name of the method followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        method's arguments, followed by a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList keylistForMethods()
	throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForMethods();
    }

    /**
     * Get a key list for generating a table of instance methods.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the
     *         class declaring a method.
     *   <LI> <B>item</B> - the name of the method followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        method's arguments, followed by a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @param hideString true if methods of {@link String} should not be
     *        shown; false otherwise
     * @return the key list
     * @exception IllegalStateException a launcher was not created for this
     *            thread.
     */
    public static TemplateProcessor.KeyMapList
	keylistForMethods(boolean hideString) throws IllegalStateException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.keylistForMethods(hideString);
    }

    private static void process(JSObject object, boolean ymode,
				JSUtilities.Locator tag)
	throws IllegalStateException, IllegalArgumentException
    {
	ObjectNamerLauncher launcher = self.get();
	tag.pushLevel();
	for(Map.Entry<String,Object> entry: object.entrySet()) {
	    String key = entry.getKey();
	    tag.setKey(key);
	    if (key.equals("execute")) {
		Object exec = entry.getValue();
		if (exec != null) {
		    if (ymode) {
			if (exec instanceof ObjectParser.Source) {
			    ((ObjectParser.Source) exec).evaluate();
			} else if (exec instanceof JSArray) {
			    tag.pushLevel();
			    for (Object o: (JSArray) exec) {
				tag.incrList();
				if (o instanceof ObjectParser.Source) {
				    ((ObjectParser.Source) o).evaluate();
				} else {
				    String msg =
					errorMsg("notStringList", o.getClass());
				    throw new IllegalArgumentException(msg);
				}
			    }
			    tag.popLevel();
			}
		    } else if (exec instanceof String) {
			String s = (String) exec;
			launcher.ep.parse(s);
		    } else if (exec instanceof JSArray) {
			tag.pushLevel();
			for (Object o:(JSArray) exec) {
			    tag.incrList();
			    if (o instanceof String) {
				String s = (String) o;
				launcher.ep.parse(s);
			    } else {
				String msg =
				    errorMsg("notStringList", o.getClass());
				throw new IllegalArgumentException(msg);
			    }
			}
			tag.popLevel();
		    }
		}
	    } else if (key.equals("factories")) {
		Object specs = entry.getValue();
		if (specs instanceof JSObject) {
		    processF(launcher, (JSObject) specs, tag);
		} else if (specs instanceof JSArray) {
		    processF(launcher, (JSArray) specs, tag);
		}
	    } else if (key.equals("define")) {
		launcher.scan(entry.getValue(), ymode, tag);
	    } else if (key.equals("create")) {
		Object specs = entry.getValue();
		if (specs instanceof JSObject) {
		    JSObject object1 = (JSObject) specs;
		    String variable = (String)(object1.get("var"));
		    String name = (String)(object1.get("name"));
		    String factory = (String)(object1.get("factory"));
		    Object spec = object1.get("configuration");
		    if (spec instanceof JSArray
			|| spec instanceof JSObject) {
			launcher.scan(spec, ymode, tag);
			NamedObjectFactory f = (NamedObjectFactory)
			    launcher.ep.get(factory);
			Object o = f.createObject(name, spec);
			launcher.ep.set(variable, o);
		    } else if (spec instanceof ObjectParser.Source) {
			spec = ((ObjectParser.Source) spec).evaluate();
			NamedObjectFactory f = (NamedObjectFactory)
			    launcher.ep.get(factory);
			Object o = f.createObject(name, spec);
			launcher.ep.set(variable, o);
		    } else {
			String msg = errorMsg("unknownSpec", spec.getClass());
			throw new IllegalStateException(msg);
		    }
		} else if (specs instanceof JSArray) {
		    JSArray list = (JSArray) specs;
		    tag.pushLevel();
		    for (Object obj1: (JSArray)list) {
			tag.incrList();
			if (obj1 instanceof JSObject) {
			    // tag.pushLevel();
			    JSObject object1 = (JSObject) obj1;
			    String variable = (String)(object1.get("var"));
			    if (variable == null) {
				String msg = errorMsg("noVar");
				throw new IllegalStateException(msg);
			    }
			    String name = (String)(object1.get("name"));
			    if (name == null) {
				String msg = errorMsg("noName");
				throw new IllegalStateException(msg);
			    }
			    String factory =
				(String)(object1.get("factory"));
			    if (factory == null) {
				String msg = errorMsg("noFactory");
				throw new IllegalStateException(msg);
			    }
			    Object spec = object1.get("configuration");
			    if (spec == null) {
				// we should not need a configuration if
				// there is nothing to configure, which
				// can happen in a few cases.
				NamedObjectFactory f = (NamedObjectFactory)
				    launcher.ep.get(factory);
				Object o = f.createObject(name, null);
				launcher.ep.set(variable, o);
				continue;
			    }
			    tag.setKey("configuration");
			    launcher.scan(spec, ymode, tag);
			    if (spec instanceof JSArray
				|| spec instanceof JSObject) {
				NamedObjectFactory f = (NamedObjectFactory)
				    launcher.ep.get(factory);
				Object o = f.createObject(name, spec);
				launcher.ep.set(variable, o);
			    } else if (spec instanceof ObjectParser.Source) {
				spec = ((ObjectParser.Source) spec).evaluate();
				NamedObjectFactory f = (NamedObjectFactory)
				    launcher.ep.get(factory);
				Object o = f.createObject(name, spec);
				launcher.ep.set(variable, o);
			    } else {
				String msg =
				    errorMsg("unknownSpec", spec.getClass());
				throw new IllegalStateException(msg);
			    }
			}
		    }
		    tag.popLevel();
		}
	    } else {
		String msg = errorMsg("unknownKey", key);
		throw new IllegalArgumentException(msg);
	    }
	}
	tag.popLevel();
    }

    private static void process(JSArray objects, boolean ymode,
				JSUtilities.Locator tag)
	throws Exception
    {
	tag.pushLevel();
	for (Object obj: objects) {
	    tag.incrList();
	    if (obj instanceof JSObject) {
		JSObject object = (JSObject)obj;
		process(object, ymode, tag);
	    } else if (obj instanceof JSArray) {
		process((JSArray) obj, ymode, tag);
	    } else {
		String msg = errorMsg("notObject", obj.getClass());
		throw new IllegalArgumentException(msg);
	    }
	}
	tag.popLevel();
    }

    private static void processF(ObjectNamerLauncher launcher,
				 JSObject object,
				 JSUtilities.Locator tag)
    {
	tag.pushLevel();
	Object context = object.get("context");
	if (context == null) {
	    String msg = errorMsg("noContext");
	    throw new IllegalArgumentException(msg);
	} else if (context instanceof JSArray) {
	    tag.setKey("context");
	    JSArray list = (JSArray) context;
	    if (list.size() != 2) {
		String msg = errorMsg("badContextSize");
		throw new IllegalArgumentException(msg);
	    }
	    Object o = list.get(0);
	    ObjectNamerOps namer;
	    String pkg = null;
	    if (o instanceof String) {
		String v = (String) o;
		o = launcher.ep.get((String) o);
		if (o == null) {
		    String msg = errorMsg("nullContextNamer", v);
		    throw new IllegalArgumentException(msg);
		} else if (o instanceof ObjectNamerOps) {
		    namer = (ObjectNamerOps) o;
		} else {
		    String msg = errorMsg("notContextNamer", v, o.getClass());
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("notVariableName", o.getClass());
		throw new IllegalArgumentException(msg);
	    }
	    o = list.get(1);
	    if (o == null) {
		String msg = errorMsg("nullContextPkg");
	    } else if (o instanceof String) {
		pkg = (String) o;
	    } else {
		String msg = errorMsg("notContextPkg", o.getClass());
		throw new IllegalArgumentException(msg);
	    }
	    for (Map.Entry<String,Object> entry: object.entrySet()) {
		String key = entry.getKey();
		if (key.equals("context")) continue;
		tag.setKey(key);
		Object value = entry.getValue();
		if (value == null) {
		    String msg = errorMsg("nullClassName");
		    throw new IllegalArgumentException(msg);
		} else if (value instanceof String) {
		    String cname = (String) value;
		    String name = (pkg == null)? cname:
			pkg + "." + cname;
		    try {
			NamedObjectFactory factory = namer.createFactory(name);
			launcher.ep.set(key, factory);
		    } catch (java.lang.Exception e) {
			String msg = errorMsg("factoryFailed", name, key);
			throw new IllegalArgumentException(msg, e);
		    }
		} else {
		    String msg = errorMsg("notClassName", value.getClass());
		    throw new IllegalArgumentException(msg);
		}
	    }
	} else {
	    String msg = errorMsg("badContext", context.getClass());
	    throw new IllegalArgumentException(msg);
	}
	tag.popLevel();
    }

    private static void processF(ObjectNamerLauncher launcher,
				 JSArray objects,
				 JSUtilities.Locator tag)
    {
	tag.pushLevel();
	for (Object obj: objects) {
	    tag.incrList();
	    if (obj instanceof JSObject) {
		JSObject object = (JSObject)obj;
		processF(launcher, object, tag);
	    } else if (obj instanceof JSArray) {
		processF(launcher, (JSArray) obj, tag);
	    } else {
		String msg = errorMsg("notObject", obj.getClass());
		throw new IllegalArgumentException(msg);
	    }
	}
	tag.popLevel();
    }

    /**
     * Close the currently active launcher.
     * If a launcher was created, this method must be called before
     * a new launcher can be created in the current thread.
     */
    @Override
    public void close() {
	ep = null;
	self.set(null);
    }

    private static ServiceLoader<ONLauncherProvider> loader = null;
    private static ServiceLoader<ONLauncherData> dloader = null;

    /**
     * Reset the SPI loaders.
     * This method resets the SPI (Service Provider Interface) loaders,
     * and can be used in the unusual case in which the classes accessible
     * from the class path or module path are modified.
     */
    public static synchronized void resetLoaders() {
	loader = null;
	dloader = null;
    }



    /**
     * Create a new instance of an object-namer launcher.
     * There may be only one launcher active per thread. A launcher
     * becomes active when its constructor is called and is active
     * until its {@link #close()} method is called.
     * @param name the name of launcher.
     * @return a new object-namer launcher
     * @exception IllegalAccessException if a Constructor object is
     *            enforcing Java language access control and the
     *            underlying constructor is inaccessible.
     * @exception IllegalArgumentException if the number of actual and
     *            formal parameters differ; if an unwrapping
     *            conversion for primitive arguments fails; or if,
     *            after possible unwrapping, a parameter value cannot
     *            be converted to the corresponding formal parameter
     *            type by a method invocation conversion; if a
     *            constructor pertains to an enum type.
     * @exception InstantiationException if the class that declares
     *            the underlying constructor represents an abstract class
     * @exception InvocationTargetException if an underlying
     *            constructor throws an exception
     * @exception NoSuchMethodException if there is an attempt to use
     *            a method that does not exist
     * @throws ExceptionInInitializerError if the initialization
     *            provoked by this method fails
     */
    public static synchronized ObjectNamerLauncher newInstance(String name)
	throws InstantiationException, IllegalAccessException,
	       IllegalArgumentException, InvocationTargetException,
	       NoSuchMethodException
    {
	if (loader == null) {
	    loader = ServiceLoader.load(ONLauncherProvider.class);
	}
	for (ServiceLoader.Provider<ONLauncherProvider> p:
		 loader.stream().collect(Collectors.toList())) {
	    ONLauncherProvider spi = p.get();
	    String lname = spi.getName();
	    if (lname.equals(name)) {
		Class<? extends ObjectNamerLauncher> clasz = spi.onlClass();
		return clasz.getDeclaredConstructor().newInstance();
	    }
	}
	return null;
    }

    /**
     * Create a new instance of an object namer launcher using an initializer.
     * There may be only one launcher active per thread. A launcher
     * becomes active when its constructor is called and is active
     * until its {@link #close()} method is called.
     * @param name the name of launcher.
     * @param dnames the names of providers for additional data
     * @return a new object-namer launcher
     * @exception IOException an IO error occurred
     * @exception IllegalAccessException if a constructor object is
     *            enforcing Java language access control and the
     *            underlying constructor is inaccessible.
     * @exception IllegalArgumentException if the number of actual and
     *            formal parameters differ; if an unwrapping
     *            conversion for primitive arguments fails; or if,
     *            after possible unwrapping, a parameter value cannot
     *            be converted to the corresponding formal parameter
     *            type by a method invocation conversion; if a
     *            constructor pertains to an enum type.
     * @exception ClassNotFoundException if the class for an object-namer
     *            launcher could not be found
     * @exception InstantiationException if the class that declares
     *            the underlying constructor represents an abstract
     *            class.
     * @exception InvocationTargetException if the underlying
     *            constructor throws an exception.
     * @exception ExceptionInInitializerError if the initialization
     *            provoked by this method fails.
     * @exception NoSuchMethodException if there is an attempt to use
     *            a method that does not exist
     */
    public static synchronized ObjectNamerLauncher
	newInstance(String name, String... dnames)
	throws InstantiationException, IllegalAccessException,
	       IllegalArgumentException, InvocationTargetException,
	       NoSuchMethodException, ClassNotFoundException, IOException
    {
	if (loader == null) {
	    loader = ServiceLoader.load(ONLauncherProvider.class);
	}
	if (dloader == null) {
	    dloader = ServiceLoader.load(ONLauncherData.class);
	}
	HashSet<String> ournames = new HashSet<>();
	for (String s: dnames) {
	    ournames.add(s);
	}
	ArrayList<JSObject> objects = new ArrayList<>();

	Class<? extends ObjectNamerLauncher> clasz = null;
	for (ServiceLoader.Provider<ONLauncherProvider> p:
		 loader.stream().collect(Collectors.toList())) {
	    ONLauncherProvider sp = p.get();
	    String lname = sp.getName();
	    if (lname.equals(name)) {
		clasz = sp.onlClass();
	    } else if (ournames.contains(lname)) {
		InputStream is = sp.getInputStream();
		if (is == null) {
		    throw new
			IOException(errorMsg("noLauncherClassSpec", lname));
		}
		objects.add(loadFromStream(sp.getClass(),
					   is,
					   sp.getTabSpacing()));
	    }
	}
	if (clasz == null) return null;
	HashSet<String> loaded = new HashSet<>();
	for (ServiceLoader.Provider<ONLauncherData> p:
		 dloader.stream().collect(Collectors.toList())) {
	    ONLauncherData sp = p.get();
	    String dname = sp.getName();
	    if (ournames.contains(dname)) {
		InputStream is = sp.getInputStream();
		if (is == null) {
		    String msg = errorMsg("noLauncherClassSpec", dname);
		    msg = msg + LINE_SEP + "### "
			+ sp.getClass();
		    throw new IOException(msg);
		}
		JSObject obj = loadFromStream(sp.getClass(),
					      is,
					      sp.getTabSpacing());
		objects.add(obj);
		loaded.add(dname);
	    }
	}
	for (String dn: ournames) {
	    if (!loaded.contains(dn)) {
		    String msg = errorMsg("noLauncherData", dn);
		    throw new IOException(msg);
	    }
	}
	JSObject[] others = new JSObject[objects.size()];
	others = objects.toArray(others);
	JSObject initializer = combine(new JSObject(), others);
	return clasz.getDeclaredConstructor(JSObject.class)
	    .newInstance(initializer);
    }

    /**
     * Get a all launcher names available via a service-provider
     * interface.
     * @return the names
     */
    public static synchronized String[] getLauncherNames() {
	if (loader == null) {
	    loader = ServiceLoader.load(ONLauncherProvider.class);
	}
	ArrayList<String> names = new ArrayList<>();
	for (ServiceLoader.Provider<ONLauncherProvider> p:
		 loader.stream().collect(Collectors.toList())) {
	    ONLauncherProvider spi = p.get();
	    names.add(spi.getName());
	}
	String[] result = new String[names.size()];
	return names.toArray(result);
    }

    static final Comparator<TemplateProcessor.KeyMap> KEY_COMPARATOR
	= new Comparator<>() {
		public int compare(TemplateProcessor.KeyMap keymap1,
				   TemplateProcessor.KeyMap keymap2)
		{
		    String name1 = (String) keymap1.get("name");
		    String name2 = (String) keymap2.get("name");
		    return name1.compareTo(name2);
		}
	    };

    /**
     * Get a key map describing the providers for each object-namer
     * launcher and object-namer launcher and object-namer launcher data.
     * The key map that is returned contains two iterator directives:
     * <UL>
     *   <LI><B>launcherList</B> - an iteration listing all providers for
     *       instances of {@link ObjectNamerLauncher}.
     *   <LI><B>launcherDataList</B> -an iteration listing all providers for
     *       object-name launcher data.
     * </UL>
     * Each of these lists in turn contain a key map with two directives:
     * <UL>
     *    <LI><B>name</B> - the name of a provider.
     *    <LI><B>description</B> - a description of the provider.
     * </UL>
     * @return the key map
     */
    public static synchronized TemplateProcessor.KeyMap getProviderKeyMap() {
	if (loader == null) {
	    loader = ServiceLoader.load(ONLauncherProvider.class);
	}
	if (dloader == null) {
	    dloader = ServiceLoader.load(ONLauncherData.class);
	}
	TemplateProcessor.KeyMap keymap = new TemplateProcessor.KeyMap();
	TemplateProcessor.KeyMapList list = new TemplateProcessor.KeyMapList();
	keymap.put("launcherList", list);
	for (ServiceLoader.Provider<ONLauncherProvider> p:
		 loader.stream().collect(Collectors.toList())) {
	    ONLauncherProvider spi = p.get();
	    String name = spi.getName();
	    String description = spi.description();
	    TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	    list.add(kmap);
	    kmap.put("name", name);
	    kmap.put("description", description);
	}
	Collections.sort(list, KEY_COMPARATOR);

	list = new TemplateProcessor.KeyMapList();
	keymap.put("launcherDataList", list);
	for (ServiceLoader.Provider<ONLauncherData> p:
		 dloader.stream().collect(Collectors.toList())) {
	    ONLauncherData spi = p.get();
	    String name = spi.getName();
	    String description = spi.description();
	    TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	    list.add(kmap);
	    kmap.put("name", name);
	    kmap.put("description", description);
	}
	Collections.sort(list, KEY_COMPARATOR);
	return keymap;
    }

    /**
     * Get all names for launcher-data additions available via the
     * service-provider interface.
     * @return the names
     */
    public static synchronized String[] getLauncherDataNames() {
	if (dloader == null) {
	    dloader = ServiceLoader.load(ONLauncherData.class);
	}
	ArrayList<String> names = new ArrayList<>();
	for (ServiceLoader.Provider<ONLauncherData> p:
		 dloader.stream().collect(Collectors.toList())) {
	    ONLauncherData spi = p.get();
	    names.add(spi.getName());
	}
	String[] result = new String[names.size()];
	return names.toArray(result);
    }


    /**
     * Find the method implementing a real-valued function of 1 argument.
     * The argument is the name of a static method with one
     * double-precision argument that returns a double-precision value,
     * and must include enough of the class name to be unique given the
     * classes used to define functions.
     * @param fname the name of the function
     * @return the method
     * @exception NoSuchMethodException if there is an attempt to use
     *            a method that does not exist
     */
    public static Method findRVFMethod (String fname)
	throws IllegalStateException, NoSuchMethodException,
	       IllegalArgumentException
    {
	ObjectNamerLauncher launcher = self.get();
	if (launcher == null) {
	    String msg = errorMsg("noLauncher");
	    throw new IllegalStateException(msg);
	}
	return launcher.ep.findRVFMethod(fname);
    }



}

//  LocalWords:  addObject exbundle namer JSOps JSObject JSArray JSON
//  LocalWords:  subclasses ObjectNamerLauncher YAML JSAarry subtree
//  LocalWords:  ExpressionParser NamedObjectFactory laucherExists lt
//  LocalWords:  returnTypes argumentTypes functionClasses UTF BZDev
//  LocalWords:  methodClasses fieldClasses unknownType notStringList
//  LocalWords:  YAML's unknownSpec unknownKey notObject notClassName
//  LocalWords:  badContextSize nullContextNamer notContextNamer SPI
//  LocalWords:  notVariableName nullContextPkg notContextPkg boolean
//  LocalWords:  nullClassName factoryFailed badContext superset anim
//  LocalWords:  getLauncherNames newInstance PACKAGENAME BLOCKQUOTE
//  LocalWords:  PRE SUPERCLASS initializer ClassNotFoundException sb
//  LocalWords:  IOException IllegalAccessException loadFromResource
//  LocalWords:  yaml IllegalStateException initializers initilizer
//  LocalWords:  lclass tabspacing vname noLauncher href HREF enum
//  LocalWords:  createAPIMap hideString IllegalArgumentException JDK
//  LocalWords:  InstantiationException InvocationTargetException api
//  LocalWords:  ExceptionInInitializerError
//  LocalWords:  fname javadoc apis dnames launcherList yrunner msg
//  LocalWords:  launcherDataList launcherExists setObject ArrayList
//  LocalWords:  pushLevel popLevel incrList setKey StringBuilder
//  LocalWords:  errorMsg locationDescription toString HashSet ymode
//  LocalWords:  TreeSet bzdev noVar noName noFactory noContext
//  LocalWords:  noLauncherClassSpec noLauncherData
