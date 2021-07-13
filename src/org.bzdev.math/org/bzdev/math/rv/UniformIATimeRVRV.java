package org.bzdev.math.rv;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.StaticRandom;
/**
 * Random variable that generates an interarrival time with uniform probability
 * over a range of values.
 */
public class UniformIATimeRVRV extends InterarrivalTimeRVRV<UniformIATimeRV> {

    LongRandomVariable lowerLimit = new FixedLongRV(0);
    LongRandomVariable upperLimit = new FixedLongRV(1);

    boolean lowerClosed = true;
    boolean upperClosed = false;

    public UniformIATimeRV doNext() {
	return new UniformIATimeRV(lowerLimit.next(), lowerClosed,
				   upperLimit.next(), upperClosed);
    }

    /**
     * Constructor.
     * The values produced are in the range [lowerLimit, upperLimit).
     * @param lowerLimit the lower limit of the values produced
     * @param upperLimit the upper limit of the values produced
     */
    public UniformIATimeRVRV(LongRandomVariable lowerLimit,
			     LongRandomVariable upperLimit)
    {
	this(lowerLimit, true, upperLimit, false);
    }

    /**
     * Constructor specifying range type (open, closed, etc.).
     * @param lowerLimit the lower limit of the values produced
     * @param lowerClosed true if the lower limit is included in the
     *        range of values produced; false otherwise
     * @param upperLimit the upper limit of the values produced
     * @param upperClosed true if the upper limit is included in the
     *        range of values produced; false otherwise
     */
    public UniformIATimeRVRV(LongRandomVariable lowerLimit,
			     boolean lowerClosed, 
			     LongRandomVariable upperLimit,
			     boolean upperClosed)
    {
	try {
	    this.lowerLimit = (LongRandomVariable)(lowerLimit.clone());
	    this.lowerClosed = lowerClosed;
	    this.upperLimit = (LongRandomVariable)(upperLimit.clone());
	    this.upperClosed = upperClosed;
	    determineIfOrdered(lowerLimit, upperLimit);
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }
}

//  LocalWords:  interarrival lowerLimit upperLimit lowerClosed
//  LocalWords:  upperClosed
