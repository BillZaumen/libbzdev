import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.util.Iterator;

public class Surface3DTest7 {
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

	


	System.out.println("testing surface2");
	Surface3D surface2 = new Surface3D.Double();
	surface2.append(surface.getSurfaceIterator(null, 1));

	if (surface2.isWellFormed(System.out) == false) {
	    System.out.println("surface2 is not well formed");
	}

	System.out.println("testing surface3");
	Surface3D surface3 = new Surface3D.Double();
	surface3.append(surface2.getSurfaceIterator(null, 1));
	if (surface3.isWellFormed(System.out) == false) {
	    System.out.println("surface3 is not well formed");
	}

	System.out.println("testing surface4");
	Surface3D surface4 = new Surface3D.Double();
	surface4.append(surface.getSurfaceIterator(null, 2));
	System.out.println("surface3.size() = " + surface3.size());
	System.out.println("surface4.size() = " + surface4.size());

	double[] coords3 = new double[48];
	double[] coords4 = new double[48];
	for (int i = 0; i < surface3.size(); i++) {
	    int type3 = surface3.getSegment(i, coords3);
	    int type4 = surface4.getSegment(i, coords4);
	    if (type3 != type4) {
		System.out.println("type mismatch");
	    }
	    if (type3 == SurfaceIterator.CUBIC_PATCH) {
		for (int k = 0; k < 48; k++) {
		    if ((float)coords3[k] != (float)coords4[k]) {
			System.out.format("CP entry %d: k = %d, %s !=%s\n",
					  i, k, coords3[k], coords4[k]);
		    }
		}
	    } else if (type3 == SurfaceIterator.CUBIC_TRIANGLE) {
		boolean failed = false;
		for (int k = 0; k < 30; k++) {
		    double ulp = Math.ulp((float)coords4[k]);
		    if (ulp < Math.ulp(1.0F)) ulp = Math.ulp(1.0F);
		    if (Math.abs(coords3[k]- coords4[k]) > 2*ulp) {
			System.out.format("CT entry %d: k = %d, %s !=%s\n",
					  i, k, coords3[k], coords4[k]);
			double diff = coords3[k] - coords4[k];
			System.out.println("... diff = " + diff);
			System.out.println(".... ulp = " + ulp);
			failed = true;
		    }
		}
		if (failed) System.exit(1);
	    }

	    /*
	    System.out.format("coords3: %g, %g, %g ...\n",
			      coords3[0], coords3[1], coords3[2]);
	    System.out.format("coords4: %g, %g, %g ...\n",
			      coords4[0], coords4[1], coords4[2]);
	    */
	}

	/*
	if (surface4.isWellFormed(System.out) == false) {
	    System.out.println("surface3 is not well formed");
	}
	*/

	System.exit(0);
    }
}
