package org.bzdev.geom;
import org.bzdev.lang.StackTraceModePermission;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.Functions;
import org.bzdev.math.Functions.Bernstein;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;
import org.bzdev.math.GLQuadrature;
import org.bzdev.math.MatrixOps;
import org.bzdev.math.VectorOps;
// import org.bzdev.p3d.P3d;
import org.bzdev.util.Cloner;

import java.awt.Color;
import java.awt.geom.IllegalPathStateException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.security.*;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class implementation an arbitrary surface, approximated by
 * planar triangles, cubic B&eacute;zier triangles, and cubic B&eacute;zier
 * patches.
 * <P>
 * A surface is represented by a set of segments. There is an orientation
 * defined for each type based on the right-hand rule:
 * <UL>
 *   <LI> for a planar triangle - a triangle whose edges are straight lines -
 *        connecting vertices v<sub>1</sub>, v<sub>2</sub>, and v<sub>3</sub>,
 *        the orientation is determined by the direction of the normal vector
 *        given by
 *        (v<sub>a</sub> &times; v<sub>b</sub>) / |v<sub>a</sub> &times; v<sub>b</sub>|,
 *        where v<sub>a</sub> = v<sub>2</sub> - v<sub>1</sub> and
 *        v<sub>b</sub> = v<sub>3</sub> - v<sub>1</sub>.  This is equivalent
 *        to traversing the vertices in the order v<sub>1</sub>, v<sub>2</sub>,
 *        v<sub>3</sub> using the right-hand rule. When barycentric coordinates
 *        are used, the values of (u,v) are (0,0) for vertex v<sub>1</sub>,
 *        (1, 0) for vertex v<sub>2</sub>, and (0,1) for vertex v<sub>3</sub>.
 *   <LI> for a cubic triangle, which uses barycentric coordinates u, v, w
 *        (where u + v + w = 1), the orientation is provide by the right-hand
 *        rule applied to the sequence of vertices whose (u,v) coordinates
 *        are (0,0), (1, 0), and (0,1) respectively.
 *   <LI> for a cubic patch, which uses coordinates (u, v), the orientation
 *        is provide by the right-hand
 *        rule applied to the sequence of vertices whose (u,v) coordinates
 *        are (0,0), (1, 0), (1, 1), and (0,1) respectively.
 * </UL>
 * A surface can be configured via a constructor so as to be oriented or
 * not.  An oriented surface requires that when two segments share a common
 * edge, these segments have the same orientation.  The use of the right
 * hand rule implies that each of these two segments will traverse the
 * common edge in the opposite direction when each segment's edges are
 * traversed as described above.  A test provided by the method
 * {@link #isWellFormed()} determines if this constraint is met.  It also
 * tests that each edge is an edge of at most two segments.
 * <P>
 * When a surface does not have an orientation, each edge may be
 * shared by at most two segments, with the constraint on the
 * direction of traversal ignored. An example of a surface without an
 * orientation is a M&ouml;bius strip (the models made by gluing a
 * paper strip together with a half-twist are topologically cylinders
 * as the edges have a non-zero but very small width - the shape is
 * such that cutting down the center is equivalent to cutting a
 * cylinder along a M&ouml;bius strip.)
 * <P>
 * Well-formed surfaces have a path called a boundary associated with
 * them. For a closed surface (e.g., an approximation to a sphere),
 * the boundary is an empty path. In some cases, the boundary may
 * consist of two or more disjoint curves.
 */
public abstract class Surface3D implements Shape3D, SurfaceOps {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static final int INITIAL_SIZE = 64;
    static int MAX_INCR = 256;
    static int MAX_INCR_48 = MAX_INCR * 48;
    double[] tmpCoords = new double[48];

    int[] types;
    int[] cindices;
    Object[] tags = null;
    Color[] colors = null;
    int lastMoveTo = -1;
    int index = 0;
    int cindex = 0;

    /**
     * Get the number of segments contained in this surface.
     * @return the number of segments contained in this surface
     */
    @Override
    public int size() {
	return index;
    }

    /**
     * Get a segment's control points.
     * The values for the offset parameter are determined by the
     * method {@link #getSegment(int, double[])}. As a result, this
     * method should only be used by subclasses.
     * @param length the number of entries in the coords argument that
     *        will be filled, starting at index 0
     * @param offset an index indicating where the control point data
     *        will be found
     * @param coords an array to hold the results
     */
    abstract protected void
	getSegment(int length, int offset, double[] coords);

    /**
     * Get the control points for the ith segment for this surface.
     * The control points depend on the segment type.
     * The number entries in the coords array is
     * <UL>
     *   <LI> 48 when the return value is {@link SurfaceIterator#CUBIC_PATCH}.
     *   <LI> 30 when the return value is
     *        {@link SurfaceIterator#CUBIC_TRIANGLE}.
     *   <LI> {15 when the return value is {@link SurfaceIterator#CUBIC_VERTEX}.
     *   <LI> 12 when the return value is
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}.
     * <UL>
     * In general, the array's length should be at least 48.
     * The format for the array is described in the documentation for
     * <P>
     * Valid indices are non-negative integers smaller than the
     * value returned by {@link #size()}.
     * @param i the index
     * @param coords an array to hold the results
     * @return the segment type ({@link SurfaceIterator#CUBIC_PATCH},
     *         {@link SurfaceIterator#CUBIC_TRIANGLE},
     *         {@link SurfaceIterator#PLANAR_TRIANGLE}, or
     *         {@link SurfaceIterator#CUBIC_VERTEX})
     * @exception IllegalArgumentException the index i is out of range
     */
    @Override
    public int getSegment(int i, double[] coords)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= index) {
	    throw new IllegalArgumentException(errorMsg("index", i, index));
	}
	int type = types[i];
	int length = 0;
	switch(type) {
	case SurfaceIterator.CUBIC_PATCH:
	    length = 48;
	    break;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    length = 30;
	    break;
	case SurfaceIterator.PLANAR_TRIANGLE:
	    //length = 12;
	    length = 9;
	    break;
	case SurfaceIterator.CUBIC_VERTEX:
	    length = 15;
	    break;
	}
	getSegment(length, cindices[i], coords);
	return type;

    }

    /**
     * Get the tag for a segment.
     * Valid indices are non-negative integers smaller than the
     * value returned by {@link #size()}.
     * @param i the index of the segment
     * @return the tag for this segment
     * @exception IllegalArgumentException the index is out of range
     */
    @Override
    public Object getSegmentTag(int i) {
	if (i < 0 || i >= index) {
	    throw new IllegalArgumentException(errorMsg("index", i, index));
	}
	return tags[i];
    }

    @Override
    public Color getSegmentColor(int i) {
	if (i < 0 || i >= index) {
	    throw new IllegalArgumentException(errorMsg("index", i, index));
	}
	return colors[i];
    }


    boolean boundaryComputed = true;
    boolean lastMultipleEdges = false;
    boolean wellFormed = true;
    Path3D boundary = null;
    ArrayList<Object> boundaryTags = null;
    ArrayList<Color> boundaryColors = null;
    ArrayList<Integer> boundarySegments = null;
    ArrayList<Integer> edgeNumbers = null;

    boolean oriented = true;
    
    @Override
    public boolean isOriented() {return oriented;}

    boolean stackTraceMode = false;

    /**
     * Set stacktrace mode.
     * When stacktrace mode is on (true), surface segments are tagged with a
     * stack trace taken at the point when the segment was created.
     * The default value is false.
     * <P>
     * When the scrunner command is used, it should be run with the
     * option --trustLevel=1 or --trustLevel=2. When the trust level is 1,
     * the -t option is also needed for any script that modifies stack-trace
     * mode. For the default trust level, the -t option is also needed but
     * one must also grant the permission
     * {@link org.bzdev.lang.StackTraceModePermission} for
     * "{@link org.bzdev.geom.Surface3D}".
     * @param mode true to turn on stacktrace mode; false to turn it off
     * @exception SecurityException a security manager was installed
     *            and the permission
     *            org.bzdev.lang.StackTraceModePermission was not
     *            granted for the class org.bzdev.geom.Surface3D
     */
    public void setStackTraceMode(boolean mode) {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new StackTraceModePermission
			       ("org.bzdev.geom.Surface3D"));
	}
	stackTraceMode = mode;
	/*  not needed as the constructor always initializes tags
	if (mode == true && tags == null) {
	    tags = new Object[types.length];
	}
	*/
    }

    Surface3D() {
	types = new int[INITIAL_SIZE];
	cindices = new int[INITIAL_SIZE];
	tags = new Object[INITIAL_SIZE];
	colors = new Color[INITIAL_SIZE];
    }

    Surface3D(int initialCapacity) {
	types = new int[initialCapacity];
	cindices = new int[initialCapacity];
	tags = new Object[initialCapacity];
	colors = new Color[initialCapacity];
    }

    Surface3D(boolean oriented) {
	this();
	this.oriented = oriented;
    }

    Surface3D(int initialCapacity, boolean oriented) {
	this(initialCapacity);
	this.oriented = oriented;
    }

    abstract void copyCoords(int sInd, int dInd, int n);

    void expandIfNeeded(int n) {
	// All cases in which the boundary has to be
	// recomputed call this method, with the exception of reset().
	boundaryComputed = false;

	// n ignored - used by subclasses - unless n == -1 (which indicates
	// that no entry will be added specific to the subclass.  If n is 0,
	// we don't have to change anything. We do something when n == -1
	// in case a type is added but with no data to go with it.
	if (n == 0) return;
	if ((index + 1) < types.length) {
	    return;
	}
	int incr = index;
	if (incr > MAX_INCR) incr = MAX_INCR;
	int newsize = types.length + incr;
	int[] tmp = new int[newsize];
	System.arraycopy(types, 0, tmp, 0, index);
	types = tmp;
	tmp = new int[newsize];
	System.arraycopy(cindices, 0, tmp, 0, index);
	cindices = tmp;
	Object[] otmp = new Object[newsize];
	System.arraycopy(tags, 0, otmp, 0, index);
	tags = otmp;
	Color[] ctmp = new Color[newsize];
	System.arraycopy(colors, 0, ctmp, 0, index);
	colors = ctmp;
    }

    static abstract class Iterator implements SurfaceIterator {
	int index = 0;
	int surfaceIndex;
	int[] surfaceTypes;
	Object[]  surfaceTags;
	Color[] surfaceColors;
	Object tag;
	Color color;
	int cindex = 0;
	Surface3D surface;

	@Override
	public boolean isOriented() {return surface.isOriented();}

	int numbCoords(int type) {
	    switch (type) {
	    case SurfaceIterator.CUBIC_PATCH:
		return 48;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		return 30;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		return 9;
	    case SurfaceIterator.CUBIC_VERTEX:
		return 15;
	    default:
		return 0;
	    }
	}

	Iterator(Surface3D surface) {
	    this.surface = surface;
	    surfaceIndex = surface.index;
	    surfaceTypes = surface.types;
	    surfaceTags = surface.tags;
	    surfaceColors = surface.colors;
	    tag = surfaceTags[0];
	    color = surfaceColors[0];
	}

	@Override
	public boolean isDone() {
	    return index >= surfaceIndex;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public Color currentColor() {
	    return color;
	}

	@Override
	public void next() {
	    if (index < surfaceIndex) {
		int type = surfaceTypes[index++];
		cindex += numbCoords(type);
		if (surfaceTags == null) {
		    tag = null;
		} else {
		    tag = surfaceTags[index];
		}
		if (surfaceColors == null) {
		    color = null;
		} else {
		    color = surfaceColors[index];
		}
	    }
	}
    }

    /**
     * Print a tag used to label Surface3D features.
     * @param out the output
     * @param prefix a prefix to print at the start of a line
     * @param tag the tag itself
     * @exception IOException an I/O error occurred
     */
    public static void printTag(Appendable out, String prefix, Object tag)
        throws IOException
    {
        if (prefix == null) prefix = "";
        if (tag instanceof String) {
            String s = (String) tag;
            out.append(prefix + s.toString() + "\n");
        } else if (tag instanceof StackTraceElement[]) {
            StackTraceElement[] array = (StackTraceElement[]) tag;
            for (StackTraceElement e: array) {
                out.append(prefix + e.toString() + "\n");
            }
        } else {
            out.append(prefix + tag.toString() + "\n");
        }
    }



    static void printDegeneratePatch(int index, boolean e0z, boolean e1z,
				     boolean e2z, boolean e3z,
				     Object tag, Appendable out)
    {
	try {
	    out.append(String.format("entry %d has adjacent zero-length edges: "
				     + "zero-length edge numbers ",
				     index));
	    if (e0z) out.append(" 0");
	    if (e1z) out.append(" 1");
	    if (e2z) out.append(" 2");
	    if (e3z) out.append(" 3");
	    out.append("\n");
	    if (tag != null) {
		if (tag instanceof StackTraceElement) {
		    out.append("Stacktrace for creation of entry "
			       + index + ":");
		    printTag(out, "    ", tag);
		} else {
		    out.append(tag.toString());
		}
	    }
	} catch (Exception e){}
    }

    static void printEdgeLoop(int entry, int edge, Object tag, Appendable out) {
	try {
	    out.append(String.format("entry %d has an edge loop at edge %d\n",
				     entry, edge));
	    if (tag != null) {
		if (tag instanceof StackTraceElement) {
		    out.append("Stacktrace for creation of entry "
			       + entry + ":");
		    printTag(out, "    ", tag);
		} else {
		    out.append(tag.toString());
		}
	    }
	} catch (Exception e){}
     }

    static void printDegenerateTriangle(int entry, int edge,
					Object tag, Appendable out)
    {
	try {
	    out.append(String.format("entry %d: edge %d connects the "
				     + "same vertices",
				     entry, edge));
	    if (tag != null) {
		if (tag instanceof StackTraceElement) {
		    out.append("Stacktrace for creation of entry "
			       + entry + ":");
		    printTag(out, "    ", tag);
		} else {
		    out.append(tag.toString());
		}
	    }
	} catch (Exception e){}
     }



    /**
     * Create an array of components given a surface iterator.
     * This is provided as a convenience for classes implementing
     * the Shape3D interface.
     * @param si the surface iterator
     * @param out an object to log error messages
     * @return a Shape3D array containing the components
     */
    public static Shape3D[] createComponents(SurfaceIterator si,
					     Appendable out) {
	// create a surface so we'll have the types and coords arrays.
	Surface3D surface = new Surface3D.Double(si.isOriented());
	surface.append(si);
	surface.computeComponents(out);
	return surface.components;
    }

    static void printTag(Appendable out, Object tag) throws IOException {
	if (tag instanceof StackTraceElement[]) {
	    printTag(out, "    ", "StackTrace at creation of entry:");
	    printTag(out, "        ", tag);
	} else {
	    printTag(out, "    Tag: ", tag);
	}
    }

    static void printTag(Appendable out, int enumber, Object tag)
	throws IOException
    {
	if (tag instanceof StackTraceElement[]) {
	    printTag(out, "    ", "StackTrace for creation of entry "
		     + enumber + ":\n");
	    printTag(out, "        ", tag);
	} else {
	    printTag(out, "    Tag for creation of entry " + enumber
		     + ": ", tag);
	}
    }


    /**
     * Class for computing the boundaries of a surface from a shape iterator.
     * The iterator will be modified when one of this class' constructors is
     * called.
     */
    public static class Boundary {

	boolean oriented = true;

	boolean hasEdgeLoop(double[] coords, int ind1, int ind2,
			    int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return true;
		if (coords[ind1+i] != coords[ind3+i]) return true;

	    }
	    return false;
	}

	boolean identicalCP(double[] coords,
			    int ind1, int ind2, int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return false;
		if (coords[ind1+i] != coords[ind3+i]) return false;
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    return true;
	}

	static class Edge implements Comparable<Edge> {
	    int entryNumber;
	    int edgeNumber;
	    int type;
	    Color color;
	    Object tag;
	    boolean oriented;
	    boolean has0;
	    double x0, y0, z0;
	    boolean has1;
	    double x1, y1, z1;
	    boolean has2;
	    double x2, y2, z2;
	    boolean has3;
	    double x3, y3, z3;
	    boolean reversed;
	    public String toString() {
		String typeString;
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    typeString = "planar-triangle";
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    typeString = "cubic-planar-triangle";
		case SurfaceIterator.CUBIC_TRIANGLE:
		    typeString = "cubic-triangle";
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    typeString = "cubic-patch";
		    break;
		default:
		    typeString = "<Unknown>";
		    break;
		}
		String fmt1 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)]";
		String fmt2 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)"
		    + "->(%g,%g,%g)->(%g,%g,%g)]";
		if (!has1 /*cindex1 == -1*/) {
		    if (reversed) {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     x3, y3, z3,
					     x0, y0, z0);
		    } else {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     x0, y0, z0,
					     x3, y3, z3);
		    }
		} else {
		    if (reversed) {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     x3, y3, z3,
					     x2, y2, z2,
					     x1, y1, z1,
					     x0, y0, z0);
		    } else {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     x0, y0, z0,
					     x1, y1, z1,
					     x2, y2, z2,
					     x3, y3, z3);
		    }
		}
	    }

	    Point3D getBasePoint() {
		if (reversed) {
		    return new Point3D.Double(x3, y3, z3);
		} else {
		    return new Point3D.Double(x0, y0, z0);
		}
	    }

	    Point3D getFinalPoint() {
		if (reversed) {
		    return new Point3D.Double(x0, y0, z0);
		} else {
		    return new Point3D.Double(x3, y3, z3);
		}
	    }


	    Point3D getCP1() {
		if (!has1 /*cindex1 == -1*/) return null;
		if (reversed) {
		    return new Point3D.Double(x2, y2, z2);
		} else {
		    return new Point3D.Double(x1, y1, z1);
		}
	    }

	    Point3D getCP2() {
		if (!has1 /*cindex1 == -1*/) return null;
		if (reversed) {
		    return new Point3D.Double(x1, y1, z1);
		} else {
		    return new Point3D.Double(x2, y2, z2);
		}
	    }


	    Edge(int type, double[] coords, Color color, Object tag,
		 int cindex0, int cindex3, boolean reversed,
		 int entryNumber, int edgeNumber, boolean oriented)
	    {
		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		this.oriented = oriented;
		double x0 = coords[cindex0++];
		double y0 = coords[cindex0++];
		double z0 = coords[cindex0];

		double x3 = coords[cindex3++];
		double y3 = coords[cindex3++];
		double z3 = coords[cindex3];

		boolean flip = false;
		if (x0 > x3) {
		    flip = true;
		    reversed = !reversed;
		} else if (x0 == x3) {
		    if (y0 > y3) {
			flip = true;
			reversed = !reversed;
		    } else if (y0 == y3) {
			if (z0 > z3) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		has0 = true; has3 = true;
		if (flip) {
		    this.x3 = x0; this.y3 = y0; this.z3 = z0;
		    this.x0 = x3; this.y0 = y3; this.z0 = z3;
		} else {
		    this.x0 = x0; this.y0 = y0; this.z0 = z0;
		    this.x3 = x3; this.y3 = y3; this.z3 = z3;
		}
		has1 = false;
		has2 = false;
		this.reversed = reversed;
	    }

	    Edge(int type, double[]coords, Color color, Object tag,
		 int cindex0, int cindex1, int cindex2, int cindex3,
		 boolean reversed, int entryNumber, int edgeNumber,
		 boolean oriented)
	    {
		double x0 = coords[cindex0++];
		double y0 = coords[cindex0++];
		double z0 = coords[cindex0];

		double x1 = coords[cindex1++];
		double y1 = coords[cindex1++];
		double z1 = coords[cindex1];

		double x2 = coords[cindex2++];
		double y2 = coords[cindex2++];
		double z2 = coords[cindex2];

		double x3 = coords[cindex3++];
		double y3 = coords[cindex3++];
		double z3 = coords[cindex3];

		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		this.oriented = oriented;
		boolean flip = false;
		if (x0 > x3) {
		    flip = true;
		    reversed = !reversed;
		} else if (x0 == x3) {
		    if (y0 > y3) {
			flip = true;
			reversed = !reversed;
		    } else if (y0 == y3) {
			if (z0 > z3) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		has0 = true; has1 = true; has2 = true; has3 = true;
		if (flip) {
		    this.x0 = x3; this.y0 = y3; this.z0 = z3;
		    this.x1 = x2; this.y1 = y2; this.z1 = z2;
		    this.x2 = x1; this.y2 = y1; this.z2 = z1;
		    this.x3 = x0; this.y3 = y0; this.z3 = z0;
		} else {
		    this.x0 = x0; this.y0 = y0; this.z0 = z0;
		    this.x1 = x1; this.y1 = y1; this.z1 = z1;
		    this.x2 = x2; this.y2 = y2; this.z2 = z2;
		    this.x3 = x3; this.y3 = y3; this.z3 = z3;
		}
		this.reversed = reversed;
	    }

	    public int match(Edge other) {
		if (x0 != other.x0) return (x0 < other.x0)? -1: 1;
		if (y0 != other.y0) return (y0 < other.y0)? -1: 1;
		if (z0 != other.z0) return (z0 < other.z0)? -1: 1;
		if (x3 != other.x3) return (x3 < other.x3)? -1: 1;
		if (y3 != other.y3) return (y3 < other.y3)? -1: 1;
		if (z3 != other.z3) return (z3 < other.z3)? -1: 1;
		if (has1 && other.has1) {
		    if (x1 != other.x1) return (x1 < other.x1)? -1: 1;
		    if (y1 != other.y1) return (y1 < other.y1)? -1: 1;
		    if (z1 != other.z1) return (z1 < other.z1)? -1: 1;
		    if (x2 != other.x2) return (x2 < other.x2)? -1: 1;
		    if (y2 != other.y2) return (y2 < other.y2)? -1: 1;
		    if (z2 != other.z2) return (z2 < other.z2)? -1: 1;
		} else if (!has1) {
		    if (other.has1) {
			// include rounding to floats because the
			// methods that add segments do that.
			double xx1 = (float)(x0*2.0/3.0 + x3/3.0);
			double yy1 = (float)(y0*2.0/3.0 + y3/3.0);
			double zz1 = (float)(z0*2.0/3.0 + z3/3.0);
			double xx2 = (float)(x0/3.0 + x3*2.0/3.0);
			double yy2 = (float)(y0/3.0 + y3*2.0/3.0);
			double zz2 = (float)(z0/3.0 + z3*2.0/3.0);
			if (other.x1 != xx1) return (other.x1 < xx1)? -1: 1;
			if (other.y1 != yy1) return (other.y1 < yy1)? -1: 1;
			if (other.z1 != zz1) return (other.z1 < zz1)? -1: 1;
			if (other.x2 != xx2) return (other.x2 < xx2)? -1: 1;
			if (other.y2 != yy2) return (other.y2 < yy2)? -1: 1;
			if (other.z2 != zz2) return (other.z2 < zz2)? -1: 1;
		    }
		} else if (!other.has1) {
		    // include rounding to floats because the
		    // methods that add segments do that.
		    double xx1 = (float)(other.x0*2.0/3.0 + other.x3/3.0);
		    double yy1 = (float)(other.y0*2.0/3.0 + other.y3/3.0);
		    double zz1 = (float)(other.z0*2.0/3.0 + other.z3/3.0);
		    double xx2 = (float)(other.x0/3.0 + other.x3*2.0/3.0);
		    double yy2 = (float)(other.y0/3.0 + other.y3*2.0/3.0);
		    double zz2 = (float)(other.z0/3.0 + other.z3*2.0/3.0);
		    if (x1 != xx1) return (x1 < xx1)? -1: 1;
		    if (y1 != yy1) return (y1 < yy1)? -1: 1;
		    if (z1 != zz1) return (z1 < zz1)? -1: 1;
		    if (x2 != xx2) return (x2 < xx2)? -1: 1;
		    if (y2 != yy2) return (y2 < yy2)? -1: 1;
		    if (z2 != zz2) return (z2 < zz2)? -1: 1;
		}
		return 0;
	    }

	    @Override
		public int compareTo (Edge other) {
		int m = match(other);
		if (m == 0) {
		    if (reversed == other.reversed) {
			if (entryNumber == other.entryNumber) {
			    if (edgeNumber == other.edgeNumber) {
				return 0;
			    } else {
				return (edgeNumber < other.edgeNumber)? -1: 1;
			    }
			} else {
			    return (entryNumber < other.entryNumber)? -1: 1;
			}
		    } else {
			return (reversed? -1: 1);
		    }
		} else {
		    return m;
		}
	    }

	    @Override
		public boolean equals(Object object) {
		if (object instanceof Edge) {
		    Edge other = (Edge)object;
		    return (compareTo(other) == 0);
		} else {
		    return false;
		}
	    }

	    @Override
		public int hashCode() {
		long tmp = (reversed? 0: 1) * 31;
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(x0))*31;
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(y0))*31;
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(z0))*31;
		if (has1) {
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(x1))*31;
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(y1))*31;
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(z1))*31;
		}
		if (has2) {
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(x2))*31;
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(y2))*31;
		    tmp = (tmp ^ java.lang.Double.doubleToLongBits(z2))*31;
		}
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(x3))*31;
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(y3))*31;
		tmp = (tmp ^ java.lang.Double.doubleToLongBits(z3))*31;
		return ((int) tmp) ^ ((int)(tmp >> 32));
	    }

	    public boolean oppositeFrom(Edge other) {
		return reversed != other.reversed;
	    }

	    public Object getTag() {return tag;}
	}

	boolean wellFormed = true;
	Path3D boundary = null;
	ArrayList<Object> boundaryTags = null;
	ArrayList<Color> boundaryColors = null;
	ArrayList<Integer> boundarySegments = null;
	ArrayList<Integer> edgeNumbers = null;
	/**
	 * Determine if the surface corresponding to this boundary
	 * is well formed.
	 * <P>
	 * Two edges match if they attach the same vertices. For an
	 * oriented surface, the direction of an edge is the direction
	 * in which a segment would be traversed counterclockwise when
	 * viewed from the outside of the surface, thus determining
	 * which vertex is the start of and edge and which is its end.
	 * An oriented surface is well formed if zero or two edges
	 * connect each pair of vertices, and if the directions are
	 * opposite for the two-vertex case, and if no edge has zero
	 * length. A non-oriented surface is well formed if either
	 * zero or two edges connect each pair of vertices and if no
	 * edge has zero length.
	 * <P>
	 * Well-formed surfaces are essentially surfaces that do not
	 * have a boundary.
	 * @return true if it is well formed; false otherwise
	 */
	public boolean isWellFormed() {
	    return wellFormed;
	}

	/**
	 * Get the path containing a surface's boundary.
	 * @return the path representing a surface's boundary
	 */
	public Path3D getPath() {
	    if (boundary == null)  return null;
	    try {
		return Cloner.makeClone(boundary);
	    } catch (CloneNotSupportedException e) {
		if (boundary instanceof Path3D.Double)
		    return new Path3D.Double(boundary);
		else if (boundary instanceof Path3D.Float) {
		    return new Path3D.Float(boundary);
		} else {
		    throw new IllegalStateException
			(errorMsg("unrecognizedPath3DType"));
		}
	    }
	}

	/**
	 * Get the tags for each segment along the boundary.
	 * When using a path iterator for the boundary, counting the
	 * lineTo and moveTo operations will generate the index for
	 * the corresponding segment.
	 * @return the tags in the same order as the boundary's path segments;
	 *         null if the surface is not well formed
	 */
	public Object[] getTags() {
	    if (boundaryTags == null) return null;
	    return boundaryTags.toArray();
	}

	public Object[] getColors() {
	    if (boundaryColors == null) return null;
	    return boundaryColors.toArray();
	}

	/**
	 * Get the indices for the segments bordering on this object's
	 * boundary.
	 * When using a path iterator for the boundary, counting the
	 * lineTo and moveTo operations will generate the index, into
	 * the array returned, for the corresponding segment's index.
	 * The index for a segment is a count of the number of calls to
	 * {@link SurfaceIterator#next()} used to obtain the specific
	 * segment from the surface iterator used in this object's
	 * constructor.
	 * @return an array containing the segment indices; null if the surface
	 *         is not well formed
	 */
	public int[] getSegmentIndices() {
	    if (boundarySegments == null) return null;;
	    int[] result = new int[boundarySegments.size()];
	    int i = 0;
	    for (Integer e: boundarySegments) {
		result[i++] = e;
	    }
	    return result;
	}

	/**
	 * Get the edge numbers of the surface segments corresponding to
	 * each path segment along the boundary.
	 * When using a path iterator for the boundary, counting the
	 * lineTo and moveTo operations will generate the index, into
	 * the array returned, for the corresponding segment.
	 * <P>
	 * The edge segments are defined as follows:
	 * <UL>
	 *   <LI> For a planar triangle,
	 *       <UL>
	 *          <LI> edge 0 is the edge from vertex 1 to vertex 2
	 *          <LI> edge 1 is the edge from vertex 2 to vertex 3
	 *          <LI> edge 2 is the edge from vertex 3 to vertex 1
	 *       <UL>
	 *   <LI> For a cubic triangle, described in (u,v) coordinates
	 *        where the barycentric coordinates are (u, v, w) with
	 *        w = 1 - u - v,
	 *       <UL>
	 *          <LI> edge 0 is the edge from (0, 0) to (1, 0)
	 *          <LI> edge 1 is the edge from (1, 0) to (0, 1)
	 *          <LI> edge 2 is the edge from (0, 1) to (0,0)
	 *       <UL>
	 *   <LI> For a cubic patch, described in (u,v) coordinates
	 *       <UL>
	 *          <LI> edge 0 is the edge from (0, 0) to (1, 0)
	 *          <LI> edge 1 is the edge from (1, 0) to (1, 1)
	 *          <LI> edge 2 is the edge from (1, 1) to (0, 1)
	 *          <LI> edge 3 is the edge from (0, 1) to (0, 0)
	 *       <UL>
	 * </UL>
	 * @return the edge numbers for the surface segments adjacent to the
	 *         path segments along the boundary; null if the 3D shape
	 *         is not well formed
	 */
	public int[] getEdgeNumbers() {
	    if (edgeNumbers == null) return null;
	    int[] result = new int[edgeNumbers.size()];
	    int i = 0;
	    for (Integer e: edgeNumbers) {
		result[i++] = e;
	    }
	    return result;
	}

	void computeBoundary (SurfaceIterator it, Appendable out) {
	    double[] coords = new double[48];
	    LinkedList<Edge> queue = new LinkedList<Edge>();
	    int i = -1;
	    boolean oriented0 = false;
	    while (!it.isDone()) {
		i++;
		int type = it.currentSegment(coords);
		Color color = it.currentColor();
		Object tag = it.currentTag();
		boolean oriented = it.isOriented();
		if (i == 0) oriented0 = oriented;
		Edge e;
		switch(type) {
		case SurfaceIterator.CUBIC_VERTEX:
		    if (identicalCP(coords, 0, 3, 6, 9)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tag, out);
			}
		    }
		    e = new Edge(type, coords, color, tag, 0, 3, 6, 9,
				 false, i, 0, oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 9, 12,
				 false, i, 1, oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 12, 0,
				 false, i, 2, oriented);
		    queue.offer(e);
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    e = new Edge(type, coords, color, tag, 0, 6, false, i, 0,
				 oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 6, 3, false,
				 i, 1, oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 0, 3, true, i, 2,
				 oriented);
		    queue.offer(e);
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (identicalCP(coords, 0, 4*3, 7*3, 9*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tag, out);
			}
		    }
		    if (identicalCP(coords, 9*3, 8*3, 6*3, 3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 1, tag, out);
			}
		    }
		    if (identicalCP(coords, 0, 1*3, 2*3, 3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 2, tag, out);
			}
		    }
		    e = new Edge(type, coords, color, tag, 0, 4*3, 7*3, 9*3,
				 false, i, 0, oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 9*3, 8*3, 6*3, 3*3,
				 false, i, 1, oriented);
		    queue.offer(e);
		    e = new Edge(type, coords, color, tag, 0, 1*3, 2*3, 3*3,
				 true, i, 2, oriented);
		    queue.offer(e);
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    boolean e1z = false;
		    boolean e2z = false;
		    boolean e3z = false;
		    boolean e4z = false;
		    if (identicalCP(coords, 0, 3, 6, 9)) {
			e1z = true;
			if (hasEdgeLoop(coords, 0, 3, 6, 9)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 0, tag, out);
			}
		    } else {
			e = new Edge(type, coords, color, tag, 0, 3, 6, 9,
				     false, i, 0, oriented);
			queue.offer(e);
		    }
		    if (identicalCP(coords, 9, 21, 33, 45)) {
			e2z = true;
			if (hasEdgeLoop(coords, 9, 21, 33, 45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 1, tag, out);
			}
			if (e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, e2z, false, false,
						     tag,
						     out);
			    }
			}
		    } else {
			e = new Edge(type, coords, color, tag, 9, 21, 33, 45,
				     false, i, 1, oriented);
			queue.offer(e);
		    }
		    if (identicalCP(coords, 36, 39, 42, 45)) {
			e3z = true;
			if (hasEdgeLoop(coords, 36, 39, 42, 45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 2, tag, out);
			}
			if (e2z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, false, e2z, e3z, false,
						     tag,
						     out);
			    }
			}
		    } else {
			e = new Edge(type, coords, color, tag, 36, 39, 42, 45,
				     true, i, 2, oriented);
			queue.offer(e);
		    }
		    if (identicalCP(coords, 0, 12, 24, 36)) {
			e4z = true;
			if (hasEdgeLoop(coords, 0, 12, 34, 36)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 3, tag, out);
			}
			if (e3z || e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, false, e3z, e4z,
						     tag,
						     out);
			    }
			}
		    } else {
			e = new Edge(type, coords, color,  tag, 0, 12, 24, 36,
				     true, i, 3, oriented);
			queue.offer(e);
		    }
		    break;
		}
		it.next();
	    }
	    Edge[] array = new Edge[queue.size()];
	    queue.toArray(array);
	    Arrays.sort(array);
	    int sz = Math.round(1.5F * queue.size()) + 1;
	    Map<Point3D,Edge> edgeMap = new HashMap<Point3D,Edge>(sz);
	    Map<Point3D,Edge> edgeMap2 = oriented? null:
		new HashMap<Point3D,Edge>(sz);
	    queue.clear();
	    int lower = 0;
	    int upper = 1;
	    while (lower < array.length) {
		boolean reversed = false;
		// boolean oriented = array[lower].oriented;
		while (upper < array.length) {
		    if (array[lower].match(array[upper]) == 0) {
			reversed =
			    (array[lower].reversed != array[upper].reversed);
			if (array[upper].oriented) oriented = true;
			upper++;
		    } else {
			break;
		    }
		}
		if (upper - lower == 1) {
		    Edge e = array[lower];
		    Point3D p = e.getBasePoint();
		    if (!oriented) {
			Point3D p1 = p;
			Point3D p2 = e.getFinalPoint();
			queue.offer(e);
			if (edgeMap.get(p1) == null) {
			    edgeMap.put(p1, e);
			} else {
			    if (edgeMap2.get(p1) == null) {
				edgeMap2.put(p1, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p1);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
			if (edgeMap.get(p2) == null) {
			    edgeMap.put(p2, e);
			} else {
			    if (edgeMap2.get(p2) == null) {
				edgeMap2.put(p2, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p2);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
		    } else if (edgeMap.containsKey(p)) {
			wellFormed = false;
			if (out != null) {
			    Edge ee = edgeMap.get(p);
			    try {
				out.append(String.format("bad boundary: "
							 + "edges for "
							 + "entry %d (edge %d) "
							 + "and "
							 + "entry %d (edge %d) "
							 + "share the same "
							 + "start point\n",
							 e.entryNumber,
							 e.edgeNumber,
							 ee.entryNumber,
							 ee.edgeNumber));
				out.append(String.format(" ... %s\n",
							 e.toString()));
				out.append(String.format(" ... %s\n",
							 ee.toString()));
				if (e != null && e.tag != null) {
				    printTag(out, e.tag);
				}
				if (ee != null && ee.tag != null) {
				    printTag(out, ee.tag);
				}
			    } catch(IOException eio) {
				throw new RuntimeException (eio.getMessage(),
							    eio);
			    }
			}
		    } else {
			queue.offer(e);
			edgeMap.put(p,e);
		    }
		} else 	if (!((upper-lower) == 2 && (reversed || !oriented))) {
		    wellFormed = false;
		    if (out != null) {
			try {
			    out.append("edge conflict:\n");
			    for (int j = lower; j < upper; j++) {
				Edge eee = array[j];
				int eindex = eee.entryNumber;
				out.append((eee == null)? "<no edge>":
					   eee.toString());
				out.append("\n");
				if (eee != null && eee.tag != null) {
				    printTag(out, eee.tag);
				} else {
				    out.append("\n");
				}
			    }
			} catch (IOException eio) {
			    throw new RuntimeException (eio.getMessage(), eio);
			}
		    }
		}
		lower = upper;
		upper++;
	    }
	    if (wellFormed) {
		Edge e;
		Path3D path = new Path3D.Double();
		boundaryTags = new ArrayList<Object>();
		boundaryColors = new ArrayList<Color>();
		boundarySegments = new ArrayList<Integer>();
		edgeNumbers = new ArrayList<Integer>();
		while ((e = queue.poll()) != null) {
		    Point3D bp = e.getBasePoint();
		    if (edgeMap.containsKey(bp)
			|| (!oriented && edgeMap2.containsKey(bp))) {
			path.moveTo(bp.getX(), bp.getY(), bp.getZ());
			Point3D cp1 = e.getCP1();
			Point3D cp2 = e.getCP2();
			Point3D fp = e.getFinalPoint();
			if (cp1 == null || cp2 == null) {
			    path.lineTo(fp.getX(), fp.getY(), fp.getZ());
			} else {
			    path.curveTo(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 fp.getX(), fp.getY(), fp.getZ());
			}
			boundaryTags.add(e.getTag());
			boundaryColors.add(e.color);
			boundarySegments.add(e.entryNumber);
			edgeNumbers.add(e.edgeNumber);
			Edge n = null;
			if (oriented) {
			    n = edgeMap.remove(fp);
			} else {
			    if (e != edgeMap.get(fp)) {
				n = edgeMap.remove(fp);
			    } else {
				n  = edgeMap2.remove(fp);
			    }
			    if (n != null) {
				Point3D p1 = n.getBasePoint();
				Point3D p2 = n.getFinalPoint();
				if (edgeMap.get(p1) == n) {
				    edgeMap.remove(p1);
				}
				if (edgeMap.get(p2) == n) {
				    edgeMap.remove(p2);
				}
				if (edgeMap2.get(p1) == n) {
				    edgeMap2.remove(p1);
				}
				if (edgeMap2.get(p2) == n) {
				    edgeMap2.remove(p2);
				}
			    }
			}

			Point3D cp3 = fp;
			if (n != null) {
			    while (n != e) {
				if (n == null) break;
				if (oriented) {
				    cp1 = n.getCP1();
				    cp2 = n.getCP2();
				    cp3 = n.getFinalPoint();
				} else {
				    Point3D base = n.getBasePoint();
				    if (base.equals(cp3)) {
					cp1 = n.getCP1();
					cp2 = n.getCP2();
					cp3 = n.getFinalPoint();
				    } else {
					cp1 = n.getCP2();
					cp2 = n.getCP1();
					cp3 = n.getBasePoint();
				    }
				}
				if (cp1 == null || cp2 == null) {
				    path.lineTo(cp3.getX(),
						cp3.getY(),
						cp3.getZ());
				} else {
				    path.curveTo
					(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 cp3.getX(), cp3.getY(), cp3.getZ());
				}
				boundaryTags.add(n.getTag());
				boundaryColors.add(n.color);
				boundarySegments.add(n.entryNumber);
				edgeNumbers.add(n.edgeNumber);
				if (oriented) {
				    n = edgeMap.remove(cp3);
				} else {
				    Edge next = edgeMap.get(cp3);
				    if (next != null && n != next) {
					n = edgeMap.remove(cp3);
				    } else {
					n = edgeMap2.remove(cp3);
				    }
				    if (n != null) {
					Point3D p1 = n.getBasePoint();
					Point3D p2 = n.getFinalPoint();
					if (n == edgeMap.get(p1)) {
					    edgeMap.remove(p1);
					}
					if (n == edgeMap.get(p2)) {
					    edgeMap.remove(p2);
					}
					if (n == edgeMap2.get(p1)) {
					    edgeMap2.remove(p1);
					}
					if (n == edgeMap2.get(p2)) {
					    edgeMap2.remove(p2);
					}
				    }
				}
			    }
			    if (n == e) path.closePath();
			}
			edgeMap.remove(bp);
		    }
		}
		boundary = path;
	    } else {
		boundary = null;
		boundaryTags = null;
		boundaryColors = null;
		boundarySegments = null;
		edgeNumbers = null;
	    }
	}

	/*
	 * Constructor for an oriented surface.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param it a surface iterator describing a surface whose
	 *        boundary is to be computed
	 */
	public Boundary(SurfaceIterator it) {
	    computeBoundary(it, null);
	}

	/*
	 * Constructor specifying if the surface is oriented.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param it a surface iterator describing a surface whose
	 *        boundary is to be computed
	 * @param oriented true if the surface is oriented; false if
	 *        it is not oriented
	 */
	public Boundary(SurfaceIterator it, boolean oriented) {
	    this.oriented = oriented;
	    computeBoundary(it, null);
	}

	/*
	 * Constructor for an oriented surface with an Appendable
	 * for recording messages describing conditions that imply
	 * that a surface is not well formed.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param it a surface iterator describing a surface whose
	 *        boundary is to be computed
	 * @param out an object used to record messages describing
	 *        conditions that imply that a surface is not
	 *        well formed.
	 */
	public Boundary(SurfaceIterator it, Appendable out) {
	    computeBoundary(it, out);
	}

	/*
	 * Constructor specifying if the surface is oriented, and
	 *  with an Appendable for recording messages
	 * describing conditions that imply that a surface is not
	 * well formed.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param it a surface iterator describing a surface whose
	 *        boundary is to be computed
	 * @param oriented true if the surface is oriented; false if
	 *        it is not oriented
	 * @param out an object used to record messages describing
	 *        conditions that imply that a surface is not
	 *        well formed.
	 */
	public Boundary(SurfaceIterator it, boolean oriented,
			Appendable out)
	{
	    this.oriented = oriented;
	    computeBoundary(it, out);
	}
    }

    boolean componentsComputed = false;
    Shape3D[] components = null;

    abstract void computeComponents(Appendable out);

    /**
     * Get the number of components for a surface.
     * Each component is a connected region of a surface.
     * @return the number of components for this surface;
     *         0 if the surface is not well formed (e.g., is not
     *         a 2-manifold)
     */
    public int numberOfComponents()  {
	if (componentsComputed == false) {
	    computeComponents(null);
	}
	return (components == null)? 0: components.length;
    }
    /**
     * Get the i<sup>th</sup> component of a surface
     * The parameter's value is in the range [0,n), where n is the
     * number of components for this surface.
     * Each component is a connected region of a surface.
     * @param i the index for the i<sup>th</sup> component of this surface
     * @return the i<sup>th</sup> component of a surface
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException the surface is not well formed
     */
    public Shape3D getComponent(int i) {
	if (componentsComputed == false) {
	    computeComponents(null);
	}
	if (components == null) {
	    throw new IllegalStateException(errorMsg("notWellFormed"));
	} else {
	    if ((i >= 0) && (i < components.length)) {
		return components[i];
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("illegalComponentIndex", i, components.length));
	    }
	}
    }


    /**
     * Surface3D class that stores the coordinates of control
     * points as double-precision numbers.
     * <P>
     * Although stored as double-precision numbers, when a surface
     * feature is created, the control points are rounded to the
     * nearest float value by first casting the value as a float
     * and then recasting it as a double.  This rounding is done to
     * reduce the chances that two expressions that are intended to
     * compute the same coordinate will not result in different values
     * being stored due to double-precision errors.
     */
    public static class Double extends Surface3D {
	double coords[] = new double[INITIAL_SIZE * 48];

	@Override
	protected  final void
	    getSegment(int length, int offset, double[] scoords)
	{
	    System.arraycopy(coords, offset, scoords, 0, length);
	}


	class Edge implements Comparable<Edge> {
	    int entryNumber;
	    int edgeNumber;
	    int type;
	    Color color;
	    Object tag;
	    int cindex0;
	    int cindex1;
	    int cindex2;
	    int cindex3;
	    boolean reversed;

	    // The field used is provided for computeComponents.
	    // and is not included in the hash code.
	    boolean used = false;

	    public String toString() {
		String typeString;
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    typeString = "planar-triangle";
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    typeString = "cubic-planar triangle";
		case SurfaceIterator.CUBIC_TRIANGLE:
		    typeString = "cubic-triangle";
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    typeString = "cubic-patch";
		    break;
		default:
		    typeString = "<Unknown>";
		    break;
		}
		String fmt1 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)]";
		String fmt2 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)"
		    + "->(%g,%g,%g)->(%g,%g,%g)]";
		if (cindex1 == -1) {
		    if (reversed) {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2],
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2]);
		    } else {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2],
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2]);
		    }
		} else {
		    if (reversed) {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2],
					     coords[cindex2],
					     coords[cindex2+1],
					     coords[cindex2+2],
					     coords[cindex1],
					     coords[cindex1+1],
					     coords[cindex1+2],
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2]);
		    } else {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2],
					     coords[cindex1],
					     coords[cindex1+1],
					     coords[cindex1+2],
					     coords[cindex2],
					     coords[cindex2+1],
					     coords[cindex2+2],
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2]);
		    }
		}
	    }

	    Point3D getBasePoint() {
		if (reversed) {
		    return new Point3D.Double(coords[cindex3],
					      coords[cindex3+1],
					      coords[cindex3+2]);
		} else {
		    return new Point3D.Double(coords[cindex0],
					      coords[cindex0+1],
					      coords[cindex0+2]);
		}
	    }

	    Point3D getFinalPoint() {
		if (reversed) {
		    return new Point3D.Double(coords[cindex0],
					      coords[cindex0+1],
					      coords[cindex0+2]);
		} else {
		    return new Point3D.Double(coords[cindex3],
					      coords[cindex3+1],
					      coords[cindex3+2]);
		}
		
	    }


	    Point3D getCP1() {
		if (cindex1 == -1) return null;
		if (reversed) {
		    return new Point3D.Double(coords[cindex2],
					      coords[cindex2+1],
					      coords[cindex2+2]);
		} else {
		    return new Point3D.Double(coords[cindex1],
					      coords[cindex1+1],
					      coords[cindex1+2]);
		}
		
	    }

	    Point3D getCP2() {
		if (cindex1 == -1) return null;
		if (reversed) {
		    return new Point3D.Double(coords[cindex1],
					      coords[cindex1+1],
					      coords[cindex1+2]);
		} else {
		    return new Point3D.Double(coords[cindex2],
					      coords[cindex2+1],
					      coords[cindex2+2]);
		}
	    }

	    double[] getCoords() {return coords;}


	    Edge(int type, Color color, Object tag,
		 int cindex0, int cindex3, boolean reversed,
		 int entryNumber, int edgeNumber)
	    {
		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		boolean flip = false;
		if (coords[cindex0] > coords[cindex3]) {
		    flip = true;
		    reversed = !reversed;
		} else if (coords[cindex0] == coords[cindex3]) {
		    if (coords[cindex0+1] > coords[cindex3+1]) {
			flip = true;
			reversed = !reversed;
		    } else if (coords[cindex0+1] == coords[cindex3+1]) {
			if (coords[cindex0+2] > coords[cindex3+2]) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		if (flip) {
		    this.cindex3 = cindex0;
		    this.cindex0 = cindex3;
		} else {
		    this.cindex0 = cindex0;
		    this.cindex3 = cindex3;
		}
		this.cindex1 = -1;
		this.cindex2 = -1;
		this.reversed = reversed;
	    }

	    Edge(int type, Color color, Object tag,
		 int cindex0, int cindex1, int cindex2, int cindex3,
		 boolean reversed, int entryNumber, int edgeNumber)
	    {
		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		boolean flip = false;
		if (coords[cindex0] > coords[cindex3]) {
		    flip = true;
		    reversed = !reversed;
		} else if (coords[cindex0] == coords[cindex3]) {
		    if (coords[cindex0+1] > coords[cindex3+1]) {
			flip = true;
			reversed = !reversed;
		    } else if (coords[cindex0+1] == coords[cindex3+1]) {
			if (coords[cindex0+2] > coords[cindex3+2]) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		if (flip) {
		    this.cindex0 = cindex3;
		    this.cindex1 = cindex2;
		    this.cindex2 = cindex1;
		    this.cindex3 = cindex0;
		} else {
		    this.cindex0 = cindex0;
		    this.cindex1 = cindex1;
		    this.cindex2 = cindex2;
		    this.cindex3 = cindex3;
		}
		this.reversed = reversed;
	    }

	    public int match(Edge other) {
		double[] otherCoords = other.getCoords();
		for (int i = 0; i < 3; i++) {
		    if (coords[cindex0+i] != otherCoords[other.cindex0+i]) {
			return coords[cindex0+i] < otherCoords[other.cindex0+i]?
				-1: 1;
		    }
		}
		for (int i = 0; i < 3; i++) {
		    if (coords[cindex3+i] != otherCoords[other.cindex3+i]) {
			return coords[cindex3+i] < otherCoords[other.cindex3+i]?
				-1: 1;
		    }
		}
		if (cindex1 != -1 && other.cindex1 != -1) {
		    for (int i = 0; i < 3; i++) {
			if (coords[cindex1+i] != otherCoords[other.cindex1+i]) {
			    return coords[cindex1+i]
				< otherCoords[other.cindex1+i]?
				-1: 1;
			}
		    }
		    for (int i = 0; i < 3; i++) {
			if (coords[cindex2+i] != otherCoords[other.cindex2+i]) {
			    return coords[cindex2+i]
				< otherCoords[other.cindex2+i]?
				-1: 1;
			}
		    }
		} else if (cindex1 == -1) {
		    if (other.cindex1 != -1) {
			// same computation of intermediate control
			// points as used in Path3D.toCubic(...)
			double x1 = coords[cindex0]*2.0/3.0
			    + coords[cindex3]/3.0;
			double y1 = coords[cindex0+1]*2.0/3.0
			    + coords[cindex3+1]/3.0;
			double z1 = coords[cindex0+2]*2.0/3.0
			    + coords[cindex3+2]/3.0;
			double x2 = coords[cindex0]/3.0
			    + coords[cindex3]*2.0/3.0;
			double y2 = coords[cindex0+1]/3.0
			    + coords[cindex3+1]*2.0/3.0;
			double z2 = coords[cindex0+2]/3.0
			    + coords[cindex3+2]*2.0/3.0;
			// round to floats because the methods that add
			// segments do that.
			x1 = (float) x1; y1 = (float) y1; z1 = (float) z1;
			x2 = (float) x2; y2 = (float) y2; z2 = (float) z2;
			if (otherCoords[other.cindex1] != x1) {
			    return (otherCoords[other.cindex1] < x1)? -1: 1;
			} else if (otherCoords[other.cindex1+1] != y1) {
			    return (otherCoords[other.cindex1+1] < y1)? -1: 1;
			} else if (otherCoords[other.cindex1+2] != z1) {
			    return (otherCoords[other.cindex1+2] < z1)? -1: 1;
			} else if (otherCoords[other.cindex2] != x2) {
			    return (otherCoords[other.cindex2] < x2)? -1: 1;
			} else if (otherCoords[other.cindex2+1] != y2) {
			    return (otherCoords[other.cindex2+1] < y2)? -1: 1;
			}  else if (otherCoords[other.cindex2+2] != z2) {
			    return (otherCoords[other.cindex2+2] < z2)? -1: 1;
			}
		    }
		} else if (other.cindex1 == -1) {
		    // same computation of intermediate control
		    // points as used in Path3DInfo.toCubic(...)
		    double x1 = otherCoords[other.cindex0]*2.0/3.0
			+ otherCoords[other.cindex3]/3.0;
		    double y1 = otherCoords[other.cindex0+1]*2.0/3.0
			+ otherCoords[other.cindex3+1]/3.0;
		    double z1 = otherCoords[other.cindex0+2]*2.0/3.0
			+ otherCoords[other.cindex3+2]/3.0;
		    double x2 = otherCoords[other.cindex0]/3.0
			+ otherCoords[other.cindex3]*2.0/3.0;
		    double y2 = otherCoords[other.cindex0+1]/3.0
			+ otherCoords[other.cindex3+1]*2.0/3.0;
		    double z2 = otherCoords[other.cindex0+2]/3.0
			+ otherCoords[other.cindex3+2]*2.0/3.0;
		    // round to floats because the methods that add
		    // segments do that.
		    x1 = (float) x1; y1 = (float) y1; z1 = (float) z1;
		    x2 = (float) x2; y2 = (float) y2; z2 = (float) z2;
		    if (coords[cindex1] != x1) {
			return (coords[cindex1] < x1)? -1: 1;
		    } else if (coords[cindex1+1] != y1) {
			return (coords[cindex1+1] < y1)? -1: 1;
		    } else if (coords[cindex1+2] != z1) {
			return (coords[cindex1+2] < z1)? -1: 1;
		    } else if (coords[cindex2] != x2) {
			return (coords[cindex2] < x2)? -1: 1;
		    } else if (coords[cindex2+1] != y2) {
			return (coords[cindex2+1] < y2)? -1: 1;
		    } else if (coords[cindex2+2] != z2) {
			return (coords[cindex2+2] < z2)? -1: 1;
		    }
		}
		return 0;
	    }

	    @Override
	    public int compareTo (Edge other) {
		int m = match(other);
		if (m == 0) {
		    if (reversed == other.reversed) {
			if (entryNumber == other.entryNumber) {
			    if (edgeNumber == other.edgeNumber) {
				return 0;
			    } else {
				return (edgeNumber < other.edgeNumber)? -1: 1;
			    }
			} else {
			    return (entryNumber < other.entryNumber)? -1: 1;
			}
		    } else {
			return (reversed? -1: 1);
		    }
		} else {
		    return m;
		}
	    }

	    @Override
	    public boolean equals(Object object) {
		if (object instanceof Edge) {
		    Edge other = (Edge)object;
		    return (compareTo(other) == 0);
		} else {
		    return false;
		}
	    }

	    @Override
	    public int hashCode() {
		long tmp = (reversed? 0: 1) * 31;
		tmp =
		    java.lang.Double.doubleToLongBits(coords[cindex0]) * 31;
		tmp = (tmp ^
		       java.lang.Double.doubleToLongBits(coords[cindex0+1]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Double.doubleToLongBits(coords[cindex0+2]))
		    * 31;
		if (cindex1 >= 0) {
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex1+1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex1+2]))
			* 31;
		}
		if (cindex2 >= 0) {
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex2]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex2+1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Double.doubleToLongBits(coords[cindex2+2]))
			* 31;
		}
		tmp = (tmp ^
		       java.lang.Double.doubleToLongBits(coords[cindex3]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Double.doubleToLongBits(coords[cindex3+1]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Double.doubleToLongBits(coords[cindex3+2]))
		    * 31;
		
		return ((int) tmp) ^ ((int)(tmp >> 32));
	    }

	    public boolean oppositeFrom(Edge other) {
		return reversed != other.reversed;
	    }

	    public Object getTag() {return tag;}
	}


	/**
	 * Constructor for an oriented surface.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 */
	public Double() {
	    super();
	    coords = new double[INITIAL_SIZE * 48];
	};

	/**
	 * Constructor specifying if the surface has an orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param oriented true if the surface has an orientation; false
	 *        if it does not
	 */
	public Double(boolean oriented) {
	    super(oriented);
	    coords = new double[INITIAL_SIZE * 48];
	};

	/**
	 * Constructor for an oriented surface giving an estimate of
	 * the number of surface segments.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param initialCapacity the initial number of surface segments
	 *        allocated in an expandable table
	 */
	public Double(int initialCapacity) {
	    super(initialCapacity);
	    coords = new double[initialCapacity*48];
	}

	/**
	 * Constructor giving an estimate of the number of surface segments
	 * and specifying if the surface has an orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param initialCapacity the initial number of surface segments
	 *        allocated in an expandable table
	 * @param oriented true if the surface has an orientation; false
	 *        if it does not
	 */
	public Double(int initialCapacity, boolean oriented) {
	    super(initialCapacity, oriented);
	    coords = new double[initialCapacity*48];
	}

	static int estimateCapacity(Shape3D surface) {
	    if (surface instanceof Surface3D) {
		Surface3D s = (Surface3D) surface;
		return s.types.length;
	    } else {
		return INITIAL_SIZE;
	    }
	}

	static int estimateCapacities(Shape3D... surfaces) {
	    int sum = 0;
	    for (int i = 0; i < surfaces.length; i++) {
		sum += estimateCapacity(surfaces[i]);
	    }
	    return sum;
	}

	static boolean allOriented(Shape3D... surfaces) {
	    for (int i = 0; i < surfaces.length; i++) {
		if (!surfaces[i].isOriented()) return false;
	    }
	    return true;
	}

	/**
	 * Constructor using another surface to determine the initial
	 * surface segments and the surface's orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param surface the path whose segments will be copied
	 */
	public Double(Shape3D surface) {
	    this(estimateCapacity(surface), surface.isOriented());
	    append(surface.getSurfaceIterator(null));
	}

	/**
	 * Constructor using another surface to determine the surface's
	 * orientation and initial surface segments, modified with a 3D
	 * transform.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param surface the path whose segments will be copied
	 * @param transform the transform to apply to the surface
	 *        segments
	 */
	public Double(Shape3D surface, Transform3D transform) {
	    this(estimateCapacity(surface), surface.isOriented());
	    append(surface.getSurfaceIterator(transform));
	}

	/**
	 * Constructor for a surface that initially contains
	 * the surface segments from multiple surfaces or shapes.
	 * The surface is oriented if all shapes are oriented.
	 * @param surface1 the first surface
	 * @param surfaces the remaining surfaces
	 */
	public Double(Shape3D surface1, Shape3D... surfaces)
	{
	    this(estimateCapacity(surface1) + estimateCapacities(surfaces),
		 surface1.isOriented() && allOriented(surfaces));
	    append(surface1.getSurfaceIterator(null));
	    for (int i = 0; i < surfaces.length; i++) {
		append(surfaces[i].getSurfaceIterator(null));
	    }
	}

	void expandIfNeeded(int n) {
	    super.expandIfNeeded(n);
	    if (n == -1) return;
	    if ((cindex + n*3) < coords.length) {
		return;
	    }
	    int incr = cindex;
	    if (incr > MAX_INCR_48) incr = MAX_INCR_48;
	    int newsize = coords.length + incr;
	    double[] tmp = new double[newsize];
	    System.arraycopy(coords, 0, tmp, 0, cindex);
	    coords = tmp;
	}

	void copyCoords(int sInd, int dInd, int n) {
	    System.arraycopy(coords, sInd, coords, dInd, n);
	}

	static class Iterator1 extends Iterator {
	    double[] coords;
	    Iterator1(Surface3D.Double surface) {
		super(surface);
	        coords = surface.coords;
	    }
	    
	    @Override
	    public int currentSegment(float[] fc) {
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		for (int i = 0; i < len; i++) {
		    fc[i] = (float) coords[cindex+i];
		}
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		System.arraycopy(coords, cindex, dc, 0, len);
		return type;
	    }
	}

	static class Iterator2 extends Iterator {
	    double[] coords;
	    Transform3D transform;
	    Iterator2(Surface3D.Double surface, Transform3D transform) {
		super(surface);
	        coords = surface.coords;
		this.transform = transform;
	    }
	    
	    @Override
	    public int currentSegment(float[] fc) {
		// int type = surface.types[index];
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		transform.transform(coords, cindex, fc, 0, len/3);
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		// int type = surface.types[index];
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		transform.transform(coords, cindex, dc, 0, len/3);
		return type;
	    }
	}
	@Override
	public final synchronized void append(SurfaceIterator si)
	{
	    while (!si.isDone()) {
		switch (si.currentSegment(tmpCoords)) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    addPlanarTriangle(tmpCoords,
				      si.currentColor(),
				      si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    addCubicVertex(tmpCoords,
				   si.currentColor(),
				   si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    addCubicTriangle(tmpCoords,
				     si.currentColor(),
				     si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    addCubicPatch(tmpCoords,
				  si.currentColor(),
				  si.currentTag());
		    break;
		default:
		    break;
		}
		si.next();
	    }
	}

	@Override
	public Object clone() {
	    try {
		return super.clone();
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

	@Override
	public Surface3D createTransformedSurface(Transform3D transform) {
	    Surface3D.Double result = new Surface3D.Double();
	    result.append(getSurfaceIterator(transform));
	    return result;
	}

	@Override
	public final synchronized
	SurfaceIterator getSurfaceIterator(Transform3D tform) {
	    if (tform == null) {
		return new Iterator1(this);
	    } else {
		return new Iterator2(this, tform);
	    }
	}

	@Override
	public final synchronized
	SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	    if (tform == null) {
		if (level == 0) {
		    return new Iterator1(this);
		} else {
		    return new SubdivisionIterator(new Iterator1(this), level);
		}
	    } else if (tform instanceof AffineTransform3D) {
		if (level == 0) {
		    return new Iterator2(this, tform);
		} else {
		    return new SubdivisionIterator(new Iterator2(this, tform),
						   level);
		}
	    } else {
		if (level == 0) {
		    return new Iterator2(this, tform);
		} else {
		    return new SubdivisionIterator(new Iterator1(this), tform,
						   level);
		}
	    }
	}

	@Override
	public final synchronized
	void addCubicPatch(double[] controlPoints, Color color, Object tag)
	{
	    expandIfNeeded(16);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_PATCH;
	    // System.arraycopy(controlPoints,0, coords, cindex, 48);
	    for (int i = 0; i < 48; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    cindex += 48;
	}

	@Override
	public final synchronized void
	addFlippedCubicPatch(double[] controlPoints, Color color, Object tag)
	{
	    expandIfNeeded(16);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_PATCH;
	    // System.arraycopy(controlPoints, 0, coords, cindex, 48);
	    for (int i = 0; i < 48; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_PATCH, cindex);
	    cindex += 48;
	}

	@Override
	public final synchronized
	void addCubicTriangle(double[] controlPoints, Color color, Object tag)
	{
	    expandIfNeeded(10);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 30);
	    for (int i = 0; i < 30; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    cindex += 30;
	}

	@Override
	public final synchronized
	void addFlippedCubicTriangle(double[] controlPoints,
				     Color color, Object tag)
	{
	    expandIfNeeded(10);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 30);
	    for (int i = 0; i < 30; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_TRIANGLE, cindex);
	    cindex += 30;
	}


	@Override
	public final synchronized
	void addPlanarTriangle(double[] controlPoints, Color color, Object tag)
	{
	    expandIfNeeded(3);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.PLANAR_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 9);
	    for (int i = 0; i < 9; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    cindex += 9;
	}

	@Override
	public final synchronized
	void addFlippedPlanarTriangle(double[] controlPoints,
				      Color color, Object tag)
	{
	    expandIfNeeded(3);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.PLANAR_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 9);
	    for (int i = 0; i < 9; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.PLANAR_TRIANGLE, cindex);
	    cindex += 9;
	}

	@Override
	public final synchronized
	void addCubicVertex(double[] controlPoints, Color color, Object tag)
	{
	    expandIfNeeded(5);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_VERTEX;
	    // System.arraycopy(controlPoints,0, coords, cindex, 15);
	    for (int i = 0; i < 15; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    cindex += 15;
	}

	@Override
	public final synchronized
	void addFlippedCubicVertex(double[] controlPoints,
				   Color color, Object tag)
	{
	    expandIfNeeded(5);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_VERTEX;
	    // System.arraycopy(controlPoints,0, coords, cindex, 15);
	    for (int i = 0; i < 15; i++) {
		int ind = cindex + i;
		coords[ind] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_VERTEX, cindex);
	    cindex += 15;
	}

	@Override
	public final synchronized void transform(Transform3D tform) {
	    expandIfNeeded(1);
	    System.arraycopy(coords, 0, coords, 3, coords.length);
	    tform.transform(coords, 3, coords, 0, coords.length/3);
	}

	private double[] tmp = new double[3];

	private int reverseOrientation(int type, int ci) {
	    switch(type) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		System.arraycopy(coords, ci + 3, tmp, 0, 3);
		System.arraycopy(coords, ci + 6, coords, ci + 3, 3);
		System.arraycopy(tmp, 0, coords, ci + 6, 3);
		/*
		for (int i = 0; i < 9; i++) {
		    coords[ci+i] = (float) coords[ci+i];
		}
		*/
		ci += 9;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		System.arraycopy(coords, ci, tmp, 0, 3);
		System.arraycopy(coords, ci + 9, coords, ci, 3);
		System.arraycopy(tmp, 0, coords, ci+9, 3);
		System.arraycopy(coords, ci+3, tmp, 0, 3);
		System.arraycopy(coords, ci + 6, coords, ci+3, 3);
		System.arraycopy(tmp, 0, coords, ci+6, 3);
		ci += 15;
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		System.arraycopy(coords, ci + 3, tmp, 0, 3);
		System.arraycopy(coords, ci + 12, coords, ci + 3, 3);
		System.arraycopy(tmp, 0, coords, ci + 12, 3);
		System.arraycopy(coords, ci + 6, tmp, 0, 3);
		System.arraycopy(coords, ci + 21, coords, ci + 6, 3);
		System.arraycopy(tmp, 0, coords, ci + 21, 3);
		System.arraycopy(coords, ci + 9, tmp, 0, 3);
		System.arraycopy(coords, ci + 27, coords, ci + 9, 3);
		System.arraycopy(tmp, 0, coords, ci + 27, 3);
		System.arraycopy(coords, ci + 18, tmp, 0, 3);
		System.arraycopy(coords, ci + 24, coords, ci + 18, 3);
		System.arraycopy(tmp, 0, coords, ci + 24, 3);
		/*
		for (int i = 0; i < 30; i++) {
		    coords[ci+i] = (float) coords[ci+i];
		}
		*/
		ci += 30;
		break;
	    case SurfaceIterator.CUBIC_PATCH:
		for (int j = 0; j < 3; j++) {
		    MatrixOps.transpose(coords, 4, 4, ci, coords, 4, 4, ci,
					false, j, 3, j, 3);
		}
		/*
		for (int i = 0; i < 48; i++) {
		    coords[ci+i] = (float) coords[ci+i];
		}
		*/
		ci += 48;
		break;
	    }
	    return ci;
	}

	@Override
	public final synchronized void reverseOrientation() {
	    expandIfNeeded(-1);
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		ci = reverseOrientation(types[i], ci);
		/*
		switch(types[i]) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    System.arraycopy(coords, ci + 3, tmp, 0, 3);
		    System.arraycopy(coords, ci + 6, coords, ci + 3, 3);
		    System.arraycopy(tmp, 0, coords, ci + 6, 3);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    System.arraycopy(coords, ci + 3, tmp, 0, 3);
		    System.arraycopy(coords, ci + 12, coords, ci + 3, 3);
		    System.arraycopy(tmp, 0, coords, ci + 12, 3);
		    System.arraycopy(coords, ci + 6, tmp, 0, 3);
		    System.arraycopy(coords, ci + 21, coords, ci + 6, 3);
		    System.arraycopy(tmp, 0, coords, ci + 21, 3);
		    System.arraycopy(coords, ci + 9, tmp, 0, 3);
		    System.arraycopy(coords, ci + 27, coords, ci + 9, 3);
		    System.arraycopy(tmp, 0, coords, ci + 27, 3);
		    System.arraycopy(coords, ci + 18, tmp, 0, 3);
		    System.arraycopy(coords, ci + 24, coords, ci + 18, 3);
		    System.arraycopy(tmp, 0, coords, ci + 24, 3);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    // for (int j = 0; j < 4; j++) {
		    //  System.arraycopy(coords, ci + j*12, tmp, 0, 3);
		    //  System.arraycopy(coords, ci + j*12+9,
		    //  coords, ci + j*12, 3);
		    //  System.arraycopy(tmp, 0, coords, ci + j*12+9, 3);
		    //  System.arraycopy(coords, ci + j*12+3, tmp, 0, 3);
		    //  System.arraycopy(coords, ci + j*12+6,
		    //			 coords, ci + j*12+3, 3);
		    // System.arraycopy(tmp, 0, coords, ci + j*12+6, 3);
		    // }
		    for (int j = 0; j < 3; j++) {
			MatrixOps.transpose(coords, 4, 4, ci, coords, 4, 4, ci,
					    false, j, 3, j, 3);
		    }
		    ci += 48;
		    break;
		}
		*/
	    }
	}

	boolean hasEdgeLoop(int ind1, int ind2, int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return true;
		if (coords[ind1+i] != coords[ind3+i]) return true;

	    }
	    return false;
	}
	boolean identicalCP(int ind1, int ind2, int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return false;
		if (coords[ind1+i] != coords[ind3+i]) return false;
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    return true;
	}

	@Override
	public synchronized void computeBoundary(Appendable out,
						 boolean multipleEdges)
	{
	    if (boundaryComputed && lastMultipleEdges == multipleEdges) return;
	    wellFormed = true;
	    LinkedList<Edge> queue = new LinkedList<Edge>();
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		int type = types[i];
		Object tag = tags[i];
		Color color = colors[i];
		Edge e;
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    e = new Edge(type, color, tag, ci, ci + 6, false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci + 6, ci + 3, false,
				 i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci, ci + 3, true, i, 2);
		    queue.offer(e);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tag, out);
			}
		    }
		    e = new Edge(type, color, tag, ci, ci+3, ci+6, ci+9,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+9, ci+12,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+ 12, ci,
				 false, i, 2);
		    queue.offer(e);
		    ci += 15;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (identicalCP(ci, ci+4*3, ci+7*3, ci+9*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    if (identicalCP(ci+9*3, ci+8*3, ci+6*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 1, tags[i], out);
			}
		    }
		    if (identicalCP(ci, ci+1*3, ci+2*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 2, tags[i], out);
			}
		    }
		    e = new Edge(type, color, tag, ci, ci+4*3, ci+7*3, ci+9*3,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag,
				 ci+9*3, ci+8*3, ci+6*3, ci+3*3,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+0, ci+1*3, ci+2*3, ci+3*3,
				 true, i, 2);
		    queue.offer(e);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    boolean e1z = false;
		    boolean e2z = false;
		    boolean e3z = false;
		    boolean e4z = false;
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			e1z = true;
			if (hasEdgeLoop(ci, ci+3, ci+6, ci+9)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 0, tags[i], out);
			}
		    } else {
			e = new Edge(type, color, tag, ci, ci+3, ci+6, ci+9,
				     false, i, 0);
			queue.offer(e);
		    }
		    if (identicalCP(ci+9, ci+21, ci+33, ci+45)) {
			e2z = true;
			if (hasEdgeLoop(ci+9, ci+21, ci+33, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 1, tags[i], out);
			}
			if (e1z) {
			    e2z = true;
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, e2z, false, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, color, tag,
				     ci+9, ci+21, ci+33, ci+45,
				     false, i, 1);
			queue.offer(e);
		    }
		    if (identicalCP(ci+36, ci+39, ci+42, ci+45)) {
			e3z = true;
			if (hasEdgeLoop(ci+36, ci+39, ci+42, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 2, tags[i], out);
			}
			if (e2z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, false, e2z, e3z, false,
						     tags[i],
						     out);
			    }
			}
			e3z = true;
		    } else {
			e = new Edge(type, color, tag,
				     ci+36, ci+39, ci+42, ci+45,
				     true, i, 2);
			queue.offer(e);
		    }
		    if (identicalCP(ci, ci+12, ci+24, ci+36)) {
			e4z = true;
			if (hasEdgeLoop(ci, ci+12, ci+24, ci+36)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i, 3, tags[i], out);
			}
			if (e3z || e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, false, e3z, e4z,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, color, tag, ci, ci+12, ci+24, ci+36,
				     true, i, 3);
			queue.offer(e);
		    }
		    ci += 48;
		    break;
		}
	    }
	    Edge[] array = new Edge[queue.size()];
	    queue.toArray(array);
	    Arrays.sort(array);
	    /*
	    if (!oriented)
	    for (Edge e: array) {
		if (e.getCP1() != null) {
		    System.out.format("(%g,%g,%g), (%g,%g,%g), "
				      + "(%g,%g,%g),(%g,%g,%g) - %s\n",
				      e.getBasePoint().getX(),
				      e.getBasePoint().getY(),
				      e.getBasePoint().getZ(),
				      e.getCP1().getX(),
				      e.getCP1().getY(),
				      e.getCP1().getZ(),
				      e.getCP2().getX(),
				      e.getCP2().getY(),
				      e.getCP2().getZ(),
				      e.getFinalPoint().getX(),
				      e.getFinalPoint().getY(),
				      e.getFinalPoint().getZ(),
				      e.reversed);
		} else {
		    System.out.format("(%g,%g,%g), (%g,%g,%g) - %s\n ",
				      e.getBasePoint().getX(),
				      e.getBasePoint().getY(),
				      e.getBasePoint().getZ(),
				      e.getFinalPoint().getX(),
				      e.getFinalPoint().getY(),
				      e.getFinalPoint().getZ(),
				      e.reversed);
		}
	    }
	    */
	    int sz = Math.round(1.5F * queue.size()) + 1;
	    Map<Point3D,Edge> edgeMap = new HashMap<Point3D,Edge>(sz);
	    Map<Point3D,Edge> edgeMap2 = oriented? null:
		new HashMap<Point3D,Edge>(sz);
	    queue.clear();
	    int lower = 0;
	    int upper = 1;
	    while (lower < array.length) {
		boolean reversed = false;
		while (upper < array.length) {
		    if (array[lower].match(array[upper]) == 0) {
			reversed =
			    (array[lower].reversed != array[upper].reversed);
			upper++;
		    } else {
			break;
		    }
		}
		if (upper - lower == 1) {
		    Edge e = array[lower];
		    Point3D p = e.getBasePoint();
		    if (!oriented) {
			Point3D p1 = p;
			Point3D p2 = e.getFinalPoint();
			queue.offer(e);
			if (edgeMap.get(p1) == null) {
			    edgeMap.put(p1, e);
			} else {
			    if (edgeMap2.get(p1) == null) {
				edgeMap2.put(p1, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p1);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
			if (edgeMap.get(p2) == null) {
			    edgeMap.put(p2, e);
			} else {
			    if (edgeMap2.get(p2) == null) {
				edgeMap2.put(p2, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p2);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
		    } else if ((!multipleEdges) && edgeMap.containsKey(p)) {
			wellFormed = false;
			if (out != null) {
			    Edge ee = edgeMap.get(p);
			    try {
				out.append(String.format("bad boundary: "
							 + "edges for "
							 + "entry %d (edge %d) "
							 + "and "
							 + "entry %d (edge %d) "
							 + "share the same "
							 + "start point\n",
							 e.entryNumber,
							 e.edgeNumber,
							 ee.entryNumber,
							 ee.edgeNumber));
				out.append(String.format(" ... %s\n",
							 e.toString()));
				out.append(String.format(" ... %s\n",
							 ee.toString()));
				if (e.entryNumber < tags.length
				    && tags[e.entryNumber] != null) {
				    printTag(out, e.entryNumber,
					     tags[e.entryNumber]);
				}
				if (ee.entryNumber < tags.length
				    && tags[ee.entryNumber] != null) {
				    printTag(out, ee.entryNumber,
					     tags[ee.entryNumber]);
				}
			    } catch(IOException eio) {
				throw new RuntimeException(eio.getMessage(),
							   eio);
			    }
			}
		    } else {
			queue.offer(e);
			if (!multipleEdges) edgeMap.put(p,e);
		    }
		} else 	if (!((upper-lower) == 2 && (reversed || !oriented))) {
		    wellFormed = false;
		    if (out != null) {
			try {
			    out.append("edge conflict:\n");
			    for (int i = lower; i < upper; i++) {
				int eindex = array[i].entryNumber;
				out.append(array[i].toString());
				out.append("\n");
				/*
				out.append("    entry " + eindex
					   + " (edge " + array[i].edgeNumber
					   + " )");
				*/
				if (eindex < tags.length
				    && tags[eindex] != null) {
				    printTag(out, eindex, tags[eindex]);
				} else {
				    out.append("\n");
				}
			    }
			} catch (IOException eio) {
			    throw new RuntimeException (eio.getMessage(), eio);
			}
		    }
		}
		lower = upper;
		upper++;
	    }
	    if (wellFormed) {
		Edge e;
		Path3D path = new Path3D.Double();
		boundaryTags = new ArrayList<Object>();
		boundarySegments = new ArrayList<Integer>();
		edgeNumbers = new ArrayList<Integer>();
		while ((e = queue.poll()) != null) {
		    Point3D bp = e.getBasePoint();
		    if (multipleEdges || edgeMap.containsKey(bp)
			|| (!oriented && edgeMap2.containsKey(bp))) {
			path.moveTo(bp.getX(), bp.getY(), bp.getZ());
			Point3D cp1 = e.getCP1();
			Point3D cp2 = e.getCP2();
			Point3D fp = e.getFinalPoint();
			if (cp1 == null || cp2 == null) {
			    path.lineTo(fp.getX(), fp.getY(), fp.getZ());
			} else {
			    path.curveTo(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 fp.getX(), fp.getY(), fp.getZ());
			}
			boundaryTags.add(e.getTag());
			boundarySegments.add(e.entryNumber);
			edgeNumbers.add(e.edgeNumber);
			Edge n = null;
			if (oriented) {
			    n = edgeMap.remove(fp);
			} else if (!multipleEdges) {
			    if (e != edgeMap.get(fp)) {
				n = edgeMap.remove(fp);
			    } else {
				n  = edgeMap2.remove(fp);
			    }
			    if (n != null) {
				Point3D p1 = n.getBasePoint();
				Point3D p2 = n.getFinalPoint();
				if (edgeMap.get(p1) == n) {
				    edgeMap.remove(p1);
				}
				if (edgeMap.get(p2) == n) {
				    edgeMap.remove(p2);
				}
				if (edgeMap2.get(p1) == n) {
				    edgeMap2.remove(p1);
				}
				if (edgeMap2.get(p2) == n) {
				    edgeMap2.remove(p2);
				}
			    }
			}

			Point3D cp3 = fp;
			if (n != null) {
			    while (n != e) {
				if (n == null) break;
				if (oriented) {
				    cp1 = n.getCP1();
				    cp2 = n.getCP2();
				    cp3 = n.getFinalPoint();
				} else {
				    Point3D base = n.getBasePoint();
				    if (base.equals(cp3)) {
					cp1 = n.getCP1();
					cp2 = n.getCP2();
					cp3 = n.getFinalPoint();
				    } else {
					cp1 = n.getCP2();
					cp2 = n.getCP1();
					cp3 = n.getBasePoint();
				    }
				}
				if (cp1 == null || cp2 == null) {
				    path.lineTo(cp3.getX(),
						cp3.getY(),
						cp3.getZ());
				} else {
				    path.curveTo
					(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 cp3.getX(), cp3.getY(), cp3.getZ());
				}
				boundaryTags.add(n.getTag());
				boundarySegments.add(n.entryNumber);
				edgeNumbers.add(n.edgeNumber);

				if (oriented) {
				    n = edgeMap.remove(cp3);
				} else {
				    Edge next = edgeMap.get(cp3);
				    if (next != null && n != next) {
					n = edgeMap.remove(cp3);
				    } else {
					n = edgeMap2.remove(cp3);
				    }
				    if (n != null) {
					Point3D p1 = n.getBasePoint();
					Point3D p2 = n.getFinalPoint();
					if (n == edgeMap.get(p1)) {
					    edgeMap.remove(p1);
					}
					if (n == edgeMap.get(p2)) {
					    edgeMap.remove(p2);
					}
					if (n == edgeMap2.get(p1)) {
					    edgeMap2.remove(p1);
					}
					if (n == edgeMap2.get(p2)) {
					    edgeMap2.remove(p2);
					}
				    }
				}
			    }
			    if (n == e) path.closePath();
			}
			edgeMap.remove(bp);
		    }
		}
		boundary = path;
	    } else {
		boundary = null;
		boundaryColors = null;
		boundaryTags  = null;
		boundarySegments = null;
		edgeNumbers = null;
	    }
	    boundaryComputed = true;
	    lastMultipleEdges = multipleEdges;
	}

	static Edge findReverse(Edge[] array, Edge key, boolean oriented) {
	    int ind = Arrays.binarySearch(array, key);
	    if (ind < 0) return null;
	    ind++;
	    Edge result = (ind < array.length)? array[ind]: null;
	    if (result != null && key.match(result) == 0) {
		if (!oriented || (result.reversed != key.reversed)) {
		    return result;
		}
	    }
	    ind -= 2;
	    if (ind < 0) return null;
	    result = array[ind];
	    if (key.match(result) == 0) {
		if (!oriented || (result.reversed != key.reversed)) {
		    return result;
		}
	    }
	    return null;
	}

	synchronized void computeComponents(Appendable out) {
	    if (componentsComputed) return;
	    ArrayList<SurfaceComponent> cList = new ArrayList<>();
	    wellFormed = true;
	    LinkedList<Edge> queue = new LinkedList<Edge>();
	    LinkedList<Integer>oqueue = new LinkedList<>();
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		int type = types[i];
		Edge e;
		oqueue.offer(ci);
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    e = new Edge(type, null, null, ci, ci + 6, false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci + 6, ci + 3, false,
				 i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci, ci + 3, true, i, 2);
		    queue.offer(e);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    e = new Edge(type, null, null, ci, ci+3, ci+6, ci+9,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+9, ci+12,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+ 12, ci,
				 false, i, 2);
		    queue.offer(e);
		    ci += 15;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (identicalCP(ci, ci+4*3, ci+7*3, ci+9*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    if (identicalCP(ci+9*3, ci+8*3, ci+6*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 1, tags[i], out);
			}
		    }
		    if (identicalCP(ci, ci+1*3, ci+2*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 2, tags[i], out);
			}
		    }
		    e = new Edge(type, null, null, ci, ci+4*3, ci+7*3, ci+9*3,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null,
				 ci+9*3, ci+8*3, ci+6*3, ci+3*3,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+0, ci+1*3, ci+2*3, ci+3*3,
				 true, i, 2);
		    queue.offer(e);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    boolean e1z = false;
		    boolean e2z = false;
		    boolean e3z = false;
		    boolean e4z = false;
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			e1z = true;
			if (hasEdgeLoop(ci, ci+3, ci+6, ci+9)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,0,tags[i], out);
			}
		    } else {
			e = new Edge(type, null, null, ci, ci+3, ci+6, ci+9,
				     false, i, 0);
			queue.offer(e);
		    }
		    if (identicalCP(ci+9, ci+21, ci+33, ci+45)) {
			e2z = true;
			if (hasEdgeLoop(ci+9, ci+21, ci+33, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,1,tags[i], out);
			}
			if (e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, e2z, false, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null,
				     ci+9, ci+21, ci+33, ci+45,
				     false, i, 1);
			queue.offer(e);
		    }
		    if (identicalCP(ci+36, ci+39, ci+42, ci+45)) {
			e3z = true;
			if (hasEdgeLoop(ci+36, ci+39, ci+42, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,2,tags[i], out);
			}
			if (e2z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, false, e2z, e3z, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null,
				     ci+36, ci+39, ci+42, ci+45,
				     true, i, 2);
			queue.offer(e);
		    }
		    if (identicalCP(ci, ci+12, ci+24, ci+36)) {
			e4z = true;
			if (hasEdgeLoop(ci, ci+12, ci+24, ci+36)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,3,tags[i], out);
			}
			if (e3z || e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, false, e3z, e4z,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null, ci, ci+12, ci+24, ci+36,
				     true, i, 3);
			queue.offer(e);
		    }
		    ci += 48;
		    break;
		}
	    }
	    Edge[] array = new Edge[queue.size()];
	    Edge[] qarray = new Edge[queue.size()];

	    HashSet<Integer> cset = new HashSet<>(index);
	    queue.toArray(array);
	    Arrays.sort(array);
	    queue.toArray(qarray);

	    /*
	    System.out.println("array:");
	    int ii = 0;
	    for (Edge e: array) {
		System.out.println((ii++) +": " + e);
	    }
	    */

	    int[] offsets = new int[oqueue.size()];
	    int oind = 0;
	    for (Integer offset: oqueue) {
		offsets[oind++] = offset;
	    }

	    int lower = 0;
	    queue.clear();
	    while (lower < qarray.length) {
		Edge edge = qarray[lower];
		if (edge.used) {
		    lower++;
		    continue;
		}
		int entry = edge.entryNumber;
		// Edge next = findReverse(array, edge);
		// if (next != null) queue.offer(next);
		// edge.used = true;
		cset.add(entry);
		int upper = lower;
		while (upper < qarray.length) {
		    edge = qarray[upper];
		    if (edge.entryNumber == entry) {
			queue.offer(edge);
			edge.used = true;
			upper++;
		    } else {
			break;
		    }
		}
		lower = upper;
		while (!queue.isEmpty()) {
		    edge = queue.poll();
		    Edge next = findReverse(array, edge, oriented);
		    if (next == null  || next.used)  continue;
		    int li = Arrays.binarySearch(qarray, next,
						 new Comparator<Edge>() {
						     public int compare(Edge e1,
									Edge e2)
						     {
							 int n1 =
							     e1.entryNumber;
							 int n2 =
							     e2.entryNumber;
							 if (n1 < n2) return -1;
							 if (n1 > n2) return 1;
							 return 0;
						     }
						 });
		    if (li < 0) {
			// System.out.println("search for li failed");
			continue;
		    }
		    entry = next.entryNumber;
		    while (li > 0 && qarray[li-1].entryNumber == entry) li--;
		    int lu = li;
		    while (lu < qarray.length
			   && qarray[lu].entryNumber == entry) {
			lu++;
		    }
		    while (li < lu) {
			if (qarray[li].used) {
			    li++;
			} else {
			    qarray[li].used = true;
			    queue.offer(qarray[li]);
			    li++;
			}
		    }
		    cset.add(entry);
		}
		int indx =  0;
		int[] tindex = new int[cset.size()];
		int[] cindex = new int[cset.size()];
		for (Integer ind: cset) {
		    tindex[indx] = ind;
		    cindex[indx] = offsets[ind];
		    indx++;
		}
		cList.add(new SurfaceComponent(this, tindex, cindex));
		cset.clear();
	    }
	    components = new Shape3D[cList.size()];
	    cList.toArray(components);
	    componentsComputed = true;
	}
    }

    /**
     * Surface3D class that stores the coordinates of control
     * points as single-precision numbers.
     */
    public static class Float extends Surface3D {
	float coords[] = new float[INITIAL_SIZE * 48];

	@Override
	protected  final void
	    getSegment(int length, int offset, double[] scoords)
	{
	    for (int i = 0; i < length; i++) {
		scoords[i] = coords[offset + i];
	    }
	}


	class Edge implements Comparable<Edge> {
	    int entryNumber;
	    int edgeNumber;
	    int type;
	    Color color;
	    Object tag;
	    int cindex0;
	    int cindex1;
	    int cindex2;
	    int cindex3;
	    boolean reversed;
	    boolean used = false;

	    public String toString() {
		String typeString;
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    typeString = "planar-triangle";
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    typeString = "cubic-planar-triangle";
			break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    typeString = "cubic-triangle";
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    typeString = "cubic-patch";
		    break;
		default:
		    typeString = "<Unknown>";
		    break;
		}
		String fmt1 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)]";
		String fmt2 = "[%s entry %d, edge %d: (%g,%g,%g)->(%g,%g,%g)"
		    + "->(%g,%g,%g)->(%g,%g,%g)]";
		if (cindex1 == -1) {
		    if (reversed) {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2],
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2]);
		    } else {
			return String.format(fmt1, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2],
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2]);
		    }
		} else {
		    if (reversed) {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2],
					     coords[cindex2],
					     coords[cindex2+1],
					     coords[cindex2+2],
					     coords[cindex1],
					     coords[cindex1+1],
					     coords[cindex1+2],
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2]);
		    } else {
			return String.format(fmt2, typeString,
					     entryNumber, edgeNumber,
					     coords[cindex0],
					     coords[cindex0+1],
					     coords[cindex0+2],
					     coords[cindex1],
					     coords[cindex1+1],
					     coords[cindex1+2],
					     coords[cindex2],
					     coords[cindex2+1],
					     coords[cindex2+2],
					     coords[cindex3],
					     coords[cindex3+1],
					     coords[cindex3+2]);
		    }
		}
	    }

	    Point3D getBasePoint() {
		if (reversed) {
		    return new Point3D.Double((double)coords[cindex3],
					      (double)coords[cindex3+1],
					      (double)coords[cindex3+2]);
		} else {
		    return new Point3D.Double((double)coords[cindex0],
					      (double)coords[cindex0+1],
					      (double)coords[cindex0+2]);
		}
	    }

	    Point3D getFinalPoint() {
		if (reversed) {
		    return new Point3D.Double((double)coords[cindex0],
					      (double)coords[cindex0+1],
					      (double)coords[cindex0+2]);
		} else {
		    return new Point3D.Double((double)coords[cindex3],
					      (double)coords[cindex3+1],
					      (double)coords[cindex3+2]);
		}
	    }

	    Point3D getCP1() {
		if (cindex1 == -1) return null;
		if (reversed) {
		    return new Point3D.Double((double)coords[cindex2],
					      (double)coords[cindex2+1],
					      (double)coords[cindex2+2]);
		} else {
		    return new Point3D.Double((double)coords[cindex1],
					      (double)coords[cindex1+1],
					      (double)coords[cindex1+2]);
		}
	    }

	    Point3D getCP2() {
		if (cindex1 == -1) return null;
		if (reversed) {
		    return new Point3D.Double((double)coords[cindex1],
					      (double)coords[cindex1+1],
					      (double) coords[cindex1+2]);
		} else {
		    return new Point3D.Double((double)coords[cindex2],
					      (double)coords[cindex2+1],
					      (double)coords[cindex2+2]);
		}
	    }

	    float[] getCoords() {return coords;}

	    Edge(int type, Color color, Object tag,
		 int cindex0, int cindex3, boolean reversed,
		 int entryNumber, int edgeNumber)
	    {
		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		boolean flip = false;
		if (coords[cindex0] > coords[cindex3]) {
		    flip = true;
		    reversed = !reversed;
		} else if (coords[cindex0] == coords[cindex3]) {
		    if (coords[cindex0+1] > coords[cindex3+1]) {
			flip = true;
			reversed = !reversed;
		    } else if (coords[cindex0+1] == coords[cindex3+1]) {
			if (coords[cindex0+2] > coords[cindex3+2]) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		if (flip) {
		    this.cindex3 = cindex0;
		    this.cindex0 = cindex3;
		} else {
		    this.cindex0 = cindex0;
		    this.cindex3 = cindex3;
		}
		this.cindex1 = -1;
		this.cindex2 = -1;
		this.reversed = reversed;
	    }

	    Edge(int type, Color color, Object tag,
		 int cindex0, int cindex1, int cindex2, int cindex3,
		 boolean reversed, int entryNumber, int edgeNumber)
	    {
		this.entryNumber = entryNumber;
		this.edgeNumber = edgeNumber;
		this.color = color;
		this.tag = tag;
		boolean flip = false;
		if (coords[cindex0] > coords[cindex3]) {
		    flip = true;
		    reversed = !reversed;
		} else if (coords[cindex0] == coords[cindex3]) {
		    if (coords[cindex0+1] > coords[cindex3+1]) {
			flip = true;
			reversed = !reversed;
		    } else if (coords[cindex0+1] == coords[cindex3+1]) {
			if (coords[cindex0+2] > coords[cindex3+2]) {
			    flip = true;
			    reversed = !reversed;
			}
		    }
		}
		this.type = type;
		if (flip) {
		    this.cindex0 = cindex3;
		    this.cindex1 = cindex2;
		    this.cindex2 = cindex1;
		    this.cindex3 = cindex0;
		} else {
		    this.cindex0 = cindex0;
		    this.cindex1 = cindex1;
		    this.cindex2 = cindex2;
		    this.cindex3 = cindex3;
		}
		this.reversed = reversed;
		/*
		System.out.format("new edge: "
				  + "%g %g %g %g %g %g %g %g %g %g %g %g %s\n",
				  coords[cindex0],
				  coords[cindex0+1], coords[cindex0+2],
				  coords[cindex1],
				  coords[cindex1+1], coords[cindex1+2],
				  coords[cindex2],
				  coords[cindex2+1], coords[cindex2+2],
				  coords[cindex3],
				  coords[cindex3+1], coords[cindex3+2],
				  reversed);
		*/
	    }

	    public int match(Edge other) {
		float[] otherCoords = other.getCoords();
		for (int i = 0; i < 3; i++) {
		    if (coords[cindex0+i] != otherCoords[other.cindex0+i]) {
			return coords[cindex0+i] < otherCoords[other.cindex0+i]?
				-1: 1;
		    }
		}
		for (int i = 0; i < 3; i++) {
		    if (coords[cindex3+i] != otherCoords[other.cindex3+i]) {
			return coords[cindex3+i] < otherCoords[other.cindex3+i]?
				-1: 1;
		    }
		}
		if (cindex1 != -1 && other.cindex1 != -1) {
		    for (int i = 0; i < 3; i++) {
			if (coords[cindex1+i] != otherCoords[other.cindex1+i]) {
			    return coords[cindex1+i]
				< otherCoords[other.cindex1+i]?
				-1: 1;
			}
		    }
		    for (int i = 0; i < 3; i++) {
			if (coords[cindex2+i] != otherCoords[other.cindex2+i]) {
			    return coords[cindex2+i]
				< otherCoords[other.cindex2+i]?
				-1: 1;
			}
		    }
		} else if (cindex1 == -1) {
		    if (other.cindex1 != -1) {
			// same computation of intermediate control
			// points as used in Path3D.toCubic(...)
			double x1 = coords[cindex0]*2.0/3.0
			    + coords[cindex3]/3.0;
			double y1 = coords[cindex0+1]*2.0/3.0
			    + coords[cindex3+1]/3.0;
			double z1 = coords[cindex0+2]*2.0/3.0
			    + coords[cindex3+2]/3.0;
			double x2 = coords[cindex0]/3.0
			    + coords[cindex3]*2.0/3.0;
			double y2 = coords[cindex0+1]/3.0
			    + coords[cindex3+1]*2.0/3.0;
			double z2 = coords[cindex0+2]/3.0
			    + coords[cindex3+2]*2.0/3.0;
			// round to floats because the methods that add
			// segments do that.
			x1 = (float) x1; y1 = (float) y1; z1 = (float) z1;
			x2 = (float) x2; y2 = (float) y2; z2 = (float) z2;
			if (otherCoords[other.cindex1] != x1) {
			    return (otherCoords[other.cindex1] < x1)? -1: 1;
			} else if (otherCoords[other.cindex1+1] != y1) {
			    return (otherCoords[other.cindex1+1] < y1)? -1: 1;
			} else if (otherCoords[other.cindex1+2] != z1) {
			    return (otherCoords[other.cindex1+2] < z1)? -1: 1;
			} else if (otherCoords[other.cindex2] != x2) {
			    return (otherCoords[other.cindex2] < x2)? -1: 1;
			} else if (otherCoords[other.cindex2+1] != y2) {
			    return (otherCoords[other.cindex2+1] < y2)? -1: 1;
			}  else if (otherCoords[other.cindex2+2] != z2) {
			    return (otherCoords[other.cindex2+2] < z2)? -1: 1;
			}
		    }
		} else if (other.cindex1 == -1) {
		    // same computation of intermediate control
		    // points as used in Path3DInfo.toCubic(...)
		    double x1 = otherCoords[other.cindex0]*2.0/3.0
			+ otherCoords[other.cindex3]/3.0;
		    double y1 = otherCoords[other.cindex0+1]*2.0/3.0
			+ otherCoords[other.cindex3+1]/3.0;
		    double z1 = otherCoords[other.cindex0+2]*2.0/3.0
			+ otherCoords[other.cindex3+2]/3.0;
		    double x2 = otherCoords[other.cindex0]/3.0
			+ otherCoords[other.cindex3]*2.0/3.0;
		    double y2 = otherCoords[other.cindex0+1]/3.0
			+ otherCoords[other.cindex3+1]*2.0/3.0;
		    double z2 = otherCoords[other.cindex0+2]/3.0
			+ otherCoords[other.cindex3+2]*2.0/3.0;
		    // round to floats because the methods that add
		    // segments do that.
		    x1 = (float) x1; y1 = (float) y1; z1 = (float) z1;
		    x2 = (float) x2; y2 = (float) y2; z2 = (float) z2;
		    if (coords[cindex1] != x1) {
			return (coords[cindex1] < x1)? -1: 1;
		    } else if (coords[cindex1+1] != y1) {
			return (coords[cindex1+1] < y1)? -1: 1;
		    } else if (coords[cindex1+2] != z1) {
			return (coords[cindex1+2] < z1)? -1: 1;
		    } else if (coords[cindex2] != x2) {
			return (coords[cindex2] < x2)? -1: 1;
		    } else if (coords[cindex2+1] != y2) {
			return (coords[cindex2+1] < y2)? -1: 1;
		    } else if (coords[cindex2+2] != z2) {
			return (coords[cindex2+2] < z2)? -1: 1;
		    }
		}
		return 0;
	    }

	    @Override
	    public int compareTo (Edge other) {
		int m = match(other);
		if (m == 0) {
		    if (reversed == other.reversed) {
			if (entryNumber == other.entryNumber) {
			    if (edgeNumber == other.edgeNumber) {
				return 0;
			    } else {
				return (edgeNumber < other.edgeNumber)? -1: 1;
			    }
			} else {
			    return (entryNumber < other.entryNumber)? -1: 1;
			}
		    } else {
			return (reversed? -1: 1);
		    }
		} else {
		    return m;
		}
	    }

	    @Override
	    public boolean equals(Object object) {
		if (object instanceof Edge) {
		    Edge other = (Edge)object;
		    return (compareTo(other) == 0);
		} else {
		    return false;
		}
	    }

	    @Override
	    public int hashCode() {
		int tmp = (reversed? 0: 1) * 31;
		tmp =
		    java.lang.Float.floatToIntBits(coords[cindex0]) * 31;
		tmp = (tmp ^
		       java.lang.Float.floatToIntBits(coords[cindex0+1]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Float.floatToIntBits(coords[cindex0+2]))
		    * 31;
		if (cindex1 >= 0) {
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex1+1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex1+2]))
			* 31;
		}
		if (cindex2 >= 0) {
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex2]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex2+1]))
			* 31;
		    tmp = (tmp ^
			   java.lang.Float.floatToIntBits(coords[cindex2+2]))
			* 31;
		}
		tmp = (tmp ^
		       java.lang.Float.floatToIntBits(coords[cindex3]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Float.floatToIntBits(coords[cindex3+1]))
		    * 31;
		tmp = (tmp ^
		       java.lang.Float.floatToIntBits(coords[cindex3+2]));
		return tmp;
	    }

	    public boolean oppositeFrom(Edge other) {
		return reversed != other.reversed;
	    }

	    public Object getTag() {return tag;}
	}


	/**
	 * Constructor for an oriented surface.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 */
	public Float() {
	    super();
	    coords = new float[INITIAL_SIZE * 48];
	};

	/**
	 * Constructor specifying if the surface has an orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param oriented true if the surface has an orientation; false
	 *        if it does not
	 */
	public Float(boolean oriented) {
	    super(oriented);
	    coords = new float[INITIAL_SIZE * 48];
	};

	/**
	 * Constructor for an oriented surface  giving an estimate of
	 * the number of surface segments.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param initialCapacity the initial number of surface segments
	 *        allocated in an expandable table
	 */
	public Float(int initialCapacity) {
	    super(initialCapacity);
	    coords = new float[initialCapacity*48];
	}

	/**
	 * Constructor giving an estimate of the number of surface segments
	 * and specifying if the surface has an orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param initialCapacity the initial number of surface segments
	 *        allocated in an expandable table
	 * @param oriented true if the surface has an orientation; false
	 *        if it does not
	 */
	public Float(int initialCapacity, boolean oriented) {
	    super(initialCapacity, oriented);
	    coords = new float[initialCapacity*48];
	}

	static int estimateCapacity(Shape3D surface) {
	    if (surface instanceof Surface3D) {
		Surface3D s = (Surface3D) surface;
		return s.types.length;
	    } else {
		return INITIAL_SIZE;
	    }
	}

	static int estimateCapacities(Shape3D... surfaces) {
	    int sum = 0;
	    for (int i = 0; i < surfaces.length; i++) {
		sum += estimateCapacity(surfaces[i]);
	    }
	    return sum;
	}

	static boolean allOriented(Shape3D... surfaces) {
	    for (int i = 0; i < surfaces.length; i++) {
		if (!surfaces[i].isOriented()) return false;
	    }
	    return true;
	}

	/**
	 * Constructor using another surface to determine the initial
	 * surface segments and the surface's orientation.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param surface the path whose segments will be copied
	 */
	public Float(Shape3D surface) {
	    this(estimateCapacity(surface));
	    append(surface.getSurfaceIterator(null));
	}

	/**
	 * Constructor using another surface to determine the surface's
	 * orientation and its initial
	 * path segments, modified with a 3D transform.
	 * A surface is oriented if it has two sides. By convention,
	 * for each of the patches and triangles that constitute a
	 * surface, the normal vector points in the direction implied
	 * by the right hand rule when traversing a patch's or triangle's
	 * vertices.  By convention, for closed surfaces the normal
	 * vector points towards the outside of the surface.
	 * @param surface the path whose segments will be copied
	 * @param transform the transform to apply to the surface
	 *        segments
	 */
	public Float(Shape3D surface, Transform3D transform) {
	    this(estimateCapacity(surface), surface.isOriented());
	    append(surface.getSurfaceIterator(transform));
	}

	/**
	 * Constructor for a surface that initially contains
	 * the surface segments from multiple surfaces or shapes.
	 * The surface is oriented if all shapes are oriented.
	 * @param surface1 the first surface
	 * @param surfaces the remaining surfaces
	 */
	public Float(Shape3D surface1, Shape3D... surfaces)
	{
	    this(estimateCapacity(surface1) + estimateCapacities(surfaces),
		 surface1.isOriented() && allOriented(surfaces));
	    append(surface1.getSurfaceIterator(null));
	    for (int i = 0; i < surfaces.length; i++) {
		append(surfaces[i].getSurfaceIterator(null));
	    }
	}

	void expandIfNeeded(int n) {
	    super.expandIfNeeded(n);
	    if (n == -1) return;
	    if ((cindex + n*3) < coords.length) {
		return;
	    }
	    int incr = cindex;
	    if (incr > MAX_INCR_48) incr = MAX_INCR_48;
	    int newsize = coords.length + incr;
	    float[] tmp = new float[newsize];
	    System.arraycopy(coords, 0, tmp, 0, cindex);
	    coords = tmp;
	}

	void copyCoords(int sInd, int dInd, int n) {
	    System.arraycopy(coords, sInd, coords, dInd, n);
	}

	static class Iterator1 extends Iterator {
	    float[] coords;
	    Iterator1(Surface3D.Float surface) {
		super(surface);
	        coords = surface.coords;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		for (int i = 0; i < len; i++) {
		    dc[i] = (double) coords[cindex+i];
		}
		return type;
	    }

	    @Override
	    public int currentSegment(float[] fc) {
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		System.arraycopy(coords, cindex, fc, 0, len);
		return type;
	    }
	}

	static class Iterator2 extends Iterator {
	    float[] coords;
	    Transform3D transform;
	    Iterator2(Surface3D.Float surface, Transform3D transform) {
		super(surface);
	        coords = surface.coords;
		this.transform = transform;
	    }

	    @Override
	    public int currentSegment(float[] fc) {
		// int type = surface.types[index];
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		transform.transform(coords, cindex, fc, 0, len/3);
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		// int type = surface.types[index];
		int type = surfaceTypes[index];
		int len = numbCoords(type);
		transform.transform(coords, cindex, dc, 0, len/3);
		return type;
	    }
	}

	@Override
	public final synchronized void append(SurfaceIterator si)
	{
	    while (!si.isDone()) {
		switch (si.currentSegment(tmpCoords)) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    addPlanarTriangle(tmpCoords,
				      si.currentColor(),
				      si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    addCubicVertex(tmpCoords,
				   si.currentColor(),
				   si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    addCubicTriangle(tmpCoords,
				     si.currentColor(),
				     si.currentTag());
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    addCubicPatch(tmpCoords,
				  si.currentColor(),
				  si.currentTag());
		    break;
		default:
		    break;
		}
		si.next();
	    }
	}

	@Override
	public Object clone() {
	    try {
		return super.clone();
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

	@Override
	public Surface3D createTransformedSurface(Transform3D transform) {
	    Surface3D.Float result = new Surface3D.Float();
	    result.append(getSurfaceIterator(transform));
	    return result;
	}

	@Override
	public final synchronized
	SurfaceIterator getSurfaceIterator(Transform3D tform) {
	    if (tform == null) {
		return new Iterator1(this);
	    } else {
		return new Iterator2(this, tform);
	    }
	}

	@Override
	public final synchronized
	SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	    if (tform == null) {
		if (level == 0) {
		    return new Iterator1(this);
		} else {
		    return new SubdivisionIterator(new Iterator1(this), level);
		}
	    } else if (tform instanceof AffineTransform3D) {
		if (level == 0) {
		    return new Iterator2(this, tform);
		} else {
		    return new SubdivisionIterator(new Iterator2(this, tform),
						   level);
		}
	    } else {
		if (level == 0) {
		    return new Iterator2(this, tform);
		} else {
		    return new SubdivisionIterator(new Iterator1(this), tform,
						   level);
		}
	    }
	}

	/*
	@Override
	public final synchronized void addCubicPatch(double[] controlPoints) {
	    addCubicPatch(controlPoints, null);
	}
	*/

	@Override
	public final synchronized void addCubicPatch(double[] controlPoints,
						     Color color,
						     Object tag)
	{
	    expandIfNeeded(16);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_PATCH;
	    // System.arraycopy(controlPoints,0, coords, cindex, 48);
	    for (int i = 0; i < 48; i++) {
		coords[cindex++] = (float)(controlPoints[i]);
	    }
	}

	@Override
	public final synchronized
	    void addFlippedCubicPatch(double[] controlPoints,
				      Color color,
				      Object tag)
	{
	    expandIfNeeded(16);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_PATCH;
	    // System.arraycopy(controlPoints,0, coords, cindex, 48);
	    int cindexSaved = cindex;
	    for (int i = 0; i < 48; i++) {
		coords[cindex++] = (float)(controlPoints[i]);
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_PATCH, cindexSaved);
	}

	@Override
	public final synchronized
	    void addCubicTriangle(double[] controlPoints)
	{
	    addCubicTriangle(controlPoints, null);
	}

	@Override
	public final synchronized
	    void addCubicTriangle(double[] controlPoints,
				  Color color,
				  Object tag)
	{
	    expandIfNeeded(10);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 30);
	    for (int i = 0; i < 30; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	}

	@Override
	public final synchronized
	    void addFlippedCubicTriangle(double[] controlPoints,
					 Color color,
					 Object tag)
	{
	    expandIfNeeded(10);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 30);
	    int cindexSaved = cindex;
	    for (int i = 0; i < 30; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_TRIANGLE, cindexSaved);
	}

	@Override
	public final synchronized
	    void addPlanarTriangle(double[] controlPoints)
	{
	    addPlanarTriangle(controlPoints, null);
	}

	@Override
	public final synchronized
	    void addPlanarTriangle(double[] controlPoints,
				   Color color,
				   Object tag)
	{
	    expandIfNeeded(3);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.PLANAR_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 9);
	    for (int i = 0; i < 9; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	}

	@Override
	public final synchronized
	    void addFlippedPlanarTriangle(double[] controlPoints,
					  Color color,
					  Object tag)
	{
	    expandIfNeeded(3);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.PLANAR_TRIANGLE;
	    // System.arraycopy(controlPoints,0, coords, cindex, 9);
	    int cindexSaved = cindex;
	    for (int i = 0; i < 9; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.PLANAR_TRIANGLE,
			       cindexSaved);
	}

	@Override
	public final synchronized
	    void addCubicVertex(double[] controlPoints,
				Color color,
				Object tag)
	{
	    expandIfNeeded(5);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_VERTEX;
	    // System.arraycopy(controlPoints,0, coords, cindex, 15);
	    for (int i = 0; i < 15; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	}

	@Override
	public final synchronized
	    void addFlippedCubicVertex(double[] controlPoints,
				       Color color,
				       Object tag)
	{
	    expandIfNeeded(5);
	    colors[index] = color;
	    if (tag == null && stackTraceMode) {
		tags[index] = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		tags[index] = tag;
	    }
	    cindices[index] = cindex;
	    types[index++] = SurfaceIterator.CUBIC_VERTEX;
	    // System.arraycopy(controlPoints,0, coords, cindex, 15);
	    int cindexSaved = cindex;
	    for (int i = 0; i < 15; i++) {
		coords[cindex++] = (float)controlPoints[i];
	    }
	    reverseOrientation(SurfaceIterator.CUBIC_VERTEX,
			       cindexSaved);
	}

	@Override
	public final synchronized void transform(Transform3D tform) {
	    expandIfNeeded(1);
	    System.arraycopy(coords, 0, coords, 3, coords.length);
	    tform.transform(coords, 3, coords, 0, coords.length/3);
	}

	private float[] tmp = new float[3];

	private int reverseOrientation(int type, int ci) {
	    switch(type) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		System.arraycopy(coords, ci + 3, tmp, 0, 3);
		System.arraycopy(coords, ci + 6, coords, ci + 3, 3);
		System.arraycopy(tmp, 0, coords, ci + 6, 3);
		ci += 9;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		System.arraycopy(coords, ci, tmp, 0, 3);
		System.arraycopy(coords, ci + 9, coords, ci, 3);
		System.arraycopy(tmp, 0, coords, ci+0, 3);
		System.arraycopy(coords, ci+3, tmp, 0, 3);
		System.arraycopy(coords, ci + 6, coords, ci+3, 3);
		System.arraycopy(tmp, 0, coords, ci+6, 3);
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		System.arraycopy(coords, ci + 3, tmp, 0, 3);
		System.arraycopy(coords, ci + 12, coords, ci + 3, 3);
		System.arraycopy(tmp, 0, coords, ci + 12, 3);
		System.arraycopy(coords, ci + 6, tmp, 0, 3);
		System.arraycopy(coords, ci + 21, coords, ci + 6, 3);
		System.arraycopy(tmp, 0, coords, ci + 21, 3);
		System.arraycopy(coords, ci + 9, tmp, 0, 3);
		System.arraycopy(coords, ci + 27, coords, ci + 9, 3);
		System.arraycopy(tmp, 0, coords, ci + 27, 3);
		System.arraycopy(coords, ci + 18, tmp, 0, 3);
		System.arraycopy(coords, ci + 24, coords, ci + 18, 3);
		System.arraycopy(tmp, 0, coords, ci + 24, 3);
		ci += 30;
		break;
	    case SurfaceIterator.CUBIC_PATCH:
		for (int j = 0; j < 3; j++) {
		    MatrixOps.transpose(coords, 4, 4, ci, coords, 4, 4, ci,
					false, j, 3, j, 3);
		}
		ci += 48;
		break;
	    }
	    return ci;
	}

	@Override
	public final synchronized void reverseOrientation() {
	    expandIfNeeded(-1);
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		ci = reverseOrientation(types[i], ci);
		/*
		switch(types[i]) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    System.arraycopy(coords, ci + 3, tmp, 0, 3);
		    System.arraycopy(coords, ci + 6, coords, ci + 3, 3);
		    System.arraycopy(tmp, 0, coords, ci + 6, 3);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    System.arraycopy(coords, ci + 3, tmp, 0, 3);
		    System.arraycopy(coords, ci + 12, coords, ci + 3, 3);
		    System.arraycopy(tmp, 0, coords, ci + 12, 3);
		    System.arraycopy(coords, ci + 6, tmp, 0, 3);
		    System.arraycopy(coords, ci + 21, coords, ci + 6, 3);
		    System.arraycopy(tmp, 0, coords, ci + 21, 3);
		    System.arraycopy(coords, ci + 9, tmp, 0, 3);
		    System.arraycopy(coords, ci + 27, coords, ci + 9, 3);
		    System.arraycopy(tmp, 0, coords, ci + 27, 3);
		    System.arraycopy(coords, ci + 18, tmp, 0, 3);
		    System.arraycopy(coords, ci + 24, coords, ci + 18, 3);
		    System.arraycopy(tmp, 0, coords, ci + 24, 3);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    for (int j = 0; j < 4; j++) {
			System.arraycopy(coords, ci + j*12, tmp, 0, 3);
			System.arraycopy(coords, ci + j*12+9,
					 coords, ci + j*12, 3);
			System.arraycopy(tmp, 0, coords, ci + j*12+9, 3);
			System.arraycopy(coords, ci + j*12+3, tmp, 0, 3);
			System.arraycopy(coords, ci + j*12+6,
					 coords, ci + j*12+3, 3);
			System.arraycopy(tmp, 0, coords, ci + j*12+6, 3);
		    }
		    ci += 48;
		    break;
		}
		*/
	    }
	}

	boolean hasEdgeLoop(int ind1, int ind2, int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return true;
		if (coords[ind1+i] != coords[ind3+i]) return true;

	    }
	    return false;
	}
	boolean identicalCP(int ind1, int ind2, int ind3, int ind4) {
	    for (int i = 0; i < 3; i++) {
		if (coords[ind1+i] != coords[ind2+i]) return false;
		if (coords[ind1+i] != coords[ind3+i]) return false;
		if (coords[ind1+i] != coords[ind4+i]) return false;
	    }
	    return true;
	}

	@Override
	public synchronized void computeBoundary(Appendable out,
						 boolean multipleEdges)
	{
	    if (boundaryComputed && multipleEdges == lastMultipleEdges) return;
	    wellFormed = true;
	    LinkedList<Edge> queue = new LinkedList<Edge>();
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		int type = types[i];
		Object tag = tags[i];
		Color color = colors[i];
		Edge e;
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    e = new Edge(type, color, tag, ci, ci + 6, false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci + 6, ci + 3, false,
				 i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci, ci + 3, true, i, 2);
		    queue.offer(e);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tag, out);
			}
		    }
		    e = new Edge(type, color, tag, ci, ci+3, ci+6, ci+9,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+9, ci+12,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+ 12, ci,
				 false, i, 2);
		    queue.offer(e);
		    ci += 15;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (identicalCP(ci, ci+4*3, ci+7*3, ci+9*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    if (identicalCP(ci+9*3, ci+8*3, ci+6*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 1, tags[i], out);
			}
		    }
		    if (identicalCP(ci, ci+1*3, ci+2*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 2, tags[i], out);
			}
		    }
		    e = new Edge(type, color, tag, ci, ci+4*3, ci+7*3, ci+9*3,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, color, tag,
				 ci+9*3, ci+8*3, ci+6*3, ci+3*3,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, color, tag, ci+0, ci+1*3, ci+2*3, ci+3*3,
				 true, i, 2);
		    queue.offer(e);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    boolean e1z = false;
		    boolean e2z = false;
		    boolean e3z = false;
		    boolean e4z = false;
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			e1z = true;
			if (hasEdgeLoop(ci, ci+3, ci+6, ci+9)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,0,tags[i], out);
			}
		    } else {
			e = new Edge(type, color, tag, ci, ci+3, ci+6, ci+9,
				     false, i, 0);
			queue.offer(e);
		    }
		    if (identicalCP(ci+9, ci+21, ci+33, ci+45)) {
			e2z = true;
			if (hasEdgeLoop(ci+9, ci+21, ci+33, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,1,tags[i], out);
			}
			if (e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, e2z, false, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, color, tag,
				     ci+9, ci+21, ci+33, ci+45,
				     false, i, 1);
			queue.offer(e);
		    }
		    if (identicalCP(ci+36, ci+39, ci+42, ci+45)) {
			e3z = true;
			if (hasEdgeLoop(ci+36, ci+39, ci+42, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,2,tags[i], out);
			}
			if (e2z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, false, e2z, e3z, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, color, tag,
				     ci+36, ci+39, ci+42, ci+45,
				     true, i, 2);
			queue.offer(e);
		    }
		    if (identicalCP(ci, ci+12, ci+24, ci+36)) {
			e4z = true;
			if (hasEdgeLoop(ci, ci+12, ci+24, ci+36)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,3,tags[i], out);
			}
			if (e3z || e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, false, e3z, e4z,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, color, tag, ci, ci+12, ci+24, ci+36,
				     true, i, 3);
			queue.offer(e);
		    }
		    ci += 48;
		    break;
		}
	    }
	    Edge[] array = new Edge[queue.size()];
	    queue.toArray(array);
	    Arrays.sort(array);
	    int sz = Math.round(1.5F * queue.size()) + 1;
	    Map<Point3D,Edge> edgeMap = new HashMap<Point3D,Edge>(sz);
	    Map<Point3D,Edge> edgeMap2 = oriented? null:
		new HashMap<Point3D,Edge>(sz);
	    queue.clear();
	    int lower = 0;
	    int upper = 1;
	    while (lower < array.length) {
		boolean reversed = false;
		while (upper < array.length) {
		    if (array[lower].match(array[upper]) == 0) {
			reversed =
			    (array[lower].reversed != array[upper].reversed);
			upper++;
		    } else {
			break;
		    }
		}
		if (upper - lower == 1) {
		    Edge e = array[lower];
		    Point3D p = e.getBasePoint();
		    if (!oriented) {
			Point3D p1 = p;
			Point3D p2 = e.getFinalPoint();
			queue.offer(e);
			if (edgeMap.get(p1) == null) {
			    edgeMap.put(p1, e);
			} else {
			    if (edgeMap2.get(p1) == null) {
				edgeMap2.put(p1, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p1);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
			if (edgeMap.get(p2) == null) {
			    edgeMap.put(p2, e);
			} else {
			    if (edgeMap2.get(p2) == null) {
				edgeMap2.put(p2, e);
			    } else {
				if (out != null) {
				    try {
					out.append("existing edges for " + p2);
				    } catch (IOException eio) {}
				}
				wellFormed = false;
			    }
			}
		    } else if ((!multipleEdges) && edgeMap.containsKey(p)) {
			wellFormed = false;
			if (out != null) {
			    Edge ee = edgeMap.get(p);
			    try {
				out.append(String.format("bad boundary: "
							 + "edges for "
							 + "entry %d (edge %d) "
							 + "and "
						     + "entry %d (edge %d) "
						     + "share the same "
						     + "start point\n",
						     e.entryNumber,
						     e.edgeNumber,
						     ee.entryNumber,
						     ee.edgeNumber));
				out.append(String.format(" ... %s\n",
							 e.toString()));
				out.append(String.format(" ... %s\n",
							 ee.toString()));
				if (e.entryNumber < tags.length
				    && tags[e.entryNumber] != null) {
				    printTag(out, e.entryNumber,
					     tags[e.entryNumber]);
				}
				if (ee.entryNumber < tags.length
				    && tags[ee.entryNumber] != null) {
				    printTag(out, ee.entryNumber,
					     tags[ee.entryNumber]);
				}
			    } catch(IOException eio) {
				throw new RuntimeException (eio.getMessage(),
							    eio);
			    }
			}
		    } else {
			queue.offer(e);
			if (!multipleEdges) edgeMap.put(p,e);
		    }
		} else 	if (!((upper-lower) == 2 && (reversed || !oriented))) {
		    wellFormed = false;
		    if (out != null) {
			try {
			    out.append("edge conflict:\n");
			    for (int i = lower; i < upper; i++) {
				int eindex = array[i].entryNumber;
				out.append(array[i].toString());
				out.append("\n");
				if (eindex < tags.length
				    && tags[eindex] != null) {
				    printTag(out, eindex, tags[eindex]);
				} else {
				    out.append("\n");
				}
			    }
			} catch (IOException eio) {
			    throw new RuntimeException (eio.getMessage(), eio);
			}
		    }
		}
		lower = upper;
		upper++;
	    }
	    if (wellFormed) {
		Edge e;
		Path3D path = new Path3D.Float();
		boundaryColors = new ArrayList<Color>();
		boundaryTags = new ArrayList<Object>();
		boundarySegments = new ArrayList<Integer>();
		edgeNumbers = new ArrayList<Integer>();
		while ((e = queue.poll()) != null) {
		    Point3D bp = e.getBasePoint();
		    if (edgeMap.containsKey(bp)
			|| (!oriented && edgeMap2.containsKey(bp))) {
			path.moveTo(bp.getX(), bp.getY(), bp.getZ());
			Point3D cp1 = e.getCP1();
			Point3D cp2 = e.getCP2();
			Point3D fp = e.getFinalPoint();
			if (cp1 == null || cp2 == null) {
			    path.lineTo(fp.getX(), fp.getY(), fp.getZ());
			} else {
			    path.curveTo(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 fp.getX(), fp.getY(), fp.getZ());
			}
			boundaryColors.add(e.color);
			boundaryTags.add(e.getTag());
			boundarySegments.add(e.entryNumber);
			edgeNumbers.add(e.edgeNumber);
			Edge n = null;
			if (oriented) {
			    n = edgeMap.remove(fp);
			} else if (!multipleEdges) {
			    if (e != edgeMap.get(fp)) {
				n = edgeMap.remove(fp);
			    } else {
				n  = edgeMap2.remove(fp);
			    }
			    if (n != null) {
				Point3D p1 = n.getBasePoint();
				Point3D p2 = n.getFinalPoint();
				if (edgeMap.get(p1) == n) {
				    edgeMap.remove(p1);
				}
				if (edgeMap.get(p2) == n) {
				    edgeMap.remove(p2);
				}
				if (edgeMap2.get(p1) == n) {
				    edgeMap2.remove(p1);
				}
				if (edgeMap2.get(p2) == n) {
				    edgeMap2.remove(p2);
				}
			    }
			}

			Point3D cp3 = fp;
			if (n != null) {
			    while (n != e) {
				if (n == null) break;
				if (oriented) {
				    cp1 = n.getCP1();
				    cp2 = n.getCP2();
				    cp3 = n.getFinalPoint();
				} else {
				    Point3D base = n.getBasePoint();
				    if (base.equals(cp3)) {
					cp1 = n.getCP1();
					cp2 = n.getCP2();
					cp3 = n.getFinalPoint();
				    } else {
					cp1 = n.getCP2();
					cp2 = n.getCP1();
					cp3 = n.getBasePoint();
				    }
				}
				if (cp1 == null || cp2 == null) {
				    path.lineTo(cp3.getX(),
						cp3.getY(),
						cp3.getZ());
				} else {
				    path.curveTo
					(cp1.getX(), cp1.getY(), cp1.getZ(),
					 cp2.getX(), cp2.getY(), cp2.getZ(),
					 cp3.getX(), cp3.getY(), cp3.getZ());
				}
				boundaryTags.add(e.color);
				boundaryTags.add(e.getTag());
				boundarySegments.add(e.entryNumber);
				edgeNumbers.add(n.edgeNumber);
				if (oriented) {
				    n = edgeMap.remove(cp3);
				} else {
				    Edge next = edgeMap.get(cp3);
				    if (next != null && n != next) {
					n = edgeMap.remove(cp3);
				    } else {
					n = edgeMap2.remove(cp3);
				    }
				    if (n != null) {
					Point3D p1 = n.getBasePoint();
					Point3D p2 = n.getFinalPoint();
					if (n == edgeMap.get(p1)) {
					    edgeMap.remove(p1);
					}
					if (n == edgeMap.get(p2)) {
					    edgeMap.remove(p2);
					}
					if (n == edgeMap2.get(p1)) {
					    edgeMap2.remove(p1);
					}
					if (n == edgeMap2.get(p2)) {
					    edgeMap2.remove(p2);
					}
				    }
				}
			    }
			    if (n == e) path.closePath();
			}
			edgeMap.remove(bp);
		    }
		}
		boundary = path;
	    } else {
		boundary = null;
		boundaryColors = null;
		boundaryTags  = null;
		boundarySegments = null;
		edgeNumbers = null;
	    }
	    boundaryComputed = true;
	    lastMultipleEdges = multipleEdges;
	}

	static Edge findReverse(Edge[] array, Edge key, boolean oriented) {
	    int ind = Arrays.binarySearch(array, key);
	    if (ind < 0) return null;
	    ind++;
	    Edge result = (ind < array.length)? array[ind]: null;
	    if (result != null && key.match(result) == 0) {
		if (!oriented || (result.reversed != key.reversed)) {
		    return result;
		}
	    }
	    ind -= 2;
	    if (ind < 0) return null;
	    result = array[ind];
	    if (key.match(result) == 0) {
		if (!oriented || (result.reversed != key.reversed)) {
		    return result;
		}
	    }
	    return null;
	}

	synchronized void computeComponents(Appendable out) {
	    if (componentsComputed) return;
	    ArrayList<SurfaceComponent> cList = new ArrayList<>();
	    wellFormed = true;
	    LinkedList<Edge> queue = new LinkedList<Edge>();
	    LinkedList<Integer>oqueue = new LinkedList<>();
	    int ci = 0;
	    for (int i = 0; i < index; i++) {
		int type = types[i];
		Edge e;
		oqueue.offer(ci);
		switch(type) {
		case SurfaceIterator.PLANAR_TRIANGLE:
		    e = new Edge(type, null, null, ci, ci + 6, false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci + 6, ci + 3, false,
				 i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci, ci + 3, true, i, 2);
		    queue.offer(e);
		    ci += 9;
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    e = new Edge(type, null, null, ci, ci+3, ci+6, ci+9,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+9, ci+12,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+ 12, ci,
				 false, i, 2);
		    queue.offer(e);
		    ci += 15;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    if (identicalCP(ci, ci+4*3, ci+7*3, ci+9*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 0, tags[i], out);
			}
		    }
		    if (identicalCP(ci+9*3, ci+8*3, ci+6*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 1, tags[i], out);
			}
		    }
		    if (identicalCP(ci, ci+1*3, ci+2*3, ci+3*3)) {
			wellFormed = false;
			if (out != null) {
			    printDegenerateTriangle(i, 2, tags[i], out);
			}
		    }
		    e = new Edge(type, null, null, ci, ci+4*3, ci+7*3, ci+9*3,
				 false, i, 0);
		    queue.offer(e);
		    e = new Edge(type, null, null,
				 ci+9*3, ci+8*3, ci+6*3, ci+3*3,
				 false, i, 1);
		    queue.offer(e);
		    e = new Edge(type, null, null, ci+0, ci+1*3, ci+2*3, ci+3*3,
				 true, i, 2);
		    queue.offer(e);
		    ci += 30;
		    break;
		case SurfaceIterator.CUBIC_PATCH:
		    boolean e1z = false;
		    boolean e2z = false;
		    boolean e3z = false;
		    boolean e4z = false;
		    if (identicalCP(ci, ci+3, ci+6, ci+9)) {
			e1z = true;
			if (hasEdgeLoop(ci, ci+3, ci+6, ci+9)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,0,tags[i], out);
			}
		    } else {
			e = new Edge(type, null, null, ci, ci+3, ci+6, ci+9,
				     false, i, 0);
			queue.offer(e);
		    }
		    if (identicalCP(ci+9, ci+21, ci+33, ci+45)) {
			e2z = true;
			if (hasEdgeLoop(ci+9, ci+21, ci+33, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,1,tags[i], out);
			}
			if (e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, e2z, false, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null,
				     ci+9, ci+21, ci+33, ci+45,
				     false, i, 1);
			queue.offer(e);
		    }
		    if (identicalCP(ci+36, ci+39, ci+42, ci+45)) {
			e3z = true;
			if (hasEdgeLoop(ci+36, ci+39, ci+42, ci+45)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,2,tags[i], out);
			}
			if (e2z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, false, e2z, e3z, false,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null,
				     ci+36, ci+39, ci+42, ci+45,
				     true, i, 2);
			queue.offer(e);
		    }
		    if (identicalCP(ci, ci+12, ci+24, ci+36)) {
			e4z = true;
			if (hasEdgeLoop(ci, ci+12, ci+24, ci+36)) {
			    wellFormed = false;
			    if (out != null) printEdgeLoop(i,3,tags[i], out);
			}
			if (e3z || e1z) {
			    wellFormed = false;
			    if (out != null) {
				printDegeneratePatch(i, e1z, false, e3z, e4z,
						     tags[i],
						     out);
			    }
			}
		    } else {
			e = new Edge(type, null, null, ci, ci+12, ci+24, ci+36,
				     true, i, 3);
			queue.offer(e);
		    }
		    ci += 48;
		    break;
		}
	    }
	    Edge[] array = new Edge[queue.size()];
	    Edge[] qarray = new Edge[queue.size()];

	    HashSet<Integer> cset = new HashSet<>(index);
	    queue.toArray(array);
	    Arrays.sort(array);
	    queue.toArray(qarray);

	    /*
	    int ii = 0;
	    for (Edge e: qarray) {
		System.out.println((ii++) +": " + e);
	    }
	    */

	    int[] offsets = new int[oqueue.size()];
	    int oind = 0;
	    for (Integer offset: oqueue) {
		offsets[oind++] = offset;
	    }

	    int lower = 0;
	    queue.clear();
	    while (lower < qarray.length) {
		Edge edge = qarray[lower];
		if (edge.used) {
		    lower++;
		    continue;
		}
		int entry = edge.entryNumber;
		// Edge next = findReverse(array, edge);
		// if (next != null) queue.offer(next);
		// edge.used = true;
		cset.add(entry);
		int upper = lower;
		while (upper < qarray.length) {
		    edge = qarray[upper];
		    if (edge.entryNumber == entry) {
			queue.offer(edge);
			edge.used = true;
			upper++;
		    } else {
			break;
		    }
		}
		lower = upper;
		while (!queue.isEmpty()) {
		    edge = queue.poll();
		    Edge next = findReverse(array, edge, oriented);
		    if (next == null  || next.used)  continue;
		    int li = Arrays.binarySearch(qarray, next,
						 new Comparator<Edge>() {
						     public int compare(Edge e1,
									Edge e2)
						     {
							 int n1 =
							     e1.entryNumber;
							 int n2 =
							     e2.entryNumber;
							 if (n1 < n2) return -1;
							 if (n1 > n2) return 1;
							 return 0;
						     }
						 });
		    if (li < 0) {
			// System.out.println("search for li failed");
			continue;
		    }
		    entry = next.entryNumber;
		    while (li > 0 && qarray[li-1].entryNumber == entry) li--;
		    int lu = li;
		    while (lu < qarray.length
			   && qarray[lu].entryNumber == entry) {
			lu++;
		    }
		    while (li < lu) {
			if (qarray[li].used) {
			    li++;
			} else {
			    qarray[li].used = true;
			    queue.offer(qarray[li]);
			    li++;
			}
		    }
		    cset.add(entry);
		}
		int indx =  0;
		int[] tindex = new int[cset.size()];
		int[] cindex = new int[cset.size()];
		for (Integer ind: cset) {
		    tindex[indx] = ind;
		    cindex[indx] = offsets[ind];
		    indx++;
		}
		cList.add(new SurfaceComponent(this, tindex, cindex));
		cset.clear();
	    }
	    components = new Shape3D[cList.size()];
	    cList.toArray(components);
	    componentsComputed = true;
	}
    }

    /**
     * Append the surface segments specified by a surface iterator.
     * @param si the surface iterator
     */
    public abstract void append(SurfaceIterator si);
    
    /**
     * Append the surface segments specified by another surface.
     * @param surface the other surface
     */
    public final void append(Shape3D surface) {
	SurfaceIterator si = surface.getSurfaceIterator(null);
	append(si);
    }

    /**
     * Append the surface segments specified by another surface after
     * applying a transformation.
     * @param surface the other surface
     * @param transform the transform to apply to a surface's control points
     */
    public final void append(Shape3D surface, Transform3D transform) {
	SurfaceIterator si = surface.getSurfaceIterator(transform);
	append(si);
    }

    /**
     * Get a bounding rectangular cuboid that encloses all of the points of
     * this surface.
     * The cuboid will have its edges parallel to the X, Y and Z axes
     * and may not be the smallest possible cuboids - for planar and
     * B&eacute;zier surface segments, the control points, whose convex
     * hull enclose the segment, are used to find the dimensions and locations
     * of the cuboid: the bounding cuboid is typically used as a quick test
     * to determine if two objects do not overlap, with more expense tests
     * used if their bounding rectangles do overlap.
     * @return a bounding rectangular cuboid
     */
    @Override
    public Rectangle3D getBounds() {
	SurfaceIterator si = getSurfaceIterator(null);
	double[] coords = new double[48];
	if (si.isDone()) return null;
	Rectangle3D bb = (this instanceof Surface3D.Double)?
	    new Rectangle3D.Double(): new Rectangle3D.Float();
	int type = si.currentSegment(coords);
	bb.setRect(coords[0], coords[1], coords[2], 0.0, 0.0, 0.0);
	switch(type) {
	case SurfaceIterator.CUBIC_PATCH:
	    bb.add(coords[30], coords[31], coords[32]);
	    bb.add(coords[33], coords[34], coords[35]);
	    bb.add(coords[36], coords[37], coords[38]);
	    bb.add(coords[39], coords[40], coords[41]);
	    bb.add(coords[42], coords[43], coords[44]);
	    bb.add(coords[45], coords[46], coords[47]);
	case SurfaceIterator.CUBIC_TRIANGLE:
	    bb.add(coords[15], coords[16], coords[17]);
	    bb.add(coords[18], coords[19], coords[20]);
	    bb.add(coords[21], coords[22], coords[23]);
	    bb.add(coords[24], coords[25], coords[26]);
	    bb.add(coords[27], coords[28], coords[29]);
	case SurfaceIterator.CUBIC_VERTEX:
	    bb.add(coords[9], coords[10], coords[11]);
	    bb.add(coords[12], coords[13], coords[14]);
	case SurfaceIterator.PLANAR_TRIANGLE:
	    bb.add(coords[3], coords[4], coords[5]);
	    bb.add(coords[6], coords[7], coords[8]);
	    break;
	default:
	    break;
	}
	si.next();
	while(!si.isDone()) {
	    switch(si.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		bb.add(coords[30], coords[31], coords[32]);
		bb.add(coords[33], coords[34], coords[35]);
		bb.add(coords[36], coords[37], coords[38]);
		bb.add(coords[39], coords[40], coords[41]);
		bb.add(coords[42], coords[43], coords[44]);
		bb.add(coords[45], coords[46], coords[47]);
	    case SurfaceIterator.CUBIC_TRIANGLE:
		bb.add(coords[15], coords[16], coords[17]);
		bb.add(coords[18], coords[19], coords[20]);
		bb.add(coords[21], coords[22], coords[23]);
		bb.add(coords[24], coords[25], coords[26]);
		bb.add(coords[27], coords[28], coords[29]);
	    case SurfaceIterator.CUBIC_VERTEX:
		bb.add(coords[9], coords[10], coords[11]);
		bb.add(coords[12], coords[13], coords[14]);
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		bb.add(coords[0], coords[1], coords[2]);
		bb.add(coords[3], coords[4], coords[5]);
		bb.add(coords[6], coords[7], coords[8]);
	    default:
		break;
	    }
	    si.next();
	}
	return bb;
    }

    void computeBoundary(Appendable out) {
	computeBoundary(out, false);
    }

    /**
     * Compute the boundary of this surface.
     * This method is public because org.bzdev.p3d.Model3D uses it.
     * <P>
     * The second argument, when true, relaxes some tests for error
     * conditions. Normally this argument should be false.  A choice
     * is provided because the Model3D class uses a partial surface
     * where planar triangles are not included. This can result in
     * cases where multiple boundary segments of this partial surface
     * share the same starting points in the partial surface, but not
     * the full surface.
     * @param out an Appendable for logging error messages
     * @param multipleEdges true if multiple edges on the boundary can
     *        begin at the same vertex; false (the default) otherwise
     */
    abstract public void computeBoundary(Appendable out,
					 boolean multipleEdges);


    /**
     * Determine if the surface is well formed, recording errors on an
     * Appendable.
     * A surface is considered to be well formed if it is a
     * 2-manifold: each edge of each segment is attached to at
     * most one other segment and segments join only at vertices.
     * For two segments with a common edge, that edge will be
     * traversed in opposite directions when the edges of each segment
     * are traversed counterclockwise (around their normal vectors)
     * and the surface is oriented.
     * @return true if a surface is well formed; false otherwise
     * @param out an Appendable used to record error messages
     */
    public synchronized boolean isWellFormed(Appendable out) {
	// we want to recompute the boundary because otherwise
	// any errors would not be printed on the output
	boundaryComputed = false;
	computeBoundary(out);
	return wellFormed;
    }

    /**
     * Determine if the surface is well formed, recording errors on an
     * Appendable, with a choice of how multiple edges at a single point
     * on a boundary are treated.
     * A surface is considered to be well formed if it is a
     * 2-manifold: each edge of each segment is attached to at
     * most one other segment and segments join only at vertices.
     * For two segments with a common edge, that edge will be
     * traversed in opposite directions when the edges of each segment
     * are traversed counterclockwise (around their normal vectors)
     * and the surface is oriented.
     * <P>
     * The second argument, when true, relaxes some tests for error
     * conditions. Normally this argument should be false.  A choice
     * is provided because the Model3D class uses a partial surface
     * where planar triangles are not included. This can result in
     * cases where multiple boundary segments of this partial surface
     * share the same starting points in the partial surface, but not
     * the full surface.
     * @return true if a surface is well formed; false otherwise
     * @param out an Appendable used to record error messages
     * @param multipleEdges true if a boundary can have multiple edges
     *        that start at the same point; false otherwise
     */
    public synchronized boolean isWellFormed(Appendable out,
					     boolean multipleEdges) {
	// we want to recompute the boundary because otherwise
	// any errors would not be printed on the output
	boundaryComputed = false;
	computeBoundary(out, multipleEdges);
	return wellFormed;
    }

    /**
     * Determine if the surface is well formed.
     * A surface is considered to be well formed if it is a
     * 2-manifold: each edge of each segment is attached to at
     * most one other segment and segments join only at vertices.
     * For two segments with a common edge, that edge will be
     * traversed in opposite directions when the edges of each segment
     * are traversed counterclockwise (around their normal vectors)
     * and the surface is oriented.
     * @return true if a surface is well formed; false otherwise
     */
    public synchronized boolean isWellFormed() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	return wellFormed;
    }

    /**
     * Get a path that is the boundary of this surface.
     * The path returned will be an empty path if the surface has no
     * boundary.
     * @return the boundary of this surface; null if a boundary cannot
     *         be computed
     */
    public synchronized Path3D getBoundary() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	if (boundary == null) return null;
	return (Path3D)boundary.clone();
    }


   @Override
    public synchronized int[] getBoundarySegmentIndices() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	if (boundarySegments == null) return null;
	int[] result = new int[boundarySegments.size()];
	int i = 0;
	for (Integer ind: boundarySegments) {
	    result[i++] = ind;
	}
	return result;
    }

    /*
    public synchronized Object[] getBoundaryTags() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	Object[] result = new Object[boundarySegments.size()];
	int i = 0;
	for (Integer ind: boundarySegments) {
	    result[i++] = getSegmentTag(ind);
	}
	return result;
    }
    */

    @Override
    public synchronized int[] getBoundaryEdgeNumbers() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	if (edgeNumbers == null) return null;
	int[] result = new int[edgeNumbers.size()];
	int i = 0;
	for (Integer e: edgeNumbers) {
	    result[i++] = e;
	}
	return result;
    }


    /**
     * Reset the surface by removing all of its segments.
     * This method should not be called if a surface iterator is in use.
     */
    public final synchronized void reset() {
	index = 0;
	cindex = 0;
	boundaryComputed = false;
	boundary = null;
	boundaryTags = null;
	edgeNumbers = null;
    }

    /**
     * Create a new surface by applying a transform to this surface.
     * @param transform the transform
     * @return the new surface
     */
    public abstract Surface3D createTransformedSurface(Transform3D transform);


    /**
     * Reverse the orientation of a surface segment for a double-precision
     * control-point array. The array argument stores the control
     * points in the order specified by
     * {@link #addCubicPatch(double[])}, {@link #addCubicTriangle(double[])},
     * and {@link #addPlanarTriangle(double[])}.
     * @param type the type of the segment (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param carray the array containing the control points
     */
    public static void reverseOrientation(int type, double[] carray) {
	double[] tmp = new double[3];
	switch(type) {
	case SurfaceIterator.PLANAR_TRIANGLE:
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 6, carray, 3, 3);
	    System.arraycopy(tmp, 0, carray, 6, 3);
	    break;
	case SurfaceIterator.CUBIC_VERTEX:
	    System.arraycopy(carray, 0, tmp, 0, 3);
	    System.arraycopy(carray, 9, carray, 0, 3);
	    System.arraycopy(tmp, 0, carray, 9, 3);
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 6, tmp, 3, 3);
	    System.arraycopy(tmp, 0, carray, 6, 3);
	    break;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 12, carray, 3, 3);
	    System.arraycopy(tmp, 0, carray, 12, 3);
	    System.arraycopy(carray, 6, tmp, 0, 3);
	    System.arraycopy(carray, 21, carray, 6, 3);
	    System.arraycopy(tmp, 0, carray, 21, 3);
	    System.arraycopy(carray, 9, tmp, 0, 3);
	    System.arraycopy(carray, 27, carray, 9, 3);
	    System.arraycopy(tmp, 0, carray, 27, 3);
	    System.arraycopy(carray, 18, tmp, 0, 3);
	    System.arraycopy(carray, 24, carray, 18, 3);
	    System.arraycopy(tmp, 0, carray, 24, 3);
	    break;
	case SurfaceIterator.CUBIC_PATCH:
	    for (int j = 0; j < 3; j++) {
		MatrixOps.transpose(carray, 4, 4, 0, carray, 4, 4, 0,
				    false, j, 3, j, 3);
	    }
	    break;
	}
    }

    /**
     * Reverse the orientation of a surface segment for a single-precision
     * control-point array. The array argument stores the control
     * points in the order specified by
     * {@link #addCubicPatch(double[])}, {@link #addCubicTriangle(double[])},
     * and {@link #addPlanarTriangle(double[])}.
     * @param type the type of the segment (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param carray the array containing the control points
     */
    public static void reverseOrientation(int type, float[] carray) {
	double[] tmp = new double[3];
	switch(type) {
	case SurfaceIterator.PLANAR_TRIANGLE:
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 6, carray, 3, 3);
	    System.arraycopy(tmp, 0, carray, 6, 3);
	    break;
	case SurfaceIterator.CUBIC_VERTEX:
	    System.arraycopy(carray, 0, tmp, 0, 3);
	    System.arraycopy(carray, 9, carray, 0, 3);
	    System.arraycopy(tmp, 0, carray, 9, 3);
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 6, tmp, 3, 3);
	    System.arraycopy(tmp, 0, carray, 6, 3);
	    break;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    System.arraycopy(carray, 3, tmp, 0, 3);
	    System.arraycopy(carray, 12, carray, 3, 3);
	    System.arraycopy(tmp, 0, carray, 12, 3);
	    System.arraycopy(carray, 6, tmp, 0, 3);
	    System.arraycopy(carray, 21, carray, 6, 3);
	    System.arraycopy(tmp, 0, carray, 21, 3);
	    System.arraycopy(carray, 9, tmp, 0, 3);
	    System.arraycopy(carray, 27, carray, 9, 3);
	    System.arraycopy(tmp, 0, carray, 27, 3);
	    System.arraycopy(carray, 18, tmp, 0, 3);
	    System.arraycopy(carray, 24, carray, 18, 3);
	    System.arraycopy(tmp, 0, carray, 24, 3);
	    break;
	case SurfaceIterator.CUBIC_PATCH:
	    for (int j = 0; j < 3; j++) {
		MatrixOps.transpose(carray, 4, 4, 0, carray, 4, 4, 0,
				    false, j, 3, j, 3);
	    }
	    break;
	}
    }

    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=0.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter u is 0.0,
     * and the last control point is the point on the curve
     * where the parameter u is 1.0. There are a total of four control points
     * for this curve.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,0</sub>, P<sub>1,0</sub>, P<sub>2,0</sub>, and P<sub>3,0</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The initial control point in the input array for
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the initial control point in the input array for
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupV0ForPatch(double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	ccoords[0] = pcoords[i++];
	ccoords[1] = pcoords[i++];
	ccoords[2] = pcoords[i++];
	i += incr;
	ccoords[3] = pcoords[i++];
	ccoords[4] = pcoords[i++];
	ccoords[5] = pcoords[i++];
	i += incr;
	ccoords[6] = pcoords[i++];
	ccoords[7] = pcoords[i++];
	ccoords[8] = pcoords[i++];
	i += incr;
	ccoords[9] = pcoords[i++];
	ccoords[10] = pcoords[i++];
	ccoords[11] = pcoords[i++];
    }

    public static boolean checkPatchCorners(double[] ucoords0,
					    double[] ucoords1,
					    double[] vcoords0,
					    double[] vcoords1)
    {
	try {
	    return checkPatchCorners(ucoords0, ucoords1,
				     vcoords0, vcoords1,
				     null);
	} catch (IOException e) {return true;}
    }

    public static boolean checkPatchCorners(double[] ucoords0,
					    double[] ucoords1,
					    double[] vcoords0,
					    double[] vcoords1,
					    Appendable out)
	throws IOException
    {
	for (int i = 0; i < 3; i++) {
	    if (ucoords0[i] != vcoords0[i]) {
		if (out != null) {
		    out.append("U0 (start) and V0 (start) not the same\n");
		}
		return false;
	    }
	    if (ucoords0[i+9] != vcoords1[i]) {
		if (out != null) {
		    out.append("U0 (end) and V1 (start) not the same\n");
		}
		return false;
	    }
	    if (ucoords1[i] != vcoords0[i+9]) {
		if (out != null) {
		    out.append("U1 (start) and V0 (end) not the same\n");
		}
		return false;
	    }
	    if (ucoords1[i+9] != vcoords1[i+9]) {
		if (out != null) {
		    out.append("U1 (end) and V1 (end) not the same\n");
		}
		return false;
	    }
	}
	return true;
    }

    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=0, where the edge's control points are given by explicit coordinates
     * and an array.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter u is 0.0,
     * and the last control point is the point on the curve
     * where the parameter u is 1.0. There are a total of four control points
     * for this curve. The first control point for this curve is
     * specified by the first three arguments and the remaining control points
     * ares specified by the forth argument.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,0</sub>, P<sub>1,0</sub>, P<sub>2,0</sub>, and P<sub>3,0</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The initial control point in the input array for
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the initial control point in the input array for
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param x the X coordinate of the initial control point for the edge
     * @param y the Y coordinate of the initial control point for the edge
     * @param z the Z coordinate of the initial control point for the edge
     * @param pcoords the coordinates for the remaining control points
     *        of the cubic B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupV0ForPatch(double x, double y, double z,
				       double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 6: 0;
	int incr = reverse? -6: 0;

	if (reverse) {
	    ccoords[0] = pcoords[i++];
	    ccoords[1] = pcoords[i++];
	    ccoords[2] = pcoords[i++];
	} else {
	    ccoords[0] = x;
	    ccoords[1] = y;
	    ccoords[2] = z;
	}
	i += incr;
	ccoords[3] = pcoords[i++];
	ccoords[4] = pcoords[i++];
	ccoords[5] = pcoords[i++];
	i += incr;
	ccoords[6] = pcoords[i++];
	ccoords[7] = pcoords[i++];
	ccoords[8] = pcoords[i++];
	i += incr;
	if (reverse) {
	    ccoords[9] = x;
	    ccoords[10] = y;
	    ccoords[11] = z;
	} else {
	    ccoords[9] = pcoords[i++];
	    ccoords[10] = pcoords[i++];
	    ccoords[11] = pcoords[i++];
	}
    }


    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=1.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter u is 0.0,
     * and the last control point is the point on the curve
     * where the parameter u is 1.0. There are a total of four control points
     * for this curve.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,3</sub>, P<sub>1,3</sub>, P<sub>2,3</sub>, and P<sub>3,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The final control point for the input array for
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * and the initial control point as for the input array of this method
     *  are the same. In addition
     * the final control point of the input array for
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * and the final control point of the input array for this method
     * are the same.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupV1ForPatch(double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;
	ccoords[36] = pcoords[i++];
	ccoords[37] = pcoords[i++];
	ccoords[38] = pcoords[i++];
	i += incr;
	ccoords[39] = pcoords[i++];
	ccoords[40] = pcoords[i++];
	ccoords[41] = pcoords[i++];
	i += incr;
	ccoords[42] = pcoords[i++];
	ccoords[43] = pcoords[i++];
	ccoords[44] = pcoords[i++];
	i += incr;
	ccoords[45] = pcoords[i++];
	ccoords[46] = pcoords[i++];
	ccoords[47] = pcoords[i++];
    }

    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=1, where the edge's control points are given by explicit coordinates
     * and an array.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter u is 0.0,
     * and the last control point is the point on the curve
     * where the parameter u is 1.0. There are a total of four control points
     * for this curve. The first control point for this curve is
     * specified by the first three arguments and the remaining control points
     * ares specified by the forth argument.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,3</sub>, P<sub>1,3</sub>, P<sub>2,3</sub>, and P<sub>3,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The final control point for the input array for
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * and the initial control point as for the input array of this method
     *  are the same. In addition
     * the final control point of the input array for
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * and the final control point of the input array for this method
     * are the same.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param x the X coordinate of the initial control point for the edge
     * @param y the Y coordinate of the initial control point for the edge
     * @param z the Z coordinate of the initial control point for the edge
     * @param pcoords the coordinates for the remaining control points of the
     *        cubic B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupV1ForPatch(double x, double y, double z,
				       double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 6: 0;
	int incr = reverse? -6: 0;
	if (reverse) {
	    ccoords[36] = pcoords[i++];
	    ccoords[37] = pcoords[i++];
	    ccoords[38] = pcoords[i++];
	} else {
	    ccoords[36] = x;
	    ccoords[37] = y;
	    ccoords[38] = z;
	}
	i += incr;
	ccoords[39] = pcoords[i++];
	ccoords[40] = pcoords[i++];
	ccoords[41] = pcoords[i++];
	i += incr;
	ccoords[42] = pcoords[i++];
	ccoords[43] = pcoords[i++];
	ccoords[44] = pcoords[i++];
	i += incr;
	if (reverse) {
	    ccoords[45] = x;
	    ccoords[46] = y;
	    ccoords[47] = z;
	} else {
	    ccoords[45] = pcoords[i++];
	    ccoords[46] = pcoords[i++];
	    ccoords[47] = pcoords[i++];
	}
    }


    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=0.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter v is 0.0,
     * and the last control point is the point on the curve
     * where the parameter v is 1.0. There are a total of four control points
     * for this curve.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,0</sub>, P<sub>0,1</sub>, P<sub>0,2</sub>, and P<sub>0,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The initial control point in the input array for
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the initial control point in the input array for
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupU0ForPatch(double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	ccoords[0] = pcoords[i++];
	ccoords[1] = pcoords[i++];
	ccoords[2] = pcoords[i++];
	i += incr;
	ccoords[12] = pcoords[i++];
	ccoords[13] = pcoords[i++];
	ccoords[14] = pcoords[i++];
	i += incr;
	ccoords[24] = pcoords[i++];
	ccoords[25] = pcoords[i++];
	ccoords[26] = pcoords[i++];
	i += incr;
	ccoords[36] = pcoords[i++];
	ccoords[37] = pcoords[i++];
	ccoords[38] = pcoords[i++];
    }

    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=0, where the edge's control points are given by explicit coordinates
     * and an array.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter v is 0.0,
     * and the last control point is the point on the curve
     * where the parameter v is 1.0. There are a total of four control points
     * for this curve. The first control point for this curve is
     * specified by the first three arguments and the remaining control points
     * ares specified by the forth argument.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>0,0</sub>, P<sub>0,1</sub>, P<sub>0,2</sub>, and P<sub>0,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 9.
     * The initial control point in the input array for
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the initial control point in the input array for
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param x the X coordinate of the initial control point for the edge
     * @param y the Y coordinate of the initial control point for the edge
     * @param z the Z coordinate of the initial control point for the edge
     * @param pcoords the coordinates for the remaining control points of the
     *        cubic B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupU0ForPatch(double x, double y, double z,
				       double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 6: 0;
	int incr = reverse? -6: 0;

	
	if (reverse) {
	    ccoords[0] = pcoords[i++];
	    ccoords[1] = pcoords[i++];
	    ccoords[2] = pcoords[i++];
	} else {
	    ccoords[0] = x;
	    ccoords[1] = y;
	    ccoords[2] = z;
	}
	i += incr;
	ccoords[12] = pcoords[i++];
	ccoords[13] = pcoords[i++];
	ccoords[14] = pcoords[i++];
	i += incr;
	ccoords[24] = pcoords[i++];
	ccoords[25] = pcoords[i++];
	ccoords[26] = pcoords[i++];
	i += incr;
	if (reverse) {
	    ccoords[36] = x;
	    ccoords[37] = y;
	    ccoords[38] = z;
	} else {
	    ccoords[36] = pcoords[i++];
	    ccoords[37] = pcoords[i++];
	    ccoords[38] = pcoords[i++];
	}
    }


    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=1.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter v is 0.0,
     * and the last control point is the point on the curve
     * where the parameter v is 1.0. There are a total of four control points
     * for this curve.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>3,0</sub>, P<sub>3,1</sub>, P<sub>3,2</sub>, and P<sub>3,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The final control point in the input array for
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the final control point in the input array for
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupU1ForPatch(double[]pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	ccoords[9] = pcoords[i++];
	ccoords[10] = pcoords[i++];
	ccoords[11] = pcoords[i++];
	i += incr;
	ccoords[21] = pcoords[i++];
	ccoords[22] = pcoords[i++];
	ccoords[23] = pcoords[i++];
	i += incr;
	ccoords[33] = pcoords[i++];
	ccoords[34] = pcoords[i++];
	ccoords[35] = pcoords[i++];
	i += incr;
	ccoords[45] = pcoords[i++];
	ccoords[46] = pcoords[i++];
	ccoords[47] = pcoords[i++];
    }

    /**
     * Set up elements of an array containing the coordinates of a
     * B&eacute;zier patch's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=1, where the edge's control points are given by explicit coordinates
     * and an array.
     * Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points for the cubic curve are listed
     * sequentially, with X, Y, and Z values for each control point
     * grouped together and in that order.  The first (O<sup>th</sup>)
     * control point is the point on the curve where the parameter v is 0.0,
     * and the last control point is the point on the curve
     * where the parameter v is 1.0. There are a total of four control points
     * for this curve. The first control point for this curve is
     * specified by the first three arguments and the remaining control points
     * ares specified by the forth argument.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>3,0</sub>, P<sub>3,1</sub>, P<sub>3,2</sub>, and P<sub>3,3</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * The final control point in the input array for
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * is the same as the initial control point in the input array for this
     * method.
     * In addition, the final control point in the input array for
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * is the same as the final point in the input array for this
     * method.
     * It its the caller's responsibility to make sure the same values
     * are used.
     * <P>
     * For an oriented surface, the direction of the normal vector is
     * the same as that for the cross product of a tangent vector in
     * the increasing-u direction with a tangent vector in the
     * increasing-v direction. This is equivalent to using the right-hand
     * rule when traversing vertices whose (u,v) coordinates are (0,0),
     * followed by (1, 0), followed by (1,1), followed by (1,0). For a
     * closed, oriented surface,the normal vector should point towards the
     * outside of the surface.
     * @param x the X coordinate of the initial control point for the edge
     * @param y the Y coordinate of the initial control point for the edge
     * @param z the Z coordinate of the initial control point for the edge
     * @param pcoords the coordinates for the remaining control points of
     *        the cubic B&eacute;zier curve
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     */
    public static void setupU1ForPatch(double x, double y, double z,
				       double[] pcoords, double[] ccoords,
				       boolean reverse) {
	int i = reverse? 6: 0;
	int incr = reverse? -6: 0;

	if (reverse) {
	    ccoords[9] = pcoords[i++];
	    ccoords[10] = pcoords[i++];
	    ccoords[11] = pcoords[i++];
	} else {
	    ccoords[9] = x;
	    ccoords[10] = y;
	    ccoords[11] = z;
	}
	i += incr;
	ccoords[21] = pcoords[i++];
	ccoords[22] = pcoords[i++];
	ccoords[23] = pcoords[i++];
	i += incr;
	ccoords[33] = pcoords[i++];
	ccoords[34] = pcoords[i++];
	ccoords[35] = pcoords[i++];
	i += incr;
	if (reverse) {
	    ccoords[45] = x;
	    ccoords[46] = y;
	    ccoords[47] = z;
	} else {
	    ccoords[45] = pcoords[i++];
	    ccoords[46] = pcoords[i++];
	    ccoords[47] = pcoords[i++];
	}
    }
 
    private static final double bvalues[][] = {
	{9.0, 0.0, 0.0, 0.0},
	{-9.0, 0.0, 0.0, 0.0},
	{0.0, 9.0, 0.0, 0.0},
	{0.0, -9.0, 0.0, 0.0},
	{-9.0, 0.0, 0.0, 0.0},
	{0.0, 0.0, 0.0, 0.0},
	{0.0, 0.0, 0.0, 0.0},
	{0.0, 9.0, 0.0, 0.0},
	{0.0, 0.0, 9.0, 0.0},
	{0.0, 0.0, 0.0, 0.0},
	{0.0, 0.0, 0.0, 0.0},
	{0.0, 0.0, 0.0, -9.0},
	{0.0, 0.0, -9.0, 0.0},
	{0.0, 0.0, 9.0, 0.0},
	{0.0, 0.0, 0.0, -9.0},
	{0.0, 0.0, 0.0, 9.0},
    };

    /**
     * Set up the remaining control points for a cubic B&eacute;zier
     * patch, using default values.
     * A cubic B&eacute;zier patch has two parameters (u,v).
     * This method sets four control points so that for a point p on
     * the surface, &part;p/(&part;u&part;v) = 0 at the corners of the
     * patch - at a corner, the values of (u,v) are either (0,0), (1,0),
     * (0,1), or (1,1). A precondition is that the control points set
     * by the methods
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * and
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * have been set.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>1,1</sub>, P<sub>1,2</sub>, P<sub>2,1</sub>, and P<sub>2,2</sub>.
     * <P>
     * The length of the array ccoords must be at least 48.
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     */
    public static void setupRestForPatch(double ccoords[]) {

	double dudv00[] = new double[3];
	double dudv10[] = new double[3];
	double dudv01[] = new double[3];
	double dudv11[] = new double[3];

	// The second partial derivatives with respect to u&v will
	// be zero at the corners. If we compute the second
	// partial derivatives at the corners with the middle
	// control points set to zero, and subtract those values from
	// zero, we obtain the valued dudv00 ... dudv11. Then
	//
	//  | dudv00 |     | 9  0  0  0 |  | CP11 |
	//  | dudv10 |  =  | 0 -9  0  0 |  | CP12 |
	//  | dudv01 |     | 0  0 -9  0 |  | CP21 |
	//  | dudv11 |     | 0  0  0  9 |  | CP22 |
	//
	// and we can solve for the control points.  The square matrix
	// contains the value of
	// Functions.dBdx(i,3,u)*Functions.dBdx(j, 3, v) for
	// (u,v) = (0,0), (1,0), (0,1), (1,1) and for i and j matching
	// CPij (with i and j taking on the values 0 and 1).
	//
	// The computation is done for the X, Y, and Z components of the
	// the derivatives and the control points.  The products of the
	// the Bernstein polynomial's derivatives are pre-computed.
	//

	for (int k = 0; k < 16; k++) {
	    int i = k % 4;
	    int j = k / 4;
	    if ((i == 1 || i == 2) && (j == 1 || j == 2)) continue;
	    for (int n = 0; n < 3; n++) {
		dudv00[n] -= ccoords[3*k + n] * bvalues[k][0];
	    }
	    for (int n = 0; n < 3; n++) {
		dudv10[n] -= ccoords[3*k + n] * bvalues[k][1];
	    }
	    for (int n = 0; n < 3; n++) {
		dudv01[n] -= ccoords[3*k + n] * bvalues[k][2];
	    }
	    for (int n = 0; n < 3; n++) {
		dudv11[n] -= ccoords[3*k + n] * bvalues[k][3];
	    }
	}
	for (int n = 0; n < 3; n++) {
	    ccoords[15+n] = dudv00[n]/9.0;
	    if (ccoords[15+n] == -0.0) ccoords[15+n] = 0.0;
	    ccoords[18+n] = -dudv10[n]/9.0;
	    if (ccoords[18+n] == -0.0) ccoords[18+n] = 0.0;
	    ccoords[27+n] = -dudv01[n]/9.0;
	    if (ccoords[27+n] == -0.0) ccoords[27+n] = 0.0;
	    ccoords[30+n] = dudv11[n]/9.0;
	    if (ccoords[30+n] == -0.0) ccoords[30+n] = 0.0;
	}
    }

    // copied from org.bzdev.math.BicubicInterpolator
    private static final double MI9[][] = {{9.0, 0.0, 0.0, 0.0},
					    {9.0, 3.0, 0.0, 0.0},
					    {9.0, 6.0, 3.0, 0.0},
					    {9.0, 9.0, 9.0, 9.0}};
    private static final double MTI9[][] = {{9.0, 9.0, 9.0, 9.0},
					    {0.0, 3.0, 6.0, 9.0},
					    {0.0, 0.0, 3.0, 9.0},
					    {0.0, 0.0, 0.0, 9.0}};
    private static final double matrix[][] = {
	{ 1.0,  0.0,  0.0,  0.0, 0.0, 0.0, 0.0, 0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{-3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0,
	 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},

	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  -3.0,  3.0,  0.0,  0.0, -2.0, -1.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  2.0, -2.0,  0.0,  0.0,  1.0,  1.0,  0.0,  0.0},

	{-3.0,  0.0,  3.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	 -2.0,  0.0, -1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0, -3.0,  0.0,  3.0,  0.0,
	  0.0,  0.0,  0.0,  0.0, -2.0,  0.0, -1.0,  0.0},
	{ 9.0, -9.0, -9.0,  9.0,  6.0,  3.0, -6.0, -3.0,
	  6.0, -6.0,  3.0, -3.0,  4.0,  2.0,  2.0,  1.0},
	{-6.0,  6.0,  6.0, -6.0, -3.0, -3.0,  3.0,  3.0,
	 -4.0,  4.0, -2.0,  2.0, -2.0, -2.0, -1.0, -1.0},

	{ 2.0,  0.0, -2.0,  0.0,  0.0,  0.0,  0.0,  0.0,
	  1.0,  0.0,  1.0,  0.0,  0.0,  0.0,  0.0,  0.0},
	{ 0.0,  0.0,  0.0,  0.0,  2.0,  0.0, -2.0,  0.0,
	  0.0,  0.0,  0.0,  0.0,  1.0,  0.0,  1.0,  0.0},
	{-6.0,  6.0,  6.0, -6.0, -4.0, -2.0,  4.0, 2.0,
	 -3.0,  3.0, -3.0,  3.0, -2.0, -1.0, -2.0, -1.0},
	{ 4.0, -4.0, -4.0,  4.0,  2.0,  2.0, -2.0, -2.0,
	  2.0, -2.0,  2.0, -2.0,  1.0,  1.0,  1.0,  1.0}
    };

    /**
     * Set up the remaining control points for a cubic B&eacute;zier
     * patch, specifying derivatives at the corners.
     * A cubic B&eacute;zier patch has two parameters (u,v).  This
     * method sets four control points so that for a point p on the
     * surface, &part;p/(&part;s&part;t) has specified values at the
     * corners of the patch - at a corner, the values of (u,v) are
     * either (0,0), (1,0), (0,1), or (1,1). The parameter s is a
     * linear function of u and the parameter t is a linear function
     * of v.  A precondition is that the control points set by the
     * methods
     * {@link #setupV0ForPatch(double[],double[],boolean)}
     * {@link #setupV1ForPatch(double[],double[],boolean)}
     * {@link #setupU0ForPatch(double[],double[],boolean)}
     * and
     * {@link #setupU1ForPatch(double[],double[],boolean)}
     * have been set.
     * <P>
     * The coordinates of each of the patch's 16 control points are grouped
     * so that for each point the X coordinate is followed immediately by the Y
     * coordinate and then the Z coordinate.  The position of a point p on the
     * patch is given by
     * <blockquote>
     * p = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)</sub>B<sub>i,3</sub>(v)
     * </blockquote>
     * where i and j vary from 0 to 3 inclusive, u and v vary from 0.0 to 1.0,
     * and
     * B<sub>i,3</sub>(u) = (3!/(i!(3-i)!)u<sup>i</sup>(1-u)<sup>(3-i)</sup>.
     * The array ccoords stores the P<sub>i,j</sub> control points with
     * the index i varying the fastest.
     * The control points that will be set are
     * P<sub>1,1</sub>, P<sub>1,2</sub>, P<sub>2,1</sub>, and P<sub>2,2</sub>.
     * <P>
     * The length of the array ccoords must be at least 48 and the length
     * of the array pcoords must be at least 12.
     * @param urange the range for s when u varies from 0 to 1
     * @param vrange the range for t when v varies from 0 to 1
     * @param d2Pdsdt00 An array containing the values
     *        &part;<sup>2</sup>x/(&part;s&part;t),
     *        &part;<sup>2</sup>y/(&part;s&part;t), and
     *        &part;<sup>2</sup>z/(&part;s&part;t), evaluated with
     *        u = 0 and v = 0
     * @param d2Pdsdt10 An array containing the values
     *        &part;<sup>2</sup>x/(&part;s&part;t),
     *        &part;<sup>2</sup>y/(&part;s&part;t), and
     *        &part;<sup>2</sup>z/(&part;s&part;t), evaluated with
     *        u = 1 and v = 0
     * @param d2Pdsdt01 An array containing the values
     *        &part;<sup>2</sup>x/(&part;s&part;t),
     *        &part;<sup>2</sup>y/(&part;s&part;t), and
     *        &part;<sup>2</sup>z/(&part;s&part;t), evaluated with
     *        u = 0 and v = 1
     * @param d2Pdsdt11 An array containing the values
     *        &part;<sup>2</sup>x/(&part;s&part;t),
     *        &part;<sup>2</sup>y/(&part;s&part;t), and
     *        &part;<sup>2</sup>z/(&part;s&part;t), evaluated with
     *        u = 1 and v = 1
     * @param ccoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     */
    public static void setupRestForPatch(double urange, double vrange,
					 double[] d2Pdsdt00,
					 double[] d2Pdsdt10,
					 double[] d2Pdsdt01,
					 double[] d2Pdsdt11,
					 double[] ccoords)
    {
	// Normalize using s = urange*u, t = vrange*v
	double scaleFactor = urange*vrange;
	for (int i = 0; i < 3; i++) {
	    d2Pdsdt00[i] *= scaleFactor;
	    d2Pdsdt10[i] *= scaleFactor;
	    d2Pdsdt01[i] *= scaleFactor;
	    d2Pdsdt11[i] *= scaleFactor;
	}
	double inits[][] = {
	    // BicubicInerpolator inits[0]
	    {ccoords[0], ccoords[1], ccoords[2]},
	    // BicubicInerpolator inits[1]
	    {ccoords[9], ccoords[10], ccoords[11]},
	    // BicubicInerpolator inits[2]
	    {ccoords[36], ccoords[37], ccoords[38]},
	    // BicubicInerpolator inits[3]
	    {ccoords[45], ccoords[46], ccoords[47]},
	    // BicubicInerpolator inits[4]
	    {3*(ccoords[3]-ccoords[0]),
	     3*(ccoords[4]-ccoords[1]),
	     3*(ccoords[5]-ccoords[2])},
	    // BicubicInerpolator inits[5]
	    {3*(ccoords[9]-ccoords[6]),
	     3*(ccoords[10]-ccoords[7]),
	     3*(ccoords[11]-ccoords[8])},
	    // BicubicInerpolator inits[6]
	    {3*(ccoords[39]-ccoords[36]),
	     3*(ccoords[40]-ccoords[37]),
	     3*(ccoords[41]-ccoords[38])},
	    // BicubicInerpolator inits[7]
	    {3*(ccoords[45]-ccoords[42]),
	     3*(ccoords[46]-ccoords[43]),
	     3*(ccoords[47]-ccoords[44])},
	    // BicubicInerpolator inits[8]
	    {3*(ccoords[12]-ccoords[0]),
	     3*(ccoords[13]-ccoords[1]),
	     3*(ccoords[14]-ccoords[2])},
	    // BicubicInerpolator inits[9]
	    {3*(ccoords[21]-ccoords[9]),
	     3*(ccoords[22]-ccoords[10]),
	     3*(ccoords[23]-ccoords[11])},
	    // BicubicInerpolator inits[10]
	    {3*(ccoords[36]-ccoords[24]),
	     3*(ccoords[37]-ccoords[25]),
	     3* (ccoords[38]-ccoords[26])},
	    // BicubicInerpolator inits[11]
	    {3*(ccoords[45]-ccoords[33]),
	     3*(ccoords[46]-ccoords[34]),
	     3*(ccoords[47]-ccoords[35])},
	    // BicubicInerpolator inits[12]
	    {d2Pdsdt00[0], d2Pdsdt00[1], d2Pdsdt00[2]},
	    // BicubicInerpolator inits[13]
	    {d2Pdsdt10[0], d2Pdsdt10[1], d2Pdsdt10[2]},
	    // BicubicInerpolator inits[14]
	    {d2Pdsdt01[0], d2Pdsdt01[1], d2Pdsdt01[2]},
	    // BicubicInerpolator inits[15]
	    {d2Pdsdt11[0], d2Pdsdt11[1], d2Pdsdt11[2]}
	};
	double[][][] a = new double[4][4][3];
	double[][][] temp = new double[4][4][3];
	for (int k = 0; k < 3; k++) {
	    for (int i = 0; i < 16; i++) {
		double sum = 0.0;
		for (int j = 0; j < 16; j++) {
		    sum += (matrix[i][j] * inits[j][k]);
		}
		int ii = i%4;
		int jj = i/4;
		a[ii][jj][k] = sum;
	    }
	}
	// compute (MI93 * a) * MTI93
	for (int m = 0; m < 3; m++) {
	    for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
		    double sum = 0.0;
		    for (int k = 0; k < 4; k++) {
			// sum += MI9[i][k]*a[k][j];
			sum += MI9[i][k]*a[k][j][m];
		    }
		    temp[i][j][m] = sum;
		}
	    }
	}
	for (int m = 0; m < 3; m++) {
	    // we only need i and j to both be 1 or 2 for the
	    // points we have to set.
	    for (int i = 1; i < 3; i++) {
		int ii = i*3;
		for (int j = 1; j < 3; j++) {
		    int jj = j * 3;
		    double sum = 0.0;
		    for (int k = 0; k < 4; k++) {
			sum += temp[i][k][m]*MTI9[k][j];
		    }
		    ccoords[ii + m +4*jj] = sum/81.0;
		}
	    }
	}
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * For an oriented surface, the orientation of the patch is
     * determined by the right-hand rule when traversing the edges of
     * the patch so that the (u,v) coordinates lie along a square
     * whose vertices are (0,0), (1,0), (1,1), (0,1), traversed in
     * that order before returning to (0,0). More precisely, at a
     * point (u,v), the direction of the normal to an oriented surface
     * is that of the cross product of a tangent vector at (u, v) in
     * the direction of increasing u with v constant and the tangent
     * vector at (u, v) with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     */
    public void addCubicPatch(double[] controlPoints) {
	addCubicPatch(controlPoints, null);
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface, reversing the
     * orientation of the patch.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * Before the orientation of the patch is reversed,
     * its orientation is determined by the right-hand
     * rule when traversing the edges of the patch so that the (u,v)
     * coordinates lie along a square whose vertices are (0,0), (1,0),
     * (1,1), (0,1), traversed in that order before returning to
     * (0,0).  More precisely, at a point (u,v), the direction of the
     * normal to an oriented surface is that opposite to the cross
     * product of a tangent vector at (u, v) in the direction of
     * increasing u with v constant and the tangent vector at (u, v)
     * with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * <P>
     * For a flipped and non-flipped patch created using the same
     * arguments, a point on the surface at coordinates (v,u) for the
     * flipped patch is the same as the point at coordinates (u,v)
     * for the non-flipped patch.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     */
    public void addFlippedCubicPatch(double[] controlPoints) {
	addFlippedCubicPatch(controlPoints, null);
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying
     * a color.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * For an oriented surface, the orientation of the patch is
     * determined by the right-hand rule when traversing the edges of
     * the patch so that the (u,v) coordinates lie along a square
     * whose vertices are (0,0), (1,0), (1,1), (0,1), traversed in
     * that order before returning to (0,0). More precisely, at a
     * point (u,v), the direction of the normal to an oriented surface
     * is that of the cross product of a tangent vector at (u, v) in
     * the direction of increasing u with v constant and the tangent
     * vector at (u, v) with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param color the color of this patch; null if none is specified
     */
    public void addCubicPatch(double[] controlPoints, Color color) {
	addCubicPatch(controlPoints, color, null);
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying a
     * color and reversing the orientation of the patch.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * Before the orientation of the patch is reversed,
     * its orientation is determined by the right-hand
     * rule when traversing the edges of the patch so that the (u,v)
     * coordinates lie along a square whose vertices are (0,0), (1,0),
     * (1,1), (0,1), traversed in that order before returning to
     * (0,0).  More precisely, at a point (u,v), the direction of the
     * normal to an oriented surface is that opposite to the cross
     * product of a tangent vector at (u, v) in the direction of
     * increasing u with v constant and the tangent vector at (u, v)
     * with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * <P>
     * For a flipped and non-flipped patch created using the same
     * arguments, a point on the surface at coordinates (v,u) for the
     * flipped patch is the same as the point at coordinates (u,v)
     * for the non-flipped patch.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param color the color of this patch; null if none is specified
     */
    public void addFlippedCubicPatch(double[] controlPoints, Color color) {
	addFlippedCubicPatch(controlPoints, color, null);
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying a tag.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * For an oriented surface, the orientation of the patch is
     * determined by the right-hand rule when traversing the edges of
     * the patch so that the (u,v) coordinates lie along a square
     * whose vertices are (0,0), (1,0), (1,1), (0,1), traversed in
     * that order before returning to (0,0). More precisely, at a
     * point (u,v), the direction of the normal to an oriented surface
     * is that of the cross product of a tangent vector at (u, v) in
     * the direction of increasing u with v constant and the tangent
     * vector at (u, v) with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param tag a tag naming this patch; null if there is none
     */
    public void addCubicPatch(double[] controlPoints, Object tag) {
	addCubicPatch(controlPoints, null, tag);
    }

    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying a  tag
     * and reversing the orientation of the patch.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * Before the orientation of the patch is reversed,
     * its orientation is determined by the right-hand
     * rule when traversing the edges of the patch so that the (u,v)
     * coordinates lie along a square whose vertices are (0,0), (1,0),
     * (1,1), (0,1), traversed in that order before returning to
     * (0,0).  More precisely, at a point (u,v), the direction of the
     * normal to an oriented surface is that opposite to the cross
     * product of a tangent vector at (u, v) in the direction of
     * increasing u with v constant and the tangent vector at (u, v)
     * with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * <P>
     * For a flipped and non-flipped patch created using the same
     * arguments, a point on the surface at coordinates (v,u) for the
     * flipped patch is the same as the point at coordinates (u,v)
     * for the non-flipped patch.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param tag a tag naming this patch; null if there is none
     */
    public void addFlippedCubicPatch(double[] controlPoints, Object tag) {
	addFlippedCubicPatch(controlPoints, null, tag);
    }


    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying
     * a color and a tag.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * The orientation of the patch is determined by the right-hand
     * rule when traversing the edges of the patch so that the (u,v)
     * coordinates lie along a square whose vertices are (0,0), (1,0),
     * (1,1), (0,1), traversed in that order before returning to
     * (0,0). More precisely, at a point (u,v), the direction of the
     * normal to an oriented surface is that of the cross product of a
     * tangent vector at (u, v) in the direction of increasing u with v
     * constant and the tangent vector at (u, v) with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param color the color of this patch; null if none is specified
     * @param tag a tag naming this patch; null if there is none
     */
    public abstract void addCubicPatch(double[] controlPoints,
				       Color color,
				       Object tag);

    /**
     * Add a cubic B&eacute;zier patch to this surface, specifying a
     * color and a tag, and reversing the patch's orientation.
     * The control points P<sub>i,j</sub>, with i and j in the range
     * [0,3], determine a cubic B&eacute;zier patch.  The equation
     * <blockquote><pre><big>
     * P = &sum;<sub>i,j</sub> P<sub>i,j</sub>B<sub>i,3</sub>(u)B<sub>j,3</sub>(v)
     * </big></pre><blockquote>
     * where B<sub>i,3</sub> are Bernstein polynomials of degree 3.
     * The orientation of the patch is determined by the right-hand
     * rule when traversing the edges of the patch so that the (u,v)
     * coordinates lie along a square whose vertices are (0,0), (1,0),
     * (1,1), (0,1), traversed in that order before returning to
     * (0,0). More precisely, at a point (u,v), the direction of the
     * normal to an oriented surface is that opposite to the cross
     * product of a tangent vector at (u, v) in the direction of
     * increasing u with v constant and the tangent vector at (u, v)
     * with constant u.
     * <P>
     * The controlPoints array contains 16 control points. The
     * matrix of control points P<sub>ij</sub> is stored in column-major
     * order with the X, Y, and Z values of a control point listed
     * sequentially.  The first 4 control points are also the control
     * points for a cubic B&eacute;zier curve that lies along the edge
     * of the patch where v=0; and the last 4 control points are the
     * control points for a B&eacute;zier curve that lies along the
     * edge of the patch where v=1.  The control points for the
     * B&eacute;zier curves along the edges where u=0 and u=1 do not
     * consist of contiguous entries in the controlPoints array.
     * <P>
     * Methods to help configure the control patches include
     * <UL>
     *   <LI>{@link #setupV0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupV1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU0ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupU1ForPatch(double[],double[],boolean)}.
     *   <LI>{@link #setupRestForPatch(double[])}.
     *   <LI>{@link #setupRestForPatch(double,double,double[],double[],double[],double[],double[])}.
     * </UL>
     * All control points associated with an edge of a cubic patch may be
     * identical, in which case the length of the edge is zero and the
     * edge is equivalent to a vertex, but it is illegal for two
     * adjacent edges to have zero length.  Furthermore, if the end points
     * of an edge are identical, the intermediate control points must
     * be located at the same point as the vertex.
     * <P>
     * For a flipped and non-flipped patch created using the same
     * arguments, a point on the surface at coordinates (v,u) for the
     * flipped patch is the same as the point at coordinates (u,v)
     * for the non-flipped patch.
     * @param controlPoints the control points defining this cubic
     *        B&eacute;zier patch.
     * @param color the color for this patch; null if none is specified
     * @param tag a tag naming this patch; null if there is none
     */
    public abstract void addFlippedCubicPatch(double[] controlPoints,
					      Color color,
					      Object tag);


    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=0 (the edge where the coordinates (u,v) vary from (0,0) to (0,1)),
     * using the arrays retrieved by PathIterator3D.
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>

     * The coordinates of the control points (P0, P1, P2, and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter v is 0.0, and the last control point is the
     * point on the curve where the parameter v is 1.0. There are a
     * total of four control points for this curve. The coordinates of
     * the first of these are provided as explicit arguments and the
     * coordinates of the rest are provided in an array.

     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0). The
     * indices for the control points set by this method are
     * (0,0,3), (0,1,2),(0,2,1), and (0,3,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point, given by the
     * first three arguments, will match the (u,v) coordinates (0,0);
     * when reverse is true, the first control point will match (0,1).  The
     * coordinates for the first control point are lastX, lastY, and lastZ:
     * the names reflect the typical use of a path iterator in which the
     * end of the last segment, or coordinates specified by a SEG_MOVETO
     * operation, is used as the start of the next.
     * <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by lastX,
     * lastY, lastZ, and the coords array, and read in the order
     * specified by the 'reverse' argument (P0 to P3 when 'reverse'
     * is false and P3 to P0 when reverse is true), will be placed at
     * locations 003, 012, 021, and 030 respectively.
     * @param lastX the initial control point's X coordinate
     * @param lastY the initial control point's Y coordinate
     * @param lastZ the initial control point's Z coordinate
     * @param coords the coordinates for the control points of a cubic
     *        B&eacute;zier curve as returned by
     *        {@link PathIterator3D#currentSegment(double[])}
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void
	setupU0ForTriangle(double lastX, double lastY, double lastZ,
			   double[] coords, double[] scoords, boolean reverse)
    {
	double pcoords[] = {lastX, lastY, lastZ,
			    coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5],
			    coords[6], coords[7], coords[8]};
	setupU0ForTriangle(pcoords, scoords, reverse);
    }

    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=0 (the edge where the coordinates (u,v) vary from (0,0) to (0,1)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>

     * The coordinates of the control points (P0, P1, P2, and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter v is 0.0, and the last control point is the
     * point on the curve where the parameter v is 1.0. There are a
     * total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (0,0,3), (0,1,2),(0,2,1), and (0,3,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords
     * array will match the (u,v) coordinates (0,0); when reverse is
     * true, the first control point will match (0,1).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when reverse
     * is true), will be placed at locations 003, 012, 021, and 030.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void setupU0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[0] = pcoords[i++];
	scoords[1] = pcoords[i++];
	scoords[2] = pcoords[i++];
	i += incr;
	scoords[3] = pcoords[i++];
	scoords[4] = pcoords[i++];
	scoords[5] = pcoords[i++];
	i += incr;
	scoords[6] = pcoords[i++];
	scoords[7] = pcoords[i++];
	scoords[8] = pcoords[i++];
	i += incr;
	scoords[9] = pcoords[i++];
	scoords[10] = pcoords[i++];
	scoords[11] = pcoords[i++];
    }

    /**
     * Set up offsetted elements of an array containing the coordinates
     * of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * u=0 (the edge where the coordinates (u,v) vary from (0,0) to (0,1)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2, and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter v is 0.0, and the last control point is the
     * point on the curve where the parameter v is 1.0. There are a
     * total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (0,0,3), (0,1,2),(0,2,1), and (0,3,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords
     * array will match the (u,v) coordinates (0,0); when reverse is
     * true, the first control point will match (0,1).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when 'reverse'
     * is true), will be placed at locations 003, 012, 021, and 030.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @param offset the offset into the scoords array, starting at which
     *        offset the results are stored
     * @see #addCubicTriangle(double[])
     */
    public static void setupU0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse, int offset)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[offset+0] = pcoords[i++];
	scoords[offset+1] = pcoords[i++];
	scoords[offset+2] = pcoords[i++];
	i += incr;
	scoords[offset+3] = pcoords[i++];
	scoords[offset+4] = pcoords[i++];
	scoords[offset+5] = pcoords[i++];
	i += incr;
	scoords[offset+6] = pcoords[i++];
	scoords[offset+7] = pcoords[i++];
	scoords[offset+8] = pcoords[i++];
	i += incr;
	scoords[offset+9] = pcoords[i++];
	scoords[offset+10] = pcoords[i++];
	scoords[offset+11] = pcoords[i++];
    }

    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=0 (the edge where the coordinates (u,v) vary from (0,0) to (1,0)),
     * using the arrays retrieved by PathIterator3D.
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>

     * The coordinates of the control points (P0, P1, P2, and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter v is 0.0, and the last control point is the
     * point on the curve where the parameter v is 1.0. There are a
     * total of four control points for this curve. The coordinates of
     * the first of these are provided as explicit arguments and the
     * coordinates of the rest are provided in an array.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (0,0,3), (1,0,2),(2,0,1), and (3,0,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point, given by the first
     * three arguments, will match the (u,v) coordinates (0,0); when
     * reverse is true, the first control point will match (1,0). The
     * coordinates for the first control point are lastX, lastY, and lastZ:
     * the names reflect the typical use of a path iterator in which the
     * end of the last segment, or coordinates specified by a SEG_MOVETO
     * operation, is used as the start of the next.
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by lastX,
     * lastY, lastZ, and the coords array, and read in the order
     * specified by the 'reverse' argument (P0 to P3 when 'reverse' is false
     * and P3 to P0 when reverse is true), will be placed at
     * locations 003, 102, 201, and 300  respectively.
     * @param lastX the first control point's X coordinate
     * @param lastY the first control point's Y coordinate
     * @param lastZ the first control point's Z coordinate
     * @param coords the coordinates for the control points of a cubic
     *        B&eacute;zier curve as returned by
     *        {@link PathIterator3D#currentSegment(double[])}
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void
	setupV0ForTriangle(double lastX, double lastY, double lastZ,
			   double[] coords, double[] scoords, boolean reverse)
    {
	double pcoords[] = {lastX, lastY, lastZ,
			    coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5],
			    coords[6], coords[7], coords[8]};
	setupV0ForTriangle(pcoords, scoords, reverse);
    }

    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=0 (the edge where the coordinates (u,v) vary from (0,0) to (1,0)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2, and P3) for
     * the cubic curve are listed sequentially, with X, Y, and Z
     * values for each control point grouped together and in that
     * order.  The first (O<sup>th</sup>) control point is the point on
     * the curve where the parameter v is 0.0, and the last control
     * point is the point on the curve where the parameter v is
     * 1.0. There are a total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (0,0,3), (1,0,2),(2,0,1), and (3,0,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords
     * array will match the (u,v) coordinates (0,0); when reverse is
     * true, the first control point will match (1,0).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when 'reverse'
     * is true), will be placed at locations 003, 102, 201, and 300.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void setupV0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[0] = pcoords[i++];
	scoords[1] = pcoords[i++];
	scoords[2] = pcoords[i++];
	i += incr;
	scoords[12] = pcoords[i++];
	scoords[13] = pcoords[i++];
	scoords[14] = pcoords[i++];
	i += incr;
	scoords[21] = pcoords[i++];
	scoords[22] = pcoords[i++];
	scoords[23] = pcoords[i++];
	i += incr;
	scoords[27] = pcoords[i++];
	scoords[28] = pcoords[i++];
	scoords[29] = pcoords[i++];
    }

    /**
     * Set up offseted elements of an array containing the coordinates of
     * a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * v=0 (the edge where the coordinates (u,v) vary from (0,0) to (1,0)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2, and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter v is 0.0, and the last control point is the
     * point on the curve where the parameter v is 1.0. There are a
     * total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (0,0,3), (1,0,2),(2,0,1), and (3,0,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords
     * array will match the (u,v) coordinates (0,0); when reverse is
     * true, the first control point will match (1,0).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when 'reverse'
     * is true), will be placed at locations 003, 102, 201, and 300.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @param offset the offset into the scoords array, starting at which
     *        offset the results are stored
     * @see #addCubicTriangle(double[])
     */
    public static void setupV0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse, int offset)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[offset+0] = pcoords[i++];
	scoords[offset+1] = pcoords[i++];
	scoords[offset+2] = pcoords[i++];
	i += incr;
	scoords[offset+12] = pcoords[i++];
	scoords[offset+13] = pcoords[i++];
	scoords[offset+14] = pcoords[i++];
	i += incr;
	scoords[offset+21] = pcoords[i++];
	scoords[offset+22] = pcoords[i++];
	scoords[offset+23] = pcoords[i++];
	i += incr;
	scoords[offset+27] = pcoords[i++];
	scoords[offset+28] = pcoords[i++];
	scoords[offset+29] = pcoords[i++];
    }

    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * w=0 (the edge where the coordinates (u,v) vary from (1,0) to (0,1))),
     * using the arrays retrieved by PathIterator3.
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2, and P3) for
     * the cubic curve are listed sequentially, with X, Y, and Z
     * values for each control point grouped together and in that
     * order.  The first (O<sup>th</sup>) control point is the point on
     * the curve where the parameter t is 0.0, and the last control
     * point is the point on the curve where the parameter t is
     * 1.0. There are a total of four control points for this curve.
     * The coordinates of the first of these are provided as explicit
     * arguments and the coordinates of the rest are provided in an
     * array.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (3,0,0), (2,1,0),(1,2,0), and (0,3,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point, given by the first
     * three arguments, will match the (u,v) coordinates (1,0); when reverse
     * is true, the first control point will match (0,1).   The
     * coordinates for the first control point are lastX, lastY, and lastZ:
     * the names reflect the typical use of a path iterator in which the
     * end of the last segment, or coordinates specified by a SEG_MOVETO
     * operation, is used as the start of the next.
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by lastX,
     * lastY, lastZ, and the coords array, and read in the order
     * specified by the 'reverse' argument (P0 to P3 when 'reverse' is
     * false and P3 to P0 when 'reverse' is true), will be placed at
     * locations 300, 210, 210, and 030.
     * @param lastX the initial control point's X coordinate
     * @param lastY the initial control point's Y coordinate
     * @param lastZ the initial control point's Z coordinate
     * @param coords the coordinates for the control points of a cubic
     *        B&eacute;zier curve as returned by
     *        {@link PathIterator3D#currentSegment(double[])}
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void
	setupW0ForTriangle(double lastX, double lastY, double lastZ,
			   double[] coords, double[] scoords, boolean reverse)
    {
	double pcoords[] = {lastX, lastY, lastZ,
			    coords[0], coords[1], coords[2],
			    coords[3], coords[4], coords[5],
			    coords[6], coords[7], coords[8]};
	setupW0ForTriangle(pcoords, scoords, reverse);
    }


    /**
     * Set up elements of an array containing the coordinates of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * w=0 (the edge where the coordinates (u,v) vary from (1,0) to (0,1)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2, and P3) for
     * the cubic curve are listed sequentially, with X, Y, and Z
     * values for each control point grouped together and in that
     * order.  The first (O<sup>th</sup>) control point is the point on
     * the curve where the parameter t is 0.0, and the last control
     * point is the point on the curve where the parameter t is
     * 1.0. There are a total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (3,0,0), (2,1,0),(1,2,0), and (0,3,0).
     * <P>
     * When the argument 'reverse' is false, the path control points
     * P<sub>0</sub>, P<sub>1</sub>, P<sub>2</sub>, and P<sub>3</sub> are
     * mapped to the cubic-triangle control points
     * P<sub>300</sub>, P<sub>210</sub>, P<sub>120</sub>, and P<sub>030</sub>
     * respectively. When the argument 'reverse' is true, the path
     * control points P<sub>3</sub>, P<sub>2</sub>, P<sub>1</sub>, and
     * P<sub>0</sub> are mapped to P<sub>300</sub>, P<sub>210</sub>,
     * P<sub>120</sub>, and P<sub>030</sub> respectively.
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords array
     * will match the (u,v) coordinates (1,0); when reverse
     * is true, the first control point will match (0,1).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when
     * 'reverse' is true), will be placed at locations 300, 210, 120,
     * and 030.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier triangle's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @see #addCubicTriangle(double[])
     */
    public static void setupW0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[27] = pcoords[i++];
	scoords[28] = pcoords[i++];
	scoords[29] = pcoords[i++];
	i += incr;
	scoords[24] = pcoords[i++];
	scoords[25] = pcoords[i++];
	scoords[26] = pcoords[i++];
	i += incr;
	scoords[18] = pcoords[i++];
	scoords[19] = pcoords[i++];
	scoords[20] = pcoords[i++];
	i += incr;
	scoords[9] = pcoords[i++];
	scoords[10] = pcoords[i++];
	scoords[11] = pcoords[i++];
    }

    /**
     * Set up offsetted elements of an array containing the coordinates
     * of a cubic
     * B&eacute;zier triangle's control points for those control points
     * corresponding to the edge of the patch for which the parameter
     * w=0 (the edge where the coordinates (u,v) vary from (1,0) to (0,1)).
     * B&eacute;zier triangle use barycentric coordinates (u,v,w) with
     * the constraint u+v+w = 1, and where u, v, and w are constrained
     * to lie in the interval [0,1]. Each edge is a cubic B&eacute;zier curve.
     * <P>
     * The coordinates of the control points (P0, P1, P2 and P3) for the
     * cubic curve are listed sequentially, with X, Y, and Z values
     * for each control point grouped together and in that order.  The
     * first (O<sup>th</sup>) control point is the point on the curve
     * where the parameter t is 0.0, and the last control point is the
     * point on the curve where the parameter t is 1.0. There are a
     * total of four control points for this curve.
     * <P>
     * A cubic B&eacute;zier triangle is specified by 10 control points.
     * These are labeled by a set of indices
     * &lambda;=(&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
     * where &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>=3
     * and where each component &lambda;<sub>i</sub> can be 0, 1, 2, or 3.
     * The control point indices, in the order in which they will appear in
     * the scoords array, are (0,0,3), (0,1,2),(0,2,1), (0,3,0),
     * (1,0,2), (1,1,1), (1,2,0), (2,0,1), (2,1,0), and (3,0,0).  The
     * indices for the control points set by this method are
     * (3,0,0), (2,1,0),(1,2,0), and (0,3,0).
     *<P>
     * For an oriented surface, a B&eacute;zier triangle's normal vector's
     * direction is set by using the right-hand rule with vertices
     * traversed in barycentric coordinates (u,v,w) from (0,0,1) to
     * (1, 0, 0) to (0, 1, 0). The corresponding edges satisfy the
     * constraints v = 0 and w = 0.  To continue from (0,1,0) back to
     * (0, 0, 1), the u=0 edge is traversed in the opposite direction.
     * The  the normal vector at the vertices is the normalized cross
     * product of tangent vectors at these vertices.
     * <UL>
     *   <LI> For the (0,0,1) vertex, one computes the cross product
     *        of the tangent for the v=0 edge with the tangent for
     *        the u=0 edge.
     *   <LI> For the (1,0,0) vertex one computes the cross product
     *        of the tangent for the w=0 edge with the tangent for
     *        the v=0 edge.
     *   <LI> For the (0,1,0) vertex one computes the cross product
     *        of the tangent for the u=0 edge with the tangent for
     *        the w=0 edge.
     * </UL>
     * After the cross products are computed, the resulting vector
     * is normalized. In each case the tangent vector is the
     * difference between an adjacent control point and the control
     * point at the vertex at which the tangent vector is computed.
     * <P>
     * When reverse is false, the first control point in the pcoords array
     * will match the (u,v) coordinates (1,0); when reverse
     * is true, the first control point will match (0,1).
     <P>
     * Given the following diagram:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * the control points P0, P1, P2, and P3, specified by the pcoords
     * array, and read in the order specified by the 'reverse'
     * argument (P0 to P3 when 'reverse' is false and P3 to P0 when 'reverse'
     * is true), will be placed at locations 300, 210, 120, and 030.
     * @param pcoords the coordinates for the control points of a cubic
     *        B&eacute;zier curve
     * @param scoords the coordinates for a cubic B&eacute;zier patch's
     *        control points
     * @param reverse true if the control points should be read from pcoords
     *        in reverse order (i.e., highest indices first); false for normal
     *        order (lowest indices first)
     * @param offset the offset into the scoords array, starting at which
     *        offset the results are stored
     * @see #addCubicTriangle(double[])
     */
    public static void setupW0ForTriangle(double[] pcoords, double[] scoords,
					  boolean reverse, int offset)
    {
	int i = reverse? 9: 0;
	int incr = reverse? -6: 0;

	scoords[offset+27] = pcoords[i++];
	scoords[offset+28] = pcoords[i++];
	scoords[offset+29] = pcoords[i++];
	i += incr;
	scoords[offset+24] = pcoords[i++];
	scoords[offset+25] = pcoords[i++];
	scoords[offset+26] = pcoords[i++];
	i += incr;
	scoords[offset+18] = pcoords[i++];
	scoords[offset+19] = pcoords[i++];
	scoords[offset+20] = pcoords[i++];
	i += incr;
	scoords[offset+9] = pcoords[i++];
	scoords[offset+10] = pcoords[i++];
	scoords[offset+11] = pcoords[i++];
    }

    private static final double f13 = 1.0/3.0;
    private static final double f276 = 27.0/6.0;

    /**
     * Set up the control point P<sub>111</sub>, the one that is not a
     * control point for the edge edge of a cubic B&eacute;zier
     * triangle, so that the surface at barycentric coordinates (1/3,
     * 1/3, 1/3) will go through a specified point.
     * <P>
     * The value set is dependent on the other control points being
     * previously configured.
     * @param x the X coordinate of the specified point
     * @param y the Y coordinate of the specified point
     * @param z the Z coordinate of the specified point
     * @param scoords the array holding the control-point coordinates
     */
    public static void
	setupCP111ForTriangle(double x, double y, double z, double[] scoords)
    {
	/*
	scoords[15] = x;
	scoords[16] = y;
	scoords[17] = z;
	*/
	scoords[15] = 0.0;
	scoords[16] = 0.0;
	scoords[17] = 0.0;
	double[] results = new double[3];

	Functions.Bernstein.sumB(results, scoords, 3, f13, f13, f13);
	/*
	System.out.format("no offset case: results = (%g, %g, %g)\n",
			  results[0], results[1], results[2]);
	*/
	scoords[15] = (x - results[0])*f276;
	scoords[16] = (y - results[1])*f276;
	scoords[17] = (z - results[2])*f276;
    }

    /**
     * Set up the control point P<sub>111</sub>, the one that is not a
     * control point for the edge edge of a cubic B&eacute;zier
     * triangle, so that the surface at barycentric coordinates (1/3,
     * 1/3, 1/3) will go through a specified point, with the point
     * stored in an offseted array.
     * <P>
     * The value set is dependent on the other control points being
     * previously configured.
     * @param x the X coordinate of the specified point
     * @param y the Y coordinate of the specified point
     * @param z the Z coordinate of the specified point
     * @param scoords the array holding the control-point coordinates
     * @param offset the offset into the scoords array, starting at which
     *        offset the results are stored
     */
    public static void
	setupCP111ForTriangle(double x, double y, double z, double[] scoords,
			      int offset)
    {
	scoords[offset+15] = 0.0;
	scoords[offset+16] = 0.0;
	scoords[offset+17] = 0.0;
	double[] results = new double[3];
	Functions.Bernstein.sumB(results, scoords, offset, 3, f13, f13, f13);
	scoords[offset+15] = (x - results[0])*f276;
	scoords[offset+16] = (y - results[1])*f276;
	scoords[offset+17] = (z - results[2])*f276;
    }


    private static final int lambdas[] = {1, 1, 1};

    /**
     * Set up the control point P<sub>111</sub>, the one that is not a
     * control point for the edge of a cubic B&eacute;zier triangle,
     * so that the surface at barycentric coordinates (u, v, 1-(u+v))
     * will go through a specified point.
     * The value of u and v must not be one that matches a point on
     * an edge.
     * <P>
     * The value set is dependent on the other control points being
     * previously configured.
     * @param x the X coordinate of the specified point
     * @param y the Y coordinate of the specified point
     * @param z the Z coordinate of the specified point
     * @param scoords the array holding the control-point coordinates
     * @param u the first barycentric coordinate
     * @param v the second barycentric coordinate
     * @exception IllegalArgumentException u and/or v are out of range
     */
    public static void
	setupCP111ForTriangle(double x, double y, double z, double[] scoords,
			      double u, double v)
	throws IllegalArgumentException
    {
	double w = 1.0 - (u + v);
	if (u <= 0.0 || v <= 0.0 || u >= 1.0 || v >= 1.0
	    || w <= 0.0 || w >= 1.0) {
	    throw new
		IllegalArgumentException(errorMsg("argOutOfRange12", u, v));
	}

	scoords[15] = 0.0;
	scoords[16] = 0.0;
	scoords[17] = 0.0;

	double[] results = new double[3];
	Functions.Bernstein.sumB(results, scoords, 3, u, v, w);
	double scale = Functions.B(3, lambdas, u, v, w);
	scoords[15] = (x - results[0]) /scale;
	scoords[16] = (y - results[1]) /scale;
	scoords[17] = (z - results[2]) /scale;
    }

    /**
     * Set the control point P<sub>111</sub>,the one that is not a
     * control point for edge of a cubic B&eacute;zier triangle, based
     * on a quadratic.
     * The value for this control point is
     * (P<sub>012</sub>+P<sub>021</sub>+P<sub>102</sub>+P<sub>120</sub>+P<sub>201</sub>+P<sub>210</sub>)/4.0
     * + (P<sub>003</sub>+P<sub>030</sub>+P<sub>300</sub>)/6.0, as
     * describe in
     * <A HREF="http://www.gamasutra.com/view/feature/131389/b%C3%A9zier_triangles_and_npatches.php?print=1">B&eacute;zier Triangles and N-Patches</A>,
     * Farin, Gerald, Curves and Surfaces for Computer Aided Geometric
     * Design--A Practical Guide, Fourth Edition (First Edition,
     * 1988), Academic Press Inc., 1996, and Farin, Gerald,
     * "Triangular Bernstein-B&eacute;zier Patches," Computer Aided Geometric
     * Design, vol. 3, no. 2, pp. 83-127, 1986.
     * <P>
     * The value set is dependent on the other control points being
     * previously configured.
     *
     * @param scoords the array holding the control-point coordinates
     */
    public static void setupCP111ForTriangle(double[] scoords) {
	scoords[15] = (scoords[3] + scoords[6] + scoords[12]
		       + scoords[18] + scoords[21] + scoords[24])/4.0
	    - (scoords[0] + scoords[9] + scoords[27])/6.0;
	scoords[16] = (scoords[4] + scoords[7] + scoords[13]
		       + scoords[19] + scoords[22] + scoords[25])/4.0
	    - (scoords[1] + scoords[10] + scoords[28])/6.0;
	scoords[17] = (scoords[5] + scoords[8] + scoords[14]
		       + scoords[20] + scoords[23] + scoords[26])/4.0
	    - (scoords[2] + scoords[11] + scoords[29])/6.0;
    }


    /**
     * Set up the control point P<sub>111</sub>, the one that is not a
     * control point for an edge of the cubic B&eacute;zier triangle,
     * so that the surface at barycentric coordinates (1/3, 1/3, 1/3)
     * will go through a point on the plane passing through the
     * vertices of the the cubic B&eacute;zier triangle.
     * This point will have the
     * value (v<sub>1</sub> + v<sub>2</sub> + v<sub>3</sub>)/3
     * where v<sub>1</sub>, v<sub>2</sub>, and v<sub>3</sub> are
     * the vertices of the cubic B&eacute;zier triangle.
     * <P>
     * This method is useful when a cubic B&eacute;zier triangle,
     * including its edges, should lie in a plane.
     * @param scoords the array holding the control-point coordinates
     */
    public static void setupPlanarCP111ForTriangle(double[] scoords) {
        scoords[15] = (scoords[0] + scoords[27] + scoords[9])/3.0;
	scoords[16] = (scoords[1] + scoords[28] + scoords[10])/3.0;
	scoords[17] = (scoords[2] + scoords[29] + scoords[11])/3.0;
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1]. 
     * <P>
     * The control points, labeled by their three indices, are located
     * as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \ 
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     * 
     * </pre>
     * </blockquote>
     * The orientation of the triangle is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction of a
     * normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * For a valid triangle, all vertices must be located at different
     * points.
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     */
    public void addCubicTriangle(double[] controlPoints) {
	addCubicTriangle(controlPoints, null, null);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, reversing the
     * triangle's orientation by exchanging its U and V coordinates.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * Before the orientation is reversed, the control points, labeled
     * by their three indices, are located as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * The orientation of the triangle before the triangle's
     * orientation is reversed is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction opposite
     * to that of a normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (u,v,w)
     * for the non-flipped triangle.
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     */
    public void addFlippedCubicTriangle(double[] controlPoints) {
	addFlippedCubicTriangle(controlPoints, null, null);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying a color.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * The control points, labeled by their three indices, are located
     * as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * The orientation of the triangle is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction of a
     * normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * For a valid triangle, all vertices must be located at different
     * points.
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     * @param color the triangle's color; null if none is specified
     */
    public void addCubicTriangle(double[] controlPoints, Color color) {
	addCubicTriangle(controlPoints, color, null);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying
     * a color and reversing the triangle's orientation by exchanging
     * its U and V coordinates.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * Before the orientation is reversed, the control points, labeled
     * by their three indices, are located as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * The orientation of the triangle before the triangle's
     * orientation is reversed is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction opposite
     * to that of a normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (u,v,w)
     * for the non-flipped triangle.
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     * @param color the triangle's color; null if non is specified
     */
    public void addFlippedCubicTriangle(double[] controlPoints, Color color) {
	addFlippedCubicTriangle(controlPoints, color, null);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying a tag.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * The control points, labeled by their three indices, are located
     * as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     * </pre>
     * </blockquote>
     * The orientation of the triangle is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction of a
     * normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * For a valid triangle, all vertices must be located at different
     * points.
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     */
    public void addCubicTriangle(double[] controlPoints, Object tag) {
	addCubicTriangle(controlPoints, null, tag);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying
     * a tag and reversing the triangle's orientation by exchanging
     * its U and V coordinates.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * Before the orientation is reversed, the control points, labeled
     * by their three indices, are located as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     *
     * </pre>
     * </blockquote>
     * The orientation of the triangle before the triangle's
     * orientation is reversed is determined by applying the
     * right-hand rule, starting with the edge (0,0)--&gt;(1,0) and
     * ending with the edge (0,0)--&gt;(0,1), where the ordered pairs
     * represent the coordinates (u,v). More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction opposite
     * to that of a normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (u,v,w)
     * for the non-flipped triangle.
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     * @param tag the triangle's tag; null if none is specified
     */
    public void addFlippedCubicTriangle(double[] controlPoints, Object tag) {
	addFlippedCubicTriangle(controlPoints, null, tag);
    }

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying a color
     * and a tag.
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * The control points, labeled by their three indices, are located
     * as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     * </pre>
     * </blockquote>
     * The orientation of the triangle is determined by applying the
     * right-hand rule, when traversing the vertices whose (u,v)
     * values are (0,0), (1,0), and (0,1) in that order. More
     * precisely, for an orientated surface at a point (u, v, 1 - u -
     * v), the cross product of a tangent vector in the direction of
     * increasing u with v constant and the tangent vector in the
     * direction of increasing v with u constant points in the
     * direction of a normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     * @param color the color for the triangle; null if none is specified
     * @param tag a tag naming this triangle
     */
    public abstract void addCubicTriangle(double[] controlPoints,
					  Color color,
					  Object tag);

    /**
     * Add a cubic B&eacute;zier triangle to this surface, specifying
     * a color and a tag, and reversing the triangle's orientation by
     * exchanging its U and V coordinates,
     * Cubic B&eacute;zier triangles uses barycentric coordinates
     * u, v, and w, where u + v + w = 1 and all three coordinates
     * are in the range [0,1].
     * <P>
     * Before the orientation is reversed, the control points,
     * labeled by their three indices, are located as follows:
     * <blockquote>
     * <pre>
     *                  (0,1)
     *                   030
     *                    *
     *                   / \
     *                  /   \
     *                 /     \
     *                /       \
     *           021 *---------* 120
     *              / \       / \
     *    [u = 0]  /   \     /   \  [w = 0]
     *   (v axis) /     \   /     \
     *           /    111\ /       \
     *      012 *---------*---------* 210
     *         / \       / \       / \
     *        /   \     /   \     /   \
     *       /     \   /     \   /     \
     *      /       \ /       \ /       \
     *     *---------*---------*---------*
     *    003       102       201       300
     *  (0,0)                          (1,0)
     *                 [v = 0]
     *                 (u axis)
     * </pre>
     * </blockquote>
     * The orientation of the triangle, again before the orientation
     * is reversed, is determined by applying the right-hand rule,
     * when traversing the vertices whose (u,v) values are (0,0),
     * (1,0), and (0,1) in that order. More precisely, for an
     * orientated surface at a point (u, v, 1 - u - v), the cross
     * product of a tangent vector in the direction of increasing u
     * with v constant and the tangent vector in the direction of
     * increasing v with u constant points in the direction opposite
     * to that of a normal to the surface.
     * <P>
     * The controlPoints array contains the control points whose
     * indices are 003, 012, 021, 030, 102, 111, 120, 201, 210, 300,
     * listed in that order with the X, Y, and Z values for each
     * control point specified as adjacent array entries, again in
     * that order.
     * <P>
     * If an edge of the segment to be added is a straight line,
     * one should use {@link Path3D#setupCubic(Point3D,Point3D)} or
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * to obtain the control points that lie along that edge (while
     * contiguous in the array returned by the toCubic method, the
     * locations at which these values should be placed in the
     * controlPoints array may not be contiguous).
     * <P>
     * Several methods exit to help set up the control points. These
     * include
     * <UL>
     *   <LI>{@link #setupU0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double[],double[],boolean)}
     *   <LI>{@link #setupU0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupV0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupW0ForTriangle(double,double,double,double[],double[],boolean)}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[])}
     *   <LI>{@link #setupCP111ForTriangle(double,double,double,double[],double,double)}
     *   <LI>{@link #setupCP111ForTriangle(double[])}
     *   <LI>{@link #setupPlanarCP111ForTriangle(double[])}
     * </UL>
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (u,v,w)
     * for the non-flipped triangle.
     * @param controlPoints the control points for a B&eacute;zier
     *        triangle
     * @param color the color for the triangle; null if none is specified
     * @param tag a tag naming this triangle
     */
    public abstract void addFlippedCubicTriangle(double[] controlPoints,
						 Color color,
						 Object tag);

    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * As a result, applying the right hand rule using the ordering of the
     * control points (v<sub>1</sub> to v<sub>3</sub> to v<sup>3</sub> yields
     * an orientation opposite to that of the triangle.
     * It is not the same as the ordering used for
     * It is not the same as the ordering used for
     * {@link #addPlanarTriangle(double,double,double,double,double,double,double,double,double)}.
     * @param controlPoints the vertices of the triangle
     */
    public void addPlanarTriangle(double[] controlPoints) {
	addPlanarTriangle(controlPoints, null, null);
    }

    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array and reversing the triangle's orientation.
     * Before the triangle's orientation is reversed, the control points
     * represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * For a flipped triangle, the order of the control points determines
     * the orientation using the right hand rule for traversing
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param controlPoints the vertices of the triangle
     */
    public void addFlippedPlanarTriangle(double[] controlPoints) {
	addFlippedPlanarTriangle(controlPoints, null, null);
    }


    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array and color.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * As a result, applying the right hand rule using the ordering of the
     * control points (v<sub>1</sub> to v<sub>3</sub> to v<sup>3</sub> yields
     * an orientation opposite to that of the triangle.
     * It is not the same as the ordering used for
     * {@link #addPlanarTriangle(double,double,double,double,double,double,double,double,double,Color)}.
     * @param controlPoints the vertices of this triangle
     * @param color the color of this triangle; null if none is specified
     */
    public void addPlanarTriangle(double[] controlPoints, Color color) {
	addPlanarTriangle(controlPoints, color, null);
    }

    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array and color, and reversing the triangle's orientation.
     * Before the triangle's orientation is reversed, the control points
     * represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * It is not the same as the ordering used for
     * {@link #addPlanarTriangle(double,double,double,double,double,double,double,double,double,Color)}.
     * For a flipped triangle, the order of the control points determines
     * the orientation using the right hand rule for traversing
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param controlPoints the vertices of this triangle
     * @param color the color of this triangle; null if none is specified
     */
    public void addFlippedPlanarTriangle(double[] controlPoints, Color color) {
	addFlippedPlanarTriangle(controlPoints, color, null);
    }
    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array and a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * As a result, applying the right hand rule using the ordering of the
     * control points (v<sub>1</sub> to v<sub>3</sub> to v<sup>3</sub> yields
     * an orientation opposite to that of the triangle.
     * It is not the same as the ordering used for
     * It is not the same as the ordering used for
     * {@link #addPlanarTriangle(double,double,double,double,double,double,double,double,double,Object)}.
     * @param controlPoints the vertices of this triangle
     * @param tag a tag naming this triangle
     */
    public void addPlanarTriangle(double[] controlPoints, Object tag) {
	addPlanarTriangle(controlPoints, null, tag);
    }

    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array and tag, and reversing the triangle's orientation.
     * Before the triangle's orientation is reversed, the control points
     * represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * For a flipped triangle, the order of the control points determines
     * the orientation using the right hand rule for traversing
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param controlPoints the vertices of this triangle
     * @param tag a tag naming this triangle; null if there is none
     */
    public void addFlippedPlanarTriangle(double[] controlPoints, Object tag) {
	addFlippedPlanarTriangle(controlPoints, null, tag);
    }

    /**
     * Add a planner triangle to the surface, specifying a control-point
     * array, a color, and a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The control-points array stores the vertices in the following
     * order, so as to match the convention for barycentric coordinates
     * used for cubic triangles.
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * This order matches the ordering used for a cubic Bezier triangle.
     * As a result, applying the right hand rule using the ordering of the
     * control points (v<sub>1</sub> to v<sub>3</sub> to v<sup>3</sub> yields
     * an orientation opposite to that of the triangle.
     * It is not the same as the ordering used for
     * {@link #addPlanarTriangle(double,double,double,double,double,double,double,double,double,Color,Object)}.
     * @param controlPoints the vertices of the triangle
     * @param color the color of this triangle; null if none is specified
     * @param tag a tag naming this triangle; null if there is none
     */
    public abstract void addPlanarTriangle(double[] controlPoints,
					   Color color,
					   Object tag);

    /**
     * Add a planner triangle to the surface, reversing the triangle's
     * orientation by exchanging the triangle's U and V coordinates,
     * and specifying a control-point array, a color, and a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The the vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v).  All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1</sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * Before the triangle's orientation is reversed, the control-points
     * array stores the vertices in the following order so as to match
     *the convention for barycentric coordinates used for cubic triangles:
     * <UL>
     *   <li>controlPoints[0], controlPoints[1], controlPoints[2] -
     *      the X, Y and Z coordinates respectively for the first
     *      vertex.
     *   <li>controlPoints[3], controlPoints[4], controlPoints[5] -
     *      the X, Y and Z coordinates respectively for the third
     *      vertex.
     *   <li>controlPoints[6], controlPoints[7], controlPoints[8] -
     *      the X, Y and Z coordinates respectively for the second
     *      vertex.
     * </UL>
     * For a flipped triangle, the order of the control points determines
     * the orientation using the right hand rule for traversing
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param controlPoints the vertices of the triangle
     * @param color the color of this triangle; null if none is specified
     * @param tag a tag naming this triangle; null if there is none
     */
    public abstract void addFlippedPlanarTriangle(double[] controlPoints,
						  Color color,
						  Object tag);

    /**
     * Add a planner triangle to the surface, specifying the coordinates
     * of the vertices.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The orientation of the triangle is that given by the right hand rule
     * when traversing the triangle from v<sub>1</sub> to v<sub>2</sub> to
     * v<sub>3</sub>, which is the same order as the arguments. This differs
     * from the convention used for {@link #addPlanarTriangle(double[])}.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     */
    public synchronized void addPlanarTriangle(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3)
    {
	addPlanarTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, null, null);
    }

    /**
     * Add a planner triangle to the surface, reversing the triangle's
     * orientation and specifying the coordinates of the vertices.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The vertices are numbered so that the right
     * hand rule indicates an outward direction before the triangle's
     * orientation is reversed, in order to be consistent with the
     * representation used in the org.bzdev.p3d package (which is based
     * on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * For a flipped triangle the orientation is given by using the
     * right hand rule when traversing the vertices from
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2</sub>, the opposite of a
     * non-flipped triangle created with the same arguments.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     */
    public synchronized void
	addFlippedPlanarTriangle(double x1, double y1, double z1,
				 double x2, double y2, double z2,
				 double x3, double y3, double z3)
    {
	addFlippedPlanarTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3,
				 null, null);
    }

    /**
     * Add a planner triangle to the surface, specifying the coordinates
     * of the vertices and specifying a color.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The orientation of the triangle is that given by the right hand rule
     * when traversing the triangle from v<sub>1</sub> to v<sub>2</sub> to
     * v<sub>3</sub>, which is the same order as the arguments. This differs
     * from the convention used for {@link #addPlanarTriangle(double[],Color)}.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param color the color of this triangle; null if none is specified
     */
    public synchronized void addPlanarTriangle(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3,
					       Color color)
    {
	addPlanarTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, color, null);
    }


    /**
     * Add a planner triangle to the surface, reversing the triangle's
     * orientation, specifying the coordinates of the vertices, and
     * specifying a color.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the
     * points respectively. The vertices are numbered so that, before
     * the orientation is reversed, the right hand rule indicates an
     * outward direction in order to be consistent with the
     * representation used in the org.bzdev.p3d package (which is
     * based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * For a flipped triangle the orientation is given by using the
     * right hand rule when traversing the vertices from
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2</sub>, the opposite of a
     * non-flipped triangle created with the same arguments.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param color the color of this triangle; null if none is specified
     */
    public synchronized void
	addFlippedPlanarTriangle(double x1, double y1, double z1,
				 double x2, double y2, double z2,
				 double x3, double y3, double z3,
				 Color color)
    {
	addFlippedPlanarTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3,
				 color, null);
    }

    /**
     * Add a planner triangle to the surface, specifying the coordinates
     * of the vertices and providing a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * <P>
     * The orientation of the triangle is that given by the right hand rule
     * when traversing the triangle from v<sub>1</sub> to v<sub>2</sub> to
     * v<sub>3</sub>, which is the same order as the arguments. This differs
     * from the convention used for
     * {@link #addPlanarTriangle(double[],Object)}.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param tag a tag naming this triangle
     */
    public synchronized void addPlanarTriangle(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3,
					       Object tag)
    {
	addPlanarTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3, null, tag);
    }


    /**
     * Add a planner triangle to the surface, reversing the triangle's
     * orientation, specifying the coordinates of the vertices, and
     * providing a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the
     * points respectively. The vertices are numbered so that, before
     * the orientation is reversed, the right hand rule indicates an
     * outward direction in order to be consistent with the
     * representation used in the org.bzdev.p3d package (which is
     * based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * For a flipped triangle the orientation is given by using the
     * right hand rule when traversing the vertices from
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2</sub>, the opposite of a
     * non-flipped triangle created with the same arguments.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param tag a tag naming this triangle
     */
    public synchronized void
	addFlippedPlanarTriangle(double x1, double y1, double z1,
				 double x2, double y2, double z2,
				 double x3, double y3, double z3,
				 Object tag)
    {
	addFlippedPlanarTriangle(x1, x2, z1, x2, y2, z2, x3, y3, z3,
				 null, tag);
    }

    /**
     * Add a planner triangle to the surface, specifying the coordinates
     * of the vertices, a color, and a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the points
     * respectively. The vertices are numbered so that the right
     * hand rule indicates an outward direction in order to be consistent
     * with the representation used in the org.bzdev.p3d package (which
     * is based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * The orientation of the triangle is that given by the right hand rule
     * when traversing the triangle from v<sub>1</sub> to v<sub>2</sub> to
     * v<sub>3</sub>, which is the same order as the arguments. This differs
     * from the convention used for
     * {@link #addPlanarTriangle(double[],Color,Object)}.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param color the color of this triangle; null if none is specified
     * @param tag a tag naming this triangle
     */
    public synchronized void addPlanarTriangle(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3,
					       Color color,
					       Object tag)
    {

	tmpCoords[0] = x1;
	tmpCoords[1] = y1;
	tmpCoords[2] = z1;
	tmpCoords[3] = x3;
	tmpCoords[4] = y3;
	tmpCoords[5] = z3;
	tmpCoords[6] = x2;
	tmpCoords[7] = y2;
	tmpCoords[8] = z2;
	addPlanarTriangle(tmpCoords, color, tag);
    }


    /**
     * Add a planner triangle to the surface, reversing the triangle's
     * orientation, and specifying the coordinates of the vertices, a color,
     * and a tag.
     * The control points represent three vertices each represented by
     * three array elements for the X, Y, and Z coordinates of the
     * points respectively. The vertices are numbered so that, before
     * the orientation is reversed, the right hand rule indicates an
     * outward direction in order to be consistent with the
     * representation used in the org.bzdev.p3d package (which is
     * based on the ordering required in STL files):
     * <blockquote><pre>
     *
     *              * v<sub>3</sub>
     *             / \
     *            /   \
     *  v axis   /     \  [w = 0]
     *  [u = 0] /       \
     *         /         \
     *     v<sub>1</sub> *-----------* v<sub>2</sub>
     *          u axis
     *          [v = 0]

     * </pre></blockquote>
     * Points on the triangle can be expressed in barycentric coordinates
     * u, v, and w. These have a constraint u + v + w = 1.  By convention,
     * we will use u and v and set w = 1 - (u + v). All three coordinates are
     * restricted to the range [0,1]. In barycentric coordinates, a point
     * p will be located at p = wv<sub>1<sub> +uv<sub>3</sub> + vv<sub>2</sub>.
     * For a flipped triangle the orientation is given by using the
     * right hand rule when traversing the vertices from
     * v<sub>1</sub> to v<sub>3</sub> to v<sub>2</sub>, the opposite of a
     * non-flipped triangle created with the same arguments.
     * <P>
     * For a flipped and non-flipped triangle created using the same
     * arguments, a point on the surface at coordinates (v,u,w) for the
     * flipped triangle is the same as the point at coordinates (v,u,w)
     * for the non-flipped triangle.
     * @param x1 the X coordinate of vertex v<sub>1</sub>
     * @param y1 the Y coordinate of vertex v<sub>1</sub>
     * @param z1 the Z coordinate of vertex v<sub>1</sub>
     * @param x2 the X coordinate of vertex v<sub>2</sub>
     * @param y2 the Y coordinate of vertex v<sub>2</sub>
     * @param z2 the Z coordinate of vertex v<sub>2</sub>
     * @param x3 the X coordinate of vertex v<sub>3</sub>
     * @param y3 the Y coordinate of vertex v<sub>3</sub>
     * @param z3 the Z coordinate of vertex v<sub>3</sub>
     * @param color the color of this triangle; null if none is specified
     * @param tag a tag naming this triangle; null if there is none
     */
    public synchronized void
	addFlippedPlanarTriangle(double x1, double y1, double z1,
				 double x2, double y2, double z2,
				 double x3, double y3, double z3,
				 Color color,
				 Object tag)
    {

	tmpCoords[0] = x1;
	tmpCoords[1] = y1;
	tmpCoords[2] = z1;
	tmpCoords[3] = x3;
	tmpCoords[4] = y3;
	tmpCoords[5] = z3;
	tmpCoords[6] = x2;
	tmpCoords[7] = y2;
	tmpCoords[8] = z2;
	addFlippedPlanarTriangle(tmpCoords, color, tag);
    }

    private static double[]tmptp1 = new double[12];
    private static double[]tmptp2 = new double[12];

    /**
     * Convert the control-point coordinates for a cubic B&eacute;zier
     * triangle into those for a cubic B&eacute;zier patch that has
     * the same shape.
     * <P>
     * The algorithm citation is
     * <A HREF="http://cg.cs.tsinghua.edu.cn/~shimin/pdf/cagd%202001_bezier.pdf">
     * Shi-Min Hu, "Conversion between triangular and rectangular
     * B&eacute;zier patches" Computer Aided Design 18 (2001) 667&ndash;671
     * </A>. The rationale for providing this method is that
     * the X3D file format supports B&eacute;zier patches but not
     * B&eacute;zier triangles, so converting a B&eacute;zier triangle
     * to a B&eacute;zier patch allows this segment of a surface to
     * be described using X3D constructs with no loss of accuracy.
     * the algorithm maps barycentric coordinates (u, v, w), where
     * u + v + w = 1, to patch coordinates (u',v') where
     * u' = u and v' = v/(1-u) = v/(v+w).
     * @param tcoords the coordinates for a cubic B&eacute;zier triangle
     * @param toffset the offset into the tcoords array for the start of
     *        the control-point coordinates for the cubic B&eacute;zier triangle
     * @param pcoords the coordinates for a cubic B&eacute;zier patch
     * @param poffset the offset into the tcoords array for the start of
     *        the control-point coordinates for the cubic B&eacute;zier patch
     */
    public static synchronized void
	triangleToPatch(double[] tcoords, int toffset,
			double[] pcoords, int poffset)
    {
	for (int i = 0; i < 3; i++) {
	    int it = toffset + i;
	    int ip = poffset + i;
	    pcoords[ip] = tcoords[it];
	    pcoords[12+ip] = tcoords[3+it];
	    pcoords[24+ip] = tcoords[6+it];
	    pcoords[36+ip] = tcoords[9+it];
	    pcoords[9+ip] = tcoords[27+it];
	    pcoords[21+ip] = tcoords[27+it];
	    pcoords[33+ip] = tcoords[27+it];
	    pcoords[45+ip] = tcoords[27+it];
	    tmptp1[i] = tcoords[12+it];
	    tmptp1[3+i] = tcoords[15+it];
	    tmptp1[6+i] = tcoords[18+it];
	}
	Path3DInfo.elevateDegree(2, tmptp2, 0, tmptp1, 0);
	for (int i = 0; i < 3; i++) {
	    int ip = poffset + i;
	    pcoords[3 +ip] = tmptp2[i];
	    pcoords[15 +ip] = tmptp2[3+i];
	    pcoords[27 +ip] = tmptp2[6+i];
	    pcoords[39 +ip] = tmptp2[9+i];
	    int it = toffset + i;
	    tmptp1[i] = tcoords[21+it];
	    tmptp1[3+i] = tcoords[24+it];
	}
	Path3DInfo.elevateDegree(1, tmptp2, 0, tmptp1, 0);
	Path3DInfo.elevateDegree(2, tmptp1, 0, tmptp2, 0);
	for (int i = 0; i < 3; i++) {
	    int ip = poffset + i;
	    pcoords[6+ip] = tmptp1[i];
	    pcoords[18+ip] = tmptp1[3+i];
	    pcoords[30+ip] = tmptp1[6+i];
	    pcoords[42+ip] = tmptp1[9+i];
	}
    }

    /**
     * Add a cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     */
    public void addCubicVertex(double[] controlPoints) {
	addCubicVertex(controlPoints, null, null);
    }

    /**
     * Add a flipped cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the
     * oriented side of the surface, this traversal will go clockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     */
    public void addFlippedCubicVertex(double[] controlPoints) {
	addFlippedCubicVertex(controlPoints, null, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface,
     * this traversal will go counterclockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param color the color of this surface segment
     */
    public void addCubicVertex(double[] controlPoints, Color color) {
	addCubicVertex(controlPoints, color, null);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface,
     * this traversal will go clockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param color the color of this surface segment
     */
    public void addFlippedCubicVertex(double[] controlPoints, Color color) {
	addFlippedCubicVertex(controlPoints, color, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface,
     * this traversal will go counterclockwise.

     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param tag the tag of this surface segment
     */
    public void addCubicVertex(double[] controlPoints, Object tag) {
	addCubicVertex(controlPoints, null, tag);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally to back P<sub>0</sub>. When viewed from the oriented
     * side  the surface, this traversal will go clockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param tag the tag of this surface segment
     */
    public void addFlippedCubicVertex(double[] controlPoints, Object tag) {
	addFlippedCubicVertex(controlPoints, null, tag);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param color the tag of this surface segment
     * @param tag the tag for this surface segment
     */
    public abstract void addCubicVertex(double[] controlPoints,
					Color color,
					Object tag);

    /**
     * Add a flipped cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * @param controlPoints the control points, in order, with each
     *        represented by three array elements containing a control
     *        point's X coordinate, followed by its Y coordinate, followed
     *        by its Z coordinate
     * @param color the color of this surface segment
     * @param tag the tag for this surface segment
     */
    public abstract void addFlippedCubicVertex(double[] controlPoints,
					       Color color,
					       Object tag);

    /**
     * Add a cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3,
			       double x4, double y4, double z4,
			       double x5, double y5, double z5)
    {
	addCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, null, null);
    }

    /**
     * Add a flipped cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      double x3, double y3, double z3,
				      double x4, double y4, double z4,
				      double x5, double y5, double z5)
    {
	addFlippedCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, null, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param color the color for this segment
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3,
			       double x4, double y4, double z4,
			       double x5, double y5, double z5,
			       Color color)
    {
	addCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, color, null);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param color the color for this segment
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      double x3, double y3, double z3,
				      double x4, double y4, double z4,
				      double x5, double y5, double z5,
				      Color color)
    {
	addFlippedCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, color, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param tag the tag for this segment
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3,
			       double x4, double y4, double z4,
			       double x5, double y5, double z5,
			       Object tag)
    {
	addCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, null, tag);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface,* this traversal will go clockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param tag the tag for this segment
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      double x3, double y3, double z3,
				      double x4, double y4, double z4,
				      double x5, double y5, double z5,
				      Object tag)
    {
	addFlippedCubicVertex(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
		       x5, y5, z5, null, tag);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented side
     * of the surface, this traversal will go counterclockwise.
     * P<sub>4</sub> and back to the start
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param color the color for this segment
     * @param tag the tag for this segment
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       double x2, double y2, double z2,
			       double x3, double y3, double z3,
			       double x4, double y4, double z4,
			       double x5, double y5, double z5,
			       Color color, Object tag)
    {
	double coords[] = {x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
			   x5, y5, z5};
	addCubicVertex(coords, color, tag);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>. When viewed from the oriented
     * side of the surface,* this traversal will go clockwise.
     * @param x1 the x coordinate for P<sub>0</sub>
     * @param y1 the y coordinate for P<sub>0</sub>
     * @param z1 the x coordinate for P<sub>0</sub>
     * @param x2 the x coordinate for P<sub>1</sub>
     * @param y2 the y coordinate for P<sub>1</sub>
     * @param z2 the x coordinate for P<sub>1</sub>
     * @param x3 the x coordinate for P<sub>2</sub>
     * @param y3 the y coordinate for P<sub>2</sub>
     * @param z3 the x coordinate for P<sub>2</sub>
     * @param x4 the x coordinate for P<sub>3</sub>
     * @param y4 the y coordinate for P<sub>3</sub>
     * @param z4 the x coordinate for P<sub>3</sub>
     * @param x5 the x coordinate for P<sub>4</sub>
     * @param y5 the y coordinate for P<sub>4</sub>
     * @param z5 the x coordinate for P<sub>4</sub>
     * @param color the color of this segment
     * @param tag the tag for this segment
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      double x3, double y3, double z3,
				      double x4, double y4, double z4,
				      double x5, double y5, double z5,
				      Color  color, Object tag)
    {
	double coords[] = {x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
			   x5, y5, z5};
	addFlippedCubicVertex(coords, color, tag);
    }

    double tmp1[] = new double[9];
    double tmp2[] = new double[9];

    /**
     * Add a cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       int type, double[] coords,
			       double vx, double vy, double vz)
    {
	addCubicVertex(x1, y1, z1, type, coords, vx, vy, vz, null, null);
    }

    /**
     * Add a flipped cubic vertex to this surface.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      int type, double[] coords,
				      double vx, double vy, double vz)
    {
	addFlippedCubicVertex(x1, y1, z1, type, coords, vx, vy, vz, null, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param color the color of the new segment
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       int type, double[] coords,
			       double vx, double vy, double vz,
			       Color color)
    {
	addCubicVertex(x1, y1, z1, type, coords, vx, vy, vz, color, null);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a color.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param color the color of the new segment
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      int type, double[] coords,
				      double vx, double vy, double vz,
				      Color color)
    {
	addFlippedCubicVertex(x1, y1, z1, type, coords, vx, vy, vz,
			      color, null);
    }

    /**
     * Add a cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param tag the tag for the new segment
     */
    public void addCubicVertex(double x1, double y1, double z1,
			       int type, double[] coords,
			       double vx, double vy, double vz,
			       Object tag)
    {
	addCubicVertex(x1, y1, z1, type, coords, vx, vy, vz, null, tag);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param tag the tag for the new segment
     */
    public void addFlippedCubicVertex(double x1, double y1, double z1,
				      int type, double[] coords,
				      double vx, double vy, double vz,
				      Object tag)
    {
	addFlippedCubicVertex(x1, y1, z1, type, coords, vx, vy, vz,
			      null, tag);
    }

    /**
     * Add a cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go counterclockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param color the color for the new segment
     * @param tag the tag for the new segment
     */
    public synchronized void addCubicVertex(double x1, double y1, double z1,
					    int type, double[] coords,
					    double vx, double vy, double vz,
					    Color color, Object tag)
    {
	switch (type) {
	case PathIterator3D.SEG_LINETO:
	    Path3DInfo.elevateDegree(1, tmp1, 0, coords, 0);
	    Path3DInfo.elevateDegree(2, tmp2, 0, tmp1, 0);
	    coords = tmp2;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    Path3DInfo.elevateDegree(2, tmp1, 0, coords, 0);
	    coords = tmp1;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	addCubicVertex(x1, y1, z1,
		       coords[0], coords[1], coords[2],
		       coords[3], coords[4], coords[5],
		       coords[6], coords[7], coords[8],
		       vx, vy, vz, color, tag);
    }

    /**
     * Add a flipped cubic vertex to this surface, specifying a color and tag.
     * A cubic vertex has five control points, P<sub>0</sub>,
     * P<sub>1</sub>, P<sub>2</sub>, P<sub>3</sub>, and
     * P<sub>4</sub>. The first four control points are the control
     * points for a cubic B&eacute;zier curve and the fifth control
     * point P<sub>4</sub> is the control point of a vertex connected
     * points along the B&eacute;zier curve by straight lines.  The
     * orientation of this surface is that obtained by using the right
     * hand rule when following the B&eacute;zier curve from
     * P<sub>0</sub> to P<sub>3</sub>, then proceeding to P<sub>4</sub>,
     * and finally back to P<sub>0</sub>.  When viewed from the oriented
     * side of the surface, this traversal will go clockwise.
     * <P>
     * The number of entries in the coords array (argument 5) is 3
     * when the type is {@link PathIterator3D#SEG_LINETO}, 6
     * when the type is {@link PathIterator3D#SEG_QUADTO}, and 9
     * when the type is {@link PathIterator3D#SEG_CUBICTO}. The format
     * for the coords array is that specified for
     * {@link PathIterator3D#currentSegment(double[])}.
     * @param x1 the X coordinate for the control point P<sub>0</sub>
     * @param y1 the Y coordinate for the control point P<sub>0</sub>
     * @param z1 the Y coordinate for the control point P<sub>0</sub>
     * @param type the segment type for the next argument
     *        ({@link PathIterator3D#SEG_LINETO},
     *         {@link PathIterator3D#SEG_QUADTO}, or
     *         {@link PathIterator3D#SEG_CUBICTO})
     * @param coords the control points, other than P<sub>0</sub> for
     *        the curve ending at P<sub>3</sub>
     * @param vx the X coordinate for P<sub>4</sub>
     * @param vy the Y coordinate for P<sub>4</sub>
     * @param vz the Z coordinate for P<sub>4</sub>
     * @param color the color for the new segment
     * @param tag the tag for the new segment
     */
    public synchronized void
	addFlippedCubicVertex(double x1, double y1, double z1,
			      int type, double[] coords,
			      double vx, double vy, double vz,
			      Color color, Object tag)
    {
	switch (type) {
	case PathIterator3D.SEG_LINETO:
	    Path3DInfo.elevateDegree(1, tmp1, 0, coords, 0);
	    Path3DInfo.elevateDegree(2, tmp2, 0, tmp1, 0);
	    coords = tmp2;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    Path3DInfo.elevateDegree(2, tmp1, 0, coords, 0);
	    coords = tmp1;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    break;
	default:
	    throw new IllegalArgumentException(errorMsg("piUnknown"));
	}
	addFlippedCubicVertex(x1, y1, z1,
			      coords[0], coords[1], coords[2],
			      coords[3], coords[4], coords[5],
			      coords[6], coords[7], coords[8],
			      vx, vy, vz, color, tag);
    }

    /**
     * Apply a transform to the surface.
     * The transform will be applied to each segment's control points.
     * @param tform the transform
     */
    public abstract void transform(Transform3D tform);
    
    /**
     * Reverse the orientation of this surface.
     * This changes the direction associated with each edge and each
     * segment.
     */
    public abstract void reverseOrientation();


    /**
     * Determine if a surface is a closed manifold.
     * @return true if the surface is closed; false otherwise
     */
    public boolean isClosedManifold() {
	if (boundaryComputed == false) {
	    computeBoundary(null);
	}
	if (wellFormed) {
	    // the boundary exists if the path is closed, but is empty.
	    return boundary.isEmpty();
	} else {
	    return false;
	}
    }

    /**
     * Get a point at a specific location on a segment of a surface.
     * @param type the segment type (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     * @return the point corresponding to coordinates (u,v)
     */
    public static Point3D
	segmentValue(int type, double[] coords, double u, double v)
	throws IllegalArgumentException
    {
	double results[] = new double[3];
	segmentValue(results, type, coords, u, v);
	return new Point3D.Double(results[0], results[1], results[2]);
    }

    /**
     * Get coordinates for a specific point on a segment of a surface.
     * @param results an array to hold the X, Y, and Z coordinates of
     *        point corresponding to parameters (u,v)
     * @param type the segment type (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    public static void
	segmentValue(double[] results,
		     int type, double[] coords, double u, double v)
	throws IllegalArgumentException
    {
	double x, y, z;
	if (type == SurfaceIterator.CUBIC_PATCH) {
	    // The large number of cases are for numerical accuracy as
	    // there are simplified expressions for particular values of
	    // u or v, and we want the same values on the edges of adjacent
	    // patches.
	    if (v == 0.0) {
		if (u == 0.0) {
		    results[0] = coords[0];
		    results[1] = coords[1];
		    results[2] = coords[2];
		    return;
		} else if (u == 1.0) {
		    results[0] = coords[9];
		    results[1] = coords[10];
		    results[2] = coords[11];
		    return;
		} else {
		    Functions.Bernstein.sumB(results, coords, 0, 3, u);
		    // x = results[0]; y = results[1]; z = results[2];
		    return;
		}
	    } else if (v == 1.0) {
		if (u == 0.0) {
		    results[0] = coords[36];
		    results[1] = coords[37];
		    results[2] = coords[38];
		    return;
		} else if (u == 1.0) {
		    results[0] = coords[45];
		    results[1] = coords[46];
		    results[2] = coords[47];
		    return;
		} else {
		    Functions.Bernstein.sumB(results, coords, 12, 3, u);
		    // x = results[0]; y = results[1]; z = results[2];
		    return;
		}
	    } else {
		if (u == 0.0) {
		    double[] vcoords = new double[12];
		    System.arraycopy(coords, 0, vcoords, 0, 3);
		    System.arraycopy(coords, 12, vcoords, 3, 3);
		    System.arraycopy(coords, 24, vcoords, 6, 3);
		    System.arraycopy(coords, 36, vcoords, 9, 3);
		    Functions.Bernstein.sumB(results, vcoords, 3, v);
		    return;
		} else if (u == 1.0) {
		    double[] vcoords = new double[12];
		    System.arraycopy(coords, 9, vcoords, 0, 3);
		    System.arraycopy(coords, 21, vcoords, 3, 3);
		    System.arraycopy(coords, 33, vcoords, 6, 3);
		    System.arraycopy(coords, 45, vcoords, 9, 3);
		    Functions.Bernstein.sumB(results, vcoords, 3, v);
		    return;
		} else {
		    double[] coords2 = new double[12];
		    Functions.Bernstein.sumB(coords2, 0, 3, coords, 0, 3, u);
		    Functions.Bernstein.sumB(coords2, 3, 3, coords, 4, 3, u);
		    Functions.Bernstein.sumB(coords2, 6, 3, coords, 8, 3, u);
		    Functions.Bernstein.sumB(coords2, 9, 3, coords, 12, 3, u);
		    Functions.Bernstein.sumB(results, coords2, 3, v);
		    /*
		    Functions.Bernstein.sumB(results, coords, 0, 3, u);
		    double val = Functions.B(0, 3, v);
		    x = results[0]*val;
		    y = results[1]*val;
		    z = results[2]*val;
		    Functions.Bernstein.sumB(results, coords, 4, 3, u);
		    val = Functions.B(1, 3, v);
		    x += results[0]*val;
		    y += results[1]*val;
		    z += results[2]*val;
		    Functions.Bernstein.sumB(results, coords, 8, 3, u);
		    val = Functions.B(2, 3, v);
		    x += results[0]*val;
		    y += results[1]*val;
		    z += results[2]*val;
		    Functions.Bernstein.sumB(results, coords, 12, 3, u);
		    val = Functions.B(3, 3, v);
		    x += results[0]*val;
		    y += results[1]*val;
		    z += results[2]*val;
		    results[0] = x; results[1] = y; results[2] = z;
		    */
		}
	    }
	} else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
	    double w = 1.0 - (u + v);
	    if (u == 0.0) {
		if (v == 0.0) {
		    // w == 1
		    results[0] = coords[0];
		    results[1] = coords[1];
		    results[2] = coords[2];
		    return;
		} else if (v == 1.0) {
		    // w == 0
		    results[0] = coords[3];
		    results[1] = coords[4];
		    results[2] = coords[5];
		    return;
		} else {
		    x = coords[0]*w;
		    y = coords[1]*w;
		    z = coords[2]*w;
		}
	    } else if (v == 0.0) {
		if (u == 1.0) {
		    // w == 0
		    results[0] = coords[6];
		    results[1] = coords[7];
		    results[2] = coords[8];
		    return;
		} else {
		    // w != 1
		    x = coords[0]*w;
		    y = coords[1]*w;
		    z = coords[2]*w;
		}
	    } else {
		// w != 0 and w != 1
		x = coords[0]*w;
		y = coords[1]*w;
		z = coords[2]*w;
	    }
	    x += coords[6]*u;
	    y += coords[7]*u;
	    z += coords[8]*u;
	    x += coords[3]*v;
	    y += coords[4]*v;
	    z += coords[5]*v;
	    results[0] = x; results[1] = y; results[2] = z;
	} else if (type == SurfaceIterator.CUBIC_VERTEX) {
	    if (v == 0.0) {
		if (u == 0.0) {
		    results[0] = coords[0];
		    results[1] = coords[1];
		    results[2] = coords[2];
		} else if (u == 1.0) {
		    results[0] = coords[9];
		    results[1] = coords[10];
		    results[2] = coords[11];
		} else {
		    Functions.Bernstein.sumB(results, coords, 0, 3, u);
		}
	    } else if (v == 1.0) {
		results[0] = coords[12];
		results[1] = coords[13];
		results[2] = coords[14];
	    } else {
		Functions.Bernstein.sumB(results, coords, 0, 3, u);
		double oneMinusV = 1 - v;
		results[0] = results[0]*oneMinusV + v*coords[12];
		results[1] = results[1]*oneMinusV + v*coords[13];
		results[2] = results[2]*oneMinusV + v*coords[14];
	    }
	} else if (type == SurfaceIterator.CUBIC_TRIANGLE) {
	    double w = 1.0 - (u + v);
	    if (u == 0.0) {
		if (v == 0.0) {
		    System.arraycopy(coords, 0, results, 0, 3);
		} else if (v == 1.0) {
		    System.arraycopy(coords, 9, results, 0, 3);
		} else {
		    Functions.Bernstein.sumB(results, coords, 3, v);
		}
	    } else if (v == 0.0) {
		if (u == 1.0) {
		    System.arraycopy(coords, 27, results, 0, 3);
		} else {
		    double[] vcoords = new double[12];
		    System.arraycopy(coords, 0, vcoords, 0, 3);
		    System.arraycopy(coords, 12, vcoords, 3, 3);
		    System.arraycopy(coords, 21, vcoords, 6, 3);
		    System.arraycopy(coords, 27, vcoords, 9, 3);
		    Functions.Bernstein.sumB(results, vcoords, 3, u);
		}
	    } else if (w == 0.0) {
		double[] wcoords = new double[12];
		    System.arraycopy(coords, 27, wcoords, 0, 3);
		    System.arraycopy(coords, 24, wcoords, 3, 3);
		    System.arraycopy(coords, 18, wcoords, 6, 3);
		    System.arraycopy(coords, 9, wcoords, 9, 3);
		    Functions.Bernstein.sumB(results, wcoords, 3, v);
	    } else {
		Functions.Bernstein.sumB(results, coords, 3, u, v, w);
	    }
	} else {
	    String msg = errorMsg("unknownType", type);
	    throw new IllegalArgumentException(msg);
	}
    }

    /**
     * Get a point at a specific location on a segment of a surface, using
     * barycentric coordinates.
     * <P>
     * The coordinates must satisfy the constraint u + w + v = 1;
     * To compute a value from the other two, use
     * <UL>
     *   <LI> u = 1.0 - (v + w);
     *   <LI> v = 1.0 - (u + w);
     *   <LI> w = 1.0 - (u + v);
     * </UL>
     * This can be done automatically by setting one of the three arguments
     * to -1.0.  If two or three are set to -1.0, an exception will be thrown.
     * @param type the segment type (SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.PLANAR_TRIANGLE, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface; -1 if the value of
     *        u should be computed from the other two parameters
     * @param v the second parameter for the surface; -1 if the value of
     *        v should be computed from the other two parameters
     * @param w the third parameter for the surface; -1 if the value of
     *        w should be computed from the other two parameters
     * @return the point corresponding to coordinates (u,v,w)
     * @exception IllegalArgumentException an argument was out of range,
     *            more than one argument was set to -1, or the type
     *            was not acceptable
     */
    public static Point3D
	segmentValue(int type, double[] coords, double u, double v, double w)
	throws IllegalArgumentException
    {
	double[] results = new double[3];
	segmentValue(results, type, coords, u, v, w);
	return new Point3D.Double(results[0], results[1], results[2]);
    }

    /**
     * Get the coordinates corresponding to a point at a specific
     * location on a segment of a surface, using barycentric
     * coordinates.
     * <P>
     * The coordinates must satisfy the constraint u + w + v = 1;
     * To compute a value from the other two, use
     * <UL>
     *   <LI> u = 1.0 - (v + w);
     *   <LI> v = 1.0 - (u + w);
     *   <LI> w = 1.0 - (u + v);
     * </UL>
     * This can be done automatically by setting one of the three arguments
     * to -1.0.  If two or three are set to -1.0, an exception will be thrown.
     * @param results an array holding the X, Y and Z coordinates of the
     *        desired point, listed in that order
     * @param type the segment type (SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.PLANAR_TRIANGLE, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface; -1 if the value of
     *        u should be computed from the other two parameters
     * @param v the second parameter for the surface; -1 if the value of
     *        v should be computed from the other two parameters
     * @param w the third parameter for the surface; -1 if the value of
     *        w should be computed from the other two parameters
     * @exception IllegalArgumentException an argument was out of range,
     *            more than one argument was set to -1, or the type
     *            was not acceptable
     */
    public static void
	segmentValue(double[] results,
		     int type, double[] coords, double u, double v, double w)
	throws IllegalArgumentException
    {
	double x, y, z;
	if (u == -1.0) {
	    if (v == -1.0 || w == -1.0) {
		String msg = errorMsg("freeBarycentric");
		throw new IllegalArgumentException(msg);
	    }
	    u = 1.0 - (v + w);
	} else if (v == -1.0) {
	    if (u == -1 || w == -1) {
		String msg = errorMsg("freeBarycentric");
		throw new IllegalArgumentException(msg);
	    }
	    v = 1.0 - (u + w);
	} else if (w == -1.0) {
	    if (v == -1.0 || u == -1.0) {
		String msg = errorMsg("freeBarycentric");
		throw new IllegalArgumentException(msg);
	    }
	    w = 1.0 - (u + v);
	}
	if (type == SurfaceIterator.CUBIC_PATCH) {
	    String msg = errorMsg("wrongType", "CUBIC_PATCH");
	    throw new IllegalArgumentException(msg);
	} else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
	    if (w == 0.0) {
		if (u == 1.0) {
		    results[0] = coords[3];
		    results[1] = coords[4];
		    results[2] = coords[5];
		    return;
		} else if (v == 1.0) {
		    results[0] = coords[6];
		    results[1] = coords[7];
		    results[2] = coords[8];
		    return;
		} else {
		    x = 0.0;
		    y = 0.0;
		    z = 0.0;
		}
	    } else  if (u == 0.0) {
		if (v == 0.0) {
		    // w == 1
		    results[0] = coords[0];
		    results[1] = coords[1];
		    results[2] = coords[2];
		    return;
		} else {
		    x = coords[0]*w;
		    y = coords[1]*w;
		    z = coords[2]*w;
		}
	    } else if (v == 0.0) {
		// w != 1
		x = coords[0]*w;
		y = coords[1]*w;
		z = coords[2]*w;
	    } else {
		// w != 0 and w != 1
		x = coords[0]*w;
		y = coords[1]*w;
		z = coords[2]*w;
	    }
	    x += coords[3]*u;
	    y += coords[4]*u;
	    z += coords[5]*u;
	    x += coords[6]*v;
	    y += coords[7]*v;
	    z += coords[8]*v;
	    results[0] = x; results[1] = y; results[2] = z;
	} else if (type == SurfaceIterator.CUBIC_VERTEX) {
	    if (v == 0.0) {
		if (u == 0.0) {
		    results[0] = coords[0];
		    results[1] = coords[1];
		    results[2] = coords[2];
		} else if (u == 1.0) {
		    results[0] = coords[9];
		    results[1] = coords[10];
		    results[2] = coords[11];
		} else {
		    Functions.Bernstein.sumB(results, coords, 0, 3, u);
		}
	    } else if (v == 1.0) {
		results[0] = coords[12];
		results[1] = coords[13];
		results[2] = coords[14];
	    } else {
		Functions.Bernstein.sumB(results, coords, 0, 3, u);
		double oneMinusV = 1 - v;
		results[0] = results[0]*oneMinusV + v*coords[12];
		results[1] = results[1]*oneMinusV + v*coords[13];
		results[2] = results[2]*oneMinusV + v*coords[14];
	    }
	} else if (type == SurfaceIterator.CUBIC_TRIANGLE) {
	    if (u == 0.0) {
		if (v == 0.0) {
		    System.arraycopy(coords, 0, results, 0, 3);
		} else if (v == 1.0) {
		    System.arraycopy(coords, 9, results, 0, 3);
		} else {
		    Functions.Bernstein.sumB(results, coords, 3, v);
		}
	    } else if (v == 0.0) {
		if (u == 1.0) {
		    System.arraycopy(coords, 27, results, 0, 3);
		} else {
		    double[] vcoords = new double[12];
		    System.arraycopy(coords, 0, vcoords, 0, 3);
		    System.arraycopy(coords, 12, vcoords, 3, 3);
		    System.arraycopy(coords, 21, vcoords, 6, 3);
		    System.arraycopy(coords, 27, vcoords, 9, 3);
		    Functions.Bernstein.sumB(results, vcoords, 3, u);
		}
	    } else if (w == 0.0) {
		double[] wcoords = new double[12];
		    System.arraycopy(coords, 27, wcoords, 0, 3);
		    System.arraycopy(coords, 24, wcoords, 3, 3);
		    System.arraycopy(coords, 18, wcoords, 6, 3);
		    System.arraycopy(coords, 9, wcoords, 9, 3);
		    Functions.Bernstein.sumB(results, wcoords, 3, v);
	    } else {
		Functions.Bernstein.sumB(results, coords, 3, u, v, w);
	    }
	} else {
	    String msg = errorMsg("unknownType", type);
	    throw new IllegalArgumentException(msg);
	}
    }

    /**
     * Get components of the "u" tangent vector at a specific point on a
     * segment of a surface. The value of the vector is &part;p/&part;u
     * where p is a point for parameters (u,v). For cases where
     * barycentric coordinates are used p(u,v,1-(u+v)) is differentiated.
     * @param results an array to hold the X, Y, and Z components of
     *        the tangent vector for the u direction when the parameters
     *        are (u,v)
     * @param type the segment type (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    public static void uTangent(double[] results, int type,
				double[] coords, double u, double v) {
	double x, y, z;
	if (type == SurfaceIterator.CUBIC_PATCH) {
	    // u or v = 0 or 1 are special cases that occur
	    // frequently, so optimizing these is useful for performance
	    // reasons.
	    double[] old = new double[3];
	    double coords3[] = new double[12];
	    Functions.Bernstein.dsumBdx(coords3, 0, 3, coords, 0, 3, u);
	    Functions.Bernstein.dsumBdx(coords3, 3, 3, coords, 4, 3, u);
	    Functions.Bernstein.dsumBdx(coords3, 6, 3, coords, 8, 3, u);
	    Functions.Bernstein.dsumBdx(coords3, 9, 3, coords,
					12, 3, u);
	    Functions.Bernstein.sumB(old, coords3, 3, v);
	    if (v == 0) {
		Functions.Bernstein.dsumBdx(results, 0, 3, coords, 0, 3, u);
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(results[i] - old[i]) > 1.e-12) {
			System.out.println("old and result differ 1");
		    }
		}
	    } else if (v == 1.0) {
		Functions.Bernstein.dsumBdx(results, 0, 3, coords, 12, 3, u);
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(results[i] - old[i]) > 1.e-12) {
			System.out.println("old and result differ 2");
		    }
		}
	    } else {
		double[] coords2 = new double[12];
		Functions.Bernstein.dsumBdx(coords2, 0, 3, coords, 0, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 3, 3, coords, 4, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 6, 3, coords, 8, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 9, 3, coords, 12, 3, u);
		Functions.Bernstein.sumB(results, coords2, 3, v);
	    }
	    /*
	    Functions.Bernstein.dsumBdx(results, coords, 0, 3, u);
	    double val = Functions.B(0, 3, v);
	    x = results[0]*val;
	    y = results[1]*val;
	    z = results[2]*val;
	    Functions.Bernstein.dsumBdx(results, coords, 4, 3, u);
	    val = Functions.B(1, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    Functions.Bernstein.dsumBdx(results, coords, 8, 3, u);
	    val = Functions.B(2, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    Functions.Bernstein.dsumBdx(results, coords, 12, 3, u);
	    val = Functions.B(3, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    results[0] = x; results[1] = y; results[2] = z;
	    */
	} else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
	    results[0] = coords[6] - coords[0];
	    results[1] = coords[7] - coords[1];
	    results[2] = coords[8] - coords[2];
	} else if (type == SurfaceIterator.CUBIC_VERTEX) {
	    if (v  == 0.0) {
		Functions.Bernstein.dsumBdx(results, coords, 3, u);
	    } else if (v == 1.0) {
		results[0] = 0.0;
		results[1] = 0.0;
		results[1] = 0.0;
	    } else {
		Functions.Bernstein.dsumBdx(results, coords, 3, u);
		double oneMinusV = 1.0 - v;
		results[0] *= oneMinusV;
		results[1] *= oneMinusV;
		results[2] *= oneMinusV;
	    }
	} else if (type == SurfaceIterator.CUBIC_TRIANGLE) {
	    double w = 1.0 - (u + v);
	    Functions.Bernstein.dsumBdx(0, results, coords, 3, u, v, w);
	    double[] tmp = new double[results.length];
	    Functions.Bernstein.dsumBdx(2, tmp, coords, 3, u, v, w);
	    for (int i = 0; i < results.length; i++) {
		results[i] -= tmp[i];
	    }
	}
    }

    /**
     * Get components of the "v" tangent vector at a specific point on a
     * segment of a surface. The value of the vector is &part;p/&part;v
     * where p is a point for parameters (u,v). For cases where
     * barycentric coordinates are used p(u,v,1-(u+v)) is differentiated.
     * @param results an array to hold the X, Y, and Z components of
     *        the tangent vector for the u direction when the parameters
     *        are (u,v)
     * @param type the segment type (SurfaceIterator.PLANAR_TRIANGLE,
     *        SurfaceIterator.CUBIC_TRIANGLE,
     *        SurfaceIterator.CUBIC_PATCH, or
     *        SurfaceIterator.CUBIC_VERTEX)
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    public static void vTangent(double[] results, int type,
				double[] coords, double u, double v) {
	double x, y, z;
	if (type == SurfaceIterator.CUBIC_PATCH) {
	    double[] coords2 = new double[12];
	    Functions.Bernstein.sumB(coords2, 0, 3, coords, 0, 3, u);
	    Functions.Bernstein.sumB(coords2, 3, 3, coords, 4, 3, u);
	    Functions.Bernstein.sumB(coords2, 6, 3, coords, 8, 3, u);
	    Functions.Bernstein.sumB(coords2, 9, 3, coords, 12, 3, u);
	    Functions.Bernstein.dsumBdx(results, coords2, 3, v);
	} else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
	    results[0] = coords[3] - coords[0];
	    results[1] = coords[4] - coords[1];
	    results[2] = coords[5] - coords[2];
	} else  if (type == SurfaceIterator.CUBIC_VERTEX) {
	    if (u == 0.0) {
		results[0] = - coords[0];
		results[1] = - coords[1];
		results[2] = - coords[2];
	    } else if (u == 1.0) {
		results[0] = - coords[9];
		results[1] = - coords[10];
		results[2] = - coords[11];
	    } else {
		Functions.Bernstein.sumB(results, 0, 3, coords, 0, 3, u);
		for (int i = 0; i < 3; i++) {
		    results[i] = - results[i];
		}
	    }
	    results[0] += coords[12];
	    results[1] += coords[13];
	    results[2] += coords[14];
	} else if (type == SurfaceIterator.CUBIC_TRIANGLE) {
	    double w = 1.0 - (u + v);
	    Functions.Bernstein.dsumBdx(1, results, coords, 3, u, v, w);
	    double[] tmp = new double[results.length];
	    Functions.Bernstein.dsumBdx(2, tmp, coords, 3, u, v, w);
	    for (int i = 0; i < results.length; i++) {
		results[i] -= tmp[i];
	    }
	}
    }

    private static int areaCPN = 8;

    private static double[][]areaWeightsCP = new double[areaCPN][areaCPN];
    private static double[] areaArgsCP =
	GLQuadrature.getArguments(0.0, 1.0, areaCPN);
    static {
	double[] ws = GLQuadrature.getWeights(0.0, 1.0, areaCPN);
	for (int i = 0; i < areaCPN; i++) {
	    for (int j = 0; j < areaCPN; j++) {
		areaWeightsCP[i][j] = ws[i]*ws[j];
	    }
	}
    }

    private static int areaCTN = 8;

    private static double[][] areaWeightsCT = new double[areaCTN][];
    private static double[] areaArgsCTu;
    private static double[][] areaArgsCTv = new double[areaCTN][];
    static {
	areaArgsCTu = GLQuadrature.getArguments(0.0, 1.0, areaCTN);
	double[] uws = GLQuadrature.getWeights(0.0, 1.0, areaCTN);
	for (int i = 0; i < areaCTN; i++) {
	    double vmax = 1.0 - areaArgsCTu[i];
	    areaArgsCTv[i] = GLQuadrature.getArguments(0.0, vmax, areaCTN);
	    areaWeightsCT[i] = GLQuadrature.getWeights(0.0, vmax, areaCTN);
	    for (int j = 0; j < areaWeightsCT[i].length; j++) {
		areaWeightsCT[i][j] *= uws[i];
	    }
	}
    }

    /**
     * Configure the number of points used in an area computation for
     * Gaussian-Legendre integration.
     * The default for both is 8.  If values are set to be less than 8,
     * they will be increased to 8, so giving values of 0 will produce
     * the default.  Normally the default should be adequate.  Setting
     * higher values is useful for testing.  One should note that increasing
     * the values improves the accuracy for each segment in the absence of
     * floating-point errors, but increases the number of values summed.
     * @param nCT the number of points for cubic B&eacute;zier triangles
     * @param nCP the number of points for cubic B&eacute;zier patches
     */
    public static void configArea(int nCT, int nCP) {
	if (nCT < 8) nCT = 8;
	if (nCP < 8) nCP = 8;
	Surface3D.areaCTN = nCT;
	Surface3D.areaCPN = nCP;

	areaWeightsCP = new double[areaCPN][areaCPN];
	areaArgsCP = GLQuadrature.getArguments(0.0, 1.0, areaCPN);
	double[] ws = GLQuadrature.getWeights(0.0, 1.0, areaCPN);
	for (int i = 0; i < areaCPN; i++) {
	    for (int j = 0; j < areaCPN; j++) {
		areaWeightsCP[i][j] = ws[i]*ws[j];
	    }
	}

	areaWeightsCT = new double[areaCTN][];
	areaArgsCTv = new double[areaCTN][];
	areaArgsCTu = GLQuadrature.getArguments(0.0, 1.0, areaCTN);
	double[] uws = GLQuadrature.getWeights(0.0, 1.0, areaCTN);
	for (int i = 0; i < areaCTN; i++) {
	    double vmax = 1.0 - areaArgsCTu[i];
	    areaArgsCTv[i] = GLQuadrature.getArguments(0.0, vmax, areaCTN);
	    areaWeightsCT[i] = GLQuadrature.getWeights(0.0, vmax, areaCTN);
	    for (int j = 0; j < areaWeightsCT[i].length; j++) {
		areaWeightsCT[i][j] *= uws[i];
	    }
	}
    }

    public static synchronized void
	cubicVertexToPatch(double[] coords, int offset,
			   double[] pcoords, int poffset)
    {
	//  Convert a CUBIC_VERTEX segment to an equivalent
	//  CUBIC_PATCH.
	int diff = poffset - offset;

	if (coords.length - 48 < offset) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	if (coords == pcoords && ((diff > 0 && diff < 15)
				  || (diff < 0 && diff > -48))) {
	    throw new IllegalArgumentException(errorMsg("arrayRegions"));
	}
	if (coords != pcoords) {
	    System.arraycopy(coords, offset, pcoords, poffset, 12);
	}

	System.arraycopy(coords, offset+12, pcoords, poffset+36, 3);
	System.arraycopy(coords, offset+12, pcoords, poffset+39, 3);
	System.arraycopy(coords, offset+12, pcoords, poffset+42, 3);
	System.arraycopy(coords, offset+12, pcoords, poffset+45, 3);

	for (int i = 0; i < 4; i++) {
	    int iii = 3*i;
	    System.arraycopy(coords, iii, tmptp1, 0, 3);
	    System.arraycopy(pcoords, 45, tmptp1, 3, 3);
	    Path3DInfo.elevateDegree(1, tmptp2, 0, tmptp1, 0);
	    Path3DInfo.elevateDegree(2, tmptp1, 0, tmptp2, 0);
	    pcoords[12 + iii] = tmptp1[3];
	    pcoords[13 + iii] = tmptp1[4];
	    pcoords[14 + iii] = tmptp1[5];
	    pcoords[24 + iii] = tmptp1[6];
	    pcoords[25 + iii] = tmptp1[7];
	    pcoords[26 + iii] = tmptp1[8];
	}
    }

    /**
     * Add the surface area for those segments associated with a
     * surface iterator to an Adder.
     * The surface iterator will be modified.
     * @param adder the adder
     * @param si the surface iterator
     */
    public static void addAreaToAdder(Adder adder, SurfaceIterator si) {
	double[] coords = new double[48];
	double[] tmpu = new double[3];
	double[] tmpv = new double[3];
	double[] cp = new double[3];
	while(si.isDone() == false) {
	    int type;
	    switch(type = si.currentSegment(coords)) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		coords[3] -= coords[0];
		coords[4] -= coords[1];
		coords[5] -= coords[2];
		coords[6] -= coords[0];
		coords[7] -= coords[1];
		coords[8] -= coords[2];
		VectorOps.crossProduct(coords, 0, coords, 3, coords, 6);
		double result = VectorOps.norm(coords, 0, 3);
		adder.add(result/2.0);
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		for (int i = 0; i < areaCTN; i++) {
		    double u = areaArgsCTu[i];
		    for (int j = 0; j < areaWeightsCT[i].length; j++) {
			double v = areaArgsCTv[i][j];
			uTangent(tmpu, type, coords, u, v);
			vTangent(tmpv, type, coords, u, v);
			VectorOps.crossProduct(cp, tmpu, tmpv);
			double norm = VectorOps.norm(cp);
			adder.add(norm*areaWeightsCT[i][j]);
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		cubicVertexToPatch(coords, 0, coords, 0);
		type = SurfaceIterator.CUBIC_PATCH;
		// Fall through as we converted the cubic-vertex segment
		// to a cubic-patch segment
	    case SurfaceIterator.CUBIC_PATCH:
		for (int i = 0; i < areaCPN; i++) {
		    double u = areaArgsCP[i];
		    for (int j = 0; j < areaCPN; j++) {
			double v = areaArgsCP[j];
			uTangent(tmpu, type, coords, u, v);
			vTangent(tmpv, type, coords, u, v);
			VectorOps.crossProduct(cp, tmpu, tmpv);
			double norm = VectorOps.norm(cp);
			adder.add(norm*areaWeightsCP[i][j]);
		    }
		}
		break;
	    }
	    si.next();
	}
    }

    static final int MIN_PARALLEL_SIZE_A = 1024;
    static final int MIN_PARALLEL_SIZE_V = 256;
    static final int MIN_PARALLEL_SIZE_CM = 192;
    static final int MIN_PARALLEL_SIZE_M = 128;

    /**
     * Compute the area of this surface.
     * @return the area
     */
    public double area() {
	boolean parallel = (size() >= MIN_PARALLEL_SIZE_A);
	return area(parallel);
	/*
	SurfaceIterator si = getSurfaceIterator(null);
	Adder adder = new Adder.Kahan();
	addAreaToAdder(adder, si);
	return adder.getSum();
	*/
    }


    // a test indicated that there was little advantage in increasing
    // this beyond 2.
    static final int AREA_SPLIT = 2;

    public double area(boolean parallel) {
	if (parallel && Runtime.getRuntime().availableProcessors() > 2) {
	    final SurfaceIteratorSplitter splitter =
		new SurfaceIteratorSplitter(AREA_SPLIT,
					    getSurfaceIterator(null));
	    int lim = AREA_SPLIT-1;
	    Adder[] adders = new Adder[lim];
	    Thread[] threads = new Thread[lim];
	    for (int i = 0; i < lim; i++) {
		final int ind = i;
		adders[i] = new Adder.Kahan();
		threads[i] = new Thread(() -> {
			SurfaceIterator tsi = splitter.getSurfaceIterator(ind);
			addAreaToAdder(adders[ind], tsi);
		});
		threads[i].start();
	    }
	    Adder adder = new Adder.Kahan();
	    addAreaToAdder(adder, splitter.getSurfaceIterator(lim));
	    try {
		for (int i = 0; i < lim; i++) {
		    threads[i].join();
		    adder.add(adders[i].getSum());
		}
	    } catch (InterruptedException ei) {
		splitter.interrupt();
		for (int i = 0; i < lim; i++) {
		    threads[i].interrupt();
		}
	    }
	    return adder.getSum();
	} else {
	    SurfaceIterator si = getSurfaceIterator(null);
	    Adder adder = new Adder.Kahan();
	    addAreaToAdder(adder, si);
	    return adder.getSum();
	}
    }



    // We only need 5 points per direction because the positions are
    // cubic polynomials of the parameters u and v, and the tangent
    // vectors are cubic in one parameter and quadratic in the other.
    // The term we integrate is in the worst case a cubic times
    // a quadratic times a cubic polynomial, and thus an 8th order
    // polynomial.  Gaussian-Legendre quadrature with n points is
    // exact for polynomials of degree (2n-1), so 9 is enough for
    // an exact integral (up to floating-point round-off errors).
    // For cubic triangles, 4 suffices.
    //
    private static final int volumeCPN = 9;

    private static double[][]volumeWeightsCP = new double[volumeCPN][volumeCPN];
    private static double[] volumeArgsCP =
	GLQuadrature.getArguments(0.0, 1.0, volumeCPN);

    static {
	double[] ws = GLQuadrature.getWeights(0.0, 1.0, volumeCPN);
	for (int i = 0; i < volumeCPN; i++) {
	    for (int j = 0; j < volumeCPN; j++) {
		volumeWeightsCP[i][j] = ws[i]*ws[j];
	    }
	}
    }

    private static final int volumeCTN = 4;

    private static double[][] volumeWeightsCT = new double[volumeCTN][];
    private static double[] volumeArgsCTu;
    private static double[][] volumeArgsCTv = new double[volumeCTN][];
    static {
	volumeArgsCTu = GLQuadrature.getArguments(0.0, 1.0, volumeCTN);
	double[] uws = GLQuadrature.getWeights(0.0, 1.0, volumeCTN);
	for (int i = 0; i < volumeCTN; i++) {
	    double vmax = 1.0 - volumeArgsCTu[i];
	    volumeArgsCTv[i] = GLQuadrature.getArguments(0.0, vmax, volumeCTN);
	    volumeWeightsCT[i] = GLQuadrature.getWeights(0.0, vmax, volumeCTN);
	    for (int j = 0; j < volumeWeightsCT[i].length; j++) {
		volumeWeightsCT[i][j] *= uws[i];
	    }
	}
    }

    static private final  Point3D defaultRefPoint =
	new Point3D.Double(0.0, 0.0, 0.0);

    // testing
    // public static boolean printit = false;

    /**
     * Add the surface-integral contributions, used in computing a volume
     * for those segments associated with a surface iterator, to an Adder.
     * The surface iterator will be modified.  The total added to the
     * adder will be 3 times the contribution of the iterator's patches
     * to the volume of the shape it represents or partially represents.
     * (Dividing by 3 at the end improves numerical accuracy.)
     * <P>
     * The volume assumes the surface is well formed and is embedded in
     * a Euclidean three-dimensional space. The algorithm first (in effect)
     * translates an arbitrary reference point (rx, ry, rz) to the origin
     * and then computes the integral of the vector (x, y, z) over the
     * translated surface. The divergence of this vector is the constant 3,
     * and Gauss's theorem states that the integral of the divergence of a
     * vector field v over a volume bounded by a surface S is equal to the
     * the surface integral of v over S (the integral over the surface of
     * the dot product of v and the normal to the surface).  Since the
     * divergence is 3, this surface integral's value is 3 times the volume.
     * It is the caller's responsibility to divide by this factor of 3 at
     * the appropriate point.
     * <P>
     * A reasonable choice of the reference point is the center of the
     * surface's bounding box, a heuristic that helps reduce floating-point
     * errors.
     * @param adder the adder
     * @param si the surface iterator
     * @param refPoint a reference point
     */
    public static void addVolumeToAdder(Adder adder, SurfaceIterator si,
					Point3D refPoint)
    {
	double[] coords = new double[48];
	double[] tmpu = new double[3];
	double[] tmpv = new double[3];
	double[] tmpr = new double[3];
	double[] cp = new double[3];
	if (refPoint == null) refPoint = defaultRefPoint;
	double xref = refPoint.getX();
	double yref = refPoint.getY();
	double zref = refPoint.getZ();
	/*
	Adder.Kahan adderPT = new Adder.Kahan();
	Adder.Kahan adderCT = new Adder.Kahan();
	Adder.Kahan adderCP = new Adder.Kahan();
	*/

	while(si.isDone() == false) {
	    int type;
	    switch(type = si.currentSegment(coords)) {
	    case SurfaceIterator.PLANAR_TRIANGLE:
		coords[3] -= coords[0];
		coords[4] -= coords[1];
		coords[5] -= coords[2];
		coords[6] -= coords[0];
		coords[7] -= coords[1];
		coords[8] -= coords[2];
		coords[0] -= xref;
		coords[1] -= yref;
		coords[2] -= zref;
		/*
		double result = VectorOps.dotCrossProduct
		    (coords, 0, coords, 3, coords, 6);
		*/
		double result = VectorOps.dotCrossProduct
		    (coords, 0, coords, 6, coords, 3);
		adder.add(result/2.0);
		// adderPT.add(result/2.0);
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		for (int i = 0; i < volumeCTN; i++) {
		    double u = volumeArgsCTu[i];
		    for (int j = 0; j < volumeWeightsCT[i].length; j++) {
			double v = volumeArgsCTv[i][j];
			uTangent(tmpu, type, coords, u, v);
			vTangent(tmpv, type, coords, u, v);
			segmentValue(tmpr, type, coords, u, v);
			tmpr[0] -= xref;
			tmpr[1] -= yref;
			tmpr[2] -= zref;
			double d = VectorOps.dotCrossProduct(tmpr, tmpu, tmpv);
			adder.add(d*volumeWeightsCT[i][j]);
			// adderCT.add(d*volumeWeightsCT[i][j]);
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		cubicVertexToPatch(coords, 0, coords, 0);
		type = SurfaceIterator.CUBIC_PATCH;
		// Fall through as we've converted a cubic-vertex segment
		// to a cubic-patch segment
	    case SurfaceIterator.CUBIC_PATCH:
		for (int i = 0; i < volumeCPN; i++) {
		    double u = volumeArgsCP[i];
		    for (int j = 0; j < volumeCPN; j++) {
			double v = volumeArgsCP[j];
			uTangent(tmpu, type, coords, u, v);
			vTangent(tmpv, type, coords, u, v);
			segmentValue(tmpr, type, coords, u, v);
			tmpr[0] -= xref;
			tmpr[1] -= yref;
			tmpr[2] -= zref;
			double d = VectorOps.dotCrossProduct(tmpr, tmpu, tmpv);
			adder.add(d*volumeWeightsCP[i][j]);
			// adderCP.add(d*volumeWeightsCP[i][j]);
		    }
		}
		break;
	    }
	    si.next();
	}
	/*
	if (printit) {
	    System.out.println("PT: " + adderPT.getSum()
			       +", CT: " + adderCT.getSum()
			       +", CP: " + adderCP.getSum()
			       + ", total = " +(adderPT.getSum()
						+ adderCT.getSum()
						+ adderCP.getSum()));
	}
	*/
    }

    private static final int VOLUME_SPLIT = 2;

    /**
     * Compute the volume enclosed by this surface.
     * The surface must be a closed manifold.
     * @return the volume enclosed by this surface
     * @exception IllegalStateException the surface is not a closed
     *            manifold, is not an oriented manifold, or is not
     *            well formed
     */
    public double volume()
	throws IllegalStateException
    {
	boolean parallel = (size() >= MIN_PARALLEL_SIZE_V);
	return volume(parallel);
    }

    public double volume(boolean parallel) {
	int maxProc = Runtime.getRuntime().availableProcessors();
	if (parallel && maxProc > 2) {
	    if (!isWellFormed()) {
		throw new IllegalStateException(errorMsg("notWellFormed"));
	    }

	    if (!isClosedManifold()) {
		throw new IllegalStateException(errorMsg("notClosedManifold"));
	    }

	    if (!isOriented()) {
		throw new IllegalStateException(errorMsg("notOriented"));
	    }

	    Adder adder = new Adder.Kahan();
	    Rectangle3D bb = getBounds();
	    final Point3D refPoint = new Point3D.Double(bb.getCenterX(),
							bb.getCenterY(),
							bb.getCenterZ());

	    int nsplit = (VOLUME_SPLIT > (maxProc-1))? maxProc-1:
		VOLUME_SPLIT;
	    SurfaceIteratorSplitter splitter =
		new SurfaceIteratorSplitter(nsplit,
					    getSurfaceIterator(null));
	    nsplit--;
	    final Adder[] adders = new Adder[nsplit];
	    final Thread[] threads = new Thread[nsplit];
	    for (int i = 0; i < nsplit; i++) {
		final int ind = i;
		adders[i] = new Adder.Kahan();
		threads[i] = new Thread(() -> {
			SurfaceIterator tsi = splitter.getSurfaceIterator(ind);
			addVolumeToAdder(adders[ind], tsi, refPoint);
		});
		threads[i].start();
	    }
	    addVolumeToAdder(adder, splitter.getSurfaceIterator(nsplit),
			     refPoint);
	    try {
		for (int i  = 0; i < nsplit; i++) {
		    threads[i].join();
		    adder.add(adders[i].getSum());
		}
	    } catch (InterruptedException ei) {
		splitter.interrupt();
		for (int i = 0; i < nsplit; i++) {
		    threads[i].interrupt();
		}
	    }
	    return adder.getSum()/3.0;
	} else {
	    if (!isWellFormed()) {
		throw new IllegalStateException(errorMsg("notWellFormed"));
	    }

	    if (!isClosedManifold()) {
		throw new IllegalStateException(errorMsg("notClosedManifold"));
	    }

	    if (!isOriented()) {
		throw new IllegalStateException(errorMsg("notOriented"));
	    }

	    Adder adder = new Adder.Kahan();
	    Rectangle3D bb = getBounds();
	    Point3D refPoint = new Point3D.Double(bb.getCenterX(),
						  bb.getCenterY(),
						  bb.getCenterZ());
	    addVolumeToAdder(adder, getSurfaceIterator(null), refPoint);
	    return adder.getSum()/3.0;
	}
    }


    // use for a volume computation.
    private static final SurfaceIntegral siV =
	new SurfaceIntegral(2, (x,y,z) -> {return x;},
			    (x,y,z) -> {return y;},
			    (x,y,z) -> {return z;});

    private static final SurfaceIntegral siX =
	new SurfaceIntegral(2, null, (x,y,z) -> {return x*y;}, null);
    private static final SurfaceIntegral siY =
	new SurfaceIntegral(2, null, null , (x,y,z) -> {return y*z;});
    private static final SurfaceIntegral siZ =
	new SurfaceIntegral(2, (x,y,z) -> {return z*x;}, null, null);

    private static final SurfaceIntegral.Batched siVXYZ
	= new SurfaceIntegral.Batched(siV, siX, siY, siZ);

    private static final SurfaceIntegral.Batched siXYZ
	= new SurfaceIntegral.Batched(siX, siY, siZ);

    /**
     * Compute the center of mass of a shape assuming uniform density.
     * @param shape the shape
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (size >= MIN_PARALLEL_SIZE_CM);
	} else {
	    size = MIN_PARALLEL_SIZE_CM;
	}
	return centerOfMassOf(shape, parallel, size);
    }
    /**
     * Compute the center of mass of a shape assuming uniform density,
     * specifying whether to use a sequential or parallel computation.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments in the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape, boolean parallel,
					 int size)
	throws IllegalArgumentException
    {
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}

	Rectangle3D bb = shape.getBounds();
	double cx = bb.getCenterX();
	double cy = bb.getCenterY();
	double cz = bb.getCenterZ();

	// To improve the accuracy of the surface integrals.
	AffineTransform3D af =
	    AffineTransform3D.getTranslateInstance(-cx, -cy, -cz);
	/*
	Adder adder = new Adder.Kahan();
	addVolumeToAdder(adder, shape.getSurfaceIterator(null),  null);
	double v = adder.getSum()/3.0;
	return centerOfMassOf(shape, v);
	*/
	double[] values = siVXYZ.integrate(shape.getSurfaceIterator(af),
					   parallel, size);
	return new Point3D.Double(3*values[1]/values[0] + cx,
				  3*values[2]/values[0] + cy,
				  3*values[3]/values[0] + cz);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.
     * @param shape the shape
     * @param v the volume of the shape.
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape, double v)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (size >= MIN_PARALLEL_SIZE_CM);
	} else {
	    size = MIN_PARALLEL_SIZE_CM;
	}
	return centerOfMassOf(shape, v, parallel, size);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume, and specifying if the computation should
     * be done in parallel.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape, double v,
					 boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));
	/*
	double xval = siX.integrate(shape.getSurfaceIterator(null)) / v;
	double yval = siY.integrate(shape.getSurfaceIterator(null)) / v;
	double zval = siZ.integrate(shape.getSurfaceIterator(null)) / v ;
	return new Point3D.Double(xval, yval, zval);
	*/
	Rectangle3D bb = shape.getBounds();
	double cx = bb.getCenterX();
	double cy = bb.getCenterY();
	double cz = bb.getCenterZ();
	// To improve the accuracy of the surface integrals.
	AffineTransform3D af =
	    AffineTransform3D.getTranslateInstance(-cx, -cy, -cz);

	double[] cmcoords = siXYZ.integrate(shape.getSurfaceIterator(af),
					    parallel, size);
	return new Point3D.Double((cmcoords[0]/v) + cx,
				  (cmcoords[1]/v) + cy,
				  (cmcoords[2]/v) + cz);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density and
     * given the shape's volume and a flatness limit.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.  Flatness parameters
     * are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param limit the flatness parameter
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape, double v, double limit)
	throws IllegalArgumentException
    {
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (s.size() >= MIN_PARALLEL_SIZE_CM);
	} else {
	    size = MIN_PARALLEL_SIZE_CM;
	}
	return centerOfMassOf(shape, v, limit, parallel, size);
    }

    /**
     * Compute the center of mass of a shape assuming uniform density
     * and given the shape's volume and a flatness limit, and
     * specifying if the computation should be done in parallel.
     * The volume is provided as a parameter to speed up the computation
     * for cases where the volume is already available.  Flatness parameters
     * are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape
     * @param v the volume of the shape.
     * @param limit the flatness parameter
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the center of mass
     */
    public static Point3D centerOfMassOf(Shape3D shape, double v, double limit,
					 boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));

	SurfaceIntegral fsiX =
	    new SurfaceIntegral(2, null, (x,y,z) -> {return x*y;}, null,
				limit);
	SurfaceIntegral fsiY =
	    new SurfaceIntegral(2, null, null , (x,y,z) -> {return y*z;},
				limit);
	SurfaceIntegral fsiZ =
	    new SurfaceIntegral(2, (x,y,z) -> {return z*x;}, null, null,
				limit);

	SurfaceIntegral.Batched fsiXYZ =
	    new SurfaceIntegral.Batched(fsiX, fsiY, fsiZ);

	/*
	double xval = fsiX.integrate(shape.getSurfaceIterator(null)) / v;
	double yval = fsiY.integrate(shape.getSurfaceIterator(null)) / v;
	double zval = fsiZ.integrate(shape.getSurfaceIterator(null)) / v ;
	return new Point3D.Double(xval, yval, zval);
	*/

	Rectangle3D bb = shape.getBounds();
	double cx = bb.getCenterX();
	double cy = bb.getCenterY();
	double cz = bb.getCenterZ();
	// To improve the accuracy of the surface integrals.
	AffineTransform3D af =
	    AffineTransform3D.getTranslateInstance(-cx, -cy, -cz);
	double[] cmcoords = fsiXYZ.integrate(shape.getSurfaceIterator(af),
					     parallel, size);
	return new Point3D.Double((cmcoords[0]/v) + cx,
				  (cmcoords[1]/v) + cy,
				  (cmcoords[2]/v) + cz);
    }



    private static class MomentData {
	double xc = 0.0;
	double yc = 0.0;
	double zc = 0.0;

	SurfaceIntegral siV3;
	SurfaceIntegral siX2;
	SurfaceIntegral siY2;
	SurfaceIntegral siZ2;
	SurfaceIntegral siXY;
	SurfaceIntegral siZX;
	SurfaceIntegral siYZ;

	SurfaceIntegral.Batched vbatched;
	SurfaceIntegral.Batched batched;

	public MomentData(Point3D cm, boolean vbatch) {
	    if (cm != null) {
		xc = cm.getX();
		yc = cm.getY();
		zc = cm.getZ();
	    }
	    siX2 = new SurfaceIntegral(3, null, (x,y,z) -> {
		    return (x-xc)*(x-xc)*(y-yc);}, null);
	    siY2 = new SurfaceIntegral(3, null, null, (x,y,z) -> {
		    return (y-yc)*(y-yc)*(z-zc);});
	    siZ2 = new SurfaceIntegral(3, (x,y,z) -> {
		    return (z-zc)*(z-zc)*(x-xc);}, null, null);

	    siXY = new SurfaceIntegral(3, null, null, (x,y,z) ->
				    {return (x-xc)*(y-yc)*(z-zc);});
	    siZX = new SurfaceIntegral(3, null, (x,y,z) -> {
		    return (z-zc)*(x-xc)*(y-yc);}, null);
	    siYZ = new SurfaceIntegral(3, (x,y,z) -> {
		    return (y-yc)*(z-zc)*(x-xc);}, null, null);

	    if (vbatch) {
		siV3 = new SurfaceIntegral(3, (x,y,z)->{return x;},
					   (x,y,z)->{return y;},
					   (x,y,z)->{return z;});
		vbatched  =
		    new SurfaceIntegral.Batched(siV3, siX2, siY2, siZ2,
						siXY, siZX, siYZ);
	    } else {
		batched  =
		    new SurfaceIntegral.Batched(siX2, siY2, siZ2,
						siXY, siZX, siYZ);
	    }
	}

	public MomentData(Point3D cm, double limit) {
	    if (cm != null) {
		xc = cm.getX();
		yc = cm.getY();
		zc = cm.getZ();
	    }
	    siX2 = new SurfaceIntegral(3, null, (x,y,z) -> {
		    return (x-xc)*(x-xc)*(y-yc);}, null, limit);
	    siY2 = new SurfaceIntegral(3, null, null, (x,y,z) -> {
		    return (y-yc)*(y-yc)*(z-zc);}, limit);
	    siZ2 = new SurfaceIntegral(3, (x,y,z) -> {
		    return (z-zc)*(z-zc)*(x-xc);}, null, null, limit);

	    siXY = new SurfaceIntegral(3, null, null, (x,y,z) -> {
		    return (x-xc)*(y-yc)*(z-zc);}, limit);
	    siZX = new SurfaceIntegral(3, null, (x,y,z) -> {
		    return (z-zc)*(x-xc)*(y-yc);}, null, limit);
	    siYZ = new SurfaceIntegral(3, (x,y,z) -> {
		    return (y-yc)*(z-zc)*(x-xc);}, null, null, limit);

	    batched  = new SurfaceIntegral.Batched(siX2, siY2, siZ2,
						   siXY, siZX, siYZ);
	}

	double[][] getMoments(SurfaceIterator si, boolean parallel, int size) {
	    double[] array = vbatched.integrate(si, parallel, size);
	    double v = array[0]/3;
	    // double sign = (array[0] < 0.0)? -1.0: 1.0;
	    double[][] moments = new double[3][3];
	    if (v == 0.0) return moments;
	    moments[0][0] = array[1]/v;
	    moments[0][1] = array[4]/v;
	    moments[0][2] = array[5]/v;
	    moments[1][0] = moments[0][1];
	    moments[1][1] = array[2]/v;
	    moments[1][2] = array[6]/v;
	    moments[2][0] = moments[0][2];
	    moments[2][1] = moments[1][2];
	    moments[2][2] = array[3]/v;
	    return moments;
	}

	double[][] getMoments(double v, SurfaceIterator si, boolean parallel,
			      int size)
	{
	    if (v == 0.0) return new double[3][3];
	    double sign = (v < 0.0)? -1.0: 1.0;
	    double[] array = batched.integrate(si, parallel, size);
	    double[][] moments = new double[3][3];
	    moments[0][0] = array[0]/v;
	    moments[0][1] = array[3]/v;
	    moments[0][2] = array[4]/v;
	    moments[1][0] = moments[0][1];
	    moments[1][1] = array[1]/v;
	    moments[1][2] = array[5]/v;
	    moments[2][0] = moments[0][2];
	    moments[2][1] = moments[1][2];
	    moments[2][2] = array[2]/v;
	    return moments;
	}
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sup>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p)
	throws IllegalArgumentException
    {
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}
	/*
	Adder adder = new Adder.Kahan();
	addVolumeToAdder(adder, shape.getSurfaceIterator(null),  null);
	double v = adder.getSum()/3.0;
	*/
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (size >= MIN_PARALLEL_SIZE_M);
	} else {
	    size = MIN_PARALLEL_SIZE_M;
	}
	MomentData md = new MomentData(p, true);
	return md.getMoments(shape.getSurfaceIterator(null), parallel, size);
	// return momentsOf(shape, p, v);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying an estimate of the number
     * of segments in a shape and whether or not the moments should be
     * computed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param parallel true if the moments should be computed in parallel;
     *        false otherwise
     * @param size an estimate of the number of element in the shape
     *        (ignored if 'parallel' is false)
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}
	/*
	Adder adder = new Adder.Kahan();
	addVolumeToAdder(adder, shape.getSurfaceIterator(null),  null);
	double v = adder.getSum()/3.0;
	*/
	MomentData md = new MomentData(p, true);
	return md.getMoments(shape.getSurfaceIterator(null), parallel, size);
	// return momentsOf(shape, p, v);
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p, double v)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}

	// double sign = (v < 0.0)? -1.0: 1.0;

	MomentData md = new MomentData(p, false);
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (size >= MIN_PARALLEL_SIZE_M);
	} else {
	    size = MIN_PARALLEL_SIZE_M;
	}
	double[][] moments = md.getMoments(v, shape.getSurfaceIterator(null),
					   parallel, size);
	return moments;
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume and whether
     * or not the computation should be performed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p, double v,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}

	// double sign = (v < 0.0)? -1.0: 1.0;

	MomentData md = new MomentData(p, false);
	double[][] moments = md.getMoments(v, shape.getSurfaceIterator(null),
					   parallel, size);
	return moments;
    }


    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume and a
     * flatness parameter.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * Flatness parameters are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param limit the flatness parameter
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p, double v,
				       double limit)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}

	// double sign = (v < 0.0)? -1.0: 1.0;

	MomentData md = new MomentData(p, limit);
	boolean parallel = true;
	int size;
	if (shape instanceof SurfaceOps) {
	    SurfaceOps s = (SurfaceOps)shape;
	    size = s.size();
	    parallel = (size >= MIN_PARALLEL_SIZE_M);
	} else {
	    size = MIN_PARALLEL_SIZE_M;
	}
	double[][] moments = md.getMoments(v, shape.getSurfaceIterator(null),
					   parallel, size);
	return moments;
    }

    /**
     * Compute the moments about axes parallel to the X, Y, and Z axis
     * and located at a point p, specifying the shape's volume, a
     * flatness parameter, and whether or not the computation should
     * be performed in parallel.
     * If p = (p<sub>x</sub>, p<sub>y</sub>, p<sub>z</sub>),
     * r = (r<sub>x</sub>, r<sub>y</sub>, r<sub>z</sub>), and
     * <UL>
     *    <LI>x = r<sub>x</sub> - p<sub>x</sub>
     *    <LI>y = r<sub>y</sub> - p<sub>y</sub>
     *    <LI>z = r<sub>z</sub> - p<sub>z</sub>
     * </UL>
     * the moments returned are
     * <BLOCKQUOTE><PRE>
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
     *    M = | &int;yx/v dV  &int;y<sup>2</sup>/v dV  &int;yz/v dV |
     *        | &int;zx/v dV  &int;zy/v dV  &int;z<sup>2</sup>/v dV |
     * <PRE></BLOCKQUOTE>
     * where M[i][j] corresponds to M<sub>ij</sub> and the integrals
     * are over the volume of the shape and v is the volume of the shape.
     * <P>
     * Flatness parameters are described in the documentation for
     * {@link SurfaceIntegral#SurfaceIntegral(int,RealValuedFunctThreeOps,RealValuedFunctThreeOps,RealValuedFunctThreeOps,double)},
     * and can be used to decrease running time at the expense of a
     * possible reduction in accuracy.
     * <P>
     * The size estimate is used to determine how many threads to
     * use when computing the moments in parallel.
     * @param shape the shape whose moments are computed
     * @param p the point about which to compute the moments
     * @param v the  shape's volume
     * @param limit the flatness parameter
     * @param parallel true if the computation is done in parallel; false
     *        if it is done sequentially.
     * @param size an estimate of the number of segments is the shape
     *        (ignored if the argument 'parallel' is false)
     * @return the moments
     */
    public static double[][] momentsOf(Shape3D shape, Point3D p,
				       double v, double limit,
				       boolean parallel, int size)
	throws IllegalArgumentException
    {
	if (v == 0) throw new IllegalArgumentException(errorMsg("zeroVolume"));
	if (!shape.isOriented()) {
	    throw new IllegalArgumentException(errorMsg("shapeNotOriented"));
	}
	if (!shape.isClosedManifold()) {
	    throw new
		IllegalArgumentException(errorMsg("shapeNotClosedManifold"));
	}

	// double sign = (v < 0.0)? -1.0: 1.0;

	MomentData md = new MomentData(p, limit);
	double[][] moments = md.getMoments(v, shape.getSurfaceIterator(null),
					   parallel, size);
	return moments;
    }
}

//  LocalWords:  exbundle eacute zier vertices isWellFormed ouml bius
//  LocalWords:  stacktrace scrunner trustLevel si coords cindex th
//  LocalWords:  SecurityException Appendable IllegalStateException
//  LocalWords:  IllegalArgumentException computeComponents getCP li
//  LocalWords:  initialCapacity eindex edgeNumber findReverse qarray
//  LocalWords:  arraycopy controlPoints tform SurfaceIterator setupV
//  LocalWords:  getSurfaceIterator blockquote ccoords pcoords setupU
//  LocalWords:  indices ForPatch boolean dudv CP CPij polynomial's
//  LocalWords:  urange vrange Pdsdt BicubicInerpolator inits MTI pre
//  LocalWords:  ij setupRestForPatch PathIterator scoords lastX HREF
//  LocalWords:  lastY lastZ currentSegment addCubicTriangle offseted
//  LocalWords:  offsetted argOutOfRange npatches php Farin toCubic
//  LocalWords:  setupCubic ForTriangle setupW setupCP setupPlanarCP
//  LocalWords:  STL wv uv vv Bezier unknownType nCT nCP notOriented
//  LocalWords:  notWellFormed notClosedManifold unrecognizedPath SEG
//  LocalWords:  DType illegalComponentIndex MOVETO freeBarycentric
//  LocalWords:  wrongTyp addCubicPatch addPlanarTriangle carray pdf
//  LocalWords:  bezier Shi Hu ndash tcoords toffset poffset rx ry rz
//  LocalWords:  wrongType integral's refPoint getSegment subclasses
//  LocalWords:  ith IOException StackTrace lineTo moveTo ci tmp xval
//  LocalWords:  MatrixOps multipleEdges getBoundaryTags barycentric
//  LocalWords:  boundaryComputed computeBoundary boundarySegments dV
//  LocalWords:  getSegmentTag printit Kahan adderPT adderCT adderCP
//  LocalWords:  VectorOps dotCrossProduct volumeWeightsCT getSum xy
//  LocalWords:  volumeWeightsCP iterator's shapeNotOriented yval xz
//  LocalWords:  shapeNotClosedManifold addVolumeToAdder zeroVolume
//  LocalWords:  centerOfMassOf zval SurfaceIntegral yx yz zx zy vx
//  LocalWords:  RealValuedFunctThreeOps momentsOf QUADTO CUBICTO vy
//  LocalWords:  vz addAreaToAdder
