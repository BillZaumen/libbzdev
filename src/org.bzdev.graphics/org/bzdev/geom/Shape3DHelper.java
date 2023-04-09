package org.bzdev.geom;
import org.bzdev.math.*;

//@exbundle org.bzdev.geom.lpack.Geom


// The interfaces Shape3D and its subclass SurfaceOps has some static
// methods and this class helps with the implementation.

class Shape3DHelper {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    private static final int MIN_PARALLEL_SIZE_CM =
	SurfaceConstants.MIN_PARALLEL_SIZE_CM;
    private static final int MIN_PARALLEL_SIZE_M =
	SurfaceConstants.MIN_PARALLEL_SIZE_M;

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
     * @param size an estimate of the number of segments is the shape
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
     *        | &int;x<sup>2</sub>/v dV  &int;xy/v dV  &int;xz/v dV |
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

    /**
     * Get a point at a specific location on a segment of a surface.
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
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
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
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
     * @param type the segment type ({@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
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
     * @param type the segment type ({@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#PLANAR_TRIANGLE}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
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
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
     * @param coords the control-point array for the segment
     * @param u the first parameter for the surface
     * @param v the second parameter for the surface
     */
    public static void uTangent(double[] results, int type,
				double[] coords, double u, double v) {
	double x, y, z;
	if (type == SurfaceIterator.CUBIC_PATCH) {
	    /*
	    double[] coords2 = new double[12];
	    Functions.Bernstein.dsumBdx(coords2, 0, 3, coords, 0, 3, u);
	    Functions.Bernstein.dsumBdx(coords2, 3, 3, coords, 4, 3, u);
	    Functions.Bernstein.dsumBdx(coords2, 6, 3, coords, 8, 3, u);
	    Functions.Bernstein.dsumBdx(coords2, 9, 3, coords, 12, 3, u);
	    Functions.Bernstein.sumB(results, coords2, 3, v);
	    */
	    if (v == 0) {
		Functions.Bernstein.dsumBdx(results, 0, 3, coords, 0, 3, u);
		/*
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(results[i] - old[i]) > 1.e-12) {
			System.out.println("old and result differ 1");
		    }
		}
		*/
	    } else if (v == 1.0) {
		Functions.Bernstein.dsumBdx(results, 0, 3, coords, 12, 3, u);
		/*
		for (int i = 0; i < 3; i++) {
		    if (Math.abs(results[i] - old[i]) > 1.e-12) {
			System.out.println("old and result differ 2");
		    }
		}
		*/
	    } else {
		double[] coords2 = new double[12];
		Functions.Bernstein.dsumBdx(coords2, 0, 3, coords, 0, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 3, 3, coords, 4, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 6, 3, coords, 8, 3, u);
		Functions.Bernstein.dsumBdx(coords2, 9, 3, coords, 12, 3, u);
		Functions.Bernstein.sumB(results, coords2, 3, v);
	    }
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
     * @param type the segment type ({@link SurfaceIterator#PLANAR_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_TRIANGLE},
     *        {@link SurfaceIterator#CUBIC_PATCH}, or
     *        {@link SurfaceIterator#CUBIC_VERTEX})
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
	    /*
	    Functions.Bernstein.sumB(results, coords, 0, 3, u);
	    double val = Functions.dBdx(0, 3, v);
	    x = results[0]*val;
	    y = results[1]*val;
	    z = results[2]*val;
	    Functions.Bernstein.sumB(results, coords, 4, 3, u);
	    val = Functions.dBdx(1, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    Functions.Bernstein.sumB(results, coords, 8, 3, u);
	    val = Functions.dBdx(2, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    Functions.Bernstein.sumB(results, coords, 12, 3, u);
	    val = Functions.dBdx(3, 3, v);
	    x += results[0]*val;
	    y += results[1]*val;
	    z += results[2]*val;
	    results[0] = x; results[1] = y; results[2] = z;
	    */
	} else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
	    results[0] = coords[3] - coords[0];
	    results[1] = coords[4] - coords[1];
	    results[2] = coords[5] - coords[2];
	} else if (type == SurfaceIterator.CUBIC_VERTEX) {
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
	areaCTN = nCT;
	areaCPN = nCP;

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

    private static double[]tmptp1 = new double[12];
    private static double[]tmptp2 = new double[12];

    public static synchronized void
	cubicVertexToPatch(double[] coords, int offset,
			   double[] pcoords, int poffset)
    {
	//  Convert a CUBIC_VERTEX segment to an equivlanet
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

    /**
     * Add the surface-integral contributions, used in computing a volume
     * for those segments associated with a surface iterator, to an Adder.
     * The surface iterator will be modified.  The total added to the
     * adder will be 3 times the contribution of the iterator's patches
     * to the volume of the shape it represents or partially represents.
     * (It is more accurate numerically to divide by 3 at the end as this
     * reduces the total number of arithmetic operations).
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
     * @param refPoint a reference point; null for a globally defined
     *        default
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
}
