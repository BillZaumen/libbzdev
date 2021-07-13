package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Implementation of Simpson's rule.
 * This class provides an implementation of Simpson's rule for numerical
 * integration. For the simplest case, one will create an anonymous inner
 * class implementing a method named <code>function</code> as follows:
 * <blockquote><code><pre>
 *      SimpsonsRule sr = new SimpsonsRule() {
 *        protected double function(double u) {
 *          return u*u + 3;
 *        }
 *      }
 * <pre></code></blockquote>
 * To integrate, one then calls <code>sr.integrate(a, b, n)</code> where
 * <code>a</code> and <code>b</code> are the limits and <code>n</code> is
 * the number of points. Alternatively, one may use a lambda expression
 * to define the function:
 * <blockquote><code><pre>
 *      SimpsonsRule sr = SimpsonsRule.newInstance((u) -&gt; u*u+3);
 *      double integral  = sr.integrate(a, b, n);
 * <pre></code></blockquote>
 * <P>
 * The type parameter P, if specified, can be used to provide a class
 * that will store the values of parameters used in computing the function
 * to be integrated.  These values can be set by calling
 * {@link #setParameters} and read by calling {@link #getParameters}.
 * Parameters may also be provided explicitly when an "integrate" method
 * is called. The two-argument "function" should be implemented in this
 * case:
 * <blockquote><code><pre>
 *      GLQuadrature<Data> srp = new SimpsonsRule<Data>() {
 *        protected double function(double u, Data data) {
 *          return u*u + data.value;
 *        }
 *      }
 * </pre></code></blockquote>
 * When used, this allows one to define parameterized functions - the
 * parameters act as additional arguments that are typically constant during the
 * integration. A parameter is stored, not copied, so it should not be
 * modified while an integral is being computed (not an issue for
 * single-threaded programs).  If a series of integrals are computed
 * for different values of the parameters, this allows one instance of
 * SimpsonsRule to be used, rather than a separate object for each value.
 * An example of usage is:
 * <blockquote><code><pre>
 *     ...
 *     Data data;
 *     data.value = 3.0;
 *     srp.setParameter(data);
 *     System.out.println(srp.integrate(a, b, n));
 * </pre></code></blockquote>
 * or
 * <blockquote><code><pre>
 *     ...
 *     Data data;
 *     data.value = 3.0;
 *     System.out.println(srp.integrateWithP(a, b, n, data));
 * </pre></code></blockquote>
 * <P>
 * In addition, the methods
 * {@link SimpsonsRule#getArguments(double,double,int)} and
 * {@link SimpsonsRule#integrate(double[])} can be used to precompute
 * function arguments in cases where those will be used repetitively
 * (e.g., for a series of integrals where the parameters change but not
 * the range or the number of points).  For example, consider the following
 * code:
 * <blockquote><code><pre>
 *       SimpsonsRule<Data> sr = new SimpsonsRule<Data>() {
 *          double[] u5cache = null;
 *          public double[] getArguments(double a, double b, int n) {
 *             double[] results = super.getArguments(a, b, n);
 *             u5cache = new double[n+1];
 *             for (i = 0; i &lt;= n; i++) {
 *                double u = results[i];
 *                double uu = u*u;
 *                u5cache[i] = uu*uu*u;
 *                result[i] = (double)i;
 *             }
 *          }
 *           protected double function(double u) {
 *                double u5 = u5cache[(int)(Math.round(u))];
 *                Data data = getParameters();
 *                return u5 * data.value;
 *           }
 *       };
 *       double[] args = sr.getArguments(a, b, 100);
 *       Data data = new Data();
 *       sr.setParameters(data);
 *       for (double value: list) {
 *              data.value = value;
 *              System.out.println(sr.integrate(args));
 *       }
 * </pre></code></blockquote>
 * As an optimization, the code caches the arguments raised to the
 * fifth power to reduce the number of multiplications needed, assuming
 * <code>list</code> contains multiple elements.   This sort of optimization
 * would typically be used only in special cases where sufficiently
 * expensive terms in the function to be integrated can be precomputed.
 * <P>
 * Finally, the method {@link #newInstance} will construct an instance of
 * SimpsonsRule where the function is represented as an instance of
 * {@link RealValuedFunction}. While the implementation is trivial in Java,
 * {@link #newInstance newInstance} simplifies the use of this class from
 * a scripting language.  For example,
 * <blockquote><code><pre>
 *         fs = {valueAt: function(u) {return Math.sin(u);}}
 *         rvf = new RealValuedFunction(scripting, fs);
 *         glq = SimpsonsRule.newInstance(rvf);
 *         integral = rvf.integrate(0.0, Math.PI, 100);
 * </pre></code></blockquote>
 * shows how to use {@link #newInstance newInstance} with ECMAScript.
 * <P>
 * Note: some of the methods are named <code>integrate</code> while
 * those that use parameters explicitly are named <code>integrateWithP</code>.
 * While Java's type system treats primitive types and class types
 * differently, a scripting language may not.
 */
abstract public class SimpsonsRule<P> {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    P parameters = null;

    /**
     * Set  parameters.
     * Parameters are used to provide additional values for computing
     * the function being integrated.  The argument is stored, not copied,
     * and will typically not be changed while an integration is in progress.
     * @param parameters the parameters
     */
    public void setParameters(P parameters) {
	this.parameters = parameters;
    }

    /**
     * Get parameters.
     * Parameters are used to provide additional values for computing
     * the function being integrated.
     * @return an instance of the class representing a root finder's
     *         parameters (this will be the same instance passed to
     *         setParameters)
     */
    public P getParameters() {return parameters;}

    /**
     * The function to integrate.
     * The default implementation calls the two-argument implementation
     * of {@link #function(double,Object)} using the parameters supplied
     * by a previous call to {@link #setParameters}.  If parameters are
     * not used, this method may be overridden.
     *  @param t the value of the parameter with respect to which
     *         one integrates
     *  @return the value of the function when its argument is t
     */
    protected double function(double t) {
	return function(t, parameters);
    };

    /**
     * The function (with parameters) to integrate.
     * Typically, this function will be defined via an anonymous
     * subclass.
     * @param t the function's argument
     * @param p the parameters
     * @return the value of the function given its arguments
     * @exception UnsupportedOperationException the method was needed
     *            but not implemented.
     */
    protected double function(double t, P p) {
	throw new UnsupportedOperationException(errorMsg("functionMissing"));
    }

    /**
     * Integrate from a to b in one step.
     * This is suitable for very short intervals.
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @return the definite integral from a to b
     */
    public double integrate(double a, double b) {
	return (b - a) * (function(a) + 4.0 *function((a+b)/2.0)
			  + function(b)) / 6.0;
    }

    /**
     * Integrate from a to b in one step with explicit parameters.
     * This is suitable for very short intervals.
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param p the parameters
     * @return the definite integral from a to b
     */
    public double integrateWithP(double a, double b, P p) {
	return (b - a) * (function(a, p) + 4.0 *function((a+b)/2.0, p)
			  + function(b, p)) / 6.0;
    }

    /**
     * Integrate from a to b in multiple steps.
     * @param n the number of subintervals over which to apply Simpson's rule
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @return the definite integral from a to b
     */
    public double integrate (double a, double b,  int n) {
	if (n%2 == 1) n++;	// need to make n even
	double incr = (b - a) / n;

	double result = function(a);
	/**
	 * The variable c, used here and in other methods, is the
	 * floating-point error correction used in Kahan's summation
	 * algorithm.  The parentheses in some statements are important
	 * even though they are redundant mathematically.
	 */
	double c = 0.0;
	for (int i = 1; i < n; i++) {
	    double x = a + i * incr;
	    double y = (((i%2 == 1)? 4.0: 2.0) * function(x)) - c;
	    double t = result + y;
	    c = (t - result) - y;
	    result = t;
	}
	result += function(b) - c;
	result *= (b - a)/ (3.0 * n);
	return result;
    }

    /**
     * Integrate from a to b in multiple steps and with explicit parameters.
     * @param n the number of subintervals over which to apply Simpson's rule
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param p the parameters
     * @return the definite integral from a to b
     */
    public double integrateWithP(double a, double b,  int n, P p) {
	if (n%2 == 1) n++;	// need to make n even
	double incr = (b - a) / n;

	double result = function(a, p);
	double c = 0.0;
	for (int i = 1; i < n; i++) {
	    double x = a + i * incr;
	    double y = (((i%2 == 1)? 4.0: 2.0) * function(x, p)) - c;
	    double t = (result + y);
	    c = (t - result) - y;
	    result = t ;
	}
	result += function(b, p) - c;
	result *= (b - a)/ (3.0 * n);
	return result;
    }

    /**
     * Set up data for an integration.
     * This method is used in conjunction with
     * {@link #integrate(double[])} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param n an estimate of the number of points to use
     * @return an array of values at which the function should
     *         be evaluated, with the last argument containing
     *         the scaling factor (b-a)/(3n)
     * @see #integrate(double[])
     */
    public double[] getArguments(double a, double b, int n) {
	if (n%2 == 1) n++;	// need to make n even
	double[] results = new double[n+2];
	double incr = (b-a)/n;
	results[n+1] = incr/3.0;
	results[0] = a;
	for (int i = 1; i < n; i++) {
	    results[i] = a + i * incr;
	}
	results[n] = b;
	return results;
    }

    /**
     * Integrate a function using precomputed arguments.
     * This method is used in conjunction with
     * {@link #getArguments(double,double,int)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param args the array returned by
     *        {@link #getArguments(double,double,int) getArguments(a,b,n)}
     * @return the definite integral from a to b
     * @see #getArguments(double,double,int)
     */
    public double integrate(double[] args) {
	double results = function(args[0]);
	double c = 0.0;
	int n = args.length - 2;
	for (int i = 1; i < n; i++) {
	    double y = (((i%2 == 1)? 4.0: 2.0) * function(args[i])) - c;
	    double t = results + y;
	    c = (t - results) - y;
	    results = t;
	}
	results += function(args[n]) - c;
	results *= args[n+1];
	return results;
    }

    /**
     * Integrate a function using precomputed arguments and with parameters.
     * This method is used in conjunction with
     * {@link #getArguments(double,double,int)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param args the array returned by
     *        {@link #getArguments(double,double,int) getArguments(a,b,n)}
     * @param p the parameters
     * @return the definite integral from a to b
     * @see #getArguments(double,double,int)
     */
    public double integrateWithP(double[] args, P p) {
	double results = function(args[0], p);
	double c = 0.0;
	int n = args.length - 2;
	for (int i = 1; i < n; i++) {
	    double y = (((i%2 == 1)? 4.0: 2.0) * function(args[i], p)) - c;
	    double t = results + y;
	    c = (t - results) - y;
	    results = t;
	}
	results += function(args[n], p) - c;
	results *= args[n+1];
	return results;
    }

    /**
     * Simpson's rule integration given an array of equally spaced values
     * for the range [a, b]: values[0] is the value for a and values[n-1]
     * is the value for b.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param values the values to integrate
     * @param n the number of values
     * @return the definite integral from a to b
     * @throws IllegalArgumentException n is less than 2
     */
    public static final double 
	integrate(double a, double b, double[] values, int n)
    {
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("forthArgOutOfRange", n));
	int nm1 = n - 1;
	if (n%2 == 1) {
	    double result = values[0];
	    double c = 0.0;
	    for (int i = 1; i < nm1; i++) {
		double y = (((i%2 == 1)? 4.0: 2.0) * values[i]) - c;
		double t = result + y;
		c = (t - result) - y;
		result = t;
	    }
	    result += values[nm1] - c;
	    result *= (b - a)/ (3.0 * nm1);
	    return result;
	} else {
	    int m = n - 1;
	    int mm1 = m - 1;
	    double incr = (b - a)/m;
	    double result = values[0];
	    double c = 0.0;
	    for (int i = 1; i < mm1; i++) {
		double y = (((i%2 == 1)? 4.0: 2.0) * values[i]) - c;
		double t = result + y;
		c = (t - result) - y;
		result = t;
	    }
	    result += values[mm1] - c;
	    result *= ((b - incr) - a)/ (3.0 * mm1);
	    // Use the trapezoidal rule for last value point.
	    // We don't use the Kahan summation algorithm for the
	    // last point - it is just one addition and the summation
	    // algorithm is used for the case where n is large.
	    result += incr * (values[mm1] + values[m]) / 2.0;
	    return result;
	}
    }

    static class RVFSimpsonsRule extends SimpsonsRule {
	RealValuedFunctOps f;
	public  RVFSimpsonsRule(RealValuedFunctOps f) {
	    super();
	    this.f = f;
	}
	protected double function(double t) {
	    return f.valueAt(t);
	}
    }

    /**
     * Create a new instance of SimpsonsRule that uses an instance of
     * RealValuedFunction or RealValuedFunctOps as its function.
     * @param f the function
     * @exception IllegalArgumentException n is less than 1
     */
    public static SimpsonsRule newInstance(RealValuedFunctOps f) {
	return new RVFSimpsonsRule(f);
    }
}

//  LocalWords:  exbundle blockquote pre SimpsonsRule sr newInstance
//  LocalWords:  setParameters getParameters GLQuadrature srp lt uu
//  LocalWords:  setParameter integrateWithP getArguments precompute
//  LocalWords:  args precomputed RealValuedFunction fs valueAt rvf
//  LocalWords:  glq param UnsupportedOperationException subintervals
//  LocalWords:  functionMissing Kahan's IllegalArgumentException
//  LocalWords:  forthArgOutOfRange Kahan RealValuedFunctOps
