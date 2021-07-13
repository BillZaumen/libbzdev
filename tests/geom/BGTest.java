import org.bzdev.geom.*;

public class BGTest {
    public static void main(String argv[]) throws Exception {
	Point3D[][] array = new Point3D[2][2];
	array[0][0] = new Point3D.Double(0.0, 0.0, 0.0);
	array[1][0] = new Point3D.Double(1.0, 0.0, 1.0);
	array[0][1] = new Point3D.Double(0.0, 1.0, 1.0);
	array[1][1] = new Point3D.Double(1.0, 1.0, 2.0);
	BezierGrid grid = new BezierGrid(array, false, false);

	double[] coords = new double[12];

	System.out.println(grid.getRemainingControlPoints(0, 0, coords));

	double[] rest = {0.25, 0.25, 4.0,
			 0.25, 0.75, 5.0,
			 0.75, 0.25, 10.0,
			 0.75, 0.75, 11.0
	};

	grid.setRemainingControlPoints(0, 0, rest);
	System.out.println(grid.getRemainingControlPoints(0, 0, coords));
	for (int i = 0; i < 4; i++) {
	    System.out.print(coords[i*3+2] + " ");
	}

	System.out.println();

	double expecting[] = {
	    0.0, 0.0, 0.0, 1.0/3, 0.0, 1.0/3, 2.0/3, 0.0, 2.0/3, 1.0, 0.0, 1.0,

	    0.0, 1.0/3, 1.0/3, 0.25, 0.25, 4, 0.25, 0.75, 5.0,
	    1.0, 1.0/3, 1+1.0/3,

	    0.0, 2.0/3, 2.0/3, 0.75, 0.25, 10.0, 0.75, 0.75, 11.0,
	    1.0, 2.0/3, 1+2.0/3,

	    0.0, 1.0, 1.0, 1.0/3, 1.0, 1+1.0/3, 2.0/3, 1.0, 1+2.0/3,
	    1.0, 1.0, 2.0
	};
	double[] pcoords = new double[48];
	if (grid.getPatch(0, 0, pcoords)) {
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(pcoords[i] - (double)(float)expecting[i])
		    > 1.e-10) {
		    throw new Exception(i + ": " + pcoords[i] + " != "
					+ expecting[i]);
		}
	    }
	} else {
	    System.out.println("getPatch failed");
	}

	System.out.println("----------");
	System.out.println();

	array[0][0] = new Point3D.Double(0.0, 10.0, 10.0);
	array[0][1] = new Point3D.Double(0.0, 0.0, 10.0);
	array[1][0] = new Point3D.Double(0.0, 10.0, 0.0);
	array[1][1] = new Point3D.Double(0.0, 0.0, 0.0);
	grid = new BezierGrid(array, false, false);

	for (int i = 0; i < grid.getUArrayLength(); i++) {
	    for (int j = 0; j < grid.getVArrayLength(); j++) {
		System.out.format("at (%d, %d):\n", i , j);
		if (grid.getFullSplineU(i, j, coords)) {
		    System.out.println("    U:");
		    System.out.format("       (%g, %g, %g)\n",
				      coords[0], coords[1], coords[2]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[3], coords[4], coords[5]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[6], coords[7], coords[8]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[9], coords[10], coords[11]);
		};
		if (grid.getFullSplineV(i, j, coords)) {
		    System.out.println("    V:");
		    System.out.format("       (%g, %g, %g)\n",
				      coords[0], coords[1], coords[2]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[3], coords[4], coords[5]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[6], coords[7], coords[8]);
		    System.out.format("       (%g, %g, %g)\n",
				      coords[9], coords[10], coords[11]);
		};
	    }
	}

	array = new Point3D[3][3];
	array[0][0] = new Point3D.Double(0.0, 0.0, 0.0);
	array[1][0] = new Point3D.Double(1.0, 0.0, 1.0);
	array[2][0] = new Point3D.Double(2.0, 0.0, 2.0);
	array[0][1] = new Point3D.Double(0.0, 1.0, 3.0);
	array[1][1] = new Point3D.Double(1.0, 1.0, 4.0);
	array[2][1] = new Point3D.Double(2.0, 1.0, 5.0);
	array[0][2] = new Point3D.Double(0.0, 2.0, 6.0);
	array[1][2] = new Point3D.Double(1.0, 2.0, 7.0);
	array[2][2] = new Point3D.Double(2.0, 2.0, 8.0);
	grid = new BezierGrid(array, false, false);
	BezierGrid grid2 = new BezierGrid(3, false, 3, false);
	double[] coords1 = new double[48];
	grid = new BezierGrid(array, false, false);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    grid2.setPatchCorners(i, j, coords1);
		}
	    }
	}
	double[] coords2 = new double[48];
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    if (grid2.getPatch(i, j, coords2) == false) {
			System.out.println("warning: coords2 not set");
		    }
		    for (int k = 0; k < 3; k++) {
			if (coords1[k] != coords2[k]) {
			    String msg = "at (" +i + ", " + j + ") (k = "
				+ k + "), "
				+ coords1[k] + "!= " + coords2[k];
			    throw new Exception(msg);
			}
		    }
		    for (int k = 9; k < 12; k++) {
			if (coords1[k] != coords2[k]) {
			    String msg = "at (" +i + ", " + j + ") (k = "
				+ k + "), "
				+ coords1[k] + "!= " + coords2[k];
			    throw new Exception(msg);
			}
		    }
		    for (int k = 36; k < 39; k++) {
			if (coords1[k] != coords2[k]) {
			    String msg = "at (" +i + ", " + j + ") (k = "
				+ k + "), "
				+ coords1[k] + "!= " + coords2[k];
			    throw new Exception(msg);
			}
		    }
		    for (int k = 45; k < 48; k++) {
			if (coords1[k] != coords2[k]) {
			    String msg = "at (" +i + ", " + j + ") (k = "
				+ k + "), "
				+ coords1[k] + "!= " + coords2[k];
			    throw new Exception(msg);
			}
		    }
		}
	    }
	}

	grid = new BezierGrid(array, true, true);
	grid2 = new BezierGrid(3, true, 3, true);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    grid2.setPatchCorners(i, j, coords1);
		}
	    }
	}

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    grid2.getPatch(i, j, coords2);
		    for (int k = 0; k < 48; k++) {
			if (coords1[k] != coords2[k]) {
			    String msg = "at (" +i + ", " + j + ") (k = " + k
				+ "), "+ coords1[k] + "!= " + coords2[k];
			    throw new Exception(msg);
			}
		    }
		}
	    }
	}

	grid = new BezierGrid(array, false, false);
	grid.traceSplines(System.out);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!grid.getPoint(i,j).equals(array[i][j])) {
		    throw new Exception("grid was not set up correctly");
		}
	    }
	}
	grid = new BezierGrid(array, false, false);
	grid2 = new BezierGrid(3, false, 3, false);

	// grid.print();

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    grid2.setPatch(i, j, coords1);
		}
		if (!grid2.getPoint(i,j).equals(array[i][j])) {
		    throw new Exception("grid was not set up correctly");
		}
		if (!grid2.getPoint(i+1,j).equals(array[i+1][j])) {
		    System.out.println("bad point " + grid2.getPoint(i+1,j));
		    System.out.println("expecting " + grid.getPoint(i+1,j));
		    System.out.format("coords: (%g, %g, %g)\n",
				      coords1[9], coords1[10], coords1[11]);
		    throw new Exception("grid was not set up correctly at ("
					+i + "+1, " + j + ")");
		}
		if (!grid2.getPoint(i,j+1).equals(array[i][j+1])) {
		    throw new Exception("grid was not set up correctly at ("
					+i + ", " + j + "+1)");
		}
		if (!grid2.getPoint(i+1,j+1).equals(array[i+1][j+1])) {
		    throw new Exception("grid was not set up correctly at ("
					+i + "+1, " + j + "+1)");
		}
	    }
	}
	// grid.print();
	// grid2.print();

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		Point3D p = grid2.getPoint(i, j);
		if (p != null) {
		    if (!p.equals(array[i][j])) {
			String msg =
			    String.format("point mismatch at (%d,%d)\n", i, j);
			throw new Exception(msg);
		    }
		}
	    }
	}


	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (!grid.getPatch(i, j, coords1)) {
		    System.out.println("grid.getPatch failed: "
				       + " i = " + i + ", j = " + j);
		}
		if (!grid2.getPatch(i, j, coords2)) {
		    System.out.println("grid2.getPatch failed: "
				       + " i = " + i + ", j = " + j);
		}
		for (int k = 0; k < 48; k++) {
		    if (coords1[k] != coords2[k]) {
			if (coords1[k] != (double)(float)coords1[k]) {
			    System.out.println("coords1 not rounded to float");
			}
			if (coords2[k] != (double)(float)coords2[k]) {
			    System.out.println("coords2 not rounded to float");
			}
			System.out.println("coords1:");
			for (int p = 0; p < 48; p += 3) {
			    if ((p > 0) && (p % 12 == 0)) System.out.println();
			    System.out.format(" (%g, %g, %g)",
					      coords1[p], coords1[p+1],
					      coords1[p+2]);
			}
			System.out.println();
			System.out.println("coords2:");
			for (int p = 0; p < 48; p += 3) {
			    if ((p > 0) && (p%12 == 0)) System.out.println();
			    System.out.format(" (%g, %g, %g)",
					      coords2[p], coords2[p+1],
					      coords2[p+2]);
			}
			System.out.println();

			String msg = "at (" + i + ", " + j + ") "
			    + "(k = " + k + "), "
			    + coords1[k] + "!= " + coords2[k];
			throw new Exception(msg);
		    }
		}
	    }
	}

	grid = new BezierGrid(array, true, true);
	grid2 = new BezierGrid(3, true, 3, true);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (grid.getPatch(i, j, coords1)) {
		    grid2.setPatch(i, j, coords1);
		}
	    }
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		grid.getPatch(i, j, coords1);
		grid2.getPatch(i, j, coords2);
		for (int k = 0; k < 48; k++) {
		    if (coords1[k] != coords2[k]) {
			String msg = "at (" +i + ", " + j + "), "
			    + coords1[k] + "!= " + coords2[k];
			throw new Exception(msg);
		    }
		}
	    }
	}
	System.out.println("--- Test use of setLinearU and setLinearV ---");

	array = new Point3D[3][3];

	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		array[i][j] = new Point3D.Double((double)i,(double)j, 0.0);
	    }
	}
	grid = new BezierGrid(array, false, false);

	Surface3D surface = new Surface3D.Double();

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		grid.setLinearU(i, j);
		grid.setLinearV(i, j);
	    }
	}

	surface.append(grid);


	Point3D p1 = grid.getPoint(0,0);
	Point3D p2 = grid.getPoint(1,0);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(2, 0);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(2, 1);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(2, 2);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(1, 2);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(0,2);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(0,1);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);
	p1 = p2;
	p2 = grid.getPoint(0,0);
	surface.addPlanarTriangle(p2.getX(), p2.getY(), p2.getZ(),
				  p1.getX(), p1.getY(), p1.getZ(),
				  1.0, 1.0, -1.0);

	Path3D boundary = surface.getBoundary();
	if (boundary == null) {
	    throw new Exception("no boundary");
	} else if (!boundary.isEmpty()) {
	    throw new Exception("boundary not empty");
	} else {
	    System.out.println("boundary empty as expected");
	}
    }
}
