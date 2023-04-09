package org.bzdev.math.rv;

/**
 * Random Variable Interface.
 * This is interface is the root of a hierarchy of interfaces that
 * are paired with a class hierarchy whose base class is
 * {@link RandomVariable} and is provided for cases where it is not
 * possible to directly create a subclass of {@link RandomVariable}.
 * The class {@link RandomVariable} itself implements this interface.
 * @see RandomVariable
 */
public interface RandomVariableOps<T> {
    /**
     * Set the minimum value for a random variable.
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
     * Tighten the minimum value for a random variable.
     * If there is no minimum value, it will be set.  Otherwise the
     * minimum of the allowed range will not decrease.
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
     * Tighten the minimum value for a random variable given a string.
     * If there is no minimum value, it will be set.  Otherwise the
     * minimum of the allowed range will not decrease. The string
     * argument is a number in a format acceptable to the constructors
     * for Integer, Long, or Double as appropriate.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    void tightenMinimumS(String min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;


    /**
     * Set the maximum value for a random variable.
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
     * Tighten the maximum value for a random variable.
     * If there is no maximum value, it will be set.  Otherwise the
     * maximum of the allowed range will not increase.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void tightenMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Tighten the maximum value for a random variable  given a string.
     * If there is no maximum value, it will be set.  Otherwise the
     * maximum of the allowed range will not increase. The string
     * argument is a number in a format acceptable to the constructors
     * for Integer, Long, or Double as appropriate.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    void tightenMaximumS(String max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Get the next value for a random variable.
     * In general, each value will be independent of the last.
     * @return the next value
     * @exception RandomVariableException if the next value could
     *             not be generated
     */
    T next() throws RandomVariableException;

    /**
     * Get the lower bound on the values that can be generated.
     * Some random numbers do not have an ordering, so that null
     * will always be returned in that case.
     * @return the lower bound; null if there is none
     */
    T getMinimum();

    /**
     * Determine if a random variable's lower bound can be generated.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    Boolean getMinimumClosed();

    /**
     * Get the upper bound on the values that can be generated.
     * Some random numbers do not have an ordering, so that null
     * will always be returned in that case.
     * @return the upper bound; null if there is none
     */
    T getMaximum();

    /**
     * Determine if a random variable's upper bound can be generated.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    Boolean getMaximumClosed();
}

//  LocalWords:  RandomVariable UnsupportedOperationException
//  LocalWords:  IllegalArgumentException
