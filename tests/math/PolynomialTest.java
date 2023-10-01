import org.bzdev.math.*;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;
import org.bzdev.lang.MathOps;



public class PolynomialTest {

    static double a = 0.0;
    static double b = 0.0;
    static double c = 0.0;

    static void testAbsRootQ() throws Exception {
	// partial test: ../geom/Path2DInfoTest.java has a more
	// comprehensive one.
	double value;
	double value2;

	Polynomial p15 = new Polynomial(578.25, -1080.0, 506.25);
	double p15val = Polynomials
	    .integrateRootP2(1.0, 578.25, -1080.0, 506.25);
	double p15val0 = Polynomials
	    .integrateRootP2(0.0, 578.25, -1080.0, 506.25);

	if (Math.abs((p15val - p15val0) - 12.883531828512188)/12 > 1.e-10) {
	    System.out.println("p15val = " + p15val);
	    System.out.println("p15val0 = " + p15val0);
	    System.out.println("diff = " +(p15val - p15val0));
	    throw new Exception();
	}

	Polynomial p = new Polynomial(3.0);
	Polynomial rp = new Polynomial(4.0);
	if (Math.abs(Polynomials.integrateAbsPRootQ(3.0, p, rp) - 18.0)
	    > 1.e-10) {
	    throw new Exception();
	}
	p = new Polynomial(2.0, 3.0);
	Polynomial pi = new Polynomial(0.0, 2.0, 3.0/2);
	value = Polynomials.integrateAbsPRootQ(3.0, p, rp);
	if (Math.abs(value - 2*pi.valueAt(3.0)) > 1.e-10) {
	    System.out.println("value = " + value);
	    throw new Exception();
	}
	p = new Polynomial (4.0, 3.0, 2.0);
	pi = new Polynomial(0.0, 4.0, 3.0/2, 2.0/3);
	value = Polynomials.integrateAbsPRootQ(3.0, p, rp);
	if (Math.abs(value - 2*pi.valueAt(3.0)) > 1.e-10) {
	    System.out.println("value = " + value);
	    throw new Exception();
	}
	Polynomial p1 = new Polynomial(2.0, 3.0);
	Polynomial rp1 = new Polynomial(2.0, 3.0, 4.0);
	GLQuadrature glq = GLQuadrature.newInstance((t) -> {
		return Math.abs(p1.valueAt(t))*Math.sqrt(rp1.valueAt(t));
	}, 8);
	value = Polynomials.integrateAbsPRootQ(3.0, p1, rp1);
	double evalue = glq.integrate(0.0, 3.0, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}
	 Polynomial p2 = new Polynomial(-2.0, 3.0);
	 Polynomial rp2 = new Polynomial(2.0, 3.0, 4.0);
	 glq = GLQuadrature.newInstance((t) -> {
		 return Math.abs(p2.valueAt(t))*Math.sqrt(rp2.valueAt(t));
	}, 8);
	value = Polynomials.integrateAbsPRootQ(3.0, p2, rp2);
	evalue = glq.integrate(0.0, 2.0/3.0, 10)
	    + glq.integrate(2.0/3.0, 3.0, 10);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    double evalue2 = glq.integrate(0.0, 1.5, 100)
		+ glq.integrate(1.5, 3.0, 100);
	    System.out.println("expected value2 = " + evalue2);
	    double evalue3 = glq.integrate(0.0, 1.5, 200)
		+ glq.integrate(1.5, 3.0, 200);
	    System.out.println("expected value3 = " + evalue3);
	    throw new Exception();
	}
	 Polynomial p3 = new Polynomial(-2.0, 3.0);
	 Polynomial rp3 = new Polynomial(5.0);
	 glq = GLQuadrature.newInstance((t) -> {
		 return Math.abs(p3.valueAt(t))*Math.sqrt(rp3.valueAt(t));
	}, 8);
	value = Polynomials.integrateAbsPRootQ(3.0, p3, rp3);
	evalue = glq.integrate(0.0, 2.0/3.0, 100)
	    + glq.integrate(2.0/3.0, 3.0, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}
	Polynomial p4 = new Polynomial(8, -8.0, 2.0);
	Polynomial rp4 = new Polynomial(5.0);
	 glq = GLQuadrature.newInstance((t) -> {
		 return Math.abs(p4.valueAt(t))*Math.sqrt(rp4.valueAt(t));
	}, 8);
	value = Polynomials.integrateAbsPRootQ(3.0, p4, rp4);
	evalue = glq.integrate(0.0, 2.0, 100)
	    + glq.integrate(2.0, 3.0, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}

	Polynomial p5 = new Polynomial(24, -14.0, 2.0);
	Polynomial rp5 = new Polynomial(5.0);
	glq = GLQuadrature.newInstance((t) -> {
		return Math.abs(p5.valueAt(t))*Math.sqrt(rp5.valueAt(t));
	    }, 8);
	value = Polynomials.integrateAbsPRootQ(2.5, p5, rp5);
	evalue = glq.integrate(0.0, 2.5, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}

	value = Polynomials.integrateAbsPRootQ(3.5, p5, rp5);
	evalue = glq.integrate(0.0, 3.0, 100)
	    + glq.integrate(3.0, 3.5, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}
	value = Polynomials.integrateAbsPRootQ(4.5, p5, rp5);
	evalue = glq.integrate(0.0, 3.0, 100)
	    + glq.integrate(3.0, 4.0, 100)
	    + glq.integrate(4.0, 4.5, 100);
	if (Math.abs(value - evalue) > 1.e-10) {
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}
    }


    static void testIntegrateRootP2() throws Exception {

	RealValuedFunctOps integrand = (uu) -> {
	    return Math.sqrt(a + b * uu + c*uu*uu);
	};
	RealValuedFunctOps integrandX = (uu) -> {
	    return uu*Math.sqrt(a + b * uu + c*uu*uu);
	};
	GLQuadrature glq = new GLQuadrature(10) {
		protected double function(double t) {
		    return integrand.valueAt(t);
		}
	    };

	GLQuadrature glqx = new GLQuadrature(10) {
		protected double function(double t) {
		    return integrandX.valueAt(t);
		}
	    };


	double v0, v1, v2;
	double v0x, v1x, v2x;

	System.out.println("... small c test");
	a = 10.0;
	b = 7.0;
	c= .0001;
	v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	System.out.println("v0 = " + v0);
	v1 = Polynomials.integrateRootP2(1.0, a, b, c);
	v1 = v1 - v0;

	v2 = glq.integrate(0, 1.0, 100);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-9) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    System.out.println("v1 - v2 = " + (v1 - v2));
	    throw new Exception();
	}


	a = 10.0;
	b = 1e-5;
	c = 0.0;

	v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	System.out.println("v0 = " + v0);
	v1 = Polynomials.integrateRootP2(1.0, a, b, c);
	v1 = v1 - v0;

	v2 = glq.integrate(0, 1.0, 100);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}

	a = 1017.0;
	b = -4068.0;
	c = 4068.0;
	v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	System.out.println("v0 = " + v0);
	v1 = Polynomials.integrateRootP2(1.0, a, b, c);
	v1 = v1 - v0;

	v2 = glq.integrate(0, 1.0, 100);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}

	a = 40.286798096407296;
	b = -81.39024991061399;
	c = 68.35837614076863;
	v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	System.out.println("v0 = " + v0);
	v1 = Polynomials.integrateRootP2(1.0, a, b, c) - v0;
	v2 = glq.integrate(0, 1.0, 100);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}

	a = 2.0;
	b = 4.0;
	c = 2.0;

	v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	System.out.println("v0 = " + v0);

	v1 = Polynomials.integrateRootP2(1.0, a, b, c) - v0;;

	v2 = glq.integrate(0, 1.0, 100);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}

	double uarray[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
	for (double u: uarray) {
	    v1 = Polynomials.integrateRootP2(u, a, b, c) - v0;
	    v2 = glq.integrate(0.0, u);
	    if (Math.abs(v1 - v2)
		/ Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
		throw new Exception();
	    }
	}

	UniformDoubleRV rv = new UniformDoubleRV(-100.0, true, 100.0, true);
	int icnt = 0;
	BasicStats stats = new BasicStats.Population();
	BasicStats statsx = new BasicStats.Population();
	for (int i = 0; i < 1000000; i++) {
	    a = rv.next();
	    b = rv.next();
	    c = rv.next();
	    if (c == 0.0) continue;
	    if ((a + b + c) < 0.0) continue;
	    if (a < 0.0) continue;
	    double descr = b*b - 4*a*c;
	    double r = Double.POSITIVE_INFINITY;
	    if (Math.abs(descr) < 1.e-10) {
		r = -b/(2*c);
	    }
	    if (descr > 0) {
		double min = -b/(2*c);
		if (min > 0.0 && min < 1.0) continue;
		double rdescr2 = Math.sqrt(descr)/2;
		double r1 = min - rdescr2;
		double r2 = min + rdescr2;
		if (r1 > 0.0 && r1 < 1.0) continue;
		if (r2 > 0.0 && r2 < 1.0) continue;
	    }
	    try {
		v0 = Polynomials.integrateRootP2(0.0, a, b, c);
		v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	    } catch (Exception e) {
		if (e instanceof ArithmeticException) {
		    continue;
		} else {
		    throw e;
		}
	    }
	    for (double u: uarray) {
		boolean badvx = false;
		try {
		    v1 = Polynomials.integrateRootP2(u, a, b, c) - v0;
		    v1x = Polynomials.integrateXRootP2(u, a, b, c);
		    badvx = Math.abs(v1x - v0x)
			/ Math.max(Math.abs(v1x), Math.abs(v0x)) < 1.e-8;
		    v1x -= v0x;
		    if (badvx) continue;
		} catch (Exception e) {
		    if (e instanceof ArithmeticException) {
			continue;
		    } else {
			System.out.println("i = " + i);
			System.out.println(e.getMessage() + ": "
					   + "a = " + a
					   + ", b = " +b
					   + ", c = " +c);
			System.out.println("u = " + u);
			throw e;
		    }
		}
		if (Double.isNaN(v1)) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1 = NaN");
		    throw new Exception();
		}
		if (Double.isNaN(v1x)) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1x = NaN");
		    throw new Exception();
		}
		v2 = ((0 < r) && (r < u))? (glq.integrate(0.0, r, 50)
			       + glq.integrate(r, u, 50)):
		    glq.integrate(0.0, u, 100);
		double delta = Math.abs(v1 - v2)
		    / Math.max(1.0, Math.max(Math.abs(v1), Math.abs(v2)));
		if (delta > 1.e-2) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
		stats.add(delta);
		v2x = ((0 < r) && (r < u))? (glqx.integrate(0.0, r, 50)
			       + glqx.integrate(r, u, 50)):
		    glqx.integrate(0.0, u, 100);
		delta = Math .abs(v1x - v2x)
		    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
		if (delta > 1.e-2) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
		    throw new Exception();
		}
		statsx.add(delta);
	    }
	    icnt++;
	    c = 0.0;
	    if (a < 0.0 ||  a+b < 0.0) continue;
	    v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	    v0x =  Polynomials.integrateXRootP2(0.0, a, b, c);
	    for (double u: uarray) {
		boolean badvx = false;
		v1 = Polynomials.integrateRootP2(u, a, b, c) - v0;
		v2 = ((0 < r) && (r < u))? (glq.integrate(0.0, r, 50)
			       + glq.integrate(r, u, 50)):
		    glq.integrate(0.0, u, 100);
		if (Math.abs(v1 - v2)
		    / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-2) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
		v1x = Polynomials.integrateXRootP2(u, a, b, c);
		if (Math.abs(v1x - v0x)/Math.max(Math.abs(v1x), Math.abs(v0x))
		    < 1.e-18) continue;
		v1x -= v0x;
		v2x = ((0 < r) && (r < u))? (glqx.integrate(0.0, r, 50)
			       + glqx.integrate(r, u, 50)):
		    glqx.integrate(0.0, u, 100);
		if (Math.abs(v1x - v2x)
		    / Math.max(Math.abs(v1x), Math.abs(v2x)) > 1.e-2) {
		    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
		    throw new Exception();
		}
	    }
	    b  = 0.0;
	    if (a < 0.0) continue;
	    v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	    v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	    for (double u: uarray) {
		v1 = Polynomials.integrateRootP2(u, a, b, c) - v0;
		v2 = ((0 < r) && (r < u))? (glq.integrate(0.0, r, 50)
			       + glq.integrate(r, u, 50)):
		    glq.integrate(0.0, u, 100);
		if (Math.abs(v1 - v2)
		    / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-2) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
		v1x = Polynomials.integrateXRootP2(u, a, b, c);
		if (Math.abs(v1x - v0x)/Math.max(Math.abs(v1x), Math.abs(v0x))
		    < 1.e-8) continue;
		v1x -= v0x;
		v2x = ((0 < r) && (r < u))? (glqx.integrate(0.0, r, 50)
			       + glqx.integrate(r, u, 50)):
		    glqx.integrate(0.0, u, 100);
		if (Math.abs(v1x - v2x)
		    / Math.max(Math.abs(v1x), Math.abs(v2x)) > 1.e-2) {
		    System.out.println("i = " + i);
		    System.out.println("a = " + a);
		    System.out.println("b = " + b);
		    System.out.println("c = " + c);
		    System.out.println("u = " + u);
		    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
		    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
		    throw new Exception();
		}
	    }
	}
	System.out.println("icnt = " + icnt);
	if (stats.size() == 0) {
	    System.out.println("no values of delta");
	} else {
	    System.out.println("delta = " + stats.getMean()
			       + " \u00B1 " + stats.getSDev());
	}
	if (statsx.size() == 0) {
	    System.out.println("no values of deltax");
	} else {
	    System.out.println("deltax = " + statsx.getMean()
			       + " \u00B1 " + statsx.getSDev());
	}
    }


    static void integralTest() throws Exception {


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
	BezierPolynomial dyduIntegral  = dydu.integral();
	double[]  bpyArray = bpy.getCoefficientsArray();
	double [] dyduIntegralArray = dyduIntegral.getCoefficientsArray();
	double diff = bpyArray[0] - dyduIntegralArray[0];
	for (int i = 0; i < bpyArray.length; i++) {
	    double delta = Math.abs((bpyArray[i] - dyduIntegralArray[i])
				    - diff);
	    if (delta > 1.e-10) {
		System.out.println("delta = " + delta);
		System.out.println("bpy.getDegree() = " + bpy.getDegree());
		System.out.println("dydu.getDegree() = " + dydu.getDegree());
		System.out.println("dyduIntegral.getDegree() = "
				   + dyduIntegral.getDegree());
		System.out.println("bpyArray[" + i +"] = " +bpyArray[i]);
		System.out.println("dyduIntegralArray[" + i +"] = "
				   +dyduIntegralArray[i]);
		System.out.println("dyduIntegralArray[" + i +"] + diff = "
				   +(dyduIntegralArray[i] + diff));
		System.out.println("diff = " + diff);
		throw new Exception();
	    }
	}
	double args[] = {0.0, 0.1, 0.2, 0.5, 0.8, 1.0};
	boolean err = false;
	for (double u: args) {
	    double val1 = dydu.integralAt(u);
	    double val2 = dyduIntegral.valueAt(u);
	    if (Math.abs(val1 - val2) > 1.e-10) {
		System.out.println("u = " + u + ", val1 = " + val1
				   + ", val2 = " + val2);
		err = true;
	    }
	}
	if (err) throw new Exception();

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

    static void printArray(String name, double[] array) {
	System.out.print(name + ":");
	for (double v: array) {
	    System.out.print(" " + v);
	}
	System.out.println();
    }
    static void printArray(String name, Polynomial p) {
	printArray(name, p.getCoefficientsArray());
    }


    static void badcase() throws Exception {

	Polynomial Px = new Polynomial(360.0, -3600.0, 15120.0,
				       -30600.0, 26010.0);

	Polynomial[] Pxs = Polynomials.factorQuarticToQuadratics(Px);
	Polynomial Pt = new Polynomial(1.0);
	for (int i = 0; i < Pxs.length; i++) {
	    Pt.multiplyBy(Pxs[i]);
	}
	printArray("Px", Px);
	printArray("Pxs[0]", Pxs[0]);
	printArray("Pxs[1]", Pxs[1]);
	printArray("Pxs[2]", Pxs[2]);
	printArray("Pt", Pt);


	double len = Polynomials.integrateRootP4(Px, 0.0, 0.4);
	if (Double.isNaN(len)) {
	    throw new Exception();
	}

	double a1, a2, b1, b2, c1, c2, val, eval;
	Polynomial tp;

	double v0x, v1x, v2x, u, delta;
	RealValuedFunctOps integrandX = (uu) -> {
	    return uu*Math.sqrt(a + b * uu + c*uu*uu);
	};
	GLQuadrature glqx = new GLQuadrature(10) {
		protected double function(double t) {
		    return integrandX.valueAt(t);
		}
	    };

	double val0, val1, val2;
	RealValuedFunctOps integrand = (uu) -> {
	    return Math.sqrt(a + b * uu + c*uu*uu);
	};
	GLQuadrature glq = new GLQuadrature(10) {
		protected double function(double t) {
		    return integrand.valueAt(t);
		}
	    };

	a = 93.01889004986154;
	b = -55.53800275486001;
	c = 5.92765100548894;
	u = 0.1;
	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	if (v0x != 0.0) throw new Exception("v0x = " + v0x);
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);
	if (Double.isNaN(v1x)) throw new Exception();
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}

	a = 73.79073403467174;
	b = -4.196333275388042E-4;
	c = -4.502589249849706;
	u = 0.1;
	val0 = Polynomials.integrateRootP2(0.0, a, b, c);
	val1 = Polynomials.integrateRootP2(u, a, b, c);
	System.out.println("val0 = " +val0 +", val1 = " + val1);
	val1 -= val0;
	System.out.println("val1 = " + val1);
	val2 = glq.integrate(0.0, u, 100);
	System.out.println("val2 = " + val2);
	delta = Math .abs(val1 - val2)
	    / Math.max(1.0, Math.max(Math.abs(val1), Math.abs(val2)));
	if (delta > 1.e-6) {
	    System.out.println("delta = " + delta);
	    System.out.println("val1 = " + val1 + ", val2 = " + val2);
	    throw new Exception();
	}
	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	if (v0x != 0.0) throw new Exception("v0x = " + v0x);
	System.out.println("... compute v1x");
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}

	a = 52.67218051744058;
	b = 89.09876633418148;
	c = 4.0758807805153765;
	u = 0.2;
	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	if (v0x != 0.0) throw new Exception("v0x = " + v0x);
	System.out.println("... compute v1x");
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}




	a = 5.655490196608142;
	b = 26.88177275582447;
	c = 0.020245158929668605;
	u = 0.8;
	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	if (v0x != 0.0) throw new Exception("v0x = " + v0x);
	System.out.println("... compute v1x");
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}

	a = 2.0;
	b = 3.0;
	c = 4.0;
	val0 = Polynomials.integrateRootP2(0.0, a, b, c);
	val1 = Polynomials.integrateRootP2(3.0, a, b, c);
	System.out.println("val0 = " +val0 +", val1 = " + val1);
	val1 -= val0;
	System.out.println("val1 = " + val1);
	val2 = glq.integrate(0.0, 3.0, 100);
	System.out.println("val2 = " + val2);
	delta = Math .abs(val1 - val2)
	    / Math.max(1.0, Math.max(Math.abs(val1), Math.abs(val2)));
	if (delta > 1.e-6) {
	    System.out.println("delta = " + delta);
	    System.out.println("val1 = " + val1 + ", val2 = " + val2);
	    throw new Exception();
	}
	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	if (v0x != 0.0) throw new Exception("v0x = " + v0x);
	v1x = Polynomials.integrateXRootP2(3.0, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, 3.0, 100);
	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}

	a = 101907.61256434761;
	b = -247290.7579205503;
	c = 150020.00218176623;

	val0 = Polynomials.integrateRootP2(0.0, a, b, c);
	val1 = Polynomials.integrateRootP2(1.0, a, b, c);
	System.out.println("val0 = " +val0 +", val1 = " + val1);
	val1 -= val0;
	System.out.println("val1 = " + val1);
	val2 = glq.integrate(0.0, 1.0, 100);
	System.out.println("val2 = " + val2);
	delta = Math .abs(val1 - val2)
	    / Math.max(1.0, Math.max(Math.abs(val1), Math.abs(val2)));
	if (delta > 1.e-6) {
	    System.out.println("delta = " + delta);
	    System.out.println("val1 = " + val1 + ", val2 = " + val2);
	    throw new Exception();
	}


	a = 85.55827757758047;
	b = 3.0217228129458817E-5;
	c = 0.0;
	u = 0.1;
	System.out.println("root = " + (-a/b));

	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);

	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);


	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-9) {
	    System.out.println("delta = " + delta);
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}


	System.out.println("a = 7.5...., b = 74.4..., c= 3.2...e-5");
	a = 7.5108446888724245;
	b = 74.49868380335275;
	c = 8.255099176324165E-5;
	u = 1.0;
	double descrx = b*b - 4*a*c;
	System.out.println("descr = " + descrx);
	System.out.println("root = " + (-b + Math.sqrt(descrx)));

	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	v1x = Polynomials.integrateXRootP2(u, a, b, c);
	System.out.println("v0x = " +v0x +", v1x = " + v1x);

	v1x = v1x - v0x;
	v2x = glqx.integrate(0.0, u, 100);
	System.out.println("final v1x = " + v1x);
	System.out.println("v2x = " + v2x);


	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-2) {
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	}


	a = 90.34082621137378;
	b = -86.8539279211837;
	c = 4.89958642404531E-4;
	u = 1.0;

	v0x = Polynomials.integrateXRootP2(0.0, a, b, c);
	v1x = Polynomials.integrateXRootP2(u, a, b, c) - v0x;
	v2x = glqx.integrate(0.0, u);

	delta = Math .abs(v1x - v2x)
	    / Math.max(1.0, Math.max(Math.abs(v1x), Math.abs(v2x)));
	if (delta > 1.e-2) {
	    System.out.println("v1x = " + v1x + ", v2x = " + v2x);
	    throw new Exception();
	}

	a = 77.65472037143329;
	b = -9.645253430112476;
	c = 0.2744434748120739;

	// X = a + bx + cx^2  (CRC table integral of 1/sqrt(X)


	double descr1 = b*b - 4*a*c;
	double rdescr1 = Math.sqrt(descr1);
	System.out.println("sqrt(descr1) = " + rdescr1);
	double r1 = -b/(2*c) - rdescr1/2;
	double r2 = -b/(2*c) + rdescr1/2;
	System.out.println("r1 = " + r1);
	System.out.println("r2 = " + r2);
	System.out.println("q = " +(4*a*c - b*b));
	RealValuedFunctOps X1 = (z) -> {
	   return  a + b*z + c*z*z;
	};
	System.out.println ("X(0) = " + X1.valueAt(0.0));
	System.out.println ("X(1) = " + X1.valueAt(1.0));
	double argval0 =  Math.sqrt(X1.valueAt(0.0)) + b/(2*Math.sqrt(c));
	double argval1 = Math.sqrt(X1.valueAt(1.0))
	    + 1.0*Math.sqrt(c) + b/(2*Math.sqrt(c));
	System.out.println("at 0, sqrt(X) = xsqrt(c) + b/(2sqrt(c)) = "
			   + argval0);
	System.out.println("at 1, sqrt(X) = xsqrt(c) + b/(2sqrt(c)) = "
			   + argval1);
	if ((argval1 <= 0.0) != (argval0 <= 0.0)) throw new Exception();
	try {
	double v0 = Polynomials.integrateRootP2(0.0, a, b, c);
	double v1 = Polynomials.integrateRootP2(1.0, a, b, c);
	System.out.println("v0 = " + v0);
	System.out.println("v1 = " + v1);
	if (Double.isNaN(v0)) throw new Exception();
	if (Double.isNaN(v1)) throw new Exception();
	} catch (ArithmeticException ea) {
	    System.out.println(ea.getMessage());
	    System.out.println("... expected an error");
	}

	/*
	Polynomial poly1 = new Polynomial(10.083251953125,
					-51.5654296875,
					66.89794921875,
					-2.4873046875,
					0.028564453125);
	*/

	Polynomial[] polys = {
	    new Polynomial(360.0, -3600.0, 15120.0, -30600.0, 26010.0),
	    new Polynomial(1091.25, -4545.0, 8373.375, -6483.375, 1732.78125),
	    new Polynomial(169.03125, -317.25, -680.0625,
			   447.74999999999994, 1732.78125),
	    new Polynomial(2592.0, -18576.0, 38916.0, -20268.0, 3330.0),
	    new Polynomial(10.083251953125,
			   -51.5654296875,
			   66.89794921875,
			   -2.4873046875,
			   0.028564453125),

	    new Polynomial(6165.000000000001,
			   -34560.0,
			   64404.0,
			   -45648.0,
			   12321.0)
	};

	int index = 0;
	for (Polynomial poly1: polys) {
	    System.out.println("poly1 = polys[" +(index++) + "]");
	    try {
		System.out.println("integrateRootP4(poly1) = "
				   + Polynomials.integrateRootP4(poly1,
								 0.0, 1.0));
		final Polynomial ipoly = poly1;
		GLQuadrature iglq = new GLQuadrature(8) {
			protected double function(double t) {
			    return Math.sqrt(ipoly.valueAt(t));
			}
		    };
		System.out.println("integrateRootP4(poly1) numeric = "
				   + iglq.integrate(0.0, 1.0, 400));

		Polynomial[] poly1s = Polynomials
		    .factorQuarticToQuadratics(poly1);
		System.out.println("poly1s.length = " + poly1s.length);
		printArray("poly1s[0]", poly1s[0]);
		printArray("poly1s[1]", poly1s[1]);
		printArray("poly1s[2]", poly1s[2]);
		Polynomial prod = poly1s[0].multiply(poly1s[1])
		    .multiply(poly1s[2]);
		printArray("prod", prod);
		for (int i = 1; i < 3; i++) {
		    double[] array = poly1s[i].getCoefficientsArray();
		    double a = array[2];
		    double b = array[1];
		    double c = array[0];
		    double descr = b*b - 4*a*c;
		    System.out.println("roots for poly1s["+i+"]");
		    System.out.println("... descr = " + descr
				       + ", min value = "
				       + poly1s[i].valueAt((-b/(2*a))));
		    if (descr == 0) {
			System.out.println("... r = " + -b/(2*c));
		    } else if (descr > 0) {
			double rdescr = Math.sqrt(descr);
			System.out.println("r1 = " + ((-b - rdescr)/(2*a)));
			System.out.println("r2 = " + ((-b + rdescr)/(2*a)));
		    } else {
			System.out.println("... no roots");
		    }
		}
	    } catch (ArithmeticException e) {
		System.out.println("... not integrable (expected)");
	    }
	}

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

    public static void factorTest() throws Exception {

	// This failed during a subsequent test at one point.
	System.out.println("a1 = 9.0, b1 = 2.0, c1 = 10.0");
	System.out.println("a2 = 9.0, b2 = 2.0, c2 = 10.0");
	System.out.println("factor "
			   + "Polynomial(100.0, 40.0, 184.0, 36.0, 81.0)");
	Polynomial p = new Polynomial(100.0, 40.0, 184.0, 36.0, 81.0);
	Polynomial[] factors = Polynomials.factorQuarticToQuadratics(p);
	if (factors.length < 3) throw new Exception();

	System.out.println("factor "
			   + "Polynomial(225.0, 180.0, 366.0, 132.0, 121.0)");
	p = new Polynomial(225.0, 180.0, 366.0, 132.0, 121.0);
	factors = Polynomials.factorQuarticToQuadratics(p);
	if (factors.length < 3) throw new Exception();
    }

    // To test Carlson's integral table.


    static class Parms {
	Polynomial P1;
	Polynomial P2;
	Polynomial P5;
	int p1;
	int p2;
	int p5;

	public void set(int p1, int p2, int p5,
		     Polynomial P1,
		     Polynomial P2,
		     Polynomial P5)
	{
	    this.p1 = p1;
	    this.p2 = p2;
	    this.p5 = p5;
	    this.P1 = P1;
	    this.P2 = P2;
	    this.P5 = P5;
	}

	public Parms() {
	}

	public Parms(int p1, int p2, int p5,
		     Polynomial P1,
		     Polynomial P2,
		     Polynomial P5)
	{
	    set(p1, p2, p5, P1, P2, P5);
	}
    }

    public static void initialCarlsonTest() {
	Polynomial P1 = new Polynomial(2.7, -1.8, 0.9);
	Polynomial P2 = new Polynomial(2.0, 2.4, 0.8);
	Polynomial P3 = new Polynomial(1.1, -0.4);

	double val = Polynomials.integrateRoot2Q(1, 1, -4, P1, P2, P3,
						 -3.0, 2.0);
	System.out.println("value = " + val);
    }


    private static void timingTestP4() {

	//  discontinuous 1st deriv test.
	GLQuadrature glql = GLQuadrature.newInstance(Math::abs, 8);
	for (int i = 1; i <= 10; i++) {
	    System.out.format("glql.integrate(-1.0, 1.0, %d) = %s\n",
			      i, glql.integrate(-1.0, 1.0, i));
	}


	// compare use ofo elliptic integrals to numberic intergration.

	// neither have real roots.
	Polynomial p1 = new Polynomial(4.0, 3.0, 100.0);
	Polynomial p2 = new Polynomial(3.0, 2.0, 200.0);

	p1.multiplyBy(p1);
	p2.multiplyBy(p2);
	Polynomial p = new Polynomial(p1);
	p.incrBy(p2);

	RealValuedFunctOps f = (x) -> {
	    return Math.sqrt(p.valueAt(x));
	};

	GLQuadrature glq = GLQuadrature.newInstance(f, 8);

	long t0 = System.nanoTime();
	int limit = 100000;

	int index = 2;
	double val = glq.integrate(0.0, 1.0, index++);

	for (;;) {
	    double tmp = glq.integrate(0.0, 1.0, index);
	    if (tmp == val) break;
	    val = tmp;
	    index++;
	}
	System.out.println("number of subdivisions = " + index);

	int scaleFactor = 4;

	double sum = 0.0;
	double val1 = 0.0;
	double val2 = 0.0;
	double val3 = 0.0;
	for (int i = 0; i < limit; i++) {
	    val1 = Polynomials.integrateRootP4(p, 0.0, 1.0);
	    sum += val1;
	}
	long t1 = System.nanoTime();
	for (int i = 0; i < limit; i++) {
	    val2 = glq.integrate(0.0, 1.0, index);
	    sum += val2;
	}
	long t2 = System.nanoTime();
	for (int i = 0; i < limit; i++) {
	    Polynomials.factorQuarticToQuadratics(p);
	}
	long t3 = System.nanoTime();
	sum = 0.0;
	for (int i = 0; i < limit; i++) {
	    val3 = glq.integrate(0.0, 1.0, index*scaleFactor);
	    sum += val3;
	}
	long t4 = System.nanoTime();

	System.out.println("time1 = " + (t1 - t0));
	System.out.println("time2 = " + (t2 - t1));
	System.out.println("time3 = " + (t3 - t2));
	System.out.println("time4 = " + (t4 - t3));
	System.out.println("val1 = " + val1);
	System.out.println("val2 = " + val2);
	System.out.println("val3 = " + val2);
    }

    public static void testApprox() throws Exception {

	double a = 7.0;
	double b = 3.0;
	// double c = 2.0;
	double c = 0.001;

	RealValuedFunctOps integral1 = (x) -> {
	    return (2.0/(3*b))*MathOps.pow(a+b*x, 3, 2)
	    + (c/(15*b*b*b))*(8*a*a - 4*a*b*x + 3*b*b*x*x)*Math.sqrt(a+b*x)
	    - (2.0/(3*b)*MathOps.pow(a, 3, 2)
	       + (c/(15*b*b*b))*8*a*a*Math.sqrt(a));
	};


	RealValuedFunctOps integral = (x) -> {
	    double z = x*b/a;
	    double term1 = (2*MathOps.pow(a,3,2)/(3*b)
			    + 8*c*MathOps.pow(a,5,2)/(15*b*b*b))
		* (Math.sqrt(1+z) - 1.0);
	    double term2 = ((2*Math.sqrt(a)/3
			     - 4*MathOps.pow(a, 3, 2)*c/(15*b*b)) * x
			    + 3*c*x*x*Math.sqrt(a)/(15*b))
		* Math.sqrt(1+z);

	    return term1 + term2;
	};

	GLQuadrature glq = GLQuadrature.newInstance((x) -> {
		return Math.sqrt(a+b*x) + (c/2.0)*x*x/Math.sqrt(a+b*x);
	    }, 8);

	System.out.println("integral(0) = " +integral.valueAt(0.0));
	System.out.println("integral1 = " +integral1.valueAt(0.8));
	System.out.println("integral = " +integral.valueAt(0.8));

	System.out.println("integral (numeric) = "
			   + glq.integrate(0.0, 0.8, 100));
	System.out.println("integral (numeric2) = "
			   + glq.integrate(0.0, 0.8, 200));

	integral1 = (x) -> {
	    return -2*(2*a - 3*b*x)*(a+b*x)*Math.sqrt(a+b*x)/(15*b*b)
	    + (c/2)*(2*x*x*x/(7*b)
		     - (6*a/(7*b))*(2*(8*a*a-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
		   * Math.sqrt(a+b*x)
	    - (-2*2*a*a/(15*b*b) + (c/2)*(-(6*a)/(7*b))*(2*8*a*a)/(15*b*b*b))
	      * Math.sqrt(a);
	};

	RealValuedFunctOps integral2 = (x) -> {
	    return -2*(2*a*a -a*b*x-3*b*b*x*x)*Math.sqrt(a+b*x)/(15*b*b)
	    + (c/2)*(2*x*x*x/(7*b)
		     - (6*a/(7*b))*(2*(8*a*a-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
		   * Math.sqrt(a+b*x)
	    - (-2*2*a*a/(15*b*b) + (c/2)*(-(6*a)/(7*b))*(2*8*a*a)/(15*b*b*b))
	      * Math.sqrt(a);
	};

	RealValuedFunctOps integral3 = (x) -> {
	    return -2*(2*a*a -a*b*x-3*b*b*x*x)*Math.sqrt(a+b*x)/(15*b*b)
	    + (c)*(x*x*x/(7*b)
		     - (6*a/(7*b))*((8*a*a-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
		   * Math.sqrt(a+b*x)
	    - (-2*2*a*a/(15*b*b) + (c)*(-(6*a)/(7*b))*(8*a*a)/(15*b*b*b))
	      * Math.sqrt(a);
	};

	RealValuedFunctOps integral4 = (x) -> {
	    double z = b*x/a;
	    return -2*(2*a*a -a*b*x-3*b*b*x*x)*Math.sqrt(a)*Math.sqrt(1+z)
	             /(15*b*b)
	    + (c)*(x*x*x/(7*b)
		     - (6*a/(7*b))*((8*a*a-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
		* Math.sqrt(a)*Math.sqrt(1+z)
	    - (-2*2*a*a/(15*b*b)*Math.sqrt(a)
	       + (c)*(-(6*a)/(7*b))*(8*a*a)*Math.sqrt(a)/(15*b*b*b));

	};

	RealValuedFunctOps integral5 = (x) -> {
	    double z = b*x/a;
	    return -2*(2*a*a*Math.sqrt(a)/(15*b*b)
		       - (a*b*x+3*b*b*x*x)*Math.sqrt(a)/(15*b*b))
			* Math.sqrt(1+z)
	    - (c)*(6*a/(7*b))*((8*a*a)/(15*b*b*b))* Math.sqrt(a)*Math.sqrt(1+z)
	    + (c)*(x*x*x/(7*b)
		     - (6*a/(7*b))*((-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
		* Math.sqrt(a)*Math.sqrt(1+z)
	    - (-2*2*a*a/(15*b*b)*Math.sqrt(a)
	       + (c)*(-(6*a)/(7*b))*(8*a*a)*Math.sqrt(a)/(15*b*b*b));
	};

	RealValuedFunctOps integral6 = (x) -> {
	    double z = b*x/a;
	    double term1 = (-2*(2*a*a*Math.sqrt(a)/(15*b*b))
			    - (c)*(6*a/(7*b))*((8*a*a)/(15*b*b*b))
			    * Math.sqrt(a))*Math.sqrt(1+z);
	    double term2 =  (-2*(-(a*b*x+3*b*b*x*x)*Math.sqrt(a)/(15*b*b))
	               + (c)*(x*x*x/(7*b) - (6*a/(7*b))
			      *((-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
			     * Math.sqrt(a))*Math.sqrt(1+z);
	    double term3 = - (-2*2*a*a/(15*b*b)*Math.sqrt(a)
	       + (c)*(-(6*a)/(7*b))*(8*a*a)*Math.sqrt(a)/(15*b*b*b));
	    return term1 + term2 + term3;
	};

	integral = (x) -> {
	    double z = b*x/a;
	    double term1 = (-2*(2*a*a*Math.sqrt(a)/(15*b*b))
			    - (c)*(6*a/(7*b))*((8*a*a)/(15*b*b*b))
			    * Math.sqrt(a))*(Math.sqrt(1+z) - 1);
	    double term2 =  (-2*(-(a*b*x+3*b*b*x*x)*Math.sqrt(a)/(15*b*b))
	               + (c)*(x*x*x/(7*b) - (6*a/(7*b))
			      *((-4*a*b*x + 3*b*b*x*x)/(15*b*b*b)))
			     * Math.sqrt(a))*Math.sqrt(1+z);
	    return term1 + term2;
	};

	glq = GLQuadrature.newInstance((x) -> {
		return x*(Math.sqrt(a+b*x) + (c/2.0)*x*x/Math.sqrt(a+b*x));
	    }, 8);

	GLQuadrature glq2 = GLQuadrature.newInstance((x) -> {
		return x*Math.sqrt(a + b*x + c*x*x);
	    }, 8);

	System.out.println("multipy integrand by x");
	System.out.println("integral1 = " +integral1.valueAt(0.8));
	System.out.println("integral2 = " +integral2.valueAt(0.8));
	System.out.println("integral3 = " +integral3.valueAt(0.8));
	System.out.println("integral4 = " +integral4.valueAt(0.8));
	System.out.println("integral5 = " +integral5.valueAt(0.8));
	System.out.println("integral6 = " +integral6.valueAt(0.8));
	System.out.println("integral = " + integral.valueAt(0.8));

	System.out.println("integral (numeric) = "
			   + glq.integrate(0.0, 0.8, 100));
	System.out.println("integral (numeric2) = "
			   + glq.integrate(0.0, 0.8, 200));
	System.out.println("glq2 case = " +glq2.integrate(0.0, 0.8, 200));

    }



    public static void main(String argv[]) throws Exception {
	if (false) {
	    timingTestP4();
	    System.exit(0);
	}

	testApprox();

	if (false) {
	    Polynomials.setRootP4SFLimit(0.1);
	    double delta = 1.e-7;
	    Polynomial dp1 =new Polynomial(0.1176470588235294,
					   -0.5882352941176471,
					   1.0);
	    Polynomial dp2 =new Polynomial(0.1176470588235294 + delta,
					   -0.5882352941176471 + delta,
					   1.0);
	    final Polynomial dp = dp1.multiply(dp2);
	    GLQuadrature dpq = GLQuadrature.newInstance((u) -> {
		    return Math.sqrt(dp.valueAt(u));
		}, 8);

	    double dplen1 = Polynomials.integrateRootP4(dp, 0.0, 1.0);
	    double dplen2 = dpq.integrate(0.0, 1.0, 200);
	    System.out.println("dplen1 = " + dplen1);
	    System.out.println("dplen2 = " + dplen2);
	    System.exit(0);
	}



	if (true) badcase();

	testAbsRootQ();

	// initialCarlsonTest();
	Polynomial pt = new Polynomial(8.0, -4.0, 2.0);
	pt.multiplyBy(2.0);
	System.out.print("pt:");
	for (double v: pt.getCoefficientsArray()) {
	    System.out.print(" " + v);
	}
	System.out.println();

        factorTest();
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

	System.out.println("shift test");
	Polynomial PS = new Polynomial(0.7, 0.3);
	Polynomial PSa = new Polynomial(PS);
	PSa.shift(1.5);
	for (int i = 0; i < 20; i++) {
	    double x = ((double)i)/10.0;
	    if (Math.abs(PS.valueAt(x + 1.5) - PSa.valueAt(x)) > 1.e-10) {
		System.out.println("x = " + x
				   + ", val1 = " + PS.valueAt(x + 1.5)
				   + ", val2 = " + PSa.valueAt(x));
		throw new Exception();
	    }
	}

	PS = new Polynomial(0.7, 0.3, 2.3, 4.4, 5.5);
	PSa = new Polynomial(PS);
	PSa.shift(1.5);
	for (int i = 0; i < 20; i++) {
	    double x = ((double)i)/10.0;
	    if (Math.abs(PS.valueAt(x + 1.5) - PSa.valueAt(x)) > 1.e-10) {
		System.out.println("x = " + x
				   + ", val1 = " + PS.valueAt(x + 1.5)
				   + ", val2 = " + PSa.valueAt(x));
		throw new Exception();
	    }
	}
	double shift = PS.reducedFormShift();
	double sf = PS.getCoefficientsArray()[PS.getDegree()];
	PS.shift(shift).multiplyBy(1/sf);
	double[] PSArray = PS.getCoefficients();
	if (Math.abs(PSArray[PSArray.length-2]) > 1.e-10) {
	    throw new Exception();
	}
	if (Math.abs(PSArray[PSArray.length-1] - 1.0) > 1.e-10) {
	    throw new Exception();
	}

	System.out.println("factor Polynomial(2.0, -3.0, -12.0, 0.0, 1.0)");
	Polynomial p = new Polynomial(2.0, -3.0, -12.0, 0.0, 1.0);
	Polynomial[] ps = Polynomials.factorReducedQuartic(p);
	if (ps == null) throw new Exception();
	System.out.print("ps[0]: ");
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    System.out.print(ps[0].getCoefficientsArray()[i] + " ");
	}
	System.out.println();
	System.out.print("ps[1]: ");
	for (int i = 0; i <= ps[1].getDegree(); i++) {
	    System.out.print(ps[1].getCoefficientsArray()[i] + " ");
	}
	System.out.println();
	ps[0].multiplyBy(ps[1]);
	System.out.print("ps[0]*ps[1]: ");
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    System.out.print(ps[0].getCoefficientsArray()[i] + " ");
	}
	if (p.getDegree() != ps[0].getDegree()) throw new Exception();
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    if (Math.abs(ps[0].getCoefficientsArray()[i]
			 - p.getCoefficientsArray()[i]) > 1.e-10) {
		throw new Exception();
	    };
	}

	System.out.println();

	System.out.println("factor Polynomial(-8.0, -19.0, 22.0, -8.0, 1.0)");
	p = new Polynomial(-8.0, -19.0, 22.0, -8.0, 1.0);
	Polynomial pr = new Polynomial(p);
	shift = pr.reducedFormShift();
	pr.shift(shift);
	ps = Polynomials.factorReducedQuartic(pr);
	ps[0].shift(-shift);
	ps[1].shift(-shift);

	System.out.print("ps[0]: ");
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    System.out.print(ps[0].getCoefficientsArray()[i] + " ");
	}
	System.out.println();
	System.out.print("ps[1]: ");
	for (int i = 0; i <= ps[1].getDegree(); i++) {
	    System.out.print(ps[1].getCoefficientsArray()[i] + " ");
	}
	System.out.println();
	ps[0].multiplyBy(ps[1]);
	System.out.print("ps[0]*ps[1]: ");
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    System.out.print(ps[0].getCoefficientsArray()[i] + " ");
	}
	System.out.println();
	if (p.getDegree() != ps[0].getDegree()) throw new Exception();
	for (int i = 0; i <= ps[0].getDegree(); i++) {
	    if (Math.abs(ps[0].getCoefficientsArray()[i]
			 - p.getCoefficientsArray()[i]) > 1.e-10) {
		throw new Exception();
	    };
	}

	UniformIntegerRV irv = new UniformIntegerRV(-20, true, 20, true);
	UniformDoubleRV drv = new UniformDoubleRV(-20.0, true, 20.0, true);

	int last = 0;
	int ircount = 0;	// irreducible count
	for (int k = 0; k < 1000000; k++) {
	    while (last == 0) last = irv.next();

	    p = new Polynomial(((double)irv.next())/last,
			       ((double)irv.next())/last,
			       ((double) irv.next())/last,
			       ((double) irv.next())/last,
			       1.0);

	    pr = new Polynomial(p);
	    shift = pr.reducedFormShift();
	    pr.shift(shift);
	    ps = Polynomials.factorReducedQuartic(pr);
	    if (ps == null) {
		ircount++;
		continue;
	    }
	    ps[0].shift(-shift);
	    ps[1].shift(-shift);
	    ps[0].multiplyBy(ps[1]);
	    if (p.getDegree() != ps[0].getDegree()) throw new Exception();
	    for (int i = 0; i <= ps[0].getDegree(); i++) {
		if (Math.abs(ps[0].getCoefficientsArray()[i]
			     - p.getCoefficientsArray()[i]) > 1.e-9) {
		    System.out.println(ps[0].getCoefficientsArray()[i]
				       + " != "
				       + p.getCoefficientsArray()[i]);
		    throw new Exception();
		};
	    }
	}
	System.out.println("ircount = " + ircount);
	ircount = 0;
	for (int k = 0; k < 1000000; k++) {
	    p = new Polynomial(drv.next(), drv.next(), drv.next(), drv.next(),
			       1.0);

	    pr = new Polynomial(p);
	    shift = pr.reducedFormShift();
	    pr.shift(shift);
	    ps = Polynomials.factorReducedQuartic(pr);
	    if (ps == null) {
		ircount++;
		continue;
	    }
	    ps[0].shift(-shift);
	    ps[1].shift(-shift);
	    ps[0].multiplyBy(ps[1]);
	    if (p.getDegree() != ps[0].getDegree()) throw new Exception();
	    for (int i = 0; i <= ps[0].getDegree(); i++) {
		if (Math.abs(ps[0].getCoefficientsArray()[i]
			     - p.getCoefficientsArray()[i]) > 1.e-9) {
		    System.out.println(ps[0].getCoefficientsArray()[i]
				       + " != "
				       + p.getCoefficientsArray()[i]);
		    throw new Exception();
		};
	    }
	}
	System.out.println("ircount = " + ircount);
	for (int k = 0; k < 1000000; k++) {
	    double dlast = 0.0;
	    while (Math.abs(dlast) < 1.0) dlast = drv.next();
	    p = new Polynomial(drv.next(), drv.next(), drv.next(), drv.next(),
			       dlast);

	    try {
		ps = Polynomials.factorQuarticToQuadratics(p);
	    } catch (Exception e) {
		double[] parray = p.getCoefficientsArray();
		System.out.format("p: %s, %s, %s, %s, %s\n",
				  parray[0], parray[1], parray[2], parray[3],
				  parray[4]);
		throw e;
	    }
	    for (int i = 1; i < ps.length; i++) {
		ps[0].multiplyBy(ps[i]);
	    }
	    if (p.getDegree() != ps[0].getDegree()) throw new Exception();
	    for (int i = 0; i <= ps[0].getDegree(); i++) {
		double val1 = ps[0].getCoefficientsArray()[i];
		double val2 = p.getCoefficientsArray()[i];
		double denom = Math.max(Math.abs(val1), Math.abs(val2));
		if (denom < 1.0) denom = 1.0;
		if (Math.abs(val1 - val2)/denom > 1.e-9) {
		    System.out.println(ps[0].getCoefficientsArray()[i]
				       + " != "
				       + p.getCoefficientsArray()[i]);
		    // throw new Exception();
		};
	    }
	}
	System.out.println("ircount = " + ircount);

	p = new Polynomial(10.0, 1.0, 1.0);
	p.multiplyBy(new Polynomial(20.0, 2.0, 1.0));

	GLQuadrature<Polynomial> glq = new GLQuadrature<>(8) {
		protected double function(double t, Polynomial pn) {
		    return Math.sqrt(pn.valueAt(t));
		}
	    };

	glq.setParameters(p);
	double integral1 = Polynomials.integrateRootP4(p, 0.2, 0.8);
	System.out.println("integral1 = " + integral1);
	double integral2 = glq.integrate(0.2, 0.8, 10);
	System.out.println("integral2 = " + integral2);

	System.out.println("scale polynomial by 3");
	System.out.println("... expecting integral1 = "
			   + Math.sqrt(3)*integral1);
	System.out.println("... expecting integral2 = "
			   + Math.sqrt(3)*integral2);
	p.multiplyBy(3.0);
	glq.setParameters(p);

	integral1 = Polynomials.integrateRootP4(p, 0.2, 0.8);
	System.out.println("integral1 = " + integral1);
	integral2 = glq.integrate(0.2, 0.8, 50);
	System.out.println("integral2 = " + integral2);

	System.out.println("known test case");
	p = new Polynomial(9.0, 6.0, 6.0, 2.0, 1.0);
	glq.setParameters(p);
	integral1 = Polynomials.integrateRootP4(p, 0.2, 0.8);
	System.out.println("integral1 = " + integral1);
	integral2 = glq.integrate(0.2, 0.8, 50);
	System.out.println("integral2 = " + integral2);

	System.out.println("... random tests");


	double a1, b1, c1, a2, b2, c2;

	int ecount = 0;
	irv = new UniformIntegerRV(-20, true, 20, true);
	for (int i = 0; i < 1000000; i++) {
	    do {
		a1 = irv.next();
		b1 = irv.next();
		c1 = irv.next();
	    } while (a1 < 1.0 || c1 <= 0.0 || b1*b1 >= 4*a1*c1);
	    do {
		a2 = irv.next();
		b2 = irv.next();
		c2 = irv.next();
	    } while (a2 < 1.0 || c2 <= 0.0 || b2*b2 >= 4*a2*c2);
	    p = new Polynomial(c1, b1, a1);
	    p.multiplyBy(new Polynomial(c2, b2, a2));
	    glq.setParameters(p);
	    try {
		integral1 = Polynomials.integrateRootP4(p, 0.2, 0.8);
	    } catch (Exception e) {
		System.out.println("a1 = " + a1
				   + ", b1 = " + b1
				   +", c1 = " + c1);
		System.out.println("a2 = " + a2
				   + ", b2 = " + b2
				   +", c2 = " + c2);
		double darray[] = p.getCoefficientsArray();
		System.out.print("p");
		for (int k = 0; k < 5; k++) {
		    System.out.print(((k == 0)?": ": ", ") + darray[k]);
		}
		System.out.println();
		System.out.println(e.getClass() +": " + e.getMessage());
		ecount++;
		continue;
	    }
	    integral2 = glq.integrate(0.2, 0.8, 100);
	    if (Math.abs(integral1 - integral2) > 1.e-10) {
		System.out.print("p: ");
		double[] parray = p.getCoefficientsArray();
		for (int k = 0; k < 5; k++) {
		    System.out.print(((k == 0)? ": ": ", ") + parray[k]);
		}
		System.out.println();
		System.out.println("integral1 = " + integral1);
		System.out.println("integral2 = " + integral2);
		throw new Exception();
	    }
	}
	System.out.println("ecount = " + ecount);

	ecount = 0;
	double integral3;
	for (int i = 0; i < 1000000; i++) {
	    do {
		a1 = drv.next();
		b1 = drv.next();
		c1 = drv.next();
	    } while (a1 < 1.0 || c1 <= 0.0 || b1*b1 >= 4*a1*c1);
	    do {
		a2 = drv.next();
		b2 = drv.next();
		c2 = drv.next();
	    } while (a2 < 1.0 || c2 <= 0.0 || b2*b2 >= 4*a2*c2);
	    p = new Polynomial(c1, b1, a1);
	    p.multiplyBy(new Polynomial(c2, b2, a2));
	    glq.setParameters(p);
	    try {
		integral1 = Polynomials.integrateRootP4(p, 0.2, 0.8);
		integral2 =  Polynomials.integrateRootP4(p, 0.0, 0.4);
		integral3 = Polynomials.integralOfRootP4(p).valueAt(0.4);
	    } catch (Exception e) {
		System.out.println(e.getMessage());
		ecount++;
		continue;
	    }
	    if (integral2 != integral3) {
		System.out.println("integral2 = " + integral2);
		System.out.println("integral3 = " + integral3);
		throw new Exception();
	    }
	    integral2 = glq.integrate(0.2, 0.8, 100);
	    double max = Math.max(Math.abs(integral2), 1.0);
	    if (Math.abs(integral1 - integral2)/max > 1.e-9) {
		System.out.println("i = " + i);
		System.out.println("a1 = " + a1);
		System.out.println("b1 = " + b1);
		System.out.println("c1 = " + c1);
		System.out.println("a2 = " + a2);
		System.out.println("b2 = " + b2);
		System.out.println("c2 = " + c2);
		System.out.println("integral1 = " + integral1);
		integral3 = glq.integrate(0.2, 0.8, 200);
		System.out.println("integral3 = " + integral3);
		System.out.println("integral2 = " + integral2);

		if (Math.abs(integral1 - integral3)
		    / Math.abs(integral1 - integral2) > 0.5) {
		    throw new Exception();
		}
	    }
	}
	System.out.println("ecount = " + ecount);

	System.out.println();
	System.out.println("check integrateRoot2Q");

	int pvalues[][] = {
	    {-1, -1, 0},
	    {-3, 1, 0},
	    {-3, -1, 0},
	    {-1, -1, -2},
	    {1, -1, -4},
	    {-1, -1, -4},
	    {-1, -1, 2},
	    {1, -1, 0},
	    {-1, -1, 4},
	    {1, 1, 0},
	    {1, -1, -2},
	    {1, 1, -2},
	    {1, 1, -4},
	};

	UniformIntegerRV pvrv = new UniformIntegerRV(0, true, 13, false);
	Polynomial q1 = new Polynomial(2.0, 1.0, 1.0);
	Polynomial q2 = new Polynomial(3.0, 2.0, 1.0);
	Polynomial q5 = new Polynomial(5.0, 1.0);
	Parms parms= new Parms();
	GLQuadrature<Parms> glq2 = new GLQuadrature<>(8) {
		protected double function(double t, Parms parms) {
		    return MathOps.pow(parms.P1.valueAt(t), parms.p1, 2)
		    * MathOps.pow(parms.P2.valueAt(t), parms.p2, 2)
		    * MathOps.pow(parms.P5.valueAt(t), parms.p5, 2);
		}
	    };
	glq2.setParameters(parms);
	BasicStats stats = new BasicStats.Population();
	double maxDelta = 0;
	for (int j = 0; j < 100000; j++) {
	    for (int i = 0; i < pvalues.length; i++) {
		try {
		    parms.set(pvalues[i][0], pvalues[i][1],
			      pvalues[i][2],
			      q1, q2, q5);

		    double val = Polynomials
			.integrateRoot2Q(pvalues[i][0], pvalues[i][1],
					 pvalues[i][2],
					 q1, q2, q5,
					 0.2, 0.8);
		    double integral = glq2.integrate(0.2, 0.8, 100);
		    integral1 = integral;
		    if (Math.abs(val - integral) > 1.e-7) {
			integral = glq2.integrate(0.2, 0.8, 200);
		    }
		    if (Math.abs(val - integral) > 1.e-7) {
			System.out
			    .format("j = %d, i = %d: [%d, %d, %d, %d, %d] "
				    + "integral = %s\n",
				    j, i,
				    pvalues[i][0], pvalues[i][1],
				    pvalues[i][1], pvalues[i][0],
				    pvalues[i][2], val);
			System.out.println("... numeric integral = " + integral
					   + ", was " + integral1);
			continue;
		    }
		    double delta = Math.abs(val - integral)
			/ Math.max(1.0, Math.max(val, integral));
		    maxDelta = Math.max(maxDelta, delta);
		    stats.add(delta);
		    try {
			double val1 = Polynomials
			    .integrateRoot2Q(pvalues[i][1], pvalues[i][0],
					     pvalues[i][2],
					     q2, q1, q5,
					     0.2, 0.8);
			double max = Math.max(Math.abs(val), Math.abs(val1));
			if (max < 1.0) max = 1.0;
			if (Math.abs(val - val1)/max > 1.e-10) {
			    throw new Exception("exchange error: "
						+ val +" != " + val1);
			}
		    } catch(Exception e) {
			System.out.format("... [%d, %d, %d, %d, %d]: %s\n",
					  pvalues[i][1], pvalues[i][0],
					  pvalues[i][0], pvalues[i][1],
					  pvalues[i][2],
					  e.getMessage());
		    }
		} catch (Exception e) {
		    System.out.format("[%d, %d, %d, %d, %d] test failed\n",
				      pvalues[i][0], pvalues[i][1],
				      pvalues[i][1], pvalues[i][0],
				      pvalues[i][2]);
		    e.printStackTrace(System.out);
		}
	    }
	    do {
		a1 = drv.next();
		b1 = drv.next();
		c1 = drv.next();
	    } while (a1 < 1.0 || c1 <= 1.0 || b1 < 1.0 ||  b1*b1 >= 4*a1*c1);
	    q1 = new Polynomial(c1, b1, a1);
	    /*
	    System.out.format("q1 = %g + %gt + %gt\u00B2\n",
			      c1, b1, a1);
	    */
	    do {
		a2 = drv.next();
		b2 = drv.next();
		c2 = drv.next();
	    } while (a2 < 1.0 || c2 <= 1.0 || b2 < 1.0 || b2*b2 >= 4*a2*c2);
	    q2 = new Polynomial(c2, b2, a2);
	    /*
	    System.out.format("q2 = %g + %gt + %gt\u00B2\n",
			      c2, b2, a2);
	    */
	    do {
		a2 = drv.next();
		b2 = drv.next();
	    } while (a2 < 1.0 || b2 < 1.0);
	    q5 = new Polynomial(a2, b2);
	    /*
	    System.out.format("q5 = %g + %gt\n",
			      a2, b2);
	    */
	}
	if (stats.size() == 0) {
	    System.out.println("no values of delta");
	} else {
	    System.out.println("delta = " + stats.getMean()
			       + " \u00B1 " + stats.getSDev());
	    System.out.println("maxDelta = " + maxDelta);
	}

	System.out.println("testing integrateRootP2");
	testIntegrateRootP2();
	System.exit(0);
    }
}
