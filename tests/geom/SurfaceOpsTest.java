import org.bzdev.geom.*;

public class SurfaceOpsTest {

    public static void main(String argv[]) throws Exception {
	Surface3D surface = new Surface3D.Double();
	
	double[] coords = new double[48];
	double[] coords1 = new double[48];
	double[] coords2 = new double[48];

	Surface3D.setupV0ForPatch(Path3D.setupCubic(0.0, 0.0, 0.0,
						    10.0, 0.0, 0.0),
				  coords1, false);
	Surface3D.setupV1ForPatch(Path3D.setupCubic(0.0, 20.0, 0.0,
						    10.0, 20.0, 0.0),
				  coords1, false);
	Surface3D.setupU0ForPatch(Path3D.setupCubic(0.0, 0.0, 0.0,
						    0.0, 20.0, 0.0),
				  coords1, false);
	Surface3D.setupU1ForPatch(Path3D.setupCubic(10.0, 0.0, 0.0,
						    10.0, 20.0, 0.0),
				  coords1, false);
	Surface3D.setupRestForPatch(coords1);
	surface.addCubicPatch(coords1, "patch1");


	Surface3D.setupV0ForPatch(Path3D.setupCubic(10.0, 0.0, 0.0,
						    20.0, 0.0, 0.0),
				  coords2, false);
	Surface3D.setupV1ForPatch(Path3D.setupCubic(10.0, 20.0, 0.0,
						    20.0, 20.0, 0.0),
				  coords2, false);
	Surface3D.setupU0ForPatch(Path3D.setupCubic(10.0, 0.0, 0.0,
						    10.0, 20.0, 0.0),
				  coords2, false);
	Surface3D.setupU1ForPatch(Path3D.setupCubic(20.0, 0.0, 0.0,
						    20.0, 20.0, 0.0),
				  coords2, false);
	Surface3D.setupRestForPatch(coords2);
	surface.addCubicPatch(coords2, "patch2");

	int sz = surface.size();
	if (sz != 2) throw new Exception("wrong size");
	if (surface.getSegment(0, coords) != SurfaceIterator.CUBIC_PATCH) {
	    throw new Exception("wrong type for patch 1");
	}
	for (int i = 0; i < 48; i++) {
	    if(coords[i] != (float) coords1[i]) {
		System.out.println("i = " + i + ", "
				   + coords[i] + " != " + coords1[i]);
		throw new Exception("coords do not match - patch 1");
	    }
	}
	if (surface.getSegment(1, coords) != SurfaceIterator.CUBIC_PATCH) {
	    throw new Exception("wrong type for patch 2");
	}
	for (int i = 0; i < 48; i++) {
	    if(coords[i] != (float)coords2[i]) {
		throw new Exception("coords do not match - patch 2");
	    }
	}

	for (int i = 0; i < sz; i++) {
	    System.out.format("segment %d has tag \"%s\"\n",
			      i, surface.getSegmentTag(i));
	}
	Path3D boundary = surface.getBoundary();
	if (boundary == null) {
	    throw new Exception ("surface is not well formed");
	}
	PathIterator3D pit = boundary.getPathIterator(null);
	int bindices[] = surface.getBoundarySegmentIndices();
	int edgeNumbers[] = surface.getBoundaryEdgeNumbers();
	int ind = -1;
	double[] pcoords = new double[9];
	double x = 0, y = 0, z = 0;
	double nx = 0, ny = 0, nz = 0;
	double fx = 0, fy = 0, fz = 0;
	while (!pit.isDone()) {
	    switch(pit.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		nx = pcoords[0];
		ny = pcoords[1];
		nz = pcoords[2];
		fx = nx; fy = ny; fz = nz;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		ind++;
		nx = pcoords[6];
		ny = pcoords[7];
		nz = pcoords[8];
		int index = bindices[ind];
		System.out.format("path entry %d: "
				  + "edge from (%g,%g,%g) to (%g,%g,%g)\n",
				  ind,
				  x, y, z, nx, ny, nz);
		System.out.format("    surface entry %d, edge number %d, "
				  + "tag %s\n",
				  index,
				  edgeNumbers[ind],
				  surface.getSegmentTag(index));
			      
		break;
	    case PathIterator3D.SEG_CLOSE:
		nx = fx;
		ny = fy;
		nz = fz;
		break;
	    }
	    x = nx;
	    y = ny;
	    z = nz;
	    pit.next();
	}
    }
}