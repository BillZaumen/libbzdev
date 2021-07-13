package org.bzdev.geom;
import java.awt.geom.*;

/**
 * Common interface for 2D transforms.
 * This interface defines a general 2D transform.
 * <P>
 * Note:
 */
public interface Transform2D {
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
    Point2D transform(Point2D ptSrc, Point2D ptDst);


    /**
     * Get the AffineTransform that approximates this transform
     * in a neighborhood of a point whose coordinates are (x, y, z).
     * <P>
     * If a transform is represented by the equations
     * <pre>
     *    x' = f(x,y)
     *    y' = g(x,y)
     * </pre>
     * and the first partial derivatives of f and gexist, then
     * we can approximate the transform at a point
     * (x<sub>0</sub>,y<sub>0</sub>) by
     * <pre>
     *    x' = f(x<sub>0</sub>,y<sub>0</sub>) + f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)(x-x<sub>0</sub>) + f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)(y-y<sub>0</sub>) + f<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>)(z-z<sub>0</sub>)
     *    y' = g(x<sub>0</sub>,y<sub>0</sub>) + g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)(x-x<sub>0</sub>) + g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)(y-y<sub>0</sub>) + g<sub>3</sub>(x<sub>0</sub>,y<sub>0</sub>)(z-z<sub>0</sub>)
     * </pre>
     * This set of equations represents an affine transform 
     *  whose non-translation components are
     * <pre>
     *   m<sub>00</sub> = f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)
     *   m<sub>10</sub> = g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)
     *   m<sub>01</sub> = f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)
     *   m<sub>11</sub> = g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)
     * </pre>
     * and whose translation components are
     * <pre>
     *    m<sub>02</sub> = f(x<sub>0</sub>,y<sub>0</sub>) - f<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)x<sub>0</sub> - f<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)y<sub>0</sub>
     *    m<sub>12</sub> = g(x<sub>0</sub>,y<sub>0</sub>) - g<sub>1</sub>(x<sub>0</sub>,y<sub>0</sub>)x<sub>0</sub> - g<sub>2</sub>(x<sub>0</sub>,y<sub>0</sub>)y<sub>0</sub>
     * </pre>
     * that is a good approximation to this transform in a sufficiently small
     * neighborhood of the point (x<sub>0</sub>,y<sub>0</sub>).
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return an AffineTransform2D that approximates this transform;
     *         null if one does not exist at the point whose coordinates
     *         are (x, y, z)
     * @exception UnsupportedOperationException this operation is not
     *            supported by this transform
     */
    AffineTransform affineTransform(double x, double y)
	throws UnsupportedOperationException;

}

//  LocalWords:  AffineTransform srcPts dstPts srcOff dstOff numPts
//  LocalWords:  ptSrc ptDst pre UnsupportedOperationException
