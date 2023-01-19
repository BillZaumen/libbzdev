import org.bzdev.gio.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class BITest {
    public static void main(String argv[]) throws Exception {

	String fn =  "bi." + argv[0];
	FileOutputStream fos = new FileOutputStream(fn);
	
	OutputStreamGraphics osg = OutputStreamGraphics
	    .newInstance(fos, 300, 300, argv[0]);

	OSGBufferedImage bi = new OSGBufferedImage(osg);
	Graphics2D g2d = bi.createGraphics();
	g2d.setBackground(Color.DARK_GRAY);
	g2d.clearRect(0, 0, 300, 300);
	g2d.setColor(Color.WHITE);
	g2d.drawString("Hello", 10, 100);
	Shape shape = new
	    Rectangle2D.Double(100.0, 100.0, 100.0, 100.0);
	g2d.setColor(Color.RED);
	g2d.fill(shape);
	g2d.setColor(Color.WHITE);
	g2d.draw(shape);

	bi.imageComplete();
	fos.flush();
	fos.close();
    }
}
