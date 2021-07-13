import org.bzdev.geom.*;
import org.bzdev.math.*;
import java.awt.geom.*;

public class Path3DInfoTest {
    public static void main(String argv[]) throws Exception {
	Path3D path = new Path3D.Double();
	path.moveTo(-8.0, 0.0, 0.0);
	path.lineTo(-8.0, -4.0, 1.0);
	path.lineTo(0.0, -4.0, 1.0);
	path.quadTo(8.0, -4.0, 1.5, 8.0, 0.0, 2.0);
	path.curveTo(8.0, 2.0, 2.3, 2.0, 4.0, 2.5, 0.0, 4.0, 3.0);
	path.curveTo(-2.0, 4.0, 2.5, -8.0, 4.0, 3.0, -8.0, 2.0, 3.5);
	path.closePath();
	path.lineTo(-7.0, 0.0, 1.0);
	path.lineTo(-7.0, 1.0, 2.0);
	path.closePath();

	int j = 0;
	PathIterator3D pit = path.getPathIterator(null);
	double[] coords = new double[9];
	double x = 0.0; double y = 0.0; double z = 0.0;
	double x0 = 0.0; double y0 = 0.0; double z0 = 0.0;;
	double lastX = 0.0; double lastY = 0.0; double lastZ = 0.0;
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		x = coords[0];
		y = coords[1];
		z = coords[2];
		lastX = x;
		lastY = y;
		lastZ = z;
		if (x != Path3DInfo.getX(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getX failed");
		}
		if (y != Path3DInfo.getY(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getY failed");
		}
		if (z != Path3DInfo.getZ(0.5, 0.0, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path3DInfo.getZ failed");
		}
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z,
						 type, coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z,
						 type, coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-5) {
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-5) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-5) {
			throw new Exception("Z derivative failed");
		    }
		    if (xpp != 0.0 || ypp != 0.0 || zpp != 0.0) {
			throw new Exception
			    ("non-zero second derivative");
		    }
		}
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z, type,
						 coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z, type,
						 coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " +(Math.abs(xp - (xv1-xv)/0.00001)));
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println("coords[2] = " + coords[2]
					   + ", coords[3] = " + coords[3]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));

			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-4) {
			throw new Exception("Z derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 1.e-4) {
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 1.e-4) {
			throw new Exception("Y second derivative failed");
		    }
		    if (Math.abs(zpp - (zp1-zp)/0.00001) > 1.e-4) {
			throw new Exception("Z second derivative failed");
		    }
		}
		x = coords[3];
		y = coords[4];
		z = coords[5];
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path3DInfo.getX(0.4, x, y, z, type, coords);
		    double yv = Path3DInfo.getY(0.4, x, y, z, type, coords);
		    double zv = Path3DInfo.getZ(0.4, x, y, z, type, coords);
		    double xv1 = Path3DInfo.getX(0.40001, x, y, z,
						 type, coords);
		    double yv1 = Path3DInfo.getY(0.40001, x, y, z,
						 type, coords);
		    double zv1 = Path3DInfo.getZ(0.40001, x, y, z,
						 type, coords);
		    double xp = Path3DInfo.dxDu(0.4, x, y, z, type, coords);
		    double yp = Path3DInfo.dyDu(0.4, x, y, z, type, coords);
		    double zp = Path3DInfo.dzDu(0.4, x, y, z, type, coords);
		    double xp1 = Path3DInfo.dxDu(0.40001, x, y, z,
						 type, coords);
		    double yp1 = Path3DInfo.dyDu(0.40001, x, y, z,
						 type, coords);
		    double zp1 = Path3DInfo.dzDu(0.40001, x, y, z,
						 type, coords);
		    double xpp = Path3DInfo.d2xDu2(0.4, x, y, z, type, coords);
		    double ypp = Path3DInfo.d2yDu2(0.4, x, y, z, type, coords);
		    double zpp = Path3DInfo.d2zDu2(0.4, x, y, z, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " + Math.abs(xp - (xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(zp - (zv1-zv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(xpp - (xp1-xp)/0.00001));
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(ypp - (yp1-yp)/0.00001));
			throw new Exception("Y second derivative failed");
		    }
		    if (Math.abs(zpp - (zp1-zp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(ypp - (yp1-yp)/0.00001));
			throw new Exception("Y second derivative failed");
		    }
		}
		x = coords[6];
		y = coords[7];
		z = coords[8];
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		coords[0] = lastX;
		coords[1] = lastY;
		coords[2] = lastZ;
		x = coords[0];
		y = coords[1];
		z = coords[2];
		break;
	    }
	    System.out.println("     ... static-method segment length = "
			       + Path3DInfo.segmentLength(type,
							  x0, y0, z0,
							  coords));
	    System.out.println("     ... segment length["
			       + j
			       +"] = " +
			       Path3DInfo.segmentLength(path, j++));
	    x0 = x;
	    y0 = y;
	    z0 = z;
	    pit.next();
	}

	Path3DInfo.SegmentData sd = null;
	double[] tarray = new double[10];
	double[] narray = new double[10];
	double[] barray = new double[10];
	for(Path3DInfo.Entry entry: Path3DInfo.getEntries(path)) {
	    Point3D start = entry.getStart();
	    x0 = (start == null)? 0.0: start.getX();
	    y0 = (start == null)? 0.0: start.getY();
	    z0 = (start == null)? 0.0: start.getZ();
	    double[] coordinates = entry.getCoords();
	    int st = entry.getType();
	    if (st == PathIterator.SEG_MOVETO) continue;
	    double u = 0.4;
	    double eps = 0.00001;
	    Path3DInfo.UValues uv = new Path3DInfo.UValues(u);
	    sd = new Path3DInfo.SegmentData(st, x0, y0, z0, coordinates, sd);
	    double x1 = Path3DInfo.getX(u, x0, y0, z0, st, coordinates);
	    double y1 = Path3DInfo.getY(u, x0, y0, z0, st, coordinates);
	    double z1 =Path3DInfo.getZ(u, x0, y0, z0, st, coordinates);
	    double x1a = Path3DInfo.getX(u+eps, x0, y0, z0, st, coordinates);
	    double y1a = Path3DInfo.getY(u+eps, x0, y0, z0, st, coordinates);
	    double z1a = Path3DInfo.getZ(u+eps, x0, y0, z0, st, coordinates);
	    double x1b = Path3DInfo.getX(u+2*eps, x0, y0, z0, st, coordinates);
	    double y1b = Path3DInfo.getY(u+2*eps, x0, y0, z0, st, coordinates);
	    double z1b = Path3DInfo.getZ(u+2*eps, x0, y0, z0, st, coordinates);
	    double[] etangent = {x1a - x1, y1a - y1, z1a - z1};
	    double[] etangent2 = {x1b - x1a, y1b - y1a, z1b - z1a};
	    double ds1a = Math.sqrt((x1a - x1)*(x1a-x1)
				    + (y1a - y1)*(y1a-y1)
				    + (z1a - z1)*(z1a-z1));
	    double ds1b = Math.sqrt((x1b - x1a)*(x1b-x1a)
				    + (y1b - y1a)*(y1b-y1a)
				    + (z1b - z1a)*(z1b-z1a));
	    double edsDu1 = ds1a/eps;
	    double edsDu1A = ds1b/eps;
	    double ed2sDu21 = (edsDu1A - edsDu1)/eps;
	    VectorOps.normalize(etangent);
	    VectorOps.normalize(etangent2);
	    double[] enormal = new double[3];
	    VectorOps.sub(enormal, etangent2, etangent);
	    try {
		VectorOps.normalize(enormal);
	    } catch (Exception e) {
		enormal[0] = 0; enormal[1] = 0; enormal[2] = 0;
	    }
	    double dxDu1 = Path3DInfo.dxDu(u, x0, y0, z0, st, coordinates);
	    double dyDu1 = Path3DInfo.dyDu(u, x0, y0, z0, st, coordinates);
	    double dzDu1 = Path3DInfo.dzDu(u, x0, y0, z0, st, coordinates);
	    double d2xDu21 = Path3DInfo.d2xDu2(u, x0, y0, z0, st, coordinates);
	    double d2yDu21 = Path3DInfo.d2yDu2(u, x0, y0, z0, st, coordinates);
	    double d2zDu21 = Path3DInfo.d2zDu2(u, x0, y0, z0, st, coordinates);
	    double dsDu1 = Path3DInfo.dsDu(u, x0, y0, z0, st, coordinates);
	    double d2sDu21 = Path3DInfo.d2sDu2(u, x0, y0, z0, st, coordinates);
	    if (Math.abs(dsDu1 - edsDu1) > 1.e-3) {
		throw new Exception("dsDu");
	    }
	    if (Math.abs(d2sDu21 - ed2sDu21) > 1.e-3) {
		throw new Exception("d2sDu2: " + d2sDu21 + " != "
				    + ed2sDu21);
	    }

	    double curv1 = Path3DInfo.curvature(u, x0, y0, z0, st, coordinates);
	    boolean tstat1 = Path3DInfo.getTangent(u, tarray, 5, x0, y0, z0,
						   st, coordinates);
	    boolean nstat1 = Path3DInfo.getNormal(u, narray, 5, x0, y0, z0,
						   st, coordinates);
	    boolean bstat1 = Path3DInfo.getBinormal(u, barray, 5, x0, y0, z0,
						    st, coordinates);
	    double x2 = sd.getX(uv);
	    double y2 = sd.getY(uv);
	    double z2 = sd.getZ(uv);
	    double dxDu2 = sd.dxDu(uv);
	    double dyDu2 = sd.dyDu(uv);
	    double dzDu2 = sd.dzDu(uv);
	    double d2xDu22 = sd.d2xDu2(uv);
	    double d2yDu22 = sd.d2yDu2(uv);
	    double d2zDu22 = sd.d2zDu2(uv);
	    double dsDu2 = sd.dsDu(uv);
	    double d2sDu22 = sd.d2sDu2(uv);
	    double curv2 = sd.curvature(uv);
	    boolean tstat2 = sd.getTangent(uv, tarray, 0);
	    boolean nstat2 = sd.getNormal(uv, narray, 0);
	    boolean bstat2 = sd.getBinormal(uv, barray, 0);
	    if (Math.abs(x1 - x2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + x1 + " != " + x2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(y1 - y2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + y1 + " != " + y2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(z1 - z2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + z1 + " != " + z2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dxDu1 - dxDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dxDu1 + " != " + dxDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dyDu1 - dyDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dyDu1 + " != " + dyDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dzDu1 - dzDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dzDu1 + " != " + dzDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2xDu21 - d2xDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2yDu21 - d2yDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2zDu21 - d2zDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2zDu21 + " != " + d2zDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(dsDu1 - dsDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dsDu1 + " != " + dsDu2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2sDu21 - d2sDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2sDu21 + " != " + d2sDu22 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (Math.abs(curv1 - curv2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + curv1 + " != " + curv2 + " ... st = "
		     + Path3DInfo.getTypeString(st));
	    }
	    if (tstat1 != tstat2) {
		throw new Exception("tstat1 != tstat2");
	    }
	    if (nstat1 != nstat2) {
		throw new Exception("nstat1 != nstat2");
	    }
	    if (tstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(tarray[i] - tarray[5+i]) > 1.e-10) {
			throw new Exception("tangents differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + tarray[i] + " != " + tarray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(tarray, 0, tarray, 0, 3)
			     - 1.0) > 1.e-10) {
		    throw new Exception("Tangent not a unit victor");
		}
	    }
	    if (nstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(narray[i] - narray[5+i]) > 1.e-10) {
			throw new Exception("normal vectors differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + narray[i] + " != " + narray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(narray, 0, narray, 0, 3)
			     - 1.0) > 1.e-10) {
		    throw new Exception("Normal vector  not a unit victor");
		}
		if (tstat1) {
		    double dp = VectorOps.dotProduct(narray, 0, tarray, 0, 3);
		    if (Math.abs(dp) > 1.e-10) {
			System.out.format("tarray = (%g, %g, %g)\n",
					  tarray[0], tarray[1], tarray[2]);
			System.out.format("etangent = (%g, %g, %g)\n",
					  etangent[0], etangent[1],
					  etangent[2]);
			System.out.format("narray = (%g, %g, %g)\n",
					  narray[0], narray[1], narray[2]);
			System.out.format("enormal = (%g, %g, %g)\n",
					  enormal[0], enormal[1], enormal[2]);
			System.out.println("enormal dot etangent = "
					   + VectorOps.dotProduct(etangent,
								  enormal));
			System.out.println("type = " + entry.getTypeString());
			System.out.println("angle = "
					   + Math.toDegrees(Math.acos(dp))
					   + " degrees");
			throw new Exception("tangent and normal vectors not "
					    + "perpendicular");
		    }
		} else {
		    throw new Exception("normal vector but no tangent?");
		}
	    }
	    if (bstat1) {
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(barray[i] - barray[5+i]) > 1.e-10) {
			throw new Exception("binormal vectors differ (u = " + u
					    + ", i = " + i + ", st = "
					    + st + "): "
					    + barray[i] + " != " + barray[5+i]);
		    }
		}
		if (Math.abs(VectorOps.dotProduct(barray, 0, barray, 0, 3)
			     - 1.0) > 1.e-10) {
		    System.out.format("barray = (%g, %g, %g)\n",
				      barray[0], barray[1], barray[2]);
		    System.out.println("type = " + entry.getTypeString());
		    throw new Exception("Binormal vector  not a unit victor");
		}
	    }
	    if (nstat1 && bstat1) {
		if (Math.abs(VectorOps.dotProduct(narray, 0, barray, 0, 3))
		    > 1.e-10) {
		    throw new Exception("Binormal and Normal vectors "
					+ "not orthogonal");

		}
	    }
	    if (tstat1 && bstat1) {
		if (Math.abs(VectorOps.dotProduct(tarray, 0, barray, 0, 3))
		    > 1.e-10) {
		    throw new Exception("Binormal and tangent vectors "
					+ "not orthogonal");

		}
	    }
	}

	int segStart = 2;
	int segEnd = 6;
	double total = 0.0;
	double segtotal = 0.0;
	for(Path3DInfo.Entry entry: Path3DInfo.getEntries(path)) {
	    System.out.println("index = " + entry.getIndex()
			       + ", type = " + entry.getTypeString()
			       + ", start = " + entry.getStart()
			       + ", end = " + entry.getEnd()
			       + ", length = " +entry.getSegmentLength());
	    total += entry.getSegmentLength();
	    int ind = entry.getIndex();
	    if (ind >= segStart && ind < segEnd) {
		segtotal += entry.getSegmentLength();
	    }
	    double[] c = entry.getCoords();
	    System.out.println("coords[0] = " + c[0]);
	}


	System.out.println("path length = " + Path3DInfo.pathLength(path));
	System.out.println("path length for segments ["
			   + segStart + ", " + segEnd +") = "
			   + Path3DInfo.pathLength(path, segStart, segEnd));
	if (Math.abs(total - Path3DInfo.pathLength(path)) > 1.e-8) {
	    System.out.println("wrong path length");
	    System.exit(1);
	}
	if (Math.abs(segtotal - Path3DInfo.pathLength
		     (path, segStart, segEnd)) > 1.e-8) {
	    System.out.println("wrong segment-range length");
	    System.exit(1);
	}

	Path3D path1 = new Path3D.Double();
	Path3D path2 = new Path3D.Double();

	path1.moveTo(0.0, 0.0, 0.0);
	path2.moveTo(0.0, 0.0, 0.0);
	path1.curveTo(100.0, 0.0, 0.0, 100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	path2.quadTo(100.0, 0.0, 0.0, 100.0, 100.0, 0.0);
	
	AffineTransform3D af =
	    AffineTransform3D.getRotateInstance(0.0, Math.PI/6.0, 0.0);

	Path3D path1t = new Path3D.Double(path1, af);
	Path3D path2t = new Path3D.Double(path2, af);

	System.out.println("path1 length: "
			   + Path3DInfo.segmentLength(path1, 1));
	System.out.println("path2 length: "
			   + Path3DInfo.segmentLength(path2, 1));
	System.out.println("path11 length: "
			   + Path3DInfo.segmentLength(path1t, 1));
	System.out.println("path2t length: "
			   + Path3DInfo.segmentLength(path2t, 1));
	coords = new double[9];
	coords[0] = 165.2016; coords[1] = 3.9624;
	coords[3] = 165.2016; coords[4] = 15.8596;

	double[] coordst = new double[9];
	af.transform(coords, 0, coordst, 0, 2);

	double spoint[] = {152.4, 3.9624, 0.0};
	double tpoint[] = new double[3];
	af.transform(spoint, 0, tpoint, 0, 1);
		
	System.out.println("canned length = "
			   + Path3DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624, 0.0,
			    coords));

	System.out.println("canned length after transform = "
			   + Path3DInfo.segmentLength
			   (PathIterator.SEG_QUADTO,
			    tpoint[0], tpoint[1], tpoint[2], coordst));

	double[] coords2 = new double[6];
	coords2[0] = 165.2016; coords2[1] = 3.9624;
	coords2[2] = 165.2016; coords2[3] = 15.8596;

	System.out.println("canned length (2D) = "
			   + Path2DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624,
			    coords2));

	RealValuedFunction fx = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
		public double derivAt(double theta) {
		    return -100.0 * Math.sin(theta);
		}
	    };
	RealValuedFunction fy = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.sin(theta);
		}
		public double derivAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
	    };
	final double angle = Math.PI/6.0;
	RealValuedFunction fz = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return theta * 100.0 * Math.tan(angle);
		}
		public double derivAt(double theta) {
		    return 100.0 * Math.tan(angle);
		}
	    };

	CubicSpline xspline = new CubicSpline1(fx, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline yspline = new CubicSpline1(fy, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline zspline = new CubicSpline1(fz, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	pit = Path3DInfo.getPathIterator(null, xspline, yspline, zspline);
	path = new Path3D.Double();
	path.append(pit, false);

	double actual = Math.PI * 100.0 / Math.cos(angle);
	if (Math.abs(Path3DInfo.pathLength(path) - actual) > 1.e-3) {
	    System.out.println("path length for skewed semicircle = "
			       + Path3DInfo.pathLength(path)
			       + ", expecting " + actual);
	    System.exit(1);
	}

	double ocoords[] = {1.0, 2.0, 3.0, 6.0, 7.0, 8.0, 10.0, 20.0, 30.0};
	double ocoords1[] = {6.0, 7.0, 8.0, 10.0, 20.0, 30.0};
	double[] ncoords = new double[12];
	double[] ncoords1 = new double[9];

	Path3DInfo.elevateDegree(2, ncoords, 0, ocoords, 0);
	Path3DInfo.elevateDegree(2, ncoords1, 1.0, 2.0, 3.0, ocoords1);

	if (ncoords[0] != ocoords[0] || ncoords[1] != ocoords[1]
	    || ncoords[2] != ocoords[2]
	    || ncoords[9] != ocoords[6] || ncoords[10] != ocoords[7]
	    || ncoords[11] != ocoords[8]) {
	    throw new Exception("ncoords error");
	}

	for (int i = 0; i < 9; i++) {
	    if (Math.abs(ncoords1[i] - ncoords[i+3]) > 1.e-10) {
	        System.out.format("ncoords1[%d] = %g while ncoords[%d] = %g\n",
				  i, ncoords1[i], i+3, ncoords[i+3]);
		throw new Exception("ncoords error");
	    }
	}

	for (int i = 0; i <= 20; i++) {
	    double u = i/20.0;
	    if (u < 0) u = 0.0;
	    if (u > 1) u = 1.0;
	    double v1 = Path3DInfo.getX(u, 1.0, 2.0, 3.0,
					PathIterator.SEG_QUADTO,
					ocoords1);
	    double v2 =  Path3DInfo.getX(u, 1.0, 2.0, 3.0,
					 PathIterator.SEG_CUBICTO,
					 ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getX(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path3DInfo.getY(u, 1.0, 2.0, 3.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path3DInfo.getY(u, 1.0, 2.0, 3.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getY(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path3DInfo.getZ(u, 1.0, 2.0, 3.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path3DInfo.getZ(u, 1.0, 2.0, 3.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getZ(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	}

	path = new Path3D.Double();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0);

	path2 = Path3DInfo.shiftClosedPath(path, 8.0, 0.0, 5.0);

	if (path2 != null) throw new Exception("null path expected");
	
	path.closePath();	

	path2 = Path3DInfo.shiftClosedPath(path, 8.0, 0.0, 5.0);
	double[][] expecting = {
	    {8.0, 0.0, 5.0},
	    {8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0},
	    {-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0},
	    {-8.0, 0.0, 1.0},
	    {-8.0, -4.0, 2.0},
	    {0.0, -4.0, 3.0},
	    {8.0, -4.0, 4.0, 8.0, 0.0, 5.0},
	    {}
	};

	PathIterator3D pi = path2.getPathIterator(null);
	int cnt = 0;
	double[] pcoords = new double[9];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			System.out.println("cnt = " + cnt);
			System.out.format("pcoords = {%g, %g, %g}\n",
					  pcoords[0], pcoords[1], pcoords[2]);
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}
	// Path3DInfo.printSegments(path2);

	path.lineTo(-7.0, 0.0, 10.0);
	path.lineTo(-7.0, 1.0, 11.0);
	Path3D path3 = Path3DInfo.shiftClosedPath(path, 8.0, 0.0, 5.0);

	coords2 = new double[9];
	double[] coords3 = new double[9];

	PathIterator3D pi2 = path2.getPathIterator(null);
	PathIterator3D pi3 = path3.getPathIterator(null);
	while (!pi2.isDone() && !pi3.isDone()) {
	    int type2 = pi2.currentSegment(coords2);
	    int type3 = pi3.currentSegment(coords3);
	    if (type2 != type3) throw new Exception("types");
	    switch(type2) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		break;
	    }
	    pi2.next();
	    pi3.next();
	}
	if (pi2.isDone() != pi3.isDone()) {
	    throw new Exception("path segments");
	}

	path.reset();

	path.moveTo(10.0, 20.0, -1.0);
	path.lineTo(30.0, 40.0, -2.0);
	path.moveTo(40.0, 40.0, -3.0);
	path.lineTo(50.0, 40.0, -4.0);
	path.lineTo(50.0, 50.0, -5.0);
	path.moveTo(40.0, 40.0, -6.0);
	path.closePath();
	path.moveTo(-8.0, 0.0, 1.0);
	path.lineTo(-8.0, -4.0, 2.0);
	path.lineTo(0.0, -4.0, 3.0);
	path.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 2.0, 1.0);
	path.closePath();	

	path2 = Path3DInfo.shiftClosedPath(path, 8.0, 0.0, 5.0);
	// This is actually no shift at all: tested as a corner case
	System.out.println("setting up path4 & path3 ");
	Path3D path4 = new Path3D.Double();
	path4.moveTo(-8.0, 0.0, 1.0);
	path4.lineTo(-8.0, -4.0, 2.0);
	path4.lineTo(0.0, -4.0, 3.0);
	path4.quadTo(8.0, -4.0, 4.0, 8.0, 0.0, 5.0);
	path4.curveTo(8.0, 2.0, 5.0, 2.0, 4.0, 6.0, 0.0, 4.0, 7.0);
	path4.curveTo(-2.0, 4.0, 1.0, -8.0, 4.0, 1.0, -8.0, 0.0, 1.0);
	path4.closePath();

	path3 = Path3DInfo.shiftClosedPath(path4, -8.0, 0.0, 1.0);
	if (path3 == null) throw new Exception("path3 is null");


	System.out.println("path4:");
	Path3DInfo.printSegments(path4);
	System.out.println("path3:");
	Path3DInfo.printSegments(path3);

	pit = path4.getPathIterator(null);
	PathIterator3D pit3 = path3.getPathIterator(null);
	double[] bcoords = new double[9];
	double[] bcoords2 = new double[9];
	int count = 0;
	while (!pit.isDone() && ! pit3.isDone()) {
	    int type = pit.currentSegment(bcoords);
	    int type2 = pit3.currentSegment(bcoords2);
	    if (type != type2) throw new Exception("type at " + count);
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
	    case PathIterator3D.SEG_LINETO:
		for (int k = 0; k < 3; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		for (int k = 0; k < 6; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_CUBICTO:
		for (int k = 0; k < 9; k++) {
		    if (bcoords[k] != bcoords2[k]) {
			throw new Exception("bcoords !+ bcoords2");
		    }
		}
		break;
	    case PathIterator3D.SEG_CLOSE:
		break;
	    }
	    pit.next();
	    pit3.next();
	    count++;
	}
	if (pit.isDone() != pit3.isDone()) {
	    throw new Exception("boundary length mismatch");
	}



	pi = path2.getPathIterator(null);
	cnt = 0;
	pcoords = new double[9];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 3; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}

	System.exit(0);
    }
}
