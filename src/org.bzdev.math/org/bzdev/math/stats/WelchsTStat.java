package org.bzdev.math.stats;

/**
 * Statistic for Welch's T Test.  Welch's t-test compares two independent data
 * sets X<sub>1</sub> and X<sub>2</sub> to determine if a difference
 * in their mean values is statistically significant.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]},
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * The statistic (See <A HREF="https://www.datanovia.com/en/lessons/types-of-t-test/unpaired-t-test/welch-t-test/">https://www.datanovia.com/en/lessons/types-of-t-test/unpaired-t-test/welch-t-test/</A>)
 * is
 * $$t = \frac{\mbox{x&#x0305;}_1 - \mbox{x&#x0305;}_2}{
 *    \sqrt{\frac{s_{X_1}^2}{n_1} + \frac{s_{X_2}^2}{n_2}}
 * }$$

 * <!-- t = (<span style="text-decoration: overline">X<sub>1</sub></span>
 * - <span style="text-decoration: overline">X<sub>2</sub></span>)
 * / sqrt(s<sub>X<sub>1</sub></sub>/n<sub>1</sub>
 * + s<sub>X<sub>2</sub></sub>/n<sub>2</sub>)-->
 * where
 * <UL>
 *  <LI> $\mbox{x&#x0305;}_1$
 *      <!-- <span style="text-decoration: overline">X<sub>1</sub></span>-->
 *      is the mean for data set X<sub>1</sub>.
 *  <LI> $\mbox{x&#x0305;}_2$
 *       <!-- <span style="text-decoration: overline">X<sub>2</sub></span>-->
 *       is the mean for data set X<sub>2</sub>.
 *  <LI> s<sub>X<sub>1</sub></sub> is the sample standard deviation
 *       for  data set X<sub>1</sub>.
 *  <LI> s<sub>X<sub>2</sub></sub> is the sample standard deviation
 *       for data set X<sub>2</sub>.
 *  <LI> n<sub>1</sub> is the size of data set X<sub>1</sub>
 *  <LI> n<sub>2</sub> is the size of data set X<sub>2</sub>
 * </UL>
 * The number of degrees of freedom $\nu$ is approximated by the equation
 * $$\nu \approx \frac{(\frac{s_{X_1}^2}{n_1} + \frac{s_{X_2}^2}{n_2})^2}{
 * \frac{s_{X_1}^4}{n_1^2\nu_1} + \frac{s_{X_2}^4}{n_2^2\nu_2}
 * }$$
 * &nu; &cong; (s<sub>X<sub>1</sub></sub><sup>2</sup>/n<sub>1</sub>
 * + s<sub>X<sub>2</sub></sub><sup>2</sup>/n<sub>2</sub>)<sup>2</sup>
 * / (s<sub>X<sub>1</sub></sub><sup>4</sup>/(n<sub>1</sub>&nu;<sub>1</sub>
 * + s<sub>X<sub>2</sub></sub><sup>4</sup>/n<sub>2</sub>)<sup>2</sup>&nu;<sub>1</sub>),
 * where
 * <UL>
 *   <LI> &nu;<sub>1</sub> = n<sub>1</sub> - 1,
 *   <LI> &nu;<sub>2</sub> = n<sub>2</sub> - 1,
 * </UL>
 * and is rounded down to an integral value.
 */
public class WelchsTStat extends Statistic {

    private double degreesOfFreedom = 0.0;

    /**
     * Set the number of degrees of freedom for this instance.
     * This must be called by a subclass whenever the statistics
     * are updated.
     * @param d the degrees of freedom; 0 no data is available
     */
    protected void setDegreesOfFreedom(double d) {
	degreesOfFreedom = d;
    }

    /**
     * Get the number of degrees of freedom for this instance.
     * @return the number of degress of freedom
     */
    public double getDegreesOfFreedom() {
	return degreesOfFreedom;
    }

    @Override
    public ProbDistribution getDistribution() {
	int dof = (int)Math.floor(degreesOfFreedom);
	return new StudentsTDistr(dof);
    }

    BasicStats.Sample stats1;
    BasicStats.Sample stats2;

    /**
     * Constructor.
     * To add data to each of the two data sets X<sub>1</sub> and
     * X<sub>2</sub>, the methods {@link #add1(double)} and
     * {@link #add2(double)} must be used.
     */
    public WelchsTStat() {
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
    public WelchsTStat(double mean1, double variance1, long n1,
		       double mean2, double variance2, long n2)
    {
	stats1 = new BasicStats.Sample(mean1, variance1, n1);
	stats2 = new BasicStats.Sample(mean2, variance2, n2);
	double v1 = stats1.getVariance();
	double v2 = stats2.getVariance();
	double tmp = v1/n1 + v2/n2;
	double d = tmp*tmp/(v1*v1/(n1*n1*(n1-1)) + v2*v2/(n2*n2*(n2-1)));
	setDegreesOfFreedom(d);
    }

    /**
     * Constructor given two data sets.
     * Additional data can be added by calling the methods
     * {@link #add1(double)} and {@link #add2(double)}.
     * @param x1 values to add to data set X<sub>1</sub>
     * @param x2 values to add to data-set X<sub>2</sub>
     */
    public WelchsTStat(double[] x1, double[] x2) {
	super();
	stats1 = (x1 == null)? new BasicStats.Sample():
	    new BasicStats.Sample(x1);
	stats2 = (x2 == null)? new BasicStats.Sample():
	    new BasicStats.Sample(x2);
	long n1 = stats1.size();
	long n2 = stats2.size();
	double v1 = stats1.getVariance();
	double v2 = stats2.getVariance();
	double tmp = v1/n1 + v2/n2;
	double d = tmp*tmp/(v1*v1/(n1*n1*(n1-1)) + v2*v2/(n2*n2*(n2-1)));
	setDegreesOfFreedom(d);
    }

    /**
     * Add an entry to data set X<sub>1</sub>.
     * @param x1 the value to add
     */
    public void add1(double x1) {
	stats1.add(x1);
	long n1 = stats1.size();
	long n2 = stats2.size();
	double v1 = stats1.getVariance();
	double v2 = stats2.getVariance();
	double tmp = v1/n1 + v2/n2;
	double d = tmp*tmp/(v1*v1/(n1*n1*(n1-1)) + v2*v2/(n2*n2*(n2-1)));
	setDegreesOfFreedom(d);
    }

    /**
     * Add an entry to data set X<sub>2</sub>.
     * @param x2 the value to add
     */
    public void add2(double x2) {
	stats2.add(x2);
	long n1 = stats1.size();
	long n2 = stats2.size();
	double v1 = stats1.getVariance();
	double v2 = stats2.getVariance();
	double tmp = v1/n1 + v2/n2;
	double d = tmp*tmp/(v1*v1/(n1*n1*(n1-1)) + v2*v2/(n2*n2*(n2-1)));
	setDegreesOfFreedom(d);
    }

    /**
     * Get the mean for data-set 1.
     * @return the mean for data-set 1
     */
    public double getMean1() {return stats1.getMean();}

    /**
     * Get the sample standard deviation for data-set 1.
     * @return the standard deviation for data-set 1
     */
    public double getSDev1() {return stats1.getSDev();}

    /**
     * Get the mean for data-set 2.
     * @return an object that records the mean and standard deviation
     *         for data-set 2
     */
    public double getMean2() {return stats2.getMean();}

    /**
     * Get the sample standard deviation for data-set 2.
     * @return the sample standard deviation for data-set 2
     */
    public double  getSDev2() {return stats2.getSDev();}

    /**
     * Get the value of the statistic.
     * @return the value of the statistic
     */
    @Override
    public double getValue() {
	long n1 = stats1.size();
	long n2 = stats2.size();
	double v1 = stats1.getVariance();
	double v2 = stats2.getVariance();
	return (stats1.getMean() - stats2.getMean())
	    / Math.sqrt(v1/n1 + v2/n2);
    }

    /**
     * Get the noncentrality parameter given a difference in mean values.
     * <A href="http://www.ncss.com/wp-content/themes/ncss/pdf/Procedures/PASS/Two-Sample_T-Tests_Allowing_Unequal_Variance-Enter_Difference.pdf">
     * Two-Sample T-Tests Allowing Unequal Variance</A>
     * describes how to compute the noncentrality parameter.
     * The difference is equal to &mu;<sub>1</sub> - &mu;<sub>0</sub> where
     * &mu;<sub>0</sub> = 0 (the value assumed by the null hypothesis
     * H<sub>0</sub>) and &mu;<sub>1</sub> is the value assumed by an
     * alternate hypothesis H<sub>1</sub>.
     * @param diff the difference of the H1 mean value and the H0 mean value.
     * @return the noncentrality parameter
     */
    @Override
    public double getNCParameter(double diff) {
     // http://www.ncss.com/wp-content/themes/ncss/pdf/Procedures/PASS/Two-Sample_T-Tests_Allowing_Unequal_Variance-Enter_Difference.pdf
     // for a description of what to return.
     long n1 = stats1.size();
     long n2 = stats2.size();
     double v1 = stats1.getVariance();
     double v2 = stats2.getVariance();
     return diff / Math.sqrt(v1/n1 + v2/n2);
    }

}

//  LocalWords:  Welch's overline sqrt cong noncentrality href
