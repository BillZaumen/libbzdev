package org.bzdev.math;
import org.bzdev.scripting.ScriptingContext;
import javax.script.ScriptException;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class  defining a real-valued function with an arbitrary number
 * of arguments.
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
 * valueAt, derivAt, and secondDerivAt to provide the values for a
 * function and its first and second partial derivative. 
 * The subclass may also override the methods getDomainMin, getDomainMax,
 * getDomainMinClosed, getDomainMaxClosed, or isInDomain.
 * For any that are not available, an
 * UnsupportedOperationException will be thrown.
 * <P>
 * The class also provides scripting-language support. If a Scripting
 * context is named <code>scripting</code>, the following EMCAScript
 * code will implement a sin function and its derivatives:
 * <blockquote><pre><code>
 *     importClass(org.bzdev.RealValuedFunctionVA);
 *     ....
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunctionVA as its argument.
 *     funct = new RealValuedFunctionVA(scripting,
 *               {valueAt: function(array) {
 *                             var x = array[0]; var y = array[1];
 *                             return Math.sin(x) * Math.cos(y);
 *                         },
 *                derivAt: function(i, array) {
 *                             var x = array[0]; var y = array[1];
 *                              if (i == 0) {
 *                                  return Math.cos(x) * Math.cos(y);
 *                              } else if (i == 1) {
 *				    return -Math.sin(x) * Math.sin(y);
 *                              }
 *                          },
 *		  secondDerivAt: function(i, j, array) {
 *                                   var x = array[0]; var y = array[1];
 *                                   if (i == 0 &amp;&amp; j == 0) {
 *                                      return -Math.sin(x) * Math.cos(y);
 *                                   } else if (i == 0 &amp;&amp; j == 1) {
 *                                      return -Math.cos(x) * Math.sin(y);
 *                                   } else if (i == 1 &amp;&amp; j == 0) {
 *                                      return -Math.cos(x) * Math.sin(y);
 *                                   } else if (i == 1 &amp;&amp; j == 1) {
 *                                      return -Math.sin(x) * Math.cos(y);
 *                                   }
 *                                }
 *               });
 *     ourObject.setFunction(funct);
 * </CODE></PRE></blockquote>
 * Alternatively, one may use the following code where the functions
 * defining the derivatives are provided by name:
 * <blockquote><pre><code>
 *     importClass(org.bzdev.RealValuedFunctionVA);
 *     ...
 *     function f(array) {
 *        var x = array[0]; var y = array[1];
 *        return Math.sin(x) * Math.cos(y);
 *     }
 *     function f1(i, array) {
 *        var x = array[0]; var y = array[1];
 *        if (i == 0) {
 *           return Math.cos(x) * Math.cos(y);
 *        } else if (i == 1) {
 *           return - Math.sin(x)*Math.sin(y);
 *        }
 *     }
 *     function f2(i,j,array) {
 *        var x = array[0]; var y = array[1];
 *        if (i == 0 &amp;&amp; j == 0) {
 *           return -Math.sin(x)*Math.cos(y);
 *        } else if (i == 0 &amp;&amp; j == 1) {
 *           return -Math.cos(x)*Math.sin(y);
 *        } else if (i == 1 &amp;&amp; j == 0) {
 *           return -Math.cos(x)*Math.sin(y);
 *        } else if (i == 1 &amp;&amp; j == 1) {
 *           return -Math.sin(x) * Math.sin(y);
 *        }
 *     }
 *     // assume ourObject is a Java class with a method setFunction
 *     // that takes a RealValuedFunction as its argument.
 *     funct = new RealValuedFunctionVA(scripting, "f", "f1", "f2");
 *     ourObject.setFunction(funct);
 * </CODE></PRE></blockquote>
 */
public class RealValuedFunctionVA
    implements VADomainOps, RealValuedFunctVAOps
{

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private ScriptingContext context;
    // script object
    private Object object;
    // function names
    private String fname = null;
    private String derivName = null;
    private String secondDerivName = null;
    /**
     * Constructor.
     * @param minArgLength the minimum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param maxArgLength the mmaximum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     */
    public RealValuedFunctionVA(int minArgLength, int maxArgLength) {
	super();
	if (minArgLength == -1) {
	    this.minArgLength = 1;
	} else {
	    this.minArgLength = minArgLength;
	}
	if (maxArgLength == -1) {
	    this.maxArgLength = Integer.MAX_VALUE;
	} else {
	    this.maxArgLength = maxArgLength;
	}
    }

    /**
     * Get the greatest lower bound of the i<sup>th</sup> argument when the
     * arguments are in the domain of the function.
     * The implementation will either return the most negative
     * double-precision number or the result of calling a method named
     * getDomainMin (with the same arguments as this method) provided
     * by an object created by a scripting language. If an object
     * created via a scripting language is not passed to a
     * constructor, and a different value is appropriate, this method
     * should be overridden.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return the minimum value
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public double getDomainMin(int argIndex)
	throws IllegalArgumentException, IllegalStateException
    {
	if (context == null && object == null) {
	    return -Double.MAX_VALUE;
	}
	Object result;
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA) object;
		return f.getDomainMin(argIndex);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    if (fname != null) {
		// The constructor that gets the function name does not
		// include one for getDomainMin.
		return -Double.MAX_VALUE;
	    } else {
		result =
		    context.callScriptMethod(object,"getDomainMin", argIndex);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    return -Double.MAX_VALUE;
	}
    }

    /**
     * Get the least upper bound of the i<sup>th</sup> argument when the
     * arguments are in the domain of the function.
     * The implementation will either return the largest
     * double-precision number or the result of calling a method named
     * getDomainMax (with the same arguments as this method) provided
     * by an object created by a scripting language. If an object
     * created via a scripting language is not passed to a
     * constructor, and a different value is appropriate, this method
     * should be overridden.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return the maximum value
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public double getDomainMax(int argIndex) throws IllegalArgumentException,
						    IllegalStateException
    {
	if (context == null && object == null) {
	    return Double.MAX_VALUE;
	}
	Object result;
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA) object;
		return f.getDomainMax(argIndex);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    if (fname != null) {
		// The constructor that gets the function name does not
		// include one for getDomainMin.
		return Double.MAX_VALUE;
	    } else {
		result =
		    context.callScriptMethod(object,"getDomainMax", argIndex);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    return Double.MAX_VALUE;
	}
    }


    /**
     * Determine if the domain minimum for the i<sup>th</sup> argument, when the
     * arguments are in the domain of the function, is in the function's domain.
     * The implementation will either return true or the result of
     * calling a method named getDomainMinClosed (with the same
     * arguments as this method) provided by an object created by a
     * scripting language.  If an object created via a scripting language
     * is not passed to a constructor, and a different value is appropriate,
     * this method should be overridden.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public boolean domainMinClosed(int argIndex)
	throws IllegalArgumentException, IllegalStateException
    {
	if (context == null && object == null) {
	    return true;
	}
	Object result;
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA) object;
		return f.domainMinClosed(argIndex);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    if (fname != null) {
		// The constructor that gets the function name does not
		// include one for getDomainMin.
		return true;
	    } else {
		result =
		    context.callScriptMethod(object,"getDomainMinClosed",
					     argIndex);
	    }
	    if (result instanceof Boolean) {
		return ((Boolean) result).booleanValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("booleanNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    return true;
	}
    }

    /**
     * Determine if the domain maximum for the i<sup>th</sup> argument, when the
     * arguments are in the domain of the function, is in the function's domain.
     * The implementation will either return true or the result of
     * calling a method named getDomainMaxClosed (with the same
     * arguments as this method) provided by an object created by a
     * scripting language. If an object created via a scripting language
     * is not passed to a constructor, and a different value is appropriate,
     * this method should be overridden.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public boolean domainMaxClosed(int argIndex)
	throws IllegalArgumentException, IllegalStateException
    {
	if (context == null && object == null) {
	    return true;
	}
	Object result;
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA) object;
		return f.domainMinClosed(argIndex);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    if (fname != null) {
		// The constructor that gets the function name does not
		// include one for getDomainMin.
		return true;
	    } else {
		result =
		    context.callScriptMethod(object,"getDomainMaxClosed",
					     argIndex);
	    }
	    if (result instanceof Boolean) {
		return ((Boolean) result).booleanValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("booleanNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    return true;
	}
    }

    private int minArgLength = 1;
    private int maxArgLength = Integer.MAX_VALUE;

    /*
     * Get the minimum number of arguments allowed in calls to
     * {@link #isInDomain(double...)},
     * {@link #valueAt(double...)},
     * {@link #derivAt(int,double...)}, and
     * {@link #secondDerivAt(int,int,double...)}.
     * @return the minimum number of arguments
     */
    @Override
    public int minArgLength() {
	return minArgLength;
    }

    /*
     * Get the maximum number of arguments allowed in calls to
     * {@link #isInDomain(double...)},
     * {@link #valueAt(double...)},
     * {@link #derivAt(double...)}, and
     * {@link #secondDerivAt(double...)}.
     * @return the maximum number of arguments
     */
    @Override
    public int maxArgLength() {
	return maxArgLength;
    }

    /**
     * Determine if a point is within the domain of
     * this function.
     * <P>
     * The default behavior of this method assumes the domain
     * is a rectangular region and uses the methods
     * {@link #getDomainMin(int)}, {@link #getDomainMax(int)},
     * {@link #domainMinClosed(int)}, {@link #domainMaxClosed(int)},
     * to determine if the arguments represent a point in the
     * functions domain.  If the domain is not rectangular
     * with each side either in or not in the domain, then
     * this method must be overridden.  If it is not possible
     * with a reasonable amount of computation to determine that
     * a point is in the domain, an UnsupportedOperationException
     * may be thrown.  If this exception is thrown, it should be
     * thrown regardless of the arguments.
     * @param args the arguments (x<sub>0</sub>,x<sub>1</sub>,...)
     *       giving the coordinates of a point
     * @return true if the point (x<sub>0</sub>,x<sub>1</sub>,...)
     *         is in this function's domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     * @exception IllegalArgumentException an argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    @Override
    public boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException
    {
	for (int i = 0; i < args.length; i++) {
	    double x = args[i];
	    double xmin = getDomainMin(i);
	    double xmax = getDomainMax(i);
	    if (domainMinClosed(i)) {
		if (x < xmin) {
		    return false;
		}
	    } else {
		if (x <= xmin) {
		    return false;
		}
	    }
	    if (domainMaxClosed(i)) {
		if (x > xmax) {
		    return false;
		}
	    } else {
		if (x >= xmax) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Constructor when the function is provided by a script object.
     * The parameter 'object' is expected to be either an instance of
     * RealValuedFunctionVA or an object defined by a scripting
     * language with methods named "valueAt", "deri1At", and
     * secondDerivAt.  Each of these methods takes an array of real numbers
     * as its last argument, and returns a number.  The method deriv1At
     * has an integer as its first argument - an index i indicating that the
     * derivative is computed with respect to x<sub>i</sub>. The method
     * secondDerivAt has an integer as its first argument and an integer
     * as its second argument - an index i and an index j indicating the
     * the derivative is a second partial derivative with respect to
     * x<sub>i</sub> and x<sub>j</sub>.
     * For a real-valued function
     * f(x<sub>0</sub>,x<sub>1</sub>,...), these methods are defined as
     * follows:
     * <UL>
     *   <LI><CODE>valueAt</CODE> returns
     *             f(x<sub>0</sub>,x<sub>1</sub>,...)  with the values
     *             x<sub>0</sub>... stored in a Java
     *             array that provides valueAt's argument(s).
     *   <LI><CODE>derivAt</CODE> returns $\frac{\partial f}{\partial x_i}$
     *       <!-- &part;f/&part;x<sub>i</sub> -->
     *       evaluated at the point (x<sub>0</sub>,x<sub>1</sub>,
     *       ...), where i is the index into the  array
     *        indicating the argument with respect to which differentiation
     *        occures. The values
     *       x<sub>0</sub>... are stored in an  array and provide the
     *        point at which the derivative is evaluated.
     *   <LI><CODE>secondDerivAt</CODE> returns
     *       $\frac{\partial^2 f}{\partial x_i \partial x_j}$
     *       <!-- &part;<sup>2</sup>f/&part;x<sub>i</sub>&part;x<sub>j</sub> -->
     *       evaluated at the point  (x<sub>0</sub>,x<sub>1</sub>,...),
     *       where i and j are indices into the array containing
     *       the arguments and  indicate with respect to which arguments
     *       the differentiation occurs.
     *       The values x<sub>0</sub>... are stored in an array.
     * </UL>
     * Except for the integer-valued indices, all the arguments for these
     * methods are double-precision numbers. If one of these methods is not
     * defined, an UnsupportedOperationException may be thrown when the
     * current object's method of the same name is called.
     * <P>
     * The object that the scripting language provides can also define several
     * other methods:
     * <UL>
     *  <IT> <CODE>getDomainMin</CODE> returns the greatest lower
     *       bound for the i<sup>th</sup> component x<sub>i</sub> of the
     *       points in the function's domain, where i is the
     *       argument passed to this method.
     *  <IT> <CODE>domainMinClosed</CODE> returns true if there exists a point
     *       in the function's domain whose i<sup>th</sup> component is equal
     *       to the greatest lower bound for that component. The integer index
     *       i is this method's sole argument.
     *  <IT> <CODE>getDomainMax</CODE> returns the least upper bound
     *       for the i<sup>th</sup> component x<sub>i</sub> of the
     *       poinst in the function's domain, where i is the
     *       argument passed to this method.
     *  <IT> <CODE>domainMaxClosed</CODE> returns true if there exists a point
     *       in the function's domain whose i<sup>th</sup> component is equal
     *       to the least upper bound for that component. The integer index
     *       i is this method's sole argument.
     *  <IT> <CODE>isInDomain</CODE> returns true if a point is in this
     *       function's domain and false if does not.  The point is passed
     *       as an argument consisting of an array containing the coordinates
     *       of this point (x<sub>0</sub>, x<sub>1</sub>, ...).
     * </UL>
     * @param minArgLength the minimum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param maxArgLength the mmaximum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param scriptingContext the scripting context
     * @param object an object from a scripting environment defining
     *        the methods defined above or a subset of those methods
     */
    public RealValuedFunctionVA(int minArgLength, int maxArgLength,
				ScriptingContext scriptingContext,
				Object object)
    {
	this(minArgLength, maxArgLength);
	context = scriptingContext;
	this.object = object;
    }

    /**
     * Constructor when the function is provided by a script.
     * The script is expected to define up to three functions, indicated
     * by their names. These functions (when not null) must satisfy the
     * following conditions:
     * <UL>
     *   <LI>the scripting-language function whose name is the
     *       <CODE>fname</CODE> argument of this method returns
     *       f(x<sub>0</sub>,x<sub>1</sub>,...)  with the values
     *       x<sub>0</sub>... stored in a Java array that provides
     *       valueAt's argument.
     *   <LI>the scripting-language function whose name is the
     *       <CODE>derivName</CODE> argument of this method returns
     *       $\frac{\partial f}{\partial x_i}$
     *       <!-- &part;f/&part;x<sub>i</sub> --> evaluated at the point
     *       (x<sub>0</sub>,x<sub>1</sub>, ...), where i is the first
     *       argument and the values x<sub>0</sub>... are stored in a
     *       Java array that provides the second argument for derivAt.
     *   <LI>the scripting-language function whose name is the
     *       <CODE>secondDerivName</CODE> argument of this method returns
     *       $\frac{\partial^2 f}{\partial x_i \partial x_j}$
     *       <!-- &part;<sup>2</sup>f/&part;x<sub>i</sub>&part;x<sub>j</sub> -->
     *       evaluated at the point (x<sub>0</sub>,x<sub>1</sub>,...),
     *       where i is the first argument, j is the second argument,
     *       and the values x<sub>0</sub>... are stored in a Java
     *       array that provides the third argument for secondDerivAt.
     * </UL>
     * @param minArgLength the minimum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param maxArgLength the mmaximum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param scriptingContext the scripting context
     * @param fname the name of a scripting-language function; null if
     *        the valueAt method is not supported
     * @param derivName the name of a scripting-language function
     *        providing the desired function's first derivative; null
     *        if the deriv1At method is not supported
     * @param secondDerivName the name of a scripting-language
     *        function providing the desired function's second
     *        derivative; null if the secondDerivAt method is not
     *        supported
a     */
    public RealValuedFunctionVA(int minArgLength, int maxArgLength,
				ScriptingContext scriptingContext,
				String fname,
				String derivName,
				String secondDerivName)
    {
	this(minArgLength,maxArgLength);
	context = scriptingContext;
	this.fname = fname;
	this.derivName = derivName;
	this.secondDerivName = secondDerivName;
    }

    /**
     * Call the function.
     * @param args the function's arguments
     * @return the value of the function for the given arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA)object;
		return f.valueAt(args);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (fname != null) {
		result = context.callScriptFunction(fname, args);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "valueAt",
						  args);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Compute the Jacobian of this function.
     * Because this is a limiting case (a 1 by n matrix, where n is the
     * number of arguments), the Jacobian is stored as a one-dimensional
     * array.  Java represents two dimensional arrays as nested one
     * dimensional arrays, so to compute the Jacobian for a vector of m
     * functions, one would could use the following code:
     * <blockquote><pre>
     *     RealValuedFunctionVA[] farray = {f1, f2, ... fm};
     *     double[][] J = new double[farray.length][];
     *     for (int i = 0; i &lt; farray.length; i++) {
     *         J[i] = farray[i].jacobian(x1, x2, x3, ...);
     *     }
     * </pre></blockquote>
     * @param args the arguments to the function.
     * @return the Jacobian
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double[] jacobian(double ... args)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	double[] results = new double[args.length];
	for (int i = 0; i < args.length; i++) {
	    results[i] = derivAt(i, args);
	}
	return results;
    }

    /**
     * Compute the Jacobian of this function, ignoring the derivatives for
     * some initial arguments.
     * Because this is a limiting case (a 1 by n matrix, where n is
     * the number of arguments minus a specified offset), the Jacobian
     * is stored as a one-dimensional array. The length of the vector
     * is reduced by stripping off the first elements of the vector
     * that would be returned by {@link #jacobian(double...)}.
     * <P>
     * Java represents two dimensional arrays as nested one
     * dimensional arrays, so to compute the Jacobian for a vector of m
     * functions, one would could use the following code:
     * <blockquote><pre>
     *     RealValuedFunctionVA[] farray = {f1, f2, ... fm};
     *     double[][] J = new double[farray.length][];
     *     for (int i = 0; i &lt; farray.length; i++) {
     *         J[i] = farray[i].jacobian(1,x,beta0, beta1, ...);
     *     }
     * </pre></blockquote>
     * if the functions f1, ... fm are functions of x, &beta;<sub>1</sub>,
     * &beta;<sub>2</sub>, ..., the call to jacobian(1, x, ...) causes
     * the argument x to be treated as a constant.
     * <P>
     * Note: this method is useful when using the Gauss-Newton method for
     * finding the parameters for a non-linear least-squares fit.
     * @param offset the offset determining the variable at which to start
     *        differentiating
     * @param args the arguments to the function.
     * @return the Jacobian
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double[] jacobian(int offset, double ... args)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	double[] results = new double[args.length-offset];
	for (int i = offset; i < args.length; i++) {
	    int j = i - offset;
	    results[j] = derivAt(i, args);
	}
	return results;
    }

    /**
     * Evaluate the partial derivative $\frac{\partial f}{\partial x_i}$
     * <!--&part;f / &part;x<sub>i</sub> -->
     * for a function f(x<sub>0</sub>,x<sub>1</sub>, ...).
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument, numbered from zero
     * @param args the function f's arguments
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double derivAt(int i, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA)object;
		return f.derivAt(i, args);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (derivName != null) {
		result = context.callScriptFunction(derivName, i, args);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "derivAt",
						  i, args);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Evaluate the partial derivative
     * $\frac{\partial^2 f}{\partial x_i \partial z_j}$
     * <!-- &part;<sup>2</sup>f / &part;x<sub>1</sub>&part;x<sub>1</sub> -->
     * for a function f(x<sub>0</sub>,x<sub>1</sub>, ...).
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument, numbered from 0
     * @param j the index indicating that the partial derivative is computed
     *        for the j<sup>th</sup> argument, numbered from 0
     * @param args the function f's arguments
     * @return the value of the partial derivative 
     *         $\frac{\partial^2 f}{\partial x_i \partial z_j}$
     *         <!-- &part;<sup>2</sup>f / &part;x<sub>i</sub>&part;x<sub>j</sub> -->
     *         for the given arguments x<sub>0</sub>, x<sub>1</sub>, ...
     * @exception IllegalArgumentException the function's arguments
     *            were out of range
     * @exception UnsupportedOperationException the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double secondDerivAt(int i, int j, double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	if (context == null && object == null)
	    throw new UnsupportedOperationException
		(errorMsg("functionNotSupported"));
	try {
	    if (object != null && object instanceof RealValuedFunctionVA) {
		RealValuedFunctionVA f = (RealValuedFunctionVA)object;
		return f.secondDerivAt(i,j, args);
	    }
	    if (context == null) throw new UnsupportedOperationException
				     (errorMsg("functionNotSupported"));
	    Object result;
	    if (secondDerivName != null) {
		result = context.callScriptFunction(secondDerivName,
						    i, j, args);
	    } else {
		if (object == null) throw new UnsupportedOperationException
					(errorMsg("functionNotSupported"));
		result = context.callScriptMethod(object, "secondDerivAt",
						  i, j, args);
	    }
	    if (result instanceof Number) {
		return ((Number) result).doubleValue();
	    } else {
		throw new UnsupportedOperationException
		    (errorMsg("numberNotReturned"));
	    }
	} catch (ScriptException e) {
	    String msg = errorMsg("callFailsArgVA");
	    throw new IllegalArgumentException(msg, e);
	} catch (NoSuchMethodException e) {
	    String msg = errorMsg("opNotSupported");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    /**
     * Class representing a real-valued function of the form
     * f(x,&beta;<sub>1</sub>,...) = &sum;<sub>i</sub> &beta;<sub>i</sub>f<sub>i</sub>(x).
     * The function this class represents is a linear function of all its
     * arguments except the first.  The functions in the sum are
     * sometimes called basis functions as they form the basis for a
     * vector space.
     */
    public static final class Linear extends RealValuedFunctionVA {
	RealValuedFunction[] farray;

	private double dmin = -Double.MAX_VALUE;
	private double dmax = Double.MAX_VALUE;
	private boolean dminClosed = true;
	private boolean dmaxClosed = true;
	private int n;

	/**
	 * Get the basis functions&mdash;the functions that will be combined
	 * linearly.
	 * These are the real-valued functions passed to the constructor,
	 * returned in the same order.
	 * @return the basis functions
	 */
	public RealValuedFunction[] getBasis() {
	    return farray.clone();
	}

	/**
	 * Constructor.
	 * The arguments are the basis functions. The basis functions must
	 * implement derivatives if this function's derivatives with
	 * respect to its first argument are to be evaluated.
	 * <P>
	 * The number of double-precision arguments that should be passed to
	 * {@link #valueAt(double...)}, {@link #derivAt(int,double...)},
	 * and {@link #secondDerivAt(int,int,double...)} will be the next
	 * integer larger than the number of arguments passed to this
	 * constructor. The first double-precision argument will be passed
	 * to the basis functions and the remainder will represent parameters
	 * for the basis functions, both in the same order as that used
	 * by this constructor.
	 * @param<T> an interface that extends {@link RealValuedFunctOps}
	 * @param args a non-zero number of real valued functions of
	 *        one argument
	 * @exception IllegalArgumentException an argument was null or
	 *            no arguments were provided
	 */
	@SafeVarargs
	public <T extends RealValuedFunctOps> Linear(T... args)
	    throws IllegalArgumentException
	{
	    super(args.length+1,args.length+1);
	    if (args.length == 0) {
		throw new IllegalArgumentException
		    (errorMsg("zeroArgumentError"));
	    }
	    n = args.length+1;
	    farray = new RealValuedFunction[args.length];
	    for (int i = 0; i < args.length; i++) {
		RealValuedFunctOps fops = args[i];
		if (fops == null) {
		    throw new IllegalArgumentException
			(errorMsg("nullArg"));
		}
		RealValuedFunction f = (fops instanceof RealValuedFunction)?
		    (RealValuedFunction)fops: new RealValuedFunction(fops);
		farray[i] = f;
		double tmp = f.getDomainMin();
		if (tmp > dmin) {
		    dmin = tmp;
		    dminClosed = f.domainMinClosed();
		} else if (tmp == dmin) {
		    if (!f.domainMinClosed()) {
			dminClosed = false;
		    }
		}
		tmp = f.getDomainMax();
		if (tmp < dmax) {
		    dmax = tmp;
		    dmaxClosed = f.domainMaxClosed();
		} else if (tmp == dmax) {
		    if (!f.domainMaxClosed()) {
			dmaxClosed = false;
		    }
		}
	    }
	}

	@Override
	public double getDomainMin(int argIndex)
	    throws IllegalArgumentException, IllegalStateException
	{
	    if (argIndex < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative", argIndex));
	    }
	    if (argIndex == 0) {
		return dmin;
	    } else if (argIndex < n) {
		return -Double.MAX_VALUE;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("argTooLarge", argIndex));
	    }
	}

	@Override
	public double getDomainMax(int argIndex)
	    throws IllegalArgumentException, IllegalStateException
	{
	    if (argIndex < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative", argIndex));
	    }
	    if (argIndex == 0) {
		return dmax;
	    } else if (argIndex < n) {
		return Double.MAX_VALUE;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("argTooLarge", argIndex));
	    }
	}

	@Override
	public boolean domainMinClosed(int argIndex)
	    throws IllegalArgumentException, IllegalStateException
	{
	    if (argIndex < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative", argIndex));
	    }
	    if (argIndex == 0) {
		return dminClosed;
	    } else if (argIndex < n) {
		return true;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("argTooLarge", argIndex));
	    }
	}

	@Override
	public boolean domainMaxClosed(int argIndex)
	    throws IllegalArgumentException, IllegalStateException
	{
	    if (argIndex < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative", argIndex));
	    }
	    if (argIndex == 0) {
		return dmaxClosed;
	    } else if (argIndex < n) {
		return true;
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("argTooLarge", argIndex));
	    }
	}

	@Override
	public double valueAt(double... args) throws
	    IllegalArgumentException, UnsupportedOperationException,
	    IllegalStateException
	{
	    if (args.length != n) {
		throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	    }
	    double x = args[0];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int i = 1; i < n; i++) {
		int index = i-1;
		double y = (args[i]*farray[index].valueAt(x)) - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    return state.total;
	}

	@Override
	public double derivAt(int i, double... args) throws
	    IllegalArgumentException, UnsupportedOperationException,
	    IllegalStateException
	{
	    if (args.length != n) {
		throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	    }
	    double x = args[0];
	    if (i == 0) {
		Adder.Kahan adder = new Adder.Kahan();
		Adder.Kahan.State state = adder.getState();
		for (int ind = 1; ind < n; ind++) {
		    int index = ind-1;
		    double y = (args[ind]*farray[index].derivAt(x)) - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
		return state.total;
	    } else {
		return farray[i-1].valueAt(x);
	    }
	}

	@Override
	public double secondDerivAt(int i, int j, double... args) throws
	    IllegalArgumentException, UnsupportedOperationException,
	    IllegalStateException
	{
	    if (args.length != n) {
		throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	    }
	    double x = args[0];
	    int k;
	    if (i == 0 && j == 0) {
		Adder.Kahan adder = new Adder.Kahan();
		Adder.Kahan.State state = adder.getState();
		for (int ind = 1; ind < n; ind++) {
		    int index = ind-1;
		    double y = (args[ind]*farray[index].secondDerivAt(x))
			- state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
		return state.total;
	    } else if (i == 0) {
		k = j - 1;
	    } else if (j == 0) {
		k = i - 1;
	    } else {
		return 0.0;
	    }
	    return farray[k].derivAt(x);
	}
    }
}

//  LocalWords:  exbundle valueAt derivAt secondDerivAt getDomainMin
//  LocalWords:  getDomainMax getDomainMinClosed getDomainMaxClosed
//  LocalWords:  isInDomain UnsupportedOperationException EMCAScript
//  LocalWords:  blockquote pre importClass ourObject setFunction th
//  LocalWords:  RealValuedFunctionVA funct RealValuedFunction args
//  LocalWords:  minArgLength maxArgLength mmaximum argIndex deri
//  LocalWords:  IllegalArgumentException functionNotSupported deriv
//  LocalWords:  numberNotReturned callFailsArgVA booleanNotReturned
//  LocalWords:  domainMinClosed domainMaxClosed valueAt's poinst
//  LocalWords:  scriptingContext scriting fname derivName Jacobian
//  LocalWords:  secondDerivName opNotSupported farray
