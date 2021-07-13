package org.bzdev.devqsim;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.ScriptListenerAdapter;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ExpressionParser.ESPObject;

//@exbundle org.bzdev.devqsim.lpack.Simulation

/**
 * Default adapter for simulation listeners.
 * This class will typically be overridden by a subclass to implement methods
 * other than stateChanged.  With the exception of the stateChanged methods,
 * the remaining methods do nothing.
 * <P>
 * Additional simulation packages will generally extend DefaultSimAdapter
 * to add additional events.  In these subclasses. additional event types
 * are named by providing an enumeration (OurStateEventType in the example)
 * and by seeing if the event supports that type.  If it does not, the
 * processing should be handed to the adapter's superclass:
 *  <blockquote><pre>
 *     public void stateChanged(SimulationStateEvent e) {
 *        Simulation sim = e.getSimulation)();
 *	  OurStateEventType et = e.getType(OurStateEventType.class);
 *	  if (et == null) {
 *	    super.stateChanged(e);
 *	    return;
 *	  }
 *	  switch (et) {
 *            // handle each case.
 *           ...
 *        }
 *     }
 *  </pre></blockquote>
 * This design pattern lets each simulation handle events in a type-secure
 * way that is extensible.
 * <P>
 * The zero-argument constructor for this class will create an adapter
 * whose methods (except for {@link #stateChanged(SimulationStateEvent)}
 * and the protected method
 * {@link ScriptListenerAdapter#callScriptMethod(String, Object...)}) can
 * be overridden to perform some action.  The two-argument constructor
 * allows the implementation of each public method (except, of course,
 * {@link #stateChanged(SimulationStateEvent)}) to be provided using a
 * scripting language.
 */

public class DefaultSimAdapter extends ScriptListenerAdapter
    implements SimulationListener
{

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    /**
     * Constructor.
     * This creates an adapter with no scripting support.
     */
    public DefaultSimAdapter() {
	super();
    }

    /**
     * Constructor with only a script object.
     * This creates an adapter for use with the ESP scripting language.
     * <P>
     * Note: This is equivalent to using the constructor
     * {@link #DefaultSimAdapter(ScriptingContext,Object)} with
     * a null first argument.
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public DefaultSimAdapter(ESPObject scriptObject) {
	this(null, scriptObject);
    }


    /**
     * Constructor given a scripting context and script object.
     * This constructor implements the adapter using a scripting language
     * provided its arguments are not null. If a method is added to the
     * script object after this constructor is called, that method will
     * be ignored, so all of the methods the adapter implements must be
     * defined by the script object when this constructor is called.
     * <P>
     * If ESP is the scripting language, the context may be null provided
     * that scriptObject is an ESP object.  This special case is provided
     * for use with {@link org.bzdev.obnaming.ObjectNamerLauncher} and the
     * yrunner program.
     * @param context the scripting context for this adapter
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     * @exception IllegalArgumentException the script object was ill formed
     */
    public DefaultSimAdapter(ScriptingContext context, Object scriptObject)
	throws IllegalArgumentException
    {
	super(context, scriptObject);
    }

    /**
     * Process a simulation-state-change event.
     * The implementation dispatches the processing to the other
     * methods in this class, based on the event type. If this
     * method is overridden, the instructions in the class documention
     * for {@link DefaultSimAdapter} must be followed.
     * @param e the event
     */
    public void stateChanged(SimulationStateEvent e) {
	Simulation sim = e.getSimulation();
	// System.out.println("found " +e.getType());
        SimulationStateEvent.Type et =
	    e.getType(SimulationStateEvent.Type.class);
	if (et == null) return;
	switch (et) {
	case SIM_START:
	    simulationStart(sim);
	    break;
	case SIM_STOP:
	    simulationStop(sim);
	    break;
	case CALL_START:
	    {
		Object source = e.getSource();
		String tag = e.getParameter(String.class);
		if (source instanceof SimObject) {
		    callStartSimObject(sim, (SimObject) source, tag);
		} else if (source instanceof Simulation) {
		    callStart(sim, tag);
		}
	    }
	    break;
	case CALL_END:
	    {
		Object source = e.getSource();
		String tag = e.getParameter(String.class);
		if (source instanceof SimObject) {
		    callEndSimObject(sim, (SimObject) source, tag);
		} else if (source instanceof Simulation) {
		    callEnd(sim, tag);
		}
	    }
	    break;
	case TASK_START:
	    {
		String tag = e.getParameter(String.class);
		Object source = e.getSource();
		if (source instanceof Simulation) {
		    taskStart(sim, tag);
		} else if (source instanceof SimObject) {
		    taskStartSimObject(sim, (SimObject) source, tag);
		}
	    }
	    break;
	case TASK_PAUSE:
	    {
		String tag = e.getParameter(String.class);
		Object source = e.getSource();
		if (source instanceof Simulation) {
		    taskPause(sim, tag);
		} else if (source instanceof SimObject) {
		    taskPauseSimObject(sim, (SimObject) source, tag);
		}
	    }
	    break;
	case TASK_RESUME:
	    {
		String tag = e.getParameter(String.class);
		Object source = e.getSource();
		if (source instanceof Simulation) {
		    taskResume(sim, tag);
		} else if (source instanceof SimObject) {
		    taskResumeSimObject(sim, (SimObject) source, tag);
		}
	    }
	    break;
	case TASK_END:
	    {
		String tag = e.getParameter(String.class);
		Object source = e.getSource();
		if (source instanceof Simulation) {
		    taskEnd(sim, tag);
		} else if (source instanceof SimObject) {
		    taskEndSimObject(sim, (SimObject) source, tag);
		}
	    }
	    break;
	case TASKQUEUE_START:
	    {
		TaskQueue q = (TaskQueue) e.getSource();
		taskQueueStart(sim, q);
	    }
	    break;
	case TASKQUEUE_PAUSE:
	    {
		TaskQueue q = (TaskQueue) e.getSource();
		taskQueuePause(sim, q);
	    }
	    break;
	case TASKQUEUE_RESUME:
	    {
		TaskQueue q = (TaskQueue) e.getSource();
		    taskQueueResume(sim, q);
	    }
	    break;
	case TASKQUEUE_STOP:
	    {
		TaskQueue q = (TaskQueue) e.getSource();
		    taskQueueStop(sim, q);
	    }
	    break;
	case SERVER_SELECTED:
	    {
		ServerQueue q = (ServerQueue) e.getSource();
		serverSelected(sim, q);
	    }
	    break;
	case SQ_INTERACTION:
	    {
		ServerQueue q = (ServerQueue) e.getSource();
		SimObject target = e.getOrigin(SimObject.class);
		String tag = e.getParameter(String.class);
		Object server = e.getServer();
		if (target == null) {
		    serverInteraction(sim, q, server, tag);
		} else {
		    serverInteractionSimObject(sim, q, server, target, tag);
		}
	    }
	    break;
	case SQ_CALLABLE:
	    {
		ServerQueue q = (ServerQueue) e.getSource();
		SimObject target = e.getOrigin(SimObject.class);
		String tag = e.getParameter(String.class);
		Object server = e.getServer();
		if (target == null) {
		    serverCallable(sim, q, server, tag);
		} else {
		    serverCallableSimObject(sim, q, server, target, tag);
		}
		
	    }
	    break;
	case SQ_RUNNABLE:
	    {
		ServerQueue q = (ServerQueue) e.getSource();
		SimObject target = e.getOrigin(SimObject.class);
		String tag = e.getParameter(String.class);
		Object server = e.getServer();
		if (target == null) {
		    serverRunnable(sim, q, server, tag);
		} else {
		    serverRunnableSimObject(sim, q, server, target, tag);
		}
	    }
	    break;
	case SQ_TASK:
	    {
		ServerQueue q = (ServerQueue) e.getSource();
		SimObject target = e.getOrigin(SimObject.class);
		String tag = e.getParameter(String.class);
		Object server = e.getServer();
		if (target == null) {
		    serverTask(sim, q, server, tag);
		} else {
		    serverTaskSimObject(sim, q, server, target, tag);
		}
	    }
	    break;
	default:
	    throw new  RuntimeException(errorMsg("missingCase"));
	}
    }

    /**
     * Indicate that a simulation has started.
     * @param sim the simulation
     */
    public void simulationStart(Simulation sim) {
	callScriptMethod("simulationStart", sim);
    }

    /**
     * Indicate that a simulation has stopped.
     * @param sim the simulation
     */
    public void simulationStop(Simulation sim) {
	callScriptMethod("simulationStop", sim);
    }


    /**
     * Indicate that a simulation has started to process a scheduled call.
     * The call was scheduled using a simulation's method
     * <code>scheduleCall</code>, <code>scheduleScript</code>, or
     * <code>scheduleCallObject</code>, and is not attributed to any
     * particular simulation object. The argument representing the
     * call must be either a Callable, a string providing a script
     * to execute, or an object in a scripting language with a
     * "call" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void callStart(Simulation sim, String tag){
	callScriptMethod("callStart", sim, tag);
    }

    /**
     * Indicate that a simulation has finished processing a scheduled call.
     * The call was scheduled using a simulation's method
     * <code>scheduleCall</code>, <code>scheduleScript</code>, or
     * <code>scheduleCallObject</code>, and is not attributed to any
     * particular simulation object. The argument representing the
     * call must be either a Callable, a string providing a script
     * to execute, or an object in a scripting language with a
     * "call" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void callEnd(Simulation sim, String tag){
	callScriptMethod("callEnd", sim, tag);
    }

    /**
     * Indicate that a simulation has started to process
     * a scheduled call associated with a simulation object.
     * The call was created using a simulation object's protected method
     * named <code>callableScript</code>, <code>scheduleScript</code>,
     * <code>scheduleCall</code>, <code>scheduleCallObject</code>, or
     * <code>bindCallable</code> and is attributed to that simulation object.
     * @param sim the simulation
     * @param obj the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void callStartSimObject(Simulation sim, SimObject obj, String tag){
	callScriptMethod("callStartSimObject", sim, obj, tag);
    }

    /**
     * Indicate that a simulation has finished processing
     * a scheduled call associated with a simulation object..
     * The call was created using a simulation object's protected method
     * named <code>callableScript</code>, <code>scheduleScript</code>,
     * <code>scheduleCall</code>, <code>scheduleCallObject</code>, or
     * <code>bindCallable</code> and is attributed to that simulation object.
     * @param sim the simulation
     * @param obj the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void callEndSimObject(Simulation sim, SimObject obj, String tag){
	callScriptMethod("callEndSimObject", sim, obj, tag);
    }

    /**
     * Indicate that a simulation has started a task.
     * The task was created by a simulation's public method
     * <code>scheduleTask</code>, <code>scheduleTaskScript</code>,
     * <code>scheduleTaskObject</code>, <code>scheduleTaskWP</code>,
     * <code>startImmediateTask</code>, or <code>unscheduledTaskThread</code>
     * and is not attributed to any particular simulation object. The
     * argument representing a task must be either a Runnable, a string
     * providing a script to execute, or an object in a scripting language
     * with a "run" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void taskStart(Simulation sim, String tag){
	callScriptMethod("taskStart", sim, tag);
    }

    /**
     * Indicate that a task a simulation is running has paused.
     * The task was created by a simulation's public method
     * <code>scheduleTask</code>, <code>scheduleTaskScript</code>,
     * <code>scheduleTaskObject</code>, <code>scheduleTaskWP</code>,
     * <code>startImmediateTask</code>, or <code>unscheduledTaskThread</code>
     * and is not attributed to any particular simulation object. The
     * argument representing a task must be either a Runnable, a string
     * providing a script to execute, or an object in a scripting language
     * with a "run" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void taskPause(Simulation sim, String tag) {
	callScriptMethod("taskPause", sim, tag);
    }

    /**
     * Indicate that a task a simulation is running has resumed.
     * The task was created by a simulation's public method
     * <code>scheduleTask</code>, <code>scheduleTaskScript</code>,
     * <code>scheduleTaskObject</code>, <code>scheduleTaskWP</code>,
     * <code>startImmediateTask</code>, or <code>unscheduledTaskThread</code>
     * and is not attributed to any particular simulation object. The
     * argument representing a task must be either a Runnable, a string
     * providing a script to execute, or an object in a scripting language
     * with a "run" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void taskResume(Simulation sim,String tag){
	callScriptMethod("taskResume", sim, tag);
    }

    /**
     * Indicate that a task a simulation is running has ended.
     * The task was created by a simulation's public method
     * <code>scheduleTask</code>, <code>scheduleTaskScript</code>,
     * <code>scheduleTaskObject</code>, <code>scheduleTaskWP</code>,
     * <code>startImmediateTask</code>, or <code>unscheduledTaskThread</code>
     * and is not attributed to any particular simulation object. The
     * argument representing a task must be either a Runnable, a string
     * providing a script to execute, or an object in a scripting language
     * with a "run" method that has no arguments.
     * @param sim the simulation
     * @param tag a description of the event; null if there is none
     */
    public void taskEnd(Simulation sim, String tag) {
	callScriptMethod("taskEnd", sim, tag);

    }

    /**
     * Indicate that a task associated with a simulation object has started.
     * The task was created using a simulation object's protected method
     * <code>unscheduledTaskThread</code>, <code>scheduleTask</code>,
     * <code>scheduleTaskScript</code>, <code>scheduleTaskObject</code>
     * <code>startImmediateTask</code>, <code>runnableScript</code>,
     * <code>runnableObject</code>, or <code>bindRunnable</code>
     * and is attributed to the simulation object.
     * @param sim the simulation
     * @param owner the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void taskStartSimObject(Simulation sim, SimObject owner, String tag)
    {
	callScriptMethod("taskStartSimObject", sim, owner, tag);

    }

    /**
     * Indicate that a task associated with a simulation object has paused.
     * The task was created using a simulation object's protected method
     * <code>unscheduledTaskThread</code>, <code>scheduleTask</code>,
     * <code>scheduleTaskScript</code>, <code>scheduleTaskObject</code>
     * <code>startImmediateTask</code>, <code>runnableScript</code>,
     * <code>runnableObject</code>, or <code>bindRunnable</code>
     * and is attributed to the simulation object.
     * @param sim the simulation
     * @param owner the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void taskPauseSimObject(Simulation sim, SimObject owner,String tag)
    {
	callScriptMethod("taskPauseSimObject", sim, owner, tag);
    }

    /**
     * Indicate that a task associated with a simulation object has resumed.
     * The task was created using a simulation object's protected method
     * <code>unscheduledTaskThread</code>, <code>scheduleTask</code>,
     * <code>scheduleTaskScript</code>, <code>scheduleTaskObject</code>
     * <code>startImmediateTask</code>, <code>runnableScript</code>,
     * <code>runnableObject</code>, or <code>bindRunnable</code>
     * and is attributed to the simulation object.
     * @param sim the simulation
     * @param owner the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void taskResumeSimObject(Simulation sim, SimObject owner, String tag)
    {
	callScriptMethod("taskResumeSimObject", sim, owner, tag);
    }

    /**
     * Indicate that a task associated with a simulation object has ended.
     * The task was created using a simulation object's protected method
     * <code>unscheduledTaskThread</code>, <code>scheduleTask</code>,
     * <code>scheduleTaskScript</code>, <code>scheduleTaskObject</code>
     * <code>startImmediateTask</code>, <code>runnableScript</code>,
     * <code>runnableObject</code>, or <code>bindRunnable</code>
     * and is attributed to the simulation object.
     * @param sim the simulation
     * @param owner the simulation object
     * @param tag a description of the event; null if there is none
     */
    public void taskEndSimObject(Simulation sim, SimObject owner, String tag)
    {
	callScriptMethod("taskEndSimObject", sim, owner, tag);
    }

    /**
     * Indicate that processing of an element on a task queue has started.
     * Subsequent calls to other adapter methods may indicate details of that
     * processing.
     * @param sim the simulation
     * @param q the task queue
     */
    public void taskQueueStart(Simulation sim, TaskQueue q) {
	callScriptMethod("taskQueueStart", sim, q);
    }

    /**
     * Indicate that processing of an element on a task queue has paused.
     * Subsequent calls to other adapter methods may indicate details of that
     * processing.
     * @param sim the simulation
     * @param q the task queue
     */
    public void taskQueuePause(Simulation sim, TaskQueue q) {
	callScriptMethod("taskQueuePause", sim, q);
    }

    /**
     * Indicate that processing of an element on a task queue has resumed.
     * Subsequent calls to other adapter methods may indicate details of that
     * processing.
     * @param sim the simulation
     * @param q the task queue
     */
    public void taskQueueResume(Simulation sim, TaskQueue q) {
	callScriptMethod("taskQueueResume", sim, q);
    }

    /**
     * Indicate that processing of an element on a task queue has ended.
     * @param sim the simulation
     * @param q the task queue
     */
    public void taskQueueStop(Simulation sim, TaskQueue q) {
	callScriptMethod("taskQueueStop", sim, q);
    }


    /**
     * Indicate that a server queue has selected a server.
     * @param sim the simulation
     * @param q the server queue
     */
    public void serverSelected(Simulation sim, ServerQueue q) {
	callScriptMethod("serverSelected", sim, q);
    }

    /**
     * Indicate that one of a server queue's servers is interacting with a
     * request to handle it.
     * For this method, the task or callable is not associated with a
     * simulation object.
     * @param sim the simulation
     * @param q the server queue
     * @param server the server handling the request
     * @param tag a description of the event; null if there is none
    */
    public void serverInteraction(Simulation sim, ServerQueue q,
				  Object server, String tag)
    {
	callScriptMethod("serverInteraction", sim, q, server, tag);
    }

    /**
     * Indicates that a server has completed processing and the
     * Callable that was queued is being processed. This callable
     * represents the processing that occurs after the queue is
     * finished with the request.
     * For this method, the callable is not associated with a simulation object.
     * @param sim the simulation
     * @param q the server queue
     * @param server the server handling the request
     * @param tag a description of the event; null if there is none
     */
    public void serverCallable(Simulation sim, ServerQueue q,
			       Object server, String tag)
    {
	callScriptMethod("serverCallable", sim, q, server, tag);
    }

    /**
     * Indicate that a server has completed and the Runnable that was
     * queued is being processed.  This Runnable
     * represents the processing that occurs after the queue is
     * finished with the request.
     * For this method, the task is not associated with a simulation object.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling the request
     * @param tag a description of the event; null if there is none
     */
    public void serverRunnable(Simulation sim, ServerQueue q,
			       Object server, String tag)
    {
	callScriptMethod("serverRunnable", sim, q, server, tag);
    }

    /**
     * Indicate that a server has completed and the task that was
     * queued is continuing to execute.
     * For this method, the task is not associated with a simulation object.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling the request
     * @param tag a description of the event; null if there is none
     */
    public void serverTask(Simulation sim, ServerQueue q,
			   Object server, String tag)
    {
	callScriptMethod("serverTask", sim, q, server, tag);
    }


    /**
     * Indicate that one of a server queue's servers is handling a
     * request by interacting with it on behalf of a simulation object.
     * The simulation object's protected method bindCallable or
     * bindRunnable was used to create the Callable or Runnable and associate it
     * with the simulation object, or a thread was created using the
     * simulation object's unscheduledTaskThread or scheduleTask methods.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling a request
     * @param target the object associated with the Callable, Runnable, or
     *        task that was queued
     * @param tag a description of the event; null if there is none
     */
    public void serverInteractionSimObject(Simulation sim, ServerQueue q,
					   Object server,
					   SimObject target, String tag)
    {
	callScriptMethod("serverInteractionSimObject",
			 sim, q, server, target, tag);
    }

    /**
     * Indicate that a server has completed and the Callable
     * (associated with a simulation object) that was queued is being
     * processed.
     * The simulation object's protected method bindCallable was used
     * to create the Callable and associate it with the simulation object.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling the request
     * @param target the object associated with the Callable that was queued
     * @param tag a description of the event; null if there is none
     */
    public void serverCallableSimObject(Simulation sim, ServerQueue q,
					Object server,
					SimObject target, String tag)
    {
	callScriptMethod("serverCallableSimObject",
			 sim, q, server, target, tag);
    }

    /**
     * Indicate that a server has completed and the Runnable (associated with
     * a simulation object) that was queued is being processed.
     * The simulation object's protected method bindRunnable was used
     * to create the Runnable and associate it with the simulation object.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling the request
     * @param target the object associated with the Runnable or
     *        task that was queued
     * @param tag a description of the event; null if there is none
     */
    public void serverRunnableSimObject(Simulation sim, ServerQueue q,
					Object server,
					SimObject target, String tag)
    {
	callScriptMethod("serverRunnableSimObject",
			 sim, q, server, target, tag);
    }

    /**
     * Indicate that a server has completed and the task that was
     * queued and that is associated with a simulation object is
     * continuing to execute.
     * The task was associated with a simulation object when the task
     * was created by creating the task by using one of the simulation object's
     * unscheduledTaskThread or scheduleTask methods.
     * @param sim the simulation
     * @param q the task queue
     * @param server the server handling the request
     * @param target the object associated with Runnable or
     *        task that was queued
     * @param tag a description of the event; null if there is none
     */
    public void serverTaskSimObject(Simulation sim, ServerQueue q,
				    Object server,
				    SimObject target, String tag)
    {
	callScriptMethod("serverTaskSimObject", sim, q, server, target, tag);
    }
}

//  LocalWords:  exbundle stateChanged DefaultSimAdapter subclasses
//  LocalWords:  OurStateEventType superclass blockquote pre et
//  LocalWords:  SimulationStateEvent getSimulation callScriptMethod
//  LocalWords:  ScriptListenerAdapter ScriptingContext scriptObject
//  LocalWords:  yrunner IllegalArgumentException documention getType
//  LocalWords:  missingCase simulationStart simulationStop callStart
//  LocalWords:  scheduleCall scheduleScript scheduleCallObject
//  LocalWords:  callEnd callableScript bindCallable callEndSimObject
//  LocalWords:  callStartSimObject scheduleTask scheduleTaskScript
//  LocalWords:  scheduleTaskObject scheduleTaskWP startImmediateTask
//  LocalWords:  unscheduledTaskThread Runnable taskStart taskPause
//  LocalWords:  taskResume taskEnd runnableScript runnableObject
//  LocalWords:  bindRunnable taskStartSimObject taskPauseSimObject
//  LocalWords:  taskResumeSimObject taskEndSimObject taskQueueStart
//  LocalWords:  taskQueuePause taskQueueResume taskQueueStop
//  LocalWords:  serverSelected serverInteraction serverCallable
//  LocalWords:  serverRunnable serverTask serverInteractionSimObject
//  LocalWords:  serverCallableSimObject serverRunnableSimObject
//  LocalWords:  serverTaskSimObject
