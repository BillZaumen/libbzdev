import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;


public class BGSphere2 {
    public static void main(String argv[]) throws Exception {
	int N = 41;
	int NC = N/2;
	
	Point3D[][] array1 = new Point3D[N][N];
	Point3D[][] array2 = new Point3D[N][N];

	double r = 100.0;

	for (int i = 0; i < N; i++) {
	    for (int j = 0; j < N; j++) {
		int k = Math.max(Math.abs(i-NC),Math.abs(j-NC));
		double theta = k*(Math.PI/(N-1));
		double x, y, z;
		if (k == 0) {
		    x = 0.0; y = 0.0; z = r;
		} else {
		    int nanglesHalf = k*4;
		    double delta = Math.PI/(nanglesHalf);
		    double angle;
		    if (i == NC+k) {
			angle = -(NC-j)*delta;
		    } else if (j == NC-k) {
			angle = -(NC + 2*k - i)*delta;
		    } else if (i == NC-k) {
			angle = -((j-NC) + 4*k)*delta;
		    } else if (j == NC+k) {
			angle = (NC+2*k-i)*delta;
		    } else {
			throw new Error();
		    }
		    x = r * Math.cos(angle) * Math.sin(theta);
		    y = r * Math.sin(angle) * Math.sin(theta);
		    z = r * Math.cos(theta);
		}
		if (k == NC) z = 0.0;
		array1[i][j] = new Point3D.Double(x, y, z);
		array2[i][j] = new Point3D.Double(x, y, -z);
	    }
	}

	// Create the sphere
	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);
	grid2.reverseOrientation(true);

	grid1.remove(NC-3, NC-3, 6, 6);

	grid1.startSpline(NC-3, NC-3);
	grid1.moveU(6);
	grid1.moveV(6);
	grid1.moveU(-6);
	grid1.moveV(-6);
	grid1.endSpline(true);
	grid1.printSplines();


	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	// Now check the grids

	System.out.println("checking grid1");
	if (!grid1.isWellFormed(System.out)) {
	    System.exit(1);
	}

	System.out.println("checking grid2");
	if (!grid2.isWellFormed(System.out)) {
	    System.exit(1);
	}


	System.out.println("checking surface");
	// The surface should be well formed, with an
	// empty boundary.  We also check the surface's
	// area, volume, and the radius at a large number
	// of points. These are compared to the values for
	// a perfect sphere

	if (!surface.isWellFormed(System.out)) {
	    System.exit(1);
	}


	Path3D boundary = surface.getBoundary();
	double[] pcoords = new double[9];
	if (boundary == null) {
	    System.out.println("not well formed");
	} else {
	    if (boundary.isEmpty()) {
		System.out.println("no boundary");
	    } else {
		PathIterator3D pit = boundary.getPathIterator(null);
		double x = Double.NaN;
		double y = Double.NaN;
		double z = Double.NaN;

		double radius = 0.0;

		while (!pit.isDone()) {
		    switch(pit.currentSegment(pcoords)) {
		    case PathIterator3D.SEG_MOVETO:
			x = pcoords[0];
			y = pcoords[1];
			z = pcoords[2];
			radius = Math.sqrt(x*x + y*y);
			break;
		    case PathIterator3D.SEG_CUBICTO:
			for (int k = 0; k <= 10; k++) {
			    double u = k/10.0;
			    if (k == 0) u = 0.0;
			    if (k == 10) u = 1.0;
			    double px =
				Path3DInfo.getX(u, x, y, z, 
						PathIterator3D.SEG_CUBICTO,
						pcoords);
			    double py =
				Path3DInfo.getY(u, x, y, z, 
						PathIterator3D.SEG_CUBICTO,
						pcoords);
			    if (Math.abs(Math.sqrt(px*px + py*py) - radius)
				> 3.0e-1) {
			    System.out.println("radius out of bounds: "
					       +Math.abs(Math.sqrt(px*px+py*py))
					       + " != "
					       + radius);
			}
			    
			}
			x = pcoords[6];
			y = pcoords[7];
			z = pcoords[8];
			
			if (Math.abs(Math.sqrt(x*x + y*y) - radius) > 1.e-5) {
			    System.out.println("radius out of bounds");
			}
			break;
		    }
		    pit.next();
		}

		System.out.println("boundary:");
		Path3DInfo.printSegments(System.out, boundary);
	    }
	}

	SurfaceIterator sit = surface.getSurfaceIterator(null);

	double[] vector = new double[3];
	BasicStats stats = new BasicStats.Population();

	double[] coords = new double[48];
	while (!sit.isDone()) {
	    // always a cubic patch for this case.
	    sit.currentSegment(coords);
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
	    sit.next();
	}
	System.out.println("mean r = " + stats.getMean()
			   + ", std dev = " + stats.getSDev());
    }
}
