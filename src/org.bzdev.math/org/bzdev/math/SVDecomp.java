package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/** 
 * Singular Value Decomposition for an m by n matrix.
 * <P>
 * For an m-by-n matrix A with m &ge; n, the singular value decomposition is
 * an m-by-n orthogonal matrix U, an n-by-n diagonal matrix S, and
 * an n-by-n orthogonal matrix V so that A = U*S*V<sup>T</sup>.
 * For an m-by-n matrix A with m &lt; n, the singular value decomposition is
 * an m-by-m orthogonal matrix U, and m-by-m diagonal matrix S, and
 * an n-by-m orthogonal matrix V so that A = U*S*V<sup>T</sup>.
 * <P>
 * The singular values, sigma[k] = S[k][k], are ordered so that
 * sigma[0] &ge; sigma[1] &ge; ... &ge; sigma[n-1].
 * <P>
 * The singular value decomposition always exists, so the constructor will
 * never fail.  The matrix condition number and the effective numerical
 * rank can be computed from this decomposition.
 * <P>
 * The column vectors of U are those eigenvectors of AA<sup>T</sup>
 * corresponding to eigenvalues that are the squares of the values on
 * the diagonal of S.  Similarly the column vectors of V are the
 * eigenvectors of A<sup>T</sup>A corresponding to eigenvalues that
 * are the squares of the values on the diagonal of S. That is, the
 * eigenvalue for the eigenvector contained in the j<sup>th</sup>
 * column of U, and the eigenvalue for the eigenvector contained in
 * the j<sup>th</sup> column of V are both the square of S<sub>jj<sub>.
 * <P>
 * The implementation is based on the one used in the Jama package (a
 * public-domain package provided via a collaboration between
 * MathWorks and the National Institute of Standards and Technology).
 * Jama defines a matrix class whereas QRDecomp treats matrices as
 * two-dimensional arrays, and was written so that the org.bzdev
 * library is self-contained. Some additional runtime tests were
 * added, and there are a few additional constructors and
 * methods. Since this is more or less a port of the Jama class, this
 * particular class is in the public domain. The Jama implementation
 * is reliable for m &ge; n, and may fail otherwise.  To handle the
 * case where m &lt; n, we construct the singular-value decomposition
 * for the matrix's transpose and then swap the value of U and V. This
 * works because, if A<sup>T</sup> = USV<sup>T</sup>, then
 * A = (USV<sup>T</sup>)<sup>T</sup> = VS<sup>T</sup>U<sup>T</sup>
 * = VSU<sup>T</sup> (S is diagonal and therefore S<sup>T<sup> = S).
 * <P>
 * Note: the definition of the singular value decomposition differs
 * from the one used in
 * <A href="https://en.wikipedia.org/wiki/Singular_value_decomposition">
 * the Wikipedia article</A>, where U is an m by m matrix and S is an
 * m by n diagonal matrix (i.e., all off-diagonal elements are zero).
 * The Wikipedia definition defines U so it contains all the eigenvectors
 * of AA<sup>T</sup>, not just the ones whose eigenvalues are non-negative.
 * The definition used in by this class, by contrast, includes only those
 * eigenvectors whose eigenvalues are non-negative.  The rationale for the
 * definition used by this class is that the matrices are smaller, which
 * reduces memory requirements.
 */

public class SVDecomp implements java.io.Serializable {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

/* ------------------------
   Class variables
 * ------------------------ */

   /** Arrays for internal storage of U and V.
   @serial internal storage of U.
   @serial internal storage of V.
   */
   private double[][] U, V;

   /** Array for internal storage of singular values.
   @serial internal storage of singular values.
   */
   private double[] s;

   /** Row and column dimensions.
   @serial row dimension.
   @serial column dimension.
   */
    private int m, n;

    /**
     * Get the number of rows for the matrix that was decomposed.
     * @return the number of rows.
     */
    public int getNumberOfRows() {return transpose? n: m;}
    /**
     * Get the number of columns for the matrix that was decomposed.
     * @return the number of columns.
     */
    public int getNumberOfColumns() {return transpose? m: n;}

    
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
   Constructor
 * ------------------------ */

    /**
     * Constructor.
     * @param A an m by n matrix with m &ge; n
     */
    public SVDecomp (double[][] A) {
	// double[][] A = Arg.getArrayCopy();
	// m = Arg.getRowDimension();
	// n = Arg.getColumnDimension();
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
    public SVDecomp(double[][] A, int m, int n, boolean strict) {
	this(A, m, n, strict, true);
    }

    /**
     * Constructor given a flat representation of a matrix.
     * The elements of the matrix can be stored in one of two orders.
     * Given a matrix A<sub>ij</sub>, if the row index varies the fastest,
     * the flat representation will consist of the sequence
     * A<sub>00</sub>, A<sub>10</sub>, .... Otherwise the sequence is
     * A<sub>00</sub>, A<sub> 01</sub>, ....
     * <P>
     * The number of rows m must be at least as large as the number of
     * columns n.
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
    public SVDecomp(double[] flatMatrix, boolean columnMajorOrder, int m, int n)
	throws IllegalArgumentException
    {
	this(getMatrixA(flatMatrix, columnMajorOrder, m, n), m, n,
	     false, false);
    }
    
    boolean transpose = false;

    private SVDecomp(double[][] A, int m, int n, boolean strict, boolean copy) {
	
	checkMatrix(A, m, n, strict);
	// Derived from LINPACK code.
	// Initialize.

	if (n > m) {
	    this.m = n;
	    this.n = m;
	    transpose = true;
	    A = MatrixOps.transpose(A, m, n);
	    m = this.m;
	    n = this.n;
	} else {
	    this.m = m;
	    this.n = n;
	}

	if (copy && transpose == false) {
	    // make A a copy so the argument matrix does not change.
	    double[][] AA;
	    if (A.length == m) {
		AA = A.clone();
	    } else {
		AA = new double[m][];
		System.arraycopy(A, 0, AA, 0, m);
	    }
	    for (int i = 0; i < m; i++) {
		if (AA[i] == A[i]) {
		    if (A[i].length == n) {
			AA[i] = A[i].clone();
		    } else {
			AA[i] = new double[n];
			System.arraycopy(A, 0, AA, 0, n);
		    }
		}
	    }
	    A = AA;
	}

	/* Apparently the failing cases are only a proper subset of (m<n), 
	   so let's not throw error.  Correct fix to come later?
	   if (m<n) {
	     throw new IllegalArgumentException
	         ("Jama SVD only works for m >= n"); 
	   }
	*/
	int nu = Math.min(m,n);
	s = new double [Math.min(m+1,n)];
	U = new double [m][nu];
	V = new double [n][n];
	double[] e = new double [n];
	double[] work = new double [m];
	boolean wantu = true;
	boolean wantv = true;

	// Reduce A to bidiagonal form, storing the diagonal elements
	// in s and the super-diagonal elements in e.

	int nct = Math.min(m-1,n);
	int nrt = Math.max(0,Math.min(n-2,m));
	for (int k = 0; k < Math.max(nct,nrt); k++) {
	    if (k < nct) {

		// Compute the transformation for the k-th column and
		// place the k-th diagonal in s[k].
		// Compute 2-norm of k-th column without under/overflow.
		s[k] = 0;
		for (int i = k; i < m; i++) {
		    s[k] = hypot(s[k],A[i][k]);
		}
		if (s[k] != 0.0) {
		    if (A[k][k] < 0.0) {
			s[k] = -s[k];
		    }
		    for (int i = k; i < m; i++) {
			A[i][k] /= s[k];
		    }
		    A[k][k] += 1.0;
		}
		s[k] = -s[k];
	    }
	    for (int j = k+1; j < n; j++) {
		if ((k < nct) & (s[k] != 0.0))  {

		    // Apply the transformation.

		    double t = 0;
		    for (int i = k; i < m; i++) {
			t += A[i][k]*A[i][j];
		    }
		    t = -t/A[k][k];
		    for (int i = k; i < m; i++) {
			A[i][j] += t*A[i][k];
		    }
		}

		// Place the k-th row of A into e for the
		// subsequent calculation of the row transformation.

		e[j] = A[k][j];
	    }
	    if (wantu & (k < nct)) {

		// Place the transformation in U for subsequent back
		// multiplication.

		for (int i = k; i < m; i++) {
		    U[i][k] = A[i][k];
		}
	    }
	    if (k < nrt) {

		// Compute the k-th row transformation and place the
		// k-th super-diagonal in e[k].
		// Compute 2-norm without under/overflow.
		e[k] = 0;
		for (int i = k+1; i < n; i++) {
		    e[k] = hypot(e[k],e[i]);
		}
		if (e[k] != 0.0) {
		    if (e[k+1] < 0.0) {
			e[k] = -e[k];
		    }
		    for (int i = k+1; i < n; i++) {
			e[i] /= e[k];
		    }
		    e[k+1] += 1.0;
		}
		e[k] = -e[k];
		if ((k+1 < m) & (e[k] != 0.0)) {

		    // Apply the transformation.

		    for (int i = k+1; i < m; i++) {
			work[i] = 0.0;
		    }
		    for (int j = k+1; j < n; j++) {
			for (int i = k+1; i < m; i++) {
			    work[i] += e[j]*A[i][j];
			}
		    }
		    for (int j = k+1; j < n; j++) {
			double t = -e[j]/e[k+1];
			for (int i = k+1; i < m; i++) {
			    A[i][j] += t*work[i];
			}
		    }
		}
		if (wantv) {

		    // Place the transformation in V for subsequent
		    // back multiplication.

		    for (int i = k+1; i < n; i++) {
			V[i][k] = e[i];
		    }
		}
	    }
	}

	// Set up the final bidiagonal matrix or order p.

	int p = Math.min(n,m+1);
	if (nct < n) {
	    s[nct] = A[nct][nct];
	}
	if (m < p) {
	    s[p-1] = 0.0;
	}
	if (nrt+1 < p) {
	    e[nrt] = A[nrt][p-1];
	}
	e[p-1] = 0.0;

	// If required, generate U.

	if (wantu) {
	    for (int j = nct; j < nu; j++) {
		for (int i = 0; i < m; i++) {
		    U[i][j] = 0.0;
		}
		U[j][j] = 1.0;
	    }
	    for (int k = nct-1; k >= 0; k--) {
		if (s[k] != 0.0) {
		    for (int j = k+1; j < nu; j++) {
			double t = 0;
			for (int i = k; i < m; i++) {
			    t += U[i][k]*U[i][j];
			}
			t = -t/U[k][k];
			for (int i = k; i < m; i++) {
			    U[i][j] += t*U[i][k];
			}
		    }
		    for (int i = k; i < m; i++ ) {
			U[i][k] = -U[i][k];
		    }
		    U[k][k] = 1.0 + U[k][k];
		    for (int i = 0; i < k-1; i++) {
			U[i][k] = 0.0;
		    }
		} else {
		    for (int i = 0; i < m; i++) {
			U[i][k] = 0.0;
		    }
		    U[k][k] = 1.0;
		}
	    }
	}

	// If required, generate V.

	if (wantv) {
	    for (int k = n-1; k >= 0; k--) {
		if ((k < nrt) & (e[k] != 0.0)) {
		    for (int j = k+1; j < nu; j++) {
			double t = 0;
			for (int i = k+1; i < n; i++) {
			    t += V[i][k]*V[i][j];
			}
			t = -t/V[k+1][k];
			for (int i = k+1; i < n; i++) {
			    V[i][j] += t*V[i][k];
			}
		    }
		}
		for (int i = 0; i < n; i++) {
		    V[i][k] = 0.0;
		}
		V[k][k] = 1.0;
	    }
	}

	// Main iteration loop for the singular values.

	int pp = p-1;
	int iter = 0;
	double eps = Math.pow(2.0,-52.0);
	double tiny = Math.pow(2.0,-966.0);
	while (p > 0) {
	    int k,kase;

	    // Here is where a test for too many iterations would go.

	    // This section of the program inspects for
	    // negligible elements in the s and e arrays.  On
	    // completion the variables kase and k are set as follows.

	    // kase = 1     if s(p) and e[k-1] are negligible and k<p
	    // kase = 2     if s(k) is negligible and k<p
	    // kase = 3     if e[k-1] is negligible, k<p, and
	    //              s(k), ..., s(p) are not negligible (qr step).
	    // kase = 4     if e(p-1) is negligible (convergence).

	    for (k = p-2; k >= -1; k--) {
		if (k == -1) {
		    break;
		}
		if (Math.abs(e[k]) <=
		    tiny + eps*(Math.abs(s[k]) + Math.abs(s[k+1]))) {
		    e[k] = 0.0;
		    break;
		}
	    }
	    if (k == p-2) {
		kase = 4;
	    } else {
		int ks;
		for (ks = p-1; ks >= k; ks--) {
		    if (ks == k) {
			break;
		    }
		    double t = (ks != p ? Math.abs(e[ks]) : 0.) + 
			(ks != k+1 ? Math.abs(e[ks-1]) : 0.);
		    if (Math.abs(s[ks]) <= tiny + eps*t)  {
			s[ks] = 0.0;
			break;
		    }
		}
		if (ks == k) {
		    kase = 3;
		} else if (ks == p-1) {
		    kase = 1;
		} else {
		    kase = 2;
		    k = ks;
		}
	    }
	    k++;

	    // Perform the task indicated by kase.

	    switch (kase) {
		// Deflate negligible s(p).
            case 1: {
		double f = e[p-2];
		e[p-2] = 0.0;
		for (int j = p-2; j >= k; j--) {
		    double t = hypot(s[j],f);
		    double cs = s[j]/t;
		    double sn = f/t;
		    s[j] = t;
		    if (j != k) {
			f = -sn*e[j-1];
			e[j-1] = cs*e[j-1];
		    }
		    if (wantv) {
			for (int i = 0; i < n; i++) {
			    t = cs*V[i][j] + sn*V[i][p-1];
			    V[i][p-1] = -sn*V[i][j] + cs*V[i][p-1];
			    V[i][j] = t;
			}
		    }
		}
            }
		break;
		// Split at negligible s(k).
            case 2: {
		double f = e[k-1];
		e[k-1] = 0.0;
		for (int j = k; j < p; j++) {
		    double t = hypot(s[j],f);
		    double cs = s[j]/t;
		    double sn = f/t;
		    s[j] = t;
		    f = -sn*e[j];
		    e[j] = cs*e[j];
		    if (wantu) {
			for (int i = 0; i < m; i++) {
			    t = cs*U[i][j] + sn*U[i][k-1];
			    U[i][k-1] = -sn*U[i][j] + cs*U[i][k-1];
			    U[i][j] = t;
			}
		    }
		}
            }
		break;
		// Perform one qr step.
            case 3: {

		// Calculate the shift.
   
		double scale =
		    Math.max
		    (Math.max
		     (Math.max
		      (Math.max
		       (Math.abs(s[p-1]), Math.abs(s[p-2])),
		       Math.abs(e[p-2])), Math.abs(s[k])),Math.abs(e[k]));
		double sp = s[p-1]/scale;
		double spm1 = s[p-2]/scale;
		double epm1 = e[p-2]/scale;
		double sk = s[k]/scale;
		double ek = e[k]/scale;
		double b = ((spm1 + sp)*(spm1 - sp) + epm1*epm1)/2.0;
		double c = (sp*epm1)*(sp*epm1);
		double shift = 0.0;
		if ((b != 0.0) | (c != 0.0)) {
		    shift = Math.sqrt(b*b + c);
		    if (b < 0.0) {
			shift = -shift;
		    }
		    shift = c/(b + shift);
		}
		double f = (sk + sp)*(sk - sp) + shift;
		double g = sk*ek;
   
		// Chase zeros.
   
		for (int j = k; j < p-1; j++) {
		    double t = hypot(f,g);
		    double cs = f/t;
		    double sn = g/t;
		    if (j != k) {
			e[j-1] = t;
		    }
		    f = cs*s[j] + sn*e[j];
		    e[j] = cs*e[j] - sn*s[j];
		    g = sn*s[j+1];
		    s[j+1] = cs*s[j+1];
		    if (wantv) {
			for (int i = 0; i < n; i++) {
			    t = cs*V[i][j] + sn*V[i][j+1];
			    V[i][j+1] = -sn*V[i][j] + cs*V[i][j+1];
			    V[i][j] = t;
			}
		    }
		    t = hypot(f,g);
		    cs = f/t;
		    sn = g/t;
		    s[j] = t;
		    f = cs*e[j] + sn*s[j+1];
		    s[j+1] = -sn*e[j] + cs*s[j+1];
		    g = sn*e[j+1];
		    e[j+1] = cs*e[j+1];
		    if (wantu && (j < m-1)) {
			for (int i = 0; i < m; i++) {
			    t = cs*U[i][j] + sn*U[i][j+1];
			    U[i][j+1] = -sn*U[i][j] + cs*U[i][j+1];
			    U[i][j] = t;
			}
		    }
		}
		e[p-2] = f;
		iter = iter + 1;
            }
		break;
		// Convergence.
            case 4: {
		// Make the singular values positive.
		if (s[k] <= 0.0) {
		    s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
		    if (wantv) {
			for (int i = 0; i <= pp; i++) {
			    V[i][k] = -V[i][k];
			}
		    }
		}
   
		// Order the singular values.
   
		while (k < pp) {
		    if (s[k] >= s[k+1]) {
			break;
		    }
		    double t = s[k];
		    s[k] = s[k+1];
		    s[k+1] = t;
		    if (wantv && (k < n-1)) {
			for (int i = 0; i < n; i++) {
			    t = V[i][k+1]; V[i][k+1] = V[i][k]; V[i][k] = t;
			}
		    }
		    if (wantu && (k < m-1)) {
			for (int i = 0; i < m; i++) {
			    t = U[i][k+1]; U[i][k+1] = U[i][k]; U[i][k] = t;
			}
		    }
		    k++;
		}
		iter = 0;
		p--;
            }
		break;
	    }
	}
    }

/* ------------------------
   Public Methods
 * ------------------------ */

   /**
    * Get the left singular vectors for this singular value decomposition.
    * For an m by n matrix A withm &ge; n whose singular value
    * decomponsition is USV, the matrix U is a square n by n matrix
    * whose columns are called the left-singular vectors.
    * @return the matrix U
   */

   public double[][] getU () {
       // return new Matrix(U,m,Math.min(m+1,n));
       if (transpose) {
	   double[][] results = new double[n][n];
	   for (int i = 0; i < n ; i++) {
	       System.arraycopy(V[i], 0, results[i], 0, n);
	   }
	   return results;
       } else {
	   int nn = Math.min(m+1,n);
	   double[][] results = new double[m][nn];
	   for (int i = 0; i < m; i++) {
	       System.arraycopy(U[i], 0, results[i], 0, nn);
	   }
	   return results;
       }
   }

    /**
     * Get the right singular vectors for this singular value decomponsition.
     * For an m by n matrix A with &ge; n whose singular value
     * decomponsition is USV the matrix V is an m by n matrix whose
     * columns are called the right-singular vectors.
     * @return the matrix V
   */

   public double[][] getV () {
       // return new Matrix(V,n,n);
       if (transpose) {
	   int nn = Math.min(m+1,n);
	   double[][] results = new double[m][nn];
	   for (int i = 0; i < m; i++) {
	       System.arraycopy(U[i], 0, results[i], 0, nn);
	   }
	   return results;
       } else {
	   double[][] results = new double[n][n];
	   for (int i = 0; i < n ; i++) {
	       System.arraycopy(V[i], 0, results[i], 0, n);
	   }
	   return results;
       }
   }

    /** Get the singular values provided by this singular value
     *  decomposition.
     * For an m by n matrix A with &ge; n whose singular value
     * decomponsition is USV the matrix S is an m by n diagonal matrix
     * with non-negative real numbers on the diagonal ordered so that
     * the largest values occur first.
     * @return a vector of length n containing the diagonal elements of S
   */

   public double[] getSingularValues () {
      return s;
   }

    /** Get the diagonal matrix for this singular value decomposition.
     * For an m by n matrix A with &ge; n whose singular value
     * decomponsition is USV the matrix S is an m by n diagonal matrix
     * with non-negative real numbers on the diagonal ordered so that the
     * largest values appear first.
     * @return the matrix S
     */
   public double[][] getS () {
       // Matrix X = new Matrix(n,n);
       // double[][] S = X.getArray();
       double[][] S = new double[n][n];
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            S[i][j] = 0.0;
         }
         S[i][i] = this.s[i];
      }
      return S;
   }

   /** 
    * Get the 2-norm of the matrix A = USV, where U, S, and V are the matrices
    * comprising this singular value decomposition.
    * For a vector, the 2-norm is simply the square root of the sum of the
    * squares of the vector's components. For a matrix A the 2-norm is
    * sup<sub>x&ne;0</sub>(&Vert;Ax&Vert;<sub>2</sub>/&Vert;x&Vert;<sub>2</sub>).
    * It is numerically equal to the largest singular value.
    * @return the 2-norm of the matrix whose singular value decomposition is
    *         this singular value decomposition
   */
   public double norm2 () {
      return s[0];
   }

   /** 
    * Get the 2-norm condition number.
    * The value is the largest singular value divided by the smallest
    * @return     max(S)/min(S)
   */

   public double cond () {
      return s[0]/s[Math.min(m,n)-1];
   }

   /**
    * Get the effective numerical rank for this singular value decomposition.
    * The effective numerical rank is the number of singular values that
    * are above a limit set by floating-point accuracy. The limit is
    * numerically equal to the first singular value multiple by
    *  2<sup>-52</sup>max(m,n) for a singular value decomposition of an
    * m by n matrix.
    * @return the effective numerical rank
   */
   public int rank () {
      double eps = Math.pow(2.0,-52.0);
      double tol = Math.max(m,n)*s[0]*eps;
      int r = 0;
      for (int i = 0; i < s.length; i++) {
         if (s[i] > tol) {
            r++;
         }
      }
      return r;
   }

  private static final long serialVersionUID = 1;
}

//  LocalWords:  exbundle ge lt th jj Jama MathWorks QRDecomp bzdev
//  LocalWords:  runtime USV VSU href
