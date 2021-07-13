import org.bzdev.p3d.*;
import java.awt.*;
import java.awt.image.*;


public class SGTest2 {

    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);

	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};

	int[][] indices = {{0,3},
			    {1,3},{1,5},{1,6},
			    {2,2},{2,3},{2,4},{2,5},{2,6},{2,7},
			    {3,3},{3,4},{3,5},
			    {4,2},{4,3},{4,4},{4,5},{4,6},
			    {5,1},{5,2},{5,3},{5,4},{5,5},
			    {6,1},{6,3},{6,5},
			    {7,5}};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);

	for (int[] pair: indices) {
	    int i = pair[0];
	    int j = pair[1];
	    sg.addComponent(i, j, -(i+j)/20.0, (i+j)/20.0);
	}
	sg.addsCompleted();
	boolean error = false;

	if (m3d.notPrintable(System.out)) {
	    System.out.println("not printable");
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
	image.write("png", "sgtest2.png");

	System.exit(error? 1: 0);
    }
}
