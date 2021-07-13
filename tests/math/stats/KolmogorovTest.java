import org.bzdev.math.stats.*;

public class KolmogorovTest {

    static double ourP(double x, int n) {
	if (x == 0.0) return 0.0;
	double xsq = x*x * n;
	x *= Math.sqrt((double)n);
	double sum = 0.0;
	int factor = -1;
	double limit = x*1.e-10;
	double term = 1.0;
	int i = 0;
	while (Math.abs(term) > limit) {
	    i++;
	    factor *= -1;
	    term = factor * Math.exp(-2.0*i*i*xsq);
	    sum += term;
	}
	return 1.0 - 2.0*sum;
    }

    public static void main(String argv[]) {

	if (argv.length == 2) {
	    double d = Double.parseDouble(argv[0]);
	    int n = Integer.parseInt(argv[1]);
	    System.out.format("P(%g,%d) = %s\n", d, n,
			      KDistr.P(d, n));
	    System.exit(0);
	}

	int errcount = 0;

	boolean printit = false;
	printit = true;

	System.out.println("PL(10.0, 1) = " + KDistr.PL(10.0, 1));
	System.out.println("ourP(10.0) = " + ourP(10.0, 1));
	System.out.println("PL(0.49, 10) = " + KDistr.PL(0.49, 10));
	System.out.println("P(0.49, 10) = " + KDistr.P(0.49, 10));
	System.out.println("ourP(0.49, 10) = " + ourP(0.49, 10));


	double delta = 1.e-5;

	for (int i = 0; i < 100; i++) {
	    double x = i / 10.0;
	    double value = KDistr.PL(x, 1);
	    double expected = ourP(x, 1);
	    if (Math.abs(value - expected) > 1.e-10) {
		errcount++;
		System.out.format("P(%g,1) = %s, expected %s\n",
				  x, value, expected);
	    }
	    double value2 = KDistr.PL(x+delta, 1);
	    double pd = KDistr.pdL(x, 1);
	    expected = (value2 - value)/delta;
	    if(Math.abs(pd - expected) > delta*10) {
		errcount++;
		System.out.format("pd(%g,1) = %s, expected %s\n",
				  x, pd, expected);
	    }
	}
	delta = 1.e-7;
	for (int i = 0; i < 100; i++) {
	    double x = i / 10.0;
	    double value = KDistr.PL(x, 2);
	    double expected = ourP(x, 2);
	    if (Math.abs(value - expected) > 1.e-10) {
		errcount++;
		System.out.format("P(%g,2) = %s, expected %s\n",
				  x, value, expected);
	    }
	    double value2 = KDistr.PL(x+delta, 2);
	    double pd = KDistr.pdL(x, 2);
	    expected = (value2 - value)/delta;
	    if(Math.abs(pd - expected) > delta*10) {
		errcount++;
		System.out.format("pd(%g,2) = %s, expected %s\n",
				  x, pd, expected);
	    }
	}

	double[][] case10 = {{0.49, 0.01},
			     {0.410, 0.05},
			     {0.368, 0.1},
			     {0.342, 0.15},
			     {0.322, 0.2}};

	for (double[] parms: case10) {
	    double d = parms[0];
	    double expected = parms[1];
	    double value = KDistr.Q(d, 10);
	    if (Math.abs(value-expected) > 0.01) {
		System.out.format("Q(%g,%d) = %s, expecting %g\n",
				  d, 10, value, expected);
	    }
	}

	KDistr kd = new KDistr(10);
	for (double[] parms: case10) {
	    double d = parms[0];
	    if (kd.P(d) != KDistr.P(d, 10)
		|| kd.Q(d) != KDistr.Q(d, 10)) {
		System.out.println("kd failed, d = " + d);
		errcount++;
	    }
	    
	}

	if (false) {
	    // use this for timing estimates.
	    System.out.println("trying for large values of n");
	    for (int n = 1000; n < 20000; n += 1000) {
		double x = 0.49*Math.sqrt(10)/Math.sqrt((double)n);
		double value = KDistr.P(x, n);
		double valueL = KDistr.PL(x, n);
		System.out.format("... P(%g,%d) = %s, P-PL= %s\n", x, n, value,
				  value-valueL);
	    }

	    for (int n = 1000; n < 20000; n += 1000) {
		double x = 2.0*0.49*Math.sqrt(10)/Math.sqrt((double)n);
		double value = KDistr.P(x, n);
		double valueL = KDistr.PL(x, n);
		System.out.format("... P(%g,%d) = %s, P-PL= %s\n", x, n, value,
				  value-valueL);
	    }
	}

	for (int i = 0; i < 1001; i++) {
	    double x = i/1000.0;
	    double value = kd.P(kd.inverseP(x));
	    if (Math.abs(x - value) > 1.e-9) {
		System.out.format("kd.P(kd.inverseP(%g)) = %s\n", x, value);
		errcount++;
	    }
	}

	for (int i = 0; i < 1001; i++) {
	    double x = i/1000.0;
	    double inverse = kd.inverseQ(x);
	    double value = kd.Q(inverse);
	    if (Math.abs(x - value) > 1.e-9) {
		System.out.format("kd.Q(kd.inverseQ(%g)) = %s\n", x, value);
		errcount++;
	    }
	}

	if (false) {
	    double max = 0;
	    for (int n = 100; n < 4000; n += 100) {
		double x = (1.0/8.0)/Math.sqrt((double)n);
		int count = 0;
		while (count++ < 200) {
		    long start = System.nanoTime();
		    double q = KDistr.Q(x, n);
		    if (q < 1.e-10) break;
		    double diff = KDistr.QL(x, n) - q;
		    if (Math.abs(diff/q) > 0.0001) {
			/*
			  System.out.format("x=%g, n=%d, diff/q = %g, "
			  + "q = %g, n*x=%g\n",
			  x, n, diff/q, q, x*n);
			*/
			if (x*n > max) max = x*n;
		    }
		    long end = System.nanoTime();
		    // if ((end - start) > 10000000L) break;
		    x *= 1.2;
		}
	    }
	    System.out.println("max = " + max);
	}
	if (errcount > 0) {
	    System.exit(1);
	}
	System.exit(0);
    }
}
