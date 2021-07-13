package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.LongRandomVariableRV;

/**
 * Base class for named random variables that produce a sequence
 * of values of type {@link org.bzdev.math.rv.LongRandomVariable}.
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type of the random variables generated
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables
 *   <LI> NRVRV - the type of the named random variable corresponding
 *        to RVRV.
 * </UL>
 */
public abstract class SimLongRVRVFactory
    <RV extends LongRandomVariable,
		RVRV extends LongRandomVariableRV<RV>,
			     NRVRV extends SimLongRVRV<RV,RVRV>>
    extends SimRVRVFactory<Long, RV,RVRV,NRVRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimLongRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
