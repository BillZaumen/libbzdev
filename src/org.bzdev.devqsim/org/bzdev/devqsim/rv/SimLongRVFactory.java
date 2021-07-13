package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;

/**
 * Base class for factories that create named random variables
 * that produce a sequence of values of type double.
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type for the random variable
 *   <LI> NRV - the type of the corresponding named random variable
 * </UL>
 */
public abstract class SimLongRVFactory
    <RV extends LongRandomVariable, NRV extends SimLongRV<RV>>
    extends SimRVFactory<Long,RV,NRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimLongRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  NRV
