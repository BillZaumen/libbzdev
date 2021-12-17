import org.bzdev.swing.*;
import org.bzdev.gio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PanelGraphicsTest3 {

    static PanelGraphics pg = null;

    public static void main(String argv[]) throws Exception {

	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	    DarkmodeMonitor.setSystemPLAF();
	    DarkmodeMonitor.init();
	}
	SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    pg = PanelGraphics.newFramedInstance(500, 400,
							 "PanelGraphics 3",
							 false, true);
		}
	    });

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
