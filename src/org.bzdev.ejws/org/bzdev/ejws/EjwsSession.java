package org.bzdev.ejws;
import org.bzdev.lang.UnexpectedExceptionError;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Object representing an EJWS session.
 * The object provides information about a session.
 */
public class EjwsSession {

    static SecureRandom sr = new SecureRandom();
    String id;
    boolean isNewID = true; // modified by EjwsSessionMgr
    EjwsSessionMgr manager;

    private String newID() throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    byte[] bytes = new byte[8];
	    sr.nextBytes(bytes);
	    md.update(bytes);
	    bytes[7] = (byte)(lastAccessedTime & 0xFF);
	    bytes[6] = (byte)((lastAccessedTime >>> 8) & 0xFF);
	    bytes[5] = (byte)((lastAccessedTime >>> 16) & 0xFF);
	    bytes[4] = (byte)((lastAccessedTime >>> 24) & 0xFF);
	    bytes[3] = (byte)((lastAccessedTime >>> 32) & 0xFF);
	    bytes[2] = (byte)((lastAccessedTime >>> 40) & 0xFF);
	    bytes[1] = (byte)((lastAccessedTime >>> 48) & 0xFF);
	    bytes[0] = (byte)((lastAccessedTime >>> 56) & 0xFF);
	    md.update(bytes);
	    sr.nextBytes(bytes);
	    return Base64.getEncoder().withoutPadding()
		.encodeToString(md.digest(bytes))
		.replaceAll("/","%");
    }

    // must be called when synchronized on the manager
    EjwsSession(EjwsSessionMgr manager) {
	try {
	    id = newID();
	    this.manager = manager;
	    manager.map.put(id, this);
	    manager.set.add(this);
	} catch (Exception e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    long creationTime = System.currentTimeMillis();
    long lastAccessedTime = creationTime;
    int maxInactiveInterval = 3600;

    void updateLastAccessTime() {
	lastAccessedTime = System.currentTimeMillis();
    }

    /**
     * Get the session ID.
     * @return the session ID
     */
    public String getID() {
	return id;
    }

    /**
     * Get the session creation time.
     * Time is measured in seconds, starting at midnight, January 1, 1970
     * UTC.
     * @return the session creation time.
     */
    public long getCreationTime() {
	return creationTime;
    }

    /**
     * Get the session's last-accessed time.
     * Time is measured in seconds, starting at midnight, January 1, 1970
     * UTC.
     * @return the session last-accessed time.
     */
    public long getLastAccessedTime() {
	return lastAccessedTime;
    }

    /**
     * Get the maximum time interval over which a session can be
     * inactive
     * @return the time interval in seconds; 0 if no limit
     */
    public int getMaxInactiveInterval() {
	return maxInactiveInterval;
    }

    /**
     * Set the maximum time interval over which a session can be
     * inactive.
     * <P>
     * This method is called by
     * {@link org.bzdev.net.HttpServerRequest#setMaxInactiveInterval(int)}
     * and {@link WebMap.RequestInfo#setMaxInactiveInterval(int)}:
     * instances of {@link EjwsSession} are normally not visible
     * outside of this package.
     * @param value the time interval in seconds
     */
    public void setMaxInactiveInterval(int value) {
	maxInactiveInterval = value;
    }

    /**
     * Determine if the session  ID is a new session ID.
     * @return true if the session ID is a new ID; false otherwise
     */
    public boolean isNew() {
	return isNewID;
    }

    // used by WebMap.RequestInfo.changeSessionID()
    String changeSessionID() {
	synchronized(manager) {
	    try {
		String oldID = id;
		String nextid = newID();
		id = nextid;
		manager.changeSessionID(this, oldID, nextid);
		return nextid;
	    } catch (NoSuchAlgorithmException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

    }
}


//  LocalWords:  EJWS EjwsSessionMgr
