package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.util.*;

/**
 * Implement a condition observer. 
 * This class' subclass
 * {@link org.bzdev.drama.common.CondObserverImpl CondObserverImpl}
 * should be used.  An overview of this class appears in the
 * documentation for that class.
 * @see org.bzdev.drama.common.CondObserverImpl
 */
public abstract class GenericCondObsrvrImpl
    <C extends GenericCondition, P extends SimObject>
{

    P parent;
    CondObserverImpl<C,P> thisInstance;

    /**
     * Indicate the actual class of this object.
     * @param instance this object as a CondObserverImpl
     */
    protected void setCondObserverImpl(CondObserverImpl<C,P> instance) {
	thisInstance = instance;
    }

    /**
     * Constructor.
     * @param parent a simulation object (SimObject) that implements
     *        CondObserver
     */
    protected GenericCondObsrvrImpl(P parent) {
	this.parent = parent;
    }

    /**
     * Get a condition-observer implementation's parent.
     * The parent is an instance of SimObject that implements CondObserver.
     * @return the parent
     */
    public P getParent() {return parent;}


    private Set<C> conditions = new HashSet<C>();

    /**
     * The mode to store in a map argument for onCondition.
     * The default simply returns its argument.  Subclasses should
     * override this when a more appropriate mode is desired.
     * For example, when an addCondition method is called for a domain,
     * the appropriate mode when the change is propagated to actors
     * and domain members is  DOMAIN_ADDED_CONDITION rather than
     * OBSERVER_ADDED_CONDITION.
     * @param mode the original mode
     * @return the new mode
     */
    protected ConditionMode transformedMode(ConditionMode mode) {
	return mode;
    }

    /**
     * Associate a condition with an observer.
     * @param c the condition
     * @return true on success; false on failure
     */
    @SuppressWarnings("unchecked")
    public boolean addCondition(C c) {
	if (c.addObserver(thisInstance)) {
	    conditions.add(c);
	    onConditionChange(c,
			      transformedMode
			      (ConditionMode.OBSERVER_ADDED_CONDITION),
			      parent);
	    return true;
	} else {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    private boolean removeCondition(C c, Iterator<C> itc) {
	if (c.removeObserver(thisInstance)) {
	    if (itc == null) {
		conditions.remove(c);
	    } else {
		itc.remove();
	    }
	    onConditionChange(c,
			      transformedMode
			      (ConditionMode.OBSERVER_REMOVED_CONDITION),
			      parent);
	    return true;
	} else {
	    return false;
	}
    }

    // For GenericCondition onDelete
    void remCondition(C c,
		      Iterator<CondObserverImpl<C,? extends SimObject>> itc)
    {
	conditions.remove(c);
	itc.remove();
	onConditionChange(c,
			  transformedMode(ConditionMode.CONDITION_DELETED), c);
    }

    /**
     * Disassociate a condition with a condition observer.
     * @param c the condition
     * @return true on success; false on failure
     */
    @SuppressWarnings("unchecked")
    public boolean removeCondition(C c) {
	return removeCondition(c, null);
    }

    /**
     * Remove all conditions. 
     * Provided for cleanup by onDelete() for a SimObject that implements
     * a condition observer, using this class.
     */
    public void removeAllConditions() {
	Iterator<C> itc = conditions.iterator();
	while (itc.hasNext()) {
	    C c = itc.next();
	    removeCondition(c, itc);
	}
    }


    /**
     * Determine if a condition observer has (is associated with) a condition.
     * @param c the condition
     * @return true if the condition observer has condition c; false otherwise
     */
    public boolean hasCondition(C c) {
	return conditions.contains(c);
    }

    /**
     * Get a set of conditions.
     * @return the set of conditions that are associated with this 
     *         condition observer
     */
    public Set<C> conditionSet() {
	return Collections.unmodifiableSet(conditions);
    }

    boolean conditionQueueMode = false;
    Map<C,ConditionInfo> conditionChangeMap = new HashMap<C,ConditionInfo>();

    /**
     * Set whether condition-change notifications should be queued or
     * sent immediately.
     * Setting this mode to true will improve performance when many conditions
     * are changed at the same simulation time.  If the mode is changed from
     * true to false while there are some queued notifications, those will
     * be forwarded.
     * @param value true if notifications should be queued; false if they
     *              should be sent immediately.
     */
    public void setConditionChangeQMode(boolean value) {
	if (conditionQueueMode && value == false) {
	    completeNotification();
	}
	conditionQueueMode = value;
    }

    /**
     * Determine if condition-change notifications are queued.
     * @return true if queued; false otherwise.
     */
    public boolean getConditionChangeQMode() {
	return conditionQueueMode;
    }

    /**
     * Respond to a condition change.
     * Subclasses must implement this method. It will be called for
     * each condition change either immediately or (with the default
     * implementation of doConditionChange(Set<C>)) at some later time
     * depending on whether condition-queue mode is set.
     * @param c the condition that changed
     */
    abstract protected void doConditionChange(C c, ConditionMode mode,
					      SimObject source);


    /**
     * Respond to a set of condition changes.
     * A set of conditions is stored when condition-queue mode is set
     * and then used when completeNotification is called.
     * The default implementation goes through each member of the set
     * and passes that condition to doConditionChange(C).
     * @param cmap a set of conditions
     * 
     */
    protected void doConditionChange(Map<C,ConditionInfo> cmap) {
	for (Map.Entry<C,ConditionInfo> entry: cmap.entrySet()) {
	    C c = entry.getKey();
	    ConditionInfo info = entry.getValue();
	    doConditionChange(c, info.getMode(), info.getSource());
	}
    }


    void onConditionChange(C c, ConditionMode mode, SimObject source) {
	if (conditionQueueMode) {
	    conditionChangeMap.put(c, new ConditionInfo(mode, source));
	} else {
	    doConditionChange(c, mode, source);
	}
    }


    void completeNotification() {
	if (conditionQueueMode) {
	    Map<C,ConditionInfo> cMap = conditionChangeMap;
	    conditionChangeMap = new HashMap<C,ConditionInfo>();
	    doConditionChange(cMap);
	}
    }
}

//  LocalWords:  CondObserverImpl SimObject CondObserver onCondition
//  LocalWords:  Subclasses addCondition GenericCondition onDelete
//  LocalWords:  doConditionChange completeNotification cmap
