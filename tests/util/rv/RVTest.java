import java.util.Spliterator;
import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import org.bzdev.math.stats.BasicStats;

class ourUniformIntegerRV extends UniformIntegerRV {
    public ourUniformIntegerRV(int lowerLimit, int upperLimit) {
	super(lowerLimit, upperLimit);
	setRequiredMinimum(2, true);
	setRequiredMaximum(20, true);
    }
}

public class RVTest {
    static void utest(BooleanRandomVariable brv, IntegerRandomVariable irv,
		      LongRandomVariable lrv, DoubleRandomVariable drv)
    {
	double udsum = 0.0;
	double udsumsq = 0.0;
	long uisum = 0;
	long uisumsq = 0;
	long ulsum = 0;
	long ulsumsq = 0;
	int bcfalse = 0; int bctrue = 0;
	for (int i = 0; i < 1000; i++) {
	    double dv = drv.next();
	    int iv = irv.next();
	    long lv = lrv.next();
	    if (brv != null) {
		boolean bv = brv.next();
		if (bv) bctrue++; else bcfalse++;
	    }
	    udsum += dv;
	    udsumsq += dv*dv;
	    uisum += iv;
	    uisumsq += iv*iv;
	    ulsum += lv;
	    ulsumsq += lv*lv;
	}
	if (brv != null) {
	    System.out.println("brv true count = " + bctrue
			       + ", false count = " + bcfalse);
	    System.out.print("brv stream:");
	    brv.stream(10).forEach((e) -> System.out.print(" " + e));
	    System.out.println();
	    brv.parallelStream(10).forEach((e) -> System.out.print(" " + e));
	    System.out.println();
	    brv.stream()
		.limit(10)
		.forEach((e) -> System.out.print(" " + e));
	    System.out.println();
	    brv.parallelStream()
		.limit(10)
		.forEach((e) -> System.out.print(" " + e));
	    System.out.println();
	}
	System.out.println("irv true count = " + bctrue
			   + ", false count = " + bcfalse);
	System.out.print("irv stream:");
	irv.stream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	irv.parallelStream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	irv.stream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();
	irv.parallelStream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();

	System.out.println("lrv true count = " + bctrue
			   + ", false count = " + bcfalse);
	System.out.print("lrv stream:");
	lrv.stream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	lrv.parallelStream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	lrv.stream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();
	lrv.parallelStream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();

	System.out.println("drv true count = " + bctrue
			   + ", false count = " + bcfalse);
	System.out.print("drv stream:");
	drv.stream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	drv.parallelStream(10).forEach((e) -> System.out.print(" " + e));
	System.out.println();
	drv.stream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();
	drv.parallelStream()
	    .limit(10)
	    .forEach((e) -> System.out.print(" " + e));
	System.out.println();

	double udmean = udsum / 1000;
	double udsdev = Math.sqrt(udsumsq/1000.0 - udmean*udmean);
	System.out.println("for drv, mean = " + udmean +", sdev = " + udsdev);
	double uimean = uisum / 1000.0;
	double uisdev = Math.sqrt(uisumsq/1000.0 - uimean*uimean);
	System.out.println("for irv, mean = " + uimean +", sdev = " + uisdev);
	double ulmean = ulsum / 1000.0;
	double ulsdev = Math.sqrt(ulsumsq/1000.0 - ulmean*ulmean);
	System.out.println("for lrv, mean = " + ulmean +", sdev = " + ulsdev);

    }

    public static void main(String argv[]) throws Exception {

	StaticRandom.maximizeQuality();
	BooleanRandomVariable trueRV = new FixedBooleanRV(true);
	BooleanRandomVariable falseRV = new FixedBooleanRV(false);

	System.out.println("Fixed cases");
	BooleanRandomVariable brv = new FixedBooleanRV(true);
	IntegerRandomVariable irv = new FixedIntegerRV(10);
	LongRandomVariable lrv = new FixedLongRV(20L);
	DoubleRandomVariable drv = new FixedDoubleRV(5.0);
	LongRandomVariable iarv = new FixedIATimeRV(25L);
	for (int i = 0; i < 10; i++) {
	    System.out.println(brv.next() +", " + irv.next()
			       +", " + lrv.next() + ", " + drv.next()
			       +", " + iarv.next());
	}
	System.out.println("-----\nexpecting\n-----\n"
			   + "true, 10, 20, 5.0, 25 (repeated)"
			   + "\n--------");


	brv = new FixedBooleanRVRV(brv).next();
	irv = new FixedIntegerRVRV(irv).next();
	lrv = new FixedLongRVRV(lrv).next();
	drv = new FixedDoubleRVRV(drv).next();
	iarv = new FixedIATimeRVRV((FixedIATimeRV)iarv).next();
	for (int i = 0; i < 10; i++) {
	    System.out.println(brv.next() +", " + irv.next()
			       +", " + lrv.next() + ", " + drv.next()
			       +", " + iarv.next());
	}
	System.out.println("-----\nexpecting\n-----\n"
			   + "true, 10, 20, 5.0, 25 (repeated)"
			   + "\n--------");

	System.out.println("\nFixed case based on a Guassian");
	GaussianRV ftgrv = new GaussianRV(5.0, 2.0);
	FixedDoubleRVRV fdrvrv = new FixedDoubleRVRV(ftgrv);
	double tsum = 0.0;
	double tsumsq = 0.0;
	int max = 100000;
	for (int i = 0; i < max; i++) {
	    drv = fdrvrv.next();
	    double val = drv.next();
	    tsum += val;
	    tsumsq += val*val;
	}
	double tmean = tsum/max;
	double tsdev = Math.sqrt(tsumsq/max - tmean*tmean);
	System.out.println("tmean = " + tmean + ", tsdev = " + tsdev);

	tsum = 0.0;
	tsumsq = 0.0;
	fdrvrv.setMinimum(4.5, true);
	fdrvrv.setMaximum(5.5, true);
	double badcnt = 0;
	for (int i = 0; i < max; i++) {
	    drv = fdrvrv.next();
	    double val = drv.next();
	    tsum += val;
	    tsumsq += val*val;
	    if (val > 5.5 || val < 4.5) badcnt++;
	}
	tmean = tsum/max;
	tsdev = Math.sqrt(tsumsq/max - tmean*tmean);
	System.out.println("tmean = " + tmean + ", tsdev = " + tsdev
			   + ", badcnt = " + badcnt);
	System.out.println();

	System.out.println("Determ cases");

	boolean barray[] = {true, false, true, false};
	int iarray[] = {0, 1, 2, 3};
	long larray[] = {0L, 1L, 2L, 3L};
	double darray[] = {0.0, 1.0, 2.0, 3.0};

	brv = new DetermBooleanRV(barray);
	irv = new DetermIntegerRV(iarray);
	lrv = new DetermLongRV(larray);
	drv = new DetermDoubleRV(darray);
	iarv = new DetermIATimeRV(larray);


	for (int i = 0; i < 8; i++) {
	    System.out.println(brv.next() +", " + irv.next()
			       +", " + lrv.next() + ", " + drv.next()
			       +", " + iarv.next());
	}
	System.out.println("-----\nexpecting\n-----");
	for (int i = 0; i < 8; i++) {
	    System.out.println(barray[i%4] +", " + iarray[i%4]
			       +", " + larray[i%4] + ", " + darray[i%4]
			       +", " + larray[i%4]);
	}
	System.out.println("--------");

	brv = new DetermBooleanRV(barray, true);
	irv = new DetermIntegerRV(iarray, 5);
	lrv = new DetermLongRV(larray, 5L);
	drv = new DetermDoubleRV(darray, 5.0);
	iarv = new DetermIATimeRV(larray, 5L);
	for (int i = 0; i < 6; i++) {
	    System.out.println(brv.next() +", " + irv.next()
			       +", " + lrv.next() + ", " + drv.next()
			       + ", " + iarv.next());
	}
	System.out.println("-----\nexpecting\n-----");
	for (int i = 0; i < 4; i++) {
	    System.out.println(barray[i] +", " + iarray[i]
			       +", " + larray[i] + ", " + darray[i]
			       +", " + larray[i]);
	}
	for (int i = 0; i < 2; i++) {
	    System.out.println("true, 5, 5, 5.0, 5");
	}
	System.out.println("--------");
	brv = new DetermBooleanRV(false, true);
	irv = new DetermIntegerRV(4, 5);
	lrv = new DetermLongRV(4L, 5L);
	drv = new DetermDoubleRV(4.0, 5.0);
	iarv = new DetermIATimeRV(4L, 5L);
	for (int i = 0; i < 3; i++) {
	    System.out.println(brv.next() +", " + irv.next()
			       + ", " + lrv.next() + ", " + drv.next()
			       + ", "  + iarv.next());
	}
	System.out.println("-----\nexpecting\n-----");
	System.out.println("false, 4, 4, 4.0, 4");
	System.out.println("true, 5, 5, 5.0, 5");
	System.out.println("true, 5, 5, 5.0, 5");
	System.out.println("--------");

	GaussianRV grv = new GaussianRV(0.0, 1.0);
	double gsum = 0.0;
	double gsumsq = 0.0;
	iarv = new PoissonIATimeRV(100.0);
	long psum = 0;
	long psumsq = 0;
	ExpDistrRV erv = new ExpDistrRV(1.0);
	double esum = 0.0;
	double esumsq = 0.0;
	for (int i = 0; i < 1000; i++) {
	    double gv = grv.next();
	    gsum += gv;
	    gsumsq += gv*gv;
	    long iav = iarv.next();
	    psum += iav;
	    psumsq += iav*iav;
	    double eav = erv.next();
	    esum += eav;
	    esumsq += eav*eav;
	}
	double gmean = gsum / 1000;
	double gsdev = Math.sqrt(gsumsq/1000 - gmean*gmean);
	System.out.println("Gaussian mean = " + gmean
			   + ", sdev = " + gsdev);
	System.out.println("...... expected 0.0, 1.0");
	System.out.println("psum = " + psum);
	double pmean = psum / 1000.0;
	double psdev = Math.sqrt(psumsq/1000.0 - pmean*pmean);
	System.out.println("Poission mean IA time  = " + pmean
			   + ", sdev = " + psdev);
	System.out.println("............... expected 100.0, 100.0");
	System.out.println(" psum = " + psum + ", iarv.next(1000) = "
			   + ((PoissonIATimeRV)iarv).next(1000));
	double emean = esum / 1000.0;
	double esdev = Math.sqrt(esumsq/1000.0 - emean*emean);
	System.out.println("Exp Distr mean = " + emean
			   + ", sdev = " + esdev);
	System.out.println("...... expecting 1.0, 1.0");
	System.out.println("esum = " + esum + ", erv.next(1000) = "
			   + erv.next(1000));
	System.out.println("Testing ExpDistrRVRV with a Gaussian random ");
	System.out.println("variable giving the mean value of each ExpDistrRV");
	ExpDistrRVRV ervrv = new ExpDistrRVRV(new GaussianRV(100.0, 30.0));
	DoubleRandomVariableRV dervrv = ervrv; // test of types.
	gsum = 0.0;
	gsumsq = 0.0;
	for (int i = 0; i < 1000; i++) {
	    erv = ervrv.next();
	    double mean = erv.getMean();
	    gsum += mean;
	    gsumsq += mean*mean;
	}
	gmean = gsum / 1000.0;
	gsdev = Math.sqrt(gsumsq/1000.0 - gmean*gmean);
	System.out.println("mean of RV means = " + gmean
			   + ", sdev = " + gsdev);
	System.out.println("........ expecting 100.0, 30.0");


	System.out.println("uniform test, [1.0, 2.0)  [0, 10)");
	brv = new UniformBooleanRV();
	drv = new UniformDoubleRV(1.0, 2.0);
	lrv = new UniformLongRV(0, 10);
	irv = new UniformIntegerRV(0, 10);
	utest(brv, irv, lrv, drv);

	System.out.println("uniform test, [1.0, 2.0]  [0, 10]");
	drv = new UniformDoubleRV(1.0, true, 2.0, true);
	irv = new UniformIntegerRV(0, true, 10, true);
	lrv = new UniformLongRV(0, true, 10, true);
	utest(brv, irv, lrv, drv);

	System.out.println("uniform test, (1.0, 2.0]  (0, 10]");
	drv = new UniformDoubleRV(1.0, false, 2.0, true);
	irv = new UniformIntegerRV(0, false, 10, true);
	lrv = new UniformLongRV(0, false, 10, true);
	utest(brv, irv, lrv, drv);

	System.out.println("uniform test, (1.0, 2.0)  (0, 10)");
	drv = new UniformDoubleRV(1.0, false, 2.0, false);
	irv = new UniformIntegerRV(0, false, 10, false);
	lrv = new UniformLongRV(0, false, 10, false);
	utest(brv, irv, lrv, drv);

	System.out.print("spliterator test:");
	irv.spliterator(10).forEachRemaining((int val) -> {
		System.out.print(" " + val);
	    });
	System.out.println();

	BasicStats stats = new BasicStats.Population();
	Spliterator<Integer> spliterator = irv.spliterator(1000000);
	Spliterator<Integer> spliterator2;
	int scount = 0;
	while ((spliterator2 = spliterator.trySplit()) != null) {
	    scount++;
	    System.out.println("... estimated size = "
			       + spliterator2.estimateSize());
	    spliterator2.forEachRemaining((val) -> {
		    stats.add((double)(int)val);
		});
	}
	spliterator.forEachRemaining((val) -> {
		stats.add((double)(int)val);
	    });
	if (stats.size() != 1000000) {
	    throw new Exception("stats.size() = " + stats.size());
	}
	BasicStats stats2 = new BasicStats.Population();
	spliterator = irv.spliterator();
	spliterator2 = spliterator.trySplit();

	while (spliterator.tryAdvance((val)->{stats2.add((double)(int)val);})
	       && stats2.size() < 500000);
	while (spliterator2.tryAdvance((val)->{stats2.add((double)(int)val);})
	       && stats2.size() < 1000000);
	System.out.println("... stats2.size()  = " + stats2.size());
	if (stats2.size() != 1000000) {
	    throw new Exception("stats2.size() = " + stats2.size());
	}


	System.out.println("uniform rvrv test, [1.0, 2.0)  [0, 10)");
	DoubleRandomVariableRV drvrv =
	    new UniformDoubleRVRV(new FixedDoubleRV(1.0),
				  new FixedDoubleRV(2.0));
	IntegerRandomVariableRV irvrv =
	    new UniformIntegerRVRV(new FixedIntegerRV(0),
				   new FixedIntegerRV(10));
	LongRandomVariableRV lrvrv = new UniformLongRVRV(new FixedLongRV(0),
							 new FixedLongRV(10));
	utest(null, irvrv.next(), lrvrv.next(), drvrv.next());

	System.out.println("uniform rvrv test, [1.0, 2.0]  [0, 10]");
	drvrv = new UniformDoubleRVRV(new FixedDoubleRV(1.0), true,
				      new FixedDoubleRV(2.0), true);
	irvrv = new UniformIntegerRVRV(new FixedIntegerRV(0), true,
				       new FixedIntegerRV(10), true);
	lrvrv = new UniformLongRVRV(new FixedLongRV(0), true,
				    new FixedLongRV(10), true);
	utest(null, irvrv.next(), lrvrv.next(), drvrv.next());

	System.out.println("uniform rvrv test, (1.0, 2.0]  (0, 10]");
	drvrv = new UniformDoubleRVRV(new FixedDoubleRV(1.0), false,
				      new FixedDoubleRV(2.0), true);
	irvrv = new UniformIntegerRVRV(new FixedIntegerRV(0), false,
				       new FixedIntegerRV(10), true);
	lrvrv = new UniformLongRVRV(new FixedLongRV(0), false,
				    new FixedLongRV(10), true);
	utest(null, irvrv.next(), lrvrv.next(), drvrv.next());

	System.out.println("uniform rvrv test, (1.0, 2.0)  (0, 10)");
	drvrv = new UniformDoubleRVRV(new FixedDoubleRV(1.0), false,
				      new FixedDoubleRV(2.0), false);
	irvrv = new UniformIntegerRVRV(new FixedIntegerRV(0), false,
				       new FixedIntegerRV(10), false);
	lrvrv = new UniformLongRVRV(new FixedLongRV(0), false,
				    new FixedLongRV(10), false);
	utest(null, irvrv.next(), lrvrv.next(), drvrv.next());

	LogNormalRV lnrv = new LogNormalRV(0.0, 1.0);
	double lnsum = 0.0;
	double lnsumsq = 0.0;
	for (int i = 0; i < 1000; i++) {
	    double lnv = lnrv.next();
	    lnsum += lnv;
	    lnsumsq += lnv*lnv;
	}
	double lnmean = lnsum / 1000;
	double lnsdev = Math.sqrt(lnsumsq/1000 - lnmean*lnmean);
	System.out.println("LogNormal mean = " + lnmean
			   + ", sdev = " + lnsdev);
	System.out.println("...... expected " + lnrv.getMean()
			   + ", " + lnrv.getSDev());

	LogNormalRVRV  lnrvrv = new LogNormalRVRV(new FixedDoubleRV(2.0),
						  new FixedDoubleRV(3.0));
	lnrv = lnrvrv.next();
	System.out.println("lnrv log-mean = " + lnrv.getMu()
			   + ", lnrv log-sdev = " + lnrv.getSigma());
	System.out.println ("..... expected 2.0, 3.0");

	double mu = LogNormalRV.getMu(1.0, 2.0);
	double sigma = LogNormalRV.getSigma(1.0, 2.0);
	lnrv = new LogNormalRV(mu, sigma);
	System.out.println ("for new LogNormalRV with mean 1.0 and Sdev 2.0, "
			    + "mean = " + lnrv.getMean()
			    + ", sdev = " + lnrv.getSDev());

	irv = new UniformIntegerRV(0, 10);
	irv.tightenMaximumS("8", true);
	irv.tightenMinimumS("3", true);
	try {
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 8)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 3)
		    throw new Exception("tightenMinimumS failed");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	irv = new ourUniformIntegerRV(0, 25);
	try {
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 20)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 2)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irv.tightenMinimum(0, true);
	    irv.tightenMaximum(22, true);
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 20)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 2)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irv.setMinimum(0, true);
	    irv.setMaximum(22, true);
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 20)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 2)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irv.tightenMinimum(7, true);
	    irv.tightenMaximum(18, true);
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 18)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 7)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irv.setMinimum(0, true);
	    irv.setMaximum(25, true);
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 20)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 2)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irv.setMinimum(7, true);
	    irv.setMaximum(18, true);
	    for (int i = 0 ; i < 1000; i++) {
		int ix = irv.next();
		if (ix > 18)
		    throw new Exception("tightenMaximumS failed");
		if (ix < 7)
		    throw new Exception("tightenMinimumS failed");
	    }
	    irvrv = new UniformIntegerRVRV(new UniformIntegerRV(0, 6),
					   new UniformIntegerRV(10,16));
	    irvrv.setMinimum(3, true);
	    irvrv.setMaximum(14, true);
	    for (int i = 0; i < 1000; i++) {
		irv = irvrv.next();
		if (irv.getMinimum() < 3 || irv.getMaximum() > 14) {
		    throw new Exception
			("irvrv.setMinimum or irvrv.setMaximum failed");
		}
	    }
	    System.out.println("PoissonIntegerRV test:");
	    double lambdas[] = {0.1, 1.0, 2.0, 5.0, 10.0, 33.0};
	    PoissonIntegerRV prv;
	    for (double lambda: lambdas) {
		prv = new PoissonIntegerRV(lambda);
		double prvsum = 0.0;
		double prvsumsq = 0.0;
		for (int i = 0; i < 100000; i++) {
		    int k = prv.next();
		    prvsum += k;
		    prvsumsq += (k * k);
		}
		double mean = prvsum / 100000.0;
		System.out.println("... lambda = " + lambda
				   + ", mean = " + mean
				   + ", variance = "
				   + (prvsumsq/100000.0 - mean*mean));
	    }
	    PoissonIntegerRVRV prvrv =
		new PoissonIntegerRVRV(new FixedDoubleRV(5.0));
	    for (int i = 0; i < 10; i++) {
		prv = prvrv.next();
		if (prv.getMean() != 5.0) {
		    System.out.println(prv.getMean() + " != 5.0");
		    System.exit(1);
		}
	    }

	    brv = new BinomialBooleanRV(0.5);
	    int tcount = 0;
	    for (int i = 0; i < 1000; i++) {
		if (brv.next()) {
		    tcount++;
		}
	    }
	    System.out.println("tcount = " + tcount
			       + ", should be close to 500");
	   BooleanRandomVariableRV brvrv =
	       new BinomialBooleanRVRV(new FixedDoubleRV(0.5));
	    for (int j = 0; j < 100; j++) {
		brv = brvrv.next();
		for (int i = 0; i < 1000; i++) {
		    if (brv.next()) {
			tcount++;
		    }
		}
	    }
	    System.out.println("tcount = " + tcount
			       + ", should be close to 50000");
	    double prob = 0.1;
	    int n = 5;
	    double bmean = 0.0;
	    double bvar = 0.0;
	    irv = new BinomialIntegerRV(prob, n);
	    for (int i = 0; i < 1000; i++) {
		double x = (double)irv.next();
		bmean += x;
		bvar += x*x;
	    }
	    bvar /= 1000;
	    bmean /= 1000;
	    bvar -= bmean*bmean;
	    System.out.println("BinomialIntRV: mean = " + bmean
			       + ", variance = " + bvar
			       + ": (expecting " + (n*prob) +",  "
				  + ((n*prob)*(1.0-prob)) + ")");
	    n = 20;
	    bmean = 0.0;
	    bvar = 0.0;
	    irv = new BinomialIntegerRV(prob, n);
	    for (int i = 0; i < 1000; i++) {
		double x = (double)irv.next();
		bmean += x;
		bvar += x*x;
	    }
	    bvar /= 1000;
	    bmean /= 1000;
	    bvar -= bmean*bmean;
	    System.out.println("BinomialIntRV: mean = " + bmean
			       + ", variance = " + bvar
			       + ": (expecting " + (n*prob) +",  "
				  + ((n*prob)*(1.0-prob)) + ")");
	    n = 90;
	    bmean = 0.0;
	    bvar = 0.0;
	    irv = new BinomialIntegerRV(prob, n);
	    for (int i = 0; i < 1000; i++) {
		double x = (double)irv.next();
		bmean += x;
		bvar += x*x;
	    }
	    bvar /= 1000;
	    bmean /= 1000;
	    bvar -= bmean*bmean;
	    System.out.println("BinomialIntRV: mean = " + bmean
			       + ", variance = " + bvar
			       + ": (expecting " + (n*prob) +",  "
				  + ((n*prob)*(1.0-prob)) + ")");

	    n = 120;
	    bmean = 0.0;
	    bvar = 0.0;
	    irv = new BinomialIntegerRV(prob, n);
	    for (int i = 0; i < 1000; i++) {
		double x = (double)irv.next();
		bmean += x;
		bvar += x*x;
	    }
	    bvar /= 1000;
	    bmean /= 1000;
	    bvar -= bmean*bmean;
	    System.out.println("BinomialIntRV: mean = " + bmean
			       + ", variance = " + bvar
			       + "(: expecting " + (n*prob) +",  "
				  + ((n*prob)*(1.0-prob)) + ")");

	    n = 12000;
	    prob = 0.04;
	    bmean = 0.0;
	    bvar = 0.0;
	    irv = new BinomialIntegerRV(prob, n);
	    for (int i = 0; i < 40000; i++) {
		double x = (double)irv.next();
		bmean += x;
		bvar += x*x;
	    }
	    bvar /=40000;
	    bmean /= 40000;
	    bvar -= bmean*bmean;
	    System.out.println("BinomialIntRV: mean = " + bmean
			       + ", variance = " + bvar
			       + "(: expecting " + (n*prob) +",  "
				  + ((n*prob)*(1.0-prob)) + ")");

	    BinomialIntegerRVRV birvrv = new
		BinomialIntegerRVRV(new FixedDoubleRV(0.5), 10);
	    BinomialIntegerRV  birv = birvrv.next();
	    System.out.println("birv.getProb() = " + birv.getProb()
			       + ", birv.getN() = " + birv.getN());
	    birvrv = new BinomialIntegerRVRV(new FixedDoubleRV(0.5),
					     new FixedIntegerRV(10));
	    birv = birvrv.next();
	    System.out.println("birv.getProb() = " + birv.getProb()
			       + ", birv.getN() = " + birv.getN());

	    BinomialLongRVRV blrvrv = new
		BinomialLongRVRV(new FixedDoubleRV(0.5), 10);
	    BinomialLongRV  blrv = blrvrv.next();
	    System.out.println("blrv.getProb() = " + blrv.getProb()
			       + ", blrv.getN() = " + blrv.getN());
	    blrvrv = new BinomialLongRVRV(new FixedDoubleRV(0.5),
					     new FixedLongRV(10));
	    blrv = blrvrv.next();
	    System.out.println("blrv.getProb() = " + blrv.getProb()
			       + ", blrv.getN() = " + blrv.getN());

	    BinomialDoubleRVRV bdrvrv = new
		BinomialDoubleRVRV(new FixedDoubleRV(0.5), 10);
	    BinomialDoubleRV  bdrv = bdrvrv.next();
	    System.out.println("bdrv.getProb() = " + bdrv.getProb()
			       + ", bdrv.getN() = " + bdrv.getN());
	    bdrvrv = new BinomialDoubleRVRV(new FixedDoubleRV(0.5),
					     new FixedDoubleRV(10));
	    bdrv = bdrvrv.next();
	    System.out.println("bdrv.getProb() = " + bdrv.getProb()
			       + ", bdrv.getN() = " + bdrv.getN());

	    double cov[][] = {
		{4.0, 1.0, 1.5},
		{1.0, 6.0, 2.0},
		{1.5, 2.0, 7.0},
	    };
	    double means[] = {10.0, 11.0, 12.0};
	    int iterations = 1000000;

	    GaussianRVs grvs = new GaussianRVs(cov, means);
	    double[][] ecov = new double[3][3];
	    double[] values = new double[3];
	    for (int k = 0; k < iterations; k++) {
		grvs.next(values);
		for (int i = 0; i < 3; i++) {
		    for (int j = 0; j < 3; j++) {
			ecov[i][j] += (values[i]-means[i])*(values[j]-means[j]);
		    }
		}
	    }
	    for (int i = 0; i < 3; i++) {
		for (int j = 0; j < 3; j++) {
		    ecov[i][j] /= iterations;
		    if (Math.abs(ecov[i][j] - cov[i][j])/cov[i][j] > 1.e-2) {
			System.out.format("ecov[%d][%d]=%g, cov[%d][%d]=%g\n",
					  i, j, ecov[i][j], i, j, cov[i][j]);
			throw new Exception("bad GaussianRVs");
		    }
		}
	    }
	    grvs = new GaussianRVs(cov, means, 3);
	    ecov = new double[3][3];
	    for (int k = 0; k < iterations; k++) {
		grvs.next(values);
		for (int i = 0; i < 3; i++) {
		    for (int j = 0; j < 3; j++) {
			ecov[i][j] += (values[i]-means[i])*(values[j]-means[j]);
		    }
		}
	    }
	    for (int i = 0; i < 3; i++) {
		for (int j = 0; j < 3; j++) {
		    ecov[i][j] /= iterations;
		    if (Math.abs(ecov[i][j] - cov[i][j])/cov[i][j] > 2.e-2) {
			System.out.format("ecov[%d][%d]=%g, cov[%d][%d]=%g\n",
					  i ,j, ecov[i][j], i, j, cov[i][j]);
			throw new Exception("bad GaussianRVs");
		    }
		}
	    }
	    GaussianRVRV grvrv = new GaussianRVRV(new UniformDoubleRV
						  (-1.0, 1.0),
						  new UniformDoubleRV
						  (1.0, 2.0));
	    System.out.println("Gaussian RVRV stream:");
	    grvrv.stream(10).forEach((rv) -> {
		    System.out.println(rv.getMean() + "+-" + rv.getSDev());
		});
	    System.out.println("Gaussian RVRV stream:");
	    grvrv.parallelStream(10).forEach((rv) -> {
		    System.out.println(rv.getMean() + "+-" + rv.getSDev());
		});
	    System.out.println("Gaussian RVRV stream:");
	    grvrv.stream().limit(10).forEach((rv) -> {
		    System.out.println(rv.getMean() + "+-" + rv.getSDev());
		});
	    System.out.println("Gaussian RVRV stream:");
	    grvrv.parallelStream().limit(10).forEach((rv) -> {
		    System.out.println(rv.getMean() + "+-" + rv.getSDev());
		});


	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
