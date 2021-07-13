package org.bzdev.devqsim;
import java.util.*;

/**
 * Simulation event for task queues.
 * This class represents the elements that will be stored on a TaskQueue's
 * internal queue.
 * The parameter T is the class for arguments used to pass
 * data associated with a task (e.g., the time to wait before the
 * task is activated once it reaches the head of the queue. It
 * should match the parameter used for the matching  TaskQueue.
 */
public class TaskQueueSimEvent<T> extends SimulationEvent {
    boolean canceled = false;
    Simulation sim;
    long offQueueTime;

    TaskQueue<T> taskQueue;
    TaskSimulationEvent tevent;
    T parameters;

    boolean scheduled = false;
    /**
     * Constructor.
     * @param sim the simulation
     * @param taskQueue the task queue
     * @param event the task simulation event that will control the task
     * @param parameters the parameters associated with this event
     */
    TaskQueueSimEvent(Simulation sim, 
		      TaskQueue<T> taskQueue,
		      TaskSimulationEvent event,
		      T parameters) {
	this.sim = sim;
	this.taskQueue = taskQueue;
	this.tevent = event;
	this.parameters = parameters;
	this.source = taskQueue;
    }

    // We don't call super.cancel() because the event may not
    // be on the simulation event queue, so we use taskQueue.cancelEvent
    // instead, which handles descheduling when needed.
    public boolean cancel() {
	if (canceled) return false;
	// for a TaskThreadSimEvent, we have a running thread and we
	// cannot cancel it with out preventing it from completing, so
	// we avoid canceling the thread-specific event but do cancel the
	// one associated with a queue entry.
	// canceled = (tevent instanceof TaskThreadSimEvent)? true:
	//    tevent.cancel();
	if (tevent instanceof TaskThreadSimEvent) {
	    if (((TaskThreadSimEvent)tevent).thread.threadQueued == false) {
		return false;
	    } else {
		((TaskThreadSimEvent)tevent).thread.queuingCanceled = true;
		canceled = true;
	    }
	} else {
	    canceled = tevent.cancel();
	}
	if (canceled) {
	    taskQueue.cancelEvent(this, this.scheduled);
	}
	return canceled;
    }

    public boolean isCanceled() {return canceled;}


    // Note: if tevent restarts a suspended task thread, then that task will
    // either run to completion or pause. If it pauses, the current
    // thread will continue, causing doAfterTaskEvent to be called.
    // Task threads and the simulation thread never run concurrently and
    // when a task thread pauses, the simulation thread immediately
    // restarts.
    protected void processEvent() {
	if (!canceled && tevent != null) {
	    if (source instanceof TaskQueue) {
		((SimObject)source).fireTaskQueueStart(false);
		taskQueue.doBeforeTaskEvent(this);
		tevent.processEvent();
		taskQueue.doAfterTaskEvent(this);
		((SimObject)source).fireTaskQueueStop(taskQueue.processing);
	    } else if (source instanceof ServerQueue) {
		((SimObject)source).fireServerQueueSelectServer();
		taskQueue.doBeforeTaskEvent(this);
		tevent.processEvent();
		taskQueue.doAfterTaskEvent(this);
		
	    } else {
		taskQueue.doBeforeTaskEvent(this);
		tevent.processEvent();
		taskQueue.doAfterTaskEvent(this);
	    }
	}
	canceled = true; 	//in case a ref exists (no cancel after it ran)
    }
}

//  LocalWords:  TaskQueue's TaskQueue taskQueue cancelEvent tevent
//  LocalWords:  descheduling TaskThreadSimEvent instanceof
//  LocalWords:  doAfterTaskEvent
