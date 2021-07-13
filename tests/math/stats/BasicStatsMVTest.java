import org.bzdev.math.stats.*;

public class BasicStatsMVTest {

    static double data[][] = {{1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0}};

    static double data2[][] = {{3.0, 5.0},
			      {-3.0, -5.0},
			      {3.0, 5.0},
			      {-3.0, -5.0},
			      {3.0, 5.0},
			      {-3.0, -5.0},
			      {3.0, 5.0},
			      {-3.0, -5.0}};


    static double[] expectedMean = {11.0, 12.0};
    static double[] expectedMean2 = {21.0, 22.0};

    static double[] pexpected = {1.0, 4.0};

    public static void main(String argv[]) throws Exception {

	// Offset data so the means will not be zero.
	for (int i = 0; i < data.length; i++) {
	    for (int j = 0; j < expectedMean.length; j++) {
		data[i][j] += expectedMean[j];
		data2[i][j] += expectedMean2[j];
	    }
	}

	BasicStatsMV cm = new BasicStatsMV.Sample(data, 2);
	double[] means = cm.getMeans();
	double[]variances = cm.getVariances();

	System.out.println("sample mean and variance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.format("variances: %g, %g\n", variances[0], variances[1]);

	cm = new BasicStatsMV.Population(data, 2);
	double[] pmeans = cm.getMeans();
	double[] pvariances = cm.getVariances();
	double[] psdevs = cm.getSDevs();

	double[] tarray = new double[2];
	cm.getMeans(tarray);
	for (int i = 0; i < pmeans.length; i++) {
	    if (pmeans[i] != tarray[i]) throw new Exception();
	}

	cm.getVariances(tarray);
	for (int i = 0; i < pmeans.length; i++) {
	    if (pvariances[i] != tarray[i]) throw new Exception();
	}

	cm.getSDevs(tarray);
	for (int i = 0; i < pmeans.length; i++) {
	    if (psdevs[i] != tarray[i]) throw new Exception();
	}


	System.out.println("population mean and variance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.format("variances: %g, %g\n", pvariances[0], pvariances[1]);

	for (int i = 0; i < variances.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    if (Math.abs(pvariances[i] - pexpected[i]) > 1.e-10) {
		throw new Exception ("population variances not as expected");
	    }
	    if (Math.abs(variances[i]/pvariances[i] - 8.0/7.0) > 1.e-10) {
		throw new Exception ("sample and population variances not "
				     + "consistent");
	    }
	}


	cm = new BasicStatsMV.Sample(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	means = cm.getMeans();
	variances = cm.getVariances();

	System.out.println("sample mean and variance using add alone");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.format("variances: %g, %g\n", variances[0], variances[1]);

	cm = new BasicStatsMV.Population(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	pmeans = cm.getMeans();
	pvariances = cm.getVariances();

	System.out.println("population mean and variance using add alone");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.format("variances: %g, %g\n", pvariances[0], pvariances[1]);

	for (int i = 0; i < variances.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    if (Math.abs(pvariances[i] - pexpected[i]) > 1.e-10) {
		throw new Exception ("population variances not as expected");
	    }
	    if (Math.abs(variances[i]/pvariances[i] - 8.0/7.0) > 1.e-10) {
		throw new Exception ("sample and population variances not "
				     + "consistent");
	    }
	}

	System.out.println();
	System.out.println("COMBINED case");
	System.out.println();

	cm = new BasicStatsMV.Sample(data, 2);
	means = cm.getMeans();
	variances = cm.getVariances();
	BasicStatsMV cm2 = new BasicStatsMV.Sample(means, variances, cm.size());
	double[] means2 = cm2.getMeans();
	double[] variances2 = cm2.getVariances();

	if (means2[0] != means[0] || means2[1] != means[1]
	    || variances2[0] != variances[0] || variances2[1] != variances[1]) {
	    throw new Exception("cm and cm2 differ");
	}

	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	    cm2.add(values);
	}

	means = cm.getMeans();
	variances = cm.getVariances();
	means2 = cm2.getMeans();
	variances2 = cm2.getVariances();

	if (Math.abs(means2[0] - means[0]) > 1.e-14
	    || Math.abs(means2[1] - means[1]) > 1.e-14
	    || Math.abs(variances2[0] - variances[0]) > 1.e-14
	    || Math.abs(variances2[1] - variances[1]) > 1.e-14) {
	    for (int i = 0; i < 2; i++) {
		System.out.format("means[%d] = %s, means2[%d] = %s\n",
				  i, means[i], i, means2[i]);
		System.out.format("variances[%d] = %s, variances2[%d] = %s\n",
				  i, variances[i], i, variances[i]);
	    }
	    throw new Exception("cm and cm2 differ");
	}

	System.out.println("sample mean and variance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.format("variances: %g, %g\n", variances[0], variances[1]);
	System.out.format("size: %d\n", cm.size());

	System.out.println("population mean and variance:");

	cm = new BasicStatsMV.Population(data, 2);

	pmeans = cm.getMeans();
	pvariances = cm.getVariances();
	cm2 = new BasicStatsMV.Population(pmeans, pvariances, cm.size());
	means2 = cm2.getMeans();
	variances2 = cm2.getVariances();

	if (means2[0] != pmeans[0] || means2[1] != pmeans[1]
	    || variances2[0] != pvariances[0]
	    || variances2[1] != pvariances[1]) {
	    throw new Exception("cm and cm2 differ");
	}

	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	    cm2.add(values);
	}
	pmeans = cm.getMeans();
	pvariances = cm.getVariances();
	means2 = cm2.getMeans();
	variances2 = cm2.getVariances();

	if (Math.abs(means2[0] - pmeans[0]) > 1.e-10
	    || Math.abs(means2[1] - pmeans[1]) > 1.e-10
	    || Math.abs(variances2[0] - pvariances[0]) > 1.e-10
	    || Math.abs(variances2[1] - pvariances[1]) > 1.e-10) {
	    System.out.format("means: %s, %s; %s, %s\n",
			      means2[0], means2[1], pmeans[0], pmeans[1]);
	    System.out.format("variances: %s, %s; %s, %s\n",
			      variances2[0], variances2[1],
			      pvariances[0], pvariances[1]);
	    throw new Exception("cm and cm2 differ");
	}

	System.out.println("population mean and variance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.format("variances: %g, %g\n", pvariances[0], pvariances[1]);
	System.out.format("size: %d\n", cm.size());

	for (int i = 0; i < variances.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    if (Math.abs(pvariances[i] - pexpected[i]) > 1.e-10) {
		throw new Exception ("population variances not as expected");
	    }
	    if (Math.abs(variances[i]/pvariances[i] - 16.0/15.0) > 1.e-10) {
		throw new Exception ("sample and population variances not "
				     + "consistent");
	    }
	}

	BasicStatsMV cm1 = new BasicStatsMV.Population(2);
	cm2 = new BasicStatsMV.Population(2);
	BasicStatsMV cm3 = new BasicStatsMV.Population(2);

	BasicStats b1 = new BasicStats.Population();
	BasicStats b2 = new BasicStats.Population();
	BasicStats b3 = new BasicStats.Population();


	for (double[] array: data) {
	    cm1.add(array);
	    cm2.add(array);
	    b1.add(array[0]);
	    b2.add(array[0]);
	}

	double[] v = cm2.getVariances();
	System.out.format("first step variances (cm2/b2): %g, %g\n",
			  v[0], b2.getVariance());

	for (double[] array: data2) {
	    cm1.add(array);
	    cm3.add(array);
	    b1.add(array[0]);
	    b3.add(array[0]);
	}
	v = cm1.getVariances();
	System.out.format("second step variances (cm1/b1): %g, %g\n",
			  v[0], b1.getVariance());
	v = cm3.getVariances();
	System.out.format("second step variances (cm3/b3): %g, %g\n",
			  v[0], b3.getVariance());

	cm2.addAll(cm3);
	b2.addAll(b3);
	v = cm2.getVariances();
	System.out.format("third step variances (cm2/b2): %g, %g\n",
			  v[0], b2.getVariance());

	// cm1 and cm2 should be identical;
	means = cm1.getMeans();
	variances = cm1.getVariances();
	means2 = cm2.getMeans();
	variances2 = cm2.getVariances();

	if (means[0] != b1.getMean() || variances[0] != b1.getVariance()) {
	    System.out.format("means: %s, %s\n", means[0], b1.getMean());
	    System.out.format("variances: %s, %s\n", variances[0],
			      b1.getVariance());
	    throw new Exception();
	}
	if (means2[0] != b2.getMean() || variances2[0] != b2.getVariance()) {
	    System.out.format("means at index 0: %g, %g\n", means2[0],
			      b2.getMean());
	    System.out.format("variances at index 0: %g, %g\n", variances2[0],
			      b2.getVariance());
	    throw new Exception();
	}

	for (int i = 0; i < cm1.length(); i++) {
	    if (Math.abs(means[i] - means2[i]) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(variances[i] - variances2[i]) > 1.e-10) {
		System.out.format("var1[%d] = %s, var2[%d] = %s\n",
				  i, variances[i], i, variances2[i]);
		throw new Exception();
	    }
	}

	System.exit(0);
    }
}
