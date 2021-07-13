import org.bzdev.math.*;

public class RungeKuttaMVTest {
    public static void main(String argv[]) {
	try {
	    double y0[] = {1.0};
	    RungeKuttaMV rk = new RungeKuttaMV(1, 0.0, y0) {
		    protected void applyFunction(double t, double[] y,
					    double[]result)
		    {
			result[0] = -y[0];
		    }
		};
	    RungeKuttaMV rka = new RungeKuttaMV(1, 0.0, y0) {
		    protected void applyFunction(double t, double[] y,
					    double[]result)
		    {
			result[0] = -y[0];
		    }
		};
	    double tol = 0.00000001;
	    rka.setTolerance(0, tol);

	    double times[] = {1.0, 1.1, 1.2, 1.34, 2.0, 2.00001, 2.401};
	    for (double time: times) {
		rka.updateTo(time);
		if (Math.abs(rka.getParam() - time) > 1.e-13) {
		    System.out.println("time = " + time + ", rka.getParam() = "
				       + rka.getParam());
		    throw new Exception("test failed");
		}
		if (Math.abs(rka.getValue(0) - Math.exp(-rka.getParam()))
		    > tol) {
		    System.out.println(rka.getParam() + " "
				       + rka.getValue(0) + " "
				       + Math.exp(-rka.getParam()));
		    throw new Exception("test failed");
		}
	    }
	    System.out.println("rka.minStepSize() = "
			       + rka.minStepSize());
	    for (int i = times.length; i > 0;) {
		double time = times[--i];
		rka.updateTo(time);
		if (Math.abs(rka.getParam() - time) > 1.e-13) {
		    System.out.println("time = " + time + ", rka.getParam() = "
				       + rka.getParam());
		    System.exit(1);
		}
		if (Math.abs(rka.getValue(0) - Math.exp(-rka.getParam()))
		    > tol) {
		    System.out.println(rka.getParam() + " "
				       + rka.getValue(0) + " "
				       + Math.exp(-rka.getParam()));
		    throw new Exception("test failed");

		}
		if (Math.abs(rka.getDeriv(0) + Math.exp(-rka.getParam()))
		    > tol) {
		    System.out.println(rka.getParam() + " "
				       + rka.getDeriv(0) + " "
				       + (-Math.exp(-rka.getParam())));
		    throw new Exception("test failed");
		}
	    }
	    System.out.println("rka.minStepSize() = "
			       + rka.minStepSize());

	    double incr = 0.01;
	    System.out.println(rk.getParam() + " " + rk.getValue(0) + " "
			       + Math.exp(rk.getParam()));
	    while (rk.getParam() < 10.0) {
		rk.update(incr);
		if (Math.abs(rk.getValue(0) - Math.exp(-rk.getParam()))
		    > 1.e-10) {
		    System.out.println(rk.getParam() + " "
				       + rk.getValue(0) + " "
				       + Math.exp(-rk.getParam()));
		    throw new Exception("test failed");
		}
		if (Math.abs(rk.getDeriv(0) + Math.exp(-rk.getParam()))
		    > 1.e-10) {
		    System.out.println(rk.getParam() + " "
				       + rk.getDeriv(0) + " "
				       + (-Math.exp(-rk.getParam())));
		    throw new Exception("test failed");

		}
	    }
	    System.out.println("-----------");
	    rk.setInitialValues(0.0, y0);
	    while (rk.getParam() < 10.0) {
		rk.update(1.0, 100);
		if (Math.abs(rk.getValue(0) - Math.exp(-rk.getParam()))
		    > 1.e-10) {
		    System.out.println(rk.getParam() + " "
				       + rk.getValue(0) + " "
				       + Math.exp(-rk.getParam()));
		    throw new Exception("test failed");
		}
	    }

	    System.out.println("-----------");
	    RungeKuttaMV rk1 = new RungeKuttaMV(1, 0.0, y0) {
		    protected void applyFunction(double t, double[] y,
					    double[]result)
		    {
			result[0] = -y[0];
		    }
		};
	    RungeKuttaMV rk2 = new RungeKuttaMV(1, 0.0, y0) {
		    protected void applyFunction(double t, double[] y,
					    double[]result)
		    {
			result[0] = -y[0];
		    }
		};

	    rk1.update(1.0, 100);
	    rk1.update(1.0, 100);
	    rk2.update(0.5, 50);
	    rk2.updateTo(2.0, 0.01);
	    if (Math.abs(rk1.getParam() - rk2.getParam()) > 1.e-10
		|| Math.abs(rk1.getValue(0) - rk2.getValue(0)) > 1.e-10) {
		System.out.format("rk1: t = %s, y = %s\n",rk1.getParam(),
				  rk1.getValue(0));
		System.out.format("rk2: t = %s, y = %s\n",rk2.getParam(),
				  rk2.getValue(0));
		System.out.println("rk1 and rk2 differ");
		    throw new Exception("test failed");
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}