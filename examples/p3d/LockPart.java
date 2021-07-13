import org.bzdev.p3d.*;
import org.bzdev.p3d.P3d.Rectangle;
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

public class LockPart {
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
	
	double h = 60.0; // hole height;
	double h1 = 4.0; // first level height;
	double h2 = 8.0;  // second level height;


	// set to false only for debugging, to see inside
	boolean top = true;
	// set to false only for debugging, to see inside
	boolean bottom = true;

	// set to false only for debugging, to see inside
	boolean sides1 = true;
	// set to false only for debugging, to see inside
	boolean sides2 = true;

	// set to false only for debugging, to see inside
	boolean inside1 = true;

	// set to false only for debugging, to see inside
	boolean inside2 = true;

	// we only test if we include the whole model
	boolean verify = (top && bottom && sides1 && sides2
			  && inside1 && inside2);



	// Add the top horizontal sections
	double y1 = h1 + h2;
	double y2 = y1 + h;
	double y3 = y2 + h2;
	double y4 = y3 + h1;


	if (top) {
	    Rectangle.addH(m3d, z1, 0.0, 0.0, w1, h1);
	    Rectangle.addH(m3d, z1, 0.0, h1, w1, y1);
	    Rectangle.addH(m3d, z1, 0.0, y1, w1, y2);
	    Rectangle.addH(m3d, z1, 0.0, y2, w1, y3);
	    Rectangle.addH(m3d, z1, 0.0, y3, w1, y4);
	}

	if (bottom) {
	    Rectangle.addFlippedH(m3d, z0, 0.0, 0.0, w1, h1);
	    Rectangle.addFlippedH(m3d, z0, 0.0, h1, w1, y1);
	    Rectangle.addFlippedH(m3d, z0, 0.0, y1, w1, y2);
	    Rectangle.addFlippedH(m3d, z0, 0.0, y2, w1, y3);
	    Rectangle.addFlippedH(m3d, z0, 0.0, y3, w1, y4);
	}

	double x1 = w1 + 2 * w2 + w;
	double x2 = x1 + w1;

	if (top) {
	    Rectangle.addH(m3d, z1, x1, 0.0, x2, h1);
	    Rectangle.addH(m3d, z1, x1, h1, x2, y1);
	    Rectangle.addH(m3d, z1, x1, y1, x2, y2);
	    Rectangle.addH(m3d, z1, x1, y2, x2, y3);
	    Rectangle.addH(m3d, z1, x1, y3, x2, y4);
	}

	if (bottom) {
	    Rectangle.addFlippedH(m3d, z0, x1, 0.0, x2, h1);
	    Rectangle.addFlippedH(m3d, z0, x1, h1, x2, y1);
	    Rectangle.addFlippedH(m3d, z0, x1, y1, x2, y2);
	    Rectangle.addFlippedH(m3d, z0, x1, y2, x2, y3);
	    Rectangle.addFlippedH(m3d, z0, x1, y3, x2, y4);
	}

	double x3 = w1 + w2;
	double x4 = x3 + w;
	double x5 = x4 + w2;
	double x6 = x5 + w1;

	if (top) {
	    Rectangle.addH(m3d, z1, w1, 0.0, x3, h1);
	    Rectangle.addH(m3d, z1, x3, 0.0, x4, h1);
	    Rectangle.addH(m3d, z1, x4, 0.0, x5, h1);

	    Rectangle.addH(m3d, z1, w1, y3, x3, y4);
	    Rectangle.addH(m3d, z1, x3, y3, x4, y4);
	    Rectangle.addH(m3d, z1, x4, y3, x5, y4);

	    Rectangle.addH(m3d, z2, w1, h1, x3, y1);
	    Rectangle.addH(m3d, z2, w1, y1, x3, y2);
	    Rectangle.addH(m3d, z2, w1, y2, x3, y3);

	    Rectangle.addH(m3d, z2, x4, h1, x5, y1);
	    Rectangle.addH(m3d, z2, x4, y1, x5, y2);
	    Rectangle.addH(m3d, z2, x4, y2, x5, y3);

	    Rectangle.addH(m3d, z2, x3, h1, x4, y1);
	    Rectangle.addH(m3d, z2, x3, y2, x4, y3);
	}

	if (bottom) {
	    Rectangle.addFlippedH(m3d, z0, w1, 0.0, x3, h1);
	    Rectangle.addFlippedH(m3d, z0, x3, 0.0, x4, h1);
	    Rectangle.addFlippedH(m3d, z0, x4, 0.0, x5, h1);

	    Rectangle.addFlippedH(m3d, z0, w1, y3, x3, y4);
	    Rectangle.addFlippedH(m3d, z0, x3, y3, x4, y4);
	    Rectangle.addFlippedH(m3d, z0, x4, y3, x5, y4);

	    Rectangle.addFlippedH(m3d, z0, w1, h1, x3, y1);
	    Rectangle.addFlippedH(m3d, z0, w1, y1, x3, y2);
	    Rectangle.addFlippedH(m3d, z0, w1, y2, x3, y3);

	    Rectangle.addFlippedH(m3d, z0, x4, h1, x5, y1);
	    Rectangle.addFlippedH(m3d, z0, x4, y1, x5, y2);
	    Rectangle.addFlippedH(m3d, z0, x4, y2, x5, y3);

	    Rectangle.addFlippedH(m3d, z0, x3, h1, x4, y1);
	    Rectangle.addFlippedH(m3d, z0, x3, y2, x4, y3);
	}

	if (sides1) {
	    Rectangle.addV(m3d, 0.0, 0.0, z0, w1, 0.0, z1);
	    Rectangle.addV(m3d, w1, 0.0, z0, x3, 0.0, z1);
	    Rectangle.addV(m3d, w1, h1, z1, x3, h1, z2);
	    Rectangle.addV(m3d, x3, 0.0, z0, x4, 0.0, z1);
	    Rectangle.addV(m3d, x3, h1, z1, x4, h1, z2);
	    Rectangle.addV(m3d, x4, 0.0, z0, x5, 0.0, z1);
	    Rectangle.addV(m3d, x4, h1, z1, x5, h1, z2);
	    Rectangle.addV(m3d, x5, 0.0, z0, x6, 0.0, z1);

	    Rectangle.addV(m3d, x6, 0.0, z0, x6, h1, z1);
	    Rectangle.addV(m3d, x6, h1, z0, x6, y1, z1);
	    Rectangle.addV(m3d, x5, h1, z1, x5, y1, z2);
	    Rectangle.addV(m3d, x6, y1, z0, x6, y2, z1);
	    Rectangle.addV(m3d, x5, y1, z1, x5, y2, z2);
	    Rectangle.addV(m3d, x6, y2, z0, x6, y3, z1);
	    Rectangle.addV(m3d, x5, y2, z1, x5, y3, z2);
	    Rectangle.addV(m3d, x6, y3, z0, x6, y4, z1);
	}

	if (sides2) {
	    Rectangle.addFlippedV(m3d, 0.0, y4, z0, w1, y4, z1);
	    Rectangle.addFlippedV(m3d, w1, y4, z0, x3, y4, z1);
	    Rectangle.addFlippedV(m3d, w1, y3, z1, x3, y3, z2);
	    Rectangle.addFlippedV(m3d, x3, y4, z0, x4, y4, z1);
	    Rectangle.addFlippedV(m3d, x3, y3, z1, x4, y3, z2);
	    Rectangle.addFlippedV(m3d, x4, y4, z0, x5, y4, z1);
	    Rectangle.addFlippedV(m3d, x4, y3, z1, x5, y3, z2);
	    Rectangle.addFlippedV(m3d, x5, y4, z0, x6, y4, z1);

	    Rectangle.addFlippedV(m3d, 0.0, 0.0, z0, 0.0, h1, z1);
	    Rectangle.addFlippedV(m3d, 0.0, h1, z0, 0.0, y1, z1);
	    Rectangle.addFlippedV(m3d, w1, h1, z1, w1, y1, z2);
	    Rectangle.addFlippedV(m3d, 0.0, y1, z0, 0.0, y2, z1);
	    Rectangle.addFlippedV(m3d, w1, y1, z1, w1, y2, z2);
	    Rectangle.addFlippedV(m3d, 0.0, y2, z0, 0.0, y3, z1);
	    Rectangle.addFlippedV(m3d, w1, y2, z1, w1, y3, z2);
	    Rectangle.addFlippedV(m3d, 0.0, y3, z0, 0.0, y4, z1);
	}

	if (inside1) {
	    Rectangle.addV(m3d, x3, y2, z0, x4, y2, z2);
	    Rectangle.addV(m3d, x3, y1, z0, x3, y2, z2);
	}

	if (inside2) {
	    Rectangle.addFlippedV(m3d, x3, y1, z0, x4, y1, z2);
	    Rectangle.addFlippedV(m3d, x4, y1, z0, x4, y2, z2);
	}

	if (verify) {
	    if (m3d.notPrintable(System.out)) {
		System.exit(1);
	    }
	    // Now create  an STL file suitable for 3D printing
	    m3d.writeSTL("Lockpart STL File", "lockpart.stl");
	}

	// Also create a picture of the object.

	int WIDTH = 700;
	int HEIGHT = 700;
	FileOutputStream os = new FileOutputStream(new File("lockpart.ps"));
	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(os, WIDTH, HEIGHT, "ps");

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

	Animation2D a2d = new Animation2D(700, 700, 30000.0, 1000);
	a2d.setBackgroundColor(Color.blue.darker().darker());

	Model3DViewFactory factory = new Model3DViewFactory(a2d);
	factory.setModel(m3d);

	factory.set("edgeColor.green", 255);
	factory.set("border", 50.0);
	factory.set("changeScale", false);

	factory.set("phi", 30.0);
	factory.set("theta", 30.0);
	factory.set("magnification", 0.9);

	factory.set("timeline.time", 0, 2.0);
	factory.set("timeline.phiRate", 0, 180.0/10);
	factory.set("timeline.time", 1, 12.0);
	factory.set("timeline.phiRate", 1, 0.0);
	factory.set("timeline.time", 2, 13.0);
	factory.set("timeline.thetaRate", 2, 90.0/5.0);
	factory.set("timeline.time", 3, 18.0);
	factory.set("timeline.thetaRate", 3,  0.0);
	factory.set("timeline.phiRate", 3, 180.0/10.0);
	factory.set("timeline.time", 4, 28.0);
	factory.set("timeline.phiRate", 4, 0.0);
	factory.set("timeline.time", 5, 29.0);
	factory.set("timeline.phi", 5, 0.0);
	factory.set("timeline.theta", 5, 0.0);
	factory.set("timeline.psi", 5, 0.0);
	factory.set("timeline.time", 6, 30.0);
	factory.set("timeline.forceScaleChange", 6, true);

	factory.createObject("view");

	File dir = new File("lptmp");
	dir.mkdirs();
	for (File f: dir.listFiles()) {
	    f.delete();
	}

	int maxframes = a2d.estimateFrameCount(32.0);
	a2d.initFrames(maxframes, "lptmp/lp-", "png");
	a2d.scheduleFrames(0, maxframes);
	a2d.run();
    }
}
