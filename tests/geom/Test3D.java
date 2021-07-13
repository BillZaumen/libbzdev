import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;

public class Test3D {

    public static void main(String argv[]) throws Exception {
	System.out.println("Point3D test");

	Point3D p = new Point3D.Double();
	p.setLocation(1.0, 2.0, 3.0);
	if (p.getX() != 1.0 || p.getY() != 2.0 || p.getZ() != 3.0) {
	    System.out.println(" ...  bad Point.Double");
	    System.exit(1);
	}
	Point3D p1 = new Point3D.Double(1.0, 2.0, 3.0);
	Point3D p2 = new Point3D.Double(3.0, 6.0, 7.0);
	if (Math.abs(p1.distance(p2)- 6.0) > 1.0e-10) {
	    System.out.println(" ... bad distance p1 to p2");
	    System.exit(1);
	}

	if (Math.abs(p1.distanceSq(p2)- 36.0) > 1.0e-10) {
	    System.out.println(" ... bad distance p1 to p2");
	    System.exit(1);
	}

	if (Math.abs(Point3D.distance(1.0, 2.0, 3.0, 3.0, 6.0, 7.0) - 6.0)
	    >= 1.e-10) {
	    System.out.println(" ... bad distance (static method)");
	    System.exit(1);
	}
	if (Math.abs(Point3D.distanceSq(1.0, 2.0, 3.0, 3.0, 6.0, 7.0) - 36.0)
	    >= 1.e-10) {
	    System.out.println(" ... bad distance sq (static method)");
	    System.exit(1);
	}
	
	p1.setLocation(p2);
	if (p1.getX() != p2.getX() || p1.getY() != p2.getY()
	    ||p1.getZ() != p2.getZ()) {
	    System.out.println(" ...p1.setLocation(p2) failed");
	    System.exit(1);
	}

	p = new Point3D.Float();
	p.setLocation(1.0, 2.0, 3.0);
	if (p.getX() != 1.0 || p.getY() != 2.0 || p.getZ() != 3.0) {
	    System.out.println(" ...  bad Point.Float");
	    System.exit(1);
	}
	p1 = new Point3D.Float(1.0, 2.0, 3.0);
	p2 = new Point3D.Float(3.0, 6.0, 7.0);
	if (Math.abs(p1.distance(p2)- 6.0) > 1.0e-10) {
	    System.out.println(" ... bad distance p1 to p2 (flaot case)");
	    System.exit(1);
	}

	if (Math.abs(p1.distanceSq(p2)- 36.0) > 1.0e-10) {
	    System.out.println(" ... bad distance p1 to p2 (float case)");
	    System.exit(1);
	}

	
	p1.setLocation(p2);
	if (p1.getX() != p2.getX() || p1.getY() != p2.getY()
	    ||p1.getZ() != p2.getZ()) {
	    System.out.println(" ...p1.setLocation(p2) failed");
	    System.exit(1);
	}
	System.out.println("... OK");
	System.out.println("test AffineTransform3D");

	AffineTransform3D tf = new AffineTransform3D();

	if (!tf.isIdentity()) {
	    System.out.println("... tf.isIdentity() failed");
	    System.exit(1);
	}

	tf.rotate(Math.PI/3.0, 0.0, 0.0);
	if (tf.getType() != AffineTransform3D.TYPE_GENERAL_ROTATION) {
	    System.out.println("tf not a general rotation [1]");
	    System.exit(0);
	}
	p1 = new Point3D.Double(1.0, 0.0, 0.0);
	p2 = new Point3D.Double(0.0, 1.0, 0.0);
	Point3D p3 = new Point3D.Double(0.0, 0.0, 1.0);
	Point3D pt1 = tf.transform(p1, null);
	Point3D pt2 = tf.transform(p2, null);
	Point3D pt3 = tf.transform(p3, null);
	if (Math.abs(pt1.getX() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt1.getY() - Math.sin(Math.PI/3.0)) > 1.e-10) {
	    System.out.println("... rotation (phi = 30 deg) failed (p1)");
	    System.exit(1);
	}
	if (Math.abs(pt2.getY() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt2.getX() + Math.sin(Math.PI/3.0)) > 1.e-10) {
	    System.out.println("... rotation (phi = 30 deg) failed (p2)");
	    System.exit(1);
	}
	if (Math.abs(pt3.getZ() - p3.getZ()) > 1.0e-10
	    || Math.abs(pt3.getX() - p3.getX()) > 1.0e-10
	    || Math.abs(pt3.getZ() - p3.getZ()) > 1.0e-10) {
	    System.out.println("... rotation (phi = 30 deg) failed (p3)");
	    System.exit(1);
	}

	tf.setToRotation(0.0, 0.0, Math.PI/3.0);
	if (tf.getType() != AffineTransform3D.TYPE_GENERAL_ROTATION) {
	    System.out.println("... tf not a general rotation [2]");
	    System.exit(0);
	}
	tf.transform(p1, pt1);
	tf.transform(p2, pt2);
	tf.transform(p3, pt3);
	if (Math.abs(pt1.getX() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt1.getY() - Math.sin(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt1.getZ()) > 1.e-10) {
	    System.out.println("... rotation (psi = 30 deg) failed");
	    System.exit(1);
	}
	if (Math.abs(pt2.getY() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt2.getX() + Math.sin(Math.PI/3.0)) > 1.e-10) {
	    System.out.println("... rotation (phi = 30 deg) failed (p2)");
	    System.exit(1);
	}
	if (Math.abs(pt3.getZ() - p3.getZ()) > 1.0e-10
	    || Math.abs(pt3.getX() - p3.getX()) > 1.0e-10
	    || Math.abs(pt3.getZ() - p3.getZ()) > 1.0e-10) {
	    System.out.println("... rotation (phi = 30 deg) failed (p3)");
	    System.exit(1);
	}

	tf.setToRotation(0.0, Math.PI/3.0, 0.0);
	if (tf.getType() != AffineTransform3D.TYPE_GENERAL_ROTATION) {
	    System.out.println("... tf not a general rotation [3]");
	    System.exit(0);
	}
	tf.transform(p1, pt1);
	tf.transform(p2, pt2);
	tf.transform(p3, pt3);
	if (Math.abs(pt1.getX() - 1.0) > 1.e-10
	    || Math.abs(pt1.getY()) > 1.e-10
	    || Math.abs(pt1.getZ()) > 1.e-10) {
	    System.out.println("... rotation (theta = 30 deg) failed");
	    System.exit(1);
	}
	if (Math.abs(pt2.getY() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt2.getZ() - Math.sin(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt2.getX()) > 1.e-10) {
	    System.out.println("... rotation (theta = 30 deg) failed");
	    System.exit(1);
	}
	if (Math.abs(pt3.getZ() - Math.cos(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt3.getY() + Math.sin(Math.PI/3.0)) > 1.e-10
	    || Math.abs(pt3.getX()) > 1.e-10) {
	    System.out.println("... rotation (psi = 30 deg) failed");
	    System.exit(1);
	}
	
	AffineTransform3D tf2 =
	    AffineTransform3D.getRotateInstance(0.6, 0.7, 0.8);
	tf.setToRotation(0.0, 0.0, 0.8);
	tf.rotate(0.0, 0.7, 0.0);
	tf.rotate(0.6, 0.0, 0.0);
	double[] m1 = new double[12];
	double[] m2 = new double[12];
	tf.getMatrix(m1);
	tf2.getMatrix(m2);
	for (int i = 0; i < 12; i++) {
	    if (Math.abs(m1[i] - m2[i]) > 1.e-10) {
		System.out.println("... m1 != m2 for index " + i);
		for (int j = 0; j < 12; j++) {
		    System.out.format("   m1[%d] = %g, m2[%d] = %g\n",
				      j, m1[j], j, m2[j]);
		}
		System.exit(1);
	    }
	}
	AffineTransform3D ttf =
	    AffineTransform3D.getTranslateInstance(1.0, 2.0, 3.0);
	ttf.transform(p1, pt1);
	ttf.transform(p2, pt2);
	ttf.transform(p3, pt3);

	if (Math.abs(pt1.getX() - p1.getX() - 1.0) >= 1.0e-10
	    || Math.abs(pt1.getY() - p1.getY() - 2.0) >= 1.0e-10
	    || Math.abs(pt1.getZ() - p1.getZ() - 3.0) >= 1.0e-10) {
	    System.out.println("... translation failed [1]");
	    System.exit(1);
	}
	if (Math.abs(pt2.getX() - p2.getX() - 1.0) >= 1.0e-10
	    || Math.abs(pt2.getY() - p2.getY() - 2.0) >= 1.0e-10
	    || Math.abs(pt2.getZ() - p2.getZ() - 3.0) >= 1.0e-10) {
	    System.out.println("... translation failed [2]");
	    System.exit(1);
	}
	if (Math.abs(pt3.getX() - p3.getX() - 1.0) >= 1.0e-10
	    || Math.abs(pt3.getY() - p3.getY() - 2.0) >= 1.0e-10
	    || Math.abs(pt3.getZ() - p3.getZ() - 3.0) >= 1.0e-10) {
	    System.out.println("... translation failed [3]");
	    System.exit(1);
	}
	AffineTransform3D ttfi = ttf.createInverse();
	ttfi.transform(pt1, pt1);
	ttfi.transform(pt2, pt2);
	ttfi.transform(pt3, pt3);
	if (Math.abs(pt1.getX() - p1.getX()) >= 1.0e-10
	    || Math.abs(pt1.getY() - p1.getY()) >= 1.0e-10
	    || Math.abs(pt1.getZ() - p1.getZ()) >= 1.0e-10) {
	    System.out.println("... inverse translation failed [1]");
	    System.exit(1);
	}
	if (Math.abs(pt2.getX() - p2.getX()) >= 1.0e-10
	    || Math.abs(pt2.getY() - p2.getY()) >= 1.0e-10
	    || Math.abs(pt2.getZ() - p2.getZ()) >= 1.0e-10) {
	    System.out.println("... inverse translation failed [2]");
	    System.exit(1);
	}
	if (Math.abs(pt3.getX() - p3.getX()) >= 1.0e-10
	    || Math.abs(pt3.getY() - p3.getY()) >= 1.0e-10
	    || Math.abs(pt3.getZ() - p3.getZ()) >= 1.0e-10) {
	    System.out.println("... inverse translation failed [3]");
	    System.exit(1);
	}

	tf.setToRotation(0.6, 0.7, 0.8);
	tf2.setToRotation(0.6, 0.7, 0.8, 1.0, 2.0, 3.0);
	
	p.setLocation(1.0, 2.0, 3.0);
	Point3D pt = tf2.transform(p, null);
	if (Math.abs(pt.getX() - p.getX()) >= 1.0e-10
	    || Math.abs(pt.getY() - p.getY()) >= 1.0e-10
	    || Math.abs(pt.getZ() - p.getZ()) >= 1.0e-10) {
	    System.out.println("... rotaton with anchor failed");
	    System.exit(1);
	}

	double[] m3 = new double[12];
	ttfi.getMatrix(m3);
	for (int i = 0; i < 9; i++) {
	    if (Math.abs(m3[i] - (((i%4)==0)? 1.0: 0.0)) > 1.e-10) {
	    System.out.println("inverse translation matrix bad for i=" + i);
	    }
	}
	if (Math.abs(m3[9] + 1.0) > 1.e-10
	    || Math.abs(m3[10] + 2.0) > 1.e-10
	    || Math.abs(m3[11] + 3.0) > 1.e-10) {
	    System.out.println("inverse translation matrix bad?");
	}
	tf2.getMatrix(m3);
	tf2.preConcatenate(ttfi);
	tf.getMatrix(m1);
	tf2.getMatrix(m2);
	pt = tf2.transform(p, null);
	if (Math.abs(pt.getX()) >= 1.0e-10
	    || Math.abs(pt.getY()) >= 1.0e-10
	    || Math.abs(pt.getZ()) >= 1.0e-10) {
	    System.out.println("... preconcat failed");
	    for (int i = 0; i < 12; i++) {
		System.out.format("     m2[%d] - m3[%d] = %g\n", i, i,
				  m2[i] - m3[i]);
	    }
	    System.exit(1);
	}
	for (int i = 0; i < 9; i++) {
	    if (Math.abs(m1[i] - m2[i]) > 1.e-10) {
		System.out.println("... rotation with anchor failed");
		for (int j = 0; j < 12; j++) {
		    System.out.format("   m1[%d] = %g, m2[%d] = %g\n",
				      j, m1[j], j, m2[j]);
		}
		System.exit(1);
	    }
	}
	System.out.println("... OK");
	
	System.out.println("Path3D.Double test");

	Path3D path = new Path3D.Double();

	double coords[][] = {
	    {1.0, 2.0, 3.0},
	    {5.0, 5.0, 5.0},
	    {6.0, 7.0, 7.1,
	     10.0, 5.0, 5.0},
	    {12.0, 6.0, 7.0,
	     14.0, 6.0, 7.1,
	     15.0, 5.0, 5.0}
	};

	p = path.getCurrentPoint();
	if (p != null) {
	    System.out.println("... getCurrentPoint() should  have "
			       + "returned null");
	    System.exit(1);
	}
	path.moveTo(1.0, 2.0, 3.0);
	p = path.getCurrentPoint();
	if (p.getX() != 1.0 || p.getY() != 2.0 || p.getZ() != 3.0) {
	    System.out.println("... getCurrentPoint() failed [1]");
	    System.exit(1);
	}
	path.lineTo(5.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 5.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [2]");
	    System.exit(1);
	}
	path.quadTo(6.0, 7.0, 7.1,
		    10.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 10.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [3]");
	    System.exit(1);
	}
	path.curveTo(12.0, 6.0, 7.0,
		     14.0, 6.0, 7.1,
		     15.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 15.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [4]");
	    System.exit(1);
	}
	path.closePath();
	path.closePath();

	AffineTransform3D aft =
	    AffineTransform3D.getTranslateInstance(1.0, 1.0, 1.0);

	PathIterator3D pi1 = path.getPathIterator(null);
	PathIterator3D pi2 = path.getPathIterator(aft);

	double c[] = new double[9];
	double c2[] = new double[9];
	int index = 0;
	while (!pi1.isDone()) {
	    if (pi2.isDone()) {
		System.out.println("... pi2 completed unexectedly");
		System.exit(1);
	    }
	    int type = pi1.currentSegment(c);
	    int type2 = pi2.currentSegment(c2);
	    if (type != type2) {
		System.out.println("type != type2");
		System.exit(1);
	    }
	    switch (type) {
	    case PathIterator3D.SEG_MOVETO:
		if (coords[index].length != 3) {
		    System.out.println ("... SEG_MOVETO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		if (coords[index].length != 3) {
		    System.out.println ("... SEG_LINETO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
	    break;
	    case PathIterator3D.SEG_QUADTO:
		if (coords[index].length != 6) {
		    System.out.println ("... SEG_QUADTO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
	    break;
	    case PathIterator3D.SEG_CUBICTO:
		if (coords[index].length != 9) {
		    System.out.println ("... SEG_CUBICTO, coords: wrong "
					+ "length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (index < coords.length) {
		    System.out.println ("SEG_CLOSE, coords: wrong length " 
					+ coords.length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    default:
		System.out.println("bad return from currentSegment()");
		System.exit(1);
	    }
	    if (index < coords.length) {
		for (int i = 0; i < coords[index].length; i++) {
		    if (c[i] != coords[index][i]) {
			System.out.println("... mismatch on index=" + index
					   + ", i=" + i);
			System.exit(1);
		    }
		    if (Math.abs(c[i] + 1.0 - c2[i]) > 1.e-10) {
			System.out.println("... c[i] != c2[i] for i = " + i
					   + ", index = " + index);
			System.exit(1);
		    }
		}
	    }
	    index++;
	    pi1.next();
	    pi2.next();
	}
	System.out.println("... OK");

	System.out.println("Path3D.Float test");

	path = new Path3D.Float();

	p = path.getCurrentPoint();
	if (p != null) {
	    System.out.println("... getCurrentPoint() should  have "
			       + "returned null");
	    System.exit(1);
	}
	path.moveTo(1.0, 2.0, 3.0);
	p = path.getCurrentPoint();
	if (p.getX() != 1.0 || p.getY() != 2.0 || p.getZ() != 3.0) {
	    System.out.println("... getCurrentPoint() failed [1]");
	    System.exit(1);
	}

	path.lineTo(5.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 5.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [2]");
	    System.exit(1);
	}
	path.quadTo(6.0, 7.0, 7.1,
		    10.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 10.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [3]");
	    System.exit(1);
	}
	path.curveTo(12.0, 6.0, 7.0,
		     14.0, 6.0, 7.1,
		     15.0, 5.0, 5.0);
	p = path.getCurrentPoint();
	if (p.getX() != 15.0 || p.getY() != 5.0 || p.getZ() != 5.0) {
	    System.out.println("... getCurrentPoint() failed [4]");
	    System.exit(1);
	}
	path.closePath();
	path.closePath();
	

	aft = AffineTransform3D.getTranslateInstance(1.0, 1.0, 1.0);

	pi1 = path.getPathIterator(null);
	pi2 = path.getPathIterator(aft);

	c = new double[9];
	c2 = new double[9];
	index = 0;
	while (!pi1.isDone()) {
	    if (pi2.isDone()) {
		System.out.println("... pi2 completed unexectedly");
		System.exit(1);
	    }
	    int type = pi1.currentSegment(c);
	    int type2 = pi2.currentSegment(c2);
	    if (type != type2) {
		System.out.println("type != type2");
		System.exit(1);
	    }
	    switch (type) {
	    case PathIterator3D.SEG_MOVETO:
		if (coords[index].length != 3) {
		    System.out.println ("... SEG_MOVETO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		if (coords[index].length != 3) {
		    System.out.println ("... SEG_LINETO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
	    break;
	    case PathIterator3D.SEG_QUADTO:
		if (coords[index].length != 6) {
		    System.out.println ("... SEG_QUADTO, coords: wrong length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
	    break;
	    case PathIterator3D.SEG_CUBICTO:
		if (coords[index].length != 9) {
		    System.out.println ("... SEG_CUBICTO, coords: wrong "
					+ "length " 
					+ coords[index].length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (index < coords.length) {
		    System.out.println ("SEG_CLOSE, coords: wrong length " 
					+ coords.length);
		    System.out.println ("... (index = " + index + ")");
		    System.exit(1);
		}
		break;
	    default:
		System.out.println("bad return from currentSegment()");
		System.exit(1);
	    }
	    if (index < coords.length) {
		for (int i = 0; i < coords[index].length; i++) {
		    if (Math.abs(c[i] - coords[index][i]) > 1.e-7) {
			System.out.println("... mismatch on index=" + index
					   + ", i=" + i + "(c[i] = " + c[i]
					   + ", coord[index][i] = "
					   + coords[index][i] + ")");
			System.exit(1);
		    }
		    if (Math.abs(c[i] + 1.0 - c2[i]) > 1.e-7) {
			System.out.println("... c[i] != c2[i] for i = " + i
					   + ", index = " + index);
			System.exit(1);
		    }
		}
	    }
	    index++;
	    pi1.next();
	    pi2.next();
	}

	System.out.println("... OK");

	System.out.println("test Rectangle3D");

	Rectangle3D r3d = new Rectangle3D.Double(1.0, 2.0, 3.0,
						 10.0, 20.0, 30.0);
	
	if (r3d.getMinX() != 1.0 || r3d.getMinY() != 2.0
	    || r3d.getMinZ() != 3.0
	    || r3d.getWidth() != 10.0
	    || r3d.getHeight() != 20.0
	    || r3d.getDepth() != 30.0) {
	    System.out.println("... bad rectangle [1]");
	    System.exit(1);
	}

	if (r3d.getMaxX() != 11.0 || r3d.getMaxY() != 22.0
	    || r3d.getMaxZ() != 33.0) {
	    System.out.println("... bad rectangle [2]");
	    System.exit(1);
	}

	if (r3d.getCenterX() != 6.0 || r3d.getCenterY() != 12.0
	    || r3d.getCenterZ() != 18.0) {
	    System.out.println("... bad rectangle [3]");
	    System.exit(1);
	}
	if (r3d.contains(0.0, 0.0, 0.0)) {
	    System.out.println("contains failed [1]");
	    System.exit(1);
	}
	
	if (r3d.contains(100.0, 100.0, 100.0)) {
	    System.out.println("contains failed [2]");
	    System.exit(1);
	}
	if (r3d.contains(6.0, 12.0, 100.0)) {
	    System.out.println("contains failed [3]");
	    System.exit(1);
	}

	if (r3d.contains(6.0, 12.0, 0.0)) {
	    System.out.println("contains failed [4]");
	    System.exit(1);
	}

	if (!r3d.contains(6.0, 12.0, 18.0)) {
	    System.out.println("contains failed [5]");
	    System.exit(1);
	}

	Rectangle3D r3d2 = new Rectangle3D.Double();
	r3d2.setRectFromDiagonal(r3d.getMinX(), r3d.getMinY(), r3d.getMinZ(),
				 r3d.getMaxX(), r3d.getMaxY(), r3d.getMaxZ());
	
	if (Math.abs (r3d.getMinX() - r3d2.getMinX()) > 1.e-10
	    || Math.abs (r3d.getMinY() - r3d2.getMinY()) > 1.e-10
	    || Math.abs (r3d.getMinZ() - r3d2.getMinZ()) > 1.e-10
	    || Math.abs (r3d.getMaxX() - r3d2.getMaxX()) > 1.e-10
	    || Math.abs (r3d.getMaxY() - r3d2.getMaxY()) > 1.e-10
	    || Math.abs (r3d.getMaxZ() - r3d2.getMaxZ()) > 1.e-10) {
	    System.out.println("... setRectFromDiagonal failed");
	    System.exit(1);
	}

	r3d = new Rectangle3D.Double(-5.0, -5.0, -5.0, 10.0, 10.0, 10.0);
	r3d.add(new Point3D.Double(5.0, 5.0, 5.0));
	if (r3d.getMinX() != -5.0 || r3d.getMinY() != -5.0
	    || r3d.getMinZ() != -5.0
	    || r3d.getMaxX() != 5.0 || r3d.getMaxY() != 5.0
	    || r3d.getMaxZ() != 5.0) {
	    System.out.println("add point to rectangle failed [1]");
	    System.exit(1);
	}

	r3d.add(new Point3D.Double(6.0, 0.0, 0.0));
	if (r3d.getMinX() != -5.0 || r3d.getMinY() != -5.0
	    || r3d.getMinZ() != -5.0
	    || r3d.getMaxX() != 6.0 || r3d.getMaxY() != 5.0
	    || r3d.getMaxZ() != 5.0) {
	    System.out.println("add point to rectangle failed [2]");
	    System.exit(1);
	}

	r3d.add(5.0, 6.0, 5.0);
	if (r3d.getMinX() != -5.0 || r3d.getMinY() != -5.0
	    || r3d.getMinZ() != -5.0
	    || r3d.getMaxX() != 6.0 || r3d.getMaxY() != 6.0
	    || r3d.getMaxZ() != 5.0) {
	    System.out.println("add point to rectangle failed [3]");
	    System.exit(1);
	}

	r3d.add(new Rectangle3D.Double(0.0, 0.0, 0.0, 1.0, 1.0, 6.0));
	if (r3d.getMinX() != -5.0 || r3d.getMinY() != -5.0
	    || r3d.getMinZ() != -5.0
	    || r3d.getMaxX() != 6.0 || r3d.getMaxY() != 6.0
	    || r3d.getMaxZ() != 6.0) {
	    System.out.println("add point to rectangle failed [4]");
	    System.exit(1);
	}

	r3d.setRectFromDiagonal(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0);
	r3d2.setRectFromDiagonal(-2.0, -2.0, -2.0, 2.0, 2.0, 2.0);
	Rectangle3D r = r3d.createUnion(r3d2);
	r3d2.setRectFromDiagonal(-8.0, -2.0, -2.0, 8.0, 2.0, 2.0);
	if (Math.abs(r.getMinX() + 5.0) > 1.e-10
	    || Math.abs(r.getMinY() + 5.0) > 1.e-10
	    || Math.abs(r.getMinZ() + 5.0) > 1.e-10
	    || Math.abs(r.getMaxX() - 5.0) > 1.e-10
	    || Math.abs(r.getMaxY() - 5.0) > 1.e-10
	    || Math.abs(r.getMaxZ() - 5.0) > 1.e-10) {
	    System.out.println("createUnion failed [1]");
	    System.exit(1);
	}
	r = r3d.createUnion(r3d2);
	if (Math.abs(r.getMinX() + 8.0) > 1.e-10
	    || Math.abs(r.getMinY() + 5.0) > 1.e-10
	    || Math.abs(r.getMinZ() + 5.0) > 1.e-10
	    || Math.abs(r.getMaxX() - 8.0) > 1.e-10
	    || Math.abs(r.getMaxY() - 5.0) > 1.e-10
	    || Math.abs(r.getMaxZ() - 5.0) > 1.e-10) {
	    System.out.println("createUnion failed [2]");
	    System.exit(1);
	}
	r3d.setRectFromDiagonal(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0);
	r3d2.setRectFromDiagonal(-2.0, -2.0, -2.0, 2.0, 2.0, 2.0);
	if (!r3d.intersects(r3d2)) {
	    System.out.println("intersects failed [1]");
	}
	if (!r3d2.intersects(r3d)) {
	    System.out.println("intersects failed [2]");
	}
	r = r3d.createIntersection(r3d2);
	r3d2.setRectFromDiagonal(-8.0, -2.0, -2.0, 8.0, 2.0, 2.0);
	if (Math.abs(r.getMinX() + 2.0) > 1.e-10
	    || Math.abs(r.getMinY() + 2.0) > 1.e-10
	    || Math.abs(r.getMinZ() + 2.0) > 1.e-10
	    || Math.abs(r.getMaxX() - 2.0) > 1.e-10
	    || Math.abs(r.getMaxY() - 2.0) > 1.e-10
	    || Math.abs(r.getMaxZ() - 2.0) > 1.e-10) {
	    System.out.println("createIntersection failed [1]");
	    System.exit(1);
	}

	r3d.setRectFromDiagonal(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0);
	r3d2.setRectFromDiagonal(-2.0, -10.0, -10.0, 2.0, 10.0, 10.0);
	if (!r3d.intersects(r3d2)) {
	    System.out.println("intersects failed [3]");
	}
	if (!r3d2.intersects(r3d)) {
	    System.out.println("intersects failed [4]");
	}
	r = r3d.createIntersection(r3d2);
	r3d2.setRectFromDiagonal(-8.0, -2.0, -2.0, 8.0, 2.0, 2.0);
	if (Math.abs(r.getMinX() + 2.0) > 1.e-10
	    || Math.abs(r.getMinY() + 5.0) > 1.e-10
	    || Math.abs(r.getMinZ() + 5.0) > 1.e-10
	    || Math.abs(r.getMaxX() - 2.0) > 1.e-10
	    || Math.abs(r.getMaxY() - 5.0) > 1.e-10
	    || Math.abs(r.getMaxZ() - 5.0) > 1.e-10) {
	    System.out.println("createIntersection failed [2]");
	    System.out.format("... (%g, %g, %g) <---> (%g, %g, %g)\n",
			      r.getMinX(), r.getMinY(), r.getMinZ(),
			      r.getMaxX(), r.getMaxY(), r.getMaxZ());
	    System.exit(1);
	}

	if (r.isEmpty()) {
	    System.out.println("empty test failed [1]");
	    System.exit(1);
	}

	r = new Rectangle3D.Double();
	if (!r.isEmpty()) {
	    System.out.println("empty test failed [2]");
	    System.exit(1);
	}

	SurfaceIterator si = r3d.getSurfaceIterator(null);
	double[] rcoords = new double[9];
	// We can use the p3d package to test this code.
	Model3D model = new Model3D();
	while (!si.isDone()) {
	    int type = si.currentSegment(rcoords);
	    if (type != SurfaceIterator.PLANAR_TRIANGLE) {
		System.out.println("... not a planar triangle");
		System.exit(1);
	    }
	    for (int i = 0; i < 9; i++) {
		if (Math.abs(Math.abs(rcoords[i]) - 5.0) > 1.e-10) {
		    System.out.println("an rcoord was incorrect");
		    System.exit(1);
		}
	    }
	    model.addTriangle(rcoords[0], rcoords[1], rcoords[2],
			      rcoords[3], rcoords[4], rcoords[5],
			      rcoords[6], rcoords[7], rcoords[8]);
	    si.next();
	}
	if (Math.abs(model.getMinX() + 5.0) > 1.e-7
	    || Math.abs(model.getMinY() + 5.0) > 1.e-7
	    || Math.abs(model.getMinZ() + 5.0) > 1.e-7
	    || Math.abs(model.getMaxX() - 5.0) > 1.e-7
	    || Math.abs(model.getMaxY() - 5.0) > 1.e-7
	    || Math.abs(model.getMaxZ() - 5.0) > 1.e-7) {
	    System.out.println("bounding box from surface iterator wrong");
	    System.exit(1);
	}
	if (model.notPrintable(System.out)) {
	    System.out.println(" ... surface iterator did not return the "
			       + "triangles that were expected");
	}

	r3d = new Rectangle3D.Double("r3d1");
	r3d.setRect(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Double(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, "r3d2");
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Float("r3d3");
	r3d.setRect(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Float(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, "r3d4");
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Double(Rectangle3D.STACKTRACE);
	r3d.setRect(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Double(0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
				     Rectangle3D.STACKTRACE);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Float(Rectangle3D.STACKTRACE);
	r3d.setRect(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	r3d = new Rectangle3D.Float(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F,
				    Rectangle3D.STACKTRACE);
	si = r3d.getSurfaceIterator(null);
	System.out.println("saw tag " +si.currentTag());

	Path3D path1d = new Path3D.Double();
	path1d.moveTo(10.0, 20.0, 30.0);
	path1d.lineTo(20.0, 30.0, 40.0);
	path1d.lineTo(30.0, 40.0, 50.0);

	Path3D path2d = new Path3D.Double();
	path2d.moveTo(30.0, 40.0, 50.0);
	path2d.lineTo(40.0, 50.0, 60.0);
	path2d.lineTo(50.0, 60.0, 70.0);

	path1d.append(path2d, true);

	double[][] expecting = {
	    {10.0, 20.0, 30.0},
	    {20.0, 30.0, 40.0},
	    {30.0, 40.0, 50.0},
	    {40.0, 50.0, 60.0},
	    {50.0, 60.0, 70.0}
	};

	PathIterator3D pi = path1d.getPathIterator(null);
	double[] pcoords = new double[9];
	int cnt = 0;
	while (!pi.isDone()) {
	    pi.currentSegment(pcoords);
	    for (int i = 0; i < 3; i++) {
		if (expecting[cnt][i] != pcoords[i]) {
		    throw new Exception("expecting");
		}
	    }
	    cnt++;
	    pi.next();
	}

	Path3D path1f = new Path3D.Double();
	path1f.moveTo(10.0, 20.0, 30.0);
	path1f.lineTo(20.0, 30.0, 40.0);
	path1f.lineTo(30.0, 40.0, 50.0);

	Path3D path2f = new Path3D.Double();
	path2f.moveTo(30.0, 40.0, 50.0);
	path2f.lineTo(40.0, 50.0, 60.0);
	path2f.lineTo(50.0, 60.0, 70.0);

	path1f.append(path2d, true);

	pi = path1f.getPathIterator(null);
	cnt = 0;
	while (!pi.isDone()) {
	    pi.currentSegment(pcoords);
	    for (int i = 0; i < 3; i++) {
		if (expecting[cnt][i] != pcoords[i]) {
		    throw new Exception("expecting");
		}
	    }
	    cnt++;
	    pi.next();
	}


	System.out.println("... OK");

	System.exit(0);
    }
}
