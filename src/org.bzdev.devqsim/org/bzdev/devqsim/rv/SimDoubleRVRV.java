package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.DoubleRandomVariableRV;

/**
 * Named random variable using a random variable that generates a
 * sequence of double-precision numbers.
 * The type parameters are:
 *   <LI> RV - the type for the random variables generate.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
 */
public abstract class SimDoubleRVRV
    <RV extends DoubleRandomVariable,
		RVRV extends DoubleRandomVariableRV<RV>>
    extends SimRandomVariableRVN<Double,RV,RVRV>
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
    protected SimDoubleRVRV(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Constructor given a random variable.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * @param rvrv the random variable itself
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimDoubleRVRV(Simulation sim, String name, boolean intern,
			 RVRV rvrv) {
	super(sim, name, intern, rvrv);
    }
}

//  LocalWords:  RVRV IllegalArgumentException getObject rvrv
