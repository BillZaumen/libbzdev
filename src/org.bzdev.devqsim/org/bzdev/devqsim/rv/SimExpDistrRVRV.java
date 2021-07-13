package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.ExpDistrRV;
import org.bzdev.math.rv.ExpDistrRVRV;

/**
 * Named random variable that generates a sequence of
 * exponentially-distributed random variable.
 */
public class SimExpDistrRVRV
    extends SimDoubleRVRV<ExpDistrRV,ExpDistrRVRV>
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
    protected SimExpDistrRVRV(Simulation sim, String name, boolean intern) {
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
    public SimExpDistrRVRV(Simulation sim, String name, boolean intern,
			   ExpDistrRVRV rv)
    {
	super(sim, name, intern, rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
