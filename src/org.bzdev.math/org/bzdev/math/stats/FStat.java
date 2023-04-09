package org.bzdev.math.stats;
import org.bzdev.math.*;
import java.util.Arrays;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class representing an F-test statistic.
 * An F-test statistic is based on the F distribution.
 * An F distribution is the distribution of the random variable
 * (X<sub>1</sub>/&nu;<sub>1</sub>) / (X<sub>2</sub>/&nu;<sub>2</sub>),
 * where X<sub>1</sub> and X<sub>2</sub> are independent random variables
 * with a &Chi;<sup>2</sup> distribution with &nu;<sub>1</sub> and
 * &nu;<sub>2</sub> degrees of freedom respectively.
 * <P>
 * For noncentral F-distributions, the random variable X<sub>1</sub> defined
 * above has a noncentral &Chi;<sup>2</sup> distribution with a
 * noncentrality parameter &lambda;.  This class provides methods to
 * compute the noncentrality parameter.
 */
public abstract class FStat extends Statistic {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    private long nu1;
    private long nu2;

    private long sz1;

    /**
     * Get the degrees of freedom for the numerator.
     * @return the degrees of freedom
     */
    public long getDegreesOfFreedom1() {
	return nu1;
    }

    /**
     * Get the degrees of freedom for the denominator.
     * @return the degrees of freedom
     */
    public long getDegreesOfFreedom2() {
	return nu2;
    }

    /**
     * Set the degrees of freedom.
     * @param nu1 the number of degrees of freedom for the numerator
     * @param nu2 the number of degrees of freedom for the denominator.
     */
    protected void setDegreesOfFreedom(long nu1, long nu2) {
	this.nu1 = nu1;
	this.nu2 = nu2;
    }

    /**
     * Set the data-set size.
     * @param sz1 the data-set size.
     */
    protected void setSize(long sz1) {
	this.sz1 = sz1;
    }

    /**
     * Constructor.
     */
    protected FStat() {
    }

    /**
     * Constructor specifying degrees of freedom and the dataset size
     * @param nu1 the number of degrees of freedom for the numerator
     * @param nu2 the number of degrees of freedom for the denominator.
     * @param sz1 the data-set size.
     */
    protected FStat(long nu1, long nu2, long sz1) {
	this.nu1 = nu1;
	this.nu2 = nu2;
	this.sz1 = sz1;
    }

    @Override
    public ProbDistribution getDistribution() throws IllegalStateException {
	if (nu1 <= 0)
	    throw new IllegalStateException
		(errorMsg("notPositiveDOF", nu1));
	if (nu1 > Integer.MAX_VALUE)
	    throw new IllegalStateException
		(errorMsg("degreesOfFreedom", nu1));
	if (nu2 <= 0)
	    throw new IllegalStateException
		(errorMsg("notPositiveDOF", nu2));
	if (nu2 > Integer.MAX_VALUE)
	    throw new IllegalStateException
		(errorMsg("degreesOfFreedom", nu1));
	return new FDistr((int)nu1, (int)nu2);
    }

    /**
     * Get a noncentral distribution for this statistic.

     * The definition of &lambda; provided by {@link FDistr}
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
     * for X<sub>i</sub>. The &Chi;<sup>2</sup> distribution assumes that
     * these ratios have a normal distribution with unit variances and a
     * mean of 0.  For a noncentral &Chi;<sup>2</sup> distribution, the
     * ratios are assumed to have a non-zero mean. The noncentrality
     * parameter &lambda; is defined by
     * &lambda; = &sum;<sub>i=1</sub><sup>k</sup>&mu;<sub>i</sub>, where
     * &mu;<sub>i</sub> is the mean value of the i<sup>th</sup> ratio.
     * <P>
     * If there are k degrees of freedom, but additional terms in the
     * &Chi;</sup>2</sup> sum, it is possible to define k variables
     * z<sub>i</sub> so that &Chi;</sup>2</sup> = z<sub>1</sub><sup>2</sup>
     *  + ... + z<sub>k</sub><sup>2</sup>, where the mean of each z<sub>i</sub>
     * is zero and its standard deviation is 1. The same value for &lamba;
     * will be obtained.  All the x<sub>i</sub> variables must satisfy
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
    @Override
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
	return new FDistr((int)nu1, (int)nu2, lambda);
    }

    /**
     * Get the noncentrality parameter appropriate for this statistic.
     * The argument is equal to the difference E'<sub>i</sub> - E<sub>i</sub>
     * where E'<sub>i</sub> is the actual value and E<sub>i</sub> is
     * the value used when the constructor was called.  This offset is
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
     * larger  than the number of degrees of freedom, the user must
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
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < sz1; i++) {
	    double tmp = diff;
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
	if (diffs.length == 1) {
	    return getNCParameter(diffs[1]);
	}
	if (diffs.length != sz1) {
	    throw new IllegalArgumentException
		(errorMsg("wrongArgNumber", diffs.length, sz1));
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < sz1; i++) {
	    double tmp = diffs[i];
	    double y = (tmp*tmp) - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	return state.total;
    }
 }

//  LocalWords:  exbundle noncentral noncentrality sz dataset FDistr
//  LocalWords:  notPositiveDOF degreesOfFreedom lamba th
//  LocalWords:  IllegalArgumentException IllegalStateException
//  LocalWords:  lambdaNegative getNCParameter wrongArgNumber
