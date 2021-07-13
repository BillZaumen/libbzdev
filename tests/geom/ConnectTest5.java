import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;


public class ConnectTest5 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 64;
	int M = 36;
	// int N = 8;
	// int M = 4;

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
		AffineTransform3D.getRotateInstance(phi, 0.0, 0.0);
	    for (int j = 0; j < M; j++) {
		array[i][j] = af.transform(template[j], null);
	    }
	}

	BezierGrid grid1 = new BezierGrid(array, true, true);

	grid1.remove(N-2, M-2, 2, 2);
	grid1.remove(N-2, 0, 2, 2);
	grid1.remove(0, M-2, 2, 2);
	grid1.remove(0, 0, 2, 2);

	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		double x = p.getX();
		double y = p.getY();
		double z = p.getZ();
		double phi = Math.atan2(y, x);
		double x0 = r1*Math.cos(phi);
		double y0 = r1*Math.sin(phi);
		double z0 = 0.0;
		x = 0.8 * (x - x0) + x0;
		y = 0.8 * (y - y0) + y0;
		z = 0.8 * (z - z0) + z0;
		return new Point3D.Double(x, y, z);
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
	    System.out.format("connection width: %d, closed = %b\n",
			      g.getVArrayLength(), g.isVClosed());

	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	connections = grid1.createConnectionsTo(grid2, 2, split, N-2, M-2);
	System.out.println("number of connections for ("
			   + (N-2) +", " + (M-2) + ") = "
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
	    m3d.createImageSequence(new FileOutputStream("connection5.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
   }
}
