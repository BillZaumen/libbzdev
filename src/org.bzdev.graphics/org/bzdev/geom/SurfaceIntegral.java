package org.bzdev.geom;
import org.bzdev.math.*;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class providing surface integrals.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * A scalar field f can be integrated over a surface S:
 * $\int_S f dA$.
 * <!-- &int;<sub>S</sub>fdA.-->
 * Similarly a vector field <B>v</B> can be integrated over S:
 * $\int_S \mathbf{v \cdot n} dA$.
 * <!-- &int;<sub>S</sub><B>v&sdot;n</B>dA.-->
 * The constructors provide the scalar or vector fields as
 * functions of (x, y, z), plus a degree for a polynomial
 * approximation to these fields at the surface. This degree
 * is used to compute the number of points used in the
 * numerical integration.
 * The methods {@link SurfaceIntegral#integrate(SurfaceIterator)},
 * {@link SurfaceIntegral#integrate(Shape3D)},
 * {@link SurfaceIntegral#integrate(Shape3D,Transform3D)}, or
 * {@link SurfaceIntegral#integrate(Shape3D,Transform3D,int)}
 * will perform the integration for a specified surface.
 * <P>
 * The implementation uses Gauss-Legendre quadrature.
 */
public class SurfaceIntegral  {
    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static final int MIN_NSCALAR = 8;
    static int nscalar = MIN_NSCALAR;

    static final int MIN_FAST_NSCALAR = 3;
    static final int fast_nscalar = MIN_FAST_NSCALAR;

    int ncp;			// cubic patch
    int nct;			// cubic triangle
    int npt;			// planar triangle

    int fncp;			// cubic patch (fast case)
    int fnct;			// cubic triangle (fast case)

    double[][] weightsCP;
    double[] argsCP;

    double[][] weightsCT;
    double[] argsCTu;
    double[][] argsCTv;

    double[][] weightsPT;
    double[] argsPTu;
    double[][] argsPTv;

    boolean fast = false;
    double limit = -1.0;

    double[][] fweightsCP;
    double[] fargsCP;

    double[][] fweightsCT;
    double[] fargsCTu;
    double[][] fargsCTv;

    private void configure() {
	argsCP = GLQuadrature.getArguments(0.0, 1.0, ncp);
	weightsCP = new double[ncp][ncp];
	double[] ws = GLQuadrature.getWeights(0.0, 1.0, ncp);
	for (int i = 0; i < ncp; i++) {
	    for (int j = 0; j < ncp; j++) {
		weightsCP[i][j] = ws[i]*ws[j];
	    }
	}
	weightsCT = new double[nct][];
	argsCTu = GLQuadrature.getArguments(0.0, 1.0, nct);
	argsCTv = new double[nct][];
	double[] uws = GLQuadrature.getWeights(0.0, 1.0, nct);
	for (int i = 0; i < nct; i++) {
	    double vmax = 1.0 - argsCTu[i];
	    argsCTv[i] = GLQuadrature.getArguments(0.0, vmax, nct);
	    weightsCT[i] = GLQuadrature.getWeights(0.0, vmax, nct);
	    for (int j = 0; j < weightsCT[i].length; j++) {
		weightsCT[i][j] *= uws[i];
	    }
	}

	weightsPT = new double[npt][];
	argsPTu = GLQuadrature.getArguments(0.0, 1.0, npt);
	argsPTv = new double[npt][];
	uws = GLQuadrature.getWeights(0.0, 1.0, npt);
	for (int i = 0; i < npt; i++) {
	    double vmax = 1.0 - argsPTu[i];
	    argsPTv[i] = GLQuadrature.getArguments(0.0, vmax, npt);
	    weightsPT[i] = GLQuadrature.getWeights(0.0, vmax, npt);
	    for (int j = 0; j < weightsPT[i].length; j++) {
		weightsPT[i][j] *= uws[i];
	    }
	}

	if (fast) {
	    fargsCP = GLQuadrature.getArguments(0.0, 1.0, fncp);
	    fweightsCP = new double[fncp][fncp];
	    ws = GLQuadrature.getWeights(0.0, 1.0, fncp);
	    for (int i = 0; i < fncp; i++) {
		for (int j = 0; j < fncp; j++) {
		    fweightsCP[i][j] = ws[i]*ws[j];
		}
	    }
	    fweightsCT = new double[fnct][];
	    fargsCTu = GLQuadrature.getArguments(0.0, 1.0, fnct);
	    fargsCTv = new double[fnct][];
	    uws = GLQuadrature.getWeights(0.0, 1.0, fnct);
	    for (int i = 0; i < fnct; i++) {
		double vmax = 1.0 - fargsCTu[i];
		fargsCTv[i] = GLQuadrature.getArguments(0.0, vmax, fnct);
		fweightsCT[i] = GLQuadrature.getWeights(0.0, vmax, fnct);
		for (int j = 0; j < fweightsCT[i].length; j++) {
		    fweightsCT[i][j] *= uws[i];
		}
	    }
	}
    }

    RealValuedFunctThreeOps sf;
    RealValuedFunctThreeOps xf;
    RealValuedFunctThreeOps yf;
    RealValuedFunctThreeOps zf;

    /**
     * Constructor for the surface integral of a scalar field.
     * @param n maximum value for the sum i+j+k of the exponents of
     *        terms x<sup>i</sup>y<sup>j</sup>z<sup>k</sup> for a
     *        polynomial in x, y and z that provides an adequate approximation
     *        to the scalar field over a surface patch
     * @param sf the scalar field as a function of x, y, and z
     */
    public SurfaceIntegral(int n, RealValuedFunctThreeOps sf) {
	ncp = nscalar*2-1;
	ncp += 6*n + 1;
	if (ncp%2 == 1)  ncp++;
	ncp /= 2;
	nct = ncp;
	npt = 2;
	npt += 6*n + 1;
	if (npt%2 == 1) npt++;
	npt /= 2;
	this.sf = sf;
	configure();
    }

    /**
     * Constructor for the surface integral of a scalar field with a
     * flatness parameter.
     * <P>
     * A flatness parameter determines if fewer points can be used for
     * a numerical integration using Gauss-Legendre quadrature at the
     * expense of some reduction in accuracy for the case of a cubic
     * B&eacute;zier triangle or cubic B&eacute;zier patch (the
     * surface integral is represented as a sum over patches).
     * Consider the 16 control points for a cubic B&eacute;zier patch:
     *<BLOCKQUOTE><PRE>
     * P<sub>03</sub> P<sub>13</sub> P<sub>23</sub> P<sub>33</sub>
     * P<sub>02</sub> P<sub>12</sub> P<sub>22</sub> P<sub>32</sub>
     * P<sub>01</sub> P<sub>11</sub> P<sub>21</sub> P<sub>31</sub>
     * P<sub>00</sub> P<sub>10</sub> P<sub>20</sub> P<sub>30</sub>
     * </PRE></BLOCKQUOTE>
     * If the patch is planar with the coordinates at each point
     * linear in u and v, the intermediate control points will lie
     * along lines connecting the corners and will be at locations 1/3
     * and 2/3 of the distance between each pair of corners. In the
     * usual case in which the cubic B&eacute;zier patch is not planar,
     * the flatness parameter is used to determine if the surface is
     * close to planar.  The criteria are as follows:
     * <UL>
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. If the
     *        absolute value of the tangent of the angle between these
     *        two vectors exceeds the flatness parameter, the test
     *        fails.
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. Compute the
     *        lengths of these two lines.  If ratio of the length of
     *        the line to an adjacent point to the length of the line
     *        to the opposite corner differs from 1/3 by more than the
     *        flatness limit, the test fails.
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. Compute the
     *        lengths of these two lines.  If the dot product of these
     *        two vectors is negative, the test fails.
     * </UL>
     * A similar set of tests is done for cubic B&ecute;zier
     * triangles.
     * <P>
     * The use of a flatness parameter can reduce running times for
     * some computations noticeably with only a minor impact on
     * accuracy. Please see
     * {@link #SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)}
     * for an example showing running times.
     * @param n maximum value for the sum i+j+k of the exponents of
     *        terms x<sup>i</sup>y<sup>j</sup>z<sup>k</sup> for a
     *        polynomial in x, y and z that provides an adequate approximation
     *        to the scalar field over a surface patch
     * @param sf the scalar field as a function of x, y, and z
     * @param limit the flatness limit
     */
    public SurfaceIntegral(int n, RealValuedFunctThreeOps sf,
			   int limit) {
	ncp = nscalar*2-1;
	ncp += 6*n + 1;
	if (ncp%2 == 1)  ncp++;
	ncp /= 2;
	nct = ncp;
	npt = 2;
	npt += 6*n + 1;
	if (npt%2 == 1) npt++;
	npt /= 2;
	this.sf = sf;
	if (limit > 0.0) fast = true;
	fncp = fast_nscalar*2-1;
	fncp += 4*n + 1;
	if (fncp%2 == 1)  fncp++;
	fncp /= 2;
	fnct = ncp;
	this.limit = limit;
	configure();
    }


    /**
     * Constructor for the surface integral of a vector field.
     * @param n maximum value for the sum i+j+k of the exponents of
     *        terms x<sup>i</sup>y<sup>j</sup>z<sup>k</sup> for a
     *        polynomial in x, y and z that provides an adequate approximation
     *        to a component of the vector field over a surface patch
     * @param xf the X component of the vector  field as a function
     *           of x, y, and z
     * @param yf the Y component of the vector  field as a function
     *           of x, y, and z
     * @param zf the Z component of the vector  field as a function
     *           of x, y, and z
     */
    public SurfaceIntegral(int n,
			   RealValuedFunctThreeOps xf,
			   RealValuedFunctThreeOps yf,
			   RealValuedFunctThreeOps zf)
    {
	if (xf == null && yf == null && zf == null) {
	    throw new IllegalArgumentException(errorMsg("noFunctions"));
	}
	ncp = 2*(3+2);
	ncp += 6*n + 1;
	if (ncp%2 == 1)  ncp++;
	ncp /= 2;
	nct = 2*(3);
	nct += 6*n+1;
	if (nct%2 == 1) nct++;
	nct /= 2;
	npt = 6*n + 1;
	if (npt%2 == 1) npt++;
	npt /= 2;
	this.xf = xf;
	this.yf = yf;
	this.zf = zf;
	configure();
	/*
	System.out.println("ncp = " + ncp +", nct = " + nct
			   + ",  npt = " + npt);
	*/
    }

    /**
     * Constructor for the surface integral of a vector field with a
     * flatness parameter.
     * <P>
     * A flatness parameter determines if fewer points can be used for
     * a numerical integration using Gauss-Legendre quadrature at the
     * expense of some reduction in accuracy for the case of a cubic
     * B&eacute;zier triangle or cubic B&eacute;zier patch (the
     * surface integral is represented as a sum over patches).
     * Consider the 16 control points for a cubic B&eacute;zier patch:
     *<BLOCKQUOTE><PRE>
     * P<sub>03</sub> P<sub>13</sub> P<sub>23</sub> P<sub>33</sub>
     * P<sub>02</sub> P<sub>12</sub> P<sub>22</sub> P<sub>32</sub>
     * P<sub>01</sub> P<sub>11</sub> P<sub>21</sub> P<sub>31</sub>
     * P<sub>00</sub> P<sub>10</sub> P<sub>20</sub> P<sub>30</sub>
     * </PRE></BLOCKQUOTE>
     * If the patch is planar with the coordinates at each point
     * linear in u and v, the intermediate control points will lie
     * along lines connecting the corners and will be at locations 1/3
     * and 2/3 of the distance between each pair of corners. In the
     * usual case in which the cubic B&eacute;zier patch is not planar,
     * the flatness parameter is used to determine if the surface is
     * close to planar.  The criteria are as follows:
     * <UL>
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. If the
     *        absolute value of the tangent of the angle between these
     *        two vectors exceeds the flatness parameter, the test
     *        fails.
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. Compute the
     *        lengths of these two lines.  If ratio of the length of
     *        the line to an adjacent point to the length of the line
     *        to the opposite corner differs from 1/3 by more than the
     *        flatness limit, the test fails.
     *   <LI> For each corner, construct a line vector from the corner
     *        to an adjacent control point (both along the edges and a
     *        diagonal) and from that corner and the opposite corner
     *        along the same edge and the same diagonal. Compute the
     *        lengths of these two lines.  If the dot product of these
     *        two vectors is negative, the test fails.
     * </UL>
     * A similar set of tests is done for cubic B&ecute;zier
     * triangles.
     * <P>
     * The use of a flatness parameter can reduce running times for
     * some computations noticeably with only a minor impact on
     * accuracy. For example, for a sphere constructed by joining two
     * {@link BezierGrid B&ecute;zier grids} with a total of 3200
     * patches, each covering a small portion of the sphere, a center
     * of mass computation's running time was reduced from 673 ms
     * to 422 ms. Similarly, a computation of moments about the
     * center of mass was reduced from 1.07 seconds to 606 ms.
     * The flatness test took 8.2 milliseconds, and thus contributes
     * very little to the total running time.  The flatness parameter
     * for these tests was 0.5. When printed with a "%g" format that
     * by default displays 5 digits past the decimal point, the
     * diagonal elements for the moments printed the same for both
     * cases. The off-diagonal elements differed in the value before
     * the decimal point, but the exponent was 10<sup>17</sup> times
     * smaller (and for a perfect sphere, should be zero).
     * @param n maximum value for the sum i+j+k of the exponents of
     *        terms x<sup>i</sup>y<sup>j</sup>z<sup>k</sup> for a
     *        polynomial in x, y and z that provides an adequate approximation
     *        to a component of the vector field over a surface patch
     * @param xf the X component of the vector  field as a function
     *           of x, y, and z
     * @param yf the Y component of the vector  field as a function
     *           of x, y, and z
     * @param zf the Z component of the vector  field as a function
     *           of x, y, and z
     * @param limit  the flatness limit
     */
    public SurfaceIntegral(int n,
			   RealValuedFunctThreeOps xf,
			   RealValuedFunctThreeOps yf,
			   RealValuedFunctThreeOps zf,
			   double limit)
    {
	if (xf == null && yf == null && zf == null) {
	    throw new IllegalArgumentException(errorMsg("noFunctions"));
	}
	ncp = 2*(3+2);
	ncp += 6*n + 1;
	if (ncp%2 == 1)  ncp++;
	ncp /= 2;
	nct = 2*(3);
	nct += 6*n+1;
	if (nct%2 == 1) nct++;
	nct /= 2;
	npt = 6*n + 1;
	if (npt%2 == 1) npt++;
	npt /= 2;
	this.xf = xf;
	this.yf = yf;
	this.zf = zf;

	if (limit > 0.0) fast = true;
	this.limit = limit;
	fncp = 2*(2+2);
	fncp += 4*n + 1;
	if (fncp%2 == 1)  fncp++;
	fncp /= 2;
	fnct = 2*(2);
	fnct += 4*n+1;
	if (fnct%2 == 1) nct++;
	fnct /= 2;

	configure();
	/*
	System.out.println("ncp = " + ncp +", nct = " + nct
			   + ",  npt = " + npt);
	*/
    }


    static final double oneThird = 1.0/3.0;

    static boolean nearlyFlat(double limit, int stype, double[] coords,
			      double[] coords1, double[] coords2,
			      double[] coords3, double[] coords4)
    {
	// tests not compete but first check timing
	switch(stype) {
	case SurfaceIterator.PLANAR_TRIANGLE:
	    // we don't treat a planar triangle as a special case.
	    return false;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    {
		// left edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 0, 3);
		double norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 3, coords, 0, 3);
		double inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		double dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;
		VectorOps.sub(coords2, 0, coords, 9, coords, 15, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// CP111
		VectorOps.sub(coords2, 0, coords, 27, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.sub(coords3, 0, coords, 15, coords, 0, 3);
		VectorOps.crossProduct(coords4, coords1, coords2);
		VectorOps.normalize(coords4);
		dp = VectorOps.dotProduct(coords3, coords4);
		VectorOps.multiply(coords4, dp, coords4);
		VectorOps.sub(coords2, coords3, coords4);
		if (Math.abs(dp/VectorOps.norm(coords2)) > limit) return false;
		VectorOps.add(coords4, 0, coords, 0, coords, 9, 3);
		VectorOps.add(coords4, 0, coords4, 0, coords, 27, 3);
		VectorOps.sub(coords4, 0, coords4, 0, coords, 15, 3);
		norm = VectorOps.norm(coords4);
		double circum = VectorOps.norm(coords1)
		    + VectorOps.norm(coords2);
		VectorOps.sub(coords2, 0, coords, 9, coords, 27, 3);
		circum += VectorOps.norm(coords2);
		if (Math.abs(3*norm/circum) > limit) return false;

		// left edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge center
		VectorOps.sub(coords2, 0, coords, 6, coords, 3, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// bottom edge start
		VectorOps.sub(coords1, 0, coords, 27, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge end
		VectorOps.sub(coords2, 0, coords, 27, coords, 21, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;


		// bottom edge center
		VectorOps.sub(coords2, 0, coords, 21, coords, 12, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// right edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 27, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 24, coords, 27, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 18, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 18, coords, 24, 3);
		norm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		return true;
	    }
	case SurfaceIterator.CUBIC_VERTEX:
	    if (coords.length < 48) {
		double[] newcoords = new double[48];
		System.arraycopy(coords, 0, newcoords, 0, 15);
		coords = newcoords;
	    }
	    Shape3DHelper.cubicVertexToPatch(coords, 0, coords, 0);
	    // fall through
	case SurfaceIterator.CUBIC_PATCH:
	    {
		// left edge start
		VectorOps.sub(coords1, 0, coords, 36, coords, 0, 3);
		double norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12, coords, 0, 3);
		double inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		double dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge end
		VectorOps.sub(coords2, 0, coords, 36, coords, 24, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge center
		VectorOps.sub(coords2, 0, coords, 24, coords, 12, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// right edge start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 9, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12+9, coords, 9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 24+9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 24+9, coords, 12+9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// bottom edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 3, coords, 0, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge center
		VectorOps.sub(coords2, 0, coords, 6, coords, 3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// top edge start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 36, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 36+3, coords, 36, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// top edge end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 36+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 36+6, coords, 36+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;


		// left bottom to top diagonal start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12+3, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left bottom to top diagonal end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 24+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left bottom to top diagonal center
		VectorOps.sub(coords2, 0, coords, 24+6, coords, 12+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// left top to bottom diagonal start
		VectorOps.sub(coords1, 0, coords, 9, coords, 36, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 24+3, coords, 36, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left top to bottom diagonal end
		VectorOps.sub(coords2, 0,  coords, 9, coords, 12+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left top to bottom diagonal center
		VectorOps.sub(coords2, 0, coords, 12+6, coords, 24+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		return true;
	    }
	}
	return false;
    }


    private void integrateS(Adder adder, SurfaceIterator si) {
	double[] coords = new double[48];
	double[] tmpu = new double[3];
	double[] tmpv = new double[3];
	double[] tmpr = new double[3];
	double[] cp = new double[3];

	double[] coords1, coords2, coords3, coords4;
	if (fast) {
	    coords1 = new double[3];
	    coords2 = new double[3];
	    coords3 = new double[3];
	    coords4 = new double[3];
	} else {
	    coords1 = null;
	    coords2 = null;
	    coords3 = null;
	    coords4 = null;
	}

	while(si.isDone() == false) {
	    int type;
	    switch(type = si.currentSegment(coords)) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		for (int i = 0; i < npt; i++) {
		    double u = argsPTu[i];
		    for (int j = 0; j < weightsPT[i].length; j++) {
			double v = argsPTv[i][j];
			SurfaceOps.uTangent(tmpu, type, coords, u, v);
			SurfaceOps.vTangent(tmpv, type, coords, u, v);
			VectorOps.crossProduct(cp, tmpu, tmpv);
			double norm = VectorOps.norm(cp);
			SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			double value = sf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			adder.add(norm*value*weightsPT[i][j]);
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		if (fast && nearlyFlat(limit, type, coords, coords1, coords2,
				       coords3, coords4)) {
		    for (int i = 0; i < fnct; i++) {
			double u = fargsCTu[i];
			for (int j = 0; j < fweightsCT[i].length; j++) {
			    double v = fargsCTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    VectorOps.crossProduct(cp, tmpu, tmpv);
			    double norm = VectorOps.norm(cp);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    double value =
				sf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    adder.add(norm*value*fweightsCT[i][j]);
			}
		    }
		} else {
		    for (int i = 0; i < nct; i++) {
			double u = argsCTu[i];
			for (int j = 0; j < weightsCT[i].length; j++) {
			    double v = argsCTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    VectorOps.crossProduct(cp, tmpu, tmpv);
			    double norm = VectorOps.norm(cp);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    double value =
				sf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    adder.add(norm*value*weightsCT[i][j]);
			}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		Shape3DHelper.cubicVertexToPatch(coords, 0, coords, 0);
		type = SurfaceIterator.CUBIC_PATCH;
		// fall through
	    case SurfaceIterator.CUBIC_PATCH:
		if (fast && nearlyFlat(limit, type, coords, coords1, coords2,
				       coords3, coords4)) {
		    for (int i = 0; i < fncp; i++) {
			double u = fargsCP[i];
			for (int j = 0; j < fncp; j++) {
			    double v = fargsCP[j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    VectorOps.crossProduct(cp, tmpu, tmpv);
			    double norm = VectorOps.norm(cp);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    double value =
				sf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    adder.add(norm*value*fweightsCP[i][j]);
			}
		    }
		} else {
		    for (int i = 0; i < ncp; i++) {
			double u = argsCP[i];
			for (int j = 0; j < ncp; j++) {
			    double v = argsCP[j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    VectorOps.crossProduct(cp, tmpu, tmpv);
			    double norm = VectorOps.norm(cp);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    double value =
				sf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    adder.add(norm*value*weightsCP[i][j]);
			}
		    }
		}
		break;
	    }
	    si.next();
	}
    }
    
    private void integrateV(Adder adder, SurfaceIterator si)
    {
	if (!si.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("notOriented"));
	}

	double[] coords = new double[48];
	double[] tmpu = new double[3];
	double[] tmpv = new double[3];
	double[] tmpr = new double[3];
	double[] vect = new double[3];
	double[] cp = new double[3];
	/*
	Adder.Kahan adderPT = new Adder.Kahan();
	Adder.Kahan adderCT = new Adder.Kahan();
	Adder.Kahan adderCP = new Adder.Kahan();
	*/

	double[] coords1, coords2, coords3, coords4;
	if (fast) {
	    coords1 = new double[3];
	    coords2 = new double[3];
	    coords3 = new double[3];
	    coords4 = new double[3];
	} else {
	    coords1 = null;
	    coords2 = null;
	    coords3 = null;
	    coords4 = null;
	}

	while(si.isDone() == false) {
	    int type;
	    switch(type = si.currentSegment(coords)) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		for (int i = 0; i < npt; i++) {
		    double u = argsPTu[i];
		    for (int j = 0; j < weightsPT[i].length; j++) {
			double v = argsPTv[i][j];
			SurfaceOps.uTangent(tmpu, type, coords, u, v);
			SurfaceOps.vTangent(tmpv, type, coords, u, v);
			SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			vect[0] = (xf == null)? 0.0:
			    xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			vect[1] = (yf == null)? 0.0:
			    yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			vect[2] = (zf == null)? 0.0:
			    zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			double d = VectorOps.dotCrossProduct(vect, tmpu, tmpv);
			adder.add(d*weightsPT[i][j]);
			// adderPT.add(d*weightsPT[i][j]);
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		if (fast && nearlyFlat(limit, type, coords, coords1, coords2,
				       coords3, coords4)) {
		    for (int i = 0; i < fnct; i++) {
			double u = fargsCTu[i];
			for (int j = 0; j < fweightsCT[i].length; j++) {
			    double v = fargsCTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    vect[0] = (xf == null)? 0.0:
				xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[1] = (yf == null)? 0.0:
				yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[2] = (zf == null)? 0.0:
				zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    double d =
				VectorOps.dotCrossProduct(vect, tmpu, tmpv);
			    adder.add(d*fweightsCT[i][j]);
			    // adderCT.add(d*weightsCT[i][j]);
			}
		    }
		} else {
		    for (int i = 0; i < nct; i++) {
			double u = argsCTu[i];
			for (int j = 0; j < weightsCT[i].length; j++) {
			    double v = argsCTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    vect[0] = (xf == null)? 0.0:
				xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[1] = (yf == null)? 0.0:
				yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[2] = (zf == null)? 0.0:
				zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    double d =
				VectorOps.dotCrossProduct(vect, tmpu, tmpv);
			    adder.add(d*weightsCT[i][j]);
			    // adderCT.add(d*weightsCT[i][j]);
			}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		Shape3DHelper.cubicVertexToPatch(coords, 0, coords, 0);
		type = SurfaceIterator.CUBIC_PATCH;
		// fall through
	    case SurfaceIterator.CUBIC_PATCH:
		if (fast && nearlyFlat(limit, type, coords, coords1, coords2,
				       coords3, coords4)) {
		    for (int i = 0; i < fncp; i++) {
			double u = fargsCP[i];
			for (int j = 0; j < fncp; j++) {
			    double v = fargsCP[j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    vect[0] = (xf == null)? 0.0:
				xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[1] = (yf == null)? 0.0:
				yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[2] = (zf == null)? 0.0:
				zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    double d =
				VectorOps.dotCrossProduct(vect, tmpu, tmpv);
			    adder.add(d*fweightsCP[i][j]);
			    // adderCP.add(d*weightsCP[i][j]);
			}
		    }
		} else {
		    for (int i = 0; i < ncp; i++) {
			double u = argsCP[i];
			for (int j = 0; j < ncp; j++) {
			    double v = argsCP[j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    vect[0] = (xf == null)? 0.0:
				xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[1] = (yf == null)? 0.0:
				yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    vect[2] = (zf == null)? 0.0:
				zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
			    double d =
				VectorOps.dotCrossProduct(vect, tmpu, tmpv);
			    adder.add(d*weightsCP[i][j]);
			    // adderCP.add(d*weightsCP[i][j]);
			}
		    }
		}
		break;
	    }
	    si.next();
	}
	/*
	System.out.println("PT: " + adderPT.getSum()
			   +", CT: " + adderCT.getSum()
			   +", CP: " + adderCP.getSum()
			   + ", total = " + (adderPT.getSum()
					     + adderCT.getSum()
					     + adderCP.getSum()));
	*/
    }

    private static double nprocFactor = 1.0;
    private static int nprocLimit = 5;

    private static int MIN_PARALLEL_SIZE =
	SurfaceConstants.MIN_PARALLEL_SIZE;

    // case were the scalar function is a constant
    private static final int MIN_PARALLEL_SIZE_A =
	SurfaceConstants.MIN_PARALLEL_SIZE_A;

    // case where the vector functions are linear in x, y, and z.
    private static final int MIN_PARALLEL_SIZE_V =
	SurfaceConstants.MIN_PARALLEL_SIZE_V;


    private int getNProc(int size) {
	int nproc = Runtime.getRuntime().availableProcessors();
	int maxpts = ncp*ncp*size;
	// int bestpts = 32;
	int bestpts;
	if (sf != null) {
	    bestpts = nscalar*nscalar*MIN_PARALLEL_SIZE_A;
	} else {
	    // 6 = (2*(3+2) + 1 + 1)/2
	    bestpts = 6*6*MIN_PARALLEL_SIZE_V;
	}
	int want = (maxpts/bestpts) + 1;
	if (want > (nproc - 1)) {
	    return nproc - 1;
	} else {
	    return want;
	}
	/*
	nproc = (int)Math.round(nproc*nprocFactor);
	if (nproc > nprocLimit) nproc = nprocLimit;
	return nproc;
	*/
    }


    /**
     * Perform a surface integral with the surface specified by a surface
     * iterator.
     * @param si the surface iterator
     * @return the surface integral
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     */
    public double integrate(SurfaceIterator si)
	throws IllegalArgumentException
    {
	return integrate(si, /*false*/true,
			 ((sf != null)?  MIN_PARALLEL_SIZE_A:
			  MIN_PARALLEL_SIZE_V));
    }
    /**
     * Perform a surface integral with the surface specified by a surface
     * iterator, specifying if the integral should be done in parallel.
     * @param si the surface iterator
     * @param parallel true if the integral should be done in parallel;
     *        false if it should be done sequentially
     * @param size the number of surface elements that the iterator provides
     * @return the surface integral
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     */
    public double integrate(SurfaceIterator si, boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (parallel) {
	    int nproc = getNProc(size);
	    if (nproc < 2) return integrate(si, false, 0);
	    boolean scalar;
	    if (sf == null) {
		scalar = false;
		if (xf == null && yf == null && zf == null) return 0.0;
		if (!si.isOriented()) {
		    throw new IllegalArgumentException(errorMsg("notOriented"));
		}
	    }
	    final double[] subtotals = new double[nproc];
	    final SurfaceIteratorSplitter splitter =
		new SurfaceIteratorSplitter(nproc, si);
	    nproc--;
	    Thread[] threads = new Thread[nproc];
	    for (int i = 0; i < nproc; i++) {
		final int index = i;
		threads[i] = new Thread(() -> {
			subtotals[index] = SurfaceIntegral.this.integrate
			    (splitter.getSurfaceIterator(index), false, 0);
		});
		threads[i].start();
	    }
	    subtotals[nproc] = integrate(splitter.getSurfaceIterator(nproc),
					 false, 0);
	    try {
		for (int i = 0; i < nproc; i++) {
		    threads[i].join();
		}
	    } catch (InterruptedException ei) {
		splitter.interrupt();
	    }
	    double sum = 0.0;
	    for (int i = 0; i <= nproc; i++) {
		sum += subtotals[i];
	    }
	    return sum;
	} else {
	    Adder adder = new Adder.Kahan();
	    if (sf != null) {
		integrateS(adder, si);
	    } else if (xf != null || yf != null || zf != null) {
		if (!si.isOriented()) {
		    throw new IllegalArgumentException(errorMsg("notOriented"));
		}
		integrateV(adder, si);
	    }
	    return adder.getSum();
	}
    }

    /**
     * Perform a surface integral with the surface specified by a 3D shape.
     * @param surface the surface over which the integral is performed
     * @return the value of the surface integral
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     */
    public double integrate(Shape3D surface)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (surface instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)surface;
	    size = s.size();
	    parallel = (size >= ((sf != null)?  MIN_PARALLEL_SIZE_A:
				 MIN_PARALLEL_SIZE_V));
	} else {
	    size = ((sf != null)?  MIN_PARALLEL_SIZE_A:
		    MIN_PARALLEL_SIZE_V);
	}
	return integrate(surface.getSurfaceIterator(null), parallel, size);
    }

    /**
     * Perform a surface integral with the surface specified by a 3D
     * shape, specifying if the integral should be done in parallel.
     * @param surface the surface over which the integral is performed
     * @param parallel true if the integral should be done in parallel;
     *        false if it should be done sequentially
     * @return the value of the surface integral
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     */
    public double integrate(Shape3D surface, boolean parallel)
	throws IllegalArgumentException
    {
	int size;
	if (surface instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)surface;
	    size = s.size();
	} else {
	    size = ((sf != null)?  MIN_PARALLEL_SIZE_A:
		    MIN_PARALLEL_SIZE_V);
	}
	return integrate(surface.getSurfaceIterator(null), parallel, size);
    }

    /**
     * Perform a surface integral with the surface specified by a 3D shape
     * modified by a transform.
     * The actual surface is not modified.
     * @param surface the surface
     * @param transform the transform to apply to the surface
     * @return the value of the surface integral over the transformed
     *         surface
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     * @see Shape3D#getSurfaceIterator(Transform3D)
     */
    public double integrate(Shape3D surface, Transform3D transform)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (surface instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)surface;
	    size = s.size();
	    parallel = (size >= ((sf != null)?  MIN_PARALLEL_SIZE_A:
				 MIN_PARALLEL_SIZE_V));
	} else {
	    size = ((sf != null)?  MIN_PARALLEL_SIZE_A: MIN_PARALLEL_SIZE_V);
	}
	return integrate(surface.getSurfaceIterator(transform), parallel, size);
    }

    /**
     * Perform a surface integral with the surface specified by a 3D
     * shape modified by a transform, specifying if the integral
     * should be done in parallel.
     * The actual surface is not modified.
     * @param surface the surface
     * @param transform the transform to apply to the surface
     * @param parallel true if the integral should be done in parallel;
     *        false if it should be done sequentially
     * @param size an estimate of the number of elements (triangles and
     *        cubic patches) comprising the surface
     * @return the value of the surface integral over the transformed
     *         surface
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     * @see Shape3D#getSurfaceIterator(Transform3D)
     */
    public double integrate(Shape3D surface, Transform3D transform,
			    boolean parallel, int size)
	throws IllegalArgumentException
    {
	return integrate(surface.getSurfaceIterator(transform), parallel,
			 size);
    }

    /**
     * Perform a surface integral with the surface specified by a 3D shape
     * modified by a transform, splitting each triangle or patch.
     * Providing a level is useful when the transform is not an affine
     * transform.
     * The actual surface is not modified.
     * @param surface the surface
     * @param transform the transform to apply to the surface
     * @param level The number of partitioning levels (each level splits
     *              each patch or triangle into quarters)
     * @return the value of the surface integral over the transformed
     *         surface
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     * @see Shape3D#getSurfaceIterator(Transform3D,int)
     */
    public double integrate(Shape3D surface,
			    Transform3D transform, int level)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (surface instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)surface;
	    size = s.size();
	    if (level > 0) {
		long sz = size;
		for (int i = 0; i < level; i++) {
		    sz *= 4;
		    if (sz > Integer.MAX_VALUE) {
			sz = Integer.MAX_VALUE;
		    }
		    size = (int) sz;
		}
	    }
	    parallel = (size >= ((sf != null)? MIN_PARALLEL_SIZE_A:
			      MIN_PARALLEL_SIZE_V));
	} else {
	    size = ((sf != null)? MIN_PARALLEL_SIZE_A:
		    MIN_PARALLEL_SIZE_V);
	}
	return integrate(surface.getSurfaceIterator(transform, level),
			 parallel, size);
    }

    /**
     * Perform a surface integral with the surface specified by a 3D
     * shape modified by a transform, splitting each triangle or
     * patch, specifying if the integral should be done in parallel.
     * Providing a level is useful when the transform is not an affine
     * transform.
     * The actual surface is not modified.
     * @param surface the surface
     * @param transform the transform to apply to the surface
     * @param level The number of partitioning levels (each level splits
     *              each patch or triangle into quarters)
     * @param parallel true if the integral should be done in parallel;
     *        false if it should be done sequentially
     * @param size an estimate of the number of elements (triangles and
     *        cubic patches) comprising the surface
     * @return the value of the surface integral over the transformed
     *         surface
     * @exception IllegalArgumentException the surface should have been
     *            oriented to integrate a vector field
     * @see Shape3D#getSurfaceIterator(Transform3D,int)
     */
    public double integrate(Shape3D surface,
			    Transform3D transform, int level,
			    boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (level > 0) {
	    long sz = size;
	    for (int i = 0; i < level; i++) {
		sz *= 4;
		if (sz > Integer.MAX_VALUE) {
		    sz = Integer.MAX_VALUE;
		}
	    }
	    size = (int) sz;
	}
	return integrate(surface.getSurfaceIterator(transform, level),
			 parallel, size);
    }


    /**
     * Class to allow multiple surface integrals to share parts of the
     * computation, allowing them to be completed faster.
     * In some cases (e.g., computing moments), the vector fields being
     * integrated are easily computed given (x, y, z) coordinates. This
     * computation is much cheaper in terms of CPU time than the computation
     * of tangent vectors and the x, y, and z coordinates at each point on
     * the surface.
     * <P>
     * All of the surface integrals must have been constructed with
     * the same parameters, and all must be of the same type (scalar or
     * vector) if this class is used.
     * <P>
     * In one test, with using the Batched class, the running time for
     * computing moments was over 5 seconds. Use of the Batched class
     * reduced this to just over 1 second.
     */
    public static class Batched {

	static String errorMsg(String key, Object... args) {
	    return GeomErrorMsg.errorMsg(key, args);
	}

	boolean isScalar;

	int ncp;			// cubic patch
	int nct;			// cubic triangle
	int npt;			// planar triangle

	int fncp;			// cubic patch (fast case)
	int fnct;			// cubic triangle (fast case)

	double[][] weightsCP;
	double[] argsCP;

	double[][] weightsCT;
	double[] argsCTu;
	double[][] argsCTv;

	double[][] weightsPT;
	double[] argsPTu;
	double[][] argsPTv;

	boolean fast = false;
	double limit = -1.0;

	double[][] fweightsCP;
	double[] fargsCP;

	double[][] fweightsCT;
	double[] fargsCTu;
	double[][] fargsCTv;

	SurfaceIntegral[] siarray = null;

	/**
	 * Constructor.
	 * @param si a surface integral
	 * @param rest additional surface integrals
	 */
	public Batched(SurfaceIntegral si, SurfaceIntegral... rest) {
	    siarray = new SurfaceIntegral[1+rest.length];
	    this.ncp = si.ncp;
	    this.nct = si.nct;
	    this.npt = si.npt;
	    this.fncp = si.fncp;
	    this.fnct = si.fnct;
	    this.weightsCP = si.weightsCP;
	    this.argsCP = si.argsCP;
	    this.weightsCT = si.weightsCT;
	    this.argsCTu = si.argsCTu;
	    this.argsCTv = si.argsCTv;
	    this.weightsPT = si.weightsPT;
	    this.argsPTu = si.argsPTu;
	    this.argsPTv = si.argsPTv;
	    this.fast = si.fast;
	    this.limit = si.limit;
	    this.fweightsCP = si.fweightsCP;
	    this.fargsCP = si.fargsCP;
	    this.fweightsCT = si.fweightsCT;
	    this.fargsCTu = si.fargsCTu;
	    this.fargsCTv = si.fargsCTv;
	    isScalar = (si.sf != null);
	    int ind = 0;
	    siarray[ind++] = si;
	    for (SurfaceIntegral rsi: rest) {
		if (this.ncp != rsi.ncp
		    || this.nct != rsi.nct
		    || this.npt != rsi.npt
		    || this.fncp != rsi.fncp
		    || this.fnct != rsi.fnct) {
		    throw new
			IllegalArgumentException(errorMsg("surfaceIntegral"));
		}
		if (!((si.sf == null && rsi.sf == null)
		      ||(si.sf != null && rsi.sf != null))) {
		    throw new
			IllegalArgumentException(errorMsg("surfaceIntegral"));
		}
		siarray[ind++] = rsi;
	    }
	}

	private void integrateS(Adder[] adders, SurfaceIterator si) {
	    double[] coords = new double[48];
	    double[] tmpu = new double[3];
	    double[] tmpv = new double[3];
	    double[] tmpr = new double[3];
	    double[] cp = new double[3];

	    double[] coords1, coords2, coords3, coords4;
	    if (fast) {
		coords1 = new double[3];
		coords2 = new double[3];
		coords3 = new double[3];
		coords4 = new double[3];
	    } else {
		coords1 = null;
		coords2 = null;
		coords3 = null;
		coords4 = null;
	    }

	    while(si.isDone() == false) {
		int type;
		switch(type = si.currentSegment(coords)) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    for (int i = 0; i < npt; i++) {
			double u = argsPTu[i];
			for (int j = 0; j < weightsPT[i].length; j++) {
			    double v = argsPTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    VectorOps.crossProduct(cp, tmpu, tmpv);
			    double norm = VectorOps.norm(cp);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    for (int k = 0; k < siarray.length; k++) {
				double value = siarray[k].sf.valueAt(tmpr[0],
								     tmpr[1],
								     tmpr[2]);
				adders[k].add(norm*value*weightsPT[i][j]);
			    }
			}
		    }
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (fast && nearlyFlat(limit, type, coords,
					   coords1, coords2,
					   coords3, coords4)) {
			for (int i = 0; i < fnct; i++) {
			    double u = fargsCTu[i];
			    for (int j = 0; j < fweightsCT[i].length; j++) {
				double v = fargsCTv[i][j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				VectorOps.crossProduct(cp, tmpu, tmpv);
				double norm = VectorOps.norm(cp);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				for (int k = 0; k <siarray.length; k++) {
				    double value =
					siarray[k].sf.valueAt(tmpr[0],
							      tmpr[1],
							      tmpr[2]);
				    adders[k].add(norm*value*fweightsCT[i][j]);
				}
			    }
			}
		    } else {
			for (int i = 0; i < nct; i++) {
			    double u = argsCTu[i];
			    for (int j = 0; j < weightsCT[i].length; j++) {
				double v = argsCTv[i][j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				VectorOps.crossProduct(cp, tmpu, tmpv);
				double norm = VectorOps.norm(cp);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				for (int k = 0; k < siarray.length; k++) {
				    double value =
					siarray[k].sf.valueAt(tmpr[0],
							      tmpr[1],
							      tmpr[2]);
				    adders[k].add(norm*value*weightsCT[i][j]);
				}
			    }
			}
		    }
		    break;
	    case SurfaceIterator.CUBIC_VERTEX:
		Shape3DHelper.cubicVertexToPatch(coords, 0, coords, 0);
		type = SurfaceIterator.CUBIC_PATCH;
		// fall through
		case SurfaceIterator.CUBIC_PATCH:
		    if (fast && nearlyFlat(limit, type, coords,
					   coords1, coords2,
					   coords3, coords4)) {
			for (int i = 0; i < fncp; i++) {
			    double u = fargsCP[i];
			    for (int j = 0; j < fncp; j++) {
				double v = fargsCP[j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				VectorOps.crossProduct(cp, tmpu, tmpv);
				double norm = VectorOps.norm(cp);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				for (int k = 0; k < siarray.length; k++) {
				    double value =
					siarray[k].sf.valueAt(tmpr[0],
							      tmpr[1],
							      tmpr[2]);
				    adders[k].add(norm*value*fweightsCP[i][j]);
				}
			    }
			}
		    } else {
			for (int i = 0; i < ncp; i++) {
			    double u = argsCP[i];
			    for (int j = 0; j < ncp; j++) {
				double v = argsCP[j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				VectorOps.crossProduct(cp, tmpu, tmpv);
				double norm = VectorOps.norm(cp);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				for (int k = 0; k < siarray.length; k++) {
				    double value =
					siarray[k].sf.valueAt(tmpr[0],
							      tmpr[1],
							      tmpr[2]);
				    adders[k].add(norm*value*weightsCP[i][j]);
				}
			    }
			}
		    }
		    break;
		}
		si.next();
	    }
	}

	private void integrateV(Adder[] adders, SurfaceIterator si)
	{
	    if (!si.isOriented()) {
		throw new IllegalArgumentException(errorMsg("notOriented"));
	    }

	    double[] coords = new double[48];
	    double[] tmpu = new double[3];
	    double[] tmpv = new double[3];
	    double[] tmpr = new double[3];
	    double[] vect = new double[3];
	    double[] cp = new double[3];
	    /*
	      Adder.Kahan adderPT = new Adder.Kahan();
	      Adder.Kahan adderCT = new Adder.Kahan();
	      Adder.Kahan adderCP = new Adder.Kahan();
	    */

	    double[] coords1, coords2, coords3, coords4;
	    if (fast) {
		coords1 = new double[3];
		coords2 = new double[3];
		coords3 = new double[3];
		coords4 = new double[3];
	    } else {
		coords1 = new double[3];
		coords2 = null;
		coords3 = null;
		coords4 = null;
	    }

	    while(si.isDone() == false) {
		int type;
		switch(type = si.currentSegment(coords)) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    for (int i = 0; i < npt; i++) {
			double u = argsPTu[i];
			for (int j = 0; j < weightsPT[i].length; j++) {
			    double v = argsPTv[i][j];
			    SurfaceOps.uTangent(tmpu, type, coords, u, v);
			    SurfaceOps.vTangent(tmpv, type, coords, u, v);
			    SurfaceOps.segmentValue(tmpr, type, coords, u, v);
			    VectorOps.crossProduct(coords1, tmpu, tmpv);
			    for (int k = 0; k < siarray.length; k++) {
				SurfaceIntegral s = siarray[k];
				vect[0] = (s.xf == null)? 0.0:
				    s.xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				vect[1] = (s.yf == null)? 0.0:
				    s.yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				vect[2] = (s.zf == null)? 0.0:
				    s.zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				double d = VectorOps.dotProduct(vect, coords1);
				adders[k].add(d*weightsPT[i][j]);
			    }
			}
		    }
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (fast && nearlyFlat(limit, type, coords,
					   coords1, coords2,
					   coords3, coords4)) {
			for (int i = 0; i < fnct; i++) {
			    double u = fargsCTu[i];
			    for (int j = 0; j < fweightsCT[i].length; j++) {
				double v = fargsCTv[i][j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				VectorOps.crossProduct(coords1, tmpu, tmpv);
				for (int k = 0; k < siarray.length; k++) {
				    SurfaceIntegral s = siarray[k];
				    vect[0] = (s.xf == null)? 0.0:
					s.xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[1] = (s.yf == null)? 0.0:
					s.yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[2] = (s.zf == null)? 0.0:
					s.zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    double d =
					VectorOps.dotProduct(vect, coords1);
				    adders[k].add(d*fweightsCT[i][j]);
				}
			    }
			}
		    } else {
			for (int i = 0; i < nct; i++) {
			    double u = argsCTu[i];
			    for (int j = 0; j < weightsCT[i].length; j++) {
				double v = argsCTv[i][j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				VectorOps.crossProduct(coords1, tmpu, tmpv);
				for (int k = 0; k < siarray.length; k++) {
				    SurfaceIntegral s = siarray[k];
				    vect[0] = (s.xf == null)? 0.0:
					s.xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[1] = (s.yf == null)? 0.0:
					s.yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[2] = (s.zf == null)? 0.0:
					s.zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    double d =
					VectorOps.dotProduct(vect, coords1);
				    adders[k].add(d*weightsCT[i][j]);
				}
			    }
			}
		    }
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    Shape3DHelper.cubicVertexToPatch(coords, 0, coords, 0);
		    type = SurfaceIterator.CUBIC_PATCH;
		    // fall through
		case SurfaceIterator.CUBIC_PATCH:
		    if (fast && nearlyFlat(limit, type, coords,
					   coords1, coords2,
					   coords3, coords4)) {
			for (int i = 0; i < fncp; i++) {
			    double u = fargsCP[i];
			    for (int j = 0; j < fncp; j++) {
				double v = fargsCP[j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				VectorOps.crossProduct(coords1, tmpu, tmpv);
				for (int k = 0; k < siarray.length; k++) {
				    SurfaceIntegral s = siarray[k];
				    vect[0] = (s.xf == null)? 0.0:
					s.xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[1] = (s.yf == null)? 0.0:
					s.yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[2] = (s.zf == null)? 0.0:
					s.zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    double d =
					VectorOps.dotProduct(vect,coords1);
				    adders[k].add(d*fweightsCP[i][j]);
				}
			    }
			}
		    } else {
			for (int i = 0; i < ncp; i++) {
			    double u = argsCP[i];
			    for (int j = 0; j < ncp; j++) {
				double v = argsCP[j];
				SurfaceOps.uTangent(tmpu, type, coords, u, v);
				SurfaceOps.vTangent(tmpv, type, coords, u, v);
				SurfaceOps.segmentValue(tmpr, type,
						       coords, u, v);
				VectorOps.crossProduct(coords1, tmpu, tmpv);
				for (int k = 0; k < siarray.length; k++) {
				    SurfaceIntegral s = siarray[k];
				    vect[0] = (s.xf == null)? 0.0:
					s.xf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[1] = (s.yf == null)? 0.0:
					s.yf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    vect[2] = (s.zf == null)? 0.0:
					s.zf.valueAt(tmpr[0], tmpr[1], tmpr[2]);
				    double d =
					VectorOps.dotProduct(vect,coords1);
				    adders[k].add(d*weightsCP[i][j]);
				}
			    }
			}
		    }
		    break;
		}
		si.next();
	    }
	    /*
	      System.out.println("PT: " + adderPT.getSum()
	      +", CT: " + adderCT.getSum()
	      +", CP: " + adderCP.getSum()
	      + ", total = " + (adderPT.getSum()
	      + adderCT.getSum()
	      + adderCP.getSum()));
	    */
	}

	// case were the scalar function is a constant
	private static final int MIN_PARALLEL_SIZE_A =
	    SurfaceConstants.MIN_PARALLEL_SIZE_A;

	// case where the vector functions are linear in x, y, and z.
	private static final int MIN_PARALLEL_SIZE_V =
	    SurfaceConstants.MIN_PARALLEL_SIZE_V;


	private static final int MIN_PARALLEL_SIZE =
	    SurfaceConstants.MIN_PARALLEL_SIZE;

	private int getNProc(int size) {
	    int nproc = Runtime.getRuntime().availableProcessors();
	    int maxpts = ncp*ncp*size;
	    // int bestpts = 32;
	    int bestpts;
	    if (isScalar) {
		bestpts = nscalar*nscalar*MIN_PARALLEL_SIZE_A;
	    } else {
		// 6 = (2*(3+2) + 1 + 1)/2
		bestpts = 6*6*MIN_PARALLEL_SIZE_V;
	    }
	    int want = (maxpts/bestpts) + 1;
	    if (want > (nproc - 1)) {
		return nproc - 1;
	    } else {
		return want;
	    }
	}

	/**
	 * Perform a series of surface integrals with the surface specified
	 * by a surface iterator.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param si the surface iterator
	 * @return the values of the surface integrals
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 */
	public double[] integrate(SurfaceIterator si)
	    throws IllegalArgumentException
	{
	    return integrate(si, /*false*/ true,
			     ((isScalar)?  MIN_PARALLEL_SIZE_A:
			      MIN_PARALLEL_SIZE_V));
	}
	/**
	 * Perform a series of surface integrals with the surface
	 * specified by a surface iterator, also specifying if the
	 * integral should be done in parallel.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param si the surface iterator
	 * @param parallel true if the integral should be done in parallel;
	 *        false if it should be done sequentially
	 * @param size an estimate of the number of elements (triangles and
	 *        cubic patches) comprising the surface
	 * @return the values of the surface integrals
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 */
	public double[] integrate(SurfaceIterator si, boolean parallel,
				  int size)
	    throws IllegalArgumentException
	{
	    if (parallel) {
		int nproc = getNProc(size);
		if (nproc < 2) return integrate(si, false, 0);
		if (!isScalar && !si.isOriented()) {
			throw new
			    IllegalArgumentException(errorMsg("notOriented"));
		}
		final double[][] subtotals = new double[nproc][];
		final SurfaceIteratorSplitter splitter =
		    new SurfaceIteratorSplitter(nproc, si);
		nproc--;
		Thread[] threads = new Thread[nproc];
		for (int i = 0; i < nproc; i++) {
		    final int index = i;
		    threads[i] = new Thread(() -> {
			    subtotals[index] = Batched.this.integrate
				(splitter.getSurfaceIterator(index), false, 0);
		    });
		    threads[i].start();
		}
		subtotals[nproc] = integrate(splitter.getSurfaceIterator(nproc),
					     false, 0);
		try {
		    for (int i = 0; i < nproc; i++) {
			threads[i].join();
		    }
		} catch (InterruptedException ei) {
		    splitter.interrupt();
		}
		double[] results = new double[siarray.length];
		for (int i = 0; i <= nproc; i++) {
		    for (int j = 0; j < results.length; j++) {
			results[j] += subtotals[i][j];
		    }
		}
		return results;
	    } else {
		Adder[] adders = new Adder.Kahan[siarray.length];
		for (int i = 0; i < adders.length; i++) {
		    adders[i] = new Adder.Kahan();
		}
		if (isScalar) {
		    integrateS(adders, si);
		} else {
		    if (!si.isOriented()) {
			throw new
			    IllegalArgumentException(errorMsg("notOriented"));
		    }
		    integrateV(adders, si);
		}
		double[] results = new double[siarray.length];
		for (int i = 0; i < results.length; i++) {
		    results[i] = adders[i].getSum();
		}
		return results;
	    }
	}

	/**
	 * Perform a series of surface integrals with the surface specified
	 * by a 3D shape.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param surface the surface over which the integral is performed
	 * @return the values of the surface integrals
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 */
	public double[] integrate(Shape3D surface)
	    throws IllegalArgumentException
	{
	    boolean parallel = true;
	    int size;
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		size = s.size();
		parallel = (size >= ((isScalar)?  MIN_PARALLEL_SIZE_A:
				     MIN_PARALLEL_SIZE_V));
	    } else {
		size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			MIN_PARALLEL_SIZE_V);
	    }
	    return integrate(surface.getSurfaceIterator(null), parallel, size);
	}

	/**
	 * Perform a series of surface integrals with the surface
	 * specified by a 3D shape, specifying if the integral should
	 * be done in parallel.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param surface the surface over which the integral is performed
	 * @param parallel true if the integral should be done in parallel;
	 *        false if it should be done sequentially
	 * @return the values of the surface integrals
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 */
	public double[] integrate(Shape3D surface, boolean parallel)
	    throws IllegalArgumentException
	 {
	     int size;
	     if (surface instanceof SurfaceOps) {
		 SurfaceOps s = (SurfaceOps)surface;
		 size = s.size();
	     } else {
		 size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			 MIN_PARALLEL_SIZE_V);
	     }
	     return integrate(surface.getSurfaceIterator(null), parallel, size);
	 }

	/**
	 * Perform a series of surface integrals with the surface specified
	 * by a 3D shape modified by a transform.
	 * The actual surface is not modified.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param surface the surface
	 * @param transform the transform to apply to the surface
	 * @return the values of the surface integrals over the transformed
	 *         surface
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 * @see Shape3D#getSurfaceIterator(Transform3D)
	 */
	public double[] integrate(Shape3D surface, Transform3D transform)
	    throws IllegalArgumentException
	{
	    boolean parallel = true;
	    int size;
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		size = s.size();
		parallel = (size >= ((isScalar)?  MIN_PARALLEL_SIZE_A:
				     MIN_PARALLEL_SIZE_V));
	    } else {
		size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			MIN_PARALLEL_SIZE_V);
	}
	    return integrate(surface.getSurfaceIterator(transform), parallel,
			     size);
	}

	/**
	 * Perform a series of surface integrals with the surface
	 * specified by a 3D shape modified by a transform, specifying
	 * if the integral should be done in parallel.
	 * The actual surface is not modified.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * @param surface the surface
	 * @param transform the transform to apply to the surface
	 * @param parallel true if the integral should be done in parallel;
	 *        false if it should be done sequentially
	 * @return the values of the surface integrals over the transformed
	 *         surface
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 * @see Shape3D#getSurfaceIterator(Transform3D)
	 */
	public double[] integrate(Shape3D surface, Transform3D transform,
				  boolean parallel)
	    throws IllegalArgumentException
	{
	    int size;
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		size = s.size();
	    } else {
		size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			MIN_PARALLEL_SIZE_V);
	    }
	    return integrate(surface.getSurfaceIterator(transform), parallel,
			     size);
	}

	/**
	 * Perform a series of surface integrals with the surface specified
	 * by a 3D shape modified by a transform, splitting each triangle
	 * or patch.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * Providing a level is useful when the transform is not an affine
	 * transform.
	 * The actual surface is not modified.
	 * @param surface the surface
	 * @param transform the transform to apply to the surface
	 * @param level The number of partitioning levels (each level splits
	 *              each patch or triangle into quarters)
	 * @return the values of the surface integrals over the transformed
	 *         surface
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 * @see Shape3D#getSurfaceIterator(Transform3D,int)
	 */
	public double[] integrate(Shape3D surface,
				Transform3D transform, int level)
	    throws IllegalArgumentException
	{
	    boolean parallel = true;
	    int size;
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		size = s.size();
		if (level > 0) {
		    long sz = size;
		    for (int i = 0; i < level; i++) {
			sz *= 4;
			if (sz > Integer.MAX_VALUE) {
			    sz = Integer.MAX_VALUE;
			}
			size = (int) sz;
		    }
		}
	    } else {
		size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			MIN_PARALLEL_SIZE_V);
		parallel = (size >= ((isScalar)?  MIN_PARALLEL_SIZE_A:
				     MIN_PARALLEL_SIZE_V));
	    }
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		parallel = (s.size() >= MIN_PARALLEL_SIZE);
	    }
	    return integrate(surface.getSurfaceIterator(transform, level),
			     parallel, size);
	}

	/**
	 * Perform a series of surface integrals with the surface
	 * specified by a 3D shape modified by a transform, splitting
	 * each triangle or patch, specifying if the integral should
	 * be done in parallel.
	 * The values returned are in the same order as the corresponding
	 * {@link SurfaceIntegral} object appears in the constructor.
	 * Providing a level is useful when the transform is not an affine
	 * transform.
	 * The actual surface is not modified.
	 * @param surface the surface
	 * @param transform the transform to apply to the surface
	 * @param level The number of partitioning levels (each level splits
	 *              each patch or triangle into quarters)
	 * @param parallel true if the integral should be done in parallel;
	 *        false if it should be done sequentially
	 * @return the values of the surface integrals over the transformed
	 *         surface
	 * @exception IllegalArgumentException the surface should have been
		      oriented to integrate a vector field
	 * @see Shape3D#getSurfaceIterator(Transform3D,int)
	 */
	public double[] integrate(Shape3D surface,
				  Transform3D transform, int level,
				  boolean parallel)
	    throws IllegalArgumentException
	{
	    int size;
	    if (surface instanceof SurfaceOps) {
		SurfaceOps s = (SurfaceOps)surface;
		size = s.size();
		if (level > 0) {
		    long sz = size;
		    for (int i = 0; i < level; i++) {
			sz *=4;
			if (sz > Integer.MAX_VALUE) {
			    sz = Integer.MAX_VALUE;
			}
		    }
		}
	    } else {
		size = ((isScalar)?  MIN_PARALLEL_SIZE_A:
			MIN_PARALLEL_SIZE_V);
	    }
	    return integrate(surface.getSurfaceIterator(transform, level),
			     parallel, size);
	}
    }
}

//  LocalWords:  fdA sdot dA SurfaceIntegral SurfaceIterator xf yf zf
//  LocalWords:  ncp nct npt Kahan adderPT adderCT adderCP weightsPT
//  LocalWords:  weightsCT weightsCP getSum CP si notOriented affine
//  LocalWords:  getSurfaceIterator exbundle eacute zier BLOCKQUOTE
//  LocalWords:  PRE ecute RealValuedFunctThreeOps BezierGrid
