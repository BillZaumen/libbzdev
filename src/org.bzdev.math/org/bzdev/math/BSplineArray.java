package org.bzdev.math;
import java.util.LinkedList;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class implementing vector-valued B-splines.
 * This class supports both periodic and non-periodic B-splines, and
 * is very similar to the {@link BSpline} class: the difference is that
 * the values it computes are arrays of double-precision numbers instead
 * of single numbers.
 * Periodic B-splines can be used to approximate period functions.
 * Additional control points and knots are added for periodic B-splines.
 * (see below).
 * <P> 
 * Constructors differ from those in the {@link BSpline} class in two
 * ways: there is an initial argument giving a dimension (the length
 * for the arrays that represent the spline's values), and in some
 * cases, the corresponding argument arrays are larger, scaled up by
 * the dimension, and stored so that the array components representing
 * a given point are adjacent.
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
 * {@link #BSplineArray(int,int,double[],double[],boolean)} describes how
 * periodic and non-periodic B-splines are created.
 */
public class BSplineArray extends VectorValuedFunction {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    int dim;

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

    public int getDimension() {
	return dim;
    }

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
		qarray = (degree > 0)? new double[carray.length-dim]: null;
		if (qarray != null) {
		    for (int i = 0; i < qarray.length; i++) {
			int ii = i/dim;
			int iip1 = ii + 1;
			int ipdim = i + dim;
			double alpha = degree
			    / (uarray[iip1+degree] - uarray[iip1]);
			qarray[i] = alpha * (carray[ipdim] - carray[i]);
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
		qqarray = (degree > 1)? new double[qarray.length-dim]: null;
		if (qqarray != null) {
		    for (int i = 0; i < qqarray.length; i++) {
			int ii = i/dim;
			int iip1 = ii + 1;
			int iip2 = ii + 2;
			int ip1dim = i + dim;
			int ip2dim = i + 2*dim;
			double alpha = (degree - 1)
			    / (uarray[iip1+degree] - uarray[iip2]);
			qqarray[i] = alpha * (qarray[ip1dim] - qarray[i]);
		    }
		}
		needQQarray = false;
	    }
	}
    }


    /**
     * Constructor for open B-splines.
     * A full description is provided by the constructor
     * {@link #BSplineArray(int,int,double[],double[],boolean)}.
     * The degree of the B-spline is equal to
     * knots.length - cpoints.length - 1.
     * @param dim the length of the array of values
     * @param knots the knots for the B-spline to be constructed
     * @param cpoints the control points for the B-spline to be constructed
     * @see #BSplineArray(int,int,double[],double[],boolean)
     */
    public BSplineArray(int dim, double[] knots, double[] cpoints)
	throws IllegalArgumentException
    {
	this(dim, knots.length - (cpoints.length/dim) - 1,
	     knots, cpoints, false);
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
     * the knots array, or (p-1) larger than the length of the knots
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
     * @param dim the dimenions for this vector-valued function
     * @param degree a non-negative integer giving the  degree of the
     *        B-spline to be constructed
     * @param knots the knots for the B-spline to be constructed
     * @param cpoints the control points for the B-spline to be constructed
     * @param periodic true if the spline is periodic; false if it is open.
     */
    public BSplineArray(int dim, int degree, double[] knots, double[] cpoints,
		   boolean periodic)
	throws IllegalArgumentException
    {
	super(dim);
	if (cpoints.length % dim != 0) {
	    throw new IllegalArgumentException
		(errorMsg("cptsNotMultipleOfDim", cpoints.length, dim));
	}
	/*
	  m = knots.length-1;
	  p = degree;
	  n = cpoints.length-1;
	  n + p + 1 = m <==> cpoints.length + degree + 1 = knots.length
	*/
	if (degree < 0) throw new IllegalArgumentException
			    (errorMsg("argNonNegative2", degree));
	if (periodic){
	    boolean padmode;
	    if (cpoints.length/dim + 1 == knots.length) {
		padmode = true;
	    } else if (cpoints.length/dim + 1 == knots.length + degree) {
		padmode = false;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("incompatibleArrayLengths"));
	    }
	    uarray = new double[knots.length + 2*degree];
	    carray = new double[cpoints.length +  dim*(padmode? degree: 0)];
	    System.arraycopy(knots, 0, uarray, degree, knots.length);
	    if (padmode) {
		System.arraycopy(cpoints, 0, carray, dim*degree,
				 cpoints.length);
	    } else {
		System.arraycopy(cpoints, 0, carray, 0, cpoints.length);
	    }
	    int offset = knots.length + degree ;
	    for (int i = 0; i < degree; i++) {
		uarray[i] = knots[0] + knots[knots.length - 1 - degree + i]
		    - knots[knots.length-1];
		uarray[offset+i] = knots[knots.length-1] +
		    knots[i+1] - knots[0];
	    }
	    if (padmode) {
		int dimdegree = dim*degree;
		for (int i = 0; i < dimdegree; i++) {
		    carray[i] = cpoints[cpoints.length-dimdegree+i];
		}
	    }
	    start = knots[0];
	    end = knots[knots.length-1];
	    if (!(start < end)) {
		throw new IllegalArgumentException
		    (errorMsg("knotSpanEmpty", start, end));
	    }
	    period = end - start;
	} else {
	    if(cpoints.length/dim + degree + 1 != knots.length) {
		throw new IllegalArgumentException
		    (errorMsg("incompatibleArrayLengths"));
	    }
	    uarray = (double[]) knots.clone();
	    carray = (double[]) cpoints.clone();
	    start = uarray[degree];
	    end = uarray[uarray.length - 1 - degree];
	}
	this.dim = dim;
	this.degree = degree;
	this.periodic = periodic;
    }

    /**
     * Constructor specifying a B-spline's degree and knots, and fitting
     * the spline to a set of data points.
     * The use of knots is the same as that for the constructor
     * {@link #BSplineArray(int,int,double[],double[],boolean)}.
     * @param dim the length of the array of values
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     */
    public BSplineArray(int dim, int degree, double[] knots, boolean periodic,
			double[] x, double[] y) {
	this(dim, degree, knots, 
	     new double[periodic?(knots.length-1 + degree)*dim:
			(knots.length - degree - 1)*dim],
	     periodic);
	if (periodic) {
	    double[] yy = new double[y.length*3-2*dim];
	    double[] xx = new double[x.length*3-2];
	    for (int i = 0; i < x.length-1; i++) {
		xx[i] = x[i]-period;
	    }
	    System.arraycopy(x, 0, xx, x.length-1, x.length);
	    for (int i = 0; i < x.length-1; i++) {
		xx[i + x.length-1 + x.length] = x[i+1] + period;
	    }
	    System.arraycopy(y, 0, yy, 0, y.length-dim);
	    System.arraycopy(y, 0, yy, y.length-dim, y.length);
	    System.arraycopy(y, dim, yy, y.length + y.length-dim, y.length-dim);
	    y = yy;
	    x = xx;
	}
	// Now do a least-squares fit of the data.
	// Each component of the arrays are fitted seperately.
	// This decreases the size of the matrices that are 
	// inverted.

	double[] tmpy = new double[y.length/dim];
	double[] tmpc = new double[carray.length/dim];
	RealValuedFunction[] functions =
	    new RealValuedFunction[carray.length/dim];
	for (int k = 0; k < dim; k++) {
	    for (int i = 0; i < tmpy.length; i++) {
		tmpy[i] = y[i*dim + k];
	    }
	    for (int ind = 0; ind < functions.length; ind++) {
		final int i = ind;
		functions[i] = new RealValuedFunction() {
			public double valueAt(double u) {
			    return N(i, BSplineArray.this.degree, u);
			}
		    };
	    }
	    LeastSquaresFit.FunctionBasis lsf =
		new LeastSquaresFit.FunctionBasis(x, tmpy, functions);
	    lsf.getParameters(tmpc);
	    for (int i = 0; i < tmpc.length; i++) {
		carray[i*dim+k] = tmpc[i];
	    }
	}
    }

    /**
     * Constructor specifying a B-spline's degree and knots, and fitting
     * the spline to a set of data points with errors.
     * The use of knots is the same as that for the constructor
     * {@link #BSplineArray(int,int,double[],double[],boolean)}.
     * @param dim the length of the array of values
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviations for the values provided by
     *        the argument y
     */
    public BSplineArray(int dim, int degree, double[] knots, boolean periodic,
			double[] x, double[] y, double[] sigma) {
	this(dim, degree, knots, 
	     new double[periodic?(knots.length-1 + degree)*dim:
			(knots.length - degree - 1)*dim],
	     periodic);
	if (periodic) {
	    double[] yy = new double[y.length*3 - 2*dim];
	    double[] ssigma = new double[sigma.length*3 - 2*dim];
	    double[] xx = new double[x.length*3 - 2];
	    for (int i = 0; i < x.length-1; i++) {
		xx[i] = x[i]-period;
	    }
	    System.arraycopy(x, 0, xx, x.length-1, x.length);
	    for (int i = 0; i < x.length-1; i++) {
		xx[i + x.length-1 + x.length] = x[i+1] + period;
	    }
	    System.arraycopy(sigma, 0, ssigma, 0, sigma.length-dim);
	    System.arraycopy(sigma, 0, ssigma, sigma.length-dim, sigma.length);
	    System.arraycopy(sigma, dim, ssigma, sigma.length+sigma.length-dim,
			     sigma.length-dim);
	    System.arraycopy(y, 0, yy, 0, y.length-dim);
	    System.arraycopy(y, 0, yy, y.length-dim, y.length);
	    System.arraycopy(y, dim, yy, y.length + y.length - dim,
			     y.length-dim);
	    y = yy;
	    sigma = ssigma;
	    x = xx;
	}
	// Now do a least-squares fit of the data.
	// Each component of the arrays are fitted seperately.
	// This decreases the size of the matrices that are 
	// inverted.

	double[] tmpy = new double[y.length/dim];
	double[] tmps = new double[sigma.length/dim];
	double[] tmpc = new double[carray.length/dim];
	RealValuedFunction[] functions =
	    new RealValuedFunction[carray.length/dim];
	for (int k = 0; k < dim; k++) {
	    for (int i = 0; i < tmpy.length; i++) {
		tmpy[i] = y[i*dim + k];
		tmps[i] = sigma[i*dim + k];
	    }
	    for (int ind = 0; ind < functions.length; ind++) {
		final int i = ind;
		functions[i] = new RealValuedFunction() {
			public double valueAt(double u) {
			    return N(i, BSplineArray.this.degree, u);
			}
		    };
	    }
	    LeastSquaresFit.FunctionBasis lsf =
		new LeastSquaresFit.FunctionBasis(x, tmpy, tmps, functions);
	    lsf.getParameters(tmpc);
	    for (int i = 0; i < tmpc.length; i++) {
		carray[i*dim+k] = tmpc[i];
	    }
	}
    }

    /**
     * Constructor specifying a B-spline's degree, number of control
     * points, mode, and a set of Y values for given X values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * @param dim the dimenions for this vector-valued function
     * @param degree the degree of the B-spline that will be created
     * @param n the number of control points
     * @param mode the mode for the B-spline (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSplineMode.UNCLAMPED)
     * @param x values in the domain of the spline
     * @param y values in the range of the spline

     */
    public BSplineArray(int dim, int degree, int n, BSpline.Mode mode,
			double[] x, double[] y)
    {
	this(dim, degree, BSpline.createKnots(degree, n, mode, x),
	     mode != null && mode == BSpline.Mode.PERIODIC, x, y);
    }

    /**
     * Constructor specifying a B-spline's degree, number of control
     * points, mode, and a set of Y values and their standard
     * deviations for given X values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * @param dim the dimenions for this vector-valued function
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
    public BSplineArray(int dim, int degree, int n, BSpline.Mode mode,
			double[] x, double[] y, double[] sigma) {
	this(dim, degree, BSpline.createKnots(degree, n, mode, x),
	     mode != null && mode==BSpline.Mode.PERIODIC, x, y, sigma);
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
	if (p < BSpline.cutoff) {
	    return naiveN(i, p, u);
	}
	BSpline.BStack stack = null;
	try {
	    stack = BSpline.findStack(pp1);
	    stack.unused = 0;
	    return N(i, p, u, stack);
	} finally {
	    BSpline.releaseStack(stack);
	}
    }

    // The implmenetation adds a stack to remember previously
    // computed values.
    private double N(int i, int p, double u, BSpline.BStack stack) {
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
    public boolean domainMinClosed() {
	return true;
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
    public boolean domainMaxClosed() {
	return true;
    }


    @Override
	public void valueAt(double[] array, int off, double u)
	throws IllegalArgumentException
    {
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
	int hp1dim = hp1*dim;
	double[] d1 = new double[hp1*dim];
	double[] d2 = new double[hp1*dim];
	for (int i = 0; i < hp1dim; i++) {
	    int j = index*dim - p*dim + i;
	    if (j < 0) continue;
	    d1[i] = carray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r*dim; j < hp1dim; j++) {
		int ii = index - p + j/dim;
		int i = index*dim-p*dim+j;
		double alpha = (u - uarray[ii])
		    / (uarray[ii+p-r+1] - uarray[ii]);
		d2[j] = (1.0 - alpha)*d1[j-dim] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	// return d1[h];
	System.arraycopy(d1, h*dim, array, off, dim);
    }

    @Override
    public void derivAt(double[] array, int off, double u)
	throws IllegalArgumentException
    {
	if (needQarray) computeQarray();
	computeDeriv(u, degree-1, 1, qarray, array, off);
    }

    @Override
    public void secondDerivAt(double[] array, int off, double u) {
	if (needQQarray) computeQQarray();
	computeDeriv(u, degree-2, 2, qqarray, array, off);

    }

    private void computeDeriv(double u, int p, int offset, double[] qarray,
			      double[] array, int off)
	throws IllegalArgumentException
    {
	if (periodic) u = getStandardU(u);
	if (u < getDomainMin() || u > getDomainMax()) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}

	if (qarray == null) {
	    Arrays.fill(array, off, dim, 0.0);
	    return;
	}

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
	int hp1dim = hp1*dim;
	double[] d1 = new double[hp1dim];
	double[] d2 = new double[hp1dim];
	for (int i = 0; i < hp1dim; i++) {
	    int j = index*dim - offset*dim - p*dim + i;
	    if (j < 0) continue;
	    d1[i] = qarray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r*dim; j < hp1dim; j++) {
		int ii = index - p + j/dim;
		int i = index*dim-p*dim+j;
		double alpha = (u - uarray[ii])
		    / (uarray[ii+p-r+1] - uarray[ii]);
		d2[j] = (1.0 - alpha)*d1[j-dim] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	// return d1[h];
	System.arraycopy(d1, h*dim, array, off, dim);
    }
}

//  LocalWords:  exbundle BSpline spline's ul li le hellip mdash lt
//  LocalWords:  frasl blockquote infin BSplineArray boolean cpoints
//  LocalWords:  getPeriodEnd getPeriodStart nbsp argNonNegative
//  LocalWords:  cptsNotMultipleOfDim incompatibleArrayLengths
//  LocalWords:  knotSpanEmpty seperately BSplineMode UNCLAMPED
//  LocalWords:  implmenetation argOutOfRangeD
