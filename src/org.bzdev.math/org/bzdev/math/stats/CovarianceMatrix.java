package org.bzdev.math.stats;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Covariance matrix.
 * This class provides the covariance matrix for a vector of measurements
 * or random variables measured or evaluated multiple times. The inner
 * classes are subclasses of this class:
 * <UL>
 *   <LI> <code>Sample</code> is used to compute the covariance matrix for
 *        the case in which the dataset whose covariance matrix is computed is a
 *        representative sample.
 *   <LI> <code>Population</code> is used to compute the covariance matrix for
 *        the case where the dataset represents every case being computed.
 * </UL>
 * <P>
 * For a row vector X of measurements or random values, the covariance
 * matrix &Sigma; is defined by
 *<BLOCKQUOTE>
 * &Sigma; = E[(X-E[X])(X - E[X])<sup>T</sup>]
 *</BLOCKQUOTE>
 * where E[X] is the expectation value of X.
 */
public abstract class CovarianceMatrix {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    protected int n;
    protected long count = 0;
    protected double[] means = null;
    protected double[] smeans = null;
    protected double[] emeans = null;
    protected double[][] matrix = null;
    protected double[] deltas = null;
    boolean completed = false;

    /**
     * The elements of the variance array that is computed
     * will be scaled by a correction factor. This factor should
     * be 1.0 when the dataset represents a total population. For
     * a sample of a population, the factor is m/(m-1) where m
     * is the sample size.
     * <P>
     * The subclasses {@link CovarianceMatrix.Sample} and
     * {@link CovarianceMatrix.Population} provide suitable
     * implementations of this method for those cases.
     * @return the correction factor; Double.NaN if the factor
     *         cannot be computed due to too few entries
     */
    protected abstract double getCorrection();

    /**
     * Add a new vector of values.
     * The vector's length must be at least n, where n is the
     * number of variables.
     * @param values a vector of values (the first n entries will
     *        be used)
     * @exception IllegalStateException method may no longer be called
     * @exception IllegalArgumentException the array length was incorrect
     */
    public void add(double[] values)
	throws IllegalArgumentException, IllegalStateException
    {
	if (completed) {
	    throw new IllegalStateException(errorMsg("addsComplete"));
	}
	if (values.length < n) {
	    throw new IllegalArgumentException
		(errorMsg("arrayTooShort", values.length, n));
	}
	long oldcount = count;
	count++;
	if (smeans != null) {
	    for (int i = 0; i < n; i++) {
		double incr = (values[i] - means[i]) / count;
		means[i] += incr;
		double delta = ((values[i] - smeans[i]) - emeans[i]) /count;
		emeans[i] += delta;
		deltas[i] = delta;
	    }
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j <= i; j++) {
		    matrix[i][j] += oldcount*deltas[i]*deltas[j]
			- matrix[i][j] /  count;
		}
	    }
	} else {
	    for (int i = 0; i < n; i++) {
		double incr = (values[i] - means[i]) / count;
		means[i] += incr;
		deltas[i] = incr;
	    }
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j <= i; j++) {
		    matrix[i][j] += oldcount*deltas[i]*deltas[j]
			- matrix[i][j] /  count;
		}
	    }
	}
    }

    /**
     * Assert that more values will not be added to the covariance-matrix
     * computation.
     * Calling this method results in the method {@link #getMatrix()}
     * returning the same array repeatedly instead of a fresh copy.
     * @see #CovarianceMatrix(double[][],int)
     */
    public void addsComplete() {
	
	completed = true;
    }

    /**
     * Get the covariance matrix.
     * The value returned must not be modified when this method is
     * called after {@link #addsComplete()} has been called.
     * @return the covariance matrix
     * @exception IllegalStateException no data is yet available
     * @exception IllegalArgumentException the array length was incorrect
     */
    public double[][] getMatrix()
	throws IllegalArgumentException, IllegalStateException
    {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	double[][] array = completed? matrix: new double[n][n];
	if (deltas != null) {
	    // double correction = count / (count - 1.0);
	    double correction = getCorrection();
	    if (Double.isNaN(correction)) {
		throw new IllegalStateException
		    (errorMsg("datasetTooSmall", count));
	    }
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j <= i; j++) {
		    array[i][j] = correction * matrix[i][j];
		}
	    }
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < i; j++) {
		    array[j][i] = array[i][j];
		}
	    }
	    if (completed) deltas = null;
	}
	return array;
    }

    /**
     * Get the correlation matrix corresponding to this covariance
     * matrix, storing the result in an array.
     * @param result an array to store the result.
     */
    public void  getCorrelationMatrix(double[][] result) {
	double[][] covMatrix = getMatrix();
	double[] diagonalsr = new double[n];
	for (int i = 0; i < n; i++) {
	    diagonalsr[i] = Math.sqrt(covMatrix[i][i]);
	}
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		result[i][j] = (i == j)? 1:
		    covMatrix[i][j]/(diagonalsr[i]*diagonalsr[j]);
	    }
	}
    }

    /**
     * Get the correlation matrix corresponding to this covariance
     * matrix.
     * Each call will produce a new matrix.
     * @return the correlation matrix
     */
    public double[][] getCorrelationMatrix() {
	double[][] cmatrix = new double[n][n];
	getCorrelationMatrix(cmatrix);
	return cmatrix;
    }

    /**
     * Get the mean values of the variables associated with this
     * covariance matrix.
     * The value returned must not be modified when this method is
     * called after {@link #addsComplete()} has been called.
     * @return the mean values
     * @exception IllegalStateException no data is yet available
     */
    public double[] getMeans() throws IllegalStateException
    {
	if (count == 0) {
	    throw new IllegalStateException(errorMsg("noData"));
	}
	if (completed) {
	    return means;
	} else {
	    return means.clone();
	}
    }

    /**
     * Get the number of rows and colums for this covariance matrix.
     * The covariance matrix is an N by N matrix, where N is the
     * number of variables associated with the matrix. N is set when
     * the constructor is called.
     * @return the number of rows and columns
     */
    public int getDimension() {
	return n;
    }

    /**
     * Constructor.
     * The method {@link #add(double[])} must be called repeatedly to
     * provide the dataset used to compute the covariance matrix and mean
     * array.  
     * @param n the number of variables
     */
    protected CovarianceMatrix(int n) {
	this.n = n;
	means = new double[n];
	matrix = new double[n][n];
	deltas = new double[n];
    }


    /**
     * Constructor given an array containing the initial data
     * used to compute a covariance matrix.
     * Note: the initial dataset will be used to estimate the mean
     * for purposes of reducing floating-point errors. More
     * dataset can be added using the {@link #add(double[])} method.
     * @param arrays an array, each component of which
     *        is an array containing the values for n
     *        variables
     * @param n the number of variables
     * @exception IllegalArgumentException the array length was incorrect
     */
    protected CovarianceMatrix(double[][]arrays, int n)
	throws IllegalArgumentException
    {
	this.n = n;
	means = new double[n];
	matrix = new double[n][n];
	this.completed = completed;
	for (int k = 0; k < arrays.length; k++) {
	    count++;
	    for (int i = 0; i < n; i++) {
		double y = (arrays[k][i] - means[i])/count;
		means[i] += y;
	    }
	}

	smeans = means.clone();
	emeans = new double[n];
	double mean[] = new double[n];
	deltas = new double[n];
	count = 0;
	for (int k = 0; k < arrays.length; k++) {
	    if (arrays[k].length < n) {
		throw new IllegalArgumentException
		    (errorMsg("subarrayTooShort", k, arrays[k].length, n));
	    }
	    count++;
	    for (int i = 0; i < n; i++) {
		double incr = ((arrays[k][i] - smeans[i]) - emeans[i])/count;
		emeans[i] += incr;
		deltas[i] = incr;
	    }
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j <= i; j++) {
		    matrix[i][j] += 
			k*deltas[i]*deltas[j] - matrix[i][j]/count;
		}
	    }
	}
    }

    /**
     * Class to compute a sample covariance matrix.
     */
    public static final class Sample extends CovarianceMatrix {

	@Override
	protected double getCorrection() {
	    if (count == 1) return Double.NaN;
	    return count / (count - 1.0);
	}

	/**
	 * Constructor.
	 * The method {@link #add(double[])} must be called repeatedly to
	 * provide the dataset used to compute the covariance matrix and mean
	 * array.  
	 * @param n the number of variables
	 */
	public Sample(int n) {
	    super(n);
	}


	/**
	 * Constructor given an array containing the initial dataset
	 * used to compute a covariance matrix. This dataset can be extended
	 * using the {@link #add(double[])} method.
	 * @param arrays an array, each component of which
	 *        is an array containing the values for n
	 *        variables
	 * @param n the number of variables
	 */
	public Sample(double[][] arrays, int n) {
	    super(arrays, n);
	}
    }

    /**
     * Class to compute the covariance matrix for a total set of
     * values, as opposed to a sample of values.
     */
    public static final class Population extends CovarianceMatrix {

	@Override
	protected double getCorrection() {
	    return 1.0;
	}

	/**
	 * Constructor.
	 * The method {@link #add(double[])} must be called repeatedly to
	 * provide the dataset used to compute the covariance matrix and mean
	 * array.  
	 * @param n the number of variables
	 */
	public Population(int n) {
	    super(n);
	}


	/**
	 * Constructor given an array containing the initial dataset
	 * used to compute a covariance matrix. This dataset can be extended
	 * using the {@link #add(double[])} method.
	 * @param arrays an array, each component of which
	 *        is an array containing the values for n
	 *        variables
	 * @param n the number of variables
	 */
	public Population(double[][] arrays, int n) {
	    super(arrays, n);
	}
    }
}

//  LocalWords:  exbundle dataset NaN addsComplete arrayTooShort
//  LocalWords:  getMatrix CovarianceMatrix noData datasetTooSmall
//  LocalWords:  subarrayTooShort IllegalStateException
//  LocalWords:  IllegalArgumentException
