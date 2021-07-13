import org.bzdev.p3d.*;
import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.devqsim.SimFunction;
import org.bzdev.math.RealValuedFunction;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;

public class AnimTest4 {
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

	SimFunction phiF =
	    new SimFunction(a2d, "phiF", false, new RealValuedFunction() {
		    public double valueAt(double t) {
			return -Math.PI/6.0;
		    }
		});
	SimFunction thetaF =
	    new SimFunction(a2d, "thetaF", false, new RealValuedFunction() {
		    double rate = Math.toRadians(90.0)/15.0;
		    public double valueAt(double t) {
			if (t >= 10.0) return 10.0 * rate;
			return t*rate;
		    }
		});
	SimFunction psiF =
	    new SimFunction(a2d, "psif", false, new RealValuedFunction() {
		    public double valueAt(double t) {
			return 0.0;
		    }
		});

	// factory.set("backsideColor.red", 255);
	factory.set("edgeColor.green", 255);
	factory.set("border", 50.0);
	
	factory.set("phiFunction", phiF);
	factory.set("thetaFunction", thetaF);
	factory.set("psiFunction", psiF);
	factory.set("phiRate", 0.0);
	factory.set("thetaRate", 90.0/15.0);
	factory.set("lightsourcePhi", 0.0);
	factory.set("visible", true);

	SimFunction nullF = a2d.nullFunction();
	SimFunction lsPhiF =
	    new SimFunction(a2d,"lsPhiF", false, new RealValuedFunction() {
		    public double valueAt(double t) {
			return 0.0;
		    }
		});
	SimFunction lsThetaF =
	    new SimFunction(a2d,"lsThetaF", false, new RealValuedFunction() {
		    double rate = -Math.PI/8.0;
		    public double valueAt(double t) {
			if (t <= 10.0 || t >= 14.0) return 0.0;
			else if (t < 12.0) return (t-10.0)*rate;
			else return (14.0 - t) * rate;
		    }
		});

	SimFunction magF =
	    new SimFunction(a2d, "magF", false, new RealValuedFunction() {
			double rate = Math.log(2.0) /4.0;
		    public double valueAt(double t) {
			if (t <= 14.0) return 1.0;
			else if (t >= 18.0) return 2.0;
			else return Math.exp(rate * (t-14.0));
		    }
		});

	SimFunction xfractF =
	    new SimFunction(a2d, "xfractF", false, new RealValuedFunction() {
		    double rate = 1.0/6.0;
		    public double valueAt(double t) {
			if (t <= 18.0) return 0.0;
			else return rate *(t-18.0);
		    }
		});


	SimFunction yfractF =
	    new SimFunction(a2d, "yfractF", false, new RealValuedFunction() {
		    double rate = 1.0/12.0;
		    public double valueAt(double t) {
			if (t <= 18.0) return 0.0;
			else return rate *(t-18.0);
		    }
		});

	factory.set("timeline.time", 1, 10.0);
	factory.set("timeline.lightsourcePhiFunction", 1, lsPhiF);
	factory.set("timeline.lightsourceThetaFunction", 1, lsThetaF);
	factory.set("timeline.magnificationFunction", 1, magF);
	factory.set("timeline.xfractFunction", 1, xfractF);
	factory.set("timeline.yfractFunction", 1, yfractF);

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