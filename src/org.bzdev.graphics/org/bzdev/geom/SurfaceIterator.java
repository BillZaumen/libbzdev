package org.bzdev.geom;
import java.awt.Color;

/**
 * An iterator describing a surface embedded in a space that has
 * three dimensions.
 * The surface is described by a series of segments, with the
 * segments appearing in an arbitrary order.
 */
public interface SurfaceIterator
{

    /**
     * A segment of a surface is a cubic B&eacute;zier patch.
     * For this type of segment, there are 16 control points for a
     * total of 48 double- or single-precision values.
     */
    static int CUBIC_PATCH = 0;

    /**
     * A segment of a surface is a cubic B&eacute;zier triangle
     * For this type of segment, there are 10 control points for a
     * total of 30 double- or single-precision values.
     */
    static int CUBIC_TRIANGLE = 1;

    /**
     * A segment of a surface is a planar triangle
     * For this type of segment, there are 3 control points for a
     * total of 9 double- or single-precision values.
     */
    static int PLANAR_TRIANGLE = 2;


    /**
     * A segment of a surface is a cubic-vertex triangle.
     * For this type of segments, there are 5 control points for a
     * total of 15 double- or single-precision values.
     * The first 4 control points define a B&eacute;zier curve and
     * the last control points defines a vertex.  The surface
     * segment consists of all points on the B&eacute;zier curve
     * connected to the vertex by straight-line segments.
     */
    static int CUBIC_VERTEX = 3;

    /**
     * Get the current segment using double-precision values.
     * The length of the argument array should be at least
     * 48. The number of elements used depends on the mode.
     * The format for the argument array is given by
     * <UL>
     *   <LI>{@link Surface3D#addCubicPatch(double[])} for
     *        {@link #CUBIC_PATCH}.
     *   <LI>{@link Surface3D#addCubicTriangle(double[])} for
     *       {@link #CUBIC_TRIANGLE}.
     *   <LI>{@link Surface3D#addPlanarTriangle(double[])} for
     *       {@link #PLANAR_TRIANGLE}.
     * </UL>
     * @param coords an array to store the values
     * @return one of the constants {@link #CUBIC_PATCH},
     *         {@link #CUBIC_TRIANGLE}, or {@link #PLANAR_TRIANGLE}
     *
     */
    public  int currentSegment(double[] coords);

    /**
     * Get the current segment using single-precision values.
     * The length of the argument array should be at least
     * 48.
     * The format for the argument array is given by
     * <UL>
     *   <LI>{@link Surface3D#addCubicPatch(double[])} for
     *        {@link #CUBIC_PATCH}.
     *   <LI>{@link Surface3D#addCubicTriangle(double[])} for
     *       {@link #CUBIC_TRIANGLE}.
     *   <LI>{@link Surface3D#addPlanarTriangle(double[])} for
     *       {@link #PLANAR_TRIANGLE}.
     * </UL>
     * @param coords an array to store the values
     * @return one of the constants {@link #CUBIC_PATCH},
     *         {@link #CUBIC_TRIANGLE}, or {@link #PLANAR_TRIANGLE}
     */
    public int currentSegment(float[] coords);

    // public int getWindingRule();
    
    /**
     * Return true if iteration is complete.
     * @return true if iteration is complete; false otherwise
     */
    boolean isDone();

    /**
     * Return the tag for the current segment.
     * A tag is typically a stack trace indicating where a patch
     * was created.
     * @return the segment's tag; null if there is none
     */
    Object currentTag();

    /**
     * Return the color for the current segment.
     * @return the segment's color; null if none was defined
     */
    Color currentColor();

    /**
     * Return true if a segment is from an oriented surface;
     * false otherwise
     */
    boolean isOriented();

    /**
     * Move to the next segment.
     */
    void next();
}

//  LocalWords:  eacute zier addCubicPatch addCubicTriangle coords
//  LocalWords:  addPlanarTriangle getWindingRule
