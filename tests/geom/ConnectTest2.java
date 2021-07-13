import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.Color;
import java.io.FileOutputStream;

public class ConnectTest2 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 5;

	Point3D[][] array = new Point3D[N][N];

	double r = 20.0;
	
	for (int i = 1; i < N-1; i++) {
	   for (int j = 1; j < N-1; j++) {

	       double thetaX = 2 * Math.PI * (i - 1) / (N-3);
	       double thetaY = 2 * Math.PI * (j - 1) / (N-3);

	       double x = r * (thetaX - Math.sin(thetaX));
	       double y = r * (thetaY - Math.sin(thetaY));
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       double z = r * (1 - Math.cos(thetaX)) * (1 - Math.cos(thetaY))
		   + 10.0;
	       array[i][j] = new Point3D.Double(x, y, z);
	   }
	}
	BezierGrid grid1 = new BezierGrid(array);
	grid1.print();

	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		return new Point3D.Double(p.getX(), p.getY(), 0.0);
	    });
	grid2.reverseOrientation(true);

	Path3DInfo.printSegments(grid1.getBoundary());


	System.out.println("creating connections ...");
	BezierGrid[] connections = grid1.createConnectionsTo(grid2, 2, split);
	System.out.println("... connections created");
	
	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	for (BezierGrid g: connections) {
	    surface.append(g);
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
	m3d.setTessellationLevel(5);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    System.exit(1);
	}

	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("connection2.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
}
