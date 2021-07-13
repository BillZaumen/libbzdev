package org.bzdev.math.stats;
import org.bzdev.math.Functions;
//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class providing methods for the &Chi;<sup>2</sup> distribution.
 * <P>
 * The &Chi;<sup>2</sup> distribution is the distribution of the sum
 * of the squares of k independent random variables, each with a
 * normal distribution and with unit variances.  For the default
 * statistic, the means of these variables are zero. For a noncentral
 * statistic, the means are nonzero.
 * <P>
 * The use of A, P and Q follows the convention in Abramowitz and
 * Stegun, "Handbook of Mathematical Functions" (10th printing [1972],
 * 9th Dover printing), chapter 26. Some of the methods have names
 * that start with an upper-case letter, contrary to the usual Java
 * convention, in order to conform to this text.
 * <P> 
 * For non-central &Chi;<sup>2</sup> distributions, the the notation
 * in Abramowitz and Stegun (See Equation 26.4.25) is followed.  This
 * corresponds to the case where, when there are k degrees of freedom,
 * &mu;<sub>i</sub> is the mean value of the random variable
 * X<sub>i</sub>, and the random variable described by the
 * distribution is
 * &Chi;<sup>2</sup> = &sum;<sub>i=1</sub><sup>k</sup>
 * X<sub>i</sub><sup>2</sup>.
 * The parameter &lambda; is the noncentrality parameter and is given by
 *  &lambda;=&sum;<sub>i=1</sub><sup>k</sup>&mu;<sub>i</sub><sup>2</sup>.
 */
public class ChiSquareDistr extends ProbDistribution {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    private long nu;

    /**
     * Get the number of degrees of freedom.
     * @return the number of degrees of freedom
     */
    public long getDegreesOfFreedom() {return nu;}


    private boolean nonCentral = false;
    private double lambda;

    /**
     * Determine if this object is a non-central &Chi;<sup>2</sup>
     * distribution.
     * @return true if this object is a non-central &Chi;<sup>2</sup>
     *         distribution; false otherwise.
     */
    public boolean isNonCentral() {return nonCentral;}

    /**
     * Get the non-centrality parameter.
     * @exception IllegalStateException this object is not a
     *            non-central &Chi;<sup>2</sup> distribution
     */
    public double getLambda() {
	if (!nonCentral) throw new IllegalStateException
			     (errorMsg("wrongNCState"));
	return lambda;
    }


    @Override
    public boolean isSymmetric(double x) {return false;}

    /**
     * Constructor.
     * @param nu the number of degrees of freedom
     */
    public ChiSquareDistr(long nu) {
	this.nu = nu;
    }

    /**
     * Constructor for a non-central &Chi;<sup>2</sup> distribution.
     * &lambda; is defined as the sum of the squares of the means of the
     * random variables whose squares are summed.
     * @param nu the number of degrees of freedom
     * @param lambda the non-centrality parameter for the distribution
     */
    public ChiSquareDistr(long nu, double lambda) {
	this.nu = nu;
	nonCentral = true;
	this.lambda = lambda;
    }


    @Override
    public double pd(double x) {
	if (nonCentral) return pd(x, nu, lambda);
	else return pd(x, nu);
    }

    @Override
    public double P(double x) {
	if (nonCentral) return P(x, nu, lambda);
	else return P(x, nu);
    }

    @Override
    public double Q(double x) {
	if (nonCentral) return Q(x, nu, lambda);
	else return Q(x, nu);
    }

    @Override
    public double getDomainMin() {
	return 0.0;
    }

    @Override
    public boolean domainMinClosed() {
	return true;
    }

    /**
     * Get the probability density for a &Chi;<sup>2</sup> distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @return the &Chi;<sup>2</sup> probability density
     */
    public static double pd(double chi2, long nu) {
	double k2 = nu/2.0;
	return Math.pow(chi2, k2 - 1.0) * Math.exp(-chi2/2.0)
	    / (Math.pow(2.0, k2)*Functions.Gamma(k2));
    }


    /**
     * Get the cumulative probability for a &Chi;<sup>2</sup> distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @return the corresponding value of the cumulative probability
     *         distribution
     */
    public static double P(double chi2, long nu) {
	double sum = 1.0;
	double term = 1.0;
	int r = 1;
	double maxterm = 1.0;
	do {
	    term *= chi2/(nu+2*r);
	    if (term > maxterm) maxterm = term;
	    sum += term;
	    r++;
	} while (term > (maxterm/(1.e15)));
	return (Math.pow(chi2/2.0, nu/2.0)
		* (Math.exp(-chi2/2) / Functions.Gamma((nu+2)/2.0)))
	    * sum;
    }

    static private final double ROOT2 = Math.sqrt(2.0);
    static private final double ROOT_2_OVER_PI = Math.sqrt(2.0/Math.PI);


    /**
     * Get the complement of the cumulative probability for
     * a &Chi;<sup>2</sup> distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @return a value mathematically equal to 1 - P(chi2, nu)
     */
    public static double Q(double chi2, long nu) {
	double chi = Math.sqrt(chi2);
	if (nu%2 == 0) {
	    double sum = 1.0;
	    double term = 1.0;
	    int r = 1;
	    long limit = (nu-2)/2;
	    while (r <= limit  && (term/sum) > 1.e-64) {
		term *= chi2/(2.0*r);
		sum += term;
		r++;
	    }
	    return Math.exp(-chi2/2.0)*sum;
	} else {
	    if (nu == 1) {
		return Functions.erfc(chi/ROOT2);
	    }
	    double sum = chi;
	    double term = chi;
	    long r = 1;
	    long limit = (nu - 1)/2;
	    while((++r) <= limit && (term/sum) > 1.e-64) {
		term *= chi2/(2*r-1);
		sum += term;
	    }
	    return Functions.erfc(chi/ROOT2)
		+ ROOT_2_OVER_PI * Math.exp(-chi2/2.0)*sum;
	}
    }

    // non-central cases


    /**
     * Get the probability density for a non-central  &Chi;<sup>2</sup>
     *  distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @param lambda the non-centrality parameter for the distribution
     * @return the value of the noncentral &Chi;<sup>2</sup> probability
     *         density for the specified parameters
     */
    public static double pd(double chi2, long nu, double lambda) {
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * pd(chi2, nu);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    sum += term * pd(chi2, nu + 2*j);
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

    /**
     * Get the cumulative probability distribution for a non-central
     * &Chi;<sup>2</sup> distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @param lambda the non-centrality parameter for the distribution
     * @return the value of the cummulative distribution function for the
     *         specified parameters
     */
    public static double P(double chi2, long nu, double lambda) {
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * P(chi2, nu);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    sum += term * P(chi2, nu + 2*j);
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

    /**
     * Get the complement of the cumulative probability distribution for
     * a non-central &Chi;<sup>2</sup> distribution.
     * @param chi2 the &Chi;<sup>2</sup> value
     * @param nu the number of degrees of freedom
     * @param lambda the non-centrality parameter for the distribution
     * @return the value of the complement of the cummulative
     *         distribution function for the specified parameters
     */
    public static double Q(double chi2, long nu, double lambda) {
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * Q(chi2, nu);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    sum += term * Q(chi2, nu + 2*j);
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

}

//  LocalWords:  Abramowitz Stegun th exbundle noncentral
//  LocalWords:  noncentrality IllegalStateException wrongNCState
