package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BooleanRandomVariable;
import org.bzdev.math.rv.BooleanRandomVariableRV;

/**
 * Base class for named random variables that produce a sequence
 * of values of type {@link org.bzdev.math.rv.BooleanRandomVariable}..
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type for the random variables generate.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
*   <LI> NRVRV - the type of the named random variable corresponding
*        to RVRV.
 * </UL>
 */
public abstract class SimBooleanRVRVFactory
    <RV extends BooleanRandomVariable,
		RVRV extends BooleanRandomVariableRV<RV>,
			     NRVRV extends SimBooleanRVRV<RV,RVRV>>
    extends SimRVRVFactory<Boolean, RV,RVRV,NRVRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimBooleanRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
