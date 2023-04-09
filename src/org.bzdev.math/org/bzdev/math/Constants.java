package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Class defining various mathematical constants.
 * This class provides additional constants besides the ones
 * provided by java.lang.Math (E and PI).
 * <P>
 * Some are implemented as static methods (e.g., Bernoulli numbers - there
 * are an infinite number of these, but only a finite subset can be
 * represented as double-precision constants).
 */
public class Constants {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }


    /**
     * Euler's Constant.
     */
    public static final double EULERS_CONSTANT =
	0.57721566490153286060651209008240243104215933593992;

    // must be an odd number
    static final int BernoulliNumberMaxIndex = 225;

    private static double[] BNArray = BernoulliNumbers(BernoulliNumberMaxIndex);

    /**
     * Get the first Bernoulli Number.
     * @param n the index for the number (must be non-negative)
     * @return the first Bernoulli number B<sub>n</sub>; negative or
     *         positive infinity if the number is too large to be
     *         represented as a double-precision number
     * @exception IllegalArgumentException the argument was negative
     */
    public static double BernoulliNumber1(int n) {
	if (n >= BernoulliNumberMaxIndex) {
	    if (n%2 == 1) return 0.0;
	    int m = n/2;
	    if (m%2 == 0) return Double.NEGATIVE_INFINITY;
	    else return Double.POSITIVE_INFINITY;
	}
	if (n < 0) throw new IllegalArgumentException
		(errorMsg("argNonNegative", n));
	return (n==1)? -BNArray[n]: BNArray[n];
    }

    /**
     * Get the second Bernoulli Number.
     * @param n the index for the number (must be non-negative)
     * @return the second Bernoulli number B<sub>n</sub>; negative or
     *         positive infinity if the number is too large to be
     *         represented as a double-precision number
     * @exception IllegalArgumentException the argument was negative
     */
    public static double BernoulliNumber2(int n)
	throws IllegalArgumentException
    {
	if (n > BernoulliNumberMaxIndex) {
	    if (n%2 == 1) return 0.0;
	    int m = n/2;
	    if (m%2 == 0) return Double.NEGATIVE_INFINITY;
	    else return Double.POSITIVE_INFINITY;
	}
	if (n < 0) throw new IllegalArgumentException
		(errorMsg("argNonNegative", n));
	return BNArray[n];
    }

    /**
     * Compute second Bernoulli Numbers for indices in the range
     * [0,n].
     * Note: if n is larger than 255, NaN will be returned for
     * all indices larger than 255.
     * @param n the maximum index;
     * @return an array of size (n+1) containing Bernoulli numbers
     */
    private static double[] BernoulliNumbers(int n) {

	double results[] = new double[n+1];
	if (n == 0) {
	    results[0] = 1.0;
	    return results;
	}
	results[0] = 1.0;
	results[1] = 0.5;
	if (n < 2) return results;
	/*
	long[] binomials1 = new long[n+1];
	long[] binomials2 = new long[n+1];
	binomials2[0] = 1;
	binomials2[1] = 2;
	binomials2[2] = 1;
	int bm = 2;
	*/
	for (int m = 2; m <= n; m += 2) {
	    double sum = 1.0;
	    sum -= (results[0] * Binomial.coefficient(m, 0)) / (m+1);
	    sum -= (results[1] * Binomial.coefficient(m, 1)) / m;
	    for (int k = 2; k < m; k += 2) {
		sum -= (results[k] * Binomial.coefficient(m, k)) / (m-k+1);
	    }
	    /*
	    sum -= results[0]*binomials2[0]/(m+1);
	    sum -= results[1]*binomials2[1]/m;
	    for (int k = 2; k < m; k += 2) {
		sum -= (results[k]*binomials2[k])/(m-k+1);
	    }
	    */
	    results[m] = sum;
	    /*
	    if (m+2 <= n) {
		Binomial.coefficients(binomials2, binomials1, m+1);
		Binomial.coefficients(binomials1, binomials2, m+2);
	    }
	    */
	}
	return results;
    }
}
//  LocalWords:  exbundle IllegalArgumentException argNonNegative NaN
//  LocalWords:  bm
