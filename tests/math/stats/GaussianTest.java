import org.bzdev.math.stats.*;

public class GaussianTest {
    static double[][] tests = {
	{-10.0, 7.694598626706419346339E-23, 7.619853024160526065973E-24, 1.0},
	{-5.0, 1.486719514734297707908E-6, 2.866515718791939116738E-7,
	 0.9999997133484281208061},
	{-1.0, 0.2419707245191433497978, 0.1586552539314570514148,
	 0.8413447460685429485852},
	{0.0, 0.3989422804014326779399,	0.5, 0.5},
	{1.0, 0.2419707245191433497978, 0.8413447460685429485852,
	 0.1586552539314570514148},
	{5.0, 1.486719514734297707908E-6, 0.9999997133484281208061,
	 2.866515718791939116738E-7},
	{10.0, 7.694598626706419346339E-23, 1, 7.619853024160526065973E-24}
    };

    public static void main(String argv[]) throws Exception {

	int errcount = 0;
	double limit = 1.e-12;
	for (int i = 0; i < tests.length; i++) {
	    double x = tests[i][0];
	    double xf = tests[i][1];
	    double xP = tests[i][2];
	    double xQ = tests[i][3];
	    double f = GaussianDistr.pd(x, 0.0, 1.0);
	    double p = GaussianDistr.P(x, 0.0, 1.0);
	    double q = GaussianDistr.Q(x, 0.0, 1.0);
	    double a = GaussianDistr.A(x, 0.0, 1.0);
	    
	    if (Math.abs((xf - f)/xf) > limit) {
		System.out.format("f(%g): %s != %s\n", x, f, xf);
		errcount++;
	    }
	    if (Math.abs((xP - p)/xP) > limit) {
		System.out.format("P(%g): %s != %s\n", x, p, xP);
		errcount++;
	    }
	    if (Math.abs((xQ - q)/xQ) > limit) {
		System.out.format("Q(%g): %s != %s\n", x, q, xQ);
		errcount++;
	    }
	    if (Math.abs((p+q) - 1.0) > limit) {
		System.out.format("P(x) + Q(x) = \n", x, x, p+q);
		errcount++;
	    }
	    if (Math.abs(a - (2.0*p - 1)) > limit) {
		System.out.format("A(%g) = %s, expecting %s\n",
				  x, a, (2.0*p - 1));
		errcount++;
	    }
	}

	if (Math.abs(GaussianDistr.pd(1.0, 1.0, 2.0) - 0.19947114020071633897)
	    > 1.e-12) throw new Exception();
	if (Math.abs(GaussianDistr.pd(0.0, 1.0, 2.0) - 0.1760326633821497388873)
	    > 1.e-12) throw new Exception();

	GaussianDistr gd = new GaussianDistr(1.0, 2.0);
	for (int i = 0; i < 20; i++) {
	    double x = i/10.0;
	    if (gd.P(x) != GaussianDistr.P(x, 1.0, 2.0)) {
		System.out.format("gd.P(%g) failed\n", x);
		errcount++;
	    }
	    if (gd.Q(x) != GaussianDistr.Q(x, 1.0, 2.0)) {
		System.out.format("gd.Q(%g) failed (%s != %s)\n", x,
				  gd.Q(x), GaussianDistr.Q(x, 1.0, 2.0));
		errcount++;
	    }
	    if (gd.pd(x) != GaussianDistr.pd(x, 1.0, 2.0)) {
		System.out.format("gd.pd(%g) failed\n", x);
		errcount++;
	    }
	    if (gd.A(x) != GaussianDistr.A(x, 1.0, 2.0)) {
		System.out.format("gd.A(%g) failed (%s != %s)\n", x,
				  gd.A(x), GaussianDistr.A(x, 1.0, 2.0));
		errcount++;
	    }
	}

	if (errcount > 0) throw new Exception("test failed");

    }
}
