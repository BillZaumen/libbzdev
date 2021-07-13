package org.bzdev.drama.common;
import org.bzdev.devqsim.*;

/**
 * Provides the ability to modify messages and indicate if they
 * should be dropped.
 * Message filters are used by the drama-simulation implementation
 * to allow users of the drama packages to indicate how messages
 * should be modified or when they should be dropped during message
 * transmission. Groups and domains make use of message filters when
 * these are provided.
 *
 */
public class MessageFilter {

    /**
     * Constant indicating that a message has been dropped.
     */
    public static final Object DELETED = new Object();

    /**
     * Filter a message.
     * @param msg the original message
     * @return a modified message; MessageFilter.DELETED if the message
     *         should be dropped
     */
    public Object filterMessage(Object msg)
    {
	return msg;
    }
}

//  LocalWords:  msg MessageFilter
