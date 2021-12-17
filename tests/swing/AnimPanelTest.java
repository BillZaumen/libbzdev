import org.bzdev.anim2d.*;
import org.bzdev.swing.*;
import java.awt.Color;


public class AnimPanelTest {
    public static void main(String argv[]) throws Exception {
	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	    DarkmodeMonitor.setSystemPLAF();
	    DarkmodeMonitor.init();
	}

	System.setSecurityManager(new SecurityManager());

	Animation2D animation = new Animation2D(400, 250, 1000.0, 40);

	AnimatedPanelGraphics apg =
	    AnimatedPanelGraphics.newFramedInstance
	    (animation, "Harmonic Oscillator", false, true,
	     AnimatedPanelGraphics.Mode.START_PAUSED_SELECTABLE);


	double scaleFactor = animation.getWidth() / 40.0;
	animation.setRanges(0.0, 0.0, 0.5, 0.5,
			    scaleFactor, scaleFactor);
	animation.setBackgroundColor(Color.BLACK);

	Ball ball = new Ball(animation, "ball", true);
	ball.init(0.0, 5.0, 1.0, 1.0);
	ball.setZorder(1, true);

	apg.setVisible(true);

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
	if (systemUI == false) {
	    Thread.currentThread().sleep(50000);
	    System.exit(0);
	}
    }
}
