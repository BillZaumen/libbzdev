import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;


public class Sphere2 {

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
		    if (x > -65.0 && x < -35.0
			&& y > -15.0 && y < 15.0) {
			System.out.format("x = %g, y = %g, i = %d, j = %d\n",
					  x, y, i, j);
		    }
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
	Surface3D surface1 = new Surface3D.Double();
	Surface3D surface2 = new Surface3D.Double();
	double[] coords = new double[48];
	double[] ucoords0 = new double[12];
	double[] ucoords1 = new double[12];
	double[] vcoords0 = new double[12];
	double[] vcoords1 = new double[12];
	for (int i = 0; i < 20; i++) {
	    for (int j = 0; j < 20; j++) {
		if (j >= 6 && j <= 7 && i >=9 && i <= 11) {
		    continue;
		}
		upaths[i].getSegment(j, ucoords0);
		upaths[i+1].getSegment(j, ucoords1);
		vpaths[j].getSegment(i, vcoords0);
		vpaths[j+1].getSegment(i, vcoords1);
		Surface3D.setupU0ForPatch(ucoords0, coords, false);
		Surface3D.setupU1ForPatch(ucoords1, coords, false);
		Surface3D.setupV0ForPatch(vcoords0, coords, false);
		Surface3D.setupV1ForPatch(vcoords1, coords, false);
		Surface3D.setupRestForPatch(coords);
		surface1.addCubicPatch(coords);
		for (int k = 2; k < coords.length; k += 3) {
		    coords[k] = -coords[k];
		}
		surface2.addCubicPatch(coords);

	    }
	}
	surface2.reverseOrientation();
	System.out.println("checking surface1");
	if (!surface1.isWellFormed(System.out)) {
	    System.exit(1);
	}
	System.out.println("checking surface2");
	if (!surface2.isWellFormed(System.out)) {
	    System.exit(1);
	}

	surface.append(surface1);
	surface.append(surface2);
	System.out.println("checking surface");
	if (!surface.isWellFormed(System.out)) {
	    System.exit(1);
	}

	Path3D boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("not well formed");
	} else {
	    if (boundary.isEmpty()) {
		System.out.println("no boundary");
	    } else {
		System.out.println("Connecting boundaries");
		Path3DInfo.printSegments(boundary);
		Path3D[] boundaries = PathSplitter.split(boundary);
		Path3DInfo.printSegments(boundaries[0]);
		PathIterator3D pit = boundaries[0].getPathIterator(null);
		double[] coords1 = new double[12];
		double[] coords2 = new double[12];
		double[] ecoords = new double[9];
		double x = 0.0, y = 0.0 , z = 0.0;
		while (!pit.isDone()) {
		    int type1 = pit.currentSegment(ecoords);
		    if (type1 == PathIterator3D.SEG_MOVETO) {
			x = ecoords[0];
			y = ecoords[1];
			z = ecoords[2];
		    } else if (type1 == PathIterator3D.SEG_CUBICTO) {
			Surface3D.setupU0ForPatch(x, y, z,
						  ecoords, coords, false);
			ecoords[2] = - ecoords[2];
			ecoords[5] = - ecoords[5];
			double ztmp = ecoords[8];
			ecoords[8] = - ecoords[8];
			Surface3D.setupU1ForPatch(x, y, -z,
						  ecoords, coords, false);
			Surface3D.setupV0ForPatch
			    (Path3D.setupCubic(x, y, z, x, y, -z),
			     coords, false);
			x = ecoords[6];
			y = ecoords[7];
			z = ztmp;
			Surface3D.setupV1ForPatch
			    (Path3D.setupCubic(x, y, z, x, y, -z),
			     coords, false);
			Surface3D.setupRestForPatch(coords);
			surface.addCubicPatch(coords);
		    }
		    pit.next();
		}
	    }
	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("not well formed");
	    System.exit(1);
	}

	System.out.println("area = " + surface.area()
			   + ", expecting larger than "
			   + (4*Math.PI*r*r));

	System.out.println("volume = " + surface.volume()
			   + ", expecting less than "
			   + (4*Math.PI*r*r*r/3));

	boundary = surface.getBoundary();
	if (!boundary.isEmpty()) {
	    System.out.println("boundary is not empty");
	    System.exit(1);
	}
	System.exit(0);
    }
}
