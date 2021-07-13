package org.bzdev.geom;
import org.bzdev.scripting.ScriptingContext;

/**
 * BasicSplinePath2D builder.
 * This class allows a BasicSplinePath2D to be specified via a table - an
 * array of entries, each of which specifies a point along the path
 * (including control points) and the type of line segment.
 * The sequence of points, based on their types, is shown in
 * the following diagram, where a transition corresponds to adding a
 * new point to the BasicSplinePath2D builder:
 * <P>
 * <P style="text-align:center">
 * <IMG SRC="doc-files/basicbuilder.png">
 * <P>
 * The line colors are not significant - they are just used to
 * make it obvious that lines cross rather than join. Bidirectional
 * arrows denote pairs of transitions (essentially two unidirectional lines
 * that overlap).
 * <P>
 * The sequence of allowed transitions and their effects are as follows:
 * <UL>
 *    <li> The first point's type is either <code>MOVE_TO</code> or
 *         <code>MOVE_TO_NEXT</code>. Only a single point of this type
 *         is allowed.
 *    <li> A segment containing only <code>CONTROL</code> points (0, 1,
 *         or 2) must end with a <CODE>SEG_END</CODE> point. These
 *         represent straight lines, quadratic B&eacute;zier curves, and
 *         cubic B&eacute;zier curves respectively. The control points
 *         must be preceded by a point whose type is <code>MOVETO</code>,
 *         <code>SEG_END</code> or <code>SEG_END_PREV</code>.
 *     <li> An open spline segment starts with either the end of another
 *          segment or a point whose type is either <code>MOVE_TO</code>
 *          or <code>MOVE_TO_NEXT</code>. An open spline ends
 *         with a point whose type is <code>SEG_END</code> or
 *         <code>SEG_END_PREV</code>, and contains points (at least one)
 *         whose types are either <code>SPLINE</code> or
 *         <code>SPLINE_FUNCTION</code>.
 *    <li> A closed subpath is indicated by a point whose type is
 *         <code>CLOSE</code>. A <code>BasicSplinePathBUilder</code>
 *         allows at most one of these points, and no point may follow
 *         a point whose type is <code>CLOSE</code>.  If this point is
 *         preceded by a point of type <code>SEG_END</code> or
 *         <code>SEG_END_PREV</code>, a straight line (unless the line
 *         will have a length of zero) will connect the previous point
 *         to the point specified by the last point whose type is
 *         <code>MOVE_TO</code> or <code><MOVE_TO_NEXT></code>. If a
 *         sequence of <code>SPLINE</code> or
 *         <code>SPLINE_FUNCTION</code> points is terminated by a
 *         <code>CLOSE</code> point, the point immediately before the
 *         sequence must have a type of <code>MOVE_TO</code> or
 *         <code>MOVE_TO_NEXT</code>, and a closed spline will be
 *         generated consisting of this initial point and the spline
 *         points.
 * </UL>
 * <P>
 * There more more constraints on the sequence of points than for
 * the class {@link SplinePathBuilder} because a
 * {@link BasicSplinePath2D} does not allow discontinuous paths, and closed
 * paths must be cyclic so that the path parameter is unbounded both from
 * above and below.  These paths are provided to support cases where one
 * uses a path to describe the motion of an object, not just for drawing.
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
 *       <CODE>"MOVE_TO"</CODE>, <CODE>"SEG_END"</CODE>,
 *       <CODE>"CONTROL"</CODE>, or<CODE>"SPLINE"</CODE>. Alternatively
 *       it can a constant whose type is {@link SplinePathBuilder.CPointType}.
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
 * <BLOCKQUOTE><CODE><PRE>
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
 * </PRE></CODE></BLOCKQUOTE>
 *  The program <CODE>epts</CODE> can generate the path specifications while
 *  providing a graphical user interface to aid in constructing the
 *  paths.
 */
public class BasicSplinePathBuilder 
    extends AbstractSplinePathBuilder<BasicSplinePath2D>
{



    // implement this one so that javadocs shows the actual type
    // instead of a type parameter
    /**
     * Get the path that was build.
     * @return the path
     */
    public BasicSplinePath2D getPath() {
	return super.getPath();
    }

    /**
     * Constructor.
     */
    public BasicSplinePathBuilder() {
	super();
	setBasicMode();
    }

    /**
     * Constructor providing a scripting context.
     * @param parent the scripting context used to support scripting
     */
    public BasicSplinePathBuilder(ScriptingContext parent) {
	super(parent);
	setBasicMode();
    }


    // Following two needed because these are abstract methods
    // in our superclass.

    BasicSplinePath2D newSplinePath2D() {
	return new BasicSplinePath2D();
    }

    BasicSplinePath2D newSplinePath2D(int rule) {
	return new BasicSplinePath2D(rule);
    }
}

//  LocalWords:  BasicSplinePath IMG SRC li SEG eacute zier MOVETO pb
//  LocalWords:  PREV subpath BasicSplinePathBUilder scrunner PRE
//  LocalWords:  SplinePathBuilder WindingRule CPointType sublists
//  LocalWords:  BLOCKQUOTE pathspec getPath epts
