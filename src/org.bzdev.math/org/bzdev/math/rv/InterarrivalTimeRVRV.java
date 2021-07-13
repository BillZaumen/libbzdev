package org.bzdev.math.rv;

/**
 * Generate random variables that generate random interarrival times
 * Interarrival times are instances of Long because a long integer
 * is the unit used to represent simulation time (as "ticks"). The
 * value must not be negative.  The constructor for this class
 * arranges that. Subclasses must not override this behavior.
 * <P>
 * Note: this class was added to simply the construction of named
 * random variables, and their factories, in the [@link org.bzdev.devqsim.rv}
 * package.
*/

public abstract class InterarrivalTimeRVRV<RV extends InterarrivalTimeRV>
    extends LongRandomVariableRV<RV>
{
    /**
     * Constructor.
     */
    protected InterarrivalTimeRVRV() {
	super();
    }
}

//  LocalWords:  interarrival Subclasses
