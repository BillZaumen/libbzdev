import org.bzdev.math.stats.*;

public class CovarianceMatrixTest {

    static double data[][] = {{1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0},
			      {1.0, 2.0},
			      {-1.0, -2.0}};

    static double[] expectedMean = {11.0, 12.0};


    static double[][] pexpected = {{1.0, 2.0}, {2.0, 4.0}};


    public static void main(String argv[]) throws Exception {

	// Offset data so the means will not be zero.
	for (int i = 0; i < data.length; i++) {
	    for (int j = 0; j < expectedMean.length; j++) {
		data[i][j] += expectedMean[j];
	    }
	}

	CovarianceMatrix cm = new CovarianceMatrix.Sample(data, 2);
	cm.addsComplete();
	double[] means = cm.getMeans();
	double[][]matrix = cm.getMatrix();
	double[][]cmatrix = cm.getCorrelationMatrix();

	System.out.println("sample covariance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);
	System.out.println("correlation matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  cmatrix[0][0], cmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  cmatrix[1][0], cmatrix[1][1]);

	cm = new CovarianceMatrix.Population(data, 2);
	cm.addsComplete();
	double[] pmeans = cm.getMeans();
	double[][] pmatrix = cm.getMatrix();

	System.out.println("population covariance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);

	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 8.0/7.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}



	cm = new CovarianceMatrix.Sample(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	means = cm.getMeans();
	matrix = cm.getMatrix();


	System.out.println("sample covariance using add alone");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);

	cm = new CovarianceMatrix.Population(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	pmeans = cm.getMeans();
	pmatrix = cm.getMatrix();

	System.out.println("population covariance using add alone");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);

	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 8.0/7.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}

	System.out.println();
	System.out.println("REPEAT WITHOUT USING addsComplete()");
	System.out.println();

	cm = new CovarianceMatrix.Sample(data, 2);
	means = cm.getMeans();
	matrix = cm.getMatrix();

	System.out.println("sample covariance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);

	cm = new CovarianceMatrix.Population(data, 2);
	pmeans = cm.getMeans();
	pmatrix = cm.getMatrix();

	System.out.println("population covariance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);

	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 8.0/7.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}

	cm = new CovarianceMatrix.Sample(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	means = cm.getMeans();
	matrix = cm.getMatrix();

	System.out.println("sample covariance using add alone");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);

	cm = new CovarianceMatrix.Population(2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	pmeans = cm.getMeans();
	pmatrix = cm.getMatrix();

	System.out.println("population covariance using add alone");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);

	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 8.0/7.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}

	System.out.println();
	System.out.println("COMBINED case");
	System.out.println();

	cm = new CovarianceMatrix.Sample(data, 2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	means = cm.getMeans();
	matrix = cm.getMatrix();

	System.out.println("sample covariance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);

	cm = new CovarianceMatrix.Population(data, 2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	cm.addsComplete();
	pmeans = cm.getMeans();
	pmatrix = cm.getMatrix();

	System.out.println("population covariance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);



	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 16.0/15.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}

	System.out.println();
	System.out.println("COMBINED case without addsComplete");
	System.out.println();

	cm = new CovarianceMatrix.Sample(data, 2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	means = cm.getMeans();
	matrix = cm.getMatrix();

	System.out.println("sample covariance");
	System.out.format("means: %g, %g\n", means[0], means[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  matrix[0][0], matrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  matrix[1][0], matrix[1][1]);

	cm = new CovarianceMatrix.Population(data, 2);
	for (int i = 0; i < data.length; i++) {
	    double[] values = data[i];
	    cm.add(values);
	}
	pmeans = cm.getMeans();
	pmatrix = cm.getMatrix();

	System.out.println("population covariance");
	System.out.format("means: %g, %g\n", pmeans[0], pmeans[1]);
	System.out.println("covariance matrix:");
	System.out.format("    \u23A1 %g, %g \u23A4\n",
			  pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    \u23A3 %g, %g \u23A6\n",
			  pmatrix[1][0], pmatrix[1][1]);


	for (int i = 0; i < matrix.length; i++) {
	    if (Math.abs(pmeans[i] - expectedMean[i]) > 1.e-10) {
		throw new Exception("population means not as expected");
	    }
	    if (Math.abs(pmeans[i] - means[i]) > 1.e-10) {
		throw new Exception("sample and popultions means not "
				    + "consistent");
	    }
	    for (int j = 0; j < matrix.length; j++) {
		if (Math.abs(pmatrix[i][j] - pexpected[i][j]) > 1.e-10) {
		    throw new Exception ("population matrix not as expected");
		}
		if (Math.abs(matrix[i][j]/pmatrix[i][j] - 16.0/15.0) > 1.e-10) {
		    throw new Exception ("sample and population matrices not "
					 + "consistent");
		}
	    }
	}

	System.exit(0);
    }
}
