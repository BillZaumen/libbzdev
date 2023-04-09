package org.bzdev.anim2d;
import org.bzdev.graphs.RefPointName;
import java.awt.geom.*;
//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Class representing two-dimensional objects with a position and orientation.
 * The orientation is the angle between a vector parallel to a unit
 * vector, that is parallel to the x axis, and a vector representing
 * the direction in which an object is oriented. The orientation is
 * represented by the angle between these two vectors. Positive angles
 * are in the counterclockwise direction.  The position is given in
 * graph coordinate space, as defined by the 
 * {@link org.bzdev.graphs.Graph Graph}
 * class, and a subclass-specific point called a reference point, at a
 * fixed location relative to the object  will be placed at that position.
 * <P>
 * Subclasses that are not abstract must have the
 * {@link org.bzdev.graphs.Graph.Graphic Graph.Graphic} method
 * {@link org.bzdev.graphs.Graph.Graphic#addTo(org.bzdev.graphs.Graph,java.awt.Graphics2D,java.awt.Graphics2D) addTo}
 * implemented.  This will typically contain code such as the following,
 * which uses an affine transformation to handle the rotations and
 * translations needed to draw the object:
 * <blockquote><pre>
 *    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
 *       Color savedColor = g2d.getColor();
 *       try {
 *          AffineTransform af = getAddToTransform();
 *          g2d.setColor(Color.BLACK);
 *          Shape shape1 = ....;
 *          shape1 = new Path2D.Double(shape1, af);
 *          graph.draw(g2d, shape1);
 *          ...
 *       } finally {
 *          g2d.setColor(savedColor);
 *       }
 *    }
 * </pre><blockquote>
 * In this example, <code>shape1</code> is created as if the object has
 * been positioned at (0.0, 0.0) in graph coordinate space with no
 * rotation. This shape is then used to create a Path2D.Double using
 * the affine transform returned by getAddToTransform() to generate the
 * shape for the current position and angle.
 * <P>
 * Reference points allow an object to be rotated about some other
 * position than (0.0, 0.0) before placing it at some position (x, y).
 * The point about which such a rotation may occur is called
 * the reference point.  The translation will be
 * such that the reference point appears at (x, y) regardless of the
 * rotation.
 * <P>
 * To draw an image, the procedure is slightly different. For example,
 * <blockquote><pre>
 *    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
 *       try {
 *          AffineTransform af = getAddToTransform();
 *          ...
 *          Point2D pt = new Point2D(imageX, imageY);
 *          af.transform(pt,pt);
 *          double x = pt.getX();
 *          double y = pt.getY();
 *          graph.drawImage(g2d, image, x, y, RefPointName.CENTER, getAngle(),
 *                          imageScale, imageScale, true);
 *          ...
 *       } catch (IOException) {
 *         ...
 *       }
 *    }
 * </pre><blockquote>
 * where <code>image</code> is an image and
 * (<code>imageX</code>, <code>imageY</code>) is the position in
 * graph coordinate space at which the image should be drawn when
 * the location is (0.0, 0.0) and the reference point for the object
 * (not the image's internal reference point) is (0.0, 0.0). The
 * argument <code>RefPointName.CENTER</code> in the call to
 * <code>drawImage</code> causes the image to be centered on the point
 * (<code>x</code>,<code>y</code>).
 * <P>
 * Subclasses that meaningfully support reference points will
 * call the method {@link #setRefPointBounds(double,double) setRefPointBounds}
 * with two arguments or the method
 * {@link #setRefPointBounds(double,double,double,double) setRefPointBounds}
 * with four arguments. These methods set a bounding box.  Standard
 * references point names exist for specifying various locations on or
 * (in one case) within the bounding box.  The bounding box must be set
 * before any drawing is done.  As a general rule, it should be set in a
 * constructor.
 * @see org.bzdev.graphs.Graph
 * @see org.bzdev.graphs.Graph.Graphic
 * @see java.awt.Graphics2D
 * @see java.awt.geom.AffineTransform
 */

public abstract class PlacedAnimationObject2D extends AnimationObject2D {

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
    public PlacedAnimationObject2D(Animation2D animation,
				   String name,
				   boolean intern)
    {
	super(animation, name, intern);
    }

    /**
     * Set an object's position and orientation.
     * Angles increase in the counterclockwise direction in graph
     * coordinate space with an angle of zero corresponding to a
     * subclass-specified direction, which will typically be one in
     * which the object 'points' towards the positive x axis from
     * position (0.0,0.0); The coordinates (x,y) are the coordinates
     * of a subclass-specific reference point that is in a fixed
     * location relative to the object.
     * @param x the x coordinate in graph coordinate space
     * @param y the y coordinate in graph coordinate space
     * @param angle the angle giving the object's orientation in radians
     */
    public void setPosition(double x, double y, double angle) {
	if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(angle)) {
	    String arg = "(" + x + ", " + y + "," + angle + ")";
	    throw new IllegalArgumentException
		(errorMsg("illegalArgument", arg));
	}
	this.x = x;
	this.y = y;
	this.angle = angle;
	if (Animation2D.level4 > -1) {
	    trace(Animation2D.level4, "setPosition - x=%g, y=%g, angle=%g "
		  + "(%g degrees)",
		  x, y, angle, Math.toRadians(angle));
	}
    }

    /**
     * Set an object's position.
     * The coordinates (x,y) are the coordinates of a subclass-specific
     * reference point that is in a fixed location relative to the object.
     * @param x the x coordinate in graph coordinate space
     * @param y the y coordinate in graph coordinate space
     */
    public void setPosition(double x, double y) {
	if (Double.isNaN(x) || Double.isNaN(y)) {
	    String arg = "("  + x + ", " + y + ")";
	    throw new IllegalArgumentException
		(errorMsg("illegalArgument", arg));
	}
	this.x = x;
	this.y = y;
    }

    /**
     * Set an object's angle.
     * Angles increase in the counterclockwise direction in graph
     * coordinate space with an angle of zero corresponding to a
     * subclass-specified direction, which will typically be one in
     * which the object 'points' towards the positive x axis from
     * position (0.0,0.0).
     * @param angle the angle giving the object's orientation in radians
     */
    public void setAngle(double angle) {
	if (Double.isNaN(angle)) {
	    String arg = "NaN";
	    throw new IllegalArgumentException
		(errorMsg("illegalArgument", arg));
	}
	this.angle = angle;
    }

    /**
     * Get an affine transform that will allow an object
     * to be drawn.
     * The affine transformation will first translate the reference
     * point to position (0.0, 0.0), then apply a rotation equal
     * to the angle passed in the last call to
     * {@link #setPosition(double,double,double)} about position (0.0, 0.0),
     * and finally translate position (0.0, 0.0) to the values of
     * (x, y) provided in the last call to
     * {@link #setPosition(double,double,double)}.
     * This affine transformation allows the user to draw an object
     * in graph-coordinate space at location (0.0, 0.0) as if the angle
     * had the value 0.0.  The object will then be rotated about the
     * reference point and the reference point will be translated to
     * the position (x, y).
     * @return the affine transform
     */
    public AffineTransform getAddToTransform() {
	AffineTransform af = AffineTransform.getTranslateInstance(x, y);
	af.rotate(angle);
	af.translate(-refPoint.getX(),-refPoint.getY());
	return af;
    }

    private double x = 0.0;
    private double y = 0.0;
    private double angle = 0.0;

    /**
     * Get the x position.
     * @return the X position in graph coordinate space
     */
    public double getX() {return x;}
    /**
     * Get the Y position.
     * @return the Y position in graph coordinate space
     */
    public double getY() {return y;}
    /**
     * Get the angle for the object's orientation.
     * @return the angle in radians
     */
    public double getAngle() {return angle;}

    double xMin = 0.0;
    double xMax = 0.0;
    double yMin = 0.0;
    double yMax = 0.0;
    double xCenter = 0.0;
    double yCenter = 0.0;

    /**
     * Get the minimum x value of the binding box.
     * This is the bounding box's minimum value of x when the
     * reference point's origin is at (0.0, 0.0) in graph coordinate space.
     * @return the minimum x value in graph coordinate space units
     */
    public double getBBXMin() {return xMin;}
    /**
     * Get the minimum y value of the binding box.
     * This is the bounding box's minimum value of y when the
     * reference point's origin is at (0.0, 0.0) in graph coordinate space.
     * @return the minimum y value in graph coordinate space units
     */
    public double getBBYMin() {return yMin;}
    /**
     * Get the maximum x value of the binding box.
     * This is the bounding box's maximum value of x when the
     * reference point's origin is at (0.0, 0.0) in graph coordinate space.
     * @return the maximum x value in graph coordinate space units
     */
    public double getBBXMax() {return xMax;}
    /**
     * Get the maximum y value of the binding box.
     * This is the bounding box's maximum value of y when the
     * reference point's origin is at (0.0, 0.0) in graph coordinate space.
     * @return the maximum y value in graph coordinate space units
     */
    public double getBBYMax() {return yMax;}


    private RefPointName refPointName = null;
    private Point2D refPoint = new Point2D.Double(0.0, 0.0);

    /**
     * Get the name of a reference point.
     * @return the reference point name, null if it is unnamed
     */
    public RefPointName getRefPointName() {
	return refPointName;
    }

    /**
     * Get the X coordinate of the reference point.
     * Drawing an object at its position in graph coordinate space
     * is equivalent to drawing that object at position (0.0, 0.0)
     * in graph coordinate space after a translation that puts the
     * reference point at position (0.0, 0.0), and then translating
     * the drawn object to its correct position. After that final
     * translation, the reference point will appear at the location
     * specified for the object.  Any rotations will be about the
     * reference point.
     * @return the X coordinate of the reference point
     */
    public double getReferencePointX() {
	return refPoint.getX();
    }

    /**
     * Get the Y component of the reference point.
     * Drawing an object at its position in graph coordinate space
     * is equivalent to drawing that object at position (0.0, 0.0)
     * in graph coordinate space after a translation that puts the
     * reference point at position (0.0, 0.0), and then translating
     * the drawn object to its correct position. After that final
     * translation, the reference point will appear at the location
     * specified for the object.  Any rotations will be about the
     * reference point.
     * @return the Y component of the reference point
     */
    public double getReferencePointY() {
	return refPoint.getY();
    }

    /**
     * Set the corners of an object's bounding box based on its height and
     * width when the object is drawn at an angle of 0 degrees.

     * The bounding box is that of the object when the object's
     * position is (0.0, 0.0) in graph coordinate space with a null
     * reference point.  The arguments width and height refer to the
     * dimensions of the bounding box in the x and y directions
     * respectively.  The point (0.0, 0.0) will be at the center of
     * the bounding box.  The reference point will be determined by
     * the last reference-point name used (if null, the reference
     * point will be at (0.0, 0.0)).
     * @param width the width of the bounding box in graph coordinate space
     * @param height the height of the box in graph coordinate space
     */
    protected void setRefPointBounds(double width, double height)
    {
	double hw = width/2.0;
	double hh = height/2.0;
	xMin = -hw;
	xMax = hw;
	yMin = -hh;
	yMax = hh;
	xCenter = 0.0;
	yCenter = 0.0;
	setRefPointByName(refPointName);
    }

    /**
     * Set the lower and upper corners of the bounding box
     * for the object when drawn at an angle of 0 degrees.
     * The bounding box is that of the object when the object's
     * position is (0.0, 0.0) in graph coordinate space with a null
     * reference point.  The reference point will be determined by the
     * last reference-point name used (if null, the reference point
     * will be at (0.0, 0.0)).
     * @param xMin the minimum x coordinate of the bounding box in
     *        graph coordinate space
     * @param xMax the maximum x coordinate of the bounding box in
     *        graph coordinate space
     * @param yMin the minimum y coordinate of the bounding box in
     *        graph coordinate space
     * @param yMax the maximum y coordinate of the bounding box in
     *        graph coordinate space
     */
    protected void setRefPointBounds(double xMin, double xMax,
				     double yMin, double yMax)
    {
	this.xMin = xMin;
	this.xMax = xMax;
	this.yMin = yMin;
	this.yMax = yMax;
	xCenter = (xMin + xMax)/2.0;
	yCenter = (yMin + yMax)/2.0;
	setRefPointByName(refPointName);
    }

    /**
     * Set the reference point based on a name
     * RefPointName is an enumeration with values UPPER_LEFT,
     * UPPER_CENTER, UPPER_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT,
     * LOWER_LEFT, LOWER_CENTER, and LOWER_RIGHT.  When the argument
     * loc is null, if (0.0, 0.0) corresponds to one of the named
     * locations, the reference point name will be set to a non-null
     * value.
     * <P>
     * This method calls {@link AnimationObject2D#update()}. Unless
     * explicitly set, the reference point name is null.
     * @param loc the name of the reference point; null for the
     *        default (position (0.0, 0.0))
     */
    public void setRefPointByName(RefPointName loc) {
	update();
	if (loc == null) {
	    setRefPoint(0.0, 0.0);
	    return;
	}
	refPointName = loc;
	switch (loc) {
	case UPPER_LEFT:
	    refPoint.setLocation(xMin, yMax);
	    break;
	case UPPER_CENTER:
	    refPoint.setLocation(xCenter, yMax);
	    break;
	case UPPER_RIGHT:
	    refPoint.setLocation(xMax, yMax);
	    break;
	case CENTER_LEFT:
	    refPoint.setLocation(xMin, yCenter);
	    break;
	case CENTER:
	    refPoint.setLocation(xCenter, yCenter);
	    break;
	case CENTER_RIGHT:
	    refPoint.setLocation(xMax, yCenter);
	    break;
	case LOWER_LEFT:
	    refPoint.setLocation(xMin, yMin);
	    break;
	case LOWER_CENTER:
	    refPoint.setLocation(xCenter, yMin);
	    break;
	case LOWER_RIGHT:
	    refPoint.setLocation(xMax, yMin);
	    break;
	}
    }

    /**
     * Set the reference point by specifying a point.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * If the reference point was never explicitly set, its
     * value will be (0.0, 0.0).
     * @param point the point in graph coordinate space corresponding
     *        to the reference point
     */
    public final void setRefPoint(Point2D point) {
       setRefPoint(point.getX(), point.getY());
    }

    /**
     * Set the reference point by specifying its x and y coordinates.
     * When the x and y match the coordinates for named reference
     * points, a named reference point is used.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * If the reference point was never explicitly set, its
     * value will be (0.0, 0.0).
     * @param x the x coordinate in graph coordinate space
     * @param y the y coordinate in graph coordinate space
     */
    public void setRefPoint(double x, double y) {
	update();
	refPoint.setLocation(x,y);
	if (x == xMin) {
	    if (y == yMin) {
		refPointName = RefPointName.LOWER_LEFT;
	    } else if (y == yCenter) {
		refPointName = RefPointName.CENTER_LEFT;
	    } else if (y == yMax) {
		refPointName = RefPointName.UPPER_LEFT;
	    } else {
		refPointName = null;
	    }
	} else if (x == xCenter) {
	    if (y == yMin) {
		refPointName = RefPointName.LOWER_CENTER;
	    } else if (y == yCenter) {
		refPointName = RefPointName.CENTER;
	    } else if (y == yMax) {
		refPointName = RefPointName.UPPER_CENTER;
	    } else {
		refPointName = null;
	    }
	} else if (x == xMax) {
	    if (y == yMin) {
		refPointName = RefPointName.LOWER_RIGHT;
	    } else if (y == yCenter) {
		refPointName = RefPointName.CENTER_RIGHT;
	    } else if (y == yMax) {
		refPointName = RefPointName.UPPER_RIGHT;
	    } else {
		refPointName = null;
	    }
	} else {
	    refPointName = null;
	}
    }

    /**
     * Set the reference point based on fractions.
     * the x coordinate of the reference point is xMin + xf * (xMax - xMin)
     * and the y coordinate is yMin + yf * (yMax - yMin).  The values
     * 0.0, 0.5, and 1.0 are handled specially: when both xf and yf
     * are set to 0.0, 0.5, or 1.0, a named reference point is used to
     * maintain consistency.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * If the reference point was never explicitly set, its
     * value will be (0.0, 0.0).
     * @param xf the x fraction (0.0 to 1.0 inclusive)
     * @param yf the y fraction (0.0 to 1.0 inclusive)
     */
    public void setRefPointByFraction(double xf, double yf) {
	if (xf == 0.0) {
	    if (y == 0.0) {
		setRefPointByName(RefPointName.LOWER_LEFT);
	    } else if (y == 0.5) {
		setRefPointByName(RefPointName.CENTER_LEFT);
	    } else if (y == 1.0) {
		setRefPointByName(RefPointName.UPPER_LEFT);
	    } else {
		double x = xMin + xf * (xMax - xMin);
		double y = yMin + yf * (yMax - yMin);
		refPointName = null;
		refPoint.setLocation(x, y);
	    }
	} else if (xf == 0.5) {
	    if (y == 0.0) {
		setRefPointByName(RefPointName.LOWER_CENTER);
	    } else if (y == 0.5) {
		setRefPointByName(RefPointName.CENTER);
	    } else if (y == 1.0) {
		setRefPointByName(RefPointName.UPPER_CENTER);
	    } else {
		double x = xMin + xf * (xMax - xMin);
		double y = yMin + yf * (yMax - yMin);
		refPointName = null;
		refPoint.setLocation(x, y);
	    }
	} else if (xf == 1.0) {
	    if (y == 0.0) {
		setRefPointByName(RefPointName.LOWER_RIGHT);
	    } else if (y == 0.5) {
		setRefPointByName(RefPointName.CENTER_RIGHT);
	    } else if (y == 1.0) {
		setRefPointByName(RefPointName.UPPER_RIGHT);
	    } else {
		update();
		double x = xMin + xf * (xMax - xMin);
		double y = yMin + yf * (yMax - yMin);
		refPointName = null;
		refPoint.setLocation(x, y);
	    }
	} else {
	    update();
	    double x = xMin + xf * (xMax - xMin);
	    double y = yMin + yf * (yMax - yMin);
	    refPointName = null;
	    refPoint.setLocation(x, y);
	}
    }

    /**
     * {@inheritDoc}
     * Defined in {@link PlacedAnimationObject2D}:
     * <UL>
     *   <LI> the reference-point name (if any).
     *   <LI> the reference point location.
     *   <LI> the reference point bounds (minimum and maximum X and Y
     *        values for the rectangle to which the reference point names
     *        refer).
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
	if (refPointName == null) {
	    out.println(prefix + "reference point unnamed");
	} else {
	    out.println(prefix + "reference point name: " + refPointName);
	}
	out.println(prefix + "reference point location: ("
		    + refPoint.getX() + ", " + refPoint.getY() + ")");
	out.println(prefix + "reference point bounds:");
	out.println(prefix + "    x min: " + xMin);
	out.println(prefix + "    x max: " + xMax);
	out.println(prefix + "    y min: " + yMin);
	out.println(prefix + "    y min: " + yMax);
    }

    /**
     * {@inheritDoc}
     * In addition, the state that is printed includes the following
     * items.
     * <P>
     * Defined in {@link PlacedAnimationObject2D}:
     * <UL>
     *   <LI> the X coordinate of this object.
     *   <LI> the Y coordinate of this object.
     *   <LI> the angle for this object, measured counterclockwise.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	double xx = getX();
	double yy = getY();
	double ang = getAngle();
	out.println(prefix + "x: " + xx);
	out.println(prefix + "y: " + yy);
	out.println(prefix + "angle: " + ang + " ("
		    + Math.toDegrees(ang) + "\u00B0)");
    }
}

//  LocalWords:  exbundle addTo blockquote pre dGCS savedColor af NaN
//  LocalWords:  getColor AffineTransform getAddToTransform setColor
//  LocalWords:  imageX imageY getX getY drawImage RefPointName xMin
//  LocalWords:  getAngle imageScale IOException setRefPointBounds xf
//  LocalWords:  IllegalArgumentException getObject illegalArgument
//  LocalWords:  setPosition xMax yMin yMax loc AnimationObject yf
//  LocalWords:  PlacedAnimationObject printName
