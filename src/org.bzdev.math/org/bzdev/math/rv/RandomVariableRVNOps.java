package org.bzdev.math.rv;
/**
 * Interface for random variables that generates another random variable
 *  that in turn generates numbers.
 * The type parameter T indicates the type of number and the type
 * parameter RV indicates the type of the random variable that generates
 * the number.
 */
public interface RandomVariableRVNOps<T extends Number, 
				      RV extends RandomVariableOps<T>> 
    extends RandomVariableRVOps<T, RV> 
{
    /**
     * Get the minimum value that will be set for random variables
     * that the current object generates.
     * @return the minimum value
     */
    T getMinimumRV();

    /**
     * Get the maximum value that will be set for random variables
     * that the current object generates.
     * @return the maximum value.
     */
    T getMaximumRV();

    /**
     * Determine if a random variable's lower bound can be generated.
     * The value returned applies to the bound that will be set for
     * the random numbers that this class generates.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    Boolean getMinimumClosed();

    /**
     * Determine if a random variable's upper bound can be generated.
     * The value returned applies to the bound that will be set for
     * the random numbers that this class generates.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    Boolean getMaximumClosed();

    /**
     * Get the next value for a random variable.
     * @return the next value (a random variable)
     * @exception RandomVariableException a failure occurred while 
     *            attempting to set a range restriction
     */
    RV next() throws RandomVariableException;

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
    void setMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException;

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
    void setMaximum(T max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Set the manimum value for a random variable, provided the new
     * value is higher than the previous value.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    void tightenMinimum(T min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Set the maximum value for a random variable, provided the new
     * value is lower than the previous value.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    void tightenMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException;
}

//  LocalWords:  RandomVariableException IllegalArgumentException
//  LocalWords:  UnsupportedOperationException
