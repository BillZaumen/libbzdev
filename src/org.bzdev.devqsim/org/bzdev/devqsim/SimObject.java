package org.bzdev.devqsim;
import org.bzdev.lang.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import javax.script.*;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import org.bzdev.util.EvntListenerList;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Base class for simulation objects.
 * This class provides the common methods
 * needed to determine if an object can be looked up by name and
 * to be able to get an object's name.  It also allows scripts to
 * be evaluated in a name space initialized with two variables:
 * "sim" (the object's simulation) and "self" (the object itself),
 * and allows these scripts to be scheduled on the event queue or
 * run a script to implement a task.
 */
@NamedObject(helperClass = "SimObjectHelper",
	     namerHelperClass = "SimulationHelper",
	     namerClass = "Simulation")
abstract public class SimObject extends SimObjectHelper
    implements NamedObjectOps
{
    private String name;
    private Class clazz;
    // private Class subclass;

    private Simulation simulation;


    final Simulation getSimulationAsSimulation() {return simulation;}

    static private long nameID =  0;
    private String genName(Class subclass) {
	return "[" +subclass.getName()  +(nameID++) +"]";

    }

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    javax.script.Bindings scriptBindings;
    javax.script.ScriptEngine scriptEngine;

    /**
     * Evaluate a script.
     * This can be used to allow an object's behavior to be partially
     * implemented in a scripting language.  Each SimObject is initialized
     * with two variables --- "sim", whose value is the simulation, and "self",
     * whose value is the current SimObject.
     * @param script the script to evaluate
     * @return the value computed by the script
     * @exception ScriptException an exception occurred while executing a
     *            script
     * @exception UnsupportedOperationException scripting is not supported
     */
    protected Object evalScript(String script)
	throws ScriptException, UnsupportedOperationException
    {
	if (scriptBindings == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return simulation.evalScript(script, scriptBindings);
    }

    /**
     * Create a Callable that will run a script using this object's name space.
     * @param script the script
     * @return the callable that will run a script
     */
    protected Callable callableScript(final String script) {
	return new Callable() {
	    public void call() {
		try {
		    scriptBindings.put("sim", simulation);
		    scriptBindings.put("self", SimObject.this);
		    evalScript(script);
		} catch (ScriptException e) {
		    throw new RuntimeException(e);
		}
	    }
	};
    }

    /**
     * Evaluate a script function.
     * This can be used to allow an object's behavior to be partially
     * implemented in a scripting language.  Each SimObject is initialized
     * with two variables --- "sim", whose value is the simulation, and "self",
     * whose value is the current SimObject.
     * @param functionName the script function to call
     * @param args the arguments
     * @return the value computed by the function
     * @exception ScriptException an exception occurred in a script object's
     *            method
     * @exception UnsupportedOperationException scripting is not supported
     */
    protected Object callScriptFunction(String functionName, Object... args)
	throws ScriptException, UnsupportedOperationException
    {
	if (scriptBindings == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	try {
	    return simulation.callScriptFunction
		(scriptBindings, functionName, args);
	} catch (NoSuchMethodException e) {
	    throw new ScriptException(e);
	}
    }

    /**
     * Evaluate a script object's method.
     * This can be used to allow an object's behavior to be partially
     * implemented in a scripting language.  Each SimObject is initialized
     * with two variables --- "sim", whose value is the simulation, and "self",
     * whose value is the current SimObject.
     * @param scriptObject the script object whose method is to be invoked
     * @param methodName the name of the method to invoke
     * @param args the arguments
     * @return the value computed by the function
     * @exception ScriptException an exception occurred in a script object's
     *            method
     * @exception UnsupportedOperationException scripting is not supported
     */
    protected Object callScriptMethod(Object scriptObject,
				      String methodName,
				      Object... args)
	throws ScriptException, UnsupportedOperationException
    {
	if (scriptBindings == null) {
	    throw new UnsupportedOperationException
		(errorMsg("scriptingNotEnabled"));
	}
	try {
	    return simulation.callScriptMethod(scriptBindings,
					       scriptObject, methodName, args);
	} catch (NoSuchMethodException e) {
	    throw new ScriptException(e);
	}
    }

    /**
     * Get a script object given a name.
     * The object is obtained from this SimObject's name space.
     * @param name the name of the object
     * @return the object; null if there is none
     */
    protected Object getScriptObject(String name) {
	if (scriptBindings == null) return null;
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return scriptBindings.get(name);
    }

    /**
     * Assign a name to a script object in a sim object's name space.
     * The names "self" and "sim" are reserved.
     * @param name the name to assign to the object
     * @param object the object
     * @exception UnsupportedOperationException the simulation does not
     *            support scripting
     * @exception IllegalArgumentException the name provided is a
     *            reserved name
     */
    protected void putScriptObject(String name, Object object)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	if (name.equals("self") || name.equals("sim")) {
	    throw new IllegalArgumentException(errorMsg("reservedName"));
	}
	scriptBindings.put(name, object);
    }


    /**
     * Schedule a script providing a delay.
     * @param script the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script is executed
     * @return the event scheduled
     * @exception UnsupportedOperationException scripting is not supported
     *
     */

    protected TaskSimulationEvent scheduleScript(String script, long delay)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return simulation.scheduleScript(script, delay, scriptBindings);
    }

    /**
     * Schedule a script object providing a delay.
     * The script object's <code>call()</code> method will be run when
     * the event is processed.
     * @param scriptObject the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script object's
     *              <code>call()</code> method is executed
     * @return the event scheduled
     * @exception UnsupportedOperationException scripting is not supported
     *
     */
    protected TaskSimulationEvent scheduleCallObject(Object scriptObject,
						  long delay)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", SimObject.this);
	return simulation.scheduleCallObject(scriptObject,
					     delay,
					     scriptBindings);
    }

    /**
     * Create a Callable that will invoke a script object's call method using
     * this object's namespace.
     * @param scriptObject the script object
     * @return a callable that will execute the script object's call method
     * @exception UnsupportedOperationException scripting is not supported
     */

    protected Callable callableObject(final Object scriptObject) {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	return new Callable() {
	    public void call() {
		try {
		    scriptBindings.put("sim", simulation);
		    scriptBindings.put("self", SimObject.this);
		    simulation.callScriptMethod(scriptBindings,
						scriptObject, "call");
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	    }
	};
    }


    /**
     * Schedule a Task specified by a script.
     * A thread executing the code provided by the script will be started
     * at a simulation time equal to the current simulation time + the value
     * of <code>delay</code>. A simulation method pauseTask is provided
     * for cases in which the scripting language does not make the class
     * org.bzdev.devqsim.TaskThread accessible.  If the task will not pause, it
     * is more efficient to use scheduleScript instead.
     * @param script the script implementing the task to schedule
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the thread that will run this task
     * @exception UnsupportedOperationException scripting is not supported
     *
     */
    protected TaskThread scheduleTaskScript(String script, long delay)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return simulation.scheduleTaskScript(script, delay, scriptBindings);
    }

    /**
     * Create a Runnable that will run a script in this object's name space.
     * @param script the script
     * @return the runnable created
     * @exception UnsupportedOperationException scripting is not supported
     */
    protected Runnable runnableScript(final String script)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	return new Runnable() {
	    public void run() {
		Thread cthread = Thread.currentThread();
		if (!(cthread instanceof TaskThread))
		    throw new IllegalStateException(errorMsg("needTaskThread"));
		/* ("must be run in a task thread");*/
		TaskThread taskThread = (TaskThread)cthread;
		simulation.configBindingSwapper(taskThread, scriptBindings);
		// taskThread.makeScriptingThread(scriptBindings,
		//			       simulation.getDefaultBindings());
		try {
		    SimObject.this.scriptBindings.put("sim", simulation);
		    SimObject.this.scriptBindings.put("self", SimObject.this);
		    simulation.evalScript(script, scriptBindings);
		} catch (ScriptException e) {
		    throw new RuntimeException(e);
		}
	    }
	};
    }


    /**
     * Create a Runnable that will run a script object in this
     * object's name space.
     * The script object's run method will be called when a task
     * thread is created and scheduled.
     * @param scriptObject the script object
     * @return the runnable created
     * @exception UnsupportedOperationException scripting is not supported
     */
    protected Runnable runnableObject(final Object scriptObject)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return new Runnable() {
	    public void run() {
		Thread cthread = Thread.currentThread();
		if (!(cthread instanceof TaskThread))
		    throw new IllegalStateException(errorMsg("needTaskThread"));
		/* ("must be run in a task thread");*/
		TaskThread taskThread = (TaskThread)cthread;
		simulation.configBindingSwapper(taskThread, scriptBindings);
		// taskThread.makeScriptingThread(scriptBindings,
		//			       simulation.getDefaultBindings());
		try {
		    SimObject.this.scriptBindings.put("sim", simulation);
		    SimObject.this.scriptBindings.put("self", SimObject.this);
		    simulation.callScriptMethod(scriptObject, "run");
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	    }
	};
    }


    /**
     * Schedule a task specified by a script object.
     * A thread executing the code provided by the script object's
     * <code>run()</code> method will be started
     * at a simulation time equal to the current simulation time + the value
     * of <code>delay</code>. A simulation method pauseTask is provided
     * for cases in which the scripting language does not make the class
     * org.bzdev.devqsim.TaskThread accessible.  If the task will not pause, it
     * is more efficient to use scheduleCallObject instead.
     * @param scriptObject the script object whose <code>run()</code>
     *                     method implements the task to schedule
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the thread that will run this task
     * @exception UnsupportedOperationException scripting is not supported
     *
     */
    protected TaskThread scheduleTaskObject(final Object scriptObject,
					    long delay)
	throws UnsupportedOperationException
    {
	if (scriptBindings == null)
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	scriptBindings.put("sim", simulation);
	scriptBindings.put("self", this);
	return simulation.scheduleTaskObject(scriptObject,
					     delay,
					     scriptBindings);
    }

    /**
     * Constructor for interned objects.
     * These objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
   protected SimObject(Simulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	simulation = sim;
	// scriptEngine = sim.getScriptEngine();
	scriptBindings = sim.createBindings();
	if (scriptBindings != null) {
	    scriptBindings.put("sim", simulation);
	    scriptBindings.put("self", this);
	}
    }

    @Override
    protected void onDelete() {
	clearTraceSets();
	super.onDelete();
    }

    /**
     * Update the object's state to that at a specified time.
     * The values of t passed to this method should form an
     * increasing sequence.  The default implementation does nothing,
     * and subclasses should override this method if the state needed
     * for drawing must be updated before the drawing is completed.
     * Aside from a direct subclass of SimObject, classes that
     * override this method should call the same method on their superclass
     * unless there is documentation to the contrary.
     * @param t the time for which the object should be calculated.
     * @param simtime the simulation time
     */
    protected void update(double t, long simtime) {
    }

    /**
     * Update the object's state to that for the current simulation time.
     * This method calls {@link #update(double,long)} with appropriate
     * arguments. Subclasses that need to call update should use the current
     * method as it will call {@link #update(double,long)} with the
     * correct sequence of time arguments.
     * <P>
     * The method {@link #update(double,long) update(double,long)}
     * should be implemented and
     * the method {@link #update() update()} should be called when
     * an object's state is to be updated only when the updated value is
     * needed.  Update() is used in the org.bzdev.anim2d package for efficiency
     * reasons. Calls to update are not built into methods such as
     * {@link #printConfiguration} because for debugging purposes it may
     * be desirable to print the configuration before and after update
     * is called. Methods that are used by
     * {@link #update(double,long) update(double,long)} should not call
     * {@link #update() update()}.
     * <P>
     * Subclasses should document methods that call update(). The
     * method update() is called by methods defined for the class
     * {@link org.bzdev.anim2d.AnimationObject2D}, the base class
     * for all anim2d objects.
     */
    public final void update() {
	update(simulation.currentTime(), simulation.currentTicks());
    }


    /**
     * Print a simulation object's configuration using a default writer.
     * When a scripting environment is used, the print writer configured
     * for the scripting environment is used; otherwise System.out is
     * used.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     */
    public void printConfiguration() {
	if (scriptBindings != null) {
	    Writer writer = null;
	    try {
		writer = simulation.getWriter();
	    } catch (Exception e) {
		writer = null;
	    }
	    if (writer != null) {
		printConfiguration(writer);
	    } else {
		printConfiguration(System.out);
	    }
	} else {
	    printConfiguration(System.out);
	}
    }

    /**
     * Print a simulation object's configuration using a default writer
     * and specifying an initial prefix.
     * When a scripting environment is used, the print writer configured
     * for the scripting environment is used; otherwise System.out is
     * used.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line with null
     * treated as an empty string
     */
    public void printConfiguration(String iPrefix) {
	if (scriptBindings != null) {
	    Writer writer = null;
	    try {
		writer = simulation.getWriter();
	    } catch (Exception e) {
		writer = null;
	    }
	    if (writer != null) {
		printConfiguration(iPrefix, writer);
	    } else {
		printConfiguration(iPrefix, System.out);
	    }
	} else {
	    printConfiguration(iPrefix, System.out);
	}
    }

    /**
     * Print a simulation object's configuration using an output stream.
     * The default method calls {@link #printConfiguration(String,Writer)}
     * with a print writer constructed from the print stream.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     * @param out the output print stream
     *
     */
    public void printConfiguration(PrintStream out) {
	PrintWriter writer = new PrintWriter(out);
	printConfiguration(writer);
	writer.flush();
    }

    /**
     * Print a simulation object's configuration using an output stream
     * and specifying an initial prefix.
     * The default method calls {@link #printConfiguration(Writer)}
     * with the print writer constructed from the print stream.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line with null
     * treated as an empty string
     * @param out the output print stream
     *
     */
    public void printConfiguration(String iPrefix, PrintStream out) {
	PrintWriter writer = new PrintWriter(out);
	printConfiguration(iPrefix, writer);
	writer.flush();
    }

    /**
     * Print a simulation object's configuration using a writer.
     * The default method calls
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}
     * with an empty prefix string for its first argument, a string
     * containing four spaces for its second argument,  and the value
     * <code>true</code> as its third argument. The fourth argument is
     * constructed from the <code>out</code> argument described for
     * this method.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     * @param out the output print stream
     *
     */
    public void printConfiguration(Writer out) {
	PrintWriter writer;
	if (out instanceof PrintWriter) {
	    writer = (PrintWriter) out;
	} else {
	    writer = new PrintWriter(out);
	}
	printConfiguration("", FOUR_SPACES, true, writer);
	writer.flush();
    }

    /**
     * Print a simulation object's configuration using a writer
     * and specifying an initial prefix.
     * The default method calls
     * {@link #printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * with iPrefix as its first argument, a string containing
     * four spaces appended to iPrefix as its second argument, and with the
     * value <code>true</code> as its third argument.  The fourth argument is
     * constructed from the <code>out</code> argument described for
     * this method.
     * <P>
     * The configuration itself is printed by the method
     * {@link #printConfiguration(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line with null
     * treated as an empty string
     * @param out the output print stream
     */
    public void printConfiguration(String iPrefix, Writer out) {
	PrintWriter writer;
	if (out instanceof PrintWriter) {
	    writer = (PrintWriter) out;
	} else {
	    writer = new PrintWriter(out);
	}
	if (iPrefix == null) iPrefix = EMPTY_STRING;
	printConfiguration(iPrefix, iPrefix + FOUR_SPACES, true, writer);
	writer.flush();
    }

    static final String EMPTY_STRING = "";
    static final String FOUR_SPACES = "    ";

    /**
     * Print a simulation object's configuration.
     * Simulation objects should implement this method to report how
     * they were configured.  The default method just prints a line
     * with the object's name when the second argument has the value
     * <code>true</code>.  A subclass must override this method and call
     * <code>printConfiguration(String,String,boolean,PrintWriter)</code>
     * (<code>PrintWriter</code> is defined in the package <code>java.io</code>)
     * on the superclass of the subclass. The overridden method's
     * arguments and the arguments passed to
     * <code>super.printConfiguration</code> must be the
     * same. Furthermore, this call must be the first
     * statement. Additional lines may then be printed with the
     * appropriate indentation.  As a recommendation, each additional
     * indentation should typically consist of 4 spaces.
     * <P>
     * Whenever this method is overridden, documentation should be
     * provided, at a minimum in the Javadoc comments for the method,
     * indicating what is being printed by the overridden method itself
     * so that programmers do not duplicate the same data when overriding
     * this method
     * <P>
     * The methods printConfiguration and printState are analogous. The
     * intention is for printConfiguration to show static or slowly varying
     * data whereas printState should show more rapidly-varying data.
     * <P>
     * Normally the printName argument has the value <code>true</code>.
     * Sometimes, however, one might choose to call this method to fill
     * in a table for some other class. In that case, printName should be set
     * to false (e.g., when the caller itself prints the name). The prefix
     * includes the standard indentation, which the other printConfiguration
     * methods in this class set to four spaces. When calling this method
     * explicitly, any previous prefix should be extended with additional
     * spaces.  Regardless of the prefix, if printName is true, the line
     * giving the object name will have the indentation specified by iPrefix.
     * When printing lines, one should use either prefix to determine the
     * indentation (iPrefix is used by {@link SimObject} itself. Calls to
     * methods such as {@link #printConfiguration(String,Writer)} that
     * provide iPrefix but not prefix will set prefix to iPrefix concatenated
     * with four spaces.
     *<P>
     * To document <code>printConfiguration(String,String,boolean,PrintWriter)</code>
     * methods that override this method, one should follow the
     * following recommendations:
     * <UL>
     *  <LI> for the first subclasses of this class that implements
     *       <code>printConfiguration(String,String,boolean,PrintWriter)</code>,
     *       use the following comment:
     *       <blockquote><code><pre>
     *  /**
     *   * Print this simulation object's configuration.
     *   * Documentation for the use of this method is provided by 
     *   * the documentation for the {{@literal @}link org.bzdev.devqsim.SimObject}
     *   * method
     *   * {{@literal @}link SimObject#printConfiguration(String,String,boolean,PrintWriter)}.
     *   * When the third argument has a value of true, the object name and
     *   * class name will be printed in a standard format with its indentation
     *   * provided by the iPrefix argument.
     *   * {@literal @}param iPrefix {{@literal @}inheritDoc}
     *   * {@literal @}param prefix {{@literal @}inheritDoc}
     *   * {@literal @}param printName {{@literal @}inheritDoc}
     *   * {@literal @}param out {{@literal @}inheritDoc}
     *   *{@literal /}
     *       </pre></code></blockquote>
     *       If the 
     *       <code>printConfiguration(String,String,boolean,PrintWriter)</code>
     *       prints something after invoking the same method on its
     *       superclass,  add the following before the parameter declarations:
     *       <blockquote><code><pre>
     *   * In addition, the configuration that is printed includes the
     *   * following items.
     *   * &lt;P&gt;
     *   * Defined in {{@literal @}link ...}:
     *   * &lt;UL&gt;
     *   *     ...
     *   * &lt;/UL&gt;
     *       </pre></code></blockquote>
     *       where the link is to the subclass overriding this method and
     *       the unnumbered list contains items describing each line or
     *       set of lines in the output.
     *  <LI> For subclasses of classes that have overridden
     *       <code>printConfiguration(String,String,boolean,PrintWriter)</code> and
     *       that are following these conventions, use the following
     *       javadoc comment:
     *       <blockquote><code><pre>
     *  /**
     *   * {{@literal @}inheritDoc}
     *   * &lt;P&gt;
     *   * Defined in {{@literal @}link ...}:
     *   * &lt;UL&gt;
     *   *    ...
     *   * &lt;/UL&gt;
     *   * {@literal @}param iPrefix {{@literal @}inheritDoc}
     *   * {@literal @} param prefix {{@literal @}inheritDoc}
     *   * {@literal @}param printName {{@literal @}inheritDoc}
     *   * {@literal @}param out {{@literal @}inheritDoc}
     *   *{@literal /}
     *       </pre></code></blockquote>
     *       For this case, if no superclass contains the following, it should
     *       be added after the first {@literal @}inheritDoc directive:
     *       <blockquote><code><pre>
     *   * In addition, the configuration that is printed includes the
     *   * following items.
     *       </pre></code></blockquote>
     *  <LI> For separate libraries, the {@literal @}inheritDoc mechanism
     *       will not be able to find text to copy as access to the
     *       source code is needed by the <code>javadoc</code> program.
     *       In this case, the 
     *       <code>printConfiguration(String,String,boolean,PrintWriter)</code>
     *       methods of each subclasses whose first superclass implements
     *       <code>printConfiguration(String,String,boolean,PrintWriter)</code>
     *       should either duplicate the javadoc comment so that
     *       the {@literal @}inheritDoc mechanism can append additional
     *       list items as done above or it should include a link to
     *       the method providing those items.
     * </UL>
     * For examples of the documentation this convention generates,
     * please look at the documentation for
     * {@link org.bzdev.anim2d.AnimationObject2D#printConfiguration(String,String,boolean,PrintWriter)}
     * and
     * {@link org.bzdev.anim2d.AnimationLayer2D#printConfiguration(String,String,boolean,PrintWriter)}.
     * <P>
     * These recommendations, if followed, will simply documenting this
     * method for subclasses of {@link SimObject} as one can use the
     * inheritDoc mechanism so that each method's documentation will list
     * all the fields it prints.
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param prefix a prefix string (typically whitespace) to put at
     *        the start of each line other than the initial line that is
     *        printed when printName is true
     * @param printName requests printing the name of an object
     * @param out the output print writer
     */
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	if (printName) {
	    if (iPrefix == null) iPrefix = EMPTY_STRING;
	    out.println(iPrefix + "\"" + getName() +"\" configuration"
			+" (class " +getClass().getName() +"):");
	}
    }

    /**
     * Print a simulation object's state on a default output stream.
     * When a scripting environment is used, the print writer configured
     * for the scripting environment is used; otherwise System.out is
     * used.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     */
    public void printState() {
	if (scriptBindings != null) {
	    Writer writer = null;
	    try {
		writer = simulation.getWriter();
	    } catch (Exception e) {
		writer = null;
	    }
	    if (writer != null) {
		printState(writer);
	    } else {
		printState(System.out);
	    }
	} else {
	    printState(System.out);
	}
    }

    /**
     * Print a simulation object's state on a default output stream
     * and specifying an initial prefix.
     * When a scripting environment is used, the print writer configured
     * for the scripting environment is used; otherwise System.out is
     * used.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     */
    public void printState(String iPrefix) {
	if (scriptBindings != null) {
	    Writer writer = null;
	    try {
		writer = simulation.getWriter();
	    } catch (Exception e) {
		writer = null;
	    }
	    if (writer != null) {
		printState(iPrefix, writer);
	    } else {
		printState(iPrefix, System.out);
	    }
	} else {
	    printState(iPrefix, System.out);
	}
    }

    /**
     * Print a simulation object's state using a writer.
     * The default method calls
     * {@link #printState(String,String,boolean,PrintWriter)}
     * with an empty string as its first argument, a string containing
     * four spaces as its second argument, and the value
     * <code>true</code>for its third argument. The fourth argument is
     * constructed from the <code>out</code> argument described for
     * this method.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     * @param out the output writer
     */
    public void printState(Writer out) {
	PrintWriter writer;
	if (out instanceof PrintWriter) {
	    writer = (PrintWriter) out;
	} else {
	    writer = new PrintWriter(out);
	}
	printState(EMPTY_STRING, FOUR_SPACES, true, writer);
	writer.flush();
    }

    /**
     * Print a simulation object's state using a writer.
     * The default method calls
     * {@link #printState(String,String,boolean,java.io.PrintWriter)}
     * with iPrefix as the first argument, four spaces appended to
     * iPrefix as the second argument,  <code>true</code> as its
     * third argument, and a print writer (constructed from <code>out</code>
     * if necessary) as its fourth argument.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param out the output print stream
     */
    public void printState(String iPrefix, Writer out) {
	PrintWriter writer;
	if (out instanceof PrintWriter) {
	    writer = (PrintWriter) out;
	} else {
	    writer = new PrintWriter(out);
	}
	printState(iPrefix, iPrefix + FOUR_SPACES, true, writer);
	writer.flush();
    }

    /**
     * Print a simulation object's state using a print stream.
     * The default method calls {@link #printState(Writer)}.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     * @param out the output print stream
     */
    public void printState(PrintStream out) {
	printState(new PrintWriter(out));
    }

    /**
     * Print a simulation object's state using a print stream
     * and specifying an initial prefix.
     * The default method calls {@link #printState(String,Writer)}.
     * <P>
     * The state itself is printed by the method
     * {@link #printState(String,String,boolean,PrintWriter)}.
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param out the output print stream
     */
    public void printState(String iPrefix, PrintStream out) {
	printState(iPrefix, new PrintWriter(out));
    }




    /**
     * Print a simulation object's state.
     * Simulation objects should implement this method to report how
     * they were configured.  The default method just prints a line
     * with the object's name when the second argument has the value
     * <code>true</code>.  A subclass must override this method and call
     * <code>printState(java.lang.String,boolean,PrintWriter)</code>
     * (<code>PrintWriter</code> is defined in the package <code>java.io</code>)
     * on the superclass of the subclass. The overridden method's
     * arguments and the arguments passed to
     * <code>super.printState</code> must be the
     * same. Furthermore, this call must be the first
     * statement. Additional lines may then be printed with the
     * appropriate indentation.  As a recommendation, each additional
     * indentation should typically consist of 4 spaces.
     * <P>
     * Whenever this method is overridden, documentation should be
     * provided, at a minimum in the Javadoc comments for the method,
     * indicating what is being printed by the overridden method itself
     * so that programmers do not duplicate the same data when overriding
     * this method
     * <P>
     * The methods printConfiguration and printState are analogous. The
     * intention is for printConfiguration to show static or slowly varying
     * data whereas printState should show more rapidly-varying data.
     * <P>
     * Normally the printName argument has the value <code>true</code>.
     * Sometimes, however, one might choose to call this method to fill
     * in a table for some other class. In that case, printName should be set
     * to false (e.g., when the caller itself prints the name). The prefix
     * includes the standard indentation, which the other printConfiguration
     * methods in this class set to four spaces. When calling this method
     * explicitly, any previous prefix should be extended with additional
     * spaces. Regardless of the prefix, if printName is true, the line
     * giving the object name will have no indentation.
     *<P>
     * To document <code>printState(String,String,boolean,PrintWriter)</code>
     * methods that override this method, one should follow the
     * following recommendations:
     * <UL>
     *  <LI> for the first subclasses of this class that implements
     *       <code>printState(String,String,boolean,PrintWriter)</code>
     *       use the following comment:
     *       <blockquote><code><pre>
     *  /**
     *   * Print this simulation object's state.
     *   * Documentation for the use of this method is provided by
     *   * the documentation for the {{@literal @}link org.bzdev.devqsim.SimObject}
     *   * method
     *   * {{@literal @}link SimObject#printState(String,String,boolean,PrintWriter)}.
     *   * When the third argument has a value of true, the object name and
     *   * class name will be printed in a standard format with its indentation
     *   * provided by the iPrefix argument.
     *   * {@literal @}param iPrefix {{@literal @}inheritDoc}
     *   * {@literal @}param prefix {{@literal @}inheritDoc}
     *   * {@literal @}param printName {{@literal @}inheritDoc}
     *   * {@literal @}param out {{@literal @}inheritDoc}
     *   *{@literal /}
     *       </pre></code></blockquote>
     *       If the method
     *       <code>printState(String,String,boolean,PrintWriter)</code>
     *       prints something after invoking the same method on its
     *       superclass, add the following before the parameter declarations:
     *       <blockquote><code><pre>
     *   * In addition, the state that is printed includes the
     *   * following items.
     *   * &lt;P&gt;
     *   * Defined in {{@literal @}link ...}:
     *   * &lt;UL&gt;
     *   *     ...
     *   * &lt;/UL&gt;
     *       </pre></code></blockquote>
     *       where the link is to the subclass overriding this method and
     *       the unnumbered list contains items describing each line or
     *       set of lines in the output.
     *  <LI> For subclasses of classes that have overridden
     *       <code>printState(String,String,boolean,PrintWriter)</code> and
     *       that are following these conventions, use the following
     *       javadoc comment:
     *       <blockquote><code><pre>
     *  /**
     *   * {{@literal @}inheritDoc}
     *   * Defined in {{@literal @}link ...}:
     *   * &lt;UL&gt;
     *   *    ...
     *   * &lt;/UL&gt;
     *   * {@literal @}param iPrefix {{@literal @}inheritDoc}
     *   * {@literal @} param prefix {{@literal @}inheritDoc}
     *   * {@literal @}param printName {{@literal @}inheritDoc}
     *   * {@literal @}param out {{@literal @}inheritDoc}
     *   *{@literal /}
     *       </pre></code></blockquote>
     *       For this case, if no superclass contains the following, it should
     *       be added after the first {@literal @}inheritDoc directive:
     *       <blockquote><code><pre>
     *   * In addition, the state that is printed includes the
     *   * following items.
     *       </pre></code></blockquote>
     *  <LI> For separate libraries, the {@literal @}inheritDoc mechanism
     *       will not be able to find text to copy as access to the
     *       source code is needed by the <code>javadoc</code> program.
     *       In this case, the
     *       <code>printState(String,String,boolean,PrintWriter)</code>
     *       methods of each subclasses whose first superclass implements
     *       <code>printState(String,String,boolean,PrintWriter)</code>
     *       should either duplicate the javadoc comment so that
     *       the {@literal @}inheritDoc mechanism can append additional
     *       list items as done above or it should include a link to
     *       the method providing those items.
     * </UL>
     * For examples of the documentation this convention generates,
     * please look at the documentation for
     * {@link org.bzdev.anim2d.AnimationObject2D#printState(String,String,boolean,PrintWriter)}
     * and
     * {@link org.bzdev.anim2d.DirectedObject2D#printState(String,String,boolean,PrintWriter)}.
     * <P>
     * These recommendations, if followed, will simply documenting this
     * method for subclasses of {@link SimObject} as one can use the
     * inheritDoc mechanism so that each method's documentation will list
     * all the fields it prints.
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param prefix a prefix string (typically whitespace) to put at
     *        the start of each line other than the initial line that is
     *        printed when printName is true
     * @param printName requests printing the name of an object
     * @param out the output print writer
     */
    public void printState(String iPrefix, String prefix, boolean printName,
			   PrintWriter out)
    {
	if (printName) {
	    if (iPrefix == null) iPrefix = EMPTY_STRING;
	    out.println(iPrefix + "\"" + getName() +"\" state"
			+" (class " + getClass().getName() +"):");
	}
	String state = isDeleted()? (deletePending()? "deletion pending":
				     "deleted"): "active";
	out.println(prefix +"simulation-object state: " +  state);
    }

    /**
     * Schedule a call.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0);
     * @param callable the Callable to run.
     * @return the event scheduled
     */
    protected TaskSimulationEvent scheduleCall(Callable callable) {
	return simulation.scheduleCall(this, callable, null);
    }

    /**
     * Schedule a call, adding a tag.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0);
     * @param callable the Callable to run.
     * @param tag a descriptive string for the Callable
     * @return the event scheduled
     */
    protected TaskSimulationEvent scheduleCall(Callable callable,
					    String tag) {
	return simulation.scheduleCall(this, callable, tag);
    }

    /**
     * Schedule a call, providing a delay.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the event scheduled
     *
     */
    protected TaskSimulationEvent scheduleCall(Callable callable, long delay)
    {
	return simulation.scheduleCall(this, callable, delay, null);
    }


    /**
     * Schedule a call, providing a delay and adding a tag.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tag a descriptive string for the Callable
     * @return the event scheduled
     *
     */
    protected TaskSimulationEvent scheduleCall(Callable callable, long delay,
					       String tag)
    {
	return simulation.scheduleCall(this, callable, delay, tag);
    }

    /**
     * Create a task thread but do not schedule it for execution.
     * @param runnable the Runnable used by the thread
     * @return the task thread
     */

    protected TaskThread unscheduledTaskThread(Runnable runnable) {
	return simulation.unscheduledTaskThread(runnable, this, (String)null);
    }

    /**
     * Create a task thread, adding a tag, but do not schedule it for execution.
     * @param runnable the Runnable used by the thread
     * @param tag a string describing a task thread
     * @return the task thread
     */

    protected TaskThread unscheduledTaskThread(Runnable runnable,
					       String tag) {
	return simulation.unscheduledTaskThread(runnable, this, tag);
    }

    /**
     * Schedule a Task.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * @param runnable the Runnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the thread that will run this task
     *
     */
    protected TaskThread scheduleTask(Runnable runnable, long delay)
    {
	return simulation.scheduleTask(runnable, delay, this, (String)null);
    }

    /**
     * Schedule a Task, adding a tag.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * @param runnable the Runnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tag a string describing a task thread
     * @return the thread that will run this task
     *
     */
    protected TaskThread scheduleTask(Runnable runnable, long delay,
				      String tag)
    {
	return simulation.scheduleTask(runnable, delay, this, tag);
    }

    /**
     * Start a simulation task with a delay of 0.
     * This is a convenience method that is equivalent to
     * scheduleTask(runnable, 0);
     * @param runnable the Runnable to execute.
     * @return the thread running this task
     */
    protected TaskThread scheduleTask(Runnable runnable)
    {
	return simulation.scheduleTask(runnable, this, (String)null);
    }

    /**
     * Start a simulation task with a delay of 0, adding a tag.
     * This is a convenience method that is equivalent to
     * scheduleTask(runnable, 0);
     * @param runnable the Runnable to execute.
     * @param tag a string describing a task thread
     * @return the thread running this task
     */
    protected TaskThread scheduleTask(Runnable runnable, String tag)
    {
	return simulation.scheduleTask(runnable, this, tag);
    }

    /**
     * Start a simulation task immediately, blocking the current task.
     * This may be called only while the simulation is running.
     * The task runs immediately, with the calling thread blocked,
     * until the task pauses or terminates.
     * @param runnable the Runnable to execute.
     * @return the thread running this task
    */
    protected TaskThread startImmediateTask(Runnable runnable)
	throws IllegalStateException
    {
	return simulation.startImmediateTask(runnable, this, (String)null);
    }

    /**
     * Start a simulation task immediately, blocking the current task and
     * adding a tag.
     * This may be called only while the simulation is running.
     * The task runs immediately, with the calling thread blocked,
     * until the task pauses or terminates.
     * @param runnable the Runnable to execute.
     * @param tag a string describing a task thread
     * @return the thread running this task
    */
    protected TaskThread startImmediateTask(Runnable runnable,
					    String tag)
	throws IllegalStateException
    {
	return simulation.startImmediateTask(runnable, this, tag);
    }

    /**
     * Bind a callable to the current object.
     * The use of this method allows simulation listeners to appropriately
     * attribute a callable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param callable the callable
     * @return a binding of the current object with the callable.
     */

    protected final SimObjectCallable bindCallable(Callable callable) {
	return new SimObjectCallable(this, callable);
    }

    /**
     * Bind a queue callable to the current object.
     * The use of this method allows simulation listeners to appropriately
     * attribute a callable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param callable the callable
     * @return a binding of the current object with the callable.
     */

    protected final <Server extends QueueServer>
    SimObjQueueCallable<Server> bindCallable(QueueCallable<Server> callable)
    {
	return new SimObjQueueCallable<Server>(this, callable);
    }

    /**
     * Bind a callable to the current object, providing a descriptive string.
     * The use of this method allows simulation listeners to appropriately
     * attribute a callable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param callable the callable
     * @param tag a descriptive string
     * @return a binding of the current object with the callable.
     */

    protected final SimObjectCallable bindCallable(Callable callable,
						   String tag)
    {
	return new SimObjectCallable(this, callable, tag);
    }


    /**
     * Bind a queue callable to the current object, providing a
     * descriptive string.
     * The use of this method allows simulation listeners to appropriately
     * attribute a callable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param callable the callable
     * @param tag a descriptive string
     * @return a binding of the current object with the callable.
     */

    protected final <Server extends QueueServer>
    SimObjQueueCallable bindCallable(QueueCallable<Server> callable,
				     String tag)
    {
	return new SimObjQueueCallable<Server>(this, callable, tag);
    }

    /**
     * Bind a runnable to the current object.
     * The use of this method allows simulation listeners to appropriately
     * attribute a runnable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param runnable the runnable
     * @return a binding of the current object with the runnable.
     */
    protected final SimObjectRunnable bindRunnable(Runnable runnable) {
	return new SimObjectRunnable(this, runnable);
    }


    /**
     * Bind a queue runnable to the current object.
     * The use of this method allows simulation listeners to appropriately
     * attribute a runnable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param runnable the queue runnable
     * @return a binding of the current object with the runnable.
     */
    protected final <Server extends QueueServer>
    SimObjQueueRunnable<Server> bindRunnable(QueueRunnable<Server> runnable)
    {
	return new SimObjQueueRunnable<Server>(this, runnable);
    }

    /**
     * Bind a runnable to the current object, providing a descriptive string.
     * The use of this method allows simulation listeners to appropriately
     * attribute a runnable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param runnable the runnable
     * @param tag a descriptive string
     * @return a binding of the current object with the runnable.
     */
    protected final SimObjectRunnable bindRunnable(Runnable runnable,
						   String tag)
    {
	return new SimObjectRunnable(this, runnable, tag);
    }


    /**
     * Bind a queue runnable to the current object, providing a
     * descriptive string.
     * The use of this method allows simulation listeners to appropriately
     * attribute a runnable to a simulation object when this would not
     * otherwise be possible.  It specifically supports task queues and
     * server queues.
     * @param runnable the runnable
     * @param tag a descriptive string
     * @return a binding of the current object with the runnable.
     */
    protected final <Server extends QueueServer>
    SimObjQueueRunnable<Server> bindRunnable(QueueRunnable<Server> runnable,
					     String tag)
    {
	return new SimObjQueueRunnable<Server>(this, runnable, tag);
    }


    private EvntListenerList listenerList = new EvntListenerList();


    /**
     * Add a simulation listener.
     * @param l the action listener to add.
     */

    public void addSimulationListener(SimulationListener l) {
	listenerList.add(SimulationListener.class, l);
    }

    /**
     * Remove a simulation listener.
     * @param l the action listener to remove.
     */
    public void removeSimulationListener(SimulationListener l) {
	listenerList.remove(SimulationListener.class, l);
    }

    /**
     * Get the event listener list.
     * All listeners in the list should be of type SimulationListener.
     * Callers should treat the list as a read-only one.
     * @return the listener list
     * @see org.bzdev.util.EvntListenerList
     */
    protected EvntListenerList getEventListenerList() {return listenerList;}

    void fireCallStart(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.CALL_START,
				 this, tevent.tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireCallEnd(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.CALL_END,
				 this, tevent.tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskStart(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.TASK_START,
				 tevent.thread.getOriginator(),
				 tevent.thread.getTag());
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskResume(TaskThreadSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.TASK_RESUME,
				 tevent.thread.getOriginator(),
				 tevent.thread.getTag());
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskPause(TaskThreadSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.TASK_PAUSE,
				 tevent.thread.getOriginator(),
				 tevent.thread.getTag());
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskEnd(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.TASK_END,
				 null,
				 tevent.thread.getTag());
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }
    void fireTaskEnd(TaskThreadSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.TASK_END,
				 tevent.thread.getOriginator(),
				 tevent.thread.getTag());
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskQueueStart(boolean restarting) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 (restarting?
				  SimulationStateEvent.Type.TASKQUEUE_RESUME:
				  SimulationStateEvent.Type.TASKQUEUE_START),
				 null, null);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireTaskQueueStop(boolean processing) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 (processing?
				  SimulationStateEvent.Type.TASKQUEUE_PAUSE:
				  SimulationStateEvent.Type.TASKQUEUE_STOP),
				 null, null);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener) listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireServerQueueSelectServer() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation,
				 SimulationStateEvent.Type.SERVER_SELECTED,
				 null, null);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }
    void fireSQServerInteraction(Object server, SimObject origin, String tag) {
	Object[] listeners = listenerList.getListenerList();
	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation, server,
				 SimulationStateEvent.Type.SQ_INTERACTION,
				 origin, tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }
    void fireSQCallable(Object server, SimObject origin, String tag) {
	Object[] listeners = listenerList.getListenerList();
	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation, server,
				 SimulationStateEvent.Type.SQ_CALLABLE,
				 origin, tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }
    void fireSQRunnable(Object server, SimObject origin, String tag) {
	Object[] listeners = listenerList.getListenerList();
	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation, server,
				 SimulationStateEvent.Type.SQ_RUNNABLE,
				 origin, tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireSQTask(Object server, SimObject origin, String tag) {
	Object[] listeners = listenerList.getListenerList();
	SimulationStateEvent event = new
	    SimulationStateEvent(this, simulation, server,
				 SimulationStateEvent.Type.SQ_TASK,
				 origin, tag);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)listeners[i+1]).stateChanged(event);
	    }
	}
    }

    LinkedHashSet<TraceSet> traceSets = null;

    /**
     * Get the trace sets currently in use.
     * @return an array containing the trace sets for this object
     */
    public TraceSet[] getTraceSets() {
	TraceSet[] array = new TraceSet[traceSets.size()];
	return traceSets.toArray(array);
    }


    /**
     * Determine if a trace set is being used.
     * @param traceSet the traceSet to check
     * @return true if traceSet is a member of the current object's
     *         set of trace sets; false otherwise
     */
    public boolean usesTraceSet(TraceSet traceSet) {
	if (traceSet == null || traceSets == null) return false;
	return traceSets.contains(traceSet);
    }

    /**
     * Add a trace set.
     * @param traceSet the trace set to add
     */
    public void addTraceSet(TraceSet traceSet) {
	if (traceSet == null) return;
	if (traceSets == null) traceSets = new LinkedHashSet<TraceSet>();
	traceSets.add(traceSet);
	traceSet.traceSet.add(this);
    }

    /**
     * Remove a trace set.
     * @param traceSet the trace set to remove
     */
    public void removeTraceSet(TraceSet traceSet) {
	if (traceSet == null) return;
	if (traceSets == null) return;
	traceSet.traceSet.remove(this);
	traceSets.remove(traceSet);
	if (traceSets.isEmpty()) traceSets = null;
    }

    /**
     * Remove all trace sets.
     */
    public void clearTraceSets() {
	if (traceSets != null) {
	    for (TraceSet tset: traceSets) {
		tset.traceSet.remove(this);
	    }
	    traceSets.clear();
	    traceSets = null;
	}
    }

    /**
     * Generate a trace message.
     * The message will be displayed for all trace sets added to this object
     * provided. For each trace set, there must be a defined output and
     * the level passed to this method must not be larger than the level
     * specified by the trace set. Each trace set can define an output,
     * with the simulation providing a default. Regardless, if the level
     * passed to this method is negative, nothing will be printed. If the
     * output and the simulation default output is null, nothing is printed.
     * <P>
     * Each call will print a formatted string starting with a tag delimited
     * by square brackets. This tag contains the following items:
     * <ul>
     *    <li> &lt;N&gt; where N is <code>level</code> argument.
     *    <li> the name of this object.
     *    <li> the current time in simulation-tick units.
     *    <li> the current time as a real number, surrounded by
     *         parentheses.
     * </ul>
     * <P>
     * A useful convention is to create a configuration class
     * for each package that makes use of this method. This class should
     * contain several static package-private fields such as
     * <blockquote><pre>
     *     static int level1 = -1;
     *     static int level2 = -1;
     * </pre></blockquote>
     * and two corresponding methods
     * <blockquote><pre>
     *      public static void setTraceLevels(int lev1, int lev2( P
     *         if (level1 &lt; -1 || level2 &lt; -1)
     *             throw new IllegalArgumentException(...)
     *         }
     *         level1 = lev1;
     *         level2 = lev2;
     *      }
     *
     *      public static
     *         &lt;T extends Enum&lt;T&gt;&gt;
     *         void setTraceLevels(T lev1, T lev2)
     *      {
     *         level1 = (level1 == null)? -1: lev1.ordinal();
     *         level2 = (level2 == null)? -1: lev2.ordinal();
     *      }
     * </pre></blockquote>
     * The second of these two methods is useful when one would
     * like to define trace levels using an enumeration to provide
     * better mnemonics for users of a package. Regardless, the
     * class or methods that set trace levels should include
     * documentation describing what is being traced at a given level.
     * @param level the trace level
     * @param format a format string
     * @param args the arguments printed according to the format
     *        string
     */
    protected void trace(int level, String format, Object... args) {
	if (traceSets == null) return;
	if (level < 0) return;
	for (TraceSet tset: traceSets) {
	    tset.trace(this, level, format, args);
	}
    }

    /**
     * Generate a trace message specifying the level via an enumeration
     * constant.
     * The message will be displayed for all trace sets added to this object
     * provided that the level argument is no larger than the level defined
     * for a given trace set.
     * For each trace set, there must be a defined output for an output to
     * be displaced. Each trace set can define an output,
     * with the simulation providing a default. If all alternative outputs
     * are null, nothing will be printed.
     * <P>
     * This method is provided so that one can create an enum to represent
     * debugging levels, thus giving them symbolic names. It is equivalent
     * to calling {@link #trace(int,String, Object...)} with its first
     * argument set to the ordinal value of the enumeration.  The disadvantage
     * is that the enumeration must be known at compile time and as a result,
     * one cannot configure trace levels at run time (configuring at run time
     * is useful for when independently written class libraries are used).
     * <P>
     * Each call will print a formatted string starting with a tag delimited
     * by square brackets. This tag contains the following items:
     * <ul>
     *    <li> &lt;N&gt; where N is the ordinal value of the
     *         <code>level</code> argument.
     *    <li> the name of this object.
     *    <li> the current time in simulation-tick units.
     *    <li> the current time as a real number, surrounded by
     *         parentheses.
     * </ul>
     * @param level an enum whose ordinal value provides the trace level;
     *        null if nothing is to be printed
     * @param format a format string
     * @param args the arguments printed according to the format
     *        string
     */
    protected void trace(Enum<?> level, String format, Object... args) {
	if (traceSets == null) return;
	if (level == null) return;
	int olevel = level.ordinal();
	for (TraceSet tset: traceSets) {
	    tset.trace(this, olevel, format, args);
	}
    }
}

//  LocalWords:  sim exbundle SimObjectHelper SimulationHelper args
//  LocalWords:  SimObject ScriptException scriptingNotEnabled anim
//  LocalWords:  UnsupportedOperationException functionName namespace
//  LocalWords:  scriptObject methodName SimObject's noScriptEngine
//  LocalWords:  IllegalArgumentException reservedName pauseTask ul
//  LocalWords:  scheduleScript needTaskThread taskThread getObject
//  LocalWords:  makeScriptingThread scriptBindings scriptEngine li
//  LocalWords:  getDefaultBindings scheduleCallObject superclass lt
//  LocalWords:  getScriptEngine simtime printConfiguration boolean
//  LocalWords:  Javadoc printState printName whitespace Subclassses
//  LocalWords:  reportState PrintStream scheduleCall scheduleTask
//  LocalWords:  SimulationListener traceSet blockquote pre lev Enum
//  LocalWords:  setTraceLevels enum PrintWriter param inheritDoc
//  LocalWords:  frasl javadoc iPrefix
