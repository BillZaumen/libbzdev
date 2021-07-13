package org.bzdev.math;

/**
 * Operations for the domain of a function with a single real-valued argument.
 * This interface is provided primarily because the documentation is common to
 * multiple classes.
 */
public interface RealValuedDomainOps {

    /**
     * Get the minimum value in the domain of the function.
     * @return the minimum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double getDomainMin() throws IllegalStateException;

    /**
     * Get the maximum value in the domain of the function.
     * @return the maximum value
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double getDomainMax() throws IllegalStateException;

    /**
     * Determine if the domain minimum is in the domain.
     * @return true if the domain minimum is in the domain; false if
     *         it is the greatest lower bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    boolean domainMinClosed() throws IllegalStateException;

    /**
     * Determine if the domain maximum is in the domain.
     * @return true if the domain maximum is in the domain; false if
     *         it is the least upper bound for the domain
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    boolean domainMaxClosed() throws IllegalStateException;

    /**
     * Determine if an argument is within the domain of
     * a function.
     * @param x a value to test
     * @return true if  x is in this function's domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    boolean isInDomain(double x) throws UnsupportedOperationException,
					IllegalStateException ;
}

//  LocalWords:  IllegalStateException UnsupportedOperationException
