package org.bzdev.devqsim;

/**
 * Interface for recording a task-thread simulation event.
 */
public interface TaskEventCallable {
    /**
     * Interface to allow task threads to be manipulated during task-thread
     * processing.
     * This allows some operations used by the implementation to occur
     * while synchronizing on particular objects used internally by
     * TaskThread methods.  The event returned may not be the event
     * passed as an argument (e.g., the return value may be an event
     * that is stored on a TaskQueue, not the simulation's event queue).
     * @param event the event used to schedule a task
     * @return the simulation event scheduled or stored; null if the
     *         the scheduling or storing is not possible
     * @see org.bzdev.devqsim.TaskThread
     */
    SimulationEvent call(TaskThreadSimEvent event);
}

//  LocalWords:  TaskThread TaskQueue
