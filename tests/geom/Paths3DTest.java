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

    }
}
