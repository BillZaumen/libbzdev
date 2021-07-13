import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

public class TLTest4{
    public static void main(String argv[]) throws Exception {
	Surface3D surface = new Surface3D.Double();
	Surface3D surface2 = new Surface3D.Double();

	double[] edge0 = Path3D.setupCubic(0.0, 0.0, 0.0, 100.0, 0.0, 0.0);
	double[] edge1 = new double[12];
	double[] edge2 = new double[12];
	double[] edge3 = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 100.0, 0.0);


	for (int i = 0; i < 12; i++) {
	    edge1[i] = edge3[i];
	    edge2[i] = edge0[i];
	}
	for (int i = 0; i < 12; i += 3) {
	    edge2[i+1] = 100.0;
	    edge1[i] = 100.0;
	}


	double[] coords = new double[48];
	// entry 0
	Surface3D.setupV0ForPatch(edge0, coords, false);
	Surface3D.setupV1ForPatch(edge2, coords, false);
	Surface3D.setupU0ForPatch(edge3, coords, false);
	Surface3D.setupU1ForPatch(edge1, coords, false);
	Surface3D.setupRestForPatch(coords);
	coords[17] = -20.0;  coords[20] = -20.0;
	coords[29] = -20.0; coords[32] = -20.0;

	surface.addFlippedCubicPatch(coords);
	// use to test boundary.
	surface2.addFlippedCubicPatch(coords);

	// entry 1
	
	surface.addPlanarTriangle(0.0, 0.0, 0.0,
				  100.0, 0.0, 0.0,
				  50.0, 50.0, 100.0);

	// entry 2

	surface.addPlanarTriangle(0.0, 100.0, 0.0,
				  0.0, 0.0, 0.0,
				  50.0, 50.0, 100.0);
	
	// entry 3
	surface.addFlippedPlanarTriangle(100.0, 100.0, 0.0,
					 100.0, 0.0, 0.0,
					 50.0, 50.0, 100.0);
	
	// entry 4
	surface.addFlippedPlanarTriangle(0.0, 100.0, 0.0,
					 100.0, 100.0, 0.0,
					 50.0, 50.0, 100.0);

	for (int i = 0; i < surface.size(); i++) {
	    int type = surface.getSegment(i, coords);
	    System.out.print("Entry " + i);
	    if (type == SurfaceIterator.CUBIC_PATCH) {
		for (int k = 0; k < 48; k += 3) {
		    if(k%12 == 0) System.out.println();
		    System.out.format(" (%g,%g,%g)",
				      coords[k], coords[k+1], coords[k+2]);
		}
		System.out.println();
	    } else if (type == SurfaceIterator.CUBIC_TRIANGLE) {
		System.out.println();
		System.out.format(" (%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[0], coords[1], coords[2],
                                  coords[12], coords[13], coords[14],
                                  coords[21], coords[22], coords[23],
                                  coords[27], coords[28], coords[29]);

		System.out.format(" (%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[27], coords[28], coords[29],
                                  coords[24], coords[25], coords[26],
                                  coords[18], coords[19], coords[20],
                                  coords[9], coords[10], coords[11]);
		System.out.format(" (%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[9], coords[10], coords[11],
                                  coords[6], coords[7], coords[8],
                                  coords[3], coords[4], coords[5],
                                  coords[0], coords[1], coords[2]);
	    } else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
		System.out.println();
		System.out.format(" (%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[0], coords[1], coords[2],
                                  coords[6], coords[7], coords[8]);

		System.out.format(" (%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[6], coords[7], coords[8],
                                  coords[3], coords[4], coords[5]);
		System.out.format(" (%g,%g,%g)-(%g,%g,%g)"
				  + "\n",
                                  coords[3], coords[4], coords[5],
                                  coords[0], coords[1], coords[2]);
	    }

	}

	if (surface.isWellFormed(System.out) == false) {
	    System.out.println("surface is not well formed");
	}

	Path3D sboundary = surface.getBoundary();
	if (!sboundary.isEmpty()) {
	    System.out.println("boundary not empty");
	    System.exit(1);
	} else {
	    System.out.println("boundary empty as expected");
	}

	Path3D boundary2 = surface2.getBoundary();
	System.out.println("segments for boundary of the cubic patch:");
	Path3DInfo.printSegments("  ", System.out, boundary2);


	Model3D m3d = new Model3D();
	Model3D tm3d = new Model3D();

	m3d.append(surface);
	m3d.setTessellationLevel(5);
	/*
	int i = 0; 
	Iterator<Model3D.Triangle>it = m3d.tessellate();

	while (it.hasNext()) {
	    Model3D.Triangle triangle = it.next();

	    System.out.format("%d: "
			      + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			      ++i,
			      triangle.getX1(), triangle.getY1(),
			      triangle.getZ1(),
			      triangle.getX2(), triangle.getY2(),
			      triangle.getZ2(),
			      triangle.getX3(), triangle.getY3(),
			      triangle.getZ3());

	    tm3d.addTriangle(triangle.getX1(), triangle.getY1(),
			     triangle.getZ1(),
			     triangle.getX2(), triangle.getY2(),
			     triangle.getZ2(),
			     triangle.getX3(), triangle.getY3(),
			     triangle.getZ3());
	}
	*/
	List<Model3D.Edge> elist = m3d.verifyClosed2DManifold();

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.out.println("Allegedly intersecting triangles:");
	    for (Model3D.Triangle triangle: tlist) {
		System.out.format("(%s,%s,%s)-(%s,%s,%s)-(%s,%s,%s)\n",
				  triangle.getX1(), triangle.getY1(),
				  triangle.getZ1(),
				  triangle.getX2(), triangle.getY2(),
				  triangle.getZ2(),
				  triangle.getX3(), triangle.getY3(),
				  triangle.getZ3());
	    }
	}

	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d not printable");
	    System.exit(1);
	} else {
	    System.out.println("m3d  printable");
	}

	m3d.createImageSequence(new FileOutputStream("tltest4.isq"),
				 "png", 8, 6);

	System.exit(0);
    }
}
