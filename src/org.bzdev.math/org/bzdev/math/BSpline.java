package org.bzdev.math;
import java.util.LinkedList;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class implementing B-splines.
 * This class supports both periodic and non-periodic B-splines.
 * Periodic B-splines can be used to approximate period functions.
 * Additional control points and knots are added for periodic B-splines.
 * (see below).
 * <P>
 * A B-spline consists of
 * <ul>
 *   <li> an increasing finite sequence of real numbers called "knots":
 *        u<sub>0</sub> &le; u<sub>1</sub> &le; ... &le; u<sub>m</sub>
 *   <li> a set of basis functions.
 *   <li> a set of control points (just numbers for this class) denoted as
 *        P<sub>0</sub>, P<sub>1</sub>, &hellip; P<sub>n</sub>.
 *   <li> a degree&mdash;normally denoted as p.  The degree of a B-spline
*        must satisfy p = m - n - 1.
 * </ul>
 * <P>
 * The basis functions are defined as follows:
 * <ul>
 *    <li> N<sub>i,0</sub>(u) = 1 if u<sub>i</sub> &le; u &lt; u<sub>i+1</sub>.
 *    <li> N<sub>i,0</sub>(u) = 0 if u &lt; u<sub>i</sub> or u<sub>i+1</sub>
 *         &le; u.
 *    <li> N<sub>i,p</sub>(u) =
 *         N<sub>i,p-1</sub>(u) (u-u<sub>i</sub>) &frasl; (u<sub>i+p</sub>-u<sub>i</sub>)
 *         + N<sub>i+1,p-1</sub>(u) (u<sub>i+p+1</sub>-u)
 *           &frasl; (u<sub>i+p+1</sub>-u<sub>i+1</sub>) for p &gt; 0.
 * </ul>
 * The value of the function defined by this spline at a point u is equal to
 * <blockquote>
 *    &sum;<sup>n</sup><sub>i=0</sub> N<sub>i,p</sub>(u)P<sub>i</sub>.
 * </blockquote>
 * with a domain [u<sub>p</sub>, u<sub>m-p</sub>] for non-periodic splines
 * and (-&infin;, &infin;) for periodic splines (for a periodic spline,
 * the value of u will be shifted by an appropriate multiple of the period
 * so as to be in the interval [u<sub>p</sub>, u<sub>m-p</sub>], at at
 * the end points of this interval, the values will match, as will their
 * derivatives.
 * <P>
 * The documentation for the constructor
 * {@link #BSpline(int,double[],double[],boolean)} describes how
 * periodic and non-periodic B-splines are created.  A couple of constructors
 * use a simplified set of arguments. These use a {@link BSpline.Mode}
 * argument to specify a menu of options for setting up the knots array
 * and for determining if the spline is periodic.
 */
public class BSpline  extends RealValuedFunction {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private double[] uarray;
    private double[] carray;
    boolean needQarray = true;
    boolean needQQarray = true;
    private double[] qarray;
    private double[] qqarray;

    private int degree;
    private boolean periodic;

    double start = 0.0;
    double end = 0.0;
    double period = 0.0;

    protected void setControlPoints(double[] cpoints)
	throws IllegalArgumentException
    {
	if (cpoints == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg"));
	}
	if (cpoints == null || cpoints.length != carray.length) {
	    throw new IllegalArgumentException
		(errorMsg("cptsWrongLength", carray.length, cpoints.length));
	}
	System.arraycopy(cpoints, 0, carray, 0, carray.length);
    }

    public double[] getControlPoints() {
	return (double[])carray.clone();
    }

    public double[] getControlPoints(double[] array) {
	if (array.length < carray.length) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	System.arraycopy(carray, 0, array, 0, carray.length);
	return array;
    }

    public int getNumberOfControlPoints() {return carray.length;}

    /**
     * Determine if the spline is a periodic spline.
     * @return true if the spline is periodic; false otherwise.
     */
    public boolean isPeriodic() {
	return periodic;
    }

    /**
     * Get the starting value for a period.
     * A periodic B-spline explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a non-periodic B-spline, the domain minimum is returned.
     * @return the starting value for a period if the spline is periodic;
     *         otherwise the domain minimum
     */
    public double getPeriodStart() {
	return start;
    }

    /**
     * Get the ending value for a period.
     * A periodic B-spline explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a non-periodic B-spline, the domain maximum is returned.
     * @return the ending value for a period if the spline is periodic;
     *         otherwise the domain maximum
     */
    public double getPeriodEnd() {
	return end;
    }

    /**
     * Get the period.
     * A periodic B-spline explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a periodic B-spline, the period is numerically equal to
     * getPeriodEnd()-getPeriodStart().
     * @return the period for a periodic spline; 0.0 if the spline is not
     *         periodic
     */
    public double getPeriod() {
	return period;
    }

    private double getStandardU(double u) {
	if (periodic) {
	    if (u < start || u > end) {
		u -= period*Math.floor(u/period);
	    }
	}
	return u;
    }

    /**
     * Get the degree if this B-spline.
     * The smallest degree is zero (some authors use 1, in which
     * case the degree will be larger by 1 than the value
     * returned by this method).
     * @return this B-spline's degree.
     */
    public int getDegree() {return degree;}

    private void computeQarray() {
	synchronized (this) {
	    if (needQarray) {
		qarray = (degree > 0)? new double[carray.length-1]: null;
		if (qarray != null) {
		    for (int i = 0; i < qarray.length; i++) {
			int ip1 = i + 1;
			double alpha = degree
			    / (uarray[ip1+degree] - uarray[ip1]);
			qarray[i] = alpha * (carray[ip1] - carray[i]);
		    }
		}
		needQarray = false;
	    }
	}
    }

    private void computeQQarray() {
	synchronized (this) {
	    if (needQQarray) {
		if (needQarray) computeQarray();
		qqarray = (degree > 1)? new double[qarray.length-1]: null;
		if (qqarray != null) {
		    for (int i = 0; i < qqarray.length; i++) {
			int ip1 = i + 1;
			int ip2 = i + 2;
			double alpha = (degree - 1)
			    / (uarray[ip1+degree] - uarray[ip2]);
			qqarray[i] = alpha * (qarray[ip1] - qarray[i]);
		    }
		}
		needQQarray = false;
	    }
	}
    }


    /**
     * Constructor for open B-splines.
     * A full description is provided by the constructor
     * {@link #BSpline(int,double[],double[],boolean)}.
     * The degree of the B-spline is equal to
     * knots.length - cpoints.length - 1.
     * @param knots the knots for the B-spline to be constructed
     * @param cpoints the control points for the B-spline to be constructed
     * @see #BSpline(int,double[],double[],boolean)
     */
    public BSpline(double[] knots, double[] cpoints)
	throws IllegalArgumentException
    {
	this(knots.length - cpoints.length - 1, knots, cpoints, false);
    }

    /**
     * Constructor.
     * There are two cases depending on whether the B-spline will be
     * periodic or non-periodic.  For the non-periodic case,
     * if m is the length of the knots array, n is the number of control
     * points, and p is the degree if the B-spline, then the following
     * constraint must be satisfied:
     * <blockquote>
     * &nbsp;&nbsp;&nbsp;&nbsp; n + p + 1 = m
     * </blockquote>
     * or
     * <blockquote>
     * &nbsp;&nbsp;&nbsp;&nbsp; m - n - 1 = p.
     * </blockquote>
     * or
     * <blockquote>
     * &nbsp;&nbsp;&nbsp;&nbsp; m - p - 1 = n.
     * </blockquote>
     * The degree matches the highest possible degree of the
     * polynomial representation of the basis functions (some authors
     * start the sequence at 1 instead of 0, in which case the degree
     * the smallest integer larger than the degree of the
     * polynomials). The domain of the spline is
     * [u<sub>s</sub>, u<sub>e</sub>].
     * where u<sub>s</sub>=knots[degree] and
     * u<sub>e</sub>=knots[knots.length-p-1].
     * <P>
     * For the periodic case, the values of the spline over a range
     * [u<sub>s</sub>,u<sub>e</sub>] repeat indefinitely in either
     * direction, and the knots array's initial and ending components
     * will be u<sub>s</sub> and u<sub>e</sub> respectively. As a
     * result, the period is u<sub>e</sub>-u<sub>s</sub>.  For a
     * periodic spline whose degree is p, the length of the
     * control-points array will either be one less than the length of
     * the knots array, or (p-1) less than the length of the knots
     * array.  The constructor will augment these arrays as follows:
     * <ul>
     *   <li> p components will be inserted before the initial
     *        component of the knots array, and p components will be
     *        inserted after the last component of the knots
     *        array. The p components before the last original entry
     *        will be copied to the start of the array in the same
     *        order as in the original array, with values decreased by
     *        the period.  The first p components past the initial
     *        component of the original array array will be copied to
     *        the end of the array, with values increased by the
     *        period.
     *   <li> When the control-points array's length is one less than
     *        the length of the knots array, p components will be
     *        inserted before the initial component of the
     *        control-points array. The last p components of the
     *        original array will be copied to the start of the
     *        augmented array in the same order as in the original
     *        array.
     * </ul>
     * Note: Copying of control points must be considered when fitting
     * a periodic spline to a function or a set of data points.
     * @param degree a non-negative integer giving the  degree of the
     *        B-spline to be constructed
     * @param knots the knots for the B-spline to be constructed
     * @param cpoints the control points for the B-spline to be constructed
     * @param periodic true if the spline is periodic; false if it is open.
     */
    public BSpline(int degree, double[] knots, double[] cpoints,
		   boolean periodic)
	throws IllegalArgumentException
    {
	super();
	/*
	  m = knots.length-1;
	  p = degree;
	  n = cpoints.length-1;
	  n + p + 1 = m <==> cpoints.length + degree + 1 = knots.length
	*/
	if (degree < 0) throw new IllegalArgumentException
			    (errorMsg("argNonNegative1", degree));
	if (periodic){
	    boolean padmode;
	    if (cpoints.length + 1 == knots.length) {
		padmode = true;
	    } else if (cpoints.length+1 == knots.length + degree) {
		padmode = false;
	    } else {
		throw new IllegalArgumentException(errorMsg("incompatibleArrayLengths"));
	    }
	    uarray = new double[knots.length + 2*degree];
	    carray = new double[cpoints.length +  (padmode? degree: 0)];
	    System.arraycopy(knots, 0, uarray, degree, knots.length);
	    if (padmode) {
		System.arraycopy(cpoints, 0, carray, degree, cpoints.length);
	    } else {
		System.arraycopy(cpoints, 0, carray, 0, cpoints.length);
	    }
	    int offset = knots.length + degree ;
	    for (int i = 0; i < degree; i++) {
		uarray[i] = knots[0] + knots[knots.length - 1 - degree + i]
		    - knots[knots.length-1];
		uarray[offset+i] = knots[knots.length-1] +
		    knots[i+1] - knots[0];
		if (padmode) {
		    carray[i] = cpoints[cpoints.length-degree+i];
		}
	    }
	    start = knots[0];
	    end = knots[knots.length-1];
	    if (!(start < end)) {
		throw new IllegalArgumentException(errorMsg("knotSpanEmpty", start, end));
	    }
	    period = end - start;
	} else {
	    if(cpoints.length + degree + 1 != knots.length) {
		throw new IllegalArgumentException(errorMsg("incompatibleArrayLengths"));
	    }
	    uarray = (double[]) knots.clone();
	    carray = (double[]) cpoints.clone();
	    start = uarray[degree];
	    end = uarray[uarray.length - 1 - degree];
	}
	this.degree = degree;
	this.periodic = periodic;
    }

    private LeastSquaresFit lsf = null;

    /**
     * Get the least squares fit, if any, used to initialize this
     * BSpline.
     * This method will always return null if it is called two or more times.
     * It is intended for use by subclasses of LeastSquaresFit or other
     * classes that need information about a least squares fit when the
     * fit is created.
     * @return the least squares fit; null if there is none.
     */
    public LeastSquaresFit getLSF() {
	LeastSquaresFit result = lsf;
	lsf = null;
	return result;
    }

    /**
     * Constructor specifying a B-spline's degree, knots, and fitting
     * the spline to a set of data points.
     * The use of knots is the same as that for the constructor
     * {@link #BSpline(int,double[],double[],boolean)}.
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     */
    public BSpline(int degree, double[] knots, boolean periodic,
		   double[] x, double[] y) {
	this(degree, knots, 
	     new double[periodic?(knots.length-1 + degree):
			(knots.length - degree - 1)],
	     periodic);
	double[] xorig = x;
	double[] yorig = y;
	if (periodic) {
	    double[] yy = new double[y.length*3-2];
	    double[] xx = new double[x.length*3-2];
	    for (int i = 0; i < x.length-1; i++) {
		xx[i] = x[i]-period;
	    }
	    System.arraycopy(x, 0, xx, x.length-1, x.length);
	    for (int i = 0; i < x.length-1; i++) {
		xx[i + x.length-1 + x.length] = x[i+1] + period;
	    }
	    System.arraycopy(y, 0, yy, 0, y.length-1);
	    System.arraycopy(y, 0, yy, y.length-1, y.length);
	    System.arraycopy(y, 1, yy, y.length + y.length-1, y.length-1);
	    y = yy;
	    x = xx;
	}
	// Now do a least-squares fit of the data.
	RealValuedFunction[] functions =
	    new RealValuedFunction[carray.length];
	for (int ind = 0; ind < functions.length; ind++) {
	    final int i = ind;
	    functions[i] = new RealValuedFunction() {
		    public double valueAt(double u) {
			return N(i, BSpline.this.degree, u);
		    }
		};
	}
	lsf = new LeastSquaresFit.FunctionBasis(x, y, functions);
	lsf.getParameters(carray);
	if (periodic) {
	    lsf = new LeastSquaresFit.FunctionBasis(xorig, yorig, functions);
	    lsf.setParameters(carray);
	}
    }

    /**
     * Constructor specifying a B-spline's degree, knots, and fitting
     * the spline to a set of data points, all with the same error.
     * The use of knots is the same as that for the constructor
     * {@link #BSpline(int,double[],double[],boolean)}.
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviation of the 'y' values
     *        for the least-squares fit.
     */
    public BSpline(int degree, double[] knots, boolean periodic,
		   double[] x, double[] y, double sigma) {
	this(degree, knots,
	     new double[periodic?(knots.length-1 + degree):
			(knots.length - degree - 1)],
	     periodic);
	double[] xorig = x;
	double[] yorig = y;
	if (periodic) {
	    double[] yy = new double[y.length*3-2];
	    double[] xx = new double[x.length*3-2];
	    for (int i = 0; i < x.length-1; i++) {
		xx[i] = x[i]-period;
	    }
	    System.arraycopy(x, 0, xx, x.length-1, x.length);
	    for (int i = 0; i < x.length-1; i++) {
		xx[i + x.length-1 + x.length] = x[i+1] + period;
	    }
	    System.arraycopy(y, 0, yy, 0, y.length-1);
	    System.arraycopy(y, 0, yy, y.length-1, y.length);
	    System.arraycopy(y, 1, yy, y.length + y.length-1, y.length-1);
	    y = yy;
	    x = xx;
	}
	// Now do a least-squares fit of the data.
	RealValuedFunction[] functions =
	    new RealValuedFunction[carray.length];
	for (int ind = 0; ind < functions.length; ind++) {
	    final int i = ind;
	    functions[i] = new RealValuedFunction() {
		    public double valueAt(double u) {
			return N(i, BSpline.this.degree, u);
		    }
		};
	}
	lsf = new LeastSquaresFit.FunctionBasis(x, y, sigma, functions);
	lsf.getParameters(carray);
	if (periodic) {
	    lsf = new LeastSquaresFit.FunctionBasis(xorig, yorig, sigma,
						    functions);
	    lsf.setParameters(carray);
	}
    }

    /**
     * Constructor specifying a B-spline's degree, knots, and fitting
     * the spline to a set of data points with errors.
     * The use of knots is the same as that for the constructor
     * {@link #BSpline(int,double[],double[],boolean)}.
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviations for the values provided by
     *        the argument y
     */
    public BSpline(int degree, double[] knots, boolean periodic,
		   double[] x, double[] y, double[] sigma) {
	this(degree, knots, 
	     new double[periodic?(knots.length-1 + degree):
			(knots.length - degree - 1)],
	     periodic);
	double[] xorig = x;
	double[] yorig = y;
	double[] sorig = sigma;
	if (periodic) {
	    double[] yy = new double[y.length*3-2];
	    double[] ssigma = new double[sigma.length*3-2];
	    double[] xx = new double[x.length*3-2];
	    for (int i = 0; i < x.length-1; i++) {
		xx[i] = x[i]-period;
	    }
	    System.arraycopy(x, 0, xx, x.length-1, x.length);
	    for (int i = 0; i < x.length-1; i++) {
		xx[i + x.length-1 + x.length] = x[i+1] + period;
	    }
	    System.arraycopy(sigma, 0, ssigma, 0, sigma.length-1);
	    System.arraycopy(sigma, 0, ssigma, sigma.length-1, sigma.length);
	    System.arraycopy(sigma, 1, ssigma, sigma.length + sigma.length - 1,
			     sigma.length-1);
	    System.arraycopy(y, 0, yy, 0, y.length-1);
	    System.arraycopy(y, 0, yy, y.length-1, y.length);
	    System.arraycopy(y, 1, yy, y.length + y.length-1, y.length-1);
	    y = yy;
	    sigma = ssigma;
	    x = xx;
	}
	// Now do a least-squares fit of the data.
	RealValuedFunction[] functions =
	    new RealValuedFunction[carray.length];
	for (int ind = 0; ind < functions.length; ind++) {
	    final int i = ind;
	    functions[i] = new RealValuedFunction() {
		    public double valueAt(double u) {
			return N(i, BSpline.this.degree, u);
		    }
		};
	}
	lsf = new LeastSquaresFit.FunctionBasis(x, y, sigma, functions);
	lsf.getParameters(carray);
	if (periodic) {
	    // For the periodic case, redo the least squares fit
	    // using only the original data so we compute the
	    // covariance matrix using the actual data.
	    lsf = new LeastSquaresFit.FunctionBasis(xorig, yorig, sorig,
						    functions);
	    lsf.setParameters(carray);
	}
    }


    /**
     * Modes for creating a B-spline from a set of data points.
     * The enumeration constants determine how the ends of the
     * spline are treated.  The constructors that use these
     * constants will compute the knots used by the spline.
     */
    public static enum Mode {
	/**
	 * Knots are to be suitable for a non-clamped B-spline.
	 */
	UNCLAMPED,
	/**
	 * Knots are to be suitable for a clamped B-spline.
	 */
	CLAMPED,
	/**
	 * Knots are to be suitable for a B-spline clamped on its left side
	 * (lowest value in its domain).
	 */
	CLAMPED_LEFT,
	/**
	 * Knots are to be suitable for a B-spline clamped on its right side
	 * (highest value in its domain).
	 */
	CLAMPED_RIGHT,
	/**
	 * Knots are to be suitable for a periodic B-spline.
	 */
	PERIODIC
    }

    /**
     * Create a knot array for a B-Spline.
     * @param degree the degree of the B-Spline
     * @param n the number of control points for the B-Spline
     * @param mode the mode for the B-spline (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSpline.Mode.UNCLAMPED)
     * @param x the x values used to create the B-Spline
     */
    public static double[] createKnots(int degree, int n, Mode mode, double[] x)
    {
	if (mode == null) {
	    mode = BSpline.Mode.UNCLAMPED;
	}
	if (n < 0) {
	    throw new IllegalArgumentException(errorMsg("argNonNegative", n));
	}
	if (degree < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative", degree));
	}
	if (n <= degree) {
	    throw new IllegalArgumentException
		(errorMsg("degreeTooLarge", degree, n));
	}
	int m = (mode == Mode.PERIODIC)? (n+1): n + degree + 1;
	double[] knots = new double[m];
	switch(mode) {
	case UNCLAMPED:
	    {
		int q = n-1-degree+1;
		double[] ts = new double[n-degree+1];
		double r = ((double)x.length)/q;
		for (int i = 1; i < q; i++) {
		    ts[i] = x[(int)Math.round(i*r)];
		}
		ts[0] = x[0];
		ts[q] = x[x.length-1];
		knots[degree] = ts[0];
		knots[m-degree-1] = ts[q];
		for (int i = 1; i < ts.length; i++) {
		    knots[i+degree] = ts[i];
		}
		for (int i = 1; i <= degree; i++) {
		    knots[degree-i] = 2.0*knots[degree] - knots[degree + i];
		    knots[m-degree-1+i] =
			2.0*knots[m-degree-1] - knots[m-degree-1-i];
		}
	    }
	    break;
	case CLAMPED:
	    {
		int q = n-1-degree+1;
		double[] ts = new double[n-degree+1];
		double r = ((double)x.length)/q;
		for (int i = 1; i < q; i++) {
		    ts[i] = x[(int)Math.round(i*r)];
		}
		ts[0] = x[0];
		ts[q] = x[x.length-1];
		for (int i = 0; i <= degree; i++) {
		    knots[i] = ts[0];
		    knots[m-degree-1+i] = ts[q];
		}
		for (int i = 1; i < ts.length; i++) {
		    knots[i+degree] = ts[i];
		}
	    }
	    break;
	case CLAMPED_LEFT:
	    {
		int q = n-1-degree+1;
		double[] ts = new double[n-degree+1];
		double r = ((double)x.length)/q;
		for (int i = 1; i < q; i++) {
		    ts[i] = x[(int)Math.round(i*r)];
		}
		ts[0] = x[0];
		ts[q] = x[x.length-1];
		for (int i = 0; i <= degree; i++) {
		    knots[i] = ts[0];
		}
		knots[m-degree-1] = ts[q];
		for (int i = 1; i < ts.length; i++) {
		    knots[i+degree] = ts[i];
		}
		for (int i = 1; i <= degree; i++) {
		    knots[m-degree-1+i] =
			2.0*knots[m-degree-1] - knots[m-degree-1-i];
		}
	    }
	    break;
	case CLAMPED_RIGHT:
	    {
		int q = n-1-degree+1;
		double[] ts = new double[n-degree+1];
		double r = ((double)x.length)/q;
		for (int i = 1; i < q; i++) {
		    ts[i] = x[(int)Math.round(i*r)];
		}
		ts[0] = x[0];
		ts[q] = x[x.length-1];
		for (int i = 0; i <= degree; i++) {
		    knots[m-degree-1+i] = ts[q];
		}
		knots[degree] = ts[0];
		for (int i = 1; i < ts.length; i++) {
		    knots[i+degree] = ts[i];
		}
		for (int i = 1; i <= degree; i++) {
		    knots[degree-i] = 2.0*knots[degree] - knots[degree + i];
		}
	    }
	    break;
	case PERIODIC:
	    {
		knots[0] = x[0];
		knots[n] = x[x.length-1];
		double r = (double)(x.length)/n;
		for (int i = 1; i < n; i++) {
		    knots[i] = x[(int)Math.floor(i*r)];
		}
	    }
	    break;
	}
	return knots;
    }

    /**
     * Constructor specifying a B-spline's degree, number of control
     * points, mode, and a set of Y values for given X values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * @param degree the degree of the B-spline that will be created
     * @param n the number of control points
     * @param mode the mode for the B-spline (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSplineMode.UNCLAMPED).
     * @param x values in the domain of the spline
     * @param y values in the range of the spline

     */
    public BSpline(int degree, int n, Mode mode, double[] x, double[] y) {
	this(degree, createKnots(degree, n, mode, x),
	     mode != null && mode == Mode.PERIODIC,
	     x, y);
    }

    /**
     * Constructor specifying a B-spline's degree, number of control
     * points, mode, and a set of Y values for given X values, with
     * a specified error for the Y values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * @param degree the degree of the B-spline that will be created
     * @param n the number of control points
     * @param mode the mode for the B-spline (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSplineMode.UNCLAMPED).
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviation for the Y values
     */
    public BSpline(int degree, int n, Mode mode,
		   double[] x, double[] y, double sigma) {
	this(degree, createKnots(degree, n, mode, x),
	     mode != null && mode == Mode.PERIODIC,
	     x, y, sigma);
    }

    /**
     * Constructor specifying a B-spline's degree, number of control
     * points, mode, and a set of Y values and their standard
     * deviations for given X values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * @param degree the degree of the B-spline that will be created
     * @param n the number of control points
     * @param mode the mode for the B-spline (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSplineMode.UNCLAMPED)
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviations for the values provided by
     *        the argument y
     */
    public BSpline(int degree, int n, Mode mode,
		   double[] x, double[] y, double[] sigma) {
	this(degree, createKnots(degree, n, mode, x),
	     mode != null && mode == Mode.PERIODIC,
	     x, y, sigma);
    }


    private final double naiveN(int i, int p, double u) {
	// naive test implementation - just use the recursive
	// definition.
	if (p == 0) {
	    if (uarray[i] <= u && u < uarray[i+1]) {
		return 1.0;
	    } else {
		return 0.0;
	    }
	} else {
	    double tmp = 0.0;
	    int ipp = i + p;
	    int ippp1 = ipp + 1;
	    int pm1 = p - 1;
	    double uai = uarray[i];
	    double uaipp = uarray[ipp];
	    if (uaipp > uai) {
		tmp += naiveN(i,pm1, u) * (u - uai)/(uaipp - uai);
	    }
	    double uaippp1 = uarray[i+p+1];
	    double uaip1 = uarray[i+1];
	    if (uaippp1 > uaip1) {
		tmp += naiveN(i+1,pm1, u) * (uaippp1 - u) / (uaippp1-uaip1);
	    }
	    return tmp;
	}
    }

    static class BStack {
	int unused = 0;
	double[] stack;
	// int[] ind;
	BStack(int n) {
	    stack = new double[n];
	    // ind = new int[n];
	}
    }

    static LinkedList<BStack> stacks = new LinkedList<>();
    static final int MAX_DEGREE = 32;
    static {
	int n = Runtime.getRuntime().availableProcessors();
	for (int i = 0; i < n; i++) {
	    stacks.add(new BStack(MAX_DEGREE));
	}
    }

    /**
     * Default cutoff for using a straightforward recursive
     * implementation of the method M.
     * When the argument to N providing the degree of a basis function
     * is below this value, the straightforward implementation is
     * used. Otherwise intermediate results are cached and reused.
     */
    public static final int CUTOFF = 8;

    static int cutoff = CUTOFF;

    /**
     * Set the cutoff for the use of the naive basis-function computation.
     * When the argument to N providing the degree of a basis function
     * is below this value, the straightforward implementation is
     * used. Otherwise intermediate results are cached and reused.
     * @value the cutoff, which must not be negative
     */
    public static void setCutoff(int value) {
	if (value < 0)
	    throw new IllegalArgumentException(errorMsg("argNonNegative", value));
	cutoff = value;
    }

    
    static synchronized BStack findStack(int degree) {
	if (degree <= MAX_DEGREE) {
	    degree = MAX_DEGREE;
	}
	BStack result = stacks.poll();
	if (result == null) {
	    return new BStack(degree);
	} else if (result.stack.length < degree) {
	    result = new BStack(degree);
	}
	return result;
    }

    static synchronized void releaseStack(BStack stack) {
	if (stack != null) {
	    stacks.add(stack);
	}
    }

    /**
     * Compute the value of a B-Spline's basis function.
     * @param i a integer representing a knot span with a value
     *        equal to the index of the knot at the start of a span
     * @param p the degree of the basis function
     * @param u the value of the B-spline's parameter
     * @return the value of the basis function at point u
     */
    public final double N(int i, int p, double u) {
	if (periodic) u = getStandardU(u);
	int pp1 = p+1;
	if (u < uarray[i] || u >= uarray[i+pp1]) return 0.0;
	if (p < cutoff) {
	    return naiveN(i, p, u);
	}
	BStack stack = null;
	try {
	    stack = findStack(pp1);
	    stack.unused = 0;
	    return N(i, p, u, stack);
	} finally {
	    releaseStack(stack);
	}
    }

    // The implementation adds a stack to remember previously
    // computed values.
    private double N(int i, int p, double u, BStack stack) {
	double result;
	if (p == 0) {
	    if (uarray[i] <= u && u < uarray[i+1]) {
		result = 1.0;
	    } else {
		result = 0.0;
	    }
	} else {
	    result = 0.0;
	    int pm1 = p-1;
	    int ipp = i + p;
	    int ippp1 = ipp + 1;
	    double uaipp = uarray[ipp];
	    if (uaipp > uarray[i]) {
		double uai = uarray[i];
		if (pm1 < stack.unused) {
		    result += stack.stack[pm1] * (u - uai)/(uaipp - uai);
		} else {
		    result += N(i,pm1,u,stack) * (u - uai)/(uaipp - uai);
		}
	    }
	    double uaippp1 = uarray[ippp1];
	    double uaip1 = uarray[i+1];
	    if (uaippp1 > uaip1) {
		result += N(i+1,pm1,u,stack) * (uaippp1-u) / (uaippp1-uaip1);
	    }
	}
	stack.stack[p] = result;
	// stack.ind[p] = i;
	if (stack.unused == p) stack.unused++;
	return result;
    }

    @Override
    public double getDomainMin() {
	if (periodic) {
	    return -Double.MAX_VALUE;
	} else {
	    return start;
	}
    }
    @Override
    public double getDomainMax() {
	if (periodic) {
	    return Double.MAX_VALUE;
	} else {
	    return end;
	}
    }

    @Override
    public double valueAt(double u) throws IllegalArgumentException {
	if (periodic) u = getStandardU(u);
	if (u < getDomainMin() || u > getDomainMax()) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}

	int p = degree;
	int pp1 = p+1;

	int index = Arrays.binarySearch(uarray, u);
	if (index < 0) {
	    index = - (index + 2);
	}
	if (index == uarray.length) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}
	int s = 0;
	if (u == uarray[index]) {
	    int ind = index;
	    while (ind > 0 && uarray[ind] == u) {
		s++;
		ind--;
	    }
	    ind = index;
	    s--;
	    while(ind < uarray.length && uarray[ind] == u) {
		s++;
		ind++;
	    }
	    ind--;
	    index = ind;
	    if (index == uarray.length-1) {
		while (s > p) {
		    s--;
		    index--;
		}
	    } else if (s > p) {
		s = p;
	    }
	}
	int h = p - s;
	int hp1 = h+1;
	double[] d1 = new double[hp1];
	double[] d2 = new double[hp1];
	for (int i = 0; i < hp1; i++) {
	    int j = index - p + i;
	    if (j < 0) continue;
	    d1[i] = carray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r; j < hp1; j++) {
		int i = index-p+j;
		double alpha = (u - uarray[i])
		    / (uarray[i+p-r+1] - uarray[i]);
		d2[j] = (1.0 - alpha)*d1[j-1] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	return d1[h];
    }

    @Override
    public double derivAt(double u) throws IllegalArgumentException
    {
	if (needQarray) computeQarray();
	return computeDeriv(u, degree-1, 1, qarray);
    }

    @Override
    public double secondDerivAt(double u) {
	if (needQQarray) computeQQarray();
	return computeDeriv(u, degree-2, 2, qqarray);
    }

    private double computeDeriv(double u, int p, int offset, double[] qarray)
	throws IllegalArgumentException
    {
	if (periodic) u = getStandardU(u);
	if (u < getDomainMin() || u > getDomainMax()) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}

	if (qarray == null) return 0.0;

	// int p = degree-1;
	// int offset = 1;
	int pp1 = p+1;

	int index = Arrays.binarySearch(uarray, u);
	if (index < 0) {
	    index = - (index + 2);
	}
	if (index >= (uarray.length - offset)) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}
	int s = 0;
	if (u == uarray[index]) {
	    int ind = index;
	    while (ind > offset && uarray[ind] == u) {
		s++;
		ind--;
	    }
	    ind = index;
	    s--;
	    while(ind < uarray.length-offset && uarray[ind] == u) {
		s++;
		ind++;
	    }
	    ind--;
	    index = ind;
	    if (index == uarray.length-1) {
		while (s > p) {
		    s--;
		    index--;
		}
	    } else if (s > p) {
		s = p;
	    }
	}

	int h = p - s;
	int hp1 = h+1;
	double[] d1 = new double[hp1];
	double[] d2 = new double[hp1];
	for (int i = 0; i < hp1; i++) {
	    int j = index - offset - p + i;
	    if (j < 0) continue;
	    d1[i] = qarray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r; j < hp1; j++) {
		int i = index-p+j;
		double alpha = (u - uarray[i])
		    / (uarray[i+p-r+1] - uarray[i]);
		d2[j] = (1.0 - alpha)*d1[j-1] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	return d1[h];
    }
}

//  LocalWords:  exbundle ul li le hellip mdash lt frasl blockquote
//  LocalWords:  infin BSpline boolean nullArg cptsWrongLength nbsp
//  LocalWords:  argArrayTooShort getPeriodEnd getPeriodStart cpoints
//  LocalWords:  spline's argNonNegative incompatibleArrayLengths
//  LocalWords:  knotSpanEmpty subclasses LeastSquaresFit BSplineMode
//  LocalWords:  degreeTooLarge UNCLAMPED argOutOfRangeD
