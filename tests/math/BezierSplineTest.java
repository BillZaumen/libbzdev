import org.bzdev.math.CubicSpline;
import org.bzdev.math.CubicBezierSpline1;
import org.bzdev.math.GLQuadrature;
import org.bzdev.lang.CallableArgsReturns;
import org.bzdev.math.Functions;
import org.bzdev.math.Functions.Bernstein;
import org.bzdev.math.RealValuedFunction;


public class BezierSplineTest {

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
	CubicSpline spline = new CubicBezierSpline1(y1, 10.0, 10.0);

	System.out.print("coefficients for {1.0, 2.0}: ");
	if (spline instanceof CubicBezierSpline1) {
	    for (double c: ((CubicBezierSpline1) spline)
		     .getBernsteinCoefficients()) {
		System.out.print(" "+ c);
	    }
	    System.out.println();
	}

	int errcount = 0;

	for (int i = 0; i <= 10; i++) {
	    double x = 10.0 + i;
	    double ys = spline.valueAt(x);
	    double yt = 1.0 + (x-10.0)/10;
	    if (Math.abs(yt-ys) > 1.e-10) {
		System.out.println("linear case 1 failed, i = "
				   + i + ", ys = " + ys + ", yt = " +yt);
		errcount++;
	    }
	}
	
	double[] y2 = {1.0, 2.0, 3.0};
	spline = new CubicBezierSpline1(y2, 10.0, 10.0);
	System.out.print("coefficients for {1.0, 2.0, 3.0}: ");
	if (spline instanceof CubicBezierSpline1) {
	    for (double c: ((CubicBezierSpline1) spline)
		     .getBernsteinCoefficients()) {
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
		errcount++;
	    }
	}

	double[] y3 = {quad(10.0), quad(20.0), quad(30.0)};
	spline = new CubicBezierSpline1(y3, 10.0, 10.0,
					CubicSpline.Mode.QUAD_FIT);
	System.out.format("coefficients (quad case) {%g, %g, %g}: ",
			  y3[0], y3[1], y3[2]);
	if (spline instanceof CubicBezierSpline1) {
	    for (double c: ((CubicBezierSpline1) spline)
		     .getBernsteinCoefficients()) {
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
		errcount++;
	    }
	}
	

	double[] y3a = {quad(10.0), quad(20.0), quad(30.0), quad(40.0)};
	spline = new CubicBezierSpline1(y3a, 10.0, 10.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK");
	else {
	    System.out.println("spline for y3a verification failed");
	    errcount++;
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
	if (spline instanceof CubicBezierSpline1) {
	    coefficients = ((CubicBezierSpline1) spline)
		.getBernsteinCoefficients();
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


	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.PARABOLIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parbolic start]");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.PARABOLIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parabolic end]");
	    errcount++;
	}


	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.PARABOLIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "parabolic runout");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "parabolic runout");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CUBIC_RUNOUT_START);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[parbolic start]");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CUBIC_RUNOUT_END);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "[cubic end]");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CUBIC_RUNOUT);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "cubic runout");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "cubic runout");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CLAMPED_START, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped start");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped start");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CLAMPED_END, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped end");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped end");
	    errcount++;
	}

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CLAMPED, 0.0, 0.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped");
	    errcount++;
	}

	System.out.println("when spline for y3a clamped to zero (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));

	spline = new CubicBezierSpline1
	    (y3a, 10.0, 10.0, CubicSpline.Mode.CLAMPED, 1.0, 1.0);
	if (spline.verify(1.e-10)) System.out.println("spline for y3a OK - "
						      + "clamped to 1, 1");
	else {
	    System.out.println("spline for y3a verification failed "
			       + "clamped to 1, 1");
	    errcount++;
	}

	System.out.println("when spline for y3a clamped to 1.0 (both ends), "
			   + "derivatives are " + spline.derivAt(10.0) + ", "
			   + spline.derivAt(40.0));

	double[] y4 = new double[31];
	for (int i = 0; i < 31; i++) {
	    y4[i] = quad(10.0 + (double) i);
	}
	spline = new CubicBezierSpline1(y4, 10.0, 1.0); 
	double max = 0.0;
	for (int i = 0; i <= 300 ; i++) {
	    double u = 10.0 + i/10.0;
	    if (u > 10.0 + 30) break;
	    
	    double v = Math.abs((spline.valueAt(u) - quad(u))/quad(u));
	    if (v > max) max = v;
	}
	System.out.println("max fractional error = " + max);
 
	spline = new CubicBezierSpline1(y3a, 10.0, 10.0); 
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
		System.out.println("error occured when i = " + i
				   + ", xx = " +xx + ", y = "
				   + y);
		e.printStackTrace();
		System.exit(1);
	    }
	}
	System.out.println("y3a inverse error = " + max);
	System.out.println("-----------");			 
	double[] y5 = {quad(40.0), quad(30.0), quad(20.0), quad(10.0)};
	spline = new CubicBezierSpline1(y5, 10.0, 10.0); max = 0.0;
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
	spline = new CubicBezierSpline1
	    (y6, 0., Math.toRadians(10.0), CubicSpline.Mode.HERMITE, y6p);
	
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
	spline = new CubicBezierSpline1
	    (y7, 0.0, Math.toRadians(1.0), CubicSpline.Mode.HERMITE, y7p);
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

        spline = new CubicBezierSpline1
	    (y8, 0.0, 0.1, CubicSpline.Mode.HERMITE, yp8);
	if (spline.verify(1.e-10)) {
	    System.out.println("Hermite spline verification for y8 ok");
	} else {
	    System.out.println("Hermite spline verification for y8 failed");
	    errcount++;
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

	System.out.println("test splines created with a RealValuedFunction");
	System.out.println("... new CubicBezierSpline1(f, 0.0, Math.PI, 10)");
	spline = new CubicBezierSpline1
	    (f, 0.0, Math.PI, 10); for (double xx: args) {
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
	    System.out.println
		("... new CubicBezierSpline1(f, 0.0, Math.PI, 10, "
		 + mode + ")");
	    spline = new CubicBezierSpline1(f, 0.0, Math.PI, 10, mode);
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
		System.out.println
		    ("... new CubicBezierSpline1(f, 0.0, Math.PI, 10, "
		     + mode + ", " + f.derivAt(0.0)
		     + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicBezierSpline1
		    (f, 0.0, Math.PI, 10, mode,
		     f.derivAt(0.0), f.derivAt(Math.PI));
		break;
	    case CLAMPED_START:
	    case CLAMPED_START_CUBIC_END:
	    case CLAMPED_START_PARABOLIC_END:
		System.out.println
		    ("... new CubicBezierSpline1(f, 0.0, Math.PI, 10, "
		     + mode + ", " + f.derivAt(0.0) + ")");
		spline = new CubicBezierSpline1
		    (f, 0.0, Math.PI, 10, mode, f.derivAt(0.0));
		break;
	    case CLAMPED_END:
	    case PARABOLIC_START_CLAMPED_END:
	    case CUBIC_START_CLAMPED_END:
		System.out.println
		    ("... new CubicBezierSpline1(f, 0.0, Math.PI, 10, "
		     + mode + ", " + f.derivAt(Math.PI) + ")");
		spline = new CubicBezierSpline1
		    (f, 0.0, Math.PI, 10, mode, f.derivAt(Math.PI));
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

	// test values and derivatives

	double[] sinvals = new double[37];
	for (int i = 0; i < 37; i++) {
	    double theta = Math.toRadians((double)(i*10));
	    sinvals[i] = Math.sin(theta);
	}
	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0));
	CubicBezierSpline1 spline2
	    = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals);

	double error = 1.e-4;
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (Math.abs(spline.valueAt(theta) - Math.sin(theta)) > error) {
		System.out.println("bad value for sinvals test");
		errcount++;
	    }
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	    if (Math.abs(spline.derivAt(theta) - Math.cos(theta)) > error) {
		System.out.println("bad deriv for sinvals test");
		errcount++;
	    }
	    if (Math.abs(spline.secondDerivAt(theta) + Math.sin(theta))
		> error*30) {
		System.out.println("bad 2nd deriv for sinvals test: "
				   + spline.secondDerivAt(theta) + ", "
				   + -Math.sin(theta));
		errcount++;
	    }
	}

	double darray0[] = new double[0];
	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.NATURAL,
					darray0);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					darray0);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED_START,
					1.0);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					 1.0);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	double darray1[] = {1.0};
	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED_START,
					darray1);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					darray1);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED_END,
					2.0);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					  2.0);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	double darray2[] = {2.0};
	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED_START,
					darray2);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					darray2);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED,
					1.0, 2.0);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					 1.0, 2.0);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
	    }
	}

	double darray3[] = {1.0, 2.0};
	spline = new CubicBezierSpline1(sinvals, 0.0, Math.toRadians(10.0),
					CubicSpline.Mode.CLAMPED,
					darray3);
	spline2 = new CubicBezierSpline1((CubicBezierSpline1) spline, sinvals,
					darray3);
	for (int i = 30; i < 300; i++) {
	    double theta = Math.toRadians((double)i);
	    if (spline.valueAt(theta) != spline2.valueAt(theta)) {
		throw new Exception("spline != spline1");
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
