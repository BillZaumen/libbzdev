import org.bzdev.p3d.*;
import java.awt.*;
import java.awt.image.*;


public class SGBTest8 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, -10.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 20.0, 0.0, 0.0);
	sgb.addRectangles(0.0, 80.0, 100.0, 20.0, 0.0, 0.0);
	sgb.addRectangles(0.0, 0.0, 20.0, 100.0, 0.0, 0.0);
	sgb.addRectangles(80.0, 0.0, 20.0, 100.0, 0.0, 0.0);

	sgb.addX(30.0);
	sgb.addX(70.0);
	sgb.addY(30.0);
	sgb.addY(70.0);

	sgb.addX(10.0);
	sgb.addX(90.0);
	sgb.addY(10.0);
	sgb.addY(90.0);

	sgb.addX(5.0);
	sgb.addX(95.0);
	sgb.addY(5.0);
	sgb.addY(95.0);

	sgb.addX(25.0);
	sgb.addX(75.0);
	sgb.addY(25.0);
	sgb.addY(75.0);


	sgb.addHalfRectangles(0.0, 0.0, 2,
			      SteppedGrid.Builder.Corner.UPPER_RIGHT,
			      5.0, 5.0);
	sgb.addHalfRectangles(90.0, 0.0, 2,
			      SteppedGrid.Builder.Corner.UPPER_LEFT,
			      5.0, 5.0);
	sgb.addHalfRectangles(90.0, 90.0, 2,
			      SteppedGrid.Builder.Corner.LOWER_LEFT,
			      5.0, 5.0);
	sgb.addHalfRectangles(0.0, 90.0, 2,
			      SteppedGrid.Builder.Corner.LOWER_RIGHT,
			      5.0, 5.0);

	sgb.addHalfRectangles(20.0, 20.0, 2,
			      SteppedGrid.Builder.Corner.LOWER_LEFT,
			      5.0, 5.0);
	sgb.addHalfRectangles(70.0, 20.0, 2,
			      SteppedGrid.Builder.Corner.LOWER_RIGHT,
			      5.0, 5.0);
	sgb.addHalfRectangles(70.0, 70.0, 2,
			      SteppedGrid.Builder.Corner.UPPER_RIGHT,
			      5.0, 5.0);
	sgb.addHalfRectangles(20.0, 70.0, 2,
			      SteppedGrid.Builder.Corner.UPPER_LEFT,
			      5.0, 5.0);


	sgb.create(m3d);

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
	image.write("png", "sgbtest8a.png");

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
	image.write("png", "sgbtest8b.png");

	System.exit(0);
    }
}
