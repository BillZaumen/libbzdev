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

public class LockPart {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	// useful for debugging.
	m3d.setStackTraceMode(true);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, 0.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);
	sgb.addRectangles(10.0, 10.0, 80.0, 80.0, 10.0, 0.0);
	sgb.removeRectangles(20.0, 20.0, 60.0, 60.0);

	SteppedGrid sg = sgb.create();

	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}

	int WIDTH = 700;
	int HEIGHT = 700;
	FileOutputStream os = new FileOutputStream(new File("lockpart2.ps"));
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

	File dir = new File("lptmp2");
	dir.mkdirs();
	for (File f: dir.listFiles()) {
	    f.delete();
	}

	int maxframes = a2d.estimateFrameCount(32.0);
	a2d.initFrames(maxframes, "lptmp2/lp-", "png");
	a2d.scheduleFrames(0, maxframes);
	a2d.run();
    }
}
