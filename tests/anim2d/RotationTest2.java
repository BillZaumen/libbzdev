import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.BasicSplinePath2D;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Formatter;

public class RotationTest2 {

    public static class Rect  extends AnimationObject2D {
	Rect(Animation2D animation, String name, boolean intern) {
	    super(animation, name, intern);
	}

	public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	    long simTime = getAnimation().currentTicks();
	    g2d.setColor(Color.BLUE.darker());
	    graph.fill(g2d, new Rectangle2D.Double(70.0, 80.0, 60.0, 40.0));
	}
    }

    public static void main(String argv[]) {
	try {

	    Animation2D animation = new Animation2D(800, 800, 10000.0, 400);
	    Background bg = new Background(animation, "background", true);
	   
	    Rect rect = new Rect(animation, "rect", true);
	    rect.setVisible(true);
	    rect.setZorder(0);

	    AnimationPath2DFactory pathf =
		new AnimationPath2DFactory(animation);

	    AnimationLayer2DFactory alf =
		new AnimationLayer2DFactory(animation);

	    alf.set("zorder", 2);
	    alf.set("visible", true);
	    alf.set("object.type", 0, "IMAGE");
	    alf.set("object.x", 0, 100.0);
	    alf.set("object.y", 0, 100.0);
	    alf.set("object.refPoint", 0, "CENTER");
	    alf.set("object.imageURL", 0, "file:../graphs/testimg.png");
	    alf.set("object.imageAngle", 0, 0.0);
	    alf.set("object.imageScaleX", 0, 0.5);
	    alf.set("object.imageScaleY", 0, 0.5);
	    alf.set("object.imageInGCS", 0, false);
	    alf.createObject("image");

	    GraphViewFactory gvf = new GraphViewFactory(animation);

	    pathf.set("cpoint.type", 0, "MOVE_TO");
	    pathf.set("cpoint.x", 0, 100.0);
	    pathf.set("cpoint.y", 0, 100.0);
	    pathf.set("cpoint.type", 1, "SEG_END");
	    pathf.set("cpoint.x", 1, 100.0);
	    pathf.set("cpoint.y", 1, 200.0);

	    AnimationPath2D path = pathf.createObject("path");

	    gvf.set("initialX", 100.0);
	    gvf.set("initialY", 100.0);
	    gvf.set("xFrameFraction", 0.5);
	    gvf.set("yFrameFraction", 0.5);
	    gvf.set("scaleX", 800.0/200.0);
	    gvf.set("scaleY", 800.0/200.0);
	    gvf.set("timeline.time", 0, 0.0);
	    gvf.set("timeline.angularVelocity", 0, 9.0);
	    gvf.set("timeline.time", 1, 10.0);
	    gvf.set("timeline.pathAngle", 1, 90.0);
	    gvf.set("timeline.angularVelocity", 1, 0.0);
	    gvf.set("timeline.path", 1, path);
	    gvf.set("timeline.velocity", 1, 20.0);

	    GraphView gv = gvf.createObject("view");

	    File dir = new File("rottmp2");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }

	    int maxframes = animation.estimateFrameCount(15.0);

	    animation.initFrames(maxframes, "rottmp2/col-", "png");
	    animation.scheduleFrames(0, maxframes);
	    animation.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
