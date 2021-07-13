package org.bzdev.geom;
import org.bzdev.math.Adder;
import org.bzdev.math.Eigenvalues;
import org.bzdev.math.VectorOps;
import java.awt.Color;


/**
 * Interface for querying a surface.
 */
public interface SurfaceOps extends Shape3D {

    /**
     * Get the number of segments contained in this surface.
     * @return the number of segments contained in this surface
     */
    public int size();

    /**
     * Get the control points for the ith segment for this surface.
     * The control points depend on the segment type.
     * The number entries in the coords array is
     * <UL>
     *   <LI> 48 when the return value is {@link SurfaceIterator#CUBIC_PATCH}.
     *   <LI> 30 when the return value is
     *        {@link SurfaceIterator#CUBIC_TRIANGLE}.
     *   <LI> 12 when the return value is
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}.
     *   <LI> 15 when the return value is
     *        {@link SurfaceIterator#CUBIC_VERTEX}.
     * <UL>
     * In general, the array's length should be at least 48.
     * The format for the array is described in the documentation for
     * <P>
     * Valid indices are non-negative integers smaller than the
     * value returned by {@link #size()}.
     * @param i the index
     * @param coords an array to hold the results
     * @return the segment type ({@link SurfaceIterator#CUBIC_PATCH},
     *         {@link SurfaceIterator#CUBIC_TRIANGLE},
     *         {@link SurfaceIterator#PLANAR_TRIANGLE},
     *         or {@link SurfaceIterator#CUBIC_VERTEX})
     * @exception IllegalArgumentException the index i is out of range
     */
    int getSegment(int i, double[] coords) throws IllegalArgumentException;


    /**
     * Get the tag for the ith segment of this surface.
     * Valid indices are non-negative integers smaller than the
     * value returned by {@link #size()}.
     * @param i the index of the segment
     * @return the tag for this segment
     * @exception IllegalArgumentException the index is out of range
     */
    Object getSegmentTag(int i) throws IllegalArgumentException;


    /**
     * Get the color for the ith segment of this surface.
     * @param i the index of the segment
     * @return the color for this segment; null if none is defined
     * @exception IllegalArgumentException the index is out of range
     */
    Color getSegmentColor(int i) throws IllegalArgumentException;


    /**
     * Get the indices for the segments bordering on this object's boundary.
     * When using a path iterator for the boundary, counting the
     * lineTo and moveTo operations will generate the index, into
     * the array returned, for the corresponding segment's index.
     * @return an array containing the segment indices; null if the surface
     *         is not well formed
     */
    int[] getBoundarySegmentIndices();

    /**
     * Get the edge numbers of the surface segments corresponding to
     * each path segment along the boundary.
     * When using a path iterator for the boundary, counting the
     * lineTo and moveTo operations will generate the index, into
     * the array returned, for the corresponding segment's edge number.
     * <P>
     * The edge numbers are defined as follows:
     * <UL>
     *   <LI> For a planar triangle,
     *       <UL>
     *          <LI> edge 0 is the edge from vertex 1 to vertex 2
     *          <LI> edge 1 is the edge from vertex 2 to vertex 3
     *          <LI> edge 2 is the edge from vertex 3 to vertex 1
     *       <UL>
     *   <LI> For a cubic triangle, described in (u,v) coordinates
     *        where the barycentric coordinates are (u, v, w) with
     *        w = 1 - u - v,
     *       <UL>
     *          <LI> edge 0 is the edge from (0, 0) to (1, 0)
     *          <LI> edge 1 is the edge from (1, 0) to (0, 1)
     *          <LI> edge 2 is the edge from (0, 1) to (0,0)
     *       <UL>
     *   <LI> For a cubic patch, described in (u,v) coordinates
     *       <UL>
     *          <LI> edge 0 is the edge from (0, 0) to (1, 0)
     *          <LI> edge 1 is the edge from (1, 0) to (1, 1)
     *          <LI> edge 2 is the edge from (1, 1) to (0, 1)
     *          <LI> edge 3 is the edge from (0, 1) to (0, 0)
     *       <UL>
     * </UL>
     * <P>
     * @return the edge numbers for the surface segments adjacent to the
     *         path segments along the boundary; null if the 3D shape
     *         is not well formed
     */
    int[] getBoundaryEdgeNumbers();

    /**
     * Compute the center of mass of a shape assuming uniform density.
     * @param shape the shape
     * @return the center of mass
     */
   static Point3D centerOfMassOf(Shape3D shape)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density,
     * specifying whether to use a sequential or parallel computation.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    static Point3D centerOfMassOf(Shape3D shape, boolean parallel,
					 int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape, parallel, size);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.
     * @param shape the shape
     * @param v the volume of the shape.
     * @return the center of mass
     */
    static Point3D centerOfMassOf(Shape3D shape, double v)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape, v);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume, and specifying if the computation should
     * be done in parallel.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    static Point3D centerOfMassOf(Shape3D shape, double v,
					 boolean parallel, int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape, v, parallel, size);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume and a flatness limit.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.  Flatness parameters
     * are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param limit the flatness parameter
     * @return the center of mass
     */
    static Point3D centerOfMassOf(Shape3D shape, double v, double limit)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape, v, limit);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density
     * and given the shape's volume and a flatness limit, and
     * specifying if the computation should be done in parallel.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.  Flatness parameters
     * are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param limit the flatness parameter
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    static Point3D centerOfMassOf(Shape3D shape, double v, double limit,
					 boolean parallel, int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.centerOfMassOf(shape, v, limit, parallel, size);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying an estimate of the number
     * of segments in a shape and whether or not the moments should be
     * computed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param parallel true if the moments should be computed in parallel;
     *        false otherwise
     * @param size an estimate of the number of element in the shape
     *        (ignored if 'parallel' is false)
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p, parallel, size);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p, double v)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p, v);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume and whether
     * or not the computation should be performed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p, double v,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p, v, parallel, size);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume and a
     * flatness parameter.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * Flatness parameters are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param limit the flatness parameter
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p, double v,
				       double limit)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p, v, limit);
    }

        /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume, a
     * flatness parameter, and whether or not the computation should
     * be performed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * Flatness parameters are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param limit the flatness parameter
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the moments
     */
    static double[][] momentsOf(Shape3D shape, Point3D p,
				       double v, double limit,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	return Shape3DHelper.momentsOf(shape, p, v, limit, parallel, size);
    }

    /**
     * Convert moments to moments of inertia.
     * For a moments matrix M, the returned value is the matrix
     * <BLOCKQUOTE><PRE>
     *     | (M<sub>11</sub>+M<sub>22</sub>)   -M<sub>01</sub>      -M<sub>02</sub>    |
     *     |   -M<sub>10</sub>    (M<sub>00</sub>+M<sub>22</sub>)   -M<sub>12</sub>    |.
     *     |   -M<sub>20</sub>      -M<sub>21</sub>    (M<sub>00</sub>+M<sub>11</sub>) |
     * </PRE></BLOCKQUOTE>
     * @param moments a 3x3 matrix containing the moments [the integral of
     * (x<sub>i</sub>-c<sub>i</sub>)((x<sub>j</sub>-c<sub>j</sub>)
     * where <B>c</B> is the location of the center of mass and <B>x</B>
     * is a point on or inside the surface, divided by the volume]
     * @return the moment of inertia matrix for the case where the
     *         volume enclosed has a uniform density and unit mass
     */
    static double[][] toMomentsOfInertia(double[][] moments) {
	double[][] results = new double[3][3];
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (i != j) {
		    results[i][j] = -moments[i][j];
		} else {
		    int ip1 = (i+1)%3;
		    int ip2 = (i+2)%3;
		    results[i][i] = moments[ip1][ip1] + moments[ip2][ip2];
		}
	    }
	}
	return results;
    }

    /**
     * Find the principal moments of a shape given its moments.
     * The array returned contains the moments sorted so that the
     * largest moment appears first.  The argument array can be
     * computed from a shape by calling
     * {@link #momentsOf(Shape3D,Point3D)},
     * {@link #momentsOf(Shape3D,Point3D,boolean,int)},
     * {@link #momentsOf(Shape3D,Point3D,double)},
     * {@link #momentsOf(Shape3D,Point3D,double,boolean,int)},
     * {@link #momentsOf(Shape3D,Point3D,double,double)},
     * {@link #momentsOf(Shape3D,Point3D,double,double,boolean,int)}.
     * or by calling {@link #toMomentsOfInertia(double[][])} on the
     * matrix returned by one of these  <CODE>momentsOf</CODE> methods.
     * <P>
     * The principal moments are the eigenvalues of the moments
     * matrix.
     * @param moments the shape's moments.
     * @return the principal moments
     */
    public static double[] principalMoments(double[][] moments) {
	Eigenvalues ev = new Eigenvalues(moments);
	return (ev.getRealEigenvalues()).clone();
    }

    /**
     * Compute the axes corresponding to the principal moments of a
     * moments matrix.
     * The moments matrix can be computed from a shape by calling the
     * the methods
     * {@link #momentsOf(Shape3D,Point3D)},
     * {@link #momentsOf(Shape3D,Point3D,boolean,int)},
     * {@link #momentsOf(Shape3D,Point3D,double)},
     * {@link #momentsOf(Shape3D,Point3D,double,boolean,int)},
     * {@link #momentsOf(Shape3D,Point3D,double,double)},
     * {@link #momentsOf(Shape3D,Point3D,double,double,boolean,int)},
     * or by calling {@link #toMomentsOfInertia(double[][])} on the
     * matrix returned by one of these  <CODE>momentsOf</CODE> methods.
     * The return value for principalAxes is a three dimensional
     * array.  If the array returned is stored as pmatrix, then
     * pmatrix[i] is an array containing the principal axes
     * corresponding to the i<sup>th</sup> principal moment. Each
     * principal axis vector contains its X component followed by its
     * Y component and is normalized so its length is 1.0.
     * <P>
     * The principal axes are actually the eigenvectors of the moments
     * matrix provided as this method's first argument.
     * @param moments a 3-by-3 matrix containing the moments
     * @return an array of vectors, each providing an axis, with the
     *         vectors corresponding to the principal moments
     */
    static double[][] principalAxes(double[][] moments) {
	Eigenvalues ev = new Eigenvalues(moments);
	double[][] eigenvectors = ev.getVT();

	double[] e2 = VectorOps.crossProduct(eigenvectors[0], eigenvectors[1]);
	if (VectorOps.dotProduct(eigenvectors[2], e2) < 0.0) {
	    // Axes chosen so that using eigenvector 0 as the X axis,
	    // eigenvalue 1 as the Y axis, and eigenvalue 2 as the Z axis
	    // the eigenvectors will point in the direction expected for the
	    // X, Y, Z normal vector <B>i</B>, <B>j</B , <B>k</B>.
	    eigenvectors[2] = e2;
	}
	double R2 = Math.sqrt(1.0/2.0);
	// If the first, second, or third eigenvector points more or less
	// towards the X, Y, or Z axis and the other eigenvectors point
	// away from their corresponding axis, rotate those by 180 degrees.
	if (eigenvectors[0][0] > R2 && eigenvectors[1][1] < -R2 &&
	    eigenvectors[2][2] < -R2) {
	    for (int i = 0; i < eigenvectors.length; i++) {
		eigenvectors[1][i] = -eigenvectors[1][i];
		eigenvectors[2][i] = -eigenvectors[2][i];
	    }
	} else if (eigenvectors[1][1] > R2 && eigenvectors[0][0] < -R2
		   && eigenvectors[2][2] < -R2) {
	    for (int i = 0; i < eigenvectors.length; i++) {
		eigenvectors[0][i] = -eigenvectors[1][i];
		eigenvectors[2][i] = -eigenvectors[2][i];
	    }
	} else if (eigenvectors[2][2] > R2 && eigenvectors[0][0] < -R2
		       && eigenvectors[1][1] < -R2) {
	    for (int i = 0; i < eigenvectors.length; i++) {
		eigenvectors[0][i] = -eigenvectors[1][i];
		eigenvectors[1][i] = -eigenvectors[2][i];
	    }
	}
	for (int i = 0; i < 3; i++) {
	    boolean changed = false;
	    for (int j = 0; j < 3; j++) {
		if (Math.abs(eigenvectors[i][j]) < SurfaceConstants.EPS2) {
		    eigenvectors[i][j]  = 0.0;
		    changed = true;
		}
	    }
	    if (changed) VectorOps.normalize(eigenvectors[i]);
	}
	return eigenvectors;
    }

    /**
     * Get an affine transform to rotate an object so that its
     * 0<sup>th</sup> principal axis lies along the X axis, its
     * 1<sup>st</sup> principal axis lies along the Y axis, and its
     * 2<sup>nd</sup> principal axis lies along the Z axis.
     * <P>
     * As a use case, suppose one has an affine transform at that will
     * shear an object s or reflect it for the
     * case where the principal axes are oriented along the X, Y, and
     * Z directions with the center of mass at (0, 0, 0).
     * Given a moments matrix (e.g., for the moments of inertia)
     * for a shape with an arbitrary orientation, the following code
     * will transform the object without moving its previous center of
     * mass point or rotation it:
     * <BLOCKQUOTE><CODE><PRE>
     *     Point3D cm = SurfaceOps.centerOfMassOf(s);
     *     double[][] moments = SurfaceOps.momentsOf(s, cm)
     *     double[][] I = SurfaceOps.toMomentsOfInertia(moments);
     *     AffineTransform afI =
     *         SurfaceOps.principalAxesTransform(I, cm, true);
     *     AffineTransform af = afI.invert().concatenate(at)
     *         .concatenate(afI);
     *     Surface3D ns = new Surface3D.Double(s, af);
     * </PRE></CODE></BLOCKQUOTE>
     * @param principalAxes the principal axes
     * @param rotCenter the point about which to perform a rotation
     *         (e.g., the center of mass)
     * @param toOrigin true if the center of rotation (rotCenter)
     *        after the rotation should be translated to (0, 0, 0); false
     *        if it should not move.
     * @return the affine transform
     */
    static AffineTransform3D principalAxesTransform(double[][] principalAxes,
						    Point3D rotCenter,
						    boolean toOrigin)
    {
	double x = rotCenter.getX();
	double y = rotCenter.getY();
	double z = rotCenter.getZ();
	AffineTransform3D at = toOrigin?
	    AffineTransform3D.getTranslateInstance(x, y,z):
	    new AffineTransform3D();

	AffineTransform3D rot = new AffineTransform3D
	    (principalAxes[0][0], principalAxes[1][0], principalAxes[2][0],
	     principalAxes[0][1], principalAxes[1][1], principalAxes[2][1],
	     principalAxes[0][2], principalAxes[1][2], principalAxes[2][2],
	     0.0, 0.0, 0.0);
	at.concatenate(rot);
	at.translate(-x, -y, -z);
	return at;
    }

    /**
     * Get a point at a specific location on a segment of a surface.
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX}))
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     * @return the point corresponding to coordinates (u,v)
     */
    static Point3D
	segmentValue(int type, double[] coords, double u, double v)
	throws IllegalArgumentException
    {
	return Shape3DHelper.segmentValue(type, coords, u, v);
    }

    /**
     * Get coordinates for a specific point on a segment of a surface.
     * @param results an array to hold the X, Y, and Z coordinates of
     *        point corresponding to parameters (u,v)
     * @param type the segment type
     *        ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    static void
	segmentValue(double[] results,
		     int type, double[] coords, double u, double v)
	throws IllegalArgumentException
    {
	Shape3DHelper.segmentValue(results, type, coords, u, v);
    }

    /**
     * Get a point at a specific location on a segment of a surface, using
     * barycentric coordinates.
     * <P>
     * The coordinates must satisfy the constraint u + w + v = 1;
     * To compute a value from the other two, use
     * <UL>
     *   <LI> u = 1.0 - (v + w);
     *   <LI> v = 1.0 - (u + w);
     *   <LI> w = 1.0 - (u + v);
     * </UL>
     * This can be done automatically by setting one of the three arguments
     * to -1.0.  If two or three are set to -1.0, an exception will be thrown.
     * @param type the segment type ({@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface; -1 if the value of
     *        u should be computed from the other two parameters
     * @param v the second parameter for the surface; -1 if the value of
     *        v should be computed from the other two parameters
     * @param w the third parameter for the surface; -1 if the value of
     *        w should be computed from the other two parameters
     * @return the point corresponding to coordinates (u,v,w)
     * @exception IllegalArgumentException an argument was out of range,
     *            more than one argument was set to -1, or the type
     *            was not acceptable
     */
    static Point3D
	segmentValue(int type, double[] coords, double u, double v, double w)
	throws IllegalArgumentException
    {
	return Shape3DHelper.segmentValue(type, coords, u, v, w);
    }

    /**
     * Get the coordinates corresponding to a point at a specific
     * location on a segment of a surface, using barycentric
     * coordinates.
     * <P>
     * The coordinates must satisfy the constraint u + w + v = 1;
     * To compute a value from the other two, use
     * <UL>
     *   <LI> u = 1.0 - (v + w);
     *   <LI> v = 1.0 - (u + w);
     *   <LI> w = 1.0 - (u + v);
     * </UL>
     * This can be done automatically by setting one of the three arguments
     * to -1.0.  If two or three are set to -1.0, an exception will be thrown.
     * @param results an array holding the X, Y and Z coordinates of the
     *        desired point, listed in that order
     * @param type the segment type ({@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface; -1 if the value of
     *        u should be computed from the other two parameters
     * @param v the second parameter for the surface; -1 if the value of
     *        v should be computed from the other two parameters
     * @param w the third parameter for the surface; -1 if the value of
     *        w should be computed from the other two parameters
     * @exception IllegalArgumentException an argument was out of range,
     *            more than one argument was set to -1, or the type
     *            was not acceptable
     */
    static void
	segmentValue(double[] results,
		     int type, double[] coords, double u, double v, double w)
	throws IllegalArgumentException
    {
	Shape3DHelper.segmentValue(results, type, coords, u, v, w);
    }

    /**
     * Get components of the "u" tangent vector at a specific point on a
     * segment of a surface. The value of the vector is &part;p/&part;u
     * where p is a point for parameters (u,v). For cases where
     * barycentric coordinates are used p(u,v,1-(u+v)) is differentiated.
     * @param results an array to hold the X, Y, and Z components of
     *        the tangent vector for the u direction when the parameters
     *        are (u,v)
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    static void uTangent(double[] results, int type,
				double[] coords, double u, double v)
    {
	Shape3DHelper.uTangent(results, type, coords, u, v);
    }

    /**
     * Get components of the "v" tangent vector at a specific point on a
     * segment of a surface. The value of the vector is &part;p/&part;v
     * where p is a point for parameters (u,v). For cases where
     * barycentric coordinates are used p(u,v,1-(u+v)) is differentiated.
     * @param results an array to hold the X, Y, and Z components of
     *        the tangent vector for the u direction when the parameters
     *        are (u,v)
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    static void vTangent(double[] results, int type,
				double[] coords, double u, double v)
    {
	Shape3DHelper.vTangent(results, type, coords, u, v);
    }

    /**
     * Configure the number of points used in an area computation for
     * Gaussian-Legendre integration.
     * The default for both is 8.  If values are set to be less than 8,
     * they will be increased to 8, so giving values of 0 will produce
     * the default.  Normally the default should be adequate.  Setting
     * higher values is useful for testing.  One should note that increasing
     * the values improves the accuracy for each segment in the absence of
     * floating-point errors, but increases the number of values summed.
     * @param nCT the number of points for cubic B&eacute;zier triangles
     * @param nCP the number of points for cubic B&eacute;zier patches
     */
    static void configArea(int nCT, int nCP) {
	Shape3DHelper.configArea(nCT, nCP);
    }

    /**
     * Add the surface area for those segments associated with a
     * surface iterator to an Adder.
     * The surface iterator will be modified.
     * @param adder the adder
     * @param si the surface iterator
     */
    static void addAreaToAdder(Adder adder, SurfaceIterator si) {
	Shape3DHelper.addAreaToAdder(adder, si);
    }

    /**
     * Add the surface-integral contributions, used in computing a volume
     * for those segments associated with a surface iterator, to an Adder.
     * The surface iterator will be modified.  The total added to the
     * adder will be 3 times the contribution of the iterator's patches
     * to the volume of the shape it represents or partially represents.
     * <P>
     * The volume assumes the surface is well formed and is embedded in
     * a Euclidean three-dimensional space. The algorithm first (in effect)
     * translates an arbitrary reference point (rx, ry, rz) to the origin
     * and then computes the integral of the vector (x, y, z) over the
     * translated surface. The divergence of this vector is the constant 3,
     * and Gauss's theorem states that the integral of the divergence of a
     * vector field v over a volume bounded by a surface S is equal to the
     * the surface integral of v over S (the integral over the surface of
     * the dot product of v and the normal to the surface).  Since the
     * divergence is 3, this surface integral's value is 3 times the volume.
     * It is the caller's responsibility to divide by this factor of 3 at
     * the appropriate point.
     * <P>
     * A reasonable choice of the reference point is the center of the
     * surface's bounding box, a heuristic that helps reduce floating-point
     * errors.
     * @param adder the adder
     * @param si the surface iterator
     * @param refPoint a reference point
     */
    static void addVolumeToAdder(Adder adder, SurfaceIterator si,
					Point3D refPoint)
    {
	Shape3DHelper.addVolumeToAdder(adder, si, refPoint);
    }
}

//  LocalWords:  ith coords SurfaceIterator IllegalArgumentException
//  LocalWords:  lineTo moveTo barycentric SurfaceIntegral BLOCKQUOTE
//  LocalWords:  RealValuedFunctThreeOps PRE dV xy xz yx yz zx zy ij
//  LocalWords:  momentsOf boolean toMomentsOfInertia principalAxes
//  LocalWords:  pmatrix th affine nd SurfaceOps centerOfMassOf afI
//  LocalWords:  AffineTransform principalAxesTransform af ns nCT nCP
//  LocalWords:  rotCenter toOrigin eacute zier iterator's rx ry rz
//  LocalWords:  si refPoint
