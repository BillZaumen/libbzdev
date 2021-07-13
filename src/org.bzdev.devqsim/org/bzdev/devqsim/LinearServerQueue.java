package org.bzdev.devqsim;

/**
 * Linear server queue.
 * This implements a ServerQueue in which the entries are processed in
 * linear order such as FIFO (First In, First Out) order.
 *
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.ServerQueue
 * @see org.bzdev.devqsim.QueueServer
 * 
 */

abstract public class LinearServerQueue<QS extends QueueServer> 
    extends ServerQueue<DelayTaskQueue.Parameter, QS>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the queue
     * @param intern true if the queue name should be interned in the
     *        simulation tables; false otherwise
     * @param tq the task queue used to queue requests when all the servers
     *        are busy
     * @param servers the queue's servers
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    @SuppressWarnings("unchecked")
    protected LinearServerQueue(Simulation sim, String name,
				boolean intern,
				TaskQueue<DelayTaskQueue.Parameter> tq,
				QS... servers) 
	throws IllegalArgumentException
    {
	super(sim, name, intern, tq, servers);
    }

    /**
     * Add a QueueCallable to the queue given a processing-time interval.
     * When the QueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the QueueCallable to add
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */

    public SimulationEvent add(QueueCallable<QS> callable,long delay) {
	return doAdd(callable, new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Add a QueueCallable to the queue given a processing-time interval
     * and time event priority.
     * When the QueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the QueueCallable to add
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent add(QueueCallable<QS> callable,
			       long delay, double tpriority)
    {
	return doAdd(callable, new DelayTaskQueue.Parameter(delay, tpriority));
    }


    /**
     * Add a SimObjQueueCallable to the queue given a processing-time interval.
     * When the SimObjQueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the SimObjQueueCallable to add
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueCallable<QS> callable,long delay) {
	return doAdd(callable, new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Add a SimObjQueueCallable to the queue given a processing-time interval
     * and a time event priority.
     * When the SimObjQueueCallable is scheduled and the corresponding event is
     * processed, its call() method will be executed.
     * @param callable the SimObjQueueCallable to add
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueCallable<QS> callable,
			       long delay, double tpriority)
    {
	return doAdd(callable, new DelayTaskQueue.Parameter(delay, tpriority));
    }

    /**
     * Queue an event providing a script object given a processing-time
     * interval.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.  The method's first argument is an 
     * object defined in the simulation's scripting language that implements
     * two methods: an <code>interactWith</code> method that takes a server 
     * as an argument (the server's type is set by the type parameter QS)
     * and a <code>call</code> method that has no arguments.  
     * The <code>interactWith</code> method will  be called first, followed 
     * by the <code>call</code> method.
     * @param scriptObject the object specifying the task to be queued
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent addCallObject(Object scriptObject, long delay) {
	return doAddCallObject(scriptObject,
			       new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Queue an event providing a script object given a processing-time
     * interval and time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.  The method's first argument is an
     * object defined in the simulation's scripting language that implements
     * two methods: an <code>interactWith</code> method that takes a server
     * as an argument (the server's type is set by the type parameter QS)
     * and a <code>call</code> method that has no arguments.
     * The <code>interactWith</code> method will  be called first, followed
     * by the <code>call</code> method.
     * @param scriptObject the object specifying the task to be queued
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    public SimulationEvent addCallObject(Object scriptObject,
					 long delay, double tpriority)
    {
	return doAddCallObject(scriptObject,
			       new DelayTaskQueue.Parameter(delay, tpriority));
    }


    /**
     * Add a QueueRunnable to the queue given a processing-time interval.
     * When the QueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the QueueRunnable to add to the queue
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(QueueRunnable<QS> runnable, long delay) {
	return doAdd(runnable, new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Add a QueueRunnable to the queue given a processing-time interval
     * and time event priority.
     * When the QueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the QueueRunnable to add to the queue
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(QueueRunnable<QS> runnable,
			       long delay, double tpriority)
    {
	return doAdd(runnable, new DelayTaskQueue.Parameter(delay, tpriority));
    }

    /**
     * Add a SimObjQueueRunnable to the queue given a processing-time interval.
     * When the SimObjQueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the SimObjQueueRunnable to add to the queue
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueRunnable<QS> runnable, long delay) {
	return doAdd(runnable, new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Add a SimObjQueueRunnable to the queue given a processing-time interval
     * and time event priority.
     * When the SimObjQueueRunnable is scheduled and the corresponding event is
     * processed, a task thread will be started executing the Runnable.
     * Task threads and the simulation's thread run one at a time, including
     * on a multiprocessor.
     * @param runnable the SimObjQueueRunnable to add to the queue
     * @param delay the interval between when runnable is scheduled
     *              to be started and when it is actually started,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent add(SimObjQueueRunnable<QS> runnable,
			       long delay, double tpriority)
    {
	return doAdd(runnable, new DelayTaskQueue.Parameter(delay, tpriority));
    }

    /**
     * Queue an event given a script object that provides QueueRunnable
     * interface and given a processing-time interval.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a QueueServer as its argument
     * and a method with no arguments named <code>run</code>, which will be
     * executed in that order in a task thread.
     * @param scriptObject the script object defining the task and its
     *        interaction with a server
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent addTaskObject(Object scriptObject, long delay) {
	return doAddTaskObject(scriptObject,
			       new DelayTaskQueue.Parameter(delay, 0.0));
    }

    /**
     * Queue an event given a script object that provides QueueRunnable
     * interface and given a processing-time interval and time event priority.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a QueueServer as its argument
     * and a method with no arguments named <code>run</code>, which will be
     * executed in that order in a task thread.
     * @param scriptObject the script object defining the task and its
     *        interaction with a server
     * @param delay the interval between when callable is scheduled
     *              to be called and when it is actually called,
     *              representing the service time for this queue entry.
     * @param tpriority the time event priority
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    public SimulationEvent addTaskObject(Object scriptObject,
					 long delay, double tpriority)
    {
	return doAddTaskObject(scriptObject,
			       new DelayTaskQueue.Parameter(delay, tpriority));
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler, long delay) {
	return addCurrentTask(handler,
			      new DelayTaskQueue.Parameter(delay, 0.0),
			      null);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and time event priority.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler,
				  long delay, double tpriority)
    {
	return addCurrentTask(handler,
			      new DelayTaskQueue.Parameter(delay, tpriority),
			      null);
    }

    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface and
     *  given a processing-time interval.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes.
     * @param scriptObject the script object specifying the task to be queued.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true if the task was restarted by the queue; false if it
     *         was canceled or could not be queued
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
					      long delay)
    {
	return
	    addCurrentTaskScriptObject(scriptObject,
				       new DelayTaskQueue.Parameter(delay, 0.0),
				       null);
    }

    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface and
     *  given a processing-time interval.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes. The
     * scriptSimEventCallable argument is an object that implements a method
     * named call that takes a single argument, a SimulationEvent. Typically
     * this argument will be stored in case it is necessary to remove the
     * task from the queue and restart it.
     * @param scriptObject the script object specifying the task to be queued.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true if the task was restarted by the queue; false if it
     *         was canceled or could not be queued
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
					      long delay,
					      Object scriptSimEventCallable)
    {
	return
	    addCurrentTaskScriptObject(scriptObject,
				       new DelayTaskQueue.Parameter(delay, 0.0),
				       scriptSimEventCallable);
    }


    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface and
     * given a processing-time interval and time event priority.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes.
     * @param scriptObject the script object specifying the task to be queued.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @return true if the task was restarted by the queue; false if it
     *         was canceled or could not be queued
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
						long delay, double tpriority)
    {
	return
	    addCurrentTaskScriptObject(scriptObject,
				       new DelayTaskQueue.Parameter(delay,
								    tpriority),
				       null);
    }

    /**
     * Add the currently running task thread to a queue, specifying a
     * script object to implement the QueueServerHandler interface and
     * given a processing-time interval and time event priority in addition
     * to an object implementing the SimEventCallable interface.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a single argument, a QueueServer,
     * and this method will be called before the task resumes. The
     * scriptSimEventCallable argument is an object that implements a method
     * named call that takes a single argument, a SimulationEvent. Typically
     * this argument will be stored in case it is necessary to remove the
     * task from the queue and restart it.
     * @param scriptObject the script object specifying the task to be queued.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @param scriptSimEventCallable the script object that obtains a
     *        SimulationEvent that can be used to remove the task from
     *        the queue
     * @return true if the task was restarted by the queue; false if it
     *         was canceled or could not be queued
     */
    public boolean addCurrentTaskScriptObject(Object scriptObject,
					      long delay, double tpriority,
					      Object scriptSimEventCallable)
    {
	return
	    addCurrentTaskScriptObject(scriptObject,
				       new DelayTaskQueue.Parameter(delay,
								    tpriority),
				       scriptSimEventCallable);
    }


    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @return true on success; false if the task cannot be placed on
     *         the queue
    public boolean addCurrentTask(long delay) {
	return addCurrentTask(null,
			      new DelayTaskQueue.Parameter(delay, 0.0),
			      null);
    }
     */

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and time event priority.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @return true on success; false if the task cannot be placed on
     *         the queue
    public boolean addCurrentTask(long delay, double tpriority) {
	return addCurrentTask(null,
			      new DelayTaskQueue.Parameter(delay, tpriority),
			      null);
    }
     */

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
    public boolean addCurrentTask(long delay, SimEventCallable callable) {
	return addCurrentTask(null,
			      new DelayTaskQueue.Parameter(delay, 0.0),
			      callable);
    }
     */

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and time event priority.
     * @param delay the delay to wait before the task is
     *              restarted once scheduled
     * @param tpriority the time event priority
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(long delay, double tpriority,
				  SimEventCallable callable)
    {
	return addCurrentTask(null,
			      new DelayTaskQueue.Parameter(delay, tpriority),
			      callable);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler, long delay,
				  SimEventCallable callable) {
	return addCurrentTask(handler,
			      new DelayTaskQueue.Parameter(delay, 0.0),
			      callable);
    }

    /**
     * Add the currently running task thread to a queue given a
     * processing-time interval and time event priority.
     * @param handler the handler whose <code>interactWith</code> method
     *                will be called with a queue server as its argument
     *                when the queue server handles the queue entry being
     *                processed
     * @param delay the delay to wait before the task is
     *              restarted once scheduled.
     * @param tpriority the time event priority
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false if the task cannot be placed on
     *         the queue
     */
    public boolean addCurrentTask(QueueServerHandler<QS> handler,
				  long delay, double tpriority,
				  SimEventCallable callable) {
	return addCurrentTask(handler,
			      new DelayTaskQueue.Parameter(delay, tpriority),
				    callable);
    }
}

//  LocalWords:  ServerQueue tq IllegalArgumentException tpriority QS
//  LocalWords:  QueueCallable SimulationEvent SimObjQueueCallable
//  LocalWords:  interactWith scriptObject QueueRunnable Runnable
//  LocalWords:  runnable SimObjQueueRunnable QueueServer boolean
//  LocalWords:  QueueServerHandler scriptSimEventCallable
//  LocalWords:  SimEventCallable addCurrentTask DelayTaskQueue
