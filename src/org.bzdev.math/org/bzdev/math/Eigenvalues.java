package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Eigenvalues and eigenvectors of a real n by n matrix A.
 * <P>
 * If A is symmetric, then A = V*D*V<sup>T</sup> where the eigenvalue
 * matrix D is diagonal and the eigenvector matrix V is an orthogonal
 * matrix whose columns are the eigenvectors of A.  Since V is
 * orthogonal, by definition all the eigenvectors are normalized.  The
 * eigenvalues are sorted in descending order and the eigenvector
 * corresponding to the i<sup>th</sup> eigenvalue is stored in the
 * i<sup>th</sup> column of V.
 * <P>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal
 * with the real eigenvalues in 1-by-1 blocks and any complex eigenvalues,
 *  &lambda;&plusmn;i&mu; (where &mu; is positive), in 2-by-2 blocks:
 * <BLOCKQUOTE><PRE>
 *     |  &lambda; &mu; |
 *     | -&mu; &lambda; |
 * </PRE></BLOCKQUOTE>
 * For the 1-by-1 blocks, the corresponding column in V is a real-valued
 * eigenvector.  For the 2-by-2 blocks, the corresponding columns in V
 * represent complex values.  For the eigenvalue &lambda;+&mu;, the first
 * column contains the real components of the eigenvector and the second
 * column contains the imaginary components of the eigenvector.  For the
 * eigenvalue &lambda;-&mu;, the second column contains the real components
 * of the eigenvector and the first column contains the imaginary components
 * of the eigenvector: the matrix product
 * <BLOCKQUOTE><PRE>
 *             |  &lambda; &mu; |
 *     | a b | |                |
 *             | -&mu; &lambda; |
 * </PRE></BLOCKQUOTE>
 * has the value
 * <BLOCKQUOTE><PRE>
 *     | (a&lambda;-b&mu;) (a&mu;+b&lambda;) |
 * </PRE></BLOCKQUOTE>
 * but 
 * <BLOCKQUOTE><PRE>
 *     (a+bi)(&lambda;+&mu;i) = (a&lambda;-b&mu;)+(a&mu;+b&lambda;)i
 * </PRE></BLOCKQUOTE>
 * and
 * <BLOCKQUOTE><PRE>
 *    (b+ai)(&lambda;-&mu;i) = (a&mu;+b&lambda;)+(a&lambda;-b&mu;)i
 * </PRE></BLOCKQUOTE>
 * which justifies the assignment of real and imaginary components to
 * complex eigenvectors as described above.  Conveniently, it is also
 * true that AV = VD for a mix of real and complex eigenvalues.
 * <P>
 * The matrix V may be badly conditioned, or even singular, so the
 * validity of the equation
 * A = V*D*inverse(V) depends upon the condition number for V.
 * The condition number for V can be computed by using the method
 * {@link SVDecomp#cond()}:
 * <BLOCKQUOTE><CODE><PRE>
 *    (new SVDecomp(V)).cond()
 * </PRE></CODE></BLOCKQUOTE>
 * For the nonsymmetric case, the eigenvectors are normalized but
 * are not orthogonal in general.
 * <P>
 * The implementation is based on the one used in the Jama package (a
 * public-domain package provided via a collaboration between
 * MathWorks and the National Institute of Standards and Technology).
 * Jama defines a matrix class whereas QRDecomp treats matrices as
 * two-dimensional arrays, and was written so that the org.bzdev
 * library is self-contained. Some additional runtime tests were
 * added, and there are a few additional constructors and
 * methods. The documentation was also extended. In addition, some
 * code was added to normalize the eigenvectors in the nonsymmetric
 * case.
 * Since this is more or less a port of the Jama class, this
 * particular class is in the public domain.

 */

public class Eigenvalues implements java.io.Serializable {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
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


    private static void checkMatrix(double[][] matrixA, int n,
				    boolean strict)
	throws IllegalArgumentException
    {
	if (n <= 0) {
	    throw new
		IllegalArgumentException(errorMsg("intArgNotPositive", n));
	}

	if (matrixA.length < n) {
	    throw new
		IllegalArgumentException(errorMsg("missingRows"));
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
					 int n)
	throws IllegalArgumentException
    {
	if (n <= 0) {
	    throw new
		IllegalArgumentException(errorMsg("intArgNotPositive", n));
	}
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


    /* ------------------------
       Class variables
       * ------------------------ */

    /** Row and column dimension (square matrix).
	@serial matrix dimension.
    */
    private int n;

    /**
     * Get the number of rows for the matrix that was decomposed.
     * @return the number of rows.
     */
    public int getNumberOfRows() {return n;}

    /**
     * Get the number of columns for the matrix that was decomposed.
     * @return the number of columns.
     */
    public int getNumberOfColumns() {return n;}

    /** Symmetry flag.
	@serial internal symmetry flag.
    */
    private boolean issymmetric;

    /** Arrays for internal storage of eigenvalues.
	@serial internal storage of eigenvalues.
    */
    private double[] d, e;

    /** Array for internal storage of eigenvectors.
	@serial internal storage of eigenvectors.
    */
    private double[][] V;

    /** Array for internal storage of nonsymmetric Hessenberg form.
	@serial internal storage of nonsymmetric Hessenberg form.
    */
    private double[][] H;

    /** Working storage for nonsymmetric algorithm.
	@serial working storage for nonsymmetric algorithm.
    */
    private double[] ort;

    /* ------------------------
       Private Methods
       * ------------------------ */

    // Symmetric Householder reduction to tridiagonal form.

    private void tred2 () {

	//  This is derived from the Algol procedures tred2 by
	//  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
	//  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
	//  Fortran subroutine in EISPACK.

	for (int j = 0; j < n; j++) {
	    d[j] = V[n-1][j];
	}

	// Householder reduction to tridiagonal form.
   
	for (int i = n-1; i > 0; i--) {
   
	    // Scale to avoid under/overflow.
   
	    double scale = 0.0;
	    double h = 0.0;
	    for (int k = 0; k < i; k++) {
		scale = scale + Math.abs(d[k]);
	    }
	    if (scale == 0.0) {
		e[i] = d[i-1];
		for (int j = 0; j < i; j++) {
		    d[j] = V[i-1][j];
		    V[i][j] = 0.0;
		    V[j][i] = 0.0;
		}
	    } else {
   
		// Generate Householder vector.
   
		for (int k = 0; k < i; k++) {
		    d[k] /= scale;
		    h += d[k] * d[k];
		}
		double f = d[i-1];
		double g = Math.sqrt(h);
		if (f > 0) {
		    g = -g;
		}
		e[i] = scale * g;
		h = h - f * g;
		d[i-1] = f - g;
		for (int j = 0; j < i; j++) {
		    e[j] = 0.0;
		}
   
		// Apply similarity transformation to remaining columns.
   
		for (int j = 0; j < i; j++) {
		    f = d[j];
		    V[j][i] = f;
		    g = e[j] + V[j][j] * f;
		    for (int k = j+1; k <= i-1; k++) {
			g += V[k][j] * d[k];
			e[k] += V[k][j] * f;
		    }
		    e[j] = g;
		}
		f = 0.0;
		for (int j = 0; j < i; j++) {
		    e[j] /= h;
		    f += e[j] * d[j];
		}
		double hh = f / (h + h);
		for (int j = 0; j < i; j++) {
		    e[j] -= hh * d[j];
		}
		for (int j = 0; j < i; j++) {
		    f = d[j];
		    g = e[j];
		    for (int k = j; k <= i-1; k++) {
			V[k][j] -= (f * e[k] + g * d[k]);
		    }
		    d[j] = V[i-1][j];
		    V[i][j] = 0.0;
		}
	    }
	    d[i] = h;
	}
   
	// Accumulate transformations.
   
	for (int i = 0; i < n-1; i++) {
	    V[n-1][i] = V[i][i];
	    V[i][i] = 1.0;
	    double h = d[i+1];
	    if (h != 0.0) {
		for (int k = 0; k <= i; k++) {
		    d[k] = V[k][i+1] / h;
		}
		for (int j = 0; j <= i; j++) {
		    double g = 0.0;
		    for (int k = 0; k <= i; k++) {
			g += V[k][i+1] * V[k][j];
		    }
		    for (int k = 0; k <= i; k++) {
			V[k][j] -= g * d[k];
		    }
		}
	    }
	    for (int k = 0; k <= i; k++) {
		V[k][i+1] = 0.0;
	    }
	}
	for (int j = 0; j < n; j++) {
	    d[j] = V[n-1][j];
	    V[n-1][j] = 0.0;
	}
	V[n-1][n-1] = 1.0;
	e[0] = 0.0;
    }

    // Symmetric tridiagonal QL algorithm.
   
    private void tql2 () {

	//  This is derived from the Algol procedures tql2, by
	//  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
	//  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
	//  Fortran subroutine in EISPACK.
   
	for (int i = 1; i < n; i++) {
	    e[i-1] = e[i];
	}
	e[n-1] = 0.0;
   
	double f = 0.0;
	double tst1 = 0.0;
	// double eps = Math.pow(2.0,-52.0);

	for (int l = 0; l < n; l++) {

	    // Find small subdiagonal element
   
	    tst1 = Math.max(tst1,Math.abs(d[l]) + Math.abs(e[l]));
	    int m = l;
	    while (m < n) {
		if (Math.abs(e[m]) <= EPS*tst1) {
		    break;
		}
		m++;
	    }
   
	    // If m == l, d[l] is an eigenvalue,
	    // otherwise, iterate.
   
	    if (m > l) {
		int iter = 0;
		do {
		    iter = iter + 1;  // (Could check iteration count here.)
   
		    // Compute implicit shift
   
		    double g = d[l];
		    double p = (d[l+1] - g) / (2.0 * e[l]);
		    double r = hypot(p,1.0);
		    if (p < 0) {
			r = -r;
		    }
		    d[l] = e[l] / (p + r);
		    d[l+1] = e[l] * (p + r);
		    double dl1 = d[l+1];
		    double h = g - d[l];
		    for (int i = l+2; i < n; i++) {
			d[i] -= h;
		    }
		    f = f + h;
   
		    // Implicit QL transformation.
   
		    p = d[m];
		    double c = 1.0;
		    double c2 = c;
		    double c3 = c;
		    double el1 = e[l+1];
		    double s = 0.0;
		    double s2 = 0.0;
		    for (int i = m-1; i >= l; i--) {
			c3 = c2;
			c2 = c;
			s2 = s;
			g = c * e[i];
			h = c * p;
			r = hypot(p,e[i]);
			e[i+1] = s * r;
			s = e[i] / r;
			c = p / r;
			p = c * d[i] - s * g;
			d[i+1] = h + s * (c * g + s * d[i]);
   
			// Accumulate transformation.
   
			for (int k = 0; k < n; k++) {
			    h = V[k][i+1];
			    V[k][i+1] = s * V[k][i] + c * h;
			    V[k][i] = c * V[k][i] - s * h;
			}
		    }
		    p = -s * s2 * c3 * el1 * e[l] / dl1;
		    e[l] = s * p;
		    d[l] = c * p;
   
		    // Check for convergence.
   
		} while (Math.abs(e[l]) > EPS*tst1);
	    }
	    d[l] = d[l] + f;
	    e[l] = 0.0;
	}
     
	// Sort eigenvalues and corresponding vectors.
   
	for (int i = 0; i < n-1; i++) {
	    int k = i;
	    double p = d[i];
	    for (int j = i+1; j < n; j++) {
		if (d[j] < p) {
		    k = j;
		    p = d[j];
		}
	    }
	    if (k != i) {
		d[k] = d[i];
		d[i] = p;
		for (int j = 0; j < n; j++) {
		    p = V[j][i];
		    V[j][i] = V[j][k];
		    V[j][k] = p;
		}
	    }
	}
    }

    // Nonsymmetric reduction to Hessenberg form.

    private void orthes () {
   
	//  This is derived from the Algol procedures orthes and ortran,
	//  by Martin and Wilkinson, Handbook for Auto. Comp.,
	//  Vol.ii-Linear Algebra, and the corresponding
	//  Fortran subroutines in EISPACK.
   
	int low = 0;
	int high = n-1;
   
	for (int m = low+1; m <= high-1; m++) {
   
	    // Scale column.
   
	    double scale = 0.0;
	    for (int i = m; i <= high; i++) {
		scale = scale + Math.abs(H[i][m-1]);
	    }
	    if (scale != 0.0) {
   
		// Compute Householder transformation.
   
		double h = 0.0;
		for (int i = high; i >= m; i--) {
		    ort[i] = H[i][m-1]/scale;
		    h += ort[i] * ort[i];
		}
		double g = Math.sqrt(h);
		if (ort[m] > 0) {
		    g = -g;
		}
		h = h - ort[m] * g;
		ort[m] = ort[m] - g;
   
		// Apply Householder similarity transformation
		// H = (I-u*u'/h)*H*(I-u*u')/h)
   
		for (int j = m; j < n; j++) {
		    double f = 0.0;
		    for (int i = high; i >= m; i--) {
			f += ort[i]*H[i][j];
		    }
		    f = f/h;
		    for (int i = m; i <= high; i++) {
			H[i][j] -= f*ort[i];
		    }
		}
   
		for (int i = 0; i <= high; i++) {
		    double f = 0.0;
		    for (int j = high; j >= m; j--) {
			f += ort[j]*H[i][j];
		    }
		    f = f/h;
		    for (int j = m; j <= high; j++) {
			H[i][j] -= f*ort[j];
		    }
		}
		ort[m] = scale*ort[m];
		H[m][m-1] = scale*g;
	    }
	}
   
	// Accumulate transformations (Algol's ortran).

	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		V[i][j] = (i == j ? 1.0 : 0.0);
	    }
	}

	for (int m = high-1; m >= low+1; m--) {
	    if (H[m][m-1] != 0.0) {
		for (int i = m+1; i <= high; i++) {
		    ort[i] = H[i][m-1];
		}
		for (int j = m; j <= high; j++) {
		    double g = 0.0;
		    for (int i = m; i <= high; i++) {
			g += ort[i] * V[i][j];
		    }
		    // Double division avoids possible underflow
		    g = (g / ort[m]) / H[m][m-1];
		    for (int i = m; i <= high; i++) {
			V[i][j] += g * ort[i];
		    }
		}
	    }
	}
    }

    // Complex scalar division.

    private transient double cdivr, cdivi;
    private void cdiv(double xr, double xi, double yr, double yi) {
	double r,d;
	if (Math.abs(yr) > Math.abs(yi)) {
	    r = yi/yr;
	    d = yr + r*yi;
	    cdivr = (xr + r*xi)/d;
	    cdivi = (xi - r*xr)/d;
	} else {
	    r = yr/yi;
	    d = yi + r*yr;
	    cdivr = (r*xr + xi)/d;
	    cdivi = (r*xi - xr)/d;
	}
    }

    // Nonsymmetric reduction from Hessenberg to real Schur form.

    private void hqr2 () {
   
	//  This is derived from the Algol procedure hqr2,
	//  by Martin and Wilkinson, Handbook for Auto. Comp.,
	//  Vol.ii-Linear Algebra, and the corresponding
	//  Fortran subroutine in EISPACK.
   
	// Initialize
   
	int nn = this.n;
	int n = nn-1;
	int low = 0;
	int high = nn-1;
	// double eps = Math.pow(2.0,-52.0);
	double exshift = 0.0;
	double p=0,q=0,r=0,s=0,z=0,t,w,x,y;
   
	// Store roots isolated by balanc and compute matrix norm
   
	double norm = 0.0;
	for (int i = 0; i < nn; i++) {
	    if (i < low | i > high) {
		d[i] = H[i][i];
		e[i] = 0.0;
	    }
	    for (int j = Math.max(i-1,0); j < nn; j++) {
		norm = norm + Math.abs(H[i][j]);
	    }
	}
   
	// Outer loop over eigenvalue index
   
	int iter = 0;
	while (n >= low) {
   
	    // Look for single small sub-diagonal element
   
	    int l = n;
	    while (l > low) {
		s = Math.abs(H[l-1][l-1]) + Math.abs(H[l][l]);
		if (s == 0.0) {
		    s = norm;
		}
		if (Math.abs(H[l][l-1]) < EPS * s) {
		    break;
		}
		l--;
	    }
       
	    // Check for convergence
	    // One root found
   
	    if (l == n) {
		H[n][n] = H[n][n] + exshift;
		d[n] = H[n][n];
		e[n] = 0.0;
		n--;
		iter = 0;
   
		// Two roots found
   
	    } else if (l == n-1) {
		w = H[n][n-1] * H[n-1][n];
		p = (H[n-1][n-1] - H[n][n]) / 2.0;
		q = p * p + w;
		z = Math.sqrt(Math.abs(q));
		H[n][n] = H[n][n] + exshift;
		H[n-1][n-1] = H[n-1][n-1] + exshift;
		x = H[n][n];
   
		// Real pair
   
		if (q >= 0) {
		    if (p >= 0) {
			z = p + z;
		    } else {
			z = p - z;
		    }
		    d[n-1] = x + z;
		    d[n] = d[n-1];
		    if (z != 0.0) {
			d[n] = x - w / z;
		    }
		    e[n-1] = 0.0;
		    e[n] = 0.0;
		    x = H[n][n-1];
		    s = Math.abs(x) + Math.abs(z);
		    p = x / s;
		    q = z / s;
		    r = Math.sqrt(p * p+q * q);
		    p = p / r;
		    q = q / r;
   
		    // Row modification
   
		    for (int j = n-1; j < nn; j++) {
			z = H[n-1][j];
			H[n-1][j] = q * z + p * H[n][j];
			H[n][j] = q * H[n][j] - p * z;
		    }
   
		    // Column modification
   
		    for (int i = 0; i <= n; i++) {
			z = H[i][n-1];
			H[i][n-1] = q * z + p * H[i][n];
			H[i][n] = q * H[i][n] - p * z;
		    }
   
		    // Accumulate transformations
   
		    for (int i = low; i <= high; i++) {
			z = V[i][n-1];
			V[i][n-1] = q * z + p * V[i][n];
			V[i][n] = q * V[i][n] - p * z;
		    }
   
		    // Complex pair
   
		} else {
		    d[n-1] = x + p;
		    d[n] = x + p;
		    e[n-1] = z;
		    e[n] = -z;
		}
		n = n - 2;
		iter = 0;
   
		// No convergence yet
   
	    } else {
   
		// Form shift
   
		x = H[n][n];
		y = 0.0;
		w = 0.0;
		if (l < n) {
		    y = H[n-1][n-1];
		    w = H[n][n-1] * H[n-1][n];
		}
   
		// Wilkinson's original ad hoc shift
   
		if (iter == 10) {
		    exshift += x;
		    for (int i = low; i <= n; i++) {
			H[i][i] -= x;
		    }
		    s = Math.abs(H[n][n-1]) + Math.abs(H[n-1][n-2]);
		    x = y = 0.75 * s;
		    w = -0.4375 * s * s;
		}

		// MATLAB's new ad hoc shift

		if (iter == 30) {
		    s = (y - x) / 2.0;
		    s = s * s + w;
		    if (s > 0) {
			s = Math.sqrt(s);
			if (y < x) {
			    s = -s;
			}
			s = x - w / ((y - x) / 2.0 + s);
			for (int i = low; i <= n; i++) {
			    H[i][i] -= s;
			}
			exshift += s;
			x = y = w = 0.964;
		    }
		}
   
		iter = iter + 1;   // (Could check iteration count here.)
   
		// Look for two consecutive small sub-diagonal elements
   
		int m = n-2;
		while (m >= l) {
		    z = H[m][m];
		    r = x - z;
		    s = y - z;
		    p = (r * s - w) / H[m+1][m] + H[m][m+1];
		    q = H[m+1][m+1] - z - r - s;
		    r = H[m+2][m+1];
		    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
		    p = p / s;
		    q = q / s;
		    r = r / s;
		    if (m == l) {
			break;
		    }
		    if (Math.abs(H[m][m-1]) * (Math.abs(q) + Math.abs(r)) <
			EPS * (Math.abs(p) * (Math.abs(H[m-1][m-1]) + Math.abs(z) +
					      Math.abs(H[m+1][m+1])))) {
			break;
		    }
		    m--;
		}
   
		for (int i = m+2; i <= n; i++) {
		    H[i][i-2] = 0.0;
		    if (i > m+2) {
			H[i][i-3] = 0.0;
		    }
		}
   
		// Double QR step involving rows l:n and columns m:n
   

		for (int k = m; k <= n-1; k++) {
		    boolean notlast = (k != n-1);
		    if (k != m) {
			p = H[k][k-1];
			q = H[k+1][k-1];
			r = (notlast ? H[k+2][k-1] : 0.0);
			x = Math.abs(p) + Math.abs(q) + Math.abs(r);
			if (x == 0.0) {
			    continue;
			}
			p = p / x;
			q = q / x;
			r = r / x;
		    }

		    s = Math.sqrt(p * p + q * q + r * r);
		    if (p < 0) {
			s = -s;
		    }
		    if (s != 0) {
			if (k != m) {
			    H[k][k-1] = -s * x;
			} else if (l != m) {
			    H[k][k-1] = -H[k][k-1];
			}
			p = p + s;
			x = p / s;
			y = q / s;
			z = r / s;
			q = q / p;
			r = r / p;
   
			// Row modification
   
			for (int j = k; j < nn; j++) {
			    p = H[k][j] + q * H[k+1][j];
			    if (notlast) {
				p = p + r * H[k+2][j];
				H[k+2][j] = H[k+2][j] - p * z;
			    }
			    H[k][j] = H[k][j] - p * x;
			    H[k+1][j] = H[k+1][j] - p * y;
			}
   
			// Column modification
   
			for (int i = 0; i <= Math.min(n,k+3); i++) {
			    p = x * H[i][k] + y * H[i][k+1];
			    if (notlast) {
				p = p + z * H[i][k+2];
				H[i][k+2] = H[i][k+2] - p * r;
			    }
			    H[i][k] = H[i][k] - p;
			    H[i][k+1] = H[i][k+1] - p * q;
			}
   
			// Accumulate transformations
   
			for (int i = low; i <= high; i++) {
			    p = x * V[i][k] + y * V[i][k+1];
			    if (notlast) {
				p = p + z * V[i][k+2];
				V[i][k+2] = V[i][k+2] - p * r;
			    }
			    V[i][k] = V[i][k] - p;
			    V[i][k+1] = V[i][k+1] - p * q;
			}
		    }  // (s != 0)
		}  // k loop
	    }  // check convergence
	}  // while (n >= low)
      
	// Backsubstitute to find vectors of upper triangular form

	if (norm == 0.0) {
	    return;
	}
   
	for (n = nn-1; n >= 0; n--) {
	    p = d[n];
	    q = e[n];
   
	    // Real vector
   
	    if (q == 0) {
		int l = n;
		H[n][n] = 1.0;
		for (int i = n-1; i >= 0; i--) {
		    w = H[i][i] - p;
		    r = 0.0;
		    for (int j = l; j <= n; j++) {
			r = r + H[i][j] * H[j][n];
		    }
		    if (e[i] < 0.0) {
			z = w;
			s = r;
		    } else {
			l = i;
			if (e[i] == 0.0) {
			    if (w != 0.0) {
				H[i][n] = -r / w;
			    } else {
				H[i][n] = -r / (EPS * norm);
			    }
   
			    // Solve real equations
   
			} else {
			    x = H[i][i+1];
			    y = H[i+1][i];
			    q = (d[i] - p) * (d[i] - p) + e[i] * e[i];
			    t = (x * s - z * r) / q;
			    H[i][n] = t;
			    if (Math.abs(x) > Math.abs(z)) {
				H[i+1][n] = (-r - w * t) / x;
			    } else {
				H[i+1][n] = (-s - y * t) / z;
			    }
			}
   
			// Overflow control
   
			t = Math.abs(H[i][n]);
			if ((EPS * t) * t > 1) {
			    for (int j = i; j <= n; j++) {
				H[j][n] = H[j][n] / t;
			    }
			}
		    }
		}
   
		// Complex vector
   
	    } else if (q < 0) {
		int l = n-1;

		// Last vector component imaginary so matrix is triangular
   
		if (Math.abs(H[n][n-1]) > Math.abs(H[n-1][n])) {
		    H[n-1][n-1] = q / H[n][n-1];
		    H[n-1][n] = -(H[n][n] - p) / H[n][n-1];
		} else {
		    cdiv(0.0,-H[n-1][n],H[n-1][n-1]-p,q);
		    H[n-1][n-1] = cdivr;
		    H[n-1][n] = cdivi;
		}
		H[n][n-1] = 0.0;
		H[n][n] = 1.0;
		for (int i = n-2; i >= 0; i--) {
		    double ra,sa,vr,vi;
		    ra = 0.0;
		    sa = 0.0;
		    for (int j = l; j <= n; j++) {
			ra = ra + H[i][j] * H[j][n-1];
			sa = sa + H[i][j] * H[j][n];
		    }
		    w = H[i][i] - p;
   
		    if (e[i] < 0.0) {
			z = w;
			r = ra;
			s = sa;
		    } else {
			l = i;
			if (e[i] == 0) {
			    cdiv(-ra,-sa,w,q);
			    H[i][n-1] = cdivr;
			    H[i][n] = cdivi;
			} else {
   
			    // Solve complex equations
   
			    x = H[i][i+1];
			    y = H[i+1][i];
			    vr = (d[i] - p) * (d[i] - p) + e[i] * e[i] - q * q;
			    vi = (d[i] - p) * 2.0 * q;
			    if (vr == 0.0 & vi == 0.0) {
				vr = EPS * norm * (Math.abs(w) + Math.abs(q) +
						   Math.abs(x) + Math.abs(y) +
						   Math.abs(z));
			    }
			    cdiv(x*r-z*ra+q*sa,x*s-z*sa-q*ra,vr,vi);
			    H[i][n-1] = cdivr;
			    H[i][n] = cdivi;
			    if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
				H[i+1][n-1] = (-ra - w * H[i][n-1]
					       + q * H[i][n]) / x;
				H[i+1][n] = (-sa - w * H[i][n]
					     - q * H[i][n-1]) / x;
			    } else {
				cdiv(-r-y*H[i][n-1],-s-y*H[i][n],z,q);
				H[i+1][n-1] = cdivr;
				H[i+1][n] = cdivi;
			    }
			}
   
			// Overflow control

			t = Math.max(Math.abs(H[i][n-1]),Math.abs(H[i][n]));
			if ((EPS * t) * t > 1) {
			    for (int j = i; j <= n; j++) {
				H[j][n-1] = H[j][n-1] / t;
				H[j][n] = H[j][n] / t;
			    }
			}
		    }
		}
	    }
	}
   
	// Vectors of isolated roots
   
	for (int i = 0; i < nn; i++) {
	    if (i < low | i > high) {
		for (int j = i; j < nn; j++) {
		    V[i][j] = H[i][j];
		}
	    }
	}
   
	// Back transformation to get eigenvectors of original matrix
   
	for (int j = nn-1; j >= low; j--) {
	    for (int i = low; i <= high; i++) {
		z = 0.0;
		for (int k = low; k <= Math.min(j,high); k++) {
		    z = z + V[i][k] * H[k][j];
		}
		V[i][j] = z;
	    }
	}
    }


    /* ------------------------
       Constructor
       * ------------------------ */

    /**
     * Constructor.
     * @param A a square matrix
     */
    public Eigenvalues (double[][] A) 	throws IllegalArgumentException {
	this(checkMatrix(A), A.length, true, true);
    }

    /**
     * Constructor given the number of rows and columns.
     * @param A an array containing a square matrix
     * @param n the number of rows and columns for A
     * @param strict true if the array A must contain exactly n rows
     *        and columns; false if A's rows and columns can be longer
     *        (in which case the additional entries will be ignored)
     */
    public Eigenvalues (double[][] A, int n, boolean strict)
	throws IllegalArgumentException
    {
	this(checkMatrix(A), n, strict, true);
    }

    /**
     * Constructor for a flat matrix.
     * @param flatMatrix an array representing a square matrix
     * @param n the number of rows and columns for the square matrix
     * @param columnMajorOrder true if the matrix is stored in the
     *        flatMatrix array in column-major order; false if it is
     *        stored in row-major order.
     * @param strict true if the array A must have a length of n*n;
     *        false if it can be longer.
     */
    public Eigenvalues (double[] flatMatrix, boolean columnMajorOrder,
			int n, boolean strict)
	throws IllegalArgumentException
    {
	this(getMatrixA(flatMatrix, columnMajorOrder, n), n, strict, false);
    }

    private static final double EPS = Math.scalb(1.0, -52);

    private Eigenvalues (double[][] A, int n, boolean strict, boolean copy)
	throws IllegalArgumentException
    {
	checkMatrix(A, n, strict);
	if (copy) {
    	    double[][] AA;
	    if (strict || A.length == n) {
		AA = A.clone();
	    } else {
		AA = new double[n][];
	    }
	    
	    for (int i = 0; i < n; i++) {
		if (AA[i] == A[i]) {
		    if (strict || A[i].length == n) {
			AA[i] = A[i].clone();
		    } else {
			AA[i] = new double[n];
			System.arraycopy(A[i], 0, AA[i], 0, n);
		    }
		} else if (AA[i] == null) {
		    if (strict || A[i].length == n) {
			AA[i] = A[i].clone();
		    } else {
			AA[i] = new double[n];
			System.arraycopy(A[i], 0, AA[i], 0, n);
		    }
		}
	    }
	    A = AA;
	}
	// n = Arg.getColumnDimension();
	this.n = n;
	V = new double[n][n];
	d = new double[n];
	e = new double[n];

	issymmetric = true;
	for (int j = 0; (j < n) & issymmetric; j++) {
	    for (int i = 0; (i < n) & issymmetric; i++) {
		issymmetric = (A[i][j] == A[j][i]);
	    }
	}

	if (issymmetric) {
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    V[i][j] = A[i][j];
		}
	    }
   
	    // Tridiagonalize.
	    tred2();
   
	    // Diagonalize.
	    tql2();

	} else {
	    H = new double[n][n];
	    ort = new double[n];
         
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < n; i++) {
		    H[i][j] = A[i][j];
		}
	    }
   
	    // Reduce to Hessenberg form.
	    orthes();
   
	    // Reduce Hessenberg to real Schur form.
	    hqr2();

	    // normalize eigenvectors.
	    for (int i = 0; i < n; i++) {
		if (e[i] == 0.0) {
		    // real case.
		    double max = 0.0;
		    for (int j = 0; j < n; j++) {
			double tmp = Math.abs(V[j][i]);
			if (tmp > max) {
			    max = tmp;
			}
		    }
		    if (max > 0.0) {
			for (int j = 0; j < n; j++) {
			    V[j][i] /= max;
			}
			double norm = 0.0;
			for (int j = 0; j < n; j++) {
			    double tmp = V[j][i];
			    norm += tmp*tmp;
			}
			norm = Math.sqrt(norm);
			for (int j = 0; j < n; j++) {
			    V[j][i] /= norm;
			    if (Math.abs(V[j][i]) < EPS) V[j][i] = 0.0;
			}
		    }
		} else {
		    // The first case we reach is one in which e[i] > 0
		    int k = i+1;
		    double max = 0.0;
		    for (int j = 0; j < n; j++) {
			double tmp = Math.abs(V[j][i]);
			if (tmp > max) {
			    max = tmp;
			}
			tmp = Math.abs(V[j][k]);
			if (tmp > max) {
			    max = tmp;
			}
		    }
		    if (max > 0) {
			for (int j = 0; j < n; j++) {
			    V[j][i] /= max;
			    V[j][k] /= max;
			}
			double norm = 0.0;
			for (int j = 0; j < n; j++) {
			    double tmp = V[j][i];
			    norm += tmp*tmp;
			    tmp = V[j][k];
			    norm += tmp*tmp;
			}
			norm = Math.sqrt(norm);
			for (int j = 0; j < n; j++) {
			    V[j][i] /= norm;
			    V[j][k] /= norm;
			    if (Math.abs(V[j][i]) < EPS) V[j][i] = 0.0;
			    if (Math.abs(V[j][k]) < EPS) V[j][k] = 0.0;
			}
			// The next entry has an eigenvalue with a negative
			// imaginary part, and we can skip it as we've treated
			// both cases as a pair.
			i++;
		    }
		}
	    }
	}
    }

    /* ------------------------
       Public Methods
       * ------------------------ */

    /**
     * Get the eigenvector matrix V.
     * The matrix returned stores the eigenvectors in columns. An
     * eigenvector is represented by a single column when the eigenvalue
     * is a real number and by two adjacent columns when the eigenvector is a
     * complex number.  The real part of the i<sup>th</sup> eigenvector is
     * stored in the i<sup>th</sup> column.  The complex part is stored in
     * column i+1) when the imaginary part of an eigenvalue is stored in
     * column i+1 in the matrix D (See {@link #getD()}), and in column
     * i-1 when the imaginary part of the eigenvalue is stored in column
     * i-1.  The i+1 case occurs when the imaginary part of the eigenvalue is
     * positive and the i-1 case occurs when it is negative.
     * @return the eigenvector matrix.
     */
    public double[][] getV () {
	// return new Matrix(V,n,n);
	double[][] result = new double[n][n];
	for (int i = 0; i < n; i++) {
	    System.arraycopy(V[i], 0, result[i], 0, n);
	}
	return result;
    }

    /**
     * Get the transpose of the eigenvector matrix V.
     * This is the transpose of the matrix returned by {@link #getV()}.
     * It is useful when an eigenvector should be accessed as a
     * one-dimensional array. This method is provided to avoid the
     * need to first create V and then compute its transpose.
     * @return the transpose of V
     * @see #getV()
     */
    public double[][] getVT() {
	double[][] result = new double[n][n];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		result[j][i] = V[i][j];
	    }
	}
	return result;
    }

    /**
     * Get a vector containing the real part of the k<sup>th</sup>
     * eigenvector.
     * @param k the index for the eigenvector's eigenvalue
     * @return the real part of the k<sup>th</sup> eigenvector
     * @exception IllegalArgumentException the index is out of range
     */
    public double[] getRealEigenvector(int k) throws IllegalArgumentException {
	if (k < 0 || k >= n) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", k));
	}
	double[] result = new double[n];
	for (int i = 0; i < n; i++) {
	    result[i] =  V[i][k];
	}
	return result;
    }

    /**
     * Get a vector containing the imaginary part of the k<sup>th</sup>
     * eigenvector.
     * @param k the index for the eigenvector's eigenvalue
     * @return the imaginary part of the k<sup>th</sup> eigenvector
     * @exception IllegalArgumentException the index is out of range
     */
    public double[] getImagEigenvector(int k) throws IllegalArgumentException {
	if (k < 0 || k >= n) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", k));
	}
	if (e[k] == 0.0) return new double[n];
	double[] result = new double[n];
	k = (e[k] < 0.0)? (k-1): (k+1);
	for (int  i = 0; i < n; i++) {
	    result[i] = V[i][k];
	}
	return result;
    }

    /**
     * Get the real parts of the eigenvalues.
     * These are stored largest first.
     * @return the real parts of the eigenvalues
     */
    public double[] getRealEigenvalues () {
	return d;
    }

    /**
     * Get the imaginary parts of the eigenvalues.
     * These are stored in order of their real parts, with
     * complex conjugate pairs adjacent, the one with a positive
     * imaginary part first.
     * @return the imaginary parts of the eigenvalues
     */

    public double[] getImagEigenvalues () {
	return e;
    }

    /**
     * Get the real part of an eigenvalue.
     * These are stored largest first.
     * @param i an index in the range [0,n) naming the eigenvalue
     * @return the real parts of the eigenvalue
     * @exception IllegalArgumentException the index is out of range
     */
    public double getRealEigenvalue(int i) throws IllegalArgumentException {
	if (i < 0 || i >= n) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return d[i];
    }

    /**
     * Get the imaginary part of an eigenvalue.
     * These are stored in order of their real parts, with
     * complex conjugate pairs adjacent, the one with a positive
     * imaginary part first.
     * @param i an index in the range [0,n) naming the eigenvalue
     * @return the real parts of the eigenvalue
     * @exception IllegalArgumentException the index is out of range
     */
    public double getImagEigenvalue(int i) throws IllegalArgumentException {
	if (i < 0 || i >= n) {
	    throw new IllegalArgumentException(errorMsg("argOutOfRangeI", i));
	}
	return e[i];
    }

    /**
     * Get the block diagonal eigenvalue matrix Real eigenvalues are
     * represented by 1-by-1 blocks containing the value itself.
     * Complex eigenvalues occur in pairs (one the complex conjugate
     * of the other. For eigenvalues &lambda;&plusmn;&mu; with
     * &mu;&ge;0, there are two rows in the matrix.  for the
     * &lambda;+&mu; case, the real part of the eigenvalue is on the
     * matrice's diagonal and the imaginary part is in the next column
     * in the same row. For the &lambda;-&mu; case, the real part of
     * the eigenvalue is on the matrice's diagonal and the imaginary
     * part is in the previous column in the same row. For each pair,
     * the one with the positive imaginary part occurs first.
     * @return the block diagonal eigenvalue matrix
     */

    public double[][] getD () {
	// Matrix X = new Matrix(n,n);
	double[][] D = new double[n][n];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		D[i][j] = 0.0;
	    }
	    D[i][i] = d[i];
	    if (e[i] > 0) {
		D[i][i+1] = e[i];
	    } else if (e[i] < 0) {
		D[i][i-1] = e[i];
	    }
	}
	return D;
    }
    private static final long serialVersionUID = 1;
}

//  LocalWords:  exbundle th plusmn BLOCKQUOTE PRE ai SVDecomp cond
//  LocalWords:  nonsymmetric Jama MathWorks QRDecomp bzdev runtime
//  LocalWords:  nullMatrix noRows intArgNotPositive missingRows tred
//  LocalWords:  wrongRowSize flatMatrixTooShort Hessenberg Bowdler
//  LocalWords:  tridiagonal Reinsch Fortran EISPACK QL tql orthes ge
//  LocalWords:  subdiagonal ortran Schur hqr balanc hoc MATLAB's Arg
//  LocalWords:  Backsubstitute flatMatrix columnMajorOrder getD getV
//  LocalWords:  getColumnDimension Tridiagonalize Diagonalize
//  LocalWords:  eigenvector's IllegalArgumentException matrice's
//  LocalWords:  argOutOfRangeI
