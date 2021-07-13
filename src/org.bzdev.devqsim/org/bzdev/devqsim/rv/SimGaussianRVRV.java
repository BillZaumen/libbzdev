package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.rv.GaussianRVRV;

/**
 * Named random variable that generates a sequence of
 * Gaussian-distributed random variable whose values are interarrival
 * times.
 * Interarrival times are long integers, so the values for each
 * random variable in the sequence are rounded to the closest long integer.
 */
public class SimGaussianRVRV
    extends SimDoubleRVRV<GaussianRV,GaussianRVRV>
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
    protected SimGaussianRVRV(Simulation sim, String name, boolean intern) {
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
    public SimGaussianRVRV(Simulation sim, String name, boolean intern,
			   GaussianRVRV rv)
    {
	super(sim, name, intern, rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
