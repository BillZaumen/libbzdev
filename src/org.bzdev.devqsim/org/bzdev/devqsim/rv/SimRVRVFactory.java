package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.RandomVariable;
import org.bzdev.math.rv.RandomVariableRV;

/**
 * Base class for factories that produce named random variables that
 * in turn produce a sequence of values of type
 * {@link org.bzdev.math.rv.RandomVariable}.  The type parameters are:
 * <UL>
 *   <LI> T - the type of the values that a generated random variable
 *        generate.
 *   <LI> RV - the type for the random variables generated.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
*   <LI> NRVRV - the type of the named random variable corresponding
*        to RVRV.
 * </UL>
 */
public abstract class SimRVRVFactory
    <T,RV extends RandomVariable<T>,
		  RVRV extends RandomVariableRV<T,RV>,
			       NRVRV extends SimRandomVariableRV<T,RV,RVRV>>
    extends SimRVFactory<RV,RVRV,NRVRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
