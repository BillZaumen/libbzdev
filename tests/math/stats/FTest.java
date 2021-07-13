import org.bzdev.math.stats.*;
import org.bzdev.lang.MathOps;
import org.bzdev.math.Functions;

public class FTest {
    static class TestData {
	double f;
	int nu1;
	int nu2;
	double lambda;
	double pd;
	double P;
	double Q;
	    
	TestData(double f, int nu1, int nu2, double pd, double P, double Q) {
	    this.f= f;
	    this.nu1 = nu1;
	    this.nu2 = nu2;
	    this.pd = pd;
	    this.P = P;
	    this.Q = Q;
	}

	TestData(double f, int nu1, int nu2, double lambda,
		 double pd, double P, double Q) {
	    this.f= f;
	    this.nu1 = nu1;
	    this.nu2 = nu2;
	    this.lambda = lambda;
	    this.pd = pd;
	    this.P = P;
	    this.Q = Q;
	}

    }

    public static double pdCas(double f, long nu1, long nu2, double lambda)
    {
	if (f == 0.0) {
	    if (nu1 == 1) {
		return Double.POSITIVE_INFINITY;
	    } else if (nu1 == 2) {
		return Math.exp(lambda/2);
	    } else {
		return 0.0;
	    }
	}
	double hlambda = lambda/2;
	double init = Math.exp(-hlambda);
	double sum = init * Math.sqrt(Math.pow(nu1*f,nu1)
				      * Math.pow((double)nu2,nu2)
				      / Math.pow(nu1*f + nu2, nu1+nu2))
	    / (f * Functions.Beta(nu1/2.0, nu2/2.0));
	double term = init;
	int j = 1;
	do {
	    term *= hlambda / j;
	    long nu1p2j = nu1 + 2*j;
	    double pd = Math.sqrt(Math.pow(nu1p2j*f,nu1p2j)
				  * MathOps.pow((double)nu2,nu2)
			  / Math.pow(nu1p2j*f + nu2, nu1p2j+nu2))
		/ (f * Functions.Beta(nu1p2j/2.0, nu2/2.0));
	    sum += term * pd;
	    j++;
	} while( term/sum > 1.e-15);
	return sum;
    }

    public static void main(String argv[]) {

	int errcount = 0;

	double delta = 0.00001;

	// test data from http://keisan.casio.com/exec/system/1180573185
	TestData[] test = {
	    new TestData(1.5, 2, 3,
			 0.1767766952966368811,
			 0.6464466094067262378,
			 0.3535533905932737622),
	    new TestData(1.7, 3, 3,
			 0.168683763703431347431,
			 0.663181488122757192756,
			 0.336818511877242807244),
	    new TestData(0.5, 1, 1,
			 0.3001054387190353565184,
			 0.391826552030607270171,
			 0.608173447969392729829),
	    new TestData(0.0, 7, 8,
			 0.0, 0.0, 1.0),
	    new TestData(20.0, 7, 8,
			 3.29777153923870776441E-5,
			 0.9998206374739741145654,
			 1.79362526025885434636E-4),
	    new TestData(40.0, 7, 8,
			 1.26565331806019235966E-6,
			 0.9999867945469482326214,
			 1.32054530517673785532E-5)};

	for (TestData tdata: test) {
	    double f = tdata.f;
	    int nu1 = tdata.nu1;
	    int nu2 = tdata.nu2;
	    double pd = FDistr.pd(f, nu1, nu2);
	    double P = FDistr.P(f, nu1, nu2);
	    double Q = FDistr.Q(f, nu1, nu2);

	    if (Math.abs(pd - tdata.pd) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d: ",
				  f, nu1, nu2);
		System.out.println("pd = " + pd + ", tdata.pd = "
				   + tdata.pd);
		errcount++;
	    }
	    if (Math.abs(P - tdata.P) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d: ",
				  f, nu1, nu2);
		System.out.println("P = " + P + ", tdata.P = " + tdata.P);
		errcount++;
	    }
	    if (Math.abs(Q - tdata.Q) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d: ",
				  f, nu1, nu2);
		System.out.println("Q = " + Q + ", tdata.Q = " + tdata.Q);
		errcount++;
	    }
	    if (Math.abs(P + Q - 1.0) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, ",
				  f, nu1, nu2);
		System.out.println("P = " + P + ", Q = " + Q);
		errcount++;
	    }

	    double P2 = FDistr.P(f+delta, nu1, nu2);
	    double epd = (P2 - P)/delta;
	    if (Math.abs(epd - pd) > 1.e-4) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, ",
				  f, nu1, nu2);
		System.out.println("pd = " + pd + ", epd = " + epd);
		errcount++;
	    }
	}

	// test data from http://keisan.casio.com/exec/system/1180573165
	TestData nctest[] = {
	    new TestData(1.5, 2, 3, 2.3,
			 0.191248271031184193579,
			 0.39989107486236300713,
			 0.60010892513763699287),
	    new TestData(1.7, 3, 3, 2.3,
			 0.195544089988270997996,
			 0.472487292474888253426,
			 0.527512707525111746574),
	    new TestData(0.5, 1, 1, 5.1,
			 0.09579533775731832138936,
			 0.057126246914457143683,
			 0.94287375308554285631),
	    new TestData(0.0, 7, 8, 5.3,
			 0.0, 0.0, 1.0),
	    new TestData(20.0, 7, 8, 2.4,
			 8.8351079490684123938E-5,
			 0.999508027566075230129,
			 4.91972433924769871097E-4),
	    new TestData(40.0, 7, 8, 2.5,
			 3.71416745637755561763E-6,
			 0.9999607667937778503517,
			 3.92332062221496483389E-5)};

	for (TestData ncdata: nctest) {
	    double f = ncdata.f;
	    int nu1 = ncdata.nu1;
	    int nu2 = ncdata.nu2;
	    double lambda = ncdata.lambda;
	    double pd = FDistr.pd(f, nu1, nu2, lambda);
	    double P = FDistr.P(f, nu1, nu2, lambda);
	    double Q = FDistr.Q(f, nu1, nu2, lambda);

	    if (Math.abs(pd - pdCas(f, nu1, nu2, lambda)) > 1.e-10) {
		System.out.println("pd = " + pd
				   + ", pdCas = " 
				   + pdCas(f, nu1, nu2, lambda));
	    }

	    if (Math.abs(pd - ncdata.pd) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, "
				  + "lambda = %g: ",
				  f, nu1, nu2, lambda);
		System.out.println("pd = " + pd + ", ncdata.pd = "
				   + ncdata.pd);
		errcount++;
	    }
	    if (Math.abs(P - ncdata.P) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, "
				  + "lambda = %g: ",
				  f, nu1, nu2, lambda);
		System.out.println("P = " + P + ", ncdata.P = " + ncdata.P);
		errcount++;
	    }
	    if (Math.abs(Q - ncdata.Q) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, "
				  + "lambda = %g: ",
				  f, nu1, nu2, lambda);
		System.out.println("Q = " + Q + ", ncdata.Q = " + ncdata.Q);
		errcount++;
	    }
	    if (Math.abs(P + Q - 1.0) > 1.e-10) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, "
				  + "lambda = %g: ",
				  f, nu1, nu2, lambda);
		System.out.println("P = " + P + ", Q = " + Q);
		errcount++;
	    }

	    double P2 = FDistr.P(f+delta, nu1, nu2, lambda);
	    double epd = (P2 - P)/delta;
	    if (Math.abs(epd - pd) > 1.e-4) {
		System.out.format("f = %g, nu1 = %d, nu2 = %d, "
				  + "lambda = %g: ",
				  f, nu1, nu2, lambda);
		System.out.println("pd = " + pd + ", epd = " + epd);
		errcount++;
	    }
	}


	if (errcount > 0) {
	    System.exit(1);
	}

	// test data from 
	// https://www.wavemetrics.com/products/igorpro/dataanalysis/statistics/tests/statistics_pxp30.htm
	double[][] ldata = {
	    {
		44.43,
		47.34,
		55.17,
		62.91,
		49.28,
		47.01,
		43.15,
		64.85
	    },
	    {
		26.84,
		16.93,
		30.02,
		27.21,
		24.86,
		27.61,
		23.62,
		17.3
	    },
	    {
		49.56,
		33.51,
		37.66,
		49.57,
		38.06,
		50.09
	    },
	    {
		44.48,
		37.59,
		38.68,
		48.1,
		44.23,
		44.43,
		36.02,
		34.62
	    }};

	double[][] ldata2 = {
	    {
		44.43,
		47.34,
		55.17,
		62.91,
		49.28,
		47.01,
		43.15,
		64.85
	    },
	    {
		26.84,
		16.93,
		30.02,
		27.21,
		24.86,
		27.61,
		23.62,
		17.3
	    },
	    {
		49.56,
		33.51,
		37.66,
		49.57,
		38.06,
		50.09
	    },
	    {46.25,
	     46.25,
	     38.65,
	     56.82,
	     69.51,
	     62.65,
	     28.6,
	     28.92,
	    }
	};

	for (int i = 0; i < ldata.length; i++) {
	    System.out.println("data set " + i + ":");
	    System.out.println("    mean = " + BasicStats.mean(ldata[i]));
	    System.out.println("    median = " + BasicStats.median(ldata[i]));
	    System.out.println("    trimmed = "
			       + BasicStats.trimmedMean(10, ldata[i]));
	}


	LeveneStat lstat = new LeveneStat(ldata);
	int dof1 = (int) lstat.getDegreesOfFreedom1();
	int dof2 = (int) lstat.getDegreesOfFreedom2();
	if (dof1 != 3) {
	    System.out.println("degrees of freedom k-1: " + dof1);
	    errcount++;
	}
	if (dof2 != 26) {
	    System.out.println("degrees of freedom N-k: " + dof2);
	    errcount++;
	}
	double value = lstat.getValue();
	if (Math.abs(value - 2.77329) > 1.e-5) {
	    System.out.println("lstat = " + value);
	    errcount++;
	}
	double cv = lstat.getCriticalValue
	    (Statistic.PValueMode.POSITIVE_SIDE, 0.05);
	if (Math.abs(cv - 2.97515) > 1.e-5) {
	    System.out.println("critical value = " + cv);
	    errcount++;
	}

	lstat = new LeveneStat(LeveneStat.Mode.MEAN, ldata2);
	dof1 = (int) lstat.getDegreesOfFreedom1();
	dof2 = (int) lstat.getDegreesOfFreedom2();
	if (dof1 != 3) {
	    System.out.println("(mean) degrees of freedom k-1: " + dof1);
	    errcount++;
	}
	if (dof2 != 26) {
	    System.out.println("(mean) degrees of freedom N-k: " + dof2);
	    errcount++;
	}
	value = lstat.getValue();
	if (Math.abs(value - 3.7164) > 1.e-5) {
	    System.out.println("(mean) lstat = " + value);
	    errcount++;
	}
	cv = lstat.getCriticalValue(Statistic.PValueMode.POSITIVE_SIDE, 0.05);
	if (Math.abs(cv - 2.97515) > 1.e-5) {
	    System.out.println("(mean) critical value = " + cv);
	    errcount++;
	}
	
	lstat = new LeveneStat(LeveneStat.Mode.MEDIAN, ldata2);
	dof1 = (int) lstat.getDegreesOfFreedom1();
	dof2 = (int) lstat.getDegreesOfFreedom2();
	if (dof1 != 3) {
	    System.out.println("(median) degrees of freedom k-1: " + dof1);
	    errcount++;
	}
	if (dof2 != 26) {
	    System.out.println("(median) degrees of freedom N-k: " + dof2);
	    errcount++;
	}
	value = lstat.getValue();
	if (Math.abs(value - 2.64065) > 1.e-5) {
	    System.out.println("(median) lstat = " + value);
	    errcount++;
	}
	cv = lstat.getCriticalValue(Statistic.PValueMode.POSITIVE_SIDE, 0.05);
	if (Math.abs(cv - 2.97515) > 1.e-5) {
	    System.out.println("(median) critical value = " + cv);
	    errcount++;
	}

	lstat = new LeveneStat(LeveneStat.Mode.TRIMMED, ldata2);
	dof1 = (int) lstat.getDegreesOfFreedom1();
	dof2 = (int) lstat.getDegreesOfFreedom2();
	if (dof1 != 3) {
	    System.out.println("(trimmed) degrees of freedom k-1: " + dof1);
	    errcount++;
	}
	if (dof2 != 26) {
	    System.out.println("(trimmed) degrees of freedom N-k: " + dof2);
	    errcount++;
	}
	value = lstat.getValue();
	if (Math.abs(value - 3.38862) > 4.e-4) {
	    errcount++;
	}
	cv = lstat.getCriticalValue(Statistic.PValueMode.POSITIVE_SIDE, 0.05);
	if (Math.abs(cv - 2.97515) > 1.e-5) {
	    System.out.println("(trimmed) critical value = " + cv);
	    errcount++;
	}

	if (errcount > 0) {
	    System.exit(1);
	}
	System.exit(0);
    }
}
