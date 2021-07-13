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
 * Class to create a Cartesian (rectilinear) grid.
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
 * lines.
 * <P>
 * Unlike many animation objects, a CartesianGrid2D has an infinite size, but
 * only the portion that is visible within a graph's dimensions
 *  will be drawn. To add a grid to a graph, use the graph's
 * {@link Graph#add(Graph.Graphic) add} method.
 */
public class CartesianGrid2D extends AnimationObject2D {

    Graphs.CartesianGrid grid = new Graphs.CartesianGrid();

    /**
     * Get the grid spacing.
     * A value of 0.0 indicates that a default value will be used.
     * @return the grid spacing in GCS units
     */
    public double getSpacing() {return grid.getSpacing();}

    /**
     * Get the grid subspacing divisor.
     * @return the grid subspacing divisor
     */
    public int getSubspacing() {return grid.getSubspacing();}

    /**
     * Set the grid spacing.
     * @param spacing the grid spacing in GCS units; 0.0 for a default
     */
    public void setSpacing(double spacing) {
	grid.setSpacing(spacing);
    }
    
    /**
     * Set the grid subspacing divisor.
     * The value determines how a coarse grid is partitioned into a
     * fine grid.
     * Each box in the coarse grid is partitioned into
     * <CODE>subspacing</CODE> boxes along each axis.
     * For example,
     * <UL>
     *    <LI>If subspacing is 1, coarse-grid boxes are not partitioed.
     *    <LI>If subspacing is 2, coarse-grid boxes are halved in each
     *        direction, creating 4 fine-grid boxes per coarse-grid box.
     *    <LI> If subspacing is 5, coarse-grid boxes are split into fifths
     *         with a total of 25 boxes per coarse-grid box.
     *     
     * </UL>
     * The coarse grid and fine grid have boarders consisting of solid
     * lines with different colors, but the same width.
     * @param subspacing the subspacing divisor
     */
    public void setSubspacing(int subspacing) {
	grid.setSubspacing(subspacing);
    }

    /**
     * Get the color for the coarse-grid lines.
     * @return the coarse-grid line color
     */
    public Color getSpacingColor() {return grid.getSpacingColor();}


    /**
     * Get the color for the fine-grid lines.
     * @return the fine-grid line color
     */
    public Color getSubspacingColor() {return grid.getSubspacingColor();}

    
    /**
     * Get the color for the axis.
     * A point is on the axis if in graph cordinate space either x = 0
     * or y = 0. The width of a line representing an axis is the same
     * as for other lines in the grid.
     * @return the axis-line color
     */
    public Color getAxisColor() {return grid.getAxisColor();}


    /**
     * Set the colors for grid lines.
     * When a point is drawn, the axis color is used if the point is
     * on an axis, otherwise the spacing color is drawn if the point
     * is on the coarse grid.  If the point is not on the coarse grid
     * (which includes the axes), and is on the fine grid, the
     * subspacing color is used.
     * @param axisColor the color for the axis
     * @param spacingColor the color for the coarse grid
     * @param subspacingColor the color for the fine grid
     */
    public void setColors(Color axisColor, Color spacingColor,
			  Color subspacingColor)
    {
	grid.setColors(axisColor, spacingColor, subspacingColor);
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
     * @param strokeWidth the stroke width; 0.0 or negative for the default
     */
    public void setStrokeWidth(double strokeWidth) {
	grid.setStrokeWidth(strokeWidth);
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
    public CartesianGrid2D(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	setVisible(true);
    }
}
