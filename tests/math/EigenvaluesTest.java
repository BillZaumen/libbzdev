import org.bzdev.math.*;

public class EigenvaluesTest {

    public static void main(String argv[]) throws Exception {
	double A1[][] = {
	    {10.0, 1.0, 1.1, 1.2, 1.3},
	    {1.0, 20.0, 2.1, 2.2, 2.3},
	    {1.1, 2.1, 30.0, 3.1, 3.2},
	    {1.2, 2.2, 3.1, 40.0, 4.1},
	    {1.3, 2.3, 3.2, 4.1, 50.0}
	};

	LUDecomp lud = new LUDecomp(A1);
	System.out.println("A1 is nonsingular = " + lud.isNonsingular());

	System.out.println("... A1 case");

	Eigenvalues ev1 = new Eigenvalues(A1);

	double[] rev1 = ev1.getRealEigenvalues();
	double[] iev1 = ev1.getImagEigenvalues();

	for (int i = 0; i < A1.length; i++) {
	    System.out.format("%g + %gi\n", rev1[i], iev1[i]);
	}
	double[][] V1 = ev1.getV();
	double[][] V1T = ev1.getVT();
	double[][] D1 = ev1.getD();
	double[][] A1V1 = MatrixOps.multiply(A1, V1);
	double[][] V1D1= MatrixOps.multiply(V1, D1);

	for (int i = 0; i < A1.length; i++) {
	    for (int j = 0; j < A1.length; j++) {
		if (Math.abs(A1V1[i][j] - V1D1[i][j]) > 1.e-10) {
		    throw new Exception();
		}
		if (V1[i][j] != V1T[j][i]) {
		    throw new Exception();
		}
	    }
	}
	
	double[][] V1TV1 = MatrixOps.multiply(V1T, V1);


	System.out.println("V1TV1");
	for (int i = 0; i < A1.length; i++) {
	    for (int j = 0; j < A1.length; j++) {
		System.out.format("%8.3g ", V1TV1[i][j]);
	    }
	    System.out.println();
	}


	System.out.println("... A2 case");

	double A2[][] = {
	    {1.0, 2.0, 3.0, 4.0, 5.0},
	    {10.5, 11.0, 12.0, 13.0, 14.0},
	    {30.0, 31.5, 32.0, 33.0, 34.0},
	    {50.0, 51.0, 52.5, 53.0, 54.0},
	    {8.0, 6.0, 7.0, 8.5, 9.0}
	};

	lud = new LUDecomp(A2);
	System.out.println("A2 is nonsingular = " + lud.isNonsingular());


	Eigenvalues ev2 = new Eigenvalues(A2);

	double[] rev2 = ev2.getRealEigenvalues();
	double[] iev2 = ev2.getImagEigenvalues();

	double[][] V2T = ev2.getVT();
	double[][] V2TT = MatrixOps.transpose(V2T);
	double[][] V2 = ev2.getV();
	double[][] D2 = ev2.getD();

	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		if (V2[i][j] != V2TT[i][j]) {
		    throw new Exception();
		}
	    }
	}
	double[][] V2TV2 = MatrixOps.multiply(V2T,V2);

	for (int i = 0; i < A2.length; i++) {
	    System.out.format("%g + %gi, norm of VT[i] = %g\n",
			      rev2[i], iev2[i], VectorOps.norm(V2T[i]));
	}
	double[][] A2V2 = MatrixOps.multiply(A2, V2);
	double[][] V2D2= MatrixOps.multiply(V2, D2);

	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		if (Math.abs(A2V2[i][j] - V2D2[i][j]) > 1.e-10) {
		    throw new Exception();
		}
	    }
	}

	System.out.println("V2");
	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		System.out.format("%8.3g ", V2[i][j]);
	    }
	    System.out.println();
	}

	System.out.println("V2T");
	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		System.out.format("%8.3g ", V2T[i][j]);
	    }
	    System.out.println();
	}


	System.out.println("D2");
	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		System.out.format("%8.3g ", D2[i][j]);
	    }
	    System.out.println();
	}

	System.out.println("V2D2");

	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		System.out.format("%8.3g ", V2D2[i][j]);
	    }
	    System.out.println();
	}

	double[][] V2D2T = MatrixOps.transpose(V2D2);
	System.out.println("V2D2T");

	for (int i = 0; i < A2.length; i++) {
	    for (int j = 0; j < A2.length; j++) {
		System.out.format("%8.3g ", V2D2T[i][j]);
	    }
	    System.out.println();
	}

	for (int i = 0; i < A2.length; i++) {
	    double lambda = ev2.getRealEigenvalue(i);
	    double mu = ev2.getImagEigenvalue(i);
	    double[] rv = ev2.getRealEigenvector(i);
	    double[] iv = ev2.getImagEigenvector(i);
	    System.out.print("index " + i + ":");
	    if (mu == 0.0) {
		double[] rv2 = MatrixOps.multiply(lambda, rv);
		for (int j = 0; j < rv2.length; j++) {
		    System.out.format(" %8.3g", rv2[j]);
		}
		System.out.println();
	    } else {
		for (int j = 0; j < rv.length; j++) {
		    double a = rv[j];
		    double b = iv[j];
		    double rv2 = a*lambda-b*mu;
		    double iv2 = a*mu + b*lambda;
		    if (Math.abs(iv2) < Math.abs(rv2*1.e-10)) iv2 = 0.0;
		    if (iv2 > 0) {
		        System.out.format(" %.3g+%.3gi", rv2, iv2);
		    } else if (iv2 < 0) {
		        System.out.format(" %.3g-%.3gi", rv2, -iv2);
		    } else {
			System.out.format(" %.3g", rv2);
		    }
		}
		System.out.println();
	    }
	}

    }
}
