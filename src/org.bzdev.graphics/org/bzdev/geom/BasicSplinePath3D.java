package org.bzdev.geom;
// import java.awt.Shape;
// import java.awt.geom.*;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bzdev.io.AppendableWriter;
import org.bzdev.math.GLQuadrature;
import org.bzdev.math.CubicSpline;
import org.bzdev.math.CubicSpline1;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.RootFinder;
import org.bzdev.math.RootFinder.Brent;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;


//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class representing a continuous path.
 * The path may be opened or closed, but without any gaps. Methods
 * or constructors for splines may be used to create smooth curves.
 * This class extends SplinePath3D by adding methods to determine
 * the X, Y, and Z coordinates of a point given a parameter that starts
 * at 0 and that is incremented by 1 when moving from one segment
 * to another.  The paths that can be represented, however, are
 * restricted.
 * <P>
 * A typical use of this class occurs in the anim2d package for
 * computing the position of an object along a specified path as
 * a function of time.
 * <P>
 * The constructors are the same as those provided by
 * {@link org.bzdev.geom.SplinePath3D SplinePath3D}.
 * This class is a subclass of {@link org.bzdev.geom.Path3D.Double} and
 * a number of the methods this class calls are final methods and thus
 * cannot be overridden. The method {@link #clear()} should be used instead
 * of {@link org.bzdev.geom.Path3D#reset()} and the method
 * {@link #refresh()} must be called if the path is modified after various
 * methods are called. See the documentation for {@link #refresh()} for
 * details.
 * <P>
 * Normally a BasicSplinePath3D will be initialized and then used without
 * any further modifications: this class is intended for cases where an
 * object of some sort follows a path, so the path will usually be created
 * and not modified.
 * @see org.bzdev.geom.SplinePath3D
 * @see #refresh()
 * @see #clear()
*/
public class BasicSplinePath3D extends SplinePath3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructs a new empty BasicSplinePath3D object.
     */
    public BasicSplinePath3D() {
	super();
    }


    /**
     * Constructs a new empty BasicSplinePath3D object with the
     * specified initial capacity to store path segments.
     * @param initialCapacity an estimate for the number of path segments
     *        in the path
     */
    public BasicSplinePath3D(int initialCapacity) {
	super(initialCapacity);
    }

    /**
     * Constructor using another path to determine the initial
     * path segments.
     * @param path the path whose segments will be copied
     */
    public BasicSplinePath3D(Path3D path) {
	this(path.types.length);
	append(path.getPathIterator(null), false);
    }

    /**
     * Constructor using another path to determine the initial
     * path segments, modified with a 3D transform.
     * @param path the path whose segments will be copied
     * @param transform the transform to apply to the path
     *        segments
     */
    public BasicSplinePath3D(Path3D path, Transform3D transform) {
	    this(path.types.length);
	    append(path.getPathIterator(transform), false);
    }

    /**
     * Constructs a new BasicSplinePath3D object from an array
     * containing at least n points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(Point3D[]pk, int n, boolean closed) {
	super(pk, n, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object from an array of
     * points.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param pk the array of points that make up the knots of a spline
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(Point3D[]pk, boolean closed) {
	super(pk, closed);
    }


    /**
     * Constructs a new BasicSplinePath3D object from an array
     * containing at least n points, given an initial
     * capacity.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(int initialCapacity,
			     Point3D[]pk, int n, boolean closed) {
	super(initialCapacity, pk, n, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object from an array of
     * points, given an initial capacity.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param pk the array of points that make up the knots of a spline
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(int initialCapacity,
			Point3D[] pk, boolean closed)
    {
	super(initialCapacity, pk, closed);
    }


    /**
     * Constructs a new BasicSplinePath3D object from arrays
     * containing at least n X, Y, and Z coordinates.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param x the X coordinates to use, one for each knot
     * @param y the Y coordinates to use, one for each knot
     * @param z the Z coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(double[] x, double[] y, double[] z, int n,
			     boolean closed) {
	super(x, y, z, n, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object from arrays of x and
     * y coordinates.
     * The array specifies the "knots" of the spline - the points the
     * spline is constrained to pass through.
     * @param x the X coordinates to use, one for each knot
     * @param y the Y coordinates to use, one for each knot
     * @param z the Z coordinates to use, one for each knot
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public BasicSplinePath3D(double[] x, double[] y, double[] z,
			     boolean closed)
    {
	super(x, y, z, closed);
    }


    /**
     * Constructs a new BasicSplinePath3D object from arrays
     * containing at least n X, Y, and Z coordinates with the specified
     * initial capacity for storing segments.
     * The arrays specify the X, Y, and Z coordinates of the "knots" of
     * the spline - the points the spline is constrained to pass
     * through.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param x the X coordinates to use, one for each knot
     * @param y the Y coordinates to use, one for each knot
     * @param z the Z coordinates to use, one for each knot
     * @param n the number of points in the array to use, with valid indices
     *        in the range [0, n)
     * @param closed true if the spline forms a closed path; false otherwise
     */
    public BasicSplinePath3D(int initialCapacity,
			     double[] x, double[] y, double[] z,
			     int n, boolean closed)
    {
	super(initialCapacity, x, y, z, n, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object from arrays
     * containing at least n X, Y, and Z coordinates with the specified
     * initial capacity for storing segments.
     * The arrays specify the X, Y, and Z coordinates of the "knots" of
     * the spline - the points the spline is constrained to pass
     * through.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param x the X coordinates to use, one for each knot
     * @param y the Y coordinates to use, one for each knot
     * @param z the Z coordinates to use, one for each knot
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal, typically
     *            because the lengths of the arrays differ
     */
    public BasicSplinePath3D(int initialCapacity,
			     double[] x, double[] y, double[] z,
			     boolean closed) {
	super(initialCapacity, x, y, z, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object given cubic splines specifying
     * the x, y and z coordinates.
     * The functions must be instances of {@link org.bzdev.math.CubicSpline}
     * and must be created with the same number of "knots". The argument
     * to each function will be rescaled so that it will be incremented by
     * 1.0 when moving from one knot to the next.
     * If closed is true and the initial knot is not at the same location
     * as the final knot, a straight line segment will be used to close
     * the path.
     * <P>
     * This constructor is provided to support cases where the default
     * algorithm for creating a spline is not appropriate: the class
     * {@link org.bzdev.math.CubicSpline} allows one to specify a number
     * of types of splines, with most differing in how the ends are treated,
     * but with one case (A Hermite spline) explicitly giving the derivative
     * at each knot.
     * @param xf the spline specifying the x coordinates to use
     * @param yf the spline specifying the y coordinates to use
     * @param zf the spline specifying the y coordinates to use
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException The splines xf, yf and zf
     *            did not have the same number of knots
     */
    public BasicSplinePath3D(CubicSpline xf, CubicSpline yf, CubicSpline zf,
			     boolean closed)
	throws IllegalArgumentException
    {
	super(xf, yf, zf, closed);
    }

    /**
     * Constructs a new BasicSplinePath3D object given cubic splines specifying
     * the x, y and z coordinates and an initial capacity.
     * The splines must be created with the same number of "knots". The argument
     * to each function will be rescaled so that it will be incremented by
     * 1.0 when moving from one knot to the next.  The path will be
     * constructed using the control points associated with the cubic splines
     * providing the x, y, and z coordinates.
     * If closed is true and the initial knot is not at the same location
     * as the final knot, a straight line segment will be used to close
     * the path.
     * <P>
     * This constructor is provided to support cases where the default
     * algorithm for creating a spline is not appropriate: the class
     * {@link org.bzdev.math.CubicSpline} allows one to specify a number
     * of types of splines, with most differing in how the ends are treated,
     * but with one case (A Hermite spline) explicitly giving the derivative
     * at each knot.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param xf the spline specifying the x coordinates to use
     * @param yf the spline specifying the y coordinates to use
     * @param zf the spline specifying the y coordinates to use
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException The spline xf and yf did
     *            not have the same number of knots
     * @exception IllegalArgumentException The splines xf, yf and zf
     *            did not have the same number of knots
     */
    public BasicSplinePath3D(int initialCapacity,
			     CubicSpline xf, CubicSpline yf, CubicSpline zf,
			     boolean closed)
	throws IllegalArgumentException
    {
	super(initialCapacity, xf, yf, zf, closed);
    }

    /**
     * Constructs a new SplinePath3D object given functions specifying
     * the X, Y, and Z coordinates.
     * @param xf the function specifying the X coordinates to use
     * @param yf the function specifying the Y coordinates to use
     * @param zf the function specifying the Z coordinates to use
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     */
    public BasicSplinePath3D(RealValuedFunctOps xf, RealValuedFunctOps yf,
			     RealValuedFunctOps zf,
			     double t1, double t2, int n, boolean closed)
	throws IllegalArgumentException
    {
	super(xf, yf, zf, t1, t2, n, closed);
    }

    /**
     * Constructs a new SplinePath3D object from functions giving
     * X, Y, and Z coordinates, and the initial capacity for storing segments.
     * @param initialCapacity an estimate for the number of path segments in
     *        the path
     * @param xf the function specifying the X coordinates to use
     * @param yf the function specifying the Y coordinates to use
     * @param zf the function specifying the Z coordinates to use
     * @param t1 an end point for the domain of the argument of xf and yf
     * @param t2 the other end point for the domain of the argument of xf
     *        and yf
     * @param n the number of segments between the points at which to
     *        evaluate the functions
     * @param closed true if the spline forms a closed path; false otherwise
     * @exception IllegalArgumentException the arguments were illegal,
     *            typically because n was not positive or either t1 or
     *            t2 were not in the domain of xf or yf.
     */
    public BasicSplinePath3D(int initialCapacity,
			     RealValuedFunctOps xf, RealValuedFunctOps yf,
			     RealValuedFunctOps zf,
			     double t1, double t2, int n, boolean closed)
	throws IllegalArgumentException
    {
	super(initialCapacity, xf, yf, zf, t1, t2, n, closed);
    }


    private static final double ROUNDOFF_ERROR = 1.e-10;

    boolean cyclic = false;

    /**
     * Determine if the path is a closed path.
     * @return true if it is closed; false if it is not closed
     */
    public boolean isClosed() {
	return cyclic;
    }

    // private int lengthCount = 0;
    private double totalLength = 0.0;

    /*
    static class Entry {
	int mode;
	double x;
	double y;
	double z;
	double[] coords = new double[9];
	double length = -1;
    }
    */

    private Path3DInfo.Entry[] entries = null;
    private double[] cumulativeLength = null;
    // DELTA s as a function of t
    private CubicSpline[] sublengths = null;

    static final int DEFAULT_NUMBEROFINTERVALS = 64;
    int numberOfIntervals = DEFAULT_NUMBEROFINTERVALS;

    static final int N4SEGLEN = 32;

    static double[][] U4SEGLEN = new double[DEFAULT_NUMBEROFINTERVALS][];
    static Path3DInfo.UValues[][] UV4SEGLEN =
	new Path3DInfo.UValues[DEFAULT_NUMBEROFINTERVALS][N4SEGLEN];
    static {
	for (int i = 1; i < DEFAULT_NUMBEROFINTERVALS; i++) {
	    double t1 = ((double)(i-1))/DEFAULT_NUMBEROFINTERVALS;
	    double t2 = ((double)(i))/DEFAULT_NUMBEROFINTERVALS;
	    U4SEGLEN[i] = GLQuadrature.getArguments(t1, t2, N4SEGLEN);
	    for (int j = 0; j < N4SEGLEN; j++) {
		UV4SEGLEN[i][j] = new Path3DInfo.UValues(U4SEGLEN[i][j]);
		U4SEGLEN[i][j] = (double) j;
	    }
	}
    }
    double[][] u4seglen = U4SEGLEN;
    Path3DInfo.UValues[][] uv4seglen = UV4SEGLEN;
    int segindex;

    GLQuadrature<Path3DInfo.SegmentData> glq4seglen =
	new GLQuadrature<Path3DInfo.SegmentData>(N4SEGLEN) {
	protected double function(double iu, Path3DInfo.SegmentData data) {
	    int j = (int)Math.round(iu);
	    return data.dsDu(uv4seglen[segindex][j]);
	}
    };

    /**
     * Set the number of intervals used for cubic splines.
     * For each path segment, a cubic spline is used to determine
     * the distance from the start of the path to a point along the
     * path represented by a path parameter.  This method sets the
     * number of points used to construct this spline.  The default
     * value (16) should be adequate for most purposes.  A smaller
     * value reduces memory usage but decreases accuracy.
     * @param value the number of intervals (must be larger than 4)
     *        or 0 for the default
     */
    public void setIntervalNumber(int value) {
	if (value == 0) value = DEFAULT_NUMBEROFINTERVALS;
	if (value <= 4) throw new IllegalArgumentException
			    (errorMsg("integerMin5", value));
	synchronized (glq4seglen) {
	    numberOfIntervals = value;
	    if (numberOfIntervals == DEFAULT_NUMBEROFINTERVALS) {
		u4seglen = U4SEGLEN;
		uv4seglen = UV4SEGLEN;
	    } else {
		u4seglen = new double[numberOfIntervals][];
		uv4seglen = new Path3DInfo.UValues[numberOfIntervals][N4SEGLEN];
		for (int i = 1; i < numberOfIntervals; i++) {
		    double t1 = ((double)(i-1))/numberOfIntervals;
		    double t2 = ((double)(i))/numberOfIntervals;
		    u4seglen[i] = GLQuadrature.getArguments(t1, t2, N4SEGLEN);
		    for (int j = 0; j < N4SEGLEN; j++) {
			uv4seglen[i][j] =
			    new Path3DInfo.UValues(u4seglen[i][j]);
			u4seglen[i][j] = (double) j;
		    }
		}
	    }
	}
    }

    /**
     * Get the number of intervals used for cubic splines.
     * For each path segment, a cubic spline is used to determine
     * the distance from the start of the path to a point along the
     * path represented by a path parameter.  This method returns the
     * number of points used to construct this spline.
     * @return the number of intervals
     */
    public int getIntervalNumber() {
	return numberOfIntervals;
    }


    /**
     * Print information about the segments that make up the path to
     * the standard output.
     * Entry i contains the X, Y, and Z coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     */
    public void printTable() {
	printTable(System.out);
    }

    /**
     * Print information about the segments that make up the path.
     * Entry i contains the X, Y, and Z coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param appendable an Appendable on which to print
     */
    public void printTable(Appendable appendable) {
	printTable(null, appendable);
    }

    /**
     * Print information about the segments that make up the path
     * adding a prefix.
     * Entry i contains the X, Y, and Z coordinate when the parameter is
     * equal to i, followed by control points, the last of which is
     * the coordinate when the parameter is i+1.
     * @param prefix a prefix to print at the start of each line
     *        (null implies an empty string)
     * @param appendable the appendable for output
     */
    public void printTable(String prefix, Appendable appendable) {
	if (prefix == null) prefix = "";
	Writer w;
	if (appendable instanceof Writer) {
	    w = (Writer) appendable;
	} else {
	 w = new AppendableWriter(appendable);
	}
	PrintWriter out;
	if (w instanceof PrintWriter) {
	    out = (PrintWriter) w;
	} else {
	    out = new PrintWriter(w);
	}
	if (entries == null) refresh();
	for (int i = 0; i < entries.length; i++) {
	    out.println(prefix + "Entry " + i + ":");
	    int m = 0;
	    switch (entries[i].type) {
	    case PathIterator3D.SEG_CUBICTO:
		out.println(prefix + "    mode: SEG_CUBICTO");
		m = 9;
		break;
	    case PathIterator3D.SEG_LINETO:
		out.println(prefix + "    mode: SEG_LINETO");
		m = 3;
		break;
	    case PathIterator3D.SEG_QUADTO:
		out.println(prefix + "    mode: SEG_QUADTO");
		m = 6;
		break;
	    default:
		System.out.println(prefix +"   [unknown mode]");
	    }
	    out.println(prefix + "    x: " + entries[i].x);
	    out.println(prefix + "    y: " + entries[i].y);
	    out.println(prefix + "    z: " + entries[i].z);
	    for (int j = 0; j < m; j++) {
		out.println(prefix + "    coords[" + j + "]: "
			    + entries[i].coords[j]);
	    }
	}
	out.flush();
    }

    /**
     * Get the maximum value of the parameter.
     * This value is applicable for open paths, not closed paths.
     * The returned value is numerically equal to the number of
     * segments that make up the path.
     * @return the maximum value of the parameter
     */
    public double getMaxParameter() {
	if (entries == null) refresh();
	return entries.length;
    }

    /**
     * Class representing a location along a path that is an instance
     * of BasicSplinePath3D.
     * A location is associated with a position along a path - a
     * particular value of the path parameter - and stores data
     * that allows various quantities to be computed with higher
     * efficiency than would otherwise be possible.  It is useful
     * for the case where multiple quantities will be computed
     * for the same location.
     */
    public class Location {
	int index;
	Path3DInfo.Entry entry;
	Path3DInfo.UValues uv;
	Location(int index, Path3DInfo.Entry entry, double u) {
	    this.index = index;
	    this.entry = entry;
	    this.uv = new Path3DInfo.UValues(u);
	}

	/**
	 * Get the path parameter corresponding to this location along
	 * the path.
	 * @return the path parameter
	 */
	public double getPathParameter() {
	    return uv.u;
	}

	/**
	 * Get the point along the path corresponding to this location.
	 * @return the point along the path
	 * @exception IllegalStateException this location is no longer valid
	 */
	public Point3D getPoint() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    Path3DInfo.SegmentData segdata = entry.getData();
	    return new Point3D.Double(segdata.getX(uv), segdata.getY(uv),
				      segdata.getZ(uv));
	}

	/**
	 * Get the X coordinate of the point along the path corresponding to
	 * this location.
	 * @return the X coordinate
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double getX() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getX(uv);
	}

	/**
	 * Get the Y coordinate of the  point along the path corresponding
	 * to this location.
	 * @return the Y coordinate
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double getY() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getY(uv);
	}

	/**
	 * Get the Y coordinate of the  point along the path corresponding
	 * to this location.
	 * @return the Y coordinate
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double getZ() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getZ(uv);
	}


	/**
	 * Compute the derivative with respect to the path parameter of the
	 * X coordinate for the point along the path corresponding to this
	 * location.
	 * @return the derivative of the X coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double dxDu() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().dxDu(uv);
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 * Y coordinate for the point along the path corresponding to this
	 * location.
	 * @return the derivative of the Y coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double dyDu() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().dyDu(uv);
	}

	/**
	 * Compute the derivative with respect to the path parameter of the
	 * Z coordinate for the point along the path corresponding to this
	 * location.
	 * @return the derivative of the Y coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double dzDu() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().dzDu(uv);
	}


	/**
	 * Compute the second derivative with respect to the path
	 * parameter of the X coordinate, evaluated at the point along
	 * the path corresponding to this location.
	 * @return the second derivative of the X coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double d2xDu2() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().d2xDu2(uv);
	}

	/**
	 * Compute the second derivative with respect to the path
	 * parameter of the Y coordinate, evaluated at the point along
	 * the path corresponding to this location.
	 * @return the second derivative of the Y coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double d2yDu2() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().d2yDu2(uv);
	}

	/**
	 * Compute the second derivative with respect to the path
	 * parameter of the Z coordinate, evaluated at the point along
	 * the path corresponding to this location.
	 * @return the second derivative of the Y coordinate with respect to the
	 *         path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double d2zDu2() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().d2zDu2(uv);
	}

	/**
	 * Compute the derivative with respect to the path parameter
	 * of the signed distance along the path from its initial point.
	 * The derivative is evaluated at the point along the path
	 * corresponding to this location.
	 * @return the derivative of the signed distance along the path
	 *         with respect to the path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double dsDu() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().dsDu(uv);
	}

	/**
	 * Compute the second derivative with respect to the path parameter
	 * of the signed distance along the path from its initial point.
	 * The derivative is evaluated at the point along the path
	 * corresponding to this location.
	 * @return the second derivative of the signed distance along the path
	 *         with respect to the path parameter
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double d2sDu2() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().d2sDu2(uv);
	}

	/**
	 * Compute the curvature at the point along the path
	 * corresponding to this location.
	 * If the current path segment's type is
	 * {@link PathIterator3D#SEG_CLOSE}, the starting and ending points
	 * of the segment are identical, and the path parameter corresponding
	 * to the argument is zero, the curvature at the end of the previous
	 * segment is returned.
	 * @return the curvature (Double.NaN if not defined)
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double curvature() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().curvature(uv);
	}

	/**
	 * Compute the torsion at the point along the path
	 * corresponding to this location.
	 * If the current path segment's type is
	 * {@link PathIterator3D#SEG_CLOSE}, the starting and ending points
	 * of the segment are identical, and the path parameter corresponding
	 * to the argument is zero, the curvature at the end of the previous
	 * segment is returned
	 * <P>
	 * The Wikipedia page
	 * <A HREF="https://en.wikipedia.org/wiki/Frenet%E2%80%93Serret_formulas">
	 * The Frenet-Serret Formulas</A> contains a definition.
	 * @return the torsion (Double.NaN if not defined)
	 * @exception IllegalStateException this location is no longer valid
	 */
	public double torsion() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().torsion(uv);
	}

	/**
	 * Determine if the curvature exists at the  point along the
	 * path corresponding to this location.
	 * In general, the curvature does not exist when all the points
	 * along a segment are the same point or when the segment is
	 * a SEG_MOVETO segment (which just indicates the start of a curve).
	 * For a SEG_CLOSE segment where the starting and ending points
	 * are identical, the curvature exists if the curvature of the
	 * previous segment exists at its end (u = 1.0).
	 * @return true if the curvature exists; false otherwise
	 * @exception IllegalStateException this location is no longer valid
	 */
	public boolean curvatureExists() {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().curvatureExists(uv);
	}

	/**
	 * Get the tangent vector for the point along the path
	 * corresponding to this location.
	 * If the tangent vector does not exist (e.g., the length of
	 * the line does not vary with the path parameter), the
	 * tangent vector will be set to zero.  The tangent vector
	 * will have unit length if it is not zero.
	 * @param array an array of length no less than 3 used to
	 *        store the tangent vector, with array[offset]
	 *        containing the tangent vector's X component and
	 *        array[offset+1] containing the tangent vector's Y
	 *        component
	 * @param offset the index into the array at which to store
	 *        the tangent vector
	 * @return true if the tangent vector exists; false if the tangent
	 *         vector does not exist
	 * @exception IllegalStateException this location is no longer valid
	 */
	public boolean getTangent(double[] array, int offset) {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getTangent(uv, array, offset);
	}

	/**
	 * Get the normal vector for the point along the path
	 *  corresponding to this location.
	 * The normal vector N is a vector of unit length,
	 * perpendicular to the tangent vector, and oriented so that
	 * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is
	 * the (signed) curvature.  If the normal vector does not
	 * exist (e.g., the length of the line does not vary with the
	 * path parameter), the normal vector will be set to zero.
	 * @param array an array of length no less than 2 used to
	 *        store the normal vector, with array[offset]
	 *        containing the normal vector's X component and
	 *        array[offset+1] containing the normal vector's Y
	 *        component
	 * @param offset the index into the array at which to store
	 *        the normal vector
	 * @return true if the normal vector exists; false if the normal
	 *         vector does not exist
	 * @exception IllegalStateException this location is no longer valid
	 */
	public boolean getNormal(double[] array, int offset) {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getNormal(uv, array, offset);
	}
	/**
	 * Get the binormal vector for the point along the path
	 * corresponding to this location.
	 * The binormal vector B is equals to the cross product T X N, where
	 * T is the tangent vector and N is the normal vector;
	 * The normal vector N is a vector of unit length,
	 * perpendicular to the tangent vector, and oriented so that
	 * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is
	 * the curvature.  If the normal vector does not
	 * exist (e.g., the length of the line does not vary with the
	 * path parameter or the path is locally straight), the binormal
	 * vector will be set to zero.
	 * @param array an array of length no less than 2 used to
	 *        store the normal vector, with array[offset]
	 *        containing the normal vector's X component and
	 *        array[offset+1] containing the normal vector's Y
	 *        component
	 * @param offset the index into the array at which to store
	 *        the normal vector
	 * @return true if the normal vector exists; false if the normal
	 *         vector does not exist
	 * @exception IllegalStateException this location is no longer valid
	 */
	public boolean getBinormal(double[] array, int offset) {
	    if (entries == null || index >= entries.length
		|| entry != entries[index]) {
		throw new IllegalStateException(errorMsg("badLocation"));
	    }
	    return entry.getData().getBinormal(uv, array, offset);
	}
    }

    /**
     * Get a location for a path parameter;
     * @param u the path parameter
     * @return the location
     */
    public Location getLocation(double u) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	return new Location(index, entries[index], t);
    }

    /**
     * Get the X coordinate for a given value of the parameter.
     * @param u the parameter
     * @return the X coordinate of the point on the path for the
     *         specified parameter
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getX(double u) throws IllegalStateException, 
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return omt2*omt*entry.x
		+ 3.0*omt2*t*entry.coords[0]
		+ 3.0*omt*t2*entry.coords[3]
		+ t2*t*entry.coords[6];
	case PathIterator3D.SEG_LINETO:
	    return entry.x * omt + t * entry.coords[0];
	case PathIterator3D.SEG_QUADTO:
	    return omt*omt*entry.x
		+ 2*omt*t*entry.coords[0] + t*t*entry.coords[3];
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
    }

    /**
     * Get the Y coordinate for a given value of the parameter.
     * @param u the parameter
     * @return the Y coordinate of the point on the path for the
     *         specified parameter
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getY(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalStateException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalStateException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return omt2*omt*entry.y
		+ 3.0*omt2*t*entry.coords[1]
		+ 3.0*omt*t2*entry.coords[4]
		+ t2*t*entry.coords[7];
	case PathIterator3D.SEG_LINETO:
	    return entry.y * omt + t * entry.coords[1];
	case PathIterator3D.SEG_QUADTO:
	    return omt*omt*entry.y
		+ 2*omt*t*entry.coords[1] + t*t*entry.coords[4];
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
    }

    /**
     * Get the Z coordinate for a given value of the parameter.
     * @param u the parameter
     * @return the Z coordinate of the point on the path for the
     *         specified parameter
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getZ(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalStateException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalStateException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return omt2*omt*entry.z
		+ 3.0*omt2*t*entry.coords[2]
		+ 3.0*omt*t2*entry.coords[5]
		+ t2*t*entry.coords[8];
	case PathIterator3D.SEG_LINETO:
	    return entry.z * omt + t * entry.coords[2];
	case PathIterator3D.SEG_QUADTO:
	    return omt*omt*entry.z
		+ 2*omt*t*entry.coords[2] + t*t*entry.coords[5];
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
    }

    /**
     * Get the point on a path corresponding to a given value of the parameter.
     * @param u the parameter
     * @return the point on the path corresponding to the specified parameter
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public Point3D getPoint(double u) throws IllegalStateException,
					     IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	double x;
	double y;
	double z;
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    x =  omt2*omt*entry.x
		+ 3.0*omt2*t*entry.coords[0]
		+ 3.0*omt*t2*entry.coords[3]
		+ t2*t*entry.coords[6];
	    y = omt2*omt*entry.y
		+ 3.0*omt2*t*entry.coords[1]
		+ 3.0*omt*t2*entry.coords[4]
		+ t2*t*entry.coords[7];
	    z = omt2*omt*entry.z
		+ 3.0*omt2*t*entry.coords[2]
		+ 3.0*omt*t2*entry.coords[5]
		+ t2*t*entry.coords[8];
	    break;
	case PathIterator3D.SEG_LINETO:
	    x = entry.x * omt + t * entry.coords[0];
	    y = entry.y * omt + t * entry.coords[1];
	    z = entry.z * omt + t * entry.coords[2];
	    break;
	case PathIterator3D.SEG_QUADTO:
	    x =  omt*omt*entry.x
		+ 2*omt*t*entry.coords[0] + t*t*entry.coords[3];
	    y = omt*omt*entry.y
		+ 2*omt*t*entry.coords[1] + t*t*entry.coords[4];
	    z = omt*omt*entry.z
		+ 2*omt*t*entry.coords[2] + t*t*entry.coords[5];
	    break;
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	return new Point3D.Double(x, y, z);
    }


    /**
     * Get the derivative of the X coordinate with respect to the parameter
     * for a given value of the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double dxDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	// double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	return entry.data.dxDu(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return 3.0 *omt2*(entry.coords[0] - entry.x)
		+ 6.0*omt2*t*(entry.coords[3] - entry.coords[0])
		+ 3.0*t2*(entry.coords[6] - entry.coords[3]);
	case PathIterator3D.SEG_LINETO:
	    return entry.coords[0] - entry.x;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 *omt*(entry.coords[0] - entry.x)
		+ 2.0*t*(entry.coords[3] - entry.coords[0]);
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }

    /**
     * Get the derivative of the Y coordinate with respect to the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double dyDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	// double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	return entry.data.dyDu(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return 3.0 *omt2*(entry.coords[1] - entry.y)
		+ 6.0*omt2*t*(entry.coords[4] - entry.coords[1])
		+ 3.0*t2*(entry.coords[7] - entry.coords[4]);
	case PathIterator3D.SEG_LINETO:
	    return entry.coords[1] - entry.y;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 *omt*(entry.coords[1] - entry.y)
		+ 2.0*t*(entry.coords[4] - entry.coords[1]);
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }

    /**
     * Get the derivative of the Z coordinate with respect to the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double dzDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	// double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	return entry.data.dzDu(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double t2 = t*t;
	    return 3.0 *omt2*(entry.coords[2] - entry.z)
		+ 6.0*omt2*t*(entry.coords[5] - entry.coords[2])
		+ 3.0*t2*(entry.coords[8] - entry.coords[5]);
	case PathIterator3D.SEG_LINETO:
	    return entry.coords[1] - entry.z;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 *omt*(entry.coords[2] - entry.z)
		+ 2.0*t*(entry.coords[5] - entry.coords[2]);
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }

    /**
     * Get the second derivative of the X coordinate with respect to 
     * the parameter for a given value of the parameter.
     * @param u the parameter
     * @return the second derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double d2xDu2(double u) throws IllegalStateException,
					  IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	Path3DInfo.Entry entry = entries[index];
	return entry.data.d2xDu2(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (entry.x - entry.coords[0] + entry.coords[3]
			  - entry.coords[0]);
	case PathIterator3D.SEG_CUBICTO:
	    double t1 = 1.0 - t;
	    return 6.0 * (t1*(entry.x -2.0*entry.coords[0] + entry.coords[3])
			  + t*(entry.coords[6] - 2.0*entry.coords[3]
			       + entry.coords[0]));
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }

    /**
     * Get the second derivative of the Y coordinate with respect to 
     * the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double d2yDu2(double u) throws IllegalStateException,
					  IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	// double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	return entry.data.d2yDu2(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (entry.y - entry.coords[1] + entry.coords[4]
			  - entry.coords[1]);
	case PathIterator3D.SEG_CUBICTO:
	    double t1 = 1.0 - t;
	    return 6.0 * (t1*(entry.y -2.0*entry.coords[1] + entry.coords[4])
			  + t*(entry.coords[7] - 2.0*entry.coords[4]
			       + entry.coords[1]));
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }

    /**
     * Get the second derivative of the Z coordinate with respect to 
     * the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double d2zDu2(double u) throws IllegalStateException,
					  IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	// double omt = 1.0 - t;
	Path3DInfo.Entry entry = entries[index];
	return entry.data.d2zDu2(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	case PathIterator3D.SEG_QUADTO:
	    return 2.0 * (entry.z - entry.coords[2] + entry.coords[5]
			  - entry.coords[2]);
	case PathIterator3D.SEG_CUBICTO:
	    double t1 = 1.0 - t;
	    return 6.0 * (t1*(entry.z -2.0*entry.coords[2] + entry.coords[5])
			  + t*(entry.coords[8] - 2.0*entry.coords[5]
			       + entry.coords[2]));
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	*/
    }


    /**
     * Compute the curvature given the path parameter.
     * The curvature is given by the expression
     * <blockquote><pre>
     * sqrt((z"y'-y"z')<sup>2</sup> + (x"z'-z"x')<sup>2</sup> + (y"x'-x"y')<sup>2</sup>) / (x'<sup>2</sup> + y'<sup>2</sup> + z'<sup>2</sup>)<sup>3/2</sup>
     * </pre></blockquote>
     * where the derivative are computed with respect to the path
     * parameter.  The radius of curvature is the multiplicative
     * inverse of the absolute value of the curvature.
     * @param u the path parameter
     * @return the curvature
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double curvature(double u)
	throws IllegalStateException, IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	Path3DInfo.Entry entry = entries[index];
	return entry.data.curvature(new Path3DInfo.UValues(t));
	/*
	double xp = Path3DInfo.dxDu(t, entry.x, entry.y, entry.z, entry.type,
				    entry.coords);
	double yp = Path3DInfo.dyDu(t, entry.x, entry.y, entry.z, entry.type,
				    entry.coords);
	double zp = Path3DInfo.dzDu(t, entry.x, entry.y, entry.z, entry.type,
				    entry.coords);
	double xpp = Path3DInfo.d2xDu2(t, entry.x, entry.y, entry.z, entry.type,
				       entry.coords);
	double ypp = Path3DInfo.d2yDu2(t, entry.x, entry.y, entry.z, entry.type,
				       entry.coords);
	double zpp = Path3DInfo.d2zDu2(t, entry.x, entry.y, entry.z, entry.type,
				       entry.coords);
	double tmp = xp*xp + yp*yp + zp*zp;
	tmp = tmp * Math.sqrt(tmp);

	double tmp1 = zpp*yp - ypp*zp;
	tmp1 *= tmp1;
	double tmp2 = xpp*zp - zpp*xp;
	tmp2 *= tmp2;
	double tmp3 = ypp*xp - xpp*yp;
	tmp3 *= tmp3;
	
	return Math.sqrt(tmp1 + tmp2 + tmp3)/tmp;
	*/
    }

    /**
     * Compute the torsion given the path parameter.
     * The torsion is given by the expression
     * <blockquote><pre>

     * (x'"(z"y'-y"z') + y"'(x"z'-z"x') + z"'(y"x'-x"y')) / ((y'z"-y"z')<sup>2</sup> + (x"z'-x'z")<sup>2</sup> + (x'y"-x"y')<sup>2</sup> )
     * </pre></blockquote>
     * where the derivative are computed with respect to the path
     * parameter.
     * The Wikipedia page
     * <A HREF="https://en.wikipedia.org/wiki/Frenet%E2%80%93Serret_formulas">
     * The Frenet-Serret Formulas</A> contains the equation above in vector
     * form.
     * @param u the path parameter
     * @return the curvature
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double torsion(double u)
	throws IllegalStateException, IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	Path3DInfo.Entry entry = entries[index];
	return entry.data.torsion(new Path3DInfo.UValues(t));
    }

    private final Path3DInfo.UValues uvOne = new Path3DInfo.UValues(1.0);

    /**
     * Determine if the curvature exists at the  point along the
     * path corresponding to a value of the path parameter.
     * In general, the curvature does not exist when all the points
     * along a segment are the same point or when the segment is
     * a SEG_MOVETO segment (which just indicates the start of a curve).
     * For a SEG_CLOSE segment where the starting and ending points
     * are identical, the curvature exists if the curvature of the
     * previous segment exists at its end (u = 1.0).
     * @param u  the path parameter
     * @return true if the curvature exists; false otherwise
     * @exception IllegalStateException this location is no longer valid
     */
    public boolean curvatureExists(double u) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	Path3DInfo.Entry entry = entries[index];
	int type = entry.type;
	double x0 = entry.x;
	double y0 = entry.y;
	double z0 = entry.z;
	double[] coords = entry.coords;
	switch (type) {
	case PathIterator3D.SEG_MOVETO:
	    return false;
	case PathIterator3D.SEG_CLOSE:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]) {
		if (t > 0.0) return false;
		Path3DInfo.SegmentData data = entry.getData();
		if (data.last == null) return false;
		return data.last.curvatureExists(uvOne);
	    }
	    break;
	case PathIterator3D.SEG_LINETO:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]) {
		return false;
	    }
	    break;
	case PathIterator3D.SEG_QUADTO:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]
		&&  x0 == coords[3] && y0 == coords[4] && z0 == coords[5]) {
		return false;
	    }
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    if (x0 == coords[0] && y0 == coords[1] && z0 == coords[2]
		&& x0 == coords[3] && y0 == coords[4] && z0 == coords[5]
		&& x0 == coords[6] && y0 == coords[7] && z0 == coords[8]) {
		return false;
	    }
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	return true;
    }

    /**
     * Get the tangent vector for a specified value of the path parameter.
     * If the tangent vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the tangent vector
     * will be set to zero.  The tangent vector will have unit length
     * if it is not zero.
     * @param u the path parameter
     * @param array an array of length no less than 3 used to store
     *        the tangent vector, with array[0] containing the tangent
     *        vector's X component, array[1] containing the tangent
     *        vector's Y component, and array[2] containing the tangent
     *        vector's Z component
     * @return true if the tangent vector exists; false if the tangent
     *         vector does not exist
     */
    public boolean getTangent(double u, double[]array) {
	return getTangent(u, array, 0);
    }

    /**
     * Get the tangent vector for a specified value of the path parameter
     * and an output-array offset.
     * If the tangent vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the tangent vector
     * will be set to zero.   The tangent vector will have unit length
     * if it is not zero.
     * @param u the path parameter
     * @param array an array of length no less than 3 used to store
     *        the tangent vector, with array[offset] containing the
     *        tangent vector's X component, array[offset+1] containing
     *        the tangent vector's Y component, and array[offset+2]
     *        containing the tangent vector's Z component
     * @param offset the index into the array at which to store the tangent
     *        vector
     * @return true if the tangent vector exists; false if the tangent
     *         vector does not exist
     */
    public boolean getTangent(double u, double[]array, int offset) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}

	Path3DInfo.Entry entry = entries[index];
	return entry.data.getTangent(new Path3DInfo.UValues(t), array, offset);
	/*
	double xp = Path2DInfo.dxDu(t, entry.x, entry.y, entry.type,
				    entry.coords);
	double yp = Path2DInfo.dyDu(t, entry.x, entry.y, entry.type,
				    entry.coords);

	double tmp = Math.sqrt(xp*xp + yp*yp);
	if (tmp == 0.0) {
	    array[offset] = 0.0;
	    array[offset+1] = 0.0;
	    return false;
	}
	array[offset] = xp/tmp;
	array[offset+1] = yp/tmp;
	return true;
	*/
    }

    /**
     * Get the normal vector for a given value of the path parameter.
     * The normal vector N is a vector of unit length, perpendicular to
     * the tangent vector, and oriented so that
     * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is the
     * curvature.
     * If the normal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the normal vector
     * will be set to zero.
     * @param u the path parameter
     * @param array an array of length no less than 2 used to store the
     *        normal vector, with array[0] containing the normal vector's
     *        X component, array[1] containing the normal vector's Y
     *        component, and array[2] containing the normal vector's Z
     *        component
     * @return true if the normal vector exists; false if the normal
     *         vector does not exist
     */
    public boolean getNormal(double u, double[]array) {
	return getNormal(u, array, 0);
    }

    /**
     * Get the binormal vector for a given value of the path parameter.
     * The binormal vector is defined as the cross product T X N of
     * the tangent and normal vectors.  If the binormal vector does
     * not exist (e.g., the length of the line does not vary with the
     * path parameter or the path is locally straight), the binormal
     * vector will be set to zero.
     * @param u the path parameter
     * @param array an array of length no less than 2 used to store the
     *        normal vector, with array[0] containing the normal vector's
     *        X component, array[1] containing the normal vector's Y
     *        component, and array[2] containing the normal vector's Z
     *        component
     * @return true if the binormal vector exists; false if the normal
     *         vector does not exist
     */
    public boolean getBinormal(double u, double[]array) {
	return getBinormal(u, array, 0);
    }

    /**
     * Get the normal vector for a given value of the path parameter and
     * an offset for the array storing the normal vector.
     * The normal vector N is a vector of unit length, perpendicular to
     * the tangent vector, and oriented so that
     * d<sup>2</sup>r/ds<sup>2</sup> = &kappa;N where &kappa; is the
     * curvature.
     * If the normal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter), the normal vector
     * will be set to zero.
     * @param u the path parameter
     * @param array an array of length no less than 3 used to store
     *        the normal vector, with array[offset] containing the
     *        normal vector's X component and array[offset+1]
     *        containing the normal vector's Y component, and
     *        array[offset+2] containing the normal vector's Z component
     * @param offset the array offset
     * @return true if the normal vector exists; false if the normal
     *         vector does not exist
     */
    public boolean getNormal(double u, double[] array, int offset) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}

	Path3DInfo.Entry entry = entries[index];
	return entry.data.getNormal(new Path3DInfo.UValues(t), array, offset);
    }

    /**
     * Get the binormal vector for a given value of the path parameter and
     * an offset for the array storing the normal vector.
     * The binormal vector is defined as the cross product
     * T X N of the tangent and normal vectors.
     * If the binormal vector does not exist (e.g., the length of the
     * line does not vary with the path parameter or the path is locally
     * straight), the binormal vector will be set to zero.
     * @param u the path parameter
     * @param array an array of length no less than 3 used to store
     *        the binormal vector, with array[offset] containing the
     *        normal vector's X component and array[offset+1]
     *        containing the normal vector's Y component, and
     *        array[offset+2] containing the bnormal vector's Z component
     * @param offset the array offset
     * @return true if the binormal vector exists; false if the normal
     *         vector does not exist
     */
    public boolean getBinormal(double u, double[] array, int offset) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}

	Path3DInfo.Entry entry = entries[index];
	return entry.data.getBinormal(new Path3DInfo.UValues(t), array, offset);
    }

    /**
     * Get the derivative of the path length with respect to the parameter
     * for a given value of the parameter.
     * @param u the parameter
     * @return the derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double dsDu(double u) throws IllegalStateException,
					IllegalArgumentException
    {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	/*
	double dxdu;
	double dydu;
	double dzdu;
	double omt = 1.0 - t;
	*/
	Path3DInfo.Entry entry = entries[index];
	return entry.data.dsDu(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_LINETO:
	    dxdu = entry.coords[0] - entry.x;
	    dydu = entry.coords[1] - entry.y;
	    dzdu = entry.coords[2] - entry.z;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    dxdu = 2.0 * (omt*(entry.coords[0]-entry.x)
			  + u*(entry.coords[3]-entry.coords[0]));
	    dydu = 2.0 * (omt*(entry.coords[1]-entry.y)
			  + u*(entry.coords[4]-entry.coords[1]));
	    dzdu = 2.0 * (omt*(entry.coords[2]-entry.z)
			  + u*(entry.coords[5]-entry.coords[2]));
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    double omt2 = omt*omt;
	    double tomt2 = 2.0 * t * omt;
	    double t2 = t*t;
	    dxdu = 3.0 * (omt2*(entry.coords[0] - entry.x)
			  + tomt2*(entry.coords[3] - entry.coords[0])
			  + t2*(entry.coords[6] - entry.coords[3]));
	    dydu = 3.0 * (omt2*(entry.coords[1] - entry.y)
			  + tomt2*(entry.coords[4] - entry.coords[1])
			  + t2*(entry.coords[7] - entry.coords[4]));
	    dzdu = 3.0 * (omt2*(entry.coords[2] - entry.z)
			  + tomt2*(entry.coords[5] - entry.coords[2])
			  + t2*(entry.coords[8] - entry.coords[5]));
	    break;
	default:
	    throw new Error(errorMsg("badSwitch"));
	}
	double dsduSquared = dxdu*dxdu + dydu*dydu + dzdu*dzdu;
	return  Math.sqrt(dsduSquared);
	*/
    }

    /**
     * Get the second derivative of the path length with respect to
     * the parameter for a given value of the parameter.
     * @param u the parameter
     * @return the second derivative
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double d2sDu2(double u) {
	if (entries == null) refresh();
	double xindex = Math.floor(u);
	int index = (int)Math.round(xindex);
	double t = u - xindex;
	if (t < -ROUNDOFF_ERROR) t = 0.0;
	if (t  > 1.0 + ROUNDOFF_ERROR) t = 1.0;
	if (cyclic) {
	    if (index < 0) {
		if (index < -entries.length) {
		    index = index % entries.length;
		}
		if (index < 0)	index += entries.length;
	    } else if (index >= entries.length) {
		index = index % entries.length;
	    }
	} else {
	    if (index < 0 || index > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange", u));
	    }
	    if (index == entries.length) {
		if (t > ROUNDOFF_ERROR)
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRange", u));
		index--;
		t = 1.0;
	    }
	}
	/*
	double dxdu;
	double dydu;
	double dzdu;
	double d2xdu2;
	double d2ydu2;
	double d2zdu2;
	double omt;
	*/
	Path3DInfo.Entry entry = entries[index];
	return entry.data.d2sDu2(new Path3DInfo.UValues(t));
	/*
	switch (entry.type) {
	case PathIterator3D.SEG_MOVETO:
	    return 0.0;
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    return 0.0;
	    // dxdu = coords[0] - x0;
	    // dydu = coords[1] - y0;
	    // break;
	case PathIterator3D.SEG_QUADTO:
	    omt =  1.0 - t;
	    dxdu = 2.0 * (omt*(entry.coords[0]-entry.x)
			  + t*(entry.coords[3]-entry.coords[0]));
	    dydu = 2.0 * (omt*(entry.coords[1]-entry.y)
			  + t*(entry.coords[4]-entry.coords[1]));
	    dzdu = 2.0 * (omt*(entry.coords[2]-entry.z)
			  + t*(entry.coords[5]-entry.coords[2]));
	    d2xdu2 = 2.0 * (entry.x - entry.coords[0] + entry.coords[3]
			    - entry.coords[0]);
	    d2ydu2 = 2.0 * (entry.y - entry.coords[1] + entry.coords[4]
			    - entry.coords[1]);
	    d2zdu2 = 2.0 * (entry.z - entry.coords[2] + entry.coords[5]
			    - entry.coords[2]);
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    omt =  1.0 - t;
	    dxdu = 3.0 * (omt*omt*(entry.coords[0] - entry.x)
			  + 2.0*t*omt*(entry.coords[3] - entry.coords[0])
			  + t*t*(entry.coords[6] - entry.coords[3]));
	    dydu = 3.0 * (omt*omt*(entry.coords[1] - entry.y)
			  + 2.0*t*omt*(entry.coords[4] - entry.coords[1])
			  + t*t*(entry.coords[7] - entry.coords[4]));
	    dzdu = 3.0 * (omt*omt*(entry.coords[2] - entry.z)
			  + 2.0*t*omt*(entry.coords[5] - entry.coords[2])
			  + t*t*(entry.coords[8] - entry.coords[5]));
	    double u1u1deriv = 2.0 * (omt);
	    double uuderiv = 2.0 * t;
	    double uu1deriv = 1.0 - 2.0 * t;
	    d2xdu2 = 3.0 * (u1u1deriv*(entry.coords[0] - entry.x)
			  + 2.0*uu1deriv*(entry.coords[3] - entry.coords[0])
			  + uuderiv*(entry.coords[6] - entry.coords[3]));
	    d2ydu2 = 3.0 * (u1u1deriv*(entry.coords[1] - entry.y)
			  + 2.0*uu1deriv*(entry.coords[4] - entry.coords[1])
			  + uuderiv*(entry.coords[7] - entry.coords[4]));
	    d2zdu2 = 3.0 * (u1u1deriv*(entry.coords[2] - entry.z)
			  + 2.0*uu1deriv*(entry.coords[5] - entry.coords[2])
			  + uuderiv*(entry.coords[8] - entry.coords[5]));
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	double dsduSquared = dxdu*dxdu + dydu*dydu + dzdu*dzdu;
	if (dsduSquared == 0.0) {
	    // Need the fully qualified class name because we are a
	    // subclass of Path3D.Double.
	    return java.lang.Double.NaN;
	} else {
	    return (dxdu*d2xdu2 + dydu*d2ydu2 + dzdu*d2zdu2)
		/ Math.sqrt(dsduSquared);
	}
	*/
    }

    private double inversionLimit = -1.0; // -1.0 => use the default

    /**
     * Get the inversion limit.
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
     */
    public double getInversionLimit() {
	return inversionLimit;
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
     */
    public void setInversionLimit(double limit) {
	if (limit < 0.0) {
	    inversionLimit = -1.0;
	} else {
	    inversionLimit = limit;
	}
    }




    private CubicSpline getSublength(int index) {
	if (sublengths == null) sublengths = new CubicSpline[entries.length];
	if (sublengths[index] != null) {
	    return sublengths[index];
	} else {
	    final Path3DInfo.Entry entry = entries[index];
	    GLQuadrature glq = new GLQuadrature(16) {
		    protected double function(double t) {
			return Path3DInfo.dsDu(t, entry.x, entry.y, entry.z,
					       entry.type, entry.coords);
		    }
		};
	    double[] svalues = new double[numberOfIntervals+1];
	    double[] dvalues = new double[numberOfIntervals+1];
	    // getSublength is called after cumulativeLength is initialized.
	    svalues[0] = cumulativeLength[index];
	    dvalues[0] = Path3DInfo.dsDu(0.0, entry.x, entry.y, entry.z,
					 entry.type, entry.coords);
	    Path3DInfo.SegmentData segdata =
		new Path3DInfo.SegmentData(entry.type,
					   entry.x, entry.y, entry.z,
					   entry.coords, null);
	    synchronized (glq4seglen) {
		for (int i = 1; i < numberOfIntervals/*+1*/; i++) {
		    double t1 = ((double)(i-1))/numberOfIntervals;
		    double t2 = ((double)(i))/numberOfIntervals;
		    segindex = i;
		    // svalues[i] = svalues[i-1] + glq.integrate(t1, t2);
		    svalues[i] = svalues[i-1]
			+ glq4seglen.integrateWithP(u4seglen[segindex],
						    segdata);
		    dvalues[i] = Path3DInfo.dsDu(t2, entry.x, entry.y, entry.z,
						 entry.type, entry.coords);
		}
		svalues[numberOfIntervals] = cumulativeLength[index]
		    + entries[index]./*length*/getSegmentLength();
		dvalues[numberOfIntervals] =
		    Path3DInfo.dsDu(1.0, entry.x, entry.y, entry.z, entry.type,
				    entry.coords);
		sublengths[index] = new CubicSpline1(svalues, 0.0,
						     1.0/numberOfIntervals,
						     CubicSpline.Mode.HERMITE,
						     dvalues);
	    }
	    if (inversionLimit >= 0.0) {
		sublengths[index].setInversionLimit(inversionLimit);
	    }
	    return sublengths[index];
	}
    }

    static final int N4LEN = 16;

    static double[] u4len = GLQuadrature.getArguments(0.0, 1.0, N4LEN);
    static Path3DInfo.UValues[] uv4len = new Path3DInfo.UValues[N4LEN];
    static {
	for (int i = 0; i < N4LEN; i++) {
	    uv4len[i] = new Path3DInfo.UValues(u4len[i]);
	    u4len[i] = (double) i;
	}
    }
    static GLQuadrature<Path3DInfo.SegmentData> glq4len =
	new GLQuadrature<Path3DInfo.SegmentData>(N4LEN) {
	protected double function(double iu, Path3DInfo.SegmentData data) {
	    int i = (int)Math.round(iu);
	    return data.dsDu(uv4len[i]);
	}
    };

    /**
     * Get the control points and mode for a segment.
     * The segments are in the same order as those returned by
     * a path iterator, but do not include an initial
     * {@link PathIterator3D#SEG_MOVETO} segment nor a terminating
     * {@link PathIterator3D#SEG_CLOSE} segment. There are two
     * control points for linear segments, three for quadratic B&eacute;zier
     * curve segments, and four for cubic B&eacute;zier curve segments.
     * Path iterators do not provide the 0<sup>th</sup> control point for
     * these cases directly&mdash;instead the caller has to store the
     * final control point obtained for the previous segment.
     * @param i the segment index specified by an integer in the
     *        range [0, n) where n is the number of segments
     * @param coords an array of at least 12 elements that will
     *        contain the control points
     * @return the mode for the segment
     *         ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     */
    public int getSegment(int i, double[] coords)
	throws IllegalArgumentException
    {
	if (entries == null) refresh();
	if (i < 0 || i >= entries.length) {
	    // System.out.println("i = " + i + ", entries.length = " + entries.length);
	    throw new IllegalArgumentException(errorMsg("segmentIndex", i));
	}
	Path3DInfo.Entry entry = entries[i];
	coords[0] = entry.x;
	coords[1] = entry.y;
	coords[2] = entry.z;
	int result = entry.type;
	switch (result) {
	case PathIterator3D.SEG_LINETO:
	    System.arraycopy(entry.coords, 0, coords, 3, 3);
	    break;
	case PathIterator3D.SEG_QUADTO:
	    System.arraycopy(entry.coords, 0, coords, 3, 6);
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    System.arraycopy(entry.coords, 0, coords, 3, 9);
	    break;
	}
	return result;
    }

   private double getSegmentLength(int i) {
	if (entries == null) refresh();
	final Path3DInfo.Entry entry = entries[i];
	if (false /*entry.length < 0*/) {
	    /*
	    switch (entry.type) {
	    case PathIterator3D.SEG_MOVETO:
		entry.length = 0.0;
		break;
	    case PathIterator3D.SEG_CLOSE:
	    case PathIterator3D.SEG_LINETO:
		{
		    double dx = entry.coords[0] - entry.x;
		    double dy = entry.coords[1] - entry.y;
		    double dz = entry.coords[2] - entry.z;
		    if (dx == 0.0 && dz == 0.0) {
			if (dy == 0.0) entry.length = 0.0;
			else entry.length = Math.abs(dy);
		    } else if (dy == 0.0 && dz == 0.0) {
			if (dx == 0.0) entry.length = 0.0;
			else entry.length = Math.abs(dx);
		    } else if (dx == 0.0 && dy == 0.0) {
			if (dz == 0.0) entry.length = 0.0;
			else entry.length = Math.abs(dz);
		    } else {
			entry.length = Math.sqrt(dx*dx + dy*dy + dz*dz);
		    }
		}
		break;
	    default:
		{
		    double[] fcoords = new double[9];
		    double delta;
		    double dx = 0.0, dy = 0.0, dz = 0.0;;
		    if (entry.type == PathIterator3D.SEG_QUADTO) {
			dx = entry.coords[3] - entry.x;
			dy = entry.coords[4] - entry.y;
			dz = entry.coords[5] - entry.y;
		    } else if (entry.type == PathIterator3D.SEG_CUBICTO) {
			dx = entry.coords[6] - entry.x;
			dy = entry.coords[7] - entry.y;
			dz = entry.coords[8] - entry.y;
		    } else {
			throw new RuntimeException
			    ("type value not expected: " + entry.type);
		    }
		    delta = 0.05 * Math.sqrt(dx*dx + dy*dy + dz*dz);
		    FlatteningPathIterator3D fpit
			= new FlatteningPathIterator3D(entry.type,
						       entry.x, entry.y,
						       entry.z,
						       entry.coords, delta, 10);
		    fpit.next();
		    double xx0 = entry.x;
		    double yy0 = entry.y;
		    double zz0 = entry.z;
		    Adder adder2 = new Adder.Kahan();
		    while (!fpit.isDone()) {
			int fst = fpit.currentSegment(fcoords);
			double flen =
			    glq4len.integrateWithP(u4len,
						   new Path3DInfo.SegmentData
						   (fst, xx0, yy0, zz0,
						    fcoords, null));
			adder2.add(flen);
			if (fst == PathIterator3D.SEG_QUADTO) {
			    xx0 = fcoords[3];
			    yy0 = fcoords[4];
			    zz0 = fcoords[5];
			} else {
			    xx0 = fcoords[6];
			    yy0 = fcoords[7];
			    zz0 = fcoords[8];
			}
			fpit.next();
		    }
		    entry.length = adder2.getSum();
		}
	    }
	    */
	    // lengthCount++;
	    // totalLength += entry.length;
	}
	// return entry.length;
	return entry.getSegmentLength();
    }

    /**
     * Get the total length of the path.
     * @return the path length
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getPathLength()
	throws IllegalStateException, IllegalArgumentException
    {
	if (entries == null) refresh();
	initCumulativeLengths();
	/*
	if (lengthCount < entries.length) {
	    for (int i = 0; i < entries.length; i++) {
		getSegmentLength(i);
	    }
	}
	*/
	return totalLength;
    }

    /**
     * Get the length of a subpath.
     * The returned value is the same if u1 and u2 are exchanged:
     * lengths are non-negative.
     * @param u1 the parameter at the first end point of a subpath
     * @param u2 the parameter at the second end point of a subpath
     * @return the length of the subpath
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getPathLength(double u1, double u2)
	throws IllegalStateException, IllegalArgumentException
    {
	if (u2 < u1) {
	    return getDistance(u2, u1);
	} else {
	    return getDistance(u1, u2);
	}
    }

    /**
     * Get the distance traversed on a subpath.
     * If u2 &lt; u1, the value returned is negative.
     * <P>
     * This is intended for cases where direction is important.
     * @param u1 the parameter at the first end point of a subpath
     * @param u2 the parameter at the second end point of a subpath
     * @return the signed distance for a subpath
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getDistance(double u1, double u2)
	throws IllegalStateException, IllegalArgumentException
    {
	if (entries == null) refresh();
	initCumulativeLengths();
	if (!cyclic) {
	    if (u1 < 0 || u1 > entries.length
		|| u2 < 0 || u2 > entries.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange2", u1, u2));
	    }
	}
	if (u1 < u2) {
	    double u0 = Math.floor(u1);
	    int ind1 = (int)Math.round(u0);
	    double t1 = u1-u0;
	    double ua = Math.ceil(u1);
	    int ind = (int)Math.round(ua);
	    double ub = Math.floor(u2);
	    int ind2 = (int)Math.round(ub);
	    double t2 = u2-ub;
	    // double sum = 0.0;
	    Adder adder = new Adder.Kahan();
	    if (cyclic) {
		int n = (ind2 - ind1) / entries.length;
		int m = (ind2 - ind) % entries.length;
		if (ind1 < 0) {
		    if (ind1 < - entries.length) {
			ind1 = ind1 % entries.length;
		    }
		    if (ind1 < 0) {
			ind1 += entries.length;
		    }
		} else {
		    ind1 = ind1 % entries.length;
		}
		if (ind < 0) {
		    if (ind < - entries.length) {
			ind = ind % entries.length;
		    }
		    if (ind < 0) {
			ind += entries.length;
		    }
		} else {
		    ind = ind % entries.length;
		}
		if (ind2 < 0) {
		    if (ind2 < - entries.length) {
			ind2 = ind2 % entries.length;
		    }
		    if (ind2 < 0) {
			ind2 += entries.length;
		    }
		} else {
		    ind2 = ind2 % entries.length;
		}
		final Path3DInfo.Entry entry1 = entries[ind1];
		/*
		GLQuadrature glq = new GLQuadrature(16) {
			protected double function(double u) {
			    return Path3DInfo.dsDu(u, entry1.x, entry1.y, 
						   entry1.type, entry1.coords);
			}
		    };
		*/
		CubicSpline spline = getSublength(ind1);

		if (n > 0) {
		    /*
		    System.out.println("adding total path length " + n
				       + " times");
		    */
		    // sum += n * getPathLength();
		    adder.add(n * getPathLength());
		}
		if (ind1 == ind2) {
		    // sum += glq.integrate(t1, t2);
		    // sum += spline.valueAt(t2) - spline.valueAt(t1);
		    adder.add(spline.valueAt(t2) - spline.valueAt(t1));
		} else {
		    if (ind1 != ind) {
			// if ind1 == ind, the sum will be covered
			// in the loop from 0 to m below.
			/*
			System.out.println("adding t = " + t1
					   + " to 1.0 for ind1 = " + ind1);
			*/
			// sum += glq.integrate(t1, 1.0);
			// sum += spline.valueAt(1.0) - spline.valueAt(t1);
			adder.add(spline.valueAt(1.0) - spline.valueAt(t1));
		    }
		    /*
		    for (int i = 0; i < m; i++) {
			int ii = (ind + i) % entries.length;
			// System.out.println("adding segment " + ii);
			// sum += getSegmentLength(ii);
			adder.add(getSegmentLength(ii));
			}*/
		    int k1 = ind;
		    int k2 = (ind + m) % entries.length;
		    double sum = 0.0;
		    if (m > 0) {
			if (m == entries.length) {
			    adder.add(totalLength);
			} else {
			    if (k1 >= k2) {
				adder.add(totalLength);
			    }
			    adder.add(cumulativeLength[k2]);
			    adder.add(-cumulativeLength[k1]);
			};

		    }
		    if (t2 > 0.0) {
			final Path3DInfo.Entry entry2 = entries[ind2];
			/*
			glq = new GLQuadrature(16) {
				protected double function(double u) {
				    return Path3DInfo.dsDu(u, entry2.x,
							   entry2.y, 
							   entry2.type,
							   entry2.coords);
				}
			    };
			*/
			spline = getSublength(ind2);
			/*
			System.out.println("adding t = 0 to " + t2
					   + " for ind2 = " + ind2);
			*/
			// sum += glq.integrate(0.0, t2);
			// sum += spline.valueAt(t2) - spline.valueAt(0.0);
			adder.add(spline.valueAt(t2) - spline.valueAt(0.0));
		    }
		}
	    } else {
		final Path3DInfo.Entry entry1 = entries[ind1];
		/*
		GLQuadrature glq = new GLQuadrature(16) {
			protected double function(double u) {
			    return Path3DInfo.dsDu(u, entry1.x, entry1.y, 
						   entry1.type,
						   entry1.coords);
			}
		    };
		*/
		CubicSpline spline = getSublength(ind1);
		if (ind1 == ind2) {
		    /*
		    System.out.println("adding " + t1 + " to " + t2
				       + " for ind1, ind2 = " + ind1);
		    */
		    // sum += glq.integrate(t1, t2);
		    // sum += spline.valueAt(t2) - spline.valueAt(t1);
		    adder.add(spline.valueAt(t2) - spline.valueAt(t1));
		} else {
		    if (ind != ind1) {
			/*
			System.out.println("adding " +t1
					   + " to 1.0  for ind1 = "
					   + ind1);
			*/
			// sum += glq.integrate(t1, 1.0);
			// sum += spline.valueAt(1.0) - spline.valueAt(t1);
			adder.add(spline.valueAt(1.0) - spline.valueAt(t1));
		    }
		    while (ind < ind2) {
			// System.out.println("adding segment " + ind);
			// sum += getSegmentLength(ind++);
			adder.add(getSegmentLength(ind++));
		    }
		    if (t2 > 0.0) {
			final Path3DInfo.Entry entry2 = entries[ind2];
			/*
			glq = new GLQuadrature(16) {
				protected double function(double u) {
				    return Path3DInfo.dsDu(u, entry2.x,
							   entry2.y, 
							   entry2.type,
							   entry2.coords);
				}
			    };
			*/
			spline = getSublength(ind2);
			// sum += spline.valueAt(t2) - spline.valueAt(0.0);
			adder.add(spline.valueAt(t2) - spline.valueAt(0.0));
			/*
			System.out.println("adding 0.0 to " + t2
					   + " for ind2 = " + ind2);
			*/
			// sum += glq.integrate(0.0, t2);
		    }
		}
	    }
	    // return sum;
	    return adder.getSum();
	} else 	if (u1 == u2) {
	    return 0.0;
	} else {
	    return -getDistance(u2, u1);
	}
    }

    /**
     * Get the distance along a path from its start for a specified
     * path parameter.  The argument may be negative for cyclic paths,
     * in which case the value returned will also be non-positive.
     * @param u the path parameter
     * @return the distance along a path from its start
     */
    public double s(double u) 
	throws IllegalStateException, IllegalArgumentException
    {
	return getDistance(0.0, u);
    }

    private void initCumulativeLengths() {
	if (cumulativeLength != null) return;
	cumulativeLength = new double[entries.length];
	// double cl = 0.0;
	Adder adder = new Adder.Kahan();
	for (int i = 0; i < entries.length; i++) {
	    // cumulativeLength[i] = cl;
	    cumulativeLength[i] = adder.getSum();
	    // cl += entries[i].length;
	    // cl += getSegmentLength(i);
	    adder.add(getSegmentLength(i));
	}
	totalLength = adder.getSum();
    }


    /**
     * Get the path parameter corresponding to a specified distance
     * along a path from its start.
     * @param s the distance along a path from its start
     * @return the path parameter
     */
    public double u(double s) 
	throws IllegalStateException, IllegalArgumentException
    {
	if (s == 0.0) return 0.0;
	double pathLen = getPathLength();
	initCumulativeLengths();
	if (cyclic) {
	    double tmp = Math.floor(s/pathLen);
	    double u0 = tmp*entries.length;
	    double s0 = tmp*pathLen;
	    double sd = s - s0;
	    int index = Arrays.binarySearch(cumulativeLength, sd);
	    if (index < 0) {
		// binarySearch for a missing key returns 
		// -(insertion point) - 1
		// where the insertion point is the index at which the
		// missing key would be inserted.  We want the index
		// before the insertion point.  Meanwhile sd >= 0.0 and
		// cumulativeLength[0] = 0.0 so the minimum insertion point
		// is 1.
		index = -index - 2;
		double du;
		try {
		    du = getSublength(index).inverseAt(sd);
		} catch (IllegalArgumentException e) {
		    // If ds/du is close to 0.0 the inverse may fail.
		    // In that case, we use Brent's algorithm. This case
		    // should not occur very often. It did show up in a
		    // test program.
		    final CubicSpline spline = getSublength(index);
		    RootFinder rf = new RootFinder.Brent() {
			    public double function(double u) {
				return spline.valueAt(u);
			    }
			};
		    try {
			du = rf.solve(sd, 0.0, 1.0);
		    } catch (RootFinder.ConvergenceException ee) {
			// if Brent's method didn't work, we must
			// be very close to an edge, so pick the
			// value that is closest.
			double sd0 = spline.valueAt(0.0);
			double sd1 = spline.valueAt(1.0);
			du = (Math.abs(sd0 - sd) < Math.abs(sd1 - sd))? 0.0:
			    1.0;
		    }
		}
		return u0 + index + du;
	    } else {
		return u0 + index;
	    }
	} else {
	    // simpler as s is non-negative with a maximum value
	    if (s < 0.0 || s > pathLen) {
		throw new 
		    IllegalArgumentException(errorMsg("argOutOfRange", s));
	    }

	    int index = Arrays.binarySearch(cumulativeLength, s);
	    if (index < 0) {
		// binarySearch for a missing key returns 
		// -(insertion point) - 1
		// where the insertion point is the index at which the
		// missing key would be inserted.  We want the index
		// before the insertion point.  Meanwhile sd >= 0.0 and
		// cumulativeLength[0] = 0.0 so the minimum insertion point
		// is 1.
		index = -index - 2;
		double du;
		try {
		    du = getSublength(index).inverseAt(s);
		} catch (IllegalArgumentException e) {
		    // If ds/du is close to 0.0 the inverse may fail.
		    // In that case, we use Brent's algorithm. This case
		    // should not occur very often. It did show up in a
		    // test program.
		    final CubicSpline spline = getSublength(index);
		    RootFinder rf = new RootFinder.Brent() {
			    public double function(double u) {
				return spline.valueAt(u);
			    }
			};
		    try {
			du = rf.solve(s, 0.0, 1.0);
		    } catch (RootFinder.ConvergenceException ee) {
			// Brent's algorithm failed as well, so we must be
			// very close to an end of a segment where
			// either du = 0.0 or du = 1.0.  We'll pick whichever
			// is closest.  If the  segment length is nearly zero,
			// the choice is arbitrary.
			double s0 = spline.valueAt(0.0);
			double s1 = spline.valueAt(1.0);
			du = Math.abs(s-s0) < Math.abs(s-s1)? 0.0: 1.0;
		    }
		}
		return index + du;
	    } else {
		return index;
	    }
	}
    }

    /**
     * Reset the path so that it contains no entries.
     * This method should be used instead of {@link Path3D#reset()}, which
     * cannot be overridden due to being declared as final.
     */
    public void clear() {
	reset();
	entries = null;
	cumulativeLength = null;
	sublengths = null;
	cyclic = false;
	// lengthCount = 0;
	totalLength = 0.0;
    }

    /**
     * Refresh the tables used to map the parameter to X, Y, and Z coordinates
     * and used to determine lengths and distances, etc. If the path is
     * modified after any of the methods
     * <ul>
     *   <li>{@link #getX(double)}
     *   <li>{@link #getY(double)}
     *   <li>{@link #dxDu(double)}
     *   <li>{@link #dyDu(double)}
     *   <li>{@link #d2xDu2(double)}
     *   <li>{@link #d2yDu2(double)}
     *   <li>{@link #dsDu(double)}
     *   <li>{@link #d2sDu2(double)}
     *   <li>{@link #curvature(double)}
     *   <li>{@link #s(double)}
     *   <li>{@link #u(double)}
     *   <li>{@link #getPathLength()}
     *   <li>{@link #getPathLength(double,double)}
     *   <li>{@link #getDistance(double,double)}
     * </ul>
     * are called, the method {@link #refresh()} must be called.  Paths
     * are modified by calling
     * <ul>
     *   <li>{@link SplinePath3D#addCycle(double[],double[],double[])}
     *   <li>{@link SplinePath3D#addCycle(double[],double[],double[],int)}
     *   <li>{@link SplinePath3D#addCycle(Point3D[])}
     *   <li>{@link SplinePath3D#addCycle(Point3D[],int)}
     *   <li>{@link SplinePath3D#cycleTo(double[],double[],double[])}
     *   <li>{@link SplinePath3D#cycleTo(double[],double[],double[],int)}
     *   <li>{@link SplinePath3D#cycleTo(Point3D[])}
     *   <li>{@link SplinePath3D#cycleTo(Point3D[],int)}
     *   <li>{@link SplinePath3D#cycleTo(org.bzdev.math.RealValuedFunctOps,org.bzdev.math.RealValuedFunctOps,org.bzdev.math.RealValuedFunctOps,double, double,int)}
     *   <li>{@link SplinePath3D#splineTo(double[],double[],double[])}
     *   <li>{@link SplinePath3D#splineTo(double[],double[],double[],int)}
     *   <li>{@link SplinePath3D#splineTo(Point3D[])}
     *   <li>{@link SplinePath3D#splineTo(Point3D[],int)}
     *   <li>{@link SplinePath3D#splineTo(org.bzdev.math.RealValuedFunctOps,org.bzdev.math.RealValuedFunctOps,org.bzdev.math.RealValuedFunctOps,double,double,int)}
     *   <li>{@link org.bzdev.geom.Path3D.Double#append(PathIterator3D,boolean)}
     *   <li>{@link Path3D.Double#curveTo(double,double,double,double,double,double,double,double,double)}
     *   <li>{@link Path3D.Double#lineTo(double,double,double)}
     *   <li>{@link Path3D.Double#moveTo(double,double,double)}
     *   <li>{@link Path3D.Double#quadTo(double,double,double,double, double,double)}
     *   <li>{@link Path3D.Double#transform(Transform3D)}
     *   <li>{@link Path3D#append(PathIterator3D,boolean)}
     *   <li>{@link Path3D#append(Path3D,boolean)}
     *   <li>{@link Path3D#closePath()}
     *   <li>{@link Path3D#reset()} (which should not be used).
     *   <li>{@link #clear()}
     * </ul>
     * The method {@link #clear()} calls {@link Path3D#reset()} but
     * also reinitializes the path so that refresh() does not have to
     * be called if segments are added after the call to {@link #clear()}
     * but before any of the methods in the list starting with
     * {@link #getX(double)} are called.
     */
    public void refresh() throws IllegalStateException {
	PathIterator3D pit = getPathIterator(null);
	if (pit.isDone()) {
	    throw new IllegalStateException(errorMsg("emptyPath"));
	}
	// local variable - same name as the instance variable
	// the instance variable is set at the end.
	boolean cyclic = false;
	// ArrayList<Path3DInfo.Entry> entries = new ArrayList<>();
	cumulativeLength = null;
	sublengths = null;
	List<Path3DInfo.Entry> elist =  Path3DInfo.getEntries(this);
	double[] coords = new double[9];
	int mode = pit.currentSegment(coords);
	if (mode != PathIterator3D.SEG_MOVETO) {
	    throw new IllegalStateException(errorMsg("piSEGMOVETO"));
	}
	/*
	double x = coords[0];
	double y = coords[1];
	double z = coords[2];
	double x0 = x;
	double y0 = y;
	double z0 = z;
	*/
	pit.next();
	while (!pit.isDone()) {
	    /*
	    Entry entry = new Entry();
	    entry.x = x;
	    entry.y = y;
	    entry.z = z;
	    entry.type = pit.currentSegment(entry.coords);
	    */
	    int type = pit.currentSegment(coords);
	    switch(type) {
	    case PathIterator3D.SEG_CLOSE:
		cyclic = true;
		/*
		if (x != x0 && y != y0) {
		    entry.type = PathIterator3D.SEG_LINETO;
		    entry.x = x0;
		    entry.y = y0;
		    entry.z = z0;
		    entries.add(entry);
		}
		*/
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (cyclic) {
		    throw new IllegalStateException
			(errorMsg("segsAfterClose"));
		}
		/*
		x = entry.coords[6];
		y = entry.coords[7];
		z = entry.coords[8];
		entries.add(entry);
		*/
		break;
	    case PathIterator3D.SEG_LINETO:
		if (cyclic) {
		    throw new IllegalStateException
			(errorMsg("segsAfterClose"));
		}
		/*
		x = entry.coords[0];
		y = entry.coords[1];
		z = entry.coords[2];
		entries.add(entry);
		*/
		break;
	    case PathIterator3D.SEG_MOVETO:
		if (mode == PathIterator3D.SEG_MOVETO) {
		    // multiple moveTo operations at the start
		    // are harmless---just use the most recent one.
		    /*
		    x = entry.coords[0];
		    y = entry.coords[1];
		    z = entry.coords[2];
		    x0 = x;
		    y0 = y;
		    z0 = z;
		    */
		} else {
		    throw new IllegalStateException
			("only initial SEG_MOVETO allowed");
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (cyclic) {
		    throw new IllegalStateException
			(errorMsg("segsAfterClose"));
		}
		/*
		x = entry.coords[3];
		y = entry.coords[4];
		z = entry.coords[5];
		entries.add(entry);
		*/
		break;
	    }
	    mode = /* entry.mode*/ type;
	    pit.next();
	} 
	// this.entries = new Entry[entries.size()];
	// this.entries = entries.toArray(this.entries);
	// lengthCount = 0;
	totalLength = 0.0;
	this.cyclic = cyclic;
	entries =
	    new Path3DInfo.Entry[elist.size() - (cyclic? 2: 1)];
	int i = -1;
	for (Path3DInfo.Entry entry: elist) {
	    if (i < 0 || i >= entries.length) {
		i++;
		continue;
	    }
	    entries[i++] = entry;
	}
    }
 }

//  LocalWords:  exbundle SplinePath anim BasicSplinePath rescaled xf
//  LocalWords:  initialCapacity IllegalArgumentException yf zf SEG
//  LocalWords:  lengthCount coords integerMin appendable Appendable
//  LocalWords:  CUBICTO LINETO QUADTO IllegalStateException NaN ds
//  LocalWords:  badLocation PathIterator MOVETO argOutOfRange omt xp
//  LocalWords:  badSwitch errorMsg blockquote pre sqrt DInfo dxDu yp
//  LocalWords:  dyDu zp dzDu xpp xDu ypp yDu zpp zDu tmp piUnknown
//  LocalWords:  dxdu dydu dzdu tomt dsduSquared xdu ydu zdu deriv uu
//  LocalWords:  uuderiv inversionLimit getSublength cumulativeLength
//  LocalWords:  svalues glq eacute zier th mdash dx dy fcoords fpit
//  LocalWords:  RuntimeException FlatteningPathIterator yy zz Kahan
//  LocalWords:  isDone fst currentSegment flen len integrateWithP lt
//  LocalWords:  SegmentData getSum totalLength getSegmentLength dsDu
//  LocalWords:  subpath GLQuadrature getPathLength valueAt sd du ul
//  LocalWords:  binarySearch getX getY sDu getDistance addCycle
//  LocalWords:  cycleTo splineTo boolean curveTo lineTo moveTo
//  LocalWords:  quadTo closePath reinitializes emptyPath ArrayList
//  LocalWords:  piSEGMOVETO segsAfterClose
