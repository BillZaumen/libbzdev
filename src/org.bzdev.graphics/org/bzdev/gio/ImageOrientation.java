package org.bzdev.gio;

/**
 * Image orientation specification.

 * These constants determine an output stream should contain the image
 * as is or if it should rotate the image clockwise or
 * counterclockwise by 90 degrees. These enumeration constants are
 * used by OutputStream Graphics to control the orientation of images
 * when they are outputed.
 *
 */
public enum ImageOrientation {
    /**
     *  No transformation is performed.  This is the default.
     */
    NORMAL,
    /**
     *  An image should be rotated 90 degrees counterclockwise,
     *  The height and width will be swapped and the image will
     *  be translated so that points that fall within a bounding
     *  box whose edges are at (0,0), (WIDTH, 0), (0, HEIGHT),
     *  and (WIDTH, HEIGHT) for a NORMAL orientation will appear within 
     *  a bounding box whose edges are (0,0), (HEIGHT, 0), (0, WIDTH),
     *  and (HEIGHT, WIDTH) for this orientation.
     * 

     */
    COUNTERCLOCKWISE90,
    /**
     * An image should be rotated 90 degrees clockwise.
     *  The height and width will be swapped and the image will
     *  be translated so that points that fall within a bounding
     *  box whose edges are at (0,0), (WIDTH, 0), (0, HEIGHT),
     *  and (WIDTH, HEIGHT) for a NORMAL orientation will appear within 
     *  a bounding box whose edges are (0,0), (HEIGHT, 0), (0, WIDTH),
     *  and (HEIGHT, WIDTH) for this orientation.
     */
    CLOCKWISE90
}

//  LocalWords:  OutputStream outputed
