import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;


public class SGTest5 {

    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);


	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);


	for (int i = 1; i < 6; i++) {
	    for (int j = 1; j < 6; j++) {
		if (i == 3 && j == 3) {
		    sg.addComponent(i, j, 1.0, -1.0, true, true);
		} else {
		    sg.addComponent(i,j, 0.0, 0.0);
		}
	    }
	}

	sg.addComponent(4, 7, 1.0, -1.0);
	sg.addComponent(7, 4, 1.0, -1.0);

	sg.addsCompleted();


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
	image.setCoordRotation(Math.PI/4, Math.PI/4, 0.0);
	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest5.png");

	Path3D boundary = sg.getBoundary();
	if (boundary == null) {
	    System.out.println("boundary was null");
	} else {
	    System.out.println("boundary:");
	    Path3DInfo.printSegments(System.out, boundary);
	}
	/*
 	System.out.println("upper:");
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		if (i < 8 && j < 8) {
		    System.out.format("(%d, %d): filled = %b, "
				      + "placeholder = %b, "
				      + "external-vertex = %b\n",
				      i, j, sg.isComponentFilled(i, j, true),
				      sg.isComponentPlaceholder(i, j, true),
				      sg.isExternalVertex(i, j, true));
		} else {
		    System.out.format("(%d, %d): filled = %b, "
				      + "placeholder = %b, "
				      + "external-vertex = %b\n",
				      i, j, false, false,
				      sg.isExternalVertex(i, j, true));
		}
	    }
	}

	System.out.println("lower:");
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		if (i < 8 && j < 8) {
		    System.out.format("(%d, %d): filled = %b, "
				      + "placeholder = %b, "
				      + "external-vertex = %b\n",
				      i, j, sg.isComponentFilled(i, j, false),
				      sg.isComponentPlaceholder(i, j, false),
				      sg.isExternalVertex(i, j, false));
		} else {
		    System.out.format("(%d, %d): filled = %b, "
				      + "placeholder = %b, "
				      + "external-vertex = %b\n",
				      i, j, false, false,
				      sg.isExternalVertex(i, j, false));
		}
	    }
	}
	*/
    }
}
