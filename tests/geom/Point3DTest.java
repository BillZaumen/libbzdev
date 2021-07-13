import org.bzdev.geom.*;

public class Point3DTest {
    public static void main(String argv[]) throws Exception {

	double x = 10.0;
	double y = 30.0;
	double z = 40.0;

	System.out.println("Check Point3D.Double");
	Point3D pt1 = new Point3D.Double();
	pt1.setLocation(x, y, z);
	Point3D pt2 = new Point3D.Double(x, y, z);
	
	if (!pt1.equals(pt2)) {
	    System.out.println("pt1 != pt2");
	    System.exit(1);
	}
	if (pt1.hashCode() != pt2.hashCode()) {
	    System.out.format("pt1 & pt2 hashcodes differ: %g != %g\n",
			      pt1.hashCode(), pt2.hashCode());
	    System.exit(1);
	}

	if (pt1.getX() != x) {
	    System.out.format("pt1.getX() = %g, expecting %g\n",
			      pt1.getX(), x);
	    System.exit(1);
	}
	if (pt1.getY() != y) {
	    System.out.format("pt1.getY() = %g, expecting %g\n",
			      pt1.getY(), y);
	    System.exit(1);
	}
	if (pt1.getZ() != z) {
	    System.out.format("pt1.getZ() = %g, expecting %g\n",
			      pt1.getZ(), z);
	    System.exit(1);
	}

	Point3D pt3 = (Point3D)(pt2.clone());
	if (!pt3.equals(pt2)) {
	    System.out.println("Clone failed");
	    System.exit(0);
	}

	System.out.println("Check Point3D.Float");
	pt1 = new Point3D.Float();
	pt1.setLocation(x, y, z);
	pt2 = new Point3D.Float(x, y, z);
	
	if (!pt1.equals(pt2)) {
	    System.out.println("pt1 != pt2");
	    System.exit(1);
	}
	if (pt1.hashCode() != pt2.hashCode()) {
	    System.out.format("pt1 & pt2 hashcodes differ: %g != %g\n",
			      pt1.hashCode(), pt2.hashCode());
	    System.exit(1);
	}

	if (pt1.getX() != (double)(float)x) {
	    System.out.format("pt1.getX() = %g, expecting %g\n",
			      pt1.getX(), (double)(float)x);
	    System.exit(1);
	}
	if (pt1.getY() != (double)(float)y) {
	    System.out.format("pt1.getY() = %g, expecting %g\n",
			      pt1.getY(), (double)(float)y);
	    System.exit(1);
	}
	if (pt1.getZ() != (double)(float)z) {
	    System.out.format("pt1.getZ() = %g, expecting %g\n",
			      pt1.getZ(), (double)(float)z);
	    System.exit(1);
	}
	pt3 = (Point3D)(pt2.clone());
	if (!pt3.equals(pt2)) {
	    System.out.println("Clone failed");
	    System.exit(0);
	}

	System.out.println("Point3D OK");
	System.exit(0);
    }
}