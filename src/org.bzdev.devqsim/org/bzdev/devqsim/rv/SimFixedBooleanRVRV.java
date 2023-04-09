package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedBooleanRV;
import org.bzdev.math.rv.FixedBooleanRVRV;

/**
 * Named random variable using a random variable that generates a
 * sequence of boolean valued random variables with a fixed distribution.
 * @see org.bzdev.math.rv.FixedBooleanRV
 */
public class SimFixedBooleanRVRV
    extends SimBooleanRVRV<FixedBooleanRV,FixedBooleanRVRV>
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
    protected SimFixedBooleanRVRV(Simulation sim, String name,
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
    public SimFixedBooleanRVRV(Simulation sim, String name, boolean intern,
				  FixedBooleanRVRV rv)
    {
	super(sim, name, intern);
	super.setRV(rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
