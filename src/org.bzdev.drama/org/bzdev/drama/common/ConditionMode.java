package org.bzdev.drama.common;

/**
 * Mode to describe onConditionChange events.
 * This enumeration is used as the second argument to a
 * condition observer's onConditionChange method.
 */
public enum ConditionMode {
    /**
     * A condition was added to a condition observer
     */
    OBSERVER_ADDED_CONDITION,
    /**
     * A condition was added to a domain. Used when
     * domain members are notified.
     */
    DOMAIN_ADDED_CONDITION,
    /**
     * A condition observer (e.g., an actor) joined a
     * domain.
     */
    OBSERVER_JOINED_DOMAIN,
    /**
     * A condition changed. I.e., the condition's notifyObservers
     * method was called.
     */
    OBSERVER_NOTIFIED,
    /**
     * A domain removed a condition.
     */
    DOMAIN_REMOVED_CONDITION,
    /**
     * An observer (e.g., an actor) asked to leave a domain.
     */
    OBSERVER_LEFT_DOMAIN,
    /**
     * An observer removed a condition.
     */
    OBSERVER_REMOVED_CONDITION,
    /**
     * A condition was deleted.
     */
    CONDITION_DELETED
}

//  LocalWords:  onConditionChange notifyObservers
