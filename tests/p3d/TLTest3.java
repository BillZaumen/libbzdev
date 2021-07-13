import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.util.Iterator;

public class TLTest3 {
    public static void main(String argv[]) throws Exception {
	Surface3D surface = new Surface3D.Double();

	double[] edge0 = {0.0, 0.0, 0.0,
			  40.0, 0.0, 0.0,
			  50.0, 0.0,  0.0,
			  100.0, 0.0, 0.0};
	double[] edge1 = new double[12];
	double[] edge2 = new double[12];
	double[] edge3 = {0.0, 0.0, 0.0,
			  0.0, 40.0, 0.0,
			  0.0, 50.0, 0.0,
			  0.0, 100.0, 0.0};
	double[] vedge0 = {0.0, 0.0, 0.0,
			   15.0, 15.0, 45.0,
			   35.0, 35.0, 55.0,
			   50.0, 50.0, 100.0};

	double[] vedge1 = {100.0, 0.0, 0.0,
			   85.0, 15.0, 45.0,
			   65.0, 35.0, 55.0,
			   50.0, 50.0, 100.0};

	double[] vedge2 = {100.0, 100.0, 0.0,
			   85.0, 85.0, 45.0,
			   65.0, 65.0, 55.0,
			   50.0, 50.0, 100.0};
	double[] vedge3 = {0.0, 100.0, 0.0,
			 15.0, 85.0, 45.0,
			 35.0, 65.0, 55.0,
			   50.0, 50.0, 100.0};

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
	surface.addFlippedCubicPatch(coords);

	// entry 1
	Surface3D.setupV0ForTriangle(edge0, coords, false);
	Surface3D.setupU0ForTriangle(vedge0, coords, false);
	Surface3D.setupW0ForTriangle(vedge1, coords, false);
	Surface3D.setupCP111ForTriangle(coords);
	surface.addCubicTriangle(coords);

	// entry 2
	Surface3D.setupV0ForTriangle(edge1, coords, false);
	Surface3D.setupU0ForTriangle(vedge1, coords, false);
	Surface3D.setupW0ForTriangle(vedge2, coords, false);
	Surface3D.setupCP111ForTriangle(coords);
	surface.addCubicTriangle(coords);
	
	// entry 3
	Surface3D.setupV0ForTriangle(edge2, coords, true);
	Surface3D.setupU0ForTriangle(vedge2, coords, false);
	Surface3D.setupW0ForTriangle(vedge3, coords, false);
	Surface3D.setupCP111ForTriangle(coords);
	surface.addCubicTriangle(coords);
	
	// entry 4
	Surface3D.setupV0ForTriangle(edge3, coords, true);
	Surface3D.setupU0ForTriangle(vedge3, coords, false);
	Surface3D.setupW0ForTriangle(vedge0, coords, false);
	Surface3D.setupCP111ForTriangle(coords);
	surface.addCubicTriangle(coords);
	


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

	    }
	}

	if (surface.isWellFormed(System.out) == false) {
	    System.out.println("surface is not well formed");
	}

	Model3D m3d = new Model3D();
	Model3D tm3d = new Model3D();

	m3d.append(surface);
	m3d.setTessellationLevel(2);

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

	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d not printagble");
	    System.exit(1);
	}

	m3d.createImageSequence(new FileOutputStream("tltest3.isq"),
				"png", 8, 4);

	System.exit(0);
    }
}
