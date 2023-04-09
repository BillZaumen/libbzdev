package org.bzdev.devqsim;
import org.bzdev.lang.Callable;
import org.bzdev.lang.CallableReturns;

import java.util.Set;
import java.util.HashSet;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Bindings;
import java.security.*;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Task-queue class.  This class enables tasks to be queued for later
 * execution (or for a running task-thread to pause.  It provides a
 * framework that distinguishes between the currently active task and
 * ones that are queued.  Subclasses define the type of queue and its
 * implementation. Entries in the queue are characterized by two
 * quantities:
 * <ul>
 *  <li> A <i> Processing-time Interval.</i>  When an element taken off
 *       a queue for processing, this interval determines the time to
 *       wait before processing of that element starts. The element will
 *       be scheduled on the simulation's event queue with a delay set to
 *       the value of the processing-time interval.
 *  <li> A <i>Time Event Priority.</i> This determines the priority to use
 *       when an element is scheduled.  The default is 0.0.
 *  </ul>
 * <p>
 * The generic type T is the type of a parameter that defines
 * each entry in the queue (the parameter will typically have multiple fields
 * contained in it).  A subclass is responsible for providing
 * methods that return the processing-time interval for a parameter
 * and its time event priority.
 * <P>
 * Subclasses will typically define a number of methods named "add" to
 * add entries to a queue.  After processing the arguments, these methods
 * will typically just return the value obtained by calling either doAdd,
 * doAddCallScript, doAddCallObject, or doAddTaskScript. In addition, the
 * subclass should implement versions of addCurrentTask that create the
 * parameter used as the first argument to
 * {@link #addCurrentTask(Object) addCurrentTask(T)} and
 * {@link #addCurrentTask(Object,org.bzdev.devqsim.SimEventCallable) addCurrentTask(T, SimEventCallable)}.
 * As with the methods whose name starts with 'doAdd', The subclasses' versions
 * of these methods should simply create a parameter from the method's arguments
 * and then call the appropriate addCurrentTask method defined in TaskQueue.
 * A subclass created for use by an application only has to implement
 * the 'add' methods it needs.  if the subclass will be put in a
 * library, one should implement a full set of methods.
 * <P>
 * There are two protected methods used to configure the queue:
 * <ul>
 *        {@link #setCanFreeze(boolean)}
 *        {@link #setCanRelease(boolean)}
 * </ul>
 * These methods would normally be called by a constructor. For an anonymous
 * class, the protected method {@link #init} can be defined. The default
 * implementation of {@link #init} does nothing but is called by TaskQueue's
 * constructor.  Overriding it will allow additional initializations. The
 * {@link #init} method is used by subclasses of {@link ServerQueue} defined in
 * the org.bzdev.devqsim package to create task queues that can be frozen
 * (these task queues are part of the server queues' implementations).
 * <P>
 * A subclass also has to implement a queue.  This will typically be done
 * by using a queue or list from the java.util package.  The subclass must
 * implement the method {@link #getSize() getSize()} to return the queue
 * size, the method
 * {@link #offerToQueue(TaskQueueSimEvent,TaskQueueSimEvent) offerToQueue}
 * to place entries on the queue, and the method
 * {@link #pollFromQueue() pollFromQueue()} to take an entry off of the queue.
 * For some classes (e.g., LifoTaskQueue), which supports preempt mode, it
 * is sometimes necessary to put a scheduled event back on the queue, adjusting
 * its interval to account for the time it was on the event queue. When this
 * is the case, the second argument to offerToQueue will be a non-null
 * {@link TaskQueueSimEvent TaskQueueSimEvent} and will contain the currently
 * scheduled event. For this scheduled event, the methods
 * {@link #getOffQueueTime(TaskQueueSimEvent) getOffQueueTime} and
 * {@link #getEventParameters(TaskQueueSimEvent) getEventParameters} may be
 * useful in computing a new interval and modifying the event parameters
 * to account for that before the event is placed back on the queue.
 * <P>
 * Task queue entries can be canceled provided they are on a queue as
 * opposed to being scheduled to run. Threads are treated differently
 * from instances of Callable or Runnable.
 * <P>
 * When a Callable or a Runnable is added to a task queue, a
 * SimulationEvent will be returned.  If this event is null, that
 * indicates that the attempt to add the Callable or Runnable to the
 * queue failed (e.g., if the queue has a capacity limit).  If this
 * event is non-null, the operation succeeded. If the event is
 * canceled (via the {@link SimulationEvent#cancel() cancel} method),
 * the returned value will be true if the event could be canceled.
 * The event can be canceled before is is scheduled and if it has not
 * been canceled previously.  If the event cannot be canceled, false
 * will be returned.
 * <P>
 * When an existing task is added to a queue it will call a method
 * such as addCurrentTask (subclasses of TaskQueue have methods with
 * this name but with different signatures), which will block while
 * the task is on the queue.  The returned value will be true if the
 * task successfully ran and false if the task was removed from the
 * queue or could not be queued.  If a variant of addCurrentTask that
 * takes a SimEventCallable is used, the SimEventCallable's call
 * method will be passed a Simulation event that can be canceled.  The
 * call to the event's cancel method will return true if the event
 * was removed from the queue and false if the event was already scheduled
 * or had already been canceled.
 * <P>
 * To implement a timeout for the Callable or Runnable case, code based
 * on the following can be used:
 * <blockquote><pre>
 *    final SimulationEvent sev = taskQueue.add(new Callable() {
 *           public void call() {
 *              // operations when queuing was successful
 *           }
 *        }, PROCESSING_TIME);
 *        if (sev == null) {
 *           // case where the queuing request failed
 *        } else {
 *           simulation.scheduleCall(new Callable() {
 *               if (sev.cancel()) {
 *                // case where the timeout occurred
 *               }
 *           }, TIMEOUT);
 *        }
 * </pre></blockquote>
 * To implement a timeout for a task thread, code based on the following can
 * be used:
 * <blockquote><pre>
 *    if (taskQueue.addCurrentTask(PROCESSING_TIME,
 *                                 new SimEventCallable() {
 *            public void call(final SimulationEvent sev) {
 *                 // store sev if it is necessary to tell if the
 *                 // timeout occurred, in which case sev.isCanceled()
 *                 // will return true.
 *                 simulation.scheduleCall(new Callable() {
 *                     sev.cancel();
 *                 }, TIMEOUT);
 *            }})) {
 *       // normal code
 *    } else {
 *       // code to run if there was a timeout or a failure to queue the
 *       // task.
 *    }
 * </pre></blockquote>
 * @see org.bzdev.devqsim.TaskThread
 */
abstract public class TaskQueue<T> extends DefaultSimObject 
    implements QueueStatus
{

    Simulation simulation;
    TaskQueueSimEvent<T> scheduledEvent = null;


    QueueDeletePolicy deletePolicy = QueueDeletePolicy.WHEN_EMPTY;

    /**
     * Set the deletion policy.
     * 
     * @param policy either TaskQueue.DeletePolicy.MUST_BE_EMPTY,
     *               TaskQueue.DeletePolicy.WHEN_EMPTY,
     *               TaskQueue.DeletePolicy.NEVER; WHEN_EMPTY
     *               is the default
     */
    public void setDeletePolicy(QueueDeletePolicy policy) {
	if (!deleting) deletePolicy = policy;
    }

    /**
     * Get the current deletion policy for the queue.
     * @return the current deletion policy
     */
    public QueueDeletePolicy getDeletePolicy() {
	return deletePolicy;
    }


    public boolean canDelete() {
	if (super.canDelete()) {
	    switch (deletePolicy) {
	    case MUST_BE_EMPTY:
		return (size() == 0);
	    case WHEN_EMPTY:
		return true;
	    case NEVER:
		return false;
	    default:
		// should not occur because we covered all the values
		// but we are getting a compiler error
		return false;
	    }
	} else {
	    return false;
	}
    }

    boolean deleting = false;
    boolean deleted = false;

    protected void onDelete() {
	if (deleting) return;
	/*
	if (canFreeze()) {
	    freeze(false);
	}
	*/
	deleting = true;
	if (scheduledEvent == null && getSize() == 0) {
	    deleted = true;
	    super.onDelete();
	    if (nObservers > 0) notifyQueueObservers();
	}
    }
    
    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }


    /**
     * Set an event's parameters.
     * This method is provided because a field in the event class that has
     * to be altered is not public.
     * @param event the event
     * @param parameters the parameters
     */
    protected void setEventParameters(TaskQueueSimEvent<T> event, 
				      T parameters) {
	event.parameters = parameters;
    }

    /**
     * Method to provide additional initializations.
     * This will be called by the TaskQueue constructor.
     * The default method does nothing, but may be overridden
     * (e.g., for initialization in an anonymous class). One
     * reason to implement this method is to call {@link #setCanFreeze}
     * and/or {@link #setCanRelease} when an anonymous class is
     * initialized.
     */
    protected void init() {
    }

    /**
     * Constructor
     * @param sim the simulation
     * @param name the name of the task queue
     * @param intern true if the object should be interned (so it can be
     *               looked up by name).
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected TaskQueue (Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	simulation = sim;
	init();
    }


    int nObservers = 0;
    Set<QueueObserver> observers = new HashSet<QueueObserver>();
    public void addObserver(QueueObserver observer) {
	if (observer == null) return;
	if (deleting) return;

	if (observers.add(observer)) {
	    nObservers++;
	}

    }

    public void removeObserver(QueueObserver observer) {
	if (observer == null) return;
	if (observers.remove(observer)) {
	    nObservers--;
	}
    }

    private void notifyQueueObservers() {
	for (QueueObserver observer: observers){
	    observer.onQueueChange(this);
	}
    }


    /**
     * Put an event on the queue.
     * This method should be implemented by subclasses, which should also
     * provide a queue on which to store an event.
     * Normally an event is just added to the end of the queue. For some
     * cases (e.g., a LIFO queue), one may need to preempt a currently
     * scheduled event.  If preemption is necessary, the preempted event's
     * time parameters will have to be adjusted and the new event will
     * have to become the scheduled event. The latter operation is handled by
     * {@link #replaceScheduledEvent(TaskQueueSimEvent) replaceScheduledEvent}.
     * If replaceScheduledEvent is used, care must be taken if the queue
     * insertion could fail (e.g., the case of a finite-sized queue).
     * For modifying the event parameters (e.g., to allow for time already
     * attributed to processing an event), the methods
     * {@link #getOffQueueTime(TaskQueueSimEvent) getOffQueueTime} and
     * {@link #getEventParameters(TaskQueueSimEvent) getEventParameters}
     * may be useful.
     * @param event the event
     * @param scheduled the event that is currently scheduled for
     *        processing; null if there is none or if preempt mode is
     *        turned off
     * @return true if an event was successfully added to the queue;
     *         false otherwise
     */
    abstract protected boolean offerToQueue(TaskQueueSimEvent<T> event,
					    TaskQueueSimEvent<T> scheduled);

    /**
     * Take an event off the queue.
     * This method should be implemented by subclasses, which should also
     * provide a queue on which to store an event.
     * @return the event; null if the queue is empty
     */
    abstract protected TaskQueueSimEvent<T> pollFromQueue();


    /**
     * Get the simulation time at which a TaskQueueSimEvent was removed from
     * a queue.
     * This is may be needed by the offerToQueue methods of subclasses
     * when a scheduled event is replaced and the time needs to be
     * adjusted before putting it back on the queue.
     * @param event the event that was removed from a queue
     * @return the last time the event was removed from a queue
     *
     */
    protected long getOffQueueTime(TaskQueueSimEvent<T> event) {
	return event.offQueueTime;
    }


    /**
     * Get TaskQueueSimEvent parameters.
     * This is may be needed by the offerToQueue methods of subclasses
     * when a scheduled event is replaced.
     * @param event an event
     * @return the parameter field of the event
     */
    protected T getEventParameters(TaskQueueSimEvent<T> event) {
	return event.parameters;
    }


    /**
     * Queue an event given a Callable and time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param callable the Callable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the Callable
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T> doAdd(Callable callable, 
					 T parameters,
					 long interval,
					 double tpriority)
    {
	if (deleting) return null;
	Simulation sim = getSimulation();
	TaskObjectSimEvent newCallable = 
	    new TaskObjectSimEvent(callable);
	newCallable.source = sim;
	if (sim.stackTraceMode) {
	    ((SimulationEvent)newCallable).stackTraceArray =
		AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}

	TaskQueueSimEvent<T> result =
	    new 
	    TaskQueueSimEvent<T>(sim, 
				 this, newCallable, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Queue an event given a Callable bound to a simulation object and
     * a time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param callable the SimObjectCallable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the Callable
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T> doAdd(SimObjectCallable callable, 
					 T parameters, 
					 long interval,
					 double tpriority)
    {
	if (deleting) return null;
	TaskObjectSimEvent newCallable = 
	    new TaskObjectSimEvent(callable.getCallable(),
				   callable.getTag());
	newCallable.source = callable.getSource();
	Simulation sim = getSimulation();
	if (sim.stackTraceMode) {
	    ((SimulationEvent) newCallable).stackTraceArray =
		AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}

	TaskQueueSimEvent<T> result =
	    new TaskQueueSimEvent<T>(sim, this, newCallable, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Queue an event given a script, a processing-time interval, and a time
     * event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.  Once scheduled, the script will
     * be evaluated.
     * @param script the script to call when the entry is taken off the
     *               head of the queue
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the script
     *         cannot be queued
     * @exception RuntimeException if an exception occurred while trying
     *            to execute a script object's <code>call</code>method
     * @exception UnsupportedOperationException if there is no script engine
     */
    protected TaskQueueSimEvent<T> doAddCallScript(final String script,
						   T parameters,
						   long interval,
						   double tpriority)
	throws RuntimeException, UnsupportedOperationException
    {
	if (deleting) return null;
	final Simulation sim = getSimulation();
	// final ScriptEngine engine = sim.getScriptEngine();
	if (!sim.hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	Callable callable = new Callable() {
		public void call() {
		    try {
			sim.evalScript(script);
		    } catch (Exception ee) {
			throw new RuntimeException(ee);
		    }
		}
	    };
	TaskObjectSimEvent newCallable = new TaskObjectSimEvent(callable);
	newCallable.source = sim;
	if (sim.stackTraceMode) {
	    ((SimulationEvent) newCallable).stackTraceArray =
		AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}
	TaskQueueSimEvent<T> result =
	    new TaskQueueSimEvent<T>(sim, this, newCallable, parameters);
	return doAdd(result, interval, tpriority);
    }


    static final Object[] emptyArgs = new Object[0];

    /**
     * Queue an event given a script object, a processing interval,
     * and a time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.  Once scheduled to run, the
     * object's <code>call()</code> method will be invoked.
     * @param scriptObject the script object whose <code>call()</code>
     *                      method will be invoked
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the object
     *         cannot be queued
     * @exception RuntimeException if an exception occurred while trying
     *            to execute a script object's <code>call</code>method
     * @exception UnsupportedOperationException if there is no script engine
     */
    protected TaskQueueSimEvent<T> doAddCallObject(final Object scriptObject,
						   T parameters,
						   long interval,
						   double tpriority)
	throws UnsupportedOperationException, RuntimeException
    {
	if (deleting) return null;
	final Simulation sim = getSimulation();
	if (!sim.hasScriptEngine()) {
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	}
	Callable callable = new Callable() {
		public void call() {
		    try {
			sim.callScriptMethod(scriptObject, "call");
		    } catch (NoSuchMethodException e) {
			throw new RuntimeException(new ScriptException(e));
		    } catch (ScriptException ee) {
			throw new RuntimeException(ee);
		    }
		}
	    };
	TaskObjectSimEvent newCallable = new TaskObjectSimEvent(callable);
	newCallable.source = sim;
	if (sim.stackTraceMode) {
	    ((SimulationEvent) newCallable).stackTraceArray =
		AccessController.doPrivileged
		(new PrivilegedAction<StackTraceElement[]>() {
		    public StackTraceElement[] run() {
			return Thread.currentThread().getStackTrace();
		    }
		});
	}
	TaskQueueSimEvent<T> result =
	    new TaskQueueSimEvent<T>(sim, this, newCallable, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Queue an event given a Runnable, a processing-time interval,
     * and a time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     *
     * @param runnable the Runnable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the Runnable
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T>doAdd(Runnable runnable,
					T parameters,
					long interval,
					double tpriority)
    {
	if (deleting) return null;
	TaskThread taskThread = 
	    getSimulation().unscheduledTaskThread(runnable);
	TaskSimulationEvent tevent = taskThread.getSimulationEvent();
	TaskQueueSimEvent<T> result =
	    new 
	    TaskQueueSimEvent<T>(simulation, this, tevent, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Queue an event given a Runnable bound to a simulation object and
     * given a processing-time interval and time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param runnable the SimObjectRunnable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the Runnable
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T>doAdd(SimObjectRunnable runnable,
					T parameters,
					long interval,
					double tpriority)
    {
	if (deleting) return null;
	TaskThread taskThread = 
	    getSimulation().unscheduledTaskThread(runnable.getRunnable(),
						  runnable.getSource(),
						  runnable.getTag());
	TaskSimulationEvent tevent = taskThread.getSimulationEvent();
	TaskQueueSimEvent<T> result =
	    new 
	    TaskQueueSimEvent<T>(simulation, this, tevent, parameters);
	return doAdd(result, interval, tpriority);
    }


    /**
     * Queue a task that will run a script given a processing-time interval
     * and a time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T. Once scheduled, the script will be
     * evaluated.
     * @param script the script that will be run as a task.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the script
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T> doAddTaskScript(final String script,
						   T parameters,
						   long interval,
						   double tpriority)
	throws UnsupportedOperationException
    {
	if (deleting) return null;
	if (!simulation.hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	Runnable runnable = new Runnable() {
		public void run() {
		    Thread cthread = Thread.currentThread();
		    if (!(cthread instanceof TaskThread))
			throw new IllegalStateException
			    ("pause called in wrong context");
		    TaskThread taskThread = (TaskThread)cthread;
		    simulation.configBindingSwapper(taskThread,
						    null);
		    try {
			simulation.evalScript(script);
		    } catch (ScriptException e) {
			throw new RuntimeException(e);
		    }
		}
	    };
	TaskThread taskThread = unscheduledTaskThread(runnable);
	TaskSimulationEvent tevent = taskThread.getSimulationEvent();
	TaskQueueSimEvent<T> result =
	    new TaskQueueSimEvent<T>(simulation, this, tevent, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Queue a script object that will run a task, given a processing-time
     * interval and a time event priority.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.  Once scheduled, the object's
     * <code>run()</code> method will be invoked.
     *
     * @param scriptObject the script script object to queue
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @param interval the processing-time interval implied by the parameters
     * @param tpriority the time event priority implied by the parameters
     * @return the simulation event on success; null if the object
     *         cannot be queued
     */
    protected TaskQueueSimEvent<T> doAddTaskObject(final Object scriptObject,
						   T parameters,
						   long interval,
						   double tpriority)
	throws UnsupportedOperationException
    {
	if (deleting) return null;
	if (!simulation.hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	Runnable runnable = new Runnable() {
		public void run() {
		    Thread cthread = Thread.currentThread();
		    if (!(cthread instanceof TaskThread))
			throw new IllegalStateException
			    ("pause called in wrong context");
		    TaskThread taskThread = (TaskThread)cthread;
		    simulation.configBindingSwapper(taskThread, null);
		    try {
			simulation.callScriptMethod(scriptObject, "run");
		    } catch (NoSuchMethodException e) {
			throw new RuntimeException(new ScriptException(e));
		    } catch (ScriptException e) {
			throw new RuntimeException(e);
		    }
		}
	    };
	TaskThread taskThread = unscheduledTaskThread(runnable);
	//taskThread.makeScriptingThread(defaultBindings, defaultBindings);
	TaskSimulationEvent tevent = taskThread.getSimulationEvent();
	TaskQueueSimEvent<T> result =
	    new TaskQueueSimEvent<T>(simulation, this, tevent, parameters);
	return doAdd(result, interval, tpriority);
    }

    /**
     * Replace the currently scheduled event.
     * This has to effect when preempt mode is off.  When preempt mode
     * is on, the replacement becomes the currently scheduled event
     * and the previous scheduled event, if any, is descheduled.  This
     * method is intended for use in calls to offerToQueue when the
     * new event should be run and the previously scheduled event
     * should be put back on the queue.  In this case, offerToQueue
     * may have to adjust the processing-time interval for the event,
     * typically reducing this interval by the amount the event has
     * already waited.
     * @param replacement the new event
     */
    protected void replaceScheduledEvent(TaskQueueSimEvent<T> replacement) {
	if (preempt) {
	    if (scheduledEvent != null) {
		scheduledEvent.scheduled = false;
		getSimulation().descheduleEvent(scheduledEvent);
	    }
	    scheduledEvent = replacement;
	}
    }

    TaskQueueSimEvent<T> 
	doAdd(TaskQueueSimEvent<T> event, long interval, double tpriority)
    {
	boolean ok = true;
	if ((releaseCount > 0 || releaseExtras > 0) && scheduledEvent == null) {
	    scheduledEvent = event;
	} else if (isFrozen() || scheduledEvent != null) {
	    ok = offerToQueue(event, (preempt? scheduledEvent: null));
	} else {
	    scheduledEvent = event;
	}
	if (ok == false) return null;
	if (releaseExtras > 0) {
	    // we added an event to the queue when processing a series of
	    // releases, so the queue size we assumed in releaseUpTo
	    // is effectively one larger - to compensate we decrease
	    // releaseExtras by one and increase releaseCount by one.
	    releaseExtras--;
	    releaseCount++;
	}
	if (releaseCount > 0) {
	    addsDuringRelease++;
	}
	if (scheduledEvent == event) {
	    simulation.scheduleEvent(event, interval, tpriority);
	    event.scheduled = true;
	    event.offQueueTime = simulation.currentTicks();
	}
	if (nObservers > 0) notifyQueueObservers();
	return event;
    }


    boolean processing = false;

    // called by event objects in this package.
    // Associated with an event that caused a task thread to be put on a
    // queue.  The call occurs just before the task thread is restarted.
    // The call to this method occurs on the simulation's
    // thread, not on the task's thread.
    void doBeforeTaskEvent(TaskQueueSimEvent<T> event) {
	currentTask = event.tevent.thread;
	processing = true;
    }

    // Called by event objects in this package.
    // Associated with an event that caused a thread to be put on a
    // queue.  After the task is restarted, this will be called if a
    // task is queued via addCurrentTask or if a task pauses, or if a task
    // terminates.  The call to this method occurs on the simulation's
    // thread, not on the task's thread.
    void doAfterTaskEvent(TaskQueueSimEvent<T> event) {
	if (pauseEvent == null) processing = false;
	currentTask = null;
	doAfterTaskEvent(pauseEvent == null);
    }


    TaskQueueSimEvent<T> doAddCurrentTask(TaskQueueSimEvent<T> event)
    {
	TaskQueueSimEvent<T> result =
	    doAdd(event, getInterval(event.parameters),
		  getTPriority(event.parameters));
	Thread thread = Thread.currentThread();
	if (result != null && result != scheduledEvent
	    && thread instanceof TaskThread) {
	    // If a task thread has to be queued and queuing was
	    // successful, mark the thread as queued.
	    ((TaskThread)thread).threadQueued = true;
	    ((TaskThread)thread).queuingCanceled = false;
	}
	return result;
    }

    /*
    protected TaskQueueSimEvent<T> 
	doAddCurrentTask(TaskQueueSimEvent<T> result) 
    {
	return result;
    }
    */

    /**
     * Add the currently running task thread to a queue.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(T param)
	throws IllegalStateException
    {
	if (deleting) return false;
	return addCurrentTask(param, null);
    }

    /**
     * Add the currently running task thread to a queue.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed in addition the the
     * SimEventCallable argument.
     * <P>
     * When the second argument (callable) is non-null and if queuing was
     * successful, this argument will be passed
     * to the second argument of
     {@link TaskThread#pause(TaskEventCallable,SimEventCallable,CallableReturns) TaskThread.pause}
     * to provide an opportunity to store the event so that the task can
     * be canceled from the queue.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @param callable this argument's call method will be run when the
     *                 event is scheduled or queued and will be passed
     *                 a simulation event (e.g., to store the event to allow
     *                 the event to be canceled)
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(final T param, SimEventCallable callable) {
	if (deleting) return false;
	TaskEventCallable tcallable = new TaskEventCallable() {
		public SimulationEvent call(TaskThreadSimEvent event) {
		    TaskQueueSimEvent<T> result =
			new TaskQueueSimEvent<T>(getSimulation(),
					     TaskQueue.this,
					     event,
					     param);
		    if (simulation.eventStackTrace != null) {
			((SimulationEvent) result).stackTraceArray
			    = simulation.eventStackTrace;
		    }
		    // This returns 'result' normally; null if the
		    // current task cannot be added to the queue
		    // in which case the call to pause below will
		    // return false and the current thread will continue
		    // to run.
		    return doAddCurrentTask(result);
		}
	    };
	CallableReturns<Boolean> afterPause = new CallableReturns<Boolean>() {
	    public Boolean call() {
		TaskThread thread = (TaskThread)(Thread.currentThread());
		if (thread.queuingCanceled) {
		    thread.queuingCanceled = false;
		    thread.threadQueued = false;
		    return Boolean.FALSE;
		} else {
		    return Boolean.TRUE;
		}
	    }
	};
	return TaskThread.pause(tcallable, callable, afterPause);
    }

    class SimulationEventValue {
	SimulationEvent value;
    }


    /**
     * Add the currently running task thread to a queue and return the
     * simulation event generated.
     * This method is provided for convenience, especially for use
     * in a script where a simpler, but slightly less efficient interface
     * can be useful when a task may have to be canceled.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @return a simulation event on success; null on failure in which case the
     *         current thread will continue to run
     */
    public SimulationEvent addCurrentTaskObject(T param) 
    {
	if (deleting) return null;
	final SimulationEventValue sev = new SimulationEventValue();
	SimEventCallable callable = new SimEventCallable() {
		public void call(SimulationEvent event) {
		    sev.value = event;
		}
	    };
	boolean ok = addCurrentTask(param, callable);
	if (ok) {
	    return sev.value;
	} else {
	    return null;
	}
    }


    TaskThread currentTask = null;
    TaskQueuePauseSimEvent<T> pauseEvent = null;

    // called by event objects in this package.
    // Associated with an event that caused a thread to pause with
    // simulation processing suspended.  The call occurs just before
    // the task thread is restarted.  The call to this method occurs
    // on the simulation's thread, not on the task's thread.
    final void doBeforeTaskEvent(TaskQueuePauseSimEvent<T> event) {
	if (event.tevent instanceof TaskThreadSimEvent) {
	    currentTask = ((TaskThreadSimEvent)(event.tevent)).thread;
	}
	pauseEvent = null; // clear it as we are now continuing the
	                   // processing of this event.
    }

    // Called by event objects in this package.
    // Associated with an event that caused a thread to pause with
    // simulation processing suspended.  This will be called if a task
    // is queued via addCurrentTask, if a task pauses, or if the task
    // terminates.  The call occurs on the simulation's thread, not
    // the task's thread.
    final void doAfterTaskEvent(TaskQueuePauseSimEvent<T> event) {
	if (pauseEvent == null) processing = false;
	currentTask = null;
	doAfterTaskEvent(pauseEvent == null);
    }

    int tryRemoveCorrection = 0;

    private void doAfterTaskEvent(boolean notNewPauseEvent) 
    {
	if (notNewPauseEvent) {
	    scheduledEvent = null;
	    int diff = 0;
	    switch(releasePolicy) {
	    case CANCELS_AS_RELEASED:
		releaseCount -= cancelsDuringRelease;
		cancelsDuringRelease = 0;
		break;
	    case REPLACE_CANCELS:
		diff = cancelsDuringRelease - addsDuringRelease;
		cancelsDuringRelease = 0;
		addsDuringRelease = 0;
		if (diff < 0) diff = 0;
		break;
	    }
	    int test = releaseCount - diff;
	    if (!isFrozen() || test > 0) {
		for(;;) {
		    scheduledEvent = pollFromQueue();
		    if (scheduledEvent != null && scheduledEvent.canceled) {
			tryRemoveCorrection--;
			if (scheduledEvent.tevent
			    instanceof TaskThreadSimEvent) {
			    ((TaskThreadSimEvent)(scheduledEvent.tevent))
				.thread.threadQueued = false;
			}
			continue;
		    } else {
			if (test > 0) {
			    releaseCount--;
			    test--;
			    if (test == 0) {
				releaseCount = 0;
				releaseExtras = 0;
				cancelsDuringRelease = 0;
				addsDuringRelease = 0;
			    }
			}
			break;
		    }
		}
		if (scheduledEvent != null) {
		    if (scheduledEvent.tevent
			instanceof TaskThreadSimEvent) {
			((TaskThreadSimEvent)(scheduledEvent.tevent))
			    .thread.threadQueued = false;
		    }
		    scheduledEvent.scheduled = true;
		    scheduledEvent.offQueueTime = simulation.currentTicks();
		    getSimulation().scheduleEvent(scheduledEvent, 
						  getInterval(scheduledEvent
							      .parameters));
		} else {
		    if (deleting && getSize() == 0) {
			deleted = true;
			super.onDelete();
		    }
		}
		if (nObservers > 0) notifyQueueObservers();
	    } else {
		if (deleting && getSize() == 0) {
		    deleted = true;
		    super.onDelete();
		    if (nObservers > 0) notifyQueueObservers();
		}
	    }
	}
    }

    // protected void doAfterTaskEvent(boolean notNewPauseEvent) {}

    private boolean frozen = false;

    private boolean queueCanFreeze = true;

    /**
     * Set whether a queue can be frozen.
     * Subclasses should call this method to change the default or to
     * change whether or not the queue can be frozen.  Normally it will
     * called in a constructor, if at all.
     * @param value true if the queue can be frozen; false if it cannot
     */
    protected void setCanFreeze(boolean value) {
	queueCanFreeze = value;
    }

    /**
     * Determine if a queue can be frozen.
     * If this returns false, the method
     * {@link #freeze(boolean) freeze()} will throw an exception
     * @return true if a queue can be frozen; false otherwise
     * @see #freeze(boolean)
     */
    public boolean canFreeze() {return queueCanFreeze;}


    /**
     * Sets whether a queue is frozen or not.
     * Freezing a queue means that all new entries go onto the queue
     * rather than the first available being immediately scheduled.
     * @param value true if the queue will be frozen; false if unfrozen
     * @exception UnsupportedOperationException the queue can not be frozen
     * @see #canFreeze()
     */
    public void freeze(boolean value)  throws UnsupportedOperationException {
	if (canFreeze() == false) {
	    throw new UnsupportedOperationException(errorMsg("cannotFreeze"));
	}
	forceFreeze(value);
    }

    /**
     * Sets whether a queue is frozen or not regardless of the value
     * returned by canFreeze.
     * Freezing a queue means that all new entries go onto the queue
     * rather than the first available being immediately scheduled.
     * This method is intended to allow subclasses to manipulate whether
     * a queue is frozen or not in cases where that operation is not
     * allowed in general.
     * @param value true if the queue will be frozen; false if unfrozen
     * @see #canFreeze()
     */
    protected void forceFreeze(boolean value) {
	boolean oldValue = frozen;
	frozen = value;
	if (oldValue != frozen && oldValue) {
	    doUnfrozen();
	}
	if (nObservers > 0) notifyQueueObservers();
    }

    /**
     * Determine if a queue is frozen.
     * @return true if it is frozen; false otherwise
     * @see #freeze(boolean)
     */
    public boolean isFrozen() {return frozen;}

    private void doUnfrozen() {
	if (scheduledEvent == null) {
	    doAfterTaskEvent(true);
	}
    }
    
    /**
     * Policies for handling the case where an event is canceled
     * while a release is in progress.  A release is in progress
     * starting with a call to one of the release-initiation methods:
     * {@link org.bzdev.devqsim.TaskQueue#forceRelease(int) forceRelease},
     * {@link org.bzdev.devqsim.TaskQueue#release(int) release},
     * {@link org.bzdev.devqsim.TaskQueue#releaseUpTo(int) releaseUpTo},
     * or
     * {@link org.bzdev.devqsim.TaskQueue#forceReleaseUpTo(int) forceReleaseUpTo}.
     * A release ends when either the specified number of events have been
     * processed so that the release count becomes zero or when the method
     * {@link org.bzdev.devqsim.TaskQueue#clearReleaseCount() clearReleaseCount}
     * has been called.
     */
    public static enum ReleasePolicy {
	/**
	 * After a call to a release-initiation method, newly canceled events
	 * do not change the number of events that will be released.
	 */
	CANCELS_IGNORED,
	/**
	 * After a call to a release-initiation method, newly canceled events
	 * will be counted as released events.
	 */
        CANCELS_AS_RELEASED,
	/**
	 * After a call to a release-initiation method, when an event is
	 * processed, newly added events replace newly canceled
	 * events, and the difference is counted as released events.
	 * The counts of newly added and newly canceled events are
	 * reset to zero every time an event on the queue is processed
	 * successfully.  This policy is intended for cases in which
	 * existing events are canceled and new ones added at the
	 * same time.
	 */
	REPLACE_CANCELS
    }


    private int releaseCount = 0;
    private boolean canrelease = false;
    private ReleasePolicy releasePolicy = ReleasePolicy.CANCELS_IGNORED;

    /**
     * Determine if the user of this class can use the methods
     * {@link #release(int) release}, {@link #releaseUpTo(int) releaseUpTo},
     * or {@link #clearReleaseCount() clearReleaseCount}.
     * @return true if the methods release, releaseUpTo, and clearReleaseCount
     *         are supported; false otherwise
     */
    public boolean canRelease() {
	return canrelease;
    }

    /**
     * Set the release policy.
     * The policy will have no effect if {@link #canRelease()} returns false.
     * @param policy either CANCEL_AS_RELEASED, CANCELS_IGNORED, or
     *        REPLACE_CANCELS
     * @exception UnsupportedOperationException the operation is not supported
     *            due to the value returned by canRelease()
     * @exception IllegalStateException a release operation was in progress
     */
    public void setReleasePolicy(ReleasePolicy policy)
	throws UnsupportedOperationException, IllegalStateException
    {
	if (releaseCount>0 || addsDuringRelease>0 || cancelsDuringRelease>0) {
	    throw new IllegalStateException(errorMsg("releasing"));
	}
	releasePolicy = policy;
    }

    /**
     * Get the current release policy.
     * @return the current release policy
     */
    public ReleasePolicy getReleasePolicy() {
	return releasePolicy;
    }


    /**
     * Forcibly set the release policy
     * @param policy the policy
     */
    protected void forceSetReleasePolicy(ReleasePolicy policy) {
	releasePolicy = policy;
    }


    /**
     * Set whether or not the methods
     * {@link #release(int) release}, {@link #releaseUpTo(int) releaseUpTo},
     * and {@link #clearReleaseCount() clearReleaseCount} are supported.
     * By default, these methods are not supported.
     * @param value true if the methods {@link #release(int) release},
     *        {@link #releaseUpTo(int) releaseUpTo}, and
     *        {@link #clearReleaseCount() clearReleaseCount} are supported;
     *        false otherwise
     */
    protected void setCanRelease(boolean value) {
	canrelease = value;
    }


    /**
     * Allow a fixed number of entries to be removed from a queue
     * even if the queue is frozen.
     * @param count the number of additional queue entries that are
     *        guaranteed to be processed regardless of whether the queue
     *        is frozen or not
     * @exception UnsupportedOperationException queue entries cannot be
     *            released
     */
    public void release(int count) throws UnsupportedOperationException {
	if (canrelease == false) {
	    throw new
		UnsupportedOperationException(errorMsg("releaseMethod"));
	}
	forceRelease(count);
    }
    /**
     * Allow a fixed number of entries to be removed from a queue
     * even if the queue is frozen and even if canRelease() returns false.
     * This method is intended to allow subclasses to process entries on
     * a queue in cases where this operation is not allowed in general.

     * @param count the number of additional queue entries that are
     *        guaranteed to be processed regardless of whether the queue
     *        is frozen or not
     * @see #canRelease()
     * @see #freeze(boolean)
     * @see #canFreeze()
     */
    protected void forceRelease(int count) {
	int prev = releaseCount;
	if ((releaseCount + count) < 0) {
		releaseCount = 0;
	} else {
	    releaseCount += count;
	}
	if (releaseCount > 0 && prev == 0) doUnfrozen();
    }

    private int releaseExtras = 0;
    private int addsDuringRelease = 0;
    private int cancelsDuringRelease = 0;

    /**
     * Allow a fixed number of entries up to the current queue size to be
     * removed from a queue even if the queue is frozen.
     * @param count the number of additional queue entries that are
     *        requested to be processed regardless of whether the queue
     *        is frozen or not
     * @exception UnsupportedOperationException queue entries cannot be
     *            released
     */
    public void releaseUpTo(int count) throws UnsupportedOperationException {
	if (canrelease == false) {
	    throw new UnsupportedOperationException
		("releaseUpTo method not supported");
	}
	forceReleaseUpTo(count);
    }
    /**
     * Allow a fixed number of entries up to the current queue size to be
     * removed from a queue even if the queue is frozen.
     * This method is intended to allow subclasses to process entries on
     * a queue in cases where this operation is not allowed in general.
     * @param count the number of additional queue entries that are
     *        requested to be processed regardless of whether the queue
     *        is frozen or not
     */
    protected void forceReleaseUpTo(int count) {
	int size = size();
	if (releaseCount >= size) {
	    releaseExtras += count;
	} else {
	    if (releaseCount + count > size) {
		int newcount = size - releaseCount;
		releaseExtras += (count - newcount);
		release(newcount);
	    } else {
		release(count);
	    }
	}
    }

    /**
     * Set the number of queued entries guaranteed that will be processed
     * regardless of whether the queue is frozen to zero, provide release
     * operations are supported
     * @exception UnsupportedOperationException queue entries cannot be
     *            released
     * @see #canRelease()
     */
    public  void clearReleaseCount() throws UnsupportedOperationException {
	if (canrelease == false) {
	    throw new UnsupportedOperationException
		("clearReleaseCount method not supported");
	}
	releaseCount = 0;
	addsDuringRelease = 0;
	cancelsDuringRelease = 0;
    }

    /**
     * Set the number of queued entries guaranteed that will be processed
     * regardless of whether the queue is frozen to zero.
     * This method is intended to allow subclasses to process entries on
     * a queue in cases where this operation is not allowed in general.
     */
    protected void forceClearReleaseCount() {
	releaseCount = 0;
	addsDuringRelease = 0;
	cancelsDuringRelease = 0;
    }

    /**
     * Determine if a a task queue supports preempt mode.
     * @return true if preempt mode is supported; false otherwise
     * @see #preempt(boolean)
     */
    public boolean canPreempt() {return false;}

    private boolean preempt = false;

    /**
     * Set preempt mode.
     * When preempt mode is off, the new queue entries cannot preempt an
     * event that has been removed from the queue for processing.  If
     * preempt mode is on, a currently scheduled event may  be put back
     * on the queue, with its processing-time adjusted appropriately and
     * the new event processed instead.
     * @param value true to turn on preempt mode; false to turn it off
     */
    public void  preempt(boolean value)
	throws UnsupportedOperationException
    {
	if (canPreempt() == false) {
	    throw new UnsupportedOperationException
		("preempt mode not supported for queue");
	}
	preempt = value;
    }

    /**
     * Suspend the currently running task thread.
     * While the current task is suspended using this method, the
     * queue does not schedule a new event. If TaskThread.pause is
     * used instead, the queue will start processing new events and
     * calling this method from the thread that called
     * TaskThread.pause will result in an error.
     * @param interval the interval for which the task-thread will wait.
     * @exception IllegalStateException the thread is not the queue's
     *            current thread
     */
    public void pauseCurrentTask(long interval) 
	throws IllegalStateException
    {
	pauseCurrentTask(interval, null);
    }

    /**
     * Suspend the currently running task thread while it is still the
     * scheduled event.
     * While suspended, the queue does not schedule a new event. If
     * TaskThread.pause is used instead, the queue will start processing
     * new events, and calling this method from the thread that called
     * TaskThread.pause without first requeuing the task will result in 
     * an error.
     * @param interval the interval for which the task-thread will wait.
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @exception IllegalStateException the thread is not the queue's
     *            current thread
     */
    public void pauseCurrentTask(final long interval, SimEventCallable callable)
	throws IllegalStateException
    {
	if ((Thread)currentTask != Thread.currentThread()) {
	    throw new 
		IllegalStateException("pauseCurrentTask not allowed");
	}
	TaskEventCallable tcallable = new TaskEventCallable() {
		public SimulationEvent call(TaskThreadSimEvent event) {
		    Simulation sim = getSimulation();
		    // sets field in TaskQueue so we can cancel the
		    // event and so we can tell if we are pausing or
		    // processing a new queue entry
		    pauseEvent = new TaskQueuePauseSimEvent<T>
			(sim, TaskQueue.this, event);
		    if (simulation.eventStackTrace != null) {
			((SimulationEvent) pauseEvent).stackTraceArray
			    = simulation.eventStackTrace;
		    }
		    if (event.source == null)
			event.source = TaskQueue.this;
		    return sim.scheduleEvent(pauseEvent, interval);
		}
	    };
	TaskThread.pause(tcallable, callable, null);
    }

    void cancelEvent(TaskQueueSimEvent<T> event, boolean scheduled) {
	if (scheduled) {
	    event.scheduled = false;
	    getSimulation().descheduleEvent(event);
	}
	doDequeueEvent(event, scheduled);
    }

    private void doDequeueEvent(TaskQueueSimEvent<T> event, 
				boolean wasScheduled)
    {
	// explicitly handle the scheduledEvent case
	if (event == scheduledEvent) {
	    if (isFrozen()) {
		scheduledEvent = null;
		if (deleting && getSize() == 0) {
		    deleted = true;
		    super.onDelete();
		    if (nObservers > 0) notifyQueueObservers();
		}
		if (event.tevent instanceof TaskThreadSimEvent) {
		    // in this case, we have canceled a queued event.
		    TaskThreadSimEvent ttse = (TaskThreadSimEvent) event.tevent;
		    ttse.processEvent();
		}
	    } else {
		if (event.tevent instanceof TaskThreadSimEvent) {
		    // in this case, we have canceled a queued event.
		    scheduledEvent = null;
		    TaskThreadSimEvent ttse = (TaskThreadSimEvent) event.tevent;
		    ttse.processEvent();
		}
		doAfterTaskEvent(true);
	    }
	} else {
	    //  for the rest, those will either be cleaned out
	    // when doBeforeTaskEvent is called due to the "canceled"
	    // flag in the event or they will be deleted from the
	    // queue when the implementation can do this efficiently.
	    // If nothing is done, the software will still function
	    // properly.
	    if (tryRemove(event) == false) {
		tryRemoveCorrection++;
	    }
	    if (event.tevent instanceof TaskThreadSimEvent) {
		// in this case, we have canceled a queued event.
		TaskThreadSimEvent ttse = (TaskThreadSimEvent) event.tevent;
		ttse.processEvent();
	    }
	    if (releaseCount > 0) {
		cancelsDuringRelease++;
	    }

	    if (nObservers > 0) notifyQueueObservers();
	}
    }


    /**
     * Try to remove an event from the queue.
     * By default, this method simply returns.
     * This may be overridden by subclasses to allow events to be
     * removed from a queue. The default behavior is to simply
     * ignore events that are in a "canceled" state (and in this
     * case, tryRemove always returns false).  If the
     * queue's data structure allows quick removal, implementing
     * this method may improve efficiency in that case.
     * @param event the event to remove.
     * @return true if the event was removed or was not in the queue; 
     *         false otherwise
     */
    protected boolean tryRemove(TaskQueueSimEvent<T> event) {
	return false;
    }


    void cancelEvent(TaskQueuePauseSimEvent<T> event, boolean scheduled) {
	if (event != pauseEvent) return;
	if (scheduled) {
	    event.scheduled = false;
	    getSimulation().descheduleEvent(event);
	}
	pauseEvent = null;
	doAfterTaskEvent(true);
    }


    /**
     * Get the raw size of the queue.
     * The size does not include the currently scheduled event. It does
     * contain any events that were canceled but could not be removed.
     * @return the queue size
     */
    abstract protected int getSize();

    /**
     * Get the size of the queue.
     * The size does not include the currently scheduled event.
     * @return the queue size
     */
    public int size() {
	return getSize() - tryRemoveCorrection;
    }

    /**
     * Find the interval used to schedule a queue entry based on
     * entry's parameters or a default value.
     * @param parameters the parameters for a queue entry
     * @return the interval in units of simulation ticks
     */
    abstract protected long getInterval(T parameters);

    /**
     * Find the time event priority used to schedule a queue entry based
     * on the entry's parameters or a default value of 0.0.
     * @param parameters the parameters for a queue entry
     * @return the priority
     */
    protected double getTPriority(T parameters) {
	return 0.0;
    }

    /*
     * Find the interval used to schedule a queue entry given that
     * entry's parameters.
     * This function parses the parameters determine an interval.
     * The value returned by getInterval() is used to compute
     * the actual delay used by the queue.
     * @param parameters the parameters for a queue entry
    abstract protected long parseForInterval(T parameters);
    */
    /*
     * Find the time event priority used to schedule a queue entry given that
     * entry's parameters.
     * This function parses the parameters determine an interval.
     * The value returned by getTPriority() is used to compute
     * the actual delay used by the queue.
     * @param parameters the parameters for a queue entry
    protected long parseForInterval(T parameters) {
	return 0.0;
    }
    */

    /**
     * Determine if the queue is busy.
     * A queue is busy if there is a scheduled event that is either
     * running or on the simulation event queue, instead of stored
     * in the queue.
     * @return true if the queue is busy; false otherwise
     */
    public boolean isBusy() {
	return (scheduledEvent != null);
    }

    /**
     * Determine if a scheduled queue entry is being processed.
     * This is true when a callable, runnable, or task (that was
     * scheduled after being pulled off the queue) is running.
     * @return true if being processed; false otherwise
     */
    public boolean isProcessing() {
	return processing;
    }

    /**
     * Determine how many servers are in use.
     * The default implementation allows only 1.
     * @return the number of servers in use
     */
    public int inUseCount() {
	return (scheduledEvent != null)? 1: 0;
    }

   /**
     * Determine the maximum number of tasks or calls that can be 
     * handled concurrently (equivalent to the number of servers in
     * a queuing system).  The default implementation just returns 1.
     * @return the number that can be processed simultaneously
     */
    public int serverCount() {
	return 1;
    }

    /**
     * In addition, the configuration that is printed includes the following:
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following.
     * <P>
     * Defined in {@link TaskQueue}:

     * <UL>
     *  <LI> the deletion policy.
     *  <LI> whether or not the queue can be frozen.
     *  <LI> the release policy.
     *  <LI> the concurrency limit.
     *  <LI> whether or not preemption is allowed.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   java.io.PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "deletionPolicy: " + getDeletePolicy());
	out.println(prefix + "can freeze: " + canFreeze());
	out.println(prefix + "release policy: " + getReleasePolicy());
	out.println(prefix + "concurrency limit: " +  serverCount());
	out.println(prefix + "preemption allowed: " + canPreempt());
    }

    /**
     * {@inheritDoc}
     * In addition, the state that is printed includes the following.
     * <P>
     * Defined in {@link TaskQueue}:
     * <UL>
     *  <LI> the queue size.
     *  <LI> whether or not the queue is frozen.
     *  <LI> the release count.
     *  <LI> whether or not the queue is busy.
     *  <LI> whether or not the queue is processing entries.
     *  <LI> the number of 'customers' being serviced.
     *  <LI> the current release policy.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "queue size: "  + size());
	out.println(prefix + "frozen: " + isFrozen());
	out.println(prefix + "release count: " + releaseCount);
	out.println(prefix + "busy: "  + isBusy());
	out.println(prefix + "processing: " + isProcessing());
	out.println(prefix + "number being serviced: " + inUseCount());
	out.println(prefix + "current release policy: "
		    + getReleasePolicy());
    }
}

//  LocalWords:  exbundle ul li doAdd doAddCallScript doAddCallObject
//  LocalWords:  doAddTaskScript addCurrentTask SimEventCallable init
//  LocalWords:  TaskQueue setCanFreeze boolean setCanRelease util
//  LocalWords:  TaskQueue's ServerQueue getSize offerToQueue pre sev
//  LocalWords:  TaskQueueSimEvent pollFromQueue LifoTaskQueue sim
//  LocalWords:  getOffQueueTime getEventParameters SimulationEvent
//  LocalWords:  SimEventCallable's blockquote taskQueue scheduleCall
//  LocalWords:  isCanceled canFreeze IllegalArgumentException param
//  LocalWords:  replaceScheduledEvent tpriority SimObjectCallable
//  LocalWords:  ScriptEngine getScriptEngine noScriptEngine
//  LocalWords:  scriptObject SimObjectRunnable taskThread autoboxing
//  LocalWords:  makeScriptingThread defaultBindings descheduled
//  LocalWords:  releaseUpTo releaseExtras releaseCount cannotFreeze
//  LocalWords:  doAddCurrentTask TaskEventCallable CallableReturns
//  LocalWords:  doAfterTaskEvent notNewPauseEvent forceRelease
//  LocalWords:  UnsupportedOperationException forceReleaseUpTo
//  LocalWords:  clearReleaseCount canRelease IllegalStateException
//  LocalWords:  releaseMethod requeuing pauseCurrentTask tryRemove
//  LocalWords:  scheduledEvent doBeforeTaskEvent getInterval
//  LocalWords:  parseForInterval getTPriority printName
//  LocalWords:  deletionPolicy
