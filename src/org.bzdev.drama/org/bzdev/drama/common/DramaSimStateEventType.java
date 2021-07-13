package org.bzdev.drama.common;

/**
 * Drama-specific simulation-state-event type.
 * The package org.bzdev.devqsim defines an enumeration for a
 * number of simulation-state event types.  This class provides
 * additional event types specific to drama-like simulation flavors.
 * @see org.bzdev.devqsim.SimulationStateEvent.Type
 */
public enum DramaSimStateEventType {
    /**
     * A message is about to be received
     */
    RECEIVE_START,
    /**
     * A message has just been received.
     */
    RECEIVE_END
}
