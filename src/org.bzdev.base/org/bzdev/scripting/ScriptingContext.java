package org.bzdev.scripting;

import javax.script.*;
import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Set;
import java.util.Collections;
import java.io.Reader;
import java.io.Writer;
import java.security.*;

import org.bzdev.lang.Callable;
import org.bzdev.lang.ExceptionedCallable;
import org.bzdev.lang.CallableReturns;
import org.bzdev.lang.ExceptionedCallableReturns;

//@exbundle org.bzdev.scripting.lpack.Scripting

/**
 * Class for running scripts.
 * A scripting context may also have a "parent" scripting context.
 * This allows classes such as {@link org.bzdev.devqsim.Simulation}
 * to be subclasses of {@link ScriptingContext} while an already
 * configured scripting context's script engine and bindings.
 * <P>
 * There are several subclasses of {@link ScriptingContext} in this
 * package. In particular, the class {@link ExtendedScriptingContext},
 * provides methods for importing Java classes into a scripting
 * environment (these methods were added by the author out of
 * frustration with the Rhino to Nashorn transition, where the default
 * mechanism for importing java classes suddenly changed). The class
 * {@link DefaultScriptingContext} will look up a scripting language by
 * name, with the name provided in a constructor.  The default is to
 * use the
 * <A HREF="{@docRoot}/org.bzdev.base/org/bzdev/util/doc-files/esp.html">ESP</A>
 * scripting language (added by the author after the "powers that be"
 * decided to drop support for Nashorn and finding that GraalVM, the suggested
 * alternative, did not provide adequate support for multithreading).
 * @see DefaultScriptingContext
 * @see ExtendedScriptingContext
 */
public class ScriptingContext {

    // Calling this method causes the class to be loaded. We
    // need this to paper over a bug in Java-11.0.1 where the
    // a BootstrapMethodError (bootstrap method initialization exception)
    // is thrown and caught by the JVM, not the application, causing a
    // crash when a ScriptingSecurityManger is installed
    static void register() {
    }

    // Determine if no security manager was running when this
    // ScriptingContext was created.
    // private final boolean noInitialSM = (System.getSecurityManager() == null);

    static final boolean graalVMMode = AccessController.doPrivileged
	    (new PrivilegedAction<Boolean>() {
		    public Boolean run() {
			try {
			    String test =
				System.getProperty("java.vendor.version");
			    if (test != null && test.startsWith("GraalVM")) {
				return true;
			    } else {
				return false;
			    }
			} catch (Exception e) {
			    return false;
			}
		    }
		});

    /**
     * Test if GraalVM is running.
     * This method is provided because scripting languages don't work
     * the same with GraalVM as with OpenJDK:
     * <UL>
     *   <LI> Immediately after a script engine is created, one should
     *        get the script engine's bindings and call
     *        bindings.put("polyglot.js.allowAllAccess", true) - otherwise
     *        attempts to call a Java method or construct a Java object
     *        will fail.
     *   <LI> If the default security manager is installed, calling
     *        engine.get(ScriptEngine.FILENAME) will throw an exception
     *        unless protected by a doPrivileged block. Seen in
     *        GraalVM CE Java11-20.1.0.
     * </UL>
     * The test makes use of the system property java.vendor.version,
     * which in GraalVM CE Java11-20.1.0 contains a string starting  with
     * GraalVM. OpenJDK does not define this system property.
     * @return true if GraalVM appears to be running; false otherwise
     */
    public static boolean usingGraalVM() {
	return graalVMMode;
    }

    static String errorMsg(String key, Object... args) {
	return Scripting.errorMsg(key, args);
    }

    private static ThreadLocal<Stack<Boolean>>
	sandboxStack = new ThreadLocal<Stack<Boolean>>()
    {
	protected Stack<Boolean> initialValue() {
	    return new Stack<Boolean>();
	}
    };

    private static InheritableThreadLocal<Boolean>
	inSandboxFlag = new InheritableThreadLocal<Boolean>()
    {
	protected Boolean initialValue() {return Boolean.FALSE;}
    };

    private static InheritableThreadLocal<Bindings>
	threadBindings = new InheritableThreadLocal<Bindings>()
    {
	protected Bindings initialValue() {return null;}
    };

    private static InheritableThreadLocal<ScriptEngine>
	threadEngine = new InheritableThreadLocal<ScriptEngine>()
    {
	protected ScriptEngine initialValue() {return null;}
    };

    static class BindingInfo {
	Bindings savedThreadBindings;
	Bindings savedCurrentBindings;
	ScriptEngine savedThreadEngine;
    }

    private BindingInfo saveBindings() {
	BindingInfo bi = new BindingInfo();
	bi.savedThreadBindings = threadBindings.get();
	bi.savedCurrentBindings =
	    engine.getBindings(ScriptContext.ENGINE_SCOPE);
	bi.savedThreadEngine = threadEngine.get();
	return bi;
    }

    private void setBindings(Bindings bindings) {
	threadBindings.set(defaultBindings);
	threadEngine.set(engine);
	engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }


    private void restoreBindings(BindingInfo bi)
    {
	threadBindings.set(bi.savedThreadBindings);
	threadEngine.set(bi.savedThreadEngine);
	engine.setBindings(bi.savedCurrentBindings, ScriptContext.ENGINE_SCOPE);
    }

    /**
     * Class to allow two sets of bindings associated with a ScriptingContext
     * to be swapped.
     * The method {@link ScriptingContext#createBindingSwapper(Bindings)}
     * is used to create instances of this class, and that method is
     * protected. Subclasses of ScriptingContext can create instances
     * of BindingSwapper, provided the subclasses have the necessary
     * permissions. As a result, the ability to swap bindings can be
     * restricted to specific classes.
     * <P>
     * Most applications should not need to create binding swappers. The
     * class was created to support classes in the package
     * org.bzdev.devqsim. The {@link ScriptingContext} class does not
     * provide a public method that returns a script engine, nor a
     * public method to set its bindings. Some methods such as
     * evalScript, however, allow alternate bindings to be
     * used, and will keep the old bindings on a stack, restoring
     * those with the method exits.  A binding swapper can be used to
     * swap those temporary bindings with the script engine's default
     * bindings. For example, the devqsim package has a class named
     * {@link org.bzdev.devqsim.TaskThread} that uses a binding swapper
     * to temporarily change the current bindings to the default bindings
     * when a thread is being paused because the simulation may be
     * running in a script and needs those original bindings for
     * instrumentation-related tasks that occur as threads are suspended
     * and restored.
     */

    public static class BindingSwapper {
	ScriptEngine engine;
	Bindings other;
	Bindings current;
	
	// defined so we will not get a public constructor.
	BindingSwapper() {}

	/**
	 * Switch from one binding to the other.
	 */
	public void swap() {
	    // always want to set something
	    if (other == null) {
		engine.setBindings(current, ScriptContext.ENGINE_SCOPE);
	    } else {
		engine.setBindings(other, ScriptContext.ENGINE_SCOPE);
		Bindings tmp = other;
		other = current;
		current = tmp;
	    }
	}
    }

    /**
     * Create a binding swapper.
     * The use of this class is intended for the case where a script
     * will be run in a separate thread and then suspend itself.  When
     * a scripting context runs a script, it puts its default bindings
     * in an inherited thread-local variable. If new thread is created
     * while that script is running, those bindings will be inherited.
     * Then the bindings that a script will run can be passed to this
     * method, and the binding swapper can be used to toggle between
     * the two sets of bindings if the new thread is suspended or
     * restored.
     * <P>
     * The caller must ensure that the default bindings are in effect
     * when threads making use of a binding swapper create additional
     * threads that use scripting contexts to run scripts.
     * <P>
     * Most applications should not need to create binding swappers. The
     * class was created to support classes in the package
     * org.bzdev.devqsim. The {@link ScriptingContext} class does not
     * provide a public method that returns a script engine, nor a
     * public method to set its bindings. Some methods such as
     * evalScript, however, allow alternate bindings to be
     * used, and will keep the old bindings on a stack, restoring
     * those with the method exits.  A binding swapper can be used to
     * swap those temporary bindings with the script engine's default
     * bindings. For example, the devqsim package has a class named
     * {@link org.bzdev.devqsim.TaskThread} that uses a binding swapper
     * to temporarily change the current bindings to the default bindings
     * when a thread is being paused because the simulation may be
     * running in a script and needs those original bindings for
     * instrumentation-related tasks that occur as threads are suspended
     * and restored.
     * @param bindings the binding to initially swap with an existing binding
     *        when {@link BindingSwapper#swap()} is called
     * @return the new binding swapper
     */
    protected BindingSwapper createBindingSwapper(Bindings bindings) 
    {
	if (engine == null) engine = getScriptEngine();
	if (engine == null) {
	    return null;
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    return null;
	}
	// do a security check.
	/*
	final SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission
		(new ScriptingContextPermission
		 (getClass().getName(), "swapbindings"));
	}
	*/
	BindingSwapper swapper = new BindingSwapper();
	swapper.engine = engine;
	swapper.other = threadBindings.get();
	swapper.current =  (bindings == null)? defaultBindings: bindings;
	return swapper;
    }

    /*
    private void checkConstructorPermission(boolean trusted)
	throws SecurityException
    {
	final SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    synchronized(ScriptingContext.class) {
		if (trusted) {
		    sm.checkPermission
			(new ScriptingContextPermission(getClass().getName(),
							"trusted"));
		} else {
		    // When trusted == false, we can safely create the
		    // scripting context as long as it has the
		    // appropriate permissions, regardless of its caller.
		    final Permission perm =
			new ScriptingContextPermission(getClass().getName());
		    AccessController.doPrivileged
			(new PrivilegedAction<Void>() {
				public Void run() {
				    sm.checkPermission(perm);
				    return null;
				}
			    });
		}
	    }
	}
    }
    */
    /*
    private void checkPrivileged() {
	if (parent != null) {
	    parent.checkPrivileged();
	    return;
	}
	final SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    final Permission perm =
		new ScriptingContextPermission(getClass().getName(),
					       "privileged");
	    if (privilegedContext == null) {
		// we created it outside of a security manager
		return;
	    } else {
		AccessController.doPrivileged
		    (new PrivilegedAction<Void>() {
			    public Void run() {
				sm.checkPermission(perm);
				return null;
			    }
			}, privilegedContext);
	    }
	}
    }
    */

    private boolean notTrusted = false;

    /*
     * Determine if this scripting context is trusted or not.
     * @return true if it is trusted; false if it is not trusted
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    public boolean isTrusted() {
	return !notTrusted;
    }
    */
    // Access-control context for scripts. This is used for
    // trusted scripting contexts. A value of null implies all permissions.
    // It is set to a non-null value if the scripting context is created
    // when a security manager is already in place and allows these values
    // to be set by a scripting-context constructor.

    AccessControlContext context = null; // for sandbox/normal mode
    AccessControlContext privilegedContext = null; // for privileged mode

    // Access-control context that provides no permissions whatsoever.
    private static final PermissionCollection noPermissions = new Permissions();
    private static final AccessControlContext noPermContext =
	new AccessControlContext (new ProtectionDomain[] {
		new ProtectionDomain(null, noPermissions)
	    });

    private static final String DEFAULT_LANG_NAME = "ECMAScript";

    /**
     * Create a script engine.
     * This method is provided due to a security issue discovered
     * with Javascript in which calling a ScriptEngineManager's method
     * getEngineByName would result in elevated privileges if called
     * after a security manager is installed. A test indicated that
     * one can suppress this behavior by using AccessController.doPrivilege
     * with a context that has no permissions.  A bug report was filed (this
     * was some time ago with a much earlier version of Java).
     * <P>
     * This method is now used to allow some initialization when
     * GraalVM is used so that specific properties can be set (otherwise
     * GraalVM's ECMAScript implementation will be severely restricted -
     * it will not be able to call Java methods or constructors.
     * @param manager a ScriptEngineManager used to obtain the engine
     * @param languageName the name of the scripting language;
     * @return the script engine created; null if none matches the specified
     *         scripting-language name
     */
    public static ScriptEngine createScriptEngine
	(final ScriptEngineManager manager, final String languageName)
    {
	if (languageName == null) {
	    throw new
		IllegalArgumentException(errorMsg("nullLang"));
	}
	ScriptEngine engine = AccessController.doPrivileged
	    (new PrivilegedAction<ScriptEngine>() {
		    public ScriptEngine run() {
			ScriptEngine engine =
			    manager.getEngineByName(languageName);
			if (graalVMMode) {
			    Bindings bindings =
				engine.getBindings(ScriptContext.ENGINE_SCOPE);
			    bindings.put("polyglot.js.allowAllAccess", true);
			}
			return engine;
		}
	    });
	return engine;
    }

    private static AccessControlContext
	getPrivilegedContextUnconditionally(final Class<?>clazz)
    {
	return AccessController.doPrivileged
	    (new PrivilegedAction<AccessControlContext>() {
		public AccessControlContext run()
		{
		    ProtectionDomain domain = clazz.getProtectionDomain();
		    if (domain == null) return AccessController.getContext();
		    ProtectionDomain[] domains = new ProtectionDomain[] {
			domain
		    };
		    return new AccessControlContext(domains);
		}
	    });
    }


    /**
     * Constructor.
     */
    public ScriptingContext() {
	/*
	checkConstructorPermission(false);
	context = noPermContext;
	privilegedContext = (System.getSecurityManager() == null)? null:
	    AccessController.getContext();
	*/
    }


    /**
     * Constructor specifying the security mode.
     * @param trusted true if the script context is trusted; false otherwise
     * @deprecated trusted scripting contexts are not applicable when
     *             the security manager has been eliminated.
     */
    @Deprecated
    public ScriptingContext(boolean trusted)  {
	/*
	if (trusted && inSandbox()) {
	    throw new SecurityException(errorMsg("trustedInSandbox"));
	}
	checkConstructorPermission(trusted);
	notTrusted = !trusted;
	privilegedContext = (System.getSecurityManager() == null)? null:
	    AccessController.getContext();
	if (trusted) {
	    context =  null;
	} else {
	    context = noPermContext;
	}
	*/
    }

    /**
     * Constructor using a parent.
     * Unless methods are overridden, the parent scripting context
     * provides the scripting language, script engine, and bindings.
     * @param parent the parent scripting context; null if there is none.
     */
    public ScriptingContext(ScriptingContext parent) {
	// if (parent == null) checkConstructorPermission(false);
	this.parent = parent;
	/*
	context = noPermContext;
	if (parent == null) {
	    privilegedContext = (System.getSecurityManager() == null)? null:
		AccessController.getContext();
	} else {
	    privilegedContext = parent.privilegedContext;
	}
	*/
    }


    /*
     * Constructor using a parent and specifying a security mode.
     * Unless methods are overridden, the parent scripting context
     * provides the scripting language, script engine, and bindings.
     * @param parent the parent scripting context; null if there is none.
     * @param trusted true if the script context is trusted; false otherwise
     * @exception SecurityException this subclass of ScriptingContext cannot
     *            be created after a security manager was installed or an
     *            attempt was made to create a trusted subclass of
     *            ScriptingContext from inside a sandbox
     * @deprecated trusted scripting contexts add nothing when the
     *             security manager has been eliminated
    @Deprecated
    public ScriptingContext(ScriptingContext parent, boolean trusted) {
	if (trusted && inSandbox()) {
	    throw new SecurityException(errorMsg("trustedInSandbox"));
	}
	if (parent == null) checkConstructorPermission(trusted);
	notTrusted = !trusted;
	this.parent = parent;
	if (parent == null) {
	    privilegedContext = (System.getSecurityManager() == null)? null:
		AccessController.getContext();
	} else {
	    privilegedContext = parent.privilegedContext;
	}
	if (trusted) {
	    context =  null;
	} else {
	    context = noPermContext;
	}
    }
    */

    /*
     * Determine if the current thread is in a ScriptingContext sandbox.
     * @return true if in a sandbox; false otherwise
     * @deprecated there is no sandbox after the security manager has
     *             been removed from Java
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    public static final boolean inSandbox() {
	    return inSandboxFlag.get();
    }
     */


    // cache values - will be set before used.
    private ScriptEngine engine = null;
    private Bindings defaultBindings = null;
    private ScriptingContext parent = null;

    /**
     * Evaluate a script provided via a Reader.
     * @param reader a Reader providing the script
     * @return the value produced by the script
     * @exception ScriptException an error occurred in the script
     * @exception NullPointerException the script or bindings were null
     * @exception UnsupportedOperationException the script engine is null
     */
    public final  Object evalScript(final Reader reader)
	throws ScriptException, UnsupportedOperationException
    {
	if (engine == null) engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new
		ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    setBindings(defaultBindings);
	    return engine.eval(reader, defaultBindings);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(reader,
							   defaultBindings);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(reader,
							   defaultBindings);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run() throws ScriptException {
				return engine.eval(reader, defaultBindings);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		throw (ScriptException) e.getCause();
	    }
	    */
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }

    /**
     * Evaluate a script provided via a Reader given the Reader's file name.
     * The file name that the script engine uses is set temporarily
     * and then restored to its previous value after the script is read.
     * @param fileName the file name for the reader
     * @param reader a Reader providing the script
     * @return the value produced by the script
     * @exception ScriptException an error occurred in the script
     * @exception NullPointerException the script or bindings were null
     * @exception UnsupportedOperationException the script engine is null
     */
    public final Object evalScript(String fileName, final Reader reader)
	throws ScriptException, UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new
		ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	Object oldFileName = engine.get(ScriptEngine.FILENAME);
	try {
	    engine.put(ScriptEngine.FILENAME, fileName);
	    // onScriptStarting();
	    setBindings(defaultBindings);
	    return engine.eval(reader, defaultBindings);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(reader,
							   defaultBindings);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(reader,
							   defaultBindings);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run() throws ScriptException {
				return engine.eval(reader, defaultBindings);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		throw (ScriptException) e.getCause();
	    }
	    */
	} finally {
	    restoreBindings(saved);
	    //onScriptEnding();
	    engine.put(ScriptEngine.FILENAME, oldFileName);
	}
    }

    /**
     * Evaluate a script provided in a string.
     * @param script the script to evaluate
     * @return the value produced by the script
     * @exception ScriptException an error occurred in the script
     * @exception NullPointerException the script or bindings were null
     * @exception UnsupportedOperationException the script engine is null
     */
    public final Object evalScript(final String script)
	throws ScriptException, UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new
		ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    setBindings(defaultBindings);
	    return engine.eval(script, defaultBindings);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(script,
							   defaultBindings);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(script,
							   defaultBindings);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run() throws ScriptException {
				return engine.eval(script, defaultBindings);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		throw (ScriptException) e.getCause();
	    }
	    */
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }

    /**
     * Evaluate a script provided in a string using specified bindings.
     * @param script the script to evaluate
     * @param bindings the script's bindings; null for the default bindings
     * @return the value produced by the script
     * @exception ScriptException an error occurred in the script
     * @exception IllegalArgumentException an argument was null or the
     *            bindings were not created by this scripting context
     */
    public final Object evalScript(final String script, Bindings bindings)
	throws ScriptException, IllegalArgumentException
    {
	if (script == null) {
	    throw new
		IllegalArgumentException(errorMsg("nullScript"));
	}
	if (bindings == null) {
	    bindings = getDefaultBindings();
	}
	final Bindings ourBindings = bindings;
	if (engine == null) {
	    // we should not have been able to create the bindings
	    // if the engine did not exist.
	    throw new IllegalArgumentException
		(errorMsg("bindingsFromOther"));
	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    setBindings(ourBindings);
	    return engine.eval(script, ourBindings);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(script, ourBindings);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run() throws ScriptException {
					return engine.eval(script, ourBindings);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run() throws ScriptException {
				return engine.eval(script, ourBindings);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		throw (ScriptException) e.getCause();
	    }
	    */
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
     }


    /*
     * Evaluate a script provided in a string using privileged mode.
     * @param script the script to evaluate
     * @return the value produced by the script
     * @exception ScriptException an error occurred in the script
     * @exception NullPointerException the script or bindings were null
     * @exception UnsupportedOperationException the script engine is null
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    protected final Object evalScriptPrivileged(final String script)
	throws ScriptException, UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new
		ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	setBindings(defaultBindings);
	try {
	    return engine.eval(script, defaultBindings);
	    -- rest was commented out
	    try {
		if (privilegedContext == null) {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
				public Object run() throws ScriptException {
				    try {
					onExecutionStarting();
					return engine.eval(script,
							   defaultBindings);
				    } finally {
					onExecutionEnding();
				    }
				}
			    });
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
				public Object run() throws ScriptException {
				    try {
					onExecutionStarting();
					return engine.eval(script,
							   defaultBindings);
				    } finally {
					onExecutionEnding();
				    }
				}
			    }, privilegedContext);
		}
	    } catch (PrivilegedActionException e) {
		throw (ScriptException) e.getCause();
	    }
	} finally {
	    restoreBindings(saved);
	}
    }
    */


    /**
     * Evaluate a script function.
     * @param functionName the script function to call
     * @param args the arguments
     * @return the value computed by the function
     * @exception ScriptException the function was not defined or matching
     *            arguments could not be found
     * @exception UnsupportedOperationException there was no script engine
     *            to use
     * @exception NoSuchMethodException the function does not exist or the
     *            arguments do not match
     */
    public final Object callScriptFunction(final String functionName,
						  final Object... args)
	throws ScriptException, UnsupportedOperationException,
	       NoSuchMethodException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new
		ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    setBindings(defaultBindings);
	    engine.setBindings(defaultBindings, ScriptContext.ENGINE_SCOPE);
	    final Invocable inv = (Invocable)engine;
	    try {
	    return inv.invokeFunction(functionName, args);
	    } catch (Exception e) {
		if (e instanceof ScriptException) {
		    throw (ScriptException) e;
		} else if (e instanceof NoSuchMethodException) {
		    throw (NoSuchMethodException) e;
		} else {
		    String msg = errorMsg("unrecognizedException");
		    throw new Error(msg, e);
		}
	    }
	    /*
	    try {
		if (context == null) {
		    if(privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeFunction(functionName,
								  args);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeFunction(functionName,
								  args);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run()
				throws ScriptException, NoSuchMethodException {
				return inv.invokeFunction(functionName, args);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		Exception ee = (Exception) e.getCause();
		if (ee instanceof ScriptException) {
		    throw (ScriptException) ee;
		} else if (ee instanceof NoSuchMethodException) {
		    throw (NoSuchMethodException) ee;
		} else {
		    String msg = errorMsg("unrecognizedException");
		    throw new Error(msg, ee);
		}
	    }
	    */
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }

    /**
     * Evaluate a script function given a set of bindings.
     * @param bindings the bindings to use when the script is evaluated;
     *        null for the default bindings
     * @param functionName the script function to call
     * @param args the arguments
     * @return the value computed by the function
     * @exception ScriptException the function was not defined or matching
     *            arguments could not be found
     * @exception IllegalArgumentException an argument was null or the
     *            bindings were not created by this scripting context
     * @exception NoSuchMethodException the function does not exist or the
     *            arguments do not match
     */
    public final Object callScriptFunction(Bindings bindings,
					   final String functionName,
					   final Object... args)
	throws ScriptException, IllegalArgumentException,
	       NoSuchMethodException
    {

	if (functionName == null) {
	    throw new
		IllegalArgumentException(errorMsg("nullFunctionName"));
	}
	if (bindings == null) {
	    bindings = getDefaultBindings();
	    if (bindings == null)
	    throw new
		IllegalArgumentException(errorMsg("noDefaultBindings"));
	}
	final Bindings ourBindings = bindings;
	if (engine == null) {
	    // we should not have been able to create the bindings
	    // if the engine did not exist.
	    throw new IllegalArgumentException(errorMsg("bindingsFromOther"));
	}

	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    final Invocable inv = (Invocable)engine;
	    setBindings(ourBindings);
	    return inv.invokeFunction(functionName, args);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeFunction(functionName,
								  args);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeFunction(functionName,
								  args);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run()
				throws ScriptException, NoSuchMethodException {
				return inv.invokeFunction(functionName, args);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		Exception ee = (Exception) e.getCause();
		if (ee instanceof ScriptException) {
		    throw (ScriptException) ee;
		} else if (ee instanceof NoSuchMethodException) {
		    throw (NoSuchMethodException) ee;
		} else {
		    String msg = errorMsg("unrecognizedException");
		    throw new Error(msg, ee);
		}
	    }
	    */
	} catch (Exception e) {
	    if (e instanceof ScriptException) {
		    throw (ScriptException) e;
	    } else if (e instanceof NoSuchMethodException) {
		throw (NoSuchMethodException) e;
	    } else {
		String msg = errorMsg("unrecognizedException");
		throw new Error(msg, e);
	    }
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }


    /**
     * Evaluate a script object's method.
     * @param scriptObject the script object whose method is to be invoked
     * @param methodName the name of the method to invoke
     * @param args the arguments
     * @return the value computed by the method
     * @exception ScriptException an exception occurred in a script object's
     *            method
     * @exception NoSuchMethodException the method does not exist or the
     *            arguments do not match
     * @exception UnsupportedOperationException scripting is not supported
     * @exception IllegalArgumentException the scriptObject was null or
     *            is not an object recognized by the scripting language
     */
    public final Object callScriptMethod(final Object scriptObject,
					 final String methodName,
					 final Object... args)
	throws ScriptException, UnsupportedOperationException,
	       NoSuchMethodException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) {
	    throw new ScriptException(errorMsg("noBindings"));
	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    engine.setBindings(defaultBindings,
				     ScriptContext.ENGINE_SCOPE);
	    setBindings(defaultBindings);
	    final Invocable inv = (Invocable)engine;
	    return inv.invokeMethod(scriptObject, methodName, args);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException {
					return inv.invokeMethod(scriptObject,
								methodName,
								args);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException {
					return inv.invokeMethod(scriptObject,
								methodName,
								args);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run()
				throws ScriptException, NoSuchMethodException {
				return inv.invokeMethod(scriptObject,
							methodName, args);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		Exception ee = (Exception) e.getCause();
		if (ee instanceof ScriptException) {
		    throw (ScriptException) ee;
		} else if (ee instanceof NoSuchMethodException) {
		    throw (NoSuchMethodException) ee;
		} else {
		    String msg = errorMsg("unrecognizedException");
		    throw new Error(msg, ee);
		}
	    }
	    */
	} catch (Exception e) {
	    if (e instanceof ScriptException) {
		throw (ScriptException) e;
	    } else if (e instanceof NoSuchMethodException) {
		throw (NoSuchMethodException) e;
	    } else {
		String msg = errorMsg("unrecognizedException");
		throw new Error(msg, e);
		}
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }

    /**
     * Evaluate a script object's method using specified bindings.
     * @param bindings the bindings
     * @param scriptObject the script object whose method is to be invoked
     * @param methodName the name of the method to invoke
     * @param args the arguments
     * @return the value computed by the method
     * @exception ScriptException an exception occurred in a script object's
     *            method
     * @exception UnsupportedOperationException scripting is not supported
     * @exception NoSuchMethodException the method does not exist or the
     *            arguments do not match
     */
    public final Object callScriptMethod(Bindings bindings,
					 final Object scriptObject,
					 final String methodName,
					 final Object... args)
	throws ScriptException, UnsupportedOperationException,
	       NoSuchMethodException
    {
	if (scriptObject == null) {
	    throw new
		IllegalArgumentException(errorMsg("nullScriptObject"));
	}
	if (methodName == null) {
	    throw new
		IllegalArgumentException(errorMsg("nullMethodName"));
	}
	if (bindings == null) {
	    bindings = getDefaultBindings();
	    if (bindings == null)
		throw new
		    IllegalArgumentException(errorMsg("noDefaultBindings"));
	}
	if (engine == null) {
	    // we should not have been able to create the bindings
	    // if the engine did not exist.
	    throw new IllegalArgumentException
		(errorMsg("bindingsFromOther"));

	}
	BindingInfo saved = saveBindings();
	try {
	    // onScriptStarting();
	    setBindings(bindings);
	    final Invocable inv = (Invocable)engine;
	    return inv.invokeMethod(scriptObject, methodName, args);
	    /*
	    try {
		if (context == null) {
		    if (privilegedContext == null) {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeMethod(scriptObject,
								methodName,
								args);
				    }
				});
		    } else {
			return AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				    public Object run()
					throws ScriptException,
					       NoSuchMethodException
				    {
					return inv.invokeMethod(scriptObject,
								methodName,
								args);
				    }
				}, privilegedContext);
		    }
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<Object>() {
			    public Object run()
				throws ScriptException, NoSuchMethodException {
				return inv.invokeMethod(scriptObject,
							methodName, args);
			    }
			}, context);
		}
	    } catch(PrivilegedActionException e) {
		Exception ee = (Exception) e.getCause();
		if (ee instanceof ScriptException) {
		    throw (ScriptException) ee;
		} else if (ee instanceof NoSuchMethodException) {
		    throw (NoSuchMethodException) ee;
		} else {
		    String msg = errorMsg("unrecognizedException");
		    throw new Error(msg, ee);
		}
	    }
	    */
	} catch (Exception e) {
	    if (e instanceof ScriptException) {
		throw (ScriptException) e;
	    } else if (e instanceof NoSuchMethodException) {
		throw (NoSuchMethodException) e;
	    } else {
		String msg = errorMsg("unrecognizedException");
		throw new Error(msg, e);
	    }
	} finally {
	    restoreBindings(saved);
	    // onScriptEnding();
	}
    }

    /**
     * Determine if a script object with a given name exists (i.e., is
     * contained the default bindings).
     * The look-up uses the default bindings that were provided by the
     * method getDefaultBindings() when it was called by a
     * constructor.  These bindings are the engine-scope bindings that
     * will be used when the script is evaluated using the methods
     * provided in this class.
     * @param name the name of the object
     * @return true if a binding for the name exists; false otherwise
     */
    public final boolean containsScriptObject(String name) {
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) return false;
	return defaultBindings.containsKey(name);
    }

    /**
     * Get a script object given a name.
     * The object is obtained using the default bindings that were
     * provided by the method getDefaultBindings() when it was called
     * by a constructor.  These bindings are the engine-scope bindings
     * that will be used when the script is evaluated using the methods
     * provided in this class.
     * @param name the name of the object
     * @return the object; null if there is none
     */
    public final Object getScriptObject(String name) {
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null) return null;
	return defaultBindings.get(name);
    }

    /**
     * Assign a name to a script or java object.
     * The object is stored in the default bindings that were
     * provided by the method getDefaultBindings() when it was called
     * by a constructor.  These bindings are the engine-scope bindings
     * that will be used when the script is evaluated using the methods
     * provided in this class.
     * @param name the name to assign to the object
     * @param object the object
     * @exception UnsupportedOperationException the script context does not
     *            provide any bindings
     */
    public final void putScriptObject(String name, Object object)
	throws UnsupportedOperationException
    {
	if (defaultBindings == null) defaultBindings = getDefaultBindings();
	if (defaultBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noBindingsExist"));
	defaultBindings.put(name, object);
    }


    /**
     * Get the script engine.
     * Subclasses providing a scripting environment must override this
     * method.  It will typically be called once, but could be called
     * multiple times, so the method must be idempotent in case it is
     * called multiple times. It will not be called by this class if
     * the constructor provided a parent.
     * @return the script engine for this scripting context; null if
     *         there is none
     */
    protected ScriptEngine doGetScriptEngine() {
	return null;
    }

    /**
     * Get the script engine.
     * When there is a parent scripting context, the method is called
     * on the parent; otherwise it returns the value provided by
     * {@link #doGetScriptEngine() doGetScriptEngine}, which returns null
     * unless overridden by a subclass.
     * @return the script engine for this scripting context; null if
     *         there is none
     */
    private ScriptEngine getScriptEngine() {
	if (engine == null) {
	    engine = ((parent == null)? doGetScriptEngine():
		      parent.getScriptEngine());
	}
	return engine;
    }

    /**
     * Determine if a script engine is available.
     * @return true if there is a script engine; false otherwise
     */
    public final  boolean hasScriptEngine() {
	if (engine == null) {
	    engine = getScriptEngine();
	}
	return (engine != null);
    }


    /**
     * Create a new set of script bindings.
     * @return a new set of script bindings; null if there is no script engine
     */
    public final Bindings createBindings() {
	if (engine == null) {
	    engine = getScriptEngine();
	}
	if (engine == null) {
	    return null;
	} else {
	    return engine.createBindings();
	}
    }


    /**
     * Get the default script bindings.
     * Subclasses providing a scripting environment must override this
     * method.  It will typically be called once, but could be called
     * multiple times, so the method must be idempotent. It will not
     * be called by this class if the constructor provided a parent.
     * @return the default bindings for this scripting context; null if
     *         there are none
     */
    protected Bindings doGetDefaultBindings() {
	return null;
    }

    /**
     *  Get the default script bindings.
     * The bindings returned are for ScriptContext.ENGINE_SCOPE.
     * Unless overridden, this method returns null.
     * Subclasses that support scripting must override this method.
     * @return the script bindings for this scripting context; null if
     *         there are none
     */
    Bindings getDefaultBindings() {
	return ((parent == null)? doGetDefaultBindings():
		parent.getDefaultBindings());
    }


    /**
     * Get the scripting language.
     * Subclasses providing a scripting environment must override this
     * method.  It will typically be called once, but could be called
     * multiple times, so the method must be idempotent. It will not
     * be called by this class if the constructor provided a parent.
     * @return the script engine for this scripting context; null if
     *         there is none
     */
    protected String doGetScriptLanguage() {
	return null;
    }

    private String scriptLanguage = null;

    /**
     * Get the script language name.
     * @return the name of the scripting language; null if there is none
     */
    public final String getScriptLanguage() {
	if (scriptLanguage == null) {
	    scriptLanguage = (parent == null)? doGetScriptLanguage():
		parent.getScriptLanguage();
	}
	return scriptLanguage;
    }

    /*
    private static ScriptingContextPermission writerPermission =
	new ScriptingContextPermission("java.io.Writer");
    */

    /**
     * Set the writer for script output.
     * @param writer the writer
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final void setWriter(Writer writer)
	throws UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	/*
	if (inSandbox()) {
	    throw new SecurityException(errorMsg("setWriter"));
	}
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(writerPermission);
	}
	*/
	engine.getContext().setWriter(writer);
    }

    /**
     * Get the writer for scripting-language output.
     * @return the writer
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final Writer getWriter() throws UnsupportedOperationException {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	return engine.getContext().getWriter();
    }


    /**
     * Set the writer for script error output.
     * @param writer the writer
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final void setErrorWriter(Writer writer)
	throws UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	/*
	if (inSandbox()) {
	    throw new SecurityException(errorMsg("setErrorWriter"));
	}
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(writerPermission);
	}
	*/
	engine.getContext().setErrorWriter(writer);
    }

    /**
     * Get the write for scripting-language error output.
     * @return the writer
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final Writer getErrorWriter()
	throws UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	return engine.getContext().getErrorWriter();
    }


    /*
    private static ScriptingContextPermission readerPermission =
	new ScriptingContextPermission("java.io.Reader");
    */

    /**
     * Set the reader for script input.
     * @param reader the reader
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final void setReader(Reader reader)
	throws UnsupportedOperationException
    {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	/*
	if (inSandbox()) {
	    throw new SecurityException(errorMsg("setReader"));
	}
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(readerPermission);
	}
	*/
	engine.getContext().setReader(reader);
    }

    /**
     * Get the reader for scripting-language input.
     * @return the reader
     * @exception UnsupportedOperationException scripting was not enabled
     */
    public final Reader getReader() throws UnsupportedOperationException {
	if (engine == null)  engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	return engine.getContext().getReader();
    }


    /*
     * Indicate that code should not be run in a scripting-context sandbox.
     * Calls to onExecutionStarting and onExecutionEnding, or to
     * onScriptStarting and onScriptEnding, must occur in pairs with
     * onExecutionStarting or onScriptStarting running first in any
     * pair. Pairs may be nested.
     * @exception SecurityException the current thread is not consistent
     *            with thread group a ScriptingSecurityManager associated
     *            with this object

   private void onExecutionStarting() throws SecurityException {
       sandboxStack.get().push(inSandboxFlag.get());
       inSandboxFlag.set(Boolean.FALSE);
   }
     */

    /*
     * Undo the effects of a previous call to onExecutionStarting.
     * Calls to onExecutionStarting and onExecutionEnding, or to
     * onScriptStarting and onScriptEnding, must occur in pairs with
     * onExecutionStarting or onScriptStarting running first in any
     * pair. Pairs may be nested.
     * @exception SecurityException the current thread is not consistent
     *            with thread group a ScriptingSecurityManager associated
     *            with this object

    private void onExecutionEnding() throws SecurityException {
	inSandboxFlag.set(sandboxStack.get().pop());
    }
     */

    /*
     * Execute code in privileged mode.
     * <P>
     * If the security manger is java.lang.SecurityManager, the
     * argument will be called using AccessController.doPrivileged
     * block with the access-controller context when the scripting
     * context was created, or null if there was no security manager
     * installed at that time.
     * <P>
     * If a subclass overrides this method, it must call
     * super.doScriptPrivileged with the same arguments. This may be
     * necessary in order to make the method accessible to other
     * classes in the subclass' package.  Ideally, the method would be
     * declared to be final, but this unfortunately precludes allowing
     * protected access in the subclass' package.
     * @param c a Callable providing the code to run
     * @exception SecurityException a security exception was thrown, typically
     *            by the Callable c
    protected final void doScriptPrivileged(final Callable c)
	throws SecurityException
    {
	c.call();
	--- rest had been commented out
       checkPrivileged();
	try {
	    onExecutionStarting();
	    if (privilegedContext == null) {
		AccessController.doPrivileged
		    (new PrivilegedAction<Void>() {
			    public Void run() {
				c.call();
				return null;
			    }
			});
	    } else {
		AccessController.doPrivileged
		    (new PrivilegedAction<Void>() {
			    public Void run() {
				c.call();
				return null;
			    }
			}, privilegedContext);
	    }
	} finally {
	    onExecutionEnding();
	}
    }
     */

    /*
     * Execute code in sandboxed mode.
     * @param c a Callable providing the code to run
     * @exception SecurityException a security exception was thrown,
     *            typically by the Callable c
     * @deprecated this method is not needed unless a security manager
     *             is used
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    protected final void doScriptSandboxed(final Callable c)
	throws SecurityException
    {
	c.call();
	-- rest was commented out
	try {
	    onScriptStarting();
	    AccessController.doPrivileged
		(new PrivilegedAction<Void>() {
		    public Void run() {
			c.call();
			return null;
		    }
		}, noPermContext);
	} finally {
	    onScriptEnding();
	}
    }
    */

    /**
     * Execute code, that can throw checked exceptions, in privileged mode.
     * <P>
     * If a subclass overrides this method, it must call
     * super.doScriptPrivleged with the same arguments. This may be
     * necessary in order to make the method accessible to other
     * classes in the subclass' package.  Ideally, the method would be
     * declared to be final, but this unfortunately precludes allowing
     * protected access in the subclass' package.
     * @param c an ExceptionedCallable providing the code to run
     * @exception Exception an exception was raised
     */
    protected final void doScriptPrivileged(final ExceptionedCallable c)
	throws Exception {
	c.call();
	/*
	checkPrivileged();
	try {
	    onExecutionStarting();
	    try {
		if (privilegedContext == null) {
		    AccessController.doPrivileged
			(new PrivilegedExceptionAction<Void>() {
				public Void run() throws Exception {
				    c.call();
				    return null;
				}
			    });
		} else {
		    AccessController.doPrivileged
			(new PrivilegedExceptionAction<Void>() {
				public Void run() throws Exception {
				    c.call();
				    return null;
				}
			    }, privilegedContext);
		}
	    } catch(PrivilegedActionException e) {
		throw (Exception) e.getCause();
	    }
	} finally {
	    onExecutionEnding();
	}
	*/
    }

    /*
     * Execute code, that can throw checked exceptions, inside a
     * scripting context's sandbox.
     * <P>
     * @param c an ExceptionedCallable providing the code to run
     * @exception SecurityException a security exception was thrown,
     *            typically  by the ExceptionedCallable c
     * @exception Exception an exception was raised
     * @deprecated this method is not needed unless a security manager
     *             is used
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    public final void doScriptSandboxed(final ExceptionedCallable c)
	throws SecurityException, Exception {
	c.call();
	-- rest was commented out but that was removed to comment out
          the whole method
	try {
	    onScriptStarting();
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			public Void run() throws Exception {
			    c.call();
			    return null;
			}
		    }, noPermContext);
	    } catch(PrivilegedActionException e) {
		throw (Exception) e.getCause();
	    }
	} finally {
	    onScriptEnding();
	}
    }
    */

    /*
     * Execute code, returning a value, in privileged mode.
     * @param c a CallableReturns providing the code to run
     * @return the value produced by running c
     * @exception SecurityException a security exception was thrown,
     *            typically by the CallableReturns argument
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    protected final <T> T doScriptPrivilegedReturns(final CallableReturns<T> c)
	throws SecurityException
    {
	return c.call();
	-- rest had been commented out
	checkPrivileged();
	try {
	    onExecutionStarting();
	    if (privilegedContext == null) {
		return AccessController.doPrivileged
		    (new PrivilegedAction<T>() {
			    public T run() {
				return c.call();
			    }
			});
	    } else {
		return AccessController.doPrivileged
		    (new PrivilegedAction<T>() {
			    public T run() {
				return c.call();
			    }
			}, privilegedContext);
	    }
	} finally {
	    onExecutionEnding();
	}
    }
    */

    /*
     * Execute code, returning a value, inside a scripting context's sandbox.
     * <P>
     * @param c a CallableReturns providing the code to run
     * @return the value produced by running c
     * @exception SecurityException a security exception was thrown,
     *            typically by the CallableReturns c
     * @deprecated this method is not needed unless a security manager
     *             is used
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    public final <T> T doScriptSandboxedReturns(final CallableReturns<T> c)
	throws SecurityException {
	return c.call();
	-- rest of the method was commented out before we commented out
           the whole method
	try {
	    onScriptStarting();
	    return AccessController.doPrivileged
		(new PrivilegedAction<T>() {
		    public T run() {
			return c.call();
		    }
		}, noPermContext);
	} finally {
	    onScriptEnding();
	}
    }
     */

    /*
     * Execute code, returning a value and possibly throwing a checked
     * exception, in privileged mode.
     * @param c an ExceptionedCallableReturns providing the code to run
     * @return the value produced by running c
     * @exception Exception a checked exception was thrown either
     *            by the runnable or by the methods onExecutionStarting()
     *            or onExecutionEnding()
     * @exception SecurityException a security exception was thrown, typically
     *            by the ExceptionedCallableReturns c
     * @exception Exception an exception was raised
     * @deprecated the security manager is being removed from Java.
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    protected final <T>
	T doScriptPrivilegedReturns(final ExceptionedCallableReturns<T> c)
	throws SecurityException, Exception
    {
	return c.call();
	-- rest had been commented out
	try {
	    checkPrivileged();
	    onExecutionStarting();
	    try {
		if (privilegedContext == null) {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<T>() {
				public T run() throws Exception {
				    return c.call();
				}
			    });
		} else {
		    return AccessController.doPrivileged
			(new PrivilegedExceptionAction<T>() {
				public T run() throws Exception {
				    return c.call();
				}
			    }, privilegedContext);
		}
	    } catch (PrivilegedActionException e) {
		throw (Exception) e.getCause();
	    }
	} finally {
	    onExecutionEnding();
	}
    }
    */

    /*
     * Execute code, returning a value and that can throw a checked exception,
     * inside a scripting context's sandbox.
     * <P>
     * @param c an ExceptionedCallableReturns providing the code to run
     * @return the value produced by running c
     * @exception Exception a checked exception was thrown either
     *            by the runnable or by the methods onExecutionStarting()
     *            or onExecutionEnding()
     * @exception SecurityException a security exception was thrown,
     *            typically by the ExceptionedCallableReturns c
     *            or onExecutionEnding()
     * @exception Exception an exception was raised
     * @deprecated this method is not needed unless a security manager
     *             is used
     * -- removed because the security manager is being removed
     *    and this method is not used in existing applications
     *    or libraries.
    @Deprecated
    public final <T>
	T doScriptSandboxedReturns(final ExceptionedCallableReturns<T> c)
	throws SecurityException, Exception
    {
	return c.call();
	-- rest should be commented out
	try {
	    onScriptStarting();
	    try {
		return AccessController.doPrivileged
		    (new PrivilegedExceptionAction<T>() {
			public T run() throws Exception {
			    return c.call();
			}
		    }, noPermContext);
	    } catch (PrivilegedActionException e) {
		throw (Exception) e.getCause();
	    }
	} finally {
	    onScriptEnding();
	}
    }
    */

    /*
     * Indicate that code will be run in a scripting-context's sandbox.
     * Calls to onExecutionStarting and onExecutionEnding, or to
     * onScriptStarting and onScriptEnding, must occur in pairs with
     * onExecutionStarting or onScriptStarting running first in any
     * pair. Pairs may be nested.
     * @exception SecurityException the current thread is not consistent
     *            with thread group a ScriptingSecurityManager associated
     *            with this object

    private void onScriptStarting() throws SecurityException {
	if (notTrusted) {
	    sandboxStack.get().push(inSandboxFlag.get());
	    inSandboxFlag.set(Boolean.TRUE);
	}
    }
    */

    /*
     * Undo the effects of a preceding call to onScriptStarting.
     * Calls to onExecutionStarting and onExecutionEnding, or to
     * onScriptStarting and onScriptEnding, must occur in pairs with
     * onExecutionStarting or onScriptStarting running first in any
     * pair. Pairs may be nested.
     * @exception SecurityException the current thread is not consistent
     *            with thread group a ScriptingSecurityManager associated
     *            with this object

    private  void onScriptEnding() throws SecurityException {
	if (notTrusted) {
	    inSandboxFlag.set(sandboxStack.get().pop());
	}
    }
    */

    private HashMap<Properties,Bindings> bindingsMap =
	new HashMap<Properties,Bindings>();

    /*
     * Specifies the sandbox mode for
     * {@link #invokePrivateFunction(Properties,PFMode,String,Object...) invokePrivateFunction}.
     * This mode is recognized when using
     * {@link ScriptingSecurityManager}, but not when using
     * {@link SecurityManager}.
     * @deprecated the sandbox mode is meaningful only if a security
     *             manager is installed, and the security manager is
     *             being removed from Java
    @Deprecated
    static public enum PFMode {
	**
	 * Invoke the function in privileged mode.  A script will run
	 * outside of a sandbox with permissions determined by a
	 * security policy for a subclass of ScriptingContext. If the
	 * subclass is the target for the permission
	 * org.bzdev.scripting.ScriptingContextPermission and the actions
	 * contain the substring "privileged", the permissions for
	 * the subclass will be used. Otherwise no permissions are granted.
	 *
	PRIVILEGED,
	**
	 * Invoke the function in sandboxed mode. A script will run
	 * inside a sandbox.
	 *
	SANDBOXED,
	**
	 * Invoke the function in normal mode.
	 *
	 NORMAL
    };
    */

    private class BindingsMap {
	Bindings defaultBindings = getDefaultBindings();
	int size = 64;
	HashMap<Properties,Object> defaultMap = new HashMap<>();
	LinkedHashMap<Bindings,HashMap<Properties,Object>> map
	    = new LinkedHashMap<>(size) {
		protected boolean removeEldestEntry

		    (Map.Entry<Bindings,HashMap<Properties,Object>> entry)
		{
		    return (size() > (size-1));
		}
	    };
	public Object get(Bindings bindings, Properties props) {
	    if (bindings == defaultBindings) {
	        return defaultMap.get(props);
	    } else {
		HashMap<Properties,Object> map2 = map.get(bindings);
		if (map2 == null) {
		    return null;
		} else {
		    return map2.get(props);
		}
	    }
	}
	public void put(Bindings bindings, Properties props, Object obj) {
	    if (bindings == defaultBindings) {
		defaultMap.put(props, obj);
	    } else {
		HashMap<Properties,Object> omap = map.get(bindings);
		if (omap == null) {
		    omap = new HashMap<Properties,Object>();
		    map.put(bindings, omap);
		}
		omap.put(props, obj);
	    }
	}
    }
    private BindingsMap bindingsMaps = new BindingsMap();

    /*
     * Invoke a function defined by a private script with specified bindings.
     * The script is stored in a {@link java.util.Properties Properties}
     * list of properties under a key with the same name that
     * {@link #getScriptLanguage} returns in order to allow
     * multiple scripting languages to be supported (EMCAScript, the
     * official name for Javascript, is supported by the JDK, with
     * other scripting languages available separately). The script will
     * be evaluated once in sandbox mode, and must return an object whose
     * methods will represent the functions that can be called (while
     * at the implementation level, the methods of an object are called,
     * the object is not visible to the caller).
     * The {@link javax.script.Invocable Invocable} interface will
     * then be used to evaluate the appropriate method without having to
     * evaluate the script each time this method is called. A separate
     * object is used for each bindings/properties pair.
     * <P>
     * The evaluation of the function is performed in the mode
     * specified by the third argument.
     * <P>
     * Note: this method was added (with a change in the format of the
     * properties for each language) because testing indicated that
     * the Graal Javascript implementation, which a time this method
     * was introduced, is likely to be the replacement for Nashorn, does
     * not allow a function defined for one set of bindings to be used with
     * objects defined in another set of bindings. To be sure this method
     * would work with Graal's scripting languages, the object containing
     * the methods is now placed in the same bindings used to obtain the
     * arguments listed in the arguments that follow the function name.
     * @param bindings the bindings to use when evaluating the
     *        function
     * @param properties a properties list containing the script as an
     *        entry for a given scripting-language name (ECMAScript
     *        for Javascript)
     * @param mode the private-function mode to use
     * @param functionName the name of the function to invoke
     * @param args the function arguments
     * @return the object produced by the script evaluation
     * @exception ScriptException an error occurred during the execution of
     *            the script
     * @exception UnsupportedOperationException no script engine was provided
     * @deprecated specifyhing mode  is meaningful only if a security
     *             manager is installed, and the security manager is
     *             being removed from Java
     * @see #invokePrivateFunction(Bindings,Properties,String,Object...)
    @Deprecated
    protected final Object invokePrivateFunction(Bindings bindings,
						 Properties properties,
						 PFMode mode,
						 final String functionName,
						 final Object... args)
	throws ScriptException, UnsupportedOperationException
    {
	return invokePrivateFunction(bindings, properties, functionName, args);
    }
     */

    /**
     * Invoke a function defined by a private script with specified bindings.
     * The script is stored in a {@link java.util.Properties Properties}
     * list of properties under a key with the same name that
     * {@link #getScriptLanguage} returns in order to allow
     * multiple scripting languages to be supported (EMCAScript, the
     * official name for Javascript, is supported by the JDK, with
     * other scripting languages available separately). The script will
     * be evaluated once in sandbox mode, and must return an object whose
     * methods will represent the functions that can be called (while
     * at the implementation level, the methods of an object are called,
     * the object is not visible to the caller).
     * The {@link javax.script.Invocable Invocable} interface will
     * then be used to evaluate the appropriate method without having to
     * evaluate the script each time this method is called. A separate
     * object is used for each bindings/properties pair.
     * <P>
     * The evaluation of the function is performed in the mode
     * specified by the third argument.
     * <P>
     * Note: this method was added (with a change in the format of the
     * properties for each language) because testing indicated that
     * the Graal Javascript implementation, which a time this method
     * was introduced, is likely to be the replacement for Nashorn, does
     * not allow a function defined for one set of bindings to be used with
     * objects defined in another set of bindings. To be sure this method
     * would work with Graal's scripting languages, the object containing
     * the methods is now placed in the same bindings used to obtain the
     * arguments listed in the arguments that follow the function name.
     * @param bindings the bindings to use when evaluating the
     *        function
     * @param properties a properties list containing the script as an
     *        entry for a given scripting-language name (ECMAScript
     *        for Javascript)
     * @param functionName the name of the function to invoke
     * @param args the function arguments
     * @return the object produced by the script evaluation
     * @exception ScriptException an error occurred during the execution of
     *            the script
     * @exception UnsupportedOperationException no script engine was provided
     */
    protected final Object invokePrivateFunction(Bindings bindings,
						 Properties properties,
						 final String functionName,
						 final Object... args)
	throws ScriptException, UnsupportedOperationException
    {
	if (engine == null) engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	}
	BindingInfo saved = saveBindings();
	try {
	    if (bindings == null) bindings = getDefaultBindings();
	    setBindings(bindings);
	    try {
		Object scriptObject =
		    bindingsMaps.get(bindings, properties);
		if (scriptObject == null) {
		    final String script = (String)
			properties.get(getScriptLanguage());
		    try {
			scriptObject = AccessController.doPrivileged
			    (new PrivilegedExceptionAction<Object>() {
				public Object run() throws ScriptException {
				    return engine.eval(script);
				}
			    }, noPermContext);
		    } catch (PrivilegedActionException epa) {
			throw (ScriptException) epa.getCause();
		    }
		    bindingsMaps.put(bindings, properties, scriptObject);
		}
		try {
		    final Invocable inv = (Invocable)engine;
		    AccessControlContext ourContext = privilegedContext;
		    return inv.invokeMethod(scriptObject,
					    functionName,
					    args);
		    /*
		    switch (mode) {
		    case PRIVILEGED:
			checkPrivileged();
			onExecutionStarting();
			break;
		    case SANDBOXED:
			onScriptStarting();
			ourContext = noPermContext;
			break;
		    case NORMAL:
			return inv.invokeMethod(scriptObject,
						functionName,
						args);
		    }
		    try {
			final Object theScriptObject = scriptObject;
			if (ourContext == null) {
			    return AccessController.doPrivileged
				(new PrivilegedExceptionAction<Object>() {
					public Object run()
					    throws ScriptException,
						   NoSuchMethodException {
					    return inv
						.invokeMethod(theScriptObject,
							      functionName,
							      args);
					}
				    });
			} else {
			    return AccessController.doPrivileged
				(new PrivilegedExceptionAction<Object>() {
					public Object run()
					    throws ScriptException,
						   NoSuchMethodException {
					    return inv
						.invokeMethod(theScriptObject,
							      functionName,
							      args);
					}
				    }, ourContext);
			}
		    } catch(PrivilegedActionException e) {
			Exception ee = (Exception) e.getCause();
			if (ee instanceof ScriptException) {
			    throw (ScriptException) ee;
			} else if (ee instanceof NoSuchMethodException) {
			    throw (NoSuchMethodException) ee;
			} else {
			    String msg = errorMsg("unrecognizedException");
			    throw new Error(msg, ee);
			}
		    }
		    */
		} finally {
		    /*
		    switch (mode) {
		    case PRIVILEGED:
			onExecutionEnding();
			checkPrivileged();
			break;
		    case SANDBOXED:
			onScriptEnding();
			break;
		    case NORMAL:
			// do nothing
		    }
		    */
		}
	    } catch(NoSuchMethodException e) {
		throw new ScriptException(e);
	    }
	} finally {
	    restoreBindings(saved);
	}
    }


    /*
     * Invoke a function from a private script.
     * The script is stored in a {@link java.util.Properties Properties}
     * list of properties under a key with the same name that
     * {@link #getScriptLanguage} returns in order to allow
     * multiple scripting languages to be supported (EMCAScript, the
     * official name for Javascript, is supported by the JDK, with
     * other scripting languages available separately). The script will
     * be evaluated once in sandbox mode, and must return an object whose
     * methods will represent the functions that can be called (while
     * at the implementation level, the methods of an object are called,
     * the object is not visible to the caller).
     * The {@link javax.script.Invocable Invocable} interface will
     * then be used to evaluate the appropriate method without having to
     * evaluate the script each time this method is called. A separate
     * object is used for each bindings/properties pair, and the
     * bindings are those currently in effect for this scripting
     * context's script engine.
     * <P>
     * The evaluation of the function is performed in the mode
     * specified by the second argument.
     * <P>
     * Note: this method was added (with a change in the format of the
     * properties for each language) because testing indicated that
     * the Graal Javascript implementation, which a time this method
     * was introduced, is likely to be the replacement for Nashorn, does
     * not allow a function defined for one set of bindings to be used with
     * objects defined in another set of bindings. To be sure this method
     * would work with Graal's scripting languages, the object containing
     * the methods is now placed in the same bindings used to obtain the
     * arguments listed in the arguments that follow the function name.
     * @param properties a properties list containing the script as an entry for
     *        a given scripting-language name (ECMAScript for Javascript)
     * @param mode the private-function mode to use
     * @param functionName the name of the function to invoke
     * @param args the function arguments
     * @return the object produced by the script evaluation
     * @exception ScriptException an error occurred during the execution of
     *            the script
     * @exception UnsupportedOperationException no script engine was provided
     * @deprecated specifyhing a mode is meaningful only if a security
     *             manager is installed, and the security manager is
     *             being removed from Java
     * @see #invokePrivateFunction(Properties,String,Object...)
    @Deprecated
    protected final Object invokePrivateFunction(Properties properties,
					   PFMode mode,
					   final String functionName,
					   final Object... args)
	throws ScriptException, UnsupportedOperationException

    {
	return invokePrivateFunction(properties, functionName, args);
    }
     */

    /**
     * Invoke a function from a private script.
     * The script is stored in a {@link java.util.Properties Properties}
     * list of properties under a key with the same name that
     * {@link #getScriptLanguage} returns in order to allow
     * multiple scripting languages to be supported (EMCAScript, the
     * official name for Javascript, is supported by the JDK, with
     * other scripting languages available separately). The script will
     * be evaluated once in sandbox mode, and must return an object whose
     * methods will represent the functions that can be called (while
     * at the implementation level, the methods of an object are called,
     * the object is not visible to the caller).
     * The {@link javax.script.Invocable Invocable} interface will
     * then be used to evaluate the appropriate method without having to
     * evaluate the script each time this method is called. A separate
     * object is used for each bindings/properties pair, and the
     * bindings are those currently in effect for this scripting
     * context's script engine.
     * <P>
     * The evaluation of the function is performed in the mode
     * specified by the second argument.
     * <P>
     * Note: this method was added (with a change in the format of the
     * properties for each language) because testing indicated that
     * the Graal Javascript implementation, which a time this method
     * was introduced, is likely to be the replacement for Nashorn, does
     * not allow a function defined for one set of bindings to be used with
     * objects defined in another set of bindings. To be sure this method
     * would work with Graal's scripting languages, the object containing
     * the methods is now placed in the same bindings used to obtain the
     * arguments listed in the arguments that follow the function name.
     * @param properties a properties list containing the script as an entry for
     *        a given scripting-language name (ECMAScript for Javascript)
     * @param functionName the name of the function to invoke
     * @param args the function arguments
     * @return the object produced by the script evaluation
     * @exception ScriptException an error occurred during the execution of
     *            the script
     * @exception UnsupportedOperationException no script engine was provided
     */
    protected final Object invokePrivateFunction(Properties properties,
					   final String functionName,
					   final Object... args)
	throws ScriptException, UnsupportedOperationException

    {
	if (engine == null) engine = getScriptEngine();
	if (engine == null) {
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	}
	return invokePrivateFunction(engine.getBindings
				     (ScriptContext.ENGINE_SCOPE),
				     properties, functionName, args);
    }

    /**
     * Get a set of object names.
     * These names are the names for scripting-language objects
     * that are defined in the current engine-scope and global-scope bindings.
     * This method is useful mainly for debugging or for obtaining
     * data for documentation.
     * @return an unmodifiable set of the current object names
     */
    public Set<String> getNames() {
	ScriptEngine engine = getScriptEngine();
	if (engine == null) return null;
	TreeSet<String> names = new TreeSet<>();
	Bindings ebindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	for (String name: ebindings.keySet()) {
	    names.add(name);
	}
	Bindings gbindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
	for (String name: gbindings.keySet()) {
	    names.add(name);
	}
	return Collections.unmodifiableSortedSet(names);
    }
}

//  LocalWords:  exbundle sandboxed ScriptingContext PFMode JVM args
//  LocalWords:  doScriptPrivileged doScriptPrivilegedReturns boolean
//  LocalWords:  invokePrivateFunction ScriptingContextPermission JDK
//  LocalWords:  ScriptingSecurityManager ScriptinSecurityManager sm
//  LocalWords:  inSandbox evalScript callScriptFunction runtime se
//  LocalWords:  callScriptMethod ExceptionedCallable CallableReturns
//  LocalWords:  ExceptionedCallableReturns DefaultScriptingContext
//  LocalWords:  ExtendedScriptingContext swapbindings getClass clazz
//  LocalWords:  SecurityException getName ECMAScript Javascript eval
//  LocalWords:  ScriptEngineManager's getEngineByName doPrivilege
//  LocalWords:  AccessController ScriptEngineManager languageName
//  LocalWords:  nullLang trustedInSandbox ScriptException noBindings
//  LocalWords:  NullPointerException UnsupportedOperationException
//  LocalWords:  scriptingNotEnabled fileName nullScript functionName
//  LocalWords:  IllegalArgumentException bindingsFromOther setWriter
//  LocalWords:  NoSuchMethodException unrecognizedException swappers
//  LocalWords:  nullFunctionName noDefaultBindings scriptObject inv
//  LocalWords:  methodName nullScriptObject nullMethodName setReader
//  LocalWords:  getDefaultBindings noBindingsExist doGetScriptEngine
//  LocalWords:  ScriptContext setErrorWriter onExecutionStarting
//  LocalWords:  onExecutionEnding onScriptStarting onScriptEnding
//  LocalWords:  doPrivileged doScriptPrivleged substring EMCAScript
//  LocalWords:  getScriptLanguage Invocable noScriptEngine swapper
//  LocalWords:  createBindingSwapper Subclasses BindingSwapper Graal
//  LocalWords:  subclasses devqsim HashSet runnable unmodifiable
//  LocalWords:  scrunner doScriptSandboxed doScriptSandboxedReturns
//  LocalWords:  ScriptingContext's evalScriptPrivileged Nashorn
//  LocalWords:  AccessControlContext BootstrapMethodError GraalVM
//  LocalWords:  ScriptingSecurityManger OpenJDK PrivilegedAction
//  LocalWords:  checkPermission privilegedContext allowedClassNames
//  LocalWords:  GraalVM's getPrivilegedContext SecurityManager
//  LocalWords:  getSecurityManager noPermContext ProtectionDomain
//  LocalWords:  getProtectionDomain getContext notTrusted errorMsg
//  LocalWords:  trustedEscalation defaultBindings ourBindings
//  LocalWords:  invokeFunction invokeMethod Graal's
