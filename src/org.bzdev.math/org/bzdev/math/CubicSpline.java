package org.bzdev.math;
import org.bzdev.lang.CallableArgsReturns;

/**
 * Abstract class defining a cubic spline.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * The spline approximates a function f such that y = f(x). When the
 * knots (the points the spline passes through) are evenly spaced in
 * terms of their X values, look-up is faster than otherwise.  Because
 * of this, two classes are provided {@link CubicSpline1 CubicSpline1}
 * for splines based on knots whose X values are evenly spaced and
 * {@link CubicSpline2 CubicSpline2} for knots whose X values have an
 * arbitrarily spacing.
 * <P>
 * A spline can be created in any of several modes described by
 * the enumeration {@link org.bzdev.math.CubicSpline.Mode CubicSpline.Mode}.
 * The default mode is "NATURAL", which sets the second derivatives
 * (but not necessarily the third derivative) to zero at the end
 * points. For all other modes, a constructor specifying a mode is
 * required.  Some modes base the spline not only on the values at
 * specific knots, but on the derivatives at all or a subset of these
 * points.
 * <P>
 * For all but one of the supported modes, the second derivative of the
 * spline (e.g., the function the spline computes) is a continuous function.
 * The mode HERMITE is an exception: Hermite splines have continuous first
 * derivatives, but second derivatives may have discontinuities.
 * <P>
 * Methods are available for determining the spline's mode, the minimum
 * and maximum values it can compute, the value at a specified point,
 * the derivative at a specified point, whether the values that define
 * the spline are strictly monotonic, and (if the spline is strictly
 * monotonic) the value of the spline's inverse at a particular point.
 *
 * @see org.bzdev.math.CubicSpline.Mode
 * @see org.bzdev.math.CubicSpline1
 * @see org.bzdev.math.CubicSpline2
 */
public abstract class CubicSpline extends RealValuedFunction {

    /**
     * The mode for creating a spline.
     * This affects the starting and ending segments of the spline. The names
     * of the modes are based on the citation given in the documentation for
     * {@link CubicSpline}.
     */
    public enum Mode {
	/**
	 * The second derivatives vanish for the initial and final
	 * points of the spline.
	 */
	NATURAL,
	/**
	 * The spline will actually be a quadratic. Valid only when
	 * exactly 3 points are used to create the spline.
	 */
	QUAD_FIT,
	/**
	 * The second derivatives are equal to each other at the first
	 * two knots of the spline. The second derivatives are also
	 * equal at the last two knots of the spline.
	 */
	PARABOLIC_RUNOUT,
	/**
	 * The second derivatives are equal at the first two knots of
	 * the spline.
	 */
	PARABOLIC_RUNOUT_START,
	/**
	 * The second derivatives are equal at the last two knots of
	 * the spline.
	 */
	PARABOLIC_RUNOUT_END,
	/**
	 * The first two segments of the spline match a single cubic polynomial.
	 * Similarly, the last two segments of the spline match a single cubic
	 * polynomial.
	 */
	CUBIC_RUNOUT,
	/**
	 * The first two segments of the spline match a single cubic polynomial.
	 */
	CUBIC_RUNOUT_START,
	/**
	 * The last two segments of the spline match a single cubic polynomial.
	 */
	CUBIC_RUNOUT_END,
	/**
	 * The first two segments of the spline match a single cubic
	 * polynomial.  The second derivatives are equal to each other
	 * at the last two knots of the spline.
	 *
	 */
	CUBIC_START_PARABOLIC_END,
	/**
	 * The second derivatives are equal to each other at the first
	 * two knots of the spline.  The last two segments of the
	 * spline match a single cubic polynomial.
	 */
	 PARABOLIC_START_CUBIC_END,
	/**
	 * The derivatives have specified values at the start and the end of
	 * the spline.
	 */
	CLAMPED,
	/**
	 * The derivative has a specified value at the start of the spline.
	 */
	CLAMPED_START,
	/**
	 * The derivative has a specified value at the end of the spline.
	 */
	CLAMPED_END,
	/**
	 * The derivative has a specified value at the start of the spline.
	 *
	 */
	CLAMPED_START_PARABOLIC_END,
	/**
	 *
	 * The derivative has a specified value at the end of the spline.
	 * The derivatives are equal to each other at the first two knots
	 * of the spline.
	 */
	PARABOLIC_START_CLAMPED_END,
	/**
	 * The derivative has a specified value at the start of the spline.
	 * The last two segments of the spline match a single cubic polynomial.
	 */
	CLAMPED_START_CUBIC_END,
	/**
	 * The first two segments of the spline match a single cubic polynomial.
	 * The derivative has a specified value at the end of the spline.
	 */
	CUBIC_START_CLAMPED_END,
	/**
	 * Both the value and the derivative of a function are specified
	 * at each point used to generate a spline.
	 */
	HERMITE
    }
    /**
     * Get the spline's mode.
     * @return the mode for the spline.
     * @see org.bzdev.math.CubicSpline.Mode
     */
    public abstract Mode getMode();

    /*
      NOT NEEDED - SEE RealValuedFunction

     * Compute the value of the spline for a specified argument.
     * @param x the value at which to evaluate the spline
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
    public abstract double valueAt(double x) throws IllegalArgumentException;
     */

    /*
     * Compute the value of derivative of the spline for a specified argument.
     * @param x the value at which to evaluate the derivative
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
     */
    // public abstract double derivAt(double x) throws IllegalArgumentException;

    /*
     * Compute the value of second derivative of the spline for a
     * specified argument.
     * @param x the value at which to evaluate the second derivative
     * @return the value at x
     * @exception IllegalArgumentException the argument was out of range.
     public abstract double secondDerivAt(double x)
	throws IllegalArgumentException;
     */

    /**
     * Verify that the spline's polynomials compute various values
     * consistently to an accuracy given by the argument
     * The cubic polynomials that the spline uses are supposed to
     * compute the same value, first derivative, and second derivative
     * where they meet, and each mode places constraints on the end
     * points.
     * @param limit the accuracy limit
     * @return true of the tests succeed; false otherwise
     */
    public abstract boolean verify(double limit);

    /**
     * Determine if the values defining a spline form a strictly monotonic
     * sequence of numbers.
     * @return true if the values are strictly monotonic; false otherwise
     */
    public abstract boolean isStrictlyMonotonic();

    /**
     * Get the inversion limit.
     * Computing the inverse in most cases requires solving a cubic
     * equation, with valid solutions being in the range [0, 1].
     * The inversion limit allows solutions in the range
     * [-inversionLimit, 1+inversionLimit] to be accepted, with values
     * outside of the interval [0, 1] replaced by 0 or 1, whichever is
     * closer.  The use of an inversion limit allows for round-off errors.
     * @return the inversion limit
     */
    public abstract double getInversionLimit();

    /**
     * Set the inversion limit.
     * Computing the inverse in most cases requires solving a cubic
     * equation, with valid solutions being in the range [0, 1].
     * The inversion limit allows solutions in the range
     * [-inversionLimit, 1+inversionLimit] to be accepted, with values
     * outside of the interval [0, 1] replaced by 0 or 1, whichever is
     * closer.  The use of an inversion limit allows for round-off errors.
     * @param limit the invrsion limit
     */
   public abstract  void setInversionLimit(double limit);

    /**
     * For a spline that represents the function y = f(x), get the value of
     * its inverse x=f<sup>-1</sup>(y).
     * @param y the argument for the inverse function
     * @return the inverse evaluated at y
     * @exception IllegalArgumentException an inverse could not be computed.
     */
    public abstract double inverseAt(double y) throws IllegalArgumentException;

    /**
     * Count the number of knots in this spline.
     * @return the number of knots
     */
    public abstract int countKnots();

    /**
     * Get the coefficients &beta;<sub>i</sub> for the segments of
     * this spline when represented as a sum of Bernstein polynomials
     * $\sum^3_{i=0}\beta_i B_{i,3}(t)$
     * <!-- &sum;<sup>3</sup><sub>i=0</sub>&beta;<sub>i</sub>B<sub>i,3</sub>(t) -->
     * where t&isin;[0,1].
     * <P>
     * There are 4 coefficients per segment, but for a spline, the
     * last coefficient of one segment is the first for the next. With
     * the exception of the initial coefficient, the first coefficient
     * for each segment is not listed, to avoid duplications. The
     * value of the coefficients match the points the spline is
     * guaranteed to pass through at indices 0, 3, 6, 9, etc. These
     * represent the i=0 case in the sum given above.  For segment j,
     * &beta;<sub>i</sub> = array[3*j + i] for i &isin; [0,3] and
     * j &isin; [0, n], where n is the number of segments.
     * <P>
     * One use of this method is to provide the control points for a
     * path - for example, the classes {@link java.awt.geom.Path2D},
     * {@link org.bzdev.geom.Path3D}, and their subclasses.
     * @return the coefficients
     */
    public abstract double[] getBernsteinCoefficients();
}

//  LocalWords:  CubicSpline spline's RealValuedFunction valueAt isin
//  LocalWords:  IllegalArgumentException derivAt secondDerivAt
//  LocalWords:  inversionLimit duplications subclasses
