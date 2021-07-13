package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Default Factory class for Drama simulations.
 * The {@link org.bzdev.drama.generic.GenericSimulation} class uses a
 * factory, an instance of {@link org.bzdev.drama.generic.GenericFactory}
 * for creating objects implicitly. This factory is used internally and
 * consequently is much simpler than a typical named-object factory.
 * <P>
 * The minimum requirement for the simulation factory class is to
 * provide a standard method for creating instances of DomainMember.
 * Subclasses may define methods that create other objects as well.
 * As with all factories, the objective is to allow the factory to
 * provide arguments to a constructor that the user of the factory
 * would prefer not to provide explicitly.
 */
public class DramaFactory
    extends GenericFactory<DramaSimulation,Actor,Condition,
	    Domain,DomainMember,DramaFactory,Group>
{
    /**
     * Create an unshared domain member.
     * @return a domain member
     */
    public DomainMember createDomainMember() {
	return new DomainMember(getSimulation());
    }

    /*
     * Create a named domain member.
     * @param name the domain member's name; 
     * @param intern true if the domain member should be interned in the
     *               simulation's name table; false otherwise.
     * @return a domain member
     * @exception IllegalArgumentException typically means a name is already
     *            in use
    public DomainMember createDomainMember(String name, boolean intern)
	throws IllegalArgumentException
    {
	return new DomainMember(getSimulation(), name, intern);
    }
    */
}

//  LocalWords:  DomainMember Subclasses unshared createDomainMember
//  LocalWords:  IllegalArgumentException boolean getSimulation
