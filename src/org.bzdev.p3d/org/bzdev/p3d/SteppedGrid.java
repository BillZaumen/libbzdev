package org.bzdev.p3d;

import org.bzdev.geom.AffineTransform3D;
import org.bzdev.geom.Path3D;
import org.bzdev.geom.PathIterator3D;
import org.bzdev.geom.Rectangle3D;
import org.bzdev.geom.Shape3D;
import org.bzdev.geom.SubdivisionIterator;
import org.bzdev.geom.Surface3D;
import org.bzdev.geom.SurfaceIterator;
import org.bzdev.geom.Transform3D;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.Cloner;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.awt.geom.Rectangle2D;

//@exbundle org.bzdev.p3d.lpack.P3d

/**
 * Stepped Grid Class.
 * An open stepped grid consists of an n by m grid of
 * horizontal rectangles whose vertices, when projected
 * onto the X-Y plane, have the values <code>(xs[i],ys[i])</code>
 * where <code>xs</code> and <code>ys</code> are one-dimensional
 * arrays. A grid consists of one or two layers, and each layer
 * has a base height <code>zbase</code> and each
 * rectangle's height is specified by an offset from the base
 * height. These rectangles are called <I>components</I> and
 * have an index <code>(i,j)</code> such that their diagonals
 * connect the vertices <code>(xs[i], ys[i], z)</code> and
 * <code>(xs[i+1], ys[i+1], z)</code>.  The maximum index (in the X
 * or Y direction, and exclusive) is one less than the size of the
 * arrays xs and ys respectively. 
 * <P>
 * A closed step grid consists of two layers, an upper layer
 * and a lower layer.  Their outwards-facing sides point in
 * opposite directions and they are connected to form a closed
 * 2 dimensional manifold.  Stepped grids are either open or closed.
 * <P>
 * The indices (i,j), where i &isin; [0, <code>xs.length-2</code>] and
 * j &isin; [0, <code>ys.length-2</code>], specify components. A
 * component has a height, can be filled, and if filled, can be
 * a placeholder. When a rectangle is a placeholder, it is not filled,
 * and it is assumed that the caller will attach another surface in its
 * place.  Rectangular components are treated as follows:
 * <UL>
 *   <LI> A unfilled component is ignored.
 *   <LI> A filled component that is not a placeholder will contain
 *        a horizontal, rectangular, plane surface.
 *   <LI> A filled component that is a placeholder does not provide a
 *        a horizontal, rectangular, plane surface. Instead, whatever
 *        surface is desired must be provided by the caller.
 *   <LI> For the upper and lower layers separately, when a filled rectangular
 *        component that is not a placeholder is adjacent to
 *        filled component, meeting at two parallel edges, a vertical
 *        rectangular surface will attach these two horizontal rectangles.
 *        Vertical rectangles will similarly join pairs of horizontal
 *        components at edges for which there is no adjacent filled
 *        component.
 * </U>
 * A vertical surface will often contain multiple rectangles, as shown
 * in the following example (the triangles making up the model are shown
 * as green lines):
 * <P style="text-align: center">
 * <img src="doc-files/sgexample.png">
 * <P>
 * If the Z components of all of the horizontal rectangles are sorted,
 * each vertical rectangle will have a lower edge at one Z coordinate
 * in this sorted list and an upper edge at the next highest Z
 * coordinate.  While this results in more rectangles than are
 * actually necessary, it trivially ensures that all triangles will
 * meet at vertices, a requirement that a printable model must
 * satisfy.
 * <P>
 * This class also supports half rectangles. A half rectangle will
 * treat one half of the rectangle as filled, and the other half as
 * either unfilled or a placeholder. The halves are separated by a
 * diagonal edge.  The filled part of a half rectangle is the half
 * that has two edges, one vertical and one horizontal, in common with
 * adjacent filled components. The other components that share this
 * vertex must be filled components.  The opposite vertex (the one at
 * the opposite end of the diagonal passing through this vertex) must
 * have adjacent components that either are not filled or are placeholders.
 * <P>
 * For a closed grid using half rectangles for the upper and lower layers
 * at a given set of indices (i, j),
 * <UL>
 *    <LI> if neither half rectangle is a placeholder, a vertical rectangle
 *         will connect matching diagonals. The vertices for these diagonals
 *         will be the ends of horizontal and vertical edges for adjacent
 *         filled rectangles.
 *    <LI> If both half rectangles are placeholders, there will be
 *         vertical rectangles attaching those edges adjacent to
 *         unfilled rectangles.
 *    <LI> If one half rectangle is a placeholder and the other half
 *         rectangle is not, there will be vertical rectangles
 *         attaching those edges adjacent to unfilled rectangles.
 *         For the placeholder, half of the rectangle will be left out
 *         and the other, which is not a placeholder, will be filled
 *         as if it was a simple rectangle.
 * </UL>
 * Finally, components are added to open grids by calling
 * the methods {@link #addComponent(int, int, double)},
 * {@link #addHalfComponent(int,int,double)},
 * {@link #addComponent(int,int,double,boolean)}, or
 * {@link #addHalfComponent(int,int,double,boolean)}.
 * Components are added to closed grids by calling
 * {@link #addComponent(int,int,double,double)},
 * {@link #addHalfComponent(int,int,double,double)},
 * {@link #addComponent(int,int,double,double,boolean,boolean)}, or
 * {@link #addHalfComponent(int,int,double,double,boolean,boolean)}
 *
 * <H2>Using placeholder</H2>
 *
 * Placeholders are meaningful for closed stepped grids, not open
 * step grids - for open step grids, the shape created is the same
 * regardless of whether a component is a placeholder or not provided.
 * When a placeholder is provided for a closed grid, one will need to
 * to extend the model to make it a closed 2D manifold for 3D printing.
 * The attachment edges can be found easily because the SteppedGrid
 * class implements the {@link Shape3D} interface, and that interface
 * includes a method for computing the boundary of a shape.
 * <P>
 * The algorithm used to find the boundary of a stepped grid is to
 * first "flatten" the layers by setting the heights above the base
 * Z value to 0, and finding the boundary of this object, which will
 * only include horizontal lines (ones where Z is the same at the
 * ends). The actual boundary is then computed by setting the edges
 * to the correct Z values (e.g., the one defined by a placeholder)
 * and connecting the edges with vertical segments. Frequently there
 * will be multiple vertical segments adjacent to each other so that
 * an existing vertex does not appear in the middle of a segment.
 */
public class SteppedGrid  implements Shape3D {

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
    }

    // from constructor
    private Model3DOps<?> m3d;
    private double[] xs;
    private double[] ys;

    // used to implement the Shape3D interface.
    private OurM3D sgm3d = new OurM3D();
    private Path3D boundary = null;

    /**
     * {@inheritDoc}
     * <P>
     * A stepped grid can be printed, but may not be a two-dimensional
     * manifold: if the lower left and upper right components at a
     * point on the grid are at a different height than the upper left
     * and lower right components, four surfaces will be joined along a
     * single line. As a result, a boundary has to be computed using
     * class-specific information.  There are two cases.
     * <UL>
     *   <LI> For an open stepped grid, the horizontal line segments
     *        for a boundary are the edges between adjacent components,
     *        one of which is filled and not a placeholder, and the other
     *        of which is either not filled or is both filled and a placeholder.
     *        The height of this horizontal line is the height of the
     *        placeholder component, if there is one, and otherwise the
     *        height of the filled component.  A half component is assumed
     *        to consist of filled component that is not a placeholder and
     *        a second half the is either unfilled or a placeholder.  In
     *        both cases, the heights are identical&mdash;the height of the
     *        component and the horizontal components of the line lie along
     *        a diagonal.
     *   <LI> For a closed stepped grid, the horizontal line segments
     *        are at the boundary between a filled placeholder component and a
     *        filled component that is not a placeholder. For half components,
     *        which must be a placeholder, this line lies along the diagonal
     *        at the height specified for that component.
     * </UL>
     * These horizontal components are connected by sequences of vertical
     * line segments, with each segment terminating at the vertex of a
     * triangle.
     */
    @Override
    public Path3D getBoundary() {
	if (!done) throw new IllegalStateException((errorMsg("notDone")));
	return boundary;
    }

    Rectangle3D r3d = new Rectangle3D.Double();

    @Override
    public Rectangle3D getBounds() {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	try {
	    return Cloner.makeClone(r3d);
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	return sgm3d.getSurfaceIterator(tform);
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	return sgm3d.getSurfaceIterator(tform, level);
    }

    boolean closedManifold = false;
    @Override
    public boolean isClosedManifold() {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	return closedManifold;
    }

    @Override
    public boolean isOriented() {
	return true;
    }

    Shape3D[] components = null;


    public int numberOfComponents() {
	if (components == null) {
	    components =
		Surface3D.createComponents(getSurfaceIterator(null), null);
	}
	return components.length;
    }

    public Shape3D getComponent(int i) {
	if (components == null) {
	    components =
		Surface3D.createComponents(getSurfaceIterator(null), null);
	}
	return components[i];
    }

    /**
     * Get the number of X indices for this grid.
     * The indices's values are in the interval [0, n) where
     * n is the value returned by this method.
     * @return the number of X indices
     */
    public int gridDimX() {return xs.length;}

    /**
     * Get the number of Y indices for this grid.
     * The indices's values are in the interval [0, n) where
     * n is the value returned by this method.
     * @return the number of Y indices
     */
    public int gridDimY() {return ys.length;}

    /**
     * Get the number of X indices for this grid's components.
     * The indices's values are in the interval [0, n) where
     * n is the value returned by this method.
     * @return the number of X indices
     */
    public int compDimX() {return xs.length-1;}

    /**
     * Get the number of X indices for this grid's components.
     * The indices's values are in the interval [0, n) where
     * n is the value returned by this method.
     * @return the number of X indices
     */
    public int compDimY() {return ys.length-1;}


    private int maxi;
    private int maxj;

    private SteppedGridLayer upper = null;
    private SteppedGridLayer lower = null;
	
    /**
     * Constructor for an open stepped grid.
     * <P>
     * The surface consists of a number of horizontal rectangles,
     * connected by vertical rectangles.
     * Heights specified for  individual components are relative
     * to the grid's base height.
     * @param m3d the model to which the grid will be added
     * @param xs the X coordinates for the grid
     * @param ys the Y coordinates for the grid
     * @param z the base height for the grid
     * @param isUpper true if the outside direction faces up; false if
     *        the outside direction faces down
     * @see #addComponent(int,int,double)
     * @see #addComponent(int,int,double,boolean)
     * @see #addsCompleted()
     */
    public SteppedGrid(Model3DOps<?> m3d, double[] xs, double[] ys,
		       double z, boolean isUpper)
	    
    {
	if (m3d == null || xs == null || ys == null) {
	    throw new IllegalArgumentException(errorMsg("nullArgument"));
	}
	this.m3d = m3d;
	double[] xxs = xs.clone();
	double[] yys = ys.clone();
	if (isUpper) {
	    upper = new SteppedGridLayer(xxs, yys, z, false);
	} else  {
	    lower = new SteppedGridLayer(xxs, yys, z, true);
	}
	this.xs = xxs;
	this.ys = yys;
	maxi = xs.length - 2;
	maxj = ys.length - 2;
    }

    /**
     * Constructor for a closed stepped grid.
     * <P>
     * The surface consists of two connected open stepped grids,
     * one facing in the positive Z direction and the other in the
     * negative Z direction. These open grids are connected by
     * vertical rectangles.
     * @param m3d the model to which the grid will be added
     * @param xs the X coordinates for the grid
     * @param ys the Y coordinates for the grid
     * @param zUpper the base height for the open grid oriented so its
     *        outside faces up
     * @param zLower the base height for the open grid oriented so its
     *        outside faces down
     * @see #addComponent(int,int,double,double)
     * @see #addComponent(int,int,double,double,boolean,boolean)
     * @see #addsCompleted()
     */
    public SteppedGrid(Model3DOps<?> m3d, double[] xs, double[] ys,
		       double zUpper, double zLower)
	    
    {
	if (m3d == null || xs == null || ys == null) {
	    throw new IllegalArgumentException(errorMsg("nullArgument"));
	}
	if (zLower >= zUpper) {
	    String msg = errorMsg("heightOrder", zUpper, zLower);
	    throw new IllegalArgumentException(msg);
	}
	this.m3d = m3d;
	double[] xxs = xs.clone();
	double[] yys = ys.clone();
	upper = new SteppedGridLayer(xxs, yys, zUpper, false);
	lower = new SteppedGridLayer(xxs, yys, zLower, true);
	this.xs = xxs;
	this.ys = yys;
	maxi = xs.length - 2;
	maxj = ys.length - 2;
    }
	


    /**
     * Add a component to an open stepped grid.
     * The component describes a horizontal rectangle whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor.  The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices at <code>(xs[i], ys[j])</code> and
     * <code>(xs[i+1], ys[j+1])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param height the relative height for the component added to the
     *        grid
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addComponent(int i, int j, double height) 
	throws IllegalStateException, IllegalArgumentException 
    {
	addComponent(i, j, height, false);
    }

    /**
     * Add a half component to an open stepped grid.
     * The component describes a horizontal rectangle whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor.  The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices either at <code>(xs[i], ys[j])</code>
     * and <code>(xs[i+1], ys[j+1])</code> or at <code>(xs[i], ys[j+1])</code>
     * and <code>(xs[i+1], ys[j])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * The component is split along a diagonal. For the vertices not
     * along this diagonal, one vertex will have two and only two
     * edges that are shared with components that are not
     * placeholders, and this half of this component is treated as a
     * component that is not a placeholder. If any edge that ends at
     * the the other vertex is shared with another component, that
     * component must be a placeholder.
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param height the relative height for the component added to the
     *        grid
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a closed one
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addHalfComponent(int i, int j, double height)
	throws IllegalStateException, IllegalArgumentException
    {
	addHalfComponent(i, j, height, false);
    }

    /**
     * Add a component to an open stepped grid, specifying if the component
     * is a placeholder.
     * The component describes a horizontal rectangle whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices either at <code>(xs[i], ys[j])</code>
     * and <code>(xs[i+1], ys[j+1])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * When <code>placeholder</code> is true, the caller is responsible
     * for providing a surface attached to the component's vertices.
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param height the relative height for the component added to the
     *        grid
     * @param placeholder true if the component to be added to the
     *        grid is a placeholder; false if it is not
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addComponent(int i, int j, double height, boolean placeholder)
	throws IllegalStateException, IllegalArgumentException

    {
	if (done) throw new IllegalStateException(errorMsg("done"));
	if ((upper != null) && (lower != null)) {
	    throw new IllegalStateException(errorMsg("oneHeight"));
	}
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	SteppedGridLayer sgl = (upper != null)? upper: lower;
	sgl.addComponent(new SteppedGridLayer.LayerComponent(i, j, height,
							     placeholder));
	r3d.add(xs[i], ys[j], sgl.zbase +  height);
	r3d.add(xs[i+1], ys[j+1], sgl.zbase + height);
    }

    /**
     * Add a half component to an open stepped grid, specifying if the component
     * is a placeholder.
     * The component describes a horizontal rectangle whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices either at <code>(xs[i], ys[j])</code>
     * and <code>(xs[i+1], ys[j+1])</code> or at <code>(xs[i], ys[j+1])</code>
     * and <code>(xs[i+1], ys[j])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * The component is split along a diagonal. For the vertices not
     * along this diagonal, one vertex will have two and only two
     * edges that are shared with components that are not
     * placeholders, and this half of this component is treated as a
     * component that is not a placeholder. If any edge that ends at
     * the the other vertex is shared with another component, that
     * component must be a placeholder.  The half of this component
     * containing this other vertex is the half to which the argument
     * placeholder applies.
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param height the relative height for the component added to the
     *        grid
     * @param placeholder true if the component to be added to the
     *        grid is a placeholder; false if it is not
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a closed one
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addHalfComponent(int i, int j,
				 double height, boolean placeholder)
	throws IllegalStateException, IllegalArgumentException

    {
	if (done) throw new IllegalStateException(errorMsg("done"));
	if ((upper != null) && (lower != null)) {
	    throw new IllegalStateException(errorMsg("oneHeight"));
	}
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	SteppedGridLayer sgl = (upper != null)? upper: lower;
	sgl.addHalfComponent(new SteppedGridLayer.LayerComponent(i, j, height,
								 placeholder));
	sgl.addComponent(new SteppedGridLayer.LayerComponent(i, j, height,
								 true));
	r3d.add(xs[i], ys[j], sgl.zbase + height);
	r3d.add(xs[i+1], ys[j+1], sgl.zbase + height);
    }


    /**
     * Add two components to a grid.
     * The components describes horizontal rectangles whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. There is a component for the upper grid and
     * a corresponding component for the lower grid, each with its own
     * base height. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices at <code>(xs[i], ys[j])</code> and
     * <code>(xs[i+1], ys[j+1])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * The value of <code>upperHeight + upperBaseHeight</code> must be
     * larger than the value of <code>lowerHeight + lowerBaseHeight</code>,
     * where <code>upperBaseHeight</code> is the base height of the upper
     * grid and <code>lowerBaseHeight</code> is the base height of the
     * lower grid (these are set by a constructor).
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param upperHeight the relative height for the component added to the
     *        upper grid
     * @param lowerHeight the relative height  for the component added to the
     *        lower grid
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a not closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addComponent(int i, int j,
			     double upperHeight, double lowerHeight)
	throws IllegalStateException, IllegalArgumentException
    {
	addComponent(i, j, upperHeight, lowerHeight, false, false);
    }

    /**
     * Add two half components to a grid.
     * The components describes horizontal rectangles whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. There is a component for the upper grid and
     * a corresponding component for the lower grid, each with its own
     * base height. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices either at <code>(xs[i], ys[j])</code>
     * and <code>(xs[i+1], ys[j+1])</code> or at <code>(xs[i], ys[j+1])</code>
     * and <code>(xs[i+1], ys[j])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * The value of <code>upperHeight + upperBaseHeight</code> must be
     * larger than the value of <code>lowerHeight + lowerBaseHeight</code>,
     * where <code>upperBaseHeight</code> is the base height of the upper
     * grid and <code>lowerBaseHeight</code> is the base height of the
     * lower grid (these are set by a constructor).
     * <P>
     * The component is split along a diagonal. For the vertices not
     * along this diagonal, one vertex will have two and only two
     * edges that are shared with components that are not
     * placeholders, and this half of this component is treated as a
     * component that is not a placeholder. If any edge that ends at
     * the the other vertex is shared with another component, that
     * component must be a placeholder.
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param upperHeight the relative height for the component added to the
     *        upper grid
     * @param lowerHeight the relative height  for the component added to the
     *        lower grid
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is a not closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addHalfComponent(int i, int j,
				 double upperHeight, double lowerHeight)
	throws IllegalStateException, IllegalArgumentException
    {
	addHalfComponent(i, j, upperHeight, lowerHeight, false, false);
    }

    /**
     * Add two components to a grid, specifying if the components are
     * placeholders.
     * The components describes horizontal rectangles whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. There is a component for the upper grid and
     * a corresponding component for the lower grid, each with its own
     * base height. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices at <code>(xs[i], ys[j])</code> and
     * <code>(xs[i+1], ys[j+1])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * When <code>upperIsPlaceholder</code> or <code>lowerIsPlaceholder</code>
     * is true, the caller is responsible for providing a surface attached
     * to the vertices of the upper-grid component or lower-grid component
     * respectively.
     * <P>
     * The value of <code>upperHeight + upperBaseHeight</code> must be
     * larger than the value of <code>lowerHeight + lowerBaseHeight</code>,
     * where <code>upperBaseHeight</code> is the base height of the upper
     * grid and <code>lowerBaseHeight</code> is the base height of the
     * lower grid (these are set by a constructor).
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param upperHeight the relative height for the component added to the
     *        upper grid
     * @param lowerHeight the relative height  for the component added to the
     *        lower grid
     * @param upperIsPlaceholder true if the component to be added to the
     *        upper grid is a placeholder; false if it is not
     * @param lowerIsPlaceholder true if the component to be added to
     *        the lower grid is a placeholder; false if it is not
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is not a closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addComponent(int i, int j,
			double upperHeight, double lowerHeight,
			boolean upperIsPlaceholder,
			boolean lowerIsPlaceholder)
	throws IllegalStateException, IllegalArgumentException
    {
	if (done) throw new IllegalStateException(errorMsg("done"));
	if (upper == null || lower == null) {
	    throw new IllegalStateException(errorMsg("twoHeights"));
	}
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	if (lower.zbase + lowerHeight >= upper.zbase + upperHeight) {
	    double top = upper.zbase + upperHeight;
	    double bottom = lower.zbase + lowerHeight;
	    String msg = errorMsg("topBottom", i, j, top, bottom);
	    throw new IllegalArgumentException(msg);
	}
	upper.addComponent
	    (new SteppedGridLayer.LayerComponent(i, j,
						 upperHeight,
						 upperIsPlaceholder));
	lower.addComponent
	    (new SteppedGridLayer.LayerComponent(i, j,
						 lowerHeight,
						 lowerIsPlaceholder));

	r3d.add(xs[i], ys[j], upper.zbase +  upperHeight);
	r3d.add(xs[i+1], ys[j+1], upper.zbase + upperHeight);
	r3d.add(xs[i], ys[j], lower.zbase +  lowerHeight);
	r3d.add(xs[i+1], ys[j+1], lower.zbase + lowerHeight);
    }

    /**
     * Add two half components to a grid, specifying if the components are
     * placeholders.
     * The components describes horizontal rectangles whose edges are
     * parallel to the X and Y axes, vertically positioned a distance
     * <code>height</code> above the grid's base height that was provided
     * by the constructor. There is a component for the upper grid and
     * a corresponding component for the lower grid, each with its own
     * base height. The diagonal of the component's rectangle, projected
     * onto the X-Y plane, has vertices either at <code>(xs[i], ys[j])</code>
     * and <code>(xs[i+1], ys[j+1])</code> or at <code>(xs[i], ys[j+1])</code>
     * and <code>(xs[i+1], ys[j])</code>, where <code>xs</code> and
     * <code>ys</code> are the arrays passed to the constructor.
     * <P>
     * The value of <code>upperHeight + upperBaseHeight</code> must be
     * larger than the value of <code>lowerHeight + lowerBaseHeight</code>,
     * where <code>upperBaseHeight</code> is the base height of the upper
     * grid and <code>lowerBaseHeight</code> is the base height of the
     * lower grid (these are set by a constructor).
     * <P>
     * The component is split along a diagonal. For the vertices not
     * along this diagonal, one vertex will have two and only two
     * edges that are shared with components that are not
     * placeholders, and this half of this component is treated as a
     * component that is not a placeholder. If any edge that ends at
     * the the other vertex is shared with another component, that
     * component must be a placeholder. The half of this component
     * containing this other vertex is the half to which the arguments
     * upperIsPlaceholder and lowerIsPlacehloder apply.
     * <P>
     * If upperPlaceholder is true and lowerPlaceholder is false,
     * the entry in the lower rid is changed to a full component
     * instead of a half component. Similarly, if lowerPlaceholder is true and
     * upperPlaceholder is false, the entry in the upper rid is changed to
     * a full component instead of a half component. When both
     * upperPlaceholder and lowerPlaceholder are false,
     * @param i the index for a component's position along the X axis
     * @param j the index for a component's position along the Y axis
     * @param upperHeight the relative height for the component added to the
     *        upper grid
     * @param lowerHeight the relative height  for the component added to the
     *        lower grid
     * @param upperIsPlaceholder true if the component to be added to the
     *        upper grid is a placeholder; false if it is not
     * @param lowerIsPlaceholder true if the component to be added to
     *        the lower grid is a placeholder; false if it is not
     * @exception IllegalStateException {@link #addsCompleted()} was called
     *            or this stepped grid is not a closed grid
     * @exception IllegalArgumentException the indices were out of range
     */
    public void addHalfComponent(int i, int j,
				 double upperHeight, double lowerHeight,
				 boolean upperIsPlaceholder,
				 boolean lowerIsPlaceholder)
	throws IllegalStateException, IllegalArgumentException
    {
	if (done) throw new IllegalStateException(errorMsg("done"));
	if (upper == null || lower == null) {
	    throw new IllegalStateException(errorMsg("twoHeights"));
	}
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	if (lower.zbase + lowerHeight >= upper.zbase + upperHeight) {
	    double top = upper.zbase + upperHeight;
	    double bottom = lower.zbase + lowerHeight;
	    String msg = errorMsg("topBottom", i, j, top, bottom);
	    throw new IllegalArgumentException(msg);
	}
	if (upperIsPlaceholder || !lowerIsPlaceholder)
	    upper.addHalfComponent
		(new SteppedGridLayer.LayerComponent(i, j,
						     upperHeight,
						     upperIsPlaceholder));
	if (lowerIsPlaceholder || !upperIsPlaceholder)
	    lower.addHalfComponent
		(new SteppedGridLayer.LayerComponent(i, j,
						     lowerHeight,
						     lowerIsPlaceholder));
	upper.addComponent
	    (new SteppedGridLayer.LayerComponent(i, j,
						 upperHeight,
						 upperIsPlaceholder
						 || !lowerIsPlaceholder));
	lower.addComponent
	    (new SteppedGridLayer.LayerComponent(i, j,
						 lowerHeight,
						 lowerIsPlaceholder
						 || !upperIsPlaceholder));
	r3d.add(xs[i], ys[j], upper.zbase + upperHeight);
	r3d.add(xs[i+1], ys[j+1], lower.zbase + lowerHeight);
    }

    boolean done = false;
    double[] zvalues;

    double maxZSpacing = 0.0;

    /**
     * Set the maximum spacing between vertices in the Z direction.
     * <P>
     * This method is intended for cases where a model is configured
     * with an instance of {@link org.bzdev.geom.Transform3D}, when
     * that transform does not map straight lines to straight lines.
     * In that case, one will typically configure a grid as collection
     * of small rectangles. This method will ensure that the spacing
     * between vertices is small in the Z direction as well.
     * @param spacing the maximum spacing; 0.0 or a negative value if
     *        there is none
     */
    public void setMaxZSpacing(double spacing) {
	maxZSpacing = spacing;
    }

    /**
     * Determine if this stepped grid has a maximum spacing in the Z
     * direction.
     * @return true if a maximum spacing exists; false otherwise
     */
    public boolean hasMaxZSpacing() {
	return maxZSpacing > 0.0;
    }

    /**
     * Get the maximum spacing between vertices in the Z direction.
     * @return the maximum spacing; 0.0 if none is defined.
     */
    public double getMaxZSpacing() {
	if (maxZSpacing < 0.0) return 0.0;
	return maxZSpacing;
    }

    // Get the z coordinate of a horizontal line on a boundary.
    private double findZ(double[] lcoords, double[] coords) {

	SteppedGridLayer sgl;
	if (upper != null && lcoords[2] == upper.zbase) {
	    sgl = upper;
	} else if (lower != null && lcoords[2] == lower.zbase) {
	    sgl = lower;
	} else {
	    throw new IllegalStateException("sgl not set");
	}
	SteppedGridLayer.LayerComponent[][] rectangles
	    = sgl.rectangles;
	SteppedGridLayer.LayerComponent[][] halfRectangles
	    = sgl.halfRectangles;

	int i1 = Arrays.binarySearch(xs, lcoords[0]);
	int i2 = Arrays.binarySearch(xs, coords[0]);
	int j1 = Arrays.binarySearch(ys, lcoords[1]);
	int j2 = Arrays.binarySearch(ys, coords[1]);

	if (i1 < 0 || j1 < 0) {
	    throw new IllegalArgumentException
		(errorMsg("offGrid", lcoords[0], lcoords[1]));
	}

	if (i2 < 0 || j2 < 0) {
	    throw new IllegalArgumentException
		(errorMsg("offGrid", coords[0], coords[1]));
	}

	SteppedGridLayer.LayerComponent c1;
	SteppedGridLayer.LayerComponent c2;
	if ((i1 == i2) || (j1 == j2)) {
	    if (i1 == i2) {
		if (j1 < j2) {
		    c1 = (i1 == 0)? null: rectangles[i1-1][j1];
		    c2 = (i1 == xs.length-1)? null: rectangles[i1][j1];
		} else if (j1 > j2) {
		    c1 = (i1 == 0)? null: rectangles[i1-1][j2];
		    c2 = (i1 == xs.length-1)? null: rectangles[i1][j2];
		} else {
		    throw new IllegalStateException(errorMsg("lineIsVertical"));
		}
	    } else if (j1 == j2) {
		if (i1 < i2) {
		    c1 = (j1 == 0)? null: rectangles[i1][j1-1];
		    c2 = (j1 == ys.length-1)? null: rectangles[i1][j1];
		} else {
		    c1 = (j1 == 0)? null: rectangles[i2][j1-1];
		    c2 = (j1 == ys.length-1)? null: rectangles[i2][j1];
		}
	    } else {
		throw new IllegalStateException(errorMsg("noComponentsSet"));
	    }
	    if (c1 == null) {
		return sgl.zbase + c2.height;
	    } else if (c2 == null) {
		return sgl.zbase + c1.height;
	    } else if (!c1.filled) {
		return sgl.zbase + c2.height;
	    } else if (!c2.filled) {
		return sgl.zbase + c1.height;
	    } else if (c1.placeholder) {
		return sgl.zbase + c1.height;
	    } else if (c2.placeholder) {
		return sgl.zbase + c2.height;
	    } else {
		double xx1 = lcoords[0];
		double yy1 = lcoords[1];
		double xx2 = coords[0];
		double yy2 = coords[1];
		throw new IllegalStateException
		    (errorMsg("noHeight", xx1, yy1, xx2, yy2));
	    }
	} else {
	    if (i1 < i2) {
		if (j1 < j2) {
		    c1 = halfRectangles[i1][j1];
		} else {
		    c1 = halfRectangles[i1][j2];
		}
	    } else {
		if (j1 < j2) {
		    c1 = halfRectangles[i2][j1];
		} else {
		    c1 = halfRectangles[i2][j2];
		}
	    }
	    return sgl.zbase + c1.height;
	}
    }

    /**
     * Indicate that all components have been added and add
     * this object to its model.
     * @exception IllegalStateException the components of this
     *            stepped grid were incorrectly configured.
     */
    public void addsCompleted() throws IllegalStateException {
	OurM3D fm3d = new OurM3D();
	if (done) return;
	done = true;
	addsCompleted(m3d, fm3d);
	fm3d.addsCompleted();
	addsCompleted(sgm3d, null);
	sgm3d.addsCompleted();

	closedManifold = sgm3d.isClosedManifold();
	Path3D sgmpath = fm3d.getBoundary();
	if (sgmpath != null) {
	    boundary = new Path3D.Double();
	    PathIterator3D it = sgmpath.getPathIterator(null);
	    double[] coords = new double[9];
	    double[] lcoords = new double[3];
	    double[] fcoords = new double[3];
	    boolean sawMove = false;
	    double z = 0.0;
	    double lz = 0.0;
	    double fz = 0.0;
	    int offset = 0;
	    while (!it.isDone()) {
		int type = it.currentSegment(coords);
		switch (type) {
		case PathIterator3D.SEG_MOVETO:
		    sawMove = true;
		    System.arraycopy(coords, 0, fcoords, 0, 3);
		    offset = 0;
		    break;
		case PathIterator3D.SEG_LINETO:
		    z = findZ(lcoords, coords);
		    if (sawMove) {
			boundary.moveTo(lcoords[0], lcoords[1], z);
			sawMove = false;
			lz = z;
			fz = z;
		    }
		    if (Math.abs(z - lz) > 1.e-10) {
			int zind1 = Arrays.binarySearch(zvalues, lz);
			int zind2 = Arrays.binarySearch(zvalues, z);
			int incr = (zind1 > zind2)? -1: 1;
			if (zind2 > zind1) {
			    zind1++;
			    for (int ind = zind1; ind != zind2; ind++) {
				boundary.lineTo(lcoords[0], lcoords[1],
						zvalues[ind]);
			    }
			} else {
			    zind1--;
			    for (int ind = zind1; ind != zind2; ind--) {
				boundary.lineTo(lcoords[0], lcoords[1],
						zvalues[ind]);
			    }
			}
			boundary.lineTo(lcoords[0], lcoords[1], z);
		    }
		    boundary.lineTo(coords[0], coords[1], z);
		    offset = 0;
		    break;
		case PathIterator3D.SEG_CLOSE:
		    if (Math.abs(lz - fz) > 1.e-10) {
			int zind1 = Arrays.binarySearch(zvalues, lz);
			int zind2 = Arrays.binarySearch(zvalues, fz);
			int incr = (zind1 > zind2)? -1: 1;
			if (zind2 > zind1) {
			    zind1++;
			    for (int ind = zind1; ind != zind2; ind++) {
				boundary.lineTo(lcoords[0], lcoords[1],
						zvalues[ind]);
			    }
			} else {
			    zind1--;
			    for (int ind = zind1; ind != zind2; ind--) {
				boundary.lineTo(lcoords[0], lcoords[1],
						zvalues[ind]);
			    }
			}
			boundary.lineTo(lcoords[0], lcoords[1], fz);
		    }
		    boundary.closePath();
		    break;
		default:
		    throw new UnexpectedExceptionError();
		}
		System.arraycopy(coords,offset, lcoords, 0, 3);
		lz = z;
		it.next();
	    }
	}
    }

    /*
     * Add this object to its model. The check of the variable 'done'
     * is not made so the method can be used elsewhere within this
     * class (e.g., to create a surface iterator).
     */
    void addsCompleted(Model3DOps<?> m3d, Model3DOps<?>fm3d)
	throws IllegalStateException
    {
	 if (upper != null) upper.addRectangles(m3d, fm3d);
	 if (lower != null) lower.addRectangles(m3d, fm3d);
	if (upper == null) {
	    zvalues = SteppedGridLayer.attach(m3d, lower, maxZSpacing);
	} else if (lower == null) {
	    zvalues = SteppedGridLayer.attach(m3d, upper, maxZSpacing);
	} else {
	    zvalues = SteppedGridLayer.attach(m3d, fm3d, upper, lower,
					      maxZSpacing);
	}
    }

    /**
     * Determine if a stepped grid has an upper grid.
     * @return true if this stepped grid has an upper grid; false otherwise
     */
    public boolean hasUpperGrid() {return upper != null;}

    /**
     * Determine if a stepped grid has a lower grid.
     * @return true if this stepped grid has a lower grid; false otherwise
     *
     */
    public boolean hasLowerGrid() {return lower != null;}

    /**
     * Determine if a component of a stepped grid is filled.
     * @param i the index for this component's position along the X axis
     * @param j the index for this component's position along the Y axis
     * @return true if this component is filled; false if it is not filled
     * @exception IllegalStateException {@link #addsCompleted()} was called
     * @exception IllegalArgumentException the indices were out of range
     */
    public boolean isComponentFilled(int i, int j, boolean isUpper)
	throws IllegalArgumentException, IllegalStateException
    {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	SteppedGridLayer sgl = isUpper? upper: lower;
	if (sgl == null) {
	    String msg = errorMsg("isUpperShouldBe", !isUpper);
	    throw new IllegalArgumentException(msg);
	}
	
	return sgl.rectangles[i][j].filled;
    }
    
    /**
     * Determine if a component of a stepped grid is a placeholder.
     * A component that is a placeholder is a filled component whose
     * horizontal rectangle is not created by this stepped grid. Instead,
     * the user is responsible for creating a set of triangles whose
     * edges include the edges of the rectangle that would otherwise have
     * been added.
     * @param i the index for this component's position along the X axis
     * @param j the index for this component's position along the Y axis
     * @return true if this component is a placeholder; false if it is not
     *         a placeholder
     * @exception IllegalStateException {@link #addsCompleted()} was called
     * @exception IllegalArgumentException the indices were out of range
     */
    public boolean isComponentPlaceholder(int i, int j, boolean isUpper)
	throws IllegalArgumentException, IllegalStateException
    {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	SteppedGridLayer sgl = isUpper? upper: lower;
	if (sgl == null) {
	    String msg = errorMsg("isUpperShouldBe", !isUpper);
	    throw new IllegalArgumentException(msg);
	}
	return sgl.rectangles[i][j].placeholder;
    }

    /**
     * Get the X value of the vertex of the lower or upper grid with
     * a specified index
     * @param i the X index for the vertex
     * @return the X value
     * @exception IllegalArgumentException the index were out of range
     */
    public double getX(int i) throws IllegalArgumentException {
	if (i < 0 || i >= xs.length) {
	    throw new IllegalArgumentException(errorMsg("arg1Range", i));
	}
	return xs[i];
    }

    /**
     * Get the Y value of the vertex of the lower or upper grid with
     * a specified index
     * @param j the Y index for the vertex
     * @return the Y value
     * @exception IllegalArgumentException the index were out of range
     */
    public double getY(int j) throws IllegalArgumentException {
	if (j < 0 || j >= ys.length) {
	    throw new IllegalArgumentException(errorMsg("arg1Range", j));
	}
	return ys[j];
    }

    /**
     * Get the Z value of the vertex of the lower or upper grid with
     * specified indices <code>(i, j)</code>.
     * @param i the X index for the vertex
     * @param j the Y index for the vertex
     * @param isUpper true if the returned value is for the upper grid;
     *        false for the lower grid
     * @return the Z value
     * @exception IllegalStateException {@link #addsCompleted()} was called
     * @exception IllegalArgumentException the indices were out of range
     */
    public double getZ(int i, int j, boolean isUpper)
	throws IllegalArgumentException, IllegalStateException
    {
	if (!done) throw new IllegalStateException(errorMsg("notDone"));
	if (i < 0 || i > maxi || j < 0 || j > maxj) {
	    throw new IllegalArgumentException(errorMsg("indicesRange", i, j));
	}
	SteppedGridLayer sgl = isUpper? upper: lower;
	if (sgl == null) {
	    String msg = errorMsg("isUpperShouldBe", !isUpper);
	    throw new IllegalArgumentException(msg);
	}
	if (i < 0 || i >= xs.length-1) {
	    throw new IllegalArgumentException(errorMsg("arg1Range", i));
	}
	if (j < 0 || j >= ys.length-1) {
	    throw new IllegalArgumentException(errorMsg("arg2Range", j));
	}
	SteppedGridLayer.LayerComponent c = sgl.rectangles[i][j];
	if (!c.filled) {
	    String msg = errorMsg("notFilled", i, j);
	    throw new IllegalArgumentException(msg);
	}
	return sgl.zbase + c.height;
    }

    private static double[] emptyArray = new double[0];

    private static LinkedList<double[]> pairs = new LinkedList<>();
    static {
	int np = Runtime.getRuntime().availableProcessors();
	int n = 2*np;
	for (int i = 0; i < n; i++) {
	    pairs.add(new double[2]);
	}
    }

    // The next three methods manage a pool of arrays of length 2.
    // The idea is that the number of concurrently executing threads
    // is limited by the number of available processors, so we can
    // use a pool to avoid allocating small arrays needlessly.

    private static synchronized double[] findPair() {
	double[] result = pairs.poll();
	if (result == null) result = new double[2];
	return result;
    }

    private static synchronized void releasePair(double[] pair) {
	pairs.add(pair);
    }

    private static synchronized void releasePairs(double[] pair1,
						  double[] pair2)
    {
	pairs.add(pair1);
	pairs.add(pair2);
    }

    private double[] getZPair(int i, int j, boolean isUpper) {
	SteppedGridLayer sgl = isUpper? upper: lower;
	if (sgl == null) {
	    String msg = errorMsg("isUpperShouldBe", !isUpper);
	    throw new IllegalArgumentException(msg);
	}
	if (i < 0 || i >= xs.length) {
	    throw new IllegalArgumentException(errorMsg("arg1Range", i));
	}
	if (j < 0 || j >= ys.length) {
	    throw new IllegalArgumentException(errorMsg("arg2Range", j));
	}
	sgl.update();

	SteppedGridLayer.LayerComponent c = null;
	SteppedGridLayer.LayerComponent cleft = null;
	SteppedGridLayer.LayerComponent clower = null;
	SteppedGridLayer.LayerComponent clowerLeft = null;

	int n = sgl.rectangles.length;
	int m = sgl.rectangles[0].length;
	if (i < n && j < m) c = sgl.rectangles[i][j];
	if (i > 0 && j < m) cleft = sgl.rectangles[i-1][j];
	if (i < m && j > 0) clower = sgl.rectangles[i][j-1];
	if (i > 0 && j > 0) clowerLeft = sgl.rectangles[i-1][j-1];

	if (c != null && !c.filled) c = null;
	if (cleft != null && !cleft.filled) cleft = null;
	if (clower != null && !clower.filled) clower = null;
	if (clowerLeft != null && !clowerLeft.filled) clowerLeft = null;

	double min = Double.POSITIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;
	double z = Double.NaN;
	if (c != null) {
	    z = sgl.zbase + c.height;
	    if (z < min) min = z;
	    if (z > max) max = z;
	}
	if (cleft != null) {
	    z = sgl.zbase + cleft.height;
	    if (z < min) min = z;
	    if (z > max) max = z;
	}
	if (clower != null) {
	    z = sgl.zbase + clower.height;
	    if (z < min) min = z;
	    if (z > max) max = z;
	}
	if (clowerLeft != null) {
	    z = sgl.zbase + clowerLeft.height;
	    if (z < min) min = z;
	    if (z > max) max = z;
	}

	if (Double.isNaN(z)) {
	    return emptyArray;
	}
	double[] pair = findPair();
	pair[0] = min;
	pair[1] = max;
	return pair;
    }

    /**
     * Get the Z values for all vertices associated with an
     * ordered pair of indices <code>(i, j)</code> for the upper or lower
     * grid.
     * <P>
     * When adjacent components have different heights, a set of vertical
     * rectangles will be added to this object's model. This method returns
     * the Z values of each vertex along the vertical line connecting
     * the components with a vertex at the point corresponding to the index
     * <code>(i, j)</code>.
     * <P>
     * If there are no filled components adjacent to the vertex associated
     * with the indices <code>(i,j)</code>, a zero-length array will be
     * returned.
     * @param i the X index for the vertex
     * @param j the Y index for the vertex
     * @param isUpper true if the returned value is for the upper grid;
     *        false for the lower grid
     * @return the Z values

     */
    public double[] getZs(int i, int j, boolean isUpper)
	throws IllegalArgumentException
    {

	double[] pair = getZPair(i, j, isUpper);
	if (pair == emptyArray) return pair;

	try {
	    int zind1 = Arrays.binarySearch(zvalues, pair[0]);
	    int zind2 = Arrays.binarySearch(zvalues, pair[1]);
	    int nz = zind2 - zind1 + 1;
	    double[] result = new double[nz];
	    System.arraycopy(zvalues, zind1, result, 0, nz);
	    return result;
	} finally {
	    releasePair(pair);
	}
    }

    /**
     * Get the Z values for all vertices associated with an
     * ordered pair of indices <code>(i, j)</code> for the upper and lower
     * grids.
     * <P>
     * When adjacent components have different heights, a set of vertical
     * rectangles will be added to this object's model. This method returns
     * the Z values of each vertex along the vertical line connecting
     * the components with a vertex at the point corresponding to the index
     * <code>(i, j)</code>.
     * <P>
     * If there are no filled components adjacent to the vertex associated
     * with the indices <code>(i,j)</code>, a zero-length array will be
     * returned.
     * @param i the X index for the vertex
     * @param j the Y index for the vertex
     * @return the Z values
     */
    public double[] getZs(int i, int j) {
	if (lower == null) return getZs(i, j, true);
	if (upper == null) return getZs(i, j, false);

	double[] pair1 = getZPair(i, j, false);
	double[] pair2 = getZPair(i, j, true);
	double max;
	double min;
	if (pair1 == emptyArray) {
	    if (pair2 == emptyArray) return emptyArray;
	    min = pair2[0];
	    max = pair2[1];
	    releasePair(pair2);
	} else if (pair2 == emptyArray) {
	    min = pair1[0];
	    max = pair1[1];
	    releasePair(pair1);
	} else {
	    min = pair1[0];
	    max = pair2[1];
	    releasePairs(pair1, pair2);
	}
	int zind1 = Arrays.binarySearch(zvalues, min);
	int zind2 = Arrays.binarySearch(zvalues, max);
	int nz = zind2 - zind1 + 1;
	double[] result = new double[nz];
	System.arraycopy(zvalues, zind1, result, 0, nz);
	return result;
    }

    private static class OurTriangle implements Model3DOps.Triangle {
	final double[] coords = new double[9];
	Color color = null;
	Object tag = null;
	@Override
	public double getX1() {return coords[0];}
	@Override
	public double getY1() {return coords[1];}
	@Override
	public double getZ1() {return coords[2];}
	@Override
	public double getX2() {return coords[6];}
	@Override
	public double getY2() {return coords[7];}
	@Override
	public double getZ2() {return coords[8];}
	@Override
	public double getX3() {return coords[3];}
	@Override
	public double getY3() {return coords[4];}
	@Override
	public double getZ3() {return coords[5];}
	@Override
	public Object getTag() {return tag;}
	@Override
	public Color getColor() {return color;}
    }


    // Stripped-down implementation
    private static class OurM3D implements Model3DOps<OurTriangle>  {
	LinkedList<OurTriangle> triangles = new LinkedList<>();

	// never used
	public int numberOfComponents() {
	    throw new UnsupportedOperationException();
	}

	// never used
	public Shape3D getComponent(int i) {
	    throw new UnsupportedOperationException();
	}

	public void addModel(Model3DOps<?> m3d) {}
	public void addModel(Model3DOps<?> m3d, Object tag) {}

	int tcount = 0;

	public OurTriangle addTriangle(Model3DOps.Triangle triangle) {
	    if (triangle instanceof OurTriangle) {
		return addTriangle((OurTriangle) triangle);
	    } else {
		return addTriangle
		    (triangle.getX1(), triangle.getY1(), triangle.getZ1(),
		     triangle.getX2(), triangle.getY2(), triangle.getZ2(),
		     triangle.getX3(), triangle.getY3(), triangle.getZ3(),
		     triangle.getColor(), triangle.getTag());
	    }
	}


	public OurTriangle addTriangle(OurTriangle triangle) {
	    tcount++;
	    triangles.add(triangle);
	    return triangle;
	}

	public OurTriangle addTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3)
	{
	    OurTriangle triangle = new OurTriangle();
	    triangle.coords[0] = x1;
	    triangle.coords[1] = y1;
	    triangle.coords[2] = z1;
	    triangle.coords[3] = x3;
	    triangle.coords[4] = y3;
	    triangle.coords[5] = z3;
	    triangle.coords[6] = x2;
	    triangle.coords[7] = y2;
	    triangle.coords[8] = z2;
	    return addTriangle(triangle);
	}

	public OurTriangle addFlippedTriangle(double x1, double y1, double z1,
					      double x2, double y2, double z2,
					      double x3, double y3, double z3)
	{
	    return addTriangle(x1, y1, z1, x3, y3, z3, x2, y2, z2);
	}

	public OurTriangle addTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3,
				       Color color)
	{
	    OurTriangle triangle = addTriangle(x1, y1, z1,
					       x2, y2, z2,
					       x3, y3, z3);
	    triangle.color = color;
	    return triangle;
	}

	public OurTriangle addFlippedTriangle(double x1, double y1, double z1,
					      double x2, double y2, double z2,
					      double x3, double y3, double z3,
					      Color color)
	{
	    OurTriangle triangle = addFlippedTriangle(x1, y1, z1,
						      x2, y2, z2,
						      x3, y3, z3);
	    triangle.color = color;
	    return triangle;
	}

	public OurTriangle addTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3,
				       Color color, Object tag)
	{
	    OurTriangle triangle =
		addTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
	    triangle.tag = tag;
	    return triangle;

	}

	public OurTriangle addFlippedTriangle(double x1, double y1, double z1,
					      double x2, double y2, double z2,
					      double x3, double y3, double z3,
					      Color color, Object tag)
	{
	    OurTriangle triangle =
		addFlippedTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
	    triangle.tag = tag;
	    return triangle;
	}

	private class OurSurfaceIterator implements SurfaceIterator {
	    Transform3D tf;

	    public OurSurfaceIterator(Transform3D tform) {
		tf = tform;
		// System.out.println("triangles.size() = " + triangles.size());
	    }

	    Iterator<OurTriangle> it = triangles.iterator();
	    OurTriangle current = it.hasNext()? it.next(): null;

	    public int currentSegment(double[] coords) {
		if (current != null) {
		    if (tf == null) {
			System.arraycopy(current.coords, 0, coords, 0, 9);
		    } else {
			tf.transform(current.coords, 0, coords, 0, 3);
		    }
		    return SurfaceIterator.PLANAR_TRIANGLE;
		} else {
		    return -1;
		}
	    }

	    public int currentSegment(float[] coords){
		if (current != null) {
		    if (tf == null) {
			for (int i = 0; i < 9; i++) {
			    coords[i] = (float)(current.coords[i]);
			}
		    } else {
			tf.transform(current.coords, 0, coords, 0, 3);
		    }
		    return SurfaceIterator.PLANAR_TRIANGLE;
		} else {
		    return -1;
		}
	    }


	    public boolean isDone() {return (current == null);}

	    public void next() {
		if (it.hasNext()) {
			current = it.next();
		} else {
		    current = null;
		}
	    }

	    // not used
	    public Color currentColor() {return current.getColor();}
	    public Object currentTag() {return current.getTag();}
	    public boolean isOriented() {return true;}
	}

	public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	    return new OurSurfaceIterator(tform);
	}

	public SurfaceIterator getSurfaceIterator(Transform3D tform, int limit)
	{
	    if (tform instanceof AffineTransform3D) {
		return new SubdivisionIterator(getSurfaceIterator(tform),
					       limit);
	    } else {
		return new SubdivisionIterator(getSurfaceIterator(null),
					       tform, limit);
	    }
	}

	boolean isClosedM = false;
	Path3D bpath = null;

	public void addsCompleted() {
	    // System.out.println("tcount = " + tcount);
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	}

	public Path3D getBoundary() {
	    return bpath;
	}

	public boolean isClosedManifold() {
	    return (bpath != null) && bpath.isEmpty();
	}

	// not used
	public Rectangle3D getBounds() {return null;}
	public boolean isOriented() {return true;}
    }

    private static class SteppedGridLayer {
	double[] xs;
	double[] ys;
	double zbase;
	LayerComponent[][] rectangles;
	LayerComponent[][] halfRectangles;
	ArrayList<LayerComponent> componentList = new ArrayList<>();
	ArrayList<LayerComponent> halfList = new ArrayList<>();
	Set<Double> zset;

	private boolean updated = false;
	boolean flipped = false;

	public boolean isFlipped() {return flipped;}

	private static class LayerComponent {
	    int i; int j;
	    boolean filled;
	    double height;
	    boolean placeholder = false;
	    public LayerComponent(int i, int j, double height,
			     boolean placeholder) {
		this(i, j, height);
		this.placeholder = placeholder;
	    }
	    public LayerComponent(int i, int j, double height) {
		this.i = i; this.j = j;
		filled = true;
		this.height = height;
	    }
	    LayerComponent(int i, int j) {
		this.i = i; this.j = j;
		filled = false;
	    }
	    // fields for half components, set in update()
	    boolean half = false;
	    int corner = 0;
	}

	public SteppedGridLayer(double[] xs, double[] ys, double z,
				boolean flipped) {
	    this.xs = xs;
	    this.ys = ys;
	    this.flipped = flipped;
	    this.zbase = z;
	    zset = new HashSet<Double>(xs.length + ys.length);
	}
    
	public void addHalfComponent(LayerComponent component) {
	    updated = false;
	    halfList.add(component);
	    zset.add(zbase + component.height);
	}

	public void addComponent(LayerComponent component) {
	    updated = false;
	    componentList.add(component);
	    zset.add(zbase + component.height);
	}

	public void update() throws IllegalStateException {
	    if (updated) return;
	    if (rectangles == null) {
		rectangles = new LayerComponent[xs.length - 1][ys.length - 1];
	    }
	    if (halfRectangles == null) {
		if (!halfList.isEmpty()) {
		    halfRectangles =
			new LayerComponent[xs.length - 1][ys.length - 1];
		}
	    }
	    for (LayerComponent c: componentList) {
		rectangles[c.i][c.j] = c;
	    }
	    componentList.clear();
	    int n = xs.length-1;
	    int m = ys.length-1;
	    int nm1 = n - 1;
	    int mm1 = m - 1;
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    if (rectangles[i][j] == null) {
			rectangles[i][j] = new LayerComponent(i, j);
		    }
		}
	    }
	    if (halfRectangles != null) {
		for (LayerComponent c: halfList) {
		    int i = c.i;
		    int j = c.j;
		    c.half = true;
		    halfRectangles[c.i][c.j] = c;
		    int im1 = i - 1;
		    int ip1 = i + 1;
		    int jm1 = j - 1;
		    int jp1 = j + 1;
		    int count = 0;
		    int corner = 0;
		    if ((i == 0 && j == 0)
			|| (i == 0 && j > 0
			    && (!rectangles[0][jm1].filled
				|| rectangles[0][jm1].placeholder))
			|| (j == 0 && i > 0
			    && (!rectangles[im1][0].filled
				|| rectangles[im1][0].placeholder))
			|| (i > 0
			    && j > 0
			    &&((!rectangles[i][jm1].filled
				|| rectangles[i][jm1].placeholder)
			       && (!rectangles[im1][j].filled
				   || rectangles[im1][j].placeholder)))) {
			count++;
			corner = 0;
		    }
		    if ((i == nm1 && j == 0)
			|| (i == nm1 && j > 0
			    && (!rectangles[i][jm1].filled
				|| rectangles[i][jm1].placeholder))
			|| (j == 0 && i < nm1
			    && (!rectangles[ip1][0].filled
				|| rectangles[ip1][0].placeholder))
			|| (i < nm1 && j > 0
			    && (((!rectangles[ip1][j].filled
				  || rectangles[ip1][j].placeholder)
				 && (!rectangles[i][jm1].filled
				     || rectangles[i][jm1].placeholder))))) {
			count++;
			corner = 1;
		    }
		    if ((i == nm1 && j == mm1)
			|| (i == nm1 && j < mm1
			    && (!rectangles[i][jp1].filled
				|| rectangles[i][jp1].placeholder))
			|| (j == mm1 && i < nm1
			    && (!rectangles[ip1][j].filled
				|| rectangles[ip1][j].placeholder))
			|| (i < nm1 && j < mm1
			    && ((!rectangles[ip1][j].filled
				 || rectangles[ip1][j].placeholder)
				&& (!rectangles[i][jp1].filled
				    || rectangles[i][jp1].placeholder)))) {
			count++;
			corner = 2;
		    }
		    if ((i == 0 && j == mm1)
			|| (i == 0 && j < mm1
			    && (!rectangles[0][jp1].filled
				|| rectangles[0][jp1].placeholder))
			|| (j == mm1 && i > 0
			    && (!rectangles[im1][j].filled
				|| rectangles[im1][j].placeholder))
			|| (j < mm1 && i > 0
			    && ((!rectangles[i][jp1].filled
				 || rectangles[i][jp1].placeholder)
				&& (!rectangles[im1][j].filled
				    || rectangles[im1][j].placeholder)))) {
			count++;
			corner = 3;
		    }
		    if (count == 1) {
			c.corner = corner;
		    } else {
			String msg = (errorMsg("SGILGCount", count, i, j));
			throw new IllegalStateException(msg);
		    }
		    // Check consistency
		    switch (corner) {
		    case 0:
			if (j == m) {
			    String msg = errorMsg("SGILG", 0, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[i][jp1].filled == false
			   || rectangles[i][jp1].placeholder) {
			    String msg = errorMsg("SGILG", 0, i, j);
			    throw new IllegalStateException(msg);
			} else if (i == n) {
			    String msg = errorMsg("SGILG", 0, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[ip1][j].filled == false
			    || rectangles[ip1][j].placeholder) {
			    String msg = errorMsg("SGILG", 0, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[ip1][jp1].filled == false
				   || rectangles[ip1][jp1].placeholder) {
			    String msg = errorMsg("SGILG", 0, i, j);
			    throw new IllegalStateException(msg);
			}
			break;
		    case 1:
			if (i == 0) {
			    String msg = errorMsg("SGILG", 1, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[im1][j].filled == false
				   || rectangles[im1][j].placeholder) {
			    String msg = errorMsg("SGILG", 1, i, j);
			    throw new IllegalStateException(msg);
			} else if (j == m) {
			    String msg = errorMsg("SGILG", 1, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[i][jp1].filled == false
				   || rectangles[i][jp1].placeholder) {
			    String msg = errorMsg("SGILG", 1, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[im1][jp1].filled == false
				   || rectangles[im1][jp1].placeholder) {
			    String msg = errorMsg("SGILG", 1, i, j);
			    throw new IllegalStateException(msg);
			}
			break;
		    case 2:
			if (i == 0) {
			    String msg = errorMsg("SGILG", 2, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[im1][j].filled == false
			    || rectangles[im1][j].placeholder) {
			    String msg = errorMsg("SGILG", 2, i, j);
			    throw new IllegalStateException(msg);
			} else if (j == 0) {
			    String msg = errorMsg("SGILG", 2, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[i][jm1].filled == false
			    || rectangles[i][jm1].placeholder) {
			    String msg = errorMsg("SGILG", 2, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[im1][jm1].filled == false
			    || rectangles[im1][jm1].placeholder) {
			    String msg = errorMsg("SGILG", 2, i, j);
			    throw new IllegalStateException(msg);
			}
			break;
		    case 3:
			if (i == n) {
			    String msg = errorMsg("SGILG", 3, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[ip1][j].filled == false
				   || rectangles[ip1][j].placeholder) {
			    String msg = errorMsg("SGILG", 3, i, j);
			    throw new IllegalStateException(msg);
			} else if (j == 0) {
			    String msg = errorMsg("SGILG", 3, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[i][jm1].filled == false
				   || rectangles[i][jm1].placeholder) {
			    String msg = errorMsg("SGILG", 3, i, j);
			    throw new IllegalStateException(msg);
			} else if (rectangles[ip1][jm1].filled == false
				   || rectangles[ip1][jm1].placeholder) {
			    String msg = errorMsg("SGILG", 3, i, j);
			    throw new IllegalStateException(msg);
			}
			break;
		    }
		}
	    }
	    if (halfRectangles != null) {
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < m; j++) {
			if (halfRectangles[i][j] == null) {
			    halfRectangles[i][j] = new LayerComponent(i, j);
			}
		    }
		}
	    }
	    halfList.clear();
	    updated = true;
	}

	public void addRectangles(Model3DOps<?> m3d, Model3DOps<?> fm3d) {
	    update();
	    int n = xs.length - 1;
	    int m = ys.length - 1;
	    int nm1 = n - 1;
	    int mm1 = m - 1;

	    if (flipped) {
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < m; j++) {
			LayerComponent c = rectangles[i][j];
			if (c.filled == false) {
			    continue;
			}
			if (halfRectangles != null) {
			    LayerComponent ch = halfRectangles[i][j];
			    if (ch.filled && ch.half) {
				switch (ch.corner) {
				case 0:
				    m3d.addFlippedTriangle(xs[i], ys[j+1],
							   zbase+ch.height,
							   xs[i+1], ys[j],
							   zbase+ch.height,
							   xs[i+1], ys[j+1],
							   zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addFlippedTriangle(xs[i], ys[j+1],
								zbase,
								xs[i+1], ys[j],
								zbase,
								xs[i+1],ys[j+1],
								zbase);
				    }
				    break;
				case 1:
				    m3d.addFlippedTriangle(xs[i], ys[j],
							   zbase+ch.height,
							   xs[i+1], ys[j+1],
							   zbase+ch.height,
							   xs[i], ys[j+1],
							   zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addFlippedTriangle(xs[i], ys[j],
								zbase,
								xs[i+1],ys[j+1],
								zbase,
								xs[i], ys[j+1],
								zbase);
				    }
				    break;
				case 2:
				    m3d.addFlippedTriangle(xs[i], ys[j],
							   zbase+ch.height,
							   xs[i+1], ys[j],
							   zbase+ch.height,
							   xs[i], ys[j+1],
							   zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addFlippedTriangle(xs[i], ys[j],
								zbase,
								xs[i+1], ys[j],
								zbase,
								xs[i], ys[j+1],
								zbase);
				    }
				    break;
				case 3:
				    m3d.addFlippedTriangle(xs[i], ys[j],
							   zbase+ch.height,
							   xs[i+1], ys[j],
							   zbase+ch.height,
							   xs[i+1], ys[j+1],
							   zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addFlippedTriangle(xs[i], ys[j],
								zbase,
								xs[i+1], ys[j],
								zbase,
								xs[i+1],ys[j+1],
								zbase);
				    }
				    break;
				default:
				    throw new UnexpectedExceptionError();
				}
				continue;
			    }
			}
			if (c.placeholder == false) {
			    P3d.Rectangle.addFlippedH(m3d, zbase + c.height,
						      xs[i], ys[j],
						      xs[i+1], ys[j+1]);
			    if (fm3d != null) {
				P3d.Rectangle.addFlippedH(fm3d, zbase,
							  xs[i], ys[j],
							  xs[i+1], ys[j+1]);
			    }
			}
		    }
		}
	    } else {
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < m; j++) {
			LayerComponent c = rectangles[i][j];
			if (c.filled == false) {
			    continue;
			}
			if (halfRectangles != null) {
			    LayerComponent ch = halfRectangles[i][j];
			    if (ch.filled == true && ch.half) {
				switch (ch.corner) {
				case 0:
				    m3d.addTriangle(xs[i], ys[j+1],
						    zbase+ch.height,
						    xs[i+1], ys[j],
						    zbase+ch.height,
						    xs[i+1], ys[j+1],
						    zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addTriangle(xs[i], ys[j+1],
							 zbase,
							 xs[i+1], ys[j],
							 zbase,
							 xs[i+1], ys[j+1],
							 zbase);
				    }
				    break;
				case 1:
				    m3d.addTriangle(xs[i], ys[j],
						    zbase+ch.height,
						    xs[i+1], ys[j+1],
						    zbase+ch.height,
						    xs[i], ys[j+1],
						    zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addTriangle(xs[i], ys[j],
							 zbase,
							 xs[i+1], ys[j+1],
							 zbase,
							 xs[i], ys[j+1],
							 zbase);
				    }
				    break;
				case 2:
				    m3d.addTriangle(xs[i], ys[j],
						    zbase+ch.height,
						    xs[i+1], ys[j],
						    zbase+ch.height,
						    xs[i], ys[j+1],
						    zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addTriangle(xs[i], ys[j],
							 zbase,
							 xs[i+1], ys[j],
							 zbase,
							 xs[i], ys[j+1],
							 zbase);
				    }
				    break;
				case 3:
				    m3d.addTriangle(xs[i], ys[j],
						    zbase+ch.height,
						    xs[i+1], ys[j],
						    zbase+ch.height,
						    xs[i+1], ys[j+1],
						    zbase+ch.height);
				    if (fm3d != null) {
					fm3d.addTriangle(xs[i], ys[j],
							 zbase,
							 xs[i+1], ys[j],
							 zbase,
							 xs[i+1], ys[j+1],
							 zbase);
				    }
				    break;
				default:
				    throw new UnexpectedExceptionError();
				}
				continue;
			    }
			}
			if (c.placeholder == false) {
			    P3d.Rectangle.addH(m3d, zbase + c.height,
					       xs[i], ys[j],
					       xs[i+1], ys[j+1]);
			    if (fm3d != null) {
				P3d.Rectangle.addH(fm3d, zbase,
						   xs[i], ys[j],
						   xs[i+1], ys[j+1]);
			    }
			}
		    }
		}
	    }
	}

	static class Pair {
	    LayerComponent c;
	    LayerComponent c2;
	    int edge;
	    Pair(LayerComponent c, LayerComponent c2, int edge) {
		this.c = c;
		this.c2 = c2;
		this.edge = edge;
	    }
	}

	public static double[] attach(Model3DOps<?> m3d, SteppedGridLayer r,
				      double maxZSpacing)
	{
	    r.update();
	    int n = r.xs.length-1;
	    int m = r.ys.length-1;
	    long max = ((long)n)*((long)m)*4;
	    int nm1 = n - 1;
	    int mm1 = m - 1;
	    Set<Double> zzset = new TreeSet<Double>();
	    // Set<Double> zzset = new HashSet<>(r.zset.size());
	    zzset.addAll(r.zset);
	    if (maxZSpacing > 0.0) {
		double lastZ = 0.0;
		boolean firstTime = true;
		LinkedList<Double> additions = new LinkedList<>();
		for (double z: zzset) {
		    if (firstTime) {
			firstTime = false;
		    } else {
			double diff = z - lastZ;
			if (diff > maxZSpacing) {
			    double zz = lastZ + maxZSpacing;
			    while (zz < z) {
				additions.add(zz);
				zz += maxZSpacing;
			    }
			}
		    }
		    lastZ = z;
		}
		zzset.addAll(additions);
	    }
	    double[] zvalues = new double[zzset.size()];
	    int ind = 0;
	    for (double z: zzset) {
		zvalues[ind++] = z;
	    }
	    // Arrays.sort(zvalues);
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    LayerComponent c = r.rectangles[i][j];

		    LayerComponent cright = (i == nm1)? null:
			r.rectangles[i+1][j];
		    LayerComponent cupper = (j == mm1)? null:
			r.rectangles[i][j+1];
		    LayerComponent clower = (j == 0)? null:
			r.rectangles[i][j-1];
		    LayerComponent cleft = (i == 0)? null:
			r.rectangles[i-1][j];

		    double x1, x2;
		    double y1, y2;
		    double z1, z2;
		    int zind1, zind2, incr;

		    if (c.filled) {
			if (cright != null && cright.filled) {
			    boolean ok = c.height != cright.height;
			    if (ok) {
				LayerComponent cH =
				    (r.halfRectangles == null)? null:
				    r.halfRectangles[i][j];
				LayerComponent crightH =
				    (r.halfRectangles == null)? null:
				    r.halfRectangles[i+1][j];
				if (cH != null && !cH.filled) cH = null;
				if (crightH != null && !crightH.filled)
				    crightH = null;
				if (cH == null && crightH == null) {
				    ok = !c.placeholder || !cright.placeholder;
				} else if (cH != null && crightH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 3) {
					ok = !cright.placeholder;
				    }
				} else if (crightH != null) {
				    if (crightH.corner == 1
					|| crightH.corner == 2) {
					ok = !c.placeholder;
				    }
				}
			    }
			    if (ok) {
				x1 = r.xs[i+1];
				y1 = r.ys[j];
				x2 = r.xs[i+1];
				y2 = r.ys[j+1];
				z1 = r.zbase + c.height;
				z2 = r.zbase + cright.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    if (r.flipped) {
					P3d.Rectangle.addV(m3d,
							   x1, y1, zvalues[ind],
							   x2, y2,
							   zvalues[ind+incr]);
				    } else {
					P3d.Rectangle.addFlippedV
					    (m3d, x1, y1, zvalues[ind],
					     x2, y2, zvalues[ind+incr]);
				    }
				}
			    }
			}
			if (cupper != null && cupper.filled) {
			    boolean ok = c.height != cupper.height;
			    if (ok) {
				LayerComponent cH =
				    (r.halfRectangles == null)? null:
				    r.halfRectangles[i][j];
				LayerComponent cupperH =
				    (r.halfRectangles == null)? null:
				    r.halfRectangles[i][j+1];
				if (cH != null && !cH.filled) cH = null;
				if (cupperH != null && !cupperH.filled)
				    cupperH = null;
				if (cH == null && cupperH == null) {
				    ok = !c.placeholder || !cupper.placeholder;
				} else if (cH != null && cupperH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 1) {
					ok = !cupper.placeholder;
				    }
				} else if (cupperH != null) {
				    if (cupperH.corner == 2
					|| cupperH.corner == 3) {
					ok = !c.placeholder;
				    }
				}
			    }
			    if (ok) {
				x1 = r.xs[i];
				y1 = r.ys[j+1];
				x2 = r.xs[i+1];
				y2 = r.ys[j+1];
				z1 = r.zbase + c.height;
				z2 = r.zbase + cupper.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    if (r.flipped) {
					P3d.Rectangle.addFlippedV
					    (m3d, x1, y1, zvalues[ind],
					     x2, y2, zvalues[ind+incr]);
				    } else {
					P3d.Rectangle.addV(m3d,
							   x1, y1, zvalues[ind],
							   x2, y2,
							   zvalues[ind+incr]);
				    }
				}
			    }
			}
		    }
		}
	    }
	    return zvalues;
	}

	public static double[] attach(Model3DOps<?> m3d, Model3DOps<?> fm3d,
				      SteppedGridLayer r1, SteppedGridLayer r2,
				      double maxZSpacing)
	{
	    r1.update();
	    r2.update();

	    int n = r1.xs.length-1;
	    int m = r2.ys.length-1;
	    long max = ((long)n)*((long)m)*4;
	    int nm1 = n - 1;
	    int mm1 = m - 1;

	    // Set<Double> zzset=new HashSet<>(r1.zset.size() + r2.zset.size());
	    Set<Double> zzset = new TreeSet<Double>();
	    zzset.addAll(r1.zset);
	    zzset.addAll(r2.zset);
	    if (maxZSpacing > 0.0) {
		double lastZ = 0.0;
		boolean firstTime = true;
		LinkedList<Double> additions = new LinkedList<>();
		for (double z: zzset) {
		    if (firstTime) {
			firstTime = false;
		    } else {
			double diff = z - lastZ;
			if (diff > maxZSpacing) {
			    double zz = lastZ + maxZSpacing;
			    while (zz < z) {
				additions.add(zz);
				zz += maxZSpacing;
			    }
			}
		    }
		    lastZ = z;
		}
		zzset.addAll(additions);
	    }
	    double[] zvalues = new double[zzset.size()];
	    int ind = 0;
	    for (double z: zzset) {
		zvalues[ind++] = z;
	    }
	    // Arrays.sort(zvalues);
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    LayerComponent c1 = r1.rectangles[i][j];
		    LayerComponent c2 = r2.rectangles[i][j];

		    LayerComponent cH1 = null;
		    LayerComponent cH1right = null;
		    LayerComponent cH1upper = null;
		    LayerComponent cH1lower = null;
		    LayerComponent cH1left = null;
		    if (r1.halfRectangles != null) {
			cH1 = r1.halfRectangles[i][j];
			cH1right = (i == nm1)? null:
			    r1.halfRectangles[i+1][j];
			cH1upper = (j == mm1)? null:
			    r1.halfRectangles[i][j+1];
			cH1lower = (j == 0)? null:
			    r1.halfRectangles[i][j-1];
			cH1left = (i == 0)? null:
			    r1.halfRectangles[i-1][j];;
		    }
		    LayerComponent cH2 = null;
		    LayerComponent cH2right = null;
		    LayerComponent cH2upper = null;
		    if (r2.halfRectangles != null) {
			cH2 = r2.halfRectangles[i][j];
			cH2right = (i == nm1)? null:
			    r2.halfRectangles[i+1][j];
			cH2upper = (j == mm1)? null:
			    r2.halfRectangles[i][j+1];
		    }

		    LayerComponent c1right = (i == nm1)? null:
			r1.rectangles[i+1][j];
		    LayerComponent c1upper = (j == mm1)? null:
			r1.rectangles[i][j+1];
		    LayerComponent c1lower = (j == 0)? null:
			r1.rectangles[i][j-1];
		    LayerComponent c1left = (i == 0)? null:
			r1.rectangles[i-1][j];

		    LayerComponent c2right = (i == nm1)? null:
			r2.rectangles[i+1][j];
		    LayerComponent c2upper = (j == mm1)? null:
			r2.rectangles[i][j+1];

		    double x1, x2;
		    double y1, y2;
		    double z1, z2;
		    int zind1, zind2, incr;

		    boolean test1 = cH1 == null || !cH1.filled
			|| cH1.placeholder ||cH2 == null || !cH2.filled
			|| cH2.placeholder;

		    if (c1.filled) {
			if ((c1left == null || !c1left.filled) && test1) {
			    x1 = r1.xs[i];
			    y1 = r1.ys[j];
			    x2 = r1.xs[i];
			    y2 = r1.ys[j+1];
			    z1 = r1.zbase + c1.height;
			    z2 = r2.zbase + c2.height;
			    zind1 = Arrays.binarySearch(zvalues, z1);
			    zind2 = Arrays.binarySearch(zvalues, z2);
			    incr = (zind1 > zind2)? -1: 1;
			    for (ind = zind1; ind != zind2; ind += incr) {
				P3d.Rectangle.addV(m3d,
						   x1, y1, zvalues[ind],
						   x2, y2,
						   zvalues[ind+incr]);
			    }
			    if (fm3d != null) {
				P3d.Rectangle.addV(fm3d,
						   x1, y1, r1.zbase,
						   x2, y2,
						   r2.zbase);
			    }

			}
			if (c1right != null && c1right.filled) {
			    boolean ok = c1.height != c1right.height;
			    if (ok) {
				LayerComponent cH = cH1;
				LayerComponent crightH =
				    (r1.halfRectangles == null)? null:
				    r1.halfRectangles[i+1][j];
				if (cH != null && !cH.filled) cH = null;
				if (crightH != null && !crightH.filled)
				    crightH = null;
				if (cH == null && crightH == null) {
				    ok = !c1.placeholder
					|| !c1right.placeholder;
				} else if (cH != null && crightH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 3) {
					ok = !c1right.placeholder;
				    } else {
					ok = false;
				    }
				} else if (crightH != null) {
				    if (crightH.corner == 1
					|| crightH.corner == 2) {
					ok = !c1.placeholder;
				    } else {
					ok = false;
				    }
				}
			    }
			    if (ok) {
				x1 = r1.xs[i+1];
				y1 = r1.ys[j];
				x2 = r1.xs[i+1];
				y2 = r1.ys[j+1];
				z1 = r1.zbase + c1.height;
				z2 = r1.zbase + c1right.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    P3d.Rectangle.addFlippedV(m3d,
							      x1, y1, zvalues[ind],
							      x2, y2,
							      zvalues[ind+incr]);
				}
			    }
			} else if (test1)  {
			    x1 = r1.xs[i+1];
			    y1 = r1.ys[j];
			    x2 = r1.xs[i+1];
			    y2 = r1.ys[j+1];
			    z1 = r1.zbase + c1.height;
			    z2 = r2.zbase + c2.height;
			    zind1 = Arrays.binarySearch(zvalues, z1);
			    zind2 = Arrays.binarySearch(zvalues, z2);
			    incr = (zind1 > zind2)? -1: 1;
			    for (ind = zind1; ind != zind2; ind += incr) {
				P3d.Rectangle.addFlippedV(m3d,
							  x1, y1, zvalues[ind],
							  x2, y2,
							  zvalues[ind+incr]);
			    }
			    if (fm3d != null) {
				P3d.Rectangle.addFlippedV(fm3d,
							  x1, y1, r1.zbase,
							  x2, y2, r2.zbase);
			    }
			}
			if ((c1lower == null || !c1lower.filled) && test1) {
			    x1 = r1.xs[i];
			    y1 = r1.ys[j];
			    x2 = r1.xs[i+1];
			    y2 = r1.ys[j];
			    z1 = r1.zbase + c1.height;
			    z2 = r2.zbase + c2.height;
			    zind1 = Arrays.binarySearch(zvalues, z1);
			    zind2 = Arrays.binarySearch(zvalues, z2);
			    incr = (zind1 > zind2)? -1: 1;
			    for (ind = zind1; ind != zind2; ind += incr) {
				P3d.Rectangle.addFlippedV(m3d,
							  x1, y1, zvalues[ind],
							  x2, y2,
							  zvalues[ind+incr]);
			    }
			    if (fm3d != null) {
				P3d.Rectangle.addFlippedV(fm3d,
							  x1, y1, r1.zbase,
							  x2, y2, r2.zbase);
			    }
			}
			if (c1upper != null && c1upper.filled) {
			    boolean ok = c1.height != c1upper.height;
			    if (ok) {
				LayerComponent cH = cH1;
				LayerComponent cupperH =
				    (r1.halfRectangles == null)? null:
				    r1.halfRectangles[i][j+1];
				if (cH != null && !cH.filled) cH = null;
				if (cupperH != null && !cupperH.filled)
				    cupperH = null;
				if (cH == null && cupperH == null) {
				    ok = !c1.placeholder
					|| !c1upper.placeholder;
				} else if (cH != null && cupperH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 1) {
					ok = !c1upper.placeholder;
				    } else {
					ok = false;
				    }
				} else if (cupperH != null) {
				    if (cupperH.corner == 2
					|| cupperH.corner == 3) {
					ok = !c1.placeholder;
				    } else {
					ok = false;
				    }
				}
			    }
			    if (ok) {
				x1 = r1.xs[i];
				y1 = r1.ys[j+1];
				x2 = r1.xs[i+1];
				y2 = r1.ys[j+1];
				z1 = r1.zbase + c1.height;
				z2 = r1.zbase + c1upper.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    P3d.Rectangle.addV(m3d,
						       x1, y1, zvalues[ind],
						       x2, y2,
						       zvalues[ind+incr]);
				}
			    }
			} else if (test1)  {
			    x1 = r1.xs[i];
			    y1 = r1.ys[j+1];
			    x2 = r1.xs[i+1];
			    y2 = r1.ys[j+1];
			    z1 = r1.zbase + c1.height;
			    z2 = r2.zbase + c2.height;
			    zind1 = Arrays.binarySearch(zvalues, z1);
			    zind2 = Arrays.binarySearch(zvalues, z2);
			    incr = (zind1 > zind2)? -1: 1;
			    for (ind = zind1; ind != zind2; ind += incr) {
				P3d.Rectangle.addV(m3d,
						   x1, y1, zvalues[ind],
						   x2, y2,
						   zvalues[ind+incr]);
			    }
			    if (fm3d != null) {
				P3d.Rectangle.addV(fm3d,
						   x1, y1, r1.zbase,
						   x2, y2, r2.zbase);
			    }
			}
		    }
		    if (c2.filled) {
			if (c2right != null && c2right.filled) {
			    boolean ok = c2.height != c2right.height;
			    if (ok) {
				LayerComponent cH = cH2;
				LayerComponent crightH =
				    (r2.halfRectangles == null)? null:
				    r2.halfRectangles[i+1][j];
				if (cH != null && !cH.filled) cH = null;
				if (crightH != null && !crightH.filled)
				    crightH = null;
				if (cH == null && crightH == null) {
				    ok = !c2.placeholder
					|| !c2right.placeholder;
				} else if (cH != null && crightH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 3) {
					ok = !c2right.placeholder;
				    } else {
					ok = false;
				    }
				} else if (crightH != null) {
				    if (crightH.corner == 1
					|| crightH.corner == 2) {
					ok = !c2.placeholder;
				    } else {
					ok = false;
				    }
				}
			    }
			    if (ok) {
				x1 = r2.xs[i+1];
				y1 = r2.ys[j];
				x2 = r2.xs[i+1];
				y2 = r2.ys[j+1];
				z1 = r2.zbase + c2.height;
				z2 = r2.zbase + c2right.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    P3d.Rectangle.addV(m3d,
						       x1, y1, zvalues[ind],
						       x2, y2,
						       zvalues[ind+incr]);
				}
			    }
			}
			if (c2upper != null && c2upper.filled) {
			    boolean ok = c2.height != c2upper.height;
			    if (ok) {
				LayerComponent cH = cH2;
				LayerComponent cupperH =
				    (r2.halfRectangles == null)? null:
				    r2.halfRectangles[i][j+1];
				if (cH != null && !cH.filled) cH = null;
				if (cupperH != null && !cupperH.filled)
				    cupperH = null;
				if (cH == null && cupperH == null) {
				    ok = !c2.placeholder
					|| !c2upper.placeholder;
				} else if (cH != null && cupperH != null) {
				    ok = false;
				} else if (cH != null) {
				    if (cH.corner == 0 || cH.corner == 1) {
					ok = !c2upper.placeholder;
				    }
				} else if (cupperH != null) {
				    if (cupperH.corner == 2
					|| cupperH.corner == 3) {
					ok = !c2.placeholder;
				    }
				}
			    }
			    if (ok) {
				x1 = r2.xs[i];
				y1 = r2.ys[j+1];
				x2 = r2.xs[i+1];
				y2 = r2.ys[j+1];
				z1 = r2.zbase + c2.height;
				z2 = r2.zbase + c2upper.height;
				zind1 = Arrays.binarySearch(zvalues, z1);
				zind2 = Arrays.binarySearch(zvalues, z2);
				incr = (zind1 > zind2)? -1: 1;
				for (ind = zind1; ind != zind2; ind += incr) {
				    P3d.Rectangle.addFlippedV
					(m3d,
					 x1, y1, zvalues[ind],
					 x2, y2,
					 zvalues[ind+incr]);
				}
			    }
			}
		    }
		    if (cH1 != null && cH2 != null
			&& cH1.filled && cH2.filled
			&& !cH1.placeholder && !cH2.placeholder) {
			z1 = r1.zbase + c1.height;
			z2 = r2.zbase + c2.height;
			zind1 = Arrays.binarySearch(zvalues, z1);
			zind2 = Arrays.binarySearch(zvalues, z2);
			incr = (zind1 > zind2)? -1: 1;
			switch (cH1.corner) {
			case 0:
			    x1 = r1.xs[i];
			    y1 = r1.ys[j+1];
			    x2 = r1.xs[i+1];
			    y2 = r1.ys[j];
			    break;
			case 1:
			    x1 = r1.xs[i];
			    y1 = r1.ys[j];
			    x2 = r1.xs[i+1];
			    y2 = r1.ys[j+1];
			    break;
			case 2:
			    x1 = r1.xs[i+1];
			    y1 = r1.ys[j];
			    x2 = r1.xs[i];
			    y2 = r1.ys[j+1];
			    break;
			case 3:
			    x1 = r1.xs[i+1];
			    y1 = r1.ys[j+1];
			    x2 = r1.xs[i];
			    y2 = r1.ys[j];
			    break;
			default:
			    throw new UnexpectedExceptionError();
			}
			for (ind = zind1; ind != zind2; ind += incr) {
			    P3d.Rectangle.addFlippedV(m3d,
						      x1, y1, zvalues[ind],
						      x2, y2,
						      zvalues[ind+incr]);
			}
			if (fm3d != null) {
			    P3d.Rectangle.addFlippedV(fm3d,
						      x1, y1, r1.zbase,
						      x2, y2, r2.zbase);
			}
		    }
		}
	    }
	    return zvalues;
	}
    }

      /**
     * Build a SteppedGrid specified by a set of rectangles and half
     * rectangles.
     * This class is provided to simplify the creation of a
     * {@link SteppedGrid}. Stepped grids are configured with a
     * set of X values and a set of Y values, sorted in ascending order.
     * The grid is divided into components that represent rectangular
     * cells. An index (i,j) places the lower left corner of each component
     * at the location (xs[i], ys[j]), where xs and ys are the arrays of
     * the X and Y values respectively. The width of a component is
     * xs[i+1]-xs[i] and the height of a component is ys[j+1]-ys[j],
     * which implies that the number of I indices is one less than the
     * number of X values and the number of J indices is one less than
     * the number of Y values.  Each component also has a z value
     * associated, measured from a base z value for the grid as a whole.
     * <P>
     * Creating a grid and filling it is tedious except for simple
     * cases. The {@link Builder} class allows a stepped grid to be
     * specified by providing individual rectangles and assigning
     * height to them.  The {@link Builder} supports both open and
     * closed grids. Open grids consist of a single grid, whereas
     * closed grids consist of two grids, which will be connected
     * where appropriate with vertical surfaces. For a closed grid,
     * rectangles denoted as placeholders are left blank and are to be
     * filled in by the caller.
     * <P>
     * The methods used depend on whether the SteppedGrid that will be
     * created is open or closed (open grids contain a single grid whereas
     * closed grids contain two, an upper and a lower grid). The methods
     * {@link Builder#addHalfRectangle(double,double,int,Corner,double)},
     * {@link Builder#addHalfRectangle(double,double,int,Corner,double,boolean)},
     * {@link Builder#addRectangle(double,double,double,double,double,boolean)}, and
     * {@link Builder#addRectangle(double,double,double,double,double,boolean,boolean)}
     * are used to configure open grids, whereas the methods
     * {@link Builder#addHalfRectangles(double,double,int,Corner,double,double)},
     * {@link Builder#addHalfRectangles(double,double,int,Corner,double,double,boolean,boolean)},
     * {@link Builder#addRectangles(double,double,double,double,double,double)},
     * {@link Builder#addRectangles(double,double,double,double,double,double,boolean,boolean)},
     * {@link Builder#addRectangle(double,double,double,double,double,boolean)}, and
     * {@link Builder#addRectangle(double,double,double,double,double,boolean,boolean)}
     * are used to configure closed grids.
    * One method is used for both open and closed grids:
     * {@link Builder#removeRectangles(double,double,double,double)}.
     * When the rectangles specified by these methods intersect, the
     * parameters for the rectangle created by the most recent call
     * are used to configure a grid component.
     * <P>
     * To configure a SteppedGrid, one simply specifies a sequence
     * of rectangles and half rectangles. The half rectangles must be
     * specified after all the full rectangles are specified.
     * when two rectangles intersect, the data for the component placed
     * at the intersection will be that specified by the most recent
     * call to addRectangle, addHalfRectangle, addRectangles,
     * addHalfRectangles, or removeRectangles. The use of placeholders
     * is the same as for* {@link SteppedGrid}.
     * After calls to addRectangle, addRectangles, addHalfRectangle,
     * addHalfRectangles, and/or removeRectangles, one will call the method
     * {@link #create()} to generate the stepped grids.
     * <P>
     * For example, if <code>m3d</code> is an instance of Model3D,
     * <BLOCKQUOTE><CODE><PRE>
     *  SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, -10.0);
     *  sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);
     *  sgb.addRectangles(20.0, 20.0, 60.0, 60.0, 0.0, 0.0, true, false);
     *  SteppedGrid sg = sgb.create()
     *  m3d.addTriangle(50.0, 50.0, 30.0,
     *                  20.0, 20.0, 10.0,
     *                  80.0, 20.0, 10.0);
     *  m3d.addTriangle(50.0, 50.0, 30.0,
     *                  80.0, 20.0, 10.0,
     *                  80.0, 80.0, 10.0);
     *  m3d.addTriangle(50.0, 50.0, 30.0,
     *                  80.0, 80.0, 10.0,
     *                  20.0, 80.0, 10.0);
     *  m3d.addTriangle(50.0, 50.0, 30.0,
     *                  20.0, 80.0, 10.0,
     *                  20.0, 20.0, 10.0);
     * </PRE></CODE></BLOCKQUOTE>
     * will create a box with a pyramid at its center. The second call
     * to addRectangles overrides the parameters provided by the first
     * call inside a rectangular area 60 by 60 in size that
     * constitutes the base of the pyramid. The second to the last
     * argument in the second call to addRectangles is true, implying
     * that the rectangle specifies a placeholder for the upper
     * grid. The lower grid will be filled as the final argument
     * is false. The empty area is then filled in by calls to
     * {@link Model3D#addTriangle(double,double,double,double,double,double,double,double,double)}.
     * The resulting object is shown in the following image:
     * <P style="text-align: center">
     * <img src="doc-files/sgbuilder.png">
     * <P>
     * Since a stepped grid implements the {@link org.bzdev.geom.Shape3D}
     * interface, one can easily obtain the boundary for a stepped grid
     * (e.g., a closed stepped grid with placeholders):
     * <BLOCKQUOTE><CODE><PRE>
     *  Path3D boundary = sg.getBoundary();
     * </PRE></CODE></BLOCKQUOTE>
     * <P>
     * If stepped-grid builder is improperly configured, sgb.create()
     * may throw an exception. The method
     * {@linkP3d#printSteppedGridBuilderCalls(Appendable,String,SteppedGrid.Builder)}
     * can be used to print information about which addRectangle or
     * addRectangles methods were responsible.
     * @see SteppedGrid
     * @see Model3D
     * @see P3d#printSteppedGridBuilderCalls(Appendable,String,SteppedGrid.Builder)
     */
    public static class Builder {
	Model3DOps<?> m3d;
	double[] xs;
	double[] ys;
	// NaN indicates that the corresponding grid is missing.
	double zBase1 = Double.NaN;
	double zBase2 = Double.NaN;

	boolean halfAdded = false;

	boolean hasStackTrace = false;
	HashMap<Integer,StackTraceElement[]> ste1 = new HashMap<>();
	HashMap<Integer,StackTraceElement[]> ste2 = new HashMap<>();
	StackTraceElement[] stackTraceUpper = null;
	StackTraceElement[] stackTraceLower = null;

	/**
	 * Get a stack trace containing the call to a
	 * {@link SteppedGrid.Builder} method that set part of the upper
	 * grid so as to cause an exception to be thrown.
	 * @return the stack trace; null if there is no such call or if
	 *         this builder was created when its model did not allow
	 *         stack traces.
	 */
	public StackTraceElement[] getUpperTrace() {
	    return stackTraceUpper;
	}

	/**
	 * Get a stack trace containing the call to a
	 * {@link SteppedGrid.Builder} method that set part of the lower
	 * grid so as to cause an exception to be thrown.
	 * @return the stack trace; null if there is no such call or if
	 *         this builder was created when its model did not allow
	 *         stack traces.
	 */
	public StackTraceElement[] getLowerTrace() {
	    return stackTraceLower;
	}


	/**
	 * Constructor for creating a partial stepped grid.
	 * @param m3d the model
	 * @param zBase the base height in the Z direction for the grid.
	 * @param isUpper true if the grid's outside direction faces up;
	 *        false if it faces down
	 */
	public Builder(Model3DOps<?> m3d, double zBase, boolean isUpper) {
	    if (Double.isNaN(zBase1))
		throw new IllegalArgumentException(errorMsg("NaN"));
	    this.m3d = m3d;
	    if (isUpper) {
		this.zBase1 = zBase;
	    } else {
		this.zBase2 = zBase;
	    }
	}

	/**
	 * Constructor for creating a full stepped grid.
	 * This constructor creates a stepped grid with an upper and
	 * lower grid.
	 * @param m3d the model
	 * @param zBase1 the base height in the Z direction for the upper grid
	 * @param zBase2 the base height in the Z direction for the lower grid
	 */
	public Builder(Model3DOps<?> m3d, double zBase1, double zBase2) {
	    if (Double.isNaN(zBase1))
		throw new IllegalArgumentException(errorMsg("NaN"));
	    if (Double.isNaN(zBase2))
		throw new IllegalArgumentException(errorMsg("NaN"));
	    if (zBase2 >= zBase1) {
		String msg = errorMsg("zBaseOrder", zBase1, zBase2);
		throw new IllegalArgumentException(msg);
	    }
	    this.m3d = m3d;
	    this.zBase1 = zBase1;
	    this.zBase2 = zBase2;
	}

	private class RectData {
	    double z1;
	    double z2;
	    boolean half = false;
	    int filledCorner = -1;
	    int length;		// used when 'half' is true
	    boolean placeholder1 = false;
	    boolean placeholder2 = false;
	    int priority1 = -1;
	    int priority2 = -1;
	    boolean filled = true;
	}

	HashMap<Rectangle2D,RectData> map = new HashMap<>(64);

	TreeSet<Double> xSet = new TreeSet<>();
	TreeSet<Double> ySet = new TreeSet<>();
	int pcount1 = 0;
	int pcount2 = 0;

	/**
	 * Add a vertical grid separator at a specified X value.
	 * This increases the total number of grid elements traversed
	 * in the X direction by 1.
	 * @param x the value of X
	 */
	public void addX(double x) {
	    float xf = (float)x;
	    xSet.add((double)xf);
	    xs = null;
	}

	/**
	 *  Add a vertical grid separator at a specified Y value.
	 * This increases the total number of grid elements traversed
	 * in the Y direction by 1.
	 * @param y the value of Y
	 */
	public void addY(double y) {
	    float yf = (float)y;
	    ySet.add((double)yf);
	    ys = null;
	}

	/**
	 * Get the number of vertical grid separators to traverse
	 * when going between two values of X
	 * @param x1 the starting value of X
	 * @param x2 the ending value of X, which must be larger than
	 *        or equal to x1
	 * @return the number of grid separators to traverse
	 */
	public int getLengthFromX(double x1, double x2) {
	    float x1f = (float)x1;
	    float x2f = (float)x2;
	    x1 = x1f;
	    x2 = x2f;
	    if (x1 >= x2) {
		throw new
		    IllegalArgumentException(errorMsg("requireLT", x1, x2));
	    }
	    if (!xSet.contains(x1)) {
		throw new IllegalArgumentException(errorMsg("argOffGrid", x1));
	    }
	    if (!xSet.contains(x2)) {
		throw new IllegalArgumentException(errorMsg("argOffGrid", x2));
	    }
	    int length = 0;
	    double x = x1;
	    while (x < x2) {
		x = xSet.higher(x);
		length++;
	    }
	    return length;
	}

	/**
	 * Get the number of horizontal grid separators to traverse
	 * when going between two values of Y
	 * @param y1 the starting value of Y
	 * @param y2 the ending value of Y, which must be larger than
	 *        or equal to y1
	 * @return the number of grid elements to traverse
	 */
	public int getLengthFromY(double y1, double y2) {
	    float y1f = (float)y1;
	    float y2f = (float)y2;
	    y1 = y1f;
	    y2 = y2f;
	    if (y1 >= y2) {
		throw new
		    IllegalArgumentException(errorMsg("requireLT", y1, y2));
	    }
	    if (!ySet.contains(y1)) {
		throw new IllegalArgumentException(errorMsg("argOffGrid", y1));
	    }
	    if (!ySet.contains(y2)) {
		throw new IllegalArgumentException(errorMsg("argOffGrid", y2));
	    }
	    int length = 0;
	    double y = y1;
	    while (y < y2) {
		y = ySet.higher(y);
		length++;
	    }
	    return length;
	}

	/**
	 * Enumeration naming the corner of a half rectangle.
	 * <P>
	 * This enumeration is used to indicate which corner of
	 * a half rectangle is the vertex to which filled rectangles,
	 * that are not placeholders, are attached.
	 */
	public static enum Corner {
	    /**
	     * The lower-left corner of a half rectangle
	     */
	    LOWER_LEFT(0),
	    /**
	     * The lower-right corner of a half rectangle
	     */
	    LOWER_RIGHT(1),

	    /**
	     * The upper-left corner of a half rectangle
	     */
	    UPPER_LEFT(3),

	    /**
	     * The upper-right corner of a half rectangle
	     */
	    UPPER_RIGHT(2);

	    private final int intval;
	    Corner(int value) {intval = value;}

	    // Integer codes are used internally in SteppedGridLayer
	    // but are not visible to users of this class.
	    int asInt() {return intval;}


	}


	/**
	 * Add half rectangles to the upper and lower grids.
	 * @param x the X coordinate of the lower left corner of the rectangle
	 *        containing this half rectangle
	 * @param y the Y coordinate of the lower left corner of the rectangle
	 *        containing this half rectangle
	 * @param length the number of grid elements
	 * @param filledCorner the corner within a filled region
	 * @param zOffset1 the height in the Z direction measured from the
	 *        the upper grid's base
	 * @param zOffset2 the height in the Z direction measured from the
	 *        the lower grid's base
	 */
	public void addHalfRectangles(double x, double y,
				      int length, Corner filledCorner,
				      double zOffset1, double zOffset2)
	{
	    addHalfRectangles(x, y, length, filledCorner,
			      zOffset1, zOffset2, false, false);
	}



	/**
	 * Add half rectangles to the upper and lower grids, specifying if
	 * the half rectangles are placeholders.
	 * @param x the X coordinate of the lower left corner of the rectangle
	 *        containing this half rectangle
	 * @param y the Y coordinate of the lower left corner of the rectangle
	 *        containing this half rectangle
	 * @param length the number of grid elements
	 * @param filledCorner the corner within a filled region
	 * @param zOffset1 the height in the Z direction measured from the
	 *        the upper grid's base
	 * @param zOffset2 the height in the Z direction measured from the
	 *        the lower grid's base
	 * @param placeholder1 true if the half-rectangle for the upper grid
	 *        is a placeholder; false if it is not
	 * @param placeholder2 true if the half-rectangle for the lower grid
	 *        is a placeholder; false if it is not
	 *
	 */
	public void addHalfRectangles(double x, double y,
				      int length, Corner filledCorner,
				      double zOffset1, double zOffset2,
				      boolean placeholder1,
				      boolean placeholder2)
	{
	    xs = null; ys = null;
	    float xf = (float)x;
	    float yf = (float)y;
	    x = xf;
	    y = yf;
	    if (!(xSet.contains(x) && ySet.contains(y))) {
		throw new
		    IllegalArgumentException(errorMsg("argOffGrid2", x, y));
	    }

	    double x1 = x;
	    double y1 = y;
	    for (int i = 0; i < length; i++) {
		Double d =  xSet.higher(x1);
		if (d == null) {
		    throw new IllegalArgumentException
			(errorMsg("lengthTooLongX", length, x, y));
		}
		x1 = d;
		d = ySet.higher(y1);
		if (d == null) {
		    throw new IllegalArgumentException
			(errorMsg("lengthTooLongY", length, x, y));
		}
		y1 = d;
	    }
	    double xlength = x1 - x;
	    double ylength = y1 - y;
	    Rectangle2D r = new Rectangle2D.Double(x, y, xlength, ylength);
	    RectData data;
	    if (map.containsKey(r)) {
		data = map.get(r);
	    } else {
		data = new RectData();
		map.put(r, data);
	    }
	    data.z1 = zOffset1;
	    data.z2 = zOffset2;
	    data.filledCorner = filledCorner.asInt();
	    data.length = length;
	    data.placeholder1 = placeholder1;
	    data.placeholder2 = placeholder2;
	    data.priority1 = pcount1++;
	    data.priority2 = pcount2++;
	    try {
		StackTraceElement[] ste =
		    Thread.currentThread().getStackTrace();
		hasStackTrace = true;
		ste1.put(data.priority1, ste);
		ste2.put(data.priority2, ste);
	    } catch (SecurityException se) {}
	    data.half = true;
	    halfAdded = true;
	}

	/**
	 * Add rectangles to the upper and lower grids.
	 * If rectangles intersect, the offsets and placeholder flags
	 * provided by the most recently defined of these rectangles are the
	 * offsets and placeholder flags used. The default placeholder
	 * flags used by this method have the value <code>false</code>.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param xlength the length of the rectangle in the X direction
	 * @param ylength the length of the rectangle in the Y direction
	 * @param zOffset1 the Z value of the upper-grid's rectangle
	 *        relative to the base value for its grid
	 * @param zOffset2 the Z value of the lower-grid's rectangle
	 *        relative to the base value for its grid
	 */
	public void addRectangles(double x, double y,
				 double xlength, double ylength,
				 double zOffset1, double zOffset2)
	{
	    addRectangles(x, y, xlength, ylength, zOffset1, zOffset2,
			 false, false);
	}

	/**
	 * Add rectangles to the upper and lower grids, specifying whether
	 * the rectangles are a placeholders.
	 * If rectangles intersect, the offsets and placeholder flags
	 * provided by the most recently defined of these rectangles are the
	 * offsets and placeholder flags used.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param xlength the length of the rectangle in the X direction
	 * @param ylength the length of the rectangle in the Y direction
	 * @param zOffset1 the Z value of the upper-grid's rectangle
	 *        relative to the base value for its grid
	 * @param zOffset2 the Z value of the lower-grid's rectangle
	 *        relative to the base value for its grid
	 * @param placeholder1 true if the rectangle to be added to the
	 *        upper grid is a placeholder; false otherwise
	 * @param placeholder2 true if the rectangle to be added to the
	 *        lower grid is a placeholder; false otherwise
	 */
	public void addRectangles(double x, double y,
				  double xlength, double ylength,
				  double zOffset1, double zOffset2,
				  boolean placeholder1,
				  boolean placeholder2)
	{
	    xs = null; ys = null;
	    if (halfAdded) {
		throw new IllegalStateException(errorMsg("halfAdded"));
	    }
	    float xf = (float)x;
	    float yf = (float)y;
	    float x2f = (float)(x + xlength);
	    float y2f = (float)(y + ylength);
	    x = xf;
	    y = yf;
	    xlength = (double)x2f - x;
	    ylength = (double)y2f - y;
	    xSet.add(x);
	    xSet.add((double) x2f);
	    ySet.add(y);
	    ySet.add((double) y2f);
	    Rectangle2D r = new Rectangle2D.Double(x, y, xlength, ylength);
	    RectData data;
	    if (map.containsKey(r)) {
		data = map.get(r);
	    } else {
		data = new RectData();
		map.put(r, data);
	    }
	    data.z1 = zOffset1;
	    data.z2 = zOffset2;
	    data.placeholder1 = placeholder1;
	    data.placeholder2 = placeholder2;
	    data.priority1 = pcount1++;
	    data.priority2 = pcount2++;
	    try {
		StackTraceElement[] ste =
		    Thread.currentThread().getStackTrace();
		hasStackTrace = true;
		ste1.put(data.priority1, ste);
		ste2.put(data.priority2, ste);
	    } catch (SecurityException se) {}
	}

	/**
	 * Remove any portions of rectangles that intersect a
	 * rectangular area in the upper and/or lower grids.
	 * @param x the lower-left X coordinate for the rectangular area
	 * @param y the lower-left Y coordinate for the rectangular area
	 * @param xlength the length of the rectangular area in the X direction
	 * @param ylength the length of the rectangular area  in the Y direction
	 *
	 */
	public void removeRectangles(double x, double y,
				     double xlength, double ylength)
	{
	    xs = null; ys = null;
	    if (halfAdded) {
		throw new IllegalStateException(errorMsg("halfAdded"));
	    }
	    float xf = (float)x;
	    float yf = (float)y;
	    float x2f = (float)(x + xlength);
	    float y2f = (float)(y + ylength);
	    x = xf;
	    y = yf;
	    xlength = (double)x2f - x;
	    ylength = (double)y2f - y;
	    xSet.add(x);
	    xSet.add((double) x2f);
	    ySet.add(y);
	    ySet.add((double) y2f);
	    Rectangle2D r = new Rectangle2D.Double(x, y, xlength, ylength);
	    RectData data;
	    if (map.containsKey(r)) {
		data = map.get(r);
	    } else {
		data = new RectData();
		map.put(r, data);
	    }
	    data.filled = false;
	    if (Double.isNaN(zBase1) == false) {
		data.priority1 = pcount1++;
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste1.put(data.priority1, ste);
		} catch (SecurityException se) {}
	    }
	    if (Double.isNaN(zBase2) == false) {
		data.priority2 = pcount2++;
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste2.put(data.priority2, ste);
		} catch (SecurityException se) {}
	    }
	}


	/**
	 * Insert a half rectangle into the upper or lower grid, but
	 * not both.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param length the number of elements in the X and Y directions
	 *        covered by this component.
	 * @param filledCorner the corner that lies within the filled region
	 *        of this half rectangle
	 * @param zOffset the Z value of the upper-grid's rectangle
	 *        relative to the base value for its grid
	 */
	public void addHalfRectangle(double x, double y,
				     int length, Corner filledCorner,
				     double zOffset)
	{
	    addHalfRectangle(x, y, length, filledCorner, zOffset,
			     false);
	}

	/**
	 * Insert a half rectangle into the upper or lower grid, but
	 * not both, and specifying if the rectangle is a placeholder.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param length the number of elements in the X and Y directions
	 *        covered by this component.
	 * @param filledCorner the corner that lies within the filled region
	 *        of this half rectangle
	 * @param zOffset the Z value of the upper-grid's rectangle
	 *        relative to the base value for its grid
	 * @param placeholder true if this object is a placeholder; false if
	 *        it is not.
	 */
	public void addHalfRectangle(double x, double y,
				     int length, Corner filledCorner,
				     double zOffset,
				     boolean placeholder)
	{
	    boolean isUpper = !Double.isNaN(zBase1);
	    boolean isLower = !Double.isNaN(zBase2);
	    if (isUpper && isLower) {
		throw new IllegalStateException();
	    }

	    xs = null; ys = null;
	    float xf = (float)x;
	    float yf = (float)y;
	    x = xf;
	    y = yf;
	    if (!xSet.contains(x)) {
		throw new IllegalArgumentException
		    (errorMsg("argOffGrid2", x, y));
	    }
	    if (!ySet.contains(y)) {
		throw new IllegalArgumentException
		    (errorMsg("argOffGrid2", x, y));
	    }

	    double x1 = x;
	    double y1 = y;
	    for (int i = 0; i < length; i++) {
		Double d = xSet.higher(x1);
		if (d == null) {
		    throw new IllegalArgumentException
			(errorMsg("lengthTooLongX", length, x, y));
		}
		x1 = d;
		d = ySet.higher(y1);
		if (d == null) {
		    throw new IllegalArgumentException
			(errorMsg("lengthTooLongY", length, x, y));
		}
		y1 = d;
	    }
	    double xlength = x1 - x;
	    double ylength = y1 - y;

	    Rectangle2D r = new Rectangle2D.Double(x, y, xlength, ylength);
	    RectData data;
	    if (map.containsKey(r)) {
		data = map.get(r);
	    } else {
		data = new RectData();
		map.put(r, data);
	    }
	    data.half = true;
	    data.filledCorner = filledCorner.asInt();
	    data.length = length;
	    if (isUpper) {
		data.z1 = zOffset;
		data.placeholder1 = placeholder;
		data.priority1 = pcount1++;
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste1.put(data.priority1, ste);
		} catch (SecurityException se) {}
	    } else {
		data.z2 = zOffset;
		data.placeholder2 = placeholder;
		data.priority2 = pcount2++;
		map.put(r, data);
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste2.put(data.priority2, ste);
		} catch (SecurityException se) {}
	    }
	    halfAdded = true;
	}

	/**
	 * Add a rectangle to the upper or lower grid.
	 * For stepped grids that do not have both a lower and upper
	 * grid, the isUpper parameter must match the choice made in
	 * the constructor.
	 * If rectangles intersect, the offsets and placeholder flags
	 * provided by the most recently defined of these rectangles are the
	 * offsets and placeholder flags used. The default placeholder
	 * flags used by this method have the value <code>false</code>.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param xlength the length of the rectangle in the X direction
	 * @param ylength the length of the rectangle in the Y direction
	 * @param zOffset the Z value of the grid's rectangle
	 *        relative to the base value for its grid
	 * @param isUpper true if the rectangle should be added to an
	 *        upper grid; false if the rectangle should be added to
	 *        the lower grid
	 */
	public void addRectangle(double x, double y,
				 double xlength, double ylength,
				 double zOffset, boolean isUpper)
	{
	    addRectangle(x, y, xlength, ylength, zOffset, isUpper, false);
	}

	/**
	 * Add a rectangle to the upper or lower grid, specifying whether
	 * the rectangle is a placeholder.
	 * For stepped grids that do not have both a lower and upper
	 * grid, the isUpper parameter must match the choice made in
	 * the constructor.
	 * If rectangles intersect, the offsets and placeholder flags
	 * provided by the most recently defined of these rectangles are the
	 * offsets and placeholder flags used.
	 * @param x the lower-left X coordinate for the rectangle
	 * @param y the lower-left Y coordinate for the rectangle
	 * @param xlength the length of the rectangle in the X direction
	 * @param ylength the length of the rectangle in the Y direction
	 * @param zOffset the Z value of the grid's rectangle
	 *        relative to the base value for its grid
	 * @param isUpper true if the rectangle should be added to an
	 *        upper grid; false if the rectangle should be added to
	 *        the lower grid
	 * @param placeholder true if the rectangle to be added is a
	 *        placeholder; false otherwise
	 */
	public void addRectangle(double x, double y,
				 double xlength, double ylength,
				 double zOffset, boolean isUpper,
				 boolean placeholder)
	{
	    xs = null; ys = null;
	    if (halfAdded) {
		throw new IllegalStateException(errorMsg("halfAdded"));
	    }
	    if (isUpper) {
		if (Double.isNaN(zBase1)) {
		    throw new
			IllegalStateException(errorMsg("notUpper", isUpper));
		}
	    } else {
		if (Double.isNaN(zBase2)) {
		    throw new
			IllegalStateException(errorMsg("notUpper", isUpper));
		}
	    }

	    float xf = (float)x;
	    float yf = (float)y;
	    x = xf;
	    y = yf;
	    float x2f = (float)(x + xlength);
	    float y2f = (float)(y + ylength);
	    x = xf;
	    y = yf;
	    xlength = (double)x2f - x;
	    ylength = (double)y2f - y;
	    xSet.add(x);
	    xSet.add((double) x2f);
	    ySet.add(y);
	    ySet.add((double) y2f);
	    Rectangle2D r = new Rectangle2D.Double(x, y, xlength, ylength);
	    RectData data;
	    if (map.containsKey(r)) {
		data = map.get(r);
	    } else {
		data = new RectData();
		map.put(r, data);
	    }
	    if (isUpper) {
		data.z1 = zOffset;
		data.placeholder1 = placeholder;
		data.priority1 = pcount1++;
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste1.put(data.priority1, ste);
		} catch (SecurityException se) {}
	    } else {
		data.z2 = zOffset;
		data.placeholder2 = placeholder;
		data.priority2 = pcount2++;
		try {
		    StackTraceElement[] ste =
			Thread.currentThread().getStackTrace();
		    hasStackTrace = true;
		    ste2.put(data.priority2, ste);
		} catch (SecurityException se) {}
		// map.put(r, data);
	    }
	}

	private static class ComponentData {
	    int priority1 = -1;
	    int priority2 = -1;
	    boolean filled = true;
	    double zincr1 = Double.NaN;
	    double zincr2 = Double.NaN;
	    boolean placeholder1;
	    boolean placeholder2;
	    int i1 = -1;
	    int j1 = -1;
	    int i2 = -1;
	    int j2 = -1;
	    boolean half1 = false;
	    boolean half2 = false;
	    int length;
	    int filledCorner1 = -1;
	    int filledCorner2 = -1;

	}

	double maxZSpacing = 0.0;

	/**
	 * Set the maximum spacing between vertices in the Z direction.
	 * <P>
	 * This method is intended for cases where a model is configured
	 * with an instance of {@link org.bzdev.geom.Transform3D}, when
	 * that transform does not map straight lines to straight lines.
	 * In that case, one will typically configure a grid as collection
	 * of small rectangles. This method will ensure that the spacing
	 * between vertices is small in the Z direction as well.
	 * @param spacing the maximum spacing; 0.0 or a negative value if
	 *        there is none
	 */
	public void setMaxZSpacing(double spacing) {
	    maxZSpacing = spacing;
	}

	/**
	 * Determine if this stepped grid has a maximum spacing in the Z
	 * direction.
	 * @return true if a maximum spacing exists; false otherwise
	 */
	public boolean hasMaxZSpacing() {
	    return maxZSpacing > 0.0;
	}

	/**
	 * Get the maximum spacing between vertices in the Z direction.
	 * @return the maximum spacing; 0.0 if none is defined.
	 */
	public double getMaxZSpacing() {
	    if (maxZSpacing < 0.0) return 0.0;
	    return maxZSpacing;
	}

	/**
	 * Get the X coordinates delimiting grid elements.
	 * @return the X coordinates in ascending order.
	 */
	public double[] getXs() {
	    double[] xs = new double[xSet.size()];
	    int ind = 0;
	    for (Double value: xSet) {
		xs[ind++] = value;
	    }
	    return xs;
	}

	/**
	 * Get the Y coordinates delimiting grid elements.
	 * @return the Y coordinates in ascending order.
	 */
	public double[] getYs() {
	    double[] ys = new double[ySet.size()];
	    int ind = 0;
	    for (Double value: ySet) {
		ys[ind++] = value;
	    }
	    return ys;
	}


	/**
	 * For a given key get the index into the array that
	 * {@link #getXs()} would return.
	 * @param x the key
	 * @return the index; a negative value if x does not lie on the
	 *         boundary between elements
	 */
	public int getIndexFromX(double x) {
	    float xf = (float) x;
	    x = xf;
	    if (xs == null) xs = getXs();
	    return Arrays.binarySearch(xs, x);
	}

	/**
	 * For a given key, get the index into the array that
	 * {@link #getYs()} would return.
	 * @param y the key
	 * @return the index; a negative value if y does not lie on the
	 *         boundary between elements
	 */
	public int getIndexFromY(double y) {
	    float yf = (float) y;
	    y = yf;
	    if (ys == null) ys = getYs();
	    return Arrays.binarySearch(ys, y);
	}


	/**
	 * Create a stepped grid and place it in the default model.
	 * @return the stepped grid that was created
	 */
	public SteppedGrid create() {
	    return create(m3d);
	}

	/**
	 * Create a stepped grid.
	 * @param m3d the model to which the grid will be added
	 * @return the stepped grid that was created
	 */
	public SteppedGrid create(Model3DOps<?> m3d) {
	    SteppedGrid sg;
	    double[] xs = new double[xSet.size()];
	    double[] ys = new double[ySet.size()];
	    int ind = 0;
	    for (Double value: xSet) {
		xs[ind++] = value;
	    }
	    ind = 0;
	    for (Double value: ySet) {
		ys[ind++] = value;
	    }

	    if (Double.isNaN(zBase1)) {
		sg = new SteppedGrid(m3d, xs, ys,zBase2, false);
	    } else if (Double.isNaN(zBase2)) {
		sg = new SteppedGrid(m3d, xs, ys, zBase1, true);
	    } else {
		sg = new SteppedGrid(m3d, xs, ys, zBase1, zBase2);
	    }
	    if (maxZSpacing > 0.0) {
		sg.setMaxZSpacing(maxZSpacing);
	    }
	    ComponentData[][] cdata = new ComponentData[xs.length][ys.length];
	    for (Map.Entry<Rectangle2D,RectData> entry: map.entrySet()) {
		Rectangle2D r = entry.getKey();
		RectData data = entry.getValue();
		int minXIndex = Arrays.binarySearch(xs, r.getMinX());
		int maxXIndex = Arrays.binarySearch(xs, r.getMaxX());
		int minYIndex = Arrays.binarySearch(ys, r.getMinY());
		int maxYIndex = Arrays.binarySearch(ys, r.getMaxY());
		for (int i = minXIndex; i < maxXIndex; i++) {
		    for (int j = minYIndex; j < maxYIndex; j++) {
			ComponentData cd = cdata[i][j];
			if (cdata[i][j] == null) {
			    cd = new ComponentData();
			    cdata[i][j] = cd;
			}
			if (cd.priority1 < data.priority1) {
			    cd.priority1 = data.priority1;
			    cd.filled = data.filled;
			    cd.zincr1 = data.z1;
			    cd.placeholder1 = data.placeholder1;
			    cd.i1 = minXIndex;
			    cd.j1 = minYIndex;
			    cd.half1 = data.half;
			    cd.filledCorner1 = data.filledCorner;
			    cd.length = data.length;
			}
			if (cd.priority2 < data.priority2) {
			    cd.priority2 = data.priority2;
			    cd.filled = data.filled;
			    cd.zincr2 = data.z2;
			    cd.placeholder2 = data.placeholder2;
			    cd.i2 = minXIndex;
			    cd.j2 = minYIndex;
			    cd.half2 = data.half;
			    cd.filledCorner2 = data.filledCorner;
			    cd.length = data.length;
			}
		    }
		}
	    }
	    int mm1 = xs.length-1;
	    int nm1 = ys.length-1;
	    for (int i = 0; i < mm1; i++) {
		for (int j = 0; j < nm1; j++) {
		    ComponentData cd = cdata[i][j];
		    if (cd != null && cd.filled) {
			if (hasStackTrace) {
			    try {
				stackTraceUpper = (cd.priority1 == -1)? null:
				    ste1.get(cd.priority1);
				stackTraceLower = (cd.priority2 == -1)? null:
				    ste1.get(cd.priority2);
			    } catch (Exception e) {
				stackTraceUpper = null;
				stackTraceLower = null;
			    }
			}
			if (cd.priority1 == -1) {
			    if (cd.half2) {
				int cdlen = cd.length-1;
				switch(cd.filledCorner2) {
				case 0:
				    if (((j - cd.j2) + (i - cd.i2)) == cdlen) {
					sg.addHalfComponent(i, j, cd.zincr2,
							    cd.placeholder2);
				    } else if (((j - cd.j2) + (i - cd.i2))
					< cdlen) {
					sg.addComponent(i, j, cd.zincr2);
				    } else if (cd.placeholder2) {
					sg.addComponent(i, j, cd.zincr2, true);
				    }
				    break;
				case 1:
				    if ((i - cd.i2) == (j - cd.j2)) {
					sg.addHalfComponent(i, j, cd.zincr2,
							    cd.placeholder2);
				    } else if ((i - cd.i2) < (j - cd.j2)) {
					if (cd.placeholder2) {
					    sg.addComponent(i, j, cd.zincr2,
							    true);
					}
				    } else {
					sg.addComponent(i, j, cd.zincr2,
							false);
				    }
				    break;
				case 2:
				    if (((j - cd.j2) + (i - cd.i2)) == cdlen) {
					sg.addHalfComponent(i, j, cd.zincr2,
							    cd.placeholder2);
				    } else if (((j - cd.j2) + (i - cd.i2))
					> cdlen) {
					sg.addComponent(i, j, cd.zincr2);
				    } else if (cd.placeholder2) {
					sg.addComponent(i, j, cd.zincr2, true);
				    }
				    break;
				case 3:
				    if ((i - cd.i2) == (j - cd.j2)) {
					sg.addHalfComponent(i, j, cd.zincr2,
							    cd.placeholder2);
				    } else if ((i - cd.i2) > (j - cd.j2)) {
					if (cd.placeholder2) {
					    sg.addComponent(i, j, cd.zincr2,
							    true);
					}
				    } else {
					sg.addComponent(i, j, cd.zincr2,
							false);
				    }
				    break;
				}
			    } else {
				sg.addComponent(i, j, cd.zincr2,
						cd.placeholder2);
			    }
			} else if (cd.priority2 == -1) {
			    if (cd.half1) {
				int cdlen = cd.length-1;
				switch(cd.filledCorner1) {
				case 0:
				    if (((j - cd.j1) + (i - cd.i1)) == cdlen) {
					sg.addHalfComponent(i, j, cd.zincr1,
							    cd.placeholder1);
				    } else if (((j - cd.j1) + (i - cd.i1))
					< cdlen) {
					sg.addComponent(i, j, cd.zincr1);
				    } else if (cd.placeholder1) {
					sg.addComponent(i, j, cd.zincr1, true);
				    }
				    break;
				case 1:
				    if ((i - cd.i1) == (j - cd.j1)) {
					sg.addHalfComponent(i, j, cd.zincr1,
							    cd.placeholder1);
				    } else if ((i - cd.i1) < (j - cd.j1)) {
					if (cd.placeholder1) {
					    sg.addComponent(i, j, cd.zincr1,
							    true);
					}
				    } else {
					sg.addComponent(i, j, cd.zincr1,
							false);
				    }
				    break;
				case 2:
				    if (((j - cd.j1) + (i - cd.i1)) == cdlen) {
					sg.addHalfComponent(i, j, cd.zincr1,
							    cd.placeholder1);
				    } else if (((j - cd.j1) + (i - cd.i1))
					> cdlen) {
					sg.addComponent(i, j, cd.zincr1);
				    } else if (cd.placeholder1) {
					sg.addComponent(i, j, cd.zincr1, true);
				    }
				    break;
				case 3:
				    if ((i - cd.i1) == (j - cd.j1)) {
					sg.addHalfComponent(i, j, cd.zincr1,
							    cd.placeholder1);
				    } else if ((i - cd.i1) > (j - cd.j1)) {
					if (cd.placeholder1) {
					    sg.addComponent(i, j, cd.zincr1,
							    true);
					}
				    } else {
					sg.addComponent(i, j, cd.zincr1,
							false);
				    }
				    break;
				}
			    } else {
				sg.addComponent(i, j, cd.zincr1,
						cd.placeholder1);
			    }
			} else {
			    if (cd.half1 && cd.half2) {
				int cdlen = cd.length-1;
				switch(cd.filledCorner1) {
				case 0:
				    if (((j - cd.j1) + (i - cd.i1)) == cdlen) {
					sg.addHalfComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
				    } else if (((j - cd.j1) + (i - cd.i1))
					< cdlen) {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2);
				    } else if (cd.placeholder1
					       || cd.placeholder2) {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2,
							cd.placeholder1,
							cd.placeholder2);
				    }
				    break;
				case 1:
				    if ((i - cd.i1) == (j - cd.j1)) {
					sg.addHalfComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
				    } else if ((i - cd.i1) < (j - cd.j1)) {
					if (cd.placeholder1
					    || cd.placeholder2) {
					    sg.addComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
					}
				    } else {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2);
				    }
				    break;
				case 2:
				    if (((j - cd.j1) + (i - cd.i1)) == cdlen) {
					sg.addHalfComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
				    } else if (((j - cd.j1) + (i - cd.i1))
					> cdlen) {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2);
				    } else if (cd.placeholder1
					       || cd.placeholder2) {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2,
							cd.placeholder1,
							cd.placeholder2);
				    }
				    break;
				case 3:
				    if ((i - cd.i1) == (j - cd.j1)) {
					sg.addHalfComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
				    } else if ((i - cd.i1) > (j - cd.j1)) {
					if (cd.placeholder1
					    || cd.placeholder2) {
					    sg.addComponent(i, j,
							    cd.zincr1,
							    cd.zincr2,
							    cd.placeholder1,
							    cd.placeholder2);
					}
				    } else {
					sg.addComponent(i, j,
							cd.zincr1, cd.zincr2);
				    }
				    break;
				}
			    } else {
				sg.addComponent(i, j, cd.zincr1, cd.zincr2,
						cd.placeholder1,
						cd.placeholder2);
			    }
			}
		    } else {
			stackTraceUpper = null;
			stackTraceLower = null;
		    }
		}
	    }
	    stackTraceUpper = null;
	    stackTraceLower = null;
	    sg.addsCompleted();
	    return sg;
	}
    }
}

//  LocalWords:  vertices xs ys zbase indices isin isUpper boolean sg
//  LocalWords:  addComponent addsCompleted nullArgument zUpper arg
//  LocalWords:  zLower heightOrder IllegalStateException oneHeight
//  LocalWords:  IllegalArgumentException indicesRange upperHeight
//  LocalWords:  upperBaseHeight lowerHeight lowerBaseHeight notDone
//  LocalWords:  upperIsPlaceholder lowerIsPlaceholder twoHeights img
//  LocalWords:  isUpperShouldBe notFilled src externalEdges PRE sgb
//  LocalWords:  SteppedGrid addRectangles addRectangle BLOCKQUOTE
//  LocalWords:  NaN zBase zBaseOrder xlength ylength zOffset zzset
//  LocalWords:  exbundle lowerIsPlacehloder upperPlaceholder HashSet
//  LocalWords:  lowerPlaceholder zvalues mdash SGILGCount SGILG sgl
//  LocalWords:  filledCorner getXs getYs addHalfComponent offGrid
//  LocalWords:  lineIsVertical noComponentsSet noHeight tcount
//  LocalWords:  addHalfRectangle addHalfRectangles removeRectangles
//  LocalWords:  SteppedGrids requireLT argOffGrid SteppedGridLayer
//  LocalWords:  lengthTooLongX lengthTooLongY halfAdded notUpper
//  LocalWords:  addTriangle getBoundary indices's topBottom
//  LocalWords:  printSteppedGridBuilderCalls Appendable
