package org.bzdev.math;
import org.bzdev.scripting.ScriptingContext;
import javax.script.ScriptException;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class  defining a real-valued function with a real argument.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * This is intended for cases in which a function should be passed
 * as an argument.
 * <P>
 * A subclass will typically override one or more of the methods
 * valueAt, derivAt, and secondDerivAt to provide the values for
 * a function, its first derivative, and its second derivative.
 * For any that are not available, an UnsupportedOperationException
 * will be thrown.
 * <P>
 * The class also provides scripting-language support. If a Scripting
 * context is named <code>scripting</code>, the following EMCAScript
 * code will implement a sin function and its derivatives:
 * <blockquote><pre><code>
 *     importClass(org.bzdev.RealValuedFunction);
 *     ....
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunction as its argument.
 *     funct = new RealValuedFunction(scripting,
 *               {valueAt: function(x) {return Math.sin(x);},
 *                derivAt: function(x) {return Math.cos(x);},
 *                secondDerivAt: function(x) {return -Math.cos(x);}});
 *     ourObject.setFunction(funct);
 * </CODE></PRE></blockquote>
 * Alternatively, one may use the following code where the functions
 * defining the derivatives are provided by name:
 * <blockquote><pre><code>
 *     importClass(org.bzdev.RealValuedFunction);
 *     ...
 *     function f(x) {return Math.sin(x);}
 *     function fp(x) {return Math.cos(x);}
 *     function fpp(x) {return -Math.sin(x);}
 *     ...
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunction as its argument.
 *     funct = new RealValuedFunction(scripting, "f", "fp", "fpp");
 *     ourObject.setFunction(funct);
 * </CODE></PRE></blockquote>
 */
public class  RealValuedFunction extends RealValuedFunctionVA
    implements RealValuedDomainOps, RealValuedFunctOps
{

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private ScriptingContext context;
    // script object
    private Object object;
    // function names
    private String fname = null;
    private String fpname = null;
    private String fppname = null;
    // support for lambda expressions
    private RealValuedFunctOps function = null;
    private RealValuedFunctOps functionp = null;
    private RealValuedFunctOps functionpp = null;
    /**
     * Constructor.
     */
    public RealValuedFunction() {
	super(1,1);
    }

    /**
     * Constructor given a function.
     * This allows a lambda expression to be used to implement
     * a real-valued function.
     * @param function the function itself
     */
    public RealValuedFunction(RealValuedFunctOps function) {
	super(1,1);
	this.function = function;
    }


    /**
     * Constructor given a function and its derivatives.
     * This allows  lambda expressions to be used to implement
     * a real-valued function.
     * @param function the function itself
     * @param functionp the first derivative of the function
     * @param functionpp the second derivative of the function
     */
    public RealValuedFunction(RealValuedFunctOps function,
			      RealValuedFunctOps functionp,
			      RealValuedFunctOps functionpp)
    {
	this(function);
	this.functionp = functionp;
	this.functionpp = functionpp;
    }

    @Override
    public final double getDomainMin(int i) throws IllegalArgumentException,
						   IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return getDomainMin();
    }

    /**
     * Get the minimum value in the domain of the function.
     * @return the minimum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public double getDomainMin() throws IllegalStateException {
	return -Double.MAX_VALUE;
    }

    @Override
    public final double getDomainMax(int i) throws IllegalArgumentException,
						   IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return getDomainMax();
    }

    /*
     * Get the maximum value in the domain of the function.
     * @return the maximum value
     */
    @Override
    public double getDomainMax() throws IllegalStateException {
	return Double.MAX_VALUE;
    }


    @Override
    public final boolean domainMinClosed(int i)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return domainMinClosed();
    }

    /*
     * Determine if the domain minimum is in the domain.
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     */
    @Override
    public boolean domainMinClosed() throws IllegalStateException {return true;}

    @Override
    public final boolean domainMaxClosed(int i)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return domainMaxClosed();
    }


    /*
     * Determine if the domain maximum is in the domain.
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public boolean domainMaxClosed() throws IllegalStateException {return true;}

    @Override
    public final boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return isInDomain(args[0]);
    }

    /*
     * Determine if an argument is within the domain of
     * a real-valued function.
     * <P>
     * The default behavior of this method assumes the domain
     * is an interval and uses the methods
     * {@link #getDomainMin()}, {@link #getDomainMin()},
     * {@link #domainMinClosed()}, and {@link #domainMinClosed()}
     * to determine if the argument represents a point in the
     * functions domain.  If the domain is not an interval
     * with each end either open or closed, then
     * this method must be overridden.  If it is not possible
     * with a reasonable amount of computation to determine that
     * the argument is in the domain, an UnsupportedOperationException
     * may be thrown.  If this exception is thrown, it should be
     * thrown regardless of the argument.
     * @param x a value to test
     * @return true if  x is in this function's domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     */
    @Override
    public boolean isInDomain(double x) throws UnsupportedOperationException,
					       IllegalStateException
    {
	double xmin = getDomainMin();
	double xmax = getDomainMax();
	if (domainMinClosed()) {
	    if (x < xmin) {
		return false;
	    }
	} else {
	    if (x <= xmin) {
		return false;
	    }
	}
	if (domainMaxClosed()) {
	    if (x > xmax) {
		return false;
	    }
	} else {
	    if (x >= xmax) {
		return false;
	    }
	}
	return true;
    }


    /**
     * Constructor when the function is provided by a script object.
     * The parameter 'object' is expected to be either an instance
     * of RealValuedFunction or an object defined
     * by a scripting language with methods named "valueAt", "derivAt"
     * and "secondDerivAt".  Each of these three methods takes a number
     * as its argument and return a number.
     * @param scriptingContext the scripting context
     * @param object an object from a scripting environment
     */
    public RealValuedFunction(ScriptingContext scriptingContext,
			      Object object)
    {
	super(1,1);
	context = scriptingContext;
	this.object = object;
    }

    /**
     * Constructor when the function is provided by a script.
     * The script is expected to define up to three functions, indicated
     * by their names.
     * @param scriptingContext the scripting context
     * @param fname the name of a function; null if the valueAt method is not
     *        supported
     * @param fpname the name of a function providing the desired function's
     *        first derivative; null if the derivAt method is not supported
     * @param fppname the name of a function providing the desired function's
     *        second derivative; null if the secondDerivAt method is not
     *        supported
     */
    public RealValuedFunction(ScriptingContext scriptingContext,
			      String fname, String fpname, String fppname)
    {
	super(1,1);
	context = scriptingContext;
	this.fname = fname;
	this.fpname = fpname;
	this.fppname = fppname;
    }

    @Override
    public final double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return valueAt(args[0]);
    }

    /**
     * Call the function.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double valueAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (function != null) {
	    return function.valueAt(arg);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunction) {
		RealValuedFunction f = (RealValuedFunction)object;
		return f.valueAt(arg);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fname != null) {
		result = context.callScriptFunction(fname, arg);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "valueAt", arg);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg", arg);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    @Override
    public final double derivAt(int i, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return derivAt(args[0]);
    }

    /**
     * Get a function that computes this function's nth derivative.
     * The value of n must be 0 (for the function itself), 1 for
     * the first derivative, or 2 for the second derivative.
     * <P>
     * If this real-valued function does not support an nth derivative,
     * the returned function's valueAt method will throw a
     * {@link java.lang.UnsupportedOperationException}.
     * @param n 1 for the first derivative; 2 for the second derivative;
     *        0 for the function itself
     * @return a function providing the derivative of this function
     * @exception IllegalArgumentException n is not 0, 1, or 2.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public RealValuedFunctOps deriv(int n)
	throws IllegalArgumentException, IllegalStateException
    {
	switch (n) {
	case 0: return this;
	case 1:
	    return new RealValuedFunctOps() {
		public double valueAt(double x) {
		    return RealValuedFunction.this.derivAt(x);
		}
	    };
	case 2:
	    return new RealValuedFunctOps() {
		public double valueAt(double x) {
		    return RealValuedFunction.this.secondDerivAt(x);
		}
	    };
	default:
	    throw new IllegalArgumentException(errorMsg("derivOrder"));
	}
    }


    /**
     * Get a function that computes the first derivative of this
     * real-valued function.
     * If this real-valued function does not support a first derivative,
     * the returned function's valueAt method will throw a
     * {@link java.lang.UnsupportedOperationException}.
     * @return the first derivative
     */
    public RealValuedFunctOps deriv() {
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return RealValuedFunction.this.derivAt(x);
	    }
	};
    }

    /**
     * Get a function that computes the second derivative of
     * this real-valued function.
     * If this real-valued function does not support a second derivative,
     * the returned function's valueAt method will throw a
     * {@link java.lang.UnsupportedOperationException}.
     * @return the second derivative
     */
    public RealValuedFunctOps secondDeriv()
    {
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return RealValuedFunction.this.secondDerivAt(x);
	    }
	};
    }

    /**
     * Evaluate the function's first derivative.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double derivAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	if (functionp != null) {
	    return functionp.valueAt(arg);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunction) {
		RealValuedFunction f = (RealValuedFunction)object;
		return f.derivAt(arg);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fpname != null) {
		result = context.callScriptFunction(fpname, arg);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "derivAt", arg);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg", arg);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    @Override
    public final double secondDerivAt(int i, int j, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	if (j != 0) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return secondDerivAt(args[0]);
    }

    /**
     * Evaluate the function's second derivative.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double secondDerivAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (functionpp != null) {
	    return functionpp.valueAt(arg);
	}
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunction) {
		RealValuedFunction f = (RealValuedFunction)object;
		return f.secondDerivAt(arg);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fppname != null) {
		result = context.callScriptFunction(fppname, arg);
	    } else {
		if (object == null) throw new UnsupportedOperationException();
		result = context.callScriptMethod(object, "secondDerivAt", arg);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArg", arg);
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Provide an instance of a real-valued function that
     * just returns its argument.
     * I.e., a function defined by f(x) = x.
     * <P>
     * Note: this is a common case for curve fitting when the
     * a function y(x) is generated and one wants to create a
     * an instance of SplinePath2D for plotting.
     */
    public static final RealValuedFunction xFunction =
	new RealValuedFunction() {
	    public double valueAt(double x) {
		return x;
	    }
	    public double derivAt(double x) {
		return 1.0;
	    }
	    public double secondDerivAt(double x) {
		return 0.0;
	    }
	};
}

//  LocalWords:  exbundle valueAt derivAt secondDerivAt EMCAScript fp
//  LocalWords:  UnsupportedOperationException blockquote pre funct
//  LocalWords:  importClass ourObject setFunction RealValuedFunction
//  LocalWords:  fpp argOutOfRangeI wrongNumberArgs getDomainMin arg
//  LocalWords:  domainMinClosed scriptingContext fname fpname
//  LocalWords:  fppname IllegalArgumentException numberNotReturned
//  LocalWords:  functionNotSupported callFailsArg opNotSupported
//  LocalWords:  SplinePath IllegalStateException derivOrder
