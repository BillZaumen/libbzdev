import org.bzdev.anim2d.*;
import org.bzdev.swing.*;

import java.awt.Color;
import java.awt.BasicStroke;

public class CLTest {
    static int index1 = 0;
    static int index2= 0;
    static int index3 = 0;
    static int index4= 0;
    public static void main(String argv[]) throws Exception {

	int frameWidth = 1046;
	int frameHeight = 722;

	Animation2D a2d = new Animation2D(frameWidth, frameHeight, 1000.0, 40);
	a2d.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);

	AnimatedPanelGraphics apg =
	    AnimatedPanelGraphics.newFramedInstance(a2d, "Near Miss",
						    true, true, null);

	int maxframes = a2d.estimateFrameCount(4.0);

	ConnectingLine2D cl = new ConnectingLine2D(a2d, "cl", true);

	cl.setColor(Color.GREEN);
	cl.setStroke(new BasicStroke(2.0F));
	cl.setVisible(true);

	cl.configure(() -> {
		return -10 + 25.0*(index1++) / 160.0;
	    }, () -> {
		return 10.0;
	    }, () -> {
		return -10 + 25.0*(index2++) / 160.0;
	    }, () -> {
		return -10.0;
	    });
                 	
	ConnectingLine2DFactory clf =
	    new ConnectingLine2DFactory(a2d);

	clf.set("u1", 0.2);
	clf.set("u2", 0.5);
	
	clf.set("color.red", 255);
	clf.set("color.green", 255);
	clf.set("color.blue", 0);
	clf.set("stroke.width", 2.0);
	clf.set("visible", true);
	clf.set("zorder", 1);

	ConnectingLine2D cl2 = clf.createObject("cl2");

	cl2.configure(() -> {
		return -5 + 25.0*(index3++) / 160.0;
	    }, () -> {
		return 10.0;
	    }, () -> {
		return -5 + 25.0*(index4++) / 160.0; 
	    }, () -> {
		return -10.0;
	    });

	cl2.configure(0.2, 0.5);

	ConnectingLine2D cl3 = clf.createObject("cl3");
	cl3.setColor(Color.GREEN);
	cl3.setStroke(new BasicStroke(2.0F));
	cl3.setVisible(true);

	cl3.configure(25.0*(index3++) / 160.0,
		      10.0,
		      () -> {
			  return 25.0*(index4++) / 160.0;
		      }, () -> {
			  return -10.0;
		      });

	ConnectingLine2D cl4 = clf.createObject("cl4");
	cl4.setColor(Color.YELLOW);
	cl4.setStroke(new BasicStroke(2.0F));
	cl4.setVisible(true);

	cl4.configure(() -> {
		return 5 + 25.0*(index3++) / 160.0;
	    }, () -> {
		return 10.0;
	    }, 5 + 25.0*(index4++) / 160.0, -10.0);


	a2d.initFrames(maxframes, apg);
	a2d.scheduleFrames(0, maxframes);
	a2d.run();
	apg.close();
    }
}
