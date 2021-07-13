package org.bzdev.net;

/**
 * Operations for managing an  HTTP session.
 * The type parameter T is the type of the session implementation
 * and can be used to reference an application-specific data structure.
 * @see org.bzdev.ejws.EjwsSession
 */
public interface HttpSessionOps<T> {

    /**
     * Remove a session ID from this object.
     * @param sid the session ID
     */
    void remove(String sid);

    /**
     * Add a session ID to this object.
     * @param sid the session ID
     */
    void add(String sid);

    /**
     * Determine if this object recognizes a session ID.
     * @param sid the session ID
     * @return true if the session exists; false otherwise
     */
    boolean contains(String sid);

    /**
     * Get the session implementation associated with a session ID.
     * This method should create a new session implementation if
     * necessary.
     * @param sid the session ID
     */
    T get(String sid);
}

//  LocalWords:  sid
