import org.bzdev.geom.SurfaceIterator;
import org.bzdev.p3d.*;
import org.bzdev.p3d.P3d.Rectangle;
import org.bzdev.math.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/*
 * TabTest was added because of a bug in which verifyEmbedded2DManifold
 * threw an ArrayIndexOutOfBoundsException.
 * The cause was an error in m3d.getMinZ() and related methods:
 * (1) a typo (minx instead of minz, etc.) and (2) not calling
 * computeBoundingBoxIfNeeded() to initialize minx, maxx, etc.
 * Note: this test includes a model error so that the manifold is
 * not closed.
 */
public class TabTest {

    static double h1 = 40.0;
    static double w1 = 7.5;
    static double dtab = 15.0;
    static double d1 = 10.0;
    static boolean print = false;

    static void addTab(Model3D m3d, double x, double y) {
	Rectangle.addFlippedV(m3d, x, y, 0.0, x, y+h1, dtab);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x, y, 0.0, x, y+h1, dtab);
	}
	Rectangle.addV(m3d, x+w1, y, 0.0, x, y+h1, dtab);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y, 0.0, x, y+h1, dtab);
	}

	Rectangle.addV(m3d, x+w1, y, dtab, x, y+h1, dtab + d1);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y, dtab, x, y+h1, dtab + d1);
	}

	Rectangle.addH(m3d, 0.0, x+w1, y, x, y+h1);
	if (print) {
	    System.out.format("rect %g %g %g %g %g \n",
			      0.0, x+w1, y, x, y+h1);
	}

	Rectangle.addFlippedH(m3d, dtab+d1, x+w1, y, x, y+h1);
	if (print) {
	    System.out.format("rect %g %g %g %g %g \n",
			      dtab+d1, x+w1, y, x, y+h1);
	}

	Rectangle.addFlippedV(m3d, x+w1, y, 0.0, x, y, dtab);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y, 0.0, x, y, dtab);
	}

	Rectangle.addFlippedV(m3d, x+w1, y, dtab, x, y, dtab + d1);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y, dtab, x, y, dtab + d1);
	}

	Rectangle.addV(m3d, x+w1, y+h1, 0.0, x, y+h1, dtab);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y+h1, 0.0, x, y + h1, dtab);
	}

	Rectangle.addV(m3d, x+w1, y+h1, dtab, x, y+h1, dtab + d1);
	if (print) {
	    System.out.format("rect %g %g %g %g %g %g\n",
			      x+w1, y+h1,dtab, x, y + h1, dtab+ d1);
	}
    }

    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D();

	double wireWidth = 120.0;

	double tab2x = wireWidth;
	double tab1y = 0.0;

	addTab(m3d, tab2x, tab1y);

	Model3D m3d2 = new Model3D();

	SurfaceIterator si = m3d.getSurfaceIterator(null);
	double[] coords = new double[9];
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    if (type != SurfaceIterator.PLANAR_TRIANGLE) {
		throw new Exception("bad type");
	    }
	    if (print) {
		System.out.format("triangle "
				  + "(%g, %g, %g)  (%g, %g, %g) (%g, %g, %g)\n",
				  coords[0], coords[1], coords[2],
				  coords[6], coords[7], coords[8],
				  coords[3], coords[4], coords[5]);
	    }
	    m3d2.addTriangle(coords[0], coords[1], coords[2],
			     coords[6], coords[7], coords[8],
			     coords[3], coords[4], coords[5],
			     null, si.currentTag());


	    si.next();
	}

	System.out.println("model size = " + m3d.size());
	System.out.println("minx = " + m3d.getMinX());
	System.out.println("miny = " + m3d.getMinY());
	System.out.println("minz = " + m3d.getMinZ());
	System.out.println("maxx = " + m3d.getMaxX());
	System.out.println("maxy = " + m3d.getMaxY());
	System.out.println("maxz = " + m3d.getMaxZ());

	System.out.println("For m3d2:");
	System.out.println("model size = " + m3d2.size());
	System.out.println("minx = " + m3d2.getMinX());
	System.out.println("miny = " + m3d2.getMinY());
	System.out.println("minz = " + m3d2.getMinZ());
	System.out.println("maxx = " + m3d2.getMaxX());
	System.out.println("maxy = " + m3d2.getMaxY());
	System.out.println("maxz = " + m3d2.getMaxZ());


	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.err.println("not an embedded manifold");
	    P3d.printTriangleErrors(System.out, tlist);
	    System.exit(1);
	}

	tlist = m3d2.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.err.println("not an embedded manifold");
	    P3d.printTriangleErrors(System.out, tlist);
	    System.exit(1);
	}
	System.exit(0);
    }
}