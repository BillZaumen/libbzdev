package org.bzdev.geom;
import org.bzdev.math.LUDecomp;
import org.bzdev.math.VectorOps;
import java.awt.geom.NoninvertibleTransformException;
import org.bzdev.util.Cloner;
import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * The AffineTransform3D class represents a 3D affine transform.
 * <script>
 * MathJax = {
 *   tex: {
 *      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *         src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * These
 * transforms are linear transforms represented by the following matrix
 * equation: $$\left (\begin{array}{c}x\prime \\ y\prime \\ z\prime \\ 1
 * \end{array}\right ) = \left (\begin{array}{cccc}
 * m_{00} &amp; m_{01} &amp; m_{02} &amp; m_{03} \\
 * m_{10} &amp; m_{11} &amp; m_{12} &amp; m_{13} \\
 * m_{20} &amp; m_{21} &amp; m_{22} &amp; m_{23} \\
 *  0 &amp; 0 &amp; 0 &amp; 1
 * \end{array}\right )$$
 * <NOSCRIPT>
 * <pre>
 * <code>
 *   _   _      _              _   _   _
 *  |  x' |    | m<sub>00</sub> m<sub>01</sub> m<sub>02</sub> m<sub>03</sub> | |  x  |
 *  |  y' |  = | m<sub>10</sub> m<sub>11</sub> m<sub>12</sub> m<sub>13</sub> | |  y  |
 *  |  z' |    | m<sub>20</sub> m<sub>21</sub> m<sub>22</sub> m<sub>23</sub> | |  z  |
 *  |_ 1 _|    |_ 0   0   0  1 _| |_ 1 _|
 * </code>
 * </pre>
 * </NOSCRIPT>
 * <P>
 * Affine Transformations can be constructed by concatenating a series of
 * translations, scaling transformation, rotations, and shears.  They can
 * also be constructed by providing the parameters m in the matrix above.
 * <P>
 * 90-degree rotations are handled the same way as the class
 * {@link java.awt.geom.AffineTransform} handles them. While there are some
 * differences due to the differences between 3D and 2D Euclidean spaces,
 * as far as possible this class was modeled after
 * {@link java.awt.geom.AffineTransform} in terms of the methods provided - both
 * their names and their signatures.
 * @see java.awt.geom.AffineTransform
 */
public class AffineTransform3D implements Transform3D, Cloneable {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    // keep as a flat matrix with constant terms suppressed.
    // The equivalent matrix is 3 by 4 as the last row of the 4 by 4
    // matrix is always the same.
    double[] matrix = new double[12];

    private static void unflatten(double[] flatmatrix, double[][] target) {
	for(int j = 0; j < 4; j++) {
	    for (int i = 0; i < 3; i++) {
		target[i][j] = flatmatrix[i+3*j];
	    }
	}
	target[3][3] = 1.0;
    }

    private static void flatten(double[][] m, double[] flatmatrix) {
	for(int j = 0; j < 4; j++) {
	    for (int i = 0; i < 3; i++) {
		flatmatrix[i+3*j] = m[i][j];
	    }
	}
    }

    static private final double identity[] = {
	1.0, 0.0, 0.0, 0.0,
	0.0, 1.0, 0.0, 0.0,
	0.0, 0.0, 1.0, 0.0
    };

    /**
     * Constructor.
     * The AffineTransform3D created will be an identity transform.
     */
    public AffineTransform3D() {
	for (int i = 0; i < 3; i++) {
	    matrix[4*i] = 1.0;
	}
    }

    // In some cases, the code called after the constructor
    // will fill in the non-zero fields for a matrix, so we
    // just return a matrix of all zeros when the flag zero is
    // true.  This is a private constructor because any
    // AffineTransform3D used must have the last element of
    // its matrix set to 1.0.
    private AffineTransform3D(boolean zero) {
	if (!zero) {
	    for (int i = 0; i < 3; i++) {
		matrix[i*4] = 1.0;
	    }
	} 
    }

    /**
     * Constructor that represents the same transform as another
     * transform.
     * @param Tx the transform to copy
     */
    public AffineTransform3D(AffineTransform3D Tx) {
	Tx.getMatrix(matrix);
    }

    /**
     * Constructor given a flat, double-precision matrix representing the transform.
     * The elements of the flat matrix are
     * {m<sub>00</sub>, m<sub>10</sub>, m<sub>20</sub>,
     * m<sub>01</sub>, m<sub>11</sub>, m<sub>21</sub>,
     * m<sub>02</sub>, m<sub>12</sub>, m<sub>22</sub>,
     * [m<sub>03</sub>, m<sub>13</sub>, m<sub>23</sub>]}
     * The last 3 are optional and are used when the length of the matrix
     * is 12 or more. Additional arguments are ignored.
     * @param flatmatrix the flattened matrix with non-varying elements
     *        removed
     */
    public AffineTransform3D(double[] flatmatrix) {
        int len = (flatmatrix.length < 12)? 9: 12;
	System.arraycopy(flatmatrix, 0, matrix, 0, len);
    }

    /**
     * Constructor given matrix elements in double precision.
     * @param m00 the matrix element m<sub>00</sub>
     * @param m10 the matrix element m<sub>10</sub>
     * @param m20 the matrix element m<sub>20</sub>
     * @param m01 the matrix element m<sub>01</sub>
     * @param m11 the matrix element m<sub>11</sub>
     * @param m21 the matrix element m<sub>21</sub>
     * @param m02 the matrix element m<sub>02</sub>
     * @param m12 the matrix element m<sub>12</sub>
     * @param m22 the matrix element m<sub>22</sub>
     * @param m03 the matrix element m<sub>03</sub>
     * @param m13 the matrix element m<sub>13</sub>
     * @param m23 the matrix element m<sub>23</sub>
     */
    public AffineTransform3D(double m00, double m10, double m20,
			     double m01, double m11, double m21,
			     double m02, double m12, double m22,
			     double m03, double m13, double m23)
    {
	matrix[0+3*0] = m00;
	matrix[1+3*0] = m10;
	matrix[2+3*0] = m20;
	matrix[0+3*1] = m01;
	matrix[1+3*1] = m11;
	matrix[2+3*1] = m21;
	matrix[0+3*2] = m02;
	matrix[1+3*2] = m12;
	matrix[2+3*2] = m22;
	matrix[0+3*3] = m03;
	matrix[1+3*3] = m13;
	matrix[2+3*3] = m23;
    }

    /**
     * Constructor given a flat, single-precision matrix representing the transform.
     * The elements of the flat matrix are
     * {m<sub>00</sub>, m<sub>10</sub>, m<sub>20</sub>,
     * m<sub>01</sub>, m<sub>11</sub>, m<sub>21</sub>,
     * m<sub>02</sub>, m<sub>12</sub>, m<sub>22</sub>,
     * [m<sub>03</sub>, m<sub>13</sub>, m<sub>23</sub>]}
     * The last 3 are optional and are used when the length of the matrix
     * is 12 or more. Additional arguments are ignored.
     * @param flatmatrix the flattened matrix with non-varying elements
     *        removed
     */
    public AffineTransform3D(float[] flatmatrix) {
        int len = (flatmatrix.length < 12)? 9: 12;
	for (int i = 0; i < len; i++) {
	    matrix[i] = (double)(flatmatrix[i]);
	}
    }

    /**
     * Constructor given matrix elements in single precision.
     * @param m00 the matrix element m<sub>00</sub>
     * @param m10 the matrix element m<sub>10</sub>
     * @param m20 the matrix element m<sub>20</sub>
     * @param m01 the matrix element m<sub>01</sub>
     * @param m11 the matrix element m<sub>11</sub>
     * @param m21 the matrix element m<sub>21</sub>
     * @param m02 the matrix element m<sub>02</sub>
     * @param m12 the matrix element m<sub>12</sub>
     * @param m22 the matrix element m<sub>22</sub>
     * @param m03 the matrix element m<sub>03</sub>
     * @param m13 the matrix element m<sub>13</sub>
     * @param m23 the matrix element m<sub>23</sub>
     */
    public AffineTransform3D(float m00, float m10, float m20,
			     float m01, float m11, float m21,
			     float m02, float m12, float m22,
			     float m03, float m13, float m23)
    {
	matrix[0+3*0] = m00;
	matrix[1+3*0] = m10;
	matrix[2+3*0] = m20;
	matrix[0+3*1] = m01;
	matrix[1+3*1] = m11;
	matrix[2+3*1] = m21;
	matrix[0+3*2] = m02;
	matrix[1+3*2] = m12;
	matrix[2+3*2] = m22;
	matrix[0+3*3] = m03;
	matrix[1+3*3] = m13;
	matrix[2+3*3] = m23;

    }

    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError();
	}
    }


    /**
     * Concatenates this transform with an AffineTransform3D.
     * Given the matrix representation M for this affine transform
     * and the matrix representation M<sub>T</sub> for the transform
     * Tx, this transform's new value is M M<sub>T</sub>.
     * @param Tx the affine transform to concatenate
     */
    public void concatenate(AffineTransform3D Tx) {
	double[] m = new double[12];
	double[] tm = new double[12];
	Tx.getMatrix(tm);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		for (int k = 0; k < 3; k++) {
		    m[i+3*j] += matrix[i+3*k] * tm[k+3*j];
		}
	    }
	    m[i+9] += matrix[i+9];
	}
	
	matrix = m;
	type = TYPE_UNKNOWN;
    }

    /**
     * Concatenates an AffineTransform3D with this affine transform.
     * Given the matrix representation M for this affine transform
     * and the matrix representation M<sub>T</sub> for the transform
     * Tx, this transform's new value is M<sub>T</sub> M.
     * @param Tx the affine transform to concatenate
     */
    public void preConcatenate(AffineTransform3D Tx) {
	// double[] m = new double[16];
	double[] m = new double[12];
	double[] tm = new double[12];
	Tx.getMatrix(tm);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		for (int k = 0; k < 3; k++) {
		    m[i+3*j] += tm[i+3*k] * matrix[k+3*j];
		}
	    }
	    m[i+9] += tm[i+9];
	}
	matrix = m;
	type = TYPE_UNKNOWN;
    }

    /**
     * Create an AffineTransform3D that is the inverse of this transform.
     * @return the inverse transform
     * @exception NoninvertibleTransformException an inverse cannot be
     *            computed
     */
    public AffineTransform3D createInverse()
	throws NoninvertibleTransformException
    {
	double[][] m = new double[4][4];
	unflatten(matrix, m);
	AffineTransform3D inverse = new AffineTransform3D(true);
	LUDecomp lud = new LUDecomp(m);
	try {
	    lud.getInverse(m);
	    flatten(m, inverse.matrix);
	    return inverse;
	} catch(IllegalStateException e) {
	    throw new java.awt.geom.NoninvertibleTransformException
		(e.getMessage());
	}
    }

    /**
     * Transform a sequence of vectors (directed line segments).
     * The translation is not applied as a translation is meaningful
     * for points.  Each vector is an ordered triplet
     * (v<sub>x</sub>,v<sub>y</sub>,v<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the vectors to transform
     * @param srcOff the offset int srcPts at which the
     *               first vector is stored
     * @param dstPts the transformed vectors
     * @param dstOff the offset in dstPts at which the first transformed
     *        vector will be stored
     * @param numPts the number of vectors
     */
    public void deltaTransform(double[]srcPts, int srcOff,
			       double[] dstPts, int dstOff,
			       int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    for (int i = 0; i < 3; i++) {
		dstPts[dstOff+i] = matrix[i+3*0]*srcPts[srcOff];
		dstPts[dstOff+i] += matrix[i+3*1]*srcPts[srcOff+1];
		dstPts[dstOff+i] += matrix[i+3*2] * srcPts[srcOff+2];
	    }
	    srcOff += 3;
	    dstOff += 3;
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof AffineTransform3D) {
	    AffineTransform3D Tx = (AffineTransform3D) obj;
	    double[] other = new double[12];
	    Tx.getMatrix(other);
	    for (int i = 0; i < 12; i++) {
		if (matrix[i] != other[i]) return false;
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Get the determinant of this AffineTransform3D's matrix
     * @return the determinant
     */
    public double getDeterminant() {
	double[][] m = new double[4][4];
	unflatten(matrix, m);
	LUDecomp lud = new LUDecomp(m);
	return lud.det();
    }

    /**
     * Get the elements of the matrix representing this AffineTransform3D,
     * with each element a double-precision number.
     * The elements are stored in row-major order:
     * <pre>
     *   {m<sub>00</sub>, m<sub>10</sub>, m<sub>20</sub>,
     *    m<sub>01</sub>, m<sub>11</sub>, m<sub>21</sub>,
     *    m<sub>02</sub>, m<sub>12</sub>, m<sub>22</sub>,
     *    m<sub>03</sub>, m<sub>13</sub>, m<sub>23</sub>}
     * Only nontrivial entries are included (a few are always 0 or 1).
     * @param flatMatrix an array that will hold the non-trivial
     * elements of the matrix
     */
    public void getMatrix(double flatMatrix[]) {
	System.arraycopy(matrix, 0, flatMatrix, 0, 12);
    }

    private static void
	setToRotation(double[] matrix, double phi, double theta, double psi)
    {
	// It is better to do this explicitly in the matrix equations
	// phi = -phi;
	// theta = -theta;
	// psi = -psi;
	double sin_phi = Math.sin(phi);
	double cos_phi = Math.cos(phi);
	if (sin_phi == 1.0 || sin_phi == -1.0) cos_phi = 0.0;
	if (cos_phi == 1.0 || cos_phi == -1.0) sin_phi = 0.0;

	double sin_theta = Math.sin(theta);
	double cos_theta = Math.cos(theta);
	if (sin_theta == 1.0 || sin_theta == -1.0) cos_theta = 0.0;
	if (cos_theta == 1.0 || cos_theta == -1.0) sin_theta = 0.0;
	double sin_psi = Math.sin(psi);
	double cos_psi = Math.cos(psi);
	if (sin_psi == 1.0 || sin_psi == -1.0) cos_psi = 0.0;
	if (cos_psi == 1.0 || cos_psi == -1.0) sin_psi = 0.0;

	// Eulerian angle version, where we rotate the coordinate system
	// matrix[0+3*0] = cos_psi * cos_phi - cos_theta * sin_phi* sin_psi;
	// matrix[0+3*1] = cos_psi * sin_phi + cos_theta * cos_phi * sin_psi;
	// matrix[0+3*2] = sin_theta * sin_psi;
	// matrix[1+3*0] = -sin_psi * cos_phi - cos_theta * sin_phi * cos_psi;
	// matrix[1+3*1] = -sin_psi * sin_phi + cos_theta * cos_phi * cos_psi;
	// matrix[1+3*2] = sin_theta * cos_psi;
	// matrix[2+3*0] = sin_theta * sin_phi;
	// matrix[2+3*1] = - sin_theta*cos_phi;
	// matrix[2+3*2] = cos_theta;

	// object version, where we rotate the object.  It is equivalent
	// to reversing the signs of all angles, so only those terms with
	// an odd number of sin functions actually change sign.
	matrix[0+3*0] = cos_psi * cos_phi - cos_theta * sin_phi* sin_psi;
	matrix[0+3*1] = -cos_psi * sin_phi - cos_theta * cos_phi * sin_psi;
	matrix[0+3*2] = sin_theta * sin_psi;
	matrix[1+3*0] = sin_psi * cos_phi + cos_theta * sin_phi * cos_psi;
	matrix[1+3*1] = -sin_psi * sin_phi + cos_theta * cos_phi * cos_psi;
	matrix[1+3*2] = -sin_theta * cos_psi;
	matrix[2+3*0] = sin_theta * sin_phi;
	matrix[2+3*1] = sin_theta*cos_phi;
	matrix[2+3*2] = cos_theta;
	for (int i = 0; i < 9; i++) {
	    if (matrix[i] == -0.0) matrix[i] = 0.0;
	}
	// clear any translations
	matrix[9] = 0.0;
	matrix[10] = 0.0;
	matrix[11] = 0.0;
    }

    private static void
	setToRotation(double[] matrix, double phi, double theta, double psi,
			  double anchorx, double anchory, double anchorz)
    {
	setToRotation(matrix, phi, theta, psi);
	double x = matrix[0]*anchorx + matrix[0+3]*anchory
	    + matrix[0+6]*anchorz;
	double y = matrix[1]*anchorx + matrix[1+3]*anchory
	    + matrix[1+6]*anchorz;
	double z = matrix[2]*anchorx + matrix[2+3]*anchory
	    + matrix[2+6]*anchorz;

	double tx = anchorx - x;
	double ty = anchory - y;
	double tz = anchorz - z;

	matrix[0+9] = tx;
	matrix[1+9] = ty;
	matrix[2+9] = tz;

    }

    /**
     * Create an AffineTransform3D that represents a rotation about
     * the point (0, 0, 0).
     * <P>
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     * @return an affine transformation representing a rotation about
     *         the point (0,0,0)
   */
    public static AffineTransform3D
	getRotateInstance(double phi, double theta, double psi)
    {
	AffineTransform3D result = new AffineTransform3D(true);
	setToRotation(result.matrix, phi, theta, psi);
	return result;
    }

    /**
     * Create an AffineTransform3D that represents a rotation about
     * an anchor point.
     * <P>
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * A translation will then be applied so that, after the
     * transform is applied, the anchor point (anchorx, anchory, anchorz)
     * will not move.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     * @param anchorx the x coordinate of the anchor point
     * @param anchory the y coordinate of the anchor point
     * @param anchorz the z coordinate of the anchor point
     * @return an affine transformation representing a rotation about
     *         the point (0,0,0)
     */
    public static  AffineTransform3D
	getRotateInstance(double phi, double theta, double psi,
			  double anchorx, double anchory, double anchorz)
    {
	AffineTransform3D result = new AffineTransform3D(true);
	setToRotation(result.matrix, phi, theta, psi,
		      anchorx, anchory, anchorz);
	return result;
    }

    private static void
	setToScale(double[] matrix, double sx, double sy, double sz)
    {
	matrix[0] = sx;
	matrix[4] = sy;
	matrix[8] = sz;

	matrix[1] = 0.0;
	matrix[2] = 0.0;
	matrix[3] = 0.0;

	matrix[5] = 0.0;
	matrix[6] = 0.0;
	matrix[7] = 0.0;

	matrix[9] = 0.0;
	matrix[10] = 0.0;
	matrix[11] = 0.0;
    }

    /**
     * Create an AffineTransform3D that will scale an object.
     * This transform will stretch an object by specified
     * amounts in the x, y, and z directions, with the point
     * (0,0,0) not moving.
     * @param sx the scaling factor in the x direction
     * @param sy the scaling factor in the y direction
     * @param sz the scaling factor in the z direction
     * @return the transform
     */
    public static AffineTransform3D
	getScaleInstance(double sx, double sy, double sz)
    {
	AffineTransform3D result = new AffineTransform3D(true);
	setToScale(result.matrix, sx, sy, sz);
	return result;
    }

    /**
     * Get the X coordinate scale factor m<sub>00</sub> for this transform.
     * @return the scale factor
     */
    public double getScaleX() {return matrix[0];}

    /**
     * Get the Y coordinate scale factor m<sub>11</sub> for this transform.
     * @return the scale factor
     */
    public double getScaleY() {return matrix[/*5*/4];}
   
    /**
     * Get the Z coordinate scale factor m<sub>22</sub> for this transform.
     * @return the scale factor
     */
    public double getScaleZ() {return matrix[/*10*/8];}

    private static void
	setToShear(double[] matrix,
		   double shxy, double shxz,
		   double shyx, double shyz,
		   double shzx, double shzy)
    {
	matrix[0] = 1.0;  // m00
	matrix[4] = 1.0;  // m11
	matrix[8] = 1.0;  // m22
	matrix[3] = shxy; // m01
	matrix[6] = shxz; // m02
	matrix[1] = shyx; // m10
	matrix[7] = shyz; // m12
	matrix[2] = shzx; // m20
	matrix[5] = shzy; // m21

	matrix[9] = 0.0;  // m03
	matrix[10] = 0.0; // m13
	matrix[11] = 0.0; // m23
    }
    
    /**
     * Create a new AffineTransform3D representing a shearing
     * transformation.
     * @param shxy the X coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>01</sub>
     * @param shxz the X coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>02</sub>
     * @param shyx the Y coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>10</sub>
     * @param shyz the Y coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>12</sub>
     * @param shzx the Z coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>20</sub>
     * @param shzy the Z coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>21</sub>
     * @return the new shearing transformation
     */
    public static AffineTransform3D
	getShearInstance(double shxy, double shxz,
			 double shyx, double shyz,
			 double shzx, double shzy)
    {
	AffineTransform3D result = new AffineTransform3D(true);
	setToShear(result.matrix, shxy, shxz, shyx, shyz, shzx, shzy);
	return result;
    }

    /**
     * Get the factor by which the X coordinate is shifted  when this
     * factor is multiplied by the Y coordinate.
     * @return the factor; equivalent to the value of m<sub>01</sub>
     */
    public double getShearXY() {return matrix[/*4*/3];}

    /**
     * Get the factor by which the X coordinate is shifted when this
     * factor is multiplied by the Z coordinate.
     * 
     * @return the factor; equivalent to the value of m<sub>02</sub>
     */
    public double getShearXZ() {return matrix[/*8*/6];}

    /**
     * Get the factor by which the Y coordinate is shifted when this
     * factor is multiplied by the X coordinate.
     * @return the factor; equivalent to the value of m<sub>10</sub>
     */
    public double getShearYX() {return matrix[1];}

    /**
     * Get the factor by which the Y coordinate is shifted when this
     * factor is multiplied by the Z coordinate.
     * @return the factor; equivalent to the value of m<sub>12</sub>
     */
    public double getShearYZ() {return matrix[/*9*/7];}

    /**
     * Get the factor by which the Z coordinate is shifted when this
     * factor is multiplied by the X coordinate.
     * @return the factor; equivalent to the value of m<sub>20</sub>
     */
    public double getShearZX() {return matrix[2];}

    /**
     * Get the factor by which the Z coordinate is shifted when this
     * factor is multiplied by the Y coordinate.
     * @return the factor; equivalent to the value of m<sub>21</sub>
     */
    public double getShearZY() {return matrix[/*6*/5];}


    private static void setToTranslation(double[] matrix,
					 double tx, double ty, double tz)
    {
	matrix[0] = 1.0;
	matrix[4] = 1.0;
	matrix[8] = 1.0;
	
	matrix[1] = 0.0;
	matrix[2] = 0.0;

	matrix[3] = 0.0;
	matrix[5] = 0.0;

	matrix[6] = 0.0;
	matrix[7] = 0.0;

	matrix[9] = tx;
	matrix[10] = ty;
	matrix[11] = tz;
    }
				       

    /**
     * Create a new AffineTransform3D representing a translation.
     * @param tx the translation in the X direction
     * @param ty the translation in the Y direction
     * @param tz the translation in the Z direction
     * @return the transform
     */
    public static AffineTransform3D
	getTranslateInstance(double tx, double ty, double tz)
    {
	AffineTransform3D result = new AffineTransform3D(true);
	setToTranslation(result.matrix, tx, ty, tz);
	return result;
    }

    /**
     * Get the translation in the X direction for this transform.
     * @return the translation; equivalent to m<sub>03</sub>
     */
    public double getTranslateX() {return matrix[9];}
    /**
     * Get the translation in the Y direction for this transform.
     * @return the translation; equivalent to m<sub>13</sub>
     */
    public double getTranslateY() {return matrix[10];}
    /**
     * Get the translation in the Z direction for this transform.
     * @return the translation; equivalent to m<sub>23</sub>
     */
    public double getTranslateZ() {return matrix[11];}


    @Override
    public int hashCode() {
	long bits = Double.doubleToLongBits(matrix[0]);
	for (int i = 1; i < 12; i++) {
	    bits ^= (bits * 31) ^ Double.doubleToLongBits(matrix[i]);
	}
	return (((int)bits)^((int)(bits>>32)));
    }


    /**
     * Apply the inverse of this transform to a sequence of points,
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     * @exception NoninvertibleTransformException this transform does
     *            not have an inverse
     */
    public void inverseTransform(double[] srcPts, int srcOff,
				 double[] dstPts, int dstOff,
				 int numPts)
	throws NoninvertibleTransformException
    {
	AffineTransform3D inverse = createInverse();
	inverse.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }


    /**
     * Apply the inverse of this transform to a point.
     * Note: ptSrc and ptDst can be the same point.
     * @param ptSrc the untransformed point
     * @param ptDst the point to be transformed (a new point will
     *        be created if the value is null)
     * @return the transformed point
     * @exception NoninvertibleTransformException this transform does
     *            not have an inverse
     */
    public Point3D inverseTransform(Point3D ptSrc, Point3D ptDst)
	throws NoninvertibleTransformException
    {
	double pts[] = {ptSrc.getX(), ptSrc.getY(), ptSrc.getZ(), 0.0, 0.0, 0.0};
	
	inverseTransform(pts, 0, pts, 3, 1);
	if (ptDst == null) {
	    try {
		ptDst = Cloner.makeClone(ptSrc);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError();
	    }
	}
	ptDst.setLocation(pts[3], pts[4], pts[5]);
	return ptDst;
    }

    /**
     * Replace this transform with its inverse.
     * @exception NoninvertibleTransformException an inverse does not exist
     */
    public void invert() throws NoninvertibleTransformException {
	double[][] m = new double[4][4];
	unflatten(matrix, m);
	LUDecomp lud = new LUDecomp(m);
	try {
	    lud.getInverse(m);
	    flatten(m, matrix);
	} catch(IllegalStateException e) {
	    throw new NoninvertibleTransformException(e.getMessage());
	}

    }

    /**
     * The transform is an identity transform.
     */
    public static final int TYPE_IDENTITY = 1;
    /**
     * The transform  will change an object's chirality when the
     * object has a chirality.
     */
    public static final int TYPE_FLIP_CHIRALITY = 2;
    /**
     * The transform is a rotation about some point for an
     * arbitrary angle.
     */
    public static final int TYPE_GENERAL_ROTATION = 4;

    /**
     * The transform is a rotation about some point by multiples
     * of 90 degrees for each Eulerian angle.
     */
    public static final int TYPE_QUADRANT_ROTATION = 8;

    /**
     * The transform is a scaling transformation, with different
     * scale factors in different directions. The scaling is
     * restricted to occur along the X, Y, or Z axes.
     */
    public static final int TYPE_SCALE = 16;
    /**
     * The transform is a scaling transformation, with the same
     * scale factor in each direction.
     */
    public static final int TYPE_UNIFORM_SCALE = 32;
    /**
     * The transform includes a translation so that the point
     * (0,0,0) will be moved by the transformation.
     */
    public static final int TYPE_TRANSLATION = 64;
    /**
     * The transform is a general one.
     */
    public static final int TYPE_GENERAL = 128;
    /**
     * The type of the transform is not currently known. This value
     * is used by {@link getType()} to determine when the type should
     * be recomputed.
     */
    private static final int TYPE_UNKNOWN = 256;


    /**
     * This field defines a mask for the two types used to
     * represent rotations.
     */
    public static final int TYPE_MASK_ROTATION =
	TYPE_GENERAL_ROTATION | TYPE_QUADRANT_ROTATION;

    /**
     * This field defines a mask for the two types used to represent
     * scaling transformations.
     */
    public static final int TYPE_MASK_SCALE = TYPE_SCALE | TYPE_UNIFORM_SCALE;


    int type = TYPE_UNKNOWN;

    /**
     * Get the type of the transformation.
     * The type of an AffineTransform3D is represented by an integer, with
     * specific bits indicating various features.  masks are provided to
     * denote useful combinations of bits.  The symbolic names for the bits
     * and their masks are constants associated with this class:
     * <UL>
     *   <LI> the value TYPE_IDENTITY is mutually exclusive with the other
     *        types and denotes the identity matrix.
     *   <LI> the value TYPE_TRANSLATION indicates that the transform includes
     *        a translation whose elements are not all zero.
     *   <LI> the value TYPE_MASK_ROTATION is a mask for two mutually exclusive
     *        types, both indicating rotations.  This should be used to find
     *        rotations of either type.
     *    <LI> the value TYPE_QUADRANT_ROTATION denotes rotations the exchange
     *         the x, y, and z axes in some way that does not involve a
     *         reflection. The tests that determine this type are dependent
     *         on floating-point accuracy, and are most likely to work for
     *         small angles (e.g. &pi;/2, not 10<sup>16</sup>&pi; + &pi;/2).
     *    <LI> the value TYPE_GENERAL_ROTATION includes all other rotations.
     *    <LI> the value TYPE_MASK_SCALE is a mask for two mutually exclusive
     *         scaling transformation.  This should be used to find scaling
     *         transformations of either type.
     *    <LI> the value TYPE_UNIFORM_SCALE indicates that the scaling is
     *         the same along the X, Y, and Z directions.
     *    <LI> the value TYPE_SCALE is used when the X, Y, and Z values are
     *         scaled.
     *    <LI> the value TYPE_GENERAL applies to all other transforms, including
     *         shears, which are not given any special designation.
     *    <LI> the value TYPE_FLIP_CHIRALITY apples to TYPE_UNIFORM_SCALE,
     *         TYPE_SCALE, and TYPE_GENERAL. TYPE_FLIP_CHIRALITY indicates
     *         a chirality change.  If an object is sufficiently
     *         asymmetric that there can be left-handed and
     *         right-handed versions of it, this bit indicates that a
     *         left-handed version will be turned into a right-handed
     *         version and vice versa.
     * </UL>
     * <P>
     * Note: Determining the type requires some computation, so the
     * implementation caches the results and clears the cache if
     * there is a change (or in a few cases, replaces it with the
     * new value when this can be trivially determined).
     * @return the type of the affine transformation as described above
     */
    public int getType() {
	if (type == TYPE_UNKNOWN) {
	    type = 0;
	    boolean noShearTerms = (matrix[1] == 0.0 && matrix[2] == 0.0
				    && matrix[3] == 0.0 && matrix[5] == 0.0
				    && matrix[6] == 0.0 && matrix[7] == 0.0);
	    // System.out.println("... no shear terms = " + noShearTerms);
	    boolean notRotNotShear = noShearTerms;

	    boolean mayBeIdent = false;
	    // Handle 90 degree rotation case
	    if (notRotNotShear) {
		if (matrix[0] == 1.0 && matrix[4] == 1.0
		    && matrix[8] == 1.0) {
		    // because identity aside from a translation, so
		    // it is not a rotation.
		    mayBeIdent = true;
		} else if ((Math.abs(matrix[0]) == 1.0)
			   && (Math.abs(matrix[4]) == 1.0)
			   && (Math.abs(matrix[8]) == 1.0)
			   && (matrix[0]*matrix[4]*matrix[8] > 0.0)) {
		    // Not a reflection so rotations of 90 degree multiples
		    // are left
		    notRotNotShear = false;
		}
	    }

	    boolean mayBeQuadrantRot = true;
	    int cnt = 0;
	    for (int i = 0; i < 9; i++) {
		if ((i > 0) && (i%3 == 0)) {
		    if (cnt != 1) break;
		    cnt = 0;
		}
		if (Math.abs(matrix[i]) == 1.0) {
		    cnt++;
		} else if (matrix[i] != 0.0) {
		    mayBeQuadrantRot = false;
		    break;
		}
	    }
	    if (cnt != 1) mayBeQuadrantRot = false;
	    /*
	    System.out.println("... mayBeQuadrantRot = " + mayBeQuadrantRot
			       + ", cnt = " + cnt);
	    */
	    boolean isTranslation =
		(matrix[9] != 0.0 || matrix[10] != 0.0 || matrix[11] != 0.0);

	    if (isTranslation) {
		// System.out.println("... type is a translation");
		type  |= TYPE_TRANSLATION;
	    }
	    // an identity transformation excludes rotations
	    // boolean mayBeIdent = (matrix[0] == 1.0 && matrix[/*5*/4] == 1.0
	    //			  && matrix[/*10*/8] == 1.0 && notRotNotShear);
	    if (!isTranslation && mayBeIdent) {
		type = TYPE_IDENTITY;
	    } else {
		// norms squared for transformed unit vectors
		double normtxsq = matrix[0]*matrix[0] + matrix[1]* matrix[1]
		    + matrix[2] * matrix[2];
		double normtysq = matrix[3]*matrix[3] + matrix[4]* matrix[4]
		    + matrix[5] * matrix[5];
		double normtzsq = matrix[6]*matrix[6] + matrix[7]* matrix[7]
		    + matrix[8] * matrix[8];

		boolean parallelTx = (float)(1.0 + matrix[1]) == 1.0F
		    && (float)(1.0 + matrix[2]) == 1.0F;
		boolean parallelTy = (float)(1.0 + matrix[3]) == 1.0F
		    && (float)(1.0 + matrix[5]) == 1.0F;
		boolean parallelTz = (float)(1.0 + matrix[6]) == 1.0F
		    && (float)(1.0 + matrix[7]) == 1.0F;
		boolean unitNormX = ((float)normtxsq) == 1.0F;
		boolean unitNormY = ((float)normtysq) == 1.0F;
		boolean unitNormZ = ((float)normtzsq) == 1.0F;

		boolean equalNormXY =
		    ((float)(1.0 + (normtxsq-normtysq))) == 1.0F;
		boolean equalNormXZ =
		    ((float)(1.0 + (normtxsq-normtzsq))) == 1.0F;

		// dot products of transformed unit vectors
		double dotTxTy = matrix[0]*matrix[3] + matrix[1]*matrix[4]
		    + matrix[2] * matrix[5];
		double dotTyTz = matrix[3]*matrix[6] + matrix[4]*matrix[7]
		    + matrix[5]*matrix[8];
		double dotTxTz = matrix[0]*matrix[6] + matrix[1]*matrix[7]
		    + matrix[2]*matrix[8];
	    
		boolean TxTyPerp = ((float)(1.0 + dotTxTy)) == 1.0F;
		boolean TyTzPerp = ((float)(1.0 + dotTxTz)) == 1.0F;
		boolean TxTzPerp = ((float)(1.0 + dotTxTz)) == 1.0F;

		// cross products of transformed unit vectors

		double cpTxTyX = matrix[1]*matrix[5] - matrix[4]*matrix[2];
		double cpTxTyY = -(matrix[0]*matrix[5] - matrix[3]* matrix[2]);
		double cpTxTyZ = matrix[0]*matrix[4] - matrix[3] * matrix[1];
		
		// sign of this cross product tells us if there was a parity
		// change.
		double cpxydotz = (cpTxTyX*matrix[6] + cpTxTyY*matrix[7]
				   + cpTxTyZ * matrix[8]);
		// System.out.println("... cpxydotz = " + cpxydotz);
		boolean flipped =  (cpxydotz < 0.0);

		if (unitNormX && unitNormY && unitNormZ) {
		    // System.out.println("... unit norms");
		    if (TxTyPerp && TyTzPerp && TxTzPerp) {
			// System.out.println("... transformed axes perpendicular");
			if (!mayBeIdent)
			    if (!flipped) {
				if (mayBeQuadrantRot) {
				    type |= TYPE_QUADRANT_ROTATION;
				} else {
				    type |= TYPE_GENERAL_ROTATION;
				}
			    } else if (noShearTerms) {
				type |= TYPE_SCALE;
			    } else {
				type |= TYPE_GENERAL;
			    }

		    } else {
			/*
			System.out.println("... transformed axes not "
					   + "perpendicular");
			*/
			type |= TYPE_GENERAL;
		    }
		} else {
		    // System.out.println("... not unit norms");
		    if (notRotNotShear) {
			// System.out.println("... scaling");
			if (equalNormXY && equalNormXZ) {
			    type |= TYPE_UNIFORM_SCALE;
			} else {
			    type |= TYPE_SCALE;
			}
		    } else {
			// System.out.println("... general");
			type |= TYPE_GENERAL;
		    }
		}

		if (flipped) {
		    // System.out.println("flipped");
		    type |= TYPE_FLIP_CHIRALITY;
		}
	    }
	}
	return type;
    }

    /**
     * Test if this AffineTransform3D is an identity transform.
     * @return true if this transform is an identity transform; false if not
     */
    public boolean isIdentity() {
	return (getType() == TYPE_IDENTITY);
    }

    /**
     * Concatenate this transform with a rotation about the point (0, 0, 0).
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     */
    public void rotate(double phi, double theta, double psi)
    {
	concatenate(AffineTransform3D.getRotateInstance(phi, theta, psi));
    }

    /**
     * Concatenate this transform with a rotation about an anchor point.
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     * @param anchorx the x coordinate of the anchor point
     * @param anchory the y coordinate of the anchor point
     * @param anchorz the z coordinate of the anchor point
     */
    public void rotate(double phi, double theta, double psi,
		       double anchorx, double anchory, double anchorz)
    {
	concatenate(AffineTransform3D.getRotateInstance
		    (phi, theta, psi, anchorx, anchory, anchorz));
    }

    /**
     * Concatenate this transform with a scaling transformation.
     * The scaling transform will stretch an object by specified
     * amounts in the x, y, and z directions, with the point
     * (0,0,0) not moving.
     * @param sx the scaling factor in the x direction
     * @param sy the scaling factor in the y direction
     * @param sz the scaling factor in the z direction
     */
    public void scale(double sx, double sy, double sz) {
	concatenate(getScaleInstance(sx, sy, sz));
    }

    /**
     * Set this transform to an identity transform.
     */
    public void setToIdentity() {
	System.arraycopy(identity, 0, matrix, 0, 16);
	type = TYPE_IDENTITY;
    }

    /**
     * Set this transform to a rotation about (0, 0, 0).
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     */
    public void setToRotation(double phi, double theta, double psi) {
	setToRotation(matrix, phi, theta, psi);
	// may be an identity transformation (e.g. if the angles
	// are multiples of 2 &pi;
	type = TYPE_UNKNOWN;
    }

    /**
     * Set this transform to a rotation about an anchor point.
     * The rotation is described by three angles based on Eulerian
     * angles: &phi;, &theta;, &psi;.  Eulerian angles as described in
     * Goldstein, "Classical Mechanics," determine how orthonormal
     * coordinate systems transform using a rotation.  For an
     * affine transformation that modifies objects, not coordinate
     * systems, the effect is the same as reversing the signs of
     * these angles, which getRotateInstance does implicitly.
     * The angle phi is the angle the object rotates
     * with positive values of phi indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  Theta then indicates a rotation about
     * the x axis, and psi indicates another rotation about the
     * z axis.
     * <P>
     * After the rotation, a translation is applied so that the
     * anchor point will have the same coordinates before and after
     * the transform is applied.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     * @param anchorx the x coordinate of the anchor point
     * @param anchory the y coordinate of the anchor point
     * @param anchorz the z coordinate of the anchor point
     */
    public void setToRotation(double phi, double theta, double psi,
			      double anchorx, double anchory, double anchorz)
    {
	setToRotation(matrix, phi, theta, psi,
		     anchorx, anchory, anchorz);
	if (((float)(1.0 + anchorx)) != 1.0F || ((float)(1.0 + anchory)) != 1.0F
	    || ((float)(1.0 + anchorz)) != 1.0F) type |= TYPE_TRANSLATION;
    }

    /**
     * Set this transform to a scaling transform.
     * The scaling transform will stretch an object by specified
     * amounts in the x, y, and z directions, with the point
     * (0,0,0) not moving.
     * @param sx the scaling factor in the x direction
     * @param sy the scaling factor in the y direction
     * @param sz the scaling factor in the z direction
     */
    public void setToScale(double sx, double sy, double sz) {
	setToScale(matrix, sx, sy, sz);
	if (sx == 1.0 && sy == 1.0 && sz == 1.0) {
	    type = TYPE_IDENTITY;
	} else if (((float)(1.0 + (sx-sy))) == 1.0F
		   && ((float)(1.0 +(sx-sz))) == 1.0F) {
	    type = TYPE_UNIFORM_SCALE;
	} else {
	    type = TYPE_SCALE;
	}
    }

    /**
     * Set this transform to a shearing transform.
     * @param shxy the X coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>01</sub>
     * @param shxz the X coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>02</sub>
     * @param shyx the Y coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>10</sub>
     * @param shyz the Y coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>12</sub>
     * @param shzx the Z coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>20</sub>
     * @param shzy the Z coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>21</sub>
     */
    public void setToShear(double shxy, double shxz,
			   double shyx, double shyz,
			   double shzx, double shzy)
    {
	setToShear(matrix, shxy, shxz, shyx, shyz, shzx, shzy);
	type = TYPE_GENERAL;
    }

    /**
     * Set this transform to a translation.
     * @param tx the translation in the X direction
     * @param ty the translation in the Y direction
     * @param tz the translation in the Z direction
     */
    public void setToTranslation(double tx, double ty, double tz) {
	setToTranslation(matrix, tx, ty, tz);
	type = TYPE_TRANSLATION;
    }

    /**
     * Set this transform to the same transformation used by another transform.
     * @param Tx the transform whose configuration will be copied
     */
    public void setTransform(AffineTransform3D Tx) {
	Tx.getMatrix(matrix);
	type = TYPE_UNKNOWN;
    }
    
    /**
     * Set this transform's parameters.
     * @param m00 the matrix element m<sub>00</sub>
     * @param m10 the matrix element m<sub>10</sub>
     * @param m20 the matrix element m<sub>20</sub>
     * @param m01 the matrix element m<sub>01</sub>
     * @param m11 the matrix element m<sub>11</sub>
     * @param m21 the matrix element m<sub>21</sub>
     * @param m02 the matrix element m<sub>02</sub>
     * @param m12 the matrix element m<sub>12</sub>
     * @param m22 the matrix element m<sub>22</sub>
     * @param m03 the matrix element m<sub>03</sub>
     * @param m13 the matrix element m<sub>13</sub>
     * @param m23 the matrix element m<sub>23</sub>
     */
    public void setTransform(double m00, double m10, double m20, 
			     double m01, double m11, double m21,
			     double m02, double m12, double m22,
			     double m03, double m13, double m23)
    {
	matrix[0] = m00;
	matrix[1] = m10;
	matrix[2] = m20;
	matrix[3] = m01;
	matrix[4] = m11;
	matrix[5] = m21;
	matrix[6] = m02;
	matrix[7] = m12;
	matrix[8] = m22;
	matrix[9] = m03;
	matrix[10] = m13;
	matrix[11] = m23;

	type = TYPE_UNKNOWN;
    }

    /**
     * Concatenate this transform with a shearing transform.
     * The parameters define the shearing transform.
     * @param shxy the X coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>01</sub>
     * @param shxz the X coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>02</sub>
     * @param shyx the Y coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>10</sub>
     * @param shyz the Y coordinate is shifted by this factor times 
     *        the Z coordinate; equivalent to the value of m<sub>12</sub>
     * @param shzx the Z coordinate is shifted by this factor times 
     *        the X coordinate; equivalent to the value of m<sub>20</sub>
     * @param shzy the Z coordinate is shifted by this factor times 
     *        the Y coordinate; equivalent to the value of m<sub>21</sub>
     */
    public void shear(double shxy, double shxz,
		      double shyx, double shyz,
		      double shzx, double shzy)
    {
	concatenate(getShearInstance(shxy, shxz, shyx, shyz, shzx, shzy));
    }
    
    @Override
    public String toString() {
	return "[[" + matrix[0] + ", " +  matrix[1] + ", " + matrix[2] + "],"
	    + "[" + matrix[3] + ", " +  matrix[4] + ", " + matrix[5] + "],"
	    + "[" + matrix[6] + ", " +  matrix[7] + ", " + matrix[8] + "],"
	    + "[" + matrix[9] + ", " +  matrix[10] + ", " + matrix[11] + "]]";
    }

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as double-precision numbers and the destination
     * points' coordinates specified as double-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * The implementation allows srcPts and dstPnts to be the same
     * array and their offsets to overlap, in which case the new
     * values will not be stored until the old values have been used.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    public void transform(double[] srcPts, int srcOff, double[] dstPts,
		     int dstOff, int numPts)
    {
	double[] tmp = null;
	int saveOff = dstOff;
	if (srcPts == dstPts &&
	    Math.abs(srcOff - dstOff) < 3*numPts) {
	    /*
	     * We have an overlap.
	     */
	    tmp = new double[numPts*3];
	    double[] stmp = dstPts;
	    dstPts = tmp;
	    tmp = stmp;
	    dstOff = 0;
	}
	for (int n = 0; n < numPts; n++) {
	    for (int i = 0; i < 3; i++) {
		dstPts[dstOff+i] = matrix[i+3*0]*srcPts[srcOff];
		dstPts[dstOff+i] += matrix[i+3*1]*srcPts[srcOff+1];
		dstPts[dstOff+i] += matrix[i+3*2] * srcPts[srcOff+2];
	    }
	    dstPts[dstOff+0] += matrix[0+3*3];
	    dstPts[dstOff+1] += matrix[1+3*3];
	    dstPts[dstOff+2] += matrix[2+3*3];
	    srcOff += 3;
	    dstOff += 3;
	}
	if (tmp != null) {
	    System.arraycopy(dstPts, 0, tmp, saveOff, numPts*3);
	}
    }

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as double-precision numbers and the destination
     * points' coordinates specified as single-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     *
     */
    public void transform(double[] srcPts, int srcOff, float[] dstPts,
		     int dstOff, int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    for (int i = 0; i < 3; i++) {
		dstPts[dstOff+i] = (float)(matrix[i+3*0]*srcPts[srcOff]);
		dstPts[dstOff+i] += (float)(matrix[i+3*1]*srcPts[srcOff+1]);
		dstPts[dstOff+i] += (float)(matrix[i+3*2] * srcPts[srcOff+2]);
	    }
	    dstPts[dstOff+0] += (float)matrix[0+3*3];
	    dstPts[dstOff+1] += (float)matrix[1+3*3];
	    dstPts[dstOff+2] += (float)matrix[2+3*3];
	    srcOff += 3;
	    dstOff += 3;
	}
    }

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as single-precision numbers and the destination
     * points' coordinates specified as single-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * The implementation allows srcPts and dstPnts to be the same
     * array and their offsets to overlap, in which case the new
     * values will not be stored until the old values have been used.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    public void transform(float[]srcPts, int srcOff, float[]dstPts,
		     int dstOff, int numPts)
    {
	float[] tmp = null;
	int saveOff = dstOff;
	if (srcPts == dstPts &&
	    Math.abs(srcOff - dstOff) < 3*numPts) {
	    /*
	     * We have an overlap.
	     */
	    tmp = new float[numPts*3];
	    float[] stmp = dstPts;
	    dstPts = tmp;
	    tmp = stmp;
	    dstOff = 0;
	}
	for (int n = 0; n < numPts; n++) {
	    for (int i = 0; i < 3; i++) {
		dstPts[dstOff+i] = (float)(matrix[i+3*0]*srcPts[srcOff]);
		dstPts[dstOff+i] += (float)(matrix[i+3*1]*srcPts[srcOff+1]);
		dstPts[dstOff+i] += (float)(matrix[i+3*2] * srcPts[srcOff+2]);
	    }
	    dstPts[dstOff+0] += (float)matrix[0+3*3];
	    dstPts[dstOff+1] += (float) matrix[1+3*3];
	    dstPts[dstOff+2] += (float) matrix[2+3*3];
	    srcOff += 3;
	    dstOff += 3;
	}
	if (tmp != null) {
	    System.arraycopy(dstPts, 0, tmp, saveOff, numPts*3);
	}
    }

    /**
     * Apply this transform to a sequence of points with the source points'
     * coordinates specified as single-precision numbers and the destination
     * points' coordinates specified as double-precision numbers.
     * Each point is an ordered triplet
     * (p<sub>x</sub>,p<sub>y</sub>,p<sub>z</sub> stored as consecutive
     * array elements in that order.
     * @param srcPts the points to transform
     * @param srcOff the offset int srcPts at which the
     *               first point is stored
     * @param dstPts the transformed points
     * @param dstOff the offset in dstPts at which the first transformed
     *        point will be stored
     * @param numPts the number of points
     */
    public void transform(float[]srcPts, int srcOff, double[]dstPts,
		     int dstOff, int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    for (int i = 0; i < 3; i++) {
		dstPts[dstOff+i] = matrix[i+3*0]*srcPts[srcOff];
		dstPts[dstOff+i] += matrix[i+3*1]*srcPts[srcOff+1];
		dstPts[dstOff+i] += matrix[i+3*2] * srcPts[srcOff+2];
	    }
	    dstPts[dstOff+0] += matrix[0+3*3];
	    dstPts[dstOff+1] += matrix[1+3*3];
	    dstPts[dstOff+2] += matrix[2+3*3];
	    srcOff += 3;
	    dstOff += 3;
	}
    }

    /**
     * Apply this transform to a single point, optionally storing the
     * transformed value in a specified point.
     * Note: ptSrc and ptDst can be the same point.
     * @param ptSrc the untransformed point
     * @param ptDst the point to be transformed (a new point will
     *        be created if the value is null)
     * @return the transformed point
     */
    public Point3D transform(Point3D ptSrc, Point3D ptDst) {
	double pts[] = {ptSrc.getX(), ptSrc.getY(), ptSrc.getZ(),
			0.0, 0.0, 0.0};
	transform(pts, 0, pts, 3, 1);
	
	if (ptDst == null) {
	    try {
		ptDst = Cloner.makeClone(ptSrc);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError();
	    }
	}
	ptDst.setLocation(pts[3], pts[4], pts[5]);
	return ptDst;
    }

    /**
     * Concatenate this transform with a translation.
     * @param tx the translation in the X direction
     * @param ty the translation in the Y direction
     * @param tz the translation in the Z direction
     */
    public void translate(double tx, double ty, double tz) {
	concatenate(getTranslateInstance(tx, ty, tz));
    }

    /**
     * Sets this affine transform to one that maps the point p1 to p2,
     * and arranges that points along a line in the direction of a
     * vector xvec1 will lie along a line in the direction of a vector
     * xvec2, and that points along a line in the direction of a
     * vector yvec1 will lie along a line in the direction of a vector
     * yvec2.
     * In addition, points along a line in the direction of a vector
     * zvec1 will lie along a line in the direction of a vector zvec2,
     * where zvec1 is the cross product of xvec1 and yvec1, and zvec2
     * is the cross product of the xvec2 and yvec2. The two vectors
     * xvec1 and yvec1 must be orthogonal, as must xvec2 and yvec2.
     * <P>
     * The argument theta adds an extra rotation in the xvec2-yvec2 plane
     * about p2.  Positive values of theta correspondings to a
     * counterclockwise rotation when xvec2 is viewed as pointing right and
     * yvec2 as pointing up.
     * with positive angles moving the
     * @param p1 the initial point; null for (0,0,0)
     * @param xvec1 a vector treated as defining an initial  'X' axis
     * @param yvec1 a vector treated as defining an initial 'Y' axis
     * @param p2 the final point; null for (0,0,0)
     * @param xvec2 a vector treated as defining a final  'X' axis
     * @param yvec2 a vector treated as defining a final 'Y' axis
     * @param theta an extra rotation in the xvec2-yvec2 plane with
     *        positive values of theta corresponding to a clockwise
     *        rotation (i.e., a rotation moving from xvec towards
     *        yvec).
     * @exception IllegalArgumentException an argument was illegal
     */
    public void
	setToMap(Point3D p1,double[] xvec1,double[] yvec1,
		 Point3D p2, double[] xvec2, double[] yvec2,
		 double theta)
	throws IllegalArgumentException
    {
	double[] xvc1 = VectorOps.unitVector(xvec1);
	double[] yvc1 = VectorOps.unitVector(yvec1);
	double[] xvc2 = VectorOps.unitVector(xvec2);
	double[] yvc2 = VectorOps.unitVector(yvec2);
	if (Math.abs(VectorOps.dotProduct(xvc1, yvc1)) > 1.e-12) {
	    throw new IllegalArgumentException(errorMsg("afMap1"));
	}
	if (Math.abs(VectorOps.dotProduct(xvc2, yvc2)) > 1.e-12) {
	    throw new IllegalArgumentException(errorMsg("afMap2"));
	}

	if (theta != 0.0) {
	    if (theta == Math.PI/2) {
		double[] tmpx = xvc2;
		double[] tmpy = yvc2;
		VectorOps.multiply(tmpx, -1.0, tmpx);
		xvc2 = tmpy;
		yvc2 = tmpx;
	    } else if (theta == Math.PI || theta == -Math.PI) {
		VectorOps.multiply(xvc2, -1.0, xvc2);
		VectorOps.multiply(yvc2, -1.0, yvc2);
	    } else if (theta == -Math.PI/2 || theta == 3*(Math.PI)/2
		       || theta == (3*Math.PI)/2) {
		double[] tmpx = xvc2;
		double[] tmpy = yvc2;
		VectorOps.multiply(tmpy, -1.0, tmpy);
		xvc2 = tmpy;
		yvc2 = tmpx;
	    } else {
		double sinTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		double[] xv2 =
		    VectorOps.add(VectorOps.multiply(cosTheta,xvc2),
				  VectorOps.multiply(sinTheta,yvc2));
		double[] yv2 =
		    VectorOps.add(VectorOps.multiply(-sinTheta,xvc2),
				  VectorOps.multiply(cosTheta,yvc2));

		/*
		if (Math.abs(VectorOps.dotProduct(xv2, yv2)) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(xvc2, yvc2)) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(xv2, xv2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(xvc2, xvc2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(yv2, yv2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(yvc2, yvc2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}

		double dx = VectorOps.dotProduct(xv2, xvc2);
		double dy = VectorOps.dotProduct(xv2, yvc2);
		if (Math.abs(Math.atan2(dy, dx) - theta) > 1.e-10) {
		    throw new RuntimeException();
		}
		double[] cp1 = VectorOps.crossProduct(xvc2, yvc2);
		double[] cp2 = VectorOps.crossProduct(xv2, yv2);
		if (Math.abs(VectorOps.dotProduct(cp1, cp1) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(cp2, cp2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		if (Math.abs(VectorOps.dotProduct(cp1, cp2) - 1) > 1.e-10) {
		    throw new RuntimeException();
		}
		*/
		xvc2 = xv2;
		yvc2 = yv2;
	    }
	}

	double[] zvec1 = VectorOps.crossProduct(xvc1, yvc1);
	double[] zvec2 = VectorOps.crossProduct(xvc2, yvc2);
	double m03 = (p2 == null)? 0.0: p2.getX();
	double m13 = (p2 == null)? 0.0: p2.getY();
	double m23 = (p2 == null)? 0.0: p2.getZ();
	double m00 = VectorOps.dotProduct(xvc1, xvc2);
	double m01 = VectorOps.dotProduct(xvc1, yvc2);
	double m02 = VectorOps.dotProduct(xvc1, zvec2);
	double m10 = VectorOps.dotProduct(yvc1, xvc2);
	double m11 = VectorOps.dotProduct(yvc1, yvc2);
	double m12 = VectorOps.dotProduct(yvc1, zvec2);
	double m20 = VectorOps.dotProduct(zvec1, xvc2);
	double m21 = VectorOps.dotProduct(zvec1, yvc2);
	double m22 = VectorOps.dotProduct(zvec1, zvec2);

	int i = 0;
	matrix[i++] = m00;
	matrix[i++] = m10;
	matrix[i++] = m20;
	matrix[i++] = m01;
	matrix[i++] = m11;
	matrix[i++] = m21;
	matrix[i++] = m02;
	matrix[i++] = m12;
	matrix[i++] = m22;
	matrix[i++] = m03;
	matrix[i++] = m13;
	matrix[i++] = m23;

	double x1 = (p1 == null)? 0.0: p1.getX();
	double y1 = (p1 == null)? 0.0: p1.getY();
	double z1 = (p1 == null)? 0.0: p1.getZ();

	if (x1 != 0.0 || y1 != 0.0 || z1 == 0.0) {
	    translate(-x1, -y1, -z1);
	}
    }

    /**
     * Create an affine transform that maps the point p1 to p2, and arranges
     * that points along a line in the direction of a vector xvec1 will
     * lie along a line in the direction of a vector xvec2, and that points
     * along a line in the direction of a vector yvec1 will lie along a line
     * in the direction of a vector yvec2.
     * In addition, points along a line in the direction of a vector
     * zvec1 will lie along a line in the direction of a vector zvec2,
     * where zvec1 is the cross product of xvec1 and yvec1, and zvec2
     * is the cross product of the xvec2 and yvec2. The two vectors
     * xvec1 and yvec1 must be orthogonal, as must xvec2 and yvec2.
     * <P>
     * The argument theta adds an extra rotation in the xvec2-yvec2 plane
     * about p2.  Positive values of theta correspondings to a
     * counterclockwise rotation when xvec2 is viewed as pointing right and
     * @param p1 the initial point; null for (0,0,0)
     * @param xvec1 a vector treated as defining an initial  'X' axis
     * @param yvec1 a vector treated as defining an initial 'Y' axis
     * @param p2 the final point; null for (0,0,0)
     * @param xvec2 a vector treated as defining a final  'X' axis
     * @param yvec2 a vector treated as defining a final 'Y' axis
     * @param theta an extra rotation in the xvec2-yvec2 plane with
     *        positive values of theta corresponding to a clockwise
     *        rotation (i.e., a rotation moving from xvec towards
     *        yvec).
     * @return the affine transform
     * @exception IllegalArgumentException an argument was illegal
     */
    public static AffineTransform3D
       getMapInstance(Point3D p1,double[] xvec1,double[] yvec1,
		      Point3D p2, double[] xvec2, double[] yvec2,
		      double theta)
	throws IllegalArgumentException
    {
	AffineTransform3D af = new AffineTransform3D(true);
	af.setToMap(p1, xvec1, yvec1, p2, xvec2, yvec2, theta);
	return af;
    }
    /**
     * Concatenate this transform with an affine transform that maps
     * the point p1 to p2, and arranges that points along a line in
     * the direction of a vector xvec1 will lie along a line in the
     * direction of a vector xvec2, and that points along a line in
     * the direction of a vector yvec1 will lie along a line in the
     * direction of a vector yvec2.
     * In addition, points along a line in the direction of a vector
     * zvec1 will lie along a line in the direction of a vector zvec2,
     * where zvec1 is the cross product of xvec1 and yvec1, and zvec2
     * is the cross product of the xvec2 and yvec2. The two vectors
     * xvec1 and yvec1 must be orthogonal, as must xvec2 and yvec2.
     * <P>
     * The argument theta adds an extra rotation in the xvec2-yvec2 plane
     * about p2.  Positive values of theta correspondings to a
     * counterclockwise rotation when xvec2 is viewed as pointing right and
     * @param p1 the initial point; null for (0,0,0)
     * @param xvec1 a vector treated as defining an initial  'X' axis
     * @param yvec1 a vector treated as defining an initial 'Y' axis
     * @param p2 the final point; null for (0,0,0)
     * @param xvec2 a vector treated as defining a final  'X' axis
     * @param yvec2 a vector treated as defining a final 'Y' axis
     * @param theta an extra rotation in the xvec2-yvec2 plane with
     *        positive values of theta corresponding to a clockwise
     *        rotation (i.e., a rotation moving from xvec towards
     *        yvec).
     * @exception IllegalArgumentException an argument was illegal
     */
    public void
	map(Point3D p1,double[] xvec1,double[] yvec1,
	    Point3D p2, double[] xvec2, double[] yvec2,
	    double theta)
	throws IllegalArgumentException
    {
	concatenate(getMapInstance(p1, xvec1, yvec1,
				   p2, xvec2, yvec2,
				   theta));
    }

    @Override
    public AffineTransform3D affineTransform(double x, double y, double z)
	throws UnsupportedOperationException
    {
	return (AffineTransform3D) this.clone();
    }

}

//  LocalWords:  AffineTransform pre Tx flatmatrix transform's srcPts
//  LocalWords:  NoninvertibleTransformException srcOff dstPts dstOff
//  LocalWords:  numPts flatMatrix Eulerian Goldstein anchorx anchory
//  LocalWords:  getRotateInstance anchorz sx sy sz shxy shxz shyx tx
//  LocalWords:  shyz shzx shzy ty tz ptSrc ptDst chirality getType
//  LocalWords:  versa noShearTerms mayBeQuadrantRot cnt boolean
//  LocalWords:  mayBeIdent notRotNotShear cpxydotz dstPnts
