package org.bzdev.geom;
import org.bzdev.math.RealValuedFunctionThree;
import org.bzdev.math.RealValuedFunctThreeOps;
import org.bzdev.util.Cloner;
import org.bzdev.lang.UnexpectedExceptionError;

/**
 * Class to create a Transform3D based on real-valued functions of
 * three arguments.
 */
public class RVFTransform3D implements Transform3D, Cloneable {

    RealValuedFunctionThree xfunct;
    RealValuedFunctionThree yfunct;
    RealValuedFunctionThree zfunct;

    /**
     * Constructor.
     * For this transform to successfully return the affine transform that
     * approximates this transform at the point (x, y, z), the functions
     * xfunct, yfunct, and zfunct must be able to evaluate their first
     * derivatives.  If these derivatives are not implemented, an
     * {@link UnsupportedOperationException} will be thrown when
     * {@link #affineTransform(double,double,double)} is called.
     * @param xfunct the real-valued function providing the X coordinate
     *        of a transformed point given the original point's X, Y, and Z
     *        coordinates.
     * @param yfunct the real-valued function providing the Y coordinate
     *        of a transformed point given the original point's X, Y, and Z
     *        coordinates.
     * @param zfunct the real-valued function providing the Z coordinate
     *        of a transformed point given the original point's X, Y, and Z
     *        coordinates.
     */
    public RVFTransform3D(RealValuedFunctThreeOps xfunct,
			  RealValuedFunctThreeOps yfunct,
			  RealValuedFunctThreeOps zfunct) {
	if (xfunct == null) {
	    throw new IllegalArgumentException();
	} else if (xfunct instanceof RealValuedFunctionThree) {
	    this.xfunct = (RealValuedFunctionThree) xfunct;
	} else {
	    this.xfunct = new RealValuedFunctionThree(xfunct) ;
	}
	if (yfunct == null) {
	    throw new IllegalArgumentException();
	} else if (yfunct instanceof RealValuedFunctionThree) {
	    this.yfunct = (RealValuedFunctionThree) yfunct;
	} else {
	    this.yfunct = new RealValuedFunctionThree(yfunct) ;
	}
	if (zfunct == null) {
	    throw new IllegalArgumentException();
	} else if (zfunct instanceof RealValuedFunctionThree) {
	    this.zfunct = (RealValuedFunctionThree) zfunct;
	} else {
	    this.zfunct = new RealValuedFunctionThree(zfunct) ;
	}
    }
			  
    @Override
    public void transform(double[] srcPts, int srcOff,
			  double[] dstPts, int dstOff, int numPts) {

	double[] tmp = null;
	int saveOff = dstOff;
	if (srcPts == dstPts &&
	    Math.abs(srcOff - dstOff) < 3*numPts) {
	    /*
	     * We have an overlap.
	     */
	    tmp = new double[numPts*3];
	    double[] stmp = dstPts;
	    dstPts = tmp;
	    tmp = stmp;
	    dstOff = 0;
	}
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] = xfunct.valueAt(srcPts[srcOff],
					    srcPts[srcOff+1],
					    srcPts[srcOff+2]);
	    dstPts[dstOff+1] = yfunct.valueAt(srcPts[srcOff],
					      srcPts[srcOff+1],
					      srcPts[srcOff+2]);
	    dstPts[dstOff+2] = zfunct.valueAt(srcPts[srcOff],
					      srcPts[srcOff+1],
					      srcPts[srcOff+2]);
	    srcOff += 3;
	    dstOff += 3;
	}
	if (tmp != null) {
	    System.arraycopy(dstPts, 0, tmp, saveOff, numPts*3);
	}
    }

    @Override
    public void transform(double[] srcPts, int srcOff, float[] dstPts,
		     int dstOff, int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] = (float) xfunct.valueAt(srcPts[srcOff],
						    srcPts[srcOff+1],
						    srcPts[srcOff+2]);
	    dstPts[dstOff+1] = (float) yfunct.valueAt(srcPts[srcOff],
						      srcPts[srcOff+1],
						      srcPts[srcOff+2]);
	    dstPts[dstOff+2] = (float) zfunct.valueAt(srcPts[srcOff],
						      srcPts[srcOff+1],
						      srcPts[srcOff+2]);
	    srcOff += 3;
	    dstOff += 3;
	}
    }

    @Override
    public void transform(float[]srcPts, int srcOff, float[]dstPts,
			  int dstOff, int numPts)
    {
	float[] tmp = null;
	int saveOff = dstOff;
	if (srcPts == dstPts &&
	    Math.abs(srcOff - dstOff) < 3*numPts) {
	    /*
	     * We have an overlap.
	     */
	    tmp = new float[numPts*3];
	    float[] stmp = dstPts;
	    dstPts = tmp;
	    tmp = stmp;
	    dstOff = 0;
	}
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] = (float) xfunct.valueAt((double)srcPts[srcOff],
						    (double)srcPts[srcOff+1],
						    (double)srcPts[srcOff+2]);
	    dstPts[dstOff+1] = (float) yfunct.valueAt((double)srcPts[srcOff],
						      (double)srcPts[srcOff+1],
						      (double)srcPts[srcOff+2]);
	    dstPts[dstOff+2] = (float) zfunct.valueAt((double)srcPts[srcOff],
						      (double)srcPts[srcOff+1],
						      (double)srcPts[srcOff+2]);
	    srcOff += 3;
	    dstOff += 3;
	}
	if (tmp != null) {
	    System.arraycopy(dstPts, 0, tmp, saveOff, numPts*3);
	}
    }

    @Override
    public void transform(float[]srcPts, int srcOff, double[]dstPts,
		     int dstOff, int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] =  xfunct.valueAt((double)srcPts[srcOff],
					     (double)srcPts[srcOff+1],
					     (double)srcPts[srcOff+2]);
	    dstPts[dstOff+1] =  yfunct.valueAt((double)srcPts[srcOff],
					       (double)srcPts[srcOff+1],
					       (double)srcPts[srcOff+2]);
	    dstPts[dstOff+2] =  zfunct.valueAt((double)srcPts[srcOff],
					       (double)srcPts[srcOff+1],
					       (double)srcPts[srcOff+2]);
	    srcOff += 3;
	    dstOff += 3;
	}
    }

    @Override
    public Point3D transform(Point3D ptSrc, Point3D ptDst) {
	double pts[] = {ptSrc.getX(), ptSrc.getY(), ptSrc.getZ(),
			0.0, 0.0, 0.0};
	transform(pts, 0, pts, 3, 1);
	
	if (ptDst == null) {
	    try {
		ptDst = Cloner.makeClone(ptSrc);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError();
	    }
	}
	ptDst.setLocation(pts[3], pts[4], pts[5]);
	return ptDst;
    }

    @Override
    public AffineTransform3D affineTransform(double x, double y, double z)
	throws UnsupportedOperationException
    {
	double m00 = xfunct.deriv1At(x, y, z);
	double m10 = yfunct.deriv1At(x, y, z);
	double m20 = zfunct.deriv1At(x, y, z);
	double m01 = xfunct.deriv2At(x, y, z);
	double m11 = yfunct.deriv2At(x, y, z);
	double m21 = zfunct.deriv2At(x, y, z);
	double m02 = xfunct.deriv3At(x, y, z);
	double m12 = yfunct.deriv3At(x, y, z);
	double m22 = zfunct.deriv3At(x, y, z);
	double m03 = xfunct.valueAt(x, y, z) - m00*x - m01*y - m02*z;
	double m13 = yfunct.valueAt(x, y, z) - m10*x - m11*y - m12*z;
	double m23 = zfunct.valueAt(x, y, z) - m20*x - m21*y - m22*z;

	return new AffineTransform3D(m00, m10, m20, m01, m11, m21,
				     m02, m12, m22, m03, m13, m23);
    }

    /**
     * Clone this object.
     * @return a copy of this object
     * @exception CloneNotSupportedException this instance cannot be cloned
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	Object result = super.clone();
	if (result instanceof RVFTransform3D) {
	    RVFTransform3D obj = (RVFTransform3D)result;
	    if (xfunct instanceof Cloneable) {
		obj.xfunct = Cloner.makeClone(xfunct);
	    }
	    if (yfunct instanceof Cloneable) {
		obj.yfunct = Cloner.makeClone(yfunct);
	    }
	    if (zfunct instanceof Cloneable) {
		obj.zfunct = Cloner.makeClone(zfunct);
	    }
	}
	return result;
    }
}
//  LocalWords:  xfunct yfunct zfunct
