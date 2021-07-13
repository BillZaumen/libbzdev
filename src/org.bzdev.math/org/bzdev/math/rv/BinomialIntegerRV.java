package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.Binomial;
import java.util.Arrays;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Integer-valued random variable that generates a binomial distribution for
 * n tries.  Each number in the sequence represents the number of
 * successful tries out of n tries.
 */
public class BinomialIntegerRV extends IntegerRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double prob;
    boolean atLimit = false;

    double[] array;
    int n;
    double sdev;
    double mean;
    boolean usePoisson = false;

    /**
     * Constructor.
     * @param prob the probability that a try is successful.
     * @param n the number of tries (must be positive)
     */
    public BinomialIntegerRV(double prob, int n)
	throws IllegalArgumentException
    {
	if (prob < 0.0 || prob > 1.0) 
	    throw new IllegalArgumentException(errorMsg("outOfRange", prob));
	if (n < 1)
	    throw new IllegalArgumentException(errorMsg("intOutOfRange", n));
	this.prob = prob;
	this.n = n;

	if (n < 61) {
	    array = new double[n+2];
	    double sum = 0.0;
	    double p = 1.0;
	    double q = 1.0;
	    double qf = 1.0 - prob;
	    for (int i = 0; i <= n; i++) {
		int k = n-i;
		array[i] = Binomial.C(n,i) * p;
		p *= prob;

	    }
	    for (int i = n; i >= 0; i--) {
		array[i] *= q;
		q *= qf;
		sum += array[i];
	    }
	    double tot = 0.0;
	    for (int i = 0; i <= n; i++) {
		array[i] /= sum;
		array[i] += tot;
		tot = array[i];
	    }
	} else {
	    mean = n * prob;
	    sdev = StrictMath.sqrt(n*prob*(1.0-prob));
	    if ((prob <= 0.05 && n >= 20) || (n >= 100 && n*prob <= 10)) {
		usePoisson = true;
	    }
	}
    }

    /**
     * Get the probability that a try succeeds.
     * @return the probability that a try succeeds
     */
    public double getProb() {
	return prob;
    }

    /**
     * Get the number of tries for this random variable.
     * @return the number of tries
     */
    public int getN() {
	return n;
    }

    @Override
    public Integer next() {
	if (n < 61) {
	    double test = StaticRandom.nextDouble();
	    int index = Arrays.binarySearch(array, test);
	    if (index < 0) {
		index = -index - 1;
	    }
	    return index;
	} else if (usePoisson) {
	    return StaticRandom.poissonInt(mean);
	} else {
	    int result;
	    do {
		result = (int)
		    Math.round(mean + sdev * StaticRandom.nextGaussian());
	    } while (result < 0 || result > n);
	    return result;
	}
    }
}

//  LocalWords:  exbundle outOfRange intOutOfRange
