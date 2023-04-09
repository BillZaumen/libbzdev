package org.bzdev.anim2d;

/**
 * Utility class for kinematic operations.
 * These are simple operations involving constant accelerations along
 * a path (including, of course, a straight line).  Some involve
 * solving a quadratic equation, which is easy, but a solution does
 * not always exist and one has to choose between two possible
 * solutions. plus allowing for round-off errors.
 * <P>
 * The methods this class provides are all static methods, and are
 * intended to implement frequently used operations. These methods
 * follow a naming convention.  The first component of a method name
 * is "dist", "time", "accel", or "v" to denote that the return value
 * is a distance (positive or negative), a time interval (which must
 * be positive, an acceleration, and a tangential velocity
 * respectively.  The second component is the word "Given". The third
 * component is DVA, TVA, TVV, DVV, or VVA, where D indicates a
 * distance argument, T indicates a time-interval argument, V
 * indicates a velocity argument, and A indicates an acceleration
 * argument. When there is one V argument the velocity is the one a
 * the start.  When there are two V arguments, the first is the
 * velocity at the start and the second is the velocity at the end.
 * all arguments and return values are double-precession numbers.
 * A return value of Double.NaN indicates that the value cannot
 * be computed.
 */
public class KinematicOps2D {

    /**
     * Get the time to travel along a path for given distance, given
     * an initial tangential velocity, and constant tangential
     * acceleration.
     * Typically a positive distance corresponds to an increasing
     * value of the path's parameter.
     * The distance along the path will satisfy the equation
     * s = v<sub>0</sub>t + (1/2)at<sup>2</sup>, where v<sub>0</sub> is
     * the initial velocity tangent to the path, a is the acceleration,
     * and t is the time needed to cover the distance s.
     * @param s the distance along the path
     * @param v0 initial velocity tangent to the path
     * @param a the acceleration tangent to the path
     * @return the time to traverse the path; NaN if one will not
     *         move by the specified distance from the initial point
     */
    public static double timeGivenDVA(double s, double v0, double a) {
	if (s == 0.0) return 0.0;
	if (a == 0.0) {
	    if (v0 == 0.0) return Double.NaN;
	    double t = s/v0;
	    if (t < 0.0) return Double.NaN;
	    return t;
	}
	double as = a*s;
	double descr = v0*v0 + 2*as;
	if (descr < 0.0) return Double.NaN;
	double root = Math.sqrt(descr);
	double tL = (-root - v0)/a;
	double tH = (root - v0)/a;
	if (tL > tH) {
	    double tmp = tL;
	    tL = tH;
	    tH = tmp;
	}
	if (tL < 0.0) {
	    if (tH < 0.0) {
		if (as >= 0.0) {
		    return 0.0;
		} else {
		    return Double.NaN;
		}
	    } else {
		return tH;
	    }
	} else {
	    return tL;
	}
    }

    /**
     * Get the final velocity after traveling along a path for given
     * distance, given an initial tangential velocity, and constant
     * tangential acceleration.
     * Typically a positive distance corresponds to an increasing
     * value of the path's parameter.
     * The distance along the path will satisfy the equation
     * s = v<sub>0</sub>t + (1/2)at<sup>2</sup>, where v<sub>0</sub> is
     * the initial velocity tangent to the path, a is the acceleration,
     * and t is the time needed to cover the distance s. The
     * final velocity is given by v = v<sub>0</sub> + at.
     * @param s the distance along the path
     * @param v0 initial velocity tangent to the path
     * @param a the acceleration tangent to the path
     * @return the velocity when the end of the path is reached; NaN if
     *         the equations above cannot be satisfied.
     */
    public static double vGivenDVA(double s, double v0, double a)  {
	return v0 + a*timeGivenDVA(s, v0, a);
    }

    /**
     * Get the distance along a path when moving for a specified
     * time with a specified tangent velocity and acceleration.
     * The distance along the path can be positive or negative.
     * Typically a positive distance corresponds to an increasing
     * value of the path's parameter.
     * <P>
     * The distance along the path will satisfy the equation
     * s = v<sub>0</sub>t + (1/2)at<sup>2</sup>, where v<sub>0</sub> is
     * the initial velocity tangent to the path, a is the acceleration,
     * and t is the time needed to cover the distance s.
     * @param t the time spent traversing the path
     * @param v0 the initial velocity tangent to the path
     * @param a the acceleration tangent to the path
     * @return the distance covered
     */
    public static double distGivenTVA(double t, double v0, double a) {
	return v0*t + 0.5*a*t*t;
    }


    /**
     * Get the final velocity along a path when moving for a specified
     * time with a specified initial tangent velocity and with
     * constant tangential acceleration.
     * @param t the time spent traversing the path
     * @param v0 the initial velocity tangent to the path
     * @param a the acceleration tangent to the path
     * @return the distance covered
     */
    public static double vGivenTVA(double t, double v0, double a) {
	return v0 + a*t;
    }

    /**
     * Get the distance traversed along a path assuming constant
     * acceleration when the traversal takes a time t, with initial
     * and final tangential velocities v1 and v2 respectively.
     * The distance is signed, with positive values typically
     * corresponding to increasing values of the path parameter.
     * @param t the time spent traversing the path
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @return the distance along the path that was traversed
     */
    public static double distGivenTVV(double t, double v1, double v2) {
	return t*(v1+v2)/2.0;
    }

    /**
     * Get the acceleration needed to traverse a path assuming constant
     * acceleration when the traversal takes a time t, with initial
     * and final tangential velocities v1 and v2 respectively.
     * @param t the time spent traversing the path
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @return the acceleration
     */
    public static double accelGivenTVV(double t, double v1, double v2) {
	if (v1 == v2) return 0.0;
	if (t == 0.0) return Double.NaN;
	return (v2-v1)/t;
    }

    /**
     * Get the time needed to to traverse a specified distance along
     * a path with constant tangential acceleration and specified
     * initial and final tangential velocities.  The time interval
     * returned must be positive.
     * @param s the distance traversed
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @return the time to traverse the path; Double.NaN if no feasible
     *         value exists.
     */
    public static double timeGivenDVV(double s, double v1, double v2) {
	if (s == 0.0) return 0.0;
	double v12 = v1+v2;
	if (v12 == 0.0) return Double.NaN;
	return 2.0*s/v12;
    }

    /**
     * Get the acceleration needed to to traverse a specified distance along
     * a path with constant tangential acceleration and specified
     * initial and final tangential velocities.
     * @param s the distance traversed
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @return the tangential acceleration; Double.NaN if no feasible
     *         value exists.
     */
    public static double accelGivenDVV(double s, double v1, double v2) {
	if (v1 == v2) return 0.0;
	if (s == 0.0) return Double.NaN;
	double v12 = v1*v1;
	double v22 = v2*v2;
	return (v22-v12)/(2.0*s);
    }

    /**
     * Get the time to traverse a distance along a path given the
     * tangential velocities at the start and end of the path and the
     * tangential acceleration along the path.
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @param a the tangential acceleration along the path
     * @return the time it will take to reach a velocity equal to v2;
     *         Double.NaN if no such time exists
     */
    public static double timeGivenVVA(double v1, double v2, double a) {
	if (v1 == v2) return 0.0;
	if (a == 0.0) return Double.NaN;
	return (v2-v1)/a;
    }

    /**
     * Get the distance along a path given the tangential velocities
     * at the start and end of the path and the tangential
     * acceleration along the path.
     * @param v1 the initial tangential velocity
     * @param v2 the final tangential velocity.
     * @param a the tangential acceleration along the path
     * @return the time it will take to reach a velocity equal to v2;
     *         Double.NaN if no such distance exists
     */
    public static double distGivenVVA(double v1, double v2, double a) {
	double t = timeGivenVVA(v1,v2,a);
	if (Double.isNaN(t)) return Double.NaN;
	return v1*t + 0.5*a*t*t;
    }


    // we never need to construct an instance - this constructor will
    // prevent us from generating a javadocs entry.
    private KinematicOps2D() {}
}
