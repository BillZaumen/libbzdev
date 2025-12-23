import org.bzdev.geom.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import org.bzdev.util.PrimArrays;
import org.bzdev.util.IntComparator;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.lang.MathOps;
import org.bzdev.math.stats.BasicStats;

public class Path2DInfoTest {

    // FOR CUBIC CASE
    // https://www.maa.org/sites/default/files/Brookfield2007-103574.pdf
    // describes how to factor a quartic polynomial into two quadratic
    // polynomials.
    // check out B.C. Carlson,
    // "A Table of Elliptic Integrals: Two Quadratic Factors".
    // https://www.ams.org/journals/mcom/1992-59-199/S0025-5718-1992-1134720-4/S0025-5718-1992-1134720-4.pdf
    // See http://www.circuitwizard.de/metapost/arclength.pdf for a
    // discussion of how to get the path length of a cubic Bezier curve
    // and comments about Carlson's notation.



    public static double quadLength(double x0, double y0, double[] coords)
	throws Exception
    {
	BezierPolynomial px = new BezierPolynomial(x0, coords[0], coords[2]);


	BezierPolynomial py = new BezierPolynomial(y0, coords[1], coords[3]);

	BezierPolynomial pxd = px.deriv();
	BezierPolynomial pyd = py.deriv();

	double[] array = Polynomials
	    .fromBezier(Polynomials.multiply(pxd, pxd)
			.add(Polynomials.multiply(pyd, pyd))
			.getCoefficientsArray(),
			0, 2);

	double a = array[0];
	double b = array[1];
	double c = array[2];
	// System.out.println("a = " + a);
	// System.out.println("b = " + b);
	// System.out.println("c = " + c);
	try {
	    return Polynomials.integrateRootP2(1.0, a, b, c);
	} catch (ArithmeticException ea) {
	    // just in case.
	    return getSegmentLength(1.0, px, py, null);
	}
    }

    static double a = 2.0;
    static double b = 4.0;
    static double c = 2.0;
    public static void testQuadLength() throws Exception {

	double xzero = 61.20042605676116;
	double yzero= 63.1878628288182;
	double tcoords[] = {
	    59.40566670562782, -96.41690520759865,
	    59.752755310873596, -62.37143949054614
	};

	double v1 = quadLength(xzero, yzero, tcoords);
	double v2 = getQuadLength(1.0, xzero, yzero, tcoords);
	if (Math.abs(v1 - v2)/Math.max(v1, v2) > 1.e-6) {
	    System.out.println("xzero = " + xzero);
	    System.out.println("yzero = " + yzero);
	    printArray("quadratic coords", tcoords);
	    System.out.println("failed: v1 = " + v1
			       + ", v2 = " +v2);
	    throw new Exception();
	}

	a = 2.0;
	b = 4.0;
	c = 2.0;

	v1 = Polynomials.integrateRootP2(1.0, a, b, c);
	RealValuedFunctOps integrand = (uu) -> {
	    return Math.sqrt(a + b * uu + c*uu*uu);
	};
	GLQuadrature glq = new GLQuadrature(10) {
		protected double function(double t) {
		    return integrand.valueAt(t);
		}
	    };
	v2 = glq.integrate(0, 1.0);
	if (Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}

	double uarray[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
	for (double u: uarray) {
	    v1 = Polynomials.integrateRootP2(u, a, b, c);
	    v2 = glq.integrate(0.0, u);
	    if (Math.abs(v1 - v2)
		/ Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-10) {
		throw new Exception();
	    }
	}

	UniformDoubleRV rv = new UniformDoubleRV(-100.0, true, 100.0, true);
	for (int i = 0; i < 1000; i++) {
	    a = rv.next();
	    b = rv.next();
	    c = rv.next();
	    if (c <= 0.0) continue;
	    if ((4*a*c-b*b) < 0.0) continue;
	    for (double u: uarray) {
		v1 = Polynomials.integrateRootP2(u, a, b, c)
		    - Polynomials.integrateRootP2(0.0, a, b, c);
		v2 = glq.integrate(0.0, u);
		if (Math.abs(v1 - v2)
		    / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-2) {
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
	    }

	    c = 0.0;
	    if  (a < 0.0) continue;
	    if (b < -a) continue;
	    for (double u: uarray) {
		v1 = Polynomials.integrateRootP2(u, a, b, c)
		    - Polynomials.integrateRootP2(0.0, a, b, c);

		v2 = glq.integrate(0.0, u);
		if (Math.abs(v1 - v2)
		    / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-2) {
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
	    }
	    b  = 0.0;
	    for (double u: uarray) {
		v1 = Polynomials.integrateRootP2(u, a, b, c)
		    - Polynomials.integrateRootP2(0.0, a, b, c);
		v2 = glq.integrate(0.0, u);
		if (Math.abs(v1 - v2)
		    / Math.max(Math.abs(v1), Math.abs(v2)) > 1.e-2) {
		    System.out.println("v1 = " + v1 + ", v2 = " + v2);
		    throw new Exception();
		}
	    }
	}

	double[] coords = new double[6];
	for (int i = 0; i < 100000; i++) {
	    double x0 = rv.next();
	    double y0 = rv.next();
	    for (int j = 0; j < 4; j++) {
		coords[j] = rv.next();
	    }
	    double val1 = quadLength(x0, y0, coords);
	    double val2 = getQuadLength(1.0, x0, y0, coords);

	    double vv1 = Path2DInfo.quadLength(0.4, x0, y0, coords);
	    double vv2 = Path2DInfo.quadLengthFunction(x0, y0, coords)
		.valueAt(0.4);
	    if (vv1 != vv2) throw new Exception();

	    if (Math.abs(val1 - val2)/Math.max(val1, val2) > 1.e-6) {
		System.out.println("x0 = " + x0);
		System.out.println("y0 = " + y0);
		printArray("quadratic coords", coords);
		System.out.println("failed: val1 = " + val1
				   + ", val2 = " +val2);
		throw new Exception();
	    }
	}
    }

    static boolean cases[] = new boolean[11];


    static void printArray(String name, double[] array) {
	System.out.print(name + ":");
	for (double v: array) {
	    System.out.print(" " + v);
	}
	System.out.println();
    }

    static void printArray(String name, double[] array, int limit) {
	System.out.print(name + ":");
	for (double v: array) {
	    if ((limit--) > 0) {
		System.out.print(" " + v);
	    }
	}
	System.out.println();
    }

    static void printArray(String name, Polynomial p) {
	printArray(name, p.getCoefficientsArray());
    }


    static boolean cases2[] = new boolean[11];

    // statistics
    static int depthcnt = 0;
    static int numericCount = 0;
    static int callCount = 0;

    static double cubicLength(double u, double x0, double y0, double[] coords)
	throws ArithmeticException
    {
	return cubicLength(0, false, u, x0, y0, coords);
    }


    static double cubicLength(int depth, boolean split,
			      double u, double x0, double y0, double[] coords)
	throws ArithmeticException
    {
	if (split) {
	    depthcnt++;
	    depth++;
	    double[] scoords = new double[12];
	    PathSplitter.split(PathIterator.SEG_CUBICTO,
			       x0, y0, coords, 0, scoords, 0, 0.5);
	    if (u <= 0.5) {
		u *= 2;
		return cubicLength(depth, false,  u, x0, y0, scoords);
	    } else {
		double len1 = cubicLength(depth, false, 1.0, x0, y0, scoords);
		x0 = scoords[4];
		y0 = scoords[5];
		System.arraycopy(scoords, 6, scoords, 0, 6);
		u -= 0.5;
		u *= 2;
		return len1 + cubicLength(depth, false, u, x0, y0, scoords);
	    }
	}

	if (false) {
	    System.out.println("cubicLength: (depth = " + depth + ")");
	    System.out.println("... u = " + u);
	    System.out.println("... x0 = " + x0);
	    System.out.println("... y0 = " + y0);
	    printArray("... coords", coords, 6);
	}

	BezierPolynomial px = new BezierPolynomial(x0, coords[0], coords[2],
						   coords[4]);

	BezierPolynomial py = new BezierPolynomial(y0, coords[1], coords[3],
						   coords[5]);

	px = px.deriv();
	py = py.deriv();

	double[] marray = Polynomials.fromBezier(null,
						  px.getCoefficientsArray(),
						 0, 2);
	// Fix up any roundoff errors where the value should be zero.
	double max = 0.0;
	for (double v: marray) {
	    max = Math.max(max, Math.abs(v));
	}
	if (max != 0.0) {
	    for (int i = 0; i < marray.length; i++) {
		if (Math.abs(marray[i])/max < 1.e-12) {
		    marray[i] = 0.0;
		}
	    }
	}
	Polynomial Px = new  Polynomial(marray);
	marray = Polynomials.fromBezier(null,
					py.getCoefficientsArray(),
					0, 2);
	max = 0.0;
	for (double v: marray) {
	    max = Math.max(max, Math.abs(v));
	}
	if (max != 0.0) {
	    for (int i = 0; i < marray.length; i++) {
		if (Math.abs(marray[i])/max < 1.e-12) {
		    marray[i] = 0.0;
		}
	    }
	}
	Polynomial Py = new  Polynomial(marray);

	int degPx = Px.getDegree();
	int degPy = Py.getDegree();
	try {
	    if (degPx > degPy) {
		return cubicLength(u, Py, Px);
		/*
		  Polynomial tmp = Px;
		  Px = Py;
		  Py = tmp;
		  int itmp = degPx;
		  degPx = degPy;
		  degPy = itmp;
		*/
	    } else {
		double result =  cubicLength(u, Px, Py);
		return result;
	    }
	} catch (Polynomials.RootP4Exception e) {
	    if (depth < 1) {
		return cubicLength(depth, true, u, x0, y0, coords);
	    } else {
		numericCount++;
		// cannot split due to depth limit. Fall back to
		// numerical integration.
		double result =  getCubicLength(u, x0, y0, coords);
		return result;
	    }
	} finally {
	    callCount++;
	}
    }

    // Note: this may modify Px or Py: it is a separate method merely
    // for testing.
    static double cubicLength (double u, Polynomial Px, Polynomial Py)
	throws ArithmeticException
    {
	// special cases.
	int degPx = Px.getDegree();
	int degPy = Py.getDegree();

	/*
	System.out.println("degPx = " + degPx +", degPy = " + degPy);
	printArray("Px", Px);
	printArray("Py", Py);
	*/
	double[] marray = null;
	if (degPx == 1) {
	    if (degPy == 1) {
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		marray = Px.getCoefficientsArray();
		cases2[0] = true;
		return Polynomials
		    .integrateRootP2(u, marray[0], marray[1], marray[2])
		    - Polynomials.integrateRootP2(0.0,
						  marray[0],
						  marray[1],
						  marray[2]);
	    }
	    // If we got here degPy is 2
	} else if (degPx == 0) {
	    if (Px.getCoefficientsArray()[0] == 0.0) {
		// integrate |Py|
		Polynomial integral = Py.integral();
		switch (Py.getDegree()) {
		case 0:
		    return Math.abs(integral.valueAt(u)
				    - integral.valueAt(0.0));
		case 1:
		    marray = Py.getCoefficientsArray();
		    double r = -marray[0] / marray[1];
		    if (r > 0.0 && r < u) {
			double val = integral.valueAt(r);
			return Math.abs(val)
			    + Math.abs(integral.valueAt(u) - val);
		    } else {
			return Math.abs(integral.valueAt(u));
		    }
		case 2:
		    marray = Py.getCoefficientsArray();
		    double descr = marray[1]*marray[1] - 4*marray[0]*marray[2];
		    if (descr <= 0.0) {
			return Math.abs(integral.valueAt(u)
					- integral.valueAt(0.0));
		    }
		    double rdescr = Math.sqrt(descr);
		    double r1 = (-marray[1] - rdescr)/(2*marray[2]);
		    double r2 = (-marray[1] + rdescr)/(2*marray[2]);
		    if (r2 < r1) {
			double tmp = r1;
			r1 = r2;
			r2 = tmp;
		    }
		    if (r2 < 0.0 || r1 == r2) {
			return Math.abs(integral.valueAt(u)
					- integral.valueAt(0.0));
		    } else if (r1 > 0.0 && r1 < u) {
			double integral1 = integral.valueAt(r1);
			double sum = Math.abs(integral1);
			if (r2 < u) {
			    double integral2 = integral.valueAt(r2);
			    sum += Math.abs(integral2 - integral1);
			    sum += Math.abs(integral.valueAt(u) - integral2);
			} else {
			    sum += Math.abs(integral.valueAt(u) - integral1);
			}
			return sum;
		    } else if (r2 > 0.0 && r2 < u) {
			double integral1 = integral.valueAt(r2);
			double sum = Math.abs(integral.valueAt(r2));
			sum += Math.abs(integral.valueAt(u) - integral1);
			return sum;
		    } else {
			return Math.abs(integral.valueAt(u)
					- integral.valueAt(0.0));
		    }
		}
	    } else {
		// no root case: Px^2 is a constant > 0 so
		// Px^2 + Py^2 is always positive
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		cases2[1] = true;
		switch (Px.getDegree()) {
		case 0:
		    double factor = Math.sqrt(Px.getCoefficientsArray()[0]);
		    return factor*u;
		case 2:
		    double[] array = Px.getCoefficientsArray();
		    return Polynomials
			    .integrateRootP2(u, array[0], array[1], array[2])
			    - Polynomials
			    .integrateRootP2(0, array[0], array[1], array[2]);
		case 4:
		    return Polynomials.integrateRootP4(Px, 0.0, u);
		}
	    }
	}
	// At this point, degPy == 2 as the other values of degPy were already
	// handled. In addition degPx is at least 1.
	double[] arrayX = Px.getCoefficientsArray();
	double[] arrayY = Py.getCoefficientsArray();
	double scaleX = arrayX[degPx];
	double scaleY = arrayY[degPy];
	Px.multiplyBy(1.0/scaleX);
	Py.multiplyBy(1.0/scaleY);


	// in case of roundoff errors
	arrayX[degPx] = 1.0;
	arrayY[degPy] = 1.0;
	double c = arrayX[0];
	double b = arrayX[1];
	double b2 = b*b;
	double c4 = 4*c;
	double descrX = b2 - c4;
	if (Math.abs(descrX)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrX = 0;
	}
	c = arrayY[0];
	b = arrayY[1];
	b2 = b*b;
	c4 = 4*c;
	double descrY = b2 - c4;
	if (Math.abs(descrY)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrY = 0;
	}

	int nx = (degPx == 1)? 1: ((descrX >=  0)? 2: 0);
	int ny = (descrY >= 0)? 2: 0;
	double[] rootsX = (nx == 2)? new double[2]:
	    (nx == 1)? new double[1]: null;
	double[] rootsY = (ny == 2)? new double[2]: null;
	if (nx == 1) {
	    rootsX[0] = -arrayX[0];
	} else if (nx == 2) {
	    double rdescrX = Math.sqrt(descrX);
	    rootsX[0] = (-arrayX[1] - rdescrX)/2.0;
	    rootsX[1] = (-arrayX[1] + rdescrX)/2.0;
	}
	if (ny == 2) {
	    double rdescrY = Math.sqrt(descrY);
	    rootsY[0] = (-arrayY[1] - rdescrY)/2.0;
	    rootsY[1] = (-arrayY[1] + rdescrY)/2.0;
	}
	/*
	for (int i = 0; i < rootsX.length; i++) {
	    System.out.format("rootsX[%d] = %s\n", i, rootsX[i]);
	}
	for (int i = 0; i < rootsY.length; i++) {
	    System.out.format("rootsy[%d] = %s\n", i, rootsY[i]);
	}
	*/
	if (nx == 1 && ny == 2) {
	    boolean test1 = Math.abs(rootsX[0] - rootsY[0]) < 1.e-12;
	    boolean test2 = Math.abs(rootsX[0] - rootsY[1]) < 1.e-12;
	    if (!test1 && ! test2) {
		// no common roots
		Px.multiplyBy(scaleX);
		Px.multiplyBy(Px);
		Py.multiplyBy(scaleY);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		return Polynomials.integrateRootP4(Px, 0.0, u);
	    }
	    Polynomial p = new Polynomial(1.0);
	    boolean useX0 = false;
	    boolean useY0 = false;
	    boolean useY1 = false;
	    if (test1) {
		p.multiplyBy(new Polynomial(-rootsX[0], 1.0));
		useX0 = true; useY0 = true;
	    }
	    if (test2 && !useX0) {
		p.multiplyBy(new Polynomial(-rootsX[0], 1.0));
		useX0 = true; useY1 = true;
	    }
	    Polynomial rpx = new Polynomial(1.0);
	    Polynomial rpy = new Polynomial(1.0);
	    if (!useX0) {
		rpx.multiplyBy(new Polynomial(-rootsX[0], 1.0));
	    }
	    if (!useY0) {
		rpy.multiplyBy(new Polynomial(-rootsY[0], 1.0));
	    }
	    if (!useY1) {
		rpy.multiplyBy(new Polynomial(-rootsY[1], 1.0));
	    }
	    rpx.multiplyBy(scaleX);
	    rpx.multiplyBy(rpx);
	    rpy.multiplyBy(scaleY);
	    rpy.multiplyBy(rpy);
	    rpx.incrBy(rpy);
	    cases2[2] = true;
	    return Polynomials.integrateAbsPRootQ(u, p, rpx);

	} else if (nx == 2 && ny == 2) {
	    boolean test1 = Math.abs(rootsX[0] - rootsY[0]) < 1.e-12;
	    boolean test2 = Math.abs(rootsX[0] - rootsY[1]) < 1.e-12;
	    boolean test3 = Math.abs(rootsX[1] - rootsY[0]) < 1.e-12;
	    boolean test4 = Math.abs(rootsX[1] - rootsY[1]) < 1.e-12;
	    Polynomial p = new Polynomial(1.0);
	    boolean useX0 = false;
	    boolean useX1 = false;
	    boolean useY0 = false;
	    boolean useY1 = false;
	    if (test1) {
		p.multiplyBy(new Polynomial(-rootsX[0], 1.0));
		useX0 = true; useY0 = true;
	    }
	    if (test2 && !useX0) {
		p.multiplyBy(new Polynomial(-rootsX[0], 1.0));
		useX0 = true; useY1 = true;
	    }
	    if (test3 && !useX1 && !useY0) {
		p.multiplyBy(new Polynomial(-rootsX[1], 1.0));
		useX1 = true; useY0 = true;
	    }
	    if (test4 && !useX1 && !useY1) {
		p.multiplyBy(new Polynomial(-rootsX[1], 1.0));
		useX1 = true; useY1 = true;
	    }
	    Polynomial rpx = new Polynomial(1.0);
	    Polynomial rpy= new Polynomial(1.0);
	    if (!useX0) {
		rpx.multiplyBy(new Polynomial(-rootsX[0], 1.0));
	    }
	    if (!useX1) {
		rpx.multiplyBy(new Polynomial(-rootsX[1], 1.0));
	    }
	    if (!useY0) {
		rpy.multiplyBy(new Polynomial(-rootsY[0], 1.0));
	    }
	    if (!useY1) {
		rpy.multiplyBy(new Polynomial(-rootsY[1], 1.0));
	    }
	    int n = p.getDegree();
	    if (n == 0) {
		// no common roots.
		Px.multiplyBy(scaleX);
		Px.multiplyBy(Px);
		Py.multiplyBy(scaleY);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		// printArray("rootsX", rootsX);
		// printArray("rootsY", rootsY);
		cases2[3] = true;
		return Polynomials.integrateRootP4(Px, 0.0, u);
	    } else if (n == 1) {
		cases2[4] = true;
		rpx.multiplyBy(scaleX);
		rpx.multiplyBy(rpx);
		rpy.multiplyBy(scaleY);
		rpy.multiplyBy(rpy);
		rpx.incrBy(rpy);
		// return  integrateProotQ(u, p, rpx);
		return Polynomials.integrateAbsPRootQ(u, p, rpx);
	    } else if (n == 2) {
		// rp must have a degree of 0, with its only coefficient
		// having a value of 1.
		double[] array = p.getCoefficientsArray();
		c = array[0];
		b = array[1];
		double descr = b*b - 4*c;
		if (Math.abs(descr) < 1.e-12) descr = 0;
		Polynomial integral = p.integral();
		if (descr <= 0) {
		    // all values of p have the same sign with one
		    // zero (when descr == 0)
		    cases2[5] = true;
		    return Math.sqrt(scaleX*scaleX + scaleY*scaleY)
			* Math.abs(integral.valueAt(u));
		} else {
		    double rdescr = Math.sqrt(descr);
		    double r1 = (-b - rdescr)/2;
		    double r2 = (-b + rdescr)/2;
		    double scale = Math.sqrt(scaleX*scaleX + scaleY*scaleY);
		    if (r1 > 0.0 && u <= r1) {
			cases2[6] = true;
			/*
			System.out.println("scaleX = " + scaleX);
			System.out.println("scaleY = " + scaleY);
			System.out.println("scale = " + scale);
			System.out.println("r1 = " + r1);
			System.out.println("r2 = " + r2);
			printArray("p", p);
			printArray("integral", integral);
			*/
			return scale * Math.abs(integral.valueAt(u));
		    } else if (r1 > 0.0 && u <= r2) {
			double sum = Math.abs(integral.valueAt(r1));
			sum += Math.abs(integral.valueAt(u)
					- integral.valueAt(r1));
			cases2[7] = true;
			return scale * sum;
		    } else if (r1 > 0.0 && u > r2) {
			double sum = Math.abs(integral.valueAt(r1));
			sum += Math.abs (integral.valueAt(r2)
					 - integral.valueAt(r1));
			sum += Math.abs(integral.valueAt(u)
					- integral.valueAt(r2));
			cases2[8] = true;
			return scale * sum;
		    } else if (r1 <= 0.0 && r2 > 0 && u > r2) {
			double sum = Math.abs(integral.valueAt(r2));
			sum += Math.abs(integral.valueAt(u)
					- integral.valueAt(r2));
			cases2[9] = true;
			return scale * sum;
		    } else {
			/*
			System.out.println("scaleX = " + scaleX);
			System.out.println("scaleY = " + scaleY);
			System.out.println("scale = " + scale);
			System.out.println("r1 = " + r1);
			System.out.println("r2 = " + r2);
			printArray("p", p);
			printArray("integral", integral);
			System.out.println("u = " + u);
			System.out.println("integral(u) = "
					   + integral.valueAt(u));
			*/
			cases2[10] = true;
			return scale * Math.abs(integral.valueAt(u));
		    }
		}
	    }
	    throw new UnexpectedExceptionError();
	} else {
	    // both do not have real roots or matching roots
	    Px.multiplyBy(scaleX);
	    Px.multiplyBy(Px);
	    Py.multiplyBy(scaleY);
	    Py.multiplyBy(Py);
	    Px.incrBy(Py);
	    return Polynomials.integrateRootP4(Px, 0.0, u);
	}
    }

    static Polynomial rptest = null;



    static void testCubicLength() throws Exception {

	BasicSplinePath2D spath = new
	    BasicSplinePath2D((t) -> {
		    return 100.0*Math.cos(t);
	    }, (t) -> {
		    return 100.0*Math.sin(2*t);
	    }, 0.0, 2*Math.PI, 20, false);
	PathIterator pit = spath.getPathIterator(null);
	double spx = 0.0, spy = 0.0;
	double spcoords[] = new double[6];
	double splen = 0.0;
	double splen2 = 0.0;
	while (!pit.isDone()) {
	    switch(pit.currentSegment(spcoords)) {
	    case PathIterator.SEG_MOVETO:
		spx = spcoords[0];
		spy = spcoords[1];
		break;
	    case PathIterator.SEG_CUBICTO:
		splen += cubicLength(1.0, spx, spy, spcoords);
		splen2 += getCubicLength(1.0, spx, spy, spcoords);
		spx = spcoords[4];
		spy = spcoords[5];
	    }
	    pit.next();
	}
	System.out.println("splen = " + splen);
	System.out.println("splen2 = " + splen2);

	Polynomial Px1 = new Polynomial(1.0);
        Px1.multiplyBy(new Polynomial(-0.8, 1.0));
	Px1.multiplyBy(new Polynomial(-1.5, 1.0));
	Px1.multiplyBy(2.5);
	Polynomial Py1 = new Polynomial(1.0);
	Py1.multiplyBy(new Polynomial(-0.8, 1.0));
	Py1.multiplyBy(new Polynomial(-1.5, 1.0));
	Py1.multiplyBy(3.2);


	Polynomial Px1sq = new Polynomial(Px1);
	Px1sq.multiplyBy(Px1);
	Polynomial Py1sq = new Polynomial(Py1);
	Px1sq.multiplyBy(Py1);
	Polynomial Pxy1sq = new Polynomial(Px1sq);
	Pxy1sq.incrBy(Py1sq);

	// Try some explicit polynomials



	// cases2[6] = false;
	// System.out.println("integral start = " +cubicLength(0.0, Px1, Py1));
	GLQuadrature case6glq = new GLQuadrature(10) {
		protected double function(double t) {
		    double xv = Px1.valueAt(t);
		    double yv = Py1.valueAt(t);
		    return Math.sqrt(xv*xv + yv*yv);
		}
	    };
	double case6len2 = case6glq.integrate(0.0, 0.5, 200);
	//                .---- cubicLength may modify its 2nd and 3rd arguments
	double case6len = cubicLength(0.5, Px1, Py1);
	System.out.println("case6len = " + case6len);
	System.out.println("case6len2 = " + case6len2);
	if (Math.abs(case6len - case6len2) > 1.e-6) {
	    System.out.println("cases2[6] = " + cases2[6]);
	    throw new Exception();
	}

	Polynomial p = new Polynomial(0.0);
	Polynomial rp = new Polynomial(2.0);
	System.out.println("p degree = " + p.getDegree());
	System.out.println("rp degree = " + rp.getDegree());
	//             .---- cubicLength may modify its 2nd and 3rd arguments
	double value = cubicLength(0.8, p, rp);
	if (Math.abs(value - 2.0*0.8) > 1.e-10) {
	    System.out.println("value = " + value);
	    throw new Exception();
	}
        rptest = new Polynomial(2.0, 4.0);
	GLQuadrature rpglq = new GLQuadrature(8) {
		protected double function(double t) {
		    return Math.abs(rptest.valueAt(t));
		}
	    };
	double value2 = rpglq.integrate(0.0, 0.8, 100);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(0.8, p, rptest);
	if (Math.abs(value - value2) > 1.e-6) {
	    System.out.println("value = " + value);
	    System.out.println("value2 = " + value2);
	    throw new Exception();
	}
        rptest = new Polynomial(-2.0, 4.0);

        rptest = new Polynomial(4.0, 16.0, 16.0);
	value2 = rpglq.integrate(0, 0.2, 100);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(0.2, p, rptest);
	if (Math.abs(value - value2) > 1.e-6) {
	    System.out.println("value = " + value);
	    System.out.println("value2 = " + value2);
	    throw new Exception();
	}
	value2 = rpglq.integrate(0, 0.5, 100);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(0.5, p, rptest);
	if (Math.abs(value - value2) > 1.e-6) {
	    System.out.println("value = " + value);
	    System.out.println("value2 = " + value2);
	    throw new Exception();
	}

	value2 = rpglq.integrate(0, 1.0, 100);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(1.0, p, rptest);
	if (Math.abs(value - value2) > 1.e-6) {
	    System.out.println("value = " + value);
	    System.out.println("value2 = " + value2);
	    throw new Exception();
	}

	rptest = new Polynomial(10.0, 1.0, 3.0);
	value2 = rpglq.integrate(0, 0.8, 100);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(0.8, p, rptest);
	if (Math.abs(value - value2) > 1.e-6) {
	    System.out.println("value = " + value);
	    System.out.println("value2 = " + value2);
	    throw new Exception();
	}

	p = new Polynomial(3.0);
	rp = new Polynomial(4.0);
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
		return p1.valueAt(t)*Math.sqrt(rp1.valueAt(t));
	}, 8);
	value = Polynomials.integrateAbsPRootQ(3.0, p1, rp1);
	double evalue = glq.integrate(0.0, 3.0, 10);
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

	Polynomial p6 = new Polynomial(4.0, 6.0, 2.0);
	Polynomial rp6 = new Polynomial(6.0, 9.0, 3.0);

	GLQuadrature glq1 = GLQuadrature.newInstance((tt) -> {
		double xv = p6.valueAt(tt);
		double yv = rp6.valueAt(tt);
		return Math.sqrt(xv*xv + yv*yv);
	    }, 8);

	evalue = glq1.integrate(0.0, 0.8);
	//      .---- cubicLength may modify its 2nd and 3rd arguments
	value = cubicLength(0.8, p6, rp6);
	if (Math.abs(value - evalue) > 1.e-10) {
	    for (int i = 0; i < cases2.length; i++) {
		if (cases2[i]) System.out.println("cases2[" +i + "] = "
						  + cases2[i]);
	    }
	    System.out.println("value = " + value);
	    System.out.println("expected value = " + evalue);
	    throw new Exception();
	}

	double x0 = 1.0;
	double y0 = 12.0;
	double[] coords1 = {
	    2.0, 14.0,
	    3.0, 16.0,
	    4.0, 18.0
	};

	double len = cubicLength(1.0, x0, y0, coords1);
	double elen = Path2DInfo.segmentLength(PathIterator.SEG_CUBICTO,
					       x0, y0, coords1);

	if (Math.abs(len - elen) > 1.e-10) {
	    System.out.println("len = " + len);
	    System.out.println("elen = " + elen);
	    throw new Exception();
	}

	double[] coords2 = {
	    2.0, 14.0,
	    2.0, 14.0,
	    2.0, 14.0
	};

	len = cubicLength(1.0, x0, y0, coords2);
	elen = Math.sqrt(5.0);

	if (Math.abs(len - elen) > 1.e-10) {
	    System.out.println("len = " + len);
	    System.out.println("elen = " + elen);
	    throw new Exception();
	}

	x0 = 20.0;
	y0 = 21.0;
	UniformIntegerRV irv = new UniformIntegerRV(1, true, 40, true);

	numericCount = 0;
	int itlimit = 100000;
	for (int i = 0; i < itlimit; i++) {
	    if (i > 0 && (i % (itlimit/10) == 0)) {
		System.out.println("... " + ((i*100)/itlimit) + "%");
	    }
	    double[] coords3 = {
		(double)irv.next(), (double)irv.next(),
		(double)irv.next(), (double)irv.next(),
		(double)irv.next(), (double)irv.next()
	    };
	    // acount no longer used here
	    try {
		len = cubicLength(1.0, x0, y0, coords3);
	    } catch (Exception e) {
		System.out.print("i = " + i +", coords3:");
		for (double v: coords3) {
		    System.out.print(" " + v);
		}
		System.out.println();
		throw e;
	    }
	    elen = getCubicLength(1.0, x0, y0, coords3);

	    if (Math.abs(len - elen)/elen > 1.e-6) {
		System.out.println("len = " + len);
		System.out.println("elen = " + elen);
		double elen1 = getCubicLengthSR(1.0, x0, y0, coords3);
		double elen2 = getCubicLength(1.0, x0, y0, coords3);
		if ((elen != elen2) && Math.abs(len - elen2)/elen2 <= 1.e-6) {
		    throw new Exception( elen +" != " + elen2);
		}

		System.out.print("i = " + i +", coords3:");
		for (double v: coords3) {
		    System.out.print(" " + v);
		}
		System.out.println();
		System.out.println("len = " + len);
		System.out.println("elen = " + elen);
		System.out.println("elen1 = " + elen1);
		System.out.println("elen2 = " + elen2);
		if (Math.abs(len-elen2) < Math.abs(elen1 - elen2)) {
		    continue;
		}
		throw new Exception();
	    }
	    try {
		double vv1 = Path2DInfo.cubicLength(0.4, x0, y0, coords3);
		double vv2 = Path2DInfo.cubicLengthFunction(x0, y0, coords3)
		    .valueAt(0.4);
		try {
		    double vv3 = Path2DInfo
			.segmentLengthFunction(PathIterator.SEG_CUBICTO,
					       x0, y0, coords3).valueAt(0.4);
		    if (Math.abs(vv1-vv3) > 1.e-10) throw new Exception();
		} catch (ArithmeticException ae) {}
		if (vv1 != vv2) {
		    System.out.println("vv1 = " + vv1);
		    System.out.println("vv2 = " + vv2);
		    System.out.println("x0 = " + x0);
		    System.out.println("y0 = " + y0);
		    printArray("coords3", coords3);
		    throw new Exception();
		}
	    } catch (ArithmeticException ea) {
		continue;
	    }
	}

	System.out.println("numericCount = " + numericCount);

	boolean done = true;
	for (int i = 0; i < 11; i++) {
	    System.out.println("cases2[" + i + "] = " + cases2[i]);
	    if (!cases2[i]) {
		done = false;
	    }
	}
	if (!done) {
	    System.out.println("missing case(s) for cubicLength");
	    // throw new Exception("missing case(s) for cubicLength");
	}
    }

    // version using simpson's rule.
    static double getCubicLengthSR(double t,
				   double x0, double y0, double[] coords)
    {
	var px = new BezierPolynomial (x0, coords[0], coords[2], coords[4]);
	var py = new BezierPolynomial (y0, coords[1], coords[3], coords[5]);


	BezierPolynomial pxd = px.deriv();
	BezierPolynomial pyd = py.deriv();

	SimpsonsRule sr = SimpsonsRule.newInstance((u) -> {
		double dpx = pxd.valueAt(u);
		double dpy = pyd.valueAt(u);
		return Math.sqrt(dpx*dpx + dpy*dpy);
	    });

	int degx = pxd.getDegree();
	int degy = pyd.getDegree();

	double[] xarray = Polynomials.fromBezier(null,
						 pxd.getCoefficientsArray(),
						 0, degx);
	double[] yarray = Polynomials.fromBezier(null,
						 pxd.getCoefficientsArray(),
						 0, degy);
	// When we switch from a Bernstein to a monomial basis,
	// the degree of the polynomial may decrease.
	for (int i = degx; i > 0; i--) {
	    if (xarray[i] == 0.0) degx--;
	    else break;
	}
	for (int i = degy; i > 0; i--) {
	    if (xarray[i] == 0.0) degy--;
	    else break;
	}
	double r1x = Double.NaN, r2x = Double.NaN,
	    r1y = Double.NaN, r2y = Double.NaN;
	int nrx = -1, nry = -1;
	if (degx == 2) {
	    double descr = xarray[1]*xarray[1] - 4*xarray[0]*xarray[2];
	    if (descr < 0) nrx = 0;
	    else if (descr == 0) nrx = 1;
	    else nrx = 2;
	    if (nrx == 1) {
		r1x = -xarray[1]/(2*xarray[2]);
	    } else if (nrx == 2) {
		double rdescr = Math.sqrt(descr);
		r1x = (-xarray[1] - rdescr)/(2*xarray[2]);
		r2x = (-xarray[1] + rdescr)/(2*xarray[2]);
	    }
	} else if (degx == 1) {
	    r1x = -xarray[0]/xarray[1];
	}
	if (degy == 2) {
	    double descr = yarray[1]*yarray[1] - 4*yarray[0]*yarray[2];
	    if (descr < 0) nry = 0;
	    else if (descr == 0) nry = 1;
	    else nry = 2;
	    if (nry == 1) {
		r1y = -yarray[1]/(2*yarray[2]);
	    } else if (nry == 2) {
		double rdescr = Math.sqrt(descr);
		r1y = (-yarray[1] - rdescr)/(2*yarray[2]);
		r2y = (-yarray[1] + rdescr)/(2*yarray[2]);
	    }
	} else if (degy == 1) {
	    r1y = -yarray[0]/yarray[1];
	}
	int cnt = 0;
	if (degx == 1) {
	    if (degy == 1) {
		if (r1x == r1y) cnt++;
	    } else if (degy == 2) {
		if (r1x == r1y) cnt++;
		if (r1x == r2y) cnt++;
	    }
	} else if (degx == 2) {
	    if (degy == 1) {
		if (r1x == r1y) cnt++;
		else if (r2x == r1y) cnt++;
	    } else if (degy == 2) {
		if (r1x == r1y) {
		    cnt++;
		    if (r2x == r2y) {
			if (r1x != r2x) cnt++;
		    }
		} else if (r1x == r2y) {
		    cnt++;
		    if (r2x == r1y) {
			if (r1x != r2x) cnt++;
		    }
		} else 	if (r2x == r1y) {
		    cnt++;
		}
		else if (r2x == r2y) {
		    cnt++;
		}
	    }
	}
	double[] roots = new double[cnt];
	int ind = 0;
	if (degx == 1) {
	    if (degy == 1) {
		if (r1x == r1y) roots[ind++] = r1x;
	    } else if (degy == 2) {
		if (r1x == r1y) roots[ind++] = r1x;
		if (r1x == r2y) roots[ind++] = r1x;
	    }
	} else if (degx == 2) {
	    if (degy == 1) {
		if (r1x == r1y) roots[ind++] = r1x;
		else if (r2x == r1y) roots[ind++] = r2x;
	    } else if (degy == 2) {
		if (r1x == r1y) {
		    roots[ind++] = r1x;
		    if (r2x == r2y) {
			if (r1x != r2x) roots[ind++] = r2x;
		    }
		} else if (r1x == r2y) {
		    roots[ind++] = r1x;
		    if (r2x == r1y) {
			if (r1x != r2x) roots[ind++] = r2x;
		    }
		} else if (r2x == r1y) {
		    roots[ind++] = r2x;
		}
		else if (r2x == r2y) {
		    roots[ind++] = r2x;
		}
	    }
	}
	Arrays.sort(roots);
	ind = 0;
	while (ind < cnt) {
	    if (roots[ind] <= 0.0) {
		ind++;
	    } else {
		break;
	    }
	}
	for (int i = ind; i < cnt; i++) {
	    if (roots[i] >= t) {
		cnt = i;
		break;
	    }
	}
	if (ind == cnt) cnt = 0;

	if (cnt == 0) {
	    return sr.integrate(0.0, t, 400);
	} else {
	    double sum = sr.integrate(0.0, roots[ind], 400);
	    for (int i = ind+1; i < cnt; i++) {
		sum += sr.integrate(roots[i-1], roots[i], 400);
	    }
	    sum += sr.integrate(roots[cnt-1], t, 400);
	    return sum;
	}
    }

    static double getQuadLength(double t, double x0, double y0,
				double[] coords)
    {
	var px = new BezierPolynomial (x0, coords[0], coords[2]);
	var py = new BezierPolynomial (y0, coords[1], coords[3]);

	return getSegmentLength(t, px, py, null);
    }

    static double getCubicLength(double t,
				 double x0, double y0, double[] coords)
    {
	var px = new BezierPolynomial (x0, coords[0], coords[2], coords[4]);
	var py = new BezierPolynomial (y0, coords[1], coords[3], coords[5]);

	return getSegmentLength(t, px, py, null);
    }

    static int SL_GLQ_ITERS = 200;

    static double getSegmentLength(double t, BezierPolynomial px,
				   BezierPolynomial py, BezierPolynomial pz)
    {
	BezierPolynomial pxd = px.deriv();
	BezierPolynomial pyd = py.deriv();
	BezierPolynomial pzd = (pz == null)? null: pz.deriv();

	GLQuadrature glq = GLQuadrature.newInstance((u) -> {
		double dpx = pxd.valueAt(u);
		double dpy = pyd.valueAt(u);
		double dpz = (pzd == null)? 0.0: pzd.valueAt(u);
		return Math.sqrt(dpx*dpx + dpy*dpy + dpz*dpz);
	    }, 8);

	int degx = pxd.getDegree();
	int degy = pyd.getDegree();
	int degz = (pz == null)? -1: pzd.getDegree();


	double[] xarray = Polynomials.fromBezier(null,
						 pxd.getCoefficientsArray(),
						 0, degx);
	double[] yarray = Polynomials.fromBezier(null,
						 pyd.getCoefficientsArray(),
						 0, degy);
	double[] zarray = (degz < 0)? new double[0]:
	    Polynomials.fromBezier(null, pzd.getCoefficientsArray(), 0, degz);


	// When we switch from a Bernstein to a monomial basis,
	// the degree of the polynomial may decrease.
	for (int i = degx; i > 0; i--) {
	    if (xarray[i] == 0.0) degx--;
	    else break;
	}
	for (int i = degy; i > 0; i--) {
	    if (yarray[i] == 0.0) degy--;
	    else break;
	}
	for (int i = degz; i > 0; i--) {
	    if (zarray[i] == 0.0) degz--;
	    else break;
	}
	double r1x = Double.NaN, r2x = Double.NaN,
	    r1y = Double.NaN, r2y = Double.NaN,
	    r1z = Double.NaN, r2z = Double.NaN;

	int nrx = 0, nry = 0, nrz = 0;
	if (degx == 2) {
	    double descr = xarray[1]*xarray[1] - 4*xarray[0]*xarray[2];
	    if (descr < 0) nrx = 0;
	    else if (descr == 0) nrx = 1;
	    else nrx = 2;
	    if (nrx == 1) {
		r1x = -xarray[1]/(2*xarray[2]);
	    } else if (nrx == 2) {
		double rdescr = Math.sqrt(descr);
		r1x = (-xarray[1] - rdescr)/(2*xarray[2]);
		r2x = (-xarray[1] + rdescr)/(2*xarray[2]);
	    }
	} else if (degx == 1) {
	    r1x = -xarray[0]/xarray[1];
	    nrx = 1;
	}
	if (r1x > r2x) {
	    double tmp = r1x;
	    r1x = r2x;
	    r2x = tmp;
	}

	double xroots[] = new double[nrx];
	if (nrx > 0) xroots[0] = r1x;
	if (nrx > 1) xroots[1] = r2x;

	if (degy == 2) {
	    double descr = yarray[1]*yarray[1] - 4*yarray[0]*yarray[2];
	    if (descr < 0) nry = 0;
	    else if (descr == 0) nry = 1;
	    else nry = 2;
	    if (nry == 1) {
		r1y = -yarray[1]/(2*yarray[2]);
	    } else if (nry == 2) {
		double rdescr = Math.sqrt(descr);
		r1y = (-yarray[1] - rdescr)/(2*yarray[2]);
		r2y = (-yarray[1] + rdescr)/(2*yarray[2]);
	    }
	} else if (degy == 1) {
	    r1y = -yarray[0]/yarray[1];
	    nry = 1;
	}
	if (r1y > r2y) {
	    double tmp = r1y;
	    r1y = r2y;
	    r2y = tmp;
	}

	double yroots[] = new double[nry];
	if (nry > 0) yroots[0] = r1y;
	if (nry > 1) yroots[1] = r2y;

	if (degz > 0) {
	    if (degz == 2) {
		double descr = zarray[1]*zarray[1] - 4*zarray[0]*zarray[2];
		if (descr < 0) nrz = 0;
		else if (descr == 0) nrz = 1;
		else nrz = 2;
		if (nrz == 1) {
		    r1z = -zarray[1]/(2*zarray[2]);
		} else if (nrz == 2) {
		    double rdescr = Math.sqrt(descr);
		    r1z = (-zarray[1] - rdescr)/(2*zarray[2]);
		    r2z = (-zarray[1] + rdescr)/(2*zarray[2]);
		}
	    } else if (degz == 1) {
		r1z = -zarray[0]/zarray[1];
		nrz = 1;
	    }
	}
	if (r1z > r2z) {
	    double tmp = r1z;
	    r1z = r2z;
	    r2z = tmp;
	}
	double zroots[] = new double[nrz];
	if (nrz > 0) zroots[0] = r1z;
	if (nrz > 1) zroots[1] = r2z;

	int cnt = 0;
	double[] roots;

	// if (pz == null) {
	double[] roots0 = new double[nrx + nry + nrz];
	int ind0 = 0;
	for (int i = 0; i < nrx; i++) {
	    roots0[ind0++] =xroots[i];
	}
	for (int i = 0; i < nry; i++) {
	    roots0[ind0++] =yroots[i];
	}
	if (pz != null) {
	    for (int i = 0; i < nrz; i++) {
		roots0[ind0++] =zroots[i];
	    }
	}
	Arrays.sort(roots0);
	int cnt0 = 0;
	for (int i = 1; i < roots0.length; i++) {
	    if (roots0[i-1] == roots0[i]) cnt0++;
	}
	roots = new double[roots0.length - cnt0];
	cnt = roots.length;
	ind0 = 0;
	if (roots.length > 0) {
	    roots[ind0++] = roots0[0];
	    for (int i = 1; i < roots0.length; i++) {
		if (roots0[i-1] != roots0[i]) {
		    roots[ind0++] = roots0[i];
		}
	    }
	}
	int ind = 0;
	while (ind < cnt) {
	    if (roots[ind] <= 0.0) {
		ind++;
	    } else {
		break;
	    }
	}
	for (int i = ind; i < cnt; i++) {
	    if (roots[i] >= t) {
		cnt = i;
		break;
	    }
	}
	if (ind == cnt) cnt = 0;
	if (cnt == 0) {
	    return glq.integrate(0.0, t, SL_GLQ_ITERS);
	} else {
	    double sum = glq.integrate(0.0, roots[ind], SL_GLQ_ITERS);
	    for (int i = ind+1; i < cnt; i++) {
		sum += glq.integrate(roots[i-1], roots[i], SL_GLQ_ITERS);
	    }
	    sum += glq.integrate(roots[cnt-1], t, SL_GLQ_ITERS);
	    return sum;
	}
    }


    static public void badCubics() throws Exception {



	Polynomial polynomial1 =
	    new Polynomial(4364.999999999998, -21923.999999999996,
			   31950.0, -11124.0, 1170.0);

	GLQuadrature glq = GLQuadrature.newInstance((u) -> {
		return Math.sqrt(polynomial1.valueAt(u));
	    }, 10);
	try {
	    double result1 = Polynomials.integrateRootP4(polynomial1, 0.0, 1.0);
	    double result2 = glq.integrate(0.0, 1.0, 600);
	    System.out.println("result1 = " + result1);
	    System.out.println("result2 = " + result2);
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	// cases that failed at some point.
	double x0 = 20.0; double y0 = 21.0;
	double len, elen, elen2;

	double coords19[] = {22.0, 15.0, 28.0, 27.0, 1.0, 18.0};
	try {
	    System.out.println("coords19 ...");
	    len = cubicLength(1.0, x0, y0, coords19);
	    if (Double.isNaN(len)) throw new Exception();
	    elen = getCubicLength(1.0, x0, y0, coords19);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords19);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-8) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords19", coords19);
		if (Math.abs(elen2 - elen) < 1.e-4)  {
		    throw new Exception();
		}
	    }
	} catch (ArithmeticException e) {
	    for (int i = 0; i < 11; i++) {
		if (cases2[i]) {
		    System.out.println("cases2[" + i + "] = " + cases2[i]);
		}
	    }
	    System.out.println(e.getMessage() + " ... expected");
	}


	double coords18[] = {34.0, 4.0, 39.0, 39.0, 34.0, 15.0};
	try {
	    System.out.println("coords18 ...");
	    len = cubicLength(1.0, x0, y0, coords18);
	    elen = getCubicLength(1.0, x0, y0, coords18);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords18);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-8) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords18", coords18);
		if (Math.abs(elen2 - elen) < 1.e-4)  {
		    throw new Exception();
		}
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}



	double coords17[] = {20.0, 7.0, 20.0, 3.0, 20.0, 3.0};
	try {
	    len = cubicLength(2, true, 1.0, x0, y0, coords17);
	    elen = getCubicLength(1.0, x0, y0, coords17);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords17);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-8) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords17", coords17);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords16[] = {32.0, 33.0, 22.0, 24.0, 7.0, 3.0};
	try {
	    len = cubicLength(2, true, 1.0, x0, y0, coords16);
	    elen = getCubicLength(1.0, x0, y0, coords16);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords16);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-8) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords16", coords16);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}


	double x15s[] = {20.0, 18.5, 17.75};
	double y15s[] = {21.0, 33.75, 31.6875};
	double coords15s[][] = {
	    {19.5, 29.0, 19.0, 33.25, 18.5, 33.75},
	    {18.25, 34.0, 18.0, 33.3125, 17.75, 31.6875},
	    {17.5, 30.0625, 17.25, 27.5, 17.0, 24.0},
	};
	for (int i = 0; i < 3; i++) {
	    try {
		System.out.println("*** trying i = " + i);
		len = cubicLength(2, true, 1.0, x15s[i], y15s[i], coords15s[i]);
		elen = getCubicLength(1.0, x15s[i], y15s[i], coords15s[i]);
		elen2 = getCubicLengthSR(1.0, x15s[i], y15s[i], coords15s[i]);
		System.out.println("len = " + len +", elen = " + elen);
		System.out.println(".... elen2 = " + elen2);
		if (Math.abs(elen - len)/elen > 1.e-4) {
		    printArray("coords15s[" + i + "]", coords15s[i]);
		    throw new Exception();
		}
	    } catch (ArithmeticException e) {
		System.out.println(e.getMessage() + " ... expected");
	    }
	}


	double[] coords15 = {19.0, 37.0, 18.0, 38.0, 17.0, 24.0};
	try {
	    len = cubicLength(0, true, 1.0, x0, y0, coords15);
	    elen = getCubicLength(1.0, x0, y0, coords15);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords15);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		printArray("coords15", coords15);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}


	double[] coords14 = {27.0, 29.0, 27.0, 29.0, 20.0, 21.0};
	try {
	    len = cubicLength(2, false, 1.0, x0, y0, coords14);
	    elen = getCubicLength(1.0, x0, y0, coords14);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords14);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords14", coords14);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords13[] = {39.0, 3.0, 34.0, 13.0, 17.0, 16.0};
	try {
	    len = cubicLength(2, false, 1.0, x0, y0, coords13);
	    elen = getCubicLength(1.0, x0, y0, coords13);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords13);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords13", coords13);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	/*
	double[][] coords12s = {
	    {16.75, 19.0, 14.6875, 17.75, 13.78125, 17.203125},
	    {12.875, 16.65625, 13.125, 16.8125, 14.5, 17.625},
	    {17.25, 19.25, 24.5, 23.5, 36.0, 30.0},
	};
	double xs[] = {20.0, 13.78125, 14.5};
	double ys[] = {21.0, 17.203125, 17.625};

	for (int j = 0; j < xs.length; j++) {
	    if (j != 1) continue;
	    len = cubicLength(2, false, 1.0, xs[j], ys[j], coords12s[j]);
	    elen = getCubicLength(1.0, xs[j], ys[j], coords12s[j]);
	    elen2 = getCubicLengthSR(1.0, xs[j], ys[j], coords12s[j]);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords12s["+j+"]", coords12s[j]);
		throw new Exception();
	    }
	}
	*/

	double[] coords12 = {7.0, 13.0, 13.0, 17.0, 36.0, 30.0};
	try {
	    len = cubicLength(2, false, 1.0, x0, y0, coords12);
	    elen = getCubicLength(1.0, x0, y0, coords12);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords12);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords12", coords12);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double[] coords11 = {2.0, 21.0, 18.0, 22.0, 26.0, 18.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords11);
	    elen = getCubicLength(1.0, x0, y0, coords11);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords11);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords11", coords11);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords10[] = {6.0, 38.0, 10.0, 34.0, 23.0, 16.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords10);

	    double testx1 = Path2DInfo.getX(0.2, x0, y0,
					    PathIterator.SEG_CUBICTO,
					    coords10);
	    double testx2 = Path2DInfo.getX(0.7, x0, y0,
					    PathIterator.SEG_CUBICTO,
					    coords10);

	    double scoords[] = new double[12];
	    PathSplitter.split(PathIterator.SEG_CUBICTO, x0, y0,
			       coords10, 0, scoords, 0, 0.5);
	    double testx1a = Path2DInfo.getX(0.4, x0, y0,
					     PathIterator.SEG_CUBICTO,
					     scoords);

	    if (Math.abs(testx1 - testx1a) > 1.e-10) throw new Exception();

	    double len1 = getCubicLength(1.0, x0, y0, scoords);
	    double x1 = scoords[4];
	    double y1 = scoords[5];
	    double x0a = scoords[4];
	    double y0a = scoords[5];
	    System.arraycopy(scoords, 6, scoords, 0, 6);
	    double testx2a = Path2DInfo.getX(0.4, x0a, y0a,
					     PathIterator.SEG_CUBICTO, scoords);
	    if (Math.abs(testx2 - testx2a) > 1.e-10) {
		System.out.println("testx2 = " + testx2);
		System.out.println("testx2a = " + testx2a);
		throw new Exception();
	    }

	    double len2 = getCubicLength(1.0, x1, y1, scoords);
	    System.out.println("slen = " + (len1 + len2));

	    elen = getCubicLength(1.0, x0, y0, coords10);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords10);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		double slen = len1 + len2;
		if (Math.abs(slen - elen) > 1.e-4) {
		    for (int i = 0; i < 11; i++) {
			if (cases2[i]) {
			    System.out.println("cases2[" + i + "] = "
					       + cases2[i]);
			}
		    }
		    printArray("coords10", coords10);
		    throw new Exception();
		}
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	try {
	    double coords9[] = {19.0, 21.0, 17.0, 16.0, 24.0, 29.0};
	    len = cubicLength(1.0, x0, y0, coords9);
	    elen = getCubicLength(1.0, x0, y0, coords9);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords9);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords9", coords9);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords8[] = {21.0, 26.0, 28.0, 17.0, 8.0, 32.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords8);
	    elen = getCubicLength(1.0, x0, y0, coords8);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords8);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords8", coords8);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords7[] = {36.0, 21.0, 38.0, 21.0, 14.0, 21.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords7);
	    elen = getCubicLength(1.0, x0, y0, coords7);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords7);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords7", coords7);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords6[] = {4.0, 31.0, 17.0, 19.0, 4.0, 31.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords6);
	    elen = getCubicLength(1.0, x0, y0, coords6);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords6);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords6", coords6);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords5[] = {4.0, 21.0, 30.0, 21.0, 11.0, 21.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords5);
	    elen = getCubicLength(1.0, x0, y0, coords5);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords5);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);

	    if (Math.abs(elen - len)/elen > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords5", coords5);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords4[] = {7.0, 18.0, 7.0, 25.0, 20.0, 14.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords4);
	    elen = getCubicLength(1.0, x0, y0, coords4);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords4);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len) > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases2[i]) {
			System.out.println("cases2[" + i + "] = " + cases2[i]);
		    }
		}
		printArray("coords4", coords4);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords3[] = {33.0, 27.0, 33.0, 15.0, 20.0, 33.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords3);
	    elen = getCubicLength(1.0, x0, y0, coords3);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords3);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords2[] = {35.0, 33.0, 38.0, 16.0, 29.0, 2.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords2);
	    elen = getCubicLength(1.0, x0, y0, coords2);
	    elen2 = getCubicLengthSR(1.0, x0, y0, coords2);
	    System.out.println("len = " + len +", elen = " + elen);
	    System.out.println(".... elen2 = " + elen2);
	    if (Math.abs(elen - len) > 1.e-4) {
		for (int i = 0; i < 11; i++) {
		    if (cases[i]) {
			System.out.println("cases[" + i + "] = " + cases[i]);
		    }
		}
		printArray("coords2", coords2);
		throw new Exception();
	    }
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}

	double coords1[] = {31.0, 26.0, 8.0, 30.0, 18.0, 33.0};
	try {
	    len = cubicLength(1.0, x0, y0, coords1);
	} catch (ArithmeticException e) {
	    System.out.println(e.getMessage() + " ... expected");
	}
    }

    static void centerOfMassTest() {

	Path2D line1 = new Path2D.Double();
	line1.moveTo(193.53515548309275, 699.4515230632213);
	line1.lineTo(213.3553390593274, 745.3553390593272);
	line1.lineTo(233.17552263556203, 791.2591550554332);

	Point2D lcm1 = Path2DInfo.centerOfMassOf(line1);

	Path2D line2 = new Path2D.Double();
	line2.moveTo(193.53515548309275, 699.4515230632213);
	line2.lineTo(213.3553390593274, 745.3553390593272);
	line2.lineTo(233.17552263556203, 791.2591550554332);
	line2.closePath();

	Point2D lcm2 = Path2DInfo.centerOfMassOf(line2);
	System.out.println("lcm1 = " + lcm1);
	System.out.println("lcm2 = " + lcm2);

	Rectangle2D line1bb = line1.getBounds2D();
	System.out.println("area of line1 = " + Path2DInfo.areaOf(line2));
	System.out.println("area of line2 = " + Path2DInfo.areaOf(line2));
	System.out.println("bb area = "
			   + (line1bb.getWidth() * line1bb.getHeight()));
	System.out.println("expecting bad cm for line1/line2 because the ");
	System.out.println("area for these curves when closed is much less ");
	System.out.println("than the area for the curve's bounding box: ");
	System.out.println("the area for these curves should be zero if ");
	System.out.println("there were no floating-point errors");

	Path2D rect = new Path2D.Double();

	rect.moveTo(100.0, 200.0);
	rect.lineTo(500.0, 200.0);
	rect.lineTo(500.0, 700.0);
	rect.lineTo(100.0, 700.0);
	rect.closePath();

	// same as rect, but using cubic Bezier Curves.
	Path2D rect2 = new Path2D.Double();
	rect2.moveTo(100.0, 200.0);
	double[] coords = new double[6];
	double[] tmp = new double[6];
	coords[0] = 500.0; coords[1] = 200.0;
	Path2DInfo.elevateDegree(1, tmp, 100.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 100.0, 200.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 200.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 100.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	rect2.closePath();

	Path2D rect4 = new Path2D.Double();
	rect4.moveTo(100.0, 200.0);
	rect4.lineTo(500.0, 200.0);
	rect4.lineTo(500.0, 700.0);
	rect4.lineTo(900.0, 700.0);
	rect4.lineTo(900.0, 1200.0);
	rect4.lineTo(500.0, 1200.0);
	rect4.lineTo(500.0, 700.0);
	rect4.lineTo(100.0, 700.0);
	rect4.closePath();

	Path2D rect5 = new Path2D.Double();
	rect5.moveTo(100.0, 200.0);
	coords[0] = 500.0; coords[1] = 200.0;
	Path2DInfo.elevateDegree(1, tmp, 100.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 100.0, 200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 900.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 900.0; coords[1] = 1200.0;
	Path2DInfo.elevateDegree(1, tmp, 900.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 900.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 1200.0;
	Path2DInfo.elevateDegree(1, tmp, 900.0, 1200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 900.0, 1200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 1200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 1200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 100.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	rect5.closePath();


	Path2D circ = Paths2D.createArc(250.0, 350.0, 250.0, 300.0,
					2*Math.PI, Math.PI/18);
	circ.closePath();
	Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
	path.append(rect, false);
	path.append(circ, false);
	Point2D p1 = Path2DInfo.centerOfMassOf(rect);
	Point2D p12 = Path2DInfo.centerOfMassOf(rect2);
	Point2D p2 = Path2DInfo.centerOfMassOf(circ);
	Point2D p3 = Path2DInfo.centerOfMassOf(path);
	System.out.println("computing p4");
	Point2D p4 = Path2DInfo.centerOfMassOf(rect4);
	System.out.println("computing p5");
	Point2D p5 = Path2DInfo.centerOfMassOf(rect5);

	if (Math.abs(p1.getX() - p12.getX()) > 1.e-10
	    || Math.abs(p1.getY() - p12.getY()) > 1.e-10) {
	    System.out.println("p1 and p12 differ");
	    System.exit(1);
	}
	System.out.println("circumference of rect = "
			   + Path2DInfo.circumferenceOf(rect2));
	System.out.println("circumference of rect2 = "
			   + Path2DInfo.circumferenceOf(rect2));
	System.out.println("area of rect = " + Path2DInfo.areaOf(rect));
	System.out.println("area of rect2 = " + Path2DInfo.areaOf(rect2));

	System.out.println("circumference of rect4 = "
			   + Path2DInfo.circumferenceOf(rect4));
	System.out.println("circumference of rect5 = "
			   + Path2DInfo.circumferenceOf(rect5));

	System.out.println("area of rect4 = " + Path2DInfo.areaOf(rect4));
	System.out.println("area of rect5 = " + Path2DInfo.areaOf(rect5));

	if (Math.abs(p4.getX() - p5.getX()) > 1.e-10
	    || Math.abs(p4.getY() - p5.getY()) > 1.e-10) {
	    System.out.println("p4 and p5 differ");
	    System.out.format("p4 = (%g, %g)\n", p4.getX(), p4.getY());
	    System.out.format("p5 = (%g, %g)\n", p5.getX(), p5.getY());
	    System.exit(1);
	}


	double x1 = p1.getX();
	double y1 = p1.getY();
	double x2 = p2.getX();
	double y2 = p2.getY();
	double x3 = p3.getX();
	double y3 = p3.getY();
	double x4 = p4.getX();
	double y4 = p4.getY();

	System.out.format("rect: (%g, %g)\n", x1, y1);
	System.out.format("circ: (%g, %g)\n", x2, y2);
	System.out.format("combo: (%g, %g)\n", x3, y3);

	System.out.format("rect4: (%g, %g)\n", x4, y4);


	// Rotate 30 degrees so none of the line segments line up with
	// the X and Y axis.
	AffineTransform af = AffineTransform.getRotateInstance(Math.PI/6);

	Point2D p1r = Path2DInfo.centerOfMassOf(rect, af);
	Point2D p2r = Path2DInfo.centerOfMassOf(circ, af);
	Point2D p3r = Path2DInfo.centerOfMassOf(path, af);

	Point2D ep1r = af.transform(p1, null);
	Point2D ep2r = af.transform(p2, null);
	Point2D ep3r = af.transform(p3, null);

	// use known values.
	x1 = (500.0 + 100) / 2;
	y1 = (200.0 + 700) / 2;
	x2 = 250.0;
	y2 = 350.0;

	if (Math.abs(x1 - p1.getX()) > 1.e-10
	    || Math.abs(y1 - p1.getY()) > 1.e-10) {
	    System.out.format("expected rect: (%s, %s)\n", x1, y1);
	    System.out.format("found rect: (%s, %s)\n", p1.getX(), p1.getY());
	    System.exit(1);
	}
	if (Math.abs(x2 - p2.getX()) > 1.e-10
	    || Math.abs(y2 - p2.getY()) > 1.e-10) {
	    System.out.format("expected circ: (%s, %s)\n", x2, y2);
	    System.out.format("found circ: (%s, %s)\n", p2.getX(), p2.getY());
	    System.exit(1);
	}

	double ra = 400.0 * 500.0;
	double ca = Math.PI*50.0*50.0;
	double x = (x1*ra - x2*ca)/ (ra - ca);
	double y = (y1*ra - y2*ca)/ (ra - ca);
	if (Math.abs(x - p3.getX()) > 1.e-8
	    || Math.abs(y - p3.getY()) > 1.e-8) {
	    System.out.format("expected combo: (%s, %s)\n", x, y);
	    System.out.format("found combo: (%s, %s)\n", p3.getX(), p3.getY());
	    System.exit(1);
	}

	if ((Math.abs(p1r.getX() - ep1r.getX()) > 1.e-10)
	    || (Math.abs(p1r.getY() - ep1r.getY()) > 1.e-10)) {
	    System.out.println("rotated rectangle failed");
	}

	if ((Math.abs(p2r.getX() - ep2r.getX()) > 1.e-10)
	    || (Math.abs(p2r.getY() - ep2r.getY()) > 1.e-10)) {
	    System.out.println("rotated circle failed");
	}
	if ((Math.abs(p1r.getX() - ep1r.getX()) > 1.e-10)
	    || (Math.abs(p1r.getY() - ep1r.getY()) > 1.e-10)) {
	    System.out.println("rotated rectangle failed");
	}
	if ((Math.abs(p3r.getX() - ep3r.getX()) > 1.e-10)
	    || (Math.abs(p3r.getY() - ep3r.getY()) > 1.e-10)) {
	    System.out.println("rotated combo failed");
	}

	Path2DInfo.printSegments(rect2);
	System.out.println("Computing rmatrix");
	double[][] rmatrix = Path2DInfo.momentsOf(rect);
	System.out.println("Computing rmatrix2");
	double[][] rmatrix2 = Path2DInfo.momentsOf(rect2);
	boolean err = false;
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix[i][j] - rmatrix2[i][j]) > 0.1) {
		    System.out.println("rmatrix != rmatrix2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, rmatrix[i][j], rmatrix2[i][j]);
		    err = true;
		}
	    }
	}

	// Try with a reference point set to be the center of mass.
	rmatrix2 = Path2DInfo.momentsOf(p1, rect);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix[i][j] - rmatrix2[i][j]) > 0.1) {
		    System.out.println("rmatrix != rmatrix2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, rmatrix[i][j], rmatrix2[i][j]);
		    err = true;
		}
	    }
	}

	AffineTransform rot = AffineTransform.getRotateInstance(Math.PI/6);

	Path2D rectr = new Path2D.Double(rect, rot);
	Point2D ref = new Point2D.Double(100.0, 200.0);
	Point2D refr = rot.transform(ref, null);

	// check three-argument momentsOf
	double[][] moments1 = Path2DInfo.momentsOf(refr, rectr);
	double[][] moments2 = Path2DInfo.momentsOf(ref, rect, rot);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(moments1[i][j] - moments2[i][j]) > 0.1) {
		    System.out.println("moments1 != moments2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, moments1[i][j], moments2[i][j]);
		    err = true;
		}
	    }
	}

	if (err) System.exit(1);

	double[][] cmatrix = Path2DInfo.momentsOf(circ);
	double[][] pmatrix = Path2DInfo.momentsOf(path);
	double[][] rmatrix4 = Path2DInfo.momentsOf(rect4);
	double[][] rmatrix5 = Path2DInfo.momentsOf(rect5);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix4[i][j] - rmatrix5[i][j]) > 0.1) {
		    System.out.println("rmatrix4 != rmatrix5");
		    System.exit(1);
		}
	    }
	}

	// expecting (400^2/12) = 13,333.3333 for Iy and
	// 500^2/12  = 20833.3333 for Ix; 0 for Ixy
	// See https://wp.optics.arizona.edu/optomech/wp-content/uploads/sites/53/2016/10/OPTI_222_W61.pdf

	System.out.println("rmatrix: ");
	System.out.format("    | %8g  %8g |\n", rmatrix[0][0], rmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", rmatrix[1][0], rmatrix[1][1]);
	System.out.println();
	if (Math.abs(rmatrix[0][0] - 13333.33333333) > 0.1
	    || Math.abs(rmatrix[1][1] - 20833.3333333) > 0.1
	    || Math.abs(rmatrix[0][1]) > 1.e-10) {
	    System.out.println("bad rmatrix");
	    System.exit(1);
	}

	// Expecting 1/4 radius squared
	// for diagonal elements (625) for a perfect
	// circle); 0 for off-diagonal elements. Our circle is a close
	// approximation.
	// See http://hyperphysics.phy-astr.gsu.edu/hbase/tdisc.html
	System.out.println("cmatrix: ");
	System.out.format("    | %8g  %8g |\n", cmatrix[0][0], cmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", cmatrix[1][0], cmatrix[1][1]);
	System.out.println();

	if (Math.abs(cmatrix[0][0] - 625.0) > 1.e-2
	    || Math.abs(cmatrix[0][0] - cmatrix[1][1]) > 1.e-3
	    || Math.abs(cmatrix[0][1]) > 1.e-8) {
	    System.out.println("bad cmatrix");
	    System.out.println("cmatrix[0][0] - 625.0 = "
			       + (cmatrix[0][0] - 625.0));
	    System.exit(1);
	}


	System.out.println("pmatrix: ");
	System.out.format("    | %8g  %8g |\n", pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", pmatrix[1][0], pmatrix[1][1]);
	System.out.println();

	// diagonals should be 4 times higher than the ones for rect.
	// xy moment should be ((400)(500))/2.

	System.out.println("rmatrix4: ");
	System.out.format("    | %8g  %8g |\n", rmatrix4[0][0], rmatrix4[0][1]);
	System.out.format("    | %8g  %8g |\n", rmatrix4[1][0], rmatrix4[1][1]);
	System.out.println();

	if (Math.abs(rmatrix4[0][0] - 4*rmatrix[0][0]) > 0.1
	    || Math.abs(rmatrix4[1][1] - 4*rmatrix[1][1]) > 0.1
	    || Math.abs(rmatrix4[0][1] - 50000) > 0.1) {
	    System.out.println("bad rmatrix4");
	    System.exit(1);
	}

	// Test eigenvalues

	double[] eigenvalues = Path2DInfo.principalMoments(rmatrix);
	System.out.format("eigenvalues = %g and %g\n",
			  eigenvalues[0], eigenvalues[1]);
	double[][]eigenvectors = Path2DInfo.principalAxes(rmatrix);
	System.out.format("eigenvectors: (%g, %g) and (%g, %g)\n",
			  eigenvectors[0][0], eigenvectors[0][1],
			  eigenvectors[1][0], eigenvectors[1][1]);

	AffineTransform af45 = AffineTransform.getRotateInstance(Math.PI/4);
	Point2D tpt = new Point2D.Double(0.0, 1.0);
	System.out.println(tpt + " -> " + af45.transform(tpt, null));
	double[][] rmatrix45 = Path2DInfo.momentsOf(rect, af45);
	System.out.println("rmatrix45: ");
	System.out.format("    | %8g  %8g |\n",
			  rmatrix45[0][0], rmatrix45[0][1]);
	System.out.format("    | %8g  %8g |\n",
			  rmatrix45[1][0], rmatrix45[1][1]);
	System.out.println();

	double[] eigenvalues45 = Path2DInfo.principalMoments(rmatrix45);
	System.out.format("eigenvalues45 = %g and %g\n",
			  eigenvalues45[0], eigenvalues45[1]);
	double[][]eigenvectors45 = Path2DInfo.principalAxes(rmatrix45);
	System.out.format("eigenvectors45: (%g, %g) and (%g, %g)\n",
			  eigenvectors45[0][0], eigenvectors45[0][1],
			  eigenvectors45[1][0], eigenvectors45[1][1]);

	for (int i = 0; i < 2; i++) {
	    double[] tmp1 = MatrixOps.multiply(rmatrix45, eigenvectors45[i]);
	    double[] tmp2 = VectorOps.multiply(eigenvalues45[i],
					       eigenvectors45[i]);
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(tmp1[j]-tmp2[j]) > 1.e-10) {
		    System.out.println("i = " + i);
		    System.out.format("tmp1 = (%s, %s)\n", tmp1[0], tmp1[1]);
		    System.out.format("tmp2 = (%s, %s)\n", tmp2[0], tmp2[1]);
		    System.out.format("ratio = (%s, %s)\n", tmp1[0]/tmp2[0],
				      tmp1[1]/tmp2[1]);
		    System.out.println("eigenvalue/eigenvector inconsistent");
		    System.exit(1);
		}
	    }
	}

    }

    static RealValuedFunction ifx = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction ify = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.sin(Math.toRadians(t));
	    }
	};

    // copied from Path2DInfo.java with a name change.  This is the
    // one we tested for small n, and can be used for larger n for
    // verification purposes.
    public static Path2D convexHullGW(double x, double y, double[] coords,
				      int n)
    {
	// psuedocode: https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	if (n < 0) {
	    throw new IllegalArgumentException(("fourthArgNeg"));
	}
	if (coords.length < 2*n) {
	    throw new IllegalArgumentException(("argarray"));
	}
	//Gift-wrapping algorithm
	double sx = x;
	double sy = y;
	int pointOnHullInd = -1;
	for (int i = 0; i < n; i++) {
	    int ind = 2*i;
	    if (coords[ind] < sx) {
		pointOnHullInd = i;
		sx = coords[ind];
		sy = coords[ind+1];
	    } else if (coords[ind] == sx && coords[ind+1] < sy) {
		pointOnHullInd = i;
		sy = coords[ind+1];
	    }
	}
	int startInd = pointOnHullInd;
	int endPointInd;
	int k = 0;
	Path2D path = new Path2D.Double();
	if (startInd == -1) {
	    path.moveTo(x, y);
	} else {
	    int ind2 = startInd*2;
	    path.moveTo(coords[ind2], coords[ind2+1]);
	}

	do {
	    if (pointOnHullInd != startInd) {
		if (pointOnHullInd == -1) {
		    path.lineTo(x, y);
		} else {
		    int ind2 = 2*pointOnHullInd;
		    path.lineTo(coords[ind2], coords[ind2+1]);
		}
	    }
	    endPointInd = -1;
	    double hx = (pointOnHullInd == -1)? x: coords[2*pointOnHullInd];
	    double hy = (pointOnHullInd == -1)? y: coords[2*pointOnHullInd + 1];
	    for (int j = -1; j < n; j++) {
		boolean test = (endPointInd == pointOnHullInd);
		if (test == false && (endPointInd != j)) {
		    double ex = (endPointInd == -1)? x: coords[2*endPointInd];
		    double ey = (endPointInd == -1)? y: coords[2*endPointInd+1];
		    double v1x = ex - hx;
		    double v1y = ey - hy;
		    double v2x = ((j == -1)? x: coords[2*j]) - hx;
		    double v2y = ((j == -1)? y: coords[2*j+1]) - hy;
		    double cprod = (j == pointOnHullInd)? 1.0:
			(v1x*v2y - v2x*v1y);
		    // allow for floating-point errors
		    double ulpcp = Math.abs(v1x*Math.ulp(v2y))
			+ Math.abs(v2y*Math.ulp(v1x))
			+ Math.abs(v2x*Math.ulp(v1y))
			+ Math.abs(v1y*Math.ulp(v2x));
		    ulpcp *= 16.0;
		    if (cprod < -ulpcp) {
			// want counterclockise hull
			test = true;
		    } else if (Math.abs(cprod) <= ulpcp) {
			// the two vectors are parallel, so replace
			// if the new one is longer
			double distsq1 = v1x*v1x + v1y*v1y;
			double distsq2 = v2x*v2x + v2y*v2y;
			if (distsq1 < distsq2) {
			    test = true;
			}
		    }
		} else {
		}
		if (test) {
		    endPointInd = j;
		}
	    }
	    k++;
	    pointOnHullInd = endPointInd;
	} while (endPointInd != startInd);
	path.closePath();
	return path;
    }

    public static Path2D convexHullAndrew(double x, double y,
					  double[] coords, int n)
    {
	// psuedocode: https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	if (n < 0) {
	    throw new IllegalArgumentException(("fourthArgNeg"));
	}
	if (coords.length < 2*n) {
	    throw new IllegalArgumentException(("argarray"));
	}
	// See
	// https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	// for Andrew's monotone chain convex hull algorithm

	// use Andrew's monotone chain convex hull algorithm
	int[] points = new int[n+1];
	for (int i = 0; i < points.length; i++) {
	    points[i] = i-1;
	}
	PrimArrays.sort(points, new IntComparator() {
		public int compare(int ind1, int ind2) {
		    if (ind1 == -1) {
			if (ind2 == -1) {
			    return 0;
			} else {
			    int ind22 = ind2*2;
			    double x2 = coords[ind22];
			    if (x == x2) {
				double y2 = coords[ind22+1];
				if (y == y2) {
				    return 0;
				} else if (y < y2) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x < x2) {
				return -1;
			    } else {
				return 1;
			    }
			}
		    } else {
			int ind12 = ind1*2;
			double x1 = coords[ind12];
			if (ind2 == -1) {
			    if (x1 == x) {
				double y1 = coords[ind12+1];
				if (y1 == y) {
				    return 0;
				} else if (y1 < y) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x1 < x) {
				return -1;
			    } else {
				return 1;
			    }
			} else {
			    int ind22 = 2*ind2;
			    double x2 = coords[ind22];
			    if (x1 == x2)  {
				double y1 = coords[ind12+1];
				double y2 = coords[ind22+1];
				if (y1 == y2) {
				    return 0;
				} else if (y1 < y2) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x1 < x2) {
				return -1;
			    } else {
				return 1;
			    }
			}
		    }
		}
		public boolean equals(Object obj) {
		    return this == obj;
		}
	    });
	/*
	  for (int i = 0; i < points.length; i++) {
	  int ind = points[i];
	  int ind2 = ind*2;
	  double x4 = (ind == -1)? x: coords[ind2];
	  double y4 = (ind == -1)? y: coords[ind2+1];
	  }
	*/
	int[] upper = new int[points.length];
	int[] lower = new int[points.length];
	int topUpper = 0;
	int topLower = 0;
	for (int i = 0; i < points.length; i++) {
	    while (topLower > 1) {
		int ind1 = lower[topLower-2];
		int ind2 = lower[topLower-1];
		int ind12 = ind1*2;
		int ind22 = ind2*2;
		int ii = points[i];
		int ii2 = ii*2;
		double x0 = (ind1 == -1)? x: coords[ind12];
		double y0 = (ind1 == -1)? y: coords[ind12+1];
		double x1 = (ind2 == -1)? x: coords[ind22];
		double y1 = (ind2 == -1)? y: coords[ind22+1];
		double x2 = (ii == -1)? x: coords[ii2];
		double y2 = (ii == -1)? y: coords[ii2+1];
		double v1x = x1-x0;
		double v1y = y1-y0;
		double v2x = x2-x0;
		double v2y = y2-y0;
		double cprod = v1x*v2y - v2x*v1y;
		double ulpcp = Math.abs(v1x*Math.ulp(v2y))
		    + Math.abs(v2y*Math.ulp(v1x))
		    + Math.abs(v2x*Math.ulp(v1y))
		    + Math.abs(v1y*Math.ulp(v2x));
		ulpcp *= 16.0;
		if (cprod < ulpcp) {
		    topLower--;
		} else {
		    break;
		}
	    }
	    lower[topLower++] = points[i];
	}
	for (int i = points.length-1; i >= 0; i--) {
	    while (topUpper > 1) {
		int ind1 = upper[topUpper-2];
		int ind2 = upper[topUpper-1];
		int ind12 = ind1*2;
		int ind22 = ind2*2;
		int ii = points[i];
		int ii2 = ii*2;
		double x0 = (ind1 == -1)? x: coords[ind12];
		double y0 = (ind1 == -1)? y: coords[ind12+1];
		double x1 = (ind2 == -1)? x: coords[ind22];
		double y1 = (ind2 == -1)? y: coords[ind22+1];
		double x2 = (ii == -1)? x: coords[ii2];
		double y2 = (ii == -1)? y: coords[ii2+1];
		double v1x = x1-x0;
		double v1y = y1-y0;
		double v2x = x2-x0;
		double v2y = y2-y0;
		double cprod = v1x*v2y - v2x*v1y;
		double ulpcp = Math.abs(v1x*Math.ulp(v2y))
		    + Math.abs(v2y*Math.ulp(v1x))
		    + Math.abs(v2x*Math.ulp(v1y))
		    + Math.abs(v1y*Math.ulp(v2x));
		ulpcp *= 16.0;
		if (cprod < ulpcp) {
		    topUpper--;
		} else {
		    break;
		}
	    }
	    upper[topUpper++] = points[i];
	}
	Path2D result = new Path2D.Double();
	boolean firstTime = true;
	topLower--;
	topUpper--;
	for (int i = 0; i < topLower; i++) {
	    int ind = lower[i];
	    int ind2 = ind*2;
	    double x3 = (ind == -1)? x: coords[ind2];
	    double y3 = (ind == -1)? y: coords[ind2+1];
	    if (firstTime) {
		result.moveTo(x3, y3);
		firstTime = false;
	    } else {
		result.lineTo(x3, y3);
	    }
	}
	for (int i = 0; i < topUpper; i++) {
	    int ind = upper[i];
	    int ind2 = ind*2;
	    double x3 = (ind == -1)? x: coords[ind2];
	    double y3 = (ind == -1)? y: coords[ind2+1];
	    if (firstTime) {
		result.moveTo(x3, y3);
		firstTime = false;
	    } else {
		result.lineTo(x3, y3);
	    }
	}
	result.closePath();
	return result;
    }

    // used by the Aki-Toussaint heuristic, but we need a stand-alone
    // test so this code was copied.
    private static boolean insideHull(double x0, double y0,
				      double[]coords, int aclen,
				      double x, double y)
    {
	double cx, cy, v1x, v1y, v2x, v2y, cprod, ulpcp;
	double fx = x0;
	double fy = y0;
	for (int i = 0; i < aclen; i += 2) {
	    cx = coords[i];
	    cy = coords[i+1];
	    v1x = cx - x0;
	    v1y = cy - y0;
	    v2x = x - x0;
	    v2y = y - y0;
	    cprod = v1x*v2y - v2x*v1y;
	    ulpcp = Math.abs(v1x*Math.ulp(v2y))
		+ Math.abs(v2y*Math.ulp(v1x))
		+ Math.abs(v2x*Math.ulp(v1y))
		+ Math.abs(v1y*Math.ulp(v2x));
	    ulpcp *= 16.0;
	    if (!(cprod > ulpcp)) {
		return false;
	    }
	    x0 = cx;
	    y0 = cy;
	}
	v1x = fx - x0;
	v1y = fy - y0;
	v2x = x - x0;
	v2y = y - y0;
	cprod = v1x*v2y - v2x*v1y;
	ulpcp = Math.abs(v1x*Math.ulp(v2y))
	    + Math.abs(v2y*Math.ulp(v1x))
	    + Math.abs(v2x*Math.ulp(v1y))
	    + Math.abs(v1y*Math.ulp(v2x));
	ulpcp *= 16.0;
	if (!(cprod > ulpcp)) {
	    return false;
	}
	return true;
    }

    static void perfTest(BasicSplinePath2D bpath,
			 java.util.List<Path2DInfo.Entry> entries)
	throws Exception
    {
	Path2DInfo.Entry entry = entries.get(3);
	int type = entry.getType();
	double x0 = entry.getStart().getX();
	double y0 = entry.getStart().getY();
	double[] coords = entry.getCoords();
	Path2DInfo.SegmentData sd = entry.getData();
	double sum = 0.0;
	double u = 0.5;
	int N = 1000000;
	long stime = 0;
	long etime = 0;

	for (int i = 0; i < 10; i++) {
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.curvature(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.curvature ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    Path2DInfo.SegmentData data =
		new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().curvature(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.curvature ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.getX(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.getX ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().getX(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.getX ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.dxDu(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.dxDu ran for "
				   + (etime - stime));
	    }
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().dxDu(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.dxDu ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.d2xDu2(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.d2xDu2 ran for "
				   + (etime - stime));
	    }
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().d2xDu2(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.d2xDu2 ran for "
				   + (etime - stime));
	    }
	}
	System.out.println("sum = " + sum);
	Thread.sleep(5000);
    }

    public static void distTest() throws Exception {
	double x0 = 10.0;
	double y0 = 20.0;
	double coords2[] = {50.0, 60.0, 90.0, 100.0};
	double pcoords2[] = {
	    5.0, 20.0,
	    11.0, 21.0,
	    50.0, 65.0,
	    90.0, 105.0,
	    110.0, 100.0};

	for (int i = 0; i < pcoords2.length; i += 2) {
	    Point2D p = new Point2D.Double(pcoords2[i], pcoords2[i+1]);
	    double u = Path2DInfo.getMinDistBezierParm(p, x0, y0, coords2, 2);
	    Point2D cp =
		new Point2D.Double(Path2DInfo.getX(u, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2),
				   Path2DInfo.getY(u, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2));
	    double dist = p.distance(cp);
	    System.out.println("u = " + u + ", p = " + p + ", cp = " + cp
			       + ", dist = " + dist);
	    for (int j = 0; j <= 100; j++ ) {
		double v = i / 100.0;
		Point2D tp =
		    new Point2D.Double(Path2DInfo.getX(v, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2),
				   Path2DInfo.getY(v, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2));
		if ((p.distance(tp) - dist) < -1.e-10) {
		    System.out.println("min = " + p.distance(tp)
				       +" for v = " + v
				       +", found " + dist);
		    throw new Exception("dist failed");
		}
	    }
	}

	double coords3[] = {40.0, 50.0, 70.0, 80.0, 90.0, 100.0};
	double pcoords3[] = {
	    5.0, 20.0,
	    11.0, 21.0,
	    40.0, 55.0,
	    70.0, 85.0,
	    90.0, 105.0,
	    100.0, 100.0
	};

	for (int i = 0; i < pcoords3.length; i += 2) {
	    Point2D p = new Point2D.Double(pcoords3[i], pcoords3[i+1]);
	    double u = Path2DInfo.getMinDistBezierParm(p, x0, y0, coords3, 3);
	    Point2D cp =
		new Point2D.Double(Path2DInfo.getX(u, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3),
				   Path2DInfo.getY(u, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3));
	    double dist = p.distance(cp);
	    System.out.println("u = " + u + ", p = " + p + ", cp = " + cp
			       + ", dist = " + dist);
	    for (int j = 0; j <= 100; j++ ) {
		double v = i / 100.0;
		Point2D tp =
		    new Point2D.Double(Path2DInfo.getX(v, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3),
				   Path2DInfo.getY(v, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3));
		if ((p.distance(tp) - dist) < -1.e-10) {
		    System.out.println("min = " + p.distance(tp)
				       +" for v = " + v
				       +", found " + dist);
		    throw new Exception("dist failed");
		}
	    }
	}

    }

    public static void intersectionTest() throws Exception {
	System.out.println("intersection test ...");

	// case that seems to behave wierdly
	double startx1 = 489.8278750335096;
	double starty1 = 818.1573721008735;
	double tcoords1[] = {
	    544.5419202414199,
	    872.8714173087839,
	    606.5591327339994,
	    869.0811134079981,
	    708.9442719099992,
	    817.8885438199983
	};
	double startx2 = 700.0;
	double starty2 = 820.0;
	double tcoords2[] = {
	    800.0,
	    820.0,
	};

	for (int i = 0; i < 10; i++) {
	    double u = 1.0 - i/100.0;
	    System.out.format("... u = %g, (%g, %g)\n",
			      u,
			      Path2DInfo.getX(u, startx1, starty1,
					      PathIterator.SEG_CUBICTO,
					      tcoords1),
			      Path2DInfo.getY(u, startx1, starty1,
					      PathIterator.SEG_CUBICTO,
					      tcoords1));
	}

	double[] ourxy
	    = Path2DInfo.getSegmentIntersectionUVXY
	    (PathIterator.SEG_CUBICTO, startx1, starty1, tcoords1,
	     PathIterator.SEG_LINETO, startx2, starty2, tcoords2);
	for (int i = 0; i < ourxy.length; i++) {
	    System.out.format("ourxy[%d] = %g\n", i, ourxy[i]);
	}


	double x1 = 10.0;
	double y1 = 20.0;
	double x2 = 30.0;
	double y2 = -20.0;

	double xv = 50.0;
	double yv = 5.0;
	double coords1V[] = {50.0, 120.0};

	double xh = 40.0;
	double yh = 10.0;
	double coords1H[] = {130.0, 50.0};

	double coords1L[] = {100.0, 110.0};
	double coords2L[] = {100.0, 120.0};

	double coords1Q[] = {50.0, 60.0, 100.0, 110.0};
	double coords2Q[] = {50.0, 30.0, 100.0, 120.0};

	double coords1C[] = {40.0, 50.0, 60.0, 75.0, 100.0, 110.0};
	double coords2C[] = {40.0, 25.0, 60.0, 65.0, 100.0, 120.0};

	double xm = 140;
	double ym = -50;
	double coordsML[] = {150.0, 200.0};
	double coordsMQ[] = {160.0, 10.0, 150.0, 200.0};
	double coordsMC[] = {160.0, -20.0, 170.0, 90.0, 150.0, 200.0};

	double coordsHQ[] = {50.0, 150.0, 80.0, 20.0};
	double coordsHC[] = {50.0, 150.0, 50.0, 140.0,  80.0, 20.0};

	// linear/linear
	double[] uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv == null) throw new Exception();
	System.out.println("u = " + uv[0] + ", v = " + uv[1]);
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_LINETO, coords2L))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_LINETO, coords2L))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_LINETO,
					      coords2L),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_LINETO,
					      coords2L));

	    throw new Exception();
	}

	/*
	for (int i = 0; i <= 10; i++) {
	    double v = i/10.0;
	    System.out.format("v = %g, x = %g, y = %g\n", v,
			      Path2DInfo.getX(v, x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(v, x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	}
	*/

	// linear/quadratic
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xh, yh, coords1H,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], xh, yh,
					  PathIterator.SEG_LINETO, coords1H)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], xh, yh,
					PathIterator.SEG_LINETO, coords1H)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xv, yv, coords1V,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], xv, yv,
					  PathIterator.SEG_LINETO, coords1V)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], xv, yv,
					PathIterator.SEG_LINETO, coords1V)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getY(uv[0], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	// quadratic/linear
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xh, yh, coords1H);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], xh, yh,
					  PathIterator.SEG_LINETO, coords1H)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], xh, yh,
					PathIterator.SEG_LINETO, coords1H)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H));

	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xv, yv, coords1V);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], xv, yv,
					  PathIterator.SEG_LINETO, coords1V)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], xv, yv,
					PathIterator.SEG_LINETO, coords1V)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getY(uv[1], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V));

	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, x1, y1, coords1L);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));

	    throw new Exception();
	}

	// quadratic/quadratic

	System.out.println("... quadratic/quadratic case");


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... quadratic/cubic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... cubic/quadratic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));

	    throw new Exception();
	}

	System.out.println("... cubic/cubic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... no-intersections test");

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    System.out.println("uv[0] = " + uv[0] + ", uv[1] = " + uv[1]);
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    System.out.println("uv[0] = " + uv[0] + ", uv[1] = " + uv[1]);
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xm, ym, coordsMC,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_CUBICTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMC,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMC,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	System.out.println("... multiple intersection test");

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_LINETO, x1, y1, coords1L	     );
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_LINETO, x1, y1, coords1L);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_CUBICTO, x1, y1, coords1C	     );
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_CUBICTO, x1, y1, coords1C);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    throw new Exception();
	}

	System.out.println("... intersection test done");
    }


    public static void main(String argv[]) throws Exception {

	final double[] coefficients = new double[8];
	if (argv.length > 0 && argv[0].equals("timing")) {
	    // Performance test - a path is created using SplinePath2D,
	    // initialied with two functions: a linear function for an
	    // X coordinate and the first 8 terms of a Fourier series
	    // (sin only, no cos) with random coefficients.  The idea is
	    // to test a lot of curves that are smooth and somewhat
	    // typical of what users may generate.

	    double pcoords[] = new double[6];
	    DoubleRandomVariable drv = new
		UniformDoubleRV(-10.0, true, 10.0, true);
	    final int N = 1000;
	    SplinePath2D path = null;
	    SplinePath2D[] paths = new SplinePath2D[N];
	    RealValuedFunctOps fx = (u) -> {
		return u*100.0;
	    };
	    RealValuedFunctOps fy = (u) -> {
		double sum = 0.0;
		double uu = 1.0;
		for (int j = 0; j < coefficients.length; j++) {
		    sum += (j == 0)? coefficients[0]:
			Math.sin(u/(2*Math.PI)*j);
		}
		return sum;
	    };
	    BasicStats stats = new BasicStats.Population();
	    BasicStats stats3 = new BasicStats.Population();
	    // The smalller value has an excessively long running time
	    // for the timing test and is off by 2e-14 istead of 2e-15.
	    // double flatness = 0.0000000001;

	    // With this value delta3 (the delta for this test) is
	    // around 2e-12 instead of 2e-15, but the running time
	    // is 3312 ms instead.
	    double flatness = 0.0000001;
	    for (int i = 0; i < N; i++) {
		if (i > 0 && (i%100 == 0)) System.out.println("... i = " + i);
		for (int j = 0; j < coefficients.length; j++) {
		    coefficients[j] = drv.next();
		}
		path = new SplinePath2D(Path2D.WIND_EVEN_ODD,
				       fx, fy, 0.0, 1.0, 64, false);
		paths[i] = path;
		PathIterator pit = path.getPathIterator(null);
		double px0 = 0.0, py0 = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		while (!pit.isDone()) {
		    switch(pit.currentSegment(pcoords)) {
		    case PathIterator.SEG_MOVETO:
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    case PathIterator.SEG_LINETO:
			// only cubics
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    case PathIterator.SEG_QUADTO:
			// only cubics
			px0 = pcoords[2];
			py0 = pcoords[3];
			break;
		    case PathIterator.SEG_CUBICTO:
			sum1 += cubicLength(1.0, px0, py0, pcoords);
			sum2 += getCubicLength(1.0, px0, py0, pcoords);
			px0 = pcoords[4];
			py0 = pcoords[5];
			break;
		    default:
			throw new Exception();
		    }
		    pit.next();
		}
		double delta = Math.abs(sum1 - sum2)
		    / ((sum2 < 1.0)? 1.0: sum2);
		stats.add(delta);
		double sum3 = 0.0;
		pit = path.getPathIterator(null, flatness);
		while (!pit.isDone()) {
		    switch(pit.currentSegment(pcoords)) {
		    case PathIterator.SEG_MOVETO:
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    case PathIterator.SEG_LINETO:
			double dx = pcoords[0] - px0;
			double dy = pcoords[1] - py0;
			sum3 += Math.sqrt(dx*dx + dy*dy);
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    default:
			throw new Exception();
		    }
		    pit.next();
		}
		double delta3 = Math.abs(sum2 - sum3)
		    / ((sum2 < 1.0)? 1.0: sum2);
		stats3.add(delta3);
	    }
	    System.out.println("delta (analytic/numeric) = " + stats.getMean()
			       + " \u00B1 " + stats.getSDev());
	    System.out.println("delta (flattened/numeric) = " + stats3.getMean()
			       + " \u00B1 " + stats3.getSDev());

	    System.out.println("callCount = " + callCount);
	    System.out.println("numericCount = " + numericCount);
	    System.out.println("depthcnt = " + depthcnt);

	    int M = 10;
	    long tm0 = System.nanoTime();
	    for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
		    path = paths[i];
		    PathIterator pit = path.getPathIterator(null);
		    double px0 = 0.0, py0 = 0.0;
		    double sum1 = 0.0;
		    while (!pit.isDone()) {
			switch(pit.currentSegment(pcoords)) {
			case PathIterator.SEG_MOVETO:
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			case PathIterator.SEG_LINETO:
			    // only cubics
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			case PathIterator.SEG_QUADTO:
			    // only cubics
			    px0 = pcoords[2];
			    py0 = pcoords[3];
			    break;
			case PathIterator.SEG_CUBICTO:
			    sum1 += cubicLength(1.0, px0, py0, pcoords);
			    // sum2 += getCubicLength(1.0, px0, py0, pcoords);
			    px0 = pcoords[4];
			    py0 = pcoords[5];
			    break;
			default:
			    throw new Exception();
			}
			pit.next();
		    }
		}
	    }
	    long tm1 = System.nanoTime();
	    for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
		    path = paths[i];
		    PathIterator pit = path.getPathIterator(null);
		    double px0 = 0.0, py0 = 0.0;
		    double sum1 = 0.0;
		    while (!pit.isDone()) {
			switch(pit.currentSegment(pcoords)) {
			case PathIterator.SEG_MOVETO:
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			case PathIterator.SEG_LINETO:
			    // only cubics
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			case PathIterator.SEG_QUADTO:
			    // only cubics
			    px0 = pcoords[2];
			    py0 = pcoords[3];
			    break;
			case PathIterator.SEG_CUBICTO:
			    sum1 += cubicLength(1.0, px0, py0, pcoords);
			    // sum2 += getCubicLength(1.0, px0, py0, pcoords);
			    px0 = pcoords[4];
			    py0 = pcoords[5];
			    break;
			default:
			    throw new Exception();
			}
			pit.next();
		    }
		}

		PathIterator pit = path.getPathIterator(null);
		double px0 = 0.0, py0 = 0.0;
		double sum2 = 0.0;
		while (!pit.isDone()) {
		    switch(pit.currentSegment(pcoords)) {
		    case PathIterator.SEG_MOVETO:
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    case PathIterator.SEG_LINETO:
			// only cubics
			px0 = pcoords[0];
			py0 = pcoords[1];
			break;
		    case PathIterator.SEG_QUADTO:
			// only cubics
			px0 = pcoords[2];
			py0 = pcoords[3];
			break;
		    case PathIterator.SEG_CUBICTO:
			sum2 += getCubicLength(1.0, px0, py0, pcoords);
			px0 = pcoords[4];
			py0 = pcoords[5];
			break;
		    default:
			throw new Exception();
		    }
		    pit.next();
		}
	    }
	    long tm2 = System.nanoTime();
	    for (int i = 0; i < N; i++) {
		for (int j = 0; j < M; j++) {
		    path = paths[i];
		    PathIterator pit = path.getPathIterator(null, flatness);
		    double px0 = 0.0, py0 = 0.0;
		    double sum3 = 0.0;
		    while (!pit.isDone()) {
			switch(pit.currentSegment(pcoords)) {
			case PathIterator.SEG_MOVETO:
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			case PathIterator.SEG_LINETO:
			    double dx = pcoords[0] - px0;
			    double dy = pcoords[1] - px0;
			    sum3 += Math.sqrt(dx*dx + dy*dy);
			    px0 = pcoords[0];
			    py0 = pcoords[1];
			    break;
			default:
			    throw new Exception();
			}
			pit.next();
		    }
		}
	    }
	    long tm3 = System.nanoTime();
	    System.out.println("analytic: " + ((tm1 - tm0)/1000000)
			       +" ms");
	    System.out.println("numeric: " + ((tm2 - tm1)/1000000)
			       +" ms");
	    System.out.println("flattened (flatness = " + flatness
			       + ") = " + ((tm3 - tm2)/1000000)
			       +" ms");
	    System.out.println("Circle test");
	    Path2D cpath = Paths2D.createArc(100.0, 100.0, 100.0, 150.0,
					     2*Math.PI, Math.PI/360);
	    cpath.closePath();
	    PathIterator cpit = cpath.getPathIterator(null);
	    double cx0 = 0.0, cy0 = 0.0;
	    double cx = 0.0, cy = 0.0;
	    double csum1 = 0.0;
	    double csum2 = 0.0;
	    double tmp1, tmp2, tmp;
	    while (!cpit.isDone()) {
		switch(cpit.currentSegment(pcoords)) {
		case PathIterator.SEG_CLOSE:
		    tmp1 = cx - cx0;
		    tmp2 = cy - cy0;
		    tmp = Math.sqrt(tmp1*tmp1 + tmp2*tmp2);
		    csum1 += tmp;
		    csum2 += tmp;
		    break;
		case PathIterator.SEG_MOVETO:
		    cx0 = pcoords[0];
		    cy0 = pcoords[1];
		    cx = cx0;
		    cy = cy0;
		    break;
		case PathIterator.SEG_LINETO:
		    // only cubics
		    cx = pcoords[0];
		    cy = pcoords[1];
		    break;
		case PathIterator.SEG_QUADTO:
		    // only cubics
		    cx = pcoords[2];
		    cy = pcoords[3];
		    break;
		case PathIterator.SEG_CUBICTO:
		    csum1 += cubicLength(1.0, cx, cy, pcoords);
		    csum2 += getCubicLength(1.0, cx, cy, pcoords);
		    cx = pcoords[4];
		    cy = pcoords[5];
		    break;
		default:
		    throw new Exception();
		}
		cpit.next();
	    }
	    System.out.println("100\u03c0 = " + (100.0*Math.PI));
	    System.out.println("circular arc (analytic) = " + csum1);
	    System.out.println("circular arc (numeric) = " + csum2);
	    flatness /= 1000.0;
	    cpit = cpath.getPathIterator(null, flatness);
	    double csum3 = 0.0;
	    Adder adder = new Adder.Kahan();
	    int fcnt = 0;
	    while (!cpit.isDone()) {
		fcnt++;
		switch(cpit.currentSegment(pcoords)) {
		case PathIterator.SEG_CLOSE:
		    tmp1 = cx - cx0;
		    tmp2 = cy - cy0;
		    tmp = Math.sqrt(tmp1*tmp1 + tmp2*tmp2);
		    adder.add(tmp);
		    break;
		case PathIterator.SEG_MOVETO:
		    cx0 = pcoords[0];
		    cy0 = pcoords[1];
		    cx = cx0;
		    cy = cy0;
		    break;
		case PathIterator.SEG_LINETO:
		    tmp1 = pcoords[0] - cx;
		    tmp2 = pcoords[1] - cy;
		    tmp = Math.sqrt(tmp1*tmp1 + tmp2*tmp2);
		    adder.add(tmp);
		    cx = pcoords[0];
		    cy = pcoords[1];
		    break;
		default:
		    throw new Exception();
		}
		cpit.next();
	    }
	    System.out.println("flattened (flatness = " + flatness
			       + ") = " + adder.getSum());
	    System.out.println("... number of flattened segments = " + fcnt);

	    System.exit(0);
	}

	int sv = SL_GLQ_ITERS;
	double slx0 = 20.0;
	double sly0 = 21.0;
	double slcoords[]  = {32.0, 33.0, 22.0, 24.0, 7.0, 3.0};

	int slind = 1;
	double len1, len2;
	for(;;) {
	    SL_GLQ_ITERS = sv;
	    len2 = getCubicLength(1.0, slx0, sly0, slcoords);
	    SL_GLQ_ITERS = slind;
	    len1 = getCubicLength(1.0, slx0, sly0, slcoords);
	    if (Math.abs(len1 - len2) < 1.e-14) {
		System.out.println("SL_GLQ_ITERS = " + SL_GLQ_ITERS);
		System.out.println("len1 = " + len1);
		System.out.println("len2 = " + len2);
		break;
	    }
	    slind++;
	}
	SL_GLQ_ITERS = sv;

	badCubics();
	System.out.println("testing cubics");
	testCubicLength();

	System.out.println("callCount = " + callCount);
	System.out.println("numbericCount = " + numericCount);
	System.out.println("depthcnt = " + depthcnt);

	// testQuadLengthSigned(); -- moved to PolynomialTest (../math)
	// testXRootP2Signed(); -- tested in PolynomialTest (../math)

	System.out.println("testing quadratics");
	testQuadLength();


	centerOfMassTest();

	distTest();
	intersectionTest();

	Path2D path = new Path2D.Double();
	path.moveTo(-8.0, 0.0);
	path.lineTo(-8.0, -4.0);
	path.lineTo(0.0, -4.0);
	path.quadTo(8.0, -4.0, 8.0, 0.0);
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0);
	path.closePath();
	path.lineTo(-7.0, 0.0);
	path.lineTo(-7.0, 1.0);
	path.closePath();

	int j = 0;
	PathIterator pit = path.getPathIterator(null);
	double[] coords = new double[6];
	double x = 0.0; double y = 0.0;
	double x0 = 0.0; double y0 = 0.0;
	double lastX = 0.0; double lastY = 0.0;
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		x = coords[0];
		y = coords[1];
		lastX = x;
		lastY = y;
		if (x != Path2DInfo.getX(0.5, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path2DInfo.getX failed");
		}
		if (y != Path2DInfo.getY(0.5, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path2DInfo.getY failed");
		}
		if (0.0 != Path2DInfo.segmentLength(0.4, type, 0.0, 0.0,
						    coords)) {
		    throw new Exception("Path2DInfo.getSegmentLength failed");
		}
		if (0.0 != Path2DInfo.segmentLengthFunction(type, 0.0, 0.0,
							    coords)
		    .valueAt(0.4)) {
			throw new Exception
			    ("Path2DInfo.getSegmentLengthFunction failed");
		}
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y,
						 type, coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y,
						 type, coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-5) {
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-5) {
			throw new Exception("X derivative failed");
		    }
		    if (xpp != 0.0 || ypp != 0.0) {
			throw new Exception
			    ("non-zero second derivative");
		    }
		    if (xppp != 0.0 || yppp != 0.0) {
			throw new Exception
			    ("non-zero third derivative");
		    }
		}
		len1 = Path2DInfo.segmentLength(0.4, type, x, y, coords);
		len2 = Path2DInfo.segmentLength(type, x, y, coords) * 0.4;
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		if (Math.abs(len1 - Path2DInfo
			     .segmentLengthFunction(type, x, y, coords)
			     .valueAt(0.4)) > 1.e-10) {
		    throw new Exception();
		}
		x = coords[0];
		y = coords[1];
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y, type,
						 coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y, type,
						 coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " +(Math.abs(xp - (xv1-xv)/0.00001)));
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println("coords[2] = " + coords[2]
					   + ", coords[3] = " + coords[3]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));

			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 1.e-4) {
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 1.e-4) {
			throw new Exception("Y second derivative failed");
		    }
		    if (xppp != 0.0 || yppp != 0.0) {
			throw new Exception
			    ("non-zero third derivative");
		    }
		}
		len1 = Path2DInfo.segmentLength(0.4, type, x, y, coords);
		len2 = getQuadLength(0.4, x, y, coords);
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		if (Math.abs(len1 - Path2DInfo
			     .segmentLengthFunction(type, x, y, coords)
			     .valueAt(0.4)) > 1.e-10) {
		    throw new Exception();
		}
		x = coords[2];
		y = coords[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y,
						 type, coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y,
						 type, coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppv1 = Path2DInfo.d2xDu2(0.40001, x, y, type,
						     coords);
		    double yppv1 = Path2DInfo.d2yDu2(0.40001, x, y, type,
						     coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);

		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " + Math.abs(xp - (xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(xpp - (xp1-xp)/0.00001));
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(ypp - (yp1-yp)/0.00001));
			throw new Exception("Y second derivative failed");
		    }
		    if (Math.abs(xppp - (xppv1-xpp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(xppp - (xppv1-xpp)/0.00001));
			throw new Exception("X third derivative failed");
		    }
		    if (Math.abs(yppp - (yppv1-ypp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(yppp - (yppv1-ypp)/0.00001));
			throw new Exception("Y third derivative failed");
		    }
		}
		len1 = Path2DInfo.segmentLength(0.4, type, x, y, coords);
		len2 = getCubicLength(0.4, x, y, coords);
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		try {
		    if (Math.abs(len1 - Path2DInfo
				 .segmentLengthFunction(type, x, y, coords)
				 .valueAt(0.4)) > 1.e-10) {
			throw new Exception();
		    }
		} catch (ArithmeticException ae) {}
		x = coords[4];
		y = coords[5];
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		coords[0] = lastX;
		coords[1] = lastY;
		len1 = Path2DInfo.segmentLength(0.4, type, x, y, coords);
		len2 = Path2DInfo.segmentLength(type, x, y, coords) * 0.4;
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		x = coords[0];
		y = coords[1];
		break;
	    }
	    System.out.println("     ... static-method segment length = "
			       + Path2DInfo.segmentLength(type,
							  x0, y0,
							  coords));
	    if (type == PathIterator.SEG_QUADTO) {
		double qlen = quadLength(x0, y0, coords);
		System.out.println("     ... integrated segment length["
				   + j +"] = " + qlen);
	    }
	    System.out.println("     ... segment length["
			       + j
			       +"] = " +
			       Path2DInfo.segmentLength(path, j++));
	    x0 = x;
	    y0 = y;
	    pit.next();
	}

	Path2DInfo.SegmentData sd = null;

	for(Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D start = entry.getStart();
	    x0 = (start == null)? 0.0: start.getX();
	    y0 = (start == null)? 0.0: start.getY();
	    double[] coordinates = entry.getCoords();
	    int st = entry.getType();
	    if (st == PathIterator.SEG_MOVETO) continue;
	    double u = 0.4;
	    Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
	    sd = new Path2DInfo.SegmentData(st, x0, y0, coordinates, sd);
	    double x1 = Path2DInfo.getX(u, x0,  y0, st, coordinates);
	    double y1 = Path2DInfo.getY(u, x0,  y0, st, coordinates);
	    double dxDu1 = Path2DInfo.dxDu(u, x0,  y0, st, coordinates);
	    double dyDu1 = Path2DInfo.dyDu(u, x0,  y0, st, coordinates);
	    double d2xDu21 = Path2DInfo.d2xDu2(u, x0,  y0, st, coordinates);
	    double d2yDu21 = Path2DInfo.d2yDu2(u, x0,  y0, st, coordinates);
	    double d3xDu31 = Path2DInfo.d2xDu2(u, x0,  y0, st, coordinates);
	    double d3yDu31 = Path2DInfo.d2yDu2(u, x0,  y0, st, coordinates);

	    double dsDu1 = Path2DInfo.dsDu(u, x0,  y0, st, coordinates);
	    double d2sDu21 = Path2DInfo.dsDu(u, x0,  y0, st, coordinates);
	    double curv1 = Path2DInfo.curvature(u, x0,  y0, st, coordinates);
	    double x2 = sd.getX(uv);
	    double y2 = sd.getY(uv);
	    double dxDu2 = sd.dxDu(uv);
	    double dyDu2 = sd.dyDu(uv);
	    double d2xDu22 = sd.d2xDu2(uv);
	    double d2yDu22 = sd.d2yDu2(uv);
	    double d3xDu32 = sd.d2xDu2(uv);
	    double d3yDu32 = sd.d2yDu2(uv);
	    double dsDu2 = sd.dsDu(uv);
	    double d2sDu22 = sd.dsDu(uv);
	    double curv2 = sd.curvature(uv);
	    if (Math.abs(x1 - x2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + x1 + " != " + x2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(y1 - y2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + y1 + " != " + y2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dxDu1 - dxDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dxDu1 + " != " + dxDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dyDu1 - dyDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dyDu1 + " != " + dyDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2xDu21 - d2xDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2yDu21 - d2yDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d3xDu31 - d3xDu32) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d3yDu31 - d3yDu32) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dsDu1 - dsDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dsDu1 + " != " + dsDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2sDu21 - d2sDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2sDu21 + " != " + d2sDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(curv1 - curv2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + curv1 + " != " + curv2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	}

	int segStart = 2;
	int segEnd = 6;
	double total = 0.0;
	double segtotal = 0.0;
	for(Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    System.out.println("index = " + entry.getIndex()
			       + ", type = " + entry.getTypeString()
			       + ", start = " + entry.getStart()
			       + ", end = " + entry.getEnd()
			       + ", length = " +entry.getSegmentLength());
	    total += entry.getSegmentLength();
	    int ind = entry.getIndex();
	    if (ind >= segStart && ind < segEnd) {
		segtotal += entry.getSegmentLength();
	    }
	    RealValuedFunctOps lf = entry.getSegmentLengthFunction();
	    if (lf != null) {
		if (Math.abs(entry.getSegmentLength() - lf.valueAt(1.0))
		    > 1.e-10) {
		    System.out.println("lf.valueAt(1.0) = "
				       + lf.valueAt(1.0));
		    System.out.println("entry.getSegmentLength() = "
				       + entry.getSegmentLength());

		    throw new Exception();
		}
	    }
	    double[] c = entry.getCoords();
	    System.out.println("coords[0] = " + c[0]);
	}


	System.out.println("path length = " + Path2DInfo.pathLength(path));
	System.out.println("path length for segments ["
			   + segStart + ", " + segEnd +") = "
			   + Path2DInfo.pathLength(path, segStart, segEnd));
	if (Math.abs(total - Path2DInfo.pathLength(path)) > 1.e-8) {
	    System.out.println("wrong path length");
	    System.exit(1);
	}
	if (Math.abs(segtotal - Path2DInfo.pathLength
		     (path, segStart, segEnd)) > 1.e-8) {
	    System.out.println("wrong segment-range length");
	    System.exit(1);
	}

	Path2D path1 = new Path2D.Double();
	Path2D path2 = new Path2D.Double();

	path1.moveTo(0.0, 0.0);
	path2.moveTo(0.0, 0.0);
	path1.curveTo(100.0, 0.0, 100.0, 0.0, 100.0, 100.0);
	path2.quadTo(100.0, 0.0, 100.0, 100.0);
	
	System.out.println("path1 length: "
			   + Path2DInfo.segmentLength(path1, 1));
	System.out.println("path2 length: "
			   + Path2DInfo.segmentLength(path2, 1));
	coords = new double[6];
	coords[0] = 165.2016; coords[1] = 3.9624;
	coords[2] = 165.2016; coords[3] = 15.8596;
		
	System.out.println("canned length = "
			   + Path2DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624,
			    coords));

	RealValuedFunction fx = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
		public double derivAt(double theta) {
		    return -100.0 * Math.sin(theta);
		}
	    };
	RealValuedFunction fy = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.sin(theta);
		}
		public double derivAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
	    };
	CubicSpline xspline = new CubicSpline1(fx, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline yspline = new CubicSpline1(fy, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	pit = Path2DInfo.getPathIterator(null, xspline, yspline);
	path = new Path2D.Double();
	path.append(pit, false);

	if (Math.abs(Path2DInfo.pathLength(path) - Math.PI*100.0) > 1.e-3) {
	    System.out.println("path length for semicircle = "
			       + Path2DInfo.pathLength(path)
			       + ", expecting " + Math.PI*100.0);
	    System.exit(1);
	}

	System.out.println("first random path test");
			       
	path1 = new Path2D.Double();
	path1.moveTo(-50.0, 3.061616997868383E-14);
	path1.curveTo(-46.601769298462514, 15.141832724372453,
		      -48.50388233105287, -15.759866713122202,
		      -42.068883707497285, -30.564833119208576);
	path1.lineTo(-31.152618371501028, -42.87790070187225);
	path2 = new Path2D.Double();
	path2.moveTo(-45.038529126015305, -20.247367433334002);
	path2.curveTo(-44.309398690673675, -23.99897723551772,
		      -43.3558834322084, -27.603839837991313,
		      -42.068883707497285, -30.564833119208576);
	path2.lineTo(-40.97725717389764, -31.79613987747496);

	double u1 = 0.8;
	double u2 = 1.1;
	double path1Length = Path2DInfo.pathLength(path1);
	Path2D path1a = new Path2D.Double();
	path1a.append(new FlatteningPathIterator2D
		      (path1.getPathIterator(null), 1.0), false);
	BasicSplinePath2D path1b = new BasicSplinePath2D(path1);
	// path1b.setIntervalNumber(128);
	double path1aLength = Path2DInfo.pathLength(path1a);
	System.out.println("path1Length = " + path1Length);
	System.out.println("path1aLength = " + path1aLength);
	System.out.println("path1bLength = "
			   + Path2DInfo.pathLength(path1b));
	System.out.println("path1bLength = " + path1b.getPathLength());
	System.out.println("path1bLength (u1 to u2) = " 
			   + path1b.getPathLength(u1, u2));
	System.out.println("path2Length = " + Path2DInfo.pathLength(path2));

	System.out.println("second random path test");

	u1 = 0.1; u2 = 0.2;
	path1 = new Path2D.Double();
	path1.moveTo(-32.755801403744655, 100.8119907272862);
	path1.curveTo(-5.150015853103061E-14, 105.0,
		      -62.893021995294355, 86.56481839811957,
		      -87.37383539249424, 63.48080724758722);
	path2 = new Path2D.Double();
	path2.moveTo(-25.664464652595242, 101.40767214418844);
	path2.curveTo(-24.16779994820443, 101.39198361529388,
		      -23.493562445806894, 101.16803753897932,
		      -23.507691113405496, 100.7538082765707);

	path1Length = Path2DInfo.pathLength(path1);
	path1a = new Path2D.Double();
	path1a.append(new FlatteningPathIterator2D
		      (path1.getPathIterator(null), 1.0), false);
	path1b = new BasicSplinePath2D(path1);
	// path1b.setIntervalNumber(16);
	path1aLength = Path2DInfo.pathLength(path1a);
	System.out.println("path1Length = " + path1Length);
	System.out.println("path1aLength = " + path1aLength);
	System.out.println("path1bLength = "
			   + Path2DInfo.pathLength(path1b));
	System.out.println("path1bLength = " + path1b.getPathLength());
	System.out.println("path1bLength (u1 to u2) = " 
			   + path1b.getPathLength(u1, u2));
	System.out.println("path2Length = " + Path2DInfo.pathLength(path2));

	double ocoords[] = {1.0, 2.0, 6.0, 7.0, 10.0, 20.0};
	double ocoords1[] = {6.0, 7.0, 10.0, 20.0};
	double[] ncoords = new double[8];
	double[] ncoords1 = new double[6];

	Path2DInfo.elevateDegree(2, ncoords, 0, ocoords, 0);
	Path2DInfo.elevateDegree(2, ncoords1, 1.0, 2.0, ocoords1);

	if (ncoords[0] != ocoords[0] || ncoords[1] != ocoords[1]
	    || ncoords[6] != ocoords[4] || ncoords[7] != ocoords[5]) {
	    throw new Exception("ncoords error");
	}

	for (int i = 0; i < 6; i++) {
	    if (Math.abs(ncoords1[i] - ncoords[i+2]) > 1.e-10) {
	        System.out.format("ncoords1[%d] = %g while ncoords[%d] = %g\n",
				  i, ncoords1[i], i+2, ncoords[i+2]);
		throw new Exception("ncoords error");
	    }
	}

	for (int i = 0; i <= 20; i++) {
	    double u = i/20.0;
	    if (u < 0) u = 0.0;
	    if (u > 1) u = 1.0;
	    double v1 = Path2DInfo.getX(u, 1.0, 2.0,
					PathIterator.SEG_QUADTO,
					ocoords1);
	    double v2 =  Path2DInfo.getX(u, 1.0, 2.0,
					 PathIterator.SEG_CUBICTO,
					 ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getX(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path2DInfo.getY(u, 1.0, 2.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path2DInfo.getY(u, 1.0, 2.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getY(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }

	}

	System.out.println("counterclockwise test");

	path = new Path2D.Double();

	path.moveTo(0.0, 0.0);
	path.lineTo(1.0, 0.0);
	path.quadTo(2.0, 0.5, 1.0, 1.0);
	path.curveTo(0.66, 1.5, 0.33, 1.5, 0.0, 1.0);
	path.closePath();

	if (!Path2DInfo.isCounterclockwise(path)) {
	    System.out.println("counterclockwise = "
			       + Path2DInfo.isCounterclockwise(path));
	    System.exit(1);
	}
	if (Path2DInfo.isClockwise(path)) {
	    System.out.println("clockwise = "
			       + Path2DInfo.isClockwise(path));
	    System.exit(1);
	}


	System.out.println("lineSegmentsIntersect test");
	{
	    double xx1 = 0.0, yy1 = 0.0,
		xx2 = 25.0,  yy2 = 55.90169943749474,
		lxx1 = -50.0, lyy1 = 0.0,
		lxx2 = -25.0, lyy2 = 55.90169943749474;

	    if (Path2DInfo.lineSegmentsIntersect(xx1, yy1, xx2, yy2,
						 lxx1, lyy1, lxx2, lyy2)) {
		System.out.println("bad intersection [0]");
		System.exit(1);
	    }

	    double x1 = 38.428887674834904;
	    double y1 = 0.0;
	    double x2 = 37.66652173679645;
	    double y2 = 3.0305813264980603;
	    double x3 = 39.95361955091182;
	    double y3 = -6.06116265299612;
	    double x4 = 39.19125361287336;
	    double y4 = -3.0305813264980594;


	    if (Path2DInfo.lineSegmentsIntersect(x1, y1, x2, y2,
						 x3, y3, x4, y4)) {
		System.out.println("bad intersection [1]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, 1.0)) {
		System.out.println("bad intersection [2]");
		System.exit(1);
	    }
	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, 1.0)) {
		System.out.println("bad intersection [3]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [4]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, -1.0)) {
		System.out.println("bad intersection [5]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, 1.0, -1.0, 1.0, 1.0)) {
		System.out.println("bad intersection [6]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [7]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, -1.0, 0.0, 1.0, 0.0)) {
		System.out.println("bad intersection [8]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 3.0, 0.0, 1.0, 0.0, 2.0, 0.0)) {
		System.out.println("bad intersection [9]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, -1.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [9]");
		System.exit(1);
	    }

	    x1 = 0.30470092700496826;
	    y1 = 9.883712979468449;
	    x2 = 7.375104376190603;
	    y2 = 4.817645302894979;
	    x3 = 5.272251521993861;
	    y3 = 9.875247930124923;
	    x4 = 1.6339912769768958;
	    y4 = 5.947394907277731;
	       
	    if (!Path2DInfo.lineSegmentsIntersect
		(x1, y1, x2, y2, x3, y3, x4, y4)) {
		System.out.println("bad intersection [10]");
		System.exit(1);
	    }

	    DoubleRandomVariable rv = new
		UniformDoubleRV(0.0, true, 10.0, true);
	    int cnt = 0;
	    for (int i = 0; i < 1000000; i++) {
		x1 = rv.next();
		y1 = rv.next();
		x2 = rv.next();
		y2 = rv.next();
		x3 = rv.next();
		y3 = rv.next();
		x4 = rv.next();
		y4 = rv.next();

		if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)
		    != Path2DInfo.lineSegmentsIntersect(x1, y1, x2, y2,
							x3, y3, x4, y4)) {
		    cnt++;
		    if (cnt == 1) {
			System.out.println("*** intersect methods differ");
			System.out.format("x1 = %s\n", x1);
			System.out.format("y1 = %s\n", y1);
			System.out.format("x2 = %s\n", x2);
			System.out.format("y2 = %s\n", y2);
			System.out.format("x3 = %s\n", x3);
			System.out.format("y3 = %s\n", y3);
			System.out.format("x4 = %s\n", x4);
			System.out.format("y4 = %s\n", y4);
			System.out.println("Path2DInfo: "
					   + Path2DInfo.lineSegmentsIntersect
					   (x1, y1, x2, y2, x3, y3, x4, y4)
					   + "; Line2D: "
					   + Line2D.linesIntersect
					   (x1, y1, x2, y2, x3, y3, x4, y4));
		    }
		}
	    }
	    System.out.println("Path2D and Line2D methods differ "
			       + cnt + " times out of 1000000 tries");
	    System.out.println("... OK");
	}

	SplinePathBuilder.CPoint cpoints1c[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(ifx, ify, 0.0, 360.0, 36),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	spb.append(cpoints1c);
	BasicSplinePath2D bpath = spb.getPath();
	java.util.List<Path2DInfo.Entry> entries = Path2DInfo.getEntries(bpath);
	perfTest(bpath, entries);

	double[] vector1 = new double[4];
	double[] vector2 = new double[4];
	double[] vector3 = new double[2];
	double[] vector4 = new double[2];
	Path2DInfo.UValues uv = new Path2DInfo.UValues(0.5);
	Path2DInfo.UValues uv0 = new Path2DInfo.UValues(0.0);
	Path2DInfo.UValues uv1 = new Path2DInfo.UValues(1.0);
	int iu = 0;
	for (Path2DInfo.Entry entry: entries) {
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    double u = iu + 0.5;
	    iu++;
	    int type = entry.getType();
	    coords = entry.getCoords();
	    Path2DInfo.SegmentData data = entry.getData();
	    double kappa1 = Path2DInfo.curvature(0.5,
						 start.getX(), start.getY(),
						 type, coords);
	    double kappa2 = data.curvature(uv);
	    double kappa3 = bpath.curvature(u);
	    bpath.getTangent(u, vector3);

	    System.out.format("kappa1 = %g, kappa2 = %g, kappa3 = %g \n",
			      kappa1, kappa2, kappa3);
	    System.out.format("tangent ... (%g, %g)\n", vector3[0], vector3[1]);
	    Path2DInfo.getTangent(0.5, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.5, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv, vector2, 0);
	    data.getTangent(uv, vector2, 2);
	    System.out.format("(%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (Math.abs(vector1[0]-vector2[0]) > 1.e-10
		|| Math.abs(vector1[1]-vector2[1]) > 1.e-10
		|| Math.abs(vector1[2]-vector2[2]) > 1.e-10
		|| Math.abs(vector1[3]-vector2[3]) > 1.e-10
		|| Math.abs(vector1[0]-vector1[2]) > 1.e-10
		|| Math.abs(vector1[1]-vector1[3]) > 1.e-10
		|| Math.abs(vector2[0]-vector2[2]) > 1.e-10
		|| Math.abs(vector2[1]-vector2[3]) > 1.e-10) {
		throw new Exception ("tangents not consistent");
	    }

	    Path2DInfo.getTangent(0.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv0, vector2, 0);
	    data.getTangent(uv0, vector2, 2);
	    System.out.format("u = 0.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    Path2DInfo.getTangent(1.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(1.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv1, vector2, 0);
	    data.getTangent(uv1, vector2, 2);
	    System.out.format("u = 1.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	}

	bpath = new BasicSplinePath2D();
	bpath.moveTo(10.0, 20.0);
	path.lineTo(20.0, 30.0);
	path.quadTo(25.0, 35.0, 30.0, 40.0);
	path.quadTo(30.0, 40.0, 35.0, 45.0);
	path.quadTo(40.0, 50.0, 40.0, 50.0);
	path.curveTo(45.0, 55.0, 50.0, 60.0, 55.0, 65.0);
	path.curveTo(55.0, 65.0, 60.0, 70.0, 65.0, 75.0);
	path.curveTo(70.0, 80.0, 75.0, 85.0, 75.0, 85.0);
	path.curveTo(75.0, 85.0, 80.0, 90.0, 80.0, 90.0);
	path.curveTo(85.0, 95.0, 85.0, 95.0, 90.0, 100.0);

	entries =  Path2DInfo.getEntries(bpath);

	for (Path2DInfo.Entry entry: entries) {
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    double u = iu + 0.5;
	    iu++;
	    int type = entry.getType();
	    coords = entry.getCoords();
	    Path2DInfo.SegmentData data = entry.getData();
	    double kappa1 = Path2DInfo.curvature(0.5,
						 start.getX(), start.getY(),
						 type, coords);
	    double kappa2 = data.curvature(uv);
	    double kappa3 = bpath.curvature(u);
	    bpath.getTangent(u, vector3);

	    System.out.format("kappa1 = %g, kappa2 = %g, kappa3 = %g \n",
			      kappa1, kappa2, kappa3);
	    System.out.format("tangent ... (%g, %g)\n", vector3[0], vector3[1]);
	    Path2DInfo.getTangent(0.5, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.5, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv, vector2, 0);
	    data.getTangent(uv, vector2, 2);
	    System.out.format("(%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (Math.abs(vector1[0]-vector2[0]) > 1.e-10
		|| Math.abs(vector1[1]-vector2[1]) > 1.e-10
		|| Math.abs(vector1[2]-vector2[2]) > 1.e-10
		|| Math.abs(vector1[3]-vector2[3]) > 1.e-10
		|| Math.abs(vector1[0]-vector1[2]) > 1.e-10
		|| Math.abs(vector1[1]-vector1[3]) > 1.e-10
		|| Math.abs(vector2[0]-vector2[2]) > 1.e-10
		|| Math.abs(vector2[1]-vector2[3]) > 1.e-10) {
		throw new Exception ("tangents not consistent");
	    }

	    Path2DInfo.getTangent(0.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.000001, vector3, 0,
				  start.getX(), start.getY(),
				  type, coords);
	    if (Math.abs(VectorOps.dotProduct(vector1, 0, vector3, 0, 2)
			 - 1.0) > 1.e-10) {
		throw new Exception("bad value, u=0.0");
	    }
	    Path2D spath = new Path2D.Double();
	    spath.moveTo(start.getX(), start.getY());
	    switch(type) {
	    case PathIterator.SEG_LINETO:
		spath.lineTo(coords[0], coords[1]);
		break;
	    case PathIterator.SEG_QUADTO:
		spath.quadTo(coords[0], coords[1], coords[2], coords[3]);
		break;
	    case PathIterator.SEG_CUBICTO:
		spath.curveTo(coords[0], coords[1], coords[2], coords[3],
			      coords[4], coords[5]);
		break;
	    default:
		spath = null;
	    }
	    data.getTangent(uv0, vector2, 0);
	    data.getTangent(uv0, vector2, 2);
	    System.out.format("u = 0.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (spath != null) {
		Path2DInfo.getTangent(spath, Path2DInfo.Location.START,
				      vector4, 0);
		if (Math.abs(vector1[0]-vector4[0]) > 1.e-10
		    || Math.abs(vector1[1]-vector4[1]) > 1.e-10) {
		    throw new Exception("getTangent with location");
		}
	    }
	    Path2DInfo.getTangent(1.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(1.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv1, vector2, 0);
	    data.getTangent(uv1, vector2, 2);
	    Path2DInfo.getTangent(0.999999,
				  vector3, 0,  start.getX(), start.getY(),
				  type, coords);

	    if (Math.abs(VectorOps.dotProduct(vector1, 0, vector3, 0, 2)
			 - 1.0) > 1.e-10) {
		throw new Exception("bad value, u=1.0");
	    }
	    System.out.format("u = 1.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (spath != null) {
		Path2DInfo.getTangent(spath, Path2DInfo.Location.END,
				       vector4, 0);
		if (Math.abs(vector1[0]-vector4[0]) > 1.e-10
		    || Math.abs(vector1[1]-vector4[1]) > 1.e-10) {
		    throw new Exception("getTangent with location");
		}
	    }
	}

	{
	    double x1 = 10.0, y1 = 30.0;
	    double x2 = 70.0, y2 = 80.0;
	    double x3 = 50.0, y3 = -10.0;
	    double x4 = 40.0, y4 = 100.0;
	    double[] array = new double[8];

	    if (Path2DInfo.getLineIntersectionUV(x1, y1, x2, y2,
						 x3, y3, x4, y4,
						 array, 1)) {
		double u = array[1];
		double v = array[2];
		double oneMu = 1 - u;
		double oneMv = 1 - v;
		array[6] = (x1*oneMu + x2*u);
		array[7] = (y1*oneMu + y2*u);
		if (Math.abs(array[6] - (x3*oneMv + x4*v)) > 1.e-10
		    || Math.abs(array[7] - (y3*oneMv + y4*v)) > 1.e-10) {
		    throw new Exception("intersection points differ");
		}
		System.out.println("intersection: x = " +array[6]
				   + ", y = " + array[7]);
		System.out.println("intersection: u = " +array[1]
				   + ", v = " + array[2]);

	    } else {
		throw new Exception("getLineIntersectionUV returned false");
	    }
	    if (Path2DInfo.getLineIntersectionXY(x1, y1, x2, y2,
						 x3, y3, x4, y4,
						 array, 1)) {
		if (Math.abs(array[6] - array[1]) > 1.e-10
		    || Math.abs(array[7] - array[2]) > 1.e-10) {
		    throw new Exception("intersection point incorrect");
		}
		System.out.println("intersection: x = " +array[1]
				   + ", y = " + array[2]);
	    } else {
		throw new Exception("getLineIntersection returned false");
	    }
	}

	{
	    double[] array = new double[18+3];
	    Path2D tpath1 = new Path2D.Double();
	    Path2D tpath2 = new Path2D.Double();
	    Path2D tpath3 = new Path2D.Double();
	    tpath1.moveTo(10.0, 20.0);
	    tpath1.lineTo(11.0, 22.0);
	    tpath2.moveTo(10.0, 20.0);
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.moveTo(10.0, 20.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);
	    Point2D point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    Path2D[] paths = {tpath1, tpath2, tpath3};
	    for (Path2D tpath: paths) {
		Point2D start = Path2DInfo.getStart(tpath);
		if (start.getX() != 10.0 || start.getY() != 20.0) {
		    throw new Exception("wrong starting point");
		}
	    }


	    array[0] =  1.0 / Math.sqrt(5.0);
	    array[1] = 2.0 / Math.sqrt(5.0);
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.START, array, 2);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.START, array, 4);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.START, array, 6);
	    for (int i = 2; i < 8; i++) {
		if (Math.abs(array[i] - array[i%2]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);
	    double etangents[] = {
		1.0/Math.sqrt(5.0), 2.0/Math.sqrt(5.0),
		2.0/Math.sqrt(4.0+25.0), 5.0/Math.sqrt(4.0+25.0),
		5.0/Math.sqrt(25.0+49.0), 7.0/Math.sqrt(25.0+49.0)
	    };
	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }
	    tpath1.closePath();
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 10.0 && point.getY() != 20.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.closePath();
	    tpath3.closePath();
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    double ctangents[] = {
		-1.0 / Math.sqrt(5.0), -2.0 / Math.sqrt(5.0),
		-3.0 / Math.sqrt(9.0 + 49.0), -7.0 / Math.sqrt(9.0 + 49.0),
		-8.0 / Math.sqrt(64.0 + 196.0), -14.0 / Math.sqrt(64 + 196.0)
	    };

	    for (int i = 0; i < 6; i++) {
		if (Math.abs(array[i] - ctangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }

	    tpath1.lineTo(11.0, 22.0);
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);

	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }

	    tpath1.moveTo(10.0, 20.0);
	    tpath1.lineTo(11.0, 22.0);
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.moveTo(10.0, 20.0);
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.moveTo(10.0, 20.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);

	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }


	    Arrays.fill(array, 0.0);
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.START, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.START, array, 3);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.START, array, 6);
	    Path2DInfo.getNormal(tpath1, Path2DInfo.Location.START, array, 9);
	    Path2DInfo.getNormal(tpath2, Path2DInfo.Location.START, array, 12);
	    Path2DInfo.getNormal(tpath3, Path2DInfo.Location.START, array, 15);
	    for (int i = 0; i < 3; i++) {
		int offset1 = i*3;
		int offset2 = offset1 + 9;
		VectorOps.crossProduct(array, 18,
				       array, offset1,
				       array, offset2);
		if (Math.abs(array[18]) > 1.e-10 ||
		    Math.abs(array[19]) > 1.e-10 ||
		    Math.abs(array[20] - 1.0) > 1.e-10) {
		    System.out.format
			("(%g, %g, %g) X (%g, %g, %g) = (%g, %g, %g)\n",
			 array[i], array[i+1], array[i+2],
			 array[i+9], array[i+1+9], array[i+2+9],
			 array[18], array[19], array[20]);
		    throw new Exception("bad normal");
		}
	    }
	}

	System.out.println("... OK");
	System.out.println("Convex Hull");
	double hx = 10.0, hy = 20.0;
	double hcoords1a[] = {30.0, 40.0};
	double hcoords1b[] = {-10.0, 40.0};
	double hcoords1c[] = {-10.0, -40.0};
	double hcoords1d[] = {30.0, -40.0};

	double hexpecting1a[] = {10.0, 20.0, 30.0, 40.0};
	double hexpecting1b[] = {-10.0, 40.0, 10.0, 20.0};
	double hexpecting1c[] = {-10.0, -40.0, 10.0, 20.0};
	double hexpecting1d[] = {10.0, 20.0, 30.0, -40.0};
	Path2D hpath = Path2DInfo.convexHull(hx, hy, hcoords1a, 1);

	int hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1b, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1c, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1d, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	double hcoords2a[] = {21.0, 22.0, 30.0, 40.0};
	double hcoords2b[] = {-10.0, 40.0, 21.0, 22.0};
	double hcoords2c[] = {-10.0, -40.0, 21.0, 22.0};
	double hcoords2d[] = {30.0, -40.0, 21.0, 22.0};

	double hexpecting2a[] = {10.0, 20.0,  21.0, 22.0, 30.0, 40.0};
	double hexpecting2b[] = {-10.0, 40.0, 10.0, 20.0, 21.0, 22.0};
	double hexpecting2c[] = {-10.0, -40.0, 21.0, 22.0, 10.0, 20.0};
	double hexpecting2d[] = {10.0, 20.0, 30.0, -40.0, 21.0, 22.0};
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2a, 2);

	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2b, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2c, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2d, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}

	double atx = -100.0;
	double aty = -100.0;
	double atcoords[] = {100.0, -100.0, 100.0, 100.0, -100.0, 100.0};
	if (insideHull(atx, aty, atcoords, 6, 0.0, 0.0) == false) {
	    throw new Exception("insideHull");
	}

	double hcoords3a[] = {30.0, 20.1, 40.0, 22.2, 50.0, 22.3};
	double hcoords3b[] = {30.0, 20.1, 40.0, -22.2, 50.0, 22.3};
	double hcoords3c[] = {30.0, -20.1, 40.0, 22.2, 50.0, 22.3};
	double hcoords3d[] = {20.0, 40.0, 30.0, 60.0, 60.0, 120.0};
	double hcoords3e[] = {30.0, 20.4, 40.0, 22.2, 50.0, 22.3};
	double hcoords3f[] = {30.0, 20.4, 40.0, -22.2, 50.0, 22.3};
	double hcoords3g[] = {30.0, -20.4, 40.0, 22.2, 50.0, 22.3};
	double hcoords3h[] = {30.0, 20.1, 40.0, 20.2, 50.0, 20.0};

	double hexpecting3a[] = {10.0, 20.0, 30.0, 20.1, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3b[] = {10.0, 20.0, 40.0, -22.2, 50.0, 22.3};
	double hexpecting3c[] = {10.0, 20.0, 30.0, -20.1, 50.0, 22.3,



				 40.0, 22.2};
	double hexpecting3d[] = {10.0, 20.0, 60.0, 120.0};
	double hexpecting3e[] = {10.0, 20.0, 30.0, 20.4, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3f[] = {10.0, 20.0, 40.0, -22.2, 50.0, 22.3};
	double hexpecting3g[] = {10.0, 20.0, 30.0, -20.4, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3h[] = {10.0, 20.0, 50.0, 20.0, 40.0, 20.2};

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3a, 3);

	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3b, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3b.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3c, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3c.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3d, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3d.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3e, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3e: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3e[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3e[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3e.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3f, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3f: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3f[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3f[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3f.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3g, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3g: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3g[hi++] != hp.getX()) {
		    System.out.format("hi = %d, hp.getX() = %g, expected %g\n",
				      (hi-1), hp.getX(), hexpecting3g[(hi-1)]);
		    throw new Exception("hull failed");
		}
		if (hexpecting3g[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3g.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3h, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3h: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3h[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3h[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3h.length) {
	    throw new Exception("hull failed: path too short");
	}

	DoubleRandomVariable rv = new
	    UniformDoubleRV(-100.0, true, 100.0, true);
	for (int m = 10; m < 1000; m++) {
	    double[] hcoords = new double[2*m];
	    for (int k = 0; k < 2*m; k += 2) {
		hcoords[k] = rv.next();
		hcoords[k+1] = rv.next();
	    }
	    Path2D p1 = convexHullGW(hx, hy, hcoords, m);
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hcoords, m);
	    PathIterator pit1 = p1.getPathIterator(null);
	    PathIterator pit2 = p2.getPathIterator(null);
	    double[] crd1 = new double[2];
	    double[] crd2 = new double[2];
	    while (!pit1.isDone() && !pit2.isDone()) {
		crd1[0] = 0.0;
		crd1[1] = 0.0;
		crd2[0] = 0.0;
		crd2[1] = 0.0;
		if (pit1.currentSegment(crd1) != pit2.currentSegment(crd2)) {
		    throw new Exception("pit1 & pit2 differ in type");
		}
		for (int k = 0; k < 2; k++) {
		    if (Math.abs(crd1[k] - crd2[k]) > 1.e-10) {
			throw new Exception("pit1 & pit2 coords differ");
		    }
		}
		pit1.next();
		pit2.next();
	    }
	    if (pit1.isDone() != pit2.isDone()) {
		throw new Exception("pit1 and pit2 differ in "
				    + "number of segments");
	    }
	}

	int limit = 3;
	double[] hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	long t0 = System.nanoTime();
	for (int m = 0; m < 1000000; m++) {
	    Path2D p1 = convexHullGW(hx, hy, hhcoords, limit);
	}
	long t1 = System.nanoTime();

	for (int m = 0; m < 1000000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	long t2 = System.nanoTime();
	System.out.println("Test with limit set to 3");
	System.out.format("GW t1-t0 = %d, Andrew t2-t1=%d\n",
			  t1-t0, t2-t1);
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 1000000; m++) {
	    Path2D p1 = convexHullGW(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 1000000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("GW t1-t0 = %d, Andrew t2-t1=%d\n",
			  t1-t0, t2-t1);

	limit = 385;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=385): t1-t0 = %d, "
			  + "t2-t1=%d\n", t1-t0, t2-t1);

	limit = 512;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=512): t1-t0 = %d, "
			  + "t2-t1=%d\n",
			  t1-t0, t2-t1);

	limit = 385;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=385): t1-t0 = %d, "
			  + "t2-t1=%d\n", t1-t0, t2-t1);

	limit = 512;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=512): t1-t0 = %d, "
			  + "t2-t1=%d\n",
			  t1-t0, t2-t1);

	System.out.println("First and last tangent tests ...");

	Path2D tpath = Paths2D.createArc(0.0, 0.0, 1.0, -1.0,
					 Math.PI/2, Math.PI/8);

	double[] tangent = Path2DInfo.firstTangent(tpath);
	double[] normal =  Path2DInfo.firstNormal(tpath);
	double[] vec1 = new double[3];
	double[] vec2 = new double[3];
	System.arraycopy(tangent, 0, vec1, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	normal =  Path2DInfo.firstNormal(tpath);
	System.arraycopy(normal, 0, vec2, 0, 2);
	if (Math.abs(VectorOps.crossProduct(vec1, vec2)[2] - 1.0) > 1.e-14) {
	    throw new Exception();
	}
	tangent = Path2DInfo.lastTangent(tpath);
	System.arraycopy(tangent, 0, vec2, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	if (Math.abs(VectorOps.crossProduct(vec1, vec2)[2] - 1.0) > 1.e-14) {
	    throw new Exception();
	}
	normal = Path2DInfo.lastNormal(tpath);
	System.arraycopy(normal, 0, vec1, 0, 2);
	if (Math.abs(VectorOps.crossProduct(vec2, vec1)[2] - 1.0) > 1.e-14) {
	    throw new Exception();
	}

	tpath.append(Paths2D.createArc(0.0, 0.0, 2.0, -2.0,
				       Math.PI/2, Math.PI/8),
		     false);
	tangent = Path2DInfo.firstTangent(tpath);
	System.arraycopy(tangent, 0, vec1, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	tangent = Path2DInfo.lastTangent(tpath);
	System.arraycopy(tangent, 0, vec2, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	if (Math.abs(VectorOps.crossProduct(vec1, vec2)[2] - 1.0) > 1.e-14) {
	    System.out.println("cross product was "
			       + VectorOps.crossProduct(vec1, vec2)[2]);
	    throw new Exception();
	}

	tpath.closePath();
	tangent = Path2DInfo.firstTangent(tpath);
	if (Math.abs(VectorOps.norm(tangent)-1.0) > 1.e-14) {
	    throw new Exception();
	}
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	tangent = Path2DInfo.lastTangent(tpath);
	if (tangent != null) {
	    throw new Exception();
	}
	System.out.println(tangent);

	tpath = new Path2D.Double();
	tpath.moveTo(1.0, 1.0);
	tpath.lineTo(2.0, 2.0);
	tangent = Path2DInfo.firstTangent(tpath);
	System.arraycopy(tangent, 0, vec1, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	tangent = Path2DInfo.lastTangent(tpath);
	System.arraycopy(tangent, 0, vec2, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	if (Math.abs(VectorOps.crossProduct(vec1, vec2)[2]) > 1.e-14) {
	    throw new Exception();
	}

	tpath = new Path2D.Double();
	tpath.moveTo(1.0, 1.0);
	tpath.quadTo(2.0, 1.0, 2.0, 2.0);
	tangent = Path2DInfo.firstTangent(tpath);
	System.arraycopy(tangent, 0, vec1, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	tangent = Path2DInfo.lastTangent(tpath);
	System.arraycopy(tangent, 0, vec2, 0, 2);
	System.out.format("(%g, %g)\n", tangent[0], tangent[1]);
	if (Math.abs(VectorOps.crossProduct(vec1, vec2)[2] - 1.0) > 1.e-14) {
	    throw new Exception();
	}
	tpath.closePath();
	tangent = Path2DInfo.firstTangent(tpath);
	if (tangent != null) {
	    throw new Exception();
	}
	System.out.println(tangent);
	tangent = Path2DInfo.lastTangent(tpath);
	System.out.println(tangent);
	if (tangent != null) {
	    throw new Exception();
	}
	System.out.println("... OK");

	tpath = new Path2D.Double();
	tpath.moveTo(1.0, 2.0);
	tpath.lineTo(2.0, 3.0);
	tpath.quadTo(3.0, 5.0, 4.0, 10.0);
	tpath.curveTo(5.0, 11.0, 12.0, 12.0, 14.0, 12.0);
	double expected[] = {1.0, 2.0, 2.0, 3.0,
	    3.0, 5.0, 4.0, 10.0, 5.0, 11.0, 12.0, 12.0, 14.0, 12.0};
	double eknots[] = {1.0, 2.0, 2.0, 3.0, 4.0, 10.0, 14.0, 12.0};
	double knots[] = Path2DInfo.getControlPoints(tpath, false);
	if (knots.length != eknots.length) throw new Exception();
	for (int i = 0; i < eknots.length; i++) {
	    if (knots[i] != eknots[i]) throw new Exception();
	}
	double[] array1 = Path2DInfo.getControlPoints(tpath, true);
	if (expected.length != array1.length) throw new Exception();
	for (int i = 0; i < expected.length; i++) {
	    if (expected[i] != array1[i]) throw new Exception();
	}
	double[] array2 = Path2DInfo.getControlPoints(tpath, true, true);
	for (int i = 0; i < knots.length/2; i++) {
	    int j1 = 2*i;
	    int j2 = j1+1;
	    int k1 = 6*i;
	    int k2 = k1 + 1;
	    System.out.format("expecting (%g, %g) = (%g, %g)\n",
			      knots[j1], knots[j2], array2[k1], array2[k2]);
	    if (knots[j1] != array2[k1]) throw new Exception();
	    if (knots[j2] != array2[k2]) throw new Exception();
	}
	Path2D tpath2 = new Path2D.Double();
	tpath2.moveTo(array2[0], array2[1]);
	int a2len = array2.length;
	for (int i = 0; i < a2len-2; i += 6) {
	    tpath2.curveTo(array2[i+2], array2[i+3], array2[i+4], array2[i+5],
			   array2[i+6], array2[i+7]);
	}
	PathIterator pi1 = tpath.getPathIterator(null);
	PathIterator pi2 = tpath2.getPathIterator(null);

	double lastx = 0.0, lasty = 0.0;
	double[] coords2 = new double[6];

	while (!pi1.isDone()) {
	    if (pi2.isDone()) throw new Exception();
	    Arrays.fill(coords, 0.0);
	    Arrays.fill(coords2, 0.0);
	    int type1 = pi1.currentSegment(coords);
	    int type2 = pi2.currentSegment(coords2);
	    if (type1 == PathIterator.SEG_MOVETO) {
		lastx = coords[0];
		lasty = coords[1];
		if (lastx != coords2[0] || lasty != coords2[1]) {
		    throw new Exception();
		}
	    } else {
		for (int k = 0; k <= 9; k++) {
		    double u = k / 10.0;
		    double x1 = Path2DInfo.getX(u, lastx, lasty, type1, coords);
		    double x2 = Path2DInfo.getX(u, lastx, lasty,
						type2, coords2);
		    double y1 = Path2DInfo.getY(u, lastx, lasty, type1, coords);
		    double y2 = Path2DInfo.getY(u, lastx, lasty,
						type2, coords2);
		    if (Math.abs(x1 - x2) > 1.e-10) {
			System.out.format("type1 = %d, type2 = %d, u = %g\n",
					  type1, type2, u);
			System.out.format("x1 = %s, x2 = %s\n", x1, x2);
			System.out.format("    %g\t%g\n", lastx, lastx);
			System.out.println("    coords\tcoords2");
			for (int kk = 0; kk < 6; kk++) {
			    System.out.format("    %g\t%g\n",
					      coords[kk], coords2[kk]);
			}
			throw new Exception();
		    }
		    if (Math.abs(y1 - y2) > 1.e-10) {
			System.out.format("type1 = %d, type2 = %d)\n",
					  type1, type2);
			System.out.format("y1 = %s, y2 = %s\n", y1, y2);
			throw new Exception();
		    }
		}
		switch(type1) {
		case PathIterator.SEG_LINETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    if (lastx != coords2[4] || lasty != coords2[5]) {
			throw new Exception();
		    }
		    break;
		case PathIterator.SEG_QUADTO:
		    lastx = coords[2];
		    lasty = coords[3];
		    if (lastx != coords2[4] || lasty != coords2[5]) {
			throw new Exception();
		    }
		    break;
		case PathIterator.SEG_CUBICTO:
		    lastx = coords[4];
		    lasty = coords[5];
		    if (lastx != coords2[4] || lasty != coords2[5]) {
			throw new Exception();
		    }
		    break;
		}
	    }
	    pi1.next();
	    pi2.next();
	}
	if (!pi2.isDone()) throw new Exception();


	System.exit(0);
    }
}
