package org.bzdev.math;
import java.util.Arrays;
import org.bzdev.lang.MathOps;
import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class providing operations on polynomials.
 * The methods in this class are static methods.
 */
public class Polynomials {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Convert a monomial polynomial to one using a Bernstein basis
     * with the same degree.
     * When this method exits,
     * &sum;<sup>n</sup><sub>i=0</sub>a<sub>i</sub>x<sup>i</sup> =
     * &sum;<sup>n</sup><sub>i=0</sub>&beta;<sub>i</sub>
     * B<sub>i,n</sub>x<sup>i</sup>.
     * <P>
     * The length of the array a must be at least n+1.
     * @param a the polynomial's coefficients using a monomial basis.
     * @param n the degree of the polynomial
     * @return an array containing the polynomial's coefficients using
     *         a Bernstein basis.
     * @exception NullPointerException an argument was null
     * @exception IllegalArgumentException an array had the wrong size or
     *            the arguments offset or n had an incorrect value
     */
    public static double[] toBezier(double[] a, int n) {
	return toBezier(null, 0, a, n);
    }

    /**
     * Convert a monomial polynomial to one using a Bernstein basis
     * with the same degree, providing an output array.
     * When this method exits,
     * &sum;<sup>n</sup><sub>i=0</sub>a<sub>i</sub>x<sup>i</sup> =
     * &sum;<sup>n</sup><sub>i=0</sub>&beta;<sub>i</sub>
     * B<sub>i,n</sub>x<sup>i</sup>.
     * <P>
     * The length of the array a must be at least n+1 and the length
     * of the array beta, if not null, must be at least offset + n + 1.
     * if beta is null, the coefficients will start at the specified offset
     * into the array beta, which will be allocated with a length of
     * offset + n + 1.
     * @param beta an array that will contain the coefficients using a
     *        Bernstein basis; null if a new array will be allocated
     * @param offset the offset into the array for the 0th Bernstein
     *        coefficient
     * @param a the polynomial's coefficients using a monomial basis.
     * @param n the degree of the polynomial
     * @return the first argument; a new array that contains the
     *         coefficients using a Bernstein basis if the first argument
     *         is null
     * @exception NullPointerException an argument was null
     * @exception IllegalArgumentException an array had the wrong size or
     *            the arguments offset or n had an incorrect value
     */
    public static double[] toBezier(double[] beta, int offset,
				   double[] a, int n)
	throws NullPointerException, IllegalArgumentException
    {
	if (beta == null) {
	    beta = new double[n+1 + offset];
	}
	if (a == null) {
	    throw new NullPointerException(errorMsg("nullArg3"));
	}
	if (offset < 0) {
	    String msg = errorMsg("secondArgNegI", offset);
	    throw new IllegalArgumentException(msg);
	}
	if (a.length < n+1 || beta.length - offset < n+1 || offset < 0) {
	    String msg = errorMsg("argArrayTooShort");
	    throw new IllegalArgumentException(msg);
	}
	if (beta == a && offset <= n) {
	    a = new double[n+1];
	    System.arraycopy(beta, 0, a, 0, n+1);
	}
	for (int k = 0; k <= n; k++) {
	    double c = 0.0;
	    double sum = 0.0;
	    for (int i = 0; i <= k; i++) {
		double term = a[i] * Binomial.coefficient(k,i)
		    / Binomial.coefficient(n,i);
		double y = term - c;
		double t = sum + y;
		c  = (t - sum) - y;
		sum = t;
	    }
	    beta[k] = sum;
	}
	return beta;
    }

    /**
     * Convert from a Bernstein basis to a monomial basis.
     * The conversion uses the relation
     * &sum;<sup>n</sup><sub>j=0</sub>&beta;<sub>j</sub>B<sub>j,n</sub>(t)
     * = &sum;<sup>n</sup><sub>i=0</sub>&sum;<sup>i</sup><sub>k=0</sub>
     * &beta;<sub>k</sub>(-1)<sup>i-k</sup>C(n,i)C(i,k)t<sup>i</sup> where
     * C(n,m) = n!/((n-m)!m!).
     * @param beta an array containing the coefficients using a
     *         Bernstein basis
     * @param offset the offset into the array for the 0th Bernstein
     *        coefficient
     * @param n the degree of the polynomial
     * @return the coefficients for a monomial basis.
     */
    public static double[] fromBezier(double[] beta, int offset, int n) {
	return fromBezier(null, beta, offset, n);
    }

    /**
     * Convert from a Bernstein basis to a monomial basis, providing an
     * array to store the monomial coefficients.
     * The conversion uses the relation
     * &sum;<sup>n</sup><sub>j=0</sub>&beta;<sub>j</sub>B<sub>j,n</sub>(t)
     * = &sum;<sup>n</sup><sub>i=0</sub>&sum;<sup>i</sup><sub>k=0</sub>
     * &beta;<sub>k</sub>(-1)<sup>i-k</sup>C(n,i)C(i,k)t<sup>i</sup> where
     * C(n,m) = n!/((n-m)!m!).
     * @param result an array that will contain the coefficients for a
     *        monomial basis (this array will be the value returned unless
     *        it is null, in which case an array will be allocated).
     * @param beta an array containing the coefficients using a
     *         Bernstein basis
     * @param offset the offset into the array for the 0th Bernstein
     *        coefficient
     * @param n the degree of the polynomial
     * @return the coefficients for a monomial basis.
     * @exception NullPointerException the second argument was null
     * @exception IllegalArgumentException the array sizes too small or
     *            the offset was out of range
     */
    public static double[] fromBezier(double[] result, double[] beta,
					 int offset, int n)
	throws NullPointerException, IllegalArgumentException
    {
	if (result == null) result = new double[n+1];
	if (beta == null) {
	    throw new NullPointerException(errorMsg("nullArg2"));
	}
	if (result.length < n+1 || beta.length + offset < n+1 || offset < 0) {
	    String msg = errorMsg("argArray1TooShortNN");
	    throw new IllegalArgumentException(msg);
	}
	if (result == beta && offset <= n) {
	    beta = new double[n+1];
	    System.arraycopy(result, offset, beta, 0, n+1);
	    offset = 0;
	}
	for (int i = 0; i <= n; i++) {
	    double c = 0.0;
	    result[i] = 0.0;
	    double sum = 0.0;
	    for (int k = 0; k <= i; k++) {
		double term = beta[offset+k] * Binomial.coefficient(n,i)
		    * Binomial.coefficient(i,k);
		if ((i-k)%2 != 0) term = -term;
		double y = term - c;
		double t = sum + y;
		c  = (t - sum) - y;
		sum = t;
	    }
	    result[i] = sum;
	}
	return result;
    }

    /**
     * Multiply one polynomial by another.
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the product of p1 and p2
     */
    public static Polynomial multiply(Polynomial p1, Polynomial p2)
	throws NullPointerException
    {
	return multiply(null, p1, p2);
    }

   /**
     * Multiply one B&eacute;zier polynomial by another.
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the product of p1 and p2
     */
    public static BezierPolynomial multiply(BezierPolynomial p1,
					    BezierPolynomial p2)
	throws NullPointerException
    {
	return multiply(null, p1, p2);
    }


    /**
     * Multiply one polynomial by another, storing the results in an
     * existing polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the product of p1 and p2 (i.e., the result argument)
     * @exception NullPointerException the second argument was null
     */
    public static Polynomial multiply(Polynomial result,
				       Polynomial p1,
				       Polynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if (result == null) {
	    if (n1 < 0 || n2 < 0) {
		return new Polynomial();
	    } else {
		result = new Polynomial(n1+n2);
		 multiply(result.coefficients,
			 p1.coefficients, n1, p2.coefficients, n2);
		int degree = n1 + n2;
		while (degree > 0 && result.coefficients[degree] == 0) {
		    degree--;
		}
		result.degree = degree;
	    }
	} else if (n1 < 0 || n2 < 0) {
	    result.softReset(-1);
	} else {
	    result.softReset(n1+n2);
	    multiply(result.coefficients,
		     p1.coefficients, n1, p2.coefficients, n2);
	    int degree = n1 + n2;
	    while (degree > 0 && result.coefficients[degree] == 0) {
		degree--;
	    }
	    result.degree = degree;
	}
	return result;
    }

    /**
     * Multiply one B&eacute;zier polynomial by another, storing the
     * results in an existing B&eacute;zier polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the product of p1 and p2 (i.e., the result argument)
     * @exception NullPointerException the second argument was null
     */
    public static BezierPolynomial multiply(BezierPolynomial result,
					    BezierPolynomial p1,
					    BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if (result == null) {
	    if (n1 < 0 || n2 < 0) {
		return new BezierPolynomial();
	    } else {
		result = new BezierPolynomial(n1+n2);
		 bezierMultiply(result.coefficients,
				p1.coefficients, n1, p2.coefficients, n2);
		int degree = n1 + n2;
		result.degree = degree;
	    }
	} else if (n1 < 0 || n2 < 0) {
	    result.softReset(-1);
	} else {
	    result.softReset(n1+n2);
	    bezierMultiply(result.coefficients,
			   p1.coefficients, n1, p2.coefficients, n2);
	    int degree = n1 + n2;
	    result.degree = degree;
	}
	return result;
    }

    /**
     * Multiply two polynomials p<sub>1</sub>(x) and p<sub>2</sub>(x) 
     * given their coefficients.
     * Coefficients are order  so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * The any of the arrays result, p1, and p2 may be identical arrays
     * (temporary copies will be made as needed).
     * @param result the coefficients for p1 multiply by p2 (the array size
     *        must be at least n1+n2+1)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @return the degree of the polynomial p<sub>1</sub>(x)p<sub>2</sub>(x)
     * @throws NullPointerException the second argument was null
     * @throws IllegalArgumentException an argument was out of range or an
     *         array was too short
     */
    public static int multiply(double[] result,
			       double[] p1, int n1,
			       double[] p2, int n2)
	throws IllegalArgumentException, NullPointerException
    {
	if (n1 < 0) {
	    String msg = errorMsg("argNonNegative3", n1);
	    throw new IllegalArgumentException(msg);
	}
	if (n2 < 0) {
	    String msg = errorMsg("argNonNegative5", n2);
	    throw new IllegalArgumentException(msg);
	}
	if (p1 == null || p2 == null || result == null)
	    throw new NullPointerException(errorMsg("nullArg"));
	int n1pn2 = n1 + n2;
	int n1pn2p1 = n1pn2 + 1;
	if (result.length < n1pn2p1) {
	    String msg = errorMsg("argArray1TooShortNN");
	    throw new IllegalArgumentException(msg);
	}

	if (p1 == p2) {
	    if (result == p1) {
		int n = Math.max(n1, n2);
		double[] tmp = new double[n+1];
		System.arraycopy(p1, 0, tmp, 0, tmp.length);
		p1 = tmp;
		p2 = tmp;
		
	    }
	} else 	if (result == p1) {
	    double[] tmp = new double[n1+1];
	    System.arraycopy(p1, 0, tmp, 0, tmp.length);
	    p1 = tmp;
	} else if (result == p2) {
	    double[] tmp = new double[n2+1];
	    System.arraycopy(p2, 0, tmp, 0, tmp.length);
	    p2 = tmp;
	}
	if (n1 < n2) {
	    double[] tmp = p1;
	    p1 = p2;
	    p2 = tmp;
	    int itmp = n1;
	    n1 = n2;
	    n2 = itmp;
	}
	/*
	double[] c = new double[n1pn2p1];
	Arrays.fill(result, 0, n1pn2p1, 0.0);
	for (int i = 0; i <= n1; i++) {
	    for (int j = 0; j <= n2; j++) {
		int ipj = i + j;
		double y = p1[i]*p2[j] - c[ipj];
		double t = result[ipj] + y;
		c[ipj] = (t - result[ipj]) - y;
		result[ipj] = t;
	    }
	}
	*/
	// do the sum as a convolution.
	for (int i = 0; i <= n1pn2; i++) {
	    int limit1 = Math.max(0, i - n1);
	    int limit2 = Math.min(i, n2);
	    double c = 0.0;
	    double sum = 0.0;
	    for (int j  = limit1; j <= limit2; j++) {
		double y = (p1[i-j] * p2[j]) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    }
	    result[i] = sum;
	}
	return n1pn2;
    }

    /**
     * Multiply two polynomials p<sub>1</sub>(x) and p<sub>2</sub>(x)
     * given their coefficients.
     * Coefficients are order  so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * The any of the arrays result, p1, and p2 may be identical arrays
     * (temporary copies will be made as needed).
     * @param result the coefficients for p1 multiply by p2 (the array size
     *        must be at least n1+n2+1)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @return the degree of the polynomial p<sub>1</sub>(x)p<sub>2</sub>(x)
     */
    public static int bezierMultiply(double[] result,
				     double[] p1, int n1,
				     double[] p2, int n2)
	throws IllegalArgumentException
    {
	double[] tmp1 = new double[n1+1];
	double[] tmp2 = new double[n2+1];
	Functions.Bernstein.scale(tmp1, p1, n1);
	Functions.Bernstein.scale(tmp2, p2, n2);
	int n = multiply(result, tmp1, n1, tmp2, n2);
	Functions.Bernstein.unscale(result, result, n);
	return n;
    }

    /**
     * Multiply a polynomial by a scalar.
     * @param s the scalar
     * @param p the polynomial
     * @return the product of s and p
     */
    public static Polynomial multiply(double s, Polynomial p)
	throws NullPointerException
    {
	return multiply(null, s, p);
    }

    /**
     * Multiply a B&eacute;zier polynomial by a scalar.
     * @param s the scalar
     * @param p the polynomial
     * @return the product of s and p
     */
    public static BezierPolynomial multiply(double s, BezierPolynomial p)
	throws NullPointerException
    {
	return multiply(null, s, p);
    }

    /**
     * Multiply a polynomial by a scalar, storing the results in an
     * existing polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param s the scalar
     * @param p the polynomial
     * @return the product of s and p (i.e., the result argument)
     */
    public static Polynomial multiply(Polynomial result,
				      double s, Polynomial p)
	throws NullPointerException
    {
	if (s == 0.0) {
	    if (result == null) {
		result = new Polynomial(0.0);
	    } else {
		result.softReset(0);
		result.degree = 0;
	    }
	    return result;
	}
	if (result == null) result = new Polynomial(p);
	if (result.degree < p.degree) {
	    result.softReset(p.degree);
	}
	int degree = p.degree;
	multiply(result.coefficients, s, p.coefficients, degree);
	while (degree > 0 && result.coefficients[degree] == 0.0) {
	    degree--;
	}
	result.degree = degree;
	return result;
    }

    /**
     * Multiply a Bezierpolynomial by a scalar, storing the results in an
     * existing polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param s the scalar
     * @param p the polynomial
     * @return the product of s and p (i.e., the result argument)
     */
    public static BezierPolynomial multiply(BezierPolynomial result,
				      double s, BezierPolynomial p)
	throws NullPointerException
    {
	if (s == 0.0) {
	    if (result == null) {
		result = new BezierPolynomial();
	    } else {
		result.softReset(-1);
	    }
	    return result;
	}
	if (result == null) result = new BezierPolynomial(p);
	if (result.degree < p.degree) {
	    result.softReset(p.degree);
	}
	int degree = p.degree;
	multiply(result.coefficients, s, p.coefficients, degree);
	result.degree = degree;
	return result;
    }

    /**
     * Multiple a polynomial p(x), given its coefficients, by a scalar s.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * <P>
     * The arrays result and p may be identical.
     * @param result the coefficients for p(x)
     * @param s the scalar
     * @param p the coefficients for p
     * @param n the degree of the polynomial p(x)
     * @return the degree of p;
     */
    public static int multiply (double result[], double s,  double[] p, int n) {
	if (s != 0.0) {
	    for (int i = 0; i < n+1; i++) {
		result[i] = s*p[i];
	    }
	    return n;
	} else {
	    result[0] = 0.0;
	    return 0;
	}
    }

    
    /**
     * Add one polynomial to another.
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the sum of p1 and p2
     */
    public static Polynomial add(Polynomial p1, Polynomial p2)
	throws NullPointerException
    {
	return add(null, p1, p2);
    }

    /**
     * Add one bezier polynomial to another.
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the sum of p1 and p2
     */
    public static BezierPolynomial add(BezierPolynomial p1,
				       BezierPolynomial p2)
	throws NullPointerException
    {
	return add(null, p1, p2);
    }


    /**
     * Add one polynomial to another, storing the results in an existing
     * polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the sum of p1 and p2 (i.e., the result argument)
     * @throws NullPointerException the second or third  argument was null
     */
    public static Polynomial add(Polynomial result,
				 Polynomial p1,
				 Polynomial p2)
	throws NullPointerException
    {

	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if (result == null) {
	    if (n1 < 0) {
		if (n2 < 0) {
		    return new Polynomial();
		} else {
		    return new Polynomial(p2);
		}
	    } else if (n2 < 0) {
		return new Polynomial(p1);
	    } else {
		int degree = Math.max(n1, n2);
		result = new Polynomial(degree);
		add(result.coefficients,
		    p1.coefficients, n1, p2.coefficients, n2);
		while (degree > 0 && result.coefficients[degree] == 0.0) {
		    degree--;
		}
		result.degree = degree;
	    }
	} else if (n1 < 0) {
	    if (n2 < 0) {
		result.reset(-1);
	    } else {
		result.setTo(p2);
	    }
	} else if (n2 < 0) {
	    result.setTo(p1);
	} else {
	    int degree = Math.max(n1, n2);
	    result.softReset(degree);
	    add(result.coefficients,
		p1.coefficients, n1, p2.coefficients, n2);
	    while (degree > 0 && result.coefficients[degree] == 0.0) {
		degree--;
	    }
	    result.degree = degree;
	}
	return result;
    }


    /**
     * Add one B&eacute;zier polynomial to another, storing the results in an
     * existing B&eacute;zier polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the sum of p1 and p2 (i.e., the result argument)
     * @throws NullPointerException an argument was null
     */
    public static BezierPolynomial add(BezierPolynomial result,
				 BezierPolynomial p1,
				 BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if (result == null) {
	    if (n1 < 0) {
		if (n2 < 0) {
		    return new BezierPolynomial();
		} else {
		    return new BezierPolynomial(p2);
		}
	    } else if (n2 < 0) {
		return new BezierPolynomial(p1);
	    } else {
		int degree = Math.max(n1, n2);
		result = new BezierPolynomial(degree);
		bezierAdd(result.coefficients,
			  p1.coefficients, n1, p2.coefficients, n2);

		result.degree = degree;
	    }
	} else if (n1 < 0) {
	    if (n2 < 0) {
		result.reset(-1);
	    } else {
		result.setTo(p2);
	    }
	} else if (n2 < 0) {
	    result.setTo(p1);
	} else {
	    int degree = Math.max(n1, n2);
	    result.softReset(degree);
	    bezierAdd(result.coefficients,
		p1.coefficients, n1, p2.coefficients, n2);
	    result.degree = degree;
	}
	return result;
    }

    /**
     * Add  two polynomials p<sub>1</sub>(x) and p<sub>2</sub>(x) given
     * their coefficients.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * The size of the <CODE>result</CODE> array must be at least
     *  max(n1+1, n2+1).
     * @param result an array of coefficients for the polynomial
     *        p<sub>1</sub>(x)+p<sub>2</sub>(x)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @return the degree of the polynomial p<sub>1</sub>(x)+p<sub>2</sub>(x)
     */
    public static int add(double[] result, double[] p1, int n1,
			  double[] p2, int n2)
    {
	return add(result, p1, n1, p2, n2, true);
    }

    /**
     * Add  two polynomials p<sub>1</sub>(x) and p<sub>2</sub>(x) given
     * their coefficients, optionally pruning the results.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * The size of the <CODE>result</CODE> array must be at least
     *  max(n1+1, n2+1).
     * <P>
     * The polynomial may be optionally pruned: when the final coefficients
     * are zero, those are eliminated from the polynomial by decreasing the
     * degree that is returned.
     * @param result an array of coefficients for the polynomial
     *        p<sub>1</sub>(x)+p<sub>2</sub>(x)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @param prune true if the result should be pruned; false otherwise
     * @return the degree of the polynomial p<sub>1</sub>(x)+p<sub>2</sub>(x)
     * @throws NullPointerException an argument was null
     */
    public static int add(double[] result, double[] p1, int n1,
			  double[] p2, int n2, boolean prune)
	throws NullPointerException
    {
	if (result == null || p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n = Math.max(n1, n2);
	int m = Math.min(n1, n2);
	for  (int i = 0; i <= m; i++) {
	    result[i] = p1[i] + p2[i];
	}
	if (n != m) {
	    if (n1 > n2) {
		if  (result != p1) {
		    for (int i = m+1; i <= n; i++) {
			result[i] = p1[i];
		    }
		}
	    } else {
		if (result != p2) {
		    for (int i = m+1; i <= n; i++) {
			result[i] = p2[i];
		    }
		}
	    }
	}
	if (prune) {
	    while (n > 0 && result[n] == 0.0) n--;
	}
	return n;
    }

    /**
     * Add two polynomials p<sub>1</sub>(x) and p<sub>2</sub>(x) represented
     * using coefficients for a Bernstein basis.
     * The size of the <CODE>result</CODE> array must be at least
     *  max(n1+1, n2+1), and the basis used will be one appropriate for
     * the degree returned.
     * @param result an array of coefficients for the polynomial
     *        p<sub>1</sub>(x)+p<sub>2</sub>(x)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @return the degree of the polynomial p<sub>1</sub>(x)+p<sub>2</sub>(x)
     * @throws NullPointerException an argument was null
     */
    public static int bezierAdd(double[] result, double[] p1, int n1,
				double[] p2, int n2)
	throws NullPointerException
    {
	if (result == null || p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (n1 == n2) {
	    return add(result, p1, n1, p2, n2, false);
	} else if (n1 < n2) {
	    int r = n2 - n1;
	    double[] tmp = (result == p2)? new double[n2+1]: result;
	    Functions.Bernstein.raiseBy(tmp, p1, n1, r);
	    add(result, tmp, n2, p2, n2, false);
	    return n2;
	} else {
	    int r = n1 - n2;
	    double[] tmp = (result == p1)? new double[n1+1]: result;
	    Functions.Bernstein.raiseBy(tmp, p2, n2, r);
	    add(result, p1, n1, tmp, n1, false);
	    return n1;
	}
    }


    /**
     * Divide one polynomial by another, returning either the quotient or
     * the remainder
     * <P>
     * Please see
     * {@link Polynomials#divide(double[],double[],double[],int,double[],int)}
     * for documentation regarding numerical accuracy.
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @param isQuotient true if the polynomial returned is a quotient;
     *        false if it is a remainder
     * @return the quotient or the remainder of p1 divided by p2
     * @throws NullPointerException an argument was null
     */
    public static Polynomial divide(Polynomial p1, Polynomial p2,
				    boolean isQuotient)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	Polynomial result = new Polynomial();
	if (isQuotient) {
	    divide(result, null, p1, p2);
	} else {
	    divide(null, result, p1, p2);
	}
	return result;
    }
    
    /**
     * Divide one B&eacute;zier polynomial by another, returning either the
     * quotient or the remainder
     * <P>
     * Please see
     * {@link Polynomials#divide(double[],double[],double[],int,double[],int)}
     * for documentation regarding numerical accuracy (this method is used
     * by the implementation of this operation for instances of
     * BezierPolynomial).
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @param isQuotient true if the polynomial returned is a quotient;
     *        false if it is a remainder
     * @return the quotient or the remainder of p1 divided by p2
     * @throws NullPointerException an argument was null
     */
    public static BezierPolynomial divide(BezierPolynomial p1,
					  BezierPolynomial p2,
					  boolean isQuotient)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	BezierPolynomial result = new BezierPolynomial();
	if (isQuotient) {
	    divide(result, null, p1, p2);
	} else {
	    divide(null, result, p1, p2);
	}
	return result;
    }


    /**
     * Divide one polynomial by another, computing both the quotient and
     * the remainder.
     * <P>
     * Please see
     * {@link Polynomials#divide(double[],double[],double[],int,double[],int)}
     * for documentation regarding numerical accuracy.
     * @param q a polynomial used to store the quotient; null if
     *        the quotient is not wanted
     * @param r the polynomial used to store the remainder; null if
     *        the remainder is not wanted
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @throws NullPointerException the third or fourth argument was null
     */
    public static void divide(Polynomial q, Polynomial r,
			      Polynomial p1, Polynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if  (n2 > n1) {
	    if (q != null) {
		q.softReset(-1);
	    }
	    if (r != null) {
		r.setTo(p1);
	    }
	    return;
	}
	if (q == null) {
	    q = new Polynomial(n1-n2);
	} else {
	    q.softReset(n1-n2);
	}
	if (r == null) {
	    r = new Polynomial(n1);
	} else {
	    r.softReset(n1);
	}
	int nq = divide(q.coefficients, r.coefficients,
			p1.coefficients, n1, p2.coefficients, n2);
	int nr = getDegree(r.coefficients, Math.min(n1,n2-1));

	while (nq > 0 && q.coefficients[nq] == 0.0) {
	    nq--;
	}
	q.degree = nq;
	while (nr > 0 && r.coefficients[nr] == 0.0) {
	    nr--;
	}
	r.degree = nr;
	return;
    }

    /**
     * Divide one B&eacute;zier polynomial by another, computing both
     * the quotient and the remainder.
     * Please see
     * {@link Polynomials#divide(double[],double[],double[],int,double[],int)}
     * for documentation regarding numerical accuracy (this method is used
     * by the implementation of this operation for instances of
     * BezierPolynomial).
     * @param q a polynomial used to store the quotient; null if
     *        the quotient is not wanted
     * @param r the polynomial used to store the remainder; null if
     *        the remainder is not wanted
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @throws NullPointerException an argument was null
     */
    public static void divide(BezierPolynomial q, BezierPolynomial r,
			      BezierPolynomial p1, BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	int n1 = p1.degree;
	int n2 = p2.degree;
	if (n1 == -1 || n2 == -1) {
	    throw new IllegalArgumentException(errorMsg("illformedPolynomial"));
	}
	if  (n2 > n1) {
	    if (q != null) {
		q.softReset(-1);
	    }
	    if (r != null) {
		r.setTo(p1);
	    }
	    return;
	}
	if (q == null) {
	    q = new BezierPolynomial(n1-n2);
	} else {
	    q.softReset(n1-n2);
	}
	if (r == null) {
	    r = new BezierPolynomial(n1);
	} else {
	    r.softReset(n1);
	}
	double[] p1c = fromBezier(null, p1.coefficients, 0, n1);
	double[] p2c = fromBezier(null, p2.coefficients, 0, n2);
	double[] qc = new double[n1-n2+1];
	double[] rc = new double[n1+1];
	int nq = divide(qc, rc, p1c, n1, p2c, n2);
	int nr = getDegree(rc, Math.min(n2,n1-1));
	while (nq > 0 && qc[nq] == 0.0) {
	    nq--;
	}
	q.degree = nq;
	toBezier(q.coefficients, 0, qc, nq);
	while (nr > 0 && rc[nr] == 0.0) {
	    nr--;
	}
	r.degree = nr;
	toBezier(r.coefficients, 0, rc, nr);
	return;
    }

    /**
     * Divide a polynomial p<sub>1</sub>(x) by a polynomial p<sub>2</sub>(x).
     * The results are polynomials q(x) and r(x) such that
     *  p<sub>1</sub>(x) = q(x)p<sub>2</sub>(x) + r(x), with the degree
     * of r(x) less than the degree of p<sub>x</sub>(x).
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * Any of the arrays q, r, p1, and p2 may be identical arrays except
     * that q &ne; p (temporary copies will be made as needed).
     * The length of q must be non-negative and at least (n1 - n2 + 1),
     * and the length of r must be at least n1+1.
     * <P>
     * The arrays that are returned should be checked to determine
     * numerical stability of any computation.  For example, the
     * following code
     * <BLOCKQUOTE><PRE><CODE>
     *	int deg1 = 10;
     *	int deg2 = 1;
     *	double[] p1 = new double[deg1+1];
     *	double[] p2 = new double[deg2+1];
     *	p1[0] = 5.055832194989883;
     *	p1[1] = -6.377532529195433;
     *	p1[2] = -1.648066454379407;
     *	p1[3] = -6.664776987879824;
     *	p1[4] = -9.913142660200338;
     *	p1[5] = 11.011644731787342;
     *	p1[6] = 6.499930124981663;
     *	p1[7] = 16.59232841567509;
     *	p1[8] = -16.11922415992573;
     *	p1[9] = 19.852145288267316;
     *	p1[10] = -4.9061808702028245;
     *	p2[0] = -14.267558704211917;
     *	p2[1] = -0.16512094757360885;
     *
     *	double[] q = new double[20];
     *	double[] r = new double[20];
     *	double[] result = new double[20];
     *
     *	int nq = Polynomials.divide(q, r, p1, deg1, p2, deg2);
     *	int nr = Polynomials.getDegree(r, deg1);
     * </CODE></PRE></BLOCKQUOTE>
     * will produce the following values for q and r:
     * <BLOCKQUOTE><PRE><CODE>
     * ... q[0] = -8.3546709168710031E18
     * ... q[1] = 9.6690064997045424E16
     * ... q[2] = -1.1190110014092871E15
     * ... q[3] = 1.2950509665220766E13
     * ... q[4] = -1.498785091272303E11
     * ... q[5] = 1.7345701496704223E9
     * ... q[6] = -2.0074483601869095E7
     * ... q[7] = 232324.34018894564
     * ... q[8] = -2687.600364379947
     * ... q[9] = 29.71264968071789
     * ... r[0] = -1.1920075776082903E20
     * </CODE></PRE></BLOCKQUOTE>
     * Due to a range of 20 orders of magnitude, the value for the
     * coefficients for p<sub>1</sub> and for the product qp<sub>2</sub>+r
     * will differ by amounts that may be comparable to p<sub>1</sub>'s
     * coefficients.
     * @param q the coefficients for the quotient polynomial q(x)
     * @param r the coefficients for the remainder polynomial r(x)
     * @param p1 the coefficients for p1
     * @param n1 the degree of p1
     * @param p2 the coefficients for p2
     * @param n2 the degree of p2
     * @return the degree of the polynomial q(x)
     * @throws NullPointerException an argument was null
     * @throws IllegalArgumentException if the array provided by the first
     *         or second  argument was too short or if
     *         the first two arguments are the same array
     */
    public static int divide(double q[], double[] r,
			     double[] p1, int n1, double[] p2, int n2)
	throws IllegalArgumentException, NullPointerException
    {
	if (q == null || r == null || p1 == null || p2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if  (q == r) throw new IllegalArgumentException(errorMsg("sameArray"));
	if (r.length < n1+1) {
	    String msg = errorMsg("argArray2TooShort");
	    new IllegalArgumentException(msg);
	}
	while (p1[n1] == 0.0 && n1 > 0) n1--;
	while (p2[n2] == 0.0 && n2 > 0) n2--;
	int nq = n1 - n2;
	Arrays.fill(q, 0, nq + 1, 0.0);
	System.arraycopy(p1, 0, r, 0, n1+1);
	double[] c = new double[n1+1];
	int n = n1;
	if (q.length < n-n2+1) {
	    String msg = errorMsg("argArray1TooShort");
	    new IllegalArgumentException(msg);
	}
	while (n >= n2) {
	    double tmp = r[n]/p2[n2];
	    q[n - n2] = tmp;
	    for (int i = 0; i < n2; i++) {
		int j = i+n-n2;
		double tot = r[j];
		double y = -tmp*p2[i] - c[j];
		double t = tot + y;
		c[j] = (t - tot) - y;
		r[j] = t;
	    }
	    r[n] = 0.0;
	    n--;
	    while (n > 0 && r[n] == 0.0) {
		    n--;
	    }
	}
	return nq;
    }

    /**
     * Get the degree of a polynomial.
     * One use of this method is to find the degree of the
     * remainder after Euclidean division.  In that case, the
     * argument n2 should be the degree of the dividend,
     * but may be the degree of the divisor if that is smaller
     * than the degree of the dividend.
     * @param r the array containing the remainder's coefficients
     * @param n2 the largest index in r that might have been set
     * @return the degree of the polynomial r(x)
     * @throws NullPointerException an argument was null
     */
    public static int getDegree(double[] r, int n2)
	throws NullPointerException
    {
	if (r == null) throw new NullPointerException(errorMsg("nullArg"));
	int n = n2;
	while (n > 0) {
	    if (r[n] != 0.0) break;
	    n--;
	}
	return n;
    }

    /**
     * Factor a reduced quartic polynomial.
     * The polynomial must be  x<sup>4</sup> + cx<sup>2</sup> + dx + e
     * for some choice of c, d, and e.
     * The algorithm is described in G. Brookfield,
     * "<A HREF="https://www.maa.org/sites/default/files/Brookfield2007-103574.pdf">
     * Factoring Quartic Polynomials: A Lost Art</A>", Mathematics Magazine,
     * Vol. 80, No. 1, February 2007, Pages 67&ndash;70.
     * This article actually describes factoring polynomials with the
     * constraint that the original polynomial and its factors have
     * only rational coefficients, i.e., in the field Q.  The proofs,
     * however are dependent only on Q being a field, and work the same
     * if Q is replaced with the field of real numbers R.
     * <P>
     * Note: The documentation for the method
     * {@link Polynomial#reducedFormShift()} indicates how to generate the
     * reduced form for a polynomial. After factoring, the factors can be
     * shifted back to recover factors for the original polynomial.
     * A reduced quartic polynomial must have its x<sup>3</sup> term set
     * to 0.0 and its x<sup>4</sup>term set to 1.0. with no roundoff errors.
     * @param p the polynomial to factor
     * @return an array of polynomials containing two factors;
     * @see #factorQuarticToQuadratics(Polynomial)
     * @exception NullPointerException the argument was null
     * @exception IllegalArgumentException the argument was not a
     *            reduced quartic polynomial
     * @exception ArithmeticException if factorization failed, probably
     *            because of a round-off error
     */
    public static Polynomial[] factorReducedQuartic(Polynomial p)
	throws IllegalArgumentException, NullPointerException,
	       ArithmeticException
    {
	if (p == null) throw new NullPointerException(errorMsg("nullArg"));
	if (p.getDegree() != 4) {
	    throw new IllegalArgumentException(errorMsg("notQuartic"));
	}
	double[] array = p.getCoefficientsArray();
	if (array[4] != 1.0) {
	    throw new IllegalArgumentException(errorMsg("notReducedQuartic"));
	}
	if (array[3] != 0.0) {
	    throw new IllegalArgumentException(errorMsg("notReducedQuartic"));
	}
	double e = array[0];
	double d = array[1];
	double c = array[2];
	double c1 = -d*d;
	double c2 = c*c - 4*e;
	double c3 = 2*c;
	double eqn[] = {c1, c2, c3, 1.0};
	int nr = RootFinder.solveCubic(eqn);
	double h2 = eqn[0];
	double hr = -1.0;
	boolean again1 = (h2 <= 0.0);
	boolean again2 = (h2 > 0);
	for (int i = 1; i < nr; i++) {
	    double val = eqn[i];
	    if (again1 && h2 <= 0.0) {
		h2 = val;
		if (h2 > 0.0) {
		    again1 = false;
		    again2 = true;
		}
	    } else if (again2) {
		long r2 = Math.round(val);
		if (val == (double) r2) {
		    double dr = Math.sqrt(val);
		    long r = Math.round(dr);
		    if (r*r == r2) {
			h2 = val;
			hr = (double)r;
			again2 = false;
		    }
		}
	    }
	}
	// check for round off issues
	if (h2 < 0.0) {
	    if ( -h2 < 1.e-12) h2 = 0.0;
	}
	double h, hp, k, kp;
	if (h2 < 0.0) {
	    throw new ArithmeticException(errorMsg("noFactors", h2));
	} else if (h2 > 0.0) {
	    h = hr < 0.0? Math.sqrt(h2): hr;
	    hp = -h;
	    double h3 = h2*h;
	    double twoh = 2*h;
	    double ch = c * h;
	    k = (h3 + ch - d) / twoh;
	    kp = (h3 + ch + d) / twoh;
	} else if (h2 == 0.0) {
	    h = 0.0;
	    double cc = c*c;
	    double s2 = cc - 4*e;
	    if (Math.abs(s2)/cc < 1.e-12) {
		// in case round off errors turned 0 into a negative value
		s2 = 0.0;
	    }
	    if (s2 < 0) {
		return null;
	    }
	    double s = Math.sqrt(s2);
	    hp = 0.0;
	    k = (c + s)/2;
	    kp = (c - s)/2;
	} else {
	    // can't happen, but this avoids having to set h, hp, k, and kp
	    // to zero in their declarations to prevent an error message
	    return null;
	}
	Polynomial[] result = {
	    new Polynomial(k, h, 1.0),
	    new Polynomial(kp, hp, 1.0)
	};
	return result;
    }

    /**
     * Factor a quartic polynomial into two quadratic polynomials;
     * The factors will consist of 1 to 3 polynomials. The first, at
     * index 0 in the returned array, is a 0</sup>th</sup> degree
     * polynomial whose value is a scale factor. This is followed
     * by one or two polynomials.  If one, the polynomial at index 1
     * will be the original polynomial scaled so that its x<sup>4</sup>
     * argument is 1.0.
     * If two, the polynomial at index 1 and 2 will
     * be quadratic polynomials representing two quadratic factors.
     * @param p the polynomial
     * @return the factors; null if factorization failed
     * @throws NullPointerException an argument was null
     * @throws IllegalArgumentException if the polynomial is not a quartic
     *         polynomial
     * @exception ArithmeticException if factorization failed, probably
     *            because of a round-off error
     */

    public static Polynomial[] factorQuarticToQuadratics(Polynomial p)
	throws IllegalArgumentException, NullPointerException,
	       ArithmeticException
    {
	if (p == null) throw new NullPointerException(errorMsg("nullArg"));
	if (p.getDegree() != 4) {
	    throw new IllegalArgumentException(errorMsg("notQuartic"));
	}
	double scale = p.getCoefficientsArray()[4];
	Polynomial pr  = new Polynomial(p);
	pr.multiplyBy(1.0/scale);
	double shift = pr.reducedFormShift();
	pr.shift(shift);
	double[] array = pr.getCoefficientsArray();
	// In case of roundoff errors
	array[4] = 1.0;
	array[3] = 0.0;
	Polynomial[] ps = Polynomials.factorReducedQuartic(pr);
	ps[0].shift(-shift);
	ps[1].shift(-shift);
	Polynomial result[] =  {
	    new Polynomial(scale),
	    ps[0],
	    ps[1]};
	return result;
    }

    /**
     * Compute the integral of |p(x)|sqrt(rp(x)) where p and rp are
     * polynomials with degrees 0, 1, or 2, and 0 or 2 respectively.
     * If p has a degree of 2, rp must have a degree of 0.
     * @param u the upper limit of integration
     * @param p A 0, 1st, or 2nd degree polynomial
     * @param rp A 0 or 2nd degree polynomial
     */
    public static double integrateAbsPRootQ(double u,
					    Polynomial p,
					    Polynomial rp)
	throws IllegalArgumentException, ArithmeticException
    {
	// p should really be sqrt(p^2), so it may flip signs when x
	// is a root of p as the square root is always non-negative.

	int pdeg = p.getDegree();
	int rpdeg = rp.getDegree();
	if (pdeg != 0 && pdeg != 1 && pdeg != 2) {
	    String msg = errorMsg("degreeError1", pdeg);
	    throw new IllegalArgumentException(msg);
	}
	if (rpdeg != 0 && rpdeg != 2) {
	    String msg = errorMsg("degreeError3", rpdeg);
	    throw new IllegalArgumentException(msg);
	}

	if (pdeg == 0 && rpdeg == 0) {
	    // corner case - both are 0 degree polynomials
	    return Math.abs(p.getCoefficientsArray()[0])
		* Math.sqrt(rp.getCoefficientsArray()[0])
		* u;
	} else if (pdeg == 1) {
	    if (rpdeg == 0) {
		double[] pa = p.getCoefficientsArray();
		if (pa[1] < 0.0) {
		    p.multiplyBy(-1.0);
		}
		double r = -pa[0]/pa[1];
		double factor = Math.sqrt(rp.getCoefficientsArray()[0]);
		// double[] tmp = p.getCoefficientsArray();
		Polynomial integral = p.integral();
		if (r > 0.0 && r < u) {
		    double rval = integral.valueAt(r);
		    return factor * (Math.abs(rval)
				     + Math.abs(integral.valueAt(u) - rval));
		}
		return factor * Math.abs(integral.valueAt(u));
	    } else if (rpdeg == 2) {
		double[] array0 = rp.getCoefficientsArray();
		double a = array0[0];
		double b = array0[1];
		double c = array0[2];
		double[] array = p.getCoefficientsArray();
		if (array[1] < 0.0) {
		    p.multiplyBy(-1.0);
		}
		double r = -array[0]/array[1];
		RealValuedFunctOps integral = (t) -> {
		    try {
			return array[0] * integrateRootP2(t, a, b, c)
			+ array[1] * integrateXRootP2(t, a, b, c);
		    } catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		    }
		};
		try {
		    if (r > 0.0 && r < u) {
			/*
			  double ival = integrateRootP2signed(u, a, b, c);
			  double X = rp.valueAt(u);
			  double val3 = array[1]*X*Math.sqrt(X)/(3*c)
			  + (array[0] - array[1]*b/(2*c))*ival ;
			  ival = integrateRootP2signed(r, a, b, c);
			  X = rp.valueAt(r);
			  double val2 = array[1]*X*Math.sqrt(X)/(3*c)
			  + (array[0] - array[1]*b/(2*c))*ival ;
			  X = rp.valueAt(0.0);
			  double val1 = array[1]*X*Math.sqrt(X)/(3*c);
			*/
			double val1 = integral.valueAt(0.0);
			double val2 = integral.valueAt(r);
			double val3 = integral.valueAt(u);
			return Math.abs(val2 - val1) + Math.abs(val3 - val2);
		    } else {
			/*
			  double ival = integrateRootP2signed(u, a, b, c);
			  double X = rp.valueAt(u);
			  double val2 = array[1]*X*Math.sqrt(X)/(3*c)
			  + (array[0] - array[1]*b/(2*c))  *ival ;
			  X = rp.valueAt(0.0);
			  double val1 = array[1]*X*Math.sqrt(X)/(3*c);
			  return Math.abs(val2 - val1);
			*/
			double val = integral.valueAt(u) - integral.valueAt(0.0);
			return Math.abs(val);
		    }
		} catch(RuntimeException er) {
		    Throwable throwable = er.getCause();
		    if (throwable instanceof ArithmeticException) {
			throw (ArithmeticException) throwable;
		    } else {
			throw new ArithmeticException(er.getMessage());
		    }
		}
	    } else {
		throw new UnexpectedExceptionError();
	    }
	} else if (pdeg == 2) {
	    // rpdeg must be 0.
	    if (rpdeg != 0) {
		String msg = errorMsg("degreeError", pdeg, rpdeg);
		throw new ArithmeticException(msg);
	    }
	    double[] pa = p.getCoefficientsArray();
	    if (pa[2] < 0) {
		p.multiplyBy(-1.0);
	    }
	    double descr = pa[1]*pa[1] - 4 * pa[0]*pa[2];
	    double max = Math.max(Math.abs(pa[0]), Math.abs(pa[1]));
	    max = Math.max(max, Math.abs(pa[2]));
	    if (Math.abs(descr)/max < 1.e-12) descr = 0.0;
	    if (descr <= 0.0) {
		Polynomial integral = p.integral();
		return Math.sqrt(rp.valueAt(0))*Math.abs(integral.valueAt(u));
	    } else {
		double rdescr = Math.sqrt(descr);
		double a2 = 2*pa[2];
		double r1 = (-pa[1] - rdescr)/a2;
		double r2 = (-pa[1] + rdescr)/a2;
		if (r2 < r1) {
		    double tmp = 41;
		    r1 = r2;
		    r2 = tmp;
		}
		Polynomial integral = p.integral();
		// NOTE: integral.valueAt(0.0) = 0 for polynomials.
		if (r1 > 0 && r1 < u) {
		    double val1 = integral.valueAt(r1);
		    double sum = Math.abs(val1);
		    if (u <= r2) {
			sum += Math.abs(integral.valueAt(u) - val1);
			return Math.sqrt(rp.valueAt(0))*sum;
		    } else {
			double val2 = integral.valueAt(r2);
			sum += Math.abs(val2 - val1);
			sum += Math.abs(integral.valueAt(u) - val2);
			return Math.sqrt(rp.valueAt(0))*sum;
		    }
		} else {
		    return Math.sqrt(rp.valueAt(0))
			* Math.abs(integral.valueAt(u));
		}
	    }
	} else {
	    throw new UnexpectedExceptionError();
	}
    }

    /**
     * Exception to indicate that the polynomial passed to
     * {@link Polynomials#integrateRootP4(Polynomial,double,double)}
     * has a factor with whose minimum value is close to zero.
     * When this condition occurs, floating point errors may make the
     * computed value questionable.
     */
    public static class RootP4Exception extends ArithmeticException {
	RootP4Exception(String msg) {
	    super(msg);
	}
    }

    private static double rootP4Limit = /*1.e-6*/ 1.e-4;

    /**
     * Set the limit for the minimum value for a factor of
     * the polynomiial passed to
     * {@link Polynomials#integrateRootP4(Polynomial,double,double)}.
     * The polynomial, if factoring is successful, will have three
     * values: a 0 degree polynomial (a scalar), and two second-degree
     * polynomals whose x<sup>2</sup> term is 1.0.  Both factors must
     * be polynomials with positive values for all values of their
     * arguments. When the minimum is too close to zero compared to
     * the maximum of the absolute value of the polynomial's coefficients,
     * an exeception will be thrown as an indication that the results
     * are not reliable.
     * <P>
     * To turn off this test, set the limit to 0.0.  The default value
     * is 1.e-4.
     * @param value the value for the limit
     */
    public static void setRootP4Limit(double value) {
	rootP4Limit = value;
    }

    /**
     * Get the value set by {@link #setRootP4Limit(double)} or its
     * default value if {@link #setRootP4Limit(double)} has not been
     * called.
     * @return the value
     * @see #setRootP4Limit(double)
     */
    public static double getRootP4Limit() {
	return rootP4Limit;
    }

    private static boolean testRootP4 = false;

    /**
     * Test that factoring is accurate when integrateRootP4 is called.
     * @param value true if factoring should be tested; false otherwise.
     */
    public static void testRootP4(boolean value) {
	testRootP4 = value;
    }

    /**
     * Integrate the square root of a quartic polynomial that has no
     * real roots.
     * The value of the polynomial must be positive for all values of
     * its argument.
     * <P>
     * Note: this method can be used in computing the length of a
     * cubic B&eacute;zier curve.
     * @param p the polynomial whose square root is to be integrated
     * @param y the lower limit of integration
     * @param x the upper limit of integration
     * @throws NullPointerException an argument was null
     * @throws IllegalArgumentException if the polynomial is not a quartic
     *         polynomial
     * @exception ArithmeticException if factorization failed, probably
     *            because of a round-off error, or if the factors are not
     *            positive for all values of their arguments
     */
    public static double integrateRootP4(Polynomial p, double y, double x)
	throws IllegalArgumentException, ArithmeticException
    {
	if (p == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg"));
	}
	if (p.getDegree() != 4) {
	    throw new IllegalArgumentException(errorMsg("wrongDegree4"));
	}
	Polynomial[] factors = factorQuarticToQuadratics(p);
	double scale = (factors[0].getCoefficientsArray()[0]);

	if (testRootP4) {
	    Polynomial testp = new Polynomial(scale);
	    testp.multiplyBy(factors[1]);
	    testp.multiplyBy(factors[2]);
	    double[] ar1 = p.getCoefficientsArray();
	    double[] ar2 = testp.getCoefficientsArray();
	    double max = 1.0;
	    for (int i = 0; i < 5; i++) {
		max = Math.max(max, Math.abs(ar1[i]));
	    }
	    for (int i = 0; i < 5; i++) {
		if (Math.abs(ar1[i] - ar2[i]) / max > 1.e-8) {
		    double v1 = ar1[i];
		    double v2 = ar2[i];
		    String msg = errorMsg("badFactoring", v1, v2);
		    throw new ArithmeticException(msg);
		}
	    }
	}
	double mindescr = Double.POSITIVE_INFINITY;
	for (int i = 1; i < 3; i++) {
	    double[] farray = factors[i].getCoefficientsArray();
	    double val1 = 4*farray[0];
	    double val2 = farray[1]*farray[1];
	    if ( val1 <= val2 ) {
		String msg = errorMsg("badDescr", (val2 - val1));
		throw new ArithmeticException(msg);
	    } else {
		double minx = -farray[1]/2;
		double minValue = Math.abs(factors[i].valueAt(minx));
		double maxc = Math.max(1.0, Math.abs(farray[0]));
		maxc = Math.max (maxc, Math.abs(farray[1]));
		if (minValue/maxc < rootP4Limit) {
		    /*
		    double rdescr = Math.sqrt(val2 - val1);
		    double diff = Math.max((x - y)*10);1.e-
		    int iscale;
		    if (diff < 1.0 || diff < (double)Integer.MAX_VALUE) {
			iscale 10 + (int) Math.ceil(Math.log(diff/10));
		    } else {
			iscale = 10;
		    }
		    int delta = 10 * iscale;
		    if ((y < x && minx > y-rdescr && minx < x+rdescr)
			|| (y > x && minx > x-rdescr && minx < y + rdescr)) {
			System.out.println("rdescr = " + rdescr);
			delta = 50 * iscale;
		    }
		    GLQuadrature glq = new GLQuadrature(8) {
			    protected double function(double t) {
				return Math.sqrt(p.valueAt(t));
			    }
			};
		    return glq.integrate(y, x, delta);
		    */
		    throw new RootP4Exception(errorMsg("badMinValue"));
		}
	    }
	}
	return Math.sqrt(scale) *
		integrateRoot2Q(1, 1, 0, factors[1], factors[2], null, y, x);
    }

    /**
     * Integrate an expression containing the square roots of two
     * quadratic polynomials using elliptic integrals.
     * The integral is
     * <BLOCKQUOTE>
     * <SPAN style="font-size: 150%">&int;<sub><SPAN style="font-size:70%">y</SPAN></sub></SPAN><SPAN style="font-size: 200%">&#8239;<sup><SPAN style="font-size:150%;">x</SPAN></sup></SPAN>
     * A(t)<sup>p&#x2081;/2;</sup>B(t)<sup>p&#x2082;/2</sup>(a<sub>5</sub>+b<sub>5</sub>)<sup>p&#x2085;/2</sup> dt
     * </BLOCKQUOTE>
     * where A(t) = f<sub>1</sub> +g<sub>1</sub>t + h<sub>1</sub>t<sup>2</sup>
     * and B(t) = f<sub>2</sub> +g<sub>2</sub>t + h<sub>2</sub>t<sup>2</sup>
     * are quadratic polynomials whose values are
     * positive for all t, and where p&#x2081; and p&#x2082; are odd integers
     * and p&#x2085; is an even integer.  These integers must have the specific
     * values given in the following table:
     * <TABLE style="border: 2px solid black">
     * <CAPTION>&nbsp;</CAPTION>
     * <TR><TH>p&#x2081;</TH><TH>p&#x2082;</TH><TH>p&#x2085;</TH></TR>
     * <TR><TD>-1</TD><TD>-1</TD><TD>0</TD></TR>
     * <TR><TD>-3</TD><TD>1</TD><TD>0</TD></TR>
     * <TR><TD>-3</TD><TD>-1</TD><TD>0</TD></TR>
     * <TR><TD>-1</TD><TD>-1</TD><TD>-2</TD></TR>
     * <TR><TD>1</TD><TD>-1</TD><TD>-4</TD></TR>
     * <TR><TD>-1</TD><TD>-1</TD><TD>-4</TD></TR>
     * <TR><TD>-1</TD><TD>-1</TD><TD>2</TD></TR>
     * <TR><TD>1</TD><TD>-1</TD><TD>0</TD></TR>
     * <TR><TD>-1</TD><TD>-1</TD><TD>4</TD></TR>
    * <TR><TD>1</TD><TD>1</TD><TD>0</TD></TR>
     * <TR><TD>1</TD><TD>-1</TD><TD>-2</TD></TR>
     * <TR><TD>1</TD><TD>1</TD><TD>-2</TD></TR>
     * <TR><TD>1</TD><TD>1</TD><TD>-4</TD></TR>
     * <TABLE>
     * The caller must ensure that neither quadratic polynomials has a
     * real root.
     * <P>
     * See B. C. Carlson,
     * "<A HREF="https://www.ams.org/journals/mcom/1992-59-199/S0025-5718-1992-1134720-4/S0025-5718-1992-1134720-4.pdf">
     * A Table of Elliptic Integrals: Two  Quadratic Factors</A>",
     * Mathematics of Computation, Volume 59, Number 199, July 1992,
     * Pages 165&ndash;180 for further details and the algorithm used.
     * The notation for this method is based on this paper. While
     * Carlson's paper requires that x &gt; y, this implementation does not
     * have that restriction.
     * @param p1 the value of p<sub>1</sub> as provided in the previous table
     * @param p2 the value of p<sub>2</sub> as provided in the previous table
     * @param p5 the value of p<sub>3</sub> as provided in the previous table
     * @param P1 the polynomial A(t) described above
     * @param P2 the polynomial B(t) described above
     * @param P5 the polynomial a<sub>5</sub> + b<sub>5</sub>t; null if
     *        p5 is zero
     * @param y the lower limit of integration
     * @param  x the upper limit of integration
     * @return the value of the integral from y to x
     * @throws NullPointerException an argument was null
     * @throws IllegalArgumentException if an argument is out of range
     */

    public static double integrateRoot2Q(int p1, int p2, int p5,
					 Polynomial P1,
					 Polynomial P2,
					 Polynomial P5,
					 double y, double x)
	throws NullPointerException, IllegalArgumentException
    {
	// Check for cases allowed in B. Carlson, "A TABLE OF ELLIPTIC
	// INTEGRALS: TWO QUADRATIC FACTORS"
	// https://www.ams.org/journals/mcom/1992-59-199/S0025-5718-1992-1134720-4/S0025-5718-1992-1134720-4.pdf

	// See if we should exchange p1 and p2

	if (P1 == null || P2 == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (x == y) return 0.0;
	if (x < y) {
	    return -integrateRoot2Q(p1, p2, p5, P1, P2, P5, x, y);
	}

	if ((Math.abs(p1) % 2 == 0) || (Math.abs(p2) % 2 == 0)
	    || Math.abs(p5) %2 == 1) {
	    String msg = errorMsg("illegalCarlsonParms", p1, p2, p5);
	    throw new IllegalArgumentException();
	}

	boolean exchange = (p2 == -3 && p1 == 1)
	    || (p2 == -3 && p1 == -1)
	    || (p2 == 1 && p1 == -1);

	if (exchange) {
	    int tmp  = p1;
	    p1 = p2;
	    p2 = tmp;
	    Polynomial tmpp = P1;
	    P1 = P2;
	    P2 = tmpp;
	}

	double array[] = P1.getCoefficientsArray();
	double f1 = array[0];
	double g1 = array[1];
	double h1 = array[2];
	array = P2.getCoefficientsArray();
	double f2 = array[0];
	double g2 = array[1];
	double h2 = array[2];
	double a5 = 1.0;
	double b5 = 0.0;
	if (P5 != null) {
	    array = P5.getCoefficientsArray();
	    a5 = array[0];
	    b5 = array[1];
	    if (b5 == 0.0 && a5 == 1.0) p5 = 0;
	}

	if (!((p1 == 1 && p2 == 1 && p5 == 0)
	      || (p1 == 1 && p2 == 1 && p5 == -2))) {
	    if ((2*Math.abs(p1) + 2*Math.abs(p2) + Math.abs(p5)) > 8) {
		String msg = errorMsg("illegalCarlsonParms", p1, p2, p5);
		throw new IllegalArgumentException(msg);
	    } else if ((2*p1 + 2*p2 + p5 > 0)) {
		String msg = errorMsg("illegalCarlsonParms", p1, p2, p5);
		throw new IllegalArgumentException(msg);
	    }
	}

	double xi1 = Math.sqrt(f1 + g1*x + h1*x*x);
	double xi2 = Math.sqrt(f2 + g2*x + h2*x*x);
	double eta1 = Math.sqrt(f1 + g1*y + h1*y*y);
	double eta2 = Math.sqrt(f2 + g2*y + h2*y*y);
	double xi1p = (g1 + 2*h1*x)/(2*xi1);
	double eta1p = (g1 + 2*h1*y)/(2*eta1);
	double B = xi1p*xi2 - eta1p*eta2;
	double E = xi1p*xi1*xi1*xi2 - eta1p*eta1*eta1*eta2;
	double theta1 = xi1*xi1 + eta1*eta1 - h1*MathOps.pow(x-y, 2);
	double theta2 = xi2*xi2 + eta2*eta2 - h2*MathOps.pow(x-y, 2);

	double zeta1 = Math.sqrt(MathOps.pow(xi1+eta1,2)
				 - h1*MathOps.pow(x-y,2));
	double zeta2 = Math.sqrt(MathOps.pow(xi2+eta2,2)
				 - h2*MathOps.pow(x-y,2));
	double U = (xi1*eta2 + eta1*xi2)/(x-y);
	double M = zeta1*zeta2/(x-y);
	double delta11 = Math.sqrt(4*f1*h1 - g1*g1);
	double delta22 = Math.sqrt(4*f2*h2 - g2*g2);
	double delta12 = Math.sqrt(2*f1*h2 + 2*f2*h1 - g1*g2);

	double Delta = Math.sqrt(MathOps.pow(delta12, 4)
				 - delta11*delta11*delta22*delta22);
	double DeltaPlus = delta12*delta12 + Delta;
	double DeltaMinus = delta12*delta12 - Delta;
	double LplusSq  = M*M + DeltaPlus;
	double LminusSq = M*M + DeltaMinus;

	double G = 2*Delta*DeltaPlus*Functions.RD(M*M,LminusSq,LplusSq)/3
	    + Delta/(2*U)
	    + (delta12*delta12*theta1 - delta11*delta11*theta2)/(4*xi1*eta1*U);
	double RF = Functions.RF(M*M,LminusSq,LplusSq);
	if (p5 == 0) {
	    if (p1 == -1 && p2 == -1) {
		return 4*RF;
	    } else if (p1 == -3 && p2 == 1) {
		return 4*(-G + DeltaPlus*RF)/(delta11*delta11);
	    }
	}
	double Sigma = G - DeltaPlus*RF + B;
	double A1111 = xi1*xi2 - eta1*eta2;
	// double S = (M*M + delta12*delta12)/2 - U*U;
	double S = (xi1*eta1*theta2 + xi2*eta2*theta1)/MathOps.pow(x-y,2);
	// need these two for a few cases where p5 is not zero
	double psi0 = 0.0;
	double psi0sq = 0.0;
	double H0 = 0.0;
	double Lambda0 = 0.0;
	if (p5 == 0
	    || (p1 == -1 && p2 == -1 && p5 == 2)
	    || (p1 == -1 && p2 == -1 && p5 == 4)
	    || (p1 == 1 && p2 == -1 && p5 == -2)
	    || (p1 == 1 && p2 == 1 && p5 == -2)
	    || (p1 == 1 && p2 == 1 && p5 == -4)) {
	    Lambda0 = delta11*delta11*h2/h1;
	    psi0 = g1*h2 - g2*h1;
	    psi0sq = psi0*psi0;
	    if (p1 == -3 && p2 == -1) {
		double An111n1 = xi2/xi1 - eta2/eta1;
		return 8*h1*((Lambda0-delta12*delta12)*G/Delta
			     - (Lambda0-DeltaPlus)*RF)/(delta11*delta11*Delta)
		    -4*psi0*An111n1/(Delta*Delta);
	    }
	    double Omega0sq = M*M + Lambda0;

	    double X0 = -(xi1p*xi2 + eta1p*eta2)/(x - y);
	    double mu0 = h1/(xi1*eta1);
	    double T0 = mu0*S + 2*h1*h2;
	    double V0sq = mu0*mu0*(S*S + Lambda0*U*U);
	    double a0 = S*Omega0sq/U + 2*Lambda0*U;
	    double b0sq = (S*S/(U*U) + Lambda0)*Omega0sq*Omega0sq;
	    double a0sq = b0sq
		+ Lambda0*(DeltaPlus-Lambda0)*(Lambda0-DeltaMinus);
	    H0 = delta11*delta11*psi0
		* (Functions.RJ(M*M,LminusSq,LplusSq,Omega0sq)/3
		   + Functions.RC(a0sq,b0sq)/2)/(h1*h1)
		- X0*Functions.RC(T0*T0,V0sq);
	    if (p1 == 1 && p2 == 1 && p5 == 0) {
		return (delta22*delta22/(h2*h2) - delta11*delta11/(h1*h1))
		    * (psi0*H0 + (Lambda0 - delta12*delta12)*RF)/8
		    - ((3*psi0sq - 4*h1*h2*delta12*delta12)
		       *(Sigma + delta12*delta12*RF)/(24*h1*h1*h2*h2))
		    + (Delta*Delta*RF -psi0*A1111)/(12*h1*h2)
		    + E/(3*h1);
	    } else if (p1 == 1 && p2 == -1 && p5 == 0) {
		return (psi0*H0 + Sigma + Lambda0*RF)/h2;
	    }
	}
	// p5 != 0
	double alpha15 = 2*f1*b5 - g1*a5;
	double alpha25 = 2*f2*b5 - g2*a5;
	double beta15 = g1*b5 -2*h1*a5;
	double beta25 = g2*b5 -2*h2*a5;
	double gamma1 = (alpha15*b5 - beta15*a5)/2;
	double gamma2 = (alpha25*b5 - beta25*a5)/2;
	double Lambda = delta11*delta11*gamma2/gamma1;
	double OmegaSq = M*M + Lambda;
	double psi = (alpha15*beta25 - alpha25*beta15)/2;
	double psisq = psi*psi;
	/*
	double psisq = gamma1*gamma1
	    * (DeltaPlus - Lambda)*(Lambda - DeltaMinus)/Lambda;
	*/
	double xi5 = a5 + b5*x;
	double eta5 = a5 + b5*y;
	double An111n1 = xi2/xi1 - eta2/eta1;
	double A1111n4 = xi1*xi2/(xi5*xi5) - eta1*eta2/(eta5*eta5);
	double X = xi5*eta5
	    * (theta1*An111n1/2 - xi5*eta5*A1111n4)/MathOps.pow(x-y,2);
	double mu = gamma1*xi5*eta5/(xi1*eta1);
	double T = mu*S + 2*gamma1*gamma2;
	double Vsq = mu*mu*(S*S + Lambda*U*U);
	double a = S*OmegaSq/U + 2*Lambda*U;
	double bsq = (S*S/(U*U) + Lambda)*OmegaSq*OmegaSq;
	// double asq = bsq + Lambda*Lambda*psisq/(gamma1*gamma2);
	double asq = a*a;
	double H = delta11*delta11*psi
	    * (Functions.RJ(M*M,LminusSq,LplusSq,OmegaSq)/3
	       + Functions.RC(asq,bsq)/2)/(gamma1*gamma1)
	    - X*Functions.RC(T*T, Vsq);

	if (p1 == -1 && p2 == -1 && p5 == -2) {
	    return -2*(b5*H + beta15*RF/gamma1);
	} else if (p1 == 1 && p2 == -1 && p5 == -4) {
	    double An111n1n2 = xi2/(xi1*xi5) - eta2/(eta1*eta5);
	    return (psi*H + G + (Lambda - DeltaPlus)*RF)/gamma2
		- (beta15*An111n1 + 2*gamma1*An111n1n2)/(2*b5*gamma2);
	} else if (p1 == -1 && p2 == -1 && p5 == -4) {
	    double A1111n2 = xi1*xi2/xi5 - eta1*eta2/eta5;
	    return b5*(beta15/gamma1 + beta25/gamma2)*H
		+ beta15*beta15*RF/(gamma1*gamma1)
		+ b5*b5*(Sigma - b5*A1111n2)/(gamma1*gamma2);
	} else if (p1 == -1 && p2 == -1 && p5 == 2) {
	    return 2*b5*H0 -2*beta15*RF/h1;
	} else if (p1 == -1 && p2 == -1 && p5 == 4) {
	    return -b5*(beta15/h1 + beta25/h2)*H0
		+ b5*b5*Sigma/(h1*h2) + beta15*beta15*RF/(h1*h1);
	} else if (p1 == 1 && p2 == -1 && p5 == -2) {
	    return 2*(-gamma1*H + h1*H0)/b5;
	} else if (p1 == 1 && p2 == 1 && p5 == -2) {
	    double b5cubed = MathOps.pow(b5, 3);
	    return -2*gamma1*gamma2*H/b5cubed
		+ ((h1*gamma2 + h2*gamma1)/b5cubed
		  - psi0sq/(4*h1*h2*b5))*H0
		+ (beta15/h1 + beta25/h2)*(Sigma + Lambda0*RF)/(4*b5*b5)
		- delta11*delta11*psi0*RF/(2*h1*h1*b5)
		+ A1111/(2*b5);
	} else if (p1 == 1 && p2 == 1 && p5 == -4) {
	    double b5sq = b5*b5;
	    double b5cubed = b5sq*b5;
	    double A1111n2 = xi1*xi2/xi5 - eta1*eta2/eta5;
	    return (-(gamma1*beta25 + gamma2*beta15)*H
		    +(h1*beta25 + h2*beta15)*H0) / b5cubed
		+(2*Sigma + (Lambda + Lambda0)*RF)/b5sq
		- A1111n2/b5;
	}
	String msg = errorMsg("illegalCarlsonParms", p1, p2, p5);
	throw new IllegalArgumentException(msg);
    }

    private static Polynomial root1pzm1 = new
	Polynomial (0.0, 0.5, -1.0/8.0, 1/16.0, -5/128.0);

    /**
     * Compute an indefinite integral of the square root of the
     * polynomial a + bx + cx<sup>2</sup>.
     * The polynomial must have a non-negative value at x=0 and at the
     * upper limit of integration.
     * @param x the upper limit of integration
     * @param a the zeroth order coefficient of a polynomial
     * @param b the first order coefficient of a polynomial
     * @param c the second order coefficient of a polynomial
     * @return the value of the integral
     * @throws ArithmeticException if the integral cannot be computed
     */
    public static double integrateRootP2(double x, double a, double b, double c)
	throws ArithmeticException
    {
	// a + bx +cx^2 to fit CRC handbook integral tables

	if (x == 0.0) return 0.0;
	double cxsq = c*x*x;
	double bx = b*x;

	double tv1 = Math.min(Math.abs(a), Math.abs(a+bx));

	if (a > 0.0 && c != 0.0 &&  Math.abs(cxsq)/tv1 < 0.001) {
	    // We can approximate sqrt(a + bx + cx^2) by
	    // sqrt(a+bx)sqrt(1 - cx^2/(a+bx)) which is approximately
	    // sqrt(a+bx) -(1/2)cx^2/sqrt(a+bx) and integrate that.
	    double z = b*x/a;
	    double abx = 1 + z;
	    double a32 = MathOps.pow(a,3,2);
	    double a52 = a*a32;
	    double asq = a*a;
	    double bsq = b*b;
	    double bcubed = bsq*b;
	    double roota = Math.sqrt(a);
	    double rootabx = Math.sqrt(abx);
	    boolean test = Math.abs(b*x/a) <= 0.001;
	    double rootabxm1;
	    if (test) {
		// for small z, use a taylor series, keeping the first
		// few terms.
		rootabxm1 = root1pzm1.valueAt(z);
	    } else {
		rootabxm1 = rootabx - 1.0;
	    }

	    double term1 = (2*a32/(3*b) + 8*c*a52/(15*bcubed)) * rootabxm1;

	    double term2 = ((2*roota/3 - 4*a32*c/(15*bsq)) * x
			    + 3*c*x*x*roota/(15*b)) * rootabx;

	    return term1 + term2;
	}

	if (c == 0.0) {
	    // CRC Standard Math Tables, page 291
	    if (a < 0.0) {
		throw new ArithmeticException(errorMsg("notIntegrableAC", a));
	    }
	    if (b == 0.0) {
		return Math.sqrt(a)*x;
	    }
	    if (a + b*x < 0.0) {
		throw new
		    ArithmeticException(errorMsg("notIntegrableABXC", a, b, x));
	    }
	    if (Math.abs(b*x/a) < .001) {
		// use a Tayler series - the constant terms cancel.
		double z = b*x/a;
		double scale = 2*Math.sqrt(a)/(3*b);
		Polynomial p = new Polynomial(a,a);
		p.multiplyBy(new Polynomial(1, 0.5, -1/8.0,
					    1/16.0, -5.0/128.0, 7.0/256.0));
		p.getCoefficientsArray()[0] = 0.0;
		return scale*p.valueAt(z);
	    } else {
		return (2.0/(3.0*b))
		    * (MathOps.pow(a+b*x, 3, 2) - MathOps.pow(a, 3, 2));
	    }
	}
	// CRC Standard Math Tables, page 297
	double qterm1 = 4*a*c;
	double qterm2 = b*b;
	// double q = 4*a*c - b*b;
	double q = qterm1 - qterm2;
	double qtest = Math.abs(q) / Math.max(Math.abs(qterm1), qterm2);
	if (qtest < 1.e-10) {
	    // reduces to a polynomial: integrate sqrt(c)(x-r) where
	    // r = -b/2c.  The integral is sqrt(c)(x*x/2 - rx).
	    // if (c < 0) throw new Exception("cannot integrate");
	    double rc = Math.sqrt(c);
	    double r =-b/(2*c);
	    if (0 < r && r < x) {
		// double term1 = (rc*r*r + r*b/rc)/2;
		double term1 = -rc*r*r/2;
		//double term2 = (rc*x*x + x*b/rc)/2 - term1;
		double term2 = rc*(x*x/2 -r*x) - term1;
		return Math.abs(term1) + Math.abs(term2);
	    } else if (x < r && r < 0) {
		// double term1 = (rc*r*r + r*b/rc)/2;
		double term1 = -rc*r*r/2;
		// double term2 = (rc*x*x + x*b/rc)/2 - term1;
		double term2 = rc*(x*x/2 -r*x) - term1;
		return -(Math.abs(term1) + Math.abs(term2));
	    } else {
		// return (rc*x*x + x*b/rc)/2;
		double term = Math.abs(rc*(x*x/2 - r*x));
		return (x >= 0)? term: -term;
	    }
	}
	double expr = a + b*x + c*x*x;
	if (expr < 0) {
	    String msg = errorMsg("notIntegrableXNeg", x);
	    throw new ArithmeticException(msg);
	}
	if (a < 0) {
	    String msg = errorMsg("notIntegrableXZero");
	    throw new ArithmeticException(msg);
	}
	double root = Math.sqrt(expr);
	double term1 = ((2*c*x + b)*root - b*Math.sqrt(a))/(4*c);
	double kinverse = q/(4*c);
	double iexpr;
	if (c > 0) {
	    double rootc = Math.sqrt(c);
	    // iexpr = Math.log(root + x*rootc+ b/(2*rootc)) / Math.sqrt(c);
	    // Seethe CRC integral table (Integrals 181 & 182,
	    // October 1963 printing)
	    // Also see
	    // https://www.math.stonybrook.edu/~bishop/classes/math126.F20/CRC_integrals.pdf
	    if (q > 0) {
		// no real roots.
		iexpr = (Functions.asinh((2*c*x + b)/Math.sqrt(q))
			 - Functions.asinh(b/Math.sqrt(q)))
		    / Math.sqrt(c);
	    } else {
		double rnq = Math.sqrt(-q);
		double c2  = c*2;
		double r1 = (-b - rnq)/c2;
		double r2 = (-b + rnq)/c2;
		if (x > 0) {
		    if (r1 > 0 && r1 < x) {
			String msg = errorMsg("notIntegrableRoot",r1, x);
			throw new ArithmeticException(msg);
		    } else if (r2 > 0 && r2 < x) {
			String msg = errorMsg("notIntegrableRoot", r2, x);
			throw new ArithmeticException (msg);
		    }
		} else if (x < 0) {
		    if (r2 < 0 && r2 > x) {
			String msg = errorMsg("notIntegrableRoot", r2, x);
			throw new ArithmeticException (msg);
		    } else if (r1 < 0 && r1 > x) {
			String msg = errorMsg("notIntegrableRoot",r1, x);
			throw new ArithmeticException(msg);
		    }
		}
		double bcval = b/(2*rootc);
		double bcvalabs = Math.abs(bcval);
		double logterm = root + x*rootc;
		if ((Math.abs(a) + Math.abs(b) + Math.abs(c)) < bcvalabs) {
		    iexpr = (Math.log1p(logterm/bcval)
			     - Math.log1p(Math.sqrt(a)/bcval))
			     / Math.sqrt(c);
		} else {
		    double logarg = logterm + bcval;
		    if (logarg < 0.0) {
			logarg = -logarg;
		    }
		    double logarg0 = Math.sqrt(a) + bcval;
		    if (logarg0 < 0.0) logarg0 = -logarg0;
		    iexpr = (Math.log(logarg) - Math.log(logarg0))
			     / Math.sqrt(c);
		}
	    }
	} else {
	    // c < 0 so for large positive or negative x, the polynomial is
	    // negative.  We can only integrate if x is between the
	    // two roots, and it must have two roots.
	    if (q > 0) {
		// values all negative because c < 0, so square root
		// is not real
		String msg = errorMsg("notIntegrableNoRoots", c);
		throw new ArithmeticException (msg);
	    } else {
		double rnq = Math.sqrt(-q);
		double c2  = c*2;
		double r1 = (-b - rnq)/c2;
		double r2 = (-b + rnq)/c2;
		if (x < r1 || x > r2) {
		    String msg = errorMsg("notIntegrableRoots2", r1, r2, x);
		    throw new ArithmeticException (msg);
		}
	    }
	    iexpr = (Math.asin((-2*c*x - b)/Math.sqrt(-q))
		     - Math.asin(-b/Math.sqrt(-q)))
		/ Math.sqrt(-c);
	}
	double term2 = kinverse*iexpr/2;
	return term1 + term2;
    }

    /**
     * Compute an indefinite integral of x times the square root of the
     * polynomial a + bx + cx<sup>2</sup>.
     * The polynomial must have a non-negative value at x=0 and at the
     * upper limit of integration.
     * @param x the upper limit of integration
     * @param a the zeroth order coefficient of a polynomial
     * @param b the first order coefficient of a polynomial
     * @param c the second order coefficient of a polynomial
     * @return the value of the integral
     * @throws ArithmeticException if the integral cannot be computed
     */
    public static double integrateXRootP2(double x,
					  double a, double b, double c)
	throws ArithmeticException
    {
	// a + bx +cx^2 to fit CRC handbook integral tables
	if (x == 0.0) return 0.0;


	/*
	// NOT YET WORKING & not sure why, so comment out until
	// the reason for the discrepancy is found. Oddly, similar
	// code with the same a, b, c works for integrateRootP2.

	double cxsq = c*x*x;
	double bx = b*x;

	double tv1 = Math.min(Math.abs(a), Math.abs(a+bx));
	System.out.println("tv1 = " + tv1);
	System.out.println("cxsq = " + cxsq);
	if (a > 0.0 && c != 0.0 &&  Math.abs(cxsq)/tv1 < 0.001) {
	    // We can approximate sqrt(a + bx + cx^2) by
	    // sqrt(a+bx)sqrt(1 - cx^2/(a+bx)) which is approximately
	    // sqrt(a+bx) -(1/2)cx^2/sqrt(a+bx) and integrate that.
	    double z = b*x/a;
	    double abx = 1 + z;
	    double roota = Math.sqrt(a);
	    double rootabx = Math.sqrt(abx);
	    double asq = a*a;
	    double bsq = b*b;
	    double bcubed =bsq*b;
	    double xsq = x*x;
	    double ab = a*b;
	    boolean test = Math.abs(b*x/a) <= 0.001;
	    double rootabxm1;
	    if (test) {
		// for small z, use a taylor series, keeping the first
		// few terms.
		rootabxm1 = root1pzm1.valueAt(z);
	    } else {
		rootabxm1 = rootabx - 1.0;
	    }

	    double term1 = (-2*(2*asq*Math.sqrt(a)/(15*bsq))
			    - (c)*(6*a/(7*b))*((8*asq)/(15*bcubed))
			    * Math.sqrt(a))*(rootabxm1);

	    double term2 =  (-2*(-(ab*x+3*bsq*xsq)*Math.sqrt(a)/(15*bsq))
			     + (c)*(xsq*x/(7*b) - (6*a/(7*b))
				    *((-4*ab*x + 3*bsq*xsq)/(15*bcubed)))
			     * Math.sqrt(a))*rootabx;

	    return term1 + term2;
	}
	*/
	if (c == 0.0) {
	    // CRC Standard Math Tables, page 291
	    if (a < 0.0) {
		String msg = errorMsg("notIntegrableAC", a);
		throw new ArithmeticException(msg);
	    }
	    if (b == 0.0) {
		return Math.sqrt(a)*x*x/2;
	    }
	    if (a + b*x < 0.0) {
		String msg = errorMsg("notIntegrableABXC", a, b, x);
		throw new ArithmeticException(msg);
	    }
	    // integrating x sqrt(a+bx): in CRC tables
	    // return -(2*(2*a - 3*b*x)*MathOps.pow(a+b*x, 3, 2)) / (15*b*b);
	    if (Math.abs(b*x/a) < 0.001) {
		// use a taylor series expansion due to numerical accuracy
		// issues.
		double scale = 2*Math.sqrt(a)/(15*b*b);
		double z = b*x/a;
		Polynomial p0 = new Polynomial(2*a*a);
		Polynomial p1 = new Polynomial(a,a);
		Polynomial p2 = new Polynomial(2*a,-3*a);
		Polynomial p3 = new Polynomial(1, 0.5, -1/8.0,
					       1/16.0, -5.0/128.0, 7.0/256.0);
		p1.multiplyBy(-1.0);
		p1.multiplyBy(p2);
		p1.multiplyBy(p3);
		p0.incrBy(p1);
		return scale*p0.valueAt(z);
	    } else {
		return (2/(15*b*b))*(2*a*MathOps.pow(a,3,2)
				     -(2*a-3*b*x)*MathOps.pow(a+b*x, 3, 2));
	    }
	}
	double qterm1 = 4*a*c;
	double qterm2 = b*b;
	// double q = 4*a*c - b*b;
	double q = qterm1 - qterm2;
	double qtest = Math.abs(q) / Math.max(Math.abs(qterm1), qterm2);
	if (qtest < 1.e-10) {
	    // r = -b /(2*c);
	    // polynomial = c*(x-r)^2 so integrate  x*sqrt(c)*(x-r)
	    // = sqrt(c)*(x*x -r*x)
	    // ==> sqrt(c)(x*x*x/3 -r*x*x/2)
	    if (c < 0.0) {
		String msg = errorMsg("notIntegrableNoRoots", c);
		throw new ArithmeticException(msg);
	    }
	    double rootc = Math.sqrt(c);
	    double r =-b/(2*c);
	    double r2 = r*r;
	    double r3 = r*r*r;
	    double x2 = x*x;
	    double x3 = x2*x;
	    if (x >= 0) {
		if (0 < r && r < x) {
		    double term1 = rootc*(r3/6.0);
		    double term2 = rootc*(x3/3.0 -r*x2/2) - term1;
		    return Math.abs(term1) + Math.abs(term2);
		} else {
		    return Math.abs(rootc*(x3/3.0 -r*x2/2));
		}
	    } else {		// x < 0
		if (x < r && r < 0) {
		    // double term1 = rootc*(r3/3.0 -r*r2/2);
		    double term1 = -rootc*r3/6;
		    double term2 = rootc*(x3/3.0 -r*x2/2) ;
		    return Math.abs(term1) + Math.abs(term2);
		} else  {
		    return Math.abs(rootc*(x3/3.0 -r*x2/2));
		}
	    }
	} else {
	    // CRC Tables have the integral of x sqrt(X) where
	    // X = a + bx +cx^2
	    double X = a + b*x + c*x*x;
	    double k = 4*c/q;
	    if (X < 0.0) {
		String msg = errorMsg("notIntegrableXNeg", x);
		throw new ArithmeticException(msg);
	    }
	    if (a < 0) {
		String msg = errorMsg("notIntegrableXZero");
		throw new ArithmeticException(msg);
	    }
	    // integral of x sqrt(X) = X sqrtX/(3c) -(b/(2c) integral sqrt(X)
	    // but integral of sqrt(X) is
	    // (2cx+b)sqrt(X)/(4c) + (1/2k) integral of 1/sqrt(X).
	    // so we have a polynomial times sqrt(X) - (b/(4kc)(integral
	    // 1/sqrt(X)).  The polynomial is
	    // (1/3c)(a-3b^2/(8c) + (b/4)x + cx^2)
	    double term0 = a-3*b*b/(8*c);
	    double term =  term0 + b*x/4 + c*x*x;
	    double rootX = Math.sqrt(X);
	    double term1 = (term*rootX - term0*Math.sqrt(a))/(3*c);
	    double rootc = Math.sqrt(c);
	    double iexpr;
	    // following copied (without comments) from rootP2 case.
	    if (c > 0) {
		if (q > 0) {
		    // no real roots.
		    iexpr = (Functions.asinh((2*c*x + b)/Math.sqrt(q))
			     - Functions.asinh(b/Math.sqrt(q)))
			/ rootc;
		} else {
		    double rnq = Math.sqrt(-q);
		    double c2  = c*2;
		    double r1 = (-b - rnq)/c2;
		    double r2 = (-b + rnq)/c2;
		    if (r2 < r1) {
			double tmp = r1;
			r1 = r2;
			r2 = tmp;
		    }
		    if (x > 0) {
			if (r1 >= 0 && r1 < x) {
			    String msg = errorMsg("notIntegrableRoot", r1, x);
			    throw new ArithmeticException (msg);
			} else if (r2 > 0 && r2 < x) {
			    String msg = errorMsg("notIntegrableRoot", r2, x);
			    throw new ArithmeticException (msg);
			}
		    } else if (x < 0) {
			if (r2 < 0 && r2 > x) {
			    String msg = errorMsg("notIntegrableRoot", r2, x);
			    throw new ArithmeticException (msg);
			} else if (r1 < 0 && r1 > x) {
			    String msg = errorMsg("notIntegrableRoot", r1, x);
			    throw new ArithmeticException (msg);
			}
		    }
		    double bcval = b/(2*rootc);
		    double bcvalabs = Math.abs(bcval);
		    double logterm = rootX + x*rootc;
		    if ((Math.abs(a) + Math.abs(b) + Math.abs(c)) < bcvalabs) {
			iexpr = (Math.log1p(logterm/bcval)
				 - Math.log1p(Math.sqrt(a)/bcval)) / rootc;
		    } else {
			double logarg = logterm + bcval;
			double logarg0 = Math.sqrt(a) + bcval;
			if (logarg < 0.0) {
			    logarg = -logarg;
			}
			if (logarg0 < 0.0) {
			    logarg0 = -logarg0;
			}
			iexpr = (Math.log(logarg) - Math.log(logarg0)) / rootc;
		    }
		}
	    } else {
		if (q > 0) {
		    String msg = errorMsg("notIntegrableNoRoots", c);
		    throw new ArithmeticException (msg);
		} else {
		    double rnq = Math.sqrt(-q);
		    double c2  = c*2;
		    double r1 = (-b - rnq)/c2;
		    double r2 = (-b + rnq)/c2;
		    if (r2 < r1) {
			double tmp = r1;
			r1 = r2;
			r2 = tmp;
		    }

		    if (x < r1 || x > r2) {
			String msg = errorMsg("notIntegrableRoots2", r1,r2,x);
			throw new ArithmeticException (msg);
		    }
		}
		double sqrtnq = Math.sqrt(-q);
		double argx = (-2*c*x - b)/sqrtnq;
		double arg0 = -b/sqrtnq;
		if (((1.0 - argx < 0.001) && (1.0 - arg0 < 0.001))
		    ||(( 1 + argx < 0.001) && (1 + arg0 < 0.001))) {
		    String msg = errorMsg("notIntegrableAsin");
		    throw new ArithmeticException(msg);
		} else {
		    iexpr = (Math.asin((-2*c*x - b)/Math.sqrt(-q))
			     - Math.asin(-b/Math.sqrt(-q)))/Math.sqrt(-c);
		    if (iexpr == -0.0) iexpr = 0.0;
		}
	    }
	    double term2 = b*iexpr/(4*k*c);
	    return term1 - term2 ;
	}
    }
}

//  LocalWords:  p's  isQuotient ne exbundle monomial th nullArg pn
//  LocalWords:  NullPointerException IllegalArgumentException Bezier
//  LocalWords:  secondArgNegI argArrayTooShort argArray TooShortNN
//  LocalWords:  argNonNegative ipj Bezierpolynomial bezier PRE nq nr
//  LocalWords:  BezierPolynomial illformedPolynomial BLOCKQUOTE qp
//  LocalWords:  getDegree sameArray TooShort quartic cx dx HREF kp
//  LocalWords:  Brookfield ndash reducedFormShift notQuartic eacute
//  LocalWords:  factorQuarticToQuadratics ArithmeticException zier
//  LocalWords:  notReducedQuartic noFactors dt px nbsp psisq asq bsq
//  LocalWords:  illegalCarlsonParms DeltaPlus DeltaMinus
