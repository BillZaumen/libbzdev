package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Class to represent groups of message recipients.
 * While only actors can originate messages, groups can distribute
 * those messages to multiple recipients.  Actors may join one or
 * more groups. A member of a group may be another group (there is
 * a circularity check to avoid infinite loops).
 * <P>
 * A group has methods named getDelay and getMessageFilter for
 * computing a delay and a message filter representing processing
 * performed by the group.  The delay is added to the delay computed
 * from domains.  The message filter should rarely, and ideally never,
 * cause a message to be deleted. It is intended for annotating a
 * message by, for example, incrementing a hop count used to indicate
 * the number of groups traversed.
 * <P>
 * The delays computed by both this object and domains give the delays
 * from the time a message was received by a group.  If the group sends
 * the message to each recipient at a different time (perhaps due to queuing),
 * the methods getDelay will return different values depending on the
 * recipient, and it is the responsibility of a subclass to compute those
 * values. The processing is similar regardless of whether a group will
 * forward a message to an actor or to another group, either of which
 * can be the next hop for the message (messages may pass through multiple
 * groups before reaching the final destination). When the event
 * delivering a message specifies a domain,
 * <UL>
 *    <LI> a CommDomainInfo object cdinfo is looked up by calling
 *         the simulation's findCommDomain method.
 *    <LI> A message filter for specific to the group processing the
 *         message is obtained by calling this group's getMessageFilter
 *         method, and that filter, if it exists, is applied to the
 *         message.
 *    <LI> the delay is initialized by calling this group's
 *          getDelay method, using the source of the event, the message,
 *          and the next hop as its arguments.
 *    <LI> cdinfo is used to look up a source and destination domain
 *         and a common ancestor and the delay is incremented by using
 *         the ancestor's getDelay method.
 *    <LI> a new event is then scheduled to forward the message to the
 *         next recipient.  This is done iteratively for each possible
 *         next hop..
 * </UL>
 * If a domain was not specified by the event, the following sequence
 * of operations is used:
 * <UL>
 *     <LI> the method getMessageFilter is used to look up a message
 *          filter for the current group. That message filter, if not
 *          null, is applied to the message.
 *     <LI> the delay is determined by calling this group's
 *          getDelay method, using the source of the event, the message,
 *          and the next hop as its arguments.
 *     <LI> the message, possibly modified, is then forwarded.
 * </UL>
 */
abstract public class GenericGroup<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericMsgRecipient<S,A,C,D,DM,F,G>
{
    S sim;

    private static String errorMsg(String key, Object... args) {
	return GenericSimulation.errorMsg(key, args);
    }

    private Map<A,GroupInfo> actorMembers = new HashMap<A,GroupInfo>();

    private Map<G, GroupInfo> groupMembers = new HashMap<G,GroupInfo>();

    private boolean deleting = false;

    @SuppressWarnings("unchecked")
    protected void onDelete() {
	deleting = true;
	Iterator<A> ita = actorMembers.keySet().iterator();
	while (ita.hasNext()) {
	    A a = ita.next();
	    a.leaveGrp((G)this, ita);
	}
	Iterator<G> itg = groupMembers.keySet().iterator();
	while (itg.hasNext()) {
	    G g = itg.next();
	    g.leaveGrp((G)this, itg);
	}
	Iterator<D> itd = domains.iterator();
	while (itd.hasNext()) {
	    D d = itd.next();
	    leaveDomain(d, itd);
	}
	super.onDelete();
    }


    boolean checkGroupMembers(G g) {
	if (groupMembers.containsKey(g)) {
	    return false;
	} else {
	    for (G og: groupMembers.keySet()) {
		if (!og.checkGroupMembers(g)) return false;
	    }
	    return true;
	}
    }

    /**
     * Determine if an actor is a group member.
     * @param recipient the recipient
     * @return true if the recipient is a group member; false otherwise
     */
    public boolean isMember(A recipient) {
	return actorMembers.containsKey(recipient);
    }

    /**
     * Determine if a group is a group member.
     * @param recipient the recipient
     * @return true if the recipient is a group member; false otherwise
     */
    public boolean isMember(G recipient) {
	return groupMembers.containsKey(recipient);
    }


    /**
     * Get the actor members of a group.
     * @return a set of the members of this group that are actors
     */
    public Set<A> getActorMembers() {
	return actorMembers.keySet();
    }

    /**
     * Get the number of actors that are members of a group.
     * @return the number of members
     */
    public int getActorMembersSize() {
	return actorMembers.size();
    }


    /**
     * Get the group members of a group.
     * @return a set of the members of this group that are groups
     */
    public Set<G> getGroupMembers() {
	return groupMembers.keySet();
    }


    /**
     * Get the number of groups that are members of a group.
     * @return the number of members
     */
    public int getGroupMembersSize() {
	return groupMembers.size();
    }


    /**
     * Get the information object for an actor that is a member of a group.
     * Usually the object returned will actually be a subclass of GroupInfo.
     * @param member the group member
     * @return a object providing information for the group 
     *         member; null if none
     * @exception IllegalStateException info object not cloneable in spite
     *            of having implemented the Cloneable interface
     */
    public GroupInfo getInfo(A member)
	throws IllegalStateException
    {
	GroupInfo info = actorMembers.get(member);
	if (info != null && info != defaultInfo) {
	    return info.copy();
	} else {
	    return null;
	}
    }

    /**
     * Get the information object for a group that is a member of a group.
     * Usually the object returned will actually be a subclass of GroupInfo.
     * @param member the group member
     * @return a object providing information for the group 
     *         member; null if none
     * @exception IllegalStateException info object not cloneable in spite
     *            of having implemented the Cloneable interface
     */
    public GroupInfo getInfo(G member)
	throws IllegalStateException
    {
	GroupInfo info = groupMembers.get(member);
	if (info != null && info != defaultInfo) {
	    return info.copy();
	} else {
	    return null;
	}
    }



    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the group
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericGroup(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.sim = sim;
    }


    static private GroupInfo defaultInfo = new GroupInfo();


    /**
     * Check registration GroupInfo argument.
     * This method may be overridden to force a type check of the
     * GroupInfo parameter used during registration, and must throw
     * an IllegalArgumentException if the type check fails.
     * @param info the <code>info</code> argument passed to
     *  {@link org.bzdev.drama.generic.GenericGroup#register(GenericMsgRecipient,GroupInfo)
     *         register(GenericMsgRecipient recipient, GroupInfo info)}
     * @param isGroup true if registering a group; false otherwise
     * @exception IllegalArgumentException the GroupInfo argument is invalid,
     *            most likely because the type is not consistent with the
     *            subclass of GenericGroup calling this method
     */
    protected void checkRegistrationInfo(GroupInfo info, boolean isGroup)
	throws IllegalArgumentException
    {
	return;
    }

    /**
     * Complete registration of an actor.
     * This will be called during registration to allow subclasses to
     * manage additional data structures.
     * @param recipient the message recipient being registered
     * @param newInfo the new information object; null if none
     * @param oldInfo the old information object; null if none
     * @param newMember true if registering a new member; false if
     *                  modifying the registration of an existing member
     */
    protected void onRegister(A recipient,
			      GroupInfo newInfo,
			      GroupInfo oldInfo,
			      boolean newMember) {
    }

    /**
     * Complete registration of a group.
     * This will be called during registration to allow subclasses to
     * manage additional data structures.
     * @param recipient the message recipient being registered
     * @param newInfo the new information object; null if none
     * @param oldInfo the old information object; null if none
     * @param newMember true if registering a new member; false if
     *                  modifying the registration of an existing member
     */
    protected void onRegister(G recipient,
			      GroupInfo newInfo,
			      GroupInfo oldInfo,
			      boolean newMember) {
    }


    // returns true for a new member; false for an existing one
    @SuppressWarnings("unchecked")
    boolean register(GenericMsgRecipient<S,A,C,D,DM,F,G> recipient, 
		     GroupInfo info)
	throws IllegalArgumentException 
    {
	if (recipient instanceof GenericActor) {
	    return register((A)recipient, info);
	} else if (recipient instanceof GenericGroup) {
	    return register((G)recipient, info);
	} else {
	    throw new 
		IllegalArgumentException
		(errorMsg("registerException", getName(), recipient.getName()));
	    /*("register argument" +" not actor or group");*/
	}
    }

  // returns true for a new member; false for an existing one
    boolean register(A recipient, GroupInfo info)
	throws IllegalArgumentException 
    {
	if (info != null) {
	    info = info.copy();
	    checkRegistrationInfo(info, false);
	}
	GroupInfo newInfo = (info == null)? defaultInfo: info;
	GroupInfo prev = actorMembers.put(recipient, newInfo);
	boolean result = (prev == null);
	if (prev == defaultInfo) prev = null;
	onRegister(recipient, info, prev, result);
	return result;
    }

  // returns true for a new member; false for an existing one
    boolean register(G recipient, GroupInfo info)
	throws IllegalArgumentException 
    {
	if (info != null) {
	    info = info.copy();
	    checkRegistrationInfo(info, true);
	}
	if (!checkGroupMembers(recipient)) {
	    throw new 
		IllegalArgumentException
		(errorMsg("groupLoop", getName(), recipient.getName()));
		/*("group loop during registration");*/
	}
	GroupInfo newInfo = (info == null)? defaultInfo: info;
	GroupInfo prev = groupMembers.put(recipient, newInfo);
	boolean result = (prev == null);
	if (prev == defaultInfo) prev = null;
	onRegister(recipient, info, prev, result);
	return result;
    }

    /**
     * Complete deregistration of an actor.
     * This will be called during deregistration to allow subclasses to
     * manage additional data structures.  It is only called if
     * the message recipient being deregistered is registered
     * @param recipient the message recipient being registered
     * @param info the information object for the recipient being 
     *              deregistered; null if none
     */
    protected void onDeregister(A recipient,
				GroupInfo info) 
    {
    }

    /**
     * Complete deregistration of a group.
     * This will be called during deregistration to allow subclasses to
     * manage additional data structures.  It is only called if
     * the message recipient being deregistered is registered
     * @param recipient the message recipient being registered
     * @param info the information object for the recipient being 
     *              deregistered; null if none
     */
    protected void onDeregister(G recipient,
				GroupInfo info) 
    {
    }


    @SuppressWarnings("unchecked")
    boolean deregister(GenericMsgRecipient<S,A,C,D,DM,F,G> recipient) {
	if (recipient instanceof GenericActor) {
	    return deregister((A)recipient);
	} else if (recipient instanceof GenericGroup) {
	    return deregister((G)recipient);
	} else {
	    throw new 
		IllegalArgumentException
		(errorMsg("deregisterException",getName(),recipient.getName()));
		/*("deregister argument" +" not actor or group");*/
	}
    }

    // returns true of a member was deleted, false if there was no member to
    // delete.
    private boolean deregister(A recipient) {
	GroupInfo info = deleting? actorMembers.get(recipient):
	    actorMembers.remove(recipient);
	if (info != null) {
	    onDeregister(recipient, (info==defaultInfo)? null: info);
	    return true;
	} else {
	    return false;
	}
    }

    // returns true of a member was deleted, false if there was no member to
    // delete.
    private boolean deregister(G recipient) {
	GroupInfo info = deleting? groupMembers.get(recipient):
	    groupMembers.remove(recipient);
	if (info != null) {
	    onDeregister(recipient, (info==defaultInfo)? null: info);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Get an iterator of actor message recipients for a given message source.
     * @param source the actor originating a message
     * @return an iterator that enumerates the source actor's immediate 
     *         recipients (other actors or other groups)
     *
     */
    abstract public Iterator<A> actorRecipientIterator(A source);


    /**
     * Get an integrator of group message recipients for a given message source.
     * @param source the actor originating a message
     * @return an iterator that enumerates the source actor's immediate 
     *         recipients (other actors or other groups)
     *
     */
    abstract public Iterator<G> groupRecipientIterator(A source);


    /**
     * Get the delay appropriate for delivering a message to an actor.
     * This delay represents the group's contribution to the delay,
     * and is added to any delays provided by domains.
     * @param source the source of the message
     * @param msg the message being sent
     * @param dest the recipient of the message
     * @return a non-negative long integer giving the delay in 
     *         simulation time units
     */
    public long getDelay(A source, Object msg, A dest)
    { 
	return 0;
    }


    /**
     * Get the delay appropriate for delivering a message to a group.
     * This delay represents this group's contribution to the delay,
     * and is added to any delays provided by domains.
     * @param source the source of the message
     * @param msg the message being sent
     * @param dest the recipient of the message
     * @return a non-negative long integer giving the delay in simulation 
     *         time units
     */
    public long getDelay(A source, Object msg, G dest)
    {
	return 0;
    }

    /**
     * Get the message filter when the destination is an Actor.
     * The current group is the one relaying the message.
     * The default implementation returns null, so if message filters
     * are to be used in a simulation, this method must be overridden.
     * This message filter, if non-null, will be applied before the
     * message filter(s) associated with domains forwarding messages,
     * and is applied in any case.
     * @param source the source of the message
     * @param dest a destination
     * @return the message filter to use; null if there is none.
     */
    public MessageFilter getMessageFilter(A source, A dest) {
	return null;
    }

    /**
     * Get the message filter when the destination is a Group.
     * The current group is the one relaying the message.
     * The default implementation returns null, so if message filters
     * are to be used in a simulation, this method must be overridden.
     * This message filter, if non-null, will be applied before the
     * message filter(s) associated with domains forwarding messages,
     * and is applied in any case.
     * @param source the source of the message
     * @param dest a destination
     * @return the message filter to use; null if there is none.
     */
    public MessageFilter getMessageFilter(A source, G dest) {
	return null;
    }

    /**
     * React to a message.
     * This should be overridden if the group tracks the messages it
     * relays. The default implementation does nothing.
     * @param source the message source
     * @param intermediateHop the last group to relay the message; null if none
     * @param message the message
     * @param recipientCount the number of recipients receiving
     *        the message.
     */
    protected void reactToMessage(A source, 
				  G intermediateHop,
				  Object message,
				  int recipientCount) {
    }

    @SuppressWarnings("unchecked")
    void receive(MessageSimulationEvent<S,A,C,D,DM,F,G> event) {
	Iterator<A> itA = actorRecipientIterator(event.source);
	Iterator<G> itG = groupRecipientIterator(event.source);

	// Simulation sim = getSimulation();
	StackTraceElement[] stacktrace = event.getStackTraceArray();
	int count = 0;
	long prevDelay = 0;
	while (itA.hasNext()) {
	    count++;
	    A recipient = itA.next();
	    if (event.domain != null) {
		CommDomainInfo<D> cdinfo =
		    sim.findCommDomain((G)this, event.commDomainTypes,
				       recipient);
		if (cdinfo == null) {
		    count--;
		    continue;
		}
		D domain = cdinfo.getDestDomain();
		Object msg = event.message;
		MessageFilter mfilter =
		    getMessageFilter(event.source, recipient);
		if (mfilter != null && msg != null) {
		    msg = mfilter.filterMessage(msg);
		    if (msg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		}
		mfilter =
		    cdinfo.getAncestorDomain().getMessageFilter
		    ((G) this, msg,
		     cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
		     recipient);

		if (mfilter != null && msg != null) {
		    Object newmsg = mfilter.filterMessage(msg);
		    if (newmsg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		    MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
			= new MessageSimulationEvent<S,A,C,D,DM,F,G>
			(newmsg, event.source, event.commDomainTypes,
			 (G)this, domain, recipient);
		    newEvent.setStackTraceArray(stacktrace);
		    long delay = getDelay(event.source, msg, recipient);
		    delay += cdinfo.getAncestorDomain().getDelay
			((G) this, newmsg,
			 cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
			 recipient);
		    sim.scheduleEvent(newEvent, delay);
		} else {
		    MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
			= new MessageSimulationEvent<S,A,C,D,DM,F,G>
			 (event.message, event.source, event.commDomainTypes,
			  (G)this, domain, recipient);
		    newEvent.setStackTraceArray(stacktrace);
		    long delay = getDelay(event.source, msg, recipient);
		    delay += cdinfo.getAncestorDomain().getDelay
			((G) this, event.message,
			 cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
			 recipient);
		    sim.scheduleEvent(newEvent, delay);
		}
	    } else {
		Object msg = event.message;
		MessageFilter mfilter =
		    getMessageFilter(event.source, recipient);
		if (mfilter != null && msg != null) {
		    msg = mfilter.filterMessage(msg);
		    if (msg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		}
		MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
		    = new MessageSimulationEvent<S,A,C,D,DM,F,G>
		    (msg, event.source, null, (G)this, null, recipient);
		newEvent.setStackTraceArray(stacktrace);

		long delay = getDelay(event.source, msg, recipient);
		sim.scheduleEvent(newEvent, delay );
	    }
	}
	while (itG.hasNext()) {
	    count++;
	    G recipient = itG.next();
	    if (event.domain != null) {
		CommDomainInfo<D> cdinfo =
		    sim.findCommDomain((G)this, event.commDomainTypes,
				       recipient);
		D domain = cdinfo.getDestDomain();
		if (cdinfo == null) {
		    count--;
		    continue;
		}
		Object msg = event.message;
		MessageFilter mfilter =
		    getMessageFilter(event.source, recipient);
		if (mfilter != null && msg != null) {
		    msg = mfilter.filterMessage(msg);
		    if (msg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		}
		mfilter =
		    cdinfo.getAncestorDomain().getMessageFilter
		    ((G) this, msg,
		     cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
		     recipient);

		if (mfilter != null && msg != null) {
		    Object newmsg = mfilter.filterMessage(msg);
		    if (newmsg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		    MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
			= new MessageSimulationEvent<S,A,C,D,DM,F,G>
			 (newmsg, event.source, event.commDomainTypes,
			  (G)this, domain, recipient);
		    newEvent.setStackTraceArray(stacktrace);
		    long delay = getDelay(event.source, msg, recipient);
		    delay += cdinfo.getAncestorDomain().getDelay
			((G) this, newmsg,
			 cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
			 recipient);
		    sim.scheduleEvent(newEvent, delay);
		} else {
		    MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
			= new MessageSimulationEvent<S,A,C,D,DM,F,G>
			(event.message, event.source, event.commDomainTypes,
			 (G)this, domain, recipient);
		    newEvent.setStackTraceArray(stacktrace);
		    long delay = getDelay(event.source, msg, recipient);
		    delay += cdinfo.getAncestorDomain().getDelay
			((G) this, event.message,
			 cdinfo.getSourceDomain(), cdinfo.getDestDomain(),
			 recipient);
		    sim.scheduleEvent(newEvent, delay);
		}
	    } else {
		Object msg = event.message;
		MessageFilter mfilter = 
		    getMessageFilter(event.source, recipient);
		if (mfilter != null && msg != null) {
		    msg = mfilter.filterMessage(msg);
		    if (msg == MessageFilter.DELETED) {
			count--;
			continue;
		    }
		}
		MessageSimulationEvent<S,A,C,D,DM,F,G> newEvent
		    = new MessageSimulationEvent<S,A,C,D,DM,F,G>
		    (msg, event.source, null, (G)this, null, recipient);
		newEvent.setStackTraceArray(stacktrace);
		long delay = getDelay(event.source, msg, recipient);
		sim.scheduleEvent(newEvent, delay);
	    }
	}
	reactToMessage(event.source, event.intermediateHop, 
		       event.message, count);
    }

    Set<D> domains = new TreeSet<D>();
    Map<D,Set<D>> domainAncestors = new HashMap<D,Set<D>>();

    /**
     * Get a set of the domains that the current group has joined and
     * that have a common ancestor.
     * The iterator for the returned set will return domains in the order
     * set by domain-specific criteria.
     * @param ancestor the ancestor domain
     * @return a set of domains that the current group has joined and that
     *         have the specified ancestor
     */
    public Set<D> getChildDomains(D ancestor) {
	Set<D> set = domainAncestors.get(ancestor);
	if (set == null) {
	    return null;
	} else {
	    return Collections.unmodifiableSet(set);
	}
    }

    /**
     * Join a domain.
     * @param d the domain
     */
    @SuppressWarnings("unchecked")
    public void joinDomain(D d) {
	if (domains.add(d)) {
	    d.addGroup((G)this);
	    D parent = d.getParent();
	    while (parent != null) {
		Set<D> set = domainAncestors.get(parent);
		if (set == null) {
		    set = new TreeSet<D>();
		    domainAncestors.put(parent,set);
		}
		set.add(d);
		parent = parent.getParent();
	    }
	}
    }

    private void clearParent(D d) {
	D parent = d.getParent();
	while (parent != null) {
	    Set<D> set = domainAncestors.get(parent);
	    set.remove(d);
	    if (set.isEmpty()) {
		domainAncestors.remove(parent);
	    }
	    parent = parent.getParent();
	}
    }


    @SuppressWarnings("unchecked")
    private void leaveDomain(D d, Iterator<D> itd) {
	if (domains.contains(d)) {
	    if (itd == null) {
		domains.remove(d);
	    } else {
		itd.remove();
	    }
	    d.removeGroup((G)this);
	    clearParent(d);
	}
    }


    /**
     * Leave a domain.
     * @param d the domain
     */
    @SuppressWarnings("unchecked")
    public void leaveDomain(D d) {
	leaveDomain(d, null);
    }

    // used by GenericDomain's onDelete() method
    void disFrom(D d, Iterator<G> itg) {
	clearParent(d);
	domains.remove(d);
	itg.remove();
    }


    /**
     * Get the set of domains that a group has joined.
     * @return a set of domains
     */
    public Set<D> domainSet() {
	return Collections.unmodifiableSet(domains);
    }

    /**
     * Determine if the current group has joined a domain.
     * @param d the domain
     * @return true if current group has joined the specified domain;
     *         false otherwise
     */
    public boolean inDomain(D d) {
	return domains.contains(d);
    }

    /**
     * {@inheritDoc}
     * <P>
     * Defined in {@link GenericGroup}:
     * <UL>
     *   <LI> the domains this object has joined.
     *   <LI> the actors that are members of this group.F
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
	if (domains.isEmpty()) {
	    out.println(prefix + "domains: (none)");
	} else {
	    out.println(prefix + "domains:");
	    for (D d: domains) {
		out.println(prefix + "    " + d.getName());
	    }
	}
	Set<A> actors = actorMembers.keySet();

	if (actors.isEmpty()) {
	    out.println(prefix + "actor members: (none)");
	} else {
	    out.println(prefix + "actor members:");
	    for (A a: actors) {
		out.println(prefix + "    " + a.getName());
	    }
	}

	Set<G> groups = groupMembers.keySet();

	if (groups.isEmpty()) {
	    out.println(prefix + "group members: (none)");
	} else {
	    out.println(prefix + "group members:");
	    for (G g: groups) {
		out.println(prefix + "    " + g.getName());
	    }
	}
    }
}

//  LocalWords:  exbundle getDelay getMessageFilter CommDomainInfo
//  LocalWords:  cdinfo findCommDomain GroupInfo cloneable sim msg
//  LocalWords:  IllegalStateException IllegalArgumentException dest
//  LocalWords:  GenericMsgRecipient isGroup GenericGroup newInfo
//  LocalWords:  oldInfo newMember registerException groupLoop
//  LocalWords:  deregistration deregistered deregisterException
//  LocalWords:  deregister intermediateHop recipientCount onDelete
//  LocalWords:  getSimulation GenericDomain's iPrefix printName
