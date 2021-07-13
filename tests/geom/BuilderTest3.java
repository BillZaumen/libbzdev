import org.bzdev.geom.*;

public class BuilderTest3 {

    public static void main(String argv[]) throws Exception {
	double r = 6.0;
	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	SplinePathBuilder.CPoint[] cpoints1= new SplinePathBuilder.CPoint[37];
	double[] xs = new double[36];
	double[] ys = new double[36];
	cpoints1[0] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, r, 0.0);
	xs[0] = r;
	ys[0] = 0.0;
	for (int i = 1; i < 36; i++) {
	    double theta = Math.toRadians(i * 10.0);
	    cpoints1[i] =
		new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.SPLINE,
		 r * Math.cos(theta), r * Math.sin(theta));
	    xs[i] = r * Math.cos(theta);
	    ys[i] = r * Math.sin(theta);
	}
	cpoints1[36] =
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE);
	// apath.append(cpoints);
	spb.initPath(cpoints1);
	BasicSplinePath2D path1 = spb.getPath();
	BasicSplinePath2D path2 = new BasicSplinePath2D();
	path2.addCycle(xs, ys, 36);
	for (int i = 0; i <= 360; i++) {
	    double theta_r = Math.toRadians(1.0 * i);
	    double s = r * theta_r;
	    double u = path1.u(s);
	    double xc = path1.getX(u);
	    double yc = path1.getY(u);
	    double xc2 = path2.getX(u);
	    double yc2 = path2.getY(u);
	    double xd = r * Math.cos(theta_r);
	    double yd = r * Math.sin(theta_r);
	    if (Math.abs(xc-xd) > 1.e-4 || Math.abs(yc-yd) > 1.e-4) {
		System.out.format("path1[%d]: (%g,%g) != (%g,%g), u = %g\n",
				  i, xc, yc, xd, yd, u);
	    }
	    if (Math.abs(xc2-xd) > 1.e-4 || Math.abs(yc2-yd) > 1.e-4) {
		System.out.format("path2[%d]: (%g,%g) != (%g,%g), u = %g\n",
				  i, xc2, yc2, xd, yd, u);
	    }
	}
	System.exit(0);
    }
}
