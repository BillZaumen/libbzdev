package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

/**
 * Class representing a condition.
 * Conditions represent quantities that are part of the global state
 * of a simulation, but that are not represented by actors or other
 * more specific simulation objects.  Domains and user-defined
 * condition observers will be notified when a condition changes.
 * <P>
 * Conditions can be used for a variety of purposes, including turning
 * on or off debugging for all the actors associated with a given
 * domain.  The simplest conditions simply provide a value.
 * <P>
 * When a condition changes its state, the object that changed the state
 * is expected to call notifyObservers() followed by completeNotification().
 * The use of two methods allows objects to queue up multiple notifications
 *  and apply them at once.
 * <P>
 * As a general rule, it is preferable to send as few notifications as
 * possible.  For example, if a condition represents a value that is
 * changing with time, one might represent the condition by a value at
 * some time and the rate of change, sending notifications only when new
 * values or rates of change are needed.
 */
abstract public class GenericCondition<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends GenericTaskObject<S,A,C,D,DM,F,G> {

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the condition
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected GenericCondition(S sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }


    Set<D> domains = new HashSet<D>();

    Map<CondObserverImpl<C,? extends SimObject>,SimObject> observers =
	new HashMap<CondObserverImpl<C, ? extends SimObject>,SimObject>();

    boolean deleting = false;

    @SuppressWarnings("unchecked")
    protected void onDelete() {
	deleting = true;
	Iterator<CondObserverImpl<C,? extends SimObject>> itc =
	    observers.keySet().iterator();
	while (itc.hasNext()) {
	    GenericCondObsrvrImpl<C,? extends SimObject> co = itc.next();
	    co.remCondition((C)this, itc);
	}
	domains.clear();
	super.onDelete();
    }


    /*
    /**
     * Associate a domain with a condition.
     * @param domain the domain
     * @return true on success; false on failure
    boolean addDomain(D domain) {
	boolean result = addObserver(domain);
	if (result) {
	    domains.add(domain);
	}
	return result
    }
     */

    /**
     * Associate a condition observer with a condition.
     * @param observer the condition observer
     * @return true on success; false on failure
     */
    @SuppressWarnings("unchecked")
    boolean addObserver(CondObserverImpl<C,? extends SimObject> observer) {
	SimObject oldObserver = 
	    observers.put(observer, observer.getParent());
	if (oldObserver != null) {
	    observers.put(observer, oldObserver);
	    return false;
	} else {
	    if (observer.getParent() instanceof GenericDomain) {
		domains.add((D)(observer.getParent()));
	    }
	    return true;
	}
    }


    /*
    /**
     * Disassociate a domain with a condition.
     * @param domain the domain
     * @return true on success; false on failure
    boolean removeDomain(D domain) {
	return domains.remove(domain);
    }
     */

    /**
     * Disassociate a condition observer with a condition.
     * @param observer the condition observer
     * @return true on success; false on failure
     */
    @SuppressWarnings("unchecked")
    boolean removeObserver(CondObserverImpl observer) {
	boolean result = deleting || (observers.remove(observer) != null);
	if (result) {
	    if (observer.getParent() instanceof GenericDomain) {
		domains.remove((D)(observer.getParent()));
	    }
	}
	return result;
    }


    /**
     * Determine if a domain is impacted by a condition.
     * A domain is impacted by a condition if and only if the domain
     * is associated with the condition by being one of the condition's
     * observers.
     * @param domain the domain
     * @return true if impacted; false otherwise
     */
    public boolean impactsDomain(D domain) {
	return domains.contains(domain);
    }

    /**
     * Determine if a condition observer observes this condition.
     * @param observer the observer
     * @return true if the observer monitors this condition; false otherwise
     */
    public boolean hasObserver(CondObserver observer) {
	return observers.containsKey(observer.getCondObserverImpl());
    }

    /**
     * Get a set of domains associated with a condition.
     * This set is a subset of the condition's observers.
     * @return a set of conditions
     */
    public Set<D> domainSet() {
	return Collections.unmodifiableSet(domains);
    }

    /**
     * Get a collection of simulation objects associated with a condition
     * While formally this method returns a collection, the collection is
     * actually a set (there are no duplicate entries).
     * @return a collection of conditions
     */
    public Collection<SimObject> observerCollection() {
	return Collections.unmodifiableCollection(observers.values());
    }


    /**
     * Notify observers that a condition may have changed.
     * Normally this must be called explicitly, so that multiple
     * of conditions can be changed before domains are notified . 
     */
    @SuppressWarnings("unchecked")
    public void notifyObservers() {
	// for (D domain: domains) {
	// domain.onConditionChange((C)this);
	// }
	for (GenericCondObsrvrImpl<C,?extends SimObject>ci:observers.keySet()) {
	    ci.onConditionChange((C) this, ConditionMode.OBSERVER_NOTIFIED,
				 this);
	}
    }

    /**
     * Indicate that the current batch of condition-change notifications is
     * complete.
     * When condition changes are queued, this will flush the queue, sending
     * the notifications to various domain members.  It should always be
     * called after a call to notifyDomains in case notification queuing is
     * enabled for a domain.
     */
    public void completeNotification() {
	// for (D domain: domains) {
	//    domain.completeNotification();
	// }
	for (GenericCondObsrvrImpl<C,?extends SimObject>ci:observers.keySet()) {
	    ci.completeNotification();
	}
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following
     * items.
     * <P>
     * Defined in {@link GenericCondition}:
     * <UL>
     *   <LI> the observers for this condition.
     * </UL>
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
	if (observers.values().size() == 0) {
	    out.println(prefix + "observers: (none)");
	} else {
	    out.println(prefix + "observers:");
	    for (SimObject obj: observers.values()) {
		String suffix = "";
		if (obj instanceof GenericDomain) {
		    suffix = " (domain)";
		}
		out.println(prefix + "    " + obj.getName() + suffix);
	    }
	}
    }
}

//  LocalWords:  notifyObservers completeNotification
//  LocalWords:  IllegalArgumentException boolean addDomain printName
//  LocalWords:  addObserver removeDomain onConditionChange
//  LocalWords:  notifyDomains GenericCondition
