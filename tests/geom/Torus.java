import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.*;

public class Torus {
    public static void main(String argv[]) throws Exception {
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

	BezierGrid grid = new BezierGrid(array, true, true);

	System.out.println("grid bounding box: " + grid.getBounds());


	if (!grid.isWellFormed(System.out)) {
	    System.exit(1);
	}

	if(grid.badSplines(System.out)) {
	    System.exit(1);
	}
	SurfaceIterator sit = grid.getSurfaceIterator(null);
	double[] coords = new double[48];

	Path3D bpath = grid.getBoundary();
	if (!bpath.isEmpty()) {
	    System.out.println("grid boundary:");
	    Path3DInfo.printSegments(bpath);
	}
	
	
	Surface3D surface = new Surface3D.Double();
	surface.append(grid);
	if (!surface.isWellFormed(System.out)) {
	    System.exit(1);
	}

	double area = surface.area();
	double volume = surface.volume();
	double tarea = 4*Math.PI*Math.PI*r1*r2;
	double tvolume = 2*Math.PI*Math.PI*r1*r2*r2;
	System.out.format("area = %s, expecting %s\n", area, tarea);
	System.out.format("volume = %s, expecting %s\n", volume, tvolume);

	Model3D m3d = new Model3D();
	m3d.setTessellationLevel(2);
	m3d.append(surface);

	Model3D.Image image = new
	    Model3D.Image(500, 400, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, 500, 400);
	g2d.dispose();
	image.setCoordRotation(0.0, Math.toRadians(30.0), 0.0);
	image.setColorFactor(0.5);
	image.setNormalFactor(0.5);
	m3d.setImageParameters(image, 50.0);
	m3d.render(image);
	image.write("png", "torus.png");

	System.exit(0);
   }
}
