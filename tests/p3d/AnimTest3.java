import org.bzdev.p3d.*;
import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.math.RealValuedFunction;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;

public class AnimTest3 {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	m3d.setStackTraceMode(true);

	P3d.Rectangle.addH(m3d, 30.0, 0.0, 0.0, 50.0, 50.0);
	P3d.Rectangle.addFlippedH(m3d, 0.0, 0.0, 0.0, 50.0, 50.0);

	P3d.Rectangle.addH(m3d, 50.0, 50.0, 0.0, 100.0, 50.0);
	P3d.Rectangle.addFlippedH(m3d, 0.0, 50.0, 0.0, 100.0, 50.0);
	P3d.Rectangle.addV(m3d, 50.0, 50.0, 30.0,  50.0, 0.0, 50.0);

	P3d.Rectangle.addFlippedV(m3d, 100.0, 50.0, 30.0,  100.0, 0.0, 50.0);

	P3d.Rectangle.addV(m3d, 0.0, 50.0, 0.0,  0.0, 0.0, 30.0);
	P3d.Rectangle.addFlippedV(m3d, 100.0, 50.0, 0.0,  100.0, 0.0, 30.0);

	P3d.Rectangle.addV(m3d, 0.0, 0.0, 0.0,  50.0, 0.0, 30.0);
	P3d.Rectangle.addV(m3d,50.0, 0.0, 0.0,  100.0, 0.0, 30.0);

	P3d.Rectangle.addFlippedV(m3d, 0.0, 50.0, 0.0,  50.0, 50.0, 30.0);
	P3d.Rectangle.addFlippedV(m3d, 50.0, 50.0, 0.0,  100.0, 50.0, 30.0);
	P3d.Rectangle.addV(m3d, 50.0, 0.0, 30.0,  100.0, 0.0, 50.0);
	P3d.Rectangle.addFlippedV(m3d, 50.0, 50.0, 30.0,  100.0, 50.0, 50.0);

	
	Animation2D a2d = new Animation2D(700, 700, 30000.0, 1000);
	a2d.setBackgroundColor(Color.blue.darker().darker());

	Model3DViewFactory factory = new Model3DViewFactory(a2d);
	factory.setModel(m3d);

	// factory.set("backsideColor.red", 255);
	factory.set("edgeColor.green", 255);
	factory.set("border", 50.0);
	
	factory.set("phi", -30.0);
	factory.set("theta",  0.0);
	factory.set("psi",  0.0);
	factory.set("phiRate", 0.0);
	factory.set("thetaRate", 90.0/15.0);
	factory.set("lightsourcePhi", 0.0);
	factory.set("visible", true);

	factory.set("timeline.time", 1, 10.0);
	factory.set("timeline.thetaRate", 1, 0.0);
	factory.set("timeline.lightsourceTheta", 1, 0.0);
	factory.set("timeline.lightsourcePhi", 1, 180.0);
	factory.set("timeline.lightsourceThetaRate", 1, 22.5);

	factory.set("timeline.time", 2, 12.0);
	factory.set("timeline.lightsourceThetaRate", 2, -22.5);

	factory.set("timeline.time", 3, 14.0);
	factory.set("timeline.lightsourceThetaRate", 3, 0.0);
	factory.set("timeline.lightsourceTheta", 3, 0.0);
	factory.set("timeline.lightsourcePhi", 3, 0.0);
	factory.set("timeline.logmagnificationRate", 3, Math.log(2.0)/4.0);

	factory.set("timeline.time", 4, 18.0);
	factory.set("timeline.logmagnificationRate", 4, 0.0);
	factory.set("timeline.xfractRate", 4, 1.0/6.0);
	factory.set("timeline.yfractRate", 4, 1.0/12.0);

	final Model3DView view = factory.createObject("view");

	int maxFrameCount = a2d.estimateFrameCount(24.0);
	File dir = new File("ltmp");
	dir.mkdirs();
	for (File f: dir.listFiles()) {
	    f.delete();
	}
	a2d.initFrames(maxFrameCount, "ltmp/img-", "png");
	a2d.scheduleFrames(0, maxFrameCount);
	a2d.run();
	System.exit(0);
   }
}