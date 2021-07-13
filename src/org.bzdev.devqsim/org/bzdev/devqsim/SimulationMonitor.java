package org.bzdev.devqsim;

/**
 * Determine when a simulation should pause.
 * Instances of this class can be used to determine when a simulation
 * should pause, based on user-defined criteria.  When passed as an
 * argument to {@link Simulation#run(SimulationMonitor)}, the simulation
 * loop will call {@link SimulationMonitor#simulationPauses()} once per
 * iteration, and will terminate if {@link SimulationMonitor#simulationPauses()}
 * returns true.
 */
abstract public class SimulationMonitor<S extends Simulation> {
   /**
     * Determines if a simulation should pause. or not.
     * @return true if the simulation should pause; false otherwise
     */
    public abstract boolean simulationPauses();

    S sim;

    protected S getSimulation() {return sim;}

    /**
     * Constructor.
     * This construct does not allow the use of simulation methods.
     */
    public SimulationMonitor() {
	sim = null;
    }

    /**
     * Constructor with simulation.
     * The use of this method allows the method simulationPauses() to
     * use various simulation methods (e.g., to look up specific objects by
     * name).
     * @param sim the simulation
     */
    public SimulationMonitor(S sim) {
	this.sim = sim;
    }
}

//  LocalWords:  SimulationMonitor simulationPauses sim
