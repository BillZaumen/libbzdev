package org.bzdev.obnaming;

/**
 * Default implementation for a named object factory.
 * The default implementation is a bare-bones implementation
 * suitable for simple applications.
 * The type parameter OBJ is class of the named object that the factory will
 * create (if it is a concrete class) or a named object class whose
 * instances will be configured.  The named objects created will have
 * {@link DefaultNamedObject} as a common superclass and
 * {@link DefaultObjectNamer} as the corresponding object namer.
 * This factory does not define any parameters and simply serves as a common
 * base class for other factories.
 * <P>
 * As an example, to create an object namer, a named object, and
 * a factory for the named object, one would define the following
 * classes:
 * <blockquote><pre><code>
 *  abstract public class OurNamedObject
 *        extends DefaultNamedObject&lt;OurNamedObject&gt;
 *  {
 *       ...
 *       protected OurNamedObject(OurObjectNamer namer,
 *                                String name,
 *                                boolean intern)
 *       {
 *          super(namer, name, intern);
 *       }
 *  }
 *
 *  public class OurObjectNamer
 *       extends DefaultObjectNamer&lt;OurNamedObject&gt;
 *  {
 *       ...
 *       public OurObjectNamer() {
 *           super(OurNamedObject.class);
 *       }
 *  }
 *
 *  abstract public class OurObjectFactory&lt;OBJ extends OurNamedObject&gt;
 *       extends DefaultNOFactory&lt;OurObjectNamer,OurNamedObject,OBJ&gt;
 *  {
 *       ...
 *       protected OurObjectFactory(OurObjectNamer namer) {
 *          super(namer);
 *       }
 *  }
 * </CODE></PRE></blockquote>
 * Additional code (indicated by an ellipsis) is, of course, necessary
 * to do anything useful.
 */
public abstract class DefaultNOFactory 
    <NMR extends ObjectNamerOps<NMD>,
     NMD extends DefaultNamedObject<NMD>,
     OBJ extends NMD>
    extends NamedObjectFactory
	    <DefaultNOFactory<NMR, NMD, OBJ>, NMR, NMD, OBJ>
{
    /**
     * Constructor.
     * @param namer the object namer associated with this factory
     */
    protected DefaultNOFactory(NMR namer) {
	super(namer);
    }
}

//  LocalWords:  DefaultNamedObject superclass DefaultObjectNamer pre
//  LocalWords:  namer blockquote OurNamedObject lt OurObjectNamer
//  LocalWords:  boolean OurObjectFactory DefaultNOFactory
