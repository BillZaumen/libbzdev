package org.bzdev.geom;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.VectorOps;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Utility class to create various 3D paths. This class consists of
 * static methods, and is analogous to the class Paths2D but for
 * 3D paths instead of 2D paths. It also includes methods for creating
 * unit vectors.
 * <P>
 * The operations include creating arcs and reversing existing paths.
 */
public class Paths3D {

    // just static
    private Paths3D() {}

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    /**
     * Create a 3D arc.
     * The normal vector must be perpendicular to the tangent
     * vector and must point towards the center of the circle that
     * contains the arc.
     * @param startx the starting X coordinate
     * @param starty the starting Y coordinate
     * @param startz the starting Z coordinate
     * @param tangent the tangent vector at the start of the arc
     * @param normal the normal vector at the start of the arc
     * @param radius the radius of the arc
     * @param theta the angular extent of the arc in radians
     * @return the new arc
     */
    public static Path3D createArc(double startx, double starty, double startz,
				   double[] tangent, double[] normal,
				   double radius,
				   double theta)
    {
	return createArc(startx, starty, startz, tangent, normal, radius,
			 theta, Paths2D.DEFAULT_MAX_DELTA);
    }

    /**
    /**
     * Create a 3D arc, specifying a maximum angular extent for each
     * path segment.
     * The normal vector must be perpendicular to the tangent
     * vector and must point towards the center of the circle that
     * contains the arc.
     * @param startx the starting X coordinate
     * @param starty the starting Y coordinate
     * @param startz the starting Z coordinate
     * @param tangent the tangent vector at the start of the arc
     * @param normal the normal vector at the start of the arc
     * @param radius the radius of the arc
     * @param theta the angular extent of the arc in radians
     * @param maxDelta the maximum angular extent of an arc segment
     *        with allowed values in the range (0, 2&pi;/3]
     * @return the new arc
     */
    public static Path3D createArc(double startx, double starty, double startz,
				   double[] tangent, double[] normal,
				   double radius,
				   double theta, double maxDelta)
    {
	double[] ourTangent = VectorOps.unitVector(tangent);
	double[] ourNormal = VectorOps.unitVector(normal);

	if (Math.abs(VectorOps.dotProduct(ourTangent, ourNormal)) > 1.e-10) {
	    throw new IllegalArgumentException(errorMsg("tangentNormal"));
	}

	Path2D arc = Paths2D.createArc(0.0, radius, 0.0, 0.0, theta, maxDelta);
	return new Path3D.Double(arc, (i, p, type, bounds) -> {
		return new Point3D.Double(startx + p.getX() * ourTangent[0]
					  + p.getY() * ourNormal[0],
					  starty + p.getX() * ourTangent[1]
					  + p.getY() * ourNormal[1],
					  startz + p.getX() * ourTangent[2]
					  + p.getY() * ourNormal[2]);
	}, 0);
    }

    private static void extendPath(Path3D path,
				   List<double[]> list,
				   boolean closed)
    {
	for (double[] coords: list) {
	    switch(coords.length) {
	    case 3:
		path.lineTo(coords[0], coords[1], coords[2]);
		break;
	    case 6:
		path.quadTo(coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5]);
		break;
	    case 9:
		path.curveTo(coords[0], coords[1], coords[2],
			     coords[3], coords[4], coords[5],
			     coords[6], coords[7], coords[8]);
		break;
	    default:
		throw new UnexpectedExceptionError();
	    }
	}
	if (closed) {
	    path.closePath();
	}
    }

    /**
     * Reverse a path.
     * For a path to be reversible, each {@link PathIterator3D#SEG_CLOSE}
     * segment that does not end a path must be followed by
     * a {@link PathIterator3D#SEG_MOVETO} segment.
     * <P>
     * Reversing a path can be useful when a path is an outer boundary
     * of one surface and an inner boundary of another.
     * @param path the path to reverse
     * @return the reversed path
     * @exception IllegalArgumentException the path cannot be reversed
     */
    public static Path3D reverse(Path3D path) throws IllegalArgumentException {
	double[] coords = new double[9];
	PathIterator3D pit = path.getPathIterator(null);

	double startx = 0.0, starty = 0.0 , startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	List<double[]> list = new LinkedList<>();
	boolean needMoveTo = true;
	Path3D npath = new Path3D.Double();
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		if (!list.isEmpty()) {
		    Collections.reverse(list);
		    npath.moveTo(lastx, lasty, lastz);
		    extendPath(npath, list, false);
		    list.clear();
		} else if (needMoveTo == false) {
		    // since the list is empty and needMoveTo is false,
		    // the last segment must have been a SEG_MOVETO one.
		    npath.moveTo(lastx, lasty, lastz);
		}
		startx = coords[0];
		starty = coords[1];
		startz = coords[2];
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		needMoveTo = false;
		break;
	    case PathIterator3D.SEG_LINETO:
		if (needMoveTo) {
		    throw new
			IllegalArgumentException(errorMsg("missingMOVETO"));
		}
		list.add(new double[] {lastx, lasty, lastz});
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (needMoveTo) {
		    throw new
			IllegalArgumentException(errorMsg("missingMOVETO"));
		}
		list.add(new double[] {coords[0], coords[1], coords[2],
				       lastx, lasty, lastz});
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (needMoveTo) {
		    throw new
			IllegalArgumentException(errorMsg("missingMOVETO"));
		}
		list.add(new double[] {coords[3], coords[4], coords[5],
				       coords[0], coords[1], coords[2],
				       lastx, lasty, lastz});
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (needMoveTo) {
		    throw new
			IllegalArgumentException(errorMsg("missingMOVETO"));
		}
		if (lastx != startx || lasty != starty || lastz != startz) {
		    list.add(new double[] {startx, starty, startz});
		    lastx = startx;
		    lasty = starty;
		    lastz = startz;
		}
		needMoveTo = true;
		Collections.reverse(list);
		npath.moveTo(startx, starty, startz);
		extendPath(npath, list, true);
		list.clear();
		break;
	    }
	    pit.next();
	}
	if (!list.isEmpty()) {
	    Collections.reverse(list);
	    npath.moveTo(lastx, lasty, lastz);
	    extendPath(npath, list, false);
	    list.clear();
	}

	return npath;
    }

    /**
     * Shift a path, possibly reversing it, so that its start is as
     * close as possible to the start of a target path.
     * The argument <CODE>path</CODE> or a new path based on it,
     * generated by shifting <CODE>path</CODE> and possibly
     * reversing <CODE>path</CODE> or the shifted <CODE>path</CODE>
     * will be returned.
     * <P>
     * To determine if the returned path was reversed, the dot product
     * of the normalized tangent vectors at the start of the target path
     * and the given path is computed.  If the absolute value of this
     * dotproduct is less than <CODE>0.0</CODE>, the path is reversed.
     * @param target the target path
     * @param path the path to shift
     * @return the path if its starting point is the closest one to
     *         the start of the target path; a new path otherwise
     * @exception IllegalArgumentException if either path is not closed,
     *         or if the number of segments for each path differ
     */
    public static Path3D alignClosedPaths(Path3D target, Path3D path)
	throws IllegalArgumentException
    {
	// the limit test will always succeed when the limit is 0.0 so
	// an IllegalStateException will never be generated
	return alignClosedPaths(target, path, 0.0);
    }

    /**
     * Shift a path, possibly reversing it, so that its start is as
     * close as possible to the start of a target path, with a limit
     * on how much tangent vectors can diverge.
     * The argument <CODE>path</CODE> or a new path based on it,
     * generated by shifting <CODE>path</CODE> and possibly
     * reversing <CODE>path</CODE> or the shifted <CODE>path</CODE>
     * will be returned.
     * <P>
     * To determine if the returned path was reversed, the dot product
     * of the normalized tangent vectors at the start of the target path
     * and the given path is computed.  If the absolute value of this
     * dot product is less than or equal to -<CODE>limit</CODE>, the
     * path is reversed.
     * @param target the target path
     * @param path the path to shift
     * @param limit a value in the range [0.0, 1.0] for the dot product
     *        test of the normalized tangents at the start of the paths after
     *        <CODE>path</CODE> was aligned.
     * @return the path if its starting point is the closest one to
     *         the start of the target path; a new path otherwise
     * @exception IllegalArgumentException if either path is not closed,
     *         if the first argument is out of range,
     *         or if the number of segments for each path differ
     * @exception IllegalStateException if the dot-product test failed
     */
    public static Path3D alignClosedPaths(Path3D target, Path3D path,
					  double limit)
	throws IllegalArgumentException, IllegalStateException
    {
	if (limit > 1.0 || limit < 0.0) {
	    throw new IllegalArgumentException();
	}
	if (!Path3DInfo.isClosed(target) || !Path3DInfo.isClosed(path)) {
	    throw new IllegalArgumentException();
	}
	double cp1[] = Path3DInfo.getControlPoints(target, false);
	double cp2[] = Path3DInfo.getControlPoints(path, false);
	if (cp1.length != cp2.length) {
	    throw new IllegalArgumentException();
	}

	double dminsq = VectorOps
	    .normSquared(VectorOps.sub(new double[cp1.length], 0,
				       cp1, 0, cp2, 0, 3));
	int ind = 0;
	for (int i = 3; i < cp2.length; i += 3) {
	    double dmsq = VectorOps
		.normSquared(VectorOps.sub(new double[cp1.length], 0,
					   cp1, 0, cp2, i, 3));
	    if (dmsq < dminsq) {
		dminsq = dmsq;
		ind = i;
	    }
	}
	double x = cp2[ind];
	double y = cp2[ind+1];
	double z = cp2[ind+2];
	Path3D result;
	if (ind == 0) {
	    result =  path;
	} else {
	    result = shiftClosedPath(path, x, y, z);
	}
	double[] t1 = new double[3];
	double[] t2 = new double[3];
	if (Path3DInfo.getStartingTangent(target, t1) == false
	    || Path3DInfo.getStartingTangent(result, t2) == false) {
	    throw new IllegalArgumentException();
	}
	t1 = VectorOps.normalize(t1);
	t2 = VectorOps.normalize(t2);
	double dprod = VectorOps.dotProduct(t1, t2);

	if (dprod <= -limit && dprod != 0.0) {
	    result = reverse(result);
	} else if (Math.abs(dprod) < limit) {
	    throw new IllegalStateException();
	}
	return result;
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
     * @return the path component with a segment starting or ending at
     *         (x, y, z), with its segments shifted cyclically so that
     *         the returned path starts at the point (x, y, z); null
     *         if no segment starts or ends with the point (x, y, z).
     * @exception IllegalArgumentException an argument was illegal
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
	double xxf = 0.0, yyf = 0.0, zzf = 0.0;
	boolean closed = false;
	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		path1.reset();
		path2.reset();
		path = path1;
		closed = false;
		xx = lastx = coords[0];
		yy = lasty = coords[1];
		zz = lastz = coords[2];
		xxf = (double)(float)xx;
		yyf = (double)(float)yy;
		zzf = (double)(float)zz;
		if (xxf == x && yyf == y && zzf == z) {
		    path = path2;
		}
		path.moveTo(lastx, lasty, lastz);
		break;
	    case PathIterator3D.SEG_LINETO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[0];
		yy = coords[1];
		zz = coords[2];
		xxf = (double)(float)xx;
		yyf = (double)(float)yy;
		zzf = (double)(float)zz;
		path.lineTo(coords[0],coords[1], coords[2]);
		if (path != path2 && xxf == x && yyf == y && zzf == z) {
		    path = path2;
		    path.moveTo(xx, yy, zz);
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[3];
		yy = coords[4];
		zz = coords[5];
		xxf = (double)(float)xx;
		yyf = (double)(float)yy;
		zzf = (double)(float)zz;
		path.quadTo(coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5]);
		if (path != path2 && xxf == x && yyf == y && zzf == z) {
		    path = path2;
		    path.moveTo(xx, yy, zz);
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (closed) {
		    throw new IllegalArgumentException(errorMsg("badSegClose"));
		}
		xx = coords[6];
		yy = coords[7];
		zz = coords[8];
		xxf = (double)(float)xx;
		yyf = (double)(float)yy;
		zzf = (double)(float)zz;
		path.curveTo(coords[0], coords[1], coords[2],
			     coords[3], coords[4], coords[5],
			     coords[6], coords[7], coords[8]);
		if (path != path2 && xxf == x && yyf == y && zzf == z) {
		    path = path2;
		    path.moveTo(xx, yy, zz);
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
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
}

//  LocalWords:  exbundle startx starty startz maxDelta PathIterator
//  LocalWords:  SEG MOVETO IllegalArgumentException needMoveTo
