import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;

public class Paths3DTest {

    public static void main(String argv[]) throws Exception {

	double tangent[] = {1.0, 1.0, 0.0};
	VectorOps.normalize(tangent);
	double normal[] = {0.0, 0.0, 1.0};

	double[] binormal = VectorOps.crossProduct(tangent, normal);

	Path3D arc = Paths3D.createArc(10.0, 20.0, 30.0,
				       tangent, normal,
				       100.0,
				       Math.PI/2);

	double expected = 100*Math.PI/2;
	System.out.println("path length = " + Path3DInfo.pathLength(arc)
			   + ", expecting " + expected);
	if (Math.abs((Path3DInfo.pathLength(arc) - expected)/expected)
	    > 1.e-8) {
	    throw new Exception();
	}
	double xc = 10.0;
	double yc = 20.0;
	double zc = 130.0;
	double[] cpoints =
	    Path3DInfo.getControlPoints(arc.getPathIterator(null), true);

	for (int i = 0; i < cpoints.length; i += 3) {
	    cpoints[i] -= xc;
	    cpoints[i+1] -= yc;
	    cpoints[i+2] -= zc;
	    if (Math.abs(VectorOps.dotProduct(cpoints, i, binormal, 0, 3))
		> 1.e-10) {
		throw new Exception();
	    }
	}
	
	Path3D arc2 = Paths3D.reverse(arc);
	System.out.println("reversed path length = "
			   + Path3DInfo.pathLength(arc2)
			   + ", expecting " + expected);
	if (Math.abs((Path3DInfo.pathLength(arc2) - expected)/expected)
	    > 1.e-8) {
	    throw new Exception();
	}
	cpoints = Path3DInfo.getControlPoints(arc2.getPathIterator(null), true);

	for (int i = 0; i < cpoints.length; i += 3) {
	    cpoints[i] -= xc;
	    cpoints[i+1] -= yc;
	    cpoints[i+2] -= zc;
	    if (Math.abs(VectorOps.dotProduct(cpoints, i, binormal, 0, 3))
		> 1.e-10) {
		throw new Exception();
	    }
	}

	Path3D cpath = new Path3D.Double();
	cpath.append(arc, false);
	cpath.append(arc, false);
	Path3D [] subpaths = PathSplitter.split(cpath);
	if (subpaths.length != 2) {
	    throw new Exception();
	}
	System.out.println("subpath-1 length = "
			   + Path3DInfo.pathLength(subpaths[0])
			   + ", expecting " + expected);
	System.out.println("subpath-2 length = "
			   + Path3DInfo.pathLength(subpaths[1])
			   + ", expecting "  + expected);

	Path3D rcpath = Paths3D.reverse(cpath);
	subpaths = PathSplitter.split(rcpath);
	if (subpaths.length != 2) {
	    throw new Exception();
	}
	System.out.println("reversed subpath-1 length = "
			   + Path3DInfo.pathLength(subpaths[0])
			   + ", expecting " + expected);
	System.out.println("reverse subpath-2 length = "
			   + Path3DInfo.pathLength(subpaths[1])
			   + ", expecting "  + expected);

	Path3D circle = Paths3D.createArc(10.0, 20.0, 30.0,
					  tangent, normal,
					  100.0,
					  2*Math.PI);
	circle.closePath();
	Path3DInfo.printSegments(circle);
	double circumference = 2 * Math.PI * 100.0;
	System.out.println("circumference = " + Path3DInfo.pathLength(circle)
			   + ", expecting " + circumference);

	if (Math.abs((Path3DInfo.pathLength(circle) - circumference)
		     / circumference) > 1.e-7) {
	    throw new Exception();
	}

	Path3D circle2 = Paths3D.reverse(circle);
	if (!Path3DInfo.isClosed(circle2)) {
	    throw new Exception();
	}
	System.out.println("circumference of circle-2 = "
			   + Path3DInfo.pathLength(circle2)
			   + ", expecting " + circumference);

	if (Math.abs((Path3DInfo.pathLength(circle2) - circumference)
		     / circumference) > 1.e-7) {
	    throw new Exception();
	}

	double[] cpoints1 = Path3DInfo
	    .getControlPoints(circle.getPathIterator(null), false);
	double[] cpoints2 = Path3DInfo
	    .getControlPoints(circle2.getPathIterator(null), false);
	if (cpoints1.length != cpoints2.length) {
	    throw new Exception();
	}
	double[] tmp = new double[3];
	for (int i = 0; i < cpoints2.length; i += 3) {
	    System.arraycopy(cpoints2, i, tmp, 0, 3);
	    tmp[0] -= xc;
	    tmp[1] -= yc;
	    tmp[2] -= zc;
	    if (Math.abs(VectorOps.norm(tmp) - 100.0) > 1.e-10) {
		System.out.println("i = " + i + ", norm = "
				   + VectorOps.norm(tmp));
		throw new Exception();
	    }
	}

	Path3D path = new Path3D.Double();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0);

	Path3D path2 = Paths3D.shiftClosedPath(path, 8.0, 0.0, 5.0);

	if (path2 != null) throw new Exception("null path expected");
	
	path.closePath();	

	path2 = Paths3D.shiftClosedPath(path, 8.0, 0.0, 5.0);
	double[][] expecting = {
	    {8.0, 0.0, 5.0},
	    {8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0},
	    {-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0},
	    {-8.0, 0.0, 1.0},
	    {-8.0, -4.0, 2.0},
	    {0.0, -4.0, 3.0},
	    {8.0, -4.0, 4.0, 8.0, 0.0, 5.0},
	    {}
	};

	PathIterator3D pi = path2.getPathIterator(null);
	int cnt = 0;
	double[] pcoords = new double[9];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			System.out.println("cnt = " + cnt);
			System.out.format("pcoords = {%g, %g, %g}\n",
					  pcoords[0], pcoords[1], pcoords[2]);
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}
	// Path3DInfo.printSegments(path2);

	path.lineTo(-7.0, 0.0, 10.0);
	path.lineTo(-7.0, 1.0, 11.0);
	Path3D path3 = Paths3D.shiftClosedPath(path, 8.0, 0.0, 5.0);

	double[] coords2 = new double[9];
	double[] coords3 = new double[9];

	PathIterator3D pi2 = path2.getPathIterator(null);
	PathIterator3D pi3 = path3.getPathIterator(null);
	while (!pi2.isDone() && !pi3.isDone()) {
	    int type2 = pi2.currentSegment(coords2);
	    int type3 = pi3.currentSegment(coords3);
	    if (type2 != type3) throw new Exception("types");
	    switch(type2) {
	    case PathIterator3D.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    }
	    pi2.next();
	    pi3.next();
	}
	if (pi2.isDone() != pi3.isDone()) {
	    throw new Exception("path segments");
	}

	path.reset();

	path.moveTo(10.0, 20.0, -1.0);
	path.lineTo(30.0, 40.0, -2.0);
	path.moveTo(40.0, 40.0, -3.0);
	path.lineTo(50.0, 40.0, -4.0);
	path.lineTo(50.0, 50.0, -5.0);
	path.moveTo(40.0, 40.0, -6.0);
	path.closePath();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0);
	path.closePath();	

	path2 = Paths3D.shiftClosedPath(path, 8.0, 0.0, 5.0);
	// This is actually no shift at all: tested as a corner case
	System.out.println("setting up path4 & path3 ");
	Path3D path4 = new Path3D.Double();
	path4.moveTo(-8.0, 0.0, 1.0);
	path4.lineTo(-8.0, -4.0, 2.0);
	path4.lineTo(0.0, -4.0, 3.0);
	path4.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path4.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path4.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 0.0, 1.0);
	path4.closePath();

	path3 = Paths3D.shiftClosedPath(path4, -8.0, 0.0, 1.0);
	if (path3 == null) throw new Exception("path3 is null");


	System.out.println("path4:");
	Path3DInfo.printSegments(path4);
	System.out.println("path3:");
	Path3DInfo.printSegments(path3);

	PathIterator3D pit = path4.getPathIterator(null);
	PathIterator3D pit3 = path3.getPathIterator(null);
	double[] bcoords = new double[9];
	double[] bcoords2 = new double[9];
	int count = 0;
	while (!pit.isDone() && ! pit3.isDone()) {
	    int type = pit.currentSegment(bcoords);
	    int type2 = pit3.currentSegment(bcoords2);
	    if (type != type2) throw new Exception("type at " + count);
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
	    case PathIterator3D.SEG_LINETO:
		for (int k = 0; k < 3; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int k = 0; k < 6; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		for (int k = 0; k < 9; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    }
	    pit.next();
	    pit3.next();
	    count++;
	}
	if (pit.isDone() != pit3.isDone()) {
	    throw new Exception("boundary length mismatch");
	}


	pi = path2.getPathIterator(null);
	cnt = 0;
	pcoords = new double[9];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}

	System.out.println("test aligning paths");
	Path3D circle3 = Paths3D.createArc(10.5, 20.5, 35.0,
					  tangent, normal,
					  100.0,
					  2*Math.PI);
	circle3.closePath();

	double[] carray3 = Path3DInfo.getControlPoints(circle3, false);
	
	for (int j = 0; j < carray3.length; j += 3) {
	    Path3D scircle3 = Paths3D.shiftClosedPath(circle3, carray3[j],
							 carray3[j+1],
							 carray3[j+2]);
	    if (scircle3 == null) {
		// Path3DInfo.printSegments(circle3);
		throw new Exception();
	    }

	    // check alignCLosedPaths.

	    Path3D aligned1 = Paths3D.alignClosedPaths(circle, circle3);
	    Path3D aligned2 = Paths3D.alignClosedPaths(circle, scircle3);
	    Path3D aligned3 = Paths3D
		.alignClosedPaths(circle,
				  Paths3D.reverse(circle3));
	    Path3D aligned4 = Paths3D
		.alignClosedPaths(circle,
				  Paths3D.reverse(scircle3));

	    double[] array0 = Path3DInfo.getControlPoints(circle3, true);
	    double[] array1 = Path3DInfo.getControlPoints(aligned1, true);
	    double[] array2 = Path3DInfo.getControlPoints(aligned2, true);
	    double[] array3 = Path3DInfo.getControlPoints(aligned3, true);
	    double[] array4 = Path3DInfo.getControlPoints(aligned4, true);

	    for (int i = 0; i < array0.length; i++) {
		if (array0[i] != array1[i]) throw new Exception();
		if (array0[i] != array2[i]) throw new Exception();
		if (array0[i] != array3[i]) throw new Exception();
		if (array0[i] != array4[i]) throw new Exception();
	    }
	}
    }
}
