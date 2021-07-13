package org.bzdev.drama.common;
import org.bzdev.devqsim.*;

//@exbundle org.bzdev.drama.common.lpack.ExceptionString

/**
 * Subsidiary information for groups.  When an element, an actor or
 * another group, joins a group, an object containing additional
 * information about the group.  A specific type of group may define
 * a "join" method that stores some its arguments in a GroupInfo
 * subclass specific to that group.

 * This object must be subclassed with the clone() method
 * implemented.
 * @see java.lang.Object#clone()
 * @see java.lang.Cloneable
 */
public class GroupInfo implements Cloneable {

    private String errorMsg(String key, Object... args) {
	return ExceptionString.errorMsg(key, args);
    }

    /**
     * Create a copy.
     * @return a copy obtained by cloning this object
     * @exception IllegalStateException info object not cloneable in spite
     *            of having implemented the Cloneable interface
     */
    public GroupInfo copy() throws IllegalStateException {
	try {
	    return (GroupInfo)clone();
	} catch (CloneNotSupportedException e) {
	    String msg = errorMsg("notClonableState");
	    throw new IllegalStateException(msg, e);
	}
    }
}

//  LocalWords:  exbundle GroupInfo subclassed IllegalStateException
//  LocalWords:  cloneable notClonableState
