package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

// See http://en.wikipedia.org/wiki/Tridiagonal_matrix_algorithm for
// an algorithm description.

// See http://www.particleincell.com/blog/2012/bezier-splines/ for
// a Bezier spline example.

/**
 * Solve the equation y = Ax where x and y are column vectors and A is
 * a tridiagonal matrix (one with nonzero values on and direction above
 * and below the diagonal).
 * The matrix equation can be written (with m=n-1) as
 * <blockquote><pre>
 * .-  -.    .-                       -.  .-  -.
 * | y1 |    | b1 c1 0 . . . . . .  0  |  | x1 |
 * | y2 |    | a2 b2 c2                |  | x2 |
 * | y3 |    | 0  a3 b3 c3             |  | x3 |
 * | .  | =  | .    .   .   .          |  | .  |
 * | .  |    | .     .   .   .         |  | .  |
 * | .  |    | .      .   .   .     cm |  | .  |
 * | yn |    | 0                an  bn |  | xn |
 *  -  -   -                          -    -  -
 * </pre></blockquote>
 * and corresponds ot the system of equations
 * <blockquote><pre>
 *  a_i x_{i-1} + b_i x_i + c_i x_{i+1} = y_i
 * </pre></blockquote>
 * with a_1 = 0 and c_n = 0. and with "_" denoting a subscript.
 * <P>
 * This is a special case that allows a very fast solution - one with
 * a time complexity of O(N) - by using the tridiagonal matrix algorithm
 * (also called the Thomas algorithm). A description of the algorithm can
 * be found in a
 * <a href="http://en.wikipedia.org/wiki/Tridiagonal_matrix_algorithm">
 * Wikipedia article</a>.  Basically, it uses row reduction to force all
 * the elements below the diagonal to be zero.  This provides a direct
 * solution for x_n, and the values for other indices can be obtained by
 * substitution. The example implementations in the Wikipedia article as seen
 * on November 22, 2013 are incomplete: they do not handle the case in
 * which a diagonal element (possibly modified by row reduction) is zero.
 * In the matrix shown above, if b1 is zero, then both c1 and a2 must be
 * non-zero (otherwise the matrix is singular). Since b1 is zero, c1 is
 * the only non-zero element in row 1, so row reduction can be used to make
 * a3 zero with a modified value of y3.  Then x2 = y1/c1 and
 * y1 = (y2 - b2y1/c1 - c2x3)/a2.  One can then solve
 * <blockquote><pre>
 * .-  -.    .-                 -.  .-  -.
 * | y'3|    | b3 c3 . . .     0 |  | x3 |
 * | .  | =  | a4 b4 c4          |  | .  |
 * | .  |    |  .  .   .         |  | .  |
 * | .  |    |  .   .   .     cm |  | .  |
 * | yn |    |  0  . . .  an  bn |  | xn |
 *  -  -   -                    -    -  -
 * </pre></blockquote>
 * where y'3 is the modified value of y3. To eliminate a corner case, explicit
 * solutions are used for 2x2 and 1x1 matrices.
 * <P>
 * The implementation, which uses the Java convention of indices
 * starting from 0 instead of 1, can represent the matrix as vectors
 * a, b and c of length n representing the subdiagonal, diagonal and
 * superdiagonal elements respectively.  All are the same length, with
 * a[0] and c[n-1] ignored.  I.e., for the n by n matrix A that these
 * vectors represent, a[i] = A[i][i-1], b[i] = A[i][i], and c[i] = A[i][i+1].
 * <P>
 * For the cyclic case, the matrix equation is
 * <blockquote><pre>
 * .-  -.    .-                       -.  .-  -.
 * | y1 |    | b1 c1 0 . . . . . 0  a1 |  | x1 |
 * | y2 |    | a2 b2 c2                |  | x2 |
 * | y3 |    | 0  a3  b3 c3            |  | x3 |
 * | .  | =  | .    .   .   .          |  | .  |
 * | .  |    | .     .   .   .         |  | .  |
 * | .  |    | .      .   .   .     cm |  | .  |
 * | yn |    | cn  0  .  .  . 0 an  bn |  | xn |
 *  -  -   -                          -    -  -
 * </pre></blockquote>
 * and corresponds to the system of equations
 * <blockquote><pre>
 *  a_1 x_n + b_1 x_1 + c_1 x_2         = z_1
 *  a_i x_{i-1} + b_i x_i + c_i x_{i+1} = z_i  (for 1 &lt; i &lt; n)
 *  c_n x_1 + a_m x_m + b_n x_n         = z_n
 * </pre></blockquote>
 * The same technique works for a solution, but with additional elements
 * that must be forced to a value of zero. and with a "fall-back" to
 * LU Decomposition when one might have to divide by the value of a
 * diagonal element that is zero.
 * <P>
 * Solutions can be obtained by using static methods or instance methods.
 * When instance methods are used, the constructor perform part of the
 * calculation and store the results in tables used for each solution.
 * This is more efficient when multiple solutions are needed, each for
 * a different value of y, but slightly more costly if the solution is
 * used for only a single value of y.
 */
public class TridiagonalSolver {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }


    /**
     * Solve a tridiagonal matrix equation y=Ax for x with A represented
     * by vectors giving the subdiagonal, diagonal, and superdiagonal elements.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, and superdiagonal elements, which can
     * have any value. The diagonal subdiagonal, and superdiagonal
     * elements are described by arrays of length n. The first element
     * of the subdiagonal vector and the (n-1) argument of the superdiagonal
     * vector are ignored.
     * <P>
     * If the arguments x and y are identical, the array y will be
     * overwritten with the solution. The solutions are only for the
     * non-cyclic case.
     * @param x an array to hold the solution
     * @param a the subdiagonal elements of a tridiagonal matrix, with
     *        a[0] ignored
     * @param b the diagonal elements of the matrix
     * @param c the superdiagonal elements of the matrix, with c[n-1]
     *          ignored
     * @param y an array to hold the known values
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solve(double[]x,
				 double[]a, double[]b, double[]c,
				 double[] y)
	throws IllegalArgumentException
    {
	return solve(x, a, b, c, y, y.length);
    }

    /**
     * Construct a TridiagonalSolver given the subdiagonal, diagonal, and
     * superdiagonal elements for a tridiagonal system of equations.
     * If the cyclic argument's value is false, the system of
     * equations can be represented by a matrix with subdiagonal,
     * diagonal, and superdiagonal elements, and with the remaining
     * elements set to 0.  If the cyclic argument's value is true,
     * the matrix is assumed to have nonzero elements at its lower-left and
     * upper right corners, corresponding to the last value of c and the
     * first value of a respectively.
     * @param a the subdiagonal elements of a tridiagonal matrix, with
     *        a[0] ignored if cyclic is false and used if cyclic is true
     * @param b the diagonal elements of the matrix
     * @param c the superdiagonal elements of the matrix, with c[c.length-1]
     *          ignored if cyclic is false and used if cyclic is true
     * @param cyclic true if the system of equations is cyclic; false otherwise
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */

    public TridiagonalSolver(double[]a, double[]b, double[]c, boolean cyclic)
	throws IllegalArgumentException
    {
	this(a, b, c, cyclic, b.length);
    }

    /**
     * Construct a TridiagonalSolver given the subdiagonal, diagonal, and
     * superdiagonal elements for a tridiagonal system of equations,  using
     * the first n elements of the one-dimensional arrays specifying the
     * system of equations.
     * If the cyclic argument's value is false, the system of
     * equations can be represented by a matrix with subdiagonal,
     * diagonal, and superdiagonal elements, and with the remaining
     * elements set to 0.  If the cyclic argument's value is true,
     * the matrix is assumed to have nonzero elements at its lower-left and
     * upper right corners, corresponding to the last value of c and the
     * first value of a respectively.
     * <P>
     * Specifying the argument n explicitly is an optimization for
     * allowing existing arrays to be reused.
     * @param a the subdiagonal elements of a tridiagonal matrix, with
     *        a[0] ignored if cyclic is false and used if cyclic is true
     * @param b the diagonal elements of the matrix
     * @param c the superdiagonal elements of the matrix, with c[n]
     *          ignored if cyclic is false and used if cyclic is true
     * @param n number of elements in the arrays to use
     * @param cyclic true if the system of equations is cyclic; false otherwise
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */

    public TridiagonalSolver(double[]a, double[]b, double[]c, boolean cyclic,
			     int n)
	throws IllegalArgumentException
    {
	this.n = n;
	nb = n;
	double[] cprime = new double[n];
	double[] d = new double[n];
	System.arraycopy(c, 0, cprime, 0, n);
	if (cyclic) {
	    double[] dprime = new double[n];
	    if (initCyclic(a, b, cprime, d, n) == false) {
		findex = 0;
		bindex = 0;
		fentries = null;
		bentries = null;
		nf = 0;
		nb = 0;
		double[][] matrix = new double[n][n];
		int nm1 = n-1;
		matrix[0][0] = b[0];
		matrix[0][1] = c[0];
		matrix[0][nm1] = a[0];
		matrix[nm1][0] = c[nm1];
		matrix[nm1][nm1] = b[nm1];
		matrix[nm1][n-2] = a[nm1];

		for (int i = 1; i < nm1; i++) {
		    matrix[i][i-1] = a[i];
		    matrix[i][i] = b[i];
		    matrix[i][i+1] = c[i];
		}

		lud = new LUDecomp(matrix);
		matrix = null;
	    }
	} else {
	    nf = n;
	    fentries = new ForwardEntry[nf];
	    bentries = new BackwardEntry[nb];
	    initNonCyclic(a, b, cprime, 0, n);
	}
    }

    /**
     * Solve a tridiagonal matrix equation y=Ax for x where A is an n
     * by n matrix with A represented by vectors giving the
     * subdiagonal, diagonal, and superdiagonal elements and with the
     * value n specified explicitly.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, and superdiagonal elements, which can
     * have any value. The diagonal subdiagonal, and superdiagonal
     * elements are described by arrays of length n. The first element
     * of the subdiagonal vector and the (n-1) argument of the superdiagonal
     * vector are ignored. The solutions are only for the
     * non-cyclic case.
     * <P>
     * If the arguments x and y are identical, the array y will be
     * overwritten with the solution.
     * @param x an array to hold the solution
     * @param a the subdiagonal elements of a tridiagonal matrix, with
     *        a[0] ignored
     * @param b the diagonal elements of the matrix
     * @param c the superdiagonal elements of the matrix, with c[n-1]
     *          ignored
     * @param y a vector contain the known values
     * @param n the number of rows and columns in the square matrix A
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solve(double[]x,
			  double[]a, double[]b, double[]c,
			  double[] y,
			  int n)
	throws IllegalArgumentException
    {
	if (x == null) x = new double[n];
	if (x != y) {
	    System.arraycopy(y, 0, x, 0, n);
	}
	double cprime[] = new double[n];
	System.arraycopy(c, 0, cprime, 0, n);
	return solve(x, a, b, cprime, 0, n);
    }

    int n;

    LUDecomp lud = null;
    static enum ForwardEntryOpCode {
	BZEROINITIAL, DIVBYB, SUBANDDIV, FIXLASTX
    }

    static class ForwardEntry {
	ForwardEntryOpCode opcode;
	int i;
	double q;
	double r;
	double s;
	double t;
    };
    int findex = 0;
    int nf;
    ForwardEntry[] fentries = null;
    private void forward(double[] x) {
	for (int k = 0; k < findex; k++) {
	    ForwardEntry fe = fentries[k];
	    int i = fe.i;
	    switch(fe.opcode) {
	    case BZEROINITIAL:
		// x[i+2] -= x[i] * a[i+2] / cp
		x[i+2] -= x[i]*fe.q;
		break;
	    case DIVBYB:
		// x[i] = x[i] / b[i];
		x[i] /= fe.q;
		break;
	    case SUBANDDIV:
		// x[i] = (x[i] - x[i-1]*a[i]) / tmp;
		x[i] = (x[i] - x[i-1]*fe.q)/fe.r;
		break;
	    case FIXLASTX:
		// x[nm1] -= x[i-1]*cj;
		x[n-1] -= x[i-1] * fe.q;
		break;
	    }
	}
    }

    static enum BackwardEntryOpCode {
	TWOBYTWOMATRIX, ONEBYONEMATRIX, BZEROCOMP, NORMALCOMP,
	    INITIALCYCLICCOMP, NORMALCYCLICCOMP
    }

    static class  BackwardEntry {
	BackwardEntryOpCode opcode;
	int i;
	double p;
	double q;
	double r;
	double s;
	double t;
    }

    int bindex = 0;
    int nb;
    BackwardEntry[] bentries = null;

    private void backward(double[] x) {
	for (int k = 0; k < bindex; k++) {
	    BackwardEntry be = bentries[k];
	    int i = be.i;
	    switch(be.opcode) {
	    case TWOBYTWOMATRIX:
		{
		    double det = be.p;
		    double xi =(x[i] * be.q - x[i+1] * be.r) / be.p;
		    double xip1 = (be.s * x[i+1] - be.t * x[i])/be.p;
		    x[i] = xi;
		    x[i+1] = xip1;
		}
		break;
	    case ONEBYONEMATRIX:
		x[i] = x[i] / be.p;
		break;
	    case BZEROCOMP:
		{
		    // double xip1 = x[i]/cp;
		    // double xi = (x[i+1] - b[i+1]*x[i]/cp - cp2*x[i+2])
		    double xip1 = x[i] / be.p;
		    double xi = (x[i+1] - be.q * x[i] - be.r * x[i+2]) / be.s;
		    x[i+1] = xip1;
		    x[i] = xi;
		}
		break;
	    case NORMALCOMP:
		// x[i] = x[i] - c[i]*x[i+1]
		x[i] = x[i] - be.p * x[i+1];
		break;
	    case INITIALCYCLICCOMP:
		// x[i] = x[i] - d[i] *x[nm1];
		x[i] = x[i] - be.p * x[n-1];
		break;
	    case NORMALCYCLICCOMP:
		// x[i] = x[i] - c[i]*x[ip1] - d[i] * x[nm1];
		x[i] = x[i] - be.p * x[i+1] -be.q * x[n-1];
		break;
	    }
	}
    }

    /**
     * Solve a tridiagonal system of equations y = Ax, whether cyclic or not.
     * If x and y are the same array, the output will overwrite the input.
     * @param x an output array to hold the solution
     * @param y an input array to hold the known values
     */
    public final void solve(double[] x, double[] y) {
	if (lud != null) {
	    lud.solve(x, y);
	} else {
	    if (x != y) {
		System.arraycopy(y, 0, x, 0, n);
	    }
	    forward(x);
	    backward(x);
	}
    }

    static final String NOSOLUTION = errorMsg("linearlyDependent");

    private void initNonCyclic(double[]a, double[]b, double[]c, int k, int n)
	throws IllegalArgumentException
    {

	BackwardEntry be;
	ForwardEntry fe;
	int nmk = n - k;
	if (nmk < 3) {
	    if (nmk == 2) {
		double det = b[k]*b[k+1] - a[k+1] * c[k];
		if (det == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		be = new BackwardEntry();
		bentries[bindex++] = be;
		be.i = k;
		be.opcode =  BackwardEntryOpCode.TWOBYTWOMATRIX;
		be.p = det;
		be.q = b[k+1]; be.r = c[k];
		be.s = b[k]; be.t = a[k+1];
		// double xk = (x[k] * b[k+1] - x[k+1]*c[k]) / det;
		// double xkp1 = (b[k] * x[k+1]- a[k+1]*x[k]) / det;
		// x[k] = xk;
		// x[k+1] = xkp1;
	    } else if (nmk == 1) {
		be = new BackwardEntry();
		bentries[bindex++] = be;
		be.i = k;
		be.opcode =  BackwardEntryOpCode.ONEBYONEMATRIX;
		be.p = b[k];
		// x[k] = x[k] / b[k];
	    }
	    return;
	}
	if (a.length < n || c.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	int i = k;
	if (b[i] == 0.0) {
	    double cp = c[i];
	    double cp2 = c[i+1];
	    if (cp == 0.0 || a[i+1] == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    // adjust x[i+2] because we have to make a[i+2] in the matrix
	    // zero (but we never use it) so that x[j] for j < i+2 will
	    // always be multiplied by zero for computing x[j] for j >= i+2.
	    fe = new ForwardEntry();
	    fentries[findex++] = fe;
	    fe.i = i;
	    fe.opcode = ForwardEntryOpCode.BZEROINITIAL;
	    fe.q = a[i+2]/cp;
	    // x[i+2] -= x[i] * a[i+2] / cp;
	    initNonCyclic(a, b, c, i+2, n);
	    be = new BackwardEntry();
	    bentries[bindex++] = be;
	    be.i = i;
	    be.opcode = BackwardEntryOpCode.BZEROCOMP;
	    be.p  = cp;
	    be.q = b[i+1]/cp;
	    be.r = cp2;
	    be.s = a[i+1];
	    // double xip1 = x[i]/cp;
	    // double xi = (x[i+1] - b[i+1]*x[i]/cp - cp2*x[i+2]) / a[i+1];
	    // x[i+1] = xip1;
	    // x[i] = xi;
	    return;
	}
	c[i] = c[i] / b[i];
	fe = new ForwardEntry();
	fentries[findex++] = fe;
	fe.i = i;
	fe.opcode = ForwardEntryOpCode.DIVBYB;
	fe.q = b[i];
	// x[i] = x[i] / b[i];
	int nm1 = n-1;
	double tmp;
	boolean needLastIteration = true;
	for (i = i+1; i < nm1; i++) {
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) {
		double cp = c[i];
		double cp2 = c[i+1];
		if (cp == 0.0 || a[i+1] == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		fe = new ForwardEntry();
		fentries[findex++] = fe;
		fe.i = i;
		fe.opcode = ForwardEntryOpCode.BZEROINITIAL;
		fe.q = a[i+2] / cp;
		// x[i+2] -= x[i] * a[i+2] / cp;

		initNonCyclic(a, b, c, i+1, k);
		be = new BackwardEntry();
		bentries[bindex++] = be;
		be.i = i;
		be.opcode = BackwardEntryOpCode.BZEROCOMP;
		be.p  = cp;
		be.q = b[i+1]/cp;
		be.r = cp2;
		be.s = a[i+1];
		// double xip1 = x[i]/cp;
		// double xi = (x[i+1] - b[i+1]*x[i]/cp - cp2*x[i+2]) / a[i+1];
		// x[i+1] = xip1;
		// x[i] = xi;
		needLastIteration=false;
		break;
	    }
	    c[i] = c[i] / tmp;
	    fe = new ForwardEntry();
	    fentries[findex++] = fe;
	    fe.i = i;
	    fe.opcode = ForwardEntryOpCode.SUBANDDIV;
	    fe.q = a[i];
	    fe.r = tmp;
	    // x[i] = (x[i] - x[i-1]*a[i]) /tmp;
	}
	if (needLastIteration) {
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    c[i] = c[i] / tmp;
	    fe = new ForwardEntry();
	    fentries[findex++] = fe;
	    fe.i = i;
	    fe.opcode = ForwardEntryOpCode.SUBANDDIV;
	    fe.q = a[i];
	    fe.r = tmp;
	    // x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	}
	while (--i >= k) {
	    be = new BackwardEntry();
	    bentries[bindex++] = be;
	    be.i = i;
	    be.opcode = BackwardEntryOpCode.NORMALCOMP;
	    be.p = c[i];
	    // x[i] = x[i] - c[i]*x[i+1];
	}
	return;
    }


    private static  double[] solve(double[]x,
				   double[]a, double[]b, double[]c,
				   int k, int n)
	throws IllegalArgumentException
    {
	int nmk = n - k;
	if (nmk < 3) {
	    if (nmk == 2) {
		double det = b[k]*b[k+1] - a[k+1] * c[k];
		if (det == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		double xk = (x[k] * b[k+1] - x[k+1]*c[k]) / det;
		double xkp1 = (b[k] * x[k+1]- a[k+1]*x[k]) / det;
		x[k] = xk;
		x[k+1] = xkp1;
	    } else if (nmk == 1) {
		x[k] = x[k] / b[k];
	    }
	    return x;
	}
	if (a.length < n || c.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	if (x.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	int i = k;
	if (b[i] == 0.0) {
	    double cp = c[i];
	    double cp2 = c[i+1];
	    if (cp == 0.0 || a[i+1] == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    // adjust x[i+2] because we have to make a[i+2] in the matrix
	    // zero (but we never use it) so that x[j] for j < i+2 will
	    // always be multiplied by zero for computing x[j] for j >= i+2.
	    x[i+2] -= x[i] * a[i+2] / cp;
	    solve(x, a, b, c, i+2, n);
	    double xip1 = x[i]/cp;
	    double xi = (x[i+1] - b[i+1]*x[i]/cp - cp2*x[i+2])
		/ a[i+1];
	    x[i+1] = xip1;
	    x[i] = xi;
	    return x;
	}
	c[i] = c[i] / b[i];
	x[i] = x[i] / b[i];
	int nm1 = n-1;
	double tmp;
	boolean needLastIteration = true;
	for (i = i+1; i < nm1; i++) {
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) {
		double cp = c[i];
		double cp2 = c[i+1];
		if (cp == 0.0 || a[i+1] == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		x[i+2] -= x[i] * a[i+2] / cp;
		solve(x, a, b, c, i+1, k);
		double xip1 = x[i]/cp;
		double xi = (x[i+1] - b[i+1]*x[i]/cp - cp2*x[i+2]) / a[i+1];
		x[i+1] = xip1;
		x[i] = xi;
		needLastIteration=false;
		break;
	    }
	    c[i] = c[i] / tmp;
	    x[i] = (x[i] - x[i-1]*a[i]) /tmp;
	}
	if (needLastIteration) {
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    c[i] = c[i] / tmp;
	    x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	}
	while (--i >= k) {
	    x[i] = x[i] - c[i]*x[i+1];
	}
	return x;
    }


    /**
     * Solve a tridiagonal matrix equation y=Ax for x where A is an n by n
     * matrix.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, and superdiagonal elements, which can
     * have any value.
     * @param x an array to hold the solution
     * @param A the n by n tridiagonal matrix (array sizes may be larger)
     * @param y a vector contain the known values
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solve(double[] x, double[][]A, double[] y)
	throws IllegalArgumentException
    {
	return solve(x, A, y, y.length);
    }
    /**
     * Solve a tridiagonal matrix equation y=Ax for x where A is an n by n
     * matrix with n given explicitly.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, and superdiagonal elements, and the
     * upper-right and lower-left elements, which can have any value.
     * @param x an array to hold the solution
     * @param A the n by n tridiagonal matrix (array sizes may be larger)
     * @param y a vector contain the known values
     * @param n the number of rows and columns in the square matrix A.
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solve(double[] x, double[][] A, double[] y, int n)
	throws IllegalArgumentException
    {
	if (x == null) x = new double[n];
	if (A[0][n-1] != 0.0 || A[n-1][0] != 0) {
	    // this is the cyclic case.
	    double[] a = new double[n];
	    double[] b = new double[n];
	    double[] c = new double[n];
	    double[] d = new double[n];
	    int nm1 = n-1;
	    for (int i = 1; i < nm1; i++) {
		a[i] = A[i][i-1];
		b[i] = A[i][i];
		c[i] = A[i][i+1];
	    }
	    if (x != y) {
		System.arraycopy(y, 0, x, 0, n);
	    } else {
		// need to preserve y in case solveCyclicFast returns false
		double tmp[] = new double[n];
		System.arraycopy(y, 0, tmp, 0, n);
		y = tmp;
	    }
	    if (solveCyclicFast(x, a, b, c, d, n)) {
		return x;
	    } else {
		LUDecomp Alud = new LUDecomp(A);
		Alud.solve(x, y);
		return x;
	    }
	}
	if (x == null) x = new double[n];
	if (x != y) {
	    System.arraycopy(y, 0, x, 0, n);
	}
	double[] cprime = new double[n];
	for (int i = 0; i < n-1; i++) {
	    cprime[i] = A[i][i+1];
	}
	return solve(x, A, cprime, 0, n);
    }

    private static synchronized double[] solve(double[]x,
					       double[][]A, double[]c,
					       int k, int n)
	throws IllegalArgumentException
    {
	int nmk = n - k;
	if (nmk < 3) {
	    if (nmk == 2) {
		int kp1 = k + 1;
		int kp2 = k + 2;
		double det = A[k][k]*A[kp1][kp2] - A[kp1][k] * c[k];
		if (det == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		double xk = (x[k] * A[kp1][kp1] - x[kp1]*c[k]) / det;
		double xkp1 = (A[k][k] * x[kp1]- A[kp1][k]*x[k]) / det;
		x[k] = xk;
		x[kp1] = xkp1;
	    } else if (nmk == 1) {
		x[k] = x[k] / A[k][k];
	    }
	    return x;
	}
	if (A.length < n || c.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	if (x.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	int i = k;
	if (A[i][i] == 0.0) {
	    int ip1 = i+1;
	    int ip2 = i+2;
	    double cp = c[i];
	    double cp2 = c[ip1];
	    if (cp == 0.0 || A[ip1][i] == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    // adjust x[ip2] because we have to make A[ip2][ip1] in the matrix
	    // zero (but we never use it) so that x[j] for j < ip2 will
	    // always be multiplied by zero for computing x[j] for j >= ip2.
	    x[ip2] -= x[i] * A[ip2][ip1] / cp;
	    solve(x, A, c, ip2, n);
	    double xip1 = x[i]/cp;
	    double xi = (x[ip1] - A[ip1][ip1]*x[i]/cp - cp2*x[ip2])
		/ A[ip1][i];
	    x[ip1] = xip1;
	    x[i] = xi;
	    return x;
	}
	c[i] = c[i] / A[i][i];
	x[i] = x[i] / A[i][i];
	int nm1 = n-1;
	double tmp;
	boolean needLastIteration = true;
	for (i = i+1; i < nm1; i++) {
	    int ip1 = i+1;
	    int ip2 = i+2;
	    int im1 = i-1;
	    tmp = (A[i][i] - A[i][im1] * c[im1]);
	    if (tmp == 0.0) {
		double cp = c[i];
		double cp2 = c[ip1];
		if (cp == 0.0 || A[ip1][i] == 0.0) {
		    throw new IllegalArgumentException(NOSOLUTION);
		}
		x[ip2] -= x[i] * A[ip2][ip1] / cp;
		solve(x, A, c, i+1, k);
		double xip1 = x[i]/cp;
		double xi = (x[ip1] - A[ip2][ip2]*x[i]/cp - cp2*x[ip2])
		    / A[ip1][i];
		x[ip1] = xip1;
		x[i] = xi;
		needLastIteration=false;
		break;
	    }
	    c[i] = c[i] / tmp;
	    x[i] = (x[i] - x[im1]*A[i][im1]) /tmp;
	}
	if (needLastIteration) {
	    int im1 = i-1;
	    tmp = (A[i][i] - A[i][im1] * c[im1]);
	    if (tmp == 0.0) {
		throw new IllegalArgumentException(NOSOLUTION);
	    }
	    x[i] = (x[i] - x[im1] * A[i][im1]) / tmp;
	}
	while (--i >= k) {
	    int ip1 = i+1;
	    x[i] = x[i] - c[i]*x[ip1];
	}
	return x;
    }

    /**
     * Solve a cyclic tridiagonal matrix equation y=Ax for x where A is an n
     * by n matrix with A represented by vectors giving the
     * subdiagonal, diagonal, and superdiagonal elements.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, superdiagonal elements, and the upper-right
     * and lower-left corner elements, all of which can have arbitrary
     * values. The diagonal subdiagonal, and superdiagonal
     * elements are described by arrays of length n. The first element
     * of the subdiagonal vector is treated as the upper-right corner
     * element and the last element of the superdiagonal vector is treated
     * as the lower-left corner element.
     * @param x an array to hold the solution
     * @param a the subdiagonal elements of the cyclic tridiagonal matrix,
     *        except for a[0], which appears in the upper right-hand corner
     * @param b the diagonal elements of the cyclic tridiagonal matrix
     * @param c the superdiagonal elements of the cyclic tridiagonal matrix
     *        except for c[n-1], which appears in the lower left-hand corner
     * @param y a vector contain the known values
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solveCyclic(double[]x,
				       double[]a, double[]b, double[]c,
				       double[] y)
    {
	return solveCyclic(x, a, b, c, y, y.length);
    }

    /**
     * Solve a cyclic tridiagonal matrix equation y=Ax for x where A is an n
     * by n matrix with A represented by vectors giving the
     * subdiagonal, diagonal, and superdiagonal elements, and with n specified.
     * A is a matrix whose elements are all zero except for the
     * diagonal, subdiagonal, superdiagonal elements, and the
     * upper-right and lower-left corner elements, all of which can
     * have arbitrary values. The diagonal subdiagonal, and
     * superdiagonal elements are described by arrays of length at
     * least n, with only the first n elements used. The first element
     * of the subdiagonal vector is treated as the upper-right corner
     * element and the last element of the superdiagonal vector is
     * treated as the lower-left corner element.
     * @param x an array to hold the solution
     * @param a the subdiagonal elements of the cyclic tridiagonal matrix,
     *        except for a[0], which appears in the upper right-hand corner
     * @param b the diagonal elements of the cyclic tridiagonal matrix
     * @param c the superdiagonal elements of the cyclic tridiagonal matrix
     *        except for c[n-1], which appears in the lower left-hand corner
     * @param y a vector contain the known values
     * @param n the number of rows and columns in the square matrix A
     * @return the solution for x of the equation y = Ax, contained in
     *         the array x or in a new array if x is null
     * @exception IllegalArgumentException the system of equations is
     *            linearly dependent so there is no unique solution or
     *            a value is out of range
     */
    public static double[] solveCyclic(double[]x,
				       double[]a, double[]b, double[]c,
				       double[] y,
				       int n)
	throws IllegalArgumentException
    {
	if (n < 3) {
	    throw new IllegalArgumentException
		(errorMsg("needAtLeast3Unknowns"));
	}
	if (n == 3) {
	    // simple solution using determinants as it is a 3 by 3 matrix
	}
	if (a.length < n || c.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	if (x == null) x = new double[n];
	if (x.length < n) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	if (x != y) {
	    System.arraycopy(y, 0, x, 0, n);
	} else {
	    // need to preserve y in case solveCyclicFast returns false
	    double tmp[] = new double[n];
	    System.arraycopy(y, 0, tmp, 0, n);
	    y = tmp;
	}
	double[] cprime = new double[n];
	System.arraycopy(c, 0, cprime, 0, n);
	double[] dprime = new double[n];
	if (solveCyclicFast(x, a, b, cprime, dprime, n)) {
	    return x;
	} else {
	    // need a general solution.  Use LU decomposition, which
	    // will be handled by a different class.
	    System.arraycopy(y, 0, x, 0, n);
	    solveCyclicSlow(x, a, b, c, n);
	    return x;
	}
    }

    private boolean initCyclic(double[] a, double[]b, double[] c,
			       double[] d, int n)
    {
	if (b[0] == 0) return false;
	this.n = n;
	nf = 2*n;
	nb = n;
	findex = 0;
	bindex = 0;
	fentries = new ForwardEntry[nf];
	bentries = new BackwardEntry[nb];

	BackwardEntry be;
	ForwardEntry fe;

	int i;
	int nm1 = n-1;
	int nm2 = n-2;
	double cj = c[nm1];
	d[nm1] = b[nm1];
	d[nm2] = c[nm2];
	d[0] = a[0];
	c[0] = c[0] / b[0];
	d[0] = a[0] / b[0];
	fe = new ForwardEntry();
	fentries[findex++] = fe;
	fe.i = 0;
	fe.opcode = ForwardEntryOpCode.DIVBYB;
	fe.q = b[0];
	// x[0] = x[0] / b[0];
	double tmp;
	for (i = 1; i < nm2; i++) {
	    fe = new ForwardEntry();
	    fentries[findex++] = fe;
	    fe.i = i;
	    fe.opcode = ForwardEntryOpCode.FIXLASTX;
	    fe.q = cj;
	    // x[nm1] -= x[i-1]*cj;
	    d[nm1] -= d[i-1]*cj;
	    cj = -cj * c[i-1];
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) return false;
	    c[i] = c[i] / tmp;
	    fe = new ForwardEntry();
	    fentries[findex++] = fe;
	    fe.i = i;
	    fe.opcode = ForwardEntryOpCode.SUBANDDIV;
	    fe.q = a[i];
	    fe.r = tmp;
	    //x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	    d[i] = -d[i-1]*a[i] / tmp;
	}
	d[nm1] -= cj*d[i-1];
	fe = new ForwardEntry();
	fentries[findex++] = fe;
	fe.i = i;
	fe.opcode = ForwardEntryOpCode.FIXLASTX;
	fe.q = cj;
	// x[nm1] -= cj * x[i-1];
	cj = a[nm1] - cj * c[i-1];
	tmp = (b[i] - a[i] * c[i-1]);
	if (tmp == 0.0) return false;
	fe = new ForwardEntry();
	fentries[findex++] = fe;
	fe.i = i;
	fe.opcode = ForwardEntryOpCode.SUBANDDIV;
	fe.q = a[i];
	fe.r = tmp;
	// x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	d[i] = (d[i] -  d[i-1]*a[i]) / tmp;
	i++;
	if (i != nm1) throw new RuntimeException
			  (errorMsg("tridiagonalIter"));
	tmp = (d[i] - cj * d[i-1]);
	if (tmp == 0.0) return false;
	fe = new ForwardEntry();
	fentries[findex++] = fe;
	fe.i = i;
	fe.opcode = ForwardEntryOpCode.SUBANDDIV;
	fe.q = cj;
	fe.r = tmp;
	// x[i] = (x[i] - x[i-1] * cj) / tmp;
	d[i] = (d[i] - d[i-1] * cj)/ tmp;
	if (--i >= 0) {
	    be = new BackwardEntry();
	    bentries[bindex++] = be;
	    be.i = i;
	    be.opcode = BackwardEntryOpCode.INITIALCYCLICCOMP;
	    be.p = d[i];
	    // x[i] = x[i] - d[i] * x[nm1];
	}
	while (--i >= 0) {
	    int ip1 = i+1;
	    be = new BackwardEntry();
	    bentries[bindex++] = be;
	    be.i = i;
	    be.opcode = BackwardEntryOpCode.NORMALCYCLICCOMP;
	    be.p = c[i];
	    be.q = d[i];
	    // x[i] = x[i] - c[i]*x[ip1] - d[i] * x[nm1];
	}
	return true;
    }


    // fast case, but may fail.
    private static boolean solveCyclicFast(double[]x,
					   double[] a, double[]b, double[] c,
					   double[] d, int n)
    {
	if (b[0] == 0) return false;

	BackwardEntry be;
	ForwardEntry fe;

	int i;
	int nm1 = n-1;
	int nm2 = n-2;
	double cj = c[nm1];
	d[nm1] = b[nm1];
	d[nm2] = c[nm2];
	d[0] = a[0];
	c[0] = c[0] / b[0];
	d[0] = a[0] / b[0];
	x[0] = x[0] / b[0];
	double tmp;
	for (i = 1; i < nm2; i++) {
	    x[nm1] -= x[i-1]*cj;
	    d[nm1] -= d[i-1]*cj;
	    cj = -cj * c[i-1];
	    tmp = (b[i] - a[i] * c[i-1]);
	    if (tmp == 0.0) return false;
	    c[i] = c[i] / tmp;
	    x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	    d[i] = -d[i-1]*a[i] / tmp;
	}
	d[nm1] -= cj*d[i-1];
	x[nm1] -= cj * x[i-1];
	cj = a[nm1] - cj * c[i-1];
	tmp = (b[i] - a[i] * c[i-1]);
	if (tmp == 0.0) return false;
	x[i] = (x[i] - x[i-1] * a[i]) / tmp;
	d[i] = (d[i] -  d[i-1]*a[i]) / tmp;
	i++;
	if (i != nm1) throw new RuntimeException
			  (errorMsg("tridiagonalIter"));
	tmp = (d[i] - cj * d[i-1]);
	if (tmp == 0.0) return false;
	x[i] = (x[i] - x[i-1] * cj) / tmp;
	d[i] = (d[i] - d[i-1] * cj)/ tmp;
	if (--i >= 0) {
	    x[i] = x[i] - d[i] * x[nm1];
	}
	while (--i >= 0) {
	    int ip1 = i+1;
	    x[i] = x[i] - c[i]*x[ip1] - d[i] * x[nm1];
	}
	return true;
    }

    private static void solveCyclicSlow(double[]x,
				 double[]a, double[]b, double[]c,
				 int n)
    {
	double[][] matrix = new double[n][n];
	int nm1 = n-1;
	matrix[0][0] = b[0];
	matrix[0][1] = c[0];
	matrix[0][nm1] = a[0];
	matrix[nm1][0] = c[nm1];
	matrix[nm1][nm1] = b[nm1];
	matrix[nm1][n-2] = a[nm1];

	for (int i = 1; i < nm1; i++) {
	    matrix[i][i-1] = a[i];
	    matrix[i][i] = b[i];
	    matrix[i][i+1] = c[i];
	}

	LUDecomp lud = new LUDecomp(matrix);
	lud.solve(x, x);
    }
}

//  LocalWords:  exbundle Bezier tridiagonal blockquote pre yn bn xn
//  LocalWords:  ot href subdiagonal superdiagonal cn lt cp tmp nm cj
//  LocalWords:  IllegalArgumentException TridiagonalSolver xip ip xk
//  LocalWords:  linearlyDependent det xkp argArrayTooShort
//  LocalWords:  solveCyclicFast needAtLeast tridiagonalIter
