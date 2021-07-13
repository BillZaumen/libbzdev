import org.bzdev.math.RungeKuttaMV;
import org.bzdev.util.units.MKS;

public class RKUpdateTest {

    static double m = 100.0;	// 100 kg for mass
    static double r = 5.0;	// 5 meter radius
    static double rcm = r;	//  center of mass at a radius of r.
    static double I = 100.0*r*r; // Moment of inertia.
    static double theta0 = 0.0;
    static double omega0 = 7.0/r; // 7 meters per second at a radius of r.
    static double g = MKS.gFract(1.0);
    static double hwidth = 0.5;	// half width blade.
    static double height = 0.2;	// height of blade;
    static double hc = 0.1;	// half-width of wire

    static double[] init = {theta0, I*omega0};


    static RungeKuttaMV rka = new RungeKuttaMV(2, 0.0, init) {
	    public void applyFunction(double t,
				      double[] values,
				      double[] results) {
		double theta = values[0];
		double ptheta = values[1];
		results[0] = ptheta/I;
		results[1] = -m*g*rcm*Math.sin(theta);
	    }
	};

    static RungeKuttaMV rkb = new RungeKuttaMV(2, 0.0, init) {
	    public void applyFunction(double t,
				      double[] values,
				      double[] results) {
		double theta = values[0];
		double ptheta = values[1];
		results[0] = ptheta/I;
		results[1] = -m*g*rcm*Math.sin(theta);
	    }
	};


    public static void main(String argv[]) {

	int errcount = 0;

	rka.setTolerance(1.0e-11);
	for (int i = 0; i < 1000; i++) {
	    double t = i * 0.02;
	    rka.updateTo(t);
	    rkb.updateTo(t, .0002);
	    if (Math.abs(rka.getParam() - rkb.getParam()) > 1.e-10
		|| Math.abs(rka.getValue(0) - rkb.getValue(0)) > 1.e-10
		|| Math.abs(rka.getValue(1) - rkb.getValue(1)) > 1.e-8) {
		System.out.format("t = %g, theta_rka(%g) = %s, "
				  + "theta_rkb(%g) = %s\n",
				  t, rka.getParam(), rka.getValue(0),
				  rkb.getParam(), rkb.getValue(0));
		System.out.format("        ptheta_rka = %s, ptheta_rkb = %s\n",
				  rka.getValue(1), rkb.getValue(1));
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	    System.exit(0);
	}
    }
}
