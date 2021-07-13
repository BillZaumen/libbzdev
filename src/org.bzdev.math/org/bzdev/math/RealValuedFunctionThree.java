package org.bzdev.math;
import org.bzdev.scripting.ScriptingContext;
import javax.script.ScriptException;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class  defining a real-valued function with three real arguments.
 * This is intended for cases in which a function should be passed
 * as an argument.
 * <P>
 * A subclass will typically override one or more of the methods
 * valueAt, deriv1At, deriv2At, deriv3At, deriv11At, deriv12At, deriv13At,
 * deriv21At, deriv22At, deriv23At, deriv31At, deriv32At, and deriv33At
 * to provide the values for a function and its first and
 * second partial derivative.  For any that are not available, an
 * UnsupportedOperationException will be thrown.
 * <P>
 * The class also provides scripting-language support. If a Scripting
 * context is named <code>scripting</code>, the following EMCAScript
 * code will implement a function and its derivatives:
 * <blockquote><code><pre>
 *     importClass(org.bzdev.RealValuedFunctionThree);
 *     ....
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunctionThree as its argument.
 *     funct = new RealValuedFunctionThree(scripting,
 *               {valueAt: function(x,y,z) {return Math.sin(x)*Math.cos(y)*z;},
 *                deriv1At: function(x,y,z) {return Math.cos(x)*Math.cos(y)*z;},
 *                deriv2At: function(x,y,z) {
 *                   return -Math.sin(x)*Math.sin(y)*z;
 *                },
 *                deriv3At: function(x,y,z) {return Math.sin(x) * Math.cos(y);},
 *                deriv11At: function(x,y,z) {
 *                    return -Math.sin(x) * Math.cos(y) * z;
 *                },
 *                deriv12At: function(x,y,z) {
 *                    return -Math.cos(x) * Math.sin(y) * z;
 *                },
 *                deriv13At: function(x,y,z) {return Math.cos(x)*Math.sin(y);},
 *                deriv21At: function(x,y,z) {
 *                    return -Math.cos(x) * Math.sin(y) * z;
 *                },
 *                deriv22At: function(x,y,z) {
 *                    return -Math.sin(x) * Math.cos(y) * z;}
 *                deriv23At: function(x,y,z) {
 *                    return -Math.sin(x) * Math.sin(y);
 *                }
 *                deriv31At: function(x,y,z) {
 *                    return Math.cos(x) * Math.cos(y);
 *                },
 *                deriv32At: function(x,y,z) {
 *                    return -Math.sin(x) * Math.sin(y);
 *                },
 *                deriv33At: function(x,y,z) {return 0.0;}
 *               };
 *     ourObject.setFunction(funct);
 * </pre></code></blockquote>
 * Alternatively, one may use the following code where the functions
 * defining the derivatives are provided by name:
 * <blockquote><code><pre>
 *     importClass(org.bzdev.RealValuedFunctionThree);
 *     ...
 *     function f(x,y,z) {return Math.sin(x) * Math.cos(y) * z;}
 *     function f1(x,y,z) {return Math.cos(x) * Math.cos(y) * z;}
 *     function f2(x,y,z) {return -Math.sin(x) * Math.sin(y) * z;}
 *     function f3(x,y,z) {return Math.sin(x) * Math.cos(y);}
 *     function f11(x,y,z) {return -Math.sin(x) * Math.cos(y) * z;}
 *     function f12(x,y,z) {return  -Math.cos(x) * Math.sin(y) * z;}
 *     function f13(x,y,z) {return  Math.cos(x) * Math.cos(y);}
 *     function f21(x,y,z) {return -Math.cos(x) * Math.sin(y) * z;}
 *     function f22(x,y,z) {return -Math.sin(x) * Math.cos(y) * z;}
 *     function f23(x,y,z) {return -Math.sin(x) * Math.sin(y);}
 *     function f31(x,y,z) {return Math.cos(x) * Math.cos(y)}
 *     function f32(x,y,z) {return  -Math.sin(x) * Math.sin(y);}
 *     function f33(x,y,z) {return  0.0;}
 *     ...
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunction as its argument.
 *     funct = new RealValuedFunction(scripting, "f", "f1", "f2", "f3"
                                      "f11", "f12", "f13", 
				      "f21", "f22", "f23",
                                      "f31", "f32", "f33");
 *     ourObject.setFunction(funct);
 * </pre></code></blockquote>
 */
public class  RealValuedFunctionThree extends RealValuedFunctionVA
    implements RealValuedFunctThreeOps
{

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private ScriptingContext context;
    // script object
    private Object object;
    // function names
    private String fname = null;
    private String f1name = null;
    private String f2name = null;
    private String f3name = null;
    private String f11name = null;
    private String f12name = null;
    private String f13name = null;
    private String f21name = null;
    private String f22name = null;
    private String f23name = null;
    private String f31name = null;
    private String f32name = null;
    private String f33name = null;

    // function interfaces
    private RealValuedFunctThreeOps function;
    private RealValuedFunctThreeOps function1;
    private RealValuedFunctThreeOps function2;
    private RealValuedFunctThreeOps function3;
    private RealValuedFunctThreeOps function11;
    private RealValuedFunctThreeOps function12;
    private RealValuedFunctThreeOps function13;
    private RealValuedFunctThreeOps function21;
    private RealValuedFunctThreeOps function22;
    private RealValuedFunctThreeOps function23;
    private RealValuedFunctThreeOps function31;
    private RealValuedFunctThreeOps function32;
    private RealValuedFunctThreeOps function33;

    /**
     * Constructor.
     */
    public RealValuedFunctionThree() {
	super(3,3);
    }

    /**
     * Constructor given a function.
     * The argument implements {@link RealValuedFunctThreeOps} and
     * can be a lambda expression  with three arguments. The interface
     * {@link RealValuedFunctThreeOps} provides a single method:
     * {@link RealValuedFunctThreeOps#valueAt(double,double,double)} that, when
     * called, provides the function's value.
     * @param function the function
     */
    public RealValuedFunctionThree(RealValuedFunctThreeOps function) {
	super(3,3);
	this.function = function;
    }

    /**
     * Constructor given a function  and its first partial derivatives.
     * The arguments implement {@link RealValuedFunctThreeOps} and
     * can be a lambda expression  with three arguments. The interface
     * {@link RealValuedFunctThreeOps} provides a single method:
     * {@link RealValuedFunctThreeOps#valueAt(double,double,double)} that, when
     * called, provides the function's value.
     * @param function the function; null if not provided
     * @param function1 the function that provides the value for the
     *        {@link #deriv1At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function2 the function that provides the value for the
     *        {@link #deriv2At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function3 the function that provides the value for the
     *        {@link #deriv3At(double,double,double)}; null if this
     *        partial derivative is not defined.
     *
     */
    public RealValuedFunctionThree(RealValuedFunctThreeOps function,
				   RealValuedFunctThreeOps function1,
				   RealValuedFunctThreeOps function2,
				   RealValuedFunctThreeOps function3)
    {
	this(function);
	this.function1 = function1;
	this.function2 = function2;
	this.function3 = function3;
    }


    /**
     * Constructor given a function  and its first and second
     * partial derivatives.
     * The arguments implement {@link RealValuedFunctThreeOps} and
     * can be a lambda expression  with three arguments. The interface
     * {@link RealValuedFunctThreeOps} provides a single method:
     * {@link RealValuedFunctThreeOps#valueAt(double,double,double)} that, when
     * called, provides the function's value.
     * @param function the function; null if not provided
     * @param function1 the function that provides the value for the
     *        {@link #deriv1At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function2 the function that provides the value for the
     *        {@link #deriv2At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function3 the function that provides the value for the
     *        {@link #deriv3At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function11 the function that provides the value for the
     *        {@link #deriv11At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function12 the function that provides the value for the
     *        {@link #deriv21At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function13 the function that provides the value for the
     *        {@link #deriv13At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function21 the function that provides the value for the
     *        {@link #deriv21At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function22 the function that provides the value for the
     *        {@link #deriv22At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function23 the function that provides the value for the
     *        {@link #deriv23At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function31 the function that provides the value for the
     *        {@link #deriv31At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function32 the function that provides the value for the
     *        {@link #deriv32At(double,double,double)}; null if this
     *        partial derivative is not defined.
     * @param function33 the function that provides the value for the
     *        {@link #deriv33At(double,double,double)}; null if this
     *        partial derivative is not defined.
     *
     */
    public RealValuedFunctionThree(RealValuedFunctThreeOps function,
				   RealValuedFunctThreeOps function1,
				   RealValuedFunctThreeOps function2,
				   RealValuedFunctThreeOps function3,
				   RealValuedFunctThreeOps function11,
				   RealValuedFunctThreeOps function12,
				   RealValuedFunctThreeOps function13,
				   RealValuedFunctThreeOps function21,
				   RealValuedFunctThreeOps function22,
				   RealValuedFunctThreeOps function23,
				   RealValuedFunctThreeOps function31,
				   RealValuedFunctThreeOps function32,
				   RealValuedFunctThreeOps function33)
    {
	this(function, function1, function2, function3);
	this.function11 = function11;
	this.function12 = function12;
	this.function13 = function13;
	this.function21 = function21;
	this.function22 = function22;
	this.function23 = function23;
	this.function31 = function31;
	this.function32 = function32;
	this.function33 = function33;
    }

    @Override
    public final double getDomainMin(int i) throws IllegalArgumentException,
						   IllegalStateException
    {
	if (i == 0) {
	    return getDomainMin1();
	} else if (i == 1) {
	    return getDomainMin2();
	} else if (i == 2) {
	    return getDomainMin3();
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    @Override
    public final double getDomainMax(int i) throws IllegalArgumentException,
						   IllegalStateException
    {
	if (i == 0) {
	    return getDomainMax1();
	} else if (i == 1) {
	    return getDomainMax2();
	} else if (i == 2) {
	    return getDomainMax3();
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    @Override
    public final boolean domainMinClosed(int i)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i == 0) {
	    return domainMin1Closed();
	} else if (i == 1) {
	    return domainMin2Closed();
	} else if (i == 2) {
	    return domainMin3Closed();
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    @Override
    public final boolean domainMaxClosed(int i)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i != 0) {
	}
	if (i == 0) {
	    return domainMax1Closed();
	} else if (i == 1) {
	    return domainMax2Closed();
	} else if (i == 2) {
	    return domainMax3Closed();
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    /**
     * Get the minimum value of the first argument in the domain of
     * the function.
     * @return the minimum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMin1() throws IllegalStateException {
	return -Double.MAX_VALUE;
    }

    /**
     * Get the maximum value of the first argument in the domain of
     * the function.
     * @return the maximum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMax1() throws IllegalStateException {
	return Double.MAX_VALUE;
    }

    /**
     * Get the minimum value of the second argument in the domain of
     * the function.
     * @return the minimum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMin2() throws IllegalStateException {
	return -Double.MAX_VALUE;
    }

    /**
     * Get the maximum value of the second argument in the domain of
     * the function.
     * @return the maximum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMax2() throws IllegalStateException {
	return Double.MAX_VALUE;
    }

    /**
     * Get the minimum value of the third argument in the domain of
     * the function.
     * @return the minimum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMin3() throws IllegalStateException {
	return -Double.MAX_VALUE;
    }

    /**
     * Get the maximum value of the third argument in the domain of
     * the function.
     * @return the maximum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMax3() throws IllegalStateException {
	return Double.MAX_VALUE;
    }

    /**
     * Determine if the domain minimum for the first argument is in the domain.
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMin1Closed() throws IllegalStateException {
	return true;
    }

    /**
     * Determine if the domain maximum for the first argument is in the domain.
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMax1Closed() throws IllegalStateException {
	return true;
    }

    /**
     * Determine if the domain minimum for the second argument is in the domain.
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMin2Closed() throws IllegalStateException {
	return true;
    }

    /**
     * Determine if the domain maximum for the second argument is in the domain.
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMax2Closed() throws IllegalStateException {
	return true;
    }

    /**
     * Determine if the domain minimum for the third argument is in the domain.
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMin3Closed() throws IllegalStateException {
	return true;
    }

    /**
     * Determine if the domain maximum for the third argument is in the domain.
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMax3Closed() throws IllegalStateException {
	return true;
    }

    @Override
    public final boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	if (args.length != 3) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return isInDomain(args[0], args[1], args[2]);
    }

    /**
     * Determine if a point (x, y) is within the domain of
     * a real-valued function of two arguments.
     * <P>
     * The default behavior of this method assumes the domain
     * is a rectangular region and uses the methods
     * {@link #getDomainMin1()}, {@link #getDomainMin2()},
     * {@link #getDomainMax1()}, {@link #getDomainMax2()}
     * {@link #domainMin1Closed()}, {@link #domainMin2Closed()},
     * {@link #domainMax1Closed()}, and {@link #domainMax2Closed()}
     * to determine if the arguments represent a point in the
     * functions domain.  If the domain is not rectangular
     * with each side either in or not in the domain, then
     * this method must be overridden.  If it is not possible
     * with a reasonable amount of computation to determine that
     * a point is in the domain, an UnsupportedOperationException
     * may be thrown.  If this exception is thrown, it should be
     * thrown regardless of the arguments.
     * @param x the 1st coordinate
     * @param y the 2nd coordinate
     * @return true if the point (x, y) is in this function's
     *         domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     */
    public boolean isInDomain(double x, double y, double z)
	throws UnsupportedOperationException, IllegalStateException
    {
	double xmin = getDomainMin1();
	double ymin = getDomainMin2();
	double zmin = getDomainMin3();
	double xmax = getDomainMax1();
	double ymax = getDomainMax2();
	double zmax = getDomainMax3();

	if (domainMin1Closed()) {
	    if (x < xmin) {
		return false;
	    }
	} else {
	    if (x <= xmin) {
		return false;
	    }
	}
	if (domainMin2Closed()) {
	    if (y < ymin) {
		return false;
	    }
	} else {
	    if (y <= ymin) {
		return false;
	    }
	}
	if (domainMin3Closed()) {
	    if (z < zmin) {
		return false;
	    }
	} else {
	    if (z <= zmin) {
		return false;
	    }
	}
	if (domainMax1Closed()) {
	    if (x > xmax) {
		return false;
	    }
	} else {
	    if (x >= xmax) {
		return false;
	    }
	}
	if (domainMax2Closed()) {
	    if (y > ymax) {
		return false;
	    }
	} else {
	    if (y >= xmax) {
		return false;
	    }
	}
	if (domainMax3Closed()) {
	    if (z > zmax) {
		return false;
	    }
	} else {
	    if (z >= zmax) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Constructor when the function is provided by a script object.
     * The parameter 'object' is expected to be either an instance
     * of RealValuedFunctionThree or an object defined
     * by a scripting language with methods named "valueAt", "deriv1At"
     * "deriv2At", "deriv11At", "deriv12At", deriv21At", and "deriv22At".
     * Each of these methods takes two arguments, both real numbers,
     * and returns a number. For a real-valued function
     * f(x<sub>1</sub>,x<sub>2</sub>), these methods are defined as follows:
     * <UL>
     *   <LI><CODE>valueAt</CODE> returns  f(x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv1At</CODE> returns &part;f/&part;x<sub>1</sub>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv2At</CODE> returns &part;f/&part;x<sub>2</sub>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv11At</CODE> returns
     *             &part;<sup>2</sup>f/&part;x<sub>1</sub><sup>2</sup>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv12At</CODE> returns
     *             &part;<sup>2</sup>f/&part;x<sub>1</sub>&part;x<sub>2</sub>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv21At</CODE> returns
     *             &part;<sup>2</sup>f/&part;x<sub>2</sub>&part;x<sub>1</sub>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     *   <LI><CODE>deriv22At</CODE> returns
     *             &part;<sup>2</sup>f/&part;x<sub>1</sub><sup>2</sup>
     *             evaluated at the point (x<sub>1</sub>,x<sub>2</sub>).
     * </UL>
     * @param scriptingContext the scripting context
     * @param object an object from a scripting environment defining
     *        the methods defined above or a subset of those methods
     */
    public RealValuedFunctionThree(ScriptingContext scriptingContext,
				 Object object)
    {
	super(3,3);
	context = scriptingContext;
	this.object = object;
    }

    /**
     * Constructor when the function and its first derivatives are
     * provided by functions defined in scripts.
     * The script functions are expected to define up to three
     * functions, and their names are used as the arguments for this
     * method.
     * @param scriptingContext the scripting context
     * @param fname the name of a scripting-language function of three
     *        arguments; null if the valueAt method is not supported
     * @param f1name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the first argument;
     *        null if the deriv1At method is not supported
     * @param f2name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the second argument;
     *        null if the deriv2At method is not supported
     * @param f3name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the third argument;
     *        null if the deriv3At method is not supported
     */
    public
	RealValuedFunctionThree(ScriptingContext scriptingContext,
				String fname,
				String f1name, String f2name, String f3name)
    {
	super(3,3);
	context = scriptingContext;
	this.fname = fname;
	this.f1name = f1name;
	this.f2name = f2name;
	this.f3name = f3name;
    }


    /**
     * Constructor when the function and its first and second
     * derivatives are provided by functions defined in scripts.
     * The script functions are expected to define up to three
     * functions, and their names are used as the arguments for this
     * method.
     * @param scriptingContext the scripting context
     * @param fname the name of a scripting-language function of three
     *        arguments; null if the valueAt method is not supported
     * @param f1name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the first argument;
     *        null if the deriv1At method is not supported
     * @param f2name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the second argument;
     *        null if the deriv2At method is not supported
     * @param f3name the name of a scripting-language function of three
     *        arguments providing the desired function's
     *        first derivative with respect to the third argument;
     *        null if the deriv3At method is not supported
     * @param f11name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv11At(double,double,double)}.
     * @param f12name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv12At(double,double,double)}.
     * @param f13name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv13At(double,double,double)}.
     * @param f21name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv21At(double,double,double)}.
     * @param f22name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv22At(double,double,double)}.
     * @param f23name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv23At(double,double,double)}.
     * @param f31name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv31At(double,double,double)}.
     * @param f32name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv32At(double,double,double)}.
     * @param f33name the name of the scripting-language function of
     *        three arguments providing the value for
     *        {@link #deriv33At(double,double,double)}.
     */
    public
	RealValuedFunctionThree(ScriptingContext scriptingContext,
				String fname,
				String f1name, String f2name, String f3name,
				String f11name, String f12name, String f13name,
				String f21name, String f22name, String f23name,
				String f31name, String f32name, String f33name)
    {
	super(3,3);
	context = scriptingContext;
	this.fname = fname;
	this.f1name = f1name;
	this.f2name = f2name;
	this.f3name = f3name;
	this.f11name = f11name;
	this.f12name = f12name;
	this.f13name = f13name;
	this.f21name = f21name;
	this.f22name = f22name;
	this.f23name = f23name;
	this.f31name = f31name;
	this.f32name = f32name;
	this.f33name = f33name;
    }

    @Override
    public final double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	if (args.length != 3) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return valueAt(args[0], args[1], args[2]);
    }


    /**
     * Call the function.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the function for the given arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double valueAt(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function != null) {
	    return function.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.valueAt(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fname != null) {
		result = context.callScriptFunction(fname, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "valueAt",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes the value of partial derivative
     * that would be computed by calling {@link derivAt(int,double...)}.
     * @param i
     * @return the function
     */
    public final RealValuedFunctThreeOps deriv(int i) {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.derivAt(i, x, y, z);
	    }
	};
    }


    /**
     * {@inheritDoc}
     * <P>
     * This method calls a method named deriviAt(...) where is the value of
     * the first argument. One should usually override those methods (for
     * i in [1,3]) instead of this one.
     */
    @Override
    public final double derivAt(int i, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (args.length != 3) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i == 0) {
	    return deriv1At(args[0], args[1], args[2]);
	} else if (i == 1) {
	    return deriv2At(args[0], args[1], args[2]);
	} else if (i == 2) {
	    return deriv3At(args[0], args[1], args[2]);
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    /**
     * Get a function computing  &part;f / &part;x<sub>1</sub>.
     * @return a function that computes &part;f / &part;x<sub>1</sub>
     *         where f is this real-valued function
     */
    public RealValuedFunctThreeOps deriv1() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv1At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>1</sub>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv1At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function1 != null) {
	    return function1.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv1At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f1name != null) {
		result = context.callScriptFunction(f1name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv1At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function computing  &part;f / &part;x<sub>2</sub>.
     * @return a function that computes &part;f / &part;x<sub>2</sub>
     *         where f is this real-valued function
     */
    public RealValuedFunctThreeOps deriv2() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv2At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>2</sub>
     * for a function f(x<sub>2</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv2At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function2 != null) {
	    return function2.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv2At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f2name != null) {
		result = context.callScriptFunction(f2name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv2At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function computing  &part;f / &part;x<sub>3</sub>.
     * @return a function that computes &part;f / &part;x<sub>3</sub>
     *         where f is this real-valued function
     */
    public RealValuedFunctThreeOps deriv3() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv3At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>3</sub>
     * for the function f(x<sub>1</sub>,x<sub>2</sub>,x<sub>3</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double deriv3At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	if (function3 != null) {
	    return function3.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv3At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f3name != null) {
		result = context.callScriptFunction(f3name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv3At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes the value of partial derivative
     * that would be computed by calling {@link derivAt(int,double...)}.
     * @param i the argument index for the first derivative
     * @param j the argument index for the second derivative
     * @return the function
     */
    public final RealValuedFunctThreeOps secondDeriv(int i, int j) {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return
		    RealValuedFunctionThree.this.secondDerivAt(i, j, x, y, z);
	    }
	};
    }

    /**
     * {@inheritDoc}
     * <P>
     * This method calls a method named derivijAt(...) where i and j
     * are the values of the first two arguments. One should usually
     * override those methods (for i and j in [1,3]) instead of this one.
     */
    @Override
    public final double secondDerivAt(int i, int j, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (args.length != 3) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i == 0 && j == 0) {
	    return deriv11At(args[0], args[1], args[2]);
	} else if (i == 0 && j == 1) {
	    return deriv12At(args[0], args[1], args[2]);
	} else if (i == 0 && j == 2) {
	    return deriv13At(args[0], args[1], args[2]);
	} else if (i == 1 && j == 0) {
	    return deriv21At(args[0], args[1], args[2]);
	} else if (i == 1 && j == 1) {
	    return deriv22At(args[0], args[1], args[2]);
	} else if (i == 1 && j == 2) {
	    return deriv23At(args[0], args[1], args[2]);
	} else if (i == 2 && j == 0) {
	    return deriv31At(args[0], args[1], args[2]);
	} else if (i == 2 && j == 1) {
	    return deriv32At(args[0], args[1], args[2]);
	} else if (i == 2 && j == 2) {
	    return deriv33At(args[0], args[1], args[2]);
	} else {
	    if (i < 0 || i > 2) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", i));
	    }
	    if (j < 0 || j > 2) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", j));
	    }
	    throw new org.bzdev.lang.UnexpectedExceptionError();
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup>
     */
    public RealValuedFunctThreeOps deriv11() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv11At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv11At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function11 != null) {
	    return function11.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv11At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f11name != null) {
		result = context.callScriptFunction(f11name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv11At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>2</sub>)
     * where f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>2</sub>)
     */
    public RealValuedFunctThreeOps deriv12() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv12At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>2</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv12At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function12 != null) {
	    return function12.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv12At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f12name != null) {
		result = context.callScriptFunction(f12name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv12At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>3</sub>)
     * where f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>3</sub>)
     */
    public RealValuedFunctThreeOps deriv13() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv13At(x, y, z);
	    }
	};
    }


    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>3</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv13At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function13 != null) {
	    return function13.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv13At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f13name != null) {
		result = context.callScriptFunction(f13name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv13At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>1</sub>)
     * where f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>1</sub>)
     */
    public RealValuedFunctThreeOps deriv21() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv12At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>1</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv21At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function21 != null) {
	    return function21.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv21At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f21name != null) {
		result = context.callScriptFunction(f11name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv21At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     */
    public RealValuedFunctThreeOps deriv22() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv22At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv22At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function22 != null) {
	    return function22.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv22At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f22name != null) {
		result = context.callScriptFunction(f22name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv22At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>3</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>3</sup>
     */
    public RealValuedFunctThreeOps deriv23() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv23At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>3</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv23At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function23 != null) {
	    return function23.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv23At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f23name != null) {
		result = context.callScriptFunction(f23name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv23At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>1</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>1</sup>
     */
    public RealValuedFunctThreeOps deriv31() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv31At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>3</sub> &part;x<sub>1</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv31At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function31 != null) {
	    return function31.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv31At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f31name != null) {
		result = context.callScriptFunction(f31name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv31At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>2</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>2</sup>
     */
    public RealValuedFunctThreeOps deriv32() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv32At(x, y, z);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>3</sub> &part;x<sub>2</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv32At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function32 != null) {
	    return function32.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv32At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f32name != null) {
		result = context.callScriptFunction(f32name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv32At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>2</sup> where
     * f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>2</sup>
     */
    public RealValuedFunctThreeOps deriv33() {
	return new RealValuedFunctThreeOps() {
	    public double valueAt(double x, double y, double z) {
		return RealValuedFunctionThree.this.deriv33At(x, y, z);
	    }
	};
    }


    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>3</sub><sup>2</sup>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @param arg3 the function's third argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv33At(double arg1, double arg2, double arg3) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function33 != null) {
	    return function33.valueAt(arg1, arg2, arg3);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionThree) {
		RealValuedFunctionThree f = (RealValuedFunctionThree)object;
		return f.deriv33At(arg1, arg2, arg3);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f33name != null) {
		result = context.callScriptFunction(f33name, arg1, arg2, arg3);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv33At",
						  arg1, arg2, arg3);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg3", arg1, arg2, arg3);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }
}

//  LocalWords:  exbundle valueAt deriv UnsupportedOperationException
//  LocalWords:  EMCAScript blockquote pre importClass ourObject nd
//  LocalWords:  setFunction RealValuedFunctionThree funct domainMin
//  LocalWords:  RealValuedFunction RealValuedFunctThreeOps domainMax
//  LocalWords:  argOutOfRangeI wrongNumberArgs getDomainMin fname
//  LocalWords:  getDomainMax scriptingContext arg numberNotReturned
//  LocalWords:  IllegalArgumentException functionNotSupported
//  LocalWords:  callFailsArg opNotSupported derivAt deriviAt
//  LocalWords:  derivijAt
