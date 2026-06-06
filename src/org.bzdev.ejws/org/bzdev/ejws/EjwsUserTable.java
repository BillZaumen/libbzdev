package org.bzdev.ejws;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
 *   <LI> {@link EjwsUserTable#loadEntriesFromDB(Map)}. This method will
 *       load the current map with entries from persistent storage.
 *   <LI> {@link EjwsUserTable#getEntryFromDB}. This method will
 *      fetch an entry from persistent storage.
 *   <LI> {@link EjwsUserTable#createEntryFromDB(String)}. This method
 *        creates a new entry based on data in persistent storage.
 *   <LI> {@link EjwsUserTable#copyEntryToDB(String,EjwsAuthenticator.Entry,boolean)}.
 *      This method will add an entry to persistent storage.
 *   <LI> {@link EjwsUserTable#removeEntryFromDB}.
 *      This method will remove an entry from persistent storage.
 * </UL>
 * In all three cases, the first argument is a string providing a
 * user name, often an email address.
 * 
 */
public abstract class EjwsUserTable
    <A extends EjwsAuthenticator, E extends EjwsAuthenticator.Entry>
{

    private Map<String,E> map = null;


    /**
     * Constructor.
     */
    public EjwsUserTable() {
    }

    // We need both an authenticator and a map to load entries,
    // and we should load entries only once.
    private boolean entriesLoaded = false;

    /**
     * Set the user-table's map.
     * This method is called by {@link EjwsBasicAuthenticator#setUserTable}
     * and {@link EjwsSecureBasicAuth#setUserTable}.
     * One of those methods should be called before an instance of this
     * class is used.
     * @param map the map
     */
    public void setMap(Map<String,E> map) {
	if (this.map == map) return;
	this.map = map;
	try {
	    if (auth != null && entriesLoaded == false) {
		loadEntriesFromDB(map);
		entriesLoaded = true;
	    }
	} catch (Exception e) {
	    System.err.println("loadentriesFromDB: " + e.getMessage());
	}
    }

    A auth = null;

    /**
     * Set the authenticator that uses this {@link EjwsUserTable}.
     * This method is called by {@link EjwsBasicAuthenticator#setUserTable}
     * and {@link EjwsSecureBasicAuth#setUserTable}.
     * One of those methods should be called before an instance of this
     * class is used.
     * @param auth the authenticator;
     */
    public void setAuth(A auth) {
	this.auth = auth;
	if (entriesLoaded == false && map != null) {
	    try {
		loadEntriesFromDB(map);
		entriesLoaded = true;
	    } catch (Exception e) {
	    }
	}
    }

    /**
     * Get the authenticator that uses this {@link EjwsUserTable}.
     * @return the authenticator.
     */
    protected A getAuthenticator() {
	return auth;
    }

    /**
     * Get the roles for a user.
     * @return the roles; null or an empty set if there are no roles
     */
    public Set<String> getRoles(String user) {
	return null;
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
     * Add or replace an entry.
     * Typically, one will call {@link #getEntry}, modify the
     * entry this method returns, and then call
     * {@link #putEntry(String,Entry)} to update the value.
     * If the operation fails this authenticator's map will be restored
     * to its previous value.
     * @param user the user name
     * @param entry the new entry
     * @return true if successful; false if the operation failed.
     */
    public final E putEntry(String user, E entry) {
	/*
	E old = map.put(user, entry);
	try {
	    copyEntryToDB(user, entry, (old == null));
	    return true;
	} catch (Exception e) {
	    map.put(user, old);
	    return false;
	}
	*/
	return map.put(user, entry);
    }

    /*
    public abstract EjwsAuthenticator.UserInfo
	createUser(String email, String alias, boolean active);
    */

    /**
     * Make a user active.
     * @param user the user
     * @return true on success; false on error

     */
    public final boolean makeActive(String user) {
	/*
	E existing = map.get(user);
	if (existing != null) {
	    boolean cactive = existing.isActive();
	    existing.setActive(true);
	    try {
		makeActiveInDB(user);
		return true;
	    } catch (Exception e) {
		existing.setActive(cactive);
		return false;
	    }
	} else {
	    return false;
	}
	*/
	A auth = getAuthenticator();
	try {
	    E entry = map.get(user);
	    if (entry != null) {
		if (entry.isActive()) return false;
		if (!auth.getTrustedKeyIDs().contains(user)) {
		    if (hasDB()) {
			makeActiveInDB(user);
		    }
		    entry.makeActive();
		}
		return true;
	    } else {
		if (hasDB()) {
		    createEntryFromDB(user);
		}
		entry = map.get(user);
		if (entry != null) {
		    if (entry.isActive()) return false;
		    if (hasDB()) {
			makeActiveInDB(user);
		    }
		    entry.makeActive();
		    return true;
		} else {
		    return false;
		}
	    }
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     *
     */
    public abstract void storeGPGKey(String value,
				     EjwsAuthenticator.GPGKeyIDs keyids)
	throws IllegalArgumentException, IllegalStateException, IOException;

    /**
     *
     */
    public abstract void storeSBLData(String s,
				      EjwsAuthenticator.AddStatus status)
	throws Exception;

    /**
     *
     */
    public abstract void storePW(String un, String pw, boolean isActive)
	throws Exception;


    /**
     * Update an entry in the map.
     * This method should be called when data in persistent storage
     * for the given user has changed&mdash;otherwise the persistent
     * storage and the corresponding authentication will be inconsistent.
     * <P>
     * If a new user is created by modifying persistent storage, this
     * method will make that new user available to an authenticator.
     * The protected method
     * {@link #getEntryFromDB(EjwsAuthenticator.Entry,String)}
     * is used to obtain the entry, and if that value is null, the
     * user is removed from the map; otherwise the map is updated,
     * either replacing an existing entry or creating a new one.
     * @param user the user name
     */
    public final boolean updateEntry(String user) {
	if (hasDB()) {
	    try {
		E entry = getEntry(user);
		if (entry == null) {
		    createEntryFromDB(user);
		    return true;
		} else {
		    entry = getEntryFromDB(entry, user);
		    if (entry != null) {
			map.put(user, entry);
		    } else {
			map.remove(user);
		    }
		    return true;
		}
	    } catch (Exception e) {
		return false;
	    }
	}
	// OK because nothing to do
	return true;
    }

    /**
     * Remove an entry.
     * @param user the user name
     * @return true on success; false on failure
     */
    public final boolean removeEntry(String user) {
	try {
	    if (hasDB()) {
		removeEntryFromDB(user);
	    }
	    map.remove(user);
	    return true;
	} catch (Exception e) {
	    map.remove(user);
	    return false;
	}
    }

    /**
     * Set up the map based on information in persistent storage.
     * This method is called by {@link #setMap}.
     * @param map the map
     */
    protected void loadEntriesFromDB(Map<String,E> map) throws Exception {
	return;
    }


    /**
     * Create an entry corresponding to the data in persistent storage.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     */
    protected void createEntryFromDB(String user)
	throws Exception
    {
	if (getEntry(user) != null) {
	    throw new IllegalStateException("entry exists: " + user);
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
    protected E getEntryFromDB(E oldEntry, String user) throws Exception {
	throw new UnsupportedOperationException();
    }

    /**
     * Make an entry active in persistent storage.
     * @param user the user name
     * @throws Exception if there is an error
     */
    protected void makeActiveInDB(String user) throws Exception {
	return;
    }

    /**
     * Add an entry to persistent storage.
     * This method will be used to create a new entry and to modify an
     * existing one.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     * @param entry the entry
     * @param newEntry true if the local map did not have an existing
     *        entry; false if it had an existing entry
     * @throws Exception if an error occurred, in which case the previous
     *         value in persistent storage should not be changed.
     */
    protected void copyEntryToDB(String user, E entry, boolean newEntry)
	throws Exception
    {
	return;
    }

    /**
     * Remove an entry from persistent storage.
     * <P>
     * Subclasses for which {@link hasDB()} returns <code>true</code>
     * must override this method.
     * @param user the user name
     */
    protected void removeEntryFromDB(String user) throws Exception {
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
