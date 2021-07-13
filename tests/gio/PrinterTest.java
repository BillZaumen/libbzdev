import org.bzdev.gio.PrinterGraphics;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class PrinterTest {
    static public void main(String argv[]) {
	try {
	    PrinterGraphics pg = new PrinterGraphics(800, 600);
	    Graphics2D g2d = pg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	    String text= "example string\u00b9\u2074";
	    g2d.drawString(text, 250, 250);
	    g2d.fillRect(0, 0, 200, 200);
	    g2d.setColor(Color.black);
	    g2d.drawRect(2, 596, 796, 2);
	    g2d.fillRect(2, 596, 796, 2);
	    g2d.drawRect(796, 0, 2, 596);
	    g2d.fillRect(796, 0, 2, 596);
	    pg.imageComplete();

	    pg = new PrinterGraphics(600, 800);
	    g2d = pg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	    text= "example string\u00b9\u2074";
	    g2d.drawString(text, 250, 250);
	    g2d.fillRect(0, 0, 200, 200);
	    g2d.setColor(Color.black);
	    g2d.drawRect(2, 796, 596, 2);
	    g2d.fillRect(2, 796, 596, 2);
	    g2d.drawRect(596, 0, 2, 796);
	    g2d.fillRect(596, 0, 2, 796);	    
	    pg.imageComplete();

	    PrinterJob pjob = PrinterJob.getPrinterJob();
	    
	    PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	    PrinterGraphics.setAttributes(aset, 800, 600);
	    System.out.println(aset.get(OrientationRequested.class));
	    aset.add(OrientationRequested.PORTRAIT);
	    System.out.println(aset.get(OrientationRequested.class));
	    pg = new PrinterGraphics(pjob, aset, 800, 600);
	    System.setSecurityManager(new SecurityManager());
	    g2d = pg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
	    text= "example string\u00b9\u2074";
	    g2d.drawString(text, 250, 250);
	    g2d.fillRect(0, 0, 200, 200);
	    g2d.setColor(Color.black);
	    g2d.drawRect(2, 596, 796, 2);
	    g2d.fillRect(2, 596, 796, 2);
	    g2d.drawRect(796, 0, 2, 596);
	    g2d.fillRect(796, 0, 2, 596);
	    pg.imageComplete();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
