import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;

public class NTorus {
    public static void main(String argv[]) throws Exception {
	// int N = 64; not needed - will use an explicit loop.
	int M = 16;		// crude torus - OK for this test
	// int N = 8;
	// int M = 4;

	boolean showVol = argv[0].equals("vol");
	boolean showArea = argv[0].equals("area");
	boolean showCM = argv[0].equals("cm");
	boolean showM = argv[0].equals("moments");

	for (int N = 4; N < 256; N++) {
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
		    AffineTransform3D.getRotateInstance(phi, 0.0, 0);
		for (int j = 0; j < M; j++) {
		    array[i][j] = af.transform(template[j], null);
		}
	    }

	    BezierGrid grid = new BezierGrid(array, true, true);

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
	    if (!surface.isOriented()) {
		System.exit(1);
	    }
	    if (!surface.isClosedManifold()) {
		System.exit(1);
	    }
	    surface.area();
	    double vol = surface.volume();
	    Point3D cm = null;
	    if (showCM || showM) {
		cm = SurfaceOps.centerOfMassOf(surface, vol);
	    }
	    System.gc();
	    long t0 = System.nanoTime();
	    double area = (showArea)? surface.area(false): 0.0;
	    long t1 = System.nanoTime();
	    area = surface.area(true);
	    long t2 = System.nanoTime();
	    double volume = surface.volume(false);
	    long t3 = System.nanoTime();
	    volume = (showVol)? surface.volume(true): 0.0;
	    long t4 = System.nanoTime();
	    if (showCM) {
		t0 = System.nanoTime();
		Point3D cm2 = SurfaceOps.centerOfMassOf(surface, vol,
							false, 0);
		t1 = System.nanoTime();
		cm2 = SurfaceOps.centerOfMassOf(surface, vol, true,
						surface.size());
		t2 = System.nanoTime();
	    }
	    if (showM) {
		t0 = System.nanoTime();
		double[][] moments = SurfaceOps.momentsOf(surface, cm, vol,
							  false, 0);
		t1 = System.nanoTime();
		moments = SurfaceOps.momentsOf(surface, cm, vol,
					       true, surface.size());
		t2 = System.nanoTime();
	    }
	    if (showArea) {
		System.out.format("(area) size = %d, "
				  + "times = %d, %d microseconds\n",
				  surface.size(),
				  ((t1-t0)/1000L), ((t2-t1)/1000L));
	    }
	    if (showVol) {
		System.out.format("(vol) size = %d, "
				  + "times = %d, %d microseconds\n",
				  surface.size(),
				  ((t3-t2)/1000L), ((t4-t3)/1000L));
	    }
	    if (showCM) {
		System.out.format("(cm) size = %d, "
				  + "times = %d, %d microseconds\n",
				  surface.size(),
				  ((t1-t0)/1000L), ((t2-t1)/1000L));
	    }
	    if (showM) {
		System.out.format("(moment) size = %d, "
				  + "times = %d, %d microseconds\n",
				  surface.size(),
				  ((t1-t0)/1000L), ((t2-t1)/1000L));
	    }
	}
	System.exit(0);
   }
}
