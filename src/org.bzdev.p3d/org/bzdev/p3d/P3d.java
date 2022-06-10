package org.bzdev.p3d;
import java.io.IOException;
import java.util.List;
import java.awt.Color;

/**
 * Methods for common cases that occur when configuring an instance of
 * Model3D or when reporting errors.  These methods are all static
 * and are provided for convenience.
 */
public class P3d {

    /**
     * Class containing static methods for adding rectangles.
     */
    public static class Rectangle {

	/**
	 * Add a rectangle whose sides are vertical.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(x1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the counterclockwise direction
	 * when viewed from the direction the normal vector points and
	 * corresponding to the use of a right-hand rule for determining
	 * the direction of the normal vector.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addV(M m3d,
		  double x1, double y1, double z1,
		  double x3, double y3, double z3)
	{

	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x1, y1, z3);
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x3, y3, z1);
	}

	/**
	 * Add a "flipped" rectangle whose sides are either vertical.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(z1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the clockwise direction when viewed
	 * from the direction the normal vector points.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedV(M m3d,
			     double x1, double y1, double z1,
			     double x3, double y3, double z3)
	{
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x1, y1, z3);
	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x3, y3, z1);
	}


	/**
	 * Add a rectangle whose sides are horizontal.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.  The order in which the
	 * vertices are traversed is
	 * <blockquote>
	 * (x1, y1, z)----&gt;(x3,y1,z)----&gt;(x3,y3,z)----&gt;(x1,y3,z)
	 * </blockquote>
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addH(M m3d, double z,
		      double x1, double y1,
		      double x3, double y3)
	{
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x1, y3, z);
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x3, y1, z);
	}

	/**
	 * Add a "flipped" rectangle whose sides are horizontal.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedH(M m3d, double z,
			     double x1, double y1,
			     double x3, double y3)
	{
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x1, y3, z);
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x3, y1, z);
	}

	/**
	 * Add a rectangle whose sides are vertical, given a Color.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(x1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the counterclockwise direction
	 * when viewed from the direction the normal vector points and
	 * corresponding to the use of a right-hand rule for determining
	 * the direction of the normal vector.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addV(M m3d,
		      double x1, double y1, double z1,
		      double x3, double y3, double z3,
		      Color color)
	{

	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x1, y1, z3, color);
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x3, y3, z1, color);
	}

	/**
	 * Add a "flipped" rectangle whose sides are either vertical,
	 * given a Color.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(z1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the clockwise direction when viewed
	 * from the direction the normal vector points.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedV(M m3d,
			     double x1, double y1, double z1,
			     double x3, double y3, double z3,
			     Color color)
	{
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x1, y1, z3, color);
	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x3, y3, z1, color);
	}


	/**
	 * Add a rectangle whose sides are horizontal, given a Color.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.  The order in which the
	 * vertices are traversed is
	 * <blockquote>
	 * (x1, y1, z)----&gt;(x3,y1,z)----&gt;(x3,y3,z)----&gt;(x1,y3,z)
	 * </blockquote>
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addH(M m3d, double z,
		      double x1, double y1,
		      double x3, double y3,
		      Color color)
	{
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x1, y3, z, color);
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x3, y1, z, color);
	}

	/**
	 * Add a "flipped" rectangle whose sides are horizontal,
	 * given a Color.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedH(M m3d, double z,
			     double x1, double y1,
			     double x3, double y3,
			     Color color)
	{
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x1, y3, z, color);
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x3, y1, z, color);
	}


	/**
	 * Add a rectangle whose sides are vertical, given a color and tag.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(x1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the counterclockwise direction
	 * when viewed from the direction the normal vector points and
	 * corresponding to the use of a right-hand rule for determining
	 * the direction of the normal vector.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 * @param tag an Object naming an instance of a tag
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addV(M m3d,
		      double x1, double y1, double z1,
		      double x3, double y3, double z3,
		      Color color, Object tag)
	{

	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x1, y1, z3, color, tag);
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x3, y3, z1, color, tag);
	}

	/**
	 * Add a "flipped" rectangle whose sides are either vertical,
	 * given a color and tag.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * The order of the vertices is
	 * <blockquote>
	 * (x1,y1,z1)----&gt;(x3,y3,z1)----&gt;(x3,y3,z3)----&gt;(z1,y1,z3),
	 * </blockquote>
	 * circling the normal vector in the clockwise direction when viewed
	 * from the direction the normal vector points.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param z1 the z coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param z3 the z coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 * @param tag an Object naming an instance of a tag
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedV(M m3d,
			     double x1, double y1, double z1,
			     double x3, double y3, double z3,
			     Color color, Object tag)
	{
	    m3d.addTriangle(x3, y3, z3, x1, y1, z1, x1, y1, z3, color, tag);
	    m3d.addTriangle(x1, y1, z1, x3, y3, z3, x3, y3, z1, color, tag);
	}


	/**
	 * Add a rectangle whose sides are horizontal, given a color and tag.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * outside of a closed manifold.  The order in which the
	 * vertices are traversed is
	 * <blockquote>
	 * (x1, y1, z)----&gt;(x3,y1,z)----&gt;(x3,y3,z)----&gt;(x1,y3,z)
	 * </blockquote>
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 * @param tag an Object naming an instance of a tag
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addH(M m3d, double z,
		      double x1, double y1,
		      double x3, double y3,
		      Color color, Object tag)
	{
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x1, y3, z, color, tag);
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x3, y1, z, color, tag);
	}

	/**
	 * Add a "flipped" rectangle whose sides are horizontal,
	 * given a color and tag.
	 * The vertices are numbered 1 to 4, traversed counterclockwise
	 * when looking at the side of the rectangle that will be on the
	 * inside of a closed manifold.
	 * <P>
	 * Flipped rectangles are useful when paired with one that is not
	 * flipped, as most of the arguments will be the same.
	 * @param m3d the model to which the rectangle should be added.
	 * @param z the z coordinate of the rectangle's vertices.
	 * @param x1 the x coordinate of the first vertex.
	 * @param y1 the y coordinate of the first vertex.
	 * @param x3 the x coordinate of the third vertex.
	 * @param y3 the y coordinate of the third vertex.
	 * @param color the color for the rectangle; null if none is specified
	 * @param tag an Object naming an instance of a tag
	 */
	public static <T extends Model3DOps.Triangle,M extends Model3DOps<T>>
	    void addFlippedH(M m3d, double z,
			     double x1, double y1,
			     double x3, double y3,
			     Color color, Object tag)
	{
	    m3d.addTriangle(x3, y3, z, x1, y1, z, x1, y3, z, color, tag);
	    m3d.addTriangle(x1, y1, z, x3, y3, z, x3, y1, z, color, tag);
	}
    }



    /**
     * Print a tag used to label Model3D triangles.
     * @param out the output
     * @param prefix a prefix to print at the start of a line
     * @param tag the tag itself
     * @exception IOException an I/O error occurred
     */
    public static void printTag(Appendable out, String prefix, Object tag)
	throws IOException
    {
	if (prefix == null) prefix = "";
	if (tag instanceof String) {
	    String s = (String) tag;
	    out.append(prefix + s.toString() + "\n");
	} else if (tag instanceof StackTraceElement[]) {
	    StackTraceElement[] array = (StackTraceElement[]) tag;
	    for (StackTraceElement e: array) {
		out.append(prefix + e.toString() + "\n");
	    }
	} else {
	    out.append(prefix + tag.toString() + "\n");
	}
    }


    /**
     * Print errors given a list of triangles.
     * @param out the output
     * @param tlist a list of triangles that caused an error
     * @exception IOException an I/O error occurred
     */
    public static void printTriangleErrors(Appendable out,
					   List<Model3D.Triangle> tlist)
	throws IOException
    {
	if (tlist != null) {
	    for (Model3D.Triangle triangle: tlist) {
		if (triangle.entryNumber != -1) {
		    if (triangle.isPatch()) {
			out.append
			    (String.format
			     ("Cubic Triangle %d: "
			      + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			      triangle.entryNumber,
			      triangle.getX1(), triangle.getY1(),
			      triangle.getZ1(),
			      triangle.getX2(), triangle.getY2(),
			      triangle.getZ2(),
			      triangle.getX3(), triangle.getY3(),
			      triangle.getZ3(),
			      triangle.getX4(), triangle.getY4(),
			      triangle.getZ4()));
		    } else {
			out.append
			    (String.format
			     ("Cubic Triangle: "
			      + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			      triangle.getX1(), triangle.getY1(),
			      triangle.getZ1(),
			      triangle.getX2(), triangle.getY2(),
			      triangle.getZ2(),
			      triangle.getX3(), triangle.getY3(),
			      triangle.getZ3()));
		    }
		} else {
		    out.append
			(String.format
			 ("Planar Triangle (%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  triangle.getX1(), triangle.getY1(), triangle.getZ1(),
			  triangle.getX2(), triangle.getY2(), triangle.getZ2(),
			  triangle.getX3(), triangle.getY3(), triangle.getZ3()
			  ));
		}
		Object tag = triangle.getTag();
		if (tag != null) {
		    if (tag instanceof StackTraceElement[]) {
			out.append("  StackTrace for triangle/patch "
				   + "creation:\n");
			printTag(out, "    ", tag);
		    } else {
			printTag(out, "  tag for triangle/patch creation: ",
				 tag);
		    }
		}
	    }
	}
    }


    /**
     * Print errors given a list of edges.
     * @param out the output
     * @param elist a list of edges that caused an error
     * @exception IOException an I/O error occurred
     */
    public static void printEdgeErrors(Appendable out,
				       List<Model3D.Edge> elist)
	throws IOException
    {
	if (elist != null) {
	    for (Model3D.Edge edge: elist) {
		Object tag = edge.getTag();
		out.append(String.format
			   ("Edge (%g,%g,%g)-->(%g,%g,%g)\n",
			    edge.getX1(), edge.getY1(), edge.getZ1(),
			    edge.getX2(), edge.getY2(), edge.getZ2()));
		if (tag != null) {
		    if (tag instanceof StackTraceElement[]) {
			out.append("  StackTrace for triangle/patch "
				   + "creation:\n");
			printTag(out, "    ", tag);
		    } else {
			printTag(out, "  tag for triangle/patch creation: ",
				 tag);
		    }
		}
	    }
	}
    }

    /**
     * Print stack traces associated with an instance of
     * {@link SteppedGrid.Builder}.
     * If {@link SteppedGrid.Builder#create()}
     * or {@link SteppedGrid.Builder#create(Model3DOps)}
     * throws an exception, the exception does not indicate directly
     * which calls were responsible. This method will print the stack
     * traces for those calls (typically addRectangle or addRectangels
     * methods).
     * @param out the output appendable
     * @param prefix a prefix that starts each line
     * @param builder the {@link SteppedGrid.Builder} that failed
     */
    public static void printSteppedGridBuilderCalls(Appendable out,
						   String prefix,
						   SteppedGrid.Builder builder)
	throws IOException
    {
	if (prefix == null) prefix = "";
	String indent = prefix + "    ";
	StackTraceElement[] stackTrace = builder.getUpperTrace();
	if (stackTrace != null) {
	    out.append(prefix + "Call that created upper surface:\n");
	    for (StackTraceElement e: stackTrace) {
		out.append(indent + e.toString() + "\n");
	    }
	}

	stackTrace = builder.getLowerTrace();
	if (stackTrace != null) {
	    out.append(prefix + "Call that created lower surface:\n");
	    for (StackTraceElement e: stackTrace) {
		out.append(indent + e.toString() + "\n");
	    }
	}
    }
}

//  LocalWords:  vertices blockquote IOException tlist StackTrace
//  LocalWords:  elist
