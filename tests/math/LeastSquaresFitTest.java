import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import org.bzdev.math.StaticRandom;

import java.util.Arrays;

public class LeastSquaresFitTest {
    static int n = 1000000;
    static int nl = 10000;
    static double function(double x) {
	return 10.0 + 2.0*x + 3.0 * x*x;
    }

    static RealValuedFunction f0 = new RealValuedFunction() {
	    public double valueAt(double arg) {
		return 1.0;
	    }
	    public double derivAt(double arg) {
		return 0.0;
	    }
	    public double secondDerivAt(double arg) {
		return 0.0;
	    }
	};
    static RealValuedFunctOps g0 = (x) -> 1.0;

    static RealValuedFunction f1 = new RealValuedFunction() {
	    public double valueAt(double arg) {
		return arg;
	    }
	    public double derivAt(double arg) {
		return 1.0;
	    }
	    public double secondDerivAt(double arg) {
		return 0.0;
	    }
	};
    static RealValuedFunctOps g1 = (x) -> x;


    static RealValuedFunction f2 = new RealValuedFunction() {
	    public double valueAt(double arg) {
		return arg*arg;
	    }
	    public double derivAt(double arg) {
		return 2.0*arg;
	    }
	    public double secondDerivAt(double arg) {
		return 2.0;
	    }
	};
    static RealValuedFunctOps g2 = (x) -> x*x;


    static RealValuedFunction fe = new RealValuedFunction() {
	    double alpha = 10.0;
	    double beta = 0.5;
	    public double valueAt(double x) {
		return alpha + Math.exp(-beta*x);
	    }
	    public double derivAt(double x) {
		return -beta*Math.exp(-beta*x);
	    }
	    public double secondDerivAt(double x) {
		return beta*beta*Math.exp(-beta*x);
	    }
	};

    static RealValuedFunctionVA rfe = new RealValuedFunctionVA(3,3) {
	    public double valueAt(double... args) {
		double x = args[0];
		double alpha = args[1];
		double beta = args[2];
		return alpha + Math.exp(-beta * x);
	    }
	    public double derivAt(int i, double... args) {
		double x = args[0];
		double alpha = args[1];
		double beta = args[2];
		switch (i) {
		case 0:
		    return -beta * Math.exp(-beta * x);
		case 1:
		    return 1.0;
		case 2:
		    return -x * Math.exp(-beta * x);
		default:
		    throw new IllegalArgumentException("i=" + i);
		}
	    }

	    public double secondDerivAt(int i, int j, double ... args) {
		double x = args[0];
		double alpha = args[1];
		double beta = args[2];
		switch (i) {
		case 0:
		    switch (j) {
		    case 0:
			return beta * beta * Math.exp(-beta * x);
		    case 1:
			return 0.0;
		    case 2:
			return x* beta * Math.exp(-beta * x);
		    default:
			throw new IllegalArgumentException();
		    }
		case 1:
		    return 0.0;
		case 2:
		    switch (j) {
		    case 0:
			return x * beta * Math.exp(-beta * x);
		    case 1:
			return 0.0;
		    case 2:
			return x * x * Math.exp(-beta * x);
		    default:
			throw new IllegalArgumentException();
		    }
		default:
		    throw new IllegalArgumentException();
		}
	    }
	};



    public static void main(String argv[]) throws Exception {

	StaticRandom.maximizeQuality();

	double ys[] = new double[n];
	double xs[] = new double[n];
	double ysl[] = new double[nl];
	double xsl[] = new double[nl];
	GaussianRV rvy = new GaussianRV(0.0, 3.0);

	Adder adder = new Adder.Kahan();
	for (int i = 0; i < n; i++) {
	    double x = ((double)i)/(n/10);
	    xs[i] = x;
	    ys[i] = function(x) + rvy.next();
	    double term = ys[i] - function(x);
	    adder.add(term*term);
	}

	for (int i = 0; i < nl; i++) {
	    double x = ((double)i)/(nl/10);
	    xsl[i] = x;
	    ysl[i] = function(x);
	}

	System.out.println("actual variance for y is " + (adder.getSum()/n));
	System.out.println("xs[500] = " + xs[500]);
	System.out.println("ys[500] = " + ys[500]);
	
	LeastSquaresFit.Linear lsf = new LeastSquaresFit.Polynomial(2, xs, ys);

	System.out.println("lsf.valueAt(5.0) = " + lsf.valueAt(5.0)
			    + ", function(5.0) = " + function(5.0));

	double[] cs = lsf.getParameters();

	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	double[][] cov = lsf.getCovarianceMatrix();


	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	System.out.println();
	System.out.println("Now try with sigma specified explicitly");
	System.out.println();

	lsf = new LeastSquaresFit.Polynomial(2, xs, ys, 3.0);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();
	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	System.out.println();
	System.out.println("Now try with a sigma array, all set to 3");
	System.out.println();

	double[] sigmas = new double[n];
	Arrays.fill(sigmas, 3.0);

	lsf = new LeastSquaresFit.Polynomial(2, xs, ys, sigmas);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();
	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	System.out.println();
	System.out.println("NOW TRY FUNCION BASIS CASES");
	System.out.println();

	lsf = new LeastSquaresFit.FunctionBasis(xs, ys, f0, f1, f2);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();

	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}
	lsf = new LeastSquaresFit.FunctionBasis(xs, ys, g0, g1, g2);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();

	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	RealValuedFunctionVA.Linear f =
	    new RealValuedFunctionVA.Linear(f0, f1, f2);

	LeastSquaresFit lsf2 =
	    new LeastSquaresFit.FunctionBasis(xs, ys, f);
	double[] cs2 = lsf2.getParameters();
	double[][] cov2 = lsf2.getCovarianceMatrix();
	for (int i = 0; i < cs2.length; i++) {
	    if (Math.abs(cs[i] - cs2[i]) > 1.e-9) {
		System.out.format("cs[%d] = %s, cs2[%d] = %s\n",
				  i, cs[i], i, cs2[i]);
		throw new Exception("cs and cs2 differ");
	    }
	}
	for (int i = 0; i < cov2.length; i++) {
	    for (int j = 0; j < cov2[i].length; j++) {
		if (Math.abs(cov[i][j] - cov2[i][j]) > 1.e-9) {
		    System.out.format("cov[%d][%d] = %s, cov2[%d][%d] = %s\n",
				      i, j, cov[i][j], i, j, cov2[i][j]);
		    throw new Exception("cov and cov2 differ");
		}
	    }
	}

	System.out.println(" .... test a non-linear fit");
	LeastSquaresFit lsf3 =
	    new LeastSquaresFit.NonLinear(xs, ys, f, null);

	double[] cs3 = lsf3.getParameters();
	double[][] cov3 = lsf3.getCovarianceMatrix();
	for (int i = 0; i < cs3.length; i++) {
	    if (Math.abs((cs[i] - cs3[i])/cs[i]) > 3.e-2) {
		System.out.format("cs[%d] = %s, cs3[%d] = %s\n",
				  i, cs[i], i, cs3[i]);
		throw new Exception("cs and cs3 differ");
	    }
	}

	for (int i = 0; i < cov3.length; i++) {
	    for (int j = 0; j < cov3[i].length; j++) {
		if (Math.abs((cov[i][j] - cov3[i][j])/cov[i][j]) > 1.e-3) {
		    System.out.format("cov[%d][%d] = %s, cov3[%d][%d] = %s\n",
				      i, j, cov[i][j], i, j, cov3[i][j]);
		    throw new Exception("cov and cov3 differ");
		}
	    }
	}

	System.out.println(" .... test a non-linear fit using a guess");
	double[] guess = new double[3];
	LeastSquaresFit lsf3a =
	    new LeastSquaresFit.NonLinear(xs, ys, f, null, guess);

	double[] cs3a = lsf3a.getParameters();
	double[][] cov3a = lsf3a.getCovarianceMatrix();
	for (int i = 0; i < cs3.length; i++) {
	    if (Math.abs((cs[i] - cs3a[i])/cs[i]) > 3.e-2) {
		System.out.format("cs[%d] = %s, cs3a[%d] = %s\n",
				  i, cs[i], i, cs3a[i]);
		throw new Exception("cs and cs3a differ");
	    }
	}


	for (int i = 0; i < cov3.length; i++) {
	    for (int j = 0; j < cov3[i].length; j++) {
		if (Math.abs((cov[i][j] - cov3a[i][j])/cov[i][j]) > 1.e-3) {
		    System.out.format("cov[%d][%d] = %s, cov3a[%d][%d] = %s\n",
				      i, j, cov[i][j], i, j, cov3[i][j]);
		    throw new Exception("cov and cov3a differ");
		}
	    }
	}

	System.out.println("try a function basis with sigma = 3.0");

	lsf = new LeastSquaresFit.FunctionBasis(xs, ys, 3.0, f0, f1, f2);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();
	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	System.out.println("Now try with a sigma array, all set to 3");
	System.out.println();

	lsf = new LeastSquaresFit.FunctionBasis(xs, ys, sigmas, f0, f1, f2);
	cs = lsf.getParameters();
	for (int i = 0; i < cs.length; i++) {
	    System.out.println("coefficient[" + i + "] = " +cs[i]);
	}
	cov = lsf.getCovarianceMatrix();
	System.out.println("covariance matrix:");
	for (int i = 0; i < cov.length; i++) {
	    for (int j = 0; j < cov.length; j++) {
		System.out.format("%12.6g ", cov[i][j]);
	    }
	    System.out.println();
	}

	lsf2 = new LeastSquaresFit.FunctionBasis(xs, ys, 3.0, f);
	cs2 = lsf2.getParameters();
	cov2 = lsf2.getCovarianceMatrix();
	for (int i = 0; i < cs2.length; i++) {
	    if (Math.abs(cs[i] - cs2[i]) > 1.e-9) {
		System.out.format("cs[%d] = %s, cs2[%d] = %s\n",
				  i, cs[i], i, cs2[i]);
		throw new Exception("cs and cs2 differ");
	    }
	}
	for (int i = 0; i < cov2.length; i++) {
	    for (int j = 0; j < cov2[i].length; j++) {
		if (Math.abs(cov[i][j] - cov2[i][j]) > 1.e-10) {
		    throw new Exception("cov and cov2 differ");
		}
	    }
	}

	lsf2 = new LeastSquaresFit.FunctionBasis(xs, ys, sigmas, f);
	cs2 = lsf2.getParameters();
	cov2 = lsf2.getCovarianceMatrix();
	for (int i = 0; i < cs2.length; i++) {
	    if (cs[i] != cs2[i]) {
		throw new Exception("cs and cs2 differ");
	    }
	}
	for (int i = 0; i < cov2.length; i++) {
	    for (int j = 0; j < cov2[i].length; j++) {
		if (cov[i][j] != cov2[i][j]) {
		    throw new Exception("cov and cov2 differ");
		}
	    }
	}

	ys = new double[100];
	xs = new double[100];
	rvy = new GaussianRV(0.0, 0.1);
	sigmas = new double[100];
	for (int i = 0; i < 100; i++) {
	    double x = i / 50.0;
	    xs[i] = x;
	    ys[i] = fe.valueAt(x) + rvy.next();
	    sigmas[i] = 0.1;
	}

	guess = new double[2];
	LeastSquaresFit
	    lsfnl = new LeastSquaresFit.NonLinear(xs, ys, 0.1, rfe, null);
	LeastSquaresFit
	    lsfnla = new LeastSquaresFit.NonLinear(xs, ys, 0.1, rfe, null,
						  guess);
	double[] parameters = lsfnl.getParameters();
	System.out.println("fitted alpha = " + parameters[0]);
	System.out.println("fitted beta = " + parameters[1]);

	double[] parametersa = lsfnla.getParameters();
	System.out.println("fitted alpha (with guess) = " + parametersa[0]);
	System.out.println("fitted beta (with guess) = " + parametersa[1]);

	System.out.println("Now use an array for the standard deviations ...");
	lsfnl = new LeastSquaresFit.NonLinear(xs, ys, sigmas, rfe, null);
	lsfnla = new LeastSquaresFit.NonLinear(xs, ys, sigmas, rfe, null,
						  guess);
	parameters = lsfnl.getParameters();
	System.out.println("fitted alpha = " + parameters[0]);
	System.out.println("fitted beta = " + parameters[1]);

	parametersa = lsfnla.getParameters();
	System.out.println("fitted alpha (with guess) = " + parametersa[0]);
	System.out.println("fitted beta (with guess) = " + parametersa[1]);

	System.out.println("covariance test:");

	ys = new double[100];
	xs = new double[100];
	rvy = new GaussianRV(0.0, 0.1);
	for (int i = 0; i < 100; i++) {
	    double x = i / 50.0;
	    xs[i] = x;
	    ys[i] = 1.0 + 3.0*x + 2.0*x*x + rvy.next();
	}

	LeastSquaresFit fit1 =
	    new LeastSquaresFit.Polynomial(2, xs, ys, 1.0);

	LeastSquaresFit fit2 =
	    new LeastSquaresFit.FunctionBasis(xs, ys, 1.0, f0, f1, f2);

	LeastSquaresFit fit3 =
	    new LeastSquaresFit.FunctionBasis(xs, ys, 1.0, f);

	LeastSquaresFit fit4 =
	    new LeastSquaresFit.NonLinear(xs, ys, 1.0, f, null);

	LeastSquaresFit fit5 =
	    new LeastSquaresFit.BSpline(3, 20, xs, ys, 1.0);

	for (int i = 0; i < 100; i++) {
	    double x = i / 50.0;
	    double y = 1.0 + 3.0*x + 2.0*x*x;
	    double y1 = fit1.valueAt(x);
	    double y2 = fit2.valueAt(x);
	    double y3 = fit3.valueAt(x);
	    double y4 = fit4.valueAt(x);
	    double y5 = fit5.valueAt(x);
	    if (Math.abs(y2-y)> 1.e-1
		|| Math.abs(y3-y)> 1.e-1
		|| Math.abs(y4-y)> 1.e-1
		|| Math.abs(y5-y)> 2.e-1) {
		System.out.format("at x = %g, y = %g: %g %g %g %g %g\n", x, y,
				  y1,  y2, y3, y4, y5);
	    }
	}

	guess = new double[3];
	LeastSquaresFit fit4a =
	    new LeastSquaresFit.NonLinear(xs, ys, 1.0, f, null, guess);

	for (int i = 0; i < 100; i++) {
	    double x = i / 50.0;
	    double y = 1.0 + 3.0*x + 2.0*x*x;
	    double y1 = fit1.valueAt(x);
	    double y2 = fit2.valueAt(x);
	    double y3 = fit3.valueAt(x);
	    double y4 = fit4a.valueAt(x);
	    double y5 = fit5.valueAt(x);
	    if (Math.abs(y2-y)> 1.e-1
		|| Math.abs(y3-y)> 1.e-1
		|| Math.abs(y4-y)> 1.e-1
		|| Math.abs(y5-y)> 2.e-1) {
		System.out.format("at x = %g, y = %g: %g %g %g %g %g\n", x, y,
				  y1,  y2, y3, y4, y5);
	    }
	}

	double ctargs[][] = {{0.2, 0.2},
			     {0.2, 0.4},
			     {0.5, 0.5},
			     {0.5, 0.2},
			     {0.3, 1.3}};

	double cdelta = 1.e-10;
	for (double[] pair: ctargs) {
	    double c1 = fit1.covariance(pair[0], pair[1]);
	    double c2 = fit2.covariance(pair[0], pair[1]);
	    double c3 = fit3.covariance(pair[0], pair[1]);
	    double c4 = fit4.covariance(pair[0], pair[1]);
	    double c5 = fit5.covariance(pair[0], pair[1]);

	    if (Math.abs(c2 - c1) > cdelta
		|| Math.abs(c3 - c1) > cdelta
		|| Math.abs(c4 - c1) > cdelta) {
		System.out.format("covariance(%g,%g): %g %g %g %g\n",
				  pair[0], pair[1], c1, c2, c3, c4);
		throw new Exception("covariance method failed");
	    }

	}

	System.exit(0);
    }
}
