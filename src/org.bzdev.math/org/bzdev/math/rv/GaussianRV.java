package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

/**
 * Random variable with a Gaussian distribution.
 */
public class GaussianRV extends DoubleRandomVariable {
    private double mean;
    private double sdev;

    /**
     * Get the mean value for the random-variable.
     * @return the mean value
     */
    public double getMean() {
	return mean;
    }

    /**
     * Get the standard deviation for the random variable.
     * @return the standard deviation
     */
    public double getSDev() {
	return sdev;
    }

    /**
     * Constructor.
     * @param mean the mean value of this random
     *        variable
     * @param sdev the standard deviation of this random variable
     */
    public GaussianRV(double mean, double sdev) {
	super();
	this.mean = mean;
	this.sdev = sdev;
    }

    public Double next() {
	double result;
	do {
	    result =  mean + sdev * StaticRandom.nextGaussian();
	} while (rangeTestFailed(result));
	return result;
    }

    /**
     * Get the sum of multiple values with the number of values an int.
     * The range check, if any, is applied to the result, not the individual
     * random values that were summed.  The implementation is more efficient
     * than one that calls next() n times.
     * @param n the number of values to use
     * @return the sum of n values of this random variable
     */
    public double next(int n) {
	return next((long) n);
    }

    /**
     * Get the sum of multiple values with the number of values a long.
     * The range check, if any, is applied to the result, not the individual
     * random values that were summed. The implementation is more efficient
     * than one that calls next() n times.
     * @param n the number of values to use
     * @return the sum of n values of this random variable
     */
    public Double next(long n) {
	double result;
	do {
	    result = n * mean + Math.sqrt((double)n) * sdev * StaticRandom.nextGaussian();
	} while (rangeTestFailed(result));
	return result;
    }


    public String toString() {
	return "GaussianRV(" + mean + "," + sdev + ")";
    }
}

//  LocalWords:  sdev GaussianRV
