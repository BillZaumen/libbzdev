package org.bzdev.math;


/**
 * Operations for function domains when there is a variable number of
 * arguments.
 */
public interface VADomainOps {

    /**
     * Get the minimum number of arguments allowed in calls to
     * methods whose arguments are in the domain of a function implementing
     * this interface.  A subclass must not return a value less than 1.
     * @return the minimum number of arguments
     */
    public int minArgLength();

    /*
     * Get the maximum number of arguments allowed in calls to
     * methods whose arguments are in the domain of a function implementing
     * this interface.  
     * @return the maximum number of arguments
     */
    public int maxArgLength();

    /**
     * Get the greatest lower bound of the i<sup>th</sup> argument when the
     * arguments are in the domain of the function implementing this interface.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return the minimum value
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double getDomainMin(int argIndex) throws IllegalArgumentException,
					     IllegalStateException;

    /**
     * Get the least upper bound of the i<sup>th</sup> argument when the
     * arguments are in the domain of the function.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return the maximum value
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public double getDomainMax(int argIndex) throws IllegalArgumentException,
						    IllegalStateException;

    /**
     * Determine if the domain minimum for the i<sup>th</sup> argument, when the
     * arguments are in the domain of the function, is in the function's domain.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMinClosed(int argIndex)
	throws IllegalArgumentException, IllegalStateException;

   /**
     * Determine if the domain maximum for the i<sup>th</sup> argument, when the
     * arguments are in the domain of the function, is in the function's domain.
     * @param argIndex the index determining the argument for which
     *        this method applies (0<sup>th</sup>, 1<sup>st</sup>, ...)
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    public boolean domainMaxClosed(int argIndex)
	throws IllegalArgumentException, IllegalStateException;

    /**
     * Determine if a sequence of arguments represents arguments  that
     * are in the domain of the function implementing this interface.
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
    public boolean isInDomain(double... args)
	throws UnsupportedOperationException, IllegalArgumentException,
	       IllegalStateException;
}

//  LocalWords:  th argIndex IllegalArgumentException
//  LocalWords:  IllegalStateException
