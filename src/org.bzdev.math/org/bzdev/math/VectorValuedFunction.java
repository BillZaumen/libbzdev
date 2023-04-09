package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Base class for vector-valued functions with one argument.
 * The vectors have real components, as does the argument, all of
 * which are a double-precision number (type "double").
 * <P>
 * Subclasses must implement the following method:
 * <ul>
 *    <li> {@link VectorValuedFunction#valueAt(double[],int,double)}. This
 *      method is expected to throw an IllegalArgumentException if its arguments
 *      are out of range.
 * </ul>
 * <p>
 * If the default behavior (throwing an UnsupportedOperationException)
 * is not appropriate, subclasses should implement one or more of the
 * following methods:
 * <ul>
 *    <li> {@link VectorValuedFunction#derivAt(double[],int,double)}. This
 *        method throws an UnsupportedOperationException by default.
 *    <li> {@link VectorValuedFunction#secondDerivAt(double[],int,double)}.
 *        This method throws an UnsupportedOperationException by default.
 *    <li> {@link RealValuedDomainOps#getDomainMin()}. The default is
 *      -Double.MAX_VALUE.
 *    <li> {@link RealValuedDomainOps#getDomainMax()}.  The default is
 *      Double.MAX_VALUE.
 *    <li> {@link RealValuedDomainOps#domainMinClosed()}. The default is
 *     "true".
 *    <li> {@link RealValuedDomainOps#domainMaxClosed()}. The default is
 *     "true".
 *    <li> {@link RealValuedDomainOps#isInDomain(double)}. The default
 *     checks that the argument is within the range specified by
 *     the domain minimum and maximum, with the end points included or not
 *     depending on whether the end points are closed or open respectively.
 * </ul>
 */
public abstract class VectorValuedFunction extends VectorValuedFunctionVA
    implements RealValuedDomainOps
{

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    // private int dim;

    /**
     * Constructor.
     * @param dim the dimension of the vectors that will be returned by
     *        various methods.
     */
    protected VectorValuedFunction(int dim) {
	super(dim,1,1);
	if (dim < 1) {
	    throw new IllegalArgumentException
		(errorMsg("firstArgNotPositive", dim));
	}
	// this.dim = dim;  (dim is protected so we inherit the field).
    }

    @Override
    public final double getDomainMin(int argIndex)
	throws IllegalArgumentException
    {
	if (argIndex != 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeI", argIndex));
	}
	return getDomainMin();
    }

    @Override
    public final double getDomainMax(int argIndex)
	throws IllegalArgumentException
    {
	if (argIndex != 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeI", argIndex));
	}
	return getDomainMax();
    }

    @Override
    public final boolean domainMinClosed(int argIndex)
	throws IllegalArgumentException
    {
	if (argIndex != 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeI", argIndex));
	}
	return domainMinClosed();
    }

    @Override
    public boolean domainMaxClosed(int argIndex)
	throws IllegalArgumentException
    {
	if (argIndex != 0) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeI", argIndex));
	}
	return domainMaxClosed();
    }
    
    @Override
    public boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	return isInDomain(args[0]);
    }

    @Override
    public double getDomainMin() {
	return -Double.MAX_VALUE;
    }

    @Override
    public double getDomainMax() {
	return Double.MAX_VALUE;
    }

    @Override
    public boolean domainMinClosed() {return true;}

    @Override
    public boolean domainMaxClosed() {return true;}

    @Override
    public boolean isInDomain(double x)
 	throws UnsupportedOperationException
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

    @Override
    public final void valueAt(double[] array, int offset, double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	valueAt(array, offset, args[0]);
    }

    @Override
    public final void derivAt(int i, double[] array, int offset, double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("firstArgNotZero", i));
	}
	derivAt(array, offset, args[0]);
    }

    public final void secondDerivAt(int i, int j,
				    double[] array, int offset,
				    double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (args.length != 1) {
	    throw new IllegalArgumentException(errorMsg("wrongNumberArgs"));
	}
	if (i != 0 || j != 0) {
	    throw new IllegalArgumentException
		(errorMsg("first2ArgsMustBeZero"));
	}
	secondDerivAt(array, offset, args[0]);
    }

   /**
     * Call the function.
     * @param u the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     */
    public final double[] valueAt(double u)  throws IllegalArgumentException 
    {
	double[] array = new double[dim];
	valueAt(array, 0, u);
	return array;
    }

     /**
     * Call the function, storing the value in an array.
     * @param array the array used to store the results of the
     *        function call.
     * @param offset the offset into the array at which to store
     *        the results of the function call.
     * @param u the function's argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     */
    public abstract void valueAt(double[] array, int offset, double u)
	throws IllegalArgumentException;


    /**
     * Evaluate the function's first derivative.
     * @param u the function's argument
     * @return the value of the function's derivative for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public final double[] derivAt(double u)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	double[] array = new double[dim];
	derivAt(array, 0, u);
	return array;
    }
    
    /**
     * Evaluate the function's first derivative, storing the results.
     * @param array the array used to store the results.
     * @param offset the offset into the array at which to store
     *        the results.
     * @param u the function's argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public void derivAt( double[] array, int offset, double u)
	throws IllegalArgumentException, UnsupportedOperationException {
	throw new UnsupportedOperationException
	    (errorMsg("functionNotSupported"));
    }



    /**
     * Evaluate the function's second derivative.
     * @param u the function's argument
     * @return the value of the function's second derivative for the
     *         given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public final double[] secondDerivAt(double u)
	throws IllegalArgumentException, UnsupportedOperationException
    { 
	double[] array = new double[dim];
	secondDerivAt(array, 0, u);
	return array;
    }

    /**
     * Evaluate the function's second derivative, storing the results.
     * @param array the array used to store the results.
     * @param offset the offset into the array at which to store
     *        the results.
     * @param u the function's argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public void secondDerivAt(double[] array, int offset, double u)
	throws IllegalArgumentException, UnsupportedOperationException {
	throw new UnsupportedOperationException
	    (errorMsg("functionNotSupported"));
    }
}

//  LocalWords:  exbundle Subclasses ul li VectorValuedFunction
//  LocalWords:  valueAt IllegalArgumentException subclasses derivAt
//  LocalWords:  UnsupportedOperationException secondDerivAt
//  LocalWords:  RealValuedDomainOps getDomainMin getDomainMax
//  LocalWords:  domainMinClosed domainMaxClosed isInDomain
//  LocalWords:  firstArgNotPositive argOutOfRangeI wrongNumberArgs
//  LocalWords:  firstArgNotZero ArgsMustBeZero functionNotSupported
