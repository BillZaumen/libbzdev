package org.bzdev.drama;

/**
 * Factory for creating instances of the class Domain.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/DomainFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/DomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class DomainFactory extends AbstractDomainFactory<Domain> {

    /**
     * Constructor.
     * @param sim the simulation
     */
    public DomainFactory(DramaSimulation sim) {
	super(sim);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public DomainFactory() {
	this(null);
    }

    @Override
    protected Domain newObject(String name) {
	return new Domain((DramaSimulation)getObjectNamer(),
			  name, willIntern());
    }
}

//  LocalWords:  IFRAME SRC px steelblue HREF
