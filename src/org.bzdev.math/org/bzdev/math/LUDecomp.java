package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/**
 * LU Decomposition class.
 * This class implements LU decomposition, which for a matrix A
 * and permutation matrix P, factors PA so that PA = LU, where
 * L is a lower triangular matrix and U is an upper triangular
 * matrix.
 * <P>
 * The implementation is based on the one used in the Jama
 * package (a public-domain package provided via a collaboration
 * between MathWorks and the National Institute of Standards
 * and Technology).  Jama defines a matrix class whereas LUDecomp
 * treats matrices as two-dimensional arrays, and was written so
 * that the org.bzdev library is self-contained: a few classes in
 * that library have to solve systems of linear equations. It is
 * intended for incidental use where performance is important but
 * where a comprehensive linear algebra package is overkill.
 * <P>
 * LUDecomp uses the class Permutation to represent permutations,
 * so the use of the permutation matrix P is implicit. One can also
 * provide the matrix used to store the triangular matrices, which
 * avoids additional memory allocation when multiple sets of equations
 * have to be solved sequentially, and store the triangular matrices
 * in the same arrays used to store the matrix being decomposed, which
 * saves space when if the matrix being decomposed will no longer be
 * needed.
 * <P>
 * Note: this is a correct implementation of the algorithm. Sample
 * implementations shown at various web sites sometimes miss important
 * details.  For example, as of late 2013, the implementation shown at
 * <a href="http://rosettacode.org/wiki/LU_decomposition">rosettacode.org</a>
 * had (and maybe still has) an implementation that could fail in some
 * circumstances. It computes the permutation matrix so as to maximize
 * the value of elements of the original matrix on the diagonal, which
 * does not necessarily prevent the u_{jj} terms from vanishing. The
 * Jama implementation, by contrast, maximizes the absolute value of
 * u_{jj} when computing the permutation, which works in all cases.
 */
public class LUDecomp implements TriangularDecomp {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    int m;
    int n;

    @Override
    public int getNumberOfRows() {return m;}

    @Override
    public int getNumberOfColumns() {return n;}

    Permutation permutation;
    double[][] matrixLU;

    @Override
    public Permutation getP() {
	return permutation;
    }

    @Override
    public double[][] getL() {
	double[][] result = new double[n][n];
	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < i; j++) {
		result[i][j] = matrixLU[i][j];
	    }
	    result[i][i] = 1.0;
	}
	return result;
    }

    @Override
    public double[][] getU() {
	double[][] result = new double[n][n];
	for (int i = 0; i < m; i++) {
	    for (int j = i; j < n; j++) {
		result[i][j] = matrixLU[i][j];
	    }
	}
	return result;
    }

    @Override
    public double det() throws IllegalStateException {
	if (n != m) throw new IllegalStateException
			(errorMsg("needSquareMatrix"));
	// inverse has the same determinate as it is 1 or -1
	double result = permutation.det(); 
	for (int j = 0; j < n; j++) {
	    result *= matrixLU[j][j];
	}
	return result;
    }

    @Override
    public boolean isNonsingular() {
	if (n != m) {
	    return false; // A nonsingular matrix must be a square matrix
	}
	for (int j = 0; j < n; j++) {
	    if (matrixLU[j][j] == 0.0) return false;
	}
	return true;
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
	if (!isNonsingular()) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	permutation.applyTo(b, x);
	// forward substitution
	for (int k = 0; k < n; k++) {
	    for (int i = k+1; i < n; i++) {
		x[i] -= x[k]*matrixLU[i][k];
	    }
	}
	// backward substitution
	for (int k = n-1; k >= 0; k--) {
	    x[k] /= matrixLU[k][k];
	    for (int i = 0; i < k; i++) {
		x[i] -= x[k] * matrixLU[i][k];
	    }
	}
    }
 
    @Override
    public void solve(double[][] x, double[][] b)
	throws IllegalArgumentException, IllegalStateException
    {
	if (b.length != n || x.length != n ) {
	    throw new IllegalArgumentException(errorMsg("wrongVectorLen"));
	}
	if (!isNonsingular()) {
	    throw new IllegalStateException(errorMsg("needNonsingular"));
	}
	int nb = b[0].length;
	permutation.applyTo(b, x);
	// forward substitution
	for (int k = 0; k < n; k++) {
	    for (int i = k+1; i < n; i++) {
		for (int j = 0; j < nb; j++) {
		    x[i][j] -= x[k][j] * matrixLU[i][k];
		}
	    }
	}
	// backward substitution
	for (int k = n-1; k >= 0; k--) {
	    for (int j = 0; j < n; j++) {
		x[k][j] /= matrixLU[k][k];
	    }

	    for (int i = 0; i < k; i++) {
		for (int j = 0; j < nb; j++) {
		    x[i][j] -= x[k][j] * matrixLU[i][k];
		}
	    }
	}
    }

    @Override
    public double[][] getInverse() {
	double result[][] = new double[n][n];
	getInverse(result);
	return result;
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
	    if (result[j].length < m) {
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
		    result[i + m*j] = x[i];
		}
	    } else {
		for (int i = 0; i < n; i++) {
		    result[n*i+j] = x[i];
		}
	    }
	    column[j] = 0.0;
	}
    }

    /**
     * Constructor.
     * @param A the matrix whose LU decomposition is to be computed
     * @exception IllegalArgumentException the input matrix is ill formed
     */
    public LUDecomp (double[][]A)
	throws IllegalArgumentException
    {
	this(A, A.length, A[0].length, true);
    }    

    private void checkMatrix(double[][] matrixA, int m, int n,
				 boolean strict)
	throws IllegalArgumentException
    {
	if (matrixA.length < m) {
		throw new
		    IllegalArgumentException
		    (errorMsg("missingRows"));
	}
	for (int i = 0; i < m; i++) {
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
	int n = matrixA[0].length;
	for (int i = 1; i < matrixA.length; i++) {
	    int len = matrixA[i].length;
	    if (len != n) return -1;
	}
	return n;
    }

    private double[][] getMatrixA(double[] flatMatrix, boolean columnMajorOrder,
				  int m, int n)
	throws IllegalArgumentException
    {
	if (m <= 0) {
	    throw new
		IllegalArgumentException(errorMsg("intArgNotPositive", m));
	}
	if (n <= 0) {
	    throw new
		IllegalArgumentException(errorMsg("intArgNotPositive", n));
	}
	if (flatMatrix.length < m*n) {
	    throw new IllegalArgumentException
		(errorMsg("flatMatrixTooShort", flatMatrix.length, m, n));
	}
	double[][] result = new double[m][n];
	int index = 0;
	if (columnMajorOrder) {
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < m; i++) {
		    result[i][j] = flatMatrix[index++];
		}
	    }
	} else {
	    for (int i = 0; i < m; i++) {
		for (int j = 0; j < n; j++) {
		    result[i][j] = flatMatrix[index++];
		}
	    }
	}
	return result;
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
     * @param columnMajorOrder true if the inverse is stored in column major
     *        order (e.g., the order used by Fortran); false if the inverse
     *        is stored in row-major-order (e.g., the order used by C)
     * @param m the number of rows
     * @param n the number of columns
     * @exception IllegalArgumentException the flat matrix is too small to
     *        represent an m by n matrix
     */
    public LUDecomp(double[] flatMatrix, boolean columnMajorOrder, int m, int n)
	throws IllegalArgumentException
    {
	double[][] matrixA = getMatrixA(flatMatrix, columnMajorOrder, m, n);
	initLUP(/*matrixA,*/ matrixA);
    }

    /**
     * Constructor specifying  matrix dimensions.
     * This is provided to allow a single array to be used multiple times
     * in order to avoid unnecessary array allocations.
     * @param A the matrix whose LU decomposition is to be computed
     * @param m the number of rows
     * @param n the number of columns
     * @param strict true if matrixA must have exactly the number of
     *        rows and columns required; false if A may have rows
     *        or columns longer than needed, in which case the additional
     *        entries will be ignored
     * @exception IllegalArgumentException the matrix and other parameters
     *            are not consistent with each other
     */
    public LUDecomp (double[][] A, int m, int n, boolean strict)
	throws IllegalArgumentException
    {
	checkMatrix(A, m, n,  strict);
	if (m == A.length && n == rowWidthCheck(A)) {
	    // System.out.println("cloning to create matrixLU");
	    matrixLU = (double[][])A.clone();
	    if (A[0] == null || A[0].length == 0) {
		throw new IllegalArgumentException(errorMsg("notMatrix"));
	    }
	    if (matrixLU[0] == null || matrixLU[0] == A[0]) {
		matrixLU[0] = (double[]) A[0].clone();
	    }
	    for (int i = 1; i < n; i++) {
		if (A[i] == null || A[i].length != m) {
		    throw new IllegalArgumentException(errorMsg("notMatrix"));
		}
		if (matrixLU[i] == null || matrixLU[i] == A[i]) {
		    matrixLU[i] = (double[])A[i].clone();
		}
	    }
	} else {
	    // System.out.println("allocating to create matrixLU");
	    matrixLU = new double[m][n];
	    for (int i = 0; i < m; i++) {
		System.arraycopy(A[i], 0, matrixLU[i], 0, n);
	    }
	}
	initLUP(/*matrixA,*/ matrixLU);
    }

    /**
     * Constructor providing an LU matrix.
     * The dimensions of matrixLU are used to compute the number of
     * rows and columns. matrixLU must not be modified after the
     * constructor is called until the instance created is no longer
     * used.
     *
     * @param A the matrix whose LU decomposition is to be computed.
     * @param LU the matrix used to store the triangular matrices
     *       L and U in a compact form
     * @exception IllegalArgumentException the matrices are not consistent
     *            with each other
     */
    public LUDecomp(double[][] A, double[][] LU)
	throws IllegalArgumentException
    {
	checkMatrix(A, LU.length, LU[0].length, false);
	if (A != LU) {
	    for (int i = 0; i < LU.length; i++) {
		System.arraycopy(A[i], 0, LU[i], 0,
				 LU[i].length);
	    }
	}
	initLUP(/*matrixA,*/ LU);
    }

    private void initLUP(/*double[][] matrixA, */double[][] matrixLU)
    {
	this.matrixLU = matrixLU;
	this.m = matrixLU.length;
	this.n = matrixLU[0].length;
	// System.out.println("matrixLU.length = " + matrixLU.length);
	// System.out.println("matrixLU[0].length = " + matrixLU[0].length);

	permutation = new Permutation(m);
	int[] pivot = new int[m];
	for (int i = 0; i < m; i++) pivot[i] = i;

	double[] luRowI;
	double[] luColJ = new double[m];

	for (int j = 0; j < n; j++) {
	    for (int i = 0; i < m; i++) {
		luColJ[i] = matrixLU[i][j];
	    }
	    for (int i = 0; i < m; i++) {
		luRowI = matrixLU[i];
		int minij = Math.min(i,j);
		double sum = 0.0;
		for (int k = 0; k < minij; k++) {
		    sum += luRowI[k]*luColJ[k];
		}
		luColJ[i] -= sum;
		// set both because they refer to the same location
		luRowI[j] = luColJ[i];
	    }
	    // pivot operation
	    int piv = j;
	    for (int i = j+1; i < m; i++) {
		if (Math.abs(luColJ[i]) > Math.abs(luColJ[piv])) {
		    piv = i;
		}
	    }
	    if (piv != j) {
		for (int k = 0; k < n; k++) {
		    double tmp = matrixLU[piv][k];
		    matrixLU[piv][k] = matrixLU[j][k];
		    matrixLU[j][k] = tmp;
		}
		permutation.swap(piv,j);
		int k = pivot[piv]; pivot[piv] = pivot[j]; pivot[j] = k;
	    }
	    int[] pvector = permutation.getVector();
	    for (int k = 0; k < m; k++) {
		if (pvector[k] != pivot[k]) 
		    throw new RuntimeException(errorMsg("pivotPermErr"));
	    }
	    if (j < m && matrixLU[j][j] != 0.0) {
		for (int i = j+1; i < m; i++) {
		    matrixLU[i][j] /= matrixLU[j][j];
		}
	    }
	}
    }
}

//  LocalWords:  exbundle Jama MathWorks LUDecomp bzdev href jj
//  LocalWords:  needSquareMatrix nonsingular argArrayTooShort
//  LocalWords:  needNonsingular wrongVectorLen argSubArrayTooShort
