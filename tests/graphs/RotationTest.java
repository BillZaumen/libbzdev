import org.bzdev.graphs.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageOrientation;


public class RotationTest {

    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph();
	graph.setOffsets(75,75);
	graph.setRanges(0.0, 200.0, 0.0, 200.0);
	graph.setRotation(Math.PI/4.0, 100.0, 100.0);

	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(2.0F));

	Path2D path = new Path2D.Double();
	path.moveTo(100.0, 100.0);
	path.lineTo(150.0, 100.0);
	path.lineTo(150.0, 110.0);
	graph.draw(g2d, path);
	graph.write("png", "rotationTest.png");
    }
}
