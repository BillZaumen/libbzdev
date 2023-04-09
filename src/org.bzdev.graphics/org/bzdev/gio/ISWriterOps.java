package org.bzdev.gio;
import java.io.*;

/**
 * Common Operations for image-sequence writers.
 * While the class {@link ImageSequenceWriter} is designed to create
 * a document that will be written to an output stream, the class
 * {@link org.bzdev.swing.AnimatedPanelGraphics} allows sequences of
 * images to be displayed directly.  This interface provides common
 * operations.
 */
public interface ISWriterOps {

    /**
     * Interface for classes that provide the parameters needed
     * to configure an image sequence writer by providing a width,
     * height, and frame rate.
     * @see org.bzdev.swing.AnimatedPanelGraphics#newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,boolean,org.bzdev.swing.AnimatedPanelGraphics.Mode)
     * @see org.bzdev.anim2d.Animation2D
     */
    public static interface AnimationParameters {
	/**
	 * Get the width parameter for an object used to configure an
	 * instance of ISWriterOps.
	 * @return the width
	 */
	int getWidthAsInt();

	/**
	 * Get the height parameter for an object used to configure an
	 * instance of ISWriterOps.
	 * @return the height
	 */
	int getHeightAsInt();

	/**
	 * Get the frame rate parameter for an object used to configure an
	 * instance of ISWriterOps.
	 * @return the frame rate
	 */
	double getFrameRate();
    }

    /**
     * Add the metadata entry without a frame rate and format.
     * A default format will be provided if needed.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames; null if
     *        not applicable
     * @exception IOException and IO exception occurred
     * @exception IllegalStateException metadata  was already added
     */
    default void addMetadata(int frameWidth, int frameHeight,
			     String frameMimeType)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, frameMimeType, -1.0, null);
    }

    /**
     * Add the metadata entry without a frame rate.
     * Subclasses that require a format field should document any
     * requirements on the printf string the format supplies. If not
     * needed, the format field will be ignored.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames; null if
     *        not applicable
     * @param format the printf string that will produce the name
     *        of an image given an integer; null for a default value
     * @exception IOException and IO exception occurred
     * @exception IllegalStateException metadata  was already added
     */
    default void addMetadata(int frameWidth, int frameHeight,
			     String frameMimeType, String format)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, frameMimeType, -1.0, format);
    }

    /**
     * Add the metadata entry without a media type and without a format string.
     * Subclasses that require a format field should document any
     * requirements on the printf string the format supplies. When
     * needed, defaults will be provided for the media type and image-name
     * format string.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameRate the number of frames per second
     * @exception IOException an IO error occurred
     * @exception IllegalStateException metadata was already added
     */
    default void addMetadata(int frameWidth, int frameHeight, double frameRate)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, null, frameRate,  null);
    }

    /**
     * Add the metadata entry in full.
     * Subclasses that require a format field should document any
     * requirements on the printf string the format supplies. If not
     * needed, the format field will be ignored.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames
     * @param frameRate the number of frames per second
     * @param format the printf string that will produce the name
     *        of an image given an integer; null for a default value
     * @exception IOException an IO error occurred
     * @exception IllegalStateException metadata was already added
     */
    void addMetadata(int frameWidth, int frameHeight, String frameMimeType,
		     double frameRate, String format)
	throws IOException, IllegalStateException;

    /**
     * Get the frame width.
     * The frame width is the common width of all images in the sequence
     * and is configured by a call to {@link #addMetadata(int,int,String)},
     * {@link #addMetadata(int,int,String,String)},
     * {@link #addMetadata(int,int,double)}, or
     * {@link #addMetadata(int,int,String,double,String)}.
     * If one of these methods has not been called, a value of zero
     * will be returned.
     * @return the frame width
     */
    int getFrameWidth();

    /**
     * Get the frame height.
     * The frame height is the common width of all images in the sequence,
     * and is configured by a call to {@link #addMetadata(int,int,String)},
     * {@link #addMetadata(int,int,String,String)},
     * {@link #addMetadata(int,int,double)}, or
     * {@link #addMetadata(int,int,String,double,String)}.
     * If one of these methods has not been called, a value of zero
     * will be returned.
     * @return the frame height
     */
    int getFrameHeight();

    /**
     * Specify the estimated number of frames as an integer.
     * The estimated frame count is an estimate of the number of images
     * or frames in a sequence.
     * @param count the number of frames; 0 for unknown
     */
    default void setEstimatedFrameCount(int count) {
	setEstimatedFrameCount((long)count);
    }

    /**
     * Specify the estimated number of frames as a long integer.
     * The estimated frame count is an estimate of the number of images
     * or frames in a sequence.
     * @param count the number of frames; 0 for unknown
     */
    void setEstimatedFrameCount(long count);


    /**
     * Create a graphics output stream for drawing graphics.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ISWriterOps isw = ...;
     *    ...
     *    OutputStreamGraphics osg = isw.nextOutputStreamGraphics();
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * @return a graphics output stream for drawing an image
     * @exception IOException - if an IOException occurred
     */
    OSGraphicsOps nextOutputStreamGraphics()
	throws IllegalStateException, IOException;

    /**
     * Create a named graphics output stream for drawing graphics.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ISWriterOps isw = ...;
     *    ...
     *    String name = ...;
     *    OutputStreamGraphics osg = isw.nextOutputStreamGraphics(name);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * The name can be used by the object implementing this interface
     * as a key for looking up an image or other object. How a name
     * is interpreted is class specific.
     * @param name the name for this output stream
     * @return a graphics output stream for drawing an image
     * @exception IOException - if an IOException occurred
     */
    OSGraphicsOps nextOutputStreamGraphics(String name)
	throws IllegalStateException, IOException;

    /**
     * Create a graphics output stream for drawing graphics, specifying a
     * repetition count.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ISWriterOps isw = ...
     *    ...
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    int count = ...;
     *    Graphics2D g2d = osg.createGraphics(count);
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @return a graphics output stream for drawing graphics
     * @exception IOException - if an IOException occurred
     */
    OSGraphicsOps nextOutputStreamGraphics(int count)
	throws IllegalStateException, IOException;

    /**
     * Create a named graphics output stream for drawing graphics,
     * specifying a repetition count.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ISWriterOps isw = ...
     *    ...
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    String name = ...;

     *    int count = ...;
     *    Graphics2D g2d = osg.createGraphics(name, count);
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * The name can be used by the object implementing this interface
     * as a key for looking up an image or other object. How a name
     * is interpreted is class specific.
     * @param name the name for this output stream
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @return a graphics output stream for drawing graphics
     * @exception IOException - if an IOException occurred
     */
    OSGraphicsOps nextOutputStreamGraphics(String name, int count)
	throws IllegalStateException, IOException;


    /**
     * Close this writer.
     * @exception IOException - if an IOException occurred
     */
    void close() throws IllegalStateException, IOException;
}

//  LocalWords:  ImageSequenceWriter newFramedInstance ISWriterOps
//  LocalWords:  AnimationParameters boolean frameWidth frameHeight
//  LocalWords:  frameMimeType IOException IllegalStateException pre
//  LocalWords:  Subclasses printf frameRate addMetadata blockquote
//  LocalWords:  isw OutputStreamGraphics osg createGraphics displose
//  LocalWords:  nextOutputStreamGraphics imageComplete OSGraphicsOps
