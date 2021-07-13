import org.bzdev.p3d.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class SGBTest5 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, -10.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);

	sgb.addRectangles(10.0, 40.0, 80.0, 20.0, 10.0, -10.0);
	sgb.addRectangles(40.0, 10.0, 20.0, 80.0, 15.0, -15.0);
	sgb.addRectangle(35.0, 35.0, 30.0, 30.0, 20.0, true);
	sgb.addRectangle(35.0, 35.0, 30.0, 30.0, -30.0, false);

	sgb.setMaxZSpacing(1.0);

	sgb.create();

	if (m3d.notPrintable(System.out)) {
	    System.out.println("not printable");
	    System.exit(1);
	}

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
	image.write("png", "sgbtest5a.png");

	image = new Model3D.Image(WIDTH, HEIGHT,
				  BufferedImage.TYPE_INT_ARGB_PRE);
	image.setBacksideColor(Color.RED);
	image.setEdgeColor(Color.GREEN);
	g2d = image.createGraphics();
	g2d.setColor(Color.BLUE.darker());
	g2d.fillRect(0, 0, WIDTH, HEIGHT);
	g2d.dispose();
	image.setCoordRotation(Math.PI/3, Math.PI + Math.PI/2 - Math.PI/6, 0.0);

	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgbtest5b.png");

	System.exit(0);
    }
}
