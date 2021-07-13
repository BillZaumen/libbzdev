package org.bzdev.drama.common;
import org.bzdev.drama.generic.GenericCondition;
import org.bzdev.devqsim.*;
import java.util.*;

/**
 * Interface for classes that can observe conditions.
 * The implementation of the methods defined by this interface must
 * delegate their implementations to an instance of
 * CondObserverImpl, which contains package-private methods for
 * interacting with conditions.  The documentation for CondObserverImpl
 * explains how this is done.
 * @see CondObserverImpl
 */
public interface CondObserver<C extends GenericCondition, P extends SimObject>
{

    /**
     * Get the condition-observer implementation for a condition observer.
     * This method is intended for use by classes in the
     * org.bzdev.drama.generic package.
     * @return the condition-observer implementation
     */

    public CondObserverImpl<C,P> getCondObserverImpl();


    /**
     * Associate a condition with an observer.
     * @param c the condition
     * @return true on success; false on failure
     */
    public boolean addCondition(C c);

    /**
     * Disassociate a condition with a condition observer.
     * @param c the condition
     * @return true on success; false on failure
     */
    public boolean removeCondition(C c);

    /**
     * Determine if a condition observer has (is associated with) a condition.
     * @param c the condition
     * @return true if the condition observer has condition c; false otherwise
     */
    public boolean hasCondition(C c);

    /**
     * Get a set of conditions
     * @return the set of conditions that are associated with this 
     *         condition observer
     */
    public Set<C> conditionSet();

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
    public void setConditionChangeQMode(boolean value);

    /**
     * Determine if condition-change notifications are queued.
     * @return true if queued; false otherwise.
     */
    public boolean getConditionChangeQMode();
}

//  LocalWords:  CondObserverImpl
