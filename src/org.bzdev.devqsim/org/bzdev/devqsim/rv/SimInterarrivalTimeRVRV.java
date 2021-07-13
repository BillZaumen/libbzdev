package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.InterarrivalTimeRVRV;

/**
 * Base class for named objects representing random variables that
 * generate random variables that generate interarrival times.
 * These random numbers produce non-negative long-integer values.
 * The type parameter RV is the type of the random variable being named.
 */
public abstract class SimInterarrivalTimeRVRV
    <RV extends InterarrivalTimeRV, RVRV extends InterarrivalTimeRVRV<RV>>
	 extends SimLongRVRV<RV, RVRV>
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
    protected SimInterarrivalTimeRVRV(Simulation sim, String name,
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
    protected SimInterarrivalTimeRVRV(Simulation sim, String name,
				   boolean intern, RVRV rv)
    {
	super(sim, name, intern, rv);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
