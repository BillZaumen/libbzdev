import org.bzdev.math.BSpline;
import org.bzdev.math.BSplineArray;
import java.util.Arrays;

public class BSplineArrayTest {
    static double knots[] = {0.0, 0.0, 0.0, 0.3, 0.5, 0.5, 0.6, 1.0, 1.0, 1.0};

    static double[] data;
    static double[] data3;

    // explicit functions copied from
    // http://www.cs.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/B-spline/bspline-ex-1.html


    static double valueAt(BSpline spline, double u) {
	double sum = 0.0;
	int p = spline.getDegree();
	for (int i = 0; i < data.length; i++) {
	    sum += data[i]*spline.N(i, p, u);
	}
	return sum;
    }

    public static void main(String argv[]) throws Exception {

	int n = knots.length - 5 - 1;

	data = new double[n];
	for (int i = 0; i < n; i++) {
	    data[i] = i*i;
	}

	BSpline bspline = new BSpline(knots, data);
	BSplineArray bsa = new BSplineArray(1, knots, data);

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
	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < 5; j++) {
		    if (Math.abs(bspline.N(i,j,u) -bsa.N(i,j,u)) > 1.e-10) {
			throw new Exception("bad N(i,j)");
		    }
		}
	    }
	}
	System.out.println("... done");
	
	System.out.println("Check spline values ...");
	n = knots.length - 3 - 1;
	data = new double[n];
	data3 = new double[n*3];
	int k3 = 0;
	for (int i = 0; i < n; i++) {
	    data[i] = i*i;
	    data3[k3++] = data[i];
	    data3[k3++] = 2*data[i];
	    data3[k3++] = 3*data[i];
	}

	bspline = new BSpline(3, knots, data, false);
	bsa = new BSplineArray(1, 3, knots, data, false);
	BSplineArray bsa3 = new BSplineArray(3, 3, knots, data3, false);
	BSplineArray bsa4 = new BSplineArray(3, knots, data3);

	if (Math.abs(bspline.getDomainMin() - bsa.getDomainMin()) > 1.e-10) {
	    throw new Exception("getDomainMin");
	}
	if (Math.abs(bspline.getDomainMax() - bsa.getDomainMax()) > 1.e-10) {
	    throw new Exception("getDomainMax");
	}

	if (Math.abs(bsa3.getDomainMin() - bsa.getDomainMin()) > 1.e-10) {
	    throw new Exception("getDomainMin");
	}
	if (Math.abs(bsa3.getDomainMax() - bsa.getDomainMax()) > 1.e-10) {
	    throw new Exception("getDomainMax");
	}

	if (Math.abs(bsa4.getDomainMin() - bsa.getDomainMin()) > 1.e-10) {
	    throw new Exception("getDomainMin");
	}
	if (Math.abs(bsa4.getDomainMax() - bsa.getDomainMax()) > 1.e-10) {
	    throw new Exception("getDomainMax");
	}


	double delta = .00001;
	for (int i = 0; i < 101; i++) {
	    double u = i/100.0;
	    if (u > 1.0) u = 1.0;
	    if (u < bspline.getDomainMin()) continue;
	    if (u > bspline.getDomainMax()) continue;

	    double v1 = bspline.valueAt(u);
	    double v2 = bsa3.valueAt(u)[0];
	    double v3 = bsa4.valueAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }
	    if (Math.abs(v1 - v3) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }

	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.valueAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
		if (Math.abs(v1 - bsa4.valueAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa4 valueAt failed");
		}
	    }

	    v1 = bspline.derivAt(u);
	    v2 = bsa.derivAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }

	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.derivAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 derivAt failed");
		}
		if (Math.abs(v1 - bsa4.derivAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa4 derivAt failed");
		}
	    }

	    v1 = bspline.secondDerivAt(u);
	    v2 = bsa.secondDerivAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }

	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.secondDerivAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 secondDerivAt failed");
		}
	    }

	}
	System.out.println("... done");

	System.out.println("checking closed spline ...");

	double closedKnots[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5,
				0.6, 0.7, 0.8, 0.9, 1.0};

	double closedCpoints[] = {0.0, 0.05, 2.0, 3.5, 0.45, 0.5,
				  0.45, 0.35, 2.0, 0.05};

	double closedCpoints3[] = new double[3*closedCpoints.length];
	k3 = 0;
	for (int i = 0; i < closedCpoints.length; i++) {
	    closedCpoints3[k3++] = closedCpoints[i];
	    closedCpoints3[k3++] = 2*closedCpoints[i];
	    closedCpoints3[k3++] = 3*closedCpoints[i];
	}

	bspline = new BSpline(3, closedKnots, closedCpoints, true);
	bsa = new BSplineArray(1, 3, closedKnots, closedCpoints, true);
	bsa3 =new BSplineArray(3, 3, closedKnots, closedCpoints3, true);

	if (Math.abs(bspline.getDomainMin() - bsa.getDomainMin()) > 1.e-10) {
	    throw new Exception("getDomainMin");
	}
	if (Math.abs(bspline.getDomainMax() - bsa.getDomainMax()) > 1.e-10) {
	    throw new Exception("getDomainMax");
	}
	for (int i = 0; i < 101; i++) {
	    double u = i/100.0;
	    if (u > 1.0) u = 1.0;
	    if (u < 0.0) u = 0.0;
	    double v1 = bspline.valueAt(u);
	    double v2 = bsa.valueAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }

	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.valueAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
	    }

	    v1 = bspline.derivAt(u);
	    v2 = bsa.derivAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }


	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.derivAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 derivAt failed");
		}
	    }

	    v1 = bspline.secondDerivAt(u);
	    v2 = bsa.secondDerivAt(u)[0];
	    if (Math.abs(v1 - v2) > 1.e-10) {
		throw new Exception("u = " + u + ": " + v1 + " != " + v2);
	    }

	    for (int j = 0; j < 3; j++) {
		if (Math.abs(v1 - bsa3.secondDerivAt(u)[j]/(j+1)) > 1.e-10) {
		    throw new Exception("bsa3 secondDerivAt failed");
		}
	    }

	}

	double y1 = bspline.valueAt(1.25);
	double y2 = bsa.valueAt(1.25)[0];
	if (Math.abs(y1-y2) > 1.e-10) {
	    throw new Exception("y1 != y2");
	}

	y1 = bspline.valueAt(-0.75);
	y2 = bsa.valueAt(-0.75)[0];
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
	double[] ys3 = new double[31*3];
	k3 = 0;
	for (int i = 0; i < 31; i++) {
	    double theta = Math.toRadians(i*3.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}

	bsa = new BSplineArray(1,8, knots, false, xs, ys);
	bsa3 = new BSplineArray(3,8, knots, false, xs, ys3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println(" ... periodic case ... ");
	knots = new double[19];
	for (int i = 0; i < 19; i++) {
	    knots[i] = Math.toRadians(i*20);
	}

	xs = new double[37];
	ys = new double[37];
	ys3 = new double[37*3];
	k3 = 0;
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians(i*10.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}
	bsa = new BSplineArray(1,8, knots, true, xs, ys);
	bsa3 = new BSplineArray(3,8, knots, true, xs, ys3);

	for (int i = 0; i < 37; i++) {
	    if (Math.abs(ys[i] - bsa.valueAt(xs[i])[0]) > 1.e-10) {
		System.out.format("%d: sin(%g)=%s, bspline.valueAt(%g)=%s\n",
				      i, xs[i], ys[i], xs[i],
				      bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match data");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.valueAt(theta)[0]+ bsa.valueAt(-theta)[0])
		> 1.e-10) {
		System.out.format("value at %g: %g %g\n",
				  theta,
				  bsa.valueAt(theta)[0],
				  bsa.valueAt(-theta)[0]);
		throw new Exception("not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.derivAt(theta)[0] - bsa.derivAt(-theta)[0])
		> 1.e-9) {
		System.out.format("deriv at %g: %s %s\n",
				  theta,
				  bsa.derivAt(theta)[0],
				  bsa.derivAt(-theta)[0]);
		throw new Exception("derivative not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.secondDerivAt(theta)[0] +
			 bsa.secondDerivAt(-theta)[0]) > 1.e-7) {
		if (i == 0) {
		    if (Math.abs(bsa.secondDerivAt(theta)[0]) < 2.0e-6
			&& Math.abs( bsa.secondDerivAt(-theta)[0]) < 2.e-6) {
			continue;
		    }
		}
		System.out.format("second deriv at %g: %s %s\n",
				  theta,
				  bsa.secondDerivAt(theta)[0],
				  bsa.secondDerivAt(-theta)[0]);
		throw new Exception("second derivative not periodic");
	    }
	}

	System.out.println("... non-periodic with sigmas ... ");

	knots = new double[27];
	for (int i = 0; i <= 10; i++) {
	    knots[i+8] = Math.toRadians(i*9);
	}

	for (int i = 0; i < 8; i++) {
	    knots[i] = 0.0;
	    knots[19 + i] = knots[18];
	}
	
	xs = new double[31];
	ys = new double[31];
	double[] sigmas = new double[31];
	ys3 = new double[31*3];
	double[] sigmas3 = new double[31*3];
	k3 = 0;
	for (int i = 0; i < 31; i++) {
	    double theta = Math.toRadians(i*3.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}
	Arrays.fill(sigmas, 0, sigmas.length, 1.0);
	Arrays.fill(sigmas3, 0, sigmas3.length, 1.0);

	bsa = new BSplineArray(1, 8, knots, false, xs, ys, sigmas);
	bsa3 = new BSplineArray(3, 8, knots, false, xs, ys3, sigmas3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}


	System.out.println("... periodic with sigmas ... ");

	knots = new double[19];
	for (int i = 0; i < 19; i++) {
	    knots[i] = Math.toRadians(i*20);
	}

	xs = new double[37];
	ys = new double[37];
	sigmas = new double[37];
	ys3 = new double[37*3];
	sigmas3 = new double[37*3];
	k3 = 0;
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians(i*10.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}
	Arrays.fill(sigmas, 0, sigmas.length, 1.0);
	Arrays.fill(sigmas3, 0, sigmas3.length, 1.0);

	bsa = new BSplineArray(1,8, knots, true, xs, ys, sigmas);
	bsa3 = new BSplineArray(3,8, knots, true, xs, ys3, sigmas3);

	for (int i = 0; i < 37; i++) {
	    if (Math.abs(ys[i] - bsa.valueAt(xs[i])[0]) > 1.e-10) {
		System.out.format("%d: sin(%g)=%s, bspline.valueAt(%g)=%s\n",
				      i, xs[i], ys[i], xs[i],
				      bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match data");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.valueAt(theta)[0]+ bsa.valueAt(-theta)[0])
		> 1.e-10) {
		System.out.format("value at %g: %g %g\n",
				  theta,
				  bsa.valueAt(theta)[0],
				  bsa.valueAt(-theta)[0]);
		throw new Exception("not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.derivAt(theta)[0] - bsa.derivAt(-theta)[0])
		> 1.e-9) {
		System.out.format("deriv at %g: %s %s\n",
				  theta,
				  bsa.derivAt(theta)[0],
				  bsa.derivAt(-theta)[0]);
		throw new Exception("derivative not periodic");
	    }
	}

	for ( int i = 0; i < 10; i++) {
	    double theta = Math.toRadians(i/1000.0);
	    if (Math.abs(bsa.secondDerivAt(theta)[0] +
			 bsa.secondDerivAt(-theta)[0]) > 1.e-7) {
		if (i == 0) {
		    if (Math.abs(bsa.secondDerivAt(theta)[0]) < 2.0e-6
			&& Math.abs( bsa.secondDerivAt(-theta)[0]) < 2.e-6) {
			continue;
		    }
		}
		System.out.format("second deriv at %g: %s %s\n",
				  theta,
				  bsa.secondDerivAt(theta)[0],
				  bsa.secondDerivAt(-theta)[0]);
		throw new Exception("second derivative not periodic");
	    }
	}

	System.out.println("... BSpline.Mode.UNCLAMPED case ...");
	xs = new double[31];
	ys = new double[31];
	ys3 = new double[31*3];
	k3 = 0;
	sigmas = new double[31];
	sigmas3 = new double[31*3];

	for (int i = 0; i < 31; i++) {
	    double theta = Math.toRadians(i*3.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}
	Arrays.fill(sigmas, 0, sigmas.length, 1.0);
	Arrays.fill(sigmas3, 0, sigmas3.length, 1.0);

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.UNCLAMPED, xs, ys);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.UNCLAMPED, xs, ys3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.UNCLAMPED, xs, ys, sigmas);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.UNCLAMPED,
				xs, ys3, sigmas3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println("... BSpline.Mode.CLAMPED case ...");

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.CLAMPED, xs, ys);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.CLAMPED, xs, ys3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-6) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.CLAMPED, xs, ys, sigmas);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.CLAMPED, xs, ys3,
				sigmas3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-6) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println("... BSpline.Mode.CLAMPED_LEFT case ...");

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.CLAMPED_LEFT, xs, ys);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.CLAMPED_LEFT, xs, ys3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.CLAMPED_LEFT,
			       xs, ys, sigmas);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.CLAMPED_LEFT,
				xs, ys3, sigmas3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-10) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println("... Bspline.Mode.CLAMPED_RIGHT case ...");

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.CLAMPED_RIGHT, xs, ys);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.CLAMPED_RIGHT, xs, ys3);
	for (int i = 0; i < 31; i++) {
	    if (Math.abs(bsa.valueAt(xs[i])[0] - ys[i]) > 1.e-6) {
		System.out.format("%d: sin(%g) = %s, spline value = %s\n",
			      i, xs[i], ys[i], bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match expected value");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-6) {
		    System.out.format
			("bsa.valueAt(%g)[0] = %s, bsa3.valueAt(%g)[%d] = %s\n",
			 xs[i], v1, xs[i], j, bsa3.valueAt(xs[i])[j]);
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println("... BSpline.Mode.PERIODIC case ... ");
	xs = new double[37];
	ys = new double[37];
	sigmas = new double[37];
	ys3 = new double[37*3];
	sigmas3 = new double[37*3];
	k3 = 0;
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians(i*10.0);
	    xs[i] = theta;
	    ys[i] = Math.sin(theta);
	    ys3[k3++] = ys[i];
	    ys3[k3++] = 2.0*ys[i];
	    ys3[k3++] = 3.0*ys[i];
	}
	Arrays.fill(sigmas, 0, sigmas.length, 1.0);
	Arrays.fill(sigmas3, 0, sigmas3.length, 1.0);

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.PERIODIC, xs, ys);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.PERIODIC, xs, ys3);

	for (int i = 0; i < 37; i++) {
	    if (Math.abs(ys[i] - bsa.valueAt(xs[i])[0]) > 1.e-10) {
		System.out.format("%d: sin(%g)=%s, bspline.valueAt(%g)=%s\n",
				      i, xs[i], ys[i], xs[i],
				      bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match data");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	bsa = new BSplineArray(1,8, 20, BSpline.Mode.PERIODIC, xs, ys, sigmas);
	bsa3 = new BSplineArray(3,8, 20, BSpline.Mode.PERIODIC, xs, ys3,
				sigmas3);

	for (int i = 0; i < 37; i++) {
	    if (Math.abs(ys[i] - bsa.valueAt(xs[i])[0]) > 1.e-10) {
		System.out.format("%d: sin(%g)=%s, bspline.valueAt(%g)=%s\n",
				      i, xs[i], ys[i], xs[i],
				      bsa.valueAt(xs[i])[0]);
		throw new Exception("spline did not match data");
	    }
	    double v1 = bsa.valueAt(xs[i])[0];
	    for (int j = 0; j < 3; j++) {
		double v2 = bsa3.valueAt(xs[i])[j]/(j+1);
		if (Math.abs(v1-v2) > 1.e-10) {
		    throw new Exception("bsa3 valueAt failed");
		}
	    }
	}

	System.out.println("... done");

	System.exit(0);
  }
}
