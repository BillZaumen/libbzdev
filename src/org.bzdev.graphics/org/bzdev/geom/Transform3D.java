 package org.bzdev.geom;

/**
 * Common interface for 3D transforms.
 * This interface defines a general 3D transform.
 */
public interface Transform3D {
    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as double-precision numbers and the destination
     * points' coordinates specified as double-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * <P>
     * If srcPts and dstPts are the same array, overlaps will be handle
     * automatically unless documentation for a subclass states otherwise.
     * @param srcPts the points to transform
     * @param srcOff the offset into srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset into dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    void transform(double[] srcPts, int srcOff, double[]dstPts,
		   int dstOff, int numPts);

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as double-precision numbers and the destination
     * points' coordinates specified as single-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     *
     */
    void transform(double[] srcPts, int srcOff, float[] dstPts,
		   int dstOff, int numPts);

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as single-precision numbers and the destination
     * points' coordinates specified as single-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * <P>
     * If srcPts and dstPts are the same array, overlaps will be handle
     * automatically unless documentation for a subclass states otherwise.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    void transform(float[]srcPts, int srcOff, float[]dstPts,
		   int dstOff, int numPts);

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as single-precision numbers and the destination
     * points' coordinates specified as double-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    void transform(float[]srcPts, int srcOff, double[]dstPts,
			  int dstOff, int numPts);

    /**
     * Apply this transform to a single point, optionally storing the
     * transformed value in a specified point.
     * Note: ptSrc and ptDst can be the same point.
     * @param ptSrc the untransformed point
     * @param ptDst the point to be transformed (a new point will
     *        be created if the value is null)
     * @return the transformed point
     */
    Point3D transform(Point3D ptSrc, Point3D ptDst);


    /**
     * Get the AffineTransform3D that approximates this transform
     * in a neighborhood of a point whose coordinates are (x, y, z).
     * <P>
     * If a transform is represented by the equations
     * <pre>
     *    x' = f(x,y,z)
     *    y' = g(x,y,z)
     *    z' = h(x,y,z)
     * </pre>
     * and the first partial derivatives of f, g, and h exist, then
     * we can approximate the transform at a point
     * (x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) by
     * <pre>
     *    x' = f(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) + f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(x-x<sub>0</sub>) + f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(y-y<sub>0</sub>) + f<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(z-z<sub>0</sub>)
     *    y' = g(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) + g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(x-x<sub>0</sub>) + g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(y-y<sub>0</sub>) + g<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(z-z<sub>0</sub>)
     *    z' = h(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) + h<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(x-x<sub>0</sub>) + h<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(y-y<sub>0</sub>) + h<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)(z-z<sub>0</sub>)
     * </pre>
     * This set of equations represents an affine transform 
     *  whose non-translation components are
     * <pre>
     *   m<sub>00</sub> = f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>10</sub> = g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>20</sub> = h<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>01</sub> = f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>11</sub> = g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>21</sub> = h<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>02</sub> = f<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>12</sub> = g<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     *   m<sub>22</sub> = h<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)
     * </pre>
     * and whose translation components are
     * <pre>
     *    m<sub>03</sub> = f(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) - f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)x<sub>0</sub> - f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)y<sub>0</sub> - f<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)z<sub>0</sub>
     *    m<sub>13</sub> = g(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) - g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)x<sub>0</sub> - g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)y<sub>0</sub> - g<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)z<sub>0</sub>
     *    m<sub>23</sub> = h(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>) - h<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)x<sub>0</sub> - h<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)y<sub>0</sub> - h<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>)z<sub>0</sub>
     * </pre>
     * that is a good approximation to this transform in a sufficiently small
     * neighborhood of the point (x<sub>0</sub>,y<sub>0</sub>,z<sub>0</sub>).
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return an AffineTransform3D that approximates this transform;
     *         null if one does not exist at the point whose coordinates
     *         are (x, y, z)
     * @exception UnsupportedOperationException this operation is not
     *            supported by this transform
     */
    AffineTransform3D affineTransform(double x, double y, double z)
	throws UnsupportedOperationException;

}

//  LocalWords:  AffineTransform srcPts dstPts srcOff dstOff numPts
//  LocalWords:  ptSrc ptDst pre UnsupportedOperationException
