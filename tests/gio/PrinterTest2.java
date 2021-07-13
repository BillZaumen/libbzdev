import org.bzdev.gio.PrinterGraphics;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class PrinterTest2 {
    static public void main(String argv[]) {	
	try {
	    PrinterGraphics pg = new PrinterGraphics(false);
	    System.out.println("portrait mode");
	    int width = pg.getWidth();
	    int height = pg.getHeight();
	    System.out.println("pg.getWidth() = " + pg.getWidth());
	    System.out.println("gp.getHeight() = " + pg.getHeight());
	    System.out.println("pg.getWidthAsDouble() = "
			       + pg.getWidthAsDouble());
	    System.out.println("gp.getasDoubleHeight() = "
			       + pg.getHeightAsDouble());
	    Graphics2D g2d = pg.createGraphics();
	    g2d.setColor(Color.BLACK);

	    g2d.drawRect(0, 0, width, 2);
	    g2d.fillRect(0, 0, width, 2);
	    g2d.drawRect(0, height-2, width, 2);
	    g2d.fillRect(0, height-2, width, 2);

	    g2d.drawRect(0, 2, 2 , height-2);
	    g2d.fillRect(0, 2, 2 , height-2);
	    g2d.drawRect(width-2, 2, 2 , height-2);
	    g2d.fillRect(width-2, 2, 2 , height-2);

	    g2d.drawRect(50, 50, 72*5, 3);
	    g2d.fillRect(50, 50, 72*5, 3);

	    pg.imageComplete();
	    
	    pg = new PrinterGraphics(true);
	    System.out.println("landscape mode");
	    width = pg.getWidth();
	    height = pg.getHeight();
	    System.out.println("pg.getWidth() = " + pg.getWidth());
	    System.out.println("gp.getHeight() = " + pg.getHeight());
	    System.out.println("pg.getWidthAsDouble() = "
			       + pg.getWidthAsDouble());
	    System.out.println("gp.getasDoubleHeight() = "
			       + pg.getHeightAsDouble());
	    g2d = pg.createGraphics();
	    g2d.setColor(Color.BLACK);

	    g2d.drawRect(0, 0, width, 2);
	    g2d.fillRect(0, 0, width, 2);
	    g2d.drawRect(0, height-2, width, 2);
	    g2d.fillRect(0, height-2, width, 2);

	    g2d.drawRect(0, 2, 2 , height-2);
	    g2d.fillRect(0, 2, 2 , height-2);
	    g2d.drawRect(width-2, 2, 2 , height-2);
	    g2d.fillRect(width-2, 2, 2 , height-2);

	    g2d.drawRect(50, 50, 72*5, 3);
	    g2d.fillRect(50, 50, 72*5, 3);

	    pg.imageComplete();


	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
