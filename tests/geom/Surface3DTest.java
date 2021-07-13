import org.bzdev.geom.*;
import java.awt.geom.*;
import org.bzdev.math.*;

public class Surface3DTest {
    public static void main(String argv[]) throws Exception {

	Surface3D cpatch = new Surface3D.Double();

	double cpatchCoords[] = new double[48];
	cpatchCoords[0] = 0.0;
	cpatchCoords[1] = 0.0;
	cpatchCoords[2] = 0.0;
	cpatchCoords[3] = 1.0;
	cpatchCoords[4] = 0.0;
	cpatchCoords[5] = 0.0;
	cpatchCoords[6] = 2.0;
	cpatchCoords[7] = 0.0;
	cpatchCoords[8] = 0.0;
	cpatchCoords[9] = 3.0;
	cpatchCoords[10] = 0.0;
	cpatchCoords[11] = 0.0;
	System.arraycopy(cpatchCoords, 0, cpatchCoords, 12, 12);
	System.arraycopy(cpatchCoords, 0, cpatchCoords, 24, 12);
	System.arraycopy(cpatchCoords, 0, cpatchCoords, 36, 12);
	cpatchCoords[12+1] = 10.0;
	cpatchCoords[12+2] = 100.0;
	cpatchCoords[12+4] = 10.0;
	cpatchCoords[12+5] = 100.0;
	cpatchCoords[12+7] = 10.0;
	cpatchCoords[12+8] = 100.0;
	cpatchCoords[12+10] = 10.0;
	cpatchCoords[12+11] = 100.0;
	cpatchCoords[24+1] = 10.0 * 2;
	cpatchCoords[24+2] = 100.0 * 2;
	cpatchCoords[24+4] = 10.0 * 2;
	cpatchCoords[24+5] = 100.0 * 2;
	cpatchCoords[24+7] = 10.0 * 2;
	cpatchCoords[24+8] = 100.0 * 2;
	cpatchCoords[24+10] = 10.0 * 2;
	cpatchCoords[24+11] = 100.0 * 2;
	cpatchCoords[36+1] = 10.0 * 3;
	cpatchCoords[36+2] = 100.0 * 3;
	cpatchCoords[36+4] = 10.0 * 3;
	cpatchCoords[36+5] = 100.0 * 3;
	cpatchCoords[36+7] = 10.0 * 3;
	cpatchCoords[36+8] = 100.0 * 3;
	cpatchCoords[36+10] = 10.0 * 3;
	cpatchCoords[36+11] = 100.0 * 3;

	cpatch.addCubicPatch(cpatchCoords);
	System.out.println("... cpatch created");
	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}

	Path3D boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

        // System.out.println("... boundary segments:");
	// Path3DInfo.printSegments("    ", System.out, boundary);

	Surface3D.Boundary b =
	    new Surface3D.Boundary(cpatch.getSurfaceIterator(null));
	Path3D bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	PathIterator3D itA = boundary.getPathIterator(null);
	PathIterator3D itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}

	double[] ctcoords = new double[30];

	System.arraycopy(cpatchCoords, 0, ctcoords, 0, 12);
	
	ctcoords[27] = 1.5;
	ctcoords[28] = 0.0;
	ctcoords[29] = 0.0;
	double[] tmp = Path3D.setupCubic(0.0, 0.0, 0.0, 1.5, 0.0, 0.0);
	System.arraycopy(tmp, 3, ctcoords, 12, 3);
	System.arraycopy(tmp, 6, ctcoords, 21, 3);
	tmp = Path3D.setupCubic(3.0, 0.0, 0.0, 1.5, 0.0, 0.0);
	System.arraycopy(tmp, 3, ctcoords, 18, 3);
	System.arraycopy(tmp, 6, ctcoords, 24, 3);
	ctcoords[15] = (ctcoords[12] + ctcoords[18])/2.0;
	ctcoords[16] = 0.0;
	ctcoords[17] = 0.0;

	Surface3D tpatch = new Surface3D.Double();
	tpatch.addPlanarTriangle(ctcoords[0], ctcoords[1], ctcoords[2],
				ctcoords[27], ctcoords[28], ctcoords[29],
				ctcoords[9], ctcoords[10], ctcoords[11]);
	SurfaceIterator tpsi = tpatch.getSurfaceIterator(null);
	double[] tpcoords = new double[48];
	if (tpsi.currentSegment(tpcoords) != SurfaceIterator.PLANAR_TRIANGLE) {
	    System.out.println(" ... tpsi gave the wrong type");
	    System.exit(1);
	}
	if (tpcoords[0] != ctcoords[0] || tpcoords[1] != ctcoords[1]
	    || tpcoords[2] != ctcoords[2]
	    || tpcoords[3] != ctcoords[9] || tpcoords[4] != ctcoords[10]
	    || tpcoords[5] != ctcoords[11]
	    || tpcoords[6] != ctcoords[27] || tpcoords[7] != ctcoords[28]
	    || tpcoords[8] != ctcoords[29]) {
	    System.out.println(" ... tpsi gave the wrong coords");
	    System.exit(1);
	}
	Point3D pt1 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
					     tpcoords, 0.0, 0.0);
	Point3D pt2 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
					     tpcoords, 1.0, 0.0);
	Point3D pt3 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
					     tpcoords, 0.0, 1.0);
	if (pt1.getX() != tpcoords[0] || pt1.getY() != tpcoords[1]
	    || pt1.getZ() != tpcoords[2]
	    || pt2.getX() != tpcoords[6] || pt2.getY() != tpcoords[7]
	    || pt2.getZ() != tpcoords[8]
	    || pt3.getX() != tpcoords[3] || pt3.getY() != tpcoords[4]
	    || pt3.getZ() != tpcoords[5]) {
	    System.out.println("segmentValue for tpcoords failed at vertices");
	    System.exit(1);
	}

	for (int i = 0; i <= 10; i++) {
	    double u = i/10.0;
	    if (i == 0) u = 0.0;
	    if (i == 10) u = 1.0;
	    for (int j = 0; j <= 10; j++) {
		double v = j/10.0;
		if (j == 0) v = 0.0;
		if (j == 10) v = 1.0;
		if (u + v > 1.0) continue;
		Point3D pt4 =
		    Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
					   tpcoords, u, v);
		double ptmatrix[][] = {
		    {pt2.getX()-pt1.getX(), pt2.getY()-pt1.getY(),
		     pt2.getZ()-pt1.getZ()},
		    {pt3.getX()-pt1.getX(), pt3.getY()-pt1.getY(),
		     pt3.getZ()-pt1.getZ()},
		    {pt4.getX()-pt1.getX(), pt4.getY()-pt1.getY(),
		     pt4.getZ()-pt1.getZ()}
		};
		LUDecomp lud = new LUDecomp(ptmatrix);
		if (lud.isNonsingular()) {
		    System.out.format("computed point for (%g, %g) not on the "
				      + "planar triangle", u, v);
		    System.exit(1);
		}
		double[] tangent = new double[3];
		Surface3D.uTangent(tangent, SurfaceIterator.PLANAR_TRIANGLE,
				   tpcoords, u, v);
		double ptmatrix1[][] = {
		    {pt2.getX()-pt1.getX(), pt2.getY()-pt1.getY(),
		     pt2.getZ()-pt1.getZ()},
		    {pt3.getX()-pt1.getX(), pt3.getY()-pt1.getY(),
		     pt3.getZ()-pt1.getZ()},
		    {tangent[0], tangent[1], tangent[2]}
		};
		LUDecomp lud1 = new LUDecomp(ptmatrix1);
		if (lud.isNonsingular()) {
		    System.out.format("u-tangent for (%g, %g) not parallel to "
				      + "the planar triangle", u, v);
		    System.exit(1);
		}
		Surface3D.vTangent(tangent, SurfaceIterator.PLANAR_TRIANGLE,
				   tpcoords, u, v);
		double ptmatrix2[][] = {
		    {pt2.getX()-pt1.getX(), pt2.getY()-pt1.getY(),
		     pt2.getZ()-pt1.getZ()},
		    {pt3.getX()-pt1.getX(), pt3.getY()-pt1.getY(),
		     pt3.getZ()-pt1.getZ()},
		    {tangent[0], tangent[1], tangent[2]}
		};
		LUDecomp lud2 = new LUDecomp(ptmatrix2);
		if (lud.isNonsingular()) {
		    System.out.format("v-tangent for (%g, %g) not parallel to "
				      + "the planar triangle", u, v);
		    System.exit(1);
		}
	    }
	}

	cpatch.addCubicTriangle(ctcoords);

	System.out.println("... cubic triangle added");
	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}
	boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	// System.out.println("... boundary segments:");
	// Path3DInfo.printSegments("    ", System.out, boundary);

	b = new Surface3D.Boundary(cpatch.getSurfaceIterator(null));
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}

	PathIterator3D pi3 = boundary.getPathIterator(null);
	double[] pcoords = new double[48];
	double lastX = 0.0, lastY = 0.0, lastZ = 0.0;
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	b = new Surface3D.Boundary(cpatch.getSurfaceIterator(null));
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}

	Rectangle3D bb =cpatch.getBounds();
	System.out.format("bounding box: (%g, %g, %g) <---> (%g, %g, %g)\n",
			  bb.getMinX(), bb.getMinY(), bb.getMinZ(),
			  bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

	System.out.println("Reverse orientation for cpatch");

	cpatch.reverseOrientation();
          
	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}
	boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	// System.out.println("... boundary segments:");
	// Path3DInfo.printSegments("    ", System.out, boundary);
	pi3 = boundary.getPathIterator(null);
	pcoords = new double[9];
	lastX = 0.0; lastY = 0.0; lastZ = 0.0;
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	b = new Surface3D.Boundary(cpatch.getSurfaceIterator(null));
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}

	Surface3D cylinder = new Surface3D.Double();
	double r = 100.0;
	double[] xs = new double[36];
	double[] ys = new double[36];
	for (int i = 0; i < 36; i++) {
	    double theta = i*Math.PI/18;
	    xs[i] = r * Math.cos(theta);
	    ys[i] = r * Math.sin(theta);
	}
	Path2D path2d = new SplinePath2D(xs, ys, true);
	
	double coords[] = new double[48];
	double coords2[] = new double[6];
	for (int j = 0; j < 30; j++) {
	    double z = j*30;
	    PathIterator pi = path2d.getPathIterator(null);
	    int type = pi.currentSegment(coords2);
	    if (type != PathIterator.SEG_MOVETO)
		throw new Exception("MOVETO expected");
	    coords[0] = coords2[0];
	    coords[1] = coords2[1];
	    coords[2] = z;
	    pi.next();
	    while (pi.isDone() == false) {
		type = pi.currentSegment(coords2);
		switch(type) {
		case PathIterator.SEG_CUBICTO:
		    coords[3] = coords2[0];
		    coords[4] = coords2[1];
		    coords[5] = z;
		    coords[6] = coords2[2];
		    coords[7] = coords2[3];
		    coords[8] = z;
		    coords[9] = coords2[4];
		    coords[10] = coords2[5];
		    coords[11] = z;
		    for (int k = 1; k < 4; k++) {
			double zz = z + k*10;
			coords[k*12] = coords[0];
			coords[k*12+1] = coords[1];
			coords[k*12+2] = zz;
			coords[k*12+3] = coords[3];
			coords[k*12+4] = coords[4];
			coords[k*12+5] = zz;
			coords[k*12+6] = coords[6];
			coords[k*12+7] = coords[7];
			coords[k*12+8] = zz;
			coords[k*12+9] = coords[9];
			coords[k*12+10] = coords[10];
			coords[k*12+11] = zz;
		    }
		    cylinder.addCubicPatch(coords);
		    coords[0] = coords2[4];
		    coords[1] = coords2[5];
		    coords[2] = z;
		    break;
		case PathIterator.SEG_CLOSE:
		    break;
		default:
		    throw new Exception("... unexpected type "
					+ Path2DInfo.getTypeString(type));
		}
		pi.next();
	    }
	}
	System.out.println("... cylinder created");
	if (!cylinder.isWellFormed(System.out)) {
	    System.out.println("... cylinder not well formed");
	    System.exit(1);
	}
	boundary = cylinder.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	System.out.println("... boundary segments:");
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	b = new Surface3D.Boundary(cylinder.getSurfaceIterator(null));
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}


	System.out.println("test non-oriented surface (moebius strip):");
	Surface3D mstrip = new Surface3D.Double(false);

	double xc = 100.0;
	double yc = 0.0;
	double hw = 10.0;


	for (int i = 0; i < 100; i++) {
	    double angle1 = i*(Math.PI/50);
	    double angle2 = (i)*(Math.PI/100.0);
	    if (i == 0) {angle1 = 0.0; angle2 = 0.0;}
	    double angle1n = (i+1)*(Math.PI/50);
	    double angle2n = (i+1)*(Math.PI/100.0);
	    if (i+1 == 100) {angle1n = 0.0; angle2n = Math.PI;}

	    double x1 = 100.0 * Math.cos(angle1)
		-10.0*Math.cos(angle1)*Math.cos(angle2);
	    double y1 = 100.0 * Math.sin(angle1)
		-10.0*Math.sin(angle1)*Math.cos(angle2);
	    double z1 = -10.0*Math.sin(angle2);

	    double x2 = 100.0 * Math.cos(angle1)
		+10.0*Math.cos(angle1)*Math.cos(angle2);
	    double y2 = 100.0 * Math.sin(angle1)
		+10.0*Math.sin(angle1)*Math.cos(angle2);
	    double z2 = 10.0*Math.sin(angle2);

	    double x1n = 100.0 * Math.cos(angle1n)
		-10.0*Math.cos(angle1n)*Math.cos(angle2n);
	    double y1n = 100.0 * Math.sin(angle1n)
		-10.0*Math.sin(angle1n)*Math.cos(angle2n);
	    double z1n = -10.0*Math.sin(angle2n);

	    double x2n = 100.0 * Math.cos(angle1n)
		+10.0*Math.cos(angle1n)*Math.cos(angle2n);
	    double y2n = 100.0 * Math.sin(angle1n)
		+10.0*Math.sin(angle1n)*Math.cos(angle2n);
	    double z2n = 10.0*Math.sin(angle2n);
	    mstrip.addPlanarTriangle(x1, y1, z1, x2, y2, z2, x1n, y1n, z1n);
	    mstrip.addPlanarTriangle(x1n, y1n, z1n, x2, y2, z2, x2n, y2n, z2n);
	}

	System.out.println("... Moebius strip created");
	if (!mstrip.isWellFormed(System.out)) {
	    System.out.println("... Moebius strip not well formed");
	    System.exit(1);
	}
	boundary = mstrip.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected [mstrip 1]");
	    System.exit(1);
	}
	if (boundary.isEmpty()) {
	    System.out.println("... empty boundary unexpected [mstrip 1]");
	    System.exit(1);
	}
	System.out.println("... boundary segments:");
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		System.out.format("lineto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	b = new Surface3D.Boundary(mstrip.getSurfaceIterator(null), System.out);
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected [mstrip bpath]");
	    System.exit(1);
	}
	if (bpath.isEmpty()) {
	    System.out.println("... empty boundary unexpected [mstrip bpath]");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}

	double ccoords[] = new double[30];

	// Add top and bottom.
	System.out.println("add a top and bottom to the cylinder:");
	boundary = cylinder.getBoundary();
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		Surface3D.setupV0ForTriangle(lastX, lastY, lastZ, pcoords,
					     ccoords, true);
		Surface3D.setupU0ForTriangle(Path3D.setupCubic(pcoords[6],
							       pcoords[7],
							       lastZ,
							       0.0, 0.0, lastZ),
					     ccoords, false);
		Surface3D.setupW0ForTriangle(Path3D.setupCubic(lastX, lastY,
							       lastZ,
							       0.0, 0.0, lastZ),
					     ccoords, false);
		Surface3D.setupPlanarCP111ForTriangle(ccoords);
		cylinder.addCubicTriangle(ccoords);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}
	if (!cylinder.isWellFormed(System.out)) {
	    System.out.println("... cylinder not well formed");
	    System.exit(1);
	}
	
	if (!cylinder.isClosedManifold()) {
	    System.out.println("... cylinder not a closed manifold");
	    System.exit(1);
	}

	b = new Surface3D.Boundary(cylinder.getSurfaceIterator(null));
	bpath = b.getPath();
	itA = bpath.getPathIterator(null);
	if (!itA.isDone()) {
	    throw new Exception("bpath is not empty");
	}


	System.out.println("... OK");

	System.out.println("planar triangle test:");
	Surface3D surface = new Surface3D.Double();
	surface.addPlanarTriangle(0.0, 0.0, 0.0, 20.0, 30.0, 40.0,
				 100.0, 35.0, 45.0);
	surface.addPlanarTriangle(20.0, 30.0, 40.0, 0.0, 0.0, 0.0,
				 -100.0, -35.0, -45.0);
	if (!surface.isWellFormed(System.out)) {
	    System.out.println ("... failed");
	    System.exit(1);
	}
	boundary = surface.getBoundary();
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		System.out.format("lineto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	b = new Surface3D.Boundary(surface.getSurfaceIterator(null));
	bpath = b.getPath();
	if (bpath == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	itA = boundary.getPathIterator(null);
	itB = bpath.getPathIterator(null);
	while(!itA.isDone()) {
	    double[] coordsA = new double[48];
	    double[] coordsB = new double[48];
	    int type1 = itA.currentSegment(coordsA);
	    int type2 = itB.currentSegment(coordsB);
	    if (type1 != type2) {
		throw new Exception("types differ");
	    }
	    for (int i = 0; i < 48; i++) {
		if (Math.abs(coordsA[i] - coordsB[i]) > 1.e-15) {
		    throw new Exception("coords differ");
		}
	    }
	    itA.next(); itB.next();
	    if (itA.isDone() != itB.isDone()) {
		throw new Exception("itA and itB differ in length");
	    }
	}


	System.out.println("... OK");

	System.out.println("check setupRestForTriangle methods:");

	double[] tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 10.0, 50.0, 100.0);
	Surface3D.setupU0ForTriangle(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 20.0, 60.0, 90.0);
	Surface3D.setupV0ForTriangle(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(20.0, 60.0, 90.0, 10.0, 50.0, 100.0);
	Surface3D.setupW0ForTriangle(tcoords, ccoords, false);
	Surface3D.setupPlanarCP111ForTriangle(ccoords);
	double[] n = {45.0-60.0, 20.0-9.0, 6.0-10.0};
	for (int i = 0; i < 30; i+=3) {
	    double v[] = {ccoords[i], ccoords[i+1], ccoords[i+2]};
	    if (Math.abs(n[0]*v[0] + n[1]*v[1] + n[2]*v[2]) > 1.e-10) {
		System.out.println("... CP "  + i/3
				   + " = ( " + v[0] +", " + v[1] + ", " + v[2]
				   + ") not in plane of triangle");
		System.exit(1);
	    }
	}
	System.out.println("... OK");

	System.out.println("Try a couple of segments from a sphere");

	// sphere in polar coords near a pole.

	double[] x1s = new double[36];
	double[] y1s = new double[36];
	double[] z1s = new double[36];

	double[] x2s = new double[36];
	double[] y2s = new double[36];
	double[] z2s = new double[36];

	double[] x3s = new double[36];
	double[] y3s = new double[36];
	double[] z3s = new double[36];

	double[] x4s = new double[36];
	double[] y4s = new double[36];
	double[] z4s = new double[36];

	double theta10 = Math.toRadians(10.0);
	double theta20 = Math.toRadians(20.0);

	for (int i = 0; i < 36; i++) {
	    double theta = Math.toRadians(i*10.0);
	    double phi1 = 0.0;
	    double phi2 = Math.toRadians(10.0);
	    double phi3 = Math.toRadians(i*10.0);
	    x1s[i] = r * Math.sin(theta);
	    y1s[i] =  0.0;
	    z1s[i] = r * Math.cos(theta);

	    x2s[i] = r * Math.cos(phi2)*Math.sin(theta);
	    y2s[i] = r * Math.sin(phi2)*Math.sin(theta);
	    z2s[i] = r * Math.cos(theta);

	    x3s[i] = r * Math.cos(phi3) * Math.sin(theta10);
	    y3s[i] = r * Math.sin(phi3) * Math.sin(theta10);
	    z3s[i] = r * Math.cos(theta10);

	    x4s[i] = r * Math.cos(phi3) * Math.sin(theta20);
	    y4s[i] = r * Math.sin(phi3) * Math.sin(theta20);
	    z4s[i] = r * Math.cos(theta20);
	}

	Path3D long1 = new SplinePath3D(x1s, y1s, z1s, true);
	Path3D long2 = new SplinePath3D(x2s, y2s, z2s, true);
	Path3D lat1 = new SplinePath3D(x3s, y3s, z3s, true);
	Path3D lat2 = new SplinePath3D(x4s,y4s, z4s, true);

	double[] longCoords1a = new double[12];
	double[] longCoords1b = new double[12];
	double[] longCoords2a = new double[12];
	double[] longCoords2b = new double[12];
	double[] latCoords1 = new double[12];
	double[] latCoords2 = new double[12];

	pi3 = long1.getPathIterator(null);
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_MOVETO) {
	    lastX = pcoords[0];
	    lastY = pcoords[1];
	    lastZ = pcoords[2];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    longCoords1a[0] = lastX;
	    longCoords1a[1] = lastY;
	    longCoords1a[2] = lastZ;
	    System.arraycopy(pcoords, 0, longCoords1a, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    longCoords1b[0] = lastX;
	    longCoords1b[1] = lastY;
	    longCoords1b[2] = lastZ;
	    System.arraycopy(pcoords, 0, longCoords1b, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}

	pi3 = long2.getPathIterator(null);
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_MOVETO) {
	    lastX = pcoords[0];
	    lastY = pcoords[1];
	    lastZ = pcoords[2];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    longCoords2a[0] = lastX;
	    longCoords2a[1] = lastY;
	    longCoords2a[2] = lastZ;
	    System.arraycopy(pcoords, 0, longCoords2a, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    longCoords2b[0] = lastX;
	    longCoords2b[1] = lastY;
	    longCoords2b[2] = lastZ;
	    System.arraycopy(pcoords, 0, longCoords2b, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}

	pi3 = lat1.getPathIterator(null);
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_MOVETO) {
	    lastX = pcoords[0];
	    lastY = pcoords[1];
	    lastZ = pcoords[2];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    latCoords1[0] = lastX;
	    latCoords1[1] = lastY;
	    latCoords1[2] = lastZ;
	    System.arraycopy(pcoords, 0, latCoords1, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}

	pi3 = lat2.getPathIterator(null);
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_MOVETO) {
	    lastX = pcoords[0];
	    lastY = pcoords[1];
	    lastZ = pcoords[2];
	} else {
	    throw new Exception();
	}
	pi3.next();
	if (pi3.currentSegment(pcoords) == PathIterator3D.SEG_CUBICTO) {
	    latCoords2[0] = lastX;
	    latCoords2[1] = lastY;
	    latCoords2[2] = lastZ;
	    System.arraycopy(pcoords, 0, latCoords2, 3, 9);
	    lastX = pcoords[6];
	    lastY = pcoords[7];
	    lastZ = pcoords[8];
	} else {
	    throw new Exception();
	}

	Surface3D.setupU0ForTriangle(longCoords1a, ctcoords, true);
	Surface3D.setupV0ForTriangle(latCoords1, ctcoords, false);
	Surface3D.setupW0ForTriangle(longCoords2a, ctcoords, true);
	// Surface3D.setupRestForTriangle(ctcoords);
	double xct = r * Math.sin(Math.toRadians(20.0/3))
	    *  Math.cos(Math.toRadians(10.0/3));
	double yct = r * Math.sin(Math.toRadians(20.0/3))
	    *  Math.sin(Math.toRadians(10.0/3));
	double zct = r * Math.cos(Math.toRadians(20.0/3));
	Surface3D.setupCP111ForTriangle(xct, yct, zct, ctcoords);

	double xcp111 = ctcoords[15];
	double ycp111 = ctcoords[16];
	double zcp111 = ctcoords[17];
	Surface3D.setupCP111ForTriangle(xct, yct, zct, ctcoords,
					1.0/3.0, 1.0/3.0);
	if (Math.abs(xcp111 - ctcoords[15]) > 1.e-10
	    || Math.abs(ycp111 - ctcoords[16]) > 1.e-10
	    || Math.abs(zcp111 - ctcoords[17]) > 1.e-10) {
	    System.out.println("setupCPP111ForTriangle with u,v failed");
	    System.exit(1);
	}

	System.out.format("xct = %g, yct = %g, zct = %g; r = %g\n",
			  xct, yct, zct,
			  Math.sqrt(xct*xct + yct*yct + zct*zct));
	double[] xyz = new double[3];
	Surface3D.segmentValue(xyz, SurfaceIterator.CUBIC_TRIANGLE, ctcoords,
			       1.0/3.0, 1.0/3.0);
	if (Math.abs(xct - xyz[0]) > 1.e-10
	    || Math.abs(yct - xyz[1]) > 1.e-10
	    || Math.abs(zct - xyz[2]) > 1.e-10) {
	    System.out.format("bad CP111: xyz values are (%g, %g, %g)\n",
			      xyz[0], xyz[1], xyz[2]);
	}
	System.out.println("ctcoords:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("CP%d = (%g,%g,%g), r = %g\n", i/3,
			      ctcoords[i], ctcoords[i+1], ctcoords[i+2],
			      Point3D.distance(0.0, 0.0, 0.0,
					       ctcoords[i],
					       ctcoords[i+1],
					       ctcoords[i+2]));
	}
	int errcount = 0;
	double[] values = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8,
			   0.9, 1.0};
	System.out.println("  Test CUBIC_TRIANGLE:");
	System.out.println(" ... setupCP111ForTriangle with multiple args");
	for (double u: values) {
	    for (double v: values) {
		if (u + v > 1.0) continue;
		Point3D p =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u, v);
		double w = 1.0 - (u + v);

		Point3D pb =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u, v, w);

		if (Math.abs(p.getX() - pb.getX()) > 1.e-10
		    || Math.abs(p.getY() - pb.getY()) > 1.e-10
		    || Math.abs(p.getZ() - pb.getZ()) > 1.e-10) {
		    System.out.format ("p != pb: "
				       + "(%g, %g, %g) != (%g, %g, %g)\n",
				       p.getX(), p.getY(), p.getZ(),
				       pb.getX(), pb.getY(), pb.getZ());
		    errcount++;
		}

		double radius = p.distance(0.0, 0.0, 0.0);
		double rr = p.distance(0.0, 0.0, p.getZ());
		double phi = Math.toDegrees(Math.atan2(p.getY(), p.getX()));
		double theta = Math.toDegrees(Math.atan2(rr, p.getZ()));

		if (Math.abs(radius - 100.0) > 0.1) {
		    System.out.format("radius = %g for (u,v) = (%g,%g)\n",
				      radius, u, v);
		    errcount++;
		}

		double delta = 0.001;
		double test = 10*delta;
		double tangent[] = new double[3];
		double tangentd[] = new double[3];
		Surface3D.uTangent(tangent, SurfaceIterator.CUBIC_TRIANGLE,
				   ctcoords, u, v);
		Point3D pd =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u+delta, v);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.format("case u: tangent and tangentd differ "
				      + "for (u,v) = (%g, %g), test = %g\n",
				      u, v, test);
		    System.out.format("        tangent[0]=%g, "
				      + "tangentd[0]=%g\n",
				      tangent[0], tangentd[0]);
		    System.out.format("        tangent[1]=%g, "
				      + "tangentd[1]=%g\n",
				      tangent[1], tangentd[1]);
		    System.out.format("        tangent[2]=%g, "
				      + "tangentd[2]=%g\n",
				      tangent[2], tangentd[2]);
		    errcount++;
		}
		double dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		if (Math.abs(dotprod) > 2.5) {
		    System.out.format("u-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}

		Surface3D.vTangent(tangent, SurfaceIterator.CUBIC_TRIANGLE,
				   ctcoords, u, v);
		pd = Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					    ctcoords, u, v+delta);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		test *= 2.0;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.format("case v: tangent and tangentd differ "
				      + "for (u,v) = (%g,%g), test = %g\n",
				      u, v, test);
		    System.out.format("        tangent[0]=%g, "
				      + "tangentd[0]=%g\n",
				      tangent[0], tangentd[0]);
		    System.out.format("        tangent[1]=%g, "
				      + "tangentd[1]=%g\n",
				      tangent[1], tangentd[1]);
		    System.out.format("        tangent[2]=%g, "
				      + "tangentd[2]=%g\n",
				      tangent[2], tangentd[2]);
		    errcount++;
		}
		dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		// 2.5 is small compared the to product of a radial vector
		// with itself, which has the value 10000.0 if the surfae
		// was a perfect sphere.
		if (Math.abs(dotprod) > 2.5) {
		    System.out.format("v-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}
		if (phi < -1.e-10 || phi > 10.0 + 1.e-10) {
		    System.out.format("phi = %g degrees for (u,v) = (%g,%g)\n",
				      phi, u, v);
		    errcount++;
		}
		if (theta < -1.0e-10 || theta > 10.0 + 1.0e-10) {
		    System.out.format("theta = %g degrees for "
				      + "(u,v) = (%g,%g)\n",
				      theta, u, v);
		    errcount++;
		}
	    }
	}

	System.out.println("... test setupCP111ForTriangle with one arg");
	Surface3D.setupCP111ForTriangle(ctcoords);
	for (double u: values) {
	    for (double v: values) {
		if (u + v > 1.0) continue;
		Point3D p =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u, v);
		double w = 1.0 - (u + v);

		Point3D pb =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u, v, w);

		if (Math.abs(p.getX() - pb.getX()) > 1.e-10
		    || Math.abs(p.getY() - pb.getY()) > 1.e-10
		    || Math.abs(p.getZ() - pb.getZ()) > 1.e-10) {
		    System.out.format ("p != pb: "
				       + "(%g, %g, %g) != (%g, %g, %g)\n",
				       p.getX(), p.getY(), p.getZ(),
				       pb.getX(), pb.getY(), pb.getZ());
		    errcount++;
		}

		double radius = p.distance(0.0, 0.0, 0.0);
		double rr = p.distance(0.0, 0.0, p.getZ());
		double phi = Math.toDegrees(Math.atan2(p.getY(), p.getX()));
		double theta = Math.toDegrees(Math.atan2(rr, p.getZ()));

		if (Math.abs(radius - 100.0) > 0.002) {
		    System.out.format("radius = %g for (u,v) = (%g,%g)\n",
				      radius, u, v);
		    errcount++;
		}

		double delta = 0.001;
		double test = 0.8*delta;
		double tangent[] = new double[3];
		double tangentd[] = new double[3];
		Surface3D.uTangent(tangent, SurfaceIterator.CUBIC_TRIANGLE,
				   ctcoords, u, v);
		Point3D pd =
		    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					   ctcoords, u+delta, v);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.format("case u: tangent and tangentd differ "
				      + "for (u,v) = (%g, %g), test = %g\n",
				      u, v, test);
		    System.out.format("        tangent[0]=%g, "
				      + "tangentd[0]=%g\n",
				      tangent[0], tangentd[0]);
		    System.out.format("        tangent[1]=%g, "
				      + "tangentd[1]=%g\n",
				      tangent[1], tangentd[1]);
		    System.out.format("        tangent[2]=%g, "
				      + "tangentd[2]=%g\n",
				      tangent[2], tangentd[2]);
		    errcount++;
		}
		double dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		if (Math.abs(dotprod) > 1.2) {
		    System.out.format("u-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}

		Surface3D.vTangent(tangent, SurfaceIterator.CUBIC_TRIANGLE,
				   ctcoords, u, v);
		pd = Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE,
					    ctcoords, u, v+delta);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		test *= 2.0;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.format("case v: tangent and tangentd differ "
				      + "for (u,v) = (%g,%g), test = %g\n",
				      u, v, test);
		    System.out.format("        tangent[0]=%g, "
				      + "tangentd[0]=%g\n",
				      tangent[0], tangentd[0]);
		    System.out.format("        tangent[1]=%g, "
				      + "tangentd[1]=%g\n",
				      tangent[1], tangentd[1]);
		    System.out.format("        tangent[2]=%g, "
				      + "tangentd[2]=%g\n",
				      tangent[2], tangentd[2]);
		    errcount++;
		}
		dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		// 2.5 is small compared the to product of a radial vector
		// with itself, which has the value 10000.0 if the surfae
		// was a perfect sphere.
		if (Math.abs(dotprod) > 1.4) {
		    System.out.format("v-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}
		if (phi < -1.e-10 || phi > 10.0 + 1.e-10) {
		    System.out.format("phi = %g degrees for (u,v) = (%g,%g)\n",
				      phi, u, v);
		    errcount++;
		}
		if (theta < -1.0e-10 || theta > 10.0 + 1.0e-10) {
		    System.out.format("theta = %g degrees for "
				      + "(u,v) = (%g,%g)\n",
				      theta, u, v);
		    errcount++;
		}
	    }
	}

	System.out.println("  Test CUBIC_PATCH:");

	double[] cpcoords = new double[48];
	Surface3D.setupU0ForPatch(longCoords1b, cpcoords, true);
	Surface3D.setupU1ForPatch(longCoords2b, cpcoords, true);
	Surface3D.setupV1ForPatch(latCoords1, cpcoords, false);
	Surface3D.setupV0ForPatch(latCoords2, cpcoords, false);
	Surface3D.setupRestForPatch(cpcoords);

	/*
	System.out.println("cpcoords:");
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("CP%d = (%g,%g,%g), r = %g\n", i/3,
			      cpcoords[i], cpcoords[i+1], cpcoords[i+2],
			      Point3D.distance(0.0, 0.0, 0.0,
					       cpcoords[i],
					       cpcoords[i+1],
					       cpcoords[i+2]));
	}
	*/

	for (double u: values) {
	    for (double v: values) {
		Point3D p = Surface3D.segmentValue(SurfaceIterator.CUBIC_PATCH,
						   cpcoords, u, v);
		double delta = 0.001;
		double test = delta;
		double radius = p.distance(0.0, 0.0, 0.0);
		double tangent[] = new double[3];
		double tangentd[] = new double[3];
		Surface3D.uTangent(tangent, SurfaceIterator.CUBIC_PATCH,
				   cpcoords, u, v);
		Point3D pd = Surface3D.segmentValue(SurfaceIterator.CUBIC_PATCH,
						   cpcoords, u+delta, v);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.println("case u: tangent and tangentd differ");
		    errcount++;
		}
		double dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		if (Math.abs(dotprod) > 2.0) {
		    System.out.format("u-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}

		Surface3D.vTangent(tangent, SurfaceIterator.CUBIC_PATCH,
				   cpcoords, u, v);
		pd = Surface3D.segmentValue(SurfaceIterator.CUBIC_PATCH,
					    cpcoords, u, v+delta);
		tangentd[0] = (pd.getX() - p.getX())/delta;
		tangentd[1] = (pd.getY() - p.getY())/delta;
		tangentd[2] = (pd.getZ() - p.getZ())/delta;
		test *= 2.0;
		if (Math.abs(tangent[0] - tangentd[0]) > test
		    || Math.abs(tangent[1] - tangentd[1]) > test
		    || Math.abs(tangent[2] - tangentd[2]) > test) {
		    System.out.format("case v: tangent and tangentd differ "
				      + "for (u,v) = (%g,%g), test = %g\n",
				      u, v, test);
		    System.out.format("        tangent[0]=%g, "
				      + "tangentd[0]=%g\n",
				      tangent[0], tangentd[0]);
		    System.out.format("        tangent[1]=%g, "
				      + "tangentd[1]=%g\n",
				      tangent[1], tangentd[1]);
		    System.out.format("        tangent[2]=%g, "
				      + "tangentd[2]=%g\n",
				      tangent[2], tangentd[2]);
		    errcount++;
		}
		dotprod = tangent[0]*p.getX() + tangent[1]*p.getY()
		    + tangent[2]*p.getZ();
		// 2.5 is small compared the to product of a radial vector
		// with itself, which has the value 10000.0 if the surfae
		// was a perfect sphere.
		if (Math.abs(dotprod) > 2.5) {
		    System.out.format("v-tangent at (u,v) = (%g, %g) not "
				      + "perpendicular to surface "
				      + "(dotprod = %g)\n", u, v, dotprod);
		    errcount++;
		}

		double rr = p.distance(0.0, 0.0, p.getZ());
		double phi = Math.toDegrees(Math.atan2(p.getY(), p.getX()));
		double theta = Math.toDegrees(Math.atan2(rr, p.getZ()));

		if (Math.abs(radius - 100.0) > 0.1) {
		    System.out.format("radius = %g for (u,v) = (%g,%g)\n",
				      radius, u, v);
		    errcount++;
		}
		if (phi < -1.e-10 || phi > 10.0 + 1.e-10) {
		    System.out.format("phi = %g degrees for (u,v) = (%g,%g)\n",
				      phi, u, v);
		    errcount++;
		}
		if (theta < 10.0-1.0e-4 || theta > 20.0 + 1.0e-4) {
		    System.out.format("theta = %g degrees for "
				      + "(u,v) = (%g,%g)\n",
				      theta, u, v);
		    errcount++;
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("Now test Surface3D.Float");

	cpatch = new Surface3D.Float();

	cpatch.addCubicPatch(cpatchCoords);
	System.out.println("... cpatch created");
	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}
	boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}

	tpatch = new Surface3D.Float();
	tpatch.addPlanarTriangle(ctcoords[0], ctcoords[1], ctcoords[2],
				ctcoords[27], ctcoords[28], ctcoords[29],
				ctcoords[9], ctcoords[10], ctcoords[11]);
	tpsi = tpatch.getSurfaceIterator(null);
	if (tpsi.currentSegment(tpcoords) != SurfaceIterator.PLANAR_TRIANGLE) {
	    System.out.println(" ... tpsi gave the wrong type");
	    System.exit(1);
	}
	if (Math.abs(tpcoords[0] - ctcoords[0]) > 1.e-6
	    || Math.abs(tpcoords[1] - ctcoords[1]) > 1.e-6
	    || Math.abs(tpcoords[2] - ctcoords[2]) > 1.e-5
	    || Math.abs(tpcoords[3] - ctcoords[9]) > 1.e-5
	    || Math.abs(tpcoords[4] - ctcoords[10]) > 1.e-5
	    || Math.abs(tpcoords[5] - ctcoords[11]) > 1.e-5
	    || Math.abs(tpcoords[6] - ctcoords[27]) > 1.e-5
	    || Math.abs(tpcoords[7] - ctcoords[28]) > 1.e-5
	    || Math.abs(tpcoords[8] - ctcoords[29]) > 1.e-5) {
	    System.out.println(" ... tpsi gave the wrong coords");
	    System.exit(1);
	}
	pt1 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
				     tpcoords, 0.0, 0.0);
	pt2 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
				     tpcoords, 1.0, 0.0);
	pt3 = Surface3D.segmentValue(SurfaceIterator.PLANAR_TRIANGLE,
				     tpcoords, 0.0, 1.0);
	if (pt1.getX() != tpcoords[0] || pt1.getY() != tpcoords[1]
	    || pt1.getZ() != tpcoords[2]
	    || pt2.getX() != tpcoords[6] || pt2.getY() != tpcoords[7]
	    || pt2.getZ() != tpcoords[8]
	    || pt3.getX() != tpcoords[3] || pt3.getY() != tpcoords[4]
	    || pt3.getZ() != tpcoords[5]) {
	    System.out.println("segmentValue for tpcoords failed at vertices");
	    System.exit(1);
	}


	cpatch.addCubicTriangle(ctcoords);

	System.out.println("... cubic triangle added");
	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}
	boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	// System.out.println("... boundary segments:");
	// Path3DInfo.printSegments("    ", System.out, boundary);
	pi3 = boundary.getPathIterator(null);
	lastX = 0.0; lastY = 0.0; lastZ = 0.0;
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	bb =cpatch.getBounds();
	System.out.format("bounding box: (%g, %g, %g) <---> (%g, %g, %g)\n",
			  bb.getMinX(), bb.getMinY(), bb.getMinZ(),
			  bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

	System.out.println("Reverse orientation for cpatch");

	cpatch.reverseOrientation();

	if (!cpatch.isWellFormed()) {
	    System.out.println("... cpatch not well formed");
	    System.exit(1);
	}
	boundary = cpatch.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	// System.out.println("... boundary segments:");
	// Path3DInfo.printSegments("    ", System.out, boundary);
	pi3 = boundary.getPathIterator(null);
	pcoords = new double[9];
	lastX = 0.0; lastY = 0.0; lastZ = 0.0;
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	cylinder = new Surface3D.Float();

	for (int j = 0; j < 30; j++) {
	    double z = j*30;
	    PathIterator pi = path2d.getPathIterator(null);
	    int type = pi.currentSegment(coords2);
	    if (type != PathIterator.SEG_MOVETO)
		throw new Exception("MOVETO expected");
	    coords[0] = coords2[0];




	    coords[1] = coords2[1];
	    coords[2] = z;
	    pi.next();
	    while (pi.isDone() == false) {
		type = pi.currentSegment(coords2);
		switch(type) {
		case PathIterator.SEG_CUBICTO:
		    coords[3] = coords2[0];
		    coords[4] = coords2[1];
		    coords[5] = z;
		    coords[6] = coords2[2];
		    coords[7] = coords2[3];
		    coords[8] = z;
		    coords[9] = coords2[4];
		    coords[10] = coords2[5];
		    coords[11] = z;
		    for (int k = 1; k < 4; k++) {
			double zz = z + k*10;
			coords[k*12] = coords[0];
			coords[k*12+1] = coords[1];
			coords[k*12+2] = zz;
			coords[k*12+3] = coords[3];
			coords[k*12+4] = coords[4];
			coords[k*12+5] = zz;
			coords[k*12+6] = coords[6];
			coords[k*12+7] = coords[7];
			coords[k*12+8] = zz;
			coords[k*12+9] = coords[9];
			coords[k*12+10] = coords[10];
			coords[k*12+11] = zz;
		    }
		    cylinder.addCubicPatch(coords);
		    coords[0] = coords2[4];
		    coords[1] = coords2[5];
		    coords[2] = z;
		    break;
		case PathIterator.SEG_CLOSE:
		    break;
		default:
		    throw new Exception("... unexpected type "
					+ Path2DInfo.getTypeString(type));
		}
		pi.next();
	    }
	}
	System.out.println("... cylinder created");
	if (!cylinder.isWellFormed(System.out)) {
	    System.out.println("... cylinder not well formed");
	    System.exit(1);
	}
	boundary = cylinder.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	System.out.println("... boundary segments:");
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	System.out.println("test non-oriented surface (moebius strip):");
	mstrip = new Surface3D.Float(false);

	xc = 100.0;
	yc = 0.0;
	hw = 10.0;


	for (int i = 0; i < 100; i++) {
	    double angle1 = i*(Math.PI/50);
	    double angle2 = (i)*(Math.PI/100.0);
	    if (i == 0) {angle1 = 0.0; angle2 = 0.0;}
	    double angle1n = (i+1)*(Math.PI/50);
	    double angle2n = (i+1)*(Math.PI/100.0);
	    if (i+1 == 100) {angle1n = 0.0; angle2n = Math.PI;}

	    double x1 = 100.0 * Math.cos(angle1)
		-10.0*Math.cos(angle1)*Math.cos(angle2);
	    double y1 = 100.0 * Math.sin(angle1)
		-10.0*Math.sin(angle1)*Math.cos(angle2);
	    double z1 = -10.0*Math.sin(angle2);

	    double x2 = 100.0 * Math.cos(angle1)
		+10.0*Math.cos(angle1)*Math.cos(angle2);
	    double y2 = 100.0 * Math.sin(angle1)
		+10.0*Math.sin(angle1)*Math.cos(angle2);
	    double z2 = 10.0*Math.sin(angle2);

	    double x1n = 100.0 * Math.cos(angle1n)
		-10.0*Math.cos(angle1n)*Math.cos(angle2n);
	    double y1n = 100.0 * Math.sin(angle1n)
		-10.0*Math.sin(angle1n)*Math.cos(angle2n);
	    double z1n = -10.0*Math.sin(angle2n);

	    double x2n = 100.0 * Math.cos(angle1n)
		+10.0*Math.cos(angle1n)*Math.cos(angle2n);
	    double y2n = 100.0 * Math.sin(angle1n)
		+10.0*Math.sin(angle1n)*Math.cos(angle2n);
	    double z2n = 10.0*Math.sin(angle2n);
	    mstrip.addPlanarTriangle(x1, y1, z1, x2, y2, z2, x1n, y1n, z1n);
	    mstrip.addPlanarTriangle(x1n, y1n, z1n, x2, y2, z2, x2n, y2n, z2n);
	}

	System.out.println("... Moebius strip created");
	if (!mstrip.isWellFormed(System.out)) {
	    System.out.println("... Moebius strip not well formed");
	    System.exit(1);
	}
	boundary = mstrip.getBoundary();
	if (boundary == null) {
	    System.out.println("... null boundary unexpected");
	    System.exit(1);
	}
	if (boundary.isEmpty()) {
	    System.out.println("... empty boundary unexpected");
	    System.exit(1);
	}
	System.out.println("... boundary segments:");
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		System.out.format("lineto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}
	ccoords = new double[30];

	// Add top and bottom.
	System.out.println("add a top and bottom to the cylinder:");
	boundary = cylinder.getBoundary();
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		Surface3D.setupV0ForTriangle(lastX, lastY, lastZ, pcoords,
					     ccoords, true);
		Surface3D.setupU0ForTriangle(Path3D.setupCubic(pcoords[6],
							       pcoords[7],
							       lastZ,
							       0.0, 0.0, lastZ),
					     ccoords, false);
		Surface3D.setupW0ForTriangle(Path3D.setupCubic(lastX, lastY,
							       lastZ,
							       0.0, 0.0, lastZ),
					     ccoords, false);
		Surface3D.setupPlanarCP111ForTriangle(ccoords);
		cylinder.addCubicTriangle(ccoords);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}
	if (!cylinder.isWellFormed(System.out)) {
	    System.out.println("... cylinder not well formed");
	    System.exit(1);
	}

	if (!cylinder.isClosedManifold()) {
	    System.out.println("... cylinder not a closed manifold");
	    System.exit(1);
	}

	if (errcount > 0) {
	    System.out.println("... failed");
	    System.exit(1);
	} else {
	    System.out.println("... OK");
	}

	System.out.println("planar triangle test:");
	surface = new Surface3D.Float();
	surface.addPlanarTriangle(0.0, 0.0, 0.0, 20.0, 30.0, 40.0,
				 100.0, 35.0, 45.0);
	surface.addPlanarTriangle(20.0, 30.0, 40.0, 0.0, 0.0, 0.0,
				 -100.0, -35.0, -45.0);
	if (!surface.isWellFormed(System.out)) {
	    System.out.println ("... failed");
	    System.exit(1);
	}
	boundary = surface.getBoundary();
	pi3 = boundary.getPathIterator(null);
	while(pi3.isDone() == false) {
	    switch(pi3.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		System.out.format("moveto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_LINETO:
		System.out.format("lineto: (%g, %g, %g)\n",
				  pcoords[0], pcoords[1], pcoords[2]);
		lastX = pcoords[0];
		lastY = pcoords[1];
		lastZ = pcoords[2];
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.format("cubicto: (%g, %g, %g) --> (%g, %g, %g)\n",
				  lastX, lastY, lastZ,
				  pcoords[6], pcoords[7], pcoords[8]);
		lastX = pcoords[6];
		lastY = pcoords[7];
		lastZ = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("closepath");
		break;
	    default:
		System.out.println("wrong type");
	    }
	    pi3.next();
	}

	System.out.println("... OK");

	System.out.println("check setupPlanarCP111ForTriangle methods:");

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 10.0, 50.0, 100.0);
	Surface3D.setupU0ForTriangle(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 20.0, 60.0, 90.0);
	Surface3D.setupV0ForTriangle(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(20.0, 60.0, 90.0, 10.0, 50.0, 100.0);
	Surface3D.setupW0ForTriangle(tcoords, ccoords, false);
	Surface3D.setupPlanarCP111ForTriangle(ccoords);
	for (int i = 0; i < 30; i+=3) {
	    double v[] = {ccoords[i], ccoords[i+1], ccoords[i+2]};
	    if (Math.abs(n[0]*v[0] + n[1]*v[1] + n[2]*v[2]) > 1.e-10) {
		System.out.println("... CP "  + i/3
				   + " = ( " + v[0] +", " + v[1] + ", " + v[2]
				   + ") not in plane of triangle");
		System.exit(1);
	    }
	}
	System.out.println("... OK");
	System.out.println("Shared point test:");

	Surface3D triv = new Surface3D.Double();
	ccoords = new double[48];
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 100.0, 0.0, 0.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 100.0, 0.0, 100.0, 100.0, 0.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 100.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 100.0, 0.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 100.0, 100.0, 0.0, 100.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 0.0, 100.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(100.0, 0.0, 0.0, 100.0, 0.0, 100.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	if (!triv.isWellFormed(System.out)) {
	    System.exit(1);
	}

	triv = new Surface3D.Double();
	Rectangle3D rect = new Rectangle3D.Double(0.0, 0.0, 0.0,
						  100.0, 100.0, 100.0);
	triv.append(rect);
	if (!triv.isWellFormed(System.out)) {
	    System.exit(1);
	}

	System.out.println("... two squares");

	triv = new Surface3D.Double();

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 100.0, 0.0, 0.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 100.0, 0.0, 100.0, 100.0, 0.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 100.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 100.0, 0.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(-100.0, 0.0, 0.0, -100.0, 100.0, 0.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, -100.0, 0.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 100.0, 0.0, -100.0, 100.0, 0.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, -100.0, 0.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(100.0, 0.0, 0.0, 100.0, -100.0, 0.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 100.0, 0.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, -100.0, 0.0, 100.0, -100.0, 0.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, -100.0, 0.0, 0.0);
	Surface3D.setupU0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, -100.0, 0.0, -100.0, -100.0, 0.0);
	Surface3D.setupU1ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, -100.0, 0.0);
	Surface3D.setupV0ForPatch(tcoords, ccoords, false);
	tcoords = Path3D.setupCubic(-100.0, 0.0, 0.0, -100.0, -100.0, 0.0);
	Surface3D.setupV1ForPatch(tcoords, ccoords, false);
	Surface3D.setupRestForPatch(ccoords);
	triv.addCubicPatch(ccoords);

	if (!triv.isWellFormed(System.out)) {
	    System.exit(1);
	}

	System.out.println("cubicTriangle to cubicPatch test:");

	tmp[0] = 0.0;
	tmp[1] = 0.0;
	tmp[2] = 10.0;
	tmp[3] = 30.0;
	tmp[4] = 0.0;
	tmp[5] = 20.0;
	tmp[6] = 70.0;
	tmp[7] = 0.0;
	tmp[8] = 30.0;
	tmp[9] = 100.0;
	tmp[10] = 0.0;
	tmp[11] = 40.0;

	Surface3D.setupV0ForTriangle(tmp, ctcoords, false);
	tmp[0] = 0.0;
	tmp[1] = 0.0;
	tmp[2] = 10.0;
	tmp[3] = 0.0;
	tmp[4] = 30.0;
	tmp[5] = 20.0;
	tmp[6] = 0.0;
	tmp[7] = 70.0;
	tmp[8] = 30.0;
	tmp[9] = 0.0;
	tmp[10] = 100.0;
	tmp[11] = 40.0;

	Surface3D.setupU0ForTriangle(tmp, ctcoords, false);
	tmp[0] = 100.0;
	tmp[1] = 0.0;
	tmp[2] = 40.0;
	tmp[3] = 70.0;
	tmp[4] = 30.0;
	tmp[5] = 50.0;
	tmp[6] = 30.0;
	tmp[7] = 70.0;
	tmp[8] = 50.0;
	tmp[9] =  0.0;
	tmp[10] = 100.0;
	tmp[11] = 40.0;
	Surface3D.setupW0ForTriangle(tmp, ctcoords, false);
	Surface3D.setupCP111ForTriangle(ctcoords);
	Surface3D.triangleToPatch(ctcoords, 0, ccoords, 0);


	double results1[] = new double[3];
	double results2[] = new double[3];
	for (int i = 0; i < 10; i++) {
	    double u = i/10.0;
	    for (int j = 0; j < 10; j++) {
		double v = j/10.0;
		if ((u + v) > 1.0) continue;
		double s = u;
		double t = v /(1-u);
		Surface3D.segmentValue(results1, SurfaceIterator.CUBIC_TRIANGLE,
				       ctcoords, u, v, -1.0);
		Surface3D.segmentValue(results2, SurfaceIterator.CUBIC_PATCH,
				       ccoords, s, t);
		for (int k = 0; k < 3; k++) {
		    if (Math.abs(results1[k] - results2[k]) > 1.e-10) {
			System.out.format("at (i=%d,j=%d), coord index %d:"
					  + "%s != %s\n", i, j,
					  k, results1[k], results2[k]);
			System.out.format("     u=%g, v=%g, s=%g, t=%g\n",
					  u, v, s, t);
			System.exit(1);
		    }
		}
	    }
	}

	System.out.println("... OK");

	System.out.println("Test cubic vertex ...");

	triv = new Surface3D.Double();

	double cvpatch[] = {10.0, 11.0, 12.0, // cp0
			    22.0, 23.0, 24.0, // cp1
			    35.0, 36.0, 37.0, // cp2
			    45.0, 46.0, 47.0, // cp3
			    25.0, 125.0, 26.0 // vertex
	};

	double patch[] = new double[48];
	double[] cvcoords = new double[15];
	triv.addCubicVertex(cvpatch);
	if (triv.getSegment(0, cvcoords) != SurfaceIterator.CUBIC_VERTEX) {
	    throw new Exception("not a cubic vertex");
	}
	for (int i = 0; i < 15; i++) {
	    if (cvpatch[i] != cvcoords[i]) {
		throw new Exception("getSegment failed");
	    }
	}

	SurfaceIterator si = triv.getSurfaceIterator(null);
	while (!si.isDone()) {
	    double[] sicoord = new double[48];
	    System.out.println("si type = " + si.currentSegment(sicoord));
	    for (int i = 0; i < 15; i++) {
		if (cvpatch[i] != sicoord[i]) {
		    throw new Exception("cubic vertex not added correctly");
		}
	    }
	    for (int i = 0; i < 5; i++) {
		System.out.format("    (%g, %g, %g)\n",
				  sicoord[3*i], sicoord[3*i+1], sicoord[3*i+2]);
	    }
	    System.out.print("elevated to a cubic patch:");
	    Surface3D.cubicVertexToPatch(sicoord, 0, patch, 0);
	    for (int i = 0; i < 48; i = i + 3) {
		if (i % 4 == 0) System.out.println();
		System.out.format(" (%g, %g, %g)",
				  patch[i], patch[i+1], patch[i+2]);
	    }
	    System.out.println();
	    si.next();
	}

	System.out.println("cvpatch: ");
	for (int i = 0; i < 5; i++) {
	    System.out.format("    (%g, %g, %g)\n",
			      cvpatch[3*i], cvpatch[3*i+1], cvpatch[3*i+2]);
	}

	Surface3D triv2 = new Surface3D.Double();
	triv2.addCubicPatch(patch);

	for (int i = 0; i <= 100; i++) {
	    double u = i / 100.0;
	    for (int j = 0; j <= 100; j++) {
		double v = j /100.0;
		Point3D p1 = Surface3D.segmentValue
		    (SurfaceIterator.CUBIC_PATCH, patch, u, v);
		Point3D p2 = Surface3D.segmentValue
		    (SurfaceIterator.CUBIC_VERTEX, cvpatch, u, v);
		Point3D p3 = SurfaceOps.segmentValue
		    (SurfaceIterator.CUBIC_VERTEX, cvpatch, u, v);
		Point3D p2b = Surface3D.segmentValue
		    (SurfaceIterator.CUBIC_VERTEX, cvpatch, u, v, -1);
		Point3D p3b = SurfaceOps.segmentValue
		    (SurfaceIterator.CUBIC_VERTEX, cvpatch, u, v, -1);
		if (!p2.equals(p3)) {
		    throw new Exception("p2 != p3");
		}
		if (!p2.equals(p2b)) {
		    throw new Exception("p2 != p2b");
		}
		if (!p3.equals(p3b)) {
		    throw new Exception("p3 != p3b");
		}
		if (Math.abs(p1.getX() - p2.getX()) > 1.e-12
		    || Math.abs(p1.getY() - p2.getY()) > 1.e-12
		    || Math.abs(p1.getZ() - p2.getZ()) > 1.e-12) {
		    System.out.format(" at (u,v) = (%g,%g), (%g, %g, %g) != "
				      + "(%g, %g, %g)\n",
				      u, v, p1.getX(), p1.getY(), p1.getZ(),
				      p2.getX(), p2.getY(), p2.getZ());
		    throw new Exception("segment values");
		}
		double[] v1 = new double[3];
		double[] v2 = new double[3];
		double[] v3 = new double[3];
		Surface3D.uTangent(v1, SurfaceIterator.CUBIC_PATCH,
				   patch, u, v);
		Surface3D.uTangent(v2, SurfaceIterator.CUBIC_VERTEX,
				   cvpatch, u, v);
		SurfaceOps.uTangent(v3, SurfaceIterator.CUBIC_VERTEX,
				   cvpatch, u, v);
		for (int k = 0; k < 3; k++) {
		    if (Math.abs(v1[k] -v2[k]) > 1.e-12
			|| v2[k] != v3[k]) {
			throw new Exception("uTangent error");
		    }
		}
		Surface3D.vTangent(v1, SurfaceIterator.CUBIC_PATCH,
				   patch, u, v);
		Surface3D.vTangent(v2, SurfaceIterator.CUBIC_VERTEX,
				   cvpatch, u, v);
		SurfaceOps.vTangent(v3, SurfaceIterator.CUBIC_VERTEX,
				   cvpatch, u, v);
		for (int k = 0; k < 3; k++) {
		    if (Math.abs(v1[k] -v2[k]) > 1.e-12
			|| v2[k] != v3[k]) {
			System.out.format("v1 = (%s, %s, %s)\n",
					  v1[0], v1[1], v1[2]);
			System.out.format("v2 = (%s, %s, %s)\n",
					  v2[0], v2[1], v2[2]);
			System.out.format("v3 = (%s, %s, %s)\n",
					  v3[0], v3[1], v3[2]);
			throw new Exception("vTangent error: (u,v) = ("
					    + u + ", " + v + ")");
		    }
		}

	    }
	}

	double area1 = triv.area(false);
	double area2 = triv2.area(false);
	if (Math.abs(area1) < 1.e-10) {
	    throw new Exception("zero area");
	}
	if (Math.abs(area1 - area2) > 1.e-4) {
	    throw new Exception("areas: " + area1 + " != " + area2);
	}
	Adder va1 = new Adder.Kahan();
	Adder va2 = new Adder.Kahan();

	triv.addVolumeToAdder(va1, triv.getSurfaceIterator(null),
			      new Point3D.Double(0.0, 0.0, 0.0));
	triv2.addVolumeToAdder(va2, triv2.getSurfaceIterator(null),
			       new Point3D.Double(0.0, 0.0, 0.0));
	double vol1 = va1.getSum();
	double vol2 = va2.getSum();

	if (Math.abs(vol1) < 1.e-10) {
	    throw new Exception("zero volume");
	}
	if (Math.abs(vol1 - vol2) > 1.e-2) {
	    throw new Exception("volumes: " + vol1 + " != " + vol2);
	}

	SubdivisionIterator subi = new
	    SubdivisionIterator(triv.getSurfaceIterator(null), 1);
	Adder va3 = new Adder.Kahan();
	double[] coords3 = new double[48];
	while (!subi.isDone()) {
	    int type = subi.currentSegment(coords3);
	    System.out.println("type = " + type);
	    switch(type) {
	    case SurfaceIterator.CUBIC_VERTEX:
		for (int i = 0; i < 5; i++) {
		    int ii = 3*i;
		    System.out.format("    (%g, %g, %g)\n",
				      coords3[ii],
				      coords3[ii+1],
				      coords3[ii+2]);
		}
		break;
	    case SurfaceIterator.CUBIC_PATCH:
		int k = 0;
		for (int i = 0; i < 4; i++) {
		    System.out.println("   ");
		    for (int j = 0; j < 4; j++) {
			System.out.format(" (%g, %g, %g)",
					  coords3[k++],
					  coords3[k++],
					  coords3[k++]);
		    }
		    System.out.println();
		}
		break;
	    }
	    subi.next();
	}
	subi = new SubdivisionIterator(triv.getSurfaceIterator(null), 1);
	Surface3D.addAreaToAdder(va3, subi);
	double area3 = va3.getSum();
	System.out.println("area1 = " + area1 +", area3 = " + area3);

	Adder va4 = new Adder.Kahan();
	subi = new SubdivisionIterator(triv.getSurfaceIterator(null), 2);
	Surface3D.addAreaToAdder(va4, subi);
	double area4 = va4.getSum();
	System.out.println("area4 = " + area4);

	Adder va5 = new Adder.Kahan();
	subi = new SubdivisionIterator(triv.getSurfaceIterator(null), 3);
	Surface3D.addAreaToAdder(va5, subi);
	double area5 = va5.getSum();
	System.out.println("area5 = " + area5);

	System.out.println("... OK");

	System.out.println("Check subdivision iterator for a cubic vertex");
	int correctCount[] = {1, 4, 16};
	for (int i = 0; i < 3; i++) {
	   subi = new SubdivisionIterator(triv.getSurfaceIterator(null), i);
	   int count = 0;
	   while (!subi.isDone()) {
	       // subi.currentSegment(coords3);
	       count++;
	       subi.next();
	   }
	   System.out.println("count = " + count + ", limit = " + i);
	   if (count != correctCount[i]) {
	       throw new Exception("wrong number of subdivisions");
	   }
	   subi = new SubdivisionIterator(triv.getSurfaceIterator(null), i);
	   count = 0;
	   while (!subi.isDone()) {
	       subi.currentSegment(coords3);
	       count++;
	       subi.next();
	   }
	   System.out.println("count = " + count + ", limit = " + i);
	}
	System.out.println("... OK");

	Path2D circle = Paths2D.createArc(51.0, 51.0, 51.0, 1.0,
					  2*Math.PI, Math.PI/2);
	circle.closePath();

	System.out.println("created 2D circle");

	Path3D circle3 = new Path3D.Double(circle, (nn, p, t, bbb) -> {
		return p;}, 0);

	PathIterator3D pi4 = circle3.getPathIterator(null);
	Surface3D surface1 = new Surface3D.Double();
	Surface3D surface2 = new Surface3D.Double();
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double startx = 0.0, starty = 0.0, startz = 0.0;
	System.out.println("creating surface1 and surface 2");
	while (!pi4.isDone()) {
	    switch(pi4.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		startx = lastx;
		starty = lasty;
		startz = lastz;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		surface1.addCubicVertex(lastx, lasty, lastz,
				       PathIterator3D.SEG_CUBICTO,
				       coords, 51.0, 51.0, 50.0);
		surface2.addCubicVertex(lastx, lasty, lastz,
				       PathIterator3D.SEG_CUBICTO,
				       coords, 51.0, 51.0, 50.0);
		surface2.addFlippedCubicVertex(lastx, lasty, lastz,
					      PathIterator3D.SEG_CUBICTO,
					      coords, 51.0, 51.0, -50.0);
		lastx = coords[6];
		lasty = coords[7];
		lastz = coords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    default:
		throw new Exception("unexpected case in switch");
	    }
	    pi4.next();
	}

	boundary = surface1.getBoundary();
	if (boundary == null) {
	    throw new Exception("... null boundary unexpected");
	}

	double blen = Path3DInfo.pathLength(boundary);
	System.out.format("surface1 boundary length = %g expecting %g for "
			  + " a perfect circle\n", blen, 100*Math.PI);


	if (!surface2.isWellFormed(System.out)) {
	    System.out.println("surface2 is not well formed");
	}

	boundary = surface2.getBoundary();


	if (boundary == null) {
	    throw new Exception ("null boundary unexpected");
	}
	if (!boundary.isEmpty()) {
	    throw new Exception("boundary for surface2 not empty");
	}

	System.out.format("surface2 volume = %g, expecting %g "
			  + "for perfect cones\n",
			  surface2.volume(), Math.PI*50.0*50.0*100.0/3.0);

	System.out.println("number of components for surface2 = "
			   + surface2.numberOfComponents());
	Shape3D comp0 = surface2.getComponent(0);
	System.out.println("comp0 bounds: " + comp0.getBounds());
	System.out.println("surface2.size() = " + surface2.size());
	si = comp0.getSurfaceIterator(null);
	int cnt = 0;
	coords = new double[48];
	while (!si.isDone()) {
	    cnt++;
	    si.next();
	}
	System.out.println("comp0.size = " + cnt);

	Surface3D surface3 = new Surface3D.Double(comp0);
	System.out.println("surface3 volume = " + surface3.volume());


	Adder va0 = new Adder.Kahan();
	SurfaceOps.addVolumeToAdder(va0, comp0.getSurfaceIterator(null),
				    new Point3D.Double(50.0, 50.0, 0.0));
	double comp0Volume = va0.getSum();

	System.out.format("comp0 volume = %g, expecting %g "
			  + "for perfect cones\n",
			 comp0Volume/3, Math.PI*50.0*50.0*100.0/3.0);

	Point3D cm = Surface3D.centerOfMassOf(surface2);
	System.out.println("cm = " + cm);
	double[][] moments = Surface3D.momentsOf(surface2,
						 new Point3D.Double(51.0, 51.0,
								    0.0));
	double[][] I = SurfaceOps.toMomentsOfInertia(moments);
	double expecting[][] = {
	    {625.0, 0.0, 0.0},
	    {0.0, 625.0, 0.0},
	    {0.0, 0.0, 750.0}
	};
	System.out.println();
	for (int i = 0; i < 3; i++) {
	    if (i == 1) {
		System.out.print("I = |");
	    } else {
		System.out.print("    |");
	    }
	    for (int j = 0; j < 3; j++) {
		double val = I[i][j];
		if (val < 1.e-13) val = 0.0;
		System.out.format("%5.3g", val);
	    }
	    if (i == 1) {
		System.out.print(" |, expecting |");
	    } else {
		System.out.print(" |            |");
	    }
	    for (int j = 0; j < 3; j++) {
		double val = expecting[i][j];
		if (val < 1.e-13) val = 0.0;
		System.out.format("%5.3g", val);
	    }
	    System.out.println(" |");
	}
	System.out.println();


	System.out.println("... OK");
	System.exit(0);
    }
}
