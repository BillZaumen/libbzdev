package org.bzdev.math.stats;
import org.bzdev.lang.UnexpectedExceptionError;

// for javadocs
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class to compute the mean and variance for multiple values..
 * This class provides the variances for a vector of measurements
 * or random variables measured or evaluated multiple times. The inner
 * classes are subclasses of this class:
 * <UL>
 *   <LI> <code>Sample</code> is used to compute the variances for
 *        the case in which the dataset whose variances are computed is a
 *        representative sample.
 *   <LI> <code>Population</code> is used to compute the variances for
 *        the case where the dataset represents every case being computed.
 * </UL>
 * See West D.H.D, Updating mean and variance estimates: An
 * improved method. Commm. ACM22, 9 (Sept 1979, 532--535) for
 * the algorithm. The implementation uses Kahan's summation
 * algorithm to further improve the accuracy.

 */
public abstract class BasicStatsMV {

    // See https://dl.acm.org/doi/pdf/10.1145/359146.359152
    // for a description of West's algorithm + an analysis
    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }


    private int n;
    private long count = 0;
    private double[] savedMeans = null;
    private double[] means = null;
    // private double[] smeans = null;
    // private double[] emeans = null;
    private double[] Ts = null;
    private double[] mc = null;
    private double[] Tc = null;
    private double[] savedVariances = null;
    private double[] savedSDevs = null;
    private double[] variances = null;

    /**
     * Get the number of data-point arrays that were entered.
     * @return the number of data-point tarrays that were entered
     */
    public long size() {
	return count;
    }

    /**
     * Get the number of variables.
     * This is the length of each array that will be entered (if an array
     * is longer than this value, only the first n elements will be used.
     * @return the number of variables.
     */
    public int length() {
	return n;
    }

    /**
     * The elements of the variances array that is computed
     * will be scaled by a correction factor. This factor should
     * be 1.0 when the dataset represents a total population. For
     * a sample of a population, the factor is m/(m-1) where m
     * is the sample size (the number of factors.
     * @return the correction factor; Double.NaN if the factor
     *         cannot be computed due to too few entries
     */
    protected abstract double getCorrection();

    /**
     * Add a new vector of values.
     * The vector's length must be at least n, where n is the
     * number of variables.
     * @param values a vector of values (the first n entries will
     * be used) 
     * @exception IllegalArgumentException the argument array was too short
     */
    public BasicStatsMV add(double[] values)
	throws IllegalArgumentException
    {
	if (values.length < n) {
	    throw new IllegalArgumentException
		(errorMsg("arrayTooShort", values.length, n));
	}
	savedVariances = null;
	savedSDevs = null;
	savedMeans = null;
	long oldcount = count;
	count++;
	for (int i = 0; i < n; i++) {
	    double Q = values[i] - means[i];
	    double R = Q/count;
	    // means[i] += R;
	    double correction = mc[i];
	    double mean = means[i];
	    double incr = R - correction;
	    double mKahan = mean + incr;
	    mc[i] = (mKahan - mean) - incr;
	    means[i] = mKahan;
	    // Ts[i] += oldcount*Q*R;
	    correction = Tc[i];
	    incr = (oldcount*Q*R) - correction;
	    double T = Ts[i];
	    double TKahan = T + incr;
	    Tc[i] = (TKahan - T) - incr;
	    Ts[i] = TKahan;
	}
	return this;
    }

    /**
     * Add the values from another BasicStatsMV object.
     * @param stats the BasicStats object whose values should be added
     * @return this object
     */
    public BasicStatsMV addAll(BasicStatsMV stats) {
	if (n > stats.length()) {
	    String msg = errorMsg("statsMVTooShort", stats.length(), n);
	    throw new IllegalArgumentException(msg);
	}
	if (stats.count == 0) return this;
	savedVariances = null;
	savedSDevs = null;
	savedMeans = null;
	long scount = stats.count;
	long newcount = count + scount;
	for (int i = 0; i < n; i++) {
	    double meani = means[i];
	    double smeani = stats.means[i];
	    double nmc = (meani*count + smeani*scount);
	    double c1 = meani*meani*count;
	    double c2 = smeani*smeani*scount;
	    meani = nmc/newcount;
	    means[i] = meani;
	    Ts[i] = (Ts[i] + stats.Ts[i]) + ((c1+c2) - newcount*meani*meani);
	    // reset Kahan's summation algorithm as we did not add.
	    mc[i] = 0.0;
	    Tc[i] = 0.0;
	}
	count = newcount;
	return this;
    }
    /**
     * Get the variances.
     * @return the variances
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getVariances() throws IllegalStateException {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	double[] results = new double[n];
	double correction = getCorrection();
	if (correction == 1.0) {
	    for (int i = 0; i < n; i++) {
		    results[i] = Ts[i]/count;
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		    results[i] = correction * Ts[i]/count;
	    }
	}
	return results;
    }

    /**
     * Get the variances.
     * If the argument array is too short or is null, a new array will
     * be allocated. The returned value will be the argument array if that
     * is long enough or a newly allocated array otherwise.
     * @param results an array to hold the variances
     * @return the variances
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getVariances(double[] results)
	throws IllegalStateException
    {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	if (results == null || results.length < n) {
	    results = new double[n];
	}
	double correction = getCorrection();
	if (correction == 1.0) {
	    for (int i = 0; i < n; i++) {
		    results[i] = Ts[i]/count;
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		    results[i] = correction * Ts[i]/count;
	    }
	}
	return results;
    }

    /**
     * Get the standard deviations.
     * @return the standard deviations
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getSDevs() throws IllegalStateException {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	double correction = getCorrection();
	if (Double.isNaN(correction)) {
	    throw new IllegalStateException
		(errorMsg("datasetTooSmall", count));
	}
	double[] results = new double[n];
	if (correction == 1.0) {
	    for (int i = 0; i < n; i++) {
		results[i] = Math.sqrt(Ts[i]/count);
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		results[i] = Math.sqrt(correction * Ts[i]/count);
	    }
	}
	return results;
    }

    /**
     * Get the standard deviations.
     * If the argument array is too short or is null, a new array will
     * be allocated. The returned value will be the argument array if that
     * is long enough or a newly allocated array otherwise.
     * @param results an array to hold the standard deviations
     * @return the standard deviations
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getSDevs(double[] results) throws IllegalStateException {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	double correction = getCorrection();
	if (Double.isNaN(correction)) {
	    throw new IllegalStateException
		(errorMsg("datasetTooSmall", count));
	}
	if (results == null || results.length < n) {
	    results = new double[n];
	}
	if (correction == 1.0) {
	    for (int i = 0; i < n; i++) {
		results[i] = Math.sqrt(Ts[i]/count);
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		results[i] = Math.sqrt(correction * Ts[i]/count);
	    }
	}
	return results;
    }

    /**
     * Get a the mean values.
     * @return the mean values
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getMeans() throws IllegalStateException {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	if (savedMeans == null) {
	    savedMeans = means.clone();
	}
	return savedMeans;
    }

    /**
     * Get a the mean values.
     * If the argument array is too short or is null, a new array will
     * be allocated. The returned value will be the argument array if that
     * is long enough or a newly allocated array otherwise.
     * @param results an array to hold the means
     * @return the mean values
     * @exception IllegalStateException the data set was too small or missing
     */
    public double[] getMeans(double[] results) throws IllegalStateException {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}

	if (results == null || results.length < n) {
	    results = new double[n];
	}
	for (int i = 0; i < n; i++) {
	    results[i] = means[i];
	}
	return results;
    }



    /**
     * Constructor.
     * The method {@link #add(double[])} must be called repeatedly to
     * provide the dataset used to compute the variance and mean
     * array.  
     * @param n the number of variables
     */
    protected BasicStatsMV(int n) {
	this.n = n;
	means = new double[n];
	Ts = new double[n];
	mc = new double[n];
	Tc = new double[n];
    }


    /**
     * Constructor given means, uncorrected variances, and a data-set size
     * @param means an array of length n containing the mean values for
     *        n variables
     * @param variances an array of length n containing the variances for
     *        n variables
     * @param m the data-set size
     * @exception IllegalArgumentException the array lengths differ
     */
    protected BasicStatsMV(double[]means, double[] variances, long m) {
	if (means.length != variances.length) {
	    throw new IllegalArgumentException(errorMsg("unequalArrayLengths"));
	}
	n = means.length;
	count = m;
	this.means = means.clone();
	// smeans = means.clone();
	Ts = variances.clone();
	mc = new double[n];
	Tc = new double[n];
	for (int i = 0; i < n; i++) {
	    Ts[i] *= (double)count;
	}
	// emeans = new double[means.length];
    }

    /**
     * Constructor given an array containing the initial dataset
     * used to compute a variance.
     * Note: More data can be added using the {@link #add(double[])} method.
     * @param arrays an array, each component of which
     *        is an array containing the values for n
     *        variables
     * @param n the number of variables
     * @exception IllegalArgumentException the arrays argument was too short
     */
    protected BasicStatsMV(double[][]arrays, int n)
	throws IllegalArgumentException
    {
	this.n = n;
	means = new double[n];
	variances = new double[n];
	Ts = new double[n];
	mc = new double[n];
	Tc = new double[n];
	for (int k = 0; k < arrays.length; k++) {
	    add(arrays[k]);
	}
    }

    /**
     * Class to compute a sample variance.
     */
    public static class Sample extends BasicStatsMV {


	/**
	 * Get an instance of Sample that serves as an identity
	 * for {@link Stream#reduce(Object,BiFunction,BinaryOperator)}.
	 * An identity implementation will not store any data, but will
	 * return an appropriate instance of BasicStats that does store
	 * the data added, but with no history of previously added values.
	 * <P>
	 * If rv is a random variable that returns a stream of double values,
	 * the following example shows a typical use of this method:
	 * <BLOCKQUOTE><CODE><PRE>
	 * BasicStatsMV stats = rvs.parallelStream(1000000)
	 *   .mapToObj(Double::valueOf)
	 *   .reduce(BasicStats.Sample.identity(10),
	 *	    BasicStats::add,
	 *	    BasicStats::addAll);
	 * </PRE></CODE></BLOCKQUOTE>
	 * The method mapToObj is needed because the reduce method is
	 * a method of {@link Stream} but not {@link DoubleStream}.
	 * When used with a parallel stream, the mapToObj method;'s argument
	 * will typically be a lambda expression that peforms significantly
	 * more compuation: otherwise the cost of threading can result in
	 * the parallel version performing worse than the sequential version.
	 * @param n the length of each array that can be entered
	 * @return an instance of BasicStat that can be used as an idenity
	 *         in a "reduce" method.
	 */
	public static final Identity identity(int n) {
	    return new Identity(n);
	}

	private static class Identity extends Sample {

	    Identity(int n) {
		super(n);
	    }

	    @Override
	    public BasicStatsMV add(double[] array) {
		Sample result = new Sample(length());
		return result.add(array);
	    }

	    @Override
	    public BasicStatsMV addAll(BasicStatsMV stats) {
		try {
		    if (stats instanceof Sample) {
			return (BasicStatsMV) stats.clone();
		    } else {
			Sample results = new Sample(length());
			return results.addAll(stats);
		    }
		} catch (CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
	    }
	}

	@Override
	protected double getCorrection() {
	    long count = size();
	    if (count == 1) return Double.NaN;
	    return count / (count - 1.0);
	}

	/**
	 * Constructor.
	 * The method {@link #add(double[])} must be called repeatedly to
	 * provide the dataset used to compute the variance and mean
	 * array.  
	 * @param n the number of variables
	 */
	public Sample(int n) {
	    super(n);
	}


	private static double[] correctVariances(double[] variances, long m) {
	    double[] result = variances.clone();
	    double scale = (m - 1.0)/ m;
	    for (int i = 0; i < variances.length; i++) {
		result[i] *= scale;
	    }
	    return result;
	}

	/**
	 * Constructor given means, variances, and the data-set size (the
	 * number for each variable)
	 * @param means an array of length n containing the mean values for
	 *        n variables
	 * @param variances an array of length n containing the variances for
	 *        n variables
	 * @param m the data-set size
	 * @exception IllegalArgumentException the array lengths differ
	 */
	public Sample(double[]means, double[] variances, long m) {
	    super(means, correctVariances(variances, m), m);
	}

	/**
	 * Constructor given an array containing the initial dataset
	 * used to compute a variance. This dataset can be extended
	 * using the {@link #add(double[])} method.
	 * @param arrays an array, each component of which
	 *        is an array containing the values for n
	 *        variables
	 * @param n the number of variables
	 * @throws IllegalArgumentException the argument arrays was too short
	 */
	public Sample(double[][] arrays, int n)
	    throws IllegalArgumentException
	{
	    super(arrays, n);
	}
    }

    /**
     * Class to compute the variance for a total set of
     * values, as opposed to a sample of values.
     */
    public static class Population extends BasicStatsMV {

	/**
	 * Get an instance of Population that serves as an identity
	 * for {@link Stream#reduce(Object,BiFunction,BinaryOperator)}.
	 * An identity implementation will not store any data, but will
	 * return an appropriate instance of BasicStats that does store
	 * the data added, but with no history of previously added values.
	 * <P>
	 * If rv is a random variable that returns a stream of double values,
	 * the following example shows a typical use of this method:
	 * <BLOCKQUOTE><CODE><PRE>
	 * BasicStatsMV stats = rvs.parallelStream(1000000)
	 *   .mapToObj(Double::valueOf)
	 *   .reduce(BasicStats.Sample.identity(10),
	 *	    BasicStats::add,
	 *	    BasicStats::addAll);
	 * </PRE></CODE></BLOCKQUOTE>
	 * The method mapToObj is needed because the reduce method is
	 * a method of {@link Stream} but not {@link DoubleStream}.
	 * When used with a parallel stream, the mapToObj method;'s argument
	 * will typically be a lambda expression that peforms significantly
	 * more compuation: otherwise the cost of threading can result in
	 * the parallel version performing worse than the sequential version.
	 * @param n the length of each array that can be entered
	 * @return an instance of BasicStat that can be used as an idenity
	 *         in a "reduce" method.
	 */
	public static final Identity identity(int n) {
	    return new Identity(n);
	}

	private static class Identity extends Sample {

	    Identity(int n) {
		super(n);
	    }

	    @Override
	    public BasicStatsMV add(double[] array) {
		Population result = new Population(length());
		return result.add(array);
	    }

	    @Override
	    public BasicStatsMV addAll(BasicStatsMV stats) {
		try {
		    if (stats instanceof Population) {
			return (BasicStatsMV) stats.clone();
		    } else {
			Population results = new Population(length());
			return results.addAll(stats);
		    }
		} catch (CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
	    }
	}


	@Override
	protected double getCorrection() {
	    return 1.0;
	}

	/**
	 * Constructor.
	 * The method {@link #add(double[])} must be called repeatedly to
	 * provide the data used to compute the variance and mean
	 * array.
	 * @param n the number of variables
	 */
	public Population(int n) {
	    super(n);
	}

	/**
	 * Constructor given means, variances, and the data-set size (the
	 * number for each variable)
	 * @param means an array of length n containing the mean values for
	 *        n variables
	 * @param variances an array of length n containing the variances for
	 *        n variables
	 * @param m the data-set size
	 * @exception IllegalArgumentException the array lengths differ
	 */
	public Population(double[]means, double[] variances, long m) {
	    super(means, variances, m);
	}


	/**
	 * Constructor given an array containing  the initial dataset
	 * used to compute a variance. This dataset can be extended
	 * using the {@link #add(double[])} method.
	 * @param arrays an array, each component of which
	 *        is an array containing the values for n
	 *        variables
	 * @param n the number of variables
	 * @throws IllegalArgumentException the argument arrays was too short
	 */
	public Population(double[][] arrays, int n)
	    throws IllegalArgumentException
	{
	    super(arrays, n);
	}
    }
}

//  LocalWords:  exbundle dataset NaN addsComplete arrayTooShort
//  LocalWords:  getVariances BasicStatsMV noData datasetTooSmall
//  LocalWords:  subarrayTooShort tarrays IllegalArgumentException
//  LocalWords:  IllegalStateException unequalArrayLengths
