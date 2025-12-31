package org.bzdev.geom;
import java.util.function.BiPredicate;
import org.bzdev.math.VectorOps;
/**
 * Interface representing a two-dimensional shape - a surface embedded
 * in a three-dimensional space.
 * <P>
 * This interface provides the common operations for all surfaces.
 * These include
 * <UL>
 *   <LI> getting a bounding box (a rectangular cuboid whose edges
 *        are parallel to the X, Y, and Z axes.
 *   <LI> getting  ta surface iterator that will allow points on the
 *        surface to be computed.
 *   <LI> testing if the surface has an orientation or not.
 *   <LI> testing if the surface is a closed manifold.
 *   <LI> getting a surface iterator that will describe the surface
 *        as a sequence of planar triangles, cubic B&eacute;zier triangles,
 *        and cubic B&eacute;zier patches.
 *   <LI> getting the boundary of the surface. Null will be returned
 *        if the surface is not a manifold) and the boundary cannot be
 *        determined by some other means.  If the surface is a closed
 *        two-dimensional manifold, an empty path will be returned.
 *        </UL>
 */
public interface Shape3D {

    /**
     * Get a bounding rectangular cuboid for a 3D shape.
     * The edges will be aligned with the X, Y and Z axes.
     * The cuboid created may not be the smallest one possible
     * (for example, shapes defined by B&eacute;zier surfaces
     * may just use the control points to determine the cuboid
     * as the convex hull for the control points includes all
     * of the surface for parameters in the normal range [0,1]).
     *
     * @return a bounding rectangular cuboid for this Shape3D; null
     *         if the shape does not contain any points
     */
    Rectangle3D getBounds();

    /**
     * Get a surface iterator for this Shape3D.

     * The surface iterator will represent the shape as a sequence of
     * B&eacute;zier patches, B&eacute;zier triangles, and/or planar
     * triangles with the order of the sequence arbitrary.

     * <P>
     * Unless the transform is an affine transform, the
     * transformation is not exact.  In this case, the patches
     * and triangles that constitute the surface should be small
     * enough that the transform can be approximated by an affine
     * transform over the region containing the control points.
     * @param tform a transform to apply to each control point; null
     *        for the identity transform
     * @return a surface iterator
     */
    SurfaceIterator getSurfaceIterator(Transform3D tform);


    /**
     * Get a surface iterator for this Shape3D, subdividing the surface.
     * The surface iterator will represent the shape as a
     * sequence of B&eacute;zier patches and B&eacute;zier
     * triangles, with the order of the sequence arbitrary.
     * <P>
     * Unless the transform is an affine transform, the
     * transformation is not exact.  In this case, the patches
     * and triangles that constitute the surface after each is subdivided
     * should be small enough that the transform can be approximated by an
     * affine transform over the region containing the control points.
     * <P>
     * If the iterator returned is an instance of {@link SubdivisionIterator},
     * the method {@link SubdivisionIterator#currentSourceID()} can be used
     * to find the number of times next() must be called for an iterator
     * created by {@link Shape3D#getSurfaceIterator(Transform3D)} (or
     * {@link Shape3D#getSurfaceIterator(Transform3D,int)} with its second
     * argument set to zero) in order to find the corresponding patch or
     * triangle. This can be useful for debugging.
     * @param tform a transform to apply to each control point; null
     *        for the identity transform
     * @param level the number of levels of partitioning (each additional
     *        level splits the previous level into quarters)
     * @return a surface iterator
     */
    default SubdivisionIterator
	getSurfaceIterator(Transform3D tform, int level)
    {
	if (tform == null) {
	    return new SubdivisionIterator(getSurfaceIterator(null), level);
	} else if (tform instanceof AffineTransform3D) {
	    return new SubdivisionIterator(getSurfaceIterator(tform), level);
	} else {
	    return new SubdivisionIterator(getSurfaceIterator(null),
					   tform,
					   level);
	}
    }

    /**
     * Determine if a surface is oriented.
     * @return true if the surface has an orientation; false if it
     *         does not
     */
    boolean isOriented();

    /**
     * Get the boundary for this Shape3D.
     * For a closed surface, the boundary will be an empty path.
     * <P>
     * Typically, a boundary will consist of a series of distinct closed
     * subpaths.  Subpaths are separated by segments whose type is
     * {link PathIterator3D#SEG_MOVETO}.  For a closed manifold, the
     * boundary will be an empty path.
     * @return the boundary of this surface; null if a boundary cannot be
     *         computed
     * @see Path3D#isEmpty()
     */
    Path3D getBoundary();

    /**
     * Get a boundary component given constraints.
     * The method {@link #getBoundary()} may return a path consisting
     * of multiple disjoint subpaths, each typically a closed path.
     * This method tests selected control points from each subpath and
     * returns tha path for which one of these control points is the one
     * closest to a neighbor specified by the first argument.
     * The control points selected are chosen as follows:
     * <UL>
     *  <LI> if last argument (all) is true, all control points are
     *       considered and if false, intermediate control points
     *       along each path segment are ignored.
     *  <LI> if the filter argument is not null, its "test" method is
     *       called and only those control points where this method
     *       returns true are considered. The "test" method takes two
     *       argument:
     *      <UL>
     *         <LI> the first argument is an offset into the array
     *              passed as the second argument
     *         <LI> the second argument is an array of control-point
     *              coordinates in groups of three, with each group
     *              containing the X coordinate followed by the Y
     *              coordinate followed by the Z coordinate.
     *      </UL>
     *  <LI> if the nieghbor is null, an arbitrary subpath with at least
     *       onc control point for which the filter returned true will
     *       be returned.
     *  <LI> if the filter and neighbor are both null, an arbitrary
     *       subpath will be returned.
     * </UL>
     * @param neighbor the test point; null if just the filter should
     *        be used
     * @param filter the control-point filter; null for no filtering
     * @param all true if all control points should be considered before
     *        filtering; false if intermediate control points should be
     *        skipped
     * @return the boundary; null if a boundary could not be computed
     *         or if no boundary matches the constraints.
     */
    default Path3D getBoundary(Point3D neighbor,
			       BiPredicate<Integer,double[]> filter,
			       boolean all)
    {
	Path3D boundary = getBoundary();
	if (boundary == null) return null;

	Path3D paths[] = PathSplitter.split(boundary);
	if (paths.length < 2) {
	    return boundary;
	}

	double mindistsq = Double.POSITIVE_INFINITY;
	boundary = null;
	for (Path3D path: paths) {
	    double[] coords = Path3DInfo.getControlPoints(path, all);
	    for (int i = 0; i < coords.length; i += 3) {
		if (filter != null) {
		    if (!filter.test(i, coords)) {
			continue;
		    }
		}
		if (neighbor == null) {
		    return path;
		}
		double dsq = neighbor
		    .distanceSq(coords[i], coords[i+1], coords[i+2]);
		if (dsq < mindistsq) {
		    mindistsq = dsq;
		    boundary = path;
		}
	    }
	}
	return boundary;
    }


    /**
     * Determine if this Shape3D is a closed two-dimensional manifold.
     * @return true if the surface is a closed two-dimensional manifold;
     *         false otherwise
     */
    boolean isClosedManifold();

    /**
     * Get the number of components for this shape.
     * Components are connected shapes - surfaces for which every point can
     * connect to any other point.
     * @return the number of components for this shape
     * @exception IllegalStateException the surface is ill-formed and its
     *            components (the components of a 2-manifold) cannot be
     *            computed.
     */
    int numberOfComponents();

    /**
     * Get a component of this shape.
     * Components are connected shapes - surfaces for which every point can
     * connect to any other point.
     * The components are referenced by an index, specified as an
     * integer in the range [0,n),  where n is the number of manifold
     * components.  If n is zero, no index is valid.
     * @param i the component's index
     * @return a model containing the specified component
     * @see #numberOfComponents()
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the shape is ill-formed and its
     *            components cannot be  computed.
     */
    Shape3D getComponent(int i)
	throws IllegalArgumentException, IllegalStateException;

}

//  LocalWords:  eacute zier tform subpaths Subpaths PathIterator SEG
//  LocalWords:  MOVETO isEmpty IllegalStateException
//  LocalWords:  numberOfComponents IllegalArgumentException
