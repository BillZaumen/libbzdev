package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable with a log normal distribution.
 * a log normal random variable is one whose logarithm is a 
 * Gaussian random variable. The mean and standard deviations
 * passed to the constructor refer by convention to the mean
 * and standard deviation of this Gaussian random variable.
 */
public class LogNormalRV extends DoubleRandomVariable {
    private double mu;
    private double sigma;

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    /**
     * Get the mean value for the logarithm of this random-variable.
     * This is equal to the first argument of the constructor.
     * @return the mean value
     */
    public double getMu() {
	return mu;
    }

    /**
     * Get the value of mu for a specified mean and standard deviation.
     * This is intended for cases where you know the mean and standard
     * deviation, but not the mu-sigma parameters used by the constructor.
     * @param mean the mean value
     * @param sdev the standard deviation
     * @return the corresponding value of mu
     * @see #LogNormalRV(double,double)
     */
    public static double getMu(double mean, double sdev) {
	if (sdev < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("sdevNotNegative", sdev));
	if (mean < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("meanNotNegative", mean));
	double variance = sdev*sdev;
	double meansq = mean*mean;
	return Math.log(meansq/Math.sqrt(variance + meansq));
    }

    /**
     * Get the value of sigma for a specified mean and standard deviation.
     * This is intended for cases where you know the mean and standard
     * deviation, but not the mu-sigma parameters used by the constructor.
     * @param mean the mean value
     * @param sdev the standard deviation
     * @return the corresponding value of sigma
     * @see #LogNormalRV(double,double)
     */
    public static double getSigma(double mean, double sdev) {
	if (sdev < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("sdevNotNegative", sdev));
	if (mean < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("meanNotNegative", mean));
	double variance = sdev*sdev;
	double meansq = mean*mean;
	return Math.sqrt(Math.log(1.0 + variance/meansq));
    }


    /**
     * Get the standard deviation for the logarithm of this random variable.
     * This is equal to the second argument of the constructor.
     * @return the standard deviation
     */
    public double getSigma() {
	return sigma;
    }

    /**
     * Get the mean value for the random-variable.
     * @return the mean value
     */
    public double getMean() {
	return Math.exp(mu + sigma*sigma/2);
    }

    /**
     * Get the standard deviation for the random variable.
     * @return the standard deviation
     */
    public double getSDev() {
	double sigmasq = sigma*sigma;
	return Math.sqrt((Math.exp(sigmasq) - 1.0)
			 * Math.exp(2.0*mu + sigmasq));
    }

    /**
     * Constructor.
     * @param mu the mean value of the logarithm of this random
     *        variable
     * @param sigma the standard deviation of the logarithm of this
     *        random variable
     */
    public LogNormalRV(double mu, double sigma) {
	super();
	if (sigma < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("sigmaNotNegative", sigma));
	this.mu = mu;
	this.sigma = sigma;
    }

    public Double next() {
	double result;
	do {
	    result = Math.exp(mu + sigma * StaticRandom.nextGaussian());
	} while (rangeTestFailed(result));
	return result;
    }

    /**
     * Get the logarithm of the next value.
     * This method is more efficient that using 
     * <code>Math.log(rv.next())</code> where <code>rv</code> is the
     * random variable.
     *  @return the logarithm of the value that would be returned
     *          by calling next
     */
    public Double logNext() {
	double result;
	do {
	    result = (mu + sigma * StaticRandom.nextGaussian());
	} while (rangeTestNeeded() && rangeTestFailed(Math.exp(result)));
	return result;
    }


    /**
     * Get the product of multiple values with the number of values an int.
     * The range check, if any, is applied to the result, not the individual
     * random values that were multiplied. The implementation is more efficient
     * than one that calls next() n times.
     * @param n the number of values to use
     * @return the product of n values of this random variable
     */
    public double next(int n) {
	return next((long) n);
    }

    /**
     * Get the product of multiple values with the number of values a long.
     * The range check, if any, is applied to the result, not the individual
     * random values that were multiplied. The implementation is more efficient
     * than one that calls next() n times.
     * @param n the number of values to use
     * @return the product of n values of this random variable
     */
    public Double next(long n) {
	double result;
	do {
	    result =
		Math.exp(n * mu + Math.sqrt((double)n) * sigma * StaticRandom.nextGaussian());
	} while (rangeTestFailed(result));
	return result;
    }

    public String toString() {
	return "LogNormalRV(" + mu + "," + sigma + ")";
    }
}

//  LocalWords:  exbundle sdev LogNormalRV sdevNotNegative rv
//  LocalWords:  meanNotNegative sigmaNotNegative
