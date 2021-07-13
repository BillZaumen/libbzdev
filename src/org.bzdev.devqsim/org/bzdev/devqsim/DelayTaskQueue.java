package org.bzdev.devqsim;
import org.bzdev.lang.Callable;

/**
 * Common code for FIFO and LIFO delay queues.
 */
abstract public class DelayTaskQueue
    extends TaskQueue<DelayTaskQueue.Parameter>
{

    /**
     * Type parameter required by TaskQueue.
     */
    public static class Parameter {
	long interval;
	double tpriority;

	/**
	 * Constructor.
	 * @param interval the processing-time interval needed to process
	 *        an event when it is scheduled
	 */
	public Parameter(long interval) {
	    this(interval, 0.0);
	}

	/**
	 * Constructor with a time event priority.
	 * @param interval the processing-time interval needed to process
	 *        an event when it is scheduled
	 * @param tpriority the time event priority when the event is scheduled
	 */
	public Parameter(long interval, double tpriority) {
	    this.interval = interval;
	    this.tpriority = tpriority;
	}
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public DelayTaskQueue(Simulation sim, boolean intern) {
	super(sim, null, intern);
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
    public DelayTaskQueue(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Add a Callable to the queue given a processing-time interval.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param interval the processing-time interval for the callable,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(Callable callable, long interval) 
    {
	return doAdd(callable, new DelayTaskQueue.Parameter(interval),
		     interval, 0.0);
    }

    /**
     * Add a Callable to the queue given a processing-time interval and
     * a time event priority.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param interval the processing-time interval for the callable,
     *                 representing the service time for this queue entry
     * @param tpriority the time event priority for the callable
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(Callable callable, long interval, double tpriority)
    {
	return doAdd(callable,
		     new DelayTaskQueue.Parameter(interval, tpriority),
		     interval, tpriority);
    }

    /**
     * Add a Callable bound to a simulation object to the queue given
     * a processing-time interval.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param interval the processing-time interval for the callable,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(SimObjectCallable callable, long interval) 
    {
	return doAdd(callable, new DelayTaskQueue.Parameter(interval),
		     interval, 0.0);
    }

    /**
     * Add a Callable bound to a simulation object to the queue given
     * a processing-time interval and time event priority.
     * When the Callable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the Callable to add
     * @param interval the processing-time interval for the callable,
     *                 representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(SimObjectCallable callable, long interval, double tpriority)
    {
	return doAdd(callable,
		     new DelayTaskQueue.Parameter(interval, tpriority),
		     interval, tpriority);
    }

    /**
     * Add a script to the queue given a processing-time interval.
     * When the script is scheduled and the corresponding event is
     * processed, the script will be evaluated.
     * @param script the script to add to the queue
     * @param interval the processing-time interval for the script,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	addCallScript(String script, long interval)
    {
	return doAddCallScript(script,
			       new DelayTaskQueue.Parameter(interval),
			       interval, 0.0);
    }

    /**
     * Add a script to the queue given a processing-time interval and a
     * time event priority.
     * When the script is scheduled and the corresponding event is
     * processed, the script will be evaluated.
     * @param script the script to add to the queue
     * @param interval the processing-time interval for the script,
     *                 representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	addCallScript(String script, long interval, double tpriority)
    {
	return doAddCallScript(script,
			       new DelayTaskQueue.Parameter(interval,
							    tpriority),
			       interval, tpriority);
    }


    /**
     * Add a script object to execute to the queue given a
     * processing-time interval.
     * When the script is scheduled and the corresponding event is
     * processed, the script object's <code>call</code> method will 
     * be executed.
     * @param scriptObject the script object to add
     * @param interval the processing-time interval for the scriptObject
     *        argument, representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	addCallObject(Object scriptObject, long interval)
    {
	return doAddCallObject(scriptObject,
			       new DelayTaskQueue.Parameter(interval),
			       interval, 0.0);
    }

    /**
     * Add a script object to execute to the queue given a
     * processing-time interval and time event priority.
     * When the script is scheduled and the corresponding event is
     * processed, the script object's <code>call</code> method will
     * be executed.
     * @param scriptObject the script object to add
     * @param interval the processing-time interval for the scriptObject
     *        argument, representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Callable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	addCallObject(Object scriptObject, long interval, double tpriority)
    {
	return doAddCallObject(scriptObject,
			       new DelayTaskQueue.Parameter(interval,
							    tpriority),
			       interval, tpriority);
    }


    /**
     * Add a Runnable to the queue given a processing-time interval.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param interval the processing-time interval for the Runnable,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(Runnable runnable, long interval)
    {
	return doAdd(runnable,
		     new DelayTaskQueue.Parameter(interval),
		     interval, 0.0);
    }
    /**
     * Add a Runnable to the queue given a processing-time interval and
     * time event priority.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param interval the processing-time interval for the Runnable,
     *                 representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(Runnable runnable, long interval, double tpriority)
    {
	return doAdd(runnable,
		     new DelayTaskQueue.Parameter(interval, tpriority),
		     interval, tpriority);
    }


    /**
     * Add a Runnable bound to a simulation object to the queue given
     * a processing-time interval.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param interval the processing-time interval for the Runnable,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(SimObjectRunnable runnable, long interval) 
    {
	return doAdd(runnable, new DelayTaskQueue.Parameter(interval, 0.0),
		     interval, 0.0);
    }

    /**
     * Add a Runnable bound to a simulation object to the queue given
     * a processing-time interval and time event priority.
     * When the Runnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the Runnable to add to the queue
     * @param interval the processing-time interval for the Runnable,
     *                 representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<DelayTaskQueue.Parameter>
	add(SimObjectRunnable runnable, long interval, double tpriority)
    {
	return doAdd(runnable,
		     new DelayTaskQueue.Parameter(interval, tpriority),
		     interval, tpriority);
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
     * @param interval the processing-time interval for the script,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskScript(String script,
						      long interval)
    {
	return doAddTaskScript(script, new Parameter(interval, 0.0),
			       interval, 0.0);
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
     * @param interval the processing-time interval for the script,
     *                 representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskScript(String script,
						      long interval,
						      double tpriority)
    {
	return doAddTaskScript(script, new Parameter(interval, tpriority),
			       interval, tpriority);
    }

    /**
     * Add a script object to be run as a task to the queue given a
     * processing-time interval.
     * When the object is scheduled and the corresponding event is
     * processed, a task thread will be started and the object's
     * <code>run()</code> method will be called.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param scriptObject the object representing a task to be added 
     *         to the queue
     * @param interval the processing-time interval for scriptObject,
     *                 representing the service time for this queue entry
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskObject(Object scriptObject,
						      long interval)
    {
	return doAddTaskObject(scriptObject, new Parameter(interval, 0.0),
			       interval, 0.0);
    }

    /**
     * Add a script object to be run as a task to the queue given a
     * processing-time interval and time event priority.
     * When the object is scheduled and the corresponding event is
     * processed, a task thread will be started and the object's
     * <code>run()</code> method will be called.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param scriptObject the object representing a task to be added
     *         to the queue
     * @param interval the processing-time interval for scriptObject,
     *                 representing the service time for this queue entry
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the Runnable cannot
     *         be queued
     */
    public TaskQueueSimEvent<Parameter> addTaskObject(Object scriptObject,
						      long interval,
						      double tpriority)
    {
	return doAddTaskObject(scriptObject,
			       new Parameter(interval, tpriority),
			       interval, tpriority);
    }

    protected long getInterval(Parameter parameter) {
	return parameter.interval;
    }

    protected double getTPriority(Parameter parameter) {
	return parameter.tpriority;
    }


    /*
    protected long parseForInterval(DelayTaskQueue.Parameter parameter) {
	return parameter.intValue();
    }
    */


    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(long delay)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(delay, 0.0), null);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and time event priority.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(long delay, double tpriority)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(delay, tpriority), null);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and SimEventCallable.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param callable a SimEventCallable used to provide access to
     *        the event representing the queued task
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(long delay, SimEventCallable callable)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(delay, 0.0), callable);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval, time event priority, and SimEventCallable.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @param callable a SimEventCallable used to provide access to
     *        the event representing the queued task
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(long delay, double tpriority,
				  SimEventCallable callable)
	throws IllegalStateException
    {
	return addCurrentTask(new Parameter(delay, tpriority), callable);
    }

    // following two needed to avoid confusing scripting languages
    // regarding type casting.
    /**
     * Given a processing-time interval, add the currently running
     * task thread to a queue and return the SimulationEvent generated.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject(long delay)
	throws IllegalStateException
    {
	return addCurrentTaskObject(new Parameter(delay, 0.0));
    }

    /**
     * Given a processing-time interval and time event priority,
     * add the currently running
     * task thread to a queue and return the SimulationEvent generated.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject(long delay, double tpriority)
	throws IllegalStateException
    {
	return addCurrentTaskObject(new Parameter(delay, tpriority));
    }

}

//  LocalWords:  TaskQueue tpriority IllegalArgumentException
//  LocalWords:  SimulationEvent scriptObject Runnable runnable
//  LocalWords:  parseForInterval DelayTaskQueue intValue
//  LocalWords:  SimEventCallable
