package org.bzdev.drama.common;
import org.bzdev.drama.generic.*;
import java.util.Set;

/**
 * Class for message recipients.
 * This class specifies the common behavior of all message recipients
 * (e.g, actors and groups).
 * <P>
 * The full implementation is in the package org.bzdev.drama.generic.  This
 * class is provided for use by {@link SimulationAdapter SimulationAdapter}
 * and thus contains only a specification of public methods and a constructor.
 */
abstract public class MessageRecipient<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericTaskObject<S,A,C,D,DM,F,G>
{

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the agent
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; 
     *        false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected MessageRecipient(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Get the groups for which this message recipient is a member.
     * @return the set of groups for which this message recipient is a member
     */
    public abstract Set<G> getGroups();

    /**
     * Determine if a message recipient is in a group
     * @param g the group to test
     * @return true if a group member of g, false otherwise
     */
    public abstract boolean inGroup(G g);

    /**
     * Join a group.
     * @param g the group to join
     * @return true if new member; false already a member
     * @exception IllegalArgumentException the addition of the group would
     *            create a loop in which two groups (perhaps via other
     *            groups) were members of each other
     */
    public abstract boolean joinGroup(G g) throws IllegalArgumentException;

    /**
     * Join a group.
     * Group-specific behavior is provided by the group being joined.
     * @param g the group to join
     * @param info an object containing additional information about a
     *             group member, with the semantics determined by subclasses
     *             of MessageRecipient (i.e., GenericActor and GenericGroup
     *             and their subclasses)
     * @return true if new member; false already a member
     * @exception IllegalArgumentException the addition of the group would
     *            create a loop in which two groups (perhaps via other
     *            groups) were members of each other; 
     *            the <code>info</code> argument has the wrong subtype
     */
    public abstract boolean joinGroup(G g, GroupInfo info)
	throws IllegalArgumentException;

    /**
     * Leave a group.
     * @param g the group to leave
     * @return true if this object is a member of g; false otherwise
     */
    public abstract boolean leaveGroup(G g);
}

//  LocalWords:  SimulationAdapter IllegalArgumentException subtype
//  LocalWords:  subclasses MessageRecipient GenericActor
//  LocalWords:  GenericGroup
