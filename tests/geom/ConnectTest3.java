import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.Color;
import java.io.FileOutputStream;

public class ConnectTest3 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 11;
	int M = 36;

	Point3D[][] array = new Point3D[N][M];

	double r = 50.0;

	for (int i = 0; i < N; i++) {
	    double z = (N-i)*10.0;
	    for (int j = 0; j < M; j++) {
		double theta = Math.toRadians(j*10);
	       double x = r * Math.cos(theta);
	       double y = r * Math.sin(theta);
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       array[i][j] = new Point3D.Double(x, y, z);
	   }
	}
	BezierGrid grid1 = new BezierGrid(array, false, true);
	grid1.remove(5, 5, 2, 2);

	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		return new Point3D.Double(0.8*p.getX(), 0.8*p.getY(),
					  p.getZ());
	    });

	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);
	System.out.println("creating connections ...");
	BezierGrid[] connections = grid1.createConnectionsTo(grid2, 2, split);
	System.out.println("... connections created");
	for (BezierGrid g: connections) {
	    surface.append(g);
	    g.print();
	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
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
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    System.exit(1);
	}

	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("connection3.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
}
