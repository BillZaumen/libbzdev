import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;


public class Sphere3 {
    public static void main(String argv[]) throws Exception {
	
	Point3D[][] array = new Point3D[21][21];

	double r = 100.0;

	for (int i = 0; i < 21; i++) {
	    for (int j = 0; j < 21; j++) {
		int k = Math.max(Math.abs(i-10),Math.abs(j-10));
		double theta = k*(Math.PI/20);
		double x, y, z;
		if (k == 0) {
		    x = 0.0; y = 0.0; z = r;
		} else {
		    int nanglesHalf = k*4;
		    double delta = Math.PI/(nanglesHalf);
		    double angle;
		    if (i == 10+k) {
			angle = (10-j)*delta;
		    } else if (j == 10-k) {
			angle = (10 + 2*k - i)*delta;
		    } else if (i == 10-k) {
			angle = ((j-10) + 4*k)*delta;
		    } else if (j == 10+k) {
			angle = -(10+2*k-i)*delta;
		    } else {
			throw new Error();
		    }
		    x = r * Math.cos(angle) * Math.sin(theta);
		    y = r * Math.sin(angle) * Math.sin(theta);
		    z = r * Math.cos(theta);
		}
		if (k == 10) z = 0.0;
		array[i][j] = new Point3D.Double(x, y, z);
	    }
	}
	BasicSplinePath3D[] upaths = new BasicSplinePath3D[21];
	BasicSplinePath3D[] vpaths = new BasicSplinePath3D[21];
	for (int i = 0; i < 21; i++) {
	    vpaths[i] = new BasicSplinePath3D(array[i], false);
	}
	for (int j = 0; j < 21; j++) {
	    Point3D[] tmp = new Point3D[21];
	    for (int i = 0; i < 21; i++) {
		tmp[i] = array[i][j];
	    }
	    upaths[j] = new BasicSplinePath3D(tmp, false);
	}

	Surface3D surface = new Surface3D.Double();
	double[] coords = new double[48];
	double[] ucoords0 = new double[12];
	double[] ucoords1 = new double[12];
	double[] vcoords0 = new double[12];
	double[] vcoords1 = new double[12];
	for (int i = 0; i < 20; i++) {
	    for (int j = 0; j < 20; j++) {
		upaths[i].getSegment(j, ucoords0);
		upaths[i+1].getSegment(j, ucoords1);
		vpaths[j].getSegment(i, vcoords0);
		vpaths[j+1].getSegment(i, vcoords1);
		Surface3D.setupU0ForPatch(ucoords0, coords, false);
		Surface3D.setupU1ForPatch(ucoords1, coords, false);
		Surface3D.setupV0ForPatch(vcoords0, coords, false);
		Surface3D.setupV1ForPatch(vcoords1, coords, false);
		Surface3D.setupRestForPatch(coords);
		surface.addCubicPatch(coords);
		for (int k = 2; k < coords.length; k += 3) {
		    coords[k] = -coords[k];
		}
		surface.addFlippedCubicPatch(coords);

	    }
	}

	Path3D boundary = surface.getBoundary();
	if (boundary == null) {
	    System.out.println("not well formed");
	} else {
	    if (boundary.isEmpty()) {
		System.out.println("no boundary");
	    } else {
		Path3DInfo.printSegments(System.out, boundary);
	    }
	}
	System.out.println("area = " + surface.area()
			   + ", expecting " + (r*r*4*Math.PI));

	System.out.println("volume = " + surface.volume()
			   + ", expecting " + (r*r*r*4*Math.PI/3));
	SurfaceIterator pit = surface.getSurfaceIterator(null);

	double[] vector = new double[3];
	BasicStats stats = new BasicStats.Population();
	while (!pit.isDone()) {
	    // always a cubic patch for this case.
	    pit.currentSegment(coords);
	    for (int i = 0; i < 11; i++) {
		for (int j = 0; j < 11; j++) {
		    double u = i/10.0;
		    double v = j/10.0;
		    if (u < 0.0) u = 0.0;
		    if (v < 0.0) v = 0.0;
		    if (u > 1.0) u = 1.0;
		    if (v > 1.0) v = 1.0;
		    Surface3D.segmentValue(vector, SurfaceIterator.CUBIC_PATCH,
					   coords, u, v);
		    stats.add(VectorOps.norm(vector));
		}
	    }
	    pit.next();
	}
	System.out.println("mean r = " + stats.getMean()
			   + ", std dev = " + stats.getSDev());
    }
}
