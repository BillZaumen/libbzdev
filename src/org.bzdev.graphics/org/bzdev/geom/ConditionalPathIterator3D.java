package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.VectorOps;
import java.util.Arrays;
import java.util.function.Predicate;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Conditional path iterator class for three dimensions.
 * This iterator adds segments to the segments returned by
 * another path iterator by splitting quadratic and cubic  B&eacute;zier-curve
 * segments unless the points along a segment fails to satisfy a condition
 * that indicates that splitting should continue. If that condition is not
 * satisfied, splitting may continue in some circumstances: the
 * second control point, for example, should not be closer to the initial
 * control point than the first control point. The condition is a predicate
 * whose argument is a control point array including the end points of a
 * segment. It will have a length of 9 for qudratic segments and a length of
 * 12 for cubic segments.
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
public class ConditionalPathIterator3D implements PathIterator3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    PathIterator3D src;
    Predicate<double[]> condition = null;
    int limit = 10;
    AffineTransform3D transform = null;

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
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param condition the condition
     */
    public ConditionalPathIterator3D(PathIterator3D src,
				     Predicate<double[]> condition)
	throws IllegalArgumentException
    {
	if (condition == null) {
	    throw new IllegalArgumentException(errorMsg("nullCondition"));
	}
	this.src = src;
	this.condition = condition;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }


    /**
     * Constructor with a recursion limit.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * @param src a path iterator to flatten
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(PathIterator3D src,
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
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor given a transform.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * The default recursion limit is 10.
     * @param src a path iterator to flatten
     * @param transform the transform to apply
     * @param condition the condition
     */
    public ConditionalPathIterator3D(PathIterator3D src,
				     AffineTransform3D transform,
				     Predicate<double[]> condition)
	throws IllegalArgumentException
    {
	if (condition == null) {
	    throw new IllegalArgumentException(errorMsg("nullCondition"));
	}
	this.src = src;
	this.transform = transform;
	this.condition = condition;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor with a transform and a recursion limit.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * The limit must be non-negative.
     * @param src a path iterator to flatten
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(PathIterator3D src,
				     AffineTransform3D transform,
				     Predicate<double[]> condition, int limit)
	throws IllegalArgumentException
    {
	if (limit < 0) {
	    throw new IllegalArgumentException(errorMsg("negativeLimit"));
	}
	if (condition == null) {
	    throw new IllegalArgumentException(errorMsg("nullCondition"));
	}
	this.src = src;
	this.transform = transform;
	this.condition = condition;
	this.limit = limit;
	workspace = new double[12*(limit+1)];
	splitCount = new int[limit+1];
    }

    /**
     * Constructor give a type and a double-precision coordinate array.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
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
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(int type, double x0, double y0, double z0,
				    double[] coords,
				    Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     condition, limit);
    }

    /**
     * Constructor given a type and a single-precision coordinate array.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO},
     *        {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(int type, float x0, float y0, float z0,
				    float[] coords,
				    Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     condition, limit);
    }

    /**
     * Constructor give a type and double-precision coordinate array,
     * with an AffineTransform.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO}, {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(int type, double x0, double y0, double z0,
				    double[] coords,
				    AffineTransform3D transform,
				    Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, condition, limit);
    }

    /**
     * Constructor give a type and single-precision coordinate array,
     * with an AffineTransform3D.
     * The limit must be non-negative.
     * The condition's test method must return true if the the path is
     * to be flattened further and false otherwise.  The test method's
     * argument is an array containing the control points, including
     * the starting point for a segment.  The length of the array is 9
     * when a segment's type is {@link PathIterator3D#SEG_QUADTO} and is
     * 12 when a segment's type is {@link PathIterator3D#SEG_CUBICTO}.
     * Segments whose type is {@link PathIterator3D#SEG_MOVETO},
     * {@link PathIterator3D#SEG_LINETO}, or {@link PathIterator3D#SEG_CLOSE}
     * are not flattened.
     * @param type the type of a segment ({@link PathIterator3D#SEG_MOVETO},
     *        {@link PathIterator3D#SEG_LINETO},
     *        {@link PathIterator3D#SEG_QUADTO},
     *        {@link PathIterator3D#SEG_CUBICTO})
     * @param x0 the starting X coordinate for the segment
     * @param y0 the starting Y coordinate for the segment,
     * @param z0 the starting Z coordinate for the segment,
     * @param coords the coordinates for the segment as returned by
     *        {@link #currentSegment(double[])}
     * @param transform a transform to apply
     * @param condition the condition
     * @param limit the recursion limit
     */
    public ConditionalPathIterator3D(int type, float x0, float y0, float z0,
				    float[] coords,
				    AffineTransform3D transform,
				    Predicate<double[]> condition, int limit)
    {
	this(new SegmentPathIterator(type, x0, y0, z0, coords),
	     transform, condition, limit);
    }

    /**
     * Get the condition parameter.
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
	double[] tcoords = null;
	double dist;
	boolean direct;
	int offset;
	switch(mode) {
	case PathIterator3D.SEG_LINETO:
	case PathIterator3D.SEG_CLOSE:
	    return false;
	case PathIterator3D.SEG_QUADTO:
	    offset = depth*9;
	    tcoords = new double[9];
	    if (transform == null) {
		v1[0] = workspace[offset+6]-workspace[offset+0];
		v1[1] = workspace[offset+7]-workspace[offset+1];
		v1[2] = workspace[offset+8]-workspace[offset+2];
		v2[0] = workspace[offset+3]-workspace[offset+0];
		v2[1] = workspace[offset+4]-workspace[offset+1];
		v2[2] = workspace[offset+5]-workspace[offset+2];
		System.arraycopy(workspace,offset, tcoords, 0, 9);
	    } else {
		transform.transform(workspace,offset,ftmp,0,3);
		System.arraycopy(ftmp, 0, tcoords, 0, 9);
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
	    }
	    if (condition.test(tcoords)) {
		return true;
	    } else {
		return false;
	    }
	case PathIterator3D.SEG_CUBICTO:
	    offset = depth*12;
	    tcoords = new double[12];
	    if (transform == null) {
		v1[0] = workspace[offset+9]-workspace[offset+0];
		v1[1] = workspace[offset+10]-workspace[offset+1];
		v1[2] = workspace[offset+11]-workspace[offset+2];
		v2[0] = workspace[offset+3]-workspace[offset+0];
		v2[1] = workspace[offset+4]-workspace[offset+1];
		v2[2] = workspace[offset+5]-workspace[offset+2];
		System.arraycopy(workspace, offset, tcoords, 0, 12);
	    } else {
		transform.transform(workspace, offset, ftmp, 0, 4);
		System.arraycopy(ftmp, 0, tcoords, 0, 12);
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
		break;
	    case PathIterator3D.SEG_MOVETO:
		System.arraycopy(ourcoords, 0, lastMoveTo, 0, 3);
		System.arraycopy(ourcoords, 0, workspace, 3, 3);
		break;
	    case PathIterator3D.SEG_LINETO:
		System.arraycopy(ourcoords, 0, workspace, 3, 3);
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
//  LocalWords:  eacute zier ConditionalPathIterator PathIterator src
//  LocalWords:  Bezier
