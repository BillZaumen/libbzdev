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

public class MobiusStrip5 {
    public static void main(String argv[]) throws Exception {
	boolean makeImages = false;
	boolean makeAnimation = false;
	boolean supports = false;
	double scale = 1.0;
	for (int i = 0; i < argv.length; i++) {
	    if (argv[i].equals("--isq")) {
		makeImages = true;
	    } else if (argv[i].equals("--supports")) {
		supports = true;
	    } else if (argv[i].equals("--animation")) {
		makeAnimation = true;
	    } else if (argv[i].equals("--scale")) {
		if (i < argv.length - 1) {
		    i++;
		    scale = Double.parseDouble(argv[i]);
		}
	    }
	}
	int N = 64;
	int M = 4;

	Point3D[][] array = new Point3D[2*N][M];
	Point3D[] template = new Point3D[M];

	double r = 25.0;
	double width = 25.0;
	double height = 3.0;

	if (scale != 1.0) {
	    r *= scale;
	    width *= scale;
	    height *= scale;
	}

	Point3D[][] array1 = null;
	BezierGrid baseTemplate = null;
	BezierGrid baseGrid = null;
	BezierGrid baseWallGrid = null;
	BezierGrid supportGrid = null;
	BezierCap supportBase = null;

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

	if (supports) {
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

	    for (int i = imin; i <= imax; i++) {
		mgrid.remove(i, 1);
	    }

	    supportGrid  = mgrid.createConnectionsTo(null, n, false, 0, M-1)[0];

	    Path3D sgb = mgrid.getBoundary(0, M-1);
	    Path3DInfo.printSegments(sgb);

	    for (int i = 1; i < n; i++) {
		double theta = -Math.toRadians(i*90.0/(n-1));
		AffineTransform3D af =
		    AffineTransform3D.getRotateInstance(0.0, theta, 0.0,
							0.0, r+width/2, zbase);
		for (int j = 0; j < supportGrid.getVArrayLength(); j++) {
		    Point3D p = supportGrid.getPoint(0, j);
		    Point3D newp = af.transform(p, null);
		    if (i == (n-1)) {
			newp.setLocation(newp.getX(), newp.getY(), zbase);
		    }
		    supportGrid.setPoint(i,j, newp);
		}
	    }

	    supportGrid.print();
	}
	surface.append(mgrid);

	if (supports) {
	    surface.append(supportGrid);
	    surface.append(baseWallGrid);
	    surface.append(baseGrid);
	    surface.isWellFormed(System.out);
	    Path3D boundary = surface.getBoundary();
	    if (boundary != null) {
		supportBase = new BezierCap(boundary, 0.0, false);
		surface.append(supportBase);
	    }
	}

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}

	if (!surface.isClosedManifold()) {
	    System.out.println("surface not a closed manifold");
	    System.exit(1);
	}
	Rectangle3D bnds = surface.getBounds();
	System.out.println("model width = " + (bnds.getWidth()/10) + "cm");
	System.out.println("model height = " + (bnds.getHeight()/10) + "cm");
	System.out.println("model depth = " + (bnds.getDepth()/10) + "cm");

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	}  else {
	    System.out.println("volume = " + (m3d.volume()/1000) + "cm\u00b3");
	    System.out.println("area = " + (m3d.area()/100) + "cm\u00b2");
	    m3d.writeSTL("Mobius Strip: units in mm",
			 ((scale == 1.0)? "MobiusStrip.stl":
			  "MobiusStrip-" + scale + "X.stl"));
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("MobiusStrip5.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
   }
}
