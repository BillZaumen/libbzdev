import org.bzdev.math.*;

public class QRTest {
    public static void main(String argv[]) throws Exception {
	double[][] A1 = {{2.0, 3.0, 4.0},
			 {11.0, 20.0, 40.0},
			 {21.0, 5.0, 10.0},
			 {33.0, 34.0, 35.0}};

	QRDecomp qr = new QRDecomp(A1);

	double[][] Q = qr.getQ();

	double[][] R = qr.getR();

	double[][] QR = MatrixOps.multiply(Q,R);
	System.out.println("Q = ");
	for (int i = 0; i < 4; i++) {
	    System.out.format("| %8.3g %8.3g %8.3g |\n",
			      Q[i][0], Q[i][1], Q[i][2]);
	}
	System.out.println();

	System.out.println("R = ");
	for (int i = 0; i < 3; i++) {
	    System.out.format("| %8.3g %8.3g %8.3g |\n",
			      Q[i][0], Q[i][1], Q[i][2]);
	}
	System.out.println();


	System.out.println("QR = ");
	for (int i = 0; i < 4; i++) {
	    System.out.format("| %8.3g %8.3g %8.3g |\n",
			      QR[i][0], QR[i][1], QR[i][2]);
	}
	System.out.println();

	double[][] H = qr.getH();

	System.out.println("H = ");
	for (int i = 0; i < 4; i++) {
	    System.out.format("| %8.3g %8.3g %8.3g |\n",
			      H[i][0], H[i][1], H[i][2]);
	}
	System.out.println();

	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 3; j++) {
		if (Math.abs(A1[i][j] - QR[i][j]) > 1.e-10) {
		    String msg =
			String.format("A1[%d][%d] = %g, QR[%d][%d] = %g\n",
				      i, j, A1[i][j], i, j, QR[i][j]);
		    throw new Exception(msg);
		}
	    }
	}

	double b[] = {2.0, 3.0, 10.0, 15.0};
	double[][] B = new double[4][5];
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 5; j++) {
		B[i][j] = b[i];
	    }
	}
	double[] x = qr.solve(b);
	double[][]X = qr.solve(B);

	for (int j = 0; j < 5; j++) {
	    for (int i = 0; i < x.length; i++) {
		if (x[i] != X[i][j]) {
		    throw new Exception("bad X");
		}
	    }
	    
	}

	double lsq =
	    VectorOps.norm(MatrixOps.subtract(MatrixOps.multiply(A1,x), b));

	System.out.println("lsq = " + lsq);

	for (int k = -10; k <= 10; k++) {
	    for (int i = 0; i < 3; i++) {
		double[] x2 = x.clone();
		x2[i] += k/100.0;
		double lsq2 =
		    VectorOps.norm(MatrixOps.subtract
				   (MatrixOps.multiply(A1,x2), b));
		if (lsq2 < lsq) throw new Exception("not least squares");
	    }
	}

    }
     
}
