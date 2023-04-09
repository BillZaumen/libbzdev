package org.bzdev.geom;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bzdev.math.Adder;
import org.bzdev.math.VectorOps;
import org.bzdev.math.VectorValuedGLQ;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * A 3D shape consisting of a 'center' point connecting to a path
 * using a sequence of Bezier vertices.  The path will typically
 * be a boundary of some other shape.  The shape of this object is
 * determined by the center point, a vector defining a direction more
 * or less perpendicular to the boundary, and a height relative to the
 * center point and the direction of the vector.
 * <P>
 * This class can be used to create cones and similar shapes.
 */
public class BezierVertex implements Shape3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    private static Point3D ourFindCenter(Path3D boundary)
	throws IllegalArgumentException
    {
	Point3D center = BezierCap.findCenter(boundary);
	if (center == null) {
	    throw new IllegalArgumentException(errorMsg("zeroLengthPath", 1));
	}
	return center;
    }

    private static Point3D findVertex(Path3D boundary, Point3D center,
				      double height) {
	double[] vector = BezierCap.findVector(boundary, center);
	VectorOps.normalize(vector);
	// BezierCap.findVector returns a vector pointing in the
	// the opposite direction from what one would expect using
	// a right-hand rule because a Bezier Cap is used to fill a
	// hole in a surface.
	if (height != 0.0) height = -height;
	VectorOps.multiply(vector, 0, height, vector, 0, 3);
	double x = (double)(float)(vector[0] + center.getX());
	double y = (double)(float)(vector[1] + center.getY());
	double z = (double)(float)(vector[2] + center.getZ());
	return new Point3D.Double(x, y, z);
    }


    double[] cpoints;
    Color[] colors;
    Color defaultColor = null;
    Object tag = null;
  
    /**
     * Set this object's tag.
     * @param tag the tag
     */
    public void setTag(Object tag) {
	this.tag = tag;
    }

    /**
     * Get this object's tag.
     * @return the tag
     */
    public Object getTag() {
	return tag;
    }


    /**
     * Constructor.
     * @param path a path
     * @param vertex the vertex
     */
    public BezierVertex(Path3D path, Point3D vertex) {
	this(path, vertex.getX(), vertex.getY(), vertex.getZ());
    }

    /**
     * Constructor given a vertex height.
     * @param path a path
     * @param height the height of the vertex
     */
    public BezierVertex(Path3D path, double height) {
	this(path, findVertex(path, ourFindCenter(path), height));
    }

    /**
     * Constructor given a vertex height and path center point.
     * @param path a path
     * @param center the center point
     * @param height the height of the vertex
     */
    public BezierVertex(Path3D path, Point3D center, double height) {
	this(path, findVertex(path, center, height));
    }

    /**
     * Constructor given explicit vertex coordinates.
     * @param path a path
     * @param vx the X coordinate of the vertex
     * @param vy the Y coordinate of the vertex
     * @param vz the Z coordinate of the vertex
     */
    public BezierVertex(Path3D path, double vx, double vy, double vz) {
	PathIterator3D pit = path.getPathIterator(null);
	double lastx = 0.0, lasty = 0.0, lastz = 0.0;
	double startx = 0.0, starty = 0.0, startz = 0.0;
	ArrayList<double[]> list = new ArrayList<>();
	double[] coords = new double[9];
	boolean more = true;
	boolean first = true;
	double[] tmp = new double[15];
	double cparray[];
	while (!pit.isDone() && more) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		if (!first) {
		    more = false;
		    continue;
		}
		first = false;
		lastx = coords[0];
		lasty = coords[1];
		lastz = coords[2];
		startx = lastx;
		starty = lasty;
		startz = lastz;
		break;
	    case PathIterator3D.SEG_LINETO:
		Path3DInfo.elevateDegree(1, tmp, lastx, lasty, lastz, coords);
		Path3DInfo.elevateDegree(2, coords, lastx, lasty, lastz, tmp);
		cparray = new double[15];
		cparray[0] = lastx;
		cparray[1] = lasty;
		cparray[2] = lastz;
		System.arraycopy(coords, 0, cparray, 3, 9);
		cparray[12] = vx;
		cparray[13] = vy;
		cparray[14] = vz;
		lastx = cparray[9];
		lasty = cparray[10];
		lastz = cparray[11];
		list.add(cparray);
		break;
	    case PathIterator3D.SEG_QUADTO:
		System.arraycopy(coords, 0, tmp, 0, 6);
		Path3DInfo.elevateDegree(2, coords, lastx, lasty, lastz, tmp);
		cparray = new double[15];
		cparray[0] = lastx;
		cparray[1] = lasty;
		cparray[2] = lastz;
		System.arraycopy(coords, 0, cparray, 3, 9);
		cparray[12] = vx;
		cparray[13] = vy;
		cparray[14] = vz;
		lastx = cparray[9];
		lasty = cparray[10];
		lastz = cparray[11];
		list.add(cparray);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		cparray = new double[15];
		cparray[0] = lastx;
		cparray[1] = lasty;
		cparray[2] = lastz;
		System.arraycopy(coords, 0, cparray, 3, 9);
		cparray[12] = vx;
		cparray[13] = vy;
		cparray[14] = vz;
		lastx = cparray[9];
		lasty = cparray[10];
		lastz = cparray[11];
		list.add(cparray);
		break;
	    case PathIterator3D.SEG_CLOSE:
		if (((float)lastx != (float)startx)
		    || ((float)lasty != (float)starty)
		    || ((float)lastz != (float)startz)) {
		    coords[0] = startx;
		    coords[1] = starty;
		    coords[2] = startz;
		    Path3DInfo.elevateDegree(1, tmp,
					     lastx, lasty, lastz, coords);
		    Path3DInfo.elevateDegree(2, coords,
					     lastx, lasty, lastz, tmp);
		    cparray = new double[15];
		    cparray[0] = lastx;
		    cparray[1] = lasty;
		    cparray[2] = lastz;
		    System.arraycopy(coords, 0, cparray, 3, 9);
		    cparray[12] = vx;
		    cparray[13] = vy;
		    cparray[14] = vz;
		    list.add(cparray);
		}
		lastx = startx;
		lasty = starty;
		lastz = startz;
		more = false;
		break;
	    }
	    pit.next();
	}
	int sz = list.size();
	colors = new Color[sz];
	sz *= 15;
	cpoints = new double[sz];
	int last = 0;
	for (double[] carray: list) {
	    System.arraycopy(carray, 0, cpoints, last, 15);
	    last += 15;
	}
    }


    boolean flipped = false;

    /**
     * Change the orientation of the B&eacute;zier triangles associated
     * with this object.
     * This method affects the orientation of triangles provided by iterators.
     * It does not change the values returned by calling methods
     * such as .
     * @param reverse true if the orientation is the reverse of
     *        the one that was initially defined; false if the orientation is
     *        the same as the one that was initially defined.
     */
    public void reverseOrientation(boolean reverse) {
	flipped = reverse;
    }

    /**
     * Reverse the orientation of this object.
     */
    public void flip() {
	flipped = !flipped;
    }

    /**
     * Determine if the orientation for this grid is reversed.
     * @return true if the orientation is reversed; false if not
     */
    public boolean isReversed() {return flipped;}

    /**
     * Set the default color for this shape.
     * @param c the color
     */
    public void setColor(Color c) {
	defaultColor = c;
    }


    /**
     * Set the color for a B&eacute;zier triangle specified by an index
     * @param i the index
     * @param c the color
     */
    public void setColor(int i, Color c) {
	colors[i] = c;
    }

    /**
     * Get the default color for this shape.
     * This is the color for the shape, excluding B&eacute;zier triangles
     * for which an explicit color has been specified.
     * @return the color
     */
    public Color getColor() {
	return defaultColor;
    }


    /**
     * Get the  color for this shape given an index.
     * This is the color for the shape, excluding B&eacute;zier triangles
     * for which an explicit color has been specified.
     * When a color for a specific index has been provided, that color is
     * returned; otherwise the default color (if any) is returned.
     * @param i the index of a B&eacute;zier triangle contained in this
     *        B&eacute;zier vertex
     * @return the color
     */
    public Color getColor(int i) {
	Color c = colors[i];
	return (c == null)? defaultColor: c;
    }



    /**
     * Print this object's control points.
     * @exception IOException an IO error occurred
     */
    public void print() throws IOException {
	print(System.out);
    }

    /**
     * Print this object's control points, specifying an output.
     * @param out the output
     * @exception IOException an IO error occurred
     */
    public void print(Appendable out) throws IOException {
	print("", out);
    }

    /**
     * Print this object's control points, specifying a prefix.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * @param prefix the prefix
     * @exception IOException an IO error occurred
     */
    public void print(String prefix) throws IOException {
	print(prefix, System.out);
    }

    /**
     * Print this object's control points, specifying a prefix and output.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * @param prefix the prefix
     * @param out the output
     * @exception IOException an IO error occurred
     */
    public void print(String prefix, Appendable out) throws IOException {
	out.append(String.format("%snumber of cubic vertices: %d\n", prefix,
				 colors.length));
	int len = colors.length;
	for (int i = 0; i < len; i++) {
	    int offset = i*15;
	    out.append(String.format("%scubic vertex %d:\n", prefix, i));
	    out.append(String.format("%s    cubic curve:\n", prefix));
	    for(int k = 0; k < 12; k += 3) {
		out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					 cpoints[offset+k],
					 cpoints[offset=k+1],
					 cpoints[offset+k+2]));
	    }
	    out.append(String.format("%s    vertex:\n", prefix));
	    out.append(String.format("%s        (%g, %g, %g)\n", prefix,
				     cpoints[offset+12],
				     cpoints[offset+13],
				     cpoints[offset+14]));
	}
    }


    // Shape3D methods

    private class Iterator1 implements SurfaceIterator {
	boolean flip = flipped;
	int index = 0;
	boolean done = false;
	Iterator1() {
	}

	@Override
	public Color currentColor() {
	    if (colors.length == 0) return defaultColor;
	    Color c = colors[index];
	    if (c == null) c = defaultColor;
	    return c;
	}


	@Override
	public int currentSegment(double[] coords) {
	    int offset = index*15;
	    if (offset < cpoints.length) {
		if (flip) {
		    System.arraycopy(cpoints, offset, coords, 9, 3);
		    System.arraycopy(cpoints, offset+3, coords, 6, 3);
		    System.arraycopy(cpoints, offset+6, coords, 3, 3);
		    System.arraycopy(cpoints, offset+9, coords, 0, 3);
		    System.arraycopy(cpoints, offset+12, coords, 12, 3);
		} else {
		    System.arraycopy(cpoints, offset, coords, 0, 15);
		}
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}
	
	double[] tcoords = null;

	@Override
	public int currentSegment(float[] coords) {
	    if (index == colors.length) {
		return SurfaceIterator.CUBIC_VERTEX;
	    }
	    if (tcoords == null) tcoords = new double[15];
	    currentSegment(tcoords);
	    for (int k = 0; k < 15; k++) {
		coords[k] = (float)tcoords[k];
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return index >= colors.length;
	}

	@Override
	public void next() {
	    if (index < colors.length) index++;
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }

    private class Iterator2 implements SurfaceIterator {
	boolean flip = flipped;
	int index = 0;
	boolean done = false;
	/*
	 * tform will never be null because getSurfaceIterator
	 * will create an instance of Iterator1 if the transform is null.
	 */
	Transform3D tform;

	Iterator2(Transform3D tform) {
	    this.tform = tform;
	}

	@Override
	public Color currentColor() {
	    if (colors.length == 0) return defaultColor;
	    Color c = colors[index];
	    if (c == null) c = defaultColor;
	    return c;
	}

	double[] tcoords = null;

	@Override
	public int currentSegment(double[] coords) {
	    if (flip && tcoords == null) tcoords = new double[15];
	    int offset = index*15;
	    if (offset < cpoints.length) {
		if (flip) {
		    System.arraycopy(cpoints, offset, tcoords, 9, 3);
		    System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
		    System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
		    System.arraycopy(cpoints, offset+9, tcoords, 0, 3);
		    System.arraycopy(cpoints, offset+12, tcoords, 12, 3);
		    tform.transform(tcoords, 0, coords, 0, 5);
		} else {
		    tform.transform(cpoints, offset, coords, 0, 5);
		}
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}
	
	@Override
	public int currentSegment(float[] coords) {
	    if (flip && tcoords == null) tcoords = new double[15];
	    int offset = index*15;
	    if (offset < cpoints.length) {
		if (flip) {
		    System.arraycopy(cpoints, offset, tcoords, 9, 3);
		    System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
		    System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
		    System.arraycopy(cpoints, offset+9, tcoords, 0, 3);
		    System.arraycopy(cpoints, offset+12, tcoords, 12, 3);
		    tform.transform(tcoords, 0, coords, 0, 5);
		} else {
		    tform.transform(cpoints, offset, coords, 0, 5);
		}
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return index <- colors.length;
	}

	@Override
	public void next() {
	    if (index < colors.length) index++;
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	if (tform == null) {
	    return new Iterator1();
	} else {
	    return new Iterator2(tform);
	}
    }

    @Override
    public final synchronized
    SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	if (tform == null) {
	    if (level == 0) {
		return new Iterator1();
	    } else {
		return new SubdivisionIterator(new Iterator1(), level);
	    }
	} else {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator1(), tform, level);
	    }
	}
    }

    Rectangle3D brect = null;

    boolean bpathSet = false;
    Path3D bpath = null;

    @Override
    public Path3D getBoundary() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return bpath;
    }

    /**
     * Determine if this BezierVertex is well formed.
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null);
    }

    /**
     * Determine if this BezierVertex is well formed, logging error messages to
     * an Appendable.
     * @param out an Appendable for logging error messages
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed(Appendable out) {
	Surface3D.Boundary boundary
	    = new Surface3D.Boundary(getSurfaceIterator(null), out);
	if (!bpathSet) { 
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (boundary.getPath() != null);
    }


    @Override
    public  Shape3D getComponent(int i)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= 1) {
	    String msg = errorMsg("illegalComponentIndex", i, 1);
	    throw new IllegalArgumentException(msg);
	}
	return this;
    }

    @Override
    public Rectangle3D getBounds() {
	if (brect == null) {
	    double[] coords = new double[48];
	    SurfaceIterator sit = getSurfaceIterator(null);
	    while (!sit.isDone()) {
		sit.currentSegment(coords);
		if (brect == null) {
		    brect = new
			Rectangle3D.Double(coords[0], coords[1], coords[2],
					   0.0, 0.0, 0.0);
		    for (int i = 3; i < 30; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		} else {
		    for (int i = 0; i < 30; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		}
		sit.next();
	    }
	}
	return brect;
    }


    @Override
    public boolean isClosedManifold() {
	return false;
	/*
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null) && bpath.isEmpty();
	*/
    }
    
    @Override
    public boolean isOriented() {return true;}

    @Override    
    public int numberOfComponents() {
	return colors.length;
    }
}

//  LocalWords:  exbundle bezier pdf getSpoke tmp circCoords CPT SEG
//  LocalWords:  VectorOps dotProduct DInfo coords MOVETO flen glq yy
//  LocalWords:  integrateWithP len SegmentData fst zz fcoords getSum
//  LocalWords:  zeroLengthPath nullArg getCenterY getCenterZ addrx
//  LocalWords:  GLQuadrature segmentTermX lastx lasty lastz addry CP
//  LocalWords:  segmentTermY addrz segmentTermZ findVector boolean
//  LocalWords:  PathIterator getPathIterator initx inity initz cmode
//  LocalWords:  isDone currentSegment arraycopy LINETO QUADTO eacute
//  LocalWords:  CUBICTO crossProduct findCenter zier illegalIndex cp
//  LocalWords:  snumber scubic setupCP ForTriangle setupV setupU
//  LocalWords:  SubdivisionIterator setupW tform getSurfaceIterator
//  LocalWords:  Appendable illegalComponentIndex
