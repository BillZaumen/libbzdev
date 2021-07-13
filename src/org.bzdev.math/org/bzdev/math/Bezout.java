package org.bzdev.math;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Compute B&eacute;zout coefficients for two integers.
 * The computation uses the extended Euclidean algorithm.
 */
public class Bezout {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Find two coefficients (s,t) such that gcd(a,b) = sa+tb for positive
     * integers a and b.
     * @param a the first integer
     * @param b the second integer
     * @param result an array whose first component is s and whose
     *        second component is t, where gcd(a,b) = sa+tb
     * @return gcd(a,b) (the greatest common divisor of a and b)
     */
    public static int gcd(int a, int b, int[] result) {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a == 0) {
	    result[0] = 0;
	    result[1] = Integer.signum(b);
	    return Math.abs(b);
	} else if (b == 0) {
	    result[0] = Integer.signum(a);;
	    result[1] = 0;
	    return Math.abs(a);
	}
	
	boolean aNeg = (a < 0);
	boolean bNeg = (b < 0);
	if (aNeg) a = -a;
	if (bNeg) b = -b;
	boolean swap = false;
	if (b > a) {
	    int tmp = a;
	    a = b;
	    b = tmp;
	    swap = true;
	}
	int rm1 = a;
	int r = b;
	int sm1 = 1;
	int s = 0;
	int tm1 = 0;
	int t = 1;
	for (;;) {
	    int q = rm1/r;
	    int rp1 = rm1 - q*r;
	    int sp1 = sm1 - q*s;
	    int tp1 = tm1 - q*t;
	    if (rp1 == 0) break;
	    rm1 = r;
	    r = rp1;
	    sm1 = s;
	    s = sp1;
	    tm1 = t;
	    t = tp1;
	}
	if (swap) {
	    if (aNeg) t = -t;
	    if (bNeg) s = -s;
	    result[1] = s;
	    result[0] = t;
	} else {
	    if (aNeg) s = -s;
	    if (bNeg) t = -t;
	    result[0] = s;
	    result[1] = t;
	}
	return r;
    }

    /**
     * Find two coefficients (s,t) such that gcd(a,b) = sa+tb for positive
     * long integers a and b.
     * @param a the first integer
     * @param b the second integer
     * @param result an array whose first component is s and whose
     *        second component is t, where gcd(a,b) = sa+tb
     * @return gcd(a,b) (the greatest common divisor of a and b)
     */
    public static long gcd(long a, long b, long[] result) {
	if (a == 0 && b == 0) {
	    throw new IllegalArgumentException(errorMsg("zeroArgument2", a, b));
	}
	if (a == 0) {
	    result[0] = 0;
	    result[1] = Long.signum(b);
	    return Math.abs(b);
	} else if (b == 0) {
	    result[0] = Long.signum(a);
	    result[1] = 0;
	    return Math.abs(a);
	}

	boolean aNeg = (a < 0);
	boolean bNeg = (b < 0);
	if (aNeg) a = -a;
	if (bNeg) b = -b;
	boolean swap = false;
	if (b > a) {
	    long tmp = a;
	    a = b;
	    b = tmp;
	    swap = true;
	}
	long rm1 = a;
	long r = b;
	long sm1 = 1;
	long s = 0;
	long tm1 = 0;
	long t = 1;
	for (;;) {
	    long q = rm1/r;
	    long rp1 = rm1 - q*r;
	    long sp1 = sm1 - q*s;
	    long tp1 = tm1 - q*t;
	    if (rp1 == 0) break;
	    rm1 = r;
	    r = rp1;
	    sm1 = s;
	    s = sp1;
	    tm1 = t;
	    t = tp1;
	}
	if (swap) {
	    if (aNeg) t = -t;
	    if (bNeg) s = -s;
	    result[1] = s;
	    result[0] = t;
	} else {
	    if (aNeg) s = -s;
	    if (bNeg) t = -t;
	    result[0] = s;
	    result[1] = t;
	}
	return r;
    }
}

//  LocalWords:  exbundle eacute zout gcd sa zeroArgument
