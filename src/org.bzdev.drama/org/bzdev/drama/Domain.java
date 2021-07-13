package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Domain class.
 * Domains are collections of actors and also are condition observers
 * so that domains are notified when the conditions they are
 * interested in change.
 * <p>
 * The operations provided in this class are the ones needed to
 * associate a domain with domain members and conditions, and to
 * forward notifications when a condition changes.  Domain members
 * represent actors.  By default, each actor has a unique domain
 * member, but actors can be configured so that multiple actors share
 * a domain member, which reduces memory use when a number of actors
 * have identical domain memberships.
 * <P>
 * A domain can be configured as a communication domain.  In this case
 * it can be used to determine if a path exists between a source and
 * destination, and the corresponding delays and message filters. The method
 * {@link #setMessageForwardingInfo(MsgForwardingInfo)} associates
 * a domain with an object that can determine the appropriate delays and
 * message filters. When configured as a communication domain, a domain
 * must call one of the configureAsCommunicationDomain methods. These
 * tag the domain with an instance of
 * {@link org.bzdev.drama.common.CommDomainType} to indicate the type
 * of the communication domain.  Subclasses determine how this tag should
 * be used.  On may optionally provide a set of additional instances of
 * CommDomainType. The use of these is also left to subclasses.
 * <P>
 * Note: this class overrides/replaces methods associated with
 * MsgForwardingInfo so that one does not have to use similar types from the
 * org.bzdev.drama.generic package directly.
 */
public class Domain 
    extends GenericDomain<DramaSimulation,Actor,Condition,
	    Domain,DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Constructor with parent.
     * @param sim the simulation
     * @param name the domain's name; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param parent the parent domain
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, boolean intern,
		  Domain parent) 
	throws IllegalArgumentException
    {
	super(sim, name, intern, parent);
    }


    /**
     * Constructor with priority.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, boolean intern,
		  int priority) 
	throws IllegalArgumentException
    {
	super(sim, name, intern, priority);
    }

    /**
     * Constructor with priority and parent.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param parent the parent domain
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, boolean intern,
		  Domain parent, int priority) 
	throws IllegalArgumentException
    {
	super(sim, name, intern, parent, priority);
    }


    /**
     * Constructor for uninterned domains.
     * @param sim the simulation
     */
    public Domain(DramaSimulation sim) {
	super(sim, null, false);
    }

    /**
     * Constructor for uninterned domain with parent.
     * @param sim the simulation
     * @param parent the parent domain
     */
    public Domain(DramaSimulation sim, Domain parent) {
	super(sim, null, false, parent);
    }


    /**
     * Constructor for uninterned domains with priority.
     * @param sim the simulation
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     */
    public Domain(DramaSimulation sim, int priority) {
	super(sim, null, false, priority);
    }

    /**
     * Constructor for uninterned domains with parent and priority.
     * @param sim the simulation
     * @param parent the parent domain
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     */
    public Domain(DramaSimulation sim, Domain parent, int priority) {
	super(sim, null, false, priority);
    }


    /**
     * Constructor for named and interned domains.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name)
	throws IllegalArgumentException
    {
	super(sim, name, true);
    }

    /**
     * Constructor for named and interned domains with parent.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param parent the parent domain
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, Domain parent)
	throws IllegalArgumentException
    {
	super(sim, name, true);
    }



    /**
     * Constructor for named and interned domains with priority.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name, int priority)
	throws IllegalArgumentException
    {
	super(sim, name, true, priority);
    }

    /**
     * Constructor for named and interned domain with parent and priority.
     * @param sim the simulation
     * @param name the domain's name; null if generated
     * @param parent the parent domain
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public Domain(DramaSimulation sim, String name,
		  Domain parent, int priority)
	throws IllegalArgumentException
    {
	super(sim, name, true, priority);
    }

    // provide these to avoid return types defined in the
    // org.bzdev.drama.generic package.

    MsgForwardingInfo info = null;

    /**
     * Configure message-forwarding parameters.
     * @param info an object providing the delay and message filter
     *        that a domain will use for a given message source and
     *        destination; null will restore the default
     */
    public void	setMessageForwardingInfo(MsgForwardingInfo info) {
	super.setMessageForwardingInfo(info);
    }

    /**
     * Get the class determining delays and message filters associated with
     * this domain.
     * @return an object providing delays and message filters
     *         associated with this domain
     */
    protected  MsgForwardingInfo getMsgForwardingInfo() {
	return info;
    }


}

//  LocalWords:  setMessageForwardingInfo MsgForwardingInfo
//  LocalWords:  configureAsCommunicationDomain Subclasses subclasses
//  LocalWords:  CommDomainType IllegalArgumentException uninterned
