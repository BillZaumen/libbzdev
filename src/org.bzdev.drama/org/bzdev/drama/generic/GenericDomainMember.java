package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

/**
 * Class representing domain membership.
 *
 * Domain members are used by actors for domain membership. By
 * default, each actor has a unique domain member, but it is
 * possible to explicitly configure an actor so that its domain
 * member object is shared with other actors.  This saves on
 * memory when multiple actors have identical domain memberships
 * and are also in multiple domains. It also simplifies setting
 * up a simulation in these cases.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <p>
 * At any point, an actor can replace a shared domain member with
 * a per-actor domain member, but it that is done, all of its
 * domain memberships must be reconfigured.
 */
public class GenericDomainMember<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericSimObject<S,A,C,D,DM,F,G>
{
    boolean shared;
    Map<D,Integer> dmap = null;

    // map of domains tracking conditions
    Map<D,Boolean> tmap = new HashMap<D,Boolean>();

    String name = null;

    /**
     * Determine if a domain member is a shared domain member.
     * @return true if it is shared; false if not
     */
    public boolean isShared() {
	return shared;
    }

    /**
     *Constructor for unshared domain members.
     * @param sim the simulation
     */
    public GenericDomainMember(S sim) {
	super(sim, null, false);
	shared = false;
	dmap = null;
    }

    /**
      * Determine if this simulation object can be deleted.
     * An GenericDomainMember can be deleted if the method delete
     * has not been called and if it is not a shared domain member
     */
    public boolean canDelete() {
	if (!shared) return false;
	return super.canDelete();
	
    }

    protected void onDelete() {
	leaveAllDomains();
	Iterator<A>ita = actors.iterator();
	while (ita.hasNext()) {
	    A a = ita.next();
	    a.dropSharedDomainMember(ita);
	}
	super.onDelete();
    }


    /**
     *Constructor for shared domain members.
     * @param sim the simulation
     * @param name the name of this domain member
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public GenericDomainMember(S sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	shared = true;
	dmap = new HashMap<D,Integer>();
    }

    SortedSet<D> domains = new TreeSet<D>();

    /**
     * Get a set of the domains that the current domain member is in.
     * The set is ordered based on domain-specific criteria.
     * This differs from domainSet in that the set returned is not
     * wrapped by an unmodifiable sorted set.
     * @return a set of domains.
     */
    protected SortedSet<D> getDomains() {
	return domains;
    }

    Map<D,Set<D>> domainAncestors = new HashMap<D,Set<D>>();

    /**
     * Get a set of the domains that the current domain member is in with a
     * common ancestor.
     * The iterator for the returned set will return domains in the order
     * set by domain-specific criteria.
     * The domains in the set are restricted to communication domains, and
     * all domains leading to the common ancestor must be communication
     * domains.
     * @param ancestor the ancestor domain
     * @return a set of ancestor domains
     */
    public Set<D> getChildDomains(D ancestor) {
	Set<D> set = domainAncestors.get(ancestor);
	if (set == null) {
	    return null;
	} else {
	    return Collections.unmodifiableSet(set);
	}
    }

    // keep track of which domains actors that use this domain member are
    // in (using their private domain member).  We just need a count, not
    // the actual actor.
    void actorJoinedDomain(D d) {
	Integer n = dmap.get(d);
	if (n != null) {
	    dmap.put(d, n + 1);
	} else {
	    dmap.put(d, 1);
	}
    }

    void actorLeftDomain(D d) {
	Integer n = dmap.get(d);
	if (n != null) {
	    int nn = n -1;
	    if (nn == 0) {
		dmap.remove(d);
	    } else {
		dmap.put(d, n - 1);
	    }
	}
    }

    /**
     * Join a domain.
     * @param d the domain
     * @return true on success; false on failure, including having already
     *         joined
     */
    public boolean joinDomain(D d) {
	return joinDomain(d, false);
    }

    /**
     * Join a domain, optionally tracking conditions that impact that domain.
     * For a shared domain member, if an actor using that domain member has
     * independently joined a domain, <code>joinDomain(...)</code> will fail
     * and return <code>false</code>.
     * @param d the domain
     * @param trackCondition true if conditions should be tracked; 
     *                       false otherwise
     * @return true on success; false on failure, including having already
     *         joined
     */
    @SuppressWarnings("unchecked")
    public boolean joinDomain(D d, 
			      boolean trackCondition) {
	if (shared && dmap.get(d) != null) return false;
	if (d.addDomainMember((DM)this, trackCondition)) {
	    domains.add(d); 
	    tmap.put(d, trackCondition);
	    d.incrActors(actors.size());
	    if (d.isCommunicationDomain()) {
		D parent = d.getParent();
		while (parent != null && parent.isCommunicationDomain()) {
		    Set<D> set = domainAncestors.get(parent);
		    if (set == null) {
			set = new TreeSet<D>();
			domainAncestors.put(parent,set);
		    }
		    set.add(d);
		    parent = parent.getParent();
		}
	    }
	    for (A a: actors) {
		d.onJoinedDomain(a, trackCondition);
	    }
	    return true;
	} else {
	    return false;
	}
    }

    void onLeaveDomain(D d) {
	d.decrActors(actors.size());
	/*
	if (tmap.get(d)) {
	    Map<C,ConditionInfo> cMap = new HashMap<C,ConditionInfo>();
	    for (C condition: d.conditionSet()) {
		cMap.put(condition, new ConditionInfo
			 (ConditionMode.OBSERVER_LEFT_DOMAIN, d));
	    }
	    for (A actor: actors) {
		actor.onConditionChange(cMap);
	    }
	}
	*/
	for (A a: actors) {
	    d.onLeftDomain(a);
	}
	tmap.remove(d);
	if (d.isCommunicationDomain()) {
	    D parent = d.getParent();
	    while (parent != null && parent.isCommunicationDomain()) {
		Set<D> set = domainAncestors.get(parent);
		set.remove(d);
		if (set.isEmpty()) {
		    domainAncestors.remove(parent);
		}
		parent = parent.getParent();
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private boolean leaveDomain(D d, Iterator<D> it) {
	if (d.removeDomainMember((DM)this)) {
	    if (it == null) domains.remove(d);
	    else it.remove();
	    onLeaveDomain(d);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Leave a domain.
     * @param d the domain
     * @return true on success; false on failure
     */
    public boolean leaveDomain(D d) {
	return leaveDomain(d, null);
	/*
	if (d.removeDomainMember((DM)this)) {
	    domains.remove(d);
	    d.decrActors(actors.size());
	    D parent = d.getParent();
	    while (parent != null) {
		Set<D> set = domainAncestors.get(parent);
		set.remove(d);
		if (set.isEmpty()) {
		    domainAncestors.remove(parent);
		}
		parent = parent.getParent();
	    }
	    return true;
	} else {
	    return false;
	}
	*/
    }

    // for GenericDomain onDelete()
    void leaveDom(D d, Iterator<?>it) {
	domains.remove(d);
	if (tmap.get(d)) {
	    Map<C,ConditionInfo> cMap = new HashMap<C,ConditionInfo>();
	    for (C condition: d.conditionSet()) {
		cMap.put(condition, new ConditionInfo
			 (ConditionMode.OBSERVER_LEFT_DOMAIN, d));
	    }
	    for (A actor: actors) {
		actor.onConditionChange(cMap);
	    }
	}
	onLeaveDomain(d);
	it.remove();
    }

    // so we can delete a domain member or allow an actor to
    // be deleted (which requires it to leave all its domains).
    void leaveAllDomains() {
	Iterator<D> itd = domains.iterator();
	while (itd.hasNext()) {
	    D d = itd.next();
	    leaveDomain(d, itd);
	}
    }

    /**
     * Determine if in a domain.
     * @param d the domain
     * @return true if in domain d; false otherwise
     */
    public boolean inDomain(D d) {
	return domains.contains(d);
    }

    /**
     * Get a set of the domains the current domain member is in.
     * @return the domain set.
     */
    public SortedSet<D> domainSet() {
	return Collections.unmodifiableSortedSet(domains);
    }

    Set<A> actors = new HashSet<A>();
    A actor = null;

    /**
     * Get the actor for an unshared domain member.
     * @return the actor; null if none or if this is a shared domain
     */
    A getActor() {return actor;}

    /**
     * Get the set of actors associated with a shared domain member.
     * @return the actors associated with this domain; an empty set if this
     *         is a shared domain
     */
    Set<A> actorSet() {return Collections.unmodifiableSet(actors);}


    void register(A actor) {
	if (actors.add(actor)) {
	    if (!shared) this.actor = actor;
	    for (D d: domains) {
		d.incrActors(1);
		d.onJoinedDomain(actor, tmap.get(d));
	    }
	}
    }

    void deregister(A actor, Iterator<A> ita) {
	boolean removed = true;
	if (ita == null) removed = actors.remove(actor);
	else ita.remove();
	if (!shared) this.actor = null;
	for (D d: domains) {
	    d.decrActors(1);
	    if (removed) d.onLeftDomain(actor);
	}
    }

    void deregister(A actor) {
	deregister(actor, null);
    }

    void onConditionChange(C c, ConditionMode mode, SimObject source) {
	for (A actor: actors) {
	    actor.onConditionChange(c, mode, source);
	}
    }

    void onConditionChange(Map<C,ConditionInfo> cMap) {
	for (A actor: actors) {
	    actor.onConditionChange(cMap);
	}
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following
     * items.
     * <P>
     * Defined in {@link GenericDomainMember}:
     * <UL>
     *   <LI> the domains this domain member has joined.
     *   <LI> the actors that this domain member handles.
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
	if (actors.isEmpty()) {
	    out.println(prefix + "actors: (none)");
	} else {
	    out.println(prefix + "actors:");
	    for (A a: actors) {
		out.println(prefix + "    " + a.getName());
	    }
	}
    }
}

//  LocalWords:  subclasses unshared GenericDomainMember domainSet DM
//  LocalWords:  IllegalArgumentException unmodifiable trackCondition
//  LocalWords:  tmap ConditionInfo cMap HashMap conditionSet isEmpty
//  LocalWords:  ConditionMode onConditionChange removeDomainMember
//  LocalWords:  getParent domainAncestors GenericDomain onDelete
//  LocalWords:  iPrefix printName
