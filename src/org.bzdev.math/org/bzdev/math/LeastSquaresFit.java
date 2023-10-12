package org.bzdev.math;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.stats.Statistic;
import org.bzdev.math.stats.ProbDistribution;
import org.bzdev.math.stats.ChiSquareDistr;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Class representing common operations for least square fits.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * A collection of data points y<sub>i</sub>, each corresponding to a
 * value of an argument x, x<sub>i</sub> is represented by some function
 * y(x; a), where x is the function's argument and 'a' is a vector of
 * parameters.  A least-squares fit minimizes the quantity
 * $$\chi^2 = \sum_i \frac{[y_i - y(x_i; a)]^2}{\sigma_i^2}$$
 * <NOSCRIPT><blockquote>
 *   &chi;<sup>2</sup> &equiv; &sum;<sub>i</sub>[y<sub>i</sub>-y(x<sub>i</sub>; a)]<sup>2</sup>/&sigma;<sub>i</sub><sup>2</sup>.
 * </blockquote></NOSCRIPT>
 * where &sigma;<sub>i</sub> is the standard deviation for y<sub>i</sub>.
 * Errors in the variable x are assumed to be negligible.
 * <P>
 * A basic assumption is that each y<sub>i</sub> is the value of a random
 * variable whose standard deviation is &sigma;<sup>i</sup>. If we were to
 * repeat the fit using a different set of values of these random variables,
 * we will, as expected, get a different set of parameters.  Naturally, one
 * would like to know the variances or standard deviations for these sets
 * of parameters.  This is provided by a covariance matrix, which gives
 * the variances of the parameters and the covariances between parameters,
 * as described in the documentation for {@link #getCovarianceMatrix()}.
 * <P>
 * Subclasses of LeastSquaresFit will typically compute the parameters
 * in a constructor.  After the least squares fit is constructed, one
 * may obtain the values of the function y(x; a) and its derivatives,
 * the parameters, the value of &chi;<sup>2</sup> for the fit, the
 * covariance matrix, and the
 * covariance of y computed at two values of x.
 * <P>
 * Note: since the class LeastSquaresFit extends RealValuedFunction,
 * it can be used by {@link org.bzdev.geom.SplinePath2D} to provide
 * values for a smooth path for plotting.  For example,
 * <blockquote><pre>
 * import org.bzdev.math.*;
 * import org.bzdev.geom.*;
 * double x1 = 0.0;
 * double x2 = 10.0;
 * LeastSquaresFit lsf = new ....;
 * int n = 20;
 * SplinePath2D path =
 *     new SplinePath2D(RealValuedFunction.xFunction, lsf, x1, x2, n);
 * </pre></blockquote>
 * The calling convention for the constructors and chiSquare methods
 * places x values before y values. This is the same convention used
 * by the CubicSpline and BSpline classes.
 * <P>
 * In addition, the inner classes for {@link LeastSquaresFit} fit implement
 * the {@link RealValuedFunction} methods
 * {@link RealValuedFunction#derivAt(double)} and
 * {@link RealValuedFunction#secondDerivAt(double)}, and all subclasses
 * should implement these methods as well. For those inner classes whose
 * constructs have arguments whose types are {@link RealValuedFunction}
 * or {@link RealValuedFunctionVA}, the real-valued functions supplied
 * should implement derivatives if the least squares fit's derivative
 * methods are used.
 */
public abstract class LeastSquaresFit extends RealValuedFunction {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private double[] parameters;
    private double chiSq;
    private double reducedChiSq;
    private int nu; 		// number of degrees of freedom.

    /**
     * Set the parameters array.
     * Subclasses must call this method, generally in a constructor.
     * @param parameters the array to use to store parameters
     */
    protected void setParameters(double[] parameters) {
	this.parameters = parameters;
    }

    /**
     * Get the parameters array.
     * This provides access to the parameters array.
     * @return parameters the array to use to store parameters
     */
    protected final double[] getParametersArray() {
	return parameters;
    }
    
    /**
     * Set the value of &chi;<sup>2</sup>.
     * @param chiSq the value for &chi;<sup>2</sup>
     */
    protected void setChiSquare(double chiSq) {
	this.chiSq = chiSq;
    }

    /**
     * Set the number of degrees of freedom.
     * @param degrees the number of degrees of freedom
     */
    protected void setDegreesOfFreedom(int degrees) {
	nu = degrees;
    }

    /**
     * Get the number of degrees of freedom for this least-squares fit.
     * @return the number of degrees of freedom.
     */
    public int getDegreesOfFreedom() {return nu;}

    /**
     * Set the value of &chi;<sup>2</sup><sub>reduced</sub>.
     * The reduced value of &chi;<sup>2</sup> is the value of
     * &chi;<sup>2</sup> divided by the number of degrees of freedom
     * (the number of points fitted minus the number of parameters).
     * @param reducedChiSq the value for &chi;<sup>2</sup><sub>reduced</sub>
     */
    protected void setReducedChiSquare(double reducedChiSq) {
	this.reducedChiSq = reducedChiSq;
    }

    /**
     * Get the number of parameters for this least-squares fit.
     * @return the number of parameters/parameters
     */
    public int getNumberOfParameters() {
	return parameters.length;
    }

    /**
     * Get the parameters.
     * @return a copy of the parameters array
     */
    public double[] getParameters() {
	return (double[]) parameters.clone();
    }

    /**
     * Get the parameters, providing an array in which to store them.
     * @param array an array to hold the parameters
     * @return a copy of the parameters array
     * @exception IllegalArgumentException the array dimensions were
     *            too small
     */
    public double[] getParameters(double[] array)
	throws IllegalArgumentException
    {
	if (array.length < parameters.length) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	System.arraycopy(parameters, 0, array, 0, parameters.length);
	return array;
    }


    /**
     * Get the &chi;<sup>2</sup> value for this least squares fit.
     * <P>
     * For the linear case, if standard deviations for the y values
     * are not known, the variance is assumed to be constant and
     * computed as the sum of squares divided by the number of degrees
     * of freedom. The result is that the &chi;<sup>2</sup> value
     * turns out to be the number of data points minus the number of
     * parameters (i.e., the number of degrees of freedom). The value
     * for this case will not be particularly useful.
     * @return the value of &chi;<sup>2</sup>.
     */
    public double getChiSquare() {
	return chiSq;
    }

    /**
     * Get the (chi-square) statistic
     * @return the statistic
     */
    public Statistic getStat() {
	return new Statistic() {
	    @Override
		public double getValue() {return chiSq;}
	    @Override
		public ProbDistribution getDistribution() {
		return new ChiSquareDistr(nu);
	    }
	};
    }

    /**
     * Get the reduced &chi;<sup>2</sup> value for this least squares fit.
     * This is the value of &chi;<sup>2</sup> divided by the number of
     * degrees of freedom (the number of data points used for the fit
     * minus the number of parameters).  The value will be about 1.0
     * for a good fit.  A much larger value typically means that the
     * model used is not a good match for the data, and a value much
     * smaller than 1.0 (but non-negative) indicates that the errors
     * assumed for the data are not accurate.
     * @return the reduced &chi;<sup>2</sup> value
     */
    public double getReducedChiSquare() {
	return reducedChiSq;
    }

    private double[][] covariance = null;

    /**
     * Set the covariance array.
     * The array is an n by n array, where n is the number of parameters.
     * Typically this method will be called by the method
     * {@link #createCovariance()} and nowhere else.
     * @param covariance the covariance array
     */
    protected void setCovariance(double[][] covariance) {
	this.covariance = covariance;
    }

    /**
     * Create the covariance array.
     * Subclasses must implement this method, which is expected to
     * set up the covariance array and then call
     * {@link #setCovariance(double[][])} to make it available.
     */
    protected abstract void createCovariance();

    /**
     * Get the covariance array for the parameters.
     * This method calls {@link #createCovariance()}.  As long as
     * subclasses use this method to obtain the covariance array,
     * they will not have to explicitly call {@link #createCovariance()}.
     * This method returns the covariance array, not a copy of the array.
     * @return the covariance array.
     */
    protected double[][] getCovarianceArray() {
	if (covariance == null) {
	    createCovariance();
	}
	return covariance;
    }

    /**
     * Get the covariance matrix for the parameters.
     * The covariance matrix C provides the variances (for the diagonal
     * elements), and the covariances for the off-diagonal elements.
     * For the diagonal elements, the values C[i][i] are the variance
     * for the i<sup>th</sup> parameter. Otherwise C[i][j] provides
     * the covariance between the i<sup>th</sup> and j<sup>th</sup>
     * parameter.
     * <P>
     * The value returned is a newly allocated matrix.
     * @return the covariance matrix.
     */
    public double[][] getCovarianceMatrix() {
	if (covariance == null) {
	    createCovariance();
	}
	double[][]result = new double[covariance.length][covariance.length];
	int n = covariance.length;
	for (int i = 0; i < n; i++) {
	    System.arraycopy(covariance[i],0, result[i],0, n);
	}
	return result;
    }

   /**
     * Get the covariance matrix, using an array provided by the caller
     * The value returned is a newly allocated matrix.  If the
     * array dimensions are larger than the number of parameters,
     * indices with values larger than or equal to the number of
     * parameters will be ignored.
     * @param array an array used to store the covariance values
     * @return the covariance matrix.
     * @exception IllegalArgumentException the array dimensions were
     *            too small
     */
    public double[][] getCovarianceMatrix(double[][] array)
	throws IllegalArgumentException
    {
	if (covariance == null) {
	    createCovariance();
	}
	if (array.length < covariance.length) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}

	int n = covariance.length;
	for (int i = 0; i < n; i++) {
	    if (array[i].length < covariance[i].length) {
		throw new
		    IllegalArgumentException(errorMsg("argArrayTooShort"));
	    }
	    System.arraycopy(covariance[i],0, array[i],0, n);
	}
	return array;
    }

    /**
     * Compute the covariance for the y values associated with two data points.
     * The covariance for two random variables X and Y is
     * E[(X-E[X](Y-E[Y])] where E[Z] is the expected value of a random
     * variable Z.  The covariance of X and X is simply the variance of X.
     * The value returned allows one to estimate the error in the fit at
     * any point, not just the data points, and whether errors at different
     * values of X are correlated.
     * @param x1 the x value for the first y value
     * @param x2 the x value for the second y value
     * @return the covariance
     */
    public abstract double covariance(double x1, double x2);

    /**
     * Compute the sum of the squares of the difference between a fit and
     * a set of data points.
     * <P>
     * Note: If the variance of the Y values are not known, they can be
     * estimated by first creating an instance of LeastSquares fit,
     * calling LeastSquaresFit.sumOfSquares(fit, x, y), and dividing the
     * value returned by this call by (n-m) where n is the length of the
     * array x and m is the number of parameters.  See
     * <A href="https://en.wikipedia.org/wiki/Ordinary_least_squares#Estimation">
     * "Ordinary least Squares" (Wikipedia)</A> for details.
     * @param fit the least-squares fit
     * @param x the X values
     * @param y the Y values
     * @return the sum of the squares of the distance between the
     *         fit and data point y for each value of x
     */
    public static double sumOfSquares(LeastSquaresFit fit, double[] x,
				      double[] y)
    {
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	int m = x.length;
	for (int i = 0; i < m; i++) {
	    double term = y[i] - fit.valueAt(x[i]);
	    term *= term;
	    double yy = term - state.c;
	    double t = state.total + yy;
	    state.c = (t - state.total) - yy;
	    state.total = t;
	}
	return state.total;
    }

    /**
     * Given a least squares fit, compute the value of &chi;<sup>2</sup>
     * for a set of data points in which the y values have the same
     * standard deviation and the x values are accurate.
     * Each data point is specified by an index into two arrays, one giving
     * the value of X and the other the value of Y for that point.
     * @param fit the least squares fit
     * @param x the X values for the data points
     * @param y the Y values for the data points
     * @param sigma the standard deviation for y
     * @return the value of &chi;<sup>2</sup>
     */
    public static double chiSquare(LeastSquaresFit fit, double[] x,
				    double[] y, double sigma)
    {
	double sigma2 = sigma*sigma;
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	int m = x.length;
	for (int i = 0; i < m; i++) {
	    double cy = fit.valueAt(x[i]);
	    double term = y[i] - fit.valueAt(x[i]);
	    if (sigma == 0.0) {
		double max1 = Math.abs(cy);
		double max2 = Math.abs(y[i]);
		double max = (max1 > max2)? max1: max2;
		// if term is zero to within floating-point accuracy limits,
		// assume an exact fit and make the term 0; otherwise
		// we get infinity due to division by zero.
		if (Math.abs(term)/max > 1.e-10) {
		    return Double.POSITIVE_INFINITY;
		} else {
		    term = 0.0;
		}
	    } else {
		term *= term;
		term /= sigma2;
	    }
	    double yy = term - state.c;
	    double t = state.total + yy;
	    state.c = (t - state.total) - yy;
	    state.total = t;
	}
	return state.total;
    }

    /**
     * Given a least squares fit, compute the value of &chi;<sup>2</sup>
     * for a set of data points in which the y values have a specified
     * standard deviation and the x values are accurate.
     * Each data point is specified by an index into two arrays, one giving
     * the value of X and the other the value of Y for that point.
     * The index also indicates which value to use in the sigma array to
     * determine the standard deviation for the corresponding y value.
     * @param fit the least squares fit
     * @param x the X values for the data points
     * @param y the Y values for the data points
     * @param sigma the standard deviations for the data points
     * @return the value of &chi;<sup>2</sup>
     */
    public static double chiSquare(LeastSquaresFit fit,
				    double[] x, double[] y, double[] sigma)
    {
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	int m = x.length;
	for (int i = 0; i < m; i++) {
	    double cy = fit.valueAt(x[i]);
	    double term = y[i] - cy;
	    if (sigma[i] == 0.0) {
		double max1 = Math.abs(cy);
		double max2 = Math.abs(y[i]);
		double max = (max1 > max2)? max1: max2;
		// if term is zero to within floating-point accuracy limits,
		// assume an exact fit and make the term 0; otherwise
		// we get infinity due to division by zero.
		if (Math.abs(term)/max > 1.e-10) {
		    return Double.POSITIVE_INFINITY;
		} else {
		    term = 0.0;
		}
	    } else {
		term *= term;
		term /= sigma[i]*sigma[i];
	    }
	    double yy = term - state.c;
	    double t = state.total + yy;
	    state.c = (t - state.total) - yy;
	    state.total = t;
	}
	return state.total;
    }

    /**
     * Get the real valued function associated with this least
     * squares fit but with altered parameters.
     * <P>
     * This method is useful for testing (varying the parameters should
     * result in a worse fit), but it can also be used in Monte-Carlo
     * models: Given the parameters and covariance matrix, one can use
     * the class {@link org.bzdev.math.rv.GaussianRVs} to generate parameters
     * with the same mean value and covariance metrix. The real-valued
     * functions corresponding to these parameters can then be used in
     * Monte-Carlo simulations or models.
     * @param parameters the parameters.
     * @return a real-valued function using the specified parameters
     * @throws IllegalArgumentException the argument has the wrong length
     */
    public RealValuedFunction getFunction(double[] parameters)
	throws IllegalArgumentException
    {
	if (parameters.length != getNumberOfParameters()) {
	    String msg = errorMsg("unexpectedArrayLen", parameters.length);
	    throw new IllegalArgumentException(msg);
	}
	final LeastSquaresFit fit = getFit();
	fit.setChiSquare(getChiSquare());
	fit.setDegreesOfFreedom(getDegreesOfFreedom());
	fit.setReducedChiSquare(getReducedChiSquare());
	fit.setParameters(parameters);
	return new RealValuedFunction() {
	    @Override
	    public double valueAt(double z) {
		return fit.valueAt(z);
	    }

	    @Override
	    public double derivAt(double z)
		throws UnsupportedOperationException
	    {
		return fit.derivAt(z);
	    }

	    @Override
	    public double secondDerivAt(double z)
		throws UnsupportedOperationException
	    {
		return fit.secondDerivAt(z);
	    }
	};
    }

    /**
     * Get a copy of this least squares fit but without its
     * parameters, degrees of freedom, chi square value, reduced
     * chi square value, or covariance set.
     * <P>
     * This method is used by {@link #getFunction(double[])}, which only
     * provides a real-valued function. The only methods that will be
     * called on the value returned are
     * {@link #setChiSquare(double)}, {@link #setDegreesOfFreedom(int)},
     * {@link #setReducedChiSquare(double)},
     * {@link #setParameters(double[])},
     * {@link #valueAt(double)}, {@link #derivAt(double)}, and
     * {@link #secondDerivAt(double)}.
     * @return a least squares fit
     */
    protected abstract LeastSquaresFit getFit();

    /**
     * Class for linear least-squares fit.
     * <P>
     * <script>
     * MathJax = {
     *	  tex: {
     *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
     *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
     * };
     * </script>
     * <script id="MathJax-script" async
     *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
     * </script>
     * For a description of the algorithms used, see 
     * <A href="http://ipnpr.jpl.nasa.gov/progress_report/42-122/122E.pdf">
     * P. H. Richter, "Estimating Errors in Least-Squares Fitting",
     * TDA Progress Report 42-122, JPL, 1995.</A>
     * <P>
     * Given a set of functions $X_j$ and corefficients $a_j$,
     * a linear least squares fit fits a function
     * $y(x; a) = \sum_j a_jX_j(x)$
     * <!--y(x; a) &equiv; &sum;<sub>j</sub> a<sub>j</sub>X<sub>j</sub>(x)-->
     * to a set of values y<sub>i</sub> corresponding to a value of
     * x given by x<sub>i</sub>.  The function y(x;a) is a linear function
     * of the parameters a but not necessarily of the variable x. Each
     * value y<sub>i</sub> is a random variable with a standard deviation
     * given by &sigma;<sub>i</sub>.
     */
    public abstract static class Linear extends LeastSquaresFit {
	private double[][] H = null;
	private TriangularDecomp decomp = null;
	private double variance = -1.0;
	
	/**
	 * Set the variance.
	 * The variance should be set to zero in cases where the
	 * covariance matrix should be set to zero.
	 * Otherwise, it should be set to a variance by which the
	 * computed covariances will be multiplied.
	 * This is intended for cases where the covariance is computed
	 * as if sigma had the value of 1.0 (some subclasses do this
	 * when all the values of sigma are the same).
	 * @param value the value of the variance, which must not be
	 *        negative.
	 */
	protected void setVariance(double value) {
	    variance = value;
	}

	/**
	 * Set the TriangularDecomp object for this least squares fit by
	 * providing the matrix to decompose.
	 * This is used to obtain the variance.  The description in the
	 * reference
	 * <A href="http://ipnpr.jpl.nasa.gov/progress_report/42-122/122E.pdf">
	 * P. H. Richter, "Estimating Errors in Least-Squares Fitting",
	 * TDA Progress Report 42-122, JPL, 1995, page 116</A>
	 * defines a matrix H = &equiv; A<sup>T</sup>A, where
	 * A<sub>ij</sub> = X<sub>ij</sub>/&sigma;<sub>i</sub> and
	 * X<sub>ij</sub> is the value of the basis function indexed by j
	 * for the data point indexed by i. This method stores the matrix
	 * H (by reference) and will create an instance of CholeskyDecomp
	 * when needed to compute the inverse of H when needed (the inverse
	 * of H is the covariance matrix).
	 * @param H the matrix to decompose
	 */
	protected void setDecomp(double[][] H) {
	    this.H = H;
	    decomp = null;
	}

	/**
	 * Set the TriangularDecomp object for this least squares fit.
	 * This is used to obtain the variance.  The description in the
	 * reference
	 * <A href="http://ipnpr.jpl.nasa.gov/progress_report/42-122/122E.pdf">
	 * P. H. Richter, "Estimating Errors in Least-Squares Fitting",
	 * TDA Progress Report 42-122, JPL, 1995, page 116</A>
	 * defines a matrix H = &equiv; A<sup>T</sup>A, where
	 * A<sub>ij</sub> = X<sub>ij</sub>/&sigma;<sub>i</sub> and
	 * X<sub>ij</sub> is the value of the basis function indexed by j
	 * for the data point indexed by i. The caller will call the
	 * constructor of one of TriangularDecomp's subclasses using H
	 * as its argument. Of the existing choices, CholeskyDecomp is
	 * the preferred one to use.  The classes the BZDev library
	 * provides have constructors that take one or two arguments:
	 * for example, <code>new CholeskyDecomp(H)</code> or
	 * <code>new CholeskyDecomp(H,H)</code> (the later if H is not
	 * further used). One a TriangularDecomp object is created,
	 * one then calls this method to make the CholeskyDecomp
	 * object available to this class: H is the inverse of the
	 * covariance matrix.
	 * @param value the TriangularDecomp instance to use
	 */
	protected void setDecomp(TriangularDecomp value) {
	    decomp = value;
	    H = null;
	}

	/**
	 * Get the TriangularDecomp object for this least squares fit.
	 * @return the trianbular decomposition
	 */
	protected TriangularDecomp getDecomp() {
	    if (decomp == null) {
		if (H != null) {
		    decomp = new CholeskyDecomp(H);
		    H = null;
		}
	    }
	    return decomp;
	}


	@Override
	protected void createCovariance() {
	    TriangularDecomp decomp = getDecomp();
	    setCovariance(decomp.getInverse());
	    if (variance != -1.0) {
		double[][] cv = getCovarianceArray();
		int np1 = cv.length;
		if (variance == 0.0) {
		    for (int i = 0; i < np1; i++) {
			for (int j = 0; j < np1; j++) {
			    cv[i][j] = 0.0;
			}
		    }
		} else {
		    for (int i = 0; i < np1; i++) {
			for (int j = 0; j < np1; j++) {
			    cv[i][j] *= variance;
			}
		    }
		}
	    }
	}
    }

    /**
     * Class to provide a linear least squares fit when the basis
     * functions are the functions whose values are 1, x, x<sup>2</sup>,
     * x<sup>3</sup>, etc.
     * The parameters are the coefficients of the polynomial, with
     * the 0th parameter being the coefficient for 1, the 1st parameter
     * being the coefficient for x, etc.
     * <P>
     * The same behavior can be obtained by using the class
     * {@link LeastSquaresFit.FunctionBasis}: this class is provided
     * to (a) increase performance and (b) to provide a simpler
     * constructor.
     * @see LeastSquaresFit.Linear
     */
    public static class Polynomial extends Linear {

	/**
	 * Get the degree of the polynomial.
	 * If the number of parameters is m, the degree n is given
	 * by n = m-1.
	 * @return the degree of the polynomials used in this fit.
	 */
	public int getDegree() {return getParametersArray().length - 1;}

	@Override
	protected LeastSquaresFit getFit() {
	    return new Polynomial(this);
	}

	// not complete implementation  - we just need enough
	// to get the function.
	private Polynomial(Polynomial existing) {
	    setParameters(new double[existing.getNumberOfParameters()]);
	}

	/**
	 * Constructor.
	 * This constructor uses the data points to compute the
	 * standard deviation. As a result, the fit will always seem
	 * to be a good one when measured by the value of &chi;<sup>2</sup>.
	 * @param n the degree of the polynomial that will be used to
	 *        fit the data
	 * @param x the values of x<sub>i</sub>
	 * @param y the values of y<sub>i</sub>
	 */
	public Polynomial(int n, double[] x, double[] y) {
	    int np1 = n + 1;
	    // double[] parameters = new double[np1];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][np1];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		double prod = 1.0;
		for (int j = 0; j < np1; j++) {
		    X[i][j] = prod;
		    prod *= xi;
		}
	    }
	    double[] tmp = new double[np1];
	    double[][] H = new double[np1][np1];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < np1; j++) {
		for (int i = 0; i < x.length; i++) {
		    tmp[j] += X[i][j]*y[i];
		}
		for (int i = 0; i < np1; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double yy = (X[k][i]*X[k][j]) - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total;
		}
	    }

	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(y));
	    if (x.length == np1) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < np1; i++) {
		    for (int j = 0; j < np1; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double sumsq = LeastSquaresFit.sumOfSquares(this, x, y);
		double variance = sumsq / (x.length - np1);
		// Chi-square is sumsq divided by the variance because
		// the sigmas all have the same value, but that just
		// gives x.length - np1.  This happens because we don't have
		// an independent estimate of the variance, so we are
		// implicitly assuming we have the right fit.
		setChiSquare(x.length - np1);
		setDegreesOfFreedom(x.length - np1);
		// reducedChiSquare = ChiSquare divided by the degrees 
		// of freedom.
		setReducedChiSquare(1.0);
		// the H matrix used sigma=1, so we should have divided it by
		// the variance, which we didn't know. The covariance is the
		// inverse of H, so we should multiply it by the variance to
		// get the right value.
		setVariance(variance);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < np1; i++) {
		    for (int j = 0; j < np1; j++) {
			cv[i][j] *= variance;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor providing a standard deviation for Y values.
	 * @param n the degree of the polynomial that will be used to
	 *        fit the data
	 * @param x the values of x<sub>i</sub>
	 * @param y the values of y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 */
	public Polynomial(int n, double[] x, double[] y, double sigma) {
	    int np1 = n + 1;
	    // double[] parameters = new double[np1];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][np1];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		double prod = 1.0;
		for (int j = 0; j < np1; j++) {
		    X[i][j] = prod;
		    prod *= xi;
		}
	    }
	    double sigma2 = sigma*sigma;
	    double[] tmp = new double[np1];
	    double[][] H = new double[np1][np1];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < np1; j++) {
		for (int i = 0; i < x.length; i++) {
		    tmp[j] += X[i][j]*y[i];
		}
		// tmp[j] /= sigma2;
		for (int i = 0; i < np1; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double term = (X[k][i]*X[k][j]);
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total; /* / sigma2;*/
		}
	    }
	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(y));
	    if (x.length == np1) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < np1; i++) {
		    for (int j = 0; j < np1; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length-np1);
		setReducedChiSquare(chiSq/(x.length-np1));
		// We didn't include the factor of sigma2 in the previous
		// matrices, and just fix up the value here, so as to
		// avoid some additional arithmetic while computing the
		// parameters.
		setVariance(sigma2);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < np1; i++) {
		    for (int j = 0; j < np1; j++) {
			cv[i][j] *= sigma2;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor given an error for each data point.
	 * @param n the degree of the polynomial that will be used to
	 *        fit the data
	 * @param x the values of x<sub>i</sub>
	 * @param y the values of y<sub>i</sub>
	 * @param sigma the values of &sigma;<sup>i</sub>
	 */
	public Polynomial(int n, double[] x, double[] y, double[] sigma) {
	    int np1 = n + 1;
	    // double[] parameters = new double[np1];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][np1];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		double prod = 1.0;
		for (int j = 0; j < np1; j++) {
		    X[i][j] = prod;
		    prod *= xi;
		}
	    }
	    double[] tmp = new double[np1];
	    double[] tmp2 = new double[y.length];
	    double[][] H = new double[np1][np1];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < np1; j++) {
		for (int i = 0; i < x.length; i++) {
		    double si = sigma[i];
		    tmp[j] += X[i][j]*y[i] / (si*si);
		}
		// tmp[j] /= sigma2;
		for (int i = 0; i < np1; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double sk = sigma[k];
			double term = (X[k][i]*X[k][j]) / (sk*sk);
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total; /* / sigma2;*/
		}
	    }
	    for (int i = 0; i < x.length; i++) {
		double si = sigma[i];
		tmp2[i] = y[i]/si;
		for (int j = 0; j < np1; j++) {
		    X[i][j] /= sigma[i];
		}
	    }

	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(tmp2));

	    if (x.length == np1) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < np1; i++) {
		    for (int j = 0; j < np1; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length-np1);
		setReducedChiSquare(chiSq/(x.length-np1));
	    }
	}

	@Override
	public double covariance(double x1, double x2) {
	    double[][] cv = getCovarianceArray();
	    int n = cv.length;
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double x1factor = 1.0;
	    for (int j = 0; j < n; j++) {
		double x2factor = 1.0;
		for (int k = 0; k < n; k++) {
		    double term = cv[j][k] * x1factor * x2factor;
		    x2factor *= x2;
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
		x1factor *= x1;
	    }
	    return state.total;
	}

	@Override
	public double valueAt(double arg) {
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    double prod = 1.0;
	    for (int i = 0; i < n; i++) {
		double term = prod*parameters[i];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
		prod *= arg;
	    }
	    return state.total;
	}

	@Override
	public double derivAt(double arg) {
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    double prod = 1.0;
	    for (int i = 1; i < n; i++) {
		double term = prod*parameters[i]*i;
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
		prod *= arg;
	    }
	    return state.total;
	}

	@Override
	public double secondDerivAt(double arg) {
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    double prod = 1.0;
	    for (int i = 2; i < n; i++) {
		double term = prod*parameters[i]*i*(i-1);
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
		prod *= arg;
	    }
	    return state.total;
	}
    }

    /**
     * Linear least squares fit using a specified function basis.
     * The parameters are provided in an array with an ordering matching
     * that of the basis functions passed as the variable-length argument
     * for this class' constructors.
     * @see LeastSquaresFit.Linear
     */
    public static class FunctionBasis extends Linear {
	
	RealValuedFunction[] fs = null;

	@Override
	protected LeastSquaresFit getFit() {
	    return new FunctionBasis(this);
	}

	// not complete implementation  - we just need enough
	// to get the function.
	private FunctionBasis(FunctionBasis existing) {
	    this.fs = existing.fs;
	    setParameters(new double[existing.getNumberOfParameters()]);
	}

	/**
	 * Constructor.
	 * The number of basis functions determines the number of
	 * parameters. If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions must implement the corresponding derivatives.
	 * @param <T> a type that implements or extends  RealValuedFunctionops
	 * @param y the values for y<sub>i</sub>
	 * @param x the values for x<sub>i</sub>
	 * @param fs the basis functions
	 * @exception IllegalArgumentException an argument was null
	 */
	@SafeVarargs
	public <T extends RealValuedFunctOps>
	    FunctionBasis(double[] x, double[] y, T... fs)
	{
	    if (x == null || y == null) {
		throw new IllegalArgumentException("nullArg");
	    }
	    int n = fs.length;
	    this.fs = new RealValuedFunction[n];
	    for (int i = 0; i < n; i++) {
		RealValuedFunctOps fops = fs[i];
		if (fops == null) {
		    throw new IllegalArgumentException(errorMsg("nullArg"));
		} else if (fops instanceof RealValuedFunction) {
		    this.fs[i] = (RealValuedFunction) fops;
		} else {
		    this.fs[i] = new RealValuedFunction(fops);
		}
	    }
	    // double[] parameters = new double[n];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][n];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		for (int j = 0; j < n; j++) {
		    X[i][j] = fs[j].valueAt(xi);
		}
	    }
	    double[] tmp = new double[n];
	    double[][] H = new double[n][n];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < x.length; i++) {
		    tmp[j] += X[i][j]*y[i];
		}
		for (int i = 0; i < n; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double yy = (X[k][i]*X[k][j]) - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total;
		}
	    }
	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(y));
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double sumsq = LeastSquaresFit.sumOfSquares(this, x, y);
		double variance = sumsq / (x.length - n);
		// Chi-square is sumsq divided by the variance because
		// the sigmas all have the same value, but that just
		// gives x.length - n.  This happens because we don't have
		// an independent estimate of the variance, so we are
		// implicitly assuming we have the right fit.
		setChiSquare(x.length - n);
		setDegreesOfFreedom(x.length - n);
		// reducedChiSquare = ChiSquare divided by the degrees 
		// of freedom.
		setReducedChiSquare(1.0);
		// the H matrix used sigma=1, so we should have divided it by
		// the variance, which we didn't know. The covariance is the
		// inverse of H, so we should multiply it by the variance to
		// get the right value.
		setVariance(variance);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] *= variance;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor given a real-valued function that is a linear
	 * combination of basis functions.
	 * The number of basis functions determines the number of
	 * parameters. If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions used by f must implement the
	 * corresponding derivatives.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param f a real-valued function with multiple arguments
	 */
	public FunctionBasis(double[] x, double[] y,
			     RealValuedFunctionVA.Linear f)
	{
	    this(x, y, f.getBasis());
	}

	/**
	 * Constructor given the standard deviation for the Y values.
	 * The number of basis functions determines the number of
	 * parameters. If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions must implement the corresponding derivatives.
	 * @param <T> a subtype of {@link RealValuedFunctOps}
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param fs the basis functions
	 */
	@SafeVarargs
	public <T extends RealValuedFunctOps>
	    FunctionBasis(double[] x, double[] y, double sigma, T... fs)
	{
	    if (x == null || y == null) {
		throw new IllegalArgumentException("nullArg");
	    }
	    int n= fs.length;
	    this.fs = new RealValuedFunction[n];
	    for (int i = 0; i < n; i++) {
		RealValuedFunctOps fops = fs[i];
		if (fops == null) {
		    throw new IllegalArgumentException(errorMsg("nullArg"));
		} else if (fops instanceof RealValuedFunction) {
		    this.fs[i] = (RealValuedFunction) fops;
		} else {
		    this.fs[i] = new RealValuedFunction(fops);
		}
	    }
	    // double[] parameters = new double[n];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][n];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		for (int j = 0; j < n; j++) {
		    X[i][j] = fs[j].valueAt(xi);
		}
	    }
	    double sigma2 = sigma*sigma;
	    double[] tmp = new double[n];
	    double[][] H = new double[n][n];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < x.length; i++) {
		    tmp[j] += X[i][j]*y[i];
		}
		// tmp[j] /= sigma2;
		for (int i = 0; i < n; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double term = (X[k][i]*X[k][j]);
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total; /* / sigma2;*/
		}
	    }
	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(y));
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length - n);
		setReducedChiSquare(chiSq/(x.length-n));
		// We didn't include the factor of sigma2 in the previous
		// matrices, and just fix up the value here, so as to
		// avoid some additional arithmetic while computing the
		// parameters.
		setVariance(sigma2);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] *= sigma2;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor given the standard deviation for the Y values
	 * and a real-valued function that is a linear combination of
	 * basis functions.
	 * The number of basis functions determines the number of
	 * parameters. If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions used by f must implement the
	 * corresponding derivatives.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param f a real-valued function with multiple arguments
	 */
	public FunctionBasis(double[] x, double[] y, double sigma,
			     RealValuedFunctionVA.Linear f)
	{
	    this(x, y, sigma, f.getBasis());
	}

	/**
	 * Constructor given the standard deviations for the Y values.
	 * The number of basis functions determines the number of
	 * parameters.  If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions must implement the corresponding derivatives.
	 * @param <T> the type of the functions
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviations &sigma;<sub>i</sub> for
	 *        the corresponding Y value y<sub>i</sub>
	 * @param fs the basis functions
	 */
	@SafeVarargs
	public <T extends RealValuedFunctOps>
	    FunctionBasis(double[] x, double[] y, double[] sigma, T... fs)
	{
	    if (x == null || y == null) {
		throw new IllegalArgumentException("nullArg");
	    }
	    int n = fs.length;
	    this.fs = new RealValuedFunction[n];
	    for (int i = 0; i < n; i++) {
		RealValuedFunctOps fops = fs[i];
		if (fops == null) {
		    throw new IllegalArgumentException(errorMsg("nullArg"));
		} else if (fops instanceof RealValuedFunction) {
		    this.fs[i] = (RealValuedFunction) fops;
		} else {
		    this.fs[i] = new RealValuedFunction(fops);
		}
	    }
	    // double[] parameters = new double[n];
	    // setParameters(parameters);
	    double X[][] = new double[x.length][n];
	    for (int i = 0; i < x.length; i++) {
		double xi = x[i];
		for (int j = 0; j < n; j++) {
		    X[i][j] = fs[j].valueAt(xi);
		}
	    }
	    double[] tmp = new double[n];
	    double[] tmp2 = new double[y.length];
	    double[][] H = new double[n][n];
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < n; j++) {
		for (int i = 0; i < x.length; i++) {
		    double si = sigma[i];
		    tmp[j] += X[i][j]*y[i] / (si*si);
		}
		// tmp[j] /= sigma2;
		for (int i = 0; i < n; i++) {
		    state.c = 0.0;
		    state.total = 0.0;
		    for (int k = 0; k < x.length; k++) {
			double sk = sigma[k];
			double term = (X[k][i]*X[k][j]) / (sk*sk);
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total; /* / sigma2;*/
		}
	    }
	    for (int i = 0; i < x.length; i++) {
		double si = sigma[i];
		tmp2[i] = y[i]/si;
		for (int j = 0; j < n; j++) {
		    X[i][j] /= si;
		}
	    }
	    /*
	    TriangularDecomp decomp = new CholeskyDecomp(H, H);
	    setDecomp(decomp);
	    decomp.solve(parameters, tmp);
	    */
	    setDecomp(H);
	    QRDecomp qr = new QRDecomp(X);
	    setParameters(qr.solve(tmp2));
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length - n);
		setReducedChiSquare(chiSq/(x.length-n));
	    }
	}

	/**
	 * Constructor given the standard deviations for the Y values
	 * and a real-valued function that is a linear combination of
	 * basis functions.
	 * The number of basis functions determines the number of
	 * parameters. If {@link LeastSquaresFit#derivAt(double)}
	 * or {@link LeastSquaresFit#secondDerivAt(double)} are used,
	 * the basis functions used by f must implement the
	 * corresponding derivatives.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviations &sigma;<sub>i</sub> for
	 *        the corresponding Y value y<sub>i</sub>
	 * @param f a real-valued function with multiple arguments
	 */
	public FunctionBasis(double[] x, double[] y, double[] sigma,
			     RealValuedFunctionVA.Linear f)
	{
	    this(x, y, sigma, f.getBasis());
	}

	@Override
	public double covariance(double x1, double x2) {
	    double[][] cv = getCovarianceArray();
	    int n = cv.length;
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < n; j++) {
		double x1factor = fs[j].valueAt(x1);
		for (int k = 0; k < n; k++) {
		    double x2factor = fs[k].valueAt(x2);
		    double term = cv[j][k] * x1factor * x2factor;
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
	    }
	    return state.total;
	}

	@Override
	public double valueAt(double arg) {
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    for (int i = 0; i < n; i++) {
		double term = fs[i].valueAt(arg)*parameters[i];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    return state.total;
	}

	@Override
	public double derivAt(double arg) throws UnsupportedOperationException {
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    for (int i = 0; i < n; i++) {
		double term = fs[i].derivAt(arg)*parameters[i];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    return state.total;
	}

	@Override
	public double secondDerivAt(double arg)
	    throws UnsupportedOperationException
	{
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    double[] parameters = getParametersArray();
	    int n = parameters.length;
	    for (int i = 0; i < n; i++) {
		double term = fs[i].secondDerivAt(arg)*parameters[i];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    return state.total;
	}
    }

    /**
     * Class to provide non-linear least-squares fits.
     * The implementation uses the Levenberg-Marquardt algorithm to
     * find the best fit.
     * <P>
     * Half of the constructors for this class take a final argument
     * providing an initial guess of the parameters for the least
     * squares fit because of the possibility of local minima, etc.,
     * that could lead to erroneous results or an exception being
     * thrown by the constructor.
     */
    public static class NonLinear extends LeastSquaresFit {

	private RealValuedFunctionVA rf;

	@Override
	protected LeastSquaresFit getFit() {
	    return new NonLinear(this);
	}

	// not complete implementation  - we just need enough
	// to get the function.
	private NonLinear(NonLinear existing) {
	    this.rf = existing.rf;
	    int n = existing.getNumberOfParameters();
	    extendedParameters = new double[n+1];
	    setParameters(new double[n]);
	}


	TriangularDecomp decomp = null;
	private double variance = -1.0;

	/**
	 * Set the variance.
	 * The variance should be set to zero in cases where the
	 * covariance matrix should be set to zero.
	 * Otherwise, it should be set to a variance by which the
	 * computed covariances will be multiplied.
	 * This is intended for cases where the covariance is computed
	 * as if sigma had the value of 1.0 (some subclasses do this
	 * when all the values of sigma are the same).
	 * @param value the value of the variance, which must not be
	 *        negative.
	 */
	protected void setVariance(double value) {
	    variance = value;
	}

	@Override
	protected void createCovariance() {
	    setCovariance(decomp.getInverse());
	    if (variance != -1.0) {
		double[][] cv = getCovarianceArray();
		int np1 = cv.length;
		if (variance == 0.0) {
		    for (int i = 0; i < np1; i++) {
			for (int j = 0; j < np1; j++) {
			    cv[i][j] = 0.0;
			}
		    }
		} else {
		    for (int i = 0; i < np1; i++) {
			for (int j = 0; j < np1; j++) {
			    cv[i][j] *= variance;
			}
		    }
		}
	    }
	}

	/**
	 * Class to provide data that controls the algorithm used to
	 * create a least-squares fit.
	 * The parameters &lambda; and &mu; are those used by the
	 * <A HREF="https://en.wikipedia.org/wiki/Levenberg%E2%80%93Marquardt_algorithm">
	 * Levenberg-Marquardt algorithm</A>. The parameter <code>limit</code>
	 * specifies a limit on the number
	 * |S<sub>i+1</sub> - S<sub>i</sub>| / S<sub>i</sub> where
	 * S<sub>i</sub> is the sum of squares for the i<sup>th</sup>
	 * iteration.  If the current value is below this limit, convergence
	 * is assumed.  The parameter <code>iterationLimit</code>, if nonzero,
	 * limits the number of iterations  If this limit is exceeded, a
	 * constructor for LeastSquaresfit or one of its subclasses will
	 * throw an exception.
	 * @see LMA
	 */
	public static class Config {
	    double lambda;
	    double nu;
	    double limit;
	    int iterationLimit;

	    /**
	     * Constructor.
	     * Default values for all the parameters will be used.
	     */
	    public Config() {
		lambda = 5.0;
		nu = 1.3;
		limit = 0.0001;
		iterationLimit = 0;
	    }

	    /**
	     * Constructor specifying parameters.
	     * @param lambda the Levenberg-Marquardt parameter &lambda;
	     * @param nu the Levenberg-Marquardt parameter &nu;
	     * @param limit the convergence limit
	     * @param iterationLimit the iteration limit
	     */
	    public Config(double lambda, double nu, double limit,
			  int iterationLimit)
	    {
		this.lambda = lambda;
		this.nu = nu;
		this.limit = limit;
		this.iterationLimit = iterationLimit;
	    }

	    /**
	     * Set the parameter &lambda;
	     * @param lambda the Levenberg-Marquardt parameter &lambda;
	     */
	    public void setLambda(double lambda) {
		this.lambda = lambda;
	    }

	    /**
	     * Get the parameter &lambda;
	     * @return the Levenberg-Marquardt parameter &lambda;
	     */
	    public double getLambda() {return lambda;}

	    /**
	     * Set the parameter &nu;
	     * @param nu the Levenberg-Marquardt parameter &nu;
	     */
	    public void setNu(double nu) {
		this.nu = nu;
	    }

	    /**
	     * Get the parameter &nu;
	     * @return the Levenberg-Marquardt parameter &nu;
	     */
	    public double getNu() {return nu;}

	    /**
	     * Set the convergence limit.
	     * @param limit the convergence limit
	     */
	    public void setLimit(double limit) {
		this.limit = limit;
	    }

	    /**
	     * Get the convergence limit.
	     * @return the convergence limit.
	     */
	    public double getLimit() {return limit;}

	    /**
	     * Set the iteration limit.
	     * @param limit the iteration limit; 0 or negative if there is none
	     */
	    public void setIterationLimit(int limit) {
		iterationLimit = limit;
	    }

	    /**
	     * Get the iteration limit.
	     * @return the iteration limit
	     */
	    public int getIterationLimit() {return iterationLimit;}

	}

	private static Config defaultConfig = new Config();


	double[] extendedParameters;

	/**
	 * Constructor.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first. Derivatives
	 * with respect to the first argument, however, must be implemented
	 * if the least-square fit's derivatives are used.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, RealValuedFunctionVA rf,
			 Config config)
	{
	    this(x, y, rf, config, null);
	}
	/**
	 * Constructor with an initial guess for the fit's parameters.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first. Derivatives
	 * with respect to the first argument, however, must be implemented
	 * if the least-square fit's derivatives are used.
	 * The order of the parameters provide in the initial guess is the
	 * same as those for the parameter arguments of the function fitting
	 * the data points.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @param guess an initial guess for the parameters
	 * @exception IllegalArgumentException the array guess is too short
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, RealValuedFunctionVA rf,
			 Config config, double[] guess)
	{
	    this.rf = rf;

	    int n = rf.minArgLength() - 1;
	    extendedParameters = new double[n+1];
	    double[] ourGuess = new double[n];
	    if (guess != null) {
		if (guess.length < ourGuess.length) {
		    throw new IllegalArgumentException
			(errorMsg("gTooShort", guess.length, ourGuess.length));

		}
		System.arraycopy(guess, 0, ourGuess, 0, ourGuess.length);
	    }
	    setParameters(ourGuess);

	    if (config == null) config = defaultConfig;

	    // y before x is the LMA convention.
	    double sumsq = LMA.findMin(rf, LMA.Mode.LEAST_SQUARES,
				       ourGuess, config.lambda, config.nu,
				       config.limit,
				       config.iterationLimit, y, x);
	    System.arraycopy(ourGuess, 0, extendedParameters, 1, n);

	    double[][] J = new double[x.length][];
	    double[][] H = new double[n][n];
	    for (int i = 0; i < x.length; i++) {
		extendedParameters[0] = x[i];
		J[i] = rf.jacobian(1,extendedParameters);
	    }
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    adder.reset();
		    for (int k = 0; k < x.length; k++) {
			double term = J[k][i]*J[k][j];
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total;
		}
	    }
	    decomp = new CholeskyDecomp(H, H);
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		sumsq = LeastSquaresFit.sumOfSquares(this, x, y);
		double variance = sumsq / (x.length - n);
		// Chi-square is sumsq divided by the variance because
		// the sigmas all have the same value, but that just
		// gives x.length - n.  This happens because we don't have
		// an independent estimate of the variance, so we are
		// implicitly assuming we have the right fit.
		setChiSquare(x.length - n);
		setDegreesOfFreedom(x.length - n);
		// reducedChiSquare = ChiSquare divided by the degrees
		// of freedom.
		setReducedChiSquare(1.0);
		// the H matrix used sigma=1, so we should have divided it by
		// the variance, which we didn't know. The covariance is the
		// inverse of H, so we should multiply it by the variance to
		// get the right value.
		setVariance(variance);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] *= variance;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor given the standard deviation for the Y values.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first. Derivatives
	 * with respect to the first argument, however, must be implemented
	 * if the least-square fit's derivatives are used.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, double sigma,
			 RealValuedFunctionVA rf,
			 Config config)
	{
	    this(x, y, sigma, rf, config, null);
	}

	/**
	 * Constructor given the standard deviation for the Y values and
	 * an initial guess for the fit's parameters.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first. Derivatives
	 * with respect to the first argument, however, must be implemented
	 * if the least-square fit's derivatives are used.
	 * The order of the parameters provide in the initial guess is the
	 * same as those for the parameter arguments of the function fitting
	 * the data points.
	 * @param x the values for x<sub>i</sub>
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @param guess an initial guess for the parameters
	 * @exception IllegalArgumentException the array guess is too short
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, double sigma,
			 RealValuedFunctionVA rf,
			 Config config,
			 double[] guess)
	{
	    this.rf = rf;

	    int n = rf.minArgLength() - 1;
	    extendedParameters = new double[n+1];
	    double[] ourGuess = new double[n];
	    setParameters(ourGuess);

	    if (config == null) config = defaultConfig;

	    if (guess != null) {
		if (guess.length < ourGuess.length) {
		    throw new IllegalArgumentException
			(errorMsg("gTooShort", guess.length, ourGuess.length));

		}
		System.arraycopy(guess, 0, ourGuess, 0, ourGuess.length);
	    }

	    // y before x is the LMA convention.
	    double sumsq = LMA.findMin(rf, LMA.Mode.LEAST_SQUARES,
				       ourGuess, config.lambda, config.nu,
				       config.limit,
				       config.iterationLimit, y, x);
	    System.arraycopy(ourGuess, 0, extendedParameters, 1, n);

	    double[][] J = new double[x.length][];
	    double[][] H = new double[n][n];
	    for (int i = 0; i < x.length; i++) {
		extendedParameters[0] = x[i];
		J[i] = rf.jacobian(1,extendedParameters);
	    }
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    adder.reset();
		    for (int k = 0; k < x.length; k++) {
			double term = J[k][i]*J[k][j];
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total;
		}
	    }
	    decomp = new CholeskyDecomp(H, H);
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] = 0.0;
		    }
		}
		*/
	    } else {
		double sigma2 = sigma*sigma;
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length - n);
		setReducedChiSquare(chiSq/(x.length-n));
		// We didn't include the factor of sigma2 in the previous
		// matrices, and just fix up the value here, so as to
		// avoid some additional arithmetic while computing the
		// parameters.
		setVariance(sigma2);
		/*
		double[][] cv = getCovarianceArray();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			cv[i][j] *= sigma2;
		    }
		}
		*/
	    }
	}

	/**
	 * Constructor given the standard deviation for the Y values.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * parameters.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first. Derivatives
	 * with respect to the first argument, however, must be implemented
	 * if the least-square fit's derivatives are used.
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param x the values for x<sub>i</sub>
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, double[] sigma,
			 RealValuedFunctionVA rf,
			 Config config)
	{
	    this(x, y, sigma, rf, config, null);
	}
	/**
	 * Constructor given the standard deviation for the Y values
	 * and
	 * an initial guess for the fit's parameters.
	 * The first argument of the function is a value of x and the
	 * remaining arguments are parameters set by the least-squares fit.
	 * parameters.
	 * The real-valued function must implement the derivatives with
	 * respect to all its arguments except the first.
	 * The order of the parameters provide in the initial guess is the
	 * same as those for the parameter arguments of the function fitting
	 * the data points.
	 * @param y the values for y<sub>i</sub>
	 * @param sigma the standard deviation for every y value
	 * @param x the values for x<sub>i</sub>
	 * @param rf the function used to fit the data points given by x and y
	 * @param config parameters used to control the least-squares fit; null
	 *        for a default set of parameters
	 * @param guess an initial guess for the parameters
	 * @exception IllegalArgumentException the array guess is too short
	 * @see LeastSquaresFit.NonLinear.Config
	 */
	public NonLinear(double[] x, double[] y, double[] sigma,
			 RealValuedFunctionVA rf,
			 Config config, double[] guess)
	{
	    this.rf = rf;

	    int n = rf.minArgLength() - 1;
	    extendedParameters = new double[n+1];
	    double[] ourGuess = new double[n];
	    setParameters(ourGuess);

	    if (config == null) config = defaultConfig;

	    if (guess != null) {
		if (guess.length < ourGuess.length) {
		    throw new IllegalArgumentException
			(errorMsg("gTooShort", guess.length, ourGuess.length));

		}
		System.arraycopy(guess, 0, ourGuess, 0, ourGuess.length);
	    }

	    // y before x is the LMA convention.
	    double sumsq = LMA.findMin(rf, LMA.Mode.WEIGHTED_LEAST_SQUARES,
				       ourGuess, config.lambda, config.nu,
				       config.limit,
				       config.iterationLimit, y, sigma, x);
	    System.arraycopy(ourGuess, 0, extendedParameters, 1, n);

	    double[][] J = new double[x.length][];
	    double[][] H = new double[n][n];
	    for (int i = 0; i < x.length; i++) {
		extendedParameters[0] = x[i];
		J[i] = rf.jacobian(1,extendedParameters);
	    }
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    adder.reset();
		    for (int k = 0; k < x.length; k++) {
			double sk = sigma[k];
			double term = J[k][i]*J[k][j] / (sk*sk);
			double yy = term - state.c;
			double t = state.total + yy;
			state.c = (t - state.total) - yy;
			state.total = t;
		    }
		    H[i][j] = state.total;
		}
	    }
	    decomp = new CholeskyDecomp(H, H);
	    if (x.length == n) {
		setChiSquare(0.0);
		setDegreesOfFreedom(0);
		setReducedChiSquare(Double.POSITIVE_INFINITY);
		setVariance(0.0);
	    } else {
		double chiSq = LeastSquaresFit.chiSquare(this, x, y, sigma);
		setChiSquare(chiSq);
		setDegreesOfFreedom(x.length - n);
		setReducedChiSquare(chiSq/(x.length-n));
	    }
	}

	@Override
	public synchronized double covariance(double x1, double x2) {
	    double[][] cv = getCovarianceArray();
	    int n = cv.length;
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    for (int j = 0; j < n; j++) {
		extendedParameters[0] = x1;
		double x1factor = rf.derivAt(j+1, extendedParameters);
		for (int k = 0; k < n; k++) {
		    extendedParameters[0] = x2;
		    double x2factor = rf.derivAt(k+1,extendedParameters);
		    double term = cv[j][k] * x1factor * x2factor;
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
	    }
	    return state.total;
	}

	@Override
	protected synchronized void setParameters(double[] parameters) {
	    super.setParameters(parameters);
	    System.arraycopy(parameters, 0,
			     extendedParameters, 1, parameters.length);
	}

	@Override
	public synchronized double valueAt(double arg) {
	    extendedParameters[0] = arg;
	    return rf.valueAt(extendedParameters);
	}

	@Override
	public synchronized double derivAt(double arg) {
	    extendedParameters[0] = arg;
	    return rf.derivAt(0, extendedParameters);
	}

	@Override
	public synchronized double secondDerivAt(double arg) {
	    extendedParameters[0] = arg;
	    return rf.secondDerivAt(0,0, extendedParameters);
	}
    }

    /**
     * Class providing a least squares fit using a BSpline.
     * The spline can be adjusted by changing its degree and the
     * number of control points it uses. One can optionally specify
     * the either end of the spline or both is/are clamped, or that
     * the spline is periodic.  The default is an unclamped, non-periodic
     * spline.
     */
    public static class BSpline extends LeastSquaresFit {

	org.bzdev.math.BSpline bspline;
	private LeastSquaresFit fit;


	@Override
	protected LeastSquaresFit getFit() {
	   return new BSpline(this);
	}

	// not complete implementation  - we just need enough
	// to get the function.
	private BSpline(BSpline existing) {
	    bspline = existing.bspline;
	    super.setParameters(existing.getParameters());
	}

	@Override
	protected void createCovariance() {
	    setCovariance(fit.getCovarianceArray());
	    fit = null;
	}


	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, and a set of Y values for given X values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.
	 * A default mode (BSpline.Mode.UNCLAMPED) will be used.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 */
	public BSpline(int degree, int n, double[] x, double[] y)
	{
	    this(degree, n, (org.bzdev.math.BSpline.Mode)null, x, y);
	}
	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, mode, and a set of Y values for given X values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.  If the mode argument is null, it must
	 * be cast to the type {@link org.bzdev.math.BSpline.Mode} to
	 * a void a conflict with a constructor that uses three arrays of
	 * double.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param mode the mode for the B-spline (either
	 *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
	 *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
	 *        or BSpline.Mode.PERIODIC); null for the default
	 *        (BSpline.Mode.UNCLAMPED)
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 */
	public BSpline(int degree, int n, org.bzdev.math.BSpline.Mode mode,
		       double[] x, double[] y)
	{
	    bspline = new  org.bzdev.math.BSpline(degree, n, mode, x, y);
	    fit = bspline.getLSF();
	    super.setParameters(fit.getParametersArray());
	    setChiSquare(fit.getChiSquare());
	    setDegreesOfFreedom(fit.getDegreesOfFreedom());
	}


	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, and a set of Y values for given X values, with
	 * a specified error for the Y values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.
	 * A default mode (BSpline.Mode.UNCLAMPED) will be used.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 * @param sigma the standard deviation for the Y values
	 */
	public BSpline(int degree, int n, double[] x, double[] y, double sigma)
	{
	    this(degree, n, null, x, y, sigma);
	}

	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, mode, and a set of Y values for given X values, with
	 * a specified error for the Y values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param mode the mode for the B-spline (either
	 *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
	 *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
	 *        or BSpline.Mode.PERIODIC); null for the default
	 *        (BSpline.Mode.UNCLAMPED).
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 * @param sigma the standard deviation for the Y values
	 */
	public BSpline(int degree, int n, org.bzdev.math.BSpline.Mode mode,
		       double[] x, double[] y, double sigma)
	{
	    bspline = new  org.bzdev.math.BSpline(degree, n, mode, x, y, sigma);
	    fit = bspline.getLSF();
	    super.setParameters(fit.getParametersArray());
	    setChiSquare(fit.getChiSquare());
	    setDegreesOfFreedom(fit.getDegreesOfFreedom());
	}

	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, mode, and a set of Y values and their standard
	 * deviations for given X values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.
	 * A default mode (BSpline.Mode.UNCLAMPED) will be used.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 * @param sigma the standard deviations for the values provided by
	 *        the argument y
	 */
	public BSpline(int degree, int n,
		       double[] x, double[] y, double[] sigma)
	{
	    this(degree, n, null, x, y, sigma);
	}

	/**
	 * Constructor specifying a B-spline's degree, number of control
	 * points, mode, and a set of Y values and their standard
	 * deviations for given X values.
	 * The number of X,Y data points must be larger than the number
	 * of control points.
	 * @param degree the degree of the B-spline that will be created
	 * @param n the number of control points
	 * @param mode the mode for the B-spline (either
	 *        BSpline.Mode.UNCLAMPED, BSpline.Mode.CLAMPED,
	 *        BSpline.Mode.CLAMPED_LEFT, BSpline.Mode.CLAMPED_RIGHT,
	 *        or BSpline.Mode.PERIODIC); null for the default
	 *        (BSpline.Mode.UNCLAMPED)
	 * @param x values in the domain of the spline
	 * @param y values in the range of the spline
	 * @param sigma the standard deviations for the values provided by
	 *        the argument y
	 */
	public BSpline(int degree, int n, org.bzdev.math.BSpline.Mode mode,
		       double[] x, double[] y, double[] sigma)
	{
	    bspline = new org.bzdev.math.BSpline(degree, n, mode, x, y, sigma);
	    fit = bspline.getLSF();
	    super.setParameters(fit.getParametersArray());
	    setChiSquare(fit.getChiSquare());
	    setDegreesOfFreedom(fit.getDegreesOfFreedom());
	}

	@Override
	protected void setParameters(double[] parameters) {
	    super.setParameters(parameters);
	    bspline.setControlPoints(parameters);
	}

	@Override
	public double covariance(double x1, double x2) {
	    double[][] cv = getCovarianceArray();
	    int n = cv.length;
	    Adder.Kahan adder = new Adder.Kahan();
	    Adder.Kahan.State state = adder.getState();
	    int p = bspline.getDegree();
	    for (int j = 0; j < n; j++) {
		double x1factor = bspline.N(j, p, x1);
		for (int k = 0; k < n; k++) {
		    double x2factor = bspline.N(k,p, x2);
		    double term = cv[j][k] * x1factor * x2factor;
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
	    }
	    return state.total;
	}

	@Override
	public double valueAt(double arg) {
	    return bspline.valueAt(arg);
	}

	@Override
	public double derivAt(double arg) {
	    return bspline.derivAt(arg);
	}

	@Override
	public double secondDerivAt(double arg) {
	    return bspline.secondDerivAt(arg);
	}
    }
}

//  LocalWords:  exbundle blockquote getCovarianceMatrix pre lsf LMA
//  LocalWords:  LeastSquaresFit RealValuedFunction SplinePath chiSq
//  LocalWords:  xFunction chiSquare reducedChiSq argArrayTooShort
//  LocalWords:  IllegalArgumentException createCovariance th indices
//  LocalWords:  setCovariance LeastSquares sumOfSquares href TDA JPL
//  LocalWords:  Wikipedia TriangularDecomp ij TriangularDecomp's cv
//  LocalWords:  CholeskyDecomp BZDev FunctionBasis np sumsq tmp fs
//  LocalWords:  getCovarianceArray reducedChiSquare Levenberg rf
//  LocalWords:  Marquardt iterationLimit LeastSquaresfit CubicSpline
//  LocalWords:  UNCLAMPED BSpline config gTooShort unclamped
