import org.bzdev.geom.*;
import org.bzdev.math.*;
import java.awt.*;
import java.awt.geom.*;


public class SplineTest {
    public static void main(String argv[]) throws Exception {
	double x[] = new double[19];
	double y[] = new double[19];
	double r = 100.0;
	for (int i = 0; i < 19; i++) {
	    double angle = i * 10.0;
	    double theta = Math.toRadians(angle);
	    x[i] = r*Math.cos(theta);
	    y[i] = r*Math.sin(theta);
	}
	CubicSpline xspline = new CubicSpline1(x, 0.0, Math.toRadians(10.0),
					       CubicSpline.Mode.CLAMPED,
					       0.0, 0.0);
	CubicSpline yspline = new CubicSpline1(y, 0.0, Math.toRadians(10.0) ,
					       CubicSpline.Mode.CLAMPED,
					       1.0*r, -1.0*r);
	
	SplinePath2D path = new SplinePath2D(xspline, yspline, false);

	System.out.println("test SplinePath2D constructor (open)...");

	int errcount = 0;

	if (Math.abs(Path2DInfo.pathLength(path) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path2DInfo.pathLength(path)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	int count = 0;
	for (Path2DInfo.Entry entry:  Path2DInfo.getEntries(path)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path2DInfo.UValues uval = new Path2DInfo.UValues(i/10.0);
		Path2DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double rr = Math.sqrt(xx*xx+yy*yy);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dxDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dxdu = " + sdata.dxDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	path = new BasicSplinePath2D(xspline, yspline, false);

	System.out.println("test BasicSplinePath2D constructor (open) ...");

	if (Math.abs(Path2DInfo.pathLength(path) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path2DInfo.pathLength(path)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path2DInfo.Entry entry:  Path2DInfo.getEntries(path)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path2DInfo.UValues uval = new Path2DInfo.UValues(i/10.0);
		Path2DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double rr = Math.sqrt(xx*xx+yy*yy);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dxDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dxdu = " + sdata.dxDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}
	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	 path = new SplinePath2D(xspline, yspline, true);

	System.out.println("test SplinePath2D constructor (closed)...");

	if (Math.abs(Path2DInfo.pathLength(path) - (r*Math.PI + 2*r)) > .007) {
	    System.out.println("path length = " + Path2DInfo.pathLength(path)
			       +", expecting " + (Math.PI*r + 2*r));
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path2DInfo.Entry entry:  Path2DInfo.getEntries(path)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path2DInfo.UValues uval = new Path2DInfo.UValues(i/10.0);
		Path2DInfo.SegmentData sdata = entry.getData();
		if (count == 19) {
		    if (Math.abs(sdata.curvature(uval)) != 0.0) {
			System.out.println("curvature of 0 expected");
		    }
		    if (Math.abs(sdata.getY(uval)) > 1.e-10) {
			System.out.println("not a straight line with y=0");
		    }
		    continue;
		}
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double rr = Math.sqrt(xx*xx+yy*yy);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dxDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dxdu = " + sdata.dxDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	path = new BasicSplinePath2D(xspline, yspline, true);

	System.out.println("test BasicSplinePath2D constructor closed) ...");
	if (Math.abs(Path2DInfo.pathLength(path) - (r*Math.PI + 2*r)) > .007) {
	    System.out.println("path length = " + Path2DInfo.pathLength(path)
			       +", expecting " + (Math.PI*r + 2*r));
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path2DInfo.Entry entry:  Path2DInfo.getEntries(path)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path2DInfo.UValues uval = new Path2DInfo.UValues(i/10.0);
		Path2DInfo.SegmentData sdata = entry.getData();
		if (count == 19) {
		    if (Math.abs(sdata.curvature(uval)) != 0.0) {
			System.out.println("curvature of 0 expected");
		    }
		    if (Math.abs(sdata.getY(uval)) > 1.e-10) {
			System.out.println("not a straight line with y=0");
		    }
		    continue;
		}
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double rr = Math.sqrt(xx*xx+yy*yy);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dxDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dxdu = " + sdata.dxDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}
	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}


	System.out.println("... OK");

	path = new SplinePath2D();
	path.append(xspline, yspline, false);

	System.out.println("test append(CubicSpline,CubicSpline,boolean) ...");

	// Path2DInfo.printSegments(path);

	if (Math.abs(Path2DInfo.pathLength(path) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path2DInfo.pathLength(path)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path2DInfo.Entry entry:  Path2DInfo.getEntries(path)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path2DInfo.UValues uval = new Path2DInfo.UValues(i/10.0);
		Path2DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double rr = Math.sqrt(xx*xx+yy*yy);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dxDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dxdu = " + sdata.dxDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");


	// System.out.println("Now test 3D spline paths");

	double[] z = new double[19];
	double phi = Math.toRadians(30);
	// we want a curve where X, Y, and Z will have differnt values
	// at each point.
	for (int i = 0; i < 19; i++) {
	    double angle = i * 10.0;
	    double theta = Math.toRadians(angle);
	    z[i] = r*Math.cos(theta);
	    x[i] = r*Math.sin(theta)*Math.cos(phi);
	    y[i] = r*Math.sin(theta)*Math.sin(phi);
	}
	
	CubicSpline zspline = new CubicSpline1(z, 0.0, Math.toRadians(10.0),
					       CubicSpline.Mode.CLAMPED,
					       0.0, 0.0);
	xspline = new CubicSpline1(x, 0.0, Math.toRadians(10.0),
				   CubicSpline.Mode.CLAMPED,
				   r*Math.cos(phi), -1.0*r*Math.cos(phi));
	yspline = new CubicSpline1(y, 0.0, Math.toRadians(10.0) ,
				   CubicSpline.Mode.CLAMPED,
				   r*Math.sin(phi), -r*Math.sin(phi));

	SplinePath3D path3d = new SplinePath3D(xspline, yspline, zspline,
					       false);
	System.out.println("test SplinePath3D constructor (open)...");

	if (Math.abs(Path3DInfo.pathLength(path3d) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path3DInfo.pathLength(path3d)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path3DInfo.Entry entry:  Path3DInfo.getEntries(path3d)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path3DInfo.UValues uval = new Path3DInfo.UValues(i/10.0);
		Path3DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double zz = sdata.getZ(uval);
		double rr = Math.sqrt(xx*xx+yy*yy+zz*zz);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dzDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dzdu = " + sdata.dzDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	path3d = new BasicSplinePath3D(xspline, yspline, zspline, false);
	System.out.println("test BasicSplinePath3D constructor (open)...");

	if (Math.abs(Path3DInfo.pathLength(path3d) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path3DInfo.pathLength(path3d)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path3DInfo.Entry entry:  Path3DInfo.getEntries(path3d)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path3DInfo.UValues uval = new Path3DInfo.UValues(i/10.0);
		Path3DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double zz = sdata.getZ(uval);
		double rr = Math.sqrt(xx*xx+yy*yy+zz*zz);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dzDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dzdu = " + sdata.dzDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	path3d = new SplinePath3D(xspline, yspline, zspline, true);
	System.out.println("test SplinePath3D constructor (closed)...");

	if (Math.abs(Path3DInfo.pathLength(path3d) - (r*Math.PI+2*r)) > .007) {
	    System.out.println("path length = " + Path3DInfo.pathLength(path3d)
			       +", expecting " + (Math.PI*r + 2*r));
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path3DInfo.Entry entry:  Path3DInfo.getEntries(path3d)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path3DInfo.UValues uval = new Path3DInfo.UValues(i/10.0);
		Path3DInfo.SegmentData sdata = entry.getData();
		if (count == 19) {
		    if (Math.abs(sdata.curvature(uval)) != 0.0) {
			System.out.println("curvature of 0 expected");
		    }
		    if (Math.abs(sdata.getX(uval)) > 1.e-10) {
			System.out.println("not a straight line with x=0");
		    }
		    if (Math.abs(sdata.getY(uval)) > 1.e-10) {
			System.out.println("not a straight line with y=0");
		    }
		    continue;
		}
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double zz = sdata.getZ(uval);
		double rr = Math.sqrt(xx*xx+yy*yy+zz*zz);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dzDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dzdu = " + sdata.dzDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	path3d = new BasicSplinePath3D(xspline, yspline, zspline, true);
	System.out.println("test BasicSplinePath3D constructor (closed)...");

	if (Math.abs(Path3DInfo.pathLength(path3d) - (r*Math.PI+2*r)) > .007) {
	    System.out.println("path length = " + Path3DInfo.pathLength(path3d)
			       +", expecting " + (Math.PI*r + 2*r));
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path3DInfo.Entry entry:  Path3DInfo.getEntries(path3d)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path3DInfo.UValues uval = new Path3DInfo.UValues(i/10.0);
		Path3DInfo.SegmentData sdata = entry.getData();
		if (count == 19) {
		    if (Math.abs(sdata.curvature(uval)) != 0.0) {
			System.out.println("curvature of 0 expected");
		    }
		    if (Math.abs(sdata.getX(uval)) > 1.e-10) {
			System.out.println("not a straight line with x=0");
		    }
		    if (Math.abs(sdata.getY(uval)) > 1.e-10) {
			System.out.println("not a straight line with y=0");
		    }
		    continue;
		}
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double zz = sdata.getZ(uval);
		double rr = Math.sqrt(xx*xx+yy*yy+zz*zz);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dzDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dzdu = " + sdata.dzDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	System.out.println("test append(CubicSpline,CubicSpline,CubicSpline,"
			   + "boolean) ...");

	path3d = new SplinePath3D();
	path3d.append(xspline, yspline, zspline, false);

	if (Math.abs(Path3DInfo.pathLength(path3d) - r*Math.PI) > .007) {
	    System.out.println("path length = " + Path3DInfo.pathLength(path3d)
			       +", expecting " + Math.PI*r);
	    errcount++;
	}

	// Path2DInfo.printSegments(path);

	count = 0;
	for (Path3DInfo.Entry entry:  Path3DInfo.getEntries(path3d)) {
	    for (int i = 0 ;i < 11; i++) {
		if (entry.getType() == PathIterator.SEG_MOVETO) continue;
		Path3DInfo.UValues uval = new Path3DInfo.UValues(i/10.0);
		Path3DInfo.SegmentData sdata = entry.getData();
		if (Math.abs(sdata.curvature(uval) - 0.01) > 1.e-4) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": curvature = " +
				       sdata.curvature(uval)
				       + ", expecting 0.01");
		}
		double xx = sdata.getX(uval);
		double yy = sdata.getY(uval);
		double zz = sdata.getZ(uval);
		double rr = Math.sqrt(xx*xx+yy*yy+zz*zz);
		if (Math.abs(rr - r) > 1.e-3) {
		    errcount++;
		    System.out.println("for segment " + count + ", u = "
				       + (i/10.0)
				       + ": r = " + rr + ", expecting " + r);
		}

		if (i == 0 && count == 0 || i == 10 && count == 18) {
		    if (sdata.dzDu(uval) != 0.0) {
			errcount++;
			System.out.println("for segment " + count + ", u = "
					   + (i/10.0)
					   + ": dzdu = " + sdata.dzDu(uval)
					   + ", expecting 0");
		    }
		}
	    }
	    count++;
	}


	try {
	    System.out.println("Try a case that will generate an error");
	    path = new SplinePath2D();
	    Point2D[] points = {new Point2D.Double(0.0, 0.0),
				new Point2D.Double(10.0, 10.0),
				new Point2D.Double(30.0, 40.0)};
	    path.splineTo(points);
	} catch (Exception e) {
	    System.out.println("... " + e.getMessage());
	    System.out.println("... [Exception expected]\n");
	}

	System.out.println("Try a 2D spline with two knots");
	Point2D points2D[] = {
	    new Point2D.Double(10.0, 10.0),
	    new Point2D.Double(10.0, 0.0)};
	Path2D path2d = new SplinePath2D(points2D, false);
	Path2DInfo.printSegments(path2d);

	System.out.println("Try a 3D spline with two knots");
	Point3D points3D[] = {
	    new Point3D.Double(0.0, 10.0, 10.0),
	    new Point3D.Double(0.0, 10.0, 0.0)};
	path3d = new SplinePath3D(points3D, false);
	Path3DInfo.printSegments(path3d);

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");


	System.exit(0);
    }
}
