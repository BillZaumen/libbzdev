import org.bzdev.math.stats.*;
import org.bzdev.math.Functions;

public class StudentTest {

    static double ourP(double t, int nu) {
	double nup1o2 = (nu+1)/2.0;
	return 0.5 + t * Functions.Gamma(nup1o2)
	    * Functions.hgF(1,2, nu+1,2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));
    }

    static double ourQ(double t, int nu) {
	double nup1o2 = (nu+1)/2.0;
	return 0.5 - t * Functions.Gamma(nup1o2)
	    * Functions.hgF(1,2, nu+1,2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));
    }

    static double ourA(double t, int nu) {
	double nup1o2 = (nu+1)/2.0;
	return 2.0 * t * Functions.Gamma(nup1o2)
	    * Functions.hgF(1,2, nu+1,2, 1.5, -t*t/nu)
	    / (Math.sqrt(Math.PI*nu) * Functions.Gamma(nu/2.0));

    }

    public static void main(String argv[]) throws Exception {
	/*
	System.out.format("StudentsTDistr.P(40.0, 8) = %s\n",
			   StudentsTDistr.P(40.0, 8));
	*/

	int errcount = 0;

	System.out.println("Test Student's t distribution ...");
	for (int nu = 1; nu < 10; nu++) {
	    for (int i = 0; i < 40; i++) {
		double t = (i)*0.1;
		double value = StudentsTDistr.A(t,nu);
		double expecting = ourA(t,nu);
		if (Math.abs(value-expecting) > 1.e-10) {
		    System.out.format("A(%g,%d) = %s, expecting %s\n",
				      t, nu, value, expecting); 
		    errcount++;
		}
		value = StudentsTDistr.P(t, nu);
		expecting = ourP(t, nu);
		if (Math.abs(value-expecting) > 1.e-10) {
		    System.out.format("P(%g,%d) = %s, expecting %s\n",
				      t, nu, value, expecting); 
		    errcount++;
		}

		value = StudentsTDistr.Q(t, nu);
		expecting = ourQ(t, nu);

		if (Math.abs(value-expecting) > 1.e-10) {
		    System.out.format("Q(%g,%d) = %s, expecting %s\n",
				      t, nu, value, expecting); 
		    errcount++;
		}
	    }
	}

	StudentsTDistr sd = new StudentsTDistr(6);
	for (int i = 0; i < 40; i++) {
	    double t = (i-20)*0.1;
	    if (sd.pd(t) != StudentsTDistr.pd(t, 6)) {
		System.out.println("sd.pd failed, t = " + t
				   + ", " + sd.pd(t) + " != "
				   + StudentsTDistr.pd(t, 6));
		errcount++;
	    }

	    if (sd.P(t) != StudentsTDistr.P(t, 6)) {
		System.out.println("sd.P failed, t = " + t
				   + ", " + sd.P(t) + " != "
				   + StudentsTDistr.P(t, 6));
		errcount++;
	    }
	    if (sd.Q(t) != StudentsTDistr.Q(t, 6)) {
		System.out.println("sd.Q failed, t = " + t
				   + ", " + sd.Q(t) + " != "
				   + StudentsTDistr.Q(t, 6));
		errcount++;
	    }
	    if (sd.A(t) != StudentsTDistr.A(t, 6)) {
		System.out.println("sd.A failed, t = " + t
				   + ", " + sd.A(t) + " != "
				   + StudentsTDistr.A(t, 6));
		errcount++;
	    }

	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	if (false) {

	    // timing test

	    double sum = 0;
	    double z = 0.0;
	    int max = 1000000;

	    long tbase = System.nanoTime();
	    for (int i = 0; i < max; i++) {
		z = (1 + i %10) / 100.0;
		sum += StudentsTDistr.P(z, 10);
	    }
	    long t1 = System.nanoTime();

	    z = 0.0;
	    for (int i = 0; i < max; i++) {
		z = (1 + i %10) / 100.0;
		sum += StudentsTDistr.A(z, 10);
	    }
	    long t2 = System.nanoTime();
	    System.out.println("P: " + (t1-tbase)
			       +", A: " + (t2-t1));
	}

	System.out.println("test inverses for Student's t distribution ...");

	sd = new StudentsTDistr(6);

	for (int i = 0; i < 101; i++) {
	    double x = i/100.0;
	    double inverse = sd.inverseP(x);
	    double value = sd.P(inverse);
	    if (Math.abs(x - value) > 1.e-9) {
		System.out.format("sd.P(sd.inverseP(%g)) = %s\n", x, value);
		errcount++;
	    }
	}

	for (int i = 0; i < 101; i++) {
	    double x = i/100.0;
	    double inverse = sd.inverseQ(x);
	    double value = sd.Q(inverse);
	    if (Math.abs(x - value) > 1.e-9) {
		System.out.format("sd.Q(sd.inverseQ(%g)) = %s\n", x, value);
		errcount++;
	    }
	}

	for (int i = 0; i < 201; i++) {
	    double x = (i-100)/100.0;
	    double inverse = sd.inverseA(x);
	    double value = sd.A(inverse);
	    if (Math.abs(x - value) > 1.e-9) {
		System.out.format("sd.A(sd.inverseA(%g)) = %s\n", x, value);
		errcount++;
	    }
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("Two-sample t-test (unpaired) ...");
	/*
	 * Data sets and values from
	 * http://www.statsdirect.com/help/default.htm
	 *       #parametric_methods/unpaired_t.htm
	 */
	double[] A1 = {134.0, 146.0, 104.0, 119.0, 124.0, 161.0,
		       107.0, 83.0, 113.0, 129.0, 97.0, 123.0};

	double[] A2 = {70.0, 118.0, 101.0, 85.0, 107.0, 132.0, 94.0};

	StudentsTStat.Mean2 sm2 = new StudentsTStat.Mean2(A1, A2);
	if (Math.abs(sm2.getValue() - 1.891436) > 1.e-6) {
	    System.out.println("t test: " + sm2.getValue()
			       + ", expecting 1.891436");
	    errcount++;
	}
	if (sm2.getDegreesOfFreedom() != 17) {
	    System.out.println("wrong degrees of freedom");
	    errcount++;
	}
	sd = new StudentsTDistr(17);
	if (sd.Q(0.0379) != sm2.getDistribution().Q(0.0379)) {
	    System.out.println("sm2 does not have the expected distribution");
	    errcount++;
	}

	if (Math.abs(sm2.getPValue(Statistic.PValueMode.TWO_SIDED) - 0.0757)
	    > .0001) {
	    System.out.format("[1] Two-sided p-value incorrect: %s != %s\n",
			      sm2.getPValue(Statistic.PValueMode.TWO_SIDED),
			      0.0757);
	    errcount++;

	}
	if (Math.abs(sm2.getPValue(Statistic.PValueMode.ONE_SIDED) - 0.0379)
	    > .0001) {
	    System.out.format("[2]Two-sided p-value incorrect: %s != %s\n",
			      sm2.getPValue(Statistic.PValueMode.TWO_SIDED),
			      0.0379);
	    errcount++;
	}
	if (Math.abs(sm2.getPValue(Statistic.PValueMode.POSITIVE_SIDE)-0.0379)
	    > .0001) {
	    System.out.format("[3] Two-sided p-value incorrect: %s != %s\n",
			      sm2.getPValue(Statistic.PValueMode.TWO_SIDED),
			      0.0379);
	    errcount++;

	}

	// reverse order so that the t-test will return a negative value
	sm2 = new StudentsTStat.Mean2(A2, A1);
	if (Math.abs(sm2.getPValue(Statistic.PValueMode.ONE_SIDED) - 0.0379)
	    > .0001) {
	    System.out.format("[4]Two-sided p-value incorrect: %s != %s\n",
			      sm2.getPValue(Statistic.PValueMode.TWO_SIDED),
			      0.0379);
	    errcount++;
	}
	if (Math.abs(sm2.getPValue(Statistic.PValueMode.NEGATIVE_SIDE)-0.0379)
	    > .0001) {
	    System.out.format("[5] Two-sided p-value incorrect: %s != %s\n",
			      sm2.getPValue(Statistic.PValueMode.TWO_SIDED),
			      0.0379);
	    errcount++;

	}


	sm2 = new StudentsTStat.Mean2();
	for (double x: A1) {
	    sm2.add1(x);
	}
	for (double x: A2) {
	    sm2.add2(x);
	}
	if (Math.abs(sm2.getValue() - 1.891436) > 1.e-6) {
	    System.out.println("t test: " + sm2.getValue()
			       + ", expecting 1.891436");
	    errcount++;
	}
	if (sm2.getDegreesOfFreedom() != 17) {
	    System.out.println("wrong degrees of freedom");
	    errcount++;
	}
	if (sd.Q(0.0379) != sm2.getDistribution().Q(0.0379)) {
	    System.out.println("sm2 does not have the expected distribution");
	    errcount++;
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	// now test critcal value code
	//t est values computed using the calculator at
	// https://www.easycalculation.com/statistics/critical-t-test.php
	sm2 = new StudentsTStat.Mean2(A1, A2);

	double cv = sm2.getCriticalValue(Statistic.PValueMode.TWO_SIDED, 0.05);
	if (Math.abs(cv - 2.1098) > 1.e-4) {
	    System.out.println ("expecting cv = 2.1098, saw " + cv);
	    errcount++;
	}
	cv = sm2.getCriticalValue(Statistic.PValueMode.ONE_SIDED, 0.05);
	if (Math.abs(cv - 1.7396) > 1.e-4) {
	    System.out.println ("expecting cv = 1.7396, saw " + cv);
	    errcount++;
	}

	cv = sm2.getCriticalValue(Statistic.PValueMode.POSITIVE_SIDE, 0.05);
	if (Math.abs(cv - 1.7396) > 1.e-4) {
	    System.out.println ("expecting cv = 1.7396, saw " + cv);
	    errcount++;
	}
	cv = sm2.getCriticalValue(Statistic.PValueMode.NEGATIVE_SIDE, 0.05);
	if (Math.abs(cv + 1.7396) > 1.e-4) {
	    System.out.println ("expecting cv = -1.7396, saw " + cv);
	    errcount++;
	}

	System.out.println("Welch's t-test ...");
	WelchsTStat wt = new WelchsTStat(A1, A2);
	if (Math.abs(wt.getValue() - 1.9107) > 1.e-4) {
	    System.out.println("wt.getValue() = " + wt.getValue());
	    errcount++;
	}
	if (Math.abs(wt.getDegreesOfFreedom() - 13.081702) > 1.e-6) {
	    System.out.println("wt.getDegreesOfFreedom() = " +
			       wt.getDegreesOfFreedom());
	    errcount++;
	}
	sd = new StudentsTDistr(13);
	if (sd.P(0.5) != wt.getDistribution().P(0.5)) {
	    System.out.println("WelchsTStat: wrong distribution");
	    errcount++;
	}
	wt = new WelchsTStat();
	for (double x: A1) {
	    wt.add1(x);
	}
	for (double x: A2) {
	    wt.add2(x);
	}
	if (Math.abs(wt.getValue() - 1.9107) > 1.e-4) {
	    System.out.println("wt.getValue() = " + wt.getValue());
	    errcount++;
	}
	if (Math.abs(wt.getDegreesOfFreedom() - 13.081702) > 1.e-6) {
	    System.out.println("wt.getDegreesOfFreedom() = " +
			       wt.getDegreesOfFreedom());
	    errcount++;
	}
	if (sd.P(0.5) != wt.getDistribution().P(0.5)) {
	    System.out.println("WelchsTStat: wrong distribution");
	    errcount++;
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("one-sample t-test for mean value ...");

	double[] XM1 = {5.0, 3.0, 6.0, 2.0};

	StudentsTStat.Mean1 sm1 = new StudentsTStat.Mean1(6.08, XM1);
	if (Math.abs(sm1.getValue() + 2.28) > .01) {
	    System.out.println("sm1.getValue = " + sm1.getValue()
			       +", expecting -2.28");
	}
	if (sm1.getDegreesOfFreedom() != 3) {
	    System.out.println("degrees of freedom = "
			       + sm1.getDegreesOfFreedom()
			       + ", expecting 3");
	}
	sm1 = new StudentsTStat.Mean1(6.08);
	for (double x: XM1) {
	    sm1.add(x);
	}
	if (Math.abs(sm1.getValue() + 2.28) > .01) {
	    System.out.println("sm1.getValue = " + sm1.getValue()
			       +", expecting -2.28");
	}
	if (sm1.getDegreesOfFreedom() != 3) {
	    System.out.println("degrees of freedom = "
			       + sm1.getDegreesOfFreedom()
			       + ", expecting 3");
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	/*
	 * http://lycofs01.lycoming.edu/~sprgene/M123/Text/UNIT_31.pdf
	 * contains the test data and values for an independent
	 * comparison.
	 */

	System.out.println("Slope test ...");
	double[] L = {4.0, 4.0, 6.0, 6.0, 8.0, 8.0, 10.0, 10.0};
	double[] M = {7.5, 6.8, 4.0, 4.4, 3.9, 3.1, 1.4, 1.7};

	StudentsTStat.Slope sts = new StudentsTStat.Slope(0.0, L, M);
	if (Math.abs(sts.getFit().getParameters()[0] - 10.225) > .001) {
	    System.out.println("sts.getFit().getParameters()[0] = "
			       + sts.getFit().getParameters()[0]
			       + ", expecting 10.225");
	    errcount++;
	}
	if (Math.abs(sts.getFit().getParameters()[1] + 0.875) > 0.001) {
	    System.out.println("sts.getFit().getParameters[1] = "
			       + sts.getFit().getParameters()[1]
			       + ", expecting- 0.875");
	    errcount++;
	}
	if (Math.abs(sts.getValue() + 8.723) > 0.001) {
	    System.out.println("t = " + sts.getValue()
			       + ", expecting -8.723");
	    errcount++;
	}
	if (sts.getDegreesOfFreedom() != 6) {
	    System.out.println("degrees of freedom = "
			       + sts.getDegreesOfFreedom()
			       + ", expecting 6");
	}


	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	/*
	 * https://onlinecourses.science.psu.edu/stat500/node/51
	 * contains an example used as an independent test.
	 */
	System.out.println("paired case ...");
	double[] P1 = {.430, .266, .567, .531, .707,
		       .716, .651, .589, .469, .723};
	double[] P2 = {.415, .238, .390, .410, .605,
		       .609, .632, .523, .411, .612};

	StudentsTStat.PairedDiff stp = new StudentsTStat.PairedDiff(0.0, P1,P2);
	if (Math.abs(stp.getValue() - 4.86) > 0.01) {
	    System.out.println("stp.getValue() = " + stp.getValue()
			       + ", expecting -4.86");
	    errcount++;
	}

	if (stp.getDegreesOfFreedom() != 9) {
	    System.out.println("stp.getDegreesOfFreedom() = "
			       + stp.getDegreesOfFreedom()
			       + ", expecting 9");
	    errcount++;
	}

	stp = new StudentsTStat.PairedDiff(0.0);
	for (int i = 0; i < P1.length; i++) {
	    stp.add(P1[i], P2[i]);
	}
	if (Math.abs(stp.getValue() - 4.86) > 0.01) {
	    System.out.println("stp.getValue() = " + stp.getValue()
			       + ", expecting -4.86");
	    errcount++;
	}

	if (stp.getDegreesOfFreedom() != 9) {
	    System.out.println("stp.getDegreesOfFreedom() = "
			       + stp.getDegreesOfFreedom()
			       + ", expecting 9");
	    errcount++;
	}
	stp = new StudentsTStat.PairedDiff(0.0);
	for (int i = 0; i < P1.length; i++) {
	    stp.add(P1[i] - P2[i]);
	}
	if (Math.abs(stp.getValue() - 4.86) > 0.01) {
	    System.out.println("stp.getValue() = " + stp.getValue()
			       + ", expecting -4.86");
	    errcount++;
	}

	if (stp.getDegreesOfFreedom() != 9) {
	    System.out.println("stp.getDegreesOfFreedom() = "
			       + stp.getDegreesOfFreedom()
			       + ", expecting 9");
	    errcount++;
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("non-central t distribution test ...");
	// used http://keisan.casio.com/exec/system/1180573219
	// to get values for comparision

	double[][] ncdata = {{1.1, 5.0, 2.1,
			      0.240224216807885554663,
			      0.159191093576257799181,
			      0.840808906423742200819},
			     {2.5, 5.0, 2.2,
			      0.287000267968713917644,
			      0.551062983371741896486,
			      0.448937016628258103514},
			     {-0.5, 5.0, 1.2,
			      0.087659900400917942696,
			      0.0487940559807395830423,
			      0.9512059440192604169577}};

	for (double[] data: ncdata) {
	    double x = data[0];
	    int nu = (int)Math.round(data[1]);
	    double mu = data[2];
	    double pd = data[3];
	    double P = data[4];
	    double Q = data[5];
	    double pdValue = StudentsTDistr.pd(x, nu, mu);
	    double Pvalue = StudentsTDistr.P(x, nu, mu);
	    double Qvalue = StudentsTDistr.Q(x, nu, mu);

	    if (Math.abs(pdValue - pd) > 1.e-10) {
		System.out.format
		    ("pd: %s, expecting %s for x=%g, nu=%d, mu=%g\n",
		     pdValue, pd, x, nu, mu);
		errcount++;
	    }
	    if (Math.abs(Pvalue - P) > 1.e-10) {
		System.out.format
		    ("P: %s, expecting %s for x=%g, nu=%d, mu=%g\n",
		     Pvalue, P, x, nu, mu);
		errcount++;
	    }
	    if (Math.abs(Qvalue - Q) > 1.e-10) {
		System.out.format
		    ("Q: %s, expecting %s for x=%g, nu=%d, mu=%g\n",
		     Qvalue, Q, x, nu, mu);
		errcount++;
	    }
	}

	sm1 = new StudentsTStat.Mean1(0.0, 0.0, 1.0, 15);
	ProbDistribution distr = sm1.getDistribution();
	System.out.println("distr.P(0.0) = " + distr.P(0.0));
	System.out.println("distr.Q(0.0) = " + distr.Q(0.0));
	double cval =
	    sm1.getCriticalValue(Statistic.PValueMode.TWO_SIDED, 0.01);
	System.out.println("cval = " + cval);
	System.out.println("distr.P(cval) = " + distr.P(cval));
	System.out.println("distr.Q(cval) = " + distr.Q(cval));

	double noncentp = sm1.getNCParameter(1.1);
	ProbDistribution adistr = sm1.getDistribution(noncentp);
	System.out.println("adistr.P(cval) = " + adistr.P(cval));
	System.out.println("adistr.Q(cval) = " + adistr.Q(cval));
	System.out.println("adistr.P(-cval) = " + adistr.P(-cval));
	System.out.println("adistr.Q(-cval) = " + adistr.Q(-cval));

	double power = sm1.getPower(noncentp, -cval, cval);
	double beta = sm1.getBeta(noncentp, -cval, cval);
	double tbeta = StudentsTDistr.P(cval, 14, noncentp)
	    - StudentsTDistr.P(-cval, 14, noncentp);

	if (Math.abs(beta - tbeta) > 1.e-10) {
	    errcount++;
	    System.out.println("tbeta = " + tbeta + ", beta = " + beta);
	}
	if (Math.abs(power+beta - 1.0) > 1.e-10) {
	    errcount++;
	    System.out.println("power = " + power + ", beta = " + beta);
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.exit(0);
    }
}
