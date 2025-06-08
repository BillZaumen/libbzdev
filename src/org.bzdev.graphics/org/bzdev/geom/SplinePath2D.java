package org.bzdev.geom;
import org.bzdev.math.TridiagonalSolver;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.CubicSpline;
import java.awt.Shape;
import java.awt.geom.*;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Path2D with smooth curves connecting sequences of points.
 * This class extends Path2D.Double by using cubic B&eacute;zier splines
 * to connect sequences of point. This can be done either by
 * using a constructor (as a convenience) or by using the
 * methods
 * {@link #splineTo(java.awt.geom.Point2D[], int) splineTo} to add a smooth
 * curve segment or
 * {@link #addCycle(java.awt.geom.Point2D[], int) addCycle} to add smooth
 * closed path.  If {@link #splineTo(java.awt.geom.Point2D[], int) splineTo}
 * is used, the starting point of the spline is the current point, which
 * is initially set using the Path2D
 * {@link java.awt.geom.Path2D#moveTo(double,double) moveTo} method.
 * The smooth curves created are cubic B&eacute;zier curve segments
 * arranged so that the first and second derivatives of their
 * coordinates with respect to the curves' parameters match (one curve
 * at u = 1 and the other at u = 0). This is equivalent to stating that
 * for the composite curve, the second derivative exits at all points and
 * is continuous.
 * <P>
 * For a call to {@link #splineTo(java.awt.geom.Point2D[], int) splineTo},
 * at the end points, the tangent to the curve points towards its
 * neighbor. As a result, calling splineTo for points p1, p2, p3, p4,
 * followed by a call to splineTo for points p5, p6, p7, p8 is not
 * equivalent to calling splineTo once for points p1, p2, ..., p8: in
 * the former case, the tangent lines can differ on both sides of
 * point p4.

 * <P>
 * When {@link #addCycle(java.awt.geom.Point2D[], int) addCycle} is used,
 * there is an implicit call to moveTo for the first point in the array and
 * this point becomes the last point, but otherwise the end points are not
 * treated specially.  The usual winding rules apply in determining whether
 * one is inside or outside a shape described by the path. Regardless of
 * whether {@link #splineTo(java.awt.geom.Point2D[], int) splineTo},
 * {@link #addCycle(java.awt.geom.Point2D[], int) addCycle}, or constructor
 * is used, the smoothed paths are simply sequences of calls to at most one
 * initial moveTo method and some number of curveTo methods.
 * <P>
 * The algorithm is described in an article,
 * <a href="http://www.particleincell.com/blog/2012/bezier-splines/">
 * "Smooth B&eacute;zier Spline Through Prescribed Points"
 * </a>
 * Each segment is a B&eacute;zier curve given by
 * <P>
 * B(u) = (1-u)<sup>3</sup>P<sub>0</sub> + 3(1-u)<sup>2</sup>uP<sub>1</sub> + 3(1-u)u<sup>2</sup>P<sub>2</sub> + t<sup>3</sup>P<sub>3</sub>.
 * <P>
 * For n-1 cubic splines with control points $P<sub>0,i</sub>
 * P<sub>1,i</sub>, P<sub>1,2</sub> and P<sub>3,i</sub>, with i in the interval
 * [0, n-1], connecting the "knot" points  K<sub>0</sub> ... K<sub>n</sub>, the
 * P<sub>1</sub> control points satisfy the equations
 * <ul>
 *  <li> 2P<sub>1,0</sub> + P<sub>1,1</sub> = K<sub>0</sub> + 2K<sub>1</sub>
 *  <li> P<sub>1,i-1</sub> + 4P<sub>1,i</sub> + P<sub>1,i+1</sub> = 4K<sub>i</sub> + 2K<sub>i+1</sub>  for i in [1, n-2]
 * <li> 2P<sub>1,n-2</sub> + 7P<sub>1,n-1</sub> = 8K<sub>n-1</sub> + K<sub>n</sub>.
 * </ul>
 * The first and last of these equations make the second derivative of
 * the curve vanish at the end points, whereas the rest make the
 * second derivative at u=1 for a segment match the second derivative
 * at u=0 for the next segment.  Alternatively, the first and last
 * equations can optionally be replaced by the equations
 * <UL>
 *  <LI> P<sub>1,0</sub> = C<sub>1</sub>
 *  <LI> P<sub>1,n-1</sub> = 2C<sub>2</sub> - K<sub>n</sub>
 * </UL>
 * respectively in order to explicitly set the first and/or last
 * intermediate control points to values matching C<sub>1</sub> and/or
 * C<sub>2</sub>. When both C<sub>1</sub> and C<sub>2</sub> are provided,
 * splines for each are computed and their control points are averaged
 * using a weighted average based on the how close a knot is to each of
 * the control points.
 * <P>
 * The initial and final control points for each segment are given by
 * <ul>
 *  <li> P<sub>0,i</sub> = K<sub>i</sub>
 *  <li> P<sub>3,i</sub> = K<sub>i+1</sub>
 * </ul>
 * with both defined for i in the interval [0, n-1], so that each segment
 * starts and ends at a knot.
 * The P<sub>2</sub> control  point is given by the equations
 * <ul>
 *  <li> P<sub>2,i</sub> = 2K<sub>i+1</sub> - P<sub>1,i+1</sub> for i in
 *       [0,n-2].
 *  <li> P<sub>2,n-1</sub> = (K<sub>n</sub> + P<sub>1,n-1</sub>)/2.
 * </ul>
 * There is a typo in the article cited above giving the wrong value for
 * the second control point P<sub>2,i</sub> for i in [0,n-2].
 * <P>
 * For a closed paths, a case not covered in the article cited above,
 * the equation for P<sub>1</sub> are given by
 * <ul>
 *   <li> P<sub>1,n</sub> + 4P<sub>1,0</sub> + P<sub>1,i+1</sub> = 4K<sub>i</sub> + 2K<sub>i+1</sub>
 *  <li> P<sub>1,i-1</sub> + 4P<sub>1,i</sub> + P<sub>1,i+1</sub> = 4K<sub>i</sub> + 2K<sub>i+1</sub>  for i in [1, n-1]
 *   <li> P<sub>1,n-1</sub> + 4P<sub>1,n</sub> + P<sub>1,0</sub> = 4K<sub>n</sub> + 2K<sub>0</sub>
 * </ul>
 * The other control points for closed paths are given by
 * <ul>
 *  <li> P<sub>0,i</sub> = K<sub>i</sub> for i in [0,n]
 *  <li> P<sub>2,i</sub> = 2K<sub>i+1</sub> - P<sub>1,i+1</sub> for i in
 *       [0, n-1]
 *  <li> P<sub>2,n</sub> = 2K<sub>0</sub> - P<sub>1,0</sub>
 *  <li> P<sub>3,i</sub> = K<sub>i+1</sub> for i in [0, n-1].
 *  <li> P<sub>3,n</sub> = K<sub>0</sub>.
 * </ul>
 */
public class SplinePath2D extends Path2D.Double {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    private double[] createA(int n, boolean cyclic, boolean startsWithMoveTo,
			     Point2D cpoint1/*, Point2D cpoint2*/)
    {
	if (!startsWithMoveTo) n++;
	if (!cyclic) n--;
	double[] result = new double[n];
	int nm1 = n-1;
	result[0] = cyclic? 1.0: 0.0;
	for (int i = 1; i < nm1; i++) {
	    result[i] = 1.0;
	}
	/*
	if (cpoint2 != null) {
	    result[nm1] = 0.0;
	} else {
	    result[nm1] = cyclic? 1.0: 2.0;
	}
	*/
	result[nm1] = cyclic? 1.0: 2.0;
	return result;
    }
    private double[] createB(int n, boolean cyclic, boolean startsWithMoveTo,
			     Point2D cpoint1/*, Point2D cpoint2*/)
    {
	if (!startsWithMoveTo) n++;
	if (!cyclic) n--;
	double[] result = new double[n];
	int nm1 = n-1;
	if (cpoint1 != null) {
	    result[0] = 1.0;
	} else {
	    result[0] =cyclic? 4.0: 2.0;
	}
	for (int i = 1; i < nm1; i++) {
	    result[i] = 4.0;
	}
	/*
	if (cpoint2 != null) {
	    result[nm1] = 1.0;
	} else {
	    result[nm1] = cyclic? 4.0: 7.0;
	}
	*/
	result[nm1] = cyclic? 4.0: 7.0;
	return result;
    }
    private double[] createC(int n, boolean cyclic, boolean startsWithMoveTo,
			     Point2D cpoint1/*, Point2D cpoint2*/)
    {
	if (!startsWithMoveTo) n++;
	if (!cyclic) n--;
	double[] result = new double[n];
	int nm1 = n-1;
	int maxi = cyclic? n: nm1;
	if (cpoint1 == null) {
	    result[0] = 1.0;
	} else {
	    result[0] = 0.0;
	}
	for (int i = 1; i < maxi; i++) {
	    result[i] = 1.0;
	}
	return result;
    }

    private double[] getw(double[] x, int n, boolean cyclic,
			  boolean startsWithMoveTo, boolean isX,
			  Point2D cpoint1, Point2D cpoint2)
    {
	if (startsWithMoveTo) {
	    // x[0] represents the coordinate for an implied moveTo.
	    if (!cyclic) n--;
	    int nm1 = n-1;
	    double[] result = new double[n];
	    if (cyclic) {
		for (int i = 0; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm1] + 2.0 * x[0];
	    } else {
		if (cpoint1 != null) {
		    result[0] = isX? cpoint1.getX(): cpoint1.getY();
		} else {
		    result[0] = x[0] + 2.0 * x[1];
		}
		for (int i = 1; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}       
		if (cpoint2 != null) {
		    double p2nm1 = isX? cpoint2.getX(): cpoint2.getY();
		    result[nm1] = 2*p2nm1 -x[n];
		} else {
		    result[nm1] =  8.0 * x[nm1] + x[n];
		}
	    }
	    return result;
	} else {
	    if (cyclic) n++;
	    double[] result = new double[n];
	    int nm1 = n - 1;
	    int nm2 = n - 2;
	    Point2D start = getCurrentPoint();
	    if (start == null) {
		throw new IllegalStateException(errorMsg("missingMOVETO"));
	    }
	    double xval = isX? start.getX(): start.getY();
	    if (cyclic) {
		result[0] = 4.0 * xval + 2.0 * x[0];
		for (int i = 0; i < nm2; i++) {
		    result[i+1] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm2] + 2.0 * xval;
	    } else {
		if (cpoint1 != null) {
		    result[0] = isX? cpoint1.getX(): cpoint1.getY();
		} else {
		    result[0] = xval + 2.0 * x[0];
		}
		for (int i = 1; i < nm1; i++) {
		    result[i] = 4.0 * x[i-1] + 2.0 * x[i];
		}
		if (cpoint2 != null) {
		    double p2nm1 = isX? cpoint2.getX(): cpoint2.getY();
		    result[nm1] = 2*p2nm1 -x[nm1];
		} else {
		    result[nm1] =  8.0 * x[nm2] + x[nm1] ;
		}
	    }
	    return result;
	}
    }

    private double[] getw(double[] x, int n, boolean cyclic,
			  boolean startsWithMoveTo, boolean isX,
			  Point2D cpoint, boolean reverse)
    {
	if (startsWithMoveTo) {
	    // x[0] represents the coordinate for an implied moveTo.
	    if (!cyclic) n--;
	    int nm1 = n-1;
	    double[] result = new double[n];
	    if (cyclic) {
		for (int i = 0; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm1] + 2.0 * x[0];
	    } else {
		int nm2 = n-2;
		if (cpoint != null) {
		    result[0] = isX? cpoint.getX(): cpoint.getY();
		} else {
		    int index0 = reverse? n: 0;
		    int index1 = reverse? nm1: 1;
		    result[0] = x[index0] + 2.0 * x[index1];
		}
		for (int i = 1; i < nm1; i++) {
		    int indexI = reverse? n-i: i;
		    int indexIp1 = reverse? indexI-1: i+1;
		    result[i] = 4.0 * x[indexI] + 2.0 * x[indexIp1];
		}
		int index1 = reverse? 1: nm1;
		int index2 = reverse? 0: n;
		result[nm1] =  8.0 * x[index1] + x[index2];
	    }
	    return result;
	} else {
	    if (cyclic) n++;
	    double[] result = new double[n];
	    int nm1 = n - 1;
	    int nm2 = n - 2;
	    Point2D start = getCurrentPoint();
	    if (start == null) {
		throw new IllegalStateException(errorMsg("missingMOVETO"));
	    }
	    double xval = isX? start.getX(): start.getY();
	    if (cyclic) {
		result[0] = 4.0 * xval + 2.0 * x[0];
		for (int i = 0; i < nm2; i++) {
		    result[i+1] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm2] + 2.0 * xval;
	    } else {
		if (cpoint != null) {
		    result[0] = isX? cpoint.getX(): cpoint.getY();
		} else {
		    if (reverse) {
			result[0] = x[nm1] + 2* x[nm2];
		    } else {
			result[0] = xval + 2.0 * x[0];
		    }
		}
		for (int i = 1; i < nm1; i++) {
		    int indexI = reverse? nm2 - i: i;
		    int indexIm1 = reverse?indexI+1 :i-1;
		    result[i] = 4.0 * x[indexIm1] + 2.0 * x[indexI];
		}
		if (reverse) {
		    result[nm1] = 8.0*x[0] + xval;
		} else {
		    result[nm1] =  8.0 * x[nm2] + x[nm1] ;
		}
	    }
	    return result;
	}
    }


    /**
     * Constructs a new empty SplinePath2D object with a default
     * winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     */
    public SplinePath2D() {
	super();
    }

    /**
     * Constructs a new empty SplinePath2D object with the specified
     * winding rule to control operations that require the interior of
     * the path to be defined.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule) {
	super(rule);
    }

    /**
     * Constructs a new empty SplinePath2D object with the
     * specified winding rule and the specified initial capacity to
     * store path segments.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments
     *        in the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity) {
	super(rule, initialCapacity);
    }

    /**
     * Constructs a new SplinePath2D object from an
     * arbitrary {@link java.awt.Shape Shape} object.
     * @param s the specified Shape object
     */
    public SplinePath2D(Shape s) {
	super(s);
    }

    /**
     * Constructs a new SplinePath2D object from an
     * arbitrary {@link java.awt.Shape Shape} object, transformed by
     * an {@link java.awt.geom.AffineTransform AffineTransform}
     * object.
     * @param s the specified Shape object
     * @param at  the specified AffineTransform object
     */
    public SplinePath2D(Shape s, AffineTransform at) {
	super(s, at);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public SplinePath2D(Point2D[]pk, int n, boolean closed) {
	super();
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}
     * and two control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     */
    public SplinePath2D(Point2D[]pk, int n, Point2D cpoint1, Point2D cpoint2)
    {
	super();
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, true, cpoint1, cpoint2);
    }


    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, starting at an offset and given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that contain the knots of a spline
     * @param offset the offset into the array for the starting knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [offset, offset+n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public SplinePath2D(Point2D[]pk, int offset, int n, boolean closed) {
	super();
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[offset+i].getX();
	    tmpy[i] = pk[offset+i].getY();
	}
	makeSpline(tmpx, tmpy, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, starting at an offset and given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO} with two
     * control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that contain the knots of a spline
     * @param offset the offset into the array for the starting knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [offset, offset+n)
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     */
    public SplinePath2D(Point2D[]pk, int offset, int n,
			Point2D cpoint1, Point2D cpoint2)
    {
	super();
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[offset+i].getX();
	    tmpy[i] = pk[offset+i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, true, cpoint1, cpoint2);
    }

    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param pk the array of points that make up the knots of a spline
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public SplinePath2D(Point2D[]pk, boolean closed) {
	this(pk, pk.length, closed);
    }

    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO} and initial
     * and final control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param pk the array of points that make up the knots of a spline
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     */
    public SplinePath2D(Point2D[]pk, Point2D cpoint1, Point2D cpoint2) {
	this(pk, pk.length, cpoint1, cpoint2);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, int n, boolean closed) {
	super(rule);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a winding rule and two control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, int n,
			Point2D cpoint1, Point2D cpoint2)
    {
	super(rule);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, true, cpoint1, cpoint2);
    }


    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given an offset into the array and given a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param offset the staring index int the array for the spline's knots
     * @param n the number of points in the array to use, with valid indices
     *        in the range [offset, offset+n)
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, int offset, int n,
			boolean closed)
    {
	super(rule);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[offset+i].getX();
	    tmpy[i] = pk[offset+i].getY();
	}
	makeSpline(tmpx, tmpy, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given an offset into the array and given a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param offset the staring index int the array for the spline's knots
     * @param n the number of points in the array to use, with valid indices
     *        in the range [offset, offset+n)
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, int offset, int n,
			Point2D cpoint1, Point2D cpoint2)
    {
	super(rule);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[offset+i].getX();
	    tmpy[i] = pk[offset+i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, true, cpoint1, cpoint2);
    }


    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, boolean closed) {
	this(rule, pk, pk.length, closed);
    }

    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a winding rule and initial and final control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param pk the array of points that make up the knots of a spline
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, Point2D[]pk, Point2D cpoint1, Point2D cpoint2)
    {
	this(rule, pk, pk.length, cpoint1, cpoint2);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a winding rule and initial capacity.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			Point2D[]pk, int n, boolean closed) {
	super(rule, initialCapacity);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from an array containing at least n
     * points, given a winding rule, initial capacity, and two control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			Point2D[]pk, int n,
			Point2D cpoint1, Point2D cpoint2)
    {
	super(rule, initialCapacity);
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, true, cpoint1, cpoint2);
    }

    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a winding rule and initial capacity.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			Point2D[] pk, boolean closed)
    {
	this(rule, initialCapacity, pk, pk.length, closed);
    }

    /**
     * Constructs a new SplinePath2D object from an array of points, given
     * a winding rule, initial capacity, and two control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param cpoint1 the control point just after the start of the path
     * @param cpoint2 the control point just before the end of the path
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			Point2D[] pk, Point2D cpoint1, Point2D cpoint2)
    {
	this(rule, initialCapacity, pk, pk.length, cpoint1, cpoint2);
    }


    /**
     * Constructs a new SplinePath2D object from arrays containing at least n
     * x and y coordinates given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public SplinePath2D(double[] x, double[] y, int n, boolean closed) {
	super();
	makeSpline(x, y, n, closed, true);
    }

    private static int countKnots(CubicSpline xspline, CubicSpline yspline)
	throws IllegalArgumentException
    {
	int nx = xspline.countKnots();
	int ny = yspline.countKnots();
	if (nx != ny)
	    throw new IllegalArgumentException(errorMsg("splineLengthsDiffer"));
	if (nx < 1) return 1;	// must allocate something
	return nx;
    }

    /**
     * Constructs a new SplinePath2D object given cubic splines specifying
     * the x and y coordinates and given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The spline must be created with the same number of "knots". The argument
     * to each function will be rescaled so that it will be incremented by
     * 1.0 when moving from one knot to the next.
     * If closed is true and the initial knot is not at the same location
     * as the final knot, a straight line segment will be used to close
     * the path.
     * <P>
     * This constructor is provided to support cases where the default
     * algorithm for creating a spline is not appropriate: the class
     * {@link org.bzdev.math.CubicSpline} allows one to specify a number
     * of types of splines, with most differing in how the ends are treated,
     * but with one case (A Hermite spline) explicitly giving the derivative
     * at each knot.
     * @param xf the spline specifying the x coordinates to use
     * @param yf the spline specifying the y coordinates to use
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the splines xf and yf did
     *            not have the same number of knots
     */
    public SplinePath2D(CubicSpline xf, CubicSpline yf,
			boolean closed)
	throws IllegalArgumentException
    {
	this(Path2D.WIND_NON_ZERO, xf, yf, closed);
    }

    /**
     * Constructs a new SplinePath2D object given cubic splines specifying
     * the x and y coordinates and a winding rule.
     * The splines must be created with the same number of "knots". The argument
     * to each spline will be rescaled so that it will be incremented by
     * 1.0 when moving from one knot to the next.
     * If closed is true and the initial knot is not at the same location
     * as the final knot, a straight line segment will be used to close
     * the path.
     * <P>
     * This constructor is provided to support cases where the default
     * algorithm for creating a spline is not appropriate: the class
     * {@link org.bzdev.math.CubicSpline} allows one to specify a number
     * of types of splines, with most differing in how the ends are treated,
     * but with one case (A Hermite spline) explicitly giving the derivative
     * at each knot.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param xf the spline specifying the x coordinates to use
     * @param yf the spline specifying the y coordinates to use
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the splines xf and yf did
     *            not have the same number of knots
     */
    public SplinePath2D(int rule,
			CubicSpline xf, CubicSpline yf,
			boolean closed)
	throws IllegalArgumentException
    {

	this(rule, countKnots(xf, yf), xf, yf, closed);
    }


    /**
     * Constructs a new SplinePath2D object given cubic splines specifying
     * the x and y coordinates, a winding rule, and an initial capacity.
     * The splines must be created with the same number of "knots". The argument
     * to each spline will be rescaled so that it will be incremented by
     * 1.0 when moving from one knot to the next.  The path will be
     * constructed using the control points associated with the cubic splines
     * providing the x and y coordinates.
     * If closed is true and the initial knot is not at the same location
     * as the final knot, a straight line segment will be used to close
     * the path.
     * <P>
     * This constructor is provided to support cases where the default
     * algorithm for creating a spline is not appropriate: the class
     * {@link org.bzdev.math.CubicSpline} allows one to specify a number
     * of types of splines, with most differing in how the ends are treated,
     * but with one case (A Hermite spline) explicitly giving the derivative
     * at each knot.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param xf the spline specifying the x coordinates to use
     * @param yf the spline specifying the y coordinates to use
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the splines xf and yf did
     *            not have the same number of knots
     */
    public SplinePath2D(int rule, int initialCapacity,
			CubicSpline xf, CubicSpline yf,
			boolean closed)
	throws IllegalArgumentException
    {
	super(rule, (initialCapacity < countKnots(xf, yf)?
		     countKnots(xf, yf): initialCapacity));
	double[] xcoords = ((CubicSpline)xf).getBernsteinCoefficients();
	double[] ycoords = ((CubicSpline)yf).getBernsteinCoefficients();

	int n = xcoords.length;
	if (n != 0) {
	    moveTo(xcoords[0], ycoords[0]);
	    for (int i = 1; i < n; i += 3) {
		curveTo(xcoords[i], ycoords[i],
			xcoords[i+1], ycoords[i+1],
			xcoords[i+2], ycoords[i+2]);
	    }
	    if (closed) closePath();
	}
    }

    /**
     * Constructs a new SplinePath2D object given functions specifying
     * the x and y coordinates and given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * When closed, the initial
     * point should not be repeated by the final point.
     * @param xf the function specifying the x coordinates to use
     * @param yf the function specifying the y coordinates to use
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     */
    public SplinePath2D(RealValuedFunctOps xf, RealValuedFunctOps yf,
			double t1, double t2, int n, boolean closed)
	throws IllegalArgumentException
    {
	super();
	if (n <= 0)
	    throw new
		IllegalArgumentException
		(errorMsg("numbSegsNotPos", n));
		/*("number of segments is not positive");*/
	if (xf instanceof RealValuedFunction
	    && yf instanceof RealValuedFunction) {
	    RealValuedFunction xxf = (RealValuedFunction) xf;
	    RealValuedFunction yyf = (RealValuedFunction) yf;
	    double tmin = xxf.getDomainMin();
	    if (tmin > yyf.getDomainMin()) tmin = yyf.getDomainMin();
	    double tmax = xxf.getDomainMax();
	    if (tmax < yyf.getDomainMax()) tmax = yyf.getDomainMax();
	    if (t1 < tmin || t1 > tmax || t2 < tmin || t2 > tmax)
		throw new IllegalArgumentException
		    (errorMsg("domainErr2", t1, t2));
	}

	int np1 = n+1;

	if (closed) {
	    if (xf.valueAt(t1) == xf.valueAt(t2) &&
		yf.valueAt(t1) == yf.valueAt(t2)) {
		// Only include one end point if both endpoints are
		// identical and the path is a closed path.
		np1--;
	    }
	}
	double[] x = new double[np1];
	double[] y = new double[np1];
	for (int i = 0; i < np1; i++) {
	    double t = t1 + ((t2 - t1) * i) / n;
	    x[i] = xf.valueAt(t);
	    y[i] = yf.valueAt(t);
	}
	makeSpline(x, y, np1, closed, true);
    }

    static int getArrayLength(double[] a1, double[] a2)
	throws IllegalArgumentException
    {
	if (a1.length == a2.length) return a1.length;
	throw new IllegalArgumentException
	    (errorMsg("arrayLengths", a1.length, a2.length));
	    /*("array lengths differ");*/
    }

    /**
     * Constructs a new SplinePath2D object from arrays of x and y coordinates
     * given a default winding rule of
     * {@link java.awt.geom.Path2D#WIND_NON_ZERO WIND_NON_ZERO}.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public SplinePath2D(double[] x, double[] y, boolean closed) {
	this(x, y, getArrayLength(x, y), closed);
    }

    /**
     * Constructs a new SplinePath2D object from arrays containing at least n
     * x and y coordinates and specifying a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule,
			double[] x, double[] y, int n, boolean closed) {
	super(rule);
	makeSpline(x, y, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from arrays of x and y coordinates
     * and specifying a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule,
			double[] x, double[] y, boolean closed) {
	this(x, y, getArrayLength(x, y), closed);
    }

    /**
     * Constructs a new SplinePath2D object from arrays containing at least n
     * x and y coordinates with the specified winding rule and initial
     * capacity for storing segments.
     * The arrays specify the x and y coordinates of the "knots" of
     * the spline - the points the spline is constrained to pass
     * through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			double[] x, double[] y, int n, boolean closed) {
	super(rule, initialCapacity);
	makeSpline(x, y, n, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from arrays of x and y coordinates
     * and specifying a winding rule.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param xf the function specifying the x coordinates to use
     * @param yf the function specifying the y coordinates to use
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, RealValuedFunctOps xf, RealValuedFunctOps yf,
			double t1, double t2, int n, boolean closed)
	throws IllegalArgumentException
    {
	super(rule);
	if (n <= 0)
	    throw new
		IllegalArgumentException
		(errorMsg("numbSegsNotPos", n));
		/*("number of segments is not positive");*/
	if (xf instanceof RealValuedFunction
	    && yf instanceof RealValuedFunction) {
	    RealValuedFunction xxf = (RealValuedFunction) xf;
	    RealValuedFunction yyf = (RealValuedFunction) yf;
	    double tmin = xxf.getDomainMin();
	    if (tmin > yyf.getDomainMin()) tmin = yyf.getDomainMin();
	    double tmax = xxf.getDomainMax();
	    if (tmax < yyf.getDomainMax()) tmax = yyf.getDomainMax();
	    if (t1 < tmin || t1 > tmax || t2 < tmin || t2 > tmax)
		throw new IllegalArgumentException
		    (errorMsg("domainErr2", t1, t2));
	}

	int np1 = n+1;

	if (closed) {
	    if (xf.valueAt(t1) == xf.valueAt(t2) &&
		yf.valueAt(t1) == yf.valueAt(t2)) {
		// Only include one end point if both endpoints are
		// identical and the path is a closed path.
		np1--;
	    }
	}
	double[] x = new double[np1];
	double[] y = new double[np1];
	for (int i = 0; i < np1; i++) {
	    double t = t1 + ((t2 - t1) * i) / n;
	    x[i] = xf.valueAt(t);
	    y[i] = yf.valueAt(t);
	}
	makeSpline(x, y, np1, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from functions giving
     * x and y coordinates with the specified winding rule and initial
     * capacity for storing segments. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param xf the function specifying the x coordinates to use
     * @param yf the function specifying the y coordinates to use
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			RealValuedFunctOps xf, RealValuedFunctOps yf,
			double t1, double t2, int n, boolean closed)
	throws IllegalArgumentException
    {
	super(rule, initialCapacity);
	if (n <= 0)
	    throw new IllegalArgumentException
		(errorMsg("numbSegsNotPos", n));
		/*("number of segments is not positive");*/
	if (xf instanceof RealValuedFunction
	    && yf instanceof RealValuedFunction) {
	    RealValuedFunction xxf = (RealValuedFunction) xf;
	    RealValuedFunction yyf = (RealValuedFunction) yf;
	    double tmin = xxf.getDomainMin();
	    if (tmin > yyf.getDomainMin()) tmin = yyf.getDomainMin();
	    double tmax = xxf.getDomainMax();
	    if (tmax < yyf.getDomainMax()) tmax = yyf.getDomainMax();
	    if (t1 < tmin || t1 > tmax || t2 < tmin || t2 > tmax)
		throw new IllegalArgumentException
		    (errorMsg("domainErr2", t1, t2));
	}
	int np1 = n+1;

	if (closed) {
	    if (xf.valueAt(t1) == xf.valueAt(t2) &&
		yf.valueAt(t1) == yf.valueAt(t2)) {
		// Only include one end point if both endpoints are
		// identical and the path is a closed path.
		np1--;
	    }
	}
	double[] x = new double[np1];
	double[] y = new double[np1];
	for (int i = 0; i < np1; i++) {
	    double t = t1 + ((t2 - t1) * i) / n;
	    x[i] = xf.valueAt(t);
	    y[i] = yf.valueAt(t);
	}
	makeSpline(x, y, np1, closed, true);
    }

    /**
     * Constructs a new SplinePath2D object from arrays containing at least n
     * x and y coordinates with the specified winding rule and initial
     * capacity for storing segments.
     * The arrays specify the x and y coordinates of the "knots" of
     * the spline - the points the spline is constrained to pass
     * through. When closed, the initial
     * point should not be repeated by the final point.
     * @param rule the winding rule ({@link Path2D#WIND_EVEN_ODD WIND_EVEN_ODD}
     *        or {@link Path2D#WIND_NON_ZERO WIND_NON_ZERO})
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     * @see java.awt.geom.Path2D#WIND_EVEN_ODD Path2D.WIND_EVEN_ODD
     * @see java.awt.geom.Path2D#WIND_NON_ZERO Path2D.WIND_NON_ZERO
     */
    public SplinePath2D(int rule, int initialCapacity,
			double[] x, double[] y,  boolean closed) {
	this(rule, initialCapacity, x, y, getArrayLength(x, y), closed);
    }


    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.).
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void splineTo(Point2D[] pk, int n) {
	splineTo(pk, n, null, null);
    }

    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of points, with initial and final control points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.).
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the spline
     * @param cpoint2 the control point just before the end of the spline
     */
    public void splineTo(Point2D[] pk, int n, Point2D cpoint1, Point2D cpoint2)
    {
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, false, false, cpoint1, cpoint2);
    }

    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of points with an offset.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.).
     * @param pk the array of points that make up the knots of a spline
     * @param offset the offset into the array for the starting point
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void splineTo(Point2D[] pk, int offset, int n) {
	splineTo(pk, offset, n, null, null);
    }
    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of points with an offset.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.).
     * @param pk the array of points that make up the knots of a spline
     * @param offset the offset into the array for the starting point
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the spline
     * @param cpoint2 the control point just before the end of the spline
     */
    public void splineTo(Point2D[] pk, int offset, int n,
			 Point2D cpoint1, Point2D cpoint2)
    {
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i+offset].getX();
	    tmpy[i] = pk[i+offset].getY();
	}
	makeSpline(tmpx, tmpy, n, false, false, cpoint1, cpoint2);
    }


    /**
     * Add a sequence of segments that form a spline, specified as arguments
     * passed to the splineTo method.
     * The arguments specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.).
     * @param points the array of points that make up the knots of a spline
     */
    public void splineTo(Point2D... points) {
	splineTo(points, points.length);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of at least n x and y coordinates.
     * The arrays specify the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot. The initial point is the one previously added to
     * the path via some other method (e.g., moveTo, lineTo, etc.).
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void splineTo(double[] x, double[]y, int n) {
	makeSpline(x, y, n, false, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of at least n x and y coordinates, with initial and final control
     * points.
     * The arrays specify the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot. The initial point is the one previously added to
     * the path via some other method (e.g., moveTo, lineTo, etc.).
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param cpoint1 the control point just after the start of the spline
     * @param cpoint2 the control point just before the end of the spline
     */
    public void splineTo(double[] x, double[]y, int n,
			 Point2D cpoint1, Point2D cpoint2) {
	makeSpline(x, y, n, false, false, cpoint1, cpoint2);
    }


    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of x and y coordinates.
     * The arrays specify the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at intermediate points. At
     * the end points, the direction of the tangent line points towards the
     * adjacent knot. The initial point is the one previously added to
     * the path via some other method (e.g., moveTo, lineTo, etc.).
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public void splineTo(double[] x, double[]y) {
	if (x.length != y.length) {
	    throw new IllegalArgumentException
		(errorMsg("arrayLengths", x.length, y.length));
		/*("array lengths differ");*/
	}
	makeSpline(x, y, x.length, false, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as functions
     * giving the x and y coordinates.
     * The "knots" of the spline - the points the spline is
     * constrained to pass through - are at evenly spaced values of
     * the argument passed to the functions, and the spline will have
     * matching first and second derivatives at intermediate
     * points. At the end points, the direction of the tangent line
     * points towards the adjacent knot. The initial point is the one
     * previously added to the path via some other method (e.g.,
     * moveTo, lineTo, etc.).  The end points at t1 and t2 are included as
     * knots.
     * <P>
     * The number of segments is one less than the number of points
     * so that n=2 implies that the function will be evaluated at t1,
     * t2, and (t1 + t2)/2.
     * @param xf the function giving the x coordinates for the path segment
     * @param yf the function giving the y coordinates for the path segment
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     */
    public void splineTo(RealValuedFunctOps xf, RealValuedFunctOps yf,
			 double t1, double t2, int n)
	throws IllegalArgumentException
    {
	if (n <= 0)
	    throw new IllegalArgumentException
		(errorMsg("numbSegsNotPos", n));
	if (xf instanceof RealValuedFunction
	    && yf instanceof RealValuedFunction) {
	    RealValuedFunction xxf = (RealValuedFunction) xf;
	    RealValuedFunction yyf = (RealValuedFunction) yf;
	    double tmin = xxf.getDomainMin();
	    if (tmin > yyf.getDomainMin()) tmin = yyf.getDomainMin();
	    double tmax = xxf.getDomainMax();
	    if (tmax < yyf.getDomainMax()) tmax = yyf.getDomainMax();
	    if (t1 < tmin || t1 > tmax || t2 < tmin || t2 > tmax)
		throw new IllegalArgumentException
		    (errorMsg("domainErr2", t1, t2));
	}

	int np1 = n+1;
	double[] x = new double[np1];
	double[] y = new double[np1];
	for (int i = 0; i < np1; i++) {
	    double t = t1 + ((t2 - t1) * i) / n;
	    x[i] = xf.valueAt(t);
	    y[i] = yf.valueAt(t);
	}
	makeSpline(x, y, np1, false, false);
    }

    /**
     * Append a spline to this path.
     * The spline is provided by a CubicSpline for the x coordinates and
     * a CubicSpline for the y coordinates.
     * @param xf the cubic spline providing the x coordinates
     * @param yf the cubic spline providing the y coordinates
     * @param connect true if the appended spline is to be connected to
     *        this path with a lineTo operation; false if a moveTo
     *        operation will be used instead
     * @exception IllegalArgumentException the splines xf and yf did
     *            not have the same number of knots
     */
    public void append(CubicSpline xf, CubicSpline yf,
		       boolean connect)
	throws IllegalArgumentException
    {
	if (countKnots(xf, yf) == 0) return;
	double[] xcoords = xf.getBernsteinCoefficients();
	double[] ycoords = yf.getBernsteinCoefficients();
	if (connect) {
	    lineTo(xcoords[0], ycoords[0]);
	} else {
	    moveTo(xcoords[0], ycoords[0]);
	}
	int n = xcoords.length;
	for (int i = 1; i < n; i += 3) {
	    curveTo(xcoords[i], ycoords[i],
		    xcoords[i+1], ycoords[i+1],
		    xcoords[i+2], ycoords[i+2]);
	}
    }


    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of points, forming a smooth closed path.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.) and there is an
     * implicit moveTo operation to this point. Element n-1 in the array
     * pk should not repeat this initial point.  The implicit moveTo may
     * result in the path containing multiple moveTo segments back to back.
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void cycleTo(Point2D[] pk, int n) {
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, true, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as
     * Point2D arguments to the cycleTo method, and forming a smooth closed
     * path.
     * The points specify the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is the one previously added to the path via
     * some other method (e.g., moveTo, lineTo, etc.). The last argument
     * should not repeat this initial point.
     * @param points the  points that make up the knots of a spline
     */
    public void cycleTo(Point2D... points) {
	cycleTo(points, points.length);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of at least n x and y coordinates, forming a smooth closed path.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will
     * have matching first and second derivatives at all points.  The
     * initial point is the one previously added to the path via some
     * other method (e.g., moveTo, lineTo, etc.) and there is an
     * implicit moveTo operation to this point. The point specified by
     * the array index n-1 should not repeat this initial point.  The
     * implicit moveTo may result in the path containing multiple
     * moveTo segments back to back.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void cycleTo(double[] x, double[] y, int n) {
	makeSpline(x, y, n, true, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of x and y coordinates, forming a smooth closed path.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will
     * have matching first and second derivatives at all points.  The
     * initial point is the one previously added to the path via some
     * other method (e.g., moveTo, lineTo, etc.) and there is an
     * implicit moveTo operation to this point. The point specified by
     * the last elements in the x and y arrays should not repeat this
     * initial point.  The implicit moveTo may result in the path
     * containing multiple moveTo segments back to back.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public void cycleTo(double[] x, double[] y) {
	if (x.length != y.length)
	    throw new IllegalArgumentException
		(errorMsg("arrayLengths", x.length, y.length));
		/*("array lengths differ");*/
	makeSpline(x, y, x.length, true, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as
     * functions giving the x and y coordinates of points along the
     * path and forming a smooth closed path.
     * <P>
     * The "knots" of the spline - the points the spline is
     * constrained to pass through - are are at evenly spaced values
     * of the argument passed to the functions, and the spline will
     * have matching first and second derivatives at all points.  The
     * initial point is the one previously added to the path via some
     * other method (e.g., moveTo, lineTo, etc.) and there is an
     * implicit moveTo operation to this point.  The end points at t1
     * and t2 are included as knots. The end point at t2 should not
     * repeat the initial point.  The implicit moveTo may result in
     * the path containing multiple moveTo segments back to back.
     * <P>
     * The number of segments is one less than the number of points
     * so that n=2 implies that the function will be evaluated at t1,
     * t2, and (t1 + t2)/2.
     * @param xf the function giving the x coordinates for the path segment
     * @param yf the function giving the y coordinates for the path segment
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between points at which to
     *          evaluate the functions
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     */
    public void cycleTo(RealValuedFunctOps xf, RealValuedFunctOps yf,
			 double t1, double t2, int n)
	throws IllegalArgumentException
    {
	if (n <= 0)
	    throw new
		IllegalArgumentException(errorMsg("numbSegsNotPos", n));
	if (xf instanceof RealValuedFunction
	    && yf instanceof RealValuedFunction) {
	    RealValuedFunction xxf = (RealValuedFunction) xf;
	    RealValuedFunction yyf = (RealValuedFunction) yf;
	    double tmin = xxf.getDomainMin();
	    if (tmin > yyf.getDomainMin()) tmin = yyf.getDomainMin();
	    double tmax = xxf.getDomainMax();
	    if (tmax < yyf.getDomainMax()) tmax = yyf.getDomainMax();
	    if (t1 < tmin || t1 > tmax || t2 < tmin || t2 > tmax)
		throw new IllegalArgumentException
		    (errorMsg("domainErr2", t1, t2));
	}

	int np1 = n+1;
	double[] x = new double[np1];
	double[] y = new double[np1];
	for (int i = 0; i < np1; i++) {
	    double t = t1 + ((t2 - t1) * i) / n;
	    x[i] = xf.valueAt(t);
	    y[i] = yf.valueAt(t);
	}
	makeSpline(x, y, np1, true, false);
    }

    /**
     * Add a sequence of segments that form a spline, specified as an array
     * of at least n points, forming a closed path starting at index 0.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is pk[0] and there is an implicit
     * moveTo operation for this point, which should not be repeated
     * as the element at index n-1 of the array provided as this method's
     * first argument.
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void addCycle(Point2D[] pk, int n) {
	double[] tmpx = new double[n];
	double[] tmpy = new double[n];
	for (int i = 0; i < n; i++) {
	    tmpx[i] = pk[i].getX();
	    tmpy[i] = pk[i].getY();
	}
	makeSpline(tmpx, tmpy, n, true, true);
    }

    /**
     * Add a sequence of segments that form a spline, specified as
     * Point2D arguments to the addCycle method, and forming a closed
     * path starting at index 0.
     * The points specify the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is the one specified by the first argument
     * and there is an implicit moveTo operation for this point, which
     * should not be repeated by the last argument.
     * @param points the  points that make up the knots of a spline
     */
    public void addCycle(Point2D... points) {
	addCycle(points, points.length);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of at least n x and y coordinates, forming a closed path starting
     * at index 0.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is (x[0], y[0]) and there is an implicit
     * moveTo operation for this point, which should not be repeated by
     * the point specified by the array arguments at index n-1.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     */
    public void addCycle(double[] x, double[]y, int n) {
	makeSpline(x, y, n, true, true);
    }

    /**
     * Add a sequence of segments that form a spline, specified as arrays
     * of x and y coordinates, forming a closed path starting at index 0.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through - and the spline will have
     * matching first and second derivatives at all points.
     * The initial point is (x[0], y[0]) and there is an implicit
     * moveTo operation for this point, which should not be repeated by
     * the point specified by the last arguments of the x and y arrays.
     * @param x the x coordinates to use, one for each knot
     * @param y the y coordinates to use, one for each knot
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public void addCycle(double[] x, double[]y) {
	if (x.length != y.length)
	    throw new IllegalArgumentException
		(errorMsg("arrayLengths", x.length, y.length));
		/*("array lengths differ");*/
	makeSpline(x, y, x.length, true, true);
    }

    private void makeSpline(double[] x, double[] y, int n,
			    boolean cyclic, boolean startsWithMoveTo)
	throws IllegalArgumentException
    {
	makeSpline(x, y, n, cyclic, startsWithMoveTo, null, null);
    }

    private enum EndMode {
	NONE,
	START,
	END,
	BOTH
    }

    private static void reverse(double[] array) {
	reverse(array, array.length);
    }

    private static void reverse(double[] array, int n) {
	int n2 = n/2;
	int j = n-1;
	for (int i = 0; i < n2; i++, j--) {
	    double tmp = array[i];
	    array[i] = array[j];
	    array[j] = tmp;
	}
    }


    private void makeSpline(double[] x, double[] y, int n,
			    boolean cyclic, boolean startsWithMoveTo,
			    Point2D cpoint1, Point2D cpoint2)
	throws IllegalArgumentException
    {
	double finalX = java.lang.Double.NaN;
	double finalY = java.lang.Double.NaN;

	if (cyclic && (cpoint1 != null || cpoint2 != null)) {
	    throw new IllegalArgumentException(errorMsg("cyclic"));
	}

	EndMode endMode = (cpoint1 == null && cpoint2 == null)? EndMode.NONE:
	    (cpoint1 != null && cpoint2 == null)? EndMode.START:
	    (cpoint1 == null && cpoint2 != null)? EndMode.END:
	    EndMode.BOTH;

	// For small values of n, the best we can do is to draw straight
	// lines.
	if (n == 2 && startsWithMoveTo) {
	    moveTo(x[0], y[0]);
	    lineTo(x[1], y[1]);
	    if (cyclic) {
		lineTo(x[0], y[0]);
	    }
	    return;
	} else if (n == 1) {
	    if (startsWithMoveTo) {
		moveTo(x[0], y[0]);
	    } else {
		Point2D cpnt = getCurrentPoint();
		finalX = cpnt.getX();
		finalY = cpnt.getY();
		if (cyclic) {
		    moveTo(finalX, finalY);
		}
		lineTo(x[0], y[0]);
		if (cyclic) {
		    lineTo(finalX, finalY);
		}
	    }
	    return;
	}

	Point2D cpoint = null;
	boolean reverse = false;
	boolean merge = false;
	int  oldN = n;
	switch (endMode) {
	case START:
	    reverse = false;
	    cpoint = cpoint1;
	    break;
	case END:
	    reverse = true;
	    cpoint = cpoint2;
	    break;
	case BOTH:
	    merge = true;
	    reverse = true;
	    cpoint = cpoint2;
	    break;
	default:
	    break;
	}

	double[] a = createA(n, cyclic, startsWithMoveTo, cpoint/*1, cpoint2*/);
	double[] b = createB(n, cyclic, startsWithMoveTo, cpoint/*1, cpoint2*/);
	double[] c = createC(n, cyclic, startsWithMoveTo, cpoint/*1, cpoint2*/);
	double[] wx = getw(x, n, cyclic, startsWithMoveTo, true,
			   cpoint/*1, cpoint2*/, reverse);
	double[] wy = getw(y, n, cyclic, startsWithMoveTo, false,
			   cpoint/*1, cpoint2*/, reverse);
	if (cyclic) {
	    TridiagonalSolver.solveCyclic(wx, a, b, c, wx, wx.length);
	    TridiagonalSolver.solveCyclic(wy, a, b, c, wy, wy.length);
	} else {
	    TridiagonalSolver.solve(wx, a, b, c, wx);
	    TridiagonalSolver.solve(wy, a, b, c, wy);
	}


	// int m = x.length;
	// int mm1 = m-1;

	n = wx.length;
	int nm1 = n-1;
	int nm2 = n-2;

	double wx1[] = null;
	double wy1[] = null;
	double wx2[] = null;
	double wy2[] = null;
	int offset = 0;
	if (reverse) {
	    wx1 = new double[wx.length];
	    wy1 = new double[wy.length];
	    reverse(x);
	    reverse(y);
	    for (int i = 0; i < nm1; i++) {
		wx1[i] = 2*x[i+1] - wx[i+1];
		wy1[i] = 2*y[i+1] - wy[i+1];
	    }
	    if (startsWithMoveTo) {
		wx1[nm1] = (x[n] + wx[nm1])/2.0;
		wy1[nm1] = (y[n] + wy[nm1])/2.0;
	    } else {
		Point2D cpnt = getCurrentPoint();
		wx1[nm1] = (cpnt.getX() + wx[nm1])/2.0;
		wy1[nm1] = (cpnt.getY() + wy[nm1])/2.0;
	    }
	    reverse(x);
	    reverse(y);
	    reverse(wx1);
	    reverse(wy1);
	    reverse(wx);
	    reverse(wy);
	    wx2 = wx;
	    wy2 = wy;
	}
	if (merge) {
	    wx = getw(x, oldN, cyclic, startsWithMoveTo, true,
		      cpoint1/*1, cpoint2*/, false);
	    wy = getw(y, oldN, cyclic, startsWithMoveTo, false,
		      cpoint1/*1, cpoint2*/, false);
	    TridiagonalSolver.solve(wx, a, b, c, wx);
	    TridiagonalSolver.solve(wy, a, b, c, wy);
	}

	/*
	System.out.print("wx: ");
	for (int k = 0; k < wx.length; k++) {
	    System.out.format("%g ", wx[k]);
	}
	System.out.println();
	System.out.print("wy: ");
	for (int k = 0; k < wx.length; k++) {
	    System.out.format("%g ", wy[k]);
	}
	System.out.println();
	*/
	double p1x, p1y, p2x, p2y, p3x, p3y;
	offset = 0;
	if (startsWithMoveTo) {
	    // n is x.length - 1
	    if (reverse) {
		offset = 0; // change?
		moveTo(x[0], y[0]);
	    } else {
		moveTo(x[0], y[0]);
		finalX = x[0];
		finalY = y[0];
	    }
	} else {
	    if (reverse) {
		offset = -1; // change?
	    } else {
		offset = -1;
	    }
	    Point2D cpnt = getCurrentPoint();
	    finalX = cpnt.getX();
	    finalY = cpnt.getY();
	    if (cyclic) moveTo(finalX, finalY);
	}
	// System.out.format("(wx[0], wy[0]) = (%g,%g)\n", wx[0], wy[0]);
        double u = 0;
        double up = 1;
	double v = u, vp = up;
	for (int i = 0; i < nm1; i++) {
	    v = u;
	    vp = up;
	    u = ((double)(i+1))/n;
	    up = 1-u;
	    if (merge) {
		double p1x1 = wx1[i];
		double p1y1 = wy1[i];
		double p2x1 = wx2[i];
		double p2y1 = wy2[i];
		double p1x2 = wx[i];
		double p1y2 = wy[i];
		double p2x2 = 2.0 * x[i+1 + offset] - wx[i+1];
		double p2y2 = 2.0 * y[i+1 + offset] - wy[i+1];
		/*
		p1x = (i == 0)? p1x2: (p1x1 + p1x2)/2.0;
		p1y = (i == 0)? p1y2: (p1y1 + p1y2)/2.0;
		p2x = (p2x1 + p2x2)/2.0;
		p2y = (p2y1 + p2y2)/2.0;
		*/
		p1x = v*p1x1 + vp*p1x2;
		p1y = v*p1y1 + vp*p1y2;
		p2x = u*p2x1 + up*p2x2;
		p2y = u*p2y1 + up*p2y2;
	    } else if (reverse) {
		p1x = wx1[i];
		p1y = wy1[i];
		p2x = wx[i];
		p2y = wy[i];
	    } else {
		p1x = wx[i];
		p1y = wy[i];
		p2x = 2.0 * x[i+1 + offset] - wx[i+1];
		p2y = 2.0 * y[i+1 + offset] - wy[i+1];
		// p3x = x[i+1 + offset];
		// p3y = y[i+1 + offset];
	    }
	    p3x = x[i+1 + offset];
	    p3y = y[i+1 + offset];
	    curveTo(p1x, p1y, p2x, p2y, p3x, p3y);
	}
	if (cyclic) {
	    p1x = wx[nm1];
	    p1y = wy[nm1];
	    p2x = 2.0 * /*x[0]*/finalX - wx[0];
	    p2y = 2.0 * /*y[0]*/finalY - wy[0];
	    p3x = /*x[0]*/ finalX;
	    p3y = /*y[0]*/ finalY;
	    curveTo(p1x, p1y, p2x, p2y, p3x, p3y);
	    closePath();
	} else {
	    if (merge) {
		double p1x1 = wx1[nm1];
		double p1y1 = wy1[nm1];
		p2x = wx2[nm1];
		p2y = wy2[nm1];
		double p1x2 = wx[nm1];
		double p1y2 = wy[nm1];
		/*
		p1x = (p1x1 + p1x2)/2;
		p1y = (p1y1 + p1y2)/2;
		*/
		p1x = v*p1x1 + vp*p1x2;
		p1y = v*p1y1 + vp*p1y2;
	    } else if (reverse) {
		p1x = wx1[nm1];
		p1y = wy1[nm1];
		p2x = wx[nm1];
		p2y = wy[nm1];
	    } else {
		p1x = wx[nm1];
		p1y = wy[nm1];
		p2x = (x[n + offset] + p1x)/2.0;
		p2y = (y[n + offset] + p1y)/2.0;
	    }
	    p3x = x[n + offset];
	    p3y = y[n + offset];
	    curveTo(p1x, p1y, p2x, p2y, p3x, p3y);
	}
    }
}

//  LocalWords:  exbundle eacute zier splineTo addCycle moveTo href
//  LocalWords:  curveTo uP ul li boolean IllegalArgumentException xf
//  LocalWords:  startsWithMoveto SplinePath initialCapacity indices
//  LocalWords:  AffineTransform splineLengthsDiffer yf domainErr
//  LocalWords:  numbSegsNotPos arrayLengths lineTo CubicSpline
//  LocalWords:  cycleTo missingMOVETO cpoint spline's rescaled
