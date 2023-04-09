package org.bzdev.geom;
import java.awt.geom.*;
import org.bzdev.util.Cloner;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * PathIterator3D based on a transformation of another PathIterator3D.
 * This class obtains the segment type and control points from
 * a path iterator that it encapsulates, and applies a transform
 * to those control points.  The path iterator passed to the constructor
 * will be modified as methods are called.
 * <P>
 * The rationale for this class is the same as for
 *  {@link TransformedPathIterator}.
 * <P>
 * The same effect can be achieved by calling FlattenedPathIterator3D
 * with a limit of 0, albeit with slightly worse performance.
 */
public class TransformedPathIterator3D implements PathIterator3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    PathIterator3D pi;
    AffineTransform3D at;
    Transform3D tf;

    /**
     * Constructor given an AffineTransform3D.
     * Because the path iterator passed to this constructor will be
     * modified by this object's methods, it is a good practice to
     * call the constructor as follows
     * <blockquote><pre><code>
     *      PathIterator3D pi = ...
     *      AffineTransform3D at = ...
     *      pi = new TransformedPathIterator3D(pi, at);
     * </CODE></PRE></blockquote>
     * to avoid having multiple references that modify the same object.
     * @param pi the path iterator
     * @param at the affine transform; null if there is none.
     * @exception NullPointerException the path iterator was null
     */
    public TransformedPathIterator3D(PathIterator3D pi, AffineTransform3D at) {
	if (pi == null)
	    throw new NullPointerException(errorMsg("nullPathIterator"));
	this.pi = pi;
	// clone the affine transform if possible (should always succeed)
	if (at != null) {
	    try {
		Object obj = at.clone();
		if (obj instanceof AffineTransform3D) {
		    this.at = (AffineTransform3D) obj;
		} else {
		    this.at = at;
		}
	    } catch (Exception e) {
		this.at = at;
	    }
	}
    }

    /**
     * Constructor given a Transform3D.
     * Because the path iterator passed to this constructor will be
     * modified by this object's methods, it is a good practice to
     * call the constructor as follows
     * <blockquote><pre><code>
     *      PathIterator pi = ...
     *      Transform3D at = ...
     *      pi = new TransformedPathIterator(pi, at);
     * </CODE></PRE></blockquote>
     * to avoid having multiple references that modify the same object.
     * @param pi the path iterator
     * @param tf the  transform; null if there is none.
     * @exception NullPointerException the path iterator was null
     */
    public TransformedPathIterator3D(PathIterator3D pi, Transform3D tf) {
	if (pi == null)
	    throw new NullPointerException(errorMsg("nullPathIterator"));
	this.pi = pi;
	// clone the affine transform if possible (should always succeed)
	if (tf != null) {
	    try {
		if (tf instanceof Cloneable) {
		    this.tf =  Cloner.makeClone(tf);
		} else {
		    this.tf = tf;
		}
	    } catch (Exception e) {
		this.tf = tf;
	    }
	}
    }


    @Override
    public int currentSegment(double[] coords) {
	int st = pi.currentSegment(coords);
	if (at != null) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		at.transform(coords, 0, coords, 0, 1);
		break;
	    case PathIterator.SEG_QUADTO:
		at.transform(coords, 0, coords, 0, 2);
		break;
	    case PathIterator.SEG_CUBICTO:
		at.transform(coords, 0, coords, 0, 3);
		break;
	    default:
		break;
	    }
	}
	if (tf != null) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		tf.transform(coords, 0, coords, 0, 1);
		break;
	    case PathIterator.SEG_QUADTO:
		tf.transform(coords, 0, coords, 0, 2);
		break;
	    case PathIterator.SEG_CUBICTO:
		tf.transform(coords, 0, coords, 0, 3);
		break;
	    default:
		break;
	    }
	}
	return st;
    }
    
    @Override
    public int currentSegment(float[] coords) {
	int st = pi.currentSegment(coords);
	if (at != null) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		at.transform(coords, 0, coords, 0, 1);
		break;
	    case PathIterator.SEG_QUADTO:
		at.transform(coords, 0, coords, 0, 2);
		break;
	    case PathIterator.SEG_CUBICTO:
		at.transform(coords, 0, coords, 0, 3);
		break;
	    default:
		break;
	    }
	}
	if (tf != null) {
	    switch (st) {
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		tf.transform(coords, 0, coords, 0, 1);
		break;
	    case PathIterator.SEG_QUADTO:
		tf.transform(coords, 0, coords, 0, 2);
		break;
	    case PathIterator.SEG_CUBICTO:
		tf.transform(coords, 0, coords, 0, 3);
		break;
	    default:
		break;
	    }
	}
	return st;
    }

    @Override
    public boolean isDone() {
	return pi.isDone();
    }

    @Override
    public void next() {
	pi.next();
    }
}

//  LocalWords:  exbundle PathIterator affine subpackages javax mdash
//  LocalWords:  FlattenedPathIterator AffineTransform3D blockquote pre
//  LocalWords:  TransformedPathIterator NullPointerException tf
//  LocalWords:  nullPathIterator
