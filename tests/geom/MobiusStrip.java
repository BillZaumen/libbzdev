import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;
import java.util.List;

public class MobiusStrip {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));

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

	Surface3D surface = new Surface3D.Double();
	surface.append(mgrid);
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
	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d is not printable");
	    List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	    for (Model3D.Triangle triangle: tlist) {
		System.out.format("(%s,%s,%s)-(%s,%s,%s)-(%s,%s,%s)\n",
				  triangle.getX1(), triangle.getY1(),
				  triangle.getZ1(),
				  triangle.getX2(), triangle.getY2(),
				  triangle.getZ2(),
				  triangle.getX3(), triangle.getY3(),
				  triangle.getZ3());
	    }
	    Model3D tm3d = new Model3D();
	    tm3d.addTriangle(7.751552581787109,
			     -105.14988708496094, -12.309081077575684,
			     10.351327896118164,
			     -105.09880065917969, -12.239604949951172,
			     7.724030017852783, -104.71210479736328,
			     0.18403615057468414);
	    tm3d.addTriangle(7.724030017852783, -104.71210479736328,
			     0.18403615057468414,
			     7.696506977081299, -104.27432250976562,
			     12.677153587341309,
			     5.1425957679748535,-104.56514739990234,
			     12.620352745056152);
	    tlist = tm3d.verifyEmbedded2DManifold();
	    if (tlist != null) {
		System.out.println("tm3d not embedded?");
	    }

	    tm3d.createImageSequence(new FileOutputStream
					("MobiusStrip-test.isq"),
					 "png", 8, 6, 0.0, 0.0, 0.0, true);
	    System.exit(0);
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("MobiusStrip.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);

   }
}
