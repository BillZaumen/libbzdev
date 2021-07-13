package org.bzdev.anim2d;

/**
 * Factory for creating animation layers.
 * This factory provides the same parameters and object-initialization methods
 * as AbstrAnimLayer2DFactory.
 * <P>
 * In addition to controlling the stacking order (z-order) and visibility,
 * this factory can configure an animation layer by providing the ability
 * to add standard graphic objects to it, including the following:
 * <UL>
 *   <LI> Arcs.
 *   <LI> Cubic curves.
 *   <LI> Ellipses.
 *   <LI> Images. (The actual image is specified via a URL, and may be
 *        scaled, translated, and rotated as desired).
 *   <LI> Lines.
 *   <LI> Quadratic paths.
 *   <LI> Rectangles.
 *   <LI> Round Rectangles.
 *   <LI> Spline paths. (A subclass of {@link java.awt.geom.Path2D.Double}
 *        that can use splines to provide smooth curves through a specified
 *        set of points.)
 *   <LI> Text.
 * </UL>
 * Most of the above are based on classes in the java.awt.geom
 * package.
 * <P>
 * While there are a few factory parameters that apply to an
 * animation layer as a whole, most of the parameters are
 * subparameters of the <CODE>object</CODE> parameter. These
 * parameters have integer keys, with the parameter
 * <CODE>object.type</CODE> indicating the type of the object. For
 * each type, a specific set of subparameters is used.  With the
 * exception of spline paths, each object is represented by a set of
 * parameters with a common integer-valued key. For spline paths, the
 * keys are also integer-valued, but multiple keys are needed to
 * describe a path. Each path is delimited by entries with a
 * <CODE>PATH_START</CODE> and <CODE>PATH_END</CODE> object type. In
 * between are a series of path segments separated by entries with a
 * <CODE>SEG_END</CODE>, <CODE>SEG_END_PREV</CODE>,
 * <CODE>SEG_END_NEXT</CODE> or <CODE>SEG_CLOSE</CODE> object type.
 * Each segment can optionally contain one or two control points (the
 * object type is <CODE>CONTROL_POINT</CODE>, corresponding to
 * quadratic or cubic B&eacute;zier curves.  Alternatively, a path
 * segment can contain a series of entries whose object type is
 * <CODE>SPLINE_POINT</CODE> or <CODE>SPLINE_FUNCTION</CODE>.  The
 * allowed sequences of <CODE>object.type</CODE> values for these
 * paths are shown in the following diagram:
 * <DIV style="text-align: center">
 * <img src="doc-files/layerpath.png">
 * </DIV>
 * <P>
 * The parameters this factory supports are shown in the following table
 *(the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html#object.type-org.bzdev.anim2d.AnimationLayer2DFactory" target="ftable">object.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public final class AnimationLayer2DFactory
    extends AbstrAnimLayer2DFactory<AnimationLayer2D>
{

    /**
     * Constructor.
     * @param a2d the animation
     */
    public AnimationLayer2DFactory(Animation2D a2d) {
	super(a2d);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * jst calls the default constructor with a null argument.
     */
    public AnimationLayer2DFactory() {
	this(null);
    }


    @Override
    protected AnimationLayer2D newObject(String name) {
	return new AnimationLayer2D(getAnimation(), name, willIntern());
    }
}

//  LocalWords:  AbstrAnimLayer DFactory subparameters SEG PREV zier
//  LocalWords:  eacute img src HREF ftable IFRAME
