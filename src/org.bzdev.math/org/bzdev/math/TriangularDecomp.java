package org.bzdev.math;

/**
 * Interface representing common operations for triangular decompositions
 * of matrices.  The BZDev class library contains implementations of
 * two decompositions: LU decomposition and Cholesky decomposition.
 * If a matrix has a Cholesky decomposition, the matrix must be a
 * Hermetian positive semi-definite matrix.  Java does not have built-in
 * operations for complex numbers, however, so for this class library,
 * Hermetian matrices must be symmetric matrices.  An LU decomposition
 * does not have this constraint, but is slower to compute.
 * <P>
 * We assume that the decomposition of a matrix A determines matrices
 * L, U, and P where L is a lower triangular matrix, U is an upper
 * triangular matrix, P is a permutation matrix, and LU = PA.
 * For the case of Cholesky decomposition, P will be the identity matrix
 * and U will be the transpose of L.
 * <P>
 * LU and Cholesky decompositions are used for a number of purposes,
 * including solving linear equations, and computing the inverses of
 * matrices.  An interface for common operations is useful for cases
 * where the type of decomposition to use can only be determined at
 * run time.  For example, in cases where Cholesky decomposition will
 * work in the vast majority of cases, one could use the following code:
 * <blockquote><pre>
 *      public TriangularDecomp decompose(double[][] matrix) {
 *         try {
 *           return CholeskyDecomp(matrix);
 *         } catch (Exception e) {
 *           return LUDecomp(matrix);
 *         }
 *      }
 * </pre></blockquote>
 * This method tries Cholesky decomposition, and if that fails, it falls
 * back on LU decomposition.  One can then use the TriangularDecomp
 * object returned to solve a set of linear equations or to compute the
 * inverse of the original matrix.
 */
public interface TriangularDecomp {
    /**
     * Get the number of rows in the triangular matrices associated with
     * an instance of this class.
     * @return the number of rows.
     */
    int getNumberOfRows();

    /**
     * Get the number of columns in the triangular matrices associated with
     * an instance of this class.
     * @return the number of columns.
     */
    int getNumberOfColumns();

    /**
     * Get the determinate of the matrix to which this decomposition applies.
     * @return the determinate
     * @exception IllegalStateException - if the matrix to which this
     *            decomposition applies is not a square matrix
     */
    double det() throws IllegalStateException;

    /**
     * Get the permutation to which this decomposition applies.
     * The permutation returned has the property that its getMatrix()
     * method returns the permutation matrix.
     * @return the permutation
     */
    Permutation getP();

    /**
     * Get the lower triangular matrix associated with the decomposition.
     * @return the lower triangular matrix
     */
    double[][] getL();


    /**
     * Get the upper triangular matrix associated with the decomposition.
     * For Cholesky decomposition, U is the transpose of the lower
     * triangular matrix returned by getL().
     * @return the upper triangular matrix
     */
    public double[][] getU();

    /**
     * Determine if the matrix to which this decomposition applies is
     * nonsingular.
     * @return true if the matrix is nonsingular; false otherwise
     */
    boolean isNonsingular();

    /**
     * Solve the system of linear equations Ax = b where x and b are
     * vectors and A is the matrix to which this decomposition applies.
     * @param b the vector b in the equation Ax = b
     * @return the vector x that satisfies the equation Ax = b
     * @exception IllegalArgumentException the argument has the wrong size
     * @exception IllegalStateException the matrix this object decomposed
     *            is a singular matrix
     */
    double[] solve(double[] b) throws
       IllegalArgumentException, IllegalStateException;

    /**
     * Solve the system of linear equations Ax = b where x and b are
     * vectors and A is the matrix to which this decomposition applies. The
     * vector x will be modified.
     * @param b the vector b in the equation Ax = b
     * @param x the vector x that satisfies the equation Ax = b
     * @exception IllegalArgumentException the argument has the wrong size
     * @exception IllegalStateException the matrix this object decomposed
     *            is a singular matrix
     */
    void solve(double[] x, double[] b)
	throws IllegalArgumentException, IllegalStateException;

    /**
     * Solve the system of linear equations AX = B where X and B are
     * matrices and A is the matrix to which this decomposition applies. The
     * matrix X will be modified.
     * @param b the matrix B in the equation AX = B
     * @param x the matrix X that satisfies the equation AX = B
     * @exception IllegalArgumentException the arguments have the wrong size
     * @exception IllegalStateException the matrix this object decomposed
     *            is a singular matrix
     */
    void solve(double[][] x, double[][] b)
	throws IllegalArgumentException, IllegalStateException;

    /**
     * Get the inverse of a matrix.
     * The matrix A whose inverse is computed satisfies PA = LU,
     * where P is the permutation matrix associated with this instance,
     * L is the lower triangular matrix associated with this instance,
     * and U is the upper triangular matrix associated with this instance.
     * <P>
     * For some cases (e.g., Cholesky decomposition), the permutation
     * matrix is the identity matrix. 
     * @return the inverse of the matrix that this instance decomposed
     * @throws IllegalStateException the matrix is singular
     */
    public double[][] getInverse();

    /**
     * Get the inverse of a matrix and store the inverse.
     * The matrix A whose inverse is computed satisfies PA = LU,
     * where P is the permutation matrix associated with this instance,
     * L is the lower triangular matrix associated with this instance,
     * and U is the upper triangular matrix associated with this instance.
     * @param result the matrix holding the inverse
     * @throws IllegalStateException the matrix is singular
     * @throws IllegalArgumentException - if result has the wrong dimensions
     */
    void getInverse(double[][] result)
	throws IllegalStateException, IllegalArgumentException;

    /**
     * Get the inverse of a matrix and store the inverse in a flat matrix.
     * The matrix A whose inverse is computed satisfies A = LL<sup>T</sup>,
     * where L is the lower triangular matrix associated with this instance.
     * @param result the matrix holding the inverse
     * @param columnMajorOrder true if the inverse is stored in column major
     *        order (e.g., the order used by Fortran); false if the inverse
     *        is stored in row-major-order (e.g., the order used by C)
     * @throws IllegalStateException the matrix is singular
     * @throws IllegalArgumentException - if result has the wrong dimensions
     */
    public void getInverse(double[] result, boolean columnMajorOrder)
	throws IllegalStateException, IllegalArgumentException;
}

//  LocalWords:  decompositions BZDev Cholesky Hermetian blockquote
//  LocalWords:  pre TriangularDecomp CholeskyDecomp LUDecomp getL
//  LocalWords:  IllegalStateException getMatrix nonsingular Fortran
//  LocalWords:  IllegalArgumentException columnMajorOrder
