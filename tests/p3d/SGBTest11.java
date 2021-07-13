import org.bzdev.p3d.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.Graph;
import org.bzdev.anim2d.Animation2D;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;

public class SGBTest11 {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	// useful for debugging.
	m3d.setStackTraceMode(true);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, 0.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);
	sgb.addRectangles(10.0, 10.0, 80.0, 80.0, 10.0, 0.0);
	sgb.removeRectangles(20.0, 20.0, 60.0, 60.0);
	sgb.create();

	System.out.println("m3d.size() = " + m3d.size());
	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}

	int WIDTH = 700;
	int HEIGHT = 700;
	FileOutputStream os = new FileOutputStream(new File("sgbtest11.png"));
	OutputStreamGraphics osg =
	    OutputStreamGraphics.newInstance(os, WIDTH, HEIGHT, "png");

	// Model3D.Image image = new 
	//    Model3D.Image (WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	Model3D.Image image = new Model3D.Image(osg);
	
	// We want to show an outline of the triangles, so set the
	// edge color.

	image.setEdgeColor(Color.GREEN);
	// image.setBacksideColor(Color.RED);

	// Standard java code for making the background a given color.

	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, WIDTH, HEIGHT);

	// rotate the image so that various surfaces will appear with
	// different shades of gray.

	image.setCoordRotation(Math.PI/6.0, Math.PI/4.0, 0.0);

	// Scale the image so there is a bit of space around the object
	// at the edges of the frame (the 50.0 parameter is the number
	// of pixels to leave as a border.

	m3d.setImageParameters(image, 50.0);
	m3d.render(image);

	g2d.setColor(Color.WHITE);
	Font font = new Font(Font.SANS_SERIF,Font.BOLD, 24);
	g2d.setFont(font);

	String title = String.format("\u03c6 = %1.1f,  \u03b8 = %1.1f",
				     30.0, 45.0);

	int textwidth = (int) Math.round
	    (g2d.getFontMetrics(font).getStringBounds(title, g2d).getWidth());
	g2d.drawString(title, 350 - textwidth/2, 700-25);

	g2d.dispose();

	// ImageIO.write(image, "png", new File("lockpart.png"));
	image.write();
    }
}
