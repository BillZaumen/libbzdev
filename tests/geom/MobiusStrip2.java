import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;

public class MobiusStrip2 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));

	boolean span = true; 	// true for i = N-2, N-1, 0, 1

	int N = 64;
	int M = 4;
	// int N = 8;
	// int M = 4;

	Point3D[][] array = new Point3D[2*N][M];
	Point3D[] template = new Point3D[M];

	double r = 100.0;
	double width = 50.0;
	double height = 10.0;

	template[0] = new Point3D.Double(0.0, width/2, height/2);
	template[1] = new Point3D.Double(0.0, -width/2,height/2);
	template[2] = new Point3D.Double(0.0, -width/2,-height/2);
	template[3] = new Point3D.Double(0.0, width/2, -height/2);


	for (int i = 0; i < 2*N; i++) {
	    double theta = (Math.PI * i)/N;
	    double psi = (2 * Math.PI * i)/N;
	    AffineTransform3D af =
		AffineTransform3D.getRotateInstance(0.0, 0.0, psi);
	    af.translate(0.0, r, 0.0);
	    af.rotate(0.0, theta, 0.0);
	    for (int j = 0; j < M; j++) {
		array[i][j] = af.transform(template[j], null);
	    }
	}
	BezierGrid grid = new BezierGrid(array, true, true);
	for (int i = 0; i < 2*N; i++) {
	    for (int j = 0; j < M; j++) {
		grid.setRegion(i, j, j);
	    }
	}

	
	// We use a subgrid to remove duplicate points.
	BezierGrid mgrid = grid.subgrid(0, 0, N+1, M+1);

	if (span) {
	    Point3D p1 = mgrid.getPoint(N-2, 1);
	    Point3D p2 = mgrid.getPoint(N-2, 2);
	    System.out.format("N-2 (%s, %s, %s) ====== (%s, %s, %s)\n",
			      p1.getX(), p1.getY(), p1.getZ(),
			      p2.getX(), p2.getY(), p2.getZ());
	    p1 = mgrid.getPoint(N-1, 1);
	    p2 = mgrid.getPoint(N-1, 2);
	    System.out.format("N-1 (%s, %s, %s)        (%s, %s, %s)\n",
			      p1.getX(), p1.getY(), p1.getZ(),
			      p2.getX(), p2.getY(), p2.getZ());
	    p1 = mgrid.getPoint(0, M-1);
	    p2 = mgrid.getPoint(0, M);
	    System.out.format("0   (%s, %s, %s)        (%s, %s, %s)\n",
			      p1.getX(), p1.getY(), p1.getZ(),
			      p2.getX(), p2.getY(), p2.getZ());
	    p1 = mgrid.getPoint(1, M-1);
	    p2 = mgrid.getPoint(1, M);
	    System.out.format("1   (%s, %s, %s)        (%s, %s, %s)\n",
			      p1.getX(), p1.getY(), p1.getZ(),
			      p2.getX(), p2.getY(), p2.getZ());
	    p1 = mgrid.getPoint(2, M-1);
	    p2 = mgrid.getPoint(2, M);
	    System.out.format("2   (%s, %s, %s) ====== (%s, %s, %s)\n",
			      p1.getX(), p1.getY(), p1.getZ(),
			      p2.getX(), p2.getY(), p2.getZ());

	    mgrid.remove(N-2, 1);
	    mgrid.remove(N-1, 1);
	    mgrid.remove(0, M-1);
	    mgrid.remove(1, M-1);
	} else {
	    mgrid.remove(1, M-1);
	    mgrid.remove(2, M-1);
	    mgrid.remove(3, M-1);
	}
	System.out.println("mgrid boundary:");
	Path3DInfo.printSegments(mgrid.getBoundary());
	
	BezierGrid[] extensions = mgrid.createConnectionsTo(null, 2, false);
	BezierGrid support = extensions[0];
	for (int j = 0; j < support.getVArrayLength(); j++) {
	    Point3D p = support.getPoint(0, j);
	    if (p == null) {
		System.out.format("null value at (0, %d), arraylen = %d\n",
				  j, support.getVArrayLength());
	    }
	    support.setPoint(1,j,new Point3D.Double(p.getX(),
						    p.getY() + 20.0,
						    p.getZ()));
	}
	System.out.println("support boundary:");
	Path3DInfo.printSegments(support.getBoundary());

	Surface3D surface = new Surface3D.Double();
	surface.append(mgrid);
	surface.append(support);

	Path3D boundary = surface.getBoundary();
	Rectangle3D bounds = boundary.getBounds();
	double cx = bounds.getCenterX();
	double cy = bounds.getCenterY();
	double cz = bounds.getCenterZ();
	System.out.format("cx=%g, cy=%g, cz = %g\n", cx, cy, cz);


	double[] coords = new double[30];
	double[] pcoords = new double[12];
	double[] tcoords = null;
	PathIterator3D pit = boundary.getPathIterator(null);
	double x = 0.0, y = 0.0, z = 0.0;
	boolean segclose = false;
	int tcount = 0;
	while (!pit.isDone()) {
	    switch(pit.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		x = pcoords[0];
		y = pcoords[1];
		z = pcoords[2];
		System.out.format("... vertex (%g, %g, %g)\n", x, y, z);
		break;
	    case PathIterator3D.SEG_LINETO:
	    case PathIterator3D.SEG_QUADTO:
		throw new RuntimeException("path segment type "
					   + "not implemented");
	    case PathIterator3D.SEG_CUBICTO:
		Surface3D.setupU0ForTriangle(x, y, z, pcoords, coords,
					     false);
		tcoords = Path3D.setupCubic(x, y, z, cx, cy, cz);
		Surface3D.setupV0ForTriangle(tcoords, coords, false);
		x = pcoords[6];
		y = pcoords[7];
		z = pcoords[8];
		System.out.format("... vertex (%g, %g, %g)\n", x, y, z);
		tcoords = Path3D.setupCubic(cx, cy, cz, x, y, z);
		Surface3D.setupW0ForTriangle(tcoords, coords, false);
		Surface3D.setupCP111ForTriangle(coords);
		surface.addCubicTriangle(coords);
		tcount++;
		break;
	    case PathIterator3D.SEG_CLOSE:
		segclose = true;
		break;
	    }
	    if (segclose) break;
	    pit.next();
	}
	System.out.println("tcount = " + tcount);
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}

	if (!surface.isClosedManifold()) {
	    System.out.println("surface not a closed manifold");
	}

	 boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("no boundary because not well formed");
	    System.exit(1);
	} else if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("boundary:");
	    Path3DInfo.printSegments(boundary);
	    // System.exit(1);
	}

	if (surface.numberOfComponents() != 1) {
	    System.out.println("number of components for surface  = "
			       + surface.numberOfComponents());
	    System.exit(1);
	}
	/*
	SurfaceIterator sit = surface.getSurfaceIterator(null);
	double[] scoords = new double[48];

	while (!sit.isDone()) {
	    switch(sit.currentSegment(scoords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		System.out.format("(%g, %g, %g) - (%g, %g, %g) - "
				  + "(%g, %g, %g)\n",
				  scoords[0], scoords[1], scoords[2],
				  scoords[9], scoords[10], scoords[11],
				  scoords[27], scoords[28], scoords[29]);
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		System.out.println("unexpected planar triangle");
	    }
	    sit.next();
	}
	*/
	Model3D m3d = new Model3D();
	m3d.setStackTraceMode(true);
	m3d.append(surface);
        m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    // System.exit(1);
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("MobiusStrip2.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);

   }
}
