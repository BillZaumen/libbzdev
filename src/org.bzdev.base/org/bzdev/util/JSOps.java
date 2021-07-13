package org.bzdev.util;

/**
 * Common operations for JSObject and JSArray.
 * This interface is used primarily for tagging.
 */
public interface JSOps {

    /**
     * Get the number of entries for this object.
     * @return the size (that is, the number of entries)
     */
    int size();

    /**
     * Get an integer identifying an instance of a class.
     * @return the identity
     */
    long identity();

    /**
     * Get an identity.
     * This method should be used only by classes implementing this
     * interface.
     */
    default long nextIdentity() {
	return JSObject.computeNextIdentity();
    }

}

//  LocalWords:  JSObject JSArray
