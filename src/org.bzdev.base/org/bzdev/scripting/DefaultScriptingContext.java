package org.bzdev.scripting;
import javax.script.*;
//@exbundle org.bzdev.scripting.lpack.Scripting

/**
 * Default implementation of a scripting context.
 * If no scripting language is specified, or the language passed
 * to {@link DefaultScriptingContext(String)} is null, the language
 * <A HREF="{@docRoot}/org.bzdev.base/org/bzdev/util/doc-files/esp.html">ESP</A> * is chosen by default. ESP was developed because the default scripting engine
 * for Java-11 has been deprecated and will go away in future releases.
 * Meanwhile the GraalVM implementation of ECMAScript (at least, at the point
 * this documentation was written) will not allow a objects from a script
 * evaluated in one thread to be used in a different thread, which is
 * problematic for classes such as {@link org.bzdev.devqsim.Simulation},
 * where code such as
 * <BLOCKQUOTE><CODE><PRE>
 * import org.bzdev.devqsim.Simulation;
 * import org.bzdev.devqsim.TaskThread;
 *
 * public class TestSim1 {
 *     public static void main(String argv[]) throws Exception {
 *
 *         Simulation sim = new Simulation();
 *         sim.scheduleTask(() -&gt; {
 *             for(int i = 0; i &lt; 5; i++) {
 *                System.out.println("task is at simulation time "
 *                                   + sim.currentTicks()
 *                                   + " [i= " + i +"]");
 *                 TaskThread.pause(10);
 *              }
 *          }, 10);
 *          sim.run();
 *     }
 * }
 * </PRE></CODE></BLOCKQUOTE>
 * can be used instead of the faster, but less readable equivalent
 * <BLOCKQUOTE><CODE><PRE>
 * import org.bzdev.devqsim.Simulation;
 * import org.bzdev.lang.Callable;
 *
 * public class TestSim2 {
 *
 *    static Callable nextCall(final Simulation sim, final int i) {
 *        return () -&gt; {
 *            System.out.println("task is at simulation time "
 *                               + sim.currentTicks()
 *                               + " [i= " + i +"]");
 *             if (i &lt; 4) {
 *                 sim.scheduleCall(nextCall(sim, i+1), 10);
 *             }
 *    };
 *
 *    public static void main(String argv[]) throws Exception {
 *	  Simulation sim = new Simulation();
 *        sim.scheduleCall(nextCall(sim, 0), 10);
 *       sim.run();
 *    }
 * }
 * </PRE></CODE></BLOCKQUOTE>
 * The equivalent for ECMAScript is
 * <BLOCKQUOTE><CODE><PRE>
 * scripting.importClass("org.bzdev.devqsim.Simulation");
 * scripting.importClass("org.bzdev.devqsim.TaskThread");
 *
 * var sim = new Simulation(scripting);
 * sim.scheduleTaskObject({run: function() {
 *       for (var i = 0; i &lt; 5; i++) {
 *          scripting.getWriter().println("task is at simulation time "
 *                                        + sim.currentTicks()
 *                                        + " [i= " + i +"]");
 *          TaskThread.pause(10);
 *      }
 *  }
 *}, 10);
sim.run()
 * </PRE></CODE></BLOCKQUOTE>
 * and for ESP is
 * <BLOCKQUOTE><CODE><PRE>
 * import(org.bzdev.devqsim, [Simulation, TaskThread]);
 *
 * var sim = new Simulation(scripting);
 * sim.scheduleTaskObject({run: function() {
 *     IntStream.range(0,5).forEachOrdered(function(i) {
 *         global.getWriter().println("task is at simulation time "
 *                                    + sim.currentTicks()
 *                                    + " [i= " + i +"]");
 *         TaskThread.pause(10);
 *         });
 *      }
 * }, 10);
 * sim.run()
 * </PRE></CODE></BLOCKQUOTE>
 */
public class DefaultScriptingContext extends ScriptingContext {
    private ScriptEngine engine;
    private Bindings defaultBindings;
    private String languageName;

    static String errorMsg(String key, Object... args) {
	return Scripting.errorMsg(key, args);
    }

    private static final String DEFAULT_LANG_NAME = "ESP";

    private void init(String languageName)
	throws IllegalArgumentException
    {
	languageName = ((languageName == null)? DEFAULT_LANG_NAME:
			languageName);
	ScriptEngineManager manager = new ScriptEngineManager();
	engine = ScriptingContext.createScriptEngine(manager, languageName);
	if (engine == null) {
	    throw new IllegalArgumentException
		(errorMsg("unsupportedLang", languageName));
	}

	if (engine.get(ScriptEngine.NAME) == null) {
	    engine.put(ScriptEngine.NAME, languageName);
	}
	defaultBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	this.languageName = languageName;
    }

    /**
     * Constructor given a scripting-language name.
     * @param languageName the name of the scripting language; null
     *        for the default (ECMAScript)
     */
    public DefaultScriptingContext(String languageName) {
	super();
	init(languageName);
    }

    /**
     * Constructor.
     * The default language, ESP, will be used. Please see
     * <A HREF="{@docRoot}/org.bzdev.base/org/bzdev/util/doc-files/esp.html">
     * ExpressionParser and the ESP scripting language</A> for a description
     * of ESP.
     *
     */
    public DefaultScriptingContext() {
	this(DEFAULT_LANG_NAME);
    }

    /*
     * Constructor specifying the language name and the security mode.
     * @param languageName the name of the scripting language; null
     *        for the default,
     * <A HREF="{@docRoot}/org.bzdev.base/org/bzdev/util/doc-files/esp.html">ESP</A>.
     * @param trusted true if the script context is trusted; false otherwise
     * @exception SecurityException this subclass of ScriptingContext cannot
     *            be created after a security manager was installed or an
     *            attempt was made to create a trusted subclass of
     *            ScriptingContext from inside a sandbox
     *
    public DefaultScriptingContext(String languageName,
				   boolean trusted)
	throws SecurityException
    {
	super(trusted);
	init(languageName);
    }
     */

    /*
     * Constructor specifying the security mode.
     * The default language, ECMAScript, will be used.
     * @param trusted true if the script context is trusted; false otherwise
     * @exception SecurityException this subclass of ScriptingContext cannot
     *            be created after a security manager was installed or an
     *            attempt was made to create a trusted subclass of
     *            ScriptingContext from inside a sandbox
    public DefaultScriptingContext(boolean trusted) throws SecurityException {
	this(DEFAULT_LANG_NAME, trusted);
    }
     */

    @Override
    protected ScriptEngine doGetScriptEngine() {
	return engine;
    }

    @Override
    protected Bindings doGetDefaultBindings() {
	return defaultBindings;
    }

    @Override
    protected String doGetScriptLanguage() {
	return languageName;
    }
}

//  LocalWords:  exbundle DefaultScriptingContext boolean HREF PRE lt
//  LocalWords:  GraalVM BLOCKQUOTE TestSim argv scheduleTask
//  LocalWords:  currentTicks TaskThread nextCall scheduleCall
//  LocalWords:  importClass scheduleTaskObject unsupportedLang
//  LocalWords:  languageName ExpressionParser SecurityException
//  LocalWords:  ScriptingContext
