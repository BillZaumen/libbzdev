package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.Binomial;
import java.util.Arrays;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Double-valued random variable that generates a binomial distribution for
 * n tries.  Each number in the sequence represents the number of
 * tries that succeeded out of n tries. The random numbers that are
 * returned will be rounded to the nearest integral values as long as
 * they are small enough so that StrictMath.rint((double)n) = n where
 * n is a long integer.
 */
public class BinomialDoubleRV extends DoubleRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double prob;
    boolean atLimit = false;

    double[] array;
    double n;
    double sdev;
    double mean;
    boolean usePoisson = false;

    static private long MAX_FOR_RINT = (1L << 53);

    /**
     * Constructor.
     * The argument <code>n</code> will be rounded to the nearest
     * integral value.
     * @param prob the probability that a try succeeds.
     * @param n the number of tries (must be positive)
     */
    public BinomialDoubleRV(double prob, double n)
	throws IllegalArgumentException
    {
	if (prob < 0.0 || prob > 1.0) 
	    throw new IllegalArgumentException(errorMsg("outOfRange", prob));
	if (Math.floor(n) < 1)
	    throw new IllegalArgumentException(errorMsg("intOutOfRange", n));
	this.prob = prob;
	if (n < (double)MAX_FOR_RINT) {
	   n = StrictMath.rint(n);
	}
	this.n = n;
	if (n < 61) {
	    int nn = (int) StrictMath.round(n);
	    array = new double[nn+2];
	    double sum = 0.0;
	    double p = 1.0;
	    double q = 1.0;
	    double qf = 1.0 - prob;
	    for (int i = 0; i <= nn; i++) {
		int k = nn-i;
		array[i] = Binomial.C(nn,i) * p;
		p *= prob;
	    }
	    for (int i = nn; i >= 0; i--) {
		array[i] *= q;
		q *= qf;
		sum += array[i];
	    }
	    double tot = 0.0;
	    for (int i = 0; i <= nn; i++) {
		array[i] /= sum;
		array[i] += tot;
		tot = array[i];
	    }
	} else {
	    mean = n * prob;
	    sdev = StrictMath.sqrt(n*prob*(1.0-prob));
	    if ((prob <= 0.05 && n >= 20.0) || (n >= 100.0 && n*prob <= 10.0)) {
		usePoisson = true;
	    }
	}
    }

    /**
     * Get the probability that a try succeeds
     * @return the probability that a a try succeeds
     */
    public double getProb() {
	return prob;
    }

    /**
     * Get the number of tries for this random variable.
     * @return the number of tries
     */
    public double getN() {
	return n;
    }

    @Override
    public Double next() {
	if (StrictMath.round(n) < 61) {
	    double test = StaticRandom.nextDouble();
	    int index = Arrays.binarySearch(array, test);
	    if (index < 0) {
		index = -index - 1;
	    }
	    return (double)index;
	} else if (usePoisson) {
	    return StaticRandom.poissonDouble(mean);
	} else {
	    double result;
	    do {
		result = mean + sdev * StaticRandom.nextGaussian();
		if (result < (double)(MAX_FOR_RINT)) {
		    result = StrictMath.rint(result);
		}
	    } while (result < 0 || result > n);
	    return result;
	}
    }
}

//  LocalWords:  exbundle StrictMath rint outOfRange intOutOfRange
