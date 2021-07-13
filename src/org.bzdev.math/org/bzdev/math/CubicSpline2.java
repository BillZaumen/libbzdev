package org.bzdev.math;
import org.bzdev.lang.CallableArgsReturns;
// import java.awt.geom.QuadCurve2D;
// import java.awt.geom.CubicCurve2D;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Create a cubic spline for an unevenly spaced set of values.
 * The spline approximates a function f such that y = f(x). When
 * the values used to compute the spline are evenly spaced, look-up
 * is faster than otherwise.
 * <P>
 * The spline can be created in any of several modes described by
 * the enumeration {@link org.bzdev.math.CubicSpline.Mode CubicSpline.Mode}.
 * The default mode is "NATURAL", which sets the second derivatives
 * (but not necessarily the third derivative) to zero at the end
 * points. For all other modes, a constructor specifying a mode is
 * required.  Some modes base the spline not only on the values at
 * specific points, but on the derivatives at all or a subset of these
 * points.  While defaults of zero are provided, normally one would
 * use a constructor in these cases that take additional arguments
 * that follow the mode argument. The mode HERMITE uses Hermite Cubic
 * splines and these are specified by providing both the value at a
 * each point and the derivative at each point. Other modes support
 * clamped splines (where derivatives are specified for the end
 * points) and parabolic and cubic run-outs (for a parabolic run-out,
 * the second derivatives at an end point and its adjacent point are
 * equal; for a cubic run-out, the two intervals closest to an end
 * point are described by a single cubic equation).
 * <P>
 * For all but one of the supported modes, the second derivative of the
 * spline (e.g., the function the spline computes) is a continuous function.
 * The mode HERMITE is an exception: Hermite splines have continuous first
 * derivatives, but second derivatives may have discontinuities.
 * <P>
 * Methods are available for determining the spline's mode, the minimum
 * and maximum values it can compute, the value at a specified point,
 * the derivative at a specified point, whether the values that define
 * the spline are strictly monotonic, and (if the spline is strictly
 * monotonic) the value of the spline's inverse at a particular point.
 * <P> 
 * Citations:
 * <ul>
 *   <li> <a href="http://en.wikipedia.org/wiki/Cubic_Hermite_spline">Cubic Hermite spline</a>
 *   <li> <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.40.170">Cubic Spline Interpolation, Sky McKinley and Megan Levine</a>
 *   <li> <a href="http://www.physics.arizona.edu/~restrepo/475A/Notes/sourcea-/node35.html">Cubic Splines, Juan M. Restrepo </a>
 * </ul>
 * <P>
 * Note: unfortunately, the notations used in the last two references
 * differ from each other.
 *
 * @see org.bzdev.math.CubicSpline.Mode
 */
public class CubicSpline2 extends CubicSpline {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    double[] x = null;
    int n;

    boolean strictlyIncreasing = false;
    boolean strictlyDecreasing = false;

    /*
     * Get the minimum value in the domain of the function that the
     * spline approximates.
     * @return the minimum value
     */
    public double getDomainMin() {
	if (x[n-1]-x[0] > 0) return x[0];
	else return x[n-1];
    }

    /*
     * Get the maximum value in the domain of the function that the
     * spline approximates.
     * @return the maximum value
     */
    public double getDomainMax() {
	if ((x[n-1]-x[0])> 0) return x[n-1];
	else return x[0];
    }

    CubicSpline.Mode mode = CubicSpline.Mode.NATURAL;

    /**
     * Get the spline's mode.
     * @return the mode for the spline.
     * @see org.bzdev.math.CubicSpline.Mode
     */
    public CubicSpline.Mode getMode() {return mode;}


    boolean quadFit = false;
    boolean parStart = false;
    boolean parEnd = false;
    boolean cubicStart = false;
    boolean cubicEnd = false;
    boolean clampedStart = false;
    boolean clampedEnd = false;
    boolean hermite = false;

    double yP0 = 0.0; // y' at the start of the spline
    double yPn = 0.0; // y' at the end of the spline.
    double yP[] = null;		// array of first derivatives
    double yPU[] = null;	// array of first derivatives

    private double[] createA(int n) {
	int nm1 = n-1;
	int nm2 = n-2;
	int nm3 = n-3;
	double[] result = new double[n];
	result[0] = 0.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] = (x[i]-x[i-1]);
	}
	result[nm1] = 0.0;
	if (parEnd) result[nm1] = -1;
	if (clampedEnd) result[nm1] = (x[nm1] - x[nm2]);
	// if (cubicEnd) result[nm1] = -6.0;
	if (cubicEnd) result[nm1] = -4.0 - 2.0*(x[nm2]-x[nm3])/(x[nm1]-x[nm2]);
	return result;
    }

    private double[] createB(int n) {
	int nm1 = n-1;
	int nm2 = n-2;
	int nm3 = n-3;
	double[] result = new double[n];
	result[0] = 1.0;
	result[nm1] = 1.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] = 2.0 * (x[i+1] - x[i-1]);
	}
	if (clampedStart) result[0] = 2.0 * (x[1] - x[0]);
	if (clampedEnd) result[nm1] = 2.0 * (x[nm1] - x[nm2]);
	// if (cubicStart) result[0] = 0.0;
	if (cubicStart) result[0] = 1.0 - (x[1] - x[0])/(x[2] - x[1]);
	// if (cubicEnd) result[nm1] = 0.0;
	if (cubicEnd) result[nm1] = 1.0 - (x[nm1] - x[nm2])/(x[nm2] - x[nm3]);
	return result;
    }

    private double[] createC(int n) {
	int nm1 = n - 1;
	int nm2 = n - 2;
	int nm3 = n - 3;
	double[] result = new double[n];
	result[0] = 0.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] =  (x[i+1]-x[i]);
	}
	result[nm1] = 0.0;
	if (clampedStart) result[0] = (x[1] - x[0]);
	if (parStart) result[0] = -1.0;
	// if (cubicStart) result[0] = -6.0;
	if (cubicStart) result[0] = -4.0 - 2.0*(x[1] - x[0])/(x[2] - x[0]);
	return result;
    }

    private double[] getw(double[] y, int n) {
	int nm1 = n - 1;
	int nm2 = n - 2;
	double[] result = new double[n];
	
	double factor = 3.0;

	// int limit = nm1 < m ? nm1: m;

	if (clampedStart) {
	    result[0] = 3.0 *(y[1] - y[0])/(x[1] - x[0]) - 3.0 * yP0;
	} else {
	    result[0] = 0.0;
	}
	if (clampedEnd) {
	    result[nm1] = 3.0 * yPn - 3.0 *(y[nm1]-y[nm2]) / (x[nm1] - x[nm2]);
	} else {
	    result[nm1] = 0.0;
	}

	for (int i = 1; i < nm1; i++) {
	    result[i] = 3.0 * ((y[i+1]-y[i])/(x[i+1] - x[i])
			       - (y[i] - y[i-1])/(x[i] - x[i-1]));
	    // result[i] = 3.0 * (y[i-1] - 2.0 * y[i] + y[i+1]);
	}
	if (cubicStart) result[0] = - result[1];
	if (cubicEnd) result[nm1] = - result[nm2];
	return result;
    }

    /**
     * Constructor given a function f(x).
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2( double[] x, CallableArgsReturns<Double,Double>f)
    {
	this(x, f, x.length);
    }

    /**
     * Constructor given a function f(x) and the number of points n.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline 
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f, int n)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    y[i] = f.call(x[i]);
	}
	cubicSpline(x, y, n, CubicSpline.Mode.NATURAL);
    }


    /**
     * Constructor given a function f(x) and a mode.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			CubicSpline.Mode mode)
    {
	this(x, f, x.length, mode);
    }

    /**
     * Constructor given a function f(x), the number of points, and a mode.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			int n, CubicSpline.Mode mode)
    {
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double xx = x[i];
	    y[i] = f.call(xx);
	}
	cubicSpline(x, y, n, mode);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			CubicSpline.Mode mode, double derivative)
    {
	this(x, f, x.length, mode, derivative);
    }
    /**
     * Constructor given a function f(x), the number of points,
     *  a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			int n, CubicSpline.Mode mode, double derivative)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double xx = x[i];
	    y[i] = f.call(xx);
	}
	cubicSpline(x, y, n, mode, derivative);
    }

    /**
     * Constructor given a function f(x), a mode, and two derivatives.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	this(x, f, x.length, mode, derivative1, derivative2);
    }

    /**
     * Constructor given a function f(x), the number of points, a mode,
     * and two derivatives.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, CallableArgsReturns<Double,Double>f,
			int n, CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double xx = x[i];
	    y[i] = f.call(xx);
	}
	cubicSpline(x, y, n, mode, derivative1, derivative2);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function and its derivative are represented by instances of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param fprime a CallableArgsReturns representing the derivative
     *        of the function for which a spline should be computed
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double x[], CallableArgsReturns<Double,Double>f,
			CubicSpline.Mode mode,
			CallableArgsReturns<Double,Double>fprime)
    {
	this(x, f, x.length, mode, fprime);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function and its derivative are represented by instances of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param fprime a CallableArgsReturns representing the derivative
     *        of the function for which a spline should be computed
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double x[], CallableArgsReturns<Double,Double>f,
			int n, CubicSpline.Mode mode,
			CallableArgsReturns<Double,Double>fprime)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	double[] y = new double[n];
	int nm1 = n - 1;
	double[] yp;
	switch (mode) {
	case HERMITE:
	    yp = new double[n];
	    break;
	case CLAMPED:
	    yp = new double[2];
	    yp[0] = fprime.call(x[0]);
	    yp[1] = fprime.call(x[nm1]);
	    break;
	case CLAMPED_START:
	    yp = new double[1];
	    yp[0] = fprime.call(x[0]);
	    break;
	case CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x[nm1]);
	    break;
	case PARABOLIC_START_CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x[nm1]);
	    break;
	case CUBIC_START_CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x[nm1]);
	    break;
	case CLAMPED_START_PARABOLIC_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x[0]);
	    break;
	case CLAMPED_START_CUBIC_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x[0]);
	    break;
	default:
	    yp = new double[0];
	}
	if (mode == CubicSpline.Mode.HERMITE) {
	    for (int i = 0; i < n; i++) {
		double xx = x[i];
		y[i] = f.call(xx);
		yp[i] = fprime.call(xx);
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		double xx = x[i];
		y[i] = f.call(xx);
	    }
	}
	cubicSpline(x, y, n, mode, yp);
    }

    /**
     * Constructor given a real-valued function.
     * @param x the values of x at which f should be evaluated
     * @param f the function for which a spline should be computed
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2( double[] x, RealValuedFunctOps f)
    {
	this(x, f, x.length);
    }

    /**
     * Constructor given a real-valued function and the number of points
     * at which it should be evaluated.
     * The array x must contain at least n values
     * @param x the values of x at which f should be evaluated
     * @param f the function for  which a spline should be computed
     * @param n the number of points to use in the spline
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, final RealValuedFunctOps f, int n)
    {
	this(x, new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, n);
    }

    /**
     * Constructor given a real-valued function  and a mode.
     * @param x the values of x at which f should be evaluated
     * @param f the function for which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, RealValuedFunction f,
			CubicSpline.Mode mode)
    {
	this(x, f, x.length, mode);
    }


        /**
     * Constructor given a real-valued function, the number of points,
     * and a mode.
     * The array x must contain at least n values
     * @param x the values of x at which f should be evaluated
     * @param f the function for which a spline should be computed
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, final RealValuedFunction f,
			int n, CubicSpline.Mode mode)
    {
	this(x, new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, n, mode,
	    new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.derivAt(args[0]);
		}
	    });
    }

    /**
     * Constructor given a real-valued function, a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f the function for  which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, RealValuedFunctOps f,
			CubicSpline.Mode mode, double derivative)
    {
	this(x, f, x.length, mode, derivative);
    }

    /**
     * Constructor given a real-valued function, the number of points,
     *  a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f the function for which a spline should be computed
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, final RealValuedFunctOps f,
			int n, CubicSpline.Mode mode, double derivative)
    {
	this(x, new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, n, mode, derivative);
    }

    /**
     * Constructor given a real-valued function, a mode, and two derivatives.
     * @param x the values of x at which f should be evaluated
     * @param f the function for   which a spline should be computed
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, RealValuedFunctOps f,
			CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	this(x, f, x.length, mode, derivative1, derivative2);
    }

    /**
     * Constructor given a function f(x), the number of points, a mode,
     * and two derivatives.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param x the values of x at which f should be evaluated
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicSpline2(double[] x, final RealValuedFunctOps f,
			int n, CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	this(x, new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, n, mode, derivative1, derivative2);
    }

    /**
     * Constructor given an array.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     */
    public CubicSpline2(double[] x, double[] y) {
	this(x, y, y.length, CubicSpline.Mode.NATURAL);
    }

    /**
     * Constructor given an array, mode, and derivative.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     */
    public CubicSpline2(double[] x, double[] y,
		       CubicSpline.Mode mode, double derivative)
    {
	super();
	cubicSpline(x, y, y.length, mode, derivative);
    }

    /**
     * Constructor given an array, mode, and two derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     */
    public CubicSpline2(double[] x, double[] y, CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	super();
	cubicSpline(x, y, y.length, mode, derivative1, derivative2);
    }

    /**
     * Constructor given an array and mode.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     */
    public CubicSpline2(double[] x, double[] y, CubicSpline.Mode mode) {
	this(x, y, y.length, mode);
    }


    /**
     * Constructor given an array and the number of points in the array
     * to use.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     */
    public CubicSpline2(double[] x, double[] y, int n) {
	this(x, y, n, CubicSpline.Mode.NATURAL);
    }
    /**
     * Constructor given an array and the number of points in the array
     * to use and a mode.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     */
    public CubicSpline2(double[] x, double[] y, int n, CubicSpline.Mode mode) {
	super();
	if (n < 2) throw new IllegalArgumentException
		       ("CubicSpline2 constructor called with n < 2");
	if (n > x.length)
	    throw new IllegalArgumentException
		("CubicSpline2 constructor called with n too large");
	cubicSpline(x, y, n, mode);
    }

    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and the value of a derivative.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at either the start or the end
     *        of the spline, depending on the mode
     */
    public CubicSpline2(double[] x, double[] y, int n,
			CubicSpline.Mode mode, double derivative)
    {
	super();
	cubicSpline(x, y, n, mode, derivative);
    }


    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and the value of two derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     */
    public CubicSpline2(double[] x, double[] y, int n,
			CubicSpline.Mode mode,
			double derivative1, double derivative2)
    {
	super();
	cubicSpline(x, y, n, mode, derivative1, derivative2);
    }

    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and an array of derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param yp the derivatives corresponding to each element of y
     *        for the fully clamped case and the appropriate number
     *        of derivatives otherwise
     */
    public CubicSpline2(double[] x, double[] y, int n,
		       CubicSpline.Mode mode, double[] yp)
    {
	super();
	cubicSpline(x, y, n, mode, yp);
    }

    /**
     * Constructor given an array, a mode and an array of derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param x the values of x at which f should be evaluated
     * @param y the values at equally spaced points
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param yp the derivatives corresponding to each element of y
     *        for the fully clamped case and the appropriate number
     *        of derivatives otherwise
     */
    public CubicSpline2(double[] x, double[] y,
				CubicSpline.Mode mode, double[] yp) {
	super();
	cubicSpline(x, y, y.length, mode, yp);
    }

    private void cubicSpline(double[] x, double[] y, int n,
			     CubicSpline.Mode mode, double... args)
    {
	if (n > y.length || n > x.length) {
	    throw new IllegalArgumentException
		("n is larger than the array length");
	}
	if (n < 2) {
	    throw new IllegalArgumentException("n must be at least 2");
	}
	if (y[0] > y[1]) {
	    double min = y[0];
	    strictlyDecreasing = true;
	    for(int i = 1; i < n; i++) {
		if (y[i] < min) {
		    min = y[i];
		} else {
		    strictlyDecreasing = false;
		    break;
		}
	    }
	} else if (y[0] < y[1]) {
	    double max = y[0];
	    strictlyIncreasing = true;
	    for (int i = 1; i < n; i++) {
		if (y[i] > max) {
		    max = y[i];
		} else {
		    strictlyIncreasing = false;
		    break;
		}
	    }
	} else {
	    strictlyIncreasing = false;
	    strictlyDecreasing = false;
	}

	this.x = new double[n];
	System.arraycopy(x, 0, this.x, 0, n);
	switch (mode) {
	case QUAD_FIT:
	    quadFit = true;
	    break;
	case PARABOLIC_RUNOUT:
	    parStart = true;
	    parEnd = true;
	    break;
	case PARABOLIC_RUNOUT_START:
	    parStart = true;
	    break;
	case PARABOLIC_RUNOUT_END:
	    parEnd = true;
	    break;
	case CLAMPED:
	    clampedStart = true;
	    clampedEnd = true;
	    if (args.length > 0) yP0 = args[0];
	    if (args.length > 1) yPn = args[1];
	    break;
	case CLAMPED_START:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0];
	    break;
	case CLAMPED_END:
	    clampedEnd = true;
	    if (args.length > 0) yPn = args[0];
	    break;
	case CLAMPED_START_PARABOLIC_END:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0];
	    parEnd = true;
	    break;
	case PARABOLIC_START_CLAMPED_END:
	    parStart = true;
	    if (args.length > 0) yPn = args[0];
	    clampedEnd = true;
	    break;
	case CLAMPED_START_CUBIC_END:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0];
	    cubicEnd = true;
	    break;
	case CUBIC_START_CLAMPED_END:
	    cubicStart = true;
	    if (args.length > 0) yPn = args[0];
	    clampedEnd = true;
	    break;
	case HERMITE:
	    hermite = true;
	    yP = new double[args.length];
	    yPU = new double[args.length];
	    for (int i = 0; i < args.length-1; i++) {
	        yP[i] = args[i]*(x[i+1] - x[i]);
		yPU[i+1] = args[i+1]*(x[i+1] -x[i]);
	    }
	}
	makeSpline(x, y, n);
    }

    double M1 = 0.0;
    double Mn = 0.0;

    double[] p0;
    double[] p1;
    double[] p2;
    double[] p3;

    int nterms = 0;

    private void makeSpline(double[] x, double[] y, int n) {
	if (n < 4) {
	    nterms = n;
	    if (n == 2) {
		// linear case;
		p0 = new double[n];
		p0[0] = y[0];
		p0[1] = (y[1] - p0[0])/(x[1]-x[0]);
		return;
	    } else if (n == 3 && !quadFit) {
		nterms = 0;
	    } else if (n == 3) {
		// quadratic case.
		double x1mx0 = x[1]-x[0];
		double x2mx0 = x[2]-x[0];
		double y1my0 = y[1]-y[0];
		double y2my0 = y[2]-y[0];
		double prod = x1mx0*x2mx0;
		double det = prod *(x2mx0 - x1mx0);
		p0 = new double[n];
		p0[0] = y[0];
		p0[1] = (y1my0*x2mx0*x2mx0 - y2my0*x1mx0*x1mx0)/det;
		p0[2] = (x1mx0*y2my0 - x2mx0*y1my0)/det;
		return;
	    } else {
		throw new Error("how did we get here (n = " + n + " )?");
	    }
	}
	double[] a = hermite? null: createA(n);
	double[] b = hermite? null: createB(n);
	double[] c = hermite? null: createC(n);
	double[] wy = hermite? null: getw(y, n);

	if (!hermite) {
	    /*
	    System.out.println("a\tb\tc\twy\ty");
	    for (int i = 0; i < a.length; i++) {
		System.out.format("%g\t%g\t%g\t%g\t%g\n",
				  a[i], b[i], c[i], wy[i], y[i]);
	    }
	    */
	    TridiagonalSolver.solve(wy, a, b, c, wy);
	}
	this.n = n;
	int nm1 = n-1;
	// int nm2 = n-2;
	// int nm3 = n-3;

	p0 = new double[nm1];
	p1 = new double[nm1];
	p2 = new double[nm1];
	p3 = new double[n];

	if (hermite) {
	    for (int i = 0; i < nm1; i++) {
		int ip1 = i+1;
		double h = x[ip1]-x[i];
		double h2 = h*h;
		double h3 = h2*h;
		p0[i] = (2.0 * y[i] + yP[i] - 2.0 * y[ip1] + yPU[ip1])/h3;
		p1[i] = (-3.0 * y[i]- 2.0 * yP[i] + 3.0 * y[ip1] - yPU[ip1])/h2;
		p2[i] = yP[i]/h;
		p3[i] = y[i];
	    }
	} else {
	    for (int i = 0; i < nm1; i++) {
		int ip1 = i + 1;
		p1[i] = wy[i];
		double hj = (x[ip1]-x[i]);
		// double hj2 = hj*hj;
		// double hj3 = hj2*hj;
		p2[i] = (y[ip1] - y[i])/hj - (2.0 *wy[i] + wy[ip1])*(hj/3.0);
		p0[i] = (wy[ip1] - wy[i])/(3*hj);
		p3[i] = y[i];
	    }
	}
	p3[nm1] = y[nm1];		// y.length == n+1
	return;
    }

    private int searchx(int ind1, int ind2, double y, boolean increasing) {
	if (y == x[ind1]) return ind1;
	if (y == x[ind2]) return ind2;
	int ind = (ind1 + ind2)/2;
	if (increasing) {
	    if (y < x[ind1] || y > x[ind2]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == x[ind]) return ind;
	    if (y < x[ind]) {
		return searchx(ind1, ind, y, increasing);
	    } else {
		return searchx(ind, ind2, y, increasing);
	    }
	} else {
	    if (y < x[ind2] || y > x[ind1]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == x[ind]) return ind;
	    if (y > x[ind]) {
		return searchx(ind1, ind, y, increasing);
	    } else {
		return searchx(ind, ind2, y, increasing);
	    }
	}
    }


    // MI9 is 9 times the inverse of M, where M is a matrix that,
    // when multiple by a vector containing the coefficients for
    // each Bernstein polynomial B<sub>i,3</sub>(t) in order of increasing
    // i, yields the coefficients for 1, t, t<sup>2</sup>, and t<sup>3</sup>.
    //
    // Thus, if we multiply MI9 by a column vector containing the coefficients
    // for 1, t, t<sup>2</sup>, and t<sup>3</sup>, and divide by 9,
    // we will get a column vector containing the coefficients for
    // the polynomials B<sub>i,3</sub>(t).
    //
    // The matrix MI9 was copied from the BicubicInterpolator class, and
    // a description of the matrix M can be found at
    // https://www.math.upenn.edu/~kadison/bernstein.pdf
    //
    /*
    private static final double MI9[][] = {{9.0, 0.0, 0.0, 0.0},
					   {9.0, 3.0, 0.0, 0.0},
					   {9.0, 6.0, 3.0, 0.0},
					   {9.0, 9.0, 9.0, 9.0}};
    */


    private static final double MI[][] = {{1.0, 0.0, 0.0, 0.0},
					  {1.0, 1.0/3.0, 0.0, 0.0},
					  {1.0, 2.0/3.0, 1.0/3.0, 0.0},
					  {1.0, 1.0, 1.0, 1.0}};


    @Override
    public double[] getBernsteinCoefficients() {
	double[] pcoefficients = new double[4];
	if (nterms > 0) {
	    pcoefficients[0] = p0[0];
	    if (nterms > 1) {
		pcoefficients[1] = p0[1];
	    }
	    if (nterms > 2) {
		pcoefficients[2] = p0[2];
	    }
	    if (nterms > 3) {
		throw new Error("nterms illegal in CubicSpline1");
	    }
	    double[] results = new double[4];
	    for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
		    results[i] += MI[i][j]*pcoefficients[j];
		}
	    }
	    return results;
	}
	int nm1 = n - 1;
	double[] cpoints = new double[3*nm1 + 1];
	cpoints[0] = p3[0];
	for (int j = 1; j < 3; j++) {
	    cpoints[j] = MI[j][0]*p3[0] + MI[j][1]*p2[0] + MI[j][2]*p1[0]
		+ MI[j][3]*p0[0];
	}
	int index = 3;
	for (int i = 1; i < nm1; i++) {
	    cpoints[index++] = p3[i];
	    for (int j = 1; j < 3; j++) {
		cpoints[index++] = MI[j][0]*p3[i] + MI[j][1]*p2[i]
		    + MI[j][2]*p1[i] + MI[j][3]*p0[i];
	    }
	}
	cpoints[index++] = p3[nm1];
	return cpoints;
    }

    @Override
    public int countKnots() {
	if (nterms > 0) return 1;
	return n;
    }

    /**
     * Compute the value of the spline for a specified argument.
     * @param xv the value at which to evaluate the spline
     * @return the value at xv
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double valueAt(double xv) throws IllegalArgumentException {
	if (nterms > 0) {
	    double xx = (xv - x[0]);
	    if (nterms == 2) {
		return p0[0] + xx * p0[1];
	    } else if (nterms == 3) {
		return p0[0] + xx * p0[1] + xx*xx*p0[2];
	    } else {
		throw new Error("nterms illegal in CubicSpline2");
	    }
	}
	int i = searchx(0, x.length-1, xv,  x[0] < x[n-1]);
	if (i < 0 || i > n-1) {
	    throw new IllegalArgumentException("argument out of range: " 
					       +xv);
	}
    	double t = (xv - x[i]);
	// System.out.println("i = " + i + ", t = " + t + ", xv = " + xv);

	if (t == 0.0) return p3[i];
    	double t2 = t * t;
	double t3 = t2 * t;
	return p0[i]*t3 + p1[i]*t2 + p2[i]*t + p3[i];
    }

    /**
     * Compute the value of derivative of the spline for a specified argument.
     * @param xv the value at which to evaluate the derivative
     * @return the value at xv
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double derivAt(double xv) throws IllegalArgumentException
    {
	if (nterms > 0) {
	    xv = (xv - x[0]);
	    if (nterms == 2) {
		return p0[1];
	    } else if (nterms == 3) {
		return  (p0[1] + 2.0 *xv*p0[2]);
	    } else {
		throw new Error("nterms illegal in CubicSpline2");
	    }
	}
	int i = searchx(0, x.length-1, xv,  x[0] < x[n-1]);
	if (i < 0 || i > n-1) {
	    throw new IllegalArgumentException("argument out of range");
	}
	if (i == (n - 1)) {
	    i--;
	}
    	double t = (xv - x[i]);

	// System.out.println("i = " + i + ", t = " + t + ", xv = " + xv);
	if (t == 0.0) return p2[i];
    	double t2 = t * t;
	return 3.0*p0[i]*t2 + 2.0*p1[i]*t + p2[i];
    }

    /**
     * Compute the value of second derivative of the spline for a specified argument.
     * @param xv the value at which to evaluate the second derivative
     * @return the value at xv
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double secondDerivAt(double xv) throws IllegalArgumentException
    {
	if (nterms > 0) {
	    xv = (xv - x[0]);
	    if (nterms == 2) {
		return 0.0;
	    } else if (nterms == 3) {
		return   2.0 * p0[2];
	    } else {
		throw new Error("nterms illegal in CubicSpline2");
	    }
	}
	int i = searchx(0, x.length-1, xv,  x[0] < x[n-1]);
	if (i < 0 || i > n-1) {
	    throw new IllegalArgumentException("argument out of range");
	}
	if (i == (n - 1)) {
	    i--;
	}
    	double t = (xv - x[i]);
	if (t == 0.0) return 2.0*p1[i];
	return 6.0*p0[i]*t + 2.0*p1[i];
    }

    /**
     * Verify that the spline's polynomials compute various values
     * consistently to an accuracy given by the argument
     * The cubic polynomials that the spline uses are supposed to
     * compute the same value, first derivative, and second derivative
     * where they meet, and each mode places constraints on the end
     * points.
     * @param limit the accuracy limit
     * @return true of the tests succeed; false otherwise
     */
    public boolean verify(double limit) {
	if (n < 3) return true;
	if (n == 3 && quadFit) return true;
	int nm1 = n - 1;
	int nm2 = n - 2;
	if (!hermite) {
	    if (parStart) {
		if (Math.abs(p1[0] - p1[1]) > limit) return false;
		if (Math.abs(p0[0]) > limit) return false;
	    } else if (clampedStart) {
		if (Math.abs(p2[0] -yP0) > limit) return false;
	    } else if (cubicStart) {
		if (Math.abs(p1[0] - 2 * p1[1] + p1[3]) > limit) return false;
	    } else {
		if (Math.abs(p1[0]) > limit) return false;
	    }

	    if (parEnd) {
		if (Math.abs(p0[nm2]) > limit) return false;
	    } else if (clampedEnd) {
		double h = x[nm1]-x[nm2];
		if (Math.abs(3*p0[nm2]*h*h + 2*p1[nm2]*h + p2[nm2] - yPn)
		    > limit)
		    return false;
	    } else if (cubicEnd) {
		if (Math.abs(p1[nm1] - 2 * p1[nm2] + p1[nm1-2]) > limit)
		    return false;
	    } else {
		double h = x[nm1]-x[nm2];
		if (Math.abs(6.0*p0[nm2]*h + 2.0*p1[nm2]) > limit) return false;
	    }
	}

	for (int i = 0; i < nm2; i++) {
	    double h = x[i+1]-x[i];
	    double h2 = h*h;
	    double h3 = h2*h;
	    if (Math.abs((p0[i]*h3 + p1[i]*h2 + p2[i]*h + p3[i]) - p3[i+1])
		> limit) {
		return false;
	    }
	    if (Math.abs((3*p0[i]*h2 + 2*p1[i]*h + p2[i]) - p2[i+1]) > limit) {
		return false;
	    }
	    if (!hermite &&
		Math.abs((6.0*p0[i]*h + 2.0*p1[i]) - 2.0 * p1[i+1]) > limit)
		return false;
	}
	return true;
    }

    public boolean isStrictlyMonotonic() {
	return strictlyIncreasing || strictlyDecreasing;
    }

    private int search(int ind1, int ind2, double y, boolean increasing) {
	if (y == p3[ind1]) return ind1;
	if (y == p3[ind2]) return ind2;
	int ind = (ind1 + ind2)/2;
	if (increasing) {
	    if (y < p3[ind1] || y > p3[ind2]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == p3[ind]) return ind;
	    if (y < p3[ind]) {
		return search(ind1, ind, y, increasing);
	    } else {
		return search(ind, ind2, y, increasing);
	    }
	} else {
	    if (y < p3[ind2] || y > p3[ind1]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == p3[ind]) return ind;
	    if (y > p3[ind]) {
		return search(ind1, ind, y, increasing);
	    } else {
		return search(ind, ind2, y, increasing);
	    }
	}
    }

    public int testSearch(double y) throws Exception {
	if (!isStrictlyMonotonic()) {
	    return -1;
	}
	if (strictlyIncreasing ) {
	    int ind = search(0, p3.length-2, y, true);
	    if (ind == p3.length && p3[ind] !=  y)
		throw new Exception(errorMsg("bracketing"));
	    if (ind >= 0 && (p3[ind] > y || p3[ind+1] < y)) {
		throw new Exception(errorMsg("bracketing"));
	    }
	    return ind;
	} else {
	    int ind = search(0, p3.length-2, y, false);
	    if (ind == p3.length && p3[ind] !=  y)
		throw new Exception(errorMsg("bracketing"));
	    if (ind >= 0 && (p3[ind] < y || p3[ind+1] > y)) {
		throw new Exception(errorMsg("bracketing"));
	    }
	    return ind;
	}
    }

    private double inversionLimit = 1.0e-8;

    /**
     * Get the inversion limit.
     * Computing the inverse in most cases requires solving a cubic
     * equation, with valid solutions being in the range [0, 1].
     * The inversion limit allows solutions in the range
     * [-inversionLimit, 1+inversionLimit] to be accepted, with values
     * outside of the interval [0, 1] replaced by 0 or 1, whichever is
     * closer.  The use of an inversion limit allows for round-off errors.
     */
    public double getInversionLimit() {
	return inversionLimit;
    }

    /**
     * Set the inversion limit.
     * Computing the inverse in most cases requires solving a cubic
     * equation, with valid solutions being in the range [0, 1].
     * The inversion limit allows solutions in the range
     * [-inversionLimit, 1+inversionLimit] to be accepted, with values
     * outside of the interval [0, 1] replaced by 0 or 1, whichever is
     * closer.  The use of an inversion limit allows for round-off errors.
     */
    public void setInversionLimit(double limit) {
	if (limit < 0.0) throw new IllegalArgumentException
			     (errorMsg("argNonNegativeD", limit));
	inversionLimit = limit;
    }


    /**
     * For a spline that represents the function y = f(x), get the value of
     * its inverse x=f<sup>-1</sup>(y).
     *
     * @param y the argument for the inverse function
     * @return the inverse evaluated at y
     * @exception IllegalArgumentException an inverse could not be computed.
     */
    public double inverseAt(double y) {
	if (nterms > 0) {
	    if (nterms == 2) {
		if (p0[1] == 0.0) {
		    throw new IllegalArgumentException(errorMsg("noInverse"));
		}
		double xv = (y - p0[0])/p0[1];
		return  xv + x[0];
	    } else if (nterms == 3) {
		double[] array = {p0[0]-y, p0[1], p0[2]};
		int m = RootFinder.solveQuadratic(array);
		if (m <= 0) {
		    throw new IllegalArgumentException(errorMsg("noInverse"));
		} else {
		    double xv = 0.0;
		    int count = 0;
		    for (int k = 0 ; k < m; k++) {
			if (array[k] >= 0.0 && array[k] <= (x[2] - x[0])) {
			    xv = array[k]; count++;
			}
		    }
		    if (count == 0) {
			throw new IllegalArgumentException
			    (errorMsg("noInverse"));
		    } else if (count == 2) {
			throw new IllegalArgumentException
			    (errorMsg("noUniqInverse"));
		    }
		    return xv + x[0];
		}
	    } else {
		throw new Error("nterms illegal in CubicSpline2");
	    }
	}
	int index;
	if (strictlyIncreasing) {
	    // binary search knots to bracket
	    index = search(0, n-1, y, true);
	} else if (strictlyDecreasing) {
	    // binary search knots to bracket
	    index = search(0, n-1, y, false);
	} else {
	    // we don't handle this case as an inverse is not always
	    // unique.
	    throw new IllegalStateException(errorMsg("notStrictlyMono"));
	}
	if (index < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", y));
	}
	if(p3[index] == y) {
	    // no need to solve a cubic equation for this case
	    return x[index];
	}
	double[] array3 = {p3[index]-y, p2[index], p1[index], p0[index]};
	int m3 = RootFinder.solveCubic(array3);
	if (m3 <= 0) {
	    throw new IllegalArgumentException(errorMsg("noInverse"));
	} else {
	    double xv = 0.0;
	    int count = 0;
	    double h = x[index+1]-x[index];
	    for (int k = 0; k < m3; k++) {
		// adjust for numerical errors.
		if (array3[k] < 0.0 && array3[k] > -h*inversionLimit) {
		    xv = 0.0;
		    count++;
		} else if (array3[k] > h
			   && array3[k] < h*(1 + inversionLimit)) {
		    xv = 1.0;
		    count++;
		} else if (array3[k] >= 0.0 && array3[k] <= h) {
		    xv = array3[k];
		    count++;
		}
	    }

	    if (count == 0) {
		throw new IllegalArgumentException
		    (errorMsg("noInverseFor", y));
	    } else if (count > 1) {
		throw new IllegalArgumentException
		    (errorMsg("noUniqInverseFor", y));
	    }
	    return xv + x[index];
	}
    }
}

//  LocalWords:  exbundle CubicSpline spline's ul li href SkycKinley
//  LocalWords:  Restrepo cubicEnd nm cubicStart CallableArgsReturns
//  LocalWords:  IllegalArgumentException lt fprime deltax yp tc twy
//  LocalWords:  ty wy hj BicubicInterpolator nterms inversionLimit
//  LocalWords:  argNonNegativeD noInverse noUniqInverse noInverseFor
//  LocalWords:  notStrictlyMono argOutOfRangeD noUniqInverseFor
