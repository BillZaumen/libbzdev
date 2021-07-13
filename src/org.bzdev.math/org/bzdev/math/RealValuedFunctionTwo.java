package org.bzdev.math;
import org.bzdev.scripting.ScriptingContext;
import javax.script.ScriptException;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class  defining a real-valued function with two real arguments.
 * This is intended for cases in which a function should be passed
 * as an argument.
 * <P>
 * A subclass will typically override one or more of the methods
 * valueAt, deriv1At, deriv2At, deriv11At, deriv12At, deriv21At, and
 * deriv22At to provide the values for a function and its first and
 * second partial derivative.  For any that are not available, an
 * UnsupportedOperationException will be thrown.
 * <P>
 * The class also provides scripting-language support. If a Scripting
 * context is named <code>scripting</code>, the following EMCAScript
 * code will implement a function and its derivatives:
 * <blockquote><code><pre>
 *     importClass(org.bzdev.RealValuedFunctionTwo);
 *     ....
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunctionTwo as its argument.
 *     funct = new RealValuedFunctionTwo(scripting,
 *               {valueAt: function(x,y) {return Math.sin(x) * Math.cos(y);},
 *                deriv1At: function(x,y) {return Math.cos(x) * Math.cos(y);},
 *                deriv2At: function(x,y) {return -Math.sin(x) * Math.sin(y);},
 *                deriv11At: function(x,y) {return -Math.sin(x) * Math.cos(y);},
 *                deriv12At: function(x,y) {return -Math.cos(x) * Math.sin(y);},
 *                deriv21At: function(x,y) {return -Math.cos(x) * Math.sin(y);},
 *                deriv22At: function(x,y) {return -Math.sin(x) * Math.cos(y);}
 *               };
 *     ourObject.setFunction(funct);
 * </pre></code></blockquote>
 * Alternatively, one may use the following code where the functions
 * defining the derivatives are provided by name:
 * <blockquote><code><pre>
 *     importClass(org.bzdev.RealValuedFunctionTwo);
 *     ...
 *     function f(x,y) {return Math.sin(x) * Math.cos(y);}
 *     function f1(x,y) {return Math.cos(x) * Math.cos(y);}
 *     function f2(x,y) {return -Math.sin(x) * Math.sin(y);}
 *     function f11(x,y) {return -Math.sin(x) * Math.cos(y);}
 *     function f12(x,y) {return  -Math.cos(x) * Math.sin(y);}
 *     function f21(x,y) {return -Math.cos(x) * Math.sin(y);}
 *     function f22(x,y) {return -Math.sin(x) * Math.cos(y);}
 *     ...
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunction as its argument.
 *     funct = new RealValuedFunction(scripting, "f", "f1", "f2",
                                      "f11", "f12", "f21", "f22");
 *     ourObject.setFunction(funct);
 * </pre></code></blockquote>
 */
public class  RealValuedFunctionTwo extends RealValuedFunctionVA
    implements RealValuedFunctTwoOps
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
    private String f11name = null;
    private String f12name = null;
    private String f21name = null;
    private String f22name = null;
    // interface based
    RealValuedFunctTwoOps function = null;
    RealValuedFunctTwoOps function1 = null;
    RealValuedFunctTwoOps function2 = null;
    RealValuedFunctTwoOps function11 = null;
    RealValuedFunctTwoOps function12 = null;
    RealValuedFunctTwoOps function21 = null;
    RealValuedFunctTwoOps function22 = null;

    /**
     * Constructor.
     */
    public RealValuedFunctionTwo() {
	super(2,2);
    }

    /**
     * Constructor given a function that determines its value.
     * The argument implements {@link RealValuedFunctTwoOps} and
     * can be a lambda expression  with two arguments. The interface
     * {@link RealValuedFunctTwoOps} provides a single method:
     * {@link RealValuedFunctTwoOps#valueAt(double,double)} that, when
     * called, provides the function's value.
     * @param function the function providing the value for the
     *        method valueAt; null if the valueAt method is not supported
     */
    public RealValuedFunctionTwo(RealValuedFunctTwoOps function) {
	super(2,2);
	this.function = function;
    }

    /**
     * Constructor given functions that determines its value and its first
     * partial derivatives.
     * The arguments implement {@link RealValuedFunctTwoOps} and
     * can be a lambda expression  with two arguments. The interface
     * {@link RealValuedFunctTwoOps} provides a single method:
     * {@link RealValuedFunctTwoOps#valueAt(double,double)} that, when
     * called, provides the function's value.
     * @param function the function providing the value for the
     *        method valueAt; null if the valueAt method is not supported
     * @param function1 the function providing the value for the
     *        method {#link #deriv1At(double,double)};
     *        null if the deriv1At method is not supported
     * @param function2 the function providing the value for the
     *        method {#link #deriv2At(double,double)};
     *        null if the deriv2At method is not
     *        supported
     */
    public RealValuedFunctionTwo(RealValuedFunctTwoOps function,
				 RealValuedFunctTwoOps function1,
				 RealValuedFunctTwoOps function2)
    {
	this(function);
	this.function1 = function1;
	this.function2 = function2;
    }

    /**
     * Constructor given functions that determines its value and its first
     * and second partial derivatives
     * The arguments implement {@link RealValuedFunctTwoOps} and
     * can be a lambda expression  with two arguments. The interface
     * {@link RealValuedFunctTwoOps} provides a single method:
     * {@link RealValuedFunctTwoOps#valueAt(double,double)} that, when
     * called, provides the function's value.
     * @param function the function providing the value for the
     *        method valueAt; null if the valueAt method is not supported
     * @param function1 the function providing the value for the
     *        method {@link #deriv1At(double,double)};
     *        null if the deriv1At method is not supported
     * @param function2 the function providing the value for the
     *        method {@link #deriv2At(double,double)};
     *        null if the deriv2At method is not
     *        supported
     * @param function11 the function providing the value for the
     *        method {@link #deriv11At(double,double)};
     *        null if the deriv11At method is not supported
     * @param function12 thea function providing the value for the
     *        method {@link #deriv12At(double,double)};
     *        null if the deriv12At method is not supported
     * @param function21 the function providing the value for the
     *        method {@link #deriv21At(double,double)};
     *        null if the deriv21At method is not supported
     * @param function22 the function providing the value for the
     *        method {@link #deriv22At(double,double)};
     *        null if the deriv22At method is not supported
     */
    public RealValuedFunctionTwo(RealValuedFunctTwoOps function,
				 RealValuedFunctTwoOps function1,
				 RealValuedFunctTwoOps function2,
				 RealValuedFunctTwoOps function11,
				 RealValuedFunctTwoOps function12,
				 RealValuedFunctTwoOps function21,
				 RealValuedFunctTwoOps function22)
    {
	this(function, function1, function2);
	this.function11 = function11;
	this.function12 = function12;
	this.function21 = function21;
	this.function22 = function22;
    }


    @Override
    public final double getDomainMin(int i) throws IllegalArgumentException,
						   IllegalStateException
    {
	if (i == 0) {
	    return getDomainMin1();
	} else if (i == 1) {
	    return getDomainMin2();
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
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    @Override
    public final boolean domainMinClosed(int i)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i != 0) {
	}
	if (i == 0) {
	    return domainMin1Closed();
	} else if (i == 1) {
	    return domainMin2Closed();
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

    @Override
    public final boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	if (args.length != 2) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return isInDomain(args[0], args[1]);
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
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean isInDomain(double x, double y)
	throws UnsupportedOperationException, IllegalStateException
    {
	double xmin = getDomainMin1();
	double ymin = getDomainMin2();
	double xmax = getDomainMax1();
	double ymax = getDomainMax2();
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
	return true;
    }


    /**
     * Constructor when the function is provided by a script object.
     * The parameter 'object' is expected to be either an instance
     * of RealValuedFunctionTwo or an object defined
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
    public RealValuedFunctionTwo(ScriptingContext scriptingContext,
				 Object object)
    {
	super(2,2);
	context = scriptingContext;
	this.object = object;
    }

    /**
     * Constructor when the function and its first partial derivatives
     *  are provided by a script.
     * The script is expected to define up to three functions, indicated
     * by their names.
     * @param scriptingContext the scripting context
     * @param fname the name of a function providing the value for the
     *        method valueAt; null if the valueAt method is not supported
     * @param f1name the name of a function providing the value for the
     *        method {@link #deriv1At(double,double)};
     *        null if the deriv1At method is not supported
     * @param f2name the name of a function providing the value for the
     *        method {@link #deriv2At(double,double)};
     *        null if the deriv2At method is not
     *        supported
     */
    public RealValuedFunctionTwo(ScriptingContext scriptingContext,
				 String fname, String f1name, String f2name)
    {
	super(2,2);
	context = scriptingContext;
	this.fname = fname;
	this.f1name = f1name;
	this.f2name = f2name;
    }


    /**
     * Constructor when the function and its first and second partial
     * derivatives are provided by a script.
     * The script is expected to define up to seven functions, indicated
     * by their names.
     * @param scriptingContext the scripting context
     * @param fname the name of a function providing the value for the
     *        method valueAt; null if the valueAt method is not supported
     * @param f1name the name of a function providing the value for the
     *        method {@link #deriv1At(double,double)};
     *        null if the deriv1At method is not supported
     * @param f2name the name of a function providing the value for the
     *        method {@link #deriv2At(double,double)};
     *        null if the deriv2At method is not
     *        supported
     * @param f11name the name of a function providing the value for the
     *        method {@link #deriv11At(double,double)};
     *        null if the deriv11At method is not supported
     * @param f12name the name of a function providing the value for the
     *        method {@link #deriv12At(double,double)};
     *        null if the deriv12At method is not supported
     * @param f21name the name of a function providing the value for the
     *        method {@link #deriv21At(double,double)};
     *        null if the deriv21At method is not supported
     * @param f22name the name of a function providing the value for the
     *        method {@link #deriv22At(double,double)};
     *        null if the deriv22At method is not supported
     */
    public RealValuedFunctionTwo(ScriptingContext scriptingContext,
				 String fname, String f1name, String f2name,
				 String f11name, String f12name,
				 String f21name, String f22name)
    {
	super(2,2);
	context = scriptingContext;
	this.fname = fname;
	this.f1name = f1name;
	this.f2name = f2name;
	this.f11name = f11name;
	this.f12name = f12name;
	this.f21name = f21name;
	this.f22name = f22name;
    }

    @Override
    public final double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	if (args.length != 2) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return valueAt(args[0], args[1]);
    }


    /**
     * Call the function.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the function for the given arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double valueAt(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function != null) {
	    return function.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.valueAt(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fname != null) {
		result = context.callScriptFunction(fname, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "valueAt",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
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
    public final RealValuedFunctTwoOps deriv(int i) {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.derivAt(i, x, y);
	    }
	};
    }

    /**
     * {@inheritDoc}
     * <P>
     * This method calls a method named
     * <CODE>deriv</CODE>&lt;i+1&gt;<CODE>At(...)</CODE> where
     * <CODE>i</CODE>is the value of
     * the first argument of this method. One should usually override
     * those methods (for (i+1) in [1,2]) instead of this one.
     */
    @Override
    public final double derivAt(int i, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (args.length != 2) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i == 0) {
	    return deriv1At(args[0], args[1]);
	} else if (i == 1) {
	    return deriv2At(args[0], args[1]);
	} else {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
    }

    /**
     * Get a function computing  &part;f / &part;x<sub>1</sub>.
     * @return a function that computes &part;f / &part;x<sub>1</sub>
     */
    public RealValuedFunctTwoOps deriv1() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv1At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>1</sub>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv1At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function1 != null) {
	    return function1.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv1At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f1name != null) {
		result = context.callScriptFunction(f1name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv1At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function computing  &part;f / &part;x<sub>2</sub>.
     * @return a function that computes &part;f / &part;x<sub>2</sub>
     */
    public RealValuedFunctTwoOps deriv2() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv2At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>2</sub>
     * for a function f(x<sub>2</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv2At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function2 != null) {
	    return function2.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv2At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f2name != null) {
		result = context.callScriptFunction(f2name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv2At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes the value of the second partial derivative
     * that would be computed by calling
     * {@link #secondDerivAt(int,int,double...)}.
     * @param i the index of the argument for the first differentiation
     * @param j the index of the argument for the second differentiation
     * @return the function
     */
    public final RealValuedFunctTwoOps secondDeriv(int i, int j) {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.secondDerivAt
		    (i, j, x, y);
	    }
	};
    }


    /**
     * {@inheritDoc}
     * <P>
     * This method calls a method named derivijAt(...) where i and j
     * are the values of the first two arguments. One should usually
     * override those methods (for i and j in [1,2]) instead of this one.
     */
    @Override
    public final double secondDerivAt(int i, int j, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (args.length != 2) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i == 0 && j == 0) {
	    return deriv11At(args[0], args[1]);
	} else if (i == 0 && j == 1) {
	    return deriv12At(args[0], args[1]);
	} else if (i == 1 && j == 0) {
	    return deriv21At(args[0], args[1]);
	} else if (i == 1 && j == 1) {
	    return deriv22At(args[0], args[1]);
	} else {
	    if (i < 0 || i > 1) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", i));
	    }
	    if (j < 0 || j > 1) {
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
    public RealValuedFunctTwoOps deriv11() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv11At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv11At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function11 != null) {
	    return function11.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv11At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f11name != null) {
		result = context.callScriptFunction(f11name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv11At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
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
    public RealValuedFunctTwoOps deriv12() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv12At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>2</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv12At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function12 != null) {
	    return function12.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv12At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f12name != null) {
		result = context.callScriptFunction(f12name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv12At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
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
    public RealValuedFunctTwoOps deriv21() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv21At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>1</sub>)
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv21At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function21 != null) {
	    return function21.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv21At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f21name != null) {
		result = context.callScriptFunction(f21name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv21At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Get a function that computes
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     * where f is this function.
     * @return a function that computes
     *         &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     */
    public RealValuedFunctTwoOps deriv22() {
	return new RealValuedFunctTwoOps() {
	    public double valueAt(double x, double y) {
		return RealValuedFunctionTwo.this.deriv22At(x, y);
	    }
	};
    }

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double deriv22At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function22 != null) {
	    return function22.valueAt(arg1, arg2);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionTwo) {
		RealValuedFunctionTwo f = (RealValuedFunctionTwo)object;
		return f.deriv22At(arg1, arg2);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (f22name != null) {
		result = context.callScriptFunction(f22name, arg1, arg2);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "deriv22At",
						  arg1, arg2);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg2", arg1, arg2);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }
}

//  LocalWords:  exbundle valueAt deriv UnsupportedOperationException
//  LocalWords:  EMCAScript blockquote pre importClass ourObject thea
//  LocalWords:  setFunction RealValuedFunctionTwo funct getDomainMin
//  LocalWords:  RealValuedFunction RealValuedFunctTwoOps domainMin
//  LocalWords:  argOutOfRangeI wrongNumberArgs getDomainMax nd fname
//  LocalWords:  domainMax scriptingContext arg functionNotSupported
//  LocalWords:  IllegalArgumentException numberNotReturned derivAt
//  LocalWords:  callFailsArg opNotSupported derivijAt
