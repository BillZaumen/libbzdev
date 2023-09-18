import org.bzdev.math.*;
import org.bzdev.lang.MathOps;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.math.rv.UniformDoubleRV;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;

public class RootFinderCubic {

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

    static long cubicDiscriminant(long a, long b, long c, long d) {
	long disc = 18*a*b*c*d - 4*b*b*b*d + b*b*c*c
	    - 4*a*c*c*c - 27*a*a*d*d;
	return disc;
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

    public static void main(String argv[])  throws Exception  {
	System.out.println("Cubic polynomials");

	double[] res = new double[3];
	double[] res1 = new double[3];
	double[] res2 = new double[3];

	for (int i = -10; i < 10; i++) {
	    for (int j = -10; j < 10; j++) {
		for (int k = -10; k < 10; k++) {
		    for (int l = -10; l < 10; l++) {
			if (l == 0) continue;
			double eqn[] = {
			    (double)i, (double) j, (double) k, (double)l
			};
			double p = eqn[2]/eqn[3];
			double q = eqn[1]/eqn[3];
			double r = eqn[0]/eqn[3];
			double a = (3*q - p*p)/3;
			double b = (2*p*p*p - 9*p*q + 27*r)/27.0;
			double cosphi = (a < 0.0)?
			    -b/2 + Math.sqrt(-a*a*a/27): -2;
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    System.out.println("cosphi = " + cosphi);
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
			    System.out.println("cosphi = " + cosphi);
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    System.out.println("cosphi = " + cosphi);
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
			if (l == 0) continue;
			double eqn[] = {
			    (double)(i*100000), (double)(j*100000),
			    (double)(k*100000), (double)(l*100000)
			};
			double p = eqn[2]/eqn[3];
			double q = eqn[1]/eqn[3];
			double r = eqn[0]/eqn[3];
			double a = (3*q - p*p)/3;
			double b = (2*p*p*p - 9*p*q + 27*r)/27.0;
			double cosphi = (a < 0.0)?
			    -b/2 + Math.sqrt(-a*a*a/27): -2;
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    System.out.println("cosphi = " + cosphi);
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
				System.out.println("cosphi = " + cosphi);
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    System.out.println("cosphi = " + cosphi);
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
			double p = eqn[2]/eqn[3];
			double q = eqn[1]/eqn[3];
			double r = eqn[0]/eqn[3];
			double a = (3*q - p*p)/3;
			double b = (2*p*p*p - 9*p*q + 27*r)/27.0;
			double cosphi = (a < 0.0)?
			    -b/2 + Math.sqrt(-a*a*a/27): -2;
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    System.out.println("nr = " + nr + ", i1 = " + i1
					       +", i2 = " + i2);
			    System.out.println("cosphi = " + cosphi);
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
			    System.out.println("cosphi = " + cosphi);
			    for (int m = 0; m < 4; m++) {
				System.out.println("eqn[" + m + "] = "
						   + eqn[m]);
			    }
			    System.out.println("i1 = " + i1 + ", i2 = " + i2);
			} else {
			    for (int n = 0; n < i1; n++) {
				if (Math.abs(res1[n] - res2[n]) > 1.e-10) {
				    System.out.println("cosphi = " + cosphi);
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
    }
}
