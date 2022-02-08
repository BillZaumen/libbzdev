package org.bzdev.net;

/**
 * Operations for managing an  HTTP session.
 * The type parameter T is the type of the session implementation
 * and can be used to reference an application-specific data structure.
 * Each session is represented by a string that provides a session ID.
 * <P>
 * For the class {@link org.bzdev.ejws.EmbeddedWebServer}, the method
 * {@link org.bzdev.ejws.EmbeddedWebServer#addSessionFilter(String,HttpSessionOps)}
 * must be called to install an instance of this class and that method
 * will pass its {@link HttpSessionOps} to
 * {@link org.bzdev.ejws.EjwsSession}'s constructor. Application-specific
 * code will typically have a reference to this class but will just use the
 * {@link HttpSessionOps#contains(String)} or
 * {@link HttpSessionOps#get(String)} methods, the latter providing an
 * object represents the state of a session.
 * @see org.bzdev.ejws.EjwsSession
 */
public interface HttpSessionOps {

    /**
     * Remove a session ID from this object.
     * @param sid the session ID
     */
    void remove(String sid);

    /**
     * Add a session ID to this object.
     * This method is responsible for creating a new session
     * implementation, but must not change an existing one.
     * If the session ID already exists, nothing is added or removed.
     * @param sid the session ID
     * @param state an object representing the state of a session
     */
    void put(String sid,Object state);

    /**
     * Rename a session implementation by changing the session ID
     * referencing it.
     * @param oldID the existing sessionID
     * @param newID the new sessionID
     * @exception IllegalStateException oldID was not already added or
     *            newID was already added
     */
    void rename(String oldID, String newID) throws IllegalStateException;

    /**
     * Determine if this object recognizes a session ID.
     * @param sid the session ID
     * @return true if the session exists; false otherwise
     */
    boolean contains(String sid);

    /**
     * Get the session implementation associated with a session ID.
     * @param sid the session ID
     */
   Object get(String sid);
}

//  LocalWords:  sid
