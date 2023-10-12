package org.bzdev.math;
import java.math.BigInteger;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Class for computing binomial coefficients.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * The methods are all static.  One computes a single binomial
 * coefficient and the others use the equation
 * $$ \left( \begin{array}{c}n \\ m\end{array} \right) =
 *    \left( \begin{array}{c}n-1 \\ m-1\end{array} \right) +
 *    \left( \begin{array}{c}n-1 \\ m\end{array} \right) $$
 * <NOSCRIPT><br>
 * <PRE>
 *    / n \       / n-1 \      /  n-1 \
 *    |   |   =  |       |  + |        |
 *    \ m /       \ m-1 /      \   m  /
 *</PRE>
 * <P></NOSCRIPT>
 * to compute multiple values efficiently.
 */
public class Binomial {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private static final int TABLE_SIZE = 67;
    private static final int ETABLE_SIZE = 512;

    // use table look up for small n for efficiency.
    // Note: table is used by the Function class for computing the zeta
    // function.
    static long[][]table = new long[TABLE_SIZE][];
    static double[][] etable = new double[ETABLE_SIZE][];

    static {
	table[0] = new long[1];
	table[0][0] = 1L;
	for (int i = 1; i < TABLE_SIZE; i++) {
	    table[i] = coefficients(table[i-1], null, i);
	}
	BigInteger[] array = exactCoefficients(table[TABLE_SIZE-1],
					      TABLE_SIZE-1);
	for (int i = TABLE_SIZE; i < ETABLE_SIZE; i++) {
	    array = exactCoefficients(array, null, i);
	    int elen = (array.length + 1)/2;
	    double[] entry = new double[elen];
	    for (int j = 0; j < elen; j++) {
		entry[j] = array[j].doubleValue();
	    }
	    etable[i] = entry;
	}
    }

    /**
     * Compute a binomial coefficient C(n,m).
     * The normal notation is
     * $$\left(\begin{array}{c}n \\ m \end{array}\right) = \frac{n!}{m!(n-m)!}$$
     * <NOSCRIPT><blockquote><pre>
     *    / n \          n!
     *    |   |   =   --------
     *    \ m /       m!(n-m)!
     * </pre></blockquote></NOSCRIPT>
     * The value is computed using the relation
     * <blockquote> <pre>
     *     C(n,k) = (n/k)C(n-1,k-1)
     * </pre> </blockquote>
     * and
     * <blockquote><pre>
     *     C(n,k) = C(n,n-k)
     * </pre></blockquote>
     * in order that integer multiplication does not overflow at
     * intermediate steps.  The maximum value of n is 64 due to
     * limits on the size of numbers that can be represented as
     * signed 64-bit integers.
     * @param n a non-negative number
     * @param m a non-negative number less than or equal to n
     * @return the binomial coefficient C(n,m)
     * @exception IllegalArgumentException an argument is out of range
     */
    public static long C(int n, int m) throws IllegalArgumentException {
	/*
	if (m > n/2) {
	    if (n < 0 || m < 0 || m > n)
		throw new IllegalArgumentException("arguments out of range ("
						   + n + "," + m + ")");
	    return C(n, n-m);
	}
	*/
	if (n < 0 || m < 0 || m > n)
	    throw new IllegalArgumentException
		(errorMsg("argsOutOfRange2", n, m));
	if (n < TABLE_SIZE) {
	    return table[n][m];
	} else {
	    throw new IllegalArgumentException
		(errorMsg("firstArgTooLarge", n));
	}
	/*
	int nn = n - m;
	int k = 0;
	long result = 1;
	while (k < m) {
	    nn++; k++;
	    result *= nn;
	    // at this point, 'result' is always divisible by k
	    // because C(n-1,k-1)*n = C(n,k)*k
	    result /= k;
	}
	return result;
	*/
    }

    private static final long MAXROUND = Long.MAX_VALUE << 9;

    /**
     * Compute a binomial coefficient C(n,m) as a double-precision number.
     * The normal notation is
     * $$\left(\begin{array}{c}n \\ m \end{array}\right) = \frac{n!}{m!(n-m)!}$$
     * <NOSCRIPT><blockquote><pre>
     *    / n \          n!
     *    |   |   =   --------
     *    \ m /       m!(n-m)!
     * </pre></blockquote></NOSCRIPT>
     * The for n &lt; 65, the value is computed using the relation
     * <blockquote><pre>
     *     C(n,k) = (n/k)C(n-1,k-1)
     * </pre></blockquote>
     * and
     * <blockquote><pre>
     *     C(n,k) = C(n,n-k)
     * </pre></blockquote>
     * in order that integer multiplication does not overflow at
     * intermediate steps.  For larger values of n, the coefficients cannot
     * be expressed as long integers (signed with 64 bits). For this case,
     * the method uses the {@link Functions#logFactorial} method to compute
     * exp(log n! - log m! - log (n-m)!).
     * <P>
     * The time complexity of the algorithm is O(1) due to table-look-up
     * being used for small values of n and asymptotic forms for large n
     * @param n a non-negative number
     * @param m a non-negative number less than or equal to n
     * @return the binomial coefficient C(n,m)
     */
    public static double coefficient(int n, int m) {
	if (n < TABLE_SIZE) {
	    return (double)C(n,m);
	}
	if (m > n/2) {
	    if (n < 0 || m < 0 || m > n)
		throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, m));
	    return coefficient(n, n-m);
	}
	if (n < 0 || m < 0 || m > n)
	    throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, m));
	if (n < ETABLE_SIZE) {
	    return etable[n][m];
	}
	if (m < 20) {
	    // use C(n,k) = ((n+1-k)/k)C(n,k-1)
	    // a few multiplications are faster than computing
	    // an exponential.
	    int k = 0;
	    int np1 = n+1;
	    long prod = 1;
	    long limit = Long.MAX_VALUE/(n+1);
	    while (prod < limit && k < m) {
		k++;
		prod *= np1-k;
		prod /= k;
	    }
	    double dprod = prod;
	    while (k < m) {
		k++;
		dprod *= (np1-k);
		dprod /= k;
		if (dprod < MAXROUND) {
		    dprod = Math.rint(dprod);
		}
	    }
	    return dprod;
	}
	double result = Math.exp(Functions.logFactorial(n)
				 - Functions.logFactorial(m)
				 - Functions.logFactorial(n-m));
	if (result < MAXROUND) {
	    return Math.rint(result);
	} else {
	    return result;
	}
    }

    /**
     * Compute the natural logarithm of a binomial coefficient
     * @param n a non-negative number
     * @param m a non-negative number less than or equal to n
     * @return the logarithm of the binomial coefficient C(n,m)
     */
    public static double logC(int n, int m) {
	if (n < 0 || m < 0 || m > n)
	    throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, m));
	return Functions.logFactorial(n)
				 - Functions.logFactorial(m)
	    - Functions.logFactorial(n-m);
    }

    /**
     * Compute binomial coefficients C(n,m) for all allowed values of m
     * given the values for C(n-1,k).
     * @param prev an array whose i<sup>th</sup> index is C(n-1,i)
     * @param result the array of length at least n+1 to hold the results
     * @param n first argument for C(n,i) with n &gt; 0
     * @return an array whose i<sup>th</sup> index is C(n,i)
     */
    static long[] coefficients(long[] prev, long[] result,
				      int n)
    {
	if (result == null) result = new long[n+1];
	result[0] = 1;
	result[n] = 1;
	for (int i = 1; i <= n; i++) {
	    result[i] = prev[i-1] + ((i == n)? 0: prev[i]);
	}
	return result;
    }

    static BigInteger[] exactCoefficients(long[] base, int n) {
	BigInteger[] results = new BigInteger[n+1];
	for (int i = 0; i <= n; i++) {
	    results[i] = new BigInteger(Long.toString(base[i]));
	}
	return results;
    }

    static BigInteger[] exactCoefficients(BigInteger[] prev,
					  BigInteger[]result,
					  int n)
    {
	if (result == null) result = new BigInteger[n+1];
	result[0] = BigInteger.ONE;
	result[n] = BigInteger.ONE;
	for (int i = 1; i < n; i++) {
	    result[i] = prev[i-1].add(prev[i]);
	}
	return result;
    }

    /**
     * Compute binomial coefficients exactly.
     * This method computes binomial coefficients using infinite-precision
     * arithmetic.
     * @param n the first argument to C(n,m)
     * @return an array of length n+1 containing the binomial
     *         coefficient C(n,m) at index m
     */
    static public BigInteger[] exactC(int n) {
	if (n < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative", n));
	}
	if (n < TABLE_SIZE) {
	    return exactCoefficients(table[n], n);
	} else {
	    int n0 = TABLE_SIZE-1;
	    BigInteger[] array1 = new BigInteger[n+1];
	    BigInteger[] array2 = new BigInteger[n+1];
	    for (int m = 0; n <= m; n++) {
		array1[m] = new BigInteger(Long.toString(C(n,m)));
	    };
	    while (n0 < n) {
		exactCoefficients(array1, array2, ++n0);
		BigInteger[] tmp = array2;
		array2 = array1;
		array1 = tmp;
	    }
	    return array1;
	}
    }

    /**
     * Compute binomial coefficients exactly given an array of coefficients.
     * This method computes binomial coefficients using infinite-precision
     * arithmetic. It is intended for cases where one needs binomial
     * coefficients C(n,m) for consecutive values of n.
     * @param prev an array whose m<sup>th</sup> index is C(n-1,m)
     * @param n the first argument to C(n,m)
     * @return an array of length n+1 containing the binomial
     *         coefficient C(n,m) at index m
     */
    static public BigInteger[] exactC(BigInteger[] prev, int n) {
	return exactCoefficients(prev, null, n);
    }
    
    /*
     * Compute binomial coefficients C(n,m) for all allowed values of m.
     * @param n the number of elements
     * @return an array whose i<sup>th</sup> index is C(n,i)
    static long[] coefficients(int n) {
	if (n == 0) {
	    long[] array = new long[1];
	    array[0] = 1;
	    return array;
	} else if (n == 1) {
	    long[] array = new long[2];
	    array[0] = 1; array[1] = 1;
	    return array;
	} else {
	    long[] results = new long[n+1];
	    coefficients(results, n);
	    return results;
	}
    }
     */

    /**
     * Compute binomial coefficients C(n,m) for all allowed values of m.
     * @param n the number of elements
     * @return an array whose i<sup>th</sup> index is C(n,i)
     */
    public static long[] coefficients(int n) {
	if (n < 0)
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative", n));
	if (n < table.length) {
	    return (long[]) table[n].clone();
	} else {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative", n));

	}
    }

    /*
     * Compute binomial coefficients C(n,m) for all allowed values of m given
     * an array in which to store the results.
     * @param array an array of length at least n+1 whose i<sup>th</sup>
     *        index will be set to C(n,i) for i in the range [0, n]
     * @param n the number of elements
    static long[] coefficients(long[] array, int n) {
	if (n == 0) {
	    array[0] = 1L;
	    return array;
	}
	long[] prev = null;
	long[] result = null;
	long[] tmp = null;
	if (n % 2 == 1) {
	    prev = array;
	    result = new long[n];
	} else {
	    result = array;
	    prev = new long[n];

	}
	result[0] = 1;
	prev[0] = 1; prev[1] = 1;
	for (int i = 2; i <= n; i++) {
	    result[i] = 1;
	    for (int j = 1; j < i; j++) {
		result[j] = prev[j-1] + prev[j];
	    }
	    tmp = prev;
	    prev = result;
	    result = tmp;
	}
	return array;
    }
     */
}

//  LocalWords:  exbundle PRE pre IllegalArgumentException nn lt prev
//  LocalWords:  argsOutOfRange firstArgTooLarge logFactorial th tmp
//  LocalWords:  argNonNegative
