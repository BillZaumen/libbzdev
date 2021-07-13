import org.bzdev.math.StaticRandom;
import org.bzdev.math.stats.*;
import org.bzdev.math.rv.GaussianRV;
import java.util.function.Function;

public class BasicStatsTest {

    static double data[] = {2.0, -2.0, 2.0, -2.0,
			    2.0, -2.0, 2.0, -2.0};

    static double data2[] = {4.0, -4.0, 4.0, -4.0,
			    4.0, -4.0, 4.0, -4.0};

    static double pexpected = 4.0;

    static double expectedMean =  11.0;
    static double expectedMean2 = 21.0;

    public static void main(String argv[]) throws Exception {

	// Offset data so the means will not be zero.
	for (int i = 0; i < data.length; i++) {
	    data[i] += expectedMean;
	    data2[i] += expectedMean2;
	}
	BasicStats cm = new BasicStats.Sample(data);
	double mean = cm.getMean();
	double variance = cm.getVariance();
	double sdev = cm.getSDev();

	System.out.println("sample stats");
	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  mean, variance, sdev);
	System.out.println("population stats");
	cm = new BasicStats.Population(data);
	double pmean = cm.getMean();
	double pvariance = cm.getVariance();
	double psdev = cm.getSDev();

	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  pmean, pvariance, psdev);

	if (Math.abs(mean - expectedMean) > 1.e-10) {
	    throw new Exception("population mean not as expected");
	}
	if (Math.abs(pmean - mean) > 1.e-10) {
	    throw new Exception("sample and popultions means not "
				+ "consistent");
	}
	if (Math.abs(pvariance - pexpected) > 1.e-10) {
	    throw new Exception ("population variance not as expected");
	}
	if (Math.abs(variance/pvariance - 8.0/7.0) > 1.e-10) {
	    throw new Exception ("sample and population variances not "
				 + "consistent");
	}

	cm = new BasicStats.Sample();
	for (int i = 0; i < data.length; i++) {
	    double value = data[i];
	     cm.add(value);
	}
	mean = cm.getMean();
	variance = cm.getVariance();
	sdev = cm.getSDev();

	System.out.println("sample stats using add");
	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  mean, variance, sdev);

	cm = new BasicStats.Population();
	for (int i = 0; i < data.length; i++) {
	    double value = data[i];
	    cm.add(value);
	}
	pmean = cm.getMean();
	pvariance = cm.getVariance();
	psdev = cm.getSDev();

	System.out.println("population covariance using add");
	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  pmean, pvariance, psdev);

	if (Math.abs(pmean - expectedMean) > 1.e-10) {
	    throw new Exception("population mean not as expected");
	}
	if (Math.abs(pmean- mean) > 1.e-10) {
	    throw new Exception("sample and popultions means not "
				+ "consistent");
	}
	if (Math.abs(pvariance - pexpected) > 1.e-10) {
	    throw new Exception ("population matrix not as expected");
	}
	if (Math.abs(variance/pvariance - 8.0/7.0) > 1.e-10) {
	    throw new Exception ("sample and population matrices not "
				 + "consistent");
	}


	System.out.println();
	System.out.println("COMBINED case");
	System.out.println();

	cm = new BasicStats.Sample(data);
	BasicStats cm2 = new BasicStats.Sample(cm.getMean(), cm.getVariance(),
					       cm.size());
	if (cm2.getMean() != cm.getMean()
	    || cm2.getVariance() != cm.getVariance()) {
	    throw new Exception("cm2 and cm differ");
	}

	for (int i = 0; i < data.length; i++) {
	    double value = data[i];
	     cm.add(value);
	     cm2.add(value);
	}

	if (Math.abs(cm2.getMean() - cm.getMean()) > 1.e-10
	    || Math.abs(cm2.getVariance() - cm.getVariance()) > 1.e-10) {
	    throw new Exception("cm2 and cm differ");
	}

	mean = cm.getMean();
	variance = cm.getVariance();
	sdev = cm.getSDev();

	System.out.println("sample stats");
	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  mean, variance, sdev);
	System.out.println("population stats");
	cm = new BasicStats.Population(data);
	cm2 = new BasicStats.Population(cm.getMean(), cm.getVariance(),
					cm.size());
	if (cm2.getMean() != cm.getMean()
	    || cm2.getVariance() != cm.getVariance()) {
	    throw new Exception("cm2 and cm differ");
	}

	for (int i = 0; i < data.length; i++) {
	    double value = data[i];
	     cm.add(value);
	     cm2.add(value);
	}

	if (Math.abs(cm2.getMean() - cm.getMean()) > 1.e-10
	    || Math.abs(cm2.getVariance() - cm.getVariance()) > 1.e-10) {
	    throw new Exception("cm2 and cm differ");
	}

	pmean = cm.getMean();
	pvariance = cm.getVariance();
	psdev = cm.getSDev();

	System.out.format("mean: %g; variance: %g; sdev: %g\n",
			  pmean, pvariance, psdev);

	if (Math.abs(mean - expectedMean) > 1.e-10) {
	    throw new Exception("population mean not as expected");
	}
	if (Math.abs(pmean - mean) > 1.e-10) {
	    throw new Exception("sample and popultions means not "
				+ "consistent");
	}
	if (Math.abs(pvariance - pexpected) > 1.e-10) {
	    throw new Exception ("population variance not as expected");
	}
	if (Math.abs(variance/pvariance - 16.0/15.0) > 1.e-10) {
	    throw new Exception ("sample and population variances not "
				 + "consistent");
	}

	// check trimmed mean.

	double[] tdata = {-1000.0, 1.0, 2.0, 3.0, 4.0, 5.0,
			  6.0, 7.0, 8.0, 9.0, 10.0+20.0, 500.0};
	
	BasicStats tcm = new BasicStats.Population(tdata);
	double tcmMean = tcm.getMean();
	// System.out.println("tcm mean = " + tcm.getMean());

	double tm = BasicStats.trimmedMean(12, tdata);
	if (Math.abs(tm - 7.5) > 1.e-10) {
	   throw new Exception("trimmed mean (12) = " + tm);
	}

	tm = BasicStats.trimmedMean(10, tdata);
	
	if (Math.abs(tm - 7.1) > 1.e-10) {
	    throw new Exception("trimmed mean (10) = " + tm);
	}

	tm = BasicStats.trimmedMean(10, 100, tdata);
	if (Math.abs(tm - 7.1) > 1.e-10) {
	    throw new Exception("trimmed mean (10) = " + tm);
	}

	tm = BasicStats.trimmedMean(6, tdata);
	if (Math.abs(tm - 5.5) > 1.e-10) {
	    throw new Exception("trimmed mean (6) = "+ tm);
	}

	tm = BasicStats.trimmedMean(6, 100, tdata);
	double etm = tcmMean*(1.0 -.72) + .72*7.5;
	if (Math.abs(tm - etm) > 1.e-10) {
	    throw new Exception("trimmed mean (6/100) = "+ tm);
	}

	double median = BasicStats.median(tdata);
	if (Math.abs(median - 5.5) > 1.e-10) {
	    throw new Exception("median = " + median);
	}

	double[] tdata1 = new double[tdata.length - 1];
	System.arraycopy(tdata, 0, tdata1, 0, tdata1.length);
	median = BasicStats.median(tdata1);
	if (Math.abs(median - 5.0) > 1.e-10) {
	    throw new Exception("median = " + median);
	}

	BasicStats stats = new BasicStats.Population();
	BasicStats stats1 = new BasicStats.Population();
	BasicStats stats2 = new BasicStats.Population();

	for (double value: data) {
	    stats.add(value);
	    stats1.add(value);
	}

	for (double value: data2) {
	    stats.add(value);
	    stats2.add(value);
	}

	stats1.addAll(stats2);

	if (stats.size() != stats1.size()) {
	    throw new Exception(stats.size() + " != "  + stats2.size());
	}

	if (Math.abs(stats.getMean() - stats1.getMean()) > 1.e-10) {
	    throw new Exception("means: " + stats.getMean() + " != "
				+ stats1.getMean());
	}
	if (Math.abs(stats.getVariance() - stats1.getVariance()) > 1.e-10) {
	    throw new Exception("variances: " + stats.getVariance() + " != "
				+ stats1.getVariance());
	}

	System.out.println("stats.mean = " + stats.getMean());
	System.out.println("stats1.mean = " + stats1.getMean());
	System.out.println("stats.variance = " + stats.getVariance());
	System.out.println("stats1.variance = " + stats1.getVariance());
	System.out.println();
	System.out.println("---------");
	System.out.println();
	System.out.println("numerical stability test");
	System.out.println("... expecting 1000018.0000 +- 1.0000");

	stats = new BasicStats.Population();
	double sum = 0.0;
	double sumsq = 0.0;
	int N = 10000;
	double x1 = 1000019.0;
	double x2 = 1000017.0;
	for (int i = 0; i < N; i++) {
	    sum += x1;
	    sum += x2;
	    sumsq += x1*x1;
	    sumsq += x2*x2;
	    stats.add(x1);
	    stats.add(x2);
	}
	int N2 = N*2;
	mean = sum/N2;
	sdev =  Math.sqrt((sumsq/N2) - mean*mean);
	System.out.format("naive value: %9.4f +- %1.5f\n", mean, sdev);
	System.out.format("BasicStats value: %9.4f +- %1.4f\n",
			  + stats.getMean(), stats.getSDev());
	System.out.format("error (means) (naive/BasicStats): %g, %g\n",
			   (mean - 1000018.0),
			   (stats.getMean() - 1000018.0));

	System.out.format("error (sdevs) (naive/BasicStats): %g, %g\n",
			  (sdev - 1.0), (stats.getSDev() - 1.0));

	System.out.println("---- streams test ---=");

	StaticRandom.maximizeQuality();
	GaussianRV rv = new GaussianRV(10.0, 1.0);

	BasicStats rvstats = new BasicStats.Population();
	rv.stream(1000000).forEach(rvstats::add);
	System.out.println("mean = " + rvstats.getMean());
	System.out.println("sdev = " + rvstats.getSDev());

	BasicStats rvstats2 = rv.parallelStream(1000000)
	    .mapToObj(Double::valueOf)
	    .reduce(BasicStats.Population.identity(),
		    BasicStats::add,
		    BasicStats::addAll);
	System.out.println("bstats2.size() = " + rvstats2.size());
	System.out.println("mean = " + rvstats2.getMean());
	System.out.println("sdev = " + rvstats2.getSDev());


	System.exit(0);
    }
}
