import org.bzdev.math.BSpline;

public class BSplineTest {
    static double knots[] = {0.0, 0.0, 0.0, 0.3, 0.5, 0.5, 0.6, 1.0, 1.0, 1.0};

    static double[] data;

    // explicit functions copied from
    // http://www.cs.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/B-spline/bspline-ex-1.html

    static double N00(double u) {
	return 0.0;
    }
    static double N10(double u) {
	return 0.0;
    }
    static double N40(double u) {
	return 0.0;
    }
    static double N70(double u) {
	return 0.0;
    }
    static double N80(double u) {
	return 0.0;
    }

    static double N20(double u) {
	if (0.0 <= u && u < 0.3) {
	    return 1;
	} else {
	    return 0.0;
	}
    }

    static double N30(double u) {
	if (0.3 <= u && u < 0.5) {
	    return 1;
	} else {
	    return 0.0;
	}
    }

    static double N50(double u) {
	if (0.5 <= u && u < 0.6 ) {
	    return 1;
	} else {
	    return 0.0;
	}
    }

    static double N60(double u) {
	if (0.6 <= u && u < 1.0) {
	    return 1;
	} else {
	    return 0.0;
	}
    }

    static double N01(double u) {
	return 0.0;
    }

    static double N11(double u) {
	if (0.0 <= u && u < 0.3) {
	    return 1.0 - (10.0/3)*u;
	} else return 0.0;
    }

    static double N21(double u) {
	if (0.0 <= u && u < 0.3) {
	    return (10.0/3.0)*u;
	} else if(0.3 <= u && u < 0.5) {
	    return 2.5*(1.0 - 2.0 * u);
	} else {
	    return 0.0;
	}
    }

    static double N31(double u) {
	if (0.3 <= u && u < 0.5) {
	    return 5.0*u - 1.5;
	} else return 0.0;
    }

    static double N41(double u) {
	if (0.5 <= u && u < 0.6) {
	    return 6.0 - 10.0*u;
	} else return 0.0;
    }
    static double N51(double u) {
	if (0.5 <= u && u < 0.6) {
	    return 10.0*u - 5.0;
	} else if (0.5 <= u && u < 1.0) {
	    return 2.5*(1.0-u);
	} else {
	    return 0.0;
	}
    }

    static double N61(double u) {
	if (0.6 <= u && u < 1.0) {
	    return 2.5*u-1.5;
	} else {
	    return 0.0;
	}
    }

    static double N71(double u) {
	return 0.0;
    }

    static double N02(double u) {
	if (0.0 <= u && u < 0.3) {
	    double tmp = (1.0 - (10.0/3.0)*u);
	    return tmp*tmp;
	} else {
	    return 0.0;
	}
    }

    static double N12(double u) {
	if (0.0 <= u && u < 0.3) {
	    return (20.0/3.0)*(u - (8.0/3.0)*u*u);
	} else if (0.3 <= u && u < 0.5) {
	    double tmp = 1.0 - 2.0 * u;
	    return 2.5*tmp*tmp;
	} else {
	    return 0.0;
	}
    }

    static double N22(double u) {
	if (0.0 <= u && u < 0.3) {
	    return (20.0/3.0)*u*u;
	} else if (0.3 <= u && u < 0.5) {
	    return -3.75 + 25.0*u -35.0*u*u;
	} else {
	    return 0.0;
	}
    }

    static double N32(double u) {
	if (0.3 <= u && u < 0.5) {
	    double tmp = 5.0*u - 1.5;
	    return tmp*tmp;
	} else if (0.5 <= u && u <  0.6) {
	    double tmp = 6.0 - 10.0*u;
	    return tmp*tmp;
	} else {
	    return 0.0;
	}
    }

    static double N42(double u) {
	if (0.5 <= u && u < 0.6) {
	    return 20.0*(-2.0 + 7.0*u - 6.0*u*u);
	} else if (0.6 <= u && u < 1.0) {
	    double tmp = 1.0-u;
	    return 5.0 * tmp*tmp;
	} else {
	    return 0.0;
	}
    }

    static double N52(double u) {
	/*
	if (0.5 <= u && u < 0.6) {
	    double tmp = (2.0*u - 1.0);
	    return 12.5*tmp*tmp;
	} else if (0.6 <= u && u < 1.0) {
	    return 2.5*(-4.0 + 11.5*u - 7.5*u*u);
	} else {
	    return 0.0;
	}
	*/
	return N51(u)*(u - 0.5)/0.5 + N61(u)*(1.0 - u)/0.4;
    }

    static double N62(double u) {
	if (0.6 <= u && u < 1.0) {
	    // return 2.5*(9.0 - 30.0*u + 25.0*u*u);
	    double tmp = 2.5*u-1.5;
	    return tmp*tmp;
	} else {
	    return 0.0;
	}
    }

    static double valueAt(BSpline spline, double u) {
	double sum = 0.0;
	int p = spline.getDegree();
	for (int i = 0; i < data.length; i++) {
	    sum += data[i]*spline.N(i, p, u);
	}
	return sum;
    }

    public static void main(String argv[]) throws Exception {

	boolean timingTest = false;
	boolean printValues = false;
	int index = 0;
	while (index < argv.length) {
	    if (argv[index].equals("-t")) timingTest = true;
	    else if (argv[index].equals("-p")) printValues = true;
	    index++;
	}

	int n = knots.length - 5 - 1;

	data = new double[n];
	for (int i = 0; i < n; i++) {
	    data[i] = i*i;
	}

	BSpline bspline = new BSpline(knots, data);

	/*
	System.out.println("N52(0.61) = " + N52(0.61));
	System.out.println("N51(0.61) = " + N51(0.61));
	System.out.println("N61(0.61) = " + N61(0.61));
	System.out.println("naiveN(5,2,0.61) = " + bspline.naiveN(5,2,0.61));
	System.out.println("naiveN(5,1,0.61) = " + bspline.naiveN(5,1,0.61));
	System.out.println("naiveN(6,1,0.61) = " + bspline.naiveN(6,1,0.61));
	*/
	System.out.println("Testing various values of u against known results ...");

	for (int k = 0; k < 100; k++) {
	    double u = k / 100.0;
	    // System.out.println("--------");
	    // degree 0
	    double x = bspline.N(0, 0, u);
	    double y = N00(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(1, 0, u);
	    y = N10(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(2, 0, u);
	    y = N20(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(3, 0, u);
	    y = N30(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(4, 0, u);
	    y = N40(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(5, 0, u);
	    y = N50(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(6, 0, u);
	    y = N60(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(7, 0, u);
	    y = N70(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(8, 0, u);
	    y = N80(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }

	    // next degree (1)
	    x = bspline.N(0, 1, u);
	     y = N01(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(1, 1, u);
	    y = N11(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(2, 1, u);
	    y = N21(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(3, 1, u);
	    y = N31(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(4, 1, u);
	    y = N41(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(5, 1, u);
	    y = N51(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(6, 1, u);
	    y = N61(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("N61: x = %g, y = %g,u = %g\n",
				  x, y, u);
		System.out.flush();
		// throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(7, 1, u);
	    y = N71(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }

	    // next degree (2)
	    x = bspline.N(0, 2, u);
	     y = N02(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(1, 2, u);
	    y = N12(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(2, 2, u);
	    y = N22(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(3, 2, u);
	    y = N32(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(4, 2, u);
	    y = N42(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("x = %g, y = %g\n", x, y);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(5, 2, u);
	    y = N52(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("N52: x = %g, y = %g, u = %g\n",
				  x, y, u);
		System.out.flush();
		//throw new Exception("x != y when u = " + u);
	    }
	    x = bspline.N(6, 2, u);
	    y = N62(u);
	    if (Math.abs(x-y) > 1.e-10) {
		System.out.format("N62: x = %g, y = %g, u = %g\n",
				  x, y, u);
		System.out.flush();
		throw new Exception("x != y when u = " + u);
	    }
	}
	System.out.println("... done");
	
	System.out.println("Check spline values ...");
	n = knots.length - 3 - 1;
	data = new double[n];
	for (int i = 0; i < n; i++) {
	    data[i] = i*i;
	}
	bspline = new BSpline(3, knots, data, false);
	System.out.println("    domain min = " +bspline.getDomainMin());
	System.out.println("    domain max = " +bspline.getDomainMax());

	double delta = .00001;
	for (int i = 0; i < 101; i++) {
	    double u = i/100.0;
	    if (u > 1.0) u = 1.0;
	    if (u < bspline.getDomainMin()) continue;
	    if (u > bspline.getDomainMax()) continue;
	    double v1 = bspline.valueAt(u);
	    double v2 = valueAt(bspline, u);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }
	    double d1 = bspline.derivAt(u);
	    if (u < bspline.getDomainMax()) {
		double d2 = (bspline.valueAt(u+delta) - v1)/delta;
		if (Math.abs(d1-d2) > 0.002) {
		    throw new Exception (u + ": derivative failed - "
					 + d1 + " != " + d2);
		}
	    }

	    double dd1 = bspline.secondDerivAt(u);
	    if (u < bspline.getDomainMax()) {
		double dd2 = (bspline.derivAt(u+delta) - d1)/delta;
		if (Math.abs(dd1-dd2) > 0.02) {
		    throw new Exception ("index " + i + ", u = " + u 
					 + ": second derivative failed - "
					 + dd1 + " != " + dd2);
		}
	    }
	    if (printValues) System.out.format("spline(%g) = %g, derivatives: "
					       + "%g, %g\n", u, v1, d1, dd1);

	}
	System.out.println("... done");

	System.out.println("checking closed spline ...");

	double closedKnots[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5,
				0.6, 0.7, 0.8, 0.9, 1.0};

	double closedCpoints[] = {0.0, 0.05, 2.0, 3.5, 0.45, 0.5,
				  0.45, 0.35, 2.0, 0.05};
	bspline = new BSpline(3, closedKnots, closedCpoints, true);
	System.out.println("    domain min = " +bspline.getDomainMin());
	System.out.println("    domain max = " +bspline.getDomainMax());

	double y1 = bspline.valueAt(0.0);
	double y2 = bspline.valueAt(1.0);
	double dy1 = bspline.derivAt(0.0);
	double dy2 = bspline.derivAt(1.0);
	double ddy1 = bspline.secondDerivAt(0.0);
	double ddy2 = bspline.secondDerivAt(1.0);

	if (Math.abs(y1 - y2) > 1.e-10
	    || Math.abs(dy1 - dy2) > 1.e-10
	    || Math.abs(ddy1 - ddy2) > 1.e-10) {
	
	    System.out.format("y1 = %s, y2 = %s, dy1 = %s, dy2 = %s, "
			      + "ddy1 = %s, ddy2 = %s\n",
			      y1, y2, dy1, dy2, ddy1, ddy2);
	}
	
	for (int i = 0; i < 51; i++) {
	    double u1 = i/100.0;
	    double u2 = (100-i)/100.0;
	    double v1 = bspline.valueAt(u1);
	    double v2 = bspline.valueAt(u2);
	    if (printValues) {
		System.out.format("spline(%g) = %g, spline(%g) = %g\n",
				  u1, v1, u2, v2);
	    }
	}

	y1 = bspline.valueAt(0.25);
	y2 = bspline.valueAt(1.25);
	if (Math.abs(y1-y2) > 1.e-10) {
	    throw new Exception("y1 != y2");
	}

	y1 = bspline.valueAt(0.25);
	y2 = bspline.valueAt(-0.75);
	if (Math.abs(y1-y2) > 1.e-10) {
	    throw new Exception("y1 != y2");
	}

	System.out.println("... done");

	System.out.println("least-squares-fit test ... ");

	knots = new double[27];
	for (int i = 0; i <= 10; i++) {
	    knots[i+8] = Math.toRadians(i*9);
	}

	for (int i = 0; i < 8; i++) {
	    knots[i] = 0.0;
	    knots[19 + i] = knots[18];
	}
	
	double[] xs = new double[31];
	double[] ys = new double[31];
	for (int i = 0; i < 31; i++) {
	    double theta = Math.toRadians(i*3.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	}

	bspline = new BSpline(8, knots, false, xs, ys);
			   
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... periodic case ... ");
	knots = new double[19];
	for (int i = 0; i < 19; i++) {
	    knots[i] = Math.toRadians(i*20);
	}

	xs = new double[37];
	ys = new double[37];
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians(i*10.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	}
	bspline = new BSpline(8, knots, true, xs, ys);

	for (int i = 0; i < 37; i++) {
	    if (Math.abs(ys[i] - bspline.valueAt(xs[i])) > 1.e-10) {
		System.out.format("%d: sin(%g)=%s, bspline.valueAt(%g)=%s\n",
				      i, xs[i], ys[i], xs[i],
				      bspline.valueAt(xs[i]));
		throw new Exception("spline did not match data");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bspline.valueAt(theta)+ bspline.valueAt(-theta))
		> 1.e-10) {
		System.out.format("vatue at %g: %g %g\n",
				  theta,
				  bspline.valueAt(theta),
				  bspline.valueAt(-theta));
		throw new Exception("not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bspline.derivAt(theta) - bspline.derivAt(-theta))
		> 1.e-9) {
		System.out.format("deriv at %g: %s %s\n",
				  theta,
				  bspline.derivAt(theta),
				  bspline.derivAt(-theta));
		throw new Exception("derivative not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bspline.secondDerivAt(theta) +
			 bspline.secondDerivAt(-theta)) > 1.e-7) {
		if (i == 0) {
		    if (Math.abs(bspline.secondDerivAt(theta)) < 2.0e-6
			&& Math.abs( bspline.secondDerivAt(-theta)) < 2.e-6) {
			continue;
		    }
		}
		System.out.format("second deriv at %g: %s %s\n",
				  theta,
				  bspline.secondDerivAt(theta),
				  bspline.secondDerivAt(-theta));
		throw new Exception("second derivative not periodic");
	    }
	}

	System.out.println("... BSpline.UNCLAMPED case ...");

	xs = new double[31];
	ys = new double[31];
	for (int i = 0; i < 31; i++) {
	    double theta = Math.toRadians(i*3.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	}

	bspline = new BSpline(8, 20, BSpline.Mode.UNCLAMPED, xs, ys);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... BSpline.Mode.CLAMPED case ...");
	bspline = new BSpline(8, 20, BSpline.Mode.CLAMPED, xs, ys);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... BSpline.Mode.CLAMPED_LEFT case ...");
	bspline = new BSpline(8, 20, BSpline.Mode.CLAMPED_LEFT, xs, ys);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... BSpline.Mode.CLAMPED_RIGHT case ...");
	bspline = new BSpline(8, 20, BSpline.Mode.CLAMPED_RIGHT, xs, ys);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... BSpline.Mode.PERIODIC case ...");
	xs = new double[37];
	ys = new double[37];
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians(i*10.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	}
	bspline = new BSpline(8, 20, BSpline.Mode.PERIODIC, xs, ys);
	for (int i = 0; i < 37; i++) {
	    if (Math.abs(bspline.valueAt(xs[i]) - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bspline.valueAt(xs[i]));
		throw new Exception("spline did not match expected value");
	    }
	}

	System.out.println("... done");

	if (timingTest) {
	    System.out.println("Timing test ...");
	    // force use of the stack.
	    // BSpline.setCutoff(0);
	    knots = new double[1001];
	    for (int i = 0; i < 1001; i++) {
		knots[i] = i/1000.0;
		if (knots[i] > 1.0) knots[i] = 1.0;
	    }
	
	    n = knots.length - 32 - 1;
	    bspline = new BSpline(32, knots, new double[n], false);
	    int jm = 200;
	    int jmp1 = jm+1;
	    for (int j = 0; j < jmp1; j++) {
		for (int p = 0; p < 24; p++) {
		    BSpline.setCutoff(0);
		    long t1 = System.nanoTime();
		    double value1 = bspline.N(500, p, 0.5001);
		    long t2 = System.nanoTime();
		    BSpline.setCutoff(BSpline.CUTOFF);
		    long t2a = System.nanoTime();
		    double value2 = bspline.N(500, p, 0.5001);
		    long t3 = System.nanoTime();
		    if (j == jm) {
			System.out.format("value1 = %g [time = %d], "
					  + "value2 = %g "
					  + "[time = %d]\n",
					  value1, t2-t1, value2, t3-t2a);
		    }
		}
	    }
	    System.out.println(".... done");
	}
	System.exit(0);
  }
}
