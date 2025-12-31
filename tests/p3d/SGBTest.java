import org.bzdev.p3d.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

// Basic test.
public class SGBTest {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);
	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, -10.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);
	sgb.addRectangles(10.0, 50.0, 80.0, 40.0, 0.0, 0.0, true, false);
	sgb.removeRectangles(10.0, 10.0, 80.0, 30.0);
	sgb.create();

	int WIDTH=800;
	int HEIGHT = 600;
	Model3D.Image image =
	    new Model3D.Image(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
	image.setBacksideColor(Color.RED);
	image.setEdgeColor(Color.GREEN);
	Graphics2D g2d = image.createGraphics();
	g2d.setColor(Color.BLUE.darker());
	g2d.fillRect(0, 0, WIDTH, HEIGHT);
	g2d.dispose();
	image.setCoordRotation(Math.PI/3, Math.PI/2 - Math.PI/6, 0.0);

	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgbtest.png");
    }
}
