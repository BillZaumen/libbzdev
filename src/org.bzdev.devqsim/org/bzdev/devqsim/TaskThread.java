package org.bzdev.devqsim;

import org.bzdev.scripting.*;
import org.bzdev.lang.Callable;
import org.bzdev.lang.CallableReturns;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.Bindings;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Threads that can appear on a simulation's event queue.
 * This class provides thread-control mechanisms that ensure
 * that only one TaskThread runs at a time, and only when the
 * thread running the simulation has paused. Aside from
 * reducing the need for locking, this eliminates the need
 * for rollbacks.
 * @see Simulation
 */

public class TaskThread extends Thread {

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    // these are used by TaskQueue and ServerQueue
    boolean threadQueued = false;
    boolean queuingCanceled = false;

    /**
     * Exception used while terminating task threads.
     * A Cancel Exception is generated when a TaskThread is
     * interrupted and pause(int) is called. This exception
     * indicates that the task should be canceled.
     */
    static public class CancelException extends RuntimeException {
	// make
	CancelException() {}
    }

    Simulation simulation;
    SimObject originator;
    String tag;
    boolean isTaskScript = false;
    // ScriptEngine engine = null;
    Bindings bindings = null;
    Bindings oldBindings = null;

    ScriptingContext.BindingSwapper swapper = null;
    // for use in Simulate and TaskQueue only
    void setBindingSwapper(ScriptingContext.BindingSwapper swapper) {
	this.swapper = swapper;
    }


    /**
     * Get the SimObject that originated the thread
     * @return the simulation object denoted as the thread's originator;
     *         null if there are none
     */
    public SimObject getOriginator() {return originator;}

    /**
     * Get a description of a thread.
     * @return a string describing the thread
     */
    public String getTag() {return tag;}

    TaskSimulationEvent event;
    Runnable runnable;
    boolean runnablePaused = true;
    int runnableCount = 0;
    Object runnableMonitor = new Object();
    volatile boolean schedPaused = false;
    boolean callingPause = false; // used to tell if a runnable
                                  //  is pausing or terminating.
    volatile int schedCount = 0;
    Object schedMonitor = new Object();
    volatile static long nextOurId = 0;
    long ourId = (nextOurId++);

    long getOurId() {return (ourId);}
    
    TaskThread(Simulation simulation, Runnable runnable) {
	super();
	this.simulation = simulation;
	this.runnable = runnable;
	this.originator = null;
	this.tag = null;
    }

    TaskThread(Simulation simulation, Runnable runnable,
	       SimObject originator, String tag) {
	super();
	this.simulation = simulation;
	this.runnable = runnable;
	this.originator = originator;
	this.tag = tag;
    }

    public void start() {
	schedPaused = true;
	runnablePaused = false;
	super.start();
    }

    public void run() {
	// essentially just does a function call, but we
	// need to follow it by some thread-manipulation
	// stuff.
	try {
	    runnable.run();
	} finally {
	    synchronized(this.schedMonitor) {
		this.schedPaused = false;
		// System.out.println("schedPaused false 3 " +this.getOurId());
		this.schedMonitor.notifyAll();
	    }
	}
    }

    void setSimulationEvent(TaskSimulationEvent event) {
	this.event = event;
    }

    Simulation getSimulation() {return simulation;}

    /**
     * Get the simulation event that will restart this thread.
     * @return the simulation event.
     */
    public TaskSimulationEvent getSimulationEvent() {
	return event;
    }

    public void interrupt() {
	synchronized(schedMonitor) {
	    synchronized(runnableMonitor) {
		simulation.descheduleEvent(event);
		super.interrupt();
	    }
	}
    }

    /**
     * Cancel a task thread.
     * When called while from the thread, the thread will exit;
     * Otherwise the thread is interrupted.
     *
     * Note: a TaskThread.CancelException is used to terminate the 
     * thread. This exception should not be caught (or if caught,
     * must be rethrown).
     */
    public void cancel() {
	if (Thread.currentThread() == this)
	    throw new CancelException();
	interrupt();
    }

    /**
     * Reschedule a task thread for a specified delay.
     * This method will fail if the current task is not already scheduled.
     * @param delay the number of simulation time units from the
     *        current simulation time at which the task will
     *        be scheduled to run
     * @return true on success; false on failure
     */

    public boolean reschedule(long delay) {
	synchronized(schedMonitor) {
	    synchronized(runnableMonitor) {
		return simulation.rescheduleEvent(event, delay);
	    }
	}
    }

    /**
     * Reschedule a task thread for a specified delay and event priority.
     * This method will fail if the current task is not already scheduled.
     * @param delay the number of simulation time units from the
     *        current simulation time at which the task will
     *        be scheduled to run
     * @param tpriority the event priority at the time the thread will
     * @return true on success; false on failure
     */

    public boolean reschedule(long delay, double tpriority) {
	synchronized(schedMonitor) {
	    synchronized(runnableMonitor) {
		return simulation.rescheduleEvent(event, delay, tpriority);
	    }
	}
    }

    /**
     * Get the current task thread.
     * @return the current thread
     * @exception IllegalStateException the current thread is not a TaskThread
     */
    static public TaskThread currentThread() throws IllegalStateException  {
	Thread cthread = Thread.currentThread();
	if (!(cthread instanceof TaskThread))
	    throw new IllegalStateException(errorMsg("notTaskThread"));
	return (TaskThread) cthread;
    }


    /**
     * Causes the current TaskThread to pause, possibly indefinitely.
     * This must be called from a TaskThread thread.
     * When a thread is paused, a TaskThreadSimEvent is generated that
     * allows the thread to be canceled or restarted.  Sometimes it is
     * necessary to encapsulate this event within another event for a
     * variety of reasons such as executing additional code when an
     * thread restarts.  This is handled by three methods:
     * <ul>
     *   <li>callable1. This argument makes it possible to
     *       handle the queuing or scheduling of an event. If callable1's
     *       call method, whose argument is the TaskThreadSimEvent generated,
     *       returns null, that signals that the queuing or
     *       scheduling failed (e.g., one attempted to queue the thread on
     *       a queue with a size limit that has been reached) and that the
     *       thread will simply continue executing.  If callable1 itself is
     *       null, the effect is the same as if callable1's call method
     *       simply returned its argument.  When callable1 is non-null and
     *       returns null, the call to 'pause' will return false; otherwise
     *       the value returned becomes the event associated with the call to
     *       'pause'.
     *  <li> callable2. This argument is intended for cases where a user
     *       wishes to keep a reference to an event so that it can be
     *       canceled.  callable2's 'call' method will be called with the
     *       simulation event returned by callable1's call method (implicitly
     *       if callable1 is null), and callable2's call method is responsible
     *       for storing its argument for later use.  If the argument to
     *       callable2's call method is null, the call method will not be
     *       called.
     *  <li> afterPause. This argument allows code to be executed
     *       immediately before pause returns.  It's call method, which
     *       takes no arguments, should return true normally, but should
     *       return false if the task should behave as if pausing, queuing,
     *       or some other operation associated with the pause,  had failed.
     * </ul>
     * <p>
     * Note: this method is intended for creating objects such as task
     * queues. Casual use of it is discouraged. While somewhat complex, this
     * complexity is necessary in order to handle cases such as finite queues
     * in which an attempt to be placed on a queue may fail. The return value
     * from a call to 'pause' returns this status information.
     * It is worth mentioning that, unless {@link java.lang.System#exit(int)}
     * is called, a program will not terminate if a thread,
     * including an instance of TaskThread, has started but has not yet
     * terminated.
     * @param callable1 a TaskEventCallable that will be given a
     *        TaskThreadSimEvent whose processEvent method restarts the
     *        current thread; null if the current TaskThreadSimEvent
     *        should be used
     * @param callable2 a SimEventCallable that will be given the simulation
     *        event generated by callable1; null if not provided
     * @param afterPause a CallableReturns<Boolean> whose 'call' method will
     *        be executed just as the current thread restarts in order to
     *        determine the return value; null if the return value should
     *        always be true
     * @return true to indicate that processing associated with the pause
     *         succeeded; false to indicate a failure
     * @exception IllegalStateException called in wrong context
     */
    static public boolean pause(TaskEventCallable callable1,
				SimEventCallable callable2,
				CallableReturns<Boolean> afterPause) 
	throws IllegalStateException 
    {
	SimulationEvent result = null;
	TaskThreadSimEvent newEvent;
	Thread cthread = Thread.currentThread();

	if (!(cthread instanceof TaskThread)) {
	    throw new IllegalStateException(errorMsg("illPlacedPause"));
	}
	TaskThread taskThread = (TaskThread)cthread;
	Simulation sim = taskThread.getSimulation();
	if (taskThread.swapper != null) {
	    taskThread.swapper.swap();
	}
	taskThread.callingPause = true;
	synchronized(taskThread.schedMonitor) {
	    boolean oldPaused;
	    synchronized(taskThread.runnableMonitor) {
		oldPaused = taskThread.runnablePaused;
		taskThread.runnablePaused =true;
	    }
	    TaskSimulationEvent oldEvent = taskThread.getSimulationEvent();
	    if (oldEvent != null && (oldEvent instanceof TaskThreadSimEvent)) {
		newEvent = (TaskThreadSimEvent)oldEvent;
	    } else {
		newEvent = new TaskThreadSimEvent(sim, taskThread);
		if (oldEvent != null) {
		    newEvent.stackTraceArray = oldEvent.stackTraceArray;
		}
		taskThread.setSimulationEvent(newEvent);
	    }
	    if (taskThread.swapper != null) {
		taskThread.swapper.swap();
	    }
	    if (callable1 != null) result = callable1.call(newEvent);
	    else result = newEvent;
	    if (callable2 != null && result != null) callable2.call(result);
	    if (taskThread.swapper != null) {
		taskThread.swapper.swap();
	    }
	    if (result == null) {
		// this indicates a failure, so ignore the event,
		// undo all changes, and return false.
		synchronized(taskThread.runnableMonitor) {
		    taskThread.runnablePaused = oldPaused;
		    taskThread.setSimulationEvent(oldEvent);
		    taskThread.callingPause = false;
		    if (taskThread.swapper != null) {
			taskThread.swapper.swap();
		    }
		    return false;
		}
	    }
	    taskThread.schedPaused = false;
	    //System.out.println("schedPaused false 4 " +taskThread.getOurId());
	    taskThread.schedMonitor.notifyAll();
	}
	synchronized(taskThread.runnableMonitor) {
	    taskThread.runnableCount++;
	    try {
		//System.out.println("runnable wait 1 " +taskThread.getOurId());
		while (taskThread.runnablePaused)
		    taskThread.runnableMonitor.wait();
	    } catch (InterruptedException e) {
		taskThread.runnableCount--;
		if (taskThread.runnableCount == 0) {
		    //System.out.println("runnablePaused true 1 " +taskThread.getOurId());
		    taskThread.runnablePaused = true;
		}
		//System.out.println("runnable wait 1 done (interrupted) ");
		try {
		    throw new CancelException();
		} finally {
		    if (taskThread.swapper != null) {
			taskThread.swapper.swap();
		    }
		}
	    }
	    taskThread.runnableCount--;
	    if (taskThread.runnableCount == 0) {
		//System.out.println("runnablePaused true 1 " +taskThread.getOurId());
		taskThread.runnablePaused = true;
	    }
	    //System.out.println("runnable wait 1 done "  +taskThread.getOurId());
	}
	boolean retval = true;
	if (taskThread.swapper != null) {
	    taskThread.swapper.swap();
	}
	taskThread.callingPause = false;
	// this runs as if the call had returned to provide an
	// opportunity to change the return value.
	if (afterPause != null) {
	    retval = afterPause.call();
	}
	return retval;
    }


    /**
     * Causes the current TaskThread to pause for a specified delay.
     * This must be called from a TaskThread thread.
     * It is worth mentioning that, unless {@link java.lang.System#exit(int)}
     * is called, a program will not terminate if a thread,
     * including an instance of TaskThread, has started but has not yet
     * terminated.
     * @param delay the number of simulation time units to wait
     * @exception IllegalStateException called in wrong context
     */
    static public void pause(long delay) 
	throws IllegalStateException 
    {
	pause(delay, 0.0);
    }
    /**
     * Causes the current TaskThread to pause for a specified delay and
     * event priority.
     * This must be called from a TaskThread thread.
     * @param delay the number of simulation time units to wait
     * @param tpriority the event priority at the time the thread will
     *        resume
     * @exception IllegalStateException called in wrong context
     */
    static public void pause(long delay, double tpriority)
	throws IllegalStateException
    {
	Thread cthread = Thread.currentThread();
	if (!(cthread instanceof TaskThread))
	    throw new IllegalStateException(errorMsg("illPlacedPause"));
	TaskThread taskThread = (TaskThread)cthread;
	Simulation sim = taskThread.getSimulation();
	if (taskThread.swapper != null) {
	    taskThread.swapper.swap();
	}
	taskThread.callingPause = true;
	synchronized(taskThread.schedMonitor) {
	    synchronized(taskThread.runnableMonitor) {
		taskThread.runnablePaused =true;
	    }
	    SimulationEvent oldEvent = taskThread.getSimulationEvent();
	    TaskThreadSimEvent newEvent;
	    if (oldEvent != null && (oldEvent instanceof TaskThreadSimEvent)) {
		newEvent = (TaskThreadSimEvent)oldEvent;
	    } else {
		newEvent = new TaskThreadSimEvent(sim, taskThread);
		if (oldEvent != null) {
		    newEvent.stackTraceArray = oldEvent.stackTraceArray;
		}
		taskThread.setSimulationEvent(newEvent);
	    }
	    sim.scheduleEvent(newEvent, delay, tpriority);
	    taskThread.schedPaused = false;
	    //System.out.println("schedPaused false 5 " +taskThread.getOurId());
	    taskThread.schedMonitor.notifyAll();
	}
	synchronized(taskThread.runnableMonitor) {
	    //System.out.println("runnable wait 2 " +taskThread.getOurId());
	    taskThread.runnableCount++;
	    try {
		while (taskThread.runnablePaused)
		    taskThread.runnableMonitor.wait();
	    } catch (InterruptedException e) {
		//System.out.println("runnable wait 2 done (interrupted)");
		taskThread.runnableCount--;
		if (taskThread.runnableCount == 0) {
		    //System.out.println("runnablePaused true 2 " +taskThread.getOurId());
		    taskThread.runnablePaused = true;
		}
		throw new CancelException();
	    }
	    taskThread.runnableCount--;
	    if (taskThread.runnableCount == 0) {
		// System.out.println("runnablePaused true 2 " +taskThread.getOurId());
		taskThread.runnablePaused = true;
	    }
	    taskThread.callingPause = false;
	    //System.out.println("runnable wait 2 done " +taskThread.getOurId());
	}
	if (taskThread.swapper != null) {
	    taskThread.swapper.swap();
	}
    }
}

//  LocalWords:  exbundle TaskThread TaskQueue ServerQueue SimObject
//  LocalWords:  ScriptEngine runnable schedPaused getOurId rethrown
//  LocalWords:  CancelException tpriority IllegalStateException ul
//  LocalWords:  notTaskThread TaskThreadSimEvent li afterPause
//  LocalWords:  TaskEventCallable processEvent SimEventCallable
//  LocalWords:  CallableReturns illPlacedPause taskThread
//  LocalWords:  runnablePaused
