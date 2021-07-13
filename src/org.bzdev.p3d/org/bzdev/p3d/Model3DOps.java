package org.bzdev.p3d;
import org.bzdev.geom.Shape3D;
import java.awt.Color;

/**
 * Class providing basic operations for a Model3D.
 * The operations included are ones needed to add objects
 * to a model.
 */
public interface Model3DOps <T extends Model3DOps.Triangle>
    extends Shape3D
{
    /**
     * Add an existing model to this model.
     * @param m3d the model to add
     */
    void addModel(Model3DOps<?> m3d);


    /**
     * Add an existing model to this model, providing a tag.
     * @param m3d the model to add
     * @param tag the tag naming the objects in this model
     */
    void addModel(Model3DOps<?> m3d, Object tag);

    /**
     * Add a triangle to the model.
     * The triangle that is stored may differ from the triangle
     * passed as this method's argument: while the type may be different,
     * some classes that implement Model3DOps can apply transformations
     * and will need to create a different triangle as a result.
     * @param triangle the triangle to add
     * @return the triangle stored by this model
     */
    T addTriangle(Model3DOps.Triangle triangle);

    /**
     * Add a triangle to the model, specifying the triangle by its vertices.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
     T addTriangle(double x1, double y1, double z1,
		   double x2, double y2, double z2,
		   double x3, double y3, double z3)
	throws IllegalArgumentException;


    /**
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    T addFlippedTriangle(double x1, double y1, double z1,
			 double x2, double y2, double z2,
			 double x3, double y3, double z3)
	throws IllegalArgumentException;

    /**
     * Add a triangle to the model, specifying the triangle by its vertices
     * and its color.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    T addTriangle(double x1, double y1, double z1,
		  double x2, double y2, double z2,
		  double x3, double y3, double z3,
		  Color color)
	throws IllegalArgumentException;

    /**
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices and color.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    T addFlippedTriangle(double x1, double y1, double z1,
			 double x2, double y2, double z2,
			 double x3, double y3, double z3,
			 Color color)
	throws IllegalArgumentException;

    /**
     * Add a triangle to the model, specifying the triangle by its vertices
     * and its color, also specifying a tag.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @param tag the tag; null if there is none
     * @return the triangle stored by this model
     * @exception IllegalArgumentException a tag was a triangle or
     *            one of the first 9 arguments had the value
     *            Double.NaN
     */
   T addTriangle(double x1, double y1, double z1,
		 double x2, double y2, double z2,
		 double x3, double y3, double z3,
		 Color color, Object tag)
	throws IllegalArgumentException;

   /**
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices and color, also specifying a tag.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @param tag the tag; null if there is none
     * @return the triangle stored by this model
     * @exception IllegalArgumentException a tag was a triangle or
     *            one of the first 9 arguments had the value
     *            Double.NaN
     */
    T addFlippedTriangle(double x1, double y1, double z1,
			 double x2, double y2, double z2,
			 double x3, double y3, double z3,
			 Color color, Object tag)
	throws IllegalArgumentException;

    /**
     * Interface providing basic operations for triangles.
     */
    public static interface Triangle {

	/**
	 * Get the x coordinate of a triangle's first vertex.
	 * @return the x coordinate
	 */
	double getX1();

	/**
	 * Get the y coordinate of a triangle's first vertex.
	 * @return the y coordinate
	 */
	double getY1();

	/**
	 * Get the z coordinate of a triangle's first vertex.
	 * @return the z coordinate
	 */
	double getZ1();


	/**
	 * Get the x coordinate of a triangle's second vertex.
	 * @return the x coordinate
	 */
	double getX2();

	/**
	 * Get the y coordinate of a triangle's second vertex.
	 * @return the y coordinate
	 */
	double getY2();


	/**
	 * Get the z coordinate of a triangle's second vertex.
	 * @return the z coordinate
	 */
	double getZ2();


	/**
	 * Get the x coordinate of a triangle's third vertex.
	 * @return the x coordinate
	 */
	double getX3();

	/**
	 * Get the y coordinate of a triangle's third vertex.
	 * @return the y coordinate
	 */
	double getY3();


	/**
	 * Get the z coordinate of a triangle's third vertex.
	 * @return the z coordinate
	 */
	double getZ3();
 
	/**
	 *Get the triangle's color.
	 * @return the color of the triangle; null if there is none.
	 */
	Color getColor();


	/**
	 * Get the tag associated with a triangle.
	 * @return the triangle's tag; null if there is none
	 */
	Object getTag();
   }
}
//  LocalWords:  DOps IllegalArgumentException NaN
