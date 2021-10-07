package org.bzdev.math;
import java.util.Arrays;

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
     * into the array beta.
     * @param beta an array that will contain the coefficients using a
     *        Bernstein basis; null if a new array will be allocated
     * @param offset the offset into the array for the 0th Bernstein
     *        coefficient
     * @param a the polynomial's coefficients using a monomial basis.
     * @param n the degree of the polynomial
     * @exception NullPointerException an argument was null
     * @exception IllegalArgumentException an array had the wrong size or
     *            the arguments offset or n had an incorrect value
     */
    public static double[] toBezier(double[] beta, int offset,
				   double[] a, int n)
    {
	if (beta == null) {
	    beta = new double[n+1 + offset];
	}
	if (a == null) {
	    throw new NullPointerException();
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
	    throw new NullPointerException();
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
     * Multiply one Bezier polynomial by another.
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
     */
    public static Polynomial multiply(Polynomial result,
				       Polynomial p1,
				       Polynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
     * Multiply one Bezier polynomial by another, storing the results in an
     * existing Bezier polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the product of p1 and p2 (i.e., the result argument)
     */
    public static BezierPolynomial multiply(BezierPolynomial result,
					    BezierPolynomial p1,
					    BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
     */
    public static int multiply(double[] result,
			       double[] p1, int n1,
			       double[] p2, int n2)
	throws IllegalArgumentException
    {
	if (n1 < 0) {
	    String msg = errorMsg("argNonNegative3", n1);
	    throw new IllegalArgumentException(msg);
	}
	if (n2 < 0) {
	    String msg = errorMsg("argNonNegative5", n2);
	    throw new IllegalArgumentException(msg);
	}
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
     * Multiply a Bezier polynomial by a scalar.
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
     */
    public static Polynomial add(Polynomial result,
				 Polynomial p1,
				 Polynomial p2)
	throws NullPointerException
    {

	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
     * Add one Bezier polynomial to another, storing the results in an
     * existing Bezier polynomial.
     * @param result the polynomial holding the results; null if a new one
     *        should be allocated
     * @param p1 the first polynomial
     * @param p2 the second polynomial
     * @return the sum of p1 and p2 (i.e., the result argument)
     */
    public static BezierPolynomial add(BezierPolynomial result,
				 BezierPolynomial p1,
				 BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
    public static int add(double[] result, double[] p1, int n1,
			  double[] p2, int n2, boolean prune)
    {
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

    public static int bezierAdd(double[] result, double[] p1, int n1,
				double[] p2, int n2)
    {
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
     * @link Polynomials#divide(double[],double[],double[],int,double[],int)}l
     * for documentation regarding numerical accuracy.
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @param isQuotient true if the polynomial returned is a quotient;
     *        false if it is a remainder
     * @return the quotient or the remainder of p1 divided by p2
     */
    public static Polynomial divide(Polynomial p1, Polynomial p2,
				    boolean isQuotient)
    {
	Polynomial result = new Polynomial();
	if (isQuotient) {
	    divide(result, null, p1, p2);
	} else {
	    divide(null, result, p1, p2);
	}
	return result;
    }
    
    /**
     * Divide one Bezier polynomial by another, returning either the
     * quotient or the remainder
     * <P>
     * Please see
     * @link Polynomials#divide(double[],double[],double[],int,double[],int)}l
     * for documentation regarding numerical accuracy (this method is used
     * by the implementation of this operation for instances of
     * BezierPolynomial).
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     * @param isQuotient true if the polynomial returned is a quotient;
     *        false if it is a remainder
     * @return the quotient or the remainder of p1 divided by p2
     */
    public static BezierPolynomial divide(BezierPolynomial p1,
					  BezierPolynomial p2,
					  boolean isQuotient)
    {
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
     * @link Polynomials#divide(double[],double[],double[],int,double[],int)}l
     * for documentation regarding numerical accuracy.
     * @param q a polynomial used to store the quotient; null if
     *        the quotient is not wanted
     * @param r the polynomial used to store the remainder; null if
     *        the remainder is not wanted
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     */
    public static void divide(Polynomial q, Polynomial r,
			      Polynomial p1, Polynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
     * Divide one Bezier polynomial by another, computing both the quotient and
     * the remainder.
     * Please see
     * @link Polynomials#divide(double[],double[],double[],int,double[],int)}l
     * for documentation regarding numerical accuracy (this method is used
     * by the implementation of this operation for instances of
     * BezierPolynomial).
     * @param q a polynomial used to store the quotient; null if
     *        the quotient is not wanted
     * @param r the polynomial used to store the remainder; null if
     *        the remainder is not wanted
     * @param p1 the first polynomial (the dividend)
     * @param p2 the second polynomial (the divisor)
     */
    public static void divide(BezierPolynomial q, BezierPolynomial r,
			      BezierPolynomial p1, BezierPolynomial p2)
	throws NullPointerException
    {
	if (p1 == null || p2 == null) {
	    throw new NullPointerException();
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
     * <BLOCKQUOTE><CODE><PRE>
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
     * </PRE></CODE></BLOCKQUOTE>
     * will produce the following values for q and r:
     * <BLOCKQUOTE><CODE><PRE>
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
     * </PRE></CODE></BLOCKQUOTE>
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
     */
    public static int divide(double q[], double[] r,
			     double[] p1, int n1, double[] p2, int n2)
	throws IllegalArgumentException
    {
	if  (q == r) throw new IllegalArgumentException(errorMsg("sameArray"));
	if (r == null || r.length < n1+1) {
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
     *
     */
    public static int getDegree(double[] r, int n2) {
	int n = n2;
	while (n > 0) {
	    if (r[n] != 0.0) break;
	    n--;
	}
	return n;
    }
}

//  LocalWords:  p's  isQuotient ne
