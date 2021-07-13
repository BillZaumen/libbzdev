package org.bzdev.devqsim;
import  org.bzdev.lang.Callable;
import java.util.*;

/**
 * Priority task queue.  Entries are taken off the queue according to
 * a priority. For two entries with the same priority, FIFO order is
 * used.
 */
public class PriorityTaskQueue 
    extends TaskQueue<PriorityTaskQueue.PriorityParam> {
    
    /**
     * Type Parameter for priority queues.
     */
    public static class PriorityParam {
	static long nextInstance = 0;
	int priority;
	long instance;
	long interval;
	/**
	 * Constructor.
	 * @param p the priority
	 * @param i the interval
	 */
	PriorityParam(int p, long i) {
	    priority = p;
	    interval = i;
	    instance = nextInstance++;
	}
    }

    Simulation sim;

    /**
     * Constructor for unnamed priority task queues.
     * If interned, a name will be automatically generated.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public PriorityTaskQueue(Simulation sim, boolean intern) {
	super(sim, null, intern);
	this.sim = sim;
    }


    /**
     * Constructor for named priority task queues.
     * @param sim the simulation
     * @param name the name for the queue
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public PriorityTaskQueue(Simulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.sim = sim;
    }


    /**
     * Add a Callable to the queue.
     * @param callable the Callable to add
     * @param priority the priority for the Callable to be added
     * @param interval the interval between when callable is scheduled
     *                 to be called and when it is actually called,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created
     */
    public SimulationEvent
	add(Callable callable, int priority, long interval) 
    {
	return doAdd(callable, new PriorityParam(priority, interval),
		     interval, 0.0);
    }

    /**
     * Add a SimObjectCallable to the queue.
     * @param callable the SimObjectCallable to add
     * @param priority the priority for the Callable to be added
     * @param interval the interval between when callable is scheduled
     *                 to be called and when it is actually called,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created
     */
    public SimulationEvent
	add(SimObjectCallable callable, int priority, long interval) 
    {
	return doAdd(callable, new PriorityParam(priority, interval),
		     interval, 0.0);
    }


    /**
     * Add a script to execute to the queue.
     * When the script reaches the head of the queue and is then processed,
     * it will be evaluated.
     * @param script the script to add to the queue
     * @param priority the priority for the Callable to be added
     * @param interval the interval between when callable is scheduled
     *                 to be called and when it is actually called,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the script cannot be
     *         queued
     */
    public SimulationEvent
	addCallScript(String script, int priority, long interval) 
    {
	return doAddCallScript(script, new PriorityParam(priority, interval),
			       interval, 0.0);
    }

    /**
     * Add a script object to execute to the queue. When the object reaches
     * the head of the queue and is scheduled, it's <code>call()</code> method
     * will be invoked.
     * @param scriptObject the script object to add
     * @param priority the priority for the Callable to be added
     * @param interval the interval between when callable is scheduled
     *                 to be called and when it is actually called,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created
     */
    public SimulationEvent
	addCallObject(Object scriptObject, int priority, long interval) 
    {
	return doAddCallObject(scriptObject,
			       new PriorityParam(priority, interval),
			       interval, 0.0);
    }


    /**
     * Add a Runnable to the queue.
     * @param runnable the Runnable to add to the queue
     * @param priority the priority for Runnable to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created
     */
    public SimulationEvent
	add(Runnable runnable, int priority,  long interval) 
    {
	return doAdd(runnable, new PriorityParam(priority, interval),
		     interval, 0.0);
    }


    /**
     * Add a SimObjectRunnable to the queue.
     * @param runnable the SimObjectRunnable to add to the queue
     * @param priority the priority for Runnable to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created
     */
    public SimulationEvent
	add(SimObjectRunnable runnable, int priority,  long interval) 
    {
	return doAdd(runnable, new PriorityParam(priority, interval),
		     interval, 0.0);
    }


    /**
     * Add a script to run in a task to the queue.
     * @param script the script to add to the queue
     * @param priority the priority for Runnable to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the script could not be
     *         queued
     */
    public SimulationEvent
	addTaskScript(String script, int priority,  long interval) 
    {
	return doAddTaskScript(script, new PriorityParam(priority, interval),
			       interval, 0.0);
    }


    /**
     * Add a script object to run in a task to the queue.
     * @param scriptObject the object to add to the queue
     * @param priority the priority for Runnable to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the object could not be
     *         queued
     */
    public SimulationEvent
	addTaskObject(Object scriptObject, int priority,  long interval) 
    {
	return doAddTaskObject(scriptObject,
			       new PriorityParam(priority, interval),
			       interval, 0.0);
    }


    /**
     * Add the currently running task thread to a priority queue.
     * @param priority the priority for task to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     */
    public void addCurrentTask(int priority, long interval)
	throws IllegalStateException 
    {
	addCurrentTask(new PriorityParam(priority, interval), null);
    }


    /**
     * Add the currently running task thread to a priority queue allowing
     * for cancellation.
     * @param priority the priority for task thread to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     */
    public void addCurrentTask(int priority, long interval, 
			       SimEventCallable callable)
	throws IllegalStateException 
    {
	addCurrentTask(new PriorityParam(priority, interval), callable);
    }


    /**
     * Add the currently running task thread to a queue and return the
     * SimulationEvent generated.
     * @param priority the priority for task thread to be added
     * @param interval the interval between when runnable is scheduled
     *                 to be started and when it is actually started,
     *                 representing the service time for this queue entry.
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject(int priority, long interval)
	throws IllegalStateException
    {
	return super.addCurrentTaskObject(new PriorityParam(priority,interval));
    }



    protected long getInterval(PriorityParam parameter) {
	return parameter.interval;
    }

    /*
    protected long parseForInterval(PriorityParam parameter) {
	return parameter.interval;
    }
    */

    static class PriorityParamComparator 
	implements Comparator<TaskQueueSimEvent<PriorityParam>>
    {
	public int compare(TaskQueueSimEvent<PriorityParam> p1,
			   TaskQueueSimEvent<PriorityParam> p2) 
	    throws ClassCastException
	{
	    PriorityParam pp1 = p1.parameters;
	    PriorityParam pp2 = p2.parameters;

	    if (pp1.priority != pp2.priority) {
		return pp1.priority - pp2.priority;
	    } else {
		if (pp1.instance == pp2.instance)
		    return 0;
		else 
		    return ((pp1.instance - pp2.instance) < 0)? -1: 1 ;
	    }
	}
	public boolean equals(TaskQueueSimEvent<PriorityParam> p1,
			      TaskQueueSimEvent<PriorityParam> p2)
	{
	    PriorityParam pp1 = p1.parameters;
	    PriorityParam pp2 = p2.parameters;
	    return (pp1.priority == pp2.priority)
		&& (pp1.instance == pp2.instance);
	}
    }

    static PriorityParamComparator comparator =
	new PriorityParamComparator();


    // default size of a PriorityQueue is 11 according to Java documentation
    PriorityQueue<TaskQueueSimEvent<PriorityParam>> pq = 
	new PriorityQueue<TaskQueueSimEvent<PriorityParam>>(11, comparator);

    protected int getSize() {return pq.size();}

    public boolean canPreempt() {return true;}

    protected boolean offerToQueue(TaskQueueSimEvent<PriorityParam> event,
				   TaskQueueSimEvent<PriorityParam> scheduled)
    {
	if (scheduled != null) {
	    // signals that we can preempt this one.
	    if (event.parameters.priority < scheduled.parameters.priority) {
		long interval;
		if (scheduled.isPending()) {
			interval = sim.currentTicks()-scheduled.offQueueTime;
		} else {
		    interval = 0;
		}
		scheduled.parameters.interval -= interval;
		if (scheduled.parameters.interval < 0)
		    scheduled.parameters.interval = 0;
		replaceScheduledEvent(event);
		return pq.offer(scheduled);
	    } else {
		return pq.offer(event);
	    }
	} else {
	    return pq.offer(event);
	}
    }

    protected TaskQueueSimEvent<PriorityParam> pollFromQueue() {
	return pq.poll();
    }
}

//  LocalWords:  IllegalArgumentException SimulationEvent Runnable
//  LocalWords:  SimObjectCallable scriptObject runnable
//  LocalWords:  SimObjectRunnable parseForInterval PriorityParam
//  LocalWords:  PriorityQueue
