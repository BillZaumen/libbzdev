package org.bzdev.geom;

import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.Writer;
import org.bzdev.io.AppendableWriter;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.ArrayMerger;

import org.bzdev.math.CubicSpline;
import org.bzdev.math.GLQuadrature;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;
import org.bzdev.math.VectorOps;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class providing static methods to obtain information about paths
 * defined by java.awt.geom.Path3D.
 * <P>
 * The method {@link Path3DInfo#shiftClosedPath(Path3D,double,double,double)}
 * returns a modified path (the same path as its argument but with its
 * starting point shifted).  The class {@link PathSplitter} will
 * allow various paths to be constructed.
 */
public class Path3DInfo {

    // just static
    private Path3DInfo() {}

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static class MergedIterator implements PathIterator3D {
	int index = 0;
	double[] pcoords;

	MergedIterator(double[]coords) {
	    pcoords = coords;
	}

	@Override
	public void next() {
	    if (index == 0) index = 3;
	    index += 9;
	}

	@Override
	public boolean isDone() {
	    return index >= pcoords.length;
	}

	@Override
	public int currentSegment(double[] coords) {
	    int len = (index == 0)? 3: 9;
	    System.arraycopy(pcoords, index, coords, 0, len);
	    return (index == 0)? PathIterator3D.SEG_MOVETO:
		PathIterator3D.SEG_CUBICTO;
	}

	@Override
	public int currentSegment(float[] coords) {
	    int len = (index == 0)? 3: 9;
	    for (int i = 0; i < len; i++) {
		coords[i] = (float) pcoords[index+i];
	    }
	    return (index == 0)? PathIterator3D.SEG_MOVETO:
		PathIterator3D.SEG_CUBICTO;
	}
    }

    /**
     * List the control points of a path.
     * @param path the path
     * @param all true if all control points are included; false if
     *        only the control points starting or ending a segment are
     *        included
     * @return an array containing the countrol points, each as
     *         a triplet of values where each triplet consists of
     *         a control point's X coordinate, followed by its Y
     *         coordinate, followed by its Z coordinate
     */
    public static double[] getControlPoints(Path3D path, boolean all)
    {
	return getControlPoints(path.getPathIterator(null), all);
    }

    /**
     * List the control points of a path provided by a path iterator
     * @param pit the path iterator providing the control points
     * @param all true if all control points are included; false if
     *        only the control points starting or ending a segment are
     *        included
     * @return an array containing the countrol points, each as
     *         a triplet of values where each triplet consists of
     *         a control point's X coordinate, followed by its Y
     *         coordinate, followed by its Z coordinate
     */
    public static double[] getControlPoints(PathIterator3D pit,
					    boolean all)
    {
	ArrayList<Double> list = new ArrayList<>();
	double[] coords = new double[9];
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	boolean sawClose = false;
	while(!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		startx = coords[0];
		starty = coords[1];
		startz = coords[2];
	    case PathIterator.SEG_LINETO:
		list.add(coords[0]);
		list.add(coords[1]);
		list.add(coords[2]);
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[3];
		sawClose = false;
		break;
	    case PathIterator.SEG_QUADTO:
		if (all) {
		    list.add(coords[0]);
		    list.add(coords[1]);
		    list.add(coords[2]);
		}
		list.add(coords[3]);
		list.add(coords[4]);
		list.add(coords[5]);
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		sawClose = false;
		break;
	    case PathIterator.SEG_CUBICTO:
		if (all) {
		    list.add(coords[0]);
		    list.add(coords[1]);
		    list.add(coords[2]);
		    list.add(coords[3]);
		    list.add(coords[4]);
		    list.add(coords[5]);
		}
		list.add(coords[6]);
		list.add(coords[7]);
		list.add(coords[8]);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		sawClose = false;
		break;
	    case PathIterator.SEG_CLOSE:
		if (sawClose == false && lastx == startx && lasty == starty
		    && lastz == startz) {
		    int sz = list.size();
		    list.remove(sz-1);
		    list.remove(sz-2);
		    list.remove(sz-3);
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
     * Get a 3D path iterator for the path implied by three cubic splines.
     * The splines must have the same number of knots.
     * @param af the affine transform to apply to the path iterator;
     *        null is equivalent to an identity transform
     * @param x the cubic spline for the X coordinates
     * @param y the cubic spline for the Y coordinates
     * @param z the cubic spline for the Z coordinates
     */
    public static PathIterator3D getPathIterator(AffineTransform3D af,
						 CubicSpline x,
						 CubicSpline y,
						 CubicSpline z)
    {
	double[] coords = ArrayMerger.merge(x.getBernsteinCoefficients(),
					    y.getBernsteinCoefficients(),
					    z.getBernsteinCoefficients());
	int n = coords.length/3;
	if (af != null) {
	    af.transform(coords, 0, coords, 0, n);
	}
	return new MergedIterator(coords);
    }

    /**
     * Elevate the degree of a three-dimensional B&eacute;zier curve by 1.
     * The algorithm is describe in
     * <A href="https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-elev.html">Degree Elevation of a B&eacute;zier Curve</A>.
     * The length of the coords array must be at least 3*(degree+1), whereas
     * the length of the result array must be at least 3*(degree+2).
     * <P>
     * Control points are stored so that the x, y, and z coordinates
     * for control point i/3 are in array locations i, i+1, and i+2
     * where i = 0 (mod 3).
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
	int n = 3*degree;
	result[rOffset] = coords[cOffset];
	result[rOffset+1] = coords[cOffset+1];
	result[rOffset+2] = coords[cOffset+2];
	result[rOffset+n+3] = coords[cOffset+n];
	result[rOffset+n+4] = coords[cOffset+n+1];
	result[rOffset+n+5] = coords[cOffset+n+2];

	double ratio = 1.0/(degree+1);
	n += 3;

	for (int i = 3; i < n; i++) {
	    double term1 = (i/3)*ratio;
	    double term2 = 1.0 - term1;
	    int ii = i + cOffset;
	    result[i+rOffset] = coords[ii-3]*term1
		+ coords[ii]*term2;
	}
    }

    /**
     * Elevate the degree of a three-dimensional B&eacute;zier curve of
     * degree n by 1,
     * specifying the last n control points in an array.
     * The length of the result array must be at least 3*(n+1), whereas the
     * length of the coords array must be at least 3*n.
     * The algorithm is describe in
     * <A href="https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-elev.html">Degree Elevation of a B&eacute;zier Curve</A>.
     * <P>
     * Control points are stored so that the x, y, and z coordinates
     * for control point (i/3)+1 are in array locations i, i+1, and i+2
     * respectively, where i = 0 (mod 3).
     * @param degree the degree of the curve
     * @param result an array storing the last n control points for an
     *        the new curve, which excludes its starting point
     * @param x the X coordinate for the start of the curve (control point 0)
     * @param y the Y coordinate for the start of the curve (control point 0)
     * @param z the Z coordinate for the start of the curve (control point 0)
     * @param coords the array containing the last n-1 control points for the
     *        original curve, which excludes its starting point
     */
    public static void elevateDegree(int degree, double[] result,
				     double x, double y, double z,
				     double[] coords)
    {
	int n = 3*degree;
	result[n] = coords[n-3];
	result[n+1] = coords[n-2];
	result[n+2] = coords[n-1];

	double ratio = 1.0/(degree+1);
	double term1 = ratio;
	double term2 = 1.0 - term1;
	result[0] = x*term1 + coords[0]*term2;
	result[1] = y*term1 + coords[1]*term2;
	result[2] = z*term1 + coords[2]*term2;

	for (int i = 3; i < n; i++) {
	    term1 = ((i+3)/3)*ratio;
	    term2 = 1.0 - term1;
	    result[i] = coords[i-3]*term1 + coords[i]*term2;
	}
    }


    /**
     * Class to represent a value of the path parameter u.
     * This class caches the parameter u and some related values
     * for use with methods defined for the {@link SegmentData} class.
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
	 *      <li> {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *      <li> {@link PathIterator3D#SEG_LINETO PathIterator3D.SEG_LINETO}
	 *      <li> {@link PathIterator3D#SEG_QUADTO PathIterator3D.SEG_QUADTO}
	 *      <li> {@link PathIterator3D#SEG_CUBICTO PathIterator3D.SEG_CUBICTO}
	 *      <li> {@link PathIterator3D#SEG_CLOSE PathIterator3D.SEG_CLOSE}
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
	    case PathIterator3D.SEG_MOVETO:
	    case PathIterator3D.SEG_LINETO:
	    case PathIterator3D.SEG_CLOSE:
		u1 = 1.0 - u;
		break;
	    case PathIterator3D.SEG_QUADTO:
		u1 = 1.0 - u;
		uu = u*u;
		u1u = u*u1;
		u1u1 = u1*u1;
		break;
	    case PathIterator3D.SEG_CUBICTO:
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

	@Override
	public boolean equals(Object other) {
	    return (other instanceof UValues)
		&& (u == ((UValues)other).u);
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
	double z0;
	double c0;
	double c1;
	double c2;
	double c3;
	double c4;
	double c5;
	double c6;
	double c7;
	double c8;
	/*
	double c0x0;
	double c2c0;
	double c4c2;
	double c1y0;
	double c3c1;
	double c5c3;
	*/
	// linear, quad, and cubic
	double c0x0;
	double c1y0;
	double c2z0;
	// quad and cubic
	double c3c0;
	double c4c1;
	double c5c2;
	// cubic only
	double c6c3;
	double c7c4;
	double c8c5;

	SegmentData last = null;

	/**
	 * Constructor.
	 * @param st the path-segment type, legal values of which
	 *    are
	 *    <ul>
	 *      <li> {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *      <li> {@link PathIterator3D#SEG_LINETO PathIterator3D.SEG_LINETO}
	 *      <li> {@link PathIterator3D#SEG_QUADTO PathIterator3D.SEG_QUADTO}
	 *      <li> {@link PathIterator3D#SEG_CUBICTO PathIterator3D.SEG_CUBICTO}
	 *      <li> {@link PathIterator3D#SEG_CLOSE PathIterator3D.SEG_CLOSE}
	 *    </ul>
	 * @param x0 the initial x coordinate for the segment (ignored for
	 *        the
	 *        {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *        case)
	 * @param y0 the initial y coordinate for the segment (ignored for
	 *        the
	 *        {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *        case)
	 * @param z0 the initial y coordinate for the segment (ignored for
	 *        the
	 *        {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *        case)
	 * @param coords the coordinates array set by a call to
	 *        {@link PathIterator3D#currentSegment(double[])}, but with the
	 *        first three elements set to the x, y, and z coordinates
	 *        respectively for the previous
	 *        {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
	 *        segment when first argument's value is
	 *        {@link PathIterator3D#SEG_CLOSE}. The size of this array
	 *        should be at least 3 and the maximum size needed is 9.
	 * @param last the previous SegmentData for a path; null if there
	 *        is none
	 * @see PathIterator
	 * @exception IllegalArgumentException a value was out of range
	 * @exception ArrayIndexOutOfBoundsException the array argument was
	 *            two small
	 * @exception NullPointerException the array argument was null
	 */
	public SegmentData(int st, double x0, double y0, double z0,
			   double[] coords, SegmentData last)
	    throws IllegalArgumentException
	{
	    this.st = st;
	    c0 = coords[0];
	    c1 = coords[1];
	    c2 = coords[2];

	    if (st == PathIterator3D.SEG_MOVETO) return;

	    this.x0 = x0;
	    this.y0 = y0;
	    this.z0 = z0;

	    c0x0 = c0 - x0;
	    c1y0 = c1 - y0;
	    c2z0 = c2 - z0;
	    if (st == PathIterator3D.SEG_LINETO || st == PathIterator3D.SEG_CLOSE) {
		return;
	    }
	    // c2c0 = c2 - c0;
	    // c3c1 = c3 - c1;
	    c3 = coords[3];
	    c4 = coords[4];
	    c5 = coords[5];
	    c3c0 = c3 - c0;
	    c4c1 = c4 - c1;
	    c5c2 = c5 - c2;
	    if (st == PathIterator3D.SEG_CUBICTO) {
		// c4 = coords[4];
		// c5 = coords[5];
		// c4c2 = c4 - c2;
		// c5c3 = c5 - c3;
		c6 = coords[6];
		c7 = coords[7];
		c8 = coords[8];
		c6c3 = c6 - c3;
		c7c4 = c7 - c4;
		c8c5 = c8 - c5;
	    }
	    this.last = last;
	}

	/**
	 * Compute the x coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the x coordinate
	 */
	public double getX(UValues uv) {
	    switch (st) {
	    case PathIterator3D.SEG_MOVETO:
		return c0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c0*uv.u + x0*uv.u1;
	    case PathIterator3D.SEG_QUADTO:
		return uv.u1u1*x0 + 2.0 * uv.u1u*c0 + uv.uu*c3;
	    case PathIterator3D.SEG_CUBICTO:
		return uv.u1u1u1*x0 + 3.0 *(uv.u1u1u*c0 + uv.u1uu*c3)
		    + uv.uuu*c6;
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the x coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the y coordinate
	 */
	public double getY(UValues uv) {
	    switch (st) {
	    case PathIterator3D.SEG_MOVETO:
		return c1;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c1*uv.u + y0*uv.u1;
	    case PathIterator3D.SEG_QUADTO:
		return uv.u1u1*y0 + 2.0 * uv.u1u*c1 + uv.uu*c4;
	    case PathIterator3D.SEG_CUBICTO:
		return uv.u1u1u1*y0 + 3.0 * (uv.u1u1u*c1 + uv.u1uu*c4)
		    + uv.uuu*c7;
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}


	/**
	 * Compute the x coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the y coordinate
	 */
	public double getZ(UValues uv) {
	    switch (st) {
	    case PathIterator3D.SEG_MOVETO:
		return c2;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c2*uv.u + z0*uv.u1;
	    case PathIterator3D.SEG_QUADTO:
		return uv.u1u1*z0 + 2.0 * uv.u1u*c2 + uv.uu*c5;
	    case PathIterator3D.SEG_CUBICTO:
		return uv.u1u1u1*z0 + 3.0 * (uv.u1u1u*c2 + uv.u1uu*c5)
		    + uv.uuu*c8;
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}



	/**
	 * Compute the derivative with respect to the path parameter of the
	 *  x coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the derivative of the x coordinate with respect to the
	 *         path parameter
	 */
	public double dxDu(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c0x0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 * (uv.u1*c0x0 + uv.u*c3c0);
	    case PathIterator3D.SEG_CUBICTO:
		return 3.0 * (uv.u1u1*c0x0 + 2.0*uv.u1u*c3c0 + uv.uu*c6c3);
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 *  y coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double dyDu(UValues uv) {
	    switch (st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c1y0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 * (uv.u1*c1y0 + uv.u*c4c1);
	    case PathIterator3D.SEG_CUBICTO:
		return 3.0 * (uv.u1u1*c1y0 + 2.0*uv.u1u*c4c1 + uv.uu*c7c4);
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 * z coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double dzDu(UValues uv) {
	    switch (st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return c2z0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 * (uv.u1*c2z0 + uv.u*c5c2);
	    case PathIterator3D.SEG_CUBICTO:
		return 3.0 * (uv.u1u1*c2z0 + 2.0*uv.u1u*c5c2 + uv.uu*c8c5);
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the x coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the second derivative of the x coordinate with respect to the
	 *         path parameter
	 */
	public double d2xDu2(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 * (c3c0 - c0x0);
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (uv.u1 *(c3c0 - c0x0) + uv.u *(c6c3 - c3c0));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the y coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the second derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double d2yDu2(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 *(c4c1 - c1y0);
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (uv.u1*(c4c1 - c1y0) + uv.u *(c7c4 - c4c1));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the z coordinate of a point on the path.
	 * @param uv the path parameter
	 * @return the second derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double d2zDu2(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 2.0 *(c5c2 - c2z0);
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (uv.u1*(c5c2 - c2z0) + uv.u *(c8c5 - c5c2));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the third derivative with respect to the path parameter
	 * of the x coordinate of a point on the path.
	 * <P>
	 * Note: the value is 0.0 except for cubic segments, where the
	 * value is a constant.
	 * @param uv the path parameter
	 * @return the third derivative of the x coordinate with respect to the
	 *         path parameter
	 */
	public double d3xDu3(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 0.0;
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (-(c3c0 - c0x0) + (c6c3 - c3c0));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the third derivative with respect to the path parameter
	 * of the y coordinate of a point on the path.
	 * <P>
	 * Note: the value is 0.0 except for cubic segments, where the
	 * value is a constant.
	 * @param uv the path parameter
	 * @return the third derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double d3yDu3(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 0.0;
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (-(c4c1 - c1y0) + (c7c4 - c4c1));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
	    }
	}

	/**
	 * Compute the third derivative with respect to the path parameter
	 * of the z coordinate of a point on the path.
	 * <P>
	 * Note: the value is 0.0 except for cubic segments, where the
	 * value is a constant.
	 * @param uv the path parameter
	 * @return the third derivative of the y coordinate with respect to the
	 *         path parameter
	 */
	public double d3zDu3(UValues uv) {
	    switch(st) {
	    case PathIterator3D.SEG_MOVETO:
		return 0.0;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		return 0.0;
	    case PathIterator3D.SEG_QUADTO:
		return 0.0;
	    case PathIterator3D.SEG_CUBICTO:
		return 6.0 * (-(c5c2 - c2z0) + (c8c5 - c5c2));
	    default:
		throw new IllegalArgumentException
		    (errorMsg("stUnknown", Path3DInfo.getTypeString(st)));
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
	    if (st == PathIterator3D.SEG_MOVETO) return 0.0;
	    double dxdu = dxDu(uv);
	    double dydu = dyDu(uv);
	    double dzdu = dzDu(uv);
	    return Math.sqrt(dxdu*dxdu + dydu*dydu + dzdu*dzdu);
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the signed distance along the path from its initial point
	 * @param uv the path parameter
	 * @return the second derivative of the signed distance along the path
	 *         with respect to the path parameter
	 */
	public double d2sDu2(UValues uv) {
	    if (st == PathIterator3D.SEG_MOVETO) return 0.0;
	    double dxdu = dxDu(uv);
	    double dydu = dyDu(uv);
	    double dzdu = dzDu(uv);
	    double d2xdu2 = d2xDu2(uv);
	    double d2ydu2 = d2yDu2(uv);
	    double d2zdu2 = d2zDu2(uv);
	    double dsduSquared = dxdu*dxdu + dydu*dydu + dzdu*dzdu;
	    return (dxdu*d2xdu2 + dydu*d2ydu2 + dzdu*d2zdu2)
		/ Math.sqrt(dsduSquared);
	}

	private static UValues uvZero = new UValues(0.0);
	private static UValues uvOne = new UValues(1.0);

	/**
	 * Compute the curvature at a point with a given path parameter
	 * If the path segment's type of SEG_CLOSE and the starting and
	 * ending points of the segment are identical, the curvature at
	 * the end of the previous segment is returned.
	 * @param uv the path parameter
	 * @return the curvature
	 */
	public double curvature(UValues uv) {
	    if (st == PathIterator3D.SEG_MOVETO) {
		return 0.0;
	    } else if (st == PathIterator3D.SEG_LINETO) {
		return 0.0;
	    } else if (st == PathIterator3D.SEG_CLOSE) {
		if (x0 == c0 && y0 == c1 && z0 == c2) {
		    if (uv.u > 0.0) return Double.NaN;
		    return last.curvature(uvOne);
		} else {
		    return 0.0;
		}
	    }
	    double xp = dxDu(uv);
	    double yp = dyDu(uv);
	    double zp = dzDu(uv);
	    double xpp = d2xDu2(uv);
	    double ypp = d2yDu2(uv);
	    double zpp = d2zDu2(uv);

	    double tmp = xp*xp + yp*yp + zp*zp;
	    tmp = tmp * Math.sqrt(tmp);
	    double tmp1 = zpp*yp - ypp*zp;
	    tmp1 *= tmp1;
	    double tmp2 = xpp*zp - zpp*xp;
	    tmp2 *= tmp2;
	    double tmp3 = ypp*xp - xpp*yp;
	    tmp3 *= tmp3;
	    return Math.sqrt(tmp1 + tmp2 + tmp3) / tmp;
	}

	/**
	 * Compute the torsion at a point with a given path parameter
	 * If the path segment's type of SEG_CLOSE and the starting and
	 * ending points of the segment are identical, the torsion at
	 * the end of the previous segment is returned.
	 * @param uv the path parameter
	 * @return the torsion
	 */
	public double torsion(UValues uv) {
	    if (st == PathIterator3D.SEG_MOVETO) {
		return 0.0;
	    } else if (st == PathIterator3D.SEG_LINETO) {
		return 0.0;
	    } else if (st == PathIterator3D.SEG_CLOSE) {
		if (x0 == c0 && y0 == c1 && z0 == c2) {
		    if (uv.u > 0.0) return Double.NaN;
		    return last.torsion(uvOne);
		} else {
		    return 0.0;
		}
	    }
	    double xp = dxDu(uv);
	    double yp = dyDu(uv);
	    double zp = dzDu(uv);
	    double xpp = d2xDu2(uv);
	    double ypp = d2yDu2(uv);
	    double zpp = d2zDu2(uv);
	    double xppp = d3xDu3(uv);
	    double yppp = d3yDu3(uv);
	    double zppp = d3zDu3(uv);

	    double numerator = xp*(ypp*zppp - yppp*zpp)
		- yp*(xpp*zppp - xppp*zpp)
		+ zp*(xpp*yppp - xppp*ypp);

	    double tmp1 = yp*zpp - ypp*zp;
	    double tmp2 = xp*zpp - xpp*zp;
	    double tmp3 = xp*ypp - xpp*yp;
	    double denominator = tmp1*tmp1 + tmp2*tmp2 + tmp3*tmp3;
	    if (denominator == 0.0) return Double.NaN;
	    return numerator/denominator;
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
		if (x0 == c0 && y0 == c1 && z0 == c2) return false;
		break;
	    case PathIterator.SEG_CLOSE:
		if (x0 == c0 && y0 == c1 && z0 == c2) {
		    if (uv.u > 0.0) return false;
		    if (last == null) return false;
		    return last.curvatureExists(uvOne);
		}
		break;
	    case PathIterator.SEG_QUADTO:
		if (x0 == c0 && y0 == c1 && z0 == c2
		    && x0 == c3 && y0 == c4 && z0 == c5) {
		    return false;
		}
		break;
	    case  PathIterator.SEG_CUBICTO:
		if (x0 == c0 && y0 == c1 && z0 == c2
		    && x0 == c3 && y0 == c4 && z0 == c5
		    && x0 == c6 && y0 == c7 && z0 == c8) {
		    return false;
		}
		break;
	    }
	    return true;
	}

	/**
	 * Get the tangent vector for a specified value of the path
	 * parameter and an output-array offset.
	 * If the tangent vector does not exist (e.g., the length of
	 * the line does not vary with the path parameter), the
	 * tangent vector will be set to zero.  The tangent vector
	 * will have unit length if it is not zero.
	 * @param uv the path parameter
	 * @param array an array of length no less than 3 used to
	 *        store the tangent vector, with array[offset]
	 *        containing the tangent vector's X component,
	 *        array[offset+1] containing the tangent vector's Y
	 *        component, and array[offset+2] containing the tangent
	 *        vector's Z component
	 * @param offset the index into the array at which to store
	 *        the tangent vector
	 * @return true if the tangent vector exists; false if the tangent
	 *         vector does not exist
	 */
	public boolean getTangent(UValues uv, double[] array, int offset) {
	    if (st == PathIterator.SEG_CLOSE) {
		if (x0 == c0 && y0 == c1 && z0 == c2) {
		    if (uv.u == 0.0) {
			return last.getTangent(uv, array, offset);
		    } else {
			array[offset] = 0.0;
			array[offset+1] = 0.0;
			return false;
		    }
		}
	    } else if (st == PathIterator.SEG_LINETO) {
		double tmp = c0x0*c0x0 + c1y0*c1y0 + c2z0*c2z0;
		if (tmp == 0.0) return false;
		tmp = Math.sqrt(tmp);
		array[offset] = c0x0/tmp;
		array[offset+1] = c1y0/tmp;
		array[offset+2] = c2z0/tmp;
		return true;
	    }
	    double xp = dxDu(uv);
	    double yp = dyDu(uv);
	    double zp = dzDu(uv);
	    double tmp = Math.sqrt(xp*xp + yp*yp + zp*zp);
	    if (tmp == 0.0) {
		array[offset] = 0.0;
		array[offset+1] = 0.0;
		array[offset+2] = 0.0;
		return false;
	    }
	    array[offset] = xp/tmp;
	    array[offset+1] = yp/tmp;
	    array[offset+2] = zp/tmp;
	    return true;
	}

	/**
	 * Get the normal vector for a given value of the path
	 * parameter and an offset for the array storing the normal
	 * vector.
	 * The normal vector N is a vector of unit length,
	 * perpendicular to the tangent vector, and oriented so that
	 * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is
	 * the curvature.  If the normal vector does not
	 * exist (e.g., the length of the line does not vary with the
	 * path parameter), the normal vector will not be changed.
	 * @param uv the path parameter
	 * @param array an array of length no less than 3 used to
	 *        store the normal vector, with array[offset]
	 *        containing the normal vector's X component,
	 *        array[offset+1] containing the normal vector's Y
	 *        component, and array[offset+2] containing the normal
	 *        vector's Z component
	 * @param offset the index into the array at which to store
	 *        the normal vector
	 * @return true if the normal vector exists; false if the normal
	 *         vector does not exist
	 */
	public boolean getNormal(UValues uv, double[] array, int offset) {
	    if (st == PathIterator.SEG_LINETO || st == PathIterator.SEG_CLOSE) {
		return false;
	    }
	    double[] vtmp = new double[3];
	    boolean result = getTangent(uv, vtmp, 0);
	    if (result == false) {
		// Arrays.fill(array, offset, offset+3, 0.0);
		return false;
	    }
	    double tmp = d2sDu2(uv);
	    double[] ttmp = new double[3];
	    VectorOps.multiply(ttmp, 0, tmp, vtmp, 0, 3);
	    ttmp[0] = d2xDu2(uv) - ttmp[0];
	    ttmp[1] = d2yDu2(uv) - ttmp[1];
	    ttmp[2] = d2zDu2(uv) - ttmp[2];
	    tmp = VectorOps.dotProduct(ttmp, ttmp);
	    tmp = Math.sqrt(tmp);
	    if (tmp == 0.0) {
		if (st != PathIterator.SEG_CUBICTO) return false;
		UValues uvuv = uv.equals(uvZero)? uvOne:
		    uv.equals(uvOne)? uvZero:
		    (uv.u <= 0.5)? uvOne: uvZero;
		double [] btmp = new double[3];
		if (getBinormal(uvuv, btmp, 0)) {
		    VectorOps.crossProduct(array, offset, btmp, 0, vtmp, 0);
		    VectorOps.normalize(array, offset, 3);
		    return true;
		} else {
		    return false;
		}
	    }
	    array[offset] = ttmp[0] / tmp;
	    array[offset+1] = ttmp[1] / tmp;
	    array[offset+2] = ttmp[2] / tmp;
	    return true;
	}
	/**
	 * Get the binormal vector for a given value of the path
	 * parameter and an offset for the array storing the normal
	 * vector. The binormal vector is defined as the cross product
	 * T &times; N of the tangent and normal vectors.
	 * If the binormal vector does not exist (e.g., the length of the
	 * line does not vary with the path parameter or the path is locally
	 * straight), the binormal vector will be set to zero.
	 * @param uv the path parameter
	 * @param array an array of length no less than 3 used to
	 *        store the normal vector, with array[offset]
	 *        containing the normal vector's X component,
	 *        array[offset+1] containing the normal vector's Y
	 *        component, and array[offset+2] containing the normal
	 *        vector's Z component
	 * @param offset the index into the array at which to store
	 *        the normal vector
	 * @return true if the binormal vector exists; false if the normal
	 *         vector does not exist
	 */
	public boolean getBinormal(UValues uv, double[] array, int offset) {
	    boolean status = getTangent(uv, array, offset);
	    if (status == false) return false;
	    double[] tmp = new double[3];
	    status = getNormal(uv, tmp, 0);
	    if (status == false) {
		Arrays.fill(array, offset, offset+3, 0.0);
		return false;
	    }
	    VectorOps.crossProduct(array, offset, array, offset, tmp, 0);
	    return true;
	}
    }

    /**
     * Compute the x coordinate of a point on the path given
     * path-segment parameters.
     * When the type is SEG_MOVETO, x0 and y0 are ignored.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by 
     *        {@link PathIterator3D PathIterator3D}
     *        (values may be PathIterator3D.SEG_MOVETO,
     *         PathIterator3D.SEG_LINETO, PathIterator3D.SEG_QUADTO,
     *         PathIterator3D.SEG_CUBICTO, and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of the x coordinate
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double getX(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return coords[0];
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return coords[0]*u + x0*u1;
	case PathIterator3D.SEG_QUADTO:
	    return u1*u1*x0 + 2.0 *u1*u*coords[0] + u*u*coords[3];
	case PathIterator3D.SEG_CUBICTO:
	    return u1*u1*u1*x0 +
		3.0 * (u1*u1*u*coords[0] + u1*u*u*coords[3])
		+ u*u*u*coords[6];
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the y coordinate of a point on a path given path-segment
     * parameters.
     * When the type is SEG_MOVETO, x0 and y0 are ignored.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of the y coordinate
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double getY(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return coords[1];
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return coords[1]*u + y0*u1;
	case PathIterator3D.SEG_QUADTO:
	    return u1*u1*y0 + 2.0 *u1*u*coords[1] + u*u*coords[4];
	case PathIterator3D.SEG_CUBICTO:
	    return u1*u1*u1*y0 +
		3.0 * (u1*u1*u*coords[1] + u1*u*u*coords[4])
		+ u*u*u*coords[7];
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }


    /**
     * Compute the z coordinate of a point on a path given path-segment
     * parameters.
     * When the type is SEG_MOVETO, x0 and y0 are ignored.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of the y coordinate
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double getZ(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return coords[2];
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return coords[2]*u + z0*u1;
	case PathIterator3D.SEG_QUADTO:
	    return u1*u1*z0 + 2.0 *u1*u*coords[2] + u*u*coords[5];
	case PathIterator3D.SEG_CUBICTO:
	    return u1*u1*u1*z0 +
		3.0 * (u1*u1*u*coords[2] + u1*u*u*coords[5])
		+ u*u*u*coords[8];
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }


    /**
     * Compute dx/du given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of dx/ds
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dxDu(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	   return coords[0] - x0;
	case PathIterator3D.SEG_QUADTO:
	    return  2.0 * (u1*(coords[0]-x0) + u*(coords[3]-coords[0]));
	case PathIterator3D.SEG_CUBICTO:
	    return 3.0 * (u1*u1*(coords[0] - x0)
			  + 2*u*u1*(coords[3] - coords[0])
			  + u*u*(coords[6] - coords[3]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute dy/du given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of dy/du
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dyDu(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return coords[1] - y0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (u1*(coords[1]-y0) + u*(coords[4]-coords[1]));
	case PathIterator3D.SEG_CUBICTO:
	    return 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[4] - coords[1])
			  + u*u*(coords[7] - coords[4]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute dz/du given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0;
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of dy/du
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dzDu(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return coords[2] - z0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (u1*(coords[2]-z0) + u*(coords[5]-coords[2]));
	case PathIterator3D.SEG_CUBICTO:
	    return 3.0 * (u1*u1*(coords[2] - z0)
			  + 2.0*u*u1*(coords[5] - coords[2])
			  + u*u*(coords[8] - coords[5]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the derivative of the path length s with respect to the
     * path's parameter u given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of ds/du
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double dsDu(double u, double x0, double y0, double z0,
			      int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double dxdu;
	double dydu;
	double dzdu;
	double u1 = 1.0 - u;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    dxdu = coords[0] - x0;
	    dydu = coords[1] - y0;
	    dzdu = coords[2] - z0;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    dxdu = 2.0 * (u1*(coords[0]-x0) + u*(coords[3]-coords[0]));
	    dydu = 2.0 * (u1*(coords[1]-y0) + u*(coords[4]-coords[1]));
	    dzdu = 2.0 * (u1*(coords[2]-z0) + u*(coords[5]-coords[2]));
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    dxdu = 3.0 * (u1*u1*(coords[0] - x0)
			  + 2*u*u1*(coords[3] - coords[0])
			  + u*u*(coords[6] - coords[3]));
	    dydu = 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[4] - coords[1])
			  + u*u*(coords[7] - coords[4]));
	    dzdu = 3.0 * (u1*u1*(coords[2] - z0)
			  + 2.0*u*u1*(coords[5] - coords[2])
			  + u*u*(coords[8] - coords[5]));
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}	
	double dsduSquared = dxdu*dxdu + dydu*dydu + dzdu*dzdu;
	return  Math.sqrt(dsduSquared);
    }

     /**
     * Compute the second derivative of x with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>x/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d2xDu2(double u, double x0, double y0, double z0,
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
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    u1 = 1.0 - u;
	    return 2.0 * (x0 - coords[0] + coords[3] - coords[0]);
	case PathIterator3D.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    return 6.0 * (u1*(x0 -2.0*coords[0] + coords[3])
			  + u*(coords[6] - 2.0*coords[3] + coords[0]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the second derivative of y with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>y/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d2yDu2(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (y0 - coords[1] + coords[4] - coords[1]);
	case PathIterator3D.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    return 6.0 * (u1*(y0 -2.0*coords[1] + coords[4])
			  + u*(coords[7] - 2.0*coords[4] + coords[1]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the second derivative of z with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>y/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d2zDu2(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (z0 - coords[2] + coords[5] - coords[2]);
	case PathIterator3D.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    return 6.0 * (u1*(z0 -2.0*coords[2] + coords[5])
			  + u*(coords[8] - 2.0*coords[5] + coords[2]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

     /**
     * Compute the third derivative of x with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * <P>
     * Note: the value is 0.0 except for a cubic segment, where it is
     * constant for u in the range [0.0, 1.0]. Curves with torsion should be
     * represented by cubic B&eacute;zier curves for accuracy.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>x/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d3xDu3(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 0.0;
	case PathIterator3D.SEG_CUBICTO:
	    return 6.0 * (-(x0 -2.0*coords[0] + coords[3])
			  + (coords[6] - 2.0*coords[3] + coords[0]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the third derivative of y with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * <P>
     * Note: the value is 0.0 except for a cubic segment, where it is
     * constant for u in the range [0.0, 1.0]. Curves with torsion should be
     * represented by cubic B&eacute;zier curves for accuracy.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>y/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d3yDu3(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 0.0;
	case PathIterator3D.SEG_CUBICTO:
	    return 6.0 * (-(y0 -2.0*coords[1] + coords[4])
			  + (coords[7] - 2.0*coords[4] + coords[1]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Compute the third derivative of z with respect to u
     *  given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * <P>
     * Note: the value is 0.0 except for a cubic segment, where it is
     * constant for u in the range [0.0, 1.0]. Curves with torsion should be
     * represented by cubic B&eacute;zier curves for accuracy.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>y/du<sup>2</sup>;
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double d3zDu3(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 0.0;
	case PathIterator3D.SEG_CUBICTO:
	    return 6.0 * (-(z0 -2.0*coords[2] + coords[5])
			  + (coords[8] - 2.0*coords[5] + coords[2]));
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }


    /**
     * Compute the second derivative of the path length s with respect to u
     * given path-segment parameters.
     * When the type is SEG_CLOSE, the first two elements of the fifth
     * argument (coords) must be set to the x and y coordinate
     * respectively for the point provided by most recent segment
     * whose type is SET_MOVETO.
     * <P>
     * The returned value will be Double.NaN in cases where division by
     * zero would occur, such as when the initial point for a segment and
     * the control points and final point given by the array argument are
     * identical. The most likely case is when the type is
     * {@link PathIterator3D#SEG_CLOSE PathIterator3D.SEG_CLOSE}.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by 
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the value of d<sup>2</sup>s/du<sup>2</sup>; Double.NaN if
     *         not defined but zero if the type is
     *         {@link PathIterator3D#SEG_MOVETO PathIterator3D.SEG_MOVETO}
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     * @see PathIterator
     */
    public static double d2sDu2(double u, double x0, double y0, double z0,
				int type, double[] coords)
	throws IllegalArgumentException
    {
	if (u < 0.0 || u > 1.0) {
	    throw new IllegalArgumentException(errorMsg("arg1OutOfRange", u));
	}
	double dxdu;
	double dydu;
	double dzdu;
	double d2xdu2;
	double d2ydu2;
	double d2zdu2;
	double u1;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    u1 = 1.0 - u;
	    dxdu = 2.0 * (u1*(coords[0]-x0) + u*(coords[3]-coords[0]));
	    dydu = 2.0 * (u1*(coords[1]-y0) + u*(coords[4]-coords[1]));
	    dzdu = 2.0 * (u1*(coords[2]-z0) + u*(coords[5]-coords[2]));
	    d2xdu2 = 2.0 * (x0 - coords[0] + coords[3] - coords[0]);
	    d2ydu2 = 2.0 * (y0 - coords[1] + coords[4] - coords[1]);
	    d2zdu2 = 2.0 * (z0 - coords[2] + coords[5] - coords[2]);
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    u1 = 1.0 - u;
	    dxdu = 3.0 * (u1*u1*(coords[0] - x0)
			  + 2.0*u*u1*(coords[3] - coords[0])
			  + u*u*(coords[6] - coords[3]));
	    dydu = 3.0 * (u1*u1*(coords[1] - y0)
			  + 2.0*u*u1*(coords[4] - coords[1])
			  + u*u*(coords[7] - coords[4]));
	    dzdu = 3.0 * (u1*u1*(coords[2] - z0)
			  + 2.0*u*u1*(coords[5] - coords[2])
			  + u*u*(coords[8] - coords[5]));
	    double u1u1deriv = -2.0 * (1 - u);
	    double uuderiv = 2.0 * u;
	    double uu1deriv = 1.0 - 2.0 * u;
	    d2xdu2 = 3.0 * (u1u1deriv*(coords[0] - x0)
			  + 2.0*uu1deriv*(coords[3] - coords[0])
			  + uuderiv*(coords[6] - coords[3]));
	    d2ydu2 = 3.0 * (u1u1deriv*(coords[1] - y0)
			  + 2.0*uu1deriv*(coords[4] - coords[1])
			  + uuderiv*(coords[7] - coords[4]));
	    d2zdu2 = 3.0 * (u1u1deriv*(coords[2] - z0)
			  + 2.0*uu1deriv*(coords[5] - coords[2])
			  + uuderiv*(coords[8] - coords[5]));
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}	
	double dsduSquared = dxdu*dxdu + dydu*dydu + dzdu*dzdu;
	if (dsduSquared == 0.0) {
	    return Double.NaN;
	} else {
	    return (dxdu*d2xdu2 + dydu*d2ydu2 + dzdu*d2zdu2)
		/ Math.sqrt(dsduSquared);
	}
    }


    /**
     * Compute the curvature given path-segment parameters.
     * The type may not be SEG_MOVETO.  When the
     * type is SEG_CLOSE, the first two elements of coords must be set
     * to the x and y coordinate respectively for the point provided by
     * most recent segment whose type is SET_MOVETO.
     * The curvature is given by the expression
     * <blockquote><pre>
     * sqrt((z"y'-y"z')<sup>2</sup> + (x"z'-z"x')<sup>2</sup> + (y"x'-x"y')<sup>2</sup>) / (x'<sup>2</sup> + y'<sup>2</sup> + z'<sup>2</sup>)<sup>3/2</sup>
     * </pre></blockquote>
     * where the derivative are computed with respect to the path
     * parameter.  The radius of curvature is the multiplicative
     * inverse of the absolute value of the curvature.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the curvature; Double.NaN if not defined (this may happen
     *         if a line segment has zero length)
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     * @exception ArrayIndexOutOfBoundsException the array argument was
     *            two small
     * @exception NullPointerException the array argument was null
     */
    public static double curvature(double u, double x0, double y0, double z0,
				   int type, double[] coords)
	throws IllegalArgumentException
    {
	if (type == PathIterator3D.SEG_MOVETO) {
	    return Double.NaN;
	} else if (type == PathIterator3D.SEG_CLOSE) {
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]) {
		return Double.NaN;
	    } else {
		return 0.0;
	    }
	} else if (type == PathIterator3D.SEG_LINETO) {
	    return 0.0;
	}

	double xp = dxDu(u, x0, y0, z0, type, coords);
	double yp = dyDu(u, x0, y0, z0, type, coords);
	double zp = dzDu(u, x0, y0, z0, type, coords);
	double xpp = d2xDu2(u, x0, y0, z0, type, coords);
	double ypp = d2yDu2(u, x0, y0, z0, type, coords);
	double zpp = d2zDu2(u, x0, y0, z0, type, coords);

	double tmp = xp*xp + yp*yp + zp*zp;
	tmp = tmp * Math.sqrt(tmp);
	if (tmp == 0.0) {
	    return Double.NaN;
	} else {
	    double tmp1 = zpp*yp - ypp*zp;
	    tmp1 *= tmp1;
	    double tmp2 = xpp*zp - zpp*xp;
	    tmp2 *= tmp2;
	    double tmp3 = ypp*xp - xpp*yp;
	    tmp3 *= tmp3;
	    return Math.sqrt(tmp1 + tmp2 + tmp3) / tmp;
	}
    }

    /**
     * Determine if the arguments allow the curvature to be computed.
     * The curvature cannot be computed when the type is
     * PathIterator3D.SEG_MOVETO or when the control points provided by
     * the array match the initial values x0 and y0.
     * @param u the path-segment parameter in the range [0,1]
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the curvature; Double.NaN if not defined (this may happen
     *         if a line segment has zero length)
     * @exception IllegalArgumentException u is out of range or the type
     *         does not have a legal value
     */
    public boolean curvatureExists(double u, double x0, double y0, double z0,
				   int type, double[] coords)
	throws IllegalArgumentException, NullPointerException,
	       IndexOutOfBoundsException
    {
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return false;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    if (x0 == coords[0] && y0 == coords[1]
		&& z0 == coords[2]) return false;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]
		&& x0 == coords[3] && y0 == coords[4] && z0 == coords[5]) {
		return false;
	    }
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]
		&& x0 == coords[3] && y0 == coords[4] && z0 == coords[5]
		&& x0 == coords[6] && y0 == coords[7] && z0 == coords[8]) {
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
     * @param array an array of length no less than 3 used to store
     *        the tangent vector, with array[offset] containing the
     *        tangent vector's X component and array[offset+1]
     *        containing the tangent vector's Y component
     * @param offset the index into the array at which to store the tangent
     *        vector
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param z0 the Z coordinate of the point on the segment for u = 0
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
				     double x0, double y0, double z0,
				     int type, double[] coords)
    {
	if (type == PathIterator.SEG_MOVETO) {
	    return false;
	}
	double xp = Path3DInfo.dxDu(u, x0, y0, z0, type, coords);
	double yp = Path3DInfo.dyDu(u, x0, y0, z0, type, coords);
	double zp = Path3DInfo.dzDu(u, x0, y0, z0, type, coords);
	double tmp = Math.sqrt(xp*xp + yp*yp + zp*zp);
	if (tmp == 0.0) {
	    array[offset] = 0.0;
	    array[offset+1] = 0.0;
	    array[offset+2] = 0.0;
	    return false;
	}
	array[offset] = xp/tmp;
	array[offset+1] = yp/tmp;
	array[offset+2] = zp/tmp;
	return true;
    }

    /**
     * Get the normal vector for a given value of the path parameter and
     * an offset for the array storing the normal vector.
     * The normal vector N is a vector of unit length, perpendicular to
     * the tangent vector, and oriented so that
     * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is the
     * curvature.
     * If the normal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the normal vector
     * will not be changed.
     * @param u the path-segment parameter in the range [0,1]
     * @param array an array of length no less than 3 used to store
     *        the normal vector, with array[offset] containing the
     *        normal vector's X component, array[offset+1] containing
     *        the normal vector's Y component, and array[offset + 2]
     *        containing the normal vector's Y component
     * @param offset the index into the array at which to store the normal
     *        vector
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param z0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO,
     *        PathIterator3D.SEG_LINETO, PathIterator3D.SEG_QUADTO,
     *        PathIterator3D.SEG_CUBICTO, and PathIterator3D.SEG_CLOSE)
     * @param coords the coordinates array as defined by
     *        {@link org.bzdev.geom.PathIterator3D PathIterator3D},
     *         but with coords[0], coords[1], and coords[2] set to the
     *         X, Y, and Z coordinates respectively for the last
     *         MOVETO operation when the type is
     *         PathIterator3D.SEG_CLOSE
     * @return true if the normal vector exists; false if the normal
     *         vector does not exist
     */
    public static boolean getNormal(double u, double[] array, int offset,
				    double x0, double y0, double z0,
				    int type, double[] coords)
    {
	if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE
	    || type == PathIterator.SEG_MOVETO) {
	    return false;
	}
	double[] vtmp = new double[3];
	boolean result = getTangent(u, vtmp, 0, x0, y0, z0, type, coords);
	if (result == false) {
	    // Arrays.fill(array, offset, offset+3, 0.0);
	    return false;
	}
	double tmp = d2sDu2(u, x0, y0, z0, type, coords);
	double[] ttmp = new double[3];
	VectorOps.multiply(ttmp, 0, tmp, vtmp, 0, 3);
	ttmp[0] = d2xDu2(u, x0, y0, z0, type, coords) - ttmp[0];
	ttmp[1] = d2yDu2(u, x0, y0, z0, type, coords) - ttmp[1];
	ttmp[2] = d2zDu2(u, x0, y0, z0, type, coords) - ttmp[2];
	tmp = VectorOps.dotProduct(ttmp, ttmp);
	tmp = Math.sqrt(tmp);
	if (tmp == 0) {
	    if (type != PathIterator.SEG_CUBICTO) return false;
	    double uu = (u == 0.0)? 1.0:
		(u == 1.0)? 0.0:
		(u <= 0.5)? 1.0: 0.0;
	    double[] btmp = new double[3];
	    if (getBinormal(uu, btmp, 0, x0, y0, z0, type, coords)) {
		VectorOps.crossProduct(array, offset, btmp, 0, vtmp, 0);
		VectorOps.normalize(array, offset, 3);
		return true;
	    }
	    return false;
	}
	array[offset] = ttmp[0] / tmp;
	array[offset+1] = ttmp[1] / tmp;
	array[offset+2] = ttmp[2] / tmp;
	return true;
    }

    /**
     * Get the binormal vector for a given value of the path parameter and
     * an offset for the array storing the normal vector.
     * The binormal vector is defined as the cross product T &times; N of the
     * tangent vector T and the normal vector N.
     * If the binormal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter or the path is locally
     * straight), the binormal vector will be set to zero.
     * @param u the path-segment parameter in the range [0,1]
     * @param array an array of length no less than 3 used to store
     *        the normal vector, with array[offset] containing the
     *        normal vector's X component, array[offset+1] containing
     *        the normal vector's Y component, and array[offset + 2]
     *        containing the normal vector's Y component
     * @param offset the index into the array at which to store the normal
     *        vector
     * @param x0 the X coordinate of the point on the segment for u = 0
     * @param y0 the Y coordinate of the point on the segment for u = 0
     * @param z0 the Y coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO,
     *        PathIterator3D.SEG_LINETO, PathIterator3D.SEG_QUADTO,
     *        PathIterator3D.SEG_CUBICTO, and PathIterator3D.SEG_CLOSE)
     * @param coords the coordinates array as defined by
     *        {@link org.bzdev.geom.PathIterator3D PathIterator3D},
     *         but with coords[0], coords[1], and coords[2] set to the
     *         X, Y, and Z coordinates respectively for the last
     *         MOVETO operation when the type is
     *         PathIterator3D.SEG_CLOSE
     * @return true if the normal vector exists; false if the normal
     *         vector does not exist
     */
    public static boolean getBinormal(double u, double[] array, int offset,
				      double x0, double y0, double z0,
				      int type, double[] coords)
    {
	boolean status = getTangent(u, array, offset, x0, y0, z0, type, coords);
	if (status == false) return false;
	double[] tmp = new double[3];
	status = getNormal(u, tmp, 0, x0, y0, z0, type, coords);
	if (status == false) {
	    Arrays.fill(array, offset, offset+3, 0.0);
	    return false;
	}
	VectorOps.crossProduct(array, offset, array, offset, tmp, 0);
	return true;
    }

    /**
     * Get the type of a path-iterator item, formatted as a string
     * @return a string describing the type
     */
    public static String getTypeString(int type) {
	switch (type) {
	case PathIterator3D.SEG_CLOSE:
	    return "SEG_CLOSE";
	case PathIterator3D.SEG_MOVETO:
	    return "SEG_MOVETO";
	case PathIterator3D.SEG_LINETO:
	    return "SEG_LINETO";
	case PathIterator3D.SEG_QUADTO:
	    return "SEG_QUADTO";
	case PathIterator3D.SEG_CUBICTO:
	    return "SEG_CUBICTO";
	default:
	    return "<Unknown>";
	}
    }

    /**
     * Class defining a list entry describing a path segment.
     * The list is returned by calling
     * {@link Path3DInfo#getEntries(Path3D) getEntries}.
     * @see Path3DInfo#getEntries(Path3D)
     */
    public static class Entry {
	private int index;
	int type;
	private boolean hasStart = false;
	double x;
	double y;
	double z;
	// private Point3D start;
	private Point3D end;
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
	public Point3D getStart() {
	    return hasStart? new Point3D.Double(x, y, z): null;
	}

	/**
	 * Get the ending point for the segment.
	 * @return the ending point
	 */
	public Point3D getEnd() {return end;}

	/**
	 * Get the length of a segment.
	 * @return the length of the segment
	 */
	public double getSegmentLength() {return segmentLength;}

	/**
	 * Get the coordinate array for the segment.
	 * This will include intermediate control points the end point
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
	 * @return the segment type (PathIterator3D.SEG_MOVETO,
	 *         PathIterator3D.SEG_LINETO, PathIterator3D.SEG_QUADTO,
	 *         PathIterator3D.SEG_CUBICTO and PathIterator3D.SEG_CLOSE)
	 */
	public int getType() {return type;}

	/**
	 * Get the type of the segment, formatted as a string
	 * @return a string describing the segment type
	 */
	public String getTypeString() {
	    switch (type) {
	    case PathIterator3D.SEG_CLOSE:
		return "SEG_CLOSE";
	    case PathIterator3D.SEG_MOVETO:
		return "SEG_MOVETO";
	    case PathIterator3D.SEG_LINETO:
		return "SEG_LINETO";
	    case PathIterator3D.SEG_QUADTO:
		return "SEG_QUADTO";
	    case PathIterator3D.SEG_CUBICTO:
		return "SEG_CUBICTO";
	    default:
		return "<Unknown>";
	    }
	}

	Entry(int ind, int type, Point3D start, Point3D end, double length,
	      double[] coords, SegmentData data) {
	    this.index = ind;
	    this.type = type;
	    // this.start = start;
	    if (start != null) {
		this.x = start.getX();
		this.y = start.getY();
		this.z = start.getZ();
		hasStart = true;
	    }
	    this.end = end;
	    segmentLength = length;
	    this.coords = new double[9];
	    switch (type) {
	    case PathIterator3D.SEG_CLOSE:
		for (int i = 0; i < 3; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator3D.SEG_MOVETO:
		for (int i = 0; i < 3; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator3D.SEG_LINETO:
		for (int i = 0; i < 3; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int i = 0; i < 6; i++) this.coords[i] = coords[i];
		break;
	    case PathIterator3D.SEG_CUBICTO:
	    default:
		for (int i = 0; i < 9; i++) this.coords[i] = coords[i];
		break;
	    }
	    this.data = data;
	}

	Entry(int ind, int type, double xs, double ys, double zs,
	      double xe, double ye, double ze,
	      double length, double[] coords,
	      SegmentData data)
	{
	    this(ind, type, new Point3D.Double(xs, ys, zs),
		 new Point3D.Double(xe, ye, ze),
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
    public static double pathLength(Path3D path) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	for (Entry entry: Path3DInfo.getEntries(path)) {
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
    public static double pathLength(Path3D path, Transform3D at) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	for (Entry entry: Path3DInfo.getEntries(path, at)) {
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
    public static double pathLength(Path3D path, int start, int end) {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	int ind = 0;
	for (Entry entry: Path3DInfo.getEntries(path)) {
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
    public static double pathLength(Path3D path, Transform3D at,
				    int start, int end)
    {
	// double length = 0.0;
	Adder adder = new Adder.Kahan();
	int ind = 0;
	for (Entry entry: Path3DInfo.getEntries(path, at)) {
	    if (ind >= start && ind < end) {
		// length += entry.getSegmentLength();
		adder.add(entry.getSegmentLength());
	    }
	    ind++;
	}
	// return length;
	return adder.getSum();
    }


    static final int N4LEN = 32;

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
     * @param x0 the x coordinate of the point on the segment for u = 0
     * @param y0 the y coordinate of the point on the segment for u = 0
     * @param z0 the z coordinate of the point on the segment for u = 0
     * @param type the segment type as defined by 
     *        {@link java.awt.geom.PathIterator PathIterator}
     *        (values may be PathIterator3D.SEG_MOVETO, PathIterator3D.SEG_LINETO,
     *         PathIterator3D.SEG_QUADTO, PathIterator3D.SEG_CUBICTO
     *         and PathIterator3D.SEG_CLOSE)
     * @param  coords the coordinates array as defined by
     *         {@link java.awt.geom.PathIterator PathIterator}, but with
     *         coords[0] and coords[1] set to the x and y coordinates
     *         respectively for the last MOVETO operation when the type
     *         is PathIterator3D.SEG_CLOSE
     * @return the segment length
     */
    public static double segmentLength(final int type,
				       final double x0,
				       final double y0,
				       final double z0,
				       final double[] coords)
    {
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	    // coords must be set so that (coords[0],coords[1]) is the
	    // position set by the last SEG_MOVETO
	case PathIterator3D.SEG_LINETO:
	    {
		double dx = coords[0] - x0;
		double dy = coords[1] - y0;
		double dz = coords[2] - z0;
		if (dx == 0.0 && dy == 0.0) {
		    if (dz == 0.0) return 0.0;
		    else return Math.abs(dz);
		} else if (dx == 0.0 && dz == 0.0) {
		    return Math.abs(dy);
		} else if (dy == 0.0 && dz == 0.0) {
		    return Math.abs(dx);
		} else {
		    return Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
	    }
	default:
	    {
		double[] fcoords = new double[9];
		double delta;
		double dx = 0.0, dy = 0.0, dz = 0.0;
		if (type == PathIterator3D.SEG_QUADTO) {
		    dx = coords[3] - x0;
		    dy = coords[4] - y0;
		    dz = coords[5] - z0;
		} else if (type == PathIterator3D.SEG_CUBICTO) {
		    dx = coords[6] - x0;
		    dy = coords[7] - y0;
		    dz = coords[8] - z0;
		} else {
		    throw new RuntimeException
			("type value not expected: " + type);
		}
		delta = 0.05 * Math.sqrt(dx*dx + dy*dy + dz*dz);
		FlatteningPathIterator3D fpit
		    = new FlatteningPathIterator3D(type, x0, y0, z0,
						   coords, delta, 10);
		fpit.next();
		double xx0 = x0;
		double yy0 = y0;
		double zz0 = z0;
		Adder adder2 = new Adder.Kahan();
		while (!fpit.isDone()) {
		    int fst = fpit.currentSegment(fcoords);
		    double flen =
			glq4len.integrateWithP(u4len,
					       new SegmentData(fst,
							       xx0, yy0, zz0,
							       fcoords,
							       null));
		    adder2.add(flen);
		    if (fst == PathIterator3D.SEG_QUADTO) {
			xx0 = fcoords[3];
			yy0 = fcoords[4];
			zz0 = fcoords[5];
		    } else {
			xx0 = fcoords[6];
			yy0 = fcoords[7];
			zz0 = fcoords[8];
		    }
		    fpit.next();
		}
		return adder2.getSum();
	    }
	    /*
	      return glq4len.integrateWithP(u4len,
					  new SegmentData(type, x0, y0, z0,
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

    public static double segmentLength(Path3D p, int segment) {
	PathIterator3D pit = p.getPathIterator(null);
	final double[] coords = new double[9];
	int segno = 0;
	int st = pit.currentSegment(coords);
	if (st != PathIterator3D.SEG_MOVETO) {
	    throw new IllegalArgumentException("ill-formed path");
	}
	if (segment == 0) return 0.0;
	double lastX = 0.0;
	double lastY = 0.0;
	double lastZ = 0.0;
	double lastMoveToX = 0.0; 
	double lastMoveToY = 0.0;
	double lastMoveToZ = 0.0;
	for (int i = 0; i < segment; i++) {
	    if (pit.isDone()) {
		throw new 
		    IllegalArgumentException(errorMsg("segNumbOutOfRange"));
	    } else {
		st = pit.currentSegment(coords);
		switch (st) {
		case PathIterator3D.SEG_CLOSE:
		    lastX = lastMoveToX;
		    lastY = lastMoveToY;
		    lastZ = lastMoveToZ;
		    /*
		      coords[0] = lastMoveToX;
		      coords[1] = lastMoveToY;
		      lastX = coords[0];
		      lastY = coords[1];
		    */
		    break;
		case PathIterator3D.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    lastMoveToX = lastX;
		    lastMoveToY = lastY;
		    lastMoveToZ = lastZ;
		    break;
		case PathIterator3D.SEG_LINETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    lastZ = coords[2];
		    break;
		case PathIterator3D.SEG_QUADTO:
		    lastX = coords[3];
		    lastY = coords[4];
		    lastZ = coords[5];
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    lastX = coords[6];
		    lastY = coords[7];
		    lastZ = coords[8];
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
	if (segmentType == PathIterator3D.SEG_CLOSE) {
	    coords[0] = lastMoveToX;
	    coords[1] = lastMoveToY;
	    coords[2] = lastMoveToZ;
	}

	// final double x0 = lastX;
	// final double y0 = lastY;

	return segmentLength(segmentType, lastX, lastY, lastZ, coords);
    }

    /**
     * Get a list of entries describing a path's segments or those for
     * the outline of a shape.
     * @param p the path or shape
     * @return the list of entries
     */
    public static List<Entry> getEntries (Path3D p) {
	return getEntries(p, null);
    }

    /**
     * Get a list of entries describing a path's segments or those for
     * the outline of a shape, modified by an affine transform.
     * @param p the path or shape
     * @param at the affine transform; null if none is to be used
     * @return the list of entries
     */
    public static List<Entry> getEntries (Path3D p, Transform3D at) {
	List<Entry> list = new LinkedList<Entry>();
	PathIterator3D pit = p.getPathIterator(at);
	if (pit.isDone()) return list;
	final double[] coords = new double[9];
	double[] fcoords = new double[9];
	int segno = 0;
	int stt = pit.currentSegment(coords);
	if (stt != PathIterator3D.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	double lastX = coords[0];
	double lastY = coords[1];
	double lastZ = coords[2];
	double lastMoveToX = lastX; 
	double lastMoveToY = lastY;
	double lastMoveToZ = lastZ;
	double length = 0.0;
	SegmentData last = null;
	for (int i = 0; !pit.isDone(); i++) {
	    final int st = pit.currentSegment(coords);
	    final double x0 = lastX;
	    final double y0 = lastY;
	    final double z0 = lastZ;
	    if (st == PathIterator3D.SEG_CLOSE) {
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
		coords[2] = lastMoveToZ;
	    }
	    SegmentData data = new SegmentData(st, x0, y0, z0, coords, last);
	    if (st != PathIterator3D.SEG_MOVETO) {
		if (st == PathIterator3D.SEG_CLOSE
		    || st == PathIterator3D.SEG_LINETO) {
		    // special case - for straight lines, this is faster. The
		    // tradeoff is the cost of the test to see determine if the
		    // current segment type is SEG_CLOSE or SEG_LINETO.
		    // Also treat horizontal and vertical lines as a special
		    // case.
		    double dx = coords[0] - x0;
		    double dy = coords[1] - y0;
		    double dz = coords[2] - z0;
		    if (dx == 0.0 && dy == 0.0) {
			if (dz == 0.0) length = 0.0;
			else length = Math.abs(dz);
		    } else if (dx == 0.0 && dz == 0.0) {
		        length = Math.abs(dy);
		    } else if (dy == 0.0 && dz == 0.0) {
			length = Math.abs(dx);
		    } else {
			length = Math.sqrt(dx*dx + dy*dy + dz*dz);
		    }
		} else {
		    double delta;
		    double dx = 0.0, dy = 0.0, dz = 0.0;;
		    if (st == PathIterator3D.SEG_QUADTO) {
			dx = coords[3] - x0;
			dy = coords[4] - y0;
			dz = coords[5] - z0;
		    } else if (st == PathIterator3D.SEG_CUBICTO) {
			dx = coords[6] - x0;
			dy = coords[7] - y0;
			dz = coords[8] - z0;
		    } else {
			throw new RuntimeException
			    ("st value not expected: " + st);
		    }
		    delta = 0.05 * Math.sqrt(dx*dx + dy*dy + dz*dz);
		    FlatteningPathIterator3D fpit
			= new FlatteningPathIterator3D(st, x0, y0, z0,
						       coords, delta, 10);
		    fpit.next();
		    double xx0 = x0;
		    double yy0 = y0;
		    double zz0 = z0;
		    Adder adder2 = new Adder.Kahan();
		    while (!fpit.isDone()) {
			int fst = fpit.currentSegment(fcoords);
			double flen =
			    glq4len.integrateWithP(u4len,
						   new SegmentData(fst,
								   xx0, yy0,
								   zz0,
								   fcoords,
								   null));
			adder2.add(flen);
			if (fst == PathIterator3D.SEG_QUADTO) {
			    xx0 = fcoords[3];
			    yy0 = fcoords[4];
			    zz0 = fcoords[5];
			} else {
			    xx0 = fcoords[6];
			    yy0 = fcoords[7];
			    zz0 = fcoords[8];
			}
			fpit.next();
		    }
		    length = adder2.getSum();
		    /*
		    double lengthOrig =
			glq4len.integrateWithP(u4len,
					       new SegmentData(st, x0, y0, z0,
							       coords, null));
		    */
		}
	    } else {
		length = 0.0;
	    }
	    boolean fullEntry = false;
	    switch (st) {
	    case PathIterator3D.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		lastZ = lastMoveToZ;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		fullEntry = true;
		break;
	    case PathIterator3D.SEG_MOVETO:
		lastX = coords[0];
		lastY = coords[1];
		lastZ = coords[2];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		lastMoveToZ = lastZ;
		fullEntry = (i > 0);
		break;
	    case PathIterator3D.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		lastZ = coords[2];
		fullEntry = true;
		break;
	    case PathIterator3D.SEG_QUADTO:
		lastX = coords[3];
		lastY = coords[4];
		lastZ = coords[5];
		fullEntry = true;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		lastX = coords[6];
		lastY = coords[7];
		lastZ = coords[8];
		fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    if (fullEntry) {
		list.add(new Entry(i, st, x0, y0, z0, lastX, lastY, lastZ,
				   length, coords, data));
	    } else {
		list.add(new Entry(i, st, null,
				   new Point3D.Double(lastX, lastY, lastZ),
				   length, coords, data));
	    }
	    last = data;
	    pit.next();
	}
	return list;
    }

    /**
     * Print information about the segments that make up a path, or the
     * outline of a shape, to the standard output.
     * Entry i contains the x and y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * <P>
     * @param s the shape or path
     */
    public static void printSegments(Path3D s) {
	printSegments(System.out, s);
    }

    /**
     * Print information about the segments that make up a path or
     * an outline of a shape.
     * Entry i contains the x and y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param appendable an Appendable on which to print
     * @param s the shape or path
     */
    public static void printSegments(Appendable appendable, Path3D s) {
	printSegments(null, appendable, s);
    }
    /**
     * Print information about the segments that make up a path or
     * the outline of a shape, adding a prefix.
     * Entry i contains the x and y coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param prefix a prefix to print at the start of each line
     *        (null implies an empty string)
     * @param appendable the appendable for output
     * @param s the shape or path
     */
    public static void printSegments(String prefix, Appendable appendable,
				  Path3D s)
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
	    case PathIterator3D.SEG_MOVETO:
		out.println(prefix + "    type: SEG_MOVETO");
		m = 3;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		out.println(prefix + "    type: SEG_CUBICTO");
		m = 9;
		break;
	    case PathIterator3D.SEG_LINETO:
		out.println(prefix + "    type: SEG_LINETO");
		m = 3;
		break;
	    case PathIterator3D.SEG_QUADTO:
		out.println(prefix + "    type: SEG_QUADTO");
		m = 6;
		break;
	    case PathIterator3D.SEG_CLOSE:
		out.println(prefix + "    type: SEG_CLOSE");
		m = 0;
		break;
	    default:
		System.out.println(prefix +"   [unknown mode]");
	    }
	    Point3D start = entry.getStart();
	    if (start == null) {
		out.println(prefix + "   startingX: none");
		out.println(prefix + "   startingY: none");
		out.println(prefix + "   startingZ: none");
	    } else {
		out.println(prefix + "    startingX: " + start.getX());
		out.println(prefix + "    startingY: " + start.getY());
		out.println(prefix + "    startingZ: " + start.getZ());
	    }
	    if (entry.type == PathIterator3D.SEG_CLOSE) {
		Point3D end = entry.getEnd();
		if (end != null) {
		    out.println(prefix + "    endingX: " + end.getX());
		    out.println(prefix + "    endingY: " + end.getY());
		    out.println(prefix + "    endingZ: " + end.getZ());
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
     * Find the first closed component of a path that goes through a point
     * (x, y, z) and shift that path component so it starts at (x, y, z);
     * The point (x, y, z) must be the last point in a segment (including
     * a MOVE_TO segment).
     * @param path the path
     * @param x the X coordinate of a point on the path
     * @param y the Y coordinate of a point on the path
     * @param z the Z coordinate of a point on the path
     * @return the patch component going through (x, y, z), with its
     *         segments shifted cyclically so that the returned path
     *         starts at the point (x, y, z).
     * @exception IllegalArgumentException
     */
    public static Path3D shiftClosedPath(Path3D path,
					 double x, double y, double z)
    {
	x = (double)(float)x;
	y = (double)(float)y;
	z = (double)(float)z;
	Path3D path1 = (path instanceof Path3D.Float)?
	    new Path3D.Float(): new Path3D.Double();
	Path3D path2 = (path instanceof Path3D.Float)?
	    new Path3D.Float(): new Path3D.Double();
	PathIterator3D pi = path.getPathIterator(null);
	double[] coords = new double[9];
	path = null;		// in case pi doesn't start with SEG_MOVETO
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double xx = 0.0, yy = 0.0, zz = 0.0;
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
		zz = lastz = coords[2];
		if (xx == x && yy == y && zz == z) {
		    path = path2;
		}
		path.moveTo(lastx, lasty, lastz);
		break;
	    case PathIterator.SEG_LINETO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[0];
		yy = coords[1];
		zz = coords[2];
		path.lineTo(coords[0],coords[1], coords[2]);
		if (path != path2 && xx == x && yy == y && zz == z) {
		    path = path2;
		    path.moveTo(x, y, z);
		}
		break;
	    case PathIterator.SEG_QUADTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[3];
		yy = coords[4];
		zz = coords[5];
		path.quadTo(coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5]);
		if (path != path2 && xx == x && yy == y && zz == z) {
		    path = path2;
		    path.moveTo(x, y, z);
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[6];
		yy = coords[7];
		zz = coords[8];
		path.curveTo(coords[0], coords[1], coords[2],
			     coords[3], coords[4], coords[5],
			     coords[6], coords[7], coords[8]);
		if (path != path2 && xx == x && yy == y && zz == z) {
		    path = path2;
		    path.moveTo(x, y, z);
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (closed) break;
		if (xx != lastx || yy != lasty || zz != lastz) {
		    path.lineTo(lastx, lasty, lastz);
		}
		if (path == path2) {
		    path.append(path1, true);
		    path.closePath();
		    return path;
		}
		closed = true;
		xx = lastx;
		yy = lasty;
		zz = lastz;
		break;
	    }
	    pi.next();
	}
	// We didn't find a match to (x, y)
	return null;
    }

    /**
     * Count the number of segments in the  first continuous portion of a
     * path that are Drawable.
     * Drawable segments exclude PathIterator.SEG_MOVETO segments and
     * PathIterator.SEG_CLOSE segments whose current point is the same
     * as that of a previous PathIterator.SEG_MOVETO segment.  The
     * test ignores any segments after a second PathIterator.SEG_MOVE,
     * excluding an initial set of back-to-back PathIterator.SEG_MOVE
     * segments, or a first PathIterator.SEG_CLOSE.  If the last point
     * is the segment preceding a PathIterator.SEG_CLOSE segment is
     * equal to t he initial segment (whose type is first
     * PathIterator.SEG_MOVETO), the terminating
     * PathIterator.SEG_CLOSE segment is not included in the count.
     * @param path the path
     * @return true if the path is closed; false otherwise
     * @throws IllegalStateException if the path does not start with a
     *         segment whose type is PathIterator.SEG_MOVE.
     */
    public static int numberOfDrawableSegments(Path3D path) {
	double[] tmp = new double[9];
	PathIterator3D pi = path.getPathIterator(null);
	int count = 0;
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	if (!pi.isDone()) {
	    do {
		// count++;
		if (pi.currentSegment(tmp) == PathIterator.SEG_MOVETO) {
		    lastx = tmp[0]; lasty = tmp[1]; lastz = tmp[2];
		    startx = lastx; starty = lasty; startz = lastz;
		} else {
		    throw new
			IllegalStateException(errorMsg("expectingMoveTo"));
		}
		pi.next();
	    } while (!pi.isDone()
		     && pi.currentSegment(tmp) == PathIterator.SEG_MOVETO);
	}
	while (!pi.isDone()) {
	    switch(pi.currentSegment(tmp)) {
	    case PathIterator.SEG_MOVETO:
		lastx = tmp[0];
		lasty = tmp[1];
		lastz = tmp[2];
		return count;
	    case PathIterator.SEG_LINETO:
		lastx = tmp[0];
		lasty = tmp[1];
		lastz = tmp[2];
		break;
	    case PathIterator.SEG_QUADTO:
		lastx = tmp[3];
		lasty = tmp[4];
		lastz = tmp[5];
		break;
	    case PathIterator.SEG_CUBICTO:
		lastx = tmp[6];
		lasty = tmp[7];
		lasty = tmp[8];
		break;
	    case PathIterator.SEG_CLOSE:
		startx = (double)(float)startx;
		starty = (double)(float)starty;
		startz = (double)(float)startz;
		lastx = (double)(float)lastx;
		lasty = (double)(float)lasty;
		lastz = (double)(float)lastz;
		if (lastx != startx || lasty != starty) count++;
		return count;
	    }
	    count++;
	    pi.next();
	}
	return count;
    }

    static double[] tmp = new double[9];
    /**
     * Determine if the first continuous portion of a path is closed.
     * The test ignores any segments
     * @param path the path
     * @return true if the path is closed; false otherwise
     */
    public static boolean isClosed(Path3D  path) {
	PathIterator3D pi = path.getPathIterator(null);

	boolean sawMove = false;
	boolean justSawMove = false;
	while (!pi.isDone()) {
	    int mode = pi.currentSegment(tmp);
	    if (mode == PathIterator.SEG_MOVETO) {
		if (sawMove && !justSawMove) {
		    return false;
		}
		sawMove = true;
		justSawMove = true;
		pi.next();
		continue;
	    }
	    if (mode == PathIterator.SEG_CLOSE) return true;
	    justSawMove = false;
	    pi.next();
	}
	return false;
    }

    /**
     * Get the tangent vector, normal vector, and binormal vector
     * of the first point along a path where all three exist.
     * <P>
     * A path can be represented as a function P(s) that specifies a
     * point as a function of the distance along a path. The tangent
     * vector T = dP/ds points in the direction of an increasing path
     * parameter.  The normal vector is perpendicular to the tangent
     * vector and is equal to (1/&kappa;)(dT/ds) where s is the
     * distance along the path.  The binormal vector B is equal to the
     * cross product T &times; N. All are unit vectors.  Because
     * dT/ds and &kappa;, are zero for a straight-line segment, these
     * segments do not have a unique normal vector. As a result, initial
     * straight-line segments are skipped
     * <P>
     * A precondition is that the path must be such that its tangent
     * vector is a continuous function of the path parameter, and
     * the tangent function should be a differentiable function as well,
     * which implies that successive straight-line segments should
     * have the same tangent vectors.
     * @param tangent the tangent vector; null if not wanted
     * @param normal the normal vector; null if not wanted
     * @param binormal the binormal vector; null if not wanted
     * @return true on success; false on failure
     */
    public static boolean getFirstTNB(Path3D path,
				      double[] tangent,
				      double[] normal,
				      double[] binormal)
    {
	PathIterator3D pi = path.getPathIterator(null);
	double[] coords = new double[9];
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	boolean hasTangent = false;
	boolean hasNormal = false;
	boolean first = true;
	boolean justSawMoveTo = false;

	if (tangent == null) tangent = new double[3];
	if (normal == null) normal = new double[3];
	if (binormal == null) binormal = new double[3];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		startx = coords[0];
		starty = coords[1];
		startz = coords[2];
		lastx = startx;
		lasty = starty;
		lastz = startz;
		if (!first && !justSawMoveTo) break;
		first = false;
		justSawMoveTo = true;
		pi.next();
		continue;
	    case PathIterator3D.SEG_LINETO:
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_LINETO, coords);
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		justSawMoveTo = false;
		pi.next();
		continue;
	    case PathIterator3D.SEG_QUADTO:
		hasNormal = getNormal(0.0, normal, 0, lastx, lasty, lastz,
				      PathIterator3D.SEG_QUADTO, coords);
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		if (!hasNormal) {
		    justSawMoveTo = false;
		    pi.next();
		    continue;
		}
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_QUADTO, coords);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		hasNormal = getNormal(0.0, normal, 0, lastx, lasty, lastz,
				      PathIterator3D.SEG_CUBICTO, coords);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		if (!hasNormal) {
		    hasNormal = getNormal(1.0, normal, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_CUBICTO, coords);
		    if (hasNormal) {
			hasTangent = getTangent(1.0, tangent, 0,
						lastx, lasty, lastz,
						PathIterator3D.SEG_CUBICTO,
						coords);
			break;
		    }
		    justSawMoveTo = false;
		    pi.next();
		    continue;
		}
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_LINETO, coords);
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (startx != lastx || starty != lasty || startz != lastz) {
		    hasTangent = getTangent(0.0, tangent, 0,
					    lastx, lasty, lastz,
					    PathIterator3D.SEG_CLOSE, coords);
		}
		break;
	    }
	    break;
	}
	if (hasNormal) {
	    VectorOps.crossProduct(binormal, tangent, normal);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Get the first point on the first segment of a path where a
     * tangent vector exists.  If the path consists of multiple
     * disjoint components, only the first component is used.
     * @param path the path
     * @param tangent the tangent, stored in x, y, z order
     * @return true if a tangent exists; false otherwise
     */
    public static boolean getStartingTangent(Path3D path, double[] tangent)
    {
	PathIterator3D pi = path.getPathIterator(null);
	double[] coords = new double[9];
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	boolean hasTangent = false;
	boolean first = true;
	boolean justSawMoveTo = false;

	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		startx = coords[0];
		starty = coords[1];
		startz = coords[2];
		lastx = startx;
		lasty = starty;
		lastz = startz;
		if (!first && !justSawMoveTo) break;
		first = false;
		justSawMoveTo = true;
		pi.next();
		continue;
	    case PathIterator3D.SEG_LINETO:
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_LINETO, coords);
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		if (!hasTangent) {
		    justSawMoveTo = false;
		    pi.next();
		    continue;
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_QUADTO, coords);
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		if (!hasTangent) {
		    justSawMoveTo = false;
		    pi.next();
		    continue;
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		hasTangent = getTangent(0.0, tangent, 0, lastx, lasty, lastz,
					PathIterator3D.SEG_CUBICTO, coords);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		if (!hasTangent) {
		    justSawMoveTo = false;
		    pi.next();
		    continue;
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (startx != lastx || starty != lasty || startz != lastz) {
		    hasTangent = getTangent(0.0, tangent, 0,
					    lastx, lasty, lastz,
					    PathIterator3D.SEG_CLOSE, coords);
		}
		break;
	    }
	    break;
	}
	return hasTangent;
    }
}

//  LocalWords:  exbundle af affine eacute zier href coords rOffset
//  LocalWords:  cOffset SegmentData IllegalArgumentException ul li
//  LocalWords:  argOutOfRange PathIterator SEG MOVETO LINETO QUADTO
//  LocalWords:  CUBICTO currentSegment NullPointerException uv ds dx
//  LocalWords:  ArrayIndexOutOfBoundsException stUnknown arg du dy
//  LocalWords:  OutOfRange piUnknown NaN blockquote pre sqrt DInfo
//  LocalWords:  getEntries getSegmentLength lt glq len th lastX dP
//  LocalWords:  integrateWithP segNumbOutOfRange lastMoveToX lastY
//  LocalWords:  lastMoveToY illFormedPath tradeoff lengthOrig dT
//  LocalWords:  appendable Appendable startingX startingY startingZ
//  LocalWords:  endingX endingY endingZ badSegClose shiftClosedPath
//  LocalWords:  PathSplitter binormal Drawable IllegalStateException
//  LocalWords:  expectingMoveTo
