package org.bzdev.math;
import java.util.Random;
import java.lang.reflect.*;
import org.bzdev.math.rv.RandomVariable;
import org.bzdev.math.Functions;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Random-number generator class.
 * Provides a single random number generator for applications as
 * all the methods are static.  The methods include ones with the
 * same name and behavior as the public methods of the class
 * java.util.Random and the setSeed methods for java.security.SecureRandom.
 * Additional methods are
 * <ul>
 *  <li> {@link #maximizeQuality()}. Maximize the quality of the random numbers
 *       (by using the class {@link java.security.SecureRandom} internally).
 *  <li> {@link #minimizeQuality()}.  Minimize the quality of the random numbers
 *       (by using the class {@link java.util.Random} internally).
 *  <li> {@link #nextPoissonIATime(double)}. Return a long integer representing
 *       Poisson-distributed interarrival times.
 *  <li> {@link #nextPoissonIATime(double,int)}. Return a long integer
 *       representing the some of a fixed number of Poisson-distributed
 *       interarrival times.
 *  <li> {@link #nextDoubleExpDistr(double)} Return a double representing an
 *       exponentionally distributed value.
 *  <li> {@link #nextDoubleExpDistr(double,int)}. Return a double representing
 *       the sum of a fixed number of exponentionally distributed values.
 *  <li> {@link #poissonInt(double)}. Return an integer representing a
 *       Poisson-distributed value.
 *  <li> {@link #poissonInt(double,boolean)}. Return an integer representing a
 *       Poisson-distributed value, requesting the use of a table to speed
 *       up the computation when the second argument has the value
 *       <code>true</code>.
 *  <li> {@link #poissonLong(double)}. Return a long integer representing a
 *       Poisson-distributed value.
 *  <li> {@link #poissonLong(double,boolean)}. Return a a long integer
 *       representing a Poisson-distributed value, requesting the use of
 *       a table to speed up the computation when the second argument has
 *       the value <code>true</code>.
 *  <li> {@link #poissonDouble(double)}. Return a double representing a
 *       Poisson-distributed value.
 *  <li> {@link #poissonDouble(double,boolean)}. Return a double representing a
 *       Poisson-distributed value, requesting the use of a table to speed
 *       up the computation when the second argument has the value
 *       <code>true</code>.
 * </ul>
 * The two-argument methods that generate Poisson distributions use
 * tables created by the class {@link org.bzdev.math.PoissonTable}.
 * The use of these tables, applicable when the parameter &lambda;&le;745,
 * will minimize computation time but at the expense of using additional
 * memory. The algorithms used for computing Poisson distributed values
 * are:
 * <ul>
 *   <li> Table lookup: used for &lambda;&le;745 and when a table
 *        is available.  A table, if not currently cached, is created
 *        when &lambda;&le;745 and the second argument of a
 *        two-argument Poisson method has the value <code>true</code>.
 *        The table may be removed by the garbage collector, although the
 *        method {@link org.bzdev.math.PoissonTable#add(double)} can be used
 *        to create a persistent table.  This algorithm is basically
 *        inverse transform sampling with pre-computed values and a
 *        binary search to find the inverse. The limit on &lambda; is
 *        set by numerical-accuracy constraints.
 *   <li> Inverse transform sampling: used for &lambda;&le;18. The inverse
 *        is found by computing the CDF for increasing values of k until
 *        the value for the CDF is larger than a uniformly distributed
 *        random number (computed once).
 *   <li> The PTRD algorithm (Wolfgang H&ouml;rmann, "The Transformed
 *        Rejection Method for Generating Poisson Random Variables,"
 *        page 6, Virtschaftsuniversit&auml;t Wien, April 1992,
 *        <a href="http://epub.wu.ac.at/352/1/document.pdf">
 *        http://epub.wu.ac.at/352/1/document.pdf</a>): used for
 *        18&lt;&lambda;&lt;2<sup>25</sup>.
 *   <li> Approximation via a normal distribution: used for
 *        &lambda;&ge;2<sup>25</sup>.
 * </ul>
 * @see org.bzdev.math.PoissonTable
 */
public class StaticRandom {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private static long MAX_FOR_RINT = (1L << 53);

    private static Random random = new Random();
    private static Random old = null;

    private static boolean mqNotCalled = true;
    private static boolean secure = false;

    /**
     * Determine if the current random number generator is a high
     * quality one.
     * @return true if it is high quality; false otherwise
     */
    public static boolean isHighQuality() {
	return secure;
    }

    /**
     * Maximize the quality of the random number generator.
     * This should normally be called before the random number generator 
     * first is used.
     * It will increase the quality of the sequence of random numbers
     * created by using a cryptographically secure random number generator,
     * but will be computationally more expensive.
     */
    static public void maximizeQuality() {
	if (mqNotCalled) {
	    old = random;
	    random = new java.security.SecureRandom();
	    mqNotCalled = false;
	} else {
	    if (old instanceof java.security.SecureRandom) {
		Random tmp = old;
		old = random;
		random = old;
	    }
	}
	secure = true;
    }

    /**
     * Minimize the quality of the random number generator.
     * This should normally be called before the random number generator 
     * first is used.
     * It will decrease the quality of the sequence of random numbers
     * created by using the default random number generator.
     */
    static public void minimizeQuality() {
	if (mqNotCalled || old instanceof java.security.SecureRandom) {
	    return;
	} else {
	    Random tmp = old;
	    old = random;
	    random = old;
	    secure = false;
	}
    }



    /**
     * Generates a sequence of random bytes and puts them into an array.
     * @param bytes the array to store the bytes generated
     */
    static public void nextBytes(byte[] bytes) {
	random.nextBytes(bytes);
    }

    /**
     * Generate a boolean with equal odds of returning true or false.
     * @return true or false
     */
    static public boolean nextBoolean() {
	return random.nextBoolean();
    }

    // So nobody will instantiate it - this class only contains static
    // methods.
    private StaticRandom(){}


    /**
     * Generate a double-precision floating point random number in the range
     * [0.0, 1.0].
     * All values in the range have an equal probability of occurring.
     * @return a number in [0.0, 1.0]
     */
    static public double nextDouble() {
	return random.nextDouble();
    }

    /**
     * Generate a single-precision floating point random number in the range
     * [0.0, 1.0].
     * All values in the range have an equal probability of occurring.
     * @return a number in [0.0, 1.0]
     */
    static public float nextFloat() {
	return random.nextFloat();
    }

    /**
     * Generate a random number with a Gaussian distribution with a mean of 0.0
     * and a standard deviation of 1.0.
     * @return a random number with a Gaussian distribution.
     */
    static public double nextGaussian() {
	return random.nextGaussian();
    }


    /**
     * Generate uniformly distributed random integer.
     * @return a random integer
     */
    static public int nextInt() {
	return random.nextInt();
    }

    /**
     * Generate uniformly distributed random integer in the range [0,n).
     * @param n the upper bound on the number generated with all generated
     *        values below this value
     * @return a random integer in the range [0,n).
     */
    static public int nextInt (int n) {
	return random.nextInt(n);
    }

    /**
     * Generate uniformly distributed random  long integer.
     * @return a random long integer
     */
    static public long nextLong() {
	return random.nextLong();
    }

    /**
     * Set the seed for the random number generator.
     * Only a subset of possible initial random-number generator states
     * may be possible due to the limited number of bits in a long integer,
     * although that is generally an issue only for cryptographic applications.
     * <p>
     * The seed is set for the current random number generator, which
     * may be changed between two implementations when 
     * {@link #maximizeQuality() maximizeQuality()}
     * or {@link #minimizeQuality()  minimizeQuality()} is called.
     * @param seed the seed
     */
    static public void setSeed(long seed) {
	random.setSeed(seed);
    }

    /**
     * Set the seed using a seed of arbitrary length.
     * For a for a high-quality random number generator, the seed is
     * used directly by an instance of java.security.SecureRandom.  If
     * a low quality random number generator is used, a SHA-1 digest
     * of the seed is computed and the first 8 bytes of the digest are
     * used.
     * <p>
     * The seed is set for the current random number generator, which
     * may be changed between two implementations when 
     * {@link #maximizeQuality() maximizeQuality()}
     * or {@link #minimizeQuality()  minimizeQuality()} is called.
     * @param seed the seed
     */
    static public void setSeed(byte[] seed) {
	if (random instanceof java.security.SecureRandom) {
	    ((java.security.SecureRandom)random).setSeed(seed);
	} else {
	    long lseed = 0;
	    try {
		java.security.MessageDigest digest =
		    java.security.MessageDigest.getInstance("SHA-1");
		byte[] nbytes = digest.digest(seed);
		for (int i = 0; i < 8; i++) {
		    lseed = lseed << 8;
		    lseed |= nbytes[i];
		}
	    } catch ( java.security.NoSuchAlgorithmException e){
		for (int i = 0; i < seed.length; i++) {
		    long top = lseed >> 56;
		    lseed = lseed << 8 | top;
		    lseed ^= seed[i];
		}
		
	    }
	    random.setSeed(lseed);
	}
    }

    static byte[] generateSeed(int nbytes) {
	if (random instanceof java.security.SecureRandom) {
	    return ((java.security.SecureRandom)random).generateSeed(nbytes);
	} else {
	    byte[] bytes = new byte[nbytes];
	    random.nextBytes(bytes);
	    return bytes;
	}
    }


    /**
     * Generate random numbers for interarrival times for
     * Poisson distributed events.
     * Note: the mean is the reciprocal of the rate for the interarrival
     * times for a Poisson distribution, where the probability density for
     * the interarrival times is given by
     * f(x) = &lambda;e<sup>-&lambda;x</sup> where &lamba; is the rate.
     * @param mean the mean value of the distribution
     * @return a random number that gives the interarrival
     *         times for Poisson distributed events
     *        
     */
    static public long nextPoissonIATime(double mean) {
        double value = -1;
	if (mean == 0.0) return 0;
        while (value < 0.0) {
	    double x = random.nextDouble();
	    if (x <= 0.0) continue;
            value = (long) (mean *(-(java.lang.StrictMath.log(x))));
        }
        return StrictMath.round(value);
    }


    /**
     * Generate a random numbers that are the sum of n random variables that
     * represent interarrival times for a Poisson process with the same mean
     * interarrival time.
     * Note: the mean is the reciprocal of the rate for the interarrival
     * times for a Poisson distribution, where the probability density for
     * the interarrival times is given by
     * f(x) = &lambda;e<sup>-&lambda;x</sup> where &lamba; is the rate.
     * This method is provided because it is faster than simply calling
     * nextPoissonIATime() n times and adding up those values.
     * @param mean the mean value for the distribution
     * @param n the number of random variables to sum
     * @return a random number that is the sum of n random variables that have
     *         an exponential distribution with the same mean value
     */
    static public long nextPoissonIATime(double mean, int n) {
	if (mean == 0.0) return 0;
	double value = -1.0;
	double product = 1.0;
	if (n < LIMIT) {
	    for (int i = 0; i < n; i++) {
		boolean notDone = true;
		while (notDone) {
		    double x = random.nextDouble();
		    if (x <= 0.0) continue;
		    notDone = false;
		    product *= x;
		}
	    }
	    value = mean * (-java.lang.StrictMath.log(product));
	} else {
	    // Gaussian approximation.
	    value = mean * n
		+ nextGaussian() * mean * StrictMath.sqrt((double)n);
	}
	return StrictMath.round(value);
    }

    /**
     * Generate random numbers with an exponential distribution.
     * Note: the mean is the reciprocal of the rate for an exponential
     * distribution, where the probability density is given by
     * f(x) = &lambda;e<sup>-&lambda;x</sup> where &lamba; is the rate.
     * @param mean the mean value for the distribution
     * @return a random number that has an exponential distribution
     */
    static public double nextDoubleExpDistr(double mean) {
	if (mean == 0.0) return 0.0;
        double value = -1.0;
        while (value < 0) {
	    double x = random.nextDouble();
	    if (x <= 0.0) continue;
            value = mean * (-java.lang.StrictMath.log(x));
        }
        return value;
    }

    static final int LIMIT = 32;

    /**
     * Generate a random numbers that are the sum of n exponentially
     * distributed random numbers, each of which has the same mean value.
     * This method is provided because it is faster than simply calling
     * nextDoubleExpDistr() n times and adding up those values.
     * @param mean the mean value for the distribution
     * @param n the number of random variables to sum
     * @return a random number that is the sum of n random variables that have
     *         an exponential distribution with the same mean value
     */
    static public double nextDoubleExpDistr(double mean, int n) {
	if (mean == 0.0) return 0.0;
	double value = -1.0;
	double product = 1.0;
	if (n < LIMIT) {
	    for (int i = 0; i < n; i++) {
		boolean notDone = true;
		while (notDone) {
		    double x = random.nextDouble();
		    if (x <= 0.0) continue;
		    notDone = false;
		    product *= x;
		}
	    }
	    value = mean * (-java.lang.StrictMath.log(product));
	} else {
	    // Gaussian approximation.
	    value = mean * n
		+ nextGaussian() * mean * StrictMath.sqrt((double)n);
	}
	return value;
    }



    static final double MAX_LAMBDA_INT =
	(int)(StrictMath.round((double)Integer.MAX_VALUE
			       - 10.0
			       * StrictMath.sqrt((double)Integer.MAX_VALUE)));


    static final double POISSON_LIMIT = 18.0;
    static final double POISSON_HOERMANN_LIMIT = (double)(1 << 26);

    static final double LOG_ROOT_2PI =
	StrictMath.log(StrictMath.sqrt(2.0*StrictMath.PI));
    static final double twelveInverse = 1.0/12.0;
    static final double Inverse360 = 1.0/360.0;

    // Function based on an algorithm presented in Wolfgang Hoermann,
    // "The Transformed Rejection Method for Generating Poisson Random Variables"
    // Department of Applied Statistics and Data Processing
    // Wirtschaftsuniversitaet Wien
    // http://epub.wu.ac.at/352/1/document.pdf

    static final long ptrd(double lambda) {
	double smu = StrictMath.sqrt(lambda);
	double b = 0.931 + 2.53*smu;
	double a = -0.059 + 0.02483*b;
	double alphaInverse = 1.1239 + 1.1328/(b-3.4);
	double vr = 0.9277 - 3.6224/(b-2.0);
	for(;;) {
	    double v;
	    do {
		v = StaticRandom.nextDouble();
	    } while (v <= 0.0 || v >= 1.0);
	    double u;
	    if (v <= 0.86*vr) {
		u = v/vr - 0.43;
		return StrictMath.round
		    (StrictMath.floor((2.0*a/(0.5-StrictMath.abs(u)) + b)*u
				      + lambda + 0.445));
	    }
	    if (v >= vr) {
		do {
		    u = StaticRandom.nextDouble() - 0.5;
		} while (u <= -0.5 || u >= 0.5);
	    } else {
		u = v/vr - 0.93;
		double sgn = StrictMath.signum(u);
		if (sgn == 0.0) {
		    // ambiguous case that just about never occurs,
		    // so we just try again.
		    continue;
		}
		u = 0.5*sgn - u;
		do {
		    v = vr*StaticRandom.nextDouble();
		} while (v <= 0.0 || v >= vr);
	    }
	    double us = 0.5 - StrictMath.abs(u);
	    if (us < 0.013 && v > us) continue;
	    long k = StrictMath.round(StrictMath.floor((2.0*a/us + b)*u
						       + lambda + 0.445));
	    v = v*alphaInverse/(a/(us*us) + b);

	    if (k >= 10) {
		double k2 = (double)k*((double)k);
		double lim =(k+0.5)*StrictMath.log(lambda/k)
		    - lambda - LOG_ROOT_2PI
		    + k - (twelveInverse - Inverse360/k2)/k;
		if (StrictMath.log(v*smu) <= lim) return k;
	    }
	    if (0 <= k && k <= 9) {
		double lim2 =  k*StrictMath.log(lambda) - lambda
		    - Functions.logFactorial((int)k);
		if (StrictMath.log(v) <= lim2) return k;
	    }
	}
    }

    public static long poissonCDF(double lambda) {
	long val = 0;
	double p = StrictMath.exp(-lambda);
	double s = p;
	double u = random.nextDouble();
	while (u > s) {
	    val++;
	    if (val < 0) return Long.MAX_VALUE;
	    p *= lambda/val;
	    s += p;
	}
	return val;
    }


    /**
     * Generate a random integer with a Poisson distribution.
     * @param lambda the mean value of the distribution
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     */
    public static int poissonInt(double lambda) {
	return poissonInt(lambda, false);
    }
    /**
     * Generate a random integer with a Poisson distribution and optionally
     * using a table for efficiency.
     * <P>
     * When <code>mode</code> is <code>true</code>, a precomputed table
     * of the cummulative distribution function is used in conjunction with
     * binary search.  Due to numerical accuracy issues, this is allowed
     * only when the parameter lambda is less than 644.  If lambda is larger
     * than 644, the parameter <code>mode</code> is set to <code>false</code>.
     * A temporary cache of tables for values of the argument
     * <code>lambda</code> is maintained transparently, with entries that are
     * not currently in use removed at the discretion of the Java garbage
     * collector.  If a table for a given value of <code>lambda</code> should
     * be persistent, the method {@link PoissonTable#add(double)} can be used.
     * @param lambda the mean value of the distribution
     * @param mode true if table should be used to speed up the
     *        computation; false otherwise
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     * @see PoissonTable#add(double)
     */
    public static int poissonInt(double lambda, boolean mode) {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	if (lambda > MAX_LAMBDA_INT) throw new IllegalArgumentException
					 (errorMsg("lambdaTooLarge", lambda));
	PoissonTable pt;
	if (lambda <= PoissonTable.MAX_LAMBDA) {
	    pt = mode? PoissonTable.createTable(lambda):
		PoissonTable.getTable(lambda);
	} else {
	    pt = null;
	}
	if (pt != null) {
	    return pt.next();
	} else if (lambda <= POISSON_LIMIT) {
	    int val = 0;
	    double p = StrictMath.exp(-lambda);
	    double s = p;
	    double u = random.nextDouble();
	    while (u > s) {
		val++;
		if (val < 0) return Integer.MAX_VALUE;
		p *= lambda/val;
		s += p;
	    }
	    return val;
	} else if (lambda < POISSON_HOERMANN_LIMIT) {
	    int val;
	    do {
		val = (int) ptrd(lambda);
	    } while (val < 0);
	    return val;
	} else {
	    double val;
	    long ival;
	    do {
		val = lambda
		    + StaticRandom.nextGaussian() * StrictMath.sqrt(lambda);
		ival = StrictMath.round(val);
	    } while ((val > (double)Integer.MAX_VALUE) || ival < 0);
	    return (int)ival;
	}
    }

    static final double MAX_LAMBDA_LONG =
	(StrictMath.round((double)Long.MAX_VALUE
			  - 10.0 * StrictMath.sqrt((double)Long.MAX_VALUE)));


    /**
     * Generate a random long integer with a Poisson distribution.
     * @param lambda the mean value of the distribution
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     */
    public static long poissonLong(double lambda) {
	return poissonLong(lambda, false);
    }
    /**
     * Generate a random long integer with a Poisson distribution and optionally
     * using a table for efficiency.
     * <P>
     * When <code>mode</code> is <code>true</code>, a precomputed table
     * of the cummulative distribution function is used in conjunction with
     * binary search.  Due to numerical accuracy issues, this is allowed
     * only when the parameter lambda is less than 644.  If lambda is larger
     * than 644, the parameter <code>mode</code> is set to <code>false</code>.
     * A temporary cache of tables for values of the argument
     * <code>lambda</code> is maintained transparently, with entries that are
     * not currently in use removed at the discretion of the Java garbage
     * collector.  If a table for a given value of <code>lambda</code> should
     * be persistent, the method {@link PoissonTable#add(double)} can be used.
     * @param lambda the mean value of the distribution
     * @param mode true if table should be used to speed up the
     *        computation; false otherwise
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     * @see PoissonTable#add(double)
     */
    public static long poissonLong(double lambda, boolean mode) {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	if (lambda > MAX_LAMBDA_LONG) throw new IllegalArgumentException
					  (errorMsg("lambdaTooLarge", lambda));
	PoissonTable pt;
	if (lambda <= PoissonTable.MAX_LAMBDA) {
	    pt = mode? PoissonTable.createTable(lambda):
		PoissonTable.getTable(lambda);
	} else {
	    pt = null;
	}
	if (pt != null) {
	    return (long) pt.next();
	} else if (lambda <= POISSON_LIMIT) {
	    long val = 0;
	    double p = StrictMath.exp(-lambda);
	    double s = p;
	    double u = random.nextDouble();
	    while (u > s) {
		val++;
		if (val < 0) return Long.MAX_VALUE;
		p *= lambda/val;
		s += p;
	    }
	    return val;
	} else if (lambda < POISSON_HOERMANN_LIMIT) {
	    return ptrd(lambda);
	} else {
	    double val;
	    long ival;
	    do {
		val = lambda
		    + StaticRandom.nextGaussian() * StrictMath.sqrt(lambda);
		ival = StrictMath.round(val);
	    } while ((val > (double)Long.MAX_VALUE) || ival < 0);
	    return ival;
	}
    }

    /**
     * Generate a random number with a Poisson distribution.
     * The number returned is a double that (when possible) will be
     * rounded to the nearest long integer and then type casted so that
     * the value returned is a double.
     * @param lambda the mean value of the distribution
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     */
    public static double poissonDouble(double lambda) {
	return poissonDouble(lambda, false);
    }
    /**
     * Generate a random number with a Poisson distribution and optionally
     * using a table for efficiency.
     * The number returned is a double that (when possible) will be
     * rounded to the nearest long integer and then type casted so that
     * the value returned is a double.
     * <P>
     * When <code>mode</code> is <code>true</code>, a precomputed table
     * of the cummulative distribution function is used in conjunction with
     * binary search.  Due to numerical accuracy issues, this is allowed
     * only when the parameter lambda is less than 644.  If lambda is larger
     * than 644, the parameter <code>mode</code> is set to <code>false</code>.
     * A temporary cache of tables for values of the argument
     * <code>lambda</code> is maintained transparently, with entries that are
     * not currently in use removed at the discretion of the Java garbage
     * collector.  If a table for a given value of <code>lambda</code> should
     * be persistent, the method {@link PoissonTable#add(double)} can be used.
     * @param lambda the mean value of the distribution
     * @param mode true if table should be used to speed up the
     *        computation; false otherwise
     * @return a random number with a Poission distribution
     * @exception an argument was out of range
     * @see PoissonTable#add(double)
     */
    public static double poissonDouble(double lambda, boolean mode) {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	PoissonTable pt;
	if (lambda <= PoissonTable.MAX_LAMBDA) {
	    pt = mode? PoissonTable.createTable(lambda):
		PoissonTable.getTable(lambda);
	} else {
	    pt = null;
	}
	if (pt != null) {
	    return (double) pt.next();
	} else if (lambda <= POISSON_LIMIT) {
	    long val = 0;
	    double p = StrictMath.exp(-lambda);
	    double s = p;
	    double u = random.nextDouble();
	    while (u > s) {
		val = val + 1;
		if (val < 0) return (double)Long.MAX_VALUE;
		p *= lambda/val;
		s += p;
	    }
	    return (double)val;
	} else if (lambda < POISSON_HOERMANN_LIMIT) {
	    return (double) ptrd(lambda);
	} else {
	    double val;
	    val = lambda
		+ StaticRandom.nextGaussian() * StrictMath.sqrt(lambda);
	    if (val < (double)(MAX_FOR_RINT)) {
		val = StrictMath.rint(val);
	    }
	    return val;
	}
    }

    /**
     * Create a new random variable.
     * @param clasz the class implementing the random variable (must be
     *        a class that implements org.cmdl.math.rv.RandomVariable)
     * @param args arguments that match a constructor for the class
     *        clasz
     * @return a new random variable
     */

    @SuppressWarnings("unchecked")
	static public <T> T newRandomVariable(Class<T>clasz, Object... args)
	throws InstantiationException,
	       IllegalAccessException,
	       IllegalArgumentException,
	       InvocationTargetException
    {
	IllegalArgumentException e = null;
	if (!RandomVariable.class.isAssignableFrom(clasz)) {
	    throw new IllegalArgumentException
		(errorMsg("notRVClass", clasz.getName()));
	}
	for (Constructor<?> c: clasz.getConstructors()) {
	    try {
		Constructor<T>ct = (Constructor<T>)c;
		return ct.newInstance(args);
	    } catch (IllegalArgumentException e1) {
		e = e1;
	    }
	}
	if (e != null) {
	    throw e;
	}
	else throw new 
		 IllegalArgumentException(errorMsg("createRVFailed"));
    }
}

//  LocalWords:  exbundle setSeed ul li maximizeQuality interarrival
//  LocalWords:  minimizeQuality nextPoissonIATime nextDoubleExpDistr
//  LocalWords:  exponentionally poissonInt boolean poissonLong le lt
//  LocalWords:  poissonDouble pre CDF PTRD ouml rmann auml Wien href
//  LocalWords:  Virtschaftsuniversit ge cryptographically SHA lamba
//  LocalWords:  cryptographic Hoermann Wirtschaftsuniversitaet clasz
//  LocalWords:  Poission precomputed cummulative PoissonTable casted
//  LocalWords:  lambdaWasNegative lambdaTooLarge args notRVClass
//  LocalWords:  createRVFailed
