package org.bzdev.anim2d;
import org.bzdev.devqsim.SimObject;
import org.bzdev.graphs.Graph;

import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.io.PrintWriter;

/**
 * Base class for animation objects.
 * This class specifies objects' z-order - the stacking order
 * when they are drawn in a frame, and whether an object is
 * currently visible or not.  Subclasses will typically implement
 * the method {@link #update(double,long) update} to update the
 * object's state to one appropriate for the current simulation
 * (or animation) time and must implement the method
 * {@link org.bzdev.graphs.Graph.Graphic#addTo(org.bzdev.graphs.Graph,java.awt.Graphics2D,java.awt.Graphics2D) addTo}
 * to draw the object. The subclass {@link DirectedObject2D} implements
 * {@link #update(double,long) update}.
 *
 * @see org.bzdev.graphs.Graph
 * @see org.bzdev.graphs.Graph.Graphic
 */
public abstract class AnimationObject2D extends SimObject
    implements Graph.Graphic
{

    private Animation2D animation;

    /**
     * Get the animation associated with this object.
     * @return the animation
     */
    protected Animation2D getAnimation() {
	return animation;
    }
    
    static final Comparator<AnimationObject2D> zorderComparator
			    = new Comparator<AnimationObject2D>()
    {
	public int compare(AnimationObject2D o1, AnimationObject2D o2) {
	    if (o1.zorder < o2.zorder) return -1;
	    else if (o1.zorder > o2.zorder) return 1;
	    else if (o1.zorder_tie_breaker < o2.zorder_tie_breaker) return -1;
	    else if (o1.zorder_tie_breaker > o2.zorder_tie_breaker) return 1;
	    else {
		int h1 = System.identityHashCode(o1);
		int h2 = System.identityHashCode(o2);
		if (h1 < h2) return -1;
		else if (h1 > h2) return 1;
		return 0;
	    }
	}
    };
								
    /**
     * The default z-order value.
     * This is the value used when an AnimationObject2D is initialized,
     * unless modified by a subclass' constructor, and has a value of 0.
     */
    static final long DEFAULT_ZORDER = 0;


    // zorder and zorder_tie_breaker not private because used
    // in a comparator.
    long zorder = DEFAULT_ZORDER;
    long zorder_tie_breaker;
    private boolean visible = false;

    /**
     * Return this object's zorder.
     * This determines the stacking order of objects when they are
     * drawn, with lower values being drawn first.
     * @return the zorder.
     */
    public long getZorder() {
	return zorder;
    }

    /**
     * Determine if this object is visible.
     * The status depends on the object's configuration, not whether
     * it is within its animation's frame.
     * @return true if this object is visible; false otherwise
     */
    public boolean isVisible() {
	return visible;
    }


    static AtomicLong current_zorder_tie_breaker = 
	new AtomicLong(Long.MIN_VALUE);
    
    /**
     * Set the z-order and visibility for an AnimationObject2D.
     * The z-order sets the stacking order for AnimationObject2D objects.
     * When multiple objects are displayed, those with smaller values of
     * z-order are drawn first.  For those with the same z-order value, the
     * one created first is drawn first (but the implementation uses a long
     * counter to determine creation-order so this order holds only for
     * the first 2<sup>63</sup>-1 objects created, which should be more than
     * large enough in practice.
     * The visibility of the object determines it should be drawn. Only the
     * portions of the object within the animation's frame will actually be
     * visible to the user, and being visible does not prevent another
     * object from appearing over the current object.
     * This method is more efficient than calling
     * {@link #setZorder(long) setZorder(zorder)} and
     * {@link #setVisible(boolean) setVisible(visibility)} independently.
     * @param zorder the z-order value
     * @param visible true if the object is visible; false otherwise
     */
    public void setZorder(long zorder, boolean visible) {
	if (this.zorder != zorder || this.visible != visible) {
	    if (this.visible) {
		animation.removeFromZorderSet(this);
	    }
	    this.zorder = zorder;
	    this.visible = visible;
	    if (visible) {
		animation.addToZorderSet(this);
	    }
	}
    }

    /**
     * Set the z-order value for an AnimationObject2D.
     * When multiple objects are displayed, those with smaller values of
     * z-order are drawn first.  For those with the same z-order value, the
     * one created first is drawn first (but the implementation uses a long
     * counter to determine creation-order so this order holds only for
     * the first 2<sup>63</sup>-1 objects created, which should be more than
     * large enough in practice.
     * @param zorder the z-order value
     */
    public void setZorder(long zorder) {
	setZorder(zorder, visible);
    }

    /**
     * Set the visibility for an AnimationObject2D.
     * The visibility of the object determines it should be drawn. Only the
     * portions of the object within the animation's frame will actually be
     * visible to the user, and being visible does not prevent another
     * object from appearing over the current object.
     */
    public void setVisible(boolean visible) {
	setZorder(zorder, visible);
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
    public AnimationObject2D(Animation2D animation,
			     String name,
			     boolean intern)
    {
	super(animation, name, intern);
	this.animation = animation;
	zorder_tie_breaker = current_zorder_tie_breaker.getAndIncrement();
    }

     /**
     * Add this object to a graph.
     * This will explicitly call update and will, possibly indirectly, call
     * the
     * {@link org.bzdev.graphs.Graph.Graphic#addTo(org.bzdev.graphs.Graph,java.awt.Graphics2D,java.awt.Graphics2D) addTo(Graph, Graphics2D, Graphics2D}
     * method, which must be implemented for non-abstract classes and
     * which is responsible for drawing the object.
     * <P>
     * Users generally do not have to call this method directly:
     * it will be called at the appropriate point when frames are
     * scheduled.
     * <P>
     * @param g the graph to which this object will be added
     * @see Animation2D#scheduleFrames(long,int)
     */
    public void  addToFrame(Graph g) {
	update();
	g.add(this);
    }

     /**
     * Add this object to a graph, using caller-supplied graphics contexts.
     * This will explicitly call update and will, possibly indirectly, call
     * the
     * {@link org.bzdev.graphs.Graph.Graphic#addTo(org.bzdev.graphs.Graph,java.awt.Graphics2D,java.awt.Graphics2D) addTo(Graph, Graphics2D, Graphics2D}
     * method, which must be implemented for non-abstract classes and
     * which is responsible for drawing the object.
     * <P>
     * Users generally do not have to call this method directly:
     * it will be called at the appropriate point when frames are
     * scheduled.
     * <P>
     * @param g the graph to which this object will be added
     * @param g2d the graphics context in user space for the graph g
     * @param g2dGCS the graphics context in graph coordinate space for
     *        the graph g
     * @see Animation2D#scheduleFrames(long,int)
     */
    public void  addToFrame(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {
	update();
	addTo(g, g2d, g2dGCS);
    }


    /**
     * Print this simulation object's configuration.
     * Documentation for the use of this method is provided by the documentation
     * for the {@link SimObject} method
     * {@link SimObject#printConfiguration(String,String,boolean,PrintWriter)}.
     * <P>
     * When the second argument has a value of true, the object name and
     * class name will be printed in a standard format with its
     * indentation provided by the iPrefix argument.
     * In addition, the configuration that is printed includes the following
     * items.
     * <P>
     * Defined in {@link AnimationObject2D}:
     * <UL>
     *   <LI> the Z-order.
     *   <LI> whether or not this object is visible.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "zorder: " + zorder);
	out.println(prefix + "visible: " + visible);
    }

    /**
     * Print this simulation object's state.
     * Documentation for the use of this method is provided by the documentation
     * for the {@link SimObject} method
     * {@link SimObject#printState(String,String,boolean,PrintWriter)}.
     * <P>
     * When the third argument has a value of true, the object name and
     * class name will be printed in a standard format with its
     * indentation provided by the iPrefix argument.
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
    }
}

//  LocalWords:  addTo DirectedObject AnimationObject zorder boolean
//  LocalWords:  setZorder setVisible IllegalArgumentException dGCS
//  LocalWords:  getObject scheduleFrames SimObject PrintWriter
//  LocalWords:  printConfiguration printName printState Subclasses
//  LocalWords:  comparator iPrefix
