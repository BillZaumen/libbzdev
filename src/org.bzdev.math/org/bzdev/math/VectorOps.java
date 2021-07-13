package org.bzdev.math;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Vector-algebra operations.
 * These include the addition of two vectors, multiplication by a scalar,
 * dot products, cross products (for 3-dimensional vectors)
 */
public class VectorOps {

    private VectorOps() {}

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Compute the sum of two vectors.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the sum v1+v2
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static double[] add(double[] v1, double[] v2) 
	throws IllegalArgumentException
    {
	if (v1.length != v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	double[] result = new double[v1.length];
	for (int i = 0; i < v1.length; i++) {
	    result[i] = v1[i] + v2[i];
	}
	return result;
    }

    /**
     * Compute the sum of two vectors, storing the results.
     * The vectors result, v1, and/or v2 may be identical.
     * @param result a vector that will store the vector v1+v2; null if a
     *        new array should be allocated
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the vector equal to v1+v2
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static double[] add(double[] result, double[] v1, double[] v2) 
	throws IllegalArgumentException
    {
	if (v1.length != v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	if (result == null) {
	    result = new double[v1.length];
	} else if (result.length != v1.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	for (int i = 0; i < v1.length; i++) {
	    result[i] = v1[i] + v2[i];
	}
	return result;
    }

    /**
     * Compute the sum of two vectors, storing the results, with offsets
     * into arrays determining the vectors.
     * If result and v1 or v2 are the same vector, then the offsets
     * for result and v1 or v2 must either be identical or such that
     * the areas used do not overlap.
     * @param result a vector that will store the vector v1+v2; null if a
     *        new array should be allocated
     * @param offset the offset into the array 'result' (the first argument)
     *        at which the vector will be stored
     * @param v1 the first vector
     * @param off1 the offset into the first vector's array
     * @param v2 the second vector
     * @param off2 the offset into the second vector's array
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static void add(double[] result, int offset,
			   double[] v1, int off1,
			   double[] v2, int off2,
			   int n) 
    {
	for (int i = 0; i < n; i++) {
	    result[offset+i] = v1[off1+i] + v2[off2+i];
	}
    }


    /**
     * Compute the difference of two vectors.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the difference v1-v2
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static double[] sub(double[] v1, double[] v2)
	throws IllegalArgumentException
    {
	if (v1.length != v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	double[] result = new double[v1.length];
	for (int i = 0; i < v1.length; i++) {
	    result[i] = v1[i] - v2[i];
	}
	return result;
    }

    /**
     * Compute the difference of two vectors, storing the results.
     * The vectors result, v1, and/or v2 may be identical vectors.
     * @param result a vector that will store the vector v1-v2; null if a
     *        new array should be allocated
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the vector equal to v1-v2
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static double[] sub(double[] result, double[] v1, double[] v2)
	throws IllegalArgumentException
    {
	if (v1.length != v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	if (result == null) {
	    result = new double[v1.length];
	} else if (result.length != v1.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	for (int i = 0; i < v1.length; i++) {
	    result[i] = v1[i] - v2[i];
	}
	return result;
    }

    /**
     * Compute the difference of two vectors, storing the results, with offsets
     * into arrays determining the vectors.
     * If result and v1 or v2 are the same vector, then the offsets
     * for result and v1 or v2 must either be identical or such that
     * the areas used do not overlap.
     * @param result a vector that will store the vector v1-v2; null if a
     *        new array should be allocated
     * @param offset the offset into the array 'result' (the first argument)
     *        at which the vector will be stored
     * @param v1 the first vector
     * @param off1 the offset into the first vector's array
     * @param v2 the second vector
     * @param off2 the offset into the second vector's array
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static void sub(double[] result, int offset,
			   double[] v1, int off1,
			   double[] v2, int off2,
			   int n)
    {
	for (int i = 0; i < n; i++) {
	    result[offset+i] = v1[off1+i] - v2[off2+i];
	}
    }


    /**
     * Multiply a vector by a scalar.
     * @param scalar the scalar
     * @param v the vector
     */
    public static double[] multiply(double scalar, double[] v) {
	double[] result = new double[v.length];
	for (int i = 0; i < v.length; i++) {
	    result[i] = scalar * v[i];
	}
	return result;
    }

    /**
     * Multiply a vector by a scalar, storing the results.
     * The vectors result and v may be identical.
     * @param result an array holding the result of the multiplication; null
     *        if a new array should be allocated
     * @param scalar the scalar
     * @param v the vector
     * @exception IllegalArgumentException the vectors differ in length
     */
    public static double[] multiply(double[] result, double scalar, double[] v)
	throws IllegalArgumentException
    {
	if (result == null) {
	    result = new double[v.length];
	} else if (result.length != v.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	for (int i = 0; i < v.length; i++) {
	    result[i] = scalar * v[i];
	}
	return result;
    }

    /**
     * Multiply a vector by a scalar, storing the results.
     * If result and v are the same vector, then either the offsets must either
     * be identical or such that the areas used do not overlap.
     * @param result an array holding the result of the multiplication; null
     *        if a new array should be allocated
     * @param rOffset the offset into the 'result' array for storing the vector
     * @param scalar the scalar
     * @param v the array storing the vector to be multiplied
     * @param vOffset the offset into the array v for the start of the vector
     */
    public static void multiply(double[] result, int rOffset, double scalar,
				double[] v, int vOffset, int n)
    {
	for (int i = 0; i < n; i++) {
	    result[rOffset+i] = scalar * v[vOffset+i];
	}
    }

    private static final double EPS = Math.scalb(1.0, -52);

    /**
     * Compute the dot product of two vectors.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the dot product of v1 and v2
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have the same length
     */
    public static double dotProduct(double[] v1, double[] v2)
	throws IllegalArgumentException
    {
	if (v1.length != v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	double asum = 0.0;
	double sum = 0.0;
	double c = 0.0;
	for (int i = 0; i < v1.length; i++) {
	    double term = v1[i]*v2[i];
	    asum += Math.abs(term);
	    double y = term - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	// if the number is zero within the floating point
	// accuracy, set it to zero.
	if (Math.abs(sum) < EPS*asum) sum = 0.0;
	return sum;
    }

    /**
     * Compute the dot product of two vectors, represented as subarrays.
     * @param v1 the first vector's array
     * @param off1 the offset into v1 for the start of the vector
     * @param v2 the second vector's array
     * @param off2 the offset into v2 for the start of the vector
     * @param n the lengths of the vectors
     * @return the dot product of v1 and v2
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have n elements past their offsets
     */
    public static double dotProduct(double[] v1, int off1,
				    double[] v2, int off2,
				    int n)
	throws IllegalArgumentException
    {
	if (n + off1 > v1.length || n + off2 >  v2.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	double asum = 0.0;
	double sum = 0.0;
	double c = 0;
	for (int i = 0; i < n; i++) {
	    double term = v1[off1 + i]*v2[off2 + i];
	    asum += Math.abs(term);
	    double y = term - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	// if the number is zero within the floating point
	// accuracy, set it to zero.
	if (Math.abs(sum) < EPS*asum) sum = 0.0;
	return sum;
    }

    /**
     * Compute the cross product of two vectors.
     * @param v1 the first vector
     * @param v2 the second vector
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have the same length
     */
    public static double[] crossProduct(double[] v1, double v2[])
	throws IllegalArgumentException
    {
	if (v1.length != 3 || v2.length != 3) {
	    throw new IllegalArgumentException(errorMsg("arrayLengthNot3"));

	}
	double[] result = new double[3];

	double term1 = v1[1]*v2[2];
	double term2 = v2[1]*v1[2];
	result[0] =  term1 - term2;
	double max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(result[0]) < EPS*max) result[0] = 0;
	term1 = v2[0]*v1[2];
	term2 = v1[0]*v2[2];
	result[1] = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(result[1]) < EPS*max) result[1] = 0;
	term1 = v1[0]*v2[1];
	term2 = v2[0]*v1[1];
	result[2] = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(result[2]) < EPS*max) result[2] = 0;
	return result;
    }

    /**
     * Compute the cross product of two vectors, storing the results.
     * @param result a vector that will be set to the cross product 
     *        v1 &times; v2; null if a new vector should be allocated
     * @param v1 the first vector
     * @param v2 the second vector
     * @return result, or a new vector if result is null
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have the same length
     */
    public static double[] crossProduct(double[] result,
					double[] v1, double v2[])
	throws IllegalArgumentException
    {
	if (v1.length != 3 || v2.length != 3) {
	    throw new IllegalArgumentException(errorMsg("arrayLengthNot3"));

	}
	if (result == null) {
	    result = new double[3];
	} else 	if (result.length < 3) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}

	double term1 = v1[1]*v2[2];
	double term2 = v2[1]*v1[2];
	double r0 =  term1 - term2;
	double max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r0) < EPS*max) r0 = 0;
	term1 = v2[0]*v1[2];
	term2 = v1[0]*v2[2];
	double r1 = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r1) < EPS*max) r1 = 0;
	term1 = v1[0]*v2[1];
	term2 = v2[0]*v1[1];
	double r2 = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r2) < EPS*max) r2 = 0;
	result[0] = r0;
	result[1] = r1;
	result[2] = r2;
	return result;
    }

    /**
     * Compute the cross product of two vectors represented as subarrays.
     * If result and v1 or v2 are the same vector, then the offsets
     * for result and v1 or v2 must either be identical or such that
     * the areas used do not overlap.
     * The arrays lengths must be at least 3, and the offsets must be
     * such that the vectors fit within their arrays. All vectors have a
     * length of 3.
     * @param result an array holding a vector that will be set to the 
     *        cross product v1 &times; v2
     * @param offset the offset into the array 'result' (the first argument)
     *        at which the vector will be stored; ignored if 'result' is null
     * @param v1 the first vector's array
     * @param off1 the offset into the first vector's array
     * @param v2 the second vector's array
     * @param off2 the offset into the second vector's array
     * @return result, or a new vector if result is null
     * @exception IllegalArgumentException the arrays v1 and v2 are too
     *            short
     */
    public static double[] crossProduct(double[] result, int offset,
					double[] v1, int off1,
					double[] v2, int off2)
	throws IllegalArgumentException
    {
	if (result == null) {
	    result = new double[3];
	    offset = 0;
	} else 	if (v1.length <3 || v2.length < 3 || result.length < 3) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	int off1p1 = off1+1;
	int off2p1 = off2+1;
	int off1p2 = off1+2;
	int off2p2 = off2+2;

	double term1 = v1[off1p1]*v2[off2p2];
	double term2 = v2[off2p1]*v1[off1p2];
	double r0 = term1 - term2;
	double max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r0) < EPS*max) r0 = 0;
	term1 = v2[off2]*v1[off1p2];
	term2 = v1[off1]*v2[off2p2];
	double r1 = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r1) < EPS*max) r1 = 0;
	term1 = v1[off1]*v2[off2p1];
	term2 = v2[off2]*v1[off1p1];
	double r2 = term1 - term2;
	max = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(r2) < EPS*max) r2 = 0;
	result[offset] = r0;
	result[offset+1] = r1;
	result[offset+2] = r2;
	return result;
    }

    /**
     * Compute the dot product of a vector with the cross product of two other
     * vectors.
     * The value it computes,
     * v<sub>1</sub>&sdot;(v<sub>2</sub>&times;v<sub>3</sub>), is sometimes
     * called the scalar triple product, written as
     * [v<sub>1</sub>,v<sub>2</sub>,v<sub>3</sub>].
     * @param v1 the first vector v<sub>1</sub>
     * @param v2 the second vector v<sub>2</sub>
     * @param v3 the third vector v<sub>3</sub>
     * @return the value
     *         v<sub>1</sub>&sdot;(v<sub>2</sub>&times;v<sub>3</sub>)
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have the same length
     *
     */
    public static double dotCrossProduct(double[]v1, double[] v2, double[] v3)
	throws IllegalArgumentException
    {
	if (v1.length != 3 || v2.length != 3 || v3.length != 3) {
	    throw new IllegalArgumentException(errorMsg("arrayLengthNot3"));

	}
	double asum = 0.0;
	double result = 0.0;
	double term1 = v2[1]*v3[2];
	double term2 = v3[1]*v2[2];
	double term3 = (term1 - term2);
	double max  = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(term3) < EPS*max) term3 = 0.0;
	term3 *= v1[0];
	asum += Math.abs(term3);
	result += term3;
	term1 = v3[0]*v2[2];
	term2 = v2[0]*v3[2];
	term3 = (term1 - term2);
	max  = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(term3) < EPS*max) term3 = 0;
	term3 *= v1[1];
	asum += Math.abs(term3);
	result += term3;
	term1 = v2[0]*v3[1];
	term2 = v3[0]*v2[1];
	term3 = (term1 - term2);
	max  = Math.max(Math.abs(term1), Math.abs(term2));
	if (Math.abs(term3) < EPS*max) term3 = 0;
	term3 *= v1[2];
	asum += Math.abs(term3);
	result += term3;
	if (Math.abs(result) < EPS*asum) result = 0.0;
	return result;
    }

    /**
     * Compute the dot product of a vector with the cross product of two other
     * vectors, with the vectors represented by an array with an offset to
     * the start of the vector.
     * @param v1 the array for the first vector v<sub>1</sub>
     * @param off1 the offset into the array v1 for the start of the
     *        vector
     * @param v2 the array for the second vector v<sub>2</sub>
     * @param off2 the offset into the array v2 for the start of the
     *        vector
     * @param v3 the array for the third vector v<sub>3</sub>
     * @param off3 the offset into the array v3 for the start of the
     *        vector
     * @return the value
     *         v<sub>1</sub>&sdot;(v<sub>2</sub>&times;v<sub>3</sub>)
     * @exception IllegalArgumentException the arrays v1 and v2 do not
     *            have the same length
     *
     */
    public static double dotCrossProduct(double[]v1, int off1,
					 double[] v2, int off2,
					 double[] v3, int off3)
	throws IllegalArgumentException
    {
	try {
	    double asum = 0.0;
	    double result = 0.0;

	    int ind20 = off2;
	    int ind21 = off2+1;
	    int ind22 = off2+2;
	    int ind30 = off3;
	    int ind31 = off3+1;
	    int ind32 = off3+2;

	    double term1 = v2[ind21]*v3[ind32];
	    double term2 = v3[ind31]*v2[ind22];
	    double term3 = (term1 - term2);
	    double max  = Math.max(Math.abs(term1), Math.abs(term2));
	    if (Math.abs(term3) < EPS*max) term3 = 0;
	    term3 *= v1[off1];
	    asum += Math.abs(term3);
	    result += term3 ;
	    term1 = v3[ind30]*v2[ind22];
	    term2 = v2[ind20]*v3[ind32];
	    term3 = (term1 - term2);
	    max  = Math.max(Math.abs(term1), Math.abs(term2));
	    if (Math.abs(term3) < EPS*max) term3 = 0;
	    term3 *= v1[off1+1];
	    asum += Math.abs(term3);
	    result += term3;
	    term1 = v2[ind20]*v3[ind31];
	    term2 = v3[ind30]*v2[ind21];
	    term3 = (term1 - term2);
	    max  = Math.max(Math.abs(term1), Math.abs(term2));
	    if (Math.abs(term3) < EPS*max) term3 = 0;
	    term3 *= v1[off1+2];
	    asum += Math.abs(term3);
	    result += term3;
	    // result += v1[off1+2]*(v2[ind20]*v3[ind31] - v3[ind30]*v2[ind21]);
	    if (Math.abs(result) < EPS*asum) result = 0.0;
	    return result;
	} catch (Exception e) {
	    String msg = errorMsg("offsetOutOfRange");
	    throw new IllegalArgumentException(msg, e);

	}
    }

    /**
     * Compute the norm of a vector.
     * @param v the vector
     * @return the norm
     */
    public static double norm(double[] v) {
	double sum = 0.0;
	for (int i = 0; i < v.length; i++) {
	    double val = v[i];
	    sum += (val*val);
	}
	return Math.sqrt(sum);
    }

    /**
     * Compute the square of the norm of a vector.
     * @param v the vector
     * @return the norm
     */
    public static double normSquared(double[] v) {
	double sum = 0.0;
	for (int i = 0; i < v.length; i++) {
	    double val = v[i];
	    sum += (val*val);
	}
	return sum;
    }


    /**
     * Compute the norm of a vector stored in an array at a specified offset.
     * @param v the vector's array
     * @param offset the offset into v
     * @param n the length of the vector
     * @return the norm
     */
    public static double norm(double[] v, int offset, int n) {
	double sum = 0.0;
	for (int i = 0; i < n; i++) {
	    double val = v[offset+i];
	    sum += (val*val);
	}
	return Math.sqrt(sum);
    }

    /**
     * Compute the square of the norm of a vector stored in an array
     * at a specified offset.
     * @param v the vector's array
     * @param offset the offset into v
     * @param n the length of the vector
     * @return the norm
     */
    public static double normSquared(double[] v, int offset, int n) {
	double sum = 0.0;
	for (int i = 0; i < n; i++) {
	    double val = v[offset+i];
	    sum += (val*val);
	}
	return sum;
    }

    /**
     * Normalize a vector.
     * @param v the vector
     * @return the vector that was normalized
     * @exception IllegalArgumentException the vector's norm was zero
     */
    public static double[] normalize(double[] v)
	throws IllegalArgumentException
    {
	double vnorm = norm(v);
	if (vnorm == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroNorm"));
	}
	for (int i = 0; i < v.length; i++) {
	    v[i] /= vnorm;
	}
	return v;
    }

    /**
     * Normalize a vector of length n, stored in an array at a specified offset.
     * @param v the vector's array
     * @param offset the offset into the array
     * @param n the length (dimension) of the vector
     * @exception IllegalArgumentException n is 0, n is negative,
     *            the vector's norm is zero, or the vector's length is
     *            not compatible with the specified value of n and offset
     */
    public static void normalize(double[] v, int offset, int n)
	throws IllegalArgumentException
    {
	if (n <= 0) {
	    String msg = errorMsg("vectLenNotPositive", n);
	    throw new IllegalArgumentException(msg);
	}
	if (offset + n > v.length) {
	    String msg = errorMsg("vectorOffset", v.length, n, offset);
	    throw new IllegalArgumentException(msg);
	}
	double vnorm = norm(v, offset, n);
	if (vnorm == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroNorm"));
	}
	for (int i = 0; i < n; i++) {
	    v[offset+i] /= vnorm;
	}
    }

    /**
     * Get a unit vector with the same direction as a specified vector.
     * @param v a vector
     * @return a unit vector
     * @exception IllegalArgumentException n is 0, n is negative,
     *            the vector's norm is zero, or the vector's length is
     *            not compatible with the specified value of n and offset
     */
    public static double[] unitVector(double[] v)
	throws IllegalArgumentException
    {
	double vnorm = norm(v);
	if (vnorm == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroNorm"));
	}
	return multiply(1.0/vnorm, v);
    }

    /**
     * Get a unit vector with the same direction as a specified
     * vector, storing the results.
     * @param result an array to hold the results
     * @param v a vector
     * @return the resulting vector
     * @exception IllegalArgumentException the vectors differ in length or
     *            the v's norm is 0
     */
    public static double[] unitVector(double[] result, double[] v) 
	throws IllegalArgumentException
    {
	if (v.length != result.length || result.length == 0) {
	    throw new IllegalArgumentException(errorMsg("vectorLengths"));
	}
	double vnorm = norm(v);
	if (vnorm == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroNorm"));
	}
	return multiply(result, 1.0/vnorm, v);
    }

    /**
     * Get a unit vector with the same direction as a specified
     * vector, storing the results and with each represented by an
     * array and an offset.
     * If result and v are the same vector, then either the offsets must either
     * be identical or such that the areas used do not overlap.
     * @param result an array to hold the results
     * @param rOffset the offset into the 'result' array for storing the vector
     * @param v a vector
     * @param vOffset the offset into the array v for the start of the vector
     * @param n the length of the vector.
     * @exception IllegalArgumentException the vectors differ in
     *            length, n is not positive, the vector's norm is
     *            zero, or the vector's length is not compatible with
     *            the specified value of n and offset
     */
    public static void unitVector(double[] result, int rOffset,
				  double[] v, int vOffset, int n)
    {
	if (n <= 0) {
	    String msg = errorMsg("vectLenNotPositive", n);
	    throw new IllegalArgumentException(msg);
	}
	if (vOffset + n > v.length) {
	    String msg = errorMsg("vectorOffset", v.length, n, vOffset);
	    throw new IllegalArgumentException(msg);
	}
	if (rOffset + n > result.length) {
	    String msg = errorMsg("vectorOffset", v.length, n, rOffset);
	    throw new IllegalArgumentException(msg);
	}
	double vnorm = norm(v);
	if (vnorm == 0.0) {
	    throw new IllegalArgumentException(errorMsg("zeroNorm"));
	}
	multiply(result, rOffset, 1.0/vnorm, v, vOffset, n);
    }
}

//  LocalWords:  exbundle IllegalArgumentException incompatibleArrays
//  LocalWords:  rOffset vOffset subarrays arrayLengthNot sdot
//  LocalWords:  argArrayTooShort offsetOutOfRange
