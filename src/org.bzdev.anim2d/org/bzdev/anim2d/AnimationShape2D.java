package org.bzdev.anim2d;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Stroke;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

import org.bzdev.graphs.Graph;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.WindingRule;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.Cloner;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Class providing an animation object representing a shape.
 * This class defines an animation object that encapsulates
 * shapes (instance of {@link java.awt.Shape Shape} or
 * shapes create by instances of
 * {@link java.awt.geom.PathIterator PathIterator}.
 * If multiple shapes are provided, their outlines are combined
 * with their inside and outside regions determined by a
 * winding rule (using the enumeration
 * {@link org.bzdev.geom.SplinePathBuilder.WindingRule WindingRule}.)
 * If the winding rule that is provided is null, the winding rule of
 * an initial path iterator, or the path iterator for an initial shape,
 * is used.
 */
public class AnimationShape2D extends AnimationObject2D {

    static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }

    /**
     * Constructor.
     * The constructor sets the visibility of this object to false.
     * This is done because, while an animation shape can be used on its
     * own, it will typically be used as part of an animation layer, and
     * an animation layer's visibility will determine if the shape is shown
     * or not.  When used directly, the caller must set the shape's
     * visibility explicitly, and should set the Z-order as well.
     * @param animation the animation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public AnimationShape2D(Animation2D animation,
			      String name,
			      boolean intern)
    {
	super(animation, name, intern);
	setVisible(false);
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

    Path2D path = null;
    Rectangle2D bbox = null;
    Color fc = null;
    Color dc = null;
    private static Stroke DEFAULT_STROKE = new BasicStroke(1.0F);
    Stroke stroke = null;
    boolean gcsMode  = false;

    @Override
    public Rectangle2D boundingBox() {
	return bbox;
    }
    
    /**
     * Get a copy of the current shape.
     * Note: the object returned is actually an instance of
     * {@link java.awt.geom.Path2D Path2D}.
     * @return the shape
     */
    public Shape getShape() {
	try {
	    return Cloner.makeClone(path);
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Clear the current path by removing its shape.
     */
    public void clear() {
	path = null;
	bbox = null;
    }


    /**
     * Provide a winding rule for this shape and initialize the shape
     * to an empty shape.
     * @param wr the winding rule for this animation shape
     * @exception NullPointerException the winding rule was null;
     */
    public void setShape(WindingRule wr) {
	if (wr == null) throw new NullPointerException(errorMsg("nullWR"));
	path = new Path2D.Double((wr == WindingRule.WIND_NON_ZERO)?
				 Path2D.WIND_NON_ZERO:
				 Path2D.WIND_EVEN_ODD);
    }

    /**
     * Set the shape for this animation shape given an instance of
     * {@link java.awt.Shape Shape}.
     * If the winding rule wr is null, the winding rule will be that
     * provided by the shape's path iterator.
     * @param wr the winding rule for the shape; null to use the winding
     *        rule provided by the shape's path iterator
     * @param s the shape
     * @exception NullPointerException both arguments are null
     */
    public void setShape(WindingRule wr, Shape s) {
	if (wr == null && s == null) {
	    throw new NullPointerException(errorMsg("nullArgs2"));
	} else {
	    if (wr == null) {
		PathIterator pi = s.getPathIterator(null);
		path = new Path2D.Double(pi.getWindingRule());
		bbox = null;
		if (s != null) {
		    path.append(pi, false);
		}
	    } else {
		path = new Path2D.Double((wr == WindingRule.WIND_NON_ZERO)?
					 Path2D.WIND_NON_ZERO:
					 Path2D.WIND_EVEN_ODD);
		bbox = null;
		if (s != null) {
		    path.append(s, false);
		}
	    }
	    if (s != null) {
		bbox = path.getBounds2D();
	    }
	}
    }

    /**
     * Set the shape for this animation shape given a path iterator
     * defining a shape.
     * If the winding rule wr is null, the winding rule will be that
     * provided by the path iterator pi.
     * @param wr the winding rule for the shape
     * @param pi the path iterator defining a shape.
     * @exception NullPointerException both arguments are null
     */
    public void setShape(WindingRule wr, PathIterator pi) {
	if (wr == null && pi == null) {
	    throw new NullPointerException(errorMsg("nullArgs2"));
	} else {
	    if (wr == null) {
		path = new Path2D.Double(pi.getWindingRule());
		path.append(pi, false);
	    } else {
		path = new Path2D.Double((wr == WindingRule.WIND_NON_ZERO)?
					 Path2D.WIND_NON_ZERO:
					 Path2D.WIND_EVEN_ODD);
		bbox = null;
		if (pi != null) {
		    path.append(pi, false);
		}
	    }
	    if (pi != null) {
		bbox = path.getBounds2D();
	    }
	}
    }

    /**
     * Append an instance of {@link java.awt.Shape Shape} to this object.
     * The winding rule is set by the first method that sets or appends
     * a shape or animation path when there is no existing shape.
     * @param s the shape
     */
    public void appendShape(Shape s) {
	if (path == null) {
	    if (s != null) {
		setShape(null, s);
	    }
	} else if (s != null) {
	    path.append(s, false);
	    if (bbox == null) {
		bbox = s.getBounds2D();
	    } else {
		bbox = bbox.createUnion(s.getBounds2D());
	    }
	}
    }

    /**
     * Append an instance of {@link java.awt.Shape Shape} to this object.
     * The winding rule is set by the first method that sets or appends
     * a shape or animation path when there is no existing shape.
     * @param pi the path iterator defining a shape
     */
    public void appendShape(PathIterator pi) {
	if (path == null) {
	    if (pi != null) {
		setShape(null, pi);
	    }
	} else if (pi != null) {
	    path.append(pi, false);
	    bbox = path.getBounds2D();
	}
    }


    /**
     * Set the color used to draw the outline of a shape.
     * @param c the color; null if the outline should not be drawn
     */
    public void setDrawColor(Color c) {
	dc = c;
    }

    /**
     * Set the color used to fill a shape.
     * @param c the color; null if the shape should not be drawn
     */
    public void setFillColor(Color c) {
	fc = c;
    }

    /**
     * Set the stroke used to draw the outline of a shape.
     * @param stroke the stroke; null for a default
     */
    public void setStroke(Stroke stroke) {
	this.stroke = (stroke == null)? DEFAULT_STROKE: stroke;
    }
    
    /**
     * Set the Graph-Coordinate-Space  mode  (GCS mode) for this shape.
     * When the outline of a shape is drawn, GCS mode determines if the
     * width of the stroke is specified in GCS units or in user-space
     * units.
     * The default value is <CODE>false</CODE>.
     * @param gcsMode true if the stroke width is given in GCS units; false
     *        if the stroke width is given in user-space units
     */
    public void setGCSMode(boolean gcsMode) {
	this.gcsMode = gcsMode;
    }

    @Override
    public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {
	Rectangle2D gbbox = g.boundingBox(true);
	if (bbox == null || touches(bbox, gbbox)) {
	    if (gcsMode) {
		Color csave = g2dGCS.getColor();
		Stroke ssave = g2dGCS.getStroke();
		if (fc != null) {
		    g2dGCS.setColor(fc);
		    g2dGCS.fill(path);
		}
		if (dc != null) {
		    g2dGCS.setColor(dc);
		    g2dGCS.setStroke(stroke);
		    g2dGCS.draw(path);
		}
		g2dGCS.setStroke(ssave);
		g2dGCS.setColor(csave);
	    } else {
		Color csave = g2d.getColor();
		Stroke ssave = g2d.getStroke();
		if (fc != null) {
		    g2d.setColor(fc);
		    g.fill(g2d, path);
		}
		if (dc != null) {
		    g2d.setColor(dc);
		    g2d.setStroke(stroke);
		    g.draw(g2d, path);
		}
		g2d.setStroke(ssave);
		g2d.setColor(csave);
	    }
	}
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
    }
}

//  LocalWords:  exbundle PathIterator WindingRule getObject wr GCS
//  LocalWords:  IllegalArgumentException NullPointerException nullWR
//  LocalWords:  nullArgs gcsMode AnimationLayer iPrefix printName
