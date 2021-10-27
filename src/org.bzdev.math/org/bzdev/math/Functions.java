package org.bzdev.math;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import org.bzdev.lang.MathOps;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class implementing elementary and special functions.
 * Nested classes are used to define classes associated with particular
 * functions.  Methods directly implemented in this class are
 * provided for common cases.  In some cases, the usual Java convention
 * in which methods begin with a lower-case letter is not followed, favoring
 * standard mathematical notation instead.
 * <P>
 * The functions provided are
 * <UL>
 *  <li> Airy functions:
 *       <UL>
 *          <li> Ai(x) - computes the Airy function Ai(x).
 *          <li> Bi(x) - computes the Airy function Bi(x).
 *          <li> dAidx(x) - computes the derivative Ai'(x) of the Airy
 *                       function Ai(x).
 *          <li> dBidx(x) - computes the derivative Bi'(x) of the Airy
 *                       function Bi(x).
 *       </UL>
 *  <li> Bernstein Polynomials:
 *       <UL>
 *          <li> B(i,n,x) - computes a Bernstein polynomial of degree n
 *                       and order i at a point x. These polynomials are
 *                       defined as follows:
 * <blockquote>
 * <pre>
 *                       /n\
 *   Bernstein(i,n,x) = |   |x<sup>i</sup>(1-x)<sup>n-i</sup>
 *                       \i/
 * </pre>
 * </blockquote>
 *          <li> dBdx(i,n,x) - computes the first derivative with respect
 *               to x of Bernstein(i,n,x).
 *          <li> d2Bdx2(i,n,x) - computes the second derivative with respect
 *               to x of B(i,n,x).
 *          <li> B(n, &lambda;, &tau;...) - computes a Bernstein polynomial for
 *               barycentric coordinates, given by
 * <blockquote>
 * <pre>
 *
 *                 n!
 *    B<sup>n</sup><sub>&lambda;</sub>(&tau;) = __________ &tau;<sub>0</sub><sup>&lambda;<sub>0</sub></sup>&tau;<sub>1</sub><sup>&lambda;<sub>1</sub></sup>...&tau;<sub>m-1</sub><sup>&lambda;<sub>m-1</sub></sup>
 *            &lambda;<sub>1</sub>!&lambda;<sub>2</sub>!...&lambda;<sub>m</sub>!
 * </pre>
 * </blockquote>
 *               where m is the length of the vectors &lambda; and
 *               &tau;, and where the sum of the components of
 *               &lambda; must be 1. Barycentric coordinates are also
 *               defined so that the sum of the components of &tau;
 *               is 1, but this constraint must be satisfied by the
 *               caller.
 *          <li> dBdx(i, n, &lambda;, &tau;...) - computes
 *               &part;B<sup>n</sup><sub>&lambda;</sub>(&tau;) / &part;&tau;<sub>i</sub>.
 *          <li> d2Bdxdy(i, j n, &lambda;, &tau;...) - computes
 *               &part;<sup>2</sup>B<sup>n</sup><sub>&lambda;</sub>(&tau;) / &part;&tau;<sub>i</sub>&part;&tau;<sub>j</sub>.  If i = j, the value is
 *               &part;<sup>2</sup>B<sup>n</sup><sub>&lambda;</sub>(&tau;) / &part;&tau;<sub>i</sub><sup>2</sup>
 *       </UL>
 *  <li> Bessel functions, spherical Bessel functions, and modified
 *       Bessel functions, each of the first and second kind:
 *       <UL>
 *          <li> J(n,x) - computes J<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> dJdx(n,x) - computes d[J<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *          <li> Y(n,x) - computes Y<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> dYdx(n,x) - computes d[Y<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *          <li> j(n,x) - computes j<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> djdx(n,x) - computes d[j<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *          <li> y(n,x) - computes y<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> dydx(n,x) - computes d[y<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *          <li> I(n,x) - computes I<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> dIdx(n,x) - computes d[I<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *          <li> K(n,x) - computes K<sub>n</sub>(x) for integer and real
 *                        values of n.
 *          <li> dKdx(n,x) - computes d[K<sub>n</sub>(x)]/dx for integer
 *                        and real values of x
 *       </UL>
 *  <li> the beta function &Beta;(x,y), which is defined as
 *       &Gamma;(x)&Gamma;(y)/&Gamma;(x+y). In addition, the
 *       incomplete beta function, defined as
 *       &Beta;<sub>x</sub>(a,b) = &int;<sub>0</sub><sup>x</sup>
 *       t<sup>a-1</sup>(1-t)<sup>b-1</sup> dt, along with the
 *       regularized incomplete beta function I<sub>x</sub>(a, b)
 *       are provided.
 *  <li> confluent hypergeometric functions:
 *       <UL>
 *         <LI> M(a, b, x) - Kummer's function of the first kind.
 *         <LI> dMdx(a, b, x) - the derivative of M with respect to its
 *                              third argument.
 *         <LI> d2Mdx2(a, b, x) - the second derivative of M with respect to its
 *                              third argument.
 *       </UL>
 *       An alternate notation is <sub>1</sub>F<sub>1</sub>(a;b;z) = M(a,b,z).
 *  <li> the error function
 *       erf(x) = (2/&pi;<sup>1/2</sup>)&int;<sub>0</sub><sup>x</sup>e<sup>-t<sup>2</sup></sup>dt
 *       and the complementary error function
 *       erfc(x) = 1 - erf(x).  Both are provided because for large x, erf(x)
 *       asymptotically approaches 1.0.
 *  <li> factorials:
 *       <UL>
 *          <li> factorial(n) - n!  (as a double-precision value).
 *          <li> logFactorial(n) - log n! (provided as a method to
 *               avoid overflow errors that would occur if the
 *               expression Math.log(Functions.factorial(n)) was used
 *               instead.)
 *          <li> longFactorial(n) - n! (as a long integer value).
 *          <li> exactFactorial - n! (as a BigInteger to provide whatever
 *               number of bits are necessary).
 *       </UL>
 *  <li> the gamma function and the digamma function:
 *       <UL>
 *         <li> Gamma(x) - &Gamma;(x).
 *         <li> logGamma(x) - log &Gamma;(x)  (to avoid the overflow errors that
 *              would occur if Math.log(Functions.Gamma(n)) was used).
 *         <li> digamma(x) - &psi;(x) (the digamma function &psi;(x) is
 *              defined as &Gamma;'(x) / &Gamma;(x)).
 *          <li> poch(x,n) computes Pochhammer's symbol (x)<sub>n</sub>
 *               defined as 1 when n=0 and x(x+1)(x+2)...(x+n-1) for
 *               n &gt; 0.  Except at points where the gamma function
 *               diverges, (x)<sub>n</sub> = &Gamma;(x+n)/&Gamma;(x).
 *       </UL>
 *  <li> the hypergeometric function:
 *       <UL>
 *         <li> hgF(a,b,c,z) computes <sub>2</sub>F<sub>1</sub>(a, b; c; z).
 *         <li> hgF(an,ad,b,c,z) computes
 *              <sub>2</sub>F<sub>1</sub>(an/ad, b; c; z) with two integer
 *              arguments used to represent the first argument as a rational
 *              number an/ad.  This form is useful for some special cases in
 *              which z &gt; 1.
 *         <li> hgF(an,ad,bn,bd,c,z) computes
 *              <sub>2</sub>F<sub>1</sub>(an/ad, bn/bd; c; z) with four integer
 *              arguments used to represent the first two arguments as rational
 *              numbers an/ad and bn/bd.  This form is useful for some
 *              special cases in  which z &gt; 1.
 *       </UL>
 *  <li> inverse hyperbolic functions:
 *        <UL>
 *         <li> asinh(x) - sinh<sup>-1</sup>(x).
 *         <li> acosh(x) - cosh<sup>-1</sup>(x).
 *         <li> atanh(x) - tanh<sup>-1</sup>(x).
 *        </UL>
 *  <li> Laguerre polynomials and associated Laguerre polynomials:
 *       <UL>
 *         <li> L(n,x) - computes the value of the Laguerre polynomial
 *              L<sub>n</sub>(x).
 *         <li> dLdx(n,x) - computes the value of derivative
 *              d[L<sub>n</sub>(x)]/dx of a Laguerre polynomial with
 *              respect to x.
 *         <li> L(n, &alpha;, x) - computes the value of the associated
 *              Laguerre polynomial L<sub>n</sub><sup>(&alpha;)</sup>(x).
 *         <li> dLdx(n,&alpha;,x) - computes the value of derivative
 *              d[L<sub>n</sub><sup>&alpha;</sup>(x)]/dx of an
 *              associated Laguerre polynomial with  respect to x.
 *       </UL>
 *  <li> Legendre polynomials and associated Legendre functions with
 *       integer coefficients:
 *       <UL>
 *           <li> P(n,x) computes P<sub>n</sub>(x).
 *           <li> dPdx(n,x) - computes dP<sub>n</sub>/dx
 *                (the derivative of Legendre polynomials of degree n with
 *                respect to x).
 *           <li> P(n,m,x) computes the associated Legendre function
 *                P<sup>m</sup><sub>n</sub>(x) for integer values of n and m.
 *           <li> dPdx(n,m,x) computes dP<sup>m</sup><sub>n</sub>/dx
 *                (the derivative of the associated Legendre polynomial of
 *                degree n and order m with respect to x).
 *       </UL>
 *  <li> A function pow(x, n) to raise a real number x to an integer power n
 *       and a function pow(x, xn, xd) to raise a real number x to a power
 *       given by the rational number xn/xd (in this case '/' denotes
 *       floating-point division although xn and xd are integers).
 *  <li> Spherical harmonics are represented by a function
 *       Yamp(l,m,&theta;) giving the "amplitude" of a spherical
 *       harmonic.  The amplitude is a real-valued function whose
 *       absolute value is the magnitude of a complex number. The
 *       value of Yamp(l,m,&theta;) is numerically equal to
 *       Y<sub>l</sub><sup>m</sup>(&theta;, 0).
 *  <li> the Riemann zeta function: zeta(x) computes &zeta;(x).
 * </UL>

 * The notation for these functions, aside from Bernstein polynomials,
 * is that used in the Handbook of Mathematical Functions (Abramowitz
 * and Stegun) and Classical Electrodynamics (Jackson). For Bernstein
 * Polynomials, the notation used is described in
* <a href = "http://www.idav.ucdavis.edu/education/CAGDNotes/Bernstein-Polynomials.pdf">Bernstein Polynomials</a>.
 *<P>
 * There are additionally a series of nested classes that provide additional
 * capabilities (for example, computing the roots of Legendre polynomials) and
 * computing both a Legendre function and its derivative simultaneously).
 * <P>
 * Many of the methods are based on formulas provided in Abramowitz
 * and Stegun, "Handbook of Mathematical Functions" (10th printing [1972],
 * 9th Dover printing).
 * Any reference to Abramowitz and Stegun in the documentation for
 * specific methods refers to this specific edition.
 */
public class Functions {

    // only static methods & static inner classes
    private Functions(){}

    /*
       Implementation note: Kahan's summation algorithm is used in
       a number of places, coded in-line.  This algorithm results
       in sequences of statements such as
          double sum = 0.0;
          double c = 0.0;
          ...
          for-or-while (...) {
	     ...
             double y = term - c;
             double t = sum + y;
             c = (t - sum) - y;
             sum = t;
	     ...
          }
       with a goal of reducing round-off errors in sequences of floating
       point additions.
    */
    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Compute the greatest common divisor of two integers
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of a and b
     * @exception IllegalArgumentException both arguments were zero
     */
    @Deprecated
    public static int gcd(int a, int b) throws IllegalArgumentException {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a < 0) a = -a;
	if (b < 0) b = -b;
	return gcdNN(a, b);
    }

    // valid for non-negative integers
    @Deprecated
    private static int gcdNN(int a, int b) {
	if (b == 0) return a;
	else return gcdNN(b, a%b);
    }

    /**
     * Compute the greatest common divisor of two non-negative long integers
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of a and b
     * @exception IllegalArgumentException both arguments were zero
     */
    @Deprecated
    public static long gcd(long a, long b) throws IllegalArgumentException {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a < 0) a = -a;
	if (b < 0) b = -b;
	return gcdNN(a, b);
    }

    // valid for non-negative integers
    @Deprecated
    private static long gcdNN(long a, long b) {
	if (b == 0) return a;
	else return gcdNN(b, a%b);
    }

    private static LinkedList<int[]> intMIPool = new LinkedList<>();
    static {
	int np = Runtime.getRuntime().availableProcessors();
	for (int i = 0; i < np; i++) {
	    intMIPool.add(new int[2]);
	}
    }

    /**
     * Compute the multiplicative inverse of an integer modulo m.
     * @param a the integer whose inverse modulo m is to be computed
     * @param m the modulus
     * @return the multiplicative inverse of a (mod m)
     * @exception IllegalArgumentException the arguments are not relatively
     *            prime
     */
    public static int modInverse(int a, int m) throws IllegalArgumentException {
	int[] result;
	synchronized (intMIPool) {
	    result = intMIPool.poll();
	    if (result == null) {
		result = new int[2];
	    }
	}
	try {
	    if (Bezout.gcd(a, m, result) != 1) {
		throw new IllegalArgumentException
		    (errorMsg("relativelyPrime", a, m));
	    }
	    int ival = result[0] % m;
	    if (ival < 0) ival = m + ival;
	    return ival;
	} finally {
	    synchronized(intMIPool) {
		intMIPool.add(result);
	    }
	}
    }

    private static LinkedList<long[]> longMIPool = new LinkedList<>();
    static {
	int np = Runtime.getRuntime().availableProcessors();
	for (int i = 0; i < np; i++) {
	    longMIPool.add(new long[2]);
	}
    }

    /**
     * Compute the multiplicative inverse of a long integer modulo m.
     * @param a the integer whose inverse modulo m is to be computed
     * @param m the modulus
     * @return the multiplicative inverse of a (mod m)
     * @exception IllegalArgumentException the arguments are not relatively
     *            prime
     */
    public static long modInverse(long a, long m)
	throws IllegalArgumentException
    {
	long[] result;
	synchronized (longMIPool) {
	    result = longMIPool.poll();
	    if (result == null) {
		result = new long[2];
	    }
	}
	try {
	    if (Bezout.gcd(a, m, result) != 1) {
		throw new IllegalArgumentException
		    (errorMsg("relativelyPrime", a, m));
	    }
	    long ival = result[0] % m;
	    if (ival < 0) ival = m + ival;
	    return ival;
	} finally {
	    synchronized(longMIPool) {
		longMIPool.add(result);
	    }
	}
    }

    /**
     * Compute the base-2 logarithm of an integer n.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * @param n an integer that is larger than 0
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
    @Deprecated
    public static double log2(int n) throws IllegalArgumentException {
	if (n <= 0) throw new IllegalArgumentException
			(errorMsg("intArgNotPositive", n));
	int m = 31 - Integer.numberOfLeadingZeros(n);
	double sum = m;
	m = 1 << m;
	double a = ((double)n)/((double)m);
	double b = 1.0;
	while (b > 1.0e-15) {
	    a *= a;
	    b = b/2.0;
	    if (a > 2.0) {
		sum = sum + b;
		a /= 2.0;
	    }
	}
	return sum;
    }


    /**
     * Compute the base-2 logarithm of an integer n, specifying its accuracy.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * One use of this method is in estimating the time or space needed by an
     * algorithm, where precise values are not needed.
     * <P>
     * Note: when the accuracy is set to 1.0, the value returned is
     * floor(log2(n)).
     * @param n an integer that is larger than 0
     * @param accuracy the accuracy of the fractional part of the returned value
     *        (values should be in the range (0.0, 1.0])
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
    @Deprecated
    public static double log2(int n, double accuracy)
	throws IllegalArgumentException
    {
	if (n <= 0) throw new IllegalArgumentException
			(errorMsg("intArgNotPositive", n));
	if (accuracy <= 0.0) throw new IllegalArgumentException
			(errorMsg("argNotPositive", accuracy));
	int m = 31 - Integer.numberOfLeadingZeros(n);
	double sum = m;
	if (accuracy < 1.0) {
	    m = 1 << (m);
	    double a = ((double)n)/((double)m);
	    double b = 1.0;
	    while (b > accuracy) {
		a *= a;
		b = b/2.0;
		if (a > 2.0) {
		    sum = sum + b;
		    a /= 2.0;
		}
	    }
	}
	return sum;
    }


    /**
     * Compute the base-2 logarithm of a long integer n.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * @param n an integer that is larger than 0
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
    @Deprecated
    public static double log2(long n) throws IllegalArgumentException {
	if (n <= 0) throw new IllegalArgumentException
			(errorMsg("intArgNotPositive", n));
	long m = 63 - Long.numberOfLeadingZeros(n);
	double sum = m;
	m = 1 << m;
	double a = ((double)n)/((double)m);
	double b = 1.0;
	while (b > 1.0e-15) {
	    a *= a;
	    b = b/2.0;
	    if (a > 2.0) {
		sum = sum + b;
		a /= 2.0;
	    }
	}
	return sum;
    }


    /**
     * Compute the base-2 logarithm of a long integer n, specifying its
     * accuracy.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * One use of this method is in estimating the time or space needed by an
     * algorithm, where precise values are not needed.
     * @param n a long integer that is larger than 0
     * @param accuracy the accuracy of the fractional part of the returned value
     *        (values should be in the range (0.0, 1.0])
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
    @Deprecated
    public static double log2(long n, double accuracy)
	throws IllegalArgumentException
    {
	if (n <= 0) throw new IllegalArgumentException
			(errorMsg("intArgNotPositive", n));
	if (accuracy <= 0.0) throw new IllegalArgumentException
			(errorMsg("argNotPositive", accuracy));
	long m = 63 - Long.numberOfLeadingZeros(n);
	double sum = m;
	if (accuracy < 1.0) {
	    m = 1 << m;
	    double a = ((double)n)/((double)m);
	    double b = 1.0;
	    while (b > accuracy) {
		a *= a;
		b = b/2.0;
		if (a > 2.0) {
		    sum = sum + b;
		    a /= 2.0;
		}
	    }
	}
	return sum;
    }


    /**
     * Class containing methods specific to Bernstein polynomials.
     * <UL>
     *   <li> Methods for summing Bernstein polynomials:
     *        <UL>
     *          <li> sumB(beta, n, x) - computes beta[i]*B(i,n,x), summed over i
     *               with the sum ranging from 0 to n inclusive.
     *          <li> dsumBdx(beta, n, x) - computes the first derivative
     *               with respect to x of beta[i]*Bernstein(i,n,x) summed
     *               over i with the sum ranging from 0 to n inclusive.
     *          <li> d2sumBdx2(beta, n, x) - computes the second derivative
     *               with respect to x of beta[i]*Bernstein(i,n,x)
     *               summed over i with the sum ranging from 0 to n inclusive.
     *        </UL>
     *   <li> Methods for computing multiple sums of Bernstein polynomials:
     *        <UL>
     *          <li> sumB(result, beta, n, x) - for each element of result,
     *               result[m], computes
     *               beta[m+i*result.length]*B(i,n,x), summed over i
     *               with the sum ranging from 0 to n inclusive.
     *          <li> dsumBdx(result, beta, n, x) - for each element of result,
     *               result[m], computes the first derivative
     *               with respect to x of
     *               beta[m +i*result.length]*Bernstein(i,n,x) summed
     *               over i with the sum ranging from 0 to n inclusive.
     *          <li> d2sumBdx2(result, beta, n, x) - for each element of
     *               result, result[m], computes the  second derivative
     *               with respect to x of
     *               beta[m+i*result.length]*Bernstein(i,n,x)
     *               summed over i with the sum ranging from 0 to n inclusive.
     *        </UL>
     *        The argument beta is equivalent to a matrix whose rows
     *        are the values of beta for a given m (so that
     *        sumB(result, beta, n, x) will set
     *        result[j] to the sum over i of beta[m][i]*B(i,n,x)), with
     *        the matrix flattened so that the row index varies faster
     *        than the column index. This is the same convention used
     *        in the standard Java libraries for the arrays of coordinates
     *        returned by PathIterator methods.
     *   <li> Methods for summing Bernstein polynomials that use barycentric
     *        coordinates (the first 5 are used by the implementation but
     *        may be useful for generating documentation, configuring GUIs,
     *        etc.). The sum can be written as
     *        &sum;<sub>&lambda;</sub> &beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
     *       where n is a non-negative integer, &tau; is a vector of length
     *       dim(&tau;), and the values of &lambda; in the sum satisfy
     *       0 &le; &lambda;<sub>i</sub> &le; n and
     *       &sum;<sub>i=0</sub><sup>dim(&lambda;)-1</sup> &lambda;<sub>i</sub> = n.
     *       For convenience, the &lambda; vectors can be encoded as an index.
     *       The methods are:
     *        <UL>
     *           <li> indexForLambdas(n, lambdas...) - for Bernstein
     *                polynomials of degree n, encode the lambda
     *                values as an index.  Indices are not contiguous
     *                but have a well-defined order.
     *           <li> lambdasForIndex(n, len, index) - for Bernstein
     *                polynomials of degree n convert an index to a
     *                vector whose length is len and whose components
     *                are lambda values.
     *           <li> lambdasForIndex(n, results, index) - for
     *                Bernstein polynomials of degree n convert an
     *                index to a vector, storing the values in the array
     *                results.  The length of the array results corresponds
     *                the number of barycentric coordinates.
     *           <li> generateIndices(n, len) - for Bernstein polynomials of
     *                degree n, generate an array containing
     *                indices in a canonical order. For each index in this
     *                array, the sum of its &lambda; components will be n.
     *                The number of &lambda; components is len.
     *           <li> generateIndices(n, m, len) - for Bernstein polynomials of
     *                degree n, generate an array containing
     *                indices in a canonical order. For each index in this
     *                array, the sum of its &lambda; components will be m.
     *                The number of &lambda; components is len.
     *           <li> sumB(beta, n, x...) - compute the sum
     *                beta[ind]*B(n, lambdasForIndex(n, len, list[ind]), x0,
     *                x1, ...)
     *                where list = generateIndices(n, len) and len is the
     *                number of arguments from and including x0 to the end of
     *                the argument list. This sum is equivalent to
     *                &sigma;<sub>&lambda;</sub> &beta;<sub>&lambda;</sub>B<sup>n</sub><sub>&lambda;</sub>(&tau;).
     *           <li> dsumBdx(xInd, beta, n, x...) - compute the partial
     *                derivative of sumB(beta, n, x0, ... ) with respect to
     *                x<sub>xInd</sub>.
     *           <li> d2sumBdxdy(xInd,yInd, beta, n, x...) - compute the partial
     *                derivative of sumB(beta, n, x0, ... ) with respect to
     *                x<sub>xInd</sub>, and then differentiate again with
     *                respect to x<sub>yInd</sub>.
     *        </UL>
     *   <li> Methods for computing multiple sums of Bernstein polynomials
     *        that use barycentric coordinates.
     *        The methods are
     *        <UL>
     *           <li> sumB(result, beta, n, x...) - for each component
     *                result[m] compute the sum
     *                beta[m + ind*result.length]*B(n,
     *                lambdasForIndex(n, len, list[ind]), x0, x1, ...)
     *                where list = generateIndices(n, len) and len is the
     *                number of arguments from and including x0 to the end of
     *                the argument list.
     *           <li> dsumBdx(result, xInd, beta, n, x...) - for each
     *                component result[m] compute the partial
     *                derivative of sumB(beta, n, x0, ... ) with
     *                respect to x<sub>xInd</sub>.
     *           <li> d2sumBdxdy(result, xInd,yInd, beta, n, x...) -
     *                for each component result[m] compute the partial
     *                derivative of sumB(beta, n, x0, ... ) with
     *                respect to x<sub>xInd</sub>, and then
     *                differentiate again with respect to
     *                x<sub>yInd</sub>.
     * </UL>
     */
    public static class Bernstein {

	private Bernstein() {}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored.
	 * @param beta the weights for each polynomial
	 * @param n the degree of the Bernstein polynomials (a non-negative
	 *        integer)
	 * @param x the point at which to evaluate the polynomials
	 * @return the sum of beta[i+m]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double sumB(double[] beta, int n, double x)
	    throws IllegalArgumentException
	{
	    return sumB(beta, 0, n, x);
	}

	/**
	 * Compute the coefficients for a weighted sum of Bernstein polynomials
	 * of degree n+r for a weighted sum of Bernstein polynomials of degree
	 * n.
	 * @param result an array holding the coefficients after the degree
	 *        was raised by r
	 * @param beta the original coefficients
	 * @paran n the degree of the polynomials before the degree was
	 *        raised
	 * @param r the degree increment
	 * @result the new degree (n+r)
	 */
	public static int raiseBy(double[] result, double[] beta, int n, int r)
	{
	    if (n < 0) {
		String msg = errorMsg("thirdArgNeg", n);
		throw new IllegalArgumentException(msg);
	    }
	    if (r < 0) {
		String msg = errorMsg("fourthArgNeg", r);
		throw new IllegalArgumentException(msg);
	    }
	    if (result == null || beta == null) {
		throw new NullPointerException();
	    }
	    if (r == 0) {
		System.arraycopy(beta, 0, result, 0, n+1);
		return n;
	    }
	    if (result == beta) {
		double[] tmp = new double[n+1];
		System.arraycopy(beta, 0, tmp, 0, n+1);
		beta = tmp;
	    }
	    // See
	    // http://web.mit.edu/hyperbook/Patrikalakis-Maekawa-Cho/node13.html
	    int npr = n + r;
	    for (int i = 0; i <= npr; i++) {
		double c = 0.0;
		double sum = 0.0;
		int limit = Math.min(i, n);
		for (int j = Math.max(0,i-r); j <= limit; j++) {
		    double term = beta[j]*Binomial.coefficient(n, j)
			    * Binomial.coefficient(r, i-j)
			    / Binomial.coefficient(npr, i);
		    double y = term - c;
		    double t = sum + y;
		    c = (t - sum) - y;
		    sum = t;
		}
		result[i] = sum;
	    }
	    /*
	    if (beta != result) {
		System.arraycopy(beta, 0, result, 0, n+1);
	    }
	    for (int ir = 0; ir < r; ir++) {
		int npir = n+ir;
		int npirp1 = npir + 1;
		result[npirp1] = result[npir];
		for (int k = npir; k > 0; k--) {
		    result[k] = ((npirp1 - k)*result[k]
				+ k*result[k-1])/npirp1;
		}
	    }
	    */
	    return npr;
	}

	/**
	 * Transform the coefficients for a polynomial using a Bernstein
	 * basis to ones for a scaled Bernstein basis.
	 * If the argument array beta contains values such that beta[i]
	 * is the coefficient for B<sup>n</sup><sub>i</sub>(t), then
	 * result[i] will be the coefficient for
	 * (1-t)<sup>i</sup>t<sup>n-i</sup> for i&isin;[0,n].
	 * The arrays result and beta may be the same array.
	 * @param result the coefficients for a scaled Bernstein basis
	 * @param beta the coefficients for a Bernstein basis
	 * @int n the degree of the Bernstein  polynomials
	 */
	public static int scale(double[] result, double[] beta, int n) {
	    for (int i = 1; i < n; i++) {
		result[i] = beta[i]*Binomial.coefficient(n, i);
	    }
	    if (result != beta) {
		result[0] = beta[0];
		result[n] = beta[n];
	    }
	    return n;
	}

	/**
	 * Transform the coefficients for a polynomial using a scaled
	 * Bernstein basis to ones for a Bernstein basis.
	 * If the argument array beta contains values such that beta[i]
	 * is the coefficient (1-t)<sup>i</sup>t<sup>n-i</sup>, then
	 * result[i] will be the coefficient for
	 * B<sup>n</sup><sub>i</sub>(t) for i&isin;[0,n].
	 * The arrays result and beta may be the same array.
	 * @param result the coefficients for a scaled Bernstein basis
	 * @param beta the coefficients for a Bernstein basis
	 * @int n the degree of the Bernstein  polynomials
	 */
	public static int unscale(double[] result, double[] beta, int n) {
	    for (int i = 1; i < n; i++) {
		result[i] = beta[i]/Binomial.coefficient(n, i);
	    }
	    if (result != beta) {
		result[0] = beta[0];
		result[n] = beta[n];
	    }
	    return n;
	}

	// timing tests indicate that array allocation is a significant
	// cost: much more so than using a pool of preallocated arrays.
	private static int ALEN = 256;
	private static class ArrayPair {
	    double[] prev = new double[ALEN];
	    double[] next = new double[ALEN];
	}
	private static LinkedList<ArrayPair> arrayPairPool = new LinkedList<>();
	static {
	    int np = Runtime.getRuntime().availableProcessors();
	    for (int i = 0; i < np; i++) {
		arrayPairPool.add(new ArrayPair());
	    }
	}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree,
	 * specifying an offset.
	 * The i<sup>th</sup> weight will be multiplied by the Bernstein
	 * polynomial B<sub>i,n</sub>(x) when computing the sum.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored.
	 * @param beta the weights for each polynomial
	 * @param m the offset into the array beta at which the weights for
	 *        each polynomial start
	 * @param n the degree of the Bernstein polynomials (a non-negative
	 *        integer)
	 * @param x the point at which to evaluate the polynomials
	 * @return the sum of beta[i+m]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double sumB(double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", n));
	    }
	    if (beta.length < m+n+1) {
		throw new IllegalArgumentException
		    (errorMsg("argArrayTooShort"));
	    }
	    if (n == 0) return beta[m];

	    double x1 = 1.0 - x;

	    if (n == 1) {
		return beta[m]*x1 + beta[m+1]*x;
	    }

	    ArrayPair pair = null;
	    double[] prev;
	    double[] next;
	    try {
		if (n < ALEN) {
		    synchronized(arrayPairPool) {
			pair = arrayPairPool.poll();
			if (pair == null) {
			    pair = new ArrayPair();
			}
		    }
		    prev = pair.prev;
		    next = pair.next;
		} else {
		    prev = new double[n+1];
		    next = new double[n+1];
		}
		System.arraycopy(beta, m, next, 0, n+1);
		for (int j = 1; j <= n; j++) {
		    double[] tmp = prev;
		    prev = next;
		    next = tmp;
		    for (int i = 0; i <= n-j; i++) {
			next[i] = prev[i]*x1 + prev[i+1]*x;
		    }
		}
		return next[0];
	    } finally {
		if (pair != null) {
		    synchronized(arrayPairPool) {
			arrayPairPool.add(pair);
		    }
		}
	    }
	}

	/**
	 * Compute the error for a weighted sum of Bernstein polynomials
	 *  of the same degree, specifying an offset.
	 * The i<sup>th</sup> weight will be multiplied by the Bernstein
	 * polynomial B<sub>i,n</sub>(x) when computing the sum.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored. The error computed is based on the
	 * spacing between adjacent double-precision numbers.
	 * @param beta the weights for each polynomial
	 * @param m the offset into the array beta at which the weights for
	 *        each polynomial start
	 * @param n the degree of the Bernstein polynomials (a non-negative
	 *        integer)
	 * @param x the point at which to evaluate the polynomials
	 * @return the error for the sum of beta[i+m]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double sumBerr(double[] beta, int m, int n, double x) {
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", n));
	    }
	    if (beta.length < m+n+1) {
		throw new IllegalArgumentException
		    (errorMsg("argArrayTooShort"));
	    }
	    if (n == 0) return 0.0;
	    double x1 = 1.0 - x;
	    double ex = Math.ulp(x);
	    double mx = Math.max(x, x1);
	    double errBeta = 0;
	    double maxBeta = 0.0;
	    for (int i = 0; i < n; i++) {
		double mb = Math.abs(beta[m+i]);
		double err = Math.ulp(mb);
		if (err > errBeta) errBeta = err;
		if (mb > maxBeta) maxBeta = mb;
	    }
	    return 2*n*(errBeta*mx + maxBeta*ex);
	}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same
	 * degree for each component of an array of arguments.
	 * The implementation uses De Casteljau's algorithm, which is
	 * fast and numerically stable.  The length of the array beta
	 * must be at least (n+1) multiplied by the length of the
	 * "result" argument vector. The array beta contains the
	 * weights for each element of the argument array result and for
	 * each Bernstein polynomial B<sub>i,n</sub>, ordered so that
	 * the weights for each element of result and a given value of i
	 * appear before the weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param result the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void sumB(double[] result, double[] beta,
				int n, double x)
	    throws IllegalArgumentException
	{
	    sumB(result, beta, 0, n, x);
	}
	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree
	 * for each component of an array of arguments, specifying an
	 * offset into the array representing weights.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (m + (n+1)) multiplied
	 * by the length of the "result" argument vector. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1.  Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param result the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array measured in multiples
	 *        of the length of the vector <code>result</code>
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void sumB(double[] result, double[] beta,
				int m, int n, double x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative4", n));
	    }
	    if (beta.length < (m + n+1)*result.length) {
		throw new
		    IllegalArgumentException(errorMsg("argArrayTooShort"));
	    }
	    if (n == 0) {
		for (int i = 0; i < result.length; i++) {
		    result[i] = beta[i + m*result.length];
		}
		return;
	    }

	    ArrayPair pair = null;

	    double[] prev = null;
	    double[] next = null;

	    try {
		if (n > 1) {
		    if (n < ALEN) {
			synchronized(arrayPairPool) {
			    pair = arrayPairPool.poll();
			    if (pair == null) {
				pair = new ArrayPair();
			    }
			}
			prev = pair.prev;
			next = pair.next;
		    } else {
			prev = new double[n+1];
			next = new double[n+1];
		    }
		}

		double x1 = 1.0 - x;
		if (n == 1) {
		    for (int i = 0; i < result.length; i++) {
			result[i] =  beta[i + m*result.length]*x1
			    + beta[i + (m+1)*result.length]*x;
		    }
		} else {

		    for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < n+1; j++) {
			    next[j] = beta[i + (m+j)*result.length];
			    prev[j] = 0.0;
			}
			for (int j = 1; j <= n; j++) {
			    double[] tmp = prev;
			    prev = next;
			    next = tmp;
			    for (int k = 0; k <= n-j; k++) {
				next[k] = prev[k]*x1 + prev[k+1]*x;
			    }
			}
			result[i] =  next[0];
		    }
		}
	    } finally {
		if (pair != null) {
		    synchronized(arrayPairPool) {
			arrayPairPool.add(pair);
		    }
		}
	    }
	}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same
	 * degree for each component of an array of arguments,
	 * specifying an offset into the array representing weights
	 * and an offset into the array storing the results.
	 * The implementation uses De Casteljau's algorithm, which is
	 * fast and numerically stable.  The length of the array beta
	 * must be at least (m + (n+1)) multiplied by the value of the
	 * 'rlen' argument. Indices larger than n+1 are ignored. The
	 * array beta contains the weights for each element of the
	 * argument array result in the range [offset, offset+rlen)
	 * and for each Bernstein polynomial B<sub>i,n</sub>, ordered
	 * so that the weights for each element of result and a given
	 * value of i appear before the weights for i+1.  Similarly,
	 * for a given value of i, the indices of beta corresponding
	 * to elements of the vector result are in the same order as
	 * for the vector result.
	 * <P>
	 * Note: this method was added because it is useful in computing
	 * values at points on B&eacute;zier patches.
	 * @param result the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param offset the offset into the result array
	 * @param rlen the length to use for the computation
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array measured in multiples
	 *        of the length of the vector <code>result</code>
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void sumB(double[] result, int offset, int rlen,
				double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative4", n));
	    }
	    if (beta.length < (m + n+1)*rlen) {
		throw new
		    IllegalArgumentException(errorMsg("argArrayTooShort"));
	    }
	    if (n == 0) {
		for (int i = 0; i < result.length; i++) {
		    result[i+offset] = beta[i + m*rlen];
		}
		return;
	    }

	    ArrayPair pair = null;

	    double[] prev = null;
	    double[] next = null;

	    try {
		if (n > 1) {
		    if (n < ALEN) {
			synchronized(arrayPairPool) {
			    pair = arrayPairPool.poll();
			    if (pair == null) {
				pair = new ArrayPair();
			    }
			}
			prev = pair.prev;
			next = pair.next;
		    } else {
			prev = new double[n+1];
			next = new double[n+1];
		    }
		}

		double x1 = 1.0 - x;
		if (n == 1) {
		    for (int i = 0; i < rlen; i++) {
			result[i+offset] =  beta[i + m*rlen]*x1
			    + beta[i + (m+1)*rlen]*x;
		    }
		} else {
		    for (int i = 0; i < rlen; i++) {
			for (int j = 0; j < n+1; j++) {
			    next[j] = beta[i + (m+j)*rlen];
			    prev[j] = 0.0;
			}
			for (int j = 1; j <= n; j++) {
			    double[] tmp = prev;
			    prev = next;
			    next = tmp;
			    for (int k = 0; k <= n-j; k++) {
				next[k] = prev[k]*x1 + prev[k+1]*x;
			    }
			}
			result[i+offset] =  next[0];
		    }
		}
	    } finally {
		if (pair != null) {
		    synchronized(arrayPairPool) {
			arrayPairPool.add(pair);
		    }
		}
	    }
	}

	/**
	 * Compute the derivative of a weighted sum of Bernstein
	 * polynomials of the same degree.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored.
	 * @param beta the weights for each polynomial
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the derivative
	 * @return the derivative of the sum of beta[i+m]*B(i,n,x)
                   for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double dsumBdx(double[] beta, int n, double x)
	    throws IllegalArgumentException
	{
	    return dsumBdx(beta, 0, n, x);
	}

	/**
	 * Compute the derivative of a weighted sum of Bernstein
	 * polynomials of the same degree using a coefficient array with
	 * an offset.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least m+n+1. Indices larger
	 * than or equal to  m+n+1 are ignored. The coefficient for
	 * B<sub>i,n</sub>(x) is beta[m+i].
	 * @param beta the weights for each polynomial
	 * @param m the offset into the array provided by the first
	 *        argument
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the derivative
	 * @return the derivative of the sum of beta[i+m]*B(i,n,x)
                   for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double dsumBdx(double[] beta, int m, int n, double x) {
	    return n * (sumB(beta, m+1, n-1, x) - sumB(beta, m, n-1, x));
	}

	/**
	 * Compute the I<sup>th</sup> derivative of a weighted sum of Bernstein
	 * polynomials of the same degree using a coefficient array with
	 * an offset.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least m+n+1. Indices larger
	 * than or equal to  m+n+1 are ignored. The coefficient for
	 * B<sub>i,n</sub>(x) is beta[m+i].
	 * @param I the number of times to differentiate
	 * @param beta the weights for each polynomial
	 * @param m the offset into the array provided by the second
	 *        argument
	 * @param n The degree of the Bernstein polynomials
	 * @param x the argument
	 * @return the second derivative of the sum of
	 *         beta[i]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	static double dIsumBdxI(int I, double[] beta, int m, int n, double x) {
	    if (I == 0) {
		return sumB(beta, m, n, x);
	    }
	    return n * (dIsumBdxI(I-1, beta, m+1, n-1, x)
			- dIsumBdxI(I-1, beta, m, n-1, x));
	}


	/**
	 * Compute the derivative of a weighted sum of Bernstein
	 * polynomials of the same degree for each component of an
	 * array of arguments.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (n+1) multiplied
	 * by the length of the "result" argument vector. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param results the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void
	    dsumBdx(double[] results, double[] beta, int n, double x)
	    throws IllegalArgumentException
	{
	    dsumBdx(results, beta, 0, n, x);
	}

	/**
	 * Compute the derivative of a weighted sum of Bernstein
	 * polynomials of the same degree for each component of an
	 * array of arguments, specifying an offset into the array
	 * representing weights.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (m + (n+1)) multiplied
	 * by the length of the "result" argument vector. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param results the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array in multiples of the length
	 *        of the vector result
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void
	    dsumBdx(double[] results, double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    double[] tmp = new double[results.length];
	    sumB(results, beta, m+1, n-1, x);
	    sumB(tmp, beta, m, n-1, x);
	    for (int i = 0; i < results.length; i++) {
		results[i] = n * (results[i] - tmp[i]);
	    }
	}

	/**
	 * Compute the derivative of a weighted sum of Bernstein
	 * polynomials of the same degree for each component of an
	 * array of arguments, specifying an offset into the array
	 * representing weights and an offset into the array storing
	 * the results.

	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (m + (n+1)) multiplied
	 * by value of the 'rlen' argument. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result in the range
	 * [offset, offset+rlen) and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * <P>
	 * Note: this method was added because it is useful in computing
	 * tangents at points on B&eacute;zier patches.
	 * @param results the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param offset the offset into the result array
	 * @param rlen the length to use for the computation
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array in multiples of the length
	 *        of the vector result
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void
	    dsumBdx(double[] results, int offset, int rlen,
		    double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    double[] tmp = new double[results.length];
	    sumB(results, offset, rlen, beta, m+1, n-1, x);
	    sumB(tmp, offset, rlen, beta, m, n-1, x);
	    for (int i = 0; i < rlen; i++) {
		results[i+offset] = n * (results[i+offset] - tmp[i+offset]);
	    }
	}



	/**
	 * Compute the second derivative of a weighted sum of Bernstein
	 * polynomials of the same degree.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored.
	 * @param beta the weights for each polynomial
	 * @param n The degree of the Bernstein polynomials
	 * @param x the argument
	 * @return the second derivative of the sum of
	 *         beta[i]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double d2sumBdx2(double[] beta, int n, double x)
	    throws IllegalArgumentException
	{
	    return n * (dsumBdx(beta, 1, n-1, x) - dsumBdx(beta, 0, n-1, x));
	}

	/**
	 * Compute the second derivative of a weighted sum of Bernstein
	 * polynomials of the same degree using a coefficient array with
	 * an offset.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least m+n+1. Indices larger
	 * than or equal to  m+n+1 are ignored. The coefficient for
	 * B<sub>i,n</sub>(x) is beta[m+i].
	 * @param beta the weights for each polynomial
	 * @param m the offset into the array provided by the first
	 *        argument
	 * @param n The degree of the Bernstein polynomials
	 * @param x the argument
	 * @return the second derivative of the sum of
	 *         beta[i]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double d2sumBdx2(double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    return n * (dsumBdx(beta, m+1, n-1, x) - dsumBdx(beta, m, n-1, x));
	}

	/**
	 * Compute the second derivative of a weighted sum of Bernstein
	 * polynomials of the same degree for each component of an
	 * array of arguments, specifying an offset into the array
	 * representing weights.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (m + (n+1)) multiplied
	 * by the length of the "result" argument vector. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param results the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array in multiples of the length
	 *        of the vector result
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void
	    d2sumBdx2(double[] results, double[] beta, int m, int n, double x)
	    throws IllegalArgumentException
	{
	    if (n < 2) {
		Arrays.fill(results, 0.0);
		return;
	    }
	    double[] tmp = new double[results.length];
	    dsumBdx(results, beta, m+1, n-1, x);
	    dsumBdx(tmp, beta, m, n-1, x);
	    for (int i = 0; i < results.length; i++) {
		results[i] = n * (results[i] - tmp[i]);
	    }
	}


	/**
	 * Compute the Ith derivative of a weighted sum of Bernstein
	 * polynomials of the same degree.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least n+1. Indices larger
	 * than n+1 are ignored.
	 * @param I the number of times to differentiate
	 * @param beta the weights for each polynomial
	 * @param n The degree of the Bernstein polynomials
	 * @param x the argument
	 * @return the second derivative of the sum of
	 *         beta[i]*B(i,n,x) for i in [0,n]
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double dIsumBdxI(int I, double[] beta, int n, double x)
	    throws IllegalArgumentException
	{
	    if (I == 0) {
		return sumB(beta, 0, n, x);
	    } else if (I == 1) {
		return dsumBdx(beta, 0, n, x);
	    } else {
		return n * (dIsumBdxI(I-1, beta, 1, n-1, x)
			    - dIsumBdxI(I-1, beta, 0, n-1, x));
	    }
	}

	/**
	 * Compute the Ith derivative of a weighted sum of Bernstein
	 * polynomials of the same degree for each component of an
	 * array of arguments, specifying an offset into the array
	 * representing weights.
	 * The implementation uses De Casteljau's algorithm, which is fast
	 * and numerically stable.
	 * The length of the array beta must be at least (m + (n+1)) multiplied
	 * by the length of the "result" argument vector. Indices larger
	 * than n+1 are ignored. The array beta contains the weights
	 * for each element of the argument array result and for each Bernstein
	 * polynomial B<sub>i,n</sub>, ordered so that the weights
	 * for each element of result and a given value of i appear before the
	 * weights for i+1. Similarly, for a given value
	 * of i, the indices of beta corresponding to elements of the vector
	 * result are in the same order as for the vector result.
	 * @param results the sum of beta[i+m]*B(i,n,x) for i in [0,n];
	 * @param beta the weights for each polynomial listed in the order
	 *        specified above
	 * @param m the offset into the beta array in multiples of the length
	 *        of the vector result
	 * @param n The degree of the Bernstein polynomials
	 * @param x the value at which to evaluate the polynomials
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void
	    dIsumBdxI(double[] results, double[] beta, int m, int n,
		      double x, int I)
	    throws IllegalArgumentException
	{
	    if (n < I) {
		Arrays.fill(results, 0.0);
	    } else if (I == 0) {
		sumB(results, beta, m, n, x);
	    } else if (I  == 1) {
		dsumBdx(results, beta, m, n, x);
	    } else {
		double[] tmp = new double[results.length];
		dIsumBdxI(results, beta, m+1, n-1, x, I-1);
		dIsumBdxI(tmp, beta, m, n-1, x, I-1);
		for (int i = 0; i < results.length; i++) {
		    results[i] = n * (results[i] - tmp[i]);
		}
	    }
	}

	/**
	 * Get an index representing a sequence of m values
	 *  &lambda;<sub>0</sub>, &lambda;<sub>1</sub>, ..., &lambda;<sub>m-1</sub>,
	 * for which &lambda;<sub>i</sub> &isin; [0, n] for i &isin; [0, m)
	 * and such that the sum of &lambda;<sub>i</sub> (i &isin; [0, m)) is
	 * n.
	 * <P>
	 * The index returned for a set of lambdas is encoded in a way that
	 * makes it easy to generate the the corresponding lambda values
	 * for a given value of n and m. Valid values for an index are
	 * in general not contiguous integers.
	 * @param n the maximum allowed value for &lambda;<sub>i</sub> for
	 *        i &isin; [0, m)
	 * @param lambdas the values
	 *        &lambda;<sub>0</sub>, &lambda;<sub>1</sub>, ... in that
	 *        order
	 * @return the index for the given arguments
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static int indexForLambdas(int n, int... lambdas)
	    throws IllegalArgumentException
	{
	    if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative1", n));
	    int index = 0;
	    int np1 = n + 1;
	    int m = 1;
	    for (int i = lambdas.length-1; i >= 0; i--) {
		int lambda = lambdas[i];
		if (lambda > n || lambda < 0) {
		    throw new IllegalArgumentException
			(errorMsg("argOutOfRangeD", lambda));
		}
		lambda *= m;
		index += lambda;
		m *= np1;
	    }
	    return index;
	}

	/**
	 * Get the sequence of m values corresponding to an index.
	 * This is the inverse of {@link #indexForLambdas} as a function
	 * of the vector &lambda;.
	 * @param n the maximum allowed value for &lambda;<sub>i</sub> for
	 *        i &isin; [0, n]
	 * @param len the number of components of the vector &lambda;
	 * @param index the index
	 * @return an array whose components are
	 *         &lambda;<sub>0</sub>, &lambda;<sub>1</sub>, ... in that
	 *         order
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static int[] lambdasForIndex(int n, int len, int index)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative1", n));
	    }
	    int np1 = n+1;
	    if (MathOps.lPow(n+1, len) <= index)
		throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, len));
	    int[] results = new int[len];

	    for (int i = len-1; i >= 0; i--) {
		results[i] = index%(np1);
		index /= (n+1);
	    }
	    return results;
	}

	/**
	 * Get the sequence of m values corresponding to an index, storing
	 * the results in an array of length m that is explicitly provided.
	 * This is the inverse of {@link #indexForLambdas} as a function
	 * of the vector &lambda;.
	 * @param n the maximum allowed value for &lambda;<sub>i</sub> for
	 *        i &isin; [0, n]
	 * @param results an array whose components will contain
	 *        &lambda;<sub>0</sub>, &lambda;<sub>1</sub>, ... in that
	 *         order
	 * @param index the index
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void lambdasForIndex(int n, int[] results, int index)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative1", n));
	    }
	    int len = results.length;
	    for (int i = len-1; i >= 0; i--) {
		results[i] = index%(n+1);
		index /= (n+1);
	    }
	}


	/**
	 * Generate a list of all the valid indices.
	 * An index if valid if the corresponding vector &lambda;'s components
	 * satisfy two constraints:
	 * <UL>
	 *   <LI> &lambda;<sub>i</sub> &isin; [0, n].
	 *   <LI> &sum;<sub>i=0</sub><sup>m-1</sup> &lambda;<sub>i</sub> = n
	 * </UL>
	 * where m is the the length of the vector containing lambda values.
	 * <P>
	 * The indices generated, for a give value of n and m are not
	 * contiguous but are ordered. These indices can also be easily
	 * converted to a factor containing the &lambda;'s.
	 * @param n the maximum value of a component of &lambda; (inclusive)
	 * @param len the number of &lambda; components (m in the description
	 *        above)
	 * @return the valid indices
	 * @exception IllegalArgumentException an argument was out of bounds
	 * @see #lambdasForIndex(int, int, int)
	 * @see #indexForLambdas(int, int...)
	 */
	public static int[] generateIndices(int n, int len)
	    throws IllegalArgumentException
	{
	    return generateIndices(n, n, len);
	}

	/**
	 * Generate a list of all the valid indices.
	 * An index if valid if the corresponding vector &lambda;'s components
	 * satisfy two constrains:
	 * <UL>
	 *   <LI> &lambda;<sub>i</sub> &isin; [0, n].
	 *   <LI> &sum;<sub>i=0</sub><sup>m-1</sup> &lambda;<sub>i</sub> = m
	 * </UL>
	 * where m is the value of len. The encoding for the index assumes
	 * that the maximum value is n.  The constraint on the sum, however,
	 * limits the maximum value to m, which must be less than or equal to
	 * n.
	 * @param n the maximum value of a component of &lambda;
	 * @param m the maximum value of the sum of &lambda; components
	 * @param len the number of &lambda; components.
	 * @return the valid indices
	 * @exception IllegalArgumentException an argument was out of bounds
	 * @see #lambdasForIndex(int, int, int)
	 * @see #indexForLambdas(int, int...)
	 */
	public static int[]
	    generateIndices(int n, int m, int len)
	    throws IllegalArgumentException
	{
	    return (int[])(generateIndicesAux(n, m, len).clone());
	}

	static final class Key {
	    int n;
	    int m;
	    int len;
	    Key(int n, int m, int len) {
		this.n = n;
		this.m = m;
		this.len = len;
	    }
	    @Override
	    public boolean equals(Object object) {
		if (object instanceof Key) {
		    Key other = (Key)object;
		    return (n == other.n && m == other.m && len == other.len);
		}
		return false;
	    }

	    @Override
	    public int hashCode() {
		int code = n;
		code = (code * 127) ^ m;
		code = (code * 127) ^ len;
		return code;
	    }
	}

	private static HashMap<Key,int[]> map = new HashMap<Key,int[]>();

	private static synchronized int[]
	    generateIndicesAux(int n, int m, int len)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative1", n));
	    }
	    if (m < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative2", m));
	    }

	    if (len < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", len));
	    }

	    Key key = new Key(n, m, len);

	    int[] results = map.get(key);
	    if (results != null) {
		return results;
	    }

	    int count = 0;
	    long max = MathOps.lPow(n+1, len);
	    int[] lambdas = new int[len];
	    int index = 0;
	    while (index < max) {
		lambdasForIndex(n, lambdas, index++);
		int sum = 0;
		for (int i = 0; i < len; i++) {
		    sum += lambdas[i];
		}
		if (sum == m) count++;
	    }
	    results = new int[count];
	    index = 0;
	    int j = 0;
	    while (j < count) {
		lambdasForIndex(n, lambdas, index);
		int sum = 0;
		for (int i = 0; i < len; i++) {
		    sum += lambdas[i];
		}
		if (sum == m) {
		    results[j++] = index;
		}
		index++;
	    }
	    map.put(key, results);
	    return results;
	}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree
	 * using barycentric coordinates.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above.
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @return the weighted sum of Bernstein polynomials for specified
	 *         barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double sumB(double[] beta, int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative2", n));
	    }
	    int np1 = n+1;
	    int xlen = x.length;
	    double[] b = new double[(int)MathOps.lPow(np1,xlen)];
	    int[] lambdaIndices = generateIndicesAux(n, n, xlen);
	    int shift = 0;
	    int index = 0;
	    for (int lindex: lambdaIndices) {
		b[lindex] = beta[index++];
	    }

	    int[] lambdas = new int[x.length];
	    for(int r = 1; r < np1; r++) {
		lambdaIndices = generateIndicesAux(n, n-r, xlen);
		for (int lindex: lambdaIndices) {
		    lambdasForIndex(n, lambdas, lindex);
		    for (int i = 0; i < x.length; i++) {
			lambdas[i]++;
			index = indexForLambdas(n, lambdas);
			b[lindex] += x[i]*b[index];
			lambdas[i]--;
		    }
		}
	    }
	    return b[0];
	}

	/**
	 * Compute a first partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above.
	 * @param xInd an index specifying that the partial derivative is
	 *        computed with respect to x<sub>xInd</sub>
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @return the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double dsumBdx(int xInd, double[] beta, int n,
				     double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", n));
	    }
	    int xlen = x.length;
	    if (xInd < 0 || xInd >= xlen) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    int np1 = n+1;
	    double[] b = new double[(int)MathOps.lPow(np1,xlen)];
	    int[] lambdaIndices = generateIndicesAux(n, n, xlen);
	    int shift = 0;
	    int index = 0;
	    for (int lindex: lambdaIndices) {
		b[lindex] = beta[index++];
	    }
	    int[] lambdas = new int[x.length];
	    for(int r = 1; r < n; r++) {
		lambdaIndices = generateIndicesAux(n, n-r, xlen);
		for (int lindex: lambdaIndices) {
		    lambdasForIndex(n, lambdas, lindex);
		    for (int i = 0; i < x.length; i++) {
			lambdas[i]++;
			index = indexForLambdas(n, lambdas);
			b[lindex] += x[i]*b[index];
			lambdas[i]--;
		    }
		}
	    }
	    for (int i = 0; i < x.length; i++) {
		lambdas[i] = (i == xInd)? 1: 0;
	    }
	    index = indexForLambdas(n, lambdas);
	    return n*b[index];
	}

	/**
	 * Compute a second partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above.
	 * @param xInd an index specifying that the 1st partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>xInd</sub>
	 * @param yInd an index specifying that the 2nd partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>yInd</sub>
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @return the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static double d2sumBdxdy(int xInd, int yInd,
					double[] beta, int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative4", n));
	    }
	    int xlen = x.length;
	    if (xInd < 0 || xInd >= xlen) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    if (yInd < 0 || yInd >= xlen) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", yInd));
	    }
	    int np1 = n+1;
	    int nm1 = n-1;
	    double[] b = new double[(int)MathOps.lPow(np1,xlen)];
	    int[] lambdaIndices = generateIndicesAux(n, n, xlen);
	    int shift = 0;
	    int index = 0;
	    for (int lindex: lambdaIndices) {
		b[lindex] = beta[index++];
	    }
	    int[] lambdas = new int[x.length];
	    for(int r = 1; r < nm1; r++) {
		lambdaIndices = generateIndicesAux(n, n-r, xlen);
		for (int lindex: lambdaIndices) {
		    lambdasForIndex(n, lambdas, lindex);
		    for (int i = 0; i < x.length; i++) {
			lambdas[i]++;
			index = indexForLambdas(n, lambdas);
			b[lindex] += x[i]*b[index];
			lambdas[i]--;
		    }
		}
	    }
	    if (xInd == yInd) {
		for (int i = 0; i < x.length; i++) {
		    lambdas[i] = (i == xInd)? 2: 0;
		}
	    } else {
		for (int i = 0; i < x.length; i++) {
		    lambdas[i] = ((i == xInd) || (i == yInd))? 1: 0;
		}
	    }
	    index = indexForLambdas(n, lambdas);
	    return n*(n-1)*b[index];
	}

	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree
	 * using barycentric coordinates for each of a sequence of values.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) lambdasForIndex(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for result element result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * @param result the weighted sum of Bernstein polynomials for specified
	 *         barycentric coordinates
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void sumB(double result[], double[] beta,
			int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", n));
	    }
	    int np1 = n+1;
	    int rlen = result.length;
	    int rlenm1 = rlen - 1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < rlen; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[k + (index++)*rlen];
		}
		for(int r = 1; r < np1; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		result[k] = b[0];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}


	/**
	 * Compute a weighted sum of Bernstein polynomials of the same degree
	 * using barycentric coordinates for each of sequence of values from
	 * an array, also specifying an offset into that array.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) lambdasForIndex(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for result element result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * <P>
	 * Note: this method was created for use in the class
	 * {@link org.bzdev.geom.Surface3D}.
	 * @param result an array that will hold the weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @param beta the weights
	 * @param offset the offset into the array beta from which the
	 *        sequence of weights starts.
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void sumB(double result[], double[] beta, int offset,
			int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative3", n));
	    }
	    int np1 = n+1;
	    int rlen = result.length;
	    int rlenm1 = rlen - 1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < rlen; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[offset + k + (index++)*rlen];
		}
		for(int r = 1; r < np1; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		result[k] = b[0];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}

	/**
	 * Compute a first partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates for each of a sequence of values.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for the result component result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * @param xInd an index specifying that the partial derivative is
	 *        computed with respect to x<sub>xInd</sub>
	 * @param result the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void dsumBdx(int xInd, double[] result, double[] beta,
				   int n, double... x)
	    throws IllegalArgumentException
	{
	    if (xInd < 0 || xInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative4", n));
	    }
	    int np1 = n+1;
	    int rlen = result.length;
	    int rlenm1 = rlen - 1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < rlen; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[k + (index++)*rlen];
		}
		for(int r = 1; r < n; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		for (int i = 0; i < x.length; i++) {
		    lambdas[i] = (i == xInd)? 1: 0;
		}
		index = indexForLambdas(n, lambdas);
		result[k] = n*b[index];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}

	/**
	 * Compute a first partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates for each of a sequence of values, specifying an offset
	 * into the array providing the weights.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for the result component result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * @param xInd an index specifying that the partial derivative is
	 *        computed with respect to x<sub>xInd</sub>
	 * @param result the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @param beta the weights
	 * @param offset the offset into the array beta at which the weights
	 *        start
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void dsumBdx(int xInd, double[] result, double[] beta,
				   int offset, int n, double... x)
	    throws IllegalArgumentException
	{
	    if (xInd < 0 || xInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative4", n));
	    }
	    int np1 = n+1;
	    int rlen = result.length;
	    int rlenm1 = rlen - 1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < rlen; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[offset+ k + (index++)*rlen];
		}
		for(int r = 1; r < n; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		for (int i = 0; i < x.length; i++) {
		    lambdas[i] = (i == xInd)? 1: 0;
		}
		index = indexForLambdas(n, lambdas);
		result[k] = n*b[index];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}


	/**
	 * Compute a second partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates for each of a sequence of values.
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for the result component result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * @param xInd an index specifying that the 1st partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>xInd</sub>
	 * @param yInd an index specifying that the 2nd partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>yInd</sub>
	 * @param result the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @param beta the weights
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void d2sumBdxdy(int xInd, int yInd,
				      double result[], double[] beta,
				      int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative5", n));
	    }
	    if (xInd < 0 || xInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    if (yInd < 0 || yInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", yInd));
	    }
	    int np1 = n+1;
	    int nm1 = n-1;
	    int rlenm1 = result.length-1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < result.length; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[k + (index++)*result.length];
		}
		for(int r = 1; r < nm1; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		if (xInd == yInd) {
		    for (int i = 0; i < x.length; i++) {
			lambdas[i] = (i == xInd)? 2: 0;
		    }
		} else {
		    for (int i = 0; i < x.length; i++) {
			lambdas[i] = ((i == xInd) || (i == yInd))? 1: 0;
		    }
		}
		index = indexForLambdas(n, lambdas);
		result[k] = n*(n-1)*b[index];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}

	/**
	 * Compute a second partial derivative of a weighted sum of
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates for each of a sequence of values, specifying an
	 * offset into the array providing the weights.
	 * Bernstein polynomials of the same degree using barycentric
	 * coordinates.
	 * The sum is given by
	 * &sum;<sub>&lambda;</sub>&beta;<sub>&lambda;</sub>B<sup>n</sup><sub>&lambda;</sub>(&tau;)
	 * where &tau; is a vector
	 * (x<sub>0</sub>, x<sub>1</sub>, ... ,x<sub>m</sub>) and m is the
	 * number of arguments represented by the variable-length argument x.
	 * The method
	 * {@link #generateIndices(int,int) generateIndices(n, m)}
	 * will create an array of indices that represent values of &lambda;
	 * and
	 * {@link #lambdasForIndex(int,int,int) generateIndices(n, m, index)}
	 * will return an array containing the components of &lambda;
	 * corresponding to the index.  The j<sup>th</sup> element of beta
	 * for the result component result[m]
	 * must contain the value &beta;<sub>&lambda;</sub> for the vector
	 * &lambda; associated with the j<sup>th</sup> entry of the list
	 * of indices described above. These elements are stored in
	 * beta[k + j*result.length] for the computation of result[k].
	 * @param xInd an index specifying that the 1st partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>xInd</sub>
	 * @param yInd an index specifying that the 2nd partial derivative in
	 *        &part;<sup>2</sup> / &part;x<sub>xInd</sub>&part;x<sub>yInd</sub>
	 *        is computed with respect to x<sub>yInd</sub>
	 * @param result the partial derivative of a weighted sum of
	 *         Bernstein polynomials for specified barycentric coordinates
	 * @param beta the weights
	 * @param offset the offset into the array beta at which the weights
	 *        start
	 * @param n the degree of the Bernstein polynomials
	 * @param x the barycentric coordinates
	 * @exception IllegalArgumentException an argument was out of bounds
	 */
	public static void d2sumBdxdy(int xInd, int yInd,
				      double result[], double[] beta,
				      int offset, int n, double... x)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		throw new IllegalArgumentException
		    (errorMsg("argNonNegative5", n));
	    }
	    if (xInd < 0 || xInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", xInd));
	    }
	    if (yInd < 0 || yInd >= x.length) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRangeI", yInd));
	    }
	    int np1 = n+1;
	    int nm1 = n-1;
	    int rlenm1 = result.length-1;
	    double[] b = new double[(int)MathOps.lPow(np1,x.length)];
	    int[] lambdas = new int[x.length];
	    for (int k = 0; k < result.length; k++) {
		int[] lambdaIndices = generateIndicesAux(n, n, x.length);
		int shift = 0;
		int index = 0;
		for (int lindex: lambdaIndices) {
		    b[lindex] = beta[offset + k + (index++)*result.length];
		}
		for(int r = 1; r < nm1; r++) {
		    lambdaIndices = generateIndicesAux(n, n-r, x.length);
		    for (int lindex: lambdaIndices) {
			lambdasForIndex(n, lambdas, lindex);
			for (int i = 0; i < x.length; i++) {
			    lambdas[i]++;
			    index = indexForLambdas(n, lambdas);
			    b[lindex] += x[i]*b[index];
			    lambdas[i]--;
			}
		    }
		}
		if (xInd == yInd) {
		    for (int i = 0; i < x.length; i++) {
			lambdas[i] = (i == xInd)? 2: 0;
		    }
		} else {
		    for (int i = 0; i < x.length; i++) {
			lambdas[i] = ((i == xInd) || (i == yInd))? 1: 0;
		    }
		}
		index = indexForLambdas(n, lambdas);
		result[k] = n*(n-1)*b[index];
		if (k < rlenm1) {
		    java.util.Arrays.fill(b, 0.0);
		}
	    }
	}
    }



    /**
     * Compute a Bernstein polynomial B<sub>i,n</sub>(x).
     * Please see
     * <a href = "http://www.idav.ucdavis.edu/education/CAGDNotes/Bernstein-Polynomials.pdf">Bernstein Polynomials</a>
     * for definitions.
     * @param i the order of the Bernstein polynomial
     * @param n the degree of the Bernstein polynomial
     * @param x the point at which to evaluate the Bernstein polynomial
     * @return the value B<sub>i,n</sub>(x)
     * @exception IllegalArgumentException the second argument was
     *            out of range
     */
    public static double B(int i, int n, double x)
	throws IllegalArgumentException
    {
	if (n < 0) {
	    throw new
		IllegalArgumentException(errorMsg("argNonNegative2", n));
	}
	if (i < 0 || i > n) {
	    return 0.0;
	}
	if (n == 0) return 1.0;
	return Binomial.coefficient(n,i)*Math.pow(x,i)*Math.pow(1.0-x,(n-i));
    }


    /**
     * Compute the derivative of a Bernstein polynomial B<sub>i,n</sub>(x)
     * with respect to x.
     * Please see
     * <a href = "http://www.idav.ucdavis.edu/education/CAGDNotes/Bernstein-Polynomials.pdf">Bernstein Polynomials</a>
     * for definitions, and the expression used to compute the derivative.
     * @param i the order of the Bernstein polynomial
     * @param n the degree of the Bernstein polynomial
     * @param x the point at which to evaluate the Bernstein polynomial's
     *        derivative
     * @return the value of dB<sub>i,n</sub>(x)/dx
     * @exception IllegalArgumentException the second argument was
     *            out of range
     */
    public static double dBdx(int i, int n, double x)
	throws IllegalArgumentException
    {
	return n * (B(i-1, n-1, x) - B(i, n-1, x));
    }

    /**
     * Compute the second derivative of a Bernstein polynomial
     * B<sub>i,n</sub>(x)with respect to x.
     * Please see
     * <a href = "http://www.idav.ucdavis.edu/education/CAGDNotes/Bernstein-Polynomials.pdf">Bernstein Polynomials</a>
     * for definitions.
     * @param i the order of the Bernstein polynomial
     * @param n the degree of the Bernstein polynomial
     * @param x the point at which to evaluate the Bernstein polynomial's
     *        derivative
     * @return the value of d<sup>2</sup>B<sub>i,n</sub>(x)/dx<sup>2</sup>
     * @exception IllegalArgumentException the second argument was
     *            out of range
     */
    public static double d2Bdx2(int i, int n, double x) {
        return n * (dBdx(i-1, n-1, x) - dBdx(i, n-1, x));
    }

    /**
     * Bernstein polynomials for a barycentric basis.
     * A barycentric basis is one in which there are m coordinates
     * &tau; = (&tau;<sub>0</sub>, &tau;<sub>1</sub>, ... , &tau;<sub>m-1</sub>)
     * with the constraint
     * &tau;<sub>0</sub> +  &tau;<sub>1</sub> + ... + &tau;<sub>m-1</sub> = 1.
     * <P>
     * Bernstein polynomials for a barycentric basis are given by the
     * equation
     * <blockquote>
     * <pre>
     *                 n!
     *    B<sup>n</sup><sub>&lambda;</sub>(&tau;) = __________ &tau;<sub>0</sub><sup>&lambda;<sub>0</sub></sup>&tau;<sub>1</sub><sup>&lambda;<sub>1</sub></sup>...&tau;<sub>m-1</sub><sup>&lambda;<sub>m-1</sub></sup>
     *             &lambda;<sub>1</sub>!&lambda;<sub>2</sub>!...&lambda;<sub>m</sub>!
     * </pre>
     * </blockquote>
     * subject to the constraint
     * &lambda;<sub>0</sub> + &lambda;<sub>1</sub> + ... + &lambda;<sub>m-1</sub> = n.
     <P>
     * Please see <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.413.7346&amp;rep=rep1&amp;type=pdf">Gerald Farin, "Triangular Bernstein-B&eacute;zier patches"</a>, which restricts m to 3.
     * @param n the degree of the polynomial
     * @param lambda the order of the polynomial, with each component in the
     *        range [0, n] listed in the order &lambda;<sub>0</sub>,
     *        &lambda;<sub>1</sub>, ... , &lambda;<sub>m-1</sub>
     * @param tau barycentric coordinates at which to evaluate the polynomial,
     *        with these arguments in the order &tau;<sub>0</sub>,
     *        &tau;<sub>1</sub>, ... , &tau;<sub>m-1</sub>
     * @return the value of the Bernstein polynomial.
     * @exception IllegalArgumentException an argument was out of bounds
     */
    public static double B(int n, int[] lambda, double... tau)
	throws IllegalArgumentException
    {
	if (n < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative1", n));
	}
	if (lambda.length != tau.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays")) ;
	}
	int k = 0;
	for (int i = 0; i < lambda.length; i++) {
	    int j = lambda[i];
	    if (j < 0 || j > n) return 0.0;
	    k += j;
	}
	if (k != n) return 0.0;

	double result = factorial(n);
	for (int i = 0; i < tau.length; i++) {
	    result *= MathOps.pow(tau[i], lambda[i]) / factorial(lambda[i]);
	}
	return result;
    }

    /**
     * First partial derivatives of Bernstein polynomials for a
     * barycentric basis.
     * The derivatives are partial derivatives with no constraints on
     * the values of the tau components (barycentric coordinates are
     * usually normalized so that &sum;<sub>i</sub>&tau;<sub>i</sub> = 1.)
     * Please <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.413.7346&amp;rep=rep1&amp;type=pdf">Gerald Farin, "Triangular Bernstein-B&eacute;zier patches"</a>
     * and {@link #B(int, int[], double...)} for additional details about the
     * arguments.
     * @param tauInd the index of the component of the argument tau for which
     *        the partial derivative should be computed.
     * @param n the degree of the polynomial
     * @param lambda the order of the polynomial, with each component in the
     *        range [0, n] listed in the order &lambda;<sub>0</sub>,
     *        &lambda;<sub>1</sub>, ... , &lambda;<sub>m-1</sub>
     * @param tau barycentric coordinates at which to evaluate the polynomial,
     *        with these arguments in the order &tau;<sub>0</sub>,
     *        &tau;<sub>1</sub>, ... , &tau;<sub>m-1</sub>
     * @return the value of the derivative of a Bernstein polynomial
     *         &part;B<sup>n</sup><sub>&lambda;</sub>/&part;&tau;<sub>i</sub>
     *         where i is the value of the argument named tauInd
     * @exception IllegalArgumentException an argument was out of bounds
     */
    public static double dBdx(int tauInd, int n, int[] lambda, double... tau)
	throws IllegalArgumentException
    {
	if (n < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative2", n));
	}
	if (lambda.length != tau.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	int k = 0;
	for (int i = 0; i < lambda.length; i++) {
	    int j = lambda[i];
	    if (j < 0 || j > n) return 0.0;
	    k += j;
	}
	if (k != n) return 0.0;

	double result = factorial(n);
	int tauIndLambda = lambda[tauInd];
	if (tauIndLambda == 0) return 0.0;
	for (int i = 0; i < tau.length; i++) {
	    if (i != tauInd) {
		result *= MathOps.pow(tau[i], lambda[i]) / factorial(lambda[i]);
	    } else if (tauIndLambda > 1) {
		result *=  tauIndLambda * MathOps.pow(tau[i], tauIndLambda-1)
		    / factorial(tauIndLambda);
	    }
	}
	return result;
    }

    /**
     * Second derivative of Bernstein polynomials for a barycentric basis.
     * The derivatives are partial derivatives with no constraints
     * on the values of the tau components (barycentric coordinates are usually
     * normalized so that &sum;<sub>i</sub>&tau;<sub>i</sub> = 1.)
     * Please <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.413.7346&amp;rep=rep1&amp;type=pdf">Gerald Farin, "Triangular Bernstein-B&eacute;zier patches"</a>
     * and {@link #B(int, int[], double...)} for additional details about the
     * arguments.
     * @param tauInd1 the index of the component of the argument tau for which
     *        the initial partial derivative should be computed.
     * @param tauInd2 the index of the component of the argument tau for which
     *        the final partial derivative should be computed.
     * @param n the degree of the polynomial
     * @param lambda the order of the polynomial, with each component in the
     *        range [0, n] listed in the order &lambda;<sub>0</sub>,
     *        &lambda;<sub>1</sub>, ... , &lambda;<sub>m-1</sub>
     * @param tau barycentric coordinates at which to evaluate the polynomial,
     *        with these arguments in the order &tau;<sub>0</sub>,
     *        &tau;<sub>1</sub>, ... , &tau;<sub>m-1</sub>
     * @return the value of the derivative of a Bernstein polynomial
     *         &part;<sup>2</sup>B<sup>n</sup><sub>&lambda;</sub>/(&part;&tau;<sub>i</sub>&part;&tau;<sub>j</sub>)
     *         where i is the value of the argument named tauInd1 and
     *         j is the value of the argument named tauInd2 (both may have
     *         the same value)
     * @exception IllegalArgumentException an argument was out of bounds
     */
    public static double d2Bdxdy(int tauInd1, int tauInd2,
				 int n, int[] lambda, double... tau)
	throws IllegalArgumentException
    {

	if (n < 0) {
	    throw new IllegalArgumentException
		(errorMsg("argNonNegative3", n));
	}
	if (lambda.length != tau.length) {
	    throw new IllegalArgumentException(errorMsg("incompatibleArrays"));
	}
	int k = 0;
	for (int i = 0; i < lambda.length; i++) {
	    int j = lambda[i];
	    if (j < 0 || j > n) return 0.0;
	    k += j;
	}
	if (k != n) return 0.0;

	double result = factorial(n);
	if (tauInd1 == tauInd2) {
	    int tauIndLambda = lambda[tauInd1];
	    if (tauIndLambda < 2) return 0.0;
	    for (int i = 0; i < tau.length; i++) {
		if (i != tauInd1) {
		    result *= MathOps.pow(tau[i], lambda[i])
			/ factorial(lambda[i]);
		} else if (tauIndLambda > 1) {
		    result *=  tauIndLambda * (tauIndLambda -1)
			* MathOps.pow(tau[i], tauIndLambda-2)
			/ factorial(tauIndLambda);
		}
	    }
	} else {
	    int tauIndLambda1 = lambda[tauInd1];
	    int tauIndLambda2 = lambda[tauInd2];
	    if (tauIndLambda1 == 0) return 0.0;
	    if (tauIndLambda2 == 0) return 0.0;
	    for (int i = 0; i < tau.length; i++) {
		if (i == tauInd1) {
		    if (tauIndLambda1 > 1) {
			result *=  tauIndLambda1
			    * MathOps.pow(tau[i], tauIndLambda1-1)
			    / factorial(tauIndLambda1);
		    }
		} else if (i == tauInd2) {
		    if (tauIndLambda2 > 1) {
			result *= tauIndLambda2 * MathOps.pow(tau[i],
							      tauIndLambda2-1)
			    / factorial(tauIndLambda2);
		    }
		} else {
		    result *= MathOps.pow(tau[i], lambda[i])
			/ factorial(lambda[i]);
		}
	    }
	}
	return result;
    }


    /**
     * Class representing Bessel functions.
     */
    static class BesselFunction {

	private BesselFunction() {}

	static final double BESSEL_LIMIT = 1.0e-12;

	static double series(double nu, double x) {
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return series(inu, x);
		}
	    }
	    double hx = x/2.0;
	    hx *= -hx;
	    double sum = 0.0;
	    double c = 0.0;
	    double term;
	    double prev;
	    int j = 0;
	    double hxterm = 1.0;

	    double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));
	    boolean skip = true;
	    do {
		term = hxterm/factorial(j);
		double g = Gamma(j + nu + 1.0);
		if (g == Double.NEGATIVE_INFINITY
		    || g == Double.POSITIVE_INFINITY) {
		    term = 0.0;
		} else {
		    term /= g;
		    // sum += term;
		    double y = term - c;
		    double t = sum + y;
		    c = (t - sum) - y;
		    sum = t;
		    skip = false;
		}
		hxterm *= hx;
		j++;
	    } while (skip || Math.abs(term/limit) > BESSEL_LIMIT);
	    return sum;
	}

	static double modSeries(double nu, double x) {
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return series(inu, x);
		}
	    }
	    double hx = x/2.0;
	    hx *= hx;
	    double sum = 0.0;
	    double c = 0.0;
	    double term;
	    double prev;
	    int j = 0;
	    double hxterm = 1.0;

	    double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));
	    boolean skip = true;
	    do {
		term = hxterm/factorial(j);
		double g = Gamma(j + nu + 1.0);
		if (g == Double.NEGATIVE_INFINITY
		    || g == Double.POSITIVE_INFINITY) {
		    term = 0.0;
		} else {
		    term /= g;
		    // sum += term;
		    double y = term - c;
		    double t = sum + y;
		    c = (t - sum) - y;
		    sum = t;
		    skip = false;
		}
		hxterm *= hx;
		j++;
	    } while (skip || Math.abs(term/limit) > BESSEL_LIMIT);
	    return sum;
	}


	// valid for nu >= 0.
	static double series(int nu, double x) {
	    double hx = x/2.0;
	    hx *= hx;
	    double sum = 0.0;
	    double c = 0.0;
	    double term;
	    double prev;
	    int j = 0;
	    double hxterm = 1.0;

	    double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));

	    do {
		term = (j%2 == 0)? hxterm: -hxterm;
		term /= factorial(j);
		term /= factorial(j + nu);
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		hxterm *= hx;
		j++;
	    } while (Math.abs(term/limit) > BESSEL_LIMIT);
	    return sum;
	}

	// valid for nu >= 0.
	static double modSeries(int nu, double x) {
	    double hx = x/2.0;
	    hx *= hx;
	    double sum = 0.0;
	    double c = 0.0;
	    double term;
	    double prev;
	    int j = 0;
	    double hxterm = 1.0;

	    double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));

	    do {
		term = hxterm;
		term /= factorial(j);
		term /= factorial(j + nu);
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		hxterm *= hx;
		j++;
	    } while (Math.abs(term/limit) > BESSEL_LIMIT);
	    return sum;
	}

	private static final double LOG2 = Math.log(2.0);

	/**
	 * Compute a Bessel function of the first kind for a real-valued order.
	 * @param nu the order of the function
	 * @param x the function's argument
	 * @exception IllegalArgumentException the argument was negative when
	 *            the order was not an integer
	 */
	static double valueAt(double nu, double x)
	   throws IllegalArgumentException
	{
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return valueAt(inu, x);
		}
		if (x == 0.0) {
		    // corner case: integer value of nu but too large to be
		    // represented as an int.
		    return 0.0;
		} else {
		    // out of range.
		    return Double.NaN;
		}
	    }
	    if (x == 0.0) {
		if (nu > 0.0) {
		    return 0.0;
		} else {
		    if (Gamma(nu+1.0) < 0) {
			return Double.NEGATIVE_INFINITY;
		    } else {
			return Double.POSITIVE_INFINITY;
		    }
		}
	    } else if ( x < 0) {
		throw new IllegalArgumentException
		    (errorMsg("secondArgNeg", x));
	    }

	    return Math.pow(x/2.0, nu)* series(nu, x);
	}

	/**
	 * Compute a Bessel function of the first kind for an
	 * integer-valued order.
	 * @param nu the order of the function
	 * @param x the function's argument.
	 */
	static double valueAt(int nu, double x) {
	    if (x == 0.0) {
		if (nu == 0) return 1.0;
		return 0.0;
	    }
	    double sign = (nu < 0.0)?(((-nu)%2 == 0)? 1.0: -1.0): 1.0;
	    if (nu < 0) nu = -nu;
	    double xx = (x < 0.0)? -x: x;
	    sign *= (x < 0)? ((nu%2 == 0)? 1.0: -1.0): 1.0;

	    return sign * Math.exp((Math.log(xx) - LOG2)*nu) * series(nu, xx);
	}

	/**
	 * Compute a modified Bessel function of the first kind (I) for a
	 * real-valued order.
	 * @param nu the order of the function (must be larger than -1)
	 * @param x the function's argument (must be non-negative)
	 * @exception IllegalArgumentException the argument was negative when
	 *            the order was not an integer
	 */
	static double modValueAt(double nu, double x)
	   throws IllegalArgumentException
	{
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return modValueAt(inu, x);
		}
		if (x == 0.0) {
		    // corner case: integer value of nu but too large to be
		    // represented as an int.
		    return 0.0;
		} else {
		    // out of range.
		    return Double.NaN;
		}
	    }
	    if (x == 0.0) {
		if (nu > 0.0) {
		    return 0.0;
		} else {
		    if (Gamma(nu+1.0) < 0) {
			return Double.NEGATIVE_INFINITY;
		    } else {
			return Double.POSITIVE_INFINITY;
		    }
		}
	    } else if ( x < 0) {
		throw new IllegalArgumentException
		    (errorMsg("secondArgNeg", x));
	    }
	    return Math.pow(x/2.0, nu) * modSeries(nu, x);
	}

	/**
	 * Compute a modified Bessel function of the first kind (I)
	 * for an integer-valued order.
	 * @param nu the order of the function
	 * @param x the function's argument.
	 */
	static double modValueAt(int nu, double x) {
	    if (x == 0.0) {
		if (nu == 0) return 1.0;
		return 0.0;
	    }
	    double sign = (nu < 0.0)?(((-nu)%2 == 0)? 1.0: -1.0): 1.0;
	    if (nu < 0) nu = -nu;
	    double xx = (x < 0.0)? -x: x;
	    sign *= (x < 0)? ((nu%2 == 0)? 1.0: -1.0): 1.0;
	    return MathOps.pow(x/2.0, nu) * modSeries(nu, x);
	}

	static double SBfunctP(int n, double z) {
	    double n2 = n/2;
	    double sum = 0.0;
	    double c = 0.0;
	    double sign = 1.0;
	    double z2 = 1.0/(2*z);
	    z2 *= z2;
	    double zz = 1.0;
	    for (int k = 0; k <= n2; k++) {
		int k2 = 2*k;
		// sum += sign*(factorial(n+k2)/(factorial(k2)*factorial(n-k2)))
		//    * zz;
		double y =
		    (sign*(factorial(n+k2)/(factorial(k2) * factorial(n-k2)))
		     * zz) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		zz *= z2;
		sign = -sign;
	    }
	    return sum;
	}

	static double SBfunctQ(int n, double z) {
	    double n12 = (n-1)/2;
	    double sum = 0.0;
	    double c = 0.0;
	    double sign = 1.0;
	    double z2 = 1.0/(2*z);
	    double zz = z2;
	    z2 *= z2;
	    for (int k = 0; k <= n12; k++) {
		int k21 = 2*k + 1;
		if (n-k21 >= 0) {
		    // sum += sign * (factorial(n+k21)
		    //       / (factorial(k21) * factorial(n-k21))) * zz;
		    double y =
			(sign * (factorial(n+k21)
				 / (factorial(k21) * factorial(n-k21))) * zz)
			- c;
		    double t = sum + y;
		    c = (t - sum) - y;
		    sum = t;
		}
		zz *= z2;
		sign = - sign;
	    }
	    return sum;
	}
    }

    /**
     * Compute a Bessel function of the first kind for a real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @exception IllegalArgumentException the argument was negative when
     *            the order was not an integer
     */
    public static double J(double nu, double x)
	throws IllegalArgumentException
    {
	return BesselFunction.valueAt(nu, x);
    }

    /**
     * Compute a Bessel function of the first kind for an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     */
    public static double J(int nu, double x) {
	return BesselFunction.valueAt(nu, x);
    }

    /**
     * Compute the derivative of Bessel function of the first kind for
     * an real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[J<sub>&nu;</sub>(x)]/dx
     */
    public static double dJdx(double nu, double x) {
	if (nu == 0.0) return -J(1, x);
	return (J(nu-1.0, x) - J(nu + 1.0, x))/2.0;
    }

    /**
     * Compute the derivative of  Bessel function of the first kind for
     * an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[J<sub>&nu;</sub>(x)]/dx
     */
    public static double dJdx(int nu, double x) {
	if (nu == 0) return -J(1, x);
	return (J(nu-1.0, x) - J(nu + 1.0, x))/2.0;
    }

    /**
     * Compute a Bessel function of the second kind for a real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @exception IllegalArgumentException the argument was negative when
     *            the order was not an integer
     */
    public static double Y(double nu, double x)
	throws IllegalArgumentException
    {
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return Y(inu, x);
		}
		// too large to handle.
		return Double.NaN;
	    }
	    return (J(nu,x)* Math.cos(Math.PI*nu) - J(-nu,x))
		/ Math.sin(Math.PI * nu);
    }

    /**
     * Compute a Bessel function of the second kind for an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     */
    public static double Y(int nu, double x) {
	double x2 = x/2.0;
	double x22 = x2*x2;
	double xt = 1.0;
	double xx = 1.0;
	double sum = 0.0;
	double c = 0.0;
	double term;
	int k = 0;

	double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));
	double sign = 1.0;

	if (nu == 0) {
	    sum += (Math.log(x2) + Constants.EULERS_CONSTANT) * J(0,x);
	    double fterm;
	    double fsum  = 0.0;
	    double fc = 0.0;
	    do {
		k++;
		// fsum += 1.0/k;
		double fy = (1.0/k) - fc;
		double ft = fsum + fy;
		fc = (ft - fsum) - fy;
		fsum = ft;
		fterm = factorial(k);
		fterm *= fterm;
		xt *= x22;
		term = ((k%2 == 0)? -1: 1)*fsum*xt/fterm;
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    } while (Math.abs(term/limit) > BesselFunction.BESSEL_LIMIT);
	    return sum * (2.0/Math.PI);
	} else if (nu < 0) {
	    nu = -nu;
	    sign = (nu%2 == 0)? 1.0: -1.0;
	}

	for (k = 0; k < nu; k++) {
	    // sum += (factorial(nu-k-1)/factorial(k))*xx;
	    double y = ((factorial(nu-k-1)/factorial(k))*xx) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    xx *= x22;
	    xt *= x2;
	}
	sum /= -(xt * Math.PI);
	sum += (2.0/Math.PI) * Math.log(x2)*J(nu,x);

	k = 0;
	xx = 1.0;
	double sum2 = 0.0;
	c = 0.0;
	x22 = -x22;
	do {
	    term = (digamma(k+1) + digamma(nu+k+1)) * xx
		/(factorial(k)*factorial(nu+k));
	    // sum2 += term;
	    double y = term - c;
	    double t = sum2 + y;
	    c = (t - sum2) - y;
	    sum2 = t;
	    xx *= x22;
	    k++;
	} while (Math.abs(term/limit) > BesselFunction.BESSEL_LIMIT);
	sum -= (xt/Math.PI)*sum2;
	return sign * sum;
    }

    /**
     * Compute the derivative of Bessel function of the second kind for
     * a real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[Y<sub>&nu;</sub>(x)]/dx
     */
    public static double dYdx(double nu, double x) {
	return (Y(nu-1.0, x) - Y(nu + 1.0, x))/2.0;
    }

    /**
     * Compute the derivative of Bessel function of the second kind for
     * an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[Y<sub>&nu;</sub>(x)]/dx
     */
    public static double dYdx(int nu, double x) {
	return (Y(nu-1.0, x) - Y(nu + 1.0, x))/2.0;
    }

    /**
     * Compute a modified Bessel function of the first kind (I) for a
     * real-valued order.
     * @param nu the order of the function
     * @param x the function's argument
     * @return the value of I<sub>&nu;</sub>(x)
     * @exception IllegalArgumentException the argument was negative when
     *            the order was not an integer
     */
    public static double I(double nu, double x)
	throws IllegalArgumentException
    {
	return BesselFunction.modValueAt(nu, x);
    }

    /**
     * Compute a modified Bessel function of the first kind (I) for an
     * integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of I<sub>&nu;</sub>(x)
     */
    public static double I(int nu, double x) {
	return BesselFunction.modValueAt(nu, x);
    }

    /**
     * Compute the derivative of modified Bessel function of the first
     * kind (I) for a real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     */
    public static double dIdx(double nu, double x) {
	return (I(nu-1.0, x) + I(nu+1.0, x))/2.0;
    }

    /**
     * Compute the derivative of modified Bessel function of the first
     * kind (I) for an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     */
    public static double dIdx(int nu, double x) {
	return (I(nu-1.0, x) + I(nu+1.0, x))/2.0;
    }

    /**
     * Compute a modified Bessel function of the second kind (K) for a
     * real-valued order.
     * @param nu the order of the function (must be larger than -1)
     * @param x the function's argument.
     * @exception IllegalArgumentException the argument was negative when
     *            the order was not an integer
     */
    public static double K(double nu, double x)
	throws IllegalArgumentException
    {
	    if (nu == Math.rint(nu)) {
		// use the integer form when possible
		long lnu = Math.round(nu);
		if (lnu <= Integer.MAX_VALUE && lnu >= Integer.MIN_VALUE) {
		    int inu = (int)lnu;
		    return K(inu, x);
		}
		// too large to handle.
		return Double.NaN;
	    }
	    return (Math.PI/(2.0*Math.sin(nu*Math.PI)))
		    * (I(-nu, x) - I(nu, x));
    }

    /**
     * Compute a modified Bessel function of the second kind (K) for
     * an integer-valued order.
     * @param nu the order of the function (must be larger than -1)
     * @param x the function's argument.
     */
    public static double K(int nu, double x) {
	if (x == 0.0) return Double.POSITIVE_INFINITY;
	double x2 = x/2.0;
	double x22 = x2*x2;
	double xt = 1.0;
	double xx = 1.0;
	double sum = 0.0;
	double c = 0.0;
	double term;
	int k = 0;

	double limit = 1.0/Math.sqrt(1.0 + Math.abs(x));
	double sign = 1.0;

	if (nu == 0) {
	    sum += -(Math.log(x2) + Constants.EULERS_CONSTANT) * I(0,x);
	    double fterm;
	    double fsum  = 0.0;
	    double fc = 0.0;
	    do {
		k++;
		// fsum += 1.0/k;
		double fy = (1.0/k) - fc;
		double ft = fsum + fy;
		fc = (ft - fsum) - fy;
		fsum = ft;
		fterm = factorial(k);
		fterm *= fterm;
		xt *= x22;
		term = fsum*xt/fterm;
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    } while (Math.abs(term/limit) > BesselFunction.BESSEL_LIMIT);
	    return sum;
	} else if (nu < 0) {
	    nu = -nu;
	}
	sign = (nu%2 == 0)? 1.0: -1.0;
	double nx22 = -x22;
	for (k = 0; k < nu; k++) {
	    // sum += (factorial(nu-k-1)/factorial(k))*xx;
	    double y = ((factorial(nu-k-1)/factorial(k))*xx) - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    xx *= nx22;
	    xt *= x2;
	}
	sum /= (xt * 2.0);
	sum -= sign * Math.log(x2)*I(nu,x);

	k = 0;
	xx = 1.0;
	double sum2 = 0.0;
	c = 0.0;
	do {
	    term = (digamma(k+1) + digamma(nu+k+1)) * xx
		/(factorial(k)*factorial(nu+k));
	    // sum2 += term;
	    double y = term - c;
	    double t = sum2 + y;
	    c = (t - sum2) - y;
	    sum2 = t;
	    xx *= x22;
	    k++;
	} while (Math.abs(term/limit) > BesselFunction.BESSEL_LIMIT);
	sum += sign*(xt/2.0)*sum2;
	return sum;
    }

    /**
     * Compute the derivative of modified Bessel function of the second
     * kind (K) for a real-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[K<sub>&nu;</sub>(x)]/dx
     */
    public static double dKdx(double nu, double x) {
	return -(K(nu-1, x) + K(nu+1, x))/ 2.0;
    }

    /**
     * Compute the derivative of modified Bessel function of the second
     * kind (K) for an integer-valued order.
     * @param nu the order of the function
     * @param x the function's argument.
     * @return the value of d[K<sub>&nu;</sub>(x)]/dx
     */
    public static double dKdx(int nu, double x) {
	return -(K(nu-1, x) + K(nu+1, x))/ 2.0;
    }


    /**
     * Compute the value of a spherical Bessel function of the first kind.
     * @param n the order of the function
     * @param x the argument for the function
     * @return the value of j<sub>n</sub>(x)
     */
    public static double j(int n, double x) {
	if (n < 0) {
	    int nn = -n;
	    return ((nn%2 == 0)?1.0: -1.0)*y(nn-1, x);
	}
	if (Math.abs(x) < 1.0) {
	    double x22 = -(x*x)/2.0;
	    double sum = 0.0;
	    double c = 0.0;
	    double term = 1.0;
	    double factor = 1.0;
	    int k = 0;
	    int n2 = n*2;
	    do {
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		term *= x22;
		k++;
		term /= k;
		term /= (n2 + 2*k + 1);
	    } while (Math.abs(term/sum) > BesselFunction.BESSEL_LIMIT);
	    return sum * MathOps.pow(x, n)/oddFactorial(n2 + 1);
	} else {
	    return
		(BesselFunction.SBfunctP(n, x) * Math.sin(x - n*(Math.PI/2))
		 + BesselFunction.SBfunctQ(n, x) * Math.cos(x - n*(Math.PI/2)))
		/ x;
	}
    }

    /**
     * Compute the derivative of a spherical Bessel function of the first
     * kind.
     * @param n the order of the function
     * @param x the argument for the function
     * @return the value of d[j<sub>n</sub>(x)]/dx
     */
	 public static double djdx(int n, double x) {
	return (n*j(n-1, x) - (n+1)*j(n+1, x))/(2*n+1);
    }

    /**
     * Compute the value of a spherical Bessel function of the second kind.
     * @param n the order of the function
     * @param x the argument for the function
     * @return the value of j<sub>n</sub>(x)
     */
    public static double y(int n, double x) {
	if (n < 0) {
	    int nn = -n;
	    return (((nn)%2 == 0)? -1.0: 1.0) * j(nn-1, x);
	}
	if (n == 0) {
	    if (x == 0.0) return Double.NEGATIVE_INFINITY;
	    return - Math.cos(x)/x;
	}
	if (Math.abs(x) < 1.0) {
	    double x22 = -(x*x)/2.0;
	    double sum = 0.0;
	    double c = 0.0;
	    double term = 1.0;
	    double factor = 1.0;
	    int k = 0;
	    int n2 = n*2;
	    int n2p1 = n2+1;
	    do {
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		term *= x22;
		k++;
		term /= k;
		term /= (2*k - n2p1);
	    } while (Math.abs(term/sum) > BesselFunction.BESSEL_LIMIT);
	    return -sum*Functions.oddFactorial(n2-1)/MathOps.pow(x, n+1);
	} else {
	    double sign = (n%2 == 0)? -1.0: 1.0;
	    return sign *
		(BesselFunction.SBfunctP(n, x) * Math.cos(x + n*(Math.PI/2))
		 - BesselFunction.SBfunctQ(n, x) * Math.sin(x + n*(Math.PI/2)))
		/ x;
	}
    }

    /**
     * Compute the derivative of a spherical Bessel function of the second
     * kind.
     * @param n the order of the function
     * @param x the argument for the function
     * @return the value of d[y<sub>n</sub>(x)]/dx
     */
    public static double dydx(int n, double x) {
	return (n*y(n-1, x) - (n+1)*y(n+1, x))/(2*n+1);
    }


    /**
     * Class representing Legendre polynomials.
     */
    public static class LegendrePolynomial {

	private LegendrePolynomial() {}

	/**
	 * Evaluate a Legendre polynomial.
	 * @param n the order of the Legendre polynomial
	 * @param x the point at which the polynomial should be evaluated
	 * @return the value of the polynomial
	 */
	public static double valueAt(int n, double x) {
	    if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argOutOfRangeI", n));
	    if (n == 0) return 1.0;
	    else if (n == 1) return x;
	    else {
		// make it exact for special cases.
		if (x == 1.0) {
		    return 1.0;
		} else if (x == -1.0) {
		    return (n%2 == 0)? 1.0: -1.0;
		} else if (x == 0.0 && (n%2) == 1) {
		    return  0.0;
		}
		double lastlast = 1.0;
		double last = x;
		for (int ind = 1; ind < n; ind++) {
		    double next = ((2*ind + 1)*x*last - ind*lastlast)/(ind+1);
		    lastlast = last;
		    last = next;
		}
		return last;
	    }
	}

	/**
	 * Evaluate Legendre polynomials for multiple orders.
	 *
	 * @param n the maximum order or degree of the polynomial
	 * @param x the point at which the polynomial is to be evaluated
	 * @param results an array of at least size n+1 to hold values for
	 *        orders 0 to n
	 * @return the value for order n at point x
	 */
	public static double valueUpTo(int n, double x, double[] results) {
	    if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argOutOfRangeI", n));
	    if (n == 0) {
		results[0] = 1.0;
		return 1.0;
	    } else if (n == 1) {
		results[0] = 1.0;
		results[1] = x;
		return x;
	    } else {
		results[0] = 1.0;
		results[1] = x;
		for (int ind = 1; ind < n; ind++) {
		    results[ind+1] =
			((2*ind+1)*x*results[ind] - ind*results[ind-1])/(ind+1);
		}
		return results[n];
	    }
	}

	/**
	 * Compute associated Legendre functions with integer coefficients.
	 * <P>
	 * Implementation note: Using Rodrigues' formula yields
	 * P<sup>n</sub><sup>m</sup>(x) = (-1)<sup>m</sup>(1-x<sup>2</sup>)<sup>m/2</sup> (d<sup>n+m</sup>/dx<sup>n+m</sup>)(x<sup>2</sup>-1)<sup>n</sup>.
	 * The term (x<sup>2</sup>-1)<sup>n</sup> can be expanded using
	 * the binomial theorem to a sum over i of terms that are equal
	 * to C(n,i)(-1)<sup>i</sup>x<sup>2(n-i)</sup>.  If we differentiate
	 * n+m times, the only nonzero values are for i less than or equal to
	 * the smallest integer such that 2(n-i) is larger than or equal to
	 * n+m: i.e., i goes from 0 to the largest integer no larger than
	 * (n-m)/2.  For the i<sup>th</sup> term, let k = n-2i. After
	 * differentiation, the i<sup>th</sup> term will contain a factor
	 * of x<sup>k-m</sup>. In addition to the (-1)<sup>i</sup> factor
	 * and the C(n,i) factor, differentiating x<sup>2(n-i)</sup> n+m
	 * times yields a factor of (2(n-i))!/(2(n-i)-(n+m))!, which is
	 * equal to (2(n-i))!/(n-m-2i)! and which in turn is equal to
	 * C(2(n-1),n)C(k,m)n!m! with the n! conveniently canceling a factor
	 * of 1/n! in Rodrigues' formula.
	 * @param n the degree
	 * @param m the order
	 * @param x the argument
	 * @return the value of P<sup>m</sup><sub>n</sub>(x)
	 * @exception IllegalArgumentException an argument is out of range.
	 */
	public static double valueAt(int n, int m, double x)
	    throws IllegalArgumentException
	{
	    if (n < 0 || m > n || -m > n) {
		throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, m));
	    }
	    if (m == 0) {
		return valueAt(n, x);
	    } else if (m < 0) {
		m = -m;
		double sign = (m%2 ==0)? 1.0: -1.0;
		return sign*valueAt(n,m,x)*factorial(n+m)/factorial(n-m);
	    }
	    int nm2 = (n-m)/2;
	    boolean flag = (m%2 == 0);
	    double sign = flag? 1.0: -1.0;
	    double factor = 1.0 - x*x;
	    if (flag) {
		factor = MathOps.pow(factor, m/2);
	    } else {
		if (factor < 0.0)
		    throw new IllegalArgumentException
			(errorMsg("thirdArgOutOfRange", x));
		factor = MathOps.pow(factor, m/2)*Math.sqrt(factor);
	    }
	    if (n > 0) {
		if (n < 62) {
		    factor /= (2L << (n-1));
		} else {
		    factor /= MathOps.pow(2.0, n);
		}
	    }
	    factor *= sign;
	    double sum = 0.0;
	    double c = 0;
	    sign = 1.0;
	    for (int i = 0; i <= nm2; i++) {
		int k = n - 2 * i;
		double term = sign*Binomial.coefficient(n,i)
		    * Binomial.coefficient(2*(n-i), n)
		    * Binomial.coefficient(k,/*k-*/m)*factorial(m)
		    * MathOps.pow(x,k-m);
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		sign = - sign;
	    }
	    return factor * sum;
	}

	/**
	 * Compute the derivative of an associated Legendre function of degree n
	 * and order m
	 * @param n the degree of the associated Legendre function
	 * @param m the order of the associated Legendre function
	 * @param x the point at which the polynomial is evaluated
	 * @param results an array of size 2 holding the values of
	 *        the Legendre function at x for degree n and order m
	 *        at index 0 and its  derivative at index 1
	 * @return the derivative at point x
	 */
	public static double derivative(int n, int m, double x,
					double[] results)
	    throws IllegalArgumentException
	{
	    if (n < 0 || m > n || -m > n) {
		throw new IllegalArgumentException
		    (errorMsg("argsOutOfRange2", n, m));
	    }
	    if (m == 0) {
		return derivative(n, x, results);
	    } else if (m < 0) {
		m = -m;
		double sign = (m%2 ==0)? 1.0: -1.0;
		double result = sign*derivative(n,m,x,results);
		if (results != null) {
		    results[1] = sign *
			results[1]*factorial(n+m)/factorial(n-m);
		}
		// return sign*valueAt(n,m,x)*factorial(n+m)/factorial(n-m);
	    }
	    int nm2 = (n-m)/2;
	    boolean flag = (m%2 == 0);
	    double sign = flag? 1.0: -1.0;
	    double factor = 1.0 - x*x;
	    double dfactor;
	    if (flag) {
		dfactor = (m/2 == 0)? 0.0: -x*(m)*MathOps.pow(factor, (m/2)-1);
		factor = MathOps.pow(factor, m/2);
	    } else {
		if (factor < 0.0)
		    throw new IllegalArgumentException
			(errorMsg("thirdArgOutOfRange", x));
		dfactor = -x*(m)*MathOps.pow(factor, (m/2)-1)
		    * Math.sqrt(factor);
		factor = MathOps.pow(factor, m/2)*Math.sqrt(factor);
	    }
	    if (n > 0) {
		if (n < 62) {
		    factor /= (2L << (n-1));
		    dfactor /= (2L << (n-1));
		} else {
		    factor /= MathOps.pow(2.0, n);
		    dfactor /= MathOps.pow(2.0, n);
		}
	    }
	    factor *= sign;
	    dfactor *= sign;
	    double sum = 0.0;
	    double dsum = 0.0;
	    double c = 0.0;
	    double dc = 0.0;
	    sign = 1.0;
	    for (int i = 0; i <= nm2; i++) {
		int k = n - 2 * i;
		double factors = sign*Binomial.coefficient(n,i)
		    * Binomial.coefficient(2*(n-i), n)
		    * Binomial.coefficient(k,/*k-*/m)*factorial(m);
		double term = factors * MathOps.pow(x,k-m);
		// sum += term;
		double y = term - c;
		double t  = sum + y;
		c = (t - sum) - y;
		sum = t;
		if (k-m > 0) {
		    double dterm = factors *(k-m) * MathOps.pow(x,k-m-1);
		    // dsum += dterm;
		    double dy = dterm - dc;
		    double dt = dsum + dy;
		    dc = (dt - dsum) - dy;
		    dsum = dt;
		}
		sign = - sign;
	    }
	    double result = dfactor * sum + factor * dsum;
	    if (results != null) {
		results[0] = factor * sum;
		results[1] = result;
	    }
	    return result;
	}


	static private final double limit = 0.001;

	/**
	 * Compute the derivative of a Legendre polynomial of order n
	 * @param n the degree of the polynomial
	 * @param x the point at which the polynomial is evaluated
	 * @param results an array of size 2 holding the values of
	 *        the Legendre polynomial at x for degree n at index 0 and its
	 *        derivative at index 1
	 * @return the derivative at point x
	 */
	static double derivative(int n, double x, double[] results) {
	    if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	    if (n == 0) {
		if (results != null) {
		    results[0] = 1.0;
		    results[1] = 0.0;
		}
		return 0.0;
	    } else if (n == 1) {
		if (results != null) {
		    results[0] = x;
		    results[1] = 1.0;
		}
		return 1.0;
	    } else if (x == 1.0) {
		if (results != null) {
		    results[0] = 1.0;
		    results[1] = (double)((n*(n+1))/2);
		}
		return (double)((n*(n + 1))/2);
	    } else if (x == -1.0) {
		if (results != null) {
		    results[0] = ((n%2 == 0)? 1.0: -1.0);
		    results[1] = (double)
			((n%2 == 0)? -(n*(n+1))/2: (n*(n+1))/2);
		}
		return (double)((n%2 == 0)? -(n*(n+1))/2: (n*(n+1))/2);
	    } else {
		if (Math.abs((Math.abs(x) - 1.0)) > limit) {
		    double lastlast = 1.0;
		    double last = x;
		    double next = 0.0;
		    for (int ind = 1; ind < n; ind++) {
			next = ((2*ind + 1)*x*last - ind*lastlast)/(ind+1);
			lastlast = last;
			last = next;
		    }
		    if (results != null) {
			results[0] = last;
			results[1] = (n*(x*last - lastlast))/(x*x-1);
		    }
		    return (n*(x*last - lastlast))/(x*x-1);
		} else {
		    // avoid division by x*x-1, which would reduce
		    // numerical accuracy when x*x is close to 1.0
		    double lastlast = 1.0;
		    double last = x;
		    double next = 0.0;
		    int nm1 = n - 1;
		    double sum = (nm1%2 == 0)? lastlast: 0.0;
		    double c = 0.0;
		    for (int ind = 1; ind < n; ind++) {
			next = ((2*ind + 1)*x*last - ind*lastlast)/(ind+1);
			if (nm1%2 == ind%2) {
			    // sum += (2*ind+1)*last;
			    double y = ((2*ind+1)*last) - c;
			    double t = sum + y;
			    c = (t - sum) - y;
			    sum = t;
			}
			lastlast = last;
			last = next;
		    }
		    if (results != null) {
			results[0] = last;
			results[1] = sum;
		    }
		    return sum;
		}
	    }
	}

	private static final double ACCURACY = 1.0e-15;
	private static final int ITERATION_LIMIT = 100;


	/**
	 * Compute the roots of a Legendre polynomial of order n
	 * A heuristic algorithm is used to estimate the iteration limit
	 * (the number of iterations before the accuracy is relaxed). The
	 * default accuracy requires a root to differ from 0 by 1 part in
	 * 1.0E15, but may be relaxed if the method is not converging on a
	 * root in a given number of iterations.
	 *
	 * @param n the order of the polynomial
	 * @param r an array of size n holding the roots of the polynomial
	 * @return the number of roots
	 */
	public static final int roots(int n, double[] r) {
	    return
		roots(n, r, (n < 50)? ITERATION_LIMIT:
		      ITERATION_LIMIT*(n/50),  ACCURACY);
	}
	/**
	 * Compute the roots of a Legendre polynomial of order n with
	 * a specified iteration limit.
	 * This method provides more fine-grained control regarding an
	 * accuracy versus computing time trade-off.
	 * @param n the order of the polynomial
	 * @param r an array of size n holding the roots of the polynomial
	 * @param iterationLimit the number of iterations before relaxing
	 *        the accuracy of the computation
	 * @return the number of roots
	 */ 
	public static final int roots(int n, double[] r, int iterationLimit)
	{
	    return roots(n, r, iterationLimit, ACCURACY);
	}
	/**
	 * Compute the roots of a Legendre polynomial of order n with
	 * a specified accuracy and iteration limit.
	 * This method provides more fine-grained control regarding an
	 * accuracy versus computing time trade-off.
	 * @param n the order of the polynomial
	 * @param r an array of size n holding the roots of the polynomial
	 * @param iterationLimit the number of iterations before relaxing
	 *        the accuracy of the computation
	 * @param accuracy the accuracy for convergence.  If the value at
	 *        a possible root exceeds the accuracy, iteration continues,
	 *        but if the iteration limit is passed, the accuracy value
	 *        is increased by a factor of 10.
	 * @return the number of roots
	 */ 
	public static final int roots(int n, double[] r,
				      int iterationLimit, double accuracy)
	{
	    if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	    if (n == 0) return 0;
	    int ind = 0;

	    if (iterationLimit <= 0) {
		iterationLimit = (n < 50)? ITERATION_LIMIT:
		    ITERATION_LIMIT*(n/50);
	    }

	    // find first root:
	    double[] tmp1 = new double[2];
	    double[] tmp2 = new double[2];
	    double x = -1.0;
	    double c = 0.0;
	    derivative(n, x, tmp1);
	    // double accuracy = ACCURACY;
	    int cnt = 0;
	    while (Math.abs(tmp1[0]) > accuracy) {
		// x -= tmp1[0] / (tmp1[1]);
		double y = (-(tmp1[0] / (tmp1[1]))) - c;
		double t = x + y;
		c = (t - x) - y;
		x = t;
		if (false) {
		    System.out.println("trying x = " +x + ", incr = -("
				       + tmp1[0] +")/(" + tmp1[1] +")" 
				       + ", accuracy = " + accuracy);
		}
		derivative(n, x, tmp1);
		if (cnt++ > iterationLimit) {
		    accuracy *= 10.0;
		    cnt = 0;
		}
	    }
	    // System.out.println("found root " +ind);
	    r[ind++] = x;
	    double delta = (x + 1.0)/2.0;
	    // x += delta;
	    double y1 = delta - c;
	    double t1 = x + y1;
	    c = (t1 - x) - y1;
	    x = t1;
	    derivative(n, x, tmp2);
	    while (x < 1.0) {
		double lastx = x;
		double[] tmp = tmp1;
		tmp1 = tmp2;
		tmp2 = tmp;
		// x += delta;
		y1 = delta - c;
		t1 = x + y1;
		c = (t1 - x) - y1;
		x = t1;
		derivative(n, x, tmp2);
		if (Math.abs(tmp2[0]) <= ACCURACY) {
		    // accidentally landed on a root
		    // System.out.println("found root " +ind);
		    r[ind++] = x;
		    // x += delta;
		    y1 = delta - c;
		    t1 = x + y1;
		    c = (t1 - x) - y1;
		    x = t1;
		    derivative(n, x, tmp2);
		} else {
		    if (Math.signum(tmp2[0]) != Math.signum(tmp1[0])) {
			// we are straddling a root
			double xr = lastx;
			while (Math.abs(tmp1[0]) >= accuracy) {
			    xr -= tmp1[0] / tmp1[1];
			    if (false) {
				System.out.println("trying x = " 
						   +x + ", incr = -("
						   + tmp1[0] 
						   +")/(" + tmp1[1] +")" 
						   + ", accuracy = " 
						   + accuracy);
			    }
			    derivative(n, xr, tmp1);
			    if (cnt++ > iterationLimit)
			    {
				accuracy *= 10.0;
				cnt = 0;
			    }
			}
			// System.out.println("found root " +ind);
			r[ind++] = xr;
		    }
		}
	    }
	    if (n%2 == 1) {
		// the middle root is zero when n is odd.
		r[n/2] = 0.0;
	    }
	    return ind;
	}

	private static double[] coefficients(int n)
	    throws IllegalArgumentException
	{
	    if (n < 0) {
		String msg = errorMsg("argNonNegative", n);
		throw new IllegalArgumentException(msg);
	    }
	    double[] coefficients = new double[n+1];
	    int floor = n/2;
	    int n2 = n*2;
	    double factor = 1.0/MathOps.pow(2.0, n);
	    for (int k = 0; k <= floor; k++) {
		int k2 = 2*k;
		double sign = (k%2 == 0)? 1: -1;
		coefficients[n-k2] = factor * sign
		    * Binomial.coefficient(n, k)
		    * Binomial.coefficient(n2 - k2, n);
	    }
	    return coefficients;
	}

	/**
	 * Create a Legendre polynomial of degree n using a monomial basis.
	 * @param n the degree of the Legendre polynomial
	 * @return the polynomial
	 * @exception the argument was negative
	 */
	public static Polynomial asPolynomial(int n)
	    throws IllegalArgumentException
	{
	    return new Polynomial(coefficients(n), n);
	}

	/**
	 * Create a Legendre polynomial of degree n using a Bernstein basis.
	 * @param n the degree of the Legendre polynomial
	 * @return the polynomial
	 * @exception the argument was negative
	 */
	public static BezierPolynomial asBezierPolynomial(int n)
		    throws IllegalArgumentException
	{
	    return new
		BezierPolynomial(Polynomials.toBezier(coefficients(n), n));
	}
    }

    /**
     * Evaluate a Legendre polynomial.
     * @param n the order of the Legendre polynomial
     * @param x the point at which the polynomial should be evaluated
     * @return the value of the polynomial
     */
    public static double P(int n, double x) {
	return LegendrePolynomial.valueAt(n, x);
    }

    /**
     * Evaluate the derivative of a Legendre polynomial.
     * @param n the degree of the Legendre polynomial
     * @param x the point at which the derivative should be evaluated
     * @return the value of the derivative
     */
    public static double dPdx(int n, double x) {
	return LegendrePolynomial.derivative(n, x, null);
    }

    /**
     * Evaluate the derivative of an associated Legendre function.
     * @param n the degree of the Legendre function
     * @param m the order of the Legendre function
     * @param x the point at which the derivative should be evaluated
     * @return the value of the derivative d[P<sub>n</sub><sup>m</sup>(x)]/dx
     */
    public static double P(int n, int m, double x) {
	return LegendrePolynomial.valueAt(n,m,x);
    }

    /**
     * Compute the derivative of an associated Legendre function of degree n
     * and order m.
     * @param n the degree of the Legendre function
     * @param m the order of the Legendre function
     * @param x the point at which the Legendre function is evaluated
     * @return the derivative at point x
     */
    public static double dPdx(int n, int m, double x) {
	return LegendrePolynomial.derivative(n, m, x, null);
    }


    static double ZETA_LIMIT = 1.0e-14;

    /**
     * Compute the Riemann zeta function.
     * @param x a real-valued argument.
     * @return the value &zeta;(x) for values that can be expressed
     *         as double-precision numbers; negative or positive infinity
     *         for other cases as appropriate.
     */
    public static double zeta(double x) {
	if (x == Math.rint(x)) {
	    if (x >= 0.0 && x <= 10.0) {
		// make use of closed-form values and oeis.org precomputed
		// values - above 10.0, the series converges very quickly
		// so we can do non-negative integer values very fast.
		if (x == 0.0) return -0.5;
		else if (x == 1.0) return Double.POSITIVE_INFINITY;
		else if (x == 2.0) return Math.PI*Math.PI / 6.0;
		else if (x == 3.0)
		    return 1.202056903159594285399738161511449990764986292;
		else if (x == 4.0) {
		    double u = Math.PI*Math.PI;
		    return u*u/90.0;
		} else if (x == 5.0)
		    return 1.036927755143369926331365486457034168;
		else if (x == 6.0) {
		    double u = Math.PI*Math.PI;
		    double uu = u*u;
		    return u*uu/945.0;
		} else if (x == 7.0)
		    return 1.00834927738192282683979754984979675959986356;
		if (x == 8.0) {
		    double u = Math.PI*Math.PI;
		    double uu = u*u;
		    return uu*uu/9450.0;
		} else if (x == 9.0)
		    return 1.0020083928260822144178527692324120604856;
		if (x == 10.0) {
		    double u = Math.PI*Math.PI;
		    double uu = u*u;
		    return uu*uu*u/93555.0;
		}
	    }
	    long xi = Math.round(x);
	    if (xi%2 == 0 && xi > 0) {
		int ni = (int) xi;
		if (ni == xi && xi <= Constants.BernoulliNumberMaxIndex) {
		    int nid2 = ni/2;
		    int sign = (nid2%2 == 0)? -1: 1;
		    /*
		    double logFactorial = 0.0;
		    for (int i = 1; i <= xi; i++) {
			logFactorial += Math.log((double)i);
		    }
		    */
		    double tmp = Constants.BernoulliNumber2((int)xi);
		    double result =
			(Math.exp(Math.log(sign*tmp) + Math.log(2*Math.PI)*x
				  - Math.log(2.0) - logFactorial((int)xi)));
		    return result;
		}
	    } else if (xi < 0 && xi > Integer.MIN_VALUE + 1) {
		if (x == -1.0) return -1.0/12.0;
		int n = (int)(-xi);
		int np1 = n+1;
		if (np1 <= Constants.BernoulliNumberMaxIndex) {
		    return - Constants.BernoulliNumber2(np1)/np1;
		} else {
		    if (np1%2 == 1) return 0.0;
		    int m = np1/2;
		    if (m%2 == 1) return Double.NEGATIVE_INFINITY;
		    else return Double.POSITIVE_INFINITY;
		}
	    }
	}

	// a couple of special cases not covered above are important ones,
	// for certain applications, so we'll do those by table lookup.
	if (x == 0.5) return -1.460354508809586812889499152515298;
	else if (x == 1.5)
	    return 2.6123753486854883433485675679240716305708;

	if (x > 5.0) {
	    // for positive x sufficiently large, the usual definition
	    // as an infinite series works quite well and converges quickly.
	    double result = 1.0;
	    double c = 0.0;
	    double term;
	    int n = 2;
	    do {
		term = Math.log((double)(n++))*x;
		if (term > 37.0) {
		    term = 0.0;
		} else {
		    term = Math.exp(-term);
		    // result += term;
		    double y = term - c;
		    double t = result + y;
		    c = (t - result) - y;
		    result = t;
		}
	    } while (term > ZETA_LIMIT);
	    return result;
	} else if (x < 0.0) {
	    // use functional equation.
	    double tmp = Math.log(2)*x + Math.log(Math.PI)*(x-1.0)
		+ logGamma(1.0-x) + Math.log(zeta(1.0-x));
	    return Math.sin(Math.PI*x/2.0) * Math.exp(tmp);
	} else {
	    // compute using the series (Hasse)
	    // given in http://mathworld.wolfram.com/RiemannZetaFunction.html
	    // http://en.wikipedia.org/wiki/Riemann_zeta_function
            // #Globally_convergent_series
	    double result = 1.0 / (1 - Math.exp(Math.log(2)*(1-x)));
	    long[] binomial; // = new long[65];
	    // long[] prev = new long[65];
	    // long[] tmp;
	    double term1 = 0.5;
	    double term;
	    double sum1 = term1;
	    double c1 = 0.0;
	    // Binomial.coefficients(prev, 0);
	    int n = 1;
	    do {
		double sum2 = 0.0;
		double c2 = 0.0;
		term1 /= 2.0;
		binomial = Binomial.table[n];
		// Binomial.coefficients(prev, binomial, n);
		for (int k = 0; k <= n; k++) {
		    double nok = (k%2 == 0)? 1.0: -1.0;
		    double term2 = Math.exp(Math.log(1.0 + k)*(-x));
		    // sum2 += nok*binomial[k]*term2;
		    double y2 = (nok*binomial[k]*term2) - c2;
		    double t2 = sum2 + y2;
		    c2 = (t2 - sum2) - y2;
		    sum2 = t2;
		}
		term = term1*sum2;
		// sum1 += term;
		double y1 = term - c1;
		double t1 = sum1 + y1;
		c1 = (t1 - sum1) - y1;
		sum1 = t1;
		// tmp = prev;
		// prev = binomial;
		// binomial = tmp;
		n++;
		/*
		if (n+4 > binomial.length) {
		    tmp = binomial;
		    binomial = new long[tmp.length * 2];
		    System.arraycopy(tmp, 0, binomial, 0, n);
		    tmp = prev;
		    prev = new long[tmp.length * 2];
		    System.arraycopy(tmp, 0, prev, 0, n);
		}
		*/
	    } while (Math.abs(term) > ZETA_LIMIT && n < 65);
	    // System.out.println("term = " + term);
	    result *= sum1;
	    return result;
	}
    }

    /**
     * Raise a number to an integer power.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * This method is provided to supplement the method
     * {@link java.lang.Math#pow} by providing a method whose second argument
     * is an integer.  This is useful in some infinite series expansions of
     * special functions.  For example, for small z, the spherical Bessel
     * function j<sub>n</sub>(z) has a first term proportional to z<sup>n</sup>
     * in its Taylor series expansion (for larger values of |z|, the
     * implementation in {@link org.bzdev.math.Functions} does not use this
     * Taylor series).
     * <P>
     * The time complexity is O(log<sub>2</sub> n). Timing measurements
     * indicate that it is faster than
     * {@link java.lang.Math#pow java.lang.Math.pow} (which handles more
     * cases).
     * @param x a real number
     * @param n the power
     * @return the value of x<sup>n</sup>
     */
    @Deprecated
    public static double pow(double x, int n) {
	if (n == 0) return 1.0;
	boolean invert = false;
	if (n < 0) {
	    invert = true;
	    n = -n;
	}
	double prod = 1.0;
	double partial = x;
	for (;;) {
	    if (n%2 == 1) {
		prod *= partial;
	    }
	    n = n >> 1;
	    if (n == 0) {
		break;
	    } else {
		partial *= partial;
	    }
	}
	if (invert) {
	    prod = 1.0/prod;
	}
	return prod;
    }

    /**
     * Raise a number to a long integer power.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * This method is provided to supplement the method
     * {@link java.lang.Math#pow} by providing a method whose second argument
     * is a long integer.  This is useful in some infinite series
     * expansions of special functions.  For example, for small z, the 
     * spherical Bessel function j<sub>n</sub>(z) has a first
     * term proportional to z<sup>n</sup> in its Taylor series expansion
     * (for larger values of |z|, the implementation in 
     * {@link org.bzdev.math.Functions} does not use this Taylor series).
     * <P>
     * The time complexity is O(log<sub>2</sub> n). Timing measurements
     * indicate that it is faster than
     * {@link java.lang.Math#pow java.lang.Math.pow} (which handles more
     * cases).
     * @param x a real number
     * @param n the power
     * @return the value of x<sup>n</sup>
     */
    @Deprecated
    public static double pow(double x, long n) {
	if (n == 0) return 1.0;
	boolean invert = false;
	if (n < 0) {
	    invert = true;
	    n = -n;
	}
	double prod = 1.0;
	double partial = x;
	for (;;) {
	    if (n%2 == 1) {
		prod *= partial;
	    }
	    n = n >> 1;
	    if (n == 0) {
		break;
	    } else {
		partial *= partial;
	    }
	}
	if (invert) {
	    prod = 1.0/prod;
	}
	return prod;
    }

    /*
     * Compute the nth root of a real number.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * The second argument must be non-negative when the first argument
     * is an even integer.
     * @param n the root index (2 for the square root, 3 for the cube root,
     *         etc.)
     * @param x the number whose root is to be computed.
     * @exception IllegalArgumentException an argument was out of range
     */
    @Deprecated
    public static double root(int n, double x) {
	if (n < 1) {
	    String msg = errorMsg("firstArgNotPositive", n);
	    throw new IllegalArgumentException(msg);
	}
	if (n == 1) return x;
	boolean negative;
	if (x == 0.0) {
	    return 0.0;
	} else if (x < 0.0) {
	    if (n%2 == 0) {
		throw new IllegalArgumentException
		    (errorMsg("notRealPow", x, 1, n));
	    }
	    x = -x;
	    negative = true;
	} else {
	    negative = false;
	}
	boolean invert = (x < 1.0);
	if (invert) x = 1.0/x;

	double a = (x + 1.0)/2.0;
	int m = n-1;
	double last = x;
	double err = 0.0;
	double xerr = Math.ulp(x);
	int cnt = 0;
	// After the first iteration or so, the sequence is
	// monotonically decreasing, so any increase indicates that
	// we've reach a floating-point accuracy limit.
	while (Math.abs(last - a) > err || ((a - last) > 0.0) && cnt++ > 2) {
	    // System.out.println(" a = " + a);
	    last = a;
	    a = (m*a + x/pow(a, m))/n;
	    err = Math.scalb(Math.ulp(a), 5);
	}
	if (invert) a = 1/a;
	return (negative)? -a: a;
    }

    /**
     * Raise a number x to a rational power.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * This method is provided to supplement the method
     * {@link java.lang.Math#pow} by providing a method whose second argument
     * is the numerator of the power and whose third argument is the
     * denominator.
     * @param z a real number
     * @param xn the numerator of fraction representing a rational number
     *        for the power
     * @param xd the denominator of a fraction representing a rational number
     *        for the power
     * @return the value of x<sup>xn/xd</sup>
     */
    @Deprecated
    public static double pow(double z, int xn, int xd) {
	if (xd == 0) {
	    throw new IllegalArgumentException(errorMsg("thirdArgZero"));
	}
	int f = gcd(Math.abs(xn), Math.abs(xd));
	xn /= f;
	xd /= f;
	if (xd < 0) {
	    xd = -xd;
	    xn = -xn;
	}
	// at this point,  gcd(xn, xd) == 1 and xd > 0;
	if (z < 0.0) {
	    if (xd == 1) return pow(z, xn);
	    if ((xd % 2) == 1) {
		// double x = ((double)xn)/xd;
		// return - Math.pow(-z, x);
		return - root(xd, pow(-z, xn));
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("notRealPow", z, xn, xd));
	    }
	} else if (xd == 1) {
	    return pow(z, xn);
	} else {
	    // double x = ((double)xn)/xd;
	    // return Math.pow(z, x);
	    return root(xd, pow(z, xn));
	}
    }

    /**
     * Raise an integer to to a non-negative integer power.
     * @deprecated
     * This method has been moved to the class {@link org.bzdev.lang.MathOps}
     * to eliminate a module dependency.
     * <P>
     * This method is provided to supplement the method
     * {@link java.lang.Math#pow} by providing a method whose second argument
     * is an integer.  This is useful in some infinite series expansions of
     * special functions.  For example, for small z, the spherical Bessel
     * function j<sub>n</sub>(z) has a first term proportional to z<sup>n</sup>
     * in its Taylor series expansion.
     * <P>
     * The time complexity is O(log<sub>2</sub> n). Timing measurements
     * indicate that it is faster than
     * {@link java.lang.Math#pow java.lang.Math.pow} (which handles more
     * cases).
     * @param x an integer
     * @param n the power
     * @return the value of x<sup>n</sup>
     * @exception IllegalArgumentException an argument was out of bounds
     * @exception ArithmeticException the combination of arguments would
     *            overflow the integer representation used
     */
    @Deprecated
    public static long lPow(int x, int n)
	throws IllegalArgumentException, ArithmeticException
    {
	if (n == 0) return 1L;
	boolean invert = false;
	if (n < 0) {
	    throw new IllegalArgumentException(errorMsg("argNonNegative", n));
	}
	boolean negative = (x < 0);
	if (negative) {
	    // compute using positive values so we can
	    // detect if a product is too large.
	    x = -x;
	}
	long prod = 1;
	long partial = x;
	for (;;) {
	    if (n%2 == 1) {
		prod *= partial;
	    }
	    n = n >> 1;
	    if (n == 0) {
		break;
	    } else {
		partial *= partial;
	    }
	    if (partial < 0) {
		throw new ArithmeticException
		    (errorMsg("argsOutOfRange2", x, n));
	    }
	}
	if (negative && (n%2 == 1)) {
	    prod = -prod;
	}
	return prod;
    }


    private static int factorialArrayLength = 0;
    private static int longFactorialArrayLength = 0;
    private static final int MAX_LONG_FACTORIAL_N = 20;
    private static final int MAX_FACTORIAL_N = 170;
    private static double[] factorialArray = new double[MAX_FACTORIAL_N + 1];
    private static long[] longFactorialArray =
	new long[MAX_LONG_FACTORIAL_N + 1];
    private static double[] logFactorialArray
	= new double[MAX_FACTORIAL_N + 1];
    static {
	BigInteger f = BigInteger.valueOf(1L);
	int i = 1;
	factorialArray[0] = 1;
	factorialArray[1] = 1;
	longFactorialArray[0] = 1;
	longFactorialArray[1] = 1;
	longFactorialArrayLength = 1; // highest index for now
	for (;;) {
	    i++;
	    f = f.multiply(BigInteger.valueOf((long)i));
	    double ff = f.doubleValue();
	    long lv = f.longValue();
	    if (ff <= Long.MAX_VALUE && lv > 0) {
		longFactorialArrayLength++;
		longFactorialArray[i] = lv;
	    }
	    if (ff == Double.POSITIVE_INFINITY) {
		factorialArrayLength = i;
		break;
	    } else {
		factorialArray[i] = ff;
		logFactorialArray[i] = Math.log(ff);
	    }
	}
	longFactorialArrayLength++; // increment to make it the array length
    }

    private static int oddFactorialArrayLength = 0;
    private static final int MAX_ODD_FACTORIAL_N = 300;
    private static double[] oddFactorialArray =
	new double[MAX_ODD_FACTORIAL_N + 1];
    static {
	BigInteger f = BigInteger.valueOf(1L);
	oddFactorialArray[0] = 1;
	oddFactorialArray[1] = 1;
	oddFactorialArray[2] = 1;
	int i = 2;
	for (;;) {
	    i++;
	    f = f.multiply(BigInteger.valueOf((long)i));
	    double ff = f.doubleValue();
	    if (ff == Double.POSITIVE_INFINITY) {
		oddFactorialArrayLength = i;
		break;
	    } else {
		oddFactorialArray[i] = ff;
		i++;
		oddFactorialArray[i] = ff;
	    }
	}
    }


    /**
     * Compute a factorial.
     * There are a small number of values that can be represented as
     * a double-precision number, so the implementation uses table
     * lookup for speed, with the values computed using infinite-precision
     * arithmetic.
     * <P>
     * Note: while not really a special function, a factorial method is
     * provided in this class because n! =  &Gamma;(n+1).
     * @param n a non-negative integer whose factorial is to be computed
     * @return the value of n! or Double.POSITIVE_INFINITY if the value
     *         cannot be represented as a double-precision number due to
     *         being too large
     * @exception IllegalArgumentException the argument n is negative
     */
    public static double factorial(int n) {
	if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	if (n < factorialArrayLength) {
	    return factorialArray[n];
	} else {
	    return Double.POSITIVE_INFINITY;
	}
    }


    /**
     * Compute a factorial, returning the results as a long integer.
     * @param n an integer in the range [0, 20]
     * @return the value of n!
     * @exception IllegalArgumentException n is out of range
     */
    public static long longFactorial(int n) {
	if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	if (n < longFactorialArrayLength) {
	    return longFactorialArray[n];
	} else {
	    throw new IllegalArgumentException
		(errorMsg("argTooLarge", n));
	}
    }

    /**
     * Compute a factorial exactly.
     * @param n a non-negative integer
     * @return the value of n! as an arbitrary-precision integer
     */
    public static BigInteger exactFactorial(int n) {
	if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	if (n < 2) return BigInteger.ONE;
	BigInteger prod = BigInteger.ONE;
	BigInteger factor = BigInteger.ONE;
	while ((--n) > 0) {
	    factor = factor.add(BigInteger.ONE);
	    prod = prod.multiply(factor);
	}
	return prod;
    }

    /**
     * Compute the product of all odd positive integers less than or
     * equal to n.
     * There are a small number of values that can be represented as
     * a double-precision number, so the implementation uses table
     * lookup for speed, with the values computed using infinite-precision
     * arithmetic.
     * <P>
     * Note: while not really a special function, a factorial method is
     * provided in this class because n! =  &Gamma;(n+1).
     * @param n a non-negative integer whose odd factorial is to be computed
     * @return the value of n! or Double.POSITIVE_INFINITY if the value
     *         cannot be represented as a double-precision number due to
     *         being too large
     * @exception IllegalArgumentException the argument n is negative
     */
    public static double oddFactorial(int n) {
	if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	if (n < oddFactorialArrayLength) {
	    return oddFactorialArray[n];
	} else {
	    return Double.POSITIVE_INFINITY;
	}
    }

    /**
     * Compute the logarithm of a factorial.
     * A finite value will be returned for arguments whose factorials are
     * too large to be represented as a double-precision number.
     * @param n a non-negative integer whose factorial is to be computed
     * @return the value of log n!
     * @exception IllegalArgumentException the argument n is negative
     */
    public static double logFactorial(int n) {
	if (n < 0) throw new IllegalArgumentException
			   (errorMsg("argNonNegative", n));
	if (n < factorialArrayLength) {
	    return logFactorialArray[n];
	} else {
	    // use n! = Gamma(n+1) + the method giving the logarithm
	    // of the asymptotic value
	    double x = n + 1.0;
	    return GammaFunction.logAsymptoticValue(x);
	}
    }

    static final double LOG_GAMMA_LIMIT = 1.0e-14;

    /**
     * Class providing static methods for computing Gamma functions,
     * their logarithms, and asymptotic values.
     */
    public static class GammaFunction {

	private GammaFunction() {}

	/** Compute the Gamma function.
	 * The function &Gamma;(x) has poles at 0, -1, -2, -3, etc.
	 * At a poll, an infinite double-precision number is returned,
	 * with the choice of positive or negative infinity based on whether
	 * the value at x+&delta; is positive or negative respectively for
	 * &delta;&gt;0, where &delta; is an arbitrarily small number.
	 * @param z a real number
	 * @return the value &Gamma;(z)
	 */
	public static double Gamma(double z) {
	    if (z == 0.0) return Double.POSITIVE_INFINITY;
	    if (Math.rint(z) == z) {
		if (z < 0.0) {
		    long iz = Math.round(z);
		    return (iz%2 == 0)? Double.POSITIVE_INFINITY:
			Double.NEGATIVE_INFINITY;
		} else if (z < (double)(factorialArrayLength + 1)) {
		    return factorial((int)z - 1);
		} else {
		    return Double.POSITIVE_INFINITY;
		}
	    }
	    if (z > 40.0) return asymptoticValue(z);
	    if (z > 0) {
		double prod = 1.0;
		double c = 0.0;
		if (z > 0.5) {
		    if (z > 1.5) {
			while (z > 1.5) {
			    // z = z-1.0;
			    double y = (-1.0) - c;
			    double t = z + y;
			    c = (t - z) - y;
			    z = t;
			    prod *= z;
			}
		    }
		    // double zz = z-1.0;
		    double yy = -1.0 - c;
		    double zz = z + yy;
		    return prod * Math.exp(logGamma1(zz));
		} else {
		    prod /= z;
		    return prod * Math.exp(logGamma1(z));
		}
	    } else {
		return Math.PI/(Math.sin(Math.PI * z) * Gamma(1-z));
	    }
	}

	/**
	 * Compute the logarithm of the Gamma function
	 * @param z a non-negative number
	 * @return the value of log (&Gamma;(x))
	 */
	public static double logGamma(double z) {
	    if (z == 0) return Double.POSITIVE_INFINITY;
	    if (z >=40.0) return logAsymptoticValue(z);
	    double sum = 0.0;
	    double c = 0.0;
	    double zc = 0.0;
	    if (z > 0.5) {
		if (z > 1.5) {
		    while (z > 1.5) {
			// z = z-1.0;
			double zy = (-1.0) - zc;
			double zt = z + zy;
			zc = (zt - z) - zy;
			z = zt;
			// sum += Math.log(z);
			double y = (Math.log(z)) - c;
			double t = sum + y;
			c = (t - sum) - y;
			sum = t;
		    }
		}
		// double zz = z-1.0;
		double yy = (-1.0) - zc;
		double zz = z + yy;
		return sum + logGamma1(zz);
	    } else if (z > 0.0) {
		sum -= Math.log(z);
		return sum + logGamma1(z);
	    } else {
		throw new IllegalArgumentException
			   (errorMsg("argNonNegativeD", z));
	    }
	}


	// log Gamma (z + 1) for z < 1.

	static double logGamma1(double z) {

	    double sum = - Constants.EULERS_CONSTANT * z;
	    double c = 0.0;
	    int k = 2;
	    double zz = -z;
	    double term;
	    double pterm = 0.0;
	    double diff = 0.0;
	    do {
		zz = -zz*z;
		term = zz * zeta((double)k)/k;
		// sum += term;
		double y = term - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		if (k%2 == 0) {
		    if (k >= 2) diff = Math.abs(pterm - term);
		    pterm = term;
		}
		k++;
	    } while (k < 4 || k%2 != 0 || diff > LOG_GAMMA_LIMIT);
	    return sum;
	}

	/**
	 * Compute the asymptotic value of the Gamma function.
	 * The computation uses Nemes' approximation with up to a t<sup>6</sup>
	 * term.  Errors are less than 1 part in 10<sup>12</sup> for t \gt; 20.
	 * <P>
	 * Citation:
	 *  http://www.ebyte.it/library/downloads/2007_MTH_Nemes_GammaFunction.pdf
	 * or http://dx.doi.org/10.3247/sl2math08.005
	 * @param x the argument, which must be positive
	 * @return an asymptotic approximation for &Gamma;(x)
	 */
	public static double asymptoticValue(double x) {
	    double x2 = x * x;
	    double x4 = x2 * x2;
	    double x6 = x4 * x2;
	    double sum = 1 + (1.0/12.0)/x2 + (1.0/1440.0)/x4
		+ (239.0/362880.0)/x6;
	    double sumToX = Math.exp(x * Math.log(sum));
	    return  sumToX * Math.sqrt((Math.PI*2.0)/x)
		* Math.exp(x * (Math.log(x) - 1.0));
	}

	/**
	 * Compute the asymptotic value of the logarithm of the Gamma function.
	 * The computation uses Nemes' approximation with up to a t<sup>6</sup>
	 * term.  Errors are less than 1 part in 10<sup>12</sup> for t \gt; 20.
	 * <P>
	 * Citation:
	 *  http://www.ebyte.it/library/downloads/2007_MTH_Nemes_GammaFunction.pdf
	 * or http://dx.doi.org/10.3247/sl2math08.005
	 * @param x the argument, which must be positive
	 * @return an asymptotic approximation for log (&Gamma;(x))
	 */
	public static double logAsymptoticValue(double x) {
	    double x2 = x * x;
	    double x4 = x2 * x2;
	    double x6 = x4 * x2;
	    double sum = 1 + (1.0/12.0)/x2 + (1.0/1440.0)/x4
		+ (239.0/362880.0)/x6;
	    double sumToX = (x * Math.log(sum));
	    return  sumToX + Math.log(Math.sqrt((Math.PI*2.0)/x))
		+ (x * (Math.log(x) - 1.0));
	}
    }

    /**
     * Compute the logarithm of the Gamma function
     * @param x a non-negative number
     * @return the value of log (&Gamma;(x))
     */
    public static double logGamma(double x) {
	return GammaFunction.logGamma(x);
    }

    /**
     * Just contains a static array.  This is provided in a separate
     * class so that it does not get initialized when Functions is
     * initialized.
     */
    static class DigammaSupport {

	private static double[] digammaB2n = new double[7];
	static {
	    for (int i = 1; i <= 7; i++) {
		int ii = 2*i;
		digammaB2n[i-1]= Constants.BernoulliNumber2(ii)/ii;
	    }
	}
    }

    /**
     * Compute the digamma function.
     * The implementation used the relation &psi;(x+1) = (1/x) + &psi;(x)
     * coupled with the first terms (up to the 1/x<sup>14</sup> term) of
     * the asymptotic series.
     * <P>
     * Citation: http://en.wikipedia.org/wiki/Digamma_function#Computation_and_approximation
     * @param x the argument
     * @return the value of the digamma function &psi;(x)
     */
    public static double digamma(double x) {

	if (x == Math.rint(x)) {
	    long lx = Math.round(x);
	    if (x <= 0.0) {
		return Double.NEGATIVE_INFINITY;
	    } else if (lx <= digammaTableSize) {
		int ix = (int) lx;
		return digamma(ix);
	    }
	}
	if (x == Math.rint(x) && x < 0) return Double.NEGATIVE_INFINITY;
	if (x < 0.0) {
	    return digamma(1.0-x) - Math.PI / Math.tan(x*Math.PI);
	}

	double sum = 0.0;
	double c  = 0.0;
	double y;
	double t;
	while  (x < 6.0) {
	    // sum += -1.0/x;
	    y = (-1.0/x) - c;
	    t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    x = 1.0 + x;
	}
	// sum += Math.log(x);
	y = (Math.log(x)) - c;
	t = sum + y;
	c = (t - sum) - y;
	sum = t;
	// sum -= 1.0/(2.0*x);
	y = (-(1.0/(2.0*x))) - c;
	t = sum + y;
	c = (t - sum) - y;
	sum = t;
	double x2 = x*x;
	double xx = 1.0;
	for (int i = 0; i < 7; i++) {
	    xx *= x2;
	    //sum -= DigammaSupport.digammaB2n[i]/xx;
	    y = (-(DigammaSupport.digammaB2n[i]/xx)) - c;
	    t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return sum;
    }

    // Use table lookup for relatively small integral values due to the
    // use of digamma functions in some infinite series.  This will make
    // the calculation for the initial terms (and maybe all that are used)
    // as accurate as possible.
    private static int digammaTableSize = 8192;
    private static double[] digammaTable = new double[digammaTableSize];
    static {
	digammaTable[0] = -Constants.EULERS_CONSTANT;
	for (int i = 1; i < digammaTableSize; i++) {
	    digammaTable[i] = digammaTable[i-1] + 1.0/i;
	}
    }

    public static double digamma(int i) {
	if (i <= 0) return Double.NEGATIVE_INFINITY;
	if (i <= digammaTableSize) {
	    return digammaTable[i-1];
	}
	return digamma((double) i);
    }

    /**
     * Compute the Gamma function.
     * The function &Gamma;(x) has poles at 0, -1, -2, -3, etc.
     * At a poll, an infinite double-precision number is returned,
     * with the choice of positive or negative infinity based on whether
     * the value at x+&delta; is positive or negative respectively for
     * &delta;&gt;0, where &delta; is an arbitrarily small number.
     * @param x a real number
     * @return the value &Gamma;(x)
     */
    public static double Gamma(double x) {
	return GammaFunction.Gamma(x);
    }

    private static int POCHHAMMER_LIMIT = 32;

    /**
     * Compute Pochhammer's symbol (x)<sub>n</sub>.
     * This notation is defined so that
     * <UL>
     *   <LI> (x)<sub>0</sub> = 1
     *   <LI> (x)<sub>n</sub> = x(x+1)(x+2) ... (x + n-1)  for n &gt; 0
     * </UL>
     * @param x the argument
     * @param n the index
     * @return the value (x)<sub>n</sub>
     */
    public static double poch(double x, int n) {
	if (n < 0) {
	    throw new IllegalArgumentException(errorMsg("argNonNegative2", n));
	}
	if (n == 0) return 1.0;
	if (x == 0.0) return 0.0;
	if (n < 2) {
	    return x;
	} else if (x < 0.0) {
	    double test = x + (n-1);
	    if (test < 0.0) {
		int sign = (n%2 == 0)? 1: -1;
		return sign*poch((-x) - (n-1), n);
	    } else if (test  > 0.0) {
		int m = (int)Math.round(Math.ceil(-x));
		while (x + (m-1) < 0) m++;
		while (x + (m-1) > 0) m--;
		if (-x == m-1) return 0.0;
		return  poch(x, m) * (x + m) * poch((x + m + 1), n-m-1);
	    } else {
		return 0.0;
	    }
	} else if (n<POCHHAMMER_LIMIT) {
	    double prod = x;
	    for (int i = 1; i < n; i++) {
		prod *= (x + i);
	    }
	    return prod;
	} else {
	    if (x > 10.0 || n > 10)
		return Math.exp
		    (GammaFunction.logGamma(x+n) - GammaFunction.logGamma(x));
	    else
		return GammaFunction.Gamma(x + n) / GammaFunction.Gamma(x);
	}
    }

    /**
     * Compute Pochhammer's symbol (x)<sub>n</sub> when x is an integer.
     * This notation is defined so that
     * <UL>
     *   <LI> (x)<sub>0</sub> = 1
     *   <LI> (x)<sub>n</sub> = x(x+1)(x+2) ... (x + n-1)  for n &gt; 0
     * </UL>
     * @param x the argument
     * @param n the index
     * @return the value (x)<sub>n</sub>
     */
    public static double poch(int x, int n) {
	if (n < 0) {
	    throw new IllegalArgumentException(errorMsg("argNonNegative2", n));
	}
	if (n == 0) return 1.0;
	if (x == 0) return 0.0;
	if (n < 2) {
	    return x;
	} else if (x < 0) {
	    int test = x + (n-1);
	    if (test < 0) {
		int sign = (n%2 == 0)? 1: -1;
		return sign*poch((-x) - (n-1), n);
	    } else {
		return 0.0;
	    }
	} else if (n<POCHHAMMER_LIMIT) {
	    double prod = x;
	    for (int i = 1; i < n; i++) {
		prod *= (x + i);
	    }
	    return prod;
	} else {
	    double y = x;
	    if (y > 10.0 || n > 10)
		return Math.exp
		    (GammaFunction.logGamma(y+n) - GammaFunction.logGamma(y));
	    else
		return GammaFunction.Gamma(y + n) / GammaFunction.Gamma(y);
	}
    }


    private static double hgPow(double z, double x) {
	if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", x));
	}
	if (z < 0.0) {
	    boolean ok = true;
	    double sign = 1;
	    if (x == Math.rint(x)) {
		int ix = (int) Math.round(x);
		return MathOps.pow(z, ix);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("nonIntegerExponent", x));
	    }
	} else if (x == Math.rint(x)) {
	    int ix = (int)Math.round(x);
	    return MathOps.pow(z, ix);
	} else {
	    return Math.pow(z, x);
	}
    }

    private static double hgPow(double z, int xn, int xd) {
	// assume gcd(xn, xd) == 1
	if (z < 0.0) {
	    if (xd == 1) return pow(z, xn);
	    if ((xd % 2) == 1) {
		double x = ((double)xn)/xd;
		return - Math.pow(-z, x);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("notRealPow", z, xn, xd));
	    }
	} else if (xd == 1) {
	    return MathOps.pow(z, xn);
	} else {
	    double x = ((double)xn)/xd;
	    return Math.pow(z, x);
	}
    }

    // Abramowitz & Stegun, 15.3.10
    private static double hgFCase10(double a, double b, double z) {
	double omz = 1.0 - z;
	double limit = 1.e-15 * Math.abs(z);
	double factor1 = 1.0;
	double logterm = Math.log(1-z);
	double factor2 = 2*digamma(1.0)-digamma(a)-digamma(b) - logterm;
	double sum = factor2;
	int n = 0;
	double term;
	do {
	    factor1 *= (a+n)*(b+n);
	    n++;
	    factor1 *= (omz)/(n*n);
	    factor2 = 2*digamma(1.0+n)-digamma(a+n)-digamma(b+n) - logterm;
	    term = factor1*factor2;
	    sum += term;
	} while (Math.abs(term) > limit);
	return (GammaFunction.Gamma(a+b)
		/(GammaFunction.Gamma(a)*GammaFunction.Gamma(b)))
	    *sum;
    }

    // Abramowitz & Stegun, 15.3.11
    private static double hgFCase11(double a, double b, int m, double z) {
	double omz = 1.0 - z;
	double zmo = z - 1.0;
	double gabm = GammaFunction.Gamma(a+b+m);

	double limit = 1.e-15 * Math.abs(z);
	double factor1 = 1.0;
	double logterm = Math.log(omz);
	double sum = factor1;
	int n = 0;
	double term;
	while (n < m-1) {
	    factor1 *= (a+n)*(b+n)/(1.0-m+n);
	    n++;
	    factor1 *= (omz)/(n);
	    term = factor1;
	    sum += term;
	}
	double total = (GammaFunction.Gamma((double)m)
			/(GammaFunction.Gamma(a+m)*GammaFunction.Gamma(b+m)))
	    * sum;

	factor1 = 1.0/factorial(m);
	double factor2 = logterm - digamma(1.0) - digamma(m+1.0)
	    + digamma(a+m) + digamma(b+m);
	n = 0;
	sum = factor1*factor2;
	do {
	    factor1 *= (a+n+m) * (b+n+m);
	    n++;
	    factor1 *= (omz)/(n*(n+m));
	    factor2 = logterm - digamma(n+1.0) - digamma(n+m+1.0)
		+ digamma(a+m+n) + digamma(b+m+n);
	    term = factor1*factor2;
	    sum += term;
	} while (Math.abs(term) > limit);
	total -=  MathOps.pow(zmo, m) * sum
	    / (GammaFunction.Gamma(a) * GammaFunction.Gamma(b));
	return gabm*total;
    }

    // Abramowitz & Stegun, 15.3.12
    private static double hgFCase12(double a, double b, int m, double z) {
	double omz = 1.0 - z;
	double limit = 1.e-15 * Math.abs(z);
	double factor1 = 1.0;
	double logterm = Math.log(1-z);
	double sum = factor1;
	int n = 0;
	double term;
	while (n < m-1) {
	    factor1 *= (a-m+n)*(b-m+n)/(1-m+n);
	    n++;
	    factor1 *= (omz)/(n);
	    term = factor1;
	    sum += term;
	};
	double total = (GammaFunction.Gamma((double)m)
			* GammaFunction.Gamma(a+b-m) * MathOps.pow(omz,-m)
			/ (GammaFunction.Gamma(a) * GammaFunction.Gamma(b)))
	    * sum;
	factor1 = 1.0/factorial(m);
	double factor2 = logterm - digamma(1.0) - digamma(m+1.0)
	    + digamma(a) + digamma(b);
	sum = factor1*factor2;
	do {
	    factor1 *= (a+n)*(b+n);
	    n++;
	    factor1 *= (omz)/(n*(n+m));
	    factor2 = logterm - digamma(n+1.0) - digamma(n+m+1.0)
		+ digamma(a+n) + digamma(b+n);
	    term = factor1*factor2;
	    sum += term;
	} while (Math.abs(term) > limit);
	total -= (GammaFunction.Gamma(a+b-m)
		  / (GammaFunction.Gamma(a-m)*GammaFunction.Gamma(b-m)))
	    * ((m%2==0)? 1.0: -1.0) * sum;
	return total;
    }

    /**
     * Compute the hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z).
     * The argument c must not be an integer less than or equal to 0.
     * If z = 1, then c-a-b must not be an integer less than or equal to 0.
     * If
     * @param a the first parameter
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the value <sub>2</sub>F<sub>1</sub>(a, b; c; z)
     */
    public static double hgF(double a, double b, double c, double z) {

	if (a > Long.MAX_VALUE || a < Long.MIN_VALUE) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", a));
	}
	if (b > Long.MAX_VALUE || b < Long.MIN_VALUE) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", b));
	}

	boolean isIntA = (a == Math.rint(a));
	boolean isIntB = (b == Math.rint(b));

	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg3NotNegIntOrZero", c));
	} else if ((isIntA && (a <= 0.0)) || (isIntB && (b <= 0.0))) {
	    long ia = Math.round(a);
	    long ib = Math.round(b);
	    long m;
	    if (isIntA && isIntB) {
		if (ib < 0 && ia < 0) {
		    if (ib < ia) m = -ia;
		    else m = -ib;
		} else if (ib < ia) {
		    m = -ib;
		} else {
		    m = -ia;
		}
	    } else if (isIntA && a <= 0.0) {
		m = -ia;
	    } else {
		m = -ib;
	    }
	    double sum = 1.0;
	    double term = 1.0;
	    double limit = Math.abs(z)*(1.e-15);
	    long n = 0;
	    boolean notDone = true;
	    while ((n < m) && notDone) {
		term *= ((a+n)*(b+n)*z)/(c+n);
		n++;
		term /= n;
		sum += term;
		notDone = (Math.abs(term) > limit);
	    }
	    return sum;
	} else if (b == c) {
	    return 1.0 / hgPow(1.0-z, a);
	} else if (a == c) {
	    return 1.0 / hgPow(1.0-z, b);
	} else if (z == 0.0) {
	    return 1.0;
	} else if (z == 1.0) {
	    double ca = c-a;
	    double cb = c-b;
	    double cab = c - a - b;
	    if (cab > 0) {
		return GammaFunction.Gamma(c)*GammaFunction.Gamma(cab)
		    / (GammaFunction.Gamma(ca) * GammaFunction.Gamma(cb));
	    } else if (cab != 0.0) {
		// 15.2.12 (Abramowitz & Stegun)
		    return ca*cb*hgF(a,b,c+1, 1.0) / (-c*cab);
	    } else {
		// 15.3.10 diverges as z->1 (cab = 0 => c = a + b)
		return Double.NaN;
	    }
	} else if (z < -0.5) {
	    if (Math.abs(a) < Math.abs(b)) {
		return Math.pow(1-z,-a)*hgF(a,c-b,c,z/(z-1));
	    } else {
		return Math.pow(1-z,-b)*hgF(b,c-a,c,z/(z-1));
	    }
	} else if (false) {
	    // Works for only some values: in general, (-z)^(-a) and (-z)^(-b)
	    // are not real numbers. A real value will exist when a
	    // and and b are rational and it meets a specific constraint: if
            // the number is represented as X/Y and gcd(X,Y) = 1, then Y
	    // is not divisible by 2 (i.e., gcd(Y,2) = 1).   In addition,
	    // neither a-b nor b-a may be a positive integer.

	    double ba = b - a;
	    double ab = a - b;
	    double ca = c - a;
	    double cb = c - b;
	    if ((a != b) && (Math.rint(a) == a) && (Math.rint(b) == b)) {
		throw new IllegalArgumentException
		    (errorMsg("argNotInts2", a, b));
	    }

	    double pow1 = 1.0 / hgPow(-z, a);
	    double pow2 = 1.0 / hgPow(-z, b);

	    double factor1 = ((ca == Math.rint(ca)) && (ca <= 0.0))? 0.0:
		(GammaFunction.Gamma(ba))
		/ (GammaFunction.Gamma(b)*GammaFunction.Gamma(ca));
	    double factor2 = ((cb == Math.rint(cb)) && (cb <= 0.0))? 0.0:
		(GammaFunction.Gamma(ab))
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(cb));

	    return GammaFunction.Gamma(c)
		* ((factor1 * pow1 * hgF(a,1-ca, 1-ba, 1/z))
		   + (factor2 * pow2 * hgF(b,1-cb,1-ab, 1/z)));
	}

	// for 0 <= z < 1
	if (z > 0.5) {
	    // the recursive calls to hgF are with 1-z, so that argument
	    // will be less then 0.5 and larger than 0, allowing faster
	    // convergence. The expression used is dependent on
	    // | arg(1-z)| < &pi; so z must not be larger than 1.0
	    double ca = c - a;
	    double cb = c - b;
	    double cab = c - a - b;
	    double abc = a + b - c;
	    double omz = 1.0 - z;
	    if (Math.rint(cab) == cab) {
		if (z >= 1.0) {
		    throw new IllegalArgumentException
			(errorMsg("zTooLargeForIntCAB",a,b,c,z));
		}
		long ml = Math.round(cab);
		if (ml < Integer.MIN_VALUE || ml > Integer.MAX_VALUE) {
		    throw new IllegalArgumentException("cabTooLarge");
		}
		int m = (int) ml;
		if (m == 0) {
		    return hgFCase10(a, b, z);
		} else if (m > 0) {
		    return hgFCase11(a, b, m, z);
		} else {
		    return hgFCase12(a, b, -m, z);
		}
	    }

	    double gammac = GammaFunction.Gamma(c);
	    double factor1 = GammaFunction.Gamma(cab)
		/ (GammaFunction.Gamma(ca)*GammaFunction.Gamma(cb));
	    double factor2 = GammaFunction.Gamma(abc)
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(b));

	    return gammac * ((factor1 * hgF(a,b,abc+1,omz))
			     + (Math.pow(omz,cab)
				* factor2 * hgF(ca,cb,cab+1,omz)));
	}
	double sum = 1.0;
	double term = 1.0;
	long n = 0;
	double limit = Math.abs(z)*(1.e-15);
	do {
	    term *= (z *(a+n)*(b+n)) / (c+n);
	    n++;
	    term /= n;
	    sum += term;
	} while (Math.abs(term) > limit);
	return sum;
    }


    /**
     * Compute the hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z) when a and b are
     * rational numbers.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param bn the  numerator for the second parameter (b)
     * @param bd the denominator for th second parameter (b)
     * @param c the third parameter
     * @param z the argument
     * @return the value <sub>2</sub>F<sub>1</sub>(an/ad, bn/bd; c; z)
     */
    public static double hgF(int an, int ad, int bn, int bd,
			     double c, double z)
    {
	int f = MathOps.gcd(Math.abs(an), Math.abs(ad));
	an /= f;
	ad /= f;
	if (ad < 0) {
	    ad = -ad;
	    an = -an;
	}
	f = MathOps.gcd(Math.abs(bn), Math.abs(bd));
	bn /= f;
	bd /= f;
	if (bd < 0) {
	    bd = -bd;
	    bn = -bn;
	}

	double a = ((double)an)/ad;
	double b = ((double)bn)/bd;
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg5NotNegIntOrZero", c));
	} else if ((ad == 1 && (an <= 0)) || (bd == 1 && (bn <= 0))) {
	    long m;
	    if (ad == 1 && bd == 1) {
		if (bn < 0 && an < 0) {
		    if (bn < an) m = -an;
		    else m = -an;
		} else if (bn < an) {
		    m = -bn;
		} else {
		    m = -an;
		}
	    } else if (ad == 1 && an <= 0) {
		m = -an;
	    } else {
		m = -bn;
	    }
	    double sum = 1.0;
	    double term = 1.0;
	    double limit = Math.abs(z)*(1.e-15);
	    long n = 0;
	    boolean notDone = true;
	    while ((n < m) && notDone) {
		term *= ((a+n)*(b+n)*z)/(c+n);
		n++;
		term /= n;
		sum += term;
		notDone = (Math.abs(term) > limit);
	    }
	    return sum;
	} else if (b == c) {
	    return 1.0 / hgPow(1.0-z, an, ad);
	} else if (a == c) {
	    return 1.0 / hgPow(1.0-z, bn, bd);
	} else if (z == 0.0) {
	    return 1.0;
	} else if (z == 1.0) {
	    double ca = c-a;
	    double cb = c-b;
	    double cab = c - a - b;
	    if (cab > 0) {
		return GammaFunction.Gamma(c)*GammaFunction.Gamma(cab)
		    / (GammaFunction.Gamma(ca) * GammaFunction.Gamma(cb));
	    } else if (cab != 0.0) {
		// 15.2.12 (Abramowitz & Stegun)
		return ca*cb*hgF(an,ad, bn,bd, c+1, 1.0) / (-c*cab);
	    } else {
		// 15.3.10 diverges as z->1 (cab = 0 => c = a + b)
		return Double.NaN;
	    }
	} else if (z < -0.5) {
	    if (Math.abs(a) < Math.abs(b)) {
		return Math.pow(1-z,-a)*hgF(an,ad, c-b,c,z/(z-1));
	    } else {
		return Math.pow(1-z,-b)*hgF(bn,bd, c-a,c,z/(z-1));
	    }
	} else if (false) {
	    // Works for only some values: in general, (-z)^(-a) and (-z)^(-b)
	    // are not real numbers. A real value will exist when a
	    // and and b are rational and it meets a specific constraint: if
            // the number is represented as X/Y and gcd(X,Y) = 1, then Y
	    // is not divisible by 2 (i.e., gcd(Y,2) = 1).  In addition,
	    // neither a-b nor b-a may be a positive integer.

	    double ba = b - a;
	    double ab = a - b;
	    double ca = c - a;
	    double cb = c - b;
	    if ((a != b) && (Math.rint(a) == a) && (Math.rint(b) == b)) {
		throw new IllegalArgumentException
		    (errorMsg("argRatioNotInts2", an, ad, bn, bd));
	    }

	    double pow1 = 1.0 / hgPow(-z, an, ad);
	    double pow2 = 1.0 / hgPow(-z, bn, bd);

	    double factor1 = ((ca == Math.rint(ca)) && (ca <= 0.0))? 0.0:
		(GammaFunction.Gamma(ba))
		/ (GammaFunction.Gamma(b)*GammaFunction.Gamma(ca));
	    double factor2 = ((cb == Math.rint(cb)) && (cb <= 0.0))? 0.0:
		(GammaFunction.Gamma(ab))
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(cb));

	    return GammaFunction.Gamma(c)
		* ((factor1 * pow1 * hgF(a, 1-ca, 1-ba, 1/z))
		   + (factor2 * pow2 * hgF(b, 1-cb,1-ab, 1/z)));
	}

	// for 0 <= z < 1
	if (z > 0.5) {
	    // the recursive calls to hgF are with 1-z, so that argument
	    // will be less then 0.5 and larger than 0, allowing faster
	    // convergence. The expression used is dependent on
	    // | arg(1-z)| < &pi; so z must not be larger than 1.0
	    double ca = c-a;
	    double cb = c-b;
	    double cab = c - a - b;
	    double abc = a + b - c;
	    double omz = 1.0 - z;

	    if (Math.rint(cab) == cab) {
		if (z >= 1.0) {
		    throw new IllegalArgumentException
			(errorMsg("zTooLargeForIntCAB",a,b,c,z));
		}
		long ml = Math.round(cab);
		if (ml < Integer.MIN_VALUE || ml > Integer.MAX_VALUE) {
		    throw new IllegalArgumentException("cabTooLarge");
		}
		int m = (int) ml;
		if (m == 0) {
		    return hgFCase10(a, b, z);
		} else if (m > 0) {
		    return hgFCase11(a, b, m, z);
		} else {
		    return hgFCase12(a, b, -m, z);
		}
	    }

	    double gammac = GammaFunction.Gamma(c);
	    double factor1 = GammaFunction.Gamma(cab)
		/ (GammaFunction.Gamma(ca)*GammaFunction.Gamma(cb));
	    double factor2 = GammaFunction.Gamma(abc)
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(b));

	    return gammac * ((factor1 * hgF(an,ad, bn,bd, abc+1,omz))
			     + (Math.pow(omz,cab)
				* factor2 * hgF(ca,cb,cab+1,omz)));
	}
	double sum = 1.0;
	double term = 1.0;
	int n = 0;
	double limit = Math.abs(z)*(1.e-15);
	do {
	    term *= (z *(a+n)*(b+n)) / (c+n);
	    n++;
	    term /= n;
	    sum += term;
	} while (Math.abs(term) > limit);
	return sum;
    }

    /**
     * Compute the hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z) when a is a rational
     * number.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the value <sub>2</sub>F<sub>1</sub>(an/ad, b; c; z)
     */
    public static double hgF(int an, int ad, double b,
			     double c, double z)
    {
	if (b > Long.MAX_VALUE || b < Long.MIN_VALUE) {
	    throw new IllegalArgumentException
		(errorMsg("argOutOfRangeD", b));
	}

	boolean isIntB = (b == Math.rint(b));
	int f = MathOps.gcd(Math.abs(an), Math.abs(ad));
	an /= f;
	ad /= f;
	if (ad < 0) {
	    ad = -ad;
	    an = -an;
	}

	double a = ((double)an)/ad;
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg4NotNegIntOrZero", c));
	} else if ((ad == 1 && (an <= 0)) || (isIntB && (b <= 0.0))) {
	    long ib = Math.round(b);
	    long m;
	    if (ad == 1 && isIntB) {
		if (ib < 0 && an < 0) {
		    if (ib < an) m = -an;
		    else m = -ib;
		} else if (ib < an) {
		    m = -ib;
		} else {
		    m = -an;
		}
	    } else if (ad == 1 && an <= 0) {
		m = -an;
	    } else {
		m = -ib;
	    }
	    double sum = 1.0;
	    double term = 1.0;
	    double limit = Math.abs(z)*(1.e-15);
	    long n = 0;
	    boolean notDone = true;
	    while ((n < m) && notDone) {
		term *= ((a+n)*(b+n)*z)/(c+n);
		n++;
		term /= n;
		sum += term;
		notDone = (Math.abs(term) > limit);
	    }
	    return sum;
	} else if (b == c) {
	    return 1.0 / hgPow(1.0-z, an, ad);
	} else if (a == c) {
	    return 1.0 / hgPow(1.0-z, b);
	} else if (z == 0.0) {
	    return 1.0;
	} else if (z == 1.0) {
	    double ca = c-a;
	    double cb = c-b;
	    double cab = c - a - b;
	    if (cab > 0) {
		return GammaFunction.Gamma(c)*GammaFunction.Gamma(cab)
		    / (GammaFunction.Gamma(ca) * GammaFunction.Gamma(cb));
	    } else if (cab != 0.0) {
		// 15.2.12 (Abramowitz & Stegun)
		return ca*cb*hgF(an, ad, b, c+1, 1.0) / (-c*cab);
	    } else {
		// 15.3.10 diverges as z->1 (cab = 0 => c = a + b)
		return Double.NaN;
	    }

	} else if (z < -0.5) {
	    if (Math.abs(a) < Math.abs(b)) {
		return Math.pow(1-z,-a)*hgF(an,ad, c-b,c,z/(z-1));
	    } else {
		return Math.pow(1-z,-b)*hgF(b,c-a,c,z/(z-1));
	    }
	} else if (false) {
	    // Works for only some values: in general, (-z)^(-a) and (-z)^(-b)
	    // are not real numbers. A real value will exist when a
	    // and and b are rational and it meets a specific constraint: if
            // the number is represented as X/Y and gcd(X,Y) = 1, then Y
	    // is not divisible by 2 (i.e., gcd(Y,2) = 1).  In addition,
	    // neither a-b nor b-a may be a positive integer.

	    double ba = b-a;
	    double ab = a - b;
	    double ca = c - a;
	    double cb = c - b;
	    if ((a != b) && (Math.rint(a) == a) && (Math.rint(b) == b)) {
		throw new IllegalArgumentException
		    (errorMsg("argRatioNumbNotInts2", an,ad, b));
	    }

	    double pow1 = 1.0 / hgPow(-z, an, ad);
	    double pow2 = 1.0 / hgPow(-z, b);

	    double factor1 = ((ca == Math.rint(ca)) && (ca <= 0.0))? 0.0:
		(GammaFunction.Gamma(ba))
		/ (GammaFunction.Gamma(b)*GammaFunction.Gamma(ca));
	    double factor2 = ((cb == Math.rint(cb)) && (cb <= 0.0))? 0.0:
		(GammaFunction.Gamma(ab))
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(cb));

	    return GammaFunction.Gamma(c)
		* ((factor1 * pow1 * hgF(a,1-ca, 1-ba, 1/z))
		   + (factor2 * pow2 * hgF(b,1-cb,1-ab, 1/z)));
	}

	// for 0 <= z < 1
	if (z > 0.5) {
	    // the recursive calls to hgF are with 1-z, so that argument
	    // will be less then 0.5 and larger than 0, allowing faster
	    // convergence. The expression used is dependent on
	    // | arg(1-z)| < &pi; so z must not be larger than 1.0
	    double ca = c-a;
	    double cb = c-b;
	    double cab = c - a - b;
	    double abc = a + b - c;
	    double omz = 1.0 - z;

	    if (Math.rint(cab) == cab) {
		if (z >= 1.0) {
		    throw new IllegalArgumentException
			(errorMsg("zTooLargeForIntCAB",a,b,c,z));
		}
		long ml = Math.round(cab);
		if (ml < Integer.MIN_VALUE || ml > Integer.MAX_VALUE) {
		    throw new IllegalArgumentException("cabTooLarge");
		}
		int m = (int) ml;
		if (m == 0) {
		    return hgFCase10(a, b, z);
		} else if (m > 0) {
		    return hgFCase11(a, b, m, z);
		} else {
		    return hgFCase12(a, b, -m, z);
		}
	    }

	    double gammac = GammaFunction.Gamma(c);
	    double factor1 = GammaFunction.Gamma(cab)
		/ (GammaFunction.Gamma(ca)*GammaFunction.Gamma(cb));
	    double factor2 = GammaFunction.Gamma(abc)
		/ (GammaFunction.Gamma(a)*GammaFunction.Gamma(b));

	    return gammac * ((factor1 * hgF(an,ad, b,abc+1,omz))
			     + (Math.pow(omz,cab)
				* factor2 * hgF(ca,cb,cab+1,omz)));
	}
	double sum = 1.0;
	double term = 1.0;
	int n = 0;
	double limit = Math.abs(z)*(1.e-15);
	do {
	    term *= (z *(a+n)*(b+n)) / (c+n);
	    n++;
	    term /= n;
	    sum += term;
	} while (Math.abs(term) > limit);
	return sum;
    }

    /**
     * Compute the derivative of the  hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z) with respect to z.
     * @param a the first parameter
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the derivative of <sub>2</sub>F<sub>1</sub>(a, b; c; z) with
     *         respect to z
     */
    public static double dhgFdx(double a, double b, double c, double z)
	throws IllegalArgumentException
    {
	return (a*b/c) * hgF(a+1, b+1, c+1, z);
    }

    /**
     * Compute the second derivative of the  hypergeometric function
     * F(a, b; c; z) = <sub>2</sub>F<sub>1</sup>(a, b; c; z) with respect to z.
     * @param a the first parameter
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the second derivative of <sub>2</sub>F<sub>1</sub>(a, b; c; z)
     *         with respect to z
     */
    public static double d2hgFdx2(double a, double b, double c, double z)
	throws IllegalArgumentException
    {
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg3NotNegIntOrZero", c));
	}
	return (a*b/c) * ((a+1)*(b+1)/(c+1)) * hgF(a+2, b+2, c+2, z);
    }

    /**
     * Compute the derivative of the hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z) with respect to z when a is a
     * rational number.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the derivative of <sub>2</sub>F<sub>1</sub>(an/ad, b; c; z)
     *         with respect to z
     */
    public static double dhgFdx(int an, int ad, double b, double c, double z)
	throws IllegalArgumentException
    {
	if (ad == 0) {
	    throw new IllegalArgumentException(errorMsg("secondArgZero"));
	}
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg4NotNegIntOrZero", c));
	}
	double a = ((double)an)/((double) ad);
	return (a*b/c) * hgF(an + ad, ad, b+1, c+1, z);
    }

    /**
     * Compute the second derivative of the hypergeometric function
     * F(a, b; c; z) = <sub>2</sub>F<sub>1</sub>(a, b; c; z) with respect
     * to z when a is a rational number.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param b the second parameter
     * @param c the third parameter
     * @param z the argument
     * @return the second derivative of
     *         <sub>2</sub>F<sub>1</sub>(an/ad, b; c; z)
     *         with respect to z
     */
    public static double d2hgFdx2(int an, int ad, double b, double c,
				  double z)
    {
	if (ad == 0) {
	    throw new IllegalArgumentException(errorMsg("secondArgZero"));
	}
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg4NotNegIntOrZero", c));
	}
	double a = ((double)an)/((double) ad);
	return (a*b/c) * ((a+1)*(b+1)/(c+1)) * hgF(an+2*ad, ad, b+2, c+2, z);
    }

    /**
     * Compute the derivative of the hypergeometric function F(a, b; c; z) =
     * <sub>2</sub>F<sub>1</sub>(a, b; c; z) with respect to z when a and b are
     * rational numbers.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param bn the  numerator for the second parameter (b)
     * @param bd the denominator for th second parameter (b)
     * @param c the third parameter
     * @param z the argument
     * @return the derivative of <sub>2</sub>F<sub>1</sub>(an/ad, bn/bd; c; z)
     *         with respect to z
     */
    public static double dhgFdx(int an, int ad, int bn, int bd,
				double c, double z)
	throws IllegalArgumentException
    {
	if (ad == 0) {
	    throw new IllegalArgumentException(errorMsg("secondArgZero"));
	}
	if (bd == 0) {
	    throw new IllegalArgumentException(errorMsg("fourthArgZero"));
	}
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg5NotNegIntOrZero", c));
	}
	double a = ((double)an)/((double) ad);
	double b = ((double)bn)/((double) bd);
	return (a*b/c) * hgF(an + ad, ad, bn+bd, bd, c+1, z);
    }

    /**
     * Compute the second derivative of the hypergeometric function
     * F(a, b; c; z) = <sub>2</sub>F<sub>1</sub>(a, b; c; z) with respect
     * to z when a and b are rational numbers.
     * @param an the numerator for the first parameter (a)
     * @param ad the denominator for the first parameter (a)
     * @param bn the  numerator for the second parameter (b)
     * @param bd the denominator for th second parameter (b)
     * @param c the third parameter
     * @param z the argument
     * @return the second derivative of
     *         <sub>2</sub>F<sub>1</sub>(an/ad, bn/bd; c; z)
     *         with respect to z
     */
    public static double d2hgFdx2(int an, int ad, int bn, int bd,
				  double c, double z)
    {
	if (ad == 0) {
	    throw new IllegalArgumentException(errorMsg("secondArgZero"));
	}
	if (bd == 0) {
	    throw new IllegalArgumentException(errorMsg("fourthArgZero"));
	}
	if (c == Math.rint(c) && c <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("arg5NotNegIntOrZero", c));
	}
	double a = ((double)an)/((double) ad);
	double b = ((double)bn)/((double) bd);
	return (a*b/c) * ((a+1)*(b+1)/(c+1))
	    * hgF(an+2*ad, ad, bn+2*bd, bd, c+2, z);
    }

    /**
     * Compute the confluent hypergeometric function M(a, b, z).
     * M(a,b,z) = &sum;<sub>i=0</sub><sup>&infin;</sup>
     * ((a)<sub>i</sub>z<sup>i</sup>) / ((b)<sub>i</sub>i!)
     *  where (a)<sub>i</sub> is a Pochhammer symbol.
     * @param a the first argument
     * @param b the second argument
     * @param z the third argument
     * @return the value M(a, b, z)
     * @exception IllegalArgumentException the function is not defined
     *            for the choice of arguments
     */
    public static double M(double a, double b, double z) {
	boolean intA = (Math.rint(a) == a);
	boolean intB = (Math.rint(b) == b);
	if (intB) {
	    if (intA) {
		return M(Math.round(a), Math.round(b), z);
	    }
	    if (b <= 0.0) {
		String msg = errorMsg("secondArgNeg", b);
		throw new IllegalArgumentException(msg);
	    }
	} else if (intA) {
	    return M(Math.round(a), b, z);
	}
	double sum = 1.0;
	double term = 1.0;
	int j = 1;
	double aa = a;
	double bb = b;
	double tc = 0.0;
	do {
	    term *= aa*z/bb;
	    term /= j;
	    aa = a + j;
	    bb = b + j;
	    j++;
	    // Use Kahan's summation algorithm for accuracy.
	    double y = term - tc;
	    double t = sum + y;
	    tc = (t - sum) - y;
	    sum = t;
	} while ((sum != 0.0 && Math.abs(term/sum) > 1.e-16)
		 || Math.abs(term) > 1.e-32);
	return sum;
    }


    /**
     * Compute the confluent hypergeometric function M(a, b, z) when
     * a is a long integer.
     * M(a,b,z) = &sum;<sub>i=0</sub><sup>&infin;</sup>
     * ((a)<sub>i</sub>z<sup>i</sup>) / ((b)<sub>i</sub>i!)
     *  where (a)<sub>i</sub> is a Pochhammer symbol.
     * @param a the first argument
     * @param b the second argument
     * @param z the third argument
     * @return the value M(a, b, z)
     * @exception IllegalArgumentException the function is not defined
     *            for the choice of arguments
     */
    public static double M(long a, double b, double z) {
	if (Math.rint(b) == b) {
	    return M(a, Math.round(b), z);
	}
	double sum = 1.0;
	double term = 1.0;
	int j = 1;
	long aa = a;
	double bb = b;
	double tc = 0.0;
	do {
	    term *= aa*z/bb;
	    term /= j;
	    aa = a + j;
	    bb = b + j;
	    j++;
	    // Use Kahan's summation algorithm for accuracy.
	    double y = term - tc;
	    double t = sum + y;
	    tc = (t - sum) - y;
	    sum = t;
	} while ((sum != 0.0 && Math.abs(term/sum) > 1.e-16)
		 || Math.abs(term) > 1.e-32);
	return sum;
    }


    /**
     * Compute the confluent hypergeometric function M(a, b, z) when
     * a and b are long integers.
     * M(a,b,z) = &sum;<sub>i=0</sub><sup>&infin;</sup>
     * ((a)<sub>i</sub>z<sup>i</sup>) / ((b)<sub>i</sub>i!)
     *  where (a)<sub>i</sub> is a Pochhammer symbol.
     * @param a the first argument
     * @param b the second argument
     * @param z the third argument
     * @return the value M(a, b, z)
     * @exception IllegalArgumentException the function is not defined
     *             for the choice of arguments
     */
    public static double M(long a, long b, double z) {
	if (b <= 0) {
	    if (b >= a) {
		String msg = errorMsg("secondGTfirst", b, a);
		throw new IllegalArgumentException(msg);
	    }
	}
	double sum = 1.0;
	double term = 1.0;
	int j = 1;
	long aa = a;
	long bb = b;
	double tc = 0.0;
	do {
	    term *= (aa*z)/bb;
	    term /= j;
	    aa = a + j;
	    bb = b + j;
	    j++;
	    // Use Kahan's summation algorithm for accuracy.
	    double y = term - tc;
	    double t = sum + y;
	    tc = (t - sum) - y;
	    sum = t;
	} while ((sum != 0.0 && Math.abs(term/sum) > 1.e-16)
		 || Math.abs(term) > 1.e-32);
	return sum;
    }

    /**
     * Compute the derivative with respect to the third argument
     * of the confluent hypergeometric function.
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double dMdx(double a, double b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+1, b+1, x);
	term *= a;
	term /= b;
	return term;
    }

    /**
     * Compute the second derivative with respect to the third argument
     * of the confluent hypergeometric function.
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double d2Mdx2(double a, double b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+2, b+2, x);
	term *= a*(a+1);
	term /= b*(b+1);
	return term;
    }

    /**
     * Compute the derivative with respect to the third argument
     * of the confluent hypergeometric function when the first
     * argument is an integer.
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double dMdx(long a, double b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+1, b+1, x);
	term *= a;
	term /= b;
	return term;
    }

    /**
     * Compute the second derivative with respect to the third argument
     * of the confluent hypergeometric function when the first
     * argument is an integer..
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double d2Mdx2(long a, double b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+2, b+2, x);
	term *= a*(a+1);
	term /= b*(b+1);
	return term;
    }

    /**
     * Compute the derivative with respect to the third argument
     * of the confluent hypergeometric function when the first two
     * arguments are integers.
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double dMdx(long a, long b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+1, b+1, x);
	term *= a;
	term /= b;
	return term;
    }

    /**
     * Compute the second derivative with respect to the third argument
     * of the confluent hypergeometric function when the first two
     * arguments are integers..
     * @param a the first argument
     * @param b the second argument
     * @param x the third argument
     */
    public static double d2Mdx2(long a, long b, double x)
	throws IllegalArgumentException
    {
	double term = M(a+2, b+2, x);
	term *= a*(a+1);
	term /= b*(b+1);
	return term;
    }


    private static final double ERFC_LIMIT = 3.1;
    private static final double ROOT_PI = Math.sqrt(Math.PI);
    /**
     * Compute the error function.
     * The error function is defined as
     * erf(x) = (2/&pi;<sup>1/2</sup>);&int;<sub>0</sub><sup>x</sup>e<sup>-t<sup>2</sup></sup>dt.
     * <P>
     * The implementation uses Equation 7.1.5 (Abramowitz and Stegun) and
     * and when x &ge; 3.5, uses 1-erfc(x).
     * @param x the argument to the error function
     * @return the value erf(x)
     */
    public static double erf(double x) {
	if (x == 0.0) return 0.0;
	if (x <= -ERFC_LIMIT) return -erf(-x);
	if (x >= ERFC_LIMIT) return (1.0 - erfc(x));
	double limit = (Math.abs(x) < 1)? (1.e-15)*Math.abs(x): 1.e-15;
	double term = x;
	double xsq = x*x;
	double sum = term;
	if (Math.abs(x) < 0.5) {
	    int n = 1;
	    do {
		term *= (-1)*xsq/n;
		sum += term/(2*n+1);
		n++;
	    } while (Math.abs(term) > limit);
	    sum *= 2.0/ROOT_PI;
	} else {
	    double m = 1;
	    do {
		m += 2;
		term *= 2.0*xsq/m;
		sum += term;
	    } while (Math.abs(term) > limit);
	    sum *= 2.0*Math.exp(-xsq)/ROOT_PI;
	}
	return sum;
    }

    /**
     * This class does a high-precision computation of erfc, which
     * fills in a table used to create a spline to allow us to
     * interpolate over a range of values where accuracy is a problem:
     * values high enough that erf(x) is too close to 1.0 for
     * erfc(x) = 1 - erf(x) to be an accurate approximation, but too
     * low for the asymptotic expansion to be accurate to one part in
     * 10 to the 10th.
     * Because the values change rapidly, we fit the spline to the
     * natural logarithm of the value of the erfc function and then
     * use Math.exp to restore it.
     */
    public static class ErfTable {

	private ErfTable() {}

	static final BigDecimal rootPI = new
	    BigDecimal
	    ("1.772453850905516027298167483341145182"
	     + "79754945612238712821380778985291");
	static final BigDecimal sf =
	    (new BigDecimal(2)).divide(rootPI, 100, RoundingMode.HALF_EVEN);

	static double erf(double arg, int scale) {
	    BigDecimal x = new BigDecimal(arg);
	    x.setScale(scale);
	    BigDecimal xsq = x.multiply(x);
	    xsq.setScale(scale, RoundingMode.HALF_EVEN);
	    BigDecimal neg1 = BigDecimal.ONE.negate();

	    double limit = 1.0e-50;

	    BigDecimal term = new BigDecimal(arg);
	    term = term.setScale(scale);
	    BigDecimal sum = BigDecimal.ONE.multiply(term);
	    sum.setScale(scale);
	    int n = 1;
	    do {
		term = term.multiply(neg1);
		term = term.multiply(xsq);
		term = term.divide(new BigDecimal(n), scale,
				   RoundingMode.HALF_EVEN);
		sum = sum.add(term.divide(new BigDecimal(2*n+1), scale,
					  RoundingMode.HALF_EVEN));

		n++;
	    } while (Math.abs(term.doubleValue()) > limit);
	    sum = sum.multiply(sf);
	    return sum.doubleValue();
	}

	static double erfc(double arg, int scale) {
	    BigDecimal x = new BigDecimal(arg);
	    x.setScale(scale);
	    BigDecimal xsq = x.multiply(x);
	    xsq.setScale(scale, RoundingMode.HALF_EVEN);
	    BigDecimal neg1 = BigDecimal.ONE.negate();

	    double limit = 1.0e-50;

	    BigDecimal term = new BigDecimal(arg);
	    term = term.setScale(scale);
	    BigDecimal sum = BigDecimal.ONE.multiply(term);
	    sum.setScale(scale);
	    int n = 1;
	    do {
		term = term.multiply(neg1);
		term = term.multiply(xsq);
		term = term.divide(new BigDecimal(n), scale,
				   RoundingMode.HALF_EVEN);
		sum = sum.add(term.divide(new BigDecimal(2*n+1), scale,
					  RoundingMode.HALF_EVEN));

		n++;
	    } while (Math.abs(term.doubleValue()) > limit);
	    sum = sum.multiply(sf);
	    BigDecimal result = BigDecimal.ONE.subtract(sum);
	    return result.doubleValue();
	}

	static final int N = 500;
	static final int M = 300;
	static final double factor = 100.0;
	static double[] table = new double[N-M];
	static {
	    try {
		java.io.InputStream is = ClassLoader.
		    getSystemResourceAsStream("org/bzdev/math/ErfTable.dat");
		java.io.DataInputStream dis = new java.io.DataInputStream(is);
		int len = dis.readInt();
		if (len != table.length) {
		    // We are not reading the right table.
		    dis.close();
		    is.close();
		    throw new Exception("table mismatch");
		}
		int ourN = dis.readInt();
		int ourM = dis.readInt();
		double ourFactor = dis.readDouble();
		if (ourN != N || ourM != M || ourFactor != factor) {
		    dis.close();
		    is.close();
		    throw new Exception("table mismatch");
		}
		for (int i = 0; i < table.length; i++) {
		    table[i] = dis.readDouble();
		}
		dis.close();
		is.close();
	    } catch (Exception e) {
		// if the resource does not exist or something fails
		// while reading it, just reconstruct the table from
		// scratch.
		//
		// Add the following line if it is necessary to test
		// that resource-loading failed (normally not needed).
		//
		// System.err.println("Building ErfTable.dat");
		for (int i = M; i < N; i++) {
		    double x = i/factor;
		    table[i-M] = Math.log(erfc(x, 100));
		}
	    }
	}

	/**
	 * Dump an internal table to System.out in binary form.
	 * This program is used to create a JAR-file resource when
	 * the BZdev class library is built. There is little point in
	 * using it for any other purpose.  The class name for this
	 * program is org.bzdev.math.Functions$ErfTable (note the use
	 * of a '$' in the name).
	 * @param argv command-line arguments (ignored)
	 */
	public static void main(String[] argv) throws java.io.IOException {
	    java.io.DataOutputStream dos =
		new java.io.DataOutputStream(System.out);
	    // write out the variables used in creating the table so we
	    // can check if the source code is consistent with the table
	    // actually being used.
	    dos.writeInt(table.length);
	    dos.writeInt(N);
	    dos.writeInt(M);
	    dos.writeDouble(factor);
	    for (int i = 0; i < table.length; i++) {
		dos.writeDouble(table[i]);
	    }
	    dos.flush();
	}

	static final CubicSpline erfcSpline =
	    new CubicSpline1(table, M/factor, 1.0/factor);
    }


    /**
     * Compute the error function using fixed-point arithmetic
     * @param arg the argument
     * @param scale the number of digits past the decimal point.
     * @return the value erf(arg)
     */
    public static double erf(double arg, int scale) {
	return ErfTable.erf(arg, scale);
    }

    /**
     * Compute the error function complement  using fixed-point arithmetic
     * @param arg the argument
     * @param scale the number of digits past the decimal point.
     * @return the value erfc(arg)
     */
    public static double erfc(double arg, int scale) {
	return ErfTable.erfc(arg, scale);
    }

    /**
     * Complementary error function.
     * This function is defined as erfc(x) = 1.0 - erf(x)
     * if the argument is larger than or equal to 3.5, the asymptotic
     * expansion given by Equation 7.1.23 (Abramowitz and Stegun) is
     * used.
     * @param x the argument
     * @return the value erfc(x)
     */
    public static double erfc(double x) {
	if (x >= ERFC_LIMIT) {
	    if (x < 4.9) {
		return Math.exp(ErfTable.erfcSpline.valueAt(x));
	    } else {
		double xsq = x*x;
		double sum = 1.0;
		double term = 1.0;
		int m = 1;
		double delta;
		do {
		    delta = (2*m-1)/(2.0*xsq);
		    term *= (-1) * delta;
		    sum += term;
		    m++;
		} while (delta < 1.0);
		return sum/(ROOT_PI*x*Math.exp(xsq));
	    }
	} else {
	    return 1.0 - erf(x);
	}
    }

    /**
     * Compute the Beta function.
     * @param x the first argument
     * @param y the second argument
     * @return either the value &Beta;(x,y) or negative or positive
     *         infinity when &Beta;(x,y) is infinite or when either
     *         &Beta;(x,y) or an intermediate value when computing it is
     *         too large to be represented as a double-precision number.
     */
    public static double Beta(double x, double y) {
	double xy = x + y;
	boolean xNPInt = (x <= 0.0 && x == Math.rint(x));
	boolean yNPInt = (y <= 0.0 && y == Math.rint(y));
	boolean xyNPInt = (xy <= 0.0 && xy == Math.rint(xy));

	double gx = Gamma(x);
	double gy = Gamma(y);
	double gxy = Gamma(xy);
	if (x > 0.0 && y > 0.0) {
	    if (gx == Double.POSITIVE_INFINITY ||
		gy == Double.POSITIVE_INFINITY ||
		gxy == Double.POSITIVE_INFINITY) {
		return Math.exp(logGamma(x) + logGamma(y) - logGamma(xy));
	    } else {
		// watch order of evaluation to reduce chances of overflows
		return (gx/gxy)*gy;
	    }
	}
	if (xyNPInt) {
	    if (xNPInt && yNPInt) {
		boolean flip = gx != gy;
		flip = (gxy == Double.NEGATIVE_INFINITY)? !flip: flip;
		return flip? Double.NEGATIVE_INFINITY: Double.POSITIVE_INFINITY;
	    } else if (xNPInt) {
		double g1mxy = Gamma(1.0-xy);
		double g1mx = Gamma(1.0-x);
		double sign = (gx == gxy)? 1.0: -1.0;
		return sign * (gy /g1mx) * g1mxy;
	    } else if (yNPInt) {
		double g1mxy = Gamma(1.0-xy);
		double g1my = Gamma(1.0-y);
		double sign = (gy == gxy)? 1.0: -1.0;
		return sign * (gx /g1my) * g1mxy;
	    } else {
		return 0.0;
	    }
	} else {
	    if (xNPInt && yNPInt) {
		boolean flip = gx != gy;
		flip = (gxy < 0)? !flip: flip;
		return flip? Double.NEGATIVE_INFINITY: Double.POSITIVE_INFINITY;
	    } else if (xNPInt) {
		boolean flip = (gy < 0.0);
		flip = (gxy < 0.0)? !flip: flip;
		if (flip) {
		    return (gx == Double.POSITIVE_INFINITY)?
			Double.NEGATIVE_INFINITY: Double.POSITIVE_INFINITY;
		} else {
		    return gx;
		}
	    } else if (yNPInt) {
		boolean flip = (gx < 0.0);
		flip = (gxy < 0.0)? !flip: flip;
		if (flip) {
		    return (gy == Double.POSITIVE_INFINITY)?
			Double.NEGATIVE_INFINITY: Double.POSITIVE_INFINITY;
		} else {
		    return gy;
		}
	    } else {
		return (gx/gxy)*gy;
	    }
	}
    }

    /**
     * Compute the incomplete beta function &Beta;<sub>x</sub>(a,b).
     * The incomplete beta function is defined as
     * &Beta;<sub>x</sub>(a,b) = &int;<sub>0</sub><sup>x</sup>
     * t<sup>a-1</sup>(1-t)<sup>b-1</sup> dt.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the value &Beta;<sub>x</sub>(a,b)
     */
    public static double Beta(double x, double a, double b) {
	if (a <= 0.0) {
	    throw new IllegalArgumentException(errorMsg("secondArgPos", a));
	}
	if (b <= 0.0) {
	    throw new IllegalArgumentException(errorMsg("thirdArgPos", b));
	}
	if (x > 0.7) {
	    return Beta(a,b) - Beta(1.0-x, b, a);
	}
	double sum = 0.0;
	double term = 1.0;
	int n = 0;
	double xp = 1;
	while (term > (1.e-15)*x) {
	    n++;
	    sum += term;
	    xp *= x;
	    term = (Functions.Beta(a+1, (double)n)
		    / Functions.Beta(a+b, (double)n)) * xp;
	}
	System.out.println("n = " + n);
	sum *= (Math.pow(x, a)*Math.pow(1-x, b) / a );
	return sum;
	// return  Math.pow(x,a)*hgF(a,1-b,a+1,x) / a;
    }


    /**
     * Compute the derivative with respect to x of the incomplete
     * beta function &Beta;<sub>x</sub>(a,b).  The incomplete beta
     * function is defined as &Beta;<sub>x</sub>(a,b) =
     * &int;<sub>0</sub><sup>x</sup>
     * t<sup>a-1</sup>(1-t)<sup>b-1</sup> dt.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the derivative with respect to x of  &Beta;<sub>x</sub>(a,b)
     */
    public static double dBetadx(double x, double a, double b) {
	return Math.pow(x,a-1)*Math.pow(1-x,b-1);
    }

    /**
     * Compute the second derivative with respect to x of the incomplete
     * beta function &Beta;<sub>x</sub>(a,b).  The incomplete beta
     * function is defined as &Beta;<sub>x</sub>(a,b) =
     * &int;<sub>0</sub><sup>x</sup>
     * t<sup>a-1</sup>(1-t)<sup>b-1</sup> dt.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the second derivative with respect to x of
     *          &Beta;<sub>x</sub>(a,b)
     */
    public static double d2Betadx2(double x, double a, double b) {
	return (a-1)*Math.pow(x,a-2)*Math.pow(1-x,b-1)
	    - (b-1)*Math.pow(x, a-1)*Math.pow(1-x,b-2);
    }

    /**
     * Compute the regularized incomplete beta function I<sub>x</sub>(a,b).
     * This incomplete beta function is defined as
     * I<sub>x</sub>(a,b) = &Beta;<sub>x</sub>(a,b) / &Beta;(a,b).
     * <P>
     * Note: I is prefaced with "Beta" in the method name to avoid
     * confusion, as Java does not use subscripts that are really
     * function arguments.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the value of I<sub>x</sub>(a,b)
     */
    public static double BetaI(double x, double a, double b) {
	return Beta(x, a, b) / Beta(a,b);
    }


    /**
     * Compute the derivative with respect to x of the regularized
     * incomplete beta function I<sub>x</sub>(a,b).
     * This incomplete beta function is defined as
     * I<sub>x</sub>(a,b) = &Beta;<sub>x</sub>(a,b) / &Beta;(a,b).
     * <P>
     * Note: I is prefaced with "Beta" in the method name to avoid
     * confusion, as Java does not use subscripts that are really
     * function arguments.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the derivative with respect to x of I<sub>x</sub>(a,b)
     */
    public static double dBetaIdx(double x, double a, double b) {
	return dBetadx(x, a, b) / Beta(a,b);
    }

    /**
     * Compute the second derivative with respect to x of the regularized
     * incomplete beta function I<sub>x</sub>(a,b).
     * This incomplete beta function is defined as
     * I<sub>x</sub>(a,b) = &Beta;<sub>x</sub>(a,b) / &Beta;(a,b).
     * <P>
     * Note: I is prefaced with "Beta" in the method name to avoid
     * confusion, as Java does not use subscripts that are really
     * function arguments.
     * @param x the limit to the integral
     * @param a the first argument
     * @param b the second argument
     * @return the second derivative with respect to x of I<sub>x</sub>(a,b)
     */
    public static double d2BetaIdx2(double x, double a, double b) {
	return d2Betadx2(x, a, b) / Beta(a,b);
    }

    // inverse hyperbolic functions  - these are not provided
    // by the java.lang.Math class.

    /**
     * Compute the inverse hyperbolic cosine.
     * @param x a real number larger than or equal to 1.0
     * @return cosh<sup>-1</sup>(x)
     */
    public static double acosh(double x) {
	return Math.log(x + Math.sqrt(x*x - 1));
    }

    /**
     * Compute the inverse hyperbolic sin.
     * @param x a real number
     * @return sinh<sup>-1</sup>(x)
     */
    public static double asinh(double x) {
	if (x < 0.0) {
	    return -Math.log(Math.sqrt(x*x + 1) - x);
	}
	return Math.log(x + Math.sqrt(x*x + 1));
    }

    /**
     * Compute the inverse hyperbolic tangent.
     * @param x a real number  in the range [-1.0, 1.0]
     * @return tanh<sup>-1</sup>(x)
     */
    public static double atanh(double             x) {
	if (x == 1.0) return Double.POSITIVE_INFINITY;
	if (x == -1.0) return Double.NEGATIVE_INFINITY;
	return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }

    // Airy Functions
    private static final double oneThird = 1.0/3.0;
    private static final double twoThirds = 2.0/3.0;
    private static final double Ai0 =
	1.0/(Math.pow(3.0,twoThirds)*Gamma(twoThirds));
    private static final double Bi0 =
	1.0/(Math.pow(3.0, 1.0/6.0)*Gamma(twoThirds));
    private static final double root3 = Math.sqrt(3.0);
    private static final double twoRoot3 = 2.0*root3;
    private static final double Ai0p =
	-1.0/(Math.pow(3.0, oneThird) *Gamma(oneThird));
    private static final double Bi0p = Math.pow(3.0, 1.0/6.0)/Gamma(oneThird);
    private static final double r3Ai0 = root3*Ai0;
    private static final double r3Ai0p = root3*Ai0p;
    private static final double AIRY_LIMIT = 1.0e-12;

    // compute f(z) and g(z) (Abramowitz & Stegun for Airy functions)
    // simultaneously as they share a lot of terms.
    private static void AiryFG(double z, double[]results) {
	double sum = 0.0;
	double c = 0.0;
	double cg = 0.0;
	double sumg = 0.0;
	double term = 1.0;
	double termg = 1.0;
	double z3 = z*z*z;
	double factor = 1.0;
	double factorg = 1.0;
	int k3 = 0;
	double limit, limitg;
	double fterm = 1.0;
	double ftermg = 1.0*z;
	do {
	    // sum += fterm;
	    double y = fterm - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    // sumg += ftermg;
	    double yg = ftermg - cg;
	    double tg = sumg + yg;
	    cg = (tg - sumg) - yg;
	    sumg = tg;
	    term *= z3;
	    term /= ++k3;
	    term /= ++k3;
	    term /= ++k3;
	    termg = z*term/(k3+1);
	    factor *= k3-2;
	    factorg *= k3-1;
	    fterm = term*factor;
	    ftermg = termg*factorg;
	    limit = Math.max(Math.abs(sum), 1.0);
	    limitg = Math.max(Math.abs(sumg), 1.0);
	} while ((Math.abs(fterm/limit) >= AIRY_LIMIT)
		 || (Math.abs(ftermg/limitg)>= AIRY_LIMIT));
	results[0] = sum;
	results[1] = sumg;
    }

    private static void derivAiryFG(double z, double[]results) {
	double sum = 0.0;
	double c = 0.0;
	double sumg = 1.0;
	double cg = 0.0;
	double z2 = z*z;
	double z3 = z2*z;
	double term = z2/2.0;
	double termg;
	double factor = 1.0;
	double factorg = 2.0;
	int k3 = 3;
	double limit, limitg;
	double fterm = z2*0.5;
	double ftermg = z3/3.0;
	do {
	    // sum += fterm;
	    double y = fterm - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    // sumg += ftermg;
	    double yg = ftermg - cg;
	    double tg = sumg + yg;
	    cg = (tg - sumg) - yg;
	    sumg = tg;
	    term *= z3;
	    term /= k3++;
	    term /= k3++;
	    term /= k3++;
	    termg = z*term/k3;
	    factor *= k3-2;
	    factorg *= k3-1;
	    fterm = term*factor;
	    ftermg = termg*factorg;
	    limit = Math.max(Math.abs(sum), 1.0);
	    limitg = Math.max(Math.abs(sumg), 1.0);
	} while ((Math.abs(fterm/limit) >= AIRY_LIMIT)
		 || (Math.abs(ftermg/limitg)>= AIRY_LIMIT));
	results[0] = sum;
	results[1] = sumg;
    }

    /**
     * Compute the Airy function of the first kind.
     * The Airy functions Ai and Bi are solutions to the
     * differential equation y'' - xy = 0.
     * @param x the argument
     * @return the value of Ai(x)
     */
    public static double Ai(double x) {
	if (x == 0.0) {
	    return Ai0;
	} else {
	    double[] results = new double[2];
	    AiryFG(x, results);
	    return Ai0*results[0] + Ai0p*results[1];
	}
	/*
	} else if (x > 0.0) {
	    return (Math.sqrt(x/3)/Math.PI)
		* K(oneThird, twoThirds * Math.pow(x, 1.5));
	} else {
	    double tmp = twoThirds*Math.pow(x,1.5);
	    return Math.sqrt(x/9)
		* (J(oneThird,tmp) + J(-oneThird,tmp));
	}
	*/
    }

    /**
     * Compute the first derivative of the Airy function of the first kind.
     * The Airy functions Ai and Bi are solutions to the
     * differential equation y'' - xy = 0.
     * @param x the argument
     * @return the value of d[Ai(x]/ dx
     */
    public static double dAidx(double x) {
	if (x == 0.0) return Ai0p;
	double[] results = new double[2];
	derivAiryFG(x, results);
	return Ai0*results[0] + Ai0p*results[1];
	/*
	return (-x/(root3*Math.PI)) * K(twoThirds, twoThirds*Math.pow(x,1.5));
	 */
    }

    /**
     * Compute the Airy function of the second kind.
     * The Airy functions Ai and Bi are solutions to the
     * differential equation y'' - xy = 0.
     * @param x the argument
     * @return the value of Bi(x)
     */
    public static double Bi(double x) {
	if (x == 0.0) {
	    return Bi0;
	} else {
	    double[] results = new double[2];
	    AiryFG(x, results);
	    return r3Ai0 * results[0] - r3Ai0p * results[1];
	}
	/*
	} else if (x > 0.0) {
	    double tmp = twoThirds*Math.pow(x,1.5);
		return Math.sqrt(x/3.0)
		* (I(oneThird, tmp) + I(-oneThird, tmp));
	} else {
	    double tmp = twoThirds*Math.pow(x,1.5);
	    return Math.sqrt(x/9)
		* (J(-oneThird, tmp) - J(oneThird, tmp));
	}
	*/
    }

    /**
     * Compute the first derivative of the Airy function of the second kind.
     * The Airy functions Ai and Bi are solutions to the
     * differential equation y'' - xy = 0.
     * @param x the argument
     * @return the value of d[Bi(x)]/dx
     */
    public static double dBidx(double x) {
	if (x == 0.0) return Bi0p;
	double[] results = new double[2];
	derivAiryFG(x, results);
	return r3Ai0 * results[0] - r3Ai0p * results[1];
	/*
	double tmp = twoThirds*Math.pow(x,1.5);
	return (x/root3) * (I(-twoThirds, tmp) + I(twoThirds, tmp));
	*/
    }

    private static final double log4PI = Math.log(4.0*Math.PI);

    /**
     * Compute the amplitude of of spherical harmonics.
     * Java cannot represent spherical harmonics because they are
     * complex-valued functions. For a spherical harmonic
     * Y<sub>lm</sub>(&theta;&phi;), one can define an amplitude
     * given by Y<sub>lm</sub>(&theta;&phi;)e<sup>-im&phi;</sup>.
     * The absolute value of the amplitude of Y<sub>lm</sub>(&theta;&phi;)
     * is equal the magnitude of Y<sub>lm</sub>(&theta;&phi;) but may
     * differ in sign depending on the value of
     * P<sub>l</sub><sup>m</sup>(&theta;). This method returns the
     * amplitude of a spherical harmonic - a normalization of the function
     * P<sub>l</sub><sup>m</sup>(&theta;).  The normalization factor
     * is sqrt((2l+1)(l-m)!/(4&pi;(l+m)!)).
     * <P>
     * Citation: J.D. Jackson, "Classical Electrodynamics," page 65,
     * John Wiley &amp; Sons, 1962 (fifth printing).
     * @param ell the degree of the spherical harmonic
     * @param m the order of the spherical harmonic
     * @param theta the angle of a point along a unit sphere with respect
     *        to the z axis in radians
     * @return the amplitude (as defined above) of the spherical harmonic
     *         Y<sub>lm</sub>(&theta;&phi;)
     */
    public static double Yamp(int ell, int m, double theta) {
	double fact = factorial(ell+m);
	if (fact == Double.POSITIVE_INFINITY) {
	    // unusual case, so we don't do this all the time.
	    double tmpl = (Math.log(2*ell+1) + logFactorial(ell-m)
			   - (log4PI + logFactorial(ell+m)))
		/ 2.0;
	    return Math.exp(tmpl)* P(ell,m,Math.cos(theta));
	}
	double tmp = (2*ell + 1) * factorial(ell - m);
	tmp /= 4.0*Math.PI * fact;
	return Math.sqrt(tmp) * P(ell, m, Math.cos(theta));
    }

    /**
     * Compute the value of a Laguerre polynomial.
     * @param n the degree of the polynomial
     * @param x the argument
     * @return the value of L<sub>n</sub>(x)
     */
    public static double L(int n, double x) {
	double z = -x;
	double term = 1.0;
	double sum = 1.0;
	double c = 0.0;
	if (n < Binomial.table.length) {
	    for (int k = 1; k <= n; k++) {
		term *= z/k;
		// sum += (Binomial.table[n][k])*term;
		double y = ((Binomial.table[n][k])*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    }
	} else if (n < Binomial.etable.length) {
	    for (int k = 1; k <= n; k++) {
		term *= z/k;
		// sum += Binomial.etable[n][k]*term;
		double y = (Binomial.etable[n][k]*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    }
	} else {
	    for (int k = 1; k <= n; k++) {
		term *= z/k;
		// sum += Binomial.coefficient(n, k)*term;
		double y = (Binomial.coefficient(n, k)*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	    }
	}
	return sum;
    }

    /**
     * Compute the value of the derivative of a Laguerre polynomial.
     * @param n the degree of the polynomial
     * @param x the argument
     * @return the derivative d[L<sub>n</sub>(x)]/dx.
     */
    public static double dLdx(int n, double x) {
	double z = -x;
	double term = -1.0;
	double sum = 0.0;
	double c = 0.0;
	if (n < Binomial.table.length) {
	    for (int k = 1; k <= n; k++) {
		// sum += (Binomial.table[n][k])*term;
		double y = ((Binomial.table[n][k])*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		term *= z/k;
	    }
	} else if (n < Binomial.etable.length) {
	    for (int k = 1; k <= n; k++) {
		// sum += Binomial.etable[n][k]*term;
		double y = (Binomial.etable[n][k]*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		term *= z/k;
	    }
	} else {
	    for (int k = 1; k <= n; k++) {
		// sum += Binomial.coefficient(n, k)*term;
		double y = (Binomial.coefficient(n, k)*term) - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
		term *= z/k;
	    }
	}
	return sum;
    }


    /**
     * Compute the value of an associated Laguerre polynomial.
     * @param n the degree of the polynomial
     * @param alpha the order of the associated Laguerre polynomial
     * @param x the argument
     * @return the value of L<sub>n</sub><sup>(&alpha;)</sup>(x)
     */

    public static double L(int n, double alpha, double x) {
	if (n == 0) return 1.0;
	double first = 1.0 + alpha - x;
	if (n == 1) return first;
	double prevprev = 1.0;
	double prev = first;
	double next = prev;
	for (int k = 1; k < n; k++ ) {
	    next = ((2*k + first)*prev - (k + alpha)*prevprev)/(k+1);
	    prevprev = prev;
	    prev = next;
	}
	return next;
    }

    /**
     * Compute the value of the derivative of an associated Laguerre polynomial.
     * @param n the degree of the polynomial
     * @param alpha the order of the associated Laguerre polynomial
     * @param x the argument
     * @return the derivative d[L<sub>n</sub><sup>(&alpha;)</sup>(x)]/dx.
     */
    public static double dLdx(int n, double alpha, double x) {
	if (n == 0) return 0.0;
	return -1.0 * L(n-1, alpha+1.0, x);
    }
}

//  LocalWords:  exbundle li Ai dAidx dBidx blockquote pre dBdx Bdx
//  LocalWords:  Bdxdy dJdx dx dYdx djdx dydx dIdx dKdx erf dt erfc
//  LocalWords:  logFactorial longFactorial exactFactorial BigInteger
//  LocalWords:  digamma logGamma poch Pochhammer's hypergeometric bn
//  LocalWords:  hgF bd asinh sinh acosh atanh tanh Laguerre dLdx dP
//  LocalWords:  dPdx lpow xn xd Yamp Abramowitz Stegun href Kummer's
//  LocalWords:  th Kahan's IllegalArgumentException argNotPositive
//  LocalWords:  intArgNotPositive sumB dsumBdx sumBdx PathIterator
//  LocalWords:  GUIs le indexForLambdas lambdasForIndex len indices
//  LocalWords:  generateIndices xInd sumBdxdy yInd Casteljau's isin
//  LocalWords:  argNonNegative argArrayTooShort argOutOfRangeD nd zz
//  LocalWords:  argsOutOfRange argOutOfRangeI polynomial's Farin tmp
//  LocalWords:  eacute zier incompatibleArrays tauInd secondArgNeg
//  LocalWords:  fsum Rodrigues thirdArgOutOfRange valueAt dsum dterm
//  LocalWords:  iterationLimit incr oeis lookup Hasse prev nok gcd
//  LocalWords:  arraycopy thirdArgZero notRealPow argTooLarge Nemes
//  LocalWords:  ArithmeticException argNonNegativeD arg argNotInts
//  LocalWords:  nonIntegerExponent NotNegIntOrZero cabTooLarge ge xy
//  LocalWords:  zTooLargeForIntCAB argRatioNotInts secondArgZero dat
//  LocalWords:  argRatioNumbNotInts fourthArgZero ErfTable BZdev lm
//  LocalWords:  argv fterm sumg ftermg oneThird twoThirds sqrt im De
//  LocalWords:  etable barycentric dMdx Mdx lPow zeroArgument pdf ir
//  LocalWords:  relativelyPrime precomputed firstArgNotPositive npir
//  LocalWords:  infin Pochhammer thirdArgNeg fourthArgNeg npirp rlen
//  LocalWords:  preallocated th monomial secondGTfirst secondArgPos
//  LocalWords:  thirdArgPos
