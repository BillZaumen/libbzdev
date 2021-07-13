package org.bzdev.geom;
import java.awt.geom.PathIterator;

/**
 * The PathIterator3D interface provides the ability to iterate
 * over a path in three dimensions.
 * <P>
 * The API is modeled after {@link java.awt.geom.PathIterator} and
 * much of the text was copied from the API documentation for that
 * class, with slight modifications.  The reason to make the interface
 * similar to {@link java.awt.geom.PathIterator} is that programmers
 * are familiar with that API. Constants with the same name in both
 * APIs have the same value.
 * <P>
 * {@link PathIterator3D} is not an extension of
 * {@link java.awt.geom.PathIterator} because {@link PathIterator3D}
 * eliminates a method instead of adding one.
 */
public interface PathIterator3D {
    /**
     * The segment type constant that specifies that the preceding
     * subpath should be closed by appending a line segment back to
     * the point corresponding to the most recent SEG_MOVETO.
     */
    static int SEG_CLOSE = PathIterator.SEG_CLOSE;
    /**
     * The segment type constant for the set of 3 points that specify
     * a cubic parametric curve to be drawn from the most recently
     * specified point.
     */
    static int SEG_CUBICTO = PathIterator.SEG_CUBICTO;
    /**
     * The segment type constant for a point that specifies the end
     * point of a line to be drawn from the most recently specified
     * point.
     */
    static  int SEG_LINETO = PathIterator.SEG_LINETO;
    /**
     * The segment type constant for a point that specifies the
     * starting location for a new subpath.
     */
    static int SEG_MOVETO = PathIterator.SEG_MOVETO;
    /**
     * The segment type constant for the pair of points that specify a
     * quadratic parametric curve to be drawn from the most recently
     * specified point.
     */
    static int SEG_QUADTO = PathIterator.SEG_QUADTO;

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration. The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or
     * SEG_CLOSE. A double array of length 9 must be passed in and can
     * be used to store the coordinates of the point(s). Each point is
     * stored as a triplet of double x,y,z coordinates. SEG_MOVETO and
     * SEG_LINETO types returns one point, SEG_QUADTO returns two
     * points, SEG_CUBICTO returns 3 points and SEG_CLOSE does not
     * return any points.
     * @param coords - an array that holds the data returned from this method
     * @return he path-segment type of the current path segment
     */
    int currentSegment(double[] coords);
    
    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration. The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or
     * SEG_CLOSE. A float array of length 9 must be passed in and can
     * be used to store the coordinates of the point(s). Each point is
     * stored as a triplet of float x,y,z coordinates. SEG_MOVETO and
     * SEG_LINETO types returns one point, SEG_QUADTO returns two
     * points, SEG_CUBICTO returns 3 points and SEG_CLOSE does not
     * return any points.
     * @param coords - an array that holds the data returned from this method
     * @return the path-segment type of the current path segment.
     */
    int currentSegment(float[] coords);

    /**
     * Tests if the iteration is complete.
     * @return true if all the segments have been read; false otherwise.
     */
    boolean isDone();

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    void next();
}

//  LocalWords:  PathIterator APIs subpath SEG MOVETO LINETO QUADTO
//  LocalWords:  CUBICTO coords
