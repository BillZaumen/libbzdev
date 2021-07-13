import org.bzdev.p3d.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.geom.AffineTransform;

import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;


public class MTest {
    public static void main(String argv[]) {
	try {
	    int WIDTH = 700;
	    int HEIGHT = 700;
	    Model3D.Image image = new 
		Model3D.Image (WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	    Model3D m3d = new Model3D();
	    Graphics2D g2d = image.createGraphics();

	    boolean testmode = argv.length == 0 || argv[0].equals("true");

	    if (testmode) {
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0, WIDTH, HEIGHT);
		g2d.setColor(Color.black);
		image.setOrigin(20.0);
		m3d.addTriangle(0.0, 0.0, 0.0,
				20.0, 0.0, 0.0,
				10.0, 20.0 * Math.cos(Math.PI/6.0), 0.0);
		m3d.addTriangle(0.0, 20.0, 0.0,
				20.0, 20.0, 0.0,
				10.0,  25.0, 0.0);
		m3d.addTriangle(20.0, 30.0, 0.0,
				10.0, 35.0, 0.0,
				0.0, 30.0, 0.0);
		m3d.addTriangle(10.0,  45.0, 0.0,
				0.0, 40.0, 0.0,
				20.0, 40.0, 0.0);

		m3d.addTriangle(40.0, 0.0, 0.0,
				60.0, 0.0, 0.0,
				40.0, 5.0, 0.0);
		m3d.addTriangle(40.0, 10.0, 0.0,
				60.0, 15.0, 0.0,
				40.0, 15.0, 0.0);
		m3d.addTriangle(60.0, 20.0, 0.0,
				40.0, 25.0, 0.0,
				40.0, 20.0, 0.0);
		m3d.addTriangle(60.0, 35.0, 0.0,
				40.0, 35.0, 0.0,
				40.0, 30.0, 0.0);
		m3d.addTriangle(40.0, 45.0, 0.0,
				40.0, 40.0, 0.0,
				60.0, 40.0, 0.0);
		m3d.addTriangle(40.0, 55.0, 0.0,
				40.0, 50.0, 0.0,
				60.0, 55.0, 0.0);

		image.setScaleFactor(10.0);
		
	    } else {
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0,0, WIDTH, HEIGHT);
		g2d.setColor(Color.black);
		image.setOrigin((float)WIDTH/2, (float)HEIGHT/2);
		m3d.addTriangle(0.0, 0.0, 0.0,
				100.0, 0.0, 0.0,
				0.0, 50.0, 0.0,
				null, "tag1");
		m3d.addTriangle(0.0, 50.0, 0.0,
				100.0, 50.0, 0.0,
				0.0, 100.0, 0.0,
				null, "tag1a");
		m3d.addTriangle(0.0, 50.0, 0.0,
				100.0, 0.0, 0.0,
				100.0, 50.0, 0.0,
				null, "tag2");
		m3d.addTriangle(0.0, 100, 0.0,
				100.0, 50.0, 0.0,
				100.0, 100.0, 0.0,
				null, "tag2a");

		m3d.addTriangle(61.0 + 40.0, 50.0, 0.0,
				61.0 + 60.0, 50.0, 0.0,
				61.0 + 60.0, 60.0, 10.0,
				null, "tag3");
		m3d.addTriangle(61.0 + 40.0, 50.0, 0.0,
				61.0 + 60.0, 60.0, 10.0,
				61.0 + 40.0, 60.0, 10.0,
				null, "tag4");
		m3d.addTriangle(61.0 + 40.0, 50.0, 0.0,
				61.0 + 60.0, 40.0, -10.0,
				61.0 + 60.0, 50.0, 0.0,
				null, "tag3a");
		m3d.addTriangle(61.0 + 40.0, 50.0, 0.0,
				61.0 + 40.0, 40.0, -10.0,
				61.0 + 60.0, 40.0, -10.0,
				null, "tag4a");
		m3d.addLineSegment(40.0, 50.0, 0.0,
				   60.0, 50.0, 0.0, null, "tag5");
	    }

	    image.setDefaultSegmentColor(Color.red);
	    image.setEdgeColor(Color.green);


	    //m3d.setTranslation(50.0, 50.0, 50.0);

	    image.setRotationOrigin(50.0, 50.0, 0.0, false);
	    // m3d.setCoordRotation(Math.PI/4.0, 0.0, 0.0);
	    // image.setCoordRotation(0.0, Math.PI/4.0, 0.0);
	    image.setDelta(10.0);

	    m3d.render(image, true);
	    // ImageIO.write(image, "png", new File("MTest-" + testmode + ".png"));
	    image.write("png", new File("MTest-" + testmode + ".png"));
	    g2d.setColor(Color.WHITE);
	    g2d.fillRect(0,0, WIDTH, HEIGHT);
	    g2d.setColor(Color.black);
	    m3d.render(image, true, -5.0, 10.0);
	    // ImageIO.write(image, "png", new File("MTest1-" + testmode +".png"));
	    image.write("png",  new File("MTest1-" + testmode +".png"));
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
