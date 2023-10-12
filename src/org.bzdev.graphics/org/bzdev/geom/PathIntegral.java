package org.bzdev.geom;

import java.awt.*;
import java.awt.geom.*;

import org.bzdev.math.RealValuedFunctTwoOps;
import org.bzdev.math.RealValuedFunctThreeOps;
import org.bzdev.math.Adder;
import org.bzdev.math.GLQuadrature;
    
//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Class to compute a path integral of a vector or scalar field.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * An instance of this class is created by providing the vector or
 * scalar fields and can then be used to find the path integral for a
 * given path.
 * <P>
 * The implementation uses Gauss-Legendre quadrature on each
 * segment of a path.
 */
public class PathIntegral {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static abstract class GLQ extends GLQuadrature<Path2DInfo.SegmentData> {
	double[] us;
	public GLQ(int n, double[] us) {
	    super(n);
	    this.us = us;
	}
	double integrate(double x0, double y0, int st, double[] coords) {
	    // this.x0 = x0;
	    // this.y0 = y0;
	    // this.st = st;
	    // this.coords = coords;
	    // us contains integers from 0 to n, each stored as a double.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(us,
				  new Path2DInfo.SegmentData(st, x0, y0, coords,
							     null));
	}
    }

    GLQ glq = null;

    // line integral of a scalar field
    static class GLQS extends GLQ {
	Path2DInfo.UValues[] uvs;
	RealValuedFunctTwoOps sf;
	
	public GLQS(int n, double[] us, Path2DInfo.UValues[] uvs,
		    RealValuedFunctTwoOps sf)
	{
	    super(n, us);
	    this.uvs = uvs;
	    this.sf = sf;
	}

	@Override
	protected double function(double iu, Path2DInfo.SegmentData data) {
	    Path2DInfo.UValues uv = uvs[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double dsdu = data.dsDu(uv);
	    return sf.valueAt(x, y)*dsdu;
	}
    }

    // line integral of a vector field
    static class GLQV extends GLQ {
	Path2DInfo.UValues[] uvs;
	RealValuedFunctTwoOps xf;
	RealValuedFunctTwoOps yf;
	
	public GLQV(int n, double[] us, Path2DInfo.UValues[] uvs,
		    RealValuedFunctTwoOps xf, RealValuedFunctTwoOps yf)
	{
	    super(n, us);
	    this.uvs = uvs;
	    this.xf = xf;
	    this.yf = yf;
	}

	@Override
	protected double function(double iu, Path2DInfo.SegmentData data) {
	    Path2DInfo.UValues uv = uvs[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    /*
	    double x = getX(u, x0, y0, st, coords);
	    double y = getY(u, x0, y0, st, coords);
	    double dxdu = dxDu(u, x0, y0, st, coords);
	    double dydu = dyDu(u, x0, y0, st, coords);
	    */
	    if (xf == null) {
		return yf.valueAt(x, y)*dydu;
	    } else if (yf == null) {
		return xf.valueAt(x, y)*dxdu;
	    } else {
		return xf.valueAt(x, y)*dxdu + yf.valueAt(x, y)*dydu;
	    }
	}
    }

    static int glqlenS(int m) {
	/*
	 * x and y are cubic polynomials of the path parameter u in the
	 * worst case.
	 */
	m *= 3;
	/*
	 * For dxdu and dydu, the worst case is a cubic B&eacute;zier curve,
	 * but the derivatives have a degree one less.  The derivative
	 * dsDu, however is Math.sqrt(dxDu*dxDu + dyDu*dyDu). Without
	 * the square root, we would have to add 4 to the argument.
	 * To be conservative about the square root, we add 30 instead.
	 * Then we add 1 and if the value is odd, we round up to the
	 * nearest even number.  Finally we divide by 2 to get the
	 * degree we need.
	 */
	m += 30;
	if (m % 2 == 1) m++;
	return m/2;
    }

    /**
     * Constructor for the line integral of a scalar field.
     * The value of the integral for a path P is
     * $\int f(x,y) ds = \int f(x,y) \frac{ds}{du} du$,
     * <!-- &int;f(x,y) ds = &int;f(x,y)(ds/du)du, --> where u is the path
     * parameter for P, and where x and y are values on the path
     * corresponding to each value of the path parameter u.
     * <P>
     * If p(x,y) is a polynomial approximation to sf, then
     * the corresponding value for n is the degree of the polynomial
     *  p(at+b,ct+d) where t is the independent variable.
     * @param n the degree of a polynomial that is an adequate
     *        approximation to the function sf(x,y) 
     * @param sf a function of x and y providing the value of
     *        the scalar field at coordinates (x, y).
     */
    public PathIntegral(int n, RealValuedFunctTwoOps sf) {
	n = glqlenS(n);
	double[] us = GLQuadrature.getArguments(0.0, 1.0, n);
	Path2DInfo.UValues[] uvs = new Path2DInfo.UValues[n];
	for (int i = 0; i < n; i++) {
	    uvs[i] = new Path2DInfo.UValues(us[i]);
	    us[i] = (double) i;
	}
	glq = new GLQS(n, us, uvs, sf);
   }

    static int glqlenV(int m) {
	/*
	 * x and y are cubic polynomials of the path parameter u in the
	 * worst case.
	 */
	m *= 3;
	/*
	 * For dxdu and dydu, the worst case is a cubic B&eacute;zier curve,
	 * but the derivatives have a degree one less.  We require
	 * that 2n-1 >= m so we add 2 to m and then add 1 if the
	 * number we got is odd.
	 */
	m += 3;
	if (m % 2 == 1) m++;
	return m/2;
    }

    /*
     * vectorMode is true when the field is a vector field. In this
     * case, the orientation of the path affects the sign of the
     * integral and for that case, we have to correct the value
     * when the argument is a shape so that the outer boundary is
     * effectively traversed in a counterclockwise direction.
     */
    boolean vectorMode = false;

    /**
     * Constructor for the line integral of a vector field.
     * The value of the integral for a path P is
     * $\int (F_x(x,y) \frac{dx}{du} + F_y(x,y) \frac{dy}{du}) du$,
     * <!-- &int;(F<sub>x</sub>(x,y)(dx/du) + F<sub>y</sub>(x,y)(dy/du)) du,-->
     * where the limits of integration are those appropriate for
     * covering a path whose path parameter is u, and where the values
     * of x and y used in the integration are those corresponding to
     * the path parameter.
     * <P>
     * If p(x,y) is a polynomial approximation to xf or yf, then
     * the corresponding value for n is the degree of the polynomial
     * p(at+b,ct+d) where t is the independent variable. The
     * polynomial approximation applies to each segment of the
     * path, where a path segment is determined by the path's
     * path iterator.
     * @param n the degree of a polynomial that is an adequate
     *        approximation to the functions xf(x,y) and yf(x,y)
     * @param xf a function of x and y providing the value of
     *        the X component of a vector field at (x, y)
     * @param yf a function of x and y providing the value of
     *        the Y component of a vector field at (x, y)
     */
    public PathIntegral(int n,
			RealValuedFunctTwoOps xf,
			RealValuedFunctTwoOps yf)
    {
	vectorMode = true;
	n = glqlenV(n);
	double[] us = GLQuadrature.getArguments(0.0, 1.0, n);
	Path2DInfo.UValues[] uvs = new Path2DInfo.UValues[n];
	for (int i = 0; i < n; i++) {
	    uvs[i] = new Path2DInfo.UValues(us[i]);
	    us[i] = (double) i;
	}
	glq = new GLQV(n, us, uvs, xf, yf);
    }

    static abstract class GLQ3D extends GLQuadrature<Path3DInfo.SegmentData> {
	double[] us;
	public GLQ3D(int n, double[] us) {
	    super(n);
	    this.us = us;
	}
	double integrate(double x0, double y0, double z0,
			 int st, double[] coords)
	{
	    // us contains integers from 0 to n, each stored as a double.
	    // These are rounded to an integer that is used to
	    // look up the UValues from an array for the corresponding
	    // values of u required for Gaussian Legendre quadrature.
	    // SegmentData objects have methods that compute x(u), y(u)
	    // and their derivatives from a UValues object, which has
	    // terms precomputed for efficiency.
	    return integrateWithP(us,
				  new Path3DInfo.SegmentData(st, x0, y0, z0,
							     coords,
							     null));
	}
    }

    GLQ3D glq3D = null;

    // line integral of a scalar field
    static class GLQS3D extends GLQ3D {
	Path3DInfo.UValues[] uvs;
	RealValuedFunctThreeOps sf;
	
	public GLQS3D(int n, double[] us, Path3DInfo.UValues[] uvs,
		    RealValuedFunctThreeOps sf)
	{
	    super(n, us);
	    this.uvs = uvs;
	    this.sf = sf;
	}

	@Override
	protected double function(double iu, Path3DInfo.SegmentData data) {
	    Path3DInfo.UValues uv = uvs[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double z = data.getZ(uv);
	    double dsdu = data.dsDu(uv);
	    return sf.valueAt(x, y, z)*dsdu;
	}
    }

    // line integral of a vector field
    static class GLQV3D extends GLQ3D {
	Path3DInfo.UValues[] uvs;
	RealValuedFunctThreeOps xf;
	RealValuedFunctThreeOps yf;
	RealValuedFunctThreeOps zf;
	
	public GLQV3D(int n, double[] us, Path3DInfo.UValues[] uvs,
		    RealValuedFunctThreeOps xf,
		    RealValuedFunctThreeOps yf,
		    RealValuedFunctThreeOps zf)
	{
	    super(n, us);
	    this.uvs = uvs;
	    this.xf = xf;
	    this.yf = yf;
	    this.zf = zf;
	}

	@Override
	protected double function(double iu, Path3DInfo.SegmentData data) {
	    Path3DInfo.UValues uv = uvs[(int)Math.round(iu)];
	    double x = data.getX(uv);
	    double y = data.getY(uv);
	    double z = data.getZ(uv);
	    double dxdu = data.dxDu(uv);
	    double dydu = data.dyDu(uv);
	    double dzdu = data.dzDu(uv);
	    double result = 0.0;
	    if (xf != null) {
		result += xf.valueAt(x, y, z)*dxdu;
	    }
	    if (yf != null) {
		result += yf.valueAt(x, y, z)*dydu;
	    }
	    if (zf != null) {
		result +=  zf.valueAt(x, y, z)*dzdu;
	    }
	    return result;
	}
    }

    static int glqlenS3D(int m) {
	/*
	 * x and y are cubic polynomials of the path parameter u in the
	 * worst case.
	 */
	m *= 3;
	/*
	 * For dxdu and dydu, the worst case is a cubic B&eacute;zier curve,
	 * but the derivatives have a degree one less.  The derivative
	 * dsDu, however is Math.sqrt(dxDu*dxDu + dyDu*dyDu). Without
	 * the square root, we would have to add 4 to the argument.
	 * To be conservative about the square root, we add 30 instead.
	 * Then we add 1 and if the value is odd, we round up to the
	 * nearest even number.  Finally we divide by 2 to get the
	 * degree we need.
	 */
	m += 30;
	if (m % 2 == 1) m++;
	return m/2;
    }


    /**
     * Constructor for the 3D line integral of a scalar field.
     * The value of the integral for a path P is
     * $\int f(x,y) ds = \int f(x,y) \frac{ds}{du} du
     * = \int f(x,y) \sqrt{(\frac{dx}{du})^2 + (\frac{dy}{du})^2} du$,
     * <!-- &int;f(x,y) ds = &int;f(x,y)(ds/du)du,-->
     * where u is the path parameter for P, and where x and y are
     * values on the path corresponding to each value of the path
     * parameter u.
     * <P>
     * If p(x,y,z) is a polynomial approximation to sf, then
     * the corresponding value for n is the degree of the polynomial
     *  p(at+b,ct+d,et+f) where t is the independent variable. The
     * polynomial approximation applies to each segment of the
     * path, where a path segment is determined by the path's
     * path iterator.
     * @param n the degree of a polynomial that is an adequate
     *        approximation to the function sf(x,y,z)
     * @param sf a function of x and y providing the value of
     *        the scalar field at coordinates (x, y, z).
     */
    public PathIntegral(int n, RealValuedFunctThreeOps sf) {
	n = glqlenS(n);
	double[] us = GLQuadrature.getArguments(0.0, 1.0, n);
	Path3DInfo.UValues[] uvs = new Path3DInfo.UValues[n];
	for (int i = 0; i < n; i++) {
	    uvs[i] = new Path3DInfo.UValues(us[i]);
	    us[i] = (double) i;
	}
	glq3D = new GLQS3D(n, us, uvs, sf);
   }

    static int glqlenV3D(int m) {
	/*
	 * x and y are cubic polynomials of the path parameter u in the
	 * worst case.
	 */
	m *= 3;
	/*
	 * For dxdu and dydu, the worst case is a cubic B&eacute;zier curve,
	 * but the derivatives have a degree one less.  We require
	 * that 2n-1 >= m so we add 2 to m and then add 1 if the
	 * number we got is odd.
	 */
	m += 3;
	if (m % 2 == 1) m++;
	return m/2;
    }

    /**
     * Constructor for the 3D line integral of a vector field.
     * The value of the integral for a path P is
     * $\int (F_x(x,y)\frac{dx}{du} + F_y(x,y)\frac{dy}{du}) du$,
     * <!-- &int;(F<sub>x</sub>(x,y)(dx/du) + F<sub>y</sub>(x,y)(dy/du)) du,-->
     * where the limits of integration are those appropriate for
     * covering a path whose path parameter is u, and where the values
     * of x and y used in the integration are those corresponding to
     * the path parameter.
     * <P>
     * If p(x,y,z) is a polynomial approximation to xf or yf, then
     * the corresponding value for n is the degree of the polynomial
     *  p(at+b,ct+d,et+f) where t is the independent variable. The
     * polynomial approximation applies to each segment of the
     * path, where a path segment is determined by the path's
     * path iterator.
     * @param n the degree of a polynomial that is an adequate
     *        approximation to the functions xf(x,y,z), yf(x,y,z),
     *        and zf(x,y,z)
     * @param xf a function of x and y providing the value of
     *        the X component of a vector field at (x, y, z)
     * @param yf a function of x and y providing the value of
     *        the Y component of a vector field at (x, y, z)
     * @param zf a function of x and y providing the value of
     *        the Z component of a vector field at (x, y, z)
     */
    public PathIntegral(int n,
			RealValuedFunctThreeOps xf,
			RealValuedFunctThreeOps yf,
			RealValuedFunctThreeOps zf)
    {
	vectorMode = true;
	n = glqlenV(n);
	double[] us = GLQuadrature.getArguments(0.0, 1.0, n);
	Path3DInfo.UValues[] uvs = new Path3DInfo.UValues[n];
	for (int i = 0; i < n; i++) {
	    uvs[i] = new Path3DInfo.UValues(us[i]);
	    us[i] = (double) i;
	}
	glq3D = new GLQV3D(n, us, uvs, xf, yf,zf);
    }


    /**
     * Compute the integral over a path specified by a path iterator.
     * @param pi the path iterator
     * @return the path (or line) integral
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 2D operations
     */
    public double integrate(PathIterator pi)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (glq == null) throw new UnsupportedOperationException();
	double[] coords = new double[6];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
	    }
	    if (st != PathIterator.SEG_MOVETO) {
		adder.add(glq.integrate(lastX, lastY, st, coords));
	    }
	    switch (st) {
	    case PathIterator.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		// fullEntry = (i > 0);
		break;
	    case PathIterator.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_QUADTO:
		lastX = coords[2];
		lastY = coords[3];
		// fullEntry = true;
		break;
	    case PathIterator.SEG_CUBICTO:
		lastX = coords[4];
		lastY = coords[5];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    coords[0] = lastMoveToX;
	    coords[1] = lastMoveToY;
	    adder.add(glq.integrate(lastX, lastY, PathIterator.SEG_LINETO,
				    coords));
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Perform a line integral over the boundary of shape.
     * A shape will be converted to an Area if it is not already an
     * instance of Area so that the outer boundaries of the shape will
     * be in the counterclockwise direction and the inner boundaries
     * in the clockwise direction. The results will be those for the
     * case where the outer boundary is counterclockwise.
     * <P>
     * The term 'counterclockwise' is defined as the direction of 
     * rotation from the positive X axis towards the positive Y axis.
     * This reflects the convention in mathematics where the X axis
     * points right and the Y axis points up, as opposed to the Java
     * convention where the X axis points right and the Y axis points
     * down.
     * @param shape the shape
     * @return the path integral over the boundary of a shape
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 2D operations
     */
    public double integrate(Shape shape)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (glq == null) throw new UnsupportedOperationException();
	if (shape instanceof Area) {
	    double v = integrate(shape.getPathIterator(null));
	    if (vectorMode) {
		double area =
		    Path2DInfo.unsignedAreaOf(shape.getPathIterator(null));
		int sign = (area < 0.0)? -1: 1;
		if (v != 0.0) v = sign*v;
	    }
	    return v;
	} else {
	    return integrate(new Area(shape));
	}
    }

    /**
     * Perform a line integral over the boundary of shape modified by an
     * affine transform.
     * A shape will be converted to an Area if it is not already
     * an instance of Area so that inner and  outer boundaries have
     * opposite orientations.  The results will be those for the
     * case where the outer boundary is counterclockwise.
     * <P>
     * The term 'counterclockwise' is defined as the direction of 
     * rotation from the positive X axis towards the positive Y axis.
     * This reflects the convention in mathematics where the X axis
     * points right and the Y axis points up, as opposed to the Java
     * convention where the X axis points right and the Y axis points
     * down.
     * @param shape the shape
     * @param af the affine transform; null for the identity transform
     * @return the path integral over the boundary of a shape modified
     *         by an affine transform
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 2D operations
     */
    public double integrate(Shape shape, AffineTransform af)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (glq == null) throw new UnsupportedOperationException();
	if (shape instanceof Area) {
	    double v = integrate(shape.getPathIterator(af));
	    if (vectorMode) {
		double area =
		    Path2DInfo.unsignedAreaOf(shape.getPathIterator(af));
		int sign = (area < 0.0)? -1: 1;
		if (v != 0.0) v = sign * v;
	    }
	    return v;
	} else {
	    return integrate(new Area(shape), af);
	}
    }

    /**
     * Compute the integral over a path specified by a path iterator.
     * @param pi the path iterator
     * @return the path (or line) integral
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator3D#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 3D   operations
     */
    public double integrate(PathIterator3D pi)
	throws IllegalArgumentException, UnsupportedOperationException
    {
	if (glq3D == null) {
	    throw new UnsupportedOperationException();
	}
	double[] coords = new double[9];
	if (pi.isDone()) return 0.0;
	int st = pi.currentSegment(coords);
	if (st != PathIterator3D.SEG_MOVETO) {
	    throw new IllegalArgumentException(errorMsg("illFormedPath"));
	}
	// double sum = 0.0;
	Adder adder = new Adder.Kahan();
	double lastX = coords[0];
	double lastY = coords[1];
	double lastZ = coords[2];
	double lastMoveToX = lastX;
	double lastMoveToY = lastY;
	double lastMoveToZ = lastZ;
	boolean noFinalClose = true; // test for closed path
	while (!pi.isDone()) {
	    st = pi.currentSegment(coords);
	    if (st == PathIterator3D.SEG_CLOSE) {
		noFinalClose = false;
		coords[0] = lastMoveToX;
		coords[1] = lastMoveToY;
		coords[2] = lastMoveToZ;
	    }
	    if (st != PathIterator3D.SEG_MOVETO) {
		adder.add(glq3D.integrate(lastX, lastY, lastZ, st, coords));
	    }
	    switch (st) {
	    case PathIterator3D.SEG_CLOSE:
		lastX = lastMoveToX;
		lastY = lastMoveToY;
		lastZ = lastMoveToZ;
		/*
		  coords[0] = lastMoveToX;
		  coords[1] = lastMoveToY;
		  lastX = coords[0];
		  lastY = coords[1];
		*/
		// fullEntry = true;
		break;
	    case PathIterator3D.SEG_MOVETO:
		noFinalClose = true;
		lastX = coords[0];
		lastY = coords[1];
		lastZ = coords[2];
		lastMoveToX = lastX;
		lastMoveToY = lastY;
		lastMoveToZ = lastZ;
		// fullEntry = (i > 0);
		break;
	    case PathIterator3D.SEG_LINETO:
		lastX = coords[0];
		lastY = coords[1];
		lastZ = coords[2];
		// fullEntry = true;
		break;
	    case PathIterator3D.SEG_QUADTO:
		lastX = coords[3];
		lastY = coords[4];
		lastZ = coords[5];
		// fullEntry = true;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		lastX = coords[6];
		lastY = coords[7];
		lastZ = coords[8];
		// fullEntry = true;
		break;
	    default:
		throw new IllegalArgumentException(errorMsg("piUnknown"));
	    }
	    pi.next();
	}
	if (noFinalClose) {
	    // We implicitly close the path with a straight line segment.
	    coords[0] = lastMoveToX;
	    coords[1] = lastMoveToY;
	    coords[2] = lastMoveToZ;
	    adder.add(glq.integrate(lastX, lastY, PathIterator3D.SEG_LINETO,
				    coords));
	}
	// return sum;
	return adder.getSum();
    }

    /**
     * Perform a line integral over a path.
     * @param path the path
     * @return the path (or line) integral
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 3D operations
     */
    public double integrate(Path3D path)
	throws IllegalArgumentException, UnsupportedOperationException	
    {
	if (glq3D == null) {
	    throw new UnsupportedOperationException();
	}
	return integrate(path.getPathIterator(null));
    }
    /**
     * Perform a line integral over a path modified by an
     * affine transform.
     * @param path the path
     * @param af the affine transform; null for the identity transform
     * @return the path (or line) integral
     * @throws IllegalArgumentException the path is not a valid path
     *         (it must start with a {@link PathIterator3D#SEG_MOVETO}
     *         segment)
     * @throws UnsupportedOperationException this instance does not
     *         support 3D operations
     */
    public double integrate(Path3D path, AffineTransform3D af )
	throws IllegalArgumentException, UnsupportedOperationException	
    {
	if (glq3D == null) {
	    throw new UnsupportedOperationException();
	}
	return integrate(path.getPathIterator(af));
    }
}

//  LocalWords:  coords UValues SegmentData precomputed getX getY ds
//  LocalWords:  dxdu dxDu dydu dyDu dsDu sqrt du vectorMode dx dy xf
//  LocalWords:  yf et zf PathIterator SEG MOVETO illFormedPath lastX
//  LocalWords:  lastMoveToX lastMoveToY lastY fullEntry piUnknown af
//  LocalWords:  affine
