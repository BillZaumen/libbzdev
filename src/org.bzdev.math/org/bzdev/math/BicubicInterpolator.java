package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Bicubic Interpolation class.
 * This class constructs a real-valued function f with two arguments u
 * and v that implements bicubic interpolation: approximation of a
 * function over a rectangular interval using a cubic polynomial. The
 * default range for the interpolation limits both u and v to the
 * interval [0,1], although the implementation will allow values to
 * be computed outside this range.  The interpolation function is
 * the sum over i and j, with both in the range [0,3] of the term
 * a<sub>ij</sub>u<sup>i</sup>v<sup>j</sup>.
 * <P>
 * The algorithm is described in
 * <a href="https://en.wikipedia.org/wiki/Bicubic_interpolation">Bicubic
 * Interpolation</a>.
 *<P>
 * For convenience, a constructor allows coordinates (x, y) to be used
 * where x and y vary between two values, one corresponding to u = 0
 * or v = 0, and the other corresponding to u = 1 or v = 1.
 * <P>
 * The function this class implements is valid on R<sup>2</sup> but is
 * a good approximation over a finite rectangular subset of
 * R<sup>2</sup>. The methods {@link #getDomainMin1()},
 * {@link #getDomainMin2()}, {@link #getDomainMax1()}, and
 * {@link #getDomainMax2()} provide the lower and upper bounds for
 * this rectangle.
 * <P>
 * The algorithm described above requires 16 values to construct the
 * interpolator: the values of f, f<sub>1</sub>, f<sub>2</sub>, and
 * f<sub>12</sub> at the corners of a rectangle, where the subscripts
 * of f denote partial derivatives with respect to the first and
 * second arguments.
 * <P>
 * Specific cases where fewer values are available can be handled as
 * well. If f<sub>12</sub>(1,0), f<sub>12</sub>(0,1) and
 * f<sub>12</sub>(1,1) are missing, one computes the sum over i and j
 * of a<sub>ij</sub>u<sup>i</sup>v<sup>j</sup> where both i and j are
 * in the range [0,3] and where i+j&le;4. This yields the following
 * 13 equations:
 * <UL>
 *  <li> f(0,0) = 0 + a<sub>00</sub>.
 *  <li> f(1,0) = 0 + a<sub>00</sub> + a<sub>10</sub>.
 *       + a<sub>20</sub> + a<sub>30</sub>.
 *  <li> f(0,1) = 0 + a<sub>00</sub> + a<sub>01</sub>
 *       + a<sub>02</sub> + a<sub>03</sub>.
 *  <li> f(1,1) = 0 + a<sub>00</sub> + a<sub>01</sub> + a<sub>02</sub>
 *       + a<sub>03</sub> + a<sub>10</sub> + a<sub>11</sub> + a<sub>12</sub>
 *       + a<sub>13</sub> + a<sub>20</sub> + a<sub>21</sub>
 *       + a<sub>22</sub> + a<sub>30</sub> + a<sub>31</sub>.
 *  <li> f1(0,0) = 0 + a<sub>10</sub>.
 *  <li> f1(1,0) = 0 + a<sub>10</sub> + 2a<sub>20</sub> + 3a<sub>30</sub>.
 *  <li> f(0,1) = 0 + a<sub>10</sub> + a<sub>11</sub>
 *       + a<sub>12</sub> + a<sub>13</sub>.
 *  <li> f1(1,1) = 0 + a<sub>10</sub> + a<sub>11</sub> + a<sub>12</sub>
 *       + a<sub>13</sub> + 2a<sub>20</sub> + 2a<sub>21</sub>
 *       + 2a<sub>22</sub> + 3a<sub>30</sub> + 3a<sub>31</sub>.
 *  <li> f2(0,0) = 0 + a<sub>01</sub>.
 *  <li> f2(1,0) = 0 + a<sub>01</sub> + a<sub>11</sub>
 *       + a<sub>21</sub> + a<sub>31</sub>.
 *  <li> f2(0,1) = 0 + a<sub>01</sub> + 2a<sub>02</sub> + 3a<sub>03</sub>.
 *  <li> f2(1,1) = 0 + a<sub>01</sub> + 2a<sub>02</sub> + 3a<sub>03</sub>
 *       + a<sub>11</sub> + 2a<sub>12</sub> + 3a<sub>13</sub>
 *      + a<sub>21</sub> + 2a<sub>22</sub> + a<sub>31</sub>.
 *  <li> f12(0,0) = 0 + a<sub>11</sub>.
 * </UL>
 * These can be solved for the coefficients a<sub>ij</sub>.
 * <P>
 * A matrix representation of cubic B&eacute;zier patches is described
 * in
 * <a href="http://www.idav.ucdavis.edu/education/CAGDNotes/Matrix-Cubic-Bezier-Patch/Matrix-Cubic-Bezier-Patch.html">A matrix representation of a cubic b&eacute;zier patch</a>.
 * <P>
 * The interpolated value of f(u,v) can be written as
 * <br>
 * [1 u u<sup>2</sup> u<sup>3</sup>] M P M<sup>T</sup> [1 v v<sup>2</sup> v<sup>3</sup>]<sup>T</sup>
 * <br>
 * where M is a constant matrix given in the citation and P is a matrix
 * of control points.  The interpolated value is
 * P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub> summed over i and j
 * from 0 to 3 inclusive.  The functions B<sub>k,n</sub>(t) are Bernstein
 * polynomials defined as C(n,i)t<sup>i</sup>(1-t)<sup>n-i</sup>.
 * Because this interpolator produces a real
 * value, the control points are one dimensional (for the multidimensional
 * case, each component is handled separately).
 */
public class BicubicInterpolator extends RealValuedFunctionTwo {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private static final double M[][] = {{ 1.0,  0.0,  0.0,  0.0},
					 {-3.0,  3.0,  0.0,  0.0},
					 { 3.0, -6.0,  3.0,  0.0},
					 {-1.0,  3.0, -3.0,  1.0}};

    // MI9 is 9 times the inverse of M
    private static final double MI9[][] = {{9.0, 0.0, 0.0, 0.0},
					   {9.0, 3.0, 0.0, 0.0},
					   {9.0, 6.0, 3.0, 0.0},
					   {9.0, 9.0, 9.0, 9.0}};

    private static final double MT[][] = {{1.0, -3.0,  3.0, -1.0},
					  {0.0,  3.0, -6.0,  3.0},
					  {0.0,  0.0,  3.0, -3.0},
					  {0.0,  0.0,  0.0,  1.0}};

    // MTI9 is 9 times the inverse of MT
    private static final double MTI9[][] = {{9.0, 9.0, 9.0, 9.0},
					    {0.0, 3.0, 6.0, 9.0},
					    {0.0, 0.0, 3.0, 9.0},
					    {0.0, 0.0, 0.0, 9.0}};

    /**
     * Get the B&eacute;zier control points for this bicubic interpolator.
     * This is a one-dimensional case, so the control points are just
     * numbers.
     * @return a matrix containing the control points
     */
    public double[][] getControlPoints() {
	// compute MI9*A*MTI9 and divide by 81=9*9.
	double[][] result = new double[4][4];
	double[][] temp = new double[4][4];
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 4; k++) {
		    // sum += MI9[i][k]*a[k][j];
		    sum += MI9[i][k]*a[k*4+j];
		}
		temp[i][j] = sum;
	    }
	}
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 4; k++) {
		    sum += temp[i][k]*MTI9[k][j];
		}
		result[i][j] = sum/81.0;
	    }
	}
	return result;
    }

    /**
     * Get the B&eacute;zier control points for this bicubic interpolator.
     * This is a one-dimensional case, so the control points are just
     * numbers.
     * <P>
     * When colOrder is true, the ordering is the one used in the geom package.
     * @param colOrder true if the elements of each column are adjacent in
     *        the the array; false if the elements of each row are adjacent
     * @return a matrix containing the control points
     */
    public double[] getControlPoints(boolean colOrder) {
	// compute MI9*A*MTI9 and divide by 81=9*9.
	double[] result = new double[16];
	double[][] temp = new double[4][4];
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 4; k++) {
		    // sum += MI9[i][k]*a[k][j];
		    sum += MI9[i][k]*a[k*4+j];
		}
		temp[i][j] = sum;
	    }
	}
	if (colOrder) {
	    for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
		    double sum = 0.0;
		    for (int k = 0; k < 4; k++) {
			sum += temp[i][k]*MTI9[k][j];
		    }
		    result[i + 4*j] = sum/81.0;
		}
	    }
	} else {
	    for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
		    double sum = 0.0;
		    for (int k = 0; k < 4; k++) {
			sum += temp[i][k]*MTI9[k][j];
		    }
		    result[i*4 + j] = sum/81.0;
		}
	    }
	}
	return result;
    }


    private static final double  matrix[][] = {
	{ 1.0,  0.0,  0.0,  0.0, 0.0, 0.0, 0.0, 0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{-3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},

	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	 -3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0},

	{-3.0,  0.0,  3.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	 -2.0,  0.0, -1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0, -3.0,  0.0,  3.0,  0.0,
	  0.0,  0.0,  0.0,  0.0, -2.0,  0.0, -1.0,  0.0},
	{ 9.0, -9.0, -9.0,  9.0,  6.0,  3.0, -6.0, -3.0,
	  6.0, -6.0,  3.0, -3.0,  4.0,  2.0,  2.0,  1.0},
	{-6.0,  6.0,  6.0, -6.0, -3.0, -3.0,  3.0,  3.0,
	 -4.0,  4.0, -2.0,  2.0, -2.0, -2.0, -1.0, -1.0},

	{ 2.0,  0.0, -2.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  2.0,  0.0, -2.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  1.0,  0.0},
	{-6.0,  6.0,  6.0, -6.0, -4.0, -2.0,  4.0, 2.0,
	 -3.0,  3.0, -3.0,  3.0, -2.0, -1.0, -2.0, -1.0},
	{ 4.0, -4.0, -4.0,  4.0,  2.0,  2.0, -2.0, -2.0,
	  2.0, -2.0,  2.0, -2.0,  1.0,  1.0,  1.0,  1.0}
    };

     double shortMatrix[][] = {
	{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,},
	{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0,},
	{-3, 0, 3, 0, 0, 0, 0, 0,-2, 0,-1, 0, 0,},
	{ 2, 0,-2, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0,},
	{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,},
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,},
	{ 2,-2,-2, 2,-1, 0, 1, 0, 1,-1, 1,-1,-1,},
	{-2, 2, 2,-2, 0, 0, 0, 0,-1, 1,-1, 1, 0,},
	{-3, 3, 0, 0,-2,-1, 0, 0, 0, 0, 0, 0, 0,},
	{ 2,-2,-2, 2, 1, 1,-1,-1,-1, 1, 0, 0,-1,},
	{ 1,-1,-1, 1, 1, 0,-1, 0, 1,-1, 0, 0, 1,},
	{ 2,-2, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0,},
	{-2, 2, 2,-2,-1,-1, 1, 1, 0, 0, 0, 0, 0,},
    };

    int firstShortInd[] =
    {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3};
    int secondShortInd[] =
    {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 0, 1};

    // double a[][] = new double[4][4];
    double a[] = new double[16];

    // min/max refers to the normalized parameter u, which is in
    // the range [0, 1].
    double xmin = 0.0;
    double xmax = 1.0;
    double ymin = 0.0;
    double ymax = 1.0;
    
    /**
     * Get the lower bound of the first argument of the interpolated
     * region.
     * @return the lower bound
     */
    public double getDomainMin1() {return (xmin < xmax)? xmin: xmax;}

    /**
     * Get the upper bound of the first argument of the interpolated
     * region.
     * @return the upper bound
     */
    public double getDomainMax1() {return (xmin < xmax)? xmax: xmin;}

    /**
     * Get the lower bound of the second argument of the interpolated
     * region.
     * @return the lower bound
     */
    public double getDomainMin2() {return (ymin < ymax)? ymin: ymax;}

    /**
     * Get the upper bound of the second argument of the interpolated
     * region.
     * @return the upper bound
     */
    public double getDomainMax2() {return (ymin < ymax)? ymax: ymin;}


    double scalex = 1.0;
    double scaley = 1.0;
    double scalex2 = 1.0;
    double scaley2 = 1.0;
    double scalexy = 1.0;

    /**
     * Constructor.
     * The values and derivatives are computed at the points
     * (0,0), (1,0), (0,1) and (1,1), which are the corners of the
     * interpolation region. For the function f being interpolated,
     * the argument is an array whose values are as follows:
     * <UL>
     *  <LI> inits[0] = f(0,0).
     *  <LI> inits[1] = f(1,0).
     *  <LI> inits[2] = f(0,1).
     *  <LI> inits[3] = f(1,1);
     *  <LI> inits[4] = f<sub>1</sub>(0,0)
     *  <LI> inits[5] = f<sub>1</sub>(1,0)
     *  <LI> inits[6] = f<sub>1</sub>(0,1)
     *  <LI> inits[7] = f<sub>1</sub>(1,1)
     *  <LI> inits[8] = f<sub>2</sub>(0,0)
     *  <LI> inits[9] = f<sub>2</sub>(1,0)
     *  <LI> inits[10] = f<sub>2</sub>(0,1)
     *  <LI> inits[11] = f<sub>2</sub>(1,1)
     *  <LI> inits[12] = f<sub>12</sub>(0,0)
     *  <LI> inits[13] = f<sub>12</sub>(1,0)
     *  <LI> inits[14] = f<sub>12</sub>(0,1)
     *  <LI> inits[15] = f<sub>12</sub>(1,1)
     * </UL>
     * where the subscripts for f denote partial derivatives with respect
     * to the first and/or second arguments.
     * <P>
     * If only inits[0] to inits[11] are provided, inits[12], inits[13],
     * inits[14], and inits[15] are assumed to be zero, making the interpolation
     * as close to linear as possible at the vertices.
     * @param inits the values of the function and various
     *        partial derivatives at the corners of the interpolation
     *        region
     * @exception IllegalArgumentException the array argument had the
     *            wrong length;
     */
    public BicubicInterpolator(double[]inits) throws IllegalArgumentException {
	if (inits == null) {
	    throw new IllegalArgumentException("nullArg");
	}
	if (inits.length == 12) {
	    double[] tmp = new double[13];
	    System.arraycopy(inits, 0, tmp, 0, 12);
	} else if (inits.length == 16) {
	    int index = 0;
	    for (int j = 0; j < 4; j++) {
		for (int i = 0; i < 4; i++) {
		    double sum = 0.0;
		    for (int k = 0; k < 16; k++) {
			sum += matrix[index][k] * inits[k];
		    }
		    // a[i][j] = sum;
		    a[4*i+j] = sum;
		    index++;
		}
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("initLength"));
	}
    }

    /**
     * Constructor given B&eacute;zier control points.
     * @param controlPoints the control points (a 4&times;4 array)
     * @exception IllegalArgumentException the argument had the
     *            wrong length;
     */
    public BicubicInterpolator(double[][] controlPoints) {
	if (controlPoints == null) {
	    throw new IllegalArgumentException("nullArg");
	} else 	if (controlPoints.length != 4) {
	    throw new IllegalArgumentException(errorMsg("initLength"));
	}
	for (int i = 0; i < 4; i++) {
	    if (controlPoints[i] == null) {
		throw new
		    IllegalArgumentException(errorMsg("nullInitSubArray", i));
	    } else if (controlPoints.length != 4) {
		throw new IllegalArgumentException(errorMsg("initLength"));
	    }
	}

	double[][] temp = new double[4][4];
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 4; k++) {
		    sum += M[i][k]*controlPoints[k][j];
		}
		temp[i][j] = sum;
	    }
	}
	for (int i = 0; i < 4; i++) {
	    int i4 = 4*i;
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 4; k++) {
		    sum += temp[i][k]*MT[k][j];
		}
		// a[i][j] = sum;
		a[i4+j] = sum;
	    }
	}
    }

    /**
     * Constructor given an interpolation region.
     * The values and derivatives are computed at the points (xmin,
     * ymin), (xmax, ymin), (xmin, ymax) and (xmax, ymax), which are
     * the corners of the interpolation region, corresponding to
     * the normalized parameters u and v with values (0, 0, (1, 0),
     * (0, 1), and (1, 1) respectively. For the function f
     * being interpolated, the argument is an array whose values are
     * as follows:
     * <UL>
     *  <LI> inits[0] = f(xmin, ymin).
     *  <LI> inits[1] = f(xmax, ymin).
     *  <LI> inits[2] = f(xmin, ymax).
     *  <LI> inits[3] = f(xmax, ymax);
     *  <LI> inits[4] = f<sub>1</sub>(xmin, ymin)
     *  <LI> inits[5] = f<sub>1</sub>(xmax, ymin)
     *  <LI> inits[6] = f<sub>1</sub>(xmin, ymax)
     *  <LI> inits[7] = f<sub>1</sub>(xmax, ymax)
     *  <LI> inits[8] = f<sub>2</sub>(xmin, ymin)
     *  <LI> inits[9] = f<sub>2</sub>(xmax, ymin)
     *  <LI> inits[10] = f<sub>2</sub>(xmin, ymax)
     *  <LI> inits[11] = f<sub>2</sub>(xmax, ymax)
     *  <LI> inits[12] = f<sub>12</sub>(xmin, ymin)
     *  <LI> inits[13] = f<sub>12</sub>(xmax, ymin)
     *  <LI> inits[14] = f<sub>12</sub>(xmin, ymax)
     *  <LI> inits[15] = f<sub>12</sub>(xmax, ymax)
     * </UL>
     * where the subscripts for f denote partial derivatives with respect
     * to the first and/or second arguments.
     * <P>
     * If only inits[0] to inits[12] are provided, in which case the
     * inits array's length will be 13, the polynomial approximation
     * a<sub>ij</sub>x<sup>i</sup>y<sup>j</sup>, summed over i and j,
     * will include terms such at 0 &le; i &le; 3, 0 &le; j &le; 3,
     * and i+j &le; 4: if i+j &gt; 4, a<sub>ij</sub> will be zero.
     * If the array length is 12, so that inits[12] is not available,
     * its value will be estimated.
     * @param xmin the bound on the interpolation region, corresponding
     *        to u = 0, for  the first argument of f
     * @param xmax the bound on the interpolation region, corresponding
     *        to u = 1, for the first argument of f
     * @param ymin the bound on the interpolation region, corresponding
     *        to v = 0, for the second argument of f
     * @param ymax the bound on the interpolation region, corresponding
     *        to v = 1, for the second argument of f
     * @param inits the values of the function and various
     *        partial derivatives at the corners of the interpolation
     *        region
     * @exception IllegalArgumentException the array argument had the
     *            wrong length;
     */
    public BicubicInterpolator(double xmin, double xmax,
			       double ymin, double ymax,
			       double[]inits)
	throws IllegalArgumentException
    {
	if (inits == null) {
	    throw new IllegalArgumentException("nullArg");
	}

	this.xmin = xmin;
	this.ymin = ymin;
	this.xmax = xmax;
	this.ymax = ymax;

	scalex = xmax - xmin;
	scaley = ymax - ymin;
	scalex2 = scalex*scalex;
	scaley2 = scaley*scaley;
	scalexy = scalex*scaley;

	int length = inits.length;
	switch (length) {
	case 16:
	case 13:
	    break;
	case 12:
	    length = 13;
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("initLength"));
	}

	double[] scaled = new double[length];
	double sc = 1.0;
	for (int i = 0; i < inits.length; i++) {
	    switch (i) {
	    case 4:
		sc = scalex;
		break;
	    case 8:
		sc = scaley;
		break;
	    case 12:
		sc = scalexy;
		break;
	    default:
		break;
	    }
	    scaled[i] = sc * inits[i];
	}
	if (inits.length == 12) {
	    scaled[12] = (scalex * (inits[6] - inits[4])
			  + scaley * (inits[9] - inits[8]))/2.0;
	}
	if (length == 13) {
	    for (int i = 0; i < 13; i++) {
		double sum = 0.0;
		for (int j = 0; j < 13; j++) {
		    sum += shortMatrix[i][j]*scaled[j];
		}
		int ii = firstShortInd[i];
		int jj = secondShortInd[i];
		a[4*ii+jj] = sum;
	    }
	} else {
	    int index = 0;
	    for (int j = 0; j < 4; j++) {
		for (int i = 0; i < 4; i++) {
		    double sum = 0.0;
		    for (int k = 0; k < 16; k++) {
			sum += matrix[index][k] * scaled[k];
		    }
		    // a[i][j] = sum;
		    a[4*i+j] = sum;
		    index++;
		}
	    }
	}
    }

    @Override
    public double valueAt(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 0; i < 4; i++) {
	    double up = 1.0;
	    int i4 = 4*i;
	    for (int j = 0; j < 4; j++) {
		// result += a[i][j]*up*tp;
		result += a[i4+j]*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result;
    }
      
    @Override
    public double deriv1At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 1; i < 4; i++) {
	    double up = i;
	    int i4 = 4*i;
	    for (int j = 0; j < 4; j++) {
		// result += a[i][j]*up*tp;
		result += a[i4+j]*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scalex;
    }

    @Override
    public double deriv2At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 0; i < 4; i++) {
	    double up = 1.0;
	    int i4 = 4*i;
	    for (int j = 1; j < 4; j++) {
		// result += a[i][j]*j*up*tp;
		result += a[i4+j]*j*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scaley;
    }

    @Override
    public double deriv11At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 2; i < 4; i++) {
	    double up = i*(i-1);
	    int i4 = i*4;
	    for (int j = 0; j < 4; j++) {
		// result += a[i][j]*up*tp;
		result += a[i4+j]*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scalex2;
    }

    @Override
    public double deriv12At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 1; i < 4; i++) {
	    double up = i;
	    int i4 = 4*i;
	    for (int j = 1; j < 4; j++) {
		// result += a[i][j]*j*up*tp;
		result += a[i4+j]*j*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scalexy;
    }

    @Override
    public double deriv21At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 1; i < 4; i++) {
	    double up = i;
	    int i4 = 4*i;
	    for (int j = 1; j < 4; j++) {
		// result += a[i][j]*j*up*tp;
		result += a[i4+j]*j*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scalexy;
    }

    @Override
    public double deriv22At(double x, double y) {
	double t = (x-xmin)/scalex;
	double u = (y-ymin)/scaley;
	double result = 0.0;
	double tp = 1.0;
	for (int i = 0; i < 4; i++) {
	    double up = 1.0;
	    int i4 = 4*i;
	    for (int j = 2; j < 4; j++) {
		// result += a[i][j]*j*up*tp;
		result += a[i4+j]*j*(j-1)*up*tp;
		up *= u;
	    }
	    tp *= t;
	}
	return result/scaley2;
    }
}

//  LocalWords:  exbundle Bicubic bicubic ij href getDomainMin le li
//  LocalWords:  getDomainMax interpolator eacute zier pre MTI inits
//  LocalWords:  colOrder IllegalArgumentException nullArg initLength
//  LocalWords:  controlPoints nullInitSubArray xmin ymin xmax ymax
//  LocalWords:  tp
