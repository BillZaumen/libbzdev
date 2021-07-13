import java.awt.geom.CubicCurve2D;
import java.util.Arrays;
import org.bzdev.math.RootFinder;
import org.bzdev.lang.MathOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.rv.UniformDoubleRV;

public class CubicTest {

    static long cubicDiscriminant(long a, long b, long c, long d) {
	long disc = 18*a*b*c*d - 4*b*b*b*d + b*b*c*c
	    - 4*a*c*c*c - 27*a*a*d*d;
	return disc;
    }

    static int numberOfCubicRoots(long a, long b, long c, long d) {
	if (a == 0 && b == 0 && c == 0) return -1;
	if (a == 0 && b == 0) return 1;
	if (a == 0) {
	    long qa = b;
	    long qb = c;
	    long qc = d;
	    long qdisc = qb*qb - 4*qa*qc;
	    if (qdisc < 0) return 0;
	    if (qdisc == 0) return 1;
	    if (qdisc > 0) return 2;
	}
	long disc = cubicDiscriminant(a, b, c, d);
	if (disc > 0) return 3;
	if (disc < 0) return 1;
	long delta0 = b*b-3*a*c;
	if (delta0 == 0) return 1;
	else return 2;
    }

    static double valueAt(double[] eqn, double t) {
	// use Kahan's addition algorithm to reduce floating-point
	// errors.
	double prod = t;
	double c = 0;
	double sum = eqn[0];
	double y = eqn[1]*prod;
	double tt = sum + y;
	c = (tt - sum) - y;
	sum = tt;
	prod *= t;
	y = (eqn[2]*prod) - c;
	tt = sum + y;
	c = (tt - sum) - y;
	sum = tt;
	prod *= t;
	y = (eqn[3]*prod) - c;
	tt = sum + y;
	return tt;
    }

    static public void main(String argv[]) {

	int  nrcc = 0;
	int nrrf = 0;
	int ccCount = 0;
	int rfCount = 0;
	int tieCount = 0;

	BasicStats statsCCRF = new BasicStats.Population();
	BasicStats statsRFCC = new BasicStats.Population();
	BasicStats statsRF = new BasicStats.Population();
	BasicStats statsCC = new BasicStats.Population();

	double[] res1 = new double[3];
	double[] res2 = new double[3];

	int cases = 0;
	for (int i = 1; i <= 30; i++) {
	    for (int j = -30; j <= 30; j++) {
		for (int k = -30; k <= 30; k++) {
		    for (int l = -30; l <= 30; l++) {
			int gcd = MathOps.gcd(i, (j == 0? i: Math.abs(j)));
			gcd = MathOps.gcd(gcd, (k == 0? gcd: Math.abs(k)));
			gcd = MathOps.gcd(gcd, (l == 0? gcd: Math.abs(l)));
			if (gcd != 1) continue;
			cases++;
			double eqn[] = {
			    (double)i, (double) j, (double) k, (double)l
			};
			int nr = numberOfCubicRoots(l, k, j, i);
			int i1 = RootFinder.solveCubic(eqn, res1);
			int i2 = CubicCurve2D.solveCubic(eqn, res2);
			if (nr != i1) {
			    nrrf++;
			}
			if (nr != i2) {
			    nrcc++;
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
			if (i1 == nr && i2 == nr) {
			    for (int p = 0; p < nr; p++) {
				double rfval = Math.abs(valueAt(eqn, res1[p]));
				double ccval = Math.abs(valueAt(eqn, res2[p]));
				if (rfval < ccval) {
				    rfCount++;
				    statsRFCC.add(rfval/ccval);
				} else if (rfval > ccval) {
				    ccCount++;
				    statsCCRF.add(ccval/rfval);
				} else {
				    tieCount++;
				}
				statsRF.add(valueAt(eqn, res1[p]));
				statsCC.add(valueAt(eqn, res2[p]));
			    }
			}
		    }
		}
	    }
	}
	System.out.println("RootFinder.solveCubic: wrong number of roots = "
			   + nrrf + " out of " + cases + " tries");
	System.out.println("CubicCurve2D.solveCubic: wrong number of roots = "
			   + nrcc + " out of " + cases + " tries");

	System.out.println("RootFinder.solveCubic better: "
			   + rfCount + " times");
	System.out.println("CubicCurve2D.solveCubic better: "
			   + ccCount + " times");
	System.out.println("Ties: " +tieCount + " times");

	System.out.println("mean ratio (RF/CC) when RF is better: "
			   + statsRFCC.getMean()
			   + " (sdev = " + statsRFCC.getSDev() +")");
	System.out.println("mean ratio (CC/RF) when CC is better: "
			   + statsCCRF.getMean()
			   + " (sdev = " + statsCCRF.getSDev() +")");

	System.out.println("RF value at root: mean = " +statsRF.getMean()
			   +", sdev = " + statsRF.getSDev()
			   +", nroots = " + statsRF.size());
	System.out.println("CC value at root: mean = " +statsCC.getMean()
			   +", sdev = " + statsCC.getSDev()
			   +", nroots = " + statsCC.size());

	System.out.println();
	System.out.println("Performance Test");

	org.bzdev.math.StaticRandom.maximizeQuality();
	UniformDoubleRV rv = new UniformDoubleRV(-100.0, true, 100.0, true);
	
	double[][] eqns = new double[1000000][4];
	System.out.println("eqns.length = " + eqns.length);
	for (int i = 0; i < eqns.length; i++) {
	    eqns[i][0] = rv.next();
	    eqns[i][1] = rv.next();
	    eqns[i][2] = rv.next();
	    eqns[i][3] = rv.next();
	}
	for (int i = 0; i < 10; i++) {
	    long time0 = System.nanoTime();
	    int cnt = 0;
	    for (double[] eqn: eqns) {
		cnt += RootFinder.solveCubic(eqn, res1);
	    }
	    long time1 = System.nanoTime();
	    for (double[] eqn: eqns) {
		cnt += CubicCurve2D.solveCubic(eqn, res2);
	    }
	    long time2 = System.nanoTime();
	    System.out.println("Run " + i +":");
	    System.out.println("for RootFinder.solveCubic, "
			       + (time1 - time0)/1000000 + " milliseconds");
	    System.out.println("for CubicCurve.solveCubic, "
			       + (time2 - time1)/1000000 + " milliseconds");
	    System.out.println("ratio = "
			       + (((100*(time1-time0))/(time2-time1))/100.0));
	    System.out.println((cnt/2) + " equations solved");
	}

	System.out.println();
	System.out.println("Try a case that failed:");
	System.out.println("ax\u00b3 + bx\u00b2 + cx + d = 0, where");
	double[] testeqn = new double[4];

	testeqn[0] = 1.0;
	testeqn[1] = 5.0;
	testeqn[2] = 7.0;
	testeqn[3] = 3.0;
	System.out.println("a = " + testeqn[3]);
	System.out.println("b = " + testeqn[2]);
	System.out.println("c = " + testeqn[1]);
	System.out.println("d = " + testeqn[0]);
	
	System.out.println();
	System.out.println("n1 corresponds to RootFinder.solveCubic");
	System.out.println("n2 corresponds to CubicCurve2D.solveCubic");
	System.out.println();

	int n1 = RootFinder.solveCubic(testeqn, res1);
	int n2 = CubicCurve2D.solveCubic(testeqn, res2);
    
	System.out.println("n1 = " + n1);
	for (int i = 0; i < n1; i++) {
	    System.out.println("    root = " + res1[i] + ", value = "
			       + valueAt(testeqn, res1[i]));
	}
	System.out.println("n2 = " + n2);
	for (int i = 0; i < n2; i++) {
	    System.out.println("    root = " + res2[i] + ", value = "
			       + valueAt(testeqn, res2[i]));
	}

	System.out.println("... second case");

	testeqn[0] = -4.0;
	testeqn[1] = -5.0;
	testeqn[2] = 2.0;
	testeqn[3] = 3.0;

	System.out.println("a = " + testeqn[3]);
	System.out.println("b = " + testeqn[2]);
	System.out.println("c = " + testeqn[1]);
	System.out.println("d = " + testeqn[0]);
	
	System.out.println();
	System.out.println("n1 corresponds to RootFinder.solveCubic");
	System.out.println("n2 corresponds to CubicCurve2D.solveCubic");
	System.out.println();

	n1 = RootFinder.solveCubic(testeqn, res1);
	n2 = CubicCurve2D.solveCubic(testeqn, res2);
    
	System.out.println("n1 = " + n1);
	for (int i = 0; i < n1; i++) {
	    System.out.println("    root = " + res1[i] + ", value = "
			       + valueAt(testeqn, res1[i]));
	}
	System.out.println("n2 = " + n2);
	for (int i = 0; i < n2; i++) {
	    System.out.println("    root = " + res2[i] + ", value = "
			       + valueAt(testeqn, res2[i]));
	}
    }
}
