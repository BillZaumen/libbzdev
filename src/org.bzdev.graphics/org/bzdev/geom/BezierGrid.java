package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.RealValuedFunctTwoOps;
import org.bzdev.math.VectorOps;
import org.bzdev.util.Cloner;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.io.Flushable;
import java.io.IOException;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class representing a grid of cubic B&eacute;zier patches.
 * The patches are specified by providing a rectangular grid of points,
 * some of which may be null. Most of the constructors specify this
 * grid directly.  There are two exceptions:
 * <UL>
 *   <LI>{@link BezierGrid#BezierGrid(Path2D,BezierGrid.Mapper)}.
 *   <LI>{@link BezierGrid#BezierGrid(Path2D,Point3DMapper,int,boolean)}.
 * </UL>
 * The first argument of these two constructorss is a 2D path representing a
 * cross section of a tube or wire, and handles a common special case.
 * <P>
 * Each element of the grid contains
 * <UL>
 *   <LI> a point on the surface that the grid represents. This
 *        point corresponds to the U-V coordinate (0,0), and may
 *        be null.  It may also be marked as "unfilled" by calling
 *        {@link #remove(int,int)} or {@link #remove(int,int,int,int)}.
 *   <LI> the intermediate control points for a cubic B&eacute;zier curve
 *        along the edge from (0,0) to (1, 0), excluding the
 *        initial control point. These control points are
 *        P<sub>10</sub> and P<sub>20</sub>, where P<sub>ij</sub> are
 *        the control points for the corresponding cubic B&eacute;zier
 *        patch. The values of these intermediate control points can
 *        be explicitly set, although they will be computed if necessary.
 *   <LI> the intermediate control points for a cubic B&eacute;zier curve
 *        along the edge from (0,0) to (0, 1), excluding the
 *        initial control point. These control points are
 *        P<sub>01</sub> and P<sub>02</sub>, where P<sub>ij</sub> are
 *        the control points for the corresponding cubic B&eacute;zier
 *        patch.  The values of these intermediate control points can
 *        be explicitly set, although they will be computed if necessary.
 *   <LI> the four control points P<sub>11</sub>, P<sub>21</sub>,
 *        P<sub>12</sub> and P<sub>22</sub>. While these control points
 *        can be provided explicitly, if these points are not provided a
 *        default will be computed.
 * </UL>
 * The remaining control points for the cubic B&eacute;zier patch
 * associated with a grid element are provided by the adjacent grid
 * elements.  All of these points are stored as double precision
 * numbers that have been rounded to the nearest float value by
 * replacing a value v with (double)(float)v.  This is done for
 * consistency with the Surface3D and Model3D classes, and to reduce
 * the impact of floating point errors in computing coordinates.  The
 * methods
 * {@link BezierGrid#createConnectionsTo(BezierGrid)},
 * {@link BezierGrid#createConnectionsTo(BezierGrid,int)},
 * {@link BezierGrid#createConnectionsTo(BezierGrid,int,boolean)},
 * {@link BezierGrid#createConnectionsTo(BezierGrid,boolean,int...)},
 * {@link BezierGrid#createConnectionsTo(BezierGrid,int,boolean,int...)}, and
 * {@link BezierGrid#createConnectionsTo(BezierGrid,int,boolean,boolean,int...)}
 * have a precondition: except when a loop is being closed (that is, the
 * intermediate control points differ from the grid points), multiple
 * points on a grid must not have the same values. For cases where it
 * is convenient to create grids in which multiple points have the
 * same values, one may be able to use the method
 * {@link BezierGrid#subgrid(int,int,int,int)} to create a suitable
 * grid.  This is useful in some unusual cases such as creating a M&ouml;bius
 * strip where to get a closed curve, the surface has to be traversed twice,
 * and a subgrid will produce a shape that is closed.
 * <P>
 * A grid has two dimensions, with indices (i,j). For an element
 * at  indices (i, j), the end points for each edge of the patch
 * are shown in the following table.
 * <BLOCKQUOTE>
 * <TABLE border="1">
 *   <CAPTION>&nbsp;</CAPTION>
 *   <TR>
 *       <TH><TH>Starting<BR>(u,v)<TH>Ending<BR>(uv)
 *           <TH>Starting<BR> indices<TH>Ending<BR>indices
 *   </TR>
 *   <TR><TH>Edge 0<TD>(0,0)<TD>(1,0)<TD>(i,j)<TD>(i+1,j)</TR>
 *   <TR><TH>Edge 1<TD>(1,0)<TD>(1,1)<TD>(i+1,j)<TD>(i+1,j+1)</TR>
 *   <TR><TH>Edge 2<TD>(0,1)<TD>(1,1)<TD>(i,j+1)<TD>(i+1,j+1)</TR>
 *   <TR><TH>Edge 3<TD>(0,0)<TD>(0,1)<TD>(i,j)<TD>(i,j+1)</TR>
 * </TABLE>
 * </BLOCKQUOTE>
 * Edge 2 and Edge 3 are traversed in the opposite direction from
 * the one shown when determining the patch's orientation, which uses
 * the right-hand rule. The implementation stores the control points
 * for Edge 0 and Edge 3 in the element associated with the
 * index (i,j). Adjacent elements are queried to find the remaining
 * edge's control points.
 * <P>
 * Splines are automatically created to set the shape of each edge,
 * ensuring that adjacent patches share common edges. The knots
 * for these splines have either constant values of i or constant
 * values of j. These splines are controlled by two constructor arguments:
 * uclosed and vclosed:
 * <UL>
 *   <LI> When uclosed is false, the splines with knots at constant values
 *        of j cover spans of points that are within the grid and not null.
 *   <LI> When uclosed is true, the splines with knots at constant values
 *        of j also cover spans of points that are within the grid and not
 *        null. If none of the elements are null, a cyclic spine is
 *        created. Otherwise splines are delimited by null grid points,
 *        and the sequence of knots with wrap around if necessary (i.e.,
 *        a knot at the maximum value of i may be followed by a knot for
 *        which i = 0).
 *   <LI> When vclosed is false, the splines with knots at constant values
 *        of i cover spans of points that are within the grid and not null.
 *   <LI> When vclosed is true, the splines with knots at constant values
 *        of i also cover spans of points that are within the grid and not
 *        null. If none of the elements are null, a cyclic spine is
 *        created. Otherwise splines are delimited by null grid points,
 *        and the sequence of knots with wrap around if necessary (i.e.,
 *        a knot at the maximum value of j may be followed by a knot for
 *        which j = 0).
 * </UL>
 * When uclosed is true and for a fixed value of j, the point at the
 * maximum value of i must not be the same as the point at i = 0.
 * Similarly, When vclosed is true and for a fixed value of i, the
 * point at the maximum value of j must not be the same as the point
 * at j = 0.  These flags are useful for some common shapes:
 * <UL>
 *   <LI> When one is true, the grid can represent a cylinder or a ring with
 *        two boundaries.
 * </UL>  When both are true, the grid can represent a torus.
 * <P>
 * One can partition the grid into regions by giving each point a
 * region ID (by default, points will be in region 0). The splines described
 * above will be split into multiple splines when a region boundary is
 * crossed. This allows the grid to have sharp corners or edges at the
 * region boundaries. A point on one side of a boundary will be connected to
 * a point on the other side by a straight line segment. The methods to
 * create regions are {@link #setRegion(int,int,int)} and
 * {@link #setRegion(int,int,int,int,int)}.
 * <P>
 * In addition, users may define additional splines.
 * The additional splines are used to set edges after the
 * automatically generated splines are created, thus overriding
 * those values. The additional splines are used in the order in which
 * they were defined. The methods {@link #startSpline(int,int)},
 * {@link #moveU(int)}, {@link #moveV(int)}, and {@link #endSpline(boolean)}
 * are used for configuring these splines.
 * <P>
 * For example, in order to create the following object,
 * <P style="text-align:center">
 *     <IMG SRC="doc-files/bgsphere.png" alt="sphere">
 * </P>
 * one can first create a grid representing a half of a sphere:
 * <BLOCKQUOTE><PRE><CODE>
 *	int N = 41; // grid height and width
 *	int NC = N/2; // grid center point.
 *	Point3D[][] array1 = new Point3D[N][N];
 *
 *	double r = 100.0;	// radius of the sphere
 *
 *	for (int i = 0; i &lt; N; i++) {
 *	    for (int j = 0; j &lt; N; j++) {
 *		int k = Math.max(Math.abs(i-NC),Math.abs(j-NC));
 *		double theta = k*(Math.PI/(N-1));
 *		double x, y, z;
 *		if (k == 0) {
 *                  // top of the sphere
 *		    x = 0.0; y = 0.0; z = r;
 *		} else {
 *                  // Each square box around the center of
 *                  // the grid (in U-V space) represents a
 *                  // line of constant latitude. Adjacent
 *                  // vertices along a specific box are
 *                  // separated by evenly spaced longitude
 *                  // values.
 *		    int nanglesHalf = k*4;
 *		    double delta = Math.PI/(nanglesHalf);
 *		    double angle;
 *		    if (i == NC+k) {
 *			angle = -(NC-j)*delta;
 *		    } else if (j == NC-k) {
 *			angle = -(NC + 2*k - i)*delta;
 *		    } else if (i == NC-k) {
 *			angle = -((j-NC) + 4*k)*delta;
 *		    } else if (j == NC+k) {
 *			angle = (NC+2*k-i)*delta;
 *		    } else {
 *			throw new Error();
 *		    }
 *		    x = r * Math.cos(angle) * Math.sin(theta);
 *		    y = r * Math.sin(angle) * Math.sin(theta);
 *                  // the 10.0 shifts the sphere up slightly
 *                  // along the Z axis
 *		    z = 10.0 + r * Math.cos(theta);
 *		}
 *		array1[i][j] = new Point3D.Double(x, y, z);
 *	    }
 *	}
 *	BezierGrid grid1 = new BezierGrid(array1);
 * </CODE></PRE></BLOCKQUOTE>
 * Note that the value of z for each point adds a constant 10.0 to
 * each value, which translates the grid upwards.
 * After this grid is created, it can be modified by removing the
 * vertices above a specific latitude (note that the area removed
 * has boundary that corresponds to a series of points with a
 * constant latitude).
 * <BLOCKQUOTE><PRE><CODE>
 *	grid1.remove(NC-3, NC-3, 6, 6);
 * </CODE></PRE></BLOCKQUOTE>
 * The half sphere now has a boundary with two components (two
 * distinct continuous, closed paths).  For these, we set a user
 * defined spline in order to make sure that splines will be
 * created going through these vertices and that the spline will
 * be a cyclic one (the argument of true for endSpline makes the
 * spline a cyclic spline).
 * <BLOCKQUOTE><PRE><CODE>
 *	grid1.startSpline(NC-3, NC-3);
 *	grid1.moveU(6);
 *	grid1.moveV(6);
 *	grid1.moveU(-6);
 *	grid1.moveV(-6);
 *	grid1.endSpline(true);
 *
 *	grid1.startSpline(0,0);
 *	grid1.moveU(N-1);
 *	grid1.moveV(N-1);
 *	grid1.moveU(-(N-1));
 *	grid1.moveV(-(N-1));
 *	grid1.endSpline(true);
 * </CODE></PRE></BLOCKQUOTE>
 * The next step is to create the bottom half of the sphere.
 * A constructor (please see the documentation for
 * {@link BezierGrid#BezierGrid(BezierGrid,boolean,UnaryOperator)}
 * for restrictions) allows one to reproduce the configuration of an
 * existing grid, with a lambda expression mapping previous vertex
 * locations into new ones:
 * <BLOCKQUOTE><PRE><CODE>
	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -&gt; {
		double x = p.getX();
		double y = p.getY();
		double z = - p.getZ();
		return new Point3D.Double(x, y, z);
	    });
 * </CODE></PRE></BLOCKQUOTE>
 * The argument <CODE>true</CODE> indicates that grid2 should have its
 * orientation reversed compared to grid1.
 * At this point, one can create a surface and append the two grids.
 * <BLOCKQUOTE><PRE><CODE>
	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);
 * </CODE></PRE></BLOCKQUOTE>
 * The next step is to create an array of grids (it will just contain a
 * single element). This grid will have two vertices in its U direction,
 * and will connect grid1 to grid2. The argument 'false' prevents the
 * grids from being split at spline boundaries, and the two arguments
 * with the value <CODE>NC-3</CODE> are indices of a point on grid1's
 * boundary corresponding to the circular hole in grid1 at the 'top' of
 * its half sphere.  Because there are only two vertices in the U direction,
 * the call to createConnectionsTo generates a cylinder.
 * The newly created grid is appended to the surface.
 * <BLOCKQUOTE><PRE><CODE>
 *	BezierGrid[] connections =
 *	    grid1.createConnectionsTo(grid2, false, NC-3, NC-3);
 *	for (BezierGrid g: connections) {
 *	    surface.append(g);
 *	}
 * </CODE></PRE></BLOCKQUOTE>
 * Similarly, one can create a second set of grids, this time with
 * 11 vertices in the U direction.
 * <BLOCKQUOTE><PRE><CODE>
 *      connections = grid1.createConnectionsTo(grid2, 11, false, 0, 0);
 * </CODE></PRE></BLOCKQUOTE>
 * In the U direction index 0 and 10 represent V values that match the
 * boundaries at the widest points of the two half spheres (grid1 and grid2).
 * The remaining vertices have to be configured manually. For an example,
 * this is done as follows:
 * <BLOCKQUOTE><PRE><CODE>
 *      BezierGrid cgrid = connections[0];
 *      int limu = cgrid.getUArrayLength() ;
 *      int limv = cgrid.getVArrayLength();
 *      for (int j = 0; j &lt; limv; j++) {
 *        for (int i = 1; i &lt; limu - 1; i++) {
 *           Point3D p = cgrid.getPoint(0, j);
 *           double t = ((double)i)/limu;
 *           double z1 = p.getZ();
 *           double z2 = cgrid.getPoint(10, j).getZ();
 *           double z = z1*(1-t) + z2*t;
 *           double scale = 1.0 + 0.25*Math.sin(Math.PI*t);
 *           double x = scale*p.getX();
 *           double y = scale*p.getY();
 *           cgrid.setPoint(i, j, x, y, z);
 *        }
 *      }
 *	surface.append(cgrid);
 * </CODE></PRE></BLOCKQUOTE>
 * The result is a 'bulge' connecting the two half spheres, with
 * the bulge using a sinusoidal shape.  Finally, one can create
 * a series of images using methods from the p3d package:
 * <BLOCKQUOTE><PRE><CODE>
 *      Model3D m3d = new Model3D();
 *      m3d.append(surface);
 *      m3d.setTessellationLevel(2);
 *      m3d.createImageSequence
 *          (new FileOutputStream("images.isq"),
 *           "png", 8, 6, 0.0, 0.0, 0.0, false);
 * </CODE></PRE></BLOCKQUOTE>
 */
public class BezierGrid implements Shape3D {
    
    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    // To determine if vertices lie in a plane by computing
    // v1 . (v2 X v3) using normalized vectors.  The limit allows
    // for roundoffs errors.
    static final double PLANAR_LIMIT = 1.e-6;


    private static class Vertex {
	int i;
	int j;
	Point3D p = null;
	Color color = null;
	// boolean ucSet = false;
	double[] uc = null;
	// boolean vcSet = false;
	double[] vc = null;
	boolean filled = false;
	int region = 0;
	double[] rest = null;
	// spline numbers
	int vsn = -1;
	int usn = -1;
    }

    int nu;
    int nv;

    private boolean linear = false;

    /**
     * Get the array length in the U direction (e.g., the length for
     * the first index).
     * @return the array length for the U direction
     */
    public int getUArrayLength() {return nu;}

    /**
     * Get the array length in the V direction (e.g., the length for
     * the second index).
     * @return the array length for the V direction
     */
    public int getVArrayLength() {return nv;}

    /**
     * Determine if the grid is closed in the U direction.
     * @return true if the grid is closed in the U direction; false otherwise
     */
    public boolean isUClosed() {return uclosed;}

    /**
     * Determine if the grid is closed in the V direction.
     * @return true if the grid is closed in the V direction; false otherwise
     */
    public boolean isVClosed() {return vclosed;}

    boolean  uclosed;
    boolean vclosed;
    Vertex array[][];
    boolean flipped = false;
    boolean rflip = false;

    /**
     * Reverse the use of true and false in the method
     * {@link #reverseOrientation(boolean)}.
     */
    protected void reverseFlip() {
	flipped = true;
	rflip = true;
    }

    /**
     * Constructor for an empty grid.
     * When a direction is closed, then when all points along a line in that
     * direction are defined, no points along that line have been
     * removed, and  all points along that lie are in the same region, then
     * the spline created will be a cyclic one.  When closed, one should
     * not duplicate an end point: the expected behavior occurs automatically.
     * @param nu the number of vertices in the U direction
     * @param uclosed true if the U direction is close; false otherwise
     * @param nv the number of vertices in the V direction
     * @param vclosed true if the V direction is closed; false otherwise
     */
    public BezierGrid(int nu, boolean uclosed, int nv, boolean vclosed) {
	this(nu, uclosed, nv, vclosed, false);
    }

    /**
     * Constructor for an empty, possibly linear grid.
     * When a direction is closed, then when all points along a line in that
     * direction are defined, no points along that line have been
     * removed, and  all points along that lie are in the same region, then
     * the spline created will be a cyclic one.  When closed, one should
     * not duplicate an end point: the expected behavior occurs automatically.
     * <P>
     * When the argument <CODE>linear</COdE> is true, each grid point is
     * configured to have its own region. By default, each line connecting
     * two adjacent grid points will be a straight line.
     * @param nu the number of vertices in the U direction
     * @param uclosed true if the U direction is close; false otherwise
     * @param nv the number of vertices in the V direction
     * @param vclosed true if the V direction is closed; false otherwise
     * @param linear true if lines connecting grid points are straight lines;
     *        false otherwise
     */
    public BezierGrid(int nu, boolean uclosed, int nv, boolean vclosed,
		      boolean linear)
    {
	this.uclosed = uclosed;
	this.vclosed = vclosed;
	this.nu = nu;
	this.nv = nv;
	array = new Vertex[nu][nv];
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		Vertex v = new Vertex();
		array[i][j] = v;
		v.i = i;
		v.j = j;
	    }
	}
	this.linear = linear;
	if (linear) {
	    int regionCode = 0;
	    for (int i = 0; i < nu; i++) {
		for (int j = 0; j < nv; j++) {
		    Vertex v = array[i][j];
		    v.region = regionCode++;
		}
	    }
	}
    }

    /**
     * Constructor.
     * The points on each patch are functions of two variables u and
     * v. Increasing u moves a point on the surface towards the next
     * highest first index and increasing v moves a point on the surface
     * towards the next highest second index.  Both u and v must be
     * in the range [0.0, 1.0].
     * <P>
     * When stored, the points provided will have their coordinates
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * @param points a two-dimensional array of points
     */
    public BezierGrid(Point3D[][] points) {
	this(points, false, false);
    }

    /**
     * Constructor for open or closed grids.
     * The points on each patch are functions of two variables u and
     * v. Increasing u moves a point on the surface towards the next
     * highest first index and increasing v moves a point on the
     * surface towards the next highest second index.  Both u and v
     * must be in the range [0.0, 1.0].  If the array is an n by m
     * array with indices [i][j], the points at i = n-1 should not
     * repeat the points at i = 0 to create a closed
     * surface&mdash;that will be done automatically when uclosed is
     * true. Similarly, when vclosed is true, the points at j = m-1
     * should not contain the same values as those at i = 0 as the
     * additional patches will be provided automatically.
     * <P>
     * When uclosed or vclosed is true, the splines used to create
     * the B&eacute;zier patches will be a smooth at all points
     * including the end points.
     * <P>
     * When stored, the points provided will have their coordinates
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * @param points a two-dimensional array of points
     * @param uclosed the grid is closed in the U direction
     * @param vclosed the grid is closed in the V direction
     */
    public BezierGrid(Point3D[][] points,
		      boolean uclosed, boolean vclosed)
    {
	this(points, uclosed, vclosed, false);
    }

    /**
     * Constructor for open or closed grids with possibily linear
     * connections between adjacent grid points.
     * The points on each patch are functions of two variables u and
     * v. Increasing u moves a point on the surface towards the next
     * highest first index and increasing v moves a point on the
     * surface towards the next highest second index.  Both u and v
     * must be in the range [0.0, 1.0].  If the array is an n by m
     * array with indices [i][j], the points at i = n-1 should not
     * repeat the points at i = 0 to create a closed
     * surface&mdash;that will be done automatically when uclosed is
     * true. Similarly, when vclosed is true, the points at j = m-1
     * should not contain the same values as those at i = 0 as the
     * additional patches will be provided automatically.
     * <P>
     * When uclosed or vclosed is true, the splines used to create
     * the B&eacute;zier patches will be a smooth at all points
     * including the end points.
     * <P>
     * When stored, the points provided will have their coordinates
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * When the argument <CODE>linear</COdE> is true, each grid point is
     * configured to have its own region. By default, each line connecting
     * two adjacent grid points will be a straight line.
     * @param points a two-dimensional array of points
     * @param uclosed the grid is closed in the U direction
     * @param vclosed the grid is closed in the V direction
     * @param linear true if lines connecting grid points are straight lines;
     *        false otherwise
     */
    public BezierGrid(Point3D[][] points,
		      boolean uclosed, boolean vclosed, boolean linear)
    {

	this(points.length, uclosed, points[0].length, vclosed, linear);
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		Point3D p = points[i][j];
		if (p != null) {
			p = new Point3D.Double((double)(float)p.getX(),
					       (double)(float)p.getY(),
					       (double)(float)p.getZ());
		}
		Vertex vertex = array[i][j];
		/*
		Vertex vertex = new Vertex();
		vertex.p = p;
		array[i][j] = vertex;
		*/
		vertex.p = p;
		vertex.filled = (p != null);
		// if (p == null) vertex.filled = false;
	    }
	}
    }

    /**
     * Constructor using real-valued functions.
     * The grid is a two-dimensional array whose values are points
     * in a three-dimensional space.  The first index refers to the 'U'
     * axis and the second index refers to the 'V' axis. The values
     * u and v are restricted to the range [0, 1) for a given pair.
     * of indices [i][j].  The real-valued functions are functions of
     * two arguments (s,t). Arrays sarray and tarray are one-dimensional
     * arrays whose indices are i and j respectively and whose values
     * are the corresponding values for s and t.
     * <P>
     * When stored, the points provided will have their coordinates
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * @param sarray the 's' values at grid points in increasing order
     * @param tarray the 't' values at grid points in increasing order
     * @param xfunct the function f<sub>x</sub>(s,t) providing the X
     *        coordinate at (s,t)
     * @param yfunct the function f<sub>y</sub>(s,t) providing the Y
     *        coordinate at (s,t)
     * @param zfunct the function f<sub>z</sub>(s,t) providing the Z
     *        coordinate at (s,t)
     */
    public BezierGrid(double[] sarray, double[] tarray,
		      RealValuedFunctTwoOps xfunct,
		      RealValuedFunctTwoOps yfunct,
		      RealValuedFunctTwoOps zfunct)
    {
	this(sarray, false, tarray, false, xfunct, yfunct, zfunct);
    }

    /**
     * Constructor using real-valued functions and specifying if the
     * U and V directions are cyclic or not.
     * The grid is a two-dimensional array whose values are points
     * in a three-dimensional space.  The first index refers to the 'U'
     * axis and the second index refers to the 'V' axis. The values
     * u and v are restricted to the range [0, 1) for a given pair.
     * of indices [i][j].  The real-valued functions are functions of
     * two arguments (s,t). Arrays sarray and tarray are one-dimensional
     * arrays whose indices are i and j respectively and whose values
     * are the corresponding values for s and t.
     * <P>
     * The argument uclosed, when true, indicates that the surface
     * corresponding to the grid is closed in the 'U' direction.
     * Similarly vclosed, when true, indicates that the surface
     * corresponding to the grid is closed in the 'V' direction.
     * When the grid is closed in the U direction, sarray's final
     * element must not repeat its initial element. Similarly
     * when the grid is closed in the V direction, tarray's final
     * element must not repeat its initial element.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * @param sarray the 's' values at grid points in increasing order
     * @param uclosed true if the U direction is closed, false otherwise
     * @param tarray the 't' values at grid points in increasing order
     * @param vclosed true if the V direction is closed, false otherwise
     * @param xfunct the function f<sub>x</sub>(s,t) providing the X
     *        coordinate at (s,t)
     * @param yfunct the function f<sub>y</sub>(s,t) providing the Y
     *        coordinate at (s,t)
     * @param zfunct the function f<sub>z</sub>(s,t) providing the Z
     *        coordinate at (s,t)
     */
    public BezierGrid(double[] sarray, boolean uclosed,
		      double[] tarray, boolean vclosed,
		      RealValuedFunctTwoOps xfunct,
		      RealValuedFunctTwoOps yfunct,
		      RealValuedFunctTwoOps zfunct)
    {
	this(sarray, uclosed, tarray, vclosed, false,
	     xfunct, yfunct, zfunct);
    }
    /**
     * Constructor using real-valued functions and specifying if the U
     * and V directions are cyclic or not, with possibly a linear
     * constraint for the line segments connecting adjacent points on
     * the grid.
     * <P>
     * The paths connecting adjectent grid points can be constrained
     * to be linear.  The grid is a two-dimensional array whose values
     * are points in a three-dimensional space.  The first index
     * refers to the 'U' axis and the second index refers to the 'V'
     * axis. The values u and v are restricted to the range [0, 1) for
     * a given pair.  of indices [i][j].  The real-valued functions
     * are functions of two arguments (s,t). Arrays sarray and tarray
     * are one-dimensional arrays whose indices are i and j
     * respectively and whose values are the corresponding values for
     * s and t.
     * <P>
     * The argument uclosed, when true, indicates that the surface
     * corresponding to the grid is closed in the 'U' direction.
     * Similarly vclosed, when true, indicates that the surface
     * corresponding to the grid is closed in the 'V' direction.
     * When the grid is closed in the U direction, sarray's final
     * element must not repeat its initial element. Similarly
     * when the grid is closed in the V direction, tarray's final
     * element must not repeat its initial element.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * When the argument <CODE>linear</COdE> is true, each grid point is
     * configured to have its own region. By default, each line connecting
     * two adjacent grid points will be a straight line.
     * @param sarray the 's' values at grid points in increasing order
     * @param uclosed true if the U direction is closed, false otherwise
     * @param tarray the 't' values at grid points in increasing order
     * @param vclosed true if the V direction is closed, false otherwise
     * @param linear true if lines connecting grid points are straight lines;
     *        false otherwise
     * @param xfunct the function f<sub>x</sub>(s,t) providing the X
     *        coordinate at (s,t)
     * @param yfunct the function f<sub>y</sub>(s,t) providing the Y
     *        coordinate at (s,t)
     * @param zfunct the function f<sub>z</sub>(s,t) providing the Z
     *        coordinate at (s,t)
     */
    public BezierGrid(double[] sarray, boolean uclosed,
		      double[] tarray, boolean vclosed,
		      boolean linear,
		      RealValuedFunctTwoOps xfunct,
		      RealValuedFunctTwoOps yfunct,
		      RealValuedFunctTwoOps zfunct)
    {
	this(sarray.length, uclosed, tarray.length, vclosed, linear);
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		double s = sarray[i];
		double t = tarray[j];
		Vertex vertex = array[i][j];
		boolean inXdomain = (xfunct instanceof RealValuedFunctionTwo)?
		    ((RealValuedFunctionTwo)xfunct).isInDomain(s, t): true;
		boolean inYdomain = (yfunct instanceof RealValuedFunctionTwo)?
		    ((RealValuedFunctionTwo)xfunct).isInDomain(s, t): true;
		boolean inZdomain = (zfunct instanceof RealValuedFunctionTwo)?
		    ((RealValuedFunctionTwo)xfunct).isInDomain(s, t): true;
		if (inXdomain && inYdomain && inZdomain) {
		    Point3D p =
			new Point3D.Double((double)(float)xfunct.valueAt(s, t),
					   (double)(float)yfunct.valueAt(s, t),
					   (double)(float)zfunct.valueAt(s, t));
		    vertex.p = p;
		    vertex.filled = true;
		    // array[i][j] = vertex;
		} else {
		    vertex.p = null;
		    vertex.filled = false;
		}
	    }
	}
    }

    /**
     * Constructor based on an existing B&eacute;zier grid.
     * Modifications to the existing grid by calls to
     * {@link #setSplineU(int,int,double[])},
     * {@link #setSplineV(int,int,double[])}, or
     * {@link #setRemainingControlPoints(int,int,double[])}
     * will not be copied to the new grid.  Other modifications
     * (additional splines and the removal of grid cells) will be
     * copied. The unary operator argument f, if not null, will
     * map a point on the existing grid to the corresponding point
     * on the new grid. If f is null, the two grids will share identical
     * grid points.
     * <P>
     * If, for example, a {@link BezierGrid} grid1 represents a 'top'
     * half of a sphere centered on (0,0,0), with the boundary of
     * grid1 having Z values of zero,
     * <BLOCKQUOTE><PRE><CODE>
     *    BezierGrid grid2 = new BezierGrid(grid1, true, (p) -&gt; {
     *         double z = p.getZ();
     *         if (z &gt; 0) z = -z;
     *         return new Point2D.Double(p.getX(), p.getY(), z);
     *       });
     *    ...
     *    Surface3D surface = new Surface3D.Double();
     *    surface.append(grid1);
     *    surface.append(grid2);
     * </CODE></PRE></BLOCKQUOTE>
     * will create the 'bottom' half of the sphere.  If the configuration
     * for grid 1 is changed (e.g., by adding splines to represent paths
     * with a constant 'latitude'), the corresponding changes will not
     * have to be made to grid 2.
     * <P>
     * While the grid points, regions, splines, and whether or not a patch
     * associated with a grid point has been removed are copied to the
     * new grid, explicitly-set control points are not copied.  As a result,
     * if the argument grid is a subgrid, or if the argument grid's methods
     * {@link #setSplineU(int,int,double[])},
     * {@link #setSplineV(int,int,double[])}, or
     * {@link #setRemainingControlPoints(int,int,double[])} have been called,
     * one may have to use these methods on the newly created grid to get
     * the desired results.  For example, consider the following statements:
     * <BLOCKQUOTE> <PRE><CODE>
     *   BezierGrid grid1 = new BezierGrid(array);
     *   BezierGrid grid2 = grid1.subgrid(10,10,6,2);
     *   BezierGrid grid3 = new BezierGrid(grid2, false, (p) -&gt; {
     *         return p;
     *       });
     * </CODE></PRE> </BLOCKQUOTE>
     * One might expect grid2 and grid 3 to be identical. They may not
     * be identical because the splines for grid2 are those computed
     * for grid1, but grid1 and grid3, except for some special cases,
     * compute their splines using different end points.
     * @param grid the existing grid
     * @param reverse true if this grid should have the opposite
     *        orientation from the existing grid; false if this grid
     *        should have the same orientation
     * @param f a unary operator to map an existing grid point into
     *        a new one.
     */
    public BezierGrid(BezierGrid grid, boolean reverse,
		      UnaryOperator<Point3D> f)
    {
	this(grid.nu, grid.uclosed, grid.nv, grid.vclosed);
	this.flipped = (reverse)? (!grid.flipped): grid.flipped;
	if (grid.rflip) {
	    rflip = grid.rflip;
	}
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		Vertex oldv = grid.array[i][j];
		Vertex newv = array[i][j];
		if (oldv.p != null) {
		    if (f == null) {
			newv.p = new Point3D.Double(oldv.p.getX(),
						    oldv.p.getY(),
						    oldv.p.getZ());
		    } else {
			newv.p = f.apply(oldv.p);
			newv.p.setLocation((double)(float)(newv.p.getX()),
					   (double)(float)(newv.p.getY()),
					   (double)(float)(newv.p.getZ()));
		    }
		}
		newv.color = oldv.color;
		newv.filled = oldv.filled;
		newv.region = oldv.region;
	    }
	}
	for (SplineDescriptor sd: grid.splineDescriptors) {
	    splineDescriptors.add(sd);
	}
    }

    /**
     * Constructor based on a 2D path.
     * A default method of the interface {@link Point3DMapper} first
     * converts the control points into a {@link Point3D} with the
     * same X and Y values, and a Z value of 0.0, and then calls the
     * method
     * {@link Point3DMapper#apply(int,Point3D,Point3DMapper.Type,Point3D...)}
     * on each control point to set the B&eacute;zier curves in the V direction,
     * repeated with the index indicating the position along the U direction.
     * The resulting grid will generate splines along the U direction but will
     * not alter the splines in the V direction.
     * <P>
     * If mapper makes the 2D path's Y coordinate the Z coordinate and
     * treats the 2D path's X coordinate (or a non-negative function
     * of that coordinate) as a radius, if the 2D path's shape is such
     * that traversing this path (when closed) is counterclockwise,
     * and if increasing the mapper's index results in a
     * counterclockwise rotation about the Z axis, then the surface's
     * orientation will be correct. Othewise the surface willhave to
     * be flipped.
     * <P>
     * When the argument mapper is an instance of {@link BezierGrid.Mapper},
     * the region will change as one moves along the U direction when
     * the 'wire' used to create the mapper has a linear segment or
     * when the normal vector is not continuous.
     * @param path the path
     * @param mapper the object that maps 2D points to 3D points
     * @param n the number of points along the U axis.
     * @param uclosed true if the U direction is closed, false otherwise
     * @see getMapper(Path3D, double[])
     */
    public BezierGrid(Path2D path, Point3DMapper<Point3D> mapper,
		      int n, boolean uclosed)
    {
	this(n, uclosed, Path2DInfo.numberOfDrawableKnots(path),
	     Path2DInfo.isClosed(path));

	PathIterator pi = path.getPathIterator(null);
	double startx = 0.0, starty = 0.0;
	double lastx = 0.0, lasty = 0.0;
	double[] coords = new double[6];
	double[] coords1 = new double[6];
	double[] coords2 = new double[6];
	int j = -1;
	Point2D.Double p0 = null;
	Point2D.Double p1 = null;
	Point2D.Double p2 = null;
	Point2D.Double p3 = null;
	int mcnt = 0;
	boolean cseen = false;
	while (!pi.isDone()) {
	    if (cseen) break;
	    if (mcnt > 1) break;
	    j++;
	    switch(pi.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		if (mcnt > 0) {
		    mcnt++;
		    continue;
		}
		startx = coords[0];
		starty = coords[1];
		p0 = new Point2D.Double(startx, starty);
		for (int i = 0; i < nu; i++) {
		    array[i][j].p =
			mapper.apply(i, p0, Point3DMapper.Type.KNOT);
		    array[i][j].filled = true;
		}
		lastx = startx;
		lasty = starty;
		p0 = new Point2D.Double(lastx, lasty);
		mcnt++;
		pi.next();
		j--;		// We use the last point for the grid point
		continue;
	    case PathIterator.SEG_LINETO:
		Path2DInfo.elevateDegree(1,coords1, lastx, lasty, coords);
		Path2DInfo.elevateDegree(2,coords2, lastx, lasty, coords1);
		break;
	    case PathIterator.SEG_QUADTO:
		Path2DInfo.elevateDegree(2,coords2, lastx, lasty, coords);
		break;
	    case PathIterator.SEG_CUBICTO:
		System.arraycopy(coords, 0, coords2, 0, 6);
		break;
	    case PathIterator.SEG_CLOSE:
		if (startx != lastx ||  starty != lasty) {
		    coords[0] = startx; coords[1] = starty;
		    Path2DInfo.elevateDegree(1,coords1, lastx, lasty, coords);
		    Path2DInfo.elevateDegree(2,coords2, lastx, lasty, coords1);
		}
		cseen = true;
		break;
	    }
	    p1 = new Point2D.Double(coords2[0], coords2[1]);
	    p2 = new Point2D.Double(coords2[2], coords2[3]);
	    p3 = new Point2D.Double(coords2[4], coords2[5]);
	    if (j == nv) {
		// happens on a CLOSE where no additional path is needed.
		break;
	    }
	    for (int i = 0; i < nu; i++) {
		array[i][j].vc = new double[9];
		Point3D p = mapper.apply(i, p0, Point3DMapper.Type.KNOT);
		p.setLocation((double)(float)p.getX(),
			      (double)(float)p.getY(),
			      (double)(float)p.getZ());
		array[i][j].p = p;
		array[i][j].filled = true;
		p = mapper.apply(i, p1, Point3DMapper.Type.FIRST_CUBIC, p0, p3);
		int k = 0;
		array[i][j].vc[k++] = (double)(float)p.getX();
		array[i][j].vc[k++] = (double)(float)p.getY();
		array[i][j].vc[k++] = (double)(float)p.getZ();
		p = mapper.apply(i, p2, Point3DMapper.Type.SECOND_CUBIC,
				 p0, p3);
		array[i][j].vc[k++] = (double)(float)p.getX();
		array[i][j].vc[k++] = (double)(float)p.getY();
		array[i][j].vc[k++] = (double)(float)p.getZ();
		p = mapper.apply(i, p3, Point3DMapper.Type.KNOT);
		array[i][j].vc[k++] = (double)(float)p.getX();
		array[i][j].vc[k++] = (double)(float)p.getY();
		array[i][j].vc[k++] = (double)(float)p.getZ();
	    }
	    p0 = p3;
	    lastx = p3.getX();
	    lasty = p3.getY();
	    pi.next();
	}
	j++;
	if (j < nv) {
	    // need a final point
	    for (int i = 0; i < nu; i++) {
		Point3D p = mapper.apply(i, p0, Point3DMapper.Type.KNOT);
		p.setLocation((double)(float)(p.getX()),
			      (double)(float)(p.getY()),
			      (double)(float)(p.getZ()));
		array[i][j].p = p;
		array[i][j].filled = true;
	    }
	}
	if (mapper instanceof Mapper) {
	    Mapper m = (Mapper) mapper;
	    for (int i = 0; i < nu; i++) {
		int regionCode = m.getRegion(i);
		setRegion(i, 0, 1, nv, regionCode);
	    }
	}
	frozenV = true;
    }

    private static class WireInfo {
	private static final Point3D origin = new Point3D.Double(0.0, 0.0, 0.0);
	private static final double xhat[] = {1.0, 0.0, 0.0};
	private static final double yhat[] = {0.0, 1.0, 0.0};

	WireInfo(double x, double y, double z) {
	    p = new Point3D.Double(x, y, z);
	    hasNormal = false;
	    /*
	    if (prev != null) {
		theta = prev.theta;
		itheta = prev.itheta;
		// ptheta = prev.ptheta;
	    }
	    */
	}

	void setVectors(double[] T, double[] N)
	{
	    hasNormal = true;
	    System.arraycopy(T, 0, this.T, 0, 3);
	    System.arraycopy(N, 0, this.N, 0, 3);
	    VectorOps.crossProduct(this.Bi, this.N, this.T);
	    at = AffineTransform3D.getMapInstance(origin, xhat, yhat, p,
						  N, Bi, theta);
	}

	Point3D p;
	boolean hasNormal;
	double deltaTheta = 0.0;
	boolean flipped = false;
	double theta = 0.0;
	double itheta = 0.0;
	// double ptheta = 0.0;
	double[] T = new double[3];
	double[] N = new double[3];
	double[] Bi = new double[3];
	double[] nextN = new double[3];
	double[] nextB = new double[3];

	AffineTransform3D at = null;

	int regionCode = 0;

    }

    /**
     * Interface for mapping one point in a two or three dimensional
     * space into a point in a three dimensional point, with
     * parameters indicating if the point is a control point.
     * When used to create a B&eacute;zier grid, this interface also indicates
     * the number of grid points in the U direction and whether the
     * U direction is closed.
     */
    public static interface Mapper extends Point3DMapper<Point3D> {
	/**
	 * Get the number of points in the U direction for the
	 * BezierGrid to be constructed.
	 * @return the number of points
	 */
	int getN();
	/**
	 * Determine if the BezierGrid to be constructed is closed in
	 * the U direction.
	 * @return true if the grid is closed in the U direction; false
	 *         otherwise
	 */
	boolean isClosed();

	/**
	 * Get the region for a given index.
	 * The region will depend on the U index only.
	 * regions start with 0 and are incremented each time a
	 * straight-line segment is seen.
	 * @param i the index
	 * @return the region
	 */
	int getRegion(int i);
    }

    /**
     * Get a mapper that will apply an indexed affine transform to
     * points along a 2D or 3D curve. The indexes range from 0 to N
     * where N is the value returned by
     * {@link Path3DInfo#numberOfDrawableSegments(Path3D) Path3DInfo.numberOfDrawableSegments(wire)}.  This mapper treats a two dimensional point (x, y) the
     * same as the three dimenional point (x, y, 0.0).
     * <P>
     * For each segment along the path 'wire' that starts with a unit tangent
     * vector T, a unit vector N perpendicular to T is set to a unit vector
     * in the direction T &times; (N&prime; &times; T), where N&prime; is
     * the previous value of N. The initial value of N&prime; is the
     * vector given by the argument inormal. For points along a 2 dimensional
     * path, an affine transform maps a unit vector in the X direction to N,
     * a unit vector in the Y direction to (N &times; T), and the point
     * (0,0) to the i<sup>th</sup> point along the 3D path <CODE>wire</CODE>,
     * excluding intermediate control points.
     * <P>
     * Straight-line segments along the 'wire' path are handled specially:
     * these are given a region that distinguishes these segments from
     * those that precede them or follow them so that straight line segments
     * in the 'wire' path correspond to cylindrical sections along the
     * generated surface.
     * <P>
     * The normal vector is represented by an array containing the vector's
     * X, Y and Z coordinates in that order, and its norm does not matter as
     * long as it is not zero.  The class {@link org.bzdev.math.VectorOps}
     * contains methods that can simplify the creation of normal vectors,
     * specifically
     * {@link org.bzdev.math.VectorOps#createUnitVector3(double,double)},
     * which uses spherical coordinates.
     * @param wire a 3D path, either open or closed
     * @param inormal the vector that provides an initial normal vector
     *        (ignored if a normal vector can be computed from the 'wire')
     * @return the mapper
     * @see org.bzdev.math.VectorOps#createVector(double...)
     * @see org.bzdev.math.VectorOps#createUnitVector(double...)
     * @see org.bzdev.math.VectorOps#createVector3(double,double,double)
     * @see org.bzdev.math.VectorOps#createUnitVector3(double,double)
     */
    public static Mapper getMapper(Path3D wire, double[] inormal)
    {
	double[] tangent = new double[3];
	double[] tmpv = new double[3];
	double[] normal = new double[3];
	boolean hasNormal =
	    Path3DInfo.getFirstTNB(wire, null, normal, null);
	boolean hasTangent = Path3DInfo.getStartingTangent(wire, tangent);
	if (!hasTangent) {
	    String msg = errorMsg("noStartingTangent");
	    throw new IllegalStateException(msg);
	}
	double[] prevNormal = new double[3];
	boolean first = true;
	if (hasNormal == false) {
	    if (inormal == null) {
		String msg = errorMsg("inormalNeeded");
		throw new IllegalStateException(msg);
	    }
	    tmpv = VectorOps.crossProduct(inormal, tangent);
	    VectorOps.crossProduct(normal, tangent, tmpv);
	    VectorOps.normalize(normal);
	    hasNormal = true;
	} else if (inormal != null) {
	    if (Path3DInfo.getStartingTangent(wire, tangent) == false) {
		String msg = errorMsg("noStartingTangent");
		throw new IllegalStateException(msg);
	    }
	    double dotprod = VectorOps.dotProduct(tangent, inormal);
	    if (Math.abs(dotprod - 1.0) < 1.e-10) {
		double dx = tangent[0];
		double dy = tangent[1];
		double dz = tangent[2];
		String msg = errorMsg("parallelT", dx, dy, dz);
		throw new IllegalStateException(msg);
	    }
	    tmpv = VectorOps.crossProduct(inormal, tangent);
	    VectorOps.crossProduct(normal, tangent, tmpv);
	    VectorOps.normalize(normal);
	}
	ArrayList<WireInfo> wlist = new ArrayList<>();
	// initialize so we can set lastInfo.theta
	WireInfo lastInfo = /*new WireInfo(null, 0.0, 0.0, 0.0)*/ null;
	PathIterator3D pi = wire.getPathIterator(null);
	double[] coords = new double[9];
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double[] tangent0 = new double[3];
	// normal0 and normal1 used to see if we need a new region.
	boolean hasNormal0 = false;
	double[] normal0  = new double[3];
	boolean hasNormal1 = false;
	double[] normal1 = new double[3];
	double[] cp = new double[3];
	int index = -1;
	boolean needLast = true;
	int regionCode = 0;
	while (!pi.isDone()) {
	    index++;
	    switch(pi.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		startx = coords[0];
		starty = coords[1];
		startz = coords[2];
		lastx = startx;
		lasty = starty;
		lastz = startz;
		// lastInfo = new WireInfo(lastx, lasty, lastz);
		break;
	    case PathIterator3D.SEG_LINETO:
		if (Path3DInfo.getTangent(0.0, tangent0, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_LINETO, coords)) {
		    VectorOps.crossProduct(cp, tangent, tangent0);
		    if (Math.abs(cp[0]) > 1.e-10
			|| Math.abs(cp[1]) > 1.e-10
			|| Math.abs(cp[2]) > 1.e-10) {
			String msg = errorMsg("tangentKink", index);
			throw new IllegalStateException(msg);
		    }
		} else {
		    String msg = errorMsg("noTangentForPath", index);
		    throw new IllegalStateException(msg);
		}
		regionCode++;
		lastInfo = new WireInfo(lastx, lasty, lastz);
		lastInfo.regionCode = regionCode;
		VectorOps.crossProduct(tmpv, normal, tangent0);
		VectorOps.crossProduct(normal, tangent0, tmpv);
		VectorOps.normalize(normal);
		lastInfo.setVectors(tangent0, normal);
		wlist.add(lastInfo);
		System.arraycopy(tangent0, 0, tangent, 0, 3);
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		regionCode++;
		hasNormal0 = false;
		hasNormal1 = false;
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (Path3DInfo.getTangent(0.0, tangent0, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_QUADTO, coords)) {
		    VectorOps.crossProduct(cp, tangent, tangent0);
		    if (Math.abs(cp[0]) > 1.e-10
			|| Math.abs(cp[1]) > 1.e-10
			|| Math.abs(cp[2]) > 1.e-10) {
			String msg = errorMsg("tangentKink", index);
			throw new IllegalStateException(msg);
		    }
		}
		hasNormal0 = Path3DInfo.getNormal(0.0, normal0, 0,
						  lastx, lasty, lastz,
						  PathIterator3D.SEG_QUADTO,
						  coords);
		if (hasNormal1) {
		    VectorOps.crossProduct(cp, normal0, normal1);
		    if (Math.abs(cp[0]) > .01
			|| Math.abs(cp[1]) > .01
			|| Math.abs(cp[2]) > .01) {
			regionCode++;
		    }
		}
		hasNormal1 = Path3DInfo.getNormal(1.0, normal1, 0,
						  lastx, lasty, lastz,
						  PathIterator3D.SEG_QUADTO,
						  coords);
		lastInfo = new WireInfo(lastx, lasty, lastz);
		lastInfo.regionCode = regionCode;
		VectorOps.crossProduct(tmpv, normal, tangent0);
		VectorOps.crossProduct(normal, tangent0, tmpv);
		VectorOps.normalize(normal);
		lastInfo.setVectors(tangent0, normal);
		wlist.add(lastInfo);
		hasTangent =
		    Path3DInfo.getTangent(1.0, tangent, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_QUADTO, coords);
		lastx = coords[3];
		lasty = coords[4];
		lastz = coords[5];
		// wlist.add(lastInfo);
		// lastInfo = new WireInfo(lastx, lasty, lastz);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (Path3DInfo.getTangent(0.0, tangent0, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_CUBICTO, coords)) {
		    VectorOps.crossProduct(cp, tangent, tangent0);
		    if (Math.abs(cp[0]) > 1.e-10
			|| Math.abs(cp[1]) > 1.e-10
			|| Math.abs(cp[2]) > 1.e-10) {
			String msg = errorMsg("tangentKink", index);
			throw new IllegalStateException(msg);
		    }
		    System.arraycopy(tangent0, 0, tangent, 0, 3);
		}
		hasNormal0 = Path3DInfo.getNormal(0.0, normal0, 0,
						  lastx, lasty, lastz,
						  PathIterator3D.SEG_CUBICTO,
						  coords);
		if (hasNormal1) {
		    VectorOps.crossProduct(cp, normal0, normal1);
		    if (Math.abs(cp[0]) > .01
			|| Math.abs(cp[1]) > .01
			|| Math.abs(cp[2]) > .01) {
			regionCode++;
		    }
		}
		hasNormal1 = Path3DInfo.getNormal(1.0, normal1, 0,
						  lastx, lasty, lastz,
						  PathIterator3D.SEG_CUBICTO,
						  coords);
		lastInfo = new WireInfo(lastx, lasty, lastz);
		lastInfo.regionCode = regionCode;
		VectorOps.crossProduct(tmpv, normal, tangent0);
		VectorOps.crossProduct(normal, tangent0, tmpv);
		VectorOps.normalize(normal);
		lastInfo.setVectors(tangent0, normal);
		wlist.add(lastInfo);
		hasTangent =
		    Path3DInfo.getTangent(1.0, tangent, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_CUBICTO, coords);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		hasTangent =
		    Path3DInfo.getTangent(0.0, tangent0, 0, lastx, lasty, lastz,
					  PathIterator3D.SEG_CLOSE, coords);
		if (startx != lastx && starty != lasty && startz != lastz) {
		    tangent0[0] = startx - lastx;
		    tangent0[1] = starty - lasty;
		    tangent0[2] = startz - lastz;
		    regionCode++;
		    lastInfo = new WireInfo(lastx, lasty, lastz);
		    lastInfo.regionCode = regionCode;
		    VectorOps.crossProduct(tmpv, normal, tangent0);
		    VectorOps.crossProduct(normal, tangent0, tmpv);
		    VectorOps.normalize(normal);
		    lastInfo.setVectors(tangent0, normal);
		    wlist.add(lastInfo);
		    lastInfo = new WireInfo(startx, starty, startz);
		} else {
		    needLast = false;
		}
		break;
	    }
	    pi.next();
	}
	if (needLast) {
	    lastInfo = new WireInfo(lastx, lasty, lastz);
	    lastInfo.regionCode = regionCode;
	    VectorOps.crossProduct(tmpv, normal, tangent);
	    VectorOps.crossProduct(normal, tangent, tmpv);
	    VectorOps.normalize(normal);
	    lastInfo.setVectors(tangent, normal);
	    wlist.add(lastInfo);
	}
	final WireInfo[] warray = new WireInfo[wlist.size()];
	wlist.toArray(warray);
	wlist = null;

	final boolean closed = Path3DInfo.isClosed(wire);
	final int nds = Path3DInfo.numberOfDrawableSegments(wire)
	    + ((closed)? 0: 1);
	return new Mapper() {
	    public Point3D apply(int i, Point3D p,
				 Point3DMapper.Type type, Point3D... bounds)
	    {
		Point3D result = new Point3D.Double();
		return warray[i].at.transform(p, result);
	    }
	    public int getN() {return nds;};
	    public boolean isClosed() {return closed;}

	    public int getRegion(int i) {
		return warray[i].regionCode;
	    }
	};
    }

    /**
     * Constructor given a 2D path and a Mapper.
     * The mapper can be created by {@link#getMapper(Path3D,double[])}
     * and that mapper will be used to create the points along the
     * grid. Control points for two-dimensional path <CODE>template</CODE>
     * will be mapped and set for control points along the V axis. The
     * number of points long the U axis is given by
     * {@link Mapper#getN()} and whether or not the U direction is
     * closed is determined by {@link Mapper#isClosed()}.
     * <P>
     * This constructor can be used to create 'wires' with the template
     * specifying the cross section, with
     * {@link #getMapper(Path3D,double[])} creating a mapper given the
     * wire's path.  When {@link #getMapper(Path3D,double[])} is used,
     * the point (0,0) will be mapped to a point on the 3D path provided
     * by <CODE>getMapper</CODE>, a unit vector pointing along X axis
     * in the 2 dimensional space containing the template will be
     * mapped to the unit normal vector N for a point on the wire's
     * path. Similarly a unit vector pointing along the Y axis in the
     * 2 dimensional space containing the template will be mapped to
     * the unit vector N &times; T, where T is the unit  tangent vector for the
     * same point on the wire's path.
     * @param template a two-dimensional path representing a cross section
     *        of a 'wire'
     * @param mapper an instance of  {@link BezierGrid.Mapper} that maps
     *        points on the template to points on the 'wire'
     */
    public BezierGrid(Path2D template, Mapper mapper) {
	this(template, mapper, mapper.getN(), mapper.isClosed());
    }

    /**
     * Transpose a BezierGrid.
     * This method returns a new grid with the U and V coordinates
     * swapped. The transposed grid will be in the same state as the
     * original.  Because of the way orientations are determined, a
     * transposed grid's orientation is opposite to that of the original
     * grid.
     * @return the transposed grid
     * @exception IllegalStateException the grid to be transposed was
     *            created as an extension to an existing grid or as
     *            a connection between existing grid boundaries; a
     *            user-defined spline was started but not finished
     */
    public BezierGrid transpose() {
	if (list.size() != 0) {
	    throw new IllegalStateException(errorMsg("incompleteSpline"));
	}
	BezierGrid transposed = new BezierGrid(nv, vclosed, nu, uclosed);
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		Vertex v = array[i][j];
		Vertex tv = transposed.array[j][i];
		tv.p = (v.p == null)? null: (Point3D)v.p.clone();
		tv.color = v.color;
	        if (v.vc != null) {
		    tv.uc = v.vc.clone();
		}
		if (v.uc != null) {
		    tv.vc = v.uc.clone();
		}
		tv.filled = v.filled;
		tv.region = v.region;
		if (v.rest != null) {
		    tv.rest = new double[12];
		    System.arraycopy(v.rest, 0, tv.rest, 0, 3);
		    System.arraycopy(v.rest, 3, tv.rest, 6, 3);
		    System.arraycopy(v.rest, 6, tv.rest, 3, 3);
		    System.arraycopy(v.rest, 9, tv.rest, 9, 3);
		}
		tv.vsn = v.usn;
		tv.usn = v.vsn;
	    }
	}
	transposed.flipped = flipped;
	transposed.frozen = frozen;
	transposed.frozenUEnds1 = frozenVEnds1;
	transposed.frozenUEnds2 = frozenVEnds2;
	transposed.frozenVEnds1 = frozenUEnds1;
	transposed.frozenVEnds2 = frozenUEnds2;
	transposed.frozenU = transposed.frozenV;
	transposed.frozenV = transposed.frozenU;
	transposed.oi = oj;
	transposed.oj = oi;
	transposed.ci = oj;
	transposed.cj = oi;
	for (SplineDescriptor sd: splineDescriptors) {
	    SplineDescriptor nsd = new SplineDescriptor();
	    nsd.points = new SplinePoint[sd.points.length];
	    for (int k = 0; k < sd.points.length; k++) {
		SplinePoint op = sd.points[k];
		SplinePoint np = new SplinePoint(op.j, op.i);
		nsd.points[k] = np;
	    }
	    nsd.cyclic = sd.cyclic;
	    nsd.splineID = sd.splineID;
	    transposed.splineDescriptors.add(nsd);
	    transposed.splinesCreated = splinesCreated;
	}
	return transposed;
    }

    /**
     * Create a subgrid of this B&eacute;zier grid.
     * A subgrid is never a closed grid in either the U or V directions,
     * or both.  The maximum size of a subgrid in either the U or V
     * directions is the corresponding size of this grid. When this
     * grid is not closed in the U direction and the size of this grid
     * in the U direction is nu, then n &le; nu-i. Similarly when this
     * grid is not closed in the U direction and the size of this grid
     * in the V direction is nv, then m &le; nv-i. The indices (i, j)
     * must satisfy i &isin; [0,nu) and j &isin; [0, nv). A subgrid
     * will copy this grid's splines and will be 'frozen' (i.e., the
     * subgrid's splines cannot be modified).
     * <P>
     * While a subgrid can be used to isolate a portion of a grid for
     * convenience, it's main purpose is to handle cases where the
     * full grid is not well formed.  For example, to create a M&ouml;bius
     * strip that can be 3D printed, one can start with a template
     * representing a rectangle, and apply an affine transformation to
     * create copies at points along the strip:
     * <BLOCKQUOTE><PRE><CODE>
     *   int N = 64;
     *   int M = 4;
     *
     *  Point3D[][] array = new Point3D[2*N][M];
     *  Point3D[] template = new Point3D[M];
     *
     *  double r = 100.0;
     *  double width = 50.0;
     *  double height = 10.0;
     *
     *  template[0] = new Point3D.Double(0.0, width/2, height/2);
     *  template[1] = new Point3D.Double(0.0, -width/2,height/2);
     *  template[2] = new Point3D.Double(0.0, -width/2,-height/2);
     *  template[3] = new Point3D.Double(0.0, width/2, -height/2);
     *
     *  for (int i = 0; i &lt; 2*N; i++) {
     *      double theta = (Math.PI * i)/N;
     *      double psi = (2 * Math.PI * i)/N;
     *      AffineTransform3D af =
     *      AffineTransform3D.getRotateInstance(0.0, 0.0, psi);
     *      af.translate(0.0, r, 0.0);
     *      af.rotate(0.0, theta, 0.0);
     *      for (int j = 0; j &lt; M; j++) {
     *          array[i][j] = af.transform(template[j], null);
     *      }
     *  }
     *  BezierGrid grid = new BezierGrid(array, true, true);
     *  for (int i = 0; i &lt; 2*N; i++) {
     *      for (int j = 0; j &lt; M; j++) {
     *          grid.setRegion(i, j, j);
     *      }
     *  }
     *  BezierGrid mgrid = grid.subgrid(0, 0, N+1, M+1);
     * </CODE></PRE></BLOCKQUOTE>
     * The U values for a full grid cover an angular extent of of
     * 4&pi; so that corresponding points match at the ends and so
     * that the splines in the U direction are cyclic. Because the V
     * values follow a rectangle, the region for each point is set to
     * the value of its V index, which forces all the edges in the V
     * direction to be linear. Unfortunately, the full grid has
     * multiple vertices that correspond to the same points, so this
     * grid is not well formed.  The use of a subgrid extracts a grid
     * corresponding to an angular extent of 2&pi;, which is just enough
     * for the desired M&ouml;bius strip.
     * @param i the  U index for an index of this grid that will
     *        correspond to the index (0,0) for the subgrid
     * @param j the V index for an index of this grid  that will
     *        correspond to the index (0,0) for the subgrid
     * @param n the number of grid points in the U direction for the
     *        subgrid
     * @param m the number of grid points in the V direction for the
     *        subgrid
     * @return the subgrid
     */
    public BezierGrid subgrid(int i, int j, int n, int m) {
	createSplines();
	if (uclosed) {
	    if (n > nu+1) {
		String msg = errorMsg("index", n, nu+2);
		throw new IllegalArgumentException(msg);
	    }
	} else if (n-i > nu) {
	    String msg = errorMsg("index2");
	    throw new IllegalArgumentException(msg);
	}
	if (vclosed) {
	    if (m > nv+1) {
		String msg = errorMsg("index", m, nv+2);
		throw new IllegalArgumentException(msg);
	    }
	} else if (m-j > nv) {
		String msg = errorMsg("index2");
	    throw new IllegalArgumentException(msg);
	}

	BezierGrid g = new BezierGrid(n, false, m, false);
	g.flipped = flipped;
	g.frozen = true;
	g.splinesCreated = true;

	int iii = i;
	for (int ii = 0; ii < n; ii++, iii++) {
	    if (uclosed && iii == nu) iii = 0;
	    int jjj = j;
	    for (int jj = 0; jj < m; jj++, jjj++) {
		if (vclosed && jjj == nv) jjj = 0;
		Point3D p = array[iii][jjj].p;
		g.array[ii][jj].p = new Point3D.Double
		    (p.getX(), p.getY(), p.getZ());
		g.array[ii][jj].region = array[iii][jjj].region;
		g.array[ii][jj].vsn = array[iii][jjj].vsn;
		g.array[ii][jj].usn = array[iii][jjj].usn;
		if (ii < n-1 && jj < m-1) {
		    g.array[ii][jj].color = array[iii][jjj].color;
		    g.array[ii][jj].filled = array[iii][jjj].filled;
		}
		if (ii < n-1) {
		    double[] uc = array[iii][jjj].uc;
		    if (uc != null) {
			double[] newuc = new double[uc.length];
			System.arraycopy(uc, 0, newuc, 0, uc.length);
			g.array[ii][jj].uc = newuc;
		    }
		}
		if (jj < m-1) {
		    double[] vc = array[iii][jjj].vc;
		    if (vc != null) {
			double[] newvc = new double[vc.length];
			System.arraycopy(vc, 0, newvc, 0, vc.length);
			g.array[ii][jj].vc = newvc;
		    }
		}
		if (ii < n-1 && jj < m-1) {
		    double[] rest = array[iii][jjj].rest;
		    if (rest != null) {
			double[] newrest = new double[rest.length];
			System.arraycopy(rest, 0, newrest, 0, rest.length);
			g.array[ii][jj].rest = newrest;
		    }
		}
	    }
	}
	return g;
    }


    /**
     * Get all the control points for the spline connecting a point
     * with indices (i, j) to a point with indices (i+1, j).
     * The coords array will contain the initial, first, second, and
     * final control points in that order. For each control point, the
     * array contains three successive values giving the control
     * point's X, Y, and Z coordinates respectively.
     * <P>
     * Intermediate control points will be set along all grid segments
     * as a side effect of calling this method.
     * @param i the U index
     * @param j the V index
     * @param coords and array to hold the results
     * @return true if the spline exists; false if it does not
     */
    public boolean getFullSplineU(int i, int j, double[] coords) {
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.uc == null) {
	    return false;
	}
	coords[0] = vertex.p.getX();
	coords[1] = vertex.p.getY();
	coords[2] = vertex.p.getZ();
	System.arraycopy(vertex.uc, 0, coords, 3, 9);
	return true;
    }

    /**
     * Get all the control points for the spline connecting a point
     * with indices (i, j) to a point with indices (i, j+1).
     * The coords array will contain the initial, first, second, and
     * final control points in that order.  For each control point,
     * the array contains three successive values giving the control
     * point's X, Y, and Z coordinates respectively.
     * <P>
     * Intermediate control points will be set along all grid segments
     * as a side effect of calling this method.
     * @param i the U index
     * @param j the V index
     * @param coords an array to hold the results
     * @return true if the spline exists; false if it does not
     */
    public boolean getFullSplineV(int i, int j, double[] coords) {
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null) {
	    return false;
	}
	coords[0] = vertex.p.getX();
	coords[1] = vertex.p.getY();
	coords[2] = vertex.p.getZ();
	System.arraycopy(vertex.vc, 0, coords, 3, 9);
	return true;
    }

    /**
     * Get the spline connecting a point with indices (i, j) to a point with
     * indices (i+1, j).
     * The coords array will contain the first, second, and final
     * control points in that order, but not the initial control
     * point.  For each control point, the array contains three
     * successive values giving the control point's X, Y, and Z
     * coordinates respectively.
     * <P>
     * Intermediate control points will be set along all grid segments
     * as a side effect of calling this method.
     * @param i the U index
     * @param j the V index
     * @param coords and array to hold the results
     * @return true if the spline exists; false if it does not
     */
    public boolean getSplineU(int i, int j, double[] coords) {
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.uc == null) {
	    return false;
	}
	System.arraycopy(vertex.uc, 0, coords, 0, 9);
	return true;
    }

    /**
     * Get the spline connecting a point with indices (i, j) to a point with
     * indices (i, j+1).
     * The coords array will contain the first, second, and final
     * control points in that order, but not the initial control
     * point.  For each control point, the array contains three
     * successive values giving the control point's X, Y, and Z
     * coordinates respectively.
     * <P>
     * Intermediate control points will be set along all grid segments
     * as a side effect of calling this method.
     * @param i the U index
     * @param j the V index
     * @param coords and array to hold the results
     * @return true if the spline exists; false if it does not
     */
    public boolean getSplineV(int i, int j, double[] coords) {
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null) {
	    return false;
	}
	System.arraycopy(vertex.vc, 0, coords, 0, 9);
	return true;
    }

    boolean frozen = false;
    boolean frozenUEnds1 = false;
    boolean frozenUEnds2 = false;
    // If either of the previous two are true, these must be false.
    // Similarly, if either of these is true, the previous two must be false.
    boolean frozenVEnds1 = false;
    boolean frozenVEnds2 = false;

    // Set when we construct a grid from a path.
    boolean frozenV = false;
    boolean frozenU = false;

    /**
     * Set the spline connecting a point with indices (i, j) to a point with
     * indices (i+1, j).
     * The array must have a length of at least 6, and will contain the
     * first and second control points, but not the initial or final
     * control points (if the final point is present in the array, that value
     * is ignored).  For each control point, the array contains three
     * successive values giving the control point's X, Y, and Z coordinates
     * respectively.
     * <P>
     * After this method is called, and successfully returns, splines
     * cannot be defined by sequences of methods starting with calls to
     * {@link #startSpline(int,int)} and grid points cannot be modified
     * (e.g, by calling {@link #setPoint(int,int,Point3D)}.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * This method returns false if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)},
     * with its 'linear' argument set to true.
     * @param i the U index
     * @param j the V index
     * @param coords the coordinates for the intermediate control points.
     * @return true if the spline is allowed for this point; false if it is not
     * @exception IllegalArgumentException the coords array is too short
     *            or i and/or j are out of range
     */
    public boolean setSplineU(int i, int j, double[] coords) {
	if (linear) return false;
	if (frozenU) return false;
	if (coords == null || coords.length < 6) {
	    throw new IllegalArgumentException(errorMsg("argarraylength"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	bpathSet = false;
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.uc == null) {
	    return false;
	}
	frozen = true;
	// System.arraycopy(coords, 0, vertex.uc, 0, 6);
	vertex.uc[0] = (double)(float)coords[0];
	vertex.uc[1] = (double)(float)coords[1];
	vertex.uc[2] = (double)(float)coords[2];
	vertex.uc[3] = (double)(float)coords[3];
	vertex.uc[4] = (double)(float)coords[4];
	vertex.uc[5] = (double)(float)coords[5];
	return true;
    }

    /**
     * Set the spline connecting a point with indices (i, j) to a point with
     * indices (i+1, j) so that the segment is a straight line.
     * After this method is called, and successfully returns, splines
     * cannot be defined by sequences of methods starting with calls to
     * {@link #startSpline(int,int)} and grid points cannot be modified
     * (e.g, by calling {@link #setPoint(int,int,Point3D)}.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * This method merely calls {@link #setSplineU(int,int,double[])} with
     * the appropriate argument, and should be used when a linear segment
     * will be attached to a planar triangle in order to ensure that the
     * tessellation will work as expected: otherwise small floating-point
     * errors can result in a surface that is not well formed.
     * <P>
     * This method returns false if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)},
     * with its 'linear' argument set to false.
     * @param i the U index
     * @param j the V index
     * @return true if the spline is allowed for this point; false if it is not
     * @exception IllegalArgumentException i and/or j are out of range
     */

    public boolean setLinearU(int i, int j) {
	if (linear) return false;
	if (frozenU) return false;
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	Vertex vertex0 = array[i][j];
	Vertex vertex3;
	if (i == nu-1) {
	    if (uclosed) {
		vertex3 = array[0][j];
	    } else {
		String msg = errorMsg("argOutOfRange2ii", i, j);
		throw new IllegalArgumentException(msg);
	    }
	} else {
	    vertex3 = array[i+1][j];
	}
	if (vertex0.p == null || vertex3.p == null) {
	    return false;
	}
	double[] pcoords = Path3D.setupCubic(vertex0.p, vertex3.p);
	double[] coords = new double[6];
	System.arraycopy(pcoords, 3, coords, 0, 6);
	return setSplineU(i, j, coords);
    }

    /**
     * Set the spline connecting a point with indices (i, j) to a point with
     * indices (i, j+1) so that the segment is a straight line.
     * After this method is called, and successfully returns, splines
     * cannot be defined by sequences of methods starting with calls to
     * {@link #startSpline(int,int)} and grid points cannot be modified
     * (e.g, by calling {@link #setPoint(int,int,Point3D)}.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * This method merely calls {@link #setSplineV(int,int,double[])} with
     * the appropriate argument, and should be used when a linear segment
     * will be attached to a planar triangle in order to ensure that the
     * tessellation will work as expected: otherwise small floating-point
     * errors can result in a surface that is not well formed.
     * <P>
     * This method returns false if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)},
     * with its 'linear' argument set to true.
     * @param i the U index
     * @param j the V index
     * @return true if the spline is allowed for this point; false if it is not
     * @exception IllegalArgumentException i and/or j are out of range
     */
    public boolean setLinearV(int i, int j) {
	if (linear) return false;
	if (frozenV) return false;
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	Vertex vertex0 = array[i][j];
	Vertex vertex3;
	if (j == nv-1) {
	    if (vclosed) {
		vertex3 = array[i][0];
	    } else {
		String msg = errorMsg("argOutOfRange2ii", i, j);
		throw new IllegalArgumentException(msg);
	    }
	} else {
	    vertex3 = array[i][j+1];
	}
	if (vertex0.p == null || vertex3.p == null) {
	    return false;
	}
	double[] pcoords = Path3D.setupCubic(vertex0.p, vertex3.p);
	double[] coords = new double[6];
	System.arraycopy(pcoords, 3, coords, 0, 6);
	return setSplineV(i, j, coords);
    }

    /**
     * Set the spline connecting a point with indices (i, j) to a point with
     * indices (i, j+1).
     * The array must have a length of at least 6, and will contain the
     * first and second control points, but not the initial or final
     * control points (if the final point is present in the array, that value
     * is ignored).  For each control point, the array contains three
     * successive values giving the control point's X, Y, and Z coordinates
     * respectively.
     * <P>
     * After this method is called, and successfully returns, splines
     * cannot be defined by sequences of methods starting with calls to
     * {@link #startSpline(int,int)} and grid points cannot be modified
     * (e.g, by calling {@link #setPoint(int,int,Point3D)}.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * This method returns false if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)}.
     * with its 'linear' argument set to true.
     * @param i the U index
     * @param j the V index
     * @param coords and array to hold the results
     * @return true if the spline is allowed for this point; false if it is not
     * @exception IllegalArgumentException the coords array is too short
     *            or i and/or j are out of range
     */
    public boolean setSplineV(int i, int j, double[] coords) {
	if (linear) return false;
	if (frozenV) return false;
	if (coords == null || coords.length < 6) {
	    throw new IllegalArgumentException(errorMsg("argarraylength"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	bpathSet = false;
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null) {
	    return false;
	}
	frozen = true;
	// System.arraycopy(coords, 0, vertex.vc, 0, 6);
	vertex.vc[0] = (double)(float)coords[0];
	vertex.vc[1] = (double)(float)coords[1];
	vertex.vc[2] = (double)(float)coords[2];
	vertex.vc[3] = (double)(float)coords[3];
	vertex.vc[4] = (double)(float)coords[4];
	vertex.vc[5] = (double)(float)coords[5];
	return true;
    }


    /**
     * Set the remaining control points for a patch.
     * The control points P<sub>11</sub>, P<sub>12</sub>, P<sub>21</sub>
     * and P<sub>22</sub> are specified by an array (rest) of length 12,
     * each stored so that the X value of a control point is followed by its
     * Y value and in turn by its Z value, with the control points listed
     * in the order shown.
     * <P>
     * If this method is not called for a pair of indices or the
     * control-point array is null, a default will be used to generate
     * the missing control points.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * <P>
     * This method returns false if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)}.
     * with its 'linear' argument set to true.
     * @param i the U index, which must be in the range [0,N) where N
     *        is the largest allowable U index
     * @param j the V index, which must be in the range[0,M) where M
     *        is the largest allowable V index
     * @param rest an array holding the remaining control points; null
     *        if these should be cleared
     * @return true if a patch exists for this point; false if it does not
     * @exception IllegalArgumentException an argument was out of range
     *            or the array was too small
     */
    public boolean setRemainingControlPoints(int i, int j, double[] rest)
	throws IllegalArgumentException
    {
	if (linear) return false;
	if (rest != null && rest.length < 12) {
	    throw new IllegalArgumentException(errorMsg("argarraylength"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	bpathSet = false;
	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null
	    || vertex.uc == null) {
	    return false;
	}
	frozen = true;
	if (rest == null) {
	    vertex.rest = null;
	    return true;
	}
	if (vertex.rest == null) {
	    vertex.rest = new double[12];
	}
	System.arraycopy(rest, 0, vertex.rest, 0, 12);
	for (int k = 0; k < 12; k++) {
	    vertex.rest[k] = (double)(float)vertex.rest[k];
	}
	return true;
    }

    /**
     * Get the remaining control points for a patch.
     * The control points P<sub>11</sub>, P<sub>12</sub>, P<sub>21</sub>
     * and P<sub>22</sub> are specified by an array (rest) of length 12,
     * each stored so that the X value of a control point is followed by its
     * Y value and in turn by its Z value, with the control points listed
     * in the order shown.
     * @param i the U index, which must be in the range [0,N) where N
     *        is the largest allowable U index
     * @param j the V index, which must be in the range[0,M) where M
     *        is the largest allowable V index
     * @param coords an array that will hold the remaining control points
     * @return true if a patch exists for this point with explicit
     *         values for the remaining control points; otherwise false
     * @exception IllegalArgumentException if an argument was out of
     *            range or the array was too small
     */
    public boolean getRemainingControlPoints(int i, int j, double[] coords)
	throws IllegalArgumentException
    {
	if (coords == null || coords.length < 12) {
	    throw new IllegalArgumentException(errorMsg("argarraylength"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}

	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null
	    || vertex.uc == null || vertex.rest == null) {
	    return false;
	}
	System.arraycopy(vertex.rest, 0, coords, 0, 12);
	return true;
    }

    /**
     * Get the current value of a point on the grid.
     * @param i the U index
     * @param j the V index
     * @return the corresponding point; null if there is none
     */
    public Point3D getPoint(int i, int j) {
	Vertex vertex = array[i][j];
	if (vertex == null) return null;
	return vertex.p;
    }

    /**
     * Get the coordinates for a cubic B&eacute;zier patch corresponding
     * to the specified grid coordinates for this B&eacute;zier grid.
     * The patch can be added directly to a surface: if this grid has
     * its orientation reversed, that will not be reflected in the patch
     * coordinates.
     * @param i the U index, which must be in the range [0,N) where N
     *        is the largest allowable U index
     * @param j the V index, which must be in the range[0,M) where M
     *        is the largest allowable V index
     * @param coords an array of length 48 or larger that will hold the
     *        coordinates for a patch
     * @return true if patch coordinates can be computed; otherwise false
     * @throws IllegalArgumentException the arguments are out of range
     *         or the coordinates array is too small
     */
    public boolean getPatch(int i, int j, double[] coords) {
	if (coords == null || coords.length < 48) {
	    throw new IllegalArgumentException(errorMsg("argarraylength"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}

	createSplines();
	Vertex vertex = array[i][j];
	if (vertex == null || vertex.p == null || vertex.vc == null
	    || vertex.uc == null) {
	    return false;
	}
	if (i == nu-1 && !uclosed) {
	    return false;
	}
	if (j == nv-1 && !vclosed) {
	    return false;
	}
	coords[0] = vertex.p.getX();
	coords[1] = vertex.p.getY();
	coords[2] = vertex.p.getZ();
	System.arraycopy(vertex.uc, 0, coords, 3, 6);
	System.arraycopy(vertex.vc, 0, coords, 12, 3);
	System.arraycopy(vertex.vc, 3, coords, 24, 3);
	// System.arraycopy(vertex.vc, 6, coords, 36, 3);
	boolean needRest = (vertex.rest == null);
	if (!needRest) {
	    // We have the value
	    System.arraycopy(vertex.rest, 0, coords, 15, 6);
	    System.arraycopy(vertex.rest, 6, coords, 27, 6);
	}
	if (j == nv-1) {
	    vertex = array[i][0];
	} else {
	    vertex = array[i][j+1];
	}
	if (vertex == null || vertex.p == null || vertex.uc == null) {
	    return false;
	}
	coords[36] = vertex.p.getX();
	coords[37] = vertex.p.getY();
	coords[38] = vertex.p.getZ();
	System.arraycopy(vertex.uc, 0, coords, 39, 6);
	if (i == nu-1) {
	    vertex = array[0][j];
	} else {
	    vertex = array[i+1][j];
	}
	if (vertex == null || vertex.p == null || vertex.vc == null) {
	    return false;
	}
	coords[9] = vertex.p.getX();
	coords[10] = vertex.p.getY();
	coords[11] = vertex.p.getZ();
	System.arraycopy(vertex.vc, 0, coords, 21, 3);
	System.arraycopy(vertex.vc, 3, coords, 33, 3);
	// System.arraycopy(vertex.vc, 6, coords, 45, 3);
	if (i == nu-1 && j == nv-1) {
	    vertex = array[0][0];
	} else if (i == nu-1) {
	    vertex = array[0][j];
	} else if (j == nv-1) {
	    vertex = array[i][0];
	} else {
	    vertex = array[i+1][j+1];
	}
	if (vertex != null && vertex.p != null) {
	    coords[45] = vertex.p.getX();
	    coords[46] = vertex.p.getY();
	    coords[47] = vertex.p.getZ();
	}
	if (needRest) {
	    // fill in the missing values
	    Surface3D.setupRestForPatch(coords);
	    for (int k = 15; k < 21; k++) {
		coords[k] = (double)(float)coords[k];
	    }
	    for (int k = 27; k < 33; k++) {
		coords[k] = (double)(float)coords[k];
	    }
	}
	return true;
    }

    /**
     * Set a point on a grid.
     * This method can be used to adjust the value of points on the
     * grid. It must be called before any call to the methods
     * {@link #setSplineU(int,int,double[])},
     * {@link #setSplineV(int,int,double[])}, or
     * {@link #setRemainingControlPoints(int,int,double[])}.
     * If a grid point already exists, its cell's status (empty or
     * filled) is unchanged.  Otherwise its cell status is set to filled
     * if the point is non null and empty if the point is null
     * @param i the U index
     * @param j the V index
     * @param p the point
     * @exception IllegalArgumentException an index was out of range
     * @exception IllegalStateException splines were previously set by calls to
     *            {@link #setSplineU(int,int,double[])},
     *            {@link #setSplineV(int,int,double[])}, or
     *            {@link #setRemainingControlPoints(int,int,double[])}
     * @see #startSpline(int,int)
     * @see #endSpline(boolean)
     */
    public void setPoint(int i, int j, Point3D p) 
	throws IllegalArgumentException, IllegalStateException
    {
	if (p == null) {
	    // test also performed in setPoint(int,int,double,double,double)
	    if (i < 0 || i >= nu || j < 0 || j >= nv) {
		String msg = errorMsg("argOutOfRange2ii", i, j);
		throw new IllegalArgumentException(msg);
	    }
	    if (frozen) {
		throw new
		    IllegalStateException(errorMsg("bgfrozen"));
	    }
	    Vertex vertex = array[i][j];
	    vertex.p = null;
	    vertex.filled = false;
	    bpathSet = false;
	    return;
	}
	setPoint(i, j, p.getX(), p.getY(), p.getZ());
    }

    /**
     * Set points at the corner of the cell associated with a grid location.
     * For each corner, if a grid point already exists, its cell's
     * status (empty or filled) is unchanged.  Otherwise its cell
     * status is set to filled.
     * @param i the U index
     * @param j the V index
     * @param coords the control points.
     * @see #getPatch(int,int,double[])
     */
    public void setPatchCorners(int i, int j, double[] coords)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i < 0 || i >= nu || j < 0 || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (frozen) {
	    throw new
		IllegalStateException(errorMsg("bgfrozen"));
	}
	Vertex vertex = array[i][j];
	if (vertex.p == null) {
	    vertex.p = new Point3D.Double((double)(float)coords[0],
					  (double)(float)coords[1],
					  (double)(float)coords[2]);
	    vertex.filled = true;
	} else {
	    vertex.p.setLocation((double)(float)coords[0],
				 (double)(float)coords[1],
				 (double)(float)coords[2]);
	}
	int ii = (uclosed)? ((i+1)%nu): i+1;
	int jj = (vclosed)? ((j+1)%nv): j+1;
	int im = (uclosed)? ((i+nu-1)%nu):i-1;
	int jm = (vclosed)? ((i+nv-1)%nv):j-1;
	if (ii < nu) {
	    vertex = array[ii][j];
	    if (vertex.p == null) {
		vertex.p =
		    new Point3D.Double((double)(float)coords[9],
				       (double)(float)coords[10],
				       (double)(float)coords[11]);
		vertex.filled = true;
	    } else {
		vertex.p.setLocation((double)(float)coords[9],
				     (double)(float)coords[10],
				     (double)(float)coords[11]);
	    }
	    if (jj < nv) {
		vertex = array[ii][jj];
		if (vertex.p == null) {
		    vertex.p =
			new Point3D.Double((double)(float)coords[45],
					   (double)(float)coords[46],
					   (double)(float)coords[47]);
		    vertex.filled = true;
		} else {
		    vertex.p.setLocation((double)(float)coords[45],
					 (double)(float)coords[46],
					 (double)(float)coords[47]);
		}
	    }
	}
	if (im >= 0) {
	    vertex = array[im][j];
	    if (vertex.uc != null) {
		System.arraycopy(coords, 0, vertex.uc, 6, 3);
	    }
	}
	if (jj < nv) {
	    vertex = array[i][jj];
	    if (vertex.p == null) {
		vertex.p =
		    new Point3D.Double((double)(float)coords[36],
				       (double)(float)coords[37],
				       (double)(float)coords[38]);
		vertex.filled = true;
	    } else {
		vertex.p.setLocation((double)(float)coords[36],
				     (double)(float)coords[37],
				     (double)(float)coords[38]);
	    }
	}
	if (jm >= 0) {
	    vertex = array[i][jm];
	    if (vertex.vc != null) {
		vertex.vc[6] = coords[0];
		vertex.vc[7] = coords[1];
		vertex.vc[8] = coords[2];
	    }
	}
	splinesCreated = false;
	bpathSet = false;
    }

    /**
     * Set control points for a cubic patch associated with a grid point.
     * For each corner, if a grid point already exists, its cell's
     * status (empty or filled) is unchanged.  Otherwise its cell
     * status is set to filled.
     * <P>
     * This throws an exception if the grid is a linear grid: one created
     * with the constructors
     * {@link BezierGrid#BezierGrid(int,boolean,int,boolean,boolean)},
     * {@link BezierGrid#BezierGrid(Point3D[][],boolean,boolean,boolean)}, or
     * {@link BezierGrid#BezierGrid(double[],boolean,double[],boolean,boolean,RealValuedFunctTwoOps,RealValuedFunctTwoOps,RealValuedFunctTwoOps)},
     * with its 'linear' argument set to true.
     * @param i the U index of the grid point
     * @param j the V index of the grid point
     * @param coords the control points.
     * @exception IllegalArgumentException if an argument is out of range
     * @exception IllegalStateException if a patch may not be set
     * @see #getPatch(int,int,double[])
     */
    public void setPatch(int i, int j, double[] coords)
	throws IllegalArgumentException, IllegalStateException
    {
	if (i < 0 || i >= nu || j < 0 || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (linear) {
	    throw  new IllegalStateException(errorMsg("linearWithPatch"));
	}
	bpathSet = false;
	createSplines();
	frozen = true;
	Vertex vertex = array[i][j];
	if (vertex.p == null) {
	    vertex.p = new Point3D.Double((double)(float)coords[0],
					  (double)(float)coords[1],
					  (double)(float)coords[2]);
	    vertex.filled = true;
	} else {
	    vertex.p.setLocation((double)(float)coords[0],
				 (double)(float)coords[1],
				 (double)(float)coords[2]);
	}
	int im = (uclosed)? ((i+nu-1)%nu): i-1;
	int jm = (vclosed)? ((j+nv-1)%nv): j-1;
	int ii = (uclosed)? ((i+1)%nu): i+1;
	int jj = (vclosed)? ((j+1)%nv): j+1;
	double[] tmp = new double[12];
	if (ii < nu) {
	    if (vertex.uc == null) vertex.uc = new double[9];
	    System.arraycopy(coords, 3, tmp, 0, 6);
	    setSplineU(i, j, tmp);
	    System.arraycopy(coords, 9, vertex.uc, 6, 3);
	    for (int k = 6; k < 9; k++) {
		vertex.uc[k] = (double)(float)vertex.uc[k];
	    }
	    vertex = array[ii][j];
	    if (vertex.p == null) {
		vertex.p =
		    new Point3D.Double((double)(float)coords[9],
				       (double)(float)coords[10],
				       (double)(float)coords[11]);
		vertex.filled = true;
	    } else {
		vertex.p.setLocation((double)(float)coords[9],
				     (double)(float)coords[10],
				     (double)(float)coords[11]);
	    }
	    if (jj < nv) {
		vertex = array[i][jj];
		System.arraycopy(coords, 39, tmp, 0, 6);
		if (vertex.uc == null) vertex.uc = new double[9];
		if (vertex.p == null) {
		    vertex.p =
			new Point3D.Double((double)(float)coords[36],
					   (double)(float)coords[37],
					   (double)(float)coords[38]);
		    vertex.filled = true;
		} else {
		    vertex.p.setLocation((double)(float)coords[36],
					 (double)(float)coords[37],
					 (double)(float)coords[38]);
		}
		setSplineU(i, jj, tmp);
		System.arraycopy(coords, 45, vertex.uc, 6, 3);
		for (int k = 6; k < 9; k++) {
		    vertex.uc[k] = (double)(float)vertex.uc[k];
		}
		vertex = array[ii][jj];
		if (vertex.p == null) {
		    vertex.p =
			new Point3D.Double((double)(float)coords[45],
					   (double)(float)coords[46],
					   (double)(float)coords[47]);
		    vertex.filled = true;
		} else {
		    vertex.p.setLocation((double)(float)coords[45],
					 (double)(float)coords[46],
					 (double)(float)coords[47]);
		}
		vertex = array[i][j];
		if (vertex.rest == null) {
		    vertex.rest = new double[12];
		}
		System.arraycopy(coords, 15, vertex.rest, 0, 6);
		System.arraycopy(coords, 27, vertex.rest, 6, 6);
		for (int k = 0; k < vertex.rest.length; k++) {
		    vertex.rest[k] = (double)(float)vertex.rest[k];
		}
	    }
	}
	if (im >= 0) {
	    vertex = array[im][j];
	    if (vertex.uc != null) {
		System.arraycopy(coords, 0, vertex.uc, 6, 3);
		for (int k = 0; k < 3; k++) {
		    vertex.uc[k] = (double)(float)vertex.uc[k];
		}
	    }
	}
	if (jj < nv) {
	    vertex = array[i][j];
	    System.arraycopy(coords, 12, tmp, 0, 3);
	    System.arraycopy(coords, 24, tmp, 3, 3);
	    if (vertex.vc == null) vertex.vc = new double[9];
	    setSplineV(i, j, tmp);
	    System.arraycopy(coords, 36, vertex.vc, 6, 3);
	    for (int k = 6; k < 9; k++) {
		vertex.vc[k] = (double)(float)vertex.vc[k];
	    }
	    vertex = array[i][jj];
	    if (vertex.p == null) {
		vertex.p =
		    new Point3D.Double((double)(float)coords[36],
				       (double)(float)coords[37],
				       (double)(float)coords[38]);
		vertex.filled = true;
	    } else {
		vertex.p.setLocation((double)(float)coords[36],
				     (double)(float)coords[37],
				     (double)(float)coords[38]);
	    }
	    if (ii < nu) {
		vertex = array[ii][j];
		System.arraycopy(coords, 21, tmp, 0, 3);
		System.arraycopy(coords, 33, tmp, 3, 3);
		if (vertex.vc == null) vertex.vc = new double[9];
		setSplineV(ii, j, tmp);
		System.arraycopy(coords, 45, vertex.vc, 6, 3);
		for (int k = 6; k < 9; k++) {
		    vertex.vc[k] = (double)(float)vertex.vc[k];
		}
	    }
	}
	if (jm >= 0) {
	    vertex = array[i][jm];
	    if (vertex.vc != null) {
		vertex.vc[6] = (double)(float)coords[0];
		vertex.vc[7] = (double)(float)coords[1];
		vertex.vc[8] = (double)(float)coords[2];
	    }
	}
    }

    /**
     * Set a point on a grid given x, y, and z coordinates for the point.
     * This method can be used to adjust the value of points on the
     * grid.  It must be called before any call to the methods
     * {@link #setSplineU(int,int,double[])},
     * {@link #setSplineV(int,int,double[])}, or
     * {@link #setRemainingControlPoints(int,int,double[])}.
     * <P>
     * When stored, the coordinates provided will have their values
     * rounded to the nearest 'float' value by first casting the values
     * to a float and then to a double.
     * If a grid point already exists, its cell's status (empty or
     * filled) is unchanged.  Otherwise its cell status is set to filled.
     * @param i the U index
     * @param j the V index
     * @param x the point's X coordinate
     * @param y the point's Y coordinate
     * @param z the point's Z coordinate
     * @exception IllegalArgumentException the coords array is too short
     *            or i and j are out of range
     * @exception IllegalStateException splines were previously set by calls to
     *            {@link #setSplineU(int,int,double[])},
     *            {@link #setSplineV(int,int,double[])}, or
     *            {@link #setRemainingControlPoints(int,int,double[])}
     */
    public void setPoint(int i, int j, double x, double y, double z) 
	throws IllegalArgumentException, IllegalStateException
    {
	if (i < 0 || i >= nu || j < 0 || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (frozen) {
	    throw new
		IllegalStateException(errorMsg("bgfrozen"));
	}

	Vertex vertex = array[i][j];
	if (vertex == null) {
	    vertex = new Vertex();
	    array[i][j] = vertex;
	}
	if (vertex.p == null) {
	    vertex.p = new Point3D.Double((double)(float)x,
				       (double)(float)y,
				       (double)(float) z);
	    vertex.filled = true;
	} else {
	    vertex.p.setLocation((double)(float)x,
				 (double)(float)y,
				 (double)(float)z);
	}
	brect = null;		// clear the bounding rectangle
	splinesCreated = false;
	bpathSet = false;
    }

    int oi = -1;
    int oj = -1;
    int ci = -1;
    int cj = -1;

    static class SplinePoint {
	int i;
	int j;
	SplinePoint(int i, int j) {
	    this.i = i;
	    this.j = j;
	}
    }

    static class SplineDescriptor {
	SplinePoint[] points;
	boolean cyclic;
	int splineID;
	// SplinePath3D path;
    }


    ArrayList<SplinePoint> list = new ArrayList<>();
    ArrayList<SplineDescriptor> splineDescriptors = new ArrayList<>();

    /**
     * Indicate the starting indices for a user-defined spline.
     * The indices i and j vary in the U and V directions respectively.
     * For example for fixed j, the value when u = 1.0 and v = 0.0
     * at grid element (i, j) is the same as the value  when = u 0.0 and
     * v = 0.0 at grid element(i+1, j).
     * @param i the first index
     * @param j the second index
     * @exception IllegalArgumentException the coords array is too short
     *            or i and j are out of range
     * @see #moveU(int)
     * @see #moveV(int)
     * @see #endSpline(boolean)
     */
    public void startSpline(int i, int j) throws IllegalArgumentException  {
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (linear) {
	    throw new
		IllegalStateException(errorMsg("linearStartSpline"));
	}
	if (frozen) {
	    throw new
		IllegalStateException(errorMsg("bgfrozen"));
	}
	list.clear();
	oi = i;
	oj = j;
	ci = i;
	cj = j;
	list.add(new SplinePoint(ci, cj));
    }

    /**
     * Add elements to a spline in the V direction.
     * The current index will change from (i, j) to (i, j+n).
     * The method does nothing if the argument is 0.
     * @param n a positive number when n elements are added to the spline
     *       in the positive V direction; a negative number when -n
     *       elements are added to the spline in the negative V direction
     * @exception IllegalArgumentException the argument was out of range
     * @exception IllegalStateException a spline has not been stated
     * @see #startSpline(int,int)
     * @see #moveU(int)
     * @see #endSpline(boolean)
     */
    public void moveV(int n) {
	if (n == 0) return;
	int next = cj + n;
	if (next < 0 || next >= nv) {
	    String msg = errorMsg("argOutOfRange1i", n);
	    throw new IllegalArgumentException(msg);
	}
	int incr = (n < 0)? -1: 1;

	if (list.size() == 0) {
	    if (ci == -1) {
		throw new IllegalStateException(errorMsg("splineNotStarted"));
	    }
	    list.add(new SplinePoint(ci, cj));
	}
	do {
	    cj += incr;
	    list.add(new SplinePoint(ci, cj));
	} while (cj != next);
    }

    /**
     * Add elements to a spline in the U direction.
     * The current index will change from (i, j) to (i+n, j).
     * The method does nothing if the argument is 0.
     * @param n a positive number when n elements are added to the spline
     *       in the positive U direction; a negative number when -n
     *       elements are added to the spline in the negative U direction
     * @exception IllegalArgumentException the argument was out of range
     * @exception IllegalStateException a spline has not been stated
     * @see #startSpline(int,int)
     * @see #moveV(int)
     * @see #endSpline(boolean)
     */
    public void moveU(int n) {
	if (n == 0) return;
	int next = ci + n;
	if (next < 0 || next >= nu) {
	    String msg = errorMsg("argOutOfRange1i", n);
	    throw new IllegalArgumentException(msg);
	}
	int incr = (n < 0)? -1: 1;
	if (list.size() == 0) {
	    if (ci == -1) {
		throw new IllegalStateException(errorMsg("splineNotStarted"));
	    }
	    list.add(new SplinePoint(ci, cj));
	}
	do {
	    ci += incr;
	    list.add(new SplinePoint(ci, cj));
	} while (ci != next);
    }

    /**
     * Assert that the end of a user-defined spline has been reached.
     * For a non-cyclic spline, the current point for the spline
     * is the end point of the spline.  For a cyclic spline,
     * points will be added in either the U or V direction, but
     * not both, until the spline is completed
     * @param cyclic true if the spline is cyclic, false otherwise
     * @exception IllegalStateException if the spline is cyclic and cannot be
     *            completed by moving in the U direction or the V direction
     *            but not both, or if a spline has not been started
     * @see #startSpline(int,int)
     * @see #moveU(int)
     * @see #moveV(int)
     */
    public void endSpline(boolean cyclic) throws IllegalStateException {
	splinesCreated = false;
	bpathSet = false;
	int size = list.size();
	if (size == 0) {
	    throw new IllegalStateException(errorMsg("emptySpline"));
	}
	if (size == 1) {
	    // Just a single point doesn't do anything useful, so we
	    // just delete the entry.
	    list.clear();
	    return;
	}
	bpath = null;
	if (cyclic) {
	    int sz = list.size();
	    if (oi != ci && oj != cj) {
		String msg = errorMsg("cannotCloseSpline", oi, ci, oj, cj);
		throw new IllegalStateException(msg);
	    } else if (oi == ci) {
		if (cj != oj) {
		    moveV(oj - cj);
		}
	    } else if (oj == cj) {
		moveU(oi - ci);
	    }
	} else {
	    oi = ci;
	    oj = cj;
	}
	SplineDescriptor sd = new SplineDescriptor();
	sd.cyclic = cyclic;
	sd.points = new SplinePoint[size];
	list.toArray(sd.points);
	splineDescriptors.add(sd);
	list.clear();
	ci = -1; cj = -1;
	oi = -1; oj = -1;
	splinesCreated = false;
	bpathSet = false;
    }

    boolean splinesCreated = false;

    /**
     * Print the points on this grid.
     * The output will first indicate if the grid is closed in the
     * U and V directions.  This will be followed by the string "grid:"
     * followed by a series of lines showing each point as an ordered
     * triplet of X, Y, and Z coordinates, or the string "(null)" if
     * undefined. Each line corresponds to a successively higher U
     * coordinate, with points on each line in ascending order for their
     * V coordinates. This is followed by a line containing the string
     * "spline status", followed by lines containing the strings
     * <UL>
     *    <LI> (uv) if a grid point has both U and V intermediate
     *         control points defined.
     *    <LI> (u&nbsp;) if a grid point has only U intermediate
     *         control points defined.
     *    <LI> (&nbsp;v) if a grid point has only V intermediate
     *         control points defined.
     *    <LI> (&nbsp;&nbsp;) if a grid point has no intermediate
     *         control points defined
     * </UL>
     * This information uses the same ordering as the grid coordinates.
     * These lines are followed by a line containing the string
     * "filled status:", in turn followed by a series of lines containing
     * <UL>
     *    <LI> (F) if a grid point's cell is filled (i.e., part of a
     *         surface.)
     *    <LI> ( ) if a grid point's cell is not filled (in which case it
     *         is part of a boundary.)
     * </UL>
     * As with the other lines, these are ordered so that successive lines
     * are in ascending order for U values and each line is in ascending
     * order for V values.
     * @exception RuntimeException an IO error occurred
     */
    public void print() {
	print(System.out);
    }


    /**
     * Print the points on this grid, using an Appendable for output.
     * <P>
     * This method calls {@link createSplines()} if it has not already
     * been called, but the state is restored
     * so that {@link createSplines()} can be called subsequently. There
     * is, however, a side effect: the intermediate control points for
     * each spline will be set.
     * <P>
     * The output will first indicate if the grid is closed in the
     * U and V directions.  This will be followed by the string "grid:"
     * followed by a series of lines showing each point as an ordered
     * triplet of X, Y, and Z coordinates, or the string "(null)" if
     * undefined. Each line corresponds to a successively higher U
     * coordinate, with points on each line in ascending order for their
     * V coordinates. This is followed by a line containing the string
     * "spline status", followed by lines containing the strings
     * <UL>
     *    <LI> (uv) if a grid point has both U and V intermediate
     *         control points defined.
     *    <LI> (u&nbsp;) if a grid point has only U intermediate
     *         control points defined.
     *    <LI> (&nbsp;v) if a grid point has only V intermediate
     *         control points defined.
     *    <LI> (&nbsp;&nbsp;) if a grid point has no intermediate
     *         control points defined
     * </UL>
     * This information uses the same ordering as the grid coordinates.
     * These lines are followed by a line containing the string
     * "filled status:", in turn followed by a series of lines containing
     * <UL>
     *    <LI> (F) if a grid point's cell is filled (i.e., part of a
     *         surface.)
     *    <LI> ( ) if a grid point's cell is not filled (in which case it
     *         is part of a boundary.)
     * </UL>
     * As with the other lines, these are ordered so that successive lines
     * are in ascending order for U values and each line is in ascending
     * order for V values.
     * @param out the output
     * @exception RuntimeException an IO error occurred
     */
    public void print(Appendable out) {
	print("", out);
    }

    /**
     * Print the points on this grid, specifying a prefix to be printed
     * at the start of each line.
     * <P>
     * This method calls {@link createSplines()} if it has not already
     * been called, but the state is restored
     * so that {@link createSplines()} can be called subsequently. There
     * is, however, a side effect: the intermediate control points for
     * each spline will be set.
     * The output will first indicate if the grid is closed in the
     * U and V directions.  This will be followed by the string "grid:"
     * followed by a series of lines showing each point as an ordered
     * triplet of X, Y, and Z coordinates, or the string "(null)" if
     * undefined. Each line corresponds to a successively higher U
     * coordinate, with points on each line in ascending order for their
     * V coordinates. This is followed by a line containing the string
     * "spline status", followed by lines containing the strings
     * <UL>
     *    <LI> (uv) if a grid point has both U and V intermediate
     *         control points defined.
     *    <LI> (u&nbsp;) if a grid point has only U intermediate
     *         control points defined.
     *    <LI> (&nbsp;v) if a grid point has only V intermediate
     *         control points defined.
     *    <LI> (&nbsp;&nbsp;) if a grid point has no intermediate
     *         control points defined
     * </UL>
     * This information uses the same ordering as the grid coordinates.
     * These lines are followed by a line containing the string
     * "filled status:", in turn followed by a series of lines containing
     * <UL>
     *    <LI> (F) if a grid point's cell is filled (i.e., part of a
     *         surface.)
     *    <LI> ( ) if a grid point's cell is not filled (in which case it
     *         is part of a boundary.)
     * </UL>
     * As with the other lines, these are ordered so that successive lines
     * are in ascending order for U values and each line is in ascending
     * order for V values.
     * @param prefix the prefix
     * @exception RuntimeException an IO error occurred
     */
    public void print(String prefix) {
	print(prefix, System.out);
    }

    /**
     * Print the points on this grid, using an Appendable for output
     * and  specifying a prefix to be printed at the start of each line.
     * <P>
     * This method calls {@link createSplines()} if it has not already
     * been called, but the state is restored
     * so that {@link createSplines()} can be called subsequently. There
     * is, however, a side effect: the intermediate control points for
     * each spline will be set.
     * <P>
     * The output will first indicate if the grid is closed in the
     * U and V directions.  This will be followed by the string "grid:"
     * followed by a series of lines showing each point as an ordered
     * triplet of X, Y, and Z coordinates, or the string "(null)" if
     * undefined. Each line corresponds to a successively higher U
     * coordinate, with points on each line in ascending order for their
     * V coordinates. This is followed by a line containing the string
     * "spline status", followed by lines containing the strings
     * <UL>
     *    <LI> (uv) if a grid point has both U and V intermediate
     *         control points defined.
     *    <LI> (u&nbsp;) if a grid point has only U intermediate
     *         control points defined.
     *    <LI> (&nbsp;v) if a grid point has only V intermediate
     *         control points defined.
     *    <LI> (&nbsp;&nbsp;) if a grid point has no intermediate
     *         control points defined
     * </UL>
     * This information uses the same ordering as the grid coordinates.
     * These lines are followed by a line containing the string
     * "filled status:", in turn followed by a series of lines containing
     * <UL>
     *    <LI> (F) if a grid point's cell is filled (i.e., part of a
     *         surface.)
     *    <LI> ( ) if a grid point's cell is not filled (in which case it
     *         is part of a boundary.)
     * </UL>
     * As with the other lines, these are ordered so that successive lines
     * are in ascending order for U values and each line is in ascending
     * order for V values.
     * @param prefix the prefix
     * @param out the output
     * @exception RuntimeException an IO error occurred
     */
    public void print(String prefix, Appendable out) {
	boolean savedCreated = splinesCreated;
	boolean result = false;
	try {
	    if (!splinesCreated) createSplines();
	    out.append(String.format("%suclosed = %b, vclosed = %b\n",
				     prefix,
				     uclosed, vclosed));
	    out.append(String.format("%sgrid:\n", prefix));
	    for (int i = 0; i < nu; i++) {
		out.append(prefix);
		for (int j = 0; j < nv; j++) {
		    Vertex v = array[i][j];
		    if (v.p == null) {
			out.append(" (null)");
		    } else {
			out.append(String.format(" (%g,%g,%g)",
						 v.p.getX(),
						 v.p.getY(),
						 v.p.getZ()));
		    }
		}
		out.append("\n");
	    }
	    out.append(String.format("%sspline status:\n", prefix));
	    for (int i = 0; i < nu; i++) {
		out.append(prefix);
		for (int j = 0; j < nv; j++) {
		    Vertex v = array[i][j];
		    out.append(String.format(" (%c%c)",
					     ((v.uc==null)? ' ': 'u'),
					     ((v.vc==null)? ' ': 'v')));
		}
		out.append("\n");
	    }
	    out.append(String.format("%sFilled status:\n", prefix));
	    for (int i = 0; i < nu; i++) {
		out.append(prefix);
		for (int j = 0; j < nv; j++) {
		    Vertex v = array[i][j];
		    out.append(String.format(" (%c)",
					     ((v.filled)? 'F': ' ')));
		}
		out.append("\n");
	    }
	} catch (IOException eio) {
	    String msg = errorMsg("noOut");
	    throw new RuntimeException(msg, eio);
	} finally {
	    splinesCreated = savedCreated;
	    if (splinesCreated == false) {
		for (int i = 0; i < nu; i++) {
		    for (int j = 0; j < nv; j++) {
			Vertex v = array[i][j];
			if ((frozenUEnds1 && i == 0)
			    || (frozenUEnds2 && i == nu-1)) {
			    v.uc = null;
			} else if ((frozenVEnds1 && j == 0)
				   || (frozenVEnds2 && j == nv-1)) {
			    v.vc = null;
			} else {
			    if (!frozenV) {
				v.vc = null;
			    }
			    if (!frozenU) {
				v.uc = null;
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Print the sequence of indices for explicitly added splines.
     * The output will list the splines in the sequence in which they
     * were defined, and whether the splines are cyclic or not. Sequences
     * of index pairs in which more than one have an index in common are
     * separated by " ... "; otherwise by ", ".
     * @exception RuntimeException an IO error occurred
     */
    public void printSplines() {
	printSplines("", System.out);
    }

    /**
     * Print the sequence of indices for explicitly added splines to
     * an Appendable
     * The output will list the splines in the sequence in which they
     * were defined, and whether the splines are cyclic or not. Sequences
     * of index pairs in which more than one have an index in common are
     * separated by " ... "; otherwise by ", ".
     * @param out the Appendable used for output.
     * @exception RuntimeException an IO error occurred
     */
    public void printSplines(Appendable out) {
	printSplines("", out);
    }

    /**
     * Print the sequence of indices for explicitly added splines to
     * an Appendable and with a prefix
     * The output will list the splines in the sequence in which they
     * were defined, and whether the splines are cyclic or not. Sequences
     * of index pairs in which more than one have an index in common are
     * separated by " ... "; otherwise by ", ".
     * The prefix will typically be used in cases where extra indentation is
     * useful.
     * @param prefix a prefix to place at the start of each line
     * @param out the Appendable used for output.
     * @exception RuntimeException an IO error occurred
     */
    public void printSplines(String prefix, Appendable out) {
	createSplines();
	try {
	    for (SplineDescriptor sd: splineDescriptors) {
		String cmsg;
		if (sd.cyclic) {
		    cmsg = (errorMsg("cyclic"));
		} else {
		    cmsg = (errorMsg("nonCyclic"));
		}
		out.append(prefix + errorMsg("spline", sd.splineID, cmsg));
		out.append(": ");
		int length = sd.points.length;
		int limit = sd.points.length-2;
		for (int i = 0; i < length; i++) {
		    if (i > 0) out.append(", ");
		    int ci = sd.points[i].i;
		    int cj = sd.points[i].j;
		    out.append(String.format("(%d, %d)", ci, cj));
		    boolean again1 = true;
		    boolean again2 = true;
		    while (i < limit) {
			if (again1 == false && again2 == false) break;
			if (sd.points[i+1].j == cj
			    && sd.points[i+2].j == cj) {
			    int ii = i;
			    while (ii < length) {
				if (sd.points[ii].j == cj) {
				    ii++;
				} else {
				    break;
				}
			    }
			    if (ii == length || sd.points[ii].j != cj) {
				ii--;
			    }
			    ci = sd.points[ii].i;
			    i = ii;
			    out.append(String.format(" ... (%d, %d)", ci, cj));
			    again1 = true;
			} else {
			    again1 = false;
			}

			if (sd.points[i+1].i == ci
			    && sd.points[i+2].i == ci) {
			    int ii = i;
			    while (ii < length) {
				if (sd.points[ii].i == ci) {
				    ii++;
				} else {
				    break;
				}
			    }
			    if (ii == length || sd.points[ii].i != ci) {
				ii--;
			    }
			    cj = sd.points[ii].j;
			    i = ii;
			    out.append(String.format(" ... (%d, %d)", ci, cj));
			    again2 = true;
			} else {
			    again2 = false;
			}
		    }
		}
		out.append("\n");
	    }
	} catch (IOException eio) {
	    String msg = errorMsg("noOut");
	    throw new RuntimeException(msg, eio);
	}
    }


    /**
     * Test if a spline was created with inappropriate values.
     * This method is intended for debugging.
     * Note: if the X, Y, or Z coordinate of a point on the grid
     * has the value Double.NaN, this method may report an error.
     * One can check the values at adjacent points on the grid to
     * help detect this condition.
     * <P>
     * This method calls {@link createSplines()} if it has not already
     * been called, but the state is restored
     * so that {@link createSplines()} can be called subsequently. There
     * is, however, a side effect: the intermediate control points for
     * each spline will be set.
     * @param out the output to use for printing messages
     * @return true if a spline was created with inconsistent values;
     *         false if the splines are acceptable
     * @exception RuntimeException an IO error occurred
     */
    public boolean badSplines(Appendable out) {
	boolean savedCreated = splinesCreated;
	boolean result = false;
	Vertex vertex;
	try {
	    for (int j = 0; j < nv; j++) {
		for (int i = 0; i < nu; i++) {
		    vertex = array[i][j];
		    if (vertex.p != null) {
			if (Double.isNaN(vertex.p.getX())
			    || Double.isNaN(vertex.p.getY())
			    || Double.isNaN(vertex.p.getZ())){
			    out.append(errorMsg("vertexNaN", i, j));
			}
		    }
		}
	    }

	    if (!splinesCreated) createSplines();
	    int num1 = uclosed? nu: nu - 1;
	    int nvm1 = vclosed? nv: nv - 1;
	    Vertex vertex2;
	    Vertex vertex3;
	    for (int j = 0; j < nv; j++) {
		for (int i = 0; i < num1; i++) {
		    vertex = array[i][j];
		    vertex2 = array[(i+1)%nu][j];
		    if (vertex.p != null) {
			if (vertex2.p != null) {
			    if (vertex.uc == null) {
				if (out != null) {
				    out.append(errorMsg("vertexnuc", i, j));
				    out.append("\n");
				}
				
				result = true;
			    } else {
				if (vertex.uc[6] != vertex2.p.getX()
				    || vertex.uc[7] != vertex2.p.getY()
				    || vertex.uc[8] != vertex2.p.getZ()) {
				    if (out != null) {
					out.append(errorMsg("vertexuc", i, j));
					out.append("\n");
				    }
				    result = true;
				}
			    }
			}
		    }
		}
		if (!uclosed) {
		    if (array[num1][j].uc != null) {
			if (out != null) {
			    out.append(errorMsg("vertexnnuc", num1, j));
			    out.append("\n");
			}
			result = true;
		    }
		}
	    }
	    for (int i = 0; i < nu; i++) {
		for (int j = 0; j < nvm1; j++) {
		    vertex = array[i][j];
		    vertex3 = array[i][(j+1)%nv];
		    if (vertex.p != null) {
			if (vertex3.p != null) {
			    if (vertex.vc == null) {
				if (out != null) {
				    out.append(errorMsg("vertexnvc", i, j));
				    out.append("\n");
				}
				result = true;
			    } else {
				if (vertex.vc[6] != vertex3.p.getX()
				    || vertex.vc[7] != vertex3.p.getY()
				    || vertex.vc[8] != vertex3.p.getZ()) {
				    if (out != null) {
					out.append(errorMsg("vertexvc", i, j));
					out.append("\n");
				    }
				    result = true;
				}
			    }
			}
		    }
		}
		if (!vclosed) {
		    if (array[i][nvm1].vc != null) {
			if (out != null) {
			    out.append(errorMsg("vertexnnvc", i, nvm1));
			    out.append("\n");
			}
			result = true;
		    }
		}
	    }
	} catch (IOException eio) {
	    String msg = errorMsg("noOut");
	    throw new RuntimeException(msg, eio);
	} finally {
	    splinesCreated = savedCreated;
	    if (splinesCreated == false) {
		for (int i = 0; i < nu; i++) {
		    for (int j = 0; j < nv; j++) {
			Vertex v = array[i][j];
			if (!frozenU) {
			    v.uc = null;
			}
			if (!frozenV) {
			    v.vc = null;
			}
		    }
		}
	    }
	}
	return result;
    }


    boolean[] splineStatus = null;

    boolean traceSplines = false;
    Appendable splout = null;

    /**
     * Trace calls to createSpline, indicating which grid points have
     * control points added to them.
     * This method is useful primarily for debugging.
     * If IO errors occur, the output will be silently dropped.
     * @param out an Appendable that will store the trace.
     */
    public void traceSplines(Appendable out) {
	splout = out;
    }

    private void sploutAppend(String s) {
	try {
	    splout.append(s);
	    splout.append("\n");
	    if (splout instanceof Flushable) {
		((Flushable)splout).flush();
	    }
	} catch (Exception e) {}
    }


    /**
     * Create all splines.
     * A standard set of splines will be created first, followed by
     * user-defined splines. If called more than once, the additional
     * calls are ignored unless a vertex is changed or a new spline is
     * added.
     * <P>
     * When stored, the coordinates of control points will have their
     * values rounded to the nearest float value by first casting a
     * value as a float and then casting the result back to a double.
     * @exception IllegalStateException a spline could not be created or
     *            was ill formed
     */
    public void createSplines() throws IllegalStateException {
	if (frozen || splinesCreated) return;
	bpathSet = false;
	brect = null;
	int splineNo = -1;
	ArrayList<Boolean> splineStat = new ArrayList<>();
	for (int i = 0; i < nu; i++) {
	    if (frozenV) continue;
	    if (frozenUEnds1 && i == 0) continue;
	    if (frozenUEnds2 && i == nu-1) continue;
	    /*
	    if (frozenUEnds) {
		if (i == 0 || i == nu-1) continue;
	    }
	    */
	    int offset = 0;
	    Point3D[] tmp = new Point3D[2*nv + 1];
	    for (int k = 0; k < nv; k++) {
		tmp[k] = array[i][k].p;
	    }
	    if (vclosed) {
		System.arraycopy(tmp, 0, tmp, nv, nv);
	    }
	    while (offset < nv) {
		while (offset < nv && tmp[offset] == null) offset++;
		if (offset == nv) break;
		int n = 0;
		if (vclosed && offset != 0) {
		    while (tmp[offset+n] != null
			   && array[i][offset%nv].region ==
			   array[i][(offset+n)%nv].region) {
			n++;
		    }
		} else {
		    while (offset+n < nv && tmp[offset+n] != null
			   && array[i][offset%nv].region ==
			   array[i][(offset+n)%nv].region) {
			n++;
		    }
		}
		Path3D spline = null;
		if (n == nv) {
		    spline = new SplinePath3D(tmp, offset, n, vclosed);
		    splineNo++;
		    splineStat.add(vclosed);
		} else if (n == 1) {
		    if (tmp[offset+1] != null) {
			if (array[i][offset%nv].region !=
			    array[i][(offset+1)%nv].region) {
			    spline = new Path3D.Double();
			    splineNo++;
			    splineStat.add(false);
			    Point3D p1 = tmp[offset];
			    Point3D p2 = tmp[offset+1];
			    spline.moveTo(p1.getX(), p1.getY(), p1.getZ());
			    spline.lineTo(p2.getX(), p2.getY(), p2.getZ());
			}
		    } else {
			offset++;
			// System.out.format("setting offset to %d\n", offset);
			continue;
		    }
		} else if (n == 2) {
		    spline = new Path3D.Double();
		    splineNo++;
		    splineStat.add(false);
		    Point3D p1 = tmp[offset];
		    Point3D p2 = tmp[offset+1];
		    spline.moveTo(p1.getX(), p1.getY(), p1.getZ());
		    spline.lineTo(p2.getX(), p2.getY(), p2.getZ());
		} else if (n > 2) {
		    spline = new SplinePath3D(tmp, offset, n, false);
		    splineNo++;
		    splineStat.add(false);

		}
		/*
		if (spline == null) {
		    System.out.println("no spline: n = " + n + ", nv = " + nv
				       + ", offset = " + offset);
		}
		*/
		// Path3DInfo.printSegments(spline);
		int index = offset;
		PathIterator3D pit = spline.getPathIterator(null);
		double[] coords = new double[9];
		double x = Double.NaN, y = Double.NaN, z = Double.NaN;
		double[] vc;
		Point3D pc;
		while (!pit.isDone()) {
		    /*
		    if (!array[i][index%nv].vcSet) {
		    */
			switch(pit.currentSegment(coords)) {
			case PathIterator3D.SEG_CLOSE:
			    break;
			case PathIterator3D.SEG_MOVETO:
			    x = coords[0];
			    y = coords[1];
			    z = coords[2];
			    if (splout != null) {
				sploutAppend("starting V spline");
			    }
			    break;
			case PathIterator3D.SEG_LINETO:
			    vc = new double[9];
			    if (splout != null) {
				sploutAppend(String.format
					      ("adding V spline at (%d, %d)",
					       i, index%nv));
			    }
			    array[i][(index)%nv].vsn = splineNo;
			    array[i][(index++)%nv].vc = vc;
			    System.arraycopy(Path3D.setupCubic
					     (array[i][(index-1)%nv].p,
					      array[i][index%nv].p),
					     3, vc, 0, 9);
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			    break;
			case PathIterator3D.SEG_CUBICTO:
			    vc = new double[9];
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding V spline at (%d, %d)",
					      i, index%nv));
			    }
			    array[i][(index)%nv].vsn = splineNo;
			    array[i][(index++)%nv].vc = vc;
			    System.arraycopy(coords, 0, vc, 0, 9);
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			    x = coords[6];
			    y = coords[7];
			    z = coords[8];
			    break;
			}
			/*
		    } else {
			index++;
		    }
			*/
		    pit.next();
		}
		/*
		System.out.println("prev offset = " + offset
				   + " and prev n = " + n
				   + " at i = " + i);
		*/
		if (n > 1 && tmp[offset+n] != null) {
		    // we decrease by one because the point terminating
		    // one spline starts the next. when the termination is
		    // due to a region change.
		    n--;
		}
		offset += n;
		// System.out.println("new offset = " + offset);
	    }
	}
	for (int j = 0; j < nv; j++) {
	    if (frozenU) continue;
	    if (frozenVEnds1 && j == 0) continue;
	    if (frozenVEnds2 && j == nv-1) continue;
	    int offset = 0;
	    Point3D[] tmp = new Point3D[2*nu + 1];
	    for (int k = 0; k < nu; k++) {
		tmp[k] = array[k][j].p;
	    }
	    if (uclosed) {
		System.arraycopy(tmp, 0, tmp, nu, nu);
	    }
	    while (offset < nu) {
		while (offset < nu && tmp[offset] ==  null) offset++;
		if (offset == nu) break;
		int n = 0;
		if (uclosed && offset != 0) {
		    while (tmp[offset+n] != null
			   && array[offset%nu][j].region ==
			   array[(offset+n)%nu][j].region) {
			n++;
		    }
		} else {
		    while (offset+n < nu && tmp[offset+n] != null
			   && array[offset%nu][j].region ==
			   array[(offset+n)%nu][j].region) {
			n++;
		    }
		}
		Path3D spline = null;
		if (n == nu) {
		    spline = new SplinePath3D(tmp, offset, n, uclosed);
		    splineNo++;
		    splineStat.add(uclosed);
		} else if (n == 1) {
		    if (tmp[offset+1] != null) {
			if (array[offset%nu][j].region !=
			    array[(offset+1)%nu][j].region) {
			    spline = new Path3D.Double();
			    splineNo++;
			    splineStat.add(false);
			    Point3D p1 = tmp[offset];
			    Point3D p2 = tmp[offset+1];
			    spline.moveTo(p1.getX(), p1.getY(), p1.getZ());
			    spline.lineTo(p2.getX(), p2.getY(), p2.getZ());
			}
		    } else {
			offset++;
			continue;
		    }
		} else if (n == 2) {
		    spline = new Path3D.Double();
		    splineNo++;
		    splineStat.add(false);
		    Point3D p1 = tmp[offset];
		    Point3D p2 = tmp[offset+1];
		    spline.moveTo(p1.getX(), p1.getY(), p1.getZ());
		    spline.lineTo(p2.getX(), p2.getY(), p2.getZ());
		} else if (n > 2) {
		    spline = new SplinePath3D(tmp, offset, n, false);
		    splineNo++;
		    splineStat.add(false);
		}
		// Path3DInfo.printSegments(spline);
		int index = offset;
		PathIterator3D pit = spline.getPathIterator(null);
		double[] coords = new double[9];
		double x = Double.NaN, y = Double.NaN, z = Double.NaN;
		Point3D pc;
		double[] uc;
		int iii = -1;
		while (!pit.isDone()) {
		    iii++;
		    /*
		    if (!array[index%nu][j].ucSet) {
		    */
			switch(pit.currentSegment(coords)) {
			case PathIterator3D.SEG_CLOSE:
			    break;
			case PathIterator3D.SEG_MOVETO:
			    x = coords[0];
			    y = coords[1];
			    z = coords[2];
			    if (splout != null) {
				sploutAppend("Starting U spline");
			    }
			    break;
			case PathIterator3D.SEG_LINETO:
			    uc = new double[9];
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)",
					      index%nu, j));
			    }
			    array[(index)%nu][j].usn = splineNo;
			    array[(index++)%nu][j].uc = uc;
			    System.arraycopy(Path3D.setupCubic
					     (array[(index-1)%nu][j].p,
					      array[index%nu][j].p),
					     3, uc, 0, 9);
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			    break;
			case PathIterator3D.SEG_CUBICTO:
			    uc = new double[9];
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)",
					      index%nu, j));
			    }
			    array[(index)%nu][j].usn = splineNo;
			    array[(index++)%nu][j].uc = uc;
			    System.arraycopy(coords, 0, uc, 0, 9);
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			    x = coords[6];
			    y = coords[7];
			    z = coords[8];
			    break;
			}
			/*
		    } else {
			index++;
		    }
			*/
		    pit.next();
		}
		if (n > 1 && tmp[offset+n] != null) {
		    // we decrease by one because the point terminating
		    // one spline starts the next. when the termination is
		    // due to a region change.
		    n--;
		}
		offset += n;
	    }
	}
	int entry = 0;
	for (SplineDescriptor sd: splineDescriptors) {
	    splineNo++;
	    sd.splineID = splineNo;
	    splineStat.add(sd.cyclic);
	    Point3D[] points = new Point3D[sd.points.length];
	    int decr = sd.cyclic? 1: 0;
	    for (int k = 0; k < sd.points.length; k++) {
		points[k] = array[sd.points[k].i][sd.points[k].j].p;
	    }
	    Path3D path = null;
	    if (points.length < 2) {
		continue;
	    } else if (points.length == 2) {
		path = new SplinePath3D();
		path.moveTo(points[0].getX(),
				 points[0].getY(),
				 points[0].getZ());
		path.lineTo(points[1].getX(),
				 points[1].getY(),
				 points[1].getZ());
	    } else if (points.length > 2) {
		path = new SplinePath3D(points,
					points.length-decr,
					sd.cyclic);
	    }
	    int index = 0;
	    double[] coords = new double[9];
	    PathIterator3D pit = path.getPathIterator(null);
	    int ci = sd.points[index].i;
	    int cj = sd.points[index].j;
	    if (pit.currentSegment(coords) != PathIterator3D.SEG_MOVETO) {
		throw new IllegalStateException(errorMsg("expectingMoveTo"));
	    }
	    pit.next();
	    int ni = -1, nj = -1;
	    boolean udir = false;
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		index++;
		ni = (type == PathIterator3D.SEG_CLOSE)? oi:
		    sd.points[index].i;
		nj = (type == PathIterator3D.SEG_CLOSE)? oj:
		    sd.points[index].j;
		udir = (cj == nj);
		switch(type) {
		case PathIterator3D.SEG_CLOSE:
		    break;
		case PathIterator3D.SEG_MOVETO:
		    oi = ni;
		    oj = nj;
		    if (splout != null) {
			sploutAppend("starting custom spline");
		    }
		    break;
		case PathIterator3D.SEG_LINETO:
		    if (coords[0] != array[ni][nj].p.getX()) {
			String msg = errorMsg("wrongSpline", ni, nj);
			throw new RuntimeException(msg);
		    }
		    if (coords[1] != array[ni][nj].p.getY()) {
			String msg = errorMsg("wrongSpline", ni, nj);
			throw new RuntimeException(msg);
		    }
		    if (coords[2] != array[ni][nj].p.getZ()) {
			String msg = errorMsg("wrongSpline", ni, nj);
			throw new RuntimeException(msg);
		    }
		    if (udir) {
			if (ni > ci) {
			    if (array[ci][cj].uc == null) {
				array[ci][cj].uc = new double[9];
			    }
			    double[] lcoords =
				Path3D.setupCubic(array[ci][cj].p,
						  array[ni][nj].p);
			    System.arraycopy(lcoords, 3,
					     array[ci][cj].uc, 0, 9);
			    array[ci][cj].usn = splineNo;
			    double[] uc = array[ci][cj].uc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)\n",
					      ci, cj));
			    }
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			} else {
			    if (array[ni][nj].uc == null) {
				array[ni][nj].uc = new double[9];
			    }
			    double[] lcoords =
				Path3D.setupCubic(array[ni][nj].p,
						  array[ci][cj].p);
			    System.arraycopy(lcoords, 3,
					     array[ni][nj].uc, 0, 9);
			    array[ni][nj].usn = splineNo;
			    double[] uc = array[ni][nj].uc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)",
					      ni, nj));
			    }
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			}
		    } else {
			if (nj > cj) {
			    if (array[ci][cj].vc == null) {
				array[ci][cj].vc = new double[9];
			    }
			    double[] lcoords =
				Path3D.setupCubic(array[ci][cj].p,
						  array[ni][nj].p);
			    System.arraycopy(lcoords, 3,
					     array[ci][cj].vc, 0, 9);
			    array[ci][cj].vsn = splineNo;
			    double[] vc = array[ci][cj].vc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding V spline at (%d, %d)",
					      ci, cj));
			    }
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			} else {
			    if (array[ni][nj].vc == null) {
				array[ni][nj].vc = new double[9];
			    }
			    double[] lcoords =
				Path3D.setupCubic(array[ni][nj].p,
						  array[ci][cj].p);
			    System.arraycopy(lcoords, 3,
					     array[ni][nj].vc, 0, 9);
			    array[ni][nj].vsn = splineNo;
			    double[] vc = array[ni][nj].vc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding V spline at (%d, %d)",
					      ni, nj));
			    }
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			}
		    }
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    if (udir) {
			if (ni > ci) {
			    if (array[ci][cj].uc == null) {
				array[ci][cj].uc = new double[9];
			    }
			    System.arraycopy(coords, 0, array[ci][cj].uc, 0, 9);
			    array[ci][cj].usn = splineNo;
			    double[] uc = array[ci][cj].uc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)",
					      ci, cj));
			    }
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			} else {
			    if (array[ni][nj].uc == null) {
				array[ni][nj].uc = new double[9];
			    }
			    array[ni][nj].uc[6] = array[ci][cj].p.getX();
			    array[ni][nj].uc[7] = array[ci][cj].p.getY();
			    array[ni][nj].uc[8] = array[ci][cj].p.getZ();
			    for (int k = 0; k < 3; k++) {
				array[ni][nj].uc[k] = coords[3+k];
				array[ni][nj].uc[3+k] = coords[k];
			    }
			    array[ni][nj].usn = splineNo;
			    double[] uc = array[ni][nj].uc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding U spline at (%d, %d)",
					      ni, nj));
			    }
			    for (int k = 0; k < 9; k++) {
				uc[k] = (double)(float)uc[k];
			    }
			}
		    } else {
			if (nj > cj) {
			    if (array[ci][cj].vc == null) {
				array[ci][cj].vc = new double[9];
			    }
			    System.arraycopy(coords, 0, array[ci][cj].vc, 0, 9);
			    array[ci][cj].vsn = splineNo;
			    double[] vc = array[ci][cj].vc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding V spline at (%d, %d)",
					      ci, cj));
			    }
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			} else {
			    if (array[ni][nj].vc == null) {
				array[ni][nj].vc = new double[9];
			    }
			    array[ni][nj].vc[6] = array[ci][cj].p.getX();
			    array[ni][nj].vc[7] = array[ci][cj].p.getY();
			    array[ni][nj].vc[8] = array[ci][cj].p.getZ();
			    for (int k = 0; k < 3; k++) {
				array[ni][nj].vc[k] = coords[3+k];
				array[ni][nj].vc[3+k] = coords[k];
			    }
			    array[ni][nj].vsn = splineNo;
			    double[] vc = array[ni][nj].vc;
			    if (splout != null) {
				sploutAppend(String.format
					     ("adding V spline at (%d, %d)",
					      ni, nj));
			    }
			    for (int k = 0; k < 9; k++) {
				vc[k] = (double)(float)vc[k];
			    }
			}
		    }
		    break;
		}
		pit.next();
		ci = ni;
		cj = nj;
	    }
	}
	splineStatus = new boolean[splineStat.size()];
	int p = 0;
	for (Boolean status: splineStat) {
	    splineStatus[p++] = status;
	}
	splinesCreated = true;
    }

    /**
     * Set the orientation of the patches.
     * This method affects the orientation of patches provided by iterators.
     * It does not change the values returned by calling methods
     * such as {@link #getFullSplineU(int,int,double[])},
     * {@link #getFullSplineV(int,int,double[])},
     * {@link #getSplineU(int,int,double[])}, or
     * {@link #getSplineV(int,int,double[])}.
     * @param reverse true if the orientation is the reverse of
     *        the one that was initially defined; false if the orientation is
     *        the same as the one that was initially defined.
     * @return this grid
     */
    public BezierGrid reverseOrientation(boolean reverse) {
	flipped = rflip? !reverse: reverse;
	return this;
    }

    /**
     * Reverse the orientation from the current orientation.
     * This method affects the orientation of patches provided by iterators.
     * It does not change the values returned by calling methods
     * such as {@link #getFullSplineU(int,int,double[])},
     * {@link #getFullSplineV(int,int,double[])},
     * {@link #getSplineU(int,int,double[])}, or
     * {@link #getSplineV(int,int,double[])}.
     * <P>
     * When using the method {#transpose()}, the new grid's initial
     * orientation is opposite to the current grid's initial orientation,
     * but if the current grid is reversed, the new grid will be reversed
     * as well.  To force the new grid to have the same orientation as
     * the current grid, {@link #flip()} can be used.
     * @return this grid
     */
    public BezierGrid flip() {
	flipped = !flipped;
	return this;
    }


    /**
     * Determine if the orientation for this grid is reversed.
     * @return true if the orientation is reversed; false if not
     */
    public boolean isReversed() {return rflip? !flipped: flipped;}

    /**
     * Set a region ID.
     * Regions represent collections of points whose boundaries
     * terminate the implicitly generated splines so that the
     * curves at the border of a region may not have continuous derivatives.
     * A region is represented by an integer ID.  Implicitly generated
     * splines have constant indices in either the U or V directions,
     * but not both.  As the varying index increases, a spline terminates
     * either when the maximum index is reached or at the first index for
     * which the region ID changes. That index is also the one for the
     * start of the next spline.
     * All grid points are initialized to be in Region 0. This method
     * must be used to change a point's region ID from this default.
     * @param i the index for the U direction
     * @param j the index for the V direction
     * @param id the region id
     * @exception IllegalArgumentException the arguments are out of range
     */
    public void setRegion(int i, int j, int id) {
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	array[i][j].region = id;
	bpathSet = false;
    }

    /**
     * Set a region ID for a rectangle of grid points.
     * Regions represent collections of points whose boundaries
     * terminate the implicitly generated splines so that the
     * curves at these locations may not have continuous derivatives.
     * A region is represented by an integer ID.
     * A region is represented by an integer ID.  Implicitly generated
     * splines have constant indices in either the U or V directions,
     * but not both.  As the varying index increases, a spline terminates
     * either when the maximum index is reached or at the first index for
     * which the region ID changes. That index is also the one for the
     * start of the next spline.
     * All grid points are initialized to be in Region 0. This method
     * must be used to change a point's region ID from this default.
     * @param i the index for the start of the rectangle in the U
     *         direction
     * @param j the index  for the start of the rectangle in the V direction
     * @param width the number of indices in the U direction
     * @param height the number of indices in the V diction
     * @param id the region id
     * @exception IllegalArgumentException if an argument was out of range
     * @exception IllegalStateException if called on a linear BezierGrid
     */
    public void setRegion(int i, int j, int width, int height, int id) {
	if (linear) {
	    throw new IllegalStateException(errorMsg("linearSetRegion"));
	}
	if (i < 0 || j < 0 || i >= nu || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (width < 0 || height < 0 || i + width > nu || j+height > nv) {
	    String msg = errorMsg("widthHeight", width, height);
	    throw new IllegalArgumentException(msg);
	}
	int lasti = i + width;
	int lastj = j + height;
	bpathSet = false;
	while (i < lasti) {
	    int jj = j;
	    while (jj < lastj) {
		array[i][jj].region = id;
		jj++;
	    }
	    i++;
	}
    }


    /**
     * Remove a patch.
     * The indices (i, j) specify the point at u=0, v=0 for the
     * corresponding patch.
     * When a patch is removed, the patch is not used as part of
     * a 3D shape, but its point (u=0, v=0) is  used in creating
     * splines.
     * @param i the index for the U direction
     * @param j the index for the V direction
     */
    public void remove(int i, int j) {
	array[i][j].filled = false;
	bpathSet = false;
    }

    /**
     * Remove a rectangle of patches
     * The patches removed will have indices varying from
     * i to i+width exclusive an j to j+height exclusive.
     * both the width and the height must be non-negative.
     * When a patch is removed, the patch is not used as part of
     * a 3D shape, but its point (u=0, v=0) is  used in creating
     * splines.
     * @param i the index for the start of the rectangle in the U direction
     * @param j the index for the start of the rectangle in the V direction
     * @param width the number of indices in the U direction
     * @param height the number of indices in the V diction
     */
    public void remove(int i, int j, int width, int height) {
	int ulimit = i + width;
	int vlimit = j + height;
	bpathSet = false;
	while (i < ulimit) {
	    int k = j;
	    while (k < vlimit) {
		array[i][k++].filled = false;
	    }
	    i++;
	}
    }


    /**
     * Restore a patch.
     * The indices (i, j) specify the point at u=0, v=0 for the
     * corresponding patch. This reverses the effect of calling
     * {@link #remove(int,int)}.
     * @param i the index in the U direction
     * @param j the index in the V direction
     */
    public void restore(int i, int j) {
	array[i][j].filled = true;
	bpathSet = false;
    }

    /**
     * Restore a rectangle of patches
     * The patches restored will have indices varying from i to
     * i+width exclusive an j to j+height exclusive.  both the width
     * and the height must be non-negative. This reverses the effect
     * of calling {@link #remove(int,int,int,int)}.
     * @param i the index for the start of the rectangle in the U direction
     * @param j the  index for the start of the rectangle in the V direction
     * @param width the number of indices in the U direction
     * @param height the number of indices in the V diction
     */
    public void restore(int i, int j, int width, int height) {
	int ulimit = i + width;
	int vlimit = j + height;
	bpathSet = false;
	while (i < ulimit) {
	    int k = j;
	    while (k < vlimit) {
		array[i][k++].filled = true;
	    }
	    i++;
	}
    }

    Object tag = null;
    boolean oriented = true;


    private class Iterator1 implements SurfaceIterator {
	int index = -1;
	int limit = nu*nv;
	int limitm1 = limit -1;
	int num1 = nu-1;
	int nvm1 = nv-1;
	boolean flip = flipped;


	Iterator1() {
	    next();
	}

	@Override
	public boolean isDone() {
	    return index >= limit;
	}

	int triangleMode = 0;
	double[] tcoords = linear? new double[9]: null;

	@Override
	public int currentSegment(double[] coords) {
	    if (triangleMode == 2) {
		System.arraycopy(tcoords, 0, coords, 0, 9);
		return SurfaceIterator.PLANAR_TRIANGLE;
	    }
	    if (index >= limit) {
		Arrays.fill(coords, 0, 48, Double.NaN);
		return -1;
	    } else {
		int i = index % nu;
		int j = index / nu;
		Vertex v1 = array[i][j];
		double x1 = v1.p.getX();
		double y1 = v1.p.getY();
		double z1 = v1.p.getZ();
		Vertex v2 = (i < num1)? array[i+1][j]: array[0][j];
		double x2 = v2.p.getX();
		double y2 = v2.p.getY();
		double z2 = v2.p.getZ();
		Vertex v3 = (j < nvm1)? array[i][j+1]: array[i][0];
		double x3 = v3.p.getX();
		double y3 = v3.p.getY();
		double z3 = v3.p.getZ();
		if (linear) {
		    Vertex v4;
		    if (i < num1) {
			if (j < nvm1) {
			    // normal case
			    v4 = array[i+1][j+1];
			} else {
			    v4 = array[i+1][0];
			}
		    } else if (j < nvm1) {
			v4 = array[0][j+1];
		    } else {
			v4 = array[0][0];
		    }
		    double x4 = v4.p.getX();
		    double y4 = v4.p.getY();
		    double z4 = v4.p.getZ();
		    tcoords[0] = x2-x1;
		    tcoords[1] = y2-y1;
		    tcoords[2] = z2-z1;
		    tcoords[3] = x3-x1;
		    tcoords[4] = y3-y1;
		    tcoords[5] = z3-z1;
		    tcoords[6] = x4-x1;
		    tcoords[7] = y4-y1;
		    tcoords[8] = z4-z1;
		    for (int k = 0; k < 9; k += 3) {
			try {
			    VectorOps.normalize(tcoords, k, 3);
			} catch (IllegalArgumentException iae) {
			    // a vector has a length of 0, which is OK here.
			}
		    }
		    double dotcp = VectorOps.dotCrossProduct(tcoords, 0,
							     tcoords, 3,
							     tcoords, 6);
		    if (Math.abs(dotcp) < PLANAR_LIMIT) {
			// The corners lie in a plane, so we can generate
			// planar triangles.
			coords[0] = x1;
			coords[1] = y1;
			coords[2] = z1;
			coords[3] = x4;
			coords[4] = y4;
			coords[5] = z4;
			coords[6] = x2;
			coords[7] = y2;
			coords[8] = z2;
			tcoords[0] = x1;
			tcoords[1] = y1;
			tcoords[2] = z1;
			tcoords[3] = x3;
			tcoords[4] = y3;
			tcoords[5] = z3;
			tcoords[6] = x4;
			tcoords[7] = y4;
			tcoords[8] = z4;
			if (flip) {
			    Surface3D.reverseOrientation(SurfaceIterator
							 .PLANAR_TRIANGLE,
							 coords);
			    Surface3D.reverseOrientation(SurfaceIterator
							 .PLANAR_TRIANGLE,
							 tcoords);
			}
			triangleMode = 1;
			return SurfaceIterator.PLANAR_TRIANGLE;
		    }
		}
		Surface3D.setupU0ForPatch(x1, y1, z1, v1.vc, coords, false);
		Surface3D.setupV0ForPatch(x1, y1, z1, v1.uc, coords, false);
		Surface3D.setupU1ForPatch(x2, y2, z2, v2.vc, coords, false);
		Surface3D.setupV1ForPatch(x3, y3, z3, v3.uc, coords, false);

		if (v1.rest == null) {
		    Surface3D.setupRestForPatch(coords);
		} else {
		    System.arraycopy(v1.rest, 0, coords, 5*3, 6);
		    System.arraycopy(v1.rest, 6, coords, 9*3, 6);
		    for (int k = 0; k < v1.rest.length; k++) {
			v1.rest[k] = (double)(float) v1.rest[k];
		    }
		}
		if (flip) {
		    Surface3D.reverseOrientation(SurfaceIterator.CUBIC_PATCH,
						 coords);
		}
		return SurfaceIterator.CUBIC_PATCH;
	    }
	}
	
	double[] dcoords = null;

	@Override
	public synchronized int currentSegment(float[] coords) {
	    if (index >= limit) {
		Arrays.fill(coords, 0, 48, Float.NaN);
		return -1;
	    } else {
		if (dcoords == null) dcoords = new double[48];
		int result = currentSegment(dcoords);
		int len = (result == SurfaceIterator.PLANAR_TRIANGLE)? 9: 48;
		for (int i = 0; i < len; i++) {
		    coords[i] = (float) dcoords[i];
		}
		return result;
	    }
	}

	@Override
	public Color currentColor() {
		int i = index % nu;
		int j = index / nu;
		return array[i][j].color;
	}

	@Override
	public  Object currentTag() {return tag;}

	@Override
	public boolean isOriented() {return oriented;}

	@Override
	public void next() {
	    switch(triangleMode) {
	    case 0:
		break;
	    case 1:
		triangleMode = 2;
		return;
	    case 2:
		triangleMode = 0;
		break;
	    }
	    while (index < limit) {
		index++;
		if (index == limit) {
		    return;
		}
		int i = index % nu;
		int j = index / nu;

		Vertex vertex = array[i][j];
		/*
		if (vertex.p == null)
		    System.out.println("... vertex.p == null");
		if (vertex.uc == null)
		    System.out.println("... vertex.uc == null");
		if (vertex.vc == null)
		    System.out.println("... vertex.vc == null");
		*/
		if (vertex.p!=null && vertex.uc!=null && vertex.vc!=null) {
		    // System.out.println("vertex filled: " + vertex.filled);
		    if (vertex.filled) {
			if (i < num1 && j < nvm1) {
			    if (array[i+1][j].p == null
				|| array[i+1][j].vc == null) continue;
			    if (array[i][j+1].p == null
				|| array[i][j+1].uc == null) continue;
			    break;
			} else if (i == num1 && j < nvm1) {
			    if (uclosed && array[0][j].p != null
				&& array[0][j].vc != null) break;
			} else if (j == nvm1 && i < num1) {
			    if (vclosed && array[i][0].p != null
				&& array[i][0].uc != null) break;
			} else if (i == num1 && j == nvm1) {
			    if (uclosed && array[0][j].p != null
				&& array[0][j].vc != null
				&& vclosed && array[i][0].p != null
				&& array[i][0].uc != null) break;
			}
		    }
		}
	    }
	}
    }

    private class Iterator2 implements SurfaceIterator {
	int index = -1;
	int limit = nu*nv;
	int num1 = nu-1;
	int nvm1 = nv-1;
	boolean flip = flipped;

	Transform3D transform;


	Iterator2(Transform3D tform) {
	    this.transform = tform;
	    next();
	}

	@Override
	public boolean isDone() {
	    return index >= limit;
	}

	double[] dcoords2 = new double[48];

	int triangleMode = 0;
	double[] tcoords = linear? new double[9]: null;

	private int setupCoords(double[] result) {
	    if (triangleMode == 2) {
		System.arraycopy(tcoords, 0, result, 0, 9);
		return SurfaceIterator.PLANAR_TRIANGLE;
	    }
	    int i = index % nu;
	    int j = index / nu;
	    Vertex v1 = array[i][j];
	    double x1 = v1.p.getX();
	    double y1 = v1.p.getY();
	    double z1 = v1.p.getZ();
	    Vertex v2 = (i < num1)? array[i+1][j]: array[0][j];
	    double x2 = v2.p.getX();
	    double y2 = v2.p.getY();
	    double z2 = v2.p.getZ();
	    Vertex v3 = (j < nvm1)? array[i][j+1]: array[i][0];
	    double x3 = v3.p.getX();
	    double y3 = v3.p.getY();
	    double z3 = v3.p.getZ();
	    if (linear) {
		Vertex v4;
		if (i < num1) {
		    if (j < nvm1) {
			// normal case
			v4 = array[i+1][j+1];
		    } else {
			v4 = array[i+1][0];
		    }
		} else if (j < nvm1) {
		    v4 = array[0][j+1];
		} else {
		    v4 = array[0][0];
		}
		double x4 = v4.p.getX();
		double y4 = v4.p.getY();
		double z4 = v4.p.getZ();
		tcoords[0] = x2-x1;
		tcoords[1] = y2-y1;
		tcoords[2] = z2-z1;
		tcoords[3] = x3-x1;
		tcoords[4] = y3-y1;
		tcoords[5] = z3-z1;
		tcoords[6] = x4-x1;
		tcoords[7] = y4-y1;
		tcoords[8] = z4-z1;
		for (int k = 0; k < 9; k += 3) {
		    try {
			VectorOps.normalize(tcoords, k, 3);
		    } catch (IllegalArgumentException iae) {
			// a vector has a length of 0, which is OK here.
		    }
		}
		double dotcp = VectorOps.dotCrossProduct(tcoords, 0,
							 tcoords, 3,
							 tcoords, 6);
		if (Math.abs(dotcp) < PLANAR_LIMIT) {
		    // The corners lie in a plane, so we can generate
		    // planar triangles.
		    result[0] = x1;
		    result[1] = y1;
		    result[2] = z1;
		    result[3] = x4;
		    result[4] = y4;
		    result[5] = z4;
		    result[6] = x2;
		    result[7] = y2;
		    result[8] = z2;
		    tcoords[0] = x1;
		    tcoords[1] = y1;
		    tcoords[2] = z1;
		    tcoords[3] = x3;
		    tcoords[4] = y3;
		    tcoords[5] = z3;
		    tcoords[6] = x4;
		    tcoords[7] = y4;
		    tcoords[8] = z4;
		    if (flip) {
			Surface3D.reverseOrientation(SurfaceIterator
						     .PLANAR_TRIANGLE,
						     result);
			Surface3D.reverseOrientation(SurfaceIterator
						     .PLANAR_TRIANGLE,
						     tcoords);
		    }
		    triangleMode = 1;
		    return SurfaceIterator.PLANAR_TRIANGLE;
		}
	    }
	    Surface3D.setupU0ForPatch(x1, y1, z1, v1.vc, result, false);
	    Surface3D.setupV0ForPatch(x1, y1, z1, v1.uc, result, false);
	    Surface3D.setupU1ForPatch(x2, y2, z2, v2.vc, result, false);
	    Surface3D.setupV1ForPatch(x3, y3, z3, v3.uc, result, false);
	    if (v1.rest == null) {
		Surface3D.setupRestForPatch(result);
	    } else {
		System.arraycopy(v1.rest, 0, result, 5*3, 6);
		System.arraycopy(v1.rest, 6, result, 9*3, 6);
		for (int k = 0; k < v1.rest.length; k++) {
		    v1.rest[k] = (double)(float) v1.rest[k];
		}
	    }
	    if (flip) {
		Surface3D.reverseOrientation(SurfaceIterator.CUBIC_PATCH,
					     result);
	    }
	    return SurfaceIterator.CUBIC_PATCH;
	}

	@Override
	public synchronized int currentSegment(double[] coords) {
	    if (index >= limit) {
		Arrays.fill(coords, 0, 48, Double.NaN);
		return -1;
	    } else {
		int type = setupCoords(dcoords2);
		int npts = (type == SurfaceIterator.PLANAR_TRIANGLE)? 3: 16;
		transform.transform(dcoords2, 0, coords, 0, npts);
		return type;
	    }
	}
	
	double[] dcoords = null;
	@Override
	public synchronized int currentSegment(float[] coords) {
	    if (index >= limit) {
		Arrays.fill(coords, 0, 48, Float.NaN);
		return -1;
	    } else {
		int type = setupCoords(dcoords2);
		int npts = (type == SurfaceIterator.PLANAR_TRIANGLE)? 3: 16;
		transform.transform(dcoords2, 0, coords, 0, npts);
		return type;
	    }
	}

	@Override
	public Color currentColor() {
	    int i = index % nu;
	    int j = index / nu;
	    return array[i][j].color;
	}

	@Override
	public  Object currentTag() {return tag;}

	@Override
	public boolean isOriented() {return oriented;}

	@Override
	public void next() {
	    switch(triangleMode) {
	    case 0:
		break;
	    case 1:
		triangleMode = 2;
		return;
	    case 2:
		triangleMode = 0;
		break;
	    }
	    while (index < limit) {
		index++;
		if (index == limit) return;
		int i = index % nu;
		int j = index / nu;
		Vertex vertex = array[i][j];
		if (vertex.p!=null && vertex.uc!=null && vertex.vc!=null) {
		    if (vertex.filled) {
			if (i < num1 && j < nvm1) {
			    if (array[i+1][j].p == null
				|| array[i+1][j].vc == null) continue;
			    if (array[i][j+1].p == null
				|| array[i][j+1].uc == null) continue;
			    break;
			} else if (i == num1 && j < nvm1) {
			    if (uclosed && array[0][j].p != null
				&& array[0][j].vc != null) break;
			} else if (j == nvm1 && i < num1) {
			    if (vclosed && array[i][0].p != null
				&& array[i][0].uc != null) break;
			} else if (i == num1 && j == nvm1) {
			    if (uclosed && array[0][j].p != null
				&& array[0][j].vc != null
				&& vclosed && array[i][0].p != null
				&& array[i][0].uc != null) break;
			}
		    }
		}
	    }
	}
    }


    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	createSplines();
	if (tform == null) {
	    return new Iterator1();
	} else {
	    return new Iterator2(tform);
	}
    }

    @Override
    public final synchronized
    SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	createSplines();
	if (tform == null) {
	    if (level == 0) {
		return new Iterator1();
	    } else {
		return new SubdivisionIterator(new Iterator1(), level);
	    }
	} else if (tform instanceof AffineTransform3D) {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator2(tform),
					       level);
	    }
	} else {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator1(),
					       tform, level);
	    }
	}
    }

    Shape3D[] components = null;

    @Override
    public int numberOfComponents() {
	if (components == null) {
	    components =
		Surface3D.createComponents(getSurfaceIterator(null), null);
	}
	return components.length;
    }

    @Override
    public Shape3D getComponent(int i) {
	if (components == null) {
	    components =
		Surface3D.createComponents(getSurfaceIterator(null), null);
	}
	return components[i];
    }

    boolean bpathSet = false;
    Path3D bpath = null;

    /**
     * Determine if the grid is well formed
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null);
    }

    /**
     * Determine if the grid is well formed, logging error messages to
     * an Appendable
     * @param out an Appendable for logging error messages
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed(Appendable out) {
	Surface3D.Boundary boundary
	    = new Surface3D.Boundary(getSurfaceIterator(null), out);
	if (!bpathSet) {
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (boundary.getPath() != null);
    }

    private Vertex findVertex(double lastx, double lasty, double lastz) {
	Vertex v;
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		v = array[i][j];
		if (v == null || v.p == null) {
		    continue;
		}
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	return null;
    }

    private Vertex findVertex(Vertex other) {
	Vertex v;
	if (other.p == null) return null;
	double lastx = other.p.getX();
	double lasty = other.p.getY();
	double lastz = other.p.getZ();
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		v = array[i][j];
		if (v == null || v.p == null || v == other) {
		    continue;
		}
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	return null;
    }


    private Vertex findVertex(Vertex lastv,
			      double lastx, double lasty, double lastz)
    {
	Vertex v;
	for (int i = -1; i < 2; i += 2) {
	    if (lastv.i == 0 && i == -1) continue;
	    if (lastv.i == nu-1 && i == 1) continue;
	    v = array[lastv.i + i][lastv.j];
	    if (v == null) continue;
	    if (v.p == null) continue;
	    if (v.p.getX() == lastx && v.p.getY() == lasty
		&& v.p.getZ() == lastz) {
		return v;
	    }
	}
	for (int j = -1; j < 2; j += 2) {
	    if (lastv.j == 0 && j == -1) continue;
	    if (lastv.j == nv-1 && j == 1) continue;
	    v = array[lastv.i][lastv.j + j];
	    if (v == null) continue;
	    if (v.p == null) continue;
	    if (v.p.getX() == lastx && v.p.getY() == lasty
		&& v.p.getZ() == lastz) {
		return v;
	    }
	}
	if (lastv.i == 0) {
	    v = array[nu-1][lastv.j];
	    if (v != null && v.p != null) {
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	if (lastv.i == nu-1) {
	    v = array[0][lastv.j];
	    if (v != null && v.p != null) {
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	if (lastv.j == 0) {
	    v = array[lastv.i][nv-1];
	    if (v != null && v.p != null) {
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	if (lastv.j == nv-1) {
	    v = array[lastv.i][0];
	    if (v != null && v.p != null) {
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    return v;
		}
	    }
	}
	/*
	v = findVertex(lastx, lasty, lastz);
	if (v == null) {
	    System.out.format("... findVertex(%g, %g, %g) also failed\n",
			       lastx, lasty, lastz);
	} else {
	    System.out.format("lastv (%d, %d), v (%d, %d)\n",
			      lastv.i, lastv.j, v.i, v.j);
	    System.out.format("v.p = (%s, %s, %s)\n", v.p.getX(),
			       v.p.getY(), v.p.getZ());

	}
	*/
	return null;
    }

    private boolean vertexOK(Vertex v, Vertex next) {
	if (next == null) return false;
	if (v.i == next.i) {
	    if (Math.abs(v.i - next.i) == 1) {
		return true;
	    } else {
		return false;
	    }
	} else if (v.j == next.j) {
	    if (Math.abs(v.j - next.j) == 1) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    private Vertex findVertex(Vertex lastv, int type, double[] coords) {
	int len = 0;
	switch(type) {
	case PathIterator3D.SEG_LINETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    len = 6;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    len = 9;
	    break;
	}
	Vertex v;
	for (int i = -1; i < 2; i += 2) {
	    if (!uclosed && lastv.i == 0 && i == -1) continue;
	    if (!uclosed && lastv.i == nu-1 && i == 1) continue;
	    v = array[(nu+lastv.i+i)%nu][lastv.j];
	    if (i < 0) {
		if (matchCoords(coords, v.p, v.uc, len, true)) {
		    return v;
		}
	    } else {
		if (matchCoords(coords, lastv.p, lastv.uc, len, false)) {
		    return v;
		}
	    }
	}
	for (int j = -1; j < 2; j += 2) {
	    if (!vclosed && lastv.j == 0 && j == -1) continue;
	    if (!vclosed && lastv.j == nv-1 && j == 1) continue;
	    v = array[lastv.i][(nu+lastv.j+j)%nu];
	    if (j < 0) {
		if (matchCoords(coords, v.p, v.vc, len, true)) {
		    return v;
		}
	    } else {
		if (matchCoords(coords, lastv.p, lastv.vc, len, false)) {
		    return v;
		}
	    }
	}
	return null;
    }

    private Vertex findVertex (double lastx, double lasty, double lastz,
			      int type, double[] coords)
    {
	int len = 0;
	switch(type) {
	case PathIterator3D.SEG_LINETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    len = 6;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    len = 9;
	    break;
	}
	Vertex v;
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		v = array[i][j];
		if (v == null || v.p == null) {
		    continue;
		}
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    if (uclosed || (v.i+1 < nu)) {
			if (matchCoords(coords, v.p, v.uc, len, false)) {
			    return v;
			}
		    }
		    if (vclosed || (v.j+1 < nv)) {
			if (matchCoords(coords, v.p, v.vc, len, false)) {
			    return v;
			}
		    }
		    if (uclosed || v.i > 0) {
			Vertex vv = array[(nu+i-1)%nu][j];
			if (matchCoords(coords, vv.p, vv.uc, len, true)) {
			    return v;
			}
		    }
		    if (vclosed || v.j > 0) {
			Vertex vv = array[i][(nv+j-1)%nv];
			if (matchCoords(coords, vv.p, vv.vc, len, true)) {
			    return v;
			}
		    }
		}
	    }
	}
	return null;
    }

    private boolean matchCoords(double[] coords1,
				Point3D p,
				double[] coords2, int len,
				boolean reverse) {
	if (coords1 == null || coords2 == null) return false;
	// For a linear segment, vc and uc are of different lengths;
	// we don't have a quad case.
	int offset = (len == 3)? 6: 0;
	if (reverse) {
	    if (p == null) return false;
	    if (coords1[--len] != p.getZ()) return false;
	    if (coords1[--len] != p.getY()) return false;
	    if (coords1[--len] != p.getX()) return false;
	    int k2 = 3;
	    while(len > 0) {
		if (coords1[--len] != coords2[offset + (--k2)]) return false;
		if (k2%3 == 0) k2 += 6;
	    }
	} else {
	    for (int k = 0; k < len; k++) {
		if (coords1[k] != coords2[offset+k]) return false;
	    }
	}
	return true;
    }

    private int findSplineID(Vertex next, Vertex prev) {
	if (next.i == prev.i) {
	    if (next.j > prev.j) {
		return prev.vsn;
	    } else if (next.j < prev.j) {
		return next.vsn;
	    } else {
		String msg = errorMsg("bgSpline", next.i, next.j);
		throw new IllegalStateException(msg);
	    }
	} else if (next.j == prev.j) {
	    if (next.i > prev.i) {
		return prev.usn;
	    } else if (next.i < prev.i) {
		return next.usn;
	    } else {
		String msg = errorMsg("bgSpline", next.i, next.j);
		throw new IllegalStateException(msg);
	    }
	} else {
	    // splines edges follow the borders of grid cells, so
	    // this must be an error.
	    String msg =
		errorMsg("bgSplineDiag", prev.i, prev.j, next.i, next.j);
	    throw new IllegalStateException(msg);
	}
    }

    private boolean gridsSeparated(Vertex[] varray, BezierGrid grid) {
	if (grid == null) return true; // assume separated if not provided
	for (int i = 0; i < varray.length - 1; i++) {
	    Vertex v1 = varray[i];
	    Vertex v2 = varray[i+1];
	    Vertex gv1 = grid.array[v1.i][v1.j];
	    Vertex gv2 = grid.array[v2.i][v2.j];
	    boolean value = false;
	    if (v1.p.getX() != gv1.p.getX()
		|| v1.p.getY() != gv1.p.getY()
		|| v1.p.getZ () != gv1.p.getZ()
		|| v2.p.getX() != gv2.p.getX()
		|| v2.p.getY() != gv2.p.getY()
		|| v2.p.getZ () != gv2.p.getZ()) value = true;
	    if (v1.i == v2.i) {
		boolean forward = (v2.j == v1.j + 1)
		    || (v2.j == 0 && v1.j == nv-1);
		if (forward) {
		    if (v1.vc != null && gv1.vc != null) {
			for (int k = 0; k < 9; k++) {
			    if(v1.vc[k] != gv1.vc[k]) {
				value = true;
				break;
			    }
			}
		    }
		} else {
		    if (v2.vc != null && gv2.vc != null) {
			for (int k = 0; k < 9; k++) {
			    if(v2.vc[k] != gv2.vc[k]) {
				value = true;
				break;
			    }
			}
		    }
		}
	    } else /* if (v1.j == v2.j) */ {
		boolean forward = (v2.i == v1.i + 1)
		    || (v2.i == 0 && v1.i == nu-1);
		if (forward) {
		    if (v1.uc != null && gv1.uc != null) {
			for (int k = 0; k < 9; k++) {
			    if(v1.uc[k] != gv1.uc[k]) {
				value = true;
				break;
			    }
			}
		    }
		} else {
		    if (v2.uc != null && gv2.uc != null) {
			for (int k = 0; k < 9; k++) {
			    if(v2.uc[k] != gv2.uc[k]) {
				value = true;
				break;
			    }
			}
		    }
		}
	    }
	    if (value == false) return false;
	}
	return true;
    }

    private boolean mustKeep(Vertex v, int[] indices) {
	if (v == null) return false;
	for (int k = 0; k < indices.length; k += 2) {
	    if (v.i == indices[0] && v.j == indices[1]) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same. The number of vertices for the
     * connecting grids along their U axes is 2. For their V axes, the
     * number varies, with each grid matching part of a boundary of
     * this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid.
     * @param grid the grid to which this grid should be connected.
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid)
	throws IllegalStateException
    {
	return createConnectionsTo(grid, 2);
    }

    /**
     * Create B&eacute;zier grids, with a specified number of vertices
     * in the 'U' direction, that will connect this grid to a specified
     * rid.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same. The number of vertices for the
     * connecting grids along their U axes is n. For their V axes, the
     * number varies, with each grid matching part of a boundary of
     * this grid.
     * @param grid the grid to which this grid should be connected.
     * @param n the number of vertices for the connecting grids along their
     *        U axes
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, int n)
	throws IllegalStateException
    {
	return createConnectionsTo(grid, n, false);
    }

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid, optionally splitting the returned grids into
     * multiple grids.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same.  The number of vertices for the
     * connecting grids along their U axes is 2. For their V axes, the
     * number varies, with each grid matching part of a boundary of
     * this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid when the argument <CODE>split</CODE> is false. When
     * <CODE>split</CODE> is true, the number of grids is the number
     * of splines that make up the boundary.
     * <P>
     * When the 'split' argument has the value true, and intermediate
     * control points for a grid's edges in the 'U' direction are
     * explicitly modified, one may have to add additional
     * B@eacute;zier patches if the grids are to be connected to each
     * other. The typical case for setting 'split' to true is when
     * adding such patches is desired.
     * @param grid the grid to which this grid should be connected.
     * @param split true if there can be multiple grids per boundary
     *        component; false otherwise
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, boolean split)
	throws IllegalStateException
    {
	return createConnectionsTo(grid, 2, split);
    }

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid, optionally splitting the returned grids into
     * multiple grids, and restricting the returned grids to ones that
     * match a specified boundary.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same. The number of vertices for the
     * connecting grids along their U axes is 2. For their V axes, the
     * number varies, with each grid matching part of a boundary of
     * this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid when the argument <CODE>split</CODE> is false. When
     * <CODE>split</CODE> is true, the number of grids is the number
     * of splines that make up the boundary.
     * <P>
     * When the 'split' argument has the value true, and intermediate
     * control points for a grid's edges in the 'U' direction are
     * explicitly modified, one may have to add additional
     * B&eacute;zier patches if the grids are to be connected to each
     * other. The typical case for setting 'split' to true is when
     * adding such patches is desired.
     * <P>
     * The final arguments are pairs of U-V indices indicating vertices
     * that are part of this grid's boundary.  Those boundary components
     * that match any of these pairs will be connected by the generated
     * grids, whereas other boundary components will not be connected.
     * @param grid the grid to which this grid should be connected.
     * @param split true if there can be multiple grids per boundary
     *        component; false otherwise
     * @param indices a sequence of pairs of U and V indices respectively
     *        for vertices along components of this grid's boundary
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *           its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, boolean split,
					    int... indices)
	throws IllegalStateException
    {
	return createConnectionsTo(grid, 2, split, indices);
    }

    private static final int[] ZERO_LENGTH_ARRAY = new int[0];

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid, specifying the number of vertices along a
     * connecting grid's U axis and optionally splitting the returned
     * grids into multiple grids.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same.  The number of vertices for the
     * connecting grids along their U axes must be at least 2 and is
     * specified by this method's second argument. For their V axes,
     * the number varies, with each grid matching part of a boundary
     * of this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid when the argument <CODE>split</CODE> is false. When
     * <CODE>split</CODE> is true, the number of grids is the number
     * of splines that make up the boundary.
     * <P>
     * When the 'split' argument has the value true, and either the
     * intermediate control points for a grid's edges in the 'U'
     * direction are explicitly modified or the number of vertices in
     * the U direction are larger than two, one may have to add
     * additional B&eacute;zier patches if the grids are to be connected to
     * each other. The typical case for setting 'split' to true is
     * when adding such patches is desired.
     * @param grid the grid to which this grid should be connected.
     * @param n the number of vertices along the U axis for each of the
     *        grids that this method creates
     * @param split true if there can be multiple grids per boundary
     *        component; false otherwise
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, int n,
					boolean split)
    {
	return createConnectionsTo(grid, n, split, ZERO_LENGTH_ARRAY);
    }

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid, specifying the number of vertices along a
     * connecting grid's U axis, optionally splitting the returned
     * grids into multiple grids, and specifying pairs of indices for
     * vertices along allowed boundaries.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same.  The number of vertices for the
     * connecting grids along their U axes must be at least 2 and is
     * specified by this method's second argument. For their V axes,
     * the number varies, with each grid matching part of a boundary
     * of this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid when the argument <CODE>split</CODE> is false. When
     * <CODE>split</CODE> is true, the number of grids is the number
     * of splines that make up the boundary.
     * <P>
     * When the 'split' argument has the value true, and either the
     * intermediate control points for a grid's edges in the 'U'
     * direction are explicitly modified or the number of vertices in
     * the U direction are larger than two, one may have to add
     * additional B&eacute;zier patches if the grids are to be connected to
     * each other. The typical case for setting 'split' to true is
     * when adding such patches is desired.
     * <P>
     * The final arguments are pairs of U-V indices indicating vertices
     * that are part of this grid's boundary.  Those boundary components
     * that match any of these pairs will be connected by the generated
     * grids, whereas other boundary components will not be connected.
     * For each grid that is generated the 'V' index
     * of the generated grid will increase as one traverses along the
     * boundary from the starting point specified by the indices, and the
     * u=0 edge of each generated grid will match its corresponding boundary.
     * @param grid the grid to which this grid should be connected.
     * @param n the number of vertices along the U axis for each of the
     *        grids that this method creates
     * @param split true if there can be multiple grids per boundary
     *        component; false otherwise
     * @param indices a sequence of pairs of U and V indices respectively
     *        for vertices along components of this grid's boundary
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, int n,
					    boolean split, int... indices)
	throws IllegalStateException, IllegalArgumentException
    {
	return createConnectionsTo(grid, n, split, false, indices);
    }

    /**
     * Create B&eacute;zier grids that will connect this grid to a
     * specified grid, specifying the number of vertices along a
     * connecting grid's U axis and optionally splitting the returned
     * grids into multiple grids, and specifying pairs of indices for
     * vertices along allowed or disallowed boundaries.
     * The indices of matching vertices on the boundaries of the two
     * grids must be the same.  The number of vertices for the
     * connecting grids along their U axes must be at least 2 and is
     * specified by this method's second argument. For their V axes,
     * the number varies, with each grid matching part of a boundary
     * of this grid.  The number of grids that are returned equals the
     * number of disjoint curves that make up the boundary of this
     * grid when the argument <CODE>split</CODE> is false. When
     * <CODE>split</CODE> is true, the number of grids is the number
     * of splines that make up the boundary.
     * <P>
     * When the 'split' argument has the value true, and either the
     * intermediate control points for a grid's edges in the 'U'
     * direction are explicitly modified or the number of vertices in
     * the U direction are larger than two, one may have to add
     * additional B&eacute;zier patches if the grids are to be connected to
     * each other. The typical case for setting 'split' to true is
     * when adding such patches is desired.
     * <P>
     * The final arguments are pairs of U-V indices indicating vertices
     * that are either part of this grid's boundary or part of this grid's
     * boundary that are excluded.  When the 'exclude' argument is false,
     * those boundary components that match any of these pairs will be
     * connected to the generated grids, whereas other boundary components
     * will not be connected. For the ones that are connected, the u=0 edge
     * of the generated grid will match the corresponding boundary of this
     * grid. The u=0, v= 0 corner of each generated grid will match the point
     * on this grid's boundary corresponding to the specified pair of indices.
     * When the 'exclude' argument is 'true', the indices indicate which
     * component of the boundary are not connected, and in this case the
     * starting point for the boundary is not specified and will in general
     * be hard to predict.
     * @param grid the grid to which this grid should be connected.
     * @param n the number of vertices along the U axis for each of the
     *        grids that this method creates
     * @param split true if there can be multiple grids per boundary
     *        component; false otherwise
     * @param exclude true if each pair of indices denotes a vertex along
     *        a component of the boundary that is excluded; false if
     *        each pair of indices denotes a vertex along a component of
     *        the boundary that is included
     * @param indices a sequence of pairs of U and V indices respectively
     *        for vertices along components of this grid's boundary
     * @return the grids that will connect corresponding components of
     *         boundaries of this grid and the specified grid
     * @exception IllegalStateException this grid is not well formed or
     *            its boundary cannot be computed.
     */
    public BezierGrid[] createConnectionsTo(BezierGrid grid, int n,
					    boolean split, boolean exclude,
					    int... indices)
	throws IllegalStateException, IllegalArgumentException
    {
	if (indices.length%2 != 0) {
	    throw new IllegalArgumentException(errorMsg("oddIndexCount"));
	}

	boolean useIndices = (indices.length != 0);

	Path3D boundary = (useIndices == false || exclude)? getBoundary():
	    getBoundary(indices);
	if (boundary == null) {
	    throw new IllegalStateException(errorMsg("noBoundaryForGrid"));
	}
	if (grid != null) grid.createSplines();
	int nm1 = n-1;
	ArrayList<Vertex[]> blist = new ArrayList<>();
	ArrayList<Integer[]> rlist = new ArrayList<>();
	LinkedList<Vertex> vlist = new LinkedList<>();
	LinkedList<Integer> vrlist = new LinkedList<>();
	LinkedList<Boolean> clist = new LinkedList<>();
	PathIterator3D pit = boundary.getPathIterator(null);
	double[] coords = new double[9];
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	int lastSplineID = -1;
	int initialSplineID = -1;
	int splineID = 0;
	boolean currentSplineCyclic = false;
	Vertex v = null;
	Vertex vn;
	ArrayList<BezierGrid> glist = new ArrayList<>();
	int regionID = 0;
	int count = 0;
	boolean keep = /*false;*/ exclude;
	HashMap<Integer,Integer> rmap = new HashMap<>();
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		keep = exclude? !keep: keep;
		if (useIndices && keep == false) {
		    while (count-- > 0) {
			blist.remove(blist.size()-1);
			rlist.remove(rlist.size()-1);
			clist.removeLast();
		    }
		}
		if (vlist.size() > 0 && (keep == true || useIndices == false)) {
		    // A SEG_MOVETO will always be preceded by a
		    // SEG_CLOSE, so the final vrlist entry was
		    // already added.
		    Vertex[] varray = vlist.toArray(new Vertex[vlist.size()]);
		    Integer[] iarray = vrlist.toArray
			(new Integer[vrlist.size()]);
		    if (gridsSeparated(varray, grid)) {
			if (varray.length/2 != iarray.length) {
			    int l1 = iarray.length;
			    int l2 = varray.length/2;
			    String msg =
				errorMsg("badRegionLen", l1, l2, "SEG_MOVETO");
			    throw new RuntimeException(msg);
			}
			blist.add(varray);
			rlist.add(iarray);
			clist.offer(currentSplineCyclic);
		    }
		}
		regionID = 0;
		vlist.clear();
		vrlist.clear();
		initialSplineID = -1;
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		lastSplineID = -1;
		currentSplineCyclic = true;
		keep = false;
		count = 0;
		// v = findVertex(lastx, lasty, lastz);
		v = null;
		/*
		  when v is null, mustKeep always returns false
		if (useIndices && mustKeep(v, indices)) {
		    keep = true;
		}
		*/
		// vlist.add(v);
		break;
	    case PathIterator3D.SEG_LINETO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_LINETO, coords);
		}
		vn = findVertex(v, coords[0], coords[1], coords[2]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_LINETO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    if (split) {
			Vertex[] varray =
			    vlist.toArray(new Vertex[vlist.size()]);
			vrlist.add(rmap.get(splineID));
			Integer[] iarray = vrlist.toArray
			    (new Integer[vrlist.size()]);
			if (gridsSeparated(varray, grid)) {
			    if (varray.length/2 != iarray.length) {
				int l1 = iarray.length;
				int l2 = varray.length/2;
				String t = "SEG_LINETO";
				String msg =
				    errorMsg("badRegionLen", l1, l2, t);
				throw new RuntimeException(msg);
			    }
			    count++;
			    blist.add(varray);
			    rlist.add(iarray);
			    clist.offer(false);
			}
			vrlist.clear();
			vlist.clear();
			vlist.add(v);
			vlist.add(v);
		    }
		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_QUADTO, coords);
		    // vlist.add(v);
		}
		vn = findVertex(v, coords[3], coords[4], coords[5]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_QUADTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_QUADTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    if (split) {
			Vertex[] varray =
			    vlist.toArray(new Vertex[vlist.size()]);
			vrlist.add(rmap.get(splineID));
			Integer[] iarray = vrlist.toArray
			    (new Integer[vrlist.size()]);
			if (gridsSeparated(varray, grid)) {
			    if (varray.length/2 != iarray.length) {
				int l1 = iarray.length;
				int l2 = varray.length/2;
				String t = "SEG_QUADTO";
				String msg =
				    errorMsg("badRegionLen", l1, l2, t);

				throw new RuntimeException(msg);
			    }
			    count++;
			    blist.add(varray);
			    rlist.add(iarray);
			    clist.offer(false);
			}
			vrlist.clear();
			vlist.clear();
			vlist.add(v);
			vlist.add(v);
		    }
		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_CUBICTO, coords);
		    // vlist.add(v);
		}
		vn = findVertex(v, coords[6], coords[7], coords[8]);
		if (vn == null) {
		    // switch to the alternate vertex
		    /*
		    Vertex va = findVertex(v);
		    if (va != null) {
			v = va;
			vn = findVertex(v, coords[6], coords[7], coords[8]);
		    }
		    */
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_CUBICTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_CUBICTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    if (split) {
			Vertex[] varray =
			    vlist.toArray(new Vertex[vlist.size()]);
			vrlist.add(rmap.get(splineID));
			Integer[] iarray = vrlist.toArray
			    (new Integer[vrlist.size()]);
			if (gridsSeparated(varray, grid)) {
			    if (varray.length/2 != iarray.length) {
				int l1 = iarray.length;
				int l2 = varray.length/2;
				String t = "SEG_CUBICTO";
				String msg =
				    errorMsg("badRegionLen", l1, l2, t);
				throw new RuntimeException(msg);
			    }
			    count++;
			    blist.add(varray);
			    rlist.add(iarray);
			    clist.offer(false);
			}
			vrlist.clear();
			vlist.clear();
			vlist.add(v);
			vlist.add(v);
		    }
		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CLOSE:
		// vn = findVertex(v, lastx, lasty, lastz);
		// vn is expected to be null because getBoundary()
		// returns paths in which the last iteration before a
		// SEG_CLOSE is at the starting point set by the last
		// SEG_MOVETO.  We handle the case where vn is not null
		// just in case.
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    vn = null;
		} else {
		    coords[0] = lastx;
		    coords[1] = lasty;
		    coords[2] = lastz;
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    if (vn == null) {
			v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				       PathIterator3D.SEG_LINETO, coords);
			vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    }
		}
		if (useIndices && vn != null
		    && mustKeep(vn, indices)) {
		    keep = true;
		}
		if (vn != null) {
		    splineID = findSplineID(vn, v);
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    vrlist.add(rmap.get(splineID));
		}
		if (currentSplineCyclic == false
		    /*initialSplineID != lastSplineID
		      || splineStatus[initialSplineID] == false */) {
		    if (vn != null) {
			vlist.add(v);
			vlist.add(vn);
		    }
		    if (vrlist.size() < vlist.size()) {
			// add the initial region ID because we looped.
			vrlist.add(0);
		    }
		    // currentSplineCyclic = false;
		} else {
		    if (vn == null) {
			int sz = vlist.size();
			if (sz > 0) {
			    vlist.remove(sz-1);
			    vlist.remove(sz-2);
			}
		    }
		    // currentSplineCyclic = true;
		}
		break;
	    }
	    pit.next();
	}
	keep = exclude? !keep: keep;
	if (useIndices && keep == false) {
	    while (count-- > 0) {
		blist.remove(blist.size()-1);
		rlist.remove(rlist.size()-1);
		clist.removeLast();
	    }
	}
	if (vlist.size() > 0 && (keep == true || useIndices == false)) {
	    Vertex[] varray = vlist.toArray(new Vertex[vlist.size()]);
	    Integer[] iarray = vrlist.toArray
		(new Integer[vrlist.size()]);
	    if (gridsSeparated(varray, grid)) {
		if (varray.length/2 != iarray.length) {
		    int l1 = iarray.length;
		    int l2 = varray.length/2;
		    String t = errorMsg("afterLoop");
		    String msg = errorMsg("badRegionLen", l1, l2, t);
		    throw new RuntimeException(msg);
		}
		blist.add(varray);
		rlist.add(iarray);
		clist.offer(currentSplineCyclic);
	    }
	}
	// now have lists of segments making up a boundary.
	int bindex = -1;
	ListIterator<Integer[]> rit = rlist.listIterator();
	for (Vertex[] vertices: blist) {

	    Integer[] regions = rit.next();
	    bindex++;
	    currentSplineCyclic = clist.poll();
	    /*
	    int gsize = currentSplineCyclic? vertices.length - 1:
		vertices.length;
	    */
	    int gsize = vertices.length/2;
	    BezierGrid connector =
		new BezierGrid(n, false, gsize, currentSplineCyclic);
	    connector.frozenUEnds1 = true;
	    if (grid != null) connector.frozenUEnds2 = true;
	    int k;
	    /*
	    if (vertices.length/2 != regions.length) {
		throw new RuntimeException("region length "
					   + regions.length
					   + ", expecting "
					   + vertices.length/2);
	    }
	    System.out.print("c" + bindex + ":");


	    System.out.format("(%d,%d)[%d] ", vertices[0].i,
			      vertices[0].j, regions[0]);
	    */
	    for (k = 2; k < vertices.length; k += 2) {
		Vertex vc1  = connector.array[0][k/2 - 1];
		Vertex vc2  = connector.array[nm1][k/2 - 1];
		v = vertices[k];
		Vertex nV = vertices[k+1];

		/*
		System.out.format("(%d,%d)[%d] ", nV.i, nV.j, regions[k/2]);
		if (k % 20 == 18) {
		    System.out.println();
		    System.out.print("    ");
		}
		if (k == vertices.length-2) {
		    System.out.println();
		}
		*/
		Vertex gV = (grid == null)? null: grid.array[v.i][v.j];
		Vertex gnV = (grid == null)? null: grid.array[nV.i][nV.j];
		vc1.p = v.p;
		vc1.filled = true;
		vc2.p = (gV == null)? null: gV.p;
		if (gV != null) vc2.filled = true;
		vc1.vc = new double[9];
		vc2.vc = new double[9];
		if (v.i == nV.i) {
		    boolean forward = (nV.j == v.j + 1)
			|| (vclosed && nV.j == 0 && v.j == nv-1);
		    if (forward) {
			if (v.vc != null) {
			    System.arraycopy(v.vc, 0, vc1.vc, 0, 9);
			} else {
			    vc1.vc = null;
			}
			if (gV != null && gV.vc != null) {
			    System.arraycopy(gV.vc, 0, vc2.vc, 0, 9);
			} else {
			    vc2.vc = null;
			}
		    } else {
			if (nV.vc != null) {
			    System.arraycopy(nV.vc, 3, vc1.vc, 0, 3);
			    System.arraycopy(nV.vc, 0, vc1.vc, 3, 3);
			    vc1.vc[6] = nV.p.getX();
			    vc1.vc[7] = nV.p.getY();
			    vc1.vc[8] = nV.p.getZ();
			} else {
			    vc1.vc = null;
			}
			if (gnV != null && gnV.vc != null) {
			    System.arraycopy(gnV.vc, 3, vc2.vc, 0, 3);
			    System.arraycopy(gnV.vc, 0, vc2.vc, 3, 3);
			    vc2.vc[6] = gnV.p.getX();
			    vc2.vc[7] = gnV.p.getY();
			    vc2.vc[8] = gnV.p.getZ();
			} else {
			    vc2.vc = null;
			}
		    }
		} else /* v.j = nV.j */ {
		    boolean forward = (nV.i == v.i + 1)
			|| (uclosed && nV.i == 0 && v.i == nu-1);
		    if (forward) {
			if (v.uc != null) {
			    System.arraycopy(v.uc, 0, vc1.vc, 0, 9);
			} else {
			    vc1.vc = null;
			}
			if (gV != null && gV.uc != null) {
			    System.arraycopy(gV.uc, 0, vc2.vc, 0, 9);
			} else {
			    vc2.vc = null;
			}
		    } else {
			if (nV.uc != null) {
			    System.arraycopy(nV.uc, 3, vc1.vc, 0, 3);
			    System.arraycopy(nV.uc, 0, vc1.vc, 3, 3);
			    vc1.vc[6] = nV.p.getX();
			    vc1.vc[7] = nV.p.getY();
			    vc1.vc[8] = nV.p.getZ();
			} else {
			    vc1.vc = null;
			}
			if (gnV != null && gnV.uc != null) {
			    System.arraycopy(gnV.uc, 3, vc2.vc, 0, 3);
			    System.arraycopy(gnV.uc, 0, vc2.vc, 3, 3);
			    vc2.vc[6] = gnV.p.getX();
			    vc2.vc[7] = gnV.p.getY();
			    vc2.vc[8] = gnV.p.getZ();
			} else {
			    vc2.vc = null;
			}
		    }
		}
	    }
	    if (currentSplineCyclic == false) {
		/*
		Vertex vc1  = connector.array[0][k/2];
		Vertex vc2  = connector.array[nm1][k/2];
		v = vertices[k-2];
		Vertex nV = vertices[k-1];
		System.out.format("v=(%d,%d), nV=(%d,%d)\n",
				  v.i, v.j, nV.i, nV.j);
		Vertex gV = (grid == null)? null: grid.array[nV.i][nV.j];
		vc1.p = nV.p;
		vc2.p = (gV == null)? null: gV.p;
		*/
		Vertex vc1  = connector.array[0][k/2 - 1];
		Vertex vc2  = connector.array[nm1][k/2 -1];
		Vertex nV = vertices[k-1];
		Vertex gV = (grid == null)? null: grid.array[nV.i][nV.j];
		vc1.p = nV.p;
		vc1.filled = true;
		vc2.p = (gV == null)? null: gV.p;
		if (gV != null) vc2.filled = true;
	    } else {
		Vertex vc1  = connector.array[0][k/2-1];
		Vertex vc2  = connector.array[nm1][k/2-1];
		v = vertices[k-1];
		Vertex nV = vertices[0];
		Vertex gV = (grid == null)? null: grid.array[v.i][v.j];
		Vertex gnV = (grid == null)? null: grid.array[nV.i][nV.j];
		vc1.p = v.p;
		vc1.filled = true;
		vc2.p = (gV == null)? null: gV.p;
		if (gV != null) vc2.filled = true;
		vc1.vc = new double[9];
		vc2.vc = new double[9];
		if (v.i == nV.i) {
		    boolean forward = (nV.j == v.j + 1)
			|| (vclosed && nV.j == 0 && v.j == nv-1);
		    if (forward) {
			if (v.vc != null) {
			    System.arraycopy(v.vc, 0, vc1.vc, 0, 9);
			} else {
			    vc1.vc = null;
			}
			if (gV != null && gV.vc != null) {
			    System.arraycopy(gV.vc, 0, vc2.vc, 0, 9);
			} else {
			    vc2.vc = null;
			}
		    } else {
			if (nV.vc != null) {
			    System.arraycopy(nV.vc, 3, vc1.vc, 0, 3);
			    System.arraycopy(nV.vc, 0, vc1.vc, 3, 3);
			    vc1.vc[6] = nV.p.getX();
			    vc1.vc[7] = nV.p.getY();
			    vc1.vc[8] = nV.p.getZ();
			} else {
			    vc1.vc = null;
			}
			if (gnV != null && gnV.vc != null) {
			    System.arraycopy(gnV.vc, 3, vc2.vc, 0, 3);
			    System.arraycopy(gnV.vc, 0, vc2.vc, 3, 3);
			    vc2.vc[6] = gnV.p.getX();
			    vc2.vc[7] = gnV.p.getY();
			    vc2.vc[8] = gnV.p.getZ();
			} else {
			    vc2.vc = null;
			}
		    }
		} else /* v.j = nV.j */ {
		    boolean forward = (nV.i == v.i + 1)
			|| (uclosed && nV.i == 0 && v.i == nu-1);
		    if (forward) {
			if (v.uc != null) {
			    System.arraycopy(v.uc, 0, vc1.vc, 0, 9);
			} else {
			    vc1.vc = null;
			}
			if (gV != null && gV.uc != null) {
			    System.arraycopy(gV.uc, 0, vc2.vc, 0, 9);
			} else {
			    vc2.vc = null;
			}
		    } else {
			if (nV.uc != null) {
			    System.arraycopy(nV.uc, 3, vc1.vc, 0, 3);
			    System.arraycopy(nV.uc, 0, vc1.vc, 3, 3);
			    vc1.vc[6] = nV.p.getX();
			    vc1.vc[7] = nV.p.getY();
			    vc1.vc[8] = nV.p.getZ();
			} else {
			    vc1.vc = null;
			}
			if (gnV != null && gnV.uc != null) {
			    System.arraycopy(gnV.uc, 3, vc2.vc, 0, 3);
			    System.arraycopy(gnV.uc, 0, vc2.vc, 3, 3);
			    vc2.vc[6] = gnV.p.getX();
			    vc2.vc[7] = gnV.p.getY();
			    vc2.vc[8] = gnV.p.getZ();
			} else {
			    vc2.vc = null;
			}
		    }
		}
	    }
	    for (k = 0; k < vertices.length; k += 2) {
		int region = regions[k/2];
		for (int i = 0; i < n; i++) {
		    connector.array[i][k/2].region = region;
		}
	    }
	    glist.add(connector);
	}
	return glist.toArray(new BezierGrid[glist.size()]);
    }

    static Point3DMapper.Type types[] = {
	Point3DMapper.Type.FIRST_CUBIC,
	Point3DMapper.Type.SECOND_CUBIC,
	Point3DMapper.Type.KNOT
    };

    private void doMapping(Point3DMapper<Point3D> mapping,
			   int ind, double[] coords1, double[] coords2,
			   Point3D p1, Point3D p2)
    {
	for (int i = 0; i < coords1.length; i+= 3) {
	    Point3DMapper.Type type = types[i%3];
	    Point3D p = new
		Point3D.Double(coords1[i], coords1[i+1], coords1[i+2]);
	    p = mapping.apply(ind, p, type, p1, p2);
	    coords2[i] = (double)(float)p.getX();
	    coords2[i+1] = (double)(float)p.getY();
	    coords2[i+2]  = (double)(float)p.getZ();
	}
    }

    static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Create an extension to a B&eacute;zier grid based on a mapping
     * function.
     * This method creates a new BezierGrid. The number of grid
     * elements in the new grid's V direction are determined by a
     * component of the boundary of this grid. That component will a
     * closed, continuous path that passes through the grid point
     * whose U-V indices are (uIndex, vIndex).  If the point at
     * (uIndex, vIndex) is not on the boundary, null is returned;
     * Otherwise the boundary that is used is the component of the
     * full boundary that contains the point corresponding to
     * (uIndex, vIndex).  This component will be shifted so that
     * it's first segment (its SEG_MOVETO segment) has coordinates
     * equal to the value of the grid point corresponding to the
     * indices (uIndex, vIndex).  This point will be located at
     * indices (0,0) in the extension grid, with increasing V
     * values following the boundary.
     * <P>
     * The grid returned will be frozen. The grid points and
     * intermediate control points in the V direction are
     * determined by the mapping function. Splines determine the
     * intermediate control points in the U direction. For a fixed
     * value of V, splines in the U direction or terminated or started
     * at U indices specified by the second argument (regionChanges).
     * <P>
     * The mapping function takes the following arguments:
     * <UL>
     *   <LI> i. This argument is the index in the U direction. Its
     *        values are in the range (0, n).
     *   <LI> p. This argument is a grid point for which U is zero.
     *   <LI> p1. This argument is optional. When present, the argument
     *        p is a control point that is not end point of a segment
     *        that is part of the boundary. p1 will be the starting point
     *         of the segment containing p.
     *   <LI> p2. This argument is optional. When present, the argument
     *        p is a control point that is not an end point of a segment
     *        that is part of the boundary. p2 will be the ending point
     *         of the segment containing p.
     * </UL>
     * If p1 or p2 is provided, the other must be provided as well. These
     * optional arguments are implemented by a variable argument whose
     * type is Point3D, so the implementation of the mapping function
     * will have as its last argument an array of length 0 or 2.
     * @param mapping the mapping function
     * @param regionChanges a list of indices for the U direction
     *        for which the grid that is created changes regions
     *        (this argument is an array that may be modified by
     *         sorting it into ascending order).
     * @param n the number of grid points in the U direction
     * @param uIndex the U-direction index for a point on this grid
     *        that is part of this grid's boundary.
     * @param vIndex the V-direction index for a point on this grid
     *        that is part of this grid's boundary.
     * @return a BezierGrid; null if (uIndex, vIndex) is not on a
     *         boundary.
     */
    public BezierGrid
	createExtensionGrid(Point3DMapper<Point3D> mapping,
			    int[] regionChanges,
			    int n, int uIndex, int vIndex)
	throws IllegalStateException, IllegalArgumentException
    {
	boolean exclude = false;
	int indices[] = {uIndex, vIndex};

	Path3D boundary = getBoundary(uIndex, vIndex);
	if (boundary == null) {
	    throw new IllegalStateException(errorMsg("noBoundaryForGrid"));
	}
	int nm1 = n-1;
	ArrayList<Vertex[]> blist = new ArrayList<>();
	ArrayList<Integer[]> rlist = new ArrayList<>();
	LinkedList<Vertex> vlist = new LinkedList<>();
	LinkedList<Integer> vrlist = new LinkedList<>();
	LinkedList<Boolean> clist = new LinkedList<>();
	PathIterator3D pit = boundary.getPathIterator(null);
	double[] coords = new double[9];
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	int lastSplineID = -1;
	int initialSplineID = -1;
	int splineID = 0;
	boolean currentSplineCyclic = false;
	Vertex[] varray = null;
	Integer[] iarray = null;
	Vertex v = null;
	Vertex vn;
	ArrayList<BezierGrid> glist = new ArrayList<>();
	int regionID = 0;
	boolean keep = false;
	HashMap<Integer,Integer> rmap = new HashMap<>();
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		if (vlist.size() > 0 && (keep == true)) {
		    // A SEG_MOVETO will always be preceded by a
		    // SEG_CLOSE, so the final vrlist entry was
		    // already added.
		    varray = vlist.toArray(new Vertex[vlist.size()]);
		    iarray = vrlist.toArray
			(new Integer[vrlist.size()]);
		    if (varray.length/2 != iarray.length) {
			int l1 = iarray.length;
			int l2 = varray.length/2;
			String t = "SEG_MOVETO";
			String msg = errorMsg("badRegionLen", l1, l2, t);
			throw new RuntimeException(msg);
		    }
		    // blist.add(varray);
		    // rlist.add(iarray);
		    // clist.offer(currentSplineCyclic);
		}
		regionID = 0;
		vlist.clear();
		vrlist.clear();
		initialSplineID = -1;
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		lastSplineID = -1;
		currentSplineCyclic = true;
		keep = false;
		// v = findVertex(lastx, lasty, lastz);
		v = null;
		if (mustKeep(v, indices)) {
		    keep = true;
		}
		// vlist.add(v);
		break;
	    case PathIterator3D.SEG_LINETO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_LINETO, coords);
		}
		vn = findVertex(v, coords[0], coords[1], coords[2]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_LINETO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }

		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_QUADTO, coords);
		    // vlist.add(v);
		}
		vn = findVertex(v, coords[3], coords[4], coords[5]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_QUADTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_QUADTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }

		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_CUBICTO, coords);
		    // vlist.add(v);
		}
		vn = findVertex(v, coords[6], coords[7], coords[8]);
		if (vn == null) {
		    // switch to the alternate vertex
		    /*
		      Vertex va = findVertex(v);
		      if (va != null) {
		      v = va;
		      vn = findVertex(v, coords[6], coords[7], coords[8]);
		      }
		    */
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_CUBICTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_CUBICTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (mustKeep(vn, indices)) {
		    keep = true;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }

		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vrlist.add(rmap.get(splineID));
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CLOSE:
		// vn = findVertex(v, lastx, lasty, lastz);
		// vn is expected to be null because getBoundary()
		// returns paths in which the last iteration before a
		// SEG_CLOSE is at the starting point set by the last
		// SEG_MOVETO.  We handle the case where vn is not null
		// just in case.
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    vn = null;
		} else {
		    coords[0] = lastx;
		    coords[1] = lasty;
		    coords[2] = lastz;
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    if (vn == null) {
			v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				       PathIterator3D.SEG_LINETO, coords);
			vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    }
		}
		if (vn != null
		    && mustKeep(vn, indices)) {
		    keep = true;
		}
		if (vn != null) {
		    splineID = findSplineID(vn, v);
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    vrlist.add(rmap.get(splineID));
		}
		if (currentSplineCyclic == false
		    /*initialSplineID != lastSplineID
		      || splineStatus[initialSplineID] == false */) {
		    if (vn != null) {
			vlist.add(v);
			vlist.add(vn);
		    }
		    if (vrlist.size() < vlist.size()) {
			// add the initial region ID because we looped.
			vrlist.add(0);
		    }
		    // currentSplineCyclic = false;
		} else {
		    if (vn == null) {
			int sz = vlist.size();
			if (sz > 0) {
			    vlist.remove(sz-1);
			    vlist.remove(sz-2);
			}
		    }
		    // currentSplineCyclic = true;
		}
		break;
	    }
	    pit.next();
	}
	if (vlist.size() > 0 && (keep == true)) {
	    varray = vlist.toArray(new Vertex[vlist.size()]);
	    iarray = vrlist.toArray
		(new Integer[vrlist.size()]);
	    if (varray.length/2 != iarray.length) {
		int l1 = iarray.length;
		int l2 = varray.length/2;
		String t = errorMsg("afterLoop");
		String msg = errorMsg("badRegionLen", l1, l2, t);
		throw new RuntimeException(msg);
	    }
	}
	// now have lists of segments making up a boundary.
	// int bindex = -1;
	// ListIterator<Integer[]> rit = rlist.listIterator();
	Vertex[] vertices = varray;

	if (vertices.length == 0) return null;

	Integer[] regions = iarray;
	// bindex++;
	// currentSplineCyclic = clist.poll();
	/*
	  int gsize = currentSplineCyclic? vertices.length - 1:
	  vertices.length;
	*/
	int gsize = vertices.length/2;
	BezierGrid connector =
	    new BezierGrid(n, false, gsize, currentSplineCyclic);
	connector.frozenUEnds1 = true;
	connector.frozenUEnds2 = false;
	int k;

	for (k = 2; k < vertices.length; k += 2) {
	    Vertex vc1  = connector.array[0][k/2 - 1];
	    // Vertex vc2  = connector.array[nm1][k/2 - 1];
	    v = vertices[k];
	    Vertex nV = vertices[k+1];

	    /*
	      System.out.format("(%d,%d)[%d] ", nV.i, nV.j, regions[k/2]);
	      if (k % 20 == 18) {
	      System.out.println();
	      System.out.print("    ");
	      }
	      if (k == vertices.length-2) {
	      System.out.println();
	      }
	    */
	    // Vertex gV = (grid == null)? null: grid.array[v.i][v.j];
	    // Vertex gnV = (grid == null)? null: grid.array[nV.i][nV.j];
	    vc1.p = v.p;
	    vc1.filled = true;
	    // vc2.p = (gV == null)? null: gV.p;
	    for (int i = 1; i < n; i++) {
		Vertex vc2 = connector.array[i][k/2 - 1];
		// vc2.p = mapping.apply(i, vc1.p);
		Point3D newp =
		    mapping.apply(i, vc1.p, Point3DMapper.Type.KNOT);
		vc2.p = new Point3D.Double((double)(float)newp.getX(),
					   (double)(float)newp.getY(),
					   (double)(float)newp.getZ());
		vc2.filled = true;
	    }
	}
	if (currentSplineCyclic == false) {
	    /*
	      Vertex vc1  = connector.array[0][k/2];
	      Vertex vc2  = connector.array[nm1][k/2];
	      v = vertices[k-2];
	      Vertex nV = vertices[k-1];
	      System.out.format("v=(%d,%d), nV=(%d,%d)\n",
	      v.i, v.j, nV.i, nV.j);
	      Vertex gV = (grid == null)? null: grid.array[nV.i][nV.j];
	      vc1.p = nV.p;
	      vc2.p = (gV == null)? null: gV.p;
	    */
	    Vertex vc1  = connector.array[0][k/2 - 1];
	    // Vertex vc2  = connector.array[nm1][k/2 -1];
	    Vertex nV = vertices[k-1];
	    vc1.p = nV.p;
	    vc1.filled = true;
	    for (int i = 1; i < n; i++) {
		Vertex vc2 = connector.array[i][k/2 - 1];
		// vc2.p = mapping.apply(i, vc1.p);
		Point3D newp =
		    mapping.apply(i, vc1.p, Point3DMapper.Type.KNOT);
		vc2.p = new Point3D.Double((double)(float)newp.getX(),
					   (double)(float)newp.getY(),
					   (double)(float)newp.getZ());
		vc2.filled = true;
	    }
	} else {
	    Vertex vc1  = connector.array[0][k/2-1];
	    // Vertex vc2  = connector.array[nm1][k/2-1];
	    v = vertices[k-1];
	    Vertex nV = vertices[0];
	    // Vertex gV = (grid == null)? null: grid.array[v.i][v.j];
	    // Vertex gnV = (grid == null)? null: grid.array[nV.i][nV.j];
	    vc1.p = v.p;
	    vc1.filled = true;
	    for (int i = 0; i < n; i++) {
		Vertex vc2 = connector.array[i][k/2-1];
		// vc2.p = mapping.apply(i, vc1.p);
		Point3D newp =
		    mapping.apply(i, vc1.p, Point3DMapper.Type.KNOT);
		vc2.p = new Point3D.Double((double)(float)newp.getX(),
					   (double)(float)newp.getY(),
					   (double)(float)newp.getZ());
		vc2.filled = true;
	    }
	}
	int minRegion = Integer.MAX_VALUE;
	int maxRegion = Integer.MIN_VALUE;
	for (k = 0; k < vertices.length; k += 2) {
	    int region = regions[k/2];
	    for (int i = 0; i < n; i++) {
		connector.array[i][k/2].region = region;
	    }
	    if (region < minRegion) minRegion = region;
	    if (region > maxRegion) maxRegion = region;
	}
	int delta = maxRegion - minRegion;
	int regionOffset = 0;
	int p = 0;
	if (regionChanges == null) regionChanges = EMPTY_INT_ARRAY;
	Arrays.sort(regionChanges);
	for (int i = 0; i < n; i++) {
	    if (p < regionChanges.length && i == regionChanges[p]) {
		while (p < regionChanges.length && i == regionChanges[p]) {
		    p++;
		}
		regionOffset += delta;
	    }
	    for (k = 0; k < vertices.length; k += 2) {
		connector.array[i][k/2].region += regionOffset;
	    }
	}
	// createSplines called so we will create the splines
	// in the U direction.  We will overwrite the ones in
	// the V direction.
	connector.createSplines();

	for (k = 2; k < vertices.length; k += 2) {
	    Vertex vc1  = connector.array[0][k/2 - 1];
	    // Vertex vc2  = connector.array[nm1][k/2 - 1];
	    v = vertices[k];
	    Vertex nV = vertices[k+1];
	    vc1.vc = new double[9];
	    // vc2.vc = new double[9];
	    if (v.i == nV.i) {
		boolean forward = (nV.j == v.j + 1)
		    || (vclosed && nV.j == 0 && v.j == nv-1);
		if (forward) {
		    if (v.vc != null) {
			System.arraycopy(v.vc, 0, vc1.vc, 0, 9);
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		} else {
		    if (nV.vc != null) {
			System.arraycopy(nV.vc, 3, vc1.vc, 0, 3);
			System.arraycopy(nV.vc, 0, vc1.vc, 3, 3);
			vc1.vc[6] = nV.p.getX();
			vc1.vc[7] = nV.p.getY();
			vc1.vc[8] = nV.p.getZ();
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		}
	    } else /* v.j = nV.j */ {
		boolean forward = (nV.i == v.i + 1)
		    || (uclosed && nV.i == 0 && v.i == nu-1);
		if (forward) {
		    if (v.uc != null) {
			System.arraycopy(v.uc, 0, vc1.vc, 0, 9);
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		} else {
		    if (nV.uc != null) {
			System.arraycopy(nV.uc, 3, vc1.vc, 0, 3);
			System.arraycopy(nV.uc, 0, vc1.vc, 3, 3);
			vc1.vc[6] = nV.p.getX();
			vc1.vc[7] = nV.p.getY();
			vc1.vc[8] = nV.p.getZ();
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		}
	    }
	}
	if (currentSplineCyclic == false) {
	    /*
	      Vertex vc1  = connector.array[0][k/2];
	      Vertex vc2  = connector.array[nm1][k/2];
	      v = vertices[k-2];
	      Vertex nV = vertices[k-1];
	      System.out.format("v=(%d,%d), nV=(%d,%d)\n",
	      v.i, v.j, nV.i, nV.j);
	      Vertex gV = (grid == null)? null: grid.array[nV.i][nV.j];
	      vc1.p = nV.p;
	      vc2.p = (gV == null)? null: gV.p;
	    */
	    Vertex vc1  = connector.array[0][k/2 - 1];
	    Vertex vc2  = connector.array[nm1][k/2 -1];
	    Vertex nV = vertices[k-1];
	    // Vertex gV = (grid == null)? null: grid.array[nV.i][nV.j];
	    vc1.p = nV.p;
	    vc1.filled = true;
	    // vc2.p = (gV == null)? null: gV.p;
	    vc2.p = mapping.apply(nm1, vc1.p, Point3DMapper.Type.KNOT);
	    Point3D newp =
		mapping.apply(nm1, vc1.p, Point3DMapper.Type.KNOT);
	    vc2.p = new Point3D.Double((double)(float)newp.getX(),
				       (double)(float)newp.getY(),
				       (double)(float)newp.getZ());
	    vc2.filled = true;
	    // if (gV != null) vc2.filled = true;
	} else {
	    Vertex vc1  = connector.array[0][k/2-1];
	    // Vertex vc2  = connector.array[nm1][k/2-1];
	    v = vertices[k-1];
	    Vertex nV = vertices[0];
	    // Vertex gV = (grid == null)? null: grid.array[v.i][v.j];
	    // Vertex gnV = (grid == null)? null: grid.array[nV.i][nV.j];
	    // vc2.p = (gV == null)? null: gV.p;
	    // if (gV != null) vc2.filled = true;
	    vc1.vc = new double[9];
	    // vc2.vc = new double[9];
	    if (v.i == nV.i) {
		boolean forward = (nV.j == v.j + 1)
		    || (vclosed && nV.j == 0 && v.j == nv-1);
		if (forward) {
		    if (v.vc != null) {
			System.arraycopy(v.vc, 0, vc1.vc, 0, 9);
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		} else {
		    if (nV.vc != null) {
			System.arraycopy(nV.vc, 3, vc1.vc, 0, 3);
			System.arraycopy(nV.vc, 0, vc1.vc, 3, 3);
			vc1.vc[6] = nV.p.getX();
			vc1.vc[7] = nV.p.getY();
			vc1.vc[8] = nV.p.getZ();
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		}
	    } else /* v.j = nV.j */ {
		boolean forward = (nV.i == v.i + 1)
		    || (uclosed && nV.i == 0 && v.i == nu-1);
		if (forward) {
		    if (v.uc != null) {
			System.arraycopy(v.uc, 0, vc1.vc, 0, 9);
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc  = null;
			}
		    }
		} else {
		    if (nV.uc != null) {
			System.arraycopy(nV.uc, 3, vc1.vc, 0, 3);
			System.arraycopy(nV.uc, 0, vc1.vc, 3, 3);
			vc1.vc[6] = nV.p.getX();
			vc1.vc[7] = nV.p.getY();
			vc1.vc[8] = nV.p.getZ();
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    if (vc2.vc == null) vc2.vc = new double[9];
			    doMapping(mapping, i, vc1.vc, vc2.vc,
				      v.p, nV.p);
			}
		    } else {
			vc1.vc = null;
			for (int i = 1; i < n; i++) {
			    Vertex vc2 = connector.array[i][k/2 - 1];
			    vc2.vc = null;
			}
		    }
		}
	    }
	}
	connector.frozen = true;
	return connector;
    }


    @Override
    public Path3D getBoundary() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return bpath;
    }

    /**
     * Get the boundary components that pass through specific grid points.
     * The indices that are specified are pairs of indices giving the
     * U-index and V-index respectively for a point on the grid.  The
     * path returned will consist of a concatenation of these
     * boundary components, each modified so it starts at the point
     * corresponding to a pair of indices. The order of these components
     * may not be same as the order of the index pairs.
     * @param indices the indices
     * @return the boundary components, concatenated into a single
     *         path
     */
    public Path3D getBoundary(int... indices)
	throws IllegalStateException, IllegalArgumentException
    {
	// boolean exclude = false;
	if (indices.length%2 != 0) {
	    throw new IllegalArgumentException(errorMsg("oddArgs"));
	}

	for (int k = 0; k < indices.length; k += 2) {
	    int indU = indices[k];
	    int  indV = indices[k+1];
	    if (indU < 0 || indU >= nu || indV < 0 || indV >= nv) {
		throw new
		    IllegalArgumentException(errorMsg("uvRange", indU, indV));
	    }
	    if (array[indU][indV].p == null) {
		throw new IllegalArgumentException
		    (errorMsg("noindUVPoint", indU, indV));
	    } else {
		Point3D p = array[indU][indV].p;
		if (p.getX() != (double)(float)p.getX()
		    || p.getY() != (double)(float)p.getY()
		    || p.getZ() != (double)(float)p.getZ()) {
		    throw new RuntimeException("(double)(float) missing");
		}
	    }
	}

	boolean useIndices = (indices.length != 0);

	Path3D boundary = getBoundary();
	if (boundary == null) {
	    throw new IllegalStateException(errorMsg("noBoundaryForGrid"));
	}
	if (useIndices == false) {
	    // the boundary is well formed, but there are no components
	    // in it that match.
	    return new Path3D.Double();
	}
	ArrayList<Vertex[]> blist = new ArrayList<>();
	LinkedList<Vertex> vlist = new LinkedList<>();
	LinkedList<Vertex> svlist = new LinkedList<>();
	LinkedList<Boolean> clist = new LinkedList<>();
	PathIterator3D pit = boundary.getPathIterator(null);
	double[] coords = new double[9];
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	int lastSplineID = -1;
	int initialSplineID = -1;
	int splineID = 0;
	boolean currentSplineCyclic = false;
	Vertex v = null;
	Vertex vn;
	Vertex sv = null;	// what will be the starting vertex
	ArrayList<BezierGrid> glist = new ArrayList<>();
	int regionID = 0;
	int count = 0;
	boolean keep = false;
	HashMap<Integer,Integer> rmap = new HashMap<>();
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		// keep = exclude? !keep: keep;
		if (useIndices && keep == false) {
		    while (count-- > 0) {
			blist.remove(blist.size()-1);
			svlist.remove(svlist.size()-1);
			clist.removeLast();
		    }
		}
		if (vlist.size() > 0 && (keep == true || useIndices == false)) {
		    // A SEG_MOVETO will always be preceded by a
		    // SEG_CLOSE.
		    Vertex[] varray = vlist.toArray(new Vertex[vlist.size()]);
		    blist.add(varray);
		    clist.offer(currentSplineCyclic);
		    svlist.add(sv);
		}
		regionID = 0;
		vlist.clear();
		initialSplineID = -1;
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		lastSplineID = -1;
		currentSplineCyclic = true;
		keep = false;
		sv = null;
		count = 0;
		// v = findVertex(lastx, lasty, lastz);
		v = null;
		/*
		  when v is null, mustKeep always returns false
		if (useIndices && mustKeep(v, indices)) {
		    keep = true;
		}
		*/
		// vlist.add(v);
		break;
	    case PathIterator3D.SEG_LINETO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_LINETO, coords);
		}
		vn = findVertex(v, coords[0], coords[1], coords[2]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_LINETO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		    sv = vn;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }

		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_QUADTO, coords);
		    // vlist.add(v);
		}
		vn = findVertex(v, coords[3], coords[4], coords[5]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_QUADTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_QUADTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		    sv = vn;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (v == null) {
		    v = findVertex(lastx, lasty, lastz,
				   PathIterator3D.SEG_CUBICTO, coords);
		}
		vn = findVertex(v, coords[6], coords[7], coords[8]);
		if (vn == null) {
		    v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				   PathIterator3D.SEG_CUBICTO, coords);
		    vn = findVertex(v, PathIterator3D.SEG_CUBICTO, coords);
		}
		if (vlist.size() == 0) {
		    vlist.add(v);
		    vlist.add(v);
		}
		if (useIndices && mustKeep(vn, indices)) {
		    keep = true;
		    sv = vn;
		}
		splineID = findSplineID(vn, v);
		if (initialSplineID == -1) {
		    lastSplineID = splineID;
		    initialSplineID = splineID;
		    rmap.put(splineID, regionID);
		}
		if (splineID != lastSplineID) {
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		    lastSplineID = splineID;
		    currentSplineCyclic = false;
		}
		vlist.add(v);
		v = vn;
		vlist.add(v);
		break;
	    case PathIterator3D.SEG_CLOSE:
		// vn = findVertex(v, lastx, lasty, lastz);
		// vn is expected to be null because getBoundary()
		// returns paths in which the last iteration before a
		// SEG_CLOSE is at the starting point set by the last
		// SEG_MOVETO.  We handle the case where vn is not null
		// just in case.
		if (v.p.getX() == lastx && v.p.getY() == lasty
		    && v.p.getZ() == lastz) {
		    vn = null;
		} else {
		    coords[0] = lastx;
		    coords[1] = lasty;
		    coords[2] = lastz;
		    vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    if (vn == null) {
			v = findVertex(v.p.getX(), v.p.getY(), v.p.getZ(),
				       PathIterator3D.SEG_LINETO, coords);
			vn = findVertex(v, PathIterator3D.SEG_LINETO, coords);
		    }
		}
		if (useIndices && vn != null
		    && mustKeep(vn, indices)) {
		    keep = true;
		    sv = vn;
		}
		if (vn != null) {
		    splineID = findSplineID(vn, v);
		    if (!rmap.containsKey(splineID)) {
			regionID++;
			rmap.put(splineID, regionID);
		    }
		}
		if (currentSplineCyclic == false
		    /*initialSplineID != lastSplineID
		      || splineStatus[initialSplineID] == false */) {
		    if (vn != null) {
			vlist.add(v);
			vlist.add(vn);
		    }
		    // currentSplineCyclic = false;
		} else {
		    if (vn == null) {
			int sz = vlist.size();
			if (sz > 0) {
			    vlist.remove(sz-1);
			    vlist.remove(sz-2);
			}
		    }
		    // currentSplineCyclic = true;
		}
		break;
	    }
	    pit.next();
	}
	//keep = exclude? !keep: keep;
	if (useIndices && keep == false) {
	    while (count-- > 0) {
		blist.remove(blist.size()-1);
		svlist.remove(svlist.size()-1);
		clist.removeLast();
	    }
	}
	if (vlist.size() > 0 && (keep == true || useIndices == false)) {
	    Vertex[] varray = vlist.toArray(new Vertex[vlist.size()]);
	    blist.add(varray);
	    svlist.add(sv);
	    clist.offer(currentSplineCyclic);
	}
	// now have lists of segments making up a boundary.
	Path3D rpath = new Path3D.Double();
	for (Vertex[] vertices: blist) {
	    Path3D path = new Path3D.Double();
	    currentSplineCyclic = clist.poll();
	    sv = svlist.poll();
	    int k;
	    v = vertices[0];
	    path.moveTo(v.p.getX(), v.p.getY(), v.p.getZ());
	    for (k = 2; k < vertices.length; k += 2) {
		v = vertices[k];
		Vertex nV = vertices[k+1];
		if (v.i == nV.i) {
		    boolean forward = (nV.j == v.j + 1)
			|| (vclosed && nV.j == 0 && v.j == nv-1);
		    if (forward) {
			if (v.vc != null) {
			    path.curveTo(v.vc[0], v.vc[1], v.vc[2],
					 v.vc[3], v.vc[4], v.vc[5],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    } else {
			if (nV.vc != null) {
			    path.curveTo(nV.vc[3], nV.vc[4], nV.vc[5],
					 nV.vc[0], nV.vc[1], nV.vc[2],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    }
		} else /* v.j = nV.j */ {
		    boolean forward = (nV.i == v.i + 1)
			|| (uclosed && nV.i == 0 && v.i == nu-1);
		    if (forward) {
			if (v.uc != null) {
			    path.curveTo(v.uc[0], v.uc[1], v.uc[2],
					 v.uc[3], v.uc[4], v.uc[5],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    } else {
			if (nV.uc != null) {
			    path.curveTo(nV.uc[3], nV.uc[4], nV.uc[5],
					 nV.uc[0], nV.uc[1], nV.uc[2],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    }
		}
	    }
	    if (currentSplineCyclic == false) {
		path.closePath();
	    } else {
		v = vertices[k-1];
		Vertex nV = vertices[0];
		if (v.i == nV.i) {
		    boolean forward = (nV.j == v.j + 1)
			|| (vclosed && nV.j == 0 && v.j == nv-1);
		    if (forward) {
			if (v.vc != null) {
			    path.curveTo(v.vc[0], v.vc[1], v.vc[2],
					 v.vc[3], v.vc[4], v.vc[5],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    } else {
			if (nV.vc != null) {
			    path.curveTo(nV.vc[3], nV.vc[4], nV.vc[5],
					 nV.vc[0], nV.vc[1], nV.vc[2],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    }
		} else /* v.j = nV.j */ {
		    boolean forward = (nV.i == v.i + 1)
			|| (uclosed && nV.i == 0 && v.i == nu-1);
		    if (forward) {
			if (v.uc != null) {
			    path.curveTo(v.uc[0], v.uc[1], v.uc[2],
					 v.uc[3], v.uc[4], v.uc[5],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    } else {
			if (nV.uc != null) {
			    path.curveTo(nV.uc[3], nV.uc[4], nV.uc[5],
					 nV.uc[0], nV.uc[1], nV.uc[2],
					 nV.p.getX(), nV.p.getY(), nV.p.getZ());
			} else {
			    path.lineTo(nV.p.getX(), nV.p.getY(), nV.p.getZ());
			}
		    }
		}
		path.closePath();
	    }
	    if (sv != null) {
		double ourX = sv.p.getX();
		double ourY = sv.p.getY();
		double ourZ = sv.p.getZ();
		path = Path3DInfo.shiftClosedPath(path, ourX, ourY, ourZ);
	    }
	    rpath.append(path, false);
	}
	return rpath;
    }

    @Override
    public boolean isClosedManifold() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null) && bpath.isEmpty();
    }

    Rectangle3D brect = null;

    @Override
    public Rectangle3D getBounds() {
	createSplines();
	if (brect == null) {
	    double[] coords = new double[48];
	    SurfaceIterator sit = getSurfaceIterator(null);
	    while (!sit.isDone()) {
		sit.currentSegment(coords);
		if (brect == null) {
		    brect = new
			Rectangle3D.Double(coords[0], coords[1], coords[2],
					   0.0, 0.0, 0.0);
		    for (int i = 3; i < 48; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		} else {
		    for (int i = 0; i < 48; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		}
		sit.next();
	    }
	}
	return brect;
    }

    @Override
    public boolean isOriented() {return true;}

    /**
     * Set the color for all patches on this grid.
     * @param c the color; null if a color is not specified
     */
    public void setColor(Color c) {
	for (int i = 0; i < nu; i++) {
	    for (int j = 0; j < nv; j++) {
		array[i][j].color = c;
	    }
	}
    }

    /**
     * Set the color for a patch on this grid.
     * The indices must be non-negative. For an index pair (i,j),
     * i must be less than the number of U values on the grid and
     * j must be less than the number of V values on the grid.
     * Indices start at 0.
     * @param i the index for the U direction
     * @param j the index for the V direction
     * @param c the color; null if a color is not specified
     * @exception IllegalArgumentException an integer argument was
     *            out of range
     */
    public void setColor(int i, int j, Color c)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= nu || j < 0 || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	array[i][j].color = c;
    }

    /**
     * Set the color for all patches on this grid.
     * The indices must be non-negative. For an index pair (i,j),
     * i must be less than the number of U values on the grid and
     * j must be less than the number of V values on the grid.
     * Indices start at 0. Both w and h must be non-negative. In
     * addition, the values of i+w and j+h must not exceed the
     * number of grid points in the U and V directions respectively.
     * @param i the index for the U direction
     * @param j the index for the V direction
     * @param w the number of indices in the U direction
     * @param h the number of indices in the V diction
     * @param c the color; null if a color is not specified
     * @exception IllegalArgumentException an integer argument was
     *            out of range
     */
    public void setColor (int i, int j, int w, int h, Color c)
	throws IllegalArgumentException
    {
	int ilimit = i+w;
	int jlimit = j+h;
	if (i < 0 || i >= nu || j < 0 || j >= nv) {
	    String msg = errorMsg("argOutOfRange2ii", i, j);
	    throw new IllegalArgumentException(msg);
	}
	if (w < 0 || h < 0 || i + w > nu || j+h > nv) {
	    String msg = errorMsg("widthHeight", w, h);
	    throw new IllegalArgumentException(msg);
	}

	while (i < ilimit) {
	    int k = j;
	    while (k < jlimit) {
		array[i][k].color = c;
		k++;
	    }
	    i++;
	}
    }
}

//  LocalWords:  eacute zier BezierGrid exbundle indices mdash sarray
//  LocalWords:  uclosed vclosed tarray xfunct yfunct zfunct sarray's
//  LocalWords:  tarray's IllegalArgumentException endSpline boolean
//  LocalWords:  IllegalStateException Appendable suclosed sgrid pc
//  LocalWords:  sspline noOut vertexnuc vertexuc vertexnnuc vertexvc
//  LocalWords:  vertexnvc vertexnnvc DInfo printSegments nv ij uv ci
//  LocalWords:  BLOCKQUOTE startSpline argOutOfRange SplinePath NaN
//  LocalWords:  moveU moveV splineNotStarted splineSpecTooShort getX
//  LocalWords:  cannotCloseSpline nonCyclic vertexNaN getY getZ cj
//  LocalWords:  SplinePoint expectingMoveTo ni nj coords uc vc ucq
//  LocalWords:  RuntimeException sqrt uvq num nvm argarraylength PRE
//  LocalWords:  setSplineU setSplineV bgfrozen setRegion unary IMG
//  LocalWords:  setRemainingControlPoints setPoint createSplines SRC
//  LocalWords:  getFullSplineU getFullSplineV getSplineU getSplineV
//  LocalWords:  widthHeight lt nanglesHalf createConnectionsTo cgrid
//  LocalWords:  limu getUArrayLength limv getVArrayLength getPoint
//  LocalWords:  setTessellationLevel createImageSequence isq png SEG
//  LocalWords:  FileOutputStream ucSet vcSet prev findVertex lastx
//  LocalWords:  lasty lastz lastv bgSpline bgSplineDiag MOVETO gsize
//  LocalWords:  vrlist initialSplineID lastSplineID splineStatus nV
//  LocalWords:  currentSplineCyclic bindex noBoundaryForGrid subgrid
//  LocalWords:  getBoundary le isin ouml bius affine AffineTransform
//  LocalWords:  af mgrid arraycopy frozenUEnds vlist LINETO QUADTO
//  LocalWords:  va vn CUBICTO gV UnaryOperator sFilled createSpline
//  LocalWords:  nbsp getPatch mustKeep useIndices uIndex vIndex rit
//  LocalWords:  regionChanges blist varray rlist iarray clist gnV
//  LocalWords:  ListIterator listIterator indU indV oddArgs uvRange
//  LocalWords:  noindUVPoint incompleteSpline reverseOrientation
//  LocalWords:  DMapper itheta ptheta
