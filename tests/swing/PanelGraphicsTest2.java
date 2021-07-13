import org.bzdev.swing.*;
import org.bzdev.gio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PanelGraphicsTest2 {

    public static void main(String argv[]) throws Exception {
	PanelGraphics pg = PanelGraphics.newFramedInstance
	    (400, 400, "PanelGraphics 2", false, true);

	Graphics2D g2d = pg.createGraphics();
	g2d.setColor(Color.RED);
	g2d.fillRect(20, 20, 360, 360);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(20, 20, 360, 360);
	pg.imageComplete();
	System.out.println("pg.isVisble() = " + pg.isVisible());
	Thread.sleep(2000);
	pg.setVisible(true);
	System.out.println("pg.isVisble() = " + pg.isVisible());

	OutputStreamGraphics osg = OutputStreamGraphics
	    .newInstance(new FileOutputStream("pgtest.png"), "png");

	pg.write(osg);
	osg.close();

	Thread.sleep(15000);
	System.exit(0);
    }
}
