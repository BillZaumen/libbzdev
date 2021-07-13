import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;


public class ExtensionTest1 {
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

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);

	System.out.println("creating extension ...");

	BezierGrid extension =
	    grid1.createExtensionGrid((index, point, type, ends) -> {
		    double xxx = (index == 3)?
		    (r1+r2 + 3*10): (point.getX() + index*10);
		    return new Point3D.Double(xxx,
					      point.getY(),
					      point.getZ());
		}, null, 4, 0, M-2);
	
	System.out.println("... extension created");
	surface.append(extension);
	System.out.format("extension V axis: %d, closed = %b\n",
			  extension.getVArrayLength(), extension.isVClosed());
	System.out.format("extension U axis: %d, closed = %b\n",
			  extension.getUArrayLength(), extension.isUClosed());

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	Path3D boundary = surface.getBoundary();
	Path3DInfo.printSegments(boundary);

	Point3D center =  BezierCap.findCenter(boundary);
	double[] vcoords = BezierCap.findVector(boundary, center);
	System.out.format("center: (%g, %g, %g), vector: (%g, %g, %g)\n",
			  center.getX(), center.getY(), center.getZ(),
			  vcoords[0], vcoords[1], vcoords[2]);
	Rectangle3D sbox = surface.getBounds();
	System.out.println("sbox.getMaxX() = " + sbox.getMaxX());

	BezierCap cap = new BezierCap(boundary, 0.0, true);

	surface.append(cap);
	

	boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("no boundary because not well formed");
	    // System.exit(1);
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
	    System.out.println("m3d is not printable");
	    // System.exit(1);
	}

	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("extension1.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
   }
}
