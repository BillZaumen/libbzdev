package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Basic matrix operations.
 * This class contains only static methods. The matrix operations
 * it supports include matrix addition, matrix multiplication (by both
 * matrices and scalars), and computing the transpose of a matrix.
 * <P>
 * Matrices can be represented in two forms:
 * <UL>
 *   <LI> A two-dimensional array in which the first index
 *        is the row number and the second index is the column
 *        number.  A matrix element A<sub>ij</sub> is thus
 *        represented as A[i][j] in Java.
 *   <LI> A one dimensional array in either row-major order or
 *        column-major order. In row-major order, the elements
 *        of a row are adjacent to each other, listed in the
 *        order specified by their columns.  In column-major
 *        order, the elements of a column are adjacent to each
 *        other, listed in the order specified by their rows.
 *        For multidimensional arrays, C uses row-major order
 *        and FORTRAN uses column-major order.
 * </UL>
 * The functions flatten and unflatten convert between these two
 * forms. One can convert a square matrix from row-major order and
( column-major order, and vice versa, by using the
 * {@link #transpose(double[],int,int,double[],int,int,boolean)}
 * method. For example,
 * <BLOCKQUOTE><CODE><PRE>
 *      MatrixOps.transpose(matrix, 20, 20, matrix, 20, 20, true);
 * </PRE></CODE></BLOCKQUOTE>
 * <P>
 * With a flat representation, matrices can be interleaved.  In this
 * form, each element of the matrix is a vector, with each element in
 * a matrix using vectors of the same length. These vectors are
 * stored contiguously. When a matrix operation is performed  for this
 * case, the arguments for a method will include the vector length and
 * the index of the component of the vector that will be used.
 * This representation is convenient for cases such as B&eacute;zier
 * patches: for the cubic case, each patch is represented by 16 control
 * points, typically named by two indices, each of which can take on
 * 4 values (0, 1, 2, and 3).  Each control point itself consists of
 * three values: the X, Y, and Z coordinates for the point. The internal
 * representation uses a sequence of three double-precision numbers for
 * each control point, with the control points in column-major order.
 * For example,
 * <BLOCKQUOTE><CODE><PRE>
 *      double[] controlPoints = {
 *                   P<sub>00x</sub>, P<sub>00y</sub>, P<sub>00z</sub>,
 *                   P<sub>10x</sub>, P<sub>10y</sub>, P<sub>10z</sub>,
 *                   ...
 *               };
 * </PRE></CODE></BLOCKQUOTE>
 * where <CODE>P<sub>ijx</sub></CODE> is the X component of the control point
 * P<sub>ij</sub>, etc.
 */
public class MatrixOps {

    private MatrixOps() {}

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Convert a matrix represented by a two-dimensional array to a
     * linear array in either row- or column-major order.
     * @param matrix the matrix to convert
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @return return the converted matrix
     * @exception IllegalArgumentException the array passed as the
     *            first argument is too short
     */
    public static double[] flatten(double[][] matrix, boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[matrix.length * matrix[0].length];
	return flatten(result, matrix, colOrder);

    }

    /**
     * Convert a matrix represented by a two-dimensional array to a
     * linear array in either row- or column-major order, providing an
     * array in which to store the results.
     * @param matrix the matrix to convert
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @param result the array in which to store the flattened matrix
     * @return the matrix containing the result
     * @exception IllegalArgumentException the array passed as the
     *            first argument is too short
     */
    public static double[] flatten(double[] result,
				   double[][] matrix,
				   boolean colOrder)
	throws IllegalArgumentException
    {
	int n = matrix.length;
	int m = matrix[0].length;
      	if (result.length < n*m) {
	    String msg =
		errorMsg("resultMatrixTooShort",result.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (colOrder) {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    result[i+n*j] = matrix[i][j];
		}
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    result[i*m+j] = matrix[i][j];
		}
	    }
	}
	return result;
    }


    /**
     * Convert a flattened matrix to an unflattened matrix.
     * The flattened matrix can be in either row- or column-major order.
     * @param matrix the flattened matrix 
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @return the unflattened matrix
     * @exception IllegalArgumentException The first argument has the
     *            wrong dimensions or the second argument has the wrong
     *            length
     */
    public static double[][] unflatten(double[] matrix,
				       int m, int n,
				       boolean colOrder)
	throws IllegalArgumentException
    {
	double[][] result = new double[m][n];
	return unflatten(result, matrix, m, n, colOrder);
    }    


    /**
     * Convert a flattened matrix to an unflattened matrix.
     * The flattened matrix can be in either row- or column-major order.
     * the length of the array matrix must be n*m and the matrix
     * result must have n rows and m columns
     * @param result the  matrix (n rows and m columns) used
     *        to store the unflattened matrix
     * @param matrix the flattened matrix 
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @return the unflattened matrix
     * @exception IllegalArgumentException The first argument has the
     *            wrong dimensions or the second argument has the wrong
     *            length
     */
    public static double[][] unflatten(double[][] result,
				       double[] matrix,
				       int m, int n,
				       boolean colOrder)
	throws IllegalArgumentException
    {
	if (result.length != m || result[0].length != n) {
	    double d1 = result.length;
	    double d2 = result[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (matrix.length < n*m) {
	    String msg = errorMsg("lengthError", matrix.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (colOrder) {
	    for (int i = 0; i < m; i++) {
		for (int j = 0; j < n; j++) {
		    result[i][j] = matrix[i+m*j];
		}
	    }
	} else {
	    for (int i = 0; i < m; i++) {
		for (int j = 0; j < n; j++) {
		    result[i][j] = matrix[i*n+j];
		}
	    }
	}
	return result;
    }

    /**
     * Add two matrices.
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix sum A+B
     * @exception IllegalArgumentException The matrix dimensions are
     *            not consistent
     */
    public static double[][] add(double[][] A, double[][] B)
	throws IllegalArgumentException
    {
	double[][] result = new double[A.length][A[0].length];
	return add(result, A, B);
    }


    /**
     * Add two matrices and store the results.
     * @param R the result matrix
     * @param A the first matrix
     * @param B the second matrix
     * All matrices must have the same number of rows and columns
     * The matrices can be identical objects.
     * @return the matrix sum A+B
     * @exception IllegalArgumentException The matrix dimensions are
     *            not consistent
     */
    public static double[][] add(double[][] R,
				 double[][] A,
				 double[][] B)
	throws IllegalArgumentException
    {
	int nR = R.length;
	int mR = R[0].length;
	int nA = A.length;
	int mA = A[0].length;
	int nB = B.length;
	int mB = B[0].length;
	if (nR != nA || nA != nB || mR != mA || mA != mB) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	for (int i = 0; i < nR; i++) {
	    for (int j = 0; j < mR; j++) {
		R[i][j] = A[i][j] + B[i][j];
	    }
	}
	return R;
    }

    /**
     * Add two flat matrices.
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix sum A+B
     * @exception IllegalArgumentException
     */
    public static double[] add(double[] A, double[] B)
	throws IllegalArgumentException
    {
	double[] result = new double[A.length];
	return add(result, A, B);
    }

    /**
     * Add two flat matrices and store the results.
     * The matrices can be identical objects.
     * @param R the result matrix
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix sum A+B
     * @exception IllegalArgumentException the arrays R, A, and B do
     *            not have the same lengths
     */
    public static double[] add(double[] R, double[] A, double[] B)
	throws IllegalArgumentException
    {
	if (R.length != A.length || A.length != B.length) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	for (int i = 0; i < A.length; i++) {
	    R[i] = A[i] + B[i];
	}
	return R;
    }
    
    /**
     * Add two flat, interleaved matrices and store the results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * @param n the number of rows
     * @param m the number of columns
     * @param C the result matrix
     * @param offsetC the index into C at which the results should be stored
     * @param A the first matrix
     * @param offsetA the offset into A for the first matrix
     * @param B the second matrix
     * @param offsetB the offset into B for the second matrix
     * @param indC the index specifying  the component of the vector
     *        representing an element of C
     * @param vlenC the length of the vectors that are the components of C
     * @param indA the index specifying  the component of the vector
     *        representing an element of A
     * @param vlenA the length of the vectors that are the components of A
     * @param indB the index specifying  the component of the vector
     *        representing an element of B
     * @param vlenB the length of the vectors that are the components of B
     */
    public static void add(int n, int m, 
			   double[] C, int offsetC,
			   double[] A, int offsetA,
			   double[] B, int offsetB,
			   int indC, int vlenC,
			   int indA, int vlenA,
			   int indB, int vlenB)
    {
	offsetC += indC;
	offsetA += indA;
	offsetB += indB;

	int nm = n*m;

	for (int i = 0; i < nm; i++) {
	    C[offsetC + vlenC*i] = A[offsetA + vlenA*i]
		+ B[offsetB + vlenB*i];
	}
    }
			       
    /**
     * Compute the transpose of a matrix.
     * @param A the matrix
     * @return a new matrix containing A<sup>T</sup>
     * @exception IllegalArgumentException the matrix R does have
     *            dimensions compatible with the matrix A, or
     *            when R and A are the same matrix, that matrix is
     *            not a square matrix
     */
    public static double[][] transpose(double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	double[][] result =  new double[n][m];
	return transpose(result, A);

    }


    /**
     * Compute the transpose of a matrix of an m by n matrix.
     * @param A the matrix
     * @param n the number of rows
     * @param m the number of columns
     * @return a new matrix containing A<sup>T</sup>
     * @exception IllegalArgumentException the matrix R does have
     *            dimensions compatible with the matrix A, or
     *            when R and A are the same matrix, that matrix is
     *            not a square matrix
     */
    public static double[][] transpose(double[][] A, int n, int m)
	throws IllegalArgumentException
    {
	double[][] result =  new double[m][n];
	return transpose(result, A, n, m);

    }

    /**
     * Compute the transpose of a matrix.
     * The matrices can be identical objects.
     * @param R the matrix that will contain the transpose of A
     * @param A the matrix to transpose
     * @return the matrix R = A<sup>T</sup>
     * @exception IllegalArgumentException the matrix R does have
     *            dimensions compatible with the matrix A, or
     *            when R and A are the same matrix, that matrix is
     *            not a square matrix
     */
    public static double[][] transpose(double[][] R, double[][] A)
	throws IllegalArgumentException
    {
	int n = A.length;
	int m = A[0].length;
	if (R.length != m || R[0].length != n) {
	    int d1 = R.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, n, m);
	    throw new IllegalArgumentException(msg);
	}
	if (R == A) {
	    for (int i = 0; i < n; i++) {
		if (A[i].length != m) {
		    int d1 = A.length;
		    int d2 = A[i].length;
		    String msg = errorMsg("badDimensions", d1, d2, n, m);
		    throw new IllegalArgumentException(msg);
		}
		for (int j = 0; j < i; j++) {
		    double tmp1 = A[i][j];
		    double tmp2 = A[j][i];
		    A[j][i] = tmp1;
		    A[i][j] = tmp2;
		}
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    R[j][i] = A[i][j];
		}
	    }
	}
	return R;
    }


    /**
     * Compute the transpose of a matrix given its dimensions.
     * The matrices can be identical objects.
     * The matrix A may have longer rows or columns than its dimensions
     * (m by n)  indicate. Similarly R may have longer rows or colums
     * than its dimensions (n, m) indicate.
     * @param R the matrix that will contain the transpose of A
     * @param A the matrix to transpose
     * @param n the number of rows of A and columns of R
     * @param m the number of columns of A and rows of R
     * @return the matrix R = A<sup>T</sup>
     * @exception IllegalArgumentException the matrix R does have
     *            dimensions compatible with the matrix A, or
     *            when R and A are the same matrix, that matrix is
     *            not a square matrix
     */
    public static double[][] transpose(double[][] R, double[][] A,
				       int n, int m)
	throws IllegalArgumentException
    {
	if (R.length < m || R[0].length < n) {
	    int d1 = R.length;
	    int d2 = (R[0] == null)? 0: R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length < n) {
	    int d1 = A.length;
	    int d2 = (A[0] == null)? 0: A[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, n, m);
	    throw new IllegalArgumentException(msg);
	}
	if (R == A) {
	    for (int i = 0; i < n; i++) {
		if (A[i].length < m) {
		    int d1 = A.length;
		    int d2 = A[i].length;
		    String msg = errorMsg("badDimensions", d1, d2, n, m);
		    throw new IllegalArgumentException(msg);
		}
		for (int j = 0; j < i; j++) {
		    double tmp1 = A[i][j];
		    double tmp2 = A[j][i];
		    A[j][i] = tmp1;
		    A[i][j] = tmp2;
		}
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
		    R[j][i] = A[i][j];
		}
	    }
	}
	return R;
    }


    /**
     * Compute the transpose of a flat matrix.
     * @param A the matrix to transpose
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true if the matrices are stored in column-major order;
     *        false if the matrices are stored in row-major order
     * @return the transpose of A
     * @exception IllegalArgumentException the array A is too short
     *            given the specified numbers of rows and columns
     */
    public static double[] transpose(double[] A, int m, int n,
				     boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[m*n];
	return transpose(result, n, m, A, m, n, colOrder);
    }


    /**
     * Compute the transpose of a flat matrix, storing the results.
     * The matrices can be identical objects but in that case they
     * must be square matrices.
     * @param R the matrix that will contain the transpose
     * @param mR the number of rows for R
     * @param nR the number of columns for R
     * @param A the matrix to transpose
     * @param mA the number of rows for A
     * @param nA the number of columns for A
     * @param colOrder true if the matrices are stored in column-major order;
     *        false if the matrices are stored in row-major order
     * @return the matrix R = A<sup>T</sup>
     * @exception IllegalArgumentException The matrix R must be an m by n
     *            matrix when A is an n by m matrix; the array A or the
     *            array R are too short given the specified numbers of
     *            rows and columns
     */
    public static double[] transpose(double[] R, 
				     int mR, int nR,
				     double[] A,
				     int mA, int nA,
				     boolean colOrder)
	throws IllegalArgumentException
    {
	if (nR != mA || mR != nA) {
	    String msg = errorMsg("badDimensions", mR, nR, mA, nA);
	    throw new IllegalArgumentException(msg);
	}

	if (A.length < nA*mA) {
	    String msg = errorMsg("lengthError", A.length, nA*mA);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < nR*mR) {
	    String msg = errorMsg("lengthError", R.length, nR*mR);
	    throw new IllegalArgumentException(msg);
	}

	int n = nA * mA;
	boolean notSame = (R != A);
	if (notSame) {
	    if (colOrder) {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[j+mR*i] = A[i+mA*j];
		    }
		}
	    } else {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[j*nR+i] = A[i*nA+j];
		    }
		}
	    }
	} else {
	    if (nA == mA) {
		if (colOrder) {
		    for (int i = 0; i < mA; i++) {
			for (int j = 0; j < i; j++) {
			    double tmp1 = A[i+mA*j];
			    double tmp2 = A[j+mA*i];
			    R[j+mR*i] = tmp1;
			    R[i+mR*j] = tmp2;
			}
		    }
		} else {
		    for (int i = 0; i < mA; i++) {
			for (int j = 0; j < i; j++) {
			    double tmp1 = A[i*nA+j];
			    double tmp2 = A[j*nA+i];
			    R[j*nR+i] = tmp1;
			    R[i*nR+j] = tmp2;
			}
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("expectingSquare"));
	    }
	}
	return R;
    }

    /**
     * Compute the transpose of an interleaved, flat matrix, storing the
     * results. The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * If R and A are identical, they must be square matrices.
     * @param R the matrix that will contain the transpose
     * @param mR the number of rows for the matrix contained by R
     * @param nR the number of columns for the matrix contained by R
     * @param offsetR the offset into R at which the matrix R starts
     * @param A the matrix to transpose
     * @param mA the number of rows for the matrix contained by A
     * @param nA the number of columns for the matrix contained by A
     * @param offsetA the offset into A at which the matrix A starts
     * @param colOrder true if the matrices are stored in column-major order;
     *        false if the matrices are stored in row-major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     * @exception IllegalArgumentException the dimensions of the matrices
     *            are not consistent, a square message was expected in
     *            the case where R == A, or the R and A arrays are too
     *            short given the specified offsets and vector dimensions
     */
    public static void transpose(double[] R,
				 int mR, int nR, int offsetR,
				 double[] A,
				 int mA, int nA, int offsetA,
				 boolean colOrder,
				 int indR, int vlenR,
				 int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*nR*mR;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}

	if (nR != mA || mR != nA) {
	    String msg = errorMsg("badDimensions", nR, mR, mA, nA);
	    throw new IllegalArgumentException(msg);
	}

	if (A.length -offsetA < vlenA*nA*mA) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}


	if (R.length -offsetR < vlenR*nR*mR) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	boolean notSame = (R != A) || (offsetA != offsetR);

	if (notSame) {
	    if (colOrder) {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[offsetR + vlenR*(j+mR*i)] =
			    A[offsetA + vlenA*(i+mA*j)];
		    }
		}
	    } else {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[offsetR + vlenR*(j*nR+i)] =
			    A[offsetA + vlenA*(i*nA+j)];
		    }
		}
	    }
	} else if (nR == mR) {
	    if (colOrder) {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < i; j++) {
			double tmp1 = A[offsetA + vlenA*(i+mA*j)];
			double tmp2 = A[offsetA + vlenA*(j+mA*i)];
			R[offsetR + vlenR*(j+mR*i)] = tmp1;
			R[offsetR + vlenR*(i+mR*j)] = tmp2;
		    }
		}
	    } else {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < i; j++) {
			double tmp1 = A[offsetA + vlenA*(i*nA+j)];
			double tmp2 = A[offsetA + vlenA*(j*nA+i)];
			R[offsetR + vlenR*(j*nR+i)] = tmp1;
			R[offsetR + vlenR*(i*nR+j)] = tmp2;
		    }
		}
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("notSquare"));
	}
    }


    /**
     * Compute the transpose of an interleaved, flat, single-precision
     * matrix, storing the results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * If R and A are identical, they must be square matrices.
     * <P>
     * Note: This method is provided because it is needed in the
     * implementation of some {@link org.bzdev.geom.Surface3D.Float} methods.
     * @param R the matrix that will contain the transpose
     * @param mR the number of rows for the matrix contained by R
     * @param nR the number of columns for the matrix contained by R
     * @param offsetR the offset into R at which the matrix R starts
     * @param A the matrix to transpose
     * @param mA the number of rows for the matrix contained by A
     * @param nA the number of columns for the matrix contained by A
     * @param offsetA the offset into A at which the matrix A starts
     * @param colOrder true if the matrices are stored in column-major order;
     *        false if the matrices are stored in row-major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     * @exception IllegalArgumentException the dimensions of the matrices
     *            are not consistent, a square message was expected in
     *            the case where R == A, or the R and A arrays are too
     *            short given the specified offsets and vector dimensions
     */
    public static void transpose(float[] R,
				 int mR, int nR, int offsetR,
				 float[] A,
				 int mA, int nA, int offsetA,
				 boolean colOrder,
				 int indR, int vlenR,
				 int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*nR*mR;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}

	if (nR != mA || mR != nA) {
	    String msg = errorMsg("badDimensions", nR, mR, mA, nA);
	    throw new IllegalArgumentException(msg);
	}

	if (A.length -offsetA < vlenA*nA*mA) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}


	if (R.length -offsetR < vlenR*nR*mR) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	boolean notSame = (R != A) || (offsetA != offsetR);

	if (notSame) {
	    if (colOrder) {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[offsetR + vlenR*(j+mR*i)] =
			    A[offsetA + vlenA*(i+mA*j)];
		    }
		}
	    } else {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < nA; j++) {
			R[offsetR + vlenR*(j*nR+i)] =
			    A[offsetA + vlenA*(i*nA+j)];
		    }
		}
	    }
	} else if (nR == mR) {
	    if (colOrder) {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < i; j++) {
			float tmp1 = A[offsetA + vlenA*(i+mA*j)];
			float tmp2 = A[offsetA + vlenA*(j+mA*i)];
			R[offsetR + vlenR*(j+mR*i)] = tmp1;
			R[offsetR + vlenR*(i+mR*j)] = tmp2;
		    }
		}
	    } else {
		for (int i = 0; i < mA; i++) {
		    for (int j = 0; j < i; j++) {
			float tmp1 = A[offsetA + vlenA*(i*nA+j)];
			float tmp2 = A[offsetA + vlenA*(j*nA+i)];
			R[offsetR + vlenR*(j*nR+i)] = tmp1;
			R[offsetR + vlenR*(i*nR+j)] = tmp2;
		    }
		}
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("notSquare"));
	}
    }


    /**
     * Reflect the rows in a matrix.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * @param A the orgininal matrix
     * @return the reflected matrix
     */
    public static double[][] reflectRows(double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	double[][] result =  new double[m][n];
	return reflectRows(result, A);
    }

    /**
     * Reflect the rows in a matrix, providing a matrix to return.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the same dimensions.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @return the reflected matrix R
     */
    public static double[][] reflectRows(double[][] R, double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	if (R.length != m || R[0].length != n) {
	    int d1 = R.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length != m || A[0].length != n) {
	    int d1 = A.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (m == 0 || n == 0) return R;
	for (int i = 0; i < m; i++) {
	    double[] rowA = A[i];
	    double[] rowR = R[i];
	    int jj = n-1;
	    int nhalf = ((n-1)/2) + 1;
	    for (int j = 0; j < nhalf; j++, jj--) {
		double tmp = rowA[j];
		rowR[j] = rowA[jj];
		rowR[jj] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the rows in a matrix, providing a matrix to return
     * and specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the dimensions large enough so that
     * R[i][j] or A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflectRows(double[][] R, double[][] A,
				       int m, int n)
	throws IllegalArgumentException
    {

	if (R.length < m || R[0].length < n) {
	    int d1 = R.length;
	    int d2 = (R[0] == null)? 0: R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length < m || A[0].length < n) {
	    int d1 = A.length;
	    int d2 = (A[0] == null)? 0: A[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	for (int i = 0; i < m; i++) {
	    double[] rowR = R[i];
	    double[] rowA = A[i];
	    int jj = n - 1;
	    int nhalf = ((n-1)/2) + 1;
	    for (int j = 0; j < nhalf; j++, jj--) {
		double tmp = rowA[j];
		rowR[j] = rowA[jj];
		rowR[jj] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the rows in a matrix, specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflectRows(double[][]A, int m, int n) {
	double[][] result =  new double[m][n];
	return reflectRows(result, A, m, n);
    }

    /**
     * Reflect the rows in a matrix, specifying dimensions and the
     * column order..
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflectRows(double[] A, int m, int n,
				       boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[m*n];
	return reflectRows(result, A, m, n, colOrder);
    }

    /**
     * Reflect the rows in a matrix, specifying dimensions and the
     * column order, and providing a matrix to hold the results.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflectRows(double[] R,
				     double[] A,
				     int m, int n,
				     boolean colOrder)
	throws IllegalArgumentException
    {
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (colOrder) {
	    for (int i = 0; i < m; i++) {
		int jj = n - 1;
		int nhalf = ((n-1)/2) + 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    double tmp = A[i+m*j];
		    R[i+m*j] = A[i+m*jj];
		    R[i+m*jj] = tmp;
		}
	    }
	} else {
	    for (int i = 0; i < m; i++) {
		int jj = n - 1;
		int nhalf = ((n-1)/2) + 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    double tmp = A[i*n+j];
		    R[i*n+j] = A[i*n+jj];
		    R[i*n+jj] = tmp;
		}
	    }
	}
	return R;
    }

    /**
     * Reflect the rows in an interleaved flat matrix, storing the
     * results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflectRows(double[] R, int offsetR,
				   double[] A, int offsetA,
				   int m, int n,
				   boolean colOrder,
				   int indR, int vlenR,
				   int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	if (colOrder) {
	    for (int i = 0 ; i < m; i++) {
		int nhalf = ((n-1)/2)+1;
		int jj = n - 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    double tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(i+m*jj)];
		    R[offsetR+vlenR*(i+m*jj)] = tmp;
		}
	    }
	} else {
	    for (int i = 0 ; i < m; i++) {
		int nhalf = ((n-1)/2)+1;
		int jj = n - 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    double tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(i*n+jj)];
		    R[offsetR + vlenR*(i*n+jj)] = tmp;
		}
	    }
	}
    }

    /**
     * Reflect the rows in an interleaved flat matrix, storing the
     * results and representing the matrices by arrays of floats.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>ik</sub> where k = n-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflectRows(float[] R, int offsetR,
				   float[] A, int offsetA,
				   int m, int n,
				   boolean colOrder,
				   int indR, int vlenR,
				   int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	if (colOrder) {
	    for (int i = 0 ; i < m; i++) {
		int nhalf = ((n-1)/2)+1;
		int jj = n - 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    float tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(i+m*jj)];
		    R[offsetR+vlenR*(i+m*jj)] = tmp;
		}
	    }
	} else {
	    for (int i = 0 ; i < m; i++) {
		int nhalf = ((n-1)/2)+1;
		int jj = n - 1;
		for (int j = 0; j < nhalf; j++, jj--) {
		    float tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(i*n+jj)];
		    R[offsetR + vlenR*(i*n+jj)] = tmp;
		}
	    }
	}
    }

    /**
     * Reflect the columns in a matrix.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * @param A the orgininal matrix
     * @return the reflected matrix
     */
    public static double[][] reflectColumns(double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	double[][] result =  new double[m][n];
	return reflectColumns(result, A);
    }

    /**
     * Reflect the columns in a matrix, providing a matrix to return.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the same dimensions.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @return the reflected matrix R
     */
    public static double[][] reflectColumns(double[][] R, double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	if (R.length != m || R[0].length != n) {
	    int d1 = R.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length != m || A[0].length != n) {
	    int d1 = A.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	for (int i = 0; i < mhalf; i++, ii--) {
	    for (int j = 0; j < n; j++) {
		double tmp = A[i][j];
		R[i][j] = A[ii][j];
		R[ii][j] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the columns in a matrix, providing a matrix to return
     * and specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the dimensions large enough so that
     * R[i][j] or A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflectColumns(double[][] R, double[][] A,
					    int m, int n)
	throws IllegalArgumentException
    {

	if (R.length < m || R[0].length < n) {
	    int d1 = R.length;
	    int d2 = (R[0] == null)? 0: R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length < m || A[0].length < n) {
	    int d1 = A.length;
	    int d2 = (A[0] == null)? 0: A[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	for (int i = 0; i < mhalf; i++, ii--) {
	    for (int j = 0; j < n; j++) {
		double tmp = A[i][j];
		R[i][j] = A[ii][j];
		R[ii][j] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the columns in a matrix, specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflectColumns(double[][]A, int m, int n) {
	double[][] result =  new double[m][n];
	return reflectColumns(result, A, m, n);
    }

    /**
     * Reflect the columns in a matrix, specifying dimensions and the
     * column order..
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflectColumns(double[] A, int m, int n,
					  boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[m*n];
	return reflectColumns(result, A, m, n, colOrder);
    }

    /**
     * Reflect the columns in a matrix, specifying dimensions and the
     * column order, and providing a matrix to hold the results.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflectColumns(double[] R,
					  double[] A,
					  int m, int n,
					  boolean colOrder)
	throws IllegalArgumentException
    {
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    double tmp = A[i+m*j];
		    R[i+m*j] = A[ii+m*j];
		    R[ii+m*j] = tmp;
		}
	    }
	} else {
	    for (int i = 0; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    double tmp = A[i*n+j];
		    R[i*n+j] = A[ii*n+j];
		    R[ii*n+j] = tmp;
		}
	    }
	}
	return R;
    }

    /**
     * Reflect the columns in an interleaved flat matrix, storing the
     * results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflectColumns(double[] R, int offsetR,
				      double[] A, int offsetA,
				      int m, int n,
				      boolean colOrder,
				      int indR, int vlenR,
				      int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    double tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(ii+m*j)];
		    R[offsetR+vlenR*(ii+m*j)] = tmp;
		}
	    }
	} else {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    double tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(ii*n+j)];
		    R[offsetR + vlenR*(ii*n+j)] = tmp;
		}
	    }
	}
    }

    /**
     * Reflect the columns in an interleaved flat matrix, storing the
     * results and representing the matrices by arrays of floats.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflectColumns(float[] R, int offsetR,
				      float[] A, int offsetA,
				      int m, int n,
				      boolean colOrder,
				      int indR, int vlenR,
				      int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	int ii = m - 1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    float tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(ii+m*j)];
		    R[offsetR+vlenR*(ii+m*j)] = tmp;
		}
	    }
	} else {
		for (int i = 0 ; i < mhalf; i++, ii--) {
		for (int j = 0; j < n; j++) {
		    float tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(ii*n+j)];
		    R[offsetR + vlenR*(ii*n+j)] = tmp;
		}
	    }
	}
    }

    /**
     * Reflect the rows and columns in a matrix.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * @param A the orgininal matrix
     * @return the reflected matrix
     */
    public static double[][] reflect(double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	double[][] result =  new double[m][n];
	return reflect(result, A);
    }

    /**
     * Reflect the rows and columns in a matrix, providing a matrix to return.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the same dimensions.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @return the reflected matrix R
     */
    public static double[][] reflect(double[][] R, double[][] A)
	throws IllegalArgumentException
    {
	int m = A.length;
	int n = A[0].length;
	if (R.length != m || R[0].length != n) {
	    int d1 = R.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length != m || A[0].length != n) {
	    int d1 = A.length;
	    int d2 = R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	for (int i = 0; i < mhalf; i++, ii--) {
	    int jj = n-1;
	    for (int j = 0; j < n; j++, jj--) {
		double tmp = A[i][j];
		R[i][j] = A[ii][jj];
		R[ii][jj] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the rows and columns in a matrix, providing a matrix to return
     * and specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * R and A must have the dimensions large enough so that
     * R[i][j] or A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * @param R the reflected matrix
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflect(double[][] R, double[][] A,
					    int m, int n)
	throws IllegalArgumentException
    {

	if (R.length < m || R[0].length < n) {
	    int d1 = R.length;
	    int d2 = (R[0] == null)? 0: R[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length < m || A[0].length < n) {
	    int d1 = A.length;
	    int d2 = (A[0] == null)? 0: A[0].length;
	    String msg = errorMsg("badDimensions", d1, d2, m, n);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	for (int i = 0; i < mhalf; i++, ii--) {
	    int jj = n-1;
	    for (int j = 0; j < n; j++, jj--) {
		double tmp = A[i][j];
		R[i][j] = A[ii][jj];
		R[ii][jj] = tmp;
	    }
	}
	return R;
    }

    /**
     * Reflect the rows and columns in a matrix, specifying dimensions.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @return the reflected matrix R
     */
    public static double[][] reflect(double[][]A, int m, int n) {
	double[][] result =  new double[m][n];
	return reflect(result, A, m, n);
    }

    /**
     * Reflect the rows and columns in a matrix, specifying dimensions and the
     * column order..
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflect(double[] A, int m, int n,
					  boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[m*n];
	return reflect(result, A, m, n, colOrder);
    }

    /**
     * Reflect the rows and columns in a matrix, specifying dimensions and the
     * column order, and providing a matrix to hold the results.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param A the orgininal matrix
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @return the reflected matrix R
     */
    public static double[] reflect(double[] R,
					  double[] A,
					  int m, int n,
					  boolean colOrder)
	throws IllegalArgumentException
    {
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++, jj--) {
		    double tmp = A[i+m*j];
		    R[i+m*j] = A[ii+m*jj];
		    R[ii+m*jj] = tmp;
		}
	    }
	} else {
	    for (int i = 0; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++, jj--) {
		    double tmp = A[i*n+j];
		    R[i*n+j] = A[ii*n+jj];
		    R[ii*n+jj] = tmp;
		}
	    }
	}
	return R;
    }

    /**
     * Reflect the rows and columns in an interleaved flat matrix, storing the
     * results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflect(double[] R, int offsetR,
				      double[] A, int offsetA,
				      int m, int n,
				      boolean colOrder,
				      int indR, int vlenR,
				      int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++, jj--) {
		    double tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(ii+m*jj)];
		    R[offsetR+vlenR*(ii+m*jj)] = tmp;
		}
	    }
	} else {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++, jj--) {
		    double tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(ii*n+jj)];
		    R[offsetR + vlenR*(ii*n+jj)] = tmp;
		}
	    }
	}
    }

    /**
     * Reflect the rows and columns in an interleaved flat matrix of
     * single-precision real numbers, storing the results.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * Each row will have its entries listed in the opposite order:
     * For an m by n matrix A, its reflected matrix R satisfies
     * R<sub>ij</sub> = A<sub>kj</sub> where k = m-1-j for indices that
     * start at 0.
     * <P>
     * A must have the dimensions large enough so that
     * A[i][j] are with the array bounds for i &isin; [0,m)
     * and j &isin; [0, m).  Elements outside this range are ignored.
     * The matrix returned will be as small as possible.
     * The matrix elements will be stored in a one-dimensional array
     * in either row major order or column major order.
     * @param R the reflected matrix, the elements of which will be set
     * @param offsetR the offset into the array R at which the reflected
     *        matrix starts
     * @param A the orgininal matrix
     * @param offsetA the offset in the array A at which the matrix to
     *        be reflected starts
     * @param m the number of rows
     * @param n the number of columns
     * @param colOrder true for column major order; false for row major order
     * @param indR The index into each vector provided by each element of R
     * @param vlenR the length of the vectors that are elements of R
     * @param indA The index into each vector provided by each element of A
     * @param vlenA the length of the vectors that are elements of A
     */
    public static void reflect(float[] R, int offsetR,
				      float[] A, int offsetA,
				      int m, int n,
				      boolean colOrder,
				      int indR, int vlenR,
				      int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A) {
	    if (vlenR != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	    int vnmR = vlenR*n*m;
	    if(Math.abs(offsetA - offsetR) < vnmR &&
	       (offsetA != offsetR)) {
		throw new IllegalArgumentException(errorMsg("offsets"));
	    }
	}
	if (A.length < n*m) {
	    String msg = errorMsg("lengthError", A.length, n*m);
	    throw new IllegalArgumentException(msg);
	}

	if (R.length < n*m) {
	    String msg = errorMsg("lengthError", R.length, n*m);
	    throw new IllegalArgumentException(msg);
	}
	if (A.length -offsetA < vlenA*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (R.length -offsetR < vlenR*n*m) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	int ii = m-1;
	int mhalf = ((m-1)/2) + 1;
	if (colOrder) {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++, jj--) {
		    float tmp = A[offsetA + vlenA*(i+m*j)];
		    R[offsetR+vlenR*(i+m*j)] = A[offsetA+vlenA*(ii+m*jj)];
		    R[offsetR+vlenR*(ii+m*jj)] = tmp;
		}
	    }
	} else {
	    for (int i = 0 ; i < mhalf; i++, ii--) {
		int jj = n-1;
		for (int j = 0; j < n; j++) {
		    float tmp = A[offsetA + vlenA*(i*n+j)];
		    R[offsetR + vlenR*(i*n+j)] = A[offsetA + vlenA*(ii*n+jj)];
		    R[offsetR + vlenR*(ii*n+jj)] = tmp;
		}
	    }
	}
    }

    /**
     * Multiply a matrix by a scalar
     * @param s the scalar
     * @param matrix a matrix
     */
    public static double[][] multiply(double s, double[][] matrix)
    {
	double[][] result = new double[matrix.length][matrix[0].length];
	return multiply(result, s, matrix);
    }

    /**
     * Multiply a matrix by a scalar, storing the results.
     * The matrices may be identical.
     * @param result the matrix to store the results
     * @param s the scalar
     * @param matrix a matrix
     * @exception IllegalArgumentException the dimensions of the matrices
     *             are not compatible
     */
    public static double[][] multiply(double[][] result,
				      double s,
				      double[][] matrix)
	throws IllegalArgumentException
    {
	if (result.length != matrix.length
	    || result[0].length != matrix[0].length) {
	    
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	int n = matrix.length;
	int m = matrix[0].length;
	for (int i = 0; i < n; i++) {
	    if (result[i].length != matrix[i].length
		|| matrix[i].length != m) {
		throw new IllegalArgumentException(errorMsg("dimensions"));
	    }
	    for (int j = 0; j < m; j++) {
		result[i][j] = s * matrix[i][j];
	    }
	}
	return result;
    }

    /**
     * Multiply a flattened matrix (or a vector) by a scalar.
     * The matrices may be identical.
     * @param s the scalar
     * @param matrix a matrix
     * @exception IllegalArgumentException the dimensions of the matrices
     *             are not compatible
     */
    public static double[] multiply(double s, double[] matrix)
	throws IllegalArgumentException
    {
	double[] result = new double[matrix.length];
	return multiply(result, s, matrix);
    }
    
    /**
     * Multiply a flattened matrix (or a vector) by a scalar,
     * storing the results.
     * @param result the matrix to store the results
     * @param s the scalar
     * @param matrix a matrix
     * @return the matrix result
     * @exception IllegalArgumentException the result matrix has the
     *            wrong length
     */
    public static double[] multiply(double[] result, double s, double[] matrix)
	throws IllegalArgumentException
    {
	if (result.length != matrix.length) {
	    String msg = errorMsg("lengthError", result.length, matrix.length);
	    throw new IllegalArgumentException(msg);
	}
	for (int i = 0; i < result.length; i++) {
	    result[i] = s * matrix[i];
	}
	return result;
    }


    /**
     * Multiply flattened matrix by a scalar and store the results in
     * a second flattened matrix, when matrix elements are interleaved.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * @param n the number of rows
     * @param m the number of columns
     * @param R the result matrix
     * @param offsetR the index into R at which the results should be stored
     * @param A the  matrix being multiplied
     * @param offsetA the offset into A for the first matrix
     * @param indR the index specifying  the component of the vector
     *        representing an element of R
     * @param vlenR the length of the vectors that are the components of R
     * @param indA the index specifying  the component of the vector
     *        representing an element of A
     * @param vlenA the length of the vectors that are the components of A
     * @exception IllegalArgumentException arguments are out of range or
     *            inconsistent
     *
     */
    public static void multiply(int n, int m,
				double[] R, int offsetR,
				double s,
				double[] A, int offsetA,
				int indR, int vlenR,
				int indA, int vlenA)
	throws IllegalArgumentException
    {
	if (R == A && (vlenR != vlenA)) {
	    throw new IllegalArgumentException(errorMsg("vectors"));
	}
	int len = n*m;
	if (R.length - offsetR < len) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (A.length - offsetA < len) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}

	offsetR += indR;
	offsetA += indA;

	for (int i = 0; i < len; i++) {
	    R[offsetR +vlenR*i] = s * A[offsetA + vlenA*i];
	}
    }



    /**
     * Multiply a matrix with a vector
     * @param A the matrix
     * @param B the vector
     * @return the vector containing the product
     * @exception IllegalArgumentException The dimensions of A and B are not
     *            compatible with matrix multiplication, or C has the
     *            wrong length
     */
    public static double[] multiply(double[][] A, double[] B)
	throws IllegalArgumentException
    {
	double result[] = new double[A.length];
	return multiply(result, A, B);
    }

    /**
     * Multiply a matrix with a vector, storing the results in a specified
     * vector
     * @param C the vector storing the result
     * @param A the matrix
     * @param B the vector
     * @return the vector containing the product
     * @exception IllegalArgumentException The dimensions of A and B are not
     *            compatible with matrix multiplication, or C has the
     *            wrong length
     */
    public static double[] multiply(double[] C, double[][] A, double[] B)
    {
	if (C.length != A.length) {
	    String msg = errorMsg("lengthError", C.length, A.length);
	    throw new IllegalArgumentException(msg);
	}
	if (A[0].length != B.length) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	double[] result;
	if (C == B) {
	    result = new double[A.length];
	} else {
	    result = C;
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < A.length; i++) {
	    state.c = 0.0;
	    state.total = 0.0;
	    for (int j = 0; j < B.length; j++) {
		double term = A[i][j]*B[j];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    result[i] += state.total;
	}
	if (C == B) {
	    System.arraycopy(result, 0, C, 0, result.length);
	}
	return result;
    }


    /**
     * Multiple two matrices
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix product AB
     * @exception IllegalArgumentException the dimensions of A,  B and C
     *            are not compatible
     */
    public static double[][] multiply(double[][] A, double[][] B)
	throws IllegalArgumentException
    {
	int n = A.length;
	int m = B.length;
	int m2 = B[0].length;
	double[][] result = new double[n][m2];
	return multiply(result, A, B);
    }

    // When the matrices are sufficiently large, we'll use a
    // temporary variable (an array of double) to store B's columns
    // as each column is used repeatedly.
    // 
    // Min rows for A
    private static final int CACHE_LIMIT1 = 64;
    // min columns for B
    private static final int CACHE_LIMIT2 = 4096;

    /**
     * Multiple two matrices, storing the results in a third matrix.
     * The result matrix C may be equal to A or B, provided these are
     * square matrices.
     * @param C the matrix whose value will be changed to the matrix
     *          product AB
     * @param A the first matrix
     * @param B the second matrix
     * @return the result matrix C
     * @exception IllegalArgumentException the dimensions of A,  B and C
     *            are not compatible
     */
    public static double[][] multiply(double[][] C,
				      double[][] A,
				      double[][] B)
	throws IllegalArgumentException
    {
	int nC = C.length;
	int mC = C[0].length;
	int nA = A.length;
	int mA = A[0].length;
	int nB = B.length;
	int mB = B[0].length;
	double[][] result;
	
	if (mA != nB || nC != nA || mC != mB) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}

	if (C != A && C != B) {
	    result = C;
	} else {
	    result = new double[nA][mB];
	}
	boolean small = (nA < CACHE_LIMIT1) && (mB*nA < CACHE_LIMIT2);

	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	if (small) {
	    for (int j = 0; j < mB; j++) {
		for (int i = 0; i < nA; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < mA; k++) {
			double term = A[i][k]*B[k][j];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    result[i][j] = state.total;
		}
	    }
	} else {
	    double[] vector = new double[mA];
	    for (int j = 0; j < mB; j++) {
		for (int k = 0; k < mA; k++) {
		    vector[k] = B[k][j];
		}
		for (int i = 0; i < nA; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < mA; k++) {
			double term = A[i][k]*vector[k];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    result[i][j] = state.total;
		}
	    }
	    vector = null;
	}
	if (result != C) {
	    for (int i = 0; i < nA; i++) {
		System.arraycopy(result[i], 0, C[i], 0, mB);
	    }
	}
	return result;
    }

    /**
     * Multiply two flattened matrices and return a flattened matrix
     * @param A the first matrix
     * @param B the second matrix
     * @param mA the number of rows for matrix A
     * @param nA the number of columns for matrix A
     * @param mB the number of rows for matrix B
     * @param nB the number of columns for matrix B
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @return a flattened matrix containing the matrix product AB
     * @exception IllegalArgumentException one of more of the matrices,
     *            represented by an array, are too short or the dimensions
     *            of the matrices are not compatible for matrix multiplication
     */
    public static double[] multiply(double[] A, int mA, int nA,
				    double[] B, int mB, int nB,
				    boolean colOrder)
	throws IllegalArgumentException
    {
	double[] result = new double[nA*mB];
	return multiply(result, mA, nB,
			A, mA, nA, B, mB, nB,
			colOrder);
    }

    /**
     * Multiply two flattened matrices and store the results in
     * a third flattened matrix.
     * The matrix C may be equal to A or B provided that these are
     * square matrices.
     * @param C the flattened matrix that will hold the result
     * @param mC the number of rows for matrix A
     * @param nC the number of columns for matrix A
     * @param A the first flattened matrix
     * @param mA the number of rows for matrix A
     * @param nA the number of columns for matrix A
     * @param B the second flattened matrix
     * @param mB the number of rows for matrix B
     * @param nB the number of columns for matrix B
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @return the matrix C
     * @exception IllegalArgumentException one of more of the matrices,
     *            represented by an array, are too short or the dimensions
     *            of the matrices are not compatible for matrix multiplication
     */
    public static double[] multiply(double[] C, int mC, int nC,
				    double[] A, int mA, int nA,
				    double[] B, int mB, int nB,
				    boolean colOrder)
    {
	if (A.length < nA*mA || B.length < nB*mB ||C.length < nC*mC) {
	    throw new IllegalArgumentException(errorMsg("dimlen"));
	}

	if (nA != mB || mC != mA || nC != nB) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	double[] result;
	if (C != A && C != B) {
	    result = C;
	} else {
	    result = new double[nA*mB];
	}

	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	if (colOrder) {
	    for (int i = 0; i < mA; i++) {
		for (int j = 0; j < nB; j++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    double tmp = 0.0;
		    for (int k = 0; k < nA; k++) {
			double term = A[i+mA*k]*B[k+mB*j];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    result[i+mA*j] = state.total;
		}
	    }
	} else {
	    for (int i = 0; i < mA; i++) {
		for (int j = 0; j < nB; j++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < nA; k++) {
			double term = A[i*nA+k]*B[k*nB+j];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    result[i*nB+j] = state.total;
		}
	    }
	}
	if (C == A || C == B) {
	    System.arraycopy(result, 0, C, 0, result.length);
	}
	return result;
    }


    /**
     * Multiply flattened matrices and store the results in
     * a third flattened matrix, when matrix elements are interleaved.
     * The class documentation for {@link MatrixOps} describes
     * interleaved matrices, which are effectively matrices of vectors
     * where an operation applies to a specific component of the vector.
     * <P>
     * This method has a large number of parameters. It is used by
     * some classes in the org.bzdev.geom package involving B&eacute;zier
     * surfaces. For example, the control points of a B&eacute;zier
     * patch with coordinate (u, v) can be represented by a 4 by 4 matrix
     * P whose components P<sub>ij</sub> are points in a three dimensional
     * space. The index i is used with u and the index j with v.
     * Points with the same value of v are stored together in the
     * order of increasing i in a flattened matrix (i.e., the  points are
     * stored in column-major order). Each of these
     * points is represented by three contiguous numbers representing
     * the control points' X, Y, and Z coordinates.  Splitting a
     * B&eacute;zier involves computing the control points for a new
     * patch, and this can be done by creating a matrix with the
     * same indices but containing only one of the coordinates
     * (X, Y, or Z).  This matrix is then multiplied with two fixed
     * matrices.  This method allows the multiplication to be done in
     * place. For example,
     * <BlOCKQUOTE><CODE><PRE>
     *   for (int k = 0; k &lt; 3; k++) {
     *       MatrixOps.multiply(C, 4, 4, 0,
     *                          P, 4, 4, patchOffset,
     *                         SR, 4, 4, 0,
     *                         true,
     *                         k, 3,
     *                         k, 3,
     *                         0, 1);
     *   }
     * </PRE></CODE></BLOCKQUOTE>
     * will perform one of these multiplications. The matrix SR is a
     * constant matrix - the same for X, Y, and Z - so its elements are
     * a one dimensional vector (i.e., a single number). The other
     * two are vectors with three dimensions. The patch offset allows
     * one out of a multiple of matrices stored in the same array to
     * be referenced.
     * <P>
     * The arrays can be identical arrays but the regions of the arrays
     * representing matrices must not partially overlap.
     * @param C the flattened matrix that will hold the result, the
     *        matrix product AB
     * @param mC the number of rows for matrix C
     * @param nC the number of columns for matrix C
     * @param offsetC the offset into C at which the matrix starts
     *        (i.e, the index in C of the first element of the matrix)
     * @param A the first flattened matrix
     * @param mA the number of rows for matrix A
     * @param nA the number of columns for matrix A
     * @param offsetA the offset into A at which the matrix starts
     *        (i.e, the index in A of the first element of the matrix)
     * @param B the second flattened matrix
     * @param mB the number of rows for matrix B
     * @param nB the number of columns for matrix B
     * @param offsetB the offset into B at which the matrix starts
     *        (i.e, the index in B of the first element of the matrix)
     * @param colOrder true for column-major order; false for row-major
     *        order
     * @param indC the index of the vector component for C to use for this
     *        computation
     * @param vlenC the vector length for an element of C
     * @param indA the index of the vector component for A to use for this
     *        computation
     * @param vlenA the vector length for an element of A
     * @param indB the index of the vector component for B to use for this
     *        computation
     * @param vlenB the vector length for an element of B
     * @exception the vector lengths are not incorrect or an array is
     *            not long enough to hold the specified number of rows
     *            and columns, or the offsets are not appropriate
     */
    public static void multiply(double[] C, int mC, int nC, int offsetC,
				double[] A, int mA, int nA, int offsetA,
				double[] B, int mB, int nB, int offsetB,
				boolean colOrder,
				int indC, int vlenC,
				int indA, int vlenA,
				int indB, int vlenB)
    {
	if (C == A) {
	    if (vlenC != vlenA) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	}

	if (C == B) {
	    if (vlenC != vlenB) {
		throw new IllegalArgumentException(errorMsg("vectors"));
	    }
	}

	if (A.length - offsetA < vlenA*nA*mA || B.length -offsetB < vlenB*nB*mB
	    || C.length - offsetC < vlenC*nC*mC) {
	    throw new IllegalArgumentException(errorMsg("offsets"));
	}
	if (nA != mB || mC != mA || nC != nB) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}

	offsetC += indC;
	offsetA += indA;
	offsetB += indB;

	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	if (colOrder) {
	    for (int i = 0; i < mA; i++) {
		for (int j = 0; j < nB; j++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    double tmp = 0.0;
		    for (int k = 0; k < nA; k++) {
			double term = A[offsetA + vlenA*(i+mA*k)]
			    * B[offsetB + vlenB*(k+mB*j)];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    C[offsetC + vlenC*(i+mA*j)] = state.total;
		}
	    }
	} else {
	    for (int i = 0; i < mA; i++) {
		for (int j = 0; j < nB; j++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < nA; k++) {
			double term = A[offsetA + vlenA*(i*nA+k)]
			    * B[offsetB + vlenB*(k*nB+j)];
			double y = term - state.c;
			double t = state.total + y;
			state.c = (t - state.total) - y;
			state.total = t;
		    }
		    C[offsetC + vlenC*(i*nB+j)] = state.total;
		}
	    }
	}
    }

    /**
     * Subtract two matrices.
     * @param A the first matrix
     * @param B the second matrix * @return the matrix difference A-B
     * @exception IllegalArgumentException The matrix dimensions are
     *            not consistent
     */
    public static double[][] subtract(double[][] A, double[][] B)
	throws IllegalArgumentException
    {
	double[][] result = new double[A.length][A[0].length];
	return subtract(result, A, B);
    }


    /**
     * Subtract two matrices and store the results.
     * @param R the result matrix
     * @param A the first matrix
     * @param B the second matrix
     * All matrices must have the same number of rows and columns
     * The matrices can be identical objects.
     * @return the matrix difference A-B
     * @exception IllegalArgumentException The matrix dimensions are
     *            not consistent
     */
    public static double[][] subtract(double[][] R,
				 double[][] A,
				 double[][] B)
	throws IllegalArgumentException
    {
	int nR = R.length;
	int mR = R[0].length;
	int nA = A.length;
	int mA = A[0].length;
	int nB = B.length;
	int mB = B[0].length;
	if (nR != nA || nA != nB || mR != mA || mA != mB) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	for (int i = 0; i < nR; i++) {
	    for (int j = 0; j < mR; j++) {
		R[i][j] = A[i][j] - B[i][j];
	    }
	}
	return R;
    }

    /**
     * Subtract two flat matrices.
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix difference A-B
     * @exception IllegalArgumentException
     */
    public static double[] subtract(double[] A, double[] B)
	throws IllegalArgumentException
    {
	double[] result = new double[A.length];
	return subtract(result, A, B);
    }

    /**
     * Subtract two flat matrices and store the results.
     * The matrices can be identical objects.
     * @param R the result matrix
     * @param A the first matrix
     * @param B the second matrix
     * @return the matrix difference A-B
     * @exception IllegalArgumentException the arrays R, A, and B do
     *            not have the same lengths
     */
    public static double[] subtract(double[] R, double[] A, double[] B)
	throws IllegalArgumentException
    {
	if (R.length != A.length || A.length != B.length) {
	    throw new IllegalArgumentException(errorMsg("dimensions"));
	}
	for (int i = 0; i < A.length; i++) {
	    R[i] = A[i] - B[i];
	}
	return R;
    }

}

//  LocalWords:  exbundle colOrder unflattened nA mA nB mB eacute ij
//  LocalWords:  zier indices BlOCKQUOTE PRE lt MatrixOps patchOffset
//  LocalWords:  nC mC offsetC offsetA offsetB indC vlenC indA vlenA
//  LocalWords:  indB vlenB unflatten versa boolean controlPoints ijx
//  LocalWords:  IllegalArgumentException badDimensions nR
//  LocalWords:  ResultMatrixTooShort lengthError mR expectingSquare
//  LocalWords:  offsetR indR vlenR notSquare dimlen
//  LocalWords:  resultMatrixTooShort
