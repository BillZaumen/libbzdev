package org.bzdev.math;
import java.util.Arrays;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Runge Kutta algorithm for solutions of second order differential
 * equations.  For an independent variable t (which will be called the
 * "parameter" mainly so that a method with a short name
 * {@link #getParam()} can be used to obtain its current value), and a
 * dependent variable <bold>y</bold>(t), the Runge Kutta algorithm
 * is used to numerically solves the differential equation
 * <blockquote>
 *  y''(t) = f(t, y(t), y'(t))
 * </blockquote>
 * The method {@link #getParam()} returns the independent variable
 * while the methods {@link #getValue()}, {@link #getDeriv()}, and
 * {@link #getSecondDeriv()} return* the dependent variable, its
 * derivative, and its second derivative respectively. The independent
 * variable is changed, and the dependent variable updated, by using
 * the methods {@link #update(double)}, {@link #update(double,int)},
 * {@link #adaptiveUpdate(double)}, {@link #updateTo(double)}, or
 * {@link #updateTo(double,double)}.  The methods {@link #update(double)},
 * {@link #update(double,int)}, and {@link #updateTo(double,double)} use the
 * 4<sup>th</sup> order Runge-Kutta method, while the methods
 * {@link #adaptiveUpdate(double)} and {@link #update(double)} use the
 * Runge-Kutta-Fehlberg method (RK45), which adaptively adjusts the
 * step size given a specified tolerance.  The method {@link #minStepSize()}
 * will report the minimum step size used by the Runge-Kutta-Fehlberg
 * method. This is useful if one wants an estimate of the number of
 * knots needed for a spline that will fit the solution to a differential
 * equation.Before
 * the Runge-Kutta-Fehlberg method is used, the method
 * {@link #setTolerance(double)} or {{@link #setTolerance(double,double)}
 * must be called.
 * <P>
 * When parameters are provided (via a generic type), the parameters
 * are used to adjust the behavior of the class' function, typically
 * by providing various constants that it needs.  This can reduce
 * the number of classes created by an application in some instances.
 * The parameters are represented by a Java class typically used as
 * a container to hold a set of values.
 * <P>
 * The static methods {@link RungeKutta2#newInstance(RealValuedFunctionThree)},
 * {@link RungeKutta2#newInstance(RealValuedFunctionThree,double,double,double)},
 * {@link RungeKutta2#newInstance(RealValuedFunctThreeOps)},
 * and
 * {@link RungeKutta2#newInstance(RealValuedFunctThreeOps,double,double,double)},
 * can be used to create new instances of the RungeKutta2 class, but without
 * parameters.
 */
abstract public class RungeKutta2<P> {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    P parameters = null;

    /**
     * Set a RungeKutta2's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'applyFunction'.
     * @param parameters the parameters
     */
    public void setParameters(P parameters) {
	this.parameters = parameters;
    }

    /**
     * Get a RungeKutta2's parameters.
     * Parameters are used to provide values that will be constant
     * while the Runge-Kutta algorithm is running and may be used
     * by the method named 'function'.
     * @return an instance of the class representing a Runge-Kutta class'
     *         parameters (this will be the same instance passed to
     *         setParameters)
     */
    public P getParameters() {return parameters;}


    /**
     *  Apply a function to compute the derivatives given a parameter t and a
     *  variables y.
     *  @param t the parameter with respect to which one differentiates
     *  @param y the value of the variable for the specified parameter t
     *  @param yp the derivative of the variable for the specified parameter t
     *  @return the second derivative of y with respect to the parameter t
     */
    abstract protected double function(double t, double y, double yp);
    double t;
    /*
    double[] y;
    double[] tmp;
    double[] k1;
    double[] k2;
    double[] k3;
    double[] k4;
    */
    double y;
    double yp;
    // double tmpy;
    // double tmpyp;
    // double k1;
    // double kp1;
    // double k2;
    // double kp2;
    // double k3;
    // double kp3;
    // double k4;
    // double kp4;

    double tc = 0.0;
    // double[] yc;
    double yc;
    double tcc = 0.0;		// correction to tc for updateTo(double)
    double ypc;

    /**
     * Set initial conditions.
     * This is also done in the constructor.
     *  @param t0 the value of the parameter with respect to which
     *         one differentiates
     *  @param y0 the value of the variables for the initial value of
     *         the parameter with respect to which one differentiates
     */
    public void setInitialValues(double t0, double y0, double yp0) {
	t = t0;
	y = y0;
	yp = yp0;
	tc = 0.0;
	tcc = 0.0;
	yc = 0.0;
	ypc = 0.0;
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;

    }

    /**
     * Constructor.
     */
    public RungeKutta2() {}

    /**
     * Constructor with initial values.
     * @param t0 the initial value of the  parameter with respect to which
     *        one differentiates
     * @param y0 the initial value of the variable for the specified
     *        initial value of the parameter
     * @param yp0 the initial value of the variable's first derivative for
     *        the specified initial value of the parameter
     */
    public RungeKutta2(double t0, double y0, double yp0) {
	setInitialValues(t0, y0, yp0);
    }

    /**
     * Get the current value of the dependent variable.
     * @return the value of the variable
     */
    public final double getValue() {
	return y;
    }


    /**
     * Get the current value of the derivative of the dependent variable.
     * @return the value of the variable's derivative
     */
    public final double getDeriv() {
	return yp;
    }

    /**
     * Get the current value of the second deriviative of the dependent variable
     * @return the derivative of the dependent variable.
     */
    public final double getSecondDeriv() {
	return function(t, y, yp);
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
	/*
	// t = t + h;
	double ty = h - tc;
	// double hh = h/2.0;
	double hh = ty/2.0;
	*/
	h = h - tc;
	double hh = h/2.0;

	double k1 = yp;
	double kp1 = function(t, y, yp);
	double k2 = yp + kp1*hh;
	double kp2 = function(t+hh,y+k1*hh, yp+kp1*hh);
	double k3 = yp + kp2*hh;
	double kp3 = function(t+hh, y+k2*hh, yp +kp2*hh);
	double k4 = yp + kp3*h;
	double kp4 = function(t+h, y+k3*h, yp+kp3*h);

	double tKahan = t + h;
	tc = (tKahan - t) - h;
	t = tKahan;

	double yy = (h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0) - yc;
	double yKahan = y + yy;
	yc = (yKahan - y) - yy;
	y = yKahan;

	yy = (h * (kp1 + 2 *kp2 + 2 *kp3 + kp4) / 6.0) - ypc;
	yKahan = yp + yy;
	ypc = (yKahan - yp) - yy;
	yp = yKahan;
    }

    /**
     * Multi-step update of the independent and dependent variables.
     * @param tincr the amount by which the independent variable changes
     * @param n the number of steps to use in changing the independent
     *        variable
     */
    public final void update(double tincr, int n) {
	double h = tincr / n;
	// double hh = h/2.0;
	for (int i = 0; i < n; i++) {
	    double ty = h - tc;
	    double hh = ty/2.0;
	    double k1 = yp;
	    double kp1 = function(t, y, yp);
	    double k2 = yp + kp1*hh;
	    double kp2 = function(t + hh, y + k1*hh, yp + kp1*hh);
	    double k3 = yp + kp2*hh;
	    double kp3 = function(t + hh, y+ k2*hh, yp + kp2*hh);
	    double k4 = yp + kp3*ty;
	    double kp4 = function(t + ty, y + k3*ty, yp + kp3*ty);
	    // t = t + h;
	    double tKahan = t + ty;
	    tc = (tKahan - t) - ty;
	    t = tKahan;
	    // y = y + h * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0;
	    double yy = (ty * (k1 + 2 *k2 + 2 *k3 + k4) / 6.0) - yc;
	    double yKahan = y + yy;
	    yc = (yKahan - y) - yy;
	    y = yKahan;

	    yy = (ty * (kp1 + 2 *kp2 + 2 *kp3 + kp4) / 6.0) - ypc;
	    yKahan = yp + yy;
	    ypc = (yKahan - yp) - yy;
	    yp = yKahan;
	}
    }

    double tol1 = 0.0;
    double tol2 = 0.0;

    double lasth = 0.0;
    double minLasth = Double.MAX_VALUE;

    /**
     * Get the minimum step size used by the Runge-Kutta-Fehlberg
     * method since the last time the initial values were set, the
     * tolerance was changed, or this method was called.
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
     * When the parameter is updated, changing it by an amount t,
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
     * variable so that adpativeUpdate will be called with an argument of 0.0.
     * @param tol the tolerance
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(double tol) {
	if (tol <= 0.0)
	    throw new IllegalArgumentException(errorMsg("argNotPositive", tol));
	tol1 = tol;
	tol2 = tol;
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }

    /**
     * Set the tolerance.
     * When the parameter is updated, changing it by an amount t,
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
     * @param tol1 the tolerance for the dependent variable
     * @param tol2 the tolerance for the first derivative of the
     *             dependent variable.
     * @exception IllegalArgumentException the argument was less than or equal
     *            to zero
     */
    public void setTolerance(double tol1, double tol2) {
	if (tol1 <= 0.0)
	    throw new
		IllegalArgumentException(errorMsg("argNotPositive", tol1));
	if (tol2 <= 0.0)
	    throw new
		IllegalArgumentException(errorMsg("argNotPositive", tol2));
	this.tol1 = tol1;
	this.tol2 = tol2;
	lasth = 0.0;
	minLasth = Double.MAX_VALUE;
    }


    /**
     * Get the current tolerance for the dependent variable.
     * @return the tolerance; zero if the tolerance has not been set
     */
    public double getTolerance1() {return tol1;}

    /**
     * Get the current tolerance for the derivative of the dependent
     * variable.
     * @return the tolerance; zero if the tolerance has not been set
     */
    public double getTolerance2() {return tol2;}

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
    * independent variable by a specified amount.
     * @param tincr the increment for the independent variable.
     * @exception IllegalStateException the method {@link #setTolerance(double)}
     *            has not been called
     */
    public void adaptiveUpdate(double tincr)  throws IllegalStateException {
	if (tincr == 0.0) return;
	if (tol1 == 0.0 || tol2 == 0.0) {
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
		if (minLasth > lasth) minLasth = lasth;
		h -= (tKahan - tlimit);
		ty = h - tc;
		tKahan = t + ty;
		hNotModified = false;
		more = false;
	    } else if (tKahan == tlimit && needInit == false) {
		more = false;
	    }
	    double k1 = ty*yp;
	    double kp1 = ty*function(t,y, yp);
	    double k2 = ty*(yp + F1*kp1);
	    double kp2 = ty*function(t + F1*ty, y + F1 *k1, yp + F1*kp1);
	    double k3 = ty*(yp + F3*kp1 + F4*kp2);
	    double kp3 = ty*function(t + F2*ty,
				     y + F3*k1 + F4 *k2,
				     yp + F3*kp1 + F4*kp2);
	    double k4 = ty * (yp + F6*kp1 + F7*kp2 + F8*kp3);
	    double kp4 = ty*function(t + F5*ty,
				     y + F6*k1 + F7*k2 + F8*k3,
				     yp + F6*kp1 + F7*kp2 + F8*kp3);
	    double k5 = ty * (yp + F9*kp1 - 8.0*kp2 +F10*kp3 + F11*kp4);
	    double kp5 = ty*function(t + ty,
				     y + F9*k1 - 8.0*k2 +F10*k3 + F11*k4,
				    yp + F9*kp1 - 8.0*kp2 +F10*kp3 + F11*kp4);
	    double k6 = ty*(yp + F13*kp1 + 2.0*kp2 + F14*kp3 + F15*kp4
			    + F16*kp5);
	    double kp6 = ty*function(t + F12*ty,
				     y + F13*k1 + 2.0*k2 + F14*k3 + F15*k4
				     + F16*k5,
				    yp + F13*kp1 + 2.0*kp2 + F14*kp3 + F15*kp4
				    + F16*kp5);

	    double yincr1  = F17*k1 + F18*k3 + F19*k4 + F20*k5;
	    double yincr2 = F21*k1 + F22*k3 + F23*k4 + F24*k5 + F25*k6;
	    double ypincr1  = F17*kp1 + F18*kp3 + F19*kp4 + F20*kp5;
	    double ypincr2 = F21*kp1 + F22*kp3 + F23*kp4 + F24*kp5 + F25*kp6;
	    double tmp1 = Math.abs(yincr1-yincr2);
	    double tmp2 = Math.abs(ypincr1-ypincr2);
	    // double residual = Math.abs((yincr2 - yincr1)/ty);
	    double residual1 = tmp1/Math.abs(ty);
	    double residual2 = tmp2/Math.abs(ty);
	    double yincrDiff1 = 2.0 * tmp1;
	    double yincrDiff2 = 2.0 * tmp2;
	    double s41 = (yincrDiff1 == 0.0)? 1.0:
		tol1 * Math.abs(ty)/yincrDiff1;
	    double s42 = (yincrDiff2 == 0.0)? 1.0:
		tol2 * Math.abs(ty)/yincrDiff2;
	    double s4 = Math.min(s41, s42);
	    // double s = Math.pow(s4, 0.25);
	    double s = Math.sqrt(s4);
	    s = Math.sqrt(s);
	    h *= s;
	    if (residual1 > tol1 || residual2 > tol2) {
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
	    yy = ypincr1 - ypc;
	    yKahan = yp + yy;
	    ypc = (yKahan - yp) - yy;
	    yp = yKahan;

	    if (hNotModified) {
		lasth = Math.abs(h);
		if (minLasth > lasth) minLasth = lasth;
	    }
	}
	tcc = 0.0;
    }

    /**
     * Update the independent and dependent variables so that the parameter
     * will have a specified value.
     * The step size will be determined adaptively by this method.
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
     * Update the independent and dependent variables so that the parameter
     * will have a specified value and so that the step size
     * is a specified value or lower.
     * <P>
     * Note, regardless of the value of h, the maximum number of
     * steps used will be no greater than Integer.MAX_VALUE.
     * @param t the new value of the parameter
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

    private static class RVFRungeKutta extends RungeKutta2 {
	RealValuedFunctionThree f;

	@Override
	public void setInitialValues(double t0, double y0, double yp0)
	    throws IllegalArgumentException
	{
	    if (!f.isInDomain(t0, y0, yp0)) {
		throw new IllegalArgumentException
		    (errorMsg("notInDomainThree", t0, y0, yp0));
	    }
	    super.setInitialValues(t0, y0, yp0);
	}

	RVFRungeKutta (RealValuedFunctionThree f)
	    throws IllegalArgumentException
	{
	    super();
	    if (f.minArgLength() > 3 || f.maxArgLength() < 3) {
		throw new IllegalArgumentException(errorMsg("minArgLength"));
	    }
	    this.f = f;
	}

	RVFRungeKutta (RealValuedFunctionThree f, double t0, double y0,
			       double yp0)
	    throws IllegalArgumentException
	{
	    super();
	    if (f.minArgLength() > 3 || f.maxArgLength() > 3) {
		throw new IllegalArgumentException(errorMsg("minArgLength"));
	    }
	    this.f = f;
	    setInitialValues(t0, y0, yp0);
	}

	@Override
	protected final double function(double t, double y, double yp) {
	    return f.valueAt(t, y, yp);
	}
    }

    private static class RVFOpsRungeKutta extends RungeKutta2 {
	RealValuedFunctThreeOps f;

	@Override
	public void setInitialValues(double t0, double y0, double yp0)
	    throws IllegalArgumentException
	{
	    super.setInitialValues(t0, y0, yp0);
	}

	RVFOpsRungeKutta (RealValuedFunctThreeOps f)
	{
	    super();
	    this.f = f;
	}

	RVFOpsRungeKutta(RealValuedFunctThreeOps f, double t0, double y0,
			       double yp0)
	{
	    super();
	    this.f = f;
	    setInitialValues(t0, y0, yp0);
	}

	@Override
	protected final double function(double t, double y, double yp) {
	    return f.valueAt(t, y, yp);
	}
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionThree as its function.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the current value of the
     * Runge-Kutta algorithm's variable (y), and its third argument is
     * the derivative of the variable with respect to the parameter
     * (i.e,, dy/dt).
     * @param f the function, which must take three arguments
     */
    public static RungeKutta2 newInstance(RealValuedFunctionThree f)
    {
	return new RVFRungeKutta(f);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctionThree as its function, providing initial values.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the current value of the
     * Runge-Kutta algorithm's variable (y), and its third argument is
     * the derivative of the variable with respect to the parmaeter
     * (i.e,, dy/dt).
     * <P>
     * @param f the function, which must take exactly three arguments
     * @param t0 the initial value of the parameter with respect to which one
     *           differentiates
     * @param y0 the initial value of the variable for the specified parameter
     * @param yp0 the initial value of the derivative of the variable
     *            for the specified parameter (i.e., dy/dt evaluated at t0)
     * @exception IllegalArgumentException the function f cannot take two
     *            arguments
     */
    public static RungeKutta2 newInstance(RealValuedFunctionThree f,
					 double t0, double y0, double yp0) {
	return new RVFRungeKutta(f, t0, y0, yp0);
    }

    /**
     * Create a new instance of RungeKutta that uses an instance of
     * RealValuedFunctThreeOps as its function.
     * This function's first and only argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the initial value the
     * Runge-Kutta algorithm's variable (y), and its third argument is
     * the derivative of the variable with respect to the parmaeter
     * (i.e,, dy/dt).
     * <P>
     * The function may be a lambda expression.
     * @param f the function, which must take three arguments
     */
    public static RungeKutta2 newInstance(RealValuedFunctThreeOps f)
    {
	return new RVFOpsRungeKutta(f);
    }

    /**
     * Create a new instance of RungeKutta2 that uses an instance of
     * RealValuedFunctThreeOps as its function, providing initial values.
     * This function's first argument is the Runge-Kutta algorithm's
     * parameter (t) and its second argument is the current value of the
     * Runge-Kutta algorithm's variable (y), and its third argument is
     * the derivative of the variable with respect to the parmaeter
     * (i.e,, dy/dt).
     * <P>
     * The function may be a lambda expression.
     * @param f the function, which must take exactly three arguments
     * @param t0 the initial value of the parameter with respect to which one
     *           differentiates
     * @param y0 the initial value of the variable for the specified parameter
     * @param yp0 the initial value of the derivative of the variable
     *            for the specified parameter (i.e., dy/dt evaluated at t0)
     */
    public static RungeKutta2 newInstance(RealValuedFunctThreeOps f,
					 double t0, double y0, double yp0) {
	return new RVFOpsRungeKutta(f, t0, y0, yp0);
    }

}

//  LocalWords:  exbundle Runge Kutta getParam blockquote getValue th
//  LocalWords:  getDeriv getSecondDeriv adaptiveUpdate updateTo RK
//  LocalWords:  Fehlberg adaptively minStepSize setTolerance param
//  LocalWords:  RungeKutta newInstance RealValuedFunctionThree yp kp
//  LocalWords:  RealValuedFunctThreeOps applyFunction setParameters
//  LocalWords:  tmp tmpy tmpyp yc tc deriviative ty hh tincr tol
//  LocalWords:  adpativeUpdate IllegalArgumentException tolNotSet
//  LocalWords:  argNotPositive IllegalStateException yincr parmaeter
//  LocalWords:  secondArgPos notInDomainThree minArgLength
