package org.bzdev.devqsim;

/**
 * Interface for obtaining state information about queues.
 * This interface is used by QueueObserver.  The queues
 * provided in the org.bzdev.devqsim package implement
 * this interface. As a result, the object representing a
 * queue's status will be the queue itself.  The rationale
 * for this interface is that TaskQueue and ServerQueue
 * are not subclasses of any type of queue. This interface
 * provides operations common to both for querying properties
 * of a queue and adding/removing observers.
 * <P>
 * The QueueObserver interface provides a single method that
 * will be called when a queue's status changes. The type of
 * this method's argument is an instance of QueueStatus (the
 * queue itself for the queues defined in this package).
 * @see org.bzdev.devqsim.QueueObserver
 */
public interface QueueStatus {
    /**
     * Add an observer.
     * @param observer the observer
     */
    void addObserver(QueueObserver observer);

    /**
     * Remove an observer.
     * @param observer the observer
     */
    void removeObserver(QueueObserver observer);

    /**
     * Determine if the queue is busy.
     * A queue is busy if all the servers are handling queue entries.
     * @return true if the queue is busy; false otherwise
     */
    boolean isBusy();

    /**
     * Determine how many servers are in use.
     * Equivalent to how many customers are being served.
     * @return the number of servers in use
     */
    int inUseCount();


    /**
     * Determine the maximum number of servers.
     * @return the maximum number of servers
     */
    int serverCount();

    /**
     * Get the size of the queue.
     * The size does not include the currently scheduled event.
     * @return the queue size
     */
    int size();

    /**
     * Determine if a queue is frozen.
     * @return true if it is frozen; false otherwise
     */
    boolean isFrozen();


    /**
     * Determine if a queue can be frozen.
     * @return true if a queue can be frozen; false otherwise
     */
    boolean canFreeze();

    /**
     * Determine if the queue has been deleted.
     * @return true if the queue is deleted; false otherwise
     */
    boolean isDeleted();

    /**
     * Get the name of the queue.
     * @return true if the queue is interned; false otherwise
     */
    String getName();

    /**
     * Determine if the queue is interned by a simulation.
     * A queue is interned if its simulation allows it to be looked
     * up by name.
     * @return true if the object is interned; false otherwise.
     */
    boolean isInterned();

}

//  LocalWords:  QueueObserver TaskQueue ServerQueue subclasses
//  LocalWords:  QueueStatus
