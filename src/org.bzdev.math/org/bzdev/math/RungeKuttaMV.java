package org.bzdev.math;
import java.util.Arrays;
import org.bzdev.lang.UnexpectedExceptionError;
//@exbundle org.bzdev.math.lpack.Math
/**
 * Multi-variable implementation of the Runge Kutta algorithm.
 * For an independent variable t, and a vector of dependent
 * variables <bold>y</bold>(t),
 * the Runge Kutta algorithm numerically solves the differential equation
 * <blockquote>
 *  <bold>y</bold>'(t) = f(t, <bold>y</bold>(t))
 * </blockquote>
 * The method {@link #getParam()} returns the independent variable
 * while the method {@link #getValue(int)} and {@link #getDeriv(int)} return
 * the dependent variable and its first derivative respectively.
 * The independent variable is changed, and the dependent
 * variables are updated, by using the methods {@link #update(double)},
 * {@link #update(double,int)}, {@link #adaptiveUpdate(double)},
 * {@link #updateTo(double)}, or {@link #updateTo(double,double)}.
 * The methods {@link #update(double)}, {@link #update(double,int)}, and
 * {@link #updateTo(double,double)} use the 4<sup>th</sup> order
 * Runge-Kutta method, while the methods {@link #adaptiveUpdate(double)}
 * and {@link #update(double)} use the Runge-Kutta-Fehlberg method
 * (RK45), which adaptively adjusts the step size given a specified
 * tolerance.  The method {@link #minStepSize()} will report the
 * minimum step size used by the Runge-Kutta-Fehlberg method. This is
 * useful if one wants an estimate of the number of knots needed for a
 * spline that will fit the solution to a differential equation. Before
 * the Runge-Kutta-Fehlberg method is used, the method
 * {@link #setTolerance(double)} must be called.
 * <P>
 * When parameters are provided (via a generic type), the parameters are
 * used to adjust the behavior of the class' function, typically by
 * providing various constants that it needs.  This can reduce the
 * number of classes created by an application in some instances.
 * The parameters are represented by a Java class typically used as
 * a container to hold a set of values.
 */
abstract public class RungeKuttaMV<P> {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    P parameters = null;

    /**
     * Set a RungeKuttaMV's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'applyFunction'.
     * @param parameters the parameters
     */
    public void setParameters(P parameters) {
	this.parameters = parameters;
    }

    /**
     * Get a RungeKuttaMV's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'applyFunction'.

     * @return an instance of the class representing a Runge-Kutta class'
     *         parameters (this will be the same instance passed to
     *         setParameters)
     */
    public P getParameters() {return parameters;}


    /**
     *  Apply a function to compute the derivatives given a parameter t and a
     *  variables y.
     *  @param t the independent variable
     *  @param y the values of the dependent variables.
     *  @param results the derivatives for the dependent variables
     */
    abstract protected void applyFunction(double t, double[] y,
					  double[]results);
    int n;
    double t;
    double[] y;
    double[] tmp;
    double[] k1;
    double[] k2;
    double[] k3;
    double[] k4;
    double[] k5;
    double[] k6;

    double tc = 0.0;
    double tcc = 0.0;		// correction to tc for updateTo(double)
    double[] yc;

    /**
     * Set initial conditions.
     * This is also done in one of the constructors.
     *  @param t0 the initial value of the independent variable
     *  @param y0 the initial values of the dependent variables
     */
    public final void setInitialValues(double t0, double[] y0) {
	needDeriv = true;
	t = t0;
	System.arraycopy(y0, 0, y, 0, n);
	tc = 0.0;
	tcc = 0.0;
	Arrays.fill(yc, 0.0);
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Constructor.
     * @param n the number of variables
     */
    public RungeKuttaMV(int n) {
	super();
	this.n = n;
	y = new double[n];
	tmp = new double[n];
	k1 = new double[n];
	k2 = new double[n];
	k3 = new double[n];
	k4 = new double[n];
	k5 = new double[n];
	k6 = new double[n];
	yc = new double[n];
    }

    /**
     * Constructor with initial values.
     *  @param n the number of variables
     *  @param t0 the initial value of the independent variable
     *  @param y0 the initial values of the dependent variables
     */
    public RungeKuttaMV(int n, double t0, double[] y0) {
	this(n);
	setInitialValues(t0, y0);
    }

    /**
     * Get the current value of a dependent variable.
     * @param index the dependent variable's index
     * @return the value of the dependent variable for the specified index
     */
    public final double getValue(int index) {
	return y[index];
    }

    /**
     * Get the current values of the dependent variables.
     * @param values an array to hold the values of the dependent variables
     */
    public final void getValues(double[] values) {
	System.arraycopy(y, 0, values, 0, n);
    }

    boolean needDeriv = true;

    /**
     * Get the current value of the derivative of a dependent variable
     * @param index the index of a dependent variable
     */
    public final double getDeriv(int index) {
	if (needDeriv) {
	    applyFunction(t, y, tmp);
	    needDeriv = false;
	}
	return tmp[index];
    }

    /**
     * Get the current value fo the derivatives of the dependent variables
     * @param derivs an array in which to store the derivatives
     */
    public final void getDerivs(double[] derivs) {
	if (needDeriv) {
	    applyFunction(t, y, tmp);
	    needDeriv = false;
	}
	System.arraycopy(tmp, 0, derivs, 0, n);
    }

    /**
     * Get the current value of the independent variable
     * @return the value of the independent variable
     */
    public final double getParam() {return t;}

    /**
     * Update the independent variable and the dependent variables.
     * @param h the amount by which the independent variable changes
     */
    public final void update(double h) {
	needDeriv = true;
	// t = t + h;
	double ty = h - tc;
	// double hh = h/2.0;
	double hh = ty/2.0;

	applyFunction(t, y, k1);
	for (int i = 0; i < n; i++) {
	    tmp[i] = y[i] + k1[i]*hh;
	}
	applyFunction(t+hh, tmp, k2);
	for (int i = 0; i < n; i++) {
	    tmp[i] = y[i] + k2[i]*hh;
	}
	applyFunction(t+hh, tmp, k3);
	for (int i = 0; i < n; i++) {
	    tmp[i] = y[i] + k3[i]*ty;
	}
	applyFunction(t + ty, tmp, k4);

	double tKahan = t + ty;
	tc = (tKahan - t) - ty;
	t = tKahan;
	for (int i = 0; i < n; i++) {
	    // y[i] = y[i] + h * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0;
	    double yy = (ty*(k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0) - yc[i];
	    double yKahan = y[i] + yy;
	    yc[i] = (yKahan - y[i]) - yy;
	    y[i] = yKahan;
	}
    }

    /**
     * Multi-step update of the independent variable  and the dependent
     * variables.
     * @param tincr the amount by which the independent variable changes
     * @param m the number of steps to use in changing the independent variable
     */
    public final void update(double tincr, int m) {
	//double tlast = t;
	needDeriv = true;
	double h = tincr / m;
	for (int j = 0; j < m; j++) {
	    double ty = h - tc;
	    double hh = ty/2.0;

	    applyFunction(t, y, k1);
	    for (int i = 0; i < n; i++) {
		tmp[i] = y[i] + k1[i]*hh;
	    }
	    applyFunction(t+hh, tmp, k2);
	    for (int i = 0; i < n; i++) {
		tmp[i] = y[i] + k2[i]*hh;
	    }
	    applyFunction(t+hh, tmp, k3);
	    for (int i = 0; i < n; i++) {
		tmp[i] = y[i] + k3[i]*ty;
	    }
	    applyFunction (t + ty, tmp, k4);

	    // t = t + h;
	    // double ty = h - tc;
	    double tKahan = t + ty;
	    tc = (tKahan - t) - ty;
	    t = tKahan;
	    for (int i = 0; i < n; i++) {
		// y[i] = y[i] + h * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0;
		double yy = (ty * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0)
		    - yc[i];
		double yKahan = y[i] + yy;
		yc[i] = (yKahan - y[i]) - yy;
		y[i] = yKahan;
	    }
	}
	// t = tlast + tincr;	// improve accuracy
    }

    private double[] tol = null;
    private double lasth = 0.0;
    private double minLasth = Double.MAX_VALUE;

    /**
     * Get the minimum step size used since the last time the
     * initial values were set, the tolerance was changed, or
     * this method was called.
     * <P>
     * After this method is called, subsequent calls will return 0.0
     * unless either {@link #adaptiveUpdate(double)} or {@link #updateTo(double)}
     * was called with an argument that would change the current value
     * of the independent variable.  Changing the initial value or
     * the tolerance will also result in this method returning 0.0 until
     * either {@link #adaptiveUpdate(double)} or {@link #updateTo(double)}
     * is called with an argument that would change the current value
     * of the independent variable.
     * @return the minimum step size; 0.0 if the minimum cannot yet be
     *         determined
     */
    public double minStepSize() {
	if (minLasth == Double.MAX_VALUE) return 0.0;
	double result = minLasth;
	minLasth = Double.MAX_VALUE;
	return result;
    }

    /**
     * Set the tolerances to the same values.
     * When the independent variable is updated, changing it by an amount t,
     * the error is bounded by the absolute value of the change in the
     * independent variable multiplied by the tolerance.  A tolerance applies to
     * the methods {@link #adaptiveUpdate(double)} and
     * {@link #updateTo(double)}.
     * <P>
     * The class {@link org.bzdev.devqsim.SimObject} had a public
     * method named {@link org.bzdev.devqsim.SimObject#update()} that
     * by default calls a protected method named
     * {@link org.bzdev.devqsim.SimObject#update(double,long)}. A simulation
     * object whose behavior is determined by a differential equation may
     * contain a field whose value is an instance of {@link RungeKutta},
     * and the implementation of these update methods may call
     * {@link #adaptiveUpdate(double)} or {@link #updateTo(double)}. When
     * this is the case, the tolerance(s) must typically be set before
     * the simulation object's update method is called.  The exception is
     * when the the simulation time matches the value of the independent
     * variable so that adpativeUpdate will be called with an argument of 0.0.
     * @param tol the tolerance
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(double tol) {
	if (tol <= 0.0)
	    throw new IllegalArgumentException(errorMsg("argNotPositive", tol));
	if (this.tol == null) {
	    this.tol = new double[n];
	}
	Arrays.fill(this.tol, tol);
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Set the tolerances.
     * When the independent variable is updated, changing it by an amount t,
     * the error is bounded by the absolute value of the change in the
     * independent variable multiplied by the tolerance.  A tolerance applies to
     * the methods {@link #adaptiveUpdate(double)} and
     * {@link #updateTo(double)}.
     * <P>
     * The class {@link org.bzdev.devqsim.SimObject} had a public
     * method named {@link org.bzdev.devqsim.SimObject#update()} that
     * by default calls a protected method named
     * {@link org.bzdev.devqsim.SimObject#update(double,long)}. A simulation
     * object whose behavior is determined by a differential equation may
     * contain a field whose value is an instance of {@link RungeKutta},
     * and the implementation of these update methods may call
     * {@link #adaptiveUpdate(double)} or {@link #updateTo(double)}. When
     * this is the case, the tolerance(s) must typically be set before
     * the simulation object's update method is called.  The exception is
     * when the the simulation time matches the value of the independent
     * variable so that adpativeUpdate will be called with an argument of 0.0.
     * @param tol the tolerances
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(double[] tol) {
	for (int i = 0; i < tol.length && i < n; i++) {
	    if (tol[i] <= 0.0)
		throw new
		    IllegalArgumentException
		    (errorMsg("argNotPositive", tol[i]));
	}
	if (this.tol == null)  {
	    this.tol = new double[n];
	}
	System.arraycopy(tol, 0, this.tol, 0, n);
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Set the tolerance for a specific dependent variable.
     * When the independent variable is updated, changing it by an amount t,
     * the error is bounded by the absolute value of the change in the
     * independent variable multiplied by the tolerance.  A tolerance applies to
     * the methods {@link #adaptiveUpdate(double)} and
     * {@link #updateTo(double)}.
     * <P>
     * The class {@link org.bzdev.devqsim.SimObject} had a public
     * method named {@link org.bzdev.devqsim.SimObject#update()} that
     * by default calls a protected method named
     * {@link org.bzdev.devqsim.SimObject#update(double,long)}. A simulation
     * object whose behavior is determined by a differential equation may
     * contain a field whose value is an instance of {@link RungeKutta},
     * and the implementation of these update methods may call
     * {@link #adaptiveUpdate(double)} or {@link #updateTo(double)}. When
     * this is the case, the tolerance(s) must typically be set before
     * the simulation object's update method is called.  The exception is
     * when the the simulation time matches the value of the independent
     * variable so that adaptiveUpdate will be called with an argument of 0.0.
     * @param i the index for the variable
     * @param tol the tolerance for the variable whose index is i
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(int i, double  tol) {
	if (i > n || i < 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	if (tol <= 0.0)
	    throw new
		IllegalArgumentException(errorMsg("argNotPositive", tol));
	if (this.tol == null)  {
	    this.tol = new double[n];
	}
	this.tol[i] = tol;
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }


    /**
     * Get the current tolerance for the dependent variable.
     * @param i the index of the dependent variable whose tolerance will
     *        be returned
     * @return the tolerance; zero if the tolerance has not been set
     */
    public double getTolerance1(int i) {return (tol == null)? 0.0: tol[i];}


    /**
     * Get an array of tolerance, indexed by the indices of the
     * dependent variables.
     * @param array the array in which to store the values
     * @exception IndexOutOfBoundException the argument array is too small
     *            to contain the tolerances
     * @exception NullPointerException the argument was null
     */
    public void getTolerances(double[] array) {
	System.arraycopy(tol, 0, array, 0, n);
    }

    private static final double F1 = 1.0/4.0;

    private static final double F2 = 3.0/8.0;
    private static final double F3 = 3.0/32.0;
    private static final double F4 = 9.0/32.0;

    private static final double F5 = 12.0/13.0;
    private static final double F6 = 1932.0/2197.0;
    private static final double F7 = -7200.0/2197.0;
    private static final double F8 = 7296.0/2197.0;

    private static final double F9 = 439.0/216.0;
    private static final double F10 = 3680.0/513.0;
    private static final double F11 = -845.0/4104.0;

    private static final double F12 = 1.0/2.0;
    private static final double F13 = -8.0/27.0;
    private static final double F14 = -3544.0/2565.0;
    private static final double F15 = 1859.0/4104.0;
    private static final double F16 = -11.0/40.0;

    private static final double F17 = 25.0/216.0;
    private static final double F18 = 1408.0/2565.0;
    private static final double F19 = 2197.0/4104.0;
    private static final double F20 = -1.0/5.0;

    private static final double F21 = 16.0/135.0;
    private static final double F22 = 6656.0/12825.0;
    private static final double F23 = 28561.0/56430.0;
    private static final double F24 = -9.0/50.0;
    private static final double F25 = 2.0/55.0;

    /**
    * Update the independent variable and variable adaptively, increasing the
    * independent variable by a specified amount.
     * @param tincr the increment for the independent variable.
     * @exception IllegalStateException the method {@link #setTolerance(double)}
     *            has not been called
     */
    public void adaptiveUpdate(double tincr)  throws IllegalStateException {
	if (tincr == 0.0) return;
	needDeriv = true;
	if (tol == null) {
	    throw new IllegalStateException(errorMsg("tolNotSet"));
	}
	boolean hNotModified = true;
	boolean needInit = (lasth == 0.0);
	double h;
	if (needInit){
	    h = tincr;
	} else {
	    if (Math.abs(lasth) > Math.abs(tincr)) {
		h = tincr;
		hNotModified = false;
	    } else {
		h = (tincr < 0)? -lasth: lasth;
	    }
	}
	double tlimit = t + (tincr - tc);
	boolean more = true;
	while (more) {
	    double ty = h - tc;
	    double tKahan = t + ty;
	    if ((tincr > 0 && tKahan > tlimit)
		|| (tincr < 0 && tKahan < tlimit)) {
		lasth = Math.abs(h);
		// if (lasth < minLasth) minLasth = lasth;
		h -= (tKahan - tlimit);
		ty = h - tc;
		tKahan = t + ty;
		hNotModified = false;
		more = false;
	    } else if (tKahan == tlimit && needInit == false) {
		more = false;
	    }
	    // double k1 = ty*function(t,y);
	    applyFunction(t, y, k1);
	    for (int i = 0; i < n; i++) {
		k1[i] *= ty;
		tmp[i] = y[i] + F1*k1[i];
	    }
	    // double k2 = ty*function(t + F1*ty, y + F1*k1);
	    applyFunction(t + F1*ty, tmp, k2);
	    for (int i = 0; i < n; i++) {
		k2[i] *= ty;
		tmp[i] = y[i] + F3*k1[i] + F4*k2[i];
	    }
	    // double k3 = ty*function(t + F2*ty, y + F3*k1 + F4*k2);
	    applyFunction(t + F2*ty, tmp, k3);
	    for (int i = 0; i < n; i++) {
		k3[i] *= ty;
		tmp[i] = y[i] + F6*k1[i] + F7*k2[i] + F8*k3[i];
	    }
	    applyFunction(t + F5*ty, tmp, k4);
	    // double k4 = ty*function(t + F5*ty, y + F6*k1 + F7*k2 + F8*k3);
	    for (int i = 0; i < n; i++) {
		k4[i] *= ty;
		tmp[i] = y[i] +  F9*k1[i] - 8.0*k2[i] + F10*k3[i] + F11*k4[i];
	    }
	    // double k5 = ty*function(t + ty,
	    //                         y + F9*k1 - 8.0*k2 + F10*k3 + F11*k4);
	    applyFunction(t + ty, tmp, k5);
	    for (int i = 0; i < n; i++) {
		k5[i] *= ty;
		tmp[i] = y[i] + F13*k1[i] + 2.0*k2[i] + F14*k3[i] + F15*k4[i]
		    + F16*k5[i];
	    }
	    // double k6 = ty*function(t + F12*ty,
	    //                         y + F13*k1 + 2.0*k2 + F14*k3 + F15*k4
	    //                         + F16*k5);
	    applyFunction(t + F12*ty, tmp, k6);
	    double s4 = Double.MAX_VALUE;
	    boolean cont = false;
	    for (int i = 0; i < n; i++) {
		k6[i] *= ty;
		double yincr2 = F21*k1[i] + F22*k3[i] + F23*k4[i] + F24*k5[i]
		    + F25*k6[i];
		// use k6 for yincr1 to avoid an extra array allocation
		k6[i] = F17*k1[i] + F18*k3[i] + F19*k4[i]
		    + F20*k5[i];
		double tmp = Math.abs(yincr2 - k6[i]);
		double residual = tmp/Math.abs(ty);
		if (tol[i] <= 0.0) {
		    throw new
			IllegalStateException(errorMsg("tolNotSetForI", i));
		}
		if (residual > tol[i]) cont = true;
		double yincrDiff = 2.0 * tmp;
		double s4tmp = (yincrDiff == 0.0)? 1.0:
		    tol[i] * Math.abs(ty)/yincrDiff;
		if (s4tmp < s4) s4 = s4tmp;
	    }
	    // double s = Math.pow(s4, 0.25);
	    double s = Math.sqrt(s4);
	    s = Math.sqrt(s);
	    h *= s;
	    if (cont) {
		more = true;
		continue;
	    }
	    tc = (tKahan - t) - ty;
	    t = tKahan;
	    // y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	    for (int i = 0; i < n; i++) {
		double yy = k6[i] - yc[i];
		double yKahan = y[i] + yy;
		yc[i] = (yKahan - y[i]) - yy;
		y[i] = yKahan;
	    }
	    if (hNotModified) {
		lasth = Math.abs(h);
		if (lasth < minLasth) minLasth = lasth;
	    }
	}
	tcc = 0.0;
    }

    /**
     * Update the independent variable and variable so that the
     * independent variable will have a specified value.
     * The step size will be determined by this method.
     * @param t the new value of the independent variable
     * @exception IllegalStateException one of the methods named setTolerance
     *            has not been called.
     */
    public final void updateTo(double t) throws IllegalStateException {
	double incr = t - (this.t - tcc);
	adaptiveUpdate(incr);
	// force the current time to be the argument t
	// but arrange so that subtracting tc will
	// move us back to this.t.
	tcc = (t - this.t);
	tc += (t - this.t);
	this.t = t;
    }


    /**
     * Update the independent variable and dependent variables so that
     * the independent variable will have a specified value and so that
     * the step size is a specified value or lower.
     * <P>
     * Note, regardless of the value of h, the maximum number of
     * steps used will be no greater than Integer.MAX_VALUE.
     * @param t the new value of the independent variable
     * @param h the step size limit
     * @exception IllegalArgumentException an argument was out of range
     *            (e.g, h was 0 or negative)
     */
    public final void updateTo(double t, double h)
	throws IllegalArgumentException
    {
	if (h <= 0.0) throw new IllegalArgumentException
			  (errorMsg("secondArgPos", h));
	if (t == this.t) return;
	double tincr = (t - this.t);
	long n = Math.round(Math.ceil(Math.abs(tincr)/h));
	int nn =  (n > Integer.MAX_VALUE)? Integer.MAX_VALUE: (int)n;
	update(tincr, nn);
	this.t = t;
    }
}

//  LocalWords:  RungeKutta adaptiveUpdate updateTo
