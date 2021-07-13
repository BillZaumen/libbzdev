package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

import org.bzdev.util.DisjointSetsUnion;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Class for domains.
 * Domains are collections of actors and also are condition observers
 * so that domains are notified when the conditions they are
 * interested in change.
 * <P>
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
 * {@link #setMessageForwardingInfo(GenericMsgFrwdngInfo)} associates
 * a domain with an object that can determine the appropriate delays and
 * message filters.  When configured as a communication domain, a domain
 * must call one of the configureAsCommunicationDomain methods. These
 * tag the domain with an instance of
 * {@link org.bzdev.drama.common.CommDomainType} to indicate the type
 * of the communication domain.  Subclasses determine how this tag should
 * be used.  On may optionally provide a set of additional instances of
 * CommDomainType. The use of these is also left to subclasses.
 */
abstract public class GenericDomain <
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericTaskObject<S,A,C,D,DM,F,G> 
    implements Comparable<D>, CondObserver<C,D>

{
    D parent;

    private static String errorMsg(String key, Object... args) {
	return GenericSimulation.errorMsg(key, args);
    }

    /**
     * Get the parent domain.
     * The parent domain is set by a constructor.  A parent domain
     * must be a communication domain, and a domain with a parent
     * will be configured as a communication domain.
     * @return the parent domain; null if none exists
     */
    public D getParent() {
	return parent;
    }

    int childcount = 0;

    /**
     * Handle creation of a new child domain
     * If a reference to the child domain is kept, it should be
     * a weak reference so that it will not inhibit garbage collection
     * of the child.
     */
    protected void onChildAdd(D child) {}

    /**
     * Handle destruction of a child domain.
     * This allows any action performed by onChildAdd to be undone.
     */
    protected void onChildRemove(D child) {}

    /*
    protected void finalize() {
	if (parent != null) parent.onChildRemove(thisInstance());
    }
    */


    /**
     * Protected constructor without priority.
     * The priority defaults to Integer.MAX_VALUE.
     * @param sim the simulation
     * @param name the domain's name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericDomain(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	parent = null;
	this.priority = Integer.MAX_VALUE;
	actorUnion.addSet(unsharedActorSet);
	defaultMsgInfo = new GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>
	    (sim, "[defaultMessageForwardingInfo]", false);
	msgInfo = defaultMsgInfo;
    }

    /**
     * Protected constructor with priority.
     * @param sim the simulation
     * @param name the domain's name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericDomain(S sim, String name, boolean intern, int priority) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	parent = null;
	this.priority = priority;
	actorUnion.addSet(unsharedActorSet);
	defaultMsgInfo = new GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>
	    (sim, "[defaultMessageForwardingInfo]", false);
	msgInfo = defaultMsgInfo;
    }

    // used by GenericDomainFactory
    void setParent(D parent) throws IllegalArgumentException {
	this.parent = parent;
	if (parent != null) {
	    if (!parent.isCommunicationDomain()) {
		String n1 = parent.getName();
		String n2 = getName();
		throw new IllegalArgumentException
		    (errorMsg("parentNotCommDomain", n1, n2));
	    }
	    parent.onChildAdd(thisInstance());
	    parent.childcount++;
	}
    }


    /**
     * Protected constructor with parent and without priority.
     * The priority defaults to Integer.MAX_VALUE.
     * @param sim the simulation
     * @param name the domain's name
     * @param intern true if the object can be looked up by using the methods
     * @param parent the parent domain
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use or that a parent is not a communication domain
     */
    protected GenericDomain(S sim, String name, boolean intern, D parent) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.parent = parent;
	this.priority = Integer.MAX_VALUE;
	if (parent != null) {
	    if (!parent.isCommunicationDomain()) {
		String n1 = parent.getName();
		String n2 = getName();
		throw new IllegalArgumentException
		    (errorMsg("parentNotCommDomain", n1, n2));
	    }
	    configureAsCommunicationDomain(parent.getCommDomainType());
	    parent.onChildAdd(thisInstance());
	    parent.childcount++;
	}
	actorUnion.addSet(unsharedActorSet);
	defaultMsgInfo = new GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>
	    (sim, "[defaultMessageForwardingInfo]", false);
	msgInfo = defaultMsgInfo;
    }

    /**
     * Protected constructor with parent and priority.
     * @param sim the simulation
     * @param name the domain's name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @param parent the parent domain
     * @param priority determines the search order for domains (lowest
     *        priorities appear first)
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericDomain(S sim, String name, boolean intern, D parent,
			     int priority) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.parent = parent;
	this.priority = priority;
	if (parent != null) {
	    if (!parent.isCommunicationDomain()) {
		String n1 = parent.getName();
		String n2 = getName();
		throw new IllegalArgumentException
		    (errorMsg("parentNotCommDomain", n1, n2));
	    }
	    configureAsCommunicationDomain(parent.getCommDomainType());
	    parent.onChildAdd(thisInstance());
	    parent.childcount++;
	}
	actorUnion.addSet(unsharedActorSet);
	defaultMsgInfo = new GenericMsgFrwdngInfo<S,A,C,D,DM,F,G>
	    (sim, "[defaultMessageForwardingInfo]", false);
	msgInfo = defaultMsgInfo;
    }

    /**
     * Determine if this simulation object can be deleted.
     * A deleted object cannot be deleted (the default behavior of
     * SimObject), but in addition, a domain cannot be deleted if it
     * has child domains (i.e., other domains designate it as their
     * parent).
     * @return true if this object can be deleted; false otherwise

     */
    public boolean canDelete() {
	return super.canDelete() && (childcount == 0);
    }


    private boolean deleting = false;
    protected void onDelete() {
	deleting = true;
	Iterator<G>itg = groups.iterator();
	while (itg.hasNext()) {
	    G g = itg.next();
	    g.disFrom(thisInstance(), itg);
	    onLeftDomain(g);
	}
	Iterator<A>ita = unsharedActorSet.iterator();
	while (ita.hasNext()) {
	    A a = ita.next();
	    a.leaveDomainCleanup(thisInstance());
	    a.getDomainMember().leaveDom(thisInstance(), ita);
	}
	Iterator<DM> itdm = domainMembers.iterator();
	while (itdm.hasNext()) {
	    DM dm = itdm.next();
	    if (dm.isShared()) {
		trackingDomainMembers.remove(dm);
		actorUnion.removeSet(dm.actors);
	    } else if (dm.actor != null) {
		unsharedActorSet.remove(dm.actor);
		trackingActorSet.remove(dm.actor);
	    }
	    dm.leaveDom(thisInstance(), itdm); 
	}
	
	if (parent != null) {
	    parent.onChildRemove(thisInstance());
	    parent.childcount--;
	    parent = null;
	}
	impl.removeAllConditions();
	// clear all the tables - some but not all may
	// not be empty.
	groups.clear();
	unsharedActorSet.clear();
	trackingActorSet.clear();
	domainMembers.clear();
	trackingDomainMembers.clear();
	// finish by invoking onDelete for our
	// superclass.
	super.onDelete();
    }


    int priority;

    // used by GenericDomainFactory.
    void setPriority(int priority) {
	this.priority = priority;
    }

    /**
     * Get the priority.
     * Priorities determine a search order in determining if
     * actors can communicate with each other. Lower numbers are
     * tried first.
     * @return the priority.
     * @see GenericSimulation
     * @see GenericSimulation#findCommDomain
     */
    public int getPriority() {return priority;}


    /**
     * Compare two Domains.
     * The order in which domains are searched is dependent on their
     * priorities. Those with higher priority values are searched later,
     * following the usual convention for priority queues. If the priority
     * values are the same, the search order is based on the domain's name.
     * The DomainMember class uses a TreeSet so that an iterator will
     * will return members in the proper order.
     * @param object a domain
     * @return s negative integer  if this domain should appear before object;
     *         0 if this domain and object have the same priority and name;
     *         a positive integer if this domain should appear after object
     * @see GenericSimulation#findCommDomain
     */
    public int compareTo(D object) {
	int objPriority = object.getPriority();
	if (this.priority == objPriority) {
	    return getName().compareTo(object.getName());
	} else {
	    return this.priority - objPriority;
	}
    }


    Set<G> groups = new HashSet<G>();

    void addGroup(G g) {
	if (groups.add(g)) {
	    onJoinedDomain(g);
	}
    }

    void removeGroup(G g) {
	if (deleting == false) {
	    if (groups.remove(g)) {
		onLeftDomain(g);
	    }
	}
    }

    /**
     * Determine if this domain contains a specified group.
     * @param g the group
     * @return true if g has joined this domain; false otherwise
     */
    public boolean containsGroup(G g) {
	return groups.contains(g);
    }


    /**
     * Get the group set.
     * @return a set of the groups that have joined this domain
     */
    public Set<G> groupSet() {
	return Collections.unmodifiableSet(groups);
    }

    /**
     * Get the number of groups that have joined this domain.
     * This method is slightly faster than calling groupSet().size().
     * @return the number of groups that have joined this domain
     */
    public int groupSetSize() {
	return groups.size();
    }


    private boolean communicationDomain = false;
    private CommDomainType commDomainType = null;
    private boolean isCommunicationDomainCalled = false;
    private boolean addedCommDomainTypeSet = false;
    private Set<CommDomainType> commDomainTypeSet = null;
    /**
     * Determine if a domain is a communication domain
     * @return true if the domain is a communication domain; false otherwise
     */
    public final boolean isCommunicationDomain() {
	isCommunicationDomainCalled = true;
	
	return communicationDomain;
    }

    /**
     * Get the communication-domain type.
     * @return the communication-domain type; null if there is none
     */
    public CommDomainType getCommDomainType() {
	isCommunicationDomainCalled = true;
	return commDomainType;
    }


    /**
     * Get the set of communication-domain types.
     * A value of null will be returned in this is not a communication
     * domain.  The set returned includes both the primary communication-domain
     * type and any additional communication-domain types that may be defined.
     * @return the set of communication-domain types; null if there is none
     */
    public Set<CommDomainType> getCommDomainTypeSet() {
	isCommunicationDomainCalled = true;
	addedCommDomainTypeSet = true;
	return commDomainTypeSet;
    }


    /**
     * Add additional communication-domain types.
     * Additional communication domains are not used by this package, nor
     * by the package <code>org.bzdev.drama</code>. They are provided for use
     * by other simulation packages based on this package. The intention is
     * for additional types to be treated as properties that may have to
     * be present for messages to be sent.
     * This method cannot be called twice or after the two-argument variant of
     * {@link #configureAsCommunicationDomain(CommDomainType,Set)}
     * is called.
     * @param typeSet a set of communication-domain types
     * @exception IllegalStateException called at the wrong time
     */
    protected void addCommDomainTypeSet(Set<CommDomainType> typeSet) {
	if (addedCommDomainTypeSet) {
	    throw new IllegalStateException
		(errorMsg("calledTooLate1", getName()));
	    // ("addCommDomainTypeSet called too late");
	}
	if (!isCommunicationDomainCalled) {
	    throw new IllegalStateException
		(errorMsg("notCommDomain1", getName()));
	    // ("addCommDomainTypeSet called but not communication domain ");
	}

	LinkedHashSet<CommDomainType> set = new LinkedHashSet<>();
	set.add(commDomainType);
	set.addAll(typeSet);
	addedCommDomainTypeSet = true;
	commDomainTypeSet = Collections.unmodifiableSet(set);
    }

    /**
     * Configure a domain as a communication domain given a set of
     * communication-domain types.
     * The first argument provides the domain's communication-domain type.
     * Additional communication domains are not used by this package, nor
     * by the package <code>org.bzdev.drama</code>. They are provided for use
     * by other simulation packages based on this package.  The intention is
     * for additional types to be treated as properties that may have to
     * be present for messages to be sent.
     * This must be called before isCommunicationDomain is called.
     * Methods with this name may be called  only once per domain.
     * @param type the type of the communication domain
     * @param typeSet a set of additional communication-domain types
     * @exception IllegalStateException this method was called after a
     *            call to {@link #isCommunicationDomain() isCommunicationDomain}
     * @see org.bzdev.drama.common.CommDomainType#typeSet(String...)
     */
    protected void configureAsCommunicationDomain(CommDomainType type,
						  Set<CommDomainType> typeSet)
    {
	if (isCommunicationDomainCalled) {
	    if (commDomainType != type || addedCommDomainTypeSet) {
		throw new IllegalStateException(errorMsg("tooLate", getName()));
	    }
	}
	if (type == null) {
	    throw new IllegalArgumentException
		(errorMsg("mustNotBeNull", getName()));
	}
	communicationDomain = true;
	commDomainType = type;
	isCommunicationDomainCalled = true;
	LinkedHashSet<CommDomainType> set = new LinkedHashSet<>();
	set.add(commDomainType);
	addedCommDomainTypeSet = true;
	if (typeSet != null) {
	    set.addAll(typeSet);
	}
	commDomainTypeSet = Collections.unmodifiableSet(typeSet);
    }

    /**
     * Configure a domain as a communication domain.
     * This must be called before isCommunicationDomain is called.
     * Methods with this name may be called  only once per domain.
     * @param type the type of the communication domain
     * @exception IllegalStateException this method was called after a
     *            call to {@link #isCommunicationDomain() isCommunicationDomain}
     * @exception IllegalArgumentException the argument was null
     */
    protected void configureAsCommunicationDomain(CommDomainType type) {
	if (isCommunicationDomainCalled) {
	    if (commDomainType == type) return;
	    throw new IllegalStateException(errorMsg("tooLate", getName()));
	}
	if (type == null) {
	    throw new IllegalArgumentException
		(errorMsg("mustNotBeNull", getName()));
	}
	communicationDomain = true;
	commDomainType = type;
	isCommunicationDomainCalled = true;
	HashSet<CommDomainType> set = new HashSet<>();
	set.add(commDomainType);
	commDomainTypeSet = Collections.unmodifiableSet(set);
    }

    /**
     * Configure a domain as a communication domain given the name of
     * a communication domain type.
     * This must be called before isCommunicationDomain is called and
     * the implementation calls
     * {@link #configureAsCommunicationDomain(CommDomainType) configureAsCommunicationDomain(CommDomainType)}.
     * @param typeName the name of the communication domain type
     * @exception IllegalArgumentException the argument was null
     * @exception IllegalStateException this method was called after a
     *            call to {@link #isCommunicationDomain() isCommunicationDomain}
     */
    protected void configureAsCommunicationDomain(String typeName) {
	if (typeName == null) {
	    throw new IllegalArgumentException
		(errorMsg("mustNotBeNull", getName()));
	}
	CommDomainType type = CommDomainType.findType(typeName);
	configureAsCommunicationDomain(type);
    }

    private D getFirstChildDomain(DM dm, D domain) {
	Iterator<D> it = dm.getChildDomains(domain).iterator();
	if (it.hasNext()) {
	    return it.next();
	} else {
	    return null;
	}
    }

    private D getFirstChildDomain(G g, D domain) {
	Iterator<D> it = g.getChildDomains(domain).iterator();
	if (it.hasNext()) {
	    return it.next();
	} else {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    private CommDomainInfo<D> trivCommDomainInfo() {
	return new CommDomainInfo<D>((D)this, (D)this, (D)this);
    }


    /**
     * Check if two actors can communicate if at least one of them is in this
     * domain.
     * The default behavior is to delegate the decision to
     * {@link #communicationMatch(GenericDomainMember,GenericDomainMember) communicationMatch(dm1,dm2)} where dm1 is the domain member for a1 and
     * dm2 is the domain member for a2.
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param a1 the  actor sending a message
     * @param a2 the actor receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericActor)
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericActor,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(A a1, A a2) {
	D xme = thisInstance();
	DM dm1 = a1.getDomainMember(xme);
	DM dm2 = a2.getDomainMember(xme);
	return communicationMatch(dm1, dm2);
    }

    /**
     * Check if communication between actors associated with two domain members
     * is allowed when at least on of the domain members is contained by this
     * domain.
     * Check if this domain allows communication between actors given their
     * domain members.
     * The default behavior is to
     * <UL>
     *   <LI> return null if this domain is not a communication domain.
     *   <LI> return a non-null value if dm1 and dm2 are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericDomainMember,GenericGroup) this.communicationMatchByDomain(dm1, dm2)}
     *        returns true.
     *   <LI> return a null value if dm1 and dm2 are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericDomainMember,GenericGroup) this.communicationMatchByDomain(dm1, dm2)}
     *        returns false.
     *   <LI> Perform a search if either dm1 or dm2 (but not both)
     *        is a member of this domain; otherwise return null
     * </UL>
     * There are two search options.
     * <UL>
     *    <LI> If dm1 is a member of the current domain, the following
     *         search is performed:
     *         <OL>
     *             <LI> The search starts with this domain. A test domain
     *                  d is set to null, and a "parent" domain p is set to
     *                  the current domain.
     *             <LI> if p is  null or p is not a communication domain, the
     *                  this portion of the search terminates.
     *             <LI> If p contains dm2, d is set to p
     *             <LI> If dm2 is a member of a domain that has p as an
     *                  ancestor or if d has been set, then the value of
     *                  {@link #communicationMatchByDomainAncestor(GenericDomainMember,GenericDomainMember,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(dm1,dm2,this,d)}
     *                  is returned (a null value of d is replaced with a
     *                  child domain of p that contains dm2 during this
     *                  call); otherwise
     *                  repeat from step 2 after replacing p with its parent.
     *         </OL>
     *    <LI> If the previous search failed or was not performed
     *        and if dm2 is a member of the current domain, the following
     *        search is performed:
     *         <OL>
     *             <LI> p is set to the parent of this domain.
     *             <LI> if p is null or p is not a communication domain,
     *                  null is returned.
     *             <LI> if p contains domain member dm1, the return value is
     *                  {@link #communicationMatchByDomainAncestor(GenericDomainMember,GenericDomainMember,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(dm1,dm2,p,this)}
     *                  otherwise repeat from Step 2 after replacing p with
     *                  its parent.
     *         </OL>
     *    <LI> If neither search succeeded, null is returned.
     * <UL>
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param dm1 the domain member of an actor sending a message
     * @param dm2 the domain member of an actor  receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericActor)
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericActor,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(DM dm1, DM dm2) {
	if (!communicationDomain) return null;
	boolean cdm1 = containsDomainMember(dm1);
	boolean cdm2 = containsDomainMember(dm2);
	if (cdm1 && cdm2) {
	    // return communicationMatchByDomain(dm1, dm2);
	    if (communicationMatchByDomain(dm1, dm2)) {
		return trivCommDomainInfo();
	    } else {
		return null;
	    }
	}
	D xme = thisInstance();
	if (cdm1) {
	    D p = xme;
	    D d = null;
	    while (p != null && p.isCommunicationDomain()) {
		if(p.containsDomainMember(dm2)) d = p;
		if (dm2.getChildDomains(p) != null || d != null) {
		    if (d == null) d = getFirstChildDomain(dm2, p);
		    return p.communicationMatchByDomainAncestor(dm1, dm2,
								xme, d);
		}
		p = p.parent;
	    }
	} else if (cdm2) {
	    D p = parent;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsDomainMember(dm1)) {
		    return p.communicationMatchByDomainAncestor(dm1, dm2,
								p, xme);
		}
		p = p.parent;
	    }
	}
	return null;
    }

    /**
     * Determine if there is a communication match for two domain members.
     * A match implies that the actors associated with the first domain member
     * can send a message to the actors associated with the second domain
     * member.  This method is called when the two domain members are
     * members of this domain.  The default behavior (which may be overridden
     * by a subclass) is to return true, which allows communication between
     * actors whose domain members have joined this domain.  The effect of
     * message filters is not included.
     * <P>
     * This method can be overridden to make communication more restrictive.
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param dm1 the first domain member
     * @param dm2 the second domain member
     * @return true if two domain members match for communication; 
     *         false otherwise
     * 
     */
    protected boolean communicationMatchByDomain(DM dm1, DM dm2) {
	return true;
    }


    /**
     * Create a CommDomainInfo object.
     * This method is provided for convenience - it allows the caller to
     * ignore type parameters and merely calls a constructor.

     * @param srcDomain the domain for a message source
     * @param ancestorDomain the closest common ancestor for the source
     *        and destination domains
     * @param destDomain the domain for a message destination
     */
    protected final CommDomainInfo<D> createCommDomainInfo(D srcDomain,
							   D ancestorDomain,
							   D destDomain)
    {
	return new CommDomainInfo<D>(srcDomain, ancestorDomain, destDomain);
    }


    /**
     * Determine if there is a communication match for two domain members
     * with this domain as a common ancestor domain.
     * A match implies that the actors associated with the first domain member
     * can send a message to the actors associated with the second domain
     * member.  This method is called by the default implementations
     * of {@link #communicationMatch(GenericDomainMember,GenericDomainMember)},
     * whose documentation describes how this method is used.
     * One should note, that the domain on which this method is called
     * is a common ancestor of the two domains passed as arguments,
     * that dm1 is a member of domain1, and dm2 is a member of domain2.
     * The common ancestor's implementation of this method is the one that
     * determines when communication is possible for the source and
     * destination actors.
     * <P>
     * The default implementation always returns a non-null value. Subclasses
     * can override this to provide different behavior (e.g., to disallow
     * communication between specific pairs of domain members).
     * The effect of message filters is not included. When a non null
     * value is returned, the returned value should be
     * createCommDomainInfo(domain1, this, domain2).
     * When communication between two domain members in different domains
     * is not allowed, this method, when called on the closest common
     * ancestor domain, should return null.
     * <P>
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param dm1 the domain member of an actor sending a message
     * @param dm2 the  domain member of an actor receiving a message
     * @param domain1 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing dm1 or
     *        the ancestor of a domain containing dm1
     * @param domain2 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing dm2 or
     *        the ancestor of a domain containing dm2; otherwise null,
     *        in which case a domain containing dm2 and that is either
     *        this domain or a domain that has this domain as an ancestor
     *        will be chosen.
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     */
    protected CommDomainInfo<D> communicationMatchByDomainAncestor(DM dm1, DM dm2,
						D domain1, D domain2)
    {

	return createCommDomainInfo(domain1, thisInstance(), domain2);
    }


    /**
     * Check if this domain allows an actor to communication with a group
     * given that either the actor or the group is a member of this domain.
     * This method merely calls
     * {@link #communicationMatch(GenericDomainMember,GenericGroup) communicationMatch(dm,g)}
     * where dm is the domain member for actor ac.
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param ac the actor for which communications will be started
     * @param g the group receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericGroup)
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericGroup,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(A ac, G g) {
	DM dm1 = ac.getDomainMember(thisInstance());
	return communicationMatch(dm1, g);
    }


    /**
     * Check if this domain allows a domain member's actors to communication
     * with a group given that the domain member or the group is a member of
     * this domain.
     * The default implementation will
     * <UL>
     *   <LI> return null if this domain is not a communication domain
     *        or if neither dm1 nor g is are members of this domain.
     *   <LI> return a non-null value if dm1 and g are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericDomainMember,GenericGroup) communicationMatchByDomain(dm1, g)}
     *        returns true.
     *   <LI> return a null value if dm1 and g are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericDomainMember,GenericGroup) communicationMatchByDomain(dm1, g)}
     *        returns false.
     *   <LI> perform a search if either dm1 or g (but not both) are
     *        members of this domain.
     * </UL>
     * The search has two cases.
     * <UL>
     *   <LI> The first search covers the case where dm1 is a member of
     *        this domain:
     *        <OL>
     *           <LI> p is set to this domain and d is set to null.
     *           <LI> if p is null or p is not a communication domain,
     *                the first search fails.
     *           <LI> g is a member of domain p, d is set to p.
     *           <LI> if p is an ancestor of a domain containing g or
     *                if d is not null, then the value of
     *                {@link #communicationMatchByDomainAncestor(GenericDomainMember,GenericGroup,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(dm1, g, this, d)}
     *                is returned (a null value of d is replaced with a
     *                  child domain of p that contains g during this
     *                  call); otherwise the search continues at step
     *                2 with p set to its parent.
     *        </OL>
     *    <LI> If the first search fails and g is a member of this domain,
     *         then the second search is started:
     *         <OL>
     *           <LI> p is set to the parent of this domain.
     *           <LI> if p is null or p is not a communication domain,
     *                the second search fails.
     *           <LI> dm1 is a member of p, then the value of
     *                {@link #communicationMatchByDomainAncestor(GenericDomainMember,GenericGroup,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(dm1, g, p, this)}
     *                is returned; otherwise the search continues at step
     *                2 with p set to its parent.
     *         </OL>
     * </UL>
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param dm1 the domain member of the agent for which 
     *        communications will be started
     * @param g the group receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericGroup)
     * @see GenericSimulation#findCommDomain(GenericActor,Set,GenericGroup,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(DM dm1, G g) {
	if (!communicationDomain) return null;
	boolean cdm1 = containsDomainMember(dm1);
	boolean cg = containsGroup(g);
	if (cdm1 && cg) {
	    if (communicationMatchByDomain(dm1, g)) {
		return trivCommDomainInfo();
	    } else {
		return null;
	    }
	}
	D xme = thisInstance();
	if (cdm1) {
	    D p = xme;
	    D d = null;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsGroup(g)) d = p;
		if (g.getChildDomains(p) != null || d != null) {
		    if (d == null) d = getFirstChildDomain(g, p);
		    return p.communicationMatchByDomainAncestor(dm1, g,
								xme, d);
		}
		p = p.parent;
	    }
	} else if (cg) {
	    D p = parent;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsDomainMember(dm1)) {
		    return p.communicationMatchByDomainAncestor(dm1, g,
								p, xme);
		}
		p = p.parent;
	    }
	}
	return null;
    }


    /**
     * Determine if there is a communication match for a domain member 
     * and a group.
     * A match implies that the actors associated with the domain member
     * can send a message to a group.  This method is called when the 
     * the domain member has joined this domain and the group is associated
     * with this domain.  The default implementation always returns true.
     * The effect of message filters is not included.
     * <P>
     * This method can be overridden to make communication more restrictive.
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param dm the  domain member
     * @param g the group
     * @return true if two domain members match for communication; 
     * false otherwise
     * 
     */
    protected boolean communicationMatchByDomain(DM dm, G g) {
	return true;
    }

    /**
     * Determine if there is a communication match for a domain member and
     * a group given a parent domain.
     * A match implies that the actors associated with the first domain member
     * can send a message to the group.
     * This method is called by the default implementations
     * of {@link #communicationMatch(GenericDomainMember,GenericGroup)},
     * whose documentation describes how this method is used.
     * One should note, that the domain on which this method is called
     * is a common ancestor of the two domains passed as arguments,
     * that dm is a member of domain1, and g is a member of domain2.
     * The common ancestor's implementation of this method is the one that
     * determines when communication is possible for the source and
     * destination actors.
     * <P>
     * The default implementation always returns a non-null value. Subclasses
     * can override this to provide different behavior (e.g., to disallow
     * communication between specific pairs of domain members).
     * The effect of message filters is not included. When a non null
     * value is returned, the returned value should be
     * createCommDomainInfo(domain1, this, domain2).
     * When communication between a domain member and a group in a
     * different domain is not allowed, this method, when called on
     * the closest common ancestor domain, should return null.
     * <P>
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param dm the domain member of an actor sending a message
     * @param g the  domain member of an actor receiving a message
     * @param domain1 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing dm1 or
     *        the ancestor of a domain containing dm1
     * @param domain2 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing g or
     *        the ancestor of a domain containing g; otherwise null,
     *        in which case a domain containing g and that is either
     *        this domain or a domain that has this domain as an ancestor
     *        will be chosen.
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     */
    protected CommDomainInfo<D> communicationMatchByDomainAncestor(DM dm, G g,
						D domain1, D domain2) 
    {
	return createCommDomainInfo(domain1, thisInstance(), domain2);
    }

    /**
     * Check if this domain member allows a group to communicate with
     * another group given that at least one of these two groups are members
     * of this domain.
     * The default implementation behaves as follows:
     * <UL>
     *   <LI> return null if this domain is not a communication domain
     *        or if neither g1 nor g2 is are members of this domain.
     *   <LI> return a non-null value if g1 and g2 are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericGroup,GenericGroup) this.communicationMatchByDomain(g1, g2)}
     *        returns true.
     *   <LI> return a null value if g1 and g2 are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericGroup,GenericGroup) this.communicationMatchByDomain(g1, g2)}
     *        returns false.
     *   <LI> perform a search if either g1 or g2 (but not both) are
     *        members of this domain.
     * </UL>
     * There are two cases for the search:
     * <UL>
     *    <LI> The first search covers the case where  g1 is a member of
     *         this domain. The search algorithm is the following.
     *         <OL>
     *           <LI> Set p to this domain and d to null.
     *           <LI> If p is null or p is not a communication domain,
     *                this search fails.
     *           <LI> If g2 is a member of p, the d is set to p.
     *           <LI> if p is an ancestor of a domain containing g2 or if
     *                d is not null, then the value of
     *                {@link #communicationMatchByDomainAncestor(GenericGroup,GenericGroup,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(g1,g2,this,d)}
     *                is returned (a null value of d is replaced with a
     *                  child domain of p that contains g2 during this
     *                  call); otherwise stop 2 is repeated with p set
     *                to its parent.
     *         </OL>
     *    <LI> If the first search failed or did not occur and if
     *         cg2 is a member of this domain, then the second
     *         search is performed. The algorithm for this second
     *         search is the following.
     *         <OL>
     *           <LI> Set p to the parent of this domain.
     *           <LI> If p is null or p is not a communication domain,
     *                return null.
     *           <LI> if p is the ancestor of one of g1's domains, then
     *                the value of
     *                {@link #communicationMatchByDomainAncestor(GenericGroup,GenericGroup,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(g1,g2,p,this)}
     *                is returned; otherwise step 2 is repeated with p set
     *                to its parent.
     *         </OL>
     * </UL>
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param g1 the group for which communications will be started
     * @param g2 the group  receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericGroup)
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericGroup,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(G g1, G g2) {
	if (!communicationDomain) return null;
	boolean cg1 = containsGroup(g1);
	boolean cg2 = containsGroup(g2);
	if (cg1 && cg2) {
	    if(communicationMatchByDomain(g1, g2)) {
		return trivCommDomainInfo();
	    } else {
		return null;
	    }
	}
	D xme = thisInstance();
	if (cg1) {
	    D p = xme;
	    D d = null;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsGroup(g2)) d = p;
		if (g2.getChildDomains(p) != null || d != null) {
		    if (d == null) d = getFirstChildDomain(g2, p);
		    return p.communicationMatchByDomainAncestor(g1, g2,
								xme, d);
		}
		p = p.parent;
	    }
	} else if (cg2) {
	    D p = parent;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsGroup(g1)) {
		    return p.communicationMatchByDomainAncestor(g1, g2,
								p, xme);
		}
		p = p.parent;
	    }
	}
	return null;
    }

    /**
     * Check if this domain allows a group to relay a message to another group.
     * The default implementation, which may be overridden by a subclass,
     * always returns true. The caller tests if this domain is a communication
     * domain and that g1 and g2 are associated with this domain.
     * The effect of message filters is not included.
     * <P>
     * This method can be overridden to make communication more restrictive.
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param g1 the group relaying a message
     * @param g2 the group receiving a message
     * @return true if a group may relay a message; false otherwise
     */
    protected boolean communicationMatchByDomain(G g1, G g2) {
	return true;
    }

    /**
     * Check if this domain allows a group to relay a message to another group
     * via a parent domain.
     * A match implies that the actors associated with the first domain member
     * can send a message to the group.
     *   This method is called by the default implementations
     * of {@link #communicationMatch(GenericGroup,GenericGroup)},
     * whose documentation describes how this method is used.
     * One should note, that the domain on which this method is called
     * is a common ancestor of the two domains passed as arguments,
     * that g1 is a member of domain1, and g2 is a member of domain2.
     * The common ancestor's implementation of this method is the one that
     * determines when communication is possible for the source and
     * destination actors.
     * <P>
     * The default implementation always returns a non-null value. Subclasses
     * can override this to provide different behavior (e.g., to disallow
     * communication between specific pairs of domain members).
     * The effect of message filters is not included. When a non null
     * value is returned, the returned value should be
     * createCommDomainInfo(domain1, this, domain2).
     * When communication between two groups in
     * different domains is not allowed, this method, when called on
     * the closest common ancestor domain, should return null.
     * <P>
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param g1 the domain member of an actor sending a message
     * @param g2 the  domain member of an actor receiving a message
     * @param domain1 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing g1 or
     *        the ancestor of a domain containing g1
     * @param domain2 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing g2 or
     *        the ancestor of a domain containing 2; otherwise null,
     *        in which case a domain containing 2 and that is either
     *        this domain or a domain that has this domain as an ancestor
     *        will be chosen.
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     */
    protected CommDomainInfo<D> communicationMatchByDomainAncestor(G g1, G g2,
						D domain1, D domain2) 
    {
	return createCommDomainInfo(domain1, thisInstance(), domain2);
    }


    /**
     * Check if this domain allows a group to  communication with an actor
     * given that either the group or the actor are members of this domain.
     * This method just calls
     * {@link #communicationMatch(GenericGroup,GenericDomainMember) communicationMatch(g,dm)} with dm set to a's domain member.
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param g the group for which communications will be started
     * @param a the actor receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericActor)
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericActor,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(G g, A a) {
	DM dm = a.getDomainMember(thisInstance());
	return communicationMatch(g, dm);
    }


    /**
     * Check if this domain allows a group to communicate with actors
     * associated with a domain member given that either the group or
     * the domain member or both are members of this domain.
     * The default implementation will
     * <UL>
     *   <LI> return null if this domain is not a communication domain
     *        or if neither g nor dm is are members of this domain.
     *   <LI> return a non-null value if g and dm are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericGroup,GenericDomainMember) this.communicationMatchByDomain(g, dm)}
     *        returns true.
     *   <LI> return a null value if dm and g are members of
     *        this domain and
     *        {@link #communicationMatchByDomain(GenericGroup,GenericDomainMember) this.communicationMatchByDomain(g, dm)}
     *        returns false.
     *   <LI> perform a search if either g or dm (but not both) are
     *        members of this domain.
     * </UL>
     * There are two cases for the search:
     * <UL>
     *    <LI> If this domain contains g, then the following algorithm is
     *         used.
     *         <OL>
     *            <LI> The variable p is set to this domain and d is set
     *                 to null.
     *            <LI> If p is null or p is not a communication domain, then
     *                 the first case fails.
     *            <LI> If p contains the domain member dm, the d = p.
     *            <LI> If p is an ancestor of a domain that contains dm,
     *                 or if d is not null, then the value of
     *                 {@link #communicationMatchByDomainAncestor(GenericGroup,GenericDomainMember,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(g,dm,this,d)}
     *                 is returned (a null value of d is replaced with a
     *                  child domain of p that contains dm during this
     *                  call); otherwise Step 2 is repeated with p set
     *                 to its parent.
     *         </OL>
     *    <LI> If this domain contains dm or if the first case failed, then
     *         the following algorithm is used.
     *         <OL>
     *            <LI> The variable p is set to this domain's parent.
     *            <LI> if p is null or if p is not a commune notion domain,
     *                 null is returned.
     *            <LI> if p contains the group g, the the value of
     *                 {@link #communicationMatchByDomainAncestor(GenericGroup,GenericDomainMember,GenericDomain,GenericDomain) p.communicationMatchByDomainAncestor(g,dm,p,this)}
     *                 is returned; otherwise Step 2 is repeated with p set to
     *                 its parent.
     *         </OL>
     * </UL>
     * <P>
     * The {@link GenericSimulation} methods named
     * <code>findCommDomain</code> uses <code>communicationMatch</code>
     * methods as part of their implementations, and constitute the
     * only use of the <code>communicationMatch</code> in this package.
     * <P>
     * To change the behavior of this method, one should override
     * the <code>communicationMatchByDomain</code> and
     * <code>communicationMatchByDomainAncestor</code> methods or override
     * the <code>findCommDomain</code> methods in {@link GenericSimulation}
     * instead.
     * @param g the group for which communications will be started
     * @param dm the domain member for the actor receiving a message
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericActor)
     * @see GenericSimulation#findCommDomain(GenericGroup,Set,GenericActor,GenericDomain)
     */
    protected CommDomainInfo<D> communicationMatch(G g, DM dm) {
	if (!communicationDomain) return null;
	boolean cg = containsGroup(g);
	boolean cdm = containsDomainMember(dm);
	if (cg && cdm) {
	    if (communicationMatchByDomain(g, dm)) {
		return trivCommDomainInfo();
	    } else {
		return null;
	    }
	}
	D xme = thisInstance();
	if (cg) {
	    D p = xme;
	    D d = null;
	    while (p != null && p.isCommunicationDomain()) {
		if(p.containsDomainMember(dm)) d = p;
		if (dm.getChildDomains(p) != null || d != null) {
		    if (d == null) d = getFirstChildDomain(dm, p);
		    return p.communicationMatchByDomainAncestor(g, dm,
								xme, d);
		}
		p = p.parent;
	    }
	} else if (cdm) {
	    D p = parent;
	    while (p != null && p.isCommunicationDomain()) {
		if (p.containsGroup(g)) {
		    return p.communicationMatchByDomainAncestor(g, dm,
								p, xme);
		}
		p = p.parent;
	    }
	}
	return null;
    }

    /**
     * Check if this domain allows a group to relay messages to the
     * actors associated with a domain member dm.
     * This method will be called only when dm has joined this domain
     * and g is associated with this domain.
     * The effect of message filters is not included.
     * <P>
     * This method can be overridden to make communication more restrictive.
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param g the group
     * @param dm the domain member
     * @return true if messages can be relayed; false otherwise
     */
    protected boolean communicationMatchByDomain(G g, DM dm) {
	return true;
    }

    /**
     * Check if this domain allows a group to relay a message to the
     * actor(s) with a specific domain member.
     * A match implies that the group
     * can send a message to the actors associated with the domain member's
     * actors.
     * This method is called by the default implementations
     * of {@link #communicationMatch(GenericGroup,GenericDomainMember)},
     * whose documentation describes how this method is used.
     * One should note, that the domain on which this method is called
     * is a common ancestor of the two domains passed as arguments,
     * that g is a member of domain1, and dm is a member of domain2.
     * The common ancestor's implementation of this method is the one that
     * determines when communication is possible for the source and
     * destination actors.
     * <P>
     * The default implementation always returns a non-null value. Subclasses
     * can override this to provide different behavior (e.g., to disallow
     * communication between specific pairs of domain members).
     * The effect of message filters is not included. When a non null
     * value is returned, the returned value should be
     * createCommDomainInfo(domain1, this, domain2).
     * When communication between a group and a domain member in a
     * different domain is not allowed, this method, when called on
     * the closest common ancestor domain, should return null.
     * <P>
     * The only methods from this package that call this method are the
     * <code>communicationMatch</code> methods defined by this class.
     * @param g the domain member of an actor sending a message
     * @param dm the  domain member of an actor receiving a message
     * @param domain1 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing g or
     *        the ancestor of a domain containing g
     * @param domain2 either this domain or a domain that has this domain as
     *        an ancestor and that is also a domain containing dm or
     *        the ancestor of a domain containing dm; otherwise null,
     *        in which case a domain containing dm and that is either
     *        this domain or a domain that has this domain as an ancestor
     *        will be chosen.
     * @return a communication-domain-info object if communication is
     *         allowed; null otherwise
     */
    protected CommDomainInfo<D> communicationMatchByDomainAncestor(G g, DM dm,
						       D domain1, D domain2)
    {
	return createCommDomainInfo(domain1, thisInstance(), domain2);
    }


    Set<DM> domainMembers = 
	new HashSet<DM>();
    Set<DM> trackingDomainMembers = 
	new HashSet<DM>();

    // domain for actors that did not join via a shared domain member
    Set<A> unsharedActorSet = new HashSet<A>();
    Set<A> trackingActorSet = new HashSet<A>();
    DisjointSetsUnion<A> actorUnion = new DisjointSetsUnion<A>();

    /**
     * Note that an actor has joined this domain, either directly
     * or due to being part of a shared domain that has joined or
     * is joining this domain.
     * <P>
     * The default method does nothing. It can be overridden when
     * some domain-specific action is needed when an actor joins
     * this domain. It is a good practice when overriding this
     * method to call super.onJoinedDomain with the same
     * arguments.
     * @param actor the actor that is joining this domain
     * @param tracksCondition true if the actor will be configured to
     *        obtain notifications when a condition associated with this
     *        domain changes; false otherwise
     */
    protected void onJoinedDomain(A actor, boolean tracksCondition) {}

    /**
     * Note that an actor has left this domain, either directly
     * or due to being part of a shared domain that has just left
     * this domain.
     * <P>
     * The default method does nothing. It can be overridden when
     * some domain-specific action is needed when an actor leaves
     * this domain. It is a good practice when overriding this
     * method to call super.onJoinedDomain with the same
     * arguments.
     */
    protected void onLeftDomain(A actor) {}


    /**
     * Note that a group has joined this domain.
     * <P>
     * The default method does nothing. It can be overridden when
     * some domain-specific action is needed when a group joins
     * this domain. It is a good practice when overriding this
     * method to call super.onJoinedDomain with the same
     * arguments.
     * @param group the group that is joining this domain
     */
    protected void onJoinedDomain(G group) {}

    /**
     * Note that a group has just left this domain.
     * <P>
     * The default method does nothing. It can be overridden when
     * some domain-specific action is needed when a group leaves
     * this domain. It is a good practice when overriding this
     * method to call super.onJoinedDomain with the same
     * arguments.
     * @param group the group that has just left this domain
     */
    protected void onLeftDomain(G group) {}

    boolean addDomainMember(DM domainMember, 
			    boolean tracksCondition)
    {
	if (domainMember == null) return false;
	if (domainMember.isShared()) {
	    if (domainMembers.add(domainMember)) {
		if (tracksCondition) {
		    trackingDomainMembers.add(domainMember);
		    for (C c: conditionSet()) {
			domainMember.onConditionChange
			    (c, ConditionMode .OBSERVER_JOINED_DOMAIN, this);
		    }
		}
		actorUnion.addSet(domainMember.actors);
		/*
		for (A actor: domainMember.actors) {
		    onJoinedDomain(actor, tracksCondition);
		}
		*/
		return true;
	    } else {
		return false;
	    }
	} else {
	    if (domainMember.actor == null) return false;
	    if(unsharedActorSet.add(domainMember.actor)) {
		if (tracksCondition) {
		    trackingActorSet.add(domainMember.actor);
		    for (C c: conditionSet()) {
			domainMember.actor.onConditionChange
			    (c, ConditionMode.OBSERVER_JOINED_DOMAIN, this);
		    }
		}
		// onJoinedDomain(domainMember.actor, tracksCondition);
		return true;
	    } else {
		return false;
	    }
	}
    }

    boolean removeDomainMember(DM domainMember) {
	if (domainMember.isShared()) {
	    if (domainMembers.remove(domainMember)) {
		trackingDomainMembers.remove(domainMember);
		for (C c: conditionSet()) {
		    domainMember.onConditionChange(c,
						   ConditionMode
						   .OBSERVER_LEFT_DOMAIN,
						   this);
		}
		actorUnion.removeSet(domainMember.actors);
		/*
		for (A actor: domainMember.actors) {
		    onLeftDomain(actor);
		}
		*/
		return true;
	    } else {
		return false;
	    }
	} else {
	    if (unsharedActorSet.remove(domainMember.actor)) {
		trackingActorSet.remove(domainMember.actor);
		for (C c: conditionSet()) {
		    domainMember.onConditionChange(c,
						   ConditionMode
						   .OBSERVER_LEFT_DOMAIN,
						   this);
		}
		// onLeftDomain(domainMember.actor);
		return true;
	    } else {
		return false;
	    }
	}
    }

    /**
     * Determine if a domain member is a member of a domain
     * @param domainMember a domain member
     * @return true if the domain member is a member of this domain;
     *         false otherwise
     */
    public boolean containsDomainMember(DM domainMember) {
	if (domainMember.isShared()) {
	    return domainMembers.contains(domainMember);
	} else {
	    // for unshared domain members, we do not put the domain
	    // member in a table as there is only one actor for that
	    // domain, and put the actor in a table instead.
	    return unsharedActorSet.contains(domainMember.actor);
	}
    }

    /**
     * Determine if an actor is a member of a domain.
     * @param actor the actor to check
     * @return true if the actor's domain member is a member of this domain;
     *         false otherwise
     */
    public boolean containsActor(A actor) {
	return unsharedActorSet.contains(actor) ||
	    domainMembers.contains(actor.getDomainMember(thisInstance()));
    }

    /**
     * Get a set of shared domain members.
     * @return a set of the shared domain members for this domain
     */
    public Set<DM> domainMemberSet() {
	return Collections.unmodifiableSet(domainMembers);
    }

    /**
     * Get a set of actors.
     * @return a set containing those actors whose domain members are
     *         members of this domain
     */
    public Set<A> actorSet() {
	/*
	Set<A> result = new HashSet<A>();
	for (DM dm: domainMembers) {
	    for (A actor: dm.actors) {
		result.add(actor);
	    }
	}
	return result;
	*/
	// calling setView prevents the caller of actorSet from using
	// DisjointSetsUnion methods to change the sets that make up the
	// union.
	return actorUnion.setView();
    }


    int nactors = 0;
    /**
     * Get the number of actors in a domain.
     * Note - using this function can be significantly more
     * efficient that calling actorSet().size().
     * @return the number of actors in this domain
     */
    public int actorSetSize() {
	return nactors;
    }

    // used by GenericDomainMember to adjust counts.
    void incrActors(int n) {
	nactors += n;
    }

    // used by GenericDomainMember to adjust counts.
    void decrActors(int n) {
	nactors -= n;
    }

    private GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> defaultMsgInfo;

    private GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> msgInfo;

    /**
     * Configure message-forwarding parameters.
     * @param info an object providing the delay and message filter
     *        that a domain will use for a given message source and
     *        destination; null will restore the default
     */
    public void
	setMessageForwardingInfo(GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> info)
    {
	if (info == null) {
	    msgInfo = defaultMsgInfo;
	} else {
	    msgInfo = info;
	}
    }

    /**
     * Get the class determining delays and message filters associated with
     * this  domain.
     * @return an object providing delays and message filters
     *         associated with this domain
     */
    protected  GenericMsgFrwdngInfo<S,A,C,D,DM,F,G> getMFI() {
	return msgInfo;
    }


    /**
     * Get the delay for a message given a source actor and its domain,
     * and a destination actor and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The delay is the
     * sum of the delays of all the domains traversed in following
     * parent domains to the closest common ancestor from the source
     * and then to the destination. Each domain traversed provides a single
     * delay for the total.
     * This method is applicable only to communication domains.
     * The implementation uses an instance of GenericMsgFrwdngInfo or one
     * of its subclasses to determine the delay contributed by each domain.
     * <P>
     * Implementation Note:  the classes in this package call
     * {@link GenericSimulation} methods named findCommDomain to get
     * an instance of {@link org.bzdev.drama.common.CommDomainInfo},
     * the methods of which determine a source and destination domain,
     * and a common ancestor domain.  If the source domain and the
     * destination domain are identical, the source domain is used
     * to look up the delay. Otherwise a sum of terms is used.
     * Starting from the source domain, one looks up a delay using
     * the source domain's delay table (an instance of
     * {@link GenericMsgFrwdngInfo}) for the delay contribution for
     * a hop from the source to the parent domain.  For the second
     * hop the parent's delay table is used to determine a delay
     * contribution for the hop from source domain to the parent's
     * parent domain.  This is repeated iteratively until the common
     * ancestor domain is reached.  A similar procedure is used
     * starting from the destination domain.  Then the total is
     * returned as the delay. For each domain determining a delay,
     * the {@link GenericMsgFrwdngInfo} for that domain determines
     * the contribution to the total delay.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the delay in units of simulation ticks
     */
    public long getDelay(A src, Object msg,
			 D sourceDomain, D destDomain,
			 A dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localDelay(sourceDomain,
						    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	long delay;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    delay = 0;
	    delay += current.getMFI().localDelay(current, src, msg, parent);
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		delay += current.getMFI().localDelay(current,
						     previousDomain,
						     msg, parent);
	    }
	    if (destDomain == ancestor) {
		return delay + ancestor.getMFI().localDelay(ancestor,
							    current, msg, dest);
	    } else {
		previousDomain = current;
		next = lit.next();
		delay += msgInfo.localDelay(ancestor, current, msg, next);
		if (next == destDomain) {
		    return
			delay + next.getMFI().localDelay(next,
							 ancestor, msg, dest);
		}
	    }
	} else {
	    next = lit.next();
	    delay = msgInfo.localDelay(ancestor, src, msg, next);
	    if (next == destDomain) {
		return
		    delay + next.getMFI().localDelay(next, ancestor, msg, dest);
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    delay += current.getMFI().localDelay(current,
						 previousDomain, msg, next);
	}
	return delay + next.getMFI().localDelay(next, current, msg, dest);
    }

    /**
     * Get the delay for a message given a source actor and its domain,
     * and a destination group and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The delay is the
     * sum of the delays of all the domains traversed in following
     * parent domains to the closest common ancestor from the source
     * and then to the destination. Each domain traversed provides a single
     * delay for the total.
     * This method is applicable only to communication domains.
     * The implementation uses an instance of GenericMsgFrwdngInfo or one
     * of its subclasses to determine the delay contributed by each domain.
     * <P>
     * Implementation Note:  the classes in this package call
     * {@link GenericSimulation} methods named findCommDomain to get
     * an instance of {@link org.bzdev.drama.common.CommDomainInfo},
     * the methods of which determine a source and destination domain,
     * and a common ancestor domain.  If the source domain and the
     * destination domain are identical, the source domain is used
     * to look up the delay. Otherwise a sum of terms is used.
     * Starting from the source domain, one looks up a delay using
     * the source domain's delay table (an instance of
     * {@link GenericMsgFrwdngInfo}) for the delay contribution for
     * a hop from the source to the parent domain.  For the second
     * hop the parent's delay table is used to determine a delay
     * contribution for the hop from source domain to the parent's
     * parent domain.  This is repeated iteratively until the common
     * ancestor domain is reached.  A similar procedure is used
     * starting from the destination domain.  Then the total is
     * returned as the delay. For each domain determining a delay,
     * the {@link GenericMsgFrwdngInfo} for that domain determines
     * the contribution to the total delay.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the delay in units of simulation ticks
     */
    public long getDelay(A src, Object msg,
			 D sourceDomain, D destDomain,
			 G dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localDelay(sourceDomain,
						    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	long delay;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    delay = 0;
	    delay += current.getMFI().localDelay(current, src, msg, parent);
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		delay += current.getMFI().localDelay(current,
						     previousDomain,
						     msg, parent);
	    }
	    if (destDomain == ancestor) {
		return delay + ancestor.getMFI().localDelay(ancestor,
							    current, msg, dest);
	    } else {
		previousDomain = current;
		next = lit.next();
		delay += msgInfo.localDelay(ancestor, current, msg, next);
		if (next == destDomain) {
		    return
			delay + next.getMFI().localDelay(next,
							 ancestor, msg, dest);
		}
	    }
	} else {
	    next = lit.next();
	    delay = msgInfo.localDelay(ancestor, src, msg, next);
	    if (next == destDomain) {
		return
		    delay + next.getMFI().localDelay(next, ancestor, msg, dest);
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    delay += current.getMFI().localDelay(current,
						 previousDomain, msg, next);
	}
	return delay + next.getMFI().localDelay(next, current, msg, dest);
    }

    /**
     * Get the delay for a message given a source group and its domain,
     * and a destination group and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The delay is the
     * sum of the delays of all the domains traversed in following
     * parent domains to the closest common ancestor from the source
     * and then to the destination. Each domain traversed provides a single
     * delay for the total.
     * This method is applicable only to communication domains.
     * The implementation uses an instance of GenericMsgFrwdngInfo or one
     * of its subclasses to determine the delay contributed by each domain.
     * <P>
     * Implementation Note:  the classes in this package call
     * {@link GenericSimulation} methods named findCommDomain to get
     * an instance of {@link org.bzdev.drama.common.CommDomainInfo},
     * the methods of which determine a source and destination domain,
     * and a common ancestor domain.  If the source domain and the
     * destination domain are identical, the source domain is used
     * to look up the delay. Otherwise a sum of terms is used.
     * Starting from the source domain, one looks up a delay using
     * the source domain's delay table (an instance of
     * {@link GenericMsgFrwdngInfo}) for the delay contribution for
     * a hop from the source to the parent domain.  For the second
     * hop the parent's delay table is used to determine a delay
     * contribution for the hop from source domain to the parent's
     * parent domain.  This is repeated iteratively until the common
     * ancestor domain is reached.  A similar procedure is used
     * starting from the destination domain.  Then the total is
     * returned as the delay. For each domain determining a delay,
     * the {@link GenericMsgFrwdngInfo} for that domain determines
     * the contribution to the total delay.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the delay in units of simulation ticks
     */
    public long getDelay(G src, Object msg,
			 D sourceDomain, D destDomain,
			 G dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localDelay(sourceDomain,
						    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	long delay;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    delay = 0;
	    delay += current.getMFI().localDelay(current, src, msg, parent);
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		delay += current.getMFI().localDelay(current,
						     previousDomain,
						     msg, parent);
	    }
	    if (destDomain == ancestor) {
		return delay + ancestor.getMFI().localDelay(ancestor,
							    current, msg, dest);
	    } else {
		previousDomain = current;
		next = lit.next();
		delay += msgInfo.localDelay(ancestor, current, msg, next);
		if (next == destDomain) {
		    return
			delay + next.getMFI().localDelay(next,
							 ancestor, msg, dest);
		}
	    }
	} else {
	    next = lit.next();
	    delay = msgInfo.localDelay(ancestor, src, msg, next);
	    if (next == destDomain) {
		return
		    delay + next.getMFI().localDelay(next, ancestor, msg, dest);
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    delay += current.getMFI().localDelay(current,
						 previousDomain, msg, next);
	}
	return delay + next.getMFI().localDelay(next, current, msg, dest);
    }


    /**
     * Get the delay for a message given a source group and its domain,
     * and a destination actor and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The delay is the
     * sum of the delays of all the domains traversed in following
     * parent domains to the closest common ancestor from the source
     * and then to the destination. Each domain traversed provides a single
     * delay for the total.
     * This method is applicable only to communication domains.
     * The implementation uses an instance of GenericMsgFrwdngInfo or one
     * of its subclasses to determine the delay contributed by each domain.
     * <P>
     * Implementation Note:  the classes in this package call
     * {@link GenericSimulation} methods named findCommDomain to get
     * an instance of {@link org.bzdev.drama.common.CommDomainInfo},
     * the methods of which determine a source and destination domain,
     * and a common ancestor domain.  If the source domain and the
     * destination domain are identical, the source domain is used
     * to look up the delay. Otherwise a sum of terms is used.
     * Starting from the source domain, one looks up a delay using
     * the source domain's delay table (an instance of
     * {@link GenericMsgFrwdngInfo}) for the delay contribution for
     * a hop from the source to the parent domain.  For the second
     * hop the parent's delay table is used to determine a delay
     * contribution for the hop from source domain to the parent's
     * parent domain.  This is repeated iteratively until the common
     * ancestor domain is reached.  A similar procedure is used
     * starting from the destination domain.  Then the total is
     * returned as the delay. For each domain determining a delay,
     * the {@link GenericMsgFrwdngInfo} for that domain determines
     * the contribution to the total delay.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the delay in units of simulation ticks
     */
    public long getDelay(G src, Object msg,
			 D sourceDomain, D destDomain,
			 A dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localDelay(sourceDomain,
						    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	long delay;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    delay = 0;
	    delay += current.getMFI().localDelay(current, src, msg, parent);
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		delay += current.getMFI().localDelay(current,
						     previousDomain,
						     msg, parent);
	    }
	    if (destDomain == ancestor) {
		return delay + ancestor.getMFI().localDelay(ancestor,
							    current, msg, dest);
	    } else {
		previousDomain = current;
		next = lit.next();
		delay += msgInfo.localDelay(ancestor, current, msg, next);
		if (next == destDomain) {
		    return
			delay + next.getMFI().localDelay(next,
							 ancestor, msg, dest);
		}
	    }
	} else {
	    next = lit.next();
	    delay = msgInfo.localDelay(ancestor, src, msg, next);
	    if (next == destDomain) {
		return
		    delay + next.getMFI().localDelay(next, ancestor, msg, dest);
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    delay += current.getMFI().localDelay(current,
						 previousDomain, msg, next);
	}
	return delay + next.getMFI().localDelay(next, current, msg, dest);
    }

    private CompoundMessageFilter addFilter(CompoundMessageFilter result,
					    MessageFilter filter)
    {
	if (filter != null) {
	    if (result == null) {
		result = new CompoundMessageFilter();
	    }
	    result.addFilter(filter);
	}
	return result;
    }

    /**
     * Get the message filter for a message given a source actor and its domain,
     * and a destination actor and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The filter
     * returned is a compound filter that applies the filters of all
     * the domains traversed in following parent domains to the
     * closest common ancestor from the source and then to the
     * destination. Each domain traversed provides a single filter,
     * possibly null to indicate no filtering for the total.  The
     * compound filter applies these filters in the order in which the
     * domains are traversed.  This method is applicable only to
     * communication domains.  The implementation uses an instance of
     * GenericMsgFrwdngInfo or one of its subclasses to determine the
     * message filter contributed by each domain.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the message filter; null if there is none
     */
    public MessageFilter getMessageFilter(A src, Object msg,
			 D sourceDomain, D destDomain,
			 A dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localMessageFilter(sourceDomain,
							    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	CompoundMessageFilter result = null;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      src, msg,
							      parent));
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		result = addFilter
		    (result,
		     current.getMFI().localMessageFilter(current,
							 previousDomain,
							 msg, parent));
	    }
	    if (destDomain == ancestor) {
		return addFilter(result,
				 ancestor.getMFI().localMessageFilter(ancestor,
								      current,
								      msg,
								      dest));
	    } else {
		previousDomain = current;
		next = lit.next();
		result = addFilter(result,
				   msgInfo.localMessageFilter(ancestor, current,
							      msg, next));
		if (next == destDomain) {
		    return
			addFilter(result,
				  next.getMFI().localMessageFilter(next,
								   ancestor,
								   msg, dest));
		}
	    }
	} else {
	    next = lit.next();
	    result = addFilter(result, msgInfo.localMessageFilter(ancestor, src,
								  msg, next));
	    if (next == destDomain) {
		return addFilter(result,
				 next.getMFI().localMessageFilter(next,
								  ancestor,
								  msg, dest));
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      previousDomain,
							      msg, next));
	}
	return addFilter(result,
			 next.getMFI().localMessageFilter(next, current,
							  msg, dest));
    }

    /**
     * Get the message filter for a message given a source actor and its domain,
     * and a destination group and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The filter
     * returned is a compound filter that applies the filters of all
     * the domains traversed in following parent domains to the
     * closest common ancestor from the source and then to the
     * destination. Each domain traversed provides a single filter,
     * possibly null to indicate no filtering for the total.  The
     * compound filter applies these filters in the order in which the
     * domains are traversed.  This method is applicable only to
     * communication domains.  The implementation uses an instance of
     * GenericMsgFrwdngInfo or one of its subclasses to determine the
     * message filter contributed by each domain.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the message filter; null if there is none
     */
    public MessageFilter getMessageFilter(A src, Object msg,
			 D sourceDomain, D destDomain,
			 G dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localMessageFilter(sourceDomain,
							    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	CompoundMessageFilter result = null;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      src, msg,
							      parent));
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		result = addFilter
		    (result,
		     current.getMFI().localMessageFilter(current,
							 previousDomain,
							 msg, parent));
	    }
	    if (destDomain == ancestor) {
		return addFilter(result,
				 ancestor.getMFI().localMessageFilter(ancestor,
								      current,
								      msg,
								      dest));
	    } else {
		previousDomain = current;
		next = lit.next();
		result = addFilter(result,
				   msgInfo.localMessageFilter(ancestor, current,
							      msg, next));
		if (next == destDomain) {
		    return
			addFilter(result,
				  next.getMFI().localMessageFilter(next,
								   ancestor,
								   msg, dest));
		}
	    }
	} else {
	    next = lit.next();
	    result = addFilter(result, msgInfo.localMessageFilter(ancestor, src,
								  msg, next));
	    if (next == destDomain) {
		return addFilter(result,
				 next.getMFI().localMessageFilter(next,
								  ancestor,
								  msg, dest));
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      previousDomain,
							      msg, next));
	}
	return addFilter(result,
			 next.getMFI().localMessageFilter(next, current,
							  msg, dest));
    }

    /**
     * Get the message filter for a message given a source group and its domain,
     * and a destination actor and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The filter
     * returned is a compound filter that applies the filters of all
     * the domains traversed in following parent domains to the
     * closest common ancestor from the source and then to the
     * destination. Each domain traversed provides a single filter,
     * possibly null to indicate no filtering for the total.  The
     * compound filter applies these filters in the order in which the
     * domains are traversed.  This method is applicable only to
     * communication domains.  The implementation uses an instance of
     * GenericMsgFrwdngInfo or one of its subclasses to determine the
     * message filter contributed by each domain.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the message filter; null if there is none
     */
    public MessageFilter getMessageFilter
	(G src, Object msg, D sourceDomain, D destDomain, A dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localMessageFilter(sourceDomain,
							    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	CompoundMessageFilter result = null;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      src, msg,
							      parent));
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		result = addFilter
		    (result,
		     current.getMFI().localMessageFilter(current,
							 previousDomain,
							 msg, parent));
	    }
	    if (destDomain == ancestor) {
		return addFilter(result,
				 ancestor.getMFI().localMessageFilter(ancestor,
								      current,
								      msg,
								      dest));
	    } else {
		previousDomain = current;
		next = lit.next();
		result = addFilter(result,
				   msgInfo.localMessageFilter(ancestor, current,
							      msg, next));
		if (next == destDomain) {
		    return
			addFilter(result,
				  next.getMFI().localMessageFilter(next,
								   ancestor,
								   msg, dest));
		}
	    }
	} else {
	    next = lit.next();
	    result = addFilter(result, msgInfo.localMessageFilter(ancestor, src,
								  msg, next));
	    if (next == destDomain) {
		return addFilter(result,
				 next.getMFI().localMessageFilter(next,
								  ancestor,
								  msg, dest));
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      previousDomain,
							      msg, next));
	}
	return addFilter(result,
			 next.getMFI().localMessageFilter(next, current,
							  msg, dest));
    }

    /**
     * Get the message filter for a message given a source group and its domain,
     * and a destination group and its domain.
     * The originator's domain and the destination's domain must have
     * this domain as their closest common ancestor.  The filter
     * returned is a compound filter that applies the filters of all
     * the domains traversed in following parent domains to the
     * closest common ancestor from the source and then to the
     * destination. Each domain traversed provides a single filter,
     * possibly null to indicate no filtering for the total.  The
     * compound filter applies these filters in the order in which the
     * domains are traversed.  This method is applicable only to
     * communication domains.  The originator's domain and the
     * destination's domain must have this domain as their closest
     * common ancestor.  The filter returned is a compound filter that
     * applies the filters of all the domains traversed in following
     * parent domains to the closest common ancestor from the source
     * and then to the destination. Each domain traversed provides a
     * single filter, possibly null to indicate no filtering for the
     * total.  The compound filter applies these filters in the order
     * in which the domains are traversed.  This method is applicable
     * only to communication domains.
     * @param src the source/originator of the message
     * @param msg the message
     * @param sourceDomain the originator's domain
     * @param destDomain the destination's domain
     * @param dest the destination
     * @return the message filter; null if there is none
     */
    public MessageFilter getMessageFilter(G src, Object msg,
			 D sourceDomain, D destDomain,
			 G dest)
    {
	D ancestor = thisInstance();
	if (sourceDomain == destDomain) {
	    return sourceDomain.getMFI().localMessageFilter(sourceDomain,
							    src, msg, dest);
	}
	LinkedList<D> list = new LinkedList<>();
	D current = destDomain;
	while (current != ancestor) {
	    list.addFirst(current);
	    current = current.getParent();
	}
	Iterator<D> lit = list.iterator();
	current = sourceDomain;
	D previousDomain = current;
	CompoundMessageFilter result = null;
	D next;
	if (ancestor != sourceDomain) {
	    D parent = current.getParent();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      src, msg,
							      parent));
	    while (parent != ancestor) {
		previousDomain = current;
		current = parent;
		parent = current.getParent();
		result = addFilter
		    (result,
		     current.getMFI().localMessageFilter(current,
							 previousDomain,
							 msg, parent));
	    }
	    if (destDomain == ancestor) {
		return addFilter(result,
				 ancestor.getMFI().localMessageFilter(ancestor,
								      current,
								      msg,
								      dest));
	    } else {
		previousDomain = current;
		next = lit.next();
		result = addFilter(result,
				   msgInfo.localMessageFilter(ancestor, current,
							      msg, next));
		if (next == destDomain) {
		    return
			addFilter(result,
				  next.getMFI().localMessageFilter(next,
								   ancestor,
								   msg, dest));
		}
	    }
	} else {
	    next = lit.next();
	    result = addFilter(result, msgInfo.localMessageFilter(ancestor, src,
								  msg, next));
	    if (next == destDomain) {
		return addFilter(result,
				 next.getMFI().localMessageFilter(next,
								  ancestor,
								  msg, dest));
	    }
	}
	while (next != destDomain) {
	    previousDomain = current;
	    current = next;
	    next = lit.next();
	    result =
		addFilter(result,
			  current.getMFI().localMessageFilter(current,
							      previousDomain,
							      msg, next));
	}
	return addFilter(result,
			 next.getMFI().localMessageFilter(next, current,
							  msg, dest));
    }

    // private Set<C> conditions = new HashSet<C>();
    @SuppressWarnings("unchecked")
    private D thisInstance() {
	try {
	    return (D) this;
	} catch (ClassCastException e) {
	    return null;
	}
    }

    private CondObserverImpl<C,D>
	impl = new CondObserverImpl<C,D>(thisInstance()) {
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
	protected void doConditionChange(C c, ConditionMode mode,
					 SimObject source)
	{
	    for (DM domainMember: trackingDomainMembers) {
		domainMember.onConditionChange(c, mode, source);
	    }
	    for (A actor: trackingActorSet) {
		actor.onConditionChange(c, mode, source);
	    }
	}
	protected void doConditionChange(Map<C,ConditionInfo> cMap) {
	    for (DM domainMember: trackingDomainMembers) {
		domainMember.onConditionChange(cMap);
	    }
	    for (A actor: trackingActorSet) {
		actor.onConditionChange(cMap);
	    }
	}
    };

    public CondObserverImpl<C,D> getCondObserverImpl() {
	return impl;
    }


    public boolean addCondition(C c) {
	return impl.addCondition(c);
    }

    public boolean removeCondition(C c) {
	return impl.removeCondition(c);
    }

    public boolean hasCondition(C c) {
	return impl.hasCondition(c);
    }

    public Set<C> conditionSet() {
	return impl.conditionSet();
    }

    public void setConditionChangeQMode(boolean value) {
	impl.setConditionChangeQMode(value);
    }

    public boolean getConditionChangeQMode() {
	return impl.getConditionChangeQMode();
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following
     * items.
     * <P>
     * Defined in {@link GenericDomain}:
     * <UL>
     *   <LI> the priority for this domain.
     *   <LI> the parent of this domain.
     *   <LI> whether or not this domain is a communication domain.
     *   <LI> a list of the communication domain types for this domain.
     *   <LI> the conditions this domain observes
     *   <LI> the domain members of this domain (i.e., the instances
     *        of {@link GenericDomainMember} that have joined this domain).
     *   <LI> the actors that are not in a shared domain that have joined
     *        this domain.
     *   <LI> the groups that have joined this domain.
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
	out.println(prefix + "priority = " +getPriority());
	out.println(prefix + "parent = "
		    +((parent == null)? "(none)": parent.getName()));
	out.println(prefix + "isCommunicationDomain = "
		    + communicationDomain);
	if (communicationDomain) {
	    if (commDomainTypeSet == null || commDomainTypeSet.isEmpty()) {
		out.println(prefix + "communication-domain types: (none)");
	    } else {
		out.println(prefix + "communication-domain types:");
		for (CommDomainType type: commDomainTypeSet) {
		    out.println(prefix + "    " + type.getName());
		}
	    }
	    out.println("communication-domain delay table "
			+ "(msg forwarding info): "
			+ getMFI().getName());
	}
	Set<C> cset = conditionSet();
	if (cset == null || cset.isEmpty()) {
	    out.println(prefix + "conditions: (none)");
	} else {
	    out.println(prefix + "conditions:");
	    for (C c: cset) {
		out.println(prefix + "    " + c.getName());
	    }
	}
	if (domainMembers.isEmpty()) {
	    out.println(prefix + "domain members: (none)");
	} else {
	    out.println(prefix + "domain members:");
	    for (DM dm: domainMembers) {
		if (trackingDomainMembers.contains(dm)) {
		    out.println(prefix + "    " + dm.getName()
				+ " (tracks conditions)");
		} else {
		    out.println(prefix + "    " + dm.getName());
		}
	    }
	}
	if (unsharedActorSet.isEmpty()) {
	    out.println(prefix + "actors not in shared domains: (none)");
	} else {
	    out.println(prefix + "actors not in shared domains:");
	    for (A a: unsharedActorSet) {
		if (trackingActorSet.contains(a)) {
		    out.println(prefix + "    " + a.getName()
				+ " (tracks conditions)");
		} else {
		    out.println(prefix + "    " + a.getName());
		}
	    }
	}
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

//  LocalWords:  exbundle setMessageForwardingInfo CommDomainType sim
//  LocalWords:  GenericMsgFrwdngInfo configureAsCommunicationDomain
//  LocalWords:  onChildAdd IllegalArgumentException SimObject dm ul
//  LocalWords:  defaultMessageForwardingInfo GenericDomainFactory li
//  LocalWords:  parentNotCommDomain onDelete superclass DomainMember
//  LocalWords:  findCommDomain TreeSet groupSet typeSet tooLate a's
//  LocalWords:  IllegalStateException calledTooLate notCommDomain OL
//  LocalWords:  addCommDomainTypeSet isCommunicationDomain typeName
//  LocalWords:  mustNotBeNull communicationMatchByDomainAncestor src
//  LocalWords:  communicationMatchByDomain tracksCondition HashSet
//  LocalWords:  domainMember onLeftDomain CommDomainInfo
//  LocalWords:  onJoinedDomain msg
//  LocalWords:  domainMembers setView actorSet DisjointSetsUnion g's
//  LocalWords:  GenericDomainMember sourceDomain dest printName dm's
//  LocalWords:  destDomain GenericDomain boolean iPrefix
//  LocalWords:  printConfiguration whitespace communicationMatch
//  LocalWords:  communcationMatchByDomain GenericGroup GenericActor
//  LocalWords:  GenericSimulation
