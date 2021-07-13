package org.bzdev.devqsim;

/**
 * Queue observer interface.
 * Used to monitor changes to a queue.
 */
public interface QueueObserver {

    /**
     * Signal a queue-change event.
     * @param qstatus an object (typically the queue itself) that allows
     *        one to determine the state of a queue
     */
    void onQueueChange(QueueStatus qstatus);
}

//  LocalWords:  qstatus
