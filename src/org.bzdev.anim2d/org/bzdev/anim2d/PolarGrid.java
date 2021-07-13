package org.bzdev.anim2d;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import org.bzdev.graphs.Colors;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graphs;

/**
 * Class to create a polar grid.
 * <P>
 * Unlike many animation objects, a PolarGrid has an infinite size, but
 * only the portion that is visible within a graph's dimensions
 *  will be drawn. To add a grid to a graph, use the graph's
 * {@link Graph#add(Graph.Graphic) add} method.
 */
public class PolarGrid extends AnimationObject2D {

    Graphs.PolarGrid grid = new Graphs.PolarGrid();

    /**
     * Get the radial spacing.
     * The radial spacing is the spacing between concentric
     * circles drawn to show polar coordinates.
     * @return the radial spacing in GCS (Graph Coordinate Space) units
     */
    public double getRadialSpacing() {return grid.getRadialSpacing();}

    /**
     * Get the angular spacing.
     * The angular spacing is the spacing between adjacent radial
     * lines drawn to show polar coordinates.
     * @return the angular spacing in degrees.
     */
    public int getAngularSpacing(){return grid.getAngularSpacing();}

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
    public boolean hasFractionalOrigin() {return grid.hasFractionalOrigin();}

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
    public boolean hasAbsoluteOrigin() {return grid.hasAbsoluteOrigin();}

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
    public double getXOrigin() {return grid.getXOrigin();}

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
    public double getYOrigin() {return grid.getYOrigin();}

    /**
     * Get the color used to draw lines representing polar coordinates.
     * @return the line color
     */
    public Color getColor() {return grid.getColor();}
	


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
	grid.setOrigin(xo, yo, fractional);
    }

    /**
     * Set the color used to draw lines representing polar coordinates.
     * @param lineColor the line color; null for a default
     */
    public void setColor(Color lineColor) {
	grid.setColor(lineColor);
    }


    /**
     * Get the width of the stroke used to draw grid lines.
     * @return the stroke width
     */
    public double getStrokeWidth() {
	return grid.getStrokeWidth();
    }

    /**
     * Set the width of the stroke used to draw grid lines.
     * @param strokeWidth the stroke width; 0.0 or negative for a default
     */
    public void setStrokeWidth(double strokeWidth) {
	grid.setStrokeWidth(strokeWidth);
    }


    /**
     * Set the radial spacing for the circles used to represent polar
     * coordinates. 
     * @param spacing the radial spacing in GCS units; 0.0 or negative
     *        for the default behavior
     */
    public void setRadialSpacing(double spacing) {
	grid.setRadialSpacing(spacing);
    }

    /**
     * Set the angular spacing between radial lines used to represent
     * polar coordinates.
     * This value, when multiplied by 2, 3, or 5, should be a divisor
     * of 90.
     * @param spacing the angle in degrees
     */
    public void setAngularSpacing(int spacing) {
	grid.setAngularSpacing(spacing);
    }

    @Override
    public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {
	g.add(grid);
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
    public PolarGrid(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	setVisible(true);
    }
}
