package org.bzdev.math;
import java.util.Arrays;
import org.bzdev.lang.UnexpectedExceptionError;
//@exbundle org.bzdev.math.lpack.Math

/**
 * Class to implement the Levenberg-Marquardt algorithm for solving
 * non-linear least squares problems.
 * A description of this algorithm can be found on the
 * <A HREF="https://en.wikipedia.org/wiki/Levenberg%E2%80%93Marquardt_algorithm">
 * Levenberg-Marquardt Algorithm Wikipedia page</A> and
 * <A HREF="http://www2.imm.dtu.dk/pubdb/views/edoc_download.php/3215/pdf/imm3215.pdf">
 * Methods for Non-Linear Least Squares Problems</A>.
 * <P>
 * This class consists of static methods. For all the public methods,
 * the first argument is an instance of RealValuedFunctionVA, and the
 * last arguments are a variable-length argument list of type double[]).
 * If a mode is provided, it will always be the second
 * argument. There are ways variable-length arguments are handled:
 * <ul>
 *   <li> for the sum-of-squares methods, the final argument contains
 *        parameters. These become the last arguments passed to the
 *        real-valued function.  The other arguments in the
 *        variable-argument list are arrays of length n, and the
 *        i<sup>th</sup> elements of these arrays are used in
 *        computing the i<sup>th</sup> term in the sum. The order of
 *        the subset of these elements that are passed to the function
 *        is the same as the order of their arrays.  Depending on a
 *        mode the first one or two arguments may be used separately
 *        and not passed to the function.
 *   <li> for the methods that find a minimum sum of squares, the last
 *        arguments are arrays of the same length, and the parameters
 *        are obtained from a separate argument. The i<sup>th</sup>
 *        elements of these arrays are used in computing the
 *        i<sup>th</sup> term in the sum. The order of the subset of
 *        these elements that are passed to the function is the same
 *        as the order of their arrays.  Depending on a mode the first
 *        one or two arguments may be used separately and not passed
 *        to the function.
 * </ul>
 * <P>
 * The methods can use one of three modes:
 * <UL>
 *    <LI> LMA.Mode.NORMAL (the default) computes the sum of the squares
 *         of values computed by the real-valued function provided as the
 *         method's first argument.
 *    <LI> LMA.Mode.LEAST_SQUARES computes the sum of the squares of the terms
 *         <code>args[0][i]-f.valueAt(args[1][i],args[2][i],...parameter[0],...)</code>
 *         where i is the array index for the term.
 *    <LI> LMA.Mode.WEIGHTED_LEAST_SQUARES computes the sum of the squares of
 *         of the terms
 *         <code>(args[0][i]-f.valueAt(args[2][i],...,parameter[0],...))/args[1][i]</code>
 * </UL>
 * the real-valued functions must be created so as to use a fixed number of
 * arguments.  The NORMAL mode is intended for general use, whereas the
 * other two modes are convenient for least-squares fits, with the advantage
 * of using functions that need fewer arguments (e.g., there are a smaller
 * number of partial derivatives to implement).
 */
public class LMA {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Compute the sum of the squares of a real-valued function giving
     * various arguments.
     * The arguments appear first in the function calls, followed by
     * the parameters.  For the i<sup>th</sup> term in the sum, the
     * parameters are always the same while the arguments, which vary
     * with an index i, are given by args[0][i], args[1][i], etc., with
     * the last args array representing the parameters.
     * The values summed are the squares of each term.
     * <P>
     * For example, if <code>parameters</code> is an array of length 3,
     * and <code>arg1</code> and <code>arg2</code> are arrays of
     * length n, then the 
     * <code>sumSquares(rf, arg1, arg2, parameters)</code>
     * will call <code>rf.valueAt(arg1[i], arg2[i], parameters[0],
     * parameters[1], parameters[2])</code> and sum the squares of
     * those values with i&isin;[0,n).
     * @param rf the real-valued function whose sum of squares should
     *        be computed
     * @param args an array of values for each argument, the last of
     *        which is the parameters array
     * @return the sum of the squares
     */
    public static double sumSquares(RealValuedFunctionVA rf,
				    double[]... args)
    {
	int nm1 = args.length - 1;
	double[] array = new double[nm1 + args[nm1].length];
	System.arraycopy(args[nm1], 0, array, nm1, args[nm1].length);
	return sumSquaresAux(array, rf, nm1, args);
    }

    private static double sumSquaresAux(double[] rfargs,
					RealValuedFunctionVA rf,
					int arglen,
					double[][] args)
	throws IllegalArgumentException
    {
	int length = args[0].length;
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < length; i++) {
	    for (int j = 0; j < arglen; j++) {
		rfargs[j] = args[j][i];
	    }
	    double term = rf.valueAt(rfargs);
	    double y = (term*term) - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	return state.total;
    }

    /**
     * The mode determining how a sum of squares is calculated.
     */
    public enum Mode {
	/**
	 *  The sum of the squares of values of a real-valued function
	 *  will be minimized.
	 */
	NORMAL,
	/**
	 * The first argument will not be passed to the real-valued
	 * function and instead will be used as values y<sub>i</sub>.
	 * The terms that are squared and summed are
	 *  y<sub>i</sub> - f<sub>i</sub>, where f<sub>i</sub>
	 * is the value of the real value function applied to the
	 * remaining arguments and parameters.
	 */
	LEAST_SQUARES,
	/**
	 * The first two arguments will not be passed to the real-valued
	 * function and instead the first will be used as values y<sub>i</sub>,
	 * and the second as values &sigma;<sub>i</sub>
	 * The terms that are squared and summed are
	 * (y<sub>i</sub> - f<sub>i</sub>)/&sigma;<sub>i</sub>,
	 * where f<sub>i</sub>
	 * is the value of the real value function applied to the
	 * remaining arguments and parameters.
	 */
	WEIGHTED_LEAST_SQUARES
    }

    /**
     * Compute the sum of the squares of a real-valued function giving
     * various arguments, treating some arguments specially.
     * The arguments appear first in the function calls, followed by
     * the parameters.  For the i<sup>th</sup> term in the sum, the
     * parameters are always the same while the arguments, which vary
     * with an index i, are given by args[0][i], args[1][i], etc., with
     * the last args array representing the parameters.
     * The values summed are the squares of each term.
     * <P>
     * The terms that are summed depend on the mode.
     * <UL>
     *   <LI> If the mode is NORMAL, all arguments are passed to the
     *        function. I.e., the function's arguments for index i
     *        are args[0][i], args[1][i], ... args[args.length-2][i],
     *        ... parameters[0], parameters[1] ... where
     *        the array parameters is equal to args[args.length-1].
     *   <LI> If the mode is LEAST_SQUARES, the first argument is not
     *        passed to the function. Instead, that argument is treated
     *        as an array y and the arguments to the function are the
     *        remaining arguments and the parameters. The i<sup>th</sup>
     *        term becomes y<sub>i</sub> - f<sub>i</sub> where
     *        f<sub>i</sub> is the value of the function given the other
     *        arguments and the parameters.
     *   <LI> If the mode is WEIGHTED_LEAST_SQUARES, the first two arguments
     *        are not passed to the function. Rather, the first represents
     *        the values of y as for the LEAST_SQUARES case and the second
     *        represents a divisor.  The term squared will be
     *        (y<sub>i</sub> - f<sub>i</sub>) / &sigma;<sub>i</sub> where
     *         y<sub>i</sub> is the value of the first argument array at index
     *         i, f<sub>i</sub> is the result of evaluating the real-valued
     *         function with the other arguments and the parameters, and
     *         &sigma;<sub>i</sub> is the value of the second argument array
     *         at index i.
     * </UL>
     * <P>
     * For example, if <code>parameters</code> is an array of length 3,
     * and <code>arg1</code> and <code>arg2</code> are arrays of
     * length n, then the 
     * <code>sumSquares(rf, Mode.LEAST_SQUARES arg1, arg2, parameters)</code>
     * will evaluate <code>arg1[i] - rf.valueAt(arg2[i], parameters[0],
     * parameters[1], parameters[2])</code> and sum the squares of
     * those values for i&isin;[0,n).
     * Similarly,
     * <code>sumSquares(rf, Mode.WEIGHTED_LEAST_SQUARES arg1, arg2, arg3, parameters)</code>
     * will evaluate
     * <code>(arg1[i] - rf.valueAt(arg3[i], parameters[0], parameters[1],
     * parameters[2]))/arg2[i]</code> and sum the squares of those values
     * for i&isin;[0,n).
     * @param rf the real-valued function whose sum of squares should
     *        be computed
     * @param mode the mode (NORMAL, LEAST_SQUARES, WEIGHTED_LEAST_SQUARES)
     * @param args an array of values for each argument, the last of
     *        which is the parameters array used in each function call
     * @return sum of the squares
     */
    public static double sumSquares(RealValuedFunctionVA rf,
				    Mode mode,
				    double[]... args)
    {
	int nm1 = args.length - 1;
	double[] array;
	if (mode == null) throw new
			      IllegalArgumentException(errorMsg("nullMode"));
	switch (mode) {
	case NORMAL:
	    array = new double[nm1 + args[nm1].length];
	    System.arraycopy(args[nm1], 0, array, nm1, args[nm1].length);
	    return sumSquaresAux(array, rf, nm1, args);
	case LEAST_SQUARES:
	    array = new double[nm1-1 + args[nm1].length];
	    System.arraycopy(args[nm1], 0, array, nm1-1, args[nm1].length);
	    return sumSquaresAux(array, rf, mode, nm1, args);
	case WEIGHTED_LEAST_SQUARES:
	    array = new double[nm1-2 + args[nm1].length];
	    System.arraycopy(args[nm1], 0, array, nm1-2, args[nm1].length);
	    return sumSquaresAux(array, rf, mode, nm1, args);
	default:
	    // to prevent a compiler error - we enumerated all possible
	    // modes and checked for a null pointer.
	    throw new UnexpectedExceptionError();
	}
    }


    private static double sumSquaresAux(double[] rfargs,
					RealValuedFunctionVA rf,
					Mode mode,
					int arglen,
					double[][] args)
	throws IllegalArgumentException
    {
	int length = args[0].length;
	int offset;
	switch(mode) {
	case WEIGHTED_LEAST_SQUARES:
	    offset = 2;
	    break;
	case LEAST_SQUARES:
	    offset = 1;
	    break;
	default:
	    offset = 0;
	}

	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < length; i++) {
	    int joffset = 0;
	    for (int j = offset; j < arglen; j++) {
		rfargs[joffset++] = args[j][i];
	    }
	    double term = rf.valueAt(rfargs);
	    switch (mode) {
	    case NORMAL:
		break;
	    case LEAST_SQUARES:
		term = args[0][i] - term;
		break;
	    case WEIGHTED_LEAST_SQUARES:
		term = (args[0][i] - term)/args[1][i];
	    }
	    double y = (term*term) - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	return state.total;
    }


    /**
     * Find parameters that minimize the sum of squares of a real-valued
     * function with multiple arguments.
     * The quantity minimized is the sum of the squares of a sequence of
     * values.  The length of the sequence is the length of the arrays
     * specified by the variable-argument list args. For a given index
     * into these arrays, rf is computed with its first arguments
     * provided by the args arrays and its final arguments provided by
     * a parameters array, initially set to the value of the 'guess'
     * argument.
     * <P>
     * For example, if rf takes 4 arguments, and one calls
     * <code>findMin(rf,guess, 5.0, 1.5, 0.0001, 0, args1, args2)</code>
     * where guess is an array with 2 elements, then
     * the values whose squares will be summed are given by
     * <code>rf.valueAt(args1[i], args2[i], guess[0], guess[1])</code>
     * for each value of i in [0, <code>args1.[0].length</code>).
     * The parameter &lambda; in the  Levenberg-Marquardt algorithm
     * has the property that, when it is zero the algorithm behaves like
     * the Gauss-Newton algorithm and when it is large, it behaves like
     * the steepest-descent algorithm with some scaling so that larger
     * increments occur along the direction in which the gradient is
     * smallest.
     * @param rf the real-valued function 
     * @param guess an initial guess, also used to store the results
     * @param lambda the initial value of the Levenberg-Marquardt
     *        parameter &lambda;, which must be non-negative
     * @param nu the Levenberg-Marquardt parameter &nu;, which must be
     *        larger than 1.0
     * @param limit the convergence limit for the sum of the squares
     * @param iterationLimit the maximum number of iterations; 0 or
     *        negative if there is no limit
     * @param args arrays of arguments (all arrays must be the same
     *        length)
     * @return the sum of the squares with parameters chosen to
     *         approximate the minimum value.
     * @exception ConvergenceException the method did not converge
     *            (for example, because an iteration limit was set
     *             to too small a value)
     */
    public static double findMin(RealValuedFunctionVA rf,
				 double[] guess,
				 double lambda, double nu, double limit,
				 int iterationLimit,
				 double[]... args)
    {
	// int n = rf.minArgLength();
	int rfArgLen = args.length + guess.length;
	if (rf.minArgLength() > rfArgLen ||
	    rf.maxArgLength() < rfArgLen) {
	    throw new IllegalArgumentException(errorMsg("rvfArgLen"));
	}

	int m = guess.length;
	if (limit == 0.0 || limit < 0.0) {
	    throw new IllegalArgumentException(errorMsg("badLimitForLMA"));
	}

	int offset = rfArgLen - m;
	//int length = args[0].length;
	int n = args[0].length;
	for (int i = 1; i < args.length; i++) {
	    if (args[i].length != n) {
		throw new
		    IllegalArgumentException(errorMsg("argArrayLengthsDiffer"));
	    }
	}

	double[] current = new double[rfArgLen];
	System.arraycopy(guess, 0, current, offset, m);
	double[] next1 = new double[rfArgLen];
	double[] next2 = new double[rfArgLen];
	double[] tmp;

	double sumsq = sumSquaresAux(current, rf, args.length, args);
	double origSumsq = sumsq;
	int iteration = 0;
	int iteration2 = 0;
	while (true) {
	    if (iterationLimit > 0) {
		if (iteration > iterationLimit) {
		    if (sumsq < origSumsq) {
			System.arraycopy(current, offset, guess, 0, m);
		    }
		    throw new ConvergenceException
			(errorMsg("iterationLimitExceeded"));
		}
		iteration++;
	    } 
	    if (sumsq == 0.0) break;
	    double sumsq1 =
		findMinStep(rf, offset, m, n, current, next1,
			    lambda, args);
	    if (Math.abs((sumsq - sumsq1)/sumsq) < limit) break;
	    double sumsq2 =
		findMinStep(rf, offset, m, n, current, next2,
			    lambda/nu, args);
	    if (sumsq1 > sumsq && sumsq2 > sumsq) {
		// Every so often, try with lambda set to zero, in case
		// we are at the minimum to within floating point
		// accuracy.
		if (iteration2 % 20 == 0) {
		    double[] next3 = new double[rfArgLen];
		    double sumsq3 = findMinStep(rf, offset, m, n, current,
						next3, 0.0, args);
		    if (sumsq3 < sumsq) {
			if ((Math.abs(sumsq - sumsq3)/sumsq) < limit) {
			    current = next3;
			    sumsq = sumsq3;
			    break;
			} else {
			    sumsq = sumsq3;
			    current = next3;
			    continue;
			}
		    }
		}
		iteration2++;

		// limit the iterations when lambda is large.
		// If our guess is the right value, without
		// some special handling the loop will never
		// terminate.
		double prev = -1.0;
		int count = 0;
		while (sumsq1 > sumsq) {
		    lambda *= nu;
		    sumsq1 = findMinStep(rf, offset, m, n, current, next1,
					 lambda, args);
		    if (sumsq1 == 0.0)	break;
		    if (prev != -1.0 && lambda > 20
			&& count > 20 && prev < sumsq1) {
			break;
		    }
		    if (lambda > 10) count++;
		    prev = sumsq1;
		}
		if (sumsq > sumsq1) {
		    sumsq = sumsq1;
		    tmp = current;
		    current = next1;
		    next1 = tmp;
		}
	    } else if (sumsq1 < sumsq) {
		if (sumsq2 < sumsq1) {
		    lambda /= nu;
		    sumsq = sumsq2;
		    tmp = current;
		    current = next2;
		    next2 = tmp;
		} else {
		    sumsq = sumsq1;
		    tmp = current;
		    current = next1;
		    next1 = tmp;
		}
	    }
	}
	System.arraycopy(current, offset, guess, 0, m);
	return sumsq;
    }

    private static double findMinStep(RealValuedFunctionVA rf,
				      int offset, int m, int n,
				      double[] current, double[] next,
				      double lambda,
				      double[]... args)
	throws IllegalArgumentException
    {
	
	double[][] J = new double[n][];
	double[][] A = new double[m][m];
	double[] c = new double[n];
	double[] b = new double[m];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < args.length; j++) {
		current[j] = args[j][i];
	    }
	    // the current has the parameters at its end and
	    // the starting index for the parameters is args.length
	    J[i] = rf.jacobian(args.length, current);
	    c[i] = rf.valueAt(current); 
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < m; j++) {
		adder.reset();
		for (int k = 0; k < n; k++) {
		    double term = J[k][i]*J[k][j];
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
		A[i][j] = state.total;
		if (i == j && lambda != 0.0) A[i][j] *= (1.0 + lambda);
	    }
	    adder.reset();
	    for (int k = 0; k < n; k++) {
		double term = -1.0*J[k][i]*c[k];
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    b[i] = state.total;
	}
	// LUDecomp lud = new LUDecomp(A);
	CholeskyDecomp lud = new CholeskyDecomp(A);
	double[] g = new double[m];
	lud.solve(g, b);
	for (int j = 0; j < m; j++) {
	    int k = j + offset;
	    next[k] = current[k] + g[j];
	}
	return sumSquaresAux(next, rf, args.length, args);
    }

    /**
     * Find parameters that minimize the sum of squares of a real-valued
     * function with multiple arguments, specifying a mode.
     * The quantity minimized is the sum of the squares of a sequence of
     * values.  The length of the sequence is the length of the arrays
     * specified by the variable-argument list args. For a given index
     * into these arrays, rf is computed with its first arguments
     * provided by the args arrays and its final arguments provided by
     * a parameters array, initially set to the value of the 'guess'
     * argument.
     * <P>
     * For example, if rf takes 4 arguments, and one calls
     * <code>findMin(rf,guess, 5.0, 1.5, 0.0001, 0, args1, args2)</code>
     * where guess is an array with 2 elements, then
     * the values whose squares will be summed are given by
     * <code>rf.valueAt(args1[i], args2[i], guess[0], guess[1])</code>
     * for each value of i in [0, <code>args1.[0].length</code>).
     * The parameter &lambda; in the  Levenberg-Marquardt algorithm
     * has the property that, when it is zero the algorithm behaves like
     * the Gauss-Newton algorithm and when it is large, it behaves like
     * the steepest-descent algorithm with some scaling so that larger
     * increments occur along the direction in which the gradient is
     * smallest.
     * @param rf the real-valued function 
     * @param mode the LMA mode (LMA.Mode.NORMAL, LMA.Mode.LEAST_SQUARES,
     *        LMA.Mode.WEIGHTED_LEAST_SQUARES)
     * @param guess an initial guess, also used to store the results
     * @param lambda the initial value of the Levenberg-Marquardt
     *        parameter &lambda;, which must be non-negative
     * @param nu the Levenberg-Marquardt parameter &nu;, which must be
     *        larger than 1.0
     * @param limit the convergence limit for the sum of the squares
     * @param iterationLimit the maximum number of iterations; 0 or
     *        negative if there is no limit
     * @param args arrays of arguments (all arrays must be the same
     *        length)
     * @return the sum of the squares with parameters chosen to
     *         approximate the minimum value.
     * @exception ConvergenceException the method did not converge
     *            (for example, because an iteration limit was set
     *             to too small a value)
     */
    public static double findMin(RealValuedFunctionVA rf,
				 Mode mode,
				 double[] guess,
				 double lambda, double nu, double limit,
				 int iterationLimit,
				 double[]... args)
    {
	// int n = rf.minArgLength();
	int moffset = 0;
	switch (mode) {
	case WEIGHTED_LEAST_SQUARES:
	    moffset++;
	case LEAST_SQUARES:
	    moffset++;
	case NORMAL:
	    break;
	}
	int rfArgLen = args.length - moffset + guess.length;
	if (rf.minArgLength() > rfArgLen ||
	    rf.maxArgLength() < rfArgLen) {
	    throw new IllegalArgumentException(errorMsg("rvfArgLen"));
	}

	int m = guess.length;
	if (limit == 0.0 || limit < 0.0) {
	    throw new IllegalArgumentException("badLimitForLMA");
	}

	int offset = rfArgLen - m;
	//int length = args[0].length;
	int n = args[0].length;
	for (int i = 1; i < args.length; i++) {
	    if (args[i].length != n) {
		throw new IllegalArgumentException("argArrayLengthsDiffer");
	    }
	}

	double[] current = new double[rfArgLen];
	System.arraycopy(guess, 0, current, offset, m);
	double[] next1 = new double[rfArgLen];
	double[] next2 = new double[rfArgLen];
	double[] tmp;

	double sumsq = sumSquaresAux(current, rf, mode, args.length, args);
	double origSumsq = sumsq;
	int iteration = 0;
	int iteration2 = 0;
	while (true) {
	    if (iterationLimit > 0) {
		if (iteration > iterationLimit) {
		    if (sumsq < origSumsq) {
			System.arraycopy(current, offset, guess, 0, m);
		    }
		    throw new ConvergenceException
			(errorMsg("iterationLimitExceeded"));
		}
		iteration++;
	    } 
	    if (sumsq == 0.0) break;
	    double sumsq1 =
		findMinStep(rf, mode, moffset, offset, m, n, current, next1,
			    lambda, args);
	    if (Math.abs((sumsq - sumsq1)/sumsq) < limit) break;
	    double sumsq2 =
		findMinStep(rf, mode, moffset, offset, m, n, current, next2,
			    lambda/nu, args);
	    if (sumsq1 > sumsq && sumsq2 > sumsq) {
		// Every so often, try with lambda set to zero, in case
		// we are at the minimum to within floating point
		// accuracy.
		if (iteration2 % 20 == 0) {
		    double[] next3 = new double[rfArgLen];
		    double sumsq3 = findMinStep(rf, mode, moffset,
						offset, m, n, current,
						next3, 0.0, args);
		    if (sumsq3 < sumsq) {
			if ((Math.abs(sumsq - sumsq3)/sumsq) < limit) {
			    current = next3;
			    sumsq = sumsq3;
			    break;
			} else {
			    sumsq = sumsq3;
			    current = next3;
			    continue;
			}
		    }
		}
		iteration2++;

		// limit the iterations when lambda is large.
		// If our guess is the right value, without
		// some special handling the loop will never
		// terminate.
		double prev = -1.0;
		int count = 0;
		while (sumsq1 > sumsq) {
		    lambda *= nu;
		    sumsq1 = findMinStep(rf, mode, moffset,
					 offset, m, n, current, next1,
					 lambda, args);
		    if (sumsq1 == 0.0)	break;
		    if (prev != -1.0 && lambda > 20
			&& count > 20 && prev < sumsq1) {
			break;
		    }
		    if (lambda > 10) count++;
		    prev = sumsq1;
		}
		if (sumsq > sumsq1) {
		    sumsq = sumsq1;
		    tmp = current;
		    current = next1;
		    next1 = tmp;
		}
	    } else if (sumsq1 < sumsq) {
		if (sumsq2 < sumsq1) {
		    lambda /= nu;
		    sumsq = sumsq2;
		    tmp = current;
		    current = next2;
		    next2 = tmp;
		} else {
		    sumsq = sumsq1;
		    tmp = current;
		    current = next1;
		    next1 = tmp;
		}
	    }
	}
	System.arraycopy(current, offset, guess, 0, m);
	return sumsq;
    }

    private static double findMinStep(RealValuedFunctionVA rf,
				      Mode mode, int moffset,
				      int offset, int m, int n,
				      double[] current, double[] next,
				      double lambda,
				      double[]... args)
	throws IllegalArgumentException
    {
	double[][] J = new double[n][];
	double[][] A = new double[m][m];
	double[] c = new double[n];
	double[] b = new double[m];
	int len = args.length - moffset;
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < len; j++) {
		current[j] = args[j+moffset][i];
	    }
	    // the current has the parameters at its end and
	    // the starting index for the parameters is args.length
	    J[i] = rf.jacobian(len, current);
	    switch(mode) {
	    case NORMAL:
		c[i] = rf.valueAt(current);
		break;
	    case LEAST_SQUARES:
	    case WEIGHTED_LEAST_SQUARES:
		c[i] = args[0][i] - rf.valueAt(current);
		break;
	    }
	}
	Adder.Kahan adder = new Adder.Kahan();
	Adder.Kahan.State state = adder.getState();
	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < m; j++) {
		adder.reset();
		for (int k = 0; k < n; k++) {
		    double term = J[k][i]*J[k][j];
		    if (mode == Mode.WEIGHTED_LEAST_SQUARES) {
			double sigma = args[1][k];
			term /= (sigma*sigma);
		    }
		    double y = term - state.c;
		    double t = state.total + y;
		    state.c = (t - state.total) - y;
		    state.total = t;
		}
		A[i][j] = state.total;
		if (i == j && lambda != 0.0) A[i][j] *= (1.0 + lambda);
	    }
	    adder.reset();
	    for (int k = 0; k < n; k++) {
		double term;
		switch(mode) {
		case NORMAL:
		    term = -1.0*J[k][i]*c[k];
		    break;
		case LEAST_SQUARES:
		    term = J[k][i]*c[k];
		    break;
		case WEIGHTED_LEAST_SQUARES:
		    double sigma = args[1][k];
		    term = J[k][i]*c[k]/(sigma*sigma);
		    break;
		default:
		    throw new UnexpectedExceptionError();
		}
		double y = term - state.c;
		double t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	    b[i] = state.total;
	}
	// LUDecomp lud = new LUDecomp(A);
	CholeskyDecomp lud = new CholeskyDecomp(A);
	double[] g = new double[m];
	lud.solve(g, b);
	for (int j = 0; j < m; j++) {
	    int k = j + offset;
	    next[k] = current[k] + g[j];
	}
	return sumSquaresAux(next, rf, mode, args.length, args);
    }

    /**
     * LMA exception class for the case where convergence fails.
     */
    public static class ConvergenceException extends MathException {
	/**
	 * Constructor.
	 * Constructs a new convergence exception. The cause is not
	 * initialized, and may subsequently be initialized by a call
	 * to {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 */
	public ConvergenceException() {
	    super();
	}

	/**
	 * Constructs a new convergence exception with the specified detailed
	 * message.
	 * The cause is not initialized, and may subsequently be
	 * initialized by a call to
	 * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 * @param msg the detail message; the detail message is saved for
	 * later retrieval by a call to
	 * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
	 */
	public ConvergenceException(String msg) {
	    super(msg);
	}

	/**
	 * Constructs a new convergence exception with the specified detailed
	 * message and cause.
	 * The cause is not initialized, and may subsequently be
	 * initialized by a call to
	 * {@link java.lang.Throwable#initCause(java.lang.Throwable) initCause}.
	 * @param msg the detail message; the detail message is saved for
	 * later retrieval by a call to
	 * {@link java.lang.Throwable#getMessage() Throwable.getMessage()}
	 * @param cause the cause
	 */
	public ConvergenceException(String msg, Throwable cause) {
	    super(msg, cause);
	}

	/**
	 * Constructs a new convergence exception with the specified cause
	 * and a detail message of (cause==null ? null :
	 * cause.toString()) (which typically contains the class and
	 * detail message of cause).
	 * This constructor is useful for convergence exceptions that are
	 * little more than wrappers for other throwables.
	 * @param cause the cause; null if the cause is nonexistent or unknown
	 */
	public ConvergenceException(Throwable cause) {
	    super(cause);
	}
    }
}

//  LocalWords:  exbundle Levenberg Marquardt HREF Wikipedia ul li th
//  LocalWords:  RealValuedFunctionVA args arg sumSquares rf valueAt
//  LocalWords:  isin nullMode findMin iterationLimit minArgLength
//  LocalWords:  ConvergenceException rvfArgLen badLimitForLMA LMA
//  LocalWords:  argArrayLengthsDiffer iterationLimitExceeded msg
//  LocalWords:  LUDecomp initCause getMessage Throwable toString
//  LocalWords:  throwables
