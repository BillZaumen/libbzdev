package org.bzdev.devqsim;
import java.util.EventListener;

/**
 * Simulation listener class.
 */
public interface SimulationListener extends EventListener {
    /**
     * Process a simulation-state-change event.
     * @param e the event
     */
    void stateChanged(SimulationStateEvent e);
}
