package org.bzdev.geom;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bzdev.math.Adder;
// import org.bzdev.math.GLQuadrature;
import org.bzdev.math.VectorOps;
import org.bzdev.math.VectorValuedGLQ;

//@exbundle org.bzdev.geom.lpack.Geom

// https://cg.cs.tsinghua.edu.cn/~shimin/pdf/cagd%202001_bezier.pdf
// for converting a Bezier triangle into a degenerate Bezier patch.

/**
 * A 3D shape consisting of a 'center' point connecting to a path
 * using a sequence of Bezier triangles.  The path will typically
 * be a boundary of some other shape.  The shape of this object is
 * determined by the center point, a vector defining a direction more
 * or less perpendicular to the boundary, and a height relative to the
 * center point and the direction of the vector.
 */
public class BezierCap implements Shape3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    double[][] spokes = null;
    Color[] colors = null;
    double[][] edges = null;
    double[][] cp111 = null;
    double[] center = new double[3];
    double height;
    double[] vector = new double[3];
    Color defaultColor = null;
    Object tag = null;

    // used by getSpoke
    double tmp[] = new double[3];
    double tmp1[] = new double[3];
    double tmp2[] = new double[3];
    double[] circCoords = new double[6];

    private double[] getSpoke(double x, double y, double z, boolean cmode) {
	tmp[0] = x;
	tmp[1] = y;
	tmp[2] = z;
	VectorOps.sub(tmp, tmp, center);
	// System.out.format("tmp = {%g, %g, %g}\n", tmp[0], tmp[1], tmp[2]);
	VectorOps.crossProduct(tmp1, vector, tmp);
	VectorOps.crossProduct(tmp2, tmp1, vector);
	VectorOps.normalize(tmp2);
	double xx = VectorOps.dotProduct(tmp, tmp2);
	double yy = VectorOps.dotProduct(tmp, vector);
	Path2D arc;
	PathIterator pit;
	// VectorOps.normalize(tmp1);
	if ((float)yy == (float)height) {
	    arc = new Path2D.Double();
	    arc.moveTo(0.0, height);
	    arc.lineTo(xx, yy);
	    pit = arc.getPathIterator(null);
	} else if (cmode) {
	    tmp[0] = 1.0; tmp[1] = 0.0;
	    // make sure this is not colinear. It is a
	    // a more expensive test than the (float)yy == (float)height
	    // one. We elevate the curve to a cubic one to simplify life
	    // for the caller if the caller then wants to modify some of
	    // the control points.
	    double vt[] = {xx, yy - height};
	    VectorOps.normalize(vt);
	    if (Math.abs(vt[1]) < 1.e-8) {
		double[] coords1 = new double[6];
		double[] coords2 = new double[6];
		coords1[0] = xx;
		coords1[1] = yy;
		Path2DInfo.elevateDegree(1, coords2, 0.0, height, coords1);
		Path2DInfo.elevateDegree(2, coords1, 0.0, height, coords2);
		arc = new Path2D.Double();
		arc.moveTo(0.0, height);
		arc.curveTo(coords1[0], coords1[1], coords1[2], coords1[3],
			    coords1[4], coords1[5]);
	    } else {
		arc = Paths2D.createArc(0.0, height, tmp, 0, xx, yy, Math.PI/4);
	    }
	    pit = arc.getPathIterator(null);
	} else {
	    Path2DInfo.getLineIntersectionXY(0.0, height, 1.0, height,
					     xx, yy, xx, yy+1.0, tmp, 0);
	    arc = new Path2D.Double();
	    arc.moveTo(0.0, height);
	    arc.curveTo(tmp[0], tmp[1], tmp[0], tmp[1], xx, yy);
	    pit = arc.getPathIterator(null);
	}
	double[] results = new double[12];
	for (int p = 0; p < 3; p++) {
	    results[p] = center[p] + height*vector[p];
	}
	pit.next();
	switch(pit.currentSegment(circCoords)) {
	case PathIterator.SEG_LINETO:
	    // System.out.println("linear case");
	    double[] lcoords =
		Path3D.setupCubic(results[0], results[1], results[2],

				  x, y, z);
	    System.arraycopy(lcoords, 3, results, 3, 6);
	    break;
	case PathIterator.SEG_CUBICTO:
	    /*
	    tmp[0] = circCoords[0];
	    tmp[1] = circCoords[1] - height;
	    System.out.println("(CPT1 - start) . vector = " +
			       +VectorOps.dotProduct(tmp, vector));
	    System.out.format("(%g, %g)-(%g,%g)-(%g, %g)\n",
			      circCoords[0], circCoords[1],
			      circCoords[2], circCoords[3],
			      circCoords[4], circCoords[5]);
	    */
	    for (int k = 0; k < 2; k++) {
		for (int p = 0; p < 3; p++) {
		    results[3 + k*3 + p] = center[p]
			+ circCoords[k*2]*tmp2[p]
			+ circCoords[k*2+1]*vector[p];
		}
	    }
	    break;
	}
	results[9] = x;
	results[10] = y;
	results[11] = z;
	for (int i = 0; i < 12; i++) {
	    results[i] = (double)(float)results[i];
	}
	return results;
    }

    // copied and modified from Path3DInfo, which uses the same
    // trick to improve efficiency.

    static final int QLEN = 32;

    static double[] u4len = VectorValuedGLQ.getArguments(0.0, 1.0, QLEN);
    static Path3DInfo.UValues[] uv4len = new Path3DInfo.UValues[QLEN];
    static {
	for (int i = 0; i < QLEN; i++) {
	    uv4len[i] = new Path3DInfo.UValues(u4len[i]);
	    u4len[i] = (double) i;
	}
    }

    static VectorValuedGLQ<Path3DInfo.SegmentData>
	glq4 = new VectorValuedGLQ<>(QLEN, 4) {
		@Override
		protected void mapping(double[] results, int m, double iu,
				       Path3DInfo.SegmentData data) {
		    int i = (int)Math.round(iu);
		    Path3DInfo.UValues uvals  = uv4len[i];
		    double dsDu = data.dsDu(uvals);
		    results[0] = data.getX(uvals)*dsDu;
		    results[1] = data.getY(uvals)*dsDu;
		    results[2] = data.getZ(uvals)*dsDu;
		    results[3] = dsDu;
		}
	    };

    private static void segmentTerms(Adder[] adders, int type,
				     double x0, double y0, double z0,
				     double[] coords)
    {
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return;
	    // return 0.0;
	case PathIterator3D.SEG_CLOSE:
	    // coords must be set so that (coords[0],coords[1]) is the
	    // position set by the last SEG_MOVETO
	case PathIterator3D.SEG_LINETO:
	    {
		double dx = coords[0] - x0;
		double dy = coords[1] - y0;
		double dz = coords[2] - z0;
		double ds = Math.sqrt(dx*dx + dy*dy + dz*dz);
		for (int i = 0; i < 3; i++) {
		    adders[i].add(ds*((coords[i] + x0)/2));
		}
		adders[3].add(ds);
	    }
	    break;
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
		    
		    glq4.integrateWithP(adders, u4len,
					new Path3DInfo.SegmentData
					(fst, xx0, yy0, zz0, fcoords,
					      null));
		    /*
		    double flen =
			glq4X.integrateWithP(u4len,
					     new Path3DInfo.SegmentData
					     (fst, xx0, yy0, zz0, fcoords,
					      null));
		    adder2.add(flen);
		    */
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
		// return adder2.getSum();
	    }
	}
    }

    private static Point3D ourFindCenter(Path3D boundary)
	throws IllegalArgumentException
    {
	Point3D center = findCenter(boundary);
	if (center == null) {
	    throw new IllegalArgumentException(errorMsg("zeroLengthPath", 1));
	}
	return center;
    }

    /**
     * Find a center point given a boundary.
     * The value returned is that for the center of mass of a
     * line with a uniform density.
     * @param boundary the boundary
     * @return the center point; null if the path length of the boundary
     *         is zero (e.g., the boundary is empty)
     */
    public static Point3D findCenter(Path3D boundary) {
	if (boundary == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg", 1));
	}
	Rectangle3D bounds = boundary.getBounds();

	/*
	return new Point3D.Double(bounds.getCenterX(),
				  bounds.getCenterY(),
				  bounds.getCenterZ());
	*/
	// compute the center of mass of the path assuming a
	// constant mass per unit length along the path
	PathIterator3D pit = boundary.getPathIterator(null);
	if (pit.isDone()) return null;
	double[] coords = new double[9];
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double initx = 0.0, inity = 0.0, initz = 0.0;
	Adder.Kahan addrx = new Adder.Kahan();
	Adder.Kahan addry = new Adder.Kahan();
	Adder.Kahan addrz = new Adder.Kahan();
	Adder.Kahan addrs = new Adder.Kahan();
	Adder[] adders = {addrx, addry, addrz, addrs};
	// GLQuadrature glq = null;
	boolean done = false;
	double tsumx = 0.0;
	double rsum = 0.0;
	int tsumcnt = 0;
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		initx = lastx;
		inity = lasty;
		initz = lastz;
		// glq = null;
		break;
	    case PathIterator3D.SEG_LINETO:
		{
		    double dx = coords[0] - lastx;
		    double dy = coords[1] - lasty;
		    double dz = coords[2] - lastz;
		    segmentTerms(adders, type, lastx, lasty, lastz, coords);
		    /*
		    addrx.add(segmentTermX(type, lastx, lasty, lastz, coords));
		    addry.add(segmentTermY(type, lastx, lasty, lastz, coords));
		    addrz.add(segmentTermZ(type, lastx, lasty, lastz, coords));
		    addrs.add(Path3DInfo.segmentLength(type,
						       lastx, lasty, lastz,
						       coords));
		    */         						       
		    lastx = coords[0];
		    lasty = coords[1];
		    lastz = coords[2];
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		segmentTerms(adders, type, lastx, lasty, lastz, coords);
		/*
		addrx.add(segmentTermX(type, lastx, lasty, lastz, coords));
		addry.add(segmentTermY(type, lastx, lasty, lastz, coords));
		addrz.add(segmentTermZ(type, lastx, lasty, lastz, coords));
		addrs.add(Path3DInfo.segmentLength(type, lastx, lasty, lastz,
						   coords));
		*/
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		segmentTerms(adders, type, lastx, lasty, lastz, coords);
		/*
		addrx.add(segmentTermX(type, lastx, lasty, lastz, coords));
		addry.add(segmentTermY(type, lastx, lasty, lastz, coords));
		addrz.add(segmentTermZ(type, lastx, lasty, lastz, coords));
		addrs.add(Path3DInfo.segmentLength(type, lastx, lasty, lastz,
						   coords));
		*/
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (lastx != initx || lasty != inity || lastz != initz) {
		    coords[0] = initx;
		    coords[1] = inity;
		    coords[2] = initz;
		    segmentTerms(adders, type, lastx, lasty, lastz, coords);
		    /*
		    addrx.add(segmentTermX(type, lastx, lasty, lastz, coords));
		    addry.add(segmentTermY(type, lastx, lasty, lastz, coords));
		    addrz.add(segmentTermZ(type, lastx, lasty, lastz, coords));
		    addrs.add(Path3DInfo.segmentLength(type,
						       lastx, lasty, lastz,
						       coords));
		    */
		}
		done = true;
		break;
	    }
	    if (done) {
		break;
	    }
	    pit.next();
	}
	double tlen = addrs.getSum();
	if (tlen == 0.0) return null;
	double xc = addrx.getSum()/tlen;
	double yc = addry.getSum()/tlen;
	double zc = addrz.getSum()/tlen;
	double ulp = Math.ulp(tlen);
	// a value is closer to zero than the floating point
	// accuracy of the length, set the value to zero.
	if (Math.abs(xc) < ulp) xc = 0.0;
	if (Math.abs(yc) < ulp) yc = 0.0;
	if (Math.abs(zc) < ulp) zc = 0.0;
	return new Point3D.Double(xc, yc, zc);
    }

    static VectorValuedGLQ<Path3DInfo.SegmentData>
	vglq4 = new VectorValuedGLQ<>(QLEN, 4) {
		@Override
		protected void mapping(double[] results, int m, double iu,
				       Path3DInfo.SegmentData data) {
		    int i = (int)Math.round(iu);
		    Path3DInfo.UValues uvals  = uv4len[i];
		    double dsDu = data.dsDu(uvals);
		    results[0] = data.getX(uvals)*dsDu;
		    results[1] = data.getY(uvals)*dsDu;
		    results[2] = data.getZ(uvals)*dsDu;
		    results[3] = dsDu;
		}
	    };

    private static void vsegmentTerms(Adder[] adders, int type,
				      double x0, double y0, double z0,
				      double[] coords, Point3D center,
				      VectorValuedGLQ<Path3DInfo.SegmentData>
				      vglq4, double[] tmp, double[] tmp1,
				      double[] tmp2)
    {
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return;
	    // return 0.0;
	case PathIterator3D.SEG_CLOSE:
	    // coords must be set so that (coords[0],coords[1]) is the
	    // position set by the last SEG_MOVETO
	case PathIterator3D.SEG_LINETO:
	    {
		double dx = coords[0] - x0;
		double dy = coords[1] - y0;
		double dz  = coords[2] - z0;
		double ds = Math.sqrt(dx*dx + dy*dy + dz*dz);

		tmp1[0] = dx; tmp1[1] = dy; tmp1[2] = dz;
		tmp2[0] = coords[0] - center.getX();
		tmp2[1] = coords[1] - center.getY();
		tmp2[2] = coords[2] - center.getZ();

		VectorOps.crossProduct(tmp, tmp1, tmp2);
		for (int i = 0; i < 3; i++) {
		    adders[i].add(tmp[i]);
		}
		adders[3].add(ds);
	    }
	    break;
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
		    
		    vglq4.integrateWithP(adders, u4len,
					new Path3DInfo.SegmentData
					(fst, xx0, yy0, zz0, fcoords,
					      null));
		    /*
		    double flen =
			glq4X.integrateWithP(u4len,
					     new Path3DInfo.SegmentData
					     (fst, xx0, yy0, zz0, fcoords,
					      null));
		    adder2.add(flen);
		    */
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
		// return adder2.getSum();
	    }
	}
    }


    /**
     * Find a vector roughly perpendicular to a boundary, given a center
     * point.  The value returned is the average over the boundary of
     * the cross product of a tangent vector to the boundary and a vector
     * from the center point to the boundary, where the tangent vector is of
     * unit length. When the boundary is a 'hole' in a surface, the vector
     * will point in the direction of surface's orientation (boundaries when
     * viewed from the 'outside' of the surface are represented by clockwise
     * curves in this case).
     * @param boundary the boundary
     * @param center the center point
     */
    public static double[] findVector(Path3D boundary, final Point3D center) {
	if (boundary == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg", 1));
	}
	if (center == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg", 2));
	}
	VectorValuedGLQ<Path3DInfo.SegmentData>
	    vglq4 = new VectorValuedGLQ<>(QLEN, 4) {
		    double[] v = new double[3];
		    double[] t = new double[3];
		    double[] cp = new double[3];
		    double xc = center.getX();
		    double yc = center.getY();
		    double zc = center.getZ();
		    @Override
		    protected void mapping(double[] results, int m, double iu,
				       Path3DInfo.SegmentData data) {
			int i = (int)Math.round(iu);
			Path3DInfo.UValues uvals  = uv4len[i];
			double dsDu = data.dsDu(uvals);
			
			v[0] = data.getX(uvals) - xc;
			v[1] = data.getY(uvals) - yc;
			v[2] = data.getZ(uvals) - zc;
			data.getTangent(uvals, t, 0);
			VectorOps.crossProduct(cp, t, v);
			results[0] = cp[0]*dsDu;
			results[1] = cp[1]*dsDu;
			results[2] = cp[2]*dsDu;
			results[3] = dsDu;
			/*
			System.out.format("(%g, %g, %g) from (%g, %g, %g) X "
					  + "(%g, %g, %g)\n",
					  results[0], results[1], results[2],
					  t[0], t[1], t[2], v[0], v[1], v[2]);
			*/
		    }
		};
	PathIterator3D pit = boundary.getPathIterator(null);
	if (pit.isDone()) return null;
	double[] coords = new double[9];
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double initx = 0.0, inity = 0.0, initz = 0.0;
	Adder.Kahan addrx = new Adder.Kahan();
	Adder.Kahan addry = new Adder.Kahan();
	Adder.Kahan addrz = new Adder.Kahan();
	Adder.Kahan addrs = new Adder.Kahan();
	Adder[] adders = {addrx, addry, addrz, addrs};
	boolean done = false;
	double[] vtmp = new double[3];
	double[] vtmp1 = new double[3];
	double[] vtmp2 = new double[3];

	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		initx = lastx;
		inity = lasty;
		initz = lastz;
		break;
	    case PathIterator3D.SEG_LINETO:
		{
		    double dx = coords[0] - lastx;
		    double dy = coords[1] - lasty;
		    double dz = coords[2] - lastz;
		    vsegmentTerms(adders, type, lastx, lasty, lastz, coords,
				  center, vglq4, vtmp, vtmp1, vtmp2);
		    lastx = coords[0];
		    lasty = coords[1];
		    lastz = coords[2];
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		vsegmentTerms(adders, type, lastx, lasty, lastz, coords,
			      center, vglq4, vtmp, vtmp1, vtmp2);
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		vsegmentTerms(adders, type, lastx, lasty, lastz, coords,
			      center, vglq4, vtmp, vtmp1, vtmp2);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (lastx != initx || lasty != inity || lastz != initz) {
		    coords[0] = initx;
		    coords[1] = inity;
		    coords[2] = initz;
		    vsegmentTerms(adders, type, lastx, lasty, lastz, coords,
				  center, vglq4, vtmp, vtmp1, vtmp2);
		}
		done = true;
		break;
	    }
	    if (done) {
		break;
	    }
	    pit.next();
	}
	double tlen = addrs.getSum();
	if (tlen == 0.0) return null;
	double[] results = new double[3];
	results[0] = addrx.getSum()/tlen;
	results[1] = addry.getSum()/tlen;
	results[2] = addrz.getSum()/tlen;
	double ulp = Math.ulp(tlen);
	// a value is closer to zero than the floating point
	// accuracy of the length, set the value to zero.
	if (Math.abs(results[0]) < ulp) results[0] = 0.0;
	if (Math.abs(results[1]) < ulp) results[1] = 0.0;
	if (Math.abs(results[2]) < ulp) results[2] = 0.0;
	return results;
    }

    
    /**
     * Constructor.
     * When cmode is false, the spokes will set the tangent at the
     * center point to be perpendicular to this shape's vector and
     * the tangent at the boundary to be parallel to this shape's vector.
     * At both of these end points, the second derivative vanishes.
     * The center point is computed by using {@link #findCenter(Path3D)}
     * with the boundary as its argument.
     * The vector is computed using {@link #findVector(Path3D,Point3D)}
     * with the boundary and the center point as its arguments.
     * @param boundary the boundary
     * @param height the height above the boundary for the central point
     *        of this shape.
     * @param cmode true if the spokes should be circular; false if they
     *        should be a Bezier curve that is as 'flat' as possible except
     *        at a bend
     */
    public BezierCap(Path3D boundary, double height, boolean cmode) {
	this(boundary, ourFindCenter(boundary), height, cmode);
    }


    /**
     * Constructor given a center point.
     * When cmode is false, the spokes will set the tangent at the
     * center point to be perpendicular to this shape's vector and
     * the tangent at the boundary to be parallel to this shape's vector.
     * At both of these end points, the second derivative vanishes.
     * The vector is computed using {@link #findVector(Path3D,Point3D)}
     * with the boundary and the center point as its arguments.
     * @param boundary the boundary
     * @param center the center point
     * @param height the height above the boundary for the central point
     *        of this shape.
     * @param cmode true if the spokes should be circular; false if they
     *        should be a Bezier curve that is as 'flat' as possible except
     *        at a bend
     */
    public BezierCap(Path3D boundary, Point3D center, double height,
		     boolean cmode) {
	this(boundary.getPathIterator(null), center,
	     findVector(boundary, center), height, cmode);
    }

    /**
     * Constructor given a center point and a vector.
     * When cmode is false, the spokes will set the tangent at the
     * center point to be perpendicular to this shape's vector and
     * the tangent at the boundary to be parallel to this shape's vector.
     * At both of these end points, the second derivative vanishes.
     * @param boundary the boundary
     * @param center the center point
     * @param height the height above the boundary for the central point
     *        of this shape.
     * @param cmode true if the spokes should be circular; false if they
     *        should be a Bezier curve that is as 'flat' as possible except
     *        at a bend
     */
    public BezierCap(Path3D boundary, Point3D center, double[] vector,
		     double height, boolean cmode) {
	this(boundary.getPathIterator(null), center, vector, height, cmode);
    }

    /**
     * Constructor given a path iterator for the boundary.
     * When cmode is false, the spokes will set the tangent at the
     * center point to be perpendicular to this shape's vector and
     * the tangent at the boundary to be parallel to this shape's vector.
     * At both of these end points, the second derivative vanishes.
     * @param pit the path iterator for the boundary
     * @param center the center point
     * @param height the height above the boundary for the central point
     *        of this shape.
     * @param cmode true if the spokes should be circular; false if they
     *        should be a Bezier curve that is as 'flat' as possible except
     *        at a bend
     */
    public BezierCap(PathIterator3D pit, Point3D center, double[] vector,
		     double height, boolean cmode)
    {

	ArrayList<double[]> spokes = new ArrayList<>();
	ArrayList<double[]> edges = new ArrayList<>();

	this.center[0] = (double)(float)center.getX();
	this.center[1] = (double)(float)center.getY();
	this.center[2] = (double)(float)center.getZ();
	this.height = (double)(float) height;
	VectorOps.unitVector(this.vector, 0, vector, 0, 3);
	double initx = 0.0, inity = 0.0 , initz = 0.0;
	double lastx = 0.0 , lasty = 0.0, lastz = 0.0;
	double[] bcoords = new double[9];
	double[] tmp;
	boolean done = false;
	while (!pit.isDone()) {
	    switch(pit.currentSegment(bcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		lastx = bcoords[0];
		lasty = bcoords[1];
		lastz = bcoords[2];
		spokes.add(getSpoke(lastx, lasty, lastz, cmode));
		initx = lastx;
		inity = lasty;
		initz = lastz;
		break;
	    case PathIterator3D.SEG_LINETO:
		tmp = Path3D.setupCubic(lastx, lasty, lastz,
					bcoords[0], bcoords[1], bcoords[2]);
		for (int i = 0; i < tmp.length; i++) {
		    tmp[i] = (double)(float)tmp[i];
		}
		edges.add(tmp);
		lastx = bcoords[0];
		lasty = bcoords[1];
		lastz = bcoords[2];
		spokes.add(getSpoke(lastx, lasty, lastz, cmode));
		break;
	    case PathIterator3D.SEG_QUADTO:
		tmp = Path3D.setupCubic(lastx, lasty, lastz,
					bcoords[0], bcoords[1], bcoords[2],
					bcoords[3], bcoords[4], bcoords[5]);
		for (int i = 0; i < tmp.length; i++) {
		    tmp[i] = (double)(float)tmp[i];
		}
		edges.add(tmp);
		lastx = bcoords[3];
		lasty = bcoords[4];
		lastz = bcoords[5];
		spokes.add(getSpoke(lastx, lasty, lastz, cmode));
		break;
	    case PathIterator3D.SEG_CUBICTO:
		tmp = new double[12];
		tmp[0] = lastx;
		tmp[1] = lasty;
		tmp[2] = lastz;
		System.arraycopy(bcoords, 0, tmp, 3, 9);
		for (int i = 0; i < tmp.length; i++) {
		    tmp[i] = (double)(float)tmp[i];
		}
		edges.add(tmp);
		lastx = bcoords[6];
		lasty = bcoords[7];
		lastz = bcoords[8];
		spokes.add(getSpoke(lastx, lasty, lastz, cmode));
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (lastx != initx || lasty != inity || lastz != initz) {
		    tmp = Path3D.setupCubic(lastx, lasty, lastz,
					    initx, inity, initz);
		    for (int i = 0; i < tmp.length; i++) {
			tmp[i] = (double)(float)tmp[i];
		    }
		    edges.add(tmp);
		    spokes.add(getSpoke(initx, inity, initz, cmode));
		}
		done = true;
		break;
	    }
	    pit.next();
	    if (done) break;
	}
	this.edges = new double[edges.size()][];
	this.colors = new Color[edges.size()];
	this.cp111 = new double[edges.size()][];
	this.spokes = new double[spokes.size()][];
	edges.toArray(this.edges);
	spokes.toArray(this.spokes);
    }

    boolean flipped = false;

    /**
     * Change the orientation of the B&eacute;zier triangles associated
     * with this object.
     * This method affects the orientation of triangles provided by iterators.
     * It does not change the values returned by calling methods
     * such as .
     * @param reverse true if the orientation is the reverse of
     *        the one that was initially defined; false if the orientation is
     *        the same as the one that was initially defined.
     */
    public void reverseOrientation(boolean reverse) {
	flipped = reverse;
    }

    /**
     * Determine if the orientation for this grid is reversed.
     * @return true if the orientation is reversed; false if not
     */
    public boolean isReversed() {return flipped;}

    /**
     * Set the default color for this shape.
     */
    public void setColor(Color c) {
	defaultColor = c;
    }


    /**
     * Set the color for a B&eacute;zier triangle specified by an index
     * @param i the index
     * @param c the color
     */
    public void setColor(int i, Color c) {
	colors[i] = c;
    }

    /**
     * Get the default color for this shape.
     * This is the color for the shape, excluding B&eacute;zier triangles
     * for which an explicit color has been specified.
     * @return the color
     */
    public Color getColor() {
	return defaultColor;
    }


    /**
     * Get the  color for this shape given an index.
     * This is the color for the shape, excluding B&eacute;zier triangles
     * for which an explicit color has been specified.
     * When a color for a specific index has been provided, that color is
     * returned; otherwise the default color (if any) is returned.
     * @return the color
     */
    public Color getColor(int i) {
	Color c = colors[i];
	return (c == null)? defaultColor: c;
    }

    /**
     * Set the inner control point of a B&eacute;zier triangle.
     * @param i the index of a B&eacute;zier triangle.
     * @param coords a vector of length 3 giving the control point
     *        that is not on the B&eacute;zier triangle's edge,
     *        with coords[0] containing the X value, coords[1]
     *        containing the Y value, and coords[2] containing the
     *        Z value.
     */
    public void setCP111(int i, double[] coords)
	throws IllegalArgumentException
    {
	if (coords == null) {
	    cp111[i] = null;
	} else {
	    if (i < 0 || i >= cp111.length) {
		String msg = errorMsg("illegalIndex", i, cp111.length);
		throw new IllegalArgumentException(msg);
	    }
	    if (cp111[i] == null) cp111[i] = new double[3];
	    for (int k = 0; k < 3; k++) {
		cp111[i][k] = (double)(float)coords[k];
	    }
	}
    }

    /**
     * Set the intermediate control points for a spoke.
     * Each spoke is a cubic B&eacute;zier curve starting at
     * the center point, followed by two control points, and ending
     * on a point on the boundary. The first intermediate control
     * point is specified by coords[0], coords[1], and coords[2].
     * The second intermediate control point is specified by coords[3],
     * coords[4], and coords[5].
     * @param i the index of a B&eacute;zier triangle.
     * @param coords an array of length 6 (or more) containing
     *        the control points.
     */
    public void setSpoke(int i, double[] coords)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= spokes.length) {
	    String msg = errorMsg("illegalIndex", i, spokes.length);
	    throw new IllegalArgumentException(msg);
	}
	for (int k = 0; k< 6; k++) {
	    spokes[i][k+3] = (double)(float)coords[k];
	}
    }

    /**
     * Print this object's control points.
     */
    public void print() throws IOException {
	print(System.out);
    }

    /**
     * Print this object's control points, specifying an output.
     * @param out the output
     */
    public void print(Appendable out) throws IOException {
	print("", out);
    }

    /**
     * Print this object's control points, specifying a prefix.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * @param prefix the prefix
     */
    public void print(String prefix) throws IOException {
	print(prefix, System.out);
    }

    /**
     * Print this object's control points, specifying a prefix and output.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * @param prefix the prefix
     * @param out the output
     */
    public void print(String prefix, Appendable out) throws IOException {
	out.append(String.format("%snumber of cubic triangles: %d\n", prefix,
				 edges.length));
	for (int i = 0; i < edges.length; i++) {
	    out.append(String.format("%scubic triangle %d:\n", prefix, i));
	    out.append(String.format("%s    spoke:\n", prefix));
	    for(int k = 0; k < 12; k += 3) {
		out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					 spokes[i][k],
					 spokes[i][k+1],
					 spokes[i][k+2]));
	    }
	    out.append(String.format("%s    boundary edge:\n", prefix));
	    for(int k = 0; k < 12; k += 3) {
		out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					 edges[i][k],
					 edges[i][k+1],
					 edges[i][k+2]));
	    }
	    if (i == edges.length-1) {
		out.append(String.format("%s    spoke:\n", prefix));
		for(int k = 0; k < 12; k += 3) {
		    out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					     spokes[i+1][k],
					     spokes[i+1][k+1],
					     spokes[i+1][k+2]));
		}
	    }
	}
    }

    int rlevel = 0;

    /**
     * Set the radial tessellation level.
     * Increasing the level by 1 doubles the number of segments in
     * the radial direction.
     * @param level the tessellation level
     */
    public void setRadialTessellation(int level) {
	this.rlevel = level;
    }

    /**
     * Get the radial tessellation level
     * @return  the radial tesselation level;
     */
    public int getRadialTessellation() {return rlevel;}


    // Shape3D methods

    private class Iterator1 implements SurfaceIterator {
	boolean flip = flipped;
	int index = -1;
	boolean done = false;
	Iterator1() {
	    next();
	}

	@Override
	public Color currentColor() {
	    Color c = colors[index];
	    if (c == null) c = defaultColor;
	    return c;
	}

	int sz = 48*((rlevel == 0)? 0: (1 << rlevel) + 1);
	int rcoordsIndex = 0;
	double[] rcoords = new double[sz];
	int depth = 0;
	int maxdepth = rlevel;
	double[] tmp = (rlevel == 0)? null: (new double[30]);
	double[] tmp1 = (rlevel == 0)? null: (new double[12]);
	double[] tmp2 = (rlevel == 0)? null: (new double[12]);

	double[][] workspaceL = new double[rlevel][];
	double[][] workspaceR = new double[rlevel][];

	private void setup(double[] buffer) {
	    // Surface3D.setupV0ForTriangle(spokes[index], buffer, !flip);
	    // Surface3D.setupU0ForTriangle(edges[index], buffer, flip);
	    // Surface3D.setupW0ForTriangle(spokes[index+1], buffer, flip);

	    if (flip) {
		Surface3D.setupV0ForTriangle(spokes[index+1], buffer, true);
		Surface3D.setupU0ForTriangle(edges[index], buffer, true);
		Surface3D.setupW0ForTriangle(spokes[index], buffer, false);
	    } else {
		Surface3D.setupV0ForTriangle(spokes[index], buffer, true);
		Surface3D.setupU0ForTriangle(edges[index], buffer, false);
		Surface3D.setupW0ForTriangle(spokes[index+1], buffer, false);
	    }


	    if (cp111[index] == null) {
		for (int i = 0; i < 3; i++) {
		    buffer[15+i] = (double)(float)
			((buffer[12+i] + buffer[18+i]) / 2.0);
		}
		double len1 = VectorOps.norm(buffer, 12, 3);
		double len2 = VectorOps.norm(buffer, 15, 3);
		double len3 = VectorOps.norm(buffer, 18, 3);
		double len = (len1 + len2) / 2.0;
		double ratio = len / len2;
		if (ratio < 1.0) {
		    for (int i = 0; i < 3; i++) {
			buffer[15+i] = (double)(float)
			    (center[i] + ratio * (buffer[15+i] - center[i]));
		    }
		}

		// Surface3D.setupCP111ForTriangle(buffer);
	    } else {
		for (int k = 0; k < 3; k++) {
		    buffer[15+k] = (double)(float)cp111[index][k];
		}
	    }
	}
	
	private void setupRcoords() {
	    setup(tmp);
	    System.arraycopy(tmp, 0, rcoords, 0, 3);
	    System.arraycopy(tmp, 3, rcoords, 12, 3);
	    System.arraycopy(tmp, 6, rcoords, 24, 3);
	    System.arraycopy(tmp, 9, rcoords, 36, 3);
	    System.arraycopy(tmp, 12, rcoords, 3, 3);
	    System.arraycopy(tmp, 21, rcoords, 6, 3);
	    System.arraycopy(tmp, 27, rcoords, 9, 3);
	    System.arraycopy(tmp, 24, rcoords, 42, 3);
	    System.arraycopy(tmp, 18, rcoords, 39, 3);

	    System.arraycopy(tmp, 27, rcoords,  21, 3);
	    System.arraycopy(tmp, 27, rcoords,  33, 3);
	    System.arraycopy(tmp, 27, rcoords,  45, 3);

	    System.arraycopy(tmp, 21, tmp1, 0, 3);
	    System.arraycopy(tmp, 24, tmp1, 3, 3);
	    Path3DInfo.elevateDegree(1, tmp2, 0, tmp1, 0);
	    Path3DInfo.elevateDegree(2, tmp1, 0, tmp2, 0);
	    System.arraycopy(tmp1, 3, rcoords, 18, 3);
	    System.arraycopy(tmp1, 6, rcoords, 30, 3);
	    
	    System.arraycopy(tmp, 12, tmp1, 0, 3);
	    System.arraycopy(tmp, 15, tmp1, 3, 3);
	    System.arraycopy(tmp, 18, tmp1, 6, 3);
	    Path3DInfo.elevateDegree(2, tmp2, 0, tmp1, 0);
	    System.arraycopy(tmp2, 3, rcoords, 15, 3);
	    System.arraycopy(tmp2, 6, rcoords, 27, 3);
	    rcoordsIndex += 48;
	    split(0, rcoords);
	}

	// splitting based on SubdivisionIterator code


	private void split(int depth, double[] ourcoords) {
	    if (depth == maxdepth) return;
	    if (workspaceL[depth] == null) workspaceL[depth] = new double[48];
	    if (workspaceR[depth] == null) workspaceR[depth] = new double[48];
	    
	    if (depth == maxdepth - 1) {
		SubdivisionIterator.getLeftPatch(rcoords, rcoordsIndex,
						 ourcoords, 0);
		rcoordsIndex += 48;
		SubdivisionIterator.getRightPatch(rcoords, rcoordsIndex,
						  ourcoords, 0);
		rcoordsIndex += 48;
		return;
	    } else {
		SubdivisionIterator.getLeftPatch(workspaceL[depth], 0,
						 ourcoords, 0);
		SubdivisionIterator.getRightPatch(workspaceR[depth], 0,
						  ourcoords, 0);
	    }
	    split(depth+1, workspaceL[depth]);
	    split(depth+1, workspaceR[depth]);
	}

	@Override
	public int currentSegment(double[] coords) {
	    if (rcoordsIndex > 0) {
		System.arraycopy(rcoords, rcoordsIndex, coords, 0, 48);
		for (int i = 0; i < 48; i++) {
		    coords[i] = (double)(float)coords[i];
		}
		boolean isTriangle = true;
		for (int i = 0; i < 3; i++) {
		    double xyz = coords[9+i];
		    if (xyz != coords[21+i] || xyz != coords[33+i]
			|| xyz != coords[45+i]) {
			isTriangle = false;
			break;
		    }
		}
		if (isTriangle) {
		    // turn it back into a real Bezier triangle
		    System.arraycopy(coords,0,tmp, 0, 3);
		    System.arraycopy(coords,12,tmp, 3, 3);
		    System.arraycopy(coords,24,tmp, 6, 3);
		    System.arraycopy(coords,36,tmp, 9, 3);
		    System.arraycopy(coords,39,tmp, 18, 3);
		    System.arraycopy(coords,42,tmp, 24, 3);
		    System.arraycopy(coords,9,tmp, 27, 3);
		    System.arraycopy(coords,3,tmp, 12, 3);
		    System.arraycopy(coords,6,tmp, 21, 3);
		    // The cubic-patch control points CP12 and CP13
		    // are a linear combination of the values for the
		    // cubic-triangle control points CP102, CP111, and
		    // CP120.  If we turn a cubic triangle into a cubic
		    // patch, CP12 = CP102/3 + 2*CP111/3 and
		    // CP13 = 2*CP111/3 + CP120/3.  If we solve for CP111,
		    // we get two equations, and hence two values.  If a
		    // cubic patch exactly matches a cubic triangle, these
		    // two equations will produce the same number.  After
		    // splitting, we get two slightly different values,
		    // so we average the two (a test case showed differences
		    // of no more than .00001 (10^(-5)), so it could be
		    // round off errors related to rounding doubles to floats.)
		    for (int i = 0; i < 3; i++) {
			tmp[15+i] = (3*(coords[15+i] + coords[27+i])
				     - (coords[39+i] + coords[3+i])) / 4;
		    }
		    System.arraycopy(tmp, 0, coords, 0, 30);
		    return SurfaceIterator.CUBIC_TRIANGLE;
		} else {
		    return SurfaceIterator.CUBIC_PATCH;
		}
	    }
	    if (index >= edges.length) {
		Arrays.fill(coords, 0, 30, Double.NaN);
		return -1;
	    }
	    setup(coords);
	    /*
	    Surface3D.setupV0ForTriangle(spokes[index], coords, !flip);
	    Surface3D.setupU0ForTriangle(edges[index], coords, flip);
	    Surface3D.setupW0ForTriangle(spokes[index+1], coords, flip);
	    if (cp111[index] == null) {
		Surface3D.setupCP111ForTriangle(coords);
	    } else {
		for (int k = 0; k < 3; k++) {
		    coords[15+k] = (double)(float)cp111[index][k];
		}
	    }
	    */
	    return SurfaceIterator.CUBIC_TRIANGLE;
	}
	
	double[] tcoords = null;

	@Override
	public int currentSegment(float[] coords) {
	    if (tcoords == null) tcoords = new double[48];
	    int result = currentSegment(tcoords);
	    int limit = (result == SurfaceIterator.CUBIC_TRIANGLE)? 30: 48;
	    for (int k = 0; k < limit; k++) {
		coords[k] = (float)tcoords[k];
	    }
	    return result;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return done;
	}

	@Override
	public void next() {
	    if (rcoordsIndex > 48) {
		rcoordsIndex -= 48;
		if (rcoordsIndex > 0) {
		    return;
		}
	    }
	    index++;
	    if (index == edges.length) {
		index--;
		done = true;
		tcoords = null;
		workspaceL = null;
		workspaceR = null;
	    } else {
		if (maxdepth > 0) {
		    rcoordsIndex = 0;
		    setupRcoords();
		    rcoordsIndex -= 48;
		}
	    }
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }

    private class Iterator2 implements SurfaceIterator {
	boolean flip = flipped;
	int index = -1;
	boolean done = false;
	/*
	 * tform will never be null because getSurfaceIterator
	 * will create an instance of Iterator1 if the transform is null.
	 */
	Transform3D tform;

	Iterator2(Transform3D tform) {
	    this.tform = tform;
	    next();
	}

	@Override
	public Color currentColor() {
	    Color c = colors[index];
	    if (c == null) c = defaultColor;
	    return c;
	}

	double[] tcoords1 = null;

	@Override
	public int currentSegment(double[] coords) {
	    if (tcoords1 == null) tcoords1 = new double[30];
	    if (index >= edges.length) {
		Arrays.fill(coords, 0, 30, Double.NaN);
		return -1;
	    }
	    Surface3D.setupV0ForTriangle(spokes[index], coords, !flip);
	    Surface3D.setupU0ForTriangle(edges[index], coords, flip);
	    Surface3D.setupW0ForTriangle(spokes[index+1], coords, flip);
	    if (cp111[index] == null) {
		Surface3D.setupCP111ForTriangle(tcoords1);
	    } else {
		for (int k = 0; k < 3; k++) {
		    tcoords1[15+k] = (double)(float)cp111[index][k];
		}
	    }
	    tform.transform(tcoords1, 0, coords, 0, 10);
	    for (int i = 0; i < 30; i++) {
		coords[i] = (double)(float)coords[i];
	    }
	    return SurfaceIterator.CUBIC_TRIANGLE;
	}
	

	double[] tcoords2 = new double[30];

	@Override
	public int currentSegment(float[] coords) {
	    if (tcoords2 == null) tcoords2 = new double[30];
	     int result = currentSegment(tcoords2);
	    for (int k = 0; k < 30; k++) {
		coords[k] = (float)tcoords2[k];
	    }
	    return result;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return done;
	}

	@Override
	public void next() {
	    index++;
	    if (index >= edges.length) {
		index = edges.length - 1;
		done = true;
		tcoords1 = null;
		tcoords2 = null;
	    }
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	if (tform == null) {
	    return new Iterator1();
	} else {
	    return new Iterator2(tform);
	}
    }

    @Override
    public final synchronized
    SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	if (tform == null) {
	    if (level == 0) {
		return new Iterator1();
	    } else {
		return new SubdivisionIterator(new Iterator1(), level);
	    }
	} else {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator1(), tform, level);
	    }
	}
    }

    Rectangle3D brect = null;

    boolean bpathSet = false;
    Path3D bpath = null;

    @Override
    public Path3D getBoundary() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return bpath;
    }

    /**
     * Determine if this BezierCap is well formed.
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null);
    }

    /**
     * Determine if this BezierCap is well formed, logging error messages to
     * an Appendable.
     * @param out an Appendable for logging error messages
     * @return true if the grid is well formed; false otherwise
    */
    public boolean isWellFormed(Appendable out) {
	Surface3D.Boundary boundary
	    = new Surface3D.Boundary(getSurfaceIterator(null), out);
	if (!bpathSet) {
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (boundary.getPath() != null);
    }

    @Override
    public  Shape3D getComponent(int i)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= 1) {
	    String msg = errorMsg("illegalComponentIndex", i, 1);
	    throw new IllegalArgumentException(msg);
	}
	return this;
    }

    @Override
    public Rectangle3D getBounds() {
	if (brect == null) {
	    double[] coords = new double[48];
	    SurfaceIterator sit = getSurfaceIterator(null);
	    while (!sit.isDone()) {
		sit.currentSegment(coords);
		if (brect == null) {
		    brect = new
			Rectangle3D.Double(coords[0], coords[1], coords[2],
					   0.0, 0.0, 0.0);
		    for (int i = 3; i < 30; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		} else {
		    for (int i = 0; i < 30; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		}
		sit.next();
	    }
	}
	return brect;
    }


    @Override
    public boolean isClosedManifold() {
	return false;
	/*
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null) && bpath.isEmpty();
	*/
    }
    
    @Override
    public boolean isOriented() {return true;}

    @Override    
    public int numberOfComponents() {
	return edges.length > 0? 1: 0;
    }
}

//  LocalWords:  exbundle bezier pdf getSpoke tmp circCoords CPT SEG
//  LocalWords:  VectorOps dotProduct DInfo coords MOVETO flen glq yy
//  LocalWords:  integrateWithP len SegmentData fst zz fcoords getSum
//  LocalWords:  zeroLengthPath nullArg getCenterY getCenterZ addrx
//  LocalWords:  GLQuadrature segmentTermX lastx lasty lastz addry CP
//  LocalWords:  segmentTermY addrz segmentTermZ findVector boolean
//  LocalWords:  PathIterator getPathIterator initx inity initz cmode
//  LocalWords:  isDone currentSegment arraycopy LINETO QUADTO eacute
//  LocalWords:  CUBICTO crossProduct findCenter zier illegalIndex cp
//  LocalWords:  snumber scubic setupCP ForTriangle setupV setupU
//  LocalWords:  SubdivisionIterator setupW tform getSurfaceIterator
//  LocalWords:  Appendable illegalComponentIndex
