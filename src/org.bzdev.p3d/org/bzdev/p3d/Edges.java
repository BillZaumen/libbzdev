package org.bzdev.p3d;

import org.bzdev.math.VectorOps;
import java.util.*;

class Edges {
    private java.util.List<Model3D.Edge> list = new LinkedList<Model3D.Edge>();

    // public java.util.List<Model3D.Edge> getList() {return list;}

    private Comparator<Model3D.Edge> comparator = new Comparator<Model3D.Edge>()
    {
	public int compare(Model3D.Edge e1, Model3D.Edge e2) {
	    // make sure edges grouped together are parallel.

	    if (e1.nx < e2.nx) return -1;
	    if (e1.nx > e2.nx) return 1;
	    if (e1.ny < e2.ny) return -1;
	    if (e1.ny >  e2.ny) return 1;
	    if (e1.nz < e2.nz) return -1;
	    if (e1.nz >  e2.nz) return 1;

	    // impose an order so edges with lower x1
	    // appear first, going to lower y1 and then
	    // lower z1 to break ties.
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

	    return 0;
	}
    };

    private Comparator<Model3D.Edge> ncomparator =
	new Comparator<Model3D.Edge>() {
	public int compare(Model3D.Edge e1, Model3D.Edge e2) {
	    // make sure edges grouped together are parallel.
	    if (e1.nx < e2.nx) return -1;
	    if (e1.nx > e2.nx) return 1;
	    if (e1.ny < e2.ny) return -1;
	    if (e1.ny > e2.ny) return 1;
	    if (e1.nz < e2.nz) return -1;
	    if (e1.nz > e2.nz) return 1;
	    
	    return 0;
	}
    };

    private Comparator<Model3D.Edge> ocomparator =
	new Comparator<Model3D.Edge>() {
	public int compare(Model3D.Edge e1, Model3D.Edge e2) {
	    // Ordering to test if two segments overlap.
	    // Used when e2's lower point is below e1's lower point.
	    // Then if e1's lower point is below e2's upper point,
	    // there is an overlap when the edges are on the same line. 
	    //
	    // e2 should be the value of'lower' below.  We ignore the
	    // case in which the end points of the edges match. While
	    // the edges are parallel as a precondition, they may not
	    // lie along the same line; hence a test for this condition.
	    if (e1.x1 == e2.x1 && e1.y1 == e2.y1 && e1.z1 == e2.z1
		&& e1.x2 == e2.x2 && e1.y2 == e2.y2 && e1.z2 == e2.z2) {
		return 0;
	    }
	    double nx = e2.x2 - e1.x1;
	    double ny = e2.y2 - e1.y1;
	    double nz = e2.z2 - e1.z1;
	    double norm = Math.sqrt(nx*nx + ny*ny + nz*nz);
	    nx = nx / norm;
	    ny = ny / norm;
	    nz = nz / norm;
	    // return 0 if the edges are not part of the
	    // same line (parallel is a precondition for calling this
	    // method)
	    if ((float)(nx * e1.nx + ny * e1.ny + nz * e1.nz) != (float)1.0) {
		return 0;
	    }

	    if (e1.x1 < e2.x2) return -1;
	    if (e1.x1 > e2.x2) return 1;
	    if (e1.y1 < e2.y2) return -1;
	    if (e1.y1 > e2.y2) return 1;
	    if (e1.z1 < e2.z2) return -1;
	    if (e1.z1 > e2.z2) return 1;
	    return 0;
	}
    };
    
    void clear() {
	list.clear();
    }

    void add(Model3D.Edge e) {
	list.add(e);
    }

    private boolean verifyAux(LinkedList<Model3D.Edge> result) {
	// check for triangles whose edges touch but that
	// would not intersect if they were offset slightly
	// from each other.
	final Model3D.Edge start = result.getFirst();
	final double[] vector1 = new double[3];
	final double[] vector2 = new double[3];
	final double[] vector3 = new double[3];
	final double[] ve1 = new double[3];
	final double[] ve2 = new double[3];
	vector1[0] = start.nx;
	vector1[1] = start.ny;
	vector1[2] = start.nz;
	vector2[0] = start.triangle.nx;
	vector2[1] = start.triangle.ny;
	vector2[2] = start.triangle.nz;
	VectorOps.crossProduct(vector3, vector1, vector2);
	Collections.sort(result, new Comparator<Model3D.Edge>() {
		public int compare(Model3D.Edge e1,
				   Model3D.Edge e2) {
		    ve1[0] = e1.triangle.nx;
		    ve1[1] = e1.triangle.ny;
		    ve1[2] = e1.triangle.nz;
		    ve2[0] = e2.triangle.nx;
		    ve2[1] = e2.triangle.ny;
		    ve2[2] = e2.triangle.nz;
		    double dot1 = VectorOps.dotProduct(vector3,
						       ve1);
		    double dot2 = VectorOps.dotProduct(vector3,
						       ve2);
		    double dot3 = VectorOps.dotProduct(vector2,
						       ve1);
		    double dot4 = VectorOps.dotProduct(vector2,
						       ve2);
		    double angle1 = Math.atan2(dot1, dot2);
		    double angle2 = Math.atan2(dot3, dot4);

		    if (angle1 < angle2) return -1;
		    if (angle1 > angle2) return 1;
		    return 0;
		}
	    });
	Model3D.Edge prevEdge = result.getLast();
	boolean evenOK = true;
	boolean oddOK = true;
	boolean oddCase = true;
	for (Model3D.Edge edge: result) {
	    if (prevEdge.reversed == edge.reversed) {
		if (oddCase) {
		    oddOK = false;
		} else {
		    evenOK = false;
		}
	    }
	    prevEdge = edge;
	    oddCase = !oddCase;
	}
	return (oddOK == false && evenOK == false);
    }

    java.util.List<Model3D.Edge> verify(boolean strict) {
	Collections.sort(list, comparator);
	LinkedList<Model3D.Edge> result = new LinkedList<>();
	LinkedList<Model3D.Edge> oresult = new LinkedList<>();
	Model3D.Edge first = null;
	int count = 2;
	Model3D.Edge lower = null;
	for (Model3D.Edge e: list) {
	    if (lower == null || ncomparator.compare(lower, e) != 0) {
		lower = e;
		oresult.clear();
	    } else {
		// ncomparator returns 0 only if the edges are parallel
		if (ocomparator.compare(e, lower) < 0) {
		    oresult.add(lower);
		    oresult.add(e);
		    return oresult;
		}
	    }
	    // the list was sorted so that edges with identical endpoints
	    // (including ones in opposite directions) are adjacent
	    if (first == null || comparator.compare(first, e) != 0) {
		if (strict) {
		    if (count != 2) {
			return result;
		    }
		} else {
		    if ((count % 2) == 1) {
			return result;
		    } else if (count > 0 && count != 2) {
			if (verifyAux(result)) {
			    return result;
			}
		    }
		}
		first = e;
		count = 0;
		result.clear();
	    }
	    count++;
	    if (strict && (count == 2) && (first.reversed == e.reversed)) {
		return result;
	    }
	    result.add(e);
	    if (e.triangle == null) {
	    }
	}
	if (strict) {
	    if (count != 2) {
		return result;
	    }
	} else {
	    if ((count % 2) == 1) {
		return result;
	    } else if (count > 0 && count != 2) {
		if (verifyAux(result)) {
		    return result;
		}
	    }
	}
	return null;
    }
}
