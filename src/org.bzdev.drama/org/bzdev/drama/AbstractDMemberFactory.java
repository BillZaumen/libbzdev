package org.bzdev.drama;
import org.bzdev.drama.generic.GenericDMFactory;

/**
 * Abstract DomainMember factory.
 * This class is the base class for factories that create subclasses
 * of org.bzdev.drama.DomainMember.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link DomainMemberFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/DomainMemberFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/DomainMemberFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
abstract public class AbstractDMemberFactory<Obj extends DomainMember>
    extends GenericDMFactory<AbstractDMemberFactory<Obj>, DramaSimulation,
	    Actor,Condition,Domain,DomainMember,DramaFactory,
	    Group,Obj>
{
    /**
     * Constructor.
     * @param sim the simulation associated with this factory
     */
    protected AbstractDMemberFactory(DramaSimulation sim) {
	super(sim, Domain.class);
    }
}

//  LocalWords:  DomainMember subclasses DomainMemberFactory IFRAME
//  LocalWords:  SRC px steelblue HREF
