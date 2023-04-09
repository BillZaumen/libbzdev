package org.bzdev.obnaming;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;

/**
 * Interface for named objects that provide a function.
 *
 */
public interface NamedFunctionOps extends NamedObjectOps, RealValuedFunctOps {

    /**
     * Get the RealValuedFunction associated with this named object.
     * @return the function; null if not defined
     */
    RealValuedFunction getFunction();

    /**
     * Get the minimum value in the domain of the function.
     * @return the minimum value
     */
    double getDomainMin();

    /**
     * Determine if the minimum value for the domain is in the domain
     * @return true if the domain's minimum value is in the domain; false
     *         otherwise
     */
    boolean domainMinClosed();

    /**
     * Get the maximum value in the domain of the function.
     * @return the maximum value
     */
    double getDomainMax();

    /**
     * Determine if the maximum value for the domain is in the domain
     * @return true if the maximum value for the domain is in the domain;
     *         false otherwise
     */
    boolean domainMaxClosed();

    /**
     * Call the function.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double valueAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the function's first derivative.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double derivAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException;


    /**
     * Evaluate the function's second derivative.
     * @param arg the function's argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double secondDerivAt(double arg) throws
	IllegalArgumentException, UnsupportedOperationException;

}

//  LocalWords:  RealValuedFunction arg IllegalArgumentException
//  LocalWords:  UnsupportedOperationException
