package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;


/**
 * Class for message recipients.
 * This models the common behavior of all message recipients (e.g, actors and
 * groups).
 */
abstract public class GenericMsgRecipient<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends MessageRecipient<S,A,C,D,DM,F,G> {

    GenericMsgRecipient(S sim, String name, 
			boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    abstract void receive(MessageSimulationEvent<S,A,C,D,DM,F,G> event);

    private Set<G> groups = new HashSet<G>();

    /**
     * Get the groups for which this message recipient is a member.
     * @return the set of groups for which this message recipient is a member
     */
    public Set<G> getGroups() {
	return Collections.unmodifiableSet(groups);
    }

    /**
     * Determine if a message recipient is in a group
     * @param g the group to test
     * @return true if a group member of g, false otherwise
     */
    public boolean inGroup(G g) {
	return groups.contains(g);
    }

    /**
     * Join a group.
     * @param g the group to join
     * @return true if new member; false already a member
     * @exception IllegalArgumentException the addition of the group would
     *            create a loop in which two groups (perhaps via other
     *            groups) were members of each other
     */
    public boolean joinGroup(G g) 
	throws IllegalArgumentException 
    {
	return joinGroup(g, null);
    }


    /**
     * Join a group.
     * Group-specific behavior is provided by the group being joined.
     * @param g the group to join
     * @param info an object containing additional information about a
     *             group member, with the semantics determined by subclasses
     *             of GenericMsgRecipient (i.e., GenericActor and GenericGroup
     *             and their subclasses)
     * @return true if new member; false already a member
     * @exception IllegalArgumentException the addition of the group would
     *            create a loop in which two groups (perhaps via other
     *            groups) were members of each other; 
     *            the <code>info</code> argument has the wrong subtype
     */
    public boolean joinGroup(G g, GroupInfo info) 
	throws IllegalArgumentException 
    {
	if (g.register(this, info)) {
	    groups.add(g);
	    return true;
	} else {
	    return false;
	}
    }

    private boolean leaveGroup(G g, Iterator<G> itg) {
	if (g.deregister(this)) {
	    if (itg == null) {
		groups.remove(g);
	    } else {
		itg.remove();
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Leave a group.
     * @param g the group to leave
     * @return true if this object is a member of g; false otherwise
     */
    public boolean leaveGroup(G g) {
	return leaveGroup(g, null);
    }

    void leaveGrp(G g, Iterator<?> itg) {
	groups.remove(g);
	itg.remove();
    }

    protected void onDelete() {
	super.onDelete();
	Iterator<G> itg = groups.iterator();
	while (itg.hasNext()) {
	    G g = itg.next();
	    leaveGroup(g, itg);
	}
    }

    void fireMessageReceiveStart(Object msgSource, Object message) 
    {
	// Guaranteed to return a non-null array
	Object[] listeners = getEventListenerList().getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new 
	    SimulationStateEvent(this, getSimulation(),
				 DramaSimStateEventType.RECEIVE_START,
				 // SimulationStateEvent.Type.RECEIVE_START,
				 msgSource, message); 
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    void fireMessageReceiveEnd(Object msgSource, Object message)
    {
	// Guaranteed to return a non-null array
	Object[] listeners = getEventListenerList().getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event

	SimulationStateEvent event = new 
	    SimulationStateEvent(this, getSimulation(),
				 DramaSimStateEventType.RECEIVE_END,
				 // SimulationStateEvent.Type.RECEIVE_END,
				 msgSource, message); 
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == SimulationListener.class) {
		((SimulationListener)
		 listeners[i+1]).stateChanged(event);
	    }
	}
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following
     * items.
     * <P>
     * Defined in {@link GenericMsgRecipient}:
     * <UL>
     *   <LI> the groups this object has joined.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
   @Override
   public void printConfiguration(String iPrefix, String prefix,
				  boolean printName,
				  java.io.PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	if (groups.isEmpty()) {
	    out.println(prefix + "groups: (none)");
	} else {
	    out.println(prefix + "groups:");
	    for (G g: groups) {
		out.println(prefix + "    " + g.getName());
	    }
	}
    }
}

//  LocalWords:  IllegalArgumentException subclasses GenericActor
//  LocalWords:  GenericMsgRecipient GenericGroup subtype iPrefix
//  LocalWords:  printName
