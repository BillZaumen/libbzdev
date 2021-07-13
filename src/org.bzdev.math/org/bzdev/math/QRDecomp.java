package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/** QR Decomposition of an m by n matrix  where m &ge; n.
 * For an m-by-n matrix A with m &ge; n, the QR decomposition provided
 * is an m-by-n orthogonal matrix Q and an n-by-n upper triangular matrix R
 * so that A = QR.  This is sometimes called a thin QR factorization or
 * a reduced QR factorization, or an economic QR decomposition
 * (Please see
 *  <A href="https://en.wikipedia.org/wiki/QR_decomposition#Advantages_and_disadvantages_2">QR decomposition</A>
 * for a description of this terminology).
 * <P>
 * The QR decomposition always exists, even if the matrix does not have
 * full rank, so the constructor will never fail.  The primary use of the
 * QR decomposition is in the least squares solution of non-square systems
 * of simultaneous linear equations.  This will fail if isFullRank()
 * returns false.
 * <P>
 * The implementation is based on the one used in the Jama package (a
 * public-domain package provided via a collaboration between
 * MathWorks and the National Institute of Standards and Technology).
 * Jama defines a matrix class whereas QRDecomp treats matrices as
 * two-dimensional arrays, and was written so that the org.bzdev
 * library is self-contained. Some additional runtime tests were
 * added, and there are a few additional constructors and methods.
 * The documentation was also extended. Since this is more or less a
 * port of the Jama class, this particular class is in the public
 * domain.  The Jama implementation (and consequently this one) uses
 * Householder vectors to compute the decomposition).
*/

public class QRDecomp implements java.io.Serializable {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /* ------------------------
       Class variables
       * ------------------------ */

    /** Array for internal storage of decomposition.
	@serial internal array storage.
    */
    private double[][] QR;

    /** Row and column dimensions.
	@serial column dimension.
	@serial row dimension.
    */
    private int m, n;

    /**
     * Get the number of rows for the matrix that was decomposed.
     * @return the number of rows.
     */
    public int getNumberOfRows() {return m;}

    /**
     * Get the number of columns for the matrix that was decomposed.
     * @return the number of columns.
     */
    public int getNumberOfColumns() {return n;}

    /** Array for internal storage of diagonal of R.
	@serial diagonal of R.
    */
    private double[] Rdiag;

    private static double[][] checkMatrix(double[][] matrixA) {
	// so we don't throw an exception  with a misleading message
	if (matrixA == null) {
	    throw new IllegalArgumentException(errorMsg("nullMatrix"));
	}
	if (matrixA.length == 0) {
	    throw new IllegalArgumentException(errorMsg("noRows"));
	}
	return matrixA;
    }

    private static void checkMatrix(double[][] matrixA, int m, int n,
				    boolean strict)
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
	if (m < n) {
	    throw new IllegalArgumentException(errorMsg("wrongDimQR", m, n));
	}

	if (matrixA.length < m) {
	    throw new
		IllegalArgumentException(errorMsg("missingRows"));
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

    private static int rowWidthCheck(double[][] matrixA) {
	int n = matrixA[0].length;
	for (int i = 1; i < matrixA.length; i++) {
	    int len = matrixA[i].length;
	    if (len != n) return -1;
	}
	return n;
    }

    private static double[][] getMatrixA(double[] flatMatrix,
					 boolean columnMajorOrder,
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

    /* ------------------------
       Constructors
       * ------------------------ */

    /**
     * Constructor.
     * @param A an m by n matrix with m &ge; n
     */
    public QRDecomp(double[][] A) {
	// uses Java rule that arguments are evaluated left to right
	this(checkMatrix(A), A.length, A[0].length, true);
    }

    /** Constructor given explicit dimensions.  
     *  @param A the m by n matrix to decompose, with m &ge; n
     *  @param m the number of rows
     * @param strict true if A must have exactly the number of
     *        rows and columns required; false if A may have rows
     *        or columns longer than needed, in which case the additional
     *        entries will be ignored
     */

    public QRDecomp (/*Matrix A*/double[][] A, int m, int n,
		     boolean strict) {
	// Initialize.
	
	checkMatrix(A, m, n, strict);
	if (m == A.length && n == rowWidthCheck(A)) {
	    QR = (double[][])A.clone();
	    for (int i = 0; i < m; i++) {
		if (A[i] == null || A[i].length != n) {
		    throw new IllegalArgumentException(errorMsg("notMatrix"));
		}
		if (QR[i] == null || QR[i] == A[i]) {
		    QR[i] = (double[]) A[i].clone();
		}
	    }
	} else {
	    QR = new double[m][n];
	    for (int i = 0; i < m; i++) {
		System.arraycopy(A[i], 0, QR[i], 0, n);
	    }
	}
	
	this.m = m;
	this.n = n;
	Rdiag = new double[n];
	init();
    }

    static double hypot(double a, double b) {
	double r;
	if (Math.abs(a) > Math.abs(b)) {
	    r = b/a;
	    r = Math.abs(a)*Math.sqrt(1+r*r);
	} else if (b != 0) {
	    r = a/b;
	    r = Math.abs(b)*Math.sqrt(1+r*r);
	} else {
	    r = 0.0;
	}
	return r;
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
    public QRDecomp(double[] flatMatrix, boolean columnMajorOrder, int m, int n)
	throws IllegalArgumentException
    {
	this(getMatrixA(flatMatrix, columnMajorOrder, m, n));
    }

    private void init() {
	// Main loop.
	for (int k = 0; k < n; k++) {
	    // Compute 2-norm of k-th column without under/overflow.
	    double nrm = 0;
	    for (int i = k; i < m; i++) {
		nrm = hypot(nrm,QR[i][k]);
	    }

	    if (nrm != 0.0) {
		// Form k-th Householder vector.
		if (QR[k][k] < 0) {
		    nrm = -nrm;
		}
		for (int i = k; i < m; i++) {
		    QR[i][k] /= nrm;
		}
		QR[k][k] += 1.0;

		// Apply transformation to remaining columns.
		for (int j = k+1; j < n; j++) {
		    double s = 0.0; 
		    for (int i = k; i < m; i++) {
			s += QR[i][k]*QR[i][j];
		    }
		    s = -s/QR[k][k];
		    for (int i = k; i < m; i++) {
			QR[i][j] += s*QR[i][k];
		    }
		}
	    }
	    Rdiag[k] = -nrm;
	}
    }

    /* ------------------------
       Public Methods
       * ------------------------ */

    /** Determine if the matrix passed to the constructor is a full-rank matrix.
     * For an m by n matrix with m &gt; n, a full-rank matrix will have a rank
     * of n.
     * @return true if it is a full-rank matrix; false otherwise
     */

    public boolean isFullRank () {
	for (int j = 0; j < n; j++) {
	    if (Rdiag[j] == 0)
		return false;
	}
	return true;
    }

    /** Return the Householder vectors
     * @return  a lower trapezoidal matrix whose columns define the
     *          reflections represented by Householder vectors
     */

    public double[][] getH () {
	// Matrix X = new Matrix(m,n);
	// double[][] H = X.getArray();
	double[][] H = new double[m][n];
	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < n; j++) {
		if (i >= j) {
		    H[i][j] = QR[i][j];
		} else {
		    H[i][j] = 0.0;
		}
	    }
	}
	return H;
    }

    /** Get the upper triangular factor R such that A = QR, where A is
     * the matrix passed to the constructor.
     * The value is in a form sometimes called
     * an "economic" decomposition as the matrix R is an n by n matrix
     * when m &ne; n and where A is an m by n matrix.
     @return a matrix equal to R
    */

    public double[][] getR () {
	// Matrix X = new Matrix(n,n);
	// double[][] R = X.getArray();
	double[][] R = new double[n][n];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		if (i < j) {
		    R[i][j] = QR[i][j];
		} else if (i == j) {
		    R[i][j] = Rdiag[i];
		} else {
		    R[i][j] = 0.0;
		}
	    }
	}
	// return X;
	return R;
    }

    /** Get the orthogonal matrix Q such that A = QR, where A is the matrix
     * passed to the constructor. The value is in a form sometimes called
     * an "economic" decomposition as the matrix is not an m by m matrix
     * when m &ne; n and where A is an m by n matrix.
     * @return a matrix equal to Q
     */
    public double[][] getQ () {
	// Matrix X = new Matrix(m,n);
	//double[][] Q = X.getArray();
	double[][] Q = new double[m][n];
	for (int k = n-1; k >= 0; k--) {
	    for (int i = 0; i < m; i++) {
		Q[i][k] = 0.0;
	    }
	    Q[k][k] = 1.0;
	    for (int j = k; j < n; j++) {
		if (QR[k][k] != 0) {
		    double s = 0.0;
		    for (int i = k; i < m; i++) {
			s += QR[i][k]*Q[i][j];
		    }
		    s = -s/QR[k][k];
		    for (int i = k; i < m; i++) {
			Q[i][j] += s*QR[i][k];
		    }
		}
	    }
	}
	return Q;
    }

    /** Least squares solution of AX = B, where A is the matrix passed
     * to the constructor.
     * The solution is a matrix whose column vectors contain the solution
     * for the corresponding column vector of B.
     * @param B  a matrix with as many rows as A and any number of columns.
     * @return a matrix, with as many rows as A has columns and with as
     *         many columns as B,  that minimizes the norms of each
     *         column vectors of A*X-B
     * @exception IllegalArgumentException  matrix row dimensions must agree
     * @exception IllegalStateException  The matrix A is rank deficient
     */
    public /*Matrix*/double[][] solve (/*Matrix B*/double[][] B) {
	if (B.length != m) {
	    // if (B.getRowDimension() != m) {
	    throw new IllegalArgumentException
		(errorMsg("wrongRowDim", B.length, m));
	}
	if (!this.isFullRank()) {
	    throw new IllegalStateException(errorMsg("rankDeficient"));
	}
      
	// Copy right hand side
	// int n = B.getColumnDimension();
	int nx = B[0].length;
	// double[][] X = B.getArrayCopy();
	double[][] X = B.clone();
	for (int i = 0; i < m; i++) {
	    if (X[i] == null) {
		X[i] = B[i].clone();
	    }
	}

	// Compute Y = transpose(Q)*B
	for (int k = 0; k < n; k++) {
	    for (int j = 0; j < nx; j++) {
		double s = 0.0; 
		for (int i = k; i < m; i++) {
		    s += QR[i][k]*X[i][j];
		}
		s = -s/QR[k][k];
		for (int i = k; i < m; i++) {
		    X[i][j] += s*QR[i][k];
		}
	    }
	}
	// Solve R*X = Y;
	for (int k = n-1; k >= 0; k--) {
	    for (int j = 0; j < nx; j++) {
		X[k][j] /= Rdiag[k];
	    }
	    for (int i = 0; i < k; i++) {
		for (int j = 0; j < nx; j++) {
		    X[i][j] -= X[k][j]*QR[i][k];
		}
	    }
	}
	// return (new Matrix(X,n,nx).getMatrix(0,n-1,0,nx-1));
	double[][] result = new double[n][nx];
	for (int i = 0; i < n; i++) {
	    System.arraycopy(X[i], 0, result[i], 0, nx);
	}
	return result;
    }

    /**
     *  Least squares solution of Ax = b, where A is the matrix passed
     * to the constructor and x and b are column vectors.
     * @param b a vector whose dimension is equal to the number of rows
     *        of A and any with any number of columns.
     *  @return a vector, whose dimension is equal the number of columns of A,
     *          that minimizes the norm of  A*x-b 
     * @exception IllegalArgumentException  matrix row dimensions must agree
     * @exception IllegalStateException  The matrix A is rank deficient
     */
    public /*Matrix*/double[] solve (/*Matrix B*/double[] b) {
	if (b.length != m) {
	    // if (B.getRowDimension() != m) {
	    throw new IllegalArgumentException
		(errorMsg("wrongRowDim", b.length, m));
	}
	if (!this.isFullRank()) {
	    throw new IllegalStateException(errorMsg("rankDeficient"));
	}
      
	// Copy right hand side
	// int nx = B.getColumnDimension();
	// int nx = B[0].length;
	// double[][] X = B.getArrayCopy();
	double[] X = b.clone();

	// Compute Y = transpose(Q)*B
	for (int k = 0; k < n; k++) {
	    double s = 0.0; 
	    for (int i = k; i < m; i++) {
		s += QR[i][k]*X[i];
	    }
	    s = -s/QR[k][k];
	    for (int i = k; i < m; i++) {
		X[i] += s*QR[i][k];
	    }
	}
	// Solve R*X = Y;
	for (int k = n-1; k >= 0; k--) {
	    X[k] /= Rdiag[k];
	    for (int i = 0; i < k; i++) {
		X[i] -= X[k]*QR[i][k];
	    }
	}
	double[] result = new double[n];
	System.arraycopy(X, 0, result, 0, n);
	// return (new Matrix(X,n,nx).getMatrix(0,n-1,0,nx-1));
	return result;
    }

    private static final long serialVersionUID = 1;
}

//  LocalWords:  exbundle ge href isFullRank Jama MathWorks QRDecomp
//  LocalWords:  bzdev runtime nullMatrix noRows intArgNotPositive ij
//  LocalWords:  wrongDimQR missingRows wrongRowSize param notMatrix
//  LocalWords:  flatMatrixTooShort flatMatrix columnMajorOrder th ne
//  LocalWords:  Fortran IllegalArgumentException getArray nx
//  LocalWords:  IllegalStateException getRowDimension wrongRowDim
//  LocalWords:  rankDeficient getColumnDimension getArrayCopy
//  LocalWords:  getMatrix
