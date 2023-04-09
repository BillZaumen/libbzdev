package org.bzdev.util;

/**
 * Common operations for JSObject and JSArray.
 * This interface is used primarily when a method's argument
 * could be either a instance of {@link JSArray} or
 * {@link JSObject}, or a subclass of either of these two
 * classes.
 * <P>
 * The method \texttt{identity} can be used to return an integer
 * code identifying an instance.  The implementation should
 * call \texttt{nextIdentity} in a constructor and use that value
 * to ensure that each object gets a unique number.
 */
public interface JSOps {

    /**
     * Get the number of entries for this object.
     * The number of entries for a {@link JSArray} is the array length
     * whereas the number of entries for a {@link JSObject} is the
     * number of properties it has.
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
     * @return a new value to use as an identity
     */
    default long nextIdentity() {
	return JSObject.computeNextIdentity();
    }

}

//  LocalWords:  JSObject JSArray
