package org.bzdev.geom;
import java.awt.geom.Point2D;
import java.util.function.Function;

/**
 * Functional interface for mapping one point in a two or three dimensional
 * space to another object when creating a sequence of objects distinguished
 * by an index.
 * @param <R> the type of the objects returned with this mapper is applied
 * @see BezierGrid
 */
@FunctionalInterface
public interface Point3DMapper<R> {

    /**
     * Control-point types for Point3DMapper.
     */
    public static enum Type {
	/**
	 * The second argument to
	 * {@link Point3DMapper#apply(int,Point3D,Type,Point3D...)} is an
	 * initial or ending point of a straight line segment or a
	 * quadratic or cubic B&eacute;zier curve.
	 */
	 KNOT,
	/**
	 * The second argument to
	 * {@link Point3DMapper#apply(int,Point3D,Type,Point3D...)} is
	 * the intermediate control point of a quadratic B&eacute;zier curve.
	 */
	QUADRATIC,
	/**
	 * The second argument to
	 * {@link Point3DMapper#apply(int,Point3D,Type,Point3D...)} is
	 * the first intermediate control point of a cubic B&eacute;zier
	 * curve.
	 */
	FIRST_CUBIC,
	/**
	 * The second argument to
	 * {@link Point3DMapper#apply(int,Point3D,Type,Point3D...)} is
	 * the second intermediate control point of a cubic B&eacute;zier
	 * curve.
	 */
	SECOND_CUBIC
    }

   /**
     * Applies this mapping to the given arguments.
     * @param n the index
     * @param p the point to map to another point
     * @param type the type of control point that describes the second
     *         argument; null if not provided
     * @param bounds there are no optional arguments (any provided
     *        should be ignored) if p is a spline's knot or an end point
     *        of a Path3D line segment; otherwise (i.e., p is an
     *        intermediate control point) there are two arguments
     *        giving the initial and final points in that order for a
     *        segment
     * @return the result of this mapping
     * @see Point3DMapper.Type
     * @see Point3D
     */
    R apply(int n, Point3D p, Type type, Point3D... bounds);

   /**
     * Applies this mapping to the given arguments.
     * The default implementation calls
     * {@link #apply(int,Point3D,Type,Point3D...)} with its third
     * argument set to null.
     * @param n the index
     * @param p the point to map to another point
     * @param bounds there are no optional arguments if p is a
     *        spline's knot or an end point of a Path3D segment;
     *        otherwise (i.e., p is an intermediate control point)
     *        there are two arguments giving the initial
     *        and final points in that order for a segment.
     * @return the result of this mapping
     */
    default R apply(int n, Point3D p, Point3D... bounds) {
	return apply(n, p, null, bounds);
    }


    /**
     * Applies this mapping to the given arguments.
     * The default implementation of this method first converts a
     * Point2D argument to an instance of Point3D with the same
     * X and Y values and with the Z value set to zero. It then
     * calls {@link #apply(int,Point3D,Type,Point3D...)}.
     * @param n the index
     * @param p the point to map to another point
     * @param type the type of control point that describes the second
     *         argument; null if not provided
     * @param bounds there are no optional arguments if p is a
     *        spline's knot or an end point of a Path3D segment;
     *        otherwise (i.e., p is an intermediate control point)
     *        there are two arguments giving the initial
     *        and final points in that order for a segment.
     * @return the result of this mapping
     * @see Point3DMapper.Type
     * @see Point2D
     */
    default R apply(int n, Point2D p, Type type, Point2D... bounds) {
	Point3D np = new Point3D.Double(p.getX(), p.getY(), 0.0);
	Point3D nbounds[] = new Point3D[bounds.length];
	for (int i = 0; i < bounds.length; i++) {
	    Point2D bp = bounds[i];
	    nbounds[i] = new Point3D.Double(bp.getX(), bp.getY(), 0.0);
	}
	return apply(n, np, type, nbounds);
    }

    /**
     * Returns a composed mapping that first applies this mapping
     * and then applies the argument mapping.
     * (This method is provided for consistency with the interfaces
     * in the package {@link java.util.function}.)
     * @param <V> the type of the object returned by the function
     *            <CODE>after</CODE>.
     * @param after the function to apply on the result of this object's
     *        apply method
     * @return the composed mapper
     */
    default <V> Point3DMapper<V>
	andThen(Function<? super R, ? extends V> after)
    {
	Point3DMapper<R> thisMapper = this;
	return new Point3DMapper<V>() {
	    public V apply(int n, Point3D p, Type type, Point3D... bounds) {
		return after.apply(thisMapper.apply(n, p, type, bounds));
	    }
	};
    }
}

//  LocalWords:  BezierGrid spline's DMapper eacute zier
