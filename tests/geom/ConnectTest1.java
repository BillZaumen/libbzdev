import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

public class ConnectTest1 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 4;

	Point3D[][] array = new Point3D[N][N];

	for (int i = 0; i < N; i++) {
	   for (int j = 0; j < N; j++) {
	       double x = 10*i;
	       double y = 10*j;
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       double z = 10.0;
	       array[i][j] = new Point3D.Double(x, y, z);
	   }
	}
	BezierGrid grid1 = new BezierGrid(array);
	System.out.println("grid1:");
	grid1.print();

	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		return new Point3D.Double(p.getX(), p.getY(), 0.0);
	    });

	grid2.reverseOrientation(true);

	Path3D boundary = grid1.getBoundary();

	// Test that we can find the boundary with a specified starting
	// point when that starting point matches the starting point
	// returned by grid1.getBoundary().
	PathIterator3D pit = boundary.getPathIterator(null);
	double[] bcoords = new double[9];
	if (!pit.isDone()) {
	    pit.currentSegment(bcoords);
	    int i = 0, j = 0;
	    boolean foundit = false;
	    for(i = 0; i < N; i++) {
		for (j = 0; j < N; j++) {
		    if ((double)(float)array[i][j].getX() == bcoords[0]
			&& (double)(float)array[i][j].getY() == bcoords[1]
			&& (double)(float)array[i][j].getZ() == bcoords[2]) {
			foundit = true;
			break;
		    }
		}
		if (foundit) break;
	    }
	    if (foundit) {
		System.out.format
		    ("comparing getBoundary() to getBoundary(%d, %d)\n", i, j);
		Path3D boundary2 = grid1.getBoundary(i, j);
		System.out.println("boundary:");
		Path3DInfo.printSegments(boundary);
		System.out.println("boundary2:");
		Path3DInfo.printSegments(boundary2);
		PathIterator3D pit2 = boundary2.getPathIterator(null);
		double[] bcoords2 = new double[9];
		int count = 0;
		while (!pit.isDone() && ! pit2.isDone()) {
		    int type = pit.currentSegment(bcoords);
		    int type2 = pit2.currentSegment(bcoords2);
		    if (type != type2) throw new Exception("type at " + count);
		    switch(type) {
		    case PathIterator3D.SEG_MOVETO:
		    case PathIterator3D.SEG_LINETO:
			for (int k = 0; k < 3; k++) {
			    if (bcoords[k] != bcoords2[k]) {
				throw new Exception("bcoords !+ bcoords2");
			    }
			}
			break;
		    case PathIterator3D.SEG_QUADTO:
			for (int k = 0; k < 6; k++) {
			    if (bcoords[k] != bcoords2[k]) {
				throw new Exception("bcoords !+ bcoords2");
			    }
			}
			break;
		    case PathIterator3D.SEG_CUBICTO:
			for (int k = 0; k < 9; k++) {
			    if (bcoords[k] != bcoords2[k]) {
				throw new Exception("bcoords !+ bcoords2");
			    }
			}
			break;
		    case PathIterator3D.SEG_CLOSE:
			break;
		    }
		    pit.next();
		    pit2.next();
		    count++;
		}
		if (pit.isDone() != pit2.isDone()) {
		    throw new Exception("boundary length mismatch");
		}
	    } else {
		throw new Exception("could not find boundary point");
	    }
	}


	System.out.println("creating connections ...");
	BezierGrid[] connections = grid1.createConnectionsTo(grid2, 2, split);
	System.out.println("... connections created");

	BezierGrid[] tconnections = grid1.createConnectionsTo(grid2, 2, false,
							     0, 0);
	System.out.println("tconnections.length = " + tconnections.length);
	System.out.println("grid tconnections[0]:");
	tconnections[0].print();

	Path3D boundary4t = grid1.getBoundary(0,0);
	int jcnt = 0;
	PathIterator3D pi4t = boundary4t.getPathIterator(null);
	double[] bcoords4t = new double[9];
	while (!pi4t.isDone()) {
	    Point3D pt = (jcnt < tconnections[0].getVArrayLength())?
		tconnections[0].getPoint(0,jcnt++): null;
	    switch (pi4t.currentSegment(bcoords4t)) {
	    case PathIterator3D.SEG_MOVETO:
	    case PathIterator3D.SEG_LINETO:
		if (pt.getX() != bcoords4t[0]
		    || pt.getY() != bcoords4t[1]
		    || pt.getZ() != bcoords4t[2]) {
		    throw new Exception("pt: cnt = " + (jcnt-1));
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (pt.getX() != bcoords4t[3]
		    || pt.getY() != bcoords4t[4]
		    || pt.getZ() != bcoords4t[5]) {
		    throw new Exception("pt");
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (pt.getX() != bcoords4t[6]
		    || pt.getY() != bcoords4t[7]
		    || pt.getZ() != bcoords4t[8]) {
		    System.out.format("(%s, %s, %s) != (%s, %s, %s)\n",
				      pt.getX(), pt.getY(), pt.getZ(),
				      bcoords4t[6], bcoords4t[7], bcoords4t[8]);
		    throw new Exception("pt: cnt = " + (jcnt-1));
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    }
	    pi4t.next();
	}
	
	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	int k = 0;
	double[] coords = new double[12];
	for (BezierGrid g: connections) {
	     System.out.println("appending connecting grid:");
	     g.print();
	     // Path3DInfo.printSegments(g.getBoundary());
	    surface.append(g);
	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("no boundary because not well formed");
	    System.exit(1);
	} else if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("surface boundary not empty:");
	    Path3DInfo.printSegments(boundary);
	    System.exit(1);
	}

	if (surface.numberOfComponents() != 1) {
	    System.out.println("number of components for surface  = "
			       + surface.numberOfComponents());
	    System.exit(1);
	}

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    System.exit(1);
	}
	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("connection1.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, true);
	}
	System.exit(0);
    }
}
