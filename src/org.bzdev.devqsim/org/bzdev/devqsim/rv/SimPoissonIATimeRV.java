package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.PoissonIATimeRV;

/**
 * Named random variable using a random variable that generates a
 * sequence of long integers with an exponential distribution and
 * with each value statistically independent from the others. The
 * number of interarrivals in a given interval then follows a Poisson
 * distribution.
 * @see org.bzdev.math.rv.PoissonIATimeRV
 */
public class SimPoissonIATimeRV extends SimInterarrivalTimeRV<PoissonIATimeRV>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    protected SimPoissonIATimeRV(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Constructor given a random variable.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * @param rv the random variable itself
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimPoissonIATimeRV(Simulation sim, String name, boolean intern,
			      PoissonIATimeRV rv)
    {
	super(sim, name, intern, rv);
	super.setRV(rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
