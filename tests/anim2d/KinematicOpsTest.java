import org.bzdev.anim2d.KinematicOps2D;


public class KinematicOpsTest {
    public static void main(String argv[]) {

	double s = 100;
	double v1 = 10;
	double v2 = 15;
	double a = 20.0;
	double t1 = 0.0;
	double t2 = 7.0;

	try {

	    double t = KinematicOps2D.timeGivenDVA(s, v1, a);
	    if (Math.abs(s-KinematicOps2D.distGivenTVA(t, v1, a)) > 1.e-10) {
		System.out.println("s = " + s + ", should be "
				   + KinematicOps2D.distGivenTVA(t, v1, a));
		System.exit(1);
	    }

	    double v = KinematicOps2D.vGivenDVA(s, v1, a);
	    if (Math.abs(v-(v1 + a * t)) > 1.e-10) {
		System.out.println("v (DVA) = " + v + ", should be "
				   + (v1 + a * t));
		System.exit(1);
	    }

	    if (Math.abs(v - KinematicOps2D.vGivenTVA(t, v1, a)) > 1.e-10) {
		System.out.println("v (TVA) = " + v + ", should be "
				   + (v1 + a * t));
		System.exit(1);
	    }


	    if (Math.abs(s - KinematicOps2D.distGivenTVV(t,v1, v)) > 1.e-10) {
		System.out.println("s = " + KinematicOps2D.distGivenTVV(t,v1, v)
				   + ", should be " + s);
		System.exit(1);
	    }

	    if (Math.abs(a - KinematicOps2D.accelGivenTVV(t, v1, v)) > 1.e-10) {
		System.out.println("a (TVV) = "
				   + KinematicOps2D.accelGivenTVV(t,v1, v)
				   + ", should be " + a);
		System.exit(1);
	    }

	    if (Math.abs(t - KinematicOps2D.timeGivenDVV(s, v1, v)) > 1.e-10) {
		System.out.println("t (DVV) = "
				   + KinematicOps2D.timeGivenDVV(s,v1, v)
				   + ", should be " + t);
		System.exit(1);
	    }

	    if (Math.abs(a - KinematicOps2D.accelGivenDVV(s, v1, v)) > 1.e-10) {
		System.out.println("a (DVV) = "
				   + KinematicOps2D.accelGivenDVV(s,v1, v)
				   + ", should be " + a);
		System.exit(1);
	    }

	    if (Math.abs(t - KinematicOps2D.timeGivenVVA(v1, v, a)) > 1.e-10) {
		System.out.println("t (VVA) = "
				   + KinematicOps2D.timeGivenVVA(v1, v, a)
				   + ", should be " + t);
		System.exit(1);
	    }

	    if (Math.abs(s - KinematicOps2D.distGivenVVA(v1, v, a)) > 1.e-10) {
		System.out.println("s (VVA) = "
				   + KinematicOps2D.distGivenVVA(v1, v, a)
				   + ", should be " + s);
		System.exit(1);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}