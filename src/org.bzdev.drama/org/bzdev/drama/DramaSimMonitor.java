package org.bzdev.drama;
import org.bzdev.devqsim.*;


/**
 * Simulation-monitor for instances of DramaSimulation.
 * This class is provided for convenience.  Subclasses
 * must implement the method 
 * {@link org.bzdev.devqsim.SimulationMonitor#simulationPauses() simulationPauses()},
 * which returns a boolean value (true if the simulation should pause;
 * false if it should not.
 * @see org.bzdev.devqsim.SimulationMonitor#simulationPauses()
 */
public abstract class DramaSimMonitor
    extends SimulationMonitor<DramaSimulation>
{
    /**
     * Constructor.
     */
    public DramaSimMonitor() {
	super();
    }

    /**
     * Constructor given a simulation.
     * @param sim the simulation to monitor
     */
    public DramaSimMonitor(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  DramaSimulation Subclasses simulationPauses boolean
