import org.bzdev.graphs.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageOrientation;


public class ImageTest {
    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph(700, 700);
	System.out.println("graph.getImageType() = " + graph.getImageType());
	graph = new Graph(700, 700, Graph.ImageType.INT_BGR);
	System.out.println("graph.getImageType() = " + graph.getImageType());

	graph = new Graph(700, 700);
	graph.setRanges(0.0, 350.0, 0.0, 350.0);
	Image img = ImageIO.read(new File("testimg.png"));
	Graphics2D g2d = graph.createGraphics();

        g2d.drawImage(img, new AffineTransform(), null);
	double scale = 350.0/700.0;
	graph.drawImage(g2d, img, 0.0, 0.0, RefPointName.LOWER_LEFT,
			0.0, scale, scale);

	graph.drawImage(g2d, img, 275.0, 275.0, RefPointName.LOWER_LEFT,
			0.0, scale/2, scale/2);
	graph.drawImage(g2d, img, 275.0, 275.0, RefPointName.LOWER_RIGHT,
			0.0, scale/2, scale/2);
	graph.drawImage(g2d, img, 275.0, 275.0, RefPointName.UPPER_LEFT,
			0.0, scale/2, scale/2);
	graph.drawImage(g2d, img, 275.0, 275.0, RefPointName.UPPER_RIGHT,
			0.0, scale/2, scale/2);


	graph.drawImage(g2d, img, 150.0, 150.0, RefPointName.LOWER_LEFT,
			0.0, scale/2, scale/2, false, false);
	graph.drawImage(g2d, img, 150.0, 150.0, RefPointName.LOWER_RIGHT,
			0.0, scale/2, scale/2, true, false );
	graph.drawImage(g2d, img, 150.0, 150.0, RefPointName.UPPER_LEFT,
			0.0, scale/2, scale/2, false, true);
	graph.drawImage(g2d, img, 150.0, 150.0, RefPointName.UPPER_RIGHT,
			0.0, scale/2, scale/2, true, true);


	graph.drawImage(g2d, img, 200.0, 0.0, RefPointName.LOWER_LEFT,
			0.0, 1.0, 1.0, false, false, false);

	graph.write("png", "testImage.png");

    }
}
