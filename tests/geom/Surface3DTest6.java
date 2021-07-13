import org.bzdev.geom.*;

public class Surface3DTest6 {

    public static void main(String argv[]) throws Exception {
	Surface3D s3d = new Surface3D.Double(false);

	double[] coords = new double[48];
	
	Surface3D.setupV0ForPatch(Path3D.setupCubic(0.0, 0.0, 0.0,
						    100.0, 0.0, 0.0),
				  coords, true);
	Surface3D.setupV1ForPatch(Path3D.setupCubic(0.0, 100.0, 0.0,
						    100.0, 100.0, 0.0),
				  coords, true);
	
	Surface3D.setupU1ForPatch(Path3D.setupCubic(0.0, 0.0, 0.0,
						    0.0, 100.0, 0.0),
				  coords, false);
	Surface3D.setupU0ForPatch(Path3D.setupCubic(100.0, 0.0, 0.0,
						    100.0, 100.0, 0.0),
				  coords, false);
	Surface3D.setupRestForPatch(coords);
	s3d.addCubicPatch(coords);

	Surface3D.setupV0ForTriangle(Path3D.setupCubic(0.0, 100.0, 0.0,
						       100.0, 100.0, 0.0),
				     coords, false);
	Surface3D.setupU0ForTriangle(Path3D.setupCubic(0.0, 100.0, 0.0,
						       50.0, 200.0, 0.0),
				     coords, false);
	Surface3D.setupW0ForTriangle(Path3D.setupCubic(100.0, 100.0, 0.0,
						       50.0, 200.0, 0.0),
				     coords, false);
	Surface3D.setupPlanarCP111ForTriangle(coords);
	s3d.addCubicTriangle(coords);

	s3d.addPlanarTriangle(100.0, 0.0, 0.0,
			      200.0, 50.0, 0.0,
			      100.0, 100.0, 0.0);

	if (s3d.numberOfComponents() != 1) {
	    throw new Exception("number of components = "
				+ s3d.numberOfComponents());
	}

	Shape3D component = s3d.getComponent(0);

	Path3D boundary1 = s3d.getBoundary();
	if (boundary1 == null) {
	    throw new Exception ("boundary1 == null");
	}
	Path3DInfo.printSegments(System.out, boundary1);
	Path3D boundary2 = component.getBoundary();

	PathIterator3D pit1 = boundary1.getPathIterator(null);
	PathIterator3D pit2 = boundary2.getPathIterator(null);

	double[] coords1 = new double[48];
	double[] coords2 = new double[48];
	while (!pit1.isDone() &&  !pit2.isDone()) {
	    if (pit1.currentSegment(coords1) != pit2.currentSegment(coords2)) {
		throw new Exception ("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coords1[i] - coords2[i]) > 1.e-10) {
		    throw new Exception("coords1 != coords2");
		}
	    }
	    pit1.next(); pit2.next();
	}
	if (pit1.isDone() != pit2.isDone()) {
	    throw new Exception ("boundary1 and boundary2 differ in number "
				 + "of segments");
	}

	System.exit(0);
    }
}
