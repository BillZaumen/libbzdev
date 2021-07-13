package org.bzdev.math.rv;

/**
 * Generate random interarrival times.
 * Interarrival times are instances of Long because a long integer
 * is the unit used to represent simulation time (as "ticks"). The
 * value must not be negative.  The constructor for this class
 * arranges that. Subclasses must not override this behavior.
 */

public abstract class InterarrivalTimeRV extends LongRandomVariable
{
    /**
     * Constructor.
     * The constructor uses the protected method setRequiredMinimum
     * so that the values generated will not be negative. Subclasses
     * must not override this behavior.
     */
    protected InterarrivalTimeRV() {
	super();
	super.setRequiredMinimum(0L, true);
    }
}

//  LocalWords:  interarrival Subclasses setRequiredMinimum
