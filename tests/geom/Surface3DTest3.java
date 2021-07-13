import org.bzdev.geom.*;

public class Surface3DTest3 {

    public static void main(String argv[]) throws Exception {
	Rectangle3D r3d1 = new Rectangle3D.Double(0.0, 0.0, 0.0,
						  100.0, 200.0, 300.0);
	Surface3D s3d = new Surface3D.Double(r3d1);

	Rectangle3D r3d2 = new Rectangle3D.Double(500.0, 500.0, 500.0,
						  100.0, 200.0, 300.0);

	s3d.append(r3d2);


	s3d.addPlanarTriangle(400.0, 400.0, 400.0,
			      500.0, 400.0, 400.0,
			      500.0, 500.0, 400.0);

	double[] coords = new double[48];
	
	Surface3D.setupV0ForTriangle(Path3D.setupCubic(600.0, 600.0, 400.0,
						       700.0, 600.0, 400.0),
				     coords, false);
	Surface3D.setupU0ForTriangle(Path3D.setupCubic(600.0, 600.0, 400.0,
						       650.0, 700.0, 400.0),
				     coords, false);
	Surface3D.setupW0ForTriangle(Path3D.setupCubic(700.0, 600.0, 400.0,
						       650.0, 700.0, 400.0),
				     coords, false);
	Surface3D.setupPlanarCP111ForTriangle(coords);

	s3d.addCubicTriangle(coords);

	Surface3D.setupV0ForPatch(Path3D.setupCubic(0.0, 0.0, 400.0,
						    100.0, 0.0, 400.0),
				  coords, false);
	Surface3D.setupV1ForPatch(Path3D.setupCubic(0.0, 100.0, 400.0,
						    100.0, 100.0, 400.0),
				  coords, false);
	
	Surface3D.setupU0ForPatch(Path3D.setupCubic(0.0, 0.0, 400.0,
						    0.0, 100.0, 400.0),
				  coords, false);
	Surface3D.setupU1ForPatch(Path3D.setupCubic(100.0, 0.0, 400.0,
						    100.0, 100.0, 400.0),
				  coords, false);
	Surface3D.setupRestForPatch(coords);
	s3d.addCubicPatch(coords);


	System.out.println("number of components = "
			   + s3d.numberOfComponents());

	Shape3D c1 = s3d.getComponent(0);
	Rectangle3D c1r = c1.getBounds();
	System.out.format("(%g,%g,%g)--(%g,%g,%g)\n",
			  c1r.getMinX(), c1r.getMinY(), c1r.getMinZ(),
			  c1r.getMaxX(), c1r.getMaxY(), c1r.getMaxZ());

	Path3D boundary = c1.getBoundary();
	if (boundary == null) {
	    throw new Exception("boundary is null");
	}
	if (!boundary.isEmpty()) {
	    throw new Exception("boundary not empty");
	}


	Shape3D c2 = s3d.getComponent(1);
	Rectangle3D c2r = c2.getBounds();
	System.out.format("(%g,%g,%g)--(%g,%g,%g)\n",
			  c2r.getMinX(), c2r.getMinY(), c2r.getMinZ(),
			  c2r.getMaxX(), c2r.getMaxY(), c2r.getMaxZ());

	boundary = c2.getBoundary();
	if (boundary == null) {
	    throw new Exception("boundary is null");
	}
	if (!boundary.isEmpty()) {
	    throw new Exception("boundary not empty");
	}

	Shape3D c3 = s3d.getComponent(2);
	Rectangle3D c3r = c3.getBounds();
	System.out.format("(%g,%g,%g)--(%g,%g,%g)\n",
			  c3r.getMinX(), c3r.getMinY(), c3r.getMinZ(),
			  c3r.getMaxX(), c3r.getMaxY(), c3r.getMaxZ());

	boundary = c3.getBoundary();
	if (boundary == null) {
	    throw new Exception("boundary is null");
	}
	if (boundary.isEmpty()) {
	    throw new Exception("boundary is empty");
	}
	Path3DInfo.printSegments(boundary);


	Shape3D c4 = s3d.getComponent(3);
	Rectangle3D c4r = c4.getBounds();
	System.out.format("(%g,%g,%g)--(%g,%g,%g)\n",
			  c4r.getMinX(), c4r.getMinY(), c4r.getMinZ(),
			  c4r.getMaxX(), c4r.getMaxY(), c4r.getMaxZ());
	boundary = c4.getBoundary();
	if (boundary == null) {
	    throw new Exception("boundary is null");
	}
	if (boundary.isEmpty()) {
	    throw new Exception("boundary is empty");
	}
	Path3DInfo.printSegments(boundary);


	Shape3D c5 = s3d.getComponent(4);
	Rectangle3D c5r = c5.getBounds();
	System.out.format("(%g,%g,%g)--(%g,%g,%g)\n",
			  c5r.getMinX(), c5r.getMinY(), c5r.getMinZ(),
			  c5r.getMaxX(), c5r.getMaxY(), c5r.getMaxZ());
	boundary = c5.getBoundary();
	if (boundary == null) {
	    throw new Exception("boundary is null");
	}
	if (boundary.isEmpty()) {
	    throw new Exception("boundary is empty");
	}
	Path3DInfo.printSegments(boundary);

    }
}