 package org.bzdev.gio;
import java.awt.Graphics2D;
import java.awt.image.ColorModel;
import java.io.IOException;

/**
 * Basic operations for output stream graphics.
 * This interface represents common operations that classes
 * such as {@link org.bzdev.graphs.Graph} use.
 */
public interface OSGraphicsOps extends GraphicsCreator {

    /**
     * Determine if this instance is requesting an alpha channel.
     * The value may be changed from that provided in the constructor
     * due to the capabilities of a particular image format.
     * @return true if an alpha channel is requested; false otherwise
     */
    boolean requestsAlpha();

    /**
     * Get the image width parameter in user space.
     * Unless a graphics context is modified, this value represents an
     * upper bound on the X coordinate of points that will appear in the
     * image in the coordinate system used by the graphics context independent
     * of the orientation.
     * <P>
     * The parameter typically refers to some object such as a buffered image
     * so that the value returned by this method is constant.
     * @return the width in user-space coordinates
     */
    int getWidth();

    /**
     * Get the image height parameter in user space.
     * Unless a graphics context is modified, this value represents an
     * upper bound on the Y coordinate of points that will appear in the
     * image in the coordinate system used by the graphics context independent
     * of the orientation.
     * <P>
     * The parameter typically refers to some object such as a buffered image
     * so that the value returned by this method is constant.
     * @return the height in user-space coordinates
     */
    public int getHeight();

    /**
     * Close resources.
     * Typically this method will perform some action when
     * a class that implements this interface has an associated
     * output stream or a resource that can be closed. If there
     * are no such resources, this method should simply return.
     * Classes that implement this method should document what
     * they actually do.
     * @exception IOException if an IO error occurred
     */
    void close() throws IOException;

    /**
     * Flush the output.
     * This method will provide a partial image or partial graphics
     * if possible or feasible. After this method is called, the
     * user must not use graphics contexts that were previously
     * created.
     * Whether this method performs any action depends on the
     * implementation of each class or subclass implementing this
     * interface.
     * @see #createGraphics()
     * @exception IOException - if an IO exception occurred
     */
    void flush() throws IOException;

    /**
     * Get the color model for the image that will be produced.
     * @return the color model
     */
    ColorModel getColorModel();

    /**
     * Get a graphics context for drawing.
     * The graphics context created is not valid after
     * {@link #flush()} or {@link #reset()} is called.
     * @return a new graphics context.
     * @exception UnsupportedOperationException this operation is not
     *            supported, typically because the implementation can
     *            only create an instance of Graphics, not Graphics2D
     *            (something that would rarely, if ever, occur in
     *            practice)
     */
    Graphics2D createGraphics() throws UnsupportedOperationException;

    /**
     * Test if the method {@link #reset()} is supported.
     * @return true if {@link #reset()} is supported; false otherwise
     */
    boolean canReset();

    /**
     * Reset this graphics output stream.
     * This is an optional operation as it is appropriate for some
     * graphics output streams but not others
     * @exception UnsupportedOperationException an instance does not
     *            support resets
     */
    void reset() throws UnsupportedOperationException;

   /**
     * Final processing for writing an image file to the output stream.
     * This method does not close an IO stream, but will flush it.
     * <P>
     * Subclasses should implement this method so that it throws an
     * exception if called multiple times without a successful intervening call
     * to {@link #reset()}. The method {@link #canReset()} can be called to
     * test if resets are allowed.
     * @exception IOException IO failure, or a PrintException (which will be
     *            provided as the cause of the IOException), or an
     *            indication that this method was called multiple times
     */
    void imageComplete() throws IOException;
}

//  LocalWords:  IOException createGraphics Subclasses canReset
//  LocalWords:  UnsupportedOperationException PrintException
