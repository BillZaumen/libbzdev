import org.bzdev.math.*;


public class LUDTest {

    static void mcompare(int[] x, int[] y) throws Exception  {
	if (x.length != y.length) throw new Exception("length");
	for (int i = 0; i < x.length; i++) {
	    if (x[i] != y[i]) {
		throw new Exception ("elements do not match");
	    }
	}
    }

    static void mcompare(double[] x, double[] y) throws Exception {
	if (x.length != y.length) throw new Exception("length");
	for (int i = 0; i < x.length; i++) {
	    if (Math.abs(x[i] - y[i]) > 1.e-10 * Math.abs(x[i])) {
		throw new Exception ("elements do not match");
	    }
	}
    }

    static void mcompare(double[][] x, double[][] y) 
	throws Exception
    {
	if (x.length != y.length) throw new Exception("length");
	for (int i = 0; i < x.length; i++) {
	    mcompare(x[i], y[i]);
	}
    }

    public static void main(String argv[]) throws Exception {

	int n = 4;
	double[][] matrix1 = 
	    {{11, 9, 24, 2}, {1, 5, 2, 6}, {3, 17, 18, 1}, {2, 5, 7, 1}};
	LUDecomp lud1 = new LUDecomp(matrix1);
	double[][] matrixL = lud1.getL();
	double[][] matrixU = lud1.getU();
	double[][] matrixP = lud1.getP().getMatrix();


	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		double lusum = 0.0;
		double pasum = 0.0;
		for (int k = 0; k < 4; k++) {
		    lusum += matrixL[i][k]*matrixU[k][j];
		    pasum += matrixP[i][k]*matrix1[k][j];
		}
		if (lusum != pasum) {
		    System.out.println("PA = " + pasum
				       +", LU = " + lusum
				       +" for i = " + i + ", j = " + j);
		    System.exit(1);
		}
	    }
	}	    
	double[][] inverse1 = new double[4][4];
	lud1.getInverse(inverse1);
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		double sum = 0.0;
		for (int k = 0; k < n; k++) {
		    sum += matrix1[i][k]*inverse1[k][j];
		}
		if ((i == j) && Math.abs(sum - 1.0) > 1.e-10) {
		    System.out.println("bad inverse1a");
		    System.exit(1);
		} else if ((i != j) && Math.abs(sum) > 1.e-10) {
		    System.out.println("bad inverse2a");
		    System.exit(1);
		}
	    }
	}

	n = 10;
	double[][] matrix = {
	    {1.0, 4.0, 7.0, 2.0, 9.0, 10.0, 3.0, 5.0, 5.5, 7.2},
	    {2.0, 3.8, 4.2, 5.3, 2.3, 8.3, 7.0, 5.3, 8.3, 9.4},
	    {5.0, 3.0, 6.1, 3.2, 1.8, 8.1, 2.2, 5.8, 3.2, 9.3},
	    {9.1, 2.3, 8.2, 4.8, 2.2, 8.2, 7.7, 3.8, 9.2, 10.2},
	    {4.3, 2.9, 1.3, 4.8, 5.5, 6.6, 3.8, 2.8, 7.7, 9.9},
	    {6.2, 7.1, 2.3, 8.1, 9.1, 5.5, 2.2, 5.6, 4.8, 6.1},
	    {3.8, 4.8, 8.9, 7.3, 5.2, 8.2, 5.7, 9.1, 7.3, 8.1},
	    {2.9, 5.9, 3.1, 2.8, 7.9, 8.5, 7.7, 9.3, 8.6, 9.2},
	    {2.1, 4.1, 9.3, 7.4, 8.1, 3.9, 4.7, 8.8, 7.1, 10.3},
	    {5.1, 3.8, 4.4, 3.2, 8.5, 9.3, 5.8, 7.1, 4.9, 0.5},
	};
	double[][] inverse = new double[n][n];
	double value = 1.0;

	LUDecomp lud = new LUDecomp(matrix, n, n, true);

	double b[] = new double[n];
	double[][] jb = new double[n][1];
	double x[] = new double[n];
	for (int i = 0; i < n; i++) {
	    b[i] = 0.5 + i;
	    jb[i][0] = b[i];
	}

	System.out.println("try lud.solve(x, b)");

	lud.solve(x, b);
	for (int i = 0; i < n; i++) {
	    double sum = 0.0;
	    double jsum = 0.0;
	    for (int j = 0; j < n; j++) {
		sum += matrix[i][j] * x[j];
	    }
	    System.out.println("sum = " + sum + " when b[" + i + "] = " + b[i]);
	}

	lud.getInverse(inverse);
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		double sum = 0.0;
		for (int k = 0; k < n; k++) {
		    sum += matrix[i][k]*inverse[k][j];
		}
		if ((i == j) && Math.abs(sum - 1.0) > 1.e-10) {
		    System.out.println("bad inverse1b");
		    System.exit(1);
		} else if ((i != j) && Math.abs(sum) > 1.e-10) {
		    System.out.println("bad inverse2b");
		    System.out.println("sum = " + sum +", i = " + i
				       + ", j = " + j);
		    
		    System.exit(1);
		}
	    }
	}

	double[] flatInverse = new double[n*n];
	System.out.println("try lud.getInverse(flatInverse, false)");
	lud.getInverse(flatInverse, false);
	int index = 0;
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j< n; j++) {
		double tmp = flatInverse[index++];
		if (tmp != inverse[i][j]) {
		    System.out.format("bad flatInverse for i = %d, j = %d: "
				      + "%g != %g\n", i, j, tmp, inverse[i][j]);
		    System.exit(1);
		}
	    }
	}

	LUDecomp ilud = new LUDecomp(flatInverse, false, n, n);
	double[][] matrix2 = ilud.getInverse();
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		if (Math.abs(matrix[i][j] - matrix2[i][j]) > 1.e-10) {
		    System.out.format("matrix2[i][j] = %g, not %g\n",
				      i, j, matrix2[i][j], matrix[i][j]);
		    System.exit(1);
		}
	    }
	}

	System.out.println("try lud.getInverse(flatInverse, true)");
	lud.getInverse(flatInverse, true);
	index = 0;
	for (int j = 0; j< n; j++) {
	    for (int i = 0; i < n; i++) {
		double tmp = flatInverse[index++];
		if (tmp != inverse[i][j]) {
		    System.out.format("bad flatInverse for i = %d, j = %d: "
				      + "%g != %g\n", i, j, tmp, inverse[i][j]);
		    System.exit(1);
		}
	    }
	}
	ilud = new LUDecomp(flatInverse, true, n, n);
	matrix2 = ilud.getInverse();
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		if (Math.abs(matrix[i][j] - matrix2[i][j]) > 1.e-10) {
		    System.out.format("matrix2[i][j] = %g, not %g\n",
				      i, j, matrix2[i][j], matrix[i][j]);
		    System.exit(1);
		}
	    }
	}

	System.out.println("LUDTest succeeded");
	System.exit(0);
    }
}