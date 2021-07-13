import java.awt.geom.Path2D;
import java.io.*;
import org.bzdev.p3d.*;
import org.bzdev.geom.*;

public class DoubleConeTest {
    public static void main(String argv[]) throws Exception {
	Path2D circle = Paths2D.createArc(51.0, 51.0, 51.0, 1.0,
					  2*Math.PI, Math.PI/2);
	circle.closePath();

	System.out.println("created 2D circle");

	Path3D circle3 = new Path3D.Double(circle, (nn, p, t, bbb) -> {
		return p;}, 0);

	PathIterator3D pi4 = circle3.getPathIterator(null);
	
	Surface3D surface = new Surface3D.Double();
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double startx = 0.0, starty = 0.0, startz = 0.0;
	double[] coords = new double[48];
	System.out.println("creating surface");
	while (!pi4.isDone()) {
	    switch(pi4.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		startx = lastx;
		starty = lasty;
		startz = lastz;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		surface.addCubicVertex(lastx, lasty, lastz,
				       PathIterator3D.SEG_CUBICTO,
				       coords, 51.0, 51.0, 50.0);
		surface.addFlippedCubicVertex(lastx, lasty, lastz,
					      PathIterator3D.SEG_CUBICTO,
					      coords, 51.0, 51.0, -50.0);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		throw new Exception("unexpected case in switch");
	    }
	    pi4.next();
	}
	
	Model3D m3d = new Model3D();
	m3d.append(surface);

	Path3D boundary = m3d.getBoundary();
	if (!boundary.isEmpty()) {
	    System.out.println("boundary should be empty");
	}

	if (m3d.notPrintable(System.out)) {
	    System.out.println("not printable");
	    System.exit(1);
	}

	m3d.setTessellationLevel(3);

	m3d.createImageSequence(new FileOutputStream("dcone.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);


	BezierVertex bv1 = new BezierVertex(circle3, 50.0);
	BezierVertex bv2 = new BezierVertex(circle3, -50.0);
	bv2.flip();

	m3d = new Model3D();

	m3d.append(bv1);
	m3d.append(bv2);

	m3d.setTessellationLevel(6);

	m3d.createImageSequence(new FileOutputStream("dcone2.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
    }
}
