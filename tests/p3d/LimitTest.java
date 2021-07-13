import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

// taken from a program that had failed because of floating-point
// round off issues.  Model3D sets coordinates whose value is very
// small to zero but the org.bzdev.geom packages do not. There were
// some cases that were missed when appending a Shape3D to the model
// resulting in what should have been a single point looking like two
// separate points.

public class LimitTest {

    private static double fix (double x) {
	return Math.round(x*10)/10.0;
    }
    
    private static Set<Point3D> getPoints(Shape3D shape) {
	int sz = 256;
	if (shape instanceof Surface3D) {
	    sz += ((Surface3D)shape).size();
	} else if (shape instanceof Model3D) {
	    sz += ((Model3D)shape).size();
	}
	var set = new HashSet<Point3D>(sz);
	SurfaceIterator si = shape.getSurfaceIterator(null);
	double coords[] = new double[48];
	while (!si.isDone()) {
	    int n = 48;
	    switch (si.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		n = 30;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		n = 15;
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		n = 9;
		break;
	    }
	    for (int i = 0; i < n; i += 3) {
		set.add(new Point3D.Double(coords[i],
					   coords[i+1],
					   coords[i+2]));
	    }
	    si.next();
	}
	return set;
    }

    private static Point3D getClosest(Set<Point3D> set, Point3D point) {
	double maxdist2 = Double.POSITIVE_INFINITY;
	Point3D best = null;
	for (Point3D p: set) {
	    double d2 = point.distanceSq(p);
	    if (d2 < maxdist2) {
		maxdist2 = d2;
		best = p;
	    }
	}
	return best;
    }

    public static void main(String argv[]) throws Exception {

	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;
	double delta = 0.7;

	double yoff = 2*r1;
	double xoff = 2*r1;


	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0,
					 2*Math.PI);
	circle.closePath();

	var lcircle = Paths2D.createArc(0.0, 0.0, fix(r1 + delta), 0.0,
					 2*Math.PI);
	lcircle.closePath();


	var loop2d = new Path2D.Double();
	loop2d.moveTo(0.0, h1);
	double delta1 = (h2 - h1)/5;
	for (int i = 1; i < 5; i++) {
	    double y = h1 + delta1*i;
	    loop2d.lineTo(0.0, y);
	}
	loop2d.lineTo(0.0, h2);
	Path2D arc = Paths2D.createArc(loop2d, r2, false, Math.PI,
				       Math.PI/8);
	loop2d.append(arc, true);
	for (int i = 1; i < 5; i++) {
	    double y = h2 - i*delta1;
	    loop2d.lineTo(2*r2, y);
	}
	loop2d.lineTo(2*r2, h1);

	Path3D loop = new Path3D.Double(loop2d, (i, p, type, bounds) -> {
		return new Point3D.Double(p.getX(), 0.0, p.getY());
	}, 0);

	
	double inormal[] = {1.0, 0.0, 0.0};
	BezierGrid loopGrid = new BezierGrid(circle,
					     BezierGrid.getMapper(loop,
								  inormal));
	
	Path3D circle1 = loopGrid.getBoundary(0, 0);

	int n = loopGrid.getUArrayLength();
	Path3D circle2 = loopGrid.getBoundary(n-1, 0);

	Model3D m3d = new Model3D();

	Model3D m3dt1 = new Model3D();
	Model3D m3dt2 = new Model3D();

	SteppedGrid.Builder sgb1 = new SteppedGrid.Builder(m3d, fix(h1/2), 0.0);

	sgb1.addRectangles(fix(-4*r1), fix(-4*r1), fix(8*r1), fix(8*r1),
			   0.0, 0.0, false, false);

	sgb1.addRectangles(fix(-2*r1), fix(-2*r1), fix(4*r1), fix(4*r1),
			   fix(h1/2), 0.0, true, false);


	SteppedGrid.Builder sgb2 = new SteppedGrid.Builder(m3d, h1, 0.0);

	sgb2.addRectangles(fix(2*r2 - 2*r1), fix(-2*r1), fix(4*r1), fix(4*r1),
			   0.0, 0.0, true, false);

	sgb2.addRectangles(fix(2*r2 - 2*r1 - xoff), fix(yoff),
			   fix(xoff + 4*r1), fix(6*r1 - yoff),
			   0.0, 0.0, false, false);

	SteppedGrid sg1 = sgb1.create();
	SteppedGrid sg2 = sgb2.create();

	Path3D sg1Boundary = sg1.getBoundary();
	if (sg1Boundary == null) {
	    System.out.println("sg1: no boundary could be computed");
	    System.exit(1);
	}
	
	/*
	System.out.println("circle1");
	Path3DInfo.printSegments(circle1);
	*/

	var c1 = new ConvexPathConnector(circle1, sg1Boundary);
	Surface3D surface = new Surface3D.Double();
	surface.append(c1);
	Path3D bb = surface.getBoundary();
	if (bb == null) {
	    System.out.println("surface for c1 has no boundary");
	}
	/*
	System.out.println("bb:");
	Path3DInfo.printSegments(bb);
	*/

	m3dt1.append(c1);
	Path3D b = m3dt1.getBoundary();
	if (b != null) {
	    Path3DInfo.printSegments(b);
	} else {
	    System.out.println("m3dt1 has no boundary");
	    var set1 = getPoints(surface);
	    var set2 = getPoints(m3dt1);
	    int n1 = set1.size();
	    int n2 = set2.size();
	    if (n1 != n2) {
		System.out.format("n1 = %d but n2 = %d\n", n1, n2);
		if (n1 < n2) {
		    if (set2.containsAll(set1)) {
			System.out.println("set1 is a subset of set2");
			set2.removeAll(set1);
			System.out.println("extra points:");
			for (Point3D p: set2) {
			    Point3D closest = getClosest(set1, p);
			    System.out.format("    (%s, %s, %s),  closest to "
					      + "(%s, %s, %s)\n",
					      p.getX(), p.getY(), p.getZ(),
					      closest.getX(),
					      closest.getY(),
					      closest.getZ());

			}
		    }
		} else {
		    if (set1.containsAll(set2)) {
			System.out.println("set2 is a subset of set1");
			set1.removeAll(set2);
			System.out.println("extra points:");
			for (Point3D p: set1) {
			    Point3D closest = getClosest(set2, p);
			    System.out.format("    (%s, %s, %s),  closest to "
					      + "(%s, %s, %s)\n",
					      p.getX(), p.getY(), p.getZ(),
					      closest.getX(),
					      closest.getY(),
					      closest.getZ());

			}
		    }
		}
		System.exit(1);
	    }
	}
	System.exit(0);
    }
}
