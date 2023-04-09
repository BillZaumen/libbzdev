package org.bzdev.geom;
import org.bzdev.math.MatrixOps;
import org.bzdev.math.Functions;
import org.bzdev.math.Binomial;
import org.bzdev.math.VectorOps;
import java.awt.Color;
import java.util.Arrays;

//@exbundle org.bzdev.geom.lpack.Geom


/**
 * Surface iterator for subdividing a surface's segments.
 * Segments of a surface or other implementations of Shape3D
 * will be subdivided into quarters. This is
 * repeated recursively until a limit is reached.
 * <P>
 * When the limit is 0, there are no subdivisions. When the
 * limit is 1, there are 4 subdivisions, and when the limit is
 * 2, there are 16 subdivisions.
 * <P>
 * Quartering in this way is useful for 3D printing: the
 * STL format requires that triangles meet at vertices and
 * subdividing in this way uniformly ensures that condition
 * will be met.
 */
public class SubdivisionIterator implements SurfaceIterator {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    int limit;

    SurfaceIterator src;
    Transform3D transform = null;
    int[] splitCount;
    int[] currentMode;

    @Override
    public boolean isOriented() {return src.isOriented();}

    /**
     * Constructor.
     * @param src the surface iterator for a Shape3D to subdivide
     * @param limit the recursion limit: the number of times each
     *        segment of a Shape3D will split into quarters
     */
    public SubdivisionIterator(SurfaceIterator src, int limit) {
	this.src = src;
	this.limit = limit;
	workspace = new double[3*48*(limit + 1)];
	splitCount = new int[3*(limit+1)];
	currentMode = new int[3*(limit+1)];
	currentMode[0] = -1;
    }

    /**
     * Constructor providing a transform.
     * @param src the surface iterator for a Shape3D to subdivide
     * @param transform the transform to apply to each of the new
     *        segments
     * @param limit the recursion limit: the number of times each
     *        segment of a Shape3D will split into quarters
     */
    public SubdivisionIterator(SurfaceIterator src,
			       Transform3D transform,
			       int limit)
    {
	this.src = src;
	this.limit = limit;
	workspace = new double[3*48*(limit + 1)];
	splitCount = new int[3*(limit+1)];
	this.transform = transform;
    }

    /**
     * Get the recursion limit.
     * @return the recursion limit
     */
    public int getRecursionLimit() {return limit;}

    boolean usingRecursion = false;
    double[] workspace = null;
    double ftmp[] = new double[48];

    int depth = 0;

    @Override
    public int currentSegment(double[] coords) {
	int offset;
	int mode = currentMode[depth];
	switch(mode) {
	case CUBIC_PATCH:
	    offset = depth*48;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 16);
	    } else {
		System.arraycopy(workspace, offset, coords, 0, 48);
	    }
	    /*
	    if (limit > 0) {
		for (int i = 0; i < 48; i++) coords[i] = (float)coords[i];
	    }
	    */
	    break;
	case CUBIC_TRIANGLE:
	    offset = depth*30;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 10);
	    } else {
		System.arraycopy(workspace, offset, coords, 0, 30);
	    }
	    /*
	    if (limit > 0) {
		for (int i = 0; i < 30; i++) coords[i] = (float)coords[i];
	    }
	    */
	    break;
	case CUBIC_VERTEX:
	    offset = depth*48;	// Partitioned into cubic patches
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 5);
	    } else {
		System.arraycopy(workspace, offset, coords, 0, 15);
	    }
	    /*
	    if (limit > 0) {
		for (int i = 0; i < 15; i++) coords[i] = (float)coords[i];
	    }
	    */
	    break;
	case PLANAR_TRIANGLE:
	    offset = depth*9;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 3);
	    } else {
		System.arraycopy(workspace, offset, coords, 0, 9);
	    }
	    /*
	    if (limit > 0) {
		for (int i = 0; i < 9; i++) coords[i] = (float)coords[i];
	    }
	    */
	    break;
	case -1:
	    mode = src.currentSegment(workspace);
	    currentMode[depth] = mode;
	    partition();
	    return currentSegment(coords);
	default:
	    throw new IllegalStateException("unknown mode");
	}
	return mode;
    }

    @Override
    public int currentSegment(float[] coords) {
	int offset;
	int mode = currentMode[depth];
	switch(mode) {
	case CUBIC_PATCH:
	    offset = depth*48;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 16);
	    } else {
		for (int i = 0; i < 48; i++) {
		    coords[i] = (float)workspace[offset+i];
		}
	    }
	    break;
	case CUBIC_TRIANGLE:
	    offset = depth*30;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 10);
	    } else {
		for (int i = 0; i < 30; i++) {
		    coords[i] = (float)workspace[offset+i];
		}
	    }
	    break;
	case CUBIC_VERTEX:
	    offset = depth*48;	// Partitioned into cubic patches
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 5);
	    } else {
		System.arraycopy(workspace, offset, coords, 0, 15);
	    }
	    break;
	case PLANAR_TRIANGLE:
	    offset = depth*9;
	    if (transform != null) {
		transform.transform(workspace, offset, coords, 0, 3);
	    } else {
		for (int i = 0; i < 9; i++) {
		    coords[i] = (float)workspace[offset+i];
		}
	    }
	    break;
	case -1:
	    mode = src.currentSegment(workspace);
	    currentMode[0] = mode;
	    partition();
	    return currentSegment(coords);
	default:
	    throw new IllegalStateException("unknown mode");
	}
	return mode;
    }
    
    @Override
    public Object currentTag() {
	return src.currentTag();
    }

    @Override
    public Color currentColor() {
	return src.currentColor();
    }

    @Override
    public boolean isDone() {
	if (depth > 0) return false;
	return src.isDone();
    }

    @Override
    public void next() {
	if (depth == 0) {
	    if (src.isDone()) {
		return;
	    }
	    if (limit == 0) {
		src.next();
		int mode = src.currentSegment(workspace);
		currentMode[0] = mode;
		return;
	    }
	    if (currentMode[0] == -1) {
		int mode = src.currentSegment(workspace);
		currentMode[0] = mode;
		partition();
		next();
	    } else {
		src.next();
		if (src.isDone()) return;
		splitCount[0] = 0;
		int mode = src.currentSegment(workspace);
		currentMode[0] = mode;
		partition();
	    }
	} else {
	    depth--;
	    partition();
	}
	return;
    }

    private void partition() {
	while (splitCount[depth] < limit) {
	    quarter();
	}
    }

    private void quarter()
    {
	int offset;
	int mode = currentMode[depth];
	switch (mode) {
	case CUBIC_PATCH:
	    offset = depth*48;
	    getBezierPatchSub(workspace, offset, workspace, offset, 3);
	    offset += 48;
	    getBezierPatchSub(workspace, offset, null, offset, 2);
	    offset += 48;
	    getBezierPatchSub(workspace, offset, null, offset, 1);
	    offset += 48;
	    getBezierPatchSub(workspace, offset, null, offset, 0);
	    break;
	case CUBIC_TRIANGLE:
	    offset = depth*30;
	    int offset1 = offset + 30;
	    int offset2 = offset1 + 30;
	    int offset3 = offset2 + 30;
	    permuteCubicTriangle(workspace,offset);
	    splitCubicTriangle(workspace, offset, workspace, offset,
			       workspace, offset1);
	    splitCubicTriangle(workspace, offset, workspace, offset2,
			       workspace, offset3);
	    splitCubicTriangle(workspace, offset1, workspace, offset,
			       workspace, offset1);
	    break;
	    /*
	    getBezierTriangleSub(workspace, offset, workspace, offset, 3);
	    offset += 30;
	    getBezierTriangleSub(workspace, offset, null, offset, 2);
	    offset += 30;
	    getBezierTriangleSub(workspace, offset, null, offset, 1);
	    offset += 30;
	    getBezierTriangleSub(workspace, offset, null, offset, 0);
	    break;
	    */
	case PLANAR_TRIANGLE:
	    offset = depth*9;
	    getPlanarTriangleSub(workspace, offset, workspace, offset, 3);
	    offset += 9;
	    getPlanarTriangleSub(workspace, offset, null, offset, 2);
	    offset += 9;
	    getPlanarTriangleSub(workspace, offset, null, offset, 1);
	    offset += 9;
	    getPlanarTriangleSub(workspace, offset, null, offset, 0);
	    break;
	case CUBIC_VERTEX:
	    offset = depth*48;	// Partitioned into cubic patches
	    System.arraycopy(workspace, offset, tmpcoords3, 0, 15);
	    double[] tmp = new double[3];
	    System.arraycopy(workspace, offset+12, tmp, 0, 3);
	    getCubicVertex0(workspace, offset, tmpcoords3, 0);
	    offset += 48;
	    getCubicVertex1(workspace, offset, tmpcoords3, 0);
	    offset += 48;
	    getCubicVertex(workspace, offset, workspace, offset-96, tmp);
	    offset += 48;
	    getCubicVertex(workspace, offset, workspace, offset-96, tmp);
	    currentMode[depth] = SurfaceIterator.CUBIC_PATCH;
	    break;
	}
	int sc = ++splitCount[depth];
	depth++;
	splitCount[depth] = sc;
	currentMode[depth] = (mode == SurfaceIterator.CUBIC_VERTEX)?
	    SurfaceIterator.CUBIC_PATCH: mode;
	depth++;
	splitCount[depth] = sc;
	currentMode[depth] = (mode == SurfaceIterator.CUBIC_VERTEX)?
	    SurfaceIterator.CUBIC_VERTEX: mode;
	depth++;
	splitCount[depth] = sc;
	currentMode[depth] = (mode == SurfaceIterator.CUBIC_VERTEX)?
	    SurfaceIterator.CUBIC_VERTEX: mode;
    }

    // See "Bezier Curves and Surfaces (2)", Hongxin Zhang and
    // Jieqing Feng, 20006-12-07, Zhejiang University.
    // http://www.cad.zju.edu.cn/home/zhx/GM/005/00-bcs2.pdf


    // matrices are 4 by 4 in column-major order and flattened.
    //               -                        -
    //              |  1.0   0.0   0.0   0.0   |
    //    SL =      |  0.5   0.5   0.0   0.0   |
    //		    |  0.25  0.5   0.25  0.0   |
    //		    |  0.125 0.375 0.375 0.125 |
    //               -                        -

    static final double SL[] = {1.0, 0.5, 0.25, 0.125,
				0.0, 0.5, 0.5, 0.375,
				0.0, 0.0, 0.25, 0.375,
				0.0, 0.0, 0.0, 0.125};

    static final double[] SLT = MatrixOps.transpose(SL, 4, 4, true);

    //               -                       -
    //              | 0.125 0.375 0.375 0.125 |
    //      SR =    | 0.0   0.25  0.5   0.25  |
    //              | 0.0   0.0   0.5   0.5   |
    //		    | 0.0   0.0   0.0   1.0   |
    //               -                       -
    
    static final double SR[] = {0.125, 0.0, 0.0, 0.0,
				0.375, 0.25, 0.0, 0.0,
				0.375, 0.5, 0.5, 0.0,
				0.125, 0.25, 0.5, 1.0};

    static final double[] SRT = MatrixOps.transpose(SR, 4, 4, true);

    /**
     * Find the value for the midpoint between two points given an
     * X, Y, or Z coordinate for each point.
     * A straight line between the two points is 'promoted' to the
     * corresponding cubic B&eacute;zier curve, and that curve
     * is subdivided, splitting it at the point where the path
     * parameter has the value 0.5.
     * <P>
     * The result, given exact arithmetic, is (x0 + x3) / 2. The
     * actual value returned may be slightly different because of
     * the use of double-precision arithmetical operations, but
     * will match the value used if an edge of a cubic patch
     * is a straight line created by using
     * {@link Path3D#setupCubic(double,double,double,double,double,double)}
     * and that edge is subdivided at a point where the path parameter
     * is 0.5. In addition, the value is computed with x0 and x3 exchanged
     * and the two are averaged so that the returned value is independent
     * of the line-segment  direction.
     * @param x0 the first coordinates (X, Y, or Z, matching the
     *        choice for x3)
     * @param x3 the second coordinate (X, Y, or Z, matching the
     *        choice for x0)
     * @return the midpoint between x0 and x3
     */
    public static double midcoord(double x0, double x3) {
	double x1 = x0*2.0/3.0 + x3/3.0;
	double x2 = x0/3.0 + x3*2.0/3.0;
	double result =  0.125*x0 + 0.375*x1 + 0.375*x2 + 0.125*x3;
	//result if the curve was reversed.
	double resultr = 0.125*x3 + 0.375*x2 + 0.375*x1 + 0.125*x0;

	return (result + resultr)/2.0;

    }


    /**
     * Split a cubic B&eacute;zier curve into two parts at the midpoint of
     * the path parameter.
     * <P>
     * This method uses the same algorithm as the one used to subdivide
     * a cubic patch, so that the end points and intermediate control points
     * in both cases will match exactly. The class
     * {@link org.bzdev.p3d.Model3D} uses this method to subdivide a
     * cubic vertex for tessellation so that floating-point round-off
     * errors do not result in an unprintable object.
     * @param curve the four control points making up a cubic B&eacute; curve
     * @return an array of vectors, each of length 12, contaiing the
     *         countrol points for the left and right partiions
     *         respectively
     */
    public static double[][] splitCubicBezierCurve(double[] curve) {
	return splitCubicBezierCurve(curve, 0);
	/*
	double[][] results = new double[2][];
	results[0] = new double[12];
	results[1] = new double[12];
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(results[0], 4, 1, 0, SL, 4, 4, 0,
			       curve, 4, 1, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(results[1], 4, 1, 0, SR, 4, 4, 0,
			       curve, 4, 1, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	return results;
	*/
    }

    /**
     * Split a cubic B&eacute;zier curve into two parts at the midpoint of
     * the path parameter, given an offset.
     * <P>
     * This method uses the same algorithm as the one used to subdivide
     * a cubic patch, so that the end points and intermediate control points
     * in both cases will match exactly. The class
     * {@link org.bzdev.p3d.Model3D} uses this method to subdivide a
     * cubic vertex for tessellation so that floating-point round-off
     * errors do not result in an unprintable object.
     * @param curve the four control points making up a cubic B&eacute; curve
     * @param offset the offset into the curve array at which the
     *        cubic B&eacute; curve starts.
     * @return an array of vectors, each of length 12, contaiing the
     *         countrol points for the left and right partiions
     *         respectively
     */
    public static double[][] splitCubicBezierCurve(double[] curve,
						   int offset)
    {
	double[][] results = new double[2][];
	results[0] = new double[12];
	results[1] = new double[12];
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(results[0], 4, 1, 0, SL, 4, 4, 0,
			       curve, 4, 1, offset,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(results[1], 4, 1, 0, SR, 4, 4, 0,
			       curve, 4, 1, offset,
			       true, i, 3, 0, 1, i, 3);
	}
	double[] tmp = new double[12];
	double[] tmp2 = new double[12];
	int ii = 3;
	for (int i = 0; i < 4; i++, ii--) {
	    int j = i*3;
	    int jj = ii*3;
	    tmp[j] = curve[offset +jj];
	    tmp[j+1] = curve[offset + jj + 1];
	    tmp[j+2] = curve[offset + jj + 2];
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(tmp2, 4, 1, 0, SR, 4, 4, 0,
			       tmp, 4, 1, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	ii = 3;
	for (int i = 0; i < 2; i++, ii--) {
	    int j = i*3;
	    int jj = ii*3;
	    double tval = tmp2[j];
	    tmp2[j] = tmp2[jj];
	    tmp2[jj] = tval;
	    tval = tmp2[j+1];
	    tmp2[j+1] = tmp2[jj+1];
	    tmp2[jj+1] = tval;
	    tval = tmp2[j+2];
	    tmp2[j+2] = tmp2[jj+2];
	    tmp2[jj+2] = tval;
	}
	for (int i = 0; i < 12; i++) {
	    results[0][i] = (results[0][i] + tmp2[i]) / 2.0;
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(tmp2, 4, 1, 0, SL, 4, 4, 0,
			       tmp, 4, 1, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	ii = 3;
	for (int i = 0; i < 2; i++, ii--) {
	    int j = i*3;
	    int jj = ii*3;
	    double tval = tmp2[j];
	    tmp2[j] = tmp2[jj];
	    tmp2[jj] = tval;
	    tval = tmp2[j+1];
	    tmp2[j+1] = tmp2[jj+1];
	    tmp2[jj+1] = tval;
	    tval = tmp2[j+2];
	    tmp2[j+2] = tmp2[jj+2];
	    tmp2[jj+2] = tval;
	}
	for (int i = 0; i < 12; i++) {
	    results[1][i] = (results[1][i] + tmp2[i]) / 2.0;
	}
	return results;
    }


    // Define constants for the array position, named after the control
    // points. This makes it easy to check the arrays v0Indices and
    // w0Indices against the diagram provided by the documentation for
    // Surface3D.addCubicTriangle to verify that the order is correct.
    static final int P003 = 0;
    static final int P012 = 3;
    static final int P021 = 6;
    static final int P030 = 9;
    static final int P102 = 12;
    static final int P111 = 15;
    static final int P120 = 18;
    static final int P201 = 21;
    static final int P210 = 24;
    static final int P300 = 27;

    static final int v0Indices[] = {
	P300, P201, P102, P003, P210, P111, P012, P120, P021, P030
    };

    static final int w0Indices[] = {
	P030, P120, P210, P300, P021, P111, P201, P012, P102, P003
    };

    /**
     * Permute coordinates so that the longest edge is bisected
     * when {@link #splitCubicTriangle(double[],int,double[],int,double[],int)}
     * is called.
     * <P>
     * If the u=0 edge is the longest one, the coords array
     * is not changed.  Otherwise the control points are permuted,
     * excluding P<sub>111</sub>, whose position never changes. The
     * final order for the control points in the coord array is
     * <UL>
     *  <LI><B>u=0</B>: P<sub>003</sub>, P<sub>012</sub>,
     *    P<sub>021</sub>, P<sub>030</sub>, P<sub>102</sub>, P<sub>111</sub>,
     *    P<sub>120</sub>, P<sub>201</sub> P<sub>210</sub>, P<sub>300</sub>.
     *  <LI><B>v=0</B>: P<sub>300</sub>, P<sub>201</sub>,
     *    P<sub>102</sub>, P<sub>003</sub>, P<sub>210</sub>, P<sub>111</sub>,
     *    P<sub>012</sub>, P<sub>120</sub>, P<sub>021</sub>, P<sub>030</sub>.
     *  <LI><B>w=0</B>: P<sub>030</sub>, P<sub>120</sub>,
     *    P<sub>210</sub>, P<sub>300</sub>, P<sub>021</sub>, P<sub>111</sub>,
     *    P<sub>201</sub>, P<sub>012</sub>, P<sub>102</sub>, P<sub>003</sub>.
     * </UL>
     * The mapping between the original control points and their offsets
     * into the array is shown in the following table:
     * <DIV style="text-align: center;">
     * <TABLE border="1"><caption>&nbsp;</caption>
     *  <TR> <TD>P<sub>003</sub><TD>0 <TD>P<sub>012</sub> <TD>3
     *  <TD>P<sub>021</sub> <TD>6  <TD>P<sub>030</sub> <TD>9
     *   <TD>P<sub>102</sub> <TD>12
     *  <TR> <TD>P<sub>111</sub> <TD>15 <TD>P<sub>120</sub> <TD>18
     *   <TD>P<sub>201</sub> <TD>21 <TD>P<sub>210</sub> <TD>24
     *   <TD>P<sub>300</sub><TD>27
     * </TABLE>
     * </DIV>
     * @param coords the coordinate array
     * @param offset the offest into coords at which the cubic-triangle
     *        coordinates start
     */
    public static void permuteCubicTriangle(double[] coords, int offset) {
	double[] tmp = null;
	double lenVsq = Point3D
	    .distanceSq(coords[offset], coords[offset+1], coords[offset+2],
			coords[offset+9], coords[offset+10], coords[offset+11]);
	double lenUsq = Point3D
	    .distanceSq(coords[offset], coords[offset+1], coords[offset+2],
			coords[offset+27], coords[offset+28],
			coords[offset+29]);
	double lenWsq = Point3D
	    .distanceSq(coords[offset+9], coords[offset+10], coords[offset+11],
			coords[offset+27], coords[offset+28],
			coords[offset+29]);
	boolean vmaxCase = (lenVsq >= lenUsq) && (lenVsq >= lenWsq);
	boolean umaxCase = (lenUsq >= lenVsq) && (lenUsq >= lenWsq);
	int map[] = null;

	if (vmaxCase) {
	    // edge where u == 0 is the longest edge
	    // Nothing to do.
	    return;
	} else if (umaxCase) {
	    // edge where v == 0 is the longest edge.
	    tmp = new double[30];
	    System.arraycopy(coords, offset, tmp, 0, 30);
	    map = v0Indices;
	} else {
	    // edge where w == 0 is the longest edge.
	    tmp = new double[30];
	    System.arraycopy(coords, offset, tmp, 0, 30);
	    map = w0Indices;
	}
	for (int i = 0; i < 10; i++) {
	    System.arraycopy(tmp, map[i], coords, offset + 3*i, 3);
	}
    }


    /**
     * Split a cubic B&eacute;zier triangle into two triangles
     * <P>
     * The coordinates are provided as X, Y, Z triplets in the
     * order specified by {@link SurfaceIterator}. Midpoints of
     * the edges are computed using
     * {@link #splitCubicBezierCurve(double[],int)}
     * so that there will be a perfect match with cubic B&eacute;zier
     * patches and planar triangles that may be split.
     * <P>
     * The new triangles are stored in the argument arrays coord1 and coord2,
     * starting at their respective offsets.  The coord array with its offset,
     * and 30 values, may overlap coord1 with its offset or coord2 with its
     * offset.
     * <P>
     * The algorithm used is similar to that described in the
     * <A HREF="https://en.wikipedia.org/wiki/B%C3%A9zier_triangle">
     * Wikipedia article</A> for cubic B&eacute;zier triangles, but
     * with the vertices reordered so calling this method on each
     * of the triangles it returns will result in the remaining two
     * edges from the original triangle being split.
     * @param coords the coordinates of the triangle to split
     * @param offset the offset into the coords array where the
     *        coordinate data starts
     * @param coords1 the coordinates of the first split triangle
     * @param offset1 the offset into the coords1 array where the
     *        coordinate data starts
     * @param coords2 the coordinates of the second split triangle
     * @param offset2 the offset into the coords2 array where the
     *        coordinate data starts
     */
    public static void splitCubicTriangle(double[] coords, int offset,
					  double[] coords1, int offset1,
					  double[] coords2, int offset2)
    {
	// orient the results so that splitting the split triangles
	// will split the edges of the original that had not yet
	// been split.
	double[][] results = new double[2][];
	boolean copy1 = (coords != coords1)
	    || (Math.abs(offset1 - offset) < 30);
	boolean copy2 = (coords != coords2)
	    || (Math.abs(offset2 - offset) < 30);

	results[0] = copy1? new double[30]: coords1;
	results[1] = copy2? new double[30]: coords2;
	int off1 = copy1? 0: offset1;
	int off2 = copy2? 0: offset2;

	double[] cp120 = new double[3];
	double[] cp210 = new double[3];
	double[] cp111L = new double[3];
	double[] cp111U = new double[3];

	double[][] splitEdges = SubdivisionIterator
	    .splitCubicBezierCurve(coords, offset);
	for (int i = 0; i < 3; i++) {
	    cp120[i] = 0.25*coords[offset+12+i]
		+ 0.5*coords[offset+15+i] + 0.25*coords[offset+18+i];
	    cp210[i] = 0.5*coords[offset+21+i] + 0.5*coords[offset+24+i];
	    cp111L[i] = 0.5*coords[offset+12+i] + 0.5*coords[offset+15+i];
	    cp111U[i] = 0.5*coords[offset+18+i] + 0.5*coords[offset+15+i];
	}

	System.arraycopy(coords, offset+27, results[0], off1+0, 3);
	System.arraycopy(coords, offset+21, results[0], off1+3, 3);
	System.arraycopy(coords, offset+12, results[0], off1+6, 3);
	System.arraycopy(coords, offset+0, results[0], off1+9, 3);
	System.arraycopy(cp210, 0, results[0], off1+12, 3);
	System.arraycopy(cp111L, 0, results[0], off1+15, 3);
	System.arraycopy(splitEdges[0], 3, results[0], off1+18, 3);
	System.arraycopy(cp120, 0, results[0], off1+21, 3);
	System.arraycopy(splitEdges[0], 6, results[0], off1+24, 3);
	System.arraycopy(splitEdges[0], 9, results[0], off1+27, 3);

	System.arraycopy(coords, offset+9, results[1], off2+0, 3);
	System.arraycopy(coords, offset+18, results[1], off2+3, 3);
	System.arraycopy(coords, offset+24, results[1], off2+6, 3);
	System.arraycopy(coords, offset+27, results[1], off2+9, 3);
	System.arraycopy(splitEdges[1], 6, results[1], off2+12, 3);
	System.arraycopy(cp111U, 0, results[1], off2+15, 3);
	System.arraycopy(cp210, 0, results[1], off2+18, 3);
	System.arraycopy(splitEdges[1], 3, results[1], off2+21, 3);
	System.arraycopy(cp120, 0, results[1], off2+24, 3);
	System.arraycopy(splitEdges[1], 0, results[1], off2+27, 3);

	if (copy1) {
	    System.arraycopy(results[0], off1, coords1, offset1, 30);
	}
	if (copy2) {
	    System.arraycopy(results[1], off2, coords2, offset2, 30);
	}
    }

    double[] tmpcoords = new double[48];
    double[] tmpcoords2 = new double[48];
    double[] tmpcoords3 = new double[48];

    private void getBezierPatchSub(double[]result, int offset,
				   double[]coords, int coffset,
				   int patchNumb)
    {
	if (coords != null) {
	    System.arraycopy(coords, coffset, tmpcoords, 0, 48);
	    for (int i = 0; i < 3; i++) {
		MatrixOps.reflect(tmpcoords2, 0, coords, coffset,
				  4, 4, true, i, 3, i, 3);
	    }
	}
	switch (patchNumb) {
	case 0:
	    splitLL(result, offset, tmpcoords);
	    splitRR(tmpcoords3, 0, tmpcoords2);
	    break;
	case 1:
	    splitRL(result, offset, tmpcoords);
	    splitLR(tmpcoords3, 0, tmpcoords2);
	    break;
	case 2:
	    splitLR(result, offset, tmpcoords);
	    splitRL(tmpcoords3, 0, tmpcoords2);
	    break;
	case 3:
	    splitRR(result, offset, tmpcoords);
	    splitLL(tmpcoords3, 0, tmpcoords2);
	    break;
	default:
	    throw new IllegalArgumentException("bad patch number");
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(tmpcoords3, 0, tmpcoords3, 0,
			      4, 4, true, i, 3, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    result[offset+i] = (result[offset+i] + tmpcoords3[i]) / 2.0;
	}
    }

    /*
    private void getCubicVertexSub(double[]result, int offset,
				   double[]coords, int coffset,
				   int patchNumb)
    {
	if (coords != null) {
	    System.arraycopy(coords, coffset, tmpcoords, 0, 48);
	}

	switch (patchNumb) {
	case 0:
	    splitLL(result, offset, tmpcoords);
	    break;
	case 1:
	    splitRL(result, offset, tmpcoords);
	    break;
	case 2:
	    splitLR(result, offset, tmpcoords);
	    break;
	case 3:
	    splitRR(result, offset, tmpcoords);
	    break;
	default:
	    throw new IllegalArgumentException("bad patch number");
	}
    }
    */

    void getCubicVertex0(double[] results, int offset,
			 double[] coords, int coffset) {
	// left cubic patch
	Shape3DHelper.cubicVertexToPatch(coords, coffset, tmpcoords, 0);
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(tmpcoords3, 0, tmpcoords, 0,
			      4, 4, true, i, 3, i, 3);
	}
	getBottomPatch(tmpcoords2, 0, tmpcoords, 0);
	getLeftPatch(results, offset, tmpcoords2, 0);
	getTopPatch(tmpcoords2, 0, tmpcoords3, 0);
	getRightPatch(tmpcoords3, 0, tmpcoords2, 0);
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(tmpcoords3, 0, tmpcoords3, 0,
			      4, 4, true, i, 3, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    results[offset+i] = (results[offset+i] + tmpcoords3[i]) / 2.0;
	}
    }

    void getCubicVertex1(double[] results, int offset,
			 double[] coords, int coffset) {
	// right cubic patch
	Shape3DHelper.cubicVertexToPatch(coords, coffset, tmpcoords, 0);
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(tmpcoords3, 0, tmpcoords, 0,
			      4, 4, true, i, 3, i, 3);
	}
	getBottomPatch(tmpcoords2, 0, tmpcoords, 0);
	getRightPatch(results, offset, tmpcoords2, 0);
	getTopPatch(tmpcoords2, 0, tmpcoords3, 0);
	getLeftPatch(tmpcoords3, 0, tmpcoords2, 0);
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(tmpcoords3, 0, tmpcoords3, 0,
			      4, 4, true, i, 3, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    results[offset+i] = (results[offset+i] + tmpcoords3[i]) / 2.0;
	}
    }

    void getCubicVertex(double[] results, int offset,
			 double[] coords, int coffset, double[] tmp) {
	System.arraycopy(coords, coffset+36, results, offset, 12);
	System.arraycopy(tmp, 0, results, offset+12, 3);
    }



    /**
     * Get the left subpatch of a B&eacute;zier patch.
     * The array arguments must be long enough to store 48 entries
     * starting from their offsets. Left to right is in the direction
     * of an increasing U parameter (the first parameter).
     * @param result the coordinate array for a new B&eacute;zier patch
     *               that will subdivide an existing patch
     * @param offset the offset into the result array for the initial
     *        entry
     * @param coords the coordinate array for an existing patch.
     * @param coffset the offset for the first entry in the coords array
     * @exception IllegalArgumentException an array was null, an offset
     *            was illegal, or an array length was too short
     */
    public static void getLeftPatch(double[] result, int offset,
				    double[] coords, int coffset)
	throws IllegalArgumentException
    {
	if (result == null || offset < 0 || offset + 48 > result.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	if (coords == null || coffset < 0 || coffset + 48 > coords.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(result, 4, 4, offset,
			       SL, 4, 4, 0, coords, 4, 4, coffset,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    /**
     * Get the bottom subpatch of a B&eacute;zier patch.
     * The array arguments must be long enough to store 48 entries
     * starting from their offsets. Bottom to Top is in the direction
     * of an increasing V parameter (the second parameter).
     * @param result the coordinate array for a new B&eacute;zier patch
     *               that will subdivide an existing patch
     * @param offset the offset into the result array for the initial
     *        entry
     * @param coords the coordinate array for an existing patch.
     * @param coffset the offset for the first entry in the coords array
     * @exception IllegalArgumentException an array was null, an offest
     *            was illegal, or an array length was too short
     */
    public static void getBottomPatch(double[] result, int offset,
				      double[] coords, int coffset)
	throws IllegalArgumentException
    {
	if (result == null || offset < 0 || offset + 48 > result.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	if (coords == null || coffset < 0 || coffset + 48 > coords.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(result, 4, 4, offset,
			       coords, 4, 4, coffset, SLT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    /**
     * Get the top subpatch of a B&eacute;zier patch.
     * The array arguments must be long enough to store 48 entries
     * starting from their offsets. Bottom to Top is in the direction
     * of an increasing V parameter (the second parameter).
     * @param result the coordinate array for a new B&eacute;zier patch
     *               that will subdivide an existing patch
     * @param offset the offset into the result array for the initial
     *        entry
     * @param coords the coordinate array for an existing patch.
     * @param coffset the offset for the first entry in the coords array
     * @exception IllegalArgumentException an array was null, an offest
     *            was illegal, or an array length was too short
     */
    public static void getTopPatch(double[] result, int offset,
				   double[] coords, int coffset)
	throws IllegalArgumentException
    {
	if (result == null || offset < 0 || offset + 48 > result.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	if (coords == null || coffset < 0 || coffset + 48 > coords.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(result, 4, 4, offset,
			       coords, 4, 4, coffset, SRT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }


    private void splitLL(double[]result, int offset, double[]coords) {
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(ftmp, 4, 4, 0, coords, 4, 4, 0, SLT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	    MatrixOps.multiply(result, 4, 4, offset,
			       SL, 4, 4, 0, ftmp, 4, 4, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }
    private void splitLR(double[]result, int offset, double[]coords) {
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(ftmp, 4, 4, 0, coords, 4, 4, 0, SRT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	    MatrixOps.multiply(result, 4, 4, offset,
			       SL, 4, 4, 0, ftmp, 4, 4, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    private void splitRL(double[]result, int offset, double[]coords) {
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(ftmp, 4, 4, 0, coords, 4, 4, 0, SLT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	    MatrixOps.multiply(result, 4, 4, offset,
			       SR, 4, 4, 0, ftmp, 4, 4, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    /**
     * Get the right subpatch of a B&eacute;zier patch.
     * The array arguments must be long enough to store 48 entries
     * starting from their offsets. Left to right is in the direction
     * of an increasing U parameter (the first parameter).
     * @param result the coordinate array for a new B&eacute;zier patch
     *               that will subdivide an existing patch
     * @param offset the offset into the result array for the initial
     *        entry
     * @param coords the coordinate array for an existing patch.
     * @param coffset the offset for the first entry in the coords array
     * @exception IllegalArgumentException an array was null, an offest
     *            was illegal, or an array length was too short
     */
    public static void getRightPatch(double[] result, int offset,
				    double[] coords, int coffset)
	throws IllegalArgumentException
    {
	if (result == null || offset < 0 || offset + 48 > result.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}
	if (coords == null || coffset < 0 || coffset + 48 > coords.length) {
	    throw new IllegalArgumentException(errorMsg("argarray"));
	}

	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(result, 4, 4, offset,
			       SR, 4, 4, 0, coords, 4, 4, coffset,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    private void splitRR(double[]result, int offset, double[]coords) {
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(ftmp, 4, 4, 0, coords, 4, 4, 0, SRT, 4, 4, 0,
			       true, i, 3, i, 3, 0, 1);
	    MatrixOps.multiply(result, 4, 4, offset,
			       SR, 4, 4, 0, ftmp, 4, 4, 0,
			       true, i, 3, 0, 1, i, 3);
	}
	for (int i = 0; i < 48; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    private void getPlanarTriangleSub(double[] results, int offset,
				      double[] coords, int coffset,
				      int patchNumb)
    {
	if (coords != null) {
	    System.arraycopy(coords, coffset, tmpcoords, 0, 9);
	}
	for (int i = 0; i < 3; i++) {
	    switch(patchNumb) {
	    case 0:
		results[offset+i] = tmpcoords[i];
		/*
		results[offset+i+3] = (tmpcoords[i] + tmpcoords[3+i])/2.0;
		results[offset+i+6] = (tmpcoords[i] + tmpcoords[6+i])/2.0;
		*/
		results[offset+i+3] = midcoord(tmpcoords[i], tmpcoords[3+i]);
		results[offset+i+6] = midcoord(tmpcoords[i], tmpcoords[6+i]);
		break;
	    case 1:
		// results[offset+i] = (tmpcoords[i] + tmpcoords[3+i])/2.0;
		results[offset+i] = midcoord(tmpcoords[i], tmpcoords[3+i]);
		results[offset+i+3] = tmpcoords[i+3];
		// results[offset+i+6] = (tmpcoords[i+3] + tmpcoords[i+6])/2.0;
		results[offset+i+6] = midcoord(tmpcoords[i+3], tmpcoords[i+6]);
		break;
	    case 2:
		// results[offset+i] = (tmpcoords[i] + tmpcoords[6+i])/2.0;
		// results[offset+i+3] = (tmpcoords[i+3] + tmpcoords[6+i])/2.0;
		results[offset+i] = midcoord(tmpcoords[i], tmpcoords[6+i]);
		results[offset+i+3] = midcoord(tmpcoords[i+3], tmpcoords[6+i]);
		results[offset+i+6] = tmpcoords[i+6];
		break;
	    case 3:
		// results[offset+i] = (tmpcoords[i] + tmpcoords[3+i])/2.0;
		// results[offset+3+i] = (tmpcoords[i+3] + tmpcoords[6+i])/2.0;
		// results[offset+6+i] = (tmpcoords[i] + tmpcoords[6+i])/2.0;
		results[offset+i] = midcoord(tmpcoords[i], tmpcoords[3+i]);
		results[offset+3+i] = midcoord(tmpcoords[i+3], tmpcoords[6+i]);
		results[offset+6+i] = midcoord(tmpcoords[i], tmpcoords[6+i]);
		break;
	    default:
		throw new IllegalArgumentException("bad patch number");
	    }
	}
	for (int i = 0; i < 9; i++) {
	    int index = offset+i;
	    results[index] = /*(float)*/results[index];
	}
    }

    /*
   private void getBezierTriangleSub(double[]result, int offset,
				     double[]coords, int coffset,
				     int patchNumb)
    {
	if (coords != null) {
	    setupForCT(coords, coffset);
	}

	switch (patchNumb) {
	case 0:
	    genCT1(result, offset);
	    break;
	case 1:
	    genCT2(result, offset);
	    break;
	case 2:
	    genCT3(result, offset);
	    break;
	case 3:
	    genCT4(result, offset);
	    break;
	default:
	    throw new IllegalArgumentException("bad patch number");
	}
    }
    */

    double[] workspaceA1 = new double[12]; // u = 0
    double[] workspaceA2 = new double[12]; // v = 0
    double[] workspaceA3 = new double[12]; // w = 0
    double[] workspaceB1 = new double[12]; // u = 0
    double[] workspaceB2 = new double[12]; // v = 0
    double[] workspaceB3 = new double[12]; // w = 0

    double[] coords1 = new double[12]; // inner top
    double[] coords2 = new double[12]; // inner left
    double[] coords3 = new double[12]; // inner right
    double[] coords1r = new double[12];
    double[] coords2r = new double[12];
    double[] coords3r = new double[12];

    double xval1;
    double yval1;
    double zval1;
    double xval2;
    double yval2;
    double zval2;
    double xval3;
    double yval3;
    double zval3;
    double xval4;
    double yval4;
    double zval4;
    
    void genCT1(double[] result, int offset) {
	Surface3D.setupU0ForTriangle(workspaceA1, result, false, offset);
	Surface3D.setupV0ForTriangle(workspaceA2, result, false, offset);
	Surface3D.setupW0ForTriangle(coords2, result, false, offset);
	Surface3D.setupCP111ForTriangle(xval1, yval1, zval1, result, offset);
	for (int i = 0; i < 30; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    void genCT2(double[] result, int offset) {
	Surface3D.setupU0ForTriangle(coords3, result, false, offset);
	Surface3D.setupV0ForTriangle(workspaceB2, result, false, offset);
	Surface3D.setupW0ForTriangle(workspaceA3, result, false, offset);
	Surface3D.setupCP111ForTriangle(xval2, yval2, zval2, result, offset);
	for (int i = 0; i < 30; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    void genCT3(double[] result, int offset) {
	Surface3D.setupU0ForTriangle(workspaceB1, result, false, offset);
	Surface3D.setupV0ForTriangle(coords1, result, false, offset);
	Surface3D.setupW0ForTriangle(workspaceB3, result, false, offset);
	Surface3D.setupCP111ForTriangle(xval3, yval3, zval3, result, offset);
	for (int i = 0; i < 30; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }

    void genCT4(double[] result, int offset) {
	Surface3D.setupU0ForTriangle(coords1, result, false, offset);
	Surface3D.setupV0ForTriangle(coords2r, result, false, offset);
	Surface3D.setupW0ForTriangle(coords3, result, false, offset);
	Surface3D.setupCP111ForTriangle(xval4, yval4, zval4, result, offset);
	for (int i = 0; i < 30; i++) {
	    int index = offset+i;
	    result[index] = /*(float)*/result[index];
	}
    }


    /*
    double[] cbtmp = new double[12];

    void cubicToBezier(double[] coords) {
	for (int i = 0; i < coords.length; i += 3) {
	    cbtmp[i] = coords[i]/Binomial.C(3,i);
	    cbtmp[i+1] = coords[i+1]/Binomial.C(3,i);
	    cbtmp[i+2] = coords[i+2]/Binomial.C(3,i);
	}
	coords[0] = cbtmp[0];
	coords[1] = cbtmp[1];
	coords[2] = cbtmp[2];
	int len = coords.length-3;
	int ind = 1;
	while (len > 6) {
	    for (int i = 0; i < len; i++) {
		cbtmp[i] += cbtmp[i+3];
		cbtmp[i+1] += cbtmp[i+4];
		cbtmp[i+2] += cbtmp[i+5];
	    }
	    coords[ind++] = cbtmp[0];
	    coords[ind++] = cbtmp[1];
	    coords[ind++] = cbtmp[2];
	    len--;
	}
    }
    */

    void splitCubicPath(double[] workspace, double[] workspaceA,
			double[] workspaceB)
    {
	double[][] split = splitCubicBezierCurve(workspace);
	System.arraycopy(split[0], 0, workspaceA, 0, 12);
	System.arraycopy(split[1], 0, workspaceB, 0, 12);
    }

    /*
    double[][][] ttmp = new double[3][3][3];

    void splitCubicPath(double[] workspace,
			double[] workspaceA, double[] workspaceB) {

	// based on split method in FlatteningPathIterator3D
	for (int i = 1; i < 4; i++) {
	    int im1 = i - 1;
	    for (int j = 0; j < 3; j++) {
		int k = 3*i+j;
		ttmp[0][im1][j] = (workspace[k-3] + 
				  workspace[k])/2.0;
	    }
	}
	for (int i = 1; i < 3; i++) {
	    int im1 = i - 1;
	    for (int j = 0; j < 3; j++) {
		int k = 3*i+j;
		ttmp[1][im1][j] = (ttmp[0][im1][j] + ttmp[0][i][j])/2.0;
	    }
	}
	for (int j = 0; j < 3; j++) {
	    ttmp[2][0][j] = (ttmp[1][0][j] + ttmp[1][1][j])/2.0;
	}
	if (workspace == workspaceA) {
	    for (int j = 0; j < 3; j++) {
		workspaceB[j] = ttmp[2][0][j];
		workspaceB[3+j] = ttmp[1][1][j];
		workspaceB[6+j] = ttmp[0][2][j];
		workspaceB[9+j] = workspace[9+j];
		workspaceA[3+j] = ttmp[0][0][j];
		workspaceA[6+j] = ttmp[1][0][j];
		workspaceA[9+j] = ttmp[2][0][j];
	    }
	} else if (workspace == workspaceB) {
	    for (int j = 0; j < 3; j++) {
		workspaceA[j] = workspace[j];
		workspaceA[3+j] = ttmp[0][0][j];
		workspaceA[6+j] = ttmp[1][0][j];
		workspaceA[9+j] = ttmp[2][0][j];
		workspaceB[j] = ttmp[2][0][j];
		workspaceB[3+j] = ttmp[1][1][j];
		workspaceB[6+j] = ttmp[0][2][j];
	    }
	} else {
	    for (int j = 0; j < 3; j++) {
		workspaceA[j] = workspace[j];
		workspaceA[3+j] = ttmp[0][0][j];
		workspaceA[+6+j] = ttmp[1][0][j];
		workspaceA[+9+j] = ttmp[2][0][j];
		workspaceB[j] = ttmp[2][0][j];
		workspaceB[3+j] = ttmp[1][1][j];
		workspaceB[6+j] = ttmp[0][2][j];
		workspaceB[9+j] = workspace[9+j];
	    }
	}
    }
    */

    /*
    static final double a11 = Functions.B(1,3, 1.0/3);
    static final double a12 = Functions.B(2, 3, 1.0/3);
    static final double a21 = Functions.B(1, 3, 2.0/3);
    static final double a22 = Functions.B(2, 3, 2.0/3);
    static final double det = a11*a22 - a21*a12;
    static final double ia11 = a22/det;
    static final double ia12 = -a12/det;
    static final double ia21 = -a21/det;
    static final double ia22 = a11/det;

    private void setupForCT(double[] coords, int coffset) {
	System.arraycopy(coords, coffset, workspaceA1, 0, 12);
	for (int i = 0; i < 3; i++) {
	    int ii = i + coffset;
	    workspaceA2[i] = coords[ii];
	    workspaceA2[3+i] = coords[12+ii];
	    workspaceA2[6+i] = coords[21+ii];
	    workspaceA2[9+i] = coords[27+ii];
	    workspaceA3[i] = coords[27+ii];
	    workspaceA3[3+i] = coords[24+ii];
	    workspaceA3[6+i] = coords[18+ii];
	    workspaceA3[9+i] = coords[9+ii];
	}

	splitCubicPath(workspaceA1, workspaceA1, workspaceB1);
	splitCubicPath(workspaceA2, workspaceA2, workspaceB2);
	splitCubicPath(workspaceA3, workspaceA3, workspaceB3);
	

	double[] val1 = new double[3];
	double[] val2 = new double[3];
	double[] tmp = new double[3];
	
	// horizontal line of inner triangle
	Arrays.fill(coords1, 3, 9, 0.0);
	Functions.Bernstein.sumB(val1, coords, coffset, 3, 1.0/6, 0.5, 1.0/3);
	Functions.Bernstein.sumB(val2, coords, coffset, 3, 1.0/3, 0.5, 1.0/6);
	System.arraycopy(workspaceB1, 0, coords1, 0, 3);
	System.arraycopy(workspaceB3, 0, coords1, 9, 3);
	Functions.Bernstein.sumB(tmp, coords1, 3, 1.0/3.0);
	VectorOps.sub(val1, val1, tmp);
	Functions.Bernstein.sumB(tmp, coords1, 3, 2.0/3.0);
	VectorOps.sub(val2, val2, tmp);

	for (int i = 0; i < 3; i++) {
	    coords1[3+i] = ia11 * val1[i] + ia12*val2[i];
	    coords1[6+i] = ia21 * val1[i] + ia22*val2[i];
	}
	System.arraycopy(workspaceB3, 0, coords1r, 0, 3);
	System.arraycopy(workspaceB1, 0, coords1r, 9, 3);
	System.arraycopy(coords1, 3, coords1r, 6, 3);
	System.arraycopy(coords1, 6, coords1r, 3, 3);

	// left line of inner triangle
	Arrays.fill(coords2, 3, 9, 0.0);
	Functions.Bernstein.sumB(val1, coords, coffset, 3, 1.0/3, 1.0/6, 0.5);
	Functions.Bernstein.sumB(val2, coords, coffset, 3, 1.0/6, 1.0/3, 0.5);
	System.arraycopy(workspaceB2, 0, coords2, 0, 3);
	System.arraycopy(workspaceB1, 0, coords2, 9, 3);
	Functions.Bernstein.sumB(tmp, coords2, 3, 1.0/3.0);
	VectorOps.sub(val1, val1, tmp);
	Functions.Bernstein.sumB(tmp, coords2, 3, 2.0/3.0);
	VectorOps.sub(val2, val2, tmp);

	for (int i = 0; i < 3; i++) {
	    coords2[3+i] = ia11 * val1[i] + ia12*val2[i];
	    coords2[6+i] = ia21 * val1[i] + ia22*val2[i];
	}
	System.arraycopy(workspaceB1, 0, coords2r, 0, 3);
	System.arraycopy(workspaceB2, 0, coords2r, 9, 3);
	System.arraycopy(coords2, 3, coords2r, 6, 3);
	System.arraycopy(coords2, 6, coords2r, 3, 3);

	// right line of inner triangle
	Arrays.fill(coords3, 3, 9, 0.0);
	Functions.Bernstein.sumB(val1, coords, coffset, 3, 0.5, 1.0/6, 1.0/3);
	Functions.Bernstein.sumB(val2, coords, coffset, 3, 0.5, 1.0/3, 1.0/6);
	System.arraycopy(workspaceB2, 0, coords3, 0, 3);
	System.arraycopy(workspaceB3, 0, coords3, 9, 3);
	Functions.Bernstein.sumB(tmp, coords3, 3, 1.0/3.0);
	VectorOps.sub(val1, val1, tmp);
	Functions.Bernstein.sumB(tmp, coords3, 3, 2.0/3.0);
	VectorOps.sub(val2, val2, tmp);

	for (int i = 0; i < 3; i++) {
	    coords3[3+i] = ia11 * val1[i] + ia12*val2[i];
	    coords3[6+i] = ia21 * val1[i] + ia22*val2[i];
	}
	System.arraycopy(workspaceB3, 0, coords3r, 0, 3);
	System.arraycopy(workspaceB2, 0, coords3r, 9, 3);
	System.arraycopy(coords3, 3, coords3r, 6, 3);
	System.arraycopy(coords3, 6, coords3r, 3, 3);


	// setup xval1, etc.
	double u = 1.0/6;
	double v = 1.0/6;
	Functions.Bernstein.sumB(tmp, coords, coffset, 3, u, v, 1.0-(u+v));
	xval1 = tmp[0];
	yval1 = tmp[1];
	zval1 = tmp[2];

	u = 4.0/6.0;
	v = 1.0/6.0;
	Functions.Bernstein.sumB(tmp, coords, coffset, 3, u, v, 1.0-(u+v));
	xval2 = tmp[0];
	yval2 = tmp[1];
	zval2 = tmp[2];

	u = 1.0/6.0;
	v = 4.0/6.0;
	Functions.Bernstein.sumB(tmp, coords, coffset, 3, u, v, 1.0-(u+v));
	xval3 = tmp[0];
	yval3 = tmp[1];
	zval3 = tmp[2];

	u = 2.0/6.0;
	v = 2.0/6.0;
	Functions.Bernstein.sumB(tmp, coords, coffset, 3, u, v, 1.0-(u+v));
	xval4 = tmp[0];
	yval4 = tmp[1];
	zval4 = tmp[2];
    }
    */
}

//  LocalWords:  exbundle STL src tmp Bezier Hongxin Zhang Jieqing SL
//  LocalWords:  Feng subpatch eacute zier coords coffset genQ
//  LocalWords:  IllegalArgumentException argarray setupP patchNumb
//  LocalWords:  FlatteningPathIterator arraycopy coffset xval
