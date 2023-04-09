package org.bzdev.p3d;
import org.bzdev.anim2d.Animation2D;

/**
 * Factory for creating instances of Mode3DView.
 * <P>
 * The parameters this factory uses defined as follows:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/p3d/Model3DViewFactory.html"  style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/p3d/Model3DViewFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class Model3DViewFactory extends AbstrM3DViewFactory<Model3DView> {

    /**
     * Constructor.
     * @param a2d the animation
     */
    public Model3DViewFactory(Animation2D a2d) {
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
    public Model3DViewFactory() {
	this(null);
    }
    @Override
    protected Model3DView newObject(String name) {
	return new Model3DView(getAnimation(), name, willIntern());
    }
}

//  LocalWords:  DView IFRAME SRC px steelblue HREF
