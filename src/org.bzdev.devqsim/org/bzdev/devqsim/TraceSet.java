package org.bzdev.devqsim;
import org.bzdev.io.AppendableWriter;
import org.bzdev.lang.StackTraceModePermission;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Set of properties for tracing simulation objects
 * This class determines the output for tracing and a trace level to
 * aid in debugging. The output can be any Appendable (the Appendable
 * interface is implements by System.out, etc.) Simulation objects
 * that are to be traced can add a trace set to a list (actually a
 * set as a given trace set can appear only once), and when
 * {@link SimObject#trace(int,String,Object...)} or
 * {@link SimObject#trace(Enum,String,Object...)} is called,
 * each of the simulation's objects' trace sets will contain the
 * traced output. The class {@link org.bzdev.swing.SimpleConsole} implements
 * the {@link java.lang.Appendable} interface and can be used to
 * put trace output in a console window when a GUI is provided.
 * <P>
 * A print level determines what outputs are shown: objects will call
 * a method named {@link SimObject#trace} that takes a level as its
 * argument.  If that level exceeds the level for a trace set, nothing
 * will be printed for that trace set.  A simulation object can be
 * members of multiple trace sets.  When this is done the trace sets
 * should use different outputs. For example, one can trace a relatively
 * large number of objects with a low trace level and a smaller subset
 * with a high trace level.
 * <P>
 * When {@link SimObject#trace} is called, a formatted message will appear
 * on the output, prefixed by the object name and the current time, and
 * terminated by a newline.  Optionally a stack trace can be added (one must
 * call {@link #setStackTraceMode(boolean)} with an argument equal to
 *  <code>true</code> to turn this capability on).  The stack trace is
 * useful in cases were the trace shows anomalous behavior and the
 * calling sequence would be useful in debugging the problem.
 * <P>
 * A simulation object is added to a trace set by calling the
 * simulation object's method {@link SimObject#addTraceSet(TraceSet)} and
 * removed by calling {@link SimObject#removeTraceSet(TraceSet)}.
 */
public class TraceSet extends DefaultSimObject {
    Simulation sim;

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
    public TraceSet(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
    }

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    @Override
	protected void onDelete() {
	for (SimObject obj: traceSet) {
	    obj.removeTraceSet(this);
	}
	super.onDelete();
    }

    PrintWriter out = null;
    int level = 0;

    /**
     * Set the trace level.
     * @param level the trace level
     */
    public void setLevel(int level) {
	this.level = level;
    }

    /**
     * Get the trace level.
     * @return the trace level
     */
    public int getLevel() {return level;}


    private boolean stacktraceMode = false;
    private int stacktraceLimit = 0;

    /**
     * Set the stacktrace mode.
     * <P>
     * When the scrunner command is used, it should be run with the
     * option --trustLevel=1 or --trustLevel=2. When the trust level is 1,
     * the -t option is also needed for any script that modifies stack-trace
     * mode. For the default trust level, the -t option is also needed but
     * one must also grant the permission
     * {@link org.bzdev.lang.StackTraceModePermission} for
     * "{@link org.bzdev.devqsim.TraceSet}".  Alternatively,
     * one may use a trusted script to call the simulation method
     * {@link Simulation#allowSetStackTraceMode(boolean,boolean)}.
     * @param mode true if a stack trace should be printed when trace
     *       output is displayed; false otherwise
     * @exception SecurityException a security manager was installed
     *            and the permission
     *            org.bzdev.lang.StackTraceModePermission was not
     *            granted for class org.bzdev.devqsim.TraceSet
     */
    public void setStackTraceMode(boolean mode) {
	/*
	if (!sim.allowsSetStackTraceModeTS()) {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new StackTraceModePermission
				   ("org.bzdev.devqsim.TraceSet"));
	    }
	}
	*/
	stacktraceMode = mode;
    }

    /**
     * Set the stack-trace limit.
     * The stack-trace limit is the maximum number of stack frames that
     * will be printed.
     * @param limit 0 if there is no limit; otherwise a positive number
     *        giving value of the limit
     * @exception IllegalArgumentException the argument was negative
     */
    public void setStackTraceLimit(int limit) throws IllegalArgumentException {
	if (limit < 0) {
	    String lim = "" + limit;
	    throw new IllegalArgumentException(errorMsg("argNegative", lim));
	    // throw new IllegalArgumentException("argument negative");
	}
	stacktraceLimit = limit;
    }

    /**
     * Get the stack-trace mode.
     * @return the stacktrace mode (true if a stacktrace will
     *         be displayed; false if not)
     */
    public boolean getStackTraceMode() {return stacktraceMode;}

    /**
     * Get the stack-trace limit.
     * The stack-trace limit is the maximum number of stack frames that
     * will be printed.
     * @return 0 if there is no limit; otherwise the value of the limit
     */
    public int getStackTraceLimit() {return stacktraceLimit;}

    /**
     * Set the per trace set output.
     * @param appendable the appendable to use for output; null if
     *        the simulation default should be used
     */
    public void setOutput(Appendable appendable) {
	if (appendable == null) {
	    out = null;
	} else if (appendable instanceof PrintWriter) {
	    out = (PrintWriter) appendable;
	} else if (appendable instanceof Writer) {
	    out = new PrintWriter((Writer)appendable);
	} else {
	    out = new PrintWriter(new AppendableWriter(appendable));
	}
    }

    Set<SimObject> traceSet = new LinkedHashSet<SimObject>();

    // used by SimObject
    void trace(SimObject object, int level, String format, Object... args) {
	if (!sim.tracingEnabled) return;
	if (level > this.level) return;
	PrintWriter output = (out == null)? sim.traceOut: out;
	if (output == null) return;
	String pf;
	if (object.isInterned()) {
	    pf = "[<%d> %s @ %d (%g)]: ";
	} else {
	    pf = "[<%d> <<%s>> @ %d (%g)]: ";
	}
	String prefix = String.format(pf, level, object.getName(),
				      sim.currentTicks(),
				      sim.currentTime());
	output.format(prefix + format + "\n", args); 
	if (stacktraceMode) {
	    StackTraceElement[] stacktrace =
		Thread.currentThread().getStackTrace();
	    // ignore this element and the caller, which is
	    // a call inside SimObject's trace method. These
	    // two lines will always be the same.
	    int lim = (stacktraceLimit == 0)? stacktrace.length:
		3 + stacktraceLimit;
	    if (lim > stacktrace.length) lim = stacktrace.length;
	    for (int i = 3; i < lim; i++) {
		output.format("       \"%s\", %d: called from %s#%s\n",
			      stacktrace[i].getFileName(),
			      stacktrace[i].getLineNumber(),
			      stacktrace[i].getClassName(),
			      stacktrace[i].getMethodName());
	    }
	}
    }
}

//  LocalWords:  exbundle Appendable SimObject setStackTraceMode sim
//  LocalWords:  boolean addTraceSet TraceSet removeTraceSet Enum
//  LocalWords:  IllegalArgumentException getObject stacktrace
//  LocalWords:  argNegative appendable SimObject's scrunner
//  LocalWords:  trustLevel allowSetStackTraceMode SecurityException
