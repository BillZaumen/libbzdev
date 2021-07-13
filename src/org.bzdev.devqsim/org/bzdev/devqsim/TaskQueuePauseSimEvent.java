package org.bzdev.devqsim;
import java.util.*;

/**
 * Task queue event for pausing a thread.
 */
class TaskQueuePauseSimEvent<T> extends SimulationEvent {
    boolean canceled = false;
    Simulation sim;
    TaskQueue<T> taskQueue;
    TaskThreadSimEvent tevent;
    boolean scheduled = false;

    /**
     * Constructor.
     * @param sim the simulation
     * @param taskQueue the task queue
     * @param event the task-thread simulation event associated with pausing
     *              the corresponding task thread
     */
    TaskQueuePauseSimEvent(Simulation sim, 
				 TaskQueue<T> taskQueue,
				 TaskThreadSimEvent event)
    {
	this.sim = sim;
	this.taskQueue = taskQueue;
	this.tevent = event;
	this.source = taskQueue;
    }

    // We don't call super.cancel() because the event may not
    // be on the simulation event queue, so we use taskQueue.cancelEvent
    // instead, which handles descheduling when needed.
    public boolean cancel() {
	if (canceled) return false;
	canceled = tevent.cancel();
	if (canceled) {
	    taskQueue.cancelEvent(this, this.scheduled);
	}
	return canceled;
    }

    protected void processEvent() {
	if (!canceled) {
	    if (source instanceof SimObject) {
		((SimObject)source).fireTaskQueueStart(true);
		taskQueue.doBeforeTaskEvent(this);
		tevent.processEvent();
		taskQueue.doAfterTaskEvent(this);
		((SimObject)source).fireTaskQueueStop(taskQueue.processing);
	    } else {
		taskQueue.doBeforeTaskEvent(this);
		tevent.processEvent();
		taskQueue.doAfterTaskEvent(this);
	    }
	}
	/*
	taskQueue.doBeforeTaskEvent(this);
	if (!canceled) tevent.processEvent();
	taskQueue.doAfterTaskEvent(this);
	*/
	canceled = true;	// in case a ref exists (don't cancel again)
    }
}
