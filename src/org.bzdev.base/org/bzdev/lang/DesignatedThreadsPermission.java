package org.bzdev.lang;
import java.security.*;

/**
 * Permission to allow adding a thread as a designated thread to
 * a designated-threads security manager.
 * @see DesignatedThreadsSM
 */
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
