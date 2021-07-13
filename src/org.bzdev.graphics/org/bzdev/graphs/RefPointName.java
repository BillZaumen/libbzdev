package org.bzdev.graphs;

/**
 * Location of a reference point for an object to be drawn or an image.
 * This enum is used by the class {@link Graph Graph} to provide
 * standard names for specific reference points.
 * <P>
 * For the class Graph, the drawImage methods make use of a reference
 * point defined in user space assuming the upper left corner
 * of the image corresponds to position (0,0), with positive x values
 * to the right of a vertical line drawn through this point and positive
 * y values below a horizontal line drawn through this point. With the
 * image positioned at 0.0, the point on the image corresponding to the
 * reference point becomes the point on the image that will appear at
 * the coordinates specified by one of Graph's draw methods, and rotations
 * of the image will occur about this point. A ReferencePointName provides
 * symbolic names for specific reference point locations.
 */
public enum RefPointName {
    /**
     * Upper-left corner.  
     */
    UPPER_LEFT,
    /**
     * Upper-edge at its center.
     */
    UPPER_CENTER,
    /**
     * Upper-right corner.
     */
    UPPER_RIGHT,
    /**
     * Centered vertically at left edge.
     */
    CENTER_LEFT,
    /**
     * Centered relative to the upper and lower edges and the left and
     * right edges.
     */
    CENTER,
    /**
     * Centered vertically at right edge.
     */
    CENTER_RIGHT,
    /**
     * Lower-left corner.
     */
    LOWER_LEFT,
    /**
     * Lower edge at its center.
     */
    LOWER_CENTER,
    /**
     * Lower-right corner.
     */
    LOWER_RIGHT
}

//  LocalWords:  enum drawImage ReferencePointName
