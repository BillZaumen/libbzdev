import org.bzdev.math.*;

public class SVDTest {
    static void test(double[][] A1, int m, int n) throws Exception{
	SVDecomp svd = new SVDecomp(A1);

	double[][] U = svd.getU();
	double[][] S = svd.getS();
	double[][] V = svd.getV();

	// dimension check
	if (m >= n) {
	    if (U.length != m && U[0].length != n) throw new Exception();
	    if (S.length != n && S[0].length != n) throw new Exception();
	    if (V.length != n && V[0].length != n) throw new Exception();
	} else {
	    if (U.length != m && U[0].length != m) throw new Exception();
	    if (S.length != m && S[0].length != m) throw new Exception();
	    if (V.length != n && V[0].length != m) throw new Exception();
	}

	// check that U and V are unitary
	double[][] uu = MatrixOps.multiply(MatrixOps.transpose(U), U);
	double[][] vv = MatrixOps.multiply(MatrixOps.transpose(V), V);
	for (int i = 0; i < uu.length; i++) {
	    for (int j = 0; j < uu.length; j++) {
		if (i == j) {
		    if (Math.abs(uu[i][j] - 1.0) > 1.e-10)
			throw new Exception();
		} else {
		    if (Math.abs(uu[i][j]) > 1.e-10)
			throw new Exception();
		}
	    }
	}

	for (int i = 0; i < vv.length; i++) {
	    for (int j = 0; j < vv.length; j++) {
		if (i == j) {
		    if (Math.abs(vv[i][j] - 1.0) > 1.e-10) {
			String msg = String.format("vv[%d][%d] = %g",
						   i, j, vv[i][j]);
			throw new Exception(msg);
		    }
		} else {
		    if (Math.abs(vv[i][j]) > 1.e-10) {
			String msg = String.format("vv[%d][%d] = %g",
						   i, j, vv[i][j]);
			throw new Exception(msg);
		    }
		}
	    }
	}

	double[] sv = svd.getSingularValues();

	// Check that S is diagonal and that getSingularValues agrees with S.
	for (int i = 0; i < Math.min(m,n); i++) {
	    for (int j = 0; j < Math.min(m,n); j++) {
		if (i == j) {
		    if (sv[i] != S[i][j]) throw new Exception();
		} else {
		    if (Math.abs(S[i][j]) > 1.e-10) throw new Exception();
		}
	    }
	}
	for (int i = 1; i < Math.min(m,n); i++) {
	    if (sv[i] > sv[i-1]) throw new Exception();
	}

	// Check that A1 = USV'
        double[][] A2 = MatrixOps.multiply
	    (U, MatrixOps.multiply(S, MatrixOps.transpose(V)));
	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < n; j++) {
		if (Math.abs(A1[i][j] - A2[i][j]) > 1.e-10) {
		    String msg =
			String.format("A1[%d][%d] = %g, A2[%d][%d] = %g",
				      i, j, A1[i][j], i, j, A2[i][j]);
		    throw new Exception(msg);
		}
	    }
	}

	// eigenvector checks.
	double[][] AAT = MatrixOps.multiply(A2, MatrixOps.transpose(A2));
	double[][] ATA = MatrixOps.multiply(MatrixOps.transpose(A2), A2);

	double[][] P1 = MatrixOps.multiply(AAT, U);
	double[][] P2 = MatrixOps.multiply(ATA, V);

	for (int i = 0; i < m; i++) {
	    for (int j = 0; j < Math.min(m,n); j++) {
		double ev = sv[j]*sv[j];
		if (Math.abs(P1[i][j] - ev*U[i][j]) > 1.e-10) {
		    throw new Exception();
		}
	    }
	}

	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < Math.min(m,n); j++) {
		double ev = sv[j]*sv[j];
		if (Math.abs(P2[i][j] - ev*V[i][j]) > 1.e-10) {
		    throw new Exception();
		}
	    }
	}

	// misc checks.
	if (sv[0] != svd.norm2())  {
	    throw new Exception();
	}

	if (Math.abs(svd.cond() - sv[0]/sv[Math.min(m,n)-1]) > 1.e-10) {
	    throw new Exception();
	}
	if (svd.rank() != 3) throw new Exception();
    }


    public static void main(String argv[]) throws Exception {
	double[][] A1 = {{2.0, 3.0, 4.0},
			 {11.0, 20.0, 40.0},
			 {21.0, 5.0, 10.0},
			 {33.0, 34.0, 35.0}};
	int m = 4;
	int n = 3;

	test(A1, m, n);
	test(MatrixOps.transpose(A1), n, m);
    }
}
