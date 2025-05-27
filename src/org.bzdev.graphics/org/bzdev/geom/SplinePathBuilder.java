package org.bzdev.geom;

//@exbundle org.bzdev.geom.lpack.Geom

import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.NamedFunctionOps;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.scripting.ScriptingContext;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

/**
 * SplinePath2D builder.
 * This class allows a SplinePath2D to be specified via a table - an
 * array of entries, each of which specifies a point along the path
 * (including control points) and the type of line segment.
 * The sequence of points, based on their types, is shown in
 * the following diagram, where a transition corresponds to adding a
 * new point to the SplinePath2D builder:
 * <P style="text-align:center">
 * <IMG SRC="doc-files/pathbuilder.png" class="imgBackground" alt="UML diagram">
 * </P>
 * <P>
 * The line colors are not significant - they are just used to
 * make it obvious that lines cross rather than join. Bidirectional
 * arrows denote pairs of transitions (essentially two unidirectional lines
 * that overlap).
 * <P>
 * The sequence of allowed transitions and their effects are as follows:
 * <UL>
 *    <li> The first point's type is either <code>MOVE_TO</code> or
 *         <code>MOVE_TO_NEXT</code>.
 *    <li> A segment containing only <code>CONTROL</code> points (0, 1,
 *         or 2) must end with either a <CODE>SEG_END</CODE> or a
 *         <CODE>CLOSE</CODE> point. These
 *         represent straight lines, quadratic B&eacute;zier curves, and
 *         cubic B&eacute;zier curves respectively. The control points
 *         must be preceded by a point whose type is <code>MOVE_TO</code>,
 *         <code>SEG_END</code> or <code>SEG_END_PREV</code>.
 *     <li> An open spline segment starts with either the end of another
 *          segment or a point whose type is either <code>MOVE_TO</code>
 *          or <code>MOVE_TO_NEXT</code>. An open spline ends
 *         with a point whose type is <code>SEG_END</code> or
 *         <code>SEG_END_PREV</code>, and contains points (at least one)
 *         whose types are either <code>SPLINE</code> or
 *         <code>SPLINE_FUNCTION</code>. The sequence of <CODE>SPLINE</CODE>
 *         and/or <CODE>SPLINE_FUNCTION</CODE> points may be followed
 *         by a <CODE>CONTROL</CODE>or preceded by a
 *         <CODE>CONTROL</CODE> in order to set the first and last control
 *         point on the segment to specific values.
 *    <li> A closed subpath is indicated by a point whose type is
 *         <code>CLOSE</code>.  If this point is preceded by a point of type
 *         <code>SEG_END</code> or <code>SEG_END_PREV</code>, a straight line
 *         (unless the line will have a length of zero) will connect the
 *         previous point to the point specified by the last point whose
 *         type is <code>MOVE_TO</code> or <code>MOVE_TO_NEXT</code>. If a
 *         sequence of <code>SPLINE</code> or <code>SPLINE_FUNCTION</code>
 *         points is terminated by a <code>CLOSE</code> point, there are
 *         two cases:
 *         <UL>
 *            <LI> If the point before the sequence of <code>SPLINE</code>
 *                 or <code>SPLINE_FUNCTION</code> points has a type of
 *                 <code>CLOSE</code>, <code>MOVE_TO</code>
 *                 or <code>MOVE_TO_NEXT</code>, a closed spline will be
 *                 generated consisting of this initial point and the spline
 *                 points. The closed path will terminate at the initial point.
 *            <LI> If the point before the sequence of <code>SPLINE</code>
 *                 or <code>SPLINE_FUNCTION</code> points has a type of
 *                 <code>SEG_END</code> or <code>SEG_END_PREV</code>, a
 *                 <code>MOVE_TO</code> point at the same location will
 *                 effectively be inserted, and the behavior will then
 *                 match that of the previous case.
 *         </UL>
 * </UL>
 * <P>
 * An instance of SplinePathBuilder can be constructed by passing it
 * an instance of {@link org.bzdev.scripting.ScriptingContext} such
 * as the variable <CODE>scripting</CODE> provided by the
 * <CODE>scrunner</CODE> command. When this is done, one can use
 * the methods {@link SplinePathBuilder#configure(Object)} or
 * {@link SplinePathBuilder#configure(Object,Object)} to configure a
 * spline-path builder. For the two-argument case, the first argument
 * is a winding rule. This can be the string <CODE>"WIND_EVEN_ODD"</CODE>
 * or <CODE>"WIND_NON_ZERO"</CODE>. Alternatively it can be a constant
 * whose type is {@link SplinePathBuilder.WindingRule}.
 * The last argument (the only one for the one-argument case) will be
 * a specification for the path.  This specification is an array or list
 * of objects that have the following attributes:
 * <UL>
 *   <LI><CODE>type</CODE>. This can be the string <CODE>"CLOSE"</CODE>,
 *       <CODE>"MOVE_TO"</CODE>, <CODE>"MOVE_TO_NEXT"</CODE>,
 *       <CODE>"SEG_END"</CODE>, <CODE>"SEG_END_PREV"</CODE>,
 *       <CODE>"SEG_END_NEXT"</CODE>, <CODE>"CONTROL"</CODE>, or
 *       <CODE>"SPLINE"</CODE>, <CODE>"SPLINE_FUNCTION"</CODE>.
 *       Alternatively it can a constant whose type is
 *       {@link SplinePathBuilder.CPointType}.
 *   <LI><CODE>x</CODE>. The X coordinate of a control point. This is
 *       ignored, and may be omitted, when the type is <CODE>CLOSE</CODE>.
 *   <LI><CODE>y</CODE>. The Y coordinate of a control point. This is
 *       ignored, and may be omitted, when the type is <CODE>CLOSE</CODE>.
 * </UL>
 * This list can also contain sublists, which will be traversed in
 * depth-first order. The sequence of objects that appear must match
 * the constraints shown in the figure above.
 * <P>
 * For example, with ECMAScript, <CODE>scrunner</CODE>  the following
 * statements can be used to configure a  path:
 * <BLOCKQUOTE><PRE><CODE>
 *     var path1 = [
 *        {type: "MOVE_TO", x: 20.0, y: 30.0},
 *        {type: "SEG_END", x: 50.0, y: 60.0}];
 *
 *     var path2 = [
 *        {type: "MOVE_TO", x: 120.0, y: 130.0},
 *        {type: "SEG_END", x: 150.0, y: 160.0}];
 *
 *     var pathspec = [path1, path2];
 *
 *     org.bzdev.geom.SplinePathBuilder pb =
 *        new org.bzdev.geom.SplinePathBuilder(scripting);
 *
 *     pb.configure("WIND_EVEN_ODD", pathspec);
 *     var path = pb.getPath();
 * </CODE></PRE></BLOCKQUOTE>
 *  The program <CODE>epts</CODE> can generate the path specifications while
 *  providing a graphical user interface to aid in constructing the
 *  paths.
 */
public class SplinePathBuilder extends AbstractSplinePathBuilder<SplinePath2D> {

    /**
     * Constructor.
     */
    public SplinePathBuilder() {
	super();
    }

    /**
     * Constructor providing a scripting context.
     * @param parent the scripting context used to support scripting
     */
    public SplinePathBuilder(ScriptingContext parent) {
	super(parent);
    }


    /**
     * Enum to define winding rules.
     * For a Path2D.Double, Path2D.Float, and SplinePath2D, a winding
     * rule is specified by an integer constant defined in Path2D and used
     * in a constructor.
     * @see java.awt.geom.Path2D
     */
    public static enum WindingRule {
	/**
	 * Specify a winding rule equal to Path2D.WIND_EVEN_ODD.
	 */
	WIND_EVEN_ODD,

	/**
	 * Specify a winding rule equal to Path2D.WIND_NON_ZERO.
	 */
	WIND_NON_ZERO
    }

    /**
     * Type of a point along a path.
     * This is used in a constructor for the class
     * {@link SplinePathBuilder.CPoint SplinePathBuilder.CPoint}.
     */
    public static enum CPointType {
	/**
	 * Move to a new location.
	 * This defines the start of a path or the start of a part of
	 * a possibly discontinuous curve.
	 */
	MOVE_TO,
	/**
	 * Move to a new location at the start of the next sequence
	 * of points, which must be specified by a SPLINE or SPLINE_FUNCTION.
	 * This defines the start of a path or the start of a part of
	 * a discontinuous curve.
	 */
	MOVE_TO_NEXT,
	/**
	 * This defines a control point for a B&eacute;zier curve.
	 * There may be one or two control points in a row, and
	 * a control point must be followed by
	 * {@link SplinePathBuilder.CPointType#SEG_END SEG_END}.
	 */
	CONTROL,
	/**
	 * This defines a point along a spline.
	 */
	SPLINE,
        /**
	 * A sequence of n segments are added, with the the points at
	 * the ends of the segments defined by functions giving the x
	 * and y coordinates of those n points, and the starting and
	 * ending values of the argument passed to the functions.
	 */
	SPLINE_FUNCTION,
	/**
	 * This indicates that the final coordinate given by a SPLINE_FUNCTION
	 * point or a sequence of  SPLINE points represents a SEG_END.
	 * If there are two consecutive splines, the derivatives may be
	 * discontinuous at a SEG_END_PREV point.
	 */
	SEG_END_PREV,
	/**
	 * This ends a B&eacute;zier curve segment or a spline segment.
	 * If there are two consecutive splines, the derivatives may be
	 * discontinuous at a SEG_END point.
	 */
	SEG_END,
	/**
	 * This indicates that the next CPoint terminates the current
	 * segment. The next CPoint's type must be a SPLINE_FUNCTION or SPLINE
	 * point. It is useful primarily when the next CPoint's is a
	 * SPLINE_FUNCTION.
	 */
	SEG_END_NEXT,
	/**
	 * The path is to be closed. If the previous CPoint has a type
	 * equal to SEG_END, the path will consist of a line (perhaps of
	 * zero length) going to the position of the last MOVE_TO point
	 * along the path. If the previous CPoint has a type equal to
	 * SPLINE, a cyclic spline will be generated.
	 */
	CLOSE
    }

    static class NamedFunctionOpsAdapter implements NamedFunctionOps {

	static String errorMsg(String key, Object... args) {
	    return GeomErrorMsg.errorMsg(key, args);
	}

	RealValuedFunction f;

	NamedFunctionOpsAdapter(RealValuedFunctOps f) {
	    if (f == null) {
		throw new IllegalArgumentException(errorMsg("nullFunction"));
	    }
	    if (f instanceof RealValuedFunction) {
		this.f = (RealValuedFunction) f;
	    } else {
		this.f = new RealValuedFunction(f);
	    }
	}

	public RealValuedFunction getFunction() {
	    return f;
	}

	public double getDomainMax() {
	    return f.getDomainMax();
	}

	public boolean domainMaxClosed() {
	    return f.domainMaxClosed();
	}

	public double getDomainMin() {
	    return f.getDomainMin();
	}

	public boolean domainMinClosed() {
	    return f.domainMinClosed();
	}

	public double valueAt(double arg) throws
	    IllegalArgumentException, UnsupportedOperationException
	{
	    return f.valueAt(arg);
	}

	public double derivAt(double arg) throws
	    IllegalArgumentException, UnsupportedOperationException
	{
	    return f.derivAt(arg);
	}

	public double secondDerivAt(double arg) throws
	    IllegalArgumentException, UnsupportedOperationException
	{
	    return f.secondDerivAt(arg);
	}

	public boolean isInterned() {return false;}

	public String getName() {return null;}

	public boolean canDelete() {return false;}

	public boolean delete() {return false;}

	public boolean isDeleted() {return false;}

	public boolean deletePending() {return false;}
    }

    /**
     * Convert a RealFunctionOps to a NamedFunctionOps if necessary.
     * @param f the function
     * @return either f, when f is an instance of NamedFunctionOps, or
     *         a newly created NamedFunctionOps implemented by f
     */
    public static  NamedFunctionOps asNamedFunctionOps(RealValuedFunctOps f) {
	if (f instanceof NamedFunctionOps) return (NamedFunctionOps) f;
	else return new NamedFunctionOpsAdapter(f);
    }


    /**
     * Control Point specification for SplinePathBuilder.
     * This class specifies a control point.  An array of control
     * points defines a segment of a path added to the graph by
     * a call to {@link AbstractSplinePathBuilder#append(SplinePathBuilder.CPoint[]) append}.
     * <P>
     * This class is annotated so that it can be used by
     * named object factories.  In this case,
     * a parameter name is typically a component in a compound key.
     * A list of these names are as follows:
     * <ul>
     *   <li> "type" - one of the following:
     *         <ul>
     *            <li> <CODE>MOVE_TO</CODE> - Move to point. The entry
     *                 must include an "x" and "y" parameter.
     *            <li> <CODE>MOVE_TO_NEXT</CODE> - Move to a point
     *                 whose coordinates are given by the next entry,
     *                 which must be a SPLINE or SPLINE_FUNCTION. If
     *                 it is a SPLINE_FUNCTION, the value of the
     *                 function at t1 will be used.
     *            <li> <CODE>CONTROL</CODE> - A control point for a
     *                 cubic or quadratic curve. Both the x and y
     *                 coordinates must be provided.  at most 2
     *                 CONTROL points may appear in a row, and this
     *                 subsequence must be terminated by a SEG_END.
     *            <li> <CODE>SPLINE</CODE> - A point along a
     *                 spline. Both the x and y coordinates must be
     *                 provided.
     *            <li> <CODE>SPLINE_FUNCTION</CODE> - Functions are
     *                 used to compute a sequence of spline points.
     *                 The parameter xf, yf, t1, t2, and n must be
     *                 provided.
     *            <li> <CODE>SEG_END</CODE> - This represents the
     *                 final point in a straight line, quadratic
     *                 curve, cubic curve, or spline.  Both the x and
     *                 y coordinates must be provided.
     *            <li> <CODE>SEG_END_PREV</CODE> - This indicates that
     *                 final point in a spline is the previous entry,
     *                 which must have a type of SPLINE or
     *                 SPLINE_FUNCTION.  For the SPLINE_FUNCTION case,
     *                 t2 will be used to compute the x and y
     *                 coordinates of this point.
     *            <li> <CODE>CLOSE</CODE> - This indicates that the
     *                 curve is closed.  If the curve is a single
     *                 spline, the spline will be a closed, smooth
     *                 curve.  Otherwise a line, if necessary, will be
     *                 drawn from the current position to the last
     *                 MOVE_TO coordinates.
     *         </ul>
     *   <li> "x" - the x coordinate.
     *   <li> "y" - the y coordinate.
     *   <li> "xf" - the named object providing a function giving the x
     *               coordinate.  This object will implement the
     *               NamedFunctionOps interface.
     *   <li> "yf" - the named object providing a function giving the y
     *               coordinate.  This object will implement the
     *               NamedFunctionOps interface.
     *   <li> "t1" - The initial value of the argument passed to functions
     *               providing the x and y coordinates.
     *   <li> "t2" - The final  value of the argument passed to functions
     *               providing the x and y coordinates.
     *   <li> "n" -  The number of segments between points at which the
     *               functions are evaluated. This is one less than the
     *               number of those points.
     * </ul>
     */
    @CompoundParmType(tipResourceBundle = "*.lpack.SPBuilderCPntTips",
		      labelResourceBundle = "*.lpack.SPBuilderCPntLabels",
		      docResourceBundle = "*.lpack.SPBuilderCPntDocs")
    public static class CPoint {
	/**
	 * Field implementing a factory parameter providing a type.
	 */
	@PrimitiveParm("type") public CPointType type = null;
	/**
	 * Field implementing a factory parameter providing an X coordinate.
	 */
	@PrimitiveParm("x") public double x = 0.0;
	/**
	 * Field implementing a factory parameter providing a Y coordinate.
	 */
	@PrimitiveParm("y") public double y = 0.0;

	// following used only for SPLINE_FUNCTION case
	/**
	 * Field implementing a factory parameter providing a function
	 * whose value is an X coordinate.
	 */
	@PrimitiveParm("xf") public NamedFunctionOps xfOps = null;
	/**
	 * Field implementing a factory parameter providing a function
	 * whose value is a Y coordinate.
	 */
	@PrimitiveParm("yf") public NamedFunctionOps yfOps = null;
	/**
	 * Field implementing a factory parameter providing  the minimum
	 * value of a function's argument.
	 */
	@PrimitiveParm("t1") public double t1 = 0.0;
	/**
	 * Field implementing a factory parameter providing the maximum value
	 * of a function's argument.
	 */
	@PrimitiveParm("t2") public double t2 = 0.0;
	/**
	 * Field implementing a factory parameter providing the number of
	 * times a function will be evaluated to generate knots for a spline.
	 */
	@PrimitiveParm("n")  public int n = 0;

	static String errorMsg(String key, Object... args) {
	    return GeomErrorMsg.errorMsg(key, args);
	}

	/**
	 * Constructor.
	 */
	public CPoint() {}

	/**
	 * Constructor given X and Y coordinates.
	 * @param type the type of the control point
	 * @param x the position along the x access.
	 * @param y the position along the y access.
	 * @exception IllegalArgumentException the type is not appropriate
	 *            for a constructor providing x and y coordinates.
	 */
	public CPoint(CPointType type, double x, double y)
	    throws IllegalArgumentException
	{
	    if (type == CPointType.CLOSE || type == CPointType.SPLINE_FUNCTION)
		throw new IllegalArgumentException(errorMsg("wrongType", type));
	    this.type = type;
	    this.x = x;
	    this.y = y;
	}

	/**
	 * Constructor for the SPLINE_FUNCTION case using instances of
	 * NamedFunctionOps.
	 * This constructor is provided for use by various subclasses
	 * of {@link org.bzdev.obnaming.NamedObjectFactory} as the last
	 * argument of a factory's set or add method in most cases cannot be an
	 * an instance of {@link org.bzdev.math.RealValuedFunction}.
	 * @param xfOps the object providing the function for the x coordinates
	 * @param yfOps the object providing the function for the y coordinates
	 * @param t1 the initial value for the range of values at which
	 *        the function will be evaluated
	 * @param t2 the final value for the range of values at which the
	 *        function will be evaluated
	 * @param n the number of segments
	 * @exception IllegalArgumentException a function was null or
	 *            n was not positive
	 */
	private CPoint(NamedFunctionOps xfOps, NamedFunctionOps yfOps,
		      double t1, double t2, int n)
	    throws IllegalArgumentException
	{
	    if (xfOps == null || yfOps == null) {
		throw new
		    IllegalArgumentException(errorMsg("nullNamedFunctionOps"));
	    }
	    if (n < 1) {
		throw new IllegalArgumentException(errorMsg("nNotPositive", n));
	    }
	    this.type = CPointType.SPLINE_FUNCTION;
	    this.xfOps = xfOps;
	    this.yfOps = yfOps;
	    this.t1 = t1;
	    this.t2 = t2;
	    this.n = n;
	}

	/**
	 * Constructor for the <CODE>SPINE_FUNCTION</CODE> case.
	 * The functions will be converted to instances of NamedFunctionOps,
	 * if necessary, for consistency with the use of this class by
	 * {@link org.bzdev.obnaming.NamedObjectFactory named object factories}.
	 * <P>
	 * @param xf the function for the x coordinates
	 * @param yf the function for the y coordinates
	 * @param t1 an end for the range of values at which  the function
	 *        will be evaluated
	 * @param t2 an end for the range of values at which  the function
	 *        will be evaluated
	 * @param n the number of segments
	 * @exception IllegalArgumentException a function was null or
	 *            n was not positive
	 */

	public CPoint(RealValuedFunctOps xf, RealValuedFunctOps yf,
		      double t1, double t2, int n)
	    throws IllegalArgumentException
	{
	    this(asNamedFunctionOps(xf), asNamedFunctionOps(yf), t1, t2, n);
	}

	/**
	 * Constructor given a point.
	 * @param type the type of the control point
	 * @param point the point at which the control point is located
	 * @exception IllegalArgumentException the type is not appropriate
	 *            for a constructor providing a point as an argument.
	 */
	public CPoint(CPointType type, Point2D point)
	    throws IllegalArgumentException
	{
	    if (type == CPointType.CLOSE || type == CPointType.SPLINE_FUNCTION)
		throw new IllegalArgumentException(errorMsg("wrongType", type));
	    this.type = type;
	    this.x = point.getX();
	    this.y = point.getY();
	}


	/**
	 * Constructor given a type.
	 * The x and y coordinates will be zero.  This form is useful
	 * primarily then the CPointType is
	 * <CODE>CLOSE</CODE>. Otherwise one should also set the
	 * public fields for this object appropriate for the given
	 * type.
	 * @param type the control point type.
	 */
	public CPoint(CPointType type) {
	    this.type = type;
	}

	/**
	 * Constructor given X and Y coordinates, and an affine
	 * transformation.
	 * @param type the type of the control point
	 * @param x the position along the x access.
	 * @param y the position along the y access.
	 * @param af an affine transform to apply to cpoint
	 */
	public CPoint(CPointType type, double x, double y, AffineTransform af) {
	    this.type = type;
	    if (af == null) {
		this.x = x;
		this.y = y;
	    }
	    double[] matrix1 = new double[6];
	    af.getMatrix(matrix1);
	    this.x = matrix1[0]*x + matrix1[2]*y + matrix1[4];
	    this.y = matrix1[1]*x + matrix1[3]*y + matrix1[5];
	}

	/**
	 * Constructor for the SPLINE_FUNCTION case using instances of
	 * NamedFunctionOps with an affine transformation applied to the
	 * values of the functions.
	 * This constructor is provided for use by various subclasses
	 * of {@link org.bzdev.obnaming.NamedObjectFactory} as the last
	 * argument of a factory's set or add method  in most cases cannot be an
	 * an instance of {@link org.bzdev.math.RealValuedFunction}.
	 * @param xfOps the object providing the function for the x coordinates
	 * @param yfOps the object providing the function for the y coordinates
	 * @param t1 the initial value for the range of values at which
	 *        the function will be evaluated
	 * @param t2 the final value for the range of values at which the
	 *        function will be evaluated
	 * @param n the number of segments
	 * @param af the affine transformation
	 * @exception IllegalArgumentException a function was null or
	 *            n was not positive
	 */
	private CPoint(NamedFunctionOps xfOps, NamedFunctionOps yfOps,
		      double t1, double t2, int n,
		      AffineTransform af)
	{
	    if (xfOps == null || yfOps == null) {
		throw new
		    IllegalArgumentException(errorMsg("nullNamedFunctionOps"));
	    }
	    if (n < 1) {
		throw new IllegalArgumentException(errorMsg("nNotPositive", n));
	    }
	    this.type = CPointType.SPLINE_FUNCTION;
	    this.t1 = t1;
	    this.t2 = t2;
	    this.n = n;
	    if (af == null) {
		this.xfOps = xfOps;
		this.yfOps = yfOps;
	    } else {
		final double[] matrix2 = new double[6];
		af.getMatrix(matrix2);
		final RealValuedFunction cfx = xfOps.getFunction();
		final RealValuedFunction cfy = yfOps.getFunction();
		RealValuedFunctOps fx = (u) -> {
		    return matrix2[0]*cfx.valueAt(u)
		    + matrix2[2]*cfy.valueAt(u) + matrix2[4];
		};
		RealValuedFunctOps fy = (u) -> {
		    return matrix2[1]*cfx.valueAt(u)
		    + matrix2[3]*cfy.valueAt(u) + matrix2[5];
		};
		this.xfOps = new NamedFunctionOpsAdapter(fx);
		this.yfOps = new NamedFunctionOpsAdapter(fy);
	    }
	}

	/**
	 * Constructor for the <CODE>SPINE_FUNCTION</CODE> case with
	 * an affine transformation applies to values computed by the
	 * functions.
	 * The functions will be converted to instances of NamedFunctionOps,
	 * if necessary, for consistency with the use of this class by
	 * {@link org.bzdev.obnaming.NamedObjectFactory named object factories}.
	 * @param xf the function for the x coordinates
	 * @param yf the function for the y coordinates
	 * @param t1 an end for the range of values at which  the function
	 *        will be evaluated
	 * @param t2 an end for the range of values at which  the function
	 *        will be evaluated
	 * @param n the number of segments
	 * @param af the affine transformation
	 * @exception IllegalArgumentException a function was null or
	 *            n was not positive
	 */

	public CPoint(RealValuedFunctOps xf, RealValuedFunctOps yf,
		      double t1, double t2, int n,
		      AffineTransform af)
	    throws IllegalArgumentException
	{
	    this(asNamedFunctionOps(xf), asNamedFunctionOps(yf), t1, t2, n, af);
	}


	/**
	 * Get the type of the control point.
	 * @return the type of the control point
	 */
	CPointType getType() {return type;}

	/**
	 * Get the X coordinate of the control point.
	 * @return the X coordinate
	 */
	double getX() {return x;}

	/**
	 * Get the Y coordinate of the control point.
	 * @return the Y coordinate
	 */
	double getY() {return y;}

	RealValuedFunction getFX() {
	    return xfOps.getFunction();
	}

	RealValuedFunction getFY() {
	    return yfOps.getFunction();
	}

	double getT1() {
	    return t1;
	}
	double getT2() {
	    return t2;
	}

	int getN() {
	    return n;
	}
    }

    // implement this one so that javadocs shows the actual type
    // instead of a type parameter
    /**
     * Get the path that was build.
     * @return the path
     */
    public SplinePath2D getPath() {
	return super.getPath();
    }

    // Following two needed because these are abstract methods
    // in our superclass.

    SplinePath2D newSplinePath2D() {
	return new SplinePath2D();
    }

    SplinePath2D newSplinePath2D(int rule) {
	return new SplinePath2D(rule);
    }
}

//  LocalWords:  PREV subpath SplinePathBuilder scrunner WindingRule
//  LocalWords:  CPointType sublists BLOCKQUOTE PRE pathspec pb epts
//  LocalWords:  getPath Enum CPoint nullFunction ul subsequence xf
//  LocalWords:  AbstractSplinePathBuilder yf NamedFunctionOps xfOps
//  LocalWords:  IllegalArgumentException wrongType subclasses yfOps
//  LocalWords:  nullNamedFunctionOps nNotPositive javadocs exbundle
//  LocalWords:  superclass SplinePath IMG SRC li SEG eacute zier af
//  LocalWords:   CPoint's affine cpoint imgBackground UML
//  LocalWords:  RealFunctionOps
