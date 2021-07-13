import org.bzdev.math.*;
import org.bzdev.geom.*;
import java.awt.geom.*;


public class NurbsTest {

    public static void main(String argv[]) throws Exception {
	double circKnots[] = {
	    0.0, 0.0, 0.0,
	    1.0/3.0, 1.0/3.0,
	    2.0/3.0, 2.0/3.0,
	    1.0, 1.0, 1.0
	};
	
	double root3o2 = Math.sqrt(3.0)/2.0;
	double hroot3o2 = root3o2/2.0;

	double circControlP[] = {
	    0.5, 0.0, 1.0,
	    0.0, 0.0, 0.5,
	    0.25, hroot3o2, 1.0,
	    0.5, root3o2, 0.5,
	    0.75, hroot3o2, 1.0,
	    1.0, 0.0, 0.5,
	    0.5, 0.0, 1.0
	};

	NurbsArray circle = new NurbsArray(2, circKnots, circControlP);

	double[] negcenter = new double[2];
	VectorOps.add(negcenter, 0, circControlP, 0, circControlP, 6, 2);
	VectorOps.add(negcenter, 0, negcenter, 0, circControlP, 12, 2);
	VectorOps.multiply(negcenter, -1.0/3.0, negcenter);
	double radius = -negcenter[1];

	Point2D[] points = new Point2D[100];
	double data[] = new double[200];
	double uvalues[] = new double[100];
	int index = 0;
	for (int i = 0; i <= 100; i++) {
	    double u = i / 100.0;
	    if (u < 0.0) u = 0.0;
	    if (u > 1.0) u = 1.0;
	    double[] point = circle.valueAt(u);
	    double r = VectorOps.norm(VectorOps.add(point, negcenter));
	    if (Math.abs(r - radius) > 1.e-12) {
		throw new Exception ("u = " + u +": r = " + r
				     + ", radius = " + radius);
		
	    }
	    if (i < points.length) {
		points[i] = new Point2D.Double(point[0], point[1]);
		uvalues[i] = u;
		data[index++] = point[0];
		data[index++] = point[1];
	    }
	}


	// Take the points, fit a spline to them, and use that to
	// estimate the path length.  We'd expect to be very close
	// to the circumference of a circle with the specified radius.
	//
	BasicSplinePath2D splinePath = new BasicSplinePath2D(points, true);
	double circumference = splinePath.getPathLength();
	double ec = 2.0*Math.PI*radius;
	if (Math.abs(circumference - ec) > 1.e-7) {
	    System.out.println("circumference = " + circumference
			       + ", expecting " + ec);
	}

	/*
	NurbsArray nurbs1 = new NurbsArray(2, 2, 7,
					   BSpline.Mode.PERIODIC,
					   null,
					   uvalues, data);

	System.out.println("nurbs1 period start = " + nurbs1.getPeriodStart());
	System.out.println("nurbs1 period end = " + nurbs1.getPeriodEnd());
	*/

    }
}