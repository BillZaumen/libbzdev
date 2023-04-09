package org.bzdev.math.stats;
import org.bzdev.math.Functions;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class providing methods for Student's t-distribution.
 * The use of A, P and Q follows the convention in Abramowitz and
 * Stegun, "Handbook of Mathematical Functions" (10th printing [1972],
 * 9th Dover printing), chapter 26. Some of the methods have names
 * that start with an upper-case letter, contrary to the usual Java
 * convention, in order to conform to this text.
 * <P>
 * This distribution is the distribution of a random variable
 * T = Z sqrt(&nu;/V) where
 * <UL>
 *   <LI> Z is a Gaussian (or normal) random variable with an expected
 *        (or mean) value of 0 and a stadard deviation of 1.
 *   <LI> V is a random variable with a &Chi;<sup>2</sup> distribution with
 *        &nu; degrees of freedom.
 *   <LI> Z and V are independent random variables.
 * </UL>
 * <P>
 * This class also handles the noncentral t distribution, where the
 * random variable T = (Z + &mu;) sqrt (&nu;/V), where &mu; is the
 * noncentrality parameter and Z, V, and &nu; are defined above.
 */
public class StudentsTDistr extends ProbDistribution {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    private int nu;


    private boolean nonCentral = false;
    private double mu;

    /**
     * Determine if this object is a non-central &Chi;<sup>2</sup>
     * distribution.
     * @return true if this object is a non-central &Chi;<sup>2</sup>
     *         distribution; false otherwise.
     */
    public boolean isNonCentral() {return nonCentral;}

    /**
     * Get the noncentrality parameter.
     * @return the noncentrality parameter
     * @exception IllegalStateException this object is not a
     *            non-central &Chi;<sup>2</sup> distribution
     */
    public double getNCParameter() {
	if (!nonCentral) throw new IllegalStateException
			     (errorMsg("notNoncentral"));
	return mu;
    }

    @Override
    public boolean isSymmetric(double x) {return x == 0.0;}

    /**
     * Constructor.
     * @param nu the number of degrees of freedom &nu;
     */
    public StudentsTDistr(int nu) {
	this.nu = nu;
    }

    /**
     * Constructor for a noncentral t distribution.
     * The distribution not be considered to be noncentral if the
     * parameter &mu; is precisely zero.
     * @param nu the number of degrees of freedom &nu;
     * @param mu the noncentrality parameter &mu;
     */
    public StudentsTDistr(int nu, double mu) {
	this.nu = nu;
	if (mu != 0.0) nonCentral = true;
	this.mu = mu;
    }

    @Override
    public double pd(double x) {
	if (nonCentral) return pd(x, nu, mu);
	else return pd(x, nu);
    }

    @Override
    public double P(double x) {
	if (nonCentral) return P(x, nu, mu);
	else return P(x, nu);
    }

    @Override
    public double Q(double x) {
	if (nonCentral) return Q(x, nu, mu);
	else return Q(x, nu);
    }

    @Override
    public double A(double x) {
	if (nonCentral) throw new UnsupportedOperationException
			    (errorMsg("noncentral"));
	return A(x, nu);
    }

    /**
     * Get the probability density for a Student's t-distribution.
     * @param t the value at which to compute the probability density
     * @param nu the number of degrees of freedom
     * @return the &Chi;<sup>2</sup> probability density
     */
    public static double pd(double t, int nu) {

	double nup1o2 = (nu+1)/2.0;

	return (Functions.Gamma(nup1o2) /
		(Math.sqrt(Math.PI) * Functions.Gamma(nu/2.0)))
	    * Math.pow(1 + t*t/nu, -nup1o2);
    }

    /**
     * Get the cumulative probability for a Student's t-distribution.
     * @param t the value at which to compute the cumulative probability
     * @param nu the number of degrees of freedom
     * @return the probability that the a value is in the range (-&infin;,t]
     */
    public static double P(double t, int nu) {
	return 0.5 * (1.0 + A(t, nu));
	/*
	double nup1o2 = (nu+1)/2.0;
	return 0.5 + t * Functions.Gamma(nup1o2)
	    * Functions.hgF(1,2, nu+1,2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));
	*/
    }

    /**
     * Get the complement of the cumulative probability for
     * a Student's t-distribution.
     * The value returned is given by Q(t,&nu;) = 1 - P(t, &nu;)
     * where &nu; is the number of degrees of freedom,
     * t&isin;(-&infin;&infin;), and P is the cumulative probability
     * distribution.
     * @param t the Student's T value
     * @param nu the number of degrees of freedom
     * @return the corresponding value of the cumulative probability distribution
     */
    public static double Q(double t, int nu) {
	return 0.5 * (1.0 - A(t, nu));
	/*
	double nup1o2 = (nu+1)/2.0;
	return 0.5 - t * Functions.Gamma(nup1o2)
	    * Functions.hgF(1,2, nu+1,2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));
	*/
    }

    /**
     * Compute the probability that a value deviates from 0 by no more than
     * a specified amount.
     * The probability is equal to the integral of the probability density
     * from -t to t for &nu; degrees of freedom with t&isin;[0,;&infin;),
     * and consequently A(t,&nu;) = P(t,&nu;)-P(-t,&nu;).
     * The value is computed using a finite series given by Abramowitz
     * &amp; Stegun, Equations 26.7.3 and 26.7.4 (there is a different
     * series for even and odd values of &nu;.) For this implementation,
     * A(-t,&nu;) = -A(t&nu;) although generally one would use only
     * non-negative values of t.
     * @param t the Student's T value
     * @param nu the number of degrees of freedom
     * @return the probability that a value is in the range [-t,t]
     */
    public static double A(double t, int nu) {
	double theta = Math.atan(t/Math.sqrt((double)nu));
	double twoOverPi = 2.0/Math.PI;
	if (nu == 1) {
	    return twoOverPi*theta;
	}
	double sinTheta = Math.sin(theta);
	if (nu == 2) {
	    return sinTheta;
	}
	double cosTheta = Math.cos(theta);
	if (nu == 3) {
	    return twoOverPi*(theta + sinTheta*cosTheta);
	}
	double cosTheta2 = cosTheta*cosTheta;
	int limit = nu-2;
	if (nu%2 == 1) {
	    double term = cosTheta;
	    double sum = term;
	    int even = 0;
	    int odd = 1;
	    do {
		even += 2;
		odd += 2;
		term *= ((double)even/(double)odd)*cosTheta2;
		sum += term;
	    } while (odd < limit);
	    return twoOverPi*(theta + sinTheta*sum);
	} else {
	    double sum = 1.0;
	    double term = 1.0;
	    int odd = -1;
	    int even = 0;
	    do {
		odd+= 2;
		even += 2;
		term *= ((double)odd/(double)even)*cosTheta2;
		sum += term;
	    } while (even < limit);
	    return sinTheta*sum;
	}
	/*
	return 2.0 * t * Functions.Gamma(nup1o2)
	    * Functions.hgF(0.5, nup1o2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));
	*/
    }
    // noncentral case
    private static final double ROOT2 = Math.sqrt(2.0);

    /**
     * Compute the probability density for noncentral t distribution.
     * The distribution is that of the random variable
     * T = (Z + &mu;)/sqrt(V/&nu;), where Z is random variable with a
     * normal distribution, &mu; is a constant, and V is a
     * &Chi;<sup>2</sub>-distributed random variable with &nu; degrees
     * of freedom.
     * @param t the value at which the probability density is computed
     * @param nu the number of degrees of freedom &nu;
     * @param mu the noncentrality parameter &mu;
     * @return the probability density for the given arguments
     */
    public static double pd(double t, int nu, double mu) {
	double musq = mu*mu;
	double tsq = t*t;
	double nuptsq = nu + tsq;
	double arg = musq*tsq/(2.0*(nu + tsq));
	double temp = ROOT2*mu*t*Functions.M((nu/2.0)+1.0,1.5,arg)
	    /(nuptsq*Functions.Gamma((nu+1.0)/2.0));
	temp += Functions.M((nu+1.0)/2.0, 0.5, arg)
	    /(Math.sqrt(nuptsq)*Functions.Gamma(1.0 + (nu/2.0)));
	temp *= Math.pow(nu,nu/2.0) * Functions.Gamma(1.0+nu)
	    * Math.exp(-musq/2.0)
	    / (Math.pow(2.0, nu) * Math.pow(nuptsq, nu/2.0)
	       * Functions.Gamma(nu/2.0));
	return temp;
    }

    /**
     * Compute the value of the cumulative distribution function for
     * a noncentral t distribution.
     * The distribution is that of the random variable
     * T = (Z + &mu;)/sqrt(V/&nu;), where Z is random variable with a
     * normal distribution, &mu; is a constant, and V is a
     * &Chi;<sup>2</sub>-distributed random variable with &nu; degrees
     * of freedom.
     * @param t the value at which the probability density is computed
     * @param nu the number of degrees of freedom &nu;
     * @param mu the noncentrality parameter &mu;
     * @return the value of the cumulative distribution function
     */
    public static double P(double t, int nu, double mu) {
	if (t < 0) return 1.0 - P(-t, nu, -mu);
	// double sum = GaussianDistr(mu, 0.0, 1.0);
	double sum = 0.0;
	double nu2 = nu/2.0;
	double t2 = t*t;
	double y = t2/(t2+nu);
	double musq2 =  mu*mu/2;
	double exp = Math.exp(-musq2);
	double pterm = exp;
	double gamma = Functions.Gamma(1.5);
	double qterm = exp*mu / (ROOT2 * gamma);
	// use Gamma(z+1) = z*Gamma(z).
	int j = 0;
	double term;
	do {
	    term = pterm*Functions.BetaI(y, 0.5+j, nu2)
		+ qterm*Functions.BetaI(y, j+1.0, nu2);
	    sum += term;
	    double gterm = (1.5 + j);
	    j++;
	    pterm *= musq2/j;
	    qterm *= musq2/gterm;
	} while(term > 1.e-32 && (sum == 0.0 || Math.abs(term/sum) > 1.e-16));
	return GaussianDistr.P(-mu, 0.0, 1.0) + sum/2.0;
    }

    /**
     * Compute the value of the complement of the cumulative distribution
     * function for a noncentral t distribution.
     * The distribution is that of the random variable
     * T = (Z + &mu;)/sqrt(V/&nu;), where Z is random variable with a
     * normal distribution, &mu; is a constant, and V is a
     * &Chi;<sup>2</sub>-distributed random variable with &nu; degrees
     * of freedom.
     * @param t the value at which the probability density is computed
     * @param nu the number of degrees of freedom &nu;
     * @param mu the noncentrality parameter &mu;
     * @return the complement of the cumulative distribution evaluated
     *         at the given arguments
     */
    public static double Q(double t, int nu, double mu) {
	if (t < 0) return P(-t, nu, -mu);
	else return 1.0 - P(t, nu, mu);
    }
}
//  LocalWords:  Abramowitz Stegun th infin nup hgF isin exbundle
//  LocalWords:  sqrt noncentral noncentrality IllegalStateException
//  LocalWords:  notNoncentral GaussianDistr
