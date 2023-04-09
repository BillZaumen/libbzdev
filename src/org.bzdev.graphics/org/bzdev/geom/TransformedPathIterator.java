package org.bzdev.geom;
import java.awt.geom.*;
import org.bzdev.util.Cloner;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * PathIterator based on an affine transformation of another PathIterator.
 * This class obtains the segment type and control points from
 * a path iterator that it encapsulates, and applies an affine transform
 * to those control points.  The path iterator passed to the constructor
 * will be modified as methods are called.
 * <P>
 * The rationale for this class is that the {@link java.awt.Shape}
 * interface allows an affine transformation to be applied when creating
 * a path iterator,  but there are no methods in subpackages of the java
 * and javax packages that will apply an affine transformation to an
 * existing path iterator&mdash;instead one must create a new path
 * and obtain an iterator for that path.
 * <P>
 * The same effect can be achieved by calling FlattenedPathIterator2D
 * with a limit of 0, albeit with slightly worse performance.
 */
public class TransformedPathIterator implements PathIterator {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    PathIterator pi;
    AffineTransform at;
    Transform2D tf;

    /**
     * Constructor given an AffineTransform.
     * Because the path iterator passed to this constructor will be
     * modified by this object's methods, it is a good practice to
     * call the constructor as follows
     * <blockquote><pre><code>
     *      PathIterator pi = ...
     *      AffineTransform at = ...
     *      pi = new TransformedPathIterator(pi, at);
     * </CODE></PRE></blockquote>
     * to avoid having multiple references that modify the same object.
     * @param pi the path iterator
     * @param at the affine transform; null if there is none.
     * @exception NullPointerException the path iterator was null
     */
    public TransformedPathIterator(PathIterator pi, AffineTransform at) {
	if (pi == null)
	    throw new NullPointerException(errorMsg("nullPathIterator"));
	this.pi = pi;
	// clone the affine transform if possible (should always succeed)
	if (at != null) {
	    try {
		Object obj = at.clone();
		if (obj instanceof AffineTransform) {
		    this.at = (AffineTransform) obj;
		} else {
		    this.at = at;
		}
	    } catch (Exception e) {
		this.at = at;
	    }
	}
    }

    /**
     * Constructor given a Transform2D.
     * Because the path iterator passed to this constructor will be
     * modified by this object's methods, it is a good practice to
     * call the constructor as follows
     * <blockquote><pre><code>
     *      PathIterator pi = ...
     *      Transform2D at = ...
     *      pi = new TransformedPathIterator(pi, at);
     * </CODE></PRE></blockquote>
     * to avoid having multiple references that modify the same object.
     * @param pi the path iterator
     * @param tf the  transform; null if there is none.
     * @exception NullPointerException the path iterator was null
     */
    public TransformedPathIterator(PathIterator pi, Transform2D tf) {
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
    public int getWindingRule() {
	return pi.getWindingRule();
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
//  LocalWords:  FlattenedPathIterator AffineTransform blockquote pre
//  LocalWords:  TransformedPathIterator NullPointerException tf
//  LocalWords:  nullPathIterator
