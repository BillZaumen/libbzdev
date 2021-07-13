package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.InterarrivalTimeRV;

/**
 * Base class for factories that create named interarrival-time random
 * variables.
 * <P>
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type for the random variable
 *   <LI> NRV - the type of the corresponding named random variable
 * </UL>
 */
public abstract class SimIATimeRVFactory
    <RV extends InterarrivalTimeRV, NRV extends SimInterarrivalTimeRV<RV>>
    extends SimLongRVFactory<RV,NRV>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimIATimeRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  NRV
