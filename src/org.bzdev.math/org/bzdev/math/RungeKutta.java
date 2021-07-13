package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Implementation of the Runge Kutta algorithm for first-order differential
 * equations. For an independent variable t (which will be called the
 * "parameter" mainly so that a method with a short name
 * {@link #getParam()} can be used to obtain its current value), and a
 * dependent variable <bold>y</bold>(t), the Runge Kutta algorithm
 * numerically solves the differential equation
 * <blockquote>
 *  y'(t) = f(t, y(t))
 * </blockquote>
 * The method {@link #getParam()} returns the independent variable
 * while the methods {@link #getValue()} and {@link #getDeriv()} return
 * the dependent variable and its first derivative respectively.
 * The independent variable is changed, and the dependent
 * variable updated, by using the methods {@link #update(double)},
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
abstract public class RungeKutta<P> {
    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    P parameters = null;

    /**
     * Set a RungeKuttaMV's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'function'.
     * @param parameters the parameters
     */
    public void setParameters(P parameters) {
	this.parameters = parameters;
    }

    /**
     * Get a RungeKuttaMV's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'function'.

     * @return an instance of the class representing a Runge-Kutta class'
     *         parameters (this will be the same instance passed to
     *         setParameters)
     */
    public P getParameters() {return parameters;}

   /**
     * Function to compute the derivative of y given an independent
     * variable t and a dependent variable y.
     *  @param t the value of the independent variable, with respect to which
     *         one differentiates
     *  @param y the value of the dependent variable for the specified parameter
     *  @return the value of dy/dt as given by the differential equation
     */
    abstract protected double function(double t, double y);

    double t;
    double y;
    double tc = 0.0;		// use for Kahan's summation algorithm for t
    double tcc = 0.0;		// correction to tc for updateTo(double)
    double yc = 0.0;		// use for Kahan's summation algorithm for y

    /**
     * Set initial conditions.
     * This is also done in the constructor.
     *  @param t0 the initial value of the independent variable
     *  @param y0 the value of the dependent variable
     *  @exception IllegalArgumentException the initial values are not in
     *             the domain of this instance's function
     */
    public void setInitialValues(double t0, double y0)
	throws IllegalArgumentException
    {
	t = t0;
	y = y0;
	tc = 0.0;
	tcc = 0.0;
	yc = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Constructor.
     * The initial values and parameters (if any) must be set before
     * the class is used.
     */
    public RungeKutta() {
	super();
    }

    /**
     * Constructor with initial values.
     *  @param t0 the initial value of the independent variable
     *  @param y0 the initial value of the dependent variable
     */
    public RungeKutta(double t0, double y0) {
	super();
	setInitialValues(t0, y0);
    }

    /**
     * Get the current value of the dependent variable.
     * @return the value of the variable
     */
    public final double getValue() {return y;}


    /**
     * Get the current value of the deriviative of the dependent variable
     * @return the derivative of the dependent variable.
     */
    public final double getDeriv() {
	return function(t, y);
    }

    /**
     * Get the current value of the independent variable.
     * @return the value of the independent variable
     */
    public final double getParam() {return t;}

    /**
     * Update the independent and dependent variables.
     * @param h the amount by which the independent variable changes
     */
    public final void update(double h) {
	h = h - tc;
	double hh = h/2.0;
	double k1 = function(t, y);
	double k2 = function(t + hh, y + k1*hh);
	double k3 = function(t + hh, y+ k2*hh);
	double k4 = function(t + h, y + k3*h);
	// t = t + h;
	double tKahan = t + h;
	tc = (tKahan - t) - h;
	t = tKahan;
	// y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	double yy = (h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0) - yc;
	double yKahan = y + yy;
	yc = (yKahan - y) - yy;
	y = yKahan;
    }

    /**
     * Multi-step update of the independent and dependent variables.
     * @param tincr the amount by which the independent variable changes
     * @param n the number of steps to use in changing the independent variable
     */
    public final void update(double tincr, int n) {
	// double tlast = t;
	double h = tincr / n;
	// double hh = h/2.0;
	for (int i = 0; i < n; i++) {
	    double ty = h - tc;
	    double hh = ty/2.0;
	    double k1 = function(t, y);
	    double k2 = function(t + hh, y + k1*hh);
	    double k3 = function(t + hh, y+ k2*hh);
	    double k4 = function(t + ty, y + k3*ty);
	    // t = t + h;
	    double tKahan = t + ty;
	    tc = (tKahan - t) - ty;
	    t = tKahan;
	    // y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	    double yy = (ty * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0) - yc;
	    double yKahan = y + yy;
	    yc = (yKahan - y) - yy;
	    y = yKahan;
	}
	// t = tlast + tincr;	// to improve numerical accuracy
    }

    double tol = 0.0;
    double lasth = 0.0;
    double minLasth = Double.MAX_VALUE;

    /**
     * Get the minimum step size used since the last time the
     * initial values were set, the tolerance was changed, or
     * this method was called.
     * <P>
     * After this method is called, subsequent calls will return 0.0
     * unless either {@link #adaptiveUpdate(double)} or
     * {@link #updateTo(double)} was called with an argument that would
     * change the current value of the independent variable.  Changing
     * the initial value or the tolerance will also result in this
     * method returning 0.0 until either {@link #adaptiveUpdate(double)}
     * or {@link #updateTo(double)} is called with an argument that
     * would change the current value of the independent variable.
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
     * Set the tolerance.
     * When the independent variable is updated, changing it by an amount t,
     * the error is bounded by the absolute value of the change in the
     * parameter multiplied by the tolerance.  A tolerance applies to
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
     * @param tol the tolerance
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(double tol) {
	if (tol <= 0.0)
	    throw new IllegalArgumentException(errorMsg("argNotPositive", tol));
	this.tol = tol;
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Get the current tolerance.
     * @return the tolerance; zero if the tolerance has not been set
     */
    public double getTolerance() {return tol;}


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
    * Update the independent and dependent variables adaptively, increasing the
    * parameter by a specified amount.
     * @param tincr the increment for the independent variable.
     * @exception IllegalStateException the method {@link #setTolerance(double)}
     *            has not been called
     */
    public void adaptiveUpdate(double tincr)  throws IllegalStateException {
	if (tincr == 0.0) return;
	if (tol == 0.0) {
	    throw new IllegalStateException(errorMsg("tolNotSet"));
	}
	boolean needInit = (lasth == 0.0);
	double h;
	boolean hNotModified = true;
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
		if (lasth < minLasth) minLasth = lasth;
		h -= (tKahan - tlimit);
		ty = h - tc;
		tKahan = t + ty;
		hNotModified = false;
		more = false;
	    } else if (tKahan == tlimit && needInit == false) {
		more = false;
	    }
	    double k1 = ty*function(t,y);
	    double k2 = ty*function(t + F1*ty, y + F1*k1);
	    double k3 = ty*function(t + F2*ty, y + F3*k1 + F4*k2);
	    double k4 = ty*function(t + F5*ty, y + F6*k1 + F7*k2 + F8*k3);
	    double k5 = ty*function(t + ty,
				    y + F9*k1 - 8.0*k2 +F10*k3 + F11*k4);
	    double k6 = ty*function(t + F12*ty,
				    y + F13*k1 + 2.0*k2 + F14*k3 + F15*k4
				    + F16*k5);

	    double yincr1  = F17*k1 + F18*k3 + F19*k4 + F20*k5;
	    double yincr2 = F21*k1 + F22*k3 + F23*k4 + F24*k5 + F25*k6;

	    /*
	    double hh = ty/2.0;
	    double kk1 = function(t, y);
	    double kk2 = function(t + hh, y + kk1*hh);
	    double kk3 = function(t + hh, y+ kk2*hh);
	    double kk4 = function(t + h, y + kk3*h);
	    // y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	    double incr3 = (h * (kk1 + 2 *kk2 + 2 *kk3 + kk4) / 6.0);
	    // incr3 should be the same as incr1?;
	    if (Math.abs((yincr1 - incr3)/incr3) > 1.e-10) {
		System.out.format("yincr1 = %s, incr3 = %s\n",
				  yincr1, incr3 );
		System.exit(1);
	    }
	    */

	    double tmp = Math.abs(yincr2 - yincr1);
	    double residual = tmp/Math.abs(ty);

	    double yincrDiff = 2.0 * tmp;
	    double s4 = (yincrDiff == 0.0)? 1.0:
		tol * Math.abs(ty)/yincrDiff;
	    // double s = Math.pow(s4, 0.25);
	    double s = Math.sqrt(s4);
	    s = Math.sqrt(s);
	    h *= s;
	    if (residual > tol) {
		more = true;
		continue;
	    }

	    tc = (tKahan - t) - ty;
	    t = tKahan;

	    // y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	    double yy = yincr1 - yc;
	    double yKahan = y + yy;
	    yc = (yKahan - y) - yy;
	    y = yKahan;
	    if (hNotModified) {
		lasth = Math.abs(h);
		if (lasth < minLasth) minLasth = lasth;
	    }
	}
	tcc = 0.0;
    }

    /**
     * Update the independent and dependent variables so that the
     * independent variable will have a specified value.
     * The step size will be determined by this method.
     * @param t the new value of the independent variable
     * @exception IllegalStateException the method {@link #setTolerance(double)}
     *            has not been called
     */
    public final void updateTo(double t) throws IllegalStateException {
	double incr = t - (this.t - tcc);;
	adaptiveUpdate(incr);
	// force the current time to be the argument t
	// but arrange so that subtracting tc will
	// move us back to this.t.
	tcc = (t - this.t);
	tc += (t - this.t);
	this.t = t;
    }

    /**
     * Update the independent and dependent variables so that the
     * independent variable will have a specified value and so that
     *  the step size is a specified value or lower.
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


    private static class RVFRungeKutta extends RungeKutta {
	RealValuedFunctionVA f;

	@Override
	public void setInitialValues(double t0, double y0)
	    throws IllegalArgumentException
	{
	    if (!f.isInDomain(t0, y0)) {
		throw new IllegalArgumentException
		    (errorMsg("notInDomainTwo", t0, y0));
	    }
	    super.setInitialValues(t0, y0);
	}

	public  RVFRungeKutta (RealValuedFunctionVA f)
	    throws IllegalArgumentException
	{
	    super();
	    if (f.minArgLength() > 2 || f.maxArgLength() < 2) {
		throw new IllegalArgumentException(errorMsg("minArgLength"));
	    }
	    this.f = f;
	}

	public  RVFRungeKutta (RealValuedFunctionVA f, double t0, double y0)
	    throws IllegalArgumentException
	{
	    super();
	    if (f.minArgLength() > 2 || f.maxArgLength() < 2) {
		throw new IllegalArgumentException(errorMsg("minArgLength"));
	    }
	    this.f = f;
	    setInitialValues(t0, y0);
	}

	public  RVFRungeKutta (RealValuedFunctionTwo f, double t0, double y0)
	    throws IllegalArgumentException
	{
	    super();
	    this.f = f;
	    setInitialValues(t0, y0);
	}

	@Override
	protected final double function(double t, double y) {
	    return f.valueAt(t, y);
	}
    }

    private static class RVFOpsRungeKutta extends RungeKutta {
	RealValuedFunctTwoOps f;

	@Override
	public void setInitialValues(double t0, double y0)
	    throws IllegalArgumentException
	{
	    super.setInitialValues(t0, y0);
	}

	public  RVFOpsRungeKutta (RealValuedFunctTwoOps f)
	    throws IllegalArgumentException
	{
	    super();
	    this.f = f;
	}

	public RVFOpsRungeKutta (RealValuedFunctTwoOps f, double t0, double y0)
	    throws IllegalArgumentException
	{
	    super();
	    this.f = f;
	    setInitialValues(t0, y0);
	}

	@Override
	protected final double function(double t, double y) {
	    return f.valueAt(t, y);
	}
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionVA as its function.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y).
     * <P>
     * Note: Usually f will be an instance of RealValuedFunctionTwo.
     * If f is not, it must except a minimum of 2 arguments and it
     * will be passed exactly two arguments when this instance is
     * updated.
     * @param f the function, which must take two arguments
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctionVA f)
	throws IllegalArgumentException
    {
	return new RVFRungeKutta(f);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionVA as its function, providing initial values.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y).
     * <P>
     * Note: Usually f will be an instance of RealValuedFunctionTwo.
     * If f is not, it must except a minimum of 2 arguments and it
     * will be passed exactly two arguments when this instance is
     * updated.
     * @param f the function, which must take exactly two arguments
     * @param t0 the initial value of the independent variable with respect
     *           to which one differentiates
     * @param y0 the initial value of the variable for the specified parameter
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctionVA f,
					 double t0, double y0)
    {
	return new RVFRungeKutta(f, t0, y0);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionTwo as its function.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y).
     * @param f the function, which must take two arguments
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctionTwo f)
	throws IllegalArgumentException
    {
	return new RVFRungeKutta(f);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionTwo as its function, providing initial values.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y).
     * @param f the function, which must take exactly two arguments
     * @param t0 the initial value of the independent variable with respect
     *           to which one differentiates
     * @param y0 the initial value of the variable for the specified parameter
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctionTwo f,
					 double t0, double y0)
    {
	return new RVFRungeKutta(f, t0, y0);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctTwoOps as its function.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y). A lambda expression may
     * be used as the first argument.
     * @param f the function, which must take two arguments
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctTwoOps f)
    {
	return new RVFOpsRungeKutta(f);
    }


    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctTwoOps as its function and providing the initial
     * conditions.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y). A lambda expression may
     * be used as the first argument.
     * @param f the function, which must take two arguments
     * @param t0 the initial value of the independent variable with respect
     *           to which one differentiates
     * @param y0 the initial value of the variable for the specified parameter
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta newInstance(RealValuedFunctTwoOps f,
					 double t0, double y0)
    {
	return new RVFOpsRungeKutta(f, t0, y0);
    }
}

//  LocalWords:  exbundle Runge Kutta blockquote th updateTo Fehlberg
//  LocalWords:  RK adaptiveUpdate setTolerance RungeKuttaMV's param
//  LocalWords:  setParameters dy dt Kahan's tc Multi tincr tlast hh
//  LocalWords:  IllegalArgumentException tol argNotPositive ty kk
//  LocalWords:  IllegalStateException tolNotSet incr yincr
//  LocalWords:  secondArgPos notInDomainTwo minArgLength RungeKutta
//  LocalWords:  RealValuedFunctionVA RealValuedFunctionTwo
