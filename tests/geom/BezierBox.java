import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.Arrays;
import org.bzdev.geom.*;
import org.bzdev.p3d.*;

public class BezierBox {

    static public void main(String argv[]) throws Exception {
	Point3D array[][] = new Point3D[6][4];

	double r = 100.0;

	for (int i = 0; i <  6; i++) {
	    double theta = Math.toRadians(i*60.0);
	    double x = r * Math.cos(theta);
	    double y = r * Math.sin(theta);
	    array[i][0] = new Point3D.Double(x, y, 0.0);
	    array[i][1] = new Point3D.Double(x, y, 25.0);
	    array[i][2] = new Point3D.Double(x, y, 75.0);
	    array[i][3] = new Point3D.Double(x, y, 100.0);
	}

	BezierGrid bg = new BezierGrid(array, true, false, true);
	bg.remove(4, 1);

	Path3D b1 = bg.getBoundary(0,0);
	Path3D b2 = bg.getBoundary(0, 3);
	Path3D b3 = bg.getBoundary(4, 1);
	BezierVertex bv1 = new BezierVertex(b1, 0.0);
	bv1.reverseOrientation(true);
	BezierVertex bv2 = new BezierVertex(b2, 0.0);
	bv2.reverseOrientation(true);
	BezierCap bc3 = new BezierCap(b3, 10.0, true);
	// BezierVertex bc3 = new BezierVertex(b3, 10.0);
	// bc3.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(bg);
	surface.append(bv1);
	surface.append(bv2);
	surface.append(bc3);

	Model3D m3d = new Model3D();
	m3d.append(surface);

	m3d.setTessellationLevel(3);


	Model3D.Image image = new
	    Model3D.Image(500, 400, BufferedImage.TYPE_INT_ARGB);
	// image.setBacksideColor(Color.RED);

	image.setEdgeColor(Color.GREEN);
	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, 500, 400);
	g2d.dispose();
	image.setCoordRotation(Math.toRadians(20.0),
			       Math.toRadians(60.0),
			       0.0);
	// image.setColorFactor(0.5);
	// image.setNormalFactor(0.5);
	m3d.setImageParameters(image, 50.0);
	m3d.render(image);
	image.write("png", "hexbox.png");

	System.out.println("bezier cap:");
	SurfaceIterator si = bc3.getSurfaceIterator(null);
	double[] coords = new double[48];
	while (!si.isDone()) {
	    switch (si.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		System.out.format("CUBIC_PATCH:\n");
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		System.out.format("CUBIC_TRIANGLE: "
				  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
				  coords[0], coords[1], coords[2],
				  coords[27], coords[28], coords[29],
				  coords[9], coords[10], coords[11]);
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		System.out.format("CUBIC_VERTEX:\n");
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		System.out.format("PLANR_TRIANGE:\n");
		break;
	    }
	    si.next();
	}
	System.out.println("... reversed: ");
	bc3.reverseOrientation(true);
	si = bc3.getSurfaceIterator(null);
	while (!si.isDone()) {
	    switch (si.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		System.out.format("CUBIC_PATCH:\n");
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		System.out.format("CUBIC_TRIANGLE: "
				  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
				  coords[0], coords[1], coords[2],
				  coords[27], coords[28], coords[29],
				  coords[9], coords[10], coords[11]);
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		System.out.format("CUBIC_VERTEX:\n");
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		System.out.format("PLANR_TRIANGE:\n");
		break;
	    }
	    si.next();
	}

	double[] buffer = new double[30];
	double pcoords[] = {
	    1.0, 2.0, 3.0,
	    4.0, 5.0, 6.0,
	    7.0, 8.0, 9.0,
	    10.0, 11.0, 12.0};
	    
	Surface3D.setupV0ForTriangle(pcoords, buffer, true);
	System.out.format("V0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[0], buffer[1], buffer[2],
			  buffer[12], buffer[13], buffer[14],
			  buffer[21], buffer[22], buffer[23],
			  buffer[27], buffer[28], buffer[29]);
	int cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	Arrays.fill(buffer, 0.0);
	Surface3D.setupV0ForTriangle(pcoords, buffer, false);
	System.out.format("V0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[0], buffer[1], buffer[2],
			  buffer[12], buffer[13], buffer[14],
			  buffer[21], buffer[22], buffer[23],
			  buffer[27], buffer[28], buffer[29]);
	cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	Arrays.fill(buffer, 0.0);
	Surface3D.setupU0ForTriangle(pcoords, buffer, false);
	System.out.format("U0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[0], buffer[1], buffer[2],
			  buffer[3], buffer[4], buffer[5],
			  buffer[6], buffer[7], buffer[8],
			  buffer[9], buffer[10], buffer[11]);
	cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	Arrays.fill(buffer, 0.0);
	Surface3D.setupU0ForTriangle(pcoords, buffer, true);
	System.out.format("U0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[0], buffer[1], buffer[2],
			  buffer[3], buffer[4], buffer[5],
			  buffer[6], buffer[7], buffer[8],
			  buffer[9], buffer[10], buffer[11]);
	cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	Arrays.fill(buffer, 0.0);
	Surface3D.setupW0ForTriangle(pcoords, buffer, false);
	System.out.format("W0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[27], buffer[28], buffer[29],
			  buffer[24], buffer[25], buffer[26],
			  buffer[18], buffer[19], buffer[20],
			  buffer[9], buffer[10], buffer[11]);
	cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	Arrays.fill(buffer, 0.0);
	Surface3D.setupW0ForTriangle(pcoords, buffer, true);
	System.out.format("W0: "
			  + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			  buffer[27], buffer[28], buffer[29],
			  buffer[24], buffer[25], buffer[26],
			  buffer[18], buffer[19], buffer[20],
			  buffer[9], buffer[10], buffer[11]);
	cnt = 0;
	for (double val: buffer) {
	    if (val == 0.0) cnt++;
	}
	System.out.println(".... zero cnt = " + cnt);
	System.exit(0);
    }
}
