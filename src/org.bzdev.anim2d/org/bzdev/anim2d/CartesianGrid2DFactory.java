package org.bzdev.anim2d;

/**
 * Factory for creating Caresian (rectilinear) grids.
 * The parameters this factory supports are shown in the following table:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/CartesianGrid2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/CartesianGrid2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public final class CartesianGrid2DFactory
    extends AbstrCartGrid2DFactory<CartesianGrid2D>
{
    /**
     * Constructor.
     * @param a2d the animation
     */
    public CartesianGrid2DFactory(Animation2D a2d) {
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
    public CartesianGrid2DFactory() {
	this(null);
    }

    @Override
    protected CartesianGrid2D newObject(String name) {
	return new CartesianGrid2D(getAnimation(), name, willIntern());
    }
}
