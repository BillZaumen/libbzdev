package org.bzdev.math;

/**
 * Interface representing a real-valued function with a variable number
 * of arguments.
 * This interface provides a single method associated with
 * a real-valued function: methods that compute a function's domain
 * and its derivatives are missing, as are methods that determine the
 * number of arguments that are allowed.
 */
@FunctionalInterface
public interface RealValuedFunctVAOps {
    /**
     * Compute the value of a function f for the given arguments.
     * @param args the arguments
     * @return the value of the function for the specified argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double valueAt(double... args) throws IllegalArgumentException,
					  UnsupportedOperationException,
					  IllegalStateException;


    /**
     * Get the minimum number of arguments allowed in calls to
     * {@link valueAt(double...)}.
     * @return the minimum number of arguments.
     */
    default int minArgLength() {
	return 0;
    }

    /**
     * Get the maximum number of arguments allowed in calls to
     * {@link valueAt(double...)}.
     * @return the maximum number of arguments.
     */
    default int maxArgLength() {
	return Integer.MAX_VALUE;
    }


}

//  LocalWords:  args valueAt
