import org.bzdev.math.*;

public class B3TriangleTest {

    /*
    static int[][] lambdas = {{0, 0, 3},
			      {0, 1, 2},
			      {0, 2, 1},
			      {0, 3, 0},
			      {1, 0, 2},
			      {1, 1, 1},
			      {1, 2, 0},
			      {2, 0, 1},
			      {2, 1, 0},
			      {3, 0, 0}};
    */


    public static void main(String argv[]) throws Exception {

	/*
	for (int index: Functions.Bernstein.generateIndices(3, 3)) {
	    int lambdas[] = Functions.Bernstein.lambdasForIndex(3, 3, index);
	    if (lambdas.length != 3) {
		throw new Exception("lambdas.length: " + lambdas.length);
	    }
	    System.out.format("{%d, %d, %d}\n",
			      lambdas[0], lambdas[1], lambdas[2]);
	}
	*/
	
	System.out.println("test BicubicTriangleInterp "
			   + "with the default triangle");
	BicubicTriangleInterp bti =
	    new BicubicTriangleInterp(0.1, 0.3, 0.4, 0.2,
				      0.5, 0.6, 0.4,
				      0.4, 0.4,
				      0.15);

	final double edge1[] = {0.1, 0.3, 0.4, 0.2};
	final double edge2[] = {0.1, 0.5, 0.4, 0.15};
	final double edge3[] = {0.2, 0.4, 0.4, 0.15};

	RealValuedFunction e1f = new RealValuedFunction() {
		public double valueAt(double x) {
		    return Functions.Bernstein.sumB(edge1, 3, x);
		}
	    };
	RealValuedFunction e2f = new RealValuedFunction() {
		public double valueAt(double x) {
		    return Functions.Bernstein.sumB(edge2, 3, x);
		}
	    };
	RealValuedFunction e3f = new RealValuedFunction() {
		public double valueAt(double x) {
		    return Functions.Bernstein.sumB(edge3, 3, x);
		}
	    };

	double argsArray[][] = {{0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0},
				{0.2, 0.2}, {0.5, 0.5}, {1.0, 1.0},
				{0.7, 0.4}, {0.4, 0.7}, {-0.1, 0.0},
				{1.1, 0.0}, {0.0, 1.1}, {0.0, -0.1},
				{-1.0, -1.0}, {2.0, 2.0}, {0.51, 0.51}};
	int index = 0;
	boolean indom = true;
	for (double[] args: argsArray) {
	    if ((index++) > 4) indom = false;
	    if (bti.isInDomain(args[0], args[1]) != indom) {
		System.out.format("... bti.isInDomain(%g,%g) = %s\n",
				  args[0], args[1],
				  bti.isInDomain(args[0],args[1]));
		System.exit(1);
	    }
	}
	double testArray[][] = {{0.0, 0.0, 0.1},
				{0.0, 1.0, 0.2},
				{1.0, 0.0, 0.15}};
	for (double[] args: testArray) {
	    if (Math.abs(bti.valueAt(args[0], args[1]) - args[2]) > 1.e-10) {
		System.out.format("... (%g, %g): %g != %g\n",
				  args[0], args[1],
				  bti.valueAt(args[0], args[1]), args[2]);
		System.exit(1);
	    }
	}

	for (int i = 0; i <= 10; i++) {
	    double x = i / 10.0;
	    if (Math.abs(bti.valueAt(0.0, x) - e1f.valueAt(x)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(0.0, x), x, e1f.valueAt(x));
		System.exit(1);
	    }
	    if (Math.abs(bti.valueAt(x, 0.0) - e2f.valueAt(x)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(x, 0.0), x, e2f.valueAt(x));
		System.exit(1);
	    }

	    if (Math.abs(bti.valueAt(x, 1.0-x) - e3f.valueAt(x)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(x, 0.0), x, e3f.valueAt(x));
		System.exit(1);
	    }
	}

	double delta = 0.00001;
	for (int i = 0; i < 10; i++) {
	    double x = i / 10.0;
	    for (int j = 0; j < 10; j++) {
		double y = j /10.0;
		if (bti.isInDomain(x,y) && bti.isInDomain(x+delta, y)
		    && bti.isInDomain(x, y + delta)
		    && bti.isInDomain(x+delta, y+ delta)) {
		    // test derivatives
		    double val = bti.deriv1At(x,y);
		    double est = (bti.valueAt(x+delta,y) - bti.valueAt(x,y))
			/ delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv1(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv2At(x,y);
		    est = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... " 
					  + "deriv2(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv11At(x,y);
		    double est1 = (bti.valueAt(x+delta,y) - bti.valueAt(x,y))
			/ delta;
		    double est2 =
			(bti.valueAt(x,y) - bti.valueAt(x - delta, y))
			/ delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv11(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv22At(x,y);
		    est1 = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    est2 = (bti.valueAt(x,y) - bti.valueAt(x,y-delta)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv22(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv12At(x,y);
		    est1 = (bti.valueAt(x+delta,y+delta)-bti.valueAt(x,y+delta))
			/ delta;
		    est2 = (bti.valueAt(x+delta,y) - bti.valueAt(x,y)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv12(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv21At(x,y);
		    est1 = (bti.valueAt(x+delta,y+delta)-bti.valueAt(x+delta,y))
			/ delta;
		    est2 = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv21(%g,%g) failed: %g != %g\n",
					 x, y, val, est);
			System.exit(1);
		    }
		}
	    }
	}
	System.out.println("... OK");

	System.out.println("try with ranges for x and y");

	bti =
	    new BicubicTriangleInterp(-1.0, 1.0, -3.0, 3.0,
				      0.1, 0.3, 0.4, 0.2,
				      0.5, 0.6, 0.4,
				      0.4, 0.4,
				      0.15);

	for (int i = 0; i <= 10; i++) {
	    double u = i / 10.0;
	    for (int j = 0; j <= 10; j++) {
		if (i + j > 10) continue;
		double v = j / 10.0;
		double w = 1.0 - (u + v);
		
		double x1 = bti.xFromUV(u, v);
		double x2 = bti.xFromVW(v, w);
		double x3 = bti.xFromWU(w, u);
		if (Math.abs(x1-x2) > 1.e-10 || Math.abs(x2-x3) > 1.e-10) {
		    System.out.println("xFrom?? failed");
		    System.exit(1);
		}
		double y1 = bti.yFromUV(u, v);
		double y2 = bti.yFromVW(v, w);
		double y3 = bti.yFromWU(w, u);
		if (Math.abs(y1-y2) > 1.e-10 || Math.abs(y2-y3) > 1.e-10) {
		    System.out.println("yFrom?? failed");
		    System.exit(1);
		}
		double u1 = bti.uFromXY(x1, y1);
		double v1 = bti.vFromXY(x1, y1);
		double w1 = bti.wFromXY(x1, y1);
		if (Math.abs(u-u1) > 1.e-10
		    || Math.abs(v-v1) > 1.e-10
		    || Math.abs(w-w1) > 1.e-10) {
		    System.out.format("[uvw]FromXY failed: "
				      + "(%g, %g, %g) != (%g, %g, %g)\n",
				      u1, v1, w1, u, v, w);
		    System.exit(1);
		}
	    }
	}

	index = 0;
	indom = true;
	for (double[] args: argsArray) {
	    double x = args[0]*2.0 - 1.0;
	    double y = args[1] * 6.0 - 3.0;
	    if ((index++) > 4) indom = false;
	    if (bti.isInDomain(x, y) != indom) {
		System.out.format("... bti.isInDomain(%g,%g) = %s\n",
				  x, y,
				  bti.isInDomain(x,y));
		System.exit(1);
	    }
	}

	for (double[] args: testArray) {
	    double x = args[0]*2.0 - 1.0;
	    double y = args[1] * 6.0 - 3.0;
	    if (Math.abs(bti.valueAt(x, y) - args[2]) > 1.e-10) {
		System.out.format("... (%g, %g): %g != %g\n",
				  x, y,
				  bti.valueAt(x, y), args[2]);
		System.exit(1);
	    }
	}

	for (int i = 0; i <= 10; i++) {
	    double u = i / 10.0;
	    double x = u*2.0 - 1.0;
	    double y = u* 6.0 - 3.0;
	    double y1 = (1.0-u)*6.0 - 3.0;
	    if (Math.abs(y1 - bti.yForZeroW(x)) > 1.e-10) {
		System.out.println("... yForZeroW failed");
		System.exit(1);
	    }
	    if (Math.abs(x - bti.xForZeroW(y1)) > 1.e-10) {
		System.out.println("... xForZeroW failed");
		System.exit(1);
	    }
	    if (Math.abs(bti.valueAt(-1.0, y) - e1f.valueAt(u)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(0.0, x), u, e1f.valueAt(u));
		System.exit(1);
	    }
	    if (Math.abs(bti.valueAt(x, -3.0) - e2f.valueAt(u)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(x, 0.0), u, e2f.valueAt(u));
		System.exit(1);
	    }

	    if (Math.abs(bti.valueAt(x, y1) - e3f.valueAt(u)) > 1.e-10) {
		System.out.format("... bti(0.0, %g) = %g, e1f(%g) = %g\n",
				  x, bti.valueAt(x, 0.0), u, e3f.valueAt(u));
		System.exit(1);
	    }
	}
	for (int i = 0; i < 10; i++) {
	    double u = i / 10.0;
	    double x = u*2.0 - 1.0;
	    for (int j = 0; j < 10; j++) {
		double v = j / 10.0;
		double y = u* 6.0 - 3.0;
		if (bti.isInDomain(x,y) && bti.isInDomain(x+delta, y)
		    && bti.isInDomain(x, y + delta)
		    && bti.isInDomain(x+delta, y+ delta)) {
		    // test derivatives
		    double val = bti.deriv1At(x,y);
		    double est = (bti.valueAt(x+delta,y) - bti.valueAt(x,y))
			/ delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv1(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv2At(x,y);
		    est = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv2(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv11At(x,y);
		    double est1 = (bti.valueAt(x+delta,y) - bti.valueAt(x,y))
			/ delta;
		    double est2 =
			(bti.valueAt(x,y) - bti.valueAt(x - delta, y))
			/ delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv11(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv22At(x,y);
		    est1 = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    est2 = (bti.valueAt(x,y) - bti.valueAt(x,y-delta)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv22(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv12At(x,y);
		    est1 = (bti.valueAt(x+delta,y+delta)-bti.valueAt(x,y+delta))
			/ delta;
		    est2 = (bti.valueAt(x+delta,y) - bti.valueAt(x,y)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv12(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		    val = bti.deriv21At(x,y);
		    est1 = (bti.valueAt(x+delta,y+delta)-bti.valueAt(x+delta,y))
			/ delta;
		    est2 = (bti.valueAt(x,y+delta) - bti.valueAt(x,y)) / delta;
		    est = (est1 - est2) / delta;
		    if (Math.abs(est-val) > 0.001) {
			System.out.format("... "
					  + "deriv21(%g,%g) failed: %g != %g\n",
					  x, y, val, est);
			System.exit(1);
		    }
		}
	    }
	}
	System.out.println("... OK");
	System.exit(0);
    }
}
