import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.image.*;
import java.util.List;

public class SGTest12c {

    public static void main(String argv[]) throws Exception
    {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);

	IntegerRandomVariable iz = new UniformIntegerRV( 0, true, 5, true);
	double z1, z2;

	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 10.0, -10.0);

	for (int i = 0; i < xs.length-1; i++) {
	    for (int j = 0; j < ys.length-1; j++) {
		z1 = iz.next();
		z2 = -iz.next();
		if ((i < 3 || i > 7 || j < 3 || j > 7)) {
		    sg.addComponent(i, j, z1, z2);
		}
		if (i > 3 && i < 7 && j > 3 && j < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
		if ((i == 3 || i == 7) && j > 3 && j < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
		if ((j == 3 || j == 7) && i > 3 && i < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
	    }
	}
	z1 = iz.next();
	z2 = -iz.next();
	sg.addHalfComponent(3,3, z1, -z2, true, false);
	z1 = iz.next();
	z2 = -iz.next();
	sg.addHalfComponent(3,7, z1, -z2, true, false);
	z1 = iz.next();
	z2 = -iz.next();
	sg.addHalfComponent(7,3, z1, -z2, true, false);
	z1 = iz.next();
	z2 = -iz.next();
	sg.addHalfComponent(7,7, z1, -z2, true, false);
	sg.addsCompleted();

	boolean error = false;


	Path3D boundary = sg.getBoundary();
	if (boundary != null) {
	    PathIterator3D pit = boundary.getPathIterator(null);
	    double lastx = 0.0;
	    double lasty = 0.0;
	    double lastz = 0.0;
	    double firstx = 0.0;
	    double firsty = 0.0;
	    double firstz = 0.0;
	    double[] fcoords = new double[3];
	    double[] coords = new double[3];
	    double[] vertex = {5.0, 5.0, 16.0};

	    if (!pit.isDone()) {
		if (pit.currentSegment(fcoords) == PathIterator3D.SEG_MOVETO) {
		    lastx = fcoords[0];
		    lasty = fcoords[1];
		    lastz = fcoords[2];
		    firstx = lastx;
		    firsty = lasty;
		    firstz = lastz;
		} else {
		    System.out.println("segment type not expected");
		    error = true;
		} 
		pit.next();
	    }
	    double x, y, z;
	    boolean firsttime = true;
	    boolean counterclockwise = true;
	    int ind = 0;
	    while (!pit.isDone()) {
		ind++;
		switch(pit.currentSegment(coords)) {
		case PathIterator3D.SEG_LINETO:
		    if (firsttime) {
			double[] v1 = VectorOps.sub(fcoords, vertex);
			double[] v2 = VectorOps.sub(coords, vertex);
			double[] n = VectorOps.crossProduct(v1, v2);
			counterclockwise = n[2] > 0.0;
			firsttime = false;
		    }
		    x = coords[0];
		    y = coords[1];
		    z = coords[2];
		    if (counterclockwise) {
			m3d.addTriangle(5.0, 5.0, 16.0,
					lastx, lasty, lastz,
					x, y, z);
		    } else {
			m3d.addFlippedTriangle(5.0, 5.0, 16.0,
					       lastx, lasty, lastz,
					       x, y, z);
		    }
		    lastx = x;
		    lasty = y;
		    lastz = z;
		    break;
		case PathIterator3D.SEG_CLOSE:
		    break;
		default:
		    System.out.println("segment type not expected");
		    error = true;
		}
		pit.next();
	    }
	} else {
	    System.out.println("null boundary not expected");
	    error = true;
	}

	if (sg.isClosedManifold()) {
	    System.out.println("closed manifold not expected");
	    error = true;
	}

	if (m3d.printable(System.out)) {
	    System.out.println("printable as expected");
	} else {
	    System.out.println("model not printable");
	    error = true;
	}

	int WIDTH=800;
	int HEIGHT = 600;
	Model3D.Image image =
	    new Model3D.Image(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
	image.setBacksideColor(Color.RED);
	image.setEdgeColor(Color.GREEN);
	image.setColorFactor(1.0);
	Graphics2D g2d = image.createGraphics();
	g2d.setColor(Color.BLUE.darker());
	g2d.fillRect(0, 0, WIDTH, HEIGHT);
	g2d.dispose();
	image.setCoordRotation(3 * Math.PI/4, Math.PI/6, 0.0);
	// image.setCoordRotation(Math.PI/4, Math.PI/4 - Math.PI/12, 0.0);
	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest12c.png");

	System.exit(error? 1: 0);
    }
}
