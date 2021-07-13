import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.Color;
import java.io.FileOutputStream;

public class BGCycloidTest {
    public static void main(String argv[]) throws Exception {

	int N = 21;

	Point3D[][] array1 = new Point3D[N][N];
	Point3D[][] array2 = new Point3D[N-1][N-1];


	double r = 20.0;
	
	for (int i = 1; i < N-1; i++) {
	   for (int j = 1; j < N-1; j++) {

	       double thetaX = 2 * Math.PI * (i - 1) / (N-3);
	       double thetaY = 2 * Math.PI * (j - 1) / (N-3);

	       double x = r * (thetaX - Math.sin(thetaX));
	       double y = r * (thetaY - Math.sin(thetaY));
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       double z = r * (1 - Math.cos(thetaX)) * (1 - Math.cos(thetaY))
		   + 10.0;
	       array1[i][j] = new Point3D.Double(x, y, z);
	       array2[i-1][j-1] = new Point3D.Double(x, y, 0.0);
	   }
	}
	for (int i = 1 ; i < N-1 ; i++) {
	    array1[0][i] = new Point3D.Double(array1[1][i].getX(),
					     array1[1][i].getY(),
					     0.0);
	    array1[i][0] = new Point3D.Double(array1[i][1].getX(),
					     array1[i][1].getY(),
					     0.0);
	    array1[N-1][i] = new Point3D.Double(array1[N-2][i].getX(),
					     array1[N-2][i].getY(),
					     0.0);
	    array1[i][N-1] = new Point3D.Double(array1[i][N-2].getX(),
					     array1[i][N-2].getY(),
					     0.0);
	}

	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);

	for (int i = 1; i < N-1; i++) {
	    grid1.setRegion(0, i, 1);
	    grid1.setRegion(N-1, i, 1);
	    grid1.setRegion(i, 0, 1);
	    grid1.setRegion(i, N-1, 1);
	}

	grid1.setColor(Color.YELLOW);
	grid2.setColor(Color.GREEN);

	boolean griderrors = false;

	if (grid1.badSplines(System.out)) {
	    System.out.println("grid1 splines failed\n");
	    griderrors = true;
	}

	if (grid2.badSplines(System.out)) {
	    System.out.println("grid2 splines failed\n");
	    griderrors  = true;
	}
	if (griderrors) System.exit(1);

	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	Surface3D tsurface = new Surface3D.Double();
	tsurface.append(grid1.transpose().flip());
	tsurface.append(grid2.transpose().flip());

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	if (!tsurface.isWellFormed(System.out)) {
	    System.out.println("transposed surface not well formed");
	    System.exit(1);
	}

	Path3D boundary = surface.getBoundary();

	if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("boundary:");
	    Path3DInfo.printSegments(boundary);
	}

	boundary = tsurface.getBoundary();

	if (boundary.isEmpty()) {
	    System.out.println("tsurface boundary is empty as expected");
	} else {
	    System.out.println("tsurface boundary:");
	    Path3DInfo.printSegments(boundary);
	}

	System.out.println("now try splitting each patch into subpatches");


	double v = surface.volume();

	System.out.println("number of components for surface  = "
			   + surface.numberOfComponents());

	System.out.println("Volume for surface is " + v);

	v = tsurface.volume();

	System.out.println("number of components for tsurface  = "
			   + tsurface.numberOfComponents());

	System.out.println("Volume for tsurface is " + v);


	Rectangle3D bounds = surface.getBounds();
	System.out.println("bounding box for surface: " + bounds);

	bounds = tsurface.getBounds();
	System.out.println("bounding box for tsurface: " + bounds);

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	System.out.println("Volume in m3d is " + m3d.volume());

	m3d.createImageSequence(new FileOutputStream("cycloid.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
						     
	m3d = new Model3D();
	m3d.append(tsurface);
	m3d.setTessellationLevel(2);
	System.out.println("tsurface Volume in m3d is " + m3d.volume());

	m3d.createImageSequence(new FileOutputStream("tcycloid.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);


	System.exit(0);
    }
}
