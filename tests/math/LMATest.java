import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import org.bzdev.math.StaticRandom;


public class LMATest {

    static RealValuedFunction f = new RealValuedFunction() {
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

    static RealValuedFunctionVA rf = new RealValuedFunctionVA(4,4) {
	    public double valueAt(double... args) {
		double y = args[0];
		double x = args[1];
		double alpha = args[2];
		double beta = args[3];
		return alpha + Math.exp(-beta * x)  - y;
	    }
	    public double derivAt(int i, double... args) {
		double y = args[0];
		double x = args[1];
		double alpha = args[2];
		double beta = args[3];
		switch (i) {
		case 0:
		    return -1.0;
		case 1:
		    return -beta * Math.exp(-beta * x);
		case 2:
		    return 1.0;
		case 3:
		    return -x * Math.exp(-beta * x);
		default:
		    throw new IllegalArgumentException("i=" + i);
		}
	    }

	    public double secondDerivAt(int i, int j, double ... args) {
		double y = args[0];
		double x = args[1];
		double alpha = args[2];
		double beta = args[3];
		switch (i) {
		case 0:
		    return 0.0;
		case 1:
		    switch (j) {
		    case 0:
			return 0.0;
		    case 1:
			return beta * beta * Math.exp(-beta * x);
		    case 2:
			return 0.0;
		    case 3:
			return x * beta * Math.exp(-beta * x);
		    default:
			throw new IllegalArgumentException();
		    }
		case 2:
		    return 0.0;
		case 3:
		    switch (j) {
		    case 0:
			return 0.0;
		    case 1:
			return x * beta * Math.exp(-beta * x);
		    case 2:
			return 0.0;
		    case 3:
			return x * x * Math.exp(-beta * x);
		    default:
			throw new IllegalArgumentException();
		    }
		default:
		    throw new IllegalArgumentException();
		}
	    }

	};

    static RealValuedFunctionVA rf2 = new RealValuedFunctionVA(3,3) {
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

    static RealValuedFunctionVA  fl =
	    new RealValuedFunctionVA.Linear(f0, f1, f2);



    public static void main(String argv[]) throws Exception {

	double[] xs = {0.0, 0.2, 0.4, 0.5, 0.8, 1.0, 1.3, 1.5, 1.7, 2.0};
	double[] ys = new double[xs.length];
	double sigma = 0.1;
	double[] sigmas = new double[xs.length];
	for (int i = 0; i < xs.length; i++) {
	    ys[i] = f.valueAt(xs[i]);
	    sigmas[i] = sigma;
	}

	System.out.println("try case where data exactly fits");

	double[] guess = {1.0, 1.0};
	double[] parms = new double[2];
	System.arraycopy(guess, 0, parms, 0, 2);

	System.out.println(" ... test NORMAL mode");

	double sumsq = LMA.findMin(rf, parms, 5.0, 1.3, 0.0001, 0, ys, xs);
	double sumsq2 = LMA.sumSquares(rf, ys, xs, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    throw new Exception("sumsq != sumsq2");
	}
	if (Math.abs(sumsq) > 1.e-10) {
	    System.out.println("sumsq = " + sumsq );
	    throw new Exception("sumsq too high");
	}
	if (Math.abs(parms[0] - 10.0) > 1.e-10
	    || Math.abs(parms[1] - 0.5) > 1.e-10) {
	    System.out.println("alpha = " + parms[0]);
	    System.out.println("beta = " + parms[1]);
	    throw new Exception("Did not converge to the correct values");
	}

	System.out.println(" ... test LEAST_SQUARES mode");
	System.arraycopy(guess, 0, parms, 0, 2);

	// make sure the two real-valued functions give the same sum of squares
	sumsq = LMA.sumSquares(rf, ys, xs, parms);
	sumsq2 = LMA.sumSquares(rf2, LMA.Mode.LEAST_SQUARES, ys, xs, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    System.out.format("sumsq = %s, sumsq2 = %s\n", sumsq, sumsq2);
	    throw new Exception("sumsq and sumsq2 differ");
	}

	sumsq = LMA.findMin(rf2, LMA.Mode.LEAST_SQUARES,
			    parms, 5.0, 1.3, 0.0001, 0, ys, xs);
	sumsq2 = LMA.sumSquares(rf2, LMA.Mode.LEAST_SQUARES, ys, xs, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    throw new Exception("sumsq != sumsq2");
	}
	if (Math.abs(sumsq) > 1.e-10) {
	    System.out.println("sumsq = " + sumsq );
	    throw new Exception("sumsq too high");
	}
	if (Math.abs(parms[0] - 10.0) > 1.e-10
	    || Math.abs(parms[1] - 0.5) > 1.e-10) {
	    System.out.println("alpha = " + parms[0]);
	    System.out.println("beta = " + parms[1]);
	    throw new Exception("Did not converge to the correct values");
	}

	System.out.println(" ... test WEIGHTEDLEAST_SQUARES mode");
	System.arraycopy(guess, 0, parms, 0, 2);

	// make sure the two real-valued functions give the same sum of squares
	sumsq = LMA.sumSquares(rf, ys, xs, parms)/(sigma*sigma);
	sumsq2 = LMA.sumSquares(rf2, LMA.Mode.WEIGHTED_LEAST_SQUARES,
				ys, sigmas, xs, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    System.out.format("sumsq = %s, sumsq2 = %s\n", sumsq, sumsq2);
	    throw new Exception("sumsq and sumsq2 differ");
	}

	sumsq = LMA.findMin(rf2, LMA.Mode.WEIGHTED_LEAST_SQUARES,
			    parms, 5.0, 1.3, 0.0001, 0, ys, sigmas, xs);
	sumsq2 = LMA.sumSquares(rf2, LMA.Mode.WEIGHTED_LEAST_SQUARES,
				ys, sigmas, xs, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    throw new Exception("sumsq != sumsq2");
	}
	if (Math.abs(sumsq) > 1.e-10) {
	    System.out.println("sumsq = " + sumsq );
	    throw new Exception("sumsq too high");
	}
	if (Math.abs(parms[0] - 10.0) > 1.e-10
	    || Math.abs(parms[1] - 0.5) > 1.e-10) {
	    System.out.println("alpha = " + parms[0]);
	    System.out.println("beta = " + parms[1]);
	    throw new Exception("Did not converge to the correct values");
	}

	System.out.println(" ... 2nd test of  WEIGHTEDLEAST_SQUARES mode");
	System.arraycopy(guess, 0, parms, 0, 2);

	double[] xs2 = new double[xs.length+1];
	double[] ys2 = new double[ys.length+1];
	double[] sigmas2 = new double[sigmas.length+1];
	System.arraycopy(xs, 0, xs2, 0, xs.length);
	System.arraycopy(ys, 0, ys2, 0, ys.length);
	System.arraycopy(sigmas, 0, sigmas2, 0, sigmas.length);
	xs2[xs.length] = 2.5;
	ys2[ys.length] = 15.0;
	sigmas2[sigmas.length] = 20.0;

	sumsq = LMA.findMin(rf2, LMA.Mode.WEIGHTED_LEAST_SQUARES,
			    parms, 5.0, 1.3, 0.0001, 0, ys2, sigmas2, xs2);
	sumsq2 = LMA.sumSquares(rf2, LMA.Mode.WEIGHTED_LEAST_SQUARES,
				ys2, sigmas2, xs2, parms);
	if (Math.abs(sumsq - sumsq2) > 1.e-10) {
	    throw new Exception("sumsq != sumsq2");
	}
	if (Math.abs(parms[0] - 10.0) > 1.e-3
	    || Math.abs(parms[1] - 0.5) > 1.e-3) {
	    System.out.println("alpha = " + parms[0]);
	    System.out.println("beta = " + parms[1]);
	    throw new Exception("Did not converge to the correct values");
	}

	System.out.println();
	System.out.println("add random noise "
			   + "(expect alpha ~= 10.0, beta ~= 0.5)");

	StaticRandom.maximizeQuality();

	GaussianRV grv = new GaussianRV(0.0, 0.1);
	ys2 = new double[ys.length];
	double sumAlpha = 0.0;
	double sumBeta = 0.0;
	double sumAlphaSq = 0.0;
	double sumBetaSq = 0.0;
	int n = 10000;
	for (int j = 0; j < n; j++ ) {
	    for (int i = 0; i < ys.length; i++) {
		ys2[i] = ys[i] + grv.next();
	    }

	    System.arraycopy(guess, 0, parms, 0, 2);
	    sumsq = LMA.findMin(rf, parms, 5.0, 1.3, 0.0001, 0, ys2, xs);
	    sumAlpha += parms[0];
	    sumBeta +=  + parms[1];
	    sumAlphaSq += parms[0]*parms[0];
	    sumBetaSq += parms[1]*parms[1];
	}
	double alpha = sumAlpha/n;
	double beta = sumBeta/n;
	double sigmaAlpha = Math.sqrt((sumAlphaSq - sumAlpha*sumAlpha/n)/n);
	double sigmaBeta = Math.sqrt((sumBetaSq - sumBeta*sumBeta/n)/n);
	
	parms[0] = alpha;
	parms[1] = beta;

	System.out.println("    ... alpha = " + parms[0]
			   + " +- " + sigmaAlpha);
	System.out.println("    ... beta = " + parms[1]
			   + " +- " + sigmaBeta);

	System.out.println("  ... least squares mode");

	sumAlpha = 0.0;
	sumBeta = 0.0;
	sumAlphaSq = 0.0;
	sumBetaSq = 0.0;
 	for (int j = 0; j < n; j++ ) {
	    for (int i = 0; i < ys.length; i++) {
		ys2[i] = ys[i] + grv.next();
	    }

	    System.arraycopy(guess, 0, parms, 0, 2);
	    sumsq = LMA.findMin(rf2, LMA.Mode.LEAST_SQUARES,
				parms, 5.0, 1.3, 0.0001, 0,
				ys2, xs);
	    sumAlpha += parms[0];
	    sumBeta +=  + parms[1];
	    sumAlphaSq += parms[0]*parms[0];
	    sumBetaSq += parms[1]*parms[1];
	}
	alpha = sumAlpha/n;
	beta = sumBeta/n;
	sigmaAlpha = Math.sqrt((sumAlphaSq - sumAlpha*sumAlpha/n)/n);
	sigmaBeta = Math.sqrt((sumBetaSq - sumBeta*sumBeta/n)/n);
	
	parms[0] = alpha;
	parms[1] = beta;

	System.out.println("    ... alpha = " + parms[0]
			   + " +- " + sigmaAlpha);
	System.out.println("    ... beta = " + parms[1]
			   + " +- " + sigmaBeta);

	System.out.println("  ... weighted least squares mode");

	sumAlpha = 0.0;
	sumBeta = 0.0;
	sumAlphaSq = 0.0;
	sumBetaSq = 0.0;
 	for (int j = 0; j < n; j++ ) {
	    for (int i = 0; i < ys.length; i++) {
		ys2[i] = ys[i] + grv.next();
		sigmas[i] += grv.next()/10.0;
	    }

	    System.arraycopy(guess, 0, parms, 0, 2);
	    sumsq = LMA.findMin(rf2, LMA.Mode.LEAST_SQUARES,
				parms, 5.0, 1.3, 0.0001, 0,
				ys2, xs);
	    sumAlpha += parms[0];
	    sumBeta +=  + parms[1];
	    sumAlphaSq += parms[0]*parms[0];
	    sumBetaSq += parms[1]*parms[1];
	}
	alpha = sumAlpha/n;
	beta = sumBeta/n;
	sigmaAlpha = Math.sqrt((sumAlphaSq - sumAlpha*sumAlpha/n)/n);
	sigmaBeta = Math.sqrt((sumBetaSq - sumBeta*sumBeta/n)/n);
	
	parms[0] = alpha;
	parms[1] = beta;

	System.out.println("    ... alpha = " + parms[0]
			   + " +- " + sigmaAlpha);
	System.out.println("    ... beta = " + parms[1]
			   + " +- " + sigmaBeta);

	double[] xls = {0.0, 1.0, 2.0, 5.0, 7.0, 9.0, 10.0};
	double[] yls = new double[xls.length];
	for (int i = 0;i < xls.length; i++) {
	    yls[i] = function(xls[i]);
	}
	System.out.println("fl.minArgLenth() = " + fl.minArgLength());
	double[] guessl = {0.0, 0.0, 0.0};
	double[] guessl2 = {10.0, 2.0, 3.0};
	double[] parmsl = new double[3];
	System.arraycopy(guessl, 0, parmsl, 0, 3);
	sumsq = LMA.findMin(fl, LMA.Mode.LEAST_SQUARES, parmsl,
			    5.0, 1.3, 0.0001, 0, yls, xls);
	System.out.println(sumsq);
	System.out.format("parml = %s, %s, %s\n",
			  parmsl[0], parmsl[1], parmsl[2]);
	System.out.println("now try randomized values, with a perfect guess");
	System.arraycopy(guessl2, 0, parmsl, 0, 3);
	GaussianRV rv = new GaussianRV(0.0, 0.1);
	for (int i = 0; i < yls.length; i++) {
	    yls[i] += rv.next();
	}
	sumsq = LMA.findMin(fl, LMA.Mode.LEAST_SQUARES, parmsl,
			    5.0, 1.3, 0.0001, 0, yls, xls);
	System.out.println(sumsq);
	System.out.format("parml = %s, %s, %s\n",
			  parmsl[0], parmsl[1], parmsl[2]);
	System.out.println("... not perfect guess");
	System.arraycopy(guessl, 0, parmsl, 0, 3);
	sumsq = LMA.findMin(fl, LMA.Mode.LEAST_SQUARES, parmsl,
			    5.0, 1.3, 0.0001, 0, yls, xls);
	System.out.println(sumsq);
	System.out.format("parml = %s, %s, %s\n",
			  parmsl[0], parmsl[1], parmsl[2]);

   }
}
