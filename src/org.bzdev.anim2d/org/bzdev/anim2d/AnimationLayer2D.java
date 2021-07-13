package org.bzdev.anim2d;
import org.bzdev.devqsim.SimObject;
import org.bzdev.graphs.Graph;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Class for specifying a layer of objects that can appear in a
 * 2D animation.
 * <P>
 * The layer's z-order refers to its stacking order relative to
 * other animation objects.  Within a layer, each object drawn
 * is rendered in the order specified by the list passed to the
 * method {@link #initGraphicArray(java.util.List)}.
 * Layers are used to simplify the determination of z-order
 * parameters as the z-order values for other objects will not
 * have to be changed if additional objects are added to a layer.
 * Each object added to a layer is an instance of
 * {@link org.bzdev.graphs.Graph.Graphic}. Factory classes that handle
 * common cases exist (e.g., {@link AnimationLayer2DFactory}).
 */
public class AnimationLayer2D extends AnimationObject2D {
    Graph.Graphic[] graphicArray = null;

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
    public AnimationLayer2D(Animation2D animation,
			      String name,
			      boolean intern)
    {
	super(animation, name, intern);
	setVisible(true);
    }

     /**
      * Test if a Rectangle2D box1 touches  a Rectangle2D box2
      * This differs from the Rectangle2D method intersects in that
      * it works when box1 has an area of zero (a line, not a point).
      * @param box1 the first bounding box
      * @param box2 the second bounding box
      * @return true if a corner of box1 is contained by box2
      */
     private boolean touches(Rectangle2D box1, Rectangle2D box2) {
	 if (box1.intersects(box2)) return true;
	 if (box1.getWidth() == 0.0 || box1.getHeight() == 0.0) {
	     double x1 = box1.getMinX();
	     double y1 = box1.getMinY();
	     double x2 = box1.getMaxX();
	     double y2 = box1.getMaxY();
	     return box2.intersectsLine(x1, y1, x2, y2);
	 } else {
	     return false;
	 }
     }

    @Override
    public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {
	if (graphicArray == null) return;
	else {
	    Rectangle2D gbbox = g.boundingBox(true);
	    for (Graph.Graphic graphic: graphicArray) {
		Rectangle2D bbox = graphic.boundingBox();
		if (bbox == null || touches(bbox, gbbox)) {
		    graphic.addTo(g, g2d, g2dGCS);
		}
	    }
	}
    }
    
    /**
     * Initialize the array of Graph.Graphic objects that represent the
     * objects that will be rendered.
     * The objects will be rendered in the order in which they appear in
     * the list.
     * @param list a list of the objects to render
     */
    public void initGraphicArray(List<? extends Graph.Graphic> list) {
	graphicArray = new Graph.Graphic[list.size()];
	list.toArray(graphicArray);
    }

    /**
     * An enum denoting the type of objects for known instances
     * of Graph.Graphic.
     * This is used by various factories.
     */
    public static enum Type {
	/**
	 * An object type has not been specified.
	 */
	NULL,
	/**
	 * The object is an arc with a chord connecting its endpoint.
	 */
	ARC_CHORD,
	/**
	 * The object is an open arc.
	 */
	ARC_OPEN,
	/**
	 * The object is an arc with lines from the center of an ellipse
	 * overlaying the arc to the arc's end points.
	 */
	ARC_PIE,
	/**
	 * The object is a path with a specified control point
	 * (at most two control points may appear consecutively).
	 */
	CONTROL_POINT,
	/**
	 * The object is a cubic B&eacute;zier curve.
	 */
	CUBIC_CURVE,
	/**
	 * The object is an ellipse.
	 */
	ELLIPSE,
	/**
	 * The object is an image.
	 */
	IMAGE,
	/**
	 * The object is a line.
	 */
	LINE,
	/**
	 * The object is a path with a 'MOVE_TO' operation.
	 */
	MOVE_TO,
	/**
	 * The next descriptor, a SPLINE or SPLINE_FUNCTION,
	 * determines the coordinates for a 'MOVE_TO' operation.
	 */
	MOVE_TO_NEXT,
	/**
	 * Indicates the start of data describing a path.
	 */
	PATH_START,
	/**
	 * Indicates the end of data describing a path.
	 */
	PATH_END,
	/**
	 * The object is a quadratic B&eacute;zier curve.
	 */
	QUAD_CURVE,
	/**
	 * The object is a rectangle.
	 */
	RECTANGLE,
	/**
	 * The object is  rectangle with rounded corners.
	 */
	ROUND_RECTANGLE,
	/**
	 * Indicates that a path segment is closed.
	 */
	SEG_CLOSE,
	/**
	 * Indicates the end of a path segment.
	 */
	SEG_END,
	/**
	 * Indicates the end of a path segment but with the ending
	 * coordinates determined by the previous entry, which is either
	 * a SPLINE_POINT or a SPLINE_FUNCTION
	 */
	SEG_END_PREV,
	/**
	 * Indicates the end of a path segment but with the ending
	 * coordinates determined by the next entry, which is either
	 * a SPLINE_POINT or a SPLINE_FUNCTION that also starts a new
	 * spline
	 */
	SEG_END_NEXT,
	/**
	 * Indicates a spline point along a path.
	 */
	SPLINE_POINT,
	/**
	 * Indicates that functions will be used to obtain a
	 * sequence of spline points for a path.
	 */
        SPLINE_FUNCTION,
	/**
	 * Indicates that the object is an instance of
	 * {@link AnimationShape2D}.
	 */
	SHAPE,
	/**
	 * The object consists of text.
	 */
        TEXT
    }

    /**
     * {@inheritDoc}
     * Defined in {@link AnimationLayer2D}:
     * <UL>
     *  <LI> the number of objects in this animation layer.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   java.io.PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "number of objects in layer: "
		    + ((graphicArray == null)? 0: graphicArray.length));
    }
}

//  LocalWords:  initGraphicArray AnimationLayer DFactory getObject
//  LocalWords:  IllegalArgumentException enum eacute zier iPrefix
//  LocalWords:  printName
