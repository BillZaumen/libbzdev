package org.bzdev.anim2d;

/**
 * Factory for AnimationPath2D objects.
 * <P>
 * This provides the same parameters and object-initialization methods
 * as AbstrAnimPath2DFactory.
 * <P>
 * Paths are described by a sequence of control points, represented by a
 * <code>cpoint</code> table with an integer key.
 * The parameter <CODE>cpoint.type</CODE> has a value that
 * is an enumeration constant whose type is
 * {@link org.bzdev.geom.SplinePathBuilder.CPointType} As one increments the
 * key, the following transition diagram shows the sequence of allowed values
 * for this type:
 * <DIV style="text-align:center">
 *     <IMG SRC="doc-files/basicpath.png" class="imgBackground"
 *      ALT="UML Diagram">
 *</DIV>
 * The parameters this factory supports are shown in the following table
 * (the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html#cpoint.type-org.bzdev.anim2d.AnimationPath2DFactory" target="ftable">cpoint.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see org.bzdev.geom.SplinePathBuilder.CPointType
 * @see org.bzdev.geom.SplinePathBuilder.WindingRule
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Cap
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Join
 * @see java.awt.BasicStroke
 */
public class AnimationPath2DFactory
    extends AbstrAnimPath2DFactory<AnimationPath2D>
{
    /**
     * Constructor.
     * @param a2d the animation associated with this factory
     */
    public AnimationPath2DFactory(Animation2D a2d) {
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
    public AnimationPath2DFactory() {
	this(null);
    }


    @Override
    protected AnimationPath2D newObject(String name) {
	return new AnimationPath2D(getAnimation(), name, willIntern());
    }
}
