package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.VectorOps;
import java.awt.geom.*;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Flattening path iterator class for two dimensions.
 * This iterator adds segments to the segments returned by
 * another path iterator by splitting quadratic and cubic  B&eacute;zier-curve
 * segments until the points along a segment deviate from a straight line
 * by no more than the value provided by the flatness parameter.  If the
 * flatness parameter is 0.0, the split will continue until stopped by
 * the recursion limit. In addition, a flatness parameter of 0.0 implies
 * that all segments will be split including ones that are straight
 * lines. For nonzero values of the flatness parameter, straight-line
 * segments are not split unless there is a transform that is not known
 * to be an affine transform. In general, when there is transform that
 * is not an affine transform (e.g., an instance of the class
 * {@link java.awt.geom.AffineTransform}), All the control points, including
 * the end points, of a segment must be separated from each other by
 * no more than the flatness parameter.
 * <P>
 * The iterator has a recursion limit (the default value is 10) to
 * prevent the number of segments from becoming excessively large.
 * Incrementing the recursion limit by 1 can double the number of
 * segments from that returned by the previous limit.
 * <P>
 * A transform can be provided in the constructors
 * {@link #FlatteningPathIterator2D(PathIterator,Transform2D,double)},
 * {@link #FlatteningPathIterator2D(PathIterator,Transform2D,double,int)}.
 * {@link #FlatteningPathIterator2D(PathIterator,AffineTransform,double)},
 * and
 * {@link #FlatteningPathIterator2D(PathIterator,AffineTransform,double,int)}.
 * The transform will be applied after the path is flattened, and
 * will be applied only to the control points. Because of this, the
 * path should be flattened sufficiently so that the transform can
 * be approximated by an affine transform over the convex hull for
 * the flattened path's control points. While the transform is applied
 * after the path is flattened, the flatness test uses transformed
 * values of the control points.
 * <P>
 * This class does not modify segment types, unlike
 * {@link java.awt.geom.FlatteningPathIterator} which turns quadratic
 * and cubic segments into straight lines. Not modifying segment types
 * is useful when applying a transform that is not an affine transform.
 */
public class FlatteningPathIterator2D implements PathIterator {
    PathIterator src;
    Transform2D transform;
    AffineTransform atransform;
    double flatness;
    int limit = 10;

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static class SegmentPathIterator implements PathIterator {
	double dx0;
	double dy0;
	float fx0;
	float fy0;
	double[] dcoords;
	float[] fcoords;
	int type;
	int windingRule = PathIterator.WIND_NON_ZERO;
	int index = 0;

	// if type is MOVE_TO, x0, y0 taken from coords
	SegmentPathIterator(int type, double x0, double y0, double[] coords) {
	    dx0 = x0;
	    dy0 = y0;
	    this.type = type;
	    dcoords = new double[6];
	    int len = 0;
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		dx0 = coords[0];
		dy0 = coords[1];
		index = 1;
	    case PathIterator.SEG_LINETO:
		len = 2;
		break;
	    case PathIterator.SEG_QUADTO:
		len = 4;
		break;
	    case PathIterator.SEG_CUBICTO:
		len = 6;
		break;
	    }   
	    if (len > 0) {
		System.arraycopy(coords, 0, dcoords, 0, len);
	    }
	}

	// if type is MOVE_TO, x0, y0 taken from coords
	SegmentPathIterator(int type, float x0, float y0, float[] coords) {
	    fx0 = x0;
	    fy0 = y0;
	    this.type = type;
	    fcoords = new float[6];
	    int len = 0;
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		index = 1;
		fx0 = coords[0];
		fy0 = coords[1];
	    case PathIterator.SEG_LINETO:
		len = 2;
		break;
	    case PathIterator.SEG_QUADTO:
		len = 4;
		break;
	    case PathIterator.SEG_CUBICTO:
		len = 6;
		break;
	    }   
	    if (len > 0) {
		System.arraycopy(coords, 0, fcoords, 0, len);
	    }
	}


	@Override
	public int getWindingRule() {return windingRule;}
	
	@Override
	public int currentSegment(double[] coords) {
	    if (index == 0) {
		if (fcoords == null) {
		    coords[0] = dx0;
		    coords[1] = dy0;
		} else {
		    coords[0] = fx0;
		    coords[1] = fy0;
		}
		return PathIterator.SEG_MOVETO;
	    } else {
		int len = 0;
		switch(type) {
		case PathIterator.SEG_MOVETO:
		case PathIterator.SEG_LINETO:
		    len = 2;
		    break;
		case PathIterator.SEG_QUADTO:
		    len = 4;
		    break;
		case PathIterator.SEG_CUBICTO:
		    len = 6;
		    break;
		}   
		if (fcoords == null) {
		    System.arraycopy(dcoords, 0, coords, 0, len);
		} else {
		    for (int i = 0; i < len; i++) {
			coords[i] = (double)fcoords[i];
		    }
		}
		return type;
	    }
	}

	@Override
	public int currentSegment(float[] coords) {
	    if (index == 0) {
		if (dcoords == null) {
		    coords[0] = fx0;
		    coords[1] = fy0;
		} else {
		    coords[0] = (float)dx0;
		    coords[1] = (float)dy0;
		}
		return PathIterator.SEG_MOVETO;
	    } else {
		int len = 0;
		switch(type) {
		case PathIterator.SEG_MOVETO:
		case PathIterator.SEG_LINETO:
		    len = 2;
		    break;
		case PathIterator.SEG_QUADTO:
		    len = 4;
		    break;
		case PathIterator.SEG_CUBICTO:
		    len = 6;
		    break;
		}   
		if (dcoords == null) {
		    System.arraycopy(fcoords, 0, coords, 0, len);
		} else {
		    for (int i = 0; i < len; i++) {
			coords[i] = (float)dcoords[i];
		    }
		}
		return type;
	    }
	}
	
	@Override
	public boolean isDone() { return index > 1;}

	@Override
	public void next() {
	    if (index < 2)
		index++;
	}
    }

    /**
     * Constructor.
     * The default recursion limit is 10.
     * @param src a path iterator to flatten
     * @param flatness the flatness constraint
     */
    public FlatteningPathIterator2D(PathIterator src, double flatness)
	throws IllegalArgumentException
    {
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	this.flatness = flatness;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor given a general transform.
     * The transform is not constrained to be an affine transform.
     * The default recursion limit is 10.
     * @param src a path iterator to flatten
     * @param transform the transform to apply
     * @param flatness the flatness constraint
     */
    public FlatteningPathIterator2D(PathIterator src, Transform2D transform,
				    double flatness)
	throws IllegalArgumentException
    {
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	this.transform = transform;
	this.flatness = flatness;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];

    }

    /**
     * Constructor given an affine transform.
     * The transform is not constrained to be an affine transform.
     * The default recursion limit is 10.
     * @param src a path iterator to flatten
     * @param transform the transform to apply
     * @param flatness the flatness constraint
     */
    public FlatteningPathIterator2D(PathIterator src, AffineTransform transform,
				    double flatness)
	throws IllegalArgumentException
    {
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	atransform = transform;
	this.flatness = flatness;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor with a recursion limit.
     * Both the flatness and the limit must be non-negative.
     * @param src a path iterator to flatten
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(PathIterator src, double flatness,
				    int limit)
	throws IllegalArgumentException
    {
	if (limit < 0) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	this.flatness = flatness;
	this.limit = limit;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and a double-precision coordinate array.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * If the segment type is PathIterator.SEG_MOVETO, the arguments
     * x0 and y0 are ignored.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, double x0, double y0,
				    double[] coords,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords), flatness, limit);
    }

    /**
     * Constructor given a type and a single-precision coordinate array.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, float x0, float y0,
				    float[] coords,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords), flatness, limit);
    }


    /**
     * Constructor with a general transform and a recursion limit.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param src a path iterator to flatten
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(PathIterator src, Transform2D transform,
				    double flatness, int limit)
	throws IllegalArgumentException
    {
	if (limit < 0) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	if (transform != null) {
	    if (transform instanceof AffineTransform) {
		atransform = (AffineTransform) transform;
	    } else {
		this.transform = transform;
	    }
	}
	this.flatness = flatness;
	this.limit = limit;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type, a double-precision coordinate array, and
     * a transform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, double x0, double y0,
				    double[] coords,
				    Transform2D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, flatness, limit);
    }

    /**
     * Constructor given a type, single-precision coordinate array and
     * a transform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, float x0, float y0,
				    float[] coords,
				    Transform2D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, flatness, limit);
    }


    /**
     * Constructor given an affine transform and a recursion limit.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param src a path iterator to flatten
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(PathIterator src, AffineTransform transform,
				    double flatness, int limit)
	throws IllegalArgumentException
    {
	if (limit < 0) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	atransform = transform;
	this.flatness = flatness;
	this.limit = limit;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and double-precision coordinate array,
     * with a transform that is an AffineTransform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, double x0, double y0,
				    double[] coords,
				    AffineTransform transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, flatness, limit);
    }

    /**
     * Constructor give a type and single-precision coordinate array,
     * with a transform that is an AffineTransform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator2D(int type, float x0, float y0,
				    float[] coords,
				    AffineTransform transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, flatness, limit);
    }


    /**
     * Get the flatness parameter.
     */
    public double getFlatness() {return flatness;}

    /**
     * Get the recursion limit.
     * @return the recursion limit
     */
    public int getRecursionLimit() {return limit;}

    boolean usingRecursion = false;
    int mode = -1;
    double[] workspace = null;

    double[][][] tmp = new double[3][3][2];
    double ftmp[] = new double[8];

    int depth = 0;
    int[] splitCount = null;

    double[] lastMoveTo  = new double[2];

    double[] v1 = new double[2];
    double[] v2 = new double[2];
    double[] v3 = new double[2];

    private boolean mustFlatten() {
	if (splitCount[depth] >= limit) return false;
	if (flatness == 0.0) return true;
	double dist;
	boolean direct;
	int offset;
	switch(mode) {
	case PathIterator.SEG_LINETO:
	case PathIterator.SEG_CLOSE:
	    if (transform != null) {
		offset = depth*4;
		v1[0] = (workspace[offset+2]+workspace[offset+0])/2.0;
		v1[1] = (workspace[offset+3]+workspace[offset+1])/2.0;
		transform.transform(v1, 0, v2, 0, 1);
		transform.transform(workspace, offset, v1, 0, 1);
		transform.transform(workspace, offset+2, v3, 0, 1);
		VectorOps.add(v1,v1,v3);
		VectorOps.multiply(v1, -0.5, v1);
		VectorOps.add(v3,v1,v2);
		if (VectorOps.norm(v3) > flatness) {
		    return true;
		}
	    }
	    return false;
	case PathIterator.SEG_QUADTO:
	    offset = depth*6;
	    if (transform == null && atransform == null) {
		v1[0] = workspace[offset+4]-workspace[offset+0];
		v1[1] = workspace[offset+5]-workspace[offset+1];
		v2[0] = workspace[offset+2]-workspace[offset+0];
		v2[1] = workspace[offset+3]-workspace[offset+1];
	    } else {
		if (transform != null) {
		    transform.transform(workspace,offset,ftmp,0,3);
		} else {
		    atransform.transform(workspace,offset,ftmp,0,3);
		}
		v1[0] = ftmp[4]-ftmp[0];
		v1[1] = ftmp[5]-ftmp[1];
		v2[0] = ftmp[2]-ftmp[0];
		v2[1] = ftmp[3]-ftmp[1];
	    }
	    // direct = (VectorOps.norm(v1) < 1.e-15);
	    direct = (VectorOps.dotProduct(v1,v2) <= 0.0)
		|| (VectorOps.norm(v1) < VectorOps.norm(v2));
	    if (direct) {
		// dist = VectorOps.norm(v2);
		// split so we have multiple lines
		return true;
	    } else {
		VectorOps.normalize(v1);
		// VectorOps.crossProduct(v3,v1,v2);
		//dist = VectorOps.norm(v3);
		dist = Math.sqrt(Math.abs(v1[0]*v2[1] - v1[1]*v2[0]));
	    }
	    if (dist > flatness) {
		return true;
	    } else {
		if (transform != null) {
		    v1[0] = (workspace[offset+4]+workspace[offset+0])/2.0;
		    v1[1] = (workspace[offset+5]+workspace[offset+1])/2.0;
		    transform.transform(v1, 0, v2, 0, 1);
		    transform.transform(workspace, offset, v1, 0, 1);
		    transform.transform(workspace, offset+4, v3, 0, 1);
		    VectorOps.add(v1,v1,v3);
		    VectorOps.multiply(v1, -0.5, v1);
		    VectorOps.add(v3,v1,v2);
		    if (VectorOps.norm(v3) > flatness) {
			return true;
		    }
		}
		return false;
	    }
	case PathIterator.SEG_CUBICTO:
	    offset = depth*8;
	    if (transform == null && atransform == null) {
		v1[0] = workspace[offset+6]-workspace[offset+0];
		v1[1] = workspace[offset+7]-workspace[offset+1];
		v2[0] = workspace[offset+2]-workspace[offset+0];
		v2[1] = workspace[offset+3]-workspace[offset+1];
	    } else {
		if (transform != null) {
		    transform.transform(workspace, offset, ftmp, 0, 4);
		} else {
		    atransform.transform(workspace, offset, ftmp, 0, 4);
		}
		v1[0] = ftmp[6]-ftmp[0];
		v1[1] = ftmp[7]-ftmp[1];
		v2[0] = ftmp[2]-ftmp[0];
		v2[1] = ftmp[3]-ftmp[1];
	    }
	    // direct = (VectorOps.norm(v1) < 1.e-15);
	    direct = (VectorOps.dotProduct(v1,v2) <= 0.0)
		|| (VectorOps.norm(v1) < VectorOps.norm(v2));
	    if (direct) {
		// dist = VectorOps.norm(v2);
		// split so each flattened segment is a line, not a loop.
		return true;
	    } else {
		VectorOps.normalize(v1);
		// VectorOps.crossProduct(v3,v1,v2);
		// dist = VectorOps.norm(v3);
		dist = Math.sqrt(Math.abs(v1[0]*v2[1]-v1[1]*v2[0]));
	    }
	    if (dist > flatness) {
		return true;
	    }
	    if (transform == null && atransform == null) {
		v2[0] = workspace[offset+4]-workspace[offset+0];
		v2[1] = workspace[offset+5]-workspace[offset+1];
	    } else {
		v2[0] = ftmp[4]-ftmp[0];
		v2[1] = ftmp[5]-ftmp[1];
	    }
	    direct = (VectorOps.dotProduct(v1,v2) <= 0.0)
		|| (VectorOps.norm(v1) < VectorOps.norm(v2));
	    if (direct) {
		// dist = VectorOps.norm(v2);
		// split so each flattened segment is a line, not a loop.
		return true;
	    } else {
		VectorOps.normalize(v1);
		// VectorOps.crossProduct(v3,v1,v2);
		// dist = VectorOps.norm(v3);
		dist = Math.sqrt(Math.abs(v1[0]*v2[1]-v1[1]*v2[0]));
	    }

	    if (dist > flatness) {
		return true;
	    }
	    if (transform == null && atransform == null) {
		v2[0] = workspace[offset+4] - workspace[offset+2];
		v2[1] = workspace[offset+5] - workspace[offset+3];
	    } else {
		v2[0] = ftmp[4] - ftmp[2];
		v2[1] = ftmp[5] - ftmp[3];
	    }
	    VectorOps.normalize(v2);
	    if (VectorOps.dotProduct(v1, v2) < 0.5) {
		return true;
	    }
	    
	    if (transform != null) {
		v1[0] = (workspace[offset+6]+workspace[offset+0])/2.0;
		v1[1] = (workspace[offset+7]+workspace[offset+1])/2.0;
		transform.transform(v1, 0, v2, 0, 1);
		transform.transform(workspace, offset, v1, 0, 1);
		transform.transform(workspace, offset+6, v3, 0, 1);
		VectorOps.add(v1,v1,v3);
		VectorOps.multiply(v1, -0.5, v1);
		VectorOps.add(v3,v1,v2);
		if (VectorOps.norm(v3) > flatness) {
		    return true;
		}
	    }
	    return false;
	default:
	    throw new UnexpectedExceptionError();
	}
    }
    
    private void split() {
	// split a cubic or quadratic Bezier curve in half.
	switch(mode) {
	case PathIterator.SEG_CLOSE:
	case PathIterator.SEG_LINETO:
	    int depth4 = 4*depth;
	    int ndepth4 = depth4 + 4;
	    for (int j = 0; j < 2; j++) {
		workspace[ndepth4+j] = workspace[depth4+j];
		workspace[ndepth4+2+j] =
		    (workspace[depth4+j] + workspace[depth4+2+j])/2.0;
		workspace[depth4+j] = workspace[ndepth4+2+j];
	    }
	    break;
	case PathIterator.SEG_QUADTO:
	    int depth6 = 6*depth;
	    for (int i = 1; i < 3; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 2; j++) {
		    int k = 2*i+j;
		    tmp[0][im1][j] = (workspace[depth6 + k-2] + 
				      workspace[depth6 + k])/2.0;
		}
	    }
	    for (int j = 0; j < 2; j++) {
		tmp[1][0][j] = (tmp[0][0][j] + tmp[0][1][j])/2.0;
	    }
	    int ndepth6 = depth6 + 6;
	    for (int j = 0; j < 2; j++) {
		workspace[ndepth6+j] = workspace[depth6+j];
		workspace[ndepth6+2+j] = tmp[0][0][j];
		workspace[ndepth6+4+j] = tmp[1][0][j];
		workspace[depth6+j] = tmp[1][0][j];
		workspace[depth6+2+j] = tmp[0][1][j];
	    }
	    break;
	case PathIterator.SEG_CUBICTO:
	    int depth8 = 8*depth;
	    for (int i = 1; i < 4; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 2; j++) {
		    int k = 2*i+j;
		    tmp[0][im1][j] = (workspace[depth8 + k-2] + 
				      workspace[depth8 + k])/2.0;
		}
	    }
	    for (int i = 1; i < 3; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 2; j++) {
		    int k = 2*i+j;
		    tmp[1][im1][j] = (tmp[0][im1][j] + tmp[0][i][j])/2.0;
		}
	    }
	    for (int j = 0; j < 2; j++) {
		tmp[2][0][j] = (tmp[1][0][j] + tmp[1][1][j])/2.0;
	    }
	    int ndepth8 = depth8 + 8;
	    for (int j = 0; j < 2; j++) {
		workspace[ndepth8+j] = workspace[depth8+j];
		workspace[ndepth8+2+j] = tmp[0][0][j];
		workspace[ndepth8+4+j] = tmp[1][0][j];
		workspace[ndepth8+6+j] = tmp[2][0][j];
		workspace[depth8+j] = tmp[2][0][j];
		workspace[depth8+2+j] = tmp[1][1][j];
		workspace[depth8+4+j] = tmp[0][2][j];
	    }
	    break;
	}
	int sc = ++splitCount[depth];
	depth++;
	splitCount[depth] = sc;
    }

    private void partition(boolean upwards) {
	if (upwards && (splitCount[depth] >= limit)){
	    // If we reached the limit, both halves of the split
	    // should be treated the same.
	    return;
	}
	while (mustFlatten()) {
	    split();
	}
    }

    @Override
    public int currentSegment(double[] coords) {
	int len;
	switch(mode) {
	case PathIterator.SEG_CLOSE:
	    len = (depth == 0)? 0: 2;
	    break;
	case PathIterator.SEG_MOVETO:
	    len = 2;
	    break;
	case PathIterator.SEG_LINETO:
	    len = 2;
	    break;
	case PathIterator.SEG_QUADTO:
	    len = 4;
	    break;
	case PathIterator.SEG_CUBICTO:
	    len = 6;
	    break;
	case -1:
	    // Works because the first segment in a path must be
	    // SEG_MOVETO, so we don't have to split anything.
	    if (transform != null || atransform != null) {
		int smode = src.currentSegment(workspace);
		if (transform != null) {
		    transform.transform(workspace, 0, coords, 0, 1);
		} else {
		    atransform.transform(workspace, 0, coords, 0, 1);
		}
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (len > 0) {
	    int offset = depth*(len+2) + 2;
	    if (transform != null || atransform != null) {
		if (transform != null) {
		    transform.transform(workspace, offset, coords, 0, len/2);
		} else {
		    atransform.transform(workspace, offset, coords, 0, len/2);
		}
	    } else {
		System.arraycopy(workspace, offset, coords, 0, len);
	    }
	    if (mode == PathIterator3D.SEG_CLOSE) {
		return PathIterator3D.SEG_LINETO;
	    }
	}
	return mode;
    }

    @Override
    public int currentSegment(float[] coords) {
	int len;
	switch(mode) {
	case PathIterator.SEG_CLOSE:
	    len = (depth == 0)?0: 2;
	    break;
	case PathIterator.SEG_MOVETO:
	    len = 2;
	    break;
	case PathIterator.SEG_LINETO:
	    len = 2;
	    break;
	case PathIterator.SEG_QUADTO:
	    len = 4;
	    break;
	case PathIterator.SEG_CUBICTO:
	    len = 6;
	    break;
	case -1:
	    // Works because the first segment in a path must be
	    // SEG_MOVETO, so we don't have to split anything.
	    if (transform != null || atransform != null) {
		int smode = src.currentSegment(workspace);
		if (transform != null) {
		    transform.transform(workspace, 0, coords, 0, 1);
		} else {
		    atransform.transform(workspace, 0, coords, 0, 1);
		}
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (transform != null || atransform != null) {
	    currentSegment(ftmp);
	    for (int i = 0; i < len; i++) {
		coords[i] = (float) ftmp[i];
	    }
	} else {
	    for (int i = 0; i < len; i++) {
		int offset = depth*(len+2) + 2;
		coords[i] = (float)(workspace[offset+i]);
	    }
	}
	if (len > 0 && mode == PathIterator3D.SEG_CLOSE) {
	    return PathIterator3D.SEG_LINETO;
	} else {
	    return mode;
	}
    }

    @Override
    public boolean isDone() {
	if (depth > 0) return false;
	return src.isDone();
    }

    double[] ourcoords = new double[12];

    @Override
    public void next() {
	if (depth == 0) {
	    if (mode == -1) {
		mode = src.currentSegment(workspace);
		if (mode == PathIterator.SEG_MOVETO) {
		    System.arraycopy(workspace, 0, lastMoveTo, 0, 2);
		}
	    } else {
		switch (mode) {
		case PathIterator.SEG_CLOSE:
		case PathIterator.SEG_MOVETO:
		case PathIterator.SEG_LINETO:
		    System.arraycopy(workspace, 2, workspace, 0, 2);
		    break;
		case PathIterator.SEG_QUADTO:
		    System.arraycopy(workspace, 4, workspace, 0, 2);
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.arraycopy(workspace, 6, workspace, 0, 2);
		    break;
		default:
		    break;
		}
	    }
	    src.next();
	    if (src.isDone()) {
		// proactively set mode, etc., to a safe value, as
		// Java JDK path iterators do that.
		mode = PathIterator.SEG_MOVETO;
		workspace[2] = 0.0; workspace[3] = 0.0;
		return;
	    }
	    splitCount[0] = 0;
	    mode = src.currentSegment(ourcoords);
	    switch (mode) {
	    case PathIterator.SEG_CLOSE:
		System.arraycopy(lastMoveTo, 0, workspace, 2, 2);
		if (((float)lastMoveTo[0] != (float)workspace[0])
		    || ((float)lastMoveTo[1] != (float)workspace[1])) {
		    if (flatness == 0.0 || transform != null) {
			partition(false);
		    }
		}
		break;
	    case PathIterator.SEG_MOVETO:
		System.arraycopy(ourcoords, 0, lastMoveTo, 0, 2);
		System.arraycopy(ourcoords, 0, workspace, 2, 2);
		break;
	    case PathIterator.SEG_LINETO:
		System.arraycopy(ourcoords, 0, workspace, 2, 2);
		if (flatness == 0.0 || transform != null) {
		    partition(false);
		}
		break;
	    case PathIterator.SEG_QUADTO:
		System.arraycopy(ourcoords, 0, workspace, 2, 4);
		partition(false);
		break;
	    case PathIterator.SEG_CUBICTO:
		System.arraycopy(ourcoords, 0, workspace, 2, 6);
		partition(false);
	    default:
		break;
	    }
	} else {
	    depth--;
	    partition(true);
	}
    }

    @Override
    public int getWindingRule() {
	return src.getWindingRule();
    }

}
//  LocalWords:  eacute zier FlatteningPathIterator PathIterator src
//  LocalWords:  Bezier
