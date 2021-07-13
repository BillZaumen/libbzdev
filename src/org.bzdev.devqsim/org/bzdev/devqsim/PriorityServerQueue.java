package org.bzdev.devqsim;

/**
 * Priority server queue.
 * This implements a ServerQueue in which the entries are processed in
 * FIFO (First In, First Out) order for each priority.  Lower priority
 * values are used first.
 * 
 * @see org.bzdev.devqsim.ServerQueue
 */

public class PriorityServerQueue<QS extends QueueServer> 
    extends ServerQueue<PriorityTaskQueue.PriorityParam, QS> 
{
    /**
     * Constructor for unnamed priority-server-queues.
     * If interned, a name will be automatically generated.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @param servers the queue's servers
     */
    @SuppressWarnings("unchecked")
    public PriorityServerQueue(Simulation sim, boolean intern,
			       QS... servers) {
	super(sim, null, intern, new PriorityTaskQueue(sim, false) {
		protected void init() {
		    setCanFreeze(true);
		}
	    }, servers);
    }

    /**
     * Constructor for named priority-server queues.
     * @param sim the simulation
     * @param name the name of the queue
     * @param intern true if the queue name should be interned in the
     *        simulation tables; false otherwise
     * @param servers the queue's servers
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    @SuppressWarnings("unchecked")
    public PriorityServerQueue(Simulation sim, String name,
			       boolean intern,
			       QS... servers) 
	throws IllegalArgumentException
    {
	super(sim, name, intern, new PriorityTaskQueue(sim, false) {
		protected void init() {
		    setCanFreeze(true);
		}
	    }, servers);
    }

    /**
     * Add a QueueCallable to the queue.
     * When the QueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the QueueCallable to add
     * @param priority the priority for the Callable to be added
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent add(QueueCallable<QS> callable, 
			       int priority, long delay) 
    {
	return doAdd(callable, 
		     new PriorityTaskQueue.PriorityParam(priority, delay));
    }

    /**
     * Add a SimObjQueueCallable to the queue.
     * When the SimObjQueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the QueueCallable to add
     * @param priority the priority for the Callable to be added
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueCallable<QS> callable, 
			       int priority, long delay) 
    {
	return doAdd(callable, 
		     new PriorityTaskQueue.PriorityParam(priority, delay));
    }

    /**
     * Queue an event given a script object that implements the QueueCallable
     * interface.
     * The method's first argument is an 
     * object defined in the simulation's scripting language that implements
     * two methods: an <code>interactWith</code> method that takes a server 
     * as an argument (the server's type is set by the type parameter QS)
     * and a <code>call</code> method that has no arguments.  
     * The <code>interactWith</code> method will  be called first, followed 
     * by the <code>call</code> method.
     * @param scriptObject the object specifying the task to be queued
     * @param priority the priority for the Runnable to be added
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent addCallScriptObject(Object scriptObject,
					       int priority, long delay)
    {
	return doAddCallObject
	    (scriptObject,
	     new PriorityTaskQueue.PriorityParam(priority, delay));
    }

    /**
     * Add a QueueRunnable to the queue.
     * When the QueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the QueueRunnable to add to the queue
     * @param priority the priority for the Runnable to be added
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(QueueRunnable<QS> runnable, 
			       int priority, long delay) 
    {
	return doAdd(runnable, 
		     new PriorityTaskQueue.PriorityParam(priority, delay));
    }

    /**
     * Add a SimObjQueueRunnable to the queue.
     * When the SimObjQueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the SimObjQueueRunnable to add to the queue
     * @param priority the priority for the Runnable to be added
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueRunnable<QS> runnable, 
			       int priority, long delay) 
    {
	return doAdd(runnable, 
		     new PriorityTaskQueue.PriorityParam(priority, delay));
    }


    /**
     * Queue an event given a script object that provides QueueRunnable
     * interface.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a QueueServer as its argument
     * and a method with no arguments named <code>run</code>, which will be
     * executed in that order in a task thread.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     *
     * @param scriptObject the script object specifying the task to be queued.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent addTaskScriptObject(Object scriptObject,
					int priority, long delay)
    {
	return doAddTaskObject
	    (scriptObject, new PriorityTaskQueue.PriorityParam(priority,delay));
    }


    /**
     * Add the currently running task thread to a queue.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler, 
				  int priority, long delay)
    {
	return super.addCurrentTask
	    (handler, new PriorityTaskQueue.PriorityParam(priority, delay),
	     null);
    }

    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes.
     * @param scriptObject the script object specifying the task to be queued.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return A simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
					      int priority, long delay)
    {
	return
	    addCurrentTaskScriptObject
	    (scriptObject, new PriorityTaskQueue.PriorityParam(priority,delay),
	     null);
    }

    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes. The
     * scriptSimEventCallable argument is an object that implements a method
     * named call that takes a single argument, a SimulationEvent. Typically
     * this argument will be stored in case it is necessary to remove the
     * task from the queue and restart it.
     * @param scriptObject the script object specifying the task to be queued.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param scriptSimEventCallable the script object that obtains a
     *        SimulationEvent that can be used to remove the task from
     *        the queue
     * @return A simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
					      int priority, long delay,
					      final Object
					      scriptSimEventCallable)
    {
	return
	    addCurrentTaskScriptObject
	    (scriptObject, new PriorityTaskQueue.PriorityParam(priority,delay),
	     null);
    }

    /**
     * Add the currently running task thread to a queue.
     * This method is provided because autoboxing cannot change
     * an int into a Long.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true on success; false if the task cannot be placed on
     *         the queue
    public boolean addCurrentTask(int priority, long delay) {
	return super.addCurrentTask
	    (null, new PriorityTaskQueue.PriorityParam(priority, delay), null);
    }
     */

    /**
     * Add the currently running task thread to a queue, returning a
     * SimulationEvent.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return the simulation event on success; null on failure in which 
     *         case the current thread will continue to run
    public SimulationEvent addCurrentTaskObject(int priority, long delay) {
	return super.addCurrentTaskObject
	    (new PriorityTaskQueue.PriorityParam(priority, delay));
    }
     */



    /**
     * Add the currently running task thread to a queue.
     * This method is provided because autoboxing cannot change
     * an int into a Long.
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
    public boolean addCurrentTask(int priority, long delay,
				  SimEventCallable callable) 
    {
	return super.addCurrentTask
	    (null, 
	     new PriorityTaskQueue.PriorityParam(priority, delay),
	     callable);
    }
     */

    /**
     * Add the currently running task thread to a queue.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param priority the priority for the task to be added
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler, 
				  int priority, long delay,
				  SimEventCallable callable)
    {
	return super.addCurrentTask
	    (handler,
	     new PriorityTaskQueue.PriorityParam(priority, delay),
	     callable);
    }
}


//  LocalWords:  ServerQueue IllegalArgumentException QueueCallable
//  LocalWords:  SimulationEvent SimObjQueueCallable interactWith QS
//  LocalWords:  scriptObject Runnable runnable QueueRunnable boolean
//  LocalWords:  SimObjQueueRunnable QueueServer QueueServerHandler
//  LocalWords:  scriptSimEventCallable autoboxing addCurrentTask
//  LocalWords:  PriorityTaskQueue PriorityParam addCurrentTaskObject
//  LocalWords:  SimEventCallable
