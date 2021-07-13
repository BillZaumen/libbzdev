import org.bzdev.anim2d.*;
import org.bzdev.math.RungeKuttaMV;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;


public class HarmonicAnim {
    public static void main(String argv[]) {
	try {
	    Animation2D animation =
		new Animation2D(800, 450, 1000.0, 40);
	    double scaleFactor = animation.getWidth() / 40.0;
	    animation.setRanges(0.0, 0.0, 0.5, 0.5,
				scaleFactor, scaleFactor);
	    animation.setBackgroundColor(Color.BLACK);

	    Ball ball = new Ball(animation, "ball", true);
	    ball.init(0.0, 5.0, 1.0, 1.0);
	    ball.setZorder(1, true);
	
	    File dir = new File("htmp");
	    dir.mkdirs();
	    int maxframes = animation.estimateFrameCount(30.0);
	    animation.initFrames(maxframes, "htmp/img-", "png");
	    animation.scheduleFrames(0, maxframes);
	    animation.run();
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

    }
}