package org.bzdev.geom;

import org.bzdev.lang.UnexpectedExceptionError;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class implementing paths in a three-dimensional Euclidean space.
 * The API is similar to that for {@link java.awt.geom.Path2D} as
 * users are likely to be familiar with that class.
 * <P>
 * Implementation note: all the subclasses of this class must be
 * in the package org.bzdev.geom.
 */
abstract public class Path3D implements Cloneable {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    // Point3D lastMoveTo = null;
    boolean closed = false;

    static final int INITIAL_SIZE = 32;
    static int MAX_INCR = 256;
    static int MAX_INCR_3 = MAX_INCR * 3;

    int[] types;
    int lastMoveTo = -1;
    int index = 0;
    int cindex = 0;

    /**
     * Determine if this path is empty.
     * An empty path contains no path segments and does not have
     * a bounding box.
     * @return true of the path is empty; false otherwise
     */
    public boolean isEmpty() {
	return index == 0;
    }

    Path3D() {
	types = new int[INITIAL_SIZE];
    }

    Path3D(int initialCapacity) {
	types = new int[initialCapacity];
    }

    abstract void copyCoords(int sInd, int dInd, int n);

    void expandIfNeeded(int n) {
	// n ignored - used by subclasses
	if ((index + 1) < types.length) {
	    return;
	}
	int incr = index;
	if (incr > MAX_INCR) incr = MAX_INCR;
	int newsize = types.length + incr;
	int[] tmp = new int[newsize];
	System.arraycopy(types, 0, tmp, 0, index);
	types = tmp;
    }

    static abstract class Iterator implements PathIterator3D {
	int index = 0;
	int pathIndex;
	int[] pathTypes;
	int cindex = 0;
	Path3D path;

	int numbCoords(int type) {
	    switch (type) {
	    case PathIterator3D.SEG_CLOSE:
		return 3;
	    case PathIterator3D.SEG_CUBICTO:
		return 9;
	    case PathIterator3D.SEG_MOVETO:
 		return 3;
	    case PathIterator3D.SEG_LINETO:
		return 3;
	    case PathIterator3D.SEG_QUADTO:
		return 6;
	    default:
		return 0;
	    }
	}

	Iterator(Path3D path) {
	    this.path = path;
	    pathIndex = path.index;
	    pathTypes = path.types;
	}

	@Override
	public boolean isDone() {
	    return index >= pathIndex;
	}

	@Override
	public void next() {
	    if (index < path.index) {
		int type = pathTypes[index++];
		cindex += numbCoords(type);
	    }
	}
    }

    /**
     * Class extending {@link Path3D} by storing coordinate data as
     * double-precision numbers.
     */
    public static class Double extends Path3D {
	double coords[] = new double[INITIAL_SIZE * 3];


	/**
	 * Constructor.
	 */
	public Double() {
	    super();
	    coords = new double[INITIAL_SIZE * 3];
	};

	/**
	 * Constructor giving an estimate of the number of path segments.
	 * @param initialCapacity the initial number of path segments
	 *        allocated in an expandable table
	 */
	public Double(int initialCapacity) {
	    super(initialCapacity);
	    coords = new double[initialCapacity*3];
	}

	/**
	 * Constructor using another path to determine the initial
	 * path segments.
	 * @param path the path whose segments will be copied
	 */
	public Double(Path3D path) {
	    this(path.types.length);
	    append(path.getPathIterator(null), false);
	}

	/**
	 * Constructor using another path to determine the initial
	 * path segments, modified with a 3D transform.
	 * @param path the path whose segments will be copied
	 * @param transform the transform to apply to the path
	 *        segments
	 */
	public Double(Path3D path, Transform3D transform) {
	    this(path.types.length);
	    append(path.getPathIterator(transform), false);
	}

	/**
	 * Constructor given a 2D path and a mapper.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * The mapper is passed a default mapper index of 0, so this
	 * constructor will typically be used when that index is ignored,
	 * e.g., when the mapper is used to create a single path.
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @see Point3DMapper
	 */
	public Double(Path2D path, Point3DMapper<Point3D> mapper) {
	    this(INITIAL_SIZE, path, mapper, 0);
	}

	/**
	 * Constructor given a 2D path, a mapper, and an index.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * When a mapper is used to create a single path, the mapper
	 * index will typically be ignored, and the
	 * constructor {@link Path3D.Double#Double(Path2D,Point3DMapper)}
	 * will result in more easily read code. A mapper index is
	 * used implicitly by the constructor
	 * {@link BezierGrid#BezierGrid(Path2D,Point3DMapper,int,boolean)},
	 * which can be used to create surfaces of revolution.
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @param mapperIndex an index used to select a mapping
	 * @see Point3DMapper
	 */
	public Double(Path2D path, Point3DMapper<Point3D> mapper,
		      int mapperIndex) {
	    this(INITIAL_SIZE, path, mapper, mapperIndex);
	}

	/**
	 * Constructor given a 2D path and a 3D transform.
	 * Each control point of the 2D path is converted to a 3D point
	 * by using the same X and Y values and setting the Z value to
	 * 0 and this new point is then transformed.
	 * @param path the 2D path
	 * @param tform the transform to apply to each control point
	 */
	public Double(Path2D path, final Transform3D tform) {
	    this(INITIAL_SIZE, path, (i, p, type, bounds) -> {
		    return tform.transform(p, null);
		}, 0);
	}

	/**
	 * Constructor given a 2D path, a 3D transform, and an initial
	 * capacity.
	 * Each control point of the 2D path is converted to a 3D point
	 * by using the same X and Y values and setting the Z value to
	 * 0 and this new point is then transformed.
	 * @param path the 2D path
	 * @param tform the transform to apply to each control point
	 */
	public Double(int initialCapacity, Path2D path, final Transform3D tform)
	{
	    this(initialCapacity, path, (i, p, type, bounds) -> {
		    return tform.transform(p, null);
		}, 0);
	}

	/**
	 * Constructor given a 2D path and an initial capacity.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * When a mapper is used to create a single path, the mapper
	 * index will typically be ignored, and the
	 * constructor {@link Path3D.Double#Double(Path2D,Point3DMapper)}
	 * will result in more easily read code. A mapper index is
	 * used implicitly by the constructor
	 * {@link BezierGrid#BezierGrid(Path2D,Point3DMapper,int,boolean)},
	 * which can be used to create surfaces of revolution.
	 * @param initialCapacity the initial number of path segments
	 *        allocated in an expandable table
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @param mapperIndex an index used to select a mapping
	 * @see Point3DMapper
	 */
	public Double(int initialCapacity, Path2D path,
		      Point3DMapper<Point3D> mapper, int mapperIndex)
	{
	    super(initialCapacity);
	    PathIterator pi = path.getPathIterator(null);
	    double[] coords = new double[6];
	    double[] ncoords = new double[9];
	    double startx = 0.0, starty = 0.0;
	    double lastx = 0.0, lasty = 0.0;
	    Point2D p2 = null;
	    Point2D p2a = null;
	    Point2D p2b = null;
	    Point3D p3 = null;
	    Point3D p4 = null;
	    Point3D p5 = null;
	    Point3D p6 = null;

	    while (!pi.isDone()) {
		switch (pi.currentSegment(coords)) {
		case PathIterator.SEG_MOVETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    startx = lastx;
		    starty = lasty;
		    p2 = new Point2D.Double(lastx, lasty);
		    p2a = p2;
		    p3 = mapper.apply(mapperIndex, p2, Point3DMapper.Type.KNOT);
		    moveTo(p3.getX(), p3.getY(), p3.getZ());
		    break;
		case PathIterator.SEG_LINETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    p2 = new Point2D.Double(lastx, lasty);
		    p2a = p2;
		    p3 = mapper.apply(mapperIndex, p2, Point3DMapper.Type.KNOT);
		    lineTo(p3.getX(), p3.getY(), p3.getZ());
		    break;
		case PathIterator.SEG_QUADTO:
		    lastx = coords[2];
		    lasty = coords[3];
		    p2b = new Point2D.Double(lastx, lasty);
		    p2 = new Point2D.Double(coords[0], coords[1]);
		    p3 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.QUADRATIC, p2a, p2b);
		    p4 = mapper.apply(mapperIndex, p2b,
				      Point3DMapper.Type.KNOT);
		    quadTo(p3.getX(), p3.getY(), p3.getZ(),
			   p4.getX(), p4.getY(), p4.getZ());
		    p2a = p2b;
		    break;
		case PathIterator.SEG_CUBICTO:
		    lastx = coords[4];
		    lasty = coords[5];
		    p2b = new Point2D.Double(lastx, lasty);
		    p2 = new Point2D.Double(coords[0], coords[1]);
		    p3 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.FIRST_CUBIC, p2a, p2b);
		    p2 = new Point2D.Double(coords[2], coords[3]);
		    p4 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.SECOND_CUBIC,
				      p2a, p2b);
		    p5 = mapper.apply(mapperIndex, p2b,
				      Point3DMapper.Type.KNOT);
		    curveTo(p3.getX(), p3.getY(), p3.getZ(),
			    p4.getX(), p4.getY(), p4.getZ(),
			    p5.getX(), p5.getY(), p5.getZ());
		    p2a = p2b;
		    break;
		case PathIterator.SEG_CLOSE:
		    lastx = startx;
		    lasty = starty;
		    closePath();
		    p2a = new Point2D.Double(startx, starty);
		    break;
		}
		pi.next();
	    }
	}

	Point3D createPoint(int cindex) {
	    return new Point3D.Double(coords[cindex], coords[cindex+1],
				      coords[cindex+2]);
	}

	void expandIfNeeded(int n) {
	    super.expandIfNeeded(n);
	    if ((cindex + n*3) < coords.length) {
		return;
	    }
	    int incr = cindex;
	    if (incr > MAX_INCR_3) incr = MAX_INCR_3;
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
	    Iterator1(Path3D.Double path) {
		super(path);
	        coords = path.coords;
	    }
	    
	    @Override
	    public int currentSegment(float[] fc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    for (int i = 0; i < len; i++) {
			fc[i] = (float) coords[cindex+i];
		    }
		}
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    System.arraycopy(coords, cindex, dc, 0, len);
		}
		return type;
	    }
	}
	
	static class Iterator2 extends Iterator {
	    double[] coords;
	    Transform3D transform;
	    Iterator2(Path3D.Double path, Transform3D transform) {
		super(path);
	        coords = path.coords;
		this.transform = transform;
	    }
	    
	    @Override
	    public int currentSegment(float[] fc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    transform.transform(coords, cindex, fc, 0, len/3);
		}
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    transform.transform(coords, cindex, dc, 0, len/3);
		}
		return type;
	    }
	}


	private double[] tmpCoords = new double[9];

	@Override
	public final synchronized void append(PathIterator3D pi,
					      boolean connect)
	{
	    if (pi.isDone()) return;
	    if (pi.currentSegment(tmpCoords)!=PathIterator3D.SEG_MOVETO) {
		String msg = errorMsg("missingMOVETO");
		throw new IllegalArgumentException(msg);
	    }
	    if (cindex == 0) {
		moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
	    } else {
		if (connect) {
		    if (coords[cindex-3] != tmpCoords[0]
			|| coords[cindex-2] != tmpCoords[1]
			|| coords[cindex-1] != tmpCoords[2]) {
			lineTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    }
		} else {
		    moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		}
	    }
	    pi.next();
	    while (!pi.isDone()) {
		switch (pi.currentSegment(tmpCoords)) {
		case PathIterator3D.SEG_MOVETO:
		    moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    break;
		case PathIterator3D.SEG_LINETO:
		    lineTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    break;
		case PathIterator3D.SEG_QUADTO:
		    quadTo(tmpCoords[0], tmpCoords[1], tmpCoords[2],
			   tmpCoords[3], tmpCoords[4], tmpCoords[5]);
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    curveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2],
			    tmpCoords[3], tmpCoords[4], tmpCoords[5],
			    tmpCoords[6], tmpCoords[7], tmpCoords[8]);
		    break;
		case PathIterator3D.SEG_CLOSE:
		    closePath();
		    break;
		}
		pi.next();
	    }
	}

	@Override
	public Object clone() {
	    return super.clone();
	}

	@Override
	public final Path3D createTransformedPath(Transform3D transform) {
	    Path3D.Double result = new Path3D.Double();
	    result.append(getPathIterator(transform), false);
	    return result;
	}

	@Override
	public final synchronized void curveTo(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3)
	{
	    expandIfNeeded(3);
	    types[index++] = PathIterator3D.SEG_CUBICTO;
	    coords[cindex++] = x1;
	    coords[cindex++] = y1;
	    coords[cindex++] = z1;
	    coords[cindex++] = x2;
	    coords[cindex++] = y2;
	    coords[cindex++] = z2;
	    coords[cindex++] = x3;
	    coords[cindex++] = y3;
	    coords[cindex++] = z3;
	}

	@Override
	public final synchronized
	PathIterator3D getPathIterator(Transform3D tform) {
	    if (tform == null) {
		return new Iterator1(this);
	    } else {
		return new Iterator2(this, tform);
	    }
	}

	@Override
	public final synchronized void lineTo(double x, double y, double z) {
	    expandIfNeeded(1);
	    types[index++] = PathIterator3D.SEG_LINETO;
	    coords[cindex++] = x;
	    coords[cindex++] = y;
	    coords[cindex++] = z;
	}

	@Override
	public final synchronized void moveTo(double x, double y, double z) {
	    expandIfNeeded(1);
	    lastMoveTo = index;
	    types[index++] = PathIterator3D.SEG_MOVETO;
	    coords[cindex++] = x;
	    coords[cindex++] = y;
	    coords[cindex++] = z;
	}

	@Override
	public final synchronized  void quadTo(double x1, double y1, double z1,
					       double x2, double y2, double z2)
	{
	    expandIfNeeded(2);
	    types[index++] = PathIterator3D.SEG_QUADTO;
	    coords[cindex++] = x1;
	    coords[cindex++] = y1;
	    coords[cindex++] = z1;
	    coords[cindex++] = x2;
	    coords[cindex++] = y2;
	    coords[cindex++] = z2;
	}

	@Override
	public final synchronized void transform(Transform3D tform) {
	    expandIfNeeded(1);
	    System.arraycopy(coords, 0, coords, 3, coords.length);
	    tform.transform(coords, 3, coords, 0, coords.length/3);
	}

	@Override
	public Point3D getStart() {
	    if (index == 0) return null;
	    return new Point3D.Double(coords[0], coords[1], coords[2]);
	}
	@Override
	public Point3D getEnd() {
	    if (index == 0 || types[index-1] == PathIterator3D.SEG_CLOSE) {
		return null;
	    }
	    return new  Point3D.Double(coords[cindex-3],
				       coords[cindex-2],
				       coords[cindex-1]);
	}
    }

    /**
     * Class extending {@link Path3D} by storing coordinate data as
     * single-precision numbers.
     */
    public static class Float extends Path3D {
	float coords[];

	/**
	 * Constructor.
	 */
	public Float() {
	    super();
	    coords = new float[INITIAL_SIZE * 3];
	};

	/**
	 * Constructor giving an estimate of the number of path segments.
	 * @param initialCapacity the initial number of path segments
	 *        allocated in an expandable table
	 */
	public Float(int initialCapacity) {
	    super(initialCapacity);
	    coords = new float[initialCapacity*3];
	}

	/**
	 * Constructor using another path to determine the initial
	 * path segments.
	 * @param path the path whose segments will be copied
	 */
	public Float(Path3D path) {
	    this(path.types.length);
	    append(path.getPathIterator(null), false);
	}

	/**
	 * Constructor using another path to determine the initial
	 * path segments, modified with a 3D transform.
	 * @param path the path whose segments will be copied
	 * @param transform the transform to apply to the path
	 *        segments
	 */
	public Float(Path3D path, Transform3D transform) {
	    this(path.types.length);
	    append(path.getPathIterator(transform), false);
	}

	/**
	 * Constructor given a 2D path and a mapper.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * The mapper is passed a default mapper index of 0, so this
	 * constructor will typically be used when that index is ignored,
	 * e.g., when the mapper is used to create a single path.
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @see Point3DMapper
	 */
	public Float(Path2D path, Point3DMapper<Point3D> mapper) {
	    this(INITIAL_SIZE, path, mapper, 0);
	}
	/**
	 * Constructor given a 2D path.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * When a mapper is used to create a single path, the mapper
	 * index will typically be ignored, and the
	 * constructor {@link Path3D.Float#Float(Path2D,Point3DMapper)}
	 * will result in more easily read code.
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @param mapperIndex an index used to select a mapping
	 * @see Point3DMapper
	 */
	public Float(Path2D path, Point3DMapper<Point3D> mapper,
		      int mapperIndex) {
	    this(INITIAL_SIZE, path, mapper, mapperIndex);
	}

	/**
	 * Constructor given a 2D path and a 3D transform.
	 * Each control point of the 2D path is converted to a 3D point
	 * by using the same X and Y values and setting the Z value to
	 * 0 and this new point is then transformed.
	 * @param path the 2D path
	 * @param tform the transform to apply to each control point
	 */
	public Float(Path2D path, final Transform3D tform) {
	    this(INITIAL_SIZE, path, (i, p, type, bounds) -> {
		    return tform.transform(p, null);
		}, 0);
	}

	/**
	 * Constructor given a 2D path, a 3D transform, and an initial
	 * capacity.
	 * Each control point of the 2D path is converted to a 3D point
	 * by using the same X and Y values and setting the Z value to
	 * 0 and this new point is then transformed.
	 * @param path the 2D path
	 * @param tform the transform to apply to each control point
	 */
	public Float(int initialCapacity, Path2D path, final Transform3D tform)
	{
	    this(initialCapacity, path, (i, p, type, bounds) -> {
		    return tform.transform(p, null);
		}, 0);
	}

	/**
	 * Constructor given a 2D path and an initial capacity.
	 * An instance of Point3DMapper is used to map the 2D path's
	 * control points to the corresponding control points of the
	 * 3D path.
	 * <P>
	 * When a mapper is used to create a single path, the mapper
	 * index will typically be ignored, and the
	 * constructor {@link Path3D.Double#Double(Path2D,Point3DMapper)}
	 * will result in more easily read code. A mapper index is
	 * used implicitly by the constructor
	 * {@link BezierGrid#BezierGrid(Path2D,Point3DMapper,int,boolean)},
	 * which can be used to create surfaces of revolution.
	 * @param initialCapacity the initial number of path segments
	 *        allocated in an expandable table
	 * @param path the 2D path
	 * @param mapper the object that converts a 2D point to a 3D point
	 * @param mapperIndex an index used to select a mapping
	 * @see Point3DMapper
	 */
	public Float(int initialCapacity, Path2D path,
		      Point3DMapper<Point3D> mapper, int mapperIndex)
	{
	    super(initialCapacity);
	    PathIterator pi = path.getPathIterator(null);
	    double[] coords = new double[6];
	    double[] ncoords = new double[9];
	    double startx = 0.0, starty = 0.0;
	    double lastx = 0.0, lasty = 0.0;
	    Point2D p2 = null;
	    Point2D p2a = null;
	    Point2D p2b = null;
	    Point3D p3 = null;
	    Point3D p4 = null;
	    Point3D p5 = null;
	    Point3D p6 = null;

	    while (!pi.isDone()) {
		switch (pi.currentSegment(coords)) {
		case PathIterator.SEG_MOVETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    startx = lastx;
		    starty = lasty;
		    p2 = new Point2D.Double(lastx, lasty);
		    p2a = p2;
		    p3 = mapper.apply(mapperIndex, p2, Point3DMapper.Type.KNOT);
		    moveTo(p3.getX(), p3.getY(), p3.getZ());
		    break;
		case PathIterator.SEG_LINETO:
		    lastx = coords[0];
		    lasty = coords[1];
		    p2 = new Point2D.Double(lastx, lasty);
		    p2a = p2;
		    p3 = mapper.apply(mapperIndex, p2, Point3DMapper.Type.KNOT);
		    lineTo(p3.getX(), p3.getY(), p3.getZ());
		    break;
		case PathIterator.SEG_QUADTO:
		    lastx = coords[2];
		    lasty = coords[3];
		    p2b = new Point2D.Double(lastx, lasty);
		    p2 = new Point2D.Double(coords[0], coords[1]);
		    p3 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.QUADRATIC, p2a, p2b);
		    p4 = mapper.apply(mapperIndex, p2b,
				      Point3DMapper.Type.KNOT);
		    quadTo(p3.getX(), p3.getY(), p3.getZ(),
			   p4.getX(), p4.getY(), p4.getZ());
		    p2a = p2b;
		    break;
		case PathIterator.SEG_CUBICTO:
		    lastx = coords[4];
		    lasty = coords[5];
		    p2b = new Point2D.Double(lastx, lasty);
		    p2 = new Point2D.Double(coords[0], coords[1]);
		    p3 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.FIRST_CUBIC, p2a, p2b);
		    p2 = new Point2D.Double(coords[2], coords[3]);
		    p4 = mapper.apply(mapperIndex, p2,
				      Point3DMapper.Type.SECOND_CUBIC,
				      p2a, p2b);
		    p5 = mapper.apply(mapperIndex, p2b,
				      Point3DMapper.Type.KNOT);
		    curveTo(p3.getX(), p3.getY(), p3.getZ(),
			    p4.getX(), p4.getY(), p4.getZ(),
			    p5.getX(), p5.getY(), p5.getZ());
		    p2a = p2b;
		    break;
		case PathIterator.SEG_CLOSE:
		    lastx = startx;
		    lasty = starty;
		    closePath();
		    p2a = new Point2D.Double(startx, starty);
		    break;
		}
		pi.next();
	    }
	}


	Point3D createPoint(int cindex) {
	    return new Point3D.Float(coords[cindex], coords[cindex+1],
				      coords[cindex+2]);
	}

	void expandIfNeeded(int n) {
	    super.expandIfNeeded(n);
	    if ((cindex + n*3) < coords.length) {
		return;
	    }
	    int incr = cindex;
	    if (incr > MAX_INCR_3) incr = MAX_INCR_3;
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
	    Iterator1(Path3D.Float path) {
		super(path);
	        coords = path.coords;
	    }
	    
	    @Override
	    public int currentSegment(double[] fc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    for (int i = 0; i < len; i++) {
			fc[i] = (double) coords[cindex+i];
		    }
		}
		return type;
	    }

	    @Override
	    public int currentSegment(float[] dc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    System.arraycopy(coords, cindex, dc, 0, len);
		}
		return type;
	    }
	}
	
	static class Iterator2 extends Iterator {
	    float[] coords;
	    Transform3D transform;
	    Iterator2(Path3D.Float path, Transform3D transform) {
		super(path);
	        coords = path.coords;
		this.transform = transform;
	    }
	    
	    @Override
	    public int currentSegment(float[] fc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    transform.transform(coords, cindex, fc, 0, len/3);
		}
		return type;
	    }

	    @Override
	    public int currentSegment(double[] dc) {
		// int type = path.types[index];
		int type = pathTypes[index];
		int len = numbCoords(type);
		if (type != PathIterator3D.SEG_CLOSE) {
		    transform.transform(coords, cindex, dc, 0, len/3);
		}
		return type;
	    }
	}

	private double[] tmpCoords = new double[9];

	@Override
	public final synchronized void append(PathIterator3D pi,
					      boolean connect)
	{
	    if (pi.isDone()) return;
	    if (pi.currentSegment(tmpCoords)!=PathIterator3D.SEG_MOVETO) {
		String msg = errorMsg("missingMOVETO");
		throw new IllegalArgumentException(msg);
	    }
	    if (cindex == 0) {
		moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
	    } else {
		    if (connect) {
			if (coords[cindex-3] != tmpCoords[0]
			    || coords[cindex-2] != tmpCoords[1]
			    || coords[cindex-1] != tmpCoords[2]) {
			    lineTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
			}
		    } else {
			moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    }
	    }
	    pi.next();
	    while (!pi.isDone()) {
		switch (pi.currentSegment(tmpCoords)) {
		case PathIterator3D.SEG_MOVETO:
		    moveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    break;
		case PathIterator3D.SEG_LINETO:
		    lineTo(tmpCoords[0], tmpCoords[1], tmpCoords[2]);
		    break;
		case PathIterator3D.SEG_QUADTO:
		    quadTo(tmpCoords[0], tmpCoords[1], tmpCoords[2],
			   tmpCoords[3], tmpCoords[4], tmpCoords[5]);
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    curveTo(tmpCoords[0], tmpCoords[1], tmpCoords[2],
			    tmpCoords[3], tmpCoords[4], tmpCoords[5],
			    tmpCoords[6], tmpCoords[7], tmpCoords[8]);
		    break;
		case PathIterator3D.SEG_CLOSE:
		    closePath();
		    break;
		}
		pi.next();
	    }
	}

	@Override
	public Object clone() {
	    return super.clone();
	}

	public final Path3D createTransformedPath(Transform3D transform) {
	    Path3D.Float result = new Path3D.Float();
	    result.append(getPathIterator(transform), false);
	    return result;
	}

	@Override
	public final synchronized void curveTo(double x1, double y1, double z1,
					       double x2, double y2, double z2,
					       double x3, double y3, double z3)
	{
	    expandIfNeeded(3);
	    types[index++] = PathIterator3D.SEG_CUBICTO;
	    coords[cindex++] = (float)x1;
	    coords[cindex++] = (float)y1;
	    coords[cindex++] = (float)z1;
	    coords[cindex++] = (float)x2;
	    coords[cindex++] = (float)y2;
	    coords[cindex++] = (float)z2;
	    coords[cindex++] = (float)x3;
	    coords[cindex++] = (float)y3;
	    coords[cindex++] = (float)z3;
	}

	@Override
	public final synchronized
	    PathIterator3D getPathIterator(Transform3D tform)
	{
	    if (tform == null) {
		return new Iterator1(this);
	    } else {
		return new Iterator2(this, tform);
	    }
	}

	@Override
	public final synchronized void lineTo(double x, double y, double z) {
	    expandIfNeeded(1);
	    types[index++] = PathIterator3D.SEG_LINETO;
	    coords[cindex++] = (float)x;
	    coords[cindex++] = (float)y;
	    coords[cindex++] = (float)z;
	}

	@Override
	public synchronized void moveTo(double x, double y, double z) {
	    expandIfNeeded(1);
	    lastMoveTo = index;
	    types[index++] = PathIterator3D.SEG_MOVETO;
	    coords[cindex++] = (float)x;
	    coords[cindex++] = (float)y;
	    coords[cindex++] = (float)z;
	}

	@Override
	public final synchronized  void quadTo(double x1, double y1, double z1,
					 double x2, double y2, double z2)
	{
	    expandIfNeeded(2);
	    types[index++] = PathIterator3D.SEG_QUADTO;
	    coords[cindex++] = (float)x1;
	    coords[cindex++] = (float)y1;
	    coords[cindex++] = (float)z1;
	    coords[cindex++] = (float)x2;
	    coords[cindex++] = (float)y2;
	    coords[cindex++] = (float)z2;
	}

	@Override
	public final synchronized void transform(Transform3D tform) {
	    expandIfNeeded(1);
	    System.arraycopy(coords, 0, coords, 3, coords.length);
	    tform.transform(coords, 3, coords, 0, coords.length/3);
	}

	@Override
	public Point3D getStart() {
	    if (index == 0) return null;
	    return new Point3D.Float(coords[0], coords[1], coords[2]);
	}
	@Override
	public Point3D getEnd() {
	    if (index == 0 || types[index-1] == PathIterator3D.SEG_CLOSE) {
		return null;
	    }
	    return new  Point3D.Float(coords[cindex-3],
				       coords[cindex-2],
				       coords[cindex-1]);
	}

    }

    /**
     * Append the path segments specified by a path iterator.
     * @param pi the path iterator
     * @param connect true if an initial MOVETO segment obtained from
     *        the path iterator should be turned into a LINETO segment
     */
    public abstract void append(PathIterator3D pi, boolean connect);

    /**
     * Append the path segments specified by another path.
     * @param path the other path
     * @param connect true if an initial MOVETO segment obtained from
     *        the other path should be turned into a LINETO segment
     */
    public final void append(Path3D path, boolean connect) {
	PathIterator3D pi = path.getPathIterator(null);
	append(pi, connect);
    }

    /**
     * Get a bounding rectangle that encloses all of the points of
     * this path.
     * @return a bounding rectangular cuboid; null if the path is empty
     */
    public Rectangle3D getBounds() {
	PathIterator3D pi = getPathIterator(null);
	double[] coords = new double[9];
	if (pi.isDone()) return null;
	Rectangle3D bb = (this instanceof Path3D.Double)?
	    new Rectangle3D.Double(): new Rectangle3D.Float();
	if (pi.currentSegment(coords) != PathIterator3D.SEG_MOVETO) {
	    String msg = errorMsg("missingMOVETO");
	    throw new IllegalStateException(msg);
	}
	bb.setRect(coords[0], coords[1], coords[2], 0.0, 0.0, 0.0);
	pi.next();
	while(!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator3D.SEG_CUBICTO:
		bb.add(coords[6], coords[7], coords[8]);
	    case PathIterator3D.SEG_QUADTO:
		bb.add(coords[3], coords[4], coords[5]);
	    case PathIterator3D.SEG_LINETO:
	    case PathIterator3D.SEG_MOVETO:
		bb.add(coords[0], coords[1], coords[2]);
	    default:
		break;
	    }
	    pi.next();
	}
	return bb;
    }

    abstract Point3D createPoint(int cindex);

    /**
     * Get the current point.
     * The current point is the last point added by a path segment,
     * or the point provided by the last MOVETO operation if the
     * last segment was created by calling {#link #closePath()}.
     * @return the current point; null if the path is empty
     */
    public final synchronized Point3D getCurrentPoint() {
	if (cindex == 0) return null;
	return createPoint(cindex-3);
    }

    /**
     * Get a path iterator for this path.
     * The iterator will ignore new segments added after the call to this
     * method, but {@link #reset()} should not be called while
     * this path iterator is in use.
     * @return a path iterator for this path
     */
    public abstract PathIterator3D getPathIterator(Transform3D tform);

    /**
     * Reset the path by removing all of its segments.
     * This method should not be called if a path iterator is in use.
     */
    public final synchronized void reset() {
	index = 0;
	cindex = 0;
	lastMoveTo = -1;
    }

    /**
     * Close a path.
     * The new current point will be the point specified by the last
     * MOVETO operation.
     */
    public final synchronized void closePath() {
	if (index == 0) {
	    String msg = errorMsg("emptyPathOnClose");
	    throw new IllegalPathStateException(msg);
	}
	if(types[index-1] == PathIterator3D.SEG_CLOSE) {
	    return;
	}
	if (lastMoveTo == -1) {
	    String msg = errorMsg("missingMOVETO");
	    throw new IllegalPathStateException(msg);
	}
	expandIfNeeded(1);
	types[index] = PathIterator3D.SEG_CLOSE;
	copyCoords(lastMoveTo, cindex, 3);
	index++;
	cindex += 3;
    }

    /**
     * Create a new path by applying a transform to this path.
     * @param transform the transform
     * @return the new path
     */
    public abstract Path3D createTransformedPath(Transform3D transform);

    /**
     * Add a segment specified by a cubic B&eacute;zier curve.
     * @param x1 the X coordinate of the first control point
     * @param y1 the Y coordinate of the first control point
     * @param z1 the Z coordinate of the first control point
     * @param x2 the X coordinate of the second control point
     * @param y2 the Y coordinate of the second control point
     * @param z2 the Z coordinate of the second control point
     * @param x3 the X coordinate of the final point of the segment
     * @param y3 the Y coordinate of the final point of the segment
     * @param z3 the Z coordinate of the final point of the segment
     */
    public abstract void curveTo(double x1, double y1, double z1,
				 double x2, double y2, double z2,
				 double x3, double y3, double z3);

    /**
     * Add a straight-line segment starting from the current point.
     * @param x the X coordinate of the segment's end point
     * @param y the Y coordinate of the segment's end point
     * @param z the Z coordinate of the segment's end point
     */
    public abstract void lineTo(double x, double y, double z);

    /**
     * Move the current point to a new location or to an initial
     * location.
     * @param x the X coordinate of the new current point
     * @param y the Y coordinate of the new current point
     * @param z the Z coordinate of the new current point
     */
    public abstract void moveTo(double x, double y, double z);

    /**
     * Add a segment specified by a quadratic B&eacute;zier curve.
     * @param x1 the X coordinate of the control point
     * @param y1 the Y coordinate of the control point
     * @param z1 the Z coordinate of the control point
     * @param x2 the X coordinate of the end point of the segment
     * @param y2 the Y coordinate of the end point of the segment
     * @param z2 the Z coordinate of the end point of the segment
     */
    public abstract void quadTo(double x1, double y1, double z1,
				double x2, double y2, double z2);

    /**
     * Apply a transform to a path.
     * The transform will be applied to each segment's control points.
     * @param tform the transform
     */
    public abstract void transform(Transform3D tform);

	@Override
	public Object clone() {
	    try {
		return super.clone();
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

    /**
     * Get the starting point of a path.
     * @return the first point along the path; null if the path is
     *         empty
     */
    public abstract Point3D getStart();

    /**
     *
    /**
     * Get the last point on a path.
     * @return the first point along the path; null if the path is
     *         empty
     * @return the last point; null if the path is empty or closed
     *         at its end
     */
    public abstract Point3D getEnd();

    /**
     * Compute the array representing the control points for a cubic
     * B&eacute;zier curve that represents a straight segment line between two
     * points.
     * The first three values of the array returned include the
     * coordinates of p1, so those values should be ignored if
     * the results are used with the curveTo method of Path3D.
     * <P>
     * This method, or the variant whose arguments are X, Y, Z coordinate,
     * should be used when adding a cubic-triangle segment to a Surface3D
     * as the resulting control points will be in the right place for
     * tests used to determine if surface is well-formed.
     * <P>
     * The array will contain four control points, starting with p1 and
     * ending with p2.
     * @param p1 the point at the start of the line segment
     * @param p2 the pint at the end of the line segment
     * @return the control points, with each control point's X, Y, and Z
     *         coordinates adjacent in the array and in X, Y, Z order.
     */
    public static double[] setupCubic(Point3D p1, Point3D p2) {
	return setupCubic(p1.getX(), p1.getY(), p1.getZ(),
			  p2.getX(), p2.getY(), p2.getZ());
    }

    /**
     * Compute the array representing the control points for a cubic
     * B&eacute;zier curve that matches a quadratic B&eacute;zier curve.
     * The first three values of the array returned include the
     * coordinates of p1, so those values should be ignored if
     * the results are used with the curveTo method of Path3D.
     * <P>
     * This method, or the variant whose arguments are X, Y, Z coordinate,
     * should be used when adding a cubic-triangle segment to a Surface3D
     * as the resulting control points will be in the right place for
     * tests used to determine if surface is well-formed.
     * <P>
     * The array will contain four control points, starting with p1 and
     * ending with p3.
     * @param p1 the point at the start of the quadratic B&eacute;zier curve
     * @param p2 the control point of the quadratic B&eacute;zier curve
     *        between the start and end points
     * @param p3 the pint at the end of the the quadratic B&eacute;zier curve
     * @return the control points, with each control point's X, Y, and Z
     *         coordinates adjacent in the array and in X, Y, Z order.
     */
    public static double[] setupCubic(Point3D p1, Point3D p2, Point3D p3) {
	return setupCubic(p1.getX(), p1.getY(), p1.getZ(),
			  p2.getX(), p2.getY(), p2.getZ(),
			  p3.getX(), p3.getY(), p3.getZ());
    }

    /**
     * Compute the array representing the control points for a cubic
     * B&eacute;zier curve that represents a straight segment line between two
     * points represented by their X, Y, Z, coordinates.
     * The choice of control points results in the functions giving the
     * X, Y, and Z coordinates linear functions of the path parameter.
     * <P>
     * This method, or the variant whose arguments are instances of Point3D,
     * should be used when adding a cubic-triangle segment to a Surface3D
     * as the resulting control points will be in the right place for
     * tests used to determine if surface is well-formed.
     * <P>
     * The array will contain four control points, starting with
     * (x0, y, z0)  and ending with (x3, y3, z3).
     * @param x0 the X coordinate of the point at the start of the line segment
     * @param y0 the Y coordinate of the point at the start of the line segment
     * @param z0 the Z coordinate of the point at the start of the line segment
     * @param x3 the X coordinate of the point at the end of the line segment
     * @param y3 the Y coordinate of the point at the end of the line segment
     * @param z3 the Z coordinate of the point at the end of the line segment
     * @return the control points, with each control point's X, Y, and Z
     *         coordinates adjacent in the array and in X, Y, Z order.
     */
    public static double[] setupCubic(double x0, double y0, double z0,
				      double x3, double y3, double z3)
    {
	double x1 = x0*2.0/3.0 + x3/3.0;
	double y1 = y0*2.0/3.0 + y3/3.0;
	double z1 = z0*2.0/3.0 + z3/3.0;
	double x2 = x0/3.0 + x3*2.0/3.0;
	double y2 = y0/3.0 + y3*2.0/3.0;
	double z2 = z0/3.0 + z3*2.0/3.0;

	double coords[] = {x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3};
	return coords;
    }

    /**
     * Compute the array representing the control points for a cubic
     * B&eacute;zier curve that matches a quadratic B&eacute;zier curve.
     * The choice of control points results in the functions giving the
     * X, Y, and Z coordinates linear functions of the path parameter.
     * <P>
     * This method, or the variant whose arguments are instances of Point3D,
     * should be used when adding a cubic-triangle segment to a Surface3D
     * as the resulting control points will be in the right place for
     * tests used to determine if surface is well-formed.
     * <P>
     * The array will contain four control points, starting with
     * (x0, y0, z0) and ending with (x3, y3, z3).
     * @param x0 the X coordinate of the point at the start of the line segment
     * @param y0 the Y coordinate of the point at the start of the line segment
     * @param z0 the Z coordinate of the point at the start of the line segment
     * @param xc the X coordinate of the intermediate control point
     * @param yc the Y coordinate of the intermediate control point
     * @param zc the Z coordinate of the intermediate control point
     * @param x3 the X coordinate of the point at the end of the line segment
     * @param y3 the Y coordinate of the point at the end of the line segment
     * @param z3 the Z coordinate of the point at the end of the line segment
     * @return the control points, with each control point's X, Y, and Z
     *         coordinates adjacent in the array and in X, Y, Z order.
     */
    public static double[] setupCubic(double x0, double y0, double z0,
				      double xc, double yc, double zc,
				      double x3, double y3, double z3)
    {
	double x1 = x0/3.0 + xc*(2.0/3.0);
	double y1 = y0/3.0 + yc*(2.0/3.0);
	double z1 = z0/3.0 + zc*(2.0/3.0);

	double x2 = xc*(2.0/3.0) + x3/3.0;
	double y2 = yc*(2.0/3.0) + y3/3.0;
	double z2 = zc*(2.0/3.0) + z3/3.0;

	double coords[] = {x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3};
	return coords;
    }

}

//  LocalWords:  exbundle API lastMoveTo initialCapacity MOVETO zier
//  LocalWords:  missingMOVETO LINETO closePath emptyPathOnClose xc
//  LocalWords:  eacute tform curveTo subclasses yc zc DMapper
//  LocalWords:  BezierGrid boolean mapperIndex
