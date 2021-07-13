import org.bzdev.p3d.*;
import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;

public class AnimTest {
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

	final Model3DView view = new Model3DView(a2d, "view", true);
	view.setModel(m3d);

	view.setEdgeColor(Color.GREEN);
	view.setBorder(50.0);

	view.setCoordRotation(-Math.PI/6.0, 0.0, 0.0);
	view.setThetaRate(Math.toRadians(90.0)/15.0);

	view.setVisible(true);
	
	System.out.println("phi = " + view.getPhi());
	System.out.println("theta = " + view.getTheta());
	System.out.println("psi = " + view.getPsi());
	System.out.println("border = " + view.getBorder());
	System.out.println("xfract = " + view.getXFract());
	System.out.println("yfract = " + view.getYFract());
	System.out.println("delta = " + view.getDelta());
	System.out.println("ls phi = " + view.getLightSourcePhi());
	System.out.println("ls theta = " + view.getLightSourceTheta());
	System.out.println("color factor = " + view.getColorFactor());
	System.out.println("backside color = " + view.getBacksideColor());
	System.out.println("edge color = " + view.getEdgeColor());
	System.out.println("default segment color = "
			   + view.getDefaultSegmentColor());
	System.out.println("default backside segment color = "
			   + view.getDefaultBacksideSegmentColor());
	System.out.println("phi rate = " + view.getPhiRate());
	System.out.println("theta rate = " + view.getThetaRate());
	System.out.println("psi rate = " + view.getPsiRate());
	System.out.println("xfract rate = " + view.getXFractRate());
	System.out.println("yfract rate = " + view.getYFractRate());
	System.out.println("magnification = " + view.getMagnification());
	System.out.println("log-magnification rate = "
			   + view.getLogMagnificationRate());
	view.printConfiguration();
	view.printState();
	a2d.scheduleCall(new Callable() {
		public void call() {
		    view.setThetaRate(0.0);
		    view.setXFract(0.0);
		    view.setLightSource(Math.PI, 0.0);
		    view.setLightSourceThetaRate(Math.PI/8.0);
		}
	    }, a2d.getTicks(10.0));

	a2d.scheduleCall(new Callable() {
		public void call() {
		    view.setLightSourceThetaRate(-Math.PI/8.0);
		}
	    }, a2d.getTicks(12.0));
	
	a2d.scheduleCall(new Callable() {
		public void call() {
		    view.setLightSourceThetaRate(0.0);
		    view.setLightSource(0.0, 0.0);
		    view.setLogMagnificationRate(2.0, 4.0);
		}
	    }, a2d.getTicks(14.0));


	a2d.scheduleCall(new Callable() {
		public void call() {
		    view.setLogMagnificationRate(0.0);
		    view.setXFractRate(1.0/6.0);
		    view.setYFractRate(1.0/12.0);
		}
	    }, a2d.getTicks(18.0));


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