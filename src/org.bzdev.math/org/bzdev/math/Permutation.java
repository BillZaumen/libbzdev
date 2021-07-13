package org.bzdev.math;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Permutation class.
 * A permutation (as supported by this class) is a one-to-one mapping of the
 * set of integers [0,n-1] onto itself. It can be represented by the
 * notation (drawn so that the parentheses cover both lines)
 * <blockquote><pre>
 *    0   1   2  ...   n-1
 * (                        )
 *   p_1 p_2 p_3 ... p_{n-1}
 * </pre></blockquote>
 * where the top line gives values in the mapping's domain and the bottom
 * line gives the corresponding value in the mapping's range.  This is
 * sometimes abbreviated by providing just the bottom line values separated
 * by commas.  When applied to a vector v1, the result is a vector v2 of the
 * same size whose value v2[i] = v1[p_i] where p_i is the value in the
 * permutation's range corresponding to a value i in the permutation's domain.
 * <P>
 * The permutation itself can be defined by a vector providing the values
 * <blockquote><pre>
 * p_1, p_2, p_3, ... p_{n-1}
 * </pre></blockquote>
 * or by providing the permutation's cycles.  The cycle notation
 * represents a permutation as the concatenation of cyclic
 * permutations.  Each cycle is defined as a list of numbers delimited
 * by parentheses and separated by spaces. For each adjacent pair, and
 * for the implicit pair consisting of the last element followed by
 * the first, the first (or left-hand) element in the pair corresponds
 * to an entry in the top line using two-line notation and the second
 * (or right-hand) element corresponds to the matching entry in the bottom
 * line.  Thus  (2 3 4) denotes the permutation (for n = 8) given by
 * <blockquote><pre>
 *    0 1 2 3 4 5 6 7 8
 * (                    )
 *    0 1 3 4 2 5 6 7 8
  * </pre></blockquote>
 * <P>
 * A permutation matrix is the matrix produced by applying a
 * permutation to the rows of the identity matrix. The resulting
 * matrix has precisely one '1' in each row and column with the other
 * elements equal to zero.  The matrix product PA thus applies the
 * permutation to the rows of A and the matrix product AP applies the
 * permutation's inverse to the columns of A.
 */
public class Permutation {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private int[] permutation;
    private boolean even = true;

    /**
     * Get the vector that defines a permutation.
     * @return the vector defining this permutation
     */
    public int[] getVector() {return (int[]) (permutation.clone());}

    /**
     * Get a permutation's cycles
     * An array defining a cycle contains the cycle in the same order as
     * in cycle notation.
     * @return an array of integer arrays, each of which defines a cycle
     */
    public int[][] getCycles() {
	int ind = 0;
	int tind = 0;
	int acount = 0;
	int n = permutation.length;
	boolean[] visited = new boolean[n];
	int[] tmp = new int[permutation.length];
	int[] len = new int[permutation.length];
	while (ind < n) {
	    if (!visited[ind]) {
		int subcount = 1;
		tmp[tind++]= ind;
		visited[ind] = true;
		int pind = permutation[ind];
		while (pind != ind) {
		    tmp[tind++] = pind;
		    visited[pind] = true;
		    subcount++;
		    pind = permutation[pind];
		}
		if (subcount > 1) {
		    len[acount++] = subcount;
		} else {
		    tind--;
		}
	    }
	    ind++;
	}
	int count = 0;
	int[][] result = new int[acount][];
	for (int i = 0; i < acount; i++) {
	    int m = len[i];
	    int[] cycle = new int[m];
	    result[i] = cycle;
	    for (int j = 0; j < m; j++) {
		cycle[j] = tmp[count++];
	    }
	}
	java.util.Arrays.sort(result, new java.util.Comparator<int[]>() {
		public int compare(int[] array1, int[] array2) {
		    if (array1.length > array2.length) return -1;
		    else if (array1.length < array2.length) return 1;
		    else return (array1[0] - array2[0]);
		}
		public boolean equals(Object obj) {
		    return (this == obj);
		}
	    });
	return result;
    }

    /**
     * Get the determinate of the permutation's matrix.
     * @return the determinate
     */
    public double det() {return even? 1.0: -1.0;}

    /**
     * Get the size of the permutation.
     * This is the number of integers over which the permutation is defined.
     * @return the size of the permutation
     */
    public int getSize() {return permutation.length;}

    /**
     * Get a permutation's matrix.
     * The matrix is defined so that PV, where P is the permutation matrix
     * and V is vector, returns a vector whose values for an index i are
     * the value of V at an index equal to the value to which the permutation
     * maps the index i. I.e., the value of PV for index i is equal to
     * V[pvector[i]] where pvector is the permutation's vector.
     * @return the matrix
     */
    public double[][] getMatrix() {
	double[][] matrix = new double[permutation.length][permutation.length];
	for (int i = 0; i < permutation.length; i++) {
	    matrix[i][permutation[i]] = 1.0;
	}
	return matrix;
    }

    // used internally only.
    Permutation(int[] vector, boolean even) {
	permutation = vector;
	this.even = even;
    }

    /**
     * Constructor for an identity permutation
     * @param n the number of integers
     */
    public Permutation(int n) {
	permutation = new int[n];
	for (int i = 0; i < n; i++) {
	    permutation[i] = i;
	}
    }

    /**
     * Constructor.
     * The values of the vector provided as an argument correspond to the
     * bottom line in two-line notation.
     * @param vector an array listing the permuted values of the integers
     *        [0,n), where n is the length of the vector
     */
    public Permutation(int[] vector) {
	permutation = (int[]) (vector.clone());
	int n = permutation.length;
	boolean[] exists = new boolean[n];
	for (int i = 0; i < n; i++) {
	    if ((permutation[i] < 0) || (permutation[i] >= n))
		throw new IllegalArgumentException(errorMsg("vectorElement"));
	    if (exists[permutation[i]]) {
		throw new IllegalArgumentException(errorMsg("illFormedPerm"));
	    } else {
		exists[permutation[i]] = true;
	    }
	}
	int ind = 0;
	int count = 0;
	while (ind < n) {
	    if (exists[ind]) {
		exists[ind] = false;
		int pind = permutation[ind];
		while (pind != ind) {
		    exists[pind] = false;
		    count++;
		    pind = permutation[pind];
		}
	    }
	    ind++;
	}
	even = (count%2) == 0;
    }

    static int[] getVectorFromMatrix(int[][] matrix)
	throws IllegalArgumentException
    {
	int[] vector = new int[matrix.length];
	for (int j = 0; j < vector.length; j++) {
	    for (int i = 0; i < vector.length; i++) {
		if (vector.length != matrix[i].length) {
		    throw new IllegalArgumentException
			(errorMsg("needSquareMatrix"));
		}
		if (matrix[i][j] == 1) {
		    vector[j] = i;
		    break;
		}
	    }
	}
	return vector;
    }

    /**
     * Construct a permutation given its matrix.
     * @param matrix the permutation matrix
     */
    public Permutation(int[][] matrix) {
	this(getVectorFromMatrix(matrix));
    }

    /**
     * Constructor given a permutation's cycles
     * The permutation's domain is the set of integers [0, n), and the
     * integer arrays denoting each cycle contain the values of the cycle
     * in the same order as in cycle notation.  Thus, (2 4 5) could be
     * represented by an integer array with an initializer {2, 4, 5}.
     * @param cycles an array of a permutation's cycles, each
     *        element of which is an integer array representing a cycle
     * @param n the number of integers in the permutation
     */
    public Permutation(int[][] cycles, int n) {
	permutation = new int[n];
	boolean[] exists = new boolean[n];
	for (int i = 0; i < n; i++) {
	    permutation[i] = i;
	}
	int count = 0;
	for (int i = 0; i < cycles.length; i++) {
	    int[] cycle = cycles[i];
	    int m = cycle.length;
	    if (m > n)
		throw new IllegalArgumentException(errorMsg("cycleTooLong"));
	    count += (m > 1)? (m-1): 0;
	    m -= 1;
	    for (int j = 0; j < m; j++) {
		if (cycle[j] > n || cycle[j] < 0)
		    throw new IllegalArgumentException(errorMsg("badCycle"));
		permutation[cycle[j]] = cycle[j+1];
	    }
	    permutation[cycle[m]] = cycle[0];
	}
	for (int i = 0; i < n; i++) {
	    if (exists[permutation[i]]) {
		throw new IllegalArgumentException(errorMsg("illFormedPerm"));
	    } else {
		exists[permutation[i]] = true;
	    }
	}
	even = (count%2 == 0);
    }

    /**
     * Modify a permutation by applying a cyclic permutation whose cycle length
     * is 2.
     * The new permutation is the result of applying a permutation consisting
     * of the cycle (i j) to the old permutation. Thus (0 1) changes
     * <blockquote><pre>
     *    0 1 2 3 4 5 6 7 8
     * (                    )
     *    0 1 3 4 2 5 6 7 8
     * </pre></blockquote>
     * to
     * <blockquote><pre>
     *    0 1 2 3 4 5 6 7 8
     * (                    ).
     *    1 0 3 4 2 5 6 7 8
     * </pre></blockquote>
     * @param i the first index
     * @param j the second index
     */
    public void swap(int i, int j) {
	if (i == j) return;
	int ival = permutation[i];
	int jval = permutation[j];
	permutation[i] = jval;
	permutation[j] = ival;
	even = !even;
    }

    /**
     * Apply a permutation to a vector.
     * @param vector the vector to permute
     * @return a new vector containing the permuted values
     */
    public double[] applyTo(double[] vector) {
	if (vector.length != permutation.length) {
	    throw new IllegalArgumentException
		(errorMsg("wrongVectorLen"));
	}
	double[]result = new double[vector.length];
	for (int i = 0; i < vector.length; i++) {
	    result[i] = vector[permutation[i]];
	}
	return result;
    }

    /**
     * Applies a permutation to a vector and store the results.
     * The vectors src and dest can be identical.
     * Given the permutation's vector pvector, after the call
     * completes, dest[i] = src[pvector[i]] for i in [0,n).
     * @param src the vector to permute
     * @param dest a vector that will contain the permuted values
     */
    public void applyTo(double[] src, double[] dest) {
	if (src.length != permutation.length && 
	    dest.length != permutation.length)
	    {
		throw new IllegalArgumentException
		    (errorMsg("wrongVectorLen"));
	    }
	if (src != dest) {
	    for (int i = 0; i < src.length; i++) {
		dest[i] = src[permutation[i]];
	    }
	} else {
	    double[] tmp = new double[src.length];
	    for (int i = 0; i < src.length; i++) {
		tmp[i] = src[permutation[i]];
	    }
	    System.arraycopy(tmp, 0, dest, 0, tmp.length);
	}
	
    }


    /**
     * Apply a permutation to a matrix.
     * The rows of the matrix are permuted, not the columns. If P
     * is the permutation's matrix, the result is the matrix product
     * PA.
     * @param A the matrix to permute
     * @return a new matrix containing the permuted values
     * @see #applyTo(double[][],double[][])
     */
    public double[][] applyTo(double[][] A) {
	int n = permutation.length;
	if (n != A.length) 
	    throw new IllegalArgumentException(errorMsg("arraySizeForPerm"));
	double[][] result = new double[n][];
	for (int i = 0; i < n; i++) {
	    double[] row = new double[A[i].length];
	    result[i] = row;
	}
	applyTo(A, result);
	return result;
    }

    /**
     * Applies a permutation to a matrix and store the results.
     * Rows are permuted, not columns.
     * The matrices src and dest can be identical.  If P is the
     * permutation's matrix, D is the destination, and S is the
     * source, then after the call completes, D = PS. I.e.,
     * given the permutation's vector pvector, after the call
     * completes, dest[i] = src[pvector[i]] for i in [0,n).
     * @param src the vector to permute
     * @param dest a vector that will contain the permuted values
     * @exception IllegalArgumentException arguments not compatible
     * @exception NullPointerException an argument was null
     */
    public void applyTo(double[][]src, double[][] dest)
	throws IllegalArgumentException
    {
	int n = permutation.length;
	if (src == null || dest == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (n != src.length || n != dest.length)
	    throw new IllegalArgumentException
		(errorMsg("arraySizeForPerm"));
	if (src != dest) {
	    for (int i = 0; i < n; i++) {
		double[] srow = src[permutation[i]];
		double[] drow = dest[i];
		if (srow.length != drow.length) {
		    // System.out.println("src and dest arrays not compatible");
		    throw new IllegalArgumentException
			(errorMsg("incompatibleArrays"));
		}
		System.arraycopy(srow, 0, drow, 0, srow.length);
	    }
	} else {
	    double[][] tmp = new double[n][];
	    for (int i = 0; i < n; i++) {
		tmp[i] = src[permutation[i]];
	    }
	    System.arraycopy(tmp, 0, dest, 0, n);
	}
    }

    /**
    /**
     * Left-multiply a permutation's matrix by a matrix A.
     * If P is the permutation's matrix and given the argument A,
     * the resulting matrix is AP. The result is the inverse permuation
     * applied to A's columns.
     * The implementation uses the fact that
     * AP = ((AP)<sup>T</sup>)<sup>T</sup>
     * = (P<sup>T</sup>A<sup>T</sup>)<sup>T</sup>
     * = (P<sup>-1</sup>A<sup>T</sup>)<sup>T</sup>
     * If the dimensions of A and result are n by m, the dimensions of aT
     * must be m by n, and regardless, it must be true that n &gt; 0 and
     * m &gt; 0.
     * @param A the matrix to permute
     * @param result the matrix AP, which must have the same dimensions as A
     * @param aT a work area sized to store the transpose of A, whose dimensions
     *        are those for A's transpose.
     */
    public void leftMultiplyBy(double[][]A, double[][]result, double[][]aT) {
	// set aT to the transpose of A
	for (int i = 0; i < aT.length; i++) {
	    for (int j = 0; j < aT[i].length; j++) {
		aT[i][j] = A[j][i];
	    }
	}
	inverse().applyTo(aT, aT);
	// now set the result to the transpose of aT, which was modified
	// in place.
	for (int i = 0; i < result.length; i++) {
	    for (int j = 0; j < result[i].length; j++) {
		result[i][j] = aT[j][i];
	    }
	}
    }

    /**
     * Left-multiply this permutation's matrix by a matrix A and
     * return the results as a newly allocated matrix.
     * This applies the inverse permutation to the columns of A.
     * Permute the columns of a matrix, returning the new matrix.
     * If P is the permutation's matrix and given the argument A,
     * the resulting matrix is AP.
     * The implementation uses the fact that
     * AP = ((AP)<sup>T</sup>)<sup>T</sup>
     * = (P<sup>T</sup>A<sup>T</sup>)<sup>T</sup>
     * = (P<sup>-1</sup>A<sup>T</sup>)<sup>T</sup>
     * @param A the matrix to permute
     * @return a matrix with permuted columns
     */
    public double[][] leftMultiplyBy(double[][]A) {

	double[][] aT = new double[A[0].length][];
	for (int i = 0; i < A[0].length; i++) {
	    aT[i] = new double[A.length];
	}
	double[][] result = new double[A.length][];
	for (int i = 0; i < A.length; i++) {
	    result[i] = new double[A[i].length];
	}
	leftMultiplyBy(A, result, aT);
	return result;
    }

    /**
     * Apply one permutation to another.
     * The permutation vector of the argument is permuted by this
     * permutation and that permuted vector is used to construct
     * a new permutation.
     * @param p a permutation
     * @return a new permutation such that its vector is
     *         this permutation of p's vector
     */
    public Permutation applyTo(Permutation p) {
	if (permutation.length != p.getSize())
	    throw new IllegalArgumentException(errorMsg("incompatiblePerms"));
	int[] result = new int[permutation.length];
	int n = permutation.length;
	for (int i = 0; i < n; i++) {
	    result[i] = p.permutation[permutation[i]];
	}
	// private constructor - this is faster because we can
	// compute whether the new permutation is even or odd
	// trivially given the existing permutations.
	return new Permutation(result, (p.even == even));
    }

    /**
     * Get the inverse permutation.
     * The inverse has the property that for a permutation p
     * and a vector v, v equals
     * <blockquote>
     * p.inverse().applyTo(p.applyTo(v)).
     * </blockquote>
     * @return the inverse permutation
     */
    public Permutation inverse() {
	int[] result = new int[permutation.length];
	for(int i = 0; i < permutation.length; i++) {
	    result[permutation[i]] = i;
	}
	return new Permutation(result, even);
    }
}

//  LocalWords:  exbundle blockquote pre mapping's PV pvector src aT
//  LocalWords:  vectorElement illFormedPerm needSquareMatrix dest
//  LocalWords:  initializer cycleTooLong badCycle wrongVectorLen p's
//  LocalWords:  applyTo arraySizeForPerm IllegalArgumentException
//  LocalWords:  NullPointerException nullArg incompatibleArrays
//  LocalWords:  permuation incompatiblePerms
