package org.bzdev.p3d;
import org.bzdev.util.CollectionScanner;

import java.util.*;

//@exbundle org.bzdev.p3d.lpack.P3d


class ManifoldComponents {

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
    }

    // Need to create a list of models, one for each component
    // of the manifold.
    private Model3D[] models;
	
    /**
     * Get the number of components
     */
    int size() {
	return models.length;
    }

    Model3D getModel(int index) {
	try {
	    return models[index];
	} catch (IndexOutOfBoundsException e) {
	    throw new IllegalArgumentException("index", e);
	}
    }

    static class EdgeComparator	implements Comparator<Model3D.Edge> {
	public int compare(Model3D.Edge e1, Model3D.Edge e2) {
	    if (e1.x1 < e2.x1) return -1;
	    if (e1.x1 > e2.x1) return 1;
	    if (e1.y1 < e2.y1) return -1;
	    if (e1.y1 > e2.y1) return 1;
	    if (e1.z1 < e2.z1) return -1;
	    if (e1.z1 > e2.z1) return 1;
	    if (e1.x2 < e2.x2) return -1;
	    if (e1.x2 > e2.x2) return 1;
	    if (e1.y2 < e2.y2) return -1;
	    if (e1.y2 > e2.y2) return 1;
	    if (e1.z2 < e2.z2) return -1;
	    if (e1.z2 > e2.z2) return 1;
	    if (e1.reversed == e2.reversed) return 0;
	    if (e1.reversed) return 1;
	    else return -1;
	}
    }

    private final EdgeComparator edgeComparator = new EdgeComparator();
    
    // Remove the map entries that map edges to a triangle.
    private static final void
	removeTriangleFromMap(TreeMap<Model3D.Edge,Model3D.Triangle>map,
			      Model3D.Triangle triangle)
    {
	double[] tcoords = new double[48];
	for (Model3D.Edge e: triangle.getEdges(true, tcoords)) {
	    map.remove(e);
	}
	/*
	double x1 = triangle.getX1();
	double y1 = triangle.getY1();
	double z1 = triangle.getZ1();
	double x2 = triangle.getX2();
	double y2 = triangle.getY2();
	double z2 = triangle.getZ2();
	double x3 = triangle.getX3();
	double y3 = triangle.getY3();
	double z3 = triangle.getZ3();
	Model3D.Edge e1 = new Model3D.Edge(x1,y1,z1,x2,y2,z2, null);
	Model3D.Edge e2 = new Model3D.Edge(x2,y2,z2,x3,y3,z3, null);
	Model3D.Edge e3 = new Model3D.Edge(x3,y3,z3,x1,y1,z1, null);
	int cnt = 0;
	if (map.remove(e1) != null) cnt++;;
	if (map.remove(e2) != null) cnt++;;
	if (map.remove(e3) != null) cnt++;;
	*/
    }

    ManifoldComponents (Model3D model) throws ManifoldException {
	this(model, true);
    }

    ManifoldComponents (Model3D model, boolean strict)
	throws ManifoldException
    {
	this(model, strict, false);
    }

    ManifoldComponents (Model3D model, boolean strict, boolean tessellate)
	throws ManifoldException
    {
	double[] tcoords = new double[48];
	ArrayList<Model3D> modelList = new ArrayList<>();
	int sz = Math.round(model.size() * 3.0F);
	TreeMap<Model3D.Edge,Model3D.Triangle> emap =
	    new TreeMap<>(edgeComparator);
	CollectionScanner<Model3D.Triangle> cs;
	if (tessellate) {
	    final Model3D ourModel = model;
	    cs = new CollectionScanner<Model3D.Triangle>();
	    cs.add(new AbstractCollection<Model3D.Triangle>(){
		    public Iterator<Model3D.Triangle> iterator() {
			return ourModel.tessellate();
		    }
		    public int size() {
			return -1; // not used
		    }
		});
	} else {
	    cs = model.trianglesAndPatches();
	}
	int tcnt = 0;
	int tcnt3 = 0;
	for (Model3D.Triangle triangle: cs) {
	    tcnt++;
	    int i = 0;
	    for (Model3D.Edge e: triangle.getEdges(true, tcoords)) {
		i++;
		if (strict && emap.containsKey(e)) {
		    String msg = "<wrong edge number>";
		    ArrayList<Model3D.Triangle> errorList = new ArrayList<>(2);
		    errorList.add(triangle);
		    errorList.add(emap.get(e));
			StringBuilder sb = new StringBuilder();
		    try {
			P3d.printTriangleErrors(sb, errorList);
		    } catch (java.io.IOException eio) {}
		    switch (i) {
		    case 1:
			msg = (errorMsg("e1", sb.toString()));
			break;
		    case 2:
			msg = (errorMsg("e2", sb.toString()));
			break;
		    case 3:
			msg = (errorMsg("e3", sb.toString()));
			break;
		    }
		    ManifoldException em = new ManifoldException(msg, true);
		    em.eTriangle1 = emap.get(e);
		    em.eTriangle2 = triangle;
		    em.failedEdge = i-1;
		    throw em;
		} else {
		    tcnt3++;
		    Model3D.Triangle nt = emap.put(e, triangle);
		    triangle.prev = nt;
		}
	    }
	}
	cs = null;
	Map.Entry<Model3D.Edge,Model3D.Triangle>
	    mapEntry = emap.firstEntry();
	while (mapEntry != null) {
	    model = new Model3D(strict);
	    Model3D.Triangle triangle = mapEntry.getValue();

	    LinkedList<Model3D.Triangle> list = new LinkedList<>();
	    list.add(triangle);
	    if (!strict) {
		Model3D.Triangle t = triangle.prev;
		while (t != null) {
		    list.add(t);
		    Model3D.Triangle current = t;
		    t = t.prev;
		    current.prev = null;
		}
	    }
	    removeTriangleFromMap(emap, triangle);
	    while(!list.isEmpty()) {
		triangle = list.poll();
		// addTriangle OK for cubic patches and triangles:
		// the case where an entryNumber field is set is
		// treated as a special case.
		model.addTriangle(triangle);
		for (Model3D.Edge e: triangle.getEdges(false, tcoords)) {
		    Model3D.Triangle t = emap.get(e);
		    if (t != null) {
			if (t == triangle) {
			 double x1 = triangle.getX1();
			 double y1 = triangle.getY1();
			 double z1 = triangle.getZ1();
			 double x2 = triangle.getX2();
			 double y2 = triangle.getY2();
			 double z2 = triangle.getZ2();
			 double x3 = triangle.getX3();
			 double y3 = triangle.getY3();
			 double z3 = triangle.getZ3();
			 double x4 = triangle.getX4();
			 double y4 = triangle.getY4();
			 double z4 = triangle.getZ4();
			 String msg;
			 if (triangle.isPatch()) {
			     msg =
			(errorMsg("patch",x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4));
			 } else {
			     msg =
			(errorMsg("triangle",x1,y1,z1,x2,y2,z2,x3,y3,z3));
			 }
			 ManifoldException em =
			    new ManifoldException(msg, false);
			 em.eTriangle1 = triangle;
			 throw em;
			}
			list.add(t);
			removeTriangleFromMap(emap, t);
		    }
		}
	    }
	    modelList.add(model);
	    mapEntry = emap.firstEntry();
	}
	models = new Model3D[modelList.size()];
	modelList.toArray(models);
    }

    static class NestingEntry {
	int innerComp;
	int outerComp;
    }

    private boolean between(double x, double xl, double xu) {
	return (xl <= x && x <= xu);
    }

    private NestingEntry[] bbTestSetup() {
	ArrayList<NestingEntry> list = new ArrayList<>();
	for (int i = 0; i < models.length; i++) {
	    for (int j = 0; j < models.length; j++) {
		if (i == j) continue;
		// enumerate possible overlaps in each dimension
		boolean iojxmin = between(models[i].getMinX(),
					  models[j].getMinX(),
					  models[j].getMaxX());
		boolean joixmin = between(models[j].getMinX(),
					  models[i].getMinX(),
					  models[i].getMaxX());
		boolean iojymin = between(models[i].getMinY(),
					  models[j].getMinY(),
					  models[j].getMaxY());
		boolean joiymin = between(models[j].getMinY(),
					  models[i].getMinY(),
					  models[i].getMaxY());
		boolean iojzmin = between(models[i].getMinZ(),
					  models[j].getMinZ(),
					  models[j].getMaxZ());
		boolean joizmin = between(models[j].getMinZ(),
					  models[i].getMinZ(),
					  models[i].getMaxZ());
		boolean iojxmax = between(models[i].getMaxX(),
					  models[j].getMinX(),
					  models[j].getMaxX());
		boolean joixmax = between(models[j].getMaxX(),
					  models[i].getMinX(),
					  models[i].getMaxX());
		boolean iojymax = between(models[i].getMaxY(),
					  models[j].getMinY(),
					  models[j].getMaxY());
		boolean joiymax = between(models[j].getMaxY(),
					  models[i].getMinY(),
					  models[i].getMaxY());
		boolean iojzmax = between(models[i].getMaxZ(),
					  models[j].getMinZ(),
					  models[j].getMaxZ());
		boolean joizmax = between(models[j].getMaxZ(),
					  models[i].getMinZ(),
					  models[i].getMaxZ());

		boolean test = iojxmin && iojymin && iojzmin
		    && iojxmax && iojymax && iojzmax;
		if (test) {
		    test = !joixmin && !joiymin && !joizmin
			&& !joixmax && !joiymax && !joizmax;
		    if (test) {
			NestingEntry entry = new NestingEntry();
			entry.innerComp = i;
			entry.outerComp = j;
			list.add(entry);
		    }
		}
	    }
	}
	NestingEntry[] array = new NestingEntry[list.size()];
	list.toArray(array);
	return array;
    }

    static class TriangleSearch {
	Model3D.Triangle innerTriangle;
	Model3D innerModel;
	int innerVertex;
	double ix;
	double iy;
	double iz;
	Model3D.Triangle triangle = null;
	Model3D model = null;
	double sepsq = Double.POSITIVE_INFINITY;
	int vertex = -1;
	boolean dsign;
	public TriangleSearch(Model3D.Triangle t, int v, Model3D model) {
	    innerTriangle = t;
	    innerVertex = v;
	    innerModel = model;
	    switch (innerVertex) {
	    case 0:
		ix = t.getX1();
		iy = t.getY1();
		iz = t.getZ1();
		break;
	    case 1:
		ix = t.getX2();
		iy = t.getY2();
		iz = t.getZ2();
		break;
	    case 2:
		ix = t.getX3();
		iy = t.getY3();
		iz = t.getZ3();
		break;
	    default:
		throw new Error("case should not occurred: " + innerVertex);
	    }
	}
    }

    double limit = 1.e-10;

    private final boolean
	closestTo(TriangleSearch sdata, Model3D.Triangle t, Model3D model)
    {
	double x1 = t.getX1();
	double y1 = t.getY1();
	double z1 = t.getZ1();
	double x2 = t.getX2();
	double y2 = t.getY2();
	double z2 = t.getZ2();
	double x3 = t.getX3();
	double y3 = t.getY3();
	double z3 = t.getZ3();
	double tmp = x2-x1;
	double asq = tmp*tmp;
	// set asq, bsq, csq to the squares of the lengths of the
	// edges of t.
	tmp = y2-y1;
	asq += tmp*tmp;
	tmp = z2-z1;
	asq += tmp*tmp;
	tmp = x3-x2;
	double bsq = tmp*tmp;
	tmp = y3-y2;
	bsq += tmp*tmp;
	tmp = z3-y3;
	bsq += tmp*tmp;
	tmp = x1 - x3;
	double csq = tmp*tmp;
	tmp = y1 - y3;
	csq += tmp*tmp;
	tmp = z1 - z3;
	csq += tmp*tmp;

	double tmpx1 = sdata.ix - x1;
	double tmpy1 = sdata.iy - y1;
	double tmpz1 = sdata.iz - z1;
	double sep1sq = tmpx1*tmpx1 + tmpy1*tmpy1 + tmpz1*tmpz1;

	double tmpx2 = sdata.ix - x2;
	double tmpy2 = sdata.iy - y2;
	double tmpz2 = sdata.iz - z2;
	double sep2sq = tmpx2*tmpx2 + tmpy2*tmpy2 + tmpz2*tmpz2;

	double tmpx3 = sdata.ix - x3;
	double tmpy3 = sdata.iy - y3;
	double tmpz3 = sdata.iz - z3;
	double sep3sq = tmpx3*tmpx3 + tmpy3*tmpy3 + tmpz3*tmpz3;

	double sepn =  tmpx1 * t.getNormX()
	    + tmpy1 * t.getNormY() + tmpz1 * t.getNormZ();
	double sepnsq = sepn*sepn;
	double tmpnx = t.getNormX() * sepn;
	double tmpny = t.getNormY() * sepn;
	double tmpnz = t.getNormZ() * sepn;

	// (tnx, tny, tnz) is the point in the plane of the triangle t
	// from which a line perpendicular to t intersects the point
	// (sdata.ix, sdata.iy, sdata.iz).
	double tnx = sdata.ix - tmpnx;
	double tny = sdata.iy - tmpny;
	double tnz = sdata.iz - tmpnz;

	double tarea2 = getTriangleArea2(x1, y1, z1, x2, y2, z2, x3, y3, z3);
	double sarea2 = getTriangleArea2(tnx, tny, tnz, x1, y1, z1, x2, y2, z2)
	    + getTriangleArea2(tnx, tny, tnz, x2 ,y2 ,z2, x3, y3, z3)
	    + getTriangleArea2(tnx, tny, tnz, x3 ,y3 ,z3, x1, y1, z1);

	// Check if a line from the plane of t, perpendicular to t, and
	// ending at the sdata point (ix, iy, iz) originates at a point
	// within t's boundary.  If so, that provides the closest point.
	// Next test the vertices and edges.
	if (sarea2 <= tarea2 + limit) {
	    // Test allows for roundoff. if the areas are equal, the point
	    // (tnx, tny, ntz) is inside the triangle.
	    if (sepnsq < sdata.sepsq) {
		if (sepnsq == sep1sq) {
		    sdata.sepsq = sepnsq;
		    sdata.vertex = 0;
		    sdata.triangle = t;
		} else if (sepnsq == sep2sq) {
		    sdata.sepsq = sepnsq;
		    sdata.vertex = 1;
		    sdata.triangle = t;
		} else if (sepnsq == sep3sq) {
		    sdata.sepsq = sepnsq;
		    sdata.vertex = 2;
		    sdata.triangle = t;
		    sdata.dsign = sepn > 0.0;
		} else  {
		    sdata.sepsq = sepnsq;
		    sdata.vertex = 6;
		    sdata.triangle = t;
		}
		sdata.dsign = sepn > 0.0;
		sdata.model = model;
		return true;
	    }
	} else {
	    double ua = ((x2-x1)*tmpx1 + (y2-y1)*tmpy1 + (z2-z1)*tmpz1)/asq;
	    double ub = ((x3-x2)*tmpx2 + (y3-y2)*tmpy2 + (z3-z2)*tmpz2)/bsq;
	    double uc = ((x1-x3)*tmpx3 + (y1-y3)*tmpy3 + (z1-z3)*tmpz3)/csq;
	    if (ua < 0.0) ua = 0.0;
	    if (ua > 1.0) ua = 1.0;
	    if (ub < 0.0) ub = 0.0;
	    if (ub > 1.0) ub = 1.0;
	    if (uc < 0.0) uc = 0.0;
	    if (uc > 1.0) uc = 1.0;
	    // closest point edge a
	    tmp = tmpx1 * (1.0-ua) + tmpx2*ua;
	    double sepasq = tmp*tmp;
	    tmp = tmpy1 * (1.0-ua) + tmpy2*ua;
	    sepasq += tmp*tmp;
	    tmp = tmpz1 * (1.0-ua) + tmpz2*ua;
	    sepasq += tmp*tmp;
	    // closest point edge b
	    tmp = tmpx2 * (1.0-ub) + tmpx3*ub;
	    double sepbsq = tmp*tmp;
	    tmp = tmpy2 * (1.0-ub) + tmpy3*ub;
	    sepbsq += tmp*tmp;
	    tmp = tmpz2 * (1.0-ub) + tmpz3*ub;
	    sepbsq += tmp*tmp;
	    // closest point on edge c
	    tmp = tmpx3 * (1.0-uc) + tmpx1*uc;
	    double sepcsq = tmp*tmp;
	    tmp = tmpy3 * (1.0-uc) + tmpy1*uc;
	    sepcsq += tmp*tmp;
	    tmp = tmpz3 * (1.0-uc) + tmpz1*uc;
	    sepcsq += tmp*tmp;

	    double sepsq = sepasq;
	    int vertex = (ua == 0.0)? 0: ((ua==1.0? 1: 3));
	    if (sepsq > sepbsq) {
		sepsq = sepbsq;
		vertex = (ub == 0.0)? 1: ((ub==1.0? 2: 4));
	    }
	    if (sepsq > sepcsq) {
		sepsq = sepcsq;
		vertex = (uc == 0.0? 2: ((uc == 1.0)? 0: 5));
	    }
	    if (sepsq < sdata.sepsq) {
		sdata.sepsq = sepsq;
		sdata.vertex = vertex;
		sdata.triangle = t;
		sdata.dsign = sepn > 0.0;
		sdata.model = model;
		return true;
	    }
	}
	return false;
    }

    boolean verifyNesting(Appendable out) {
	NestingEntry[] entries = bbTestSetup();

	for (int innerComp = 0; innerComp < models.length; innerComp++) {
	    int count = 0;
	    Model3D innerModel = models[innerComp];
	    // find the right-most triangle.
	    Model3D.Triangle ti = null;
	    int v = -1;
	    double maxx = innerModel.getMaxX();
	    for (Model3D.Triangle t: innerModel.triangles()) {
		double x1 = t.getX1();
		double x2 = t.getX2();
		double x3 = t.getX3();
		double x;

		if (x1 > x2) {
		    x = x1;
		    v = 0;
		} else {
		    x = x2;
		    v = 1;
		}
		if (x < x3) {
		    x = x3;
		    v = 2;
		}
		if (x == maxx) {
		    ti = t;
		    break;
		}
	    }
	    TriangleSearch sdata = new TriangleSearch(ti, v, innerModel);
	    for (NestingEntry entry: entries) {
		if (entry.innerComp != innerComp) continue;
		Model3D outerModel = models[entry.outerComp];

		for (Model3D.Triangle t: outerModel.triangles()) {
		    if (closestTo(sdata, t, outerModel)) count++;
		}
	    }
	    // The search provides the closest triangle from an outer
	    // component whose bounding box contains the inner
	    // component's bounding box.  A component whose bounding
	    // box is not contained in another component's bounding
	    // box must have a positive volume.  If the outer
	    // component's volume is positive and its closest triangle
	    // points towards the inner component, the inner
	    // component's volume must be positive.  If the outer
	    // component's volume is positive and its closest triangle
	    // points away from the inner component, the inner component's
	    // volume must be negative. Note that no other component's
	    // triangle can be in the way because we chose the closest one,
	    // and components do not intersect each other.

	    boolean innerPositive = innerModel.volume() > 0.0;
	    if (count == 0) {
		// in this case, there is nothing that can possibly surround
		// the component, so it must have a positive volume
		if (innerModel.volume() >= 0.0) continue;
		try {
		    out.append(errorMsg("insideOut", innerComp));
		    // out.append("\n");
		} catch (java.io.IOException e){}
		return false;
	    }
	    if (sdata.dsign == innerPositive) {
		// The valid cases are:
		//    outer true, dsign false, inner false
		//    outer true, dsign true, inner true
		//    outer false dsign true, inner true
		//    outer false, dsign false, inner false
		// and these reduce to dsign == inner
		continue;
	    }
	    try {
		out.append(errorMsg("insideOut", innerComp));
		// out.append("\n");
	    } catch (java.io.IOException e) {}
	    return false;
	}
	return true;
    }

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

}
