package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.VectorOps;
import java.util.Arrays;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Flattening path iterator class for three dimensions.
 * This iterator adds segments to the segments returned by
 * another path iterator by splitting quadratic and cubic  B&eacute;zier-curve
 * segments until the points along a segment deviate from a straight line
 * by no more than the value provided by the flatness parameter.  If the
 * flatness parameter is 0.0, the split will continue until stopped by
 * the recursion limit. In addition, a flatness parameter of 0.0 implies
 * that all segments will be split including ones that are straight
 * lines. For nonzero values of the flatness parameter, straight-line
 * segments are not split unless there is a transform that is not known
 * to be an affine transform.
 * <P>
 * The iterator has a recursion limit (the default value is 10) to
 * prevent the number of segments from becoming excessively large.
 * Incrementing the recursion limit by 1 can double the number of
 * segments from that returned by the previous limit.
 * <P>
 * A transform can be provided in the constructors
 * {@link #FlatteningPathIterator3D(PathIterator3D,Transform3D,double)}
 * and
 * {@link #FlatteningPathIterator3D(PathIterator3D,Transform3D,double,int)}.
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
 * and cubic segments into straight lines.  Not modifying segment types
 * is useful when applying a transform that is not an affine transform.
 */
public class FlatteningPathIterator3D implements PathIterator3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    PathIterator3D src;
    Transform3D transform;
    double flatness;
    int limit = 10;
    boolean nonAffine = false;

    static class SegmentPathIterator implements PathIterator3D {
	double dx0;
	double dy0;
	double dz0;
	float fx0;
	float fy0;
	float fz0;
	double[] dcoords;
	float[] fcoords;
	int type;
	int index = 0;

	// if type is MOVE_TO, x0, y0, and z0 are taken from coords
	SegmentPathIterator(int type, double x0, double y0, double z0,
			    double[] coords)
	{
	    dx0 = x0;
	    dy0 = y0;
	    dz0 = z0;
	    this.type = type;
	    dcoords = new double[9];
	    int len = 0;
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
		dx0 = coords[0];
		dy0 = coords[1];
		dz0 = coords[2];
		index = 1;
	    case PathIterator3D.SEG_LINETO:
		len = 3;
		break;
	    case PathIterator3D.SEG_QUADTO:
		len = 6;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		len = 9;
		break;
	    }
	    if (len > 0) {
		System.arraycopy(coords, 0, dcoords, 0, len);
	    }
	}

	// if type is MOVE_TO, x0, y0, and z0  are taken from coords
	SegmentPathIterator(int type, float x0, float y0, float z0,
			    float[] coords)
	{
	    fx0 = x0;
	    fy0 = y0;
	    fz0 = z0;
	    this.type = type;
	    fcoords = new float[6];
	    int len = 0;
	    switch(type) {
	    case PathIterator3D.SEG_MOVETO:
		index = 1;
		fx0 = coords[0];
		fy0 = coords[1];
		fz0 = coords[2];
	    case PathIterator3D.SEG_LINETO:
		len = 3;
		break;
	    case PathIterator3D.SEG_QUADTO:
		len = 6;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		len = 9;
		break;
	    }
	    if (len > 0) {
		System.arraycopy(coords, 0, fcoords, 0, len);
	    }
	}

	@Override
	public int currentSegment(double[] coords) {
	    if (index == 0) {
		if (fcoords == null) {
		    coords[0] = dx0;
		    coords[1] = dy0;
		    coords[2] = dz0;
		} else {
		    coords[0] = fx0;
		    coords[1] = fy0;
		    coords[2] = fz0;
		}
		return PathIterator3D.SEG_MOVETO;
	    } else {
		int len = 0;
		switch(type) {
		case PathIterator3D.SEG_MOVETO:
		case PathIterator3D.SEG_LINETO:
		    len = 3;
		    break;
		case PathIterator3D.SEG_QUADTO:
		    len = 6;
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    len = 9;
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
		    coords[2] = fz0;
		} else {
		    coords[0] = (float)dx0;
		    coords[1] = (float)dy0;
		    coords[2] = (float)dz0;
		}
		return PathIterator3D.SEG_MOVETO;
	    } else {
		int len = 0;
		switch(type) {
		case PathIterator3D.SEG_MOVETO:
		case PathIterator3D.SEG_LINETO:
		    len = 3;
		    break;
		case PathIterator3D.SEG_QUADTO:
		    len = 6;
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    len = 9;
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
    public FlatteningPathIterator3D(PathIterator3D src, double flatness)
	throws IllegalArgumentException
    {
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	this.flatness = flatness;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor given a transform.
     * The transform is not constrained to be an affine transform.
     * The default recursion limit is 10.
     * @param src a path iterator to flatten
     * @param transform the transform to apply
     * @param flatness the flatness constraint
     */
    public FlatteningPathIterator3D(PathIterator3D src, Transform3D transform,
				    double flatness)
	throws IllegalArgumentException
    {
	if (flatness < 0.0) {
	    throw new IllegalArgumentException(errorMsg("negativeFlatness"));
	}
	this.src = src;
	this.transform = transform;
	if (!(transform instanceof AffineTransform3D)) {
	    nonAffine = true;
	}
	this.flatness = flatness;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor with a recursion limit.
     * Both the flatness and the limit must be non-negative.
     * 
     * @param src a path iterator to flatten
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(PathIterator3D src, double flatness,
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
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor with a transform and a recursion limit.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param src a path iterator to flatten
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(PathIterator3D src, Transform3D transform,
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
	this.transform = transform;
	if (!(transform instanceof AffineTransform3D)) {
	    nonAffine = true;
	}
	this.flatness = flatness;
	this.limit = limit;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and a double-precision coordinate array.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * If the segment type is PathIterator3D.SEG_MOVETO, the arguments
     * x0 and y0 are ignored.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, double x0, double y0, double z0,
				    double[] coords,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     flatness, limit);
    }

    /**
     * Constructor given a type and a single-precision coordinate array.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO},
     *        {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, float x0, float y0, float z0,
				    float[] coords,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     flatness, limit);
    }

    /**
     * Constructor give a type, a double-precision coordinate array, and
     * a transform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, double x0, double y0, double z0,
				    double[] coords,
				    Transform3D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, flatness, limit);
    }

    /**
     * Constructor given a type, single-precision coordinate array and
     * a transform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, float x0, float y0, float z0,
				    float[] coords,
				    Transform3D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, flatness, limit);
    }

    /**
     * Constructor give a type and double-precision coordinate array,
     * with a transform that is an AffineTransform.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, double x0, double y0, double z0,
				    double[] coords,
				    AffineTransform3D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, flatness, limit);
    }

    /**
     * Constructor give a type and single-precision coordinate array,
     * with a transform that is an AffineTransform3D.
     * Both the flatness and the limit must be non-negative.
     * The transform is not constrained to be an affine transform.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param flatness the flatness constraint
     * @param limit the recursion limit
     */
    public FlatteningPathIterator3D(int type, float x0, float y0, float z0,
				    float[] coords,
				    AffineTransform3D transform,
				    double flatness, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, flatness, limit);
    }

    /**
     * Get the flatness parameter.
     * @return the flatness parameter
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
    double[][][] tmp = new double[3][3][3];
    double ftmp[] = new double[12];

    int depth = 0;
    int[] splitCount = null;

    double[] lastMoveTo  = new double[3];

    double[] v1 = new double[3];
    double[] v2 = new double[3];
    double[] v3 = new double[3];

    private boolean mustFlatten() {
	if (splitCount[depth] >= limit) return false;
	if (flatness == 0.0) return true;
	double dist;
	boolean direct;
	int offset;
	switch(mode) {
	case PathIterator3D.SEG_LINETO:
	case PathIterator3D.SEG_CLOSE:
	    if (nonAffine) {
		offset = depth*6;
		v1[0] = (workspace[offset+3]+workspace[offset+0])/2.0;
		v1[1] = (workspace[offset+4]+workspace[offset+1])/2.0;
		v1[2] = (workspace[offset+5]+workspace[offset+2])/2.0;
		transform.transform(v1, 0, v2, 0, 1);
		transform.transform(workspace, offset, v1, 0, 1);
		transform.transform(workspace, offset+3, v3, 0, 1);
		VectorOps.add(v1,v1,v3);
		VectorOps.multiply(v1, -0.5, v1);
		VectorOps.add(v3,v1,v2);
		if (VectorOps.norm(v3) > flatness) {
		    return true;
		}
	    }
	    return false;
	case PathIterator3D.SEG_QUADTO:
	    offset = depth*9;
	    if (transform == null) {
		v1[0] = workspace[offset+6]-workspace[offset+0];
		v1[1] = workspace[offset+7]-workspace[offset+1];
		v1[2] = workspace[offset+8]-workspace[offset+2];
		v2[0] = workspace[offset+3]-workspace[offset+0];
		v2[1] = workspace[offset+4]-workspace[offset+1];
		v2[2] = workspace[offset+5]-workspace[offset+2];
	    } else {
		transform.transform(workspace,offset,ftmp,0,3);
		v1[0] = ftmp[6]-ftmp[0];
		v1[1] = ftmp[7]-ftmp[1];
		v1[2] = ftmp[8]-ftmp[2];
		v2[0] = ftmp[3]-ftmp[0];
		v2[1] = ftmp[4]-ftmp[1];
		v2[2] = ftmp[5]-ftmp[2];
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
		VectorOps.crossProduct(v3,v1,v2);
		dist = VectorOps.norm(v3);
	    }
	    if (dist > flatness) {
		return true;
	    } else {
		if (nonAffine) {
		    v1[0] = (workspace[offset+6]+workspace[offset+0])/2.0;
		    v1[1] = (workspace[offset+7]+workspace[offset+1])/2.0;
		    v1[2] = (workspace[offset+8]+workspace[offset+2])/2.0;
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
	    }
	case PathIterator3D.SEG_CUBICTO:
	    offset = depth*12;
	    if (transform == null) {
		v1[0] = workspace[offset+9]-workspace[offset+0];
		v1[1] = workspace[offset+10]-workspace[offset+1];
		v1[2] = workspace[offset+11]-workspace[offset+2];
		v2[0] = workspace[offset+3]-workspace[offset+0];
		v2[1] = workspace[offset+4]-workspace[offset+1];
		v2[2] = workspace[offset+5]-workspace[offset+2];
	    } else {
		transform.transform(workspace, offset, ftmp, 0, 4);
		v1[0] = ftmp[9]-ftmp[0];
		v1[1] = ftmp[10]-ftmp[1];
		v1[2] = ftmp[11]-ftmp[2];
		v2[0] = ftmp[3]-ftmp[0];
		v2[1] = ftmp[4]-ftmp[1];
		v2[2] = ftmp[5]-ftmp[2];
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
		VectorOps.crossProduct(v3,v1,v2);
		dist = VectorOps.norm(v3);
	    }
	    if (dist > flatness) {
		return true;
	    }
	    if (transform == null) {
		v2[0] = workspace[offset+6]-workspace[offset+0];
		v2[1] = workspace[offset+7]-workspace[offset+1];
		v2[2] = workspace[offset+8]-workspace[offset+2];
	    } else {
		v2[0] = ftmp[6]-ftmp[0];
		v2[1] = ftmp[7]-ftmp[1];
		v2[2] = ftmp[8]-ftmp[2];
	    }
	    direct = (VectorOps.dotProduct(v1,v2) <= 0.0)
		|| (VectorOps.norm(v1) < VectorOps.norm(v2));
	    if (direct) {
		// dist = VectorOps.norm(v2);
		// split so each flattened segment is a line, not a loop.
		return true;
	    } else {
		VectorOps.normalize(v1);
		VectorOps.crossProduct(v3,v1,v2);
		dist = VectorOps.norm(v3);
	    }
	    if (dist > flatness) {
		return true;
	    }

	    if (transform == null) {
		v2[0] = workspace[offset+6] - workspace[offset+3];
		v2[1] = workspace[offset+7] - workspace[offset+4];
		v2[2] = workspace[offset+8] - workspace[offset+5];
	    } else {
		v2[0] = ftmp[6] - ftmp[3];
		v2[1] = ftmp[7] - ftmp[4];
		v2[2] = ftmp[8] - ftmp[5];
	    }
	    VectorOps.normalize(v2);
	    if (VectorOps.dotProduct(v1, v2) < 0.5) {
		return true;
	    }

	    if (nonAffine) {
		v1[0] = (workspace[offset+9]+workspace[offset+0])/2.0;
		v1[1] = (workspace[offset+10]+workspace[offset+1])/2.0;
		v1[2] = (workspace[offset+11]+workspace[offset+2])/2.0;
		transform.transform(v1, 0, v2, 0, 1);
		transform.transform(workspace, offset, v1, 0, 1);
		transform.transform(workspace, offset+9, v3, 0, 1);
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
	case PathIterator3D.SEG_CLOSE:
	case PathIterator3D.SEG_LINETO:
	    int depth6 = 6*depth;
	    int ndepth6 = depth6 + 6;
	    for (int j = 0; j < 3; j++) {
		workspace[ndepth6+j] = workspace[depth6+j];
		workspace[ndepth6+3+j] =
		    (workspace[depth6+j] + workspace[depth6+3+j])/2.0;
		workspace[depth6+j] = workspace[ndepth6+3+j];
	    }
	    break;
	case PathIterator3D.SEG_QUADTO:
	    int depth9 = 9*depth;
	    for (int i = 1; i < 3; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 3; j++) {
		    int k = 3*i+j;
		    tmp[0][im1][j] = (workspace[depth9 + k-3] + 
				      workspace[depth9 + k])/2.0;
		}
	    }
	    for (int j = 0; j < 3; j++) {
		tmp[1][0][j] = (tmp[0][0][j] + tmp[0][1][j])/2.0;
	    }
	    int ndepth9 = depth9 + 9;
	    for (int j = 0; j < 3; j++) {
		workspace[ndepth9+j] = workspace[depth9+j];
		workspace[ndepth9+3+j] = tmp[0][0][j];
		workspace[ndepth9+6+j] = tmp[1][0][j];
		workspace[depth9+j] = tmp[1][0][j];
		workspace[depth9+3+j] = tmp[0][1][j];
	    }
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    int depth12 = 12*depth;
	    for (int i = 1; i < 4; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 3; j++) {
		    int k = 3*i+j;
		    tmp[0][im1][j] = (workspace[depth12 + k-3] + 
				      workspace[depth12 + k])/2.0;
		}
	    }
	    for (int i = 1; i < 3; i++) {
		int im1 = i - 1;
		for (int j = 0; j < 3; j++) {
		    int k = 3*i+j;
		    tmp[1][im1][j] = (tmp[0][im1][j] + tmp[0][i][j])/2.0;
		}
	    }
	    for (int j = 0; j < 3; j++) {
		tmp[2][0][j] = (tmp[1][0][j] + tmp[1][1][j])/2.0;
	    }
	    int ndepth12 = depth12 + 12;
	    for (int j = 0; j < 3; j++) {
		workspace[ndepth12+j] = workspace[depth12+j];
		workspace[ndepth12+3+j] = tmp[0][0][j];
		workspace[ndepth12+6+j] = tmp[1][0][j];
		workspace[ndepth12+9+j] = tmp[2][0][j];
		workspace[depth12+j] = tmp[2][0][j];
		workspace[depth12+3+j] = tmp[1][1][j];
		workspace[depth12+6+j] = tmp[0][2][j];
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
	case PathIterator3D.SEG_CLOSE:
	    len = (depth == 0)? 0: 3;
	    break;
	case PathIterator3D.SEG_MOVETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_LINETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    len = 6;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    len = 9;
	    break;
	case -1:
	    // Works because the first segment in a path must be
	    // SEG_MOVETO, so we don't have to split anything.
	    if (transform != null) {
		int smode = src.currentSegment(workspace);
		transform.transform(workspace, 0, coords, 0, 1);
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (len > 0) {
	    int offset = depth*(len+3) + 3;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, len/3);
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
	case PathIterator3D.SEG_CLOSE:
	    len = (depth == 0)? 0: 3;
	    break;
	case PathIterator3D.SEG_MOVETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_LINETO:
	    len = 3;
	    break;
	case PathIterator3D.SEG_QUADTO:
	    len = 6;
	    break;
	case PathIterator3D.SEG_CUBICTO:
	    len = 9;
	    break;
	case -1:
	    // Works because the first segment in a path must be
	    // SEG_MOVETO, so we don't have to split anything.
	    if (transform != null) {
		int smode = src.currentSegment(workspace);
		transform.transform(workspace, 0, coords, 0, 1);
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (transform != null) {
	    currentSegment(ftmp);
	    for (int i = 0; i < len; i++) {
		coords[i] = (float) ftmp[i];
	    }
	} else {
	    int offset = depth*(len+3) + 3;
	    for (int i = 0; i < len; i++) {
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
		if (mode == SEG_MOVETO) {
		    System.arraycopy(workspace, 0, lastMoveTo, 0, 3);
		}
	    } else {
		switch (mode) {
		case PathIterator3D.SEG_CLOSE:
		case PathIterator3D.SEG_MOVETO:
		case PathIterator3D.SEG_LINETO:
		    System.arraycopy(workspace, 3, workspace, 0, 3);
		    break;
		case PathIterator3D.SEG_QUADTO:
		    System.arraycopy(workspace, 6, workspace, 0, 3);
		    break;
		case PathIterator3D.SEG_CUBICTO:
		    System.arraycopy(workspace, 9, workspace, 0, 3);
		    break;
		default:
		    break;
		}
	    }
	    src.next();
	    if (src.isDone()) {
		// proactively set mode, etc., to a safe value, as
		// Java JDK path iterators do that.
		mode = PathIterator3D.SEG_MOVETO;
		workspace[3] = 0.0; workspace[4] = 0.0; workspace[5] = 0.0;
		return;
	    }
	    splitCount[0] = 0;
	    mode = src.currentSegment(ourcoords);
	    switch (mode) {
	    case PathIterator3D.SEG_CLOSE:
		System.arraycopy(lastMoveTo, 0, workspace, 3, 3);
		if (((float)lastMoveTo[0] != (float)workspace[0])
		    || ((float)lastMoveTo[1] != (float)workspace[1])
		    || ((float)lastMoveTo[2] != (float)workspace[2])) {
		    if (flatness == 0.0 || nonAffine) {
			partition(false);
		    }
		}
		break;
	    case PathIterator3D.SEG_MOVETO:
		System.arraycopy(ourcoords, 0, lastMoveTo, 0, 3);
		System.arraycopy(ourcoords, 0, workspace, 3, 3);
		break;
	    case PathIterator3D.SEG_LINETO:
		System.arraycopy(ourcoords, 0, workspace, 3, 3);
		if (flatness == 0.0 || nonAffine) {
		    partition(false);
		}
		break;
	    case PathIterator3D.SEG_QUADTO:
		System.arraycopy(ourcoords, 0, workspace, 3, 6);
		partition(false);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.arraycopy(ourcoords, 0, workspace, 3, 9);
		partition(false);
	    default:
		break;
	    }
	} else {
	    depth--;
	    partition(true);
	}
    }
}
//  LocalWords:  eacute zier FlatteningPathIterator PathIterator src
//  LocalWords:  Bezier
