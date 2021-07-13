import org.bzdev.p3d.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;

public class OpsTest {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	// useful for debugging.
	m3d.setStackTraceMode(true);

	// Add a series of horizontal and vertical rectangles, each
	// of which will be represented by two triangles.  The methods
	// with the word Flipped in create the triangles with the
	// orientation reversed, which is useful when rectangles occur
	// in pairs as most of the arguments will be the same for the
	// two.

	P3d.Rectangle.addH(m3d, 30.0, 0.0, 0.0, 50.0, 50.0);
	P3d.Rectangle.addFlippedH(m3d, 0.0, 0.0, 0.0, 50.0, 50.0);

	P3d.Rectangle.addH(m3d, 50.0, 50.0, 0.0, 100.0, 50.0);
	P3d.Rectangle.addFlippedH(m3d, 0.0, 50.0, 0.0, 100.0, 50.0);
	P3d.Rectangle.addV(m3d, 50.0, 50.0, 30.0,  50.0, 0.0, 50.0);

	P3d.Rectangle.addFlippedV(m3d, 100.0, 50.0, 30.0,  100.0, 0.0, 50.0);

	P3d.Rectangle.addV(m3d, 0.0, 50.0, 0.0,  0.0, 0.0, 30.0);
	P3d.Rectangle.addFlippedV(m3d, 100.0, 50.0, 0.0,  100.0, 0.0, 30.0);

	P3d.Rectangle.addV(m3d, 0.0, 0.0, 0.0,  50.0, 0.0, 30.0);
	P3d.Rectangle.addV(m3d,50.0, 0.0, 0.0,  100.0, 0.0, 30.0);

	P3d.Rectangle.addFlippedV(m3d, 0.0, 50.0, 0.0,  50.0, 50.0, 30.0);
	P3d.Rectangle.addFlippedV(m3d, 50.0, 50.0, 0.0,  100.0, 50.0, 30.0);
	P3d.Rectangle.addV(m3d, 50.0, 0.0, 30.0,  100.0, 0.0, 50.0);
	P3d.Rectangle.addFlippedV(m3d, 50.0, 50.0, 30.0,  100.0, 50.0, 50.0);

	// test the model to make sure we can generate a valid
	// STL file.
	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.out.println("triangles intersect");
	    P3d.printTriangleErrors(System.out, tlist);
	}
	List<Model3D.Edge> elist = m3d.verifyClosed2DManifold();
	if (elist != null) {
	    System.out.println("not a closed manifold");
	    P3d.printEdgeErrors(System.out, elist);
	}

	System.out.println("Surface area = " + m3d.area());
	System.out.println("Volume = " + m3d.volume());

	// Now create  an STL file suitable for 3D printing

	m3d.writeSTL("OpsTest STL File", "opstest.stl");

	// Also create a picture of the object.

	int WIDTH = 700;
	int HEIGHT = 700;
	Model3D.Image image = new 
	    Model3D.Image (WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	// We want to show an outline of the triangles, so set the
	// edge color.

	image.setEdgeColor(Color.GREEN);

	// Standard java code for making the background a given color.

	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, WIDTH, HEIGHT);

	// rotate the image so that various surfaces will appear with
	// different shades of gray.

	image.setCoordRotation(-Math.PI/6.0, Math.PI/4.0, 0.0);

	// Scale the image so there is a bit of space around the object
	// at the edges of the frame (the 50.0 parameter is the number
	// of pixels to leave as a border.

	m3d.setImageParameters(image, 50.0);
	
	m3d.render(image);
	// ImageIO.write(image, "png", new File("opstest.png"));
	image.write("png",  new File("opstest.png"));
	System.exit(0);
   }
}