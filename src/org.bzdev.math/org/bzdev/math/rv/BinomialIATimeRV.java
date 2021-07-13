package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.Binomial;
import java.util.Arrays;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Long random variable that generates a binomial distribution for n
 * tries, treated as an interarrival time.  Each number in the
 * sequence represents the number of successful tries out of n tries.
 * <P>
 * An interarrival time is defined to be a non-negative long integer
 * (as required by the package {@link org.bzdev.devqsim} and packages
 * based on that package). The class {@link InterarrivalTimeRV} enforces
 * the restriction that the value generated is not negative. When that
 * class is used, the Java type system prevents setting a variable of that
 * type to an instance of {@link BinomialLongRV}, even though that class
 * would produce the desired values.
 */
public class BinomialIATimeRV extends InterarrivalTimeRV {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double prob;
    boolean atLimit = false;

    double[] array;
    long n;
    double sdev;
    double mean;
    boolean usePoisson = false;

    /**
     * Constructor.
     * @param prob the probability that a try succeeds.
     * @param n the number of tries (must be positive)
     */
    public BinomialIATimeRV(double prob, long n)
	throws IllegalArgumentException
    {
	if (prob < 0.0 || prob > 1.0) 
	    throw new IllegalArgumentException(errorMsg("outOfRange", prob));
	if (n < 1)
	    throw new IllegalArgumentException(errorMsg("intOutOfRange", n));
	this.prob = prob;
	this.n = n;

	if (n < 61) {
	    int nn = (int)n;
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
	    if ((prob <= 0.05 && n >= 20) || (n >= 100 && n*prob <= 10)) {
		usePoisson = true;
	    }
	}
    }

    /**
     * Get the probability that a try succeeds.
     * @return the probability that a try succeeds.
     */
    public double getProb() {
	return prob;
    }


    /**
     * Get the number of tries for this random variable.
     * @return the number of tries
     */
    public long getN() {
	return n;
    }


    @Override
    public Long next() {
	if (n < 61) {
	    double test = StaticRandom.nextDouble();
	    int index = Arrays.binarySearch(array, test);
	    if (index < 0) {
		index = -index - 1;
	    }
	    return (long)index;
	} else if (usePoisson) {
	    return StaticRandom.poissonLong(mean);
	} else {
	    long result;
	    do {
		result = Math.round(mean + sdev * StaticRandom.nextGaussian());
	    } while (result < 0 || result > n);
	    return result;
	}
    }
}

//  LocalWords:  exbundle interarrival InterarrivalTimeRV outOfRange
//  LocalWords:  BinomialLongRV intOutOfRange
