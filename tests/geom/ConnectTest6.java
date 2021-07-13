import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;


public class ConnectTest6 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 41;
	int NC = N/2;
	
	Point3D[][] array1 = new Point3D[N][N];

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
	    }
	}

	// Create the sphere
	BezierGrid grid1 = new BezierGrid(array1);

	grid1.remove(NC-3, NC-3, 6, 6);

	grid1.startSpline(NC-3, NC-3);
	grid1.moveU(6);
	grid1.moveV(6);
	grid1.moveU(-6);
	grid1.moveV(-6);
	grid1.endSpline(true);
	grid1.printSplines();

	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		if (z > 0.0) z = -z;
		return new Point3D.Double(x, y, z);
	    });


	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	System.out.println("creating connections ...");
	BezierGrid[] connections = grid1.createConnectionsTo(grid2, 2, split);
	System.out.println("... connections created");
	for (BezierGrid g: connections) {
	    surface.append(g);
	    System.out.format("connection width: %d, closed = %b\n",
			      g.getVArrayLength(), g.isVClosed());

	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	connections = grid1.createConnectionsTo(grid2, 2, split, 23, 20);
	System.out.println("number of connections for (23, 20) = "
			   + connections.length);

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
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    System.exit(1);
	}

	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("connection6.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
}
