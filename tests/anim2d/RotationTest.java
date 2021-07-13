import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.BasicSplinePath2D;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Formatter;

public class RotationTest {

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

	    final GraphView gv = new GraphView(animation, "graphview", true);
	    gv.setVisible(true);

	    gv.initialize(100.0, 100.0, 0.5, 0.5,
			  800.0/200.0, 800.0/200.0,
			  1.0);
	    // gv.setRotationRate((Math.PI/2.0)/10.0);
	    gv.setAngularVelocity((Math.PI/2.0)/10.0);

	    File dir = new File("rottmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }

	    int maxframes = animation.estimateFrameCount(10.0);

	    animation.initFrames(maxframes, "rottmp/col-", "png");
	    animation.scheduleFrames(0, maxframes);
	    animation.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
