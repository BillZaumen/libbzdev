package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

import org.bzdev.util.DisjointSetsUnion;
import org.bzdev.util.DisjointSortedSetsUnion;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Base class representing actors.
 * Actors are task objects that can send and receive messages. They
 * can be members of domains, and when joining a domain, can request
 * to be notified when a condition changes.
 */
abstract public class GenericActor<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericMsgRecipient<S,A,C,D,DM,F,G> implements CondObserver<C,A>
{

    S sim;

    private static String errorMsg(String key, Object... args) {
	return GenericSimulation.errorMsg(key, args);
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the actor
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.devqsim.Simulation#getObjectNames(Class)
     */
    @SuppressWarnings("unchecked")
    protected GenericActor(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.sim = sim;
	domainMember = sim.getFactory().createDomainMember();
	domainMember.register((A)this);
    }


    // private Set<C> conditions = new HashSet<C>();
    @SuppressWarnings("unchecked")
    private A thisInstance() {
	try {
	    return (A)this;
	} catch (ClassCastException e) {
	    return null;
	}
    }

    private CondObserverImpl<C,A>
	impl = new CondObserverImpl<C,A>(thisInstance()) {
	/*
	protected ConditionMode transformedMode(ConditionMode mode) {
	    switch (mode) {
	    default:
		return mode;
	    case OBSERVER_ADDED_CONDITION:
		return ConditionMode.DOMAIN_ADDED_CONDITION;
	    case OBSERVER_REMOVED_CONDITION:
		return ConditionMode.DOMAIN_REMOVED_CONDITION;
	    }
	}
	*/
	protected void doConditionChange(C c, ConditionMode mode,
					 SimObject source)
	{
	    GenericActor.this.onConditionChange(c, mode, source);
	}
    };

    public CondObserverImpl<C,A> getCondObserverImpl() {
	return impl;
    }


    /**
     * Associate a condition with an actor.
     * This adds a condition independently from those associated with
     * domains.
     * @param c the condition
     * @return true on success; false on failure
     */
    public boolean addCondition(C c) {
	return impl.addCondition(c);
    }

    /**
     * Disassociate a condition with a condition observer.
     * This removes a condition independently from those associated with
     * domains.
     * @param c the condition
     * @return true on success; false on failure
     */
    public boolean removeCondition(C c) {
	return impl.removeCondition(c);
    }

    /**
     * Determine if a condition observer has (is associated with) a condition.
     * This does not include conditions associated with a domain.
     * @param c the condition
     * @return true if the condition observer has condition c; false otherwise
     */
    public boolean hasCondition(C c) {
	return impl.hasCondition(c);
    }

    /**
     * Get a set of conditions
     * This does not include conditions associated with a domain.
     * @return the set of conditions that are associated with this
     *         condition observer
     */
    public Set<C> conditionSet() {
	return impl.conditionSet();
    }

    /**
     * Set whether condition-change notifications should be queued or
     * sent immediately.
     * Setting this mode to true will improve performance when many conditions
     * are changed at the same simulation time.  If the mode is changed from
     * true to false while there are some queued notifications, those will
     * be forwarded.
     * This does not apply to conditions associated with a domain.
     * @param value true if notifications should be queued; false if they
     *              should be sent immediately.
     */
    public void setConditionChangeQMode(boolean value) {
	impl.setConditionChangeQMode(value);
    }

    /**
     * Determine if condition-change notifications are queued.
     * This does not apply to conditions associated with a domain.
     * @return true if queued; false otherwise.
     */
    public boolean getConditionChangeQMode() {
	return impl.getConditionChangeQMode();
    }

    boolean messagesQueued = false;

    protected void onDelete() {
	if (sharedDomainMember != null) {
	    setSharedDomainMember(null);
	}
	domainMember.leaveAllDomains();
	impl.removeAllConditions();
	super.onDelete();
    }

    /**
     * Configure actor so that incoming messages are queued.
     * When the value transitions from true to false, the queue will
     * be drained by calling drainMessageQueue().
     * @param value true if messages should be queued; false otherwise
     */
    protected void setMessageQueueing(boolean value) {
	if (! value && messagesQueued) {
	    drainMessageQueue();
	}
	messagesQueued = value;
    }

    Queue<MessageSimulationEvent<S,A,C,D,DM,F,G>> msgQueue = 
	new LinkedList<MessageSimulationEvent<S,A,C,D,DM,F,G>>();
    
    /**
     * Drain an actor's message queue.
     * This will process all messages that have been queued by
     * calling the actor's doReceive method for each queued message.
     */

    @SuppressWarnings("unchecked")
    protected void drainMessageQueue() {
	for (MessageSimulationEvent<S,A,C,D,DM,F,G> event: msgQueue) {
	    doReceive(event.message, (A)event.source, true);
	}
	msgQueue.clear();
    }


    // receive a message
    @SuppressWarnings("unchecked")
    void receive(MessageSimulationEvent<S,A,C,D,DM,F,G> event) {
	if (messagesQueued) {
	    msgQueue.offer(event);
	} else {
	    doReceive(event.message, (A)event.source, false);
	}
    }

    /**
     * Respond to a message.
     * Subclasses should implement this method to perform any necessary
     * processing in response to a message.
     * @param message the message just received
     * @param source the actor that originated the message
     * @param wereQueued true if the message was queued; false otherwise
     */

    protected void doReceive(Object message, 
			     A source, 
			     boolean wereQueued)
    {
    }


    /**
     * Send a message to an actor.
     * @param msg the message to send
     * @param dest the destination actor
     * @param delay how many time units to wait before the message is
     *              processed by its destination
     */
    @SuppressWarnings("unchecked")
    protected final void send(Object msg, A dest, long delay) 
    {
	// Simulation sim = getSimulation();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, (A)this, (Set<CommDomainType>)null,
			   null, null, dest), 
			  delay);
    }

    /**
     * Send a message to a group.
     * The group will then distribute the message to some group-specific
     * subset of the group's members.
     * @param msg the message to send
     * @param dest the destination group
     * @param delay how many time units to wait before the message is
     *              processed by its destination
     */
    @SuppressWarnings("unchecked")
    protected final void send(Object msg, G dest, long delay) 
    {
	// Simulation sim = getSimulation();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg,(A)this, (Set<CommDomainType>)null,
			   null, null, dest), 
			  delay);
    }

    /** Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericActor,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param dest the destination
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     */
    public String[] findMsgFrwdngInfo(A dest, D domain) {
	return sim.findMsgFrwdngInfo(thisInstance(),
				     domain.getCommDomainTypeSet(),
				     dest,
				     domain);
    }

    /**
     * Send a message to an actor.
     * The message delay and any message filtering will be determined by
     * the specified domain. If communication is not possible with the
     * specified domain, no message will be sent. This actor or the
     * destination must be members of the domain provided by the third
     * argument.
     * <P>
     * If sim is the simulation for this object, the implementation
     * will call sim.findCommDomain(this,domain.getCommDomainTypeSet(),
     * dest,domain) to provide an instance cdinfo of
     * {@link org.bzdev.drama.common.CommDomainInfo}}. To compute a
     * message delay, the {@link GenericMsgFrwdngInfo} object for various
     * domains will be used.  These domains are taken from the set of
     * domains starting at both cdinfo.getSourceDomain() and
     * cdinfo.getDestDomain(), including sequences of parent domains
     * and ending at cdinfo.getAncestorDomain().
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericActor,GenericDomain)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param dest the destination actor
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     * @return true if the message was sent; false if the message could
     *         not be sent
     */
    protected final boolean send(Object msg,
				 A dest,
				 D domain)
    {
	S sim = getSimulation();
	A src = thisInstance();
	Set<CommDomainType> commDomainTypes = domain.getCommDomainTypeSet();
	// CommDomainInfo<D> cdinfo =
	//    domain.communicationMatch(src, dest);
	CommDomainInfo<D> cdinfo =
	    sim.findCommDomain(src, commDomainTypes, dest, domain);
	if (cdinfo == null) {
	    return false;
	}
	long delay = cdinfo.getAncestorDomain().getDelay
	    (src, msg, cdinfo.getSourceDomain(), cdinfo.getDestDomain(), dest);
	MessageFilter mfilter = cdinfo.getAncestorDomain().getMessageFilter
	    (src, msg,cdinfo.getSourceDomain(), cdinfo.getDestDomain(), dest);
	if (mfilter != null && msg != null) {
	    msg = mfilter.filterMessage(msg);
	    if (msg == MessageFilter.DELETED) {
		// true because filtered messages are assumed to have been
		// sent and lost in transit.
		return true;
	    }
	}
	domain = cdinfo.getDestDomain();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, src, commDomainTypes, null, domain, dest),
			  delay);
	return true;
    }

    /** Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericActor,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericGroup,GenericDomain)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param dest the destination
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     */
    public String[] findMsgFrwdngInfo(G dest, D domain) {
	return sim.findMsgFrwdngInfo(thisInstance(),
				     domain.getCommDomainTypeSet(),
				     dest,
				     domain);
    }

    /**
     * Send a message to a group given a domain.
     * The message delay and any message filtering will be determined by
     * the specified domain. The message will not be sent if the domain
     * does not allow communication between the current actor and the
     * destination group. This actor or the
     * destination must be members of the domain provided by the third
     * argument.
     * <P>
     * If sim is the simulation for this object, the implementation
     * will call sim.findCommDomain(this,domain.getCommDomainTypeSet(),
     * dest,domain) to provide an instance cdinfo of
     * {@link org.bzdev.drama.common.CommDomainInfo}}. To compute a
     * message delay, the {@link GenericMsgFrwdngInfo} object for various
     * domains will be used.  These domains are taken from the set of
     * domains starting at both cdinfo.getSourceDomain() and
     * cdinfo.getDestDomain(),  including sequences of parent domains
     * and ending at cdinfo.getAncestorDomain().
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericGroup,GenericDomain)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param dest the destination group
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     * @return true if the message was sent; false if the message could
     *         not be sent
     */
    protected final boolean send(Object msg,
				 G dest,
				 D domain)
    {
	S sim = getSimulation();
	A src = thisInstance();
	Set<CommDomainType> commDomainTypes = domain.getCommDomainTypeSet();
	// CommDomainInfo<D> cdinfo =
	//    domain.communicationMatch(src, dest);
	CommDomainInfo<D> cdinfo = sim.findCommDomain(src,
						      commDomainTypes,
						      dest,
						      domain);
	if (cdinfo == null) {
	    return false;
	}
	long delay = cdinfo.getAncestorDomain().getDelay
	    (src, msg, cdinfo.getSourceDomain(), cdinfo.getDestDomain(), dest);
	MessageFilter mfilter = cdinfo.getAncestorDomain().getMessageFilter
	    (src, msg,cdinfo.getSourceDomain(),cdinfo.getDestDomain(), dest);
	if (mfilter != null && msg != null) {
	    msg = mfilter.filterMessage(msg);
	    if (msg == MessageFilter.DELETED) return true;
	}
	domain = cdinfo.getDestDomain();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, src, commDomainTypes, null, domain, dest),
			  delay);
	return true;
    }

    /**
     * Send a message to an actor using a default domain.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * <P>
     * This method calls send(msg, dest, (Set&lt;CommDomainType&gt;)null).
     * @param msg the message to send
     * @param dest the destination actor
     * @return true on success; false if the message cannot be sent
     */
    protected final boolean send(Object msg, A dest) {
	return send(msg, dest, (Set<CommDomainType>)null);
    }


    /** Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericActor,Set)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param dest the destination
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     */
    public String[] findMsgFrwdngInfo(A dest,
				      Set<CommDomainType> commDomainTypes)
    {
	return sim.findMsgFrwdngInfo(thisInstance(),
				    commDomainTypes,
				     dest);
    }

    /**
     * Send a message to an actor using a  domain constrained by a set 
     * of communication-domain types to use.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * In some simulation flavors, domains may represent constraints
     * that must be satisfied in order to successfully send a message.
     * If a message filter causes a message to be dropped, this method
     * will return true - a return value of false indicates that the
     * destination is not reachable, not that there has been a transient
     * failure.
     * <P>
     * If sim is the simulation for this object, the implementation
     * will call sim.findCommDomain(this,commDomainTypes, dest,domain)
     * to provide an instance cdinfo of
     * {@link org.bzdev.drama.common.CommDomainInfo}}. To compute a
     * message delay, the {@link GenericMsgFrwdngInfo} object for various
     * domains will be used.  These domains are taken from the set of
     * domains starting at both cdinfo.getSourceDomain() and
     * cdinfo.getDestDomain(), including sequences of parent domains
     * and ending at cdinfo.getAncestorDomain().
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericActor,Set)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param dest the destination actor
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @return true on success; false if the message cannot be sent
     */
    protected final boolean send(Object msg,
				 A dest, 				
				 Set<CommDomainType> commDomainTypes) 
    {
	A src = thisInstance();
	CommDomainInfo<D> cdinfo =
	    sim.findCommDomain(src, commDomainTypes, dest);
	if (cdinfo == null) return false;
	long delay = cdinfo.getAncestorDomain().getDelay
	    (src, msg, cdinfo.getSourceDomain(), cdinfo.getDestDomain(), dest);
	MessageFilter mfilter = cdinfo.getAncestorDomain().getMessageFilter
	    (src, msg,cdinfo.getSourceDomain(),cdinfo.getDestDomain(), dest);
	if (mfilter != null && msg != null) {
	    msg = mfilter.filterMessage(msg);
	    if (msg == MessageFilter.DELETED) return true;
	}
	D domain = cdinfo.getDestDomain();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, src, commDomainTypes, null, domain, dest),
			  delay);
	return true;
    }


    /**
     * Send a message to a group using a default domain.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * <P>
     * This method just calls send(msg, dest, (Set&lt;CommDomainType&gt;)null).
     * @param msg the message to send
     * @param dest the destination group
     * @return true on success; false if the message cannot be sent
     */
    protected final boolean send(Object msg, G dest) {
	return send(msg, dest, (Set<CommDomainType>) null);
    }

    /** Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericGroup,Set)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param dest the destination
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     */
    public String[] findMsgFrwdngInfo(G dest,
				      Set<CommDomainType> commDomainTypes)
    {
	return sim.findMsgFrwdngInfo(thisInstance(),
				    commDomainTypes,
				     dest);
    }

    /**
     * Send a message to a group using a domain constrained by a set 
     * of communication-domain types to use.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * If a message filter causes a message to be dropped, this method
     * will return true - a return value of false indicates that the
     * destination is not reachable, not that there has been a transient
     * failure.
     * <P>
     * If sim is the simulation for this object, the implementation
     * will call sim.findCommDomain(this,commDomainTypes, dest,domain)
     * to provide an instance cdinfo of
     * {@link org.bzdev.drama.common.CommDomainInfo}}. To compute a
     * message delay, the {@link GenericMsgFrwdngInfo} object for various
     * domains will be used.  These domains are taken from the set of
     * domains starting at both cdinfo.getSourceDomain() and
     * cdinfo.getDestDomain(),  including sequences of parent domains
     * and ending at cdinfo.getAncestorDomain().
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericGroup,Set)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param dest the destination group
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     * @return true on success; false if the message cannot be sent
     */
    @SuppressWarnings("unchecked")
    protected final boolean send(Object msg,
				 G dest,
				 Set<CommDomainType> commDomainTypes)
    {
	A src = thisInstance();
	CommDomainInfo<D> cdinfo =
	    sim.findCommDomain(src, commDomainTypes, dest);
	if (cdinfo == null) return false;
	long delay = cdinfo.getAncestorDomain().getDelay
	    (src, msg,cdinfo.getSourceDomain(),cdinfo.getDestDomain(), dest);
	MessageFilter mfilter = cdinfo.getAncestorDomain().getMessageFilter
	    (src, msg,cdinfo.getSourceDomain(),cdinfo.getDestDomain(), dest);
	if (mfilter != null && msg != null) {
	    msg = mfilter.filterMessage(msg);
	    if (msg == MessageFilter.DELETED) return true;
	}
	D domain = cdinfo.getDestDomain();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, src, commDomainTypes, null, domain, dest),
			  delay);
	return true;
    }


    /**
     * Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param domain the domain specifying the recipient actors
     */
    public String[] findMsgFrwdngInfo(D domain)
    {
	Iterator<A> it = domain.actorSet().iterator();
	Set<String> result = new HashSet<>();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) {
		String[] array = findMsgFrwdngInfo(actor, domain);
		if (array != null) {
		    for (String name: array) {
			result.add(name);
		    }
		}
	    }
	}
	String s[] = new String[result.size()];
	return result.toArray(s);
    }

    /**
     * Send a message to all actors in a domain.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * <p>
     * The message will not be delivered to the source (the current
     * actor).  Otherwise this is equivalent to calling
     * send(msg,actor) repeatedly for each actor in the domain
     * specified by the second argument.
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericDomain)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param domain the domain specifying the recipient actors
     */

    protected void send(Object msg, D domain) {
	Iterator<A> it = domain.actorSet().iterator();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) send(msg, actor);
	}
    }

    /**
     * Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericDomain,GenericDomain)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param dests the domain specifying the recipient actors
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     */
    public String[] findMsgFrwdngInfo(D dests, D domain)
    {
	Iterator<A> it = domain.actorSet().iterator();
	Set<String> result = new HashSet<>();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) {
		String[] array = findMsgFrwdngInfo(actor, domain);
		if (array != null) {
		    for (String name: array) {
			result.add(name);
		    }
		}
	    }
	}
	String s[] = new String[result.size()];
	return result.toArray(s);
    }


    /**
     * Send a message to all actors in a domain, using a specific domain for
     * communication.
     * The message delay and any message filtering will be determined by
     * the specified domain.
     * <p>
     * The message will not be delivered to the source (the current actor).
     * Otherwise it is equivalent to calling
     * send(msg, act, domain) where act is each actor in the domain dests.
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericDomain,GenericDomain)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param dests the destination domain
     * @param domain the domain determining message delay, message handling,
     *        and what message filter (if any) to use
     */
    protected final void send(Object msg,
			      D dests,
			      D domain)
    {
	Iterator<A> it = dests.actorSet().iterator();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) send(msg, actor, domain/*, null*/);
	}
    }


    /**
     * Find the names of the forwarding tables (MsgFrwdngInfo) used
     * by a call to
     * {@link #send(Object,GenericDomain,Set)}.
     * This method is intended primarily as a debugging aid. It can
     * be used to determine which subclasses of {@link GenericMsgFrwdngInfo}
     * to instrument.
     * @param dests the domain specifying the recipient actors
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     */
    public String[] findMsgFrwdngInfo(D dests,
				      Set<CommDomainType> commDomainTypes)
    {
	Iterator<A> it = dests.actorSet().iterator();
	Set<String> result = new HashSet<>();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) {
		String[] array = findMsgFrwdngInfo(actor, commDomainTypes);
		if (array != null) {
		    for (String name: array) {
			result.add(name);
		    }
		}
	    }
	}
	String s[] = new String[result.size()];
	return result.toArray(s);
    }

    /**
     * Send a message to all actors in a domain, ignoring some domains.
     * The delay and any message filter will be determined by the domain
     * chosen from those domains of which this actor is a member.  The
     * search is in the order given by the domains' priorities and the
     * first match that allows communication between actors is chosen.
     * In some simulation flavors, domains may represent constraints
     * that must be satisfied in order to successfully send a message.
     * Ignoring all of an actor's domains at a given priority level
     * will remove that constraint in this case.
     * <p>
     * The message will not be delivered to the source (the current
     * actor).  Otherwise it is equivalent to calling
     * send(msg, act, domain,commDomainTypes) where act is each actor
     * in the domain dests.
     * <P>
     * A list of the names of the {@link GenericMsgFrwdngInfo} objects
     * used can be generated by calling
     * {@link #findMsgFrwdngInfo(GenericDomain,Set)}. This list is
     * useful for debugging as it can determine which objects to instrument.
     * @param msg the message to send
     * @param domain the domain specifying the recipient actors
     * @param commDomainTypes the types of domains to allow; null if any
     *        domain is acceptable
     */
    protected void send(Object msg, D domain,
			Set<CommDomainType> commDomainTypes)
    {
	Iterator<A> it = domain.actorSet().iterator();
	while (it.hasNext()) {
	    A actor = it.next();
	    if (actor != this) send(msg, actor, commDomainTypes);
	}
    }

    // version including an intermediate hop (used by the GenericGroup class)
    // in the case where domains are not used and hence is null.
    @SuppressWarnings("unchecked")
    final void send(Object msg,
		    G intermediateHop,
		    GenericMsgRecipient<S,A,C,D,DM,F,G> dest,
		    long delay) 
    {
	A src = thisInstance();
	// Simulation sim = getSimulation();
	sim.scheduleEvent(new MessageSimulationEvent<S,A,C,D,DM,F,G>
			  (msg, src, null, intermediateHop, null, dest),
			  delay);
    }

    // private boolean sharedDomainMember = false;
    private DM sharedDomainMember = null;
    private DM domainMember;

    @SuppressWarnings("unchecked")
    private void setSharedDomainMember(DM sharedDomainMember, Iterator<A> ita) 
	throws IllegalStateException, IllegalArgumentException
    {
	if (sharedDomainMember == this.sharedDomainMember) {
	    // nothing to do as the domain has not changed.
	    return;
	}
	if (sharedDomainMember != null && !sharedDomainMember.isShared()) {
	    String n1 = getName();
	    String n2 = sharedDomainMember.getName();
	    throw new IllegalArgumentException
		(errorMsg("notSharedDomainMember", n1, n2));
	}
	if (sharedDomainMember != null) {
	    for (D d: domainMember.domainSet()) {
		if (sharedDomainMember.inDomain(d)) {
		    String n1 = getName();
		    String n2 = sharedDomainMember.getName();
		    String n3 = d.getName();
		    throw new IllegalStateException
			(errorMsg("domainConflict", n1, n2, n3));
		}
	    }
	    if (this.sharedDomainMember != null) {
		sharedDomainMember.register((A)this);
		for (D d: domainMember.domainSet()) {
		    this.sharedDomainMember.actorLeftDomain(d);
		    sharedDomainMember.actorJoinedDomain(d);
		}
		this.sharedDomainMember.deregister((A)this);
		for (D d: this.sharedDomainMember.domainSet()) {
		    for (C c: d.conditionSet()) {
			onConditionChange(c,
					  ConditionMode.OBSERVER_LEFT_DOMAIN,
					  d);
		    }
		}
		this.sharedDomainMember = sharedDomainMember;
	    } else {
		sharedDomainMember.register((A)this);
		for (D d: domainMember.domainSet()) {
		    sharedDomainMember.actorJoinedDomain(d);
		}
		this.sharedDomainMember = sharedDomainMember;
	    }
	    for (D d: this.sharedDomainMember.domainSet()) {
		for (C c: d.conditionSet()) {
		    onConditionChange(c,
				      ConditionMode.OBSERVER_JOINED_DOMAIN,
				      d);
		}
	    }
	} else {
	    if (this.sharedDomainMember != null) {
		for (D d: domainMember.domainSet()) {
		    this.sharedDomainMember.actorLeftDomain(d);
		}
		if (ita == null) this.sharedDomainMember.deregister((A)this);
		else this.sharedDomainMember.deregister((A)this, ita);
		for (D d: this.sharedDomainMember.domainSet()) {
		    for (C c: d.conditionSet()) {
			onConditionChange(c,
					  ConditionMode.OBSERVER_LEFT_DOMAIN,
					  d);
		    }
		}
		this.sharedDomainMember = null;
	    }
	}
    }

    /**
     * Configure a shared domain member.
     * If a shared domain member is in use and the argument is null,
     * a new private domain member will be provided and it will be
     * necessary to configure it using 
     * {@link 
     *   org.bzdev.drama.generic.GenericActor#joinDomain(org.bzdev.drama.generic.GenericDomain)
     *   joinDomain(GenericDomain)} or
     * {@link 
     *   org.bzdev.drama.generic.GenericActor#joinDomain(org.bzdev.drama.generic.GenericDomain,boolean)
     *   joinDomain(GenericDomain, boolean)}
     * @param sharedDomainMember the domain Member to share; null if a
     *        private domain member should be used
     * @exception IllegalStateException the actor and the shared domain member
     *            have a domain in common
     * @exception IllegalArgumentException the argument is not null and not
     *            a shared domain member
     */
    public void setSharedDomainMember(DM sharedDomainMember) 
	throws IllegalStateException, IllegalArgumentException
    {
	setSharedDomainMember(sharedDomainMember, null);
    }

    // for GenericDomainMember onDelete()
    void dropSharedDomainMember(Iterator<A> ita) {
	setSharedDomainMember(null, ita);
    }

   DM getDomainMember() {return domainMember;}

   // Note - will not return null if the domain member has not joined
   // domain d.  Rather, d is used to disambiguate the domain member to
   // to chose (sharedDomainMember or domainMember).

   DM getDomainMember(D d) {
       if (sharedDomainMember == null) {
	   return domainMember;
       } else {
	   if (domainMember.inDomain(d)) {
	       return domainMember;
	   } else {
	       return sharedDomainMember;
	   }
       }
   }



    /**
     * Get a set of the domains that the current actor is in with a
     * common ancestor domain.
     * The iterator for the returned set will return domains in the order
     * set by domain-specific criteria.
     * @param ancestor the ancestor domain
     * @return a set of domains that the current actor is in and that have
     *         the specified ancestor
     */
   public Set<D> getChildDomains(D ancestor) {
       return domainMember.getChildDomains(ancestor);
   }

    private Set<D> domains = 
	new HashSet<D>();

    /**
     * Join a domain.
     * Conditions will not be tracked.
     * The implementation calls <code>joinDomain(d, false)</code> so
     * normally this method should not be overridden.
     * @param d the domain to join
     * @return true on success; false on failure, including having already
     *         joined
     */
    public boolean joinDomain(D d) {
	return joinDomain(d, false);
    }

    /**
     * Join a domain, specifying whether to track conditions.
     * @param d the domain to join
     * @param trackConditions true if the actor should be notified when
     *        conditions that d has have changed; false otherwise
     * @return true on success; false on failure, including having already
     *         joined by the actor or a shared domain member
     */
    public boolean joinDomain(D d, boolean trackConditions) 
    {
	if (sharedDomainMember != null) {
	    if (sharedDomainMember.inDomain(d)) {
		return false;
	    } else {
		boolean result = domainMember.joinDomain(d, trackConditions);
		if (result) {
		    sharedDomainMember.actorJoinedDomain(d);
		}
		return result;
	    }
	} else {
	    return domainMember.joinDomain(d, trackConditions);
	}
    }


    void leaveDomainCleanup(D d) {
	if (sharedDomainMember != null) {
	    if (!sharedDomainMember.inDomain(d)) {
		sharedDomainMember.actorLeftDomain(d);
	    }
	}
    }


    /**
     * Leave a domain.
     * @param d the domain to leave
     * @return true on success; false on failure
     */
    public boolean leaveDomain(D d) {
	if (sharedDomainMember != null) {
	    if (sharedDomainMember.inDomain(d)) {
		return false;
	    }
	    boolean result = domainMember.leaveDomain(d);
	    if (result) {
		sharedDomainMember.actorLeftDomain(d);
	    }
	    return result;
	} else {
	    return domainMember.leaveDomain(d);
	}
    }

    /**
     * Determine if an actor's domain member is in a specific domain.
     * @param d the domain
     * @return true if the domain member is in the domain; false if not
     */
    public boolean inDomain(D d) {
	if (sharedDomainMember != null &&
	    sharedDomainMember.inDomain(d)) {
	    return true;
	}
	return domainMember.inDomain(d);
    }

    /**
     * Get a set of the domains an actor's domain member is in.
     * @return a set containing the domains.
     */
    public Set<D> domainSet() {
	if (sharedDomainMember != null) {
	    return new 
		DisjointSortedSetsUnion<D>(domainMember.getDomains(),
					   sharedDomainMember.getDomains());
	} else {
	    return domainMember.domainSet();
	}
    }

    /**
     * Respond to a change in conditions
     * This should be overridden by subclasses that react to the value
     * of a condition. The third argument (source) depends on the condition
     * mode:
     * <UL>
     *  <LI> for a mode of <code>CONDITION_DELETED</code>, the third
     *       argument is the condition that was deleted (the same as the
     *       first argument).
     *  <LI> for a mode of <code>DOMAIN_ADDED_CONDITION</code>, the
     *       third argument is the domain that added the condition.
     *  <LI> for a mode of <code>DOMAIN_REMOVED_CONDITION</code>, the
     *       third argument is the domain that added the condition.
     *  <LI> for a mode of <code>OBSERVER_ADDED_CONDITION</code>, the third
     *       argument is the condition observer (e.g., an actor or group)
     *       that added a condition.
     *  <LI> for a mode of <code>OBSERVER_REMOVED_CONDITION</code>, the third
     *       argument is the condition observer (e.g., an actor or group)
     *       that added a condition.
     *  <LI> for a mode of <code>OBSERVER_JOINED_DOMAIN</code>, the third
     *       argument is the domain an actor left
     *  <LI> for a mode of <code>OBSERVER_LEFT_DOMAIN</code>, the third argument
     *       is the domain that an actor left.
     *  <LI> for a mode of <code>OBSERVER_NOTIFIED</code>, the third
     *       argument is the condition that was changed (the same as the
     *       first argument).
     * </UL>
     * The default behavior is to do nothing.
     * @param condition the condition that changed
     * @param mode the condition mode
     * @param source the object responsible for the change
     * @see org.bzdev.drama.Actor
     */
    protected void onConditionChange(C condition, ConditionMode mode,
				     SimObject source)
    {
    }

    /**
     * Respond to a change in conditions
     * This should be overridden by subclasses that react to the value
     * of a condition if the default behavior - invoking 
     * onConditionChange(Condition,ConditionMode,SimObject) - is not
     * appropriate.
     * @param conditionMap a map keyed by conditions that changed.
     */
    protected void onConditionChange(Map<C,ConditionInfo> conditionMap) {
	for (Map.Entry<C,ConditionInfo> entry: conditionMap.entrySet()) {
	    C c = entry.getKey();
	    ConditionInfo info = entry.getValue();
	    onConditionChange(c, info.getMode(), info.getSource());
	}
    }

    /**
     * {@inheritDoc}
     * <P>
     * Defined in {@link GenericActor}:
     * <UL>
     *   <LI> the domains this object has joined.
     *   <LI> the conditions this object explicitly observes.
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
	Set<D> ds = domainSet();
	if (ds.isEmpty()) {
	    out.println(prefix +"domains: (none)");
	} else {
	    out.println(prefix +"domains:");
	    for (D d: ds) {
		out.println(prefix +"    " +d.getName()
			    +" (" +d.getClass().getName() +")");
	    }
	}
	Set<C> conds =  conditionSet();
	if (conds.isEmpty()) {
	    out.println(prefix + "explicit conditions: (none)");
	} else {
	    out.println(prefix + "explicit conditions:");
	    for (C c: conds) {
		out.println(prefix + "    " + c.getName());
	    }
	}
    }
}

//  LocalWords:  exbundle sim IllegalArgumentException getObject msg
//  LocalWords:  getObjectNames HashSet ConditionMode transformedMode
//  LocalWords:  drainMessageQueue doReceive wereQueued dest dests lt
//  LocalWords:  getSimulation commDomainTypes GenericGroup boolean
//  LocalWords:  sharedDomainMember notSharedDomainMember joinDomain
//  LocalWords:  domainConflict GenericDomain IllegalStateException
//  LocalWords:  GenericDomainMember onDelete domainMember SimObject
//  LocalWords:  trackConditions onConditionChange conditionMap src
//  LocalWords:  GenericActor iPrefix printName cdinfo getDestDomain
//  LocalWords:  GenericMsgFrwdngInfo getSourceDomain CommDomainInfo
//  LocalWords:  getAncestorDomain communicationMatch CommDomainType
//  LocalWords:  findCommDomain MsgFrwdngInfo findMsgFrwdngInfo
