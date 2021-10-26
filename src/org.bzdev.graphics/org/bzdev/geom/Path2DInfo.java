package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.io.AppendableWriter;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;
import org.bzdev.math.BezierPolynomial;
import org.bzdev.math.CubicSpline;
import org.bzdev.math.Functions;
import org.bzdev.math.GLQuadrature;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.RootFinder;
import org.bzdev.math.VectorOps;
import org.bzdev.util.ArrayMerger;
import org.bzdev.util.PrimArrays;
import org.bzdev.util.IntComparator;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.Writer;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class providing static methods to obtain information about paths
 * defined by java.awt.geom.Path2D.
 * <P>
 * The method {@link Path2DInfo#shiftClosedPath(Path2D,double,double)}
 * returns a modified path (the same path as its argument but with its
 * starting point shifted).  The classes {@link Paths2D} and
 * {@link PathSplitter} will allow various paths to be constructed.
 */
public class Path2DInfo {

    // just static
    private Path2DInfo() {}

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static class MergedIterator implements PathIterator {
	int index = 0;
	double[] pcoords;

	MergedIterator(double[]coords) {
	    pcoords = coords;
	}

	@Override
	public int getWindingRule() {
	    return PathIterator.WIND_NON_ZERO;
	}

	@Override
	public void next() {
	    if (index == 0) index = 2;
	    index += 6;
	}

	@Override
	public boolean isDone() {
	    return index >= pcoords.length;
	}

	@Override
	public int currentSegment(double[] coords) {
	    int len = (index == 0)? 2: 6;
	    System.arraycopy(pcoords, index, coords, 0, len);
	    return (index == 0)? PathIterator.SEG_MOVETO:
		PathIterator.SEG_CUBICTO;
	}

	@Override
	public int currentSegment(float[] coords) {
	    int len = (index == 0)? 2: 6;
	    for (int i = 0; i < len; i++) {
		coords[i] = (float)pcoords[index+i];
	    }
	    return (index == 0)? PathIterator.SEG_MOVETO:
		PathIterator.SEG_CUBICTO;
	}
    }

    /**
     * Get a path iterator for the path implied by two cubic splines.
     * The splines must have the same number of knots.
     * @param af the affine transform to apply to the path iterator;
     *        null is equivalent to an identity transform
     * @param x the cubic spline for the X coordinates
     * @param y the cubic spline for the Y coordinates
     */
    public static PathIterator getPathIterator(AffineTransform af,
					       CubicSpline x, CubicSpline y)
    {
	double[] coords = ArrayMerger.merge(x.getBernsteinCoefficients(),
					    y.getBernsteinCoefficients());
	int n = coords.length/2;
	if (af != null) {
	    af.transform(coords, 0, coords, 0, n);
	}
	return new MergedIterator(coords);
    }

    /**
     * Elevate the degree of a two-dimensional B&eacute;zier curve by 1.
     * The algorithm is describe in
     * <A href="https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-elev.html">Degree Elevation of a B&eacute;zier Curve</A>.
     * The length of the coords array must be at least 2*(degree+1), whereas
     * the length of the result array must be at least 2*(degree+2).
     * <P>
     * Control points are stored so that the X and Y coordinates
     * for control point (i/2) are in array locations i and i+1 respectively,
     * where i = 0 (mod 2).
     * @param degree the degree of the original curve
     * @param result an array storing the control points for an
     *        the new curve
     * @param rOffset the offset into the result array at which the
     *        control points for the new curve will be stored
     * @param coords the array containing the control points for the
     *        original curve
     * @param cOffset the offset into the coords array at which the
              control points for the original curve start
     */
    public static void elevateDegree(int degree, double[] result, int rOffset,
				     double[] coords, int cOffset)
    {
	int n = 2*degree;
	result[rOffset] = coords[cOffset];
	result[rOffset+1] = coords[cOffset+1];
	result[rOffset+n+2] = coords[cOffset+n];
	result[rOffset+n+3] = coords[cOffset+n+1];

	double ratio = 1.0/(degree+1);
	n += 2;

	for (int i = 2; i < n; i++) {
	    double term1 = (i/2)*ratio;
	    double term2 = 1.0 - term1;
	    int ii = i + cOffset;
	    result[i+rOffset] = coords[ii-2]*term1
		+ coords[ii]*term2;
	}
    }

    /**
     * Elevate the degree of a two-dimensional B&eacute;zier curve of degree
     * n by 1,
     * specifying the last n control points of the original curve in an array.
     * The length of the result array must be at least 2*(degree+1), whereas the
     * length of the coords array must be at least 2*degree.
     * The algorithm is describe in
     * <A href="https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-elev.html">Degree Elevation of a B&eacute;zier Curve</A>.
     * <P>
     * Control points are stored so that the X and Y coordinates
     * for control point (i/2)+1 are in array locations i and i+1 respectively,
     * where i = 0 (mod 2).
     * @param degree the degree of the curve
     * @param result an array storing the last n control points for
     *        the new curve, which excludes its starting point
     * @param x the X coordinate for the start of the curve (control point 0)
     * @param y the Y coordinate for the start of the curve (control point 0)
     * @param coords the array containing the last n-1 control points for the
     *        original curve, which excludes its starting point
     */
    public static void elevateDegree(int degree, double[] result,
				     double x, double y,
				     double[] coords)
    {
	int n = 2*degree;
	result[n] = coords[n-2];
	result[n+1] = coords[n-1];

	double ratio = 1.0/(degree+1);
	double term1 = ratio;
	double term2 = 1.0 - term1;
	result[0] = x*term1 + coords[0]*term2;
	result[1] = y*term1 + coords[1]*term2;

	for (int i = 2; i < n; i++) {
	    term1 = ((i+2)/2)*ratio;
	    term2 = 1.0 - term1;
	    result[i] = coords[i-2]*term1 + coords[i]*term2;
	}
    }

    private static boolean insideHull(double x0, double y0,
				      double[]coords, int aclen,
				      double x, double y)
    {
	double cx, cy, v1x, v1y, v2x, v2y, cprod, ulpcp;
	double fx = x0;
	double fy = y0;
	for (int i = 0; i < aclen; i += 2) {
	    cx = coords[i];
	    cy = coords[i+1];
	    v1x = cx - x0;
	    v1y = cy - y0;
	    v2x = x - x0;
	    v2y = y - y0;
	    cprod = v1x*v2y - v2x*v1y;
	    ulpcp = Math.abs(v1x*Math.ulp(v2y))
		+ Math.abs(v2y*Math.ulp(v1x))
		+ Math.abs(v2x*Math.ulp(v1y))
		+ Math.abs(v1y*Math.ulp(v2x));
	    ulpcp *= 16.0;
	    if (!(cprod > ulpcp)) {
		return false;
	    }
	    x0 = cx;
	    y0 = cy;
	}
	v1x = fx - x0;
	v1y = fy - y0;
	v2x = x - x0;
	v2y = y - y0;
	cprod = v1x*v2y - v2x*v1y;
	ulpcp = Math.abs(v1x*Math.ulp(v2y))
	    + Math.abs(v2y*Math.ulp(v1x))
	    + Math.abs(v2x*Math.ulp(v1y))
	    + Math.abs(v1y*Math.ulp(v2x));
	ulpcp *= 16.0;
	if (!(cprod > ulpcp)) {
	    return false;
	}
	return true;
    }

    /**
     * List the control points of a path.
     * @param path the path
     * @param all true if all control points are included; false if
     *        only the control points starting or ending a segment are
     *        included
     * @return an array containing the control points, each as
     *         a pair of values
     */
    public static double[] getControlPoints(Path2D path, boolean all)
    {
	return getControlPoints(path.getPathIterator(null), all);
    }


    /**
     * List the control points of a path specified by a path iterator.
     * @param pit the path iterator providing the control points
     * @param all true if all control points are included; false if
     *        only the control points starting or ending a segment are
     *        included
     * @return an array containing the control points, each as
     *         a pair of values
     */
    public static double[] getControlPoints(PathIterator pit,
					    boolean all)
    {
	ArrayList<Double> list = new ArrayList<>();
	double[] coords = new double[6];
	double startx = 0.0, starty = 0.0;
	double lastx = 0.0, lasty = 0.0;
	boolean sawClose = false;
	while(!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		startx = coords[0];
		starty = coords[1];
	    case PathIterator.SEG_LINETO:
		list.add(coords[0]);
		list.add(coords[1]);
		lastx = coords[0];
		lasty = coords[1];
		sawClose = false;
		break;
	    case PathIterator.SEG_QUADTO:
		if (all) {
		    list.add(coords[0]);
		    list.add(coords[1]);
		}
		list.add(coords[2]);
		list.add(coords[3]);
		lastx = coords[2];
		lasty = coords[3];
		sawClose = false;
		break;
	    case PathIterator.SEG_CUBICTO:
		if (all) {
		    list.add(coords[0]);
		    list.add(coords[1]);
		    list.add(coords[2]);
		    list.add(coords[3]);
		}
		list.add(coords[4]);
		list.add(coords[5]);
		lastx = coords[4];
		lasty = coords[5];
		sawClose = false;
		break;
	    case PathIterator.SEG_CLOSE:
		if (sawClose == false && lastx == startx && lasty == starty) {
		    int sz = list.size();
		    list.remove(sz-1);
		    list.remove(sz-2);
		}
		sawClose = true;
		break;
	    default:
		throw new UnexpectedExceptionError();
	    }
	    pit.next();
	}
	double[] results = new double[list.size()];
	int i = 0;
	for (Double d: list) {
	    results[i++] = d.doubleValue();
	}
	return results;
    }

    /**
     * Get a polygon connecting a path's control points.
     * @param path the path to process
     * @param all true if all control points should be included;
     *        false for just the 'knots' (the end-points of each
     *        segment)
     * @param af an affine transform to apply the path before
     *        processing; null for an identity transform
     * @return a path consisting of straight-line  segments
     */
    public static Path2D controlPointPolygon(Path2D path, boolean all,
					     AffineTransform af) {
	PathIterator pi = path.getPathIterator(af);
	Path2D newPath = new Path2D.Double();
	double[] coords = new double[6];
	while (!pi.isDone()) {
	    switch (pi.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		newPath.moveTo(coords[0], coords[1]);
		break;
	    case PathIterator.SEG_LINETO:
		newPath.lineTo(coords[0], coords[1]);
		break;
	    case PathIterator.SEG_QUADTO:
		if (all) {
		    newPath.lineTo(coords[0], coords[1]);
		}
		newPath.lineTo(coords[2], coords[3]);
		break;
	    case PathIterator.SEG_CUBICTO:
		if (all) {
		    newPath.lineTo(coords[0], coords[1]);
		    newPath.lineTo(coords[2], coords[3]);
		}
		newPath.lineTo(coords[4], coords[5]);
		break;
	    case PathIterator.SEG_CLOSE:
		newPath.closePath();
		break;
	    }
	    pi.next();
	}
	return newPath;
    }

    /**
     * Compute the convex hull of a set of control points.
     * The control points are stored in an array. This allows a convex
     * hull to be computed using the arrays returned by
     * {@link java.awt.geom.PathIterator}.  Note: the implementation
     * uses the gift-wrapping algorithm for trivial cases (see <A
     * href="https://en.wikipedia.org/wiki/Gift_wrapping_algorithm">
     * the Wikipedia description of this algorithm</A>). For n &ge; 2,
     * <A
     * href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain#Pseudo-code">Andrew's
     * monotone chain convex hull algorithm</A> is used (the
     * implementation is quite different than the Java code shown in
     * this citation).  For n&gt;384, the
     * <A href="https://en.wikipedia.org/wiki/Convex_hull_algorithms#Akl%E2%80%93Toussaint_heuristic">
     * Aki-Toussaint heuristic</A> is used with an octagon estimate of
     * the convex hull.  <P> The path that is returned is oriented so
     * that each successive edge along the convex hull turns counter
     * clockwise (e.g, from the positive X axis towards the positive Y
     * axis) when axes use the the standard mathematical convention in
     * which the positive X axis points right and the positive Y axis
     * points up.
     * @param coords the coordinates of the remaining control points,
     *        listed with X values of a point immediately followed by
     *        the Y value of the same point
     * @param offset the offset into the array coords where the data
     *        starts;
     * @param n the number of control points in the array (for B&eacute;zier
     *        curves, this is one more than the degree of the curve)
     * @return a path whose segments are straight lines, providing the
     *         convex hull oriented so that the path is counterclockwise
     *         (turning from increasing X towards increasing Y)
     */
    public static Path2D convexHull(double[] coords, int offset, int n)
    {
	return convexHull(coords[offset], coords[offset+1],
			  coords, offset+2, n-1);
    }

    /**
     * Compute the convex hull of a set of control points.
     * The initial control point is given explicitly by the
     * x and y arguments and the remaining control points are
     * stored in an array. This allows a convex hull to be computed
     * using the arrays returned by {@link java.awt.geom.PathIterator}.
     * Note: the implementation uses the gift-wrapping
     * algorithm for trivial cases (see
     * <A href="https://en.wikipedia.org/wiki/Gift_wrapping_algorithm">
     * the Wikipedia description of this algorithm</A>). For n &ge; 2,
     * <A href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain#Pseudo-code">Andrew's monotone chain convex
     * hull algorithm</A> is used (the implementation is quite different
     * than the Java code shown in this citation).  For n&gt;384, the
     * <A href="https://en.wikipedia.org/wiki/Convex_hull_algorithms#Akl%E2%80%93Toussaint_heuristic">
     * Aki-Toussaint heuristic</A> is used with an octagon estimate of
     * the convex hull.
     * <P>
     * The path that is returned is oriented so that each successive
     * edge along the convex hull turns counter clockwise (e.g, from
     * the positive X axis towards the positive Y axis) when axes
     * use the the standard mathematical convention in which the
     * positive X axis points right and the positive Y axis points up.
     * @param x the X coordinate of the initial control point
     * @param y the Y coordinate of the initial control point
     * @param coords the coordinates of the remaining control points,
     *        listed with X values of a point immediately followed by
     *        the Y value of the same point
     * @param n the number of control points in the array (for B&eacute;zier
     *        curves, this is the degree of the curve)
     * @return a path whose segments are straight lines, providing the
     *         convex hull oriented so that the path is counterclockwise
     *         (turning from increasing X towards increasing Y)
     */
    public static Path2D convexHull(double x, double y, double[] coords, int n)
    {
	return convexHull(x, y, coords, 0, n);
    }
    /**
     * Compute the convex hull of a set of control points.
     * The initial control point is given explicitly by the
     * x and y arguments and the remaining control points are
     * stored in an array. This allows a convex hull to be computed
     * using the arrays returned by {@link java.awt.geom.PathIterator}.
     * Note: the implementation uses the gift-wrapping
     * algorithm for trivial cases (see
     * <A href="https://en.wikipedia.org/wiki/Gift_wrapping_algorithm">
     * the Wikipedia description of this algorithm</A>). For n &ge; 2,
     * <A href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain#Pseudo-code">Andrew's monotone chain convex
     * hull algorithm</A> is used (the implementation is quite different
     * than the Java code shown in this citation).  For n&gt;384, the
     * <A href="https://en.wikipedia.org/wiki/Convex_hull_algorithms#Akl%E2%80%93Toussaint_heuristic">
     * Aki-Toussaint heuristic</A> is used with an octagon estimate of
     * the convex hull.
     * <P>
     * The path that is returned is oriented so that each successive
     * edge along the convex hull turns counter clockwise (e.g, from
     * the positive X axis towards the positive Y axis) when axes
     * use the the standard mathematical convention in which the
     * positive X axis points right and the positive Y axis points up.
     * @param x the X coordinate of the initial control point
     * @param y the Y coordinate of the initial control point
     * @param coords the coordinates of the remaining control points,
     *        listed with X values of a point immediately followed by
     *        the Y value of the same point
     * @param offset the offset into the array coords where the data
     *        starts;
     * @param n the number of control points in the array (for B&eacute;zier
     *        curves, this is the degree of the curve)
     * @return a path whose segments are straight lines, providing the
     *         convex hull oriented so that the path is counterclockwise
     *         (turning from increasing X towards increasing Y)
     */
    public static Path2D convexHull(double x, double y, double[] coords,
				    int offset, int n)
    {
	// psuedocode: https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	if (n < 0) {
	    throw new IllegalArgumentException(errorMsg("fourthArgNeg"));
	}
	if (coords.length - offset < 2*n) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	// See
	// https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	// for Andrew's monotone chain convex hull algorithm

	if (n >= 2) {
	    // use Andrew's monotone chain convex hull algorithm
	    int[] points = new int[n+1];
	    for (int i = 0; i < points.length; i++) {
		points[i] = i-1;
	    }
	    if (n > 384) {
		// Aki-Toussaint heuristic using an octagon boundary.
		int minXInd = -1;
		int maxXInd = -1;
		int minYInd = -1;
		int maxYInd = -1;
		int minXpYInd = -1;
		int maxXpYInd = -1;
		int minXmYInd = -1;
		int maxXmYInd = -1;
		for (int i = 1; i < points.length; i++) {
		    int ival = points[i];
		    int i2 = ival*2;
		    double atx2= coords[offset + i2];
		    double aty2 = coords[offset + i2+1];
		    int ii2 = minXInd*2;
		    double atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    if (atx2 < atx1) minXInd = ival;
		    ii2 = maxXInd*2;
		    atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    if (atx1 < atx2) maxXInd = ival;
		    ii2 = minYInd*2;
		    double aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if (aty2 < aty1) minYInd = ival;
		    ii2 = maxYInd*2;
		    aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if (aty1 < aty2) maxYInd = ival;
		    ii2 = minXpYInd*2;
		    atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if ((atx2+aty2) < (atx1+aty1)) minXpYInd = ival;
		    ii2 = maxXpYInd*2;
		    atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if ((atx1+aty1) < (atx2+aty2)) maxXpYInd = ival;
		    ii2 = minXmYInd*2;
		    atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if ((atx2-aty2) < (atx1-aty1)) minXmYInd = ival;
		    ii2 = maxXmYInd*2;
		    atx1 = (ii2 == -2)? x: coords[offset + ii2];
		    aty1 = (ii2 == -2)? y: coords[offset + ii2+1];
		    if ((atx1-aty1) < (atx2-aty2)) maxXmYInd = ival;
		}
		int atind = 2*minXInd;
		double atx0 = (atind == -2)? x: coords[offset + atind];
		double aty0 = (atind == -2)? y: coords[offset + atind+1];
		double[] atcoords = new double[14];
		int ind = 0;
		atind = 2*minXpYInd;
		double atx = (atind == -2)? x: coords[offset + atind];
		double aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*minYInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*maxXmYInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*maxXInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*maxXpYInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*maxYInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		atind = 2*minXmYInd;
		atx = (atind == -2)? x: coords[offset + atind];
		aty = (atind == -2)? y: coords[offset + atind+1];
		atcoords[ind++] = atx;
		atcoords[ind++] = aty;
		Path2D atpath = convexHull(atx0, aty0, atcoords, 7);
		PathIterator pi = atpath.getPathIterator(null);
		double[] pcoords = new double[6];
		ind = 0;
		while (!pi.isDone()) {
		    switch(pi.currentSegment(pcoords)) {
		    case PathIterator.SEG_MOVETO:
			atx0 = pcoords[0];
			aty0 = pcoords[1];
			break;
		    case PathIterator.SEG_LINETO:
			atcoords[ind++] = pcoords[0];
			atcoords[ind++] = pcoords[1];
			break;
		    default:
			break;
		    }
		    pi.next();
		}
		int aclen = ind;
		ind = 0;
		for (int i = 0; i < points.length; i++) {
		    int i2 = points[i]*2;
		    double tpx = (i2 == -2)? x: coords[offset + i2];
		    double tpy = (i2 == -2)? y: coords[offset + i2+1];
		    if (insideHull(atx0, aty0, atcoords, aclen, tpx, tpy)) {
			points[i] = -2;
			ind++;
		    }
		}
		int[] newpoints = new int[points.length - ind];
		ind = 0;
		for (int i = 0; i < points.length; i++) {
		    if (points[i] == -2) continue;
		    newpoints[ind++] = points[i];
		}
		points = newpoints;
	    }
	    PrimArrays.sort(points, new IntComparator() {
		    public int compare(int ind1, int ind2) {
			if (ind1 == -1) {
			    if (ind2 == -1) {
				return 0;
			    } else {
				int ind22 = ind2*2;
				double x2 = coords[offset + ind22];
				if (x == x2) {
				    double y2 = coords[offset + ind22+1];
				    if (y == y2) {
					return 0;
				    } else if (y < y2) {
					return -1;
				    } else {
					return 1;
				    }
				} else if (x < x2) {
				    return -1;
				} else {
				    return 1;
				}
			    }
			} else {
			    int ind12 = ind1*2;
			    double x1 = coords[offset + ind12];
			    if (ind2 == -1) {
				if (x1 == x) {
				    double y1 = coords[offset + ind12+1];
				    if (y1 == y) {
					return 0;
				    } else if (y1 < y) {
					return -1;
				    } else {
					return 1;
				    }
				} else if (x1 < x) {
				    return -1;
				} else {
				    return 1;
				}
			    } else {
				int ind22 = 2*ind2;
				double x2 = coords[offset + ind22];
				if (x1 == x2)  {
				    double y1 = coords[offset + ind12+1];
				    double y2 = coords[offset + ind22+1];
				    if (y1 == y2) {
					return 0;
				    } else if (y1 < y2) {
					return -1;
				    } else {
					return 1;
				    }
				} else if (x1 < x2) {
				    return -1;
				} else {
				    return 1;
				}
			    }
			}
		    }
		    public boolean equals(Object obj) {
			return this == obj;
		    }
		});
	    /*
	    for (int i = 0; i < points.length; i++) {
		int ind = points[i];
		int ind2 = ind*2;
		double x4 = (ind == -1)? x: coords[offset + ind2];
		double y4 = (ind == -1)? y: coords[offset + ind2+1];
	    }
	    */
	    int[] upper = new int[points.length];
	    int[] lower = new int[points.length];
	    int topUpper = 0;
	    int topLower = 0;
	    for (int i = 0; i < points.length; i++) {
		while (topLower > 1) {
		    int ind1 = lower[topLower-2];
		    int ind2 = lower[topLower-1];
		    int ind12 = ind1*2;
		    int ind22 = ind2*2;
		    int ii = points[i];
		    int ii2 = ii*2;
		    double x0 = (ind1 == -1)? x: coords[offset + ind12];
		    double y0 = (ind1 == -1)? y: coords[offset + ind12+1];
		    double x1 = (ind2 == -1)? x: coords[offset + ind22];
		    double y1 = (ind2 == -1)? y: coords[offset + ind22+1];
		    double x2 = (ii == -1)? x: coords[offset + ii2];
		    double y2 = (ii == -1)? y: coords[offset + ii2+1];
		    double v1x = x1-x0;
		    double v1y = y1-y0;
		    double v2x = x2-x0;
		    double v2y = y2-y0;
		    double cprod = v1x*v2y - v2x*v1y;
		    double ulpcp = Math.abs(v1x*Math.ulp(v2y))
			+ Math.abs(v2y*Math.ulp(v1x))
			+ Math.abs(v2x*Math.ulp(v1y))
			+ Math.abs(v1y*Math.ulp(v2x));
		    ulpcp *= 16.0;
		    if (cprod < ulpcp) {
			topLower--;
		    } else {
			break;
		    }
		}
		lower[topLower++] = points[i];
	    }
	    for (int i = points.length-1; i >= 0; i--) {
		while (topUpper > 1) {
		    int ind1 = upper[topUpper-2];
		    int ind2 = upper[topUpper-1];
		    int ind12 = ind1*2;
		    int ind22 = ind2*2;
		    int ii = points[i];
		    int ii2 = ii*2;
		    double x0 = (ind1 == -1)? x: coords[offset + ind12];
		    double y0 = (ind1 == -1)? y: coords[offset + ind12+1];
		    double x1 = (ind2 == -1)? x: coords[offset + ind22];
		    double y1 = (ind2 == -1)? y: coords[offset + ind22+1];
		    double x2 = (ii == -1)? x: coords[offset + ii2];
		    double y2 = (ii == -1)? y: coords[offset + ii2+1];
		    double v1x = x1-x0;
		    double v1y = y1-y0;
		    double v2x = x2-x0;
		    double v2y = y2-y0;
		    double cprod = v1x*v2y - v2x*v1y;
		    double ulpcp = Math.abs(v1x*Math.ulp(v2y))
			+ Math.abs(v2y*Math.ulp(v1x))
			+ Math.abs(v2x*Math.ulp(v1y))
			+ Math.abs(v1y*Math.ulp(v2x));
		    ulpcp *= 16.0;
		    if (cprod < ulpcp) {
			topUpper--;
		    } else {
			break;
		    }
		}
		upper[topUpper++] = points[i];
	    }
	    Path2D result = new Path2D.Double();
	    boolean firstTime = true;
	    topLower--;
	    topUpper--;
	    for (int i = 0; i < topLower; i++) {
		int ind = lower[i];
		int ind2 = ind*2;
		double x3 = (ind == -1)? x: coords[offset + ind2];
		double y3 = (ind == -1)? y: coords[offset + ind2+1];
		if (firstTime) {
		    result.moveTo(x3, y3);
		    firstTime = false;
		} else {
		    result.lineTo(x3, y3);
		}
	    }
	    for (int i = 0; i < topUpper; i++) {
		int ind = upper[i];
		int ind2 = ind*2;
		double x3 = (ind == -1)? x: coords[offset + ind2];
		double y3 = (ind == -1)? y: coords[offset + ind2+1];
		if (firstTime) {
		    result.moveTo(x3, y3);
		    firstTime = false;
		} else {
		    result.lineTo(x3, y3);
		}
	    }
	    result.closePath();
	    return result;
	}

	//Gift-wrapping algorithm
	double sx = x;
	double sy = y;
	int pointOnHullInd = -1;
	for (int i = 0; i < n; i++) {
	    int ind = 2*i;
	    if (coords[offset + ind] < sx) {
		pointOnHullInd = i;
		sx = coords[offset + ind];
		sy = coords[offset + ind+1];
	    } else if (coords[offset + ind] == sx
		       && coords[offset + ind+1] < sy) {
		pointOnHullInd = i;
		sy = coords[offset + ind+1];
	    }
	}
	int startInd = pointOnHullInd;
	int endPointInd;
	int k = 0;
	Path2D path = new Path2D.Double();
	if (startInd == -1) {
	    path.moveTo(x, y);
	} else {
	    int ind2 = startInd*2;
	    path.moveTo(coords[offset + ind2], coords[offset + ind2+1]);
	}

	do {
	    if (pointOnHullInd != startInd) {
		if (pointOnHullInd == -1) {
		    path.lineTo(x, y);
		} else {
		    int ind2 = 2*pointOnHullInd;
		    path.lineTo(coords[offset + ind2],
				coords[offset + ind2+1]);
		}
	    }
	    endPointInd = -1;
	    double hx = (pointOnHullInd == -1)? x:
		coords[offset + 2*pointOnHullInd];
	    double hy = (pointOnHullInd == -1)? y:
		coords[offset + 2*pointOnHullInd + 1];
	    for (int j = -1; j < n; j++) {
		boolean test = (endPointInd == pointOnHullInd);
		if (test == false && (endPointInd != j)) {
		    double ex = (endPointInd == -1)? x:
			coords[offset + 2*endPointInd];
		    double ey = (endPointInd == -1)? y:
			coords[offset + 2*endPointInd+1];
		    double v1x = ex - hx;
		    double v1y = ey - hy;
		    double v2x = ((j == -1)? x: coords[offset + 2*j]) - hx;
		    double v2y = ((j == -1)? y: coords[offset + 2*j+1]) - hy;
		    double cprod = (j == pointOnHullInd)? 1.0:
			(v1x*v2y - v2x*v1y);
		    // allow for floating-point errors
		    double ulpcp = Math.abs(v1x*Math.ulp(v2y))
			+ Math.abs(v2y*Math.ulp(v1x))
			+ Math.abs(v2x*Math.ulp(v1y))
			+ Math.abs(v1y*Math.ulp(v2x));
		    ulpcp *= 16.0;
		    if (cprod < -ulpcp) {
			// want counterclockwise hull
			test = true;
		    } else if (Math.abs(cprod) <= ulpcp) {
			// the two vectors are parallel, so replace
			// if the new one is longer
			double distsq1 = v1x*v1x + v1y*v1y;
			double distsq2 = v2x*v2x + v2y*v2y;
			if (distsq1 < distsq2) {
			    test = true;
			}
		    }
		}
		if (test) {
		    endPointInd = j;
		}
	    }
	    k++;
	    pointOnHullInd = endPointInd;
	} while (endPointInd != startInd);
	path.closePath();
	return path;
    }

    /**
     * Determine if two line segments intersect.
     * The segments are assumed to have a finite length.
     * <P>
     * For Java-7 (openjdk) and some other versions, there is a bug in 
     * {@link java.awt.geom.Line2D#linesIntersect(double,double,double,double,double,double,double,double)}
     * where two nearly parallel but well separated line segments will
     * appear to intersect. This method computes the same value, but
     * using a different algorithm, and with better numerical stability.
     * @param x1 the X coordinate for the start of the first line segment
     * @param y1 the Y coordinate for the start of the first line segment
     * @param x2 the X coordinate for the end of the first line segment
     * @param y2 the Y coordinate for the end of the first line segment
     * @param x3 the X coordinate for the start of the second line segment
     * @param y3 the Y coordinate for the start of the second line segment
     * @param x4 the X coordinate for the end of the second line segment
     * @param y4 the Y coordinate for the end of the second line segment
     * @return true if the line segments intersect; false if they do not
     *         intersect
     */
    public static boolean lineSegmentsIntersect(double x1, double y1,
						double x2, double y2,
						double x3, double y3,
						double x4, double y4)
    {
	double A11 = x2 - x1;
	double A12 = x3 - x4;
	double A21 = y2 - y1;
	double A22 = y3 - y4;

	double limit;
	if (x2 == x1 && y2 == y1) {
	    limit = 0.0;
	} else {
	    double tmp1 = Math.max(Math.abs(x2), Math.abs(x1));
	    double tmp2 = Math.max(Math.abs(y1), Math.abs(y2));
	    double tmp = Math.max(tmp1, tmp2);
	    limit = Math.max(Math.abs(A11), Math.abs(A21)) / tmp;
	}
	
	double p1 = A11*A22;
	double p2 = A12*A21;
	double det = (p1 - p2);
	double pm = Math.max(Math.abs(p1), Math.abs(p2));
	if (limit < 1.e-10 || pm == 0.0 || Math.abs(det) / pm < 1.e-7) {

	    // nearly parallel line segments or a near zero-length segment
	    double v1x = A11;
	    double v1y = A21;
	    double v3x = x3 - x1;
	    double v3y = y3 - y1;
	    double v4x = x4 - x1;
	    double v4y = y4 - y1;
	    
	    double norm = Math.sqrt(v3x*v3x+v3y*v3y);
	    double nv3x = (norm == 0.0)? 0.0: v3x/norm;
	    double nv3y = (norm == 0.0)? 0.0: v3y/norm;
	    if (norm == 0.0) return true;

	    norm = Math.sqrt(v4x*v4x + v4y*v4y);
	    double nv4x = (norm == 0.0)? 0.0: v4x/norm;
	    double nv4y = (norm == 0.0)? 0.0: v4y/norm;
	    if (norm == 0.0) return true;

	    norm = Math.sqrt(v1x*v1x+v1y*v1y);
	    if (norm == 0.0) {
		return ((v3x == 0.0 && v3y == 0.0)
			|| (v4x == 0.0 && v4y == 0.0));
	    }
	    double nv1x = v1x/norm;
	    double nv1y = v1y/norm;

	    // We have already determined that the lines are
	    // parallel or nearly parallel.
	    // If these cross products are non zero, then the lines
	    // are parallel but with some perpendicular spacing, in
	    // which case the lines do not intersect.
	    double cp = nv1x*nv3y - nv3x*nv1y;
	    if (Math.abs(cp) > 1.e-10) return false;

	    cp = nv1x*nv4y - nv4x*nv1y;
	    if (Math.abs(cp) > 1.e-10) return false;

	    double dot1 = v3x*nv1x + v3y*nv1y;
	    double dot2 = v4x*nv1x  +v4y*nv1y;
	    if (Math.signum(dot1) != Math.signum(dot2)) {
		return true;
	    }
	    if ((dot1 >= 0.0 && dot1 <= norm)
		|| (dot2 >= 0.0 && dot2 <= norm)) {
		return true;
	    }
	    return false;
	} else {
	    double delta1 = x3 - x1;
	    double delta2 = y3 - y1;
	    double u = (delta1*A22 - delta2*A12) / det;
	    double v = (A11*delta2 - A21*delta1) / det;
	    return (u >= 0.0 && u <= 1.0 && v >= 0.0 && v <= 1.0);
	}
    }

    /**
     * Get the intersection of two lines.
     * The first line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>1</sub>(1-u) + x<sub>2</sub>u
     * y = y<sub>1</sub>(1-u) + y<sub>2</sub>u
     * </PRE></BLOCKQUOTE>
     * and the second line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>3</sub>(1-u) + x<sub>4</sub>u
     * y = y<sub>3</sub>(1-u) + y<sub>4</sub>u
     * </PRE></BLOCKQUOTE>
     * If the lines to not intersect, the value false is returned and
     * the XY coordinates of the intersection point are set to
     * {@link java.lang.Double#NaN}. The lines are considered to be of
     * infinite length.
     * @param x1 the X coordinate for the start of the first line segment
     * @param y1 the Y coordinate for the start of the first line segment
     * @param x2 the X coordinate for the end of the first line segment
     * @param y2 the Y coordinate for the end of the first line segment
     * @param x3 the X coordinate for the start of the second line segment
     * @param y3 the Y coordinate for the start of the second line segment
     * @param x4 the X coordinate for the end of the second line segment
     * @param y4 the Y coordinate for the end of the second line segment
     * @param xy an array that will contain the X value of the intersection
     *        point, at an offset into the array, followed by the Y value
     *        of the intersection point
     * @param offset the offset into the array xy so that xy[offset] contains
     *        the X component of the intersection point and xy[offset+1]
     *        contains the Y component of the intersection point
     * @return true if the the lines intersect at a single point; false
     *         otherwise
     */
    public static boolean getLineIntersectionXY(double x1, double y1,
						double x2, double y2,
						double x3, double y3,
						double x4, double y4,
						double[] xy,
						int offset)
    {
	double x1mx2 = x1 - x2;
	double y1my2 = y1 - y2;
	double x4mx3 = x4 - x3;
	double y4my3 = y4 - y3;

	double det = x1mx2*y4my3 - y1my2*x4mx3;
	if (det == 0.0) {
	    xy[offset++] = Double.NaN;
	    xy[offset] = Double.NaN;
	    return false;
	}
	double u  = ((x1-x3)*y4my3 - (y1-y3)*x4mx3)/det;
	double oneMu = 1.0 - u;
	xy[offset++] = x1*oneMu + x2*u;
	xy[offset] = y1*oneMu + y2*u;
	return true;
    }

    /**
     * Get the path parameters for the intersection of two lines.
     * The first line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>1</sub>(1-u) + x<sub>2</sub>u
     * y = y<sub>1</sub>(1-u) + y<sub>2</sub>u
     * </PRE></BLOCKQUOTE>
     * and the second line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>3</sub>(1-v) + x<sub>4</sub>v
     * y = y<sub>3</sub>(1-v) + y<sub>4</sub>v
     * </PRE></BLOCKQUOTE>
     * If the lines to not intersect, the value false is returned and
     * the XY coordinates of the intersection point are set to
     * {@link java.lang.Double#NaN}. The lines are considered to be of
     * infinite length.  If a path parameter is between 0 and 1, the
     * intersection point lies along the path segment used to define the
     * line.
     * @param x1 the X coordinate for the start of the first line segment
     * @param y1 the Y coordinate for the start of the first line segment
     * @param x2 the X coordinate for the end of the first line segment
     * @param y2 the Y coordinate for the end of the first line segment
     * @param x3 the X coordinate for the start of the second line segment
     * @param y3 the Y coordinate for the start of the second line segment
     * @param x4 the X coordinate for the end of the second line segment
     * @param y4 the Y coordinate for the end of the second line segment
     * @param uv an array containing the path parameters for the two paths
     * @param offset the offset into the array uv so that uv[offset] contains
     *        the path parameter u for the first line at the intersection
     *        point and so that uv[offset+1] contains the path parameter v
     *        for the second line at the intersection  point
     *        contains the Y component of the intersection point
     * @return true if the the lines intersect at a single point; false
     *         otherwise
     */
    public static boolean getLineIntersectionUV(double x1, double y1,
						double x2, double y2,
						double x3, double y3,
						double x4, double y4,
						double[] uv,
						int offset)
    {
	double x1mx2 = x1 - x2;
	double y1my2 = y1 - y2;
	double x4mx3 = x4 - x3;
	double y4my3 = y4 - y3;

	double det = x1mx2*y4my3 - y1my2*x4mx3;
	if (det == 0.0) {
	    uv[offset] = Double.NaN;
	    uv[offset++] = Double.NaN;
	    return false;
	}
	uv[offset++]  = ((x1-x3)*y4my3 - (y1-y3)*x4mx3) / det;
	uv[offset] = (x1mx2*(y1-y3) - (y1my2)*(x1-x3)) / det;
	return true;
    }
    /**
     * Get the path parameters and the intersection point for the
     * intersection of two lines.
     * The first line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>1</sub>(1-u) + x<sub>2</sub>u
     * y = y<sub>1</sub>(1-u) + y<sub>2</sub>u
     * </PRE></BLOCKQUOTE>
     * and the second line is represented by the parameterized curve
     * <BLOCKQUOTE><PRE>
     * x = x<sub>3</sub>(1-u) + x<sub>4</sub>u
     * y = y<sub>3</sub>(1-u) + y<sub>4</sub>u
     * </PRE></BLOCKQUOTE>
     * If the lines to not intersect, the value false is returned and
     * the u-v parameters and the XY coordinates of the intersection
     * point are set to {@link java.lang.Double#NaN}. The lines are
     * considered to be of infinite length.  If a path parameter is
     * between 0 and 1, the intersection point lies along the path
     * segment used to define the line.
     * @param x1 the X coordinate for the start of the first line segment
     * @param y1 the Y coordinate for the start of the first line segment
     * @param x2 the X coordinate for the end of the first line segment
     * @param y2 the Y coordinate for the end of the first line segment
     * @param x3 the X coordinate for the start of the second line segment
     * @param y3 the Y coordinate for the start of the second line segment
     * @param x4 the X coordinate for the end of the second line segment
     * @param y4 the Y coordinate for the end of the second line segment
     * @param uvxy an array containing the path parameters for the two paths
     * @param offset the offset into the array uvxy so that uvxy[offset]
     *        contains the path parameter u for the first line at the
     *        intersection point, uvxy[offset+1] contains the path parameter v
     *        for the second line at the intersection point,
     *        uvxy[offset+2] contains the x coordinate of the intersection
     *        point, and uvxy[offset+3] contains the Y component of the
     *        intersection point
     * @return true if the the lines intersect at a single point; false
     *         otherwise
     */
    public static boolean getLineIntersectionUVXY(double x1, double y1,
						double x2, double y2,
						double x3, double y3,
						double x4, double y4,
						double[] uvxy,
						int offset)
    {
	double x1mx2 = x1 - x2;
	double y1my2 = y1 - y2;
	double x4mx3 = x4 - x3;
	double y4my3 = y4 - y3;

	double det = x1mx2*y4my3 - y1my2*x4mx3;
	if (det == 0.0) {
	    uvxy[offset++] = Double.NaN;
	    uvxy[offset++] = Double.NaN;
	    uvxy[offset++] = Double.NaN;
	    uvxy[offset] = Double.NaN;
	    return false;
	}
	double u  = ((x1-x3)*y4my3 - (y1-y3)*x4mx3)/det;
	double oneMu = 1.0 - u;
	uvxy[offset++]  = u;
	uvxy[offset++] =(x1mx2*(y1-y3) - (y1my2)*(x1-x3)) / det;
	uvxy[offset++] = x1*oneMu + x2*u;
	uvxy[offset] = y1*oneMu + y2*u;
	return true;
    }

    /**
     * Find the parameter for a B&eacute;zier curve for the point on the
     * curve closest to a specified point.
     * <P>
     * The calling convention matches the standard use of the interface
     * {@link java.awt.geom.PathIterator}. As currently defined, the
     * constants {@link PathIterator#SEG_LINETO},
     * {@link PathIterator#SEG_QUADTO}, and {@link PathIterator#SEG_CUBICTO}
     * have integer values equal to the degree of the corresponding B&eacute;
     * curve, although is is safer to not depend on this being true in
     * future versions of Java.
     * @param p the specified point
     * @param x0 the initial control point's X coordinate
     * @param y0 the initial control point's Y coordinate
     * @param coords the coordinates for the remaining control points,
     *        listed in order as pairs of X-Y coordinates with X coordinates
     *        preceding Y coordinates
     * @param degree the degree of the B&eacute;zier curve (this is one half
     *        the minimum length for the coords array).
     */
    public static double getMinDistBezierParm(Point2D p, double x0, double y0,
					      double[] coords, int degree)
    {
	// System.out.println("p = " + p + ", x0 = " + x0 + ", y0 = " + y0);
	double[] bcoords = new double[degree+1];
	bcoords[0] = x0;
	for (int i = 1; i < degree+1; i++) {
	    int im1 = i - 1;
	    bcoords[i] = coords[2*im1];
	}
	BezierPolynomial bx = new BezierPolynomial(bcoords, degree);
	for (int i = 0; i < degree+1; i++) {
	    bcoords[i] -= p.getX();
	}
	BezierPolynomial bxmp = new BezierPolynomial(bcoords, degree);

	bcoords[0] = y0;
	for (int i = 1; i < degree+1; i++) {
	    int im1 = i - 1;
	    bcoords[i] = coords[2*im1+1];
	}
	BezierPolynomial by = new BezierPolynomial(bcoords, degree);

	for (int i = 0; i < degree+1; i++) {
	    bcoords[i] -= p.getY();
	}
	BezierPolynomial bymp = new BezierPolynomial(bcoords, degree);

	BezierPolynomial bxd = bx.deriv();
	BezierPolynomial byd = by.deriv();
	BezierPolynomial tmpx = bxmp.multiply(bxd);
	BezierPolynomial tmpy = bymp.multiply(byd);
	BezierPolynomial bp = tmpx.add(tmpy);

	double[] roots = new double[bp.getDegree()];
	/*
	System.out.println("bp.getDegree() = " + bp.getDegree());
	System.out.println("Array:");
	for (int i = 0; i < bp.getCoefficientsArray().length; i++) {
	    System.out.println("      " + bp.getCoefficientsArray()[i]);
	}
	*/
	int nroots = RootFinder.solveBezier(bp.getCoefficientsArray(),
					    0, bp.getDegree(), roots);
	// System.out.println("nroots = " + nroots);

	double tx = bxmp.valueAt(0.0);
	double ty = bymp.valueAt(0.0);
	// System.out.println("tx (u=0) = " + tx);
	// System.out.println("ty (u=0) = " + ty);
	double minValue = tx*tx + ty*ty;
	double umin = 0.0;
	tx = bxmp.valueAt(1.0);
	ty = bymp.valueAt(1.0);
	// System.out.println("tx (u=1) = " + tx);
	// System.out.println("ty (u=1) = " + ty);
	double tmin = tx*tx + ty*ty;
	if (tmin < minValue) {
	    minValue = tmin;
	    umin = 1.0;
	}
	//System.out.println("... umin = " + umin + ", minValue = " + minValue);
	for (int i = 0; i < nroots; i++) {
	    double u = roots[i];
	    tx = bxmp.valueAt(u);
	    ty = bymp.valueAt(u);
	    tmin = tx*tx + ty*ty;
	    if (tmin < minValue) {
		minValue = tmin;
		umin = u;
	    }
	    /*
	    System.out.println("... trying " + u + ", umin = " + umin
			       + ", minValue = " + minValue);
	    */
	}
	return umin;
    }

    private static class BBFunct implements RealValuedFunctOps {
	double x1, y1, x2, y2;
	double[] coords1, coords2;
	int degree1, degree2;

	double[] coords1x, coords1y, coords2x, coords2y;

	double u = -1.0;
	public double getU() {return u;}

	BBFunct(double x1, double y1, double[] coords1, int degree1,
		double x2, double y2, double[] coords2, int degree2)
	{
	    this.x1 = x1;
	    this.x2 = x2;
	    this.y1 = y1;
	    this.y2 = y2;
	    this.coords1 = coords1;
	    this.degree1 = degree1;
	    this.coords2 = coords2;
	    this.degree2 = degree2;

	    coords1x = new double[degree1+1];
	    coords1y = new double[degree1+1];
	    coords2x = new double[degree2+1];
	    coords2y = new double[degree2+1];

	    coords1x[0] = x1;
	    coords1y[0] = y1;
	    for (int i = 1; i < coords1x.length; i++) {
		coords1x[i] = coords1[2*(i-1)];
		coords1y[i] = coords1[2*(i-1) + 1];
	    }

	    coords2x[0] = x2;
	    coords2y[0] = y2;
	    for (int i = 1; i < coords2x.length; i++) {
		coords2x[i] = coords2[2*(i-1)];
		coords2y[i] = coords2[2*(i-1) + 1];
	    }
	}

	public double getXForU(double u) {
	    return Functions.Bernstein.sumB(coords1x, 0, degree1, u);
	}

	public double getYForU(double u) {
	    return Functions.Bernstein.sumB(coords1y, 0, degree1, u);
	}

	public double getXForV(double v) {
	    return Functions.Bernstein.sumB(coords2x, 0, degree2, v);
	}

	public double getYForV(double v) {
	    return Functions.Bernstein.sumB(coords2y, 0, degree2, v);
	}

	private double getdXForU(double u) {
	    return Functions.Bernstein.dsumBdx(coords1x, 0, degree1, u);
	}

	private double getdYForU(double u) {
	    return Functions.Bernstein.dsumBdx(coords1y, 0, degree1, u);
	}


	@Override
	public double valueAt(double v) {
	    Point2D pt = new Point2D.Double(getXForV(v), getYForV(v));
	    u = getMinDistBezierParm(pt, x1, y1, coords1, degree1);
	    double tangentx = getdXForU(u);
	    double tangenty = getdYForU(u);
	    double norm = Math.sqrt(tangentx*tangentx + tangenty*tangenty);
	    double dist = pt.distance(getXForU(u), getYForU(u));
	    if (norm != 0.0) {
		tangentx /= norm;
		tangenty /= norm;
		double vx = getXForV(v) - getXForU(u);
		double vy = getYForV(v) - getYForU(u);
		double cprod =tangentx *(getYForV(v) - getYForU(u))
		    - (tangenty)*(getXForV(v) - getXForU(u));
		if (cprod == 0.0) return dist;
		dist *= Math.signum(cprod);
		return dist;
	    } else {
		return dist;
	    }
	}
    }

    private static double[] getBezierBezierUV(double x1, double y1,
					      double[] coords1, int degree1,
					      double x2, double y2,
					      double[] coords2, int degree2)
    {
	double cmax = Math.abs(x1);
	cmax +=  Math.abs(y1);
	cmax += Math.abs(x2);
	cmax += Math.abs(y2);
	for (int i = 0; i < degree1; i++) {
	    cmax += Math.abs(coords1[i]);
	}
	for (int i = 0; i < degree2; i++) {
	    cmax += Math.abs(coords2[i]);
	}
	cmax /= (4 + degree1 + degree2);

	BBFunct function = new BBFunct(x1, y1, coords1, degree1,
				       x2, y2, coords2, degree2);
	RootFinder.Brent brf = RootFinder.Brent.newInstance(function);
	double vmin = 0.0;
	double vmax = 1.0;
	double spacing = 1.0;
	double limit = 2;
	boolean done = false;
	// make intervals large enough that we can't possibly go past
	// the end of the array
	double intervals[] = new double[2*(degree1 + degree2) + 2 ];
	int ilim = 4*(degree1 + degree2) + 1;
	double lim = (double)(ilim);
	ilim++;
	double base = 0.0;
	double baseVal = function.valueAt(base);
	intervals[0] = 0.0;
	int icount = 1;

	for (int i = 1; i < ilim; i++) {
	    double v = i/lim;
	    double test = function.valueAt(v);
	    if (Math.signum(baseVal) != Math.signum(test)) {
		intervals[icount++] = v;
		base = v;
		baseVal = test;
	    }
	}
	if (Math.abs(intervals[icount-1] -1.0) < 1.e-10) {
	    // prevent roundoff errors
	    intervals[icount-1] = 1.0;
	}
	if (icount == 1) {
	    // could not bracket
	    return null;
	}

	double[] result = new double[2*(icount-1)];
	int rcnt = 0;
	for (int i = 1; i < icount; i++) {
	    try {
		vmin = intervals[i-1];
		vmax = intervals[i];
		double v = brf.solve(0.0, vmin, vmax);
		double val = function.valueAt(v);
		if (Math.abs(val) > (cmax / 1.e9)) {
		    // We can get a false root because, even though we
		    // don't go through zero, there can be a sign change
		    // (e.g., when curves do not intersect). This can fool
		    // Brent's method into acting as if a solution had been
		    // found.
		    continue;
		}
		result[rcnt++] = function.getU();
		result[rcnt++] = v;
	    } catch (RootFinder.ConvergenceException e) {
		continue;
	    }
	}
	if (result.length != rcnt) {
	    double[] newresult = new double[rcnt];
	    System.arraycopy(result, 0, newresult, 0, rcnt);
	    result = newresult;
	}
	return (rcnt == 0)? null: result;
    }

    private static double[] getLineBezierUV(double x1, double y1, double x2,
					    double y2,
					    double x3, double y3,
					    double[] coords, int degree)
    {
	ArrayList<Double> list = new ArrayList<>();
	double[] bcoords = new double[degree+1];
	if (x1 == x2) {
	    // "- x1" because Bernstein polynomials of degree n are a
	    // partition of unity.
	    bcoords[0] = x3 - x1;
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = coords[2*i] - x1;
	    }
	    double[] result = new double[degree];
	    int n = RootFinder.solveBezier(bcoords, 0, degree, result);
	    if (n == 0) return null;
	    bcoords[0] = y3;
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = coords[2*i+1];
	    }
	    for (int i = 0; i < n; i++) {
		double y =
		    Functions.Bernstein.sumB(bcoords, 0, degree, result[i]);
		double yerr =
		    Functions.Bernstein.sumBerr(bcoords, 0, degree, result[i]);
		if (y1 == y2) {
		    // zero-length line segment: (x1, y1) must be on the
		    // quadratic curve
		    if (Math.abs(y1 - y) <= yerr) {
			list.add(0.0);
			list.add(result[i]);
		    }
		} else {
		    if (Math.abs(y-y1) < yerr) {
			list.add(0.0);
		    } else if (Math.abs(y-y2) < yerr) {
			list.add(1.0);
		    } else {
			double u = (y - y1)/(y2-y1);
			if (u < 0.0 || u > 1.0) continue;
			list.add(u);
		    }
		    list.add(result[i]);
		}
	    }
	} else if (y1 == y2) {
	    // "- y1" because Bernstein polynomials of degree n are a
	    // partition of unity.
	    bcoords[0] = y3 - y1;
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = coords[2*i+1] - y1;
	    }
	    double[] result = new double[degree];
	    int n = RootFinder.solveBezier(bcoords, 0, degree, result);
	    if (n == 0) return null;
	    bcoords[0] = x3;
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = coords[2*i];
	    }
	    for (int i = 0; i < n; i++) {
		double x =
		    Functions.Bernstein.sumB(bcoords, 0, degree, result[i]);
		double xerr =
		    Functions.Bernstein.sumBerr(bcoords, 0, degree, result[i]);
		if (Math.abs(x-x1) < xerr) {
		    list.add(0.0);
		} else if (Math.abs(x-x2) < xerr) {
		    list.add(1.0);
		} else {
		    double u = (x - x1)/(x2-x1);
		    if (u < 0.0 || u > 1.0) continue;
		    list.add(u);
		}
		list.add(result[i]);
	    }
	} else {
	    double a = (y2 - y1) / (x2 - x1);
	    bcoords[0] = (y3 - y1) - a*(x3 - x1);
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = (coords[2*i+1]-y1) -a*(coords[2*i] - x1);
	    }
	    double[] result = new double[2];
	    int n = RootFinder.solveBezier(bcoords, 0, degree, result);
	    if (n == 0) return null;
	    bcoords[0] = x3;
	    for (int i = 0; i < degree; i++) {
		bcoords[i+1] = coords[2*i];
	    }
	    for (int i = 0; i < n; i++) {
		double x = Functions.Bernstein.sumB(bcoords, 0, degree,
						    result[i]);
		double xerr =
		    Functions.Bernstein.sumBerr(bcoords, 0, degree, result[i]);
		if (Math.abs(x-x1) < xerr) {
		    list.add(0.0);
		} else if (Math.abs(x-x2) < xerr) {
		    list.add(1.0);
		} else {
		    double u = (x - x1)/(x2-x1);
		    if (u < 0.0 || u > 1.0) continue;
		    list.add(u);
		}
		list.add(result[i]);
	    }
	}
	if (list.size()  == 0) return null;
	double[] rval = new double[list.size()];
	for (int i = 0; i < list.size(); i++) {
	    rval[i] = list.get(i);
	}
	return rval;
    }

    /**
     * Get the path parameters for intersection of two Path2D segments.
     * @param type1 the type of the first segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x1 the X coordinate of the first segment's initial control point
     * @param y1 the Y coordinate of the first segment's initial control point
     * @param coords1 the remaining control points for the first
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @param type2 the type of the second segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x2 the X coordinate of the second segment's initial control point
     * @param y2 the Y coordinate of the second segment's initial control point
     * @param coords2 the remaining control points for the second
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @return a array whose length is even and whose entries occur in
     *         pairs with an even entry containing the path parameter
     *         for the first curve followed by the path parameter for
     *         the second curve corresponding to an intersection.
     */
    public static double[] getSegmentIntersectionUV(int type1,
						    double x1, double y1,
						    double[] coords1,
						    int type2,
						    double x2, double y2,
						    double[] coords2)
    {
	switch(type1) {
	case PathIterator.SEG_LINETO:
	    switch(type2) {
	    case PathIterator.SEG_LINETO:
		double[] uv = new double[2];
		if (getLineIntersectionUV(x1, y1, coords1[0], coords1[1],
					  x2, y2, coords2[0], coords2[1],
					  uv, 0)) {
		    if (uv[0] < 0.0 || uv[0] > 1.0
			|| uv[1] < 0.0 || uv[1] > 1.0) {
			return null;
		    }
		    return uv;
		} else {
		    return null;
		}
	    case PathIterator.SEG_QUADTO:
		return getLineBezierUV(x1, y1, coords1[0], coords1[1],
				       x2, y2, coords2, 2);
	    case PathIterator.SEG_CUBICTO:
		return getLineBezierUV(x1, y1, coords1[0], coords1[1],
				       x2, y2, coords2, 3);
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	case PathIterator.SEG_QUADTO:
	    switch(type2) {
	    case PathIterator.SEG_LINETO:
		{
		    double[] res = getLineBezierUV(x2, y2,
						   coords2[0], coords2[1],
						   x1, y1, coords1, 2);
		    if (res != null) {
			for (int i = 0; i < res.length; i += 2) {
			    double tmp = res[i];
			    res[i] = res[i+1];
			    res[i+1] = tmp;
			}
		    }
		    return res;
		}
	    case PathIterator.SEG_QUADTO:
		return getBezierBezierUV(x1, y1, coords1, 2,
					 x2, y2, coords2, 2);
	    case PathIterator.SEG_CUBICTO:
		return getBezierBezierUV(x1, y1, coords1, 2,
					 x2, y2, coords2, 3);
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	case PathIterator.SEG_CUBICTO:
	    switch(type2) {
	    case PathIterator.SEG_LINETO:
		{
		    double[] res = getLineBezierUV(x2, y2,
						   coords2[0], coords2[1],
						   x1, y1, coords1, 3);
		    if (res != null) {
			for (int i = 0; i < res.length; i += 2) {
			    double tmp = res[i];
			    res[i] = res[i+1];
			    res[i+1] = tmp;
			}
		    }
		    return res;
		}
	    case PathIterator.SEG_QUADTO:
		return getBezierBezierUV(x1, y1, coords1, 3,
					 x2, y2, coords2, 2);
	    case PathIterator.SEG_CUBICTO:
		return getBezierBezierUV(x1, y1, coords1, 3,
					 x2, y2, coords2, 3);
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Get the intersection of two Path2D segments.
     * @param type1 the type of the first segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x1 the X coordinate of the first segment's initial control point
     * @param y1 the Y coordinate of the first segment's initial control point
     * @param coords1 the remaining control points for the first
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @param type2 the type of the second segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x2 the X coordinate of the second segment's initial control point
     * @param y2 the Y coordinate of the second segment's initial control point
     * @param coords2 the remaining control points for the second
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @return a array whose length is even and whose entries occur in
     *         pairs with an even entry containing the X coordinate and
     *         the next entry containing the Y coordinate for an intersection
     *         of the two curves
     */
    public static double[] getSegmentIntersectionXY(int type1,
						    double x1, double y1,
						    double[] coords1,
						    int type2,
						    double x2, double y2,
						    double[] coords2)
    {
	double[] result = getSegmentIntersectionUV(type1, x1, y1, coords1,
						   type2, x2, y2, coords2);
	if (result != null) {
	    for (int i = 0; i < result.length; i+= 2) {
		result[i+1] = getY(result[i], x1, y1, type1, coords1);
		result[i] = getX(result[i], x1, y1, type1, coords1);
	    }
	}
	return result;
    }

    /**
     * Get the intersection of two Path2D segments, providing both path
     *  parameters and XY coordinates.
     * @param type1 the type of the first segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x1 the X coordinate of the first segment's initial control point
     * @param y1 the Y coordinate of the first segment's initial control point
     * @param coords1 the remaining control points for the first
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @param type2 the type of the second segment
     *        (PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, or
     *        PathIterator.SEG_CUBICTO)
     * @param x2 the X coordinate of the second segment's initial control point
     * @param y2 the Y coordinate of the second segment's initial control point
     * @param coords2 the remaining control points for the second
     *        segment, listed in order with the X and Y coordinates
     *        adjacent and in that order (the same convention used by
     *        {@link PathIterator#currentSegment(double[])})
     * @return a array whose length is even and whose entries occur in
     *         quartets in the following order:
     *         <UL>
     *            <LI> the path parameter for the first curve at an
     *                 intersection
     *            <LI> the path parameter for the second curve at an
     *                 intersection
     *            <LI> the X coordinate for the intersection
     *            <LI> the Y coordinate for the intersection
     *         </UL>
     *         with this sequence repeated for as many intersections
     *         as was found; null if there are no intersections.
     */
    public static double[] getSegmentIntersectionUVXY(int type1,
						      double x1, double y1,
						      double[] coords1,
						      int type2,
						      double x2, double y2,
						      double[] coords2)
    {
	double[] iresult = getSegmentIntersectionUV(type1, x1, y1, coords1,
						    type2, x2, y2, coords2);
	double[] result = null;
	if (iresult != null) {
	    result = new double[iresult.length * 2];
	    for (int i = 0; i < iresult.length; i+= 2) {
		int i2 = i*2;
		result[i2] = iresult[i];
		result[i2+1] = iresult[i+1];
		result[i2+2] = getX(iresult[i], x1, y1, type1, coords1);
		result[i2+3] = getY(iresult[i], x1, y1, type1, coords1);
	    }
	}
	return result;
    }

    /**
     * Class to represent a value of the path parameter u.
     * This class caches the parameter u and some related values
     * for use with methods defined for the {@link SegmentData} class.
     * This is useful when the same parameter will be used multiple times.
     * @see SegmentData
     */
    public static final class UValues {
	double u;
	double u1;
	double uu;
	double u1u;
	double u1u1;
	double uuu;
	double u1uu;
	double u1u1u;
	double u1u1u1;

	/**
	 * Get the path parameter used to create this object.
	 * @return the path parameter
	 */
	public double getU() {return u;}

	/**
	 * Constructor.
	 * @param u the value of the path parameter in the range [0.0, 1.0]
	 * @exception IllegalArgumentException the argument was out of range
	 */
	public UValues(double u) {
	    if (u < 0.0 || u > 1.0) {
		throw new
		    IllegalArgumentException(errorMsg("argOutOfRange", u));
	    }
	    this.u = u;
	    u1 = 1.0 - u;
	    uu = u*u;
	    u1u = u*u1;
	    u1u1 = u1*u1;
	    uuu = uu*u;
	    u1uu = u1*uu;
	    u1u1u = u1u1*u;
	    u1u1u1 = u1u1*u1;
	}

	/**
	 * Constructor given the segment type.
	 * When a segment type is provided, this object may be used only
	 * with a SegmentData object with the same segment type.
	 * @param u the value of the path parameter in the range [0.0, 1.0]
	 * @param st the path-segment type, legal values of which
	 *    are
	 *    <ul>
	 *      <li> {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
	 *      <li> {@link PathIterator#SEG_LINETO PathIterator.SEG_LINETO}
	 *      <li> {@link PathIterator#SEG_QUADTO PathIterator.SEG_QUADTO}
	 *      <li> {@link PathIterator#SEG_CUBICTO PathIterator.SEG_CUBICTO}
	 *      <li> {@link PathIterator#SEG_CLOSE PathIterator.SEG_CLOSE}
	 *    </ul>
	 * @exception IllegalArgumentException an argument was out of range
	 */
	public UValues(double u, int st) {
	    if (u < 0.0 || u > 1.0)
		throw new
		    IllegalArgumentException
		    (errorMsg("argOutOfRange2i", u, st));
	    this.u = u;
	    switch(st) {
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
	    case PathIterator.SEG_CLOSE:
		u1 = 1.0 - u;
		break;
	    case PathIterator.SEG_QUADTO:
		u1 = 1.0 - u;
		uu = u*u;
		u1u = u*u1;
		u1u1 = u1*u1;
		break;
	    case PathIterator.SEG_CUBICTO:
		u1 = 1.0 - u;
		uu = u*u;
		u1u = u*u1;
		u1u1 = u1*u1;
		uuu = uu*u;
		u1uu = u1*uu;
		u1u1u = u1u1*u;
		u1u1u1 = u1u1*u1;
		break;
	    default:
		throw new
		    IllegalArgumentException
		    (errorMsg("argOutOfRange2i", u, st));
	    }
	}

    }

    /**
     * Class to store values used in computations on a segment of
     * a path.
     */
    public static final class SegmentData {
	int st;
	// only the values needed for specific values of st are
	// explicitly initialized.
	double x0;
	double y0;
	double c0;
	double c1;
	double c2;
	double c3;
	double c4;
	double c5;
	double c0x0;
	double c2c0;
	double c4c2;
	double c1y0;
	double c3c1;
	double c5c3;
	SegmentData last = null;

	/**
	 * Constructor.
	 * @param st the path-segment type, legal values of which
	 *    are
	 *    <ul>
	 *      <li> {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
	 *      <li> {@link PathIterator#SEG_LINETO PathIterator.SEG_LINETO}
	 *      <li> {@link PathIterator#SEG_QUADTO PathIterator.SEG_QUADTO}
	 *      <li> {@link PathIterator#SEG_CUBICTO PathIterator.SEG_CUBICTO}
	 *      <li> {@link PathIterator#SEG_CLOSE PathIterator.SEG_CLOSE}
	 *    </ul>
	 * @param x0 the initial x coordinate for the segment (ignored for
	 *        the {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
	 *        case)
	 * @param y0 the initial y coordinate for the segment (ignored for
	 *        the {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
	 *        case)
	 * @param coords the coordinates array set by a call to
	 *        {@link PathIterator#currentSegment(double[])}, but with the
	 *        first three elements set to the x and coordinates
	 *        respectively for the previous
	 *        {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
	 *        segment when first argument's value is
	 *        {@link PathIterator#SEG_CLOSE}. The size of this array
	 *        should be at least 2 and the maximum size needed is 6.
	 * @param last the previous SegmentData for a path; null if there is
	 *        none
	 * @see PathIterator
	 * @exception IllegalArgumentException a value was out of range
	 * @exception ArrayIndexOutOfBoundsException the array argument was
	 *            two small
	 * @exception NullPointerException the array argument was null
	 */
	public SegmentData(int st, double x0, double y0, double[] coords,
			   SegmentData last)
	    throws IllegalArgumentException
	{
	    this.st = st;
	    c0 = coords[0];
	    c1 = coords[1];
	    this.last = last;

	    if (st == PathIterator.SEG_MOVETO) return;

	    this.x0 = x0;
	    this.y0 = y0;

	    c2 = coords[2];
	    c3 = coords[3];
	    c0x0 = c0 - x0;
	    c1y0 = c1 - y0;
	    if (st == PathIterator.SEG_LINETO || st == PathIterator.SEG_CLOSE) {
		return;
	    }
	    c2c0 = c2 - c0;
	    c3c1 = c3 - c1;
	    if (st == PathIterator.SEG_CUBICTO) {
		c4 = coords[4];
		c5 = coords[5];
		c4c2 = c4 - c2;
		c5c3 = c5 - c3;
	    }
	}

	/**
	 * Compute the X coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the X coordinate
	 */
	public double getX(UValues uv) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
		return c0;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return c0*uv.u + x0*uv.u1;
	    case PathIterator.SEG_QUADTO:
		return uv.u1u1*x0 + 2.0 * uv.u1u*c0 + uv.uu*c2;
	    case PathIterator.SEG_CUBICTO:
		return uv.u1u1u1*x0 + 3.0 *(uv.u1u1u*c0 + uv.u1uu*c2)
		    + uv.uuu*c4;
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the X coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the Y coordinate
	 */
	public double getY(UValues uv) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
		return c1;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return c1*uv.u + y0*uv.u1;
	    case PathIterator.SEG_QUADTO:
		return uv.u1u1*y0 + 2.0 * uv.u1u*c1 + uv.uu*c3;
	    case PathIterator.SEG_CUBICTO:
		return uv.u1u1u1*y0 + 3.0 * (uv.u1u1u*c1 + uv.u1uu*c3)
		    + uv.uuu*c5;
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 *  X coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the derivative of the X coordinate with respect to the
	 *         path parameter
	 */
	public double dxDu(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_MOVETO:
		return 0.0;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return c0x0;
	    case PathIterator.SEG_QUADTO:
		return 2.0 * (uv.u1*c0x0 + uv.u*c2c0);
	    case PathIterator.SEG_CUBICTO:
		return 3.0 * (uv.u1u1*c0x0 + 2.0*uv.u1u*c2c0 + uv.uu*c4c2);
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 * Y coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the derivative of the Y coordinate with respect to the
	 *         path parameter
	 */
	public double dyDu(UValues uv) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
		return 0.0;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return c1y0;
	    case PathIterator.SEG_QUADTO:
		return 2.0 * (uv.u1*c1y0 + uv.u*c3c1);
	    case PathIterator.SEG_CUBICTO:
		return 3.0 * (uv.u1u1*c1y0 + 2.0*uv.u1u*c3c1 + uv.uu*c5c3);
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the X coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the second derivative of the X coordinate with respect to the
	 *         path parameter
	 */
	public double d2xDu2(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_MOVETO:
		return 0.0;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return 0.0;
	    case PathIterator.SEG_QUADTO:
		return 2.0 * (c2c0 - c0x0);
	    case PathIterator.SEG_CUBICTO:
		return 6.0 * (uv.u1 *(c2c0 - c0x0) + uv.u *(c4c2 - c2c0));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the third derivative with respect to the path parameter
	 * of the X coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the third derivative of the X coordinate with respect to the
	 *         path parameter
	 */
	public double d3xDu3(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_CUBICTO:
		return 6*(c4 - 3*c2 + 3*c0 - x0);
	    default:
		return 0.0;
	    }
	}

	/**
	 * Compute the third derivative with respect to the path parameter
	 * of the Y coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the third derivative of the X coordinate with respect to the
	 *         path parameter
	 */
	public double d3yDu3(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_CUBICTO:
		return 6*(c5 - 3*c3 + 3*c1 - y0);
	    default:
		return 0.0;
	    }
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the Y coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the second derivative of the Y coordinate with respect to the
	 *         path parameter
	 */
	public double d2yDu2(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_MOVETO:
		return 0.0;
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_LINETO:
		return 0.0;
	    case PathIterator.SEG_QUADTO:
		return 2.0 *(c3c1 - c1y0);
	    case PathIterator.SEG_CUBICTO:
		return 6.0 * (uv.u1*(c3c1 - c1y0) + uv.u *(c5c3 - c3c1));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path2DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the derivative with respect to the path parameter
	 * of the signed distance along the path from its initial point
	 * @param uv the path parameter
	 * @return the derivative of the signed distance along the path
	 *         with respect to the path parameter
	 */
	public double dsDu(UValues uv) {
	    if (st == PathIterator.SEG_MOVETO) return 0.0;
	    double dxdu = dxDu(uv);
	    double dydu = dyDu(uv);
	    return Math.sqrt(dxdu*dxdu + dydu*dydu);
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the signed distance along the path from its initial point
	 * @param uv the path parameter
	 * @return the second derivative of the signed distance along the path
	 *         with respect to the path parameter
	 */
	public double d2sDu2(UValues uv) {
	    if (st == PathIterator.SEG_MOVETO
		|| st == PathIterator.SEG_LINETO
		|| st == PathIterator.SEG_CLOSE) {
		return 0.0;
	    }
	    double dxdu = dxDu(uv);
	    double dydu = dyDu(uv);
	    double d2xdu2 = d2xDu2(uv);
	    double d2ydu2 = d2yDu2(uv);
	    double dsduSquared = dxdu*dxdu + dydu*dydu;
	    if (dsduSquared == 0.0) {
		return java.lang.Double.NaN;
	    }
	    return (dxdu*d2xdu2 + dydu*d2ydu2) / Math.sqrt(dsduSquared);
	}

	private static final UValues uvOne = new UValues(1.0);

	/**
	 * Compute the signed curvature at a point with a given path
	 * parameter If the path segment's type is
	 * {@link PathIterator#SEG_CLOSE}, the starting and ending points
	 * of the segment are identical, and the path parameter corresponding
	 * to the argument is zero, the curvature at the end of the previous
	 * segment is returned.  A positive curvature
	 * corresponds to a rotation from the positive X axis towards
	 * the positive Y axis for an angular distance of 90 degrees.
	 * Similarly a negative curvature corresponds to a rotation
	 * from the positive Y axis towards the positive X axis for an
	 * angular distance of 90 degrees.
	 * @param uv the path parameter
	 * @return the curvature (Double.NaN if not defined) with
	 *         positive values indicating that the tangent vector
	 *         rotates counterclockwise as the path parameter increases
	 */
	public double curvature(UValues uv) {
	    if (st == PathIterator.SEG_MOVETO) {
		return Double.NaN;
	    } else if (st == PathIterator.SEG_LINETO) {
		return 0.0;
	    } else if (st == PathIterator.SEG_CLOSE) {
		if (x0 == c0 && y0 == c1) {
		    if (uv.u > 0.0) return Double.NaN;
		    if (last == null) return Double.NaN;
		    return last.curvature(uvOne);
		} else {
		    return 0.0;
		}
	    }
	    double xp = dxDu(uv);
	    double yp = dyDu(uv);
	    double xpp = d2xDu2(uv);
	    double ypp = d2yDu2(uv);

	    double tmp = xp*xp + yp*yp;
	    tmp = tmp * Math.sqrt(tmp);
	    if (tmp == 0.0) {
		// As an example of when this can happen, consider
		// a curve in which y is constant and x is a quadratic
		// or cubic function of u such that x = 0 when u = 0 and x = 0
		// when u = 1.  At some intermediate point, xp will be
		// zero. The path, however is restricted to a straight line
		// that doubles back on itself. At the turning point, the
		// curvature could be positive infinity or negative infinity,
		// but which it is cannot be determined, so NaN is an
		// appropriate value.
		return Double.NaN;
	    }
	    return (xp*ypp - yp *xpp)/tmp;
	}

	/**
	 * Determine if the curvature exists for the specified argument.
	 * In general, the curvature does not exist when all the points
	 * along a segment are the same point or when the segment is
	 * a SEG_MOVETO segment (which just indicates the start of a curve).
	 * For a SEG_CLOSE segment where the starting and ending points
	 * are identical, the curvature exists if the curvature of the
	 * previous segment exists at its end (u = 1.0).
	 * @param uv the path parameter
	 * @return true if the curvature exists; false otherwise
	 */
	public boolean curvatureExists(UValues uv) {
	    switch(st) {
	    case PathIterator.SEG_MOVETO:
		return false;
	    case PathIterator.SEG_LINETO:
		if (x0 == c0 && y0 == c1) return false;
		break;
	    case PathIterator.SEG_CLOSE:
		if (x0 == c0 & y0 == c1) {
		    if (uv.u > 0.0) return false;
		    if (last == null) return false;
		    return last.curvatureExists(uvOne);
		}
		break;
	    case PathIterator.SEG_QUADTO:
		if (x0 == c0 && y0 == c1 && x0 == c2 && y0 == c3) {
		    return false;
		}
		break;
	    case  PathIterator.SEG_CUBICTO:
		if (x0 == c0 && y0 == c1 && x0 == c2 && y0 == c3
		    && x0 == c4 && y0 == c5) {
		    return false;
		}
		break;
	    }
	    return true;
	}

	/**
	 * Get the type for this segment
	 * @return the type of this segment, legal values of which
	 *         are  {@link PathIterator#SEG_MOVETO},
	 *        {@link PathIterator#SEG_LINETO},
	 *        {@link PathIterator#SEG_QUADTO},
	 *        {@link PathIterator#SEG_CUBICTO}, and
	 *        {@link PathIterator#SEG_CLOSE}.
	 */
	public int getType() {
	    return st;
	}

	/**
	 * Get the tangent vector for a specified value of the path
	 * parameter and an output-array offset.
	 * If the tangent vector does not exist (e.g., the length of
	 * the line does not vary with the path parameter), the
	 * tangent vector will be set to zero.  The tangent vector
	 * will have unit length if it is not zero.
	 * @param uv the path parameter
	 * @param array an array of length no less than 2 used to
	 *        store the tangent vector, with array[offset]
	 *        containing the tangent vector's X component and
	 *        array[offset+1] containing the tangent vector's Y
	 *        component
	 * @param offset the index into the array at which to store
	 *        the tangent vector
	 * @return true if the tangent vector exists; false if the tangent
	 *         vector does not exist
	 */
	public boolean getTangent(UValues uv, double[] array, int offset) {
	    if (st == PathIterator.SEG_CLOSE) {
		if (x0 == c0 && y0 == c1) {
		    if (uv.u == 0.0) {
			return last.getTangent(uv, array, offset);
		    } else {
			array[offset] = 0.0;
			array[offset+1] = 0.0;
			return false;
		    }
		}
	    }
	    double xp = dxDu(uv);
	    double yp = dyDu(uv);
	    double tmp = Math.sqrt(xp*xp + yp*yp);
	    if (tmp == 0.0) {
		switch(st) {
		case PathIterator.SEG_QUADTO:
		    if (c0 == x0 && c1 == y0) {
			xp = c2 - c0;
			yp = c3 - c1;
			tmp = Math.sqrt(xp*xp + yp*yp);
		    } else if (c0 == c2 && c1 == c3) {
		    xp = c0 - x0;
		    yp = c1 - y0;
		    tmp = Math.sqrt(xp*xp + yp*yp);
		}
		    break;
		case PathIterator.SEG_CUBICTO:
		    if (c0 == x0 && c1 == y0) {
			if (c0 == c2 && c1 == c3) {
			    xp = c4 - c2;
			    yp  = c5 - c3;
			    tmp = Math.sqrt(xp*xp + yp*yp);
			} else if (uv.getU() == 0.0) {
			    xp = c2 - c0;
			    yp = c3 - c1;
			    tmp = Math.sqrt(xp*xp + yp*yp);
			}
		    } else if (c2 == c4 && c3 == c5) {
			if (c0 == c2 && c1 == c3) {
			    xp = c0 - x0;
			    yp = c1 - y0;
			    tmp = Math.sqrt(xp*xp + yp*yp);
			} else if (uv.getU() == 1.0) {
			    xp = c2 - c0;
			    yp = c3 - c1;
			    tmp = Math.sqrt(xp*xp + yp*yp);
			}
		    }
		    break;
		}
	    }
	    if (tmp == 0.0) {
		array[offset] = 0.0;
		array[offset+1] = 0.0;
		return false;
	    }
	    array[offset] = xp/tmp;
	    // prevent -0.0
	    if (array[offset] == 0.0) array[offset] = 0.0;
	    offset++;
	    array[offset] = yp/tmp;
	    // prevent -0.0
	    if (array[offset] == 0.0) array[offset] = 0.0;
	    return true;
	}

	/**
	 * Get the normal vector for a given value of the path
	 * parameter and an offset for the array storing the normal
	 * vector.
	 * The normal vector N is a vector of unit length,
	 * perpendicular to the tangent vector, and oriented so that
	 * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is
	 * the (signed) curvature.  If the normal vector does not
	 * exist (e.g., the length of the line does not vary with the
	 * path parameter), the normal vector will be set to zero.
	 * <P>
	 * Note: the use of the signed curvature results in the normal
	 * vector always pointing in the counter-clockwise direction
	 * (i.e., a rotating the X axis towards the Y axis). This allows
	 * a normal vector exist for straight-line segments. A different
	 * definition is used for 3D paths.
	 * @param uv the path parameter
	 * @param array an array of length no less than 2 used to
	 *        store the normal vector, with array[offset]
	 *        containing the normal vector's X component and
	 *        array[offset+1] containing the normal vector's Y
	 *        component
	 * @param offset the index into the array at which to store
	 *        the normal vector
	 * @return true if the normal vector exists; false if the normal
	 *         vector does not exist
	 */
	public boolean getNormal(UValues uv, double[] array, int offset) {
	    boolean status = getTangent(uv, array, offset);
	    if (status == false) {
		return status;
	    }
	    double tmp = array[offset];
	    double tmp2 = -array[offset+1];
	    // prevent -0.0
	    if (tmp2 == 0.0) tmp2 = 0.0;
	    array[offset] = tmp2;
	    array[offset+1] = tmp;
	    return true;
	}

	/*
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	*/
	}
    /**
     * Compute the X coordinate of a point on the path given
     * path-segment parameters.
     * When the type is {@link PathIterator#SEG_MOVETO}, x0 and y0 are
     * ignored.  When the type is {@link PathIterator#SEG_CLOSE}, the
     * first two elements of coords must be set to the X and Y
     * coordinate respectively for the point provided by most recent
     * segment whose type is {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by 
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *         {@link PathIterator#SEG_LINETO},
     *         {@link PathIterator#SEG_QUADTO},{@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param coords the coordinates array as defined by {@link
     *         java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *         operation when the type is {@link PathIterator#SEG_CLOSE}
     * @return the value of the X coordinate
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double getX(double u, double x0, double y0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return coords[0];
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return coords[0]*u + x0*u1;
	case PathIterator.SEG_QUADTO:
	    return u1*u1*x0 + 2.0 *u1*u*coords[0] + u*u*coords[2];
	case PathIterator.SEG_CUBICTO:
	    double u1sq = u1*u1;
	    double usq = u*u;
	    return u1sq*u1*x0 +
		3.0 * (u1sq*u*coords[0] + u1*usq*coords[2])
		+ usq*u*coords[4];
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the Y coordinate of a point on a path given path-segment
     * parameters.
     * When the type is {@link PathIterator#SEG_MOVETO}, x0 and y0 are
     * ignored.  When the type is {@link PathIterator#SEG_CLOSE}, the
     * first two elements of coords must be set to the X and Y
     * coordinate respectively for the point provided by most recent
     * segment whose type is {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is {@link PathIterator#SEG_CLOSE}
     * @return the value of the Y coordinate
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double getY(double u, double x0, double y0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return coords[1];
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return coords[1]*u + y0*u1;
	case PathIterator.SEG_QUADTO:
	    return u1*u1*y0 + 2.0 *u1*u*coords[1] + u*u*coords[3];
	case PathIterator.SEG_CUBICTO:
	    double u1sq = u1*u1;
	    double usq = u*u;
	    return u1sq*u1*y0 +
		3.0 * (u1sq*u*coords[1] + u1*usq*coords[3])
		+ usq*u*coords[5];
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }


    /**
     * Compute dx/du given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param coords the coordinates array as defined by {@link
     *         java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last
     *         {@link PathIterator#SEG_MOVETO} operation when the type
     *         is {@link PathIterator#SEG_CLOSE}
     * @return the value of dx/ds
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dxDu(double u, double x0, double y0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	   return coords[0] - x0;
	case PathIterator.SEG_QUADTO:
	    return  2.0 * (u1*(coords[0]-x0) + u*(coords[2]-coords[0]));
	case PathIterator.SEG_CUBICTO:
	    return 3.0 * (u1*u1*(coords[0] - x0)
			  + 2*u*u1*(coords[2] - coords[0])
			  + u*u*(coords[4] - coords[2]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute dy/du given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is {@link PathIterator#SEG_CLOSE}
     * @return the value of dy/du
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dyDu(double u, double x0, double y0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return coords[1] - y0;
	case PathIterator.SEG_QUADTO:
	    return 2.0 * (u1*(coords[1]-y0) + u*(coords[3]-coords[1]));
	case PathIterator.SEG_CUBICTO:
	    return 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[3] - coords[1])
			  + u*u*(coords[5] - coords[3]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the derivative of the path length s with respect to the
     * path's parameter u given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *          operation when the type is
     *          {@link PathIterator#SEG_CLOSE}
     * @return the value of ds/du
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dsDu(double u, double x0, double y0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double dxdu;
	double dydu;
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    dxdu = coords[0] - x0;
	    dydu = coords[1] - y0;
	    break;
	case PathIterator.SEG_QUADTO:

	    dxdu = 2.0 * (u1*(coords[0]-x0) + u*(coords[2]-coords[0]));
	    dydu = 2.0 * (u1*(coords[1]-y0) + u*(coords[3]-coords[1]));
	    break;
	case PathIterator.SEG_CUBICTO:
	    dxdu = 3.0 * (u1*u1*(coords[0] - x0)
			  + 2*u*u1*(coords[2] - coords[0])
			  + u*u*(coords[4] - coords[2]));
	    dydu = 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[3] - coords[1])
			  + u*u*(coords[5] - coords[3]));
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}	
	double dsduSquared = dxdu*dxdu + dydu*dydu;
	return  Math.sqrt(dsduSquared);
    }

    /**
     * Compute the second derivative of x with respect to u
     *  given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *         operation when the type is {@link PathIterator#SEG_CLOSE}
     * @return the value of <sup>2</sup>x/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d2xDu2(double u, double x0, double y0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double d2xdu2;
	double d2ydu2;
	double u1;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return 0.0;
	case PathIterator.SEG_QUADTO:
	    u1 = 1.0 - u;
	    return 2.0 * (x0 - coords[0] + coords[2] - coords[0]);
	case PathIterator.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    // double u1u1deriv = 2.0 * (1 - u);
	    // double uuderiv = 2.0 * u;
	    // double uu1deriv = 1.0 - 2.0 * u;
	    return 6.0 * (u1*(x0 -2.0*coords[0] + coords[2])
			  + u*(coords[4] - 2.0*coords[2] + coords[0]));
	    /*
	    return 3.0 * (u1u1deriv*(coords[0] - x0)
			  + 2.0*uu1deriv*(coords[2] - coords[0])
			  + uuderiv*(coords[4] - coords[2]));
	    */
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the third derivative of x with respect to u
     *  given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *         operation when the type is {@link PathIterator#SEG_CLOSE}
     * @return the value of <sup>2</sup>x/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d3xDu3(double u, double x0, double y0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double d2xdu2;
	double d2ydu2;
	double u1;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	case PathIterator.SEG_QUADTO:
	    return 0.0;
	case PathIterator.SEG_CUBICTO:
	    return 6*(coords[4] - 3*coords[2] + 3*coords[0] - x0);
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the second derivative of y with respect to u
     *  given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *         operation when the type is
     *         {@link PathIterator#SEG_CLOSE}
     * @return the value of d<sup>2</sup>y/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d2yDu2(double u, double x0, double y0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return 0.0;
	case PathIterator.SEG_QUADTO:
	    return 2.0 * (y0 - coords[1] + coords[3] - coords[1]);
	case PathIterator.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    return 6.0 * (u1*(y0 -2.0*coords[1] + coords[3])
			  + u*(coords[5] - 2.0*coords[3] + coords[1]));
	    /*
	    double u1u1deriv = 2.0 * (1 - u);
	    double uuderiv = 2.0 * u;
	    double uu1deriv = 1.0 - 2.0 * u;
	    return 3.0 * (u1u1deriv*(coords[1] - y0)
			  + 2.0*uu1deriv*(coords[3] - coords[1])
			  + uuderiv*(coords[5] - coords[3]));
	    */
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the third derivative of y with respect to u
     *  given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last {@link PathIterator#SEG_MOVETO}
     *         operation when the type is
     *         {@link PathIterator#SEG_CLOSE}
     * @return the value of d<sup>3</sup>y/du<sup>3</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d3yDu3(double u, double x0, double y0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	case PathIterator.SEG_QUADTO:
	    return 0.0;
	case PathIterator.SEG_CUBICTO:
	    return 6*(coords[5] - 3*coords[3] + 3*coords[1] - y0);
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the second derivative of the path length s with respect to u
     * given path-segment parameters.
     * When the type is {@link PathIterator#SEG_CLOSE}, the first two
     * elements of the fifth argument (coords) must be set to the x
     * and y coordinate respectively for the point provided by most
     * recent segment whose type is {@link PathIterator#SEG_MOVETO}.
     * <P>
     * The returned value will be Double.NaN in cases where division by
     * zero would occur, such as when the initial point for a segment and
     * the control points and final point given by the array argument are
     * identical. The most likely case is when the type is
     * {@link PathIterator#SEG_CLOSE PathIterator.SEG_CLOSE}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by 
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param coords the coordinates array as defined by {@link
     *        java.awt.geom.PathIterator PathIterator}, but with
     *        coords[0] and coords[1] set to the X and Y coordinates
     *        respectively for the last
     *        {@link PathIterator#SEG_MOVETO} operation when the type is
     *        {@link PathIterator#SEG_CLOSE}
     * @return the value of d<sup>2</sup>s/du<sup>2</sup>; Double.NaN if
     *         not defined but zero if the type is
     *         {@link PathIterator#SEG_MOVETO PathIterator.SEG_MOVETO}
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     * @see PathIterator
     */
    public static double d2sDu2(double u, double x0, double y0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double dxdu;
	double dydu;
	double d2xdu2;
	double d2ydu2;
	double u1;
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    return 0.0;
	    // dxdu = coords[0] - x0;
	    // dydu = coords[1] - y0;
	    // break;
	case PathIterator.SEG_QUADTO:
	    u1 = 1.0 - u;
	    dxdu = 2.0 * (u1*(coords[0]-x0) + u*(coords[2]-coords[0]));
	    dydu = 2.0 * (u1*(coords[1]-y0) + u*(coords[3]-coords[1]));
	    d2xdu2 = 2.0 * (x0 - coords[0] + coords[2] - coords[0]);
	    d2ydu2 = 2.0 * (y0 - coords[1] + coords[3] - coords[1]);
	    break;
	case PathIterator.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    dxdu = 3.0 * (u1*u1*(coords[0] - x0)
			  + 2.0*u*u1*(coords[2] - coords[0])
			  + u*u*(coords[4] - coords[2]));
	    dydu = 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[3] - coords[1])
			  + u*u*(coords[5] - coords[3]));
	    double u1u1deriv = 2.0 * (1 - u);
	    double uuderiv = 2.0 * u;
	    double uu1deriv = 1.0 - 2.0 * u;
	    d2xdu2 = 3.0 * (u1u1deriv*(coords[0] - x0)
			  + 2.0*uu1deriv*(coords[2] - coords[0])
			  + uuderiv*(coords[4] - coords[2]));
	    d2ydu2 = 3.0 * (u1u1deriv*(coords[1] - y0)
			  + 2.0*uu1deriv*(coords[3] - coords[1])
			  + uuderiv*(coords[5] - coords[3]));
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}	
	double dsduSquared = dxdu*dxdu + dydu*dydu;
	if (dsduSquared == 0.0) {
	    return Double.NaN;
	} else {
	    return (dxdu*d2xdu2 + dydu*d2ydu2) / Math.sqrt(dsduSquared);
	}
    }


    /**
     * Compute the signed curvature given path-segment parameters.
     * The type may not be {@link PathIterator#SEG_MOVETO}.  When the
     * type is {@link PathIterator#SEG_CLOSE}, the first two elements
     * of coords must be set to the X and Y coordinate respectively
     * for the point provided by most recent segment whose type is
     * {@link PathIterator#SEG_MOVETO}.  A positive curvature
     * corresponds to a rotation from the positive X axis towards
     * the positive Y axis for an angular distance of 90 degrees.
     * Similarly a negative curvature corresponds to a rotation
     * from the positive Y axis towards the positive X axis for an
     * angular distance of 90 degrees.
     * <P>
     * If an instance of {@link Path2DInfo.Entry} <CODE>entry</CODE>
     * is available, it is significantly more efficient to call
     * <CODE>entry.getData().curvature(new UValues(u))</CODE> instead
     * of this method (a timing test showed an improvement of more than
     * a factor of 10).
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be {@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO}
     *         and {@link PathIterator#SEG_CLOSE})
     * @param coords the coordinates array as defined by {@link
     *        java.awt.geom.PathIterator PathIterator}, but with
     *        coords[0] and coords[1] set to the X and Y coordinates
     *        respectively for the last
     *        {@link PathIterator#SEG_MOVETO} operation when the type is
     *         {@link PathIterator#SEG_CLOSE}
     * @return the curvature with positive values indicating that the
     *         tangent vector rotates counterclockwise as the path
     *         parameter increases; Double.NaN if not defined
     *         (this may happen if a line segment has zero length)
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double curvature(double u, double x0, double y0,
				   int type, double[] coords)
	throws IllegalArgumentException
    {
	if (type == PathIterator.SEG_MOVETO) {
	    return Double.NaN;
	} else if (type == PathIterator.SEG_LINETO) {
	    return 0.0;
	} else if (type == PathIterator.SEG_CLOSE) {
	    if (x0 == coords[0] && y0 == coords[1]) {
		return Double.NaN;
	    } else {
		return 0.0;
	    }
	}

	double xp = dxDu(u, x0, y0, type, coords);
	double yp = dyDu(u, x0, y0, type, coords);
	double xpp = d2xDu2(u, x0, y0, type, coords);
	double ypp = d2yDu2(u, x0, y0, type, coords);

	double tmp = xp*xp + yp*yp;
	tmp = tmp * Math.sqrt(tmp);
	if (tmp == 0.0) {
	    // As an example of when this can happen, consider
	    // a curve in which y is constant and x is a quadratic
	    // or cubic function of u such that x = 0 when u = 0 and x = 0
	    // when u = 1.  At some intermediate point, xp will be
	    // zero. The path, however is restricted to a straight line
	    // that doubles back on itself. At the turning point, the
	    // curvature could be positive infinity or negative infinity,
	    // but which it is cannot be determined, so NaN is an
	    // appropriate value.
	    return Double.NaN;
	} else {
	    return (xp*ypp - yp *xpp)/tmp;
	}
    }

    /**
     * Determine if the arguments allow the curvature to be computed.
     * The curvature cannot be computed when the type is
     * PathIterator.SEG_MOVETO or when the control points provided by
     * the array match the initial values x0 and y0.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO,
     *         PathIterator.SEG_QUADTO, PathIterator.SEG_CUBICTO
     *         and PathIterator.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator.SEG_CLOSE
     * @return true if the curvature exists for the path parameter;
     *         false otherwise.
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     */
    public static boolean curvatureExists(double u, double x0, double y0,
					  int type, double[] coords)
	throws IllegalArgumentException, NullPointerException,
	       IndexOutOfBoundsException
    {
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return false;
	case PathIterator.SEG_CLOSE:
	    if (x0 == coords[0] && y0 == coords[1]) {
		return false;
	    }
	    break;
	case PathIterator.SEG_LINETO:
	    if (x0 == coords[0] && y0 == coords[1]) return false;
	    break;
	case PathIterator.SEG_QUADTO:
	    if (x0 == coords[0] && y0 == coords[1]
		&& x0 == coords[2] && y0 == coords[3]) {
		return false;
	    }
	    break;
	case PathIterator.SEG_CUBICTO:
	    if (x0 == coords[0] && y0 == coords[1]
		&& x0 == coords[2] && y0 == coords[3]
		&& x0 == coords[4] && y0 == coords[5]) {
		return false;
	    }
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	return true;
    }

    /**
     * Get the tangent vector for a specified value of the path parameter
     * and an output-array offset.
     * If the tangent vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the tangent vector
     * will be set to zero.   The tangent vector will have unit length
     * if it is not zero.
     * @param u the path-segment parameter in the range [0,1]
     * @param array an array of length no less than 2 used to store
     *        the tangent vector, with array[offset] containing the
     *        tangent vector's X component and array[offset+1]
     *        containing the tangent vector's Y component
     * @param offset the index into the array at which to store the tangent
     *        vector
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO,
     *         PathIterator.SEG_QUADTO, PathIterator.SEG_CUBICTO
     *         and PathIterator.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator.SEG_CLOSE
     * @return true if the tangent vector exists; false if the tangent
     *         vector does not exist
     */
    public static boolean getTangent(double u, double[] array, int offset,
				     double x0, double y0, int type,
				     double[] coords)
    {
	double xp = Path2DInfo.dxDu(u, x0, y0, type, coords);
	double yp = Path2DInfo.dyDu(u, x0, y0, type, coords);
	double tmp = Math.sqrt(xp*xp + yp*yp);
	if (tmp == 0.0) {
	    switch(type) {
	    case PathIterator.SEG_QUADTO:
		if (coords[0] == x0 && coords[1] == y0) {
		    xp = coords[2] - coords[0];
		    yp = coords[3] - coords[1];
		    tmp = Math.sqrt(xp*xp + yp*yp);
		} else if (coords[0] == coords[2] && coords[1] == coords[3]) {
		    xp = coords[0] - x0;
		    yp = coords[1] - y0;
		    tmp = Math.sqrt(xp*xp + yp*yp);
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		if (coords[0] == x0 && coords[1] == y0) {
		    if (coords[0] == coords[2] && coords[1] == coords[3]) {
			xp = coords[4] - coords[2];
			yp  = coords[5] - coords[3];
			tmp = Math.sqrt(xp*xp + yp*yp);
		    } else if (u == 0.0) {
			xp = coords[2] - coords[0];
			yp = coords[3] - coords[1];
			tmp = Math.sqrt(xp*xp + yp*yp);
		    }
		} else if (coords[2] == coords[4] && coords[3] == coords[5]) {
		    if (coords[0] == coords[2] && coords[1] == coords[3]) {
			xp = coords[0] - x0;
			yp = coords[1] - y0;
			tmp = Math.sqrt(xp*xp + yp*yp);
		    } else if (u == 1.0) {
			xp = coords[2] - coords[0];
			yp = coords[3] - coords[1];
			tmp = Math.sqrt(xp*xp + yp*yp);
		    }
		}
		break;
	    }
	}
	if (tmp == 0.0) {
	    array[offset] = 0.0;
	    array[offset+1] = 0.0;
	    return false;
	}
	array[offset] = xp/tmp;
	// prevent -0.0
	if (array[offset] == 0.0) array[offset] = 0.0;
	offset++;
	array[offset] = yp/tmp;
	// prevent -0.0
	if (array[offset] == 0.0) array[offset] = 0.0;
	return true;
    }

    /**
     * Get the normal vector for a given value of the path parameter and
     * an offset for the array storing the normal vector.
     * The normal vector N is a vector of unit length, perpendicular to
     * the tangent vector, and oriented so that
     * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is the
     * (signed) curvature.
     * If the normal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the normal vector
     * will be set to zero.
     * <P>
     * Note: the use of the signed curvature results in the normal
     * vector always pointing in the counter-clockwise direction
     * (i.e., a rotating the X axis towards the Y axis). This allows
     * a normal vector exist for straight-line segments. A different
     * definition is used for 3D paths.
     * @param u the path-segment parameter in the range [0,1]
     * @param array an array of length no less than 2 used to store
     *        the normal vector, with array[offset] containing the
     *        normal vector's X component and array[offset+1]
     *        containing the normal vector's Y component
     * @param offset the index into the array at which to store the normal
     *        vector
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO,
     *         PathIterator.SEG_QUADTO, PathIterator.SEG_CUBICTO
     *         and PathIterator.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator.SEG_CLOSE
     * @return true if the normal vector exists; false if the normal
     *         vector does not exist
     */
    public static boolean getNormal(double u, double[] array, int offset,
				    double x0, double y0, int type,
				     double[] coords)
    {
	boolean status = getTangent(u, array, offset, x0, y0, type, coords);
	if (status == false) {
	    return status;
	}
	double tmp = array[offset];
	double tmp2 = -array[offset+1];
	// prevent -0.0
	if (tmp2 == 0.0) tmp2 = 0.0;
	array[offset] = tmp2;
	array[offset+1] = tmp;
	return true;
    }

    /**
     * Get the starting point along a path.
     * To get the last point on a path, use {@link Path2D#getCurrentPoint()}.
     * @param path the path
     * @return the first point on the path; null if the path is empty
     * @exception NullPointerException the path is null
     * @exception IllegalArgumentException the path is ill formed
     *            (All paths must start with a segment whose type
     *            is {@link PathIterator#SEG_MOVETO}.)
     * @see Path2D#getCurrentPoint()
     */
    public static Point2D getStart(Path2D path)
	throws NullPointerException, IllegalArgumentException
    {
	if (path == null) {
	    throw new NullPointerException(errorMsg("nullArg", 1));
	}
	PathIterator pi = path.getPathIterator(null);
	if (pi.isDone()) return null;
	double[] coords = new double[6];
	if (pi.currentSegment(coords) != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("piSEGMOVETO"));
	}
	return new Point2D.Double(coords[0], coords[1]);
    }

    /**
     * Enumeration naming Locations along a path.
     */
    public static enum Location {
	/**
	 * The start of a path
	 */
	START,
	/**
	 * The end of a path
	 */
	END
    }

    /**
     * Get the tangent vector at the start or end of a path.
     * @param path the path
     * @param location {@link Location#START} if the tangent vector is
     *        computed at the start of the path;
     *        {@link Location#END}if the tangent vector is computed at the
     *        end of the path.
     * @param array an array used to store the tangent vector
     * @param offset the offset into the array such that the X component
     *        of the tangent vector is array[offset] and the Y component
     *        of the tangent vector is array[offset+1]
     * @return true if the tangent vector exists; false otherwise
     * @exception IllegalArgumentException the path is not a valid path
     *            or the array and offset are not suitable for storing the
     *            tangent vector
     * @exception NullPointerException an argument (path or array) is null
     */
    public static boolean getTangent(Path2D path, Location location,
				     double[] array, int offset)
	throws IllegalArgumentException, NullPointerException
    {
	if (path == null) {
	    throw new NullPointerException(errorMsg("nullArg", 1));
	}
	if (location == null) {
	    throw new NullPointerException(errorMsg("nullArg", 2));
	}
	if (array == null) {
	    throw new NullPointerException(errorMsg("nullArg", 3));
	}
	if (array.length < 2) {
	    throw new IllegalArgumentException(errorMsg("tangentLength", 3));
	}
	if (offset < 0 || offset + 1 > array.length - 1) {
	    throw new
		IllegalArgumentException(errorMsg("tangentOffset", 4, offset));
	}
	PathIterator pi = path.getPathIterator(null);
	if (pi.isDone()) {
	    array[offset++] = 0.0;
	    array[offset] = 0.0;
	    return false;
	}
	double[] coords = new double[6];
	if (location == Location.START) {
	    // tangent at start
	    if (pi.currentSegment(coords) != PathIterator.SEG_MOVETO) {
		array[offset++] = 0.0;
		array[offset] = 0.0;
		throw new IllegalArgumentException(errorMsg("piSEGMOVETO"));
	    }
	    double x1 = coords[0];
	    double y1 = coords[1];
	    pi.next();
	    if (pi.isDone()) {
		array[offset++] = 0.0;
		array[offset] = 0.0;
		return false;
	    }
	    switch(pi.currentSegment(coords)) {
	    case PathIterator.SEG_LINETO:
		{
		    double dx = coords[0] - x1;
		    double dy = coords[1] - y1;
		    double norm = Math.sqrt(dx*dx + dy*dy);
		    if (norm == 0.0) {
			array[offset++] = 0.0;
			array[offset] = 0.0;
			return false;
		    } else {
			array[offset] = dx/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			offset++;
			array[offset] = dy/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			return true;
		    }
		}
	    case PathIterator.SEG_QUADTO:
		{
		    double dx = coords[0] - x1;
		    double dy = coords[1] - y1;
		    double norm = Math.sqrt(dx*dx + dy*dy);
		    if (norm == 0.0) {
			if (coords[0] == x1 && coords[1] == y1) {
			    dx = coords[2] - coords[0];
			    dy = coords[3] - coords[1];
			    norm = Math.sqrt(dx*dx + dy*dy);
			} else if (coords[0] == coords[2]
				   && coords[1] == coords[3]) {
			    dx = coords[0] - x1;
			    dx = coords[1] - y1;
			    norm = Math.sqrt(dx*dx + dy*dy);
			}
		    }
		    if (norm == 0.0) {
			array[offset++] = 0.0;
			array[offset] = 0.0;
			return false;
		    } else {
			array[offset] = dx/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			offset++;
			array[offset] = dy/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			return true;
		    }
		}
	    case PathIterator.SEG_CUBICTO:
		{
		    double dx = coords[0] - x1;
		    double dy = coords[1] - y1;
		    double norm = Math.sqrt(dx*dx + dy*dy);
		    if (norm == 0.0) {
			if (coords[0] == x1 && coords[1] == y1) {
			    if (coords[0] == coords[2]
				&& coords[1] == coords[3]) {
				dx = coords[4] - coords[2];
				dy = coords[5] - coords[3];
				norm = Math.sqrt(dx*dx + dy*dy);
			    } else /*if (u == 0.0)*/ {
				dx = coords[2] - coords[0];
				dy = coords[3] - coords[1];
				norm = Math.sqrt(dx*dx + dy*dy);
			    }
			} else if (coords[2] == coords[4]
				   && coords[3] == coords[5]) {
			    if (coords[0] == coords[2]
				&& coords[1] == coords[3]) {
				dx = coords[0] - x1;
				dy = coords[1] - y1;
				norm = Math.sqrt(dx*dx + dy*dy);
			    }
			}
		    }
		    if (norm == 0.0) {
			array[offset++] = 0.0;
			array[offset] = 0.0;
			return false;
		    } else {
			array[offset] = dx/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			offset++;
			array[offset] = dy/norm;
			// prevent -0.0
			if (array[offset] == 0.0) array[offset] = 0.0;
			return true;
		    }
		}
	    default:
		array[offset++] = 0.0;
		array[offset] = 0.0;
		return false;
	    }
	} else {
	    // tangent at end
	    double lastX = 0.0, lastY = 0.0;
	    double px = 0.0, py = 0.0;
	    double x = 0.0, y = 0.0;
	    double lx = 0.0, ly = 0.0;
	    boolean hasTangent = false;
	    boolean closeSeen = false;
	    int lastType = -1;
	    while (!pi.isDone()) {
		int ourType = pi.currentSegment(coords);
		switch(ourType) {
		case PathIterator.SEG_MOVETO:
		    hasTangent = false;
		    lastType = -1;
		    lastX = coords[0];
		    lastY = coords[1];
		    x = lastX;
		    y = lastY;
		    closeSeen = false;
		    break;
		case PathIterator.SEG_LINETO:
		    hasTangent = true;
		    lastType = ourType;
		    lx = x;
		    ly = y;
		    px = x;
		    py = y;
		    x = coords[0];
		    y = coords[1];
		    closeSeen = false;
		    break;
		case PathIterator.SEG_QUADTO:
		    hasTangent = true;
		    lastType = ourType;
		    lx = x;
		    ly = y;
		    px = coords[0];
		    py = coords[1];
		    x = coords[2];
		    y = coords[3];
		    closeSeen = false;
		    break;
		case PathIterator.SEG_CUBICTO:
		    hasTangent = true;
		    lastType = ourType;
		    lx = x;
		    ly = y;
		    px = coords[2];
		    py = coords[3];
		    x = coords[4];
		    y = coords[5];
		    closeSeen = false;
		    break;
		case PathIterator.SEG_CLOSE:
		    if (closeSeen == false) {
			if (x != lastX || y != lastY) {
			    hasTangent = true;
			    lastType = PathIterator.SEG_LINETO;
			    lx = x;
			    ly = y;
			    px = x;
			    py = y;
			    x = lastX;
			    y = lastY;
			}
		    }
		    closeSeen = true;
		    break;
		}
		pi.next();
	    }
	    if (hasTangent) {
		double dx = x - px;
		double dy = y - py;
		double norm = Math.sqrt(dx*dx + dy*dy);
		if (norm == 0.0) {
		    switch(lastType) {
		    case PathIterator.SEG_QUADTO:
			px = lx;
			py = ly;
			dx = x - px;
			dy = y - py;
			norm = Math.sqrt(dx*dx + dy*dy);
			break;
		    case PathIterator.SEG_CUBICTO:
			if (coords[2] == coords[0] && coords[3] == coords[1]) {
			    px = lx;
			    py = ly;
			    dx = x - px;
			    dy = y - py;
			    norm = Math.sqrt(dx*dx + dy*dy);
			} else {
			    px = coords[0];
			    py = coords[1];
			    x = coords[2];
			    y = coords[3];
			    dx = x - px;
			    dy = y - py;
			    norm = Math.sqrt(dx*dx + dy*dy);
			}
			break;
		    default:
			break;
		    }
		}
		if (norm == 0.0) {
		    array[offset++] = 0.0;
		    array[offset] = 0.0;
		    return false;
		} else {
		    array[offset] = dx/norm;
		    // prevent -0.0
		    if (array[offset] == 0.0) array[offset] = 0.0;
		    offset++;
		    array[offset] = dy/norm;
		    // prevent -0.0
		    if (array[offset] == 0.0) array[offset] = 0.0;
		    return true;
		}
	    } else {
		array[offset++] = 0.0;
		array[offset] = 0.0;
		return false;
	    }
	}
    }

    /**
     * Get the normal vector at the start or end of a path.
     * @param path the path
     * @param location {@link Location#START} if the normal vector is
     *        computed at the start of the path;
     *        {@link Location#END}if the normal vector is computed at the
     *        end of the path.
     * @param array an array used to store the normal vector
     * @param offset the offset into the array such that the X component
     *        of the normal vector is array[offset] and the Y component
     *        of the normal vector is array[offset+1]
     * @return true if the tangent vector exists; false otherwise
     * @exception IllegalArgumentException the path is not a valid path
     *            or the array and offset are not suitable for storing the
     *            normal vector
     * @exception NullPointerException an argument (path or array) is null
     */
    public static boolean getNormal(Path2D path, Location location,
				    double[] array, int offset)
    {
	boolean result = getTangent(path, location, array, offset);
	double tmp = array[offset];
	double tmp2 = -array[offset+1];
	// prevent -0.0
	if (tmp2 == 0.0) tmp2 = 0.0;
	array[offset] = tmp2;
	array[offset+1] = tmp;
	return result;
    }

    /**
     * Get the type of a path-iterator item, formatted as a string
     * @return a string describing the type
     */
    public static String getTypeString(int type) {
	switch (type) {
	case PathIterator.SEG_CLOSE:
	    return "SEG_CLOSE";
	case PathIterator.SEG_MOVETO:
	    return "SEG_MOVETO";
	case PathIterator.SEG_LINETO:
	    return "SEG_LINETO";
	case PathIterator.SEG_QUADTO:
	    return "SEG_QUADTO";
	case PathIterator.SEG_CUBICTO:
	    return "SEG_CUBICTO";
	default:
	    return "<Unknown>";
	}
    }

    /**
     * Class defining a list entry describing a path segment.
     * The list is returned by calling
     * {@link Path2DInfo#getEntries(Shape) getEntries}.
     * @see Path2DInfo#getEntries(Shape)
     */
    public static class Entry {
	private int index;
	int type;
	private boolean hasStart = false;
	double x;
	double y;
	// private Point2D start;
	private Point2D end;
	private double segmentLength;
        double[] coords;
	SegmentData data;

	/**
	 * Get an index indicating the segment's position along a path.
	 * @return the index
	 */
	public int getIndex() {return index;}

	/**
	 * Get the starting point for the segment
	 * @return the starting point for the segment
	 */
	public Point2D getStart() {
	    return hasStart? new Point2D.Double(x, y): null;
	    // return start;
	}

	/**
	 * Get the ending point for the segment.
	 * @return the ending point
	 */
	public Point2D getEnd() {return end;}

	/**
	 * Get the length of a segment.
	 * @return the length of the segment
	 */
	public double getSegmentLength() {return segmentLength;}

	/**
	 * Get the coordinate array for the segment.
	 * This will include intermediate control points and the end point
	 * but not the starting point of the segment.
	 * The coordinate array that is returned must not be modified.
	 * @return the coordinate array for the segment
	 */
	public double[] getCoords() {return coords;}

	/**
	 * Get the segment data associated with this segment.
	 * @return the segment data for this entry
	 */
	public SegmentData getData() {return data;}

	/**
	 * Get the type of the segment
	 * @return the segment type (PathIterator.SEG_MOVETO,
	 *         PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO,
	 *         PathIterator.SEG_CUBICTO and PathIterator.SEG_CLOSE)
	 */
	public int getType() {return type;}

	/**
	 * Get the type of the segment, formatted as a string
	 * @return a string describing the segment type
	 */
	public String getTypeString() {
	    switch (type) {
	    case PathIterator.SEG_CLOSE:
		return "SEG_CLOSE";
	    case PathIterator.SEG_MOVETO:
		return "SEG_MOVETO";
	    case PathIterator.SEG_LINETO:
		return "SEG_LINETO";
	    case PathIterator.SEG_QUADTO:
		return "SEG_QUADTO";
	    case PathIterator.SEG_CUBICTO:
		return "SEG_CUBICTO";
	    default:
		return "<Unknown>";
	    }
	}

	Entry(int ind, int type, Point2D start, Point2D end, double length,
	      double[] coords, SegmentData data) {
	    this.index = ind;
	    this.type = type;
	    // this.start = start;
	    if (start != null) {
		this.x = start.getX();
		this.y = start.getY();
		hasStart = true;
	    }
	    this.end = end;
	    segmentLength = length;
	    this.coords = new double[6];
	    switch (type) {
	    case PathIterator.SEG_CLOSE:
		for (int i = 0; i < 2; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 2; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 2; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 4; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator.SEG_CUBICTO:
	    default:
		for (int i = 0; i < 6; i++) this.coords[i] = coords[i];
		break;
	    }
	    this.data = data;
	}

	Entry(int ind, int type, double xs, double ys, double xe,
	      double ye, double length, double[] coords,
	      SegmentData data)
	{
	    this(ind, type, new Point2D.Double(xs, ys),
		 new Point2D.Double(xe, ye),
		 length, coords, data);
	}
    }

    /**
     * Compute the length of a path or the length of an outline of a shape.
     * This is the sum of the the lengths of all the path or outline segments.
     * A shape's outline may pass through the shape, so this method does not
     * reliably compute the circumference of a shape.
     * @param path the path
     * @return the path length
     */
    public static double pathLength(Shape path) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	for (Entry entry: Path2DInfo.getEntries(path)) {
	    // length += entry.getSegmentLength();
	    adder.add(entry.getSegmentLength());
	}
	// return length;
	return adder.getSum();
    }

    /**
     * Compute the length of a path or the length of an outline of a shape,
     * modified by an affine transform.
     * This is the sum of the the lengths of all the path or outline segments.
     * A shape's outline may pass through the shape, so this method does not
     * reliably compute the circumference of a shape.
     * @param path the path
     * @param at the affine transform; null if none is to be used
     * @return the path length
     */
    public static double pathLength(Shape path, AffineTransform at) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	for (Entry entry: Path2DInfo.getEntries(path, at)) {
	    // length += entry.getSegmentLength();
	    adder.add(entry.getSegmentLength());
	}
	// return length;
	return adder.getSum();
    }

    /**
     * Compute the length of a path or the length of an outline of a shape
     * for a given range of path/outline segments.
     * This is the sum of the the lengths of all the path segments whose
     * indices are in the interval [start, end) with start &lt; end.
     * Some of the segments for a shape may pass though the interior of the
     * shape.
     * @param path the path
     * @param start the starting index
     * @param end the index just past the last index counted
     * @return the length of the portion of a path in the specified interval
     */
    public static double pathLength(Shape path, int start, int end) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	int ind = 0;
	for (Entry entry: Path2DInfo.getEntries(path)) {
	    if (ind >= start && ind < end) {
		// length += entry.getSegmentLength();
		adder.add(entry.getSegmentLength());
	    }
	    ind++;
	}
	// return length;
	return adder.getSum();
    }

    /**
     * Compute the length of a path or the length of an outline of a shape
     * for a given range of path/outline segments, modified by an affine
     * transform.
     * This is the sum of the the lengths of all the path segments whose
     * indices are in the interval [start, end) with start &lt; end.
     * Some of the segments for a shape may pass though the interior of the
     * shape.
     * @param path the path
     * @param at the affine transform; null if none is to be used
     * @param start the starting index
     * @param end the index just past the last index counted
     * @return the length of the portion of a path in the specified interval
     */
    public static double pathLength(Shape path, AffineTransform at,
				    int start, int end)
    {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	int ind = 0;
	for (Entry entry: Path2DInfo.getEntries(path, at)) {
	    if (ind >= start && ind < end) {
		// length += entry.getSegmentLength();
		adder.add(entry.getSegmentLength());
	    }
	    ind++;
	}
	// return length;
	return adder.getSum();
    }


    static final int N4LEN = 16;

    static double[] u4len = GLQuadrature.getArguments(0.0, 1.0, N4LEN);
    static UValues[] uv4len = new UValues[N4LEN];
    static {
	for (int i = 0; i < N4LEN; i++) {
	    uv4len[i] = new UValues(u4len[i]);
	    u4len[i] = (double) i;
	}
    }
    static GLQuadrature<SegmentData> glq4len =
	new GLQuadrature<SegmentData>(N4LEN) {
	protected double function(double iu, SegmentData data) {
	    int i = (int)Math.round(iu);
	    return data.dsDu(uv4len[i]);
	}
    };


    /**
     * Compute the length of a segment given parameters describing it.
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by 
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO,
     *         PathIterator.SEG_QUADTO, PathIterator.SEG_CUBICTO
     *         and PathIterator.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the X and Y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator.SEG_CLOSE
     * @return the segment length
     */
    public static double segmentLength(final int type,
				       final double x0, final double y0,
				       final double[] coords)
    {
	switch (type) {
	case PathIterator.SEG_MOVETO:
	    return 0.0;
	case PathIterator.SEG_CLOSE:
	    // coords must be set so that (coords[0],coords[1]) is the
	    // position set by the last SEG_MOVETO
	    /*
	    if ((coords[0] == x0) && (coords[1] == y0)) {
		return 0.0;
	    } else {
		double dx = coords[0] - x0;
		double dy = coords[1] - y0;
		return Math.sqrt(dx*dx + dy*dy);
	    }
	    */
	case PathIterator.SEG_LINETO:
	    {
		double dx = coords[0] - x0;
		double dy = coords[1] - y0;
		if (dx == 0.0) {
		    if (dy == 0.0) return 0.0;
		    else return Math.abs(dy);
		} else if (dy == 0.0) {
		    return Math.abs(dx);
		} else {
		    return Math.sqrt(dx*dx + dy*dy);
		}
	    }
	default:
	    {
		double[] fcoords = new double[6];
		double delta;
		double dx = 0.0, dy = 0.0;
		if (type == PathIterator.SEG_QUADTO) {
		    dx = coords[2] - x0;
		    dy = coords[3] - y0;
		} else if (type == PathIterator.SEG_CUBICTO) {
		    dx = coords[4] - x0;
		    dy = coords[5] - y0;
		} else {
		    throw new RuntimeException
			("type value not expected: " + type);
		}
		delta = 0.05 * Math.sqrt(dx*dx + dy*dy);
		FlatteningPathIterator2D fpit
		    = new FlatteningPathIterator2D(type, x0, y0,
						   coords, delta, 10);
		fpit.next();
		double xx0 = x0;
		double yy0 = y0;
		Adder adder2 = new Adder.Kahan();
		while (!fpit.isDone()) {
		    int fst = fpit.currentSegment(fcoords);
		    double flen =
			glq4len.integrateWithP(u4len,
					       new SegmentData(fst,
							       xx0, yy0,
							       fcoords,
							       null));
		    adder2.add(flen);
		    if (fst == PathIterator.SEG_QUADTO) {
			xx0 = fcoords[2];
			yy0 = fcoords[3];
		    } else {
			xx0 = fcoords[4];
			yy0 = fcoords[5];
		    }
		    fpit.next();
		}
		return adder2.getSum();
	    }
	    /*
	    return glq4len.integrateWithP(u4len,
					  new SegmentData(type, x0, y0,
							  coords, null));
	    */
	}
    }

    /**
     * Get the length of the i<sup>th</sup> segment of a path or of a
     * shape's outline.
     * For a shape, some of the segments may pass through the interior of
     * the shape.
     * @param p the path or shape
     * @param segment the segment number from 0 up to but not
     *        including the number of segments
     * @return the segment length
     */

    public static double segmentLength(Shape p, int segment) {
	PathIterator pit = p.getPathIterator(null);
	final double[] coords = new double[6];
	int segno = 0;
	int st = pit.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException("ill-formed path");
	}
	if (segment == 0) return 0.0;
	double lastX = 0.0;
	double lastY = 0.0;
	double lastMoveToX = 0.0; 
	double lastMoveToY = 0.0;
	for (int i = 0; i < segment; i++) {
	    if (pit.isDone()) {
		throw new 
		    IllegalArgumentException(errorMsg("segNumbOutOfRange"));
	    } else {
		st = pit.currentSegment(coords);
		switch (st) {
		case PathIterator.SEG_CLOSE:
		    lastX = lastMoveToX;
		    lastY = lastMoveToY;
		    /*
		      coords[0] = lastMoveToX;
		      coords[1] = lastMoveToY;
		      lastX = coords[0];
		      lastY = coords[1];
		    */
		    break;
		case PathIterator.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    lastMoveToX = lastX;
		    lastMoveToY = lastY;
		    break;
		case PathIterator.SEG_LINETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    break;
		case PathIterator.SEG_QUADTO:
		    lastX = coords[2];
		    lastY = coords[3];
		    break;
		case PathIterator.SEG_CUBICTO:
		    lastX = coords[4];
		    lastY = coords[5];
		    break;
		default:
		    throw new IllegalArgumentException(errorMsg("piUnknown"));
		}
		pit.next();
	    }
	}
	if (pit.isDone()) {
	    throw new 
		IllegalArgumentException(errorMsg("segNumbOutOfRange"));
	}
	final int segmentType = pit.currentSegment(coords);
	if (segmentType == PathIterator.SEG_CLOSE) {
	    coords[0] = lastMoveToX;
	    coords[1] = lastMoveToY;
	}

	// final double x0 = lastX;
	// final double y0 = lastY;

	return segmentLength(segmentType, lastX, lastY, coords);

	/*
	GLQuadrature glq = new GLQuadrature(16) {
		protected double function(double u) {
		    return Path2DInfo.dsDu(u, x0, y0, segmentType, coords);
		}
	    };
	return glq.integrate(0.0, 1.0);
	*/
    }

    /**
     * Get a list of entries describing a path's segments or those for
     * the outline of a shape.
     * @param p the path or shape
     * @return the list of entries
     */
    public static List<Entry> getEntries (Shape p) {
	return getEntries(p, null);
    }

    /**
     * Get a list of entries describing a path's segments or those for
     * the outline of a shape, modified by an affine transform.
     * @param p the path or shape
     * @param at the affine transform; null if none is to be used
     * @return the list of entries
     */
    public static List<Entry> getEntries (Shape p, AffineTransform at) {
	List<Entry> list = new LinkedList<Entry>();
	PathIterator pit = p.getPathIterator(at);
	if (pit.isDone()) return list;
	final double[] coords = new double[6];
	double[] fcoords = new double[6];
	int segno = 0;
	int stt = pit.currentSegment(coords);
	if (stt != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX; 
	double lastMoveToY = lastY;

	double length = 0.0;
	SegmentData last = null;
	for (int i = 0; !pit.isDone(); i++) {
	    final int st = pit.currentSegment(coords);
	    final double x0 = lastX;
	    final double y0 = lastY;
	    if (st == PathIterator.SEG_CLOSE) {
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    SegmentData data = new SegmentData(st, x0, y0, coords, last);
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // special case - for straight lines, this is faster. The
		    // tradeoff is the cost of the test to see determine if the
		    // current segment type is SEG_CLOSE or SEG_LINETO.
		    // Also treat horizontal and vertical lines as a special
		    // case.
		    double dx = coords[0] - x0;
		    double dy = coords[1] - y0;
		    if (dx == 0.0) {
			if (dy == 0.0) length = 0.0;
			else length = Math.abs(dy);
		    } else if (dy == 0.0) {
			length = Math.abs(dx);
		    } else {
			length = Math.sqrt(dx*dx + dy*dy);
		    }
		} else {
		    /*
		    GLQuadrature glq = new GLQuadrature(16) {
			    protected double function(double u) {
				return Path2DInfo.dsDu(u, x0, y0,
						       st, coords);
			    }
			};
		    length = glq.integrate(0.0, 1.0);
		    */
		    double delta;
		    double dx = 0.0, dy = 0.0;
		    if (st == PathIterator.SEG_QUADTO) {
			dx = coords[2] - x0;
			dy = coords[3] - y0;
		    } else if (st == PathIterator.SEG_CUBICTO) {
			dx = coords[4] - x0;
			dy = coords[5] - y0;
		    } else {
			throw new RuntimeException
			    ("st value not expected: " + st);
		    }
		    delta = 0.05 * Math.sqrt(dx*dx + dy*dy);
		    FlatteningPathIterator2D fpit
			= new FlatteningPathIterator2D(st, x0, y0,
						       coords, delta, 10);
		    fpit.next();
		    double xx0 = x0;
		    double yy0 = y0;
		    Adder adder2 = new Adder.Kahan();
		    while (!fpit.isDone()) {
			int fst = fpit.currentSegment(fcoords);
			double flen =
			    glq4len.integrateWithP(u4len,
						   new SegmentData(fst,
								   xx0, yy0,
								   fcoords,
								   null));
			adder2.add(flen);
			if (fst == PathIterator.SEG_QUADTO) {
			    xx0 = fcoords[2];
			    yy0 = fcoords[3];
			} else {
			    xx0 = fcoords[4];
			    yy0 = fcoords[5];
			}
			fpit.next();
		    }
		    length = adder2.getSum();
		    /*
		    length =
			glq4len.integrateWithP(u4len,
					       new SegmentData(st, x0, y0,
							       coords, null));
		    */
		}
	    } else {
		length = 0.0;
	    }
	    boolean fullEntry = false;
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    if (fullEntry) {
		list.add(new Entry(i, st, x0, y0, lastX, lastY, length,
				   coords, data));
	    } else {
		list.add(new Entry(i, st, null,
				   new Point2D.Double(lastX, lastY),
				   length, coords, data));
	    }
	    last = data;
	    pit.next();
	}
	return list;
    }

    // The numerical integration uses only 3 terms per segment.
    // This works because the function being integrated is a
    // fifth order polynomial of the parameter u and Gaussian-
    // Legendre quadrature with n points is exact for polynomials
    // of degree (2n-1) or less.

    static final int N4FORM = 3;

    // The u4form array initially contains the values of u to use in
    // the integration.  We create a parallel array containing the
    // corresponding UValues objects, which precompute some terms,
    // and then replace u4form values with the index of the corresponding
    // array containing the matching UValues object.
    // This allows the integration to be completed faster because of the
    // use of precomputed terms.

    static double[] u4form = GLQuadrature.getArguments(0.0, 1.0, N4FORM);
    static UValues[] uv4form = new UValues[N4FORM];
    static {
	for (int i = 0; i < N4FORM; i++) {
	    uv4form[i] = new UValues(u4form[i]);
	    u4form[i] = (double) i;
	}
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = xdy - ydx along a path.  For
     * a closed path, the generalized Stokes' theorem (which
     * reduces to Green's theorem in the 2-dimensional case)
     * implies that
     * <blockquote>
     * &int;<sub>&part;X</sub>&omega; = &int;<sub>X</sub> d&omega;
     * </blockquote>
     * or
     * <blockquote>
     * &int;<sub>&part;X</sub>(xdy - ydx) = 2&int;<sub>X</sub> dxdy
     * </blockquote>.
     */
    static class GLQ4Form extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4Form() {super(N4FORM);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form, new SegmentData(st, x0, y0, coords,
							  null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return x*dydu - y*dxdu;
	}
    }
    private static GLQ4Form glq = new GLQ4Form();

    /**
     * Determine if a path is oriented counterclockwise when traversed
     * in the direction of an increasing path parameter.
     * Paths must start with a {@link PathIterator#SEG_MOVETO} segment
     * and end with a {@link PathIterator#SEG_CLOSE} segment, with no other
     * {@link PathIterator#SEG_CLOSE} segments along the path, and a path
     * should not intersect itself.  Self-intersecting paths are not
     * detected, with the exception of multiple loops whose signed areas
     * sum to zero, where a signed area is positive for counterclockwise
     * loops and negative for clockwise loops. The signed area is used to
     * distinguish clockwise and counterclockwise paths
     * <P>
     * The terms clockwise and counterclockwise assume the normal mathematical
     * convention in which the positive X axis points right and the positive
     * Y axis points left.  This is the opposite of the convention used in
     * Java's AWT classes in which the positive Y axis points downwards.
     * @param path the path
     * @return true if the path is counterclockwise; false if it is clockwise
     * @exception the path is not a simple loop (a path terminated by a
     *            {@link PathIterator#SEG_CLOSE} segment, with no other
     *            {@link PathIterator#SEG_CLOSE} segments along
     *            the path) or the region enclosed by the path has
     *            zero area or the path crosses itself (in such a way
     *            that the area computed is zero)
     */
    public static boolean isCounterclockwise(Path2D path)
	throws IllegalArgumentException
    {
	double area2 = integralxDyMinusyDx(path.getPathIterator(null),
					   true);
	if (area2 == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroAreaPath"));
	}
	return (area2 > 0.0);
    }

    /**
     * Determine if a path is oriented clockwise when traversed
     * in the direction of an increasing path parameter.
     * Paths must start with a {@link PathIterator#SEG_MOVETO} segment
     * and end with a {@link PathIterator#SEG_CLOSE} segment, with no other
     * {@link PathIterator#SEG_CLOSE} segments along the path, and a path
     * should not intersect itself.  Self-intersecting paths are not
     * detected, with the exception of multiple loops whose signed areas
     * sum to zero, where a signed area is positive for counterclockwise
     * loops and negative for clockwise loops. The signed area is used to
     * distinguish clockwise and counterclockwise paths
     * <P>
     * The terms clockwise and counterclockwise assume the normal mathematical
     * convention in which the positive X axis points right and the positive
     * Y axis points left.  This is the opposite of the convention used in
     * Java's AWT classes in which the positive Y axis points downwards.
     * @param path the path
     * @return true if the path is clockwise; false if it is counterclockwise
     * @exception the path is not a simple loop (a path terminated by a
     *            {@link PathIterator#SEG_CLOSE} segment, with no other
     *            {@link PathIterator#SEG_CLOSE} segments along
     *            the path) or the region enclosed by the path has
     *            zero area or the path crosses itself (in such a way
     *            that the area computed is zero)
     */
    public static boolean isClockwise(Path2D path)
	throws IllegalArgumentException
    {
	double area2 = integralxDyMinusyDx(path.getPathIterator(null),
					   true);
	if (area2 == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroAreaPath"));
	}
	return (area2 < 0.0);
    }


    /**
     * Integrate the differential form &omega; = xdy - ydx along a path.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralxDyMinusyDx(PathIterator pi,
					      boolean singleLoop)
    {
	double[] coords = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean mustStop = false;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    if (mustStop) {
		throw new IllegalArgumentException(errorMsg("notSimpleClosedPath"));
	    }
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
		if (singleLoop) mustStop = true;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double cmy = coords[1] - lastY;
		    double cmx = coords[0] - lastX;
		    if (cmy != 0.0 || cmx != 0.0) {
			double k1 = coords[0]*cmy - coords[1]*cmx;
			double k2 = lastX * cmy - lastY * cmx;
			// sum += (k1 + k2)/2.0;
			adder.add((k1 + k2)/2.0);
			/*
			if (Math.abs((k1+k2)/2.0
				     - glq.integrate(lastX, lastY, st, coords))
			    > 1.0e-8) {
			    throw new RuntimeException("Exact integral failed");
			}
			*/
		    }
		} else {
		    // sum += glq.integrate(lastX, lastY, st, coords);
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX, coords[0], coords[2]):
			new BezierPolynomial(lastX, coords[0],
					     coords[2], coords[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY, coords[1], coords[3]):
			new BezierPolynomial(lastY, coords[1],
					     coords[3], coords[5]);
		    BezierPolynomial dxdu = bx.deriv();
		    BezierPolynomial dydu = by.deriv();
		    bx.multiplyBy(dydu);
		    by.multiplyBy(dxdu);
		    by.multiplyBy(-1);
		    BezierPolynomial integrand = bx.add(by);
		    adder.add(integrand.integralAt(1.0));
		    /*
		    adder.add(glq.integrate(lastX, lastY, st, coords));
		    */
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (singleLoop && !mustStop) {
	    throw new IllegalArgumentException(errorMsg("notSimpleClosedPath"));
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double cmy = lastMoveToY - lastY;
	    double cmx = lastMoveToX - lastX;
	    if (cmy != 0.0 || cmx != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double k1 = lastMoveToX*cmy - lastMoveToY*cmx;
		double k2 = lastX*cmy - lastY*cmx;
		adder.add((k1 + k2)/2.0);

	    }
	}
	// return sum;
	return adder.getSum();
    }

    static final int N4FORM_XY = 5;
    static double[] u4form_xy = GLQuadrature.getArguments(0.0, 1.0, N4FORM_XY);
    static UValues[] uv4form_xy = new UValues[N4FORM_XY];
    static {
	for (int i = 0; i < N4FORM_XY; i++) {
	    uv4form_xy[i] = new UValues(u4form_xy[i]);
	    u4form_xy[i] = (double) i;
	}
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = -xydx along a path.  For
     * a closed path, the generalized Stokes' theorem (which
     * reduces to Green's theorem in the 2-dimensional case)
     * implies that
     * <blockquote>
     * &int;<sub>&part;X</sub>&omega; = &int;<sub>X</sub> d&omega;
     * </blockquote>
     * or
     * <blockquote>
     * &int;<sub>&part;X</sub>(xdy - ydx) = 2&int;<sub>X</sub> dxdy
     * </blockquote>.
     */
    static class GLQ4FormXydx extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4FormXydx() {super(N4FORM_XY);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form_xy, new SegmentData(st, x0, y0, coords,
							     null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form_xy[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double dxdu = data.dxDu(uv);
	    // double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return -x*y*dxdu;
	}
    }
    private static GLQ4FormXydx glqxyDx = new GLQ4FormXydx();

    /**
     * Integrate the differential form &omega; = -xydx along a path.
     * Useful for computing the center of mass of a shape.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralNegativeXyDx(PathIterator pi, double yc)
    {
	double[] coords = new double[6];
	double[] coords2 = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double clastY = lastY - yc;
		    double Ay = coords[1] - lastY;
		    double Ax = coords[0] - lastX;
		    if (Ay != 0.0 || Ax != 0.0) {
			double v = Ax*Ax*Ay/3 + Ax*(Ay*lastX + Ax*clastY)/2
			    + Ax*lastX*clastY;
			v = -v;
			adder.add(v);
			/*
			if (Math.abs((k1+k2)/2.0
				     - glq.integrate(lastX, lastY, st, coords))
			    > 1.0e-8) {
			    throw new RuntimeException("Exact integral failed");
			}
			*/
		    }
		} else {
		    // sum += glq.integrate(lastX, lastY, st, coords);
		    for (int i = 0; i < 6; i++) {
			coords2[i] = coords[i];
			if (i % 2 == 1) {
			    coords2[i] -= yc;
			}
		    }
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX, coords2[0], coords2[2]):
			new BezierPolynomial(lastX, coords2[0],
					     coords2[2], coords2[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY-yc, coords2[1], coords2[3]):
			new BezierPolynomial(lastY-yc, coords2[1],
					     coords2[3], coords2[5]);
		    BezierPolynomial dxdu = bx.deriv();
		    bx.multiplyBy(by);
		    bx.multiplyBy(dxdu);
		    double v = bx.integralAt(1.0);
		    if (v != 0.0) v = -v;
		    adder.add(v);
		    /*
		    adder.add(glqxyDx.integrate(lastX, lastY-yc, st, coords2));
		    */
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double clastY = lastY - yc;
	    double Ay = lastMoveToY - lastY;
	    double Ax = lastMoveToX - lastX;
	    if (Ay != 0.0 || Ax != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double v = Ax*Ax*Ay/3 + Ax*(Ay*lastX + Ax*clastY)/2
		    + Ax*lastX*clastY;
		v = -v;
		adder.add(v);
	    }
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = -xydx along a path.  For
     * a closed path, the generalized Stokes' theorem (which
     * reduces to Green's theorem in the 2-dimensional case)
     * implies that
     * <blockquote>
     * &int;<sub>&part;X</sub>&omega; = &int;<sub>X</sub> d&omega;
     * </blockquote>
     * or
     * <blockquote>
     * &int;<sub>&part;X</sub>(xdy - ydx) = 2&int;<sub>X</sub> dxdy
     * </blockquote>.
     */
    static class GLQ4FormXydy extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4FormXydy() {super(N4FORM_XY);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form_xy, new SegmentData(st, x0, y0, coords,
							     null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form_xy[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    // double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return x*y*dydu;
	}
    }
    private static GLQ4FormXydy glqxyDy = new GLQ4FormXydy();

    /**
     * Integrate the differential form &omega; = xydy along a path.
     * Useful for computing the center of mass of a shape.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralXyDy(PathIterator pi, double xc)
    {
	double[] coords = new double[6];
	double[] coords2 = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double clastX = lastX - xc;
		    double Ay = coords[1] - lastY;
		    double Ax = coords[0] - lastX;
		    if (Ay != 0.0 || Ax != 0.0) {
			double v = Ay*Ax*Ay/3 + Ay*(Ay*clastX + Ax*lastY)/2
			    + Ay*clastX*lastY;
			adder.add(v);
			/*
			if (Math.abs((k1+k2)/2.0
				     - glq.integrate(lastX, lastY, st, coords))
			    > 1.0e-8) {
			    throw new RuntimeException("Exact integral failed");
			}
			*/
		    }
		} else {
		    // sum += glq.integrate(lastX, lastY, st, coords);
		    for (int i = 0; i < 6; i++) {
			coords2[i] = coords[i];
			if (i % 2 == 0) {
			    coords2[i] -= xc;
			}
		    }
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX-xc, coords2[0], coords2[2]):
			new BezierPolynomial(lastX-xc, coords2[0],
					     coords2[2], coords2[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY, coords2[1], coords2[3]):
			new BezierPolynomial(lastY, coords2[1],
					     coords2[3], coords2[5]);
		    BezierPolynomial dydu = by.deriv();
		    by.multiplyBy(bx);
		    by.multiplyBy(dydu);
		    double v = by.integralAt(1.0);
		    adder.add(v);
		    /*
		    adder.add(glqxyDy.integrate(lastX-xc, lastY, st, coords2));
		    */
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double clastX = lastX - xc;
	    double Ay = lastMoveToY - lastY;
	    double Ax = lastMoveToX - lastX;
	    if (Ay != 0.0 || Ax != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double v = Ay*Ax*Ay/3 + Ay*(Ay*clastX + Ax*lastY)/2
		    + Ay*clastX*lastY;
		adder.add(v);
	    }
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Compute the center of mass of a shape, assuming the area has
     * a uniform density.
     * <P>
     * Note: the algorithm used is fast, but will lose accuracy if
     * the area is very small compared to the area of the shape's
     * bounding box.
     * @param shape the shape
     * @return the center of mass
     */
    public static Point2D centerOfMassOf(Shape shape) {
	return centerOfMassOf(shape, null);
    }

    /**
     * Compute the center of mass of a shape, optionally modified by
     * an affine transformation assuming the area has
     * a uniform density.
     * <P>
     * Note: the algorithm used is fast, but will lose accuracy if
     * the area is very small compared to the area of the shape's
     * bounding box.
     * @param shape the shape
     * @param af the affine transformation
     * @return the center of mass
     */
    public static Point2D centerOfMassOf(Shape shape, AffineTransform af) {
	if (shape instanceof Area) {
	    double signedArea =
		integralxDyMinusyDx(shape.getPathIterator(af), false) / 2.0;
	    Rectangle2D bounds = shape.getBounds2D();
	    double x = bounds.getCenterX();
	    double y = bounds.getCenterY();
	    Point2D point = new Point2D.Double(x, y);
	    if (af != null) {
		af.transform(point,point);
		x = point.getX();
		y = point.getY();
	    }
	    if (signedArea == 0.0) {
		// degenerate case: the object is a closed curve that
		// does not enclose any area.
		// Fix this later.
		return null;
	    }

	    double xval = integralNegativeXyDx(shape.getPathIterator(af), y);
	    double yval = integralXyDy(shape.getPathIterator(af), x);
	    point.setLocation(xval/signedArea, yval/signedArea);
	    return point;
	} else {
	    return centerOfMassOf(new Area(shape), af);
	}
    }

    static final int N4FORM_X2Y = 6;
    static double[] u4form_x2y =
	GLQuadrature.getArguments(0.0, 1.0, N4FORM_X2Y);
    static UValues[] uv4form_x2y = new UValues[N4FORM_X2Y];
    static {
	for (int i = 0; i < N4FORM_XY; i++) {
	    uv4form_x2y[i] = new UValues(u4form_x2y[i]);
	    u4form_x2y[i] = (double) i;
	}
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = -x<sup>2</sup>ydx along a path.  For
     * a closed path, the generalized Stokes' theorem (which
     * reduces to Green's theorem in the 2-dimensional case)
     * implies that
     * <blockquote>
     * &int;<sub>&part;X</sub>&omega; = &int;<sub>X</sub> d&omega;
     * </blockquote>
     * or
     * <blockquote>
     * &int;<sub>&part;X</sub>(xdy - ydx) = 2&int;<sub>X</sub> dxdy
     * </blockquote>.
     */
    static class GLQ4FormX2ydx extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4FormX2ydx() {super(N4FORM_X2Y);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form_x2y,
				  new SegmentData(st, x0, y0, coords, null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form_x2y[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double dxdu = data.dxDu(uv);
	    // double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return -x*x*y*dxdu;
	}
    }
    private static GLQ4FormX2ydx glqx2yDx = new GLQ4FormX2ydx();

    /**
     * Integrate the differential form &omega; = -x<sup>2</sup>ydx along a path.
     * Useful for computing the center of mass of a shape.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralNegativeX2yDx(PathIterator pi,
						double xc, double yc)
    {
	double[] coords = new double[6];
	double[] coords2 = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double clastY = lastY - yc;
		    double clastX = lastX - xc;
		    double Ay = coords[1] - lastY;
		    double Ax = coords[0] - lastX;
		    if (Ay != 0.0 || Ax != 0.0) {
			double v = Ax*Ax*Ay/4
			    + (2*Ax*clastX*Ay + Ax*Ax*clastY)/3
			    + (clastX*clastX*Ay+2*Ax*clastX*clastY)/2
			    + clastX*clastX*clastY;
			v *= -Ax;
			adder.add(v);
			/*
			if (Math.abs((k1+k2)/2.0
				     - glq.integrate(lastX, lastY, st, coords))
			    > 1.0e-8) {
			    throw new RuntimeException("Exact integral failed");
			}
			*/
		    }
		} else {
		    for (int i = 0; i < 6; i++) {
			coords2[i] = coords[i];
			if (i % 2 == 0) {
			    coords2[i] -= xc;
			} else {
			    coords2[i] -= yc;
			}
		    }
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX-xc, coords2[0], coords2[2]):
			new BezierPolynomial(lastX-xc, coords2[0],
					     coords2[2], coords2[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY-yc, coords2[1], coords2[3]):
			new BezierPolynomial(lastY-yc, coords2[1],
					     coords2[3], coords2[5]);
		    BezierPolynomial dxdu = bx.deriv();
		    bx.multiplyBy(bx);
		    bx.multiplyBy(by);
		    bx.multiplyBy(dxdu);
		    double v = bx.integralAt(1.0);
		    if (v != 0.0) v = -v;
		    adder.add(v);
		    /*
		    adder.add(glqx2yDx.integrate(lastX-xc, lastY-yc,
						st, coords2));
		    */
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double clastX = lastX - yc;
	    double clastY = lastY - yc;
	    double Ay = lastMoveToY - lastY;
	    double Ax = lastMoveToX - lastX;
	    if (Ay != 0.0 || Ax != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double v = Ax*Ax*Ay/4 + (2*Ax*clastX*Ay + Ax*Ax*clastY)/3
		    + (clastX*clastX*Ay+2*Ax*clastX*clastY)/2
		    + clastX*clastX*clastY;
		v *= -Ax;
		adder.add(v);
	    }
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = y<sup>2</sup>xdy along a path.
     */
    static class GLQ4FormY2xdy extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4FormY2xdy() {super(N4FORM_X2Y);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form_x2y,
				  new SegmentData(st, x0, y0, coords, null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form_x2y[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    // double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return y*y*x*dydu;
	}
    }
    private static GLQ4FormY2xdy glqy2xDy = new GLQ4FormY2xdy();

    /**
     * Integrate the differential form &omega; = y<sup>2</sup>xdy along a path.
     * Useful for computing the center of mass of a shape.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralY2xDy(PathIterator pi,
					double xc, double yc)
    {
	double[] coords = new double[6];
	double[] coords2 = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double clastY = lastY - yc;
		    double clastX = lastX - xc;
		    double Ay = coords[1] - lastY;
		    double Ax = coords[0] - lastX;
		    if (Ay != 0.0 || Ax != 0.0) {
			double v = Ay*Ay*Ax/4
			    + (2*Ay*clastY*Ax + Ay*Ay*clastX)/3
			    + (clastY*clastY*Ax + 2*Ay*clastY*clastX)/2
			    + clastY*clastY*clastX;
			v *= Ay;
			/*
			System.out.format("(%g,%g)->(%g,%g)\n", clastX, clastY,
					  coords[0]-xc, coords[1]-yc);
			System.out.println("... adding " + v);
			*/
			adder.add(v);
		    }
		} else {
		    // sum += glq.integrate(lastX, lastY, st, coords);
		    for (int i = 0; i < 6; i++) {
			coords2[i] = coords[i];
			if (i % 2 == 0) {
			    coords2[i] -= xc;
			} else {
			    coords2[i] -= yc;
			}
		    }
		    /*
		    System.out.format("(%g,%g)->(%g,%g)->(%g,%g)->(%g,%g)\n",
				      lastX-xc, lastY-yc,
				      coords2[0], coords2[1],
				      coords2[2], coords2[3],
				      coords2[4], coords2[5]);
		    */
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX-xc, coords2[0], coords2[2]):
			new BezierPolynomial(lastX-xc, coords2[0],
					     coords2[2], coords2[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY-yc, coords2[1], coords2[3]):
			new BezierPolynomial(lastY-yc, coords2[1],
					     coords2[3], coords2[5]);
		    BezierPolynomial dydu = by.deriv();
		    by.multiplyBy(by);
		    by.multiplyBy(bx);
		    by.multiplyBy(dydu);
		    double v = by.integralAt(1.0);
		    /*
		    double v = glqy2xDy.integrate(lastX-xc, lastY-yc,
						  st, coords2);
		    */
		    // System.out.println("... adding " + v);
		    adder.add(v);
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double clastX = lastX - yc;
	    double clastY = lastY - yc;
	    double Ay = lastMoveToY - lastY;
	    double Ax = lastMoveToX - lastX;
	    if (Ay != 0.0 || Ax != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double v = Ay*Ay*Ax/4
		    + (2*Ay*clastY*Ax + Ay*Ay*clastX)/3
		    + (clastY*clastY*Ax + 2*Ay*clastY*clastX)/2
		    + clastY*clastY*clastX;
		v *= Ay;

		/*
		double v = Ay*Ax*Ax / 4
		    + (2*Ay*Ax*clastX + clastY*Ax*Ax) / 3
		    + (Ay*clastX*clastX + 2*clastY*Ax*clastX) / 2
		    + clastY*clastX*clastX;
		v *= Ay/2;
		*/
		// System.out.println("... adding [final]" + v);
		adder.add(v);
	    }
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Gaussian-Legendre Quadrature for integrating the
     * differential form &omega; = (1/2)yx<sup>2</sup>dy along a path.
     */
    static class GLQ4FormX2ydy extends GLQuadrature<SegmentData> {
	double x0;
	double y0;
	int st;
	double[] coords;

	GLQ4FormX2ydy() {super(N4FORM_X2Y);}
	/**
	 * Integrate the differential form over a path segment.
	 * @param x0 the value of x when u=0
	 * @param y0 the value of y when u=0
	 * @param st the segment type
	 * @param coords the control points (excluding the start of
	 *        a segment
	 */
	double integrate(double x0, double y0, int st, double[]coords) {
	    this.x0 = x0;
	    this.y0 = y0;
	    this.st = st;
	    this.coords = coords;
	    // u4form contains the values 0.0, 1.0., and 2.0.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(u4form_x2y,
				  new SegmentData(st, x0, y0, coords, null));
	}

	@Override
	protected double function(double iu, SegmentData data) {
	    UValues uv = uv4form_x2y[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    // double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    return 0.5*y*x*x*dydu;
	}
    }
    private static GLQ4FormX2ydy glqx2yDy = new GLQ4FormX2ydy();

    /**
     * Integrate the differential form &omega; = y<sup>2</sup>xdy along a path.
     * Useful for computing the center of mass of a shape.
     * @param pi the path iterator providing the path to follow
     * @param simpleLoop true if the path must be a single loop terminated by
     *        a {@link PathIterator#SEG_CLOSE} segment; false otherwise
     * @return the value of the integral
     */
    private static double integralX2yDy(PathIterator pi,
					double xc, double yc)
    {
	double[] coords = new double[6];
	double[] coords2 = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		if (st == PathIterator.SEG_CLOSE
		    || st == PathIterator.SEG_LINETO) {
		    // linear case - we'll save multiplications by computing
		    // the integral in closed form.
		    double clastY = lastY - yc;
		    double clastX = lastX - xc;
		    double Ay = coords[1] - lastY;
		    double Ax = coords[0] - lastX;
		    if (Ay != 0.0 || Ax != 0.0) {
			double v = Ay*Ax*Ax / 4
			    + (2*Ay*Ax*clastX + clastY*Ax*Ax) / 3
			    + (Ay*clastX*clastX + 2*clastY*Ax*clastX) / 2
			    + clastY*clastX*clastX;
			v *= Ay/2;
			adder.add(v);
			/*
			if (Math.abs((k1+k2)/2.0
				     - glq.integrate(lastX, lastY, st, coords))
			    > 1.0e-8) {
			    throw new RuntimeException("Exact integral failed");
			}
			*/
		    }
		} else {
		    // sum += glq.integrate(lastX, lastY, st, coords);
		    for (int i = 0; i < 6; i++) {
			coords2[i] = coords[i];
			if (i % 2 == 0) {
			    coords2[i] -= xc;
			} else {
			    coords2[i] -= yc;
			}
		    }
		    int degree = (st == PathIterator.SEG_QUADTO)? 2: 3;
		    BezierPolynomial bx = (degree == 2)?
			new BezierPolynomial(lastX-xc, coords2[0], coords2[2]):
			new BezierPolynomial(lastX-xc, coords2[0],
					     coords2[2], coords2[4]);
		    BezierPolynomial by = (degree  == 2)?
			new BezierPolynomial(lastY-yc, coords2[1], coords2[3]):
			new BezierPolynomial(lastY-yc, coords2[1],
					     coords2[3], coords2[5]);
		    BezierPolynomial dydu = by.deriv();
		    bx.multiplyBy(bx);
		    bx.multiplyBy(by);
		    bx.multiplyBy(dydu);
		    double v = 0.5 * bx.integralAt(1.0);
		    adder.add(v);
		    /*
		    adder.add(glqx2yDy.integrate(lastX-xc, lastY-yc,
						 st, coords2));
		    */
		}
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    double clastX = lastX - yc;
	    double clastY = lastY - yc;
	    double Ay = lastMoveToY - lastY;
	    double Ax = lastMoveToX - lastX;
	    if (Ay != 0.0 || Ax != 0.0) {
		// this is a linear case as before, so we can do the
		//integral in closed form.
		double v = Ay*Ax*Ax / 4
		    + (2*Ay*Ax*clastX + clastY*Ax*Ax) / 3
		    + (Ay*clastX*clastX + 2*clastY*Ax*clastX) / 2
		    + clastY*clastX*clastX;
		v *= Ay/2;
		adder.add(v);
	    }
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Get the moments about the center of mass for the area inside a shape.
     * <UL>
     *   <LI> moments[0][0] = (1/A) &int;x<sup>2</sup>dA.
     *   <LI> moments[0][1] = (1/A) &int;xydA.
     *   <LI> moments[1][0] = (1/A) &int;xydA.
     *   <LI> moments[1][1] = (1/A) &int;y<sup>2</sup>dA.
     * </UL>
     * <P>
     * Note: the moment of inertia tensor has different
     * values. Assuming that the area has a Z value of 0 and that the
     * density (mass per unit area) is 1.0/A, one obtains
     * <UL>
     *    <LI> I<sub>xx</sub> = moments[1][1].
     *    <LI> I<sub>xy</sub> = - moments[0][1].
     *    <LI> I<sub>xy</sub> = - moments[1][0].
     *    <LI> I<sub>yy</sub> = moments[0][0].
     *    <LI> I<sub>zz</sub> = moments[0][0] + moments[1][1]
     * </UL>
     * Any other component with a 'z' index is 0.
     * @param shape the shape
     * @return  a matrix containing the moments
     */
    public static double[][] momentsOf(Shape shape) {
	return momentsOf(shape, null);
    }

    /**
     * Get the moments for the area inside a shape modified by an
     * affine transformation and about the center of mass of the modified
     * shape.
     * <UL>
     *   <LI> moments[0][0] = (1/A) &int;x<sup>2</sup>dA.
     *   <LI> moments[0][1] = (1/A) &int;xydA.
     *   <LI> moments[1][0] = (1/A) &int;xydA.
     *   <LI> moments[1][1] = (1/A) &int;y<sup>2</sup>dA.
     * </UL>
     * <P>
     * Note: the moment of inertia tensor has different
     * values. Assuming that the area has a Z value of 0 and that the
     * density (mass per unit area) is 1.0, one obtains
     * <UL>
     *    <LI> I<sub>xx</sub> = moments[1][1].
     *    <LI> I<sub>xy</sub> = - moments[0][1].
     *    <LI> I<sub>xy</sub> = - moments[1][0].
     *    <LI> I<sub>yy</sub> = moments[0][0].
     *    <LI> I<sub>zz</sub> = moments[0][0] + moments[1][1]
     * </UL>
     * Any other component with a 'z' index is 0.
     * <P>
     * Note: the algorithm used is fast, but will lose accuracy if
     * the area is very small compared to the area of the shape's
     * bounding box.
     * @param shape the shape
     * @param af the affine transform
     * @return a matrix containing the moments
     */
    public static double[][] momentsOf(Shape shape, AffineTransform af) {
	if (shape instanceof Area) {
	    double[][] results = new double[2][2];
	    Point2D center = centerOfMassOf(shape, af);
	    double signedArea =
		integralxDyMinusyDx(shape.getPathIterator(af), false) / 2.0;

	    if (signedArea == 0.0) {
		// degenerate case: the object is a closed curve that
		// does not enclose any area.
		// Fix this later.
		return null;
	    }
	    double x2Moment = integralNegativeX2yDx(shape.getPathIterator(af),
						    center.getX(),
						    center.getY());
	    double y2Moment = integralY2xDy(shape.getPathIterator(af),
					    center.getX(), center.getY());
	    double xyMoment = integralX2yDy(shape.getPathIterator(af),
					    center.getX(), center.getY());
	    if (x2Moment != 0.0) x2Moment /= signedArea;
	    if (y2Moment != 0.0) y2Moment /= signedArea;
	    if (xyMoment != 0.0) xyMoment /= signedArea;
	    /*
	    if (signedArea < 0.0) {
		if (x2Moment != 0.0) x2Moment *= -1.0;
		if (y2Moment != 0.0) y2Moment *= -1.0;
		if (xyMoment != 0.0) xyMoment *= -1.0;
	    }
	    */
	    results[0][0] = x2Moment;
	    results[0][1] = xyMoment;
	    results[1][0] = xyMoment;
	    results[1][1] = y2Moment;
	    return results;
	} else {
	    return momentsOf(new Area(shape), af);
	}
    }

    /**
     * Get the moments for the area inside a shape about a reference point.
     * For a reference point (x<sub>r</sub>, y<sub>r</sub>)
     * <UL>
     *   <LI> moments[0][0] = (1/A) &int;(x-x<sub>r</sub>)<sup>2</sup>dA.
     *   <LI> moments[0][1] = (1/A) &int;(x-x<sub>r</sub>)(y-y<sub>r</sub>)dA.
     *   <LI> moments[1][0] = (1/A) &int;(x-x<sub>r</sub>)(y-y<sub>r</sub>)dA.
     *   <LI> moments[1][1] = (1/A) &int;(y-y<sub>r</sub>)<sup>2</sup>dA.
     * </UL>
     * <P>
     * Note: the moment of inertia tensor has different
     * values. Assuming that the area has a Z value of 0 and that the
     * density (mass per unit area) is 1.0, one obtains
     * <UL>
     *    <LI> I<sub>xx</sub> = moments[1][1].
     *    <LI> I<sub>xy</sub> = - moments[0][1].
     *    <LI> I<sub>xy</sub> = - moments[1][0].
     *    <LI> I<sub>yy</sub> = moments[0][0].
     *    <LI> I<sub>zz</sub> = moments[0][0] + moments[1][1]
     * </UL>
     * Any other component with a 'z' index is 0.
     * <P>
     * Note: the first argument is a point because
     * {@link #momentsOf(Shape,AffineTransform)} can have a null
     * second argument, so putting the point first prevents a compile-time
     * ambiguity for cases where the AffineTransform should be null.
     * <P>
     * Note: the algorithm used is fast, but will lose accuracy if
     * the area is very small compared to the area of the shape's
     * bounding box.
     * @param center the point about which the moments are computed
     * @param shape the shape
     * @return a matrix containing the moments
     */
    public static double[][] momentsOf(Point2D center, Shape shape) {
	if (shape instanceof Area) {
	    double[][] results = new double[2][2];
	    double signedArea =
		integralxDyMinusyDx(shape.getPathIterator(null), false) / 2.0;

	    if (signedArea == 0.0) {
		// degenerate case: the object is a closed curve that
		// does not enclose any area.
		// Fix this later.
		return null;
	    }
	    double x2Moment = integralNegativeX2yDx(shape.getPathIterator(null),
						    center.getX(),
						    center.getY());
	    double y2Moment = integralY2xDy(shape.getPathIterator(null),
					    center.getX(), center.getY());
	    double xyMoment = integralX2yDy(shape.getPathIterator(null),
					    center.getX(), center.getY());

	    if (x2Moment != 0.0) x2Moment /= signedArea;
	    if (y2Moment != 0.0) y2Moment /= signedArea;
	    if (xyMoment != 0.0) xyMoment /= signedArea;
	    /*
	    if (signedArea < 0.0) {
		if (x2Moment != 0.0) x2Moment *= -1.0;
		if (y2Moment != 0.0) y2Moment *= -1.0;
		if (xyMoment != 0.0) xyMoment *= -1.0;
	    }
	    */
	    results[0][0] = x2Moment;
	    results[0][1] = xyMoment;
	    results[1][0] = xyMoment;
	    results[1][1] = y2Moment;
	    return results;
	} else {
	    return momentsOf(center, new Area(shape));
	}
    }

    /**
     * Get the moments for the area inside a shape about a reference point
     * after applying an affine transform to both.
     * For a reference point (x<sub>r</sub>, y<sub>r</sub>)
     * <UL>
     *   <LI> moments[0][0] = (1/A) &int;(x-x<sub>r</sub>)<sup>2</sup>dA.
     *   <LI> moments[0][1] = (1/A) &int;(x-x<sub>r</sub>)(y-y<sub>r</sub>)dA.
     *   <LI> moments[1][0] = (1/A) &int;(x-x<sub>r</sub>)(y-y<sub>r</sub>)dA.
     *   <LI> moments[1][1] = (1/A) &int;(y-y<sub>r</sub>)<sup>2</sup>dA.
     * </UL>
     * <P>
     * Note: the moment of inertia tensor has different
     * values. Assuming that the area has a Z value of 0 and that the
     * density (mass per unit area) is 1.0, one obtains
     * <UL>
     *    <LI> I<sub>xx</sub> = moments[1][1].
     *    <LI> I<sub>xy</sub> = - moments[0][1].
     *    <LI> I<sub>xy</sub> = - moments[1][0].
     *    <LI> I<sub>yy</sub> = moments[0][0].
     *    <LI> I<sub>zz</sub> = moments[0][0] + moments[1][1]
     * </UL>
     * Any other component with a 'z' index is 0.
     * <P>
     * Note: the first argument is a point because
     * {@link #momentsOf(Shape,AffineTransform)} can have a null
     * second argument, so putting the point first prevents a compile-time
     * ambiguity for cases where the AffineTransform should be null.
     * <P>
     * Note: the algorithm used is fast, but will lose accuracy if
     * the area is very small compared to the area of the shape's
     * bounding box.
     * @param center the point about which the moments are computed
     * @param shape the shape
     * @param af the affine transform to apply to the shape and the
     *        center point
     * @return a matrix containing the moments
     */
    public static double[][] momentsOf(Point2D center, Shape shape,
				       AffineTransform af)
    {
	if (af == null) {
	    return momentsOf(center, shape);
	} if (shape instanceof Area) {
	    Area area = (Area) shape;
	    return momentsOf(af.transform(center, null),
			     area.createTransformedArea(af));
	} else {
	    return momentsOf(center, new Area(shape), af);
	}
    }

    /**
     * Convert moments to moments of inertia.
     * This conversion is for the case where the shape whose moments
     * were calculated lies in the X-Y plane. For a moments matrix M,
     * the value returned is the matrix
     * <BLOCKQUOTE><PRE>
     *     |  M<sub>11</sub>  -M<sub>01</sub>      0     |
     *     | -M<sub>10</sub>   M<sub>00</sub>      0     |.
     *     |   0     0   (M<sub>00</sub>+M<sub>11</sub>) |
     * </PRE></BLOCKQUOTE>
     * To compute the principal moments of inertia and their corresponding
     * axes, use {@link SurfaceOps#principalMoments(double[][])} and
     * {@link SurfaceOps#principalAxes(double[][])}.
     * @param moments the 2-by-2 moments matrix
     * @return the 3-by-3 moments of inertia matrix if the area enclosed
     *         has a uniform surface density with a total mass of 1
     */
    public static double[][] toMomentsOfInertia(double[][] moments) {
	double[][] results = new double[3][3];
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (i != j) {
		    results[i][j] = -moments[i][j];
		} else {
		    int ip1 = (i+1)%2;
		    results[i][i] = moments[ip1][ip1];
		}
	    }
	}
	results[2][2] = moments[0][0] + moments[1][1];
	return results;
    }

    /**
     * Find the principle moments of a shape given its moments.
     * The array returned contains the moments sorted so that the
     * smallest moment appears first.  The argument array can be
     * computed from a shape by calling {@link #momentsOf(Shape)} or
     * {@link #momentsOf(Shape, AffineTransform)}.
     * <P>
     * The principal moments are the eigenvalues of the moments
     * matrix. For the special case of a 2x2 matrix, an easily
     * computed solution can be found at
     * <A href="http://www.math.harvard.edu/archive/21b_fall_04/exhibits/2dmatrices/index.html">
     *this web page</A>.
     * @param moments the shape's moments.
     * @return the principal moments
     */
    public static double[] principalMoments(double[][] moments) {
	if (moments[0][1] != moments[1][0]) {
	    throw new IllegalArgumentException(errorMsg("badMoments"));
	}
	if (moments[0][1] == 0.0) {
	    double val1 = moments[0][0];
	    double val2 = moments[1][1];
	    double results[] = {Math.min(val1, val2), Math.max(val1, val2)};
	    return results;
	}

	double T = moments[0][0] + moments[1][1];
	double D = moments[0][0]*moments[1][1] - moments[1][0]*moments[0][1];
	double T2 = T/2;
	double root = Math.sqrt(T2*T2 - D);

	double L1 = T2 - root;
	double L2 = T2 + root;

	double results[] = {L1, L2};
	return results;
    }

    /**
     * Compute the principal axes corresponding a moments matrix.
     * The moments matrix can be computed from a shape by calling the
     * method {@link #momentsOf(Shape)} or by calling the method
     * {@link #momentsOf(Shape, AffineTransform)}. The method
     * {@link #principalMoments(double[][])} can be used to compute
     * the principal moments given a moments array.  The return value
     * for principalAxes is a two dimensional array.  If the array
     * returned is stored as pmatrix, then pmatrix[i] is an array
     * containing the principal axis corresponding to the
     * i<sup>th</sup> principal moment. Each principal axis vector
     * contains its X component followed by its Y component and
     * is normalized so its length is 1.0.
     * <P>
     * The principal axes are actually the eigenvectors of the moments
     * matrix provided as this method's first argument.  In addition,
     * the eigenvector at index 1 points counterclockwise from the one
     * at index 0, assuming the convention in which the positive Y
     * axis is counterclockwise from the positive X axis.
     * For the special case of a 2x2 matrix, an easily
     * computed solution to the eigenvalue problem can be found at
     * <A href="http://www.math.harvard.edu/archive/21b_fall_04/exhibits/2dmatrices/index.html">
     *this web page</A>.
     * @param moments the moments
     * @areturn an array of vectors, each providing an axis.
     */
    public static double[][] principalAxes(double[][] moments)
    {
	if (moments[0][1] != moments[1][0]) {
	    throw new IllegalArgumentException(errorMsg("badMoments"));
	}
	if (moments[0][1] == 0.0) {
	    double val1 = moments[0][0];
	    double val2 = moments[1][1];
	    double L1 = Math.min(val1, val2);
	    double L2 = Math.max(val1, val2);
	    if (moments[0][0] == L1) {
		double results[][] = {{1.0, 0.0}, {0.0, 1.0}};
		return results;
	    } else {
		double results[][] = {{0.0, -1.0}, {1.0, 0.0}};
		return results;
	    }
	} else {
	    double T = moments[0][0] + moments[1][1];
	    double D =
		moments[0][0]*moments[1][1] - moments[1][0]*moments[0][1];
	    double T2 = T/2;
	    double root = Math.sqrt(T2*T2 - D);
	    double L1 = T2 - root;
	    double L2 = T2 + root;
	    double[] v1 = {L1 - moments[1][1], moments[0][1]};
	    double[] v2 = {L2 - moments[1][1], moments[0][1]};
	    VectorOps.normalize(v1);
	    VectorOps.normalize(v2);
	    if ((v1[0]*v2[0] - v1[1]*v2[0]) < 0.0) {
		v2[0] = -v2[0];
		v2[1] = -v2[1];
	    }
	    double result[][] = {v1, v2};
	    return  result;
	}
    }


    /**
     * Compute the area of a shape.
     * Shapes will be converted to instance of {@link java.awt.geom.Area},
     * and as a result, shapes described by an open, continuous curve will
     * effectively be closed with a line connecting the curve's end points.
     * The area is that of the set of points within the boundary of the
     * shape. The boundary of the shape is determined by a
     * PathIterator that represents the boundary as a series of segments
     * that are either straight lines, quadratic B&eacute;zier curves, or
     * cubic B&eacute;zier curves. This is true for all shapes including
     * ones representing ellipses (circles are a special case) and arcs.
     * For these classes, the Java class library provides enough accuracy
     * so that the objects look right. For a circle of unit radius, the
     * area for an Ellipse2D differs from that of a true circle
     * by less than 3 parts in 10,000.
     * <P>
     * Accuracy is set by the floating-point representation - the
     * algorithm used is exact.  Time complexity is linear in the
     * number of path segments for the shape's path iterator when the
     * shape is an instance of Area. For any other class that implements
     * the Shape interface, the shape is first used to create an instance
     * of Area; however, the time complexity for this operation is not specified
     * in the Java documentation.
     * <P>
     * If the shape's path iterator has a winding rule of
     * {@link PathIterator#WIND_NON_ZERO}, and one counterclockwise
     * path component is inside another counterclockwise path
     * component, the conversion to {@link java.awt.geom.Area} will
     * result in an area consisting of the union of the two path
     * components, and the inner path component will not appear in
     * the area's iterator. When using this method, one should be
     * careful about how shapes are represented and how winding rules
     * affect the points that are considered to be inside or outside a
     * shape.
     * @param shape the shape
     * @return the area of the shape
     */
    public static double areaOf(Shape shape) {
	if (shape instanceof Area) {
	    return
		Math.abs(integralxDyMinusyDx(shape.getPathIterator(null),
					     false)/2.0);
	} else {
	    return areaOf(new Area(shape));
	}
    }

    /**
     * Compute the area of a shape modified by an affine transformation.
     * Shapes will be converted to instance of {@link java.awt.geom.Area},
     * and as a result, shapes described by an open, continuous curve will
     * effectively be closed with a line connecting the curve's end points.
     * The area is that of the set of points within the boundary of the
     * shape. The boundary of the shape is determined by a
     * PathIterator that represents the boundary as a series of segments
     * that are either straight lines, quadratic B&eacute;zier curves, or
     * cubic B&eacute;zier curves. This is true for all shapes including
     * ones representing ellipses (circles are a special case) and arcs.
     * For these classes, the Java class library provides enough accuracy
     * so that the objects look right. For a circle of unit radius, the
     * area for an Ellipse2D differs from that of a true circle
     * by less than 3 parts in 10,000.
     * <P>
     * Accuracy is set by the floating-point representation - the
     * algorithm used is exact.  Time complexity is linear in the
     * number of path segments for the shape's path iterator when the
     * shape is an instance of Area. For any other class that implements
     * the Shape interface, the shape is first used to create an instance
     * of Area; however the time complexity for this operation is not specified
     * in the Java documentation.
     * <P>
     * If the shape's path iterator has a winding rule of
     * {@link PathIterator#WIND_NON_ZERO}, and one counterclockwise
     * path component is inside another counterclockwise path
     * component, the conversion to {@link java.awt.geom.Area} will
     * result in an area consisting of the union of the two path
     * components, and the inner path component will not appear in
     * the area's iterator. When using this method, one should be
     * careful about how shapes are represented and how winding rules
     * affect the points that are considered to be inside or outside a
     * shape.
     * @param shape the shape
     * @param at the affine transform to modify the shape; null if none
     *        is to be used
     * @return the area of the shape
     */
    public static double areaOf(Shape shape, AffineTransform at) {
	if (shape instanceof Area) {
	    return
		Math.abs(integralxDyMinusyDx(shape.getPathIterator(at),
					     false)/2.0);
	} else {
	    return areaOf(new Area(shape), at);
	}
    }

    /**
     * Compute the area of a shape specified by a path iterator.
     * The winding rule for the path iterator must be
     * {@link java.awt.geom.PathIterator#WIND_NON_ZERO}. It is the
     * caller's responsibility to ensure the following:
     * <ul>
     *  <li> the winding rule for the path iterator is
     *       {@link java.awt.geom.PathIterator#WIND_NON_ZERO}.
     *  <li> each subpath is closed.
     *  <li> subpaths do not intersect.
     *  <li> The angular direction (clockwise versus counterclockwise) are
     *       opposite for holes than for outer boundaries.
     * </ul>
     * Methods that take a {@link java.awt.Shape} as an argument will
     * automatically ensure that these constraints are met (perhaps by
     * creating an Area).  This method is provided for cases where the
     * computation must be as fast as possible.  The path iterator will
     * be modified by this method.
     * <P>
     * Accuracy is set by the floating-point representation - the algorithm
     * used is exact.  Time complexity is linear in the number of path
     * segments.
     * @param pi a path iterator.
     * @return the area of the shape specified by the path iterator pi
     * @exception IllegalArgumentException the winding rule is not correct.
     */
    public static double areaOf(PathIterator pi)
	throws IllegalArgumentException
    {
	if (pi.getWindingRule() != PathIterator.WIND_NON_ZERO) {
	    throw new IllegalArgumentException("wrongWindingRule");
	}
	return Math.abs(integralxDyMinusyDx(pi, false)/2.0);
    }


    /**
     * Compute the area of a shape specified by a path iterator and affine
     * transformation.
     * <P>
     * The winding rule for the path iterator must be
     * {@link java.awt.geom.PathIterator#WIND_NON_ZERO}. It is the
     * caller's responsibility to ensure the following:
     * <ul>
     *  <li> the winding rule for the path iterator is
     *       {@link java.awt.geom.PathIterator#WIND_NON_ZERO}.
     *  <li> each subpath is closed.
     *  <li> subpaths do not intersect.
     *  <li> The angular direction (clockwise versus counterclockwise) are
     *       opposite for holes than for outer boundaries.
     * </ul>
     * Methods that take a {@link java.awt.Shape} as an argument will
     * automatically ensure that these constraints are met (perhaps by
     * creating an Area).  This method is provided for cases where the
     * computation must be as fast as possible.  The path iterator will
     * be modified by this method.
     * <P>
     * Accuracy is set by the floating-point representation - the algorithm
     * used is exact.  Time complexity is linear in the number of path
     * segments.
     * @param pi a path iterator.
     * @return the area of the shape specified by the path iterator pi
     * @exception IllegalArgumentException the winding rule is not correct.
     */
    public static double areaOf(PathIterator pi, AffineTransform at)
	throws IllegalArgumentException
    {
	pi = new TransformedPathIterator(pi, at);
	if (pi.getWindingRule() != PathIterator.WIND_NON_ZERO) {
	    throw new IllegalArgumentException("wrongWindingRule");
	}
	return Math.abs(integralxDyMinusyDx(pi, false)/2.0);
    }

    static double unsignedAreaOf(PathIterator pi) {
	return integralxDyMinusyDx(pi, false)/2.0;
    }

    /**
     * Compute the circumference of a shape.
     * The circumference is defined as the length of the boundary of a
     * shape (i.e., if the shape contains a hole, the circumference of the
     * hole is included). The boundary of the shape is determined by a
     * PathIterator that represents the boundary as a series of segments
     * that are either straight lines, quadratic B&eacute;zier curves, or
     * cubic B&eacute;zier curves. This is true for all shapes including
     * ones representing ellipses (circles are a special case) and arcs.
     * For these classes, the Java class library provides enough accuracy
     * so that the objects look right. For a circle of unit radius, the
     * circumference for an Ellipse2D differs from that of a true circle
     * by less than 2 parts in 10,000.
     * <P>
     * A Shape that is not an Area is first converted to an Area.
     * This will result in open, continuous paths being closed by adding a
     * straight line between the path's end points.
     * An Area is required to be described by non-overlapping, closed
     * paths using the winding rule WIND_NON_ZERO.  The documentation
     * for {@link java.awt.geom.Area} states that "The interiors of
     * the individual stored sub-paths are all non-empty and
     * non-overlapping. Paths are decomposed during construction into
     * separate component non-overlapping parts, empty pieces of the
     * path are discarded, and then these non-empty and
     * non-overlapping properties are maintained through all
     * subsequent constructive-area-geometry operations. Outlines of
     * different component sub-paths may touch each other, as long as
     * they do not cross so that their enclosed areas overlap." With
     * the double-precision version of various objects, tests produced
     * subpaths that touch each other at a single point, but not cases
     * in which a line segment was shared. If a single-precision
     * object was created, however, subpaths may have a curve in
     * common.  For example, if a path <code>p</code> consists of two
     * aligned rectangles with a gap between them of 0.0000000001, The
     * path <code>s</code> defined by
     * <blockquote><code>
     *      Path2D s = new Path2D.Float(new Area(p));
     * </code></blockquote>
     * will include two path segments, one from each rectangle, with
     * the same end points.  For such corner cases, the circumference
     * returned by this method is not what one would expect when viewing
     * a filled path.
     * @param shape the shape
     * @return the circumference
     */
    public static double circumferenceOf(Shape shape) {
	if (shape instanceof Area) {
	    return pathLength(shape);
	} else {
	    return circumferenceOf(new Area(shape));
	}
    }

    /**
     * Compute the circumference of a shape modified by an affine transform.
     * The circumference is defined as the length of the boundary of a
     * shape (i.e., if the shape contains a hole, the circumference of the
     * hole is included).  The boundary of the shape is determined by a
     * PathIterator that represents the boundary as a series of segments
     * that are either straight lines, quadratic B&eacute;zier curves, or
     * cubic B&eacute;zier curves. This is true for all shapes including
     * ones representing ellipses (circles are a special case) and arcs.
     * For these classes, the Java class library provides enough accuracy
     * so that the objects look right. For a circle of unit radius, the
     * circumference for an Ellipse2D differs from that of a true circle
     * by less than 2 parts in 10,000.
     * <P>
     * A Shape that is not an Area is first converted to an Area.
     * This will result in open, continuous paths being closed by adding a
     * straight line between the path's end points. An
     * Area is required to be described by non-overlapping, closed
     * paths using the winding rule WIND_NON_ZERO.  The documentation
     * for {@link java.awt.geom.Area} states that "The interiors of
     * the individual stored sub-paths are all non-empty and
     * non-overlapping. Paths are decomposed during construction into
     * separate component non-overlapping parts, empty pieces of the
     * path are discarded, and then these non-empty and
     * non-overlapping properties are maintained through all
     * subsequent constructive-area-geometry operations. Outlines of
     * different component sub-paths may touch each other, as long as
     * they do not cross so that their enclosed areas overlap." With
     * the double-precision version of various objects, tests produced
     * subpaths that touch each other at a single point, but not cases
     * in which a line segment was shared. If a single-precision
     * object was created, however, subpaths may have a curve in
     * common.  For example, if a path <code>p</code> consists of two
     * aligned rectangles with a gap between them of 0.0000000001, The
     * path <code>s</code> defined by
     * <blockquote><code>
     *      Path2D s = new Path2D.Float(new Area(p));
     * </code></blockquote>
     * will include two path segments, one from each rectangle, with
     * the same end points.  For such corner cases, the circumference
     * returned by this method is not what one would expect when viewing
     * a filled path.
     * @param shape the shape
     * @param at the affine transform; null if there is none
     * @return the circumference
     */
    public static double circumferenceOf(Shape shape, AffineTransform at) {
	if (shape instanceof Area) {
	    return pathLength(shape, at);
	} else {
	    return circumferenceOf(new Area(shape), at);
	}
    }

    /**
     * Print information about the segments that make up a path, or the
     * outline of a shape, to the standard output.
     * Entry i contains the X and Y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * <P>
     * @param s the shape or path
     */
    public static void printSegments(Shape s) {
	printSegments(System.out, s);
    }

    /**
     * Print information about the segments that make up a path or
     * an outline of a shape.
     * Entry i contains the X and Y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param appendable an Appendable on which to print
     * @param s the shape or path
     */
    public static void printSegments(Appendable appendable, Shape s) {
	printSegments(null, appendable, s);
    }
    /**
     * Print information about the segments that make up a path or
     * the outline of a shape, adding a prefix.
     * Entry i contains the X and Y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param prefix a prefix to print at the start of each line
     *        (null implies an empty string)
     * @param appendable the appendable for output
     * @param s the shape or path
     */
    public static void printSegments(String prefix, Appendable appendable,
				  Shape s)
    {
	if (prefix == null) prefix = "";
	Writer w;
	if (appendable instanceof Writer) {
	    w = (Writer) appendable;
	} else {
	 w = new AppendableWriter(appendable);
	}
	PrintWriter out;
	if (w instanceof PrintWriter) {
	    out = (PrintWriter) w;
	} else {
	    out = new PrintWriter(w);
	}
	int i = 0;
	for (Entry entry: getEntries(s)) {
	    out.println(prefix + "Entry " + i + ":");
	    int m = 0;
	    switch (entry.type) {
	    case PathIterator.SEG_MOVETO:
		out.println(prefix + "    type: SEG_MOVETO");
		m = 2;
		break;
	    case PathIterator.SEG_CUBICTO:
		out.println(prefix + "    type: SEG_CUBICTO");
		m = 6;
		break;
	    case PathIterator.SEG_LINETO:
		out.println(prefix + "    type: SEG_LINETO");
		m = 2;
		break;
	    case PathIterator.SEG_QUADTO:
		out.println(prefix + "    type: SEG_QUADTO");
		m = 4;
		break;
	    case PathIterator.SEG_CLOSE:
		out.println(prefix + "    type: SEG_CLOSE");
		m = 0;
		break;
	    default:
		System.out.println(prefix +"   [unknown mode]");
	    }
	    Point2D start = entry.getStart();
	    if (start == null) {
		out.println(prefix + "   startingX: none");
		out.println(prefix + "   startingY: none");
	    } else {
		out.println(prefix + "    startingX: " + start.getX());
		out.println(prefix + "    startingY: " + start.getY());
	    }
	    if (entry.type == PathIterator.SEG_CLOSE) {
		Point2D end = entry.getEnd();
		if (end != null) {
		    out.println(prefix + "    endingX: " + end.getX());
		    out.println(prefix + "    endingY: " + end.getY());
		}
	    }
	    for (int j = 0; j < m; j++) {
		out.println(prefix + "    coords[" + j + "]: "
			    + entry.coords[j]);
	    }
	    i++;
	}
	out.flush();
    }

    /**
     * Get a list of CPoint objects that can be used to configure
     * a spline-path builder.
     * The list can be modified by using the methods
     * {@link SplinePathBuilder#modifyCPoints(List,boolean,AffineTransform)}
     * or
     * {@link SplinePathBuilder#modifyCPoints(SplinePathBuilder.CPoint[],boolean,AffineTransform)}
     * if the modification consists of reversing the order of entries
     * in the list or applying an affine transformation to the control
     * points.
     * @param path the path
     * @return a list of CPoint objects specifying the control points
     *         for a path
     */
    public static List<SplinePathBuilder.CPoint> getCPoints(Path2D path)
    {
	LinkedList<SplinePathBuilder.CPoint> list = new LinkedList<>();
	PathIterator pi = path.getPathIterator(null);
	double[] coords = new double[6];
	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.MOVE_TO,
			  coords[0], coords[1]));
		break;
	    case PathIterator.SEG_LINETO:
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END,
			  coords[0], coords[1]));
		break;
	    case PathIterator.SEG_QUADTO:
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.CONTROL,
			  coords[0], coords[1]));
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END,
			  coords[2], coords[3]));
		break;
	    case PathIterator.SEG_CUBICTO:
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.CONTROL,
			  coords[0], coords[1]));
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.CONTROL,
			  coords[2], coords[3]));
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.SEG_END,
			  coords[4], coords[5]));
		break;
	    case PathIterator.SEG_CLOSE:
		list.add(new SplinePathBuilder.CPoint
			 (SplinePathBuilder.CPointType.CLOSE));
		break;
	    }
	    pi.next();
	}
	return list;
    }

    /**
     * Find the first closed component of a path that goes through a point
     * (x, y) and shift that path component so it starts at (x, y).
     * The point (x, y) must be the last point in a segment (including
     * a MOVE_TO segment).
     * @param path the path
     * @param x the X coordinate of a point on the path
     * @param y the Y coordinate of a point on the path
     * @return the patch component going through (x, y), with its
     *         segments shifted cyclically so that the returned path
     *         starts at the point (x, y).
     * @exception IllegalArgumentException
     */
    public static Path2D shiftClosedPath(Path2D path, double x, double y) {
	Path2D path1 = (path instanceof Path2D.Float)?
	    new Path2D.Float(): new Path2D.Double();
	Path2D path2 = (path instanceof Path2D.Float)?
	    new Path2D.Float(): new Path2D.Double();
	PathIterator pi = path.getPathIterator(null);
	double[] coords = new double[6];
	path = null;		// in case pi doesn't start with SEG_MOVETO
	double lastx = 0.0, lasty = 0.0;
	double xx = 0.0, yy = 0.0;
	boolean closed = false;
	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		path1.reset();
		path2.reset();
		path = path1;
		closed = false;
		xx = lastx = coords[0];
		yy = lasty = coords[1];
		if (xx == x && yy == y) {
		    path = path2;
		}
		path.moveTo(lastx, lasty);
		break;
	    case PathIterator.SEG_LINETO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[0];
		yy = coords[1];
		path.lineTo(coords[0],coords[1]);
		if (path != path2 && xx == x && yy == y) {
		    path = path2;
		    path.moveTo(x, y);
		}
		break;
	    case PathIterator.SEG_QUADTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[2];
		yy = coords[3];
		path.quadTo(coords[0], coords[1], coords[2], coords[3]);
		if (path != path2 && xx == x && yy == y) {
		    path = path2;
		    path.moveTo(x, y);
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[4];
		yy = coords[5];
		path.curveTo(coords[0], coords[1], coords[2], coords[3],
			     coords[4], coords[5]);
		if (path != path2 && xx == x && yy == y) {
		    path = path2;
		    path.moveTo(x, y);
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (closed) break;
		if (xx != lastx || yy != lasty) {
		    path.lineTo(lastx, lasty);
		}
		if (path == path2) {
		    path.append(path1, true);
		    path.closePath();
		    return path;
		}
		closed = true;
		xx = lastx;
		yy = lasty;
		break;
	    }
	    pi.next();
	}
	// We didn't find a match to (x, y)
	return null;
    }

    /*
    public static int numberOfSegments(Path2D path) {
	PathIterator pi = path.getPathIterator(null);
	int count = 0;
	while (!isDone()) {
	    count++;
	    next();
	}
	return count;
    }
    */

    /**
     * Count the number of segments in the  first continuous portion of a
     * path that are Drawable.
     * Drawable segments exclude PathIterator.SEG_MOVETO segments and
     * PathIterator.SEG_CLOSE segments whose current point is the same as
     * that of a previous PathIterator.SEG_MOVETO segment.
     * The test ignores any segments after a second PathIterator.SEG_MOVE
     * or a first PathIterator.SEG_CLOSE.  If the last point is the segment
     * preceding a PathIterator.SEG_CLOSE segment is equal to t he
     * initial segment (whose type is first PathIterator.SEG_MOVETO),
     * the terminating PathIterator.SEG_CLOSE segment is not included in
     * the count.
     * @param path the path
     * @return the number of drawable segments
     * @throws IllegalStateException if the path does not start with a
     *         segment whose type is PathIterator.SEG_MOVE.
     */
    public static int numberOfDrawableSegments(Path2D path) {
	double[] tmp = new double[6];
	PathIterator pi = path.getPathIterator(null);
	int count = 0;
	double startx = 0.0, starty = 0.0;
	double lastx = 0.0, lasty = 0.0;
	if (!pi.isDone()) {
	    // count++;
	    if (pi.currentSegment(tmp) == PathIterator.SEG_MOVETO) {
		lastx = tmp[0]; lasty = tmp[1];
		startx = lastx; starty = lasty;
	    } else {
		throw new IllegalStateException(errorMsg("expectingMoveTo"));
	    }
	    pi.next();
	}
	while (!pi.isDone()) {
	    switch(pi.currentSegment(tmp)) {
	    case PathIterator.SEG_MOVETO:
		lastx = tmp[0];
		lasty = tmp[1];
		return count;
	    case PathIterator.SEG_LINETO:
		lastx = tmp[0];
		lasty = tmp[1];
		break;
	    case PathIterator.SEG_QUADTO:
		lastx = tmp[2];
		lasty = tmp[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		lastx = tmp[4];
		lasty = tmp[5];
		break;
	    case PathIterator.SEG_CLOSE:
		startx = (double)(float)startx;
		starty = (double)(float)starty;
		lastx = (double)(float)lastx;
		lasty = (double)(float)lasty;
		if (lastx != startx || lasty != starty) count++;
		return count;
	    }
	    count++;
	    pi.next();
	}
	return count;
    }

    static double[] tmp = new double[6];
    /**
     * Determine if the first continuous portion of a path is closed.
     * The test ignores any segments
     * @param path the path
     * @return true if the path is closed; false otherwise
     */
    public static boolean isClosed(Path2D  path) {
	PathIterator pi = path.getPathIterator(null);
	if (!pi.isDone()) {
	    pi.next();
	}
	while (!pi.isDone()) {
	    int mode = pi.currentSegment(tmp);
	    if (mode == PathIterator.SEG_MOVETO) return false;
	    if (mode == PathIterator.SEG_CLOSE) return true;
	    pi.next();
	}
	return false;
    }

    /**
     * Get the tangent vector for the start of a path.
     * @param path the path
     * @return the tangent vector; null if the path's first component
     *         is a closed path
     * @exception IllegalArgumentException if the path did not start
     *     with a {@link PathIterator#SEG_MOVETO} segment or if the
     *     path had only {@link PathIterator#SEG_MOVETO} segments
     *     before a {@link PathIterator#SEG_CLOSE} segment
     */
    public static double[] firstTangent(Path2D path)
	throws IllegalArgumentException
    {
	PathIterator pi = path.getPathIterator(null);
	double[] tangent = null;
	double[] coords = new double[6];
	if (!pi.isDone()) {
	    if (pi.currentSegment(coords) != PathIterator.SEG_MOVETO) {
		throw new
		    IllegalArgumentException(errorMsg("missingSEGMOVETO"));
	    }
	    tangent = new double[2];
	    tangent[0] = -coords[0];
	    tangent[1] = -coords[1];
	    pi.next();
	}
	boolean done = false;
	while (!pi.isDone()) {
	    switch (pi.currentSegment(coords)) {
	   case  PathIterator.SEG_MOVETO:
		tangent[0] = -coords[0];
		tangent[1] = -coords[1];
		break;
	    case PathIterator.SEG_CLOSE:
		throw new
		    IllegalArgumentException(errorMsg("noSegsBeforeClose"));
	    default:
		tangent[0] += coords[0];
		tangent[1] += coords[1];
		done = true;
		break;
	    }
	    pi.next();
	    if (done) break;
	}
	while (!pi.isDone()) {
	    int mode = pi.currentSegment(tmp);
	    if (mode == PathIterator.SEG_MOVETO) {
		try {
		    VectorOps.normalize(tangent);
		} catch (IllegalArgumentException e) {}
		return tangent;
	    }
	    if (mode == PathIterator.SEG_CLOSE) return null;
	    pi.next();
	}
	try {
	    VectorOps.normalize(tangent);
	} catch (IllegalArgumentException e) {}
	return tangent;
    }

    /**
     * Get the tangent vector for the end of a path.
     * The tangent vector, if it exists and has a non-zero length,
     * will have unit length.
     * @param path the path
     * @return the tangent vector; null if the last component of the
     *         path is a closed path
     * @exception IllegalArgumentException if the path did not start
     *     with a {@link PathIterator#SEG_MOVETO} segment, if the
     *     path ends with a {@link PathIterator#SEG_MOVETO} segment,
     *     or if the segment type is not recognized
     */
    public static double[] lastTangent(Path2D path) {
	PathIterator pi = path.getPathIterator(null);
	double[] tangent = new double[2];
	double[] coords = new double[6];
	double[] pt = new double[2];
	double lastX = 0, lastY = 0;
	if (!pi.isDone()) {
	    if (pi.currentSegment(coords) != PathIterator.SEG_MOVETO) {
		throw new
		    IllegalArgumentException(errorMsg("missingSEGMOVETO"));
	    }
	    lastX = coords[0];
	    lastY = coords[1];
	    pt[0] = lastX;
	    pt[1] = lastY;
	    pi.next();
	}
	int mode = PathIterator.SEG_MOVETO;
	while (!pi.isDone()) {
	    mode = pi.currentSegment(coords);
	    switch (mode) {
	    case PathIterator.SEG_MOVETO:
		lastX = coords[0];
		lastY = coords[1];
		pt[0] = lastX;
		pt[1] = lastY;
		break;
	    case PathIterator.SEG_LINETO:
		tangent[0] = coords[0] - pt[0];
		tangent[1] = coords[1] - pt[1];
		pt[0] = coords[0];
		pt[1] = coords[1];
		break;
	    case PathIterator.SEG_QUADTO:
		tangent[0] = coords[2] - coords[0];
		tangent[1] = coords[3] - coords[1];
		pt[0] = coords[2];
		pt[1] = coords[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		tangent[0] = coords[4] - coords[2];
		tangent[1] = coords[5] - coords[3];
		pt[0] = coords[4];
		pt[1] = coords[5];
		break;
	    case PathIterator.SEG_CLOSE:
		pt[0] = lastX;
		pt[1] = lastY;
		break;
	    default:
		String msg = errorMsg("unknownSegmentType", mode);
		throw new IllegalArgumentException(msg);
	    }
	    pi.next();
	}
	if (mode == PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException("endingMOVETO");
	}
	try {
	    return (mode == PathIterator.SEG_CLOSE)? null:
		VectorOps.normalize(tangent);
	} catch (IllegalArgumentException e) {
	    return tangent;
	}
    }

    /**
     * Get the normal vector for the start of a path.
     * The normal vector, if it exists and has a non-zero length,
     * will have unit length.
     * This vector is perpendicular to the tangent vector and
     * is equal to a vector that results from a 90 degree
     * counterclockwise rotation of the tangent vector.
     * @param path the path
     * @return the normal vector; null if the path's first component
     *         is a closed path
     * @exception IllegalArgumentException if the path did not start
     *     with a {@link PathIterator#SEG_MOVETO} segment or if the
     *     path had only {@link PathIterator#SEG_MOVETO} segments
     *     before a {@link PathIterator#SEG_CLOSE} segment
     */
    public static double[] firstNormal(Path2D path)
	throws IllegalArgumentException
    {
	double[] result = firstTangent(path);
	if (result != null) {
	    double tmp = result[0];
	    result[0] = - result[1];
	    result[1] = tmp;
	}
	return result;
    }

    /**
     * Get the normal vector for the end of a path.
     * The normal vector, if it exists and has a non-zero length,
     * will have unit length.
     * This vector is perpendicular to the tangent vector and
     * is equal to a vector that results from a 90 degree
     * counterclockwise rotation of the tangent vector.
     * @param path the path
     * @return the normal vector; null if the last component of the
     *         path is a closed path
     * @exception IllegalArgumentException if the path did not start
     *     with a {@link PathIterator#SEG_MOVETO} segment, if the
     *     path ends with a {@link PathIterator#SEG_MOVETO} segment,
     *     or if the segment type is not recognized
     */
    public static double[] lastNormal(Path2D path) {
	double[] result = lastTangent(path);
	if (result != null) {
	    double tmp = result[0];
	    result[0] = - result[1];
	    result[1] = tmp;
	}
	return result;
    }
}

//  LocalWords:  exbundle af eacute zier href coords rOffset cOffset
//  LocalWords:  SegmentData IllegalArgumentException argOutOfRange
//  LocalWords:  ul li PathIterator SEG MOVETO LINETO QUADTO CUBICTO
//  LocalWords:  currentSegment ArrayIndexOutOfBoundsException uv NaN
//  LocalWords:  NullPointerException stUnknown arg OutOfRange dx du
//  LocalWords:  piUnknown ds dy deriv uuderiv uu dxdu dydu DInfo lt
//  LocalWords:  getEntries getSegmentLength indices sqrt glq len th
//  LocalWords:  integrateWithP segNumbOutOfRange lastMoveToX lastX
//  LocalWords:  lastMoveToY lastY GLQuadrature dsDu segmentType xdy
//  LocalWords:  illFormedPath tradeoff UValues precompute ydx dxdy
//  LocalWords:  blockquote getX getY dxDu dyDu AWT zeroAreaPath PRE
//  LocalWords:  simpleLoop notSimpleClosedPath RuntimeException XY
//  LocalWords:  fullEntry subpath subpaths wrongWindingRule endingX
//  LocalWords:  appendable Appendable startingX startingY endingY xy
//  LocalWords:  openjdk linesIntersect affine uvxy xp nullArg CPoint
//  LocalWords:  getCurrentPoint piSEGMOVETO tangentLength boolean de
//  LocalWords:  tangentOffset precomputed SplinePathBuilder xindex
//  LocalWords:  modifyCPoints AffineTransform ROUNDOFF errorMsg tmp
//  LocalWords:  badSegClose Casteljau's ge Akl Toussaint Aki bp tx
//  LocalWords:  psuedocode fourthArgNeg argarray shiftClosedPath ty
//  LocalWords:  PathSplitter getDegree getCoefficientsArray nroots
//  LocalWords:  umin minValue roundoff xydx yc xydy glqy xDy xc Ay
//  LocalWords:  clastX clastY yx dA xydA yy momentsOf pmatrix zz
//  LocalWords:  principalMoments signedArea xyMoment SurfaceOps
//  LocalWords:  principalAxes numberOfSegments getPathIterator
//  LocalWords:  isDone Drawable IllegalStateException badMoments
//  LocalWords:  expectingMoveTo drawable missingSEGMOVETO
//  LocalWords:  noSegsBeforeClose unknownSegmentType endingMOVETO
