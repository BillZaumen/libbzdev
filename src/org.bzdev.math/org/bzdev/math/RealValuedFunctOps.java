package org.bzdev.math;
import java.util.function.DoubleUnaryOperator;

/**
 * Interface representing a real-valued function.
 * This interface provides a single method associated with
 * a real-valued function: methods that compute a function's domain
 * and its derivatives are missing.
 * <P>
 * This class was created before the Java interface
 * {@link DoubleUnaryOperator} was defined and consequently this
 * class was modified to extend DoubleUnaryOperator.  Any implementation
 * that overrides {@link RealValuedFunctOps#applyAsDouble(double)} must
 * ensure that {@link RealValuedFunctOps#applyAsDouble(double)} and
 * {@link RealValuedFunctOps#valueAt(double)} return the same value.
 */
@FunctionalInterface
public interface RealValuedFunctOps
    extends DoubleUnaryOperator, RealValuedFunctVAOps
{
    /**
     * Compute the value of a function f for a given argument.
     * @param arg the argument
     * @return the value of the function for the specified argument
     * @exception IllegalArgumentException the function's argument
     *            was out of range
     * @exception UnsupportedOperationException  the operation is
     *            not supported.
     * @exception IllegalStateException the function was not fully
     *            initialized.
     */
    double valueAt(double arg) throws IllegalArgumentException,
				      UnsupportedOperationException,
				      IllegalStateException;

    @Override
    default double valueAt(double... args)
	throws IllegalArgumentException, UnsupportedOperationException,
	       IllegalStateException
    {
	return valueAt(args[0]);
    }

    @Override
    default int minArgLength() {return 1;}

    @Override
    default int maxArgLength() {return 1;}


    /**
     * Get the identity function f(x) = x.
     * <P>
     * Note this just returns RealValuedFunction.xFunction
     * but is provided by analogy with the interface
     * {@link java.util.function.Function}.
     * @return a function that returns its value
     */
    static RealValuedFunction identity() {
	return RealValuedFunction.xFunction;
    }

    @Override
    default double applyAsDouble(double arg) {
	return valueAt(arg);
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the <CODE>after</CODE> function to
     * the result.
     * If evaluation of either function throws an exception, it is
     * relayed to the caller of the composed function.
     * @param after the function to apply to the result of calling
     *        this function
     * @return a composed function that applies this function and then
     *         applies the <CODE>after</CODE> function
     * @exception NullPointerException if <CODE>after</CODE> is null
     * @see #compose(RealValuedFunctOps)
     */
    default RealValuedFunctOps andThen(RealValuedFunctOps after) {
	if (after == null) throw new NullPointerException();
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return after.valueAt(RealValuedFunctOps.this.valueAt(x));
	    }
	};
    }

    /**
     * Returns a composed operator that first applies this operator to
     * its input, and then applies the after operator to the
     * result. If evaluation of either operator throws an exception,
     * it is relayed to the caller of the composed operator.
     * @param after the operator to apply after this operator is applied
     * @return a composed operator that first applies this operator
     *         and then applies the after operator
     * @exception NullPointerException if <CODE>before</CODE> is null
     */
    @Override
    default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
	if (after == null) throw new NullPointerException();
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return after.applyAsDouble(RealValuedFunctOps.this.valueAt(x));
	    }
	};
    }

    /**
     * Returns a composed function that first applies the <CODE>before</CODE>
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is
     * relayed to the caller of the composed function.
     * @param before the function to call to obtain the argument passed
     *        to this function
     * @return a composed function that first calls the <CODE>before</CODE>
     *         function and passes the results to this function
     * @exception NullPointerException if <CODE>before</CODE> is null
     */
    default RealValuedFunctOps compose(RealValuedFunctOps before) {
	if (before == null) throw new NullPointerException();
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return RealValuedFunctOps.this.valueAt(before.valueAt(x));
	    }
	};
    }

    /**
     * Returns a composed function that first applies the before
     * function to its input, and then applies this function to the
     * result. If evaluation of either function throws an exception,
     * it is relayed to the caller of the composed function.
     * @param before the function to call to obtain the argument
     *        passed to this function
     * @return a composed function that first calls the before function
     *          and passes the results to this function
     * @exception NullPointerException if <CODE>before</CODE> is null
     */
    @Override
    default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
	if (before == null) throw new NullPointerException();
	return new RealValuedFunctOps() {
	    public double valueAt(double x) {
		return RealValuedFunctOps.this.valueAt(before.applyAsDouble(x));
	    }
	};
    }
}

//  LocalWords:  DoubleUnaryOperator RealValuedFunctOps applyAsDouble
//  LocalWords:  valueAt arg
