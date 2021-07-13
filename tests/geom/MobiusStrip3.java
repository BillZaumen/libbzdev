import java.io.FileOutputStream;
import java.util.List;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.p3d.Model3D;

public class MobiusStrip3 {
    public static void main(String argv[]) throws Exception {
	boolean makeImages = false;
	boolean supports = true;
	for (int i = 0; i < argv.length; i++) {
	    if (argv[i].equals("--isq")) {
		makeImages = true;
	    }
	}

	int N = 64;
	int M = 4;
	// int N = 8;
	// int M = 4;

	Point3D[][] array = new Point3D[2*N][M];
	Point3D[] template = new Point3D[M];

	double r = 25.0;
	double width = 25.0;
	double height = 3.0;

	Point3D[][] array1 = null;
	BezierGrid baseWallGrid = null;
	BezierGrid baseTemplateOrig = null;
	BezierGrid baseTemplate = null;
	BezierGrid baseGrid = null;

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
	System.out.println("r+width/2 = " + (r+width/2));
	System.out.println("zbase = " + zbase);

	
	int imin = 19;
	int imax = 46;
	double[] coords = new double[48];
	baseTemplate = mgrid.subgrid(imin, 1, (imax - imin) + 2, 2);

	for (int i = imin; i <= imax; i++) {
	    mgrid.remove(i, 1);
	}

	baseTemplate.print();

	baseGrid = new BezierGrid(baseTemplate, false, (p) -> {
		return new Point3D.Double(p.getX(), p.getY(), zbase);
	    });
	System.out.println();
	System.out.println("--- creating baseWallGrid ---");
	System.out.println();

	baseTemplate.reverseOrientation(true);
	BezierGrid[] grids = baseTemplate.createConnectionsTo(baseGrid, 2);
	baseTemplate.reverseOrientation(false);
	if (grids.length != 1) {
	    System.out.println("expecting one grid");
	    System.exit(1);
	}
	baseWallGrid = grids[0];


	System.out.println("------");
	System.out.format("baseWallGrid (dimensions %d, %d)\n",
			  baseWallGrid.getUArrayLength(),
			  baseWallGrid.getVArrayLength());

	Path3D bwb = baseWallGrid.getBoundary();
	if (bwb == null) {
	    System.out.println("null boundary for baseWallGrid");
	} else if (bwb.isEmpty()) {
	    System.out.println("empty boundary for baseWallGrid");
	} else {
	    Path3DInfo.printSegments(bwb);
	}

	System.out.format("baseGrid (dimensions %d, %d)\n",
			  baseGrid.getUArrayLength(),
			  baseGrid.getVArrayLength());
	Path3D bb = baseGrid.getBoundary();
	if (bb == null) {
	    System.out.println("null boundary for baseGrid");
	} else if (bb.isEmpty()) {
	    System.out.println("empty boundary for baseGrid");
	} else {
	    Path3DInfo.printSegments(bb);
	}

	System.out.println("----- now create surface -------");
	Surface3D surface = new Surface3D.Double();
	if (false) {
	    surface.append(mgrid);
	    surface.append(baseTemplateOrig);
	} else {
	    surface.append(mgrid);
	     surface.append(baseWallGrid);
	    surface.append(baseGrid);
	    surface.isWellFormed(System.out);
	}

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	}  else {
	    System.out.println("volume = " + (m3d.volume()/1000) + "cm\u00b3");
	    System.out.println("area = " + (m3d.area()/100) + "cm\u00b2");
	    m3d.writeSTL("Mobius Strip: units in mm", "MobiusStrip.stl");
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("MobiusStrip3.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);

   }
}
