package org.bzdev.ejws;
import java.util.Map;


/**
 * Default user-table implementation.
 * This class is used by subclasses of EjwsAuthenticator to manage a
 * map that associates user names with entries used by an
 * authenticator. It provides the operations needed by
 * {@link EjwsBasicAuthenticator} and {@link EjwsSecureBasicAuth},
 * which require a simpler API than instances of
 * {@link java.util.Map} provides.
 * <P>
 * Subclasses of this class can be written so that user data is
 * stored in a database, or whatever type of persistent storage is
 * desired. A sublcass should override {@link EjwsUserTable#hasDB()}
 * so that it will always return true. A subclass should also override
 * the following protected methods:
 * <UL>
 *   <LI> {@link EjwsUserTable#getEntryFromDB(String)}. This method will
 *      fetch an entry from persistent storage.
 *   <LI> {@link EjwsUserTable#addEntryToDB(String,EjwsAuthenticator.Entry)}.
 *      This method will add an entry to persistent storage.
 *   <LI> {@link EjwsUserTable#removeEntryFromDB(String)}.
 *      This method will remove an entry from persistent storage.
 * </UL>
 * In all three cases, the first argument is a string providing a
 * user name, often an email address.
 * 
 */
public class EjwsUserTable<E extends EjwsAuthenticator.Entry> {

    private Map<String,E> map;


    /**
     * Constructor.
     */
    public EjwsUserTable(Map<String,E> map) {
	this.map = map;
	
    }

    /**
     * Add an entry.
     * @param user the user name
     */
    public final boolean addEntry(String user, E entry) {
	try {
	    if (hasDB()) {
		addEntryToDB(user, entry);
	    }
	    map.put(user, entry);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Get an entry.
     * The value returned will be the entry in the map passed to
     * the constructor. Instead of using this method, one can look
     * up the entry directly from the map.
     * @param user the user name
     * @return the entry
     */
    public final E getEntry(String user) {
	return map.get(user);
    }

    /**
     * Update an entry.
     * This method should be called when data in persistent storage
     * for the given user has changed&mdash;otherwise the persistent
     * storage and the corresponding authentication will be inconsistent.
     * <P>
     * If a new user is created by modifying persistent storage, this
     * method will make that new user available to an authenticator.
     * @param user the user name
     */
    public final void updateEntry(String user) {
	if (hasDB()) {
	    E entry = getEntryFromDB(user);
	    if (entry != null) {
		map.put(user, entry);
	    } else {
		map.remove(user);
	    }
	}
    }

    /**
     * Remove an entry.
     * @param user the user name
     */
    public final boolean removeEntry(String user) {
	try {
	    if (hasDB()) {
		removeEntryFromDB(user);
	    }
	    map.remove(user);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Get an entry corresponding to the data in persistent storage.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     * @return the entry; null if there is none in persistent storage
     */
    protected E getEntryFromDB(String user) {
	return null;
    }

    /**
     * Add an entry to persistent storage.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     * @param entry the entry
     */
    protected void addEntryToDB(String user, E entry) {
	return;
    }

    /**
     * Remove an entry from persistent storage.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     */
    protected void removeEntryFromDB(String user) {
	return;
    }

    /**
     * Determine if persistent storage exists.
     * For a given instance of EjwsUserTable, this method should
     * always return the same value.
     * @return true if persistent storage exists; false otherwise.
     */
    protected boolean hasDB() {
	return false;
    }
}
