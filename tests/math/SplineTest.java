import org.bzdev.math.CubicSpline;
import org.bzdev.math.CubicSpline1;
import org.bzdev.math.GLQuadrature;
import org.bzdev.lang.CallableArgsReturns;
import org.bzdev.math.Functions;
import org.bzdev.math.Functions.Bernstein;
import org.bzdev.math.RealValuedFunction;


public class SplineTest {

    private static final double M[][] = {{ 1.0,  0.0,  0.0,  0.0},
					 {-3.0,  3.0,  0.0,  0.0},
					 { 3.0, -6.0,  3.0,  0.0},
					 {-1.0,  3.0, -3.0,  1.0}};


    private static final double MI9[][] = {{9.0, 0.0, 0.0, 0.0},
					   {9.0, 3.0, 0.0, 0.0},
					   {9.0, 6.0, 3.0, 0.0},
					   {9.0, 9.0, 9.0, 9.0}};

    private static final double MI[][] = {{1.0, 0.0, 0.0, 0.0},
					  {1.0, 1.0/3.0, 0.0, 0.0},
					  {1.0, 2.0/3.0, 1.0/3.0, 0.0},
					  {1.0, 1.0, 1.0, 1.0}};

    private static double quad(double x) {
	return 5.0 + 3.0 * x + 7.0 * x * x;
    }

    private static double f(double x) {return x + Math.sin(x);}
    private static double fp(double x) {return 1.0 + Math.cos(x);}

    public static void main(String argv[]) throws Exception {

	double[] y1 = {1.0, 2.0};
	CubicSpline spline = new CubicSpline1(y1, 10.0, 10.0);

	System.out.print("coefficients for {1.0, 2.0}: ");
	if (spline instanceof CubicSpline1) {
	    for (double c: ((CubicSpline1) spline).getBernsteinCoefficients()) {
		System.out.print(" "+ c);
	    }
	    System.out.println();
	}

	for (int i = 0; i <= 10; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = 1.0 + (x-10.0)/10;
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("linear case 1 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
		System.exit(1);
	    }
	}
	
	double[] y2 = {1.0, 2.0, 3.0};
	spline = new CubicSpline1(y2, 10.0, 10.0);
	System.out.print("coefficients for {1.0, 2.0, 3.0}: ");
	if (spline instanceof CubicSpline1) {
	    for (double c: ((CubicSpline1) spline).getBernsteinCoefficients()) {
		System.out.print(" "+ c);
	    }
	    System.out.println();
	}


	for (int i = 0; i <= 20; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = 1.0 + (x-10.0)/10;
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("linear case 2 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
		System.exit(1);
	    }
	}

	double[] y3 = {quad(10.0), quad(20.0), quad(30.0)};
	spline = new CubicSpline1(y3, 10.0, 10.0, CubicSpline.Mode.QUAD_FIT);
	System.out.format("coefficients (quad case) {%g, %g, %g}: ",
			  y3[0], y3[1], y3[2]);
	if (spline instanceof CubicSpline1) {
	    for (double c: ((CubicSpline1) spline).getBernsteinCoefficients()) {
		System.out.print(" "+ c);
	    }
	    System.out.println();
	}
	for (int i = 0; i <= 20; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = quad(x);
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("quadratic case 1 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
		System.exit(1);
	    }
	}
	

	double[] y3a = {quad(10.0), quad(20.0), quad(30.0), quad(40.0)};
	spline = new CubicSpline1(y3a, 10.0, 10.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK");
	else {
	    System.out.println("spline for y3a verification failed");
	    System.exit(1);
	}

	final CubicSpline s1 = spline;
	System.out.println("s1.valueAt(10.0) = " + s1.valueAt(10.0)
			   + ", quad(10.0) = " + quad(10.0));
	System.out.println("s1.valueAt(10.1) = " + s1.valueAt(10.1));
	System.out.println("s1.valueAt(15.0) = " + s1.valueAt(15.0)
			   + ", quad(15.0) = " + quad(15.0));
	System.out.println("s1.valueAt(19.9) = " + s1.valueAt(19.9));
	System.out.println("s1.valueAt(20.0) = " + s1.valueAt(20.0)
			   + ", quad(20.0) = " + quad(20.0));
	System.out.println("s1.valueAt(20.1) = " + s1.valueAt(20.1));
	System.out.println("s1.valueAt(25.0) = " + s1.valueAt(25.0)
			   + ", quad(25.0) = " + quad(25.0));

	System.out.println("s1.valueAt(29.9) = " + s1.valueAt(29.9));
	System.out.println("s1.valueAt(30.0) = " + s1.valueAt(30.0)
			   + ", quad(30.0) = " + quad(30.0));
	
	System.out.format("coefficients (quad(x)) {%g, %g, %g, %g}: ",
			  y3a[0], y3a[1], y3a[2], y3a[3]);
	double[] coefficients = null;
	if (spline instanceof CubicSpline1) {
	    coefficients = ((CubicSpline1) spline).getBernsteinCoefficients();
	    for (double c: coefficients) {
		System.out.print(" "+ c);
	    }
	    System.out.println();
	}
	double coeff1[] = {coefficients[0], coefficients[1],
			   coefficients[2], coefficients[3]};
	double coeff2[] = {coefficients[3], coefficients[4],
			   coefficients[5], coefficients[6]};
	System.out.println("expect s1.valueAt(15) to be "
			   + Functions.Bernstein.sumB(coeff1, 3, 0.5));
	System.out.println("expect s1.valueAt(25) to be "
			   + Functions.Bernstein.sumB(coeff2, 3, 0.5));

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
	double integral = q.integrate(10.0, 30.0, 2);
	if (Math.abs(integral - (s1.valueAt(30.0)-s1.valueAt(10.0))) > 1.e-8) {
	    System.out.println("bad derivative: " + integral + " != "
			       + (s1.valueAt(30.0) - s1.valueAt(10.0)));
	}

	GLQuadrature q2 = new GLQuadrature(16) {
		public double function(double x) {
		    return s1.secondDerivAt(x);
		}
	    };
	integral = q2.integrate(10.0, 30.0, 2);
	if (Math.abs(integral - (s1.derivAt(30.0)-s1.derivAt(10.0))) > 1.e-8) {
	    System.out.println("bad second derivative: " + integral + " != "
			       + (s1.valueAt(30.0) - s1.valueAt(10.0)));
	}


	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.PARABOLIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parbolic start]");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.PARABOLIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parabolic end]");
	    System.exit(1);
	}


	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.PARABOLIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic runout");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "parabolic runout");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CUBIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parbolic start]");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CUBIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[cubic end]");
	    System.exit(1);
	}


	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CUBIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic runout");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "cubic runout");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CLAMPED_START, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped start");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CLAMPED_END, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped end");
	    System.exit(1);
	}

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CLAMPED, 0.0, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped");
	    System.exit(1);
	}
	System.out.println("when spline for y3a clamped to zero (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));

	spline = new CubicSpline1(y3a, 10.0, 10.0,
				 CubicSpline.Mode.CLAMPED, 1.0, 1.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped to 1, 1");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped to 1, 1");
	    System.exit(1);
	}

	System.out.println("when spline for y3a clamped to 1.0 (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));

	double[] y4 = new double[31];
	for (int i = 0; i < 31; i++) {
	    y4[i] = quad(10.0 + (double) i);
	}
	spline = new CubicSpline1(y4, 10.0, 1.0);

	double max = 0.0;
	for (int i = 0; i <= 300 ; i++) {
	    double u = 10.0 + i/10.0;
	    if (u > 10.0 + 30) break;
	    
	    double v = Math.abs((spline.valueAt(u) - quad(u))/quad(u));
	    if (v > max) max = v;
	}
	System.out.println("max fractional error = " + max);
 
	spline = new CubicSpline1(y3a, 10.0, 10.0);
	
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
	spline = new CubicSpline1(y5, 10.0, 10.0);
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
	for (int i = 0; i <= 36; i++) {
	    x = Math.toRadians(i*10);
	    y6[i] = Math.sin(x);
	    y6p[i] = Math.cos(x);
	}
	spline = new CubicSpline1(y6, 0., Math.toRadians(10.0),
				  CubicSpline.Mode.HERMITE, y6p);
	
	System.out.println("spline(0.0) = " + spline.valueAt(0.0)
			   + ", deriv = " + spline.derivAt(0.0));
	System.out.println("spline(pi/4) = " + spline.valueAt(Math.PI/4.0)
			   + ", deriv = " + spline.derivAt(Math.PI/4.0));

	System.out.println("spline(pi/2) = " + spline.valueAt(Math.PI/2.0)
			   + ", deriv = " + spline.derivAt(Math.PI/2.0));

	double[] y7 = new double[361];
	double[] y7p = new double[361];
	for (int i = 0; i <= 360; i++) {
	    x = Math.toRadians(i*1.0);
	    y7[i] = Math.sin(x);
	    y7p[i] = Math.cos(x);
	}
	spline = new CubicSpline1(y7, 0.0, Math.toRadians(1.0),
				  CubicSpline.Mode.HERMITE, y7p);
	System.out.println("now space points by 1 degree:");
	System.out.println("spline(0.0) = " + spline.valueAt(0.0)
			   + ", deriv = " + spline.derivAt(0.0));
	System.out.println("spline(pi/4) = " + spline.valueAt(Math.PI/4.0)
			   + ", deriv = " + spline.derivAt(Math.PI/4.0));

	System.out.println("spline(pi/2) = " + spline.valueAt(Math.PI/2.0)
			   + ", deriv = " + spline.derivAt(Math.PI/2.0));
 
	double[] y8 = new double[100];
	double[] yp8 = new double[100];
	for (int i = 0; i < 100; i++) {
	    x = i/10.0;
	    y8[i] = f(x);
	    yp8[i] = fp(x);
	}

        spline = new CubicSpline1(y8, 0.0, 0.1,
				  CubicSpline.Mode.HERMITE,
				  yp8);
	if (spline.verify(1.e-10)) {
	    System.out.println("Hermite spline verification for y8 ok");
	} else {
	    System.out.println("Hermite spline verification for y8 failed");
	    System.exit(1);
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

	double args[] = {0.0, Math.PI/6, Math.PI/3, Math.PI/2,
			 4*Math.PI/6, 5*Math.PI/6, Math.PI};

	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double x) {return Math.sin(x);}
		public double derivAt(double x) {return Math.cos(x);}
	    };

	int errcount = 0;
	System.out.println("test splines created with a RealValuedFunction");
	System.out.println("... new CubicSpline1(f, 0.0, Math.PI, 10)");
	spline = new CubicSpline1(f, 0.0, Math.PI, 10);
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
	    System.out.println("... new CubicSpline1(f, 0.0, Math.PI, 10, "
			       + mode + ")");
	    spline = new CubicSpline1(f, 0.0, Math.PI, 10, mode);
	    double err = 1.e-4;
	    switch (mode) {
	    case PARABOLIC_RUNOUT:
	    case PARABOLIC_RUNOUT_START:
	    case PARABOLIC_RUNOUT_END:
	    case CLAMPED_START_PARABOLIC_END:
	    case PARABOLIC_START_CLAMPED_END:
		err = 5.0e-4;
		break;
	    default:
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
		System.out.println("... new CubicSpline1(f, 0.0, Math.PI, 10, "
				   + mode + ", " + f.derivAt(0.0)
				   + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicSpline1(f, 0.0, Math.PI, 10, mode,
					  f.derivAt(0.0), f.derivAt(Math.PI));
		break;
	    case CLAMPED_START:
	    case CLAMPED_START_CUBIC_END:
	    case CLAMPED_START_PARABOLIC_END:
		System.out.println("... new CubicSpline1(f, 0.0, Math.PI, 10, "
				   + mode + ", " + f.derivAt(0.0) + ")");
		spline = new CubicSpline1(f, 0.0, Math.PI, 10, mode,
					  f.derivAt(0.0));
		break;
	    case CLAMPED_END:
	    case PARABOLIC_START_CLAMPED_END:
	    case CUBIC_START_CLAMPED_END:
		System.out.println("... new CubicSpline1(f, 0.0, Math.PI, 10, "
				   + mode + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicSpline1(f, 0.0, Math.PI, 10, mode,
					  f.derivAt(Math.PI));
		break;
	    default:
		continue;
	    }

	    double err = 1.e-4;
	    switch (mode) {
	    case PARABOLIC_RUNOUT:
	    case PARABOLIC_RUNOUT_START:
	    case PARABOLIC_RUNOUT_END:
	    case CLAMPED_START_PARABOLIC_END:
	    case PARABOLIC_START_CLAMPED_END:
		err = 5.0e-4;
		break;
	    default:
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


	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	}

	System.out.println("... OK");
	System.exit(0);
    }
}
