package org.bzdev.math.stats;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class representing a statistic.
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
 * Subclasses implement specific statistics. This class declares
 * common methods and defines methods for computing p values and
 * critical values. The documentation for a subclass  describes the
 * statistic that subclass implements. Please see
 * <A href="package-summary.html#package.description">the package
 * description</A> for a summary of the definitions of these
 * quantities and some related quantities.
 * <P>
 * The following sequence of operations is typical:
 * <OL>
 *   <LI> Create an instance of a statistic, providing some parameters
 *        and optionally providing some or all of a data set or (for
 *        some classes) multiple data sets.
 *   <LI> With a few exceptions, one can then add data to the statistics.
 *        This is typically done by calling a method whose name is
 *        <code>add</code> or whose name starts with the string
 *        <code>add</code>.
 *   <LI> One will then call one or more of the methods
 *        <UL>
 *           <LI> {@link #getValue()} to get the value of the statistic.
 *           <LI> {@link #getPValue(Statistic.PValueMode)} to get the
 *                p value for the statistic.
 *           <LI> {@link #getCriticalValue(Statistic.PValueMode,double)} to
 *                get the critical value(s) for the statistic.
 *           <LI> {@link #getDistribution()} to get the probability
 *                distribution for the statistic.
 *        </UL>
 *    <LI> For statistics whose distributions can be created using a
 *         noncentrality parameter, one may also want to use critical
 *         values to check the probability of a type 2 error by
 *         calling the method
 *         {@link #getBeta(double,double,double)} or
 *         {@link #getBeta(double,double,boolean)}, which will return the
 *         probability of a type 2 error given a noncentrality
 *         parameter.  The methods {@link #getNCParameter(double)} or
 *         {@link #getNCParameter(double...)} can be used to obtain
 *         the appropriate noncentrality parameters for a subclass of
 *         {@link Statistic} (the arguments are defined by each
 *         subclass). Instead of calling
 *          {@link #getBeta(double,double,double)} or
 *         {@link #getBeta(double,double,boolean)}, one may call
 *         {@link #getPower(double,double,double)} or
 *         {@link #getPower(double,double,boolean)} to get the statistical
 *         power (defined as 1-&beta;).  For each statistic, the meaning
 *         of a noncentrality parameter, if one exists, is dependent on that
 *         statistic's distribution.
 * </OL>
 * <P>
 * As an example of the use of nocentrality parameter, Student's
 * t-distribution with &nu; degrees of freedom is the distribution of a
 * random variable T defined as $T = \sqrt{\frac{\mu}{V}}$
 * <!--T = Z sqrt(&nu;/V)--> where Z is a normal
 * distribution with an expected value of 0 and a variance of 1,
 * V has a &chi;<sup>2</sup> distribution with &nu; degrees of freedom,
 * and Z and V are independent. The noncentral t-distribution is defined
 * as the distribution of the random variable T defined by
 * $T = (Z + \mu)\sqrt{\frac{\mu}{V}}$
 * <!-- T = (Z+&mu;) sqrt(&nu;/V) --> using the same assumptions for Z and V,
 * and with &mu; being a constant.  This effectively just shifts Z by
 * a constant but results in a different distribution than the distribution
 * when &mu; is zero.
 * <P>
 * In some cases, one may want to estimate the dataset size needed so that
 * type 1 and type 2 errors are within specified limits. Many of the
 * subclasses of {@link Statistic} have constructors that take a number
 * of parameters including the dataset size. These constructors can
 * be used to save the state of a statistic in cases where repeated
 * runs are necessary, but these constructors can also be used for
 * estimating the required dataset size.  Essentially, one would try
 * a data set size, varying it until an adequate size is reached.
 * For each data set size tried, one would compute the desired critical
 * values given the value of &alpha; and then compute the value of
 * &beta; given the worst-case deviation from the expected value (and
 * the corresponding noncentrality parameter) that
 * one would want to detect.
 */
public abstract class Statistic {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor.
     */
    protected Statistic() {super();}

    /**
     * Get the value for a statistic that indicates no deviation
     * from the null hypothesis.  The default is 0.0. It is unusual
     * for this method to be overridden as standard statistics use
     * 0.0 for this purpose.
     * @return the value
     */
    public double optimalValue() {return 0.0;}

    /**
     * Get the value of this statistic.
     * @return the value of this statistic
     * @exception IllegalStateException the value cannot be computed
     *            (for example, because data has not yet been entered)
     */
    public abstract double getValue() throws IllegalStateException;

    /**
     * Get the probability distribution for this statistic.
     * The distribution is the distribution for the statistic, not the 
     * the distribution for the data the statistic describes.
     * @return the probability distribution
     * @exception IllegalStateException the value cannot be computed
     *            (for example, because data has not yet been entered)
     */
    public abstract ProbDistribution getDistribution()
	throws IllegalStateException;

    /**
     * Get the noncentral probability distribution associated with this
     * statistic.
     * This distribution is used for statistical-power calculations or
     * for calculating the probability of a type II error (frequently
     * denoted by the Greek letter &beta;), and is a function of a
     * statistic dependent parameter.
     * <P>
     * When it can be computed, subclasses should override this method
     * and provide subclass-specific documentation.
     * @param nonCentrality the subclass-specific parameter that indicates
     *        how "non-central" the statistic is.
     * @return an appropriate probability distribution.
     * @exception UnsupportedOperationException the operation is not
     *            supported for this statistic
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     */
    public ProbDistribution getDistribution(double nonCentrality)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	throw new UnsupportedOperationException(errorMsg("notSupportedStat"));
    }


    /**
     * Mode for P-value computations.
     */
    public static enum PValueMode {
	/**
	 * Indicates that the probability of the statistic being
	 * larger than (or equal to) the value for this statistic
	 * should be computed.  This is generally the correct choice
	 * when a statistic's probability distribution's domain has
	 * a minimum value of 0.
	 */
	POSITIVE_SIDE,
	/**
	 * Indicates the the probability of the statistic being
	 * smaller than (or equal to) the value for this statistic
	 * should be computed.
	 */
	NEGATIVE_SIDE,
	/**
	 * Indicates that the probability of the statistic being
	 * more extreme (or equal to) the value for this statistic
	 * should be computed.  If a statistic is not symmetric,
	 * this option will result in an error.
	 */
        TWO_SIDED,
	/**
	 * Indicates that the p value computation should use the
	 * mode NEGATIVE_SIDE if the statistic is smaller than the
	 * value returned by the method {@link Statistic#optimalValue()} and
	 * otherwise use the mode POSITIVE_SIDE.
	 * This mode is used primarily in cases where the domain
	 * minimum for the probability distribution functions is the value
	 * returned by {@link Statistic#optimalValue()}, in which case it
	 * acts like a synonym for POSITIVE_SIDE.
	 */
        ONE_SIDED
    }

    /**
     * Get the p-value for this statistic.
     * If the argument is null, a default mode is chosen.  If the
     * probability distribution is symmetric about the value returned
     * by {@link #optimalValue()}, the TWO_SIDED option is used. Otherwise
     * the ONE_SIDED option is used.
     * <P>
     * Before using p-values, please read
     * "<a href="https://www.amstat.org/newsroom/pressreleases/P-ValueStatement.pdf">
     * Statement on Statistical Significance and P-Values</A>," as p-values
     * are often misinterpreted.
     * @param mode one of the PvalueMode enumeration constants POSITIVE_SIDE,
     *        NEGATIVE_SIDE, TWO_SIDED, ONE_SIDED; null for a default
     *        based on the type of statistic
     * @return the p-value
     * @exception IllegalArgumentException the mode is not one that
     *            is accepted for this statistic
     */
    public double getPValue(PValueMode mode) {
	double stat = getValue();
	double ov = optimalValue();
	ProbDistribution distr = getDistribution();
	if (mode == null) {
	    if (distr.isSymmetric(ov)) mode = PValueMode.TWO_SIDED;
	    else mode = PValueMode.ONE_SIDED;
	}

	switch(mode) {
	case POSITIVE_SIDE:
	    return distr.Q(stat);
	case NEGATIVE_SIDE:
	    return distr.P(stat);
	case TWO_SIDED:
	    if (!distr.isSymmetric(ov))
		throw new IllegalArgumentException
		    (errorMsg("badPValueMode", "BOTH_SIDES"));
	    if (stat == ov) return 1.0;
	    if (stat < ov) {
		// return distr.P(stat) + distr.Q(2*ov-stat);
		return 2.0 * distr.P(stat);
	    } else {
		// return distr.Q(stat) + distr.P(2*ov-stat);
		return 2.0 * distr.Q(stat);
	    }
	case ONE_SIDED:
	    if (stat < ov) {
		return distr.P(stat);
	    } else {
		return distr.Q(stat);
	    }
	default:
	    throw new Error("missing case");
	}
    }

    /**
     * Get the critical value.
     * The critical value is obtained from the inverse of the
     * function implemented by {@link #getPValue(PValueMode)}.
     * <P>
     * The modes TWO_SIDED and ONE_SIDED are supported only when the
     * probability distribution is symmetric about a statistic-dependent
     * value (typically zero).  For the ONE_SIDED case, the optimal value
     * for the statistic (the value when the errors are zero) must be 0.0.
     * @param mode one of the PvalueMode enumeration constants POSITIVE_SIDE,
     *        NEGATIVE_SIDE, TWO_SIDED, ONE_SIDED; null for a default
     *        based on the type of statistic
     * @param alpha the probability that a value at least as
     *        extreme as the returned value occurred by chance
     * @return the critical value
     */
    public double getCriticalValue(PValueMode mode, double alpha) {
	double ov = optimalValue();
	ProbDistribution distr = getDistribution();
	if (mode == null) {
	    if (distr.isSymmetric(ov)) mode = PValueMode.TWO_SIDED;
	    else mode = PValueMode.ONE_SIDED;
	}

	switch(mode) {
	case POSITIVE_SIDE:
	    return distr.inverseQ(alpha);
	case NEGATIVE_SIDE:
	    return distr.inverseP(alpha);
	case TWO_SIDED:
	    if (!distr.isSymmetric(ov))
		throw new IllegalArgumentException
		    (errorMsg("badPValueMode", "TWO_SIDED"));
	    return distr.inverseQ(alpha/2.0);
	case ONE_SIDED:
	    if (ov != 0.0) {
		throw new IllegalArgumentException
		    (errorMsg("badPValueModeZ", "ONE_SIDED"));
	    }
	    return distr.inverseQ(alpha);
	default:
	    throw new Error("missing case");
	}
    }

    /**
     * Get the noncentrality parameter given a subclass-specific argument.
     * The default behavior is to throw an exception.
     * Typically the argument will be the difference
     * between some offset value and a current value.
     * @param arg the argument
     * @return the noncentrality parameter
     */
    public double getNCParameter(double arg)
	throws IllegalArgumentException, IllegalStateException,
	       UnsupportedOperationException
    {
	throw new UnsupportedOperationException(errorMsg("notSupportedStat"));
    }

    /**
     * Get the noncentrality parameter given subclass-specific arguments.
     * The default behavior when one argument is provided is to return
     * the value obtained by calling the method
     * {@link #getNCParameter(double)}, although subclasses may override
     * this behavior. Typically the arguments will be a difference
     * between some offset values and current values.
     * @param args the arguments
     * @return the noncentrality parameter.
     */
    public double getNCParameter(double... args)
	throws IllegalArgumentException, IllegalStateException,
	       UnsupportedOperationException
    {
	if (args.length == 0) {
	    throw new IllegalArgumentException(errorMsg("noArgs"));
	}
	if (args.length == 1) {
	    return getNCParameter(args[0]);
	} else {
	    throw new UnsupportedOperationException
		(errorMsg("notSupportedStat"));
	}
    }

    /**
     * Get the statistical power given two critical values.
     * The statistical power is the probability that the null hypothesis
     * was rejected when the alternate hypothesis is in fact true.
     * The critical values give the range of values of a statistic for which the
     * null hypothesis is likely to be true.
     * The noncentrality parameter determines the alternative hypothesis.
     * @param nonCentrality the noncentrality parameter
     * @param cv1 the first critical value
     * @param cv2 the second critical value
     * @return the statistical power
     * @exception UnsupportedOperationException the operation is not
     *            supported for this statistic
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     * @see #getCriticalValue(PValueMode,double)
     */
    public double getPower(double nonCentrality, double cv1, double cv2)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	ProbDistribution distr = getDistribution(nonCentrality);
	if (cv2 > cv1)
	    return distr.Q(cv2) + distr.P(cv1);
	else if (cv2 < cv1)
	    return distr.Q(cv1) + distr.P(cv2);
	else return 1.0;
    }

    /**
     * Get the statistical power given one critical value.
     * The statistical power is the probability that the null hypothesis
     * was rejected when the alternate hypothesis is in fact true.
     * The critical values give the range of values of a statistic for which the
     * null hypothesis is likely to be true.
     * The noncentrality parameter determines the alternative hypothesis.
     * @param nonCentrality the noncentrality parameter
     * @param cv the critical value
     * @param upperBound true if the critical value is an upper bound
     *        on the range of values for which the null hypothesis is
     *        assumed to be true; false otherwise
     * @return the statistical power
     * @exception UnsupportedOperationException the operation is not
     *            supported for this statistic
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     * @see #getCriticalValue(PValueMode,double)
     */
    public double getPower(double nonCentrality, double cv,
			   boolean upperBound)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	ProbDistribution distr = getDistribution(nonCentrality);
	if (upperBound) {
	    return distr.Q(cv);
	} else {
	    return distr.P(cv);
	}
    }

    /**
     * Get the probability &beta; of a type 2 error given two critical values.
     * The probability &beta; is the probability that the null hypothesis
     * was not rejected when the alternate hypothesis is in fact true.
     * The critical values give the range of values of a statistic for which the
     * null hypothesis is likely to be true.
     * The noncentrality parameter determines the alternative hypothesis.
     * @param nonCentrality the noncentrality parameter
     * @param cv1 the first critical value
     * @param cv2 the second critical value
     * @return the probability of a type 2 error
     * @exception UnsupportedOperationException the operation is not
     *            supported for this statistic
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     * @see #getCriticalValue(PValueMode,double)
     */
    public double getBeta(double nonCentrality, double cv1, double cv2)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	ProbDistribution distr = getDistribution(nonCentrality);
	if (cv2 > cv1)
	    return distr.P(cv2) - distr.P(cv1);
	else if (cv2 < cv1)
	    return distr.P(cv1) - distr.P(cv2);
	else return 0.0;
    }

    /**
     * Get the probability &beta; of  a type 2 error given one critical value.
     * The probability &beta; is the probability that the null hypothesis
     * was not rejected when the alternate hypothesis is in fact true.
     * The critical values give the range of values of a statistic for which the
     * null hypothesis is likely to be true.
     * The noncentrality parameter determines the alternative hypothesis.
     * @param nonCentrality the noncentrality parameter
     * @param cv the critical value
     * @param upperBound true if the critical value is an upper bound
     *        on the range of values for which the null hypothesis is
     *        assumed to be true; false otherwise
     * @return the probability of a type 2 error
     * @exception UnsupportedOperationException the operation is not
     *            supported for this statistic
     * @exception IllegalArgumentException the argument is not allowed
     *            for this statistic
     * @exception IllegalStateException the state of this statistic does
     *            not allow this function to return a meaningful value
     *            (e.g., because enough data has not be provided)
     * @see #getCriticalValue(PValueMode,double)
     */
    public double getBeta(double nonCentrality, double cv,
			   boolean upperBound)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	ProbDistribution distr = getDistribution(nonCentrality);
	if (upperBound) {
	    return distr.P(cv);
	} else {
	    return distr.Q(cv);
	}
    }

}

//  LocalWords:  IllegalStateException optimalValue href PvalueMode
//  LocalWords:  IllegalArgumentException badPValueMode distr ov OL
//  LocalWords:  getPvalue PValueMode badPValueModeZ exbundle arg cv
//  LocalWords:  getValue getPValue getCriticalValue getDistribution
//  LocalWords:  noncentral nonCentrality notSupportedStat args
//  LocalWords:  UnsupportedOperationException noncentrality noArgs
//  LocalWords:  getNCParameter getBeta boolean getPower dataset
//  LocalWords:  upperBound
