package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * DomainMember class.
 * Domain members are used by actors for domain membership. By
 * default, each actor has a unique domain member, but it is
 * possible to explicitly configure an actor so that its domain
 * member object is shared with other actors.  This saves on
 * memory when multiple actors have identical domain memberships
 * and are also in multiple domains. It also simplifies setting
 * up a simulation in these cases.
 * <p>
 * At any point, an actor can replace a shared domain member with
 * a per-actor domain member, but it that is done, all of its
 * domain memberships must be reconfigured.
 */
public class DomainMember
    extends GenericDomainMember<DramaSimulation,Actor,
	    Condition,Domain,DomainMember,DramaFactory,Group>
{
    /**
     *Constructor for unshared domain members.
     * @param sim the simulation
     */
    public DomainMember(DramaSimulation sim) {
	super(sim);
    }


    /**
     *Constructor for shared domain members.
     * @param sim the simulation
     * @param name the name of this domain member; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public DomainMember(DramaSimulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }


    /**
     *Constructor for interned-and-named shared domain members.
     * @param sim the simulation
     * @param name the name of this domain member; null if generated
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public DomainMember(DramaSimulation sim, String name)
	throws IllegalArgumentException
    {
	super(sim, name, true);
    }
}

//  LocalWords:  DomainMember unshared IllegalArgumentException
