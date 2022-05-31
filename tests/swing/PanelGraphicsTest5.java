import org.bzdev.swing.*;
import org.bzdev.gio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PanelGraphicsTest5 {

    public static void main(String argv[]) throws Exception {
	try {
	    System.setSecurityManager(new SecurityManager());
	} catch (UnsupportedOperationException eu) {}

	PanelGraphics pg1 = PanelGraphics.newFramedInstance
	    (500, 400, "PanelGraphics 5 ", true, true);

	PanelGraphics.Creator pgc = pg1.newPanelGraphicsCreator(true);
	Graphics2D g2d = pgc.createGraphics();
	System.out.println("g2d transform for pg1 = " +g2d.getTransform());
	g2d.setColor(Color.GREEN);
	g2d.fillRect(20, 20, 360, 360);
	g2d.dispose();
	Thread.currentThread().sleep(5000);
	System.out.println("applying");
	pgc.apply();
	Thread.currentThread().sleep(5000);
	System.out.println("applying again");
	pgc.apply();
	pgc.dispose();
	Thread.currentThread().sleep(8000);
	System.exit(0);
    }
}
