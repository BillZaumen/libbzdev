import org.bzdev.math.*;

public class RungeKutta2Test {

    static RealValuedFunctionThree f = new RealValuedFunctionThree() {
	    public double valueAt(double t0, double y, double yp) {
		return -0.5*yp - y;
	    }
	};

    public static void main(String argv[]) throws Exception {

	// test y'' + y'/2 + y = 0;
	RungeKutta2 rk2 = new RungeKutta2(0.0, 1.0, 2.0) {
		protected double function(double t, double x, double v) {
		    return -0.5*v - x;
		}
	    };

	RungeKutta2 rkf = RungeKutta2.newInstance(f, 0.0, 1.0, 2.0);

	double init[] = {1.0, 2.0};

	RungeKuttaMV rk = new RungeKuttaMV(2, 0.0, init) {
		protected void applyFunction(double t,
					     double[]y,
					     double[]results)
		{
		    results[0] = y[1];
		    results[1] = -0.5*y[1] - y[0];
		}
	    };

	double h = 0.01;
	rk.update(h);
	rk2.update(h);
	rkf.update(h);

	if (Math.abs(rk2.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rk2.getValue() - rk.getValue(0)) > 1.e-10) {
	    System.out.format("rk2.getValue() = %s, rk.getValue(0) = %s\n",
			      rk2.getValue(), rk.getValue(0));
	    throw new Exception("values don't match");
	}
	if (Math.abs(rk2.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    System.out.format("rk2.getDeriv() = %s, rk.getValue(1) = %s\n",
			      rk2.getDeriv(), rk.getValue(0));
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rkf.getValue() - rk.getValue(0)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    throw new Exception("values don't match");
	}

	rk.update(3.0, 300);
	rk2.update(3.0, 300);
	rkf.update(3.0, 300);
	if (Math.abs(rk2.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rk2.getValue() - rk.getValue(0)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rk2.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rkf.getValue() - rk.getValue(0)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    throw new Exception("values don't match");
	}

	rk.updateTo(5.0, h);
	rk2.updateTo(5.0, h);
	rkf.updateTo(5.0, h);
	if (Math.abs(rk2.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rk2.getValue() - rk.getValue(0)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rk2.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getParam() - rk.getParam()) > 1.e-10) {
	    throw new Exception("paramters (t) don't match");
	}
	if (Math.abs(rkf.getValue() - rk.getValue(0)) > 1.e-10) {
	    throw new Exception("values don't match");
	}
	if (Math.abs(rkf.getDeriv() - rk.getValue(1)) > 1.e-10) {
	    throw new Exception("values don't match");
	}

	RungeKutta2 rka = new RungeKutta2(0.0, 1.0, 0.0) {
		protected double function(double t, double x, double v) {
		    return -x;
		}
	    };

	double tol = 0.0000000001;
	rka.setTolerance(tol);

	for (int i = 0; i < 19; i++) {
	    double x = Math.PI*i/18.0;
	    rka.updateTo(x);
	    double t = rka.getParam();
	    double y = rka.getValue();
	    double yp = rka.getDeriv();
	    double ypp = rka.getSecondDeriv();
	    double cosx = Math.cos(x);
	    double sinx = Math.sin(x);
	    if (Math.abs(y - cosx) > tol) {
		System.out.format("x = %g degrees, y = %s, cos(x) = %s\n",
				  Math.toDegrees(t), y, cosx);
		throw new Exception("values don't match");
	    }
	    if (Math.abs(yp + sinx) > 3*tol) {
		System.out.format("x = %g degrees, yp = %s, -sin(x) = %s\n",
				  Math.toDegrees(t), yp, -sinx);
		throw new Exception("values don't match");
	    }
	    if (Math.abs(ypp + cosx) > tol) {
		System.out.format("x = %g degrees, ypp = %s, -cos(x) = %s\n",
				  Math.toDegrees(t), ypp, -cosx);
		throw new Exception("values don't match");
	    }
	}
	rka.updateTo(0.0);
	if (Math.abs(rka.getValue() - 1.0) > tol) {
	    System.out.format("y = %s at x = 0.0\n", rka.getValue());
	    throw new Exception("values don't match");
	}

	System.out.println("rka.minStepSize() = "
			   + rka.minStepSize());

	rka = new RungeKutta2(0.0, 1.0, 0.0) {
		protected double function(double t, double x, double v) {
		    if (t == 0.0) return -0.5 * x;
		    return - (v/t + x);
		}
	    };
	rka.setTolerance(1.e-11);

	for (int i = 0; i <= 20; i++) {
	    double t = i/10.0;
	    rka.updateTo(t);
	    if (Math.abs(rka.getValue() - Functions.J(0,t)) > 1.e-10) {
		System.out.format("t = %g, rka.getValue() = %s, J0 = %s\n",
				  t, rka.getValue(), Functions.J(0,t));

	    }
	    // if (i == 0) rka.updateTo(0.0001);
	}
    }
}
