import org.bzdev.geom.*;
import java.awt.geom.*;
import java.util.Arrays;

import org.bzdev.math.LUDecomp;
import org.bzdev.math.CubicSpline1;
import org.bzdev.math.CubicSpline;
import org.bzdev.math.CubicSpline.Mode;
import org.bzdev.util.ArrayMerger;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;
import org.bzdev.math.BicubicInterpolator;

public class Surface3DTest2 {
    public static void main(String argv[]) throws Exception {

	Rectangle3D r3d = new Rectangle3D.Double(0.0, 0.0, 0.0,
						 100.0, 200.0, 300.0);

	Surface3D sr3d = new Surface3D.Double(r3d);

	if (sr3d.area() != (double)(2*100*200 + 2*100*300 + 2*200*300)) {
	    System.out.println("sr3d.area() = " + sr3d.area()
			       + ", should be "
			       +(2*100*200 + 2*100*300 + 2*200*300));
	    System.exit(1);
	}

	double[] coords = new double[48];

	double[] pcoords1 = Path3D.setupCubic(0.0, 0.0, 0.0,
					      100.0, 0.0, 0.0);
	double[] pcoords2 = Path3D.setupCubic(100.0, 0.0, 0.0,
					       100.0, 200.0, 0.0);
	double[] pcoords3 = Path3D.setupCubic(100.0, 200.0, 0.0,
					       0.0, 200.0, 0.0);
	double[] pcoords4 = Path3D.setupCubic(0.0, 200.0, 0.0,
					      0.0, 0.0, 0.0);

	Surface3D.setupV0ForPatch(pcoords1, coords, false);
	Surface3D.setupU1ForPatch(pcoords2, coords, false);
	Surface3D.setupV1ForPatch(pcoords3, coords, true);
	Surface3D.setupU0ForPatch(pcoords4, coords, true);
	Surface3D.setupRestForPatch(coords);

	Surface3D s3d = new Surface3D.Double();
	s3d.addCubicPatch(coords);
	System.out.println("area for square = " +s3d.area()
			   + ", expected 20000.0");


	double[] pcoords23 = Path3D.setupCubic(100.0, 0.0, 0.0,
					       0.0, 200.0, 0.0);

	double bcinits[] = {0.0, 1.0, 1.0, 4.0,
			    0.0, 2.0, 1.0, 3.0,
			    0.0, 1.0, 3.0, 5.0,
			    0.0, 0.0, 2.0, 2.0};
	BicubicInterpolator bci = new BicubicInterpolator(bcinits);
	double[] bcicoords = bci.getControlPoints(true);

	double[] xcoords = (double[]) bcicoords.clone();
	double[] ycoords = (double[]) bcicoords.clone();
	double[] zcoords = (double[]) bcicoords.clone();
	for (int i = 0; i < xcoords.length; i++) {
	    ycoords[i] *= 10.0;
	    zcoords[i] *= 100.0;
	}

	double[] bcoords = ArrayMerger.merge(xcoords, ycoords, zcoords);
	// Sanity test.  Basically, this makes sure that we should use
	// getControlPoints(true) and not getControlPoints(false)
	for (int i = 0; i < 11; i++) {
	    double u = i/10.0;
	    for (int j = 0; j < 11; j++) {
		double v = j/10.0;
		double actual = bci.valueAt(u,v);
		Point3D p = Surface3D.segmentValue(SurfaceIterator.CUBIC_PATCH,
						   bcoords, u, v);
		double xExpected = p.getX();
		double yExpected = p.getY();
		double zExpected = p.getZ();
		if (Math.abs(xExpected - actual) > 1.e-10) {
		    System.out.println("xExpected = " + xExpected
				       + ", actual = " + actual);
		    System.exit(1);
		}
		if (Math.abs(yExpected - 10.0*actual) > 1.e-10) {
		    System.out.println("yExpected = " + yExpected
				       + ", 10*actual = " + (10.0*actual));
		    System.exit(1);
		}
		if (Math.abs(zExpected - 100.0*actual) > 1.e-10) {
		    System.out.println("yExpected = " + zExpected
				       + ", 10*actual = " + (100.0*actual));
		    System.exit(1);
		}
	    }
	}

	// set the values we are going to modify to zero to ensure
	// an error if we fail to set them.
	Arrays.fill(bcoords, 15, 15+6, 0.0);
	Arrays.fill(bcoords, 27, 27+6, 0.0);

	double d2duv00[] = {0.0, 0.0, 0.0};
	double d2duv10[] = {0.0, 0.0, 0.0};
	double d2duv01[] = {2.0, 20.0, 200.0};
	double d2duv11[] = {2.0, 20.0, 200.0};

	System.out.println("testing setupRestForPath (with multiple args)");
	Surface3D.setupRestForPatch(1.0, 1.0,
				    d2duv00, d2duv10, d2duv01, d2duv11,
				    bcoords);

	// now make sure we computed the correct values.
	for (int i = 0; i < 11; i++) {
	    double u = i/10.0;
	    for (int j = 0; j < 11; j++) {
		double v = j/10.0;
		double actual = bci.valueAt(u,v);
		Point3D p = Surface3D.segmentValue(SurfaceIterator.CUBIC_PATCH,
						   bcoords, u, v);
		double xExpected = p.getX();
		double yExpected = p.getY();
		double zExpected = p.getZ();
		if (Math.abs(xExpected - actual) > 1.e-10) {
		    System.out.println("xExpected = " + xExpected
				       + ", actual = " + actual
				       + ": u = " + u + ", v = " + v);
		    System.exit(1);
		}
		if (Math.abs(yExpected - 10.0*actual) > 1.e-10) {
		    System.out.println("yExpected = " + yExpected
				       + ", 10*actual = " + (10.0*actual)
				       + ": u = " + u + ", v = " + v);
		    System.exit(1);
		}
		if (Math.abs(zExpected - 100.0*actual) > 1.e-10) {
		    System.out.println("yExpected = " + zExpected
				       + ", 10*actual = " + (100.0*actual)
				       + ": u = " + u + ", v = " + v);
		    System.exit(1);
		}
	    }
	}


	s3d = new Surface3D.Double();

	Surface3D.setupV0ForTriangle(pcoords1, coords, false);
	Surface3D.setupW0ForTriangle(pcoords23, coords, false);
	Surface3D.setupU0ForTriangle(pcoords4, coords, true);
	Surface3D.setupPlanarCP111ForTriangle(coords);

	for (int i = 0; i < 30; i += 3) {
	    System.out.format("control point %d = (%g, %g, %g)\n",
			      i/3, coords[i], coords[i+1], coords[i+2]);
	}

	s3d.addCubicTriangle(coords);

	System.out.println("area for triangle = " +s3d.area()
			   + ", expected 10000.0");
	
	// now create a sphere and try that - we know the area and
	// volume.
	
	double xt[][] = new double[37][19];
	double yt[][] = new double[37][19];
	double zt[][] = new double[37][19];
	double xp[][] = new double[19][37];
	double yp[][] = new double[19][37];
	double zp[][] = new double[19][37];
	double r = 100.0;
	for (int j = 0; j < 37; j++) {
	    double pAngle = j*10;
	    double phi = Math.toRadians(pAngle);
	    for (int i = 0; i < 19; i++) {
		double tAngle = i * 10.0;
		double theta = Math.toRadians(tAngle);
		if (j == 36) {
		    zt[j][i] = zt[0][i];
		    xt[j][i] = xt[0][i];
		    yt[j][i] = yt[0][i];
		} else {
		    double tsin = Math.sin(theta);
		    if (i == 18 || i == 0) tsin = 0.0;
		    zt[j][i] = r*Math.cos(theta);
		    xt[j][i] = r*tsin*Math.cos(phi);
		    yt[j][i] = r*tsin*Math.sin(phi);
		}
		zp[i][j] = zt[j][i];
		xp[i][j] = xt[j][i];
		yp[i][j] = yt[j][i];
	    }
	}
	double[][] xtcoords = new double[37][];
	double[][] ytcoords = new double[37][];
	double[][] ztcoords = new double[37][];
	for (int j = 0; j < 37; j++) {
	    double pAngle = j*10;
	    if (j == 36) pAngle = 0*10;
	    double phi = Math.toRadians(pAngle);
	    xtcoords[j] = (new CubicSpline1(xt[j], 0.0, Math.toRadians(10.0),
					    CubicSpline.Mode.CLAMPED,
					    r*Math.cos(phi), -r*Math.cos(phi)))
		.getBernsteinCoefficients();
	    ytcoords[j] = (new CubicSpline1(yt[j], 0.0, Math.toRadians(10.0),
					    CubicSpline.Mode.CLAMPED,
					    r*Math.sin(phi), -r*Math.sin(phi)))
		.getBernsteinCoefficients();
	    ztcoords[j] = (new CubicSpline1(zt[j], 0.0, Math.toRadians(10.0),
					    CubicSpline.Mode.CLAMPED,
					    0.0, 0.0))
		.getBernsteinCoefficients();
	}
	double[][] xpcoords = new double[19][];
	double[][] ypcoords = new double[19][];
	double[][] zpcoords = new double[19][];
	for (int i = 0; i < 19; i++) {
	    double tAngle = i * 10.0;
	    double theta = Math.toRadians(tAngle);
	    xpcoords[i] = (new CubicSpline1(xp[i], 0.0, Math.toRadians(10.0),
					    CubicSpline.Mode.CLAMPED,
					    0.0, 0.0))
		.getBernsteinCoefficients();
	    double tsin = Math.sin(theta);
	    if (i == 0 || i == 18) tsin = 0.0;
	    ypcoords[i] = (new CubicSpline1(yp[i], 0.0, Math.toRadians(10.0),
					    CubicSpline.Mode.CLAMPED,
					    r*tsin,
					    -r*tsin))
		.getBernsteinCoefficients();
	    zpcoords[i] = (new CubicSpline1(zp[i], 0.0, Math.toRadians(10.0)))
		.getBernsteinCoefficients();
	}
	Surface3D sphere = new Surface3D.Double();

	for (int k = 0; k < xtcoords[0].length; k++) {
	    if (xtcoords[0][k] != xtcoords[36][k]) {
		System.out.println("bad xtcoords at k = " + k);
		System.exit(1);
	    }
	    if (ytcoords[0][k] != ytcoords[36][k]) {
		System.out.println("bad ytcoords at k = " + k);
		System.out.println("yt at [0][k]: " + ytcoords[0][k]);
		System.out.println("yt at [36][k]: " + ytcoords[36][k]);
		System.exit(1);
	    }
	    if (ztcoords[0][k] != ztcoords[36][k]) {
		System.out.println("bad ztcoords at k = " + k);
		System.out.println("zt at [0][k]: " + ztcoords[0][k]);
		System.out.println("zt at [36][k]: " + ztcoords[36][k]);
		System.exit(1);
	    }
	}

	int ecount  = 0;
	for (int i = 0; i < 36; i++) {
	    int ip1 = i + 1;
	    double[] vcoords = ArrayMerger.merge(0, 4,
						 xtcoords[i],
						 ytcoords[i],
						 ztcoords[i]);
	    double[] ucoords = ArrayMerger.merge(i*3, i*3 + 4,
						 xpcoords[1],
						 ypcoords[1],
						 zpcoords[1]);
	    double[] wcoords = ArrayMerger.merge(0, 4,
						 xtcoords[ip1],
						 ytcoords[ip1],
						 ztcoords[ip1]);
	    Surface3D.setupU0ForTriangle(vcoords, coords, true);
	    Surface3D.setupV0ForTriangle(ucoords, coords, false);
	    Surface3D.setupW0ForTriangle(wcoords, coords, true);
	    double phi = Math.toRadians((2*i + 1)*5.0);
	    double theta = Math.toRadians(20.0/3.0);
	    Surface3D.setupCP111ForTriangle(r*Math.sin(theta)*Math.cos(phi),
					    r*Math.sin(theta)*Math.sin(phi),
					    r*Math.cos(theta), coords,
					    1.0/3, 1.0/3);
	    sphere.addCubicTriangle(coords); ecount++;
	    for (int j = 2; j < 18; j++) {
		double[] u0coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[i],
						      ytcoords[i],
						      ztcoords[i]);
		double[] u1coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[ip1],
						      ytcoords[ip1],
						      ztcoords[ip1]);
		double[] v1coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j-1],
						      ypcoords[j-1],
						      zpcoords[j-1]);
		double[] v0coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j],
						      ypcoords[j],
						      zpcoords[j]);
		Surface3D.setupU0ForPatch(u0coords, coords, true);
		Surface3D.setupU1ForPatch(u1coords, coords, true);
		Surface3D.setupV0ForPatch(v0coords, coords, false);
		Surface3D.setupV1ForPatch(v1coords, coords, false);
		Surface3D.setupRestForPatch(coords);
		sphere.addCubicPatch(coords); ecount++;
	    }
	    vcoords = ArrayMerger.merge(3*17, 3*17+4,
					xtcoords[ip1],
					ytcoords[ip1], ztcoords[ip1]);
	    ucoords = ArrayMerger.merge(i*3, i*3 + 4,
					xpcoords[17], ypcoords[17],
					zpcoords[17]);
	    wcoords = ArrayMerger.merge(3*17, 3*17+4,
					xtcoords[i], ytcoords[i],
					ztcoords[i]);
	    Surface3D.setupU0ForTriangle(vcoords, coords, false);
	    Surface3D.setupV0ForTriangle(ucoords, coords, true);
	    Surface3D.setupW0ForTriangle(wcoords, coords, false);
	    theta = Math.toRadians(170.0 + 10.0/3.0);
	    Surface3D.setupCP111ForTriangle(r*Math.sin(theta)*Math.cos(phi),
					    r*Math.sin(theta)*Math.sin(phi),
					    r*Math.cos(theta), coords,
					    1.0/3, 1.0/3);
	    sphere.addCubicTriangle(coords); ecount++;
	}

	if (!sphere.isWellFormed(System.out)) {
	    System.out.println("not well formed");
	}

	Path3D boundary = sphere.getBoundary();
	if (boundary != null  && !boundary.isEmpty()) {
	    System.out.println("sphere boundary segments:");
	    Path3DInfo.printSegments("    ", System.out, boundary);
	}

	if (!sphere.isClosedManifold()) {
	    System.out.println("sphere is not a closed manifold");
	    System.exit(1);
	}

	SurfaceIterator si = sphere.getSurfaceIterator(null);
	int count = 0;
	Adder sumr = new Adder.Kahan();
	Adder sumrsq = new Adder.Kahan();
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    for (int i = 0; i < 11; i++) {
		double u = i/10.0;
		for (int j = 0; j < 11; j++) {
		    double v = j/10.0;
		    Point3D  p = Surface3D.segmentValue(type, coords, u, v);
		    double radius = p.distance(0.0, 0.0, 0.0);
		    sumr.add(radius);
		    sumrsq.add(radius*radius);
		    count++;
		    if (Math.abs(radius - r) > 0.5) {
			System.out.println("radius = " + radius
					   + " for type = " + type);
		    }
		}
	    }
	    si.next();
	}

	double meanr = sumr.getSum() / count;
	double sdevsq = sumrsq.getSum() / count - meanr*meanr;
	System.out.println("mean radius = " + meanr + ", sdev = "
			   + Math.sqrt(sdevsq));

	System.out.println("surface area of sphere = "
			   + sphere.area() + ", expecting "
			   + 4*Math.PI*r*r);

	System.out.println("volume of sphere = "
			   +sphere.volume() + ", expecting "
			   + ((4*Math.PI*r*r*r)/3.0));

	System.out.println("try a second sphere (different initialization)");

	Surface3D sphere2 = new Surface3D.Double();
	for (int i = 0; i < 36; i++) {
	    int ip1 = i + 1;
	    double phi = Math.toRadians(i*10.0);
	    double phip1 = Math.toRadians(ip1*10.0);
	    double[] vcoords = ArrayMerger.merge(0, 4,
						 xtcoords[i],
						 ytcoords[i],
						 ztcoords[i]);
	    double[] ucoords = ArrayMerger.merge(i*3, i*3 + 4,
						 xpcoords[1],
						 ypcoords[1],
						 zpcoords[1]);
	    double[] wcoords = ArrayMerger.merge(0, 4,
						 xtcoords[ip1],
						 ytcoords[ip1],
						 ztcoords[ip1]);
	    Surface3D.setupU0ForTriangle(vcoords, coords, true);
	    Surface3D.setupV0ForTriangle(ucoords, coords, false);
	    Surface3D.setupW0ForTriangle(wcoords, coords, true);
	    Surface3D.setupCP111ForTriangle(coords);
	    sphere2.addCubicTriangle(coords); ecount++;
	    for (int j = 2; j < 18; j++) {
		double theta = Math.toRadians(j*10.0);
		double thetam1=Math.toRadians((j-1)*10.0);
		double[] u0coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[i],
						      ytcoords[i],
						      ztcoords[i]);
		double[] u1coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[ip1],
						      ytcoords[ip1],
						      ztcoords[ip1]);
		double[] v1coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j-1],
						      ypcoords[j-1],
						      zpcoords[j-1]);
		double[] v0coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j],
						      ypcoords[j],
						      zpcoords[j]);
		Surface3D.setupU0ForPatch(u0coords, coords, true);
		Surface3D.setupU1ForPatch(u1coords, coords, true);
		Surface3D.setupV0ForPatch(v0coords, coords, false);
		Surface3D.setupV1ForPatch(v1coords, coords, false);
		double xd2duv00[] = {-r*Math.cos(theta)*Math.sin(phi),
				     r*Math.cos(theta)*Math.cos(phi), 0.0};
		double xd2duv10[] = {-r*Math.cos(theta)*Math.sin(phip1),
				     r*Math.cos(theta)*Math.cos(phip1), 0.0};
		double xd2duv01[] = {-r*Math.cos(thetam1)*Math.sin(phi),
				     r*Math.cos(thetam1)*Math.cos(phi), 0.0};
		double xd2duv11[] = {-r*Math.cos(thetam1)*Math.sin(phip1),
				     r*Math.cos(thetam1)*Math.cos(phip1), 0.0};
		Surface3D.setupRestForPatch(Math.toRadians(10.0),
					    Math.toRadians(10.0),
					    xd2duv00, xd2duv10,
					    xd2duv01, xd2duv11,
					    coords);
		sphere2.addCubicPatch(coords); ecount++;
	    }
	    vcoords = ArrayMerger.merge(3*17, 3*17+4,
					xtcoords[ip1],
					ytcoords[ip1], ztcoords[ip1]);
	    ucoords = ArrayMerger.merge(i*3, i*3 + 4,
					xpcoords[17], ypcoords[17],
					zpcoords[17]);
	    wcoords = ArrayMerger.merge(3*17, 3*17+4,
					xtcoords[i], ytcoords[i],
					ztcoords[i]);
	    Surface3D.setupU0ForTriangle(vcoords, coords, false);
	    Surface3D.setupV0ForTriangle(ucoords, coords, true);
	    Surface3D.setupW0ForTriangle(wcoords, coords, false);
	    Surface3D.setupCP111ForTriangle(coords);
	    sphere2.addCubicTriangle(coords); ecount++;
	}
	si = sphere2.getSurfaceIterator(null);
	count = 0;
	sumr = new Adder.Kahan();
	sumrsq = new Adder.Kahan();
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    for (int i = 0; i < 11; i++) {
		double u = i/10.0;
		for (int j = 0; j < 11; j++) {
		    double v = j/10.0;
		    Point3D  p = Surface3D.segmentValue(type, coords, u, v);
		    double radius = p.distance(0.0, 0.0, 0.0);
		    sumr.add(radius);
		    sumrsq.add(radius*radius);
		    count++;
		    if (Math.abs(radius - r) > 0.5) {
			System.out.println("radius = " + radius
					   + " for type = " + type);
		    }
		}
	    }
	    si.next();
	}

	meanr = sumr.getSum() / count;
	sdevsq = sumrsq.getSum() / count - meanr*meanr;

	System.out.println("mean radius = " + meanr + ", sdev = "
			   + Math.sqrt(sdevsq));

	System.out.println("surface area of sphere2 = "
			   + sphere2.area() + ", expecting "
			   + 4*Math.PI*r*r);

	System.out.println("volume of sphere2 = "
			   +sphere2.volume() + ", expecting "
			   + ((4*Math.PI*r*r*r)/3.0));

	System.out.println("try a third sphere (no triangular cubic patches)");

	Surface3D sphere3 = new Surface3D.Double();
	for (int i = 0; i < 36; i++) {
	    int ip1 = i + 1;
	    double phi = Math.toRadians(i*10.0);
	    double phip1 = Math.toRadians(ip1*10.0);
	    for (int j = 1; j < 19; j++) {
		double theta = Math.toRadians(j*10.0);
		double thetam1=Math.toRadians((j-1)*10.0);
		double[] u0coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[i],
						      ytcoords[i],
						      ztcoords[i]);
		double[] u1coords = ArrayMerger.merge(3*(j-1), 3*(j-1)+4,
						      xtcoords[ip1],
						      ytcoords[ip1],
						      ztcoords[ip1]);
		double[] v1coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j-1],
						      ypcoords[j-1],
						      zpcoords[j-1]);
		double[] v0coords = ArrayMerger.merge(3*i, 3*i+4,
						      xpcoords[j],
						      ypcoords[j],
						      zpcoords[j]);
		Surface3D.setupU0ForPatch(u0coords, coords, true);
		Surface3D.setupU1ForPatch(u1coords, coords, true);
		Surface3D.setupV0ForPatch(v0coords, coords, false);
		Surface3D.setupV1ForPatch(v1coords, coords, false);
		double xd2duv00[] = {-r*Math.cos(theta)*Math.sin(phi),
				     r*Math.cos(theta)*Math.cos(phi), 0.0};
		double xd2duv10[] = {-r*Math.cos(theta)*Math.sin(phip1),
				     r*Math.cos(theta)*Math.cos(phip1), 0.0};
		double xd2duv01[] = {-r*Math.cos(thetam1)*Math.sin(phi),
				     r*Math.cos(thetam1)*Math.cos(phi), 0.0};
		double xd2duv11[] = {-r*Math.cos(thetam1)*Math.sin(phip1),
				     r*Math.cos(thetam1)*Math.cos(phip1), 0.0};
		Surface3D.setupRestForPatch(Math.toRadians(10.0),
					    Math.toRadians(10.0),
					    xd2duv00, xd2duv10,
					    xd2duv01, xd2duv11,
					    coords);
		sphere3.addCubicPatch(coords); ecount++;
	    }
	}
	if (sphere3.isWellFormed(System.out) == false) {
	    System.out.println("not well formed");
	}

	si = sphere3.getSurfaceIterator(null);
	count = 0;
	sumr = new Adder.Kahan();
	sumrsq = new Adder.Kahan();
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    for (int i = 0; i < 11; i++) {
		double u = i/10.0;
		for (int j = 0; j < 11; j++) {
		    double v = j/10.0;
		    Point3D  p = Surface3D.segmentValue(type, coords, u, v);
		    double radius = p.distance(0.0, 0.0, 0.0);
		    sumr.add(radius);
		    sumrsq.add(radius*radius);
		    count++;
		    if (Math.abs(radius - r) > 0.5) {
			System.out.println("radius = " + radius
					   + " for type = " + type);
		    }
		}
	    }
	    si.next();
	}
	meanr = sumr.getSum() / count;
	sdevsq = sumrsq.getSum() / count - meanr*meanr;

	System.out.println("mean radius = " + meanr + ", sdev = "
			   + Math.sqrt(sdevsq));

	System.out.println("surface area of sphere3 = "
			   + sphere3.area() + ", expecting "
			   + 4*Math.PI*r*r);

	System.out.println("volume of sphere3 = "
			   +sphere3.volume() + ", expecting "
			   + ((4*Math.PI*r*r*r)/3.0));
    }
}
