import org.bzdev.swing.*;
import org.bzdev.gio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PanelGraphicsTest4 {

    public static void main(String argv[]) throws Exception {
	final PanelGraphics pg = PanelGraphics.newFramedInstance
	    (500, 400, "PanelGraphics 4", false, true);

	SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    try {
			Graphics2D g2d = pg.createGraphics();
			g2d.setColor(Color.RED);
			g2d.fillRect(20, 20, 360, 360);
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(5.0F));
			g2d.drawRect(20, 20, 360, 360);
			g2d.dispose();
			pg.imageComplete();
			pg.setVisible(true);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    });

	PanelGraphics.Creator pgc1 = pg.newPanelGraphicsCreator(false);
	Graphics2D g2d = pgc1.createGraphics();
	g2d.setColor(Color.YELLOW);
	g2d.fillRect(20, 20, 360, 360);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(20, 20, 360, 360);
	g2d.dispose();

	PanelGraphics.Creator pgc2 = pg.newPanelGraphicsCreator(false);
	g2d = pgc2.createGraphics();
	g2d.setColor(Color.GREEN);
	g2d.fillRect(20, 20, 360, 360);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(20, 20, 360, 360);
	g2d.dispose();
	Thread.sleep(5000);
	pgc1.apply();
	// pg.imageComplete();
	Thread.sleep(5000);
	pgc2.apply();
	pg.imageComplete();
	Thread.sleep(5000);
	System.exit(0);
    }
}
