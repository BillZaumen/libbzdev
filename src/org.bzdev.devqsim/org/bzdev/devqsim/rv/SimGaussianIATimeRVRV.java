package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianIATimeRV;
import org.bzdev.math.rv.GaussianIATimeRVRV;

/**
 * Named random variable that generates a Gaussian-distributed random
 * variable.
 */
public class SimGaussianIATimeRVRV
    extends SimInterarrivalTimeRVRV<GaussianIATimeRV,GaussianIATimeRVRV>
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
    protected SimGaussianIATimeRVRV(Simulation sim, String name, boolean intern) {
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
    public SimGaussianIATimeRVRV(Simulation sim, String name, boolean intern,
			   GaussianIATimeRVRV rv)
    {
	super(sim, name, intern, rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
