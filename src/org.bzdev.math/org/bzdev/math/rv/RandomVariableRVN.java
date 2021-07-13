package org.bzdev.math.rv;
/**
 * Random variable that generates another random variable that generates
 * numbers.
 * The type parameter T indicates the type of number. Subclasses must
 * implement the methods {@link RandomVariableRVN#cmp cmp} and
 * {@link RandomVariableRVN#doNext() doNext}.
 * Unless T is a type other than Integer, Long, or Double, one should
 * not create a subclass that extends this class.
 */
abstract public class RandomVariableRVN<T extends Number, 
				       RV extends RandomVariable<T>> 
    extends RandomVariableRV<T, RV> implements RandomVariableRVNOps<T,RV>
{
    T min = null;
    boolean minClosed = true;
    T max = null;
    boolean maxClosed = true;

    /**
     * Get the minimum value that will be set for random variables
     * that the current object generates.
     * @return the minimum value.
     */
    public T getMinimumRV() {
	return min;
    }

    /**
     * Get the maximum value that will be set for random variables
     * that the current object generates.
     * @return the maximum value.
     */
    public T getMaximumRV() {
	return max;
    }

    /**
     * Determine if a random variable's lower bound can be generated.
     * The value returned applies to the bound that will be set for
     * the random numbers that this class generates.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    public Boolean getMinimumClosed() {
	return minClosed? Boolean.TRUE: Boolean.FALSE;
    }

    /**
     * Determine if a random variable's upper bound can be generated.
     * The value returned applies to the bound that will be set for
     * the random numbers that this class generates.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    public Boolean getMaximumClosed() {
	return maxClosed? Boolean.TRUE: Boolean.FALSE;
    }

    /**
     * Get the next value for a random variable.
     * The next value is actually computed by calling
     * {@link #doNext()}, which subclasses must
     * implement and which should either create a new random variable
     * or clone an existing one.
     * Direct subclasses will in general have to include the method
     * definition "public RV next() {return super.next();}" with RV
     * replaced with the type or type parameter appearing in the RV
     * slot above in order to prevent issues with type-erasures. This
     * is handled by the classes
     * {@link IntegerRandomVariableRV IntegerRandomVariableRV},
     * {@link LongRandomVariableRV LongRandomVariableRV}, and
     * {@link DoubleRandomVariableRV DoubleRandomVariableRV}
     *
     * @return the next random variable
     * @exception RandomVariableException a failure occurred while 
     *            attempting to set a range restriction
     */
    public RV next() throws RandomVariableException {
	RV rv = doNext();
	try {
	    if (min != null) rv.setMinimum(min, minClosed);
	    if (max != null) rv.setMaximum(max, maxClosed);
	} catch (UnsupportedOperationException eu) {
	    throw new RandomVariableException(eu);
	} catch (IllegalArgumentException ei) {
	    throw new RandomVariableException(ei);
	}
	return rv;
    }

    /**
     * Get the next value for a random variable.
     * In general, each value will be independent of the last.
     * @exception RandomVariableException the next random variable
     *            could not be generated.
     */
    abstract protected RV doNext() throws RandomVariableException;

    /**
     * Set the minimum value for a random variable.
     * The minimum value will be set for the random variable returned by
     * a call to <code>next()</code>
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void setMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException 
    {
	this.min = min;
	this.minClosed = closed;
    }

    /**
     * Set the maximum value for a random variable.
     * The maximum value will be set for the random variable returned by
     * a call to <code>next()</code>
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void setMaximum(T max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	this.max = max;
	this.maxClosed = closed;
    }


    /**
     * Compare two numbers of type T.
     * @return a negative value if v1 $lt; v2, 0 if v1 == v2,
     *         and a positive value if v1 &gt; v2
     */
    abstract protected int cmp(T v1, T v2);

    public void tightenMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (this.min != null) {
	    int test = cmp(this.min, min);
	    if (test == -1) {
		this.min = min;
		minClosed = closed;
	    } else if (test == 0) {
		if (minClosed) minClosed = closed;
	    }
	} else {
	    setMinimum(min, closed);
	}
    }

    public void tightenMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (this.max != null) {
	    int test = cmp(this.max, max);
	    if (test == 1) {
		this.max = max;
		minClosed = closed;
	    } else if (test == 0) {
		if (minClosed) minClosed = closed;
	    }
	} else {
	    setMaximum(max, closed);
	}
    }
}

//  LocalWords:  Subclasses RandomVariableRVN cmp doNext subclasses
//  LocalWords:  IntegerRandomVariableRV LongRandomVariableRV lt
//  LocalWords:  DoubleRandomVariableRV RandomVariableException
//  LocalWords:  UnsupportedOperationException
//  LocalWords:  IllegalArgumentException
