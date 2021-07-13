import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.BasicSplinePath2D;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Formatter;

class Background extends AnimationObject2D {
    Background(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
    }

    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	long simTime = getAnimation().currentTicks();
	System.out.println("simTime = " + simTime
			   + ", frame = " + (1L + (simTime/40)));
	g2d.setColor(Color.GRAY.darker().darker());
	g2d.fillRect(0,0,1600, 900);
	g2d.setColor(Color.WHITE);
	for (int j = 0; j < 400; j++) {
	    double xj = j * 30.0;
	    graph.fill(g2d, new Rectangle2D.Double(xj, 50.0, 15.0, 5.0));
	    if (j % 10 == 0) {
		graph.fill(g2d, new Rectangle2D.Double(xj, 45.0, 15.0, 5.0));
	    }
	}
    }
}


public class GraphViewTest {

    public static void main(String argv[]) {
	try {

	    Animation2D animation = new Animation2D();
	    //Graph graph = new Graph(1600, 900, BufferedImage.TYPE_INT_RGB);
	    Background bg = new Background(animation, "background", true);
	    bg.setZorder(1, true);
	    final GraphView gv = new GraphView(animation, "graphview", true);
	    gv.setZorder(0, true);

	    gv.initialize(0.0, 0.0, 0.0, 0.0,
			  1600.0/200.0, 1600.0/200.0,
			  1.0);

	    BasicSplinePath2D path = new BasicSplinePath2D();
	    path.moveTo(0.0, 0.0);
	    path.lineTo(1200.0, 0.0);
	    gv.setPath(path, 0.0);
	    gv.setPathVelocity(1200.0/30.0);
	    gv.printConfiguration();
	    gv.printState();
	    if (argv.length > 0 && argv[0].equals("print")) {
		System.out.println("... now try printing with indentation");
		gv.printConfiguration("    ");
		gv.printState("    ");
		System.exit(0);
	    }

	    int secs = 30;

	    animation.initFrames(25*secs, "tmp/col-", "png");
	    animation.scheduleFrames(0, 25 * secs);

	    File dir = new File("tmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }

	    animation.scheduleCall(new Callable() {
		    public void call() {
			gv.setLogZoomRate(Math.log(1/2.0) / 15.0);
		    }
		}, 25 * 15 * animation.getTicksPerFrame());

	    animation.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}