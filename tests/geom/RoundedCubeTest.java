import org.bzdev.geom.*;

public class RoundedCubeTest {
    public static void main(String argv[]) throws Exception {
	Surface3D surface = new Surface3D.Double();

	double[] edge0 = {0.0, 0.0, 0.0,
			  40.0, 0.0, 0.0,
			  50.0, 0.0,  0.0,
			  100.0, 0.0, 0.0};
	double[] edge0a = new double[12];
	double[] edge1 = new double[12];
	double[] edge1a = new double[12];
	double[] edge2 = new double[12];
	double[] edge2a = new double[12];
	double[] edge3 = {0.0, 0.0, 0.0,
			  0.0, 40.0, 0.0,
			  0.0, 50.0, 0.0,
			  0.0, 100.0, 0.0};
	double[] edge3a = new double[12];
	double[] vedge0 = {0.0, 0.0, 0.0,
			   0.0, 0.0, 40.0,
			   0.0, 0.0, 50.0,
			   0.0, 0.0, 100.0};
	double[] vedge1 = {100.0, 0.0, 0.0,
			   100.0, 0.0, 40.0,
			   100.0, 0.0, 50.0,
			   100.0, 0.0, 100.0};
	double[] vedge2 = {100.0, 100.0, 0.0,
			   100.0, 100.0, 40.0,
			   100.0, 100.0, 50.0,
			   100.0, 100.0, 100.0};
	double[] vedge3 = {0.0, 100.0, 0.0,
			   0.0, 100.0, 40.0,
			   0.0, 100.0, 50.0,
			   0.0, 100.0, 100.0};

	for (int i = 0; i < 12; i++) {
	    edge1[i] = edge3[i];
	    edge2[i] = edge0[i];
	    edge0a[i] = edge0[i];
	    edge1a[i] = edge1[i];
	    edge2a[i] = edge2[i];
	    edge3a[i] = edge3[i];
	}
	for (int i = 0; i < 12; i += 3) {
	    edge2[i+1] = 100.0;
	    edge2a[i+1] = 100.0;
	    edge1[i] = 100.0;
	    edge1a[i] = 100.0;
	    edge0a[i+2] = 100.0;
	    edge1a[i+2] = 100.0;
	    edge2a[i+2] = 100.0;
	    edge3a[i+2] = 100.0;
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
	for (int i = 2; i < 48; i += 3) {
	    coords[i] += 100.0;
	}
	surface.addCubicPatch(coords);

	// entry 2
	Surface3D.setupV0ForPatch(edge0, coords, false);
	Surface3D.setupV1ForPatch(edge0a, coords, false);
	Surface3D.setupU0ForPatch(vedge0, coords, false);
	Surface3D.setupU1ForPatch(vedge1, coords, false);
	Surface3D.setupRestForPatch(coords);
	surface.addCubicPatch(coords);
	
	// entry 3
	Surface3D.setupV0ForPatch(edge2, coords, false);
	Surface3D.setupV1ForPatch(edge2a, coords, false);
	Surface3D.setupU0ForPatch(vedge3, coords, false);
	Surface3D.setupU1ForPatch(vedge2, coords, false);
	Surface3D.setupRestForPatch(coords);
	surface.addFlippedCubicPatch(coords);

	// entry 4
	Surface3D.setupV0ForPatch(edge1, coords, false);
	Surface3D.setupV1ForPatch(edge1a, coords, false);
	Surface3D.setupU0ForPatch(vedge1, coords, false);
	Surface3D.setupU1ForPatch(vedge2, coords, false);
	Surface3D.setupRestForPatch(coords);
	surface.addCubicPatch(coords);

	// entry 5
	Surface3D.setupV0ForPatch(edge3, coords, false);
	Surface3D.setupV1ForPatch(edge3a, coords, false);
	Surface3D.setupU0ForPatch(vedge0, coords, false);
	Surface3D.setupU1ForPatch(vedge3, coords, false);
	Surface3D.setupRestForPatch(coords);
	surface.addFlippedCubicPatch(coords);

	for (int i = 0; i < surface.size(); i++) {
	    surface.getSegment(i, coords);
	    System.out.print("Entry " + i);
	    for (int k = 0; k < 48; k += 3) {
		if(k%12 == 0) System.out.println();
		System.out.format(" (%g,%g,%g)",
				  coords[k], coords[k+1], coords[k+2]);
	    }
	    System.out.println();
	}

	for (int i = 0; i < surface.size(); i++) {
	    surface.getSegment(i, coords);
	    for (int k = 0; k < 48; k++) {
		if (coords[k] != (float)0.0 && coords[k] != (float)40.0
		    && coords[k] != (float)50.0 && coords[k] != (float)100.0) {
		    System.out.format("coords value off: segment %d, index %d: "
				      + "value = %s\n",
				      i, k, coords[i]);
		}
	    }
	}

	if (surface.isWellFormed(System.out) == false) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}
	Path3D boundary = surface.getBoundary();
	if (surface.getBoundary() == null) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}
	if (!boundary.isEmpty()) {
	    System.out.println("boundary not empty");
	    System.exit(1);
	}

	System.exit(0);
    }
}
