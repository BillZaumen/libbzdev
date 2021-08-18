package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.VectorOps;
import java.awt.geom.*;
import java.util.function.Predicate;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Conditional path iterator class for two dimensions.
 * This iterator adds segments to the segments returned by
 * another path iterator by splitting quadratic and cubic  B&eacute;zier-curve
 * segments unless the points along a segment fails to satisfy a condition
 * that indicates that splitting should continue. If that condition is not
 * satisfied, splitting may continue in some circumstances: the
 * second control point, for example, should not be closer to the initial
 * control point than the first control point.
 * <P>
 * The iterator has a recursion limit (the default value is 10) to
 * prevent the number of segments from becoming excessively large.
 * Incrementing the recursion limit by 1 can double the number of
 * segments from that returned by the previous limit.
 * <P>
 * The path may be partially flattened.
 * This class does not modify segment types, unlike
 * {@link java.awt.geom.FlatteningPathIterator} which turns quadratic
 * and cubic segments into straight lines.
 */
public class ConditionalPathIterator2D implements PathIterator {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    PathIterator src;
    Transform2D transform;
    AffineTransform atransform;
    int limit = 10;

    Predicate<double[]> condition = null;

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
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param condition the condition
     */
    public ConditionalPathIterator2D(PathIterator src,
				     Predicate<double[]> condition)
	throws IllegalArgumentException
    {
	this.src = src;
	this.condition = condition;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }


    /**
     * Constructor given an affine transform.
     * The transform is not constrained to be an affine transform.
     * The default recursion limit is 10.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param transform the transform to apply
     * @param condition the condition
     */
    public ConditionalPathIterator2D(PathIterator src,
				     AffineTransform transform,
				     Predicate<double[]> condition)
	throws IllegalArgumentException
    {
	this.src = src;
	atransform = transform;
	this.condition = condition;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor with a recursion limit.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(PathIterator src,
				     Predicate<double[]> condition,
				     int limit)
	throws IllegalArgumentException
    {
	if (limit < 0 ) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (condition == null) {
	    throw new IllegalArgumentException(errorMsg("nullCondition"));
	}
	this.src = src;
	this.condition = condition;
	this.limit = limit;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and a double-precision coordinate array.
     * The the limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * If the segment type is PathIterator.SEG_MOVETO, the arguments
     * x0 and y0 are ignored.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(int type, double x0, double y0,
				     double[] coords,
				     Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords), condition, limit);
    }

    /**
     * Constructor given a type and a single-precision coordinate array.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(int type, float x0, float y0,
				     float[] coords,
				     Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords), condition, limit);
    }


    /**
     * Constructor given an affine transform and a recursion limit.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(PathIterator src,
				     AffineTransform transform,
				     Predicate<double[]> condition, int limit)
	throws IllegalArgumentException
    {
	if (limit < 0 ) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (condition == null) {
	    throw new IllegalArgumentException(errorMsg("nullCondition"));
	}
	this.src = src;
	atransform = transform;
	this.condition = condition;
	this.limit = limit;
	workspace = new double[8*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and double-precision coordinate array,
     * with an AffineTransform.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(int type, double x0, double y0,
				     double[] coords,
				     AffineTransform transform,
				     Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, condition, limit);
    }

    /**
     * Constructor give a type and single-precision coordinate array,
     * with an AffineTransform.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 6
     * when a segment's type is {@link PathIterator#SEG_QUADTO} and is
     * 8 when a segment's type is {@link PathIterator#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator#SEG_MOVETO},
     * {@link PathIterator#SEG_LINETO}, or {@link PathIterator#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator#SEG_MOVETO},
     *        {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
     *        {@link PathIterator#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator2D(int type, float x0, float y0,
				     float[] coords,
				     AffineTransform transform,
				     Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, coords),
	     transform, condition, limit);
    }


    /**
     * Get the condition.
     * @return the condition
     */
    public Predicate<double[]> getCondition() {return condition;}

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
	double dist;
	boolean direct;
	int offset;
	double[] tcoords;
	switch(mode) {
	case PathIterator.SEG_LINETO:
	case PathIterator.SEG_CLOSE:
	    return false;
	case PathIterator.SEG_QUADTO:
	    offset = depth*6;
	    tcoords = new double[6];
	    if (atransform == null) {
		v1[0] = workspace[offset+4]-workspace[offset+0];
		v1[1] = workspace[offset+5]-workspace[offset+1];
		v2[0] = workspace[offset+2]-workspace[offset+0];
		v2[1] = workspace[offset+3]-workspace[offset+1];
		System.arraycopy(workspace,offset, tcoords, 0, 6);
	    } else {
		atransform.transform(workspace,offset,ftmp,0,3);
		System.arraycopy(ftmp, 0, tcoords, 0, 6);
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
	    }


	    if (condition.test(tcoords)) {
		return true;
	    } else {
		return false;
	    }
	case PathIterator.SEG_CUBICTO:
	    offset = depth*8;
	    tcoords = new double[8];
	    if (atransform == null) {
		v1[0] = workspace[offset+6]-workspace[offset+0];
		v1[1] = workspace[offset+7]-workspace[offset+1];
		v2[0] = workspace[offset+2]-workspace[offset+0];
		v2[1] = workspace[offset+3]-workspace[offset+1];
		System.arraycopy(workspace, offset, tcoords, 0, 8);
	    } else {
		atransform.transform(workspace, offset, ftmp, 0, 4);
		System.arraycopy(ftmp, 0, tcoords, 0, 8);
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
	    } 

	    if (atransform == null) {
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
	    }

	    if (atransform == null) {
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
	    if (condition.test(tcoords)) {
		return true;
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
	    if (atransform != null) {
		int smode = src.currentSegment(workspace);
		atransform.transform(workspace, 0, coords, 0, 1);
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (len > 0) {
	    int offset = depth*(len+2) + 2;
	    if (atransform != null) {
		atransform.transform(workspace, offset, coords, 0, len/2);
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
	    if (atransform != null) {
		int smode = src.currentSegment(workspace);
		atransform.transform(workspace, 0, coords, 0, 1);
		return smode;
	    } else {
		return src.currentSegment(coords);
	    }
	default:
	    throw new IllegalStateException("unknown mode");
	}
	if (atransform != null) {
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
		break;
	    case PathIterator.SEG_MOVETO:
		System.arraycopy(ourcoords, 0, lastMoveTo, 0, 2);
		System.arraycopy(ourcoords, 0, workspace, 2, 2);
		break;
	    case PathIterator.SEG_LINETO:
		System.arraycopy(ourcoords, 0, workspace, 2, 2);
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
//  LocalWords:  eacute zier ConditionalPathIterator PathIterator src
//  LocalWords:  Bezier
