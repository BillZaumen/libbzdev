package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Vector-valued functions with a variable number of arguments.
 */
public abstract class VectorValuedFunctionVA implements VADomainOps {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * The dimenion for the vectors that will be returned.
     * <P>
     * This value is read-only.
     */
    protected int dim;

    /**
     * Get the dimension of the vectors returned by a vector-valued
     * function.
     * @return the dimension of the vectors returned by this vector-valued
     *         function
     */
   public int getDimension() {
	return dim;
    }

    /**
     * Constructor.
     * @param dim the dimension of the arrays that will be returned by
     *        methods that evaluate the function or its derivatives.
     * @param minArgLength the minimum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     * @param maxArgLength the mmaximum number of double-precision arguments
     *        for methods that allow a variable number of arguments;
     *        -1 for the default
     */
    protected VectorValuedFunctionVA(int dim,
				     int minArgLength,
				     int maxArgLength)
    {
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
	this.dim = dim;
    }

    @Override
    public double getDomainMin(int argIndex)
	throws IllegalArgumentException
    {
	return -Double.MAX_VALUE;
    }

    @Override
    public double getDomainMax(int argIndex) throws IllegalArgumentException {
	return Double.MAX_VALUE;
    }

    @Override
    public boolean domainMinClosed(int argIndex)
	throws IllegalArgumentException
    {
	return true;
    }

    @Override
    public boolean domainMaxClosed(int argIndex)
	throws IllegalArgumentException
    {
	return true;
    }

    @Override
    public boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException
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

    private int minArgLength = 1;
    private int maxArgLength = Integer.MAX_VALUE;

    @Override
    public int minArgLength() {
	return minArgLength;
    }

    @Override
    public int maxArgLength() {
	return maxArgLength;
    }

    /**
     * Call the function.
     * <P>
     * A subclass must implement
     * {@link #valueAt(double[],int,double...)}, which this method
     * calls to compute the value.
     * @param args the function's arguments
     * @return the value of the function for the given arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public final double[] valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	double[] array = new double[dim];
	valueAt(array, 0, args);
	return array;
    }

    /**
     * Call the function, storing the results in an array.
     * @param array the array used to store the results.
     * @param offset the offset into the array at which to store
     *        the results.
     * @param args the function's arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public void valueAt(double[] array, int offset, double... args) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	throw new UnsupportedOperationException
	    (errorMsg("functionNotSupported"));
    }


    /**
     * Evaluate the first partial derivative
     * &part;f / &part;x<sub>i</sub>
     * for a function f(x<sub>0</sub>,x<sub>1</sub>, ...).
     * <P>
     * A subclass must implement
     * {@link #derivAt(int,double[],int,double...)}, which this method
     * calls to compute a derivative.
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument
     * @param args the function f's arguments
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public final double[] derivAt(int i, double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	double[] array = new double[dim];
	derivAt(i, array, 0, args);
	return array;
    }

    /**
     * Evaluate the first partial derivative, storing the results
     * &part;f / &part;x<sub>i</sub>
     * for a function f(x<sub>0</sub>,x<sub>1</sub>, ...).
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument
     * @param array the array used to store the results.
     * @param offset the offset into the array at which to store
     *        the results.
     * @param args the function f's arguments
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public void derivAt(int i, double[] array, int offset, double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	throw new UnsupportedOperationException
	    (errorMsg("functionNotSupported"));
    }

    /**
     * Evaluate the second partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>1</sub>&part;x<sub>1</sub>
     * for a function f(x<sub>1</sub>x<sub>2</sub>).
     * <P>
     * A subclass must implement
     * {@link #secondDerivAt(int,int,double[],int,double...)}, which this method
     * calls to compute a derivative.
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument
     * @param j the index indicating that the partial derivative is computed
     *        for the j<sup>th</sup> argument
     * @param args the function f's arguments
     * @return the value of the partial derivative 
     *         &part;<sup>2</sup>f / &part;x<sub>i</sub>&part;x<sub>j</sub>
     *         for the given arguments x<sub>0</sub>, x<sub>1</sub>, ...
     * @exception IllegalArgumentException the function's arguments
     *            were out of range
     * @exception UnsupportedOperationException the operation is
     *            not supported.
     */
    public final double[] secondDerivAt(int i, int j, double... args) throws
	IllegalArgumentException, UnsupportedOperationException
    {
	double[] array = new double[dim];
	secondDerivAt(i, j, array, 0, args);
	return array;
    }

    /**
     * Evaluate the second partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>1</sub>&part;x<sub>1</sub>
     * for a function f(x<sub>1</sub>x<sub>2</sub>), storing the results
     * in an array.
     * @param i the index indicating that the partial derivative is computed
     *        for the i<sup>th</sup> argument
     * @param j the index indicating that the partial derivative is computed
     *        for the j<sup>th</sup> argument
     * @param array the array used to store the results.
     * @param offset the offset into the array at which to store
     *        the results.
     * @param args the function f's arguments
     * @exception IllegalArgumentException the function's arguments
     *            were out of range
     * @exception UnsupportedOperationException the operation is
     *            not supported.
     */
    public void secondDerivAt(int i, int j, double[] array, int offset,
				  double... args)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	throw new UnsupportedOperationException
	    (errorMsg("functionNotSupported"));
    }


}
//  LocalWords:  exbundle minArgLength maxArgLength mmaximum valueAt
//  LocalWords:  args IllegalArgumentException functionNotSupported
//  LocalWords:  UnsupportedOperationException derivAt th f's
//  LocalWords:  secondDerivAt
