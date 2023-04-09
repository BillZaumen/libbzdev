package org.bzdev.devqsim;

/**
 * Superclass for simulation events.
 * This class should be subclassed to create new types of simulation
 * events - it is public to make it possible for an event to be canceled.
 */
abstract public class SimulationEvent 
    implements Comparable<SimulationEvent> {
    Simulation simulation;
    // fields for SimulationEventQuueue
    SimulationEvent leftPQEntry = null;
    SimulationEvent rightPQEntry = null;
    SimulationEvent parentPQEntry = null;
    long time;
    double tpriority;
    long instance;

    Object source = null;

    StackTraceElement[] stackTraceArray = null;

    /**
     * Get the stack-trace array.
     * When a simulation is in stack-trace mode, a stack trace is
     * used to tag the point of time at which various events were
     * created.  In a few instances, a new event may be created
     * with its stack trace being that of a preceding event.
     * @return the stack-trace array; null if there is none
     */
    protected StackTraceElement[] getStackTraceArray() {
	return stackTraceArray;
    }

    /**
     * Set the stack-trace array.
     * When a simulation is in stack-trace mode, a stack trace is
     * used to tag the point of time at which various events were
     * created.  In a few instances, a new event may be created
     * with its stack trace being that of a preceding event.
     * @param array the stack-trace array; null if not provided
     */
    protected void setStackTraceArray(StackTraceElement[] array) {
	stackTraceArray = array;
    }

    /**
     * Get the object labeled as the source of a simulation event.
     * This object will either be the simulation itself or a SimObject
     * that is responsible for creating the event.
     * @return the source of the event
     */
    public Object getSource() {return source;}

    /**
     * get the time field
     * @return the time in units of ticks
     */
    public long getTime() {
	return time;
    }

    /**
     * Comparison method.
     * @param event another simulation event
     * @return -1, 0, or 1 integer when this object is less than, 
     *         equal to, or greater than the object denoted by event
     *         in terms of the time ordering of simulation events
     */
    final public int compareTo(SimulationEvent event) {
	if (time == event.time) {
	    if (tpriority == event.tpriority) {
		long v = instance - event.instance;
		if (v < 0) return -1;
		if (v > 0) return 1;
		return 0;
	    } else {
		return ((tpriority > event.tpriority)? 1: -1);
	    }
	} else {
	    long v = time - event.time;
	    return v > 0 ? 1: -1;
	}
    }

    // Operations:

    boolean canceled = false;

    /**
     * Cancel an event.  The default behavior is to deschedule the
     * event from the simulation's event queue.  Subclasses should
     * perform any additional processing and also call super.cancel()
     * or deschedule the event explicitly.
     * @return true if the event could be canceled; false otherwise
     */
    public  boolean cancel() {
	if (simulation == null && !canceled) {
	    // in this case, the entry is on a queue.
	    return true;
	}
	canceled = true;
	return simulation.descheduleEvent(this);
    }

    /**
     * Check if an event was canceled.
     * @return true if the current event was canceled; false otherwise
     */
    public boolean isCanceled() {return canceled;}

    boolean pending = false;

    /**
     * Determine if an event is pending.
     * An event is pending if it has been scheduled on the event queue
     * but not yet been processed.  It is not pending while the event
     * is being processed.
     * @return true if this event is pending; false otherwise
     */

    public boolean isPending() {return pending;}

    /**
     * Process an event.
     * This method will be invoked by the simulation scheduler when
     * the event should occur.  Subclasses must implement it if any
     * processing is to occur.
     */
    abstract protected void processEvent();
}

//  LocalWords:  Superclass subclassed SimulationEventQuueue
//  LocalWords:  SimObject deschedule Subclasses
