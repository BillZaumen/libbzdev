package org.bzdev.math;
import java.util.LinkedList;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class implementing vector-valued NURBS (Non Uniform Rational B-Splines).
 * This class supports both periodic and non-periodic NURBS, and is
 * very similar to the {@link BSplineArray} class.  For purposes of
 * this class, a periodic NURBS array adds additional knots and
 * control points - copies of the ones explicitly specified, so that
 * the spline will be smooth at its end points. The parameter u will
 * be shifted if necessary, always by a multiple of its period, so
 * that the spline's value will be computed using values of the
 * parameter within a specific range.  Details regarding the
 * additional control points, weights, and knots for the periodic case
 * are described below.
 * <P>
 * A NURBS array consists of
 * <ul>
 *   <li> an increasing finite sequence of real numbers called "knots":
 *        u<sub>0</sub> &le; u<sub>1</sub> &le; ... &le; u<sub>m</sub>
 *   <li> a set of basis functions.
 *   <li> a set of control points (just numbers for this class) denoted as
 *        P<sub>0</sub>, P<sub>1</sub>, &hellip; P<sub>n</sub>.
 *   <li> a degree&mdash;normally denoted as p.  The degree of a NURBS
 *        must satisfy p = m - n - 1.
 *   <li> a set of weights denoted as w<sub>0</sub>, w<sub>1</sub>
 *        &hellip; w<sub>n</sub>.
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
 *    (&sum;<sup>n</sup><sub>i=0</sub> N<sub>i,p</sub>(u)
 *    w<sub>i</sub>P<sub>i</sub>) / (&sum;<sup>n</sup><sub>i=0</sub>
 *    N<sub>i,p</sub>(u)w<sub>i</sub>)
 * </blockquote>
 * with a domain [u<sub>p</sub>, u<sub>m-p</sub>] for non-periodic splines
 * and (-&infin;, &infin;) for periodic splines (for a periodic spline,
 * the value of u will be shifted by an appropriate multiple of the period
 * so as to be in the interval [u<sub>p</sub>, u<sub>m-p</sub>], at at
 * the end points of this interval, the values will match, as will their
 * derivatives.
 * <P>
 * The documentation for the constructor
 * {@link #NurbsArray(int,int,double[],double[],boolean)} describes how
 * periodic and non-periodic NURBSs are created.
 */
public class NurbsArray extends VectorValuedFunction {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    int dim;
    private int hdim;		// dimension for homogeneous coordinates

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

    /**
     * Get the number of dimensions for the NURBS.
     * This is also the number of dimensions for the control points,
     * excluding their weights.
     * @return the number of dimensions
     */
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
     * A periodic NURBS explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a non-periodic NURBS, the domain minimum is returned.
     * @return the starting value for a period if the spline is periodic;
     *         otherwise the domain minimum
     */
    public double getPeriodStart() {
	return start;
    }

    /**
     * Get the ending value for a period.
     * A periodic NURBS explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a non-periodic NURBS, the domain maximum is returned.
     * @return the ending value for a period if the spline is periodic;
     *         otherwise the domain maximum
     */
    public double getPeriodEnd() {
	return end;
    }

    /**
     * Get the period.
     * A periodic NURBS explicitly handles a range of values of
     * the parameters. For other values of the parameter, the parameter
     * is shifted by the period until it falls within this range.
     * For a periodic NURBS, the period is numerically equal to
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
     * Get the degree if this NURBS.
     * The smallest degree is zero (some authors use 1, in which
     * case the degree will be larger by 1 than the value
     * returned by this method).
     * @return this NURBS' degree.
     */
    public int getDegree() {return degree;}

    private void computeQarray() {
	synchronized (this) {
	    if (needQarray) {
		qarray = (degree > 0)? new double[carray.length-hdim]: null;
		if (qarray != null) {
		    for (int i = 0; i < qarray.length; i++) {
			int ii = i/hdim;
			int iip1 = ii + 1;
			int ipdim = i + hdim;
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
		qqarray = (degree > 1)? new double[qarray.length-hdim]: null;
		if (qqarray != null) {
		    for (int i = 0; i < qqarray.length; i++) {
			int ii = i/hdim;
			int iip1 = ii + 1;
			int iip2 = ii + 2;
			int ip1dim = i + hdim;
			int ip2dim = i + 2*hdim;
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
     * Constructor for open NURBSs.
     * A full description is provided by the constructor
     * {@link #NurbsArray(int,int,double[],double[],boolean)}.
     * The degree of the NURBS is equal to
     * knots.length - cpoints.length - 1.
     * @param dim the length of the array of values
     * @param knots the knots for the NURBS to be constructed
     * @param cpoints the control points and weights for the NURBS to 
     *        be constructed
     * @see #NurbsArray(int,int,double[],double[],boolean)
     */
    public NurbsArray(int dim, double[] knots, double[] cpoints)
	throws IllegalArgumentException
    {
	this(dim, knots.length - (cpoints.length/(dim+1)) - 1,
	     knots, cpoints, false);
    }

    /**
     * Constructor.
     * There are two cases depending on whether the NURBS will be
     * periodic or non-periodic.  For the non-periodic case,
     * if m is the length of the knots array, n is the number of control
     * points, and p is the degree if the NURBS, then the following
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

     * where each control point and weight consists of 1 + &lt;the
     * number of dimension&gt; double-precision numbers. For each control
     * point and weight, the array contains the coordinates of the
     * control point (listed in order), followed by the weight, placed
     * contiguously in the array.
     * <P>
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
     * array, in both cases multiplied by 1 + &lt;the number of dimensions&gt;.
     * The constructor will augment these arrays as follows:
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
     *        NURBS to be constructed
     * @param knots the knots for the NURBS to be constructed
     * @param cpoints the control points and weights for the NURBS to be
     *        constructed
     * @param periodic true if the spline is periodic; false if it is open.
     */
    public NurbsArray(int dim, int degree, double[] knots, double[] cpoints,
		   boolean periodic)
	throws IllegalArgumentException
    {
	super(dim);
	hdim = dim+1;
	if (cpoints.length % hdim != 0) {
	    throw new IllegalArgumentException
		(errorMsg("cptsNotMultipleOfDim", cpoints.length, hdim));
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
	    if (cpoints.length/hdim + 1 == knots.length) {
		padmode = true;
	    } else if (cpoints.length/hdim + 1 == knots.length + degree) {
		padmode = false;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("incompatibleArrayLengths"));
	    }
	    uarray = new double[knots.length + 2*degree];
	    carray = new double[cpoints.length +  hdim*(padmode? degree: 0)];
	    System.arraycopy(knots, 0, uarray, degree, knots.length);
	    if (padmode) {
		System.arraycopy(cpoints, 0, carray,
				 hdim*degree, cpoints.length);
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
		int dimdegree = hdim*degree;
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
	    if(cpoints.length/hdim + degree + 1 != knots.length) {
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
	int reducedLen = carray.length/hdim;
	for (int i = 0; i < reducedLen; i++) {
	    int base = i * hdim;
	    for (int j = 0; j < dim; j++) {
		carray[base+j] *= carray[base + dim];
	    }
	}
    }

    /*
    private static LeastSquaresFit.NonLinear.Config defaultConfig =
	new LeastSquaresFit.NonLinear.Config();

    /*
     * Constructor specifying a NURBS's degree, knots, and fitting
     * the spline to a set of data points.
     * The use of knots is the same as that for the constructor
     * {@link #NurbsArray(int,int,double[],double[],boolean)}.
     * @param dim the length of the array of values
     * @param degree the degree of the NURBS that will be created
     * @param knots the knots the NURBS uses
     * @param periodic true if the NURBS is periodic, false otherwise
     * @param config configuration parameters for the Levenberg-Marquardt
     *        algorithm; null for the default
     * @param x values in the domain of the spline
     * @param y values in the range of the spline

    public NurbsArray(int dim, int degree, double[] knots, boolean periodic,
		      LeastSquaresFit.NonLinear.Config config,
		      double[] x, double[] y) {
	this(dim, degree, knots,
	     new double[periodic?(knots.length-1 + degree)*(dim+1):
			(knots.length - degree - 1)*(dim+1)],
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
	    System.arraycopy(y, dim, yy, y.length + y.length-dim,
			     y.length-dim);
	    y = yy;
	    x = xx;
	}

	double[] newx = new double[dim*x.length];
	int nxi = 0;
	for (int i = 0; i < x.length; i++) {
	    for (int j = 0; j < dim; j++) {
		newx[nxi++] = x[i];
	    }
	}
	x = newx;

	if (config == null) config = defaultConfig;

	int len = carray.length-1;
	RealValuedFunctionVA funct = new RealValuedFunctionVA(len+3, len+3) {
		double g = 0.0;
		private void setup(double... args) {
		    double u = args[2];
		    int index = 0;
		    g = N(index, NurbsArray.this.degree, u);
		    for (int i = 2+NurbsArray.this.hdim;
			 i < args.length;
			 i += NurbsArray.this.hdim) {
			index++;
			g += args[i]*N(index, NurbsArray.this.degree, u);
		    }
		}
		private double valueAtAux(double... args) {
		    double y = args[0];
		    int ind = (int) Math.round(args[1]) % NurbsArray.this.dim;
		    double u = args[2];
		    double sum = 0.0;
		    int index = 0;
		    for (int i = 3 + ind;
			 i < args.length;
			 i += NurbsArray.this.hdim) {
			sum += N(index, NurbsArray.this.degree, u)
			    * args[i];
			index++;
		    }
		    return sum;
		}

		@Override
		public double valueAt(double... args) {
		    if (false) {
			System.out.print(args[3]);
			for (int i = 4; i < args.length; i++) {
			    System.out.print(", " + args[i]);
			}
			System.out.println();
		    }
		    setup(args);
		    double y = args[0];
		    if (false) {
			System.out.println("valueAt(" + y + ", "
					   + Math.round(args[1]) + ", "
					   + args[2] + ", ...) = "
					   + (y - valueAtAux(args)/g));
		    }
		    return (y - valueAtAux(args)/g);
		}

		@Override
		public double[] jacobian(int offset, double... args) {
		    // System.out.print("(Jacobian) ");
		    setup(args);
		    double[] results =  super.jacobian(offset, args);
		    if (false) {
			System.out.print(results[0]);
			for (int i = 1; i <results.length; i++) {
			    System.out.print(", " + results[i]);
			}
			System.out.println();
		    }
		    return results;
		}

		// We only differentiate with respect to control-points
		// and weights, and we only call derivAt when the jacobian
		// is computed.
		@Override
		public double derivAt(int index, double... args) {
		    double u = args[2];
		    boolean wmode =
			(Math.round(index - 2) % NurbsArray.this.hdim) == 0;
		    int i = (index - 2) / NurbsArray.this.hdim;
		    double results;
		    if (wmode) {
			double v = valueAtAux(args);
			results =  (v/(g*g))*N(i, NurbsArray.this.degree, u);
		    } else {
			int ia = (int)(args[1] % NurbsArray.this.dim) + 1;
			int ib = (int)((index - 2) % NurbsArray.this.hdim);
			results =  (ia == ib)?
			    -N(i, NurbsArray.this.degree, u)/g: 0.0;
		    }
		    if (false) {
			double sv = args[index];
			double delta = 0.00001;
			double vv1 = valueAt(args);
			args[index] = args[index] + delta;
			double vv2 = valueAt(args);
			double deriv = (vv2 - vv1)/delta;
			args[index] = sv;
			double diff = deriv-results;
			System.out.println("index = " + index
					   + ", arg[index] = " + sv
					   + ", i = " + i
					   + ", args[1] = " + args[1]);
			if (Math.abs(deriv-results) > 1.e-5) {
			    throw new RuntimeException("deriv failed, diff = "
						       + diff);
			}
		    }
		    return results;
		}
	    };

	double [] guess = new double[carray.length-1];
	for (int i = dim; i < guess.length; i += hdim) {
	    guess[i] = 1.0;
	}
	double[] indices = new double[y.length];
	for (int i = 0; i < y.length; i++) {
	    indices[i] = (double) i;
	}

	if (false) {
	    for (int i = 0; i < x.length; i++) {
		System.out.format("x[%d] = %s\n", i, x[i]);
	    }
	}

	LMA.findMin(funct, guess,
		    config.getLambda(), config.getNu(),
		    config.getLimit(), config.getIterationLimit(),
		    y, indices, x);

	for (int i = 0; i < guess.length && i < dim; i++) {
	    carray[i] = guess[i];
	}
	if (guess.length > dim) {
	    carray[dim] = 1.0;
	}
	for (int i = hdim; i < guess.length; i++) {
	    if (i % hdim == 0) {
		carray[i+dim] = guess[i];
	    } else {
		carray[i-1] = guess[i];
	    }
	}
    }

    /*
     * Constructor specifying a NURBS' degree, number of control
     * points, mode, and a set of Y values for given X values.
     * The number of X,Y data points must be larger than the number
     * of control points.
     * <P>
     * The <code>mode</code> argument specifies the mode that would be
     * used to create a BSplineArray in homogeneous coordinates (the
     * conversion to conventional coordinates turns this higher-dimensional
     * turns a B-Spline into a NURBS).
     * @param degree the degree of the NURBS that will be created
     * @param n the number of control points
     * @param mode the mode for the NURBS (either
     *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
     *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
     *        or BSpline.Mode.PERIODIC); null for the default
     *        (BSplineMode.UNCLAMPED)
     * @param config configuration parameters for the Levenberg-Marquardt
     *        algorithm; null for the default
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @see LeastSquaresFit.NonLinear.Config
     * @see BSpline.Mode

    public NurbsArray(int dim, int degree, int n, BSpline.Mode mode,
		      LeastSquaresFit.NonLinear.Config config,
			double[] x, double[] y)
    {
	this(dim, degree, BSpline.createKnots(degree, n, mode, x),
	     mode != null && mode == BSpline.Mode.PERIODIC, config, x, y);
    }


    /*
     * Constructor specifying a NURBS' degree and knots, and fitting
     * the spline to a set of data points with errors.
     * The use of knots is the same as that for the constructor
     * {@link #NurbsArray(int,int,double[],double[],boolean)}.
     * @param dim the length of the array of values
     * @param degree the degree of the B-spline that will be created
     * @param knots the knots the B-spline uses
     * @param periodic true if the B-spline is periodic, false otherwise
     * @param config configuration parameters for the Levenberg-Marquardt
     *        algorithm; null for the default
     * @param x values in the domain of the spline
     * @param y values in the range of the spline
     * @param sigma the standard deviations for the values provided by
     *        the argument y

    public NurbsArray(int dim, int degree, double[] knots, boolean periodic,
		      LeastSquaresFit.NonLinear.Config config,
		      double[] x, double[] y, double[] sigma) {
	this(dim, degree, knots,
	     new double[periodic?(knots.length-1 + degree)*(dim+1):
			(knots.length - degree - 1)*(dim+1)],
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

	double[] newx = new double[dim*x.length];
	int nxi = 0;
	for (int i = 0; i < x.length; i++) {
	    for (int j = 0; j < dim; j++) {
		newx[nxi++] = x[i];
	    }
	}
	x = newx;

	if (config == null) config = defaultConfig;

	int len = carray.length-1;
	RealValuedFunctionVA funct = new RealValuedFunctionVA(len+4, len+4) {
		double g = 0.0;
		private void setup(double... args) {
		    double u = args[3];
		    int index = 0;
		    g = N(index, NurbsArray.this.degree, u);
		    for (int i = 3+NurbsArray.this.hdim;
			 i < args.length;
			 i += NurbsArray.this.hdim) {
			index++;
			g += args[i]*N(index, NurbsArray.this.degree, u);
		    }
		}
		private double valueAtAux(double... args) {
		    double y = args[0];
		    int ind = (int) Math.round(args[2]) % NurbsArray.this.dim;
		    double u = args[3];
		    double sum = 0.0;
		    int index = 0;
		    for (int i = 4 + ind;
			 i < args.length;
			 i += NurbsArray.this.hdim) {
			sum += N(index, NurbsArray.this.degree, u)
			    * args[i];
			index++;
		    }
		    return sum;
		}

		@Override
		public double valueAt(double... args) {
		    setup(args);
		    double y = args[0];
		    double sigma = args[1];
		    return (y - valueAtAux(args)/g)/sigma;
		}

		@Override
		public double[] jacobian(int offset, double... args) {
		    setup(args);
		    return super.jacobian(offset, args);
		}

		// We only differentiate with respect to control-points
		// and weights, and we only call derivAt when the jacobian
		// is computed.
		@Override
		public double derivAt(int index, double... args) {
		    double sigma = args[1];
		    double u = args[3];
		    boolean wmode = ((index - 3) % NurbsArray.this.hdim) == 0;
		    int i = (index - 3) / NurbsArray.this.hdim;
		    double results;
		    if (wmode) {
			double v = valueAtAux(args);
			results = ((v/(g*g))*N(i, NurbsArray.this.degree, u))
			    / sigma ;
		    } else {
			int ia = (int)(args[2] % NurbsArray.this.dim) + 1;
			int ib = (int)((index - 3) % NurbsArray.this.hdim);
			results = (ia == ib)?
			    (-N(i, NurbsArray.this.degree, u)/g)/sigma: 0.0;
		    }
		    return results;
		}
	    };

	double [] guess = new double[carray.length-1];
	for (int i = dim; i < guess.length; i += hdim) {
	    guess[i] = 1.0;
	}
	double[] indices = new double[y.length];
	for (int i = 0; i < y.length; i++) {
	    indices[i] = (double) i;
	}

	LMA.findMin(funct, guess,
		    config.getLambda(), config.getNu(),
		    config.getLimit(), config.getIterationLimit(),
		    y, sigma, indices, x);

	for (int i = 0; i < guess.length && i < dim; i++) {
	    carray[i] = guess[i];
	}
	if (guess.length > dim) {
	    carray[dim] = 1.0;
	}
	for (int i = hdim; i < guess.length; i++) {
	    if (i % hdim == 0) {
		carray[i+dim] = guess[i];
	    } else {
		carray[i-1] = guess[i];
	    }
	}
    }
    */

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
     * Compute the value of a NURBS's basis function.
     * @param i a integer representing a knot span with a value
     *        equal to the index of the knot at the start of a span
     * @param p the degree of the basis function
     * @param u the value of the NURBS's parameter
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

    // The implementation adds a stack to remember previously
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
	double w = hCValueAt(array, off, u);
	for (int i = 0; i < dim; i++) {
	    array[off+i] /= w;
	}
    }


    // Compute the value in homogeneous coordinates. dim values are
    // stored in the array and the value for the additional dimension is
    // returned.
    private double hCValueAt(double[] array, int off, double u)
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
	int hp1dim = hp1*hdim;
	double[] d1 = new double[hp1*hdim];
	double[] d2 = new double[hp1*hdim];
	for (int i = 0; i < hp1dim; i++) {
	    int j = index*hdim - p*hdim + i;
	    if (j < 0) continue;
	    d1[i] = carray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r*hdim; j < hp1dim; j++) {
		int ii = index - p + j/hdim;
		int i = index*hdim-p*hdim+j;
		double alpha = (u - uarray[ii])
		    / (uarray[ii+p-r+1] - uarray[ii]);
		d2[j] = (1.0 - alpha)*d1[j-hdim] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	// return d1[h];
	int base = h*hdim;
	System.arraycopy(d1, base, array, off, dim);
	return d1[base+dim];
    }

    @Override
    public void derivAt(double[] array, int off, double u)
	throws IllegalArgumentException
    {
	if (needQarray) computeQarray();
	double [] fparray = new double[dim];
	double wp = computeDeriv(u, degree-1, 1, qarray, fparray, 0);
	double[] farray = new double[dim];
	double w = hCValueAt(farray, 0, u);
	for (int i = 0; i < dim; i++) {
	    array[i+off] = fparray[i] / w - (farray[i] * wp) / (w*w);
	}
    }

    @Override
    public void secondDerivAt(double[] array, int off, double u) {
	if (needQarray) computeQarray();
	if (needQQarray) computeQQarray();
	double [] fparray = new double[dim];
	double wp = computeDeriv(u, degree-1, 1, qarray, fparray, 0);
	double[] farray = new double[dim];
	double w = hCValueAt(farray, 0, u);
	double w2 = w*w;
	double w3 = w2*2;
	double[] fpparray = new double[dim];
	double wpp = computeDeriv(u, degree-2, 2, qqarray, fpparray, off);
	for (int i = 0; i < dim; i++) {
	    array[i+off] = fpparray[i]/w
		- 2.0 * (fparray[i]*wp)/(w2)
		- (farray[i] * wpp) / (w2)
		+ 2.0 * (farray[i] * wp)/(w3);
	}
    }

    // Compute the derivative in homogeneous coordinates. dim values are
    // stored in the array and the value for the additional dimension is
    // returned.
    private double computeDeriv(double u, int p, int offset, double[] qarray,
			      double[] array, int off)
	throws IllegalArgumentException
    {
	if (periodic) u = getStandardU(u);
	if (u < getDomainMin() || u > getDomainMax()) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeD", u));
	}

	if (qarray == null) {
	    Arrays.fill(array, off, dim, 0.0);
	    return 0.0;
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
	int hp1dim = hp1*hdim;
	double[] d1 = new double[hp1dim];
	double[] d2 = new double[hp1dim];
	for (int i = 0; i < hp1dim; i++) {
	    int j = index*hdim - offset*hdim - p*hdim + i;
	    if (j < 0) continue;
	    d1[i] = qarray[j];
	}
	for (int r = 1; r < hp1; r++) {
	    for (int j = r*hdim; j < hp1dim; j++) {
		int ii = index - p + j/hdim;
		int i = index*hdim-p*hdim+j;
		double alpha = (u - uarray[ii])
		    / (uarray[ii+p-r+1] - uarray[ii]);
		d2[j] = (1.0 - alpha)*d1[j-hdim] + alpha *d1[j];
	    }
	    double[] tmp = d1;
	    d1 = d2;
	    d2 = tmp;
	}
	// return d1[h];
	int base = h*hdim;
	System.arraycopy(d1, base, array, off, dim);
	return d1[base+dim];
    }
}

//  LocalWords:  exbundle NURBS BSplineArray ul li le hellip mdash lt
//  LocalWords:  frasl blockquote infin NurbsArray boolean NURBSs yy
//  LocalWords:  getPeriodEnd getPeriodStart NURBS's cpoints nbsp nxi
//  LocalWords:  cptsNotMultipleOfDim argNonNegative knotSpanEmpty ia
//  LocalWords:  incompatibleArrayLengths defaultConfig config newx
//  LocalWords:  Levenberg Marquardt arraycopy len carray funct args
//  LocalWords:  RealValuedFunctionVA valueAtAux valueAt jacobian ib
//  LocalWords:  derivAt wmode sv vv deriv arg RuntimeException hdim
//  LocalWords:  indices LMA findMin getLambda getNu getLimit BSpline
//  LocalWords:  getIterationLimit BSplineMode UNCLAMPED createKnots
//  LocalWords:  ssigma
