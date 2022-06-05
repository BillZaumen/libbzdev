package org.bzdev.devqsim;

import org.bzdev.scripting.ScriptingContext;
import org.bzdev.lang.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.EvntListenerList;

import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.bzdev.io.AppendableWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.*;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Bindings;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Top level class for a simulation.
 * This class models the flow of time, handles scheduling of events,
 * and provides tables that allow simulation objects to be looked up by
 * name.  Subclasses provide additional functionality.
 */

@ObjectNamer(helperClass = "SimulationHelper",
	     helperSuperclass = "org.bzdev.scripting.ScriptingContext",
	     objectClass = "SimObject",
	     objectHelperClass="SimObjectHelper",
	     helperSuperclassConstrTypes = {
		 @ObjectNamer.ConstrTypes(
			  value ={"org.bzdev.scripting.ScriptingContext"},
			  exceptions = {"SecurityException"})
	     })
public class Simulation extends SimulationHelper
    implements ObjectNamerOps<SimObject>
{
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.devqsim.lpack.Simulation");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    class State {
	// PriorityQueue<SimulationEvent> pq = null;
	SimulationEventQueue pq = null;
	SimulationEventQueue initq = null;
	long  currentTicks = 0;
	double currentPriority = 0.0;
	long nextInstance = 0;
	long nextInitInstance = 0;
	boolean simulationRunning = false;
    }

    State state;
    private Simulation parent = null;
    double ticksPerUnitTime = 1.0;

    /**
     * Convert a time or time interval to ticks.
     * The value returned will be rounded to the closest long integer.
     * @param time the time or interval in unit-time units
     * @return the corresponding number of ticks.
     */
    public long getTicks(double time) {
	return Math.round(time*ticksPerUnitTime);
    }

    /**
     * Convert a time or time interval to ticks, rounding up.
     * The value returned will be rounded to the closest long integer
     * equal to or larger than the argument after the conversion.
     * @param time the time or interval in unit-time units
     * @return the corresponding number of ticks.
     */
    public long getTicksCeil(double time) {
	return Math.round(Math.ceil(time*ticksPerUnitTime));
    }

    /**
     * Convert a time or time interval to ticks, rounding down.
     * The value returned will be rounded down to the closest long integer
     * equal to or smaller than the argument after the conversion.
     * @param time the time or interval in unit-time units
     * @return the corresponding number of ticks.
     */
    public long getTicksFloor(double time) {
	return Math.round(Math.floor(time*ticksPerUnitTime));
    }

    /**
     * Get the time in the simulation's time units given the time in ticks.
     * @param ticks the number of ticks
     * @return the time
     */
    public double getTime(long ticks) {
	return ((double)ticks)/ticksPerUnitTime;
    }


    boolean stackTraceMode = false;

    boolean allowSSTMode = false;
    boolean allowSSTModeTS = false;
    /**
     * Configure whether or not {@link #setStackTraceMode(boolean)} checks
     * permissions.
     * This method also controls the use of
     * {@link TraceSet#setStackTraceMode(boolean)} for those trace sets
     * associated with this simulation.
     * @param allow true if calls to {@link #setStackTraceMode(boolean)} cannot
     *        throw a security exception; false otherwise
     * @param tsallow true if calls to
     *        {@link TraceSet#setStackTraceMode(boolean)} for trace sets
     *        associated with this simulation cannot
     *        throw a security exception; false otherwise
     * @exception SecurityException a security manager was installed
     *            and the permission
     *            org.bzdev.lang.StackTraceModePermission was not
     *            granted for the class org.bzdev.devqsim.Simulation,
     *            and for the class org.bzdev.devqsim.TraceSet when
     *            tsallow has the value <code>true</code>
     */
    public void allowSetStackTraceMode(boolean allow, boolean tsallow) {
	/*
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new StackTraceModePermission
			       ("org.bzdev.devqsim.Simulation"));
	    if (tsallow) sm.checkPermission(new StackTraceModePermission
					    ("org.bzdev.devqsim.TraceSet"));
	}
	*/
	allowSSTMode = allow;
	allowSSTModeTS = tsallow;
    }

    /**
     * Determine if the simulation is configured so that
     * calls to {@link #setStackTraceMode(boolean)} or
     * {@link TraceSet#setStackTraceMode(boolean)} for trace sets
     * associated with this simulation cannot throw a security exception.
     * @return true if a security exception cannot be thrown; false otherwise
     */
    public boolean allowsSetStackTraceMode() {
	if (allowSSTMode) return true;
	else {
	    return false;
	}
    }

    /**
     * Determine if the simulation is configured so that
     * calls to
     * {@link TraceSet#setStackTraceMode(boolean)} for trace sets
     * associated with this simulation cannot throw a security exception.
     * @return true if a security exception cannot be thrown; false otherwise
     */
    public boolean allowsSetStackTraceModeTS() {
	if (allowSSTModeTS) return true;
	else {
	    return false;
	}
    }

    /**
     * Set stack-trace mode.
     * When stack-trace mode is on, certain events are tagged with the
     * stack trace at the point the event was created. At each point
     * in simulation time, the method
     * {@link #getEventStackTrace() getEventStackTrace()} will return
     * a StackTraceElement array containing the stack trace.
     * This option is useful for modest-sized simulations for debugging
     * purposes.
     * <P>
     * When the scrunner command is used, it should be run with the
     * option --trustLevel=1 or --trustLevel=2. When the trust level is 1,
     * the -t option is also needed for any script that modifies stack-trace
     * mode. For the default trust level, the -t option is also needed but
     * one must also grant the permission
     * {@link org.bzdev.lang.StackTraceModePermission} for
     * "{@link org.bzdev.devqsim.Simulation}".
     * @param mode true to turn stack-trace mode on; false to turn it off
     * @exception SecurityException a security manager was installed
     *            and the permission
     *            org.bzdev.lang.StackTraceModePermission was not
     *            granted for class org.bzdev.devqsim.Simulation
     */
    public void setStackTraceMode(boolean mode) {
	/*
	if (!allowSSTMode) {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new StackTraceModePermission
				   ("org.bzdev.devqsim.Simulation"));
	    }
	}
	*/
	stackTraceMode = mode;
    }

    /**
     * Get the stack-trace mode.
     * When stack-trace mode is on, certain events are tagged with the
     * stack trace at the point the event was created. At each point
     * in simulation time, the method
     * {@link #getEventStackTrace() getEventStackTrace()} will return
     * a StackTraceElement array containing the stack trace.
     * This option is useful for modest-sized simulations for debugging
     * purposes.
     * @return true if stack-trace mode is on; false if it is off
     */
    public boolean getStackTraceMode() {
	return stackTraceMode;
    }

    StackTraceElement[] eventStackTrace = null;

    /**
     * Get the stack trace in effect when the current event was created.
     * @return an array containing the stack trace
     */
    public StackTraceElement[] getEventStackTrace() {
	return eventStackTrace;
    }


    /**
     * Constructor.
     */

    public Simulation() {
	this((Simulation)null);
    }

    /**
     * Constructor with a parent simulation or scripting context.
     * When a simulation has a parent, the simulation shares the
     * parent's scripting context.  In addition, if the parent is a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them. In addition, the parent simulation will be added
     * an an alternative object namer, so that the set/add methods of
     * factory classes can find objects are defined by a parent
     * simulation.  The value returned by
     * {@link #getTicksPerUnitTime() getTicksPerUnitTime()} will be that of
     * the parent simulation if the parent is not null or the default
     * value of 1.0.
     *
     * @param parent the simulation's parent; null if there is none
     */
    public Simulation (ScriptingContext parent) {
	super(parent);
	if (parent == null || !(parent instanceof Simulation)) {
	    state = new State();
	    state.pq = new SimulationEventQueue();
	    state.initq = new SimulationEventQueue();
	} else {
	    Simulation parentAsSim = (Simulation)parent;
	    state = parentAsSim.state;
	    ticksPerUnitTime = parentAsSim.ticksPerUnitTime;
	    this.parent = parentAsSim;
	    addObjectNamer(parentAsSim);
	}
    }

    /**
     * Constructor with a parent simulation or scripting context and
     * with a time-unit specification.
     * When a simulation has a parent, the simulation shares the
     * parent's scripting context.  In addition, if the parent is a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them. In addition, the parent simulation will be added
     * an an alternative object namer, so that the set/add methods of
     * factory classes can find objects are defined by a parent
     * simulation.
     *
     * @param parent the simulation's parent; null if there is none
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    public Simulation (ScriptingContext parent, double ticksPerUnitTime) {
	super(parent);
	if (parent == null || !(parent instanceof Simulation)) {
	    state = new State();
	    state.pq = new SimulationEventQueue();
	    state.initq = new SimulationEventQueue();
	} else {
	    Simulation parentAsSim = (Simulation)parent;
	    state = parentAsSim.state;
	    this.parent = parentAsSim;
	    addObjectNamer(parentAsSim);
	}
	this.ticksPerUnitTime = ticksPerUnitTime;
    }


    /**
     * Constructor give a time-unit specification.
     * @param ticksPerUnitTime the number of ticks per unit time when
     *        time is provided as a double-precision number
     */
    public Simulation(double ticksPerUnitTime) {
	this((Simulation) null, ticksPerUnitTime);
    }


    /**
     * Get the simulation's parent.
     * The parent is the simulation passed as an argument in
     * a constructor.
     * @return the simulation's parent; null if there is none.
     * @see org.bzdev.devqsim.Simulation#Simulation(ScriptingContext)
     */
    public Simulation getParent() {
	return parent;
    }

    /**
     * Get the current simulation time in tick units.
     * @return the current simulation time
     */
    public final long currentTicks() {return state.currentTicks;}

    /**
     * @deprecated Replaced by {@link #currentTicks()}
     */
    @Deprecated
    public final long getCurrentTime() {return state.currentTicks;}


    /**
     * Get the current simulation time in real-valued time units.
     * The value is the value returned by
     * {@link #getCurrentTime() getCurrentTime()} divided by the value
     * returned by {@link #getTicksPerUnitTime() getTicksPerUnitTime()}.
     * @return the current simulation time
     */
    public final double currentTime() {
	return state.currentTicks / ticksPerUnitTime;
    }

    /**
     * Get the change in the current simulation time from an initial value.
     * This is provided to eliminate round-off errors from computing the
     * difference between large floating-point numbers.
     * @param ticks the initial simulation-time value
     * @return the difference in time in real-valued time units
     */
    public final double currentTimeFrom(long ticks) {
	return (state.currentTicks - ticks) / ticksPerUnitTime;
    }

    /**
     * Get the number of ticks per unit time.
     * @return the number of ticks per unit time
     */
    public final double getTicksPerUnitTime() {
	return ticksPerUnitTime;
    }


    /**
     * Get the priority for the current event at the current time.
     * @return the priority; 0.0 if none has been set.
     */
    public final double getCurrentTimePriority() {
	return state.currentPriority;
    }

    /**
     * Check if there are any pending events.
     * @return true if events have been scheduled but not yet run;
     *         false otherwise
     */
    public final boolean moreEventsScheduled() {
	return (state.pq.peek() != null);
    }

    /**
     * Get the interval to the next scheduled event.
     * The value returned is that maximum interval given the current
     * state of the simulation by which the current simulation time
     * may be advanced.
     * @return the time for the next scheduled event, or Integer.MAX_VALUE
     *         time if no event next event is scheduled
     */
    public final long getNextEventInterval() {
	SimulationEvent pqe = state.pq.peek();
	if (pqe == null) {
	    return Long.MAX_VALUE;
	} else {
	    return (pqe.time - state.currentTicks);
	}
    }

    /**
     * Advance Simulation time.
     * This method increments the current simulation time by at most
     * the number of ticks given by its argument.  It will not advance the
     * current simulation time beyond the time at which the next scheduled
     * event is scheduled. If there is no scheduled event, the simulation
     * time will be advanced by the number of ticks given by this method's
     * argument.
     * @param interval the time interval by which to advance the current
     *        simulation time
     * @return the amount of time by which the current simulation time
     *         was actually advanced
     */
    public final long advance(long interval) {
	SimulationEvent pqe = state.pq.peek();
	if (pqe != null) {
	    if (interval > (int)(pqe.time - state.currentTicks)) {
		interval = (int)(pqe.time - state.currentTicks);
	    }
	}
	state.currentTicks += interval;
	state.currentPriority = 0.0;
	return interval;
    }

    /**
     * Schedule an event given a delay.
     * Typically used in the implementation of core classes, not called by
     * user code unless a user defines new types of events.
     * @param event the event to schedule
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until an event occurs
     * @return the event scheduled
     */
    public final SimulationEvent scheduleEvent(SimulationEvent event,
					       long delay)
    {
	long time = state.currentTicks + delay;
	long instance = state.nextInstance++;
	event.simulation = this;
	event.time = time;
	event.tpriority = 0.0;
	event.instance = instance;
	state.pq.add(event);
	event.pending = true;
	if (event.source == null) event.source = this;
	if (stackTraceMode && event.stackTraceArray == null) {
	    event.stackTraceArray = AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});

	}
	return event;
    }

    /**
     * Schedule an event given a delay and a priority.
     * Typically used in the implementation of core classes, not called by
     * user code unless a user defines new types of events.
     * @param event the event to schedule
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until an event occurs
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     */
    public final SimulationEvent scheduleEvent(SimulationEvent event,
					       long delay, double tpriority)
    {
	long time = state.currentTicks + delay;
	long instance = state.nextInstance++;
	event.simulation = this;
	event.time = time;
	event.tpriority = tpriority;
	event.instance = instance;
	state.pq.add(event);
	event.pending = true;
	if (event.source == null) event.source = this;
	if (stackTraceMode && event.stackTraceArray == null) {
	    event.stackTraceArray = AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}
	return event;
    }

    final SimulationEvent scheduleInitEvent(SimulationEvent event,
					    long priority)
    {
	event.time = priority;	// time is a misnomer, but we are reusing
	                        // the event classes and the priority queue
	event.tpriority = 0.0;
	long instance = state.nextInitInstance++;
	event.simulation = this;
	state.initq.add(event);
	event.pending = true;
	return event;
    }

    /**
     * Deschedule (cancel) an event.
     * @param pqentry the simulation event to deschedule
     * @return true on success; false on failure
     */
    public final  boolean descheduleEvent(SimulationEvent pqentry) {
	boolean result = state.pq.remove(pqentry);
	if (result && pqentry != null) {
	    pqentry.pending = false;
	}
	return result;
    }

    /**
     * Reschedule a simulation event.
     * This method will fail if an event is not currently scheduled or if
     * an attempt is made to schedule it at a previous point in simulation
     * time.
     * @param event the event
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until an event occurs
     * @return true on success; false on failure
     */
    public final boolean rescheduleEvent(SimulationEvent event, long delay)
    {
	if (state.pq.remove(event)) {
	    event.pending = false;
	    return (scheduleEvent(event, delay) == event);
	} else {
	    return false;
	}
    }

    /**
     * Reschedule a simulation event given a delay and a priority.
     * @param event the event
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until an event occurs
     * @param tpriority the priority of the event at the time it is scheduled
     * @return true on success; false on failure
     */
    public final boolean rescheduleEvent(SimulationEvent event, long delay,
					 double tpriority)
    {
	if (state.pq.remove(event)) {
	    event.pending = false;
	    return (scheduleEvent(event, delay, tpriority) == event);
	} else {
	    return false;
	}
    }


    /**
     * Schedule a call.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0).
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @return the event scheduled
     */

    public TaskSimulationEvent scheduleCall(Callable callable) {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	return (TaskSimulationEvent) scheduleEvent(event, 0);
    }

    /**
     * Schedule a call with a priority.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0, tpriority).
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param tpriority the event priority at the current time
     * @return the event scheduled
     */

    public TaskSimulationEvent
	scheduleCallWP(Callable callable, double tpriority)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	return (TaskSimulationEvent) scheduleEvent(event, 0, tpriority);
    }


    /**
     * Schedule a call specified by a SimObjectCallable.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0).
     * @param soc the SimObjectCallable to run.
     * @return the event scheduled
     */

    public TaskSimulationEvent scheduleCall(SimObjectCallable soc) {
	TaskObjectSimEvent event = new TaskObjectSimEvent(soc.getCallable(),
							  soc.getTag());
	event.source = soc.getSource();
	return (TaskSimulationEvent) scheduleEvent(event, 0);
    }

    /**
     * Schedule a call specified by a SimObjectCallable and given a priority.
     * The <code>call</code> method of a Callable will be run at the
     * current simulation time after the current event completes.
     * Equivalent to scheduleCall(callable, 0).
     * @param soc the SimObjectCallable to run.
     * @param tpriority the event priority at the current time
     * @return the event scheduled
     */

    public TaskSimulationEvent scheduleCallWP(SimObjectCallable soc,
					      double tpriority)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(soc.getCallable(),
							  soc.getTag());
	event.source = soc.getSource();
	return (TaskSimulationEvent) scheduleEvent(event, 0, tpriority);
    }


    // provides data needed for simulation state events when the call is
    // tagged.
    TaskSimulationEvent scheduleCall(Object source, Callable callable,
				     String tag)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable, tag);
	event.source = source;
	return (TaskSimulationEvent) scheduleEvent(event, 0);
    }

    // provides data needed for simulation state events when the call is
    // tagged.
    TaskSimulationEvent scheduleCall(Object source, Callable callable,
				     double priority,
				     String tag)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable, tag);
	event.source = source;
	return (TaskSimulationEvent) scheduleEvent(event, 0);
    }



    /**
     * Schedule a call providing a delay.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the event scheduled
     *
     */

    public TaskSimulationEvent scheduleCall(Callable callable, long delay ) {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	return (TaskSimulationEvent) scheduleEvent(event, delay);
    }

    TaskSimulationEvent scheduleCall(Callable callable, long delay,
				     StackTraceElement[] stacktrace)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	((SimulationEvent) event).stackTraceArray = stacktrace;
	return (TaskSimulationEvent) scheduleEvent(event, delay);
    }


    /**
     * Schedule a call providing a delay and priority.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     *
     */

    public TaskSimulationEvent scheduleCall(Callable callable, long delay,
					    double tpriority)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
    }

    /**
     * Schedule a script providing a delay.
     * @param script the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script is executed
     * @return the event scheduled
     *
     */

    public TaskSimulationEvent scheduleScript(String script, long delay)
	throws UnsupportedOperationException
    {
	return scheduleScript(script, delay, null);
    }

    /**
     * Schedule a script providing a delay and priority.
     * @param script the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script is executed
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     *
     */

    public TaskSimulationEvent scheduleScript(String script, long delay,
					      double tpriority)
	throws UnsupportedOperationException
    {
	return scheduleScript(script, delay, tpriority, null);
    }


    TaskSimulationEvent scheduleScript(final String script, long delay,
				       final Bindings bindings)
	throws UnsupportedOperationException
    {
	return scheduleScript(script, delay, 0.0, bindings);
    }

    TaskSimulationEvent scheduleScript(final String script, long delay,
				       double tpriority,
				       final Bindings bindings)
	throws UnsupportedOperationException
    {
	if (!hasScriptEngine())
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	try {
	    // onExecutionStarting();
	    Callable callable = new Callable() {
		    public void call() {
			try {
			    evalScript(script, bindings);
			} catch (ScriptException e) {
			    throw new RuntimeException(e);
			}
		    }
		};

	    TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	    return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
	} finally {
	    // onExecutionEnding();
	}
    }

    static final Object[] emptyArgs = new Object[0];

    /**
     * Schedule a script object providing a delay.
     * The script object's <code>call()</code> method will be run when
     * the event is processed.
     * @param scriptObject the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script object's
     *              <code>call()</code> method is executed
     * @return the event scheduled
     *
     */
    public TaskSimulationEvent scheduleCallObject(Object scriptObject,
						  long delay)
	throws UnsupportedOperationException
    {
	return scheduleCallObject(scriptObject, delay, null);
    }

    /**
     * Schedule a script object providing a delay and priority.
     * The script object's <code>call()</code> method will be run when
     * the event is processed.
     * @param scriptObject the script to execute
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the script object's
     *              <code>call()</code> method is executed
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     *
     */
    public TaskSimulationEvent scheduleCallObject(Object scriptObject,
						  long delay, double tpriority)
	throws UnsupportedOperationException
    {
	return scheduleCallObject(scriptObject, delay, tpriority,
				  null);
    }

    TaskSimulationEvent
	    scheduleCallObject(final Object scriptObject, long delay,
			       final Bindings bindings)
	throws UnsupportedOperationException
    {
	return scheduleCallObject(scriptObject, delay, 0.0, bindings);
    }

    TaskSimulationEvent
	    scheduleCallObject(final Object scriptObject,
			       long delay, double tpriority,
			       final Bindings bindings)
	throws UnsupportedOperationException
    {
	if (!hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	final Object simvar = (bindings == null)? null: bindings.get("sim");
	final Object selfvar = (bindings == null)? null: bindings.get("self");
	Callable callable = new Callable() {
		public void call() {
		    try {
			if (simvar != null) bindings.put("sim", simvar);
			if (selfvar != null) bindings.put("self", selfvar);
			callScriptMethod(bindings, scriptObject, "call");
		    } catch (NoSuchMethodException e) {
			throw new RuntimeException(new ScriptException(e));
		    } catch (ScriptException ee) {
			throw new RuntimeException(ee);
		    }
		}
	    };

	TaskObjectSimEvent event = new TaskObjectSimEvent(callable);
	return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
    }


    /**
     * Schedule a call providing a delay and with the simulation paused.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.  The simulation will be put in a
     * state in which it is paused (e.g. not running) while the
     * scheduled call is being processed, so any calls put on the
     * initialization queue will be processed when the simulation is
     * restarted and before the next normal event is processed.
     * Simulation listeners will be notified that the simulation has
     * paused and restarted.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the event scheduled
     *
     */

    public TaskSimulationEvent scheduleCallPaused(final Callable callable,
						  long delay)
    {
	return scheduleCallPaused(callable, delay, 0.0);
    }


    /**
     * Schedule a call providing a delay and priority with the simulation
     * paused.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.  The simulation will be put in a
     * state in which it is paused (e.g. not running) while the
     * scheduled call is being processed, so any calls put on the
     * initialization queue will be processed when the simulation is
     * restarted and before the next normal event is processed.
     * Simulation listeners will be notified that the simulation has
     * paused and restarted.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     *
     */
    public TaskSimulationEvent scheduleCallPaused(final Callable callable,
						  long delay, double tpriority)
    {
	Callable wrapper = new Callable() {
		public void call() {
		    try {
			state.simulationRunning = false;
			fireSimulationEnding();
			if (parent == null) {
			    callable.call();
			} else {
			    doScriptPrivileged(callable);
			}
		    } finally {
			runInitq();
			fireSimulationStarting();
			state.simulationRunning = true;
		    }
		}
	    };

	TaskObjectSimEvent event = new TaskObjectSimEvent(wrapper);
	return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
    }


    /**
     * Schedule a call specified by a SimObjectCallable, providing a delay.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.  A SimObjectCallable allows the call to
     * be annotated with data accessible in simulation state events (used to
     * monitor or instrument a simulation).
     * @param soc the SimObjectCallable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the event scheduled
     */
    public TaskSimulationEvent scheduleCall(SimObjectCallable soc, long delay) {
	TaskObjectSimEvent event = new TaskObjectSimEvent(soc.getCallable(),
							  soc.getTag());
	event.source = soc.getSource();
	return (TaskSimulationEvent) scheduleEvent(event, delay);
    }

    /**
     * Schedule a call specified by a SimObjectCallable, providing a delay and
     * priority.
     * The <code>call</code> method of a Callable will be run at a
     * particular simulation time.  A SimObjectCallable allows the call to
     * be annotated with data accessible in simulation state events (used to
     * monitor or instrument a simulation).
     * @param soc the SimObjectCallable to run.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the event scheduled
     */
    public TaskSimulationEvent scheduleCall(SimObjectCallable soc,
					    long delay, double tpriority)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(soc.getCallable(),
							  soc.getTag());
	event.source = soc.getSource();
	return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
    }


    // used by SimObject so we can make the source the object that
    // created the call.
    TaskSimulationEvent scheduleCall(Object source,
				     Callable callable, long delay,
				     String tag)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable, tag);
	event.source = source;
	return (TaskSimulationEvent) scheduleEvent(event, delay);
    }

    TaskSimulationEvent scheduleCall(Object source,
				     Callable callable,
				     long delay, double tpriority,
				     String tag)
    {
	TaskObjectSimEvent event = new TaskObjectSimEvent(callable, tag);
	event.source = source;
	return (TaskSimulationEvent) scheduleEvent(event, delay, tpriority);
    }

    /**
     * Schedule an initialization call.
     * The <code>call</code> method of the Callable argument will be
     * invoked when a <code>run</code> method of the simulation is
     * called and before any scheduled simulation events are executed.
     * This is intended for use by classes such as factory classes that
     * need to schedule some further initialization events after other
     * factories have had a chance to create their objects.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Callable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Callable.
     * @param callable the Callable to run.
     * @param priority the priority setting the order in which each
     *        Callable's call method is run, with lower values run
     *        first
     * @return the event scheduled
     *
     */
    public TaskSimulationEvent scheduleInitCall(Callable callable,
						int priority) {
	return (TaskSimulationEvent)
	    scheduleInitEvent(new TaskObjectSimEvent(callable), (long)priority);
    }


    /**
     * Create a task thread but do not schedule it for execution.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param runnable the Runnable used by the thread
     * @return the task thread
     */

    public TaskThread unscheduledTaskThread(final Runnable runnable) {
	final TaskThread taskThread = new TaskThread(this, new Runnable() {
		public void run() {
		    try {
			runnable.run();
		    } catch (TaskThread.CancelException e) {
		    }
		    TaskThread thread = (TaskThread)(Thread.currentThread());
		    synchronized(thread.schedMonitor) {
			thread.schedPaused = false;
			//System.out.println("schedPaused false 1 " +thread.getOurId());
			thread.schedMonitor.notifyAll();
		    }
		}
	    });
	Callable callable = new Callable() {
		public void call() {
		    taskThread.start();
		    boolean interrupted = false;
		    synchronized(taskThread.schedMonitor) {
			taskThread.schedCount++;
			//System.out.println("sched wait 5 " + taskThread.getOurId());
			while (taskThread.schedPaused) {
			    try {
				taskThread.schedMonitor.wait();
			    } catch (InterruptedException e) {
				//System.out.println("sched wait 5 (interrupt) " +taskThread.getOurId());
				interrupted = true;
			    }
			}
		    }
		    // System.out.println("sched wait 5 done " + taskThread.getOurId());
		    taskThread.schedCount--;

		    if (interrupted) Thread.currentThread().interrupt();
		}
	    };
	TaskObjectSimEvent event =
	    new TaskObjectSimEvent(callable, taskThread);
	event.source = this;
	if (stackTraceMode) {
	    event.stackTraceArray = AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}
	taskThread.setSimulationEvent(event);
	return taskThread;
    }

    /**
     * Create a task thread specified by a SimObjectRunnable,
     * but do not schedule it for execution.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param sor the SimObjectRunnable used by the thread
     * @return the task thread
     */

    public TaskThread unscheduledTaskThread(SimObjectRunnable sor) {
	return unscheduledTaskThread(sor.getRunnable(), sor.getSource(),
				     sor.getTag());
    }

    // package-specific interface.
    TaskThread unscheduledTaskThread(final Runnable runnable,
				     SimObject originator,
				     String tag)
    {
	final TaskThread taskThread = new TaskThread(this, new Runnable() {
		public void run() {
		    try {
			runnable.run();
		    } catch (TaskThread.CancelException e) {
		    }
		    TaskThread thread = (TaskThread)(Thread.currentThread());
		    synchronized(thread.schedMonitor) {
			thread.schedPaused = false;
			//System.out.println("schedPaused false 1 " +thread.getOurId());
			thread.schedMonitor.notifyAll();
		    }
		}
	    }, originator, tag);
	Callable callable = new Callable() {
		public void call() {
		    taskThread.start();
		    boolean interrupted = false;
		    synchronized(taskThread.schedMonitor) {
			taskThread.schedCount++;
			//System.out.println("sched wait 5 " + taskThread.getOurId());
			while (taskThread.schedPaused) {
			    try {
				taskThread.schedMonitor.wait();
			    } catch (InterruptedException e) {
				//System.out.println("sched wait 5 (interrupt) " +taskThread.getOurId());
				interrupted = true;
			    }
			}
		    }
		    // System.out.println("sched wait 5 done " + taskThread.getOurId());
		    taskThread.schedCount--;

		    if (interrupted) Thread.currentThread().interrupt();
		}
	    };
	TaskObjectSimEvent event =
	    new TaskObjectSimEvent(callable, taskThread);
	// Initially the event source is the originator.
	event.source = originator;
	if (stackTraceMode) {
	    event.stackTraceArray = AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}
	taskThread.setSimulationEvent(event);
	return taskThread;
    }

    /**
     * Schedule a Task given a delay.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * @param runnable the Runnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTask(Runnable runnable, long delay) {
	TaskThread taskThread = unscheduledTaskThread(runnable);
	scheduleEvent(taskThread.getSimulationEvent(), delay);
	return taskThread;
    }

    TaskThread scheduleTask(Runnable runnable, long delay,
			    StackTraceElement[] stacktrace)
    {
	TaskThread taskThread = unscheduledTaskThread(runnable);
	TaskSimulationEvent event = taskThread.getSimulationEvent();
	((SimulationEvent) event).stackTraceArray = stacktrace;
	scheduleEvent(taskThread.getSimulationEvent(), delay);
	return taskThread;
    }


    /**
     * Schedule a Task given a delay and priority.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * @param runnable the Runnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTask(Runnable runnable,
				   long delay, double tpriority)
    {
	TaskThread taskThread = unscheduledTaskThread(runnable);
	scheduleEvent(taskThread.getSimulationEvent(), delay, tpriority);
	return taskThread;
    }


    /**
     * Schedule a Task specified by a script and given a delay.
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
     *
     */
    public TaskThread scheduleTaskScript(String script, long delay)
	throws UnsupportedOperationException
    {
	return scheduleTaskScript(script, delay, 0.0, null);
    }

    /**
     * Schedule a Task specified by a script and given a delay and priority.
     * A thread executing the code provided by the script will be started
     * at a simulation time equal to the current simulation time + the value
     * of <code>delay</code>. A simulation method pauseTask is provided
     * for cases in which the scripting language does not make the class
     * org.bzdev.devqsim.TaskThread accessible.  If the task will not pause, it
     * is more efficient to use scheduleScript instead.
     * @param script the script implementing the task to schedule
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTaskScript(String script,
					 long delay, double tpriority)
	throws UnsupportedOperationException
    {
	return scheduleTaskScript(script, delay, tpriority, null);
    }


    final TaskThread scheduleTaskScript(String script, long delay,
					Bindings bindings)
	throws UnsupportedOperationException
    {
	return scheduleTaskScript(script, delay, 0.0, bindings);
    }


    void configBindingSwapper(TaskThread thisThread, Bindings bindings) {
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			thisThread.setBindingSwapper
			    (createBindingSwapper(bindings));
			return (Void) null;
		    }
		});
    }

    TaskThread scheduleTaskScript(final String script,
				  long delay, double tpriority,
				  final Bindings bindings)
	throws UnsupportedOperationException
    {
	if (!hasScriptEngine())
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	final Object simvar = (bindings == null)? null: bindings.get("sim");
	final Object selfvar = (bindings == null)? null: bindings.get("self");
	Runnable runnable = new Runnable() {
		public void run() {
		    Thread cthread = Thread.currentThread();
		    if (!(cthread instanceof TaskThread))
			throw new IllegalStateException
			    (errorMsg("pauseWrongContext"));
		    TaskThread taskThread = (TaskThread)cthread;
		    configBindingSwapper(taskThread, bindings);
		    try {
			if (simvar != null) bindings.put("sim", simvar);
			if (selfvar != null) bindings.put("self", selfvar);
			evalScript(script, bindings);
		    } catch (ScriptException e) {
			throw new RuntimeException(e);
		    }
		}
	    };
	TaskThread taskThread = unscheduledTaskThread(runnable);
	scheduleEvent(taskThread.getSimulationEvent(), delay, tpriority);
	return taskThread;
    }

    /**
     * Schedule a task specified by a script object given a delay.
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
     *
     */
    public TaskThread scheduleTaskObject(final Object scriptObject, long delay)
	throws UnsupportedOperationException
    {
	return scheduleTaskObject(scriptObject, delay, 0.0, null);
    }

    /**
     * Schedule a task specified by a script object given a delay and priority.
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
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTaskObject(final Object scriptObject,
					 long delay, double tpriority)
	throws UnsupportedOperationException
    {
	return scheduleTaskObject(scriptObject, delay, tpriority, null);
    }


   TaskThread scheduleTaskObject(final Object scriptObject, long delay,
				 final Bindings bindings)
	throws UnsupportedOperationException
    {
	return scheduleTaskObject(scriptObject, delay, 0.0, bindings);
    }

   TaskThread scheduleTaskObject(final Object scriptObject,
				 long delay, double tpriority,
				 final Bindings bindings)
	throws UnsupportedOperationException
    {
	if (!hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	final Object simvar = (bindings == null)? null: bindings.get("sim");
	final Object selfvar = (bindings == null)? null: bindings.get("self");
	Runnable runnable = new Runnable() {
		public void run() {
		    Thread cthread = Thread.currentThread();
		    if (!(cthread instanceof TaskThread))
			throw new IllegalStateException
			    (errorMsg("pauseWrongContext"));
		    TaskThread taskThread = (TaskThread)cthread;
		    configBindingSwapper(taskThread, bindings);
		    try {
			if (simvar != null) bindings.put("sim", simvar);
			if (selfvar != null) bindings.put("self", selfvar);
			callScriptMethod(bindings, scriptObject, "run");
		    } catch (NoSuchMethodException e) {
			throw new RuntimeException(new ScriptException(e));
		    } catch (ScriptException ee) {
			throw new RuntimeException(ee);
		    }
		}
	    };
	TaskThread taskThread = unscheduledTaskThread(runnable);
	scheduleEvent(taskThread.getSimulationEvent(), delay, tpriority);
	return taskThread;
    }


    /**
     * Schedule a Task specified by a SimObjectRunnable given a delay.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param sor the SimObjectRunnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTask(SimObjectRunnable sor, long delay) {
	TaskThread taskThread = unscheduledTaskThread(sor.getRunnable(),
						      sor.getSource(),
						      sor.getTag());
	scheduleEvent(taskThread.getSimulationEvent(), delay);
	return taskThread;
    }

    /**
     * Schedule a Task specified by a SimObjectRunnable given a delay and
     * priority.
     * A thread executing the code provided by the Runnable will be started
     *  at a simulation time equal to the current simulation time + the value
     *  of <code>delay</code>.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param sor the SimObjectRunnable to execute.
     * @param delay the number of time units to wait, measured from the
     *              current simulation time, until the call is made
     * @param tpriority the priority level of the event at the time it is
     *        scheduled
     * @return the thread that will run this task
     *
     */
    public TaskThread scheduleTask(SimObjectRunnable sor,
				   long delay, double tpriority)
    {
	TaskThread taskThread = unscheduledTaskThread(sor.getRunnable(),
						      sor.getSource(),
						      sor.getTag());
	scheduleEvent(taskThread.getSimulationEvent(), delay, tpriority);
	return taskThread;
    }


    TaskThread scheduleTask(Runnable runnable, long delay,
			    SimObject originator, String tag) {
	TaskThread taskThread = unscheduledTaskThread(runnable,
						      originator,
						      tag);
	scheduleEvent(taskThread.getSimulationEvent(), delay);
	return taskThread;
    }

    TaskThread scheduleTask(Runnable runnable, long delay, double tpriority,
			    SimObject originator, String tag) {
	TaskThread taskThread = unscheduledTaskThread(runnable,
						      originator,
						      tag);
	scheduleEvent(taskThread.getSimulationEvent(), delay, tpriority);
	return taskThread;
    }


    /**
     * Start a simulation task with a default delay of zero.
     * This is a convenience method that is equivalent to
     * scheduleTask(runnable, 0);
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * @param runnable the Runnable to execute.
     * @return the thread running this task
     */
    public TaskThread scheduleTask(Runnable runnable)
    {
	return scheduleTask(runnable, 0);
    }

    /**
     * Start a simulation task with a default delay of zero.
     * This is a convenience method that is equivalent to
     * scheduleTask(runnable, 0);
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * @param runnable the Runnable to execute.
     * @param tpriority the event priority at the current time
     * @return the thread running this task
     */
    public TaskThread scheduleTaskWP(Runnable runnable, double tpriority)
    {
	return scheduleTask(runnable, 0, tpriority);
    }

    /**
     * Start a simulation task specified by a SimObjectRunnable, with
     * a default delay of zero.
     * This is a convenience method that is equivalent to
     * scheduleTask(sor, 0);
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param sor the SimObjectRunnable to execute.
     * @return the thread running this task
     */
    public TaskThread scheduleTask(SimObjectRunnable sor)
    {
	return scheduleTask(sor, 0);
    }

    /**
     * Start a simulation task specified by a SimObjectRunnable, with
     * a default delay of zero and a specified priority.
     * This is a convenience method that is equivalent to
     * scheduleTask(sor, 0);
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * @param sor the SimObjectRunnable to execute.
     * @param tpriority the event priority at the current time
     * @return the thread running this task
     */
    public TaskThread scheduleTaskWP(SimObjectRunnable sor, double tpriority)
    {
	return scheduleTask(sor, 0, tpriority);
    }


    TaskThread scheduleTask(Runnable runnable,
			    SimObject originator,
			    String tag)
    {
	return scheduleTask(runnable, 0, originator, tag);
    }

    TaskThread scheduleTaskWP(Runnable runnable, double tpriority,
			      SimObject originator,
			      String tag)
    {
	return scheduleTask(runnable, 0, tpriority, originator, tag);
    }

    /**
     * Start a simulation task immediately, blocking the current task.
     * This may be called only while the simulation is running.
     * The task runs immediately, with the calling thread blocked,
     * until the task pauses or terminates.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * <P>
     * For scripting languages that can handle Java lambda expressions,
     * the Runnable argument can be the expression in that scripting language
     * that the script engine will treat as a lambda expression. For example,
     * with the Nashorn ECMAScript implementation, the expression
     * <CODE>function() {...}</CODE> will be converted into a lambda expression
     * that Java will then convert into a Runnable.
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param runnable the Runnable to execute.
     * @return the thread running this task
    */
    public TaskThread startImmediateTask(final Runnable runnable)
	throws IllegalStateException
    {
	Thread thread = Thread.currentThread();
	if (!state.simulationRunning) {
	    throw new IllegalStateException
		(errorMsg("startImmediateErr"));
	}
	// Need to save bindings if a script engine is available
	// in case we call this method from a script that uses a
	// simulation object's name space.
	/*
	ScriptEngine engine = getScriptEngine();
	Bindings bindings = (engine == null)? null:
	    engine.getBindings(ScriptContext.ENGINE_SCOPE);
	*/
	// to do: throw an IllegalStateException if we call this
	// while the simulation is not running.
	TaskThread taskThread = new TaskThread(this, new Runnable() {
		public void run() {
		    try {
			runnable.run();
		    } catch (TaskThread.CancelException e) {
		    }
		    TaskThread thread = (TaskThread)(Thread.currentThread());
		    synchronized(thread.schedMonitor) {
			thread.schedPaused = false;
			// System.out.println("schedPaused false 2 " +thread.getOurId());
			thread.schedMonitor.notifyAll();
		    }
		}
	    });
	taskThread.start();
	boolean interrupted = false;
	synchronized(taskThread.schedMonitor) {
	    // System.out.println("sched wait 6 " +taskThread.getOurId());
		while (taskThread.schedPaused) {
		    taskThread.schedCount++;
		    try {
			taskThread.schedMonitor.wait();
		    } catch (InterruptedException e) {
			// System.out.println("sched wait 6 (interrupt) " +taskThread.getOurId());
			interrupted = true;
		    }
		}
	}
	/*
	if (bindings != null) {
	    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}
	*/
	taskThread.schedCount--;
	// System.out.println("sched wait 5 done " +taskThread.getOurId());
	if (interrupted) Thread.currentThread().interrupt();
	return taskThread;
    }

    /**
     * Immediately Start a simulation task specified by a SimObjectRunnable,
     * blocking the current task.
     * This may be called only while the simulation is running.
     * The task runs immediately, with the calling thread blocked,
     * until the task pauses or terminates.
     * After a task thread starts running, a program will not terminate until
     * that task thread terminates, is canceled, or
     * {@link java.lang.System#exit(int)} is called by some thread.
     * @param sor the SimObjectRunnable to execute.
     * @return the thread running this task
    */
    public TaskThread startImmediateTask(SimObjectRunnable sor)
	throws IllegalStateException
    {
	return startImmediateTask(sor.getRunnable(), sor.getSource(),
				  sor.getTag());
    }

    // internal implementation for use in SimObject
    TaskThread startImmediateTask(final Runnable runnable,
				  SimObject originator,
				  String tag)
	throws IllegalStateException
    {
	Thread thread = Thread.currentThread();
	if (!state.simulationRunning) {
	    throw new IllegalStateException
		(errorMsg("startImmediateErr"));
	}
	// to do: throw an IllegalStateException if we call this
	// while the simulation is not running.
	TaskThread taskThread = new TaskThread(this, new Runnable() {
		public void run() {
		    try {
			runnable.run();
		    } catch (TaskThread.CancelException e) {
		    }
		    TaskThread thread = (TaskThread)(Thread.currentThread());
		    synchronized(thread.schedMonitor) {
			thread.schedPaused = false;
			// System.out.println("schedPaused false 2 " +thread.getOurId());
			thread.schedMonitor.notifyAll();
		    }
		}
	    }, originator, tag);
	taskThread.start();
	boolean interrupted = false;
	synchronized(taskThread.schedMonitor) {
	    // System.out.println("sched wait 6 " +taskThread.getOurId());
		while (taskThread.schedPaused) {
		    taskThread.schedCount++;
		    try {
			taskThread.schedMonitor.wait();
		    } catch (InterruptedException e) {
			// System.out.println("sched wait 6 (interrupt) " +taskThread.getOurId());
			interrupted = true;
		    }
		}
	}
	taskThread.schedCount--;
	// System.out.println("sched wait 5 done " +taskThread.getOurId());
	if (interrupted) Thread.currentThread().interrupt();
	return taskThread;
    }

    // complete initialization
    private void runInitq() {
	SimulationEvent eqe = state.initq.poll();
	while (eqe != null) {
	    eqe.pending = false;
	    eqe.processEvent();
	    eqe = state.initq.poll();
	}
    }


    /**
     * Run the simulation until the event queue empties.
     * This method will return when either the event queue is empty
     * or if the current thread is interrupted. A return caused by
     * an interrupt will occur just before an event would have been
     * pulled off of the event queue and will not change the thread's
     * interrupt status.
     * <P>
     * One should note that this method may return while tasks that have
     * started to run have not yet terminated.  If that is the case, the
     * program will not terminate until {@link java.lang.System#exit(int)}
     * is called.
     */
    public void run() {
	if (state.simulationRunning)
	    throw new IllegalStateException(errorMsg("simRunning"));
	Callable simLoop = new Callable() {
		public void call() {
		    try {
			runInitq();
			fireSimulationStarting();
			state.simulationRunning = true;
			if (Thread.currentThread().isInterrupted()) return;
			SimulationEvent eqe = state.pq.poll();
			while (eqe != null) {
			    if (eqe.time > state.currentTicks) {
				state.currentTicks = eqe.time;
				state.currentPriority = eqe.tpriority;
			    }
			    eqe.pending = false;
			    eventStackTrace = eqe.stackTraceArray;
			    eqe.processEvent();
			    if (Thread.currentThread().isInterrupted()) return;
			    eqe = state.pq.poll();
			}
		    } finally {
			state.simulationRunning = false;
			fireSimulationEnding();
		    }
		}
	    };
	if (parent == null) {
	    simLoop.call();
	} else {
	    doScriptPrivileged(simLoop);
	}
    }

    /**
     * Run simulation for a fixed number of time units.
     * This method will return when the specified simulation time has
     * expired, or if the current thread is interrupted.  A return caused by
     * an interrupt will occur just before an event would have been
     * pulled off of the event queue and will not change the interrupt
     * status. If the simulation ends before the end of the interval is
     * reached, the simulation time will be advanced to the end of the interval.
     * <P>
     * One should note that this method may return while tasks that have
     * started to run have not yet terminated.  If that is the case, the
     * program will not terminate until {@link java.lang.System#exit(int)}
     * is called.
     * @param interval the time interval over which to run the simulation
     */
    public void run(long interval) {
	if (state.simulationRunning)
	    throw new IllegalStateException(errorMsg("simRunning"));
	final long maxtime = state.currentTicks + interval;
	Callable simLoop = new Callable() {
		public void call() {
		    try {
			runInitq();
			fireSimulationStarting();
			state.simulationRunning = true;
			if (Thread.currentThread().isInterrupted()) return;
			SimulationEvent eqe = state.pq.poll();
			if (eqe != null && eqe.time > maxtime) {
			    state.pq.add(eqe);
			    eqe.pending = true;
			    state.currentTicks = maxtime;
			    state.currentPriority = 0.0;
			    return;
			}
			while (eqe != null) {
			    if (eqe.time > maxtime) {
				state.pq.add(eqe);
				eqe.pending = true;
				state.currentTicks = maxtime;
				state.currentPriority = 0.0;
				return;
			    }
			    if (eqe.time > state.currentTicks) {
				state.currentTicks = eqe.time;
				state.currentPriority = eqe.tpriority;
			    }
			    eqe.pending = false;
			    eventStackTrace = eqe.stackTraceArray;
			    eqe.processEvent();
			    if (Thread.currentThread().isInterrupted()) return;
			    eqe = state.pq.poll();
			}
			state.currentTicks = maxtime;
			state.currentPriority = 0.0;
		    } finally {
			state.simulationRunning = false;
			fireSimulationEnding();
		    }
		}
	    };
	if (parent == null) {
	    simLoop.call();
	} else {
	    doScriptPrivileged(simLoop);
	}
    }

    /**
     * Create a SimulationMonitor implemented in a scripting language.
     * A simulation monitor is an object that controls when a simulation
     * pauses versus continues to run.  When implemented in a scripting
     * language, the simulation monitor is represented by a object
     * with a method named simulationPauses.  This method takes the
     * simulation as its argument and returns either
     * <ul>
     *   <li> true, a non-zero integer, or some object if the simulation
     *        should pause.
     *   <li> false, 0, or null if the simulation should continue running.
     * </ul>
     * For example (using ECMAScript), if <code>sim</code> is a simulation,
     * then
     * <blockquote>
     * <code><pre>
     *   count = 0;
     *   monitor = sim.createMonitor({
     *     simulationPauses: function(simulation) {
     *      count++;
     *      if (count % 1000 == 0) {
     *        return true;
     *      } else {
     *        return false;
     *      }
     *   }
     * </pre></code>
     * </blockquote>
     * will make the simulation pause each time 1000 events
     * have been processed.  The argument <code>simulation</code> gives the
     * monitor the simulation it is monitoring.
     * <P>
     * Note: using a script to implement a simulation monitor is inefficient,
     * but possibly useful during debugging.
     * @param object the scripting-language object or a string that, when
     *        evaluated by the current scripting language, returns an object
     *        as defined above
     * @return a simulation monitor
     */
    public SimulationMonitor createMonitor(Object object)
	throws IllegalArgumentException
    {
	if (object == null) return null;
	if (object instanceof String) {
	    try {
		object = evalScript((String)object);
	    } catch (ScriptException e) {
		String msg = errorMsg("createMonitorFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    } catch (NullPointerException e) {
		String msg = errorMsg("createMonitorFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    } catch (UnsupportedOperationException e) {
		String msg = errorMsg("createMonitorFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    }
	}
	return new SimScriptMonitor(this, object);
    }

    /**
     * Run a simulation under the control of a simulation monitor.
     * The monitor will determine when the simulation stops or pauses.
     * The class SimulationMonitor uses generics so that a monitor
     * can make use of methods specific to a subclass of Simulation.
     * <P>
     * One should note that this method may return while tasks that have
     * started to run have not yet terminated.  If that is the case, the
     * program will not terminate until {@link java.lang.System#exit(int)}
     * is called.
     * <P>
     * The method {@link SimulationMonitor#simulationPauses()} will be
     * called once per iteration, and the simulation will pause if this
     * method returns true.
     * @param monitor the simulation monitor
     */
    public void run(final SimulationMonitor<? extends Simulation> monitor) {
	if (monitor.getSimulation()!=null && this!=monitor.getSimulation()) {
	    throw new IllegalArgumentException
		(errorMsg("monitorWrongSim"));
	}
	if (state.simulationRunning)
	    throw new IllegalStateException(errorMsg("simRunning"));
	Callable simLoop = new Callable() {
		public void call() {
		    try {
			runInitq();
			fireSimulationStarting();
			state.simulationRunning = true;
			if (monitor.simulationPauses()) {
			    return;
			}
			if (Thread.currentThread().isInterrupted()) return;
			SimulationEvent eqe = state.pq.poll();
			while (eqe != null) {
			    if (eqe.time >= state.currentTicks) {
				state.currentTicks = eqe.time;
				state.currentPriority = eqe.tpriority;
			    }
			    eqe.pending = false;
			    eventStackTrace = eqe.stackTraceArray;
			    eqe.processEvent();
			    if (monitor.simulationPauses()) {
				return;
			    }
			    if (Thread.currentThread().isInterrupted()) return;
			    eqe = state.pq.poll();
			}
		    } finally {
			state.simulationRunning = false;
			fireSimulationEnding();
		    }
		}
	    };
	if (parent == null) {
	    simLoop.call();
	} else {
	    doScriptPrivileged(simLoop);
	}
    }

    EvntListenerList listenerList = new EvntListenerList();

    /**
     * Add a simulation listener.
     * Normally a user will use a simulation adapter, created by
     * subclassing {@link DefaultSimAdapter} or by using the method
     * {@link #createAdapter(Object)} (if the adapter is written using
     * a scripting language).
     * @param l the action listener to add.
     * @see #createAdapter(Object)
     * @see DefaultSimAdapter
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
     * Create a simulation listener based on a script object that implements an
     * adapter.
     * In a scripting language, one might write the adapter as follows:
     * <blockquote><code><pre>
     *    adapter = simulation.createAdapter({
     *       simulationStart: function(sim) {println("simulation start");}
     *       simulationstop: function(sim) {println("simulation stop");}
     *    });
     *    simulation.addSimulationListener(adapter);
     * </pre></code>
     * </blockquote>
     * where <code>simulation</code> is an instance of <code>Simulation</code>
     * and the scripting language is ECMAScript.
     * <P>
     * To respond to particular events, the script object must
     * implement one or more methods.  The arguments to these methods
     * are:
     * <ul>
     *   <li><code>sim</code>. The simulation that scheduled the event
     *   <li><code>q</code>. A task queue or server queue associated with the
     *       event.
     *   <li><code>server</code>. A queue server associated with the event.
     *   <li><code>obj</code>. A simulation object associated with the event
     *       by being responsible for generating it.  Methods with this
     *       argument are the result of events generated by calling certain
     *       methods of the class {@link SimObject} or its subclasses.
     *   <li><code>tag</code>. A tag labeling the event (for
     *       debugging/tracing purposes). The tag can be any object, but is
     *       typically a string or stack trace.
     *  </ul>
     *  Some methods use a subset of these arguments.  The methods are:
     *   <ul>
     *   <li><code>simulationStart(sim)</code>. A simulation has started.
     *   <li><code>simulationStop(sim)</code>. A simulation has stopped
     *       (ended or paused).
     *   <li><code>callStart(sim,tag)</code>. A call scheduled by a
     *       simulation has started.  The call was scheduled using a
     *       simulation's method <code>scheduleCall</code>,
     *       <code>scheduleScript</code>, or
     *       <code>scheduleCallObject</code>, and is not attributed to
     *       any particular simulation object. The argument
     *       representing the call must be either a Callable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "call" method that has no
     *       arguments.
     *   <li><code>callEnd(sim, tag)</code>. A call scheduled by a
     *       simulation has ended.  The call was scheduled using a
     *       simulation's method <code>scheduleCall</code>,
     *       <code>scheduleScript</code>, or
     *       <code>scheduleCallObject</code>, and is not attributed to
     *       any particular simulation object. The argument
     *       representing the call must be either a Callable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "call" method that has no
     *       arguments.
     *   <li><code>callStartSimObject(sim,obj,tag)</code>. A call that
     *       a simulation object scheduled has started.  The call was
     *       created using a simulation object's protected method
     *       named <code>callableScript</code>,
     *       <code>scheduleScript</code>, <code>scheduleCall</code>,
     *       <code>scheduleCallObject</code>, or
     *       <code>bindCallable</code> and is attributed to that
     *       simulation object.
     *   <li><code>callEndSimObject(sim,obj,tag)</code>.  A call that
     *       an simulation object scheduled has ended.  The call was
     *       created using a simulation object's protected method
     *       named <code>callableScript</code>,
     *       <code>scheduleScript</code>, <code>scheduleCall</code>,
     *       <code>scheduleCallObject</code>, or
     *       <code>bindCallable</code> and is attributed to that
     *       simulation object.
     *   <li><code>taskStart(sim,tag)</code>. A task scheduled by a
     *       simulation has started.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskPause(sim,tag)</code>. A task scheduled by a
     *       simulation has paused. The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskResume(sim,tag)</code>. A task scheduled by a
     *       simulation has resumed.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskEnd(sim,tag)</code>. A task scheduled by a
     *       simulation has ended.  The task was created by a
     *       simulation's public method <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>,
     *       <code>scheduleTaskWP</code>,
     *       <code>startImmediateTask</code>, or
     *       <code>unscheduledTaskThread</code> and is not attributed
     *       to any particular simulation object. The argument
     *       representing a task must be either a Runnable, a string
     *       providing a script to execute, or an object in a
     *       scripting language with a "run" method that has no
     *       arguments.
     *   <li><code>taskStartSimObject(sim,obj,tag)</code>. A task that
     *       a simulation object scheduled has started.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskPauseSimObject(sim,obj,tag)</code>. A task that
     *       a simulation object scheduled has paused.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskResumeSimObject(sim,obj,tag)</code>. A task
     *       that a simulation object scheduled has resumed.  The task
     *       was created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskEndSimObject(sim,obj,tag)</code>. A task that a
     *       simulation object scheduled has ended.  The task was
     *       created using a simulation object's protected method
     *       <code>unscheduledTaskThread</code>,
     *       <code>scheduleTask</code>,
     *       <code>scheduleTaskScript</code>,
     *       <code>scheduleTaskObject</code>
     *       <code>startImmediateTask</code>,
     *       <code>runnableScript</code>, <code>runnableObject</code>,
     *       or <code>bindRunnable</code> and is attributed to the
     *       simulation object.
     *   <li><code>taskQueueStart(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has started.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueuePause(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has paused.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueueResume(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has resumed.
     *       Subsequent calls to other adapter methods may indicate
     *       details of that processing.
     *   <li><code>taskQueueEnd(sim,q)</code>. Processing of an
     *       element on a task queue of a simulation has ended.
     *   <li><code>serverSelected(sim,q)</code>. A server was
     *       selected by a server queue in a simulation.
     *   <li><code>serverInteraction(sim,q,server,tag)</code>. A
     *       server associated with a server queue in a simulation
     *       begins interacting with the callable, runnable, or task
     *       it is serving.  For this method, the task or callable is
     *       not associated with a simulation object.
     *   <li><code>serverCallable(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and a callable is run to continue the
     *       simulation.  For this method, the task or callable is not
     *       associated with a simulation object.
     *   <li><code>serverRunnable(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and a Runnable starts execution to
     *       continue the simulation.  For this method, the task or
     *       callable is not associated with a simulation object.
     *   <li><code>serverTask(sim,q,server,tag)</code>. A server
     *       associated with a server queue in a simulation finishes
     *       its interaction and the task that was queued resumes
     *       execution.  For this method, the task or callable is not
     *       associated with a simulation object.
     *   <li><code>serverInteractionSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       begins interacting with the object it is serving.  The
     *       simulation object's protected method bindCallable or
     *       bindRunnable was used to create the Callable or Runnable
     *       and associate it with the simulation object, or a thread
     *       was created using the simulation object's
     *       unscheduledTaskThread or scheduleTask methods.
     *   <li><code>serverCallableSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a callable associated with a
     *       simulation object is run to continue the simulation.  The
     *       simulation object's protected method bindCallable was
     *       used to create the Callable and associate it with the
     *       simulation object.
     *   <li><code>serverRunnableSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a Runnable associated with a
     *       simulation object starts execution to continue the
     *       simulation.  The simulation object's protected method
     *       bindRunnable was used to create the Runnable and
     *       associate it with the simulation object.
     *   <li><code>serverTaskSimObject(sim,q,server,obj,tag)</code>.
     *       A server associated with a server queue in a simulation
     *       finishes its interaction and a Task associated with a
     *       simulation object resumes execution to continue the
     *       simulation.  The task was associated with a simulation
     *       object when the task was created by creating the task by
     *       using one of the simulation object's
     *       unscheduledTaskThread or scheduleTask methods.
     * </ul>
     * <P>
     * Subclasses of {@link Simulation} may define additional methods
     * (for example, {@link org.bzdev.drama.DramaSimulation} adds additional
     * methods for message-related events).
     * @param object the script object implementing the adapter
     */
    public SimulationListener createAdapter(Object object)
	throws IllegalArgumentException
    {
	if (object == null) return null;
	/*
	if (object instanceof String) {
	    try {
		object = evalScript((String)object);
	    } catch (ScriptException e) {
		String msg = errorMsg("createAdapterFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    } catch (NullPointerException e) {
		String msg = errorMsg("createAdapterFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    } catch (UnsupportedOperationException e) {
		String msg = errorMsg("createAdapterFailed", (String)object);
		throw new IllegalArgumentException(msg, e);
	    }
	}
	*/
	return new DefaultSimAdapter(this, object);
    }


    private void fireSimulationStarting() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, this,
				 SimulationStateEvent.Type.SIM_START,
				 this, null);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    private void fireSimulationEnding() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, this,
				 SimulationStateEvent.Type.SIM_STOP,
				 this, null);
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }


    void fireCallStart(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, this,
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
	    SimulationStateEvent(this, this,
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
	    SimulationStateEvent(this, this,
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
	    SimulationStateEvent(this, this,
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
	    SimulationStateEvent(this, this,
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

    void fireTaskEnd(TaskThreadSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, this,
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

    void fireTaskEnd(TaskObjectSimEvent tevent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new
	    SimulationStateEvent(this, this,
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

    /**
     * Pause the currently-running task thread given a delay.
     * Equivalent to TaskThread.pause(delay), but provided primarily for use
     * in scripts.
     * @param delay the interval for which to pause
     */
    public void pauseTask(long delay) {
	TaskThread.pause(delay);
    }

    /**
     * Pause the currently-running task thread based on a delay and priority.
     * Equivalent to TaskThread.pause(delay, tpriority), but provided
     * primarily for use in scripts.
     * @param delay the interval for which to pause
     * @param tpriority the priority level to use for the event associated
     *        with the task.
     */
    public void pauseTask(long delay, double tpriority) {
	TaskThread.pause(delay, tpriority);
    }

    PrintWriter traceOut = null;

    /**
     * Set the default trace output.
     * By default, no trace output will be produced, so this method
     * should be called to turn on tracing (alternatively, the
     * corresponding method {@link TraceSet#setOutput(Appendable)} can be used
     * for each trace set).
     * @param appendable the appendable to use for output; null if
     *        the default is to print nothing
     * @see TraceSet
     * @see SimObject#trace(int,String,Object...)
     * @see SimObject#trace(Enum,String,Object...)
     */
    public void setTraceOutput(Appendable appendable) {
	if (appendable == null) {
	    traceOut = null;
	} else if (appendable instanceof PrintWriter) {
	    traceOut = (PrintWriter) appendable;
	} else if (appendable instanceof Writer) {
	    traceOut = new PrintWriter((Writer)appendable);
	} else {
	    traceOut = new PrintWriter(new AppendableWriter(appendable));
	}
    }

    boolean tracingEnabled = true;

    /**
     * Set whether or not tracing is enabled.
     * A trace will be printed when tracing is enabled if a simulation
     * or a TraceSet created for this simulation has provided an
     * Appendable for output.  If tracing is not enabled, no trace
     * messages will be displayed for any TraceSet associated with
     * this simulation.
     * @param value true if tracing is to be enabled; false if
     *        tracing is to be disabled
     */
    public void enableTracing(boolean value) {tracingEnabled = value;}

    /**
     * Determine if tracing is enabled.
     * A trace will be printed when tracing is enabled if a simulation
     * or a TraceSet created for this simulation has provided an
     * Appendable for output.  If tracing is not enabled, no trace
     * messages will be displayed for any TraceSet associated with
     * this simulation.
     * @return true if tracing is enabled; false otherwise
     */
    public boolean isTracingEnabled() {return tracingEnabled;}

}

//  LocalWords:  exbundle SimulationHelper SimObject SimObjectHelper
//  LocalWords:  SecurityException PriorityQueue SimulationEvent pq
//  LocalWords:  setStackTraceMode boolean TraceSet tsallow scrunner
//  LocalWords:  setTraceSetMode getEventStackTrace StackTraceElement
//  LocalWords:  trustLevel namer getTicksPerUnitTime currentTicks ul
//  LocalWords:  ticksPerUnitTime ScriptingContext getCurrentTime sim
//  LocalWords:  tpriority Deschedule pqentry deschedule scheduleCall
//  LocalWords:  SimObjectCallable noScriptEngine onExecutionStarting
//  LocalWords:  onExecutionEnding scriptObject Callable's getOurId
//  LocalWords:  schedPaused sched taskThread SimObjectRunnable sor
//  LocalWords:  pauseTask scheduleScript pauseWrongContext li pre
//  LocalWords:  scheduleCallObject scheduleTask startImmediateErr
//  LocalWords:  ScriptEngine getScriptEngine IllegalStateException
//  LocalWords:  setBindings ScriptContext simRunning ECMAScript msg
//  LocalWords:  SimulationMonitor simulationPauses blockquote Enum
//  LocalWords:  createMonitor createMonitorFailed monitorWrongSim
//  LocalWords:  subclassing DefaultSimAdapter createAdapter println
//  LocalWords:  simulationStart simulationstop addSimulationListener
//  LocalWords:  simulationStop callStart callEnd callStartSimObject
//  LocalWords:  callableScript bindCallable callEndSimObject taskEnd
//  LocalWords:  taskStart scheduleTaskScript scheduleTaskObject
//  LocalWords:  scheduleTaskWP startImmediateTask taskPause errorMsg
//  LocalWords:  unscheduledTaskThread taskResume taskStartSimObject
//  LocalWords:  runnableScript runnableObject bindRunnable setOutput
//  LocalWords:  taskPauseSimObject taskResumeSimObject taskQueueEnd
//  LocalWords:  taskEndSimObject taskQueueStart taskQueuePause
//  LocalWords:  taskQueueResume serverSelected serverInteraction
//  LocalWords:  serverCallable serverRunnable serverTask instanceof
//  LocalWords:  serverInteractionSimObject serverCallableSimObject
//  LocalWords:  serverRunnableSimObject serverTaskSimObject
//  LocalWords:  evalScript ScriptException createAdapterFailed
//  LocalWords:  IllegalArgumentException NullPointerException
//  LocalWords:  UnsupportedOperationException appendable
