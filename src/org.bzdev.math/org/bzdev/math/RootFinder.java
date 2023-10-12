package org.bzdev.math;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import org.bzdev.lang.MathOps;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Root finder.  
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
 * Solves the equation y = f(x,p) for x, where x and y have a
 * type of double and p has a type set by a type parameter and
 * represents a set of parameters that do not vary.
 * There are Three implementations: RootFinder.Brent, RootFinder.Newton and
 * RootFinder.Halley that implement Brent's, Newton's and Halley's method
 * respectively.  If the type parameter is not used, one should
 * not be provided. Otherwise it is a user-defined type used in
 * computing the values of f and its derivatives with respect to
 * x.
 * <P>
 * Once a RootFinder rf is constructed, calling rf.findRoot(guess)
 * finds a root by solving 0 = f(x, p).  To solve the equation
 * y = f(x, p), call solve(y, guess) for the Newton and Halley case,
 * or solve(y, lower, upper) for the Brent case, and the value of x will be
 * returned. The argument "guess" is an initial guess and the arguments
 * "lower" and "upper" are values of x bracketing the value giving the
 * solution, where f(lower, p) - y and f(upper, p) - y have opposite signs.
 * <P>
 * Parameters allow a single root finder to use a number
 * of related functions without the need to allocate a new root finder
 * for each case.
 * If parameters are used, one will define a class to hold the
 * parameters. For example,
 * <pre><code>
 *    class Parameters {
 *        double p1;
 *        double p2;
 *        public Parameters(double x1, double y1) {
 *           p1 = x1;
 *           p2 = x2;
 *        }
 *    }
 * </code></pre>
 * Then create the RootFinder:
 * <pre><code>
 *    Parameters parameters = new Parameters(1.0, 2.0);
 *    RootFinder&lt;Parameters&gt; rf = new RootFinder.Newton&lt;Parameters&gt;(parameters)
 *       {
 *          public double function(double x) {
 *             Parameters p = getParameters();
 *             double p1 = p.x1;
 *             double p2 = p.x2;
 *             double result;
 *             ...
 *             return result;
 *          }
 *          public double firstDerivative(double x) {
 *             Parameters p = getParameters();
 *             double p1 = p.x1;
 *             double p2 = p.x2;
 *             double result;
 *             ...
 *             return result;
 *          }
 *       };
 * </code></pre>
 * To change the parameters, either create a new instance parameters2 and call
 * rf.setParameters(parameters2) or call rf.getParameters() and manipulate the
 * values (e.g., by setting parameters.p1 and parameters.p2).
 * <P>
 * The criteria for convergence can be changed by calling setEpsilon. Limits
 * on the number of iteration steps and some control over those can be set
 * by calling setLimits.
 *
 */
public abstract class RootFinder<P> {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    P parameters = null;
    
    /**
     * Set a RootFinder's parameters.
     * Parameters are used to provide additional values for computing.
     * root-finder's function and their derivatives.
     * @param parameters the parameters
     */
    public void setParameters(P parameters) {
	this.parameters = parameters;
    }

    /**
     * Get a RootFinder's parameters.
     * Parameters are used to provide additional values for computing.
     * root-finder's function and their derivatives.
     * @return an instance of the class representing a root finder's
     *         parameters (this will be the same instance passed to
     *         setParameters)
     */
    public P getParameters() {return parameters;}

    /**
     * Find a root.
     * Starting from an initial guess, findRoot returns the value of
     * x that satisfies f(x, p) = 0, where p represents the current parameters.
     * The implementation simply calls solve with the parameter y set to 0,
     * and with the initial arguments that solve requires (which depend on
     * the root-finding algorithm being used).
     * @param initialArgs the initial arguments required by a subclass
     * @return the root
     * @exception RootFinder.ConvergenceException the method failed to converge
     * @exception MathException an error occurred calling the function f or
     *            one of its derivatives.
     */
    public final double findRoot(double... initialArgs)
	throws RootFinder.ConvergenceException
    {
	return solve(0.0, initialArgs);
    }

    /**
     * Solve an equation.
     * Starting from an initial guess, findRoot returns the value of
     * x that satisfies f(x, p) = y, where p represents the current parameters.
     * @param y the desired value of f(x, p)
     * @param initialArgs the initial arguments required by a subclass
     * @return the value of x that satisfies f(x, p) = y
     * @exception RootFinder.ConvergenceException the method failed
     * @exception MathException an error occurred calling the function f or
     *            one of its derivatives.
     */
    public abstract double solve(double y, double... initialArgs)
	throws RootFinder.ConvergenceException;

    /**
     * Constructor.
     */
    protected RootFinder() {}

    /**
     * Constructor given parameters.
     * @param parameters the parameters
     */
    protected RootFinder(P parameters) {
	this.parameters = parameters;
    }

    /**
     * The iteration limit.
     */
    protected int maxLimit = 128;

    /**
     * Set the iterations limit.
     * The limit is a bound on the maximum number of iterations.
     * @param maxLimit  the maximum-iteration limit
     */
    public void setLimit(int maxLimit) {
	this.maxLimit = maxLimit;
    }

    double epsilon = 1.0e-12;
    boolean relative = false;

    /**
     * Get the current value of epsilon
     * The value of epsilon determines when the algorithm terminates
     * normally.  If the current value of x is in the range [-1.0,
     * 1.0] the requirement for convergence is that the change in the
     * value of x from the previous iteration is less than epsilon and
     * that the corresponding value of y is within epsilon of its
     * desired value. Otherwise the requirement for convergence is
     * that the change in the value of x, with that change divided by
     * x, is less than epsilon and that the corresponding value of y
     * is within epsilon of its desired value.
     * @return the value of epsilon
     */
    public double getEpsilon() {return epsilon;}

    /**
     * Set the current value of epsilon.
     * The value of epsilon determines when the algorithm terminates
     * normally. The error associated with evaluating the function
     * will be set to epsilon.
     * @param epsilon the value of epsilon
     * @exception IllegalArgumentException the argument was not
     *            a positive real number
     */
    public void setEpsilon(double epsilon) throws IllegalArgumentException  {
	setEpsilon(epsilon, false);
    }

    /**
     * Set the current value of epsilon, indicating if it is a relative
     * or absolute value.
     * When absolute, the error in evaluating the function will be
     * epsilon when the default implementation of {@link #ferror(double)}
     * is used.  When relative, the error will be the value of the
     * function after that value is multiplied by epsilon.
     * @param epsilon the value of epsilon
     * @param relative true if the value is relative; false if it is
     * absolute.
     * @exception IllegalArgumentException the first argument was not
     *            a positive real number
     */
    public void setEpsilon(double epsilon, boolean relative)
	throws IllegalArgumentException
    {
	if (epsilon <= 0.0)
	    throw new IllegalArgumentException
		(errorMsg("argNotPositive", epsilon));
	this.epsilon = epsilon;
	this.relative = relative;
    }

    /**
     * The function f in the equation y = f(x, p).
     * The function f(x, p) is used with x varied and with the
     * parameters p constant.
     * @param x the argument of the function
     * @return the value of the function f
     * @see setParameters
     */
    public abstract double function(double x);

    /**
     * The error in the value returned by
     * {@link function(double) function}(x).
     * The default implementation returns the same value as
     * {@link getEpsilon()}. Callers are encouraged to override
     * this method.
     * @param x the argument passed to {@link function(double)}
     * @return the error for the value
     *         {@link function(double) function}(x)
     */
    public abstract double ferror(double x);

    /**
     * RootFinder class using Brent's method.
     * The implementation is based on the
     * <a href="http://en.wikipedia.org/wiki/Brent%27s_method">Wikipedia
     * article about Brent's method</a>.
     */
    public static abstract class Brent<P> extends RootFinder<P> {
	/**
	 * Constructor.
	 */
	public Brent() {super();}

	/**
	 * Constructor given parameters.
	 * @param parameters the parameters
	 */
	public Brent(P parameters) {super(parameters);}

	/**
	 * The function f in the equation y = f(x, p).
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the argument of the function
	 * @return the value of the function f
	 */
        public abstract double function(double x);

	/**
	 * The error in the value returned by
	 * {@link function(double) function}(x).
	 * The default implementation returns the same value as
	 * {@link getEpsilon()} when epsilon was set in absolute mode
	 * and the value of {@link function(double) function}(x)
	 * multiplied by epsilon when epsilon was set in relative mode.
	 * Callers are encouraged to override this method when feasible.
	 * @param x the argument passed to {@link function(double)}
	 * @return the error for {@link function(double) function}(x)
	 */
	public double ferror(double x) {
	    if (relative) {
		return function(x)*epsilon;
	    } else {
		return epsilon;
	    }
	}


	/**
	 * Solve an equation.
	 * Starting from an initial guess, findRoot returns the value of
	 * x that satisfies f(x, p) = y, where p represents the current
	 * parameters.  The initial arguments must be two values of x
	 * such that f(x, p) - y will differ in sign.
	 * @param y the desired value of f(x, p)
	 * @param initialArgs two initial arguments giving the upper and
	 *        lower values that bracket the root.
	 * @exception RootFinder.ConvergenceException the method failed
	 * @exception MathException an error occurred calling the function f or
	 *            one of its derivatives.
	 */
	public double solve(double y, double... initialArgs) {
	    double lower = initialArgs[0];
	    double upper = initialArgs[1];
	    double epsilon = getEpsilon();
	    double a, b, c, d = 0.0;
	    if (lower > upper) {
		a = upper;
		b = lower;
	    } else {
		a = lower;
		b = upper;
	    }
	    double fa, fb;
	    try {
		fa = function(a)-y;
		fb = function(b)-y;
	    } catch (Exception e) {
		String msg = errorMsg("functionEvalFailedBrent");
		throw new MathException(msg, e);
	    }
	    // if (fa == 0.0) return a;
	    // if (fb == 0.0) return b;
	    double err = Math.ulp(y) + ferror(a);
	    if (Math.abs(fa) <= err) return a;
	    err = Math.ulp(y) + ferror(b);
	    if (Math.abs(fb) <= err) return b;
	    double sfa = Math.signum(fa);
	    double sfb = Math.signum(fb);
	    if (sfa == sfb)
		throw new ConvergenceException
		    (errorMsg("solutionNotBracketed"));

	    if (Math.abs(fa) < Math.abs(fb)) {
		double tmp = a;
		a = b;
		b = tmp;
		tmp = fa;
		fa = fb;
		fb = tmp;
	    }
	    c = a;
	    double fc = fa;
	    boolean mflag = true;
	    double s;
	    do {
		/*
		System.out.format("a = %s, b = %s, c = %s\n", a, b, c);
		System.out.format("fa = %s, fb = %s, fc = %s\n", fa, fb, fc);
		*/
		if (fa != a * fc && fb != fc) {
		    s = a*fb*fc/((fa-fb)*(fa-fc))
			+ b*fa*fc/((fb-fa)*(fb-fc))
			+ c*fa*fb/((fc-fa)*(fc-fb));
		} else {
		    s = b - fb*(b-a)/(fb-fa);
		}
		double t1 = (3.0*a + b) / 4.0;
		double t2 = b;
		if (t1 > t2) {
		    double tmp = t1;
		    t1 = t2;
		    t2 = tmp;
		}
		double lim1 = Math.scalb(Math.max(Math.ulp(b), Math.ulp(c)), 5);
		double lim2 = Math.scalb(Math.max(Math.ulp(c), Math.ulp(d)), 5);
		if (!((s > t1) && (s < t2))
		    || (mflag && Math.abs(s-b) >= Math.abs((b-c)/2.0))
		    || (!mflag && Math.abs(s-b) >= Math.abs(c-d)/2.0)
		    || (mflag && (Math.abs(b-c) < lim1))
		    || (!mflag && (Math.abs(c-d) < lim2))) {
		    s = (a+b)/2.0;
		    mflag = true;
		} else {
		    mflag = false;
		}
		double fs;
		try {
		    fs = function(s) - y;
		} catch (Exception e) {
		    String msg = errorMsg("functionEvalFailedBrent");
		    throw new MathException(msg, e);
		}
		double sfs = Math.signum(fs);
		sfa = Math.signum(fa);
		err = Math.ulp(y) + ferror(s);
		if (Math.abs(fs) < err) {
		    return s;
		}
		d = c;
		c = b;
		fc = fb;
		if (sfs != sfa) {
		    b = s;
		    fb = fs;
		} else {
		    a = s;
		    fa = fs;
		}
		if (Math.abs(fa) < Math.abs(fb)) {
		    double tmp = a;
		    a = b;
		    b = tmp;
		    tmp = fa;
		    fa = fb;
		    fb = tmp;
		}
		err = Math.ulp(y) + ferror(b);
		// if (fb == 0.0) return b;
		if (Math.abs(fb) < Math.scalb(err, 5)) return b;
	    } while (Math.abs(b-a) > /*epsilon*/
		     Math.scalb(Math.max(Math.ulp(b), Math.ulp(a)), 5));
	    return b;
	}

	/**
	 * Create a new instance of RootFinder.Brent using
	 * a {@link RealValuedFunction} or {@link RealValuedFunctOps}
	 * to provide the root finder's function.
	 * <P>
	 * A {@link RealValuedFunction}'s
	 * {@link RealValuedFunction#derivAt(double)} and
	 * {@link RealValuedFunction#secondDerivAt(double)} do not have
	 * to be implemented as these are not used.
	 * @param f a function
	 * @return a new root finder
	 */
	public static Brent newInstance(final RealValuedFunctOps f) {
	    return new Brent() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}
	    };
	}

	/**
	 * Create a new instance of RootFinder.Brent using
	 * a {@link RealValuedFunction} or {@link RealValuedFunctOps}
	 * to provide the root finder's function, plus a second function
	 * to provide error values (this function will be used to
	 * implement the method {@link RootFinder#ferror(double)}).
	 * <P>
	 * A {@link RealValuedFunction}'s
	 * {@link RealValuedFunction#derivAt(double)} and
	 * {@link RealValuedFunction#secondDerivAt(double)} do not have
	 * to be implemented as these are not used.
	 * The function provided as the second argument will be used to
	 * implement the method {@link RootFinder#ferror(double)}.
	 * @param f a function
	 * @param ef a function providing the error for f(x)
	 * @return a new root finder
	 */
	public static Brent newInstance(final RealValuedFunctOps f,
					final RealValuedFunctOps ef)
	{
	    return new Brent() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}

		@Override
		public double ferror(double x) {
		    return ef.valueAt(x);
		}
	    };
	}

    }

    /**
     * RootFinder class using Newton's method.
     * Newton's algorithm solves the equation f(x) = 0 by starting
     * with an initial guess x<sub>0</sub> and generating a sequence
     * such that 
     * x<sub>n+1</sub> = x<sub>n</sub> - f(x<sub>n</sub>)/f'(x<sub>n</sub>).
     * <P>
     * If an iteration limit is exceeded, Newton's method is assumed to
     * not converge.  Otherwise Newton's method is assumed to converge if
     * either of two conditions hold:
     * <ul>
     *   <li> for some n, f(x<sub>n</sub>) = 0 and for all smaller values
     *        of n, the convergence criteria hold.
     *   <li> for all n, |f(x<sub>n</sub>)| &gt; |f(x<sub>n+1</sub>)|.
     * </ul>
     * <P>
     * If Newton's method will not converge, the implementation will
     * attempt to use Brent's method.
     * If 0 is in the interval (f(x<sub>n</sub>), f(x<sub>n+1</sub>))
     * then Brent's algorithm will be used with upper and lower initial
     * values of x<sub>n</sub> and x<sub>n+1</sub>. Otherwise if 0
     * is in the interval (f'(x<sub>n</sub>), f'(x<sub>n+1</sub>)),
     * Brent's algorithm is used to compute a value x<sub>m</sub> such
     * that f'(x<sub>m</sub>) = 0, and if 0 is in the interval
     * (f(x),f(x<sub>m</sub>), Brent's algorithm is used to find the root.
     * If Brent's algorithm cannot be used in either of these cases to find
     * the root, an exception is thrown to indicate non-convergence.
     */
    public static abstract class Newton<P> extends RootFinder<P> {
	
	/**
	 * Constructor.
	 */
	public Newton() {super();}

	/**
	 * Constructor given parameters.
	 * @param parameters the parameters
	 */
	public Newton(P parameters) {super(parameters);}

	private DoubleUnaryOperator f = (x) -> function(x);
	private DoubleUnaryOperator df = (x) -> firstDerivative(x);

	private boolean optimized = true;

	/**
	 * Determine if this instance optimizes its result.
	 * @return true if results will be optimized; false otherwise
	 * @see #setOptimized(boolean)
	 */
	public boolean isOptimized() {return optimized;}

	/**
	 * Set whether or not this instance optimizes its results.
	 * The default value is true. Setting the value to false will
	 * speed up the computation by terminating the computation as
	 * soon as the convergence criteria are satisfied.
	 * <P>
	 * When set to true, methods that compute a root will call the method
	 * {@link RootFinder#refineSolution(DoubleUnaryOperator,DoubleUnaryOperator,double,double)}
	 * to improve the value computed. Static methods such as
	 * {@link RootFinder#solveCubic(double[],double[])} or
	 * {@link RootFinder#solvePolynomial(double[],int,double[])}
	 * use instances of {@link RootFinder.Newton}, with optimization
	 * turned off, and then explicitly call refineSolution using a
	 * more accurate implementation of the function, thereby reducing
	 * running time (the more accurate implementations in these cases
	 * use Kahan's addition algorithm for a sum of terms that should add
	 * to zero when a root is found).
	 * @param optimized true if the results will be optimized; otherwise
	 *        false
	 */
	public void setOptimized(boolean optimized) {
	    this.optimized = optimized;
	}

	/**
	 * The function f in the equation y = f(x, p).
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the argument of the function
	 * @return the value of the function f
	 */
        public abstract double function(double x);

	/**
	 * The error in the value returned by
	 * {@link function(double) function}(x).
	 * The default implementation returns the same value as
	 * {@link getEpsilon()} when epsilon was set in absolute mode
	 * and the value of {@link function(double) function}(x)
	 * multiplied by epsilon when epsilon was set in relative mode.
	 * Callers are encouraged to override this method when feasible.
	 * @param x the argument passed to {@link function(double)}
	 * @return the error for {@link function(double) function}(x)
	 */
	public double ferror(double x) {
	    if (relative) {
		return function(x)*epsilon;
	    } else {
		return epsilon;
	    }
	}

	/**
	 * The first derivative of the function f(x, p) with respect
	 * to x.
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the varying argument to the function f
	 * @return the function f's first derivative
	 */
	public  abstract double firstDerivative(double x);

	double lowerBound;
	double upperBound;

	private double withBrent(double y, double guess)
	{
	    double epsilon = getEpsilon();
	    double a, b, c, d = 0.0;
	    double err;
	    if (lowerBound > upperBound) {
		a = upperBound;
		b = lowerBound;
	    } else {
		a = lowerBound;
		b = upperBound;
	    }
	    double fa, fb;
	    try {
		fa = function(a)-y;
		fb = function(b)-y;
	    } catch (Exception e) {
		String msg = errorMsg("functionEvalFailedBrent");
		throw new MathException(msg, e);
	    }
	    /*
	    Not needed - we only call withBrent if we are having trouble
	    converging.
	    double err = Math.ulp(y) + ferror(a);
	    if (Math.abs(fa) <= err) return a;
	    err = Math.ulp(y) + ferror(b);
	    if (Math.abs(fb) <= err) return b;
	    */
	    double sfa = Math.signum(fa);
	    double sfb = Math.signum(fb);
	    if (sfa == sfb)
		throw new ConvergenceException
		    (errorMsg("solutionNotBracketed"));

	    if (Math.abs(fa) < Math.abs(fb)) {
		double tmp = a;
		a = b;
		b = tmp;
		tmp = fa;
		fa = fb;
		fb = tmp;
	    }
	    c = a;
	    double fc = fa;
	    boolean mflag = true;
	    double s;
	    do {
		if (fa != a * fc && fb != fc) {
		    s = a*fb*fc/((fa-fb)*(fa-fc))
			+ b*fa*fc/((fb-fa)*(fb-fc))
			+ c*fa*fb/((fc-fa)*(fc-fb));
		} else {
		    s = b - fb*(b-a)/(fb-fa);
		}
		double t1 = (3.0*a + b) / 4.0;
		double t2 = b;
		if (t1 > t2) {
		    double tmp = t1;
		    t1 = t2;
		    t2 = tmp;
		}
		double lim1 = Math.scalb(Math.max(Math.ulp(b), Math.ulp(c)), 5);
		double lim2 = Math.scalb(Math.max(Math.ulp(c), Math.ulp(d)), 5);
		if (!((s > t1) && (s < t2))
		    || (mflag && Math.abs(s-b) >= Math.abs((b-c)/2.0))
		    || (!mflag && Math.abs(s-b) >= Math.abs(c-d)/2.0)
		    || (mflag && (Math.abs(b-c) < lim1))
		    || (!mflag && (Math.abs(c-d) < lim2))) {
		    s = (a+b)/2.0;
		    mflag = true;
		} else {
		    mflag = false;
		}
		double fs;
		try {
		    fs = function(s) - y;
		} catch (Exception e) {
		String msg = errorMsg("functionEvalFailedBrent");
		throw new MathException(msg, e);
		}
		double sfs = Math.signum(fs);
		sfa = Math.signum(fa);
		err = Math.ulp(y) + ferror(s);
		if (Math.abs(fs) < err) {
		    double min = Math.min(a,b);
		    double max = Math.max(a,b);
		    lowerBound = min;
		    upperBound = max;
		    return s;
		}
		double u = firstDerivative(s);
		if (u != 0.0) {
		    double newS = s - fs/u;
		    double min = Math.min(a,b);
		    double max = Math.max(a,b);
		    if (newS > min && newS < max) {
			// We want to use Brent's method up until
			// the point where Newton's method starts
			// making progress.
			double newFs = function(newS) - s;
			if (Math.abs(newFs) < Math.abs(fs)) {
			    lowerBound = min;
			    upperBound = max;
			    return newS;
			}
		    }
		}
		d = c;
		c = b;
		fc = fb;
		if (sfs != sfa) {
		    b = s;
		    fb = fs;
		} else {
		    a = s;
		    fa = fs;
		}
		if (Math.abs(fa) < Math.abs(fb)) {
		    double tmp = a;
		    a = b;
		    b = tmp;
		    tmp = fa;
		    fa = fb;
		    fb = tmp;
		}
		err = Math.ulp(y) + ferror(b);
		if (Math.abs(fb) < Math.scalb(err, 5)) return b;
	    } while (Math.abs(b-a) > /*epsilon*/
		     Math.scalb(Math.max(Math.ulp(b), Math.ulp(a)), 5));
	    lowerBound=Math.min(a,b);
	    upperBound = Math.max(a,b);
	    return b;
	}

	/**
	 * Solve an equation.
	 * Starting from an initial guess, findRoot returns the value of
	 * x that satisfies f(x, p) = y, where p represents the current
	 * parameters. The implementation used Newton's method but if
	 * the algorithm is not converging and guesses have bracketed
	 * the solution, Brent's method will be tried.
	 * <P>
	 * If Newton's method does not converge and if there are two
	 * arguments giving an interval containing the solution, Brent's
	 * method will be used until Newton's method would provide a
	 * better solution. . The initial interval must bracket the solution:
	 * if the interval is [xl, xu], then f(xl, p) - y and f(xu, p) - y
	 * must have opposite signs. For example,
	 * <CODE>solve(y, guess, xl, xu)</CODE> will use Newton's method
	 * with an initial guess (<CODE>guess</CODE>) and if that does not
	 * converge, it will call
	 * <CODE>b.{@link Brent#solve(double,double...) solve}(x, xl, ux)</CODE>
	 * where <CODE>b</CODE> is a suitably initialized instance of
	 * {@link RootFinder.Brent}.  As the iteration continues, the
	 * interval will become smaller.
	 * @param y the desired value of f(x, p)
	 * @param initialArgs a single mandatory argument providing a
	 *        guess and optionally two additional arguments giving
	 *        a range over which a solution is bracketed
	 * @exception RootFinder.ConvergenceException the method failed
	 * @exception MathException an error occurred calling the function f or
	 *            one of its derivatives.
	 */
	@Override
	public double solve(double y, double... initialArgs) {
	    double guess = initialArgs[0];
	    double rootval = function(guess) - y;
	    boolean notDone = true;
	    double oldDiff = Double.MAX_VALUE;
	    double xerr = Math.ulp(guess);
	    double xerrOD = 0.0;
	    double iterationCount = 0;
	    boolean hasBounds = (initialArgs.length > 2);

	    if (Math.abs(rootval) < ferror(guess) + Math.ulp(y)) {
		return guess;
	    }

	    lowerBound = hasBounds? initialArgs[1]:
		Double.NEGATIVE_INFINITY;
	    upperBound = hasBounds? initialArgs[2]:
		Double.POSITIVE_INFINITY;
	    if (lowerBound > upperBound) {
		double tmp = lowerBound;
		lowerBound = upperBound;
		upperBound = tmp;
	    }

	    for(;;) {
		double u;
		double x;
		double newrootval;

		try {
		    u = firstDerivative(guess);
		    if (u == 0.0) {
			if (Math.abs(function(guess) - y)
			    < Math.ulp(y) + ferror(guess)) {
			    return optimized?
				refineSolution((t)->function(t),
					       (t)->firstDerivative(t),
					       y, guess):
				guess;
			}
			if (hasBounds) {
			    Brent brf = new Brent() {
				    public double function(double x) {
					return Newton.this.function(x);
				    }
				};
			    return optimized?
				refineSolution((t)->function(t),
						  (t)->firstDerivative(t),
						  y, brf.solve(y, lowerBound,
								 upperBound)):
				brf.solve(y, lowerBound, upperBound);
			}
			throw new ConvergenceException
			    (errorMsg("newtonNotProg"));
		    }
		    x = guess - (rootval / u);
		    newrootval = function(x) - y;
		    if (hasBounds) {
			if (x < lowerBound || x > upperBound) {
			    x = withBrent(y, guess);
			    newrootval = function(x) - y;
			}
		    }
		    /*
		    System.out.println("guess = " + x + ", val = "
				       + newrootval + ", error = "
				       +(Math.ulp(y) + ferror(x)));
		    */
		} catch (ConvergenceException e) {
		    throw e;
		} catch (Exception ee) {
		    String msg = errorMsg("fdFailed");
		    throw new MathException(msg, ee);
		}

		if (Math.abs(newrootval) < Math.ulp(y) + ferror(x))
		    return optimized? refineSolution((t)->function(t),
						     (t)->firstDerivative(t),
						     y, x):
			x;

		double diff = Math.abs(x - guess);
		xerr = Math.ulp(x) + Math.ulp(guess);
		if ((rootval > 0.0 && newrootval < 0.0)
		    || (rootval < 0.0 && newrootval > 0.0)) {
		    if (/*diff == 0.0*/Math.abs(diff) < xerr) {
			// if diff < xerr, we are very close to the
			// best possible solution and we know that
			// one must exist.
			return (optimized)?
			    refineSolution((t)->function(t),
					   (t)->firstDerivative(t),
					   y, x):
			    ((Math.abs(rootval) < newrootval)? x: guess);
		    }
		    if (Math.abs(newrootval) >= Math.abs(rootval)) {
			// If the desired value must be between x and
			// guess and the iteration made things worse,
			// use Brent's method.
			Brent brf = new Brent() {
				public double function(double x) {
				    return Newton.this.function(x);
				}
			    };
			return optimized?
			    refineSolution((t)->function(t),
					   (t)->firstDerivative(t),
					   y,  brf.solve(y, x, guess)):
			     brf.solve(y, x, guess);
		    } else {
			oldDiff = diff;
			xerrOD = xerr;
			guess = x;
			rootval = newrootval;
		    }
		} else if (Math.abs(rootval) > Math.abs(newrootval)) {
		    oldDiff = diff;
		    xerrOD = xerr;
		    guess = x;
		    rootval = newrootval;
		} else {
		    // We are not making progress or making things worse.
		    // Try to find a root xm for the first derivative
		    // using Brent's method if the first derivative at
		    // x and the guess have the opposite sign, and try
		    // Brent's method for the range [x, xm] or [guess, xm]
		    // if we've bracketed the root.
		    double fdg = firstDerivative(guess);
		    double fdx = firstDerivative(x);

		    if ((fdg < 0.0 && fdx > 0.0) || (fdg > 0.0 && fdx < 0.0)) {
			try {
			    Brent bfr = new Brent() {
				    public double function(double x) {
					return Newton.this.firstDerivative(x);
				    }
				};
			    bfr.setEpsilon(Math.max(fdg, fdx)*1.e-3);
			    double xm = bfr.solve(0.0, x, guess);
			    double xmval = function(xm) - y;
			    /*
			    System.out.println("xmval = " + xmval);
			    System.out.println("newrootval = " + newrootval);
			    System.out.println("rootval = " + rootval);
			    System.out.println("diff = " + diff);
			    System.out.println("xm = " + xm);
			    System.out.println("guess = " + guess);
			    System.out.println("x = " + x);
			    */
			    if (xmval == 0.0) {
				return optimized?
				    refineSolution((t)->function(t),
						   (t)->firstDerivative(t),
						   y, xm):
				    xm;
			    }
			    if ((newrootval > 0.0 && xmval < 0.0)
				|| (newrootval < 0.0 && xmval > 0.0)) {
				bfr = new Brent() {
					public double function(double x) {
					    return Newton.this.function(x);
					}
					public double ferror(double x) {
					    return Newton.this.ferror(x);
					}
				    };
				return optimized?
				    refineSolution((t)->function(t),
						   (t)->firstDerivative(t),
						   y, bfr.solve(y, x, xm)):
				    bfr.solve(y, x, xm);
			    } else if ((rootval > 0.0 && xmval < 0.0)
				       || rootval < 0.0 && xmval > 0.0) {
				bfr = new Brent() {
					public double function(double x) {
					    return Newton.this.function(x);
					}
					public double ferror(double x) {
					    return Newton.this.ferror(x);
					}
				    };
				return optimized?
				    refineSolution((t)->function(t),
						   (t)->firstDerivative(t),
						   y,
						   bfr.solve(y, guess, xm)):
				    bfr.solve(y, guess, xm);
			    } else {
				if (hasBounds) {
				    Brent brf = new Brent() {
					    public double function(double x) {
						return Newton.this.function(x);
					    }
					    public double ferror(double x) {
						return Newton.this.ferror(x);
					    }
					};
				    return optimized?
					refineSolution((t)->function(t),
						       (t)->firstDerivative(t),
						       y,
						       brf.solve(y,
								 lowerBound,
								 upperBound)):
					brf.solve(y, lowerBound, upperBound);
				}
				throw new ConvergenceException
				    (errorMsg("newtonNotConv"));
			    }
			} catch (ConvergenceException e) {
			    throw e;
			} catch (Exception ee) {
			    String msg = errorMsg("functionEvalNewton");
			    throw new MathException(msg, ee);
			}
		    } else {
			if (hasBounds) {
			    Brent brf = new Brent() {
				    public double function(double x) {
					return Newton.this.function(x);
				    }
				    public double ferror(double x) {
					return Newton.this.ferror(x);
				    }
				};
			    return optimized?
				refineSolution((t)->function(t),
					       (t)->firstDerivative(t),
					       y,
					       brf.solve(y,
							 lowerBound,
							 upperBound)):
				brf.solve(y, lowerBound, upperBound);
			}
			throw new ConvergenceException
			    (errorMsg("newtonNotConv"));
		    }
		}
		if (Math.abs(oldDiff) < xerrOD &&
		    Math.abs(rootval) <= Math.ulp(guess) + ferror(guess)) {
		    return optimized?
			refineSolution((t)->function(t),
				       (t)->firstDerivative(t),
				       y, guess):
			guess;
		}
		/*
		if (Math.abs(guess) <= 1.0) {
		    if (oldDiff < epsilon && Math.abs(rootval) < epsilon)
			return guess;
		} else {
		    if ((oldDiff/guess) < epsilon 
			&& Math.abs(rootval) < epsilon)
			return guess;
		}
		*/
		if (iterationCount++ > maxLimit) {
		    if (hasBounds) {
			Brent brf = new Brent() {
				public double function(double x) {
				    return Newton.this.function(x);
				}
				public double ferror(double x) {
				    return Newton.this.ferror(x);
				}
			    };
			return optimized?
			    refineSolution((t)->function(t),
					   (t)->firstDerivative(t),
					   0.0, brf.solve(y,
							  lowerBound,
							  upperBound)):
			    brf.solve(y, lowerBound, upperBound);
		    }
		    throw new ConvergenceException
			(errorMsg("newtonNotConv"));
		}
	    }
	}

	/**
	 * Create a new instance of RootFinder.Newton using
	 * a {@link RealValuedFunction} to provide the root finder's function
	 * and its first derivative.
	 * <P>
	 * The {@link RealValuedFunction}'s
	 * {@link RealValuedFunction#secondDerivAt(double)}
	 * method does not have to be implemented as this method is
	 * not used.
	 * @param f a function
	 * @return a new root finder
	 */
	public static Newton newInstance(final RealValuedFunction f) {
	    return new Newton() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}
		@Override
		public double firstDerivative(double x) {
		    return f.derivAt(x);
		}
	    };
	}

	/**
	 * Create a new instance of RootFinder.Newton using
	 * a {@link RealValuedFunction} to provide the root finder's function
	 * and its first derivative, plus an error function.
	 * <P>
	 * The {@link RealValuedFunction}'s
	 * {@link RealValuedFunction#secondDerivAt(double)}
	 * method does not have to be implemented as this method is
	 * not used. The function ef will be used to implement the
	 * method {@link RootFinder#ferror(double)}.
	 * @param f a function
	 * @param ef a function providing the error for the value f(x)
	 * @return a new root finder
	 */
	public static Newton newInstance(final RealValuedFunction f,
					 final RealValuedFunctOps ef)
	{
	    return new Newton() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}
		@Override
		public double ferror(double x) {
		    return ef.valueAt(x);
		}

		@Override
		public double firstDerivative(double x) {
		    return f.derivAt(x);
		}
	    };
	}

	/**
	 * Create a new instance of RootFinder.Newton using
	 * an instance of  {@link DoubleUnaryOperator}
	 * or {@link RealValuedFunctOps} to provide the root finder's
	 * function and its first derivative.
	 * @param f the function for the root finder
	 * @param df the derivative of f
	 * @return a new root finder
	 */
	public static Newton newInstance(final DoubleUnaryOperator f,
					 final DoubleUnaryOperator df)
	{
	    return new Newton() {
		@Override
		public double function(double x) {
		    return f.applyAsDouble(x);
		}
		@Override
		public double firstDerivative(double x) {
		    return df.applyAsDouble(x);
		}
	    };
	}

	/**
	 * Create a new instance of RootFinder.Newton using
	 * an instance of  {@link DoubleUnaryOperator}
	 *  or {@link RealValuedFunctOps}
	 *  to provide the root finder's function
	 * and its first derivative.
	 * The function provided as the second argument will be used to
	 * implement the method {@link RootFinder#ferror(double)}.
	 * @param f the function for the root finder
	 * @param df the derivative of f
	 * @param ef a function providing the error in the computation of f
	 * @return a new root finder
	 */
	public static Newton newInstance(final DoubleUnaryOperator f,
					 final DoubleUnaryOperator df,
					 final DoubleUnaryOperator ef)
	{
	    if (ef == null) {
		return new Newton() {
		    @Override
		    public double function(double x) {
			return f.applyAsDouble(x);
		    }
		    @Override
		    public double firstDerivative(double x) {
			return df.applyAsDouble(x);
		    }
		};
	    } else {
		return new Newton() {
		    @Override
		    public double function(double x) {
			return f.applyAsDouble(x);
		    }

		    @Override
		    public double ferror(double x) {
			return ef.applyAsDouble(x);
		    }

		    @Override
		    public double firstDerivative(double x) {
			return df.applyAsDouble(x);
		    }
		};
	    }
	}
    }


    /**
     * RootFinder class using Halley's method.
     * Halley's algorithm solves the equation f(x) = 0 by starting
     * with an initial guess x<sub>0</sub> and generating a sequence
     * such that 
     * x<sub>n+1</sub> = x<sub>n</sub> - (2f(x<sub>n</sub>)f'(x<sub>n</sub>))/(2[f'(x<sub>n</sub>)]<sup>2</sup> - f(x<sub>n</sub>)f''(x<sub>n</sub>)).
     * <P>
     * If an iteration limit is exceeded, Halley's method is assumed to
     * not converge.  Otherwise Halley's method is assumed to converge if
     * either of two conditions hold:
     * <ul>
     *   <li> for some n, f(x<sub>n</sub>) = 0 and for all smaller values
     *        of n, the convergence criteria hold.
     *   <li> for all n, |f(x<sub>n</sub>)| &gt; |f(x<sub>n+1</sub>)|.
     * </ul>
     * <P>
     * If Halley's method will not converge, the implementation will
     * attempt to use Brent's method.
     * If 0 is in the interval (f(x<sub>n</sub>), f(x<sub>n+1</sub>))
     * then Brent's algorithm will be used with upper and lower initial
     * values of x<sub>n</sub> and x<sub>n+1</sub>. Otherwise if 0
     * is in the interval (f'(x<sub>n</sub>), f'(x<sub>n+1</sub>)),
     * Brent's algorithm is used to compute a value x<sub>m</sub> such
     * that f'(x<sub>m</sub>) = 0, and if 0 is in the interval
     * (f(x),f(x<sub>m</sub>), Brent's algorithm is used to find the root.
     * If Brent's algorithm cannot be used in either of these cases to find
     * the root, an exception is thrown to indicate non-convergence.

     * <P>
     * The implementation detects cases where the algorithm will not
     * converge and will use Brent's method in this case if possible.
     */
    public static abstract class Halley<P> extends RootFinder<P> {

	/**
	 * Constructor.
	 */
	public Halley() {super();}

	/**
	 * Constructor given parameters.
	 * @param parameters the parameters
	 */
	public Halley(P parameters) {super(parameters);}

	/**
	 * The function f in the equation y = f(x, p).
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the argument of the function
	 * @return the value of the function f
	 */
	public abstract double function(double x);

	/**
	 * The error in the value returned by
	 * {@link function(double) function}(x).
	 * The default implementation returns the same value as
	 * {@link getEpsilon()} when epsilon was set in absolute mode
	 * and the value of {@link function(double) function}(x)
	 * multiplied by epsilon when epsilon was set in relative mode.
	 * Callers are encouraged to override this method when feasible.
	 * @param x the argument passed to {@link function(double)}
	 * @return the error for {@link function(double) function}(x)
	 */
	public double ferror(double x) {
	    if (relative) {
		return function(x)*epsilon;
	    } else {
		return epsilon;
	    }
	}

	/**
	 * The first derivative of the function f(x, p) with respect
	 * to x.
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the varying argument to the function f
	 * @return the function f's first derivative
	 */
	public abstract double firstDerivative(double x);

	/**
	 * The second derivative of the function f(x, p) with respect
	 * to x.
	 * The function f(x, p) is used with x varied and with the
	 * parameters p constant.
	 * @param x the varying argument to the function f
	 * @return the function f's second derivative
	 */
	public  abstract double secondDerivative(double x);

	/**
	 * Solve an equation.
	 * Starting from an initial guess, findRoot returns the value of
	 * x that satisfies f(x, p) = y, where p represents the current
	 * parameters.
	 * @param y the desired value of f(x, p)
	 * @param initialArgs a single argument providing a guess
	 * @exception RootFinder.ConvergenceException the method failed
	 * @exception MathException an error occurred calling the function f or
	 *            one of its derivatives.
	 */
	@Override
	public double solve(double y, double... initialArgs ) {
	    double guess = initialArgs[0];
	    double rootval = function(guess) - y;
	    boolean notDone = true;
	    double oldDiff = Double.MAX_VALUE;
	    double xerr = Math.ulp(guess);
	    double xerrOD = 0.0;
	    double iterationCount = 0;
	    for(;;) {
		double u;
		double w;
		double uw;
		double x;
		double newrootval;

		try {
		    u = firstDerivative(guess);
		    w = secondDerivative(guess);
		    uw = 2.0 * u*u - rootval * w;
		    if (uw == 0.0) {
			throw new ConvergenceException
			    (errorMsg("halleyNotProg"));
		    }
		    x = guess - ((2.0 * rootval * u) / uw);
		    newrootval = function(x) - y;
		} catch (ConvergenceException e) {
		    throw e;
		} catch (Exception ee) {
		    String msg = errorMsg("fdFailed");
		    throw new MathException(msg, ee);
		}

		// if (newrootval == 0.0) return x;
		if (Math.abs(newrootval) < ferror(x) + Math.ulp(y)) {
		    return x;
		}

		double diff = Math.abs(x - guess);
		xerr = Math.ulp(x) + Math.ulp(guess);
		if ((rootval > 0.0 && newrootval < 0.0)
		    || (rootval < 0.0 && newrootval > 0.0)) {
		    if (/*diff == 0.0*/ Math.abs(diff) < xerr)
			throw new ConvergenceException
			    (errorMsg("halleyNotProg"));
		    if (Math.abs(newrootval) >= Math.abs(rootval)) {
			// If the desired value must be between x and
			// guess and the iteration made things worse,
			// use Brent's method.
			Brent brf = new Brent() {
				public double function(double x) {
				    return Halley.this.function(x);
				}
			    };
			return brf.solve(y, x, guess);
		    } else {
			oldDiff = diff;
			xerrOD = xerr;
			guess = x;
			rootval = newrootval;
		    }
		} else if (Math.abs(rootval) > Math.abs(newrootval)) {
		    oldDiff = diff;
		    xerrOD = xerr;
		    guess = x;
		    rootval = newrootval;
		} else {
		    // We are not making progress or making things worse.
		    // First try to find a root for the first derivative
		    // using Brent's method.
		    double fdg = firstDerivative(guess);
		    double fdx = firstDerivative(x);

		    if ((fdg < 0.0 && fdx > 0.0) || (fdg > 0.0 && fdx < 0.0)) {
			try {
			    Brent bfr = new Brent() {
				    public double function(double x) {
					return Halley.this.firstDerivative(x);
				    }
				};
			    bfr.setEpsilon(Math.max(fdg, fdx)*1.e-3);
			    double xm = bfr.solve(0.0, x, guess);
			    double xmval = function(xm) - y;
			    if (xmval == 0.0) return xm;
			    if ((newrootval > 0.0 && xmval < 0.0)
				|| (newrootval < 0.0 && xmval > 0.0)) {
				bfr = new Brent() {
					public double function(double x) {
					    return Halley.this.function(x);
					}
					public double ferror(double x) {
					    return Halley.this.ferror(x);
					}
				    };
				return bfr.solve(y, x, xm);
			    } else if ((rootval > 0.0 && xmval < 0.0)
				       || (rootval < 0.0 && xmval > 0.0)) {
				bfr = new Brent() {
					public double function(double x) {
					    return Halley.this.function(x);
					}
					public double ferror(double x) {
					    return Halley.this.ferror(x);
					}
				    };
				return bfr.solve(y, guess, xm);
			    } else {
				throw new ConvergenceException
				    (errorMsg("halleyNotConv"));
			    }
			} catch (ConvergenceException e) {
			    throw e;
			} catch (Exception ee) {
			    String msg = errorMsg("functionEvalHalley");
			    throw new MathException(msg, ee);
			}
		    } else {
			throw new ConvergenceException
			    (errorMsg("halleyNotConv"));
		    }
		}
		if (Math.abs(oldDiff) < xerrOD &&
		    Math.abs(newrootval) < ferror(guess) + Math.ulp(y)) {
		    return guess;
		}
		/*
		if (Math.abs(guess) <= 1.0) {
		    if (oldDiff < epsilon && Math.abs(newrootval) < epsilon)
			return guess;
		} else {
		    if ((oldDiff/guess) < epsilon 
			&& Math.abs(newrootval) < epsilon)
			return guess;
		}
		*/
		if (iterationCount++ > maxLimit) {
		    throw new ConvergenceException
			(errorMsg("halleyNotConv"));
		}
	    }
	}

	/**
	 * Create a new instance of RootFinder.Halley using
	 * a {@link RealValuedFunction} to provide the root finder's function
	 * and that function's first and second derivative.
	 * @param f a function
	 * @return the new root finder
	 */
	public static Halley newInstance(final RealValuedFunction f) {
	    return new Halley() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}
		@Override
		public double firstDerivative(double x) {
		    return f.derivAt(x);
		}
		@Override
		public double secondDerivative(double x) {
		    return f.secondDerivAt(x);
		}
	    };
	}
	/**
	 * Create a new instance of RootFinder.Halley using
	 * a {@link RealValuedFunction} to provide the root finder's function
	 * and that function's first and second derivative.
	 * The function provided as the second argument will be used to
	 * implement the method {@link RootFinder#ferror(double)}.
	 * @param f a function
	 * @param ef a function providing the error in the value f(x)
	 * @return a new root finder
	 */
	public static Halley newInstance(final RealValuedFunction f,
					 final RealValuedFunctOps ef)
	{
	    return new Halley() {
		@Override
		public double function(double x) {
		    return f.valueAt(x);
		}
		@Override
		public double ferror(double x) {
		    return ef.valueAt(x);
		}

		@Override
		public double firstDerivative(double x) {
		    return f.derivAt(x);
		}
		@Override
		public double secondDerivative(double x) {
		    return f.secondDerivAt(x);
		}
	    };
	}
    }

    /**
     * RootFinder exception class.
     */
    public static class ConvergenceException extends MathException {
	/**
	 * Constructor.
	 * Constructs a new convergence exception. The cause is not
	 * initialized, and may subsequently be initialized by a call
	 * to {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 */
	public ConvergenceException() {
	    super();
	}

	/**
	 * Constructs a new convergence exception with the specified detailed
	 * message.
	 * The cause is not initialized, and may subsequently be
	 * initialized by a call to
	 * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 * @param msg the detail message; the detail message is saved for
	 * later retrieval by a call to
	 * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
	 */
	public ConvergenceException(String msg) {
	    super(msg);
	}

	/**
	 * Constructs a new convergence exception with the specified detailed
	 * message and cause.
	 * The cause is not initialized, and may subsequently be
	 * initialized by a call to
	 * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 * @param msg the detail message; the detail message is saved for
	 * later retrieval by a call to
	 * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
	 * @param cause the cause
	 */
	public ConvergenceException(String msg, Throwable cause) {
	    super(msg, cause);
	}

	/**
	 * Constructs a new convergence exception with the specified cause
	 * and a detail message of (cause==null ? null :
	 * cause.toString()) (which typically contains the class and
	 * detail message of cause).
	 * This constructor is useful for convergence exceptions that are
	 * little more than wrappers for other throwables.
	 * @param cause the cause; null if the cause is nonexistent or unknown
	 */
	public ConvergenceException(Throwable cause) {
	    super(cause);
	}
    }

    /**
     * Solves the quadratic equation ax<sup>2</sup> + bx + c and places the
     * real roots into its argument array, which is used for both input and
     * output.
     * <P>
     * This method duplicates the behavior of the method
     * {@link java.awt.geom.QuadCurve2D#solveQuadratic(double[])}. It is
     * provided to break a dependency on the jdk.desktop module.
     * @param eqn the input/output array containing the roots (starting at
     *        index 0) for output and the coefficients ordered so that
     *        eqn[0] = c, eqn[1] = b, and eqn[2] = a.
     * @return the number of roots; -1 if the coefficients a and b are zero
     */
    public static int solveQuadratic(double[] eqn) {
	return solveQuadratic(eqn, eqn);
    }

    /**
     * Solves the quadratic equation ax<sup>2</sup> + bx + c.
     * The length of the array res should be large enough to hold all
     * of the roots (a length of at least 2 should be used).
     * <P>
     * If multiple roots have the same value, only one is shown.
     * If roots are sufficiently close that they cannot be distinguished
     * due to floating-point limitations, only one root may be shown.
     * This method duplicates the calling convention used by the method
     * {@link java.awt.geom.QuadCurve2D#solveQuadratic(double[],double[])}.
     * It was provided to break a dependency on the jdk.desktop module.
     * @param eqn the input/output array containing the roots (starting at
     *        index 0) for output and the coefficients ordered so that
     *        eqn[0] = c, eqn[1] = b, and eqn[2] = a.
     * @param res the output array containing the roots (starting at
     *        index 0)
     * @return the number of roots; -1 if the coefficients a and b are zero
     */
    public static int solveQuadratic(double[] eqn, double[] res) {
	double a = eqn[2];
	double b = eqn[1];
	double c = eqn[0];

	DoubleUnaryOperator f = (t) -> {
	    double sum = c;
	    double prod = t;
	    double y = b*prod;
	    double tt = sum + y;
	    double cc = (tt - sum) - y;
	    sum = tt;
	    prod *= t;
	    y = a*prod;
	    tt = sum + y;
	    // cc = (tt - sum) - y;
	    // sum = tt;
	    // return sum;
	    return tt;
	};

	DoubleUnaryOperator df = (t) -> {
	    return b + 2*a*t;
	};

	if (a == 0.0) {
	    if (b == 0.0) return -1;
	    // reduces to a linear equation bx + c = 0 with b != 0
	    res[0] = -c / b;
	    return 1;
	}
	long idelta = 0;
	boolean noOverflow = true;
	boolean hasIntCoefficients = (Math.rint(a) == a)
	    && (Math.rint(b) == b) && (Math.rint(c) == c);

	if (hasIntCoefficients) {
	    long ia = Math.round(a);
	    long ib = Math.round(b);
	    long ic = Math.round(c);
	    long gcd = Math.abs(ia);
	    if (ib != 0) gcd = MathOps.gcd(gcd, Math.abs(ib));
	    if (ic != 0) gcd = MathOps.gcd(gcd, Math.abs(ic));
	    if (gcd > 1) {
		ia /= gcd;
		ib /= gcd;
		ic /= gcd;
	    }
	    long iac = ia*ic;
	    long iac4 = 4*iac;
	    long aia = Math.abs(ia);
	    long aic = Math.abs(ic);
	    boolean notSafe = (aia >= ((1L)<<30)) || (aic >= ((1L)<<30));
	    if (notSafe && ia != 0 && ic != 0) {
		int log2 = 2
		    + (64 - Long.numberOfLeadingZeros(aia - 1))
		    + (64 - Long.numberOfLeadingZeros(aic - 1));
		// 62 because we need a factor of two for the sum
		if (log2 >= 62) noOverflow = false;
	    }
	    /*
	    boolean iac4Neg = (ia != 0) && (ic != 0)
		&& Math.signum(ia) != Math.signum(ic);
	    */
	    long ibb = ib*ib;
	    if (Math.abs(ib) >= (1L)<<30) noOverflow = false;
	    idelta = ibb - iac4;
	    /*
	    if (iac4Neg) {
		if (idelta <= 0) {
		    // we use idelta only to determine whether it is
		    // negative, 0, or positive, so setting it to 1
		    // corrects for overflow.
		    idelta = 1;
		    noOverflow = true;
		}
	    }
	    */
	}

	double delta;
	double limit;
	if (hasIntCoefficients && noOverflow) {
	    delta = idelta;
	    limit = 0.0;
	} else {
	    double b2 = b*b;
	    double ac4 = 4*a*c;
	    limit = Math.ulp(b2);
	    limit = Math.max(limit, Math.ulp(ac4));
	    limit *= 10;
	    delta = b2 - ac4;
	}
	double negb = -b;
	double a2 = 2.0*a;
	if (Math.abs(delta) <= limit) {
	    res[0] = negb/a2;
	    return 1;
	} else if (delta > 0.0) {
	    double sqrt = Math.sqrt(delta);
	    // correct results in case of floating-point errors
	    double r1 = refineSolution(f, df, 0.0, (negb - sqrt) / a2);
	    double r2 = refineSolution(f, df, 0.0, (negb + sqrt) / a2);
	    if (r1 < r2) {
		res[0] = r1;
		res[1] = r2;
	    } else {
		res[0] = r2;
		res[1] = r1;
	    }
	    return 2;
	}
	return 0;
    }

    /**
     * Solves the cubic equation ax<sup>3</sup> + bx<sup>2</sup> + cx + d
     * and places the real roots into its argument array, which is
     * used for both input and output.
     * <P>
     * If multiple roots have the same value, only one of those roots
     * is shown.  If roots are sufficiently close that they cannot be
     * distinguished due to floating-point limitations, only one root
     * may be shown even though multiple distinct roots actually exist.
     * This method duplicates the calling convention used by the method
     * {@link java.awt.geom.CubicCurve2D#solveCubic(double[])}.
     * It was provided to break a dependency on the jdk.desktop module,
     * but experience with it indicates that is is more accurate than
     * the implementation of
     * {@link java.awt.geom.CubicCurve2D#solveCubic(double[])}
     * provided in Java 11.0.1.
     * <P>
     * A cubic function
     * f(x) = ax<sup>3</sup> + bx<sup>2</sup> + cx + d has an
     * inflection point at -b/(3a). The number of critical
     * points is dependent on the value
     * &Delta;<sub>0</sub> = b<sup>2</sup> - 3ac:
     * <UL>
     *   <LI>if &Delta; is negative, there are no critical points
     *       and the cubic function is strictly monotonic. The
     *       implementation uses Newton's method, with the inflection
     *       point as an initial guess.
     *   <LI>if &Delta; is zero, there is one critical point, which
     *       is also an inflection point. There is only one real root,
     *       which is a triple root if the inflection point is a root,
     *       or a single root if the inflection point is not a root. In
     *       the later case, the root's value is
     *       (-b - (27ad - b<sup>3</sup>)<sup>1/3</sup> / (3a).
     *   <LI>if &Delta; is positive, there are two critical points
     *       with the inflection point between these two.  The
     *       critical points are $(-b-\sqrt{\Delta_0})/(3a)$
     *       <!--(-b - sqrt(&Delta;<sub>0</sub>))/(3a)-->
     *       and $(-b + sqrt{\Delta_0})/(3a)$.
     *       <!--(-b + sqrt(&Delta;<sub>0</sub>))/(3a).--> If one of these
     *       is a root r (both cannot be roots), it is a double root, and
     *       the other root has the value -(2ar  + b)/a. Otherwise,
     *       let x<sub>1</sub> be the lower of these two critical points
     *        and let x<sub>2</sub> be the higher of these two values.
     *       <UL>
     *         <LI> If f(x<sub>1</sub>) and f&Prime;(x<sub>1</sub>)
     *               have opposite signs, there is a real root below
     *               x<sub>1</sub>. One can find the root using Newton's
     *               method with an initial guess
     *               $x_g = x_1 - (\frac32)^m\sqrt{-2f(x_1)/f\prime(x_1)}$,
     *               <!-- x<sub>g</sub> =  x<sub>1</sub> -
     *     (3/2)<sup>m</sup>sqrt(-2f&Prime;(x<sub>1</sub>)/f(x<sub>1</sub>)-->,
     *               where m is the smallest non-negative integer such that
     *               f(x<sub>g</sub>) and f(x<sub>1</sub> have opposite
     *               signs.
     *         <LI> If f(x<sub>1</sub>) and f(x<sub>1</sub>) have
     *              opposite signs, there is a root between these two
     *              values. One can find the root using Newton's method
     *              with a lower and upper bound and with the inflection
     *              point as an initial guess. When upper and lower
     *              bounds are provided, the {@link RootFinder.Newton} class
     *              will temporarily switch to Brent's method when Newton's
     *              method's next value is out of range, and will tighten
     *              the upper and lower bounds as new values are tried.
     *         <LI> If f(x<sub>2</sub>) and f&Prime;(x<sub>2</sub>)
     *               have opposite signs, there is a real root above
     *               x<sub>2</sub>.  One can find the root using Newton's
     *               method with an initial guess
     *               $x_g = x_2 - (\frac32)^m\sqrt{-2f(x_2)/f\prime(x_2)}$,
     *               <!--x<sub>g</sub> = x<sub>2</sub> -
     *     (3/2)<sup>m</sup>sqrt(-2f&Prime;(x<sub>2</sub>)/f(x<sub>2</sub>),-->
     *               where m is the smallest non-negative integer such that
     *               f(x<sub>g</sub>) and f(x<sub>2</sub> have opposite
     *               signs.
     *       </UL>
     * </UL>
     * @param eqn the input/output array containing the roots (starting at
     *        index 0) for output and the coefficients ordered so that
     *        eqn[0] = d, eqn[1] = c, eqn[2] = b, and eqn[3] = a.
     * @return the number of roots; -1 if the coefficients a, b, and c are zero
     */
    public static int solveCubic(double[] eqn) {
	return solveCubic(eqn, eqn);
    }


    private static final double DEGREES120 = Math.toRadians(120.0);
    private static final double DEGREES240 = Math.toRadians(240.0);
    // scale factor for use in scalb
    private static final int CUBIC_ERR_SF = 5;

    private static final double SIN120 = Math.sqrt(3.0)/2;
    private static final double COS120 = -0.5;
    private static final double SIN240 = -SIN120;
    private static final double COS240 = COS120;



    // We can compute the discriminant for a cubic equation using
    // integer arithmetic when it has integer coefficients and
    // the absolute value of those coefficients is below this value.
    private static long CUBIC_SAFE_ILIMIT = 23355;

    private static double twiddleSolution(DoubleUnaryOperator f, double y,
					  double arg)
    {
	double tmp = f.applyAsDouble(arg) - y;
	double nv = Math.nextAfter(arg, Double.MAX_VALUE);
	double pv = Math.nextAfter(arg, -Double.MAX_VALUE);
	double tmpn = f.applyAsDouble(nv) - y;
	double tmpp = f.applyAsDouble(pv) - y;
	if (nv == pv) {
	    // don't know which way to go.
	    return arg;
	} else	if (nv < pv) {
	    if (Math.abs(tmpn) < Math.abs(tmp)) {
		arg = nv;
		nv = Math.nextAfter(arg, Double.MAX_VALUE);
		if (Math.abs(f.applyAsDouble(nv) - y) < Math.abs(tmpn)) {
		    arg = nv;
		}
	    }
	} else {
	    if (Math.abs(tmpp) < Math.abs(tmp)) {
		arg = pv;
		pv = Math.nextAfter(arg, -Double.MAX_VALUE);
		if (Math.abs(f.applyAsDouble(pv) - y) < Math.abs(tmpp)) {
		    arg = pv;
		}
	    }
	}
	return arg;
    }

    private static final int REFINE_SOLUTION_LIMIT = 64;

    /**
     * Use Newton's method to refine a solution to the equation
     * f(x) - y = 0.
     * Newton's method uses a guess that is improved by approximating
     * f(x) by x<sub>0</sub> + f'(x<sub>0</sub>)(x - x<sub>0</sub>)
     * and solving the linear equation
     * y = x<sub>0</sub> + f'(x<sub>0</sub>)(x - x<sub>0</sub>), iterating
     * until a convergence criteria is satisfied or until the implementation
     * detects that it will not converge.  This implementation assumes
     * the convergence criteria has been satisfied and iterates as long as
     * the solution can be improved.
     * <P>
     * The algorithm starts by setting x<sub>1</sub> to the value of
     * a guess that is provided by one of this methods arguments.
     * <P>
     * A sequence of values {x<sub>1</sub>, ... x<sub>n</sub>, ...} is
     * generated as follows:
     * For x<sub>n</sub>, if f(x<sub>n</sub>) - y = 0, then
     * x<sub>n+1</sub> = x<n>. Otherwise
     * if f(x<sub>n</sub>) - y != 0, Newton's method is used to determine
     * a value w = x<sub>n</sub> = (f(x<sub>n</sub>)-y)/f'(x<sub>1</sub>))
     * unless f'(x<sub>1</sub>) = 0, in which case  w = x<sub>n</sub>.
     *  If the sign if f(x<sub>n</sub>) - y and f(w) - y differ, a point
     * z = x<sub>1</sub> + (u - x<sub>1</sub>) (f(x<sub>1</sub> - y)
     * / (f(x<sub>1</sub>) - f(w)) is also considered. Note that z
     * is an interpolation point between x<sub>1</sub> and w.
     * The value of |f(x) - y| is compared at x<sub>1</sub>, w,
     * and (if calculated) z and the value of x<sub>n+1</sub>
     * is set to the one which provides the minimal value of |f(x)-y|.
     * <P>
     * This sequence converges when x<sub>n+1</sub> = x<sub>n</sub>,
     * and that must occur because there are a finite number of
     * double-precision numbers between f(x<sub>1</sub>) and 0.
     * <P>
     * The final step in the algorithm is to compute f(x) at adjacent
     * floating-point numbers and pick the one that provides the
     * minimum value for |f(x) - y|. The initial guess will be used
     * in this final step if the argument df is null.
     * @param f the function
     * @param df the function's derivative; null if only the final
     *        step is used
     * @param y the value for f(x)
     * @param guess an initial guess (assumed to be very close to
     *        the solution)
     * @return a value x such that f(x) = y to within floating-point
     *         errors
     */
    public static double refineSolution(DoubleUnaryOperator f,
					DoubleUnaryOperator df,
					double y, double guess)
    {
	if (Double.isNaN(guess)) {
	    throw new IllegalArgumentException(errorMsg("arg4NaN"));
	}
	if (df == null) return twiddleSolution(f, y, guess);
	double tmp = f.applyAsDouble(guess) - y;
	int count = 0;
	while (tmp != 0 && (count++) < REFINE_SOLUTION_LIMIT) {
	    double d = df.applyAsDouble(guess);
	    if (d == 0.0) break;
	    double tmp2 = guess - tmp/d;
	    if (Double.isNaN(tmp2)) break;
	    double tmp3 = f.applyAsDouble(tmp2) - y;
	    if (tmp3 != 0) {
		if (Math.signum(tmp) != Math.signum(tmp3)) {
		    // best value is in between these two?
		    double tmp4 = guess
			+ (tmp2 - guess)*(tmp/(tmp - tmp3));
		    double tmp5 = f.applyAsDouble(tmp4) - y;
		    double tmp5a = Math.abs(tmp5);
		    if (tmp5a < Math.abs(tmp) &&
			tmp5a < Math.abs(tmp3)) {
			tmp2 = tmp4;
			tmp3 = tmp5;
		    }
		}
	    }
	    if (Math.abs(tmp3) < Math.abs(tmp)) {
		guess = tmp2;
		tmp = tmp3;
		if (tmp == 0.0) {
		    break;
		}
	    } else {
		break;
	    }
	}
	return twiddleSolution(f, y, guess);
    }


    /**
     * Solves the cubic equation ax<sup>3</sup> + bx<sup>2</sup> + cx + d = 0.
     * The length of the array res should be large enough to hold all
     * of the real roots (a length of at least 3 should be used unless one
     * knows a priori that there are less than 3 real roots).
     * <P>
     * If multiple real roots have the same value, only one of those roots
     * is shown.  If roots are sufficiently close that they cannot be
     * distinguished due to floating-point limitations, only one root
     * may be shown even though multiple distinct roots actually exist.
     * This method duplicates the calling convention used by the method
     * {@link java.awt.geom.CubicCurve2D#solveCubic(double[],double[])}.
     * It was provided to break a dependency on the jdk.desktop module,
     * but experience with it indicates that is is more accurate than
     * the implementation of
     * {@link java.awt.geom.CubicCurve2D#solveCubic(double[],double[])}
     * provided in Java 11.0.1.
     * <P>
     * A cubic function
     * f(x) = ax<sup>3</sup> + bx<sup>2</sup> + cx + d has an
     * inflection point at -b/(3a). The number of critical
     * points is dependent on the value
     * &Delta;<sub>0</sub> = b<sup>2</sup> - 3ac:
     * <UL>
     *   <LI>if &Delta; is negative, there are no critical points
     *       and the cubic function is strictly monotonic. The
     *       implementation uses Newton's method, with the inflection
     *       point as an initial guess.
     *   <LI>if &Delta; is zero, there is one critical point, which
     *       is also an inflection point. There is only one real root,
     *       which is a triple root if the inflection point is a root,
     *       or a single root if the inflection point is not a root. In
     *       the later case, the root's value is
     *       (-b - (27ad - b<sup>3</sup>)<sup>1/3</sup> / (3a).
     *   <LI>if &Delta; is positive, there are two critical points
     *       with the inflection point between these two.  The
     *       critical points are
     *       $(-b - \sqrt{\Delta_0})/(3a)$
     *       <!--(-b - sqrt(&Delta;<sub>0</sub>))/(3a)-->
     *       and
     *       $(-b + \sqrt{\Delta_0})/(3a)$.
     *       <!--(-b + sqrt(&Delta;<sub>0</sub>))/(3a).-->
     *       If one of these is a root r (both cannot be roots), it
     *       is a double root, and the other root has the value
     *       -(2ar + b)/a. Otherwise, let x<sub>1</sub> be the lower of
     *       these two critical points and let x<sub>2</sub> be the
     *       higher of these two values.
     *       <UL>
     *         <LI> If f(x<sub>1</sub>) and f&Prime;(x<sub>1</sub>)
     *               have opposite signs, there is a real root below
     *               x<sub>1</sub>. One can find the root using Newton's
     *               method with an initial guess
     *               $x_g = x_1 - (\frac32)^m\sqrt{-2f(x_1)/f\prime(x_1)}$,
     *               <!-- x<sub>g</sub> = x<sub>1</sub> -
     *     (3/2)<sup>m</sup>sqrt(-2f&Prime;(x<sub>1</sub>)/f(x<sub>1</sub>),-->
     *               where m is the smallest non-negative integer such that
     *               f(x<sub>g</sub>) and f(x<sub>1</sub> have opposite
     *               signs.
     *         <LI> If f(x<sub>1</sub>) and f(x<sub>1</sub>) have
     *              opposite signs, there is a root between these two
     *              values. One can find the root using Newton's method
     *              with a lower and upper bound and with the inflection
     *              point as an initial guess. When upper and lower
     *              bounds are provided, the {@link RootFinder.Newton} class
     *              will temporarily switch to Brent's method when Newton's
     *              method's next value is out of range, and will tighten
     *              the upper and lower bounds as new values are tried.
     *         <LI> If f(x<sub>2</sub>) and f&Prime;(x<sub>2</sub>)
     *               have opposite signs, there is a real root above
     *               x<sub>2</sub>.  One can find the root using Newton's
     *               method with an initial guess
     *               $x_g = x_2 - (\frac32)^m\sqrt{-2f(x_2)/f\prime(x_2)}$,
     *               x<sub>g</sub> = x<sub>2</sub> -
     *     (3/2)<sup>m</sup>sqrt(-2f&Prime;(x<sub>2</sub>)/f(x<sub>2</sub>),
     *               where m is the smallest non-negative integer such that
     *               f(x<sub>g</sub>) and f(x<sub>2</sub> have opposite
     *               signs.
     *       </UL>
     * </UL>
     * @param eqn the input/output array containing the roots (starting at
     *        index 0) for output and the coefficients ordered so that
     *        eqn[0] = d, eqn[1] = c, eqn[2] = b, and eqn[3] = a.
     * @param res the output array containing the roots (starting at
     *        index 0) in numerical order
     * @return the number of roots; -1 if the coefficients a, b, and c are zero
     */
    public static int solveCubic(double[] eqn, double[] res) {
	double norm = eqn[3];

	if (norm == 0.0) {
	    // equation is actually a quadratic
	    return solveQuadratic(eqn, res);
	}

	double e0 = eqn[0];
	double e1 = eqn[1];
	double e2 = eqn[2];
	double e3 = eqn[3];

	if (e0 == 0.0) {
	    if (e1 == 0.0) {
		if (e2 == 0.0) {
		    res[0] = 0.0;
		    return 1;
		} else {
		    double r = -e2/e3;
		    if (r > 0) {
			res[0] = 0.0;
			res[1] = r;
		    } else {
			res[0] = r;
			res[1] = 0.0;
		    }
		    return 2;
		}
	    } else {
		double[] eqn2 = {e1, e2, e3};
		int n = solveQuadratic(eqn2);
		if (n <= 0) {
		    res[0] = 0.0;
		    return 1;
		} else if (n == 1) {
		    n = 2;
		    res[0] = 0.0;
		    res[1] = eqn2[0];
		} else {
		    n = 3;
		    res[0] = 0.0;
		    res[1] = eqn2[0];
		    res[2] = eqn2[1];
		}
		double tmp;
		if (n == 3 && res[1] > res[2]) {
		    tmp = res[2] ;
		    res[2] = res[1];
		    res[1] = tmp;
		}
		if (n >= 2 && res[0] > res[1]) {
		    tmp = res[1];
		    res[1] = res[0];
		    res[0]  = tmp;
		}
		if (n == 3 && res[1] > res[2]) {
		    tmp = res[2];
		    res[2] = res[1];
		    res[1] = tmp;
		}
		return n;
	    }
	} else if (e2 == 0.0) {
	    double a = e1/e3;
	    double b = e0/e3;
	    // avoid cases where we have to compute acos of
	    // a value close to 1.0 or -1.0.
	    if (a >= 0 || (27*b*b/4*Math.abs(a*a*a)) > 0.9) {
		return depressedCubic(eqn, res);
	    }
	}

	boolean hasIntCoefficients = Math.rint(e0) == e0
	    && Math.rint(e1) == e1 && Math.rint(e2) == e2
	    && Math.rint(e3) == e3;

	double ulp1 = Math.ulp(e1);
	double ulp2 = Math.ulp(e2);
	double ulp3 = Math.ulp(e3);

	double abs1 = Math.abs(e1);
	double abs2 = Math.abs(e2);
	double abs3 = Math.abs(e3);

	// double dlimit;
	// long idiscr = 0;
	long idelta0 = 0;
	double discr;
	// boolean safe_ilimit = false;
	boolean noOverflow = true;
	boolean safeDelta0 = false;

	if (hasIntCoefficients) {
	    // For integral coefficients, not too large, we can
	    // compute the discriminant exactly.  Otherwise we
	    // can compute it module 2^64.
	    long ie0 = Math.round(e0);
	    long ie1 = Math.round(e1);
	    long ie2 = Math.round(e2);
	    long ie3 = Math.round(e3);
	    long bb = ie2*ie2;
	    if (Math.abs(ie2) >= (1L)<<30) noOverflow = false;
	    long ac = ie3*ie1;
	    long ac3 =  3*ac;
	    boolean ie31t = (ie3 != 0) && (ie1 != 0);
	    if (ie31t && noOverflow) {
		int log2 = 2
		    + (64 - Long.numberOfLeadingZeros(Math.abs(ie3)-1))
		    + (64 - Long.numberOfLeadingZeros(Math.abs(ie1)-1));
		// leave a factor of two for the sum, so 62 instead of 63
		if (log2 >= 62) noOverflow = false;
	    }
	    safeDelta0 = noOverflow;
	    idelta0 = bb - ac3;
	    /*
	    boolean ac3Neg = ie31t && (Math.signum(ie3) != Math.signum(ie1));
	    if (ac3Neg) {
		if (idelta0 <= 0) {
		    // we use idelta0 only to determine whether it is
		    // negative, 0, or positive, so setting it to 1
		    // corrects for overflow.
		    idelta0 = 1;
		    noOverflow = true;
		    safeDelta0 = false;
		}
	    }
	    */
	}

	DoubleUnaryOperator f = (t) -> {
	    double prod = t;
	    double c = 0;
	    double sum = e0;
	    double y = e1*prod;
	    double tt = sum + y;
	    c = (tt - sum) - y;
	    sum = tt;
	    prod *= t;
	    y = (e2*prod) - c;
	    tt = sum + y;
	    c = (tt - sum) - y;
	    sum = tt;
	    prod *= t;
	    y = (e3*prod) -c;
	    tt = sum + y;
	    return tt;
	};

	DoubleUnaryOperator fastF = (t) -> {
	    // Fast version
	    double prod = t;
	    double sum = e0;
	    sum += e1*prod;
	    prod *= t;
	    sum += e2*prod;
	    prod *= t;
	    sum += e3*prod;
	    return sum;
	};

	DoubleUnaryOperator ferr = (t) -> {
	    t = Math.abs(t);
	    double prod = 1.0;
	    double et = Math.ulp(t);
	    double sum = Math.ulp(e0);
	    double prodErr = 1.0;
	    double factor = Math.abs(e1);
	    sum += Math.ulp(factor)*prod + et*Math.abs(factor);
	    prod *= t;
	    factor = Math.abs(e2);
	    sum += Math.ulp(factor)*prod + 2*et*factor;
	    prod *= t;
	    factor = Math.abs(e3);
	    sum += Math.ulp(factor)*prod + 3*et*factor;
	    return Math.scalb(sum, 5);
	};

	DoubleUnaryOperator df = (t) -> {
	    double prod = t;
	    double sum = e1;
	    sum += 2*e2*prod; prod *= t;
	    sum += 3*e3*prod;
	    return sum;
	};

	DoubleUnaryOperator d2f = (t) -> {
	    return 6*e3*t + 2*e2;
	};

	// System.out.println("discr = " + discr + ",dlimit = " + dlimit);
	double delta0 = (safeDelta0)? idelta0: (e2*e2 - 3*e3*e1);
	// System.out.println("delta0 = " + delta0 + ", idelta0 = " + idelta0);

	double ulpThree = Math.ulp(3.0);
	double edelta0 = safeDelta0? 0.0:
	    (2*ulp2*abs2 + ulpThree*e3*e1 + 3*abs1*ulp3 + 3 *ulp1*abs3);
	double threeE3 = 3*e3;
	int ncritical = 0;
	double cpt1 = Double.NEGATIVE_INFINITY;
	double cpt2 = Double.POSITIVE_INFINITY;
	double inflpt = -e2/threeE3;
	// test if delta0 is zero.
	if (Math.abs(delta0) <= edelta0) {
	    ncritical = 1;
	    cpt1 = inflpt;
	} else if (delta0 > 0.0) {
	    ncritical = 2;
	    double sqrtDelta0 = Math.sqrt(delta0);
	    cpt1 = (-e2 - sqrtDelta0)/threeE3;
	    cpt2 = (-e2 + sqrtDelta0)/threeE3;
	    if (cpt1 > cpt2) {
		double cptTmp = cpt1;
		cpt1 = cpt2;
		cpt2 = cptTmp;
	    }
	}
	int nroots = 0;
	if (ncritical == 0) {
	    // The cubic is strictly monotonic. Use the inflection point
	    // as a guess.
	    Newton rf = Newton.newInstance(fastF, df, ferr);
	    rf.setOptimized(false);
	    res[0] = refineSolution(f, df, 0.0, rf.solve(0.0, inflpt));
	    return 1;
	} else if (ncritical == 1) {
	    /*
	    System.out.println("cpt1 = " + cpt1 + ", value = "
			       + f.applyAsDouble(cpt1));
	    */
	    double root = (-e2 - Math.cbrt(27*e0*e3*e3 - e2*e2*e2))/(3*e3);
	    res[0] = refineSolution(f, df, 0.0, root);
	    /*
	    System.out.println("root = " + root + ", value = "
			       +f.applyAsDouble(root));
	    */
	    return 1;
	} else {
	    double cpt1val = f.applyAsDouble(cpt1);
	    double cpt1d2 = d2f.applyAsDouble(cpt1);
	    double cpt2val = f.applyAsDouble(cpt2);
	    double cpt2d2 = d2f.applyAsDouble(cpt2);

	    /*
	    System.out.println("cpt1 = " + cpt1 + ", value = " +cpt1val);
	    System.out.println("cpt2 = " + cpt2 + ", value = " + cpt2val);
	    System.out.println("cpt1d2 = " + cpt1d2);
	    System.out.println("cpt2d2 = " + cpt2d2);
	    */
	    boolean cpt1IsRoot = (Math.abs(cpt1val) < ferr.applyAsDouble(cpt1));
	    boolean cpt2IsRoot = (Math.abs(cpt2val) < ferr.applyAsDouble(cpt2));

	    if (cpt1IsRoot && cpt2IsRoot) {
		// should not happen unless floating point errors cause a
		// single root to appear as two roots. Pick the one that
		// works best, including the inflection point.
		double inflptval = f.applyAsDouble(inflpt);
		/*
		System.out.println("cpt1 = " + cpt1 + ", value = " +cpt1val);
		System.out.println("cpt2 = " + cpt2 + ", value = " + cpt2val);
		System.out.println("cpt1Err = " + ferr.applyAsDouble(cpt1));
		System.out.println("cpt2Err = " + ferr.applyAsDouble(cpt2));
		System.out.println("cpt1d2 = " + cpt1d2);
		System.out.println("cpt2d2 = " + cpt2d2);
		System.out.println("inflpt = " + inflpt
				   + ", value = " + inflptval);
		System.out.println("a  = " + e3);
		System.out.println("b  = " + e2);
		System.out.println("c  = " + e1);
		System.out.println("d  = " + e0);
		*/
		if (Math.abs(cpt1val) == Math.abs(cpt2val)) {
		    if (cpt1val < inflptval) {
			// pick cpt1
			res[0] = cpt1;
			return 1;
		    } else {
			// pick inflpt
			res[0] = inflpt;
			return 1;
		    }
		} else if (Math.abs(cpt1val) < Math.abs(cpt2val)) {
		    if (cpt1val < inflptval) {
			// pick cpt1
			res[0] = cpt1;
			return 1;
		    } else {
			// pick inflpt
			res[0] = inflpt;
			return 1;
		    }
		} else {
		    if (cpt2val < inflptval) {
			// pick cpt2
			res[0] = cpt2;
			return 1;
		    } else {
			// pick inflpt
			res[0] = inflpt;
			return 1;
		    }
		}
	    } else if (cpt1IsRoot) {
		// cpt1 is a root, so we have a double root.
		// Since we have another critical point, we also
		// have a single root.
		// System.out.println("double root at " + cpt1);
		res[0] = cpt1;
		res[1] = (2*cpt1*e3 + e2)/(-e3);
		/*
		System.out.println("single root at " +res[1] + ", val = "
				   + f.applyAsDouble(res[1]));
		*/
		if (res[1] < res[0]) {
		    double rtmp = res[0];
		    res[0] = res[1];
		    res[1] = rtmp;
		}
		return 2;
	    } else if (cpt2IsRoot) {
		// cpt2 is a root, so we have a double root.
		// Since we have another critical point, we also
		// have a single root.
		// System.out.println("double root at " + cpt2);
		res[0] = cpt2;
		res[1] = (2*cpt2*e3 + e2)/(-e3);
		/*
		System.out.println("single root at " +res[1] + ", val = "
				   + f.applyAsDouble(res[1]));
		*/
		if (res[1] < res[0]) {
		    double rtmp = res[0];
		    res[0] = res[1];
		    res[1] = rtmp;
		}
		return 2;
	    } else {
		// 1 or 3 roots
		if (Math.signum(cpt1val) != Math.signum(cpt2val)) {
		    // 3 real roots
		    double root;
		    double cpt1d2val = d2f.applyAsDouble(cpt1);
		    Newton rf = Newton.newInstance(fastF, df, ferr);
		    rf.setOptimized(false);
		    double rootval;
		    double diff = Math.sqrt(-2*cpt1val/cpt1d2val);
		    do {
			root = cpt1 - diff;
			rootval = f.applyAsDouble(root);
			diff *= 1.5;
		    } while (Math.signum(rootval) == Math.signum(cpt1val));
		    res[0] = refineSolution(f, df, 0.0,
					    rf.solve(0.0, root, root, cpt1));
		    res[1] = refineSolution(f, df, 0.0,
					    rf.solve(0.0, inflpt, cpt1, cpt2));
		    double cpt2d2val = d2f.applyAsDouble(cpt2);
		    diff = Math.sqrt(-2*cpt2val/cpt2d2val);
		    do {
			root = cpt2 + diff;
			rootval = f.applyAsDouble(root);
			diff *= 1.5;
		    } while (Math.signum(rootval) == Math.signum(cpt2val));
		    res[2] = refineSolution(f, df, 0.0,
					    rf.solve(0.0, root, cpt2, root));
		    return 3;
		} else {
		    // one real root
		    double root;
		    double rootval;
		    double diff;
		    double cpt1d2val = d2f.applyAsDouble(cpt1);
		    Newton rf = Newton.newInstance(fastF, df, ferr);
		    rf.setOptimized(false);
		    if (Math.signum(cpt1val) != Math.signum(cpt1d2val)) {
			diff = Math.sqrt(-2*cpt1val/cpt1d2val);
			do {
			    root = cpt1 - diff;
			    rootval = f.applyAsDouble(root);
			    diff *= 1.5;
			} while (Math.signum(rootval) == Math.signum(cpt1val));
			/*
			System.out.println
			    ("root = " + root + ", cpt1 = " + cpt1);
			System.out.println("cpt1d2val = " + cpt1d2val);
			*/
			root = refineSolution(f, df, 0.0,
					      rf.solve(0.0, root, root, cpt1));
		    } else {
			// must be the other critical point
			double cpt2d2val = d2f.applyAsDouble(cpt2);
			diff = Math.sqrt(-2*cpt2val/cpt2d2val);
			do {
			    root = cpt2 + diff;
			    rootval = f.applyAsDouble(root);
			    diff *= 1.5;
			} while (Math.signum(rootval) == Math.signum(cpt2val));
			root = refineSolution(f, df, 0.0,
					      rf.solve(0.0, root, cpt2, root));
		    }
		    /*
		    System.out.println("root = " + root + ", value = "
				       + f.applyAsDouble(root));
		    */
		    res[0] = root;
		    return 1;
		}
	    }
	}
    }

    private static double guessRoot(double[] eqn, DoubleUnaryOperator f,
				    int n, double x, double v0,
				    boolean up, boolean isInflected)
    {

	/*
	System.out.println("guessRoot: " + x + ", " + v0 + ", " + up
			   + ", isInflected = " + isInflected);
	*/
	double[] tmp = new double[n+1];
	System.arraycopy(eqn, 0, tmp, 0, n+1);

	/*
	RealValuedFunctOps f = (t) -> {
	    double prod = t;
	    double sum = eqn[0];
	    double c = 0.0;
	    for (int i = 1; i <= n; i++) {
		 double y = (prod*eqn[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};
	*/

	double sum = 0.0;
	int k;
	int scale = 5;
	for (k = 1; k <= n; k++) {
	    sum = tmp[k]/k;
	    double err = Math.ulp(sum);
	    double c = 0.0;
	    double prod = x;
	    double eprod = 1.0;
	    int prodExp = 1;
	    double xerr = Math.ulp(x);
	    for (int i = k+1; i <= n; i++) {
		tmp[i] *= (i-k+1);
		tmp[i] /= k;
		double y = (prod*tmp[i]) - c;
		err += (prodExp++)*xerr*eprod*Math.abs(tmp[i])
		    + Math.abs(prod)*Math.ulp(tmp[i]);
		double tot = sum + y;
		c = (tot - sum) - y;
		sum = tot;
		prod *= x;
		eprod *= Math.abs(x);
	    }
	    err = Math.scalb(err, scale);
	    // the value should be zero when k = 1 because we
	    // are supposed to be at a critical point.
	    if (k == 1) {
	    }
	    if (isInflected) {
		if (Math.abs(sum) > err) {
		    break;
		}
	    } else if (k == 1) {
		// We know the derivative is zero because this is
		// a critical point, not an inflection point.
		// So, if Math.abs(sum) > err, then the number scale
		// must have been too small.
		while(Math.abs(sum) > err) {
		    scale++;
		    err = Math.scalb(err, 1);
		}
		// System.out.println("scale = " + scale);
	    } else {
		if (Math.abs(sum) > err) {
		    break;
		}
	    }
	}
	// System.out.println(" ... sum = " + sum + ", k = " + k);
	if (sum == 0.0) return x; // indicates guess failed.

	double delta;
	double guess;
	if (k%2 == 0) {
	    double arg = -v0/sum;
	    if (arg <= 0.0) return x;
	    delta = MathOps.root(k, -v0/sum);
	    if (up == false ) {
		delta = - delta;
	    }
	    // System.out.println("delta = " + delta);
	    guess = x + delta;
	} else {
	    /*
	    System.out.println("calling MathOps.root: k = " + k + ", arg = "
			       + -v0/sum);
	    */
	    delta = MathOps.root(k, -v0/sum);
	    // System.out.println("delta = " + delta);
	    if ((up && delta > 0.0)
		|| (!up && delta < 0.0) ) {
		guess = x + delta;
	    } else {
		return x;
	    }
	}
	double v = f.applyAsDouble(guess);
	while (Math.signum(v0) == Math.signum(v)) {
	    if (Math.abs(v) > Math.abs(v0)) {
		return x;
	    }
	    guess += delta;
	    delta *= 1.5;
	    v = f.applyAsDouble(guess);
	}
	return guess;
    }

    private static double findPolyRoot(double[] eqn, int n, double guess) {
	RealValuedFunctOps f = (t) -> {
	    double prod = t;
	    double sum = eqn[0];
	    double c = 0.0;
	    for (int i = 1; i <= n; i++) {
		 double y = (prod*eqn[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps fastF = (t) -> {
	    double prod = t;
	    double sum = eqn[0];
	    for (int i = 1; i <= n; i++) {
		sum += (prod*eqn[i]);
		prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps ferr = (t) -> {
	    t = Math.abs(t);
	    double prod = 1.0;
	    double et = Math.ulp(t);
	    double sum = Math.ulp(eqn[0]);
	    double prodErr = 1.0;
	    for (int i = 1; i <= n; i++) {
		double factor = Math.abs(eqn[i]);
		sum += Math.ulp(factor)*prod + i*et*Math.abs(factor);
		prod *= t;
	    }
	    return Math.scalb(sum, 5);
	};

	RealValuedFunctOps df = (t) -> {
	    double prod = t;
	    double sum = eqn[1];
	    double c = 0.0;
	    for (int i = 2; i <= n; i++) {
		double y = (i*prod*eqn[i]) - c;
		double tot = sum + y;
		c = (tot - sum) - y;
		sum = tot;
		prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps fastDF = (t) -> {
	    double prod = t;
	    double sum = eqn[1];
	    for (int i = 2; i <= n; i++) {
		sum += i*prod*eqn[i];
		prod *= t;
	    }
	    return sum;
	};

	RootFinder.Newton rf = RootFinder.Newton
	    .newInstance(new RealValuedFunction(fastF, fastDF, null), ferr);
	rf.setOptimized(false);
	double root = refineSolution(f, df, 0.0, rf.solve(0.0, guess));
	    // System.out.println("root = " + root);
	return root;
    }

    private static double
	findPolyRoot(double[] eqn, int n, double x1, double x2)
    {
	if (x1 == x2) {
	    return findPolyRoot(eqn, n, x1);
	}

	RealValuedFunctOps f = (t) -> {
	    double prod = t;
	    double sum = eqn[0];
	    double c = 0.0;
	    for (int i = 1; i <= n; i++) {
		 double y = (prod*eqn[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps fastF = (t) -> {
	    double prod = t;
	    double sum = eqn[0];
	    for (int i = 1; i <= n; i++) {
		sum += prod*eqn[i];
		prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps ferr = (t) -> {
	    double prod = t;
	    double sum = Math.ulp(eqn[0]);
	    for (int i = 1; i <= n; i++) {
		sum += Math.ulp((prod*eqn[i]));
		prod *= t;
	    }
	    return Math.scalb(sum, 5);
	};

	RealValuedFunctOps df = (t) -> {
	    double prod = t;
	    double sum = eqn[1];
	    double c = 0.0;
	    for (int i = 2; i <= n; i++) {
		double y = (i*prod*eqn[i]) - c;
		double tot = sum + y;
		c = (tot - sum) - y;
		sum = tot;
		prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps fastDF = (t) -> {
	    double prod = t;
	    double sum = eqn[1];
	    for (int i = 2; i <= n; i++) {
		sum += (i*prod*eqn[i]);
		prod *= t;
	    }
	    return sum;
	};

	double guess = (x1 + x2) / 2.0;
	/*
	System.out.println("findPolyRoot guess = " + guess
			   + ", val = " + f.applyAsDouble(guess)
			   + ", df/dx = " + df.applyAsDouble(guess));
	*/
	RootFinder.Newton rf = RootFinder.Newton
	    .newInstance(new RealValuedFunction(fastF, fastDF, null), ferr);
	rf.setOptimized(false);
	/*
	double xl = Math.min(x1, x2);
	double xu = Math.max(x1, x2);
	*/
	double root = refineSolution(f, df, 0.0,
				      ((x1 == x2)? rf.solve(0.0, x1):
				       rf.solve(0.0, guess, x1, x2)));
	return root;
    }


    private static double
	findBezierRoot(int offset, double[] eqn, int n, double x1, double x2)
    {
	RealValuedFunctOps f = (t) -> {
	    return Functions.Bernstein.sumB(eqn, offset, n, t);
	};

	RealValuedFunctOps ferr = (t) -> {
	    // We can use a more stringent criterium for this case
	    // because we want the root as accurate as possible.
	    // A looser one is used to determine if a value is
	    // actually a root, specifically when critical points may
	    // be roots.
	    return Functions.Bernstein.sumBerr(eqn, offset, n, t);
	};

	if (x1 == x2) {
	    return x1;
	}


	RealValuedFunctOps df = (t) -> {
	    return Functions.Bernstein.dsumBdx(eqn, offset, n, t);
	};

	double guess = (x1 + x2) / 2.0;
	/*
	System.out.println("findPolyRoot guess = " + guess
			   + ", val = " + f.applyAsDouble(guess)
			   + ", df/dx = " + df.applyAsDouble(guess));
	*/
	RootFinder.Newton rf = RootFinder.Newton
	    .newInstance(new RealValuedFunction(f, df, null), ferr);

	return rf.solve(0.0, guess, x1, x2);
    }

    /**
     * Find the zeros of a {@link Polynomial}.
     * @param p the polynomial
     * @param res an array to hold the results
     * @return the number of roots; -1 if the degree of the
     *         polynomial is zero.
     * @exception IllegalArgumentException if the array
     *            argument is too short
     */
    public static int solvePolynomial(Polynomial p, double[] res) {
	return solvePolynomial(p.getCoefficientsArray(), p.getDegree(), res);
    }

    /**
     * Find the zeros of a polynomial of degree n.
     * @param eqn the coefficients of the polynomial for input,
     *        ordered so that eqn[i] provides the coefficient for
     *        x<sup>i</sup> where x is the polynomial's indeterminate
     * @param n the degree of the polynomial
     * @param res the roots that were found
     * @return the number of roots; -1 if the degree of the
     *         polynomial is zero.
     * @exception IllegalArgumentException n is negative or the array
     *            arguments are too short
     */
    public static int solvePolynomial(double[] eqn, int n, double[] res) {
	return solvePolynomial(eqn, n, res, null, false);
    }

    /**
     * Find the roots  and critical points of a polynomial of degree n.
     */
    private static int solvePolynomial(double[] eqn, int n,
				       double[] res, double[] cpts,
				       boolean skipQuartic)
	throws IllegalArgumentException
    {
	/*
	System.out.println("calling solvePolynomials (int n = " + n + ")");
	for (int i = 0; i <= n; i++) {
	    System.out.println(" ... eqn[" + i + "] = " + eqn[i]);
	}
	*/
	if (n < 0) {
	    String msg = errorMsg("argNonNegative2", n);
	    throw new IllegalArgumentException(msg);
	}
	if (eqn == null || eqn.length <= n) {
	    throw new IllegalArgumentException(errorMsg("argArray1TooShort"));
	}
	while (n > 0 && eqn[n] == 0.0) n--;
	switch (n) {
	case 0:
	    return -1;
	case 1:
	    if (res == null || res.length == 0) {
		String msg = errorMsg("argArray3TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[0] = -eqn[0] /eqn[1];
	    if (cpts != null) {
		res[1] = 0;
	    }
	    return 1;
	case 2:
	    if (cpts != null) {
		if (res == null || res.length < 2) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		int nr = solveQuadratic(eqn, res);
		cpts[0] = -eqn[1]/(2*eqn[2]);
		res[nr] = 1;
		return nr;
	    } else {
		return solveQuadratic(eqn, res);
	    }
	case 3:
	    if (cpts != null) {
		cpts[0] = eqn[1];
		cpts[1] = 2*eqn[2];
		cpts[2] = 3*eqn[3];
		int ni = solveQuadratic(cpts,cpts);
		int nr = solveCubic(eqn, res);
		if (res == null || res.length <= nr) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[nr] = ni;
		return nr;
	    } else {
		return solveCubic(eqn, res);
	    }
	case 4:
	    if (skipQuartic || eqn[3] != 0.0 || cpts != null) {
		// depressedQuartic uses solvePolynomial if the
		// analysis of the types of roots fails (e.g.,
		// because of floating point errors).  When
		// this occurs, the skipQuartic argument is
		// set to true as a signal that we should not
		// call solveQuartic and instead find a solution
		// numerically.
		//
		// depressedQuartic is not used if cpts != null because
		// we would have to solve a cubic and a quadratic to
		// get both the critical points and the inflection points.
		break;
	    }
	    return depressedQuartic(eqn, res);
	default:
	    break;
	}
	if (eqn[0] == 0.0) {
	    // 0.0 is a root, so we can solve a lower-order case.
	    // System.out.println("Can factor out x");
	    double[] neweqn = new double[n];
	    System.arraycopy(eqn, 1, neweqn, 0, n);
	    /*
	    for (int i = 0; i <= n; i++) {
		System.out.println("eqn[" + i + "] = " + eqn[i]);
	    }
	    for (int i = 0; i < n; i++) {
		System.out.println("neweqn[" + i + "] = " + neweqn[i]);
	    }
	    */
	    int nroots = solvePolynomial(neweqn, n-1, res, cpts, skipQuartic);
	    int ni = (cpts != null)? (int)res[nroots]: 0;
	    // System.out.println("nroots = " + nroots + ", ni = " + ni);
	    /*
	    for (int i = 0; i < nroots; i++) {
		System.out.println(" ... new root: " + res[i]);
	    }
	    */
	    if (nroots == 0) {
		if (res == null || res.length == 0) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = 0.0;
		if (cpts != null) {
		    res[1] = ni;
		}
		return 1;
	    }
	    boolean shifted = false;
	    boolean noZero = true;
	    int offset = nroots -1;
	    for (int i = 0; i < nroots; i++) {
		double root = res[i];
		if (root == 0.0) {
		    noZero = false;
		    break;
		} else if (root > 0.0) {
		    shifted = true;
		    if (res.length <= offset+1) {
			String msg = errorMsg("argArray3TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[offset+1] = res[offset];
		    offset--;
		}
	    }
	    offset++;
	    if (shifted) {
		res[offset] = 0.0;
	    } else if (noZero){
		if (res.length <= nroots) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[nroots] = 0.0;
	    }
	    /*
	    for (int i = 0; i < nroots + ((shifted)? 0: 1); i++) {
		System.out.println("returning root " + res[i]);
	    }
	    */
	    nroots +=  ((shifted || noZero)? 1: 0);
	    if (cpts != null) {
		res[nroots] = ni;
	    }
	    return nroots;
	}
	// see if we solve a lower-order polynomial in x^2 instead of x
	// or just compute a root.
	boolean ttCase = true;
	boolean rsqCase = (n%2 == 0);
	for (int i = 1; i <= n; i ++) {
	    if (eqn[i] != 0.0) {
		if (i%2 == 1) rsqCase = false;
		if (i < n) ttCase = false;
	    }
	}
	if (ttCase) {
	    // System.out.println("ttCase");
	    double tmp = -eqn[0] / eqn[n];
	    if (n == 1) {
		if (res.length < 1) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = tmp;
		if (cpts != null) {
		    res[1] = 0;
		}
		return 1;
	    } else if (n%2 == 0) {
		if (tmp < 0) {
		    if (cpts != null) {
			cpts[0] = 0.0;
			res[0] = 1;
		    }
		    return 0;
		}
		if (tmp == 0.0) {
		    if (res.length < 1) {
			String msg = errorMsg("argArray3TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[0] = 0.0;
		    if (cpts != null) {
			cpts[0] = 0.0;
			res[1] = 1;
		    }
		    return 1;
		} else {
		    if (res.length < 2) {
			String msg = errorMsg("argArray3TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    double r = MathOps.root(n, tmp);
		    res[0] = -r;
		    res[1] = r;
		    if (cpts != null) {
			cpts[0] = 0.0;
			res[2] = 1;
		    }
		    return 2;
		}
	    } else {
		if (res.length < 1) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = MathOps.root(n, tmp);
		if (cpts != null) {
		    tmp /= (n-1);
		    if (tmp == 0) {
			cpts[0] = 0;
			res[1] = 1;
		    } else if (tmp < 0) {
			res[1] = 0;
		    } else {
			double r = MathOps.root(n-1, tmp);
			cpts[0] = r;
			cpts[1] = -r;
			res[1] = 2;
		    }
		}
		return 1;
	    }
	} else if (rsqCase) {
	    // System.out.println("rsqCase");
	    double[] array = new double[n/2 + 1];
	    double[] carray = (cpts == null)? null: new double[n/2];
	    for (int i = 0; i <= n; i += 2) {
		array[i/2] = eqn[i];
	    }
	    int nr = solvePolynomial(array, n/2, array, carray, false);
	    int ni = (cpts == null)? 0: (int)Math.round(array[nr]);
	    if (cpts != null) {
		int j = 0;
		cpts[j++] = 0.0;
		for (int i = 0; i < ni; i++) {
		    double r2 = carray[i];
		    if (r2 <= 0.0) continue;
		    double r = Math.sqrt(r2);
		    cpts[j++] = r;
		    cpts[j++] = -r;
		}
		Arrays.sort(cpts, 0, j);
	    }
	    if (nr > 0) {
		int j = 0;
		if (res.length < nr) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		for (int i = 0; i < nr; i++) {
		    double r2  = array[i];
		    if (r2 < 0.0) continue;
		    if (r2 == 0.0) {
			res[j++] = 0.0;
		    } else {
			double r = Math.sqrt(r2);
			res[j++] = r;
			res[j++] = -r;
		    }
		}
		Arrays.sort(res, 0, j);
		if (cpts != null) {
		    res[j] = ni;
		}
		return j;
	    } else {
		if (cpts != null) {
		    res[0] = ni;
		}
		return 0;
	    }
	}

	// N > 3 because we already took care of the lower-order polynomials
	final int N = n;
	final double[] EQN = (res == eqn)? new double[n+1]: eqn;
	if (EQN != eqn) {
	    System.arraycopy(eqn, 0, EQN, 0, n+1);
	}

	DoubleUnaryOperator f = (t) -> {
	    double prod = t;
	    double sum = EQN[0];
	    double c = 0.0;
	    for (int i = 1; i <= N; i++) {
		 double y = (prod*EQN[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};

	RealValuedFunctOps fastF = (t) -> {
	    double prod = t;
	    double sum = EQN[0];
	    for (int i = 1; i <= N; i++) {
		sum += prod*EQN[i];
		prod *= t;
	    }
	    return sum;
	};

	DoubleUnaryOperator ferr = (t) -> {
	    double prod = t;
	    double sum = Math.ulp(EQN[0]);
	    for (int i = 1; i <= N; i++) {
		sum += Math.ulp((prod*EQN[i]));
		 prod *= t;
	    }
	    return Math.scalb(sum, 5);
	};

	DoubleUnaryOperator df = (t) -> {
	    double prod = t;
	    double sum = EQN[1];
	    double c = 0.0;
	    for (int i = 2; i <= N; i++) {
		 double y = (i*prod*EQN[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};

	DoubleUnaryOperator d2f = (t) -> {
	    double prod = t;
	    double sum = 2*EQN[2];
	    double c = 0.0;
	    for (int i = 3; i <= N; i++) {
		double y = (i*(i-1)*prod*EQN[i]) - c;
		 double tot = sum + y;
		 c = (tot - sum) - y;
		 sum = tot;
		 prod *= t;
	    }
	    return sum;
	};

	// Find the critical points.
	cpts = (cpts == null)? new double[n+1]: cpts;
	// ... and the inflection points
	double[] ipts = new double[n-1];
	for (int i = 1; i <= n; i++) {
	    cpts[i-1] = i*EQN[i];
	}
	// System.out.println("(solving for critical points)");
	int ncpts = solvePolynomial(cpts, n-1, cpts, ipts, false);
	int nipts = (ncpts == -1)? 0: (int) Math.round(cpts[ncpts]);
	// failed to find any critical points, so assume there are none
	if (ncpts < 0) ncpts = 0;
	/*
	System.out.println("ncpts = " + ncpts + ", nipts = " + nipts);
	System.out.println("critical points (for n = " + n + ")");
	for (int i = 0; i < ncpts; i++) {
	    System.out.println("    " + cpts[i] + ", f(x) = "
			       + f.applyAsDouble(cpts[i])
			       + ", df/dx = " + df.applyAsDouble(cpts[i])
			       + ", d2f/dx = " +d2f.applyAsDouble(cpts[i]));
	}
	System.out.println("inflection points (for n = " + n + ")");
	for (int i = 0; i < nipts; i++) {
	    System.out.println("    " + ipts[i] + ", f(x) = "
			       + f.applyAsDouble(ipts[i])
			       + ", df/dx = " + df.applyAsDouble(ipts[i])
			       + ", d2f/dx = " +d2f.applyAsDouble(ipts[i]));
	}
	*/
	if (ncpts == 0) {
	    // no critical points, so we must have a zero.
	    double x1 = 0.0;
	    double x2 = 0.0;
	    if (nipts == 1) {
		x1 = ipts[0];
		x2 = ipts[0];
	    } else if (nipts != 0) {
		double delta = 1.0;
		x1 = ipts[0];
		/*
		if (nipts <= 0) {
		    System.out.println("nipts = " + nipts);
		    for (int i = 0; i < EQN.length; i++) {
			System.out.println("eqn[" + i +"] = " + EQN[i]);
		    }
		}
		*/
		x2 = ipts[nipts-1];
		double val1 = f.applyAsDouble(x1);
		double v1 = Math.signum(val1);
		double val2 = f.applyAsDouble(x2);
		double v2 = Math.signum(val2);
		// System.out.println("initial x1 = "+ x1 + ", val = " + val1);
		// System.out.println("initial x2 = "+ x2 + ", val = " + val2);
		if (v1 != v2) {
		    int j = 1;
		    double v = Math.signum(f.applyAsDouble(ipts[j]));
		    while (v == v1) {
			x1 = ipts[j];
			v = Math.signum(f.applyAsDouble(ipts[++j]));
		    }
		    j = nipts -1;
		    do {
			x2 = ipts[j];
			v = Math.signum(f.applyAsDouble(ipts[--j]));
		    } while (v == v2);
		} else {
		    if (Math.abs(val1) < Math.abs(val2)) {
			x2 = x1;
		    } else {
			x1 = x2;
		    }
		}
	    }
	    // now find a root.
	    /*
	    System.out.println("nipts = " + nipts);
	    System.out.println("x1 = "+ x1 + ", val = " + f.applyAsDouble(x1));
	    System.out.println("x2 = "+ x2 + ", val = " + f.applyAsDouble(x2));
	    */
	    if (res.length < 0) {
		String msg = errorMsg("argArray3TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[0] = findPolyRoot(EQN, n, x1, x2);
	    if (res.length > 1) {
		res[1] = ncpts;
	    }
	    return 1;
	}

	// pts will contain a sorted array of both critical points and
	// inflection points, represented by their X values.
	double[] pts = (nipts == 0)? cpts: new double[ncpts+nipts];
	// array indicating if a point is an inflection point.
	boolean[] isip = new boolean[ncpts+nipts];
	int npts;
	if (pts != cpts) {
	    int i = 0;
	    int j = 0;
	    int k = 0;
	    while (i < ncpts || j < nipts) {
		double x1 = (i >= ncpts)? Double.POSITIVE_INFINITY: cpts[i];
		double x2 = (j >= nipts)? Double.POSITIVE_INFINITY: ipts[j];
		/*
		System.out.println("i = " + i + ", j = " + j + ", k = " + k
				   + ", x1 = " + x1 + ", x2 = " + x2);
		*/
		if (x1 == x2) {
		    // System.out.println("choosing x1/x2");
		    pts[k++] = x1;
		    i++; j++;
		} else if (x1 < x2) {
		    // System.out.println("choosing x1");
		    pts[k++] = x1;
		    i++;
		} else {
		    // System.out.println("choosing x2");
		    isip[k] = true;
		    pts[k++] = x2;
		    j++;
		}
	    }
	    npts = k;
	} else {
	    npts = ncpts;
	}
	/*
	System.out.println("critical points + inflection points:");
	for (int i = 0; i < npts; i++) {
	    System.out.println("    " + pts[i] + ", isInflected = " + isip[i]);
	}
	*/
	// Note: two zeros must have at least one critical points and
	// possibly some inflection points between those two zeros.

	int ptind = 0;
	int resind = 0;
	double x = pts[ptind];
	boolean isInflection = isip[ptind];
	double lcp = Double.NEGATIVE_INFINITY;
	double val = f.applyAsDouble(x);
	double err = ferr.applyAsDouble(x);
	if (Math.abs(val) < err) {
	    if (res.length <= resind) {
		String msg = errorMsg("argArray3TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[resind++] = x;
	    /*
	    System.out.println("set res[" + (resind-1) + "] to "
			       + res[resind-1]);
	    */
	    ptind++;
	    if (ptind == npts) {
		if (res.length > resind) {
		    res[resind] = ncpts;
		}
		return resind;
	    } else {
		// Two successive critical/inflection points can't be zeros
		// so we arrange for the current critical point to
		// be one that is not a zero.
		// x = pts[resind];
		x = pts[ptind];
		isInflection = isip[ptind];
		val = f.applyAsDouble(x);
		err = ferr.applyAsDouble(x);
	    }
	} else {
	    // double d2 = d2f.applyAsDouble(x);
	    double guess = guessRoot(EQN, fastF, n, x, val,
				     false, isInflection);
	    // System.out.println("guess = " + guess + ", x = " + x);
	    /*
	    System.out.println("guess = " + guess
			       + "val = " + f.applyAsDouble(guess)
			       + "deriv = " + df.applyAsDouble(guess)
			       + "deriv2 = " + d2f.applyAsDouble(guess));
	    */;
	    if (guess != x) {
		// The first root is below the first critical/inflection point.
		/*
		System.out.println("x = " + x + ", val = " +f.applyAsDouble(x));
		System.out.println("guess = " + guess
				   + ", val = " +f.applyAsDouble(guess));
		*/
		if (res.length <= resind) {
		    String msg = errorMsg("argArray3TooShort");
		    throw new IllegalArgumentException(msg);
		}
		if (Math.signum(f.applyAsDouble(x))
		    != Math.signum(f.applyAsDouble(guess))) {
		    res[resind++] = findPolyRoot(EQN, N, guess, x);
		} else {
		    res[resind++] = findPolyRoot(EQN, N, guess);
		}
		/*
		System.out.println("set res[" + (resind-1) + "] to "
				   + res[resind-1]);
		*/
	    } else {
		// We still have to find the first root
		// System.out.println("still searching for first root");
		boolean more = true;
		do {
		    ptind++;
		    if (ptind == npts) {
			// System.out.println("last critical/inflection point");
			guess = guessRoot(EQN, fastF,
					  n, x, val, true, isInflection);
			// System.out.println("guess = " +guess +", x = " +x);
			if (guess > x) {
			    if (res.length <= resind) {
				String msg = errorMsg("argArray3TooShort");
				throw new IllegalArgumentException(msg);
			    }
			    if (Math.signum(f.applyAsDouble(x))
				!= Math.signum(f.applyAsDouble(guess))) {
				res[resind++] = findPolyRoot(EQN, N, x, guess);
			    } else {
				res[resind++] = findPolyRoot(EQN, N, guess);
			    }
			    if (resind > 1 && res[resind-1] == res[resind-2]) {
				resind--;
			    }
			    /*
			    System.out.println("set res[" + (resind-1) + "] to "
					       + res[resind-1]);
			    */
			}
			if (res.length > resind) {
			    res[resind] = ncpts;
			}
			return resind;
		    }
		    double x2 = pts[ptind];
		    boolean isInflection2 = isip[ptind];
		    double v = f.applyAsDouble(x2);
		    double ev = ferr.applyAsDouble(x2);
		    if (Math.abs(v) < ev) {
			/*
			System.out.println("critical/inflection point is a "
					   + "root");
			*/
			if (res.length <= resind) {
			    String msg = errorMsg("argArray3TooShort");
			    throw new IllegalArgumentException(msg);
			}
			res[resind++] = refineSolution(f, df, 0.0, x2);
			if (resind > 1 && res[resind-1] == res[resind-2]) {
			    resind--;
			}
			/*
			System.out.println("set res[" + (resind-1) + "] to "
					   + res[resind-1]);
			*/
			ptind++;
			// Two successive critical/inflection points can't
			// be zeros so we arrange for the current
			// critical/inflection point to be one that is
			// not a zero.
			if (ptind < npts) {
			    x = pts[ptind];
			    // System.out.println("setting x to " + x);
			    isInflection = isip[ptind];
			    val = f.applyAsDouble(x);
			    err = ferr.applyAsDouble(x);
			} else {
			    /*
			    System.out.println("no more critical/inflection "
					      +  "points, so return");
			    */
			    if (res.length > resind) {
				res[resind] = ncpts;
			    }
			    return resind;
			}
			more = false;
		    } else if (Math.signum(val) != Math.signum(v)) {
			// the first root is between x and x2.
			/*
			System.out.println("first root beween " +x
					   + " and " + x2);
			*/
			if (res.length <= resind) {
			    String msg = errorMsg("argArray3TooShort");
			    throw new IllegalArgumentException(msg);
			}
			res[resind++] = findPolyRoot(EQN, N, x, x2);
			if (resind > 1 && res[resind-1] == res[resind-2]) {
			    resind--;
			}
			/*
			System.out.println("set res[" + (resind-1) + "] to "
					   + res[resind-1]);
			*/
			x = x2;
			isInflection  = isInflection2;
			val = v;
			err = ev;
			more = false;
		    } else {
			x = x2;
			isInflection  = isInflection2;
			val = v;
			err = ev;
		    }
		} while (more);
	    }
	}
	// At this point, x is a critical or inflection point and that point
	// is not a zero.
	/*
	System.out.println("... searching more critical points"
			   + ", x = " + x);
	*/
	while (ptind < npts) {
	    ptind++;
	    if (ptind == npts) {
		double guess = guessRoot(EQN, fastF,
					 N, x, val, true, isInflection);
		/*
		System.out.println("guess = " + guess + ", x = " + x);
		System.out.println("val at guess = " + f.applyAsDouble(guess));
		*/
		if (guess > x) {
		    if (res.length <= resind) {
			String msg = errorMsg("argArray3TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    if (Math.signum(f.applyAsDouble(x)) !=
			Math.signum(f.applyAsDouble(guess))) {
			    res[resind++] = findPolyRoot(EQN, N, x, guess);
			} else {
			    res[resind++] = findPolyRoot(EQN, N, guess);
			}
		    if (resind > 1 && res[resind-1] == res[resind-2]) {
			resind--;
		    }
		    /*
		    System.out.println("set res[" + (resind-1) + "] to "
				       + res[resind-1]);
		    */
		    if (res.length > resind) {
			res[resind] = ncpts;
		    }
		    return resind;
		}
	    } else {
		double x2 = pts[ptind];
		// System.out.println("trying interval [" + x +", " +x2 +"]");
		boolean isInflection2 = isip[ptind];
		double v = f.applyAsDouble(x2);
		double ev = ferr.applyAsDouble(x2);
		if (Math.abs(v) < ev) {
		    /*
		    System.out.println("critical/inflection point is a root: "
				       + x2
				       + ", ptind = " + ptind
				       + ", v = " + v +", ev = " + ev);
		    */
		    if (res.length <= resind) {
			String msg = errorMsg("argArray3TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[resind++] = refineSolution(f, null, 0.0, x2);
		    if (resind > 1 && res[resind-1] == res[resind-2]) {
			resind--;
		    }
		    /*
		    System.out.println("set res[" + (resind-1) + "] to "
				       + res[resind-1]);
		    */
		    ptind++;
		    if (ptind == npts) {
			// if the last critical/inflection point is a root,
			// there are no more roots.
			if (res.length > resind) {
			    res[resind] = ncpts;
			}
			return resind;
		    }
		    // since the previous critical/inflection point was a root,
		    // this one cannot be a root.
		    x = pts[ptind];
		    isInflection = isip[ptind];
		    val = f.applyAsDouble(x);
		    err = ferr.applyAsDouble(x);
		} else {
		    // System.out.println("checking (" +x + ", " + x2 + ")");
		    if (Math.signum(val) != Math.signum(v)) {
			// System.out.println("bracketing");
			if (res.length <= resind) {
			    String msg = errorMsg("argArray3TooShort");
			    throw new IllegalArgumentException(msg);
			}
			res[resind++] = findPolyRoot(EQN, N, x, x2);
			/*
			System.out.println("set res[" + (resind-1) + "] to "
					   + res[resind-1]);
			*/
			if (resind > 1 && res[resind-1] == res[resind-2]) {
			    resind--;
			}
		    }
		    x = x2;
		    isInflection = isInflection2;
		    val = v;
		    err = ev;
		}
	    }
	}
	if (res.length > resind) {
	    res[resind] = ncpts;
	}
	return resind;
    }

    /**
     * Solve the quartic equation
     * ax<sup>4</sup> + bx<sup>3</sup> + cx<sup>2</sup> + dx + e = 0
     * and store the results in the argument array.
     * The algorithm is based on the description in the Wikipedia article
     * on the
     * <A href="https://en.wikipedia.org/wiki/Quartic_function">quartic function</A>.
     * <P>
     * A number of special cases are handled explicitly and for
     * coefficients that are integer values, a computation of the
     * discriminant is performed using two's complement arithmetic
     * with long integers.  This is equivalent to modular arithmetic
     * (modulo 2<sup>64</sup>) and allows one to reliably determine if
     * the discriminant is zero, and to improve the accuracy of
     * determining if the discriminant is positive, negative, or
     * zero. Also, the real-valued form of the discriminant uses Kahan's
     * additional algorithm to improve the accuracy of the sum.The same
     * procedure is used for some other quantities as well. These
     * are combined to determine the number of roots based on whether
     * these quantities are positive, zero, or negative. Cases that are
     * not covered should not occur unless as the result of floating point
     * errors, in which case a numerical method is used instead. To
     * further reduce floating-point errors, Newton's method and some
     * floating-point adjustments are made to improve the accuracy of
     * the solutions.  In addition, for cases where there are known to
     * be multiple roots, the critical points are computed by solving
     * a cubic equation and those solutions are used when they are
     * also solutions. If floating-point errors split a single multiple
     * root of the polynomial into multiple zeros, the critical points are
     * used to eliminate these spurious solutions.
     * @param eqn an array containing the coefficients of the polynomial,
     *        where eqn[0] = e, eqn[1] = d, eqn[2] = c, eqn[3] = d, and
     *        eqn[4] = e (the array length must be at least 5), also used
     *        as the output array
     * @return the number of real-valued solutions; -1 if a, b, c, and d
     *         are 0
     */
    public static int solveQuartic(double[] eqn) {
	return solveQuartic(eqn, eqn);
    }


    /**
     * Solve the quartic equation
     * ax<sup>4</sup> + bx<sup>3</sup> + cx<sup>2</sup> + dx + e = 0
     * and store the results in an output array.
     * The algorithm is based on the description in the Wikipedia article
     * on the
     * <A href="https://en.wikipedia.org/wiki/Quartic_function">quartic function</A>.
     * <P>
     * A number of special cases are handled explicitly and for
     * coefficients that are integer values, a computation of the
     * discriminant is performed using two's complement arithmetic
     * with long integers.  This is equivalent to modular arithmetic
     * (modulo 2<sup>64</sup>) and allows one to reliably determine if
     * the discriminant is zero, and to improve the accuracy of
     * determining if the discriminant is positive, negative, or
     * zero. Also, the real-valued form of the discriminant uses Kahan's
     * additional algorithm to improve the accuracy of the sum.The same
     * procedure is used for some other quantities as well. These
     * are combined to determine the number of roots based on whether
     * these quantities are positive, zero, or negative. Cases that are
     * not covered should not occur unless as the result of floating point
     * errors, in which case a numerical method is used instead. To
     * further reduce floating-point errors, Newton's method and some
     * floating-point adjustments are made to improve the accuracy of
     * the solutions.  In addition, for cases where there are known to
     * be multiple roots, the critical points are computed by solving
     * a cubic equation and those solutions are used when they are
     * also solutions. If floating-point errors split a single multiple
     * root of the polynomial into multiple zeros, the critical points are
     * used to eliminate these spurious solutions.
     * @param eqn an array containing the coefficients of the polynomial,
     *        where eqn[0] = e, eqn[1] = d, eqn[2] = c, eqn[3] = d, and
     *        eqn[4] = e (the array length must be at least 5)
     * @param res an array whose length is at least 4 that will contain
     *         the results
     * @return the number of real-valued solutions; -1 if a, b, c, and d
     *         are 0
     */
    private static int solveQuartic(double[] eqn, double[] res) {
	double a = eqn[4];
	if (a == 0.0) {
	    return solveCubic(eqn, res);
	}

	double b = eqn[3];
	double c = eqn[2];
	double d = eqn[1];
	double e = eqn[0];

	// special cases.
	if (e == 0.0) {
	    // System.out.println("e == 0.0 case");
	    if (res.length <= 4) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[0] = d;
	    res[1] = c;
	    res[2] = b;
	    res[3] = a;
	    int nq = solveCubic(res);
	    for (int i = 0; i < nq; i++) {
		if (res[i] == 0.0) {
		    for (int j = i+1; j < nq; j++) {
			res[j-1] = res[j];
		    }
		    i--;
		    nq--;
		}
	    }
	    if (nq == 0 || res[nq-1] != 0.0) {
		res[nq++] = 0.0;
	    }
	    Arrays.sort(res, 0, nq);
	    return nq;
	}

	if (b == 0.0 && d == 0.0) {
	    if (res.length <= 2) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[0] = e;
	    res[1] = c;
	    res[2] = a;
	    int nq = solveQuadratic(res);
	    int ii = nq;
	    for (int i = 0; i < nq; i++) {
		double root2 = res[i];
		if (root2 != 0) {
		    double root = Math.sqrt(Math.abs(root2));
		    res[i] = root;
		    res[ii++] = - root;
		}
	    }
	    Arrays.sort(res, 0, ii);
	}

	long ia = Math.round(a);
	long ib = Math.round(b);
	long ic = Math.round(c);
	long id = Math.round(d);
	long ie = Math.round(e);

	boolean hasIntCoefficients =
	    (Math.rint(a) == a && Math.rint(b) == b && Math.rint(c) == c
	     && Math.rint(d) == d && Math.rint(e) == e);
	long iDelta = 0;
	long iP = 0;
	long iR = 0;
	long iDelta0 = 0;
	long iD = 0;
	boolean safeInts = false;
	if (hasIntCoefficients) {
	    long aia = Math.abs(ia);
	    long aib = Math.abs(ib);
	    long aic = Math.abs(ic);
	    long aid = Math.abs(id);
	    long aie = Math.abs(ie);

	    /* - temporarily delete for testing
	    long gcd = MathOps.gcd(aia, aib);
	    gcd = MathOps.gcd(gcd, aic);
	    gcd = MathOps.gcd(gcd, aid);
	    gcd = MathOps.gcd(gcd, aie);
	    if (gcd > 1) {
		ia /= gcd;
		ib /= gcd;
		ic /= gcd;
		id /= gcd;
		ie /= gcd;
		a = ia;
		b = ib;
		c = ic;
		d = id;
		e = ie;
	    }
	    */
	    if (aia < Long.MAX_VALUE/6 && aib < Long.MAX_VALUE/6
		&& aic < Long.MAX_VALUE/6 && aid < Long.MAX_VALUE/6
		&& aie < Long.MAX_VALUE/6) {
		safeInts = true;
	    }
	    long ia2 = ia*ia;
	    long ib2 = ib*ib;
	    long ic2 = ic*ic;
	    long id2 = id*id;
	    long ie2 = ie*ie;
	    iDelta = 256*ia2*ia*ie2*ie - 192*ia2*ib*id*ie2 - 128*ia2*ic2*ie2
		+ 144*ia2*ic*id2*ie - 27*ia2*id2*id2
		+ 144*ia*ib2*ic*ie2 - 6*ia*ib2*id2*ie - 80*ia*ib*ic2*id*ie
		+ 18*ia*ib*ic*id2*id + 16*ia*ic2*ic2*ie
		- 4*ia*ic2*ic*id2 - 27*ib2*ib2*ie2 + 18*ib2*ib*ic*id*ie
		- 4*ib2*ib*id2*id - 4*ib2*ic2*ic*ie + ib2*ic2*id2;
	    iP = 8*ia*ic - 3*ib2;
	    iR = ib2*ib +  8*id*ia2 - 4*ia*ib*ic;
	    iDelta0 = ic2 - 3*ib*id + 12*ia*ie;

	    iD = 64*ia2*ia*ie - 16*ia2*ic2 + 16*ia*ib2*ic - 16*ia2*ib*id
		- 3*ib2*ib2;
	}

	double a2 = a*a;
	double b2 = b*b;
	double c2 = c*c;
	double d2 = d*d;
	double e2 = e*e;
	double a3 = a2*a;
	double b3 = b2*b;
	double c3 = c2*c;
	double d3 = d2*d;
	double e3 = e2*e;


	// <https://en.wikipedia.org/wiki/Quartic_function#
	// Solving_a_quartic_equation>
	// We use Kahan's addition algorithm to get the quantities
	// Delta, P, Q, R, Delta0, and D as accurately as we can
	// manage.

	double Delta = 256*a3*e3;
	double errDelta = Math.ulp(Delta);
	double y = -192*a2*b*d*e2;
	errDelta += Math.ulp(y);
	double tt = Delta + y;
	double cc = (tt - Delta) - y;
	Delta = tt;
	double term = -128*a2*c2*e2;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = 144*a2*c*d2*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -27*a2*d3*d;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = 144*a*b2*c*e2;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -6*a*b2*d2*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -80*a*b*c2*d*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = 18*a*b*c*d3;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = 16*a*c3*c*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -4*a*c3*d2;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -27*b3*b*e2;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = 18*b3*c*d*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -4*b3*d3;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = -4*b2*c3*e;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	term = b2*c2*d2;
	errDelta += Math.ulp(term);
	y = term - cc;
	tt = Delta + y;
	cc = (tt - Delta) - y;
	Delta = tt;
	if (hasIntCoefficients) {
	    if (iDelta == 0 && Math.abs(Delta) < Math.scalb(errDelta,10)) {
		Delta = 0;
	    }
	} else if (Math.abs(Delta) < errDelta) {
	    Delta = 0;
	}

	double Pterm1 = 8*a*c;
	double Pterm2 = 3*b2;
	double P = Pterm1 - Pterm2;
	double errP = Math.ulp(Pterm1) + Math.ulp(Pterm2);
	if (hasIntCoefficients) {
	    if (iP == 0 && Math.abs(P) < Math.scalb(errP,10)) P = 0;
	} else if (Math.abs(P) < errP) {
	    P = 0;
	}

	/*
	System.out.format("a = %g, b = %g, c = %g, d = %g, e = %g\n",
			  a, b, c, d, e);
	System.out.format("a2 = %g, b2 = %g, c2 = %g, d2 = %g, e2 = %g\n",
			  a2, b2, c2, d2, e2);
	System.out.format("a3 = %g, b3 = %g, c3 = %g, d3 = %g, e3 = %g\n",
			  a3, b3, c3, d3, e3);
	*/
	double R = b3;
	double errR = Math.ulp(R);
        y = 8*d*a2;
	errR += Math.ulp(y);
	tt = R + y;
	cc = (tt - R) - y;
	R = tt;
	term = -4*a*b*c;
	errR += Math.ulp(term);
	y = term - cc;
	tt = R + y;
	cc = (tt - R) - y;
	R = tt;
	// System.out.println("R = " + R  +", iR = " + iR);
	if (hasIntCoefficients) {
	    if (iR == 0 && Math.abs(R) < Math.scalb(errR,10)) R = 0;
	} else if (Math.abs(R) < errR) {
	    R = 0;
	}

	double Delta0 = c2;
	double errDelta0 = Math.ulp(c2);
	y = -3*b*d;
	errDelta0 += Math.ulp(y);
	tt = Delta0 + y;
	cc = (tt - Delta0) - y;
	Delta0 = tt;
	term = 12*a*e;
	errDelta0 += Math.ulp(term);
	y = term - cc;
	tt = Delta0 + y;
	Delta0 = tt;
	if (hasIntCoefficients) {
	    if (iDelta0 == 0 && Math.abs(Delta0) < Math.scalb(errDelta0,10)) {
		Delta0 = 0;
	    }
	} else if (Math.abs(Delta0) < errDelta0) {
	    Delta0 = 0;
	}

	double D = 64*a3*e;
	double errD = Math.ulp(D);
	y = -16*a2*c2;
	errD += Math.ulp(y);
	tt = D + y;
	cc = (tt - D) - y;
	D = tt;
	term = 16*a*b2*c;
	errD += Math.ulp(term);
	y = term - cc;
	tt = D + y;
	cc = (tt - D) - y;
	D = tt;
	term = -16*a2*b*d;
	errD += Math.ulp(term);
	y = term - cc;
	tt = D + y;
	cc = (tt - D) - y;
	D = tt;
	term = -3*b3*b;
	errD += Math.ulp(term);
	y = term - cc;
	tt = D + y;
	D = tt;
	if (hasIntCoefficients) {
	    if (iD == 0 && Math.abs(D) < Math.scalb(errD,10)) D = 0;
	} else if (Math.abs(D) < errD) {
	    D = 0;
	}
	/*
	System.out.println("Delta = " + Delta);
	System.out.println("P = " + P);
	System.out.println("R = " + R);
	System.out.println("Delta0 = " + Delta0);
	System.out.println("D = " + D);
	*/
	final double afin = a;
	final double bfin = b;
	final double cfin = c;
	final double dfin = d;
	final double efin = e;

	DoubleUnaryOperator f = (t) -> {
	    double prod = t;
	    double sum = efin;
	    double yy = dfin*prod;
	    double ttt = sum + yy;
	    double ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t;
	    yy = (cfin*prod) - ccc;
	    ttt = sum + yy;
	    ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t;
	    yy = (bfin*prod) -ccc;
	    ccc = (ttt - sum) - yy;
	    ttt = sum + yy;
	    prod *= t;
	    yy = (afin*prod) - ccc;
	    ttt = sum + yy;
	    return ttt;
	};

	DoubleUnaryOperator ferr = (t) -> {
	    double prod = t;
	    double sum = Math.ulp(efin);
	    sum += Math.ulp(dfin*prod);
	    prod *= t;
	    sum += Math.ulp(cfin*prod);
	    prod *= t;
	    sum += Math.ulp(bfin*prod);
	    prod *= t;
	    sum += Math.ulp(afin*prod);
	    return sum;
	};

	DoubleUnaryOperator df = (t) -> {
	    double sum = dfin;
	    double prod = t;
	    double yy = 2*cfin*prod;
	    double ttt = sum + yy;
	    double ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t;
	    yy = (3*bfin*prod) - ccc;
	    ccc = (ttt - sum) - yy;
	    ttt = sum + yy;
	    prod *= t;
	    yy = (4*afin*prod) - ccc;
	    ttt = sum + yy;
	    return ttt;
	};


	int nr = -1; // if not changed, case analysis failed.
	int resind = 0;
	int multiple = 0;
	double mroot = 0.0;
	if (Delta < 0) {
	    // two distinct real roots & two complex conjugate non-real roots
	    // System.out.println("case 1: nr = 2");
	    nr = 2;
	} else if (Delta > 0) {
	    if (P < 0 && D < 0) {
		// four distinct real roots
		// System.out.println("case 2: nr = 2");
		nr = 4;
	    } else if (P > 0 || D > 0) {
		// two paris of non-real complex conjugate roots
		// System.out.println("case 3: nr = 0");
		return 0;
	    }
	} else if (Delta == 0) {
	    if (P < 0 && D < 0 && Delta0 != 0) {
		// double real root & two real simple roots
		// System.out.println("case 4: nr = 3");
		multiple = 1;
		nr = 3;
		if (res.length <= 3) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = d;
		res[1] = 2*c;
		res[2] = 3*b;
		res[3] = 4*a;
		int nn = solveCubic(res);
		double rmin = Double.POSITIVE_INFINITY;
		for (int i = 0; i < nn; i++) {
		    double aval = Math.abs(f.applyAsDouble(res[i]));
		    if (aval < rmin) {
			rmin = aval;
			mroot = res[i];
		    }
		}
		// System.out.println("mroot = " + mroot);
	    } else if ((D > 0) || ((P > 0) && (D != 0 || R != 0))) {
		// double real root and two complex conjugate roots
		multiple = 1;
		// System.out.println("case 5: nr = 1");
		if (hasIntCoefficients && safeInts) {
		    if (res.length <= 3) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[3] = 4*ia;
		    res[2] = 3*ib;
		    res[1] = 2*ic;
		    res[0] = id;
		} else {
		    if (res.length <= 3) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[3] = 4*a;
		    res[2] = 3*b;
		    res[1] = 2*c;
		    res[0] = d;
		}
		int n = solveCubic(res);
		int ind = -1;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; i++) {
		    double aval = Math.abs(f.applyAsDouble(res[i]));
		    if (aval < min) {
			min = aval;
			ind = i;
		    }
		}
		if (ind != 0) {
		    if (res.length < 1) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[0] = res[ind];
		}
		return 1;
	    } else if (Delta0 == 0 && D != 0) {
		// triple real root and a real simple root
		multiple = 1;
		// System.out.println("case 6: nr = 2");
		if (res.length <= 2) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		if (hasIntCoefficients && safeInts) {
		    res[2] = 6*ia;
		    res[1] = 3*ib;
		    res[0] = c;
		} else {
		    res[2] = 6*a;
		    res[1] = 3*b;
		    res[0] = c;
		}
		int n = solveQuadratic(res);
		/*
		for (int i = 0; i < n; i++) {
		    System.out.println("... res[" + i + "] = "
				       + res[i] + ", f(x) = "
				       + f.applyAsDouble(res[i]));
		}
		*/
		if (n == 0) {
		    // case analysis failed due to floating-point
		    // errors, Fall back onto a numerical solution.
		    if (eqn == res) {
			eqn[4] = a;
			eqn[3] = b;
			eqn[2] = c;
			eqn[1] = d;
			eqn[0] = e;
		    }
		    return solvePolynomial(eqn, 4, res, null, true);
		}
		if (n == 2) {
		    if (Math.abs(f.applyAsDouble(res[0])) >
			Math.abs(f.applyAsDouble(res[1]))) {
			res[0] = res[1];
		    }
		    res[1] = refineSolution(f, null, 0.0, -b/a - 3*res[0]);
		}
		return 2;
	    } else if (D == 0) {
		if (P < 0) {
		    // System.out.println("case 7: nr = 2");
		    // two real double roots
		    multiple = 2;
		    if (res.length <= 3) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    if (hasIntCoefficients && safeInts) {
			res[0] = id;
			res[1] = 2*ic;
			res[2] = 3*ib;
			res[3] = 4*ia;
		    } else {
			res[0] = d;
			res[1] = 2*c;
			res[2] = 3*b;
			res[3] = 4*a;
		    }
		    int n = solveCubic(res);
		    if (n < 3) {
			// close to a case transition.
			if (eqn == res) {
			    eqn[4] = a;
			    eqn[3] = b;
			    eqn[2] = c;
			    eqn[1] = d;
			    eqn[0] = e;
			}
			return solvePolynomial(eqn, 4, res, null, true);
		    }
		    int ind = -1;
		    double max = -1.0;
		    if (res.length < n) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    for (int i = 0; i < n; i++) {
			double aval = Math.abs(f.applyAsDouble(res[i]));
			if (aval > max) {
			    max = aval;
			    ind = i;
			}
		    }
		    switch (ind) {
		    case 0:
			res[0] = res[1];
			// fall through
		    case 1:
			res[1]  = res[2];
			// fall through
		    case 2:
			break;
		    }
		    return 2;
		} else if (P > 0 && R == 0) {
		    // System.out.println("case 8: nr = 0");
		    // two complex conjugate double roots
		    return 0;
		} else if (Delta0 == 0) {
		    if (res.length < 1) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    multiple = 1;
		    // System.out.println("case 9: nr = 1");
		    // one root equal to -b/(4a).
		    res[0] = -b/(4*a);
		    return 1;
		}
	    }
	}


	if (nr == -1) {
	    // None of the cases fit, which indicates that
	    // we can't determine the number of roots due to
	    // floating-point errors.  Use a numerical analysis
	    // instead.
	    return solvePolynomial(eqn, 4, res, null, true);
	}

	// for reduced quartic
	double p = (8*c*a -3*b2)/(8*a2);
	// double q = (b3 - 4*c*b*a + 8*d*a2)/(8*a3);
	double q = b3;
	y = -4*a*b*c;
	tt = q + y;
	cc = (tt - q) - y;
	q = tt;
	y = (8*d*a2) - cc;
	q += y;
	q /= 8*a3;
	// double r = (-3*b3*b + 256*e*a3 - 64*d*b*a2 + 16*c*b2*a)/(256*a3*a);
	double r = -3*b3*b;
	y = 256*e*a3;
	tt = r  + y;
	cc = (tt - r) - y;
	r = tt;
	y = (-64*d*b*a2) - cc;
	tt = r + y;
	cc = (tt - r) - y;
	r = tt;
	y = (16*c*b2*a) - cc;
	r += y;
	r /= (256*a3*a);

	/*
	System.out.println("Depressed Quartic: y^4 + py^2 + qy + r = 0");
	System.out.println("    p = " + p);
	System.out.println("    q = " + q);
	System.out.println("    r = " + r);
	*/
	double offset = -b/(4*a);

	// System.out.println(" -b/(4a) = " + offset);

	double tmp[] = {-q*q, 2*p*p - 8*r, 8*p, 8.0};
	/*
	System.out.println("Resolvent Cubic: "
			   + "8m^3 + 8pm^2 + (2p^2 - 8)m - q^2)");
	System.out.println("            8 = " + tmp[3]);
	System.out.println("           8p = " + tmp[2]);
	System.out.println("    (2p^2-8r) = " + tmp[1]);
	System.out.println("         -q^2 = " + tmp[0]);
	*/
	/*
	if (mroot != 0) {
	    for (int i = 0; i < 4; i++) {
		System.out.println("tmp[" + i + "] = " + tmp[i]);
	    }
	}
	*/
	// System.out.println("SOLVING CUBIC");
	int n = solveCubic(tmp);
	// System.out.println("SOLVED CUBIC");
	double m = Double.NEGATIVE_INFINITY;

	// System.out.println("Roots of Resolvent Cubic:");
	for (int i = 0; i < n; i++) {
	    /*
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + (-q*q +(2*p*p - 8*r)*tmp[i]
				  + 8*p*tmp[i]*tmp[i]
				  + 8*tmp[i]*tmp[i]*tmp[i]));
	    */
	    if (tmp[i] > m) {
		m = tmp[i];
	    }
	}
	// System.out.println("m = " + m);
	if (m == 0) {
	    // System.out.println("q = " + q);
	    if (Math.abs(q) > Math.scalb(Math.ulp(q), 5)) {
		String msg = errorMsg("floatingPointErr");
		throw new RuntimeException(msg);
	    }
	    tmp[0] = r;
	    tmp[1] = p;
	    tmp[2] = 1.0;
	    n = solveQuadratic(tmp);
	    // System.out.println("solveQuadratic(tmp) returned " + n);
	    for (int i = 0; i < n; i++) {
		// System.out.println("tmp[" + i +"] = " + tmp[i]);
		double rr = tmp[i];
		if (rr == 0.0) {
		    res[resind++] = offset;
		} else if (rr > 0) {
		    rr = Math.sqrt(rr);
		    res[resind++] = -rr + offset;
		    res[resind++] = rr + offset;
		}
	    }
	    Arrays.sort(res, 0, resind);
	    return resind;
	} else if (m < 0) {
	    return 0;
	}
	double r2m = Math.sqrt(2*m);
	double pterm = p/2.0 + m;
	double qterm = q/(2*r2m);
	tmp[0] = pterm - qterm;
	tmp[1] = r2m;
	tmp[2]  = 1.0;
	// System.out.println("Solve y^2 + p/2 + m + sqrt(2m)y - q/(2sqrt(2m):");
	n = solveQuadratic(tmp);
	/*
	for (int i = 0; i < n; i++) {
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + ((pterm-qterm) + r2m*tmp[i] + tmp[i]*tmp[i]));
	}
	*/
	// System.out.println("... found " + n + " solutions");
	for (int i = 0; i < n; i++) {
	    res[resind++] = refineSolution(f, df, 0.0, tmp[i] + offset);
	    /*
	    System.out.println("    " + res[resind-1]
			       + ", was " + (tmp[i] + offset));
	    */
	}
	tmp[0] = pterm + qterm;
	tmp[1] = -r2m;
	tmp[2] = 1.0;
	// System.out.println("Solve y^2 + p/2 + m - sqrt(2m)y +1/(2sqrt(2m):)");
	n = solveQuadratic(tmp);
	/*
	for (int i = 0; i < n; i++) {
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + ((pterm+qterm) - r2m*tmp[i] + tmp[i]*tmp[i]));
	}
	*/
	// System.out.println("... found " + n + " solutions");
	for (int i = 0; i < n; i++) {
	    if (res.length <= resind) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[resind++] = refineSolution(f, df, 0.0, tmp[i] + offset);
	    /*
	    System.out.println("    " + res[resind-1]
			       + ", was " + (tmp[i] + offset));
	    */
	}
	// check for spurious solutions and eliminate them.
	for (int i = 0; i < resind; i++) {
	    double rt = res[i];
	    double err = Math.scalb(ferr.applyAsDouble(rt), 5);
	    if (Math.abs(f.applyAsDouble(rt)) > err) {
		/*
		System.out.println("eliminating " + rt + " because "
				   + Math.abs(f.applyAsDouble(rt))
				   + " > " + err);
		*/
		res[i--] = res[--resind];
	    }
	}
	Arrays.sort(res, 0, resind);
	if (resind > nr) {
	    for (int i = 1; i < resind; i++) {
		if (res[i-1] == res[i]) {
		    for (int j = i; j < resind; j++) {
			res[j-1] = res[j];
		    }
		    i--;
		    resind--;
		}
	    }
	    if (mroot != 0) {
	    }
	}
	if (nr == 3 && resind > nr) {
	    if (mroot < res[0]) {
		res[0] = mroot;
		res[1] = res[2];
		res[2] = res[3];
		resind = nr;
	    } else if (mroot > res[nr]) {
		res[2] = mroot;
		resind = nr;
	    } else {
		int j = -1;
		for (int i = 0; i < resind; i++) {
		    if (res[i] == mroot) {
			j  = i;
			break;
		    }
		}
		if (j != -1) {
		    if (j == 0) {
			res[1] = res[2];
			res[2] = res[3];
		    } else if (j == nr) {
			res[nr-1] = mroot;
		    } else {
			double dist1 = Math.abs(res[j-1] - mroot);
			double dist2 = Math.abs(res[j+1] = mroot);
			if (dist1 < dist2) {
			    res[j-1] = mroot;
			    for (int i = j; i < nr; i++) {
				res[i] = res[i+1];
			    }
			} else {
			    for (int i = j+1; i < nr; i++) {
				res[i] = res[i+1];
			    }
			}
		    }
		    resind = nr;
		} else {
		    j = -1;
		    for (int i = 0; i < nr; i++) {
			if (res[i] < mroot && res[i+1] > mroot) {
			    j = i;
			    break;
			}
		    }
		    res[j] = mroot;
		    for (int i = j+1; i < nr; i++) {
			res[i] = res[i+1];
		    }
		    resind = nr;
		}
	    }
	}
	return resind;
    }

    // Case in which eqn[2] = 0, eqn[3] != 0, and eqn[0] != 0.
    //

    private static int depressedCubic(double[] eqn, double[] res) {

	double norm = eqn[3];

	if (norm == 0.0) {
	    // equation is actually a quadratic
	    return solveQuadratic(eqn, res);
	}

	double e0 = eqn[0];
	double e1 = eqn[1];
	// double e2 = eqn[2];
	double e3 = eqn[3];

	boolean noOverflow = true;
	boolean hasIntCoefficients = Math.rint(e0) == e0
	    && Math.rint(e1) == e1 && Math.rint(e3) == e3;

	double ulp0 = Math.ulp(e0);
	double ulp1 = Math.ulp(e1);
	// double ulp2 = Math.ulp(e2);
	double ulp3 = Math.ulp(e3);

	double abs0 = Math.abs(e0);
	double abs1 = Math.abs(e1);
	// double abs2 = Math.abs(e2);
	double abs3 = Math.abs(e3);

	double dlimit;
	long idiscr = 0;
	double discr;

	if (hasIntCoefficients) {
	    // For integral coefficients, not too large, we can
	    // compute the discriminant exactly.  Otherwise we
	    // can compute it module 2^64.
	    long ie0 = Math.round(e0);
	    long ie1 = Math.round(e1);
	    // long ie2 = Math.round(e2);
	    long ie3 = Math.round(e3);
	    long aie0 = Math.abs(ie0);
	    long aie1 = Math.abs(ie1);
	    long aie3 = Math.abs(ie3);

	    long gcd = aie0;
	    if (ie1 != 0) gcd = MathOps.gcd(gcd, aie1);
	    if (ie3 != 0) gcd = MathOps.gcd(gcd, aie3);
	    if (gcd != 1) {
		ie0 /= gcd;
		ie1 /= gcd;
		ie3 /= gcd;
		aie0 /= gcd;
		aie1 /= gcd;
		aie3 /= gcd;
	    }

	    boolean notSafe = aie0 >= CUBIC_SAFE_ILIMIT
		|| aie1 >= CUBIC_SAFE_ILIMIT || aie3 >= CUBIC_SAFE_ILIMIT;
	    // idiscr = -(4*ie3*ie1*ie1*ie1 + 27*ie3*ie3*ie0*ie0);
	    boolean term1nz = (ie3 != 0) && (ie1 != 0);
	    if (term1nz && notSafe) {
		int log2 = 2 + (64 - Long.numberOfLeadingZeros(aie3-1))
		    + 3 * (64 - Long.numberOfLeadingZeros(aie1-1));
		// 62 instead of 63 to leave a factor of two for the sum
		if (log2 >= 62) noOverflow = false;
	    }
	    long term1 = 4*ie3*ie1*ie1*ie1;

	    if (notSafe && ie3 != 0 && ie1 != 0 && noOverflow) {
		int log2 = 5
		    + 2 * (64 - Long.numberOfLeadingZeros(aie3-1))
		    + 2 * (64 - Long.numberOfLeadingZeros(aie0-1));
		// 62 instead of 63 to leave a factor of two for the sum
		if (log2 >= 62) noOverflow = false;
	    }
	    long term2 = 27*ie3*ie3*ie0*ie0;

	    idiscr = term1 + term2;
	    if (noOverflow) {
		if (term1 > 0 && term2 > 0) {
		    if (idiscr < 0) noOverflow = false;
		} else if (term1 < 0 && term2 < 0) {
		    if (idiscr >= 0) noOverflow = false;
		}
	    }
	    idiscr = -idiscr;
	    /*
	    System.out.println("idiscr = " + idiscr + ", noOverflow = "
			       + noOverflow);
	    double ourdiscr = - (4*e3*e1*e1*e1 + 27*e3*e3*e0*e0);
	    System.out.println("ourdiscr = " + ourdiscr);
	    long idiscr2 = -(4*ie3*ie1*ie1*ie1 + 27*ie3*ie3*ie0*ie0);
	    if (idiscr != idiscr2) throw new RuntimeException("idiscr");
	    */
	}
	if (hasIntCoefficients && noOverflow) {
	    discr = (double)idiscr;
	    dlimit = 0;
	} else  {
	    // Use an addition algorithm to compute the discriminant as
	    // accurately as we can manage. See
	    // <https://en.wikipedia.org/wiki/Cubic_function
	    // #General_solution_to_the_cubic_equation_with_real_coefficients>
	    // for the discriminate and how to use it.
	    discr = 4*e3*e1*e1*e1;
	    dlimit = Math.ulp(discr);
	    double kterm = 27*e3*e3*e0*e0;
	    dlimit += Math.ulp(kterm);
	    discr += kterm;
	    discr = -discr;
	    dlimit = Math.scalb(dlimit, 5);
	}

	DoubleUnaryOperator f = (t) -> {
	    double prod = t;
	    double c = 0;
	    double sum = e0;
	    double y = e1*prod;
	    double tt = sum + y;
	    c = (tt - sum) - y;
	    sum = tt;
	    prod *= t*t;
	    y = (e3*prod) -c;
	    tt = sum + y;
	    // c = (tt - sum) - y;
	    // sum = tt;
	    // return sum;
	    return tt;
	};

	DoubleUnaryOperator df = (t) -> {
	    double prod = t;
	    double sum = e1;
	    prod *= t;
	    sum += 3*e3*prod;
	    return sum;
	};

	if (Math.abs(discr) <= dlimit) {
	    double delta0 = - 3*e3*e1;
	    double ulpThree = Math.ulp(3.0);
	    double edelta0 = ulpThree*e3*e1 + 3*abs1*ulp3 + 3 *ulp1*abs3;

	    if (Math.abs(delta0) < edelta0) {
		if (res.length < 1) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = 0.0;
		return 1;
	    } else  {
		if (res.length < 2) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		double r1 = (9*e3*e0)/(2*delta0);
		double r2 = -(9*e3*e3*e0) / (e3 * delta0);
		r1 = refineSolution(f, df, 0.0, r1);
		r2 = refineSolution(f, df, 0.0, r2);
		if (r1 < r2) {
		    res[0] = r1;
		    res[1] = r2;
		} else {
		    res[0] = r2;
		    res[1] = r1;
		}
		return 2;
	    }
	}

	// See C.R.C Standard Mathematical Tables, 12th edition,
	// page 358 for solutions using a reduced cubic.
	// Using that reference's notation, p = 0.

	double a = eqn[1]/norm;
	double b = eqn[0]/norm;

	// special cases - just in case the previous one didn't
	// handle these.
	if (eqn[1] == 0.0) {
	    if (res.length < 1) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    if (eqn[0] == 0.0) {
		res[0] = 0.0;
	    } else if (b < 0) {
		res[0] = Math.cbrt(-b);
	    } else {
		res[0] = -Math.cbrt(b);
	    }
	    return 1;
	} else if (eqn[0] == 0.0) {
	    // a == 0 case handled by eqn[1] == 0;.0 case above
	    if (res.length < 2) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    if (a < 0) {
		res[0] = 0.0;
		res[1] = -a;
	    } else {
		res[0] = -a;
		res[1] = 0.0;
	    }
	    return 2;
	}
	// normal case
	double sqrtTerm1 = b*b/4;
	double sqrtTerm2 = a*a*a/27;
	double sqrtTerm = sqrtTerm1 + sqrtTerm2;
	double limit = Math.abs(b)*Math.ulp(b/2);
	limit +=  (a*a)*(Math.ulp(a)/3 + Math.abs(a)*Math.ulp(1.0/3))/3;
	limit = Math.scalb(limit, CUBIC_ERR_SF);
	if (discr < 0 && sqrtTerm < 0) {
	    // This can only occur if dlimit was too small to detect
	    // that the discriminant should have been zero to within
	    // floating-point limits. The code was copied from the
	    // discr == 0 case.
	    double delta0 = - 3*e3*e1;
	    double ulpThree = Math.ulp(3.0);
	    double edelta0 = ulpThree*e3*e1 + 3*abs1*ulp3 + 3 *ulp1*abs3;

	    if (Math.abs(delta0) <= edelta0) {
		if (res.length < 1) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[0] = 0.0;
		return 1;
	    } else {
		if (res.length < 2) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		double r1 = (9*e3*e0)/(2*delta0);
		double r2 = -(9*e3*e3*e0) / (e3 * delta0);
		r1 = refineSolution(f, df, 0.0, r1);
		r2 = refineSolution(f, df, 0.0, r2);
		if (r1 < r2) {
		    res[0] = r1;
		    res[1] = r2;
		} else {
		    res[0] = r2;
		    res[1] = r1;
		}
		return 2;
	    }
	} else if (discr < 0) {
	    if (res.length < 1) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    double negb2 = -b/2;
	    sqrtTerm = Math.sqrt(sqrtTerm);

	    double term = negb2 + sqrtTerm;
	    if (term > 0) {
		term = Math.cbrt(term);
	    } else if (term < 0) {
		term = -Math.cbrt(-term);
	    }
	    double A = term;
	    term = negb2 - sqrtTerm;
	    if (term > 0) {
		term = Math.cbrt(term);
	    } else if (term < 0) {
		term = -Math.cbrt(-term);
	    }
	    double B = term;
	    double root = A + B;
	    res[0] = refineSolution(f, df, 0.0, root);
	    return 1;
	} else {
	    // Trigonometric solution
	    if (res.length < 3) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    double negb2 = -b/2;
	    double sqrt = Math.sqrt(-sqrtTerm2);
	    double ratio = negb2 / sqrt;
	    // in case of floating-point errors
	    if (ratio > 1.0) ratio = 1.0;
	    if (ratio < -1.0) ratio = -1.0;
	    boolean close = Math.abs(ratio) > .999999999;
	    double phi = Math.acos(ratio);
	    double term = 2*Math.sqrt(-a/3);
	    double phi3 = phi/3;
	    double lim1 = Math.ulp(negb2);
	    double lim2 = Math.ulp(1.0/sqrt);
	    double lim = ratio
		* Math.sqrt(lim1*lim1/(sqrt*sqrt) + lim2*lim2*negb2*negb2);
	    lim = Math.scalb(lim, CUBIC_ERR_SF);

	    DoubleUnaryOperator g = (t) -> {
		double prod = t;
		double sum = b;
		sum += a*prod; prod *= t*t;
		sum += prod;
		return sum;
	    };

	    DoubleUnaryOperator gErr = (t) -> {
		double prod = t;
		double err = Math.ulp(b);
		double ea = Math.ulp(a);
		double et = Math.ulp(prod);
		err += et*Math.abs(a) + ea*Math.abs(t);
		err += 3*et;
		return Math.scalb(err, CUBIC_ERR_SF);
	    };

	    double cosphi3 = Math.cos(phi3);
	    double sinphi3 = Math.sin(phi3);
	    res[0] = term*cosphi3;
	    res[1] = term*(cosphi3*COS120 - sinphi3*SIN120);
	    res[2] = term*(cosphi3*COS240 - sinphi3*SIN240);

	    // Arrays.sort(res, 0, 3);
	    double tmp;
	    if (res[1] > res[2]) {
		tmp = res[2] ;
		res[2] = res[1];
		res[1] = tmp;
	    }
	    if (res[0] > res[1]) {
		tmp = res[1];
		res[1] = res[0];
		res[0]  = tmp;
	    }
	    if (res[1] > res[2]) {
		tmp = res[2];
		res[2] = res[1];
		res[1] = tmp;
	    }
	    int nzeros = 3;
	    boolean cplower = false;
	    if (close) {
		// a == 0 case handled previously.
		double cp2 = Math.sqrt(-a/3);
		double gcpe = gErr.applyAsDouble(cp2);
		double cp1 = -cp2;
		double gcp2 = Math.abs(g.applyAsDouble(cp2));
		double gcp1 = Math.abs(g.applyAsDouble(cp1));
		// with two distinct critical points, we have to test for
		// the case where one is a double root.  Both cannot be
		// roots because you would need another critical point
		// in between, and there are only two at most.
		if (gcp2 < gcpe) {
		    nzeros--;
		    res[1] = cp2;
		} else if (gcp1 < gcpe) {
		    nzeros--;
		    res[0] = cp1;
		    res[1] = res[2];
		    cplower = true;
		}
	    }

	    // try to improve the accuracy using a modified Newton's method.
	    for (int i = 0; i < nzeros; i++) {
		if (nzeros < 3) {
		    // Don't try to fix up a double root - Newton's
		    // method won't work very well and the computation of
		    // the root's location was a simple one, so the
		    // result should be very close. In this case we
		    // just use refineSolution to check adjacent floating
		    // point values.
		    if ((i == 0 && cplower) || (i == 1 && !cplower)) {

			res[i] = refineSolution(f, null, 0.0, res[i]);
			continue;
		    }
		}
		res[i] = refineSolution(f, df, 0.0, res[i]);
	    }
	    // Sort again just in case the ordering changed.
	    if (nzeros == 3 && res[1] > res[2]) {
		tmp = res[2] ;
		res[2] = res[1];
		res[1] = tmp;
	    }
	    if (nzeros >= 2 && res[0] > res[1]) {
		tmp = res[1];
		res[1] = res[0];
		res[0]  = tmp;
	    }
	    if (nzeros == 3 && res[1] > res[2]) {
		tmp = res[2];
		res[2] = res[1];
		res[1] = tmp;
	    }
	    return nzeros;
	}
    }

    // This handles a speical case:
    // eqn[3] is zero and eqn[1] is not zero.
    // eqn[4] is not zero as well (otherwise we are solving a quadratic
    // equation.

    private static final long SAFE_QUARTIC_LIMIT = 426;

    private static int depressedQuartic(double[] eqn, double[] res) {
	double a = eqn[4];
	if (a == 0.0) {
	    return solveCubic(eqn, res);
	}

	// double b = eqn[3];
	double c = eqn[2];
	double d = eqn[1];
	double e = eqn[0];

	long ia = Math.round(a);
	long ic = Math.round(c);
	long id = Math.round(d);
	long ie = Math.round(e);

	boolean hasIntCoefficients =
	    (Math.rint(a) == a && Math.rint(c) == c
	     && Math.rint(d) == d && Math.rint(e) == e);
	long iDelta = 0;
	long iP = 0;
	long iR = 0;
	long iDelta0 = 0;
	long iD = 0;
	boolean noOverflow = true;
	boolean safeInts = false;
	if (hasIntCoefficients) {
	    long aia = Math.abs(ia);
	    // long aib = Math.abs(ib);
	    long aic = Math.abs(ic);
	    long aid = Math.abs(id);
	    long aie = Math.abs(ie);

	    long gcd = aia;
	    //  if (aib != 0) gcd = MathOps.gcd(gcd, aib);
	    if (aic != 0) gcd = MathOps.gcd(gcd, aic);
	    if (aid != 0) gcd = MathOps.gcd(gcd, aid);
	    if (aie != 0) gcd = MathOps.gcd(gcd, aie);
	    if (gcd > 1) {
		ia /= gcd;
		// ib /= gcd;
		ic /= gcd;
		id /= gcd;
		ie /= gcd;
		a = ia;
		// b = ib;
		c = ic;
		d = id;
		e = ie;
	    }

	    boolean notSafe = aia >= SAFE_QUARTIC_LIMIT
		|| aic >= SAFE_QUARTIC_LIMIT || aid >= SAFE_QUARTIC_LIMIT
		|| aie >= SAFE_QUARTIC_LIMIT;

	    if (aia < Long.MAX_VALUE/6 && aic < Long.MAX_VALUE/6
		&& aid < Long.MAX_VALUE/6 && aie < Long.MAX_VALUE/6) {
		safeInts = true;
	    }
	    long ia2 = ia*ia;
	    // long ib2 = ib*ib;
	    long ic2 = ic*ic;
	    long id2 = id*id;
	    long ie2 = ie*ie;
	    iDelta = 256*ia2*ia*ie2*ie  - 128*ia2*ic2*ie2
		+ 144*ia2*ic*id2*ie - 27*ia2*id2*id2
		+ 16*ia*ic2*ic2*ie - 4*ia*ic2*ic*id2;

	    int iaLog2;
	    int icLog2;
	    int idLog2;
	    int ieLog2;

	    if (notSafe) {
		iaLog2 = (64 - Long.numberOfLeadingZeros(aia-1));
		icLog2 = (64 - Long.numberOfLeadingZeros(aic-1));
		idLog2 = (64 - Long.numberOfLeadingZeros(aid-1));
		ieLog2 = (64 - Long.numberOfLeadingZeros(aie-1));
	    } else {
		iaLog2 = 0;
		icLog2 = 0;
		idLog2 = 0;
		ieLog2 = 0;
	    }

	    // Use 60 because we have 6 terms. If we limit the terms
	    // to 1/8 of the maximum value (2^63 - 1), then the iDelta
	    // will not overflow.
	    if (notSafe && ie != 0) {
		if (8 + 3*iaLog2 + 3*ieLog2 >= 60) noOverflow = false;
		if (ic != 0) {
		    if (7 + 2*iaLog2 +2*icLog2 + 2*ieLog2 >= 60) {
			noOverflow = false;
		    }
		    if (id != 0) {
			if  (8 + 2*iaLog2 + icLog2 + 2*idLog2 + ieLog2 >= 60) {
			    noOverflow = false;
			}
		    }
		    if (4 + iaLog2 + 4*icLog2 + ieLog2 >= 60) {
			noOverflow = false;
		    }
		}
	    }
	    if (notSafe && id != 0) {
		if (5 + 2*iaLog2 + 2*idLog2 >= 60) noOverflow = false;
		if (ic != 0) {
		    if (2 + iaLog2 + 3*icLog2 + 2 *idLog2 >= 60) {
			noOverflow = false;
		    }
		}
	    }

	    iP = 8*ia*ic;
	    iR = 8*id*ia2;
	    iDelta0 = ic2 + 12*ia*ie;

	    iD = 64*ia2*ia*ie - 16*ia2*ic2;
	}

	double a2 = a*a;
	double c2 = c*c;
	double d2 = d*d;
	double e2 = e*e;
	double a3 = a2*a;
	double c3 = c2*c;
	double d3 = d2*d;
	double e3 = e2*e;


	// <https://en.wikipedia.org/wiki/Quartic_function#
	// Solving_a_quartic_equation>

	double Delta, errDelta;
	double P, errP;
	double R, errR;
	double Delta0, errDelta0;
	double D, errD;

	if (hasIntCoefficients && noOverflow) {
	    Delta = iDelta;
	    P = iP;
	    R = iR;
	    Delta0 = iDelta0;
	    D = iD;
	} else {
	    // We use Kahan's addition algorithm to get the quantities
	    // Delta, P, Q, R, Delta0, and D as accurately as we can
	    // manage.

	    Delta = 256*a3*e3;
	    errDelta = Math.ulp(Delta);
	    double term = -128*a2*c2*e2;
	    errDelta += Math.ulp(term);
	    double y = term;
	    double tt = Delta + y;
	    double cc = (tt - Delta) - y;
	    Delta = tt;
	    term = 144*a2*c*d2*e;
	    errDelta += Math.ulp(term);
	    y = term - cc;
	    tt = Delta + y;
	    cc = (tt - Delta) - y;
	    Delta = tt;
	    term = -27*a2*d3*d;
	    errDelta += Math.ulp(term);
	    y = term - cc;
	    tt = Delta + y;
	    cc = (tt - Delta) - y;
	    Delta = tt;
	    term = 16*a*c3*c*e;
	    errDelta += Math.ulp(term);
	    y = term - cc;
	    tt = Delta + y;
	    cc = (tt - Delta) - y;
	    Delta = tt;
	    term = -4*a*c3*d2;
	    errDelta += Math.ulp(term);
	    y = term - cc;
	    tt = Delta + y;
	    cc = (tt - Delta) - y;
	    Delta = tt;
	    if (Math.abs(Delta) < errDelta) {
		Delta = 0;
	    }

	    double Pterm1 = 8*a*c;
	    P = Pterm1;
	    errP = Math.ulp(Pterm1);
	    if (hasIntCoefficients) {
		if (iP == 0 && Math.abs(P) < Math.scalb(errP,10)) P = 0;
	    } else if (Math.abs(P) < errP) {
		P = 0;
	    }

	/*
	System.out.format("a = %g, b = %g, c = %g, d = %g, e = %g\n",
			  a, b, c, d, e);
	System.out.format("a2 = %g, b2 = %g, c2 = %g, d2 = %g, e2 = %g\n",
			  a2, b2, c2, d2, e2);
	System.out.format("a3 = %g, b3 = %g, c3 = %g, d3 = %g, e3 = %g\n",
			  a3, b3, c3, d3, e3);
	*/
	    R = 8*d*a2;
	    errR = Math.ulp(R);
	    Delta0 = c2;
	    errDelta0 = Math.ulp(c2);
	    term = 12*a*e;
	    errDelta0 += Math.ulp(term);
	    Delta0 += term;
	    if (Math.abs(Delta0) < Math.scalb(errDelta0,/*10*/5)) {
		Delta0 = 0.0;
	    }

	    D = 64*a3*e;
	    errD = Math.ulp(D);
	    y = -16*a2*c2;
	    D += y;
	    errD += Math.ulp(y);
	    if (Math.abs(D) < errD) {
		D = 0;
	    }
	}
	/*
	System.out.println("Delta = " + Delta);
	System.out.println("P = " + P);
	System.out.println("R = " + R);
	System.out.println("Delta0 = " + Delta0);
	System.out.println("D = " + D);
	*/
	final double afin = a;
	final double cfin = c;
	final double dfin = d;
	final double efin = e;

	DoubleUnaryOperator f = (t) -> {
	    double prod = t;
	    double sum = efin;
	    double yy = dfin*prod;
	    double ttt = sum + yy;
	    double ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t;
	    yy = (cfin*prod) - ccc;
	    ttt = sum + yy;
	    ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t*t;
	    yy = (afin*prod) - ccc;
	    ttt = sum + yy;
	    return ttt;
	};

	DoubleUnaryOperator ferr = (t) -> {
	    double prod = t;
	    double sum = Math.ulp(efin);
	    sum += Math.ulp(dfin*prod);
	    prod *= t;
	    sum += Math.ulp(cfin*prod);
	    prod *= t*t;
	    sum += Math.ulp(afin*prod);
	    return sum;
	};

	DoubleUnaryOperator df = (t) -> {
	    double sum = dfin;
	    double prod = t;
	    double yy = 2*cfin*prod;
	    double ttt = sum + yy;
	    double ccc = (ttt - sum) - yy;
	    sum = ttt;
	    prod *= t*t;
	    yy = (4*afin*prod) - ccc;
	    ttt = sum + yy;
	    return ttt;
	};


	int nr = -1; // if not changed, case analysis failed.
	int resind = 0;
	int multiple = 0;
	double mroot = 0.0;
	if (Delta < 0) {
	    // two distinct real roots & two complex conjugate non-real roots
	    // System.out.println("case 1: nr = 2");
	    nr = 2;
	} else if (Delta > 0) {
	    if (P < 0 && D < 0) {
		// four distinct real roots
		// System.out.println("case 2: nr = 2");
		nr = 4;
	    } else if (P > 0 || D > 0) {
		// two paris of non-real complex conjugate roots
		// System.out.println("case 3: nr = 0");
		return 0;
	    }
	} else if (Delta == 0) {
	    if (P < 0 && D < 0 && Delta0 != 0) {
		if (res.length <= 3) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		// double real root & two real simple roots
		// System.out.println("case 4: nr = 3");
		multiple = 1;
		nr = 3;
		res[0] = d;
		res[1] = 2*c;
		res[2] = 0.0;
		res[3] = 4*a;
		int nn = solveCubic(res);
		double rmin = Double.POSITIVE_INFINITY;
		for (int i = 0; i < nn; i++) {
		    double aval = Math.abs(f.applyAsDouble(res[i]));
		    if (aval < rmin) {
			rmin = aval;
			mroot = res[i];
		    }
		}
		// System.out.println("mroot = " + mroot);
	    } else if ((D > 0) || ((P > 0) && (D != 0 || R != 0))) {
		// double real root and two complex conjugate roots
		if (res.length <= 3) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		multiple = 1;
		// System.out.println("case 5: nr = 1");
		if (hasIntCoefficients && safeInts) {
		    res[3] = 4*ia;
		    res[2] = 0.0;
		    res[1] = 2*ic;
		    res[0] = id;
		} else {
		    res[3] = 4*a;
		    res[2] = 0.0;
		    res[1] = 2*c;
		    res[0] = d;
		}
		int n = solveCubic(res);
		int ind = -1;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; i++) {
		    double aval = Math.abs(f.applyAsDouble(res[i]));
		    if (aval < min) {
			min = aval;
			ind = i;
		    }
		}
		if (ind != 0) {
		    res[0] = res[ind];
		}
		return 1;
	    } else if (Delta0 == 0 && D != 0) {
		// triple real root and a real simple root
		if (res.length <= 2) {
		    String msg = errorMsg("argArray2TooShort");
		    throw new IllegalArgumentException(msg);
		}
		multiple = 1;
		// System.out.println("case 6: nr = 2");
		if (hasIntCoefficients && safeInts) {
		    res[2] = 6*ia;
		    res[1] = 0.0;
		    res[0] = c;
		} else {
		    res[2] = 6*a;
		    res[1] = 0.0;
		    res[0] = c;
		}
		double rtsq = (c/a)/6;
		int n;
		if (rtsq == 0.0) {
		    res[0] = 0.0;
		    return 1;
		} else if (rtsq > 0) {
		    double rt = Math.sqrt(rtsq);
		    res[0] = -rt;
		    res[1] = rt;
		    n = 2;
		} else {
		    n = 0;
		}
		/*
		for (int i = 0; i < n; i++) {
		    System.out.println("... res[" + i + "] = "
				       + res[i] + ", f(x) = "
				       + f.applyAsDouble(res[i]));
		}
		*/
		if (n == 0) {
		    // case analysis failed due to floating-point
		    // errors, Fall back onto a numerical solution.
		    if (eqn == res) {
			eqn[4] = a;
			eqn[3] = 0.0;
			eqn[2] = c;
			eqn[1] = d;
			eqn[0] = e;
		    }
		    return solvePolynomial(eqn, 4, res, null, true);
		}
		if (n == 2) {
		    if (Math.abs(f.applyAsDouble(res[0])) >
			Math.abs(f.applyAsDouble(res[1]))) {
			res[0] = res[1];
		    }
		    if (res[0] == 0.0) {
			return 1;
		    }
		    res[1] = refineSolution(f, null, 0.0, -3*res[0]);
		    if (res[0] > res[1]) {
			double tmp = res[0];
			res[0] = res[1];
			res[1] = tmp;
		    }
		}
		return 2;
	    } else if (D == 0) {
		if (P < 0) {
		    if (res.length <= 3) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    // System.out.println("case 7: nr = 2");
		    // two real double roots
		    multiple = 2;
		    if (hasIntCoefficients && safeInts) {
			res[0] = id;
			res[1] = 2*ic;
			res[2] = 0.0;
			res[3] = 4*ia;
		    } else {
			res[0] = d;
			res[1] = 2*c;
			res[2] = 0.0;
			res[3] = 4*a;
		    }
		    int n = solveCubic(res);
		    if (n < 3) {
			// close to a case transition.
			if (eqn == res) {
			    eqn[4] = a;
			    eqn[3] = 0.0;
			    eqn[2] = c;
			    eqn[1] = d;
			    eqn[0] = e;
			}
			return solvePolynomial(eqn, 4, res, null, true);
		    }
		    int ind = -1;
		    double max = -1.0;
		    for (int i = 0; i < n; i++) {
			double aval = Math.abs(f.applyAsDouble(res[i]));
			if (aval > max) {
			    max = aval;
			    ind = i;
			}
		    }
		    switch (ind) {
		    case 0:
			res[0] = res[1];
			// fall through
		    case 1:
			res[1]  = res[2];
			// fall through
		    case 2:
			break;
		    }
		    return 2;
		} else if (P > 0) {
		    // System.out.println("case 8: nr = 0");
		    // two complex conjugate double roots
		    return 0;
		} else if (Delta0 == 0) {
		    if (res.length < 1) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    multiple = 1;
		    // System.out.println("case 9: nr = 1");
		    // one root equal to -b/(4a).
		    res[0] = /*-b/(4*a)*/ 0.0;
		    return 1;
		}
	    }
	}


	if (nr == -1) {
	    // None of the cases fit, which indicates that
	    // we can't determine the number of roots due to
	    // floating-point errors.  Use a numerical analysis
	    // instead.
	    return solvePolynomial(eqn, 4, res, null, true);
	}

	/*
	// for reduced quartic
	double p = (8*c*a -3*b2)/(8*a2);
	// double q = (b3 - 4*c*b*a + 8*d*a2)/(8*a3);
	double q = b3;
	y = -4*a*b*c;
	tt = q + y;
	cc = (tt - q) - y;
	q = tt;
	y = (8*d*a2) - cc;
	q += y;
	q /= 8*a3;
	// double r = (-3*b3*b + 256*e*a3 - 64*d*b*a2 + 16*c*b2*a)/(256*a3*a);
	double r = -3*b3*b;
	y = 256*e*a3;
	tt = r  + y;
	cc = (tt - r) - y;
	r = tt;
	y = (-64*d*b*a2) - cc;
	tt = r + y;
	cc = (tt - r) - y;
	r = tt;
	y = (16*c*b2*a) - cc;
	r += y;
	r /= (256*a3*a);
	*/

	double p = c/a;
	double q = d/a;
	double r = e/a;

	/*
	System.out.println("Depressed Quartic: y^4 + py^2 + qy + r = 0");
	System.out.println("    p = " + p);
	System.out.println("    q = " + q);
	System.out.println("    r = " + r);
	*/

	double tmp[] = {-q*q, 2*p*p - 8*r, 8*p, 8.0};
	/*
	System.out.println("Resolvent Cubic: "
			   + "8m^3 + 8pm^2 + (2p^2 - 8)m - q^2)");
	System.out.println("            8 = " + tmp[3]);
	System.out.println("           8p = " + tmp[2]);
	System.out.println("    (2p^2-8r) = " + tmp[1]);
	System.out.println("         -q^2 = " + tmp[0]);
	*/
	/*
	if (mroot != 0) {
	    for (int i = 0; i < 4; i++) {
		System.out.println("tmp[" + i + "] = " + tmp[i]);
	    }
	}
	*/
	// System.out.println("SOLVING CUBIC");
	int n = solveCubic(tmp);
	// System.out.println("SOLVED CUBIC");
	double m = Double.NEGATIVE_INFINITY;

	// System.out.println("Roots of Resolvent Cubic:");
	for (int i = 0; i < n; i++) {
	    /*
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + (-q*q +(2*p*p - 8*r)*tmp[i]
				  + 8*p*tmp[i]*tmp[i]
				  + 8*tmp[i]*tmp[i]*tmp[i]));
	    */
	    if (tmp[i] > m) {
		m = tmp[i];
	    }
	}
	// System.out.println("m = " + m);
	if (m == 0) {
	    // System.out.println("q = " + q);
	    if (Math.abs(q) > Math.scalb(Math.ulp(q), 5)) {
		String msg = errorMsg("argArray2TooShort");
		throw new RuntimeException(msg);
	    }
	    tmp[0] = r;
	    tmp[1] = p;
	    tmp[2] = 1.0;
	    n = solveQuadratic(tmp);
	    // System.out.println("solveQuadratic(tmp) returned " + n);
	    for (int i = 0; i < n; i++) {
		// System.out.println("tmp[" + i +"] = " + tmp[i]);
		double rr = tmp[i];
		if (rr == 0.0) {
		    if (res.length <= resind) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[resind++] = 0.0;
		} else if (rr > 0) {
		    if (res.length <= resind+1) {
			String msg = errorMsg("argArray2TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    rr = Math.sqrt(rr);
		    res[resind++] = -rr;
		    res[resind++] = rr;
		}
	    }
	    Arrays.sort(res, 0, resind);
	    return resind;
	} else if (m < 0) {
	    return 0;
	}
	double r2m = Math.sqrt(2*m);
	double pterm = p/2.0 + m;
	double qterm = q/(2*r2m);
	tmp[0] = pterm - qterm;
	tmp[1] = r2m;
	tmp[2]  = 1.0;
	// System.out.println("Solve y^2 + p/2 + m + sqrt(2m)y - q/(2sqrt(2m):");
	n = solveQuadratic(tmp);
	/*
	for (int i = 0; i < n; i++) {
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + ((pterm-qterm) + r2m*tmp[i] + tmp[i]*tmp[i]));
	}
	*/
	// System.out.println("... found " + n + " solutions");
	for (int i = 0; i < n; i++) {
	    if (res.length <=resind) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[resind++] = refineSolution(f, df, 0.0, tmp[i]);
	    /*
	    System.out.println("    " + res[resind-1]
			       + ", was " + (tmp[i]));
	    */
	}
	tmp[0] = pterm + qterm;
	tmp[1] = -r2m;
	tmp[2] = 1.0;
	// System.out.println("Solve y^2 + p/2 + m - sqrt(2m)y +1/(2sqrt(2m):)");
	n = solveQuadratic(tmp);
	/*
	for (int i = 0; i < n; i++) {
	    System.out.println("    " + tmp[i] + ", value of polynomial = "
			       + ((pterm+qterm) - r2m*tmp[i] + tmp[i]*tmp[i]));
	}
	*/
	// System.out.println("... found " + n + " solutions");
	for (int i = 0; i < n; i++) {
	    if (res.length <=resind) {
		String msg = errorMsg("argArray2TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[resind++] = refineSolution(f, df, 0.0, tmp[i]);
	    /*
	    System.out.println("    " + res[resind-1]
			       + ", was " + (tmp[i]));
	    */
	}
	// check for spurious solutions and eliminate them.
	for (int i = 0; i < resind; i++) {
	    double rt = res[i];
	    double err = Math.scalb(ferr.applyAsDouble(rt), 5);
	    if (Math.abs(f.applyAsDouble(rt)) > err) {
		/*
		System.out.println("eliminating " + rt + " because "
				   + Math.abs(f.applyAsDouble(rt))
				   + " > " + err);
		*/
		res[i--] = res[--resind];
	    }
	}
	Arrays.sort(res, 0, resind);
	if (resind > nr) {
	    for (int i = 1; i < resind; i++) {
		if (res[i-1] == res[i]) {
		    for (int j = i; j < resind; j++) {
			res[j-1] = res[j];
		    }
		    i--;
		    resind--;
		}
	    }
	}
	if (nr == 3 && resind > nr) {
	    if (mroot < res[0]) {
		res[0] = mroot;
		res[1] = res[2];
		res[2] = res[3];
		resind = nr;
	    } else if (mroot > res[nr]) {
		res[2] = mroot;
		resind = nr;
	    } else {
		int j = -1;
		for (int i = 0; i < resind; i++) {
		    if (res[i] == mroot) {
			j  = i;
			break;
		    }
		}
		if (j != -1) {
		    if (j == 0) {
			res[1] = res[2];
			res[2] = res[3];
		    } else if (j == nr) {
			res[nr-1] = mroot;
		    } else {
			double dist1 = Math.abs(res[j-1] - mroot);
			double dist2 = Math.abs(res[j+1] = mroot);
			if (dist1 < dist2) {
			    res[j-1] = mroot;
			    for (int i = j; i < nr; i++) {
				res[i] = res[i+1];
			    }
			} else {
			    for (int i = j+1; i < nr; i++) {
				res[i] = res[i+1];
			    }
			}
		    }
		    resind = nr;
		} else {
		    j = -1;
		    for (int i = 0; i < nr; i++) {
			if (res[i] < mroot && res[i+1] > mroot) {
			    j = i;
			    break;
			}
		    }
		    res[j] = mroot;
		    for (int i = j+1; i < nr; i++) {
			res[i] = res[i+1];
		    }
		    resind = nr;
		}
	    }
	}
	return resind;
    }

    static final double BEZIER_X_LIMIT = Math.scalb(Math.ulp(1.0), 5);
    static final int BEZIER_FERR_SCALB = 8;

    /**
     * Find the zeros of a {@link BezierPolynomial}.
     * @param p the polynomial
     * @param res an array to hold the results
     * @return the number of roots; -1 if the degree of the
     *         polynomial is zero.
     * @exception IllegalArgumentException if the array
     *            argument is too short
     */
    public static int solvePolynomial(BezierPolynomial p, double[] res) {
	return solveBezier(p.getCoefficientsArray(), 0, p.getDegree(), res);
    }


    /**
     * Find the zeros of a sum of Bernstein polynomial of degree n.
     * The name of the method, solveBezier, reflects the
     * use of Bernstein polynomials for B&eacute;zier curves.
     * The roots returned are in the range [0.0, 1.0], the range
     * appropriate for B&eacute;zier curves.
     * @param eqn the coefficients of the polynomial for input, where
     *        eqn[i] is the coefficient for the Bernstein polynomial
     *         B<sub>i,n</sub>(t)
     * @param offset the offset into the array eqn at which the
     *        coefficients start
     * @param n the degree of the polynomial
     * @param res the roots that were found
     * @return the number of roots; -1 if the degree of the
     *         polynomial is zero.
     * @exception IllegalArgumentException n is negative or the array
     *            arguments are too short
     */
    public static int solveBezier(double[] eqn, int offset, int n, double[] res)
	throws IllegalArgumentException
    {
	return solveBezier(eqn, offset, n, res, null);
    }

    /**
     * Find the roots  and critical points of a polynomial of degree n.
     */
    private static int solveBezier(double[] eqn, int offset, int n,
				   double[] res, double[] cpts)
	throws IllegalArgumentException
    {

	double[] meqn = Polynomials.fromBezier(eqn, 0, n);
	// If this is actually a lower-order polynomial,
	// convert the Bezier polynomial to a monomial and solve
	// that.
	if (meqn[n] == 0.0) {
	    int rtcnt = solvePolynomial(meqn, n, res, cpts, false);
	    int imin = 0;
	    int imax = rtcnt;
	    for (int i = 0; i < rtcnt; i++) {
		if (res[i] < 0.0) imin++;
		else break;
	    }
	    for (int i = rtcnt-1; i >= 0; i--) {
		if (res[i] > 1.0) imax--;
		else break;
	    }
	    int retcount = imax - imin;
	    System.arraycopy(meqn, imin, meqn, 0, retcount);
	    if (cpts != null) {
		rtcnt = (int) Math.round(res[rtcnt]);
		imin = 0;
		imax = rtcnt;
		for (int i = 0; i < rtcnt; i++) {
		    if (cpts[i] < 0.0) imin++;
		    else break;
		}
		for (int i = rtcnt-1; i >= 0; i--) {
		    if (cpts[i] > 1.0) imax--;
		    else break;
		}
		res[retcount] = (double)(imax - imin);
	    }
	    /*
	    System.out.println("retcount = " + retcount);
	    for (int i = 0; i < retcount; i++) {
		System.out.format("res[%d] = %g\n", i, res[i]);
	    }
	    */
	    if (cpts != null) {
		/*
		System.out.format("(cnt) res[%d] = %g\n",
				  retcount, res[retcount]);
		for (int i = 0; i < imax-imin; i++) {
		    System.out.format("cpts[%d] = %g\n", i, cpts[i]);
		}
		*/
	    }
	    return retcount;
	}

	// We treat x = 0.0 and x = 1.0 as critical points
	// and add those to the res array when cpts is not null.

	/*
	System.out.println("calling solveBeziers "
			   + "(int n = " + n + ")");
	for (int i = 0; i <= n; i++) {
	    System.out.println(" ... eqn[" + (offset+i) + "] = "
			       + eqn[offset+i]);
	}
	*/
	if (n < 0) {
	    String msg = errorMsg("thirdArgNegI", n);
	    throw new IllegalArgumentException(msg);
	}
	if (eqn == null || eqn.length <= offset+n) {
	    String msg = errorMsg("argArray1TooShort");
	    throw new IllegalArgumentException(msg);
	}

	// N >= 3 because we already took care of the lower-order polynomials
	final int N = n;
	final double[] EQN = (res == eqn)? new double[n+1]: eqn;
	if (EQN != eqn) {
	    System.arraycopy(eqn, offset, EQN, 0, n+1);
	    offset = 0;
	}
	final int OFFSET = offset;

	DoubleUnaryOperator f = (t) -> {
	    return Functions.Bernstein.sumB(EQN, OFFSET, N, t);
	};

	DoubleUnaryOperator df = (t) -> {
	    return Functions.Bernstein.dsumBdx(EQN, OFFSET, N, t);
	};

	DoubleUnaryOperator d2f = (t) -> {
	    return Functions.Bernstein.d2sumBdx2(EQN, OFFSET, N, t);
	};

	DoubleUnaryOperator ferr = (t) -> {
	    double err = Functions.Bernstein.sumBerr(EQN, OFFSET, N, t);
	    return Math.scalb(err, BEZIER_FERR_SCALB);
	};


	switch (n) {
	case 0:
	    return -1;
	case 1:
	    if (res == null || res.length == 0) {
		String msg = errorMsg("argArray4TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    double error1 = Math.ulp(eqn[offset]);
	    double error2 = error1 + Math.ulp(eqn[offset+1]);
	    double denom = eqn[offset+1] - eqn[offset];
	    if (Math.abs(denom) < error2) return -1;
	    double value1 = -eqn[offset] / denom;
	    double error = error1/Math.abs(denom) + Math.abs(value1)*error2;
	    if (Math.abs(value1) < error) value1 = 0.0;
	    if (Math.abs(1-value1) < error) value1 = 1.0;
	    if (value1 < 0.0 || value1 > 1.0) {
		if (cpts != null) {
		    return 2;
		}
		return 0;
	    }
	    res[0] = value1;
	    if (cpts != null) {
		res[1] = 0.0;
	    }
	    return 1;
	case 2:
	    double eqn0 = eqn[offset];
	    double eqn1 = eqn[offset+1];
	    double eqn2 = eqn[offset+2];
	    double tmp2[] = {eqn0, 2*(eqn1 - eqn0), eqn0 + eqn2 - 2*eqn1};
	    double err0 = Math.ulp(eqn0);
	    double err1 = 2*(Math.ulp(eqn1) + Math.ulp(eqn0));
	    double err2 = Math.ulp(eqn0) + Math.ulp(eqn2) + 2*Math.ulp(eqn1);
	    if (Math.abs(tmp2[2]) < err2) {
		tmp2[2]  = 0.0;
	    }
	    int nr = solveQuadratic(tmp2, res);
	    switch(nr) {
	    case -1:
		/*
		System.out.println("solveQuadratic failed. Coefficients:");
		System.out.println("    " + tmp2[0] + ", " + tmp2[1]
				   + ", " + tmp2[2]);
		System.out.println("from Bezier coefficients ");
		System.out.println("    " + eqn0 + ", " + eqn1
				   + ", " + eqn2);
		*/
		return -1;
	    case 0:
		if (cpts != null) {
		    double extr = (tmp2[2] == 0.0)? -1.0:
			-tmp2[1]/(2*tmp2[2]);
		    cpts[0] = extr;
		    boolean extrtest = (extr >= 0.0 && extr <= 1.0);
		    res[0] = extrtest? 1: 0;
		}
		return 0;
	    case 1:
		if (f.applyAsDouble(0.0) < ferr.applyAsDouble(0.0)) {
		    res[0] = 0.0;
		} else if (f.applyAsDouble(1.0) < ferr.applyAsDouble(1.0)) {
		    res[0] = 1.0;
		} else if (res[0] < 0.0 || res[0] > 1.0) {
		    nr = 0;
		}
		if (cpts != null) {
		    double extr = (tmp2[2] == 0.0)? -1.0:
			-tmp2[1]/(2*tmp2[2]);
		    cpts[0] = extr;
		    boolean extrtest = (extr >= 0.0 && extr <= 1.0);
		    res[nr] = extrtest? 1: 0;
		}
		return nr;
	    case 2:
		boolean test1 =
		    Math.abs(f.applyAsDouble(0.0)) < ferr.applyAsDouble(0.0);
		boolean test2 =
		    Math.abs(f.applyAsDouble(1.0)) < ferr.applyAsDouble(1.0);
		if (test1 && test2) {
		    res[0] = 0.0;
		    res[1] = 1.0;
		} else if (test1) {
		    if (Math.signum(df.applyAsDouble(0.0)) !=
			Math.signum(d2f.applyAsDouble(0.0))) {
			// The first root is at 0.0 and the second
			// is above 0.0
			res[0] = 0.0;
			if (res[1] > 1.0) {
			    nr= 1;
			}
		    } else {
			res[0] = 0.0;
			nr = 1;
		    }
		} else if (test2) {
		    if (Math.signum(df.applyAsDouble(1.0)) ==
			Math.signum(d2f.applyAsDouble(1.0))) {
			// The first root is below 1.0. If it is below
			// 0.0, we have only one root in [0,1]. The final
			// root, however, must be 1.0
			if (res[0] < 0.0) {
			    res[0] = 1.0;
			    nr = 1;
			} else {
			    res[1] = 1.0;
			}
		    } else {
			// No roots are below 1.0.
			res[0] = 1.0;
			nr = 1;
		    }
		} else {
		    if (res[1] < 0.0 || res[0] > 1.0
			|| (res[0] < 0.0 && res[1] > 1.0)) {
			nr = 0;
		    } else if (res[0] >= 0.0 && res[1] > 1.0) {
			nr = 1;
		    } else if (res[0] < 0.0 && res[1] <= 1.0) {
			res[0] = res[1];
			nr = 1;
		    }
		}
		if (cpts != null) {
		    double extr = (tmp2[2] == 0.0)? -1.0:
			-tmp2[1]/(2*tmp2[2]);
		    cpts[0] = extr;
		    boolean extrtest = (extr >= 0.0 && extr <= 1.0);
		    res[nr] = extrtest? 1: 0;
		}
		/*
		System.out.println("returning nr = " + nr);
		for (int p = 0; p < nr; p++) {
		    System.out.println("   res[" + p + "] = " + res[p]);
		}
		*/
		return nr;
	    }
	default:
	    break;
	}

	// Find the critical points.
	cpts = (cpts == null)? new double[n+1]: cpts;
	// ... and the inflection points
	double[] ipts = new double[n-1];
	for (int i = 0; i < n; i++) {
	    cpts[i] = n*(EQN[i+1] - EQN[i]);
	}
	// System.out.println("(solving for critical points)");
	int ncpts = solveBezier(cpts, 0, n-1, cpts, ipts);
	int nipts =(ncpts == -1)? 0: (int) Math.round(cpts[ncpts]);
	// shouldn't happen
	if (ncpts < 0) ncpts = 0;
	/*
	  System.out.println("ncpts = " + ncpts + ", nipts = " + nipts);
	  System.out.println("critical points (for n = " + n + ")");
	  for (int i = 0; i < ncpts; i++) {
	  System.out.println("    " + cpts[i] + ", f(x) = "
	  + f.applyAsDouble(cpts[i])
	  + ", df/dx = " + df.applyAsDouble(cpts[i])
	  + ", d2f/dx = " +d2f.applyAsDouble(cpts[i]));
	  }
	  System.out.println("inflection points (for n = " + n + ")");
	  for (int i = 0; i < nipts; i++) {
	  System.out.println("    " + ipts[i] + ", f(x) = "
	  + f.applyAsDouble(ipts[i])
	  + ", df/dx = " + df.applyAsDouble(ipts[i])
	  + ", d2f/dx = " +d2f.applyAsDouble(ipts[i]));
	  }
	*/
	if (ncpts == 0) {
	    // no critical points, so see if there is a root between
	    // 0.0 and 1.0.
	    double v0 = f.applyAsDouble(0.0);
	    double v1 = f.applyAsDouble(1.0);
	    if (Math.abs(v0) < ferr.applyAsDouble(0.0)) {
		// 0.0 is the root.
		res[0] = 0.0;
		if (cpts != null) {
		    res[1] = 0.0;
		}
		return 1;
	    } else if (Math.abs(v1) < ferr.applyAsDouble(1.0)) {
		// 1.0 is the root.
		res[0] = 1.0;
		if (cpts != null) {
		    res[1] = 0.0;
		}
		return 1;
	    } else if (Math.signum(v0) != Math.signum(v1))  {
		// root is somewhere between 0.0 and 1.0;
		res[0] = findBezierRoot(0, eqn, n, 0.0, 1.0);
		if (cpts != null) {
		    res[1] = 0.0;
		}
		return 1;
	    } else {
		if (cpts != null) {
		    res[0] = 0.0;
		}
		return 0;
	    }
	}
	double[] pts = (nipts == 0)? cpts: new double[ncpts+nipts];
	// array indicating if a point is an inflection point.
	boolean[] isip = new boolean[ncpts+nipts];
	// System.out.println("ncpts = " + ncpts);
	// System.out.println("nipts = " + nipts);
	// System.out.println("isip.length = " + isip.length);
	int npts;
	if (pts != cpts) {
	    int i = 0;
	    int j = 0;
	    int k = 0;
	    while (i < ncpts || j < nipts) {
		double x1 = (i >= ncpts)? Double.POSITIVE_INFINITY: cpts[i];
		double x2 = (j >= nipts)? Double.POSITIVE_INFINITY: ipts[j];
		/*
		  System.out.println("i = " + i + ", j = " + j + ", k = " + k
		  + ", x1 = " + x1 + ", x2 = " + x2);
		*/
		if (Math.abs(x1 - x2) < BEZIER_X_LIMIT) {
		    // x values range from 0.0 to 1.0 and Berstein polynomials
		    // use powers of x and (1-x), so we can't meaningfully
		    // distinguish values that are close together.
		    // System.out.println("choosing x1/x2");
		    pts[k++] = x1;
		    i++; j++;
		} else if (x1 < x2) {
		    // System.out.println("choosing x1");
		    pts[k++] = x1;
		    i++;
		} else {
		    // System.out.println("choosing x2");
		    isip[k] = true;
		    pts[k++] = x2;
		    j++;
		}
	    }
	    npts = k;
	} else {
	    npts = ncpts;
	}
	/*
	  System.out.println("critical points + inflection points (n = "
			     + n + "):");
	  for (int i = 0; i < npts; i++) {
	  System.out.println("    " + pts[i] + ", isInflected = " + isip[i]);
	  }
	*/
	// Note: two zeros must have at least one critical points and
	// possibly some inflection points between those two zeros.

	int ptind = 0;
	int resind = 0;
	// double x = pts[ptind];
	// boolean isInflection = isip[ptind];

	double x = 0.0;
	boolean isInflection = true; // pretend it is an inflection point.
	double val = f.applyAsDouble(x);
	double err = ferr.applyAsDouble(x);
	if (Math.abs(val) < err) {
	    if (res == null || res.length <= resind) {
		String msg = errorMsg("argArray4TooShort");
		throw new IllegalArgumentException(msg);
	    }
	    res[resind++] = x;
	    /*
	    System.out.println("set res[" + (resind-1) + "] to "
	      + res[resind-1]);
	    */
	    if (ptind == npts) {
		x = 1.0;
		val = f.applyAsDouble(x);
		err = ferr.applyAsDouble(x);
		if (Math.abs(val) < err) {
		    res[resind++] = x;
		}
		if (res.length > resind) {
		    res[resind] = ncpts;
		}
		return resind;
	    } else {
		// Two successive critical/inflection points can't be zeros
		// so we arrange for the current critical point to
		// be one that is not a zero.
		// x = pts[resind];
		x = pts[ptind];
		isInflection = isip[ptind];
		val = f.applyAsDouble(x);
		err = ferr.applyAsDouble(x);
	    }
	}
	// At this point, x is a critical or inflection point and that point
	// is not a zero.
	/*
	System.out.println("... searching more critical points"
	  + ", x = " + x);
	*/
	while (ptind <= npts) {
	    double x2;
	    boolean isInflection2;
	    if (ptind == npts) {
		x2 = 1.0;
		isInflection2 = true;
	    } else {
		x2 = pts[ptind];
		isInflection2 = isip[ptind];
	    }
	    // System.out.println("trying interval [" + x +", " +x2 +"]");
	    double v = f.applyAsDouble(x2);
	    double ev = ferr.applyAsDouble(x2);
	    /*
	    System.out.println("for x = " + x2 + ", v = " + v + ", ev = " + ev);
	    */
	    if (Math.abs(v) < ev) {
		/*
		  System.out.println("critical/inflection point is a root: "
		  + x2
		  + ", ptind = " + ptind
		  + ", v = " + v +", ev = " + ev);
		*/
		if (res == null || res.length <= resind) {
		    String msg = errorMsg("argArray4TooShort");
		    throw new IllegalArgumentException(msg);
		}
		res[resind++] = refineSolution(f, null, 0.0, x2);
		if (resind > 1 && res[resind-1] == res[resind-2]) {
		    resind--;
		}
		if (ptind == npts) break;
		/*
		  System.out.println("set res[" + (resind-1) + "] to "
		  + res[resind-1]);
		*/
		ptind++;
		// System.out.println("incremented ptind to " + ptind);
		if (ptind == npts) {
		    // if the last critical/inflection point is a root,
		    // there are no more roots.
		    if (res.length > resind) {
			res[resind] = ncpts;
		    }
		    return resind;
		}
		// since the previous critical/inflection point was a root,
		// this one cannot be a root.
		x = pts[ptind];
		isInflection = isip[ptind];
		if (isInflection && (ptind + 1 == npts)) {
		    // If incrementing ptind got us from a root to an
		    // inflection point, and this inflection point is
		    // the last point, we have no more roots.
		    break;
		}
		val = f.applyAsDouble(x);
		err = ferr.applyAsDouble(x);
	    } else {
		// System.out.println("checking (" +x + ", " + x2 + ")");
		if (Math.signum(val) != Math.signum(v)) {
		    // System.out.println("bracketing");
		    if (res == null || res.length <= resind) {
			String msg = errorMsg("argArray4TooShort");
			throw new IllegalArgumentException(msg);
		    }
		    res[resind++] = findBezierRoot(0, EQN, N, x, x2);
		    /*
		      System.out.println("set res[" + (resind-1) + "] to "
		      + res[resind-1]);
		    */
		    if (resind > 1 && res[resind-1] == res[resind-2]) {
			resind--;
		    }
		}
		x = x2;
		isInflection = isInflection2;
		val = v;
		err = ev;
	    }
	    ptind++;
	}
	if (res.length > resind) {
	    res[resind] = ncpts;
	}

	return resind;
    }
}

//  LocalWords:  RootFinder findRoot MathException Wikipedia exbundle
//  LocalWords:  rf pre getParameters firstDerivative setParameters
//  LocalWords:  setEpsilon setLimits RootFinder's initialArgs href
//  LocalWords:  ConvergenceException maxLimit argNotPositive derivAt
//  LocalWords:  functionEvalFailedBrent solutionNotBracketed ul li
//  LocalWords:  RealValuedFunction RealValuedFunctOps secondDerivAt
//  LocalWords:  f's newtonNotProg fdFailed newtonNotConv initCause
//  LocalWords:  functionEvalNewton halleyNotProg halleyNotConv msg
//  LocalWords:  functionEvalHalley getMessage Throwable toString jdk
//  LocalWords:  throwables solveQuadratic eqn cx solveCubic abdc lt
//  LocalWords:  Kahan's th scalb priori aterm bterm quartic gcd aia
//  LocalWords:  aib aic aie ia ib ic ie iR nr paris mroot tmp ferror
//  LocalWords:  IllegalArgumentException getEpsilon fb ef withBrent
//  LocalWords:  ulp xl xu ux newrootval xerr xm xmval rootval df tt
//  LocalWords:  oldDiff DoubleUnaryOperator rint dlimit discr arg dx
//  LocalWords:  refineSolution guessRoot isInflected findPolyRoot ev
//  LocalWords:  applyAsDouble solvePolynomials solveQuartic ttCase
//  LocalWords:  solvePolynomial skipQuartic rsqCase ncpts nipts cpts
//  LocalWords:  ipts pts npts isip resind deriv beween ptind boolean
//  LocalWords:  setOptimized idelta NaN sqrt ar idiscr ilimit delt
//  LocalWords:  cpt inflpt inflptval py qy Resolvent pterm qterm
