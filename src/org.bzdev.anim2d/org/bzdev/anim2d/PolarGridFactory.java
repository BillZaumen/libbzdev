package org.bzdev.anim2d;

/**
 * Factory for creating polar grids.
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/PolarGridFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/PolarGridFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public final class PolarGridFactory
    extends AbstrPolarGridFactory<PolarGrid>
{
    /**
     * Constructor.
     * @param a2d the animation
     */
    public PolarGridFactory(Animation2D a2d) {
	super(a2d);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public PolarGridFactory() {
	this(null);
    }

    @Override
    protected PolarGrid newObject(String name) {
	return new PolarGrid(getAnimation(), name, willIntern());
    }
}
