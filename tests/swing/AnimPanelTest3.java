import org.bzdev.anim2d.*;
import org.bzdev.geom.*;
import org.bzdev.swing.*;
import org.bzdev.gio.*;
import java.awt.Color;

public class AnimPanelTest3 {
    public static void main(String argv[]) throws Exception {

	Animation2D animation = new Animation2D(400, 250, 1000.0, 40);

	ISWriterOps agp = null;

	boolean before = false;
	boolean after = false;
	if (argv.length > 1) {
	    if (argv[1].equals("--before")) {
		before = true;
	    } else if (argv[1].equals("--after")) {
		after = true;
	    }
	}

	if (before) {
	    System.setSecurityManager(new SecurityManager());
	}

	AnimatedPanelGraphics.ExitAccessor ea =
	    new AnimatedPanelGraphics.ExitAccessor();

	if (after) {
	    System.setSecurityManager(new SecurityManager());
	}
	
	AnimatedPanelGraphics apg = null;

	if (argv[0].equals("--false"))  {
	    apg = AnimatedPanelGraphics.newFramedInstance
		(animation, "Test Panel", true, false,
		 AnimatedPanelGraphics.Mode.START_PAUSED_SELECTABLE);
	} else if (argv[0].equals("--true")) {
	    apg = AnimatedPanelGraphics.newFramedInstance
		(animation, "Test Panel", true, true,
		 AnimatedPanelGraphics.Mode.START_PAUSED_SELECTABLE);
	} else if (argv[0].equals("--accessor")) {
	    apg = AnimatedPanelGraphics.newFramedInstance
		(animation, "Test Panel", true, ea,
		 AnimatedPanelGraphics.Mode.START_PAUSED_SELECTABLE);
	}


	double scaleFactor = animation.getWidth() / 40.0;
	animation.setRanges(0.0, 0.0, 0.5, 0.5,
			    scaleFactor, scaleFactor);
	animation.setBackgroundColor(Color.BLACK);
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
	Thread.currentThread().sleep(20000);
	System.exit(0);
    }
}
