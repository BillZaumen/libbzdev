import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.image.*;
import java.util.List;

public class SGTest12b {

    public static void main(String argv[]) throws Exception
    {
	Model3D m3d = new Model3D(true);
	m3d.setStackTraceMode(true);

	double z1 = 0.0;
	double z2 = 0.0;


	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 10.0, -10.0);

	for (int i = 0; i < xs.length-1; i++) {
	    for (int j = 0; j < ys.length-1; j++) {
		if ((i < 3 || i > 7 || j < 3 || j > 7)) {
		    sg.addComponent(i, j, z1, z2);
		}
	    }
	}
	sg.addHalfComponent(3,3, z1, -z2, false, false);
	sg.addHalfComponent(3,7, z1, -z2, false, false);
	sg.addHalfComponent(7,3, z1, -z2, false, false);
	sg.addHalfComponent(7,7, z1, -z2, false, false);
	sg.addsCompleted();

	boolean error = false;

	Path3D boundary = sg.getBoundary();
	if (boundary != null) {
	    System.out.println("boundary not null as expected"
			       + " (should be empty)");
	    Path3DInfo.printSegments(System.out, boundary);
	} else {
	    System.out.println("null boundary");
	    error = true;
	}

	if (!m3d.isClosedManifold()) {
	    System.out.println("manifold not closed (m3d)");
	    error = true;
	    List<Model3D.Edge> edges = m3d.verifyClosed2DManifold();
	    if (edges == null) {
		System.out.println("edges = null");
	    } else {
		P3d.printEdgeErrors(System.out,edges);
	    }
	}


	if (!sg.isClosedManifold()) {
	    System.out.println("manifold not closed (sg)");
	    error = true;
	}
	if (m3d.notPrintable(System.out)) {
	    System.out.println("model not printable");
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
	image.setCoordRotation(Math.PI/4, Math.PI/4-Math.PI/12, 0.0);
	// image.setCoordRotation(0.0, Math.PI/4-Math.PI/12, 0.0);

	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest12b.png");
	System.exit(error? 1: 0);
    }
}
