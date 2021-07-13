import java.awt.geom.*;
import java.util.Arrays;
import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.math.VectorOps;

public class Location3DTest {

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

	BasicSplinePath3D path1 = new BasicSplinePath3D();
	path1.moveTo(-8.0, 0.3, -7.0);
	double lx0 = -8.5, ly0 = -4.0, lz0 = -11.0;
	path1.lineTo(lx0, ly0, lz0);
	final double qx0 = 0.5;
	final double qy0 = -4.0;
	final double qz0 = -5.4;
	path1.lineTo(qx0, qy0, qz0);
	final double qx1 = 11.0, qy1 = 12.0, qz1 = 5.3;
	final double x0 = 8.0, y0 = -3.0, z0 = 3.7;
	final double x1 = 9.0, y1 = 2.0, z1 = 6.2;
	final double x2 = 6.0, y2 = 4.0, z2  = 7.3;
	final double x3 = -8.0, y3 = 5.0, z3 = 11.6;
	path1.quadTo(qx1, qy1, qz1, x0, y0, z0);
	path1.curveTo(x1, y1, z1, x2, y2, z2, x3, y3, z3);
	path1.curveTo(-2.0, 4.0, 3.3, -8.0, 4.0, 5.2, -8.0, 5.0, 6.8);

	// from https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_curves

	RealValuedFunctOps qxf =
	    (t) -> {return (1-t)*((1-t)*qx0 + t*qx1)
		    + t*((1-t)*qx1 + t*x0);};
	RealValuedFunctOps qyf =
	    (t) -> {return (1-t)*((1-t)*qy0 + t*qy1)
		    + t*((1-t)*qy1 + t*y0);};
	RealValuedFunctOps qzf =
	    (t) -> {return (1-t)*((1-t)*qz0 + t*qz1)
		    + t*((1-t)*qz1 + t*z0);};

	// from
	// http://mathfaculty.fullerton.edu/mathews/n2003/BezierCurveMod.html
	// so we have an independent test for the values.

	RealValuedFunctOps xf =
	    (t) -> {return x0 + 3*t*(x1 - x0) + 3*t*t*(x0 + x2 - 2*x1)
		    + t*t*t*(x3 - x0 + 3*x1 - 3*x2);};

	RealValuedFunctOps yf =
	    (t) -> {return y0 + 3*t*(y1-y0) + 3*t*t*(y0+y2 - 2*y1)
		    + t*t*t*(y3-y0+3*y1 - 3*y2);};

	RealValuedFunctOps zf =
	    (t) -> {return z0 + 3*t*(z1-z0) + 3*t*t*(z0+z2 - 2*z1)
		    + t*t*t*(z3-z0+3*z1 - 3*z2);};

	BasicSplinePath3D path2 = new BasicSplinePath3D();
	path2.moveTo(-8.0, 0.3, -7.0);
	path2.lineTo(lx0, ly0, lz0);
	path2.lineTo(qx0, qy0, qz0);
	path2.quadTo(qx1, qy1, qz1, x0, y0, z0);
	path2.curveTo(x1, y1, z1, x2, y2, z2, x3, y3, z3);
	path2.curveTo(-2.0, 4.0, 3.3, -8.0, 4.0, 5.2, -8.0, 5.0, 6.8);
	path2.closePath();

	double umax1 = path1.getMaxParameter();
	double umax2 = path2.getMaxParameter();
	double delta = 0.0000001;

	double array1[] = new double[5];
	double array2[] = new double[5];
	double array3[] = new double[5];
	boolean status1;
	boolean status2;
	boolean status3;
	for (int i = 0; i < 10; i++) {
	    for (int j = 0; j < 10; j++) {
		double u = i + j/10.0;
		if (u > umax1) break;
		boolean runtest2 = (u+delta <= umax1);
		BasicSplinePath3D.Location loc = path1.getLocation(u);
		BasicSplinePath3D.Location loc2 = runtest2?
		    path1.getLocation(u+delta): null;
		Point3D p1 = loc.getPoint();
		Point3D p2 = path1.getPoint(u);
		test(u, p1.getX(), p2.getX());
		test(u, p1.getY(), p2.getY());
		test(u, p1.getZ(), p2.getZ());
		test(u, loc.getX(), path1.getX(u));
		test(u, loc.getY(), path1.getY(u));
		test(u, loc.getZ(), path1.getZ(u));

		if (i == 1) {
		    double t = j / 10.0;
		    test(u, loc.getX(), (1-t)*lx0 + t * qx0);
		    test(u, loc.getY(), (1-t)*ly0 + t * qy0);
		    test(u, loc.getZ(), (1-t)*lz0 + t * qz0);
		} else if (i == 2) {
		    double t = j / 10.0;
		    test(u, loc.getX(), qxf.valueAt(t));
		    test(u, loc.getY(), qyf.valueAt(t));
		    test(u, loc.getZ(), qzf.valueAt(t));
		} else if (i == 3) {
		    double t = j / 10.0;
		    test(u, loc.getX(), xf.valueAt(t));
		    test(u, loc.getY(), yf.valueAt(t));
		    test(u, loc.getZ(), zf.valueAt(t));
		}
		    
		test(u, loc.dxDu(), path1.dxDu(u));
		if (runtest2) {
		    test2(u, loc.dxDu(), (loc2.getX() - loc.getX())/delta);
		}
		test(u, loc.dyDu(), path1.dyDu(u));
		if (runtest2) {
		    test2(u, loc.dyDu(), (loc2.getY() - loc.getY())/delta);
		}
		test(u, loc.dzDu(), path1.dzDu(u));
		if (runtest2) {
		    test2(u, loc.dzDu(), (loc2.getZ() - loc.getZ())/delta);
		}

		test(u, loc.d2xDu2(), path1.d2xDu2(u));
		test(u, loc.d2yDu2(), path1.d2yDu2(u));
		test(u, loc.d2zDu2(), path1.d2zDu2(u));
		if (runtest2) {
		    test2(u, loc.d2xDu2(), (loc2.dxDu() - loc.dxDu())/delta);
		    test2(u, loc.d2yDu2(), (loc2.dyDu() - loc.dyDu())/delta);
		    test2(u, loc.d2zDu2(), (loc2.dzDu() - loc.dzDu())/delta);
		}

		test(u, loc.dsDu(), path1.dsDu(u));
		test(u, loc.d2sDu2(), path1.d2sDu2(u));
		if (runtest2) {
		    test2(u, loc.d2sDu2(),
			  (loc2.d2sDu2() - loc.d2sDu2())/delta);
		}

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
		test(u, array1[3], array2[3]);

		// make sure tangent is (dr/du) / (ds/du) = dr/ds
		test(u, array1[1], loc.dxDu() / loc.dsDu());
		test(u, array1[2], loc.dyDu() / loc.dsDu());
		test(u, array1[3], loc.dzDu() / loc.dsDu());

		// make sure tangent has a unit length
		double dot = VectorOps.dotProduct(array1, 1, array1, 1, 3);
		test(u, dot, 1.0);


		Arrays.fill(array1, 0.0);
		Arrays.fill(array2, 0.0);
		status1 = loc.getNormal(array1, 1);
		status2 = path1.getNormal(u, array2, 1);

		if (status1 != status2) {
		    throw new Exception(status1 + " != " + status2);
		}
		test(u, array1[1], array2[1]);
		test(u, array1[2], array2[2]);
		test(u, array1[3], array2[3]);
		
		Arrays.fill(array1, 0.0);
		Arrays.fill(array2, 0.0);
		status1 = loc.getBinormal(array1, 1);
		status2 = path1.getBinormal(u, array2, 1);

		if (status1 != status2) {
		    throw new Exception(status1 + " != " + status2);
		}
		test(u, array1[1], array2[1]);
		test(u, array1[2], array2[2]);
		test(u, array1[3], array2[3]);

		Arrays.fill(array1, 0.0);
		Arrays.fill(array2, 0.0);
		status1 = loc.getTangent(array1, 0);
		status2 = loc.getNormal(array2, 0);
		status3 = loc.getBinormal(array3, 0);
		dot = VectorOps.dotProduct(array1, 0, array2, 0, 3);
		if (Math.abs(dot) > 1.e-10) {
		    throw new Exception("u = " + u + ", dot = " + dot);
		}
		dot = VectorOps.dotProduct(array1, 0, array3, 0, 3);
		if (Math.abs(dot) > 1.e-10) {
		    System.out.format("T = (%g,%g,%g)\n",
				      array1[0], array1[1], array1[2]);
		    System.out.format("B = (%g,%g,%g)\n",
				      array3[0], array3[1], array3[2]);
		    throw new Exception("u = " + u + ", dot = " + dot);
		}
		dot = VectorOps.dotProduct(array2, 0, array3, 0, 3);
		if (Math.abs(dot) > 1.e-10) {
		    System.out.format("T = (%g,%g,%g)\n",
				      array1[0], array1[1], array1[2]);
		    System.out.format("N = (%g,%g,%g)\n",
				      array2[0], array2[1], array2[2]);
		    System.out.format("B = (%g,%g,%g)\n",
				      array3[0], array3[1], array3[2]);
		    System.out.println("T dot T = "
				       + VectorOps.dotProduct(array1, 0,
							      array1, 0, 3));
		    System.out.println("N dot N = "
				       + VectorOps.dotProduct(array2, 0,
							      array2, 0, 3));
		    System.out.println("T dot N = "
				       + VectorOps.dotProduct(array1, 0,
							      array2, 0, 3));
		    System.out.println("B dot B = "
				       + VectorOps.dotProduct(array3, 0,
							      array3, 0, 3));
		    double[] xtmp1 = new double[3];
		    double[] xtmp2 = new double[3];
		    System.arraycopy(array1, 0, xtmp1, 0, 3);
		    System.arraycopy(array2, 0, xtmp2, 0, 3);
		    double[] xprod = VectorOps.crossProduct(xtmp1, xtmp2);
		    System.out.format("T X N = (%g,%g,%g)\n",
				      xprod[0], xprod[1], xprod[2]);
		    System.out.println("(T X N) dot (T X N) = "
				       + VectorOps.dotProduct(xprod, 0,
							      xprod, 0, 3));
		    throw new Exception("u = " + u + ", dot = " + dot);
		}

	    }
	}

    }
}
