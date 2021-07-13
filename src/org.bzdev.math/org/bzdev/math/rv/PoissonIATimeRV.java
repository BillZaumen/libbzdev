package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class for exponentially distributed interarrival times suitable for
 * events with a Poisson distribution.
 */
public class PoissonIATimeRV extends InterarrivalTimeRV {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double mean;
    /**
     * Get the mean value of the distribution.
     * @return the mean value of the distribution
     */
    public double getMean() {
	return mean;
    }

    /**
     * Constructor.
     * @param mean the mean value for the random variable
     */
    public PoissonIATimeRV(double mean) throws IllegalArgumentException {
	super();
	if (mean < 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("meanNotNegative", mean));
	}
	this.mean = mean;
	super.setMinimum(0L, true);
    }

    public Long next() {
	long result;
	do {
	    result = StaticRandom.nextPoissonIATime(mean);
	} while (rangeTestFailed(result));
	return result;
    }

    /**
     * Get a random number that represents the sum of n interarrival times
     * for a Poisson process.
     * The implementation is more efficient than adding the results of
     * calling next() n times.  The range test (if any) tests the returned
     * value divided by n.
     * @param n the number of interarrival times to sum
     * @return the sum of n values of this random variable
     */
    public Long next(int n) {
	long result;
	do {
	    result = StaticRandom.nextPoissonIATime(mean, n);
	} while (rangeTestFailed(Math.round(((double)result)/((double)n))));
	return result;
    }
}

//  LocalWords:  exbundle interarrival meanNotNegative
