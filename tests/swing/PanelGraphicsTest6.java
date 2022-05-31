import org.bzdev.swing.*;
import org.bzdev.gio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PanelGraphicsTest6 {

    public static void main(String argv[]) throws Exception {

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
	    try {
		System.setSecurityManager(new SecurityManager());
	    } catch (UnsupportedOperationException eu) {System.exit(0);}
	}
	PanelGraphics.ExitAccessor ea = new PanelGraphics.ExitAccessor();

	if (after) {
	    try {
		System.setSecurityManager(new SecurityManager());
	    } catch (UnsupportedOperationException eu) {
		System.exit(0);
	    }
	}

	PanelGraphics pg = null;
	if (argv.length > 0) {
	    if (argv[0].equals("--boolean")) {
		pg = PanelGraphics.newFramedInstance
		    (500, 400, "PanelGraphics 6", false, true);
	    } else if (argv[0].equals("--accessor")) {
		pg = PanelGraphics.newFramedInstance
		    (500, 400, "PanelGraphics 6", false, ea);
	    } else /* --none */ {
		pg = PanelGraphics.newFramedInstance
		    (500, 400, "PanelGraphics 6", false, false);
	    }
	}

	Graphics2D g2d = pg.createGraphics();

	g2d.setColor(Color.RED);
	g2d.fillRect(20, 20, 360, 360);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(20, 20, 360, 360);
	pg.imageComplete();
	pg.setVisible(true);

	Thread.currentThread().sleep(20000);
	System.exit(0);
    }
}
