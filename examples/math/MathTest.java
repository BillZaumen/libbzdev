import org.bzdev.math.*;

public class MathTest {
    public static void main(String argv[]) {
	try {
	    System.out.println("Integrate the product of a 5th and 7th"
			       + "order Legendre Polynomial");
	    System.out.println("from -1 to 1:");
	    GLQuadrature glq = new GLQuadrature(7) {
		    protected double function(double x) {
			return Functions.P(5,x)*Functions.P(7,x);
		    }
		};
	    System.out.println("    integral = " + glq.integrate(-1.0, 1.0)
			       + " (exact value is 0)");

	    SimpsonsRule sr = new SimpsonsRule() {
		    protected double function(double x) {
			return Functions.P(5,x)*Functions.P(7,x);
		    }
		};
	    System.out.println("    Simpson's Rule: "
			       + sr.integrate(-1.0, 1.0, 256));

	    double[] rs = new double[10];
	    int n = Functions.LegendrePolynomial.roots(10, rs);
	    System.out.println("Legendre Polynomial of order 10:");
	    for (int i = 0; i < n; i++) {
		System.out.println("  root " + (i+1) + " = " + rs[i]
				   + ", derivative at root = " 
				   + Functions.dPdx(10, rs[i]));
	    }

	    System.out.println("Root finding:");
	    RootFinder.Newton nrf = new RootFinder.Newton() {
		    public double function(double x) {
			return Math.exp(-x) - 0.5;
		    }
		    public double firstDerivative(double x) {
			return -Math.exp(-x);
		    }
		};
	    System.out.println ("    Newton: root of exp(-x) - 1/2 = "
				+ nrf.findRoot(0.0));
	    RootFinder.Halley hrf = new RootFinder.Halley() {
		    public double function(double x) {
			return Math.exp(-x) - 0.5;
		    }
		    public double firstDerivative(double x) {
			return -Math.exp(-x);
		    }
		    public double secondDerivative(double x) {
			return Math.exp(x);
		    }
		};
	    System.out.println ("    Halley: root of exp(-x) - 1/2 = "
				+ hrf.findRoot(0.0));
	    RootFinder.Brent brf = new RootFinder.Brent() {
		    public double function(double x) {
			return Math.exp(-x) - 0.5;
		    }
		};
	    System.out.println ("    Brent: root of exp(-x) - 1/2 = "
				+ nrf.findRoot(0.0, 4.0));


	    RootFinder<Integer> rf = new RootFinder.Newton<Integer>() {
		public double function(double x) {
		    return Functions.P(getParameters(), x);
		}
		public double firstDerivative(double x) {
		    return Functions.dPdx(getParameters(), x);
		}
	    };
	    rf.setParameters(10);
	    System.out.println("a root of Functions.P(10, x) is x = "
			       + rf.findRoot(-1.0));


	    System.out.println("Permutation of 10 elements with cycles");
	    System.out.println("(2, 3, 4)(5, 6, 7)(0, 1):");
	    int cycles[][] = {{2, 3, 4}, {5, 6, 7}, {0, 1}};
	    Permutation p = new Permutation(cycles, 10);

	    cycles = p.getCycles();
	    for (int i = 0; i < cycles.length; i++) {
		if (cycles[i].length > 0) {
		    System.out.print("(" + cycles[i][0]);
		    for (int j = 1; j < cycles[i].length; j++) {
			System.out.print(", " + cycles[i][j]);
		    }
		    System.out.print(")");
		}
	    }
	    System.out.println();

	    System.out.println("LU Decomposition:");
	    double[][] A = 
		{{11, 9, 24, 2},
		 {1, 5, 2, 6},
		 {3, 17, 18, 1},
		 {2, 5, 7, 1}};
	    LUDecomp lud = new LUDecomp(A);
	    double[][] matrixL = lud.getL();
	    double[][] matrixU = lud.getU();
	    double[][] matrixP = lud.getP().getMatrix();
	    double[][] inverse =
		(lud.isNonsingular()? lud.getInverse(): null);

	    double B[] = {1.0, 2.0, 3.0, 4.0};
	    double[] X = lud.solve(B);

	    System.out.println("Triadiagonal systems:");
	    double a[] = {0.0, 0.5, 0.75, 5.0, 3.0};
	    double b[] = {2.0, 3.0, 4.0, 5.0, 7.0};
	    double c[] = {1.0, 2.0, 7.0, 9.0, 0.0};
	    double y[] = {20.0, 30.0, 40.0, 50.0, 60.0};
	    double[] x = new double[5];
	    TridiagonalSolver ts = new TridiagonalSolver(a,b,c, false);
	    TridiagonalSolver.solve(x, a, b, c, y);

	    System.out.println("RungeKutta with multiple variables:");
	    double[] y0 = {0.0, 5.0};
	    RungeKuttaMV rk = new RungeKuttaMV(2, 0.0, y0) {
		    protected void applyFunction(double t, double[] y,
						 double[] yderivative)
		    {
			yderivative[0] = y[1];
			yderivative[1] = - 1.0 * y[0];
		    }
		};
	    for (int i = 0; i < 1000; i++) {
		rk.update(0.01);
		double rkx = rk.getValue(0);
		double rkp = rk.getValue(1);
		if (i % 50 == 0) {
		    System.out.println("t = " + rk.getParam()
				       + ", x = " + rkx
				       + ", p = " + rkp);
		}
	    }
	    System.out.println("RungeKutta with multiple variables and parameters:");
	    HParms parameters = new HParms();
	    parameters.m = 1.0;
	    parameters.k = 1.0;
	    RungeKuttaMV<HParms> rkhp = new RungeKuttaMV<HParms>(2, 0.0, y0) {
		protected void applyFunction(double t, double[] y,
					     double[] yderivative)
		{
		    HParms parameters = getParameters();
		    double m = parameters.m;
		    double k = parameters.k;
		    yderivative[0] = y[1] / m;
		    yderivative[1] = -k * y[0];
		}
	    };
	    rkhp.setParameters(parameters);
	    for (int i = 0; i < 1000; i++) {
		rkhp.update(0.01);
		double rkx = rkhp.getValue(0);
		double rkp = rkhp.getValue(1);
		if (i % 50 == 0) {
		    System.out.println("t = " + rkhp.getParam()
				       + ", x = " + rkx
				       + ", p = " + rkp);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
