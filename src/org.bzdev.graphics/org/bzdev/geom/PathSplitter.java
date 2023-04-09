package org.bzdev.geom;
import java.awt.geom.*;
import java.util.ArrayList;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Utility class with static methods for splitting paths into
 * subpaths.
 * @see PathIterator
 * @see Path2D
 * @see PathIterator3D
 * @see Path3D
 */
public class PathSplitter {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    // just static
    private PathSplitter() {}

    /**
     * Split a path (2 dimensions).
     * The paths end in two ways. If a path-segment's type is
     * {@link PathIterator#SEG_CLOSE}, a new path starts, optionally with
     * segment whose type is {@link PathIterator#SEG_MOVETO}.  If a
     * {@link PathIterator#SEG_MOVETO} segment is not preceded by a
     * {@link PathIterator#SEG_CLOSE} segment, the
     * {@link PathIterator#SEG_MOVETO} segment becomes the first segment in
     * the next path.
     * @param path the path to split
     * @return an array of subpaths
     */
    public static Path2D[] split(Path2D path) {
	return split(path, null);
    }

    /**
     * Split a path (2 dimensions) with an affine transformation.
     * The paths end in two ways. If a path-segment's type is
     * {@link PathIterator#SEG_CLOSE}, a new path starts, optionally with
     * segment whose type is {@link PathIterator#SEG_MOVETO}.  If a
     * {@link PathIterator#SEG_MOVETO} segment is not preceded by a
     * {@link PathIterator#SEG_CLOSE} segment, the
     * {@link PathIterator#SEG_MOVETO} segment becomes the first segment in
     * the next path.
     * @param path the path to split
     * @param at the affine transformation to apply to the path's components;
     *        null if there is none
     * @return an array of subpaths
     */
    public static Path2D[] split(Path2D path, AffineTransform at) {
	int windingRule = path.getWindingRule();
	PathIterator pit = path.getPathIterator(at);
	ArrayList<Path2D> list = new ArrayList<>();
	Path2D cp = null;
	if (path instanceof Path2D.Float) {
	    float[] coords = new float[6];
	    float  lastX = 0.0F;
	    float lastY = 0.0F;
	    boolean needNextPath  = true;
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		if (type != PathIterator.SEG_MOVETO && needNextPath) {
		    cp = new Path2D.Float(windingRule);
		    list.add(cp);
		    cp.moveTo(lastX, lastY);
		    needNextPath = false;
		}
		switch(type) {
		case PathIterator.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    cp = new Path2D.Float(windingRule);
		    list.add(cp);
		    cp.moveTo(lastX, lastY);
		    needNextPath = false;
		    break;
		case PathIterator.SEG_CLOSE:
		    cp.closePath();
		    needNextPath = true;
		    break;
		case PathIterator.SEG_LINETO:
		    cp.lineTo(coords[0], coords[1]);
		    break;
		case PathIterator.SEG_QUADTO:
		    cp.quadTo(coords[0], coords[1], coords[2], coords[3]);
		    break;
		case PathIterator.SEG_CUBICTO:
		    cp.curveTo(coords[0], coords[1], coords[2], coords[3],
			       coords[4], coords[5]);
		    break;
		}
		pit.next();
	    }
	} else {
	    double[] coords = new double[6];
	    double  lastX = 0.0;
	    double lastY = 0.0;
	    boolean needNextPath  = true;
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		if (type != PathIterator.SEG_MOVETO && needNextPath) {
		    cp = new Path2D.Double(windingRule);
		    list.add(cp);
		    cp.moveTo(lastX, lastY);
		    needNextPath = false;
		}
		switch(type) {
		case PathIterator.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    cp = new Path2D.Double(windingRule);
		    list.add(cp);
		    cp.moveTo(lastX, lastY);
		    needNextPath = false;
		    break;
		case PathIterator.SEG_CLOSE:
		    cp.closePath();
		    needNextPath = true;
		    break;
		case PathIterator.SEG_LINETO:
		    cp.lineTo(coords[0], coords[1]);
		    break;
		case PathIterator.SEG_QUADTO:
		    cp.quadTo(coords[0], coords[1], coords[2], coords[3]);
		    break;
		case PathIterator.SEG_CUBICTO:
		    cp.curveTo(coords[0], coords[1], coords[2], coords[3],
			       coords[4], coords[5]);
		    break;
		}
		pit.next();
	    }
	}
	return list.toArray(new Path2D[list.size()]);
    }


    /**
     * Split a path (3 dimensions).
     * The paths end in two ways. If a path-segment's type is
     * {@link PathIterator3D#SEG_CLOSE}, a new path starts, optionally with
     * segment whose type is {@link PathIterator3D#SEG_MOVETO}.  If a
     * {@link PathIterator3D#SEG_MOVETO} segment is not preceded by a
     * {@link PathIterator3D#SEG_CLOSE} segment, the
     * {@link PathIterator3D#SEG_MOVETO} segment becomes the first segment in
     * the next path.
     * @param path the path to split
     * @return an array of subpaths
     */
    public static Path3D[] split(Path3D path) {
	return split(path, null);
    }


    /**
     * Split a path (3 dimensions) with an affine transformation.
     * The paths end in two ways. If a path-segment's type is
     * {@link PathIterator3D#SEG_CLOSE}, a new path starts, optionally with
     * segment whose type is {@link PathIterator3D#SEG_MOVETO}.  If a
     * {@link PathIterator3D#SEG_MOVETO} segment is not preceded by a
     * {@link PathIterator3D#SEG_CLOSE} segment, the
     * {@link PathIterator3D#SEG_MOVETO} segment becomes the first segment in
     * the next path.
     * @param path the path to split
     * @param at the affine transformation to apply to the path's components;
     *        null if there is none
     * @return an array of subpaths
     */
    public static Path3D[] split(Path3D path, AffineTransform3D at) {
	PathIterator3D pit = path.getPathIterator(at);
	ArrayList<Path3D> list = new ArrayList<>();
	Path3D cp = null;
	if (path instanceof Path3D.Float) {
	    float[] coords = new float[9];
	    float lastX = 0.0F;
	    float lastY = 0.0F;
	    float lastZ = 0.0F;
	    boolean needNextPath  = true;
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		if (type != PathIterator3D.SEG_MOVETO && needNextPath) {
		    cp = new Path3D.Float();
		    list.add(cp);
		    cp.moveTo(lastX, lastY, lastZ);
		    needNextPath = false;
		}
		switch(type) {
		case PathIterator3D.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    lastZ = coords[2];
		    cp = new Path3D.Float();
		    list.add(cp);
		    cp.moveTo(lastX, lastY, lastZ);
		    needNextPath = false;
		    break;
		case PathIterator3D.SEG_CLOSE:
		    cp.closePath();
		    needNextPath = true;
		    break;
		case PathIterator3D.SEG_LINETO:
		    cp.lineTo(coords[0], coords[1], coords[2]);
		    break;
		case PathIterator3D.SEG_QUADTO:
		    cp.quadTo(coords[0], coords[1], coords[2],
			      coords[3], coords[4], coords[5]);
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    cp.curveTo(coords[0], coords[1], coords[2],
			       coords[3], coords[4], coords[5],
			       coords[6], coords[7], coords[8]);
		    break;
		}
		pit.next();
	    }
	} else {
	    double[] coords = new double[9];
	    double  lastX = 0.0;
	    double lastY = 0.0;
	    double lastZ = 0.0;
	    boolean needNextPath  = true;
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		if (type != PathIterator3D.SEG_MOVETO && needNextPath) {
		    cp = new Path3D.Double();
		    list.add(cp);
		    cp.moveTo(lastX, lastY, lastZ);
		    needNextPath = false;
		}
		switch(type) {
		case PathIterator3D.SEG_MOVETO:
		    lastX = coords[0];
		    lastY = coords[1];
		    lastZ = coords[2];
		    cp = new Path3D.Double();
		    list.add(cp);
		    cp.moveTo(lastX, lastY, lastZ);
		    needNextPath = false;
		    break;
		case PathIterator3D.SEG_CLOSE:
		    cp.closePath();
		    needNextPath = true;
		    break;
		case PathIterator3D.SEG_LINETO:
		    cp.lineTo(coords[0], coords[1], coords[2]);
		    break;
		case PathIterator3D.SEG_QUADTO:
		    cp.quadTo(coords[0], coords[1], coords[2],
			      coords[3],coords[4], coords[5]);
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    cp.curveTo(coords[0], coords[1], coords[2],
			       coords[3], coords[4], coords[5],
			       coords[6], coords[7], coords[8]);
		    break;
		}
		pit.next();
	    }
	}
	return list.toArray(new Path3D[list.size()]);
    }

    /**
     * Split a cubic B&eacute;zier curve with 2 dimensions into two parts
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, and Y coordinates of each control point are adjacent in
     * the coords array in that order. the 0<sup>th</sup> control point's
     * coordinates are (startX,startY). The coords array provides the other
     * control points with the first followed by the second.
     * This is the same order used by
     * {@link java.awt.geom.PathIterator#currentSegment(double[])}.
     * <P>
     * The two curves obtained from splitting the original curve are
     * called the first and second curve and have control points
     * stored the the scords argument. The 0<sup>th</sup> control
     * point of the first curve is the the point (startX, startY) and
     * is not copied into the scoords argument.  The scoords argument
     * contains the first control point of the first curve, followed
     * by its second and third control points.  The third control
     * point is also the 0<sup>th</sup> control point of the second
     * curve.  This is followed in turn by the first, second, and
     * third control point of the second curve.  For each control
     * point stored in the scoords array, the array contains that
     * control point's X coordinate, followed by its Y coordinate.
     * <P>
     * The minimum length for the coords array is 6 and the minimum
     * length for the scoords array is 12.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitCubic(double startX, double startY,
				  double[] coords, int offset,
				  double[] scoords, int soffset,
				  double u)
    {
	int off1 = offset;
	int off2 = offset;
	double tmp1;
	double tmp2 = coords[off2];
	final double tmpx10 = startX*(1-u) + tmp2*u;

	off2++;
	tmp2 = coords[off2];
	final double tmpy10 = startY*(1-u) + tmp2*u;

	off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx12 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy12 = tmp1*(1-u) + tmp2*u;

	final double tmpx20 = tmpx10*(1-u) + tmpx11*u;
	final double tmpy20 = tmpy10*(1-u) + tmpy11*u;

	final double tmpx21 = tmpx11*(1-u) + tmpx12*u;
	final double tmpy21 = tmpy11*(1-u) + tmpy12*u;

	final double tmpx30 = tmpx20*(1-u) + tmpx21*u;
	final double tmpy30 = tmpy20*(1-u) + tmpy21*u;

	scoords[soffset++] = tmpx10;
	scoords[soffset++] = tmpy10;
	scoords[soffset++] = tmpx20;
	scoords[soffset++] = tmpy20;
	scoords[soffset++] = tmpx30;
	scoords[soffset++] = tmpy30;
	scoords[soffset++] = tmpx21;
	scoords[soffset++] = tmpy21;
	scoords[soffset++] = tmpx12;
	scoords[soffset++] = tmpy12;
	scoords[soffset++] = coords[offset+4];
	scoords[soffset++] = coords[offset+5];
    }

    /**
     * Split a cubic B&eacute;zier curve with 3 dimensions into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, Y, and Z coordinates of each control point are adjacent
     * in the coords array in that order. the 0<sup>th</sup> control
     * point's coordinates are (startX,startY,startZ). The coords
     * array provides the other control points with the first followed
     * by the second, in turn followed by the third. This is the same order
     * used by
     * {@link PathIterator3D#currentSegment(double[])}.
     * <P>
     * The scoords argument contains control points from the split
     * curves, denoted as the first curve and second curve.  The
     * scoords argument contains the first control point of the first
     * curve, followed by its second and third control points.  This
     * third control point is also the 0<sup>th</sup> control point of
     * the second curve. The remainder of the scoords array contains
     * the first control point of the second curve followed by the
     * second control point of the second curve in turn followed by
     * the third control point of the second curve. For each control
     * point, the array contains that control point's X coordinate,
     * followed by its Y coordinate, and finally its Z coordinate.
     * The 0<sup>th</sup> control point for the original curve is the
     * 0<sup>th</sup> control point for the first curve and is not
     * copied into the scoords array.
     * <P>
     * The minimum length for the coords array is 9 and the minimum
     * length for the scoords array is 18.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param startZ the starting Z coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitCubic(double startX, double startY, double startZ,
				  double[] coords, int offset,
				  double[] scoords, int soffset,
				  double u)
    {
	int off1 = offset;
	int off2 = offset;
	double tmp1;
	double tmp2 = coords[off2];
	final double tmpx10 = startX*(1-u) + tmp2*u;

	off2++;
	tmp2 = coords[off2];
	final double tmpy10 = startY*(1-u) + tmp2*u;

        off2++;
	tmp2 = coords[off2];
	final double tmpz10 = startZ*(1-u) + tmp2*u;

	off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpz11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx12 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy12 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpz12 = tmp1*(1-u) + tmp2*u;

	final double tmpx20 = tmpx10*(1-u) + tmpx11*u;
	final double tmpy20 = tmpy10*(1-u) + tmpy11*u;
	final double tmpz20 = tmpz10*(1-u) + tmpz11*u;


	final double tmpx21 = tmpx11*(1-u) + tmpx12*u;
	final double tmpy21 = tmpy11*(1-u) + tmpy12*u;
	final double tmpz21 = tmpz11*(1-u) + tmpz12*u;

	final double tmpx30 = tmpx20*(1-u) + tmpx21*u;
	final double tmpy30 = tmpy20*(1-u) + tmpy21*u;
	final double tmpz30 = tmpz20*(1-u) + tmpz21*u;

	scoords[soffset++] = tmpx10;
	scoords[soffset++] = tmpy10;
	scoords[soffset++] = tmpz10;
	scoords[soffset++] = tmpx20;
	scoords[soffset++] = tmpy20;
	scoords[soffset++] = tmpz20;
	scoords[soffset++] = tmpx30;
	scoords[soffset++] = tmpy30;
	scoords[soffset++] = tmpz30;
	scoords[soffset++] = tmpx21;
	scoords[soffset++] = tmpy21;
	scoords[soffset++] = tmpz21;
	scoords[soffset++] = tmpx12;
	scoords[soffset++] = tmpy12;
	scoords[soffset++] = tmpz12;
	scoords[soffset++] = coords[offset+6];
	scoords[soffset++] = coords[offset+7];
	scoords[soffset++] = coords[offset+8];
    }

    /**
     * Split a 2-dimensional line segment into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * A line segment has two control points: the point at its start and
     * the point at its end. The array coords contains the X and Y
     * coordinate of the control point at the end of the line, placed in
     * the array in that order.
     * The scoords array contains the point corresponding to the value
     * of u, with the X, and Y coordinates of that point listed
     * in that order.  The remainder contains the X and Y coordinates,
     * again in that order, for the final point in the original line
     * segment.
     * <P>
     * The minimum length of the coords array is 2 and the minimum
     * lnegth of the scoords array is 4.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param coords an array containing the remaining control point
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitLine(double startX, double startY,
				  double[] coords, int offset,
				  double[] scoords, int soffset,
				  double u)
    {
	double tmp1 = coords[offset++];
	double tmp2 = coords[offset++];
	scoords[soffset++] = startX*(1-u) + tmp1*u;
	scoords[soffset++] = startY*(1-u) + tmp2*u;
	scoords[soffset++] = tmp1;
	scoords[soffset++] = tmp2;
    }

    /**
     * Split a 3-dimensional line segment into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * A line segment has two control points: the point at its start and
     * the point at its end. The array coords contains the X, Y, and Z
     * coordinate of the control point at the end of the line, placed in
     * the array in that order.
     * The scoords array contains the point corresponding to the value
     * of u, with the X, Y, and Z coordinates of that point listed
     * in that order.  The remainder contains the X, Y, and Z coordinates,
     * again in that order, for the final point in the original line
     * segment.
     * <P>
     * The minimum length of the coords array is 3 and the minimum
     * lnegth of the scoords array is 6.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param startZ the starting Z coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitLine(double startX, double startY, double startZ,
				  double[] coords, int offset,
				  double[] scoords, int soffset,
				  double u)
    {
	double tmp1 = coords[offset++];
	double tmp2 = coords[offset++];
	double tmp3 = coords[offset++];
	scoords[soffset++] = startX*(1-u) + tmp1*u;
	scoords[soffset++] = startY*(1-u) + tmp2*u;
	scoords[soffset++] = startZ*(1-u) + tmp3*u;
	scoords[soffset++] = tmp1;
	scoords[soffset++] = tmp2;
	scoords[soffset++] = tmp3;
    }




    /**
     * Split a quadratic B&eacute;zier curve with 2 dimensions into two parts
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, and Y coordinates of each control point are adjacent in
     * the coords array in that order. the 0<sup>th</sup> control point's
     * coordinates are (startX,startY). The coords array provides the other
     * control points with the first followed by the second. This is the
     * same order used by
     * {@link java.awt.geom.PathIterator#currentSegment(double[])}.
     * <P>
     * The scoords argument contains control points from the split
     * curves, denoted as the first curve and second curve.  The
     * scoords argument contains the first control point of the first
     * curve, followed by its second control point The third control
     * point is also the 0<sup>th</sup> control point of the second
     * curve. The remainder of the scoords array contains the first
     * control point of the second curve followed by the second
     * control point of the second curve.  For each control point, the
     * array contains that control point's X coordinate, followed by
     * its Y coordinate.  The 0<sup>th</sup> control point is not
     * copied into the scoords array.
     * <P>
     * The minimum length for the coords array is 6 and the minimum
     * length for the scoords array is 12.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitQuad(double startX, double startY,
				 double[] coords, int offset,
				 double[] scoords, int soffset,
				 double u)
    {
	int off1 = offset;
	int off2 = offset;
	double tmp1;
	double tmp2 = coords[off2];

	final double tmpx10 = startX*(1-u) + tmp2*u;

	off2++;
	tmp2 = coords[off2];
	final double tmpy10 = startY*(1-u) + tmp2*u;

	off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy11 = tmp1*(1-u) + tmp2*u;

	final double tmpx20 = tmpx10*(1-u) + tmpx11*u;
	final double tmpy20 = tmpy10*(1-u) + tmpy11*u;

	scoords[soffset++] = tmpx10;
	scoords[soffset++] = tmpy10;
	scoords[soffset++] = tmpx20;
	scoords[soffset++] = tmpy20;
	scoords[soffset++] = tmpx11;
	scoords[soffset++] = tmpy11;
	scoords[soffset++] = coords[offset+2];
	scoords[soffset++] = coords[offset+3];
    }

    /**
     * Split a quadratic B&eacute;zier curve with 3 dimensions into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, Y, and Z coordinates of each control point are adjacent
     * in the coords array in that order. the 0<sup>th</sup> control
     * point's coordinates are (startX,startY,startZ). The coords
     * array provides the other control points with the first followed
     * by the second. This is the same order used by
     * {@link PathIterator3D#currentSegment(double[])}.
     * <P>
     * The scoords argument contains control points from the split
     * curves, denoted as the first curve and second curve.  The
     * scoords argument contains the first control point of the first
     * curve, followed by its second control point.  This second
     * control point is also the 0<sup>th</sup> control point of the
     * second curve. The remainder of the scoords array contains the
     * first control point of the second curve followed by the second
     * control point of the second curve. For each control point, the
     * array contains that control point's X coordinate, followed by
     * its Y coordinate, and finally its Z coordinate.  The
     * 0<sup>th</sup> control point for the original curve is the
     * 0<sup>th</sup> control point for the first curve and is not
     * copied into the scoords array.
     * <P>
     * The minimum length for the coords array is 9 and the minimum
     * length for the scoords array is 18.
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param startZ the starting Z coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    private static void splitQuad(double startX, double startY, double startZ,
				  double[] coords, int offset,
				  double[] scoords, int soffset,
				  double u)
    {
	int off1 = offset;
	int off2 = offset;
	double tmp1;
	double tmp2 = coords[off2];
	final double tmpx10 = startX*(1-u) + tmp2*u;

	off2++;
	tmp2 = coords[off2];
	final double tmpy10 = startY*(1-u) + tmp2*u;

	off2++;
	tmp2 = coords[off2];
	final double tmpz10 = startZ*(1-u) + tmp2*u;

	off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpx11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpy11 = tmp1*(1-u) + tmp2*u;

	off1++; off2++;
	tmp1 = coords[off1];
	tmp2 = coords[off2];
	final double tmpz11 = tmp1*(1-u) + tmp2*u;

	final double tmpx20 = tmpx10*(1-u) + tmpx11*u;
	final double tmpy20 = tmpy10*(1-u) + tmpy11*u;
	final double tmpz20 = tmpz10*(1-u) + tmpz11*u;

	scoords[soffset++] = tmpx10;
	scoords[soffset++] = tmpy10;
	scoords[soffset++] = tmpz10;
	scoords[soffset++] = tmpx20;
	scoords[soffset++] = tmpy20;
	scoords[soffset++] = tmpz20;
	scoords[soffset++] = tmpx11;
	scoords[soffset++] = tmpy11;
	scoords[soffset++] = tmpz11;
	scoords[soffset++] = coords[offset+3];
	scoords[soffset++] = coords[offset+4];
	scoords[soffset++] = coords[offset+5];
    }

/**
     * Split a path segment with 2 dimensions into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the start of the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, and Y coordinates of each control point are adjacent
     * in the coords array in that order. the 0<sup>th</sup> control
     * point's coordinates are (startX,startY). The coords
     * array provides the other control points with the first followed
     * by the second. This is the same order used by
     * {@link PathIterator#currentSegment(double[])}.
     * <P>
     * The scoords argument contains control points from the split
     * curves, denoted as the first curve and second curve.  The
     * scoords argument contains the first control point of the first
     * curve, followed by its second and control point if present.
     * This last control point is also the 0<sup>th</sup> control point
     * of the second curve. The remainder of the scoords array contains the
     * first control point of the second curve followed by the second
     * and control points, if present, of the second curve. For eac
     *  control point, the array contains that control point's
     * X coordinate, followed by its Y coordinate.  The
     * 0<sup>th</sup> control point for the original curve is the
     * 0<sup>th</sup> control point for the first curve and is not
     * copied into the scoords array.
     * <P>
     * The minimum length for the coords array is 6 and the minimum
     * length for the scoords array is 12.
     * @param type the segment type ({@link PathIterator#SEG_LINETO},
     *        {@link PathIterator#SEG_QUADTO}, or
     *        {@link PathIterator#SEG_CUBICTO})
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     * @exception IllegalArgumentException a parameter was out of range
     */
    public static void split(int type,
			     double startX, double startY,
			     double[] coords, int offset,
			     double[] scoords, int soffset,
			     double u)
    {
	switch(type) {
	case PathIterator.SEG_LINETO:
	    splitLine(startX, startY, coords, offset, scoords, soffset, u);
	    break;
	case PathIterator.SEG_QUADTO:
	    splitQuad(startX, startY, coords, offset, scoords, soffset, u);
	    break;
	case PathIterator.SEG_CUBICTO:
	    splitCubic(startX, startY, coords, offset, scoords, soffset, u);
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Split a path segment with 3 dimensions into two parts.
     * The path parameter u is the fractional distance at which the split
     * occurs, measured from the startof the line segment to its end.
     * The parameter u must be in the range [0,1].
     * The X, Y, and Z coordinates of each control point are adjacent
     * in the coords array in that order. the 0<sup>th</sup> control
     * point's coordinates are (startX,startY,startZ). The coords
     * array provides the other control points with the first followed
     * by the second. This is the same order used by
     * {@link PathIterator3D#currentSegment(double[])}.
     * <P>
     * The scoords argument contains control points from the split
     * curves, denoted as the first curve and second curve.  The
     * scoords argument contains the first control point of the first
     * curve, followed by its second and third control points if present.
     * The last of these control points
     * is also the 0<sup>th</sup> control point of the
     * second curve. The remainder of the scoords array contains the
     * first control point of the second curve followed by the second
     * and thir control point of the second curve if present. For each
     * control point, the array contains that control point's
     * X coordinate, followed by its Y coordinate, and finally
     * its Z coordinate.  The* 0<sup>th</sup> control point for the
     * original curve is the 0<sup>th</sup> control point for the
     * first curve and is not copied into the scoords array.
     * <P>
     * The minimum length for the coords array is 9 and the minimum
     * length for the scoords array is 18.
     * @param type the segment type ({@link PathIterator3D#SEG_LINETO},
     *        {@link PathIterator3D#SEG_QUADTO}, or
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param startZ the starting Z coordinate
     * @param coords an array containing the remaining control points
     * @param offset the offset for the 1<sup>st</sup> control point
     * @param scoords an array containing the control points for
     *        a pair of B&eacute;zier curves.
     * @param soffset the offset for the 0<sup>th</sup> control point
     *        of the first cubic B&ecute;zier curve of the pair of
     *        curves created
     * @param u the path parameter at which a curve is to be split
     */
    public static void split(int type,
			     double startX, double startY, double startZ,
			     double[] coords, int offset,
			     double[] scoords, int soffset,
			     double u)
    {
	switch(type) {
	case PathIterator3D.SEG_LINETO:
	    splitLine(startX, startY, startZ,
		      coords, offset, scoords, soffset, u);
	    break;
	case PathIterator3D.SEG_QUADTO:
	    splitQuad(startX, startY, startZ,
		      coords, offset, scoords, soffset, u);
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    splitCubic(startX, startY, startZ,
		       coords, offset, scoords, soffset, u);
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
    }

    /**
     * Split a B&eacute;zier curve of degree n at a point t&isin;[0,1].
     * After the split, the first curve's initial point will be (x, y)
     * and its remaining control points will be in the array <CODE>first</CODE>.
     * The second curve's initial point will be returned and its
     * remaining control points will be in the array <CODE>second</CODE>.
     * For all the arrays, the initial point of the curve is excluded and
     * the array contains the remaining control points in order, with X
     * values immediately followed by the corresponding Y values,
     * the same convention used by the {@link java.awt.geom.PathIterator}
     * interface.
     * <P>
     * See <A href="https://pomax.github.io/bezierinfo/#splitting"> A
     * Primer on B&eacute;zier Curves</A> for a description of how to
     * use de Casteljau's algorithm for splitting a B&eacute;zier curve.
     * @param first the control point array for the first curve; null if
     *        these control points should not be computed
     * @param second the control point array for the second curve; null if
     *        these control points should not be computed
     * @param t the point at which the curve is split
     * @param degree the degree of the initial B&eacute;zier curve
     * @param x the starting X coordinate for the initial B&eacute;zier curve
     * @param y the starting Y coordinate for the initial B&eacute;zier curve
     * @param coords the coordinates for the remaining control points
     *        for the initial B&eacute;zier curve, where the control
     *        points appear in order as pairs of X followed by Y
     *        coordinates
     * @return the initial point of the second curve; null if the argument
     *        <CODE>second</CODE> is null
     */

    public static Point2D split(double[] first, double[] second,
				double t, int degree,
				double x, double y, double[] coords)
    {
	double[] tmp = new double[2*(degree+1)];
	tmp[0] = x;
	tmp[1] = y;
	for (int i = 0; i < 2*degree; i++) {
	    int k = i+2;
	    tmp[k] = coords[i];
	}
	double[] tmp2 = new double[tmp.length];
	double[] tmp3 = null;
	int n = degree;
	int nf = 0;
	int nl = degree-1;
	int m = 0;
	boolean firstTime = true;

	while (nf < degree) {
	    if (n == 0) {
		if (first != null) {
		    first[2*nf] = tmp[0];
		    first[2*nf+1] = tmp[1];
		}
		return (second == null)? null:
		    new Point2D.Double(tmp[0], tmp[1]);
	    } else {
		for (int i = 0; i < n; i++) {
		    if (i == 0) {
			if (firstTime) {
			    firstTime = false;
			} else if (first != null) {
			    first[2*nf] = tmp[0];
			    first[2*nf+1] = tmp[1];
			    nf++;
			}
		    }
		    if (second != null && i == n-1) {
			second[2*nl] = tmp[2*n];
			second[2*nl+1] = tmp[2*n+1];
			nl--;
		    }
		    double omt = 1.0 - t;
		    int ip1 = i+1;
		    tmp2[2*i] = omt*tmp[2*i] + t * tmp[2*ip1];
		    tmp2[2*i+1] = omt*tmp[2*i+1] + t * tmp[2*ip1+1];
		}
		// swap tmp and tmp2.
		tmp3 = tmp;
		tmp = tmp2;
		tmp2 = tmp3;
		n--;
	    }
	}
	return null;
    }


    /**
     * Get a subpath of a path with 2 dimensions.
     * The path returned will be the same as a portion of the
     * path provided in the first argument. The path parameters
     * are those for the path provided by this method's first
     * argument.  For the i<sup>th</sup> segment in a path,
     * The value of a path parameter is (i + u) where u &isin; [0,1].
     * @param path the path whose subpath will be computed
     * @param u1 the path parameter determining the
     *        start of the new path
     * @param u2 the path parameter determining the
     *        end of the new path.
     * @return the subpath
     */
    public static Path2D subpath(Path2D path, double u1, double u2)
	throws IllegalArgumentException
    {
	// System.out.println("u1 = " + u1 + ", u2 = " + u2);
	if (u1 > u2) {
	    throw new IllegalArgumentException(errorMsg("u1GTu2"));
	}
	int start = (int) Math.round(Math.floor(u1));
	int end = (int) Math.round(Math.floor(u2));
	double uu1 = u1 - start;
	if (Math.abs(u1 - (start + 1)) < 1.e-10) {
	    start++;
	    uu1 = u1 - start;
	    if (Math.abs(uu1) < 1.e-10) uu1 = 0.0;
	} else if (Math.abs(uu1) < 1.e-10) {
	    uu1 = 0.0;
	}
	double uu2 = u2 - end;
	if (Math.abs(uu2) < 1.e-10) uu2 = 0.0;
	return subpath(path, start, uu1, end, uu2);
    }

    /**
     * Get a subpath of a path with 2 dimensions, specifying
     * path parameters by a segment index and a path parameter specific
     * to that index.
     * @param path the path whose subpath will be computed
     * @param start the index for the path segment determining
     *        the start of the new path
     * @param u1 the path parameter for segment <code>start</code>
     *        determining the start of the new path, with a range of [0,1]
     * @param end the index for the path segment determining
     *        the end of the new path
     * @param u2 the path parameter for segment <code>end</code>  determining
     *        the end of the new path, with a range of [0,1]
     * @return the subpath
     */
    public static Path2D subpath(Path2D path, int start, double u1,
			     int end, double u2)
    {
	Path2D p;
	if (start > end) {
	    throw new IllegalArgumentException(errorMsg("startGTend"));
	}
	if (start < 0) {
	    throw new IllegalArgumentException(errorMsg("startNegative"));
	}
	if (end < 0) {
	    throw new IllegalArgumentException(errorMsg("endNegative"));
	}
	if (path instanceof Path2D.Double) {
	    p = new Path2D.Double(path.getWindingRule());
	} else if (path instanceof Path2D.Float) {
	    p = new Path2D.Float(path.getWindingRule());
	} else {
	    p = (Path2D)path.clone();
	    p.reset();
	}
	PathIterator pit = path.getPathIterator(null);
	if (pit.isDone()) {
	    throw new IllegalStateException(errorMsg("iterationError"));
	}
	if (end > start && Math.abs(u2) < 1.e-10) {
	    end--;
	    u2 = 1.0;
	}

	/*
	System.out.format("start = %d, u1 = %s, end = %d, u2 = %s\n",
			  start, u1, end, u2);
	*/

	double[] coords = new double[6];
	double[] scoords = new double[24];
	double lastX = 0.0;
	double lastY = 0.0;
	double x = 0;
	double y = 0;
	int type = pit.currentSegment(coords);
	if (type == PathIterator.SEG_MOVETO) {
	    lastX = coords[0];
	    lastY = coords[1];
	    x = lastX;
	    y = lastY;
	    if (end == 0 && u2 == 0.0) {
		p.moveTo(x, y);
		return p;
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	int index = -1;
	while (index < start) {
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		if (index != -1) {
		    throw new IllegalArgumentException(errorMsg("needMoveTo"));
		}
		break;
	    case PathIterator.SEG_CLOSE:
		throw new
		    IllegalArgumentException(errorMsg("misplacedClose"));
	    case PathIterator.SEG_LINETO:
		x = coords[0];
		y = coords[1];
		break;
	    case PathIterator.SEG_QUADTO:
		x = coords[2];
		y = coords[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		x = coords[4];
		y = coords[5];
		break;
	    }
	    pit.next();
	    if (pit.isDone()) {
		throw new IllegalArgumentException(errorMsg("iterationError"));
	    }
	    type = pit.currentSegment(coords);
	    index++;
	}
	switch(type) {
	case PathIterator.SEG_MOVETO:
	    throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	case PathIterator.SEG_CLOSE:
	    /*
	    System.out.format("end = %d, u2 = %g, index =%d\n", end, u2,
			      index);
	    */
	    if (x == lastX && y == lastY) {
		throw new IllegalArgumentException(errorMsg("badClose"));
	    } else if (index == end) {
		coords[0] = lastX;
		coords[1] = lastY;
		splitLine(x, y, coords, 0, scoords, 0, u1);
		p.moveTo(scoords[0],scoords[1]);
		u2 = (u2 - u1) / (1.0 - u1);
		splitLine(scoords[0], scoords[1], scoords, 2, scoords, 4, u2);
		p.lineTo(scoords[4], scoords[5]);
		return p;
	    } else {
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    }
	case PathIterator.SEG_LINETO:
	    splitLine(x, y, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[0], scoords[1]);
	    // System.out.format("moveTo (%g, %g)\n", scoords[0], scoords[1]);
	    if (index < end) {
		x = coords[0];
		y = coords[1];
		p.lineTo(x, y);
		/*
		System.out.format("(%d < %d): lineTo (%g, %g)\n",
				  index, end, x, y);
		*/
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitLine(scoords[0], scoords[1], scoords, 2, scoords, 4, u2);
		p.lineTo(scoords[4], scoords[5]);
		/*
		System.out.format("(%d >= %d): lineTo (%g, %g)\n",
				  index, end, scoords[4], scoords[5]);
		*/
		return p;
	    }
	    break;
	case PathIterator.SEG_QUADTO:
	    splitQuad(x, y, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[2], scoords[3]);
	    if (index < end) {
		p.quadTo(scoords[4], scoords[5], scoords[6], scoords[7]);
		x = coords[2];
		y = coords[3];
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitQuad(scoords[2], scoords[3], scoords, 4, scoords, 8, u2);
		p.quadTo(scoords[8], scoords[9], scoords[10], scoords[11]);
		return p;
	    }
	    break;
	case PathIterator.SEG_CUBICTO:
	    splitCubic(x, y, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[4], scoords[5]);
	    if (index < end) {
		p.curveTo(scoords[6], scoords[7],
			  scoords[8], scoords[9],
			  scoords[10], scoords[11]);
		x = coords[4];
		y = coords[5];
		/*
		System.out.format("cubic from (%g,%g) to (%g,%g)\n",
				  scoords[4], scoords[5],
				  scoords[10], scoords[11]);
		*/
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitCubic(scoords[4], scoords[5], scoords, 6,
			   scoords, 12, u2);
		p.curveTo(scoords[12], scoords[13],
			  scoords[14], scoords[15],
			  scoords[16], scoords[17]);
		/*
		System.out.format("returning from a cubic: "
				  + "(%g,%g) to (%g, %g)\n",
				  scoords[4],scoords[5], scoords[16],
				  scoords[17]);
		*/
		return p;
	    }
	    break;
	}
	int endm1 = end-1;
	while(index < endm1) {
	    pit.next();
	    if (pit.isDone()) {
		throw new IllegalArgumentException(errorMsg("iterationError"));
	    }
	    type = pit.currentSegment(coords);
	    index++;
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	    case PathIterator.SEG_CLOSE:
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    case PathIterator.SEG_LINETO:
		p.lineTo(coords[0], coords[1]);
		// System.out.format("lineTo (%g, %g)\n", coords[0], coords[1]);
		x = coords[0];
		y = coords[1];
		break;
	    case PathIterator.SEG_QUADTO:
		p.quadTo(coords[0], coords[1], coords[2], coords[3]);
		x = coords[2];
		y = coords[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		p.curveTo(coords[0], coords[1], coords[2], coords[3],
			  coords[4], coords[5]);
		x = coords[4];
		y = coords[5];
		break;
	    }
	}
	// index == end
	if (Math.abs(u2) < 1.e-10) return p;
	pit.next();
	if (pit.isDone()) {
	    throw new IllegalArgumentException(errorMsg("iterationError"));
	}
	type = pit.currentSegment(coords);
	switch(type) {
	case PathIterator.SEG_MOVETO:
	    throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	case PathIterator.SEG_CLOSE:
	    if (x == lastX && y == lastY) {
		throw new IllegalArgumentException(errorMsg("badClose"));
	    }
	    if (u2 <=  1.0) {
		coords[0] = lastX;
		coords[1] = lastY;
		/*
		System.out.format("setting to lastX = %g and lastY = %g\n",
				  lastX, lastY);
		*/
	    } else {
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    }
	case PathIterator.SEG_LINETO:
	    splitLine(x, y, coords, 0, scoords, 0, u2);
	    p.lineTo(scoords[0], scoords[1]);
	    /*
	    System.out.format("final line segment: (%g,%g) to (%g, %g)\n",
			      x, y, scoords[0], scoords[1]);
	    */
	    break;
	case PathIterator.SEG_QUADTO:
	    splitQuad(x, y, coords, 0, scoords, 0, u2);
	    p.quadTo(scoords[0], scoords[1], scoords[2], scoords[3]);
	    break;
	case PathIterator.SEG_CUBICTO:
	    splitCubic(x, y, coords, 0, scoords, 0, u2);
	    p.curveTo(scoords[0], scoords[1], scoords[2], scoords[3],
		      scoords[4], scoords[5]);
	    /*
	    System.out.format("final cubic segment: (%g,%g) to (%g, %g)\n",
			      x, y, scoords[4], scoords[5]);
	    */
	    break;
	}
	return p;
    }

    /**
     * Get a subpath of a path with three dimensions.
     * The path parameters
     * are those for the path provided by this method's first
     * argument.  For the i<sup>th</sup> segment in a path,
     * The value of a path parameter is (i + u) where u &isin; [0,1].
     * @param path the path whose subpath will be computed
     * @param u1 the path parameter determining the
     *        start of the new path
     * @param u2 the path parameter determining the
     *        end of the new path.
     * @return the subpath
     */
    public static Path3D subpath(Path3D path, double u1, double u2)
	throws IllegalArgumentException
    {
	// System.out.println("u1 = " + u1 + ", u2 = " + u2);
	if (u1 > u2) {
	    throw new IllegalArgumentException(errorMsg("u1GTu2"));
	}
	int start = (int) Math.round(Math.floor(u1));
	int end = (int) Math.round(Math.floor(u2));
	double uu1 = u1 - start;
	if (Math.abs(u1 - (start + 1)) < 1.e-10) {
	    start++;
	    uu1 = u1 - start;
	    if (Math.abs(uu1) < 1.e-10) uu1 = 0.0;
	} else if (Math.abs(uu1) < 1.e-10) {
	    uu1 = 0.0;
	}
	double uu2 = u2 - end;
	if (Math.abs(uu2) < 1.e-10) uu2 = 0.0;
	return subpath(path, start, uu1, end, uu2);
    }

    /**
     * Get a subpath of a path with three dimensions, specifying
     * path parameters by a segment index and a path parameter specific
     * to that index.
     * @param path the path whose subpath will be computed
     * @param start the index for the path segment determining
     *        the start of the new path
     * @param u1 the path parameter for segment <code>start</code>
     *        determining the start of the new path, with a range of [0,1]
     * @param end the index for the path segment determining
     *        the end of the new path
     * @param u2 the path parameter for segment <code>end</code>  determining
     *        the end of the new path, with a range of [0,1]
     * @return the subpath
     */
    public static Path3D subpath(Path3D path, int start, double u1,
			     int end, double u2)
    {
	Path3D p;
	if (start > end) {
	    throw new IllegalArgumentException(errorMsg("startGTend"));
	}
	if (start < 0) {
	    throw new IllegalArgumentException(errorMsg("startNegative"));
	}
	if (end < 0) {
	    throw new IllegalArgumentException(errorMsg("endNegative"));
	}
	if (path instanceof Path3D.Double) {
	    p = new Path3D.Double();
	} else if (path instanceof Path3D.Float) {
	    p = new Path3D.Float();
	} else {
	    p = (Path3D)path.clone();
	    p.reset();
	}
	PathIterator3D pit = path.getPathIterator(null);
	if (pit.isDone()) {
	    throw new IllegalArgumentException(errorMsg("iterationError"));
	}
	if (end > start && Math.abs(u2) < 1.e-10) {
	    end--;
	    u2 = 1.0;
	}

	/*
	System.out.format("start = %d, u1 = %s, end = %d, u2 = %s\n",
			  start, u1, end, u2);
	*/

	double[] coords = new double[9];
	double[] scoords = new double[36];
	double lastX = 0.0;
	double lastY = 0.0;
	double lastZ = 0.0;
	double x = 0.0;
	double y = 0.0;
	double z = 0.0;
	int type = pit.currentSegment(coords);
	if (type == PathIterator.SEG_MOVETO) {
	    lastX = coords[0];
	    lastY = coords[1];
	    lastZ = coords[2];
	    x = lastX;
	    y = lastY;
	    z = lastZ;
	    if (end == 0 && u2 == 0.0) {
		p.moveTo(x, y, z);
		return p;
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("needMoveTo"));
	}
	int index = -1;
	while (index < start) {
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		if (index != -1) {
		    throw new
			IllegalArgumentException(errorMsg("misplacedMoveTo"));
		}
		break;
	    case PathIterator.SEG_CLOSE:
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    case PathIterator.SEG_LINETO:
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    case PathIterator.SEG_QUADTO:
		x = coords[3];
		y = coords[4];
		z = coords[5];
		break;
	    case PathIterator.SEG_CUBICTO:
		x = coords[6];
		y = coords[7];
		z = coords[8];
		break;
	    }
	    pit.next();
	    if (pit.isDone()) {
		throw new IllegalArgumentException(errorMsg("iterationError"));
	    }
	    type = pit.currentSegment(coords);
	    index++;
	}
	switch(type) {
	case PathIterator.SEG_MOVETO:
	    throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	case PathIterator.SEG_CLOSE:
	    /*
	    System.out.format("end = %d, u2 = %g, index =%d\n", end, u2,
			      index);
	    */
	    if (x == lastX && y == lastY) {
		throw new IllegalArgumentException(errorMsg("badClose"));
	    } else if (index == end) {
		coords[0] = lastX;
		coords[1] = lastY;
		coords[2] = lastZ;
		splitLine(x, y, z, coords, 0, scoords, 0, u1);
		p.moveTo(scoords[0],scoords[1], scoords[2]);
		u2 = (u2 - u1) / (1.0 - u1);
		splitLine(scoords[0], scoords[1], scoords[2],
			  scoords, 3, scoords, 6, u2);
		p.lineTo(scoords[6], scoords[7],scoords[8]);
		return p;
	    } else {
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    }
	case PathIterator.SEG_LINETO:
	    splitLine(x, y, z, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[0], scoords[1], scoords[2]);
	    // System.out.format("moveTo (%g, %g)\n", scoords[0], scoords[1]);
	    if (index < end) {
		x = coords[0];
		y = coords[1];
		z = coords[2];
		p.lineTo(x, y, z);
		/*
		System.out.format("(%d < %d): lineTo (%g, %g)\n",
				  index, end, x, y);
		*/
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitLine(scoords[0], scoords[1], scoords[2],
			  scoords, 3, scoords, 6, u2);
		p.lineTo(scoords[6], scoords[7], scoords[8]);
		/*
		System.out.format("(%d >= %d): lineTo (%g, %g)\n",
				  index, end, scoords[4], scoords[5]);
		*/
		return p;
	    }
	    break;
	case PathIterator.SEG_QUADTO:
	    splitQuad(x, y, z, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[3], scoords[4], scoords[5]);
	    if (index < end) {
		p.quadTo(scoords[6], scoords[7], scoords[8],
			 scoords[9], scoords[10], scoords[11]);
		x = coords[3];
		y = coords[4];
		z = coords[5];
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitQuad(scoords[3], scoords[4], scoords[5],
			  scoords, 6, scoords, 12, u2);
		p.quadTo(scoords[12], scoords[13], scoords[14],
			 scoords[15], scoords[16], scoords[17]);
		return p;
	    }
	    break;
	case PathIterator.SEG_CUBICTO:
	    splitCubic(x, y, z, coords, 0, scoords, 0, u1);
	    p.moveTo(scoords[6], scoords[7], scoords[8]);
	    if (index < end) {
		p.curveTo(scoords[9], scoords[10], scoords[11],
			  scoords[12],scoords[13], scoords[14],
			  scoords[15], scoords[16], scoords[17]);
		x = coords[6];
		y = coords[7];
		z = coords[8];
		/*
		System.out.format("cubic from (%g,%g) to (%g,%g)\n",
				  scoords[4], scoords[5],
				  scoords[10], scoords[11]);
		*/
	    } else {
		u2 = (u2 - u1)/(1.0 - u1);
		splitCubic(scoords[6], scoords[7], scoords[8],
			   scoords, 9, scoords, 18, u2);
		p.curveTo(scoords[18], scoords[19], scoords[20],
			  scoords[21], scoords[22], scoords[23],
			  scoords[24], scoords[25], scoords[26]);
		/*
		System.out.format("returning from a cubic: "
				  + "(%g,%g) to (%g, %g)\n",
				  scoords[4],scoords[5], scoords[16],
				  scoords[17]);
		*/
		return p;
	    }
	    break;
	}
	int endm1 = end-1;
	while(index < endm1) {
	    pit.next();
	    if (pit.isDone()) {
		throw new IllegalArgumentException(errorMsg("iterationError"));
	    }
	    type = pit.currentSegment(coords);
	    index++;
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	    case PathIterator.SEG_CLOSE:
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    case PathIterator.SEG_LINETO:
		p.lineTo(coords[0], coords[1], coords[2]);
		// System.out.format("lineTo (%g, %g)\n", coords[0], coords[1]);
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    case PathIterator.SEG_QUADTO:
		p.quadTo(coords[0], coords[1], coords[2],
			 coords[3], coords[4], coords[5]);
		x = coords[3];
		y = coords[4];
		z = coords[5];
		break;
	    case PathIterator.SEG_CUBICTO:
		p.curveTo(coords[0], coords[1], coords[2],
			  coords[3], coords[4], coords[5],
			  coords[6], coords[7], coords[8]);
		x = coords[6];
		y = coords[7];
		z = coords[8];
		break;
	    }
	}
	// index == end
	if (Math.abs(u2) < 1.e-10) return p;
	pit.next();
	if (pit.isDone()) {
	    throw new IllegalArgumentException(errorMsg("iterationError"));
	}
	type = pit.currentSegment(coords);
	switch(type) {
	case PathIterator.SEG_MOVETO:
	    throw new IllegalArgumentException(errorMsg("misplacedMoveTo"));
	case PathIterator.SEG_CLOSE:
	    if (x == lastX && y == lastY && z == lastZ) {
		throw new IllegalArgumentException(errorMsg("badClose"));
	    }
	    if (u2 <=  1.0) {
		coords[0] = lastX;
		coords[1] = lastY;
		coords[2] = lastZ;
		/*
		System.out.format("setting to lastX = %g and lastY = %g\n",
				  lastX, lastY);
		*/
	    } else {
		throw new IllegalArgumentException(errorMsg("misplacedClose"));
	    }
	case PathIterator.SEG_LINETO:
	    splitLine(x, y, z, coords, 0, scoords, 0, u2);
	    p.lineTo(scoords[0], scoords[1], scoords[2]);
	    /*
	    System.out.format("final line segment: (%g,%g) to (%g, %g)\n",
			      x, y, scoords[0], scoords[1]);
	    */
	    break;
	case PathIterator.SEG_QUADTO:
	    splitQuad(x, y, z, coords, 0, scoords, 0, u2);
	    p.quadTo(scoords[0], scoords[1], scoords[2],
		     scoords[3], scoords[4], scoords[5]);
	    break;
	case PathIterator.SEG_CUBICTO:
	    splitCubic(x, y, z, coords, 0, scoords, 0, u2);
	    p.curveTo(scoords[0], scoords[1], scoords[2],
		      scoords[3], scoords[4], scoords[5],
		      scoords[6], scoords[7], scoords[8]);
	    /*
	    System.out.format("final cubic segment: (%g,%g) to (%g, %g)\n",
			      x, y, scoords[4], scoords[5]);
	    */
	    break;
	}
	return p;
    }

}
//  LocalWords:  subpaths SEG MOVETO PathIterator eacute zier coords
//  LocalWords:  th startX startY currentSegment scords scoords ecute
//  LocalWords:  soffset startZ
