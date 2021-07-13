package org.bzdev.anim2d;
import org.bzdev.graphs.Graph;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.CPoint;
import org.bzdev.geom.SplinePathBuilder.WindingRule;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Class defining a path for animation objects.
 * Some animation objects can follow a path.  This class defines an
 * animation object that can provide such a path and, if necessary,
 * display it as part of an animation (displaying a path is useful
 * for debugging).
 */
public class AnimationPath2D extends AnimationObject2D {

    private static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }

    BasicSplinePath2D path = null;
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
    public AnimationPath2D(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	setZorder(0, false);
    }

    /**
     * Set the path.
     * @param spath the path
     */
    public void setPath(BasicSplinePath2D spath) {
	path = spath;
    }

    /**
     * Initialize the path with a default winding rule.
     */
    public void initPath() {
	path = new BasicSplinePath2D();
    }

    /**
     * Get the current path.
     * @return the path
     */
    public BasicSplinePath2D getPath() {
	if (path == null) initPath();
	return path;
    }

    /**
     * Get the maximum value of the path parameter.
     * This value is applicable for open paths, not closed paths.
     * For closed paths, it represents the period at which the path
     * is guaranteed to repeat.
     * The returned value is numerically equal to the number of
     * segments that make up the path.
     * @return the maximum value of the parameter
     * @exception IllegalStateException the path was not set
     * @see org.bzdev.geom.BasicSplinePath2D#getMaxParameter()
     */
    public double getMaxParameter() {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
		/*("path not set");*/
	return path.getMaxParameter();
    }

    /**
     * Determine if the path is a closed path.
     * @return true if it is closed; false if it is not closed
     */
    public boolean isClosed() {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.isClosed();
    }

    /**
     * Get the x coordinate for a given value of the path parameter.
     * @param u the parameter
     * @return the x coordinate of the point on the path for the
     *         specified parameter
     * @exception IllegalStateException the path is not a simple path or
     *            was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#getX(double)
     */
    public double getX(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
		/*("path not set");*/
	return path.getX(u);
    }

    /**
     * Get the y coordinate for a given value of the parameter.
     * @param u the parameter
     * @return the y coordinate of the point on the path for the
     *         specified parameter
     * @exception IllegalStateException the path is not a simple path or
     *            was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#getY(double)
     */
    public double getY(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
		/*("path not set");*/
	return path.getY(u);
    }

    /**
     * Get the point on a path corresponding to a given value of the
     * path parameter.
     * @param u the parameter
     * @return the point on the path corresponding to the specified parameter
     * @exception IllegalStateException the path is not a simple path or
     *            was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#getPoint(double)
     */
    public Point2D getPoint(double u) throws IllegalStateException,
					     IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.getPoint(u);
    }

    /**
     * Get the derivative of the x coordinate with respect to the path parameter
     * for a given value of the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path or
     *            was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#dxDu(double)
     */
    public double dxDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.dxDu(u);
    }

    /**
     * Get the derivative of the y coordinate with respect to the path
     * parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path or
     *            was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#dyDu(double)
     */
    public double dyDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.dyDu(u);
    }

    /**
     * Get the second derivative of the x coordinate with respect to
     * the path parameter for a given value of the parameter.
     * @param u the parameter
     * @return the second derivative
     * @exception IllegalStateException the path is not a simple path or
     *            the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#d2xDu2(double)
     */
    public double d2xDu2(double u) throws IllegalStateException,
					  IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.d2xDu2(u);
    }

    /**
     * Get the second derivative of the y coordinate with respect to
     * the path  parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     *            or the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#d2yDu2(double)
     */
    public double d2yDu2(double u) throws IllegalStateException,
					  IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.d2yDu2(u);
    }

    /**
     * Compute the curvature given path parameter.
     * The curvature is given by the expression
     * <blockquote><pre>
     *    (x'y" - y'x")/(xp<sup>2</sup> + y'<sup>2</sup>)<sup>3/2</sup>
     * </pre></blockquote>
     * where the derivative are computed with respect to the path parameter,
     * and is positive if the path turns counterclockwise as the parameter
     * increases, is negative if the path turns clockwise, and is zero if
     * the path follows a straight line.  The radius of curvature is the
     * multiplicative inverse of the absolute value of the curvature.
     * @param u the path-segment parameter
     * @return the curvature
     * @exception IllegalStateException the path is not a simple path
     *            or the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#curvature(double)
     */
    public double curvature(double u)
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.curvature(u);
    }

    /**
     * Get the derivative of the path length with respect to the path parameter
     * for a given value of the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     *            or the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#dsDu(double)
     */
    public double dsDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.dsDu(u);
    }

    /**
     * Get the inversion limit for the path.
     * The inversion limit (a negative number indicates that the default
     * should be used) is used by cubic splines used to map path distances
     * to path parameters. The value supplied is used when a spline is
     * created.
     * <P>
     * Computing the inverse for a cubic spline in most cases requires
     * solving a cubic equation, with valid solutions being in the
     * range [0, 1].  The inversion limit allows solutions in the
     * range [-inversionLimit, 1+inversionLimit] to be accepted, with
     * values outside of the interval [0, 1] replaced by 0 or 1,
     * whichever is closer.  The use of an inversion limit allows for
     * round-off errors.
     * @return the inversion limit
     * @exception IllegalStateException the path was not set
     * @see org.bzdev.geom.BasicSplinePath2D#getInversionLimit()
     */
    public double getInversionLimit() {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.getInversionLimit();
    }

    /**
     * Set the inversion limit.
     * The inversion limit (a negative number indicates that the default
     * should be used) is used by cubic splines used to map path distances
     * to path parameters. The value supplied is used when a spline is
     * created.
     * <P>
     * Computing the inverse for a cubic spline in most cases requires
     * solving a cubic equation, with valid solutions being in the
     * range [0, 1].  The inversion limit allows solutions in the
     * range [-inversionLimit, 1+inversionLimit] to be accepted, with
     * values outside of the interval [0, 1] replaced by 0 or 1,
     * whichever is closer.  The use of an inversion limit allows for
     * round-off errors.
     * @param limit the inversion limit; or a negative number to indicate
     *        that the default will be used.
     * @exception IllegalStateException the path was not set
     * @see org.bzdev.geom.BasicSplinePath2D#setInversionLimit(double)
     */
    public void setInversionLimit(double limit) {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	path.setInversionLimit(limit);
    }

    /**
     * Get the total length of the path.
     * @return the path length
     * @exception IllegalStateException the path is not a simple path
     *            or the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#getPathLength()
     */
    public double getPathLength()
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.getPathLength();
    }

    /**
     * Get the length of a subpath.
     * The returned value is the same if u1 and u2 are exchanged:
     * lengths are non-negative.
     * @param u1 the parameter at the first end point of a subpath
     * @param u2 the parameter at the second end point of a subpath
     * @exception IllegalStateException the path is not a simple path
     *            or the path was not set
     * @exception IllegalArgumentException the parameter is out of bounds
     * @see org.bzdev.geom.BasicSplinePath2D#getPathLength(double,double)
     */
    public double getPathLength(double u1, double u2)
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.getPathLength(u1, u2);
    }

    /**
     * Get the distance traversed on a subpath.
     * If u2 &lt; u1, the value returned is negative.
     * <P>
     * This is intended for cases where direction is important.
     * @param u1 the parameter at the first end point of a subpath
     * @param u2 the parameter at the second end point of a subpath
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getDistance(double u1, double u2)
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.getDistance(u1, u2);
    }

    /*
     * Get the distance along a path from its start for a specified
     * path parameter.
     * @param u the path parameter
     * @return the distance along a path from its start
     * @exception IllegalStateException the path was not set
     * @see org.bzdev.geom.BasicSplinePath2D#s(double)
     */
    public double s(double u)
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.s(u);
    }

    /**
     * Get the path parameter corresponding to a specified distance
     * along a path from its start.
     * @param s the distance along a path from its start
     * @return the path parameter
     * @exception IllegalStateException the path was not set
     * @see org.bzdev.geom.BasicSplinePath2D#u(double)
     */
    public double u(double s)
	throws IllegalStateException, IllegalArgumentException
    {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotSet"));
	return path.u(s);
    }

    private Color color = new Color(168, 168, 255);

    /**
     * Set the color for a line drawing this path when addTo is called.
     * @param color the color
     */
    public void setColor(Color color) {
	this.color = color;
    }

    /**
     * Get the color for a line drawing this path when addTo is called.
     * @return the color
     */
    public Color getColor() {return color;}


    private Stroke stroke = new BasicStroke(1.0F);

    /**
     * Set the stroke for a line drawing this path when addTo is called.
     * @param stroke the stroke
     */
    public void setStroke(Stroke stroke) {
	this.stroke = stroke;
    }

    /**
     * Get the stroke for a line drawing this path when addTo is called.
     * @return the stroke
     */
    public Stroke getStroke() {return stroke;}

    private boolean gcsMode = false;

    /**
     * Set GCS mode.
     * When GCS mode is set the stroke's width and dash array are interpreted
     * in graph coordinate space units; otherwise in user-space units (the
     * default).
     * @param gcsMode true if the stroke's units are in graph coordinate space
     *        units; false if they are in user space units.
     */
    public void setGcsMode(boolean gcsMode) {
	this.gcsMode = gcsMode;
    }

    /**
     * Get GCS mode.
     * When GCS mode is set the stroke's width and dash array are interpreted
     * in graph coordinate space units; otherwise in user-space units.
     * @return true if in graph-coordinate-space mode; false if in
     *          user-space mode
     */
    public boolean getGcsMode() {return gcsMode;}

    private boolean showSegmentsMode = false;
    private double radius  = 5.0;

    /**
     * Configure this path so that it will draw the end of each
     * path segment when visible and when GCS mode is false.
     * @param mode true to draw the segment ends; false to just draw
     *        the path
     * @param radius the radius of a circle representing the end of
     *        a segment
     */
    public void showSegments(boolean mode, double radius) {
	showSegmentsMode = mode;
	this.radius = radius;
    }


    @Override
    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dgcs) {
	if (path == null) return;
	if (gcsMode) {
	    Color oldColor = g2dgcs.getColor();
	    Stroke oldStroke = g2dgcs.getStroke();
	    try {
		g2dgcs.setColor(color);
		g2dgcs.setStroke(stroke);
		g2dgcs.draw(path);
	    } finally {
		g2dgcs.setColor(oldColor);
		g2dgcs.setStroke(oldStroke);
	    }
	} else {
	    Color oldColor = g2d.getColor();
	    Stroke oldStroke = g2d.getStroke();
	    try {
		g2d.setColor(color);
		g2d.setStroke(stroke);
		graph.draw(g2d, path);
		if (showSegmentsMode) {
		    PathIterator pit = path.getPathIterator(null);
		    Ellipse2D circle = new Ellipse2D.Double();
		    double x = 0.0; double y = 0.0;
		    double[] coords = new double[6];
		    double radius2 = 2*radius;
		    while(!pit.isDone()) {
			switch (pit.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
			    x = coords[0];
			    y = coords[1];
			    break;
			case PathIterator.SEG_QUADTO:
			    x = coords[2];
			    y = coords[3];
			    break;
			case PathIterator.SEG_CUBICTO:
			    x = coords[4];
			    y = coords[5];
			    break;
			case PathIterator.SEG_CLOSE:
			    pit.next();
			    continue;
			}
			Point2D p = graph.coordTransform(x, y);
			circle.setFrame(p.getX()-radius, p.getY()-radius,
					radius2, radius2);
			g2d.fill(circle);
			g2d.draw(circle);
			pit.next();
		    }
		}
	    } finally {
		g2d.setColor(oldColor);
		g2d.setStroke(oldStroke);
	    }
	}
    }

    /**
     * {@inheritDoc}
     * Defined in {@link AnimationPath2D}:
     * <UL>
     *  <LI> the configuration for a BasicSplinePath2D, provided by
     *       calling this path's method
     *       {@link BasicSplinePath2D#printTable(String,Appendable)}.
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
	if (path == null) {
	    out.println(prefix + "path: (none set)");
	} else {
	    out.println(prefix + "path:");
	    path.printTable(prefix + "    ", out);
	}
    }

}

//  LocalWords:  exbundle IllegalArgumentException getObject spath xp
//  LocalWords:  IllegalStateException getMaxParameter pathNotSet xDu
//  LocalWords:  getX getY getPoint dxDu dyDu yDu blockquote pre x'y
//  LocalWords:  y'x dsDu inversionLimit getInversionLimit subpath lt
//  LocalWords:  setInversionLimit getPathLength addTo GCS gcsMode
//  LocalWords:  AnimationPath BasicSplinePath printTable Appendable
//  LocalWords:  iPrefix printName
