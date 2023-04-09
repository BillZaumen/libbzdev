package org.bzdev.anim2d;
import org.bzdev.devqsim.SimFunction;

import org.bzdev.scripting.ScriptingContext;
import org.bzdev.graphs.GraphCreator;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graph.Just;
import org.bzdev.graphs.Graph.BLineP;
import org.bzdev.devqsim.Simulation;
import org.bzdev.lang.Callable;
import org.bzdev.imageio.ImageMimeInfo;
import org.bzdev.util.ErrorMessage;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ISWriterOps.AnimationParameters;
import org.bzdev.gio.OSGraphicsOps;
import org.bzdev.gio.SurrogateGraphics2D;
import org.bzdev.io.DirectoryAccessor;
import org.bzdev.util.SafeFormatter;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.*;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Class for scheduling the sequence of events for a 2D animation.
 * Nearly all the methods are inherited from the Simulation class.
 * Simulation time is represented in integral units called 'ticks',
 * implemented as long integers.  The constructors set the number
 * of ticks per animation frame and the number of ticks per second.
 * With this choice of units,the method
 * {@link org.bzdev.devqsim.Simulation#currentTicks() currentTicks()}
 * provides the time in ticks whereas the method
 * {@link org.bzdev.devqsim.Simulation#currentTime() currentTime()} provides the
 * time in seconds (i.e., dividing the number of ticks per second by
 * the number of ticks per frame provides the frame rate in units of
 * frames per second).
 * <P>
 * Users should first create an instance of this class and call the
 * method {@link Animation2D#initFrames(int,String,String) initFrames(int,String,String)}
 * (or {@link Animation2D#initFrames(int,String,org.bzdev.gio.ISWriterOps) initFrames(int,String,ISWriterOps}).
 * Various subclasses of {@link AnimationObject2D AnimationObject2D} will
 * have to be created as well, and simulation events will have to be scheduled.
 * The method {@link Animation2D#scheduleFrames(long,int) scheduleFrames}
 * will schedule events needed to create a sequence of frames, and may be
 * called multiple times as long as the sequences of frames specified do not
 * overlap and are in the order in which they will appear.
 * To produce the simulation, the method
 * {@link org.bzdev.devqsim.Simulation#run() run} can be used, with additional
 * arguments if the animation generates a non-terminating sequence of events.
 * <P>
 * Some of the constructors allow the use of a parent simulation or
 * scripting context. Because a simulation and its parent (if the
 * parent is a simulation) share the same event queue and the look-up
 * methods allow a simulation to find objects in a parent simulation,
 * it is relatively easy to add an animation to an existing
 * simulation, whether to create a visual representation of a
 * simulation or to simply create a graph that varies with time.  One
 * add can multiple, independent animations to the same simulation if
 * desired.
 * <P>

 * The method {@link #setImageType(Graph.ImageType)} can be used to
 * change the image type of the animation's graph's buffered image.
 * The default should be changed if the animation is to have a transparent
 * background: some formats (e.g., JPEG) do not work when the graph's
 * image type includes an alpha channel. Similarly, the method {@link
 * #requestAlpha(boolean)} can be used to configure graphs that use an
 * instance of {@link ISWriterOps}. While not applicable to the
 * file-based class {@link ImageSequenceWriter}, it is appropriate for
 * instances of {@link ISWriterOps} associated with graphics displays.
 *
 * @see org.bzdev.devqsim.Simulation
 */
public class Animation2D extends Simulation
    implements ISWriterOps.AnimationParameters, GraphCreator
{

    static final int DEFAULT_WIDTH = 1600;
    static final int DEFAULT_HEIGHT = 900;

    static final double DEFAULT_TICKS_PER_SECOND = 1000.0;
    static final long  DEFAULT_TICKS_PER_FRAME = 40;

    // ImageSequenceWriter isw;
    ISWriterOps isw;
    DirectoryAccessor da;
    // ISWriterOps currentISW= null;

    private AnimationPath2D ourNullPath;
    private SimFunction ourNullFunction;


    private static ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.anim2d.lpack.Animation2D");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    /**
     * Return the null path for this animation.
     * A null path is used by some of the factories to indicate
     * that a path should be set to null, indicating that an existing
     * function should be removed. This is used in factories that support
     * timelines.
     * @return a null path.
     */

    public AnimationPath2D nullPath() {
	return ourNullPath;
    }

    /**
     * Return the null SimFunction for this animation.
     * A null SimFunction is used by some of the factories to indicate
     * that a function should be set to null, indicating that an existing
     * function should be removed.  This is used in factories that support
     * timelines.
     * <P>
     * A null SimFunction is a SimFunction for which
     * {@link org.bzdev.devqsim.SimFunction#getFunction()} will return null, as
     * opposed to a variable of type SimFunction whose value is null.
     * @return a null SimFunction.
     */
    public SimFunction nullFunction() {
	return ourNullFunction;
    }

    long ticksPerFrame;

    /**
     * Get the number of simulation ticks per frame.
     * @return the number of ticks per frame
     */
    public long getTicksPerFrame() {return ticksPerFrame;}
    
    long endingFrameTick = 0;
    int totalFrames = 0;
    int maxFrames;
    int nframes = 0;
    String filenameTemplate;
    String imageType;
    String imageFileExtension;

								
    TreeSet<AnimationObject2D> zorderSet =
	new TreeSet<AnimationObject2D>(AnimationObject2D.zorderComparator);
    
    void addToZorderSet(AnimationObject2D obj) {
	zorderSet.add(obj);
    }

    void removeFromZorderSet(AnimationObject2D obj) {
	zorderSet.remove(obj);
    }

    /**
     * Get the set of animation objects that are associated with this
     * animation and that are visible, ordered by their z-order.
     * <P>
     * Note: this method would rarely, if ever, be used when an animation
     * is running. It is used mostly when an animation is being developed
     * (for example, the program EPTS uses it to create a background image
     * from an animation that has been configured using a scripting
     * environment but has not been run).
     * @return a sorted set of visible animation objects
     */
    public SortedSet<AnimationObject2D> getObjectsByZorder() {
	return Collections.unmodifiableSortedSet(zorderSet);
    }

    double framePriority = Double.MAX_VALUE;

    /**
     * Set the priority for simulation events that generate frames.
     * The default is the largest value for the primitive type
     * <code>double</code> because normally frames should be created
     * after other simulation events at the current time complete. If
     * this behavior is not desired, the priority can be changed.
     * @param value the priority
     */
    public void setFramePriority(double value) {
	framePriority = value;
    }
    
    /**
     * Get the priority for simulation events that generate frames.
     * @return value the priority
     */
    public double getFramePriority() {return framePriority;}

    boolean initFramesCalled = false;

    boolean supplyGraphicsContexts = true;

    /**
     * Determine how graphics contexts are provided when an animation
     * object is drawn.
     * <P>
     * Setting this mode to true reduces the number of times a new graphics
     * context has to be created and disposed, but some programming errors
     * may cause erratic behavior. Setting it to false is safer but less
     * efficient.
     * @param mode true if Animation2D supplies graphics contexts that
     *        will be shared by all objects at a given time; false if
     *        a graph supplies the graphics contexts each time an object
     *        is drawn
     */
    public void setSupplyGraphicsContexts(boolean mode) {
	supplyGraphicsContexts = true;
    }

    boolean osgMode = false;

    /**
     * Schedule a subsequence of frames.
     * This schedules a subsequence of frames that will be part of the
     * sequence of frames of which an animation consists.
     * An <code>initFrames</code> method must be called before this
     * method is called.
     * @param startingFrameTick the starting time of a frame in units of
     *        simulation ticks
     * @param nframes the number of frames to schedule
     * @exception IllegalStateException the frames are out of order due to
     *            previous calls of this method or the maximum number of
     *            frames (specified in initFrames) would be exceeded
     * @see #initFrames(int,String,ISWriterOps)
     * @see #initFrames(int,String,String)
     * @see #initFrames(int,String,String,DirectoryAccessor)
     */
    public void scheduleFrames(long startingFrameTick, final int nframes)
	throws IllegalStateException
    {
	if (startingFrameTick < endingFrameTick) {
	    throw new IllegalStateException(errorMsg("frameTickOrder"));
	}
	if (totalFrames + nframes > maxFrames) {
	    throw new IllegalStateException
		(errorMsg("maxFrameError", maxFrames));
	}
	totalFrames += nframes;
	this. endingFrameTick = startingFrameTick + nframes * ticksPerFrame;
	
	Callable genFrame = new Callable() {
		int ourNframes = nframes;
		boolean ourg2ds = supplyGraphicsContexts;
		OSGraphicsOps osg = null;
		public void call() {
		    try {
			if (osgMode) {
			    osg  = isw.nextOutputStreamGraphics();
			    graph.setOSGraphics(osg);
			}
		    } catch (IOException e) {
			String msg = errorMsg("nextOSGFailed");
			throw new RuntimeException(msg, e);
		    }
		    graph.clear();
		    Graphics2D g2d = null;
		    Graphics2D g2dGCS = null;
		    if (ourg2ds) {
			g2d = graph.createGraphics();
			g2dGCS = graph.createGraphicsGCS();
		    }
		    try {
			if (ourg2ds) {
			    // Save these and restore for each iteration
			    int remaining = zorderSet.size();
			    if (remaining > 1) {
				AffineTransform af = g2d.getTransform();
				Paint paint = g2d.getPaint();
				Stroke stroke = g2d.getStroke();
				Font font = g2d.getFont();
				AffineTransform afGCS = g2dGCS.getTransform();
				Paint paintGCS = g2dGCS.getPaint();
				Stroke strokeGCS = g2dGCS.getStroke();
				Font fontGCS = g2dGCS.getFont();
				for (AnimationObject2D obj: zorderSet) {
				    obj.addToFrame(graph, g2d, g2dGCS);
				    if ((--remaining) > 0) {
					g2d.setTransform(af);
					g2d.setPaint(paint);
					g2d.setStroke(stroke);
					g2d.setFont(font);
					g2dGCS.setTransform(afGCS );
					g2dGCS.setPaint(paintGCS);
					g2dGCS.setStroke(strokeGCS);
					g2dGCS.setFont(fontGCS);
				    }
				}
			    } else {
				for (AnimationObject2D obj: zorderSet) {
				    obj.addToFrame(graph, g2d, g2dGCS);
				}
			    }
			} else {
			    for (AnimationObject2D obj: zorderSet) {
				obj.addToFrame(graph);
			    }
			}
		    } finally {
			if (ourg2ds) {
			    g2d.dispose();
			    g2dGCS.dispose();
			    Animation2D.this.nframes++;
			    if ((--ourNframes) > 0) {
				scheduleCall(this, ticksPerFrame, framePriority);
			    } else {
				initFramesCalled = false;
			    }
			    try {
				if (osgMode) {
				    graph.write();
				    osg.close();
				} else {
				    String name =
					String.format
					(Locale.ROOT, filenameTemplate,
					 Animation2D.this.nframes);
				    if (isw != null) {
					ImageSequenceWriter iw
					    = (ImageSequenceWriter) isw;
					OutputStream os =
					    iw.nextOutputStream(name, false, 0);
					graph.write(imageType, os);
					os.close();
				    } else if (da != null) {
					OutputStream os =
					    da.getOutputStream(name);
					graph.write(imageType, os);
					os.close();
				    } else {
					graph.write(imageType, name);
				    }
				}
			    } catch (IOException e) {
				ErrorMessage.display(e);
			    }
			}
		    }
		}
	    };
	scheduleCall(genFrame, startingFrameTick - currentTicks(),
		     framePriority);
    }

    /**
     * Initialize a sequence of frames given a file-name prefix and
     * image type.
     * The file-name prefix should not include a format directive
     * (e.g., %03d) as this will be computed based on the maximum
     * number of frames.
     * <P>
     * Once, called, any method names <CODE>initFrames</CODE> may
     * not be called again until the simulation has created the number
     * of frames specified by the first argument. The imageType argument
     * must be a string listed by the method
     * {@link javax.imageio.ImageIO#getWriterFormatNames()} or by
     * {@link org.bzdev.imageio.ImageMimeInfo#getFormatNames()}.
     * @param maxFrames the maximum number of frames in the sequence.
     * @param filenamePrefix the initial part of an image file name
     * @param imageType the type of the images making up a sequence of
     *        frames ("png", "jpeg", etc.)
     * @exception IOException an IO error occurred
     */
    public void initFrames(int maxFrames,
			   String filenamePrefix, String imageType)
	throws IOException
    {
	initFrames(maxFrames, filenamePrefix, imageType, null, null);
    }

    /**
     * Initialize a sequence of frames given a file-name prefix,
     * image type, and directory accessor.
     * The file-name prefix should not include a format directive
     * (e.g., %03d) as this will be computed based on the maximum
     * number of frames.
     * The file-name prefix must not specify a parent directory.
     * <P>
     * Once, called, any method names <CODE>initFrames</CODE> may
     * not be called again until the simulation has created the number
     * of frames specified by the first argument.
     * @param maxFrames the maximum number of frames in the sequence.
     * @param filenamePrefix the initial part of an image file name
     * @param imageType the type of the images making up a sequence of
     *        frames
     * @param da a directory accessor for the directory that will
     *        contain the sequence of images
     * @exception IOException an IO error occurred
     */
    public void initFrames(int maxFrames,
			   String filenamePrefix, String imageType,
			   DirectoryAccessor da)
	throws IOException
    {
	initFrames(maxFrames, filenamePrefix, imageType, null, da);
    }


    /**
     * Initialize a sequence of frames given an image type and image
     * sequence writer.
     * <P>
     * Once, called, any method names <CODE>initFrames</CODE> may
     * not be called again until the simulation has created the number
     * of frames specified by the first argument.
     * @param maxFrames the maximum number of frames in the sequence.
     * @param imageFormat the format name for the images making up a
     *        sequence of frames
     * @param isw an image sequence writer that will store the images
     *        produced by the animation
     * @exception IOException an IO error occurred
     */
    public void initFrames(int maxFrames,
			   String imageFormat,
			   ISWriterOps isw)
	throws IOException, IllegalArgumentException
    {
	if (isw == null) throw new
			     IllegalArgumentException(errorMsg("nullISW"));
	initFrames(maxFrames, null, imageFormat, isw, null);
    }
	

    /**
     * Initialize a sequence of frames given an image sequence writer.
     * The second argument's type is an interface implemented by
     * {@link org.bzdev.gio.ImageSequenceWriter} and
     * {@link org.bzdev.swing.AnimatedPanelGraphics}, so by merely
     * changing this argument, the animation can be either stored in
     * a file (or passed to an output stream), that will contain a
     * sequence of images, or displayed directly in a window.
     * <P>
     * Once, called, any method names <CODE>initFrames</CODE> may
     * not be called again until the simulation has created the number
     * of frames specified by the first argument.
     * @param maxFrames the maximum number of frames in the sequence.
     * @param isw an image sequence writer that will store the images
     *        produced by the animation
     * @exception IOException an IO error occurred
     */
    public void initFrames(int maxFrames, ISWriterOps isw)
	throws IOException, IllegalArgumentException
    {
	initFrames(maxFrames, null, null, isw, null);
    }

    /**
     * Initialize a sequence of frames given a file-name prefix, image
     * type, and an ISWriterOps or DirectoryAccessor to store
     * the sequence of images produced.
     * If a media type instead of an image type is provided as the
     * third argument, the media type must include a '/' (for example,
     * the media type "image/png" that corresponds to the image type "png")).
     * <P>
     * Once, called, any method names <CODE>initFrames</CODE> may
     * not be called again until the simulation has created the number
     * of frames specified by the first argument.
     * @param maxFrames the maximum number of frames in the sequence.
     * @param filenamePrefix the initial part of an image file name
     * @param imageType the type of the images, or the media type for
     *        the images, making up a sequence of frames; null for a
     *        default.
     * @param isw an image sequence writer that will store the images
     *        produced by the animation
     * @param da a directory accessor providing a directory in which
     *        image files will appear
     * @exception IOException an IO error occurred
     */
    private void initFrames(int maxFrames,
			    String filenamePrefix, String imageType,
			    ISWriterOps isw,
			    DirectoryAccessor da)
	throws IOException, IllegalArgumentException
    {
	Graph old = graph;
	if (da == null && isw == null) {
	    osgMode = false;
	    if (itype == null) setImageType(null); // sets to default
	    graph = new Graph(graphWidth, graphHeight, itype);
	} else if (da != null || isw instanceof ImageSequenceWriter) {
	    osgMode = false;
	    if (itype == null) setImageType(null); // sets to default
	    graph = new Graph(graphWidth, graphHeight, itype);
	} else {
	    osgMode = true;
	    graph = new Graph(graphWidth, graphHeight, graphRequestAlpha, isw);
	}
	initGraph(old);
	if (maxFrames < 0) {
	    throw new IllegalArgumentException
		(errorMsg("negMaxFrames", maxFrames));
	}

	if (initFramesCalled) {
	    throw new IllegalStateException(errorMsg("tooEarly"));
	}

	String mimeType;
	int n = 1;
	int i = maxFrames;
	while (i >= 10) {
	    i /= 10;
	    n++;
	}

	if (filenamePrefix == null && imageType == null && da == null
	    && isw != null) {
	    this.maxFrames = maxFrames;
	    this.nframes = 0;
	    initFramesCalled = true;
	    this.isw = isw;
	    this.da = null;
	    // filenameTemplate is needed by this class.
	    // the mimeType and template are passed to all ISWriterOps,
	    // but some will ignore those fields - the are specifically
	    // needed by the ImageSequenceWriter class.
	    this.imageType = "png";
	    mimeType = "image/png";
	    filenameTemplate = "img%0" + n + "d.png";
	    isw.addMetadata(graphWidth, graphHeight, mimeType,
			    ticksPerSecond / ticksPerFrame,
			    filenameTemplate);
	    isw.setEstimatedFrameCount(maxFrames);
	    return;
	}

	if (imageType == null) {
	    imageType = "png";
	    mimeType = "image/png";
	} else if (imageType.indexOf('/') == -1) {
	    mimeType = ImageMimeInfo.getMimeType(imageType);
	    if (mimeType == null) {
		throw new IllegalArgumentException
		    (errorMsg("noMIMEType1", imageType));
	    }
	} else {
	    mimeType = imageType;
	    imageType = ImageMimeInfo.getFormatNameForMimeType(mimeType);
	    if (imageType == null) {
		throw new IllegalArgumentException
		    (errorMsg("noFormatName", mimeType));
	    }
	}
	this.imageType = imageType;
	this.imageFileExtension =
	    ImageMimeInfo.getExtensionForMimeType(mimeType);
	if (filenamePrefix == null) {
	    if (da != null) {
		filenamePrefix = "img";
	    } else if (isw != null) {
		filenamePrefix = "img";
	    } else {
		throw new IllegalArgumentException(errorMsg("fnamePrefix"));
	    }
	}
	filenameTemplate = filenamePrefix + "%0" + n + "d."
	    + imageFileExtension;
	totalFrames = 0;
	this.maxFrames = maxFrames;
	this.nframes = 0;
	initFramesCalled = true;
	this.isw = isw;
	this.da = da;
	if (isw != null) {
	    isw.addMetadata(graphWidth, graphHeight, mimeType,
			    ticksPerSecond / ticksPerFrame, filenameTemplate);
	    isw.setEstimatedFrameCount(maxFrames);
	}
     }

    Graph graph;

    /**
     * Get the graph used to represent image frames.
     * The animation will create its graph when one of the initFrames
     * methods are called.
     * @return the graph; null if the graph has not yet been created
     * @see #initFrames(int,ISWriterOps)
     * @see #initFrames(int,String,ISWriterOps)
     * @see #initFrames(int,String,String,DirectoryAccessor)
     * @see #initFrames(int,String,String)
     */
    public Graph getGraph() {
	return graph;
    }

    /**
     * Get the height of the animation's graph as an integer.
     * The value returned is the same as the height passed to
     * the constructor (or the default height for constructors that
     * do not specify one).
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @return the height
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public int getHeightAsInt() {
	return graphHeight;
    }

    /**
     * Get the width of the animation's graph as an integer.
     * The value returned is the same as the width passed to
     * the constructor (or the default width for constructors that
     * do not specify one).
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @return the width
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public int getWidthAsInt() {
	return graphWidth;
    }

    /**
     * Get the width of the animation's graph.
     * The value returned has the same numerical value as the width passed to
     * the constructor (or the default width for constructors that
     * do not specify one).
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @return the width in user-space units
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public double getWidth(){
	return (double)graphWidth;
    }

    /**
     * Get the height of the animation's graph.
     * The value returned has the same numerical value as the height passed to
     * the constructor (or the default height for constructors that
     * do not specify one).
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @return the height in user-space units
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public double getHeight(){
	return (double)graphHeight;
    }

    /**
     * Set the graph offsets symmetrically.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param x the x offset in user space
     * @param y the y offset in user space
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setOffsets(int x, int y) {
	if (graph == null) {
	    initXL = x;
	    initXU = x;
	    initYL = y;
	    initYU = y;
	} else {
	    graph.setOffsets(x, y);
	}
    }

    /**
     * Set the graph offsets.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param xL the lower x offset in user space
     * @param yL the lower y offset in user space
     * @param xU the upper x offset in user space
     * @param yU the upper y offset in user space
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setOffsets(int xL, int xU, int yL, int yU) {
	if (graph == null) {
	    setOffsetsDelayed = true;
	    initXL = xL;
	    initXU = xU;
	    initYL = yL;
	    initYU = yU;
	} else {
	    graph.setOffsets(xL, xU, yL, yU);
	}
    }

    /**
     * Set the x and y ranges.
     * All values are in graph coordinate space.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param xLower the lower value of the range in the x direction
     * @param xUpper the upper value of the range in the x direction
     * @param yLower the lower value of the range in the y direction
     * @param yUpper the upper value of the range in the y direction
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setRanges(double xLower, double xUpper,
			  double yLower, double yUpper)
    {
	if (graph == null) {
	    setRanges4Delayed = true;
	    setRanges6Delayed = false;
	    initXLower = xLower;
	    initXUpper = xUpper;
	    initYLower = yLower;
	    initYUpper = yUpper;
	} else {
	    graph.setRanges(xLower, xUpper, yLower, yUpper);
	}
    }

    /**
     * Set ranges based on scale factors.
     * A scale factor is the ratio of a distance in user space to a
     * distance in graph coordinate space. If angles are to be
     * preserved, the scale factors for the X and Y directions must be
     * equal. The scale factors are the numbers by which a coordinate
     * difference in graph coordinate space along the X
     * or Y directions respectively must be multiplied to obtain the
     * corresponding difference in user space.
     * <P>
     * In animations where a single image, configured to provide
     * pixels in graph coordinate space units, provides the background
     * for the animation with the same scale factors for the X and Y
     * directions and where one unit in user space matches one unit in
     * image space, the scale factor will be the inverse of
     * the image scale factor used to configure an instance of
     * {@link AnimationLayer2DFactory}. If this number is multiplied
     * by a factor f, the apparent size of the image on the screen will
     * also be multiplied by f.  If the animation's width and height
     * are equal to f multiplied by the image's width and height respectively,
     * the frame will be just large enough to fully contain the image.
     * <P>
     * Note: this method modifies an animation's graph, but may be
     * called before {@link #getGraph()} will return a non-null value.
     * The arguments will be stored and used when the graph is actually
     * created.
     * @param xgcs the x coordinate of a point in graph coordinate
     *        space that will be positioned at a specified location on
     *        the graph
     * @param ygcs the y coordinate of a point in graph coordinate
     *        space  that will be positioned at a specified location on
     *        the graph
     * @param xf the fractional distance from the graph's left offset to its
     *        right offset at which the point xgcs in graph coordinate space
     *        appears
     * @param yf the fractional distance from the graph's lower offset to its
     *        upper offset at which the point ygcs in graph coordinate space
     *        appears
     * @param scaleFactorX the scale factor for the X direction
     * @param scaleFactorY the scale factor for the Y direction
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setRanges(double xgcs, double ygcs, double xf, double yf,
			   double scaleFactorX, double scaleFactorY) {
	if (graph == null) {
	    setRanges6Delayed = true;
	    setRanges4Delayed = false;
	    initXgcs = xgcs;
	    initYgcs = ygcs;
	    initXf = xf;
	    initYf = yf;
	    initScaleFactorX = scaleFactorX;
	    initScaleFactorY = scaleFactorY;
	} else {
	    graph.setRanges(xgcs, ygcs, xf, yf, scaleFactorX, scaleFactorY);
	}
    }

     /**
      * Get the X coordinate for the lower end of this animation's graph's
      * range.
      * This method may be called before the graph is created, provided
      * that {@link #setRanges(double,double,double,double)} or
      * {@link #setRanges(double,double,double,double,double,double)},
      * and possibly {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)}, have been
      * called.
      * @return the X coordinate lower end of this animation's  graph's range
      *         in graph coordinate space
      * @exception IllegalStateException the method
      *            {@link #setRanges(double,double,double,double)} or
      *            {@link #setRanges(double,double,double,double,double,double)}
      *            has not been called
      */
    public double getXLower() {
	if (graph == null) {
	    if (setRanges4Delayed) {
		return initXLower;
	    } else if (setRanges6Delayed) {
		return Graph.getXLower(graphWidth, initXgcs, initXf,
				       initScaleFactorX, initXL, initXU);
	    } else {
		throw new IllegalStateException(errorMsg("noSetRanges"));
	    }
	} else {
	    return graph.getXLower();
	}
    }

     /**
      * Get the Y coordinate for the lower end of this animation's graph's
      * range.
      * This method may be called before the graph is created, provided
      * that {@link #setRanges(double,double,double,double)} or
      * {@link #setRanges(double,double,double,double,double,double)},
      * and possibly {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)}, have been
      * called.
      * @return the Y coordinate lower end of this animation's  graph's range
      *         in graph coordinate space
      * @exception IllegalStateException the method
      *            {@link #setRanges(double,double,double,double)} or
      *            {@link #setRanges(double,double,double,double,double,double)}
      *            has not been called
      */
    public double getYLower() {
	if (graph == null) {
	    if (setRanges4Delayed) {
		return initYLower;
	    } else if (setRanges6Delayed) {
		return Graph.getYLower(graphHeight, initYgcs, initYf,
				       initScaleFactorY, initYL, initYU);
	    } else {
		throw new IllegalStateException(errorMsg("noSetRanges"));
	    }
	} else {
	    return graph.getYLower();
	}
    }

     /**
      * Get the X coordinate for the upper end of this animation's graph's
      * range.
      * This method may be called before the graph is created, provided
      * that {@link #setRanges(double,double,double,double)} or
      * {@link #setRanges(double,double,double,double,double,double)},
      * and possibly {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)}, have been
      * called.
      * @return the X coordinate upper end of this animation's  graph's range
      *         in graph coordinate space
      * @exception IllegalStateException the method
      *            {@link #setRanges(double,double,double,double)} or
      *            {@link #setRanges(double,double,double,double,double,double)}
      *            has not been called
      */
    public double getXUpper() {
	if (graph == null) {
	    if (setRanges4Delayed) {
		return initXUpper;
	    } else if (setRanges6Delayed) {
		return Graph.getXUpper(graphWidth, initXgcs, initXf,
				       initScaleFactorX, initXL, initXU);
	    } else {
		throw new IllegalStateException(errorMsg("noSetRanges"));
	    }
	} else {
	    return graph.getXUpper();
	}
    }

     /**
      * Get the Y coordinate for the upper end of this animation's graph's
      * range.
      * This method may be called before the graph is created, provided
      * that {@link #setRanges(double,double,double,double)} or
      * {@link #setRanges(double,double,double,double,double,double)},
      * and possibly {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)}, have been
      * called.
      * @return the Y coordinate upper end of this animation's  graph's range
      *         in graph coordinate space
      * @exception IllegalStateException the method
      *            {@link #setRanges(double,double,double,double)} or
      *            {@link #setRanges(double,double,double,double,double,double)}
      *            has not been called
      */
    public double getYUpper() {
	if (graph == null) {
	    if (setRanges4Delayed) {
		return initYUpper;
	    } else if (setRanges6Delayed) {
		return Graph.getYUpper(graphHeight, initYgcs, initYf,
				       initScaleFactorY, initYL, initYU);
	    } else {
		throw new IllegalStateException(errorMsg("noSetRanges"));
	    }
	} else {
	    return graph.getYUpper();
	}
    }

     /**
      * Get the lower X offset for this animation's graph.
      * This method may be called before this animation's graph has
      * been created provided that {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)} has been
      * called.
      * Lower and upper refer to the left and right offsets respectively.
      * @return the lower X offset in user-space units
      */
    public int getXLowerOffset() {
	if (graph == null) {
	    return initXL;
	} else {
	    return graph.getXLowerOffset();
	}
    }

     /**
      * Get the lower X offset for this animation's graph.
      * This method may be called before this animation's graph has
      * been created provided that {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)} has been
      * called.
      * Lower and upper refer to the left and right offsets respectively.
      * @return the lower X offset in user-space units
      */
    public int getYLowerOffset() {
	if (graph == null) {
	    return initYL;
	} else {
	    return graph.getYLowerOffset();
	}
    }

     /**
      * Get the upper X offset for this animation's graph.
      * This method may be called before this animation's graph has
      * been created provided that {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)} has been
      * called.
      * Lower and upper refer to the left and right offsets respectively.
      * @return the upper X offset in user-space units
      */
    public int getXUpperOffset() {
	if (graph == null) {
	    return initXU;
	} else {
	    return graph.getXUpperOffset();
	}
    }

     /**
      * Get the upper Y offset for this animation's graph.
      * This method may be called before this animation's graph has
      * been created provided that {@link #setOffsets(int,int)}
      * or {@link #setOffsets(int,int,int,int)} has been
      * called.
      * Lower and upper refer to the left and right offsets respectively.
      * @return the upper Y offset in user-space units
      */
    public int getYUpperOffset() {
	if (graph == null) {
	    return initYU;
	} else {
	    return graph.getYUpperOffset();
	}
    }


    /**
     * Set the color to use when clearing the animation's graph.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param color the background color
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setBackgroundColor(Color color) {
	if (graph == null) {
	    setBackgroundColorDelayed = true;
	    initBackgroundColor = color;
	} else {
	    graph.setBackgroundColor(color);
	}
    }

    /**
     * Set whether or clearing the animation's graph should add the
     * background color to each pixel in the graph or replace the
     * pixels.
     * For an alpha of 255, the appearance does not depend on the
     * mode.  For other values of alpha, when the mode is true, the
     * background color, which is transparent or translucent, will
     * be added to each pixel; when the mode is false, each pixel will
     * be replaced with one having the background color.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param mode true for adding; false for replacing
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setClearByFillMode(boolean mode) {
	if (graph == null) {
	    setClearByFillModeDelayed = true;
	    initCBFMode = mode;
	} else {
	    graph.setClearByFillMode(mode);
	}
    }

    /**
     * Set the font to use for this animation's graph.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param f the font to use
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setFont(Font f) {
	if (graph == null) {
	    setFontDelayed = true;
	    initFont = f;
	} else {
	    graph.setFont(f);
	}
    }

    /**
     * Set the font color for this graph.
     * <P>
     * Note: this method may be called before {@link #getGraph()}
     * will return a non-null value. The arguments will be stored
     * and used when the graph  is actually created.
     * @param c the color; null if the default color should be used
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setFontColor(Color c) {
	if (graph == null) {
	    setFontColorDelayed = true;
	    initFontColor = c;
	} else {
	    graph.setFontColor(c);
	}
    }


    /**
     * Set the font justification for this animation's graph.
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @param j the font justification
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setFontJustification(Just j) {
	if (graph == null) {
	    setFontJustDelayed = true;
	    initFontJust = j;
	} else {
	    graph.setFontJustification(j);
	}
    }

    /**
     * Set the font vertical alignment for this animation's graph.
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @param blp the vertical alignment
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setFontBaseline(BLineP blp) {
	if (graph == null) {
	    setFontBaselineDelayed = true;
	    initBaselineP = blp;
	} else {
	    graph.setFontBaseline(blp);
	}
    }

    /**
     * Set the font angle for this animation's graph.
     * <P>
     * Note: this method is provided for convenience: it simply calls
     * the method with the same name on the animation's graph.
     * @param angle the angle in degrees
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    public void setFontAngle(double angle) {
	if (graph == null) {
	    setFontAngleDelayed = true;
	    initFontAngle = angle;
	} else {
	    graph.setFontAngle(angle);
	}
    }

    double ticksPerSecond;

    /**
     * Get the number of ticks per second.
     * @return the number of ticks per second
     */
    public double getTicksPerSecond() {return ticksPerSecond;}

    /**
     * Get the animation frame rate.
     * @return the animation frame rate in units of frames per second.
     */
    public double getFrameRate() {
	return ticksPerSecond/ticksPerFrame;
    }

    /**
     * Estimate the number of frames needed for an animation to run for a
     * specified time.
     * @param seconds the time the animation should run in seconds
     * @return the corresponding number of frames
     * @exception IllegalArgumentException the argument is out of
                  range - either negative or too large
     */
    public int estimateFrameCount(double seconds)
	throws IllegalArgumentException
    {
	double result = seconds * ticksPerSecond / ticksPerFrame;
	if (seconds < 0.0 || result > Integer.MAX_VALUE) {
	    throw new IllegalArgumentException
		(errorMsg("outOfRange1", seconds));
		/*("argument out of range");*/
	}
	return (int) Math.round(result);
    }

    /*
    private static final Graph.ImageType DEFAULT_IMAGE_TYPE =
	Graph.ImageType.INT_RGB;

    private static final Graph.ImageType DEFAULT_REQUEST_ALPHA_IMAGE_TYPE
	= Graph.ImageType.INT_ARGB_PRE;
    */

    private Graph.ImageType itype = null;
    private boolean graphRequestAlpha = false;

    /**
     * Set the graph image type.
     * <P>
     * When null, the Graph.ImageType.INT_RGB is used.  A useful
     * value when transparency is needed is Graph.ImageType.INT_ARBG_PRE.
     * This is not the default because JPEG images cannot be created
     * when an alpha channel is used.
     * @param type the image type; null for a standard choice
     * @exception IllegalStateException this method was called after
     *            an initFrames method was called
     */
    public void setImageType(Graph.ImageType type)
	throws IllegalStateException
    {
	if (graph != null)
	    throw new IllegalStateException(errorMsg("graphAlreadyCreated"));
	if (type == null) {
	    itype = SurrogateGraphics2D.getGraphImageType(graphRequestAlpha);
	    /*
	    itype = graphRequestAlpha? DEFAULT_REQUEST_ALPHA_IMAGE_TYPE:
		DEFAULT_IMAGE_TYPE;
	    */
	} else {
	    itype = type;
	}
    }

    /**
     * Get the image type for the graph associated with this animation.
     * The value returned after a graph is created (by calling
     * one of the initFrames methods) may not be the value existed
     * before the graph was created: it can differ when an argument
     * to initFrames is an instance of ISWriterOps that is not also an
     * instance of ImageSequenceWriter.
     * <P>
     * Note the value can be changed by calling
     * {@link #setImageType(Graph.ImageType)} but that method cannot
     * be called after the graph is created.  The image type provides
     * the type of a {@link java.awt.image.BufferedImage}. This type
     * should not be confused with the string naming an image format
     * used for a file or entry in an image-sequence writer.
     * @return the image type
     */
    public Graph.ImageType getImageType() {
	if (graph != null) {
	    return graph.getImageType();
	}
	if (itype == null) {
	    return SurrogateGraphics2D.getGraphImageType(graphRequestAlpha);
	    /*
	    return graphRequestAlpha? DEFAULT_REQUEST_ALPHA_IMAGE_TYPE:
		DEFAULT_IMAGE_TYPE;
	    */
	} else {
	    return itype;
	}
    }

    /**
     * Set a flag indicating if an alpha channel is requested for the
     * graph associated with this animation.
     * A value of true is applicable when initFrames is called with an
     * argument specifying an {@link ISWriterOps} that is not an
     * instance of {@link ImageSequenceWriter} or one of its
     * subclasses (if any).
     * @param mode true if an alpha channel was requested; false otherwise
     */
    public void requestAlpha(boolean mode) throws IllegalStateException {
	if (graph != null)
	    throw new IllegalStateException(errorMsg("graphAlreadyCreated"));
	graphRequestAlpha = mode;
    }

    /**
     * Determine if the graph associated with this animation is one
     * for which an alpha channel is requested.
     * The value reported by this method is used for graphs associated
     * with an {@link ISWriterOps} that is not an instance of
     * {@link ImageSequenceWriter} or one of its subclasses (if any).
     * @return true if an alpha channel was requested; false otherwise
     */
    public boolean getRequestAlpha() {
	return graphRequestAlpha;
    }

    
    /**
     * Constructor.
     */
    public Animation2D() {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT,
	     DEFAULT_TICKS_PER_SECOND, DEFAULT_TICKS_PER_FRAME);
    }

    /**
     * Constructor given frame-rate data.
     * @param ticksPerSecond the number of animation time units per second
     * @param ticksPerFrame the number of animation time units per frame
     */
    public Animation2D(double ticksPerSecond, long ticksPerFrame) {
	this(DEFAULT_WIDTH, DEFAULT_HEIGHT, ticksPerSecond, ticksPerFrame);
	
    }

    /**
     * Constructor given a width and height of the animation.
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     */
    public Animation2D(int width, int height) {
	this((Simulation)null, width, height,
	     DEFAULT_TICKS_PER_SECOND, DEFAULT_TICKS_PER_FRAME);
    }

    /**
     * Constructor given a width and height of the animation, and frame-rate
     * data.
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param ticksPerSecond the number of animation time units per second
     * @param ticksPerFrame the number of animation time units per frame
     */
    public Animation2D(int width, int height,
			double ticksPerSecond, long ticksPerFrame)
    {
	this((Simulation)null, width, height,
	     ticksPerSecond, ticksPerFrame);
    }

    /**
     * Constructor with a parent simulation or scripting context.
     * When an animation has a parent, the parent's scripting context
     * is used, unless specific methods are overridden.  When an
     * animation has a parent that is an instance of Simulation, the
     * parent's event queue is used instead of the simulation's event
     * queue and time structures. This allows multiple simulations,
     * perhaps with different flavors, to be combined into a single
     * simulation. While the event queue and simulation time are
     * shared, tables of simulation objects are not.  Running any of
     * the simulations sharing a parent will run all of them.  Default
     * values are used for the image height and width and for the
     * frame-rate data
     *
     * @param parent the simulation's parent
     */
    public Animation2D (ScriptingContext parent) {
	this (parent, DEFAULT_WIDTH, DEFAULT_HEIGHT,
	      DEFAULT_TICKS_PER_SECOND, DEFAULT_TICKS_PER_FRAME);
    }

    /**
     * Constructor with parent simulation and frame-rate data.
     * When an animation has a parent, the parent's scripting context
     * is used, unless specific methods are overridden.  When an
     * animation has a parent that is an instance of Simulation, the
     * parent's event queue is used instead of the simulation's event
     * queue and time structures.  This allows multiple simulations,
     * perhaps with different flavors, to be combined into a single
     * simulation. While the event queue and simulation time are
     * shared, tables of simulation objects are not.  Running any of
     * the simulations sharing a parent will run all of them.
     *
     * @param parent the simulation's parent
     * @param ticksPerSecond the number of animation time units per second
     * @param ticksPerFrame the number of animation time units per frame
     */
    public Animation2D (ScriptingContext parent,
		      double ticksPerSecond, long ticksPerFrame)
    {
	this (parent, DEFAULT_WIDTH, DEFAULT_HEIGHT, ticksPerSecond,
	      ticksPerFrame);
    }

    /**
     * Constructor with parent simulation and an image height and width.
     * When an animation has a parent, the parent's scripting context
     * is used, unless specific methods are overridden.  When an
     * animation has a parent that is an instance of Simulation, the
     * parent's event queue is used instead of the simulation's event
     * queue and time structures. This allows multiple simulations,
     * perhaps with different flavors, to be combined into a single
     * simulation. While the event queue and simulation time are
     * shared, tables of simulation objects are not.  Running any of
     * the simulations sharing a parent will run all of them.  Default
     * values are used for the image height and width and for the
     * frame-rate data
     *
     * @param parent the simulation's parent
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     */
    public Animation2D (ScriptingContext parent, int width, int height) {
	this (parent, width, height,
	      DEFAULT_TICKS_PER_SECOND, DEFAULT_TICKS_PER_FRAME);
    }

    private int graphWidth;
    private int graphHeight;

    private boolean setOffsetsDelayed = false;
    private int initXL = 0;
    private int initXU = 0;
    private int initYL = 0;
    private int initYU = 0;

    private boolean setRanges4Delayed = false;
    private double initXLower = 0.0;
    private double initXUpper = 0.0;
    private double initYLower = 0.0;
    private double initYUpper = 0.0;

    private boolean setRanges6Delayed = false;
    private double initXgcs = 0.0;
    private double initYgcs = 0.0;
    private double initXf = 0.0;
    private double initYf = 0.0;
    private double initScaleFactorX = 0.0;
    private double initScaleFactorY = 0.0;

    private boolean setBackgroundColorDelayed = false;
    private Color initBackgroundColor = null;

    private boolean setClearByFillModeDelayed = false;
    private boolean initCBFMode = false;

    private boolean setFontDelayed = false;
    private Font initFont = null;

    private boolean setFontColorDelayed = false;
    private Color initFontColor = null;

    private boolean setFontJustDelayed = false;
    private Just initFontJust = null;

    private boolean setFontBaselineDelayed = false;
    private BLineP initBaselineP = null;

    private boolean setFontAngleDelayed = false;
    private double initFontAngle = 0.0;
    
    private void initGraph(Graph old) {
	if (old == null) {
	    if (setOffsetsDelayed) {
		setOffsets(initXL, initXU, initYL, initYU);
	    }
	    if (setRanges4Delayed) {
		setRanges(initXLower, initXUpper, initYLower, initYUpper);
	    }
	    if (setRanges6Delayed) {
		setRanges(initXgcs, initYgcs, initXf, initYf,
			  initScaleFactorX, initScaleFactorY);
	    }
	    if (setBackgroundColorDelayed) {
		setBackgroundColor(initBackgroundColor);
	    }
	    if (setClearByFillModeDelayed) {
		setClearByFillMode(initCBFMode);
	    }
	    if (setFontDelayed) {
		setFont(initFont);
	    }
	    if (setFontColorDelayed) {
		setFontColor(initFontColor);
	    }
	    if (setFontJustDelayed) {
		setFontJustification(initFontJust);
	    }
	    if (setFontBaselineDelayed) {
		setFontBaseline(initBaselineP);
	    }
	    if (setFontAngleDelayed) {
		setFontAngle(initFontAngle);
	    }
	} else {
	    setOffsets(old.getXLowerOffset(), old.getXUpperOffset(),
		       old.getYLowerOffset(), old.getYUpperOffset());
	    setRanges(old.getXLower(), old.getXUpper(),
		      old.getYLower(), old.getYUpper());
	    setBackgroundColor(old.getBackgroundColor());
	    setClearByFillMode(old.getClearByFillMode());
	    setFont(old.getFont());
	    setFontColor(old.getFontColor());
	    setFontJustification(old.getFontJustification());
	    setFontBaseline(old.getFontBaseline());
	    setFontAngle(old.getFontAngle());
	}
    }


    /**
     * Constructor with parent simulation and image and frame-rate data.
     * When an animation has a parent, the parent's scripting context is
     * used, unless specific methods are overridden.
     * When an animation has a parent that is an instance of
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them.
     *
     * @param parent the simulation's parent
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param ticksPerSecond the number of animation time units per second
     * @param ticksPerFrame the number of animation time units per frame
     */
    public Animation2D (ScriptingContext parent, int width, int height,
		      double ticksPerSecond, long ticksPerFrame)
    {
	super(parent, ticksPerSecond);
	if (ticksPerFrame <= 0) {
	    throw new IllegalArgumentException
		(errorMsg("negTicksPerFrame", ticksPerFrame));
	}
	if (ticksPerSecond <= 0.0) {
	    String arg = "" + ticksPerSecond;
	    throw new IllegalArgumentException
		(errorMsg("negTicksPerFrame", ticksPerSecond));
	}
	if (width <= 0) {
	    throw new IllegalArgumentException(errorMsg("negWidth", width));
	}
	if (height <= 0) {
	    throw new IllegalArgumentException(errorMsg("negHeight", height));
	}

	graphWidth = width;
	graphHeight = height;
	this.ticksPerSecond = ticksPerSecond;
	this.ticksPerFrame = ticksPerFrame;
	ourNullPath = new AnimationPath2D(this, "null", false);
	ourNullFunction = new SimFunction(this, "null", false, null);
    }

    static int level1 = -1;
    static int level2 = -1;
    static int level3 = -1;
    static int level4 = -1;

    /**
     * Set the levels for org.bzdev.anim2d trace messages.
     * The defaults are -1, indicating that no messages will be
     * printed.  Legal values are -1 or a non-negative integer.
     * For level1, the arguments to various methods will be shown.
     * For level2 or level4, the following parameters are shown
     * <ul>
     *   <li> x - the X position
     *   <li> y - the Y position
     *   <li> angle - the angle in radians (absolute).
     * </ul>
     * the x &amp; y position, and the angle.
     * For level3, the trace will show
     * <ul>
     *   <li> u - the path parameter
     *   <li> v - the path velocity
     *   <li> a - the path acceleration
     *   <li> angularV - the path angular velocity
     *   <li> angularA - the path angular acceleration
     * </ul> the path parameter, velocity, acceleration,
     * This is provided to allow the caller to choose appropriate
     * values for a given application.
     * @param level1 the level for non-update messages
     * @param level2 the level for update messages
     * @param level3 the level for additional update messages
     * @param level4 the level for calls to the PlacedAnimationObject2D
     *        method setPosition not shown by update messages
     * @exception IllegalArgumentException an argument was illegal
     */
    public static void setTraceLevels(int level1, int level2,
				      int level3, int level4)
	throws IllegalArgumentException
    {
	if (level1 < -1 || level2 < -1 || level3 < -1 || level4 < -1) {
	    throw new IllegalArgumentException(errorMsg("badTraceLevel"));
		/*("trace level was less than -1");*/
	}

	Animation2D.level1 = level1;
	Animation2D.level2 = level2;
	Animation2D.level3 = level3;
	Animation2D.level4 = level4;
    }

    /**
     * Set the levels for org.bzdev.anim2d trace messages given
     * enumeration constants.
     * The default levels are -1, indicating that no messages will be
     * printed, corresponding to a null argument for this method. Otherwise
     * the level is the ordinal value of the enumeration types.
     * For level1, the arguments to various methods will be shown.
     * For level2 or level4, the trace will show
     * <ul>
     *   <li> x - the X position
     *   <li> y - the Y position
     *   <li> angle - the angle in radians (absolute).
     * </ul>
     * the x &amp; y position, and the angle.
     * For level3, the trace will show
     * <ul>
     *   <li> u - the path parameter
     *   <li> v - the path velocity
     *   <li> a - the path acceleration
     *   <li> angularV - the path angular velocity
     *   <li> angularA - the path angular acceleration
     * </ul> the path parameter, velocity, acceleration,
     * This is provided to allow the caller to choose appropriate
     * values for a given application.
     * @param <T> the type of an enumeration naming trace levels
     * @param level1 the level for non-update messages; null if level 1
     *        messages should not be displayed
     * @param level2 the level for update messages; null if level 2
     *        messages should not be displayed
     * @param level3 the level for additional update messages; null if level 3
     *        messages should not be displayed
     * @param level4 the level for calls to the PlacedAnimationObject2D
     *        method setPosition; null if level 4
     *        messages should not be displayed
     */
    public static
	<T extends Enum<T>> void setTraceLevels(T level1, T level2,
						T level3, T level4)
    {
	Animation2D.level1 = (level1 == null)? -1: level1.ordinal();
	Animation2D.level2 = (level2 == null)? -1: level2.ordinal();
	Animation2D.level3 = (level3 == null)? -1: level3.ordinal();
	Animation2D.level4 = (level4 == null)? -1: level4.ordinal();
    }
}

//  LocalWords:  exbundle currentTicks currentTime initFrames nframes
//  LocalWords:  ImageSequenceWriter subclasses AnimationObject da xL
//  LocalWords:  scheduleFrames configureImageType ImageType accessor
//  LocalWords:  SimFunction getFunction subsequence frameTickOrder
//  LocalWords:  startingFrameTick IllegalStateException maxFrames yL
//  LocalWords:  DirectoryAccessor maxFrameError filenamePrefix isw
//  LocalWords:  imageType nullISW negMaxFrames tooEarly noMIMEType
//  LocalWords:  noFormatName mimeType img fnamePrefix getGraph xU yU
//  LocalWords:  xLower xUpper yLower yUpper xgcs ygcs xf yf blp ul
//  LocalWords:  scaleFactorX scaleFactorY IllegalArgumentException
//  LocalWords:  outOfRange  ticksPerSecond ticksPerFrame ISWriterOps
//  LocalWords:  negTicksPerFrame negWidth negHeight li angularV png
//  LocalWords:  angularA PlacedAnimationObject setPosition boolean
//  LocalWords:  badTraceLevel setImageType requestAlpha currentISW
//  LocalWords:  nextOSGFailed graphAlreadyCreated filenameTemplate
//  LocalWords:  AnimationLayer DFactory EPTS setRanges setOffsets
//  LocalWords:  noSetRanges
