import org.bzdev.geom.*;
import org.bzdev.math.*;
import org.bzdev.math.rv.IntegerRandomVariable;
import org.bzdev.math.rv.UniformIntegerRV;
import java.awt.*;
import java.awt.geom.*;
import java.io.PrintWriter;

public class PathSplitTest3D {
    public static void main(String argv[]) throws Exception {
	Path3D path = new Path3D.Double();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 6.0, 2.0, 4.0, 7.0, 0.0, 4.0, 8.0);
	path.curveTo(-2.0, 4.0, 9.0, -8.0, 4.0, 10.0, -8.0, 2.0, 11.0);
	path.closePath();
	path.lineTo(-7.0, 0.0, 12.0);
	path.lineTo(-7.0, 1.0, 13.0);
	path.closePath();
	path.moveTo(10.0, 10.0, 4.0);
	path.lineTo(11.0, 11.0, 15.0);
	path.moveTo(20.0, 20.0, 17.0);
	path.lineTo(30.0, 40.0, 17.0);
	path.lineTo(40.0, 40.0, 18.0);
	path.closePath();

	Path3D[] subpaths = PathSplitter.split(path);

	PathIterator3D pit = path.getPathIterator(null);
	double[] coords = new double[9];
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1]
				   + ", " + coords[2] + ")");
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1]
				   + ", " + coords[2] + ")");
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 6; i += 3)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ", " + coords[i+2] + ")");
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 9; i += 3)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ", " + coords[i+2] + ")");
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		break;
	    }
	    pit.next();
	}

	for (int j = 0; j < subpaths.length; j++) {
	    System.out.println("SUBPATH " + j + ":");
	    pit = subpaths[j].getPathIterator(null);
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		switch (type) {
		case PathIterator.SEG_MOVETO:
		    System.out.println("MOVETO (" + coords[0]
				       + ", " + coords[1]
				       + ", " + coords[2] + ")");
		    break;
		case PathIterator.SEG_LINETO:
		    System.out.println("LINETO (" + coords[0]
				       + ", " + coords[1]
				       + ", " + coords[2] + ")");
		    break;
		case PathIterator.SEG_QUADTO:
		    System.out.println("QUADTO:");
		    for (int i = 0; i < 6; i += 3)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ", " + coords[i+2] + ")");
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.out.println("CUBICTO:");
		    for (int i = 0; i < 9; i += 3)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ", " + coords[i+2] + ")");
		    break;
		case PathIterator.SEG_CLOSE:
		    System.out.println("CLOSE PATH");
		    break;
		}
		pit.next();
	    }
	}

	System.out.println("------ Path3D.Float case ----------");

	path = new Path3D.Float();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 6.0, 2.0, 4.0, 7.0, 0.0, 4.0, 8.0);
	path.curveTo(-2.0, 4.0, 9.0, -8.0, 4.0, 10.0, -8.0, 2.0, 11.0);
	path.closePath();
	path.lineTo(-7.0, 0.0, 12.0);
	path.lineTo(-7.0, 1.0, 13.0);
	path.closePath();
	path.moveTo(10.0, 10.0, 4.0);
	path.lineTo(11.0, 11.0, 15.0);
	path.moveTo(20.0, 20.0, 17.0);
	path.lineTo(30.0, 40.0, 17.0);
	path.lineTo(40.0, 40.0, 18.0);
	path.closePath();

	subpaths = PathSplitter.split(path);

	pit = path.getPathIterator(null);
	coords = new double[9];
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1]
				   + ", " + coords[2] + ")");
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1]
				   + ", " + coords[2] + ")");
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 6; i += 3)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ", " + coords[i+2] + ")");
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 9; i += 3)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ", " + coords[i+2] + ")");
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		break;
	    }
	    pit.next();
	}

	for (int j = 0; j < subpaths.length; j++) {
	    System.out.println("SUBPATH " + j + ":");
	    pit = subpaths[j].getPathIterator(null);
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		switch (type) {
		case PathIterator.SEG_MOVETO:
		    System.out.println("MOVETO (" + coords[0]
				       + ", " + coords[1]
				       + ", " + coords[2] + ")");
		    break;
		case PathIterator.SEG_LINETO:
		    System.out.println("LINETO (" + coords[0]
				       + ", " + coords[1]
				       + ", " + coords[2] + ")");
		    break;
		case PathIterator.SEG_QUADTO:
		    System.out.println("QUADTO:");
		    for (int i = 0; i < 6; i += 3)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ", " + coords[i+2] + ")");
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.out.println("CUBICTO:");
		    for (int i = 0; i < 9; i += 3)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ", " + coords[i+2] + ")");
		    break;
		case PathIterator.SEG_CLOSE:
		    System.out.println("CLOSE PATH");
		    break;
		}
		pit.next();
	    }
	}

	double startX = 44.0;
	double startY = 55.0;
	double startZ = 66.0;
	coords[0] = 50.0;
	coords[1] = 60.0;
	coords[2] = 70.0;
	coords[3] = 90.0;
	coords[4] = 100.0;
	coords[5] = 110.0;
	coords[6] = 105.0;
	coords[7] = 115.0;
	coords[8] = 125.0;
	double[] scoords = new double[18];

	PathSplitter.split(PathIterator3D.SEG_QUADTO, startX, startY, startZ,
			       coords, 0, scoords, 0, 0.4);

	double u = 0.25;

	double ex = Path3DInfo.getX(u, startX, startY, startZ,
				    PathIterator3D.SEG_QUADTO, coords);
	double ey = Path3DInfo.getY(u, startX, startY, startZ,
				    PathIterator3D.SEG_QUADTO, coords);
	double ez = Path3DInfo.getZ(u, startX, startY, startZ,
				    PathIterator3D.SEG_QUADTO, coords);

	u = 0.625;

	double x = Path3DInfo.getX(u, startX, startY, startZ,
				   PathIterator3D.SEG_QUADTO, scoords);
	double y = Path3DInfo.getY(u, startX, startY, startZ,
				   PathIterator3D.SEG_QUADTO, scoords);
	double z = Path3DInfo.getZ(u, startX, startY, startZ,
				   PathIterator3D.SEG_QUADTO, scoords);
	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}

	double sx = scoords[3];
	double sy = scoords[4];
	double sz = scoords[5];
	System.arraycopy(scoords, 6, scoords, 0, 6);

	u = 0.75;

	ex = Path3DInfo.getX(u, startX, startY, startZ,
			     PathIterator3D.SEG_QUADTO, coords);
	ey = Path3DInfo.getY(u, startX, startY, startZ,
			     PathIterator3D.SEG_QUADTO, coords);
	ez = Path3DInfo.getZ(u, startX, startY, startZ,
			     PathIterator3D.SEG_QUADTO, coords);

	u = (0.75-0.4)/0.6;

	x = Path3DInfo.getX(u, sx, sy, sz, PathIterator3D.SEG_QUADTO, scoords);
	y = Path3DInfo.getY(u, sx, sy, sz, PathIterator3D.SEG_QUADTO, scoords);
	z = Path3DInfo.getZ(u, sx, sy, sz, PathIterator3D.SEG_QUADTO, scoords);

	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}

	PathSplitter.split(PathIterator3D.SEG_CUBICTO, startX, startY, startZ,
			   coords, 0, scoords, 0, 0.4);

	u = 0.25;

	ex = Path3DInfo.getX(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);
	ey = Path3DInfo.getY(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);
	ez = Path3DInfo.getZ(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);

	u = 0.625;

	x = Path3DInfo.getX(u, startX, startY, startZ,
			    PathIterator3D.SEG_CUBICTO, scoords);
	y = Path3DInfo.getY(u, startX, startY, startZ,
			    PathIterator3D.SEG_CUBICTO, scoords);
	z = Path3DInfo.getZ(u, startX, startY, startZ,
			    PathIterator3D.SEG_CUBICTO, scoords);

	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}


	sx = scoords[6];
	sy = scoords[7];
	sz = scoords[8];
	System.arraycopy(scoords, 9, scoords, 0, 9);

	u = 0.75;

	ex = Path3DInfo.getX(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);
	ey = Path3DInfo.getY(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);
	ez = Path3DInfo.getZ(u, startX, startY, startZ,
			     PathIterator3D.SEG_CUBICTO, coords);

	u = (0.75-0.4)/0.6;

	x = Path3DInfo.getX(u, sx, sy, sz, PathIterator3D.SEG_CUBICTO, scoords);
	y = Path3DInfo.getY(u, sx, sy, sz, PathIterator3D.SEG_CUBICTO, scoords);
	z = Path3DInfo.getZ(u, sx, sy, sz, PathIterator3D.SEG_CUBICTO, scoords);

	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}

	PathSplitter.split(PathIterator3D.SEG_LINETO, startX, startY, startZ,
			   coords, 0, scoords, 0, 0.4);

	u = 0.25;

	ex = Path3DInfo.getX(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);
	ey = Path3DInfo.getY(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);
	ez = Path3DInfo.getZ(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);
	u = 0.625;

	x = Path3DInfo.getX(u, startX, startY, startZ,
			    PathIterator3D.SEG_LINETO, scoords);
	y = Path3DInfo.getY(u, startX, startY, startZ,
			    PathIterator3D.SEG_LINETO, scoords);
	z = Path3DInfo.getZ(u, startX, startY, startZ,
			    PathIterator3D.SEG_LINETO, scoords);

	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}


	sx = scoords[0];
	sy = scoords[1];
	sz = scoords[2];
	System.arraycopy(scoords, 3, scoords, 0, 3);

	u = 0.75;

	ex = Path3DInfo.getX(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);
	ey = Path3DInfo.getY(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);
	ez = Path3DInfo.getZ(u, startX, startY, startZ,
			     PathIterator3D.SEG_LINETO, coords);

	u = (0.75-0.4)/0.6;

	x = Path3DInfo.getX(u, sx, sy, sz, PathIterator3D.SEG_LINETO, scoords);
	y = Path3DInfo.getY(u, sx, sy, sz, PathIterator3D.SEG_LINETO, scoords);
	z = Path3DInfo.getZ(u, sx, sy, sz, PathIterator3D.SEG_LINETO, scoords);

	if (Math.abs(ex-x)>1.e-10 || Math.abs(ey-y) > 1.e-10
	    || Math.abs(ez-z)>1.e-10) {
	    throw new Exception("split path differs from original path");
	}

	System.out.println("---- subpath tests ------");

	BasicSplinePath3D bpath = new BasicSplinePath3D();
	bpath.moveTo(0.0, 0.0, 0.0);
	bpath.lineTo(10.0, 20.0, 30.0);
	bpath.lineTo(20.0, 40.0, 60.0);
	bpath.lineTo(30.0, 60.0, 90.0);
	Path3D subpath = PathSplitter.subpath(bpath, 0.4, 0.6);
	Path3DInfo.printSegments(System.out, subpath);

	double blen = Path3DInfo.pathLength(bpath);
	double splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen*0.2/3 - splen) > 1.e-10) {
	    throw new Exception("blen*0.2/3 = " + blen*0.2/3
				+ ", splen = " + splen);
	}
	subpath = PathSplitter.subpath(bpath, 0.4, 1.6);
	splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen*1.2/3 - splen) > 1.e-10) {
	    throw new Exception("blen*1.2/3 = " + blen*1.2/3
				+ ", splen = " + splen);
	}

	subpath = PathSplitter.subpath(bpath, 0.4, 2.4);
	splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen*2.0/3 - splen) > 1.e-10) {
	    throw new Exception("blen*2.0/3 = " + blen*2.0/3
				+ ", splen = " + splen);
	}

	bpath = new BasicSplinePath3D();
	bpath.moveTo(0.0, 0.0, 0.0);
	bpath.quadTo(7.0, 7.0, 7.0,
		     10.0, 20.0, 30.0);
	bpath.quadTo(15.0, 15.0, 15.0,
		     20.0, 40.0, 60.0);
	bpath.quadTo(25.0, 25.0, 25.0,
		     30.0, 60.0, 90.0);
	blen = bpath.getPathLength(0.4, 0.6);
	subpath = PathSplitter.subpath(bpath, 0.4, 0.6);
	splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen - splen) > 1.e-5) {
	    throw new Exception("blen = " + blen
				+ ", splen = " + splen);
	}
	blen = bpath.getPathLength(0.4, 1.6);
	subpath = PathSplitter.subpath(bpath, 0.4, 1.6);
	splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen - splen) > 1.e-5) {
	    throw new Exception("blen = " + blen*1.2/3
				+ ", splen = " + splen);
	}

	blen = bpath.getPathLength(0.4, 2.4);
	subpath = PathSplitter.subpath(bpath, 0.4, 2.4);
	splen = Path3DInfo.pathLength(subpath);
	if (Math.abs(blen - splen) > 1.e-5) {
	    Path3DInfo.printSegments(System.out, subpath);
	    System.out.println("bpath length [0.0, 0.4] = "
			       + bpath.getPathLength(0.0, 0.4));
	    System.out.println("bpath length [0.4, 1.0] = "
			       + bpath.getPathLength(0.4, 1.0));
	    System.out.println("bpath length [1.0, 2.0] = "
			       + bpath.getPathLength(1.0, 2.0));
	    System.out.println("bpath length [2.0, 2.4] = "
			       + bpath.getPathLength(2.0, 2.4));
	    System.out.println("bpath length [2.4, 3.0] = "
			       + bpath.getPathLength(2.4, 3.0));

	    System.out.println("path segment lengths:");
	    for (Path3DInfo.Entry entry: Path3DInfo.getEntries(bpath)) {
		System.out.println("      " + entry.getSegmentLength());
	    }
	    System.out.println("subpath lengths:");
	    for (Path3DInfo.Entry entry: Path3DInfo.getEntries(subpath)) {
		System.out.println("      " + entry.getSegmentLength());
	    }
	    throw new Exception("blen = " + blen
				+ ", splen = " + splen);
	}
	// System.exit(0);

	System.out.println("--- randomly generated tests --- ");

	IntegerRandomVariable rv = new UniformIntegerRV(1,4);
	for (int k = 0; k < 100; k++) {
	    bpath = new BasicSplinePath3D();
	    bpath.moveTo(0.0, 0.0, 0.0);
	    int cnt = 0;
	    z = 0.0;
	    for (int i = 1; i < 120; i++) {
		double z1 = z++;
		double z2 = z++;
		double z3 = z++;
		// System.out.format("(z1, z2, z3) = (%g, %g, %g)\n", z1, z2, z3);
		double r = i;
		double theta = Math.PI/10 * i;
		double r1, r2, theta1, theta2;
		switch (rv.next()) {
		case PathIterator.SEG_LINETO:
		    bpath.lineTo(r*Math.cos(theta), r*Math.sin(theta), z3);
		    break;
		case PathIterator.SEG_QUADTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    bpath.quadTo(r*Math.cos(theta), r*Math.sin(theta),
				 (z1 + z2)/2,
				 r1*Math.cos(theta1), r1*Math.sin(theta1), z3);
		case PathIterator.SEG_CUBICTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    i++;
		    r2 = i;
		    theta2 = Math.PI/10 * i;
		    bpath.curveTo(r*Math.cos(theta), r*Math.sin(theta), z1,
				  r1*Math.cos(theta1), r1*Math.sin(theta1), z2,
				  r2*Math.cos(theta2), r2*Math.sin(theta2), z3);
		}
	    }
	    double umax = bpath.getMaxParameter();
	    int max = (int)(Math.round(umax) * 10);
	    IntegerRandomVariable urv = new UniformIntegerRV(0,true, max,true);
	    for (int i = 0; i < 1000; i++) {
		int ifactor = urv.next();
		double value1;
		if (ifactor % 10 == 0) {
		    value1 = (double)(ifactor/10);
		} else {
		    value1 = (ifactor / 10.0);
		}
		double value2;
		do {
		    ifactor = urv.next();
		    if (ifactor % 10 == 0) {
			value2 = (double)(ifactor/10);
		    } else {
			value2 = (ifactor / 10.0);
		    }
		} while (value1 == value2);
		double u1, u2;
		if (value1 < value2) {
		    u1 = value1;
		    u2 = value2;
		} else {
		    u1 = value2;
		    u2 = value1;
		}
		Path3D p = PathSplitter.subpath(bpath, u1, u2);
		Path3D pstart = (u1 > 0)? PathSplitter.subpath(bpath, 0.0, u1):
		    new Path3D.Double();
		Path3D pend = (u2 < umax)?
		    PathSplitter.subpath(bpath, u2, umax): new Path3D.Double();
		double olen = bpath.getPathLength(u1, u2);
		double plen = Path3DInfo.pathLength(p);
		double flen = Path3DInfo.pathLength(bpath);
		double slen = Path3DInfo.pathLength(pstart);
		double elen = Path3DInfo.pathLength(pend);
		double r = Math.abs((flen - (slen + plen + elen))/flen);
		if (r > 1.e-5) {
		    System.out.format("u1=%g, u2=%g, slen=%s, plen=%s, elen=%s,"
				      + "  flen = %s\n",
				      u1, u2, slen, plen, elen, flen);
		    PrintWriter out = new PrintWriter("pstest.log", "UTF-8");
		    out.format("u1 = %s, u2 = %s\n", u1, u2);
		    out.format("X at u1 = %s\n", bpath.getX(u1));
		    out.format("Y at u1 = %s\n", bpath.getY(u1));
		    out.format("Z at u1 = %s\n", bpath.getZ(u1));
		    out.format("X at u2 = %s\n", bpath.getX(u2));
		    out.format("Y at u2 = %s\n", bpath.getY(u2));
		    out.format("Z at u2 = %s\n", bpath.getZ(u2));
		    out.println("PATH:");
		    Path3DInfo.printSegments("    ", out, bpath);
		    out.println("SPLIT PATH:");
		    Path3DInfo.printSegments("    ", out, p);
		    out.format("split path length = %s, path lenth = %s\n",
			       plen, olen);
		    System.out.format("slen=%s, plen=%s, elen=%s, flen = %s\n",
				      slen, plen, elen, flen);
		    out.flush();
		    out.close();
		    throw new Exception(" r = " + r);
		}

		if (Math.abs((olen - plen)/olen) > 1.e-5) {
		    PrintWriter out = new PrintWriter("pstest.log", "UTF-8");
		    out.format("u1 = %s, u2 = %s\n", u1, u2);
		    out.format("X at u1 = %s\n", bpath.getX(u1));
		    out.format("Y at u1 = %s\n", bpath.getY(u1));
		    out.format("Z at u1 = %s\n", bpath.getZ(u1));
		    out.format("X at u2 = %s\n", bpath.getX(u2));
		    out.format("Y at u2 = %s\n", bpath.getY(u2));
		    out.format("Z at u2 = %s\n", bpath.getZ(u2));
		    out.println("PATH:");
		    Path3DInfo.printSegments("    ", out, bpath);
		    out.println("SPLIT PATH:");
		    Path3DInfo.printSegments("    ", out, p);
		    out.format("split path length = %s, path lenth = %s\n",
			       plen, olen);
		    out.flush();
		    out.close();
		    String msg = String.format("lengths: %s %s: %g\n",
					       plen, olen,
					       Math.abs((plen - olen)/olen));
		    throw new Exception(msg);
		}
	    }
	}


	System.exit(0);
    }
}
