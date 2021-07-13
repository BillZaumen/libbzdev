package org.bzdev.math.stats;
import org.bzdev.lang.MathOps;
import org.bzdev.math.Functions;
//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class providing methods for the F-distribution.
 * An F-distribution is the distribution of the random variable
 * (X<sub>1</sub>/&nu;<sub>1</sub>) / (X<sub>2</sub>/&nu;<sub>2</sub>),
 * where X<sub>1</sub> and X<sub>2</sub> are independent random variables
 * with a &Chi;<sup>2</sup> distribution with &nu;<sub>1</sub> and
 * &nu;<sub>2</sub> degrees of freedom respectively.
 * <P>
 * For noncentral F-distributions, the random variable X<sub>1</sub> defined
 * above has a noncentral &Chi;<sup>2</sup> distribution with a
 * noncentrality parameter &lambda;.
 */
public class FDistr extends ProbDistribution {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    private long nu1;
    private long nu2;

    /**
     * Get the number of degrees of freedom (parameter 1).
     * @return the number of degrees of freedom 
     */
    public long getDegreesOfFreedom1() {return nu1;}

    /**
     * Get the number of degrees of freedom (parameter 2).
     * @return the number of degrees of freedom
     */
    public long getDegreesOfFreedom2() {return nu2;}

    private boolean nonCentral = false;
    private double lambda;

    /**
     * Determine if this object is a noncentral &Chi;<sup>2</sup>
     * distribution.
     * @return true if this object is a noncentral &Chi;<sup>2</sup>
     *         distribution; false otherwise.
     */
    public boolean isNonCentral() {return nonCentral;}

    /**
     * Get the noncentrality parameter.
     * @return the noncentrality parameter
     * @exception IllegalStateException this object is not a
     *            noncentral &Chi;<sup>2</sup> distribution
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
     * The distribution constructed is that for the random variable
     * defined by
     * (X<sub>1</sub>/&nu;<sub>1</sub>) / (X<sub>2</sub>/&nu;<sub>2</sub>)
     * where X<sub>1</sub> and X<sub>2</sub> are independent random
     * variables, each with a &Chi;<sup>2</sup> distribution and
     * where X<sub>1</sub> has &nu;<sub>1</sub> degrees of freedom and
     * X<sub>2</sub> has &nu;<sub>2</sub> degrees of freedom.
     * @param nu1 the number of degrees of freedom for the first
     *        random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom for the second
     *        random variable  X<sub>2</sub>
     */
    public FDistr(long nu1, long nu2) {
	this.nu1 = nu1;
	this.nu2 = nu2;
    }

    /**
     * Constructor for a noncentral F-distribution.
     * The distribution constructed is that for the random variable
     * defined by
     * (X<sub>1</sub>/&nu;<sub>1</sub>) / (X<sub>2</sub>/&nu;<sub>2</sub>)
     * where X<sub>1</sub> and X<sub>2</sub> are independent random
     * variables, with X<sub>1</sub> having a noncentral &Chi;<sup>2</sup>
     * distribution with &nu;<sub>1</sub> degrees of freedom and with
     * X<sub>2</sub> having a &Chi;<sup>2</sup> distribution with
     * &nu;<sub>2</sub> degrees of freedom.
     * @param nu1 the number of degrees of freedom for the first
     *        random variable  X<sub>1</sub>
     * @param nu2 the number of degrees of freedom for the second
     *        random variable X<sub>2</sub>
     * @param lambda the noncentrality parameter for the distribution
     *        for the first random variable
     */
    public FDistr(long nu1, long nu2, double lambda) {
	this.nu1 = nu1;
	this.nu2 = nu2;
	nonCentral = true;
	this.lambda = lambda;
    }


    @Override
    public double pd(double x) {
	if (nonCentral) return pd(x, nu1, nu2, lambda);
	else return pd(x, nu1, nu2);
    }

    @Override
    public double P(double x) {
	if (nonCentral) return P(x, nu1, nu2, lambda);
	else return P(x, nu1, nu2);
    }

    @Override
    public double Q(double x) {
	if (nonCentral) return Q(x, nu1, nu2, lambda);
	else return Q(x, nu1, nu2);
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
     * Get the probability density for an F-distribution.
     * As mentioned in the class description, an F-distribution is the
     * distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables with a
     * &Chi;<sup>2</sup> distribution with &nu;<sub>1</sub> and
     * &nu;<sub>2</sub> degrees of freedom respectively.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @return the f-distribution probability density
     */
    public static double pd(double f, long nu1, long nu2) {
	if (f == 0.0) {
	    if (nu1 == 1) {
		return Double.POSITIVE_INFINITY;
	    } else if (nu1 == 2) {
		return 1.0;
	    } else {
		return 0.0;
	    }
	}
	if (f == 0.0) return 0.0;
	return Math.sqrt(MathOps.pow(nu1*f,nu1)*MathOps.pow((double)nu2,nu2)
		/ Math.pow(nu1*f + nu2, nu1+nu2))
	    / (f * Functions.Beta(nu1/2.0, nu2/2.0));
     }


    /**
     * Get the cumulative probability for an F-distribution.
     * As mentioned in the class description, an F-distribution is the
     * distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables with a
     * &Chi;<sup>2</sup> distribution with &nu;<sub>1</sub> and
     * &nu;<sub>2</sub> degrees of freedom respectively.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @return the corresponding value of the cumulative probability
     *         distribution
     */
    public static double P(double f, long nu1, long nu2) {
	double x = nu1*f/(nu2 + nu1*f);
	return Functions.BetaI(x, nu1/2.0, nu2/2.0);
    }

    static private final double ROOT2 = Math.sqrt(2.0);
    static private final double ROOT_2_OVER_PI = Math.sqrt(2.0/Math.PI);


    /**
     * Get the complement of the cumulative probability for
     * an F-distribution.
     * As mentioned in the class description, an F-distribution is the
     * distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables with a
     * &Chi;<sup>2</sup> distribution with &nu;<sub>1</sub> and
     * &nu;<sub>2</sub> degrees of freedom respectively.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @return a value mathematically equal to 1 - P(f, nu)
     */
    public static double Q(double f, long nu1, long nu2) {
	double x = nu2 / (nu2 + nu1*f);
	return Functions.BetaI(x, nu2/2.0, nu1/2.0);
    }

    // noncentral cases

    /**
     * Get the probability density for a noncentral F-distribution.
     * As mentioned in the class description, a noncentral
     * F-distribution is the distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables where
     * X<sub>1</sub> has a noncentral &Chi;<sup>2</sup> distribution
     * with &nu;<sub>1</sub> degrees of freedom and a noncentrality
     * parameter &lambda;, and where X<sub>2</sub> has a &Chi;<sup>2</sup>
     * distribution with &nu;<sub>2</sub> degrees of freedom.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @param lambda the noncentrality parameter &lambda; for the distribution
     *        X<sub>1</sub>
     * @return the value of the noncentral f-distribution probability density
     *         for the specified parameters
     */
    public static double pd(double f, long nu1, long nu2, double lambda) {
	if (f == 0.0) {
	    if (nu1 == 1) {
		return Double.POSITIVE_INFINITY;
	    } else if (nu1 == 2) {
		return Math.exp(lambda/2);
	    } else {
		return 0.0;
	    }
	}
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * pd(f, nu1, nu2);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    long nu1p2j = nu1 + 2*j;
	    double pd = Math.sqrt(MathOps.pow(nu1*f,nu1p2j)
				  * MathOps.pow((double)nu2,nu2)
			  / Math.pow(nu1*f + nu2, nu1p2j+nu2))
		/ (f * Functions.Beta(nu1p2j/2.0, nu2/2.0));
	    sum += term * pd;
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

    /**
     * Get the cumulative probability distribution for a noncentral
     * F-distribution.
     * As mentioned in the class description, a noncentral
     * F-distribution is the distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables where
     * X<sub>1</sub> has a noncentral &Chi;<sup>2</sup> distribution
     * with &nu;<sub>1</sub> degrees of freedom and a noncentrality
     * parameter &lambda;, and where X<sub>2</sub> has a &Chi;<sup>2</sup>
     * distribution with &nu;<sub>2</sub> degrees of freedom.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @param lambda the noncentrality parameter &lambda; for the distribution
     *        X<sub>1</sub>
     * @return the value of the cumulative distribution function for the
     *         specified parameters
     */
    public static double P(double f, long nu1, long nu2, double lambda) {
	if (f == 0.0) return 0.0;
	double x = nu1*f/(nu2 + nu1*f);
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * P(f, nu1, nu2);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    double P = Functions.BetaI(x, j + nu1/2.0, nu2/2.0);
	    sum += term * P;
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

    /**
     * Get the complement of the cumulative probability distribution for
     * a noncentral f distribution.
     * As mentioned in the class description, a noncentral
     * F-distribution is the distribution of the random variable
     * F = (X<sub>1</sub>/&nu;<sub>1</sub>) /
     * (X<sub>2</sub>/&nu;<sub>2</sub>), where X<sub>1</sub> and
     * X<sub>2</sub> are independent random variables where
     * X<sub>1</sub> has a noncentral &Chi;<sup>2</sup> distribution
     * with &nu;<sub>1</sub> degrees of freedom and a noncentrality
     * parameter &lambda;, and where X<sub>2</sub> has a &Chi;<sup>2</sup>
     * distribution with &nu;<sub>2</sub> degrees of freedom.
     * @param f a value of the random variable F
     * @param nu1 the number of degrees of freedom &nu;<sub>1</sub>
     *        for the first random variable X<sub>1</sub>
     * @param nu2 the number of degrees of freedom &nu;<sub>2</sub>
     *        for the first random variable X<sub>2</sub>
     * @param lambda the noncentrality parameter &lambda; for the distribution
     *        X<sub>1</sub>
     * @return the value of the complement of the cumulative
     *         distribution function for the specified parameters
     */
    public static double Q(double f, long nu1, long nu2, double lambda) {
	if (f == 0.0) return 1.0;
	double x = nu2 / (nu2 + nu1*f);
	 
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * Q(f, nu1, nu2);
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    double Q = Functions.BetaI(x, nu2/2.0, j + nu1/2.0);
	    sum += term * Q;
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

}

//  LocalWords:  Abramowitz Stegun th exbundle noncentral
//  LocalWords:  noncentrality IllegalStateException wrongNCState
