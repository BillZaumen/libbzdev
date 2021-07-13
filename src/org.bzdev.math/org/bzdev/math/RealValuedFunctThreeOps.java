package org.bzdev.math;

/**
 * Interface representing a real-valued function of two arguments.
 * This interface provides a single method associated with
 * a real-valued function: methods that compute  a function's domain
 * and its derivatives are missing.
 */
@FunctionalInterface
public interface RealValuedFunctThreeOps extends RealValuedFunctVAOps {
    /**
     * Compute the value of a function f for a given argument.
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the value of the function for the specified arguments
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double valueAt(double arg1, double arg2, double arg3)
	throws IllegalArgumentException,
	       UnsupportedOperationException,
	       IllegalStateException;

    @Override
    default double valueAt(double... args) throws
	IllegalArgumentException, UnsupportedOperationException,
	IllegalStateException
    {
	return valueAt(args[0], args[1], args[2]);
    }

    @Override
    default int minArgLength() {return 3;}

    @Override
    default int maxArgLength() {return 3;}

}

//  LocalWords:  arg
