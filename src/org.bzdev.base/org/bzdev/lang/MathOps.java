package org.bzdev.lang;

//@exbundle org.bzdev.lang.lpack.Lang

/**
 * Mathematical operations related to java primitive types and
 * some trivial extensions to the package {@link java.lang.Math}.
 * This class contains only static methods.
 */
public class MathOps {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }

    // to prevent a constructor entry in the API documentation
    private MathOps() {}

    /**
     * Turn a double into an int.
     * This method is provided for convenience when scripting
     * languages such as the ones supported by
     * {@link org.bzdev.util.ExpressionParser} are used:
     * {@link org.bzdev.util.ExpressionParser} has no explicit iteration
     * constructs and instead relies on Java streams. In this case one
     * may have to force an expression to provide a specific numberical type.
     * @param value the double-precision value
     * @return the corresponding integer
     * @exception IllegalArgumentException
     *            if the double value is not the value one would obtain
     *            by converting the closest integer into a double-precision
     *            value or if the integer value is too large or too small
     *            to be prepresented as an int
     */
    public static int asInt(double value) throws IllegalArgumentException {
	long lvalue = Math.round(value);
	if (lvalue == value) {
	    if (lvalue < Integer.MIN_VALUE || lvalue > Integer.MAX_VALUE) {
		throw new
		    IllegalArgumentException(errorMsg("range", value));
	    }
	    return (int) lvalue;
	} else {
	    throw new IllegalArgumentException(errorMsg("notInteger", value));
	}
    }


    /**
     * Turn a double into a long.
     * This method is provided for convenience when scripting
     * languages such as the ones supported by
     * {@link org.bzdev.util.ExpressionParser} are used:
     * {@link org.bzdev.util.ExpressionParser} has no explicit iteration
     * constructs and instead relies on Java streams. In this case one
     * may have to force an expression to provide a specific numberical type.
     * @param value the double-precision value
     * @return the corresponding integer
     * @exception IllegalArgumentException
     *            if the double value is not the value one would obtain
     *            by converting the closest integer into a double-precision
     *            value
     */
    public static long asLong(double value) throws IllegalArgumentException {
	long lvalue = Math.round(value);
	if (lvalue == value) {
	    return lvalue;
	} else {
	    throw new IllegalArgumentException(errorMsg("notInteger", value));
	}
    }

    /**
     * Convert an int to a double.
     * This method is provided for convenience when scripting
     * languages such as the ones supported by
     * {@link org.bzdev.util.ExpressionParser} are used:
     * {@link org.bzdev.util.ExpressionParser} has no explicit iteration
     * constructs and instead relies on Java streams. In this case one
     * may have to force an expression to provide a specific numberical type.
     * @param value the value to convert
     * @return the corresponding double-precision value
     */
    public static double asDouble(int value) {
	return (double)value;
    }

    /**
     * Convert a long to a double.
     * This method is provided for convenience when scripting
     * languages such as the ones supported by
     * {@link org.bzdev.util.ExpressionParser} are used:
     * {@link org.bzdev.util.ExpressionParser} has no explicit iteration
     * constructs and instead relies on Java streams. In this case one
     * may have to force an expression to provide a specific numberical type.
     * @param value the value to convert
     * @return the corresponding double-precision value
     */
    public static double asDouble(long value) {
	return (double)value;
    }

    /**
     * Convert a double to a double.
     * This method simply returns its value, and is provided for
     * convenience when scripting languages such as the ones
     * supported by {@link org.bzdev.util.ExpressionParser} are used:
     * {@link org.bzdev.util.ExpressionParser} has no explicit iteration
     * constructs and instead relies on Java streams. In this case one
     * may have to force an expression to provide a specific numberical type.
     * @param value the value to convert
     * @return the corresponding double-precision value
     */
    public static double asDouble(double value) {
	return value;
    }


    /**
     * Round a double-precision floating point number to the nearest
     * single-precision floating point number larger than or equal
     * to its value.
     * @param value the value to round
     * @return the closest single-precision floating point number greater than
     *         or equal to the argument
     */
    public static float fceil(double value) {
	float v1 = (float)value;
	if (v1 == value) return v1;
	float v2  = Math.nextAfter(v1, value);
	return (v1 < v2)? v2: v1;
    }

    /**
     * Round a double-precision floating point number to the nearest
     * single-precision floating point number less than or equal to
     * its value.
     * @param value the value to round
     * @return the closest single-precision floating point number less than
     *         or equal to the argument
     */
    public static float ffloor(double value) {
	float v1 = (float)value;
	if (v1 == value) return v1;
	float v2  = Math.nextAfter(v1, value);
	return (v1 < v2)? v1: v2;
    }

    /**
     * Round a double-precision floating point number to the nearest
     * single-precision floating point number whose absolute value
     * is less than or equal to the double-precision number's value.
     * @param value the value to round
     * @return the closest single-precision floating point number less than
     *         or equal to the argument
     */
    public static float froundTowardZero(double value) {
	float v1 = (float)value;
	if (v1 == value) return v1;
	float v2  = Math.nextAfter(v1, 0.0);
	return (Math.abs((double)v1) < Math.abs(value))? v1: v2;
    }

    /**
     * Round a double-precision floating point number to the nearest
     * single-precision floating point number whose absolute value
     * is greater than or equal to the double-precision number's value.
     * @param value the value to round
     * @return the closest single-precision floating point number greater than
     *         or equal to the argument
     */
    public static float froundTowardInf(double value) {
	float v1 = (float)value;
	if (v1 == value) return v1;
	float v2  = Math.nextAfter(v1, ((value > 0)? Double.MAX_VALUE:
					-Double.MAX_VALUE));
	return (Math.abs((double)v1) > Math.abs(value))? v1: v2;
    }

    /**
     * Compute the greatest common divisor of two integers
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of a and b
     * @exception IllegalArgumentException both arguments were zero
     */
    public static int gcd(int a, int b) throws IllegalArgumentException {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a < 0) a = -a;
	if (b < 0) b = -b;
	return gcdNN(a, b);
    }

    // valid for non-negative integers
    private static int gcdNN(int a, int b) {
	if (b == 0) return a;
	else return gcdNN(b, a%b);
    }

    /**
     * Compute the greatest common divisor of two non-negative long integers
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of a and b
     * @exception IllegalArgumentException both arguments were zero
     */
    public static long gcd(long a, long b) throws IllegalArgumentException {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a < 0) a = -a;
	if (b < 0) b = -b;
	return gcdNN(a, b);
    }

    // valid for non-negative integers
    private static long gcdNN(long a, long b) {
	if (b == 0) return a;
	else return gcdNN(b, a%b);
    }

    /**
     * Round a double-precision number to produce an integer.
     * <P>
     * This method was added specifically to support the
     * class {@link org.bzdev.util.ExpressionParser}: Java does not let
     * one cast a double-precision method to an integer and
     * {@link Math#round(double)} returns a long integer.
     * @param value the value to round
     * @return the value rounded to the nearest integer
     * @exception IllegalArgumentException the value is out of range
     *            and cannot be converted to an integer
     */
    public static int intRound(double value)
	throws IllegalArgumentException
    {
	long lvalue = Math.round(value);
	if (lvalue < Integer.MIN_VALUE || lvalue > Integer.MAX_VALUE) {
	    throw new IllegalArgumentException(errorMsg("range", value));
	}
	return (int)lvalue;
    }

    /**
     * Raise a number to an integer power.
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

    /**
     * Compute the nth root of a real number.
     * The second argument must be non-negative when the first argument
     * is an even integer.
     * @param n the root index (2 for the square root, 3 for the cube root,
     *         etc.)
     * @param x the number whose root is to be computed.
     * @return the nth root
     * @exception IllegalArgumentException an argument was out of range
     */

    public static double root(int n, double x) {
	if (n < 1) throw new IllegalArgumentException
		       (errorMsg("firstArgNotPositive", n));
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



    /**
     * Compute the base-2 logarithm of an integer n.
     * @param n an integer that is larger than 0
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
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
     * One use of this method is in estimating the time or space needed by an
     * algorithm, where precise values are not needed.
     * <P>
     * Note: when the accuracy is set to 1.0, the value returned is
     * floor(log2(n)). The accuracy is an upper bound on the difference
     * between the returned value and the actual value.
     * @param n an integer that is larger than 0
     * @param accuracy the accuracy of the fractional part of the returned value
     *        (values should be in the range (0.0, 1.0])
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
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
     * @param n an integer that is larger than 0
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
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
     * One use of this method is in estimating the time or space needed by an
     * algorithm, where precise values are not needed.
     * @param n a long integer that is larger than 0
     * @param accuracy the accuracy of the fractional part of the returned value
     *        (values should be in the range (0.0, 1.0])
     * @return the base-2 logarithm of n
     * @exception IllegalArgumentException an argument was out of range
     */
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
}

//  LocalWords:  exbundle IllegalArgumentException zeroArgument xn xd
//  LocalWords:  firstArgNotPositive notRealPow thirdArgZero gcd
//  LocalWords:  ArithmeticException argNonNegative argsOutOfRange
//  LocalWords:  intArgNotPositive argNotPositive
