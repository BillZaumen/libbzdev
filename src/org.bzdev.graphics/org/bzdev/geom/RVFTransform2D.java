package org.bzdev.geom;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.RealValuedFunctTwoOps;
import org.bzdev.util.Cloner;
import org.bzdev.lang.UnexpectedExceptionError;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

/**
 * Class to create a Transform2D based on real-valued functions of
 * two arguments.
 */
public class RVFTransform2D implements Transform2D, Cloneable {

    RealValuedFunctionTwo xfunct;
    RealValuedFunctionTwo yfunct;

    /**
     * Constructor.
     * For this transform to successfully return the affine transform that
     * approximates this transform at the point (x, y), the functions
     * xfunct and yfunct must be able to evaluate their first
     * derivatives. If these derivatives are not implemented, an
     * {@link UnsupportedOperationException} will be thrown when
     * {@link #affineTransform(double,double)} is called.
     * @param xfunct the real-valued function providing the X coordinate
     *        of a transformed point given the original point's X and Y
     *        coordinates.
     * @param yfunct the real-valued function providing the Y coordinate
     *        of a transformed point given the original point's X and Y
     *        coordinates.
     */
    public RVFTransform2D(RealValuedFunctionTwo xfunct,
			  RealValuedFunctionTwo yfunct) {
	if (xfunct == null) {
	    throw new IllegalArgumentException();
	} else if (xfunct instanceof RealValuedFunctionTwo) {
	    this.xfunct = (RealValuedFunctionTwo) xfunct;
	} else {
	    this.xfunct = new RealValuedFunctionTwo(xfunct) ;
	}
	if (yfunct == null) {
	    throw new IllegalArgumentException();
	} else if (yfunct instanceof RealValuedFunctionTwo) {
	    this.yfunct = (RealValuedFunctionTwo) yfunct;
	} else {
	    this.yfunct = new RealValuedFunctionTwo(yfunct) ;
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
	    tmp = new double[numPts*2];
	    double[] stmp = dstPts;
	    dstPts = tmp;
	    tmp = stmp;
	    dstOff = 0;
	}
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] = xfunct.valueAt(srcPts[srcOff],
					    srcPts[srcOff+1]);
	    dstPts[dstOff+1] = yfunct.valueAt(srcPts[srcOff],
					      srcPts[srcOff+1]);
	    srcOff += 2;
	    dstOff += 2;
	}
	if (tmp != null) {
	    System.arraycopy(dstPts, 0, tmp, saveOff, numPts*2);
	}
    }

    @Override
    public void transform(double[] srcPts, int srcOff, float[] dstPts,
		     int dstOff, int numPts)
    {
	for (int n = 0; n < numPts; n++) {
	    dstPts[dstOff] = (float) xfunct.valueAt(srcPts[srcOff],
						    srcPts[srcOff+1]);
	    dstPts[dstOff+1] = (float) yfunct.valueAt(srcPts[srcOff],
						      srcPts[srcOff+1]);
	    srcOff += 2;
	    dstOff += 2;
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
						    (double)srcPts[srcOff+1]);
	    dstPts[dstOff+1] = (float) yfunct.valueAt((double)srcPts[srcOff],
						      (double)srcPts[srcOff+1]);
	    srcOff += 2;
	    dstOff += 2;
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
					     (double)srcPts[srcOff+1]);
	    dstPts[dstOff+1] =  yfunct.valueAt((double)srcPts[srcOff],
					       (double)srcPts[srcOff+1]);
	    srcOff += 2;
	    dstOff += 2;
	}
    }

    @Override
    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
	double pts[] = {ptSrc.getX(), ptSrc.getY(),
			0.0, 0.0,};
	transform(pts, 0, pts, 2, 1);
	
	if (ptDst == null) {
	    try {
		ptDst = Cloner.makeClone(ptSrc);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError();
	    }
	}
	ptDst.setLocation(pts[2], pts[2]);
	return ptDst;
    }

    @Override
    public AffineTransform affineTransform(double x, double y)
	throws UnsupportedOperationException
    {
	double m00 = xfunct.deriv1At(x, y);
	double m10 = yfunct.deriv1At(x, y);
	double m01 = xfunct.deriv2At(x, y);
	double m11 = yfunct.deriv2At(x, y);
	double m02 = xfunct.valueAt(x, y) - m00*x - m01*y;
	double m12 = yfunct.valueAt(x, y) - m10*x - m11*y;

	return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Clone this object.
     * @return a copy of this object
     * @exception CloneNotSupportedException this instance cannot be cloned
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	Object result = super.clone();
	if (result instanceof RVFTransform2D) {
	    RVFTransform2D obj = (RVFTransform2D)result;
	    if (xfunct instanceof Cloneable) {
		obj.xfunct = Cloner.makeClone(xfunct);
	    }
	    if (yfunct instanceof Cloneable) {
		obj.yfunct = Cloner.makeClone(yfunct);
	    }
	}
	return result;
    }

}
//  LocalWords:  xfunct yfunct
