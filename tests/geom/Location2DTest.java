import java.awt.geom.*;
import java.util.Arrays;
import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctOps;

public class Location2DTest {

    static void test(double u, double x, double y) throws Exception {
	if (x == 0.0 || y == 0.0) {
	    if (Math.abs(x-y) >= 1.e-15) {
		throw new Exception("u = " + u + ": " + x + " != " + y);
	    }
	}
    }

    static void test2(double u, double x, double y) throws Exception {
	if (x == 0.0 || y == 0.0) {
	    if (Math.abs(x-y) >= 1.e-5) {
		throw new Exception("u = " + u + ": " + x + " != " + y);
	    }
	}
    }

    public static void main(String argv[]) throws Exception {

	BasicSplinePath2D path1 = new BasicSplinePath2D();
	path1.moveTo(-8.0, 0.3);
	double lx0 = -8.5, ly0 = -4.0;
	path1.lineTo(lx0, ly0);
	final double qx0 = 0.5;
	final double qy0 = -4.0;
	path1.lineTo(qx0, qy0);
	final double qx1 = 11.0, qy1 = 12.0;
	final double x0 = 8.0, y0 = -3.0;
	final double x1 = 9.0, y1 = 2.0;
	final double x2 = 6.0, y2 = 4.0;
	final double x3 = -8.0, y3 = 5.0;
	path1.quadTo(qx1, qy1, x0, y0);
	path1.curveTo(x1, y1, x2, y2, x3, y3);
	path1.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 5.0);

	// from https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_curves

	RealValuedFunctOps qxf =
	    (t) -> {return (1-t)*((1-t)*qx0 + t*qx1)
		    + t*((1-t)*qx1 + t*x0);};
	RealValuedFunctOps qyf =
	    (t) -> {return (1-t)*((1-t)*qy0 + t*qy1)
		    + t*((1-t)*qy1 + t*y0);};

	// from
	// http://mathfaculty.fullerton.edu/mathews/n2003/BezierCurveMod.html
	// so we have an independent test for the values.

	RealValuedFunctOps xf =
	    (t) -> {return x0 + 3*t*(x1 - x0) + 3*t*t*(x0 + x2 - 2*x1)
		    + t*t*t*(x3 - x0 + 3*x1 - 3*x2);};

	RealValuedFunctOps yf =
	    (t) -> {return y0 + 3*t*(y1-y0) + 3*t*t*(y0+y2 - 2*y1)
		    + t*t*t*(y3-y0+3*y1 - 3*y2);};

	BasicSplinePath2D path2 = new BasicSplinePath2D();
	path2.moveTo(-8.0, 0.3);
	path2.lineTo(lx0, ly0);
	path2.lineTo(qx0, qy0);
	path2.quadTo(qx1, qy1, x0, y0);
	path2.curveTo(x1, y1, x2, y2, x3, y3);
	path2.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 5.0);
	path2.closePath();

	double umax1 = path1.getMaxParameter();
	double umax2 = path2.getMaxParameter();
	double delta = 0.0000001;

	double array1[] = new double[4];
	double array2[] = new double[4];
	boolean status1;
	boolean status2;
	for (int i = 0; i < 10; i++) {
	    for (int j = 0; j < 10; j++) {
		double u = i + j/10.0;
		if (u > umax1) break;
		boolean runtest2 = (u+delta <= umax1);
		BasicSplinePath2D.Location loc = path1.getLocation(u);
		BasicSplinePath2D.Location loc2 = runtest2?
		    path1.getLocation(u+delta): null;
		Point2D p1 = loc.getPoint();
		Point2D p2 = path1.getPoint(u);
		test(u, p1.getX(), p2.getX());
		test(u, p1.getY(), p2.getY());
		test(u, loc.getX(), path1.getX(u));
		test(u, loc.getY(), path1.getY(u));

		if (i == 1) {
		    double t = j / 10.0;
		    test(u, loc.getX(), (1-t)*lx0 + t * qx0);
		    test(u, loc.getY(), (1-t)*ly0 + t * qy0);
		} else if (i == 2) {
		    double t = j / 10.0;
		    test(u, loc.getX(), qxf.valueAt(t));
		    test(u, loc.getY(), qyf.valueAt(t));
		} else if (i == 3) {
		    double t = j / 10.0;
		    test(u, loc.getX(), xf.valueAt(t));
		    test(u, loc.getY(), yf.valueAt(t));
		}
		    
		test(u, loc.dxDu(), path1.dxDu(u));
		if (runtest2) {
		    test2(u, loc.dxDu(), (loc2.getX() - loc.getX())/delta);
		}
		test(u, loc.dyDu(), path1.dyDu(u));
		if (runtest2) {
		    test2(u, loc.dyDu(), (loc2.getY() - loc.getY())/delta);
		}

		test(u, loc.d2xDu2(), path1.d2xDu2(u));
		test(u, loc.d2yDu2(), path1.d2yDu2(u));

		if (runtest2) {
		    test2(u, loc.d2xDu2(), (loc2.dxDu() - loc.dxDu())/delta);
		    test2(u, loc.d2yDu2(), (loc2.dyDu() - loc.dyDu())/delta);
		}

		test(u, loc.dsDu(), path1.dsDu(u));
		test(u, loc.d2sDu2(), path1.d2sDu2(u));

		if (loc.curvatureExists() != path1.curvatureExists(u)) {
		    throw new Exception("u = " + u + loc.curvatureExists()
					+ " != " + path1.curvatureExists(u));
		}
		if (loc.curvatureExists()) {
		    test(u, loc.curvature(), path1.curvature(u));
		}

		status1 = loc.getTangent(array1, 1);
		status2 = path1.getTangent(u, array2, 1);
		if (status1 != status2) {
		    throw new Exception(status1 + " != " + status2);
		}
		test(u, array1[1], array2[1]);
		test(u, array1[2], array2[2]);

		Arrays.fill(array1, 0.0);
		Arrays.fill(array2, 0.0);

		status1 = loc.getNormal(array1, 1);
		status2 = path1.getNormal(u, array2, 1);
		if (status1 != status2) {
		    throw new Exception(status1 + " != " + status2);
		}
		test(u, array1[1], array2[1]);
		test(u, array1[2], array2[2]);
	    }
	}

    }
}
