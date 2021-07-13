import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.image.*;

public class SGTest12 {

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



	Path3D boundary = sg.getBoundary();
	if (boundary != null) {
	    Path3DInfo.printSegments(System.out, boundary);
	} else {
	    System.out.println("null boundary");
	}
	boolean error = false;
	if (sg.isClosedManifold()) {
	    System.out.println("closed manifold not expected");
	    error = true;
	}

	if (m3d.printable()) {
	    System.out.println("not-printable output expected");
	    error = true;
	} else {
	    System.out.println("model not printable as expected");
	}

	SurfaceIterator si = m3d.getSurfaceIterator(null);
	int errcount = 0;
	while (!si.isDone()) {
	    double[] coords = new double[48];
	    int type = si.currentSegment(coords);
	    if ((Math.abs(coords[2]-coords[5]) < 1.e-10)
		&& (Math.abs(coords[5] - coords[8]) < 1.e-10)) {
		si.next();
		continue;
	    }
	    if ((coords[2] < 0.0) && (coords[5] < 0.0) && (coords[8] < 0.0)) {
		si.next();
		continue;
	    }
	    if (((coords[0] > 3.0) && (coords[0] < 7.0)) &&
		((coords[1] > 3.0) && (coords[1]) < 7.0)) {
		System.out.format("(%g,%g,%g)->(%g,%g,%g)->(%g,%g,%g)\n",
				  coords[0], coords[1], coords[2],
				  coords[6], coords[7], coords[8],
				  coords[3], coords[4], coords[5]);
		errcount++;
	    }
	    si.next();
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
	/*
	image.setCoordRotation(Math.PI/4-Math.PI/12,
			       Math.PI/2 - Math.PI/12,
			       0.0);
	*/
	m3d.setImageParameters(image);

	m3d.render(image);
	image.write("png", "sgtest12.png");

	if (errcount > 0) System.exit(1);
	System.exit(error? 1: 0);
    }
}
