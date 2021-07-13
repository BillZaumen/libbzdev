import java.awt.Color;
import java.io.FileOutputStream;
import java.io.File;
import java.util.List;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;
import org.bzdev.p3d.Model3DView;
import org.bzdev.anim2d.Animation2D;

public class MobiusStrip4 {
    public static void main(String argv[]) throws Exception {
	boolean makeImages = false;
	for (int i = 0; i < argv.length; i++) {
	    if (argv[i].equals("--isq")) {
		makeImages = true;
	    }
	}
	int N = 64;
	int M = 4;

	Point3D[][] array = new Point3D[2*N][M];
	Point3D[] template = new Point3D[M];

	double r = 25.0;
	double width = 25.0;
	double height = 3.0;

	Point3D[][] array1 = null;
	BezierGrid baseTemplate = null;
	BezierGrid baseGrid = null;
	BezierGrid baseWallGrid = null;
	BezierGrid supportGrid = null;
	BezierGrid supportBase = null;

	template[0] = new Point3D.Double(0.0, width/2, height/2);
	template[1] = new Point3D.Double(0.0, -width/2,height/2);
	template[2] = new Point3D.Double(0.0, -width/2,-height/2);
	template[3] = new Point3D.Double(0.0, width/2, -height/2);

	for (int i = 0; i < 2*N; i++) {
	    double thetaDeg = (360.0*i)/N;
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
	double zbase = grid.getBounds().getMinZ();

	// We use a subgrid to remove duplicate points.
	BezierGrid mgrid = grid.subgrid(0, 0, N+1, M+1);
	int n = 10;

	Surface3D surface = new Surface3D.Double();

	System.out.println("r+width/2 = " + (r+width/2));
	System.out.println("zbase = " + zbase);

	int imin = 19;
	int imax = 46;
	    
	baseTemplate = mgrid.subgrid(imin, 1, (imax - imin) + 2, 2);
	baseGrid = new BezierGrid(baseTemplate, false, (p) -> {
		return new Point3D.Double(p.getX(), p.getY(), zbase);
	    });

	baseTemplate.reverseOrientation(true);
	BezierGrid[] grids = baseTemplate.createConnectionsTo(baseGrid, 2);
	baseTemplate.reverseOrientation(false);
	baseWallGrid = grids[0];
	    
	int[][] pairs = {{N-2, 1}, {N-1, 1}, {0, M-1}, {1, M-1}};
	for (int[] pair: pairs) {
	    mgrid.remove(pair[0], pair[1]);
	}

	/*
	  mgrid.remove(N-2, 1);
	  mgrid.remove(N-1, 1);
	  mgrid.remove(0, M-1);
	  mgrid.remove(1, M-1);
	*/

	AffineTransform3D af = AffineTransform3D.getRotateInstance
	    (0.0, -Math.toRadians(90.0), 0.0, 0.0, r+width/2, zbase);
	supportBase = new BezierGrid(pairs.length+1, false, 2, false);
	int ib = 0;
	double[] coords1 = new double[48];
	double[] coords2 = new double[48];
	for (int[] pair: pairs) {
	    mgrid.getPatch(pair[0], pair[1], coords1);
	    af.transform(coords1, 0, coords2, 0, 16);
	    for (int k = 2; k < coords2.length; k += 3) {
		coords2[k] = zbase;
	    }
	    supportBase.setPatch(ib++, 0, coords2);
	}
	supportBase.print();

	surface.append(supportBase);
	surface.isWellFormed(System.out);

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}

	if (!surface.isClosedManifold()) {
	    System.out.println("surface not a closed manifold");
	    // System.exit(1);
	}

	Path3D boundary = surface.getBoundary();
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
	    // System.exit(1);
	}

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable (expected)");
	}  else {
	    System.out.println("volume = " + (m3d.volume()/1000) + "cm\u00b3");
	    System.out.println("area = " + (m3d.area()/100) + "cm\u00b2");
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("MobiusStrip4.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
    }
