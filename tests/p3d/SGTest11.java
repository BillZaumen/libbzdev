import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;

public class SGTest11 {

    public static void main(String argv[]) throws Exception
    {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);


	double xs[] = {0.0, 1.0, 2.0, 3.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);

	sg.addComponent(0, 1, 1.0, -1.0);
	sg.addComponent(1, 0, 1.0, -1.0);
	sg.addComponent(1, 1, 1.0, -1.0);
	sg.addComponent(1, 2, 1.0, -1.0);
	sg.addComponent(2, 1, 1.0, -1.0);

	sg.addHalfComponent(0, 0, 1.0, -1.0, true, false);
	sg.addHalfComponent(2, 0, 1.0, -1.0, true, false);
	sg.addHalfComponent(0, 2, 1.0, -1.0, true, false);
	sg.addHalfComponent(2, 2, 1.0, -1.0, true, false);

	sg.addsCompleted();

	Path3DInfo.printSegments(System.out, sg.getBoundary());

	boolean error = false;
	if (m3d.notPrintable()) {
	    System.out.println("not printable as expected");
	} else {
	    System.out.println("printable  (not expected)");
	    error = true;
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
	image.setCoordRotation(0.0, Math.PI/2 - Math.PI/12, 0.0);
	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest11a.png");
	System.exit(error? 1: 0);
    }
}
