package org.bzdev.lang;
import java.security.*;

/**
 * Permission to allow adding a thread as a designated thread to
 * a designated-threads security manager.
 * <P>
 * Deprecated because the SecurityManager class is being removed
 * from Java.
 * @see DesignatedThreadsSM
 */
@Deprecated
public final class DesignatedThreadsPermission extends BasicPermission {
    /**
     * Constructor.
     */
    public DesignatedThreadsPermission() {
	super("<designated-threads permission>");
    }

    /**
     * Constructor with actions.
     * @param name ignored
     * @param actions ignored
     */
    public DesignatedThreadsPermission(String name, String actions) {
	this();
    }
}

//  LocalWords:  DesignatedThreadsSM
