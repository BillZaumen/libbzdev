import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.Arrays;
import org.bzdev.geom.*;
import org.bzdev.p3d.*;

public class BezierBox2 {

    static public void main(String argv[]) throws Exception {
	Point3D array[][] = new Point3D[6][4];

	double r = 100.0;

	for (int i = 0; i <  6; i++) {
	    double theta = Math.toRadians(i*60.0);
	    double x = r * Math.cos(theta);
	    double y = r * Math.sin(theta);
	    array[i][0] = new Point3D.Double(x, y, 0.0);
	    array[i][1] = new Point3D.Double(x, y, 25.0);
	    array[i][2] = new Point3D.Double(x, y, 75.0);
	    array[i][3] = new Point3D.Double(x, y, 100.0);
	}

	BezierGrid bg = new BezierGrid(array, true, false, false);
	int region = 0;
	// what happens when the 'linear' argument is true, but with
	// the grid still treated as cubic Bezier curves
	for (int i = 0; i < 6; i++) {
	    for (int j = 0; j < 4; j++) {
		bg.setRegion(i, j, region++);
		if (bg.getRegion(i,j) != region-1) {
		    throw new Exception("getRegion failed");
		}
	    }
	}

	bg.remove(4, 1);

	Path3D b1 = bg.getBoundary(0,0);
	Path3D b2 = bg.getBoundary(0, 3);
	Path3D b3 = bg.getBoundary(4, 1);
	BezierVertex bv1 = new BezierVertex(b1, 0.0);
	bv1.reverseOrientation(true);
	BezierVertex bv2 = new BezierVertex(b2, 0.0);
	bv2.reverseOrientation(true);
	BezierCap bc3 = new BezierCap(b3, 10.0, true);
	// BezierVertex bc3 = new BezierVertex(b3, 10.0);
	// bc3.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(bg);
	surface.append(bv1);
	surface.append(bv2);
	surface.append(bc3);

	Model3D m3d = new Model3D();
	m3d.append(surface);

	m3d.setTessellationLevel(3);


	Model3D.Image image = new
	    Model3D.Image(500, 400, BufferedImage.TYPE_INT_ARGB);
	// image.setBacksideColor(Color.RED);

	image.setEdgeColor(Color.GREEN);
	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, 500, 400);
	g2d.dispose();
	image.setCoordRotation(Math.toRadians(20.0),
			       Math.toRadians(60.0),
			       0.0);
	// image.setColorFactor(0.5);
	// image.setNormalFactor(0.5);
	m3d.setImageParameters(image, 50.0);
	m3d.render(image);
	image.write("png", "hexbox2.png");
	System.exit(0);
    }
}
