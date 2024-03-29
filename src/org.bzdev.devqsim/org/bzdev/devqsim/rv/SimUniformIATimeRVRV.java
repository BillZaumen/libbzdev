package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformIATimeRV;
import org.bzdev.math.rv.UniformIATimeRVRV;

/**
 * Named random variable using a random variable that generates a
 * sequence of long-integer valued random variableac, eachrepresenting
 * exponentially distributed interarrival times, that is suitable for
 * modeling arrivals that have a uniform distribution.
 * @see org.bzdev.math.rv.UniformIATimeRV
 */
public class SimUniformIATimeRVRV
    extends SimInterarrivalTimeRVRV<UniformIATimeRV,UniformIATimeRVRV>
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
    protected SimUniformIATimeRVRV(Simulation sim, String name,
				   boolean intern)
    {
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
    public SimUniformIATimeRVRV(Simulation sim, String name, boolean intern,
				  UniformIATimeRVRV rv)
    {
	super(sim, name, intern);
	super.setRV(rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
