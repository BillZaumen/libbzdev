import org.bzdev.math.*;
import org.bzdev.lang.MathOps;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.math.rv.UniformDoubleRV;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;

public class RootFinderTest {

    static long cubicDiscriminant(long a, long b, long c, long d) {
	long disc = 18*a*b*c*d - 4*b*b*b*d + b*b*c*c
	    - 4*a*c*c*c - 27*a*a*d*d;
	return disc;
    }

    static  long delta0(long a, long b, long c, long d) {
	return b*b-3*a*c;
    }


    static int numberOfCubicRoots(long a, long b, long c, long d) {
	if (a == 0 && b == 0 && c == 0) return -1;
	if (a == 0 && b == 0) return 1;
	long gcd;
	if (a == 0) {
	    long qa = b;
	    long qb = c;
	    long qc = d;
	    gcd = Math.abs(qa);
	    if (qb != 0) gcd = MathOps.gcd(gcd, Math.abs(qb));
	    if (qc != 0) gcd = MathOps.gcd(gcd, Math.abs(qc));
	    qa /= gcd;
	    qb /= gcd;
	    qc /= gcd;
	    long qdisc = qb*qb - 4*qa*qc;
	    if (qdisc < 0) return 0;
	    if (qdisc == 0) return 1;
	    if (qdisc > 0) return 2;
	}
	gcd = Math.abs(a);
	if (b != 0) gcd = MathOps.gcd(gcd,Math.abs(b));
	if (c != 0) gcd = MathOps.gcd(gcd,Math.abs(c));
	if (d != 0) gcd = MathOps.gcd(gcd,Math.abs(d));
	a /= gcd;
	b /= gcd;
	c /= gcd;
	d /= gcd;
	long disc = cubicDiscriminant(a, b, c, d);
	if (disc > 0) return 3;
	if (disc < 0) return 1;
	long delta0 = b*b-3*a*c;
	if (delta0 == 0) return 1;
	else return 2;
    }
    // really zeros & all real. args must be small in absolute value.
    static int numberOfQuarticRoots(long a, long b, long c, long d, long e) {
	if (a == 0) {
	    return numberOfCubicRoots(b, c, d, e);
	}
	long gcd = Math.abs(a);
	if (b != 0) gcd = MathOps.gcd(gcd,Math.abs(b));
	if (c != 0) gcd = MathOps.gcd(gcd,Math.abs(c));
	if (d != 0) gcd = MathOps.gcd(gcd,Math.abs(d));
	if (e != 0) gcd = MathOps.gcd(gcd,Math.abs(e));
	a /= gcd;
	b /= gcd;
	c /= gcd;
	d /= gcd;
	e /= gcd;
	long Delta = 256*a*a*a*e*e*e - 192*a*a*b*d*e*e - 128*a*a*c*c*e*e
	    + 144*a*a*c*d*d*e - 27*a*a*d*d*d*d
	    + 144*a*b*b*c*e*e - 6*a*b*b*d*d*e - 80*a*b*c*c*d*e
	    + 18*a*b*c*d*d*d + 16*a*c*c*c*c*e
	    - 4*a*c*c*c*d*d - 27*b*b*b*b*e*e + 18*b*b*b*c*d*e
	    - 4*b*b*b*d*d*d - 4*b*b*c*c*c*e + b*b*c*c*d*d;

	long P = 8*a*c - 3*b*b;
	long R = b*b*b +  8*d*a*a - 4*a*b*c;
	long Delta0 = c*c - 3*b*d + 12*a*e;

	long D = 64*a*a*a*e - 16*a*a*c*c + 16*a*b*b*c - 16*a*a*b*d
	    - 3*b*b*b*b;

	/*
	System.out.println("    Delta = " + Delta);
	System.out.println("    P = " + P);
	System.out.println("    R = " + R);
	System.out.println("    Delta0 = " + Delta0);
	System.out.println("    D = " + D);
	*/

	boolean trace = false;

	if (Delta < 0) {
	    if (trace) System.out.println("... case 1");
	    return 2;
	} else if (Delta > 0) {
	    if (trace) System.out.println("... case 2");
	    if (P < 0 && D < 0) {
		if (trace) System.out.println("... case 3");
		return 4;
	    }
	    if (P > 0 || D > 0) {
		if (trace) System.out.println("... case 4");
		return 0;
	    }
	} else if (Delta == 0) {
	    if (P < 0 && D < 0 && Delta0 != 0) {
		if (trace) System.out.println("... case 5");
		return 3;
	    }
	    if ((D > 0) || ((P > 0) && (D != 0 || R != 0))) {
		if (trace) System.out.println("... case 6");
		return 1;
	    }
	    if (Delta0 == 0 && D != 0) {
		if (trace) System.out.println("... case 7");
		return 2;
	    }
	    if (D == 0) {
		if (P < 0) {
		    if (trace) System.out.println("... case 8");
		    return 2;
		}
		if (Delta0 == 0) {
		    if (trace) System.out.println("... case 9");
		    return 1;
		}
		if (trace) System.out.println("... case 10");
		return 0;
	    }
	}
	System.out.println("Delta = " + Delta);
	System.out.println("P = " + P);
	System.out.println("R = " + R);
	System.out.println("Delta0 = " + Delta0);
	System.out.println("D = " + D);
	return -1;		// should not happen
    }


    static boolean checkRoot(double[] eqn, double r) {
	Adder adder = new Adder.Kahan();
	double sum = 0.0;
	double prod = 1.0;
	double lim = 0.0;
	for (double coefficient: eqn) {
	    double t1 = Math.ulp(coefficient);
	    double t2 = Math.ulp(prod);
	    double t = t1*t1*prod*prod + t2*t2*coefficient*coefficient;
	    lim += t;
	    adder.add(coefficient*prod);
	    prod *= r;
	}
	lim = Math.sqrt(lim);
	lim = Math.scalb(lim, 7);
	if (Math.abs(adder.getSum()) > lim) {
		System.out.println(" ......... lim = " + lim
				   + ", sum = " + adder.getSum());
	}
	return (Math.abs(adder.getSum()) <= lim);
    }

    static double valueAt(double[] eqn, double r) {
	Adder adder = new Adder.Kahan();
	double prod = 1.0;
	for (double coefficient: eqn) {
	    adder.add(coefficient*prod);
	    prod *= r;
	}
	return adder.getSum();
    }

    private static final long SAFE_QUARTIC_LIMIT = 426;
    private static long CUBIC_SAFE_ILIMIT = 23355;

    public static void testOverflow() throws Exception{
	// Test these independently because the GCD computations
	// make it hard to generate a good test case.
	if ((1L << 63) > 0) {
	    throw new Exception("(1L << 63) > 0");
	}
	long a = (1L<<30)-1;
	double aa = a;
	int log2 = 2 + (64 - Long.numberOfLeadingZeros(a - 1))
	    + (64 - Long.numberOfLeadingZeros(a - 1));
	if (log2 < 62) throw new Exception("log2 too small");
	if (5*a*a < 0) throw new Exception("bad limit");
	if (Math.abs(5*a*a - (5*aa*aa)) > 0) {
	    throw new Exception("long and double arithmetic differ");
	}
	a = 23355-1;
	aa = a;
	int log2a = 2 + (64 - Long.numberOfLeadingZeros(a-1))
	    + 3 * (64 - Long.numberOfLeadingZeros(a-1));
	int log2b =  5 + 2 * (64 - Long.numberOfLeadingZeros(a-1))
	    + 2 * (64 - Long.numberOfLeadingZeros(a -1));
	log2 = Math.max(log2a, log2b);
	if (log2 < 62) throw new Exception("log2 too small");
	if ((4+27)*a*a*a*a < 0) throw new Exception("bad limit");
	if (Math.abs(31*a*a*a*a - (31*aa*aa*aa*aa)) > 0) {
	    throw new Exception("long and double arithmetic differ");
	}


	a = 426;
	aa = a;
	log2 = 8 + 6*(64 - Long.numberOfLeadingZeros(a-1));
	if (log2 < 60) throw new Exception("log2 too small");
	long f = 256 + 128 + 144 + 27 + 16 + 4;
	if ((f)*a*a*a*a*a*a < 0)
	    throw new Exception("bad limit");
	if (Math.abs(f*a*a*a*a*a*a - (f*aa*aa*aa*aa*aa*aa)) > 0) {
	    throw new Exception("long and double arithmetic differ");
	}
    }

    public static void testBezier() throws Exception {

	double[] badCase1 = {
	    400.0,
	    4666.666666666667,
	    8933.333333333334,
	    13200.0
	};
	double[] bcRoots = new double[3];
	double[] badCase1m = Polynomials.fromBezier(badCase1, 0, 3);
	double[] bcRootsm = new double[3];
	int nbcRoots = RootFinder.solveBezier(badCase1, 0, 3, bcRoots);
	int nbcRootsm = RootFinder.solvePolynomial(badCase1m, 1, bcRootsm);
	int imin = 0;
	int imax = nbcRootsm;
	for (int i = 0; i < nbcRootsm; i++) {
	    if (bcRootsm[i] < 0.0) imin++;
	    if (bcRootsm[i] > 1.0) imax--;
	}

	if (nbcRoots != imax-imin) {
	    System.out.println("nbcRoots = " + nbcRoots
			       + ", imax-imin = " + (imax-imin));
	    throw new Exception();
	}
	for (int i = 0; i < nbcRoots; i++) {
	    if (Math.abs(bcRoots[i] - bcRootsm[i+imin]) > 1.e-10) {
		throw new Exception();
	    }
	}

	double[] linear = {-2.0, 5.0};
	double[] linearB = new double[linear.length];
	Polynomials.toBezier(linearB, 0, linear, 1);
	double[] linroot1 = new double[1];
	double[] linroot2 = new double[1];
	int nrlin1 = RootFinder.solvePolynomial(linear, 1, linroot1);
	int nrlin2 = RootFinder.solveBezier(linearB, 0, 1, linroot2);
	if (nrlin1 != nrlin2) throw new Exception("");
	for (int i = 0; i < nrlin1; i++) {
	    if (Math.abs(linroot1[i] - Math.abs(linroot2[i])) > 1.e-10) {
		throw new Exception();
	    }
	}

	double[] factor = new double[2];
	double[] polynomial = new double[10];
	double[] bpolynomial = new double[10];
	double[] eroots = new double[10];
	double[] roots = new double[10];
	int degree;
	int enr;
	int nr;

	// list of the numerators for cases that failed, so
	// we can reproduce these for a test.
	int numlist[][] = {
	    {5, 10, 1, 9, 9, 10, 8, 3, 1},
	    {8, 9, 10, 7, 6, 0},
	    {9, 4, 1, 6, 10, 1, 6, 9},
	    {4, 9, 2, 8, 9, 3, 10, 6, 6}
	};
	int ccount = 0;
	for (int[] numerators: numlist) {
	    System.out.println("Specific Roots Case " + (++ccount));
	    polynomial[0] = 1.0;
	    int deg = 0;
	    TreeSet<Integer> nums = new TreeSet<>();
	    for (int i = 0; i < numerators.length; i++) {
		int num = numerators[i];
		if (num >= 0 && num <= 10) {
		    nums.add(num);
		}
		factor[0] = -num/10.0;
		factor[1] = 1;
		deg = Polynomials.multiply(polynomial, polynomial, deg,
					   factor, 1);
	    }
	    degree = deg;
	    enr = 0;
	    for (Integer num: nums) {
		eroots[enr++] = num / 10.0;
	    }
	    double[] roots2 = new double[degree];

	    Polynomials.toBezier(bpolynomial, 0, polynomial, degree);
	    nr = RootFinder.solveBezier(bpolynomial, 0, degree, roots);

	    double[] inverse = Polynomials.fromBezier(bpolynomial, 0, degree);
	    int onr = RootFinder.solvePolynomial(polynomial, degree, roots2);
	    int start = 0;
	    int cnt = onr;
	    for (int k = 0; k < onr; k++) {
		if (roots2[k] < -1.e-12) {
		    start++;
		    cnt--;
		} else if (roots2[k] > (1.0 + 1.e-12)) {
		    cnt--;
		}
	    }
	    onr = cnt;
	    if (onr != enr) {
		// bad case - converting to a Bernstein basis and
		// back gives a different number of roots.
		continue;
	    }
	    if (nr != enr) {
		System.out.println("nr = " + nr);
		System.out.println("enr = " + enr);
		for (int p = 0; p <= degree; p++) {
		    System.out.println("poly[" + p + "] = " + polynomial[p]);
		}
		for (int k = 0; k < enr; k++) {
		    System.out.println("eroots[" + k + "] = "
				       + eroots[k]);
		}
		for (int k = 0; k < nr; k++) {
		    System.out.println("roots[" + k + "] = " + roots[k]);
		}
		for (int p = 0; p < onr; p++) {
		    System.out.println("originalRoots[" + p + "] = "
				       + roots2[p]);
		}
		System.out.print("nums:");
		for (int num: numerators) {
		    System.out.print(" " +num);
		}
		System.out.println();
		throw new Exception();
	    }
	    for (int k = 0; k < nr; k++) {
		double err = 256*Math.abs(eroots[k] - roots2[k]);
		if (err < 1.e-11) err = 1.e-11;
		if (Math.abs(eroots[k] - roots[k]) > err) {
		    System.out.println("k = " + k + ", diff = "
				       + Math.abs(eroots[k] - roots[k])
				       + ", err = " + err);
		    for (int p = 0; p <= degree; p++) {
			System.out.println("poly[" + p + "] = "
					   + polynomial[p]);
		    }

		    for (int p = 0; p < enr; p++) {
			System.out.println("eroots[" + p + "] = " + eroots[p]);
		    }
		    for (int p = 0; p < nr; p++) {
			System.out.println("roots[" + p + "] = "
					   + roots[p]);
		    }
		    for (int p = 0; p < onr; p++) {
			System.out.println("originalRoots[" + p + "] = "
					   + roots2[p]);
		    }
		    System.out.print("nums:");
		    for (int num: numerators) {
			System.out.print(" " +num);
		    }
		    System.out.println();
		    throw new Exception();
		}
	    }
	}

	double[][] polynomials = {
	    {-4.0, -1.0, 5.0},
	    {4.0, -9.0, 5.0},
	    {0.012096000000000003, -0.21585600000000005,
	     1.22532, -3.1036, 3.485, -0.69, -1.7, 1.0},
	    {0.0, 0.072, 0.564, -1.6239999999999999,
	     -0.23199999999999976, 3.7199999999999998,
	     -3.5, 1.0},
	    {-0.008279040000000001, 0.09514624000000002,
	     -0.2800832, -0.3113839999999999, 2.4230000000000005,
	     -2.769400000000001, -1.6990000000000025, 5.65, -4.1, 1.0}
	};
	int count = 0;
	for (double[] poly: polynomials) {
	    System.out.println("Case "  + (++count));
	    double[] bpoly = new double[poly.length];
	    double[] roots2 = new double[poly.length];
	    Polynomials.toBezier(bpoly, 0, poly, poly.length-1);
	    enr = RootFinder.solvePolynomial(poly, poly.length-1, eroots);
	    int off = 0;
	    int enr1 = 0;
	    for (int i = 0; i < enr; i++) {
		if (eroots[i] < 0.0) off++;
		else if (eroots[i] <= 1.0) enr1++;
	    }
	    enr = enr1;
	    System.arraycopy(eroots, off, eroots, 0, enr);

	    nr = RootFinder.solveBezier(bpoly, 0, poly.length-1, roots);
	    if (nr != enr) {
		System.out.println("nr = " + nr);
		System.out.println("enr = " + enr);
		double[] inverse =
		    Polynomials.fromBezier(bpoly, 0, poly.length-1);
		int inr = RootFinder.solvePolynomial(inverse, poly.length-1,
						     roots2);
		System.out.println("inr = " + inr);
		for (int k = 0; k < enr; k++) {
		    System.out.println("eroots[" + k + "] = " + eroots[k]);
		}
		for (int k = 0; k < nr; k++) {
		    System.out.println("roots[" + k + "] = " + roots[k]);
		}
		for (int k = 0; k < inr; k++) {
		    System.out.println("iroots[" + k + "] = " + roots2[k]);
		}
		if (enr == inr) {
		    // enr != inr, round-off errors are affecting the
		    // roots enough for the test to be doubtful.
		    throw new Exception();
		} else {
		    continue;
		}
	    }

	    for (int k = 0; k < nr; k++) {
		if (Math.abs(eroots[k] - roots[k]) > 1.e-12) {
		    System.out.println("k = " + k + ", diff = "
				       + Math.abs(eroots[k] - roots[k]));
		    for (int p = 0; p < enr; p++) {
			System.out.println("eroots[" + p + "] = " + eroots[p]);
		    }
		    for (int p = 0; p < nr; p++) {
			System.out.println("roots[" + p + "] = " + roots[p]);
		    }
		    throw new Exception();
		}
	    }
	}
	System.out.println("finished initial Bezier cases");

	for (int i = -10; i <= 10 ; i++) {
	    for (int j = -10; j <= 10; j++) {
		enr = 0;
		polynomial[0] = i;
		polynomial[1] = 5;
		eroots[enr]  = -i/5.0;
		if (eroots[enr] >= 0.0 && eroots[enr] <= 1.0) enr++;
		factor[0] = j;
		factor[1] = 5;
		if (i != j) {
		    eroots[enr] = -j/5.0;
		    if (eroots[enr] >= 0.0 && eroots[enr] <= 1.0) enr++;
		}
		if (enr == 2) {
		    if (eroots[0] > eroots[1]) {
			double tmp = eroots[0];
			eroots[0] = eroots[1];
			eroots[1] = tmp;
		    }
		}
		degree = Polynomials.multiply(polynomial,
					      polynomial, 1, factor, 1);
		Polynomials.toBezier(bpolynomial, 0, polynomial, degree);
		nr = RootFinder.solveBezier(bpolynomial, 0, degree, roots);
		if (nr != enr) {
		    double rootsm[] = new double[roots.length];
		    int nrm = RootFinder.solvePolynomial(polynomial, degree,
							 rootsm);
		    System.out.println("nr = " + nr);
		    System.out.println("enr = " + enr);
		    System.out.println("nrm = " + nrm);
		    for (int k = 0; k < enr; k++) {
			System.out.println("eroots[" + k + "] = " + eroots[k]);
		    }
		    for (int k = 0; k < nr; k++) {
			System.out.println("roots[" + k + "] = " + roots[k]);
		    }
		    throw new Exception();
		}
		for (int k = 0; k < nr; k++) {
		    if (Math.abs(eroots[k] - roots[k]) > 1.e-14) {
			System.out.println("i = " + i + ", j = " + j);
			for (int p = 0; p <= degree; p++) {
			    System.out.println("poly[" + p + "] = "
					       + polynomial[p]);
			}

			for (int p = 0; p < enr; p++) {
			    System.out.println("eroots[" + p + "] = "
					       + eroots[p]);
			}
			for (int p = 0; p < nr; p++) {
			    System.out.println("roots[" + p + "] = "
					       + roots[p]);
			}
			int onr = RootFinder.solvePolynomial(polynomial, degree,
							     roots);
			for (int p = 0; p < onr; p++) {
			    System.out.println("originalRoots[" + p + "] = "
					       + roots[p]);
			}
			throw new Exception();
		    }
		}
	    }
	}
	for (int i = -10; i <= 10 ; i++) {
	    for (int j = -10; j <= 10; j++) {
		for (int l = -10; l <= 10; l++) {
		    enr = 0;
		    polynomial[0] = i;
		    polynomial[1] = 5;
		    eroots[enr]  = -i/5.0;
		    if (eroots[enr] >= 0.0 && eroots[enr] <= 1.0) enr++;
		    factor[0] = j;
		    factor[1] = 5;
		    if (i != j) {
			eroots[enr] = -j/5.0;
			if (eroots[enr] >= 0.0 && eroots[enr] <= 1.0) enr++;
		    }
		    degree = Polynomials.multiply(polynomial,
						  polynomial, 1, factor, 1);
		    factor[0] = l;
		    factor[1] = 5;
		    if (i != l && j != l) {
			eroots[enr] = -l/5.0;
			if (eroots[enr] >= 0.0 && eroots[enr] <= 1.0) enr++;
		    }
		    degree = Polynomials.multiply(polynomial,
						  polynomial, degree,
						  factor, 1);
		    double[] roots2 = new double[polynomial.length];
		    if (enr == 2) {
			if (eroots[0] > eroots[1]) {
			    double tmp = eroots[0];
			    eroots[0] = eroots[1];
			    eroots[1] = tmp;
			}
		    } else if (enr == 3) {
			Arrays.sort(eroots, 0, enr);
		    }

		    Polynomials.toBezier(bpolynomial, 0, polynomial, degree);
		    nr = RootFinder.solveBezier(bpolynomial, 0, degree, roots);
		    double[] inverse =
			Polynomials.fromBezier(bpolynomial, 0, degree);
		    int onr = RootFinder.solvePolynomial(polynomial, degree,
							 roots2);
		    int start = 0;
		    int cnt = onr;
		    for (int k = 0; k < onr; k++) {
			if (roots2[k] < -1.e-12) {
			    start++;
			    cnt--;
			} else if (roots2[k] > (1.0 + 1.e-12)) {
			    cnt--;
			}
		    }
		    onr = cnt;
		    if (onr != enr) {
			// bad case - converting to a Bernstein basis and
			// back gives a different number of roots.
			continue;
		    }
		    System.arraycopy(roots2, start, roots2, 0, onr);
		    if (nr != enr) {
			System.out.println("nr = " + nr);
			System.out.println("enr = " + enr);
			for (int k = 0; k < enr; k++) {
			    System.out.println("eroots[" + k + "] = "
					       + eroots[k]);
			}
			for (int k = 0; k < nr; k++) {
			    System.out.println("roots[" + k + "] = "
					       + roots[k]);
			}
			throw new Exception();
		    }
		    for (int k = 0; k < nr; k++) {
			double err = Math.abs(eroots[k] - roots2[k]);
			if (err < 1.e-12) err = 1.e-12;
			if (Math.abs(eroots[k] - roots[k]) > err) {
			    System.out.println("i = " + i + ", j = " + j);
			    for (int p = 0; p <= degree; p++) {
				System.out.println("poly[" + p + "] = "
						   + polynomial[p]);
			    }

			    for (int p = 0; p < enr; p++) {
				System.out.println("eroots[" + p + "] = "
						   + eroots[p]);
			    }
			    for (int p = 0; p < nr; p++) {
				System.out.println("roots[" + p + "] = "
						   + roots[p]);
			    }
			    for (int p = 0; p < onr; p++) {
				System.out.println("originalRoots[" + p + "] = "
						   + roots2[p]);
			    }
			    throw new Exception();
			}
		    }
		}
	    }
	}

	UniformIntegerRV degreeRV = new UniformIntegerRV(1, true, 10, false);
	UniformIntegerRV numRV = new UniformIntegerRV(-15, true, 15, true);

	for (int i = 0; i < 1000000; i++) {
	    degree = degreeRV.next();
	    polynomial[0] = 1.0;
	    int deg = 0;
	    TreeSet<Integer> nums = new TreeSet<>();
	    LinkedList<Integer> allNums = new LinkedList<>();

	    for (int j = 1 ; j <= degree; j++) {
		int num = numRV.next();
		allNums.add(num);
		if (num >= 0 && num <= 10) {
		    nums.add(num);
		}
		factor[0] = -num/10.0;
		factor[1] = 1;
	        deg = Polynomials.multiply(polynomial, polynomial, deg,
					   factor, 1);
	    }
	    degree = deg;
	    enr = 0;
	    for (Integer num: nums) {
		eroots[enr++] = num / 10.0;
	    }
	    double[] roots2 = new double[degree];

	    Polynomials.toBezier(bpolynomial, 0, polynomial, degree);
	    nr = RootFinder.solveBezier(bpolynomial, 0, degree, roots);

	    double[] inverse = Polynomials.fromBezier(bpolynomial, 0, degree);
	    int onr = RootFinder.solvePolynomial(polynomial, degree, roots2);
	    int start = 0;
	    int cnt = onr;
	    for (int k = 0; i < onr; i++) {
		if (roots2[k] < -1.e-12) {
		    start++;
		    cnt--;
		} else if (roots2[k] > (1.0 + 1.e-12)) {
		    cnt--;
		}
	    }
	    onr = cnt;
	    if (onr != enr) {
		// bad case - converting to a Bernstein basis and
		// back gives a different number of roots.
		continue;
	    }
	    if (nr != enr) {
		System.out.println("nr = " + nr);
		System.out.println("enr = " + enr);
		for (int p = 0; p <= degree; p++) {
		    System.out.println("poly[" + p + "] = " + polynomial[p]);
		}
		for (int k = 0; k < enr; k++) {
		    System.out.println("eroots[" + k + "] = "
				       + eroots[k]);
		}
		for (int k = 0; k < nr; k++) {
		    System.out.println("roots[" + k + "] = " + roots[k]);
		}
		for (int p = 0; p < onr; p++) {
		    System.out.println("originalRoots[" + p + "] = "
				       + roots2[p]);
		}
		System.out.print("nums:");
		for (int num: allNums) {
		    System.out.print(" " +num);
		}
		System.out.println();
		throw new Exception();
	    }
	    for (int k = 0; k < nr; k++) {
		double err = 256*Math.abs(eroots[k] - roots2[k]);
		if (err < 1.e-11) err = 1.e-11;
		if (Math.abs(eroots[k] - roots[k]) > err) {
		    System.out.println("k = " + k + ", diff = "
				       + Math.abs(eroots[k] - roots[k])
				       + ", err = " + err);
		    for (int p = 0; p <= degree; p++) {
			System.out.println("poly[" + p + "] = "
					   + polynomial[p]);
		    }

		    for (int p = 0; p < enr; p++) {
			System.out.println("eroots[" + p + "] = " + eroots[p]);
		    }
		    for (int p = 0; p < nr; p++) {
			System.out.println("roots[" + p + "] = "
					   + roots[p]);
		    }
		    for (int p = 0; p < onr; p++) {
			System.out.println("originalRoots[" + p + "] = "
					   + roots2[p]);
		    }
		    System.out.print("nums:");
		    for (int num: allNums) {
			System.out.print(" " +num);
		    }
		    System.out.println();
		    throw new Exception();
		}
	    }
	}
    }


    public static void main(String argv[]) throws Exception {

	testOverflow();

	testBezier();

	double[] res1 = new double[4];
	double[] res2 = new double[4];


	double[] testeqn = new double[11];
	double[] testres = new double[10];
	int ncroots;
	try {
	    int expectedNcroots;
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -10.0;
	    testeqn[1] = -10.0;
	    testeqn[2] = -10.0;
	    testeqn[3] = -10.0;
	    testeqn[4] = -3.0;
	    ncroots = RootFinder.solvePolynomial(testeqn, 4, testres);
	    System.out.println("ncroots = " + ncroots);
	    expectedNcroots = numberOfQuarticRoots(-10,-10,-10,-10,-3);
	    if (ncroots != expectedNcroots) {
		throw new Exception(ncroots + " != " + expectedNcroots);
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -1000000.0;
	    testeqn[1] = -1000000.0;
	    testeqn[2] = 0.0;
	    testeqn[3] = 900000.0;
	    ncroots = RootFinder.solvePolynomial(testeqn, 3, testres);
	    System.out.println("ncroots = " + ncroots);
	    expectedNcroots = numberOfCubicRoots(900000, 0, -1000000, -1000000);
	    if (ncroots != expectedNcroots) {
		throw new Exception(ncroots + " != " + expectedNcroots);
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -10;
	    testeqn[1] = 0;
	    testeqn[2] = -10;
	    testeqn[3] = -10;
	    testeqn[4] = -10;
	    testeqn[5] = -9;
	    ncroots = RootFinder.solvePolynomial(testeqn, 5, testres);
	    System.out.println("ncroots = " + ncroots);
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -7;
	    testeqn[1] = -1;
	    testeqn[2] = 3;
	    testeqn[3] = -4;
	    testeqn[4] = 2;
	    ncroots = RootFinder.solvePolynomial(testeqn, 4, testres);
	    System.out.println("ncroots = " + ncroots);
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -10.0;
	    testeqn[1] = -1.0;
	    testeqn[2] = 0.0;
	    testeqn[3] = -10.0;
	    ncroots = RootFinder.solvePolynomial(testeqn, 3, testres);
	    System.out.println("ncroots = " + ncroots);
	    for (int i = 0; i < ncroots; i++) {
		if (checkRoot(testeqn, testres[i]) == false) {
		    System.out.println("index " + i + ", root " + testres[i]
				       +": value at root = "
				       + valueAt(testeqn, testres[i]));
		    throw new Exception("bad root");
		} else {
		    System.out.println(" root " + testres[i] + ", value "
				       + valueAt(testeqn, testres[i]));
		}
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -10.0;
	    testeqn[1] = -10.0;
	    testeqn[2] = -10.0;
	    testeqn[3] = -10.0;
	    testeqn[4] = -9.0;
	    ncroots = RootFinder.solvePolynomial(testeqn, 4, testres);
	    System.out.println("ncroots = " + ncroots);
	    for (int i = 0; i < ncroots; i++) {
		if (checkRoot(testeqn, testres[i]) == false) {
		    System.out.println("index " + i + ", root " + testres[i]
				       +": value at root = "
				       + valueAt(testeqn, testres[i]));
		    throw new Exception("bad root");
		} else {
		    System.out.println(" root " + testres[i] + ", value "
				       + valueAt(testeqn, testres[i]));
		}
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    // These caused a failure in some previous test.
	    double teqn[] = {
		(double)0,
		(double)(-10), (double)(-10),
		(double)(-10), (double)(-10),
		(double)(-3)};
	    testeqn[0] = 0.0;
	    testeqn[1] = -10.0;
	    testeqn[2] = -10.0;
	    testeqn[3] = -10.0;
	    testeqn[4] = -10.0;
	    testeqn[5] = -3.0;
	    ncroots = RootFinder.solvePolynomial(teqn, 5, testres);
	    System.out.println("ncroots = " + ncroots);
	    for (int i = 0; i < ncroots; i++) {
		if (checkRoot(testeqn, testres[i]) == false) {
		    System.out.println("index " + i + ", root " + testres[i]
				       +": value at root = "
				       + valueAt(testeqn, testres[i]));
		    throw new Exception("bad root");
		} else {
		    System.out.println(" root " + testres[i] + ", value "
				       + valueAt(testeqn, testres[i]));
		}
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -1.4171246775459796E16;
	    testeqn[1] = 3.513650618500768E11;
	    testeqn[2] = -2903928.1581868273;
	    ncroots = RootFinder.solvePolynomial(testeqn, 3, testres);
	    for (int i = 0; i < ncroots; i++) {
		if (checkRoot(testeqn, testres[i]) == false) {
		    System.out.println("index " + i + ", root " + testres[i]
				       +": value at root = "
				       + valueAt(testeqn, testres[i]));
		    throw new Exception("bad root");
		} else {
		    System.out.println(" root " + testres[i] + ", value "
				       + valueAt(testeqn, testres[i]));
		}
	    }
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -69.40488535996082;
	    testeqn[1] = 422.35940724925354;
	    testeqn[2] = 540.5092264822232;
	    testeqn[3] = 764.3674641482926;
	    testeqn[4] = 2649.525675601257;
	    testeqn[5] = 4097.8155446426845;
	    testeqn[6] = -5.652226533981207;
	    RootFinder.solvePolynomial(testeqn, 6, testres);
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -10.0;
	    testeqn[1] = -10.0;
	    testeqn[2] = -9.0;
	    testeqn[3] = -7.0;
	    testeqn[4] = 0.0;
	    testeqn[5] = 1.0;
	    RootFinder.solvePolynomial(testeqn, 6, testres);
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = 341.1336249906967;
	    testeqn[1] = 3134.4654148877507;
	    testeqn[2] = -34893.7470153787;
	    testeqn[3] = 44103.14083358347;
	    testeqn[4] = -150181.33668646173;
	    testeqn[5] = 26486.59415434386;
	    testeqn[6] = -401115.7665357522;
	    RootFinder.solvePolynomial(testeqn, 6, testres);
	    System.out.println("---");
	    Arrays.fill(testeqn, 0.0);
	    testeqn[0] = -405.41990342884134;
	    testeqn[1] = 1933.6008048097553;
	    testeqn[2] = 3535.9688450331273;
	    testeqn[3] = 9048.567535677628;
	    testeqn[4] = 20567.03736716688;
	    testeqn[5] = 18.180664199551984;
	    RootFinder.solvePolynomial(testeqn, 5, testres);
	    System.out.println("---");
	    Arrays.fill (testeqn, 0.0);
	    testeqn[0] = 148.31269120811385;
	    testeqn[1] = 69.25633646201769;
	    testeqn[2] = -131.45232141494625;
	    testeqn[3] = 335.80362881088547;
	    testeqn[4] = 23.247416896840747;
	    testeqn[5] = 1364.8318595103747;
	    testeqn[6] = -2201.477375188818;
	    testeqn[7] = -77.52076808675474;
	    testeqn[8] = 7155.636366307679;
	    int nroots = RootFinder.solvePolynomial(testeqn, 8, testres);
	    if (nroots != 2) {
		for (int i = 0; i < nroots; i++) {
		    System.out.println("found root " + testres[i]);
		}
		throw new Exception("should have two real roots, not " +nroots);
	    }
	    System.out.println("---");
	    Arrays.fill (testeqn, 0.0);
	    testeqn[0] = -61.8659409100642;
	    testeqn[1] = 148.31269120811385;
	    testeqn[2] = 34.62816823100884;
	    testeqn[3] = -43.81744047164875;
	    testeqn[4] = 83.95090720272137;
	    testeqn[5] = 4.649483379368149;
	    testeqn[6] = 227.47197658506246;
	    testeqn[7] = -314.4967678841168 ;
	    testeqn[8] = -9.690096010844343;
	    testeqn[9] = 795.0707073675198;

	    nroots = RootFinder.solvePolynomial(testeqn, 9, testres);
	    if (nroots <= 0)
		throw new Exception("should have at least one real root");
	    System.out.println("---");
	    Arrays.fill (testeqn, 0.0);
	    testeqn[0] = 83.49541951281307;
	    testeqn[1] = -61.8659409100642;
	    testeqn[2] = 74.15634560405692;
	    testeqn[3] = 11.542722743669614;
	    testeqn[4] = -10.954360117912188;
	    testeqn[5] = 16.790181440544274;
	    testeqn[6] = 0.7749138965613582;
	    testeqn[7] = 32.49599665500892;
	    testeqn[8] = -39.3120959855146;
	    testeqn[9] = -1.0766773345382603;
	    testeqn[10] = 79.50707073675198;
	    RootFinder.solvePolynomial(testeqn, 10, testres);
	    System.out.println("---");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	System.out.println("Quadratic polynomials");

	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    double eqn[] = {(double)i, (double) j, (double) k};
		    int i1 = RootFinder.solveQuadratic(eqn, res1);
		    int i2 = QuadCurve2D.solveQuadratic(eqn, res2);
		    if (i2 > 0) {
			Arrays.sort(res2, 0, i2);
		    }
		    // eliminate duplicates
		    if (i2 == 2 && res2[0] == res2[1]) i2--;
		    if (i1 != i2) {
			throw new Exception("i1 = " + i1 + ", i2 = " + i2);
		    }
		    for (int n = 0; n < i1; n++) {
			if (Math.abs(res1[n] - res2[n]) > 1.e-15) {
			    for (int m = 0; m < 3; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    if (Math.abs(valueAt(eqn, res1[n])) >
				Math.abs(valueAt(eqn, res2[n]))) {
				throw new Exception("roots don't match");
			    } else {
				for (int mm = 0; mm < i1; mm++) {
				    System.out.println("res1[" + mm + "] = "
						       + res1[mm]
						       + ", value = "
						       +valueAt(eqn, res1[mm]));
				}
				for (int mm = 0; mm < i1; mm++) {
				    System.out.println("res2[" + mm + "] = "
						       + res2[mm]
						       + ", value = "
						       +valueAt(eqn, res2[mm]));
				}
				System.out.println("roots differ");
				break;
			    }
			}
		    }
		}
	    }
	}


	double cubicTests[][] = {
	    {-3.61E-4, 0.14459999999999962, -14.52, 8.0},
	    {-1.2089816958161867E-4, 0.06973379629629672,
	     -10.083333333333334, 8.0},
	};
	double cubicRes[] = new double[4];
	System.out.println("check borderline cubic cases");

	int kc = 0;
	for (double[] cubicTest: cubicTests) {
	    System.out.println("Case " + (++kc));
	    int nct = RootFinder.solveCubic(cubicTest, cubicRes);
	    for (int i = 0; i < nct; i++) {
		System.out.println("cubicRes[" + i + "] = " + cubicRes[i]
				   + ", value = "
				   + valueAt(cubicTest, cubicRes[i]));
	    }
	}
	System.out.println(".... continue");



	double[][] quads = {{5.0, 3.0, 2.0},
			    {3.0, 1.0, 10.0},
			    {3.0, -6.0, 3.0},
			    {10.0, 0.0, 1.0},
			    {3.0, 10.0, 0.0},
			    {-2.0, -10.0, -10.0},
			    {-10.0, 7.0, 1.0}
	};

	System.out.println("Quadratic polynomials - specific cases");

	for (double[] eqn: quads) {
	    int i1 = RootFinder.solveQuadratic(eqn, res1);
	    int i2 = QuadCurve2D.solveQuadratic(eqn, res2);
	    Arrays.sort(res2, 0, i2);
	    // eliminate duplicates
	    if (i2 == 2 && res2[0] == res2[1]) i2--;
	    if (i1 != i2) {
		System.out.println("i1 = " + i1 + ", i2 = " + i2);
		System.out.println("eqn[0] = " + eqn[0]);
		System.out.println("eqn[1] = " + eqn[1]);
		System.out.println("eqn[2] = " + eqn[2]);
		for (int i = 0; i < i1; i++) {
		    System.out.println("res1[i] =" + res1[i]);
		}
		for (int i = 0; i < i2; i++) {
		    System.out.println("res2[i] =" + res2[i]);
		}
		throw new Exception("i1 = " + i1 + ", i2 = " + i2);
	    }
	    for (int i = 0; i < i1; i++) {
		if (Math.abs(res1[i] - res2[i]) > 1.e-15
		    && (Math.abs(valueAt(eqn, res1[i]))
			> Math.abs(valueAt(eqn, res2[i])))) {
		    System.out.println("i1 = " + i1 + ", i2 = " + i2);
		    System.out.println("eqn[0] = " + eqn[0]);
		    System.out.println("eqn[1] = " + eqn[1]);
		    System.out.println("eqn[2] = " + eqn[2]);
		    for (int j = 0; j < i1; j++) {
			System.out.println("res1[" + j + "] = " + res1[j]
					   +", value = "
					   + valueAt(eqn, res1[j]));
		    }
		    for (int j = 0; j < i2; j++) {
			System.out.println("res2[" + j + "] = " + res2[j]
					   +", value = "
					   + valueAt(eqn, res2[j]));
		    }
		    throw new Exception("roots don't match");
		}
	    }
	}


	double[][] cubics = {{-6.0, 11.0, -6.0, 1.0},
			     {0.0, 0.0, 0.0, 1.0},
			     {1.0, 4.0, 0.0, 2.0},
			     {-8.0, 4.0, 1.0, 4.0},
			     {57.0, 0.0, 0.0, 4.0},
			     {0.0, 9.0, 0.0, 1.0},
			     {2.0, 5.0, -4.0, 1.0},
			     {-9.0, -10.0, 7.0, 8.0},
			     {-7.0, -9.0, 3.0, 5.0},
			     // CubicCurve2D.solveCubic fails for
			     // the following case
			     {-8.0, -10.0, 4.0, 6.0},
			     // CubicCurve2D.solveCubic fails for
			     // the following case
			     {-1.0, -5.0, -7.0, -3.0},
	};

	System.out.println("Cubic polynomials - specific cases");
	int sccnt = 0;
	for (double[] eqn: cubics) {
	    System.out.println("Specific Case " + (++sccnt));
	    int nr = numberOfCubicRoots(Math.round(eqn[3]),
					Math.round(eqn[2]),
					Math.round(eqn[1]),
					Math.round(eqn[0]));
	    int i1 = RootFinder.solveCubic(eqn, res1);
	    int i2 = CubicCurve2D.solveCubic(eqn, res2);
	    if (i2 > 0) {
		Arrays.sort(res2, 0, i2);
	    }
	    if (i2 == 3 && res2[0] == res2[1] && res2[1] == res2[2]) {
		i2 = 1;
	    }
	    if (i2 == 3 && res2[1] == res2[2]) i2--;
	    if (i2 == 3 && res2[0] == res2[1]) {
		res2[1] = res2[2];
		i2--;
	    }
	    if (i2 == 2 && res2[0] == res2[1]) i2--;
	    if (i1 != i2) {
		for (int j = 0; j < 4; j++) {
		    System.out.println("eqn[" + j + "] = " + eqn[j]);
		}
		for (int j = 0; j < i1; j++) {
		    System.out.println("res1[" + j + "] = " + res1[j]
				       + ", val = " + valueAt(eqn, res1[j]));
		}
		for (int j = 0; j < i2; j++) {
		    System.out.println("res2[" + j + "] = " + res2[j]
				       + ", val = " + valueAt(eqn, res2[j]));
		}
		boolean test1 = true;
		for (int j = 0; j < i1; j++) {
		    if (checkRoot(eqn, res1[j]) == false) {
			System.out.println("... checkRoot failed for "
					   + res1[j]);
			test1 = false;
		    }
		}
		boolean test2 = true;
		for (int j = 0; j < i2; j++) {
		    if (checkRoot(eqn, res2[j]) == false) {
			test2 = false;
		    }
		}
		if (test1 == false) {
		    System.out.println("test1 = " + test1);
		    System.out.println("test2 = " + test2);
		    throw new Exception("i1 = " + i1 + ", i2 = " + i2);
		}
	    } else {
		for (int i = 0; i < i1; i++) {
		    if (Math.abs(res1[i] - res2[i]) > 1.e-16) {
			for (int j = 0; j < 4; j++) {
			    System.out.println("eqn[" + j + "] = " + eqn[j]);
			}
			for (int j = 0; j < i1; j++) {
			    System.out.println("res1[" + j + "] = " + res1[j]
					       + ", " + valueAt(eqn, res1[j]));
			}
			for (int j = 0; j < i2; j++) {
			    System.out.println("res2[" + j + "] = " + res2[j]
					       + ", " + valueAt(eqn, res2[j]));
			}
			if (Math.abs(valueAt(eqn,res1[i]))
			    > Math.abs(valueAt(eqn,res2[i]))) {
			    throw new Exception("roots don't match");
			} else {
			    System.out.println
				("roots don't match: "
				 + Math.abs(valueAt(eqn,res1[i]))
				 + " <= "
				 + Math.abs(valueAt(eqn,res2[i])));
			}
		    }
		}
	    }
	}

	int nr4[] = {2, 0, 0, 2, 4, 4, 2, 4};
	double[][] quarts = {
	    // 2 roots - Delta < 0 but the bad values are split between
	    // the two quadratic-equation solutions. Code was added to
	    // eliminate spurious solutions.
	    {72.0581111757958, 35.79467407144418, 18.70882529726137,
	     -47.29136556123544, -0.04809305736725378},
	    // 0 roots
	    {-10.0, 0.0, 1.0, 0.0, -10.0},
	    // 0 roots
	    {-10.0, -2.0, 0.0, 2.0, -1.0},
	    // 2 roots
	    {-10.0, -10.0, -10.0, -8.0, 2.0},
	    // 4 roots
	    {-3.0, 12.0, -2.0, -4.0, 1.0},
	    // 4 roots
	    {6.0, -2.0, -12.0, 2.0, 1.0},
	    //2 roots
	    {-20.0, -36.0, 42.0, -20.0, 3.0},
	    // 4 roots
	    {-250.0, -480.0, -30.0, 80.0, 15.0}
	};

	System.out.println("Quartic polynomials - specific cases");

	double[] res4 = new double[4];
	for  (int i = 0; i < quarts.length; i++) {
	    System.out.println("Quartic case, i = " + i);
	    int inz = numberOfQuarticRoots(Math.round(quarts[i][4]),
					   Math.round(quarts[i][3]),
					   Math.round(quarts[i][2]),
					   Math.round(quarts[i][1]),
					   Math.round(quarts[i][0]));
	    int nz = RootFinder.solvePolynomial(quarts[i], 4, res4);
	    // int nz = RootFinder.solveQuartic(quarts[i], res4);
	    if (inz != nz && i != 0) {
		throw new Exception("inz != nz (" + inz + " != " + nz + ")");
	    }
	    if (nr4[i] != nz) {
		System.out.println("i = " + i + ": wrong number of zeros - "
				   + nz + "!= " + nr4[i]);
		throw new Exception("failed - wrong number of roots");
	    }
	    boolean failed = false;
	    for (int j = 0; j < nz; j++) {
		if (checkRoot(quarts[i], res4[j]) == false) {
		    failed = true;
		}
	    }
	    if (failed) {
		for (int j = 0; j < nz; j++) {
		    System.out.println("j = " + j
				       + ": value at " + res4[j] + " = "
				       + valueAt(quarts[i], res4[j]));
		}
		throw new Exception("failed - wrong root");
	    }
	}


	int nr5[] = {1, 5, 1, 3, 1, 1};
	double[][] quints = {
	    {-10.0, -10.0, 0.0, 0.0, 0.0, -10.0},
	    // 5 roots
	    {200.0, -250.0, -240.0, -10.0, 20.0, 3.0},
	    // 1 root
	    {10.0, 40.0, 10.0, -20.0, -5.0, 4.0},
	    //3 roots
	    {-300.0, 175.0, 120.0, -10.0, -20.0, 3.0},
	    // 1 root
	    {-56.0, 135.0, -180.0, 110.0, -30.0, 3.0},
	    // 1 root
	    {-4.0, 7.0, -10.0, -8.0, 1.0, 1.0}
	};

	System.out.println("Quintic polynomials - specific cases");

	double[] res5 = new double[5];
	for  (int i = 0; i < quints.length; i++) {
	    System.out.println("Quintic case " + i);
	    int nr = RootFinder.solvePolynomial(quints[i], 5, res5);
	    if (nr5[i] != nr ) {
		System.out.println("wrong number of zeros: "
				   + nr + " != " + nr5[i]);
		for (int j = 0; j < nr; j++) {
		    System.out.format("res[%d] = %g; f(%g) = %g\n",
				      j, res5[j],
				      res5[j], valueAt(quints[i], res5[j]));
		}
		throw new Exception("failed");
	    }
	    for (int j = 0; j < nr5[i]; j++) {
		if (checkRoot(quints[i], res5[j]) == false) {
		    System.out.println("j = " + j
				       + ": value at " + res5[j] + " = "
				       + valueAt(quints[i], res5[j]));
		    throw new Exception("failed - wrong root");
		}
	    }
	}


	System.out.println("Quartic polynomials");
	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			for (int n = -10; n <10; n++) {
			    double eqn[] = {
				(double)i, (double) j, (double) k, (double)l,
				(double)n
			    };
			    /*
			    System.out.println("New Quartic Case");
			    for (int q = 0; q < eqn.length; q++) {
				System.out.println
				    ("eqn[" + q + "] = " + eqn[q]);
			    }
			    */
			    int inz = numberOfQuarticRoots((long)n, (long)l,
							   (long)k, (long)j,
							   (long)i);

			    int nz = -2;
			    try {
				nz = RootFinder.solvePolynomial(eqn, 4, res4);
			    } catch (Exception e) {
				e.printStackTrace();
				System.out.println("eqn[0] = " + i);
				System.out.println("eqn[1] = " + j);
				System.out.println("eqn[2] = " + k);
				System.out.println("eqn[3] = " + l);
				System.out.println("eqn[4] = " + n);
				System.exit(1);
			    }
			    if (inz != nz) {
				for (int p = 0; p < eqn.length; p++) {
				    System.out.println("eqn[" + p + "] = "
						       + eqn[p]);
				}
				throw new Exception("inz != nz ("
						    + inz + " != " + nz + ")");
			    }

			    for (int p = 0; p < nz; p++) {
				if (checkRoot(eqn, res4[p]) == false) {
				    for (int q = 0; q < eqn.length; q++) {
					System.out.println
					    ("eqn[" + q + "] = " + eqn[q]);
				    }
				    System.out.println
					("p = " + p  + ": value at "
					 + res4[p] + " = "
					 + valueAt(eqn, res4[p]));
				    throw new Exception("failed - wrong root");
				}
			    }
			}
		    }
		}
	    }
	}

	System.out.println("Quintic Polynomials");

	int primes[] = {2, 4, 7};

	int percentDone4 = 0;
	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		if ((percentDone4 % 4) == 0) {
		    System.out.println("... " + ((percentDone4)/4)
				       + "% complete");
		}
		percentDone4++;
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			for (int n = -10; n <10; n++) {
			    for (int m = -10; m < 10; m++) {
				double eqn[] = {
				    (double)i, (double) j, (double) k,
				    (double)l, (double)n, (double) m
				};
				// The quintic tests are time consuming, so
				// prune out ones where P_1(x) = KP_2(x)
				// with K a nonzero integer.
				if (i != 0) {
				    boolean skip = false;
				    for (int prime: primes) {
					if ((Math.abs(i) % prime == 0)
					    && (Math.abs(j) % prime == 0)
					    && (Math.abs(k) % prime == 0)
					    && (Math.abs(l) % prime == 0)
					    && (Math.abs(n) % prime == 0)
					    && (Math.abs(m) % prime == 0)) {
					    skip = true;
					    break;
					}
				    }
				    if (skip) continue;
				} else if (j != 0) {
				    // This is the i = 0 case: the polynomial
				    // is actually a quartic.
				    boolean skip = false;
				    for (int prime: primes) {
					if ((Math.abs(j) % prime == 0)
					    && (Math.abs(k) % prime == 0)
					    && (Math.abs(l) % prime == 0)
					    && (Math.abs(n) % prime == 0)
					    && (Math.abs(m) % prime == 0)) {
					    skip = true;
					    break;
					}
				    }
				    if (skip) continue;
				}
				/*
				try {
				    RootFinder.solvePolynomial(eqn, 5, res5);
				} catch (Exception rfe) {
				    System.out.println("... eqn[0] = " + i);
				    System.out.println("... eqn[1] = " + j);
				    System.out.println("... eqn[2] = " + k);
				    System.out.println("... eqn[3] = " + l);
				    System.out.println("... eqn[4] = " + n);
				    System.out.println("... eqn[5] = " + m);
				    rfe.printStackTrace();
				    System.exit(1);
				}
				*/
				int nz =
				    RootFinder.solvePolynomial(eqn, 5, res5);
				for (int p = 0; p < nz; p++) {
				    if (checkRoot(eqn, res5[p]) == false) {
					for (int q = 0; q < eqn.length; q++) {
					    System.out.println
						("eqn[" + q + "] = " + eqn[q]);
					}
					System.out.println
					    ("p = " + p  + ": value at "
					     + res5[p] + " = "
					     + valueAt(eqn, res5[p]));
					throw new Exception
					    ("failed - wrong root");
				    }
				}
			    }
			}
		    }
		}
	    }
	}



	System.out.println("Cubic polynomials");

	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			double eqn[] = {
			    (double)i, (double) j, (double) k, (double)l
			};
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m  + "] = "
						   + eqn[m]);
			    }
			    throw new Exception("wrong number of roots");
			}
			if (i2 > 0) {
			    Arrays.sort(res2, 0, i2);
			}
			// eliminate duplicates
			if (i2 == 3 && res2[0] == res2[1] &&
			    res2[1] == res2[2]) {
			    i2 = 1;
			}
			if (i2 == 3 && res2[1] == res2[2]) i2--;
			if (i2 == 3 && res2[0] == res2[1]) {
			    res2[1] = res2[2];
			    i2--;
			}
			if (i2 == 2 && res2[0] == res2[1]) i2--;
			if (i1 != i2) {
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    for (int m = 0; m < 4; m++) {
					System.out.println("eqn[" + m + "] = "
							   + eqn[m]);
				    }
				    System.out.println("res1[" + n + "] = "
						       + res1[n] + ", "
						       + valueAt(eqn, res1[n]));
				    System.out.println("res2[" + n + "] = "
						       + res2[n] + ", "
						       + valueAt(eqn, res1[n]));
				    throw new Exception("roots don't match");
				}
			    }
			}
		    }
		}
	    }
	}

	// Same as above, but multiply coefficients by 100,000 to test
	// the use of integer arithmetic for large integer coefficients.

	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			double eqn[] = {
			    (double)(i*100000), (double)(j*100000),
			    (double)(k*100000), (double)(l*100000)
			};
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m  + "] = "
						   + eqn[m]);
			    }
			    throw new Exception("wrong number of roots");
			}
			if (i2 > 0) {
			    Arrays.sort(res2, 0, i2);
			}
			// eliminate duplicates
			if (i2 == 3 && res2[0] == res2[1] &&
			    res2[1] == res2[2]) {
			    i2 = 1;
			}
			if (i2 == 3 && res2[1] == res2[2]) i2--;
			if (i2 == 3 && res2[0] == res2[1]) {
			    res2[1] = res2[2];
			    i2--;
			}
			if (i2 == 2 && res2[0] == res2[1]) i2--;
			if (i1 != i2) {
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    for (int m = 0; m < 4; m++) {
					System.out.println("eqn[" + m + "] = "
							   + eqn[m]);
				    }
				    System.out.println("res1[" + n + "] = "
						       + res1[n] + ", "
						       + valueAt(eqn, res1[n]));
				    System.out.println("res2[" + n + "] = "
						       + res2[n] + ", "
						       + valueAt(eqn, res1[n]));
				    throw new Exception("roots don't match");
				}
			    }
			}
		    }
		}
	    }
	}

	// Same as above, but multiply coefficients by 10,000,000 to test
	// for the case where we can't use integer arithmetic even though
	// we have integer coefficients.

	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			double eqn[] = {
			    (double)(i*10000000), (double)(j*10000000),
			    (double)(k*10000000), (double)(l*10000000)
			};
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m  + "] = "
						   + eqn[m]);
			    }
			    throw new Exception("wrong number of roots");
			}
			if (i2 > 0) {
			    Arrays.sort(res2, 0, i2);
			}
			// eliminate duplicates
			if (i2 == 3 && res2[0] == res2[1] &&
			    res2[1] == res2[2]) {
			    i2 = 1;
			}
			if (i2 == 3 && res2[1] == res2[2]) i2--;
			if (i2 == 3 && res2[0] == res2[1]) {
			    res2[1] = res2[2];
			    i2--;
			}
			if (i2 == 2 && res2[0] == res2[1]) i2--;
			if (i1 != i2) {
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    for (int m = 0; m < 4; m++) {
					System.out.println("eqn[" + m + "] = "
							   + eqn[m]);
				    }
				    System.out.println("res1[" + n + "] = "
						       + res1[n] + ", "
						       + valueAt(eqn, res1[n]));
				    System.out.println("res2[" + n + "] = "
						       + res2[n] + ", "
						       + valueAt(eqn, res1[n]));
				    throw new Exception("roots don't match");
				}
			    }
			}
		    }
		}
	    }
	}

	RootFinder rfb = new RootFinder.Brent() {
		public double function(double x) {
		    System.out.println("x = "  + x);
		    return x*x*x;
		}
		public double ferror(double x) {
		    return 3*x*x*Math.ulp(x);
		}
	    };

	double b1 = rfb.solve(1.0, 0.0, 3.0);
	System.out.println("f(x) = x\u00b3 = 1: x = "
			   + rfb.solve(1.0, 0.0, 3.0));

	rfb = RootFinder.Brent.newInstance(x -> x*x*x);

	RootFinder rfb1 = new RootFinder.Brent() {
		public double function(double x) {
		    return x*x*x;
		}
	    };

	b1 = rfb1.solve(1.0, 0.0, 3.0);
	double b2 = rfb.solve(1.0, 0.0, 3.0);
	if (Math.abs(b1-b2) > 1.e-15) throw new Exception("lambda");


	RootFinder rf = new RootFinder.Newton() {
		public double function(double x) {
		    return Math.cos(x);
		}
		public double ferror(double x) {
		    return 5*Math.ulp(1.0);
		}
		public double firstDerivative(double x) {
		    return -Math.sin(x);
		}
	    };

	double result = rf.solve(0.0, 1.5);
	System.out.println("result (Newton) = " + result
			   +" actual = " + Math.PI /2.0);

	rf = new RootFinder.Halley() {
		public double function(double x) {
		    return Math.cos(x);
		}
		public double ferror(double x) {
		    return 5*Math.ulp(1.0);
		}
		public double firstDerivative(double x) {
		    return -Math.sin(x);
		}
		public double secondDerivative(double x) {
		    return -Math.cos(x);
		}
	    };
	result = rf.solve(0.0, 1.5);
	System.out.println("result (Halley)= " + result
			   +" actual = " + Math.PI /2.0);


	RootFinder<Double> rfp =
	    new RootFinder.Newton<Double>(Double.valueOf(1.0)) {
		public double function(double x) {
		    return Math.cos(x) + getParameters();
		}
		public double firstDerivative(double x) {
		    return -Math.sin(x);
		}
	    };

	result = rfp.solve(1.0, 1.5);
	System.out.println("result = " + result
			   +" actual = " + Math.PI /2.0);
	

	rfp = new RootFinder.Halley<Double>(Double.valueOf(1.0)) {
		public double function(double x) {
		    return Math.cos(x) + getParameters();
		}
		public double firstDerivative(double x) {
		    return -Math.sin(x);
		}
		public double secondDerivative(double x) {
		    return -Math.cos(x);
		}
	    };
	result = rfp.solve(1.0, 1.5);
	System.out.println("result = " + result
			   +" actual = " + Math.PI /2.0);
 
	rfp.setParameters(Double.valueOf(10.0));

	try {
	    result = rfp.findRoot(1.5);
	} catch (Exception e) {
	    System.out.println("expected exception: " + e.getMessage());
	}

	System.out.println("Random polynomials:");
	double[] resR = new double[11];
	org.bzdev.math.StaticRandom.maximizeQuality();
	UniformIntegerRV drv = new UniformIntegerRV(1, true, 10, true);
	UniformDoubleRV rv = new UniformDoubleRV(-100.0, true, 100.0, true);
	int degcnt[] = new int[10+1];
	for (int i = 0; i < 10000000; i++) {
	    if (i > 0 && i%100000 == 0) {
		System.out.println("  " + (i/100000) + "% complete");
	    }
	    int degree = drv.next();
	    degcnt[degree]++;
	    double[] eqn = new double[degree+1];
	    for (int j = 0; j <= degree; j++) {
		eqn[j] = rv.next();
	    }
	    try {
		int nzeros = RootFinder.solvePolynomial(eqn, degree, resR);
		for (int j = 0; j < nzeros; j++) {
		    if (checkRoot(eqn, resR[j]) == false) {
			System.out.println("resR[" + j +"] =" + resR[j]
					   + ", f(x) = "
					   + valueAt(eqn, resR[j]));
			throw new Exception("failed at root " + j);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("... failure occurred with");
		for (int j = 0; j <=degree; j++) {
		    System.out.println("    eqn[" + j + "] = " + eqn[j]);
		}
		System.out.println(" ... a polynomial of degree " + degree);
		System.exit(1);
	    }
	}
	System.out.println("  100% complete");
	System.out.println("Distribution:");
	for (int i = 1; i < 11; i++) {
	    System.out.println("degree " + i + ", " + degcnt[i]
			       + " polynomials tested");
	}

	RootFinder.Newton rfn = new RootFinder.Newton() {
		double lastT = org.bzdev.util.units.MKS.degC(25.0);
		double k1 = 1.0/1080.0;
		double k2 = 5.0/3600.0;
		double outsideTemperature = org.bzdev.util.units.MKS.degC(30.0);
		double a = k2 * outsideTemperature * outsideTemperature;
		double b = - (2.0 * k2 * outsideTemperature + k1);
		double c = k2;
		double q = 4.0 * a * c - b * b;
		double qroot = Math.sqrt((q >= 0.0)? q: -q);

		private double f(double x) {
		    double logarg = (2.0*c*x + b - qroot)
			/ (2.0*c*x + b + qroot);
		    if (logarg == 0.0)
			throw new MathException("zero argument for logarithm");
		    if (logarg < 0) logarg = -logarg;
		    return (1.0/qroot)*Math.log(logarg);
		}

		private double timediff(double T1, double T2) {
		    return (outsideTemperature + b/(2.0*c))*(f(T2)-f(T1))
			- Math.log((a + b * T2 + c * T2 * T2)
				   /(a + b * T1 + c * T1 * T1))
			/ (2.0 * c);
		}

		public double function(double T) {
		    return timediff(lastT, T);
		}

		public double firstDerivative(double T) {
		    double denom = k2*T*T
			- (2.0*k2*outsideTemperature + k1) * T
			+ k2*outsideTemperature*outsideTemperature;
		    if (denom == 0.0) {
			throw new MathException
			    ("division by zero in Newton's method");
		    } else {
			return (outsideTemperature - T) / denom;
		    }
		}
	    };
	double T = org.bzdev.util.units.MKS.degC(20.0);

	System.out.println("firstDerivative at T = " + (T+1.0) + " is "
			   + rfn.firstDerivative(T+1.0)
			   + ", should be close to "
			   + ((rfn.function(T+1.0+0.0001)- rfn.function(T+1.0))
			      / 0.0001));
	double deltat = rfn.function(T);
	double resultT = rfn.solve(deltat,
				   org.bzdev.util.units.MKS.degC(25.0));
	System.out.println("resultT = " + resultT
			   + ", deltat = " + deltat
			   +", expected "
			   + rfn.function(resultT));

	final double lastT = org.bzdev.util.units.MKS.degC(25.0);
	final double k1 = 1.0/1080.0;
	final double k2 = 5.0/3600.0;
	final double outsideTemperature = org.bzdev.util.units.MKS.degC(30.0);
	final double a = k2 * outsideTemperature * outsideTemperature;
	final double b = - (2.0 * k2 * outsideTemperature + k1);
	final double c = k2;
	final double q = 4.0 * a * c - b * b;
	final double qroot = Math.sqrt((q >= 0.0)? q: -q);
	final double Tmin = outsideTemperature + (k1 - qroot)/(2.0*k2);

	RootFinder.Brent rfnb = new RootFinder.Brent() {

		private double f(double x) {
		    double logarg = (2.0*c*x + b - qroot)
			/ (2.0*c*x + b + qroot);
		    if (logarg == 0.0)
			throw new MathException("zero argument for logarithm");
		    if (logarg < 0) logarg = -logarg;
		    return (1.0/qroot)*Math.log(logarg);
		}

		private double timediff(double T1, double T2) {
		    double ratio = (a + b * T2 + c * T2 * T2)
			/ (a + b * T1 + c * T1 * T1);
		    System.out.println("ratio = " + ratio);
		    return (outsideTemperature + b/(2.0*c))*(f(T2)-f(T1))
			- Math.log(ratio)
			/ (2.0 * c);
		}

		public double function(double T) {
		    System.out.println("calling function for x = " + T
				       +", value = " + timediff(lastT, T));
		    return timediff(lastT, T);
		}
	    };

	System.out.println("Tmin = " + Tmin);

	double resultTB = rfnb.solve(deltat,
				     org.bzdev.util.units.MKS.degC(25.0),
				     org.bzdev.util.units.MKS.degC(19.0));
	System.out.println("with Brent's algorithm, resultT is "
			   + resultTB);

	// Try a botched Newton's method with Brent's method as
	// a fallback.

	rfn = new RootFinder.Newton() {
		public double function(double x) {
		    return x*x*x;
		}
		public double firstDerivative(double x) {
		    return 0.0;
		}
	    };
	System.out.println("Newton's method, falling back on Brent's method");
	System.out.println("... result = " + rfn.solve(0.0, 1.0, -1.0, 1.0)
			   + ", expecting 0.0");

	System.exit(0);
    }
}
