import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.RefPointName;
import org.bzdev.lang.Callable;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.geom.BasicSplinePathBuilder;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.CPoint;
import org.bzdev.math.RealValuedFunction;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class RectObject3 extends DirectedObject2D {
    static Image image;
    double imageScale = 2.0/64;

    static {
	try {
	    image = ImageIO.read(new File("testIcon.png"));
	} catch(IOException eio) {
	    eio.printStackTrace();
	    System.exit(1);
	}
    }

    // Path2D shape;
    RectObject3(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	/*
	shape = new Path2D.Double();
	shape.moveTo(-1.0, -0.5);
	shape.lineTo(1.0, -0.5);
	shape.lineTo(1.0, 0.5);
	shape.lineTo(-1.0, 0.5);
	shape.closePath();
	*/
    }

    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	try {
	    AffineTransform af = getAddToTransform();
	    Animation2D a2d = getAnimation();
	    System.out.format("time %g: x=%g, y=%g, angle=%g (radians: %g)\n",
			      a2d.currentTime(), getX(), getY(),
			      Math.toDegrees(getAngle()), getAngle());
	    
	    Point2D pt = new Point2D.Double(0.0, 0.0);
	    af.transform(pt, pt);
	    double xx = pt.getX();
	    double yy = pt.getY();
	    graph.drawImage(g2d, image, xx, yy, RefPointName.CENTER, getAngle(),
			    imageScale, imageScale, true);
	} catch (IOException eio) {
	    eio.printStackTrace();
	    System.exit(1);
	}
    }
}

public class RelativeTest3 {
    public static void main(String argv[]) {
	try {

	    final Animation2D animation = new Animation2D();
	    //Graph graph = new Graph(1600, 900, BufferedImage.TYPE_INT_RGB);
	    animation.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);

	    final RectObject3 obj = new RectObject3(animation, "obj", true);
	    obj.setZorder(0, true);

	    double r = 8.0;
	    BasicSplinePathBuilder spb = new BasicSplinePathBuilder();

	    AnimationPath2D apath =
		new AnimationPath2D(animation, "path", true);
	    SplinePathBuilder.CPoint[] cpoints =
		new SplinePathBuilder.CPoint[37];
	    cpoints[0] = new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.MOVE_TO, r, 0.0);
	    for (int i = 1; i < 36; i++) {
		double theta = Math.toRadians(i * 10.0);
		cpoints[i] =
		    new SplinePathBuilder.CPoint
		    (SplinePathBuilder.CPointType.SPLINE,
		     r * Math.cos(theta), r * Math.sin(theta));
	    }
	    cpoints[36] =
		new
		SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE);
	    // apath.append(cpoints);
	    spb.initPath(cpoints);
	    BasicSplinePath2D path = spb.getPath();
	    apath.setPath(path);
	    System.out.format("Initial point = (%g, %g), tangent angle = %g\n",
			      path.getX(0.0), path.getY(0.0),
			      Math.toDegrees(Math.atan2(path.dyDu(0.0),
							path.dxDu(0.0))));



	    System.out.format("dyDu(0.0) = %g, dxDu(0.0) = %g\n",
			      path.dyDu(0.0), path.dxDu(0.0));

	    double u = 36.0;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    
	    u = 35.0;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    


	    u = 0.0001;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    u = -u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    
	    u = 35.0 + u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));

	    u = 1.0 + u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));

	    

	    
	    double length = 0.0;
	    for (Path2DInfo.Entry entry:
		     Path2DInfo.getEntries(apath.getPath())) {
		double seglen = entry.getSegmentLength();
		length += seglen;
	    }

	    // create a path using the factory.
	    AnimationPath2DFactory factory =
		new AnimationPath2DFactory(animation);
	    factory.set("color.red", 255);
	    factory.set("color.green", 255);
	    factory.set("color.blue", 255);
	    factory.set("color.alpha", 255);
	    factory.set("stroke.width", 2.0);
	    factory.set("stroke.dashIncrement", 20.0);
	    factory.set("stroke.dashPattern", "- ");
	    factory.set("zorder", 10);
	    factory.set("visible", true);
	    factory.set("cpoint.type", 0, "MOVE_TO");
	    factory.set("cpoint.x", 0, 7.0);
	    factory.set("cpoint.y", 0, 0.0);
	    double rc = 7.0;
	    for (int i = 1; i < 36; i++) {
		double theta = Math.toRadians(i * 10.0);
		factory.set("cpoint.type", i, "SPLINE");
		factory.set("cpoint.x", i, rc* Math.cos(theta));
		factory.set("cpoint.y", i, rc * Math.sin(theta));
	    }
	    factory.set("cpoint.type", 36, "CLOSE");
	    AnimationPath2D innerCircle = factory.createObject("innerCircle");

	    double speed = length / 7.5;
	    int maxFrames = animation.estimateFrameCount(7.5);
	    int maxSimtime = 7500;

	    animation.initFrames(maxFrames, "rtmp3/col-", "png");
	    animation.scheduleFrames(0, maxFrames);
	    File dir = new File("rtmp3");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    obj.setPath(apath, 0.5);
	    obj.setPathVelocity(speed);
	    obj.setPathAcceleration(0.0);

	    System.out.format("time %g: x=%g, y=%g, angle=%g\n",
			      animation.currentTime(), obj.getX(), obj.getY(),
			      Math.toDegrees(obj.getAngle()));

	    animation.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("setting angle relative to false");
			obj.setAngleRelative(false);
		    }
		}, maxSimtime/2);



	    animation.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("setting angle relative to true");
			obj.setAngleRelative(true);
		    }
		}, 3*maxSimtime/4);

	    final RealValuedFunction angleF = new RealValuedFunction() {
		    public double valueAt(double t) {return 0.0;}
		};

	    animation.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("setting angle function");
			obj.setPathAngleByF(angleF);
		    }
		}, 3*maxSimtime/4 + 100);

	    animation.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("setting angle function to null");
			obj.setPathAngleByF(null);
		    }
		}, 3*maxSimtime/4 + 400);



	    animation.run(maxSimtime);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
