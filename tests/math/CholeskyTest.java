import org.bzdev.math.*;

public class CholeskyTest {
    public static void main(String argv[]) {
	double[][] matrix = {{4.0, 12.0, -16.0},
			     {12.0, 37.0, -43.0},
			     {-16.0, -43.0, 98.0}};
    
	double[] flat = {4.0, 12.0, -16.0,
			  12.0, 37.0, -43.0,
			  -16.0, -43.0, 98.0};

	CholeskyDecomp choleskyDecomp = new CholeskyDecomp(matrix);

	double explicitDet = (4.0 * (37.0*98.0 - 43.0 * 43.0)
			      - 12.0 * (12.0*98.0 - 16.0*43.0)
			      - 16.0 * (-12.0*43.0 + 16.0*37.0));

	System.out.println("determinate = " + choleskyDecomp.det()
			   + ", expecting " + explicitDet);

	double[][] L = choleskyDecomp.getL();
	double[][] U = choleskyDecomp.getU();

	System.out.println("A = ");
	for (int i = 0; i < 3; i++) {
	    for (int j  = 0; j < 3; j++) {
		System.out.print(" " + matrix[i][j]);
	    }
	    System.out.println();
	}

	System.out.println("L = ");
	for (int i = 0; i < 3; i++) {
	    for (int j  = 0; j < 3; j++) {
		System.out.print(" " + L[i][j]);
	    }
	    System.out.println();
	}

	System.out.println("UD = ");
	for (int i = 0; i < 3; i++) {
	    for (int j  = 0; j < 3; j++) {
		System.out.print(" " + U[i][j]);
	    }
	    System.out.println();
	}

	double[] b = {1.0, 2.0, 3.0};
	double[] x = new double[3];
	double[] y = new double[3];
	choleskyDecomp.solve(x, b);
	for (int i = 0; i < 3; i++) {
	    double sum = 0.0;
	    for (int j = 0; j < 3; j++) {
		sum += matrix[i][j]*x[j];
	    }
	    y[i] = sum;
	    if (Math.abs(y[i] - b[i]) > 1.e-10) {
		System.out.format("y[%d] = %g\n", i, y[i]);
		System.exit(1);
	    }
	}

	double[][] B = {{1.0, 4.0, 7.0, 10.0},
		      {2.0, 5.0, 8.0, 11.0},
		      {3.0, 6.0, 9.0, 12.0}};
	double[][] X = new double[3][4];

	double[][] Y = new double[3][4];
	choleskyDecomp.solve(X, B);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		double sum = 0.0;
		for (int k = 0; k < 3; k++) {
		    sum += matrix[i][k]*X[k][j];
		}
		Y[i][j] = sum;
		if (Math.abs(Y[i][j] - B[i][j]) > 1.e-10) {
		    System.out.format("Y[%d][%d] = %g\n", i, j, Y[i][j]);
		    System.exit(1);
		}
	    }
	}

	X = choleskyDecomp.getInverse();
	Y = new double[3][3];
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		double sum = 0.0;
		for (int k = 0; k < 3; k++) {
		    sum += matrix[i][k]*X[k][j];
		}
		Y[i][j] = sum;
		if (i == j) {
		    if (Math.abs(sum - 1.0) > 1.e-10) {
			System.out.format("Y[%d][%d]  = %g, expecting 1.0\n",
					  i, j, sum);
			System.exit(1);
		    }
		} else {
		    if (Math.abs(sum) > 1.e-10) {
			System.out.format("Y[%d][%d]  = %g, expecting 0.0\n",
					  i, j, sum);
			System.exit(1);
		    }
		}
	    }
	}

	double[][] tmpmatrix = new double[3][3];

	CholeskyDecomp cd = new CholeskyDecomp(matrix, tmpmatrix);
	double[][] LL = cd.getL();
	double[][] UU = cd.getU();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (LL[i][j] != L[i][j] || UU[i][j] != U[i][j]) {
		    System.out.println("LL not L or UU not U");
		    System.exit(1);
		}
		tmpmatrix[i][j] = matrix[i][j];
	    }
	}
	
	cd = new CholeskyDecomp(tmpmatrix, tmpmatrix);
	LL = cd.getL();
	UU = cd.getU();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (LL[i][j] != L[i][j] || UU[i][j] != U[i][j]) {
		    System.out.println("LL not L or UU not U");
		    System.exit(1);
		}
	    }
	}

	cd = new CholeskyDecomp(flat, 3);
	LL = cd.getL();
	UU = cd.getU();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (LL[i][j] != L[i][j] || UU[i][j] != U[i][j]) {
		    System.out.println("LL not L or UU not U");
		    System.exit(1);
		}
	    }
	}

	cd = new CholeskyDecomp(matrix, 3, true);
	LL = cd.getL();
	UU = cd.getU();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (LL[i][j] != L[i][j] || UU[i][j] != U[i][j]) {
		    System.out.println("LL not L or UU not U");
		    System.exit(1);
		}
	    }
	}

	System.exit(0);
    }
}