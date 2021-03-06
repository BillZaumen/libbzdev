import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;


public class MTorus {
    public static void main(String argv[]) throws Exception {
	int N = 64;
	int M = 36;
	// int N = 8;
	// int M = 4;

	Point3D[][] array = new Point3D[N][M];
	Point3D[] template = new Point3D[M];

	double r1 = 100.0;
	double r2 = 30.0;

	for (int i = 0; i < M; i++) {
	    double theta = (2 * Math.PI * i)/M;
	    double x = r1 + r2 * Math.cos(theta);
	    double y = 0.0;
	    double z = r2 * Math.sin(theta);
	    template[i] = new Point3D.Double(x, y, z);
	    array[0][i] = template[i];
	}

	for (int i = 1; i < N; i++) {
	    double phi = (2 * Math.PI * i)/N;
	    AffineTransform3D af =
		AffineTransform3D.getRotateInstance(phi, 0.0, 0.0);
	    for (int j = 0; j < M; j++) {
		array[i][j] = af.transform(template[j], null);
	    }
	}

	BezierGrid grid = new BezierGrid(array, true, true);

	grid.setRegion(32, 16, 4, 4, 1);
	grid.setRegion(0,0, 1);
	grid.setRegion(0,35, 1);
	grid.setRegion(63,0, 1);
	grid.setRegion(63,35, 1);

	grid.setRegion(15,15,1);

	grid.startSpline(45, 20);
	grid.moveU(1);
	grid.endSpline(false);
	grid.startSpline(45, 20);
	grid.moveV(1);
	grid.endSpline(false);
	grid.startSpline(47, 20);
	grid.moveU(-1);
	grid.endSpline(false);
	grid.startSpline(47, 20);
	grid.moveV(-1);
	grid.endSpline(false);

	double[] coords1 = new double[12];
	grid.getFullSplineU(45,20, coords1);
	double[] coords2 = Path3D.setupCubic(grid.getPoint(45,20),
					     grid.getPoint(46,20));
	boolean error = false;
	for (int i = 0; i < 12; i++) {
	    if (Math.abs((double)(float)coords2[i]
			 - (double)(float)coords1[i]) > 1.e-10) {
		System.out.format("i=%d: desired %g, actual %g\n", i,
				  coords2[i], coords1[i]);
		error = true;
	    }
	}
	if (error) throw new Exception("bad spline seen");

	grid.getFullSplineV(45,20, coords1);
	coords2 = Path3D.setupCubic(grid.getPoint(45,20), grid.getPoint(45,21));
	for (int i = 0; i < 12; i++) {
	    if (Math.abs((double)(float)coords2[i] - (double)(float)coords1[i])
		> 1.e-10) {
		System.out.format("desired %g, actual %g\n", coords2[i],
				  coords1[i]);
		error = true;
	    }
	}
	if (error) throw new Exception("bad spline seen");

	grid.getSplineU(46,20, coords1);
	coords2 = Path3D.setupCubic(grid.getPoint(46,20), grid.getPoint(47,20));
	for (int i = 0; i < 9; i++) {
	    if (Math.abs((double)(float)coords2[i+3] -
			 (double)(float)coords1[i]) > 1.e-10) {
		System.out.format("desired %g, actual %g\n", coords2[i+3],
				  coords1[i]);
		error = true;
	    }
	}
	if (error) throw new Exception("bad spline seen");


	grid.getSplineV(47,19, coords1);
	coords2 = Path3D.setupCubic(grid.getPoint(47,19), grid.getPoint(47,20));
	for (int i = 0; i < 9; i++) {
	    if (Math.abs((double)(float)coords2[i+3]
			 - (double)(float)coords1[i]) > 1.e-10) {
		System.out.format("desired %g, actual %g\n", coords2[i+3],
				  coords1[i]);
		error = true;
	    }
	}
	if (error) throw new Exception("bad spline seen");


	System.out.println("grid bounding box: " + grid.getBounds());


	if (!grid.isWellFormed(System.out)) {
	    System.exit(1);
	}

	if(grid.badSplines(System.out)) {
	    System.exit(1);
	}
	SurfaceIterator sit = grid.getSurfaceIterator(null);
	double[] coords = new double[48];

	Path3D bpath = grid.getBoundary();
	if (!bpath.isEmpty()) {
	    System.out.println("grid boundary:");
	    Path3DInfo.printSegments(bpath);
	}
	
	
	Surface3D surface = new Surface3D.Double();
	surface.append(grid);
	if (!surface.isWellFormed(System.out)) {
	    System.exit(1);
	}

	double area = surface.area();
	double volume = surface.volume();
	double tarea = 4*Math.PI*Math.PI*r1*r2;
	double tvolume = 2*Math.PI*Math.PI*r1*r2*r2;
	System.out.format("area = %s, expecting %s\n", area, tarea);
	System.out.format("volume = %s, expecting %s\n", volume, tvolume);
	System.exit(0);
   }
}
