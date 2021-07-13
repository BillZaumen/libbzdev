package org.bzdev.math;
import java.util.function.DoubleBinaryOperator;

/**
 * Interface representing a real-valued function of two arguments.
 * This interface provides a single method associated with
 * a real-valued function: methods that compute  a function's domain
 * and its derivatives are missing.
 * <P>
 * This class was created before the Java interface
 * {@link DoubleBinaryOperator} was defined and consequently this
 * class was modified to extend DoubleBinaryOperator.  Any implementation
 * that overrides {@link RealValuedFunctTwoOps#applyAsDouble(double,double)}
 * must ensure that {@link RealValuedFunctTwoOps#applyAsDouble(double,double)}
 * and {@link RealValuedFunctTwoOps#valueAt(double,double)} return the same
 * value.
 */
@FunctionalInterface
public interface RealValuedFunctTwoOps
    extends DoubleBinaryOperator, RealValuedFunctVAOps
{
    /**
     * Compute the value of a function f for the given arguments.
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the value of the function for the specified arguments
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double valueAt(double arg1, double arg2)
	throws IllegalArgumentException,
	       UnsupportedOperationException,
	       IllegalStateException;

    @Override
    default double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	return valueAt(args[0]);
    }

    @Override
    default int minArgLength() {return 2;}

    @Override
    default int maxArgLength() {return 2;}

    @Override
    default double applyAsDouble(double arg1, double arg2) {
	return valueAt(arg1, arg2);
    }
}

//  LocalWords:  DoubleBinaryOperator RealValuedFunctTwoOps valueAt
//  LocalWords:  applyAsDouble arg
