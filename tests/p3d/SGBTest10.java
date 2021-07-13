import org.bzdev.p3d.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.Graph;
import org.bzdev.anim2d.Animation2D;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;

public class SGBTest10 {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	// useful for debugging.
	m3d.setStackTraceMode(true);

	double z0 = 0.0;  // base
	double z1 = 10.0; // first level
	double z2 = 20.0; // second level

	double w = 30.0; // hole width
	double w1 = 4.0; // first level width
	double w2 = 8.0; // second level width
	double tw = w + 2*(w1+w2);
	
	double h = 60.0; // hole height;
	double h1 = 4.0; // first level height;
	double h2 = 8.0;  // second level height;
	double th = h + 2*(h1+h2);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, z1, z0);

	sgb.addRectangles(0.0, 0.0, w1+w2, th, 0.0, 0.0);
	sgb.addRectangles(w+w1+w2, 0.0, w1+w2, th, 0.0, 0.0);
	sgb.addRectangles(0.0, 0.0, tw, h1+h2, 0.0, 0.0);
	sgb.addRectangles(0.0, h+h1+h2, tw, h1+h2, 0.0, 0.0);

	sgb.addRectangles(w1, h1, w2, h+2*h2, z2-z1, 0.0);
	sgb.addRectangles(w+w1+w2, h1, w2, h+2*h2, z2-z1, 0.0);
	sgb.addRectangles(w1, h1, w+2*w2, h2, z2-z1, 0.0);
	sgb.addRectangles(w1, h+h1+h2, w+2*w2, h2, z2-z1, 0.0);

	sgb.create();

	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}
	// Now create  an STL file suitable for 3D printing
	m3d.writeSTL("Lockpart STL File", "lockpart2.stl");

	// Also create a picture of the object.

	int WIDTH = 700;
	int HEIGHT = 700;
	FileOutputStream os = new FileOutputStream(new File("sgbtest10.png"));
	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(os, WIDTH, HEIGHT, "png");

	// Model3D.Image image = new 
	//    Model3D.Image (WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	Model3D.Image image = new Model3D.Image(osg);
	
	// We want to show an outline of the triangles, so set the
	// edge color.

	image.setEdgeColor(Color.GREEN);
	// image.setBacksideColor(Color.RED);

	// Standard java code for making the background a given color.

	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, WIDTH, HEIGHT);

	// rotate the image so that various surfaces will appear with
	// different shades of gray.

	image.setCoordRotation(Math.PI/6.0, Math.PI/4.0, 0.0);

	// Scale the image so there is a bit of space around the object
	// at the edges of the frame (the 50.0 parameter is the number
	// of pixels to leave as a border.

	m3d.setImageParameters(image, 50.0);
	
	m3d.render(image);
	// ImageIO.write(image, "png", new File("lockpart.png"));
	image.write();
    }
}
