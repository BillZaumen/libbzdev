import org.bzdev.math.*;

public class RungeKuttaTest {
    // unit-test program

    static RealValuedFunctionTwo f = new RealValuedFunctionTwo() {
	public double valueAt(double t, double y) {
	    return -y;
	}
    };


    public static void main(String argv[]) {
	try {
	    RungeKutta rk = new RungeKutta(0.0, 1.0) {
		    protected double function(double t, double y) {
			return -y;
		    }
		};

	    RungeKutta rka  = new RungeKutta(0.0, 1.0) {
		    protected double function(double t, double y) {
			return -y;
		    }
		};
	    double tol = 0.00000001;
	    rka.setTolerance(tol);

	    RungeKutta rkb = RungeKutta.newInstance((t,y)-> {return -y;},
						    0.0, 1.0);
	    rkb.setTolerance(tol);

	    RungeKutta rkf = RungeKutta.newInstance(f, 0.0, 1.0);
	    System.out.println(rk.getParam() + " " + rk.getValue() + " "
			       + Math.exp(rk.getParam()));
	    double times[] = {1.0, 1.1, 1.2, 1.34, 2.0, 2.00001, 2.401};
	    for (double time: times) {
		rka.updateTo(time);
		rkb.updateTo(time);
		if (Math.abs(rka.getParam() - time) > 1.e-13) {
		    System.out.println("time = " + time + ", rka.getParam() = "
				       + rka.getParam());
		    System.exit(1);
		}
		if (Math.abs(rkb.getParam() - time) > 1.e-13) {
		    System.out.println("time = " + time + ", rkb.getParam() = "
				       + rkb.getParam());
		    System.exit(1);
		}
		if (Math.abs(rka.getValue() - Math.exp(-rka.getParam()))
		    > tol) {
		    System.out.println(rka.getParam() + " "
				       + rka.getValue() + " "
				       + Math.exp(-rka.getParam()));
		    System.exit(1);
		}
		if (Math.abs(rkb.getValue() - Math.exp(-rkb.getParam()))
		    > tol) {
		    System.out.println(rkb.getParam() + " "
				       + rkb.getValue() + " "
				       + Math.exp(-rkb.getParam()));
		    System.exit(1);
		}
	    }
	    System.out.println("rka.mininStepSize() = "
			       + rka.minStepSize());
	    for (int i = times.length; i > 0;) {
		double time = times[--i];
		rka.updateTo(time);
		if (Math.abs(rka.getParam() - time) > 1.e-13) {
		    System.out.println("time = " + time + ", rka.getParam() = "
				       + rka.getParam());
		    System.exit(1);
		}
		if (Math.abs(rka.getValue() - Math.exp(-rka.getParam()))
		    > tol) {
		    System.out.println(rka.getParam() + " "
				       + rka.getValue() + " "
				       + Math.exp(-rka.getParam()));
		    System.exit(1);
		}
	    }
	    System.out.println("rka.mininStepSize() = "
			       + rka.minStepSize());

	    double incr = 0.01;
	    while (rk.getParam() < 10.0) {
		rk.update(incr);
		rkf.update(incr);
		if (Math.abs(rk.getParam() - rkf.getParam()) > 1.e-10
		    || Math.abs(rk.getValue() - rkf.getValue()) > 1.e-10) {
		    System.out.println("rk and rkf differ");
		    System.out.println(rk.getParam() + " " + rk.getValue() + " "
				       + Math.exp(-rk.getParam()));
		    System.exit(1);
		}
	    }
	    System.out.println("... compare rk and rkf");
	    rk.setInitialValues(0.0, 1.0);
	    rkf.setInitialValues(0.0, 1.0);
	    while (rk.getParam() < 10.0) {
		rk.update(1.0, 100);
		rkf.update(1.0, 100);
		if (Math.abs(rk.getParam() - rkf.getParam()) > 1.e-10
		    || Math.abs(rk.getValue() - rkf.getValue()) > 1.e-10) {
		    System.out.println("rk and rkf differ");
		    System.out.println(rk.getParam() + " " + rk.getValue() + " "
				       + Math.exp(-rk.getParam()));
		    System.exit(1);
		}
		if (Math.abs(rk.getDeriv() + rk.getValue()) > 1.e-10) {
		    System.out.println("derivative not equal to -y");
		}
	    }
	    System.out.println("... compare various update methods");
	    RungeKutta rk1 = new RungeKutta(0.0, 1.0) {
		    protected double function(double t, double y) {
			return -y;
		    }
		};
	    RungeKutta rk2 = new RungeKutta(0.0, 1.0) {
		    protected double function(double t, double y) {
			return -y;
		    }
		};
	    rk1.update(1.0, 100);
	    rk1.update(1.0, 100);
	    rk2.update(0.5, 50);
	    rk2.updateTo(2.0, 0.01);
	    if (Math.abs(rk1.getParam() - rk2.getParam()) > 1.e-10
		|| Math.abs(rk1.getValue() - rk2.getValue()) > 1.e-10) {
		System.out.format("rk1: t = %s, y = %s\n",rk1.getParam(),
				  rk1.getValue());
		System.out.format("rk2: t = %s, y = %s\n",rk2.getParam(),
				  rk2.getValue());
		System.out.println("rk1 and rk2 differ");
		System.exit(1);
	    }

	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.out.println("... OK");
	System.exit(0);
    }
}
