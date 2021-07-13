package org.bzdev.math.stats;
import org.bzdev.math.LeastSquaresFit;
//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Base class for classes statistics for Student's t-test.
 * Each subclass provides a different statistic. A description of the
 * statistic for each subclass is provided in the documentation for
 * that subclass. For a test of the differences between two data sets
 * with unequal variances, use {@link WelchsTStat}.
 * <P>
 * The methods this class implements allow one to determine the number
 * of degrees of freedom and the probability distribution for this
 * statistic.  For a t-test, this distribution depends only on the
 * number of degrees of freedom.
 * <P>
 * As described by the documentation for {@link StudentsTDistr},
 * the Student's t distribution is that of the random variable
 * T = Z sqrt(&nu;/V) where
 * <UL>
 *   <LI> Z is a Gaussian (or normal) random variable with an expected
 *        value of 0 and a mean of 1.
 *   <LI> V is a random variable with a &Chi;<sup>2</sup> distribution with
 *        &nu; degrees of freedom.
 *   <LI> Z and V are independent random variables.
 * </UL>
 * In a typical case, V will be a sample standard deviation
 * divided by the square of the corresponding population standard deviation.
 * Similarly, Z will be the sample mean of a random number for a
 * sample size of n divided by &sigma;/sqrt(n) where &sigma; is the
 * the random number's population standard deviation.
 */
public abstract class StudentsTStat extends Statistic {

    private int degreesOfFreedom = 0;

    /**
     * Set the number of degrees of freedom for this instance.
     * This must be called by a subclass whenever the statistics
     * are updated.
     * @param d the degrees of freedom; 0 no data is available
     */
    protected void setDegreesOfFreedom(int d) {
	degreesOfFreedom = d;
    }

    public int getDegreesOfFreedom() {
	return degreesOfFreedom;
    }

    @Override
    public ProbDistribution getDistribution() {
	return new StudentsTDistr(degreesOfFreedom);
    }

    /**
     * Get a noncentral distribution for this statistic.
     * The definition of &mu; provided by {@link StudentsTDistr},
     * has to be applied to specific cases.

     * @param mu the noncentrality parameter
     * @return the probability distribution
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     */
    public ProbDistribution getDistribution(double mu) {
	return new StudentsTDistr(degreesOfFreedom, mu);
    }

    /**
     * Get the noncentrality parameter given a difference in mean values.
     * A Student's t-test can typically be written as
     * T = ((<span style="text-decoration: overline">X</span>
     * - &mu;<sub>0</sub>)/(&sigma;/sqrt(n)))/((sqrt(S/&sigma;<sup>2</sup>))/sqrt(&nu;))
     * where &nu; is the number of degrees of freedom, &sigma; is the population
     * standard deviation of a random variable for which
     * <span style="text-decoration: overline">X</span> is the sample mean of
     * X, &mu;<sub>0</sub> is the population mean of X,
     * n is the sample size, and S is a sum of
     * squares.  Note that sqrt(S/(&sigma;<sup>2</sup>&nu;) will be the
     * sample standard deviation for a suitable choice of &nu;
     * <P>
     * If we set &theta; = &mu;<sub>1</sub> - &mu;<sub>0</sub>, we can
     * express T as
     * T = ((<span style="text-decoration: overline">X</span>
     * - &mu;<sub>0</sub> - &theta; + &theta;)/(&sigma;/sqrt(n))) /
     * ((sqrt(S/&sigma;<sup>2</sup>))/sqrt(&nu;)) or
     * T = ((<span style="text-decoration: overline">X</span>
     * - &mu;<sub>1</sub> + &theta;)/(&sigma;/sqrt(n))) /
     * ((sqrt(S/&sigma;<sup>2</sup>))/sqrt(&nu;))
     * If we set Z = (<span style="text-decoration: overline">X</span>
     * - &mu;<sub>1</sub>)/(&sigma;/sqrt(n)) and
     * &mu; = &theta;/(&sigma;/sqrt(n)), then T can be written as
     * (Z + &mu;)/sqrt(V/&nu;).  If the actual mean is &mu;<sub>1</sub>,
     * then Z has a normal distribution with a mean of zero and a
     * variance of 1. Meanwhile V has a &Chi;<sup>2</sup> distribution
     * with &nu; degrees of freedom, and Z and V are
     * independent. Consequently, the random variable
     * (Z + &mu;)/sqrt(V/&nu;) has a noncentral t distribution characterized
     * by the number of degrees of freedom &nu; and the noncentrality
     * parameter &mu;
     * <P>
     * For citations for specific cases, please see
     * <UL>
     *   <LI><A href="http://www.biostat.jhsph.edu/~iruczins/teaching/140.615/notes/n.ssp.pdf">
     *        Sample Size &amp; Power Calculations</A> and
     *   <A href="http://ageconsearch.umn.edu/bitstream/116234/2/sjart_st0062.pdf">
     *  Sample size and power calculations using the noncentral t-distribution</A>
     *  for two-sample case.
     *    <LI><A href="http://www.real-statistics.com/students-t-distribution/statistical-power-of-the-t-tests/">
     *        Statistical power of the t-tests</A> for the one-sample case.
     *    <LI>
     * </UL>
     * @param diff the difference of the H1 mean value and the H0 mean value.
     */
    public abstract double getNCParameter(double diff);

    /**
     * Statistic for Student's t-test for the comparison of two
     * means given independent samples with the same variance.
     * The test determines if the difference between the mean values
     * for two data sets is statistically significant.
     * <P>
     * "<A href="http://www.chem.uoa.gr/applets/AppletTtest/Appl_Ttest2.html">
     * Student's t-test: Comparison of two means<A>" has a
     * description of the test.
     * If the data sets are X<sub>1</sub> and X<sub>2</sub>, then the statistic
     * is t = (<span style="text-decoration: overline">X</span><sub>1</sub>
     * - <span style="text-decoration: overline">X</span><sub>2</sub>)
     * / (s<sub>X<sub>1</sub>X<sub>2</sub></sub>
     * sqrt(1/n<sub>1</sub> + 1/n<sub>2</sub>) where
     * <UL>
     *  <LI>s<sub>X<sub>1</sub>X<sub>2</sub></sub> =
     *      sqrt(((n<sub>1</sub>-1)s<sub>X<sub>1</sub></sub><sup>2</sup>
     *      + (n<sub>2</sub>-1)s<sub>X<sub>2</sub></sub><sup>2</sup>)
     *      / (n<sub>1</sub>+n<sub>2</sub>-2)).
     *  <LI><span style="text-decoration: overline">X</span><sub>1</sub> is the
     *      mean for the data set X<sub>1</sub>.
     *  <LI><span style="text-decoration: overline">X</span><sub>2</sub> is the
     *      mean for the data set X<sub>2</sub>.
     *  <LI> n<sub>1</sub> is the size of data set X<sub>1</sub>.
     *  <LI> n<sub>2</sub> is the size of data set X<sub>2</sub>.
     *  <LI> s<sub>X<sub>1</sub></sub> is the sample standard deviation for
     *       data set X<sub>1</sub>.
     *  <LI> s<sub>X<sub>2</sub></sub> is the sample standard deviation for
     *       data set X<sub>2</sub>.
     * </UL>
     */
    public static class Mean2 extends StudentsTStat {
	
	BasicStats.Sample stats1;
	BasicStats.Sample stats2;

	/**
	 * Constructor.
	 * To add data to each of the two data sets, the methods
	 * {@link #add1(double)} and {@link #add2(double)} must be used.
	 */
	public Mean2() {
	    super();
	    stats1 = new BasicStats.Sample();
	    stats2 = new BasicStats.Sample();
	}
	
	/**
	 * Constructor given a description of two data sets.
	 * @param mean1 the mean value of the first data set
	 * @param variance1 the sample variance of the first data set
	 * @param n1 the size of the first data set
	 * @param mean2 the mean value of the second data set
	 * @param variance2 the sample variance of the second data set
	 * @param n2 the size of the second data set
	 */
	public Mean2(double mean1, double variance1, long n1,
		     double mean2, double variance2, long n2)
	{
	    super();
	    stats1 = new BasicStats.Sample(mean1, variance1, n1);
	    stats2 = new BasicStats.Sample(mean2, variance2, n2);
	    int d = (int)(n1+n2-2);
	    setDegreesOfFreedom(d);
	}

	/**
	 * Constructor given two data sets.
	 * @param a values to add to data set A
	 * @param b values to add to data set B
	 */
	public Mean2(double[] a, double[] b) {
	    super();
	    stats1 = (a == null)? new BasicStats.Sample():
		new BasicStats.Sample(a);
	    stats2 = (b == null)? new BasicStats.Sample():
		new BasicStats.Sample(b);
	    long n1 = stats1.size();
	    long n2 = stats2.size();
	    int d = (int)(n1+n2-2);
	    setDegreesOfFreedom(d);
	}

	/**
	 * Add an entry to data set A.
	 * @param x1 the value to add
	 */
	public void add1(double x1) {
	    stats1.add(x1);
	    long n1 = stats1.size();
	    long n2 = stats2.size();
	    int d = (int)(n1+n2-2);
	    setDegreesOfFreedom(d);
	}

	/**
	 * Add an entry to data set B.
	 * @param x2 the value to add
	 */
	public void add2(double x2) {
	    stats2.add(x2);
	    long n1 = stats1.size();
	    long n2 = stats2.size();
	    int d = (int)(n1+n2-2);
	    setDegreesOfFreedom(d);
	}

	/**
	 * Get the mean for data set A.
	 * @return the mean for data set A
	 */
	public double getMean1() {return stats1.getMean();}

	/**
	 * Get the sample standard deviation for data set A.
	 * @return the standard deviation for data set A
	 */
	public double getSDev1() {return stats1.getSDev();}

	/**
	 * Get the mean for data set B.
	 * @return an object that records the mean and standard deviation
	 *         for data set B
	 */
	public double getMean2() {return stats2.getMean();}

	/**
	 * Get the sample standard deviation for data set B.
	 * @return the sample standard deviation for data set B
	 */
	public double getSDev2() {return stats2.getSDev();}

	@Override
	public double getValue() {
	    long n1 = stats1.size();
	    long n2 = stats2.size();
	    double va = stats1.getVariance();
	    double vb = stats2.getVariance();
	    double sab = Math.sqrt(((n1-1)*va + (n2-1)*vb)/(n1+n2-2));
	    return (stats1.getMean() - stats2.getMean())
		/ (sab * Math.sqrt((1.0/n1) + (1.0/n2)));
	}

	@Override
	 public double getNCParameter(double diff) {
	    long n1 = stats1.size();
	    long n2 = stats2.size();
	    double va = stats1.getVariance();
	    double vb = stats2.getVariance();
	    double sab = Math.sqrt(((n1-1)*va + (n2-1)*vb)/(n1+n2-2));
	    double tmp = Math.sqrt(1.0/n1 + 1.0/n2);
	    return diff / (sab * tmp);
	}
    }

    /**
     * Student's t-test for determining if the mean value of a data set
     * has a specified value. For a data set X, the statistic is
     * t = (<span style="text-decoration: overline">X</span> - &mu;<sub>0</sub>)
     * / (s<sub>X</sub> / sqrt(n))
     * where
     * <UL>
     *   <LI> <span style="text-decoration: overline">X</span> is the mean
     *        value of the data set X.
     *   <LI> &mu;<sub>0</sub> is the value against which the mean of X is
     *        to be tested.
     *   <LI> s<sub>X</sub> is the sample standard deviation of X.
     *   <LI> n is the size of the data set X.
     * </UL>
     */
    public static class Mean1 extends StudentsTStat {

	private BasicStats.Sample stats;
	double mu;

	/**
	 * Constructor.
	 * @param mu the specified mean value 
	 */
	public Mean1(double mu) {
	    this.mu = mu;
	    stats = new BasicStats.Sample();
	}

	/**
	 * Constructor given a mean value to test against and a
	 * description of a data set.
	 * @param mu the mean value to test against
	 * @param mean the mean value of the data set
	 * @param variance the sample variance of the data set
	 * @param n the size of the data set
	 */
	public Mean1(double mu, double mean, double variance, long n) {
	    this.mu = mu;
	    stats = new BasicStats.Sample(mean, variance, n);
	    if (n < 2) {
		throw new IllegalArgumentException
		    (errorMsg("datasetTooSmall", n));
	    }
	    if ((n-1) > Integer.MAX_VALUE) {
		throw new IllegalArgumentException
		    (errorMsg("datasetTooLarge", n));
	    }
	    setDegreesOfFreedom((int)(n-1));
	}

	/**
	 * Constructor providing a data set.
	 * @param mu the specified mean value 
	 * @param array the data whose mean is to be tested
	 */
	public Mean1(double mu, double[] array) {
	    this.mu = mu;
	    stats = new BasicStats.Sample(array);
	    setDegreesOfFreedom(array.length-1);
	}
	
	/**
	 * Add additional data.
	 * @param y an additional data point
	 */
	public void add(double y) {
	    stats.add(y);
	    long n = stats.size();
	    setDegreesOfFreedom((int)(n-1));
	}

	/**
	 * Get the mean value of the data set.
	 * @return the mean value
	 */
	public double getMean() {return stats.getMean();}

	/**
	 * Get the sample standard deviation of the data set.
	 * @return the sample standard deviation
	 */
	public double getSDev() {return stats.getSDev();}

	@Override
	public double getValue() {
	    return (stats.getMean() - mu) * Math.sqrt(stats.size())
		/ stats.getSDev();
	}

	@Override
	public double getNCParameter(double diff) {
	    return Math.sqrt(stats.size())*diff / stats.getSDev();
	}
    }

    /**
     * Class providing Student's t-test to determine if the slope of 
     * a linear regression has a specific value.
     * Using a value of 0 for the specified slope tests whether the x
     * and y values are independent of each other.
     */
    public static class Slope extends StudentsTStat {
	double beta0;

	double getSpecifiedSlope() {return beta0;}

	double tscore;
	double ssr;
	BasicStats.Population sx;

	LeastSquaresFit.Polynomial lsf;
	
	/**
	 * Get the least squares fit (a polynomial fit of degree 1)
	 * @return the least squares fit for this statistic's data set;
	 *         null if not available
	 */
	public LeastSquaresFit.Polynomial getFit() {return lsf;}

	/**
	 * Constructor.
	 * The x and y values must be arrays of equal lengths and
	 * each data point consists of an x and y value with the
	 * same index.
	 * @param beta0 the specified slope
	 * @param x the X values
	 * @param y the Y values
	 */
	public Slope(double beta0, double[] x, double[] y) {
	    this.beta0 = beta0;
	    lsf = new LeastSquaresFit.Polynomial(1, x, y);
	    double[] params = lsf.getParameters();
	    ssr = LeastSquaresFit.sumOfSquares(lsf, x, y);
	    sx = new BasicStats.Population(x);
	    int n = x.length;
	    setDegreesOfFreedom(n-2);
	    tscore = (params[1] - beta0)*Math.sqrt(n-2.0)
		/ Math.sqrt(ssr / (n*sx.getVariance()));
	}

	/**
	 * Constructor given a description of the statistic.
	 * @param beta0 the specified slope
	 * @param beta  the slope determined by a least squares fit
	 * @param variance the population variance of the x values used
	 *        in the least-squares fit.
	 * @param ssq the sum of the squares of the residuals
	 * @param n the number of points used to create the least squares fit
	 */
	public Slope(double beta0, double beta,
		     double variance, double ssq,
		     int n)
	{
	    setDegreesOfFreedom(n-2);
	    // mean value is never used, so we just set it to zero.
	    sx = new BasicStats.Population(0.0, variance, n);
	    ssr = ssq;
	    tscore = (beta - beta0)*Math.sqrt(n-2.0)
		/ Math.sqrt(ssq / n*variance);
	}

	@Override
	public double getValue() {return tscore;}

	@Override
	public double getNCParameter(double diff) {
	    double variance = sx.getVariance();
	    double n = sx.size();
	    double s = Math.sqrt(ssr/(n*variance));
	    return diff/s;
	}

    }

    /**
     * Student's t statistic for dependent paired differences.
     * For a data set x<sub>1i</sub> and a data set x<sub>2i</sub>
     * the difference d<sub>i</sub> = x<sub>2i</sub>-x<sub>1i</sub>
     * will be computed and added to the statistic.
     * The distributions from which each pair of values is taken are
     * assumed to have the same variances. The statistic is
     *  t = (<span style="text-decoration: overline">X</span><sub>D</sub>
     *  - &mu;<sub>0</sub>) / (s<sub>D</sub>/sqrt(n))  where
     * <UL>
     *    <LI> <span style="text-decoration: overline">X</span><sub>D</sub> is
     *         the mean value of a data set of differences D.
     *    <LI> s<sub>D</sub> is the sample standard deviation for D.
     *    <LI> n is the size of the data set.
     *    <LI> &mu;<sub>0</sub> is the value against which D's mean value
     *         will be tested.
     * </UL>
     */
    public static class PairedDiff  extends StudentsTStat {
	private double mu0;

	/**
	 * Get the specified value, against which the data set's mean value
	 * will be tested.
	 * @return the specified value for the mean
	 */
	public double getSpecifiedMean() {
	    return mu0;
	}

	BasicStats.Sample stats;
	
	/**
	 * Get the mean of the data set x<sub>2i</sub> - x<sub>1i</sub>.
	 * @return the mean value of data set
	 */
	public double getMean() {return stats.getMean();}

	/**
	 * Get the sample standard deviation  of the data set
	 * x<sub>2i</sub> - x<sub>1i</sub>.
	 * @return the sample standard deviation for the data set
	 */
	public double getSDev() {return stats.getSDev();}

	/**
	 * Constructor.
	 * Data must be added by using the method {@link #add(double,double)}
	 * or {@link #add(double)}.
	 * @param mu0 the specified mean value to test against
	 */
	public PairedDiff(double mu0) {
	    this.mu0 = mu0;
	    stats = new BasicStats.Sample();
	}

	/**
	 * Constructor given a mean value to test against and a
	 * description of a data set.
	 * @param mu0 the mean value to test against
	 * @param mean the mean value of the data set
	 * @param variance the sample variance of the data set
	 * @param n the size of the data set
	 */
	public PairedDiff(double mu0, double mean, double variance, long n) {
	    this.mu0 = mu0;
	    stats = new BasicStats.Sample(mean, variance, n);
	}

	/**
	 * Constructor given paired values.
	 * For a data set x<sub>1i</sub> and a data set x<sub>2i</sub>
	 * the difference d<sub>i</sub> = x<sub>2i</sub>-x<sub>1i</sub>
	 * will be computed and added to the statistic.
	 * @param mu0 the specified mean value to test against
	 * @param x1 the first values from each of the pairs
	 * @param x2 the second values from each of the pairs.
	 */
	public PairedDiff(double mu0, double[] x1, double[] x2) {
	    this.mu0 = mu0;
	    double[] diffs = new double[x1.length];
	    for (int i = 0; i < x1.length; i++) {
		diffs[i] = x1[i]-x2[i];
	    }
	    stats = new BasicStats.Sample(diffs);
	    setDegreesOfFreedom(x1.length - 1);
	}

	/**
	 * Constructor given differences.
	 * For a data set x<sub>1i</sub> and a data set x<sub>2i</sub>
	 * the difference d<sub>i</sub> =
	 * x<sub>1i</sub>-x<sub>2i</sub>.  This constructor specifies
	 * the mean value against which the statistic tests the data
	 * and specifies an initial set of differences between paired
	 * valued.
	 * @param mu0 the specified mean value to test against
	 * @param diff the difference in values between each pair
	 */
	public PairedDiff(double mu0, double[] diff) {
	    this.mu0 = mu0;
	    stats = new BasicStats.Sample(diff);
	    setDegreesOfFreedom(diff.length-1);
	}

	/**
	 * Add paired values.
	 * The difference x2-x1 will be added.
	 * @param x1 the first paired value
	 * @param x2 the second paired value
	 */
	public void add(double x1, double x2) {
	    stats.add(x1-x2);
	    long n = stats.size();
	    int d = (int)n - 1;
	    setDegreesOfFreedom(d);
	}

	/**
	 * Add a difference.
	 * @param diff the difference
	 */
	public void add(double diff) {
	    stats.add(diff);
	    long n = stats.size();
	    int d = (int)n - 1;
	    setDegreesOfFreedom(d);
	}

	@Override
	public double getValue() {
	    long n = stats.size();
	    return (stats.getMean() - mu0)*Math.sqrt((double)n)
		/ stats.getSDev();
	}

	@Override
	public double getNCParameter(double diff) {
	    return diff* Math.sqrt(0.5*stats.size()/stats.getSDev());
	}
    }
}

//  LocalWords:  WelchsTStat StudentsTDistr sqrt noncentral overline
//  LocalWords:  noncentrality IllegalArgumentException href ssq
//  LocalWords:  IllegalStateException set's
