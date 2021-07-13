package org.bzdev.anim2d;

/**
 * Factory for creating animation shapes.
 * In addition to controlling the stacking order (z-order) and visibility,
 * this factory can configure an animation shape by adding animation paths
 * to it. Each path provides part of a shape's outline.  A winding rule is used
 * to determine which paths represent an interior or exterior boundary for
 * the shape.  The Z-order and visibility will be ignored by animation layers.
 * When an animation shape is used as part of an animation layer, its
 * visibility should be set to <CODE>false</CODE>, which is the default for
 * this factory.
 * <P>
 * This factory provides a method
 * {@link AbstrAnimShape2DFactory#set(String,int,Object)} that will accept
 * instances of {@link java.awt.Shape}, {@link java.awt.geom.PathIterator},
 * {@link String}, and {@link AnimationPath2D} as its third argument. If a
 * string is provided, the string must be the name of an instance of
 * {@link AnimationPath2D} associated with the factory's animation.
 * <P>
 * The parameters this factory supports are shown in the following table
 *(the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html#object.type-org.bzdev.anim2d.AnimationShape2DFactory" target="ftable">object.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * For example, with the <CODE>scrunner</CODE> command, one can use the
 * following ECMAScript code to create a shape:
 * <BLOCkQUOTE><CODE><PRE>
 *   scripting.importClass("org.bzdev.anim2d.Animation2D");
 *   scripting.importClass("org.bzdev.geom.Paths2D");
 *   Animation a2d = new Animation2D(scripting);
 *   ...
 *   a2d.createFactories("org.bzdev.anim2d", {
 *        shapef: "AnimationShape2DFactory"
 *   };
 *   var circle1 =
 *      Paths2D.createArc(0.0, 0.0, 100.0, 0.0, Math.toRadians(360));
 *   circle1.closePath();
 *   var circle2 = Paths2D.reverse
 *     (Paths2D.createArc(0.0, 0.0, 75.0, 0.0, Math.toRadians(360)));
 *   circle2.closePath();
 *   shapef.createObject("annulus", [
 *     {visible: true, zorder: 0},
 *     {fillColor.css: "paleyellow"},
 *     {drawColor.css: "black"},
 *     {stroke.width: 5.0},
 *     {windingRule: "WIND_NON_ZERO"},
 *     {withIndex: [
 *           {shape: circle1},
 *           {shape: circle2}
 *        ]}
 *   ]);
 * </PRE></CODE></BLOCKQUOTE>
 * In this example, paths are created directly and those paths are
 * used to define the outline for a shape.
 * @see AnimationShape2D
 */
public final class AnimationShape2DFactory
    extends AbstrAnimShape2DFactory<AnimationShape2D>
{

    /**
     * Constructor.
     * @param a2d the animation
     */
    public AnimationShape2DFactory(Animation2D a2d) {
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
    public AnimationShape2DFactory() {
	this(null);
    }


    @Override
    protected AnimationShape2D newObject(String name) {
	return new AnimationShape2D(getAnimation(), name, willIntern());
    }
}

//  LocalWords:  HREF ftable IFRAME SRC AnimationShape AbstrAnimShape
//  LocalWords:  DFactory AnimationPath
