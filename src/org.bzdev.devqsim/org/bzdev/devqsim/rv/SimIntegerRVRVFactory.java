package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.IntegerRandomVariable;
import org.bzdev.math.rv.IntegerRandomVariableRV;

/**
 * Base class for named random variables that produce a sequence
 * of values of type {@link org.bzdev.math.rv.IntegerRandomVariable}..
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type for the random variables generate.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
*   <LI> NRVRV - the type of the named random variable corresponding
*        to RVRV.
 * </UL>
 */
public abstract class SimIntegerRVRVFactory
    <RV extends IntegerRandomVariable,
		RVRV extends IntegerRandomVariableRV<RV>,
			     NRVRV extends SimIntegerRVRV<RV,RVRV>>
    extends SimRVRVFactory<Integer, RV,RVRV,NRVRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimIntegerRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
