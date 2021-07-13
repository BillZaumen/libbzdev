package org.bzdev.devqsim;

/**
 * Interface to provide a method that can be called when a simulation event 
 * is created.  Used when tasks are added to queues so that the simulation
 * event that will be queued can be optionally stored (e.g., so the event
 * can be removed from the queue or canceled.
 * 
 */
public interface SimEventCallable {
    /**
     * Method to call when a simulation event is created.
     * @param event the simulation event
     */
    void call(SimulationEvent event);
}
