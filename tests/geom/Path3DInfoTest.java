import org.bzdev.geom.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Path3DInfoTest {

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

    /**
     * Compute the length of a quadratic path segment from its start to
     * a position specified by the path parameter
     * @param u the path parameter in the range [0.0, 1.0]
     * @param x0 the X coordinate of the first control point
     * @param y0 the Y coordinate of the first control point
     * @param z0 the Z coordinate of the first control point
     * @param coords the remaining control points in order, with each
     *        control point represented by 3 consecutive elements
     *        contining a control point's X coordinate, Y coordinate,
     *        and Z coordinate respectively (only the first 6 indices
     *         will be used)
     * @return the path length from u = 0 to the given value of u.
     */
    public static double quadLength(double u,
			     double x0, double y0, double z0,
			     double[] coords)
	throws Exception
    {
	BezierPolynomial px = new BezierPolynomial(x0, coords[0], coords[3]);

	BezierPolynomial py = new BezierPolynomial(y0, coords[1], coords[4]);

	BezierPolynomial pz = new BezierPolynomial(z0, coords[2], coords[5]);

	BezierPolynomial pxd = px.deriv();
	BezierPolynomial pyd = py.deriv();
	BezierPolynomial pzd = pz.deriv();


	double[] array = Polynomials
	    .fromBezier(Polynomials.multiply(pxd, pxd)
			.add(Polynomials.multiply(pyd, pyd))
			.add(Polynomials.multiply(pzd, pzd))
			.getCoefficientsArray(),
			0, 2);

	double a = array[0];
	double b = array[1];
	double c = array[2];
	try {
	    return Polynomials.integrateRootP2(u, a, b, c);
	} catch (ArithmeticException ea) {
	    return Path2DInfoTest.getSegmentLength(u, px, py, pz);
	}
    }

    /**
     * Compute the length of a cubic path segment from its start to
     * a position specified by the path parameter
     * @param u the path parameter in the range [0.0, 1.0]
     * @param x0 the X coordinate of the first control point
     * @param y0 the Y coordinate of the first control point
     * @param z0 the Z coordinate of the first control point
     * @param coords the remaining control points in order, with each
     *        control point represented by 3 consecutive elements
     *        contining a control point's X coordinate, Y coordinate,
     *        and Z coordinate respectively (only the first 9 indices
     *        will be used)
     * @return the path length from u = 0 to the given value of u.
     */
    public static double cubicLength(double u, double x0, double y0, double z0,
				     double[] coords)
    {
	return cubicLength(0, false, u, x0, y0, z0, coords);
    }

    static double cubicLength(int depth, boolean split,
			      double u, double x0, double y0, double z0,
			      double[] coords)
    {
	if (split) {
	    depth++;
	    double[] scoords = new double[18];
	    PathSplitter.split(PathIterator.SEG_CUBICTO,
			       x0, y0, z0, coords, 0, scoords, 0, 0.5);
	    if (u <= 0.5) {
		u *= 2;
		return cubicLength(depth, false, u, x0, y0, z0, scoords);
	    } else {
		double len1 = cubicLength(depth, false, 1.0, x0, y0, z0,
					  scoords);
		x0 = scoords[6];
		y0 = scoords[7];
		z0 = scoords[8];
		System.arraycopy(scoords, 9, scoords, 0, 9);
		u -= 0.5;
		u *= 2;
		return len1 + cubicLength(depth, false, u, x0, y0, z0,
					  scoords);
	    }
	}

	BezierPolynomial px = new BezierPolynomial(x0, coords[0], coords[3],
						   coords[6]);

	BezierPolynomial py = new BezierPolynomial(y0, coords[1], coords[4],
						   coords[7]);

	BezierPolynomial pz = new BezierPolynomial(z0, coords[2], coords[5],
						   coords[8]);

	BezierPolynomial pxd = px.deriv();
	BezierPolynomial pyd = py.deriv();
	BezierPolynomial pzd = pz.deriv();

	double[] marray = Polynomials.fromBezier(null,
						  pxd.getCoefficientsArray(),
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
					pyd.getCoefficientsArray(),
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
	Polynomial Py = new Polynomial(marray);

	marray = Polynomials.fromBezier(null,
					pzd.getCoefficientsArray(),
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
	Polynomial Pz = new Polynomial(marray);

	Polynomial parray[] = {Px, Py, Pz};
	Arrays.sort(parray, 0, 3,
		    (p1, p2) -> {
			int d1 = p1.getDegree();
			int d2 = p2.getDegree();
			return d1 - d2;
		    });
	Px = parray[0];
	Py = parray[1];
	Pz = parray[2];
	try {
	    double result = cubicLength(u, Px, Py, Pz);
	    return result;
	} catch (ArithmeticException e) {
	    if ((e instanceof Polynomials.RootP4Exception)
		&& (depth < 1)) {
		return cubicLength(depth, true, u, x0, y0, z0, coords);
	    } else {
		// cannot split due to depth limit. Fall back to
		// numerical integration.
		// double result =  getCubicLength(u, x0, y0, coords);
		double result = Path2DInfoTest
		    .getSegmentLength(u, px, py, pz);
		return result;
	    }
	}
    }

    static boolean[] cases = new boolean[13];

    // Note: this may modify Px, Py or Pz: it is a separate method merely
    // for testing.
    static double cubicLength (double u, Polynomial Px, Polynomial Py,
			       Polynomial Pz)
	throws ArithmeticException
    {
	// special cases.
	int degPx = Px.getDegree();
	int degPy = Py.getDegree();
	int degPz = Pz.getDegree();
	// degPx <= degPy <= degPz due to the sorting before this
	// was called.

	/*
	System.out.println("degPx = " + degPx +", degPy = " + degPy
			   + ", degPz = " + degPz);
	printArray("Px", Px);
	printArray("Py", Py);
	printArray("Pz", Pz);
	*/

	double[] marray = null;
	if (degPx == 1) {
	    if (degPz == 1) {
		// degPy must also be 1
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Pz.multiplyBy(Pz);
		Px.incrBy(Py);
		Px.incrBy(Pz);
		marray = Px.getCoefficientsArray();
		cases[0] = true;
		return Polynomials
		    .integrateRootP2(u, marray[0], marray[1], marray[2])
		    - Polynomials.integrateRootP2(0.0,
						  marray[0],
						  marray[1],
						  marray[2]);
	    }
	    // If we got here degPz is 2 because the polynomials were
	    // sorted so that deg(Px) <= deg(Py) <= deg(Pz)
	} else if (degPx == 0) {
	    if (Px.getCoefficientsArray()[0] == 0.0) {
		// integrate sqrt(Py^2 + Pz^2) as there are no X components
		// This reduces to the 2D case, so  just use that.
		cases[1] = true;
		return Path2DInfoTest.cubicLength(u, Py, Pz);
	    } else {
		// no root case: Px^2 is a constant > 0 so
		// Px^2 + Py^2 + Pz^2 is always positive => there
		// are no roots.
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Pz.multiplyBy(Pz);
		Px.incrBy(Py);
		Px.incrBy(Pz);
		// The sum of squares implies the degree must be even
		switch (Px.getDegree()) {
		case 0:
		    double factor = Math.sqrt(Px.getCoefficientsArray()[0]);
		    cases[2] = true;
		    return factor*u;
		case 2:
		    double[] array = Px.getCoefficientsArray();
		    cases[3] = true;
		    return Polynomials
			    .integrateRootP2(u, array[0], array[1], array[2])
			    - Polynomials
			    .integrateRootP2(0, array[0], array[1], array[2]);
		case 4:
		    cases[4] = true;
		    return Polynomials.integrateRootP4(Px, 0.0, u);
		}
	    }
	}
	// At this point, deg(Pz) == 2 and deg(Px) must be at least 1,
	// so deg(Py) > 0.
	double[] arrayX = Px.getCoefficientsArray();
	double[] arrayY = Py.getCoefficientsArray();
	double[] arrayZ = Pz.getCoefficientsArray();
	double scaleX = arrayX[degPx];
	double scaleY = arrayY[degPy];
	double scaleZ = arrayZ[degPz];
	Px.multiplyBy(1.0/scaleX);
	Py.multiplyBy(1.0/scaleY);
	Pz.multiplyBy(1.0/scaleZ);


	// in case of roundoff errors
	arrayX[degPx] = 1.0;
	arrayY[degPy] = 1.0;
	arrayZ[degPz] = 1.0;

	HashMap<Double,Integer> rmapX = new HashMap<>();
	HashMap<Double,Integer> rmapY = new HashMap<>();
	HashMap<Double,Integer> rmapZ = new HashMap<>();

	double c = arrayX[0];
	double b = arrayX[1];
	double b2 = b*b;
	double c4 = 4*c;
	double descrX = b2 - c4;
	if (Math.abs(descrX)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrX = 0;
	}
	if (degPx == 1) {
	    rmapX.put(-c, 1);
	} else if (degPx == 2) {
	    if (descrX == 0) {
		rmapX.put(-b/2.0, 2);
	    } else if (descrX > 0) {
		double rdescrX = Math.sqrt(descrX);
		rmapX.put((-b - rdescrX)/2.0, 1);
		rmapX.put((-b + rdescrX)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapX.entrySet()) {
	    System.out.println("x root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/

	c = arrayY[0];
	b = arrayY[1];
	b2 = b*b;
	c4 = 4*c;
	double descrY = b2 - c4;
	if (Math.abs(descrY)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrY = 0;
	}
	if (degPy == 1) {
	    rmapY.put(-c, 1);
	} else if (degPy == 2) {
	    if (descrY == 0) {
		rmapY.put(-b/2.0, 2);
	    } else if (descrY > 0) {
		double rdescrY = Math.sqrt(descrY);
		rmapY.put((-b - rdescrY)/2.0, 1);
		rmapY.put((-b + rdescrY)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapY.entrySet()) {
	    System.out.println("y root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/
	c = arrayZ[0];
	b = arrayZ[1];
	b2 = b*b;
	c4 = 4*c;
	double descrZ = b2 - c4;
	double rdescrZ = Double.NaN;
	if (Math.abs(descrZ)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrZ = 0;
	}
	if (degPz == 1) {
	    rmapZ.put(-c, 1);
	} else if (degPz == 2) {
	    if (descrZ == 0) {
		rmapZ.put(-b/2.0, 2);
	    } else if (descrZ > 0) {
		rdescrZ = Math.sqrt(descrZ);
		rmapZ.put((-b - rdescrZ)/2.0, 1);
		rmapZ.put((-b + rdescrZ)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapZ.entrySet()) {
	    System.out.println("z root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/

	Map<Double,Integer> rmap = new HashMap<>();
	for (Map.Entry<Double,Integer> entry: rmapX.entrySet()) {
	    double root = entry.getKey();
	    int max = entry.getValue();
	    if (max > 0) {
		if (rmapY.containsKey(root)) {
		    int omax = rmapY.get(root);
		    if (omax < max) max = omax;
		} else {
		    max = 0;
		}
		if (max > 0) {
		    if (rmapZ.containsKey(root)) {
			int omax = rmapZ.get(root);
			if (omax < max) max = omax;
		    } else {
			max = 0;
		    }
		}
	    }
	    if (max > 0) {
		rmap.put(root, max);
	    }
	}

	switch (rmap.size()) {
	case 0:
	    // no common roots.
	    Px.multiplyBy(scaleX);
	    Px.multiplyBy(Px);
	    Py.multiplyBy(scaleY);
	    Py.multiplyBy(Py);
	    Pz.multiplyBy(scaleZ);
	    Pz.multiplyBy(Pz);
	    Px.incrBy(Py);
	    Px.incrBy(Pz);
	    cases[5] = true;
	    return Polynomials.integrateRootP4(Px, 0.0, u);
	case 1:
	    // one common root (single or double);
	    for (Map.Entry<Double,Integer> entry: rmap.entrySet()) {
		double root = entry.getKey();
		double cnt = entry.getValue();
		if (cnt == 2) {
		    // double root, so the square root is constant
		    // and with two roots, the absolute value is
		    // just the polynomial sqaured.
		    double scale = Math.sqrt(scaleX*scaleX
					     + scaleY*scaleY
					     + scaleZ*scaleZ);
		    Polynomial p = new Polynomial(-root, 1.0);
		    p.multiplyBy(p);
		    Polynomial integral = p.integral();
		    cases[6] = true;
		    return scale * integral.valueAt(u);
		} else {
		    // single root.
		    Polynomial p = new Polynomial(-root, 1.0);
		    Polynomial rpx = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryX: rmapX.entrySet()) {
			double r = entryX.getKey();
			double cntx = entryX.getValue();
			if (r == root) cntx--;
			while (cntx > 0) {
			    rpx.multiplyBy(new Polynomial(-r, 1.0));
			    cntx--;
			}
		    }
		    Polynomial rpy = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryY: rmapY.entrySet()) {
			double r = entryY.getKey();
			double cnty = entryY.getValue();
			if (r == root) cnty--;
			while (cnty > 0) {
			    rpy.multiplyBy(new Polynomial(-r, 1.0));
			    cnty--;
			}
		    }
		    Polynomial rpz = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryZ: rmapZ.entrySet()) {
			double r = entryZ.getKey();
			double cntz = entryZ.getValue();
			if (r == root) cntz--;
			while (cntz > 0) {
			    rpz.multiplyBy(new Polynomial(-r, 1.0));
			    cntz --;
			}
		    }
		    // at this point, rpx, rpy, and rpz are either 0 or
		    // first degree polynomials, with at least 1 a first
		    // degree polynomial.
		    rpx.multiplyBy(scaleX);
		    rpx.multiplyBy(rpx);
		    rpy.multiplyBy(scaleY);
		    rpy.multiplyBy(rpy);
		    rpz.multiplyBy(scaleZ);
		    rpz.multiplyBy(rpz);
		    rpx.incrBy(rpy);
		    rpx.incrBy(rpz);
		    cases[7] = true;
		    return Polynomials.integrateAbsPRootQ(u, p, rpx);
		}
	    }
	case 2:
	    // two separate roots. The polynomial whose square root
	    // is taken is a 0 degree polynomial (i.e., a scalar),
	    // but we have to integrate the absolute value
	    // |(u - r1)(u - r2)|.  The result is a sum of absolute
	    // values with no roots between the limits of integration
	    // for each integral in the sum.
	    {
		double r1 = (-b - rdescrZ)/2;
		double r2 = (-b + rdescrZ)/2;
		double scale = Math.sqrt(scaleX*scaleX
					 + scaleY*scaleY
					 + scaleZ*scaleZ);
		Polynomial p = new Polynomial(1.0);
		p.multiplyBy(new Polynomial(-r1, 1.0));
		p.multiplyBy(new Polynomial(-r2, 1.0));
		Polynomial integral = p.integral();
		if (r1 > 0.0 && u <= r1) {
		    // cases2[6] = true;
		    cases[8] = true;
		    return scale * Math.abs(integral.valueAt(u));
		} else if (r1 > 0.0 && u <= r2) {
		    double sum = Math.abs(integral.valueAt(r1));
		    sum += Math.abs(integral.valueAt(u)
				    - integral.valueAt(r1));
		    cases[9] = true;
		    return scale * sum;
		} else if (r1 > 0.0 && u > r2) {
		    double sum = Math.abs(integral.valueAt(r1));
		    sum += Math.abs (integral.valueAt(r2)
				     - integral.valueAt(r1));
		    sum += Math.abs(integral.valueAt(u)
				    - integral.valueAt(r2));
		    // cases2[8] = true;
		    cases[10] = true;
		    return scale * sum;
		} else if (r1 <= 0.0 && r2 > 0 && u > r2) {
		    double sum = Math.abs(integral.valueAt(r2));
		    sum += Math.abs(integral.valueAt(u)
				    - integral.valueAt(r2));
		    // cases2[9] = true;
		    cases[11] = true;
		    return scale * sum;
		} else {
		    cases[12] = true;
		    return scale*Math.abs(integral.valueAt(u));
		}
	    }
	default:
	    throw new UnexpectedExceptionError();
	}
    }

    static double getCubicLength(double t,
				 double x0, double y0, double z0,
				 double[] coords)
    {
	var px = new BezierPolynomial (x0, coords[0], coords[3], coords[6]);
	var py = new BezierPolynomial (y0, coords[1], coords[4], coords[7]);
	var pz = new BezierPolynomial (z0, coords[2], coords[5], coords[8]);

	return Path2DInfoTest.getSegmentLength(t, px, py, pz);
    }

    static double x0 = 20.0;
    static double y0 = 21.0;
    static double z0 = 22.0;

    static class CData {
	Polynomial p1;
	Polynomial p2;
	Polynomial p3;
	public CData(Polynomial p1, Polynomial p2, Polynomial p3) {
	    this.p1 = p1;
	    this.p2 = p2;
	    this.p3 = p3;
	}
    }

    // copied frolm Path2DInfo for testing
    // Note: this may modify Px or Py: it is a separate method merely
    // for testing.
    static RealValuedFunctOps cubicLengthFunction (Polynomial Px, Polynomial Py)
	throws ArithmeticException
    {
	// special cases.
	int degPx = Px.getDegree();
	int degPy = Py.getDegree();

	// double[] marray = null;
	if (degPx == 1) {
	    if (degPy == 1) {
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		double[] marray = Px.getCoefficientsArray();
		final double a = marray[0];
		final double b = marray[1];
		final double c = marray[2];
		// cases2[0] = true;
		return new RealValuedFunctOps() {
		    public double valueAt(double u) {
			return Polynomials.integrateRootP2(u, a, b, c);
		    }
		};
	    }
	    // If we got here degPy is 2
	} else if (degPx == 0) {
	    if (Px.getCoefficientsArray()[0] == 0.0) {
		// integrate |Py|
		final Polynomial integral = Py.integral();
		// The integral will have a value of 0 at u = 0.0;
		final double[] marray = Py.getCoefficientsArray();
		final int PyDeg = Py.getDegree();
		return new RealValuedFunctOps() {
		    public double valueAt(double u) {
			switch (PyDeg) {
			case 0:
			    return Math.abs(integral.valueAt(u));
			case 1:
			    // marray = Py.getCoefficientsArray();
			    double r = -marray[0] / marray[1];
			    if (r > 0.0 && r < u) {
				double val = integral.valueAt(r);
				return Math.abs(val)
				    + Math.abs(integral.valueAt(u) - val);
			    } else {
				return Math.abs(integral.valueAt(u));
			    }
			case 2:
			    // marray = Py.getCoefficientsArray();
			    double descr = marray[1]*marray[1]
				- 4*marray[0]*marray[2];
			    if (descr <= 0.0) {
				return Math.abs(integral.valueAt(u));
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
				    sum += Math.abs(integral.valueAt(u)
						    - integral2);
				} else {
				    sum += Math.abs(integral.valueAt(u)
						    - integral1);
				}
				return sum;
			    } else if (r2 > 0.0 && r2 < u) {
				double integral1 = integral.valueAt(r2);
				double sum = Math.abs(integral.valueAt(r2));
				sum += Math.abs(integral.valueAt(u)
						- integral1);
				return sum;
			    } else {
				return Math.abs(integral.valueAt(u));
			    }
			default:
			    throw new UnexpectedExceptionError();
			}
		    }
		};
	    } else {
		// no root case: Px^2 is a constant > 0 so
		// Px^2 + Py^2 is always positive
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Px.incrBy(Py);
		final Polynomial PX = Px;
		final double[] marray = Px.getCoefficientsArray();
		// cases2[1] = true;
		switch (Px.getDegree()) {
		case 0:
		    final double factor = Math.sqrt(marray[0]);
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return factor*u;
			}
		    };
		case 2:
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return Polynomials
				.integrateRootP2(u,
						 marray[0],
						 marray[1],
						 marray[2]);
			}
		    };
		case 4:
		    return Polynomials.integralOfRootP4(PX);
		}
	    }
	}
	// At this point, degPy == 2 as the other values of degPy were already
	// handled. In addition degPx is at least 1.
	double[] arrayX = Px.getCoefficientsArray();
	double[] arrayY = Py.getCoefficientsArray();
	final double scaleX = arrayX[degPx];
	final double scaleY = arrayY[degPy];
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
		// return Polynomials.integrateRootP4(Px, 0.0, u);
		return Polynomials.integralOfRootP4(Px);
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
	    // cases2[2] = true;
	    // return integrateProotQ(U, p, rpx);
	    final Polynomial P = p;
	    final Polynomial Q = rpx;
	    return new RealValuedFunctOps() {
		public double valueAt(double u) {
		    return Polynomials.integrateAbsPRootQ(u, P, Q);
		}
	    };

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
		// cases2[3] = true;
		final Polynomial PX = Px;
		return Polynomials.integralOfRootP4(Px);
	    } else if (n == 1) {
		// cases2[4] = true;
		rpx.multiplyBy(scaleX);
		rpx.multiplyBy(rpx);
		rpy.multiplyBy(scaleY);
		rpy.multiplyBy(rpy);
		rpx.incrBy(rpy);
		// return  integrateProotQ(u, p, rpx);
		final Polynomial P = p;
		final Polynomial Q = rpx;
		return new RealValuedFunctOps() {
		    public double valueAt(double u) {
			return Polynomials.integrateAbsPRootQ(u, P, Q);
		    }
		};
	    } else if (n == 2) {
		// rp must have a degree of 0, with its only coefficient
		// having a value of 1.
		double[] array = p.getCoefficientsArray();
		c = array[0];
		b = array[1];
		double descr = b*b - 4*c;
		if (Math.abs(descr) < 1.e-12) descr = 0;
		final Polynomial integral = p.integral();
		if (descr <= 0) {
		    // all values of p have the same sign with one
		    // zero (when descr == 0)
		    // cases2[5] = true;
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return Math.sqrt(scaleX*scaleX + scaleY*scaleY)
				* Math.abs(integral.valueAt(u));
			}
		    };
		} else {
		    double rdescr = Math.sqrt(descr);
		    final double r1 = (-b - rdescr)/2;
		    final double r2 = (-b + rdescr)/2;
		    final double scale = Math.sqrt(scaleX*scaleX
						   + scaleY*scaleY);
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    if (r1 > 0.0 && u <= r1) {
				return scale * Math.abs(integral.valueAt(u));
			    } else if (r1 > 0.0 && u <= r2) {
				double sum = Math.abs(integral.valueAt(r1));
				sum += Math.abs(integral.valueAt(u)
						- integral.valueAt(r1));
				return scale * sum;
			    } else if (r1 > 0.0 && u > r2) {
				double sum = Math.abs(integral.valueAt(r1));
				sum += Math.abs (integral.valueAt(r2)
						 - integral.valueAt(r1));
				sum += Math.abs(integral.valueAt(u)
						- integral.valueAt(r2));
				return scale * sum;
			    } else if (r1 <= 0.0 && r2 > 0 && u > r2) {
				double sum = Math.abs(integral.valueAt(r2));
				sum += Math.abs(integral.valueAt(u)
						- integral.valueAt(r2));
				return scale * sum;
			    } else {
				return scale * Math.abs(integral.valueAt(u));
			    }
			}
		    };
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
	    return Polynomials.integralOfRootP4(Px);
	}
    }


    // copied from Path3DInfo for testing.
    static RealValuedFunctOps cubicLengthFunction(Polynomial Px,
						  Polynomial Py,
						  Polynomial Pz)
	throws ArithmeticException
    {
	// special cases.
	int degPx = Px.getDegree();
	int degPy = Py.getDegree();
	int degPz = Pz.getDegree();
	// degPx <= degPy <= degPz due to the sorting before this
	// was called.

	/*
	System.out.println("degPx = " + degPx +", degPy = " + degPy
			   + ", degPz = " + degPz);
	printArray("Px", Px);
	printArray("Py", Py);
	printArray("Pz", Pz);
	*/

	double[] marray = null;
	if (degPx == 1) {
	    if (degPz == 1) {
		// degPy must also be 1
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Pz.multiplyBy(Pz);
		Px.incrBy(Py);
		Px.incrBy(Pz);
		marray = Px.getCoefficientsArray();
		final double a = marray[0];
		final double b = marray[1];
		final double c = marray[2];
		// cases[0] = true;
		return new RealValuedFunctOps() {
		    public double valueAt(double u) {
			return Polynomials.integrateRootP2(u, a, b, c);
		    }
		};
	    }
	    // If we got here degPz is 2 because the polynomials were
	    // sorted so that deg(Px) <= deg(Py) <= deg(Pz)
	} else if (degPx == 0) {
	    if (Px.getCoefficientsArray()[0] == 0.0) {
		// integrate sqrt(Py^2 + Pz^2) as there are no X components
		// This reduces to the 2D case, so  just use that.
		// cases[1] = true;
		return cubicLengthFunction(Py, Pz);
	    } else {
		// no root case: Px^2 is a constant > 0 so
		// Px^2 + Py^2 + Pz^2 is always positive => there
		// are no roots.
		Px.multiplyBy(Px);
		Py.multiplyBy(Py);
		Pz.multiplyBy(Pz);
		Px.incrBy(Py);
		Px.incrBy(Pz);
		final Polynomial PX = Px;
		final double[] array = Px.getCoefficientsArray();
		// The sum of squares implies the degree must be even
		switch (Px.getDegree()) {
		case 0:
		    final double factor = Math.sqrt(array[0]);
		    // cases[2] = true;
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return factor*u;
			}
		    };
		case 2:
		    // double[] array = Px.getCoefficientsArray();
		    // cases[3] = true;
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return Polynomials
				.integrateRootP2(u,
						 array[0], array[1], array[2]);
			}
		    };
		case 4:
		    //cases[4] = true;
		    return Polynomials.integralOfRootP4(PX);
		}
	    }
	}
	// At this point, deg(Pz) == 2 and deg(Px) must be at least 1,
	// so deg(Py) > 0.
	double[] arrayX = Px.getCoefficientsArray();
	double[] arrayY = Py.getCoefficientsArray();
	double[] arrayZ = Pz.getCoefficientsArray();
	double scaleX = arrayX[degPx];
	double scaleY = arrayY[degPy];
	double scaleZ = arrayZ[degPz];
	Px.multiplyBy(1.0/scaleX);
	Py.multiplyBy(1.0/scaleY);
	Pz.multiplyBy(1.0/scaleZ);


	// in case of roundoff errors
	arrayX[degPx] = 1.0;
	arrayY[degPy] = 1.0;
	arrayZ[degPz] = 1.0;

	HashMap<Double,Integer> rmapX = new HashMap<>();
	HashMap<Double,Integer> rmapY = new HashMap<>();
	HashMap<Double,Integer> rmapZ = new HashMap<>();

	double c = arrayX[0];
	double b = arrayX[1];
	double b2 = b*b;
	double c4 = 4*c;
	double descrX = b2 - c4;
	if (Math.abs(descrX)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrX = 0;
	}
	if (degPx == 1) {
	    rmapX.put(-c, 1);
	} else if (degPx == 2) {
	    if (descrX == 0) {
		rmapX.put(-b/2.0, 2);
	    } else if (descrX > 0) {
		double rdescrX = Math.sqrt(descrX);
		rmapX.put((-b - rdescrX)/2.0, 1);
		rmapX.put((-b + rdescrX)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapX.entrySet()) {
	    System.out.println("x root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/

	c = arrayY[0];
	b = arrayY[1];
	b2 = b*b;
	c4 = 4*c;
	double descrY = b2 - c4;
	if (Math.abs(descrY)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrY = 0;
	}
	if (degPy == 1) {
	    rmapY.put(-c, 1);
	} else if (degPy == 2) {
	    if (descrY == 0) {
		rmapY.put(-b/2.0, 2);
	    } else if (descrY > 0) {
		double rdescrY = Math.sqrt(descrY);
		rmapY.put((-b - rdescrY)/2.0, 1);
		rmapY.put((-b + rdescrY)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapY.entrySet()) {
	    System.out.println("y root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/
	c = arrayZ[0];
	b = arrayZ[1];
	b2 = b*b;
	c4 = 4*c;
	double descrZ = b2 - c4;
	double rdescrZ = Double.NaN;
	if (Math.abs(descrZ)/Math.max(b2, Math.abs(c4)) < 1.e-12) {
	    descrZ = 0;
	}
	if (degPz == 1) {
	    rmapZ.put(-c, 1);
	} else if (degPz == 2) {
	    if (descrZ == 0) {
		rmapZ.put(-b/2.0, 2);
	    } else if (descrZ > 0) {
		rdescrZ = Math.sqrt(descrZ);
		rmapZ.put((-b - rdescrZ)/2.0, 1);
		rmapZ.put((-b + rdescrZ)/2.0, 1);
	    }
	}
	/*
	for (Map.Entry<Double,Integer> ent: rmapZ.entrySet()) {
	    System.out.println("z root " + ent.getKey()
			       + ", multiplicity = " + ent.getValue());
	}
	*/

	Map<Double,Integer> rmap = new HashMap<>();
	for (Map.Entry<Double,Integer> entry: rmapX.entrySet()) {
	    double root = entry.getKey();
	    int max = entry.getValue();
	    if (max > 0) {
		if (rmapY.containsKey(root)) {
		    int omax = rmapY.get(root);
		    if (omax < max) max = omax;
		} else {
		    max = 0;
		}
		if (max > 0) {
		    if (rmapZ.containsKey(root)) {
			int omax = rmapZ.get(root);
			if (omax < max) max = omax;
		    } else {
			max = 0;
		    }
		}
	    }
	    if (max > 0) {
		rmap.put(root, max);
	    }
	}

	switch (rmap.size()) {
	case 0:
	    // no common roots.
	    Px.multiplyBy(scaleX);
	    Px.multiplyBy(Px);
	    Py.multiplyBy(scaleY);
	    Py.multiplyBy(Py);
	    Pz.multiplyBy(scaleZ);
	    Pz.multiplyBy(Pz);
	    Px.incrBy(Py);
	    Px.incrBy(Pz);
	    // cases[5] = true;
	    return Polynomials.integralOfRootP4(Px);
	case 1:
	    // one common root (single or double);
	    for (Map.Entry<Double,Integer> entry: rmap.entrySet()) {
		double root = entry.getKey();
		double cnt = entry.getValue();
		if (cnt == 2) {
		    // double root, so the square root is constant
		    // and with two roots, the absolute value is
		    // just the polynomial sqaured.
		    final double scale = Math.sqrt(scaleX*scaleX
					     + scaleY*scaleY
					     + scaleZ*scaleZ);
		    Polynomial p = new Polynomial(-root, 1.0);
		    p.multiplyBy(p);
		    final Polynomial integral = p.integral();
		    // cases[6] = true;
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return scale * integral.valueAt(u);
			}
		    };
		} else {
		    // single root.
		    Polynomial p = new Polynomial(-root, 1.0);
		    Polynomial rpx = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryX: rmapX.entrySet()) {
			double r = entryX.getKey();
			double cntx = entryX.getValue();
			if (r == root) cntx--;
			while (cntx > 0) {
			    rpx.multiplyBy(new Polynomial(-r, 1.0));
			    cntx--;
			}
		    }
		    Polynomial rpy = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryY: rmapY.entrySet()) {
			double r = entryY.getKey();
			double cnty = entryY.getValue();
			if (r == root) cnty--;
			while (cnty > 0) {
			    rpy.multiplyBy(new Polynomial(-r, 1.0));
			    cnty--;
			}
		    }
		    Polynomial rpz = new Polynomial(1.0);
		    for (Map.Entry<Double,Integer> entryZ: rmapZ.entrySet()) {
			double r = entryZ.getKey();
			double cntz = entryZ.getValue();
			if (r == root) cntz--;
			while (cntz > 0) {
			    rpz.multiplyBy(new Polynomial(-r, 1.0));
			    cntz --;
			}
		    }
		    // at this point, rpx, rpy, and rpz are either 0 or
		    // first degree polynomials, with at least 1 a first
		    // degree polynomial.
		    rpx.multiplyBy(scaleX);
		    rpx.multiplyBy(rpx);
		    rpy.multiplyBy(scaleY);
		    rpy.multiplyBy(rpy);
		    rpz.multiplyBy(scaleZ);
		    rpz.multiplyBy(rpz);
		    rpx.incrBy(rpy);
		    rpx.incrBy(rpz);
		    // cases[7] = true;
		    // return Polynomials.integrateAbsPRootQ(u, p, rpx);
		    final Polynomial P = p;
		    final Polynomial Q = rpx;
		    return new RealValuedFunctOps() {
			public double valueAt(double u) {
			    return Polynomials.integrateAbsPRootQ(u, P, Q);
			}
		    };
		}
	    }
	case 2:
	    // two separate roots. The polynomial whose square root
	    // is taken is a 0 degree polynomial (i.e., a scalar),
	    // but we have to integrate the absolute value
	    // |(u - r1)(u - r2)|.  The result is a sum of absolute
	    // values with no roots between the limits of integration
	    // for each integral in the sum.
	    {
		final double r1 = (-b - rdescrZ)/2;
		final double r2 = (-b + rdescrZ)/2;
		final double scale = Math.sqrt(scaleX*scaleX
					       + scaleY*scaleY
					       + scaleZ*scaleZ);
		Polynomial p = new Polynomial(1.0);
		p.multiplyBy(new Polynomial(-r1, 1.0));
		p.multiplyBy(new Polynomial(-r2, 1.0));
		final Polynomial integral = p.integral();
		return new RealValuedFunctOps() {
		    public double valueAt(double u) {
			if (r1 > 0.0 && u <= r1) {
			    // cases[8] = true;
			    return scale * Math.abs(integral.valueAt(u));
			} else if (r1 > 0.0 && u <= r2) {
			    double sum = Math.abs(integral.valueAt(r1));
			    sum += Math.abs(integral.valueAt(u)
					    - integral.valueAt(r1));
			    // cases[9] = true;
			    return scale * sum;
			} else if (r1 > 0.0 && u > r2) {
			    double sum = Math.abs(integral.valueAt(r1));
			    sum += Math.abs (integral.valueAt(r2)
					     - integral.valueAt(r1));
			    sum += Math.abs(integral.valueAt(u)
					    - integral.valueAt(r2));
			    // cases[10] = true;
			    return scale * sum;
			} else if (r1 <= 0.0 && r2 > 0 && u > r2) {
			    double sum = Math.abs(integral.valueAt(r2));
			    sum += Math.abs(integral.valueAt(u)
					    - integral.valueAt(r2));
			    // cases[11] = true;
			    return scale * sum;
			} else {
			    // cases[12] = true;
			    return scale*Math.abs(integral.valueAt(u));
			}
		    }
		};
	    }
	default:
	    throw new UnexpectedExceptionError();
	}
    }

    static void testCubicLength() throws Exception {
	double len, elen, flen;

	CData array[] = {
	    new CData(new Polynomial(10.0, 2.0), // case 0
		      new Polynomial(20.0, 3.0),
		      new Polynomial(30.0, 4.0)),
	    new CData(new Polynomial(0.0), // case 1
		      new Polynomial(10.0, 20.0, 30.0),
		      new Polynomial(11.0, 21.0, 31.0)),
	    new CData(new Polynomial(10.0), // case 2
		      new Polynomial(20.0),
		      new Polynomial(30.0)),
	    new CData(new Polynomial(10.0), // case 3
		      new Polynomial(1.0, 3.0),
		      new Polynomial(2.0, 1.0)),
	    new CData(new Polynomial(10.0), // case 4
		      new Polynomial(1.0, 2.0, 3.0),
		      new Polynomial(2.0, 3.0, 4.0)),
	    new CData(new Polynomial(-1.0, -10.0, 1.0), //case5
		      new Polynomial(-1.0 , -20.0, 1.0),
		      new Polynomial(-1.0, -30.0, 1.0)),
	    new CData(new Polynomial(1.0, -2.0, 1.0), // case 6
		      new Polynomial(1.0, -2.0, 1.0),
		      new Polynomial(1.0, -2.0, 1.0)),
	    new CData(new Polynomial(2.0, -3.0, 1.0),  // case 7
		      new Polynomial(3.0, -4.0, 1.0),
		      new Polynomial(4.0, -5.0, 1.0)),
	    new CData(new Polynomial(6.0, -5.0, 1.0), // case 8
		      new Polynomial(12.0, -10.0, 2.0),
		      new Polynomial(18.0, -15.0, 3.0)),
	    new CData(new Polynomial(2.0, -5.0, 2.0), // case 9
		      new Polynomial(4.0, -10.0, 4.0),
		      new Polynomial(6.0, -15.0, 6.0)),
	    new CData(new Polynomial(1.0, -5.0, 6.0), // case 10
		      new Polynomial(2.0, -10.0, 12.0),
		      new Polynomial(3.0, -15.0, 18.0)),
	    new CData(new Polynomial(3.0, -7.0, 6.0),
		      new Polynomial(6.0, -15.0, 12.0),
		      new Polynomial(9.0 -21.0, 18.0)),
	    new CData(new Polynomial(3.0, -7.0, 6.0), // case 11
		      new Polynomial(6.0, -14.0, 12.0),
		      new Polynomial(9.0, -21.0, 18.0)),
	    new CData(new Polynomial(2.0, 3.0, 1.0), // case 12
		      new Polynomial(4.0, 6.0, 2.0),
		      new Polynomial(6.0, 9.0, 3.0)),
	    new CData(new Polynomial(-1.0, 1.0, 2.0),
		      new Polynomial(-2.0, 2.0, 4.0),
		      new Polynomial(-3.0, 3.0, 6.0)),
	};

	boolean cases2[] = new boolean[cases.length];
	for (int i = 0; i < array.length; i++) {
	    Polynomial p1 = array[i].p1;
	    Polynomial p2 = array[i].p2;
	    Polynomial p3 = array[i].p3;
	    Polynomial ip1 = p1.integral();
	    Polynomial ip2 = p2.integral();
	    Polynomial ip3 = p3.integral();

	    BezierPolynomial bp1 = new
		BezierPolynomial(Polynomials
				 .toBezier(ip1.getCoefficientsArray(),
					   ip1.getDegree()),
				 ip1.getDegree());
	    BezierPolynomial bp2 = new
		BezierPolynomial(Polynomials
				 .toBezier(ip2.getCoefficientsArray(),
					   ip2.getDegree()),
				 ip2.getDegree());
	    BezierPolynomial bp3 = new
		BezierPolynomial(Polynomials
				 .toBezier(ip3.getCoefficientsArray(),
					   ip3.getDegree()),
				 ip3.getDegree());
	    // Need a copy because cubiecLength and cubicLengthFunction
	    // modify their arguments
	    Polynomial fp1 = new Polynomial(p1);
	    Polynomial fp2 = new Polynomial(p2);
	    Polynomial fp3 = new Polynomial(p3);

	    len = cubicLength(1.0, p1, p2, p3);
	    elen = Path2DInfoTest.getSegmentLength(1.0, bp1, bp2, bp3);
	    flen = cubicLengthFunction(fp1, fp2, fp3).valueAt(1.0);
	    if (Math.abs(len - flen) > 1.e-10) {
		System.out.println("len = " + len);
		System.out.println("flen = " + flen);
		throw new Exception();
	    }

	    if (Math.abs(len - elen)/Math.abs(elen) > 1.e-6) {
		System.out.println("i = " + i);
		System.out.println("len = " + len);
		System.out.println("elen = " + elen);
		for (int j = 0; j < cases.length; j++) {
		    if (cases[j]) {
			System.out.println("cases[" + j + "] = true");
			cases2[j] = cases[j];
		    }
		}
		throw new Exception();
	    } else {
		for (int j = 0; j < cases.length; j++) {
		    if (cases[j]) {
			cases2[j] = cases[j];
		    }
		}
	    }
	    Arrays.fill(cases, false);
	}
	for (int i = 0; i < cases2.length; i++) {
	    if (cases2[i]) {
		System.out.println("cases[" + i + "] = true");
	    }
	}
	System.arraycopy(cases2, 0, cases, 0, cases.length);

	x0 = 20.0;
	y0 = 21.0;
	z0 = 22.0;

	double[] tcoords1 = {
	    32.0, 40.0, 10.0,
	    4.0, 22.0, 12.0,
	    29.0, 4.0, 20.0
	};
	len = cubicLength(1.0, x0, y0, z0, tcoords1);
	elen = getCubicLength(1.0, x0, y0, z0, tcoords1);
	if (Math.abs(len - elen)/Math.abs(elen) > 1.e-6) {
	    System.out.println("len = " + len);
	    System.out.println("elen = " + elen);
	    for (int i = 0; i < cases.length; i++) {
		if (cases[i]) System.out.println("cases[" + i + "] = true");
	    }
	    throw new Exception();
	}


	UniformIntegerRV irv = new UniformIntegerRV(1, true, 40, true);
	for (int i = 0; i < 100000; i++) {
	    if (i > 0 && (i % 10000 == 0)) {
		System.out.println("... " + ((i*100)/100000) + "%");
	    }
	    double[] coords3 = {
		(double)irv.next(), (double)irv.next(), (double)irv.next(),
		(double)irv.next(), (double)irv.next(), (double)irv.next(),
		(double)irv.next(), (double)irv.next(), (double)irv.next()
	    };
	    len = cubicLength(1.0, x0, y0, z0, coords3);
	    elen = getCubicLength(1.0, x0, y0, z0, coords3);
	    try {
		flen = Path3DInfo.cubicLengthFunction(x0, y0, z0, coords3)
		    .valueAt(1.0);
		if (Math.abs(len - flen) > 1.e-10) {
		    System.out.println("len = " + len);
		    System.out.println("flen = " + elen);
		    throw new Exception();
		}
	    } catch (ArithmeticException e) {
	    }
	    if (Math.abs(len - elen)/Math.abs(elen) > 1.e-6) {
		System.out.print("i = " + i +", coords3:");
		for (double v: coords3) {
		    System.out.print(" " + v);
		}
		System.out.println();
		System.out.println("len = " + len);
		System.out.println("elen = " + elen);
		throw new Exception();
	    }
	}
	boolean missing = false;
	for (int i = 0; i < cases.length; i++) {
	    if (cases[i] == false) {
		System.out.println("missing case " + i);
		missing = true;
	    }
	}
	if (missing) throw new Exception();
    }

    static void badcases() throws Exception {
	double t1 = 0.0;
	double t2 = 0.015625;
	int type = 3;
	double x = -196.9615506024416;
	double y = -98.4807753012208;
	double z = 0.0;
	double coords[] =  {
	    0.0, 0.0, 0.0,
	    23.26910921960485, 11.634554609802425, 0.0,
	    46.538218439209714, 23.269109219604857, 0.0
	};

	double len = Path3DInfo.segmentLength(t2, type,
					      x, y, z,
					      coords);
	System.out.println("len = " + len);
	if (len < 0.0 || Double.isNaN(len)) throw new Exception();
    }


    static void testGetCubicLength() throws Exception {
	Path2D cpath2d = Paths2D.createArc(100.0, 100.0, 100.0, 150.0,
					 2*Math.PI, Math.PI/360);
	cpath2d.closePath();
	Path3D cpath = new Path3D.Double(cpath2d, new AffineTransform3D());
	PathIterator3D pit = cpath
	    .getPathIterator(AffineTransform3D
			     .getRotateInstance(Math.PI/4,
						Math.PI/4,
						Math.PI/4));
	double pcoords[] = new double[9];
	double cx0 = 0.0, cy0 = 0.0, cz0 = 0.0;
	double cx = 0.0, cy = 0.0, cz = 0.0;
	double tmp1, tmp2, tmp3, tmp;

	double sum = 0;
	while (!pit.isDone()) {
	    switch(pit.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_CLOSE:
		tmp1 = cx - cx0;
		tmp2 = cy - cy0;
		tmp3 = cz - cz0;
		tmp = Math.sqrt(tmp1*tmp1 + tmp2*tmp2 + tmp3*tmp3);
		sum += tmp;
		break;
	    case PathIterator3D.SEG_MOVETO:
		cx0 = pcoords[0];
		cy0 = pcoords[1];
		cz0 = pcoords[2];
		cx = cx0;
		cy = cy0;
		cz = cz0;
		break;
	    case PathIterator3D.SEG_LINETO:
		// only cubics
		cx = pcoords[0];
		cy = pcoords[1];
		cz = pcoords[2];
		break;
	    case PathIterator3D.SEG_QUADTO:
		// only cubics
		cx = pcoords[3];
		cy = pcoords[4];
		cz = pcoords[5];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		sum += getCubicLength(1.0, cx, cy, cz, pcoords);
		cx = pcoords[6];
		cy = pcoords[7];
		cz = pcoords[8];
		break;
	    default:
		throw new Exception();
	    }
	    pit.next();
	}

	System.out.println("100\u03c0 = " + (100.0*Math.PI));
	System.out.println("sum = " + sum);
	if (Math.abs(sum - 100*Math.PI) > 1.e-10) throw new Exception();

    }

    public static void main(String argv[]) throws Exception {

	badcases();
	testGetCubicLength();

	testCubicLength();

	Path3D path = new Path3D.Double();
	path.moveTo(-8.0, 0.0, 0.0);
	path.lineTo(-8.0, -4.0, 1.0);
	path.lineTo(0.0, -4.0, 1.0);
	path.quadTo(8.0, -4.0, 1.5, 8.0, 0.0, 2.0);
	path.curveTo(8.0, 2.0, 2.3, 2.0, 4.0, 2.5, 0.0, 4.0, 3.0);
	path.curveTo(-2.0, 4.0, 2.5, -8.0, 4.0, 3.0, -8.0, 2.0, 3.5);
	path.closePath();
	path.lineTo(-7.0, 0.0, 1.0);
	path.lineTo(-7.0, 1.0, 2.0);
	path.closePath();

	int j = 0;
	PathIterator3D pit = path.getPathIterator(null);
	double[] coords = new double[9];
	double x = 0.0; double y = 0.0; double z = 0.0;
	double x0 = 0.0; double y0 = 0.0; double z0 = 0.0;;
	double lastX = 0.0; double lastY = 0.0; double lastZ = 0.0;
	double len1, len2, len3;
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		x = coords[0];
		y = coords[1];
		z = coords[2];
		lastX = x;
		lastY = y;
		lastZ = z;
		if (x != Path3DInfo.getX(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getX failed");
		}
		if (y != Path3DInfo.getY(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getY failed");
		}
		if (z != Path3DInfo.getZ(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getZ failed");
		}
		if (0.0 != Path3DInfo.segmentLength(0.4, type, 0.0, 0.0, 0.0,
						    coords)) {
		    throw new Exception("Path3DInfo.getSegmentLength failed");
		}
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z,
						 type, coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z,
						 type, coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
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
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-5) {
			throw new Exception("Z derivative failed");
		    }
		    if (xpp != 0.0 || ypp != 0.0 || zpp != 0.0) {
			throw new Exception
			    ("non-zero second derivative");
		    }
		}
		len1 = Path3DInfo.segmentLength(0.4, type, x, y, z, coords);
		len2 = Path3DInfo.segmentLength(type, x, y, z, coords) * 0.4;
		try {
		    len3 = Path3DInfo
			.segmentLengthFunction(type, x, y, z, coords)
			.valueAt(0.4);
		    if (len1 != len3) throw new Exception();
		} catch(ArithmeticException ae) {
		}
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z, type,
						 coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z, type,
						 coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
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
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-4) {
			throw new Exception("Z derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 1.e-4) {
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 1.e-4) {
			throw new Exception("Y second derivative failed");
		    }
		    if (Math.abs(zpp - (zp1-zp)/0.00001) > 1.e-4) {
			throw new Exception("Z second derivative failed");
		    }
		}
		len1 = Path3DInfo.segmentLength(0.4, type, x, y, z, coords);
		len2 = quadLength(0.4, x, y, z, coords);
		try {
		    len3 = Path3DInfo
			.segmentLengthFunction(type, x, y, z, coords)
			.valueAt(0.4);
		    if (len1 != len3) throw new Exception();
		} catch(ArithmeticException ae) {
		}
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		x = coords[3];
		y = coords[4];
		z = coords[5];
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z,
						 type, coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z,
						 type, coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " + Math.abs(xp - (xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-4) {
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
		    if (Math.abs(zpp - (zp1-zp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(ypp - (yp1-yp)/0.00001));
			throw new Exception("Y second derivative failed");
		    }
		}
		len1 = Path3DInfo.segmentLength(0.4, type, x, y, z, coords);
		len2 = cubicLength(0.4, x, y, z, coords);
		try {
		    len3 = Path3DInfo
			.segmentLengthFunction(type, x, y, z, coords)
			.valueAt(0.4);
		    if (len1 != len3) throw new Exception();
		} catch(ArithmeticException ae) {
		}
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		x = coords[6];
		y = coords[7];
		z = coords[8];
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		coords[0] = lastX;
		coords[1] = lastY;
		coords[2] = lastZ;
		len1 = Path3DInfo.segmentLength(0.4, type, x, y, z, coords);
		len2 = Path3DInfo.segmentLength(type, x, y, z, coords) * 0.4;
		if (Math.abs(len1 - len2) > 1.e-10) {
		    throw new
			Exception("Path2DInfo.getSegmentLength failed");
		}
		try {
		    len3 = Path3DInfo
			.segmentLengthFunction(type, x, y, z, coords)
			.valueAt(0.4);
		    if (len1 != len3) throw new Exception();
		} catch(ArithmeticException ae) {
		}
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    }
	    System.out.println("     ... static-method segment length = "
			       + Path3DInfo.segmentLength(type,
							  x0, y0, z0,
							  coords));
	    System.out.println("     ... segment length["
			       + j
			       +"] = " +
			       Path3DInfo.segmentLength(path, j++));
	    x0 = x;
	    y0 = y;
	    z0 = z;
	    pit.next();
	}

	Path3DInfo.SegmentData sd = null;
	double[] tarray = new double[10];
	double[] narray = new double[10];
	double[] barray = new double[10];
	for(Path3DInfo.Entry entry: Path3DInfo.getEntries(path)) {
	    Point3D start = entry.getStart();
	    x0 = (start == null)? 0.0: start.getX();
	    y0 = (start == null)? 0.0: start.getY();
	    z0 = (start == null)? 0.0: start.getZ();
	    double[] coordinates = entry.getCoords();
	    int st = entry.getType();
	    if (st == PathIterator.SEG_MOVETO) continue;
	    double u = 0.4;
	    double eps = 0.00001;
	    Path3DInfo.UValues uv = new Path3DInfo.UValues(u);
	    sd = new Path3DInfo.SegmentData(st, x0, y0, z0, coordinates, sd);
	    double x1 = Path3DInfo.getX(u, x0, y0, z0, st, coordinates);
	    double y1 = Path3DInfo.getY(u, x0, y0, z0, st, coordinates);
	    double z1 =Path3DInfo.getZ(u, x0, y0, z0, st, coordinates);
	    double x1a = Path3DInfo.getX(u+eps, x0, y0, z0, st, coordinates);
	    double y1a = Path3DInfo.getY(u+eps, x0, y0, z0, st, coordinates);
	    double z1a = Path3DInfo.getZ(u+eps, x0, y0, z0, st, coordinates);
	    double x1b = Path3DInfo.getX(u+2*eps, x0, y0, z0, st, coordinates);
	    double y1b = Path3DInfo.getY(u+2*eps, x0, y0, z0, st, coordinates);
	    double z1b = Path3DInfo.getZ(u+2*eps, x0, y0, z0, st, coordinates);
	    double[] etangent = {x1a - x1, y1a - y1, z1a - z1};
	    double[] etangent2 = {x1b - x1a, y1b - y1a, z1b - z1a};
	    double ds1a = Math.sqrt((x1a - x1)*(x1a-x1)
				    + (y1a - y1)*(y1a-y1)
				    + (z1a - z1)*(z1a-z1));
	    double ds1b = Math.sqrt((x1b - x1a)*(x1b-x1a)
				    + (y1b - y1a)*(y1b-y1a)
				    + (z1b - z1a)*(z1b-z1a));
	    double edsDu1 = ds1a/eps;
	    double edsDu1A = ds1b/eps;
	    double ed2sDu21 = (edsDu1A - edsDu1)/eps;
	    VectorOps.normalize(etangent);
	    VectorOps.normalize(etangent2);
	    double[] enormal = new double[3];
	    VectorOps.sub(enormal, etangent2, etangent);
	    try {
		VectorOps.normalize(enormal);
	    } catch (Exception e) {
		enormal[0] = 0; enormal[1] = 0; enormal[2] = 0;
	    }
	    double dxDu1 = Path3DInfo.dxDu(u, x0, y0, z0, st, coordinates);
	    double dyDu1 = Path3DInfo.dyDu(u, x0, y0, z0, st, coordinates);
	    double dzDu1 = Path3DInfo.dzDu(u, x0, y0, z0, st, coordinates);
	    double d2xDu21 = Path3DInfo.d2xDu2(u, x0, y0, z0, st, coordinates);
	    double d2yDu21 = Path3DInfo.d2yDu2(u, x0, y0, z0, st, coordinates);
	    double d2zDu21 = Path3DInfo.d2zDu2(u, x0, y0, z0, st, coordinates);
	    double dsDu1 = Path3DInfo.dsDu(u, x0, y0, z0, st, coordinates);
	    double d2sDu21 = Path3DInfo.d2sDu2(u, x0, y0, z0, st, coordinates);
	    if (Math.abs(dsDu1 - edsDu1) > 1.e-3) {
		throw new Exception("dsDu");
	    }
	    if (Math.abs(d2sDu21 - ed2sDu21) > 1.e-3) {
		throw new Exception("d2sDu2: " + d2sDu21 + " != "
				    + ed2sDu21);
	    }

	    double curv1 = Path3DInfo.curvature(u, x0, y0, z0, st, coordinates);
	    boolean tstat1 = Path3DInfo.getTangent(u, tarray, 5, x0, y0, z0,
						   st, coordinates);
	    boolean nstat1 = Path3DInfo.getNormal(u, narray, 5, x0, y0, z0,
						   st, coordinates);
	    boolean bstat1 = Path3DInfo.getBinormal(u, barray, 5, x0, y0, z0,
						    st, coordinates);
	    double x2 = sd.getX(uv);
	    double y2 = sd.getY(uv);
	    double z2 = sd.getZ(uv);
	    double dxDu2 = sd.dxDu(uv);
	    double dyDu2 = sd.dyDu(uv);
	    double dzDu2 = sd.dzDu(uv);
	    double d2xDu22 = sd.d2xDu2(uv);
	    double d2yDu22 = sd.d2yDu2(uv);
	    double d2zDu22 = sd.d2zDu2(uv);
	    double dsDu2 = sd.dsDu(uv);
	    double d2sDu22 = sd.d2sDu2(uv);
	    double curv2 = sd.curvature(uv);
	    boolean tstat2 = sd.getTangent(uv, tarray, 0);
	    boolean nstat2 = sd.getNormal(uv, narray, 0);
	    boolean bstat2 = sd.getBinormal(uv, barray, 0);
	    if (Math.abs(x1 - x2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + x1 + " != " + x2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(y1 - y2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + y1 + " != " + y2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(z1 - z2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + z1 + " != " + z2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dxDu1 - dxDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dxDu1 + " != " + dxDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dyDu1 - dyDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dyDu1 + " != " + dyDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dzDu1 - dzDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dzDu1 + " != " + dzDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2xDu21 - d2xDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2yDu21 - d2yDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2zDu21 - d2zDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2zDu21 + " != " + d2zDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dsDu1 - dsDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dsDu1 + " != " + dsDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2sDu21 - d2sDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2sDu21 + " != " + d2sDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(curv1 - curv2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + curv1 + " != " + curv2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (tstat1 != tstat2) {
		throw new Exception("tstat1 != tstat2");
	    }
	    if (nstat1 != nstat2) {
		throw new Exception("nstat1 != nstat2");
	    }
	    if (tstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(tarray[i] - tarray[5+i]) > 1.e-10) {
			throw new Exception("tangents differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + tarray[i] + " != " + tarray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(tarray, 0, tarray, 0, 3)
			     - 1.0) > 1.e-10) {
		    throw new Exception("Tangent not a unit victor");
		}
	    }
	    if (nstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(narray[i] - narray[5+i]) > 1.e-10) {
			throw new Exception("normal vectors differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + narray[i] + " != " + narray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(narray, 0, narray, 0, 3)
			     - 1.0) > 1.e-10) {
		    throw new Exception("Normal vector  not a unit victor");
		}
		if (tstat1) {
		    double dp = VectorOps.dotProduct(narray, 0, tarray, 0, 3);
		    if (Math.abs(dp) > 1.e-10) {
			System.out.format("tarray = (%g, %g, %g)\n",
					  tarray[0], tarray[1], tarray[2]);
			System.out.format("etangent = (%g, %g, %g)\n",
					  etangent[0], etangent[1],
					  etangent[2]);
			System.out.format("narray = (%g, %g, %g)\n",
					  narray[0], narray[1], narray[2]);
			System.out.format("enormal = (%g, %g, %g)\n",
					  enormal[0], enormal[1], enormal[2]);
			System.out.println("enormal dot etangent = "
					   + VectorOps.dotProduct(etangent,
								  enormal));
			System.out.println("type = " + entry.getTypeString());
			System.out.println("angle = "
					   + Math.toDegrees(Math.acos(dp))
					   + " degrees");
			throw new Exception("tangent and normal vectors not "
					    + "perpendicular");
		    }
		} else {
		    throw new Exception("normal vector but no tangent?");
		}
	    }
	    if (bstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(barray[i] - barray[5+i]) > 1.e-10) {
			throw new Exception("binormal vectors differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + barray[i] + " != " + barray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(barray, 0, barray, 0, 3)
			     - 1.0) > 1.e-10) {
		    System.out.format("barray = (%g, %g, %g)\n",
				      barray[0], barray[1], barray[2]);
		    System.out.println("type = " + entry.getTypeString());
		    throw new Exception("Binormal vector  not a unit victor");
		}
	    }
	    if (nstat1 && bstat1) {
		if (Math.abs(VectorOps.dotProduct(narray, 0, barray, 0, 3))
		    > 1.e-10) {
		    throw new Exception("Binormal and Normal vectors "
					+ "not orthogonal");

		}
	    }
	    if (tstat1 && bstat1) {
		if (Math.abs(VectorOps.dotProduct(tarray, 0, barray, 0, 3))
		    > 1.e-10) {
		    throw new Exception("Binormal and tangent vectors "
					+ "not orthogonal");

		}
	    }
	}

	int segStart = 2;
	int segEnd = 6;
	double total = 0.0;
	double segtotal = 0.0;
	for(Path3DInfo.Entry entry: Path3DInfo.getEntries(path)) {
	    System.out.println("index = " + entry.getIndex()
			       + ", type = " + entry.getTypeString()
			       + ", start = " + entry.getStart()
			       + ", end = " + entry.getEnd()
			       + ", length = " +entry.getSegmentLength());
	    total += entry.getSegmentLength();
	    RealValuedFunctOps segf = entry.getSegmentLengthFunction();
	    if (segf != null) {
		double seglen1 = entry.getSegmentLength();
		double seglen2 = segf.valueAt(1.0);
		if (Math.abs(seglen1 - seglen2) > 1.e-10) {
		    System.out.println("seglen1 = " + seglen1);
		    System.out.println("seglen2 = " + seglen2);
		    throw new Exception();
		}
	    }

	    int ind = entry.getIndex();
	    if (ind >= segStart && ind < segEnd) {
		segtotal += entry.getSegmentLength();
	    }
	    double[] c = entry.getCoords();
	    System.out.println("coords[0] = " + c[0]);
	}


	System.out.println("path length = " + Path3DInfo.pathLength(path));
	System.out.println("path length for segments ["
			   + segStart + ", " + segEnd +") = "
			   + Path3DInfo.pathLength(path, segStart, segEnd));
	if (Math.abs(total - Path3DInfo.pathLength(path)) > 1.e-8) {
	    System.out.println("wrong path length");
	    System.exit(1);
	}
	if (Math.abs(segtotal - Path3DInfo.pathLength
		     (path, segStart, segEnd)) > 1.e-8) {
	    System.out.println("wrong segment-range length");
	    System.exit(1);
	}

	Path3D path1 = new Path3D.Double();
	Path3D path2 = new Path3D.Double();

	path1.moveTo(0.0, 0.0, 0.0);
	path2.moveTo(0.0, 0.0, 0.0);
	path1.curveTo(100.0, 0.0, 0.0, 100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	path2.quadTo(100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	
	AffineTransform3D af =
	    AffineTransform3D.getRotateInstance(0.0, Math.PI/6.0, 0.0);

	Path3D path1t = new Path3D.Double(path1, af);
	Path3D path2t = new Path3D.Double(path2, af);

	System.out.println("path1 length: "
			   + Path3DInfo.segmentLength(path1, 1));
	System.out.println("path2 length: "
			   + Path3DInfo.segmentLength(path2, 1));
	System.out.println("path11 length: "
			   + Path3DInfo.segmentLength(path1t, 1));
	System.out.println("path2t length: "
			   + Path3DInfo.segmentLength(path2t, 1));
	coords = new double[9];
	coords[0] = 165.2016; coords[1] = 3.9624;
	coords[3] = 165.2016; coords[4] = 15.8596;

	double[] coordst = new double[9];
	af.transform(coords, 0, coordst, 0, 2);

	double spoint[] = {152.4, 3.9624, 0.0};
	double tpoint[] = new double[3];
	af.transform(spoint, 0, tpoint, 0, 1);
		
	System.out.println("canned length = "
			   + Path3DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624, 0.0,
			    coords));

	System.out.println("canned length after transform = "
			   + Path3DInfo.segmentLength
			   (PathIterator.SEG_QUADTO,
			    tpoint[0], tpoint[1], tpoint[2], coordst));

	double[] coords2 = new double[6];
	coords2[0] = 165.2016; coords2[1] = 3.9624;
	coords2[2] = 165.2016; coords2[3] = 15.8596;

	System.out.println("canned length (2D) = "
			   + Path2DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624,
			    coords2));

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
	final double angle = Math.PI/6.0;
	RealValuedFunction fz = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return theta * 100.0 * Math.tan(angle);
		}
		public double derivAt(double theta) {
		    return 100.0 * Math.tan(angle);
		}
	    };

	CubicSpline xspline = new CubicSpline1(fx, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline yspline = new CubicSpline1(fy, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline zspline = new CubicSpline1(fz, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	pit = Path3DInfo.getPathIterator(null, xspline, yspline, zspline);
	path = new Path3D.Double();
	path.append(pit, false);

	double actual = Math.PI * 100.0 / Math.cos(angle);
	if (Math.abs(Path3DInfo.pathLength(path) - actual) > 1.e-3) {
	    System.out.println("path length for skewed semicircle = "
			       + Path3DInfo.pathLength(path)
			       + ", expecting " + actual);
	    System.exit(1);
	}

	double ocoords[] = {1.0, 2.0, 3.0, 6.0, 7.0, 8.0, 10.0, 20.0, 30.0};
	double ocoords1[] = {6.0, 7.0, 8.0, 10.0, 20.0, 30.0};
	double[] ncoords = new double[12];
	double[] ncoords1 = new double[9];

	Path3DInfo.elevateDegree(2, ncoords, 0, ocoords, 0);
	Path3DInfo.elevateDegree(2, ncoords1, 1.0, 2.0, 3.0, ocoords1);

	if (ncoords[0] != ocoords[0] || ncoords[1] != ocoords[1]
	    || ncoords[2] != ocoords[2]
	    || ncoords[9] != ocoords[6] || ncoords[10] != ocoords[7]
	    || ncoords[11] != ocoords[8]) {
	    throw new Exception("ncoords error");
	}

	for (int i = 0; i < 9; i++) {
	    if (Math.abs(ncoords1[i] - ncoords[i+3]) > 1.e-10) {
	        System.out.format("ncoords1[%d] = %g while ncoords[%d] = %g\n",
				  i, ncoords1[i], i+3, ncoords[i+3]);
		throw new Exception("ncoords error");
	    }
	}

	for (int i = 0; i <= 20; i++) {
	    double u = i/20.0;
	    if (u < 0) u = 0.0;
	    if (u > 1) u = 1.0;
	    double v1 = Path3DInfo.getX(u, 1.0, 2.0, 3.0,
					PathIterator.SEG_QUADTO,
					ocoords1);
	    double v2 =  Path3DInfo.getX(u, 1.0, 2.0, 3.0,
					 PathIterator.SEG_CUBICTO,
					 ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getX(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path3DInfo.getY(u, 1.0, 2.0, 3.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path3DInfo.getY(u, 1.0, 2.0, 3.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getY(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path3DInfo.getZ(u, 1.0, 2.0, 3.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path3DInfo.getZ(u, 1.0, 2.0, 3.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getZ(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	}


	Path3D tpath = new Path3D.Double();
	tpath.moveTo(1.0, 2.0, 2.5);
	tpath.lineTo(2.0, 3.0, 3.5);
	tpath.quadTo(3.0, 5.0, 5.5, 4.0, 10.0, 10.5);
	tpath.curveTo(5.0, 11.0, 11.5, 12.0, 12.0, 12.5, 14.0, 12.0, 12.5);

	double expected[] = {1.0, 2.0, 2.5, 2.0, 3.0, 3.5,
	    3.0, 5.0, 5.5, 4.0, 10.0, 10.5, 5.0, 11.0, 11.5,
	    12.0, 12.0, 12.5, 14.0, 12.0, 12.5};
	double eknots[] = {1.0, 2.0, 2.5, 2.0, 3.0, 3.5,
	    4.0, 10.0, 10.5, 14.0, 12.0, 12.5};
	double knots[] = Path3DInfo.getControlPoints(tpath, false);

	for (int i = 0; i < eknots.length; i++) {
	    if (knots[i] != eknots[i]) {
		System.out.format("knots[%d] = %g, eknots[%d] = %g\n",
				  i, knots[i], i, eknots[i]);
		throw new Exception();
	    }
	}
	double[] array1 = Path3DInfo.getControlPoints(tpath, true);
	if (expected.length != array1.length) throw new Exception();
	for (int i = 0; i < expected.length; i++) {
	    if (expected[i] != array1[i]) throw new Exception();
	}
	double[] array2 = Path3DInfo.getControlPoints(tpath, true, true);
	for (int i = 0; i < knots.length/3; i++) {
	    int j1 = 3*i;
	    int j2 = j1+1;
	    int j3 = j1 + 2;
	    int k1 = 9*i;
	    int k2 = k1 + 1;
	    int k3 = k1 + 2;
	    System.out.format("expecting (%g, %g, %g) = (%g, %g, %g)\n",
			      knots[j1], knots[j2], knots[j3],
			      array2[k1], array2[k2], array2[k3]);
	    if (knots[j1] != array2[k1]) throw new Exception();
	    if (knots[j2] != array2[k2]) throw new Exception();
	    if (knots[j3] != array2[k3]) throw new Exception();
	}
	Path3D tpath2 = new Path3D.Double();
	tpath2.moveTo(array2[0], array2[1], array2[2]);
	int a2len = array2.length;
	for (int i = 0; i < a2len-3; i += 9) {
	    tpath2.curveTo(array2[i+3], array2[i+4], array2[i+5],
			   array2[i+6], array2[i+7], array2[i+8],
			   array2[i+9], array2[i+10], array2[i+11]);
	}
	PathIterator3D pi1 = tpath.getPathIterator(null);
	PathIterator3D pi2 = tpath2.getPathIterator(null);

	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	coords2 = new double[9];
	while (!pi1.isDone()) {
	    if (pi2.isDone()) throw new Exception();
	    Arrays.fill(coords, 0.0);
	    Arrays.fill(coords2, 0.0);
	    int type1 = pi1.currentSegment(coords);
	    int type2 = pi2.currentSegment(coords2);
	    if (type1 == PathIterator.SEG_MOVETO) {
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		if (lastx != coords2[0] || lasty != coords2[1]
		    || lastz != coords2[2]) {
		    throw new Exception();
		}
	    } else {
		for (int k = 0; k <= 9; k++) {
		    double u = k / 10.0;
		    double x1 = Path3DInfo.getX(u, lastx, lasty, lastz,
						type1, coords);
		    double x2 = Path3DInfo.getX(u, lastx, lasty, lastz,
						type2, coords2);
		    double y1 = Path3DInfo.getY(u, lastx, lasty, lastz,
						type1, coords);
		    double y2 = Path3DInfo.getY(u, lastx, lasty, lastz,
						type2, coords2);
		    double z1 = Path3DInfo.getY(u, lastx, lasty, lastz,
						type1, coords);
		    double z2 = Path3DInfo.getY(u, lastx, lasty, lastz,
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
		    if (Math.abs(z1 - z2) > 1.e-10) {
			System.out.format("type1 = %d, type2 = %d)\n",
					  type1, type2);
			System.out.format("z1 = %s, z2 = %s\n", z1, z2);
			throw new Exception();
		    }
		}
		switch(type1) {
		case PathIterator.SEG_LINETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    lastz = coords[2];
		    if (lastx != coords2[6] || lasty != coords2[7]
			|| lastz != coords2[8]) {
			throw new Exception();
		    }
		    break;
		case PathIterator.SEG_QUADTO:
		    lastx = coords[3];
		    lasty = coords[4];
		    lastz = coords[5];
		    if (lastx != coords2[6] || lasty != coords2[7]
			|| lastz != coords2[8]) {
			throw new Exception();
		    }
		    break;
		case PathIterator.SEG_CUBICTO:
		    lastx = coords[6];
		    lasty = coords[7];
		    lastz = coords[8];
		    if (lastx != coords2[6] || lasty != coords2[7]
			|| lastz != coords2[8]) {
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
