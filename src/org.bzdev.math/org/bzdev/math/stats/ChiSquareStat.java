package org.bzdev.math.stats;
import org.bzdev.math.*;
import java.util.Arrays;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class representing a ChiSquare statistic.
 * This class supports two forms for &Chi;<sup>2</sup> statistics.
 * <UL>
 *  <LI> &Chi;<sup>2</sup> =
 *   &sum;<sub>i</sub> (x<sub>i</sub> - E<sub>i</sub>)<sup>2</sup>/E<sub>i</sub>.
 *   This form is appropriate when the data points x<sub>i</sub> are counts
 *   where there is an expected value E<sub>i</sub>.
 *  <LI> &Chi;<sup>2</sup> = 
 *   &sum;<sub>i</sub> (x<sub>i</sub> -E<sub>i</sub>)<sup>2</sup>/&sigma;<sub>i</sub><sup>2</sup>.
 *   This form is appropriate when the data points x<sub>i</sub> are
 *   measurements with a standard error &sigma;<sub>i</sub> and a true
 *   or expected value E<sub>i</sub>.
 * </UL>
 *   As a general rule, only one of these forms would be used.
 */
public class ChiSquareStat extends Statistic {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    // Use an adder to sum the chi square terms to make the
    // sum numerically stable. The value for chi square will
    // be state.total.
    private Adder.Kahan adder = new Adder.Kahan();
    private Adder.Kahan.State state = adder.getState();

    private boolean frozen = false;

    private double[] sigmas;

    /**
     * Disallow additional data.
     *
     */
    public void freeze() {
	frozen = true;
    }

    /**
     * Determine if this statistic is frozen.
     * @return true if no more data can be added; false otherwise
     */
    public boolean isFrozen() {
	return frozen;
    }

    private long n;
    private long constraints;

    /**
     * Set the constraint count.
     * When the constraint count is set to a specific value, the
     * the number of degrees of freedom will be equal to
     * {@link #size() size()}-{@link #getConstraints() getConstraints()}.
     * The constraint count may be modified if
     * {@link #setDegreesOfFreedom(long)} is called.
     * @param constraints the number of constraints
     * @exception IllegalArgumentException the argument was out of range
     * @exception IllegalStateException this method may not be called given
     *            the constructor used
     */
    public void setConstraints(long constraints)
	throws IllegalArgumentException, IllegalStateException
    {
	if (frozen) throw new IllegalStateException
			(errorMsg("notAllowedC", "setConstraints"));
	if (constraints < 0) {
	    throw new IllegalArgumentException
		(errorMsg("constraintsNegative", constraints));
	}
	this.constraints = constraints;
    }

    /**
     * Get the constraint count.
     * @return the constraint count
     */
    public long getConstraints() {
	return constraints;
    }

    /**
     * Get the number of values used to compute this statistic.
     * @return the number of values
     */
    public long size() {
	return n;
    }

   /**
     * Get the number of degrees of freedom for this statistic.
     * @return the number of degrees of freedom.
     * @exception IllegalStateException more data points must be added
     *            before this method is called
     */
    public long getDegreesOfFreedom() {
	long result = n - constraints;
	if (result < 0)
	    throw new IllegalStateException(errorMsg("datasetTooSmall", n));
	return result;
    }


    /**
     * Set the number of degrees of freedom
     * The argument is the number of degrees of freedom appropriate
     * for the value currently returned by {@link #size()}. If N additional
     * entries are added, the number of degrees of freedom will increase
     * by N.  If this behavior is not appropriate, this method must be
     * called when all entries are added.  Calling this method may alter
     * the constraint count.
     * @param degreesOfFreedom the number of degrees of freedom
     * @exception IllegalArgumentException the argument was out of range
     * @exception IllegalStateException this method may not be called given
     *            the constructor used
     */
    public void setDegreesOfFreedom(long degreesOfFreedom)
	throws IllegalArgumentException, IllegalStateException
    {
	if (degreesOfFreedom < 1)
	    throw new IllegalArgumentException
		(errorMsg("notPositiveDOF", degreesOfFreedom));
	if (degreesOfFreedom > n)
	    throw new IllegalArgumentException
		(errorMsg("degreesOfFreedom", degreesOfFreedom));
	if (frozen) throw new IllegalStateException
			(errorMsg("notAllowedC", "setDegreesOfFreedom"));
	constraints = (long)(n - degreesOfFreedom);
    }

    /**
     * Constructor.
     * A constraint indicates the number of parameters used to
     * determine the true or expected values that are determined
     * by the data.
     */
    public ChiSquareStat() {}

    /**
     * Constructor given an explicit &Chi;<sup>2</sup> value.
     * The number of degrees of freedom is (n - constraints).
     * If negative, additional data will have to be added before
     * the methods returning the probability distrubition or the number
     * of degrees of freedom can be used.
     * <P>
     * This constructor is intended for analysis purposes or for cases
     * in which one might wish to save and restore previously computed
     * statistics.
     * @param chisq the &chi;<sup>2</sup> value
     * @param n the size of the data set
     * @param constraints the number of constraints.
     *
     */
    public ChiSquareStat(double chisq, long n, long constraints) {
	state.total = chisq;
	this.n = n;
	this.constraints = constraints;
    }

    /**
     * Constructor given a data-point array an array of the corresponding
     * true (or expected) values for the data points.
     * @param data an array giving a collection of data points
     * @param expected the actual or expected values corresponding to each
     *        data point
     *
     */
    public ChiSquareStat(double[] data, double[] expected) {
	for (int i = 0; i < data.length; i++) {
	    double v = (data[i]-expected[i]);
	    double y = v*v/Math.abs(expected[i]);
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	n = data.length;
	sigmas = expected.clone();
    }

    /**
     * Constructor given a data-point array and a corresponding true (or
     * expected) value for the data points.
     * @param data an array giving a collection of data points
     * @param expected the actual or expected values corresponding to each
     *        data point
     *
     */
    public ChiSquareStat(double[] data, double expected) {
	for (int i = 0; i < data.length; i++) {
	    double v = (data[i]-expected);
	    double y = v*v/Math.abs(expected);
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	n = data.length;
	sigmas = new double[data.length];
	Arrays.fill(sigmas, expected);
    }

    /**
     * Contructor given a data-point array, an array of the
     * corresponding true (or expected) values, and an array of the
     * corresponding standard deviation for each data point.
     * @param data an array giving a collection of data points
     * @param expected the actual or expected values corresponding to each
     *        data point
     * @param sigma the standard error for each data point
     */
    public ChiSquareStat(double[] data, double[] expected, double[] sigma) {
	for (int i = 0; i < data.length; i++) {
	    double v = (data[i]-expected[i]);
	    double y = v*v/(sigma[i]*sigma[i]);
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	n = data.length;
	sigmas = sigma.clone();
    }

    /**
     * Contructor given a data-point array, an array of the
     * corresponding true (or expected values), and the standard
     * deviation for the data points.
     * @param data an array giving a collection of data points
     * @param expected the actual or expected values corresponding to each
     *        data point
     * @param sigma the standard error for each data point
     */
    public ChiSquareStat(double[] data, double[] expected, double sigma) {
	for (int i = 0; i < data.length; i++) {
	    double v = (data[i]-expected[i]);
	    double y = v*v/(sigma*sigma);
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	n = data.length;
	sigmas = new double[data.length];
	Arrays.fill(sigmas, sigma);
    }

    /**
     * Contructor given a data-point array, a corresponding true
     * (or expected) value for the data points, and the standard
     * deviation for the data points.
     * @param data an array giving a collection of data points
     * @param expected the actual or expected value for each
     *        data point (the same value for all)
     * @param sigma the standard deviation for each data point (the
     *        same value for all)
     */
    public ChiSquareStat(double[] data, double expected, double sigma) {
	for (int i = 0; i < data.length; i++) {
	    double v = (data[i]-expected);
	    double y = v*v/(sigma*sigma);
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	n = data.length;
	sigmas = new double[data.length];
	Arrays.fill(sigmas, sigma);
    }


    /**
     * Constructor for categorical data.
     * This test determines if two categories are independent. Each
     * category is modeled by a fixed number of elements specified by
     * an index, and a matrix provides counts of entries that are
     * associated with a pair of elements, one from each category. One
     * category is represented by the rows of the matrix and the other
     * by its columns.
     * <P>
     * One must not use one of the "add" methods after this constructor
     * is used, nor change the number of degrees of freedom, nor set
     * the number of constraints - otherwise an exception will be thrown.
     * @param data an N by M matrix of counts
     */
    public ChiSquareStat(int[][] data) {
	int n = data.length;
	int m = data[0].length;
	long[] rtotals = new long[n];
	long[] ctotals = new long[m];
	long total = 0;
	for(int i = 0; i < n; i++) {
	    for (int j = 0; j < m; j++) {
		rtotals[i] += data[i][j];
		ctotals[j] += data[i][j];
	    }
	    total += rtotals[i];
	}

	for(int i = 0; i < n; i++) {
	    for (int j = 0; j < m; j++) {
		double value = (double)data[i][j];
		double expected = ((double)rtotals[i])*((double)ctotals[j])
		    / ((double) total);
		double tmp = (value - expected);
		double y = (tmp*tmp)/expected;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	}
	this.n = ((long)n)*((long)m);
	setDegreesOfFreedom((n-1)*(m-1));
	frozen = true;
    }

    /**
     * Add a data point.
     * @param d a data point
     * @param e the true (or expected) value for the data point
     * @exception IllegalStateException this method may not be called given
     *            the constructor used
     */
    public void add(double d, double e) throws IllegalStateException {
	if (frozen) throw new IllegalStateException
			(errorMsg("notAllowedC", "add"));
	double v = (d-e);
	double y = v*v/Math.abs(e);
	double t = state.total + y;
	state.c = (t - state.total) - y;
	state.total = t;
	n++;
	sigmas = null;
    }

    /**
     * Add a data point with a specified standard error.
     * @param d a data point
     * @param e the true (or expected) value for the data point
     * @param sigma the standard error for the data point
     * @exception IllegalStateException this method may not be called given
     *            the constructor used
     */
    public void add(double d, double e, double sigma)
	throws IllegalStateException
    {
	if (frozen) throw new IllegalStateException
			(errorMsg("notAllowedC", "add"));
	double v = (d-e);
	double y = v*v/(sigma*sigma);
	double t = state.total + y;
	state.c = (t - state.total) - y;
	state.total = t;
	n++;
	sigmas = null;
    }

    @Override
    public double getValue() throws IllegalStateException {
	if (n <= 0) throw new IllegalStateException(errorMsg("noData"));
	return state.total;
    }

    @Override
    public ProbDistribution getDistribution() throws IllegalStateException {
	if (n - constraints <= 0)
	    throw new IllegalStateException
		(errorMsg("datasetTooSmallConstraints", constraints));
	if (n - constraints > Integer.MAX_VALUE) {
	    throw new IllegalStateException
		(errorMsg("dofTooLarge"));
	}
	return new ChiSquareDistr((int)(n - constraints));
    }

    /**
     * Get a noncentral distribution for this statistic.

     * The definition of &lambda; provided by {@link ChiSquareDistr}
     * is such that, if the mean of k independent, normally
     * distributed random variables is &mu;<sub>i</sub> and these
     * random variables have variances equal to 1.0, then the
     * distribution of the sum of the squares of these random
     * variables is a noncentral &Chi;<sup>2</sup> distribution with k
     * degrees of freedom and a noncentrality parameter &lamba;. For
     * the statistic provided by this class, each of these random
     * variables is a ratio of a difference and a quantity that is
     * essential a standard deviation.  The difference is the
     * difference between a value X<sub>i</sub> and an expected value
     * E<sub>i</sub>, and the standard deviation is the standard deviation
     * for X<sub>i</sub>. The &Chi;<sup>2</sub> distribution assumes that
     * these ratios have a normal distribution with unit variances and a
     * mean of 0.  For a noncentral &Chi;<sup>2</sup> distribution, the
     * ratios are assumed to have a non-zero mean. The noncentrality
     * parameter &lambda; is defined by
     * &lambda; = &sum;<sub>i=1</sub><sup>k</sup>&mu;<sub>i</sub>, where
     * &mu;<sub>i</sub> is the mean value of the i<sup>th</sup> ratio.
     * <P>
     * If there are k degrees of freedom, but additional terms in the
     * &Chi;<sup>2</sup> sum, it is possible to define k variables
     * z<sub>i</sub> so that &Chi;</sup>2</sup> = z<sub>1</sub><sup>2</sup>
     *  + ... + z<sub>k</sub><sup>2</sup>, where the mean of each z<sub>i</sub>
     * is zero and its standard deviation is 1. The same value for &lamba;
     * will be obtained.  All the x<sub>i</sub> variables must satisify
     * (n-k) linear equations that act as constraints, where n is the total
     * number of terms.
     * @param lambda the noncentrality parameter
     * @return the (noncentral) probability distribution
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     */
    public ProbDistribution getDistribution(double lambda)
	throws IllegalArgumentException, IllegalStateException
    {
	if (lambda == 0.0) {
	    return getDistribution();
	}
	if (lambda < 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("lambdaNegative", lambda));
	}
	if (n - constraints <= 0)
	    throw new IllegalStateException
		(errorMsg("datasetTooSmallConstraints", constraints));
	if (n - constraints > Integer.MAX_VALUE) {
	    throw new IllegalStateException
		(errorMsg("dofTooLarge"));
	}
	return new ChiSquareDistr((int)(n - constraints), lambda);
    }

    /**
     * Get the noncentrality parameter appropriate for this statistic.
     * The argument is equal to the difference E'<sub>i</sub> - E<sub>i</sub>
     * where E'<sub>i</sub> is the actual value and E<sub>i</sub> is
     * the value used when the contructor was called.  This offset is
     * assumed be the same for all i. If we set Y<sub>i</sub> =
     * &delta;<sub>i</sub> = E'<sub>i</sub> - E<sub>i</sub>, then
     * (X<sub>i</sub>-E<sub>i</sub> - &delta;<sub>i</sub> + &delta;<sub>i</sub>)
     * / &sigma;<sub>i</sub>
     * = (X<sub>i</sub> - E'<sub>i</sub> + &delta;<sub>i</sub>)
     * / &sigma;<sub>i</sub>.  The standard deviation of X<sub>i</sub> is
     * &sigma;<sub>i</sub> and hence the standard deviation  of Y<sub>i</sub>
     * is also &sigma;<sub>i</sub>. Consequently, the variance of
     * (Y<sub>i</sub> + &delta;<sub>i</sub>)/&sigma;<sub>i</sub> is 1.0
     * and the corresponding mean value
     * &mu;<sub>i</sub> = &delta;<sub>i</sub> / &sigma;<sub>i</sub>.
     * The distribution of
     * &sum;<sub>i=1</sub><sup>k</sup>
     * (Y<sub>i</sub> + &delta;<sub>i</sub>)/&sigma;<sub>i</sub>
     * is thus a noncentral &Chi;<sup>2</sup> with a non-centrality parameter
     * &lambda; = &sum;<sub>i=1</sub><sup>k</sup> &mu;<sub>i</sub>.
     * <P>
     * This method computes &lambda; for the case were the differences
     * &delta;<sub>i</sub> are identical.  If the number of variables is
     * large than the number of degrees of freedom, the user must
     * ensure that constraining all the &delta;<sub>i</sub> values so
     * that they are equal does not violate the constraint equations
     * that reduce the number of degrees of freedom.
     *
     * @param diff the difference from their expected values for the
     *        mean value of the random variables used in the sum of
     *        squares
     * @exception IllegalStateException additional data was added after
     *            the constructor was called or the number of constraints
     *            was not zero
     */
    @Override
    public double getNCParameter(double diff) {
	if (sigmas == null) {
	    throw new IllegalStateException(errorMsg("wrongNCState"));
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < n; i++) {
	    double tmp = diff/sigmas[i];
	    double y = (tmp*tmp) - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	return state.total;
    }

    /**
     * Get the noncentrality parameter appropriate for this statistic
     * given multiple differences.
     * The i<sup>th</sup>  argument is equal to the difference
     * E'<sub>i</sub> - E<sub>i</sub> where E'<sub>i</sub> is the
     * desired value and E<sub>i</sub> is the value used when the
     * constructor was called.
     * <P>
     * If the number of variables is large than the number of degrees
     * of freedom, the user must ensure that the choice of arguments
     * is consistent with the constraint equations that reduce the
     * number of degrees of freedom.
     * <P>
     * If there is only a single argument or an array of length 1,
     * the argument or array value will be passed to
     * {@link #getNCParameter(double)}.
     * @param diffs the difference from their expected values for the
     *        mean values of the random variables used in the sum of
     *        squares
     * @exception IllegalStateException additional data was added after
     *            the constructor was called or the number of constraints
     *            was not zero
     */
    @Override
    public double getNCParameter(double... diffs) {
	if (sigmas == null) {
	    throw new IllegalStateException(errorMsg("wrongNCState"));
	}
	if (diffs.length == 1) {
	    return getNCParameter(diffs[1]);
	}
	if (diffs.length != n) {
	    throw new IllegalArgumentException
		(errorMsg("wrongArgNumber", diffs.length, n));
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < n; i++) {
	    double tmp = diffs[i]/sigmas[i];
	    double y = (tmp*tmp) - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	return state.total;
    }
 }

//  LocalWords:  exbundle ChiSquare
