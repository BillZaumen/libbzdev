import org.bzdev.math.BicubicInterpolator;
import org.bzdev.math.LUDecomp;
import org.bzdev.math.Functions;

public class BicubicTest {

    static double f(double x,double y) {return Math.sin(x) * Math.cos(y);}
    static double f1(double x,double y) {return Math.cos(x) * Math.cos(y);}
    static double f2(double x, double y) {return -Math.sin(x) * Math.sin(y);}
    static double f11(double x, double y) {return -Math.sin(x) * Math.cos(y);}
    static double f12(double x, double y) {return -Math.cos(x) * Math.sin(y);}
    static double f21(double x, double y) {return -Math.cos(x) * Math.sin(y);}
    static double f22(double x, double y) {return -Math.sin(x) * Math.cos(y);}

    private static final double  matrix[][] = {
	{ 1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{-3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},

	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	 -3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0},

	{-3.0,  0.0,  3.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	 -2.0,  0.0, -1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0, -3.0,  0.0,  3.0,  0.0,
	  0.0,  0.0,  0.0,  0.0, -2.0,  0.0, -1.0,  0.0},
	{ 9.0, -9.0, -9.0,  9.0,  6.0,  3.0, -6.0, -3.0,
	  6.0, -6.0,  3.0, -3.0,  4.0,  2.0,  2.0,  1.0},
	{-6.0,  6.0,  6.0, -6.0, -3.0, -3.0,  3.0,  3.0,
	 -4.0,  4.0, -2.0,  2.0, -2.0, -2.0, -1.0, -1.0},

	{ 2.0,  0.0, -2.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  2.0,  0.0, -2.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  1.0,  0.0},
	{-6.0,  6.0,  6.0, -6.0, -4.0, -2.0,  4.0, 2.0,
	 -3.0,  3.0, -3.0,  3.0, -2.0, -1.0, -2.0, -1.0},
	{ 4.0, -4.0, -4.0,  4.0,  2.0,  2.0, -2.0, -2.0,
	  2.0, -2.0,  2.0, -2.0,  1.0,  1.0,  1.0,  1.0}
    };

    static String terms[] = {"a00", "a10", "a20", "a30",
			   "a01", "a11", "a21", "a31",
			   "a02", "a12", "a22", "a32",
			   "a03", "a13", "a23", "a33"};
    static String fs[] = {"f(0,0)", "f(1,0)", "f(0,1)", "f(1,1)",
			  "f1(0,0)", "f1(1,0)", "f1(0,1)", "f1(1,1)",
			  "f2(0,0)", "f2(1,0)", "f2(0,1)", "f2(1,1)",
			  "f12(0,0)", "f12(1,0)", "f12(0,1)", "f12(1,1)"};


    public static void main(String argv[]) throws Exception {

	int errcount = 0;

	if (false) {
	    // matrix test. This is not normally needed. The matrix
	    // was copied by hand from a Wikipedia article. It is the
	    // inverse of a matrix representing a system of linear
	    // equations. The test is primarily for debugging: if the
	    // matrix is wrong, the other tests should fail anyway.
	    System.out.print("First test a copy of the equations that ");
	    System.out.println
		("BicubicInterpolator solves during initialization.");
	    System.out.println
		("These are derived from a matrix by inverting it,");
	    System.out.println("So the equations should match "
			       + "those in the Wikipedia article");
	    System.out.println("(This also crosschecks the Wikipedia article)");

	    for (int i = 0; i < matrix.length; i++) {
		if (matrix[i].length != matrix.length) {
		    errcount++;
		    System.out.println("row " + i + " has the wrong length ("
				       + matrix[i].length + ")");
		}
	    }
	    if (errcount > 0) System.exit(1);
	    LUDecomp lud = new LUDecomp(matrix);
	    double[][] orig = lud.getInverse();
	    for (int i = 0; i < 16; i++) {
		System.out.print(fs[i] + " = 0");
		for (int j = 0; j < 16; j++) {
		    double val = orig[i][j];
		    if (Math.abs(val) > 1.e-10) {
			System.out.format(" + %.1g*%s", val, terms[j]);
		    }
		}
		System.out.println();
	    }
	}
	double inits1[] = {
	    f(0.0, 0.0),
	    f(1.0, 0.0),
	    f(0.0, 1.0),
	    f(1.0, 1.0),
	    f1(0.0, 0.0),
	    f1(1.0, 0.0),
	    f1(0.0, 1.0),
	    f1(1.0, 1.0),
	    f2(0.0, 0.0),
	    f2(1.0, 0.0),
	    f2(0.0, 1.0),
	    f2(1.0, 1.0),
	    f12(0.0, 0.0),
	    f12(1.0, 0.0),
	    f12(0.0, 1.0),
	    f12(1.0, 1.0),
	};
	System.out.println();
	System.out.println("try a case, printing out values at the corners");

	BicubicInterpolator interp1 = new BicubicInterpolator(inits1);

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f(x,y) - interp1.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%d,%d) = %g; interp1 = %g\n",
				      i, j, f(x,y), interp1.valueAt(x,y));
		}
	    }
	}

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f1(x,y) - interp1.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%d,%d) = %g; interp1 = %g\n",
				      i, j, f1(x,y), interp1.deriv1At(x,y));
		}
	    }
	}
	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f2(x,y) - interp1.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%d,%d) = %g; interp1 = %g\n",
				      i, j, f2(x,y), interp1.deriv2At(x,y));
		}
	    }
	}
	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f12(x,y) - interp1.deriv12At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f12(%d,%d) = %g; interp1 = %g\n",
				      i, j, f12(x,y), interp1.deriv12At(x,y));
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("try a case, printing out values at the corners,");
	System.out.println("with interpolation region specified explicitly.");
	double xmin = -0.25;
	double xmax = 0.25;
	double ymin = -0.4;
	double ymax = 0.4;

	double inits2[] = {
	    f(xmin, ymin),
	    f(xmax, ymin),
	    f(xmin, ymax),
	    f(xmax, ymax),
	    f1(xmin, ymin),
	    f1(xmax, ymin),
	    f1(xmin, ymax),
	    f1(xmax, ymax),
	    f2(xmin, ymin),
	    f2(xmax, ymin),
	    f2(xmin, ymax),
	    f2(xmax, ymax),
	    f12(xmin, ymin),
	    f12(xmax, ymin),
	    f12(xmin, ymax),
	    f12(xmax, ymax),
	};

	BicubicInterpolator interp2 = new
	    BicubicInterpolator(xmin, xmax, ymin, ymax, inits2);

	double xs[] = {xmin, xmax};
	double ys[] = {ymin, ymax};
	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f(x,y) - interp2.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%g,%g) = %g; interp2 = %g\n",
				      x, y, f(x,y), interp2.valueAt(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f1(x,y) - interp2.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%g,%g) = %g; interp2 = %g\n",
				      x, y, f1(x,y), interp2.deriv1At(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f2(x,y) - interp2.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%g,%g) = %g; interp2 = %g\n",
				      x, y, f2(x,y), interp2.deriv2At(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f12(x,y) - interp2.deriv12At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f12(%g,%g) = %g; interp2 = %g\n",
				      x, y, f12(x,y), interp2.deriv12At(x,y));
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("compare at some points inside the interpolation "
			   + "region");
	double xis[] = {0.1, 0.2};
	double yis[] = {0.1, 0.2};
	double delta = .007;
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f(x,y) - interp2.valueAt(x,y)) > delta
		    || Math.abs(f(x,y)-interp2.valueAt(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f(x,y),
			 interp1.valueAt(x,y),
			 interp2.valueAt(x,y));
		}
				  
	    }
	}
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f1(x,y) - interp2.deriv1At(x,y)) > delta
		    || Math.abs(f1(x,y)-interp2.deriv1At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f1(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f1(x,y),
			 interp1.deriv1At(x,y), interp2.deriv1At(x,y));
		}
	    }
	}
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f2(x,y) - interp2.deriv2At(x,y)) > delta
		    || Math.abs(f2(x,y)-interp2.deriv2At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f2(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f2(x,y),
			 interp1.deriv2At(x,y), interp2.deriv2At(x,y));
		}
	    }
	}

	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f11(x,y) - interp2.deriv11At(x,y)) > delta
		    || Math.abs(f11(x,y)-interp2.deriv11At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f11(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f11(x,y),
			 interp1.deriv11At(x,y), interp2.deriv11At(x,y));
		}
	    }
	}
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f12(x,y) - interp2.deriv12At(x,y)) > delta
		    || Math.abs(f12(x,y)-interp2.deriv12At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f12(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f12(x,y),
			 interp1.deriv12At(x,y), interp2.deriv12At(x,y));
		}
	    }
	}
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f21(x,y) - interp2.deriv21At(x,y)) > delta
		    || Math.abs(f21(x,y)-interp2.deriv21At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f21(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f21(x,y),
			 interp1.deriv21At(x,y), interp2.deriv21At(x,y));
		}
	    }
	}
	for (double x: xis) {
	    for (double y: yis) {
		if (Math.abs(f22(x,y) - interp2.deriv22At(x,y)) > delta
		    || Math.abs(f22(x,y)-interp2.deriv22At(x,y)) > delta) {
		    errcount++;
		    System.out.format
			("f22(%g,%g) = %g, interp1 = %g, interp2 = %g\n",
			 x, y, f22(x,y),
			 interp1.deriv22At(x,y), interp2.deriv22At(x,y));
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}


	System.out.println("Try short form for initializer");
	/*
	double[] inits3 = new double[13];
	System.arraycopy(inits1, 0, inits3, 0, 13);
	BicubicInterpolator interp3 = new BicubicInterpolator(inits3);

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f(x,y) - interp3.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%d,%d) = %g; interp3 = %g\n",
				      i, j, f(x,y), interp3.valueAt(x,y));
		}
	    }
	}

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f1(x,y) - interp3.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%d,%d) = %g; interp3 = %g\n",
				      i, j, f1(x,y), interp3.deriv1At(x,y));
		}
	    }
	}
	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f2(x,y) - interp3.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%d,%d) = %g; interp3 = %g\n",
				      i, j, f2(x,y), interp3.deriv2At(x,y));
		}
	    }
	}
	for (int i = 0; i <= 0; i++) {
	    double x = i;
	    for (int j = 0; j <= 0; j++) {
		double y = j;
		if (Math.abs(f12(x,y) - interp3.deriv12At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f12(%d,%d) = %g; interp3 = %g\n",
				      i, j, f12(x,y), interp3.deriv12At(x,y));
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("Try short form for initializer without f12(0,0)");
	double[] inits4 = new double[13];
	System.arraycopy(inits1, 0, inits4, 0, 13);
	BicubicInterpolator interp4 = new BicubicInterpolator(inits4);

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f(x,y) - interp4.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%d,%d) = %g; interp4 = %g\n",
				      i, j, f(x,y), interp4.valueAt(x,y));
		}
	    }
	}

	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f1(x,y) - interp4.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%d,%d) = %g; interp4 = %g\n",
				      i, j, f1(x,y), interp4.deriv1At(x,y));
		}
	    }
	}
	for (int i = 0; i <= 1; i++) {
	    double x = i;
	    for (int j = 0; j <= 1; j++) {
		double y = j;
		if (Math.abs(f2(x,y) - interp4.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%d,%d) = %g; interp4 = %g\n",
				      i, j, f2(x,y), interp4.deriv2At(x,y));
		}
	    }
	}
	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("Try short form of the initializer with an explicit"
			   + " interpolation region");
	double inits5[] = new double[13];
	System.arraycopy(inits2, 0, inits5, 0, 13);
	BicubicInterpolator interp5 = new
	    BicubicInterpolator(xmin, xmax, ymin, ymax, inits5);
	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f(x,y) - interp5.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%g,%g) = %g; interp5 = %g\n",
				      x, y, f(x,y), interp5.valueAt(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f1(x,y) - interp5.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%g,%g) = %g; interp5 = %g\n",
				      x, y, f1(x,y), interp5.deriv1At(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f2(x,y) - interp5.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%g,%g) = %g; interp5 = %g\n",
				      x, y, f2(x,y), interp5.deriv2At(x,y));
		}
	    }
	}

	if (Math.abs(f12(xmin,ymin) - interp5.deriv12At(xmin,ymin)) > 1.e-10) {
	    errcount++;
	    System.out.format("f12(%g,%g) = %g; interp5 = %g\n",
			      xmin, ymin, f12(xmin,ymin),
			      interp5.deriv12At(xmin,ymin));
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("Try short form of the initializer with an explicit"
			   + " interpolation region but without "
			   + "f12(xmin,ymin)");
	*/
	double inits6[] = new double[12];
	System.arraycopy(inits2, 0, inits6, 0, 12);
	BicubicInterpolator interp6 = new
	    BicubicInterpolator(xmin, xmax, ymin, ymax, inits6);
	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f(x,y) - interp6.valueAt(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f(%g,%g) = %g; interp6 = %g\n",
				      x, y, f(x,y), interp6.valueAt(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f1(x,y) - interp6.deriv1At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f1(%g,%g) = %g; interp6 = %g\n",
				      x, y, f1(x,y), interp6.deriv1At(x,y));
		}
	    }
	}

	for (double x: xs) {
	    for (double y: ys) {
		if (Math.abs(f2(x,y) - interp6.deriv2At(x,y)) > 1.e-10) {
		    errcount++;
		    System.out.format("f2(%g,%g) = %g; interp6 = %g\n",
				      x, y, f2(x,y), interp6.deriv2At(x,y));
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("compare more points inside "
			   + "the interpolation region");
	double expected = (((f1(xmin,ymax) - f1(xmin, ymin))/(ymax - ymin))
			   + ((f2(xmax, ymin) - f2(xmin,ymin))/(xmax - xmin))
			   ) / 2.0;
	if (Math.abs(expected - interp6.deriv12At(xmin, ymin)) > 1.e-10) {
	    errcount++;
	    System.out.format("f12 at (xmin, ymin) = %g, "
			      + "interp6 = %g, expected %g\n",
			      f12(xmin, ymin), interp6.deriv12At(xmin, ymin),
			      expected);
	}

	BicubicInterpolator interps[] = {/*interp3, interp4, interp5,*/ interp6};
	int index = 6;
	for (BicubicInterpolator interp: interps) {
	    delta = .007;
	    if (index > 5) delta = .04;
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f(x,y)-interp.valueAt(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f(x,y),
			     interp1.valueAt(x,y),
			     index, interp.valueAt(x,y));
		    }
		}
	    }
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f1(x,y) - interp.deriv1At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f1(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f1(x,y),
			     interp1.deriv1At(x,y),
			     index, interp.deriv1At(x,y));
		    }
		}
	    }
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f2(x,y)-interp.deriv2At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f2(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f2(x,y),
			     interp1.deriv2At(x,y),
			     index, interp.deriv2At(x,y));
		    }
		}
	    }

	    delta = 0.04;
	    if (index > 5) delta = 0.16;
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f11(x,y)-interp.deriv11At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f11(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f11(x,y),
			     interp1.deriv11At(x,y),
			     index, interp.deriv11At(x,y));
		    }
		}
	    }
	    if (index > 5) delta = 0.08;
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f12(x,y)-interp.deriv12At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f12(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f12(x,y),
			     interp1.deriv12At(x,y),
			     index, interp.deriv12At(x,y));
		    }
		}
	    }
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f21(x,y)-interp.deriv21At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f21(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f21(x,y),
			     interp1.deriv21At(x,y),
			     index, interp.deriv21At(x,y));
		    }
		}
	    }
	    for (double x: xis) {
		for (double y: yis) {
		    if (Math.abs(f22(x,y)-interp.deriv22At(x,y)) > delta) {
			errcount++;
			System.out.format
			    ("f22(%g,%g) = %g, interp1 = %g, interp%d = %g\n",
			     x, y, f22(x,y),
			     interp1.deriv22At(x,y),
			     index, interp.deriv22At(x,y));
		    }
		}
	    }
	    index++;
	}
	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("check control points");

	double[][] controlPoints = interp1.getControlPoints();

	for (double x: xis) {
	    for (double y: yis) {
		double val1 = interp1.valueAt(x, y);
		double val2 = 0.0;
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 4; j++) {
			val2 += controlPoints[i][j]
			    * Functions.B(i,3,x) * Functions.B(j,3,y);
		    }
		}
		if (Math.abs(val1 - val2) > 1.e-10) {
		    errcount++;
		    System.out.format
			("Bezier patch does not bicubic interpolator: "
			 + "for (%g, %g) val1 = %g, val2 = %g\n",
			 x, y, val1, val2);
		}

	    }
	}

	if (Math.abs(controlPoints[0][0] - f(0.0, 0.0)) > 1.e-10) {
	    errcount++;
	    System.out.format("P[%d][%d]: %g != %g\n",
			      0, 0, controlPoints[0][0], f(0.0, 0.0));
	}
	if (Math.abs(controlPoints[0][3] - f(0.0, 1.0)) > 1.e-10) {
	    errcount++;
	    System.out.format("P[%d][%d]: %g != %g\n",
			      0, 3, controlPoints[0][3], f(0.0, 1.0));
	}
	if (Math.abs(controlPoints[3][0] - f(1.0, 0.0)) > 1.e-10) {
	    errcount++;
	    System.out.format("P[%d][%d]: %g != %g\n",
			      3, 0, controlPoints[3][0], f(1.0, 0.0));
	}
	if (Math.abs(controlPoints[3][3] - f(1.0, 1.0)) > 1.e-10) {
	    errcount++;
	    System.out.format("P[%d][%d]: %g != %g\n",
			      3, 3, controlPoints[3][3], f(1.0, 1.0));
	}
	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("check initialization from control points");
	BicubicInterpolator interp7 = new BicubicInterpolator(controlPoints);
	for (double x: xs) {
	    for (double y: ys) {
		double val1 = interp1.valueAt(x,y);
		double val3 = interp7.valueAt(x,y);
		if (Math.abs(val3 - val1) > 1.e-10) {
		    errcount++;
		    System.out.format
			("at (%g,%g): interp1 = %g, interp7 = %g\n",
			 x, y, val1, val3);
		}
	    }
	}
	if (errcount > 0) {
	    System.out.println("BicubicInterpolator test failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}
    }
}
