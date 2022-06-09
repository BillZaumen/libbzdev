package org.bzdev.scripting;
import javax.script.ScriptException;
import java.util.HashSet;
import java.util.Properties;
import java.io.InputStream;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ExpressionParser.ESPObject;
import org.bzdev.util.ExpressionParser.ESPFunction;

//@exbundle org.bzdev.scripting.lpack.Scripting

/**
 * Create an adapter for interfaces representing listeners.
 * The adapter allows listener interfaces to be implemented in a scripting
 * language or in Java.  A listener interface for this purpose is simply
 * an interface all of whose methods have no return value and no declared
 * exceptions. Subclasses will extend this class and implement the
 * listener interface.  Each listener method implemented will simply
 * call {@link #callScriptMethod(String,Object...)} with a first argument
 * being the name of a method for an object implemented in a scripting
 * language. Typically this name will be the same as the method's name,
 * although this is not necessary.
 *<P>
 * A subclass should contain two constructors.  The constructor with
 * no arguments will set the scripting context and script object to
 * null. The methods will then return without performing any actions.
 * The constructor with two arguments is used when the implementation
 * for the adapter is provided by a scripting language.
 * <P>
 * Example:
 * <blockquote><code><pre>
 *    public interface FooListener {
 *        void method1(Object src, int status);
 *        void method2(Object src, String msg);
 *    }
 *
 *    public class FooScriptAdapter extends ScriptListenerAdapter
 *                                  implements FooListener
 *    {
 *        public FooScriptAdapter() {
 *            super();
 *        }
 *
 *        public FooScriptAdapter(Object scriptObject)
 *        {
 *            super(scriptObject);
 *        }

 *        public FooScriptAdapter(ScriptingContext context,
 *                                Object scriptObject)
 *        {
 *            super(context, scriptObject);
 *        }
 *
 *        void method1(Object src, int status) {
 *            callScriptMethod("method1", src, status);
 *        }
 *
 *        void method2(Object src, String msg) {
 *            callScriptMethod("method2", src, msg);
 *        }
 *    }
 * </pre></code></blockquote>
 * In a script one can create the adapter with code such as
 * <blockquote><code><pre>
 *    var adapter = new FooScriptAdapter(scripting, {
 *          method1(src, status) {
 *             ...
 *          }
 *          method2(src, msg) {
 *             ...
 *          }
 *        });
 * </pre></code></blockquote>
 */
public abstract class ScriptListenerAdapter {

    static String errorMsg(String key, Object... args) {
	return Scripting.errorMsg(key, args);
    }

    ScriptingContext context;
    Object scriptObject;

    /**
     * Exception thrown when a scripting language throws an exception.
     * This class is used to turn a ScriptException or
     * NoSuchMethodException into a RuntimeException, with the class
     * name used as an indication of what failed.
     */
    public static class ScriptMethodException extends RuntimeException {
	ScriptMethodException(Throwable cause) {
	    super(cause);
	}
    }

    // To make sure a script engine converts the argument to a Java
    // string.
    static class StringSet extends HashSet<String> {
	StringSet() {super();}
	public boolean add(String string) {
	    return super.add(string);
	}
    }


    private StringSet methods = null;
    static final String listPropertyScript =
	"ListPropertyScript.xml";
    static Properties listPropScriptProperties = null;

    /**
     * Constructor.
     * This creates an adapter with no scripting support.
     */
    protected ScriptListenerAdapter() {
	this(null, null);
    }

    /**
     * Constructor with only a script object.
     * This creates an adapter for use with the ESP scripting language
     * when an instance of {@link ScriptingContext} that can provide a
     * script engine is not available (an example of such a case is when
     * the yrunner command is used, in which case ESP will be started
     * without a scripting context).
     * <P>
     * Note: This is equivalent to using the constructor
     * {@link #ScriptListenerAdapter(ScriptingContext,Object)} with
     * a null first argument.
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    protected ScriptListenerAdapter(ESPObject scriptObject) {
	this(null, scriptObject);
    }


    /**
     * Constructor given a scripting context and script object.
     * This constructor implements the adapter using a scripting language
     * provided its arguments are not null. If a method is added to the
     * script object after this constructor is called, that method will
     * be ignored, so all of the methods the adapter implements must be
     * defined by the script object when this constructor is called
     * <P>
     * If ESP is the scripting language, the context may be null provided
     * that scriptObject is an ESP object.  This special case is provided
     * for use with {@link org.bzdev.obnaming.ObjectNamerLauncher} and the
     * yrunner program.
     * @param context the scripting context for this adapter; null if
     *        there is none or (optionally) if ESP is the scripting
     *        language
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     * @exception IllegalArgumentException the script object was ill formed
     */
    protected ScriptListenerAdapter(ScriptingContext context,
				     Object scriptObject)
	throws IllegalArgumentException
    {
	if (context != null && !context.hasScriptEngine()) {
	    // Internally, we treat a null context as an
	    // indication that there is no script engine available.
	    context = null;
	}
	this.context = context;
	if (context != null && scriptObject != null
	    && scriptObject instanceof String) {
	    try {
		scriptObject = context.evalScript((String)scriptObject);
	    } catch(ScriptException e) {
		String msg = errorMsg("illformedScriptObject");
		throw new IllegalArgumentException(msg, e);
	    }
	}
	this.scriptObject = scriptObject;
	if (context != null && scriptObject != null) {
	    methods = new StringSet();
	    if (listPropScriptProperties == null) {
		listPropScriptProperties = new Properties();
		try {
		    java.security.AccessController.doPrivileged
			(new java.security.PrivilegedExceptionAction<Void>() {
			    public Void run() throws java.io.IOException {
				InputStream is =
				    ScriptingContext.class.getResourceAsStream
				    (listPropertyScript);
				listPropScriptProperties.loadFromXML(is);
				return null;
			    }
			});
		} catch (java.security.PrivilegedActionException e) {
		    Throwable cause = e.getCause();
		    String msg = errorMsg("noResource", listPropertyScript);
		    throw new Error(msg, cause);
		}
	    }
	    try {
		context.invokePrivateFunction
		    (listPropScriptProperties,
		     // ScriptingContext.PFMode.SANDBOXED,
		     "recordProperties",
		     methods, scriptObject);
	    } catch(ScriptException ee) {
		String msg = errorMsg("illformedScriptObject");
		throw new IllegalArgumentException(msg, ee);
	    }
	} else if (scriptObject instanceof ESPObject) {
	    ESPObject espo = (ESPObject) scriptObject;
	    methods = new StringSet();
	    for (String m: espo.keySet()) {
		methods.add(m);
	    }
	}
    }

    /**
     * Call a method defined in a scripting language.
     * This method will be used to implement the listener interface
     * declared for a subclass of this class.
     * @param name the name of a method
     * @param args the arguments for the method
     * @exception ScriptMethodException an error occurred (as a runtime
     *            exception, this exception does not have to be caught).
     * @exception UnsupportedOperationException scripting is not supported
     *            but the constructor set a non-null value for the
     *            scripting context and the script object
     * @exception IllegalArgumentException the scriptObject was null or
     *            is not an object recognized by the scripting language
     */
    protected void callScriptMethod(String name, Object... args) {
	if (context == null) {
	    if (scriptObject == null) {
		return;
	    } else if (scriptObject instanceof ESPObject) {
		ESPObject espo = (ESPObject)scriptObject;
		if (espo.containsKey(name)) {
		    Object m = espo.get(name);
		    if (m instanceof ESPFunction) {
			ESPFunction f = (ESPFunction) m;
			f.invoke(args);
		    } else {
			return;
		    }
		} else {
		    return;
		}
	    }
	} else {
	    if (!methods.contains(name)) {
		// test for undefined methods to avoid
		// using the scripting language in cases
		// where it is expected to fail.
		return;
	    }
	    try {
		context.callScriptMethod(scriptObject, name, args);
	    } catch (ScriptException e) {
		throw new ScriptMethodException(e);
	    } catch(NoSuchMethodException e) {}
	    
	}
    }
}

//  LocalWords:  callScriptMethod blockquote pre FooListener src msg
//  LocalWords:  FooScriptAdapter ScriptListenerAdapter scriptObject
//  LocalWords:  ScriptingContext args
//  LocalWords:  ScriptException NoSuchMethodException runtime
//  LocalWords:  RuntimeException ScriptMethodException
