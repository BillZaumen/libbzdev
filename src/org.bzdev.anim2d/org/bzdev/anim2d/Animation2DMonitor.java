package org.bzdev.anim2d;
import org.bzdev.devqsim.*;

/**
 * Simulation-monitor for instances of Animation2D.
 * This class is provided for convenience.  Subclasses
 * must implement the method 
 * {@link org.bzdev.devqsim.SimulationMonitor#simulationPauses() simulationPauses()},
 * which returns a boolean value (true if the simulation should pause;
 * false if it should not.
 * @see org.bzdev.devqsim.SimulationMonitor#simulationPauses()
 */
public abstract class Animation2DMonitor
    extends SimulationMonitor<Animation2D>
{
    /**
     * Constructor.
     */
    public Animation2DMonitor() {
	super();
    }

    /**
     * Constructor given an animation.
     * @param a2d the animation to monitor
     */
    public Animation2DMonitor(Animation2D a2d) {
	super(a2d);
    }
}
