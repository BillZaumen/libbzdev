package org.bzdev.math;
import org.bzdev.lang.CallableArgsReturns;
// import java.awt.geom.QuadCurve2D;
// import java.awt.geom.CubicCurve2D;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Class providing a cubic spline for an evenly spaced set of values,
 * with each segment using a Bernstein Polynomial basis.
 * This class provides the same functionality as the class
 * {@link CubicSpline1}: the difference is in its internal
 * representation. The method
 * {@link CubicBezierSpline1#getBernsteinCoefficients()}
 * will provides more accurate values than the corresponding method
 * {@link CubicSpline1#getBernsteinCoefficients()} due to eliminating
 * the addition of terms that cancel, and those methods are used when
 * a cubic spline is used to create an instance of
 * {@link org.bzdev.geom.SplinePath2D}.
 * <P>
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
public class CubicBezierSpline1 extends CubicSpline {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    double delta;
    double x0;
    int n;

    boolean strictlyIncreasing = false;
    boolean strictlyDecreasing = false;

    /*
     * Get the minimum value in the domain of the function that the
     * spline approximates.
     * @return the minimum value
     */
    public double getDomainMin() {
	if (delta > 0) return x0;
	else return x0 - n*delta;
    }

    /*
     * Get the maximum value in the domain of the function that the
     * spline approximates.
     * @return the maximum value
     */
    public double getDomainMax() {
	if (delta > 0) return x0 + n*delta;
	else return x0;
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

    private double[] createA(int n) {
	int nm1 = n-1;
	int nm2 = n-2;
	double[] result = new double[n];
	result[0] = 0.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] = 1.0;
	}
	result[nm1] = 0;
	if (parEnd) result[nm1] = -1;
	if (clampedEnd) result[nm1] = 1.0;
	if (cubicEnd) result[nm1] = -6.0;
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
	    result[i] = 4.0;
	}
	if (clampedStart) result[0] = 2.0;
	if (clampedEnd) result[nm1] = 2.0;
	if (cubicStart) result[0] = 0.0;
	if (cubicEnd) result[nm1] = 0.0;
	return result;
    }

    private double[] createC(int n) {
	int nm1 = n - 1;
	int nm2 = n - 2;
	int nm3 = n - 3;
	double[] result = new double[n];
	result[0] = 0.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] = 1.0;
	}
	result[nm1] = 0.0;
	if (clampedStart) result[0] = 1.0;
	if (parStart) result[0] = -1.0;
	if (cubicStart) result[0] = -6.0;
	return result;
    }

    private double[] getw(double[] y, int n, double delta) {
	int nm1 = n - 1;
	int nm2 = n - 2;
	double[] result = new double[n];
	
	double factor = 3.0;

	// int limit = nm1 < m ? nm1: m;

	if (clampedStart) {
	    result[0] = 3.0 *(y[1] - y[0]) - 3.0 * yP0;
	} else {
	    result[0] = 0.0;
	}
	if (clampedEnd) {
	    result[nm1] = 3.0 * yPn - 3.0 *(y[nm1]-y[nm2]);
	} else {
	    result[nm1] = 0.0;
	}

	for (int i = 1; i < nm1; i++) {
	    result[i] = 3.0 * (y[i-1] - 2.0 * y[i] + y[i+1]);
	}
	if (cubicStart) result[0] = - result[1];
	if (cubicEnd) result[nm1] = - result[nm2];
	return result;
    }

    /**
     * Constructor given a function f(x).
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline 
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(CallableArgsReturns<Double,Double>f,
		       double x0, double x1, int n)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("tooFewPoints", n, 2));
	double deltax = (x1 - x0) / (n-1);
	this.x0 = x0;
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double x = x0 + i * deltax;
	    y[i] = f.call(x);
	}
	cubicSpline(y, n, x0, deltax, CubicSpline.Mode.NATURAL);
    }


    /**
     * Constructor given a function f(x) and a mode.
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(CallableArgsReturns<Double,Double>f,
		       double x0, double x1, int n, CubicSpline.Mode mode)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("tooFewPoints", n, 2));
	double deltax = (x1 - x0) / (n-1);
	this.x0 = x0;
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double x = x0 + i * deltax;
	    y[i] = f.call(x);
	}
	cubicSpline(y, n, x0, deltax, mode);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns. The mode must be one that
     * requires at most one derivative.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(CallableArgsReturns<Double,Double>f,
		       double x0, double x1, int n, CubicSpline.Mode mode,
		       double derivative)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("tooFewPoints", n, 2));
	double deltax = (x1 - x0) / (n-1);
	this.x0 = x0;
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double x = x0 + i * deltax;
	    y[i] = f.call(x);
	}
	cubicSpline(y, n, x0, deltax, mode, derivative);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns. The mode must be one that
     * requires at most two derivatives.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(CallableArgsReturns<Double,Double>f,
		       double x0, double x1, int n, CubicSpline.Mode mode,
		       double derivative1, double derivative2)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("tooFewPoints", n, 2));
	double deltax = (x1 - x0) / (n-1);
	this.x0 = x0;
	double[] y = new double[n];
	for (int i = 0; i < n; i++) {
	    double x = x0 + i * deltax;
	    y[i] = f.call(x);
	}
	cubicSpline(y, n, x0, deltax, mode, derivative1, derivative2);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function and its derivative are represented by instances of
     * org.bzdev.lang.CallableArgsReturns.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline 
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param fprime a CallableArgsReturns representing the derivative
     *        of the function for which a spline should be computed
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(CallableArgsReturns<Double,Double>f,
		       double x0, double x1, int n, CubicSpline.Mode mode,
		       CallableArgsReturns<Double,Double>fprime)
    {
	super();
	if (n < 2) throw new IllegalArgumentException
		       (errorMsg("tooFewPoints", n, 2));
	double deltax = (x1 - x0) / (n-1);
	this.x0 = x0;
	double[] y = new double[n];
	double[] yp;
	switch (mode) {
	case HERMITE:
	    yp = new double[n];
	    break;
	case CLAMPED:
	    yp = new double[2];
	    yp[0] = fprime.call(x0);
	    yp[1] = fprime.call(x1);
	    break;
	case CLAMPED_START:
	    yp = new double[1];
	    yp[0] = fprime.call(x0);
	    break;
	case CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x1);
	    break;
	case PARABOLIC_START_CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x1);
	    break;
	case CUBIC_START_CLAMPED_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x1);
	    break;
	case CLAMPED_START_PARABOLIC_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x0);
	    break;
	case CLAMPED_START_CUBIC_END:
	    yp = new double[1];
	    yp[0] = fprime.call(x0);
	    break;
	default:
	    yp = new double[0];
	}
	if (mode == CubicSpline.Mode.HERMITE) {
	    for (int i = 0; i < n; i++) {
		double x = x0 + i * deltax;
		y[i] = f.call(x);
		yp[i] = fprime.call(x);
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		double x = x0 + i * deltax;
		y[i] = f.call(x);
	    }
	}
	cubicSpline(y, n, x0, deltax, mode, yp);
    }

    /**
     * Constructor given a real-valued function.
     * @param f the function to evaluate
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     */
    public CubicBezierSpline1(final RealValuedFunctOps f, double x0, double x1, int n)
    {
	this(new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, x0, x1, n);
    }

    /**
     * Constructor given a real-valued function and a mode.
     * For some modes, the RealValuedFunction f must provide a
     * derivative. For all modes, it must provide a value.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     * @exception UnsupportedOperationException the real-valued function f
     *            did not provide a valueAt method or derivAt method
     */
    public CubicBezierSpline1(final RealValuedFunction f,
		       double x0, double x1, int n, CubicSpline.Mode mode)
    {
	this(new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, x0, x1, n, mode,
	    new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.derivAt(args[0]);
		}
	    });
    }

    /**
     * Constructor given a real-valued function, a mode, and a first derivative
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     * @exception UnsupportedOperationException the real-valued function f
     *            did not provide a valueAt method
     */
    public CubicBezierSpline1(final RealValuedFunctOps f,
		       double x0, double x1, int n, CubicSpline.Mode mode,
		       double derivative)
    {
	this(new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, x0, x1, n, mode, derivative);
    }

    /**
     * Constructor given a function f(x), a mode, and a first derivative
     * The function is represented by an instance of
     * org.bzdev.lang.CallableArgsReturns.
     * @param f a CallableArgsReturns representing a function for
     *        which a spline should be computed
     * @param x0 the first end point in the function's domain
     * @param x1 the second end point in the function's domain
     * @param n the number of points to use in the spline
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     * @exception IllegalArgumentException n &lt; 2 or an error
     *            occurred while evaluating the function.
     * @exception UnsupportedOperationException the real-valued function f
     *            did not provide a valueAt method
     */
    public CubicBezierSpline1(final RealValuedFunctOps f,
		       double x0, double x1, int n, CubicSpline.Mode mode,
		       double derivative1, double derivative2)
    {
	this(new CallableArgsReturns<Double,Double>() {
		public Double call(Double... args) {
		    return f.valueAt(args[0]);
		}
	    }, x0, x1, n, mode, derivative1, derivative2);
    }



    /**
     * Constructor given an array.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     */
    public CubicBezierSpline1(double[] y, double x0, double deltax) {
	this(y, y.length, x0, deltax, CubicSpline.Mode.NATURAL);
    }

    /**
     * Constructor given an array, mode, and derivative.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at the edge specified by mode
     */
    public CubicBezierSpline1(double[] y, double x0, double deltax,
		       CubicSpline.Mode mode, double derivative)
    {
	super();
	cubicSpline(y, y.length, x0, deltax, mode, derivative);
    }

    /**
     * Constructor given an array, mode, and two derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     */
    public CubicBezierSpline1(double[] y, double x0, double deltax,
		       CubicSpline.Mode mode, double derivative1, double derivative2)
    {
	super();
	cubicSpline(y, y.length, x0, deltax, mode, derivative1, derivative2);
    }

    /**
     * Constructor given an array and mode.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     */
    public CubicBezierSpline1(double[] y, double x0, double deltax, CubicSpline.Mode mode) {
	this(y, y.length, x0, deltax, mode);
    }


    /**
     * Constructor given an array and the number of points in the array
     * to use.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     */
    public CubicBezierSpline1(double[] y, int n, double x0, double deltax) {
	this(y, n, x0, deltax, CubicSpline.Mode.NATURAL);
    }
    /**
     * Constructor given an array and the number of points in the array
     * to use and a mode.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     */
    public CubicBezierSpline1(double[] y, int n, double x0, double deltax,
		       CubicSpline.Mode mode) {
	super();
	cubicSpline(y, n, x0, deltax, mode);
    }

    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and the value of a derivative.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative the derivative at either the start or the end
     *        of the spline, depending on the mode
     */
    public CubicBezierSpline1(double[] y, int n, double x0, double deltax,
		       CubicSpline.Mode mode, double derivative) {
	cubicSpline(y, n, x0, deltax, mode, derivative);
    }


    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and the value of two derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param derivative1 the derivative at the first edge
     * @param derivative2 the derivative at the second edge
     */
    public CubicBezierSpline1(double[] y, int n, double x0, double deltax,
		       CubicSpline.Mode mode, double derivative1, double derivative2) {
	super();
	cubicSpline(y, n, x0, deltax, mode, derivative1, derivative2);
    }

    /**
     * Constructor given an array and the number of points in the array
     * to use, a mode and an array of derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param n the number of indices in y that will be used
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param yp the derivatives corresponding to each element of y
     *        for the fully clamped case and the appropriate number
     *        of derivatives otherwise
     */
    public CubicBezierSpline1(double[] y, int n, double x0, double deltax,
		       CubicSpline.Mode mode, double[] yp) {
	super();
	cubicSpline(y, n, x0, deltax, mode, yp);
    }

    /**
     * Constructor given an array, a mode and an array of derivatives.
     * The indices used to compute the spline are in the range [0,n).
     * for an index i, the corresponding value of x is x0 + i*deltax
     * and y[i] will be equal to f(x).
     * @param y the values at equally spaced points
     * @param x0 the argument corresponding to index 0 of the array y
     * @param deltax the difference in the argument corresponding to
     *        successive indices of y
     * @param mode the mode for the spline as described in the API
     *        documentation for {@link CubicSpline.Mode  CubicSpline.Mode}.
     * @param yp the derivatives corresponding to each element of y
     *        for the fully clamped case and the appropriate number
     *        of derivatives otherwise
     */
    public CubicBezierSpline1(double[] y, double x0, double deltax,
		       CubicSpline.Mode mode, double[] yp) {
	super();
	cubicSpline(y, y.length, x0, deltax, mode, yp);
    }

    private void cubicSpline(double[] y, int n, double x0, double deltax,
			     CubicSpline.Mode mode, double... args)
    {
	if (n > y.length) {
	    throw new IllegalArgumentException
		(errorMsg("argGtArrayLen", n));
	}
	if (n < 2) {
	    throw new IllegalArgumentException
		(errorMsg("tooFewPoints", n, 2));
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

	this.x0 = x0;
	this.delta = deltax;
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
	    if (args.length > 0) yP0 = args[0]*delta;
	    if (args.length > 1) yPn = args[1]*delta;
	    break;
	case CLAMPED_START:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0]*delta;
	    break;
	case CLAMPED_END:
	    clampedEnd = true;
	    if (args.length > 0) yPn = args[0]*delta;
	    break;
	case CLAMPED_START_PARABOLIC_END:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0]*delta;
	    parEnd = true;
	    break;
	case PARABOLIC_START_CLAMPED_END:
	    parStart = true;
	    if (args.length > 0) yPn = args[0]*delta;
	    clampedEnd = true;
	    break;
	case CLAMPED_START_CUBIC_END:
	    clampedStart = true;
	    if (args.length > 0) yP0 = args[0]*delta;
	    cubicEnd = true;
	    break;
	case CUBIC_START_CLAMPED_END:
	    cubicStart = true;
	    if (args.length > 0) yPn = args[0]*delta;
	    clampedEnd = true;
	    break;
	case HERMITE:
	    hermite = true;
	    yP = new double[args.length];
	    for (int i = 0; i < args.length; i++) {
		yP[i] = args[i]*delta;
	    }
	}
	makeSpline(y, n, deltax);
    }

    double M1 = 0.0;
    double Mn = 0.0;

    double []beta;

    double[] p0;
    double[] p1;
    double[] p2;
    double[] p3;

    int nterms = 0;

    private void makeSpline(double[] y, int n, double delta) {
	if (n < 4) {
	    nterms = n;
	    if (n == 2) {
		// linear case;
		/*
		p0 = new double[n];
		p0[0] = y[0];
		p0[1] = y[1] - p0[0];
		*/
		beta = new double[2];
		beta[0] = y[0];
		beta[1] = y[1];
		return;
	    } else if (n == 3 && !quadFit) {
		nterms = 0;
	    } else if (n == 3) {
		// quadratic case.
		beta = new double[5];
		// p0 = new double[n];
		/*
		p0[0] = y[0];
		p0[2] = ((y[2] + y[0])/2 - y[1]);
		p0[1] = 2*(y[1]-y[0]) +(y[0]-y[2])/2;
		p0[1] = 2*(y[1]-y[0]) +(y[0]-y[2])/2;
		*/
		double p0 = y[0];
		double p1 = (4.0*y[1] - y[0] - y[2])/2.0;
		double p2 = y[2];
		// now split it into two quadratics, with t in [0,1]
		// corresponding to x in [x0,x0+delta] for the first quadratic
		// and x in [x0+delta, x0 + 2delta] for the second quadratic
		beta[0] = p0;
		beta[1] = (p0 + p1) /2;
		beta[2] = y[1];
		beta[3] = (p1 + p2) / 2;
		beta[4] = p2;
		return;
	    } else {
		throw new Error("how did we get here (n = " + n + " )?");
	    }
	}
	double[] a = hermite? null: createA(n);
	double[] b = hermite? null: createB(n);
	double[] c = hermite? null: createC(n);
	double[] wy = hermite? null: getw(y, n, delta);

	if (!hermite) {
	    TridiagonalSolver.solve(wy, a, b, c, wy);
	}
	this.n = n;
	int nm1 = n-1;
	// int nm2 = n-2;
	// int nm3 = n-3;


	beta = new double[nm1*3 + 1];
	p0 = new double[nm1];
	p1 = new double[nm1];
	p2 = new double[nm1];
	p3 = new double[n];

	/*
	double[] bp0 = new double[nm1];
	double[] bp1 = new double[nm1];
	double[] bp2 = new double[nm1];
	double[] bp3 = new double[n];
	*/

	if (hermite) {
	    int j = 0;
	    for (int i = 0; i < nm1; i++) {
		int ip1 = i+1;
		/*
		p0[i] = 2.0 * y[i] + yP[i] - 2.0 * y[ip1] + yP[ip1];
		p1[i] = -3.0 * y[i]- 2.0 * yP[i] + 3.0 * y[ip1] - yP[ip1];
		p2[i] = yP[i];
		p3[i] = y[i];
		// bezier case
		p0[i] = y[i];
		p1[i] = y[i] + yP[i]/3.0;
		p2[i] = y[ip1] - yP[ip1]/3.0;
		p3[i] = y[ip1];
		*/
		beta[j++] = y[i];
		beta[j++] = y[i] + yP[i]/3.0;
		beta[j++] = y[ip1] - yP[ip1]/3.0;
	    }
	    beta[j] = y[nm1];
	} else {
	    int j = 0;
	    beta[j++] = y[0];
	    for (int i = 0; i < nm1; i++) {
		/*
		p1[i] = wy[i];
		p2[i] = (y[i+1] - y[i]) - (2.0 *wy[i] + wy[i+1])/3.0;
		// p0[i] = y[i+1] - y[i] - p2[i] - p1[i];
		p0[i] = (wy[i+1] - wy[i])/3.0;
		p3[i] = y[i];
		// bezier case
		p1[i] = wy[i];
		p2[i] = 2.0 * y[i+1] - wy[i+1];
		p3[i] = y[i+1];
		p0[i] = y[i];
		*/
		double yi = y[i];
		double yip1 = y[i+1];
		double wyi = wy[i];
		double p2 = (yip1 - yi) - (2.0 *wyi + wy[i+1])/3.0;
		beta[j++] = y[i] + p2/3.0;
		beta[j++] = yi + (2.0*p2 + wyi)/3.0;
		beta[j++] = y[i+1];
	    }
	    if (j != beta.length)
		throw new RuntimeException("j (" + j + ") != beta.length ("
					   + (beta.length-1) + ")");
	}
	// p3[nm1] = y[nm1];		// y.length == n

	// bezier test
	/*
	if (nterms > 3) {
	    double[] bc = getBernsteinCoefficients();
	    int j = 0;
	    for (int i = 0; i < bc.length; i+=3)  {
		if (Math.abs(bc[i] - bp0[j]) > 1.e-14
		    || Math.abs(bc[i+1] - bp1[j]) > 1.e-14
		    || Math.abs(bc[i+2] - bp2[j]) > 1.e-14
		    || Math.abs(bc[i+3] - bp3[j]) > 1.e-14) {
		    System.out.format("\nbc: %s, %s, %s, %s\n",
				      bc[i], bc[i+1], bc[i+2], bc[i+3]); 
		    System.out.format("bps: %s, %s, %s, %s\n",
				      bp0[j], bp1[j], bp2[j], bp3[j]);
		    throw new RuntimeException("bc mismatch, j = " + j);
		}
		j++;
	    }
	}
	*/
	return;
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
    public int countKnots() {
	if (nterms > 0) return 1;
	return n;
    }

    @Override
    public double[] getBernsteinCoefficients() {
	double[] pcoefficients = new double[4];
	double[] cpoints;
	if (nterms > 0) {
	    if (nterms == 2) {
		// linear case: convert to cubic
		cpoints = new double[4];
		cpoints[0] = beta[0];
		cpoints[3] = beta[1];
		double diff = beta[1] - beta[0];
		cpoints[1] = beta[0] + diff/3.0;
		cpoints[2] = beta[1] - diff/3.0;
	    } else if (nterms == 3) {
		// quadratic case: raise degree by 1 to get a cubic
		cpoints = new double[7];
		cpoints[0] = beta[0];
		cpoints[1] = (beta[0] + 2.0*beta[1])/3.0;
		cpoints[2] = (2.0*beta[1] + beta[2])/3.0;
		cpoints[3] = beta[2];
		cpoints[4] = (beta[2] + 2.0*beta[3])/3.0;
		cpoints[5] = (2*beta[3] + beta[4])/3.0;
		cpoints[6] = beta[4];
	    } else {
		throw new Error("nterms illegal in CubicBezierSpline1");
	    }
	} else {
	    cpoints = (double[])beta.clone();
	}
	/*
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
	*/
	return cpoints;
    }

  /**
     * Compute the value of the spline for a specified argument.
     * @param x the value at which to evaluate the spline
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double valueAt(double x) throws IllegalArgumentException {
	double z = (x - x0)/delta;
	double ix = Math.floor(z);
	long iL = Math.round(ix);
	int i = (int) iL;
	if (nterms > 0) {
	    // x = (x - x0) / delta;
	    if (nterms == 2) {
		// return p0[0] + x * p0[1];
		return beta[0]*(1-z) + beta[1]*z;
	    } else if (nterms == 3) {
		z = z - ix;
		if (i == 2 && z == 0.0) {
		    i = 1;
		    z = 1.0;
		}
		if (i < 0 || i > 1) {
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRangeD", x));
		}
		// if (x == 0) return beta[0];
		// return p0[0] + x * p0[1] + x*x*p0[2];
		return Functions.Bernstein.sumB(beta, i*2, 2, z);
	    } else {
		throw new Error("nterms illegal in CubicBezierSpline1");
	    }
	}
	if (i < 0 || i > n) 
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", x));
	double t = z - ix;
	if (i == (n)) {
	    if (t > 0) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeD", x));
	    }
	    // return p3[n];	// special case - we explicitly added it.
	    return beta[beta.length - 1];
	}
	if (t == 0.0) return beta[3*i];
	return Functions.Bernstein.sumB(beta, i*3, 3, t);
	/*
	if (t == 0.0) return p3[i];
	double t2 = t * t;
	double t3 = t2 * t;

	return p0[i]*t3 + p1[i]*t2 + p2[i]*t + p3[i];
	*/
    }

    /**
     * Compute the value of derivative of the spline for a specified argument.
     * @param x the value at which to evaluate the derivative
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double derivAt(double x) throws IllegalArgumentException
    {
	double z = (x - x0)/delta;
	double ix = Math.floor(z);
	long iL = Math.round(ix);
	int i = (int) iL;
	if (nterms > 0) {
	    // x = (x - x0) / delta;
	    if (nterms == 2) {
		// return p0[0] + x * p0[1];
		return (beta[1] - beta[0])/delta;
	    } else if (nterms == 3) {
		z = z - ix;
		if (i == 2 && z == 0.0) {
		    i = 1;
		    z = 1.0;
		}
		if (i < 0 || i > 1) {
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRangeD", x));
		}
		// if (x == 0) return beta[0];
		// return p0[0] + x * p0[1] + x*x*p0[2];
		return Functions.Bernstein.dsumBdx(beta, i*2, 2, z)/delta;
	    } else {
		throw new Error("nterms illegal in CubicBezierSpline1");
	    }
	}
	if (i < 0 || i > n) 
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", x));
	double t = z - ix;
	if (i == (n-1)) {
	    if (t > 0) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeD", x));
	    }
	    return 3*(beta[beta.length - 1] - beta[beta.length-2])/delta;
	}
	int i3 = 3*i;
	/*
	if (t == 0.0 && beta.length == i3+1) {
	    System.out.println ("i = " + i + ", t = " + t
				+ ", beta.length = " + beta.length
				+ "n = " +n);
	}
	*/
	double b1 = beta[i3+1];
	if (t == 0.0) return 3*(b1-beta[i3])/delta;
	double b2 = beta[i3+2];
	double onemt = 1 - t;
	double result = 3*((b1-beta[i3])*onemt*onemt
			   + 2*(b2-b1)*t*onemt + (beta[i3+3]-b2)*t*t)/delta;
	/*
	double result2 = Functions.Bernstein.dsumBdx(beta, i3, 3, t)/delta;
	if (Math.abs(result - result2) > 1.e-14) {
	    System.out.println("result = " + result + ", result2 = " +result2);
	    throw new RuntimeException();
	}
	*/
	return result;
    }

    /**
     * Compute the value of second derivative of the spline for a specified argument.
     * @param x the value at which to evaluate the second derivative
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
     */
    public double secondDerivAt(double x) throws IllegalArgumentException
    {
	double z = (x - x0)/delta;
	double delta2 = delta*delta;
	double ix = Math.floor(z);
	long iL = Math.round(ix);
	int i = (int) iL;
	if (nterms > 0) {
	    // x = (x - x0) / delta;
	    if (nterms == 2) {
		return 0.0;
	    } else if (nterms == 3) {
		z = z - ix;
		if (i == 2 && z == 0.0) {
		    i = 1;
		    z = 1.0;
		}
		if (i < 0 || i > 1) {
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRangeD", x));
		}
		// if (x == 0) return beta[0];
		// return p0[0] + x * p0[1] + x*x*p0[2];
		return Functions.Bernstein.d2sumBdx2(beta, i*2, 2, z)/delta2;
	    } else {
		throw new Error("nterms illegal in CubicBezierSpline1");
	    }
	}
	if (i < 0 || i > n) 
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", x));
	double t = z - ix;
	int i3 = i*3;
	if (i == (n-1)) {
	    if (t > 0) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeD", x));
	    }
	    // return p3[n];	// special case - we explicitly added it.
	    return 6*(beta[beta.length-1] + beta[beta.length-3]
		    -2*beta[beta.length-2])/delta2;
	}
	// explicitly computing it give
	// 6[(b2 + b0 -2b1)(1-x) + (b3+b1-2b2)x]
	double b1 = beta[i3+1];
	double b2 = beta[i3+2];
	if (t == 0.0) return 6*(b2 + beta[i3] - 2*b1)/delta2;
	double result = 6*((b2 + beta[i3] - 2*b1)*(1-t)
			   + (beta[i3+3] + b1 - 2*b2)*t)/delta2;
	/*
	double result2 = Functions.Bernstein.d2sumBdx2(beta, i3, 3, t)/delta2;
	if (Math.abs(result - result2) > 1.e-12) {
	    System.out.println("result = " + result +", result2 = " + result2);
	    throw new RuntimeException();
	}
	*/
	return result;
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
		// if (Math.abs(p1[0] - p1[1]) > limit) return false;
		/*
		if (Math.abs(Functions.Bernstein.d2sumBdx2
			     (beta, 0, 3, 0.0) - Functions.Bernstein.d2sumBdx2
			     (beta, 3, 3, 0.0)) > limit) return false;
		*/
		// if (Math.abs(p0[0]) > limit) return false;
		if (Math.abs(Functions.Bernstein.dIsumBdxI(3, beta, 0, 3, 0.0))
		    > limit) return false;
	    } else if (clampedStart) {
		// if (Math.abs(p2[0] -yP0) > limit) return false;
		if (Math.abs(Functions.Bernstein.dsumBdx(beta, 0, 3, 0.0) - yP0)
		    > limit) return false;
	    } else if (cubicStart) {
		// if (Math.abs(p1[0] - 2*p1[1] + p1[3]) > limit) return false;
		if (Math.abs(Functions.Bernstein.d2sumBdx2(beta,0,3,0.0)
			     -2*Functions.Bernstein.d2sumBdx2(beta,0,3,1.0)
			     +Functions.Bernstein.d2sumBdx2(beta,3,3,1.0))
		    > limit) return false;
	    } else {
		if (Math.abs(Functions.Bernstein.d2sumBdx2
			     (beta, 0, 3, 0.0)) > limit) {
		    return false;
		}
		// if (Math.abs(p1[0]) > limit) return false;
	    }

	    if (parEnd) {
		if (Math.abs(p0[nm2]) > limit) return false;
		if (Math.abs(Functions.Bernstein.dIsumBdxI
			     (3, beta, 3*nm2, 3, 0.0)) > limit) return false;
	    } else if (clampedEnd) {
		/*
		if (Math.abs(3*p0[nm2] + 2*p1[nm2] + p2[nm2] - yPn) > limit)
		    return false;
		*/
		if (Math.abs(Functions.Bernstein.dsumBdx
			     (beta, 3*nm2, 3, 1.0) - yPn)
		    > limit) return false;
	    } else if (cubicEnd) {
		/*
		if (Math.abs(p1[nm1] - 2 * p1[nm2] + p1[nm1-2]) > limit)
		    return false;
		*/
		int nm3 = nm2-1;
		if (Math.abs(Functions.Bernstein.d2sumBdx2
			     (beta,nm3*3,3,0.0)
			     -2*Functions.Bernstein.d2sumBdx2
			     (beta,nm3*3,3,1.0)
			     +Functions.Bernstein.d2sumBdx2
			     (beta,3*nm2,3,1.0))
		    > limit) return false;
	    } else {
		if (Math.abs(Functions.Bernstein.d2sumBdx2
			     (beta, nm2*3, 3, 1.0)) > limit) {
		    return false;
		}
		// if (Math.abs(6.0*p0[nm2] + 2.0*p1[nm2]) > limit) return false;
	    }
	}
	for (int i = 0; i < nm2; i++) {
	    if (Math.abs((p0[i] + p1[i] + p2[i] + p3[i]) - p3[i+1]) > limit)
		return false;
	    if (Math.abs((3*p0[i] + 2*p1[i] + p2[i]) - p2[i+1]) > limit)
		return false;
	    if (!hermite &&
		Math.abs((6.0*p0[i] + 2.0*p1[i]) - 2.0 * p1[i+1]) > limit)
		return false;
	    if (Math.abs(Functions.Bernstein.sumB(beta,3*i,3,1.0)
			 - Functions.Bernstein.sumB(beta,3*(i+1),3,0.0))
		> limit) return false;
	    if (Math.abs(Functions.Bernstein.dsumBdx(beta,3*i,3,1.0)
			 - Functions.Bernstein.dsumBdx(beta,3*(i+1),3,0.0))
		> limit) return false;
	    if (!hermite &&
		Math.abs(Functions.Bernstein.d2sumBdx2(beta,3*i,3,1.0)
			 - Functions.Bernstein.d2sumBdx2(beta,3*(i+1),3,0.0))
		> limit) return false;
	}
	return true;
    }

    public boolean isStrictlyMonotonic() {
	return strictlyIncreasing || strictlyDecreasing;
    }

    private int search(int ind1, int ind2, double y, boolean increasing) {
	int f = 3;
	switch (nterms) {
	case 0: f = 3;
	    break;
	case 1: f = 1;
	    break;
	default:
	    f = 2;
	    break;
	}
	if (y == beta[f*ind1]) return ind1;
	if (y == beta[f*ind2]) return ind2;
	int ind = (ind1 + ind2)/2;
	if (increasing) {
	    if (y < beta[f*ind1] || y > beta[f*ind2]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == beta[f*ind]) return ind;
	    if (y < beta[f*ind]) {
		return search(ind1, ind, y, increasing);
	    } else {
		return search(ind, ind2, y, increasing);
	    }
	} else {
	    if (y < beta[f*ind2] || y > beta[f*ind1]) {
		return -1;	// y out of range
	    }
	    if (ind1 + 1 == ind2) return ind;
	    if (y == beta[f*ind]) return ind;
	    if (y > beta[f*ind]) {
		return search(ind1, ind, y, increasing);
	    } else {
		return search(ind, ind2, y, increasing);
	    }
	}
    }

    /*
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
    */
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
	if (limit < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative", limit));
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
		/*
		if (p0[1] == 0.0) {
		    throw new IllegalArgumentException(errorMsg("noInverse"));
		}
		*/
		double[] barray = {beta[0] - y, beta[1] - y};
		double[] result = new double[1];
		int n = RootFinder.solveBezier(barray, 0, 1, result);
		if (n != 1) {
		    throw new IllegalArgumentException(errorMsg("noInverse"));
		} else {
		    return delta * result[0] + x0;
		}
		/*
		double x = (y - p0[0])/p0[1];
		return  x*delta + x0;
		*/
	    } else if (nterms == 3) {
		double[] array = {beta[0]-y, beta[1] - y, beta[2] - y};
		double[] results = new double[2];
		int m = RootFinder.solveBezier(array, 0, 2, results);
		/*
		double[] array = {p0[0]-y, p0[1], p0[2]};
		int m = RootFinder.solveQuadratic(array);
		*/
		if (m <= 0) {
		    throw new IllegalArgumentException(errorMsg("noInverse"));
		} else {
		    double x = 0.0;
		    int count = 0;
		    for (int k = 0 ; k < m; k++) {
			if (results[k] >= 0.0 && results[k] <= 2.0) {
			    x = results[k]; count++;
			}
		    }
		    if (count == 0) {
			throw new IllegalArgumentException
			    (errorMsg("noInverse"));
		    } else if (count == 2) {
			throw new IllegalArgumentException
			    (errorMsg("noUniqInverse"));
		    }
		    return x*delta + x0;
		}
	    } else {
		throw new Error("nterms illegal in CubicBezierSpline1");
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
	    throw new IllegalStateException
		(errorMsg("notStrictlyMono"));
	}
	if (index < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", y));
	}
	int ind3 = index * 3;
	double[] array3 = {beta[ind3] - y, beta[ind3+1] - y,
			   beta[ind3+2] - y, beta[ind3+3] - y};
	double[] results3 = new double[3];
	int m3 = RootFinder.solveBezier(array3, 0, 3, results3);
	/*
	if(p3[index] == y) {
	    // no need to solve a cubic equation for this case
	    return x0 + delta * index;
	}

	double[] array3 = {p3[index]-y, p2[index], p1[index], p0[index]};
	int m3 = RootFinder.solveCubic(array3);
	*/
	if (m3 <= 0) {
	    throw new IllegalArgumentException(errorMsg("noInverse"));
	} else {
	    double x = 0.0;
	    int count = 0;
	    for (int k = 0; k < m3; k++) {
		// adjust for numerical errors.
		if (results3[k] < 0.0 && results3[k] > -inversionLimit) {
		    x = 0.0;
		    count++;
		} else if (results3[k] > 1.0 &&
			   results3[k] < (1.0 + inversionLimit)) {
		    x = 1.0;
		    count++;
		} else if (results3[k] >= 0.0 && results3[k] <= 1.0) {
		    x = results3[k];
		    count++;
		}
	    }
	    if (count == 0) {
		String sr = " - spline roots (";
		for (int k = 0; k < m3; k++) {
		    sr += ((k==0)? "": ", ") + array3[k];
		}
		sr += ")";
		throw new IllegalArgumentException
		    (errorMsg("noInverseFor", (y + sr)));
	    } else if (count > 1) {
		String sr = " - spline roots (";
		for (int k = 0; k < m3; k++) {
		    sr += ((k==0)? "": ", ") + array3[k] + ")";
		}
		throw new IllegalArgumentException
		    (errorMsg("noUniqInverseFor", (y + sr)));
	    }
	    double z = x + index;
	    return z * delta + x0;
	}
    }
}

//  LocalWords:  exbundle CubicSpline CubicBezierSpline spline's ul
//  LocalWords:  getBernsteinCoefficients li href Restrepo nm lt yp
//  LocalWords:  CallableArgsReturns IllegalArgumentException fprime
//  LocalWords:  tooFewPoints RealValuedFunction valueAt derivAt bp
//  LocalWords:  UnsupportedOperationException deltax argGtArrayLen
//  LocalWords:  yP ip bezier wy nterms bc nbc RuntimeException yPn
//  LocalWords:  BicubicInterpolator cpoints argOutOfRangeD errorMsg
//  LocalWords:  inversionLimit argNonNegative noInverse RootFinder
//  LocalWords:  solveQuadratic noUniqInverse notStrictlyMono
//  LocalWords:  solveCubic noInverseFor noUniqInverseFor
