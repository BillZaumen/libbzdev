package org.bzdev.devqsim;
import org.bzdev.lang.Callable;
import org.bzdev.lang.CallableReturns;
import java.util.*;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.security.*;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Queue with servers.

 * This class provides support for creating queues with one or more
 * servers. Instances of QueueServer represent the queue's servers,
 * although typically a server will be a class that implements
 * QueueServer (which is defined as an interface) so that the
 * operations the server supports can be different for different
 * queues.  As long as servers are available (each server queue has a
 * fixed number), new requests will be processed immediately, with
 * requests queued when servers are not available.  If the queue is not
 * empty, the order in which entries are processed depends on the type
 * of server queue being implemented.
 * <P>
 * A server queue can queue objects that implement QueueCallable or
 * QueueRunnable, or are instances of TaskThread:
 * <ul>
 *   <li> for the QueueCallable case, the user must provide two
 *   methods, interactWith(Server) and call(), where Server is
 *   a class implementing the QueueServer interface.  The method
 *   interact with provides the code representing an interaction
 *   with a particular server, and when interactWith completes
 *   execution, the server is free to process other events. The
 *   method call() will then be executed and will handle any additional
 *   actions (e.g., scheduling additional events that would occur
 *   after a 'customer' finishes interacting with the server.
 *   <li> for the QueueRunnable case, the user must provide two
 *   methods, interactWith(Server) and run(), where Server is
 *   a class implementing the QueueServer interface.  A TaskThread
 *   will be started, and this thread will first call interactWith.
 *   When interactWith returns, the server is free to process other
 *   events. The method run() will then be executed and the thread
 *   will terminate when run() returns.
 *   <li> for the TaskThread case, when a task thread is placed on a
 *   queue, an instance of the interface QueueServerHandler&lt;Server&gt;
 *   must be provided, where Server implements the QueueServer interface.
 *   QueueServerHandler provides a single method, interactWith(Server),
 *   and this method will be called when the task resumes execution and
 *   before the method suspending the task returns.  When interactWith
 *   returns, the server is free to process other events.
 * </ul>
 *  When an event is scheduled, the processing time is the sum of the time
 *  provided when the event was queued and the value provided by the
 *  server, obtained by calling the getInterval() method declared by the
 *  QueueServer interface.  This total represents the elapsed time needed
 *  to interact with a server and is the sum of the delays attributed to
 *  the server and to its 'customer'.
 * <p>
 * As long as the queue is not empty, entries will be pulled off the
 * queue and processed whenever a server is available.  If no servers
 * are free, no entries in the queue are processed so that new entries
 * merely accumulate.
 * <p>
 * The length of time for which a server is busy is the sum of the
 * values of the server's <code>getInterval()</code> method and the
 * server queue's task queue's <code>getInterval(param)</code> method,
 * where the argument <code>param</code> is the value provided when
 * the entry is added to the queue.  This entry's type is T (one of
 * the two type parameters) and its definition is provided by
 * ServerQueue's subclasses. Objects of type T not only determine
 * delay, but also provide any additional information needed for
 * queuing (e.g., a priority level when the underlying task queue is
 * a priority queue).
 * <P>

 * Otherwise, the behavior is similar to that of a task queue: when
 * a Callable or a Runnable is added to a server queue, a
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
 * such as addCurrentTask (subclasses of ServerQueue have methods with
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
 *
 * @see org.bzdev.devqsim.QueueServer
 * @see org.bzdev.devqsim.QueueCallable
 * @see org.bzdev.devqsim.QueueRunnable
 * @see org.bzdev.devqsim.QueueServerHandler
 * @see org.bzdev.devqsim.TaskThread
 * @see org.bzdev.devqsim.TaskQueue
 */

abstract public class ServerQueue<T, QS extends QueueServer> 
    extends DefaultSimObject implements QueueStatus
{
    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    LinkedList<QS> slist = new LinkedList<QS>();
    TaskQueue<T> tq;
    private int nservers = 0;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the queue
     * @param intern true if the queue name should be interned in the
     *        simulation tables; false otherwise
     * @param tq  the TaskQueue used to store entries
     * @param servers the queue's servers
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    @SuppressWarnings("unchecked")
    protected ServerQueue(Simulation sim, String name,
			  boolean intern, TaskQueue<T> tq, QS... servers) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.tq = tq;
	nservers = servers.length;
	for (QS qs: servers) {
	    slist.add(qs);
	}
	if (slist.size() == 0) tq.freeze(true);
    }

    QueueDeletePolicy deletePolicy = QueueDeletePolicy.WHEN_EMPTY;

    /**
     * Set the deletion policy for this server queue's task queue.
     * 
     * @param policy either TaskQueue.DeletePolicy.MUST_BE_EMPTY,
     *               TaskQueue.DeletePolicy.WHEN_EMPTY,
     *               TaskQueue.DeletePolicy.NEVER; WHEN_EMPTY
     *               is the default
     */
    public void setDeletePolicy(QueueDeletePolicy policy) {
	if (!deleting) {
	    deletePolicy = policy;
	    tq.setDeletePolicy(policy);
	}
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
		return (size() == 0 && inUseCount() == 0);
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
    boolean waitingForLastServer = false;

    protected void onDelete() {
	if (deleting) return;
	if (canFreeze()) {
	    freeze(false);
	}
	
	tq.addObserver(new QueueObserver() {
		public void onQueueChange(QueueStatus qstatus) {
		    if (qstatus.isDeleted()) {
			if (inUseCount() == 0) {
			    ServerQueue.super.onDelete();
			} else {
			    waitingForLastServer = true;
			}
			tq.removeObserver(this);
		    }
		}
	    });
	deleting = true;
	tq.delete();
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
     * Get the TaskQueue associated with this server queue.
     * @return the TaskQueue
     */
    protected final TaskQueue<T> getTaskQueue() {return tq;}

    // track separately from superclass because we'll freeze the
    // queue when all servers are busy.
    private boolean frozen = false;
    private boolean canfreeze = true;


    /**
     * Set whether a queue can be frozen.
     * Subclasses should call this method to change the default or to
     * change whether or not the queue can be frozen.  Normally it will
     * called in a constructor, if at all.
     * @param value true if the queue can be frozen; false if it cannot
     */
    protected void setCanFreeze(boolean value) {
	canfreeze = value;
    }

    /**
     * Determine if a queue can be frozen.
     * @return true if a queue can be frozen; false otherwise
     * @see #freeze(boolean)
     */
    public boolean canFreeze() {return canfreeze;}


    /**
     * Sets whether a queue is frozen or not.
     * Freezing a queue means that all new entries go onto the queue
     * rather than the first available being immediately scheduled.
     * Subclasses must throw an UnsupportedOperationException if the queue
     * cannot be frozen by the user.

     * @param value true if the queue will be frozen; false if unfrozen
     * @exception UnsupportedOperationException the queue can not be frozen
     */
    public void freeze(boolean value)  throws UnsupportedOperationException {
	if (slist.size() > 0) {
	    tq.freeze(value);
	}
	frozen = value;
	if (nObservers > 0) notifyQueueObservers();
    }

    /**
     * Determine if a queue is frozen.
     * @return true if it is frozen; false otherwise
     * @see #freeze(boolean)
     */
    public boolean isFrozen() {return frozen;}


    /**
     * Get a server.
     * This places a server into use. When no more servers are
     * available, the underlying task queue will stop processing
     * entries until a server becomes available.
     * @return the server
     */
    protected QS getIdleServer() {
	QS server = slist.poll();
	if (server == null) {
	    throw new 
		IllegalStateException(errorMsg("noAvailServ"));
	}
	if (slist.size() == 0) {
	    tq.freeze(true);
	}
	if (nObservers > 0) notifyQueueObservers();
	return server;
    }

    /**
     * Release a server.
     * This makes the server available for use and allow the underlying
     * task queue to process entries.
     * @param server the server
     */

    protected void putIdleServer(QS server) {
	boolean slistWasEmpty = (slist.size() == 0);
	slist.add(server);
	if (waitingForLastServer && inUseCount() == 0) {
	    waitingForLastServer = false;
	    super.onDelete();
	}
	if (slistWasEmpty && !frozen) {
	    tq.freeze(false);
	}
	if (nObservers > 0) notifyQueueObservers();
    }


    /**
     * Queue an event given a QueueCallable.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param callable the Callable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAdd(final QueueCallable<QS> callable, T parameters)  
    {
	if (deleting) return null;
	final long cinterval = tq.getInterval(parameters);
	final Simulation simulation = getSimulation();
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;
	Callable ourCallable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Callable newCallable = new Callable() {
			    public void call() {
				try {
				    ServerQueue.this.fireSQServerInteraction
					((Object) server, null, null);
				    callable.interactWith(server);
				} finally {
				    putIdleServer(server);
				}
				ServerQueue.this.fireSQCallable
				    ((Object) server, null, null);
				callable.call();
			    }
			};
		    simulation.scheduleCall(newCallable, interval, stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) {
	    oldsize = tq.size() + (tq.isBusy()? 1: 0);
	}
	TaskQueueSimEvent<T> result = tq.doAdd(ourCallable, parameters, 0, 0.0);
	// make the source the server queue, not the internal task queue used
	// to help implement the server queue.
	result.source = this;
	if (nObservers > 0 && oldsize != (tq.size() + (tq.isBusy()? 1: 0))) {
	    notifyQueueObservers();
	}
	return result;
    }

  /**
     * Queue an event given a SimObjQueueCallable.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param qcallable the Callable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAdd(SimObjQueueCallable<QS> qcallable, T parameters)  
    {
	if (deleting) return null;
	final long cinterval = tq./*parseForInterval*/getInterval(parameters);
	final Simulation simulation = getSimulation();
	final QueueCallable<QS> callable = qcallable.getCallable();
	final SimObject source = qcallable.getSource();
	final String tag = qcallable.getTag();
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;
	Callable ourCallable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Callable newCallable = new Callable() {
			    public void call() {
				try {
				    ServerQueue.this.fireSQServerInteraction
					((Object) server, source, tag);
				    callable.interactWith(server);
				} finally {
				    putIdleServer(server);
				}
				ServerQueue.this.fireSQCallable
				    ((Object) server, source, tag);
				callable.call();
			    }
			};
		    simulation.scheduleCall(newCallable, interval, stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) {
	    oldsize = tq.size() + (tq.isBusy()? 1: 0);
	}
	TaskQueueSimEvent<T> result = tq.doAdd(ourCallable, parameters, 0, 0.0);
	// make the source the server queue, not the internal task queue used
	// to help implement the server queue.
	result.source = this; 
	
	if (nObservers > 0 && oldsize != (tq.size() + (tq.isBusy()? 1: 0))) {
	    notifyQueueObservers();
	}
	return result;
    }

    static final Object[] emptyArgs = new Object[0];

    /**
     * Queue an event given a script object that implements the QueueCallable
     * interface.
     * The script object must implement a method named
     * <code>interactWith</code> that takes a QueueServer as its argument
     * and a method with no arguments named <code>call</code>, which will be
     * executed in that order in the simulation's thread.
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
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueCallable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAddCallObject(final Object scriptObject, T parameters)  
	throws UnsupportedOperationException
    {
	if (deleting) return null;
	final long cinterval = tq.getInterval(parameters);
	final Simulation simulation = getSimulation();
	if (!simulation.hasScriptEngine())
	    throw new UnsupportedOperationException
		(errorMsg("noScriptEngine"));
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;
	Callable ourCallable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Callable newCallable = new Callable() {
			    public void call() {
				try {
				    try {
					ServerQueue.this.
					    fireSQServerInteraction
					    ((Object) server, null, null);
					simulation.callScriptMethod
					    (scriptObject,
					     "interactWith", server);
				    } finally {
					putIdleServer(server);
				    }
				    ServerQueue.this.fireSQCallable
					((Object) server, null, null);
				    simulation.callScriptMethod
					(scriptObject, "call");
				} catch (NoSuchMethodException e) {
				    throw new RuntimeException
					(new ScriptException(e));
				} catch (ScriptException ee) {
				    throw new RuntimeException(ee);
				}
			    }
			};
		    simulation.scheduleCall(newCallable, interval,
					    stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) {
	    oldsize = tq.size() + (tq.isBusy()? 1: 0);
	}
	TaskQueueSimEvent<T> result = tq.doAdd(ourCallable, parameters,
					       0, 0.0);
	// make the source the server queue, not the internal task queue
	// used to help implement the server queue.
	result.source = this;
	if (nObservers > 0 && oldsize != (tq.size()+(tq.isBusy()? 1: 0))) {
	    notifyQueueObservers();
	}
	return result;
    }

    /**
     * Queue an event given a QueueRunnable.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     *
     * @param runnable the Runnable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAdd(final QueueRunnable<QS> runnable, T parameters) 
    {
	if (deleting) return null;
	final long cinterval = tq./*parseForInterval*/getInterval(parameters);
	final Simulation simulation = getSimulation();
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;

	Callable callable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Runnable ourRunnable = new Runnable() {
			    public void run() {
				try {
				    ServerQueue.this.fireSQServerInteraction
					((Object) server, null, null);
				    runnable.interactWith(server);
				} finally {
				    putIdleServer(server);
				}
				ServerQueue.this.fireSQRunnable
				    ((Object) server, null, null);
				runnable.run();
			    }
			};
		    simulation.scheduleTask(ourRunnable, interval, stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) oldsize = tq.size();
	TaskQueueSimEvent<T> result = tq.doAdd(callable, parameters, 0, 0.0);
	// make the source the server queue, not the internal task queue used
	// to help implement the server queue.
	result.source = this;
	if (nObservers > 0 && oldsize != tq.size()) notifyQueueObservers();
	return result;
    }

    /**
     * Queue an event given a SimObjQueueRunnable.
     * This should be called by a subclass to add an event to a queue,
     * as the currently scheduled event is handled specially.  The subclass
     * is responsible for using its "add" method's arguments to
     * construct an instance of type T.
     * @param qrunnable the Runnable specifying the task to be queued.
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAdd(final SimObjQueueRunnable<QS> qrunnable, T parameters) 
    {
	if (deleting) return null;
	final long cinterval = tq./*parseForInterval*/getInterval(parameters);
	final Simulation simulation = getSimulation();
	final QueueRunnable<QS> runnable = qrunnable.getRunnable();
	final SimObject source = qrunnable.getSource();
	final String tag = qrunnable.getTag();
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;
	Callable callable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Runnable ourRunnable = new Runnable() {
			    public void run() {
				try {
				    ServerQueue.this.fireSQServerInteraction
					((Object) server, source, tag);
				    runnable.interactWith(server);
				} finally {
				    putIdleServer(server);
				}
				ServerQueue.this.fireSQRunnable
				    ((Object) server, source, tag);
				runnable.run();
			    }
			};
		    simulation.scheduleTask(ourRunnable, interval, stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) oldsize = tq.size();
	TaskQueueSimEvent<T> result = tq.doAdd(callable, parameters, 0, 0.0);
	// make the source the server queue, not the internal task queue used
	// to help implement the server queue.
	result.source = this;
	if (nObservers > 0 && oldsize != tq.size()) notifyQueueObservers();
	return result;
    }


    /**
     * Queue an event given a script object that provides a QueueRunnable
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
     * @param parameters the parameters used in queuing or scheduling
     *                   the request
     * @return the SimulationEvent created; null if the QueueRunnable cannot be
     *         queued
     */
    protected TaskQueueSimEvent<T> 
	doAddTaskObject(final Object scriptObject, T parameters) 
	throws UnsupportedOperationException
    {
	if (deleting) return null;
	final long cinterval = tq./*parseForInterval*/getInterval(parameters);
	final Simulation simulation = getSimulation();
	// final ScriptEngine engine = simulation.getScriptEngine();
	if (!simulation.hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	final StackTraceElement[] stacktrace = simulation.stackTraceMode?
	    AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    }): null;
	Callable callable = new Callable() {
		public void call() {
		    final QS server = getIdleServer();
		    long interval = cinterval + server.getInterval();
		    Runnable ourRunnable = new Runnable() {
			    public void run() {
				try {
				    try {
					ServerQueue.this
					    .fireSQServerInteraction
					    ((Object) server, null, null);
					simulation.callScriptMethod
					    (scriptObject,
					     "interactWith", server);
				    } finally {
					putIdleServer(server);
				    }
				    ServerQueue.this.fireSQRunnable
					((Object) server, null, null);
				    simulation.callScriptMethod
					(scriptObject, "run");
				} catch (NoSuchMethodException e) {
				    throw new RuntimeException
					(new ScriptException(e));
				} catch (ScriptException ee) {
				    throw new RuntimeException(ee);
				}
			    }
			};
		    simulation.scheduleTask(ourRunnable, interval,
					    stacktrace);
		}
	    };
	int oldsize = 0;
	if(nObservers > 0) oldsize = tq.size();
	TaskQueueSimEvent<T> result =
	    tq.doAdd(callable, parameters, 0, 0.0);
	// make the source the server queue, not the internal task queue used
	// to help implement the server queue.
	result.source = this;
	if (nObservers > 0 && oldsize != tq.size()) notifyQueueObservers();
	return result;
    }


    /*
     * Add the currently running task thread to a queue.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed in addition the the
     * SimEventCallable argument.
     * @param handler this argument's <code>interactWith</code> method
     *                will be called with a server as its argument when
     *                the event is scheduled
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
    public boolean addCurrentTask(QueueServerHandler<QS> handler, T param)
	throws IllegalStateException
    {
	return addCurrentTask(handler, param, null);
    }
    */

    class SimulationEventValue {
	SimulationEvent value;
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
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed.
     * @param scriptObject the script object specifying the interaction
     *        with a server
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @param scriptSimEventCallable the script object that obtains a
     *        SimulationEvent that can be used to remove the task from
     *        the queue
     * @return true if the task was restarted by the queue; false if it
     *         was canceled or could not be queued
     */
    public boolean addCurrentTaskScriptObject(final Object scriptObject,
					      T param,
					      final Object
					      scriptSimEventCallable)
	throws UnsupportedOperationException
    {
	final Simulation simulation = getSimulation();
	// final ScriptEngine engine = simulation.getScriptEngine();
	if (!simulation.hasScriptEngine())
	    throw new UnsupportedOperationException(errorMsg("noScriptEngine"));
	QueueServerHandler<QS> handler = new QueueServerHandler<QS>() {
	    public void interactWith(QS server) {
		try {
		    simulation.callScriptMethod(scriptObject,
						"interactWith", server);
		} catch (NoSuchMethodException e) {
		    throw new RuntimeException(new ScriptException(e));
		} catch (ScriptException ee) {
		    throw new RuntimeException(ee);
		}
	    }
	};
	final SimulationEventValue sev = new SimulationEventValue();
	SimEventCallable callable = (scriptSimEventCallable == null)? null:
	    new SimEventCallable() {
		public void call(SimulationEvent event) {
		    try {
			simulation.callScriptMethod
			    (scriptSimEventCallable, "call", event);
		    } catch (NoSuchMethodException e) {
			throw new RuntimeException
			    (new ScriptException(e));
		    } catch (ScriptException ee) {
			throw new RuntimeException(ee);
		    }
		}
	    };

	return addCurrentTask(handler, param, callable);
    }


    /**
     * Add the currently running task thread to a queue.
     * This will normally be called
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed in addition the the
     * SimEventCallable argument.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
    public boolean addCurrentTask(T param)
	throws IllegalStateException
    {
	return addCurrentTask(null, param, null);
    }
     */

    /**
     * Add the currently running task thread to a queue.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed in addition the the
     * SimEventCallable argument.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
    public boolean addCurrentTask(T param, SimEventCallable callable) {
	return addCurrentTask(null, param, callable);
    }
     */

    /**
     * Add the currently running task thread to a queue.
     * This is public because the type parameter T may be a type for
     * which autoboxing is possible.  Otherwise it is usually more
     * convenient for a subclass to implement a method that simply
     * calls this method, with arguments providing data that will allow
     * an object of type T to be constructed in addition the the
     * SimEventCallable argument.
     * @param handler this argument's <code>interactWith</code> method
     *                will be called with a server as its argument when
     *                the event is scheduled; may be null if the 
     *                <code>interactWith</code> method would simply
     *                return without performing any operations.
     * @param param the parameters used to determine how a queue insertion
     *              is done and how long to wait before the task is
     *              restarted once scheduled
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     * @return true on success; false on failure in which case the
     *         current thread will continue to run
     */
    public boolean addCurrentTask(final QueueServerHandler<QS> handler,
			       final T param, 
			       SimEventCallable callable)
    {
	if (deleting) return false;
	CallableReturns<Boolean> sqcallable = new CallableReturns<Boolean>() {
		public Boolean call() {
		    TaskThread thread = (TaskThread)(Thread.currentThread());
		    if (thread.queuingCanceled) {
			return Boolean.FALSE;
		    }
		    QS server = getIdleServer();
		    long interval = tq./*parseForInterval*/getInterval(param)
			+ server.getInterval();
		    if (interval > 0) {
			TaskThread.pause(interval);
		    }
		    try {
			if (handler != null) {
			    ServerQueue.this.fireSQServerInteraction
				((Object) server, thread.getOriginator(), 
				 thread.getTag());
			    handler.interactWith(server);
			}
		    } finally {
			putIdleServer(server);
			ServerQueue.this.fireSQTask
			    ((Object) server, thread.getOriginator(),
			     thread.getTag());
		    }
		    return Boolean.TRUE;
		}
	    };
	TaskEventCallable tcallable = new TaskEventCallable() {
		public SimulationEvent call(TaskThreadSimEvent event) {
		    TaskQueueSimEvent<T> result =
			new TaskQueueSimEvent<T>(getSimulation(),
						 tq, event, param);
		    int oldsize = 0;
		    if(nObservers > 0) {
			oldsize = tq.size() + (tq.isBusy()? 1: 0);
		    }
		    result.source = ServerQueue.this;
		    if (event.source == null) event.source = ServerQueue.this;
		    SimulationEvent simEventResult = tq.doAdd(result, 0, 0.0);
		    if (simEventResult == null) return null;

		    simEventResult.source = ServerQueue.this;
		    if (nObservers > 0 && 
			oldsize != (tq.size() + (tq.isBusy()? 1: 0))) {
			notifyQueueObservers();
		    }
		    Thread thread = Thread.currentThread();
		    ((TaskThread)thread).threadQueued = true;
		    ((TaskThread)thread).queuingCanceled = false;
		    return simEventResult;
		}
	    };
	return TaskThread.pause(tcallable, callable, sqcallable);
    }

    /**
     * Get the size of the queue.
     * The size does not include the currently scheduled event.
     * @return the queue size
     */
    public int size() {
	int sz =  tq.size();
	// If tq is busy, that means we've taken an element off
	// the queue, regardless of whether we gave it to a server.
	// tq will stay busy until the server finishes its processing.
	if (tq.isBusy()) {
	    sz++;
	}
	// if the entry is being processed, we incremented the inUse
	// count; we'll decrement that as processing for this entry
	// ends.
	if (tq.isProcessing()) {
	    sz--;
	}
	return sz;
    }

    /**
     * Determine if the queue is busy.
     * A queue is busy if all the servers are handling queue entries.
     * @return true if the queue is busy; false otherwise
     */
    public boolean isBusy() {
	if (nservers > 0) {
	    return (slist.size() == 0);
	} else {
	    return (tq.size() > 0);
	}
    }


    /**
     * Determine how many servers are in use.
     * @return the number of servers in use
     */
    public int inUseCount() {
	return nservers - slist.size();
    }


   /**
     * Determine the maximum number of servers.
     */
    public int serverCount() {
	return nservers;
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following.
     * <P>
     * Defined in {@link ServerQueue}:
     * <UL>
     *  <LI> the number of servers.
     *  <LI> the deletion policy.
     *  <LI> the value of the can freeze flag.
     *  <LI> the value of the concurrency limit.
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
	out.println(prefix + "number of servers: " + serverCount());
	out.println(prefix + "deletionPolicy: " + getDeletePolicy());
	out.println(prefix + "can freeze: " + canFreeze());
	out.println(prefix + "concurrency limit: " +  serverCount());
    }

    /**
     * {@inheritDoc}
     * In addition, the state that is printed includes the following.
     * <P>
     * Defined in {@link ServerQueue}:
     * <UL>
     *  <LI> the queue size.
     *  <LI> whether or the queue is frozen.
     *  <LI> whether or not the queue is busy.
     *  <LI> the number of 'customers' being serviced. 
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
	out.println(prefix + "busy: "  + isBusy());
	out.println(prefix + "number being serviced: " + inUseCount());
    }
}

//  LocalWords:  exbundle QueueServer QueueCallable QueueRunnable ul
//  LocalWords:  TaskThread li interactWith QueueServerHandler lt pre
//  LocalWords:  getInterval param ServerQueue's SimulationEvent sev
//  LocalWords:  addCurrentTask ServerQueue SimEventCallable sim tq
//  LocalWords:  SimEventCallable's blockquote taskQueue scheduleCall
//  LocalWords:  isCanceled IllegalArgumentException boolean Runnable
//  LocalWords:  superclass UnsupportedOperationException noAvailServ
//  LocalWords:  SimObjQueueCallable qcallable parseForInterval QS
//  LocalWords:  scriptObject noScriptEngine SimObjQueueRunnable
//  LocalWords:  qrunnable ScriptEngine getScriptEngine autoboxing
//  LocalWords:  IllegalStateException scriptSimEventCallable inUse
//  LocalWords:  printName deletionPolicy subclasses runnable iPrefix
