import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;


public class ConnectTest8 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	// can be set to false for debugging (to get a simpler case
	// without all the holes)
	boolean hasHoles = true;
	
	int N = 41;
	int NC = N/2;
	
	Point3D[][] array1 = new Point3D[N][N];

	double r = 100.0;
	double r1 = 90.0;

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
	    }
	}

	// Create the sphere
	BezierGrid grid1 = new BezierGrid(array1);

	if (hasHoles) {
	    //  add a bunch of holes in it.
	    for (int i = 1; i < N-2 ; i += 2) {
		for (int j = 1; j < N-2; j += 2) {
		    grid1.remove(i,j);
		}
	    }
	}

	for (int k = 2; k <= NC; k++) {
	    int delta = 2*k;
	    grid1.startSpline(NC-k, NC-k);
	    grid1.moveU(delta);
	    grid1.moveV(delta);
	    grid1.moveU(-delta);
	    grid1.moveV(-delta);
	    grid1.endSpline(true);
	}
	
	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		double scale = r1/r;
		x *= scale;
		y *= scale;
		z *= scale;
		return new Point3D.Double(x, y, z);
	    });

	BezierGrid grid3 = new BezierGrid(grid1, true, (p) -> {
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		if (z > 0.0) z = -z;
		return new Point3D.Double(x, y, z);
	    });

	BezierGrid grid4 = new BezierGrid(grid2, true, (p) -> {
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		if (z > 0.0) z = -z;
		return new Point3D.Double(x, y, z);
	    });

	Surface3D surface = new Surface3D.Double();
	surface.setStackTraceMode(true);
	surface.append(grid1);
	if (hasHoles) surface.append(grid2);
	surface.append(grid3);
	if (hasHoles)  surface.append(grid4);

	if (!hasHoles) {
	    // System.out.println("grid1 boundary:");
	    // Path3DInfo.printSegments(grid1.getBoundary());
	    // System.out.println("grid3 boundary:");
	    // Path3DInfo.printSegments(grid3.getBoundary());
	    
	    int ii, jj;
	    Point3D pt;
	    ii = N-1;
	    jj = 0;
	    pt = grid1.getPoint(ii,jj); 
	    System.out.format("grid1.getPoint(%d,%d) = (%s, %s, %s)\n",
			      ii, jj,
			      pt.getX(), pt.getY(), pt.getZ());
	    pt = grid3.getPoint(ii,jj); 
	    System.out.format("grid3.getPoint(%d,%d) = (%s, %s, %s)\n",
			      ii, jj,
			      pt.getX(), pt.getY(), pt.getZ());
			      
	    double[] coords = new double[48];
	    if (grid1.getPatch(ii-1, jj, coords)) {
		System.out.println("grid1 patch:");
		for (int i = 0; i < 48; i++) {
		    System.out.print(" " + coords[i] + ",");
		    if (i % 3 == 2) System.out.println();
		}
	    } else {
		System.out.println("getPatch failed for grid1");
	    }
	    if (grid3.getPatch(ii-1, jj, coords)) {
		System.out.println("grid3 patch:");
		for (int i = 0; i < 48; i++) {
		    System.out.print(" " + coords[i] + ",");
		    if (i % 3 == 2) System.out.println();
		}
	    } else {
		System.out.println("getPatch failed for grid1");
	    }
	}
	/*
	Entry 102:
	type: SEG_CUBICTO
	startingX: 70.71067811865476
	startingY: -70.71067811865474
	startingZ: 0.0
	coords[0]: 69.78507751876967
	coords[1]: -71.63627871853981
	coords[2]: 0.0
	coords[3]: 68.84130045767918
	coords[4]: -72.54370285721953
	coords[5]: 0.0
	coords[6]: 67.88007455329418
	coords[7]: -73.43225094356856
	coords[8]: 0.0
	*/



	if (hasHoles) {
	    System.out.println("creating connections ...");
	    BezierGrid[] connections1 =
		grid1.createConnectionsTo(grid2, 2, false, true, 0, 0);
	    int count = 0;
	    System.out.println("... connections1");
	    for (BezierGrid g: connections1) {
		for (int i = 0; i < 2; i++) {
		    for (int j = 0; j < g.getVArrayLength(); j++) {
			Point3D p = g.getPoint(i,j);
			if (p.getZ() == 0.0) {
			    System.out.format("z is 0 for count=%d, "
					      + "i=%d, j=%d\n",
					      count, i, j);
			}
		    }
		}
		count++;
	    }

	    BezierGrid[] connections2 =
		grid3.createConnectionsTo(grid4, 2, false, true, 0, 0);
	    System.out.println("... connections2");
	    count = 0;
	    for (BezierGrid g: connections1) {
		for (int i = 0; i < 2; i++) {
		    for (int j = 0; j < g.getVArrayLength(); j++) {
			Point3D p = g.getPoint(i,j);
			if (p.getZ() == 0.0) {
			    System.out.format("z is 0 for count=%d, "
					      + "i=%d, j=%d\n",
					      count, i, j);
			}
		    }
		}
		count++;
	    }

	    System.out.println("... adding connections1");
	    for (BezierGrid g: connections1) {
		surface.append(g);
	    }
	    System.out.println("... adding connections2");
	    for (BezierGrid g: connections2) {
		surface.append(g);
	    }
	    System.out.println("... connections created");
	}

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	if (!surface.isClosedManifold()) {
	    System.out.println("surface not a closed manifold");
	    System.exit(1);
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
	    System.exit(1);
	}

	if (surface.numberOfComponents() != 1) {
	    System.out.println("number of components for surface  = "
			       + surface.numberOfComponents());
	    System.exit(1);
	}

	Model3D m3d = new Model3D();
	m3d.setStackTraceMode(true);
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    System.exit(1);
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("connection8.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
}
