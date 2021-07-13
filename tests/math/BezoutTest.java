import org.bzdev.math.*;
import org.bzdev.lang.MathOps;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class BezoutTest {

    static void intTest() throws Exception {
	int[] result = new int[2];
	int a = 45;
	int b = 17;
	int gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}
	b = 25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}
	a = -45;
	b = 17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = 25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	a = 45;
	b = -17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = -25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	a = -45;
	b = -17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = -25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}
	for (int i = -99; i < 100; i++) {
	    for (int j = -99; j < 100; j++) {
		if (i == 0 && j == 0) continue;
		a = i;
		b = j;
		gcd = Bezout.gcd(a, b, result);
		if (MathOps.gcd(a,b) != gcd) {
		    System.out.println("a = " + a +  ", b = " + b
				       + ", gcd = " + gcd);
		    System.out.println("MathOps.gcd returned "
				       + MathOps.gcd(a,b));
		    throw new Exception("gcd failed");
		}
		if (gcd != result[0]*a + result[1]*b) {
		    System.out.println("a = " + a +  ", b = " + b
				       + ", gcd = " + gcd);
		    System.out.println("s = " + result[0]
				       + ", t = " + result[1]);
		    throw new Exception("Bezout coefficients failed");
		}
	    }
	}
	StaticRandom.maximizeQuality();
	IntegerRandomVariable rv = new UniformIntegerRV(-(1<<20), 1<<20);
	for (int i = 0; i < 10000000; i++) {
	    a = rv.next();
	    b = rv.next();
	    if (a == 0 && b == 0) continue;
	    gcd = Bezout.gcd(a, b, result);
	    if (MathOps.gcd(a,b) != gcd) {
		throw new Exception("gcd failed");
	    }
	    if (gcd != result[0]*a + result[1]*b) {
		System.out.println("a = " + a +  ", b = " + b
				   + ", gcd = " + gcd);
		System.out.println("s = " + result[0]
				   + ", t = " + result[1]);
		throw new Exception("Bezout coefficients failed");
	    }
	}
    }

    static void longTest() throws Exception {
	long[] result = new long[2];
	long a = 45;
	long b = 17;
	long gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}
	b = 25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}
	a = -45;
	b = 17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = 25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	a = 45;
	b = -17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = -25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	a = -45;
	b = -17;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	b = -25;
	gcd = Bezout.gcd(a, b, result);
	if (MathOps.gcd(a,b) != gcd) {
	    throw new Exception("gcd failed");
	}
	if (gcd != result[0]*a + result[1]*b) {
	    throw new Exception("Bezout coefficients failed");
	}

	for (int i = -99; i < 100; i++) {
	    for (int j = -99; j < 100; j++) {
		if (i == 0 && j == 0) continue;
		a = i;
		b = j;
		gcd = Bezout.gcd(a, b, result);
		if (MathOps.gcd(a,b) != gcd) {
		    throw new Exception("gcd failed");
		}
		if (gcd != result[0]*a + result[1]*b) {
		    System.out.println("a = " + a +  ", b = " + b
				       + ", gcd = " + gcd);
		    System.out.println("s = " + result[0]
				       + ", t = " + result[1]);
		    throw new Exception("Bezout coefficients failed");
		}
	    }
	}
	StaticRandom.maximizeQuality();
	LongRandomVariable rv = new UniformLongRV(-(1<<20), 1<<20);
	for (int i = 0; i < 10000000; i++) {
	    a = rv.next();
	    b = rv.next();
	    if (a == 0 && b == 0) continue;
	    gcd = Bezout.gcd(a, b, result);
	    if (MathOps.gcd(a,b) != gcd) {
		throw new Exception("gcd failed");
	    }
	    if (gcd != result[0]*a + result[1]*b) {
		System.out.println("a = " + a +  ", b = " + b
				   + ", gcd = " + gcd);
		System.out.println("s = " + result[0]
				   + ", t = " + result[1]);
		throw new Exception("Bezout coefficients failed");
	    }
	}
    }


    public static void main(String argv[]) throws Exception {
	System.out.println("checking integer cases ...");
	intTest();
	System.out.println("checking long cases ...");
	longTest();
	System.out.println("... OK");
	System.exit(0);
    }

}
