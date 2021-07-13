package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.LongRandomVariableRV;

/**
 * Base class for named objects representing random variables that
 * generate boolean random variables.
 * The type parameters are:
 * <UL>
 *  <LI> RV. This is the type of the random variable that instances of
 *       this class will generate
 *  <LI> RVRV. This is the type of the random variable that this class
 *       will name
 * </UL>
 */
public class SimLongRVRV
    <RV extends LongRandomVariable,
		RVRV extends LongRandomVariableRV<RV>>
    extends SimRandomVariableRV<Long,RV,RVRV>
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
    protected SimLongRVRV(Simulation sim, String name, boolean intern) {
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
     protected SimLongRVRV(Simulation sim, String name, boolean intern,
				RVRV rvrv)
    {
	super(sim, name, intern, rvrv);
    }

}

//  LocalWords:  IllegalArgumentException getObject rv
