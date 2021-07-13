package org.bzdev.devqsim;
import org.bzdev.lang.Callable;
import java.util.*;

/**
 * Base class for wait task queues.
 * These queues hold entries indefinitely until released or until the
 * queue is not frozen.  Unless a subclass specifies otherwise, instances
 * of AbstractWaitTaskQueue are initially frozen so that all entries added
 * to the queue are queued and not run until explicitly released. When
 * frozen, the methods
 * {@link TaskQueue#release(int) release},
 * {@link TaskQueue#releaseUpTo(int) releaseUpTo},
 * {@link TaskQueue#clearReleaseCount() clearReleaseCount}, or
 * {@link TaskQueue#freeze(boolean freeze) freeze} (with an argument
 * whose value is <code>false</code>) must be used to allow a wait
 * task queue to decrease in size.  For subclasses in which 
 * {@link TaskQueue#freeze(boolean freeze) freeze} must not be used,
 * the protected method
 * {@link TaskQueue#setCanFreeze(boolean) setCanFreeze} should be called
 * with an argument of false after the queue is properly configured.
 * @see WaitTaskQueue
 */
abstract public class AbstractWaitTaskQueue
    extends TaskQueue<AbstractWaitTaskQueue.Parameter>
{
    // Following is a trivial modification of the code for
    // DelayTaskQueue - different parameters and no interval or
    // delay arguments

    /**
     * Type parameter required by TaskQueue.
     */
    public static class Parameter {
	double tpriority;

	/**
	 * Constructor.
	 */
	public Parameter() {
	    tpriority = 0.0;
	}

	/**
	 * Constructor with a time event priority.
	 * @param tpriority the time event priority when the event is scheduled
	 */
	public Parameter(double tpriority) {
	    this.tpriority = tpriority;
	}
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public AbstractWaitTaskQueue(Simulation sim, boolean intern) {
	super(sim, null, intern);
	freeze(true);
    }

    /**
     * Constructor for named queues.
     * @param sim the simulation
     * @param name the name for the queue
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public AbstractWaitTaskQueue(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	freeze(true);
    }

    /**
     * Add a Callable to the queue.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(Callable callable) 
    {
	return doAdd(callable, new Parameter(), 0L, 0.0);
    }

    /**
     * Add a Callable to the queue a time event priority.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param tpriority the time event priority for the callable
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(Callable callable, double tpriority)
    {
	return doAdd(callable, new Parameter(tpriority), 0L, tpriority);
    }

    /**
     * Add a Callable bound to a simulation object to the queue.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(SimObjectCallable callable) 
    {
	return doAdd(callable, new Parameter(), 0L, 0.0);
    }

    /**
     * Add a Callable bound to a simulation object to the queue given a
     * time event priority.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(SimObjectCallable callable, double tpriority)
    {
	return doAdd(callable, new Parameter(tpriority), 0L, tpriority);
    }

    /**
     * Add a script to the queue.
     * When the script is scheduled and the corresponding event is
     * processed, the script will be evaluated.
     * @param script the script to add to the queue
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	addCallScript(String script)
    {
	return doAddCallScript(script, new Parameter(), 0L, 0.0);
    }

    /**
     * Add a script to the queue a time event priority.
     * When the script is scheduled and the corresponding event is
     * processed, the script will be evaluated.
     * @param script the script to add to the queue
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	addCallScript(String script, double tpriority)
    {
	return doAddCallScript(script, new Parameter(tpriority), 0L, tpriority);
    }


    /**
     * Add a script object to execute to the queue.
     * When the script is scheduled and the corresponding event is
     * processed, the script object's <code>call</code> method will 
     * be executed.
     * @param scriptObject the script object to add
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	addCallObject(Object scriptObject)
    {
	return doAddCallObject(scriptObject, new Parameter(), 0L, 0.0);
    }

    /**
     * Add a script object to execute to the queue given a time event priority.
     * When the script is scheduled and the corresponding event is
     * processed, the script object's <code>call</code> method will
     * be executed.
     * @param scriptObject the script object to add
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	addCallObject(Object scriptObject, double tpriority)
    {
	return doAddCallObject(scriptObject, new Parameter(tpriority),
			       0L, tpriority);
    }


    /**
     * Add a Runnable to the queue.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(Runnable runnable)
    {
	return doAdd(runnable, new Parameter(), 0L, 0.0);
    }
    /**
     * Add a Runnable to the queue given a time event priority.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(Runnable runnable, double tpriority)
    {
	return doAdd(runnable, new Parameter(tpriority), 0L, tpriority);
    }


    /**
     * Add a Runnable bound to a simulation object to the queue.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(SimObjectRunnable runnable) 
    {
	return doAdd(runnable, new Parameter(0.0), 0L, 0.0);
    }

    /**
     * Add a Runnable bound to a simulation object to the queue given
     * a time event priority.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter>
	add(SimObjectRunnable runnable, double tpriority)
    {
	return doAdd(runnable, new Parameter(tpriority), 0L, tpriority);
    }


    /**
     * Add a script to be run as a task to the queue given a processing-time
     * interval.
     * When script is scheduled and the corresponding event is
     * processed, a task thread will be started executing the script by
     * evaluating it.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param script the script representing a task to be added to the queue
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskScript(String script)
    {
	return doAddTaskScript(script, new Parameter(0.0), 0L, 0.0);
    }

    /**
     * Add a script to be run as a task to the queue given a processing-time
     * interval and time event priority.
     * When script is scheduled and the corresponding event is
     * processed, a task thread will be started executing the script by
     * evaluating it.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param script the script representing a task to be added to the queue
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskScript(String script,
						      double tpriority)
    {
	return doAddTaskScript(script, new Parameter(tpriority), 0L, tpriority);
    }

    /**
     * Add a script object to be run as a task to the queue.
     * When the object is scheduled and the corresponding event is
     * processed, a task thread will be started and the object's
     * <code>run()</code> method will be called.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param scriptObject the object representing a task to be added 
     *         to the queue
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskObject(Object scriptObject)
    {
	return doAddTaskObject(scriptObject, new Parameter(0.0), 0L, 0.0);
    }

    /**
     * Add a script object to be run as a task to the queue given a
     * time event priority.
     * When the object is scheduled and the corresponding event is
     * processed, a task thread will be started and the object's
     * <code>run()</code> method will be called.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param scriptObject the object representing a task to be added
     *         to the queue
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskObject(Object scriptObject,
						      double tpriority)
    {
	return doAddTaskObject(scriptObject, new Parameter(tpriority),
			       0L, tpriority);
    }

    protected long getInterval(Parameter parameter) {
	return 0L;
    }

    protected double getTPriority(Parameter parameter) {
	return parameter.tpriority;
    }

    /**
     * Add the currently running task thread to a queue.
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask()
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(0.0), null);
    }

    /**
     * Add the currently running task thread to a queue given a
     * time event priority.
     * @param tpriority the time event priority
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(double tpriority)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(tpriority), null);
    }

    /**
     * Add the currently running task thread to a queue given a
     * SimEventCallable.
     * @param callable a SimEventCallable used to provide access to
     *        the event representing the queued task
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(SimEventCallable callable)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(0.0), callable);
    }

    /**
     * Add the currently running task thread to a queue given a
     * time event priority, and SimEventCallable.
     * @param tpriority the time event priority
     * @param callable a SimEventCallable used to provide access to
     *        the event representing the queued task
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(double tpriority,
				  SimEventCallable callable)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(tpriority), callable);
    }

    // following two needed to avoid confusing scripting languages
    // regarding type casting.
    /**
     * Add the currently running task thread to a queue and return the
     *  SimulationEvent generated.
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject()
	throws IllegalStateException
    {
	return addCurrentTaskObject(new Parameter());
    }

    /**
     * Given a time event priority, add the currently running
     * task thread to a queue and return the SimulationEvent generated.
     * @param tpriority the time event priority
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject(double tpriority)
	throws IllegalStateException
    {
	return addCurrentTaskObject(new Parameter(tpriority));
    }

    // All subclasses of AbstractWaitTaskQueue use the same data structure
    // for queuing, so we've added more or less the same code that is in
    // FifoTaskQueue

    LinkedList<TaskQueueSimEvent<Parameter>> queue =
	new LinkedList<TaskQueueSimEvent<Parameter>>();

    protected int getSize() {return queue.size();}

    protected boolean offerToQueue(TaskQueueSimEvent<Parameter>
				   event,
				   TaskQueueSimEvent<Parameter>
				   scheduled)
    {
	return queue.offer(event);
    }

    protected TaskQueueSimEvent<Parameter> pollFromQueue() {
	return queue.poll();
    }
}

//  LocalWords:  AbstractWaitTaskQueue TaskQueue releaseUpTo boolean
//  LocalWords:  clearReleaseCount subclasses setCanFreeze tpriority
//  LocalWords:  WaitTaskQueue DelayTaskQueue SimulationEvent
//  LocalWords:  IllegalArgumentException scriptObject Runnable
//  LocalWords:  runnable SimEventCallable FifoTaskQueue
