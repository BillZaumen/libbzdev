package org.bzdev.p3d;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.math.VectorOps;
import org.bzdev.math.LUDecomp;
// import java.awt.geom.Line2D;
import java.util.*;

//@exbundle org.bzdev.p3d.lpack.P3d

class Triangles {

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
    }

    double dp[][] = new double[3][3];
    boolean planar[] = new boolean[3];

    double minx, miny, minz;
    double xrange, yrange, zrange;
    int n = 25;
    int np1 = n+1;
    double deltax;
    double deltay;
    double deltaz;

    static class TList extends LinkedList<Model3D.Triangle> {
    }

    void updateListsAndMap(Model3D.Triangle triangle,
			   TList lists[],
			   Map<Model3D.Triangle,ArrayList<Integer>> tmap)
    {
	double tminx = triangle.getMinX();
	double tminy = triangle.getMinY();
	double tminz = triangle.getMinZ();
	double tmaxx = triangle.getMaxX();
	double tmaxy = triangle.getMaxY();
	double tmaxz = triangle.getMaxZ();
	

	int indexX = (int)Math.round(Math.floor((tminx-minx)/deltax));
	int indexY = (int)Math.round(Math.floor((tminy-miny)/deltay));
	int indexZ = (int)Math.round(Math.floor((tminz-minz)/deltaz));
	int index = indexX*np1*np1 +indexY*np1 + indexZ;

	ArrayList<Integer>tlist = new ArrayList<Integer>();
	
	double x = tminx;
	int i = indexX;
	while (x <= tmaxx) {
	    int j = indexY;
	    double y = tminy;
	    while (y <= tmaxy) {
		int k = indexZ;
		double z = tminz;
		while (z <= tmaxz) {
		    int ind = i*np1*np1 + j*np1 + k;
		    tlist.add(ind);
		    if (lists[ind] == null) {
			lists[ind] = new TList();
		    }
		    lists[ind].add(triangle);
		    z += deltaz;
		    k++;
		}
		y += deltay;
		j++;
	    }
	    x += deltax;
	    i++;
	}
	tmap.put(triangle, tlist);
    }

    final static int MAX_TESSELLATION_LEVEL = 10;

    LinkedList<Model3D.Triangle> verify(Model3D m3d) {
	LinkedList<Model3D.Triangle> result = null;
	TList[] lists =
	    new TList[np1*np1*np1];
	Map<Model3D.Triangle, ArrayList<Integer>> tmap =
	    new HashMap<Model3D.Triangle,ArrayList<Integer>>();
	minx = m3d.getMinX();
	miny = m3d.getMinY();
	minz = m3d.getMinZ();
	xrange = m3d.getBoundingBoxX();
	yrange = m3d.getBoundingBoxY();
	zrange = m3d.getBoundingBoxZ();
	// to handle corner case of a zero-thickness linear sheet.
	if (xrange <= 0.0) xrange = 1.0;
	if (yrange <= 0.0) yrange = 1.0;
	if (zrange <= 0.0) zrange = 1.0;

	deltax = xrange/n;
	deltay = yrange/n;
	deltaz = zrange/n;

	if (m3d.cubics.size() > 0 || m3d.cubicVertices.size() > 0) {
	    int level = m3d.getTessellationLevel();
	    if (level > MAX_TESSELLATION_LEVEL) level = MAX_TESSELLATION_LEVEL;
	    Iterator<Model3D.Triangle> it = m3d.tessellate(level);
	    while (it.hasNext()) {
		Model3D.Triangle triangle = it.next();
		updateListsAndMap(triangle, lists, tmap);
	    }
	} else {
	    for (Model3D.Triangle triangle: m3d.triangleMap.values()) {
		updateListsAndMap(triangle, lists, tmap);
	    }
	    for (Model3D.Triangle triangle: m3d.triangleSet) {
		updateListsAndMap(triangle, lists, tmap);
	    }
	}
	Set<Model3D.Triangle> tset = new HashSet<Model3D.Triangle>();
	for (Map.Entry<Model3D.Triangle,ArrayList<Integer>>
		 entry: tmap.entrySet()) {
	    Model3D.Triangle t1 = entry.getKey();
	    tset.clear();
	    for (int ind: entry.getValue()) {
		for (Model3D.Triangle t2: lists[ind]) {
		    if (t2 != t1) {
			tset.add(t2);
		    }
		}
	    }
	    for (Model3D.Triangle t2: tset) {
		if (!areDisjoint(t1, t2)) {
		    if (result == null) {
			result = new LinkedList<Model3D.Triangle>();
			result.add(t1); result.add(t2);
		    }
		    return result;
		}
	    }
	}
	return result;
    }


    double[] coords = new double[3];
    synchronized boolean areDisjoint(Model3D.Triangle t1,
				     Model3D.Triangle t2)
    {
	return areDisjoint1(t1, t2) && areDisjoint1(t2, t1);

	/*

	if (oppositeTriangles(t1, t2)) {
	    // System.out.println("returning false 1");
	    return false;
	}
	boolean test = areDisjoint(t1, t2, dp, planar);
	if (test == false) {
	    // Also check with the triangles reversed since we use the
	    // normal vector for only one of the pair.
	    Model3D.Triangle tmp = t1;
	    t1 = t2;
	    t2 = tmp;
	    test = areDisjoint(t1, t2, dp, planar);
	}
	int cnt = 0;
	for (int i = 0; i < 3; i++) {
	    if (planar[i]) cnt++;
	}
	if (test == false) {
	    switch (cnt) {
	    case 0:
		boolean ta = getIntersection(coords, t1,
					     t2.x2, t2.y2, t2.z2,
					     t2.x3, t2.y3, t2.z3);
		double xa = coords[0];
		double ya = coords[1];
		double za = coords[2];
		boolean tb = getIntersection(coords, t1,
					     t2.x1, t2.y1, t2.z1,
					     t2.x3, t2.y3, t2.z3);
		double xb = coords[0];
		double yb = coords[1];
		double zb = coords[2];
		boolean tc = getIntersection(coords, t1,
					     t2.x1, t2.y1, t2.z1,
					     t2.x2, t2.y2, t2.z2);
		double xc = coords[0];
		double yc = coords[1];
		double zc = coords[2];
		boolean useAB = ta && tb;
		boolean useAC = ta && tc;
		boolean useBC = tb && tc;

		if (useAB) {
		    if (overlapsTriangle(t1, xa, ya, za, xb, yb, zb)) {
			// System.out.println("returning false 2");
			return false;
		    }
		    if (edgeIntersect(t1, xa, ya, za, xb, yb, zb)) {
			// System.out.println("returning false 3");
			return false;
		    }
		}
		if (useAC) {
		    if (overlapsTriangle(t1, xa, ya, za, xc, yc, zc)) {
			// System.out.println("returning false 4");
			return false;
		    }
		    if (edgeIntersect(t1, xa, ya, za, xc, yc, zc)) {
			// System.out.println("returning false 5");
			return false;
		    }
		}
		if (useBC) {
		    if (overlapsTriangle(t1, xb, yb, zb, xc, yc, zc)) {
			// System.out.println("returning false 6");
			return false;
		    }
		    if (edgeIntersect(t1, xb, yb, zb, xc, yc, zc)) {
			// System.out.println("returning false 7");
			return false;
		    }
		}
		return true;
	    case 1:
		if (planar[0]) {
		    // System.out.println("planar[0]");
		    boolean valid = getIntersection(coords, t1,
						    t2.x2, t2.y2, t2.z2,
						    t2.x3, t2.y3, t2.z3);
		    if (overlapsTriangle(t1, t2.x1, t2.y1, t2.z1,
					 coords[0], coords[1], coords[2],
					 valid)) {
			return false;
		    }
		    return !valid
			|| !edgeIntersect(t1, t2.x1, t2.y1, t2.z1,
					  coords[0], coords[1], coords[2]);
		} else if (planar[1]) {
		    // System.out.println("planar[1]");
		    boolean valid = getIntersection(coords, t1,
						    t2.x1, t2.y1, t2.z1,
						    t2.x3, t2.y3, t2.z3);
		    if (overlapsTriangle(t1, t2.x2, t2.y2, t2.z2,
					 coords[0], coords[1], coords[2],
					 valid)) {
			return false;
		    }
		    return !valid
			|| !edgeIntersect(t1, t2.x2, t2.y2, t2.z2,
					  coords[0], coords[1], coords[2]);
		} else if (planar[2]) {
		    // System.out.println("planar[2]");
		    boolean valid = getIntersection(coords, t1,
						    t2.x1, t2.y1, t2.z1,
						    t2.x2, t2.y2, t2.z2);
		    if (overlapsTriangle(t1, t2.x3, t2.y3, t2.z3,
					 coords[0], coords[1], coords[2],
					 valid)) {
			return false;
		    }
		    return !valid
			|| !edgeIntersect(t1, t2.x3, t2.y3, t2.z3,
					  coords[0], coords[1], coords[2]);
		}
		break;
	    default:
		break;
	    }
	}
	switch (cnt) {
	case 0:
	    return true;
	case 1:
	    if (planarOverlapsTriangle(t1, t2, planar)) {
		// System.out.println("returning false 8");
		return false;
	    } else {
		return true;
	    }
	default:
	    if (planarOverlapsTriangle(t1, t2, planar)) {
		// System.out.println("returning false 9");
		return false;
	    }
	    // System.out.println("planarLineTest");
	    return planarLineTest(t1, t2, planar);
	}
	*/
    }

    boolean areDisjoint1(Model3D.Triangle t1,
			 Model3D.Triangle t2)
    {
	/*
	System.out.format("t1: (%s, %s, %s)--(%s, %s, %s)--(%s, %s, %s)\n",
			  t1.getX1(), t1.getY1(), t1.getZ1(),
			  t1.getX2(), t1.getY2(), t1.getZ2(),
			  t1.getX3(), t1.getY3(), t1.getZ3());
	System.out.format("t2: (%s, %s, %s)--(%s, %s, %s)--(%s, %s, %s)\n",
			  t2.getX1(), t2.getY1(), t2.getZ1(),
			  t2.getX2(), t2.getY2(), t2.getZ2(),
			  t2.getX3(), t2.getY3(), t2.getZ3());
	*/

	// Does half of the tests. It looks at the intersection of
	// t2's edges with t1's plane, taking just the end points of
	// the edges if those edges lie in the plane. If any of those
	// points falls within the triangle t1, this method returns
	// false. This will miss cases such as t1 being fully enclosed
	// by t2, but that case will be caught by calling
	// this method with the arguments reversed.
	double t1x1 = t1.getX1();
	double t1y1 = t1.getY1();
	double t1z1 = t1.getZ1();
	double t1x2 = t1.getX2();
	double t1y2 = t1.getY2();
	double t1z2 = t1.getZ2();
	double t1x3 = t1.getX3();
	double t1y3 = t1.getY3();
	double t1z3 = t1.getZ3();
	double[] v21 = {t1x2-t1x1, t1y2-t1y1, t1z2-t1z1};
	double[] v31 = {t1x3-t1x1, t1y3-t1y1, t1z3-t1z1};
	double[] n1;
	try {
	    n1 = VectorOps.normalize(VectorOps.crossProduct(v21, v31));
	} catch (IllegalArgumentException e) {
	    // the cross product was zero, which can only happen if
	    // the triangle is colinear.  Returning false in this case
	    // will result in a non-null triangle list containing the
	    // bad triangle.
	    // System.out.println("could not normalize");
	    return false;
	}
	for (int i = 0; i < 3; i++) {
	    // in case of round off errors or negative zeros
	    if (Math.abs(n1[i]) < 1.0e-13) n1[i] = 0.0;
	    if (Math.abs(n1[i]) > 1.0) n1[i] = 1.0;
	    if (Math.abs(n1[i]) < -1.0) n1[i] = -1.0;
	}
	int i = 0;
	double max = 0.0;
	for (int j = 0; j < 3; j++) {
	    double a = Math.abs(n1[j]);
	    if (a > max) {
		i = j;
		max = a;
	    }
	}
	// double[] xdir = null;
	double[] ydir = null;
	switch (i) {
	case 0:
	    // xdir = new double[] {0.0, 1.0, 0.0};
	    ydir = new double[] {0.0, 0.0, 1.0};
	    break;
	case 1:
	    // xdir = new double[] {0.0, 0.0, 1.0};
	    ydir = new double[] {1.0, 0.0, 0.0};
	    break;
	case 2:
	    // xdir = new double[] {1.0, 0.0, 0.0};
	    ydir = new double[] {0.0, 1.0, 0.0};
	    break;
	}
	double[] xaxis = VectorOps.normalize(VectorOps.crossProduct(ydir, n1));
	double[] yaxis = VectorOps.normalize(VectorOps.crossProduct(n1, xaxis));
	// Coordinate system puts the origin at (t1x1, t1y1, t1z1)
	// with n1 opointing in the new Z axis direction. Similsarly,
	// xaxis and yaxis point in the new X and Y directions respectively.

	// new t1 coordinates
	double x11 = 0.0, y11 = 0.0, z11 = 0.0;
	double x21 = VectorOps.dotProduct(xaxis, v21);
	double y21 = VectorOps.dotProduct(yaxis, v21);
	double z21 = 0.0;
	double x31 = VectorOps.dotProduct(xaxis, v31);
	double y31 = VectorOps.dotProduct(yaxis, v31);
	double z31 = 0.0;
	/*
	System.out.format("new t1: (0,0,0)--(%s, %s, 0)--(%s, %s, 0)\n",
			  x21, y21, x31, y31);
	*/
	// new t2 coordinates
	double v12[] = {t2.getX1()-t1x1, t2.getY1()-t1y1, t2.getZ1()-t1z1};
	double v22[] = {t2.getX2()-t1x1, t2.getY2()-t1y1, t2.getZ2()-t1z1};
	double v32[] = {t2.getX3()-t1x1, t2.getY3()-t1y1, t2.getZ3()-t1z1};
	double x12 = VectorOps.dotProduct(xaxis, v12);
	double y12 = VectorOps.dotProduct(yaxis, v12);
	double z12 = VectorOps.dotProduct(n1, v12);
	double x22 = VectorOps.dotProduct(xaxis, v22);
	double y22 = VectorOps.dotProduct(yaxis, v22);
	double z22 = VectorOps.dotProduct(n1, v22);
	double x32 = VectorOps.dotProduct(xaxis, v32);
	double y32 = VectorOps.dotProduct(yaxis, v32);
	double z32 = VectorOps.dotProduct(n1, v32);
	/*
	System.out.format("new t2: (%s, %s, %s)--(%s, %s, %s)--(%s, %s, %s)\n",
			  x12, y12, z12,
			  x22, y22, z22,
			  x32, y32, z32);
	*/
	if (z12 > 0.0 && z22 > 0.0 && z32 > 0.0) return true;
	if (z12 < 0.0 && z22 < 0.0 && z32 < 0.0) return true;
	boolean vm1 = vertexMatch(t2.getX1(), t2.getY1(), t2.getZ1(), t1);
	boolean vm2 = vertexMatch(t2.getX2(), t2.getY2(), t2.getZ2(), t1);
	boolean vm3 = vertexMatch(t2.getX3(), t2.getY3(), t2.getZ3(), t1);
	// System.out.println("vm1 = " + vm1 +", vm2 = " + vm2 + ", vm3 = " +vm3);

	if (vm1 && vm2 && vm3) return false;
	if (vm1 && vm2) return true;
	if (vm1 && vm3) return true;
	if (vm2 && vm3) return true;

	// barycentric to cartesian
	double matrix[][] = {{x11, x21, x31},
			 {y11, y21, y31},
			 {1.0, 1.0, 1.0}};
	LUDecomp lud = new LUDecomp(matrix);
	if (lud.isNonsingular() == false) {
	    // System.out.println("singular");
	    return false;
	}
	double[] B = new double[3];
	// B[2] = 1.0  set each time by the method 'inside'
	boolean testZ12 = false;
	boolean testZ22 = false;
	boolean testZ32 = false;
	if (z12 == 0.0 && z22 == 0.0 && z32 == 0.0) {
	    // System.out.println("case 1: the triangles are in the same plane");
	    double[][] t1Segments = {{x11, y11, x21, y21},
				     {x11, y11, x31, y31},
				     {x21, y21, x31, y31}};

	    double[][] t2Segments = {{x12, y12, x22, y22},
				     {x12, y12, x32, y32},
				     {x22, y22, x32, y32}};

	    for (double[] t1seg: t1Segments) {
		for (double[] t2seg: t2Segments) {
		    if (Path2DInfo.lineSegmentsIntersect(t1seg[0], t1seg[1],
							 t1seg[2], t1seg[3],
							 t2seg[0], t2seg[1],
							 t2seg[2], t2seg[3])) {
			if (t2seg[0] == t1seg[0] && t2seg[1] == t1seg[1]) {
			    continue;
			} else if (t2seg[0] == t1seg[2]
				   && t2seg[1] == t1seg[3]) {
			    continue;
			} else if (t2seg[2] == t1seg[0]
				   && t2seg[3] == t1seg[1]) {
			    continue;
			} else if (t2seg[2] == t1seg[2]
				   && t2seg[3] == t1seg[3]) {
			    continue;
			}
			return false;
		    }
		}
	    }
	    // if we did not find an intersection, the vertices have to
	    // be checked in case t2 is totally within t1.
	    testZ12 = true;
	    testZ22 = true;
	    testZ32 = true;
	} else if (z12 == 0.0 && z22 == 0.0) {
	    // System.out.println("case 2");
	    testZ12 = true;
	    testZ22 = true;
	} else if (z22 == 0.0 && z32 == 0.0) {
	    // System.out.println("case 3");
	    testZ22 = true;
	    testZ32 = true;
	} else if (z12 == 0.0) {
	    // System.out.println("case 4");
	    testZ12 = true;
	} else if (z22 == 0.0) {
	    // System.out.println("case 5");
	    testZ22 = true;
	} else if (z32 == 0.0) {
	    // System.out.println("case 6");
	    testZ32 = true;
	}
	if (testZ12) {
	    B[0] = x12;
	    B[1] = y12;
	    // System.out.println("got here 1");
	    if (inside(lud, B)) return false;
	}
	if (testZ22) {
	    B[0] = x22;
	    B[1] = y22;
	    // System.out.println("got here 2");
	    if (inside(lud, B)) return false;
	}
	if (testZ32) {
	    B[0] = x32;
	    B[1] = y32;
	    // System.out.println("got here 3");
	    if (inside(lud, B)) return false;
	}
	if (z12 != 0.0 && z22 != 0.0 && Math.signum(z12) != Math.signum(z22)) {
	    double u = z22 / (z22-z12);
	    double onemu = 1.0 - u;
	    B[0] = x12*u + x22*onemu;
	    B[1] = y12*u + y22*onemu;
	    // System.out.println("got here 4");
	    if (inside(lud, B)) return false;
	}
	if (z12 != 0.0 && z32 != 0.0 && Math.signum(z12) != Math.signum(z32)) {
	    double u = z32/(z32-z12);
	    double onemu = 1.0 - u;
	    B[0] = x12*u + x32*onemu;
	    B[1] = y12*u + y32*onemu;
	    // System.out.println("got here 5");
	    if (inside(lud, B)) return false;
	}
	if (z22 != 0.0 && z32 != 0.0 && Math.signum(z22) != Math.signum(z32)) {
	    double u = z32/(z32-z22);
	    double onemu = 1.0 - u;
	    B[0]= x22*u + x32*onemu;
	    B[1]  = y22*u + y32*onemu;
	    // System.out.println("got here 6");
	    if (inside(lud, B)) return false;
	}
	return true;
    }

    boolean inside(LUDecomp lud, double[] B) {
	// B contains the cartesian coordinates, with an extra row containing
	// 1.0.  Solving the system of equations provides a vector
	// containing 3 barycentric coordinates and if any of these are
	// outside the range [0,1], the point is outside the triangle.
	// The vertices of the triangle are points at which one of the
	// barycentric coordinates has the value 1.0 and the rest are 0.0.
	/*
	System.out.println("B[0] = " + B[0]);
	System.out.println("B[1] = " + B[1]);
	*/
	B[2] = 1.0;
	for (double lambda: lud.solve(B)) {
	    if (Math.abs(lambda) < limit) lambda = 0.0;
	    // System.out.print(" " + lambda);
	    if (lambda <= 0.0 || lambda >= 1.0) {
		// System.out.println();
		return false;
	    }
	}
	// System.out.println();
	return true;
    }

    /*
    boolean isTriangleAreaZero(double x1, double y1,
				      double x2, double y2,
				      double x3, double y3)
    {
	double ux = x2 - x1;
	double uy = y2 - y1;
	double vx = x3 - x1;
	double vy = y3 - y1;

	double cpz = (ux*vy - vx*uy);

	return (Math.abs(cpz) <= limit);
    }
    */

    /*
    static double getTriangleArea2(double x1, double y1, double z1,
				   double x2, double y2, double z2,
				   double x3, double y3, double z3) 
    {
	// compute twice the triangle area.
	double ux = x2 - x1;
	double uy = y2 - y1;
	double uz = z2 - z1;
	double vx = x3 - x1;
	double vy = y3 - y1;
	double vz = z3 - z1;

	double cpx = uy*vz - vy*uz;
	double cpy = (vx*uz - ux*vz);
	double cpz = (ux*vy - vx*uy);

	return Math.sqrt(cpx*cpx + cpy*cpy + cpz*cpz);
    }
    */
    double limit = /*1.e-10;*/ (double)Math.ulp(1F);

    void setLimit(double limit) {
	if (limit < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("negativeLimit", limit));
	this.limit = limit;
    }

    private boolean vertexMatch(double x, double y, double z,
				Model3D.Triangle t1)
    {
	return vertexMatch(x, y, z,
			   t1.getX1(), t1.getY1(), t1.getZ1(),
			   t1.getX2(), t1.getY2(), t1.getZ2(),
			   t1.getX3(), t1.getY3(), t1.getZ3());
    }

    private boolean vertexMatch(double x, double y, double z,
				       double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3) 
    {
	// triangle vertices match exactly.
	boolean result = (x == x1 && y == y1 && z == z1)
	    || (x == x2 && y == y2 && z == z2)
	    || (x == x3 && y == y3 && z == z3);
	/*
	boolean result = (Math.abs(x-x1) < limit) &&
	    (Math.abs(y-y1) < limit) && (Math.abs(z - z1) < limit);
	result = result || ((Math.abs(x-x2) < limit) &&
			    (Math.abs(y-y2) < limit) && 
			    (Math.abs(z - z2) < limit));
	result = result || ((Math.abs(x-x3) < limit) &&
			    (Math.abs(y-y3) < limit) && 
			    (Math.abs(z - z3) < limit));
	*/
	return result;
    }

    /*
    private boolean edgeIntersect(Model3D.Triangle triangle,
				  double x1, double y1, double z1,
				  double x2, double y2, double z2)
    {
	double tx1 = triangle.x2 - triangle.x1;
	double ty1 = triangle.y2 - triangle.y1;
	double tz1 = triangle.z2 - triangle.z1;
	double norm = Math.sqrt(tx1*tx1 + ty1*ty1 + tz1 * tz1);
	double nx1 = tx1 / norm;
	double ny1 = ty1 / norm;
	double nz1 = tz1 / norm;

	if ((x1 == triangle.x1 && y1 == triangle.y1 && z1 == triangle.z1)
	    || (x1 == triangle.x2 && y1 == triangle.y2 && z1 == triangle.z2)
	    || (x1 == triangle.x3 && y1 == triangle.y3 && z1 == triangle.z3)
	    || (x2 == triangle.x1 && y2 == triangle.y1 && z2 == triangle.z1)
	    || (x2 == triangle.x2 && y2 == triangle.y2 && z2 == triangle.z2)
	    || (x2 == triangle.x3 && y2 == triangle.y3 && z2 == triangle.z3)) {
	    double vx = x1-x2;
	    double vy = y1-y2;
	    double vz = z1-z2;
	    double vn = Math.sqrt(vx*vx + vy*vy + vz*vz);
	    vx /= vn;
	    vy /= vn;
	    vz /= vn;
	    double dp = nx1*vx + ny1*vy + nz1*vz;
	    if (Math.abs(dp) > limit) {
		// test for a vector out of the plane of the triangle
		// meeting one of the three vertices
		return false;
	    }
	}

	double nx2 = triangle.ny*nz1 - ny1*triangle.nz;
	double ny2 = nx1*triangle.nz - triangle.nx*nz1;
	double nz2 = triangle.nx*ny1 - nx1*triangle.ny;

	double lxx1 = nx1 * (x1 - triangle.x1)
	    + ny1 * (y1 - triangle.y1) + nz1 * (z1 - triangle.z1);
	double lyy1 = nx2 * (x1 - triangle.x1)
	    + ny2 * (y1 - triangle.y1) + nz2 * (z1 - triangle.z1);
	double lzz1 = triangle.nx * (x1 - triangle.x1)
	    + triangle.ny * (y1 - triangle.y1)
	    + triangle.nz * (z1 - triangle.z1);


	double lxx2 = nx1 * (x2 - triangle.x1)
	    + ny1 * (y2 - triangle.y1) + nz1 * (z2 - triangle.z1);
	double lyy2 = nx2 * (x2 - triangle.x1)
	    + ny2 * (y2 - triangle.y1) + nz2 * (z2 - triangle.z1);
	double lzz2 = triangle.nx * (x2 - triangle.x1)
	    + triangle.ny * (y2 - triangle.y1)
	    + triangle.nz * (z2 - triangle.z1);

	// if (lzz1 > 0.0 && lzz2 > 0.0) return false;
	// if (lzz1 < 0.0 && lzz2 < 0.0) return false;
	if (lzz1 > limit && lzz2 > limit) return false;
	if (lzz1 < -limit && lzz2 < -limit) return false;

	double xx1 = nx1 * (triangle.x2 - triangle.x1)
	    + ny1 * (triangle.y2 - triangle.y1)
	    + nz1 * (triangle.z2 - triangle.z1);
	double yy1 = nx2 * (triangle.x2 - triangle.x1)
	    + ny2 * (triangle.y2 - triangle.y1)
	    + nz2 * (triangle.z2 - triangle.z1);

	double zz1 = triangle.nx * (triangle.x2 - triangle.x1)
	    + triangle.ny * (triangle.y2 - triangle.y1)
	    + triangle.nz * (triangle.z2 - triangle.z1);
	// if (Math.abs(zz1) > limit) System.out.println("bad zz1");

	double xx2 = nx1 * (triangle.x3 - triangle.x1)
	    + ny1 * (triangle.y3 - triangle.y1)
	    + nz1 * (triangle.z3 - triangle.z1);
	double yy2 = nx2 * (triangle.x3 - triangle.x1)
	    + ny2 * (triangle.y3 - triangle.y1)
	    + nz2 * (triangle.z3 - triangle.z1);
	double zz2 = triangle.nx * (triangle.x3 - triangle.x1)
	    + triangle.ny * (triangle.y3 - triangle.y1)
	    + triangle.nz * (triangle.z3 - triangle.z1);
	// if (Math.abs(zz2) > limit) System.out.println("bad zz2");

	if (lzz1 != 0.0 || lzz2 != 0.0 ) {
	    double xxx, yyy;
	    if (lzz1 == 0.0) {
		xxx = lxx1;
		yyy = lyy1;
	    } else if (lzz2 == 0.0) {
		xxx = lxx2;
		yyy = lyy2;
	    } else {
	        double t = lzz1/(lzz1 - lzz2);
		xxx = lxx1 + t * (lxx2 - lxx1);
		yyy = lyy1 + t * (lyy2 - lyy1);
	    }
	    if (Math.abs(xxx) <= limit && Math.abs(yyy) <= limit) return false;

	    if (Math.abs(xxx-xx1) <= limit
		&& Math.abs(yyy-yy1) <= limit) return false;

	    if (Math.abs(xxx-xx2) <= limit
		&& Math.abs(yyy-yy2) <= limit) return false;

	    if (isTriangleAreaZero(xxx, yyy, 0.0, 0.0, xx1, yy1)) {
		double dp = xxx*xx1 + yyy * yy1;
		return (Math.abs(dp) >= limit
			&& Math.abs((dp - xx1*xx1 - yy1*yy1)/dp) <= limit);
	    }

	    if (isTriangleAreaZero(xxx, yyy, 0.0, 0.0, xx2, yy2)) {
		double dp = xxx*xx2 + yyy * yy2;
		return (Math.abs(dp) >= limit
			&& Math.abs((dp - xx2*xx2 - yy2*yy2)/dp) <= limit);
	    }

	    if (isTriangleAreaZero(xxx, yyy, xx1, yy1, xx2, yy2)) {
		double dp = (xxx-xx1)*(xx2-xx1) + (yyy-yy1)*(yy2-yy1);
		return (Math.abs(dp) >= limit
			&& Math.abs((dp - (xx2-xx1)*(xx2-xx1)
				     - (yy2-yy1)*(yy2-yy1))/dp) <= limit);
	    }
	    return false;
	}

	// remaining case is a triangle and line that are both
	// in the same plane.

	if (Path2DInfo.lineSegmentsIntersect(0.0, 0.0, xx1, yy1,
					     lxx1, lyy1, lxx2, lyy2)) {
	    if ((Math.abs(lxx1) > limit || Math.abs(lyy1) > limit)
		&& (Math.abs(lxx1-xx1) > limit || Math.abs(lyy1-yy1) > limit)
		&&(Math.abs(lxx2) > limit || Math.abs(lyy2) > limit)
		&& (Math.abs(lxx2-xx1) > limit || Math.abs(lyy2-yy1) > limit))
		return true;
	}
	if (Path2DInfo.lineSegmentsIntersect(0.0, 0.0, xx2, yy2,
					     lxx1, lyy1, lxx2, lyy2)) {
	    if ((Math.abs(lxx1) > limit || Math.abs(lyy1) > limit)
		&& (Math.abs(lxx1-xx2) > limit || Math.abs(lyy1-yy2) > limit)
		&&(Math.abs(lxx2) > limit || Math.abs(lyy2) > limit)
		&& (Math.abs(lxx2-xx2) > limit || Math.abs(lyy2-yy2) > limit))
		return true;
	}
	if (Path2DInfo.lineSegmentsIntersect(xx1, yy1, xx2, yy2,
					     lxx1, lyy1, lxx2, lyy2)) {
	    if ((Math.abs(lxx1-xx1) > limit || Math.abs(lyy1-yy1) > limit)
		&& (Math.abs(lxx1-xx2) > limit || Math.abs(lyy1-yy2) > limit)
		&&(Math.abs(lxx2-xx1) > limit || Math.abs(lyy2-yy1) > limit)
		&& (Math.abs(lxx2-xx2) > limit || Math.abs(lyy2-yy2) > limit))
		return true;
	}
	return false;
    }

    private boolean planarLineTest(Model3D.Triangle t1,
				   Model3D.Triangle t2,
				   boolean[] planar)
    {
	if (planar[0] && planar[1]) {
	    if (edgeIntersect(t1, t2.x1, t2.y1, t2.z1, t2.x2, t2.y2, t2.z2)) {
		// System.out.println("returning false 10");
		return false;
	    }
	}
	if (planar[0] && planar[2]) {
	    if (edgeIntersect(t1, t2.x1, t2.y1, t2.z1, t2.x3, t2.y3, t2.z3)) {
		// System.out.println("returning false 11");
		return false;
	    }
	}
	if (planar[1] && planar[2]) {
	    if (edgeIntersect(t1, t2.x2, t2.y2, t2.z2, t2.x3, t2.y3, t2.z3)) {
		// System.out.println("returning false 12");
		return false;
	    }
	}
	return true;
    }


    private boolean insideTriangle(Model3D.Triangle t1,
				   double x, double y, double z)
    {
	double dot12 = (t1.x1 - x)*(t1.x2 - x)
	    + (t1.y1 - y)*(t1.y2 - y) + (t1.z1 -z)*(t1.z2 - z);
	double dot13 = (t1.x1 - x)*(t1.x3 - x)
	    + (t1.y1 - y)*(t1.y3 - y) + (t1.z1 -z)*(t1.z3 - z);
	double dot23 = (t1.x2 - x)*(t1.x3 - x)
	    + (t1.y2 - y)*(t1.y3 - y) + (t1.z2 -z)*(t1.z3 - z);

	if (Math.abs(dot12) < limit) dot12 = 0;
	if (Math.abs(dot13) < limit) dot13 = 0;
	if (Math.abs(dot23) < limit) dot23 = 0;

	double s12 = Math.signum(dot12);
	double s13 = Math.signum(dot13);
	double s23 = Math.signum(dot23);

	// triangles may touch at vertices.
	if (s12 == 0.0 || s13 == 0.0 || s23 == 0.0) return false;

	// if inside the triangle, including on an edge but not on
	// a vertex, then all the dot products will be non-zero and
	// at least two will have opposite signs.
	if (s12 != s13 || s12 != s23 || s13 != s23) {
	    // check that sum of angles is 360 degrees - true
	    // when the point is inside the triangle
	    double array1[] = new double[3];
	    double array2[] = new double[3];
	    double array3[] = new double[3];
	    array1[0] = (t1.x1 - x);
	    array1[1] = (t1.y1 - y);
	    array1[2] = (t1.z1 - z);
	    array2[0] = (t1.x2 - x);
	    array2[1] = (t1.y2 - y);
	    array2[2] = (t1.z2 - z);
	    array3[0] = (t1.x3 - x);
	    array3[1] = (t1.y3 - y);
	    array3[2] = (t1.z3 - z);
	    double n1 = VectorOps.norm(array1);
	    double n2 = VectorOps.norm(array2);
	    double n3 = VectorOps.norm(array3);
	    double dn12 = dot12/(n1*n2);
	    double dn23 = dot23/(n2*n3);
	    double dn31 = dot13/(n1*n3);
	    // compensate for floating point errors:
	    if (dn12 < -1.0) dn12 = -1.0;
	    if (dn12 > 1.0) dn12 = 1.0;
	    if (dn23 < -1.0) dn23 = -1.0;
	    if (dn23 > 1.0) dn23 = 1.0;
	    if (dn31 < -1.0) dn31 = -1.0;
	    if (dn31 > 1.0) dn31 = 1.0;

	    double theta12 = Math.acos(dn12);
	    double theta23 = Math.acos(dn23);
	    double theta31 = Math.acos(dn31);
	    if (Math.abs(theta12 + theta23 + theta31 - 2.0*Math.PI) > limit) {
		return false;
	    }
	    return true;
	}
	return false;
    }

    // case where a line segment is in the plane of t1 and at least
    // one point is inside the triangle, or on an edge but not at
    // a vertex.

    private boolean overlapsTriangle(Model3D.Triangle t1,
				   double x1, double y1, double z1,
				     double x2, double y2, double z2,
				     boolean valid)
    {
	if (valid) {
	    return (insideTriangle(t1, x1, y1, z1)
		    || insideTriangle(t1, x2, y2, z2));
	} else {
	    return insideTriangle(t1, x1, y1, z1);
	}
    }

    private boolean overlapsTriangle(Model3D.Triangle t1,
				   double x1, double y1, double z1,
				   double x2, double y2, double z2)
    {
	return insideTriangle(t1, x1, y1, z1)
	    || insideTriangle(t1, x2, y2, z2);
    }

    private boolean overlapsTriangle(Model3D.Triangle t1, Model3D.Triangle t2) {
	return insideTriangle(t1, t2.x1, t2.y1, t2.z1)
	    || insideTriangle(t1, t2.x2, t2.y2, t2.z2)
	    || insideTriangle(t1, t2.x3, t2.y3, t2.z3);
    }

    private boolean planarOverlapsTriangle(Model3D.Triangle t1,
					   Model3D.Triangle t2,
					   boolean[] planar)
    {
	boolean result = false;
	

	if (planar[0]) {
	    result = result || insideTriangle(t1, t2.x1, t2.y1, t2.z1);
	}
	if (planar[1]) {
	    result = result || insideTriangle(t1, t2.x2, t2.y2, t2.z2);
	}
	if (planar[2]) {
	    result = result || insideTriangle(t1, t2.x3, t2.y3, t2.z3);
	}
	return result;
    }

    private double vnorm(Model3D.Triangle t1, Model3D.Triangle t2,
			 int i, int j)
    {
	double tmp;
	double sum = 0.0;
	switch (i) {
	case 0:
	    switch (j) {
	    case 0:
		tmp = t2.x1 - t1.x1;
		sum += tmp*tmp;
		tmp = t2.y1 - t1.y1;
		sum += tmp*tmp;
		tmp = t2.z1 - t1.z1;
		sum += tmp*tmp;
		break;
	    case 1:
		tmp = t2.x1 - t1.x2;
		sum += tmp*tmp;
		tmp = t2.y1 - t1.y2;
		sum += tmp*tmp;
		tmp = t2.z1 - t1.z2;
		sum += tmp*tmp;
		break;
	    case 2:
		tmp = t2.x1 - t1.x3;
		sum += tmp*tmp;
		tmp = t2.y1 - t1.y3;
		sum += tmp*tmp;
		tmp = t2.z1 - t1.z3;
		sum += tmp*tmp;
		break;
	    }
	    break;
	case 1:
	    switch (j) {
	    case 0:
		tmp = t2.x2 - t1.x1;
		sum += tmp*tmp;
		tmp = t2.y2 - t1.y1;
		sum += tmp*tmp;
		tmp = t2.z2 - t1.z1;
		sum += tmp*tmp;
		break;
	    case 1:
		tmp = t2.x2 - t1.x2;
		sum += tmp*tmp;
		tmp = t2.y2 - t1.y2;
		sum += tmp*tmp;
		tmp = t2.z2 - t1.z2;
		sum += tmp*tmp;
		break;
	    case 2:
		tmp = t2.x2 - t1.x3;
		sum += tmp*tmp;
		tmp = t2.y2 - t1.y3;
		sum += tmp*tmp;
		tmp = t2.z2 - t1.z3;
		sum += tmp*tmp;
		break;
	    }
	    break;
	case 2:
	    switch (j) {
	    case 0:
		tmp = t2.x3 - t1.x1;
		sum += tmp*tmp;
		tmp = t2.y3 - t1.y1;
		sum += tmp*tmp;
		tmp = t2.z3 - t1.z1;
		sum += tmp*tmp;
		break;
	    case 1:
		tmp = t2.x3 - t1.x2;
		sum += tmp*tmp;
		tmp = t2.y3 - t1.y2;
		sum += tmp*tmp;
		tmp = t2.z3 - t1.z2;
		sum += tmp*tmp;
		break;
	    case 2:
		tmp = t2.x3 - t1.x3;
		sum += tmp*tmp;
		tmp = t2.y3 - t1.y3;
		sum += tmp*tmp;
		tmp = t2.z3 - t1.z3;
		sum += tmp*tmp;
		break;
	    }
	    break;
	}
	if (sum == 0.0) return 1.0; // to avoid div by zero (shouldn't happen)
	return Math.sqrt(sum);
    }

    // if returns false, we need to check further: it means an edge
    // of one triangle will intersect the plane of the other triangle
    // at some point.
    private boolean areDisjoint(Model3D.Triangle t1,
				       Model3D.Triangle t2,
				       double dp[][], boolean[] planar)
    {
	java.util.Arrays.fill(planar, false);

	dp[0][0] = (t2.x1 - t1.x1) * t1.nx + (t2.y1 - t1.y1) * t1.ny
	    + (t2.z1 - t1.z1) * t1.nz;
	if (Math.abs(dp[0][0]/vnorm(t1,t2,0,0)) < limit) dp[0][0] = 0.0;
	dp[1][0] = (t2.x2 - t1.x1) * t1.nx + (t2.y2 - t1.y1) * t1.ny
	    + (t2.z2 - t1.z1) * t1.nz;
	if (Math.abs(dp[1][0]/vnorm(t1,t2,1,0)) < limit) dp[1][0] = 0.0;
	dp[2][0] = (t2.x3 - t1.x1) * t1.nx + (t2.y3 - t1.y1) * t1.ny
	    + (t2.z3 - t1.z1) * t1.nz;
	if (Math.abs(dp[2][0]/vnorm(t1,t2,2,0)) < limit) dp[2][0] = 0.0;
	dp[0][1] = (t2.x1 - t1.x2) * t1.nx + (t2.y1 - t1.y2) * t1.ny
	    + (t2.z1 - t1.z1) * t1.nz;
	if (Math.abs(dp[0][1]/vnorm(t1,t2,0,1)) < limit) dp[0][1] = 0.0;
	dp[1][1] = (t2.x2 - t1.x2) * t1.nx + (t2.y2 - t1.y2) * t1.ny
	    + (t2.z2 - t1.z2) * t1.nz;
	if (Math.abs(dp[1][1]/vnorm(t1,t2,1,1)) < limit) dp[1][1] = 0.0;
	dp[2][1] = (t2.x3 - t1.x2) * t1.nx + (t2.y3 - t1.y2) * t1.ny
	    + (t2.z3 - t1.z2) * t1.nz;
	if (Math.abs(dp[2][1]/vnorm(t1,t2,2,1)) < limit) dp[2][1] = 0.0;
	dp[0][2] = (t2.x1 - t1.x3) * t1.nx + (t2.y1 - t1.y3) * t1.ny
	    + (t2.z1 - t1.z3) * t1.nz;
	if (Math.abs(dp[0][2]/vnorm(t1,t2,0,2)) < limit) dp[0][2] = 0.0;
	dp[1][2] = (t2.x2 - t1.x3) * t1.nx + (t2.y2 - t1.y3) * t1.ny
	    + (t2.z2 - t1.z3) * t1.nz;
	if (Math.abs(dp[1][2]/vnorm(t1,t2,1,2)) < limit) dp[1][2] = 0.0;
	dp[2][2] = (t2.x3 - t1.x3) * t1.nx + (t2.y3 - t1.y3) * t1.ny
	    + (t2.z3 - t1.z3) * t1.nz;
	if (Math.abs(dp[2][2]/vnorm(t1,t2,2,2)) < limit) dp[2][2] = 0.0;

	boolean result = true;
	for (int i = 0; i < 3; i++) {
	    boolean pm = false;
	    double sn = 0.0;
	    int ii = 0; int jj = 0;
	    for (int j = 0; j < 3; j++) {
		double x = Math.signum(dp[j][i]);
		if (x != 0.0) {
		    if (pm == false) {
			sn = x;
			ii = i;
			jj = j;
			pm = true;
		    } else {
			if (x != sn) {
			    result = false;
			}
		    }
		} else {
		    planar[j] = true;
		}
	    }
	}
	return result;
    }

    private boolean getIntersection(double[] coords, Model3D.Triangle t1,
				    double x1, double y1, double z1,
				    double x2, double y2, double z2)
    {
	// intersect the line from (x1, y1, z1) to (x2, y2, z2) with
	// the plane that contains triangle t1.  The return value is
	// true if the line segment intersects
	// the plane of the triangle t1 and neither (x1, y1, z1) nor
	// (x2, y2, z2) is in the plane of the triangle.  coords will
	// be set to the coordinates of the intersection point.

	double vx = x2 - x1;
	double vy = y2 - y1;
	double vz = z2 - z1;
	double vdot = vx*t1.nx + vy*t1.ny + vz*t1.nz;
	if (Math.abs(vdot) < limit) return false;

	double dot1 = (x1 - t1.x1)*t1.nx
	    + (y1 - t1.y1)*t1.ny
	    + (z1 - t1.z1)*t1.nz;
	double dot2 = (x2 - t1.x1)*t1.nx
	    + (y2 - t1.y1)*t1.ny
	    + (z2 - t1.z1)*t1.nz;
	// f such that (x1, y1, z1) + f*(xvx, vy, vz) is in the plane of
	// the triangle:
	double f = dot1 / (dot1 - dot2);
	coords[0] = x1 + f * vx;
	coords[1] = y1 + f * vy;
	coords[2] = z1 + f * vz;

	return (f >= limit && f <= 1.0-limit);
    }

    private boolean oppositeTriangles(Model3D.Triangle t1,
				      Model3D.Triangle t2)
    {
	if (vertexMatch(t2.x1, t2.y1, t2.z1,
			t1.x1, t1.y1, t1.z1,
			t1.x2, t1.y2, t1.z2,
			t1.x3, t1.y3, t1.z3)
	    && vertexMatch(t2.x2, t2.y2, t2.z2,
			   t1.x1, t1.y1, t1.z1,
			   t1.x2, t1.y2, t1.z2,
			   t1.x3, t1.y3, t1.z3)
	    && vertexMatch(t2.x3, t2.y3, t2.z3,
			   t1.x1, t1.y1, t1.z1,
			   t1.x2, t1.y2, t1.z2,
			   t1.x3, t1.y3, t1.z3)) {
	    return true;
	}
	return false;

    }
    */
}

//  LocalWords:  exbundle negativeLimit edgeIntesect
//  LocalWords:  getX getY getZ
