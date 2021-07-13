package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class for exponentially distributed interarrival times suitable for
 * events with a Poisson distribution.
 */
public class ExpDistrRV extends DoubleRandomVariable {

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
    public ExpDistrRV(double mean) throws IllegalArgumentException {
	super();
	if (mean < 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("meanNotNegative", mean));
	}
	this.mean = mean;
	super.setRequiredMinimum(0.0, true);
    }

    public Double next() {
	Double result;
	do {
	    result = StaticRandom.nextDoubleExpDistr(mean);
	} while (rangeTestFailed(result));
	return result;
    }

    /**
     * Get a random number that represents the sum of n interarrival times
     * for an exponentially distributed random variable, where each value is
     * independent of another.
     * The implementation is more efficient than adding the results of
     * calling next() n times.  The range test (if any) tests the returned
     * value divided by n.
     * @param n the number of interarrival times to sum
     * @return the sum of n values of this random variable
     */
    public Double next(int n) {
	double result;
	do {
	    result = StaticRandom.nextDoubleExpDistr(mean, n);
	} while (rangeTestFailed(Math.round(((double)result)/((double)n))));
	return result;
    }
}

//  LocalWords:  exbundle interarrival meanNotNegative
