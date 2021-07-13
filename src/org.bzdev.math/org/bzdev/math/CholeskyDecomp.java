package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Cholesky Decomposition class.
 * This class implements Cholesky decomposition, which factors
 * a matrix A so that A = LL<sup>T</sup>, where L is a lower-triangular
 * matrix. The matrix A must be positive definite and symmetric.
 * <P>
 * This class implements the TriangleDecomp interface, which
 * assumes that classes implementing it will create a
 * lower triangular matrix L, an upper triangular matrix U,
 * and a permutation matrix P such that PA = LU.  For the
 * case of Cholesky decomposition, P is the identity matrix
 * and U is the transpose of L.
 */

public class CholeskyDecomp implements TriangularDecomp {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private int n;
    private double determinate = 1.0;

    private double[][] L;

    private void init(double[][]A, double[][] L, int n)
	throws IllegalArgumentException, MathException
    {
	if (A.length < n) {
	    throw new IllegalArgumentException(errorMsg("colTooShort"));
	}
	if (L.length < n) {
	    throw new IllegalArgumentException(errorMsg("colTooShort"));
	}
	// Adder.Kahan adder = new Adder.Kahan();
	// Adder.Kahan.State state = adder.getState();
	Adder.Kahan.State state = new Adder.Kahan.State();
	for (int i = 0; i < n; i++) {
	    double sum;
	    if (A[i].length < n) {
		throw new IllegalArgumentException(errorMsg("rowTooShort"));
	    }
	    if (L[i].length < n) {
		throw new IllegalArgumentException(errorMsg("rowTooShort"));
	    }

	    for (int j = 0; i > j; j++) {
		// sum = 0.0;
		state.c = 0.0;
		state.total = 0.0;
		// adder.reset();
		for (int k = 0; k < j; k++) {
		    double term = L[i][k]*L[j][k];
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		    // sum += L[i][k]*L[j][k];
		}
		L[i][j] = (A[i][j] - state.total)/L[j][j];
		L[j][i] = L[i][j];
	    }
	    // sum = 0.0;
	    // adder.reset();
	    state.c = 0.0;
	    state.total = 0.0;
	    for (int k = 0; k < i; k++) {
		double term = L[i][k];
		term *= term;
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    double tmp = A[i][i];
	    tmp -= state.total;
	    determinate *= tmp;
	    if (tmp <= 0.0) {
		throw new MathException
		    (errorMsg("notPositiveDefinite", i, i, tmp));
	    }
	    L[i][i] = Math.sqrt(tmp);
	}
    }

    @Override
    public int getNumberOfRows() {return n;}

    @Override
    public int getNumberOfColumns() {return n;}

    private void checkMatrix(double[][] matrixA,  int n,
				 boolean strict)
	throws IllegalArgumentException
    {
	if (matrixA.length < n) {
		throw new
		    IllegalArgumentException
		    (errorMsg("missingRows"));
	}
	for (int i = 0; i < n; i++) {
	    double[] row = matrixA[i];
	    if (row == null) {
		throw new
		    IllegalArgumentException(errorMsg("missingRows"));
	    }
	    int len = row.length;
	    if ((strict && (len != n)) || (n < len || len == 0)) {
		throw new
		    IllegalArgumentException(errorMsg("wrongRowSize"));
	    }
	}
    }

    private int rowWidthCheck(double[][] matrixA) {
	for (int i = 0; i < matrixA.length; i++) {
	    int len = matrixA[i].length;
	    if (len < n) return -1;
	}
	return n;
    }

    private double[][] getMatrixA(double[] flatMatrix, boolean columnMajorOrder,
				  int n)
	throws IllegalArgumentException
    {
	if (flatMatrix.length < n*n) {
	    throw new IllegalArgumentException
		(errorMsg("flatMatrixTooShort", flatMatrix.length, n, n));
	}
	double[][] result = new double[n][n];
	int index = 0;
	if (columnMajorOrder) {
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < n; i++) {
		    result[i][j] = flatMatrix[index++];
		}
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    result[i][j] = flatMatrix[index++];
		}
	    }
	}
	return result;
    }

    /**
     * Constructor providing a  matrix and a workspace.
     * The dimensions of matrix are used to compute the number of
     * rows and columns. The matrix L must not be modified after the
     * constructor is called until the instance created is no longer
     * used. Both arguments may reference the same object, but in this
     * case the original matrix will be overwritten.
     * @param matrix the matrix whose LU decomposition is to be computed.
     * @param LU the matrix used to store the triangular matrices
     *       L and U.
     * @exception IllegalArgumentException the matrices are not consistent
     *            with each other
     */
    public CholeskyDecomp(double[][] matrix, double[][] LU)
	throws IllegalArgumentException, MathException
    {
	/*
	for (int i = 0; i < L.length; i++) {
	    for (int j = i; j < L.length; j++) {
		L[i][j] = 0.0;
	    }
	}
	*/
	n = matrix.length;
	init(matrix,LU, n);
	this.L = LU;
    }

    /**
     * Constructor.
     * @param matrix a matrix whose Cholesky decomposition is to be
     *        computed
     */
    public CholeskyDecomp(double[][] matrix) 
	throws IllegalArgumentException, MathException
    {
	n = matrix.length;
	L = new double[n][n];
	init(matrix,L, n);
    }

    /**
     * Constructor given a flat representation of a matrix.
     * The elements of the matrix can be stored in one of two orders.
     * Given a matrix A<sub>ij</sub>, if the row index varies the fastest,
     * the flat representation will consist of the sequence
     * A<sub>00</sub>, A<sub>10</sub>, .... Otherwise the sequence is
     * A<sub>00</sub>, A<sub> 01</sub>, ....
     * @param flatMatrix a one-dimensional array representing a two dimensional
     *        matrix
     * @param n the number of rows and columns
     * @exception IllegalArgumentException the flat matrix is too small to
     *        represent an m by n matrix
     */
    public CholeskyDecomp(double[] flatMatrix, int n)
	throws IllegalArgumentException
    {
	this.n = n;
	// The distinction between row major order and column major order
	// does not matter for symmetric matrices.
	double[][] matrixA = getMatrixA(flatMatrix, true, n);
	init(matrixA, matrixA, n);
	L = matrixA;
    }

    /**
     * Constructor specifying  matrix dimensions.
     * This is provided to allow a single array to be used multiple times
     * in order to avoid unnecessary array allocations.
     * @param A the matrix whose LU decomposition is to be computed
     * @param n the number of rows and columns
     * @param strict true if matrixA must have exactly the number of
     *        rows and columns required; false if matrixA may have rows
     *        or columns longer than needed, in which case the additional
     *        entries will be ignored
     * @exception IllegalArgumentException the matrix and other parameters
     *            are not consistent with each other
     */
    public CholeskyDecomp (double[][] A, int n, boolean strict)
	throws IllegalArgumentException
    {
	this.n = n;
	checkMatrix(A, n, strict);
	L = new double[n][n];
	init(A, L, n);
    }

    @Override
    public Permutation getP() {
	return new Permutation(n);
    }

    @Override
    public double[][] getL() {
	double[][] result = new double[n][n];
	for (int i = 0; i < n; i++) {
	    System.arraycopy(L[i], 0, result[i], 0, i+1);
	}
	return result;
    }

    @Override
    public double[][] getU() {
	double[][] result = new double[n][n];
	for (int i = 0; i < n; i++) {
	    System.arraycopy(L[i], i, result[i], i, n-i);
	}
	return result;
    }

    @Override
    public double det() {
	return determinate;
    }

    @Override
    public double[] solve(double[] b) throws
	IllegalArgumentException, IllegalStateException
    {
	if (b.length < n) {
	    throw new IllegalArgumentException
		(errorMsg("argArrayTooShort"));
	}
	if (!isNonsingular()) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	double[] x = new double[n];
	solve(x, b);
	return x;
    }

    @Override
    public void solve(double[] x, double[] b)
 	throws IllegalArgumentException, IllegalStateException
   {
	if (b.length != n || x.length != n ) {
	    throw new IllegalArgumentException(errorMsg("wrongVectorLen"));
	}
	if (determinate == 0.0) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	// forward substitution
	for (int i = 0; i < n; i++) {
	    double sum = b[i];
	    for (int k = 0; k < i; k++) {
		sum -= L[i][k]*x[k];
	    }
	    x[i] = sum/L[i][i];
	}
	// backward substitution
	for(int i = n-1; i >= 0; i--) {
	    double sum = x[i];
	    for (int k = i+1; k < n; k++) {
		sum -= L[i][k]*x[k];
	    }
	    x[i] = sum/L[i][i];
	}
    }

    @Override
    public void solve(double[][] x, double[][] b)
	throws IllegalArgumentException, IllegalStateException
    {
	if (b.length != n || x.length != n ) {
	    throw new IllegalArgumentException(errorMsg("wrongVectorLen"));
	}
	if (determinate == 0.0) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	double m = x[0].length;
	for (int j = 0; j < m; j++) {
	    // forward substitution
	    for (int i = 0; i < n; i++) {
		double sum = b[i][j];
		for (int k = 0; k < i; k++) {
		    sum -= L[i][k]*x[k][j];
		}
		x[i][j] = sum/L[i][i];
	    }
	    // backward substitution
	    for(int i = n-1; i >= 0; i--) {
		double sum = x[i][j];
		for (int k = i+1; k < n; k++) {
		    sum -= L[i][k]*x[k][j];
		}
		x[i][j] = sum/L[i][i];
	    }
	}
    }

    @Override
    public double[][] getInverse() throws IllegalStateException {
	double result[][] = new double[n][n];
	getInverse(result);
	return result;
    }

    @Override
    public boolean isNonsingular() {
	return determinate != 0.0;
    }

    @Override
    public void getInverse(double[][] result)
	throws IllegalStateException, IllegalArgumentException
    {
	if (!isNonsingular()) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	if (result.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	double[] column = new double[n];
	double[] x = new double[n];
	for (int j = 0; j < n; j++) {
	    if (result[j].length < n) {
		throw new IllegalArgumentException
		    (errorMsg("argSubArrayTooShort", j));
	    }
	    column[j] = 1.0;
	    solve(x, column);
	    for (int i = 0; i < n; i++) {
		result[i][j] = x[i];
	    }
	    column[j] = 0.0;
	}
    }

    @Override
    public void getInverse(double[] result, boolean columnMajorOrder)
	throws IllegalStateException, IllegalArgumentException
    {
	if (!isNonsingular()) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	if (result.length < n*n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	double[] column = new double[n];
	double[] x = new double[n];
	for (int j = 0; j < n; j++) {
	    column[j] = 1.0;
	    solve(x, column);
	    if (columnMajorOrder) {
		for (int i = 0; i < n; i++) {
		    result[i + n*j] = x[i];
		}
	    } else {
		for (int i = 0; i < n; i++) {
		    result[n*i+j] = x[i];
		}
	    }
	    column[j] = 0.0;
	}
    }
}

//  LocalWords:  exbundle Cholesky TriangleDecomp colTooShort Kahan
//  LocalWords:  getState rowTooShort notPositiveDefinite missingRows
//  LocalWords:  wrongRowSize flatMatrixTooShort ij flatMatrix
//  LocalWords:  IllegalArgumentException matrixA argArrayTooShort
//  LocalWords:  needNonsingular wrongVectorLen argSubArrayTooShort
