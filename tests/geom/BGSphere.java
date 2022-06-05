import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctThreeOps;
import org.bzdev.math.Adder;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;


public class BGSphere {

    static int tot;
    static Adder adder1;
    static Adder adder2;
    static Adder adder3;
    static double testNearlyFlat(double limit, SurfaceIterator si) {
	double[] coords = new double[48];
	int stype;
	tot = 0;
	int count = 0;
	while (!si.isDone()) {
	    stype = si.currentSegment(coords);
	    if (nearlyFlat(limit, stype, coords)) {
		count++;
	    }
	    tot++;
	    si.next();
	}
	return ((double)(count))/tot ;
    }

    static final double oneThird = 1.0/3.0;

    static double[] coords1 = new double[3];
    static double[] coords2 = new double[3];
    static double[] coords3 = new double[3];
    static double[] coords4 = new double[3];

    static boolean nearlyFlat(double limit, int stype, double[] coords)
    {
	// tests not compete but first check timing
	switch(stype) {
	case SurfaceIterator.PLANAR_TRIANGLE:
	    return true;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    {
		// left edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 0, 3);
		double norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 3, coords, 0, 3);
		double inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		double dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;
		VectorOps.sub(coords2, 0, coords, 9, coords, 15, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// CP111
		VectorOps.sub(coords2, 0, coords, 27, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.sub(coords3, 0, coords, 15, coords, 0, 3);
		VectorOps.crossProduct(coords4, coords1, coords2);
		VectorOps.normalize(coords4);
		dp = VectorOps.dotProduct(coords3, coords4);
		VectorOps.multiply(coords4, dp, coords4);
		VectorOps.sub(coords2, coords3, coords4);
		if (Math.abs(dp/VectorOps.norm(coords2)) > limit) return false;
		VectorOps.add(coords4, 0, coords, 0, coords, 9, 3);
		VectorOps.add(coords4, 0, coords4, 0, coords, 27, 3);
		VectorOps.sub(coords4, 0, coords4, 0, coords, 15, 3);
		norm = VectorOps.norm(coords4);
		double circum = VectorOps.norm(coords1)
		    + VectorOps.norm(coords2);
		VectorOps.sub(coords2, 0, coords, 9, coords, 27, 3);
		circum += VectorOps.norm(coords2);
		if (Math.abs(3*norm/circum) > limit) return false;

		// left edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge center
		VectorOps.sub(coords2, 0, coords, 6, coords, 3, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// bottom edge start
		VectorOps.sub(coords1, 0, coords, 27, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge end
		VectorOps.sub(coords2, 0, coords, 27, coords, 21, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;


		// bottom edge center
		VectorOps.sub(coords2, 0, coords, 21, coords, 12, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// right edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 27, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 24, coords, 27, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 18, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 18, coords, 24, 3);
		norm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		return true;
	    }
	case SurfaceIterator.CUBIC_PATCH:
	    {
		// left edge start
		VectorOps.sub(coords1, 0, coords, 36, coords, 0, 3);
		double norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12, coords, 0, 3);
		double inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		double dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge end
		VectorOps.sub(coords2, 0, coords, 36, coords, 24, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left edge center
		VectorOps.sub(coords2, 0, coords, 24, coords, 12, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// right edge start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 9, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12+9, coords, 9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 24+9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 24+9, coords, 12+9, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// bottom edge start
		VectorOps.sub(coords1, 0, coords, 9, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 3, coords, 0, 3);
		dp = VectorOps.dotProduct(coords1, coords2);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge end
		VectorOps.sub(coords2, 0, coords, 9, coords, 6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// bottom edge center
		VectorOps.sub(coords2, 0, coords, 6, coords, 3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// top edge start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 36, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 36+3, coords, 36, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// top edge end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 36+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// right edge center
		VectorOps.sub(coords2, 0, coords, 36+6, coords, 36+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;


		// left bottom to top diagnonal start
		VectorOps.sub(coords1, 0, coords, 36+9, coords, 0, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 12+3, coords, 0, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left bottom to top diagnonal end
		VectorOps.sub(coords2, 0, coords, 36+9, coords, 24+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left bottom to top diagnonal center
		VectorOps.sub(coords2, 0, coords, 24+6, coords, 12+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		// left top to bottom diagnonal start
		VectorOps.sub(coords1, 0, coords, 9, coords, 36, 3);
		norm = VectorOps.norm(coords1);
		VectorOps.sub(coords2, 0, coords, 24+3, coords, 36, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left top to bottom diagnonal end
		VectorOps.sub(coords2, 0,  coords, 9, coords, 12+6, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;
		VectorOps.crossProduct(coords3, coords1, coords2);
		VectorOps.multiply(coords3, 0, 1/dp, coords3, 0, 3);
		if (VectorOps.norm(coords3) > limit) return false;

		// left top to bottom diagnonal center
		VectorOps.sub(coords2, 0, coords, 12+6, coords, 24+3, 3);
		inorm = VectorOps.norm(coords2);
		if (Math.abs(inorm/norm - oneThird) > limit) return false;
		dp = VectorOps.dotProduct(coords1, coords2);
		if (dp < 0.0) return false;

		return true;
	    }
	}
	return false;
    }


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

	/*
	System.out.println("array1:");
	for (int i = 0; i < N; i++) {
	    for (int j = 0; j < N; j++) {
		System.out.format(" (%g, %g, %g)",
				  array1[i][j].getX(),
				  array1[i][j].getY(),
				  array1[i][j].getZ());
	    }
	    System.out.println();
	}
	*/

	// Create the sphere
	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);

	Point3D pt = grid1.getPoint(10,10);
	grid1.setPoint(10,10,pt);

	double[] ptcoords = new double[9];
	boolean flag = grid1.getSplineU(10, 10, ptcoords);
	if (flag == true) {
	    flag = grid1.setSplineU(10, 10, ptcoords);
	    if (flag != true) System.out.println("setSplineU failed");
	} else {
	    System.out.println("getSplineU failed");
	}
	flag = grid1.getSplineV(10, 10, ptcoords);
	if (flag == true) {
	    flag = grid1.setSplineV(10, 10, ptcoords);
	    if (flag != true) System.out.println("setSplineV failed");
	} else {
	    System.out.println("getSplineV failed");
	}

	if(grid1.badSplines(System.out)) {
	    System.exit(1);
	}

	if(grid2.badSplines(System.out)) {
	    System.exit(1);
	}

	grid2.reverseOrientation(true);

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

	long t0 = System.nanoTime();
	double vol = surface.volume();
	double t1 = System.nanoTime();
	System.out.println("volume = " + vol + ", expecting "
			   + (r*r*r*4*Math.PI/3));

	System.out.println("volume() took " + ((t1 - t0)/1000)
			   + " microseconds");


	t0 = System.nanoTime();
	double ratio = testNearlyFlat(0.1, surface.getSurfaceIterator(null));
	t1 = System.nanoTime();
	System.out.println("nearlyFlat ratio = " + ratio
			   + " using " + tot + " patches,\n"
			   + " running time = " +((t1 - t0)/1000)
			   + " microseconds");

	t0 = System.nanoTime();
	Point3D cm = SurfaceOps.centerOfMassOf(surface, vol);
	t1 = System.nanoTime();
	System.out.println("cm = " + cm);
	System.out.println("centerOfMassOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");


	t0 = System.nanoTime();
	double[][] moments = SurfaceOps.momentsOf(surface, cm, vol);
	t1 = System.nanoTime();
	System.out.println("moments:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");

	double[][] I = SurfaceOps.toMomentsOfInertia(moments);
	System.out.println("I:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", I[i][j]);
	    }
	    System.out.println(" |");
	}

	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, vol, 0.1);
	t1 = System.nanoTime();
	System.out.println("fast cm = " + cm);
	System.out.println("fast centerOfMassOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");

	t0 = System.nanoTime();
	moments = SurfaceOps.momentsOf(surface, cm, vol, 0.5);
	t1 = System.nanoTime();
	System.out.println("fast moments:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("fast momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");

	double[] vector = new double[3];
	BasicStats stats = new BasicStats.Population();

	SurfaceIterator pit = surface.getSurfaceIterator(null);
	double[] coords = new double[48];
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
	System.out.println("available processors = "
			   + Runtime.getRuntime().availableProcessors());

	t0 = System.nanoTime();
	for (int i = 0; i < 1000; i++) {
	    Thread t = new Thread(() -> {return;});
	    t.start();
	    t.join();
	}
	t1 = System.nanoTime();
	System.out.println("min time for thread = "
			   + ((t1 - t0)/1000000L) + " microseconds");

	pit = surface.getSurfaceIterator(null);
	RealValuedFunctThreeOps tf = (xarg, yarg, zarg) -> {return xarg*yarg;};
	double sum = 0;
	int index = 0;;
	t0 = System.nanoTime();
	while (!pit.isDone()) {
	    sum += tf.valueAt((double)index++, index, index);
	    pit.next();
	}
	t1 = System.nanoTime();
	System.out.println("lambda-expression time for a surface integral: "
			   + (t1- t0)/1000L + " microseconds");


	AffineTransform3D af =
	    AffineTransform3D.getTranslateInstance(100.0, 200.0, 300.0);

	Surface3D tsurface = new Surface3D.Double(surface, af);
	cm = SurfaceOps.centerOfMassOf(tsurface);
	System.out.println("tsurface cm = " + cm
			   + ", expecting (100, 200, 300)\n");

	af = AffineTransform3D.getScaleInstance(2.0, 1.0, 1.0);
	Surface3D scaled = new Surface3D.Double(surface, af);
	cm = SurfaceOps.centerOfMassOf(scaled);
	System.out.println("scaled cm = " + cm
			   + ", expecting (0, 0, 0)\n");

	moments = SurfaceOps.momentsOf(scaled, cm);
	System.out.println("moments when stretched along X axis");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}

	af = AffineTransform3D.getScaleInstance(1.0, 2.0, 1.0);
	scaled = new Surface3D.Double(surface, af);
	cm = SurfaceOps.centerOfMassOf(scaled);
	moments = SurfaceOps.momentsOf(scaled, cm);
	System.out.println("moments when stretched along Y axis");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}

	af = AffineTransform3D.getScaleInstance(1.0, 1.0, 2.0);
	scaled = new Surface3D.Double(surface, af);
	cm = SurfaceOps.centerOfMassOf(scaled);
	moments = SurfaceOps.momentsOf(scaled, cm);
	System.out.println("moments when stretched along Z axis");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("... expecting one diagonal element to be "
			   + "4 times the others, and");
	System.out.println("    off diagonal elements to be approximately 0");

	Point3D p = new Point3D.Double(-100.0, -100.0, 0);
	moments = SurfaceOps.momentsOf(surface, p);
	System.out.println("moments when p set to (-100, -100, 0)");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("moments[0][1] should be about 10000");
	p = new Point3D.Double(-100.0, 00.0, -100.0);
	moments = SurfaceOps.momentsOf(surface, p);
	System.out.println("moments when p set to (-100, 0, -100)");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("moments[0][2] should be about 10000");
	p = new Point3D.Double(0.0, -100.0, -100.0);
	moments = SurfaceOps.momentsOf(surface, p);
	System.out.println("moments when p set to (0, -100, -100)");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("moments[1][2] should be about 10000");

	// check SurfaceIteratorSplitter

	System.out.println("splitter test");

	t0 = System.nanoTime();
	Adder adder = new Adder.Kahan();
	pit = surface.getSurfaceIterator(null);
	Surface3D.addAreaToAdder(adder, pit);
	t1 = System.nanoTime();
	System.out.println("vol = " + adder.getSum()
			   + ", no-splitter timing = "
			   + (t1- t0)/1000L + " microseconds");


	t0 = System.nanoTime();
	final SurfaceIteratorSplitter splitter =
	    new SurfaceIteratorSplitter(3, surface.getSurfaceIterator(null));



	adder1 = new Adder.Kahan();
	adder2 = new Adder.Kahan();
	adder3 = new Adder.Kahan();
	Thread thread1 = new Thread(() -> {
		SurfaceIterator sit1 = splitter.getSurfaceIterator(0);
		Surface3D.addAreaToAdder(adder1, sit1);

	});

	Thread thread2 = new Thread(() -> {
		SurfaceIterator sit2 = splitter.getSurfaceIterator(1);
		Surface3D.addAreaToAdder(adder2, sit2);
	});
	thread1.start();
	thread2.start();
	Surface3D.addAreaToAdder(adder3, splitter.getSurfaceIterator(2));
	thread1.join();
	thread2.join();
	t1 = System.nanoTime();
	System.out.println("vol1 = " + adder1.getSum() +
			   ", vol2 = " + adder2.getSum()
			   +", vol3 = " + adder3.getSum());
	System.out.println("total = " + (adder1.getSum() + adder2.getSum()
					 + adder3.getSum()));
	System.out.println("splitter timing = "
			   + (t1- t0)/1000L + " microseconds");

	System.out.println("parallel operation tests");
	t0 = System.nanoTime();
	double area = surface.area();
	t1 = System.nanoTime();
	System.out.println("sequential: area = " + area
			   + ", " + ((t1-t0)/1000L) + " microseconds");
	t0 = System.nanoTime();
	area = surface.area(true);
	t1 = System.nanoTime();
	System.out.println("parallel: area = " + area
			   + ", " + ((t1-t0)/1000L) + " microseconds");

	t0 = System.nanoTime();
	double volume = surface.volume();
	t1 = System.nanoTime();
	System.out.println("sequential: volume = " + volume
			   + ", " + ((t1-t0)/1000L) + " microseconds");
	t0 = System.nanoTime();
	volume = surface.volume(true);
	t1 = System.nanoTime();
	System.out.println("parallel: volume = " + volume
			   + ", " + ((t1-t0)/1000L) + " microseconds");

	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, false, surface.size());
	t1 = System.nanoTime();
	System.out.println("sequential: cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");
	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, true, surface.size());
	t1 = System.nanoTime();
	System.out.println("parallel: cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");

	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, volume, false,
				      surface.size());
	t1 = System.nanoTime();
	System.out.println("sequential (with volume): cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");
	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, volume, true, surface.size());
	t1 = System.nanoTime();
	System.out.println("parallel (with volume): cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");

	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, volume, 0.5,
				      false, surface.size());
	t1 = System.nanoTime();
	System.out.println("sequential (with volume & limit): cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");
	t0 = System.nanoTime();
	cm = SurfaceOps.centerOfMassOf(surface, volume, 0.5,
				      true, surface.size());
	t1 = System.nanoTime();
	System.out.println("parallel (with volume & limit): cm = " + cm
			   + ", " + ((t1-t0)/1000L) + " microseconds");

	System.out.println("parallel only:");
	cm = SurfaceOps.centerOfMassOf(tsurface, true, surface.size());
	System.out.println("tsurface cm = " + cm
			   + ", expecting (100, 200, 300)\n");
	cm = SurfaceOps.centerOfMassOf(tsurface, true, surface.size());
	System.out.println("tsurface cm = " + cm
			   + ", expecting (100, 200, 300)\n");
	cm = SurfaceOps.centerOfMassOf(tsurface, tsurface.volume(), true,
				      surface.size());
	System.out.println("tsurface cm = " + cm
			   + ", expecting (100, 200, 300)\n");
	cm = SurfaceOps.centerOfMassOf(tsurface, tsurface.volume(), 0.5, true,
				      surface.size());
	System.out.println("tsurface cm = " + cm
			   + ", expecting (100, 200, 300)\n");

	vol = surface.volume(true);
	cm = SurfaceOps.centerOfMassOf(surface, vol, true, surface.size());
	t0 = System.nanoTime();
	moments = SurfaceOps.momentsOf(surface, cm, vol, false,
				      surface.size());
	t1 = System.nanoTime();
	System.out.println("sequential moments:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");
	t0 = System.nanoTime();
	moments = SurfaceOps.momentsOf(surface, cm, vol, true, surface.size());
	t1 = System.nanoTime();
	System.out.println("parallel moments:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");

	t0 = System.nanoTime();
	moments = SurfaceOps.momentsOf(surface, cm, false, 0);
	t1 = System.nanoTime();
	System.out.println("sequential moments (no vol):");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");
	t0 = System.nanoTime();
	moments = SurfaceOps.momentsOf(surface, cm, true, surface.size());
	t1 = System.nanoTime();
	System.out.println("parallel moments (no vol):");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}
	System.out.println("expecting 2000 on diagonals, rest 0");
	System.out.println("momentsOf(...) took " + ((t1 - t0)/1000)
			   + " microseconds");
    }
}
