package org.bzdev.math;
import java.util.Arrays;
import org.bzdev.lang.MathOps;

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
		result = new Polynomial();
	    } else {
		result.softReset(-1);
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
	if (p == null || p.getDegree() != 4) {
	    throw new IllegalArgumentException(errorMsg("nullArg"));
	}
	Polynomial[] factors = factorQuarticToQuadratics(p);
	double scale = (factors[0].getCoefficientsArray()[0]);

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
		throw new ArithmeticException("factoring not accurate: "
					      + ar1[i] + " != "
					      + ar2[i]);
	    }
	}
	for (int i = 1; i < 3; i++) {
	    double[] farray = factors[i].getCoefficientsArray();
	    if (4*farray[0] <= farray[1]*farray[1]) {
		throw new ArithmeticException("cannot integrate");
	    }
	}
	return Math.sqrt(scale)*
	    integrateRoot2Q(1, 1, 0, factors[1], factors[2], null,
			    y, x);
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
