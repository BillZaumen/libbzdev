package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.IntegerRandomVariable;
import org.bzdev.math.rv.IntegerRandomVariableRV;

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
public class SimIntegerRVRV
    <RV extends IntegerRandomVariable,
		RVRV extends IntegerRandomVariableRV<RV>>
    extends SimRandomVariableRV<Integer,RV,RVRV>
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
    protected SimIntegerRVRV(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
