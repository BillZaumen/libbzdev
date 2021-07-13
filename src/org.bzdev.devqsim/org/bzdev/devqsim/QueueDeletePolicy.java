package org.bzdev.devqsim;

/**
 * Policy for determining if a queue can be deleted.
 */
public enum QueueDeletePolicy {
    /**
     * A queue must be empty and not processing any more elements
     *  before it can be deleted
     */
    MUST_BE_EMPTY,
    /**
     * A queue will not accept new entries once delete() is called
     * and will be deleted when it is empty and not processing any 
     * more elements
     */
    WHEN_EMPTY,
    /**
     * A queue can never be deleted
     */
    NEVER
}
