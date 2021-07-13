package org.bzdev.obnaming;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.RealValuedFunctTwoOps;

/**
 * Interface for named objects that provide a function of two arguments.
 *
 */
public interface NamedFunctionTwoOps
    extends NamedObjectOps, RealValuedFunctTwoOps
{

    /**
     * Get the RealValuedFunctionTwo associated with this named object.
     * @return the function; null if not defined
     */
    RealValuedFunctionTwo getFunction();

    /**
     * Get the minimum value of the first argument in the domain of
     * the function.
     * @return the minimum value
     */
    double getDomainMin1();

    /**
     * Determine if the domain minimum for the first argument is in the domain.
     * @return true if the domain minimum is in the domain; false otherwise
     */
    boolean domainMin1Closed();

    /**
     * Get the maximum value of the first argument in the domain of the function.
     * @return the maximum value
     */
    double getDomainMax1();

    /**
     * Determine if the domain maximum for the first argument is in the domain.
     * @return true if the domain maximum is in the domain; false otherwise
     */
    boolean domainMax1Closed();

    /**
     * Get the minimum value of the second argument in the domain of
     * the function.
     * @return the minimum value
     */
    double getDomainMin2();

    /**
     * Determine if the domain minumum for the second argument is in the domain.
     * @return true if the domain minimum is in the domain; false otherwise
     */
    boolean domainMin2Closed();

    /**
     * Get the maximum value of the second argument in the domain of the
     * function.
     * @return the maximum value
     */
    double getDomainMax2();

    /**
     * Determine if the domain maximum for the second argument is in the domain.
     * @return true if the domain maximum is in the domain; false otherwise
     */
    boolean domainMax2Closed();

    /**
     * Determine if a point (x, y) is within the domain of
     * a real-valued function of two arguments.
     * @param x the 1st argument
     * @param y the 2nd argument
     * @return true if the point (x, y) is in this function's
     *         domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     */
     boolean isInDomain(double x, double y)
	 throws UnsupportedOperationException;

    /**
     * Call the function.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the function for the given argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double valueAt(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>1</sub>
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double deriv1At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the partial derivative
     * &part;f / &part;x<sub>2</sub>
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    double deriv2At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>1</sub><sup>2</sup>
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double deriv11At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>1</sub> &part;x<sub>2</sub>)
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double deriv12At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;


    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / (&part;x<sub>2</sub> &part;x<sub>1</sub>)
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double deriv21At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

    /**
     * Evaluate the partial derivative
     * &part;<sup>2</sup>f / &part;x<sub>2</sub><sup>2</sup>
     * for the function f(x<sub>1</sub>x<sub>2</sub>) defined by this object.
     * @param arg1 the function's first argument
     * @param arg2 the function's second argument
     * @return the value of the partial derivative for the given argument
     * @exception IllegalArgumentException the function's argument(s)
     *            were out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     */
    public double deriv22At(double arg1, double arg2) throws
	IllegalArgumentException, UnsupportedOperationException;

}

//  LocalWords:  RealValuedFunctionTwo  nd arg
//  LocalWords:  UnsupportedOperationException
//  LocalWords:  IllegalArgumentException
