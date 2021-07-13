import org.bzdev.math.CubicSpline;
import org.bzdev.math.CubicSpline2;
import org.bzdev.math.GLQuadrature;
import org.bzdev.lang.CallableArgsReturns;
import org.bzdev.math.RealValuedFunction;

public class SplineTest2 {

    private static double quad(double x) {
	return 5.0 + 3.0 * x + 7.0 * x * x;
    }

    private static double f(double x) {return x + Math.sin(x);}
    private static double fp(double x) {return 1.0 + Math.cos(x);}

    public static void main(String argv[]) throws Exception {

	double[] y1 = {1.0, 2.0};
	double[] x1 = {10.0, 20.0};
	CubicSpline spline = new CubicSpline2(x1, y1);

	for (int i = 0; i <= 10; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = 1.0 + (x-10.0)/10;
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("linear case 1 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
	    }
	}
	
	double[] y2 = {1.0, 2.0, 3.0};
	double[] x2 = {10.0, 20.0, 30.0};
	spline = new CubicSpline2(x2, y2);

	for (int i = 0; i <= 20; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = 1.0 + (x-10.0)/10;
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("linear case 2 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
	    }
	}

	double[] y3 = {quad(10.0), quad(15.0), quad(30.0)};
	double[] x3 = {10.0, 15.0, 30.0};
	spline = new CubicSpline2(x3, y3, CubicSpline.Mode.QUAD_FIT);
	for (int i = 0; i <= 20; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = quad(x);
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("quadratic case 1 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
	    }
	}
	
	double[] y3a = {quad(10.0), quad(20.0), quad(30.0), quad(40.0)};
	double[] x3a = {10.0, 25.0, 35.0, 40.0};
	spline = new CubicSpline2(x3a, y3a);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK");
	else System.out.println("spline for y3a verification failed");

	final CubicSpline s1 = spline;
	System.out.println("s1.valueAt(10.0) = " + s1.valueAt(10.0)
			   + ", quad(10.0) = " + quad(10.0));
	System.out.println("s1.valueAt(10.1) = " + s1.valueAt(10.1));
	System.out.println("s1.valueAt(15.0) = " + s1.valueAt(15.0));
	System.out.println("s1.valueAt(24.9) = " + s1.valueAt(24.9));
	System.out.println("s1.valueAt(25.0) = " + s1.valueAt(25.0)
			   + ", quad(20.0) = " + quad(20.0));
	System.out.println("s1.valueAt(25.1) = " + s1.valueAt(25.1));
	System.out.println("s1.valueAt(35.0) = " + s1.valueAt(35.0)
			   + ", quad(30.0) = " + quad(30.0));
	System.out.println("s1.valueAt(39.9) = " + s1.valueAt(39.9));
	System.out.println("s1.valueAt(40.0) = " + s1.valueAt(40.0)
			   + ", quad(40.0) = " + quad(40.0));
	
	double x = 15.0;
	System.out.println("At " + x + ", " + 
			   ((s1.valueAt(x+0.001) - s1.valueAt(x))/0.001)
			   + " should match derivative " + s1.derivAt(x));

	System.out.println("At " + x + ", " + 
			   ((s1.derivAt(x+0.001) - s1.derivAt(x))/0.001)
			   + " should match second derivative "
			   + s1.secondDerivAt(x));

	GLQuadrature q = new GLQuadrature(16) {
		public double function(double x) {
		    return s1.derivAt(x);
		}
	    };
	double integral = q.integrate(10.0, 30.0, 4);
	if (Math.abs(integral - (s1.valueAt(30.0) - s1.valueAt(10.0))) > 1.e-8) {
	    System.out.println("bad derivative: " + integral + " != "
			       + (s1.valueAt(30.0) - s1.valueAt(10.0)));
	}

	GLQuadrature q2 = new GLQuadrature(16) {
		public double function(double x) {
		    return s1.secondDerivAt(x);
		}
	    };
	integral = q2.integrate(10.0, 30.0, 4);
	if (Math.abs(integral - (s1.derivAt(30.0) - s1.derivAt(10.0))) > 1.e-8) {
	    System.out.println("bad second derivative: " + integral + " != "
			       + (s1.derivAt(30.0) - s1.derivAt(10.0)));
	}

	spline = new CubicSpline2(x3a, y3a,
				  CubicSpline.Mode.PARABOLIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic start");
	else System.out.println("spline for y3a verification failed "
				+ "[parbolic start]");

	spline = new CubicSpline2(x3a, y3a,
				  CubicSpline.Mode.PARABOLIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic end");
	else System.out.println("spline for y3a verification failed "
				+ "[parabolic end]");


	spline = new CubicSpline2(x3a, y3a, CubicSpline.Mode.PARABOLIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic runout");
	else System.out.println("spline for y3a verification failed "
				+ "parabolic runout");

	spline = new CubicSpline2(x3a, y3a,
				 CubicSpline.Mode.CUBIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic start");
	else System.out.println("spline for y3a verification failed "
				+ "[parbolic start]");

	spline = new CubicSpline2(x3a, y3a, CubicSpline.Mode.CUBIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic end");
	else System.out.println("spline for y3a verification failed "
				+ "[cubic end]");


	spline = new CubicSpline2(x3a, y3a, CubicSpline.Mode.CUBIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic runout");
	else System.out.println("spline for y3a verification failed "
				+ "cubic runout");


	spline = new CubicSpline2(x3a, y3a,
				  CubicSpline.Mode.CLAMPED_START, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped start");
	else System.out.println("spline for y3a verification failed "
				+ "clamped start");

	spline = new CubicSpline2(x3a, y3a,
				 CubicSpline.Mode.CLAMPED_END, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped end");
	else System.out.println("spline for y3a verification failed "
				+ "clamped end");

	spline = new CubicSpline2(x3a, y3a,
				 CubicSpline.Mode.CLAMPED, 0.0, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped");
	else System.out.println("spline for y3a verification failed "
				+ "clamped");
	System.out.println("when spline for y3a clamped to zero (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));

	spline = new CubicSpline2(x3a, y3a,
				 CubicSpline.Mode.CLAMPED, 1.0, 1.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped to 1, 1");
	else System.out.println("spline for y3a verification failed "
				+ "clamped to 1, 1");

	System.out.println("when spline for y3a clamped to 1.0 (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));



	double[] y4 = new double[31];
	double[] x4 = new double[31];
	for (int i = 0; i < 31; i++) {
	    x4[i] = 10.0 + (double) i;
	    y4[i] = quad(x4[i]);
	}
	spline = new CubicSpline2(x4, y4);

	double max = 0.0;
	for (int i = 0; i <= 300 ; i++) {
	    double u = 10.0 + i/10.0;
	    if (u > 10.0 + 30) break;
	    
	    double v = Math.abs((spline.valueAt(u) - quad(u))/quad(u));
	    if (v > max) max = v;
	}
	System.out.println("max fractional error = " + max);
 

	spline = new CubicSpline2(x3a, y3a);
	
	max = 0.0;
	for (int i = 0; i < 30; i++) {
	    double xx = 10.0 + i;
	    double y = quad(xx);
	    try {
		double u = Math.abs(spline.valueAt(spline.inverseAt(y)) - y);
		if (u > max) max = u;
		/*
		double xxx = spline.inverseAt(y);
		System.out.println("x = " + xx +", inverse = "
				   + xxx
				   + ", quad(" +xx + ") = " + y
				   + ", spline.valueAt(" + xxx + ") = "
				   + spline.valueAt(xxx));
		*/
				   
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}
	System.out.println("y3a inverse error = " + max);
	System.out.println("-----------");			 
	double[] y5 = {quad(40.0), quad(30.0), quad(20.0), quad(10.0)};
	double[] x5 = {10.0, 25.0, 35.0, 40.0};
	spline = new CubicSpline2(x5, y5);
	max = 0.0;
	for (int i = 0; i < 30; i++) {
	    x = 10.0 + i;
	    double xx = 40.0 - i;
	    double y = quad(xx);
	    try {
		double u = Math.abs(spline.valueAt(spline.inverseAt(y))- y);
		if (u > max) max = u;
		/*
		double xxx = spline.inverseAt(y);
		System.out.println("x = " + x +", inverse = "
				   + xxx
				   + ", quad(" +xx + ") = " + y
				   + ", spline.valueAt(" + xxx + ") = "
				   + spline.valueAt(xxx));
		*/
				   
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(1);
	    }
	}
	System.out.println("y5 inverse error = " + max);

	double[] y6 = new double[37];
	double[] y6p = new double[37];
	double x6[] = new double[37];
	for (int i = 0; i <= 36; i++) {
	    x = Math.toRadians(i*10);
	    x6[i] = x;
	    y6[i] = Math.sin(x);
	    y6p[i] = Math.cos(x);
	}
	spline = new CubicSpline2(x6, y6,
				 CubicSpline.Mode.HERMITE, y6p);
	
	System.out.println("spline(0.0) = " + spline.valueAt(0.0)
			   + ", deriv = " + spline.derivAt(0.0));
	System.out.println("spline(pi/4) = " + spline.valueAt(Math.PI/4.0)
			   + ", deriv = " + spline.derivAt(Math.PI/4.0));

	System.out.println("spline(pi/2) = " + spline.valueAt(Math.PI/2.0)
			   + ", deriv = " + spline.derivAt(Math.PI/2.0));

	double[] y7 = new double[361];
	double[] y7p = new double[361];
	double[] x7 = new double[361];
	for (int i = 0; i <= 360; i++) {
	    x = Math.toRadians(i*1.0);
	    x7[i] = x;
	    y7[i] = Math.sin(x);
	    y7p[i] = Math.cos(x);
	}
	spline = new CubicSpline2(x7, y7, CubicSpline.Mode.HERMITE, y7p);
	System.out.println("now space points by 1 degree:");
	System.out.println("spline(0.0) = " + spline.valueAt(0.0)
			   + ", deriv = " + spline.derivAt(0.0));
	System.out.println("spline(pi/4) = " + spline.valueAt(Math.PI/4.0)
			   + ", deriv = " + spline.derivAt(Math.PI/4.0));

	System.out.println("spline(pi/2) = " + spline.valueAt(Math.PI/2.0)
			   + ", deriv = " + spline.derivAt(Math.PI/2.0));
 

	double[] y8 = new double[100];
	double[] yp8 = new double[100];
	double[] x8 = new double[100];
	for (int i = 0; i < 100; i++) {
	    x = i/10.0;
	    x8[i] = x;
	    y8[i] = f(x);
	    yp8[i] = fp(x);
	}

        spline = new CubicSpline2(x8, y8, CubicSpline.Mode.HERMITE, yp8);
	if (spline.verify(1.e-10)) {
	    System.out.println("Hermite spline verification for y8 ok");
	} else {
	    System.out.println("Hermite spline verification for y8 failed");
	}

	max = 0.0;
	double dmax = 0.0;

	for (int i = 0; i <= 990; i++) {
	    x = i/100.0;
	    double z = Math.abs(spline.valueAt(x) - f(x));
	    if (z > max) {
		max = z;
	    }
	    z = Math.abs(spline.derivAt(x) - fp(x));
	    if (z > dmax) {
		dmax = z;
	    }
	}
	System.out.println ("for Hermite spline, max = " + max
			   + ", dmax = " + dmax);

	System.out.println("Hermite spline at non-uniformly spaced points...");
	double[] y9 = new double[100];
	double[] yp9 = new double[100];
	double[] x9 = new double[100];
	for (int i = 0; i < 100; i++) {
	    x = i*i/1000.0;
	    x9[i] = x;
	    y9[i] = f(x);
	    yp9[i] = fp(x);
	}

        spline = new CubicSpline2(x9, y9, CubicSpline.Mode.HERMITE, yp9);
	if (spline.verify(1.e-10)) {
	    System.out.println("Hermite spline verification for y9 ok");
	} else {
	    System.out.println("Hermite spline verification for y9 failed");
	}

	max = 0.0;
	dmax = 0.0;

	for (int i = 0; i <= 990; i++) {
	    x = i/100.0;
	    if (x > x9[99]) continue;
	    double z = Math.abs(spline.valueAt(x) - f(x));
	    if (z > max) {
		max = z;
	    }
	    z = Math.abs(spline.derivAt(x) - fp(x));
	    if (z > dmax) {
		dmax = z;
	    }
	}
	System.out.println ("for Hermite spline, max = " + max
			   + ", dmax = " + dmax);

	double args[] = {0.0, Math.PI/6, Math.PI/3, Math.PI/2,
			 4*Math.PI/6, 5*Math.PI/6, Math.PI};

	double[] xvalues = new double[11];
	for (int i = 0; i < 11; i++) {
	    xvalues[i] = Math.PI*i/10.0;
	}
	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double x) {return Math.sin(x);}
		public double derivAt(double x) {return Math.cos(x);}
	    };

	int errcount = 0;
	System.out.println("test splines created with a RealValuedFunction");
	System.out.println("... new CubicSpline2(xvalues, f)");
	spline = new CubicSpline2(xvalues, f);
	for (double xx: args) {
	    if (Math.abs(spline.valueAt(xx) - f.valueAt(xx)) > 1.e-4) {
		System.out.println("spline mismatch at xx = " + xx
				   + ": spline.valueAt = "
				   + spline.valueAt(xx)
				   + " while f.valueAt = " + f.valueAt(xx));
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	}

	CubicSpline.Mode modes[] = {CubicSpline.Mode.NATURAL,
				    CubicSpline.Mode.PARABOLIC_RUNOUT,
				    CubicSpline.Mode.PARABOLIC_RUNOUT_START,
				    CubicSpline.Mode.PARABOLIC_RUNOUT_END,
				    CubicSpline.Mode.CUBIC_RUNOUT,
				    CubicSpline.Mode.CUBIC_RUNOUT_START,
				    CubicSpline.Mode.CUBIC_RUNOUT_END,
				    CubicSpline.Mode.CUBIC_START_PARABOLIC_END,
				    CubicSpline.Mode.PARABOLIC_START_CUBIC_END,
				    CubicSpline.Mode.CLAMPED,
				    CubicSpline.Mode.CLAMPED_START,
				    CubicSpline.Mode.CLAMPED_END,
				    CubicSpline.Mode.CLAMPED_START_PARABOLIC_END,
				    CubicSpline.Mode.PARABOLIC_START_CLAMPED_END,
				    CubicSpline.Mode.CLAMPED_START_CUBIC_END,
				    CubicSpline.Mode.CUBIC_START_CLAMPED_END,
				    CubicSpline.Mode.HERMITE,
	};

	for (CubicSpline.Mode mode: modes) {
	    System.out.println("... new CubicSpline2(xvalues, f, " + mode + ")");
	    spline = new CubicSpline2(xvalues, f, mode);
	    double err;
	    switch (mode) {
	    case PARABOLIC_RUNOUT:
	    case PARABOLIC_RUNOUT_START:
	    case PARABOLIC_RUNOUT_END:
		err = 3.e-4;
		break;
	    case CLAMPED_START_PARABOLIC_END:
	    case PARABOLIC_START_CLAMPED_END:
	    case CUBIC_START_CLAMPED_END:
		err = 7.0e-4;
		break;
	    case CLAMPED_START_CUBIC_END:
		err = 7.2e-4;
		break;
	    default:
		err = 1.e-4;
		break;
	    }

	    for (double xx: args) {
		if (Math.abs(spline.valueAt(xx) - f.valueAt(xx)) > err) {
		    System.out.println("spline mismatch at xx = " + xx
				       + ": spline.valueAt = "
				       + spline.valueAt(xx)
				       + " while f.valueAt = " + f.valueAt(xx));
		    errcount++;
		}
	    }
	}

	for (CubicSpline.Mode mode: modes) {
	    switch(mode) {
	    case NATURAL:
	    case PARABOLIC_RUNOUT:
	    case PARABOLIC_RUNOUT_START:
	    case CUBIC_RUNOUT:
	    case CUBIC_RUNOUT_START:
	    case PARABOLIC_RUNOUT_END:
	    case CUBIC_RUNOUT_END:
	    case CUBIC_START_PARABOLIC_END:
	    case PARABOLIC_START_CUBIC_END:
	    case HERMITE:
		// tested these above - no extra arguments needed
		continue;
	    case CLAMPED:
		System.out.println("... new CubicSpline2(xvalues, f, "
				   + mode + ", " + f.derivAt(0.0)
				   + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicSpline2(xvalues, f, mode,
					  f.derivAt(0.0), f.derivAt(Math.PI));
		break;
	    case CLAMPED_START:
	    case CLAMPED_START_CUBIC_END:
	    case CLAMPED_START_PARABOLIC_END:
		System.out.println("... new CubicSpline2(xvalues, f, "
				   + mode + ", " + f.derivAt(0.0) + ")");
		spline = new CubicSpline2(xvalues, f, mode, f.derivAt(0.0));
		break;
	    case CLAMPED_END:
	    case PARABOLIC_START_CLAMPED_END:
	    case CUBIC_START_CLAMPED_END:
		System.out.println("... new CubicSpline2(xvalues, f, 10, "
				   + mode + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicSpline2(xvalues, f, mode, f.derivAt(Math.PI));
		break;
	    default:
		continue;
	    }

	    double err;
	    switch (mode) {
	    case CLAMPED_START_PARABOLIC_END:
	    case PARABOLIC_START_CLAMPED_END:
		err = 3.e-4;
		break;
	    case CLAMPED_START_CUBIC_END:
	    case CUBIC_START_CLAMPED_END:
		err = 8.0e-4;
		break;
	    default:
		err = 1.e-4;
		break;
	    }

	    for (double xx: args) {
		if (Math.abs(spline.valueAt(xx) - f.valueAt(xx)) > err) {
		    System.out.println("spline mismatch at xx = " + xx
				       + ": spline.valueAt = "
				       + spline.valueAt(xx)
				       + " while f.valueAt = " + f.valueAt(xx));
		    errcount++;
		}
	    }
	}



	System.exit(0);
    }
}
