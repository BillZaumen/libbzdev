package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import org.bzdev.lang.Callable;
import java.util.*;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Superclass for those classes that need standard methods for
 * scheduling a specific task or method call. 
 * Subclasses can provide implementations of the methods defaultCall()
 * and defaultTask() to provide default code to either call or run in
 * a task thread. Methods allow these to be scheduled and canceled.
 */
abstract public class GenericTaskObject<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericSimObject<S,A,C,D,DM,F,G>
{
    private SimulationEvent defaultCallEvent = null;

    private static String errorMsg(String key, Object... args) {
	return GenericSimulation.errorMsg(key, args);
    }


    /**
     * Schedule an event to trigger a call to defaultCall.
     * @param delay the number of time units to wait past the current time
     * @exception IllegalStateException a call was already scheduled
     */
    protected void scheduleDefaultCall(long delay) throws IllegalStateException
    {
	if (defaultCallEvent != null) 
	    throw new IllegalStateException
		(errorMsg("defaultCall", getName()));
		/*("defaultCall already scheduled");*/
	Callable callable = new Callable() {
		public void call() {
		    defaultCallEvent = null;
		    defaultCall();
		}
	    };
	defaultCallEvent = getSimulation().scheduleCall(callable, delay);
	/*
	defaultCallEvent =
	    getSimulation().scheduleEvent(new GenericTaskObjectSimEvent(callable), 
					  delay);
	*/
    }

    /**
     * Cancel a scheduled call to the defaultCall method.
     */
    protected void cancelDefaultCall() {
	if (defaultCallEvent != null) {
	    defaultCallEvent.cancel();
	    defaultCallEvent = null;
	}
    }


    /*
    /**
     * Schedule an event to trigger a call to a user-supplied Callable
     * @param callable the Callable to call
     * @param delay the number of time units to wait past the current time
     * @return the corresponding simulation event
    protected SimulationEvent scheduleCall(Callable callable, long delay) {
	return getSimulation().scheduleEvent(new GenericTaskObjectSimEvent(callable), 
					     delay);
    }
    */

    boolean defaultTaskRunning = false;

    /**
     * The method to call when the default task is scheduled.
     * A default task differs from a default call in that a
     * default task can be suspended or paused for a given amount of
     * simulation time, or can be placed on a queue.
     */
    protected void defaultTask() {
    }

    private TaskThread defaultTaskThread = null;

    /**
     * Schedule the default task for immediate execution.
     */
    protected void scheduleDefaultTask() 
	throws InterruptedException, IllegalStateException
    {
	scheduleDefaultTask(0);
    }

    /**
     * Schedule the default task for future execution.
     * @param delay the amount of simulation time to wait before the
     *              task is run
     */
    protected void scheduleDefaultTask(long delay) 
	throws InterruptedException, IllegalStateException
    {
	if (defaultTaskRunning) {
	    throw new IllegalStateException
		(errorMsg("defaultTask", getName()));
		/*("defaultTask already scheduled");*/
	}
	defaultTaskRunning = true;
	Runnable runnable = new Runnable() {
		public void run() {
		    try {
			defaultTask();
		    } finally {
			defaultTaskRunning = false;
			defaultTaskThread = null;
		    }
		}
	    };

	defaultTaskThread = getSimulation().scheduleTask(runnable, delay);
    }

    /**
     * Cancel the default task.
     */

    protected void cancelDefaultTask() {
	if (defaultTaskRunning) {
	    defaultTaskThread.cancel();
	    defaultTaskRunning = false;
	    defaultTaskThread = null;
	}
    }

    /**
     * Respond to a scheduled event.
     * Subclasses that schedule work should implement this method. When
     * an event queued by scheduleDefaultCall is ready, this method will be
     * called,
     */
    protected void defaultCall() {}


    protected void onDelete() {
	cancelDefaultCall();
	cancelDefaultTask();
	super.onDelete();
    }



    /**
     * Constructor for task objects that are interned in the
     * simulation tables.
     * @param sim the simulation
     * @param name the name of the belief; null if one should be chosen
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericTaskObject(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }
}

//  LocalWords:  exbundle Superclass Subclasses defaultCall
//  LocalWords:  defaultTask IllegalStateException defaultCallEvent
//  LocalWords:  getSimulation scheduleEvent SimulationEvent
//  LocalWords:  GenericTaskObjectSimEvent scheduleCall
//  LocalWords:  scheduleDefaultCall IllegalArgumentException
