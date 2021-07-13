package org.bzdev.drama;

/**
 * Factory for creating instances of the class DomainMember.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/DomainMemberFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/DomainMemberFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class DomainMemberFactory extends AbstractDMemberFactory<DomainMember> {
    /**
     * Constructor.
     * @param sim the simulation
     */
    public DomainMemberFactory(DramaSimulation sim) {
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
    public DomainMemberFactory() {
	this(null);
    }

    @Override
    protected DomainMember newObject(String name) {
	return new DomainMember(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  DomainMember IFRAME SRC px steelblue HREF
