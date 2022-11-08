package org.bzdev.anim2d;
import org.bzdev.graphs.*;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

//@exbundle org.bzdev.anim2d.lpack.Animation2D


/**
 * Configure a graph to provide a movable and zoomable view of plane.
 * This class is used to support animations by providing a view that one
 * would obtain as one moved along some path above the plane while
 * looking vertically downwards at the plane.  The view can be zoomed.
 * <P>
 * By default, an instance of GraphView is constructed so that it is
 * visible and so that its zorder is the smallest (most negative) integer
 * that can be represented by the primitive type <code>long</code>.
 */

public class GraphView extends DirectedObject2D {

    private double xf;
    private double yf;
    private double scaleX;
    private double scaleY;
    private double zoom;
    private double zoomRate = 0.0;
    private boolean initialized = false;

    // graph coordinate space initial positions of the reference point and
    // initial zoom factor for use by printConfiguration
    private double initialX = 0.0;
    private double initialY = 0.0;
    private double initialZoom = 0.0;
    
    private static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }
    /**
     * Constructor.
     * @param animation the animation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public GraphView (Animation2D animation,
			     String name,
			     boolean intern)
    {
	super(animation, name, intern);
	setZorder(Long.MIN_VALUE, true);
    }

    /**
     * Initializer.
     * The description of the arguments makes use of terminology
     * defined by the {@link Graph Graph} class.
     * <p>
     * The location (xgcs,ygcs) in graph coordinate space will be
     * positioned at a specific point in user space. This point
     * is located at (xlo+ xf*(w -xlo -xuo), h - (ylo + yf*(h-ylo-yuo)))
     * in user space, where
     * <ul>
     *   <it> xlo is the graph's lower x offset in user space.
     *   <it> xuo is the graph's upper x offset in user space.
     *   <it> ylo is the graph's lower y offset in user space.
     *   <it> yuo is the graphs' upper y offset in user space.
     *   <it> w is the width of the graph in user space.
     *   <it> h is the height of the graph in user space.
     * </ul>
     * Typically in an animation, the lower and upper x and y offsets are
     * set to zero, which is the default behavior when a graph is created.
     * <p> 
     * The scale factors scaleX and scaleY are the amount by which to
     * multiple a distance in graph coordinate space along the X and Y
     * axes respectively to get the corresponding distance in user space.
     * The zoom parameter is basically the magnification, so a value of 2.0
     * makes the image appear twice as big.
     * <P>
     * To rotate the view about the point (xgcs, ycgs), use the
     * methods {@link DirectedObject2D#setAngle(double)} or
     * {@link DirectedObject2D#setPathAngle(double)} (possibly calling
     * {@link DirectedObject2D#setAngleRelative(boolean)} if the angle
     * should be relative to a path). Objects in the view will appear to
     * rotate in the opposite direction of the view's rotation. The
     * setPath methods for {@link DirectedObject2D} can also be used,
     * as can methods that set angular velocity, angular acceleration,
     * etc.
     * <P>
     * As an example, if (xgsc, ygsc) = (500.0, 0.0), and xf and yf
     * are both 0.5, the point (500.0, 0.0) in graph coordinate space
     * will appear in the middle of the frame.  The factors scaleX and
     * scaleY determine how graph coordinate space is scaled to be
     * displayed in the frame when the zoom factor is 1.0.  The view
     * can subsequently be moved and zoomed.  If the graph view is
     * moved to 600.0, 0.0), then the point (600.0, 0.0) will be in
     * the middle of the frame.
     * <P>
     * The effect is that one can 'pan' over user space, zooming in and
     * out as appropriate. As a subclass of DirectedObject2D, one can
     * specify a path to follow and control the motion along that path.
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
     * @param scaleX the scale factor for the X direction
     * @param scaleY the scale factor for the Y direction
     * @param zoom the zoom level, which must be a positive real number
     * @exception IllegalStateException initialize was called multiple times
     */
    public void initialize(double xgcs, double ygcs,
			   double xf, double yf,
			   double scaleX, double scaleY,
			   double zoom)
    {
	if (initialized)
	    throw new IllegalStateException
		(errorMsg("multipleInits"));
		/*("initialize can be called only once");*/
	setPosition(xgcs, ygcs);
	initialX = xgcs;
	initialY = ygcs;
	this.xf = xf;
	this.yf = yf;
	this.scaleX = scaleX;
	this.scaleY = scaleY;
	this.zoom = zoom;
	this.initialZoom = zoom;
	initialized = true;
    }

    /**
     * Return the default value of the angle-relative flag.
     * This method returns <code>false</code> unless
     * overridden.
     * @return the default value for setPath methods
     * @see #setPath(AnimationPath2D,double)
     * @see #setPath(BasicSplinePath2D,double)
     */
    protected boolean defaultAngleRelative() {
	return false;
    }


    /**
     * Mode for setting zoom values in GraphView factories.
     */
    public static enum ZoomMode {
	/**
	 * Keep the current value
	 */
	KEEP,
	/**
	 * Set the zoom value explicitly.
	 */
	SET_VALUE,
	/**
	 * Set the logarithmic zoom rate.
	 */
	SET_RATE,
	/**
	 * Set the rate so that a target value is reached after
	 * a specified time interval.
	 */
	 SET_TARGET
    }


    /**
     * Set the zoom factor.
     * @param zoom the zoom factor.
     */
    public void setZoom(double zoom) {
	if (zoom <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("illegalZoom", zoom));
	}
	trace(Animation2D.level1, "setZoom - zoom=%g", zoom);
	this.zoom = zoom;
    }
    
    /**
     * Set the rotation.
     * The argument gives the angle the graph view rotates in the
     * counterclockwise direction in graph coordinate space space, causing
     * objects in graph coordinate space to rotate clockwise.
     * @param theta the rotation in radians
     */
    @Override
    public void setAngle(double theta) {
	super.setAngle(theta);
    }

    /**
     * Set an object's position and orientation.
     * The argument gives the angle the graph view rotates in the
     * counterclockwise direction in graph coordinate space, causing
     * objects in graph coordinate space to rotate clockwise.
     * The coordinates (x,y) are the coordinates of the view's reference
     * point, which will appear at a specific location in the frame
     * determined when the view was initialized.
     * @param x the x coordinate in graph coordinate space
     * @param y the y coordinate in graph coordinate space
     * @param angle the angle giving the view's orientation in radians
     */
    @Override
    public void setPosition(double x, double y, double angle) {
	super.setPosition(x, y, angle);
    }

    /**
     * Set the zoom rate logarithmicly.
     * The zoom factor will change with time as exp(zoomRate * t);
     * @param zoomRate the logarithmic zoom rate
     */
    public void setLogZoomRate(double zoomRate) {
	trace(Animation2D.level1, "setLogZoomRate - zoomRate=%g",
	      zoomRate);
	this.zoomRate = zoomRate;
    }

    /**
     * Set the zoom rate logarithmicly to zoom to a target value in
     * a specified time.
     * @param zoomTarget the target zoom value after a time interval of deltaT
     *        elapses
     * @param deltaT the time interval for the zoom factor to change
     *        from its current value to the target value
     */
    public void setLogZoomRate(double zoomTarget, double deltaT) {
	zoomRate = Math.log(zoomTarget/zoom) / deltaT;
	trace(Animation2D.level1,
	      "setLogZoomRate - zoomRate=%g for target=%g, deltaT=%g",
	      zoomRate, zoomTarget, deltaT);
    }

    // override superclass to include zooming.
    @Override
    public void update(double t, long simtime) {
	// The time getUpdatedTime() returns is the last value
	// of t passed to DirectedObject2D.update(t, simtime).
	// We do this first as the value returned will
	// be changed when update is called on our superclass.
	double lastTime = getUpdatedTime();
	if (initialized) {
	    if (zoomRate != 0.0) {
		zoom *= Math.exp(zoomRate * (t - lastTime));
	    }
	}
	super.update(t, simtime);
	if (initialized && zoomRate != 0.0) {
	    trace(Animation2D.level2, "    ...(update) - zoom=%g", zoom);
	}
    }

    /**
     * Add this object to a graph.
     * A GraphView is a special case. There should be only on in use
     * per graph. It will set up the graph so that the graph displays
     * a particular range in graph coordinate space. Angles refer to
     * the view, not the object.  Graphic operations at a particular
     * time should be performed after a graph's {@link
     * Graph#add(Graphic) add} method is called for any GraphView
     * defined for the graph, and after {@link #update(double,long)
     * update} is called with the current time as its argument.
     * @param graph the graph on which this object should be drawn
     * @param g2d  (ignored but needed by Graph.Graphics interface)
     * @param g2dGCS  (ignored but needed by Graph.Graphics interface)
     * @exception IllegalStateException initialize was not called
     */
    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS)
	throws IllegalStateException
    {
	if (!initialized)
	    throw new IllegalStateException(errorMsg("noInit"));
		/*("initialize not called");*/

	graph.setRanges(getX(), getY(), xf,  yf,
			scaleX*zoom,
			scaleY*zoom);
	graph.setRotation(getAngle(), getX(), getY());
    }

    /**
     * Get the graph width in graph coordinate space excluding the offsets.
     * @param graph the graph
     * @return the graph width in graph coordinate space, excluding any
     *        offsets
     * @exception IllegalStateException initialize was not called
     */
    public double gwidth(Graph graph) throws IllegalStateException {
	if (!initialized)
	    throw new IllegalStateException(errorMsg("noInit"));
	return (graph.getXUpper(0.0, xf, scaleX*zoom)
		- graph.getXLower(0.0, xf,scaleX*zoom));
    }

    /**
     * Get the graph height in graph coordinate space excluding the offsets.
     * @param graph the graph
     * @return the graph height in graph coordinate space, excluding any
     *        offsets
     * @exception IllegalStateException initialize was not called
     */
    public double gheight(Graph graph) throws IllegalStateException {
	if (!initialized)
	    throw new IllegalStateException(errorMsg("noInit"));
	return (graph.getYUpper(0.0, xf, scaleX*zoom)
		- graph.getYLower(0.0, xf,scaleX*zoom));
    }

    /**
     * {@inheritDoc}
     * Defined in {@link GraphView}:
     * <UL>
     *   <LI> the X reference point fractional position.
     *   <LI> the Y reference point fractional position.
     *   <LI> the X scale factor.
     *   <LI> the Y scale factor.
     *   <LI> the X initial position.
     *   <LI> the Y initial position.
     *   <LI> the initial zoom factor.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   java.io.PrintWriter out) {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "X reference-point fractional position: "
		    + xf);
	out.println(prefix + "Y reference-point fractional position: "
		    + yf);
	out.println(prefix + "X scale factor: "+ scaleX);
	out.println(prefix + "Y scale factor: "+ scaleY);
	out.println(prefix + "initial X position: "+ initialX);
	out.println(prefix + "initial Y position: "+ initialY);
	out.println(prefix + "initial zoom factor: " + initialZoom);
    }

    /**
     * {@inheritDoc}
     * Defined in {@link GraphView}:
     * <UL>
     *   <LI> the zoom factor.
     *   <LI> the zoom rate (which is logarithmic).
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out) {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "zoom factor: " + zoom);
	out.println(prefix + "zoom rate (logarithmic): " + zoomRate);
    }
}

//  LocalWords:  exbundle zoomable GraphView zorder getObject xgcs xf
//  LocalWords:  printConfiguration IllegalArgumentException ygcs xlo
//  LocalWords:  Initializer xuo ylo yf yuo ul scaleX scaleY ycgs
//  LocalWords:  DirectedObject setAngle setPathAngle boolean setPath
//  LocalWords:  setAngleRelative xgsc ygsc IllegalStateException
//  LocalWords:  multipleInits illegalZoom setZoom zoomRate deltaT
//  LocalWords:  setLogZoomRate zoomTarget superclass getUpdatedTime
//  LocalWords:  simtime dGCS noInit printName AnimationPath iPrefix
//  LocalWords:  BasicSplinePath logarithmicly
