import org.bzdev.graphs.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageOrientation;

public class BBTest {

    private static void createGraph(Graph graph, boolean rotate)
	throws Exception
    {
	    graph.setOffsets(75,75);
	    graph.setRanges(0.0, 500.0, 0.0, 500.0);
	    double xscale1 = graph.getXScale();
	    double yscale1 = graph.getYScale();
	    graph.setRanges(0.0, 0.0, 0.0, 0.0, xscale1, yscale1);
	    double angle = 0;
	    if (rotate) {
		angle = -Math.PI/8.0;
		graph.setRotation(angle, 250.0, 250.0);
		angle = -angle;
	    }

	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(4.0F));
	    
	    Image img = ImageIO.read(new File("testimg.png"));


	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.UPPER_LEFT,
			    angle+0.0, 0.25, 0.25);
	    Rectangle2D bbox =
		graph.imageBoundingBox(img, 150.0, 500.0,
				       RefPointName.UPPER_LEFT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);
	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.LOWER_LEFT,
			    angle+0.0, 0.25, 0.25);

	    bbox = graph.imageBoundingBox(img, 150.0, 500.0,
				       RefPointName.LOWER_LEFT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.UPPER_RIGHT,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 150.0, 500.0,
				       RefPointName.UPPER_RIGHT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 150.0, 500.0, RefPointName.LOWER_RIGHT,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 150.0, 500.0,
				       RefPointName.LOWER_RIGHT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 150.0, 400.0, RefPointName.CENTER_LEFT,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 150.0, 400.0,
				       RefPointName.CENTER_LEFT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 150.0, 400.0, RefPointName.CENTER_RIGHT,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 150.0, 400.0,
				       RefPointName.CENTER_RIGHT,
				       angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 350.0, 100.0, RefPointName.UPPER_CENTER,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 350.0, 100.0,
					  RefPointName.UPPER_CENTER,
					  angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 350.0, 100.0, RefPointName.LOWER_CENTER,
			    angle+0.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 350.0, 100.0,
					  RefPointName.LOWER_CENTER,
					  angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 350.0, 200.0, RefPointName.CENTER,
			    angle+0.0, 0.25, 0.125);
	    bbox = graph.imageBoundingBox(img, 350.0, 200.0,
					  RefPointName.CENTER,
					  angle+0.0, 0.25, 0.125, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 300.0, 450.0, RefPointName.CENTER,
			    angle+Math.PI/4.0, 0.25, 0.25);
	    bbox = graph.imageBoundingBox(img, 300.0, 450.0,
					  RefPointName.CENTER,
					  angle+Math.PI/4.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    angle+0.0, 0.25, 0.125);
	    bbox = graph.imageBoundingBox(img, 370.0, 250.0,
					  RefPointName.LOWER_LEFT,
					  angle+0.0, 0.25, 0.125, true);
	    graph.draw(g2d, bbox);


	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    angle+Math.PI/2.0, 0.25, 0.125);
	    bbox = graph.imageBoundingBox(img, 370.0, 250.0,
					  RefPointName.LOWER_LEFT,
					  angle+Math.PI/2.0, 0.25, 0.125, true);
	    graph.draw(g2d, bbox);
	    graph.drawImage(g2d, img, 370.0, 250.0, RefPointName.LOWER_LEFT,
			    angle+Math.PI, 0.25, 0.125);
	    bbox = graph.imageBoundingBox(img, 370.0, 250.0,
					  RefPointName.LOWER_LEFT,
					  angle+Math.PI, 0.25, 0.125, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 200.0, 100.0, RefPointName.CENTER,
			    angle+0.0, 0.25, 0.25, false, true);
	    bbox = graph.imageBoundingBox(img, 200, 100.0,
					  RefPointName.CENTER,
					  angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);

	    graph.drawImage(g2d, img, 50.0, 100.0, RefPointName.CENTER,
			    angle+0.0, 0.25, 0.25, true, false);
	    bbox = graph.imageBoundingBox(img, 50.0, 100.0,
					  RefPointName.CENTER,
					  angle+0.0, 0.25, 0.25, true);
	    graph.draw(g2d, bbox);
	    g2d.dispose();
    } 

    public static void main(String argv[]) throws Exception {
	try { 
	    double angle = Math.PI/16.0;
	    Graph graph = new Graph();
	    System.out.println("creating testBBox.png");
	    createGraph(graph, false);
	    System.out.format("%g -> %g\n", angle,
			      graph.getUserSpaceAngle(angle, true));
	    System.out.format("%g -> %g\n", 0.0,
			      graph.getUserSpaceAngle(0.0, true));
	    graph.write("png", "testBBox.png");

	    graph = new Graph();
	    System.out.println("creating testBBoxRot.png");
	    createGraph(graph, true);
	    System.out.format("%g -> %g\n", angle,
			      graph.getUserSpaceAngle(angle, true));
	    System.out.format("%g -> %g\n", 0.0,
			      graph.getUserSpaceAngle(0.0, true));
	    graph.write("png", "testBBoxRot.png");

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
