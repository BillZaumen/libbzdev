package org.bzdev.math.stats;
import java.util.Arrays;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.lang.MathOps;

// for javadocs
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

//@exbundle org.bzdev.math.stats.lpack.Stats


/**
 * Class to compute means and variances of a series of values.
 * See West D.H.D, Updating mean and variance estimaes: An
 * improved method. Commm. ACM22, 9 (Sept 1979, 532--535) for
 * the algorithm. The implementation uses Kahan's summation
 * algorithm to further improve the accuracy.
 * <P>
 * This is an abstract class: subclasses provide population and
 * sample variances and standard deviations.
 */
public abstract class BasicStats implements Cloneable {

    // See https://dl.acm.org/doi/pdf/10.1145/359146.359152
    // for a description of West's algorithm + an analysis

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    private long savedCount = 0;
    private double savedVariance = Double.NaN;
    /*
    private long savedCount = 0;
    private double savedVariance = Double.NaN;
    private long count = 0;
    private double  mean = 0.0;
    boolean useSmean = false;
    double smean = 0.0;
    double emean = 0.0;
    protected double variance = 0.0;
    */

    private long count = 0;
    private double mean = 0.0;
    private double T = 0.0;
    // for Kahan's addition algorithm
    private double mc = 0.0;
    private double Tc = 0.0;

    @Override
    public Object clone() throws CloneNotSupportedException {
	return super.clone();
    }

    /**
     * Get the correction factor. The variance that is computed
     * will be scaled by a correction factor. This factor should
     * be 1.0 when the data represents a total population. For
     * a sample of a population, the factor is m/(m-1) where m
     * is the sample size (the number of data points).
     * @return the correction factor
     */
    protected abstract double getCorrection();

    /**
     * Add a new value to the computation of the mean and variance.
     * @param value the value to add
     * @return this object
     */
    public BasicStats add(double value) {
	long oldcount = count;
	count++;
	double Q = value - mean;
	double R = Q/count;
	// mean += R;
	double incr = R - mc;
	double mKahan = mean + incr;
	mc = (mKahan - mean) -incr;
	mean = mKahan;
	// T += oldcount*Q*R;
	incr = oldcount*Q*R - Tc;
	double TKahan = T + incr;
	Tc = (TKahan - T) - incr;
	T = TKahan;
	return this;
	/*
	long oldcount = count;
	double delta;
	if (useSmean) {
	    count++;
	    mean += (value - mean) / count;
	    delta = ((value - smean) - emean) / count;
	    emean += delta;
	} else {
	    count++;
	    delta = (value - mean) / count;
	    mean += delta;
	}
	variance += oldcount*delta*delta - variance/count;
	*/
    }

    /**
     * Add the values from another BasicStats object.
     * @param stats the BasicStats object whose values should be added
     * @return this object
     */
    public BasicStats addAll(BasicStats stats) {
	if (stats.count == 0) return this;
	double nmc = (mean*count + stats.mean*stats.count);
	double c1 = mean*mean*count;
	double c2 = stats.mean*stats.mean*stats.count;
	count += stats.count;
	mean = nmc/count;
	T = (T + stats.T) + ((c1+c2) - count*mean*mean);
	return this;
    }

    /**
     * Get the number of data points that were entered.
     * @return the number of data points that were entered
     */
    public long size() {
	return count;
    }

    /**
     * Get the variance.
     * @return the variance
     * @exception IllegalStateException not enough data to compute a variance
     */
    public double getVariance() throws IllegalStateException {
	if (count == savedCount) {
	    return savedVariance;
	} else {
	    // double correction = count / (count - 1.0);
	    double correction = getCorrection();
	    if (Double.isNaN(correction)) {
		throw new IllegalStateException
		    (errorMsg("datasetTooSmall", count));
	    }
	    // savedVariance = variance*correction;
	    savedVariance = correction *T/count;
	    savedCount = count;
	    return savedVariance;
	}
    }

    /**
     * Get the standard deviation.
     * @return the standard deviation
     */
    public double getSDev() {
	return Math.sqrt(getVariance());
    }

    /**
     * Get the mean value.
     * @return the mean value
     */
    public double getMean() {
	return mean;
    }


    /**
     * Constructor.
     * The method {@link #add(double)} must be called repeatedly to
     * provide the data used to compute the covariance matrix and mean
     * array.  
     */
    protected BasicStats() {
    }

    /**
     * Constructor given a mean, variance, and data-set size.
     * The uncorrected variance is the variance for a population variance.
     * Constructors for subclasses should divide the variance provided
     * by the value that {@link #getCorrection()} would return.
     * @param mean the mean value
     * @param variance the uncorrected variance
     * @param n the data-set size
     */
    protected BasicStats(double mean, double variance, long n) {
	/*
	this.mean = mean;
	this.smean = mean;
	emean = 0.0;
	this.variance = variance;
	count = n;
	useSmean = true;
	*/
	this.mean = mean;
	count = n;
	T = variance*count;
    }


    /**
     * Constructor given an array containing all the data
     * used to compute a mean and variance.
     * @param array an array, each component of which
     *        is a value to add to the computation
     * @exception NullPointerException the argument was null.
     */
    protected BasicStats(double[]array) {
	for (int k = 0; k < array.length; k++) {
	    long oldcount = count;
	    count++;
	    double Q = array[k] - mean;
	    double R = Q/count;
	    mean += R;
	    T += oldcount*Q*R;
	}
	/*
	for (int k = 0; k < array.length; k++) {
	    count++;
	    double y = (array[k] - mean)/count;
	    mean += y;
	}
	smean = mean;
	count = 0;
	double mn = 0.0;
	for (int k = 0; k < array.length; k++) {
	    count++;
	    double delta = ((array[k] - smean) - emean)/count;
	    emean += delta;
	    variance += k*delta*delta - variance/count;
	}
	useSmean = true;
	*/
    }

    /**
     * BasicStats specialized  to compute a sample mean and variance.
     */
    public static class Sample extends BasicStats {

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
	 * BasicStats stats = rv.parallelStream(1000000)
	 *   .mapToObj(Double::valueOf)
	 *   .reduce(BasicStats.Sample.identity(),
	 *	    BasicStats::add,
	 *	    BasicStats::addAll);
	 * </PRE></CODE></BLOCKQUOTE>
	 * The method mapToObj is needed because the reduce method is
	 * a method of {@link Stream} but not {@link DoubleStream}.
	 * When used with a parallel stream, the mapToObj method;'s argument
	 * will typically be a lambda expression that peforms significantly
	 * more compuation: otherwise the cost of threading can result in
	 * the parallel version performing worse than the sequential version.
	 * @return an instance of BasicStat that can be used as an idenity
	 *         in a "reduce" method.
	 */
	public static final Identity identity() {
	    return new Identity();
	}

	private static class Identity extends Sample {
	    Identity() {
		super();
	    }

	    @Override
	    public BasicStats add(double x) {
		Sample result = new Sample();
		return result.add(x);
	    }

	    @Override
	    public BasicStats addAll(BasicStats stats) {
		try {
		    if (stats instanceof Sample) {
			return (BasicStats) stats.clone();
		    } else {
			Sample sample = new Sample();
			return sample.addAll(stats);
		    }
		} catch (CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
	    }
	}


	@Override
	protected double getCorrection() {
	    long count = size();
	    return count / (count - 1.0);
	}

	/**
	 * Constructor.
	 * The method {@link #add(double)} must be called repeatedly to
	 * provide the data used to compute the covariance matrix and mean
	 * array.  
	 */
	public Sample() {
	    super();
	}

	/**
	 * Constructor given a mean, variance, and data-set size.
	 * @param mean the mean value
	 * @param variance the sample variance
	 * @param n the data-set size
	 */
	public Sample(double mean, double variance, long n) {
	    super(mean, ((n-1.0)/n)*variance, n);
	}

	/**
	 * Constructor given an array containing all the data
	 * used to compute a covariance matrix.
	 * @param array an array, each component of which
	 *        is an array containing the values for n
	 *        variables
	 */
	public Sample(double[] array) {
	    super(array);
	}
    }

    /**
     * BasicStats specialized  to compute a population mean and variance.
     */
    public static class Population extends BasicStats {

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
	 * BasicStats stats = rv.parallelStream(1000000)
	 *   .mapToObj(Double::valueOf)
	 *   .reduce(BasicStats.Population.identity(),
	 *	    BasicStats::add,
	 *	    BasicStats::addAll);
	 * </PRE></CODE></BLOCKQUOTE>
	 * The method mapToObj is needed because the reduce method is
	 * a method of {@link Stream} but not {@link DoubleStream}.
	 * When used with a parallel stream, the mapToObj method;'s argument
	 * will typically be a lambda expression that peforms significantly
	 * more compuation: otherwise the cost of threading can result in
	 * the parallel version performing worse than the sequential version.
	 * @return an instance of BasicStat that can be used as an idenity
	 *         in a "reduce" method.
	 */
	public static final Identity identity() {
	    return new Identity();
	}


	private static class Identity extends Population {
	    Identity() {
		super();
	    }

	    @Override
	    public BasicStats add(double x) {
		Population result = new Population();
		return result.add(x);
	    }

	    @Override
	    public BasicStats addAll(BasicStats stats) {
		try {
		    if (stats instanceof Population) {
			return (BasicStats) stats.clone();
		    } else {
			Population result = new Population();
			return result.addAll(stats);
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
	 * The method {@link #add(double)} must be called repeatedly to
	 * provide the data used to compute the covariance matrix and mean
	 * array.  
	 */
	public Population() {
	    super();
	}

	/**
	 * Constructor given a mean, variance, and data-set size.
	 * @param mean the mean value
	 * @param variance the variance
	 * @param n the data-set size
	 */
	public Population(double mean, double variance, long n) {
	    super(mean, variance, n);
	}

	/**
	 * Constructor given an array containing all the data
	 * used to compute a mean and variance
	 * @param array an array, each component of which
	 *        is a value to add to the computation
	 */
	public Population(double[] array) {
	    super(array);
	}
    }

    /**
     * Compute the mean of a data set.
     * @param data the data whose trimmed is to be computed
     * @return the mean of the data set
     * @exception IllegalArgumentException if the length of the array is zero
     * @exception NullPointerException the last argument was null.
     */
    public static double mean(double[] data) {
	if (data.length == 0) {
	    throw new IllegalArgumentException(errorMsg("emptyDataset"));
	}
	BasicStats stats = new BasicStats.Population(data);
	return stats.getMean();
    }


    /**
     * Compute the P-percent trimmed mean of a data set when P = (100)(1/D)
     * for a positive integer D.
     * The data will be divided into a number of bins and the
     * mean will be computed ignoring the first and last bin.
     * <P>
     * There are several common cases:
     * <UL>
     *   <LI> for a 25% trimmed mean, set the number of bins to 4.
     *   <LI> for a 10% trimmed mean, set the number of bins to 10.
     *   <LI> for a 5% trimmed mean, set the number of bins to 20.
     * </UL>
     * If there data set length is not an integral multiple of the
     * number of bins, The trimmed means are computed given offsets
     * from the initial index (0) and ending index (data.length),
     * repeated twice to bracket the desired value.  An esimate is
     * then made by interpolation.
     * @param D the number of bins in which to divide the
     *        data
     * @param data the data whose trimmed mean is to be computed
     * @return the trimmed mean of the data
     * @exception IllegalArgumentException if D is not a positive
     *            integer or the length of the array is zero
     * @exception NullPointerException the last argument was null
     */
    public static double trimmedMean(int D, double[] data) {
	if (D < 1) {
	    throw new IllegalArgumentException(errorMsg("argsNotPositive"));
	}
	if (data.length == 0) {
	    throw new IllegalArgumentException(errorMsg("emptyDataset"));
	}
	data = data.clone();
	Arrays.sort(data);
	boolean exact = ((data.length % D) == 0);
	BasicStats stats = new BasicStats.Population();
	if (exact) {
	    int min = data.length / D;
	    int max = data.length - min;
	    for (int j = min; j < max; j++) {
		stats.add(data[j]);
	    }
	    return stats.getMean();
	} else {
	    int min1 = data.length / D;
	    int max1 = data.length - min1;
	    for (int j = min1; j < max1; j++) {
		stats.add(data[j]);
	    }
	    double mean1 = stats.getMean();
	    stats = new BasicStats.Population();
	    int min2 = min1 + 1;
	    int max2 = data.length - min2;
	    for (int j = min2; j < max2; j++) {
		stats.add(data[j]);
	    }
	    double mean2 = stats.getMean();
	    double t = data.length / ((double)D);
	    t = t - Math.floor(t);
	    if (t < 0.0) t = 0.0;
	    if (t > 1.0) t = 1.0;
	    double mean = mean2*t + mean1*(1.0 - t);
	    return mean;
	}
    }

    /**
     * Compute the P-percent trimmed mean of a data set when P = 100(N/D)
     * for positive integers N and D.
     * The data will be divided into a number of bins and the
     * mean will be computed ignoring the first and last bin.
     *<P>
     * The data will be divided into bins whose length is the length
     * of the data set multiplied by N/D.  Rational numbers are used
     * to represent the fraction trimmed to minimize the dependency
     * of the result on floating-point roundoff errors. A typical
     * choice for D is 100 (in which case N is the percentage trimmed)
     * or 1000 (so that 125, for example, will correspond to 12.5%).
     * <P>
     * If the data set length is not an integral multiple of the
     * number of bins, The trimmed means are computed given offsets
     * from the initial index (0) and ending index (data.length),
     * repeated twice to bracket the desired value.  An esimate is
     * then made by interpolation.
     * @param N the numererator of the ratio N/D representing the
     *        fraction of the data set trimmed from both ends
     * @param D the denominator of the ratio N/D  representing the
     *        fraction of the data set trimmed from both ends
     * @param data the data whose trimmed mean is to be computed
     * @return the trimmed mean of the data
     * @exception IllegalArgumentException if N or D is not a positive
     *            integer or the length of the array is zero
     * @exception NullPointerException the last argument was null
     */
    public static double trimmedMean(int N, int D, double[] data) {
	if (N < 1 || D < 1) {
	    throw new IllegalArgumentException(errorMsg("argsNotPositive"));
	}
	if (data.length == 0) {
	    throw new IllegalArgumentException(errorMsg("emptyDataset"));
	}
	int gcd = MathOps.gcd(N,D);
	N /= gcd;
	D /= gcd;
	data = data.clone();
	Arrays.sort(data);
	boolean exact = (((data.length*(long)N) % D) == 0);
	BasicStats stats = new BasicStats.Population();
	if (exact) {
	    int min = (int)((((long)data.length)*((long)N))/ D);
	    int max = data.length - min;
	    for (int j = min; j < max; j++) {
		stats.add(data[j]);
	    }
	    return stats.getMean();
	} else {
	    int min1 = (int)((((long)data.length)*((long)N))/ D);
	    int max1 = data.length - min1;
	    for (int j = min1; j < max1; j++) {
		stats.add(data[j]);
	    }
	    double mean1 = stats.getMean();
	    stats = new BasicStats.Population();
	    int min2 = min1 + 1;
	    int max2 = data.length - min2;
	    for (int j = min2; j < max2; j++) {
		stats.add(data[j]);
	    }
	    double mean2 = stats.getMean();
	    // double t1 = (((long)data.length)*((long)N)) / ((double)D);
	    long r = (((long)data.length)*((long)N) - ((long)min1)*((long)D));
	    double t = r / ((double)D);
	    // t1 = t1 - Math.floor(t1);
	    // System.out.println("t = " + t + ", t1 = " + t1);
	    if (t < 0.0) t = 0.0;
	    if (t > 1.0) t = 1.0;
	    double mean = mean2*t + mean1*(1.0 - t);
	    return mean;
	}
    }


    /**
     * Compute the median of a data set.
     * @param data the data set
     * @return the median of the values stored in argument array
     * @exception IllegalArgumentException if the length of the array is zero
     * @exception NullPointerException the last argument was null.
     */
    public static double median(double[] data) {
	if (data.length == 0) {
	    throw new IllegalArgumentException(errorMsg("emptyDataset"));
	}
	data = data.clone();
	Arrays.sort(data);
	return (data.length %2 == 1)? data[data.length/2]:
	    (data[data.length/2] + data[data.length/2 - 1])/2.0;

    }

}
//  LocalWords:  exbundle addsComplete datasetTooSmall getCorrection
//  LocalWords:  IllegalStateException
