package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.InterarrivalTimeRVRV;

/**
 * Base class for named random variables that produce a sequence
 * of values of type {@link org.bzdev.math.rv.InterarrivalTimeRV}.
 * The type parameters are:
 * <UL>
 *   <LI> RV - the type of the random variables generated
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables
 *   <LI> NRVRV - the type of the named random variable corresponding
 *        to RVRV.
 * </UL>
 */
public abstract class SimIATimeRVRVFactory
    <RV extends InterarrivalTimeRV,
		RVRV extends InterarrivalTimeRVRV<RV>,
			     NRVRV extends SimInterarrivalTimeRVRV<RV, RVRV>>
    extends SimLongRVRVFactory<RV, RVRV, NRVRV>
{

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimIATimeRVRVFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  RVRV NRVRV
