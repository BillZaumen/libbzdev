import org.bzdev.math.*;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;


public class PolynomialTest {

    static void integralTest() {
	BezierPolynomial bpx =
	    new BezierPolynomial(200.000,
				 200.000,
				 200.000,
				 200.000);
	BezierPolynomial bpy =
	    new BezierPolynomial(250.000,
				 83.3333,
				 -83.3333,
				 -250.000);

	BezierPolynomial dydu = bpy.deriv();
	/*
	BezierPolynomial integrand = bpy.multiply(bpy);
	integrand.multiplyBy(bpx);
	integrand.multiplyBy(dydu);
	System.out.println("integral of y^2x dy/du du = "
			   + integrand.integralAt(1.0));
	*/
	bpy.multiplyBy(bpy);
	bpy.multiplyBy(bpx);
	bpy.multiplyBy(dydu);
	System.out.println("integral of y^2x dy/du du = "
			   + bpy.integralAt(1.0));
    }

    static void badcase() throws Exception {
	int deg1 = 10;
	int deg2 = 1;
	double[] p1 = new double[deg1+1];
	double[] p2 = new double[deg2+1];
	p1[0] = 5.055832194989883;
	p1[1] = -6.377532529195433;
	p1[2] = -1.648066454379407;
	p1[3] = -6.664776987879824;
	p1[4] = -9.913142660200338;
	p1[5] = 11.011644731787342;
	p1[6] = 6.499930124981663;
	p1[7] = 16.59232841567509;
	p1[8] = -16.11922415992573;
	p1[9] = 19.852145288267316;
	p1[10] = -4.9061808702028245;
	p2[0] = -14.267558704211917;
	p2[1] = -0.16512094757360885;

	double[] q = new double[20];
	double[] r = new double[20];
	double[] result = new double[20];

	int nq = Polynomials.divide(q, r, p1, deg1, p2, deg2);
	int nr = Polynomials.getDegree(r, deg1);
	double denom = 1.0;
	for (int j = 0; j <= deg1; j++) {
	    denom = Math.max(denom, Math.abs(p1[j]));
	}
	for (int j = 0; j <= deg2; j++) {
	    denom = Math.max(denom, Math.abs(p2[j]));
	}
	for (int j = 0; j <= nq; j++) {
	    denom = Math.max(denom, Math.abs(q[j]));
	}
	for (int j = 0; j <= nr; j++) {
	    denom = Math.max(denom, Math.abs(r[j]));
	}
	System.out.println("denom = " + denom);

	for (int i = 0; i <= nq; i++) {
	    System.out.println(" ... q[" + i + "] = " + q[i]);
	}

	for (int i = 0; i <= nr; i++) {
	    System.out.println(" ... r[" + i + "] = " + r[i]);
	}
	int n = Polynomials.multiply(result, q, nq, p2, deg2);
	for (int i = 0; i <= nr; i++) {
	    System.out.println("... for i = " + i
			       + ", (p1[i] - result[i]) - r[i] = "
			       + ((p1[i] -result[i]) - r[i]));
	}
	for (int i = nr+1; i <= deg1; i++) {
	    System.out.println("... for i = " + i
			       + ", p1[i] = " + p1[i] + ", result[i] = "
			       + result[i]);
	}
	n = Polynomials.add(result, result, n, r, nr);
	if (n != deg1) throw new Exception();
	for (int j = 0; j <= deg1; j++) {
	    double err = Math.abs((result[j] - p1[j])/denom);
	    if (err > 1.e-14) {
		System.out.println("result[" + j + "] = " + result[j]
				   + ", p1[" + j + "] = " + p1[j]);
		throw new Exception();
	    }
	}
    }

    static double valueAt(double[] coeff, double x) {
	double c = 0.0;
	double sum = coeff[0];
	double prod = 1.0;
	for (int i = 1; i < coeff.length; i++) {
	    prod *= x;
	    double y = coeff[i]*prod - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return sum;
    }

    static double valueAt(double[] coeff, int n,  double x) {
	double c = 0.0;
	double sum = coeff[0];
	double prod = 1.0;
	for (int i = 1; i <= n; i++) {
	    prod *= x;
	    double y = coeff[i]*prod - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	}
	return sum;
    }

    static void bezierTest() throws Exception {
	double[] beta1 = {1.0, 3.0, 8.0, 20.0, 15.0, 11.0};

	double[] beta1Scaled = new double[beta1.length];
	Functions.Bernstein.scale(beta1Scaled, beta1, beta1.length-1);
	double[] beta1Unscaled = new double[beta1.length];
	Functions.Bernstein.unscale(beta1Unscaled, beta1Scaled, beta1.length-1);
	for (int i = 0; i < beta1.length; i++) {
	    if (Math.abs(beta1Scaled[i]/beta1[i] - Binomial.coefficient(5,i))
		> 1.e-14) throw new Exception();
	    if (Math.abs(beta1Unscaled[i] - beta1[i]) > 1.e-14) {
		throw new Exception();
	    }
	}

	double[] coeff1 = Polynomials.fromBezier(beta1, 0, 5);
	System.out.print("coeff1: " + coeff1[0]);
	for (int i = 1; i < coeff1.length; i++) {
	    System.out.print(", " + coeff1[i]);
	}
	System.out.println();
	double v1 = Functions.Bernstein.sumB(beta1, 0, 5, 4.5);
	double v3 = Functions.Bernstein.sumB(beta1, 5, 4.5);
	if (v1 != v3) throw new Exception();
	double v2 = valueAt(coeff1, 4.5);
	if (Math.abs(v1 - v2) > 1.e-14) {
	    System.out.println("v1 = " + v1);
	    System.out.println("v2 = " + v2);
	    throw new Exception();
	}

	double beta2[] = new double[beta1.length];
	Polynomials.toBezier(beta2, 0, coeff1, 5);
	for (int i = 0; i < beta1.length; i++) {
	    if (Math.abs(beta1[i]-beta2[i]) > 1.e-14) {
		throw new Exception();
	    }
	}

	double[] p1 = {1.0, 3.0, 8.0, 20.0, 15.0, 11.0};
	double[] p2  =  {3.0, 7.0, 4.0, 8.0};

	double[] p1c = new double[p1.length];
	double[] p2c = new double[p2.length];

	Polynomials.fromBezier(p1c, p1, 0, p1.length-1);
	Polynomials.fromBezier(p2c, p2, 0, p2.length-1);

	double[] add12  = new double[p1.length];
	double[] add12c  = new double[p1.length];

	int addDegree = Polynomials.bezierAdd(add12, p1, 5, p2, 3);
	int addDegreec = Polynomials.add(add12c, p1c,5, p2c, 3);
	if (addDegree != addDegreec) throw new Exception();
	Polynomials.toBezier(add12c, 0, add12c, 5);
	for (int i = 0; i <= addDegree; i++) {
	    if (Math.abs(add12[i] - add12c[i]) > 1.e-14) {
		throw new Exception();
	    }
	}

	double[] mult12 = new double[p1.length+p2.length - 1];
	double[] mult12c = new double[p1.length+p2.length - 1];
	int multDegree = Polynomials.bezierMultiply(mult12, p1, 5, p2, 3);
	int multDegreec = Polynomials.multiply(mult12c, p1c, 5, p2c, 3);
	Polynomials.toBezier(mult12c, 0, mult12c, 8);
	if (multDegree != multDegreec) {
	    throw new Exception();
	}
	for (int i = 0; i <= multDegree; i++) {
	    if (Math.abs(mult12[i] - mult12c[i]) > 1.e-12) {
		System.out.println("i = " + i + ", mult = " + mult12[i]
				   + ", multc = " + mult12c[i]);
		throw new Exception();
	    }
	}

	UniformIntegerRV degreeRV = new UniformIntegerRV(0, true,
							 10, true);
	UniformIntegerRV divDegreeRV = new UniformIntegerRV(0, true, 3, true);
	UniformDoubleRV coeffRV = new UniformDoubleRV(-20.0, true, 20.0, true);

	p1 = new double[11];
	p2 = new double[11];
	double[] pdiv = new double[4];
	double[] p3 = new double[21];
	BasicStats statsAddition = new BasicStats.Population();
	BasicStats statsMultiplication = new BasicStats.Population();
	BasicStats statsDivision = new BasicStats.Population();

	for (int i = 0; i < 1000000; i++) {
	    int deg1 = degreeRV.next();
	    for (int j = 0; j <= deg1; j++) {
		p1[j] = coeffRV.next();
	    }
	    int deg2 = degreeRV.next();
	    for (int j = 0; j <= deg1; j++) {
		p2[j] = coeffRV.next();
	    }
	    int divdeg = divDegreeRV.next();
	    for (int j = 0; j < divdeg; j++) {
		pdiv[j] = coeffRV.next();
	    }
	    BezierPolynomial Pdiv = new BezierPolynomial(pdiv, divdeg);
	    int n = Polynomials.bezierMultiply(p3, p1, deg1, p2, deg2);
	    for (int k = 0; k <= 10.0; k++) {
		double x = k/10.0;
		v1 = Functions.Bernstein.sumB(p1, deg1, x);
		v2 = Functions.Bernstein.sumB(p2, deg2, x);
		v3 = Functions.Bernstein.sumB(p3, n, x);
		double v = v1*v2;
		double vm = Math.max(Math.abs(v), Math.abs(v3));
		if (vm < 1.0) vm = 1.0;
		statsMultiplication.add(Math.abs((v3-v)/vm));
	    }

	    BezierPolynomial P1 = new BezierPolynomial(p1, deg1);
	    BezierPolynomial P2 = new BezierPolynomial(p2, deg2);
	    BezierPolynomial P3 = new BezierPolynomial(21);
	    BezierPolynomial P4 = null;
	    BezierPolynomial P5 = null;

	    Polynomials.multiply(P3, P1, P2);
	    P4 = Polynomials.multiply(P1, P2);
	    P5 = new BezierPolynomial(P1);
	    P5.multiplyBy(P2);
	    if (P3.getDegree() != P4.getDegree()) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P3.getCoefficientsArray()[j]
		    != P4.getCoefficientsArray()[j])
		    throw new Exception();
		if (P3.getCoefficientsArray()[j]
		    != P5.getCoefficientsArray()[j])
		    throw new Exception();
	    }

	    if (P3.getDegree() != n) {
		for (int k = 0; k <= deg1; k++) {
		    System.out.println("p1[" + k + "] = " + p1[k]);
		}
		for (int k = 0; k <= deg2; k++) {
		    System.out.println("p2[" + k + "] = " + p2[k]);
		}
		System.out.println("n = " + n + ", P3.getDegree() = "
				   + P3.getDegree());
		throw new Exception();
	    }
	    for (int j = 0; j <= n; j++) {
		if (P3.getCoefficientsArray()[j] !=  p3[j]) {
		    throw new Exception();
		}
	    }

	    double s = 0.0;
	    n = Polynomials.multiply(p3, s, p1, deg1);
	    if (n != 0) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (p3[j] != s*p1[j]) throw new Exception();
	    }

	    s = 7.0;
	    n = Polynomials.multiply(p3, s, p1, deg1);
	    if (n != deg1) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (p3[j] != s*p1[j]) throw new Exception();
	    }

	    Polynomials.multiply(P3, s, P1);
	    if (P3.getDegree() != n) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P3.getCoefficientsArray()[j] != s*p1[j])
		    throw new Exception();
	    }
	    P4 = Polynomials.multiply(s, P1);
	    if (P4.getDegree() != P3.getDegree()) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P4.getCoefficientsArray()[j] != s*p1[j])
		    throw new Exception();
	    }
	    P5 = new BezierPolynomial(P1);
	    P5.multiplyBy(s);
	    if (P5.getDegree() != P3.getDegree()) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P5.getCoefficientsArray()[j] != s*p1[j])
		    throw new Exception();
	    }

	    n = Polynomials.bezierAdd(p3, p1, deg1, p2, deg2);
	    for (int k = 0; k <= 10.0; k++) {
		double x = k/10.0;
		v1 = Functions.Bernstein.sumB(p1, deg1, x);
		v2 = Functions.Bernstein.sumB(p2, deg2, x);
		v3 = Functions.Bernstein.sumB(p3, n, x);
		double v = v1+v2;
		double vm = Math.max(Math.abs(v), Math.abs(v3));
		if (vm < 1.0) vm = 1.0;
		statsAddition.add(Math.abs((v3-v)/vm));
	    }
	    Polynomials.add(P3, P1, P2);

	    P4 = Polynomials.add(P1, P2);
	    P5 = new BezierPolynomial(P1);
	    P5.incrBy(P2);
	    if (P3.getDegree() != P4.getDegree()) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P3.getCoefficientsArray()[j]
		    != P4.getCoefficientsArray()[j])
		    throw new Exception();
		if (P3.getCoefficientsArray()[j]
		    != P5.getCoefficientsArray()[j])
		    throw new Exception();
	    }

	    if (P3.getDegree() != n) throw new Exception();
	    for (int j = 0; j <= n; j++) {
		if (P3.getCoefficientsArray()[j] !=  p3[j]) {
		    throw new Exception();
		}
	    }

	    BezierPolynomial P = new BezierPolynomial();
	    BezierPolynomial Q = new BezierPolynomial();
	    try {
		Polynomials.divide(P, Q, P1, Pdiv);
	    } catch (Exception e) {
		System.out.println("Division failed");
		for (int k = 0; k < P1.getDegree(); k++) {
		    System.out.println("P1[" + k + "] ="
				       + P1.getCoefficientsArray()[k]);
		}
		for (int k = 0; k < Pdiv.getDegree(); k++) {
		    System.out.println("Pdiv[" + k + "] ="
				       + Pdiv.getCoefficientsArray()[k]);
		}
		throw e;
	    }
	    P3 = Pdiv.multiply(P).add(Q);
	    if (P3.getDegree() != P1.getDegree()) {
		throw new Exception();
	    }
	    for (int k = 0; k <= 10; k++) {
		double x = k/10.0;
		v1 = P1.valueAt(x);
		v3  = P3.valueAt(x);
		if (Double.isNaN(v1) || Double.isNaN(v3)) {
		    // tests showed this was due to P2 being zero.
		    break;
		}
		double vm = Math.max(Math.abs(v1), Math.abs(v3));
		if (vm < 1.0) vm = 1.0;
		statsDivision.add((v1-v3)/vm);
	    }
	}
	System.out.println("statsMultiplication: mean = "
			   + statsMultiplication.getMean());
	System.out.println("statsMultiplication: sdev = "
			   + statsMultiplication.getSDev());
	System.out.println("statsAddition: mean = "
			   + statsAddition.getMean());
	System.out.println("statsAddition: sdev = "
			   + statsAddition.getSDev());
	System.out.println("statsDivision: mean = "
			   + statsDivision.getMean());
	System.out.println("statsDivision: sdev = "
			   + statsDivision.getSDev());
    }

    public static void  raiseTest() {
	UniformIntegerRV degreeRV1 = new UniformIntegerRV(3, true,
							 10, true);
	UniformDoubleRV coeffRV = new UniformDoubleRV(-20.0, true, 20.0, true);

	UniformIntegerRV raiseRV1 = new UniformIntegerRV(1, true, 9, true);
	double[] p1 = new double[20];
	double[] p1b = new double[20];
	BasicStats statsRaise = new BasicStats.Population();

	for (int i = 0; i < 1000000; i++) {
	    int deg1 = degreeRV1.next();
	    for (int j = 0; j <= deg1; j++) {
		p1[j] += coeffRV.next();
	    }
	    int raise = raiseRV1.next();
	    int n = Functions.Bernstein.raiseBy(p1b, p1, deg1, raise);
	    for (int k = 0; k <= 10.0; k++) {
		double x = k/10.0;
		double v1 = Functions.Bernstein.sumB(p1, deg1, x);
		double v2 = Functions.Bernstein.sumB(p1b, deg1+raise, x);
		double v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		statsRaise.add((v1 - v2)/v);
	    }
	}
	System.out.println("StatsRaise: mean  = " + statsRaise.getMean());
	System.out.println("StatsRaise: sdev  = " + statsRaise.getSDev());
    }

    public static void main(String argv[]) throws Exception {
	if (true) badcase();
	// First test methods that use arrays as arguments
	// as these are the ones the other methods call.

	double[] p1 = {1.0, 2.0, 3.0};
	double[] p2 = {4.0, 5.0, 6.0, 7.0};
	double[] result = new double[p1.length + p2.length];
	double[] expected1 = {5.0, 7.0, 9.0, 7.0};

	Polynomial P1 = new Polynomial(p1);
	Polynomial P2 = new Polynomial(p2, 3);

	int n = Polynomials.add(result, p1, 2, p2, 3);
	if (n != 3) throw new Exception();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - expected1[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + expected1[i]);
		throw new Exception();
	    }
	}
	result = P1.add(P2).getCoefficients();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - expected1[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + expected1[i]);
		throw new Exception();
	    }
	}

	result = new double[2+3+1];
	double[] expected2 = {4.0, 13.0, 28.0, 34.0, 32.0, 21.0};
	n = Polynomials.multiply(result, p1, 2, p2, 3);
	if (n != 5) throw new Exception();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - expected2[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + expected2[i]);
		throw new Exception();
	    }
	}
	result = P1.multiply(P2).getCoefficients();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - expected2[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + expected2[i]);
		throw new Exception();
	    }
	}

	double[] p3 = result;
	result = new double[4];
	n = Polynomials.multiply(result, 2.0, p2, 3);
	if (n != 3) throw new Exception();
	for (int i = 0; i <= 3; i++) {
	    if (Math.abs(result[i] - 2.0 * p2[i]) > 1.e-14) {
		throw new Exception();
	    }
	}
	result = P2.multiply(2.0).getCoefficients();
	for (int i = 0; i <= 3; i++) {
	    if (Math.abs(result[i] - 2.0 * p2[i]) > 1.e-14) {
		throw new Exception();
	    }
	}
	result = new double[5];
	double[] r = new double[6];

	Polynomial P3 = new Polynomial(p3);
	Polynomial Q = new Polynomial();
	Polynomial R = new Polynomial();

	n = Polynomials.divide(result, r, p3, 5, p1, 2);
	Polynomials.divide(Q, R, P3, P1);
	int nr = Polynomials.getDegree(r, 5);
	if (n != 3) throw new Exception();
	if (Polynomials.getDegree(r, 5) != 0) throw new Exception();
	if (r[0] != 0) throw new Exception();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - p2[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + p2[i]);
		throw new Exception();
	    }
	}
	if (Q.getDegree() != 3) throw new Exception();
	if (R.getDegree() != nr) throw new Exception();
	result = Q.getCoefficients();
	for (int i = 0; i < n; i++) {
	    if (Math.abs(result[i] - p2[i]) > 1.e-10) {
		System.out.println("result[" + i + "] = " + result[i]
				   + ", expected value = " + p2[i]);
		throw new Exception();
	    }
	}
	Polynomial Q2 = P3.divide(P1, true);
	Polynomial R2 = P3.divide(P1, false);
	if (Q.getDegree() != Q2.getDegree()) throw new Exception();
	if (R.getDegree() != R2.getDegree()) throw new Exception();
	double[] cq = Q.getCoefficients();
	double[] cq2 = Q2.getCoefficients();
	if (cq.length != cq2.length) throw new Exception();
	for (int i = 0; i < cq.length; i++) {
	    if (Math.abs(cq[i]-cq2[i]) > 1.e-14) throw new Exception();
	}
	
	double[] cr = R.getCoefficients();
	double[] cr2 = R2.getCoefficients();
	if (cr.length != cr2.length) throw new Exception();
	for (int i = 0; i < cr.length; i++) {
	    if (Math.abs(cr[i]-cr2[i]) > 1.e-14) throw new Exception();
	}
	StaticRandom.maximizeQuality();
	// Now try some randomly generated cases.
	// Test the corrsponding Bezier functions.
	System.out.println ("Bezier/Bernstein case:");
	bezierTest();
	raiseTest();

	System.out.println("--------");
	System.out.println("Monomial case:");

	UniformIntegerRV degreeRV1 = new UniformIntegerRV(5, true,
							 10, true);
	UniformIntegerRV degreeRV2 = new UniformIntegerRV(1, true,
							 5, true);
	UniformDoubleRV coeffRV = new UniformDoubleRV(-20.0, true, 20.0, true);


	result = new double[20];
	double[] resultb = new double[20];
	double[] resultb2 = new double[20];
	double[] q = new double[20];
	double[] qb = new double[20];
	r = new double[20];
	double[] rb = new double[20];
	p1 = new double[20];
	double[] p1b = new double[20];
	p2 = new double[20];
	double[] p2b = new double[20];
	p3 = new double[21];
	int nb = 0;

	BasicStats statsMult = new BasicStats.Population();
	BasicStats statsAdd = new BasicStats.Population();
	BasicStats statsDiv = new BasicStats.Population();
	BasicStats statsToBezier = new BasicStats.Population();

	double[] p44 = new double[21];
	double[] p66 = new double[21];


	int N = 1000000;
	// For median computation
	int index = 0;
	double[] data = new double[11*N];
	int divcount = 0;
	int[] degstat = new int[6];

	for (int i = 0; i < N; i++) {
	    int deg1 = degreeRV1.next();
	    int deg2 = degreeRV2.next();
	    for (int j = 0; j <= deg1; j++) {
		p1[j] = coeffRV.next();
	    }
	    for (int j = 0; j <= deg2; j++) {
		p2[j] = coeffRV.next();
	    }
	    int deg3 = Polynomials.multiply(p3, p1, deg1, p2, deg2);
	    for (int k = 0; k <= 10; k++) {
		double x = k/10.0;
		double v1 = valueAt(p3, deg3, x);
		double v2 = valueAt(p1, deg1, x)*valueAt(p2, deg2, x);
		double vm = Math.max(Math.abs(v1), Math.abs(v2));
		if (vm < 1.0) vm = 1.0;
		statsMult.add((v1-v2)/vm);
	    }

	    deg3 = Polynomials.add(p3, p1, deg1, p2, deg2);
	    for (int k = 0; k <= 10; k++) {
		double x = k/10.0;
		double v1 = valueAt(p3, deg3, x);
		double v2 = valueAt(p1, deg1, x) + valueAt(p2, deg2, x);
		double vm = Math.max(Math.abs(v1), Math.abs(v2));
		if (vm < 1.0) vm = 1.0;
		statsAdd.add((v1-v2)/vm);
	    }

	    int deg = Polynomials.divide(q, r, p1, deg1, p2, deg2);
	    int rdeg = Polynomials.getDegree(r, deg1);

	    int np44 = Polynomials.multiply(p44, q, deg, p2, deg2);
	    int np66 = Polynomials.add(p66, p44, np44, r, rdeg);

	    if (np66 != deg1) throw new Exception();
	    for (int k = 0; k <= 10.0; k++) {
		double x = k/10.0;
		double v1 = valueAt(p1, deg1, x);
		double v2 = valueAt(p2, deg2, x)*valueAt(q, deg, x)
		    + valueAt(r, rdeg, x);
		double v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		double stat = Math.abs((v1 - v2)/v);
		statsDiv.add(stat);
		data[index++] = stat;
		if (stat > 1.0) {
		    degstat[deg2]++;
		    if (divcount == 0) {
			System.out.println("found stat > 1.0 case:");
			for (int p = 0; p <= deg1; p++) {
			    System.out.println("    p1[" + p + "] = " + p1[p]);
			}
			for (int p = 0; p <= deg2; p++) {
			    System.out.println("    p2[" + p + "] = " + p2[p]);
			}
		    }
		    divcount++;
		}
	    }
	    double denom = 1.0;
	    for (int j = 0; j <= deg1; j++) {
		denom = Math.max(denom, Math.abs(p1[j]));
	    }
	    for (int j = 0; j <= deg2; j++) {
		denom = Math.max(denom, Math.abs(p2[j]));
	    }
	    for (int j = 0; j <= deg; j++) {
		denom = Math.max(denom, Math.abs(q[j]));
	    }
	    for (int j = 0; j <= rdeg; j++) {
		denom = Math.max(denom, Math.abs(r[j]));
	    }

	    for (int k = 0; k < deg1; k++) {
		double c1 = p1[k];
		double c6 = p66[k];
		double value = (c1-c6)/denom;
		if (Math.abs(value) > 1.e-10) {
		    throw new Exception();
		}
	    }

	    Polynomials.toBezier(p1b, 0, p1, deg1);
	    Polynomials.toBezier(p2b, 0, p2, deg2);
	    Polynomials.toBezier(qb, 0, q, deg);
	    Polynomials.toBezier(rb, 0, r, rdeg);
	    for (int k = 0; k <= 10.0; k++) {
		double x = k/10.0;
		double v1 = valueAt(r, rdeg, x);
		double v2 = Functions.Bernstein.sumB(rb, rdeg, x);
		double v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		statsToBezier.add((v1 - v2)/v);
		v1 = valueAt(q, deg, x);
		v2 = Functions.Bernstein.sumB(qb, deg, x);
		v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		statsToBezier.add((v1 - v2)/v);
		v1 = valueAt(p1, deg1, x);
		v2 = Functions.Bernstein.sumB(p1b, deg1, x);
		v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		statsToBezier.add((v1 - v2)/v);
		v1 = valueAt(p2, deg2, x);
		v2 = Functions.Bernstein.sumB(p2b, deg2, x);
		v = Math.max(Math.abs(v1), Math.abs(v2));
		if (v < 1.0) v = 1.0;
		statsToBezier.add((v1 - v2)/v);
		/*
		if (Math.abs((v1 - v2)/v) > 1.e-10) {
		    System.out.println("x = " + x
				       + ", v1 = " + v1 + ", v2 = " + v2);
		    throw new RuntimeException();
		}
		*/
	    }

	    // System.out.println("case " + i);

	    try {
		n = Polynomials.multiply(result, q, deg, p2, deg2);
		nb = Polynomials.bezierMultiply(resultb, qb, deg, p2b, deg2);
	    } catch (Exception e1) {
		System.out.println("deg = " + deg + ", deg2 = " + deg2);
	    }
	    n = Polynomials.add(result, result, n, r, rdeg);
	    nb = Polynomials.bezierAdd(resultb, resultb, nb, rb, rdeg);

	    Polynomials.fromBezier(resultb2, resultb, 0, nb);
	    if (n != deg1) throw new Exception();
	    for (int j = 0; j <= deg1; j++) {
		if (Math.abs((result[j] - p1[j])/denom) > 1.e-12) {
		    System.out.println("error in case " + i + ":");
		    System.out.println("int deg1 = " + deg1 + ";");
		    System.out.println("int deg2 = " + deg2 + ";");
		    System.out.println("double[] p1 = new double[deg1+1];");
		    for (int k = 0; k <= deg1; k++) {
			System.out.println("    p1[" + k + "] = " + p1[k] +";");
		    }
		    System.out.println("double[] p2 = new double[deg2+1];");
		    for (int k = 0; k <= deg2; k++) {
			System.out.println("    p2[" + k + "] = " + p2[k] +";");
		    }
		    System.out.println("result[" + j + "] = " + result[j]
				       + ", p1[" + j + "] = " + p1[j]);
		    throw new Exception();
		}
		if (Math.abs((resultb2[j] - p1[j])/denom) > 1.e-11) {
		    System.out.println("error in bezier case " + i + ":");
		    System.out.println("int deg1 = " + deg1 + ";");
		    System.out.println("int deg2 = " + deg2 + ";");
		    System.out.println("double[] p1 = new double[deg1+1];");
		    for (int k = 0; k <= deg1; k++) {
			System.out.println("    p1[" + k + "] = " + p1[k] +";");
		    }
		    System.out.println("double[] p2 = new double[deg2+1];");
		    for (int k = 0; k <= deg2; k++) {
			System.out.println("    p2[" + k + "] = " + p2[k] +";");
		    }
		    System.out.println("resultb2[" + j + "] = " + resultb2[j]
				       + ", p1[" + j + "] = " + p1[j]);
		    System.out.println("(err = "
				       + Math.abs((resultb2[j] - p1[j])/denom)
				       + ")");
		    throw new Exception();
		}
	    }
	    P1 = new Polynomial(p1, deg1);
	    if (P1.getDegree() != deg1) {
		throw new Exception();
	    }
	    P2 = new Polynomial(p2, deg2);
	    Polynomials.divide(Q, R, P1, P2);
	    Polynomial T = Q.multiply(P2).add(R);
	    if (T.getDegree() != P1.getDegree()) {
		throw new Exception();
	    }
	    if (T.getDegree() != deg1) {
		System.out.println("T.getDegree() = " + T.getDegree()
				   + ", deg1 = " + deg1);
		for (int  j = 0; j <= deg1; j++) {
		    System.out.println(T.getCoefficients()[j] + " <--> "
				       + p1[j]);
		}
		throw new Exception();
	    }
	    double[] result2 = T.getCoefficients();
	    for (int j = 0; j <= deg1; j++) {
		if (Math.abs((result2[j] - p1[j])/denom) > 1.e-12) {
		    throw new Exception();
		}
	    }
	}

	System.out.println("StatsMult: mean = " + statsMult.getMean());
	System.out.println("StatsMult: sdev = " + statsMult.getSDev());
	System.out.println("StatsAdd: mean  = " + statsAdd.getMean());
	System.out.println("StatsAdd: sdev = " + statsAdd.getSDev());
	System.out.println("StatsDiv: mean  = " + statsDiv.getMean());
	System.out.println("StatsDiv: sdev = " + statsDiv.getSDev());
	double median = BasicStats.median(data);
	System.out.println("StatsDiv: median = " + median);
	if (median == 0.0) {
	    for (int i = 0; i < 10; i++) {
		System.out.println("    data[" + i + "] = " + data[i]);
	    }
	    System.out.println("    ...");
	}
	if (divcount > 0) {
	    System.out.println("    divcount = " + divcount);
	    for (int i = 0; i < degstat.length; i++) {
		System.out.println("    degstat[" + i + "] = " + degstat[i]);
	    }
	}
	System.out.println("StatsToBezier: mean  = " + statsToBezier.getMean());
	System.out.println("StatsToBezier: sdev = " + statsToBezier.getSDev());

	double[] p5 = {10.0, 10.0, 10.0, 10.0, 10.0};
	Polynomial P5 = new Polynomial(p5, 4);
	if (Math.abs(P5.valueAt(0.0)-10.0) > 1.e-14) throw new Exception();
	if (Math.abs(P5.valueAt(1.0) - 50.0) > 1.e-14) throw new Exception();
	Polynomial dP5 = P5.deriv();
	if (Math.abs(dP5.valueAt(2.0) - P5.derivAt(2.0)) > 1.e-14) {
	    throw new Exception();
	}
	Polynomial d2P5 = P5.secondDeriv();
	if (Math.abs(d2P5.valueAt(1.0) - P5.secondDerivAt(1.0)) > 1.e-14) {
	    System.out.println("d2P5.valueAt(1.0) = " + d2P5.valueAt(1.0));
	    System.out.println("P5.secondDerivAt(1.0) = "
			       + P5.secondDerivAt(1.0));
	    throw new Exception();
	}
	if (Math.abs(d2P5.valueAt(2.0) - P5.secondDerivAt(2.0)) > 1.e-14) {
	    System.out.println("d2P5.valueAt(2.0) = " + d2P5.valueAt(2.0));
	    System.out.println("P5.secondDerivAt(2.0) = "
			       + P5.secondDerivAt(2.0));
	    throw new Exception();
	}
	Polynomial iP5 = P5.integral();
	Polynomial diP5 = iP5.deriv();
	if (iP5.getDegree() != P5.getDegree() + 1) {
	    throw new Exception();
	}
	double ip5val = 10*2.0 + 10.0*2.0*2.0/2.0 + 10.0*2.0*2.0*2.0/3.0
	    + 10.0*2.0*2.0*2.0*2.0/4.0 + 10.0*2.0*2.0*2.0*2.0*2.0/5.0;
	if (Math.abs(iP5.valueAt(2.0) - ip5val) > 1.e-14) throw new Exception();

	for (int i = 0; i < p5.length; i++) {
	    if (Math.abs(diP5.getCoefficientsArray()[i] - p5[i]) > 1.e-14) {
		throw new Exception();
	    }
	}

	if (Math.abs(iP5.valueAt(3.0) - P5.integralAt(3.0)) > 1.e-14) {
	    throw new Exception();
	}


	double[] p6 = {1.0, 2.0, 3.0};
	Polynomial P6 = new Polynomial(p6);
	P5.incrBy(P6);
	double[] p5a = {11.0, 12.0, 13.0, 10.0, 10.0};
	if (P5.getDegree() != 4) throw new Exception();
	double[] coeff = P5.getCoefficients();
	for (int i = 0; i <= 4; i++) {
	    if (Math.abs(coeff[i] - p5a[i]) > 1.e-14) throw new Exception();
	}
	P5.multiplyBy(2.0);
	if (P5.getDegree() != 4) throw new Exception();
	coeff = P5.getCoefficients();
	for (int i = 0; i <= 4; i++) {
	    if (Math.abs(coeff[i] - 2.0*p5a[i]) > 1.e-14) throw new Exception();
	}
	Polynomial T5 = P5.multiply(P6);
	P5.multiplyBy(P6);
	double[] coeff1 =T5.getCoefficients();
	double[] coeff2 =P5.getCoefficients();
	if (coeff1.length != coeff2.length) {
	    System.out.println("T5 degree = " + T5.getDegree());
	    System.out.println("P5 degree = " + P5.getDegree());
	    System.out.println("T5 coeff len = " + coeff1.length);
	    System.out.println("P5 coeff len  = " + coeff2.length);
	    throw new Exception();
	}
	for (int i = 0;  i < coeff1.length; i++) {
	    if (Math.abs(coeff1[i] - coeff2[i]) > 1.e-14) throw new Exception();
	}

	integralTest();

    }
}
