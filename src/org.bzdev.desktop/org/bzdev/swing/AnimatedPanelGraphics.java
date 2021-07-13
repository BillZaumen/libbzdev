package org.bzdev.swing;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ISWriterOps.AnimationParameters;
import org.bzdev.gio.OSGraphicsOps;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.image.ColorModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.net.URL;

//@exbundle org.bzdev.swing.lpack.Swing


/**
 * Animation-panel class implementing the ISWriterOps interface.
 * While instances of this class can be created directly, it is
 * usually more convenient to use the method
 * {@link #newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,boolean,AnimatedPanelGraphics.Mode)},
 * {@link #newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,AnimatedPanelGraphics.ExitAccessor,AnimatedPanelGraphics.Mode)},
 * {@link #newFramedInstance(int,int,double,String,boolean,boolean,AnimatedPanelGraphics.Mode)}.
 * or
 * {@link #newFramedInstance(int,int,double,String,boolean,AnimatedPanelGraphics.ExitAccessor,AnimatedPanelGraphics.Mode)}.
 * The interface {@link org.bzdev.gio.ISWriterOps.AnimationParameters} is
 * implemented by the class {@link org.bzdev.anim2d.Animation2D}.
 * <P>
 * Once an instance is created directly, one must call
 * {@link AnimatedPanelGraphics#addMetadata(int,int,double)} or
 * {@link AnimatedPanelGraphics#addMetadata(int,int,String,double,String)},
 * which instances of {@link org.bzdev.anim2d.Animation2D} will do when the
 * user calls its
 * {@link org.bzdev.anim2d.Animation2D#initFrames(int,ISWriterOps)}
 * method (the preferred initFrames method for this case), and which the
 * <CODE>newFramedInstance</CODE> methods of
 * <CODE>AnimatedPanelGraphics</CODE> will also do.
 * <P>
 * To create an image to display, one will call
 * {@link #nextOutputStreamGraphics()} or
 * {@link #nextOutputStreamGraphics(int)} to create an output-stream-graphics
 * object. This object's {@link OSGraphicsOps#createGraphics()}
 * method will then be called to create a graphics context.  That graphics
 * context will be used for drawing the image.  once done, the
 * output-stream-graphics method {@link  OSGraphicsOps#imageComplete()}
 * must be called before creating the next image.  This sequence of operations
 * is handled automatically by the {@link org.bzdev.anim2d.Animation2D} class.
 * <P>
 * Once all the images are created, the method
 * {@link #close()} must be called. This method indicates that no more
 * images will be generated,  and any progress bar displayed as a result
 * of calling newFramedInstance will be removed.
 * <P>
 * As an example, one can create an animation and display it immediately
 * in a window as follows:
 * <BLOCKQUOTE><CODE><PRE>
 *     Animation2D a2d = new Animation2D(400, 250, 1000.0, 40);
 *     AnimatedPanelGraphics apg =
 *      AnimatedPanelGraphics.newFramedInstance
 *          (animation, "Example", true, true, null);
 *     // create and configure the animation objects
 *     ...
 *     // get number of frames for an animation running for 30 seconds
 *     int maxframes = a2d.estimateFrameCount(30.0);
 *     a2d.initFrames(maxframes, apg);
 *     a2d.scheduleFrames(0, maxframes);
 *     a2d.run();
 *     apg.close();
 * </PRE></CODE></BLOCKQUOTE>
 * <P>
 * While the AnimatedPanelGraphics class has Java 'swing' components
 * associated with it, the constructors and the newFramedInstance methods
 * are thread safe - their use is not restricted to the event dispatch
 * thread. Consequently, it is easy to create a command-line program that
 * will open a window to display its output and exit when that window is
 * closed.
 */
public class AnimatedPanelGraphics implements ISWriterOps {

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    boolean requestAlpha = true;
    PanelGraphics pg = null;
    double frameRate = -1.0;
    int frameIATime = -1;

    static ImageIcon playIcon;
    static ImageIcon pauseIcon;
    static ImageIcon adjustLeftIcon;
    static ImageIcon adjustRightIcon;
    static ImageIcon snapshotIcon;

    static {
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			URL playURL = AnimatedPanelGraphics.class
			    .getResource("/org/bzdev/swing/icons/play.png");
			URL pauseURL = AnimatedPanelGraphics.class
			    .getResource("/org/bzdev/swing/icons/pause.png");
			URL adjustLeftURL = AnimatedPanelGraphics.class
			    .getResource("/org/bzdev/swing/icons/aleft.png");
			URL adjustRightURL = AnimatedPanelGraphics.class
			    .getResource("/org/bzdev/swing/icons/aright.png");
			URL cameraURL = AnimatedPanelGraphics.class
			    .getResource("/org/bzdev/swing/icons/camera.png");
			/*
			URL playURL = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/play.png");
			URL pauseURL = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/pause.png");
			URL adjustLeftURL = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/aleft.png");
			URL adjustRightURL = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/aright.png");
			URL cameraURL = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/camera.png");
			*/
			playIcon = (playURL == null)? null:
			    new ImageIcon(playURL);
			pauseIcon = (pauseURL == null)? null:
			    new ImageIcon(pauseURL);
			adjustLeftIcon = (adjustLeftURL == null)? null:
			    new ImageIcon(adjustLeftURL);
			adjustRightIcon = (adjustRightURL == null)? null:
			    new ImageIcon(adjustRightURL);
			snapshotIcon = (cameraURL == null)? null:
			    new ImageIcon(cameraURL);

			return (Void)null;
		    }
		});
    }

    static final private String bundleName = "org.bzdev.swing.lpack.APGBundle";
    ResourceBundle bundle = null;
    // Provide default values in case we can't find the resource
    // bundle (which should be in the libbzdev JAR file)
    String playTip = "Play";
    String pauseTip = "Pause";
    String adjustDownTip = "Previous Frame";
    String adjustUpTip  = "Next Frame";
    String snapshotTip = "Snapshot";
    String exitTitle = "Exit Application";
    String exitMsg = "Application will exit";
    String snapshotFormat = "Snapshot %1$d of \"%2$s\"";
    String untitled = "[untitled]";
    /**
     * Set the locale.
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
	// An Animated PanelGraphics instance is not a swing
	// component, so we don't call super.setLocale()
	bundle = ResourceBundle.getBundle(bundleName, locale);
	if (bundle != null) {
	    try {
		String playTip = bundle.getString("playTip");
		String pauseTip = bundle.getString("pauseTip");
		String exitTitle = bundle.getString("exitTitle");
		String exitMsg = bundle.getString("exitMsg");
		String adjustDownTip = bundle.getString("adjustDownTip");
		String adjustUpTip = bundle.getString("adjustUpTip");
		String snapshotTip = bundle.getString("snapshotTip");
		String snapshotFormat = bundle.getString("snapshotFormat");
		String untitled = bundle.getString("untitled");
		if (playTip != null) {
		    this.playTip = playTip;
		}
		if (pauseTip != null) {
		    this.pauseTip = pauseTip;
		}
		if (adjustDownTip != null) {
		    this.adjustDownTip = adjustDownTip;
		}
		if (adjustUpTip != null) {
		    this.adjustUpTip = adjustUpTip;
		}
		if (snapshotTip != null) {
		    this.snapshotTip = snapshotTip;
		}
		if (exitTitle != null) {
		    this.exitTitle = exitTitle;
		}
		if (exitMsg != null) {
		    this.exitMsg = exitMsg;
		}
		if (snapshotFormat != null) {
		    this.snapshotFormat = snapshotFormat;
		}
		if (untitled != null) {
		    this.untitled = untitled;
		}
	    } catch (Exception e) {}
	}
    }

    // Explicitly provide this one so it appears in the javadoc
    // section specific to this class: this is the constructor that
    // will typically be used for animations.
    @Override
    public void addMetadata(int frameWidth, int frameHeight, double frameRate)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, null, frameRate,  null);
    }

    /**
     * {@inheritDoc}
     * The frameMimeType and format arguments are ignored. One
     * should use the method {@link ISWriterOps#addMetadata(int,int,double)}
     * instead for clarity.
     */
    @Override
    public void addMetadata(int frameWidth, int frameHeight,
			    String frameMimeType,
			    double frameRate, String format)
	throws IOException, IllegalStateException
    {
	if (pg == null) {
	    pg = new PanelGraphics(frameWidth, frameHeight, requestAlpha);
	    pg.setFlushImmediately(true);
	}
	if (timer != null) {
	    // If the timer is not null, this method was already
	    // called, as the timer is created here and no place else.
	    if (frameWidth != pg.getWidth()
		|| frameHeight != pg.getHeight()
		|| frameRate != this.frameRate) {
		throw new IllegalArgumentException(errorMsg("metadataChanged"));
	    }
	}
	this.frameRate = frameRate;
	frameIATime = (int)Math.round(1000.0/frameRate);
	timer = new Timer(frameIATime, playListener);
	timer.setRepeats(true);
    }

    long estFrameCount = 0;
    JProgressBar pbar;

    @Override
    public void setEstimatedFrameCount(long count) {
	this.estFrameCount = count;
    }

    /**
     * Get the estimated frame count.
     * @return the estimated frame count
     */
    public long getEstimatedFrameCount() {
	return estFrameCount;
    }

    @Override
    public int getFrameWidth() {return (pg == null)? 0:  pg.getWidth();}

    @Override
    public int getFrameHeight() {return (pg == null)? 0: pg.getHeight();}

    /**
     * Constructor.
     * The additional parameters can be set by using the method
     * {@link ISWriterOps#addMetadata(int,int,double)};
     * The {@link Mode} for this object is
     * {@link AnimatedPanelGraphics.Mode#AUTO_RUN_NO_CONTROLS}.
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     */
    public AnimatedPanelGraphics(boolean requestAlpha) {
	this.requestAlpha = requestAlpha;
    }

    /**
     * Constructor specifying the mode.
     * The additional parameters can be set by using the method
     * {@link ISWriterOps#addMetadata(int,int,double)};
     * The default {@link Mode} for this object is
     * {@link AnimatedPanelGraphics.Mode#AUTO_RUN_NO_CONTROLS}.
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     * @param mode the mode for this object; null for the default
     * @see Mode
     */
    public AnimatedPanelGraphics(boolean requestAlpha, Mode mode) {
	this.requestAlpha = requestAlpha;
	setMode(mode);
    }


    /**
     * Get the JPanel associated with this instance.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @return the panel; null if the panel it not yet available
     * @exception IllegalStateException the panel is not yet available
     */
    public JPanel getPanel() {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	return pg.getPanel();
    }

    /**
     * Set the background color for the panel associated with this object.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @param c the color; null if the color of the panel's parent
     *        should be used
     * @exception IllegalStateException the panel is not yet available
     */
    public void setBackground(Color c) {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	pg.setBackground(c);
    }

    /**
     * Get the background color for the panel associated with this object.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @return the background color
     * @exception IllegalStateException the panel is not yet available
     */
    public Color getBackground() {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	return pg.getBackground();
    }


    /**
     * Find the top-level container for the panel associated with
     * this object and return non-null if the container is a Window.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @return the window; null if there is none
     * @exception IllegalStateException the panel is not yet available
     */
    public Window getPanelWindow() {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	return pg.getPanelWindow();
    }

    /**
     * Set the visibility of the panel associated with this
     * AnimatedPanelGraphics.
     * This method has no effect if the panel is not contained in a window.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @param visible true if the panel should be visible; false otherwise
     * @exception IllegalStateException the panel is not yet available
     */
    public void setVisible(boolean visible) {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	/*
	if (startTimeNeeded) {
	    startTime = System.nanoTime();
	    startTimeNeeded = false;
	}
	*/
	pg.setVisible(visible);
    }

    /**
     * Determine if the window containing the panel associated with
     * this instance of PanelGraphics is visible.
     * The JPanel associated with this instance is not available
     * until {@link #addMetadata(int,int,double)} or
     * {@link #addMetadata(int,int,String,double,String)} is called.
     * @return true if it is visible; false if it is not visible or
     *         if the panel is not contained within a window
     * @exception IllegalStateException the panel is not yet available
     */
    public boolean isVisible() {
	if (pg == null) throw new IllegalStateException(errorMsg("noMetadata"));
	return pg.isVisible();
    }

    LinkedList<PanelGraphics.Creator> queue = new LinkedList<>();
    LinkedList<PanelGraphics.Creator> used = new LinkedList<>();
    PanelGraphics.Creator current = null;
    static final long MILLION = 1000000L;

    /**
     * The mode for an AnimatedPanelGraphics instance.
     * These constants are used by panels created by
     * {@link AnimatedPanelGraphics#AnimatedPanelGraphics(boolean)} or
     * {@link AnimatedPanelGraphics#AnimatedPanelGraphics(boolean,Mode)}
     * and by frames created by
     * {@link AnimatedPanelGraphics#newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,boolean,Mode)}
     * or
     * {@link AnimatedPanelGraphics#newFramedInstance(int,int,double,String,boolean,boolean,Mode)}.
     */
    public static enum Mode {
	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is called
	 * the animation/video will run in the panel associated with an
	 * {@link AnimatedPanelGraphics} instance and cannot be replayed as
	 * there are no controls.
	 *
	 */
	AUTO_RUN_NO_CONTROLS,
	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is called,
	 * the animation/video will run in the panel associated with an
	 * {@link AnimatedPanelGraphics} instance and cannot be replayed but
	 * may be paused.
	 * The controls for a frame should contain a
	 * button that will play or pause the animation or video.
	 */
	AUTO_RUN,
	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is called,
	 * the animation/video will run in the panel associated with an
	 * {@link AnimatedPanelGraphics} instance and can be replayed once
	 * it has run to completion.
	 * The controls for a frame should contain a
	 * button that will play or pause the animation or video.
	 */
	AUTO_RUN_REPLAYABLE,
	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is
	 * called, the animation/video will be ready to run in the
	 * panel associated with an {@link AnimatedPanelGraphics}
	 * instance and cannot be replayed, but can be paused at any
	 * point. The controls for a frame should contain a
	 * button that will play or pause the animation or video. This
	 * button will have to be pushed to start the video or animation.
	 */
	START_PAUSED,

	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is
	 * called, the animation/video will be ready to run in the
	 * panel associated with an {@link AnimatedPanelGraphics}
	 * instance and can be replayed after it runs to completion
	 * and paused at any point. The controls for a frame should contain a
	 * button that will play or pause the animation or video.
	 */
	START_PAUSED_REPLAYABLE,
	/**
	 * Once the method {@link AnimatedPanelGraphics#close()} is called,
	 * the animation/video will be ready to run in the panel associated
	 * with an {@link AnimatedPanelGraphics} instance and can be replayed
	 * after it runs to completion, paused at any point, and repositioned
	 * when paused. The controls for a frame should
	 * contain a button that will play or pause the animation or
	 * video. Additional controls should allow the animation/video to
	 * be repositioned in time and single stepped in either direction.
	 * Finally, there should be a control that provides a snapshot of
	 * a frame.
	 */
	START_PAUSED_SELECTABLE,
    }

    Mode mode = Mode.AUTO_RUN_NO_CONTROLS;

    JButton button = null;
    JButton adjustLeftButton = null;
    JSlider slider = null;
    JButton adjustRightButton = null;
    JButton snapshotButton = null;

    boolean keep = false;

    /**
     * Set the current mode.
     * @param mode the mode
     * @see Mode
     */
    private void setMode(Mode mode) {
	this.mode = mode;
	switch(mode) {
	case AUTO_RUN_REPLAYABLE:
	case START_PAUSED_REPLAYABLE:
	case START_PAUSED_SELECTABLE:
	    keep = true;
	    break;
	default:
	    keep = false;
	}
    }

    boolean needSwap = false;	// need to sqp used and queue to replay.
    private void swapIfNeeded() {
	if (needSwap) {
	    LinkedList<PanelGraphics.Creator> tmp = used;
	    used = queue;
	    queue = tmp;
	    finished = false;
	    needSwap = false;
	}
    }

    int currentRepCount = 0;

    ActionListener playListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		swapIfNeeded();
		if (currentRepCount > 0) {
		    currentRepCount--;
		    return;
		}
		synchronized(queue) {
		    if (queue.isEmpty() && currentRepCount == 0) {
			timer.stop();
			finished = true;
			switch(mode) {
			case AUTO_RUN:
			case START_PAUSED:
			    button.setIcon(playIcon);
			    button.setToolTipText(playTip);
			    button.setEnabled(false);
			    break;
			case AUTO_RUN_REPLAYABLE:
			case START_PAUSED_REPLAYABLE:
			case START_PAUSED_SELECTABLE:
			    button.setIcon(playIcon);
			    button.setToolTipText(playTip);
			    needSwap = true;
			    /*
			    LinkedList<PanelGraphics.Creator> tmp = used;
			    used = queue;
			    queue = tmp;
			    finished = false;
			    */
			    if (mode == Mode.START_PAUSED_SELECTABLE) {
				if (snapshotButton != null) {
				    snapshotButton.setEnabled(true);
				}
				if (adjustLeftButton != null) {
				    adjustLeftButton.setEnabled(true);
				}
				if (adjustRightButton != null) {
				    adjustRightButton.setEnabled(true);
				}
				if (slider != null) {
				    slider.setEnabled(true);

				}
			    }
			    break;
			}
			return;
		    }
		    current = queue.poll();
		    currentRepCount = current.getRepetitionCount();
		}
		if (currentRepCount > 0) {
		    current.apply();
		    currentRepCount--;
		}
		if (keep) used.add(current);
		if (slider != null) {
		    int val = (int) (((long)used.size())*LIMIT / frameCount);
		    if (queue.size() == 0) {
			val = LIMIT;
		    }
		    final int ourval = val;
		    SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				slider.setValue(ourval);
			    }
			});
		}
	    };
	};

    Timer timer = null;
    int frameCount = 0;

    boolean fullyLoaded = false;

    /**
     * Test if the video/animation has been loaded.
     * A video/animation is considered to be loaded
     * when the method {@link #close()} has been called.
     * @return true if the video/animation is loaded; false otherwise
     */
    public boolean isLoaded() {
	synchronized(queue) {
	    return fullyLoaded;
	}
    }

    boolean finished = false;
    Thread playThread = null;

    /**
     * Start the video/animation.
     * If loading is not yet complete, this method
     * has no effect.
     */
    public void play() {
	synchronized(queue) {
	    switch(mode) {
	    case START_PAUSED_SELECTABLE:
		if (mode == Mode.START_PAUSED_SELECTABLE) {
		    if (snapshotButton != null) {
			snapshotButton.setEnabled(false);
		    }
		    if (adjustLeftButton != null) {
			adjustLeftButton.setEnabled(false);
		    }
		    if (adjustRightButton != null) {
			adjustRightButton.setEnabled(false);
		    }
		    if (slider != null) {
			slider.setEnabled(false);
		    }
		}
	    case AUTO_RUN_NO_CONTROLS:
	    case AUTO_RUN:
	    case AUTO_RUN_REPLAYABLE:
	    case START_PAUSED_REPLAYABLE:
	    case START_PAUSED:
		if (fullyLoaded && !timer.isRunning()) timer.start();
		break;
	    }
	}
    }

    /**
     * Stop the video/animation.
     */
    public void stop() {
	synchronized(queue) {
	    if (timer.isRunning()) {
		timer.stop();
	    }
	}
    }

    /**
     * Move the current frame by a specified increment from
     * its current location in the video/animation's time sequence.
     * If the frame sequence is not fully loaded, this method
     * returns without performing any action.
     * @param incr the number of frames by which to shift the current
     *        frame
     */
    public void adjust(int incr) {
	if (incr == 0) return;
	boolean running  = false;
	PanelGraphics.Creator c = null;
	synchronized(queue) {
	    if (!fullyLoaded) return;
	    running = timer.isRunning();
	    if (running) {
		timer.stop();
	    }
	    if (incr > 0) {
		if (queue.isEmpty()) incr = 0;
		while (incr > 0) {
		    c = queue.poll();
		    if (--incr == 0 || queue.isEmpty()) {
			incr = 0;
			current = c;
		    }
		    if (keep) used.add(c);
		}
	    } else {
		int decr = - incr;
		if (!keep) return;
		if (used.isEmpty()) decr = 0;
		while (decr > 0) {
		    c = used.pollLast();
		    if (--decr == 0 || used.isEmpty()) {
			decr = 0;
			current = c;
		    }
		    queue.addFirst(c);
		}
	    }
	    if (c != null) {
		current = c;
		current.apply();
	    }
	    if (running) {
		timer.start();
	    }
	}
    }

    long startTime = System.nanoTime();
    boolean startTimeNeeded = true;
    boolean pbarTestNeeded = true;
    /**
     * {@inheritDoc}
     * The name field is ignored by this class.
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics(String name)
	throws IllegalStateException
    {
	return nextOutputStreamGraphics();
    }

    @Override
    public OSGraphicsOps nextOutputStreamGraphics()
	throws IllegalStateException
    {
	if (startTimeNeeded) {
	    if (pbar == null) {
		pbarTestNeeded = false;
	    } else {
		startTime = System.nanoTime();
	    }
	    startTimeNeeded = false;
	}
	return new OSGraphicsOps() {
	    PanelGraphics.Creator pgc = pg.newPanelGraphicsCreator(keep);

	    @Override
	    public boolean requestsAlpha() {
		return pg.requestsAlpha();
	    }

	    @Override
	    public int getWidth() {
		return pg.getWidth();
	    }

	    @Override
	    public int getHeight() {
		return pg.getHeight();
	    }

	    @Override
	    public ColorModel getColorModel() {
		return pg.getColorModel();
	    }

	    @Override
	    public Graphics2D createGraphics()
		throws UnsupportedOperationException
	    {
		return pgc.createGraphics();
	    }

	    @Override
	    public void flush() throws IOException {
	    }

	    @Override
	    public boolean canReset() {return false;}

	    @Override
	    public void reset() throws UnsupportedOperationException
	    {
		throw new
		    UnsupportedOperationException
		    (errorMsg("notImplemented", "reset"));
	    }

	    @Override
	    public void close() {
	    }

	    @Override
	    public void imageComplete() throws IOException {
		synchronized(queue) {
		    queue.add(pgc);
		    frameCount++;
		    if (pbarTestNeeded && estFrameCount > 0) {
			long ctime = System.nanoTime();
			boolean test = (ctime - startTime) > 1000000000L;
			if (test) {
			    if ((frameCount*100L)/estFrameCount <= 25) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    Container parent = pbar.getParent();
					    if (parent != null) {
						parent.setVisible(true);
					    }
					}
				    });
			    }
			    pbarTestNeeded = false;
			}
		    }
		    queue.notifyAll();
		    if (pbar != null && estFrameCount > 0) {
			long value = (frameCount*100L)/estFrameCount;
			if (value > 100) value = 100;
			if (value < 0) value = 0;
			final int ivalue = (int) value;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    pbar.setValue(ivalue);
				}
			    });
		    }
		}
	    }
	};
    }


    /**
     * {@inheritDoc}
     * The name field is ignored by this class.
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics(String name, int count)
	throws IllegalStateException
    {
	return nextOutputStreamGraphics(count);
    }

    @Override
    public OSGraphicsOps nextOutputStreamGraphics(final int count)
	throws IllegalStateException, IllegalArgumentException
    {
	if (startTimeNeeded) {
	    if (pbar == null) {
		pbarTestNeeded = false;
	    } else {
		startTime = System.nanoTime();
	    }
	    startTimeNeeded = false;
	}
	return new OSGraphicsOps() {
	    PanelGraphics.Creator pgc =
		pg.newPanelGraphicsCreator(false, count);
	    @Override
	    public boolean requestsAlpha() {
		return pg.requestsAlpha();
	    }

	    @Override
	    public int getWidth() {
		return pg.getWidth();
	    }

	    @Override
	    public int getHeight() {
		return pg.getHeight();
	    }

	    @Override
	    public ColorModel getColorModel() {
		return pg.getColorModel();
	    }

	    @Override
	    public Graphics2D createGraphics()
		throws UnsupportedOperationException
	    {
		/*
		if (pbar == null) {
		    startTimeNeeded = false;
		} else if (startTimeNeeded) {
		    startTime = System.nanoTime();
		    startTimeNeeded = false;
		}
		*/
		return pgc.createGraphics();
	    }

	    @Override
	    public void flush() throws IOException {
	    }

	    @Override
	    public boolean canReset() {return false;}

	    @Override
	    public void reset() throws UnsupportedOperationException
	    {
		throw new UnsupportedOperationException
		    (errorMsg("notImplemented", "reset"));
	    }

	    @Override
	    public void close() {
	    }

	    @Override
	    public void imageComplete() throws IOException {
		synchronized(queue) {
		    queue.add(pgc);
		    frameCount += count;
		    if (pbarTestNeeded && estFrameCount > 0) {
			long ctime = System.nanoTime();
			boolean test = (ctime - startTime) > 1000000000L;
			if (test) {
			    if ((frameCount*100L)/estFrameCount <= 25) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    Container parent = pbar.getParent();
					    if (parent != null) {
						parent.setVisible(true);
					    }
					}
				    });
			    }
			    pbarTestNeeded = false;
			}
		    }
		    if (pbar != null && estFrameCount > 0) {
			long value = (frameCount*100L)/estFrameCount;
			if (value > 100) value = 100;
			if (value < 0) value = 0;
			final int ivalue = (int) value;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    pbar.setValue(ivalue);
				}
			    });
		    }
		}
	    }
	};
    }

    Component origGlassPane = null;


    /**
     * {@inheritDoc}
     * For the case of {@link AnimatedPanelGraphics}, calling this
     * method indicates that all the frames have been supplied. If
     * there is a progress bar, its parent container will be made
     * invisible and the original glass pane will be restored. The
     * first image in the sequence will be displayed.
     */
    @Override
    public void close() {
	boolean fl = false;
	synchronized(queue) {
	    fullyLoaded = true;
	    if (pbar != null) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    Container parent = pbar.getParent();
			    // Test should not be needed but we
			    // don't want to throw an exception if bpar
			    // is not null but has no parent.
			    if (parent != null) {
				parent.setVisible(false);
				Window w =
				    SwingUtilities.windowForComponent(pbar);
				// restore any initial glass pane.
				if (w != null && w instanceof JFrame) {
				    JFrame jf = (JFrame) w;
				    jf.setGlassPane(origGlassPane);
				}
			    }
			}
		    });
	    }
	    if (!queue.isEmpty()) {
		current = queue.peek();
		current.apply();
		queue.notifyAll();
	    } else {
		// nothing more to do as nothing was loaded.
		return;
	    }
	}
	switch (mode) {
	case START_PAUSED:
	case START_PAUSED_REPLAYABLE:
	case START_PAUSED_SELECTABLE:
	    if (SwingUtilities.isEventDispatchThread()) {
		if (button != null) {
		    button.setEnabled(true);
		    button.setIcon(playIcon);
		    button.setToolTipText(playTip);
		}
		if (mode == Mode.START_PAUSED_SELECTABLE) {
		    if (snapshotButton != null) {
			snapshotButton.setEnabled(true);
		    }
		    if (adjustLeftButton != null) {
			adjustLeftButton.setEnabled(true);
		    }
		    if (adjustRightButton != null) {
			adjustRightButton.setEnabled(true);
		    }
		    if (slider != null) {
			slider.setEnabled(true);
		    }
		}
	    } else {
		try {
		    SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
				if (button != null) {
				    button.setEnabled(true);
				    button.setIcon(playIcon);
				    button.setToolTipText(playTip);
				}
				if (mode == Mode.START_PAUSED_SELECTABLE) {
				    if (snapshotButton != null) {
					snapshotButton.setEnabled(true);
				    }
				    if (adjustLeftButton != null) {
					adjustLeftButton.setEnabled(true);
				    }
				    if (adjustRightButton != null) {
					adjustRightButton.setEnabled(true);
				    }
				    if (slider != null) {
					slider.setEnabled(true);
				    }
				}
			    }
			});
		} catch(Exception e) {}
	    }
	    break;
	case AUTO_RUN_REPLAYABLE:
	case AUTO_RUN:
	    if (SwingUtilities.isEventDispatchThread()) {
		if (button != null) {
		    button.setEnabled(true);
		    button.setIcon(pauseIcon);
		    button.setToolTipText(pauseTip);
		}
	    } else {
		try {
		    SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
				if (button != null) {
				    button.setEnabled(true);
				    button.setIcon(pauseIcon);
				    button.setToolTipText(pauseTip);
				}
			    }
			});
		} catch(Exception e) {}
	    }
	    // fall through
	case AUTO_RUN_NO_CONTROLS:
	    play();
	    break;
	}
    }

    void addButtonListener() {
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    synchronized(queue) {
			if (timer.isRunning()) {
			    timer.stop();
			    button.setIcon(playIcon);
			    button.setToolTipText(playTip);
			    if (snapshotButton != null) {
				snapshotButton.setEnabled(true);
			    }
			    if (adjustLeftButton != null) {
				adjustLeftButton.setEnabled(true);
			    }
			    if (adjustRightButton != null) {
				adjustRightButton.setEnabled(true);
			    }
			    if (slider != null) {
				slider.setEnabled(true);
			    }
			} else {
			    swapIfNeeded();
			    if (!finished) {
				timer.start();
				button.setIcon(pauseIcon);
				button.setToolTipText(pauseTip);
				if (snapshotButton != null) {
				    snapshotButton.setEnabled(false);
				}
				if (adjustLeftButton != null) {
				    adjustLeftButton.setEnabled(false);
				}
				if (adjustRightButton != null) {
				    adjustRightButton.setEnabled(false);
				}
				if (slider != null) {
				    slider.setEnabled(false);
				}
			    }
			}
		    }
		}
	    });
    }

    static final int LIMIT = 100;
    long snapshotCounter = 0;

    void addSnapshotListener() {
	snapshotButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (current == null) return;
		    Window w = getPanelWindow();
		    Frame f = (w != null && w instanceof Frame)? (Frame)w: null;
		    String title = (f == null)? untitled: f.getTitle();
		    title.trim();
		    if (title.length() == 0) title = untitled;
		    String snapshotTitle =
			String.format(snapshotFormat, snapshotCounter++, title);
		    PanelGraphics snapshot =
			PanelGraphics.newFramedInstance(getFrameWidth(),
							getFrameHeight(),
							snapshotTitle,
							false, false, true);
		    try {
			pg.write(snapshot);
			snapshot.setBackground(getBackground());
			snapshot.setVisible(true);
		    } catch (Exception ex) {
			SwingErrorMessage.display(pg.getPanelWindow(),
						  null,
						  ex.getMessage());
		    }
		}
	    });
    }

    void addAdjustButtonListener() {
	adjustLeftButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    synchronized(queue) {
			if (timer.isRunning()) {
			    return;
			} else {
			    needSwap = false;
			    finished = false;
			    adjust(-1);
			    if (slider != null) {
				int val = (int)
				    (((long)used.size())*LIMIT / frameCount);
				if (queue.size() == 0) {
				    val = LIMIT;
				}
				final int ourval = val;
				slider.setValue(ourval);
			    }
			}
		    }
		}
	    });
	slider.addChangeListener(new ChangeListener() {
		int last = 0;
		boolean needLast = true;

		private void adjustTo(int val) {
		    long lval = val;
		    lval *= frameCount;
		    lval /= LIMIT;
		    int ival = (int) lval;
		    ival -= used.size();
		    if (ival > queue.size()) {
			ival = queue.size();
		    }
		    if (-ival > used.size()) {
			ival = - used.size();
		    }
		    adjust(ival);
		}

		public void stateChanged(ChangeEvent e) {
		    if (slider.isEnabled()) {
			if (!slider.getValueIsAdjusting()) {
			    needSwap = false;
			    finished = false;
			    currentRepCount = 0;
			    int val = slider.getValue();
			    adjustTo(val);
			    needLast = true;
			} else {
			    int val = slider.getValue();
			    if (needLast) {
				last = val;
				needLast = false;
			    } else {
				adjustTo(val);
			    }
			}
		    }
		}
	    });

	adjustRightButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    synchronized(queue) {
			if (timer.isRunning()) {
			    return;
			} else {
			    needSwap = false;
			    finished = false;
			    adjust(1);
			    if (slider != null) {
				int val = (int)
				    (((long)used.size())*LIMIT / frameCount);
				if (queue.size() == 0) {
				    val = LIMIT;
				}
				final int ourval = val;
				slider.setValue(ourval);
			    }
			}
		    }
		}
	    });
    }

    static final Color controlPanelColor =
	(new Color(255-20,255-20,255-10)).darker();

    /**
     * Class to determine how an application can exit when a
     * security manager is installed and a window created with
     * {@link AnimatedPanelGraphics#newFramedInstance(int,int,double,String,boolean,ExitAccessor,Mode)}
     * or
     * {@link AnimatedPanelGraphics#newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,ExitAccessor,Mode)}
     * is closed.  When an instance of this class is used in such a
     * newFramedInstance method, the application will exit if the
     * console's frame is closed and an instance of this class was
     * created when there is no security manager installed.  If a
     * security manager was installed when an instance of this class
     * was created, closing the frame will result in a dialog box
     * asking if the application should exit unless the permission
     * {@link ExitPermission} was granted with a target of
     * "org.bzdev.swing.AnimatedPanelGraphics" (in which case the
     * application will immediately exit).
     * @see ExitPermission
     */
    public static class ExitAccessor {
	private boolean allow;

	/**
	 * Constructor.
	 */
	public ExitAccessor() {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm == null) {
		allow = true;
	    } else {
		try {
		    sm.checkPermission
			(new ExitPermission
			 ("org.bzdev.swing.AnimatedPanelGraphics"));
		    allow = true;
		} catch (SecurityException se) {
		    allow = false;
		}
	    }
	}
	/**
	 * Return true if closing a frame created with
	 * {@link AnimatedPanelGraphics#newFramedInstance(int,int,double,String,boolean,ExitAccessor,Mode)}
	 * or
	 * {@link AnimatedPanelGraphics#newFramedInstance(ISWriterOps.AnimationParameters,String,boolean,ExitAccessor,Mode)}
}	 * can cause an application to exit; false if the user must confirm
	 * that the application will exit.
	 */
	boolean allow() {return allow;}
    }



    /**
     * Create an instance of AnimatedPanelGraphics whose JPanel
     * is placed within a JFrame given a set of animation parameters.
     * <P>
     * Note: the first argument can be an instance of the class
     * {@link org.bzdev.anim2d.Animation2D}, in which case the frames
     * created by an animation will be displayed in sequence at the
     * specified frame rate (the frame rate determines the time between
     * frames with a granularity of 1 millisecond - a value set by
     * the JRE/JDK).
     * <P>
     * The mode argument determines if controls are shown as part of
     * the frame and if the animation will run automatically or must be
     * manually started.
     * @param ap the animation parameters
     * @param title the frame's title
     * @param visible true if the frame should initially be visible
     * @param exitOnClose true if the application should exit when the
     *        frame is closed
     * @param mode the mode for the AnimatedPanelGraphics that will be
     *        created; null for a default
     *        ({@link AnimatedPanelGraphics.Mode#START_PAUSED_SELECTABLE})
     * @see Mode
     * @see org.bzdev.gio.ISWriterOps.AnimationParameters
     * @see org.bzdev.anim2d.Animation2D
     */
    public static AnimatedPanelGraphics
	newFramedInstance(ISWriterOps.AnimationParameters ap,
			  String title, boolean visible, boolean exitOnClose,
			  Mode mode)
	throws IOException
    {
	double frameRate = ap.getFrameRate();
	return newFramedInstance(ap.getWidthAsInt(), ap.getHeightAsInt(),
				 frameRate,
				 title, visible, exitOnClose, null, mode);
    }

    /**
     * Create an instance of AnimatedPanelGraphics whose JPanel
     * is placed within a JFrame given a set of animation parameters with
     * the frame's exit mode determined by an exit accessor.
     * <P>
     * Note: the first argument can be an instance of the class
     * {@link org.bzdev.anim2d.Animation2D}, in which case the frames
     * created by an animation will be displayed in sequence at the
     * specified frame rate (the frame rate determines the time between
     * frames with a granularity of 1 millisecond - a value set by
     * the JRE/JDK).
     * <P>
     * The mode argument determines if controls are shown as part of
     * the frame and if the animation will run automatically or must be
     * manually started.
     * @param ap the animation parameters
     * @param title the frame's title
     * @param visible true if the frame should initially be visible
     * @param accessor an exit accessor that determines if the frame will
     *        ask the user if the application should exit before exiting
     *        the application; null implies that the application will exit
     *        if either no security manager was installed or the permission
     *        {@link ExitPermission} was granted for this class
     * @param mode the mode for the AnimatedPanelGraphics that will be
     *        created; null for a default
     *        ({@link AnimatedPanelGraphics.Mode#START_PAUSED_SELECTABLE})
     * @see Mode
     * @see org.bzdev.gio.ISWriterOps.AnimationParameters
     * @see org.bzdev.anim2d.Animation2D
     */
    public static AnimatedPanelGraphics
	newFramedInstance(ISWriterOps.AnimationParameters ap,
			  String title, boolean visible,
			  ExitAccessor accessor,
			  Mode mode)
	throws IOException
    {
	double frameRate = ap.getFrameRate();
	return newFramedInstance(ap.getWidthAsInt(), ap.getHeightAsInt(),
				 frameRate,
				 title, visible, true, accessor, mode);
    }

    /**
     * Create an instance of AnimatedPanelGraphics whose JPanel
     * is placed within a JFrame.
     * <P>
     * The mode argument determines if controls are shown as part of
     * the frame and if the animation will run automatically or must be
     * manually started.
     * @param width the target frame width in user-space units
     * @param height the target frame height in user-space units
     * @param frameRate the frame rate in units of frames per second
     * @param title the frame's title
     * @param visible true if the frame should initially be visible
     * @param exitOnClose true if the application should exit when the
     *        frame is closed
     * @param mode the mode for the AnimatedPanelGraphics that will be
     *        created; null for a default
     *        ({@link AnimatedPanelGraphics.Mode#START_PAUSED_SELECTABLE})
     * @see Mode
     * @see org.bzdev.gio.ISWriterOps.AnimationParameters
     * @see org.bzdev.anim2d.Animation2D
     */
    public static AnimatedPanelGraphics
	newFramedInstance(final int width,
			  final int height,
			  double frameRate,
			  final String title,
			  final boolean visible,
			  final boolean exitOnClose,
			  Mode mode)
	throws IOException
    {
	return newFramedInstance(width, height, frameRate, title,
				 visible, exitOnClose, null, mode);
    }


    /**
     * Create an instance of AnimatedPanelGraphics whose JPanel
     * is placed within a JFrame with the frame's exit mode determined
     * by an exit accessor.
     * <P>
     * The mode argument determines if controls are shown as part of
     * the frame and if the animation will run automatically or must be
     * manually started.
     * @param width the target frame width in user-space units
     * @param height the target frame height in user-space units
     * @param frameRate the frame rate in units of frames per second
     * @param title the frame's title
     * @param visible true if the frame should initially be visible
     * @param accessor an exit accessor that determines if the frame will
     *        ask the user if the application should exit before exiting
     *        the application; null implies that the application will exit
     *        if either no security manager was installed or the permission
     *        {@link ExitPermission} was granted for this class
     * @param mode the mode for the AnimatedPanelGraphics that will be
     *        created; null for a default
     *        ({@link AnimatedPanelGraphics.Mode#START_PAUSED_SELECTABLE})
     * @see Mode
     * @see org.bzdev.gio.ISWriterOps.AnimationParameters
     * @see org.bzdev.anim2d.Animation2D
     */
    public static AnimatedPanelGraphics
	newFramedInstance(final int width,
			  final int height,
			  double frameRate,
			  final String title,
			  final boolean visible,
			  final ExitAccessor accessor,
			  Mode mode)
	throws IOException
    {
	return newFramedInstance(width, height, frameRate, title,
				 visible, true, accessor, mode);
    }

    private static AnimatedPanelGraphics
	newFramedInstance(final int width,
			  final int height,
			  double frameRate,
			  final String title,
			  final boolean visible,
			  final boolean exitOnClose,
			  ExitAccessor accessor,
			  Mode mode)
	throws IOException
    {
	if (mode == null) mode = Mode.START_PAUSED_SELECTABLE;
	final AnimatedPanelGraphics apg =
	    new AnimatedPanelGraphics(true, mode);
	final Mode ourMode = mode;
	apg.addMetadata(width,height, frameRate);
	final JPanel panel = apg.getPanel();
	boolean ask = false;
	if (exitOnClose == true) {
	    if (accessor == null) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    try {
			sm.checkPermission
			    (new ExitPermission
			     ("org.bzdev.swing."
			      + "AnimatedPanelGraphics"));
		    } catch (SecurityException se) {
			// System.out.println("setting Ask to true");
			ask = true;
		    }
		} else {
		    ask = false;
		}
	    } else {
		ask = !accessor.allow();
	    }
	}
	// System.out.println("ask = " + ask);
	final boolean askOnClose = ask;
	Runnable r = new Runnable() {
		JFrame frame = null;
		public void run() {
		    frame = (title == null)? new JFrame():
			new JFrame(title);
		    AccessController.doPrivileged
			(new PrivilegedAction<Void>() {
				public Void run() {
				    doit();
				    return (Void)null;
				}
			    });
		}
		private void doit () {
		    int closeOperation = exitOnClose?
			WindowConstants.EXIT_ON_CLOSE:
			WindowConstants.HIDE_ON_CLOSE;
		    if (exitOnClose == false) {
			frame.setDefaultCloseOperation
			    (WindowConstants.HIDE_ON_CLOSE);
		    } else if (askOnClose /*System.getSecurityManager() != null
				   && exitOnClose*/) {
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
				    String title = apg.exitTitle;
				    String msg = apg.exitMsg;
				    int option =
					JOptionPane.OK_CANCEL_OPTION;
				    int ok = JOptionPane.showConfirmDialog
					(frame, msg, title, option);
				    if (ok == JOptionPane.OK_OPTION) {
					AccessController.doPrivileged
					    (new
					     PrivilegedAction<Void>() {
						    public Void run() {
							System.exit(0);
							return null;
						    }
						});
				    } else {
					// in case Swing tries to make
					// the window invisible.
					SwingUtilities.invokeLater
					    (new Runnable() {
						    public void run() {
							frame.setVisible
							    (true);
						    }
						});
				    }
				}
			    });
		    } else {
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
				    System.exit(0);
				}
			    });
		    }
		    panel.setOpaque(true);
		    Dimension psize = new Dimension(width,height);
		    panel.setPreferredSize(psize);
		    Container fpane = frame.getContentPane();
		    fpane.setLayout(new BorderLayout());
		    apg.origGlassPane = frame.getGlassPane();
		    JPanel pbpanel = new JPanel(new GridBagLayout());
		    pbpanel.setOpaque(false);
		    apg.pbar = new JProgressBar(0, 100);
		    Dimension pbsize = new Dimension(width/2,10);
		    GridBagConstraints c = new GridBagConstraints();
		    c.anchor = GridBagConstraints.CENTER;
		    c.fill = GridBagConstraints.NONE;
		    c.gridx = 0; c.gridy = 0;
		    apg.pbar.setPreferredSize(pbsize);
		    apg.pbar.setMaximumSize(pbsize);
		    pbpanel.add(apg.pbar, c);
		    pbpanel.setPreferredSize(psize);
		    frame.setGlassPane(pbpanel);
		    pbpanel.setVisible(true);

		    if (ourMode != Mode.AUTO_RUN_NO_CONTROLS) {
			JPanel controlPanel = new JPanel(new FlowLayout()) {
				public void setLocale(Locale locale) {
				    super.setLocale(locale);
				    apg.setLocale(locale);
				}
			    };
			controlPanel.setBackground(controlPanelColor);
			apg.setLocale(Locale.getDefault());
			apg.button = new JButton();
			apg.button.setIcon(playIcon);
			apg.button.setToolTipText(apg.playTip);
			apg.button.setEnabled(false);
			apg.addButtonListener();
			controlPanel.add(apg.button);
			if (ourMode == Mode.START_PAUSED_SELECTABLE) {
			    apg.snapshotButton = new JButton();
			    apg.snapshotButton.setIcon(apg.snapshotIcon);
			    apg.snapshotButton.setToolTipText
				(apg.snapshotTip);
			    apg.addSnapshotListener();
			    Insets adjustInsets = new Insets(0,0,0,0);
			    apg.adjustLeftButton = new JButton();
			    apg.adjustLeftButton.setMargin(adjustInsets);
			    apg.adjustLeftButton.setIcon(adjustLeftIcon);
			    apg.adjustLeftButton.setToolTipText
				(apg.adjustDownTip);
			    apg.slider = new JSlider(0, LIMIT, 0);
			    apg.slider.setExtent(0);
			    apg.adjustRightButton = new JButton();
			    apg.adjustRightButton.setMargin(adjustInsets);
			    apg.adjustRightButton.setIcon(adjustRightIcon);
			    apg.adjustRightButton.setToolTipText
				(apg.adjustUpTip);
			    apg.addAdjustButtonListener();
			    controlPanel.add(apg.snapshotButton);
			    controlPanel.add(new JLabel("    "));
			    controlPanel.add(apg.adjustLeftButton);
			    controlPanel.add(apg.slider);
			    controlPanel.add(apg.adjustRightButton);
			}
			frame.add(BorderLayout.SOUTH, controlPanel);
		    }
		    frame.add(BorderLayout.CENTER, panel);
		    apg.setBackground(Color.BLACK);
		    // frame.setBackground(Color.BLACK);
		    frame.pack();
		    frame.setVisible(visible);
		}
	    };
	if (SwingUtilities.isEventDispatchThread()) {
	    r.run();
	} else {
	    try {
		SwingUtilities.invokeAndWait(r);
	    } catch (InterruptedException e) {
	    } catch (InvocationTargetException e) {
	    }
	}
	return apg;
    }
}

//  LocalWords:  ISWriterOps libbzdev PanelGraphics setLocale playTip
//  LocalWords:  pauseTip javadoc frameMimeType addMetadata JPanel ap
//  LocalWords:  requestAlpha AnimatedPanelGraphics newFramedInstance
//  LocalWords:  AnimationParameters boolean fullyLoaded playThread
//  LocalWords:  JFrame JRE JDK exitOnClose frameRate setSize PRE apg
//  LocalWords:  initFrames nextOutputStreamGraphics createGraphics
//  LocalWords:  OutputStreamGraphics imageComplete BLOCKQUOTE bpar
//  LocalWords:  maxframes scheduleFrames exitTitle exitMsg exbundle
//  LocalWords:  adjustDownTip adjustUpTip snapshotTip snapshotFormat
//  LocalWords:  repositioned OSGraphicsOps IllegalStateException
//  LocalWords:  noMetadata startTimeNeeded startTime nanoTime incr
//  LocalWords:  notImplemented pbar metadataChanged ExitAccessor
//  LocalWords:  ExitPermission  accessor getSecurityManager
