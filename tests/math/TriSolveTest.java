import org.bzdev.math.*;

public class TriSolveTest {
    public static void main(String argv[]) {
	try {
	    double[][]A = {{2.0, 1.0,  0,0, 0.0, 0.0},
			   {0.5, 3.0,  2.0, 0.0, 0.0},
			   {0.0, 0.75, 4.0, 7.0, 0.0},
			   {0.0, 0.0,  5.0, 5.0, 9.0},
			   {0.0, 0.0,  0.0, 3.0, 7.0}};
	    double a[] = {0.0, 0.5, 0.75, 5.0, 3.0};
	    double b[] = {2.0, 3.0, 4.0, 5.0, 7.0};
	    double c[] = {1.0, 2.0, 7.0, 9.0, 0.0};

	    double y[] = {20.0, 30.0, 40.0, 50.0, 60.0};
	
	    double x1[] = new double[5];
	    double x2[] = new double[5];
	    double x3[] = new double[5];
	    double yp[] = new double[5];
	    TridiagonalSolver ts = new TridiagonalSolver(a,b,c, false);
	    TridiagonalSolver.solve(x1, a, b, c, y);
	    ts.solve(x3, y);
	    for (int i = 0; i < 5; i++) {
		if (Math.abs(x3[i] - x1[i]) > 1.0e-10) {
		    for (int kk = 0; kk < 5; kk++) {
			System.out.println("x1[" + kk + "] = " + x1[kk]
					   + ", x3[" + kk + "] = "
					   + x3[kk]);
		    }
		    throw new Exception("x1 != x3 for i = " + i);
		}
	    }

	    yp[0] = x1[0]*b[0] + x1[1]*c[0];
	    for (int i = 1; i < 4; i++) {
		yp[i] = x1[i-1]*a[i] + x1[i]*b[i] + x1[i+1]*c[i];
	    }
	    yp[4] = x1[3]*a[4] + x1[4]*b[4];
	    for (int i = 4; i >= 0; i--) {
		System.out.println("y[" + i +"] = " + y[i]
				   + ", yp[" + i + "] = " + yp[i]
				   + ", x1[" + i + "] = " + x1[i]);
		if (Math.abs(y[i] - yp[i]) > 1.e-10) {
		    throw new Exception ("y[" + i +"] = " + y[i] + ", yp["
					 + i + "] = " + yp[i]);
		}
	    }
	    System.out.println("ts.solve(x1, a, b, c, y) OK");
	    TridiagonalSolver.solve(x2, A, y, 5);
	    for (int i = 0; i < 5; i++) {
		yp[i] = 0.0;
		for (int j = 0; j < 5; j++) {
		    yp[i] += A[i][j] * x2[j];
		}
	    }
	    for (int i = 0; i < 5; i++) {
		if (Math.abs(y[i] - yp[i]) > 1.e-10) {
		    throw new Exception ("y[" + i +"] = " + y[i] + ", yp["
					 + i + "] = " + yp[i]);
		}
	    }
	    System.out.println("ts.solve(x1, A, y, 4) OK");
	    System.out.println("Triadiagonal test1 succeeded");
	    System.out.println("repeating ts.solve(x1, a, b, c, y) with "
			       + "a diagonal element zeroed");
	    for (int j = 0; j < 5; j++) {
		double sv = b[j];
		b[j] = 0.0;
		ts = new TridiagonalSolver(a, b, c, false);
		TridiagonalSolver.solve(x1, a, b, c, y);
		ts.solve(x3, y);
		for (int i = 0; i < 5; i++) {
		    if (Math.abs(x3[i] - x1[i]) > 1.0e-10) {
			for (int kk = 0; kk < 5; kk++) {
			    System.out.println("x1[" + kk + "] = " + x1[kk]
					       + ", x3[" + kk + "] = "
					       + x3[kk]);
			}
			throw new Exception("x1 != x3 for i = " + i);
		    }
		}
		yp[0] = x1[0]*b[0] + x1[1]*c[0];
		for (int i = 1; i < 4; i++) {
		    yp[i] = x1[i-1]*a[i] + x1[i]*b[i] + x1[i+1]*c[i];
		}
		yp[4] = x1[3]*a[4] + x1[4]*b[4];

		for (int i = 0; i < 5; i++) {
		    yp[i] = 0.0;
		    for (int k  = 0; k < 5; k++) {
			yp[i] += A[i][k] * x2[k];
		    }
		}
		for (int i = 0; i < 5; i++) {
		    /*
		      System.out.println ("y[" + i +"] = " + y[i] + ", yp["
		      + i + "] = " + yp[i]);
		    */
		    if (Math.abs(y[i] - yp[i]) > 1.e-10) {
			throw new Exception ("y[" + i +"] = " + y[i] + ", yp["
					     + i + "] = " + yp[i]);
		    }
		}
		System.out.println("... succeeded for b[" +j +"] = 0.0");
		b[j] = sv;
	    }
	    System.out.println("repeating ts.solve(x1, a, b, c, y) with "
			       + "two diagonal elements zeroed");
	    for (int j = 0; j < 4; j++) {
		for (int k = j+1; k < 5; k++) {
		    double sv = b[j];
		    double sv2 = b[k];
		    b[j] = 0.0;
		    b[k] = 0.0;
		    ts = new TridiagonalSolver(a, b, c, false);
		    TridiagonalSolver.solve(x1, a, b, c, y);
		    ts.solve(x3, y);

		    for (int i = 0; i < 5; i++) {
			if (Math.abs(x3[i] - x1[i]) > 1.0e-10) {
			    for (int kk = 0; kk < 5; kk++) {
				System.out.println("x1[ " + kk + "] = " + x1[kk]
						   + "x3[" + kk + "] = "
						   + x3[kk]);
			    }
			    throw new Exception("x1 != x3 for i = " + i);
			}
		    }

		    yp[0] = x1[0]*b[0] + x1[1]*c[0];
		    for (int i = 1; i < 4; i++) {
			yp[i] = x1[i-1]*a[i] + x1[i]*b[i] + x1[i+1]*c[i];
		    }
		    yp[4] = x1[3]*a[4] + x1[4]*b[4];

		    for (int i = 0; i < 4; i++) {
			/*
			  System.out.println ("y[" + i +"] = " + y[i] + ", yp["
			  + i + "] = " + yp[i]);
			*/
			if (Math.abs(y[i] - yp[i]) > 1.e-10) {
			    throw new Exception ("y[" + i +"] = "
						 + y[i] + ", yp["
						 + i + "] = " + yp[i]);
			}
		    }
		    System.out.println("... succeeded for b[" +j
				       +"] = 0.0, b[" + k + "] = 0.0");
		    b[j] = sv;
		    b[k] = sv2;
		}
	    }

	    System.out.println("try tests using matrix argument");
	    for (int j = 0; j < 5; j++) {
		double sv = A[j][j];
		A[j][j] = 0.0;
		TridiagonalSolver.solve(x1, A, y, 5);
		for (int i = 0; i < 5; i++) {
		    yp[i] = 0.0;
		    for (int k = 0; k < 5; k++) {
			yp[i] += A[i][k] * x1[k];
		    }
		}
		for (int i = 0; i < 5; i++) {
		    if (Math.abs(y[i] - yp[i]) > 1.e-10) {
			throw new Exception ("y[" + i +"] = " + y[i] + ", yp["
					     + i + "] = " + yp[i]);
		    }
		}
		System.out.println("... succeeded for A[j][j] = 0 for  j = "
				   + j);
		A[j][j] = sv;
	    }
	    System.out.println("repeating ts.solve(x1, a, b, c, y) with "
			       + "two diagonal elements zeroed");
	    for (int j = 0; j < 4; j++) {
		for (int k = j+1; k < 5; k++) {
		    double sv = A[j][j];
		    double sv2 = A[k][k];
		    A[j][j] = 0.0;
		    A[k][k] = 0.0;
		    TridiagonalSolver.solve(x1,A, y, 5);
		    for (int i = 0; i < 5; i++) {
			yp[i] = 0.0;
			for (int m = 0; m < 5; m++) {
			    yp[i] += A[i][m] * x1[m];
			}
		    }
		    for (int i = 0; i < 5; i++) {
			if (Math.abs(y[i] - yp[i]) > 1.e-10) {
			    throw new Exception ("y[" + i +"] = "
						 + y[i] + ", yp["
						 + i + "] = " + yp[i]);
			}
		    }
		    System.out.println("... succeeded for A[j][j] = 0 and "
				       + " A[k][k] = 0 where j = " + j + " and "
				       + "k = " + k);
		    A[j][j] = sv;
		    A[k][k] = sv2;
		}
	    }

	    System.out.println("Triadiagonal test2 succeeded");
	    a[0] = 1.0;
	    c[4] = 1.0;
	    ts = new TridiagonalSolver(a, b, c, true, 5);
	    TridiagonalSolver.solveCyclic(x1, a, b, c, y, 5);
	    ts.solve(x3, y);

	    for (int i = 0; i < 5; i++) {
		if (Math.abs(x3[i] - x1[i]) > 1.0e-10) {
		    for (int kk = 0; kk < 5; kk++) {
			System.out.println("x1[ " + kk + "] = " + x1[kk]
					   + " x3[" + kk + "] = "
					   + x3[kk]);
		    }
		    throw new Exception("x1 != x3 for i = " + i);
		}
	    }

	    yp[0] = x1[0]*b[0] + x1[1]*c[0] + x1[4]*a[0];
	    yp[1] = x1[0]*a[1] + x1[1]*b[1] + x1[2]*c[1];
	    yp[2] = x1[1]*a[2] + x1[2]*b[2] + x1[3]*c[2];
	    yp[3] = x1[2]*a[3] + x1[3]*b[3] + x1[4]*c[3];
	    yp[4] = x1[0]*c[4] + x1[3]*a[4] + x1[4]*b[4];

	    for (int i = 4; i >- 0; i--) {
		if (Math.abs(y[i] - yp[i]) > 1.e-10) {
		    throw new Exception ("y[" + i +"] = "
					 + y[i] + ", yp["
					 + i + "] = " + yp[i]);
		}
	    }

	    System.out.println("Cyclic tridiagonal test succeeded");

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
