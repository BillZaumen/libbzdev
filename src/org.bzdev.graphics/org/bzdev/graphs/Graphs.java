package org.bzdev.graphs;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import org.bzdev.geom.SplinePath2D;
import org.bzdev.geom.SplinePathBuilder;

/**
 * Utility package containing various inner classes.
 */
public class Graphs {

    // just inner classes.
    private Graphs() {}

    /**
     * Class to create a Cartesian grid.
     * A grid consists of a series of evenly spaced horizontal and
     * vertical lines, with two lines intersection at the point
     * (0.0, 0.0) in graph coordinate space. There are three types
     * of lines:
     * <UL>
     *    <LI> Axis lines are vertical and horizontal lines that go
     *         through the point (0.0, 0.0) in graph coordinate space.
     *    <LI> Coarse-grid lines are lines spaced by a specified spacing,
     *         which is the same in the X and Y directions.
     *    <LI> Fine-grid lines are lines at locations that subdivide the
     *         coarse grid so that two adjacent and parallel course-grid
     *         lines will be separated by (N-1) fine-grid lines, where
     *         N is the subspacing divisor.  When N is 1, a fine grid is
     *         not shown.
     * </UL>
     * Each of these three types of lines have their own color, with the
     * color choice for axis lines overriding the choice for coarse-grid
     * lines. While any choice is allowed, as a general rule, the color
     * should be lighter as the feature becomes more fine-grained: the
     * axis color should be more noticeable than the spacing color, which
     * should in tern be more noticeable than the subspacing color.
     */
    public static class CartesianGrid implements Graph.Graphic {

	double spacing = 0.0;
	int subspacing = 1;

	/**
	 * Get the grid spacing.
	 * This defines the spacing for course-grid lines.
	 * A value of 0.0 indicates that a default value will be used.
	 * The default assumes that roughly 10 increments should appear
	 * before the lower and upper values defined for the graph in
	 * either the X or Y direction, whichever is shortest.
	 * @return the grid spacing in GCS (Graph Coordinate Space) units
	 */
	public double getSpacing() {return spacing;}

	/**
	 * Get the grid subspacing divisor.
	 * @return the grid subspacing divisor
	 */
	public int getSubspacing() {return subspacing;}

	/**
	 * Set the grid spacing.
	 * @param spacing the grid spacing in GCS (Graph Coordinate Space) 
	 *        units; 0.0 or a negative value for a default
	 */
	public void setSpacing(double spacing) {
	    this.spacing = spacing;
	}
    
	/**
	 * Set the grid subspacing divisor.
	 * The value determines how a coarse grid is partitioned into
	 * a fine grid.  Each box in the coarse grid is partitioned
	 * into <CODE>subspacing</CODE> boxes along each axis.
	 * For example,
	 * <UL>
	 *    <LI>If subspacing is 1, coarse-grid boxes are not partitioned.
	 *    <LI>If subspacing is 2, coarse-grid boxes are halved in each
	 *        direction, creating 4 fine-grid boxes per coarse-grid box.
	 *    <LI> If subspacing is 5, coarse-grid boxes are split into fifths
	 *         with a total of 25 boxes per coarse-grid box.
	 *     
	 * </UL>
	 * The coarse grid and fine grid have boarders consisting of solid
	 * lines with different colors, but the same width.
	 * @param subspacing the subspacing divisor; 0 or a negative value
	 *        will be replaced with 1
	 */
	public void setSubspacing(int subspacing) {
	    if (subspacing < 1) subspacing = 1;
	    this.subspacing = subspacing;
	}

	/**
	 * The default color for the course-grained grid as a CSS
	 * specification.
	 */
	public static final String SPACING_COLOR_CSS = "skyblue";

	/**
	 * The default color for the fine-grained grid as a CSS
	 * specification.
	 */
	public static final String SUBSPACING_COLOR_CSS = "lightblue";

	/**
	 * The default color for the axes passing through (0.0, 0.0)
	 * as a CSS specification.
	 */
	public static final String AXIS_COLOR_CSS = "royalblue";

	/**
	 * The default color for the coarse-grained grid.
	 */
	public static final Color SPACING_COLOR =
	    Colors.getColorByCSS(SPACING_COLOR_CSS);
	/**
	 * The default color for the fine-grained grid.
	 */
	public static final Color SUBSPACING_COLOR =
	    Colors.getColorByCSS(SUBSPACING_COLOR_CSS);
	/**
	 * The default color for the axes passing through (0.0, 0.0)
	 */
	public static final Color AXIS_COLOR =
	    Colors.getColorByCSS(AXIS_COLOR_CSS);

	Color spacingColor = SPACING_COLOR;
	Color subspacingColor = SUBSPACING_COLOR;
	Color axisColor = AXIS_COLOR;

	/**
	 * Get the color for the coarse-grid lines.
	 * @return the coarse-grid line color
	 */
	public Color getSpacingColor() {return spacingColor;}


	/**
	 * Get the color for the fine-grid lines.
	 * @return the fine-grid line color
	 */
	public Color getSubspacingColor() {return subspacingColor;}

    
	/**
	 * Get the color for the axis.
	 * A point is on the axis if in graph coordinate space either x = 0
	 * or y = 0. The width of a line representing an axis is the same
	 * as for other lines in the grid.
	 * @return the axis-line color
	 */
	public Color getAxisColor() {return axisColor;}


	/**
	 * Set the colors for grid lines.
	 * When a point is drawn, the axis color is used if the point is
	 * on an axis, otherwise the spacing color is drawn if the point
	 * is on the coarse grid.  If the point is not on the coarse grid
	 * (which includes the axes), and is on the fine grid, the
	 * subspacing color is used. If the axis color is the same as
	 * the spacing color, the axis will not be visually discernible.
	 * @param axisColor the color for the axis; null for a default
	 * @param spacingColor the color for the coarse grid; null for a
	 *        default
	 * @param subspacingColor the color for the fine grid; null for a
	 *        default
	 */
	public void setColors(Color axisColor,
			      Color spacingColor,
			      Color subspacingColor)
	{
	    this.axisColor = (axisColor == null)? AXIS_COLOR: axisColor;
	    this.spacingColor = (spacingColor == null)? SPACING_COLOR:
		spacingColor;
	    this.subspacingColor = (subspacingColor == null)? SUBSPACING_COLOR:
		subspacingColor;
	}

	/**
	 * The default stroke width for a Cartesian grid.
	 */
	public static final double STROKE_WIDTH = 2.0;

	private double strokeWidth = STROKE_WIDTH;

	/**
	 * Get the width of the stroke used to draw grid lines.
	 * @return the stroke width
	 */
	public double getStrokeWidth() {
	    return strokeWidth;
	}

	/**
	 * Set the width of the stroke used to draw grid lines.
	 * @param strokeWidth the stroke width; 0.0 or negative for
	 *        the default
	 */
	public void setStrokeWidth(double strokeWidth) {
	    this.strokeWidth = (strokeWidth <= 0.0)? STROKE_WIDTH: strokeWidth;
	}


	@Override
	public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {

	    // so we don't change the field if it is 0.0
	    double spacing = this.spacing;

	    double xlower = g.getXLower();
	    double xupper = g.getXUpper();
	    double ylower = g.getYLower();
	    double yupper = g.getYUpper();

	    if (xlower > xupper) {
		double tmp = xlower;
		xlower = xupper;
		xupper = tmp;
	    }

	    if (ylower > yupper) {
		double tmp = ylower;
		xlower = yupper;
		yupper = tmp;
	    }

	    if (spacing <= 0.0) {
		double fw = xupper - xlower;
		double fh = yupper - ylower;
		spacing = (fw > fh)? fh: fw;
		spacing = Math.floor(Math.log10(spacing/10));
		if (spacing > -0.1) {
		    if (spacing < 0.0) spacing = 0.0;
		    spacing = Math.round(Math.pow(10.0, spacing));
		} else {
		    spacing = 1.0 / Math.round(Math.pow(10.0, -spacing));
		}
	    }
	    
	    
	    double rxlower = Math.floor(xlower/spacing) * spacing;
	    double rylower = Math.floor(ylower/spacing) * spacing;

	    Color savedColor = g2d.getColor();
	    Stroke savedStroke = g2d.getStroke();
	    try {
		g2d.setStroke(new BasicStroke((float)strokeWidth));
		if (subspacing != 1) {
		    g2d.setColor(subspacingColor);
		    double spacingN = spacing/subspacing;
		    for (double x = rxlower; x <= xupper; x+= spacingN) {
			if (x < xlower) continue;
			g.draw(g2d, new Line2D.Double(x, ylower, x, yupper));
		    }
		    for (double y = rylower; y <= yupper; y+= spacingN) {
			if  (y < ylower) continue;
			g.draw(g2d, new Line2D.Double(xlower, y, xupper, y));
		    }
		}
		g2d.setColor(spacingColor);
		for (double x = rxlower; x <= xupper; x+= spacing) {
		    if (x < xlower) continue;
		    g.draw(g2d, new Line2D.Double(x, ylower, x, yupper));
		}
		for (double y = rylower; y <= yupper; y+= spacing) {
		    if  (y < ylower) continue;
		    g.draw(g2d, new Line2D.Double(xlower, y, xupper, y));
		}
		g2d.setColor(axisColor);
		if (0.0 >= xlower && 0.0 <= xupper) {
		    g.draw(g2d, new Line2D.Double(0.0, ylower, 0.0, yupper));
		}
		if (0.0 >= ylower && 0.0 <= yupper) {
		    g.draw(g2d, new Line2D.Double(xlower, 0.0, xupper, 0.0));
		} 
	    } finally {
		g2d.setColor(savedColor);
		g2d.setStroke(savedStroke);
	    }
	}

	/**
	 * Constructor.
	 */
	public CartesianGrid() {
	    this(0.0, 1);
	}

	/**
	 * Constructor.
	 * @param spacing the grid spacing in GCS (Graph Coordinate Space)
	 *        units; 0.0 for a default
	 * @param subspacing the subspacing divisor
	 */
	public CartesianGrid(double spacing, int subspacing) {
	    this.spacing = spacing;
	    this.subspacing = subspacing;
	}
    }

    /**
     * Class to create a series of lines and circles representing polar
     * coordinates.
     * The caller may specify a line color, a line width, an origin
     * specified as either a point in graph coordinate space or a
     * fractional position on the graph, the angular separation in
     * degrees between radial lines, the radial separation between
     * circles.
     * <P> 
     * To improve the visual appearance, if radial lines near the
     * origin would intersect due to their line thickness, the angular
     * separation will be increased near the origin. For this to be
     * possible the product of the angular separation in degrees with
     * 2, 3, or 5 must be a divisor of 90 degrees.  The angular
     * separation in degrees is restricted to integer values.
     */
    public static class PolarGrid implements Graph.Graphic {
	double radialSpacing;
	int angularSpacing;
	boolean fractional;

	/**
	 * The default line color for a polar-coordinate grid, provided
	 * as a CSS specification.
	 */
	public static final String LINE_COLOR_CSS = "royalblue";

	/**
	 * The default line color for a polar-coordinate grid.
	 */
	public static final Color LINE_COLOR =
	    Colors.getColorByCSS(LINE_COLOR_CSS);
	Color lineColor = LINE_COLOR;
	double xo;
	double yo;

	
	/**
	 * The default stroke width for a polar-coordinate grid.
	 */
	public static final double STROKE_WIDTH = 2.0;
	double strokeWidth = STROKE_WIDTH;

	/**
	 * Get the radial spacing.
	 * The radial spacing is the spacing between concentric
	 * circles drawn to show polar coordinates.
	 * @return the radial spacing in GCS (Graph Coordinate Space) units;
	 *         0.0 indicates the default behavior
	 */
	public double getRadialSpacing() {
	    return (radialSpacing < 0.0)? 0.0: radialSpacing;
	}

	/**
	 * Get the angular spacing.
	 * The angular spacing is the spacing between adjacent radial
	 * lines drawn to show polar coordinates.
	 * @return the angular spacing in degrees.
	 */
	public int getAngularSpacing(){return angularSpacing;}

	/**
	 * Determine if the origin is located by its fractional position.
	 * When true, {@link #getXOrigin()} will return a fraction f such
	 * that the X origin is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>
	 * and {@link #getYOrigin()} will return a faction f such that
	 * the Y origin is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>.
	 * When false, the x and y GCS (Graph Coordinate Space)
	 * coordinates of the origin are the values returned by
	 * {@link #getXOrigin()} and {@link #getYOrigin()} respectively.
	 * @return true if the origin's location is specified by its 
	 *         fractional position in the graph; false if the origin
	 *         is located by its absolute position using GCS coordinates.
	 * @see #hasAbsoluteOrigin()
	 */
	public boolean hasFractionalOrigin() {return fractional;}

	/**
	 * Determine if the origin is located by its absolute position.
	 * When true, the x and y GCS (Graph Coordinate Space) 
	 * coordinates of the origin are the values returned by
	 * {@link #getXOrigin()} and {@link #getXOrigin()} respectively.
	 * When false, {@link #getXOrigin()} will return a fraction f such
	 * that the X origin on a graph is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>
	 * and {@link #getYOrigin()} will return a faction f such that
	 * the Y origin is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>.
	 * @return true if the origin is located by its absolute position
	 *         using GCS coordinates; false if the origin's location
	 *         is specified by its fractional position in the graph
	 * @see #hasFractionalOrigin()
	 */
	public boolean hasAbsoluteOrigin() {return !fractional;}

	/**
	 * Get the X origin for polar coordinates.
	 * The interpretation of the value returned depends on the
	 * value returned by {@link #hasFractionalOrigin()} or
	 * {@link #hasAbsoluteOrigin()}. When {@link #hasFractionalOrigin()}
	 * returns true, the value returned is the fraction f such
	 * that the X origin on a graph is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>
	 * in graph coordinate space units.
	 * When {@link #hasAbsoluteOrigin()} returns true, the value
	 * returned is the X coordinate of the origin in GCS units.
	 * @return the X origin.
	 */
	public double getXOrigin() {return xo;}

	/**
	 * Get the Y origin for polar coordinates.
	 * The interpretation of the value returned depends on the
	 * value returned by {@link #hasFractionalOrigin()} or
	 * {@link #hasAbsoluteOrigin()}. When {@link #hasFractionalOrigin()}
	 * returns true, the value returned is the fraction f such
	 * that the Y origin on a graph is located at
	 * <CODE>graph.getYLower()*(1.0-f) + graph.getYUpper()*f</CODE>
	 * in graph coordinate space units.
	 * When {@link #hasAbsoluteOrigin()} returns true, the value
	 * returned is the Y coordinate of the origin in GCS units.
	 * @return the Y origin.
	 */
	public double getYOrigin() {return yo;}

	/**
	 * Get the color used to draw lines representing polar coordinates.
	 * @return the line color
	 */
	public Color getColor() {return lineColor;}
	

	/**
	 * Get the stroke width for lines drawn to represent polar coordinates.
	 * @return the line width.
	 */
	public double getStrokeWidth() {return strokeWidth;}

	/**
	 * Set the origin for polar coordinates.
	 * When fractional units are used, xo is a fraction f such that
	 * the X origin is located at
	 * <CODE>graph.getXLower()*(1.0-f) + graph.getXUpper()*f</CODE>
	 * and yo is a fraction f such that
	 * the Y origin is located at
	 * <CODE>graph.getYLower()*(1.0-f) + graph.getYUpper()*f</CODE>.
	 * When absolute units are used, (xo, yo) is the location of the
	 * polar coordinate system's origin in GCS.
	 * @param xo a fraction in the range [0,1] giving the X coordinate
	 *        of the origin as a fraction of the value between
	 *        <CODE>graph.getXLower()</CODE> and
	 *        <CODE>garph.getXUpper()</CODE> when the argument
	 *        <CODE>fractional</CODE> is true; the X coordinate in
	 *        GCS units when the argument <CODE>fractional</CODE> is
	 *        false
	 * @param yo a fraction in the range [0,1] giving the Y coordinate
	 *        of the origin as a fraction of the value between
	 *        <CODE>graph.getXLower()</CODE> and
	 *        <CODE>garph.getXUpper()</CODE> when the argument
	 *        <CODE>fractional</CODE> is true; the X coordinate in
	 *        GCS units when the argument <CODE>fractional</CODE> is
	 *        false
	 * @param fractional true if fractional units are used; false if
	 *        absolute units are used
	 */
	public void setOrigin(double xo, double yo, boolean fractional) {
	    this.fractional = fractional;
	    this.xo = xo;
	    this.yo = yo;
	}

	/**
	 * Set the color used to draw lines representing polar coordinates.
	 * @param lineColor the line color; null for a default
	 */
	public void setColor(Color lineColor) {
	    this.lineColor = (lineColor == null)? LINE_COLOR: lineColor;
	}

	/**
	 * Set the stroke width for lines drawn to represent polar coordinates.
	 * @param width the stroke width; 0.0 or negative for a default
	 */
	public void setStrokeWidth(double width) {
	    strokeWidth = (width <= 0.0)? STROKE_WIDTH: width;
	}

	/**
	 * Set the radial spacing for the circles used to represent polar
	 * coordinates.
	 * @param spacing the radial spacing in GCS units; 0.0 or negative
     *        for the default behavior
	 */
	public void setRadialSpacing(double spacing) {
	    radialSpacing = spacing;
	}

	/**
	 * Set the angular spacing between radial lines used to represent
	 * polar coordinates.
	 * This value, when multiplied by 2, 3, or 5, should be a divisor
	 * of 90.
	 * @param spacing the angle in degrees
	 */
	public void setAngularSpacing(int spacing) {
	    angularSpacing = spacing;
	}

	private static double sin[] = new double[36];
	private static double cos[] = new double[36];
	static {
	    for (int i = 0; i < 36; i++) {
		double theta = Math.toRadians((double)10*i);
		sin[i] = Math.sin(theta);
		cos[i] = Math.cos(theta);
	    }
	}

	static boolean ok(double xo, double yo, double r, double xlower,
			  double ylower, double xupper, double yupper)
	{
	    if (xo + r < xlower) return false;
	    if (xo - r > xupper) return false;
	    if (yo + r < ylower) return false;
	    if (yo - r > yupper) return false;
	    return true;
	}
	    

	@Override
	public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {

	    double xlower = g.getXLower();
	    double xupper = g.getXUpper();
	    double ylower = g.getYLower();
	    double yupper = g.getYUpper();
	    double scalef = g.getXScale();

	    double xo;
	    double yo;
	    if (fractional) {
		xo = xlower *(1.0-this.xo) + xupper*this.xo;
		yo = ylower *(1.0-this.yo) + yupper*this.yo;
	    } else {
		xo = this.xo;
		yo = this.yo;
	    }

	    double radialSpacing = this.radialSpacing;

	    double t1 = xo - xlower;
	    double t2 = xupper - xo;
	    boolean left = (t1 < 0.0);
	    boolean right = (t2 < 0.0);
	    double rsx  = (t1 > t2)? t1: t2;
	    t1 = yo - ylower;
	    t2 = yupper - yo;
	    boolean below = (t1 < 0.0);
	    boolean above  = (t2 < 0.0);
	    double rsy = (t1 > t2)? t1: t2;
	    double maxr = Math.sqrt(rsx*rsx + rsy*rsy);

	    if (radialSpacing <= 0.0) {
		radialSpacing = (rsx > rsy)? rsy: rsx;
		radialSpacing /= 10.0;
	    }
	    double arcdist = (1.5*strokeWidth) / scalef;
	    double minr = arcdist/Math.toRadians((double)angularSpacing);
	    int as = angularSpacing*2;
	    if ((90 % as) != 0) {
		as = angularSpacing*3;
		if ((90 %as) != 0) {
		    as = angularSpacing*5;
		    if ((90%as) != 0) {
			as = angularSpacing;
		    }
		}
	    }
	    if ((left || right) && minr < rsx) as = angularSpacing;
	    if ((above || below) && minr < rsy) as = angularSpacing;

	    Color savedColor = g2d.getColor();
	    Stroke savedStroke = g2d.getStroke();
	    Shape savedClip = g2d.getClip();
	    try {
		g2d.setColor(lineColor);
		g2d.setStroke(new BasicStroke((float)strokeWidth));
		int xlo = g.getXLowerOffset();
		int xuo = g.getXUpperOffset();
		int ylo = g.getYLowerOffset();
		int yuo = g.getYUpperOffset();
		g2d.clip(new Rectangle(xlo, yuo,
				       g.getWidthAsInt() - (xlo + xuo),
				       g.getHeightAsInt() - (ylo + yuo)));
		double r = radialSpacing;
		double[] xs = new double[36];
		double[] ys = new double[36];
		while (r <= maxr) {
		    if (true || ok(xo, yo, r, xlower, ylower, xupper, yupper)) {
			for (int i = 0; i < 36; i++) {
			    xs[i] = xo + r*cos[i];
			    ys[i] = yo + r*sin[i];
			}
			Path2D circle = new SplinePath2D(xs, ys, true);
			g.draw(g2d, circle);
		    }
		    r += radialSpacing;
		}
		if (as == angularSpacing) {
		    for (int j = 0; j <= 90; j += angularSpacing) {
			double theta = Math.toRadians((double)j);
			double deltax = maxr * Math.cos(theta);
			double deltay = maxr * Math.sin(theta);
			if (((left && above) || (right && below)) == false) {
			    g.draw(g2d, new Line2D.Double(xo - deltax,
							  yo - deltay,
							  xo + deltax,
							  yo + deltay));
			}
			if (((left && below) || (right && above)) == false) {
			    g.draw(g2d, new Line2D.Double(xo - deltax,
							  yo + deltay,
							  xo + deltax,
							  yo - deltay));
			}
		    }
		} else {
		    int angularS = angularSpacing;
		    for (int k = 0; k < 2; k++) {
			for (int j = 0; j <= 90; j += angularS) {
			    double theta = Math.toRadians((double)j);
			    double deltax = maxr * Math.cos(theta);
			    double deltay = maxr * Math.sin(theta);
			    double xminr = minr * Math.cos(theta);
			    double yminr = minr * Math.sin(theta); 

			    if ((!left && !below)) {
				g.draw(g2d, new Line2D.Double(xo - deltax,
							      yo - deltay,
							      xo - xminr,
							      yo - yminr));
			    }

			    if ((!right && !above)) {
				g.draw(g2d, new Line2D.Double(xo + xminr,
							      yo + yminr,
							      xo + deltax,
							      yo + deltay));
			    }

			    if ((!left && !above)) {
				g.draw(g2d, new Line2D.Double(xo - deltax,
							      yo + deltay,
							      xo - xminr,
							      yo + yminr));
			    }

			    if ((!right && !below)) {
				g.draw(g2d, new Line2D.Double(xo + xminr,
							      yo - yminr,
							      xo + deltax,
							      yo - deltay));
			    }
			}
			maxr = minr;
			minr = arcdist / Math.toRadians(as);
			angularS = as;
		    }
		}
	    } finally {
		g2d.setColor(savedColor);
		g2d.setStroke(savedStroke);
		g2d.setClip(savedClip);
	    }
	    

	}


	/**
	 * Constructor.
	 */
	public PolarGrid() {
	    this(0.0, 10, 0.5, 0.5, true);
	}

	/**
	 * Constructor specifying a radial spacing and an angular spacing.
	 * @param radialSpacing the radial spacing in GCS units.
	 * @param angularSpacing the angle in degrees
	 */
	public PolarGrid(double radialSpacing, int angularSpacing) {
	    this(radialSpacing, angularSpacing, 0.5, 0.5, true);
	}

	/**
	 * Constructor specifying a radial spacing and an angular spacing.
	 * @param radialSpacing the radial spacing in GCS units.
	 * @param angularSpacing the angle in degrees
	 * @param xo a fraction in the range [0,1] giving the X coordinate
	 *        of the origin as a fraction of the value between
	 *        <CODE>graph.getXLower()</CODE> and
	 *        <CODE>garph.getXUpper()</CODE> when the argument
	 *        <CODE>fractional</CODE> is true; the X coordinate in
	 *        GCS units when the argument <CODE>fractional</CODE> is
	 *        false
	 * @param yo a fraction in the range [0,1] giving the Y coordinate
	 *        of the origin as a fraction of the value between
	 *        <CODE>graph.getXLower()</CODE> and
	 *        <CODE>garph.getXUpper()</CODE> when the argument
	 *        <CODE>fractional</CODE> is true; the X coordinate in
	 *        GCS units when the argument <CODE>fractional</CODE> is
	 *        false
	 * @param fractional true if fractional units are used; false if
	 *        absolute units are used
	 */
	public PolarGrid(double radialSpacing, int angularSpacing,
		     double xo, double yo, boolean fractional) {
	    this.fractional = fractional;
	    this.radialSpacing = radialSpacing;
	    this.angularSpacing = angularSpacing;
	    this.xo = xo;
	    this.yo = yo;
	}
    }
}

//  LocalWords:  subspacing GCS skyblue lightblue royalblue axisColor
//  LocalWords:  spacingColor subspacingColor rxlower rylower xo
//  LocalWords:  spacingN getXOrigin getYOrigin hasAbsoluteOrigin
//  LocalWords:  hasFractionalOrigin angularSpacing
