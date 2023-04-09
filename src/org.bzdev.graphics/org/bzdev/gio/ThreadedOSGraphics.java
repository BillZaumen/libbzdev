package org.bzdev.gio;
import java.io.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.geom.Point2D;

//@exbundle org.bzdev.gio.lpack.Gio

/**
 * Multithreaded OutputStreamGraphics class using a paint method.
 * In order to create some images with some formats (e.g., postscript),
 * the existing class libraries require that one provide a "paint"
 * method, whose argument is a graphics context to create the image. This
 * paint method may be called multiple times with possibly different
 * graphics contexts.  In some cases one call is used to find the size of a
 * bounding box, which is represented near the start of the output stream,
 * and a second call is used to actually create the graphics.  By
 * contrast, the image formats supported by the javax.imageio package
 * can be created by using a BufferedImage to create the image and then
 * using an ImageIO
 * {@link javax.imageio.ImageIO#write(java.awt.image.RenderedImage,String,java.io.OutputStream) write}
 * method.  By contrast, OutputStreamGraphics has a subclass named
 * ImageGraphics that uses an instance of BufferedImage internally and
 * that provides createGraphics method to provide graphics contexts
 * for drawing the image.
 * <P>
 * This class provides a paint method for use by an instance of an
 * interface named GraphicsWriter, whose writeGraphics() method is
 * called in a separate thread from the thread calling the constructor.
 * The paint method creates a Graphics2DRecorder initialized with the
 * graphics context provided by the paint method's argument. The paint
 * method then waits until {@link #imageComplete() imageComplete} is
 * called. Until that happens, one may call
 * {@link #createGraphics() createGraphics} (perhaps multiple times) to
 * obtain graphics contexts and use those graphics contexts to draw the
 * graphics.  After {@link #imageComplete() imageComplete} is called,
 * the paint method continues execution.  On any additional calls to
 * the paint method, the Graphics2DRecorder is used to replay the
 * original sequence of operations.  Finally, the output file or stream is
 * created.
 * <P>
 * Subclasses of this class do not have to explicitly manage threads.
 * They must provide the following:
 * <ul>
 *   <li> the method newGraphicsWriter() to create a GraphicsWriter.
 *   <li> an implementation of GraphicsWriter that calls paint as
 *        needed, triggered by calling the GraphicsWriter method
 *        {@link org.bzdev.gio.ThreadedOSGraphics.GraphicsWriter#writeGraphics()  writeGraphics}.
 * </ul>  
 */
public abstract class ThreadedOSGraphics
    extends OutputStreamGraphics implements GraphicsCreator
{
    static String errorMsg(String key, Object... args) {
	return OutputStreamGraphics.errorMsg(key, args);
    }

    /**
     * Interface to be implemented by classes used as components of
     * subclasses of ThreadedOSGraphics.
     * This interface provides a single method that will trigger operations
     * that ultimate call a paint method of a subclass of ThreadedOSGraphics.
     * a Class implementing this interface will have its constructor called
     * by the {@link ThreadedOSGraphics#newGraphicsWriter() newGraphicsWriter}
     * method of a subclass of ThreadedOSGraphics, which in turn is called
     * by the constructor of {@link ThreadedOSGraphics},
     * <P>
     * Note: if an object implementing this interface is dependent on
     * whether or not an alpha channel is present, the implementation
     * of {@link ThreadedOSGraphics#newGraphicsWriter()} is
     * responsible for setting up the GraphicsWriter appropriately.
     */
    public static interface GraphicsWriter {

	/**
	 * Write to a  graphics file or stream.
	 * This method is required to call a ThreadedOSGraphics
	 * paint method and is called by the constructor of a
	 * ThreadedOSGraphics.
	 * @throws Exception an error occurred
	 */
	void writeGraphics() throws Exception;
    }

    /**
     * Create a new instance of GraphicsWriter.
     * This is called by the constructor of ThreadedOSGraphics after
     * its superclass has been initialized.
     * @return a new graphics writer
     */
    protected abstract GraphicsWriter newGraphicsWriter();

    GraphicsWriter writer;

    volatile boolean continueWriting = false;

    /**
     * Get an additional implementation-specific scale factor.
     * This method should be overridden in cases where the print
     * area cannot be set directly (e.g., for postscript) and
     * where the implementation will have to scale the dimensions
     * so they will fit into the space provided. Currently (Java
     * version 1.7 when this was written), Java postscript printing
     * does not seem to let you set the dimensions of the area that will
     * be displayed without clipping, so scaling is necessary.  Other
     * formats may have similar issues.
     * <P>
     * The default implementation returns a value of 1.0 and that value
     * will not be used to add an affine transformation.
     * @param orientation the image orientation
     * @return the scale factor
     */
    protected double getImplScaleFactor(ImageOrientation orientation) {
	return 1.0;
    }

    /**
     * Draw the image.
     * This method may be called multiple times.  A subclass may
     * have to override this method to make it visible to other
     * classes in a package. This should be done as follows:
     * <blockquote><pre><code>
     *    protected void paint(Graphics2D g2d) {
     *       super.paint(g2d);
     *    }
     * </CODE></PRE></blockquote>
     * It is intended to be called from a thread that the constructor
     * for this class (ThreadedOSGraphics) creates where the
     * writeGraphics method of a GraphicsWriter is called.  The method
     * paint may be called multiple times. On the first call, the
     * graphics operations are performed.  On subsequent calls, these
     * are replayed transparently.  When paint is called the first
     * time, the thread calling paint will block until all the
     * graphics operations that are recorded have been completed.
     * @param g2d the graphics context
     */
    protected void paint(Graphics2D g2d) {
	Graphics2D oldg2d = g2d;
	if (recorder == null || writeException == null) {
	    double sf = getScaleFactor();
	    double isf = getImplScaleFactor(getOrientation());
	    Point2D tp = getTranslation();
	    double xt = tp.getX();
	    double yt = tp.getY();
	    if (sf != 1.0 || isf != 1.0 || xt != 0.0 || yt != 0.0) {
		Graphics g = g2d.create();
		if (g instanceof Graphics2D) {
		    g2d = (Graphics2D) g;
		}

		if (xt != 0.0 || yt != 0.0) {
		    g2d.translate(xt, yt);
		}
		if (sf != 1.0 || isf != 1.0) {
		    double scale = sf*isf;
		    g2d.scale(scale, scale);
		}
	    }
	}

	if (recorder == null) {
	    recorder = new Graphics2DRecorder(g2d);
	    colorModel = g2d.getDeviceConfiguration().getColorModel();
	    synchronized(writeThread) {
		writeThread.notify();
	    }
	    synchronized(recorder) {
		try {
		    while (!continueWriting) {
			recorder.wait();
		    }
		} catch(InterruptedException ie) {
		    writeException = ie;
		} catch (IllegalMonitorStateException ime) {
		    writeException = ime;
		}
	    }
	} else if (writeException == null) {
	    recorder.playback(g2d);
	}
	if (oldg2d != g2d) g2d.dispose();
    }

    Graphics2DRecorder recorder = null;
    Thread writeThread;
    boolean writingDone = false;
    Exception writeException = null;

    /**
     * Log an exception.
     * This should be set by subclasses if an error occurs. After
     * it is called with a non-null argument, subsequent calls are
     * ignored.
     * @param e the exception
     */
    protected void setWriteException(Exception e) {
	if (writeException == null) writeException = e;
    }


    /**
     * Constructor.
     * The parameters (some of them) will typically be passed to a
     * constructor for a class implementing GraphicsWriter. An instance
     * of GraphicsWriter will be created, and a thread that will
     * call the GraphicsWriter's writeGraphics method will be
     * started. writeGraphics will call
     * {@link #paint(java.awt.Graphics2D) paint}, which will block
     * when it is safe for the constructor to return.
     * @param os an output stream to which a image file will be
     *        written.
     * @param width the image width
     * @param height the image height
     * @param orientation the image orientation
     * @param type the string "ps"
     * @param prefersAlpha true if an alpha channel is desired (but
     *        ignored for postscript); false  otherwise
     */
    protected ThreadedOSGraphics(OutputStream os, int width, int height,
				 ImageOrientation orientation, String type,
				 boolean prefersAlpha)
    {
	super(os, width, height, orientation, type, prefersAlpha);
	writer = newGraphicsWriter();
	writeThread = new Thread() {
		public void run() {
		    try {
			writer.writeGraphics();
		    } catch (Exception ep) {
			writeException = ep;
		    }
		    synchronized (ThreadedOSGraphics.this) {
			writingDone = true;
			ThreadedOSGraphics.this.notify();
		    }
		}
	    };
	writeThread.start();
	synchronized (writeThread) {
	    try {
		while (recorder == null) {
		    writeThread.wait();
		}
	    } catch(InterruptedException ie) {
		String msg = errorMsg("tosgConstrFailed");
		throw new
		    RuntimeException(msg, ie);
	    } catch (IllegalMonitorStateException ime) {
		String msg = errorMsg("tosgConstrFailed");
		throw new
		    RuntimeException(msg, ime);
	    }
	}
    }

    ColorModel colorModel = null;
    public ColorModel getColorModel() {
	return colorModel;
    }


    /**
     * Get a graphics context for constructing the image file.
     * @return the graphics context
     */
    public Graphics2D createGraphics() {
	return recorder.createGraphics();
    }

    boolean done = false;

    /**
     * Complete writing the image file to the image stream.
     * Does not close the stream, but will flush it.
     * @exception IOException IO failure or a PrintException (which will be
     *            provided as the cause of the IOException)
     */
    @Override
    public void imageComplete() throws IOException {
	if (done) throw new IOException(errorMsg("imageComplete"));
	done = true;
	synchronized(recorder) {
	    continueWriting = true;
	    recorder.notify();
	}
	synchronized(this) {
	    try {
		while (writingDone == false) {
		    wait();
		}
	    } catch(InterruptedException ie) {
		writeException = ie;
	    } catch (IllegalMonitorStateException ime) {
		writeException = ime;
	    }
	    if (writeException != null) {
		String msg = errorMsg("cannotCreateFST", getType());
		throw new
		    IOException(msg, writeException);
	    }
	}
    }
}

//  LocalWords:  exbundle Multithreaded OutputStreamGraphics javax ul
//  LocalWords:  imageio BufferedImage ImageIO ImageGraphics li pre
//  LocalWords:  createGraphics GraphicsWriter writeGraphics affine
//  LocalWords:  DRecorder imageComplete Subclasses newGraphicsWriter
//  LocalWords:  subclasses ThreadedOSGraphics superclass blockquote
//  LocalWords:  GraphicsWriter's os ps prefersAlpha tosgConstrFailed
//  LocalWords:  IOException PrintException cannotCreateFST
