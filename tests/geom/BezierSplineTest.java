import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class BezierSplineTest {
    private static double dsin(double x) {
	double u = Math.toRadians(x);
	return Math.sin(u);
    }


    // copied from SplinePath2D, with mods, for testing.
    // This checks a getw function to make sure the version
    // that can reverse the path works correctly. It was added
    // primarily for debugging.

    private static String errormsg(String msg) {return msg;}

    private static double[] getw(double[] x, int n, boolean cyclic,
				 Point2D start, boolean isX,
				 Point2D cpoint1, Point2D cpoint2)
    {
	boolean startsWithMoveTo = (start == null);

	if (startsWithMoveTo) {
	    // x[0] represents the coordinate for an implied moveTo.
	    if (!cyclic) n--;
	    int nm1 = n-1;
	    double[] result = new double[n];
	    if (cyclic) {
		for (int i = 0; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm1] + 2.0 * x[0];
	    } else {
		if (cpoint1 != null) {
		    result[0] = isX? cpoint1.getX(): cpoint1.getY();
		} else {
		    result[0] = x[0] + 2.0 * x[1];
		}
		for (int i = 1; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		if (cpoint2 != null) {
		    double p2nm1 = isX? cpoint2.getX(): cpoint2.getY();
		    result[nm1] = 2*p2nm1 -x[n];
		} else {
		    result[nm1] =  8.0 * x[nm1] + x[n];
		}
	    }
	    return result;
	} else {
	    if (cyclic) n++;
	    double[] result = new double[n];
	    int nm1 = n - 1;
	    int nm2 = n - 2;
	    // Point2D start = getCurrentPoint();
	    if (start == null) {
		throw new IllegalStateException(errormsg("missingMOVETO"));
	    }
	    double xval = isX? start.getX(): start.getY();
	    if (cyclic) {
		result[0] = 4.0 * xval + 2.0 * x[0];
		for (int i = 0; i < nm2; i++) {
		    result[i+1] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm2] + 2.0 * xval;
	    } else {
		if (cpoint1 != null) {
		    result[0] = isX? cpoint1.getX(): cpoint1.getY();
		} else {
		    result[0] = xval + 2.0 * x[0];
		}
		for (int i = 1; i < nm1; i++) {
		    result[i] = 4.0 * x[i-1] + 2.0 * x[i];
		}
		if (cpoint2 != null) {
		    double p2nm1 = isX? cpoint2.getX(): cpoint2.getY();
		    result[nm1] = 2*p2nm1 -x[nm1];
		} else {
		    result[nm1] =  8.0 * x[nm2] + x[nm1] ;
		}
	    }
	    return result;
	}
    }



    private static double[] getw(double[] x, int n, boolean cyclic,
				 Point2D start, boolean isX,
				 Point2D cpoint, boolean reverse)
    {
	boolean startsWithMoveTo = (start == null);

	if (startsWithMoveTo) {
	    // x[0] represents the coordinate for an implied moveTo.
	    if (!cyclic) n--;
	    int nm1 = n-1;
	    double[] result = new double[n];
	    if (cyclic) {
		for (int i = 0; i < nm1; i++) {
		    result[i] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm1] + 2.0 * x[0];
	    } else {
		int nm2 = n-2;
		if (cpoint != null) {
		    result[0] = isX? cpoint.getX(): cpoint.getY();
		} else {
		    int index0 = reverse? n: 0;
		    int index1 = reverse? nm1: 1;
		    result[0] = x[index0] + 2.0 * x[index1];
		}
		for (int i = 1; i < nm1; i++) {
		    int indexI = reverse? n-i: i;
		    int indexIp1 = reverse? indexI-1: i+1;
		    result[i] = 4.0 * x[indexI] + 2.0 * x[indexIp1];
		}
		int index1 = reverse? 1: nm1;
		int index2 = reverse? 0: n;
		result[nm1] =  8.0 * x[index1] + x[index2];
	    }
	    return result;
	} else {
	    if (cyclic) n++;
	    double[] result = new double[n];
	    int nm1 = n - 1;
	    int nm2 = n - 2;
	    // Point2D start = getCurrentPoint();
	    if (start == null) {
		throw new IllegalStateException(errormsg("missingMOVETO"));
	    }
	    double xval = isX? start.getX(): start.getY();
	    if (cyclic) {
		result[0] = 4.0 * xval + 2.0 * x[0];
		for (int i = 0; i < nm2; i++) {
		    result[i+1] = 4.0 * x[i] + 2.0 * x[i+1];
		}
		result[nm1] = 4.0 * x[nm2] + 2.0 * xval;
	    } else {
		if (cpoint != null) {
		    result[0] = isX? cpoint.getX(): cpoint.getY();
		} else {
		    if (reverse) {
			result[0] = x[nm1] + 2* x[nm2];
		    } else {
			result[0] = xval + 2.0 * x[0];
		    }
		}
		for (int i = 1; i < nm1; i++) {
		    int indexI = reverse? nm2 - i: i;
		    int indexIm1 = reverse?indexI+1 :i-1;
		    result[i] = 4.0 * x[indexIm1] + 2.0 * x[indexI];
		}
		if (reverse) {
		    result[nm1] = 8.0*x[0] + xval;
		} else {
		    result[nm1] =  8.0 * x[nm2] + x[nm1] ;
		}
	    }
	    return result;
	}
    }

    private static void reverse(double[] array) {
	reverse(array, array.length);
    }

    private static void reverse(double[] array, int n) {
	int n2 = n/2;
	int j = n-1;
	for (int i = 0; i < n2; i++, j--) {
	    double tmp = array[i];
	    array[i] = array[j];
	    array[j] = tmp;
	}
    }


    private static void initialTest() throws Exception {
	// check getw for reversal.
	double x[] = {1.0, 4.0, 9.0, 16.0, 25.0};

	double[] w1 = getw(x, x.length, false, null, true, null, null);
	double[] w2 = getw(x, x.length, false, null, true, null, false);
	reverse(x);
	double[] w3 = getw(x, x.length, false, null, true, null, true);
	reverse(x);
	if (w1.length != w2.length) throw new Exception();
	for (int i = 0; i < w1.length; i++) {
	    if (w1[i] != w2[i] || w1[i] != w3[i]) {
		for (int j = 0; j < w1.length; j++) {
		    System.out.format("i = %d, w1[i] = %s, w2[i] = %s"
				      + "w3[i] = %s\n",
				      j, w1[j], w2[j], w3[j]);
		}
		throw new Exception();
	    }
	}

	Point2D cpoint = new Point2D.Double(2.0, 0.0);
	w1 = getw(x, x.length, false, null, true, cpoint, null);
	w2 = getw(x, x.length, false, null, true, cpoint, false);
	reverse(x);
	w3 = getw(x, x.length, false, null, true, cpoint, true);
	reverse(x);
	if (w1.length != w2.length) throw new Exception();
	for (int i = 0; i < w1.length; i++) {
	    if (w1[i] != w2[i] || w1[i] != w3[i]) {
		for (int j = 0; j < w1.length; j++) {
		    System.out.format("i = %d, w1[i] = %s, w2[i] = %s"
				      + "w3[i] = %s\n",
				      j, w1[j], w2[j], w3[j]);
		}
		throw new Exception();
	    }
	}

	Point2D start1 = new Point2D.Double(1.0, 0.0);
	double xp1[] = {4.0, 9.0, 16.0, 25.0};
	Point2D start2 = new Point2D.Double(25.0, 0.0);
	double xp2[] = {16.0, 9.0, 4.0, 1.0};

	w1 = getw(xp1, xp1.length, false, start1, true, null, null);
	w2 = getw(xp1, xp1.length, false, start1, true, null, false);
	w3 = getw(xp2, xp2.length, false, start2, true, null, true);
	if (w1.length != w2.length) throw new Exception();
	for (int i = 0; i < w1.length; i++) {
	    if (w1[i] != w2[i] || w1[i] != w3[i]) {
		for (int j = 0; j < w1.length; j++) {
		    System.out.format("i = %d, w1[i] = %s, w2[i] = %s"
				      + "w3[i] = %s\n",
				      j, w1[j], w2[j], w3[j]);
		}
		throw new Exception();
	    }
	}

	w1 = getw(xp1, xp1.length, false, start1, true, cpoint, null);
	w2 = getw(xp1, xp1.length, false, start1, true, cpoint, false);
	w3 = getw(xp2, xp2.length, false, start2, true, cpoint, true);
	if (w1.length != w2.length) throw new Exception();
	for (int i = 0; i < w1.length; i++) {
	    if (w1[i] != w2[i] || w1[i] != w3[i]) {
		for (int j = 0; j < w1.length; j++) {
		    System.out.format("i = %d, w1[i] = %s, w2[i] = %s"
				      + "w3[i] = %s\n",
				      j, w1[j], w2[j], w3[j]);
		}
		throw new Exception();
	    }
	}
    }

    private static void testSymmetric() throws Exception {
	// try a symmetric case
	Point2D knots3[] = {
	    new Point2D.Double(0.0, 0.0),
	    new Point2D.Double(1.0, 1.0),
	    new Point2D.Double(2.0, 4.0),
	    new Point2D.Double(3.0, 9.0),
	    new Point2D.Double(4.0, 4.0),
	    new Point2D.Double(5.0, 1.0),
	    new Point2D.Double(6.0, 0.0)
	};
	Point2D cpoint3a = new Point2D.Double(1.0/3.0, 0.0);
	Point2D cpoint3b = new Point2D.Double(5.0 + 2.0/3.0, 0.0);

	Point3D cpoint3aY = new Point3D.Double(1.0/3.0, 0.0, 0.0);
	Point3D cpoint3bY = new Point3D.Double(5.0 + 2.0/3.0, 0.0, 0.0);

	Point3D cpoint3aZ = new Point3D.Double(1.0/3.0, 0.0, 0.0);
	Point3D cpoint3bZ = new Point3D.Double(5.0 + 2.0/3.0, 0.0, 0.0);


	System.out.println("symmetric case");

	SplinePath2D spath3 = new
	    SplinePath2D(knots3, knots3.length, null, null);
	SplinePath2D spath3a = new
	    SplinePath2D(knots3, knots3.length, cpoint3a, null);
	SplinePath2D spath3b = new
	    SplinePath2D(knots3, knots3.length, null, cpoint3b);
	SplinePath2D spath3c = new
	    SplinePath2D(knots3, knots3.length, cpoint3a, cpoint3b);

	System.out.println("no control points");
	Path2DInfo.printSegments(spath3);
	System.out.println("control points at start");
	Path2DInfo.printSegments(spath3a);
	System.out.println("control points at end");
	Path2DInfo.printSegments(spath3b);

	System.out.println("control points at both ends");
	Path2DInfo.printSegments(spath3c);

	Point2D start = new Point2D.Double(0.0, 0.0);

	Point2D knots4[] = {
	    new Point2D.Double(1.0, 1.0),
	    new Point2D.Double(2.0, 4.0),
	    new Point2D.Double(3.0, 9.0),
	    new Point2D.Double(4.0, 4.0),
	    new Point2D.Double(5.0, 1.0),
	    new Point2D.Double(6.0, 0.0)
	};

	Point3D knots4Y[] = {
	    new Point3D.Double(1.0, 0.0, 1.0),
	    new Point3D.Double(2.0, 0.0, 4.0),
	    new Point3D.Double(3.0, 0.0, 9.0),
	    new Point3D.Double(4.0, 0.0, 4.0),
	    new Point3D.Double(5.0, 0.0, 1.0),
	    new Point3D.Double(6.0, 0.0, 0.0)
	};

	Point3D knots4Z[] = {
	    new Point3D.Double(1.0, 1.0, 0.0),
	    new Point3D.Double(2.0, 4.0, 0.0),
	    new Point3D.Double(3.0, 9.0, 0.0),
	    new Point3D.Double(4.0, 4.0, 0.0),
	    new Point3D.Double(5.0, 1.0, 0.0),
	    new Point3D.Double(6.0, 0.0, 0.0)
	};

	SplinePath2D spath4 = new SplinePath2D();
	spath4.moveTo(0.0, 0.0);
	spath4.splineTo(knots4, knots4.length, null, null);

	double coords1[] = new double[6];
	double coords2[] = new double[6];

	PathIterator pi1 = spath3.getPathIterator(null);
	PathIterator pi2 = spath4.getPathIterator(null);
	while (!pi1.isDone() && !pi2.isDone()) {
	    int type1 = pi1.currentSegment(coords1);
	    int type2 = pi2.currentSegment(coords2);
	    if (type1 != type2) throw new Exception();
	    for (int i = 0; i < 6; i++) {
		if (coords1[i] != coords2[i]) throw new Exception();
	    }
	    coords1 = new double[6];
	    coords2 = new double[6];
	    pi1.next(); pi2.next();
	}
	if (pi1.isDone() != pi2.isDone()) throw new Exception();

	SplinePath2D spath4a = new SplinePath2D();
	spath4a.moveTo(0.0, 0.0);
	spath4a.splineTo(knots4, knots4.length, cpoint3a, null);

	SplinePath3D spath4aY = new SplinePath3D();
	spath4aY.moveTo(0.0, 0.0, 0.0);
	spath4aY.splineTo(knots4Y, knots4Y.length, cpoint3aY, null);

	SplinePath3D spath4aZ = new SplinePath3D();
	spath4aZ.moveTo(0.0, 0.0, 0.0);
	spath4aZ.splineTo(knots4Z, knots4Z.length, cpoint3aZ, null);

	coords1 = new double[6];
	coords2 = new double[6];
	double[] coordsY = new double[9];
	double[] coordsZ = new double[9];

	pi1 = spath3a.getPathIterator(null);
	pi2 = spath4a.getPathIterator(null);
	PathIterator3D pi2Y = spath4aY.getPathIterator(null);
	PathIterator3D pi2Z = spath4aZ.getPathIterator(null);
	while (!pi1.isDone() && !pi2.isDone()) {
	    int type1 = pi1.currentSegment(coords1);
	    int type2 = pi2.currentSegment(coords2);
	    int type2Y = pi2Y.currentSegment(coordsY);
	    int type2Z = pi2Z.currentSegment(coordsZ);
	    if (type1 != type2) throw new Exception();
	    for (int i = 0; i < 6; i++) {
		if (coords1[i] != coords2[i]) throw new Exception();
	    }
	    if (type2 != type2Y) throw new Exception();
	    if (type2 != type2Z) throw new Exception();
	    int ii, jj;
	    for (ii = 0,jj = 0; ii < 6; ii+=2, jj+=3) {
		if (coordsY[jj] != coords2[ii]
		    || coordsZ[jj] != coords2[ii]
		    || coordsY[jj+1] != 0.0
		    || coordsZ[jj+1] != coords2[ii+1]
		    || coordsY[jj+2] != coords2[ii+1]
		    || coordsZ[jj+2] != 0.0) throw new Exception();
	    }
	    coords1 = new double[6];
	    coords2 = new double[6];
	    coordsY = new double[9];
	    coordsZ = new double[9];
	    pi1.next(); pi2.next();
	    pi2Y.next(); pi2Z.next();
	}
	if (pi1.isDone() != pi2.isDone()) throw new Exception();

	SplinePath2D spath4b = new SplinePath2D();
	spath4b.moveTo(0.0, 0.0);
	spath4b.splineTo(knots4, knots4.length, null, cpoint3b);

	SplinePath3D spath4bY = new SplinePath3D();
	spath4bY.moveTo(0.0, 0.0, 0.0);
	spath4bY.splineTo(knots4Y, knots4Y.length, null, cpoint3bY);

	SplinePath3D spath4bZ = new SplinePath3D();
	spath4bZ.moveTo(0.0, 0.0, 0.0);
	spath4bZ.splineTo(knots4Z, knots4Z.length, null, cpoint3bZ);


	coords1 = new double[6];
	coords2 = new double[6];

	pi1 = spath3b.getPathIterator(null);
	pi2 = spath4b.getPathIterator(null);
	pi2Y = spath4bY.getPathIterator(null);
	pi2Z = spath4bZ.getPathIterator(null);
	while (!pi1.isDone() && !pi2.isDone()) {
	    int type1 = pi1.currentSegment(coords1);
	    int type2 = pi2.currentSegment(coords2);
	    int type2Y = pi2Y.currentSegment(coordsY);
	    int type2Z = pi2Z.currentSegment(coordsZ);
	    if (type1 != type2) throw new Exception();
	    for (int i = 0; i < 6; i++) {
		if (coords1[i] != coords2[i]) throw new Exception();
	    }
	    if (type2 != type2Y) {
		throw new Exception();
	    }
	    if (type2 != type2Z) throw new Exception();
	    int ii, jj;
	    for (ii = 0,jj = 0; ii < 6; ii+=2, jj+=3) {
		if (coordsY[jj] != coords2[ii]
		    || coordsZ[jj] != coords2[ii]
		    || coordsY[jj+1] != 0.0
		    || coordsZ[jj+1] != coords2[ii+1]
		    || coordsY[jj+2] != coords2[ii+1]
		    || coordsZ[jj+2] != 0.0) throw new Exception();
	    }
	    coords1 = new double[6];
	    coords2 = new double[6];
	    coordsY = new double[9];
	    coordsZ = new double[9];
	    pi1.next(); pi2.next();
	    pi2Y.next(); pi2Z.next();
	}
	if (pi1.isDone() != pi2.isDone()) throw new Exception();

	SplinePath2D spath4c = new SplinePath2D();
	spath4c.moveTo(0.0, 0.0);
	spath4c.splineTo(knots4, knots4.length, cpoint3a, cpoint3b);

	SplinePath3D spath4cY = new SplinePath3D();
	spath4cY.moveTo(0.0, 0.0, 0.0);
	spath4cY.splineTo(knots4Y, knots4Y.length, cpoint3aY, cpoint3bY);

	SplinePath3D spath4cZ = new SplinePath3D();
	spath4cZ.moveTo(0.0, 0.0, 0.0);
	spath4cZ.splineTo(knots4Z, knots4Z.length, cpoint3aZ, cpoint3bZ);

	pi1 = spath3c.getPathIterator(null);
	pi2 = spath4c.getPathIterator(null);
	pi2Y = spath4cY.getPathIterator(null);
	pi2Z = spath4cZ.getPathIterator(null);
	while (!pi1.isDone() && !pi2.isDone()) {
	    int type1 = pi1.currentSegment(coords1);
	    int type2 = pi2.currentSegment(coords2);
	    int type2Y = pi2Y.currentSegment(coordsY);
	    int type2Z = pi2Z.currentSegment(coordsZ);
	    if (type1 != type2) throw new Exception();
	    for (int i = 0; i < 6; i++) {
		if (coords1[i] != coords2[i]) throw new Exception();
	    }
	    if (type2 != type2Y) throw new Exception();
	    if (type2 != type2Z) throw new Exception();
	    int ii, jj;
	    for (ii = 0,jj = 0; ii < 6; ii+=2, jj+=3) {
		if (coordsY[jj] != coords2[ii]
		    || coordsZ[jj] != coords2[ii]
		    || coordsY[jj+1] != 0.0
		    || coordsZ[jj+1] != coords2[ii+1]
		    || coordsY[jj+2] != coords2[ii+1]
		    || coordsZ[jj+2] != 0.0) throw new Exception();
	    }
	    coords1 = new double[6];
	    coords2 = new double[6];
	    coordsY = new double[9];
	    coordsZ = new double[9];
	    pi1.next(); pi2.next();
	    pi2Y.next(); pi2Z.next();
	}
	if (pi1.isDone() != pi2.isDone()) throw new Exception();
     }


     public static void main(String argv[]) {
	 try {
	     initialTest();
	     testSymmetric();
	     double xp[] = new double[37];
	     double yp[] = new double[37];
	     Path2D path = new Path2D.Double();
	     SplinePathBuilder spb = new SplinePathBuilder();
	     BasicSplinePathBuilder bspb = new BasicSplinePathBuilder();
	     for (int i = 0; i < 37; i++) {
		 double x = 10 * i;
		 xp[i] = x;
		 yp[i] = dsin(x);
		 if (i == 0) {
		     path.moveTo(xp[i], yp[i]);
		     spb.append(new SplinePathBuilder.CPoint
				(SplinePathBuilder.CPointType.MOVE_TO,
				 xp[i], yp[i]));
		     bspb.append(new SplinePathBuilder.CPoint
				 (SplinePathBuilder.CPointType.MOVE_TO,
				  xp[i], yp[i]));
		 } else {
		     path.lineTo(xp[i], yp[i]);
		     spb.append(new SplinePathBuilder
				.CPoint(((i < 36)?
					 SplinePathBuilder.CPointType.SPLINE:
					 SplinePathBuilder.CPointType.SEG_END),
					xp[i], yp[i]));
		     bspb.append(new SplinePathBuilder
				 .CPoint(((i < 36)?
					  SplinePathBuilder.CPointType.SPLINE:
					  SplinePathBuilder.CPointType.SEG_END),
					 xp[i], yp[i]));
		 }
	     }
	     SplinePath2D curve = new SplinePath2D(xp, yp, xp.length, false);


	     SplinePath2D curve2 = spb.getPath();
	     SplinePath2D curveb = bspb.getPath();

	     Graph bezier1 = new Graph();
	     bezier1.setOffsets(25,25);
	     bezier1.setRanges(0.0, 360.0, -1.0, 1.0);
	     Graphics2D g2d = bezier1.createGraphics();
	     g2d.setColor(Color.RED);
	     bezier1.draw(g2d, path);
	     g2d.setColor(Color.BLACK);
	     bezier1.draw(g2d, curve);
	     bezier1.write("png", "bezier1.png");

	     System.out.println("show entries for curve");
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(curve)) {
		 double[] coords = entry.getCoords();
		 Point2D start = entry.getStart();
		 Point2D end = entry.getEnd();
		 int type = entry.getType();
		 if ( type == PathIterator.SEG_MOVETO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		 } else if (type == PathIterator.SEG_LINETO ||
			    type == PathIterator.SEG_QUADTO ||
			    type == PathIterator.SEG_CUBICTO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		     if (type == PathIterator.SEG_CUBICTO) {
			 System.out.println("    .... ("
					    + coords[0] +", " + coords[1]
					    + "), ("
					    + coords[2] +", " + coords[3] +")");
		     }
		 } else {
		     System.out.println(entry.getTypeString() +": [no coords]");
		 }
	     }

	     System.out.println("repeat for curve2");
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(curve2)) {
		 double[] coords = entry.getCoords();
		 Point2D start = entry.getStart();
		 Point2D end = entry.getEnd();
		 int type = entry.getType();
		 if ( type == PathIterator.SEG_MOVETO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		 } else if (type == PathIterator.SEG_LINETO ||
			    type == PathIterator.SEG_QUADTO ||
			    type == PathIterator.SEG_CUBICTO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		     if (type == PathIterator.SEG_CUBICTO) {
			 System.out.println("    .... ("
					    + coords[0] +", " + coords[1]
					    + "), ("
					    + coords[2] +", " + coords[3] +")");
		     }
		 } else {
		     System.out.println(entry.getTypeString() +": [no coords]");
		 }
	     }
	     System.out.println("repeat for curveb");
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(curveb)) {
		 double[] coords = entry.getCoords();
		 Point2D start = entry.getStart();
		 Point2D end = entry.getEnd();
		 int type = entry.getType();
		 if ( type == PathIterator.SEG_MOVETO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		 } else if (type == PathIterator.SEG_LINETO ||
			    type == PathIterator.SEG_QUADTO ||
			    type == PathIterator.SEG_CUBICTO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		     if (type == PathIterator.SEG_CUBICTO) {
			 System.out.println("    .... ("
					    + coords[0] +", " + coords[1]
					    + "), ("
					    + coords[2] +", " + coords[3] +")");
		     }
		 } else {
		     System.out.println(entry.getTypeString() +": [no coords]");
		 }
	     }

	     double xc[] = new double[18];
	     double yc[] = new double[18];
	     Path2D polygon = new Path2D.Double();
	     for (int i = 0; i < 18; i++) {
		 double theta = Math.toRadians((double)20.0 * i);
		 double r = 100.0;
		 xc[i]  = r * Math.cos(theta);
		 yc[i] = r * Math.sin(theta);
		 if (i == 0) {
		     polygon.moveTo(100.0, 0.0);
		 } else {
		     polygon.lineTo(xc[i], yc[i]);
		     if (i == 35) {
			 // 			polygon.lineTo(xc[0], yc[0]);
			 polygon.closePath();
		     }
		 }
	     }
	     SplinePath2D circle = new SplinePath2D(xc, yc, true);
	     Graph bezier2 = new Graph();
	     bezier2.setOffsets(25,25);
	     bezier2.setRanges(-100.0, 100.0, -100.0, 100.0);
	     g2d = bezier2.createGraphics();
	     g2d.setColor(Color.RED);
	     bezier2.draw(g2d, polygon);
	     g2d.setColor(Color.BLACK);
	     bezier2.draw(g2d, circle);
	     bezier2.write("png", "bezier2.png");

	     Graph bezier3 = new Graph();
	     bezier3.setOffsets(25,25);
	     bezier3.setRanges(-100.0, 100.0, -100.0, 100.0);
	     SplinePath2D mpath = new SplinePath2D();
	     mpath.moveTo(0.0, 0.0);
	     Point2D[] points1 = {
		 new Point2D.Double(5.0, 5.0),
		 new Point2D.Double(10.0, 10.0),
		 new Point2D.Double(15.0, 40.0),
		 new Point2D.Double(20.0, 80.0)
	     };
	     Point2D[] points2 = {
		 new Point2D.Double(25.0, 40.0),
		 new Point2D.Double(30.0, 10.0),
		 new Point2D.Double(35.0, 5.0),
		 new Point2D.Double(40.0, 0.0)
	     };
	     double xpath[] = {-5.0, -10.0, -15.0, -20.0};
	     double ypath[] = {5.0, 10.0, 40.0, 80.0};

	     mpath.splineTo(points1, points1.length);
	     mpath.splineTo(points2);
	     mpath.moveTo(0.0, 0.0);
	     mpath.splineTo(xpath, ypath);
	     mpath.addCycle(xc, yc);
	     g2d = bezier3.createGraphics();
	     g2d.setColor(Color.BLACK);
	     bezier3.draw(g2d, mpath);
	     bezier3.write("png", "bezier3.png");

	     SplinePath2D cpath1 = new SplinePath2D();
	     SplinePath2D cpath2 = new SplinePath2D();
	     Point2D p0 = new Point2D.Double(10.0, 0.0);
	     Point2D p1 = new Point2D.Double(0.0, 10.0);
	     Point2D p2 = new Point2D.Double(-10.0, 0.0);
	     Point2D p3 = new Point2D.Double(0.0, -10.0);
	     System.out.println("cpath1 addCycle");
	     cpath1.addCycle(p0, p1, p2, p3);
	     cpath2.moveTo(p0.getX(), p0.getY());
	     System.out.println("cpath2.cycleTo");
	     cpath2.cycleTo(p1, p2, p3);

	     System.out.println("cpath1:");
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(cpath1)) {
		 double[] coords = entry.getCoords();
		 Point2D start = entry.getStart();
		 Point2D end = entry.getEnd();
		 int type = entry.getType();
		 if ( type == PathIterator.SEG_MOVETO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		 } else if (type == PathIterator.SEG_LINETO ||
			    type == PathIterator.SEG_QUADTO ||
			    type == PathIterator.SEG_CUBICTO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		     if (type == PathIterator.SEG_CUBICTO) {
			 System.out.println("    .... ("
					    + coords[0] +", " + coords[1]
					    + "), ("
					    + coords[2] +", " + coords[3] +")");
		     }
		 } else {
		     System.out.println(entry.getTypeString() +": [no coords]");
		 }
	     }
	     System.out.println("cpath2:");
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(cpath2)) {
		 double[] coords = entry.getCoords();
		 Point2D start = entry.getStart();
		 Point2D end = entry.getEnd();
		 int type = entry.getType();
		 if ( type == PathIterator.SEG_MOVETO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		 } else if (type == PathIterator.SEG_LINETO ||
			    type == PathIterator.SEG_QUADTO ||
			    type == PathIterator.SEG_CUBICTO) {
		     System.out.println(entry.getTypeString() +": ("
					+ end.getX() + ", " + end.getY() + ")");
		     if (type == PathIterator.SEG_CUBICTO) {
			 System.out.println("    .... ("
					    + coords[0] +", " + coords[1]
					    + "), ("
					    + coords[2] +", " + coords[3] +")");
		     }
		 } else {
		     System.out.println(entry.getTypeString() +": [no coords]");
		 }
	     }
	     SplinePath2D cpath3 = new SplinePath2D();
	     SplinePath2D cpath3a = new SplinePath2D();
	     double xs[] = new double[36];
	     double ys[] = new double[36];
	     double xsInner[] = new double[36];
	     double ysInner[] = new double[36];
	     double r = 10.0;
	     for (int i = 0; i < 36; i++) {
		 double theta = Math.toRadians(i * 10.0);
		 xs[i] = r * Math.cos(theta);
		 ys[i] = r * Math.sin(theta);
		 xsInner[i] = -xs[i]/2.0;
		 ysInner[i] = ys[i]/2.0;
	     }
	     cpath3.addCycle(xs, ys, 36);
	     cpath3a.addCycle(xs, ys, 36);
	     cpath3a.addCycle(xsInner, ysInner, 36);
	     double length = 0;
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(cpath3)) {
		 double seglen = entry.getSegmentLength();
		 length += seglen;
	     }
	     System.out.println("cpath3 length = " + length
				+ ", should be " + 2 * Math.PI * r);

	     System.out.println("cpath3a length = "
				+ Path2DInfo.pathLength(cpath3a)
				+ ", should be " + 2 * Math.PI * (r + r/2));

	     SplinePath2D cpath4 = new SplinePath2D();
	     xs = new double[35];
	     ys = new double[35];
	     cpath4.moveTo(r, 0.0);
	     for (int i = 1; i < 36; i++) {
		 double theta = Math.toRadians(i * 10.0);
		 xs[i-1] = r * Math.cos(theta);
		 ys[i-1] = r * Math.sin(theta);
	     }
	     length = 0.0;
	     cpath4.cycleTo(xs,ys,35);
	     for (Path2DInfo.Entry entry: Path2DInfo.getEntries(cpath4)) {
		 double seglen = entry.getSegmentLength();
		 length += seglen;
	     }
	     System.out.println("cpath4 length = " + length
				+ ", should be " + 2 * Math.PI * r);

	     System.out.println();
	     System.out.println("spline with control points");
	     Point2D knots[] = {
		 new Point2D.Double(0.0, 0.0),
		 new Point2D.Double(1.0, 1.0),
		 new Point2D.Double(2.0, 4.0),
		 new Point2D.Double(3.0, 9.0),
		 new Point2D.Double(4.0, 16.0),
		 new Point2D.Double(5.0, 25.0)
	     };
	     SplinePath2D splinePath1 = new
		 SplinePath2D(knots, knots.length, false);
	     Point2D cpoint1;
	     Point2D cpoint2;
	     java.util.List<Path2DInfo.Entry> cplist =
		 Path2DInfo.getEntries(splinePath1);
	     Path2DInfo.Entry[] cparray = new Path2DInfo.Entry[cplist.size()];
	     cplist.toArray(cparray);
	     double[] scoords = cparray[1].getCoords();
	     cpoint1 = new Point2D.Double(scoords[0], scoords[1]);
	     scoords = cparray[cparray.length-1].getCoords();
	     cpoint2 = new Point2D.Double(scoords[2], scoords[3]);
	     SplinePath2D splinePath2 = new
		 SplinePath2D(knots, knots.length, cpoint1, cpoint2);

	     PathIterator pit1 = splinePath1.getPathIterator(null);
	     PathIterator pit2 = splinePath2.getPathIterator(null);
	     double[] scoords1 = new double[6];
	     double[] scoords2 = new double[6];
	     while (!pit1.isDone()) {
		 if (pit2.isDone()) throw new Exception("path iterator");
		 int type1 = pit1.currentSegment(scoords1);
		 int type2 = pit2.currentSegment(scoords2);
		 System.out.println("checking segment with type " + type1);
		 if (type1 != type2) throw new Exception("type");
		 int scind;
		 switch(type1) {
		 case PathIterator.SEG_CUBICTO:
		     if (Math.abs(scoords1[4] - scoords2[4]) > 1.e-10) {
			 scind = 4;
			 System.out.format("%d: %s: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c4");
		     }
		     if (Math.abs(scoords1[5] - scoords2[5]) > 1.e-10) {
			 scind = 5;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c5");
		     }
		 case PathIterator.SEG_QUADTO:
		     if (Math.abs(scoords1[2] - scoords2[2]) > 1.e-10) {
			 scind = 2;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 // throw new Exception("c2");
		     }
		     if (Math.abs(scoords1[3] - scoords2[3]) > 1.e-10) {
			 scind = 3;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c3");
		     }
		 case PathIterator.SEG_MOVETO:
		 case PathIterator.SEG_LINETO:
		     if (Math.abs(scoords1[0] - scoords2[0]) > 1.e-10) {
			 scind = 0;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 // throw new Exception("c0");
		     }
		     if (Math.abs(scoords1[1] - scoords2[1]) > 1.e-10) {
			 scind = 1;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c1");
		     }
		     break;
		 case PathIterator.SEG_CLOSE:
		     throw new Exception("close");
		 }

		 pit1.next();
		 pit2.next();
	     }
	     if (!pit2.isDone()) throw new Exception("path iterator");

	     System.out.println("...");

	     splinePath1 = new SplinePath2D();
	     splinePath1.moveTo(0.0, 0.0);
	     splinePath1.splineTo(knots, 1, knots.length-1);
	     splinePath2 = new SplinePath2D();
	     splinePath2.moveTo(0.0, 0.0);
	     splinePath2.splineTo(knots, 1, knots.length-1, cpoint1, cpoint2);

	     while (!pit1.isDone()) {
		 if (pit2.isDone()) throw new Exception("path iterator");
		 int type1 = pit1.currentSegment(scoords1);
		 int type2 = pit2.currentSegment(scoords2);
		 System.out.println("checking segment with type " + type1);
		 if (type1 != type2) throw new Exception("type");
		 int scind;
		 switch(type1) {
		 case PathIterator.SEG_CUBICTO:
		     if (Math.abs(scoords1[4] - scoords2[4]) > 1.e-10) {
			 scind = 4;
			 System.out.format("%d: %s: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c4");
		     }
		     if (Math.abs(scoords1[5] - scoords2[5]) > 1.e-10) {
			 scind = 5;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c5");
		     }
		 case PathIterator.SEG_QUADTO:
		     if (Math.abs(scoords1[2] - scoords2[2]) > 1.e-10) {
			 scind = 2;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c2");
		     }
		     if (Math.abs(scoords1[3] - scoords2[3]) > 1.e-10) {
			 scind = 3;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c3");
		     }
		 case PathIterator.SEG_MOVETO:
		 case PathIterator.SEG_LINETO:
		     if (Math.abs(scoords1[0] - scoords2[0]) > 1.e-10) {
			 scind = 0;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c0");
		     }
		     if (Math.abs(scoords1[1] - scoords2[1]) > 1.e-10) {
			 scind = 1;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c1");
		     }
		     break;
		 case PathIterator.SEG_CLOSE:
		     throw new Exception("close");
		 }

		 pit1.next();
		 pit2.next();
	     }
	     if (!pit2.isDone()) throw new Exception("path iterator");


	     System.out.println("...");
	     splinePath1 = new SplinePath2D();
	     splinePath1.moveTo(-1.0, 0.0);
	     splinePath1.lineTo(0.0, 0.0);
	     splinePath1.splineTo(knots, 1, knots.length-1);
	     splinePath2 = new SplinePath2D();
	     splinePath2.moveTo(-1.0, 0.0);
	     splinePath2.lineTo(0.0, 0.0);
	     splinePath2.splineTo(knots, 1, knots.length-1, cpoint1, cpoint2);
	     pit1 = splinePath1.getPathIterator(null);
	     pit2 = splinePath2.getPathIterator(null);
	     while (!pit1.isDone()) {
		 if (pit2.isDone()) throw new Exception("path iterator");
		 int type1 = pit1.currentSegment(scoords1);
		 int type2 = pit2.currentSegment(scoords2);
		 System.out.println("checking segment with type " + type1);
		 if (type1 != type2) throw new Exception("type");
		 int scind;
		 switch(type1) {
		 case PathIterator.SEG_CUBICTO:
		     if (Math.abs(scoords1[4] - scoords2[4]) > 1.e-10) {
			 scind = 4;
			 System.out.format("%d: %s: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c4");
		     }
		     if (Math.abs(scoords1[5] - scoords2[5]) > 1.e-10) {
			 scind = 5;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c5");
		     }
		 case PathIterator.SEG_QUADTO:
		     if (Math.abs(scoords1[2] - scoords2[2]) > 1.e-10) {
			 scind = 2;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c2");
		     }
		     if (Math.abs(scoords1[3] - scoords2[3]) > 1.e-10) {
			 scind = 3;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c3");
		     }
		 case PathIterator.SEG_MOVETO:
		 case PathIterator.SEG_LINETO:
		     if (Math.abs(scoords1[0] - scoords2[0]) > 1.e-10) {
			 scind = 0;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c0");
		     }
		     if (Math.abs(scoords1[1] - scoords2[1]) > 1.e-10) {
			 scind = 1;
			 System.out.format("%d: %s != %s\n", scind,
					   scoords1[scind], scoords2[scind]);
			 throw new Exception("c1");
		     }
		     break;
		 case PathIterator.SEG_CLOSE:
		     throw new Exception("close");
		 }

		 pit1.next();
		 pit2.next();
	     }
	     if (!pit2.isDone()) throw new Exception("path iterator");





	     BasicSplinePath2D splinePath3 = new BasicSplinePath2D();
	     splinePath3.moveTo(-1.0, 0.0);
	     splinePath3.lineTo(0.0, 0.0);
	     cpoint1.setLocation(cpoint1.getX(), 0.0);
	     System.out.println("cpoint1 = " + cpoint1);
	     System.out.println("cpoint2 = " + cpoint2);
	     splinePath3.splineTo(knots, 1, knots.length-1, cpoint1, cpoint2);
	     Graph bezier4 = new Graph();
	     bezier4.setOffsets(25,25);
	     bezier4.setRanges(0.0, 6.0, -1.0, 26.0);
	     bezier4.setBackgroundColor(Color.WHITE);
	     bezier4.clear();
	     g2d = bezier4.createGraphics();
	     g2d.setColor(Color.BLACK);
	     bezier4.draw(g2d, splinePath3);
	     bezier4.write("png", "bezier4.png");

	     System.out.println("splinePath3:");
	     splinePath3.printTable();
	     double[] tangent = new double[2];
	     splinePath3.getTangent(1.0, tangent);
	     System.out.format("tangent at 1.0 = [%s, %s]\n",
			       tangent[0], tangent[1]);
	     splinePath3.getTangent(1.1, tangent);
	     System.out.format("tangent at 1.1 = [%s, %s]\n",
			       tangent[0], tangent[1]);

	     Point2D knots2[] = {
		 new Point2D.Double(30.0, 50.0),
		 new Point2D.Double(0.0, 0.0)
	     };
	     System.out.println("splinePath4a ...");
	     BasicSplinePath2D  splinePath4 = new BasicSplinePath2D();
	     splinePath4.moveTo(0.0, 0.0);
	     cpoint1.setLocation(5.0, 10.0);
	     System.out.println("cpoint1 = " + cpoint1);
	     splinePath4.splineTo(knots2, knots2.length, cpoint1, null);
	     Path2DInfo.printSegments(splinePath4);

	     System.out.println("splinePath4b ...");
	     splinePath4 = new BasicSplinePath2D();
	     splinePath4.moveTo(0.0, 0.0);
	     cpoint2.setLocation(15.0, 30.0);
	     System.out.println("cpoint2 = " + cpoint2);
	     splinePath4.splineTo(knots2, knots2.length, cpoint1, cpoint2);
	     Path2DInfo.printSegments(splinePath4);

	     System.out.println("splinePath4c ...");
	     splinePath4 = new BasicSplinePath2D();
	     splinePath4.moveTo(0.0, 0.0);
	     cpoint2.setLocation(15.0, 30.0);
	     splinePath4.splineTo(knots2, knots2.length, null, cpoint2);
	     Path2DInfo.printSegments(splinePath4);


	 } catch (Exception e) {
	     e.printStackTrace();
	     System.exit(1);
	 }
	 System.exit(0);
     }
}
