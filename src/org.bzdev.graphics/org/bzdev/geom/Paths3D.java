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
 * 3D paths instead of 2D paths.
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
}

//  LocalWords:  exbundle startx starty startz maxDelta PathIterator
//  LocalWords:  SEG MOVETO IllegalArgumentException needMoveTo
