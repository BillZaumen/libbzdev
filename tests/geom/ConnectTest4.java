import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.Color;
import java.io.FileOutputStream;

public class ConnectTest4 {
    public static void main(String argv[]) throws Exception {

	boolean split  = (argv.length > 0 && argv[0].equals("--split"));
	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));

	double r = 50.0;

	// quick test to check boundary.
	int NN = 5;
	int MM = 8;
	Point3D[][] sarray = new Point3D[NN][MM];
	for (int i = 0; i < NN; i++) {
	    double z = (NN-i)*10.0;
	    for (int j = 0; j < MM; j++) {
		double theta = Math.toRadians(j*45);
		double x = r * Math.cos(theta);
		double y = r * Math.sin(theta);
		if (Math.abs(x) < 1.e-10) x = 0;
		if (Math.abs(y) < 1.e-10) y = 0;
		sarray[i][j] = new Point3D.Double(x, y, z);
	    }
	}

	BezierGrid sgrid = new BezierGrid(sarray, false, true);
	sgrid.remove(1, MM-1, 2, 1);
	sgrid.remove(1, 0, 2, 1);
	sgrid.print();
	Path3DInfo.printSegments(sgrid.getBoundary());
	System.out.println("---------");

	int N = 11;
	int M = 36;

	Point3D[][] array = new Point3D[N][M];


	for (int i = 0; i < N; i++) {
	    double z = (N-i)*10.0;
	    for (int j = 0; j < M; j++) {
		double theta = Math.toRadians(j*10);
		double x = r * Math.cos(theta);
		double y = r * Math.sin(theta);
		if (Math.abs(x) < 1.e-10) x = 0;
		if (Math.abs(y) < 1.e-10) y = 0;
		array[i][j] = new Point3D.Double(x, y, z);
	    }
	}
	BezierGrid grid1 = new BezierGrid(array, false, true);
	grid1.remove(5, M-1, 2, 1);
	grid1.remove(5, 0, 2, 1);

	// print a set of images just containing grid1.
	Surface3D grid1Surface = new Surface3D.Double();
	grid1Surface.append(grid1);
	Model3D grid1Model = new Model3D();
	grid1Model.append(grid1Surface);
	grid1Model.setTessellationLevel(2);
	grid1Model.createImageSequence(new FileOutputStream("grid1Model.isq"),
				       "png",
				       8, 6,
				       0.0, 0.0, 0.0, false);

	// check boundary methods.

	Path3D grid1Boundary = grid1.getBoundary();
	Path3D grid1Boundary1 = grid1.getBoundary(0, 0);
	Path3D grid1Boundary2 = grid1.getBoundary(5, 0);
	Path3D grid1Boundary3 = grid1.getBoundary(N-1, 10);

	System.out.println("grid1 boundary 2:");
	Path3DInfo.printSegments(grid1Boundary2);

	PathIterator3D pit = grid1Boundary.getPathIterator(null);
	PathIterator3D pit1 = grid1Boundary1.getPathIterator(null);
	PathIterator3D pit2 = grid1Boundary2.getPathIterator(null);
	PathIterator3D pit3 = grid1Boundary3.getPathIterator(null);
	double[] coords = new double[9];
	double[] coords1 = new double[9];
	double[] coords2 = new double[9];
	double[] coords3 = new double[9];
	double[] pcoords = new double[9];
	if (pit.currentSegment(coords) != PathIterator3D.SEG_MOVETO) {
	    throw new Exception("bad pit");
	}
	int type1 = pit1.currentSegment(coords1);
	if (type1 != PathIterator3D.SEG_MOVETO) {
	    throw new Exception("bad pit");
	}
	int type2 = pit2.currentSegment(coords2);
	if (type2 != PathIterator3D.SEG_MOVETO) {
	    throw new Exception("bad pit");
	}
	int type3 = pit3.currentSegment(coords3);
	if (type3 != PathIterator3D.SEG_MOVETO) {
	    throw new Exception("bad pit");
	}
	PathIterator3D cpit = null;
	int ptype = -1;

	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		if (cpit != null) {
		    throw new Exception();
		}
		if (type1 == PathIterator3D.SEG_MOVETO) {
		    if (coords[0] == coords1[0]
			&& coords[1] == coords1[1]
			&& coords[2] == coords1[2]) {
			cpit = pit1;
		    }
		} else if (type2 == PathIterator3D.SEG_MOVETO) {
		    if (coords[0] == coords2[0]
			&& coords[1] == coords2[1]
			&& coords[2] == coords2[2]) {
			cpit = pit2;
		    }
		} else if (type3 == PathIterator3D.SEG_MOVETO) {
		    if (coords[0] == coords3[0]
			&& coords[1] == coords3[1]
			&& coords[2] == coords3[2]) {
			cpit = pit3;
		    }
		}
		break;
	    case PathIterator3D.SEG_LINETO:
		if (cpit != null) {
		    if (ptype != PathIterator3D.SEG_LINETO) {
			throw new Exception();
		    }
		    if (pcoords[0] != coords[0]
			|| pcoords[1] != coords[1]
			|| pcoords[2] != coords[2]) {
			throw new Exception();
		    }

		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		if (cpit != null) {
		    if (ptype != PathIterator3D.SEG_QUADTO) {
			throw new Exception();
		    }
		    if (pcoords[0] != coords[0]
			|| pcoords[1] != coords[1]
			|| pcoords[2] != coords[2]) {
			throw new Exception();
		    }
		    if (pcoords[3] != coords[3]
			|| pcoords[4] != coords[4]
			|| pcoords[5] != coords[5]) {
			throw new Exception();
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		if (cpit != null) {
		    if (ptype != PathIterator3D.SEG_CUBICTO) {
			throw new Exception();
		    }
		    if (pcoords[0] != coords[0]
			|| pcoords[1] != coords[1]
			|| pcoords[2] != coords[2]) {
			throw new Exception();
		    }
		    if (pcoords[3] != coords[3]
			|| pcoords[4] != coords[4]
			|| pcoords[5] != coords[5]) {
			throw new Exception();
		    }
		    if (pcoords[6] != coords[6]
			|| pcoords[7] != coords[7]
			|| pcoords[8] != coords[8]) {
			throw new Exception();
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (cpit != null) {
		    if (ptype != PathIterator3D.SEG_CLOSE) {
			throw new Exception();
		    }
		    cpit.next();
		    if (!cpit.isDone()) {
			throw new Exception();
		    }
		    cpit  = null;
		    ptype = -1;
		}
	    }
	    if (cpit != null) {
		if (cpit.isDone()) {
		    throw new Exception();
		}
		cpit.next();
		ptype = cpit.currentSegment(pcoords);
	    }
	    pit.next();
	}


	BezierGrid grid2 = new BezierGrid(grid1, true, (p) -> {
		return new Point3D.Double(0.8*p.getX(), 0.8*p.getY(),
					  p.getZ());
	    });

	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);
	System.out.println("creating connections ...");
	BezierGrid[] connections = grid1.createConnectionsTo(grid2, 2, split);
	System.out.println("... connections created");
	for (BezierGrid g: connections) {
	    surface.append(g);
	    System.out.format("connection width: %d, closed = %b\n",
			      g.getVArrayLength(), g.isVClosed());

	}
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	connections = grid1.createConnectionsTo(grid2, 2, split, 0, 5);
	System.out.println("number of connections for (0, 5) = "
			   + connections.length);

	Path3D boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("no boundary because not well formed");
	    System.exit(1);
	} else if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("boundary:");
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
	    m3d.createImageSequence(new FileOutputStream("connection4.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
    }
}
