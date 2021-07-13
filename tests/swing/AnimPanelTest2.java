import org.bzdev.anim2d.*;
import org.bzdev.geom.*;
import org.bzdev.swing.*;
import org.bzdev.gio.*;
import java.awt.Color;

public class AnimPanelTest2 {
    public static void main(String argv[]) throws Exception {

	Animation2D animation = new Animation2D(400, 250, 1000.0, 40);

	ISWriterOps apg = (argv.length == 0)?
	    AnimatedPanelGraphics.newFramedInstance
	    (animation, "Harmonic Oscillator", true, true,
	     AnimatedPanelGraphics.Mode.START_PAUSED_SELECTABLE):
	    new ImageSequenceWriter(argv[0]);

	double scaleFactor = animation.getWidth() / 40.0;
	animation.setRanges(0.0, 0.0, 0.5, 0.5,
			    scaleFactor, scaleFactor);
	animation.setBackgroundColor(Color.BLACK);

	Ball ball = new Ball(animation, "ball", true);
	ball.init(0.0, 5.0, 1.0, 1.0);
	ball.setZorder(1, true);

	AnimationPath2D path  = new AnimationPath2D(animation, "path", true);
	BasicSplinePath2D spath = new BasicSplinePath2D();
	spath.moveTo(0.0, 0.0);
	spath.lineTo(0.0, 10.0);
	path.setPath(spath);
	path.setZorder(0, false);
	
	double zoom = 4.3;
	// zoom = 1.0;
	GraphView gv = new GraphView(animation, "view", true);
	// gv.setZorder(0, true);
	gv.initialize(0.0, 0.0, 0.5, 0.5, scaleFactor, scaleFactor, zoom);
	gv.setPath(spath,0.0, 0.0, false, 0.0);
	gv.setPathVelocity((10.0/30.0)/zoom);
	// apg.setVisible(true);

	int maxframes = animation.estimateFrameCount(30.0);
	System.out.println("maxframes = " + maxframes);
	animation.initFrames(maxframes, apg);

	animation.scheduleFrames(0, maxframes);

	long astart = System.nanoTime();
	animation.run();
	long aend = System.nanoTime();
	System.out.println("animation.run() took "
			   + ((aend - astart)/1000000)
			   + " ms");
	apg.close();

	if (argv.length == 0) {
	    Thread.currentThread().sleep(50000);
	}
	System.exit(0);
    }
}
