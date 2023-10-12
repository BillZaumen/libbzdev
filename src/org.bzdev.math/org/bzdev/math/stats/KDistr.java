package org.bzdev.math.stats;
import org.bzdev.lang.MathOps;
import org.bzdev.math.VectorOps;

/**
 * Class providing methods for the Kolmogorov distribution.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * All the methods for this class are static methods.
 * The use of A, P and Q follows the convention in Abramowitz and
 * Stegun, "Handbook of Mathematical Functions" (10th printing [1972],
 * 9th Dover printing), chapter 26. Some of the methods have names
 * that start with an upper-case letter, contrary to the usual Java
 * convention, in order to conform to this text.
 * <P>
 * Suppose there is an ordered set of n uniform random variables,
 * each having a value in the range [0, 1). The Kolmogorov distribution
 * is the distribution of the quantity
 * D<sub>n</sub> = max(D<sub>n</sub><sup>-</sup>,D<sub>n</sub><sup>+</sup>)
 * where
 *  <LI> $D_n^- = \max_{i=1}^n (x_i - \frac{i-1}n)$.
 *  <LI> $D_n^+ = \max_{i=1}^n (\frac{i}{n} - x_i)$.
 * <UL>
 * </UL>
 * <NOSCRIPT><UL>
 *   <LI> D<sub>n</sub><sup>-</sup> =
 *        max<sub>i=1,...,n</sub>{x<sub>i</sub>- (i-1)/n}
 *   <LI> D<sub>n</sub><sup>+</sup> =
 *        max<sub>i=1,...,n</sub>{i/n - x<sub>i</sub>}
 * </UL></NOSCRIPT>
 * <P>
 * The algorithms used are described in
 * <A href="https://www.jstatsoft.org/article/view/v008i18/kolmo.pdf">
 *  George Marsaglia, Wai Wan Tsang, Jingbo Wang,
 * "Evaluating Kolmogorov's Distribution"</A> and
 * <A href="https://www.jstatsoft.org/article/view/v065c03/v65c03.pdf">
 * Luis Carvalho, "An Improved Evaluation of Kolmogorov's Distribution"</A>
 * <P>
 * Note that some authors use D<sub>n</sub> while others use
 * n<sup>1/2</sup>D<sub>n</sub> as a statistic.
 */
public class KDistr extends ProbDistribution {

    @Override
    public boolean isSymmetric(double x) {return false;}

    int n;

    /**
     * Constructor.
     * @param n the number of variates
     */
    public KDistr(int n) {
	this.n = n;
	getDomainMax();		// to force cached_dmax to be set.
    }


    @Override
    public double P(double x) {
	return P(x, n);
    }

    @Override
    public double getDomainMin() {
	return 0.0;
    }

    @Override
    public boolean domainMinClosed() {
	return true;
    }

    private volatile double cached_dmax = -1;
    @Override
    public double getDomainMax() {
	if (cached_dmax < 0) {
	    
	    double d = ((Integer.MAX_VALUE/2)-1)/n;
	    while (2*((int)(n*d) + 1) < 0) d -= 1.0;
	    cached_dmax = d;
	}
	return cached_dmax;
    }

    @Override
    public boolean domainMaxClosed() {
	return true;
    }


    private static final double PI_SQ = Math.PI * Math.PI;
    private static final double ROOT_2PI = Math.sqrt(2.0*Math.PI);
    private static final double PI_SQ_OVER_4 = PI_SQ/4.0;

    private static double getP(double x) {
	if (x == 0.0) return 0.0;
	double sum = 0.0;
	double xsq = x*x;
	int factor = -1;
	double limit = x*1.e-10;
	if (limit > 1.e-15) limit = 1.e-15;
	double term;
	int i = 0;
	double test;
	do {
	    i++;
	    factor *= -1;
	    term = factor * Math.exp(-2.0*i*i*xsq);
	    sum += term;
	    test = Math.abs(term)/sum;
	} while (test > 1.e-15);
	return 1.0 - 2.0*sum;
    }

    private static double getDerivP(double x) {
	if (x == 0.0) return 0.0;
	double sum = 0.0;
	double xsq = x*x;
	int factor = -1;
	double term;
	int i = 0;
	double test;
	do {
	    i++;
	    double i2 = ((double)i)*i;
	    factor *= -1;
	    term = i2 * factor * Math.exp(-2.0*i2*xsq);
	    sum += term;
	    test = Math.abs(term)/sum;
	} while (test > 1.e-15);
	return (8.0*x*sum);
    }

    private static double getSum(double x) {
	double xsq = x*x;
	double sum = 0.0;
	int k = 0;
	double term;
	double limit = x * 1.e-15;
	if (limit > 1.e-15) limit = 1.e-15;
	do {
	    k++;
	    double f = 2.0*((double)k)*((double)k) - 1.0;
	    term = Math.exp(-f*f*PI_SQ/(8*xsq));
	    sum += term;
	} while (term > limit);
	return sum;
    }

    private static double getSum2(double x) {
	double xsq = x*x;
	double sum = 0.0;
	int k = 0;
	double term;
	double limit = x * 1.e-10;
	if (limit > 1.e-15) limit = 1.e-15;
	do {
	    k++;
	    int f = 2*k-1;
	    term = f*f*Math.exp(-f*f*PI_SQ/(8*xsq));
	    sum += term;
	} while (term > limit);
	return sum;
    }

    /**
     * Evaluate the probability density for the Kolmogorov distribution using
     * an estimate of this distribution.
     * The estimate uses the limiting form for this distribution, with
     * accuracy improving as n &rarr; &infin;.
     * @param x the argument representing a statistic
     * @param n the number of variates
     * @return the probability density
     */
    public static double pdL(double x, int n) {
	if (x == 0.0) return 0.0;
	double rn = Math.sqrt(n);
	x *= rn;
	double xsq = x*x;
	if (x > 0.5) {
	    return getDerivP(x)*rn;
	}
	return rn*ROOT_2PI*(PI_SQ_OVER_4*getSum2(x)/xsq - getSum(x))/xsq;
    }

    /**
     * Evaluate the cumulative probability function for a Kolmogorov
     * distribution using an estimate for this distribution.
     * The estimate uses the limiting form for this distribution, with
     * accuracy improving as n &rarr; &infin;.
     * @param x the argument representing a statistic
     * @param n the number of variates
     * @return the cumulative probability for the given arguments
     */
    public static double PL(double x, int n) {
	if (x == 0.0) return 0.0;
	double rn = Math.sqrt((double)n);
	x *= rn;
	if (x > 0.5) {
	    return getP(x);
	}
	return ROOT_2PI * getSum(x) / x;
    }


    /**
     * Evaluate the complement of the cumulative probability function
     * for a Kolmogorov distribution using an estimate of this distribution.
     * The estimate uses the limiting form for this distribution, with
     * accuracy improving as n &rarr; &infin;. Numerically
     * QL(x,n) = 1 - PL(x,n)
     * @param x the argument representing a statistic
     * @param n the number of variates
     * @return the complement of the cumulative probabilty for the given
     *         arguments
     */
    public static double QL(double x, int n) {
	return 1.0 - PL(x,n);
    }

    private static final double DEFAULT_LIMIT = 256.0;
    private static double limit = DEFAULT_LIMIT;

    /**
     * Set the limit for using the limiting form.
     * The limit is the minimum value of x*n beyond which the limiting
     * form PL(x,n) will be used instead of the more accurate algorithm
     * that P(x,n) implements.  The default limit was determined by
     * computing a relative difference, given by the absolute value of 
     * (Q(x,n)-QL(x,n))/Q(x,n), and finding the maximum value of x*n with
     * the constraint that the relative difference is larger than 0.0001
     * and that Q(x,n) &ge; 1.e-10.
     * @param limit a positive number; 0 or negative for the default value
     */
    public static void setLimit(double limit)
    {
       KDistr.limit = (limit > 0)? limit: DEFAULT_LIMIT;
    }

    /**
     * Evaluate the cumulative probability function for the Kolmogorov
     * distribution.
     * @param d the argument representing a statistic
     * @param n the number of variates
     * @return the cumulative probability for the given arguments
     */
    public static double P(double d, int n) {
	double nd = n*d;
	if (nd > limit) return PL(d, n);
	int k = (int)(nd) + 1;
	double h = k - n*d;
	int m = 2*k-1;
	int mm1 = m - 1;
	int mm2 = m - 2;
	int ns = 0;
	double[] v = new double[m];
	double[] w = new double[mm2 < 0? 0: mm2];
	double[] q = new double[m];
	q[k-1] = 1.0;
	{
	    double htmp = 1.0; // used for successive powers of h
	    double ftmp = 1.0; // used for successive 1/(j!)
	    for (int j = 0; j < m-1; j++) {
		ftmp /= j+1;
		htmp *= h;
		if (j < m-2) w[j] = ftmp;
		v[j] = (1-htmp)*ftmp;
	    }
	    v[m-1] =(1 - 2*htmp*h + ((h > 0.5) ? MathOps.pow(2*h-1, m): 0))
		* ftmp / m;
	}
	for (int i = 1; i <= n; i++) {
	    double s = ((double)i) / n;
	    double u = q[0];
	    q[0] = VectorOps.dotProduct(v,q) * s;
	    if (m > 1) {
		for (int j = 1; j < m-1; j++) {
		    double a = u;
		    int mmjm1 = mm1 - j;
		    u = q[j];
		    q[j] = (VectorOps.dotProduct(w, 0, q, j, mmjm1)
			    + v[mmjm1]*q[mm1] + a)
			* s;
		}
		q[mm1] = (v[0]*q[mm1] + u) * s;
	    }
	    if (q[k-1] > 1.e+140) {
		for (int j = 0; j < m; j++) {
		    q[j] *= 1.e-140;
		}
		ns++;
	    }
	    if (q[k-1] < 1.e-140) {
		for (int j = 0; j < m; j++) {
		    q[j] *= 1.e+140;
		}
		ns--;
	    }
	}
	if (ns != 0) {
	    return q[k-1]*MathOps.pow(1.e+140, ns);
	} else {
	    return q[k-1];
	}
    }

    /**
     * Evaluate the complement of the cumulative probability function
     * for the Kolmogorov distribution.
     * Numerically, Q(x,n) = 1 - P(x,n).
     * @param x the argument representing a statistic
     * @param n the number of variates
     * @return the complement of the cumulative probability for the
     *         given arguments
     */
    public static double Q(double x, int n) {
	return 1.0 - P(x, n);
    }
}

//  LocalWords:  Kolmogorov Abramowitz Stegun th href Marsaglia Wai
//  LocalWords:  Tsang Jingbo Kolmogorov's Carvalho dmax QL ge
