package org.bzdev.math;
import java.util.Hashtable;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Implementation of Gauss-Legendre quadrature.
 * n-point Gaussian quadrature will produce exact results when
 * integrating a polynomial of degree (2n-1). It computes the
 * integral over the range [-1, 1] as a weighted sum of the
 * value of the integrand at points specified by the algorithm.
 * Thus, it is not suitable for integrating a function whose value
 * is known at (for example) evenly spaced points.  If, however, the
 * integrand can be computed at any desired point, Gaussian quadrature
 * requires the integrand be evaluated a relatively small number of
 * times.
 * <P>
 *  For the simplest case, one will create an anonymous inner
 * class implementing a method named <code>function</code> as follows:
 * <blockquote><pre><code>
 *      GLQuadrature glq = new GLQuadrature(3) {
 *        protected double function(double u) {
 *          return u*u + 3;
 *        }
 *      }
 * </CODE></PRE></blockquote>
 * To integrate, one then calls <code>glq.integrate(a, b)</code> where
 * <code>a</code> and <code>b</code> are the limits.  Alternatively one
 * may call <code>glq.integrate(a, b, m)</code> where m is the number of
 * subintervals on which to use Gauss-Legendre quadrature (this is not
 * necessary in the example above because Gauss-Legendre quadrature is
 * exact for polynomials of sufficiently low degree).
 * <P>
 * The type parameter P, if specified, can be used to provide a class
 * that will store the values of parameters used in computing the function
 * to be integrated.  These values can be set by calling
 * {@link #setParameters} and read by calling {@link #getParameters}.
 * Parameters may also be provided explicitly when an "integrate" method
 * is called. The two-argument "function" should be implemented in this
 * case:
 * <blockquote><pre><code>
 *      GLQuadrature&lt;Data&gt; glqp = new GLQuadrature&lt;Data&gt;(3) {
 *        protected double function(double u, Data data) {
 *          return u*u + data.value;
 *        }
 *      }
 * </CODE></PRE></blockquote>
 * When used, this allows one to define parameterized functions - the
 * parameters act as additional arguments that are typically constant during the
 * integration. A parameter is stored, not copied, so it should not be
 * modified while an integral is being computed (not an issue for
 * single-threaded programs). If a series of integrals are computed
 * for different values of the parameters, this allows one instance of
 * GLQuadrature to be used, rather than a separate object for each value.
 * An example of usage is:
 * <blockquote><pre><code>
 *     ...
 *     Data data;
 *     data.value = 3.0;
 *     glqp.setParameter(data);
 *     System.out.println(glqp.integrate(a, b));
 * </CODE></PRE></blockquote>
 * or
 * <blockquote><pre><code>
 *     ...
 *     Data data;
 *     data.value = 3.0;
 *     System.out.println(glqp.integrateWithP(a, b, data));
 * </CODE></PRE></blockquote>
 * <P>
 * In addition, the methods
 * {@link GLQuadrature#getArguments(double,double)} and
 * {@link GLQuadrature#integrate(double[])} can be used to precompute
 * function arguments in cases where those will be used repetitively
 * (e.g., for a series of integrals where the parameters change but not
 * the range or the number of points).  For example, consider the following
 * code:
 * <blockquote><pre><code>
 *       GLQuadrature&lt;Data&gt; glq = new GLQuadrature&lt;Data&gt;(3) {
 *          double[] u5cache = null;
 *          public double[] getArguments(double a, double b) {
 *             double[] results = super.getArguments(a, b);
 *             int n = results.length - 1;
 *             u5cache = new double[n];
 *             for (i = 0; i &lt; n; i++) {
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
 *       double[] args = glq.getArguments(a, b);
 *       Data data = new Data();
 *       glq.setParameters(data);
 *       for (double value: list) {
 *              data.value = value;
 *              System.our.println(glq.integrate(args));
 *       }
 * </CODE></PRE></blockquote>
 * As an optimization, the code caches the arguments raised to the
 * fifth power to reduce the number of multiplications needed, assuming
 * <code>list</code> contains multiple elements. This sort of optimization
 * would typically be used only in special cases where sufficiently
 * expensive terms in the function to be integrated can be precomputed.
 * <P>
 * Finally, the method {@link #newInstance} will construct an instance of
 * GLQuadrature where the function is represented as an instance of
 * {@link RealValuedFunction}. While the implementation is trivial in Java,
 * {@link #newInstance newInstance} simplifies the use of this class
 * from a scripting language.  For example,
 * <blockquote><pre><code>
 *         fs = {valueAt: function(u) {return Math.sin(u);}}
 *         rvf = new RealValuedFunction(scripting, fs);
 *         glq = GLQuadrature.newInstance(rvf, 16);
 *         integral = glq.integrate(0.0, Math.PI);
 * </CODE></PRE></blockquote>
 * shows how to use {@link #newInstance newInstance} with ECMAScript.
 * <P>
 * Note: some of the methods are named <code>integrate</code> while
 * those that use parameters explicitly are named <code>integrateWithP</code>.
 * While Java's type system treats primitive types and class types
 * differently, a scripting language may not.
 */
abstract public class GLQuadrature<P> {

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
     * @param t the function's argument
     * @return the value of the function given its argument
     */
    protected double function(double t) {
	return function(t, parameters);
    };

    /**
     * The function (with parameters) to integrate.
     * Typically, this function will be defined via an anonymous
     * subclass.  If the argument p is null, one will normally call
     * {@link #getParameters()} to obtain any parameters that were
     * configured via the {@link #setParameters} method. The
     * methods named integrateWithP will use this method.
     * @param t the function's argument
     * @param p the parameters
     * @return the value of the function given its arguments
     * @exception UnsupportedOperationException the method was needed
     *            but not implemented.
     */
    protected double function(double t, P p) {
	throw new UnsupportedOperationException(errorMsg("functionMissing"));
    }


    // Following values from "Table of the Zeros of the Legendre
    // polynomials of order 1--16 and the weight coefficients for
    // Gauss' mechanical quadrature formula," Arnold N. Lowan,
    // Norman Davids, and Arthur Levenson, October 25, 1941.
    // http://www-troja.fjfi.cvut.cz/~klimo/nm/c8/legpoly.pdf
    static final double x1[] = {0.0};
    static final double w1[] = {2.0};

    static final double x2[] = {0.577350269189626, 0.577350269189626};
    static final double w2[] = {1.0, 1.0};

    static final double x3[] = {
	-0.774596669241483,
	0.0,
	0.774596669241483
    };
    static final double w3[] = {
	0.555555555555556,
	0.888888888888889,
	0.555555555555556
    };

    static final double x4[] = {
	- 0.861136311594053,
	-0.339981043584856,
	0.339981043584856,
	0.861136311594053
    };
    static final double w4[] = {
	0.347854845137454,
	0.652145154862546,
	0.652145154862546,
	0.347854845137454
    };

    static final double x5[] = {
	-0.906179845938664,
	-0.538469310105683,
	0.0,
	0.538469310105683,
	0.906179845938664
    };
    static final double w5[] = {
	0.236926885056189,
	0.478628670499366,
	0.568888888888889,
	0.478628670499366,
	0.236926885056189
    };

    static final double x6[] = {
	-0.932469514203152,
	-0.661209386466265,
	-0.238619186083197,
	0.238619186083197,
	0.661209386466265,
	0.93246951420315
    };
    static final double w6[] = {
	0.171324492379170,
	0.360761573048139,
	0.467913934572691,
	0.467913934572691,
	0.360761573048139,
	0.171324492379170
    };
    
    static final double x7[] = {
	-0.949107912342759,
	-0.741531185599394,
	-0.405845151377397,
	0.0,
	0.405845151377397,
	0.741531185599394,
	0.949107912342759	
    };
    static final double w7[] = {
	0.129484966168870,
	0.279705391489277,
	0.381830050505119,
	0.417959183673469,
	0.381830050505119,
	0.279705391489277,
	0.129484966168870,
    };

    static final double x8[] = {
	-0.960289856497536,
	-0.796666477413627,
	-0.525532409916329,
	-0.183434642495650,
	0.183434642495650,
	0.525532409916329,
	0.79666647741362,
	0.960289856497536
    };
    static final double w8[] = {
	0.101228536290376,
	0.222381034453374,
	0.313706645877887,
	0.362683783378362,
	0.362683783378362,
	0.313706645877887,
	0.222381034453374,
	0.101228536290376
    };

    static final double x9[] = {
	-0.968160239507626,
	-0.836031107326636,
	-0.613371432700590,
	-0.324253423403809,
	0.000000000000000,
	0.324253423403809,
	0.613371432700590,
	0.836031107326636,
	0.968160239507626
    };
    static final double w9[] = {
	0.081274388361574,
	0.180648160694857,
	0.260610696402935,
	0.312347077040003,
	0.330239355001260,
	0.312347077040003,
	0.260610696402935,
	0.180648160694857,
	0.081274388361574
    };

    static final double x10[] = {
	-0.973906528517172,
	-0.865063366688985,
	-0.679409568299024,
	-0.433395394129247,
	-0.148874338981631,
	0.148874338981631,
	0.433395394129247,
	0.679409568299024,
	0.865063366688985,
	0.973906528517172
    };
    static final double w10[] = {
	0.066671344308688,
	0.149451349150581,
	0.219086362515982,
	0.269266719309996,
	0.295524224714753,
	0.295524224714753,
	0.269266719309996,
	0.219086362515982,
	0.149451349150581,
	0.066671344308688
    };

    static final double x11[] = {
	-0.978228658146057,
	-0.887062599768095,  
	-0.730152005574049, 
	-0.519096129110681,
	-0.269543155952345,
	0.000000000000000,
	0.269543155952345,
	0.519096129110681,
	0.730152005574049, 
	0.887062599768095,  
	0.978228658146057
    };
    static final double w11[] = {
	0.055668567116174,
	0.125580369464905,
	0.186290210927734,
	0.233193764591990,
	0.262804544510247,
	0.272925086777901,
	0.262804544510247,
	0.233193764591990,
	0.186290210927734,
	0.125580369464905,
	0.055668567116174,
    };



    static final double x12[] = {
	-0.981560634246719,
	-0.904117256370475,
	-0.769902674194305,
	-0.587317954286617,
	-0.367831498918180,
	-0.125333408511469,
	0.125333408511469,
	0.367831498918180,
	0.587317954286617,
	0.769902674194305,
	0.904117256370475,
	0.981560634246719
    };
    static final double w12[] = {
	0.047175336386512,
	0.106939325995318,
	0.160078328543346,
	0.203167426723066,
	0.233492536538355,
	0.249147045813403,
	0.249147045813403,
	0.233492536538355,
	0.203167426723066,
	0.160078328543346,
	0.106939325995318,
	0.047175336386512
    };

    static final double x13[] = {
	-0.984183054718588,
	-0.917598399222978,
	-0.801578090733310,
	-0.642349339440340,
	-0.448492751036447,
	-0.230458315955135,
	0.000000000000000,
	0.230458315955135,
	0.448492751036447,
	0.642349339440340,
	0.801578090733310,
	0.917598399222978,
	0.984183054718588
    };
    static final double w13[] = {
	0.040484004765316,
	0.092121499837728,
	0.138873510219787,
	0.178145980761946,
	0.207816047536889,
	0.226283180262897,
	0.232551553230874,
	0.226283180262897,
	0.207816047536889,
	0.178145980761946,
	0.138873510219787,
	0.092121499837728,
	0.040484004765316
    };

    static final double x14[] = {
	-0.986283808696812,
	-0.928434883663574,
	-0.827201315069765,
	-0.687292904811685,
	-0.515248636358154,
	-0.319112368927890,
	-0.108054948707344,
	0.108054948707344,
	0.319112368927890,
	0.515248636358154,
	0.687292904811685,
	0.827201315069765,
	0.928434883663574,
	0.986283808696812
    };
    static final double w14[] = {
	0.035119460331752,
	0.080158087159760,
	0.121518570687903,
	0.157203167158194,
	0.185538397477938,
	0.205198463721290,
	0.215263853463158,
	0.215263853463158,
	0.205198463721290,
	0.185538397477938,
	0.157203167158194,
	0.121518570687903,
	0.080158087159760,
	0.035119460331752
    };

    static final double x15[] = {
	-0.987992518020485,
	-0.937273392400706,
	-0.848206583410427,
	-0.724417731360170,
	-0.570972172608539,
	-0.394151347077563,
	-0.201194093997435,
	0.000000000000000,
	0.201194093997435,
	0.394151347077563,
	0.570972172608539,
	0.724417731360170,
	0.848206583410427,
	0.937273392400706,
	0.987992518020485
    };
    static final double w15[] = {
	0.030753241996117,
	0.070366047488108,
	0.107159220467172,
	0.139570677926154,
	0.166269205816994,
	0.186161000015562,
	0.198431485327111,
	0.202578241925561,
	0.198431485327111,
	0.186161000015562,
	0.166269205816994,
	0.139570677926154,
	0.107159220467172,
	0.070366047488108,
	0.030753241996117
    };

    static final double x16[] = {
	-0.989400934991650,
	-0.944575023073233,
	-0.865631202387832,
	-0.755404408355003,
	-0.617876244402644,
	-0.458016777657227,
	-0.281603550779259,
	-0.095012509837637,
	0.095012509837637,
	0.281603550779259,
	0.458016777657227,
	0.617876244402644,
	0.755404408355003,
	0.865631202387832,
	0.944575023073233,
	0.989400934991650
    };
    static final double w16[] = {
	0.027152459411754,
	0.062253523938648,
	0.095158511682493,
	0.124628971255534,
	0.149595988816577,
	0.169156519395003,
	0.182603415044924,
	0.189450610455069,
	0.189450610455069,
	0.182603415044924,
	0.169156519395003,
	0.149595988816577,
	0.124628971255534,
	0.095158511682493,
	0.062253523938648,
	0.027152459411754
    };

    static final double[] xs[] = {null, x1,
				  x2, x3, x4, x5, x6, x7, x8, x9, x10,
				  x11, x12, x13, x14, x15, x16};
    static final double[] ws[] = {null, w1,
		     w2, w3, w4, w5, w6, w7, w8, w9, w10,
		     w11, w12, w13, w14, w15, w16};
    int n;
    double[] x;
    double[] w;

    /**
     * Get the number of points for which a function is evaluated
     * @return the number of points
     */
    public int getNumberOfPoints() {
	return n;
    }

    static Hashtable<Integer,double[]> xtbl = new Hashtable<Integer,double[]>();
    static Hashtable<Integer,double[]> wtbl = new Hashtable<Integer,double[]>();

    /**
     * Constructor.
     * Note: to determine weights used in the integration, the roots
     * of Legendre polynomials or order n have to be calculated, and
     * the accuracy decreases for large values of n: for n = 1000, for
     * example, the value at a computed root may be around 10^-11
     * whereas for small values of n, it is around 10^-15, so the accuracy
     * of the weights decreases for very large values of n. As a result,
     * increasing n to excessively large values can be counterproductive
     * beyond merely increasing computation time needlessly.
     * @param n the number of points to use
     * @exception IllegalArgumentException n is less than 1
     */
    public GLQuadrature(int n) {
	if (n < 1) throw new IllegalArgumentException
		       (errorMsg("argOutOfRangeI", n));
	this.n = n;
	if (n > 16)  {
	    synchronized(xtbl) {
		x = xtbl.get(n);
		w = wtbl.get(n);
		if (x == null || w == null) {
		    x = new double[n];
		    w = new double[n];
		    Functions.LegendrePolynomial.roots(n, x);
		    for (int i = 0; i < n; i++) {
			double deriv = Functions.dPdx(n, x[i]);
			w[i] = 2.0 / ((1.0 - x[i]*x[i])*deriv*deriv);
		    }
		    xtbl.put(n, x);
		    wtbl.put(n, w);
		}
	    }
	} else {
	    x = xs[n];
	    w = ws[n];
	}
	/*
	System.out.println("created " + n +", " + x.length 
			   + ", " + w.length);
	*/
    }
    
    /**
     * Integrate the function.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @return the definite integral from a to b
     */
    public double integrate(double a, double b) {
	double sum = 0.0;
	double halfRange = (b - a)/2.0;
	double mean = (a + b)/2.0;
	double c = 0.0;
	for (int i = 0; i < n; i++) {
	    double u = halfRange * x[i] + mean;
	    double y = (w[i]*function(u)) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return halfRange * sum;
    }

    /**
     * Integrate the function, accumulating the integral in an adder.
     * @param adder the adder used to accumulate integrals
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     */
    public void integrate(Adder adder, double a, double b) {
	if (adder instanceof Adder.Kahan) {
	    Adder.Kahan kadder = (Adder.Kahan) adder;
	    double halfRange = (b - a)/2.0;
	    double mean = (a + b)/2.0;
	    // double sum = 0.0;
	    // double c = 0.0;
	    Adder.Kahan.State state = kadder.getState();
	    for (int i = 0; i < n; i++) {
		double u = halfRange * x[i] + mean;
		double y = (halfRange*w[i]*function(u)) - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	} else {
	    adder.add(integrate(a, b));
	}
    }

    /**
     * Integrate the function with explicit parameters.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param p the parameters
     * @return the definite integral from a to b
     */
    public double integrateWithP(double a, double b, P p) {
	double sum = 0.0;
	double halfRange = (b - a)/2.0;
	double mean = (a + b)/2.0;
	double c = 0.0;
	for (int i = 0; i < n; i++) {
	    double u = halfRange * x[i] + mean;
	    double y =  (w[i]*function(u, p)) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return halfRange * sum;
    }

    /**
     * Integrate the function with explicit parameters, accumulating
     * the integral in an adder.
     * @param adder the adder
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param p the parameters
     */
    public void integrateWithP(Adder adder, double a, double b, P p) {
	if (adder instanceof Adder.Kahan) {
	    Adder.Kahan kadder = (Adder.Kahan)adder;
	    double halfRange = (b - a)/2.0;
	    if (halfRange == 0.0) return;
	    double mean = (a + b)/2.0;
	    // double sum = 0.0;
	    // double c = 0.0;
	    Adder.Kahan.State state = kadder.getState();
	    for (int i = 0; i < n; i++) {
		double u = halfRange * x[i] + mean;
		double y =  (halfRange*w[i]*function(u, p)) - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	} else {
	    adder.add(integrateWithP(a, b, p));
	}
    }


    /**
     * Set up data for an integration.
     * This method is used in conjunction with
     * {@link #integrate(double[])} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @return an array of values at which the function should
     *         be evaluated, with the last element containing
     *         half the range of integration (the array length is
     *         one larger than the number of points provided to
     *         the constructor)
     * @see #integrate(double[])
     */
    public double[] getArguments(double a, double b) {
	double halfRange = (b - a)/2.0;
	double mean = (a + b)/2.0;
	double[] arguments = new double[n+1];
	for (int i = 0; i < n; i++) {
	    double u = halfRange * x[i] + mean;
	    arguments[i] = u;
	}
	arguments[n] = halfRange;
	return arguments;
    }

    /**
     * Set up data for an integration using an Gauss Legendre quadrature
     * with a specified number of points.
     * This method is used in conjunction with
     * {@link #integrate(double[])} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters.  The caller must ensure that the value passed
     * as this method's final argument matches the value used in the
     * constructor for the GLQuadrature instance used.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param n the number of points at which the function will
     *          be evaluated
     * @return an array of values at which the function should
     *         be evaluated, with the last element containing
     *         half the range of integration (the array length is
     *         one larger than the number of points provided to
     *         the constructor)
     * @exception IllegalArgumentException n is less than 1
     * @see #integrate(double[])
     */
    public static double[] getArguments(double a, double b, int n) {
	if (n < 1) throw new IllegalArgumentException
		       (errorMsg("argOutOfRangeI", n));
	double halfRange = (b - a)/2.0;
	double mean = (a + b)/2.0;
	double[] arguments = new double[n+1];
	double[] x = null;
	double[] w = null;
	if (n > 16)  {
	    synchronized(xtbl) {
		x = xtbl.get(n);
		w = wtbl.get(n);
		if (x == null || w == null) {
		    x = new double[n];
		    w = new double[n];
		    Functions.LegendrePolynomial.roots(n, x);
		    for (int i = 0; i < n; i++) {
			double deriv = Functions.dPdx(n, x[i]);
			w[i] = 2.0 / ((1.0 - x[i]*x[i])*deriv*deriv);
		    }
		    xtbl.put(n, x);
		    wtbl.put(n, w);
		}
	    }
	} else {
	    x = xs[n];
	}
	for (int i = 0; i < n; i++) {
	    double u = halfRange * x[i] + mean;
	    arguments[i] = u;
	}
	arguments[n] = halfRange;
	return arguments;
    }

    /**
     * Get weights for explicit integration.
     * To compute an integral from a to b explicitly (e.g., without
     * creating an instance of GLQuadrature), one can use the following
     * sequence of statements:
     * <blockquote><pre>
     *     double[] arguments = GLQuadrature.getArguments(a, b, n);
     *     double[] weights = GLQuadrature.getWeights(a, b, n);
     *     Adder adder = new Adder.Kahan();
     *     for (int i = 0; i &lt; n; i++) {
     *          double u = arguments[i];
     *          ...
     *          y =  [ an expression that depends on u ] ;
     *          adder.add(weights[i]*y]));
     *     }
     *     integral = adder.getSum();
     * </pre></blockquote>
     * The weights returned by this method include a scaling
     * factor for making the integral go from a to b
     * instead of -1 to 1.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param n the number of points at which a function will
     * @return an array of weights
     * @exception IllegalArgumentException n is less than 1
     *          be evaluated.
     */
    public static double[] getWeights(double a, double b, int n) {
	if (n < 1) throw new IllegalArgumentException
		       (errorMsg("argOutOfRangeI", n));
	double halfRange = (b - a)/2.0;
	double[] w = null;
	if (n > 16)  {
	    synchronized(xtbl) {
		double[] x = xtbl.get(n);
		w = wtbl.get(n);
		if (x == null || w == null) {
		    x = new double[n];
		    w = new double[n];
		    Functions.LegendrePolynomial.roots(n, x);
		    for (int i = 0; i < n; i++) {
			double deriv = Functions.dPdx(n, x[i]);
			w[i] = 2.0 / ((1.0 - x[i]*x[i])*deriv*deriv);
		    }
		    xtbl.put(n, x);
		    wtbl.put(n, w);
		}
	    }
	} else {
	    w = ws[n];
	}
	double[] results = new double[n];
	for (int i = 0; i < n; i++) {
	    results[i] = w[i]*halfRange;
	}
	return results;
    }


    /**
     * Integrate a function using precomputed arguments.
     * This method is used in conjunction with
     * {@link #getArguments(double,double)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param arguments the array returned by
     *        {@link #getArguments(double,double) getArguments(a,b)}
     * @return the definite integral from a to b
     * @see #getArguments(double,double)
     */
    public double integrate(double[] arguments) {
	double sum = 0;
	double c = 0.0;
	for (int i = 0; i < n; i++) {
	    double y = (w[i]*function(arguments[i])) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return arguments[n]*sum;
    }

    /**
     * Integrate a function using precomputed arguments, accumulating
     * the integral in an adder.
     * This method is used in conjunction with
     * {@link #getArguments(double,double)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param adder the adder
     * @param arguments the array returned by
     *        {@link #getArguments(double,double) getArguments(a,b)}
     * @see #getArguments(double,double)
     */
    public void integrate(Adder adder, double[] arguments) {
	if (adder instanceof Adder.Kahan) {
	    Adder.Kahan kadder = (Adder.Kahan) adder;
	    Adder.Kahan.State state = kadder.getState();
	    // double sum = 0;
	    // double c = 0.0;
	    double factor = arguments[n];
	    for (int i = 0; i < n; i++) {
		double y = (factor*w[i]*function(arguments[i])) - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	} else {
	    adder.add(integrate(arguments));
	}
    }

    /**
     * Integrate a function using precomputed arguments with parameters.
     * This method is used in conjunction with
     * {@link #getArguments(double,double)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param arguments the array returned by
     *        {@link #getArguments(double,double) getArguments(a,b)}
     * @param p the parameters
     * @return the definite integral from a to b
     * @see #getArguments(double,double)
     */
    public double integrateWithP(double[] arguments, P p) {
	double sum = 0;
	double c = 0.0;
	for (int i = 0; i < n; i++) {
	    double y = (w[i]*function(arguments[i], p)) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return arguments[n]*sum;
    }

    /**
     * Integrate a function using precomputed arguments with parameters,
     * accumulating the integral in an adder.
     * This method is used in conjunction with
     * {@link #getArguments(double,double)} for cases where a function
     * may be integrated repeatedly over the same range, but with
     * varying parameters. It is provided as an optimization for
     * a special case.
     * @param adder the adder
     * @param arguments the array returned by
     *        {@link #getArguments(double,double) getArguments(a,b)}
     * @param p the parameters
     * @see #getArguments(double,double)
     */
    public void integrateWithP(Adder adder, double[] arguments, P p) {
	if (adder instanceof Adder.Kahan) {
	    Adder.Kahan kadder = (Adder.Kahan) adder;
	    Adder.Kahan.State state = kadder.getState();
	    double factor = arguments[n];
	    for (int i = 0; i < n; i++) {
		double y = (factor*w[i]*function(arguments[i], p)) - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	} else {
	    adder.add(integrateWithP(arguments, p));
	}
    }


    /**
     * Integrate the function using multiple subintervals.
     * The interval [a,b] is divided into m intervals, each
     * of which is integrated separately and summed.  This is useful
     * when the argument n passed to the constructor is not very large
     * due to numerical accuracy issues in computing the values
     * at a particular point for Legendre polynomials with a very
     * high degree.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param m the number of subintervals
     * @return the definite integral from a to b
     */
    public double integrate(double a, double b, int m) {
	double sum = integrate(a, a + (b-a)/(double)m);
	double c = 0.0;
	int mm1 = m-1;
	for (int i = 1; i < mm1; i++) {
	    double aa = a + i * (b-a)/((double) m);
	    double bb = a + (i+1)*(b-a)/((double) m);
	    double y = integrate(aa, bb) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	if (m > 1) {
	    sum += integrate(a + (m-1)*(b-a)/((double) m),b) - c;
	}
	return sum;
    }

    /**
     * Integrate the function using multiple subintervals, accumulating
     * the integral in an adder.
     * The interval [a,b] is divided into m intervals, each
     * of which is integrated separately and summed.  This is useful
     * when the argument n passed to the constructor is very large
     * due to numerical accuracy issues in computing the values
     * at a particular point for Legendre polynomials with a very
     * high degree.
     * @param adder the adder
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param m the number of subintervals
     */
    public void integrate(Adder adder, double a, double b, int m) {
	integrate(adder, a, a + (b-a)/(double)m);
	int mm1 = m-1;
	for (int i = 1; i < mm1; i++) {
	    double aa = a + i * (b-a)/((double) m);
	    double bb = a + (i+1)*(b-a)/((double) m);
	    integrate(adder, aa, bb);
	}
	if (m > 1) {
	    integrate(adder, a + (m-1)*(b-a)/((double) m),b);
	}
    }

    /**
     * Integrate the function with explicit parameters using multiple
     * subintervals.
     * The interval [a,b] is divided into m intervals, each
     * of which is integrated separately and summed.  This is useful
     * when the argument n passed to the constructor is not very large
     * due to numerical accuracy issues in computing the values
     * at a particular point for Legendre polynomials with a very
     * high degree.
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param m the number of subintervals
     * @param p the parameters
     * @return the definite integral from a to b
     */
    public double integrateWithP(double a, double b, int m, P p) {
	double sum = integrateWithP(a, a + (b-a)/(double)m, p);
	double c = 0.0;
	int mm1 = m-1;
	for (int i = 1; i < mm1; i++) {
	    double aa = a + i * (b-a)/((double) m);
	    double bb = a + (i+1)*(b-a)/((double) m);
	    double y = integrateWithP(aa, bb, p) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	if (m > 1) {
	    sum += integrateWithP(a + (m-1)*(b-a)/((double) m),b, p) - c;
	}
	return sum;
    }


    /**
     * Integrate the function with explicit parameters using multiple
     * subintervals, accumulating the integral in an adder.
     * The interval [a,b] is divided into m intervals, each
     * of which is integrated separately and summed.  This is useful
     * when the argument n passed to the constructor is very large
     * due to numerical accuracy issues in computing the values
     * at a particular point for Legendre polynomials with a very
     * high degree.
     * @param adder the adder
     * @param a the lower limit of integration
     * @param b the upper limit of integration
     * @param m the number of subintervals
     * @param p the parameters
     */
    public void integrateWithP(Adder adder, double a, double b, int m, P p) {
	integrateWithP(adder, a, a + (b-a)/(double)m, p);
	int mm1 = m-1;
	for (int i = 1; i < mm1; i++) {
	    double aa = a + i * (b-a)/((double) m);
	    double bb = a + (i+1)*(b-a)/((double) m);
	    integrateWithP(adder, aa, bb, p);
	}
	if (m > 1) {
	    integrateWithP(adder, a + (m-1)*(b-a)/((double) m),b, p);
	}
    }

    static class RVFGLQuadrature extends GLQuadrature {
	RealValuedFunctOps f;
	public  RVFGLQuadrature (RealValuedFunctOps f, int n) {
	    super(n);
	    this.f = f;
	}
	protected double function(double t) {
	    return f.valueAt(t);
	}
    }

    /**
     * Create a new instance of GLQuadrature that uses an instance of
     * RealValuedFunction or RealValueFunctOps as its function.
     * @param f the function
     * @param n the number of points to use
     * @return a new instance of GLQuadrature
     * @exception IllegalArgumentException n is less than 1
     */
    public static GLQuadrature newInstance(RealValuedFunctOps f, int n) {
	return new RVFGLQuadrature(f, n);
    }
}

//  LocalWords:  exbundle integrand blockquote pre GLQuadrature glq
//  LocalWords:  subintervals setParameters getParameters glqp lt uu
//  LocalWords:  setParameter integrateWithP getArguments precompute
//  LocalWords:  args precomputed newInstance RealValuedFunction fs
//  LocalWords:  valueAt rvf UnsupportedOperationException Lowan
//  LocalWords:  functionMissing Levenson IllegalArgumentException
//  LocalWords:  argOutOfRangeI getWeights Kahan getSum
//  LocalWords:  RealValueFunctOps
