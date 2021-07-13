package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.DoubleRandomVariableRV;

/**
 * Base class for named random variables that produce a sequence
 * of values of type {@link org.bzdev.math.rv.DoubleRandomVariable}..
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type for the random variables generate.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
*   <LI> NRVRV - the type of the named random variable corresponding
*        to RVRV.
 * </UL>
 */
public abstract class SimDoubleRVRVFactory
    <RV extends DoubleRandomVariable,
		RVRV extends DoubleRandomVariableRV<RV>,
			     NRVRV extends SimDoubleRVRV<RV,RVRV>>
    extends SimRVRVFactory<Double, RV,RVRV,NRVRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimDoubleRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
