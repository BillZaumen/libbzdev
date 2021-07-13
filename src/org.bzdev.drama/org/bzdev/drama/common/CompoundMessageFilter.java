package org.bzdev.drama.common;
import org.bzdev.devqsim.*;
import java.util.LinkedList;

/**
 * A message filter that applies a series of message filters sequentially.
 * The filters are applied in the order in which they are added by
 * calls to {@link #addFilter(MessageFilter) addFilter}.  By default
 * and for efficiency reasons regarding the use of this class in
 * GenericDomain, no test for a circular filter (one in which a filter
 * is added to itself) is done.  A method named circularityTest is
 * provided for cases in which such a test is necessary. A circularity
 * test is not needed in the case where a CompoundMessageFilter is
 * created and never added to another CompoundMessageFilter.
 */
public class CompoundMessageFilter extends MessageFilter {
    LinkedList<MessageFilter> flist = new LinkedList<>();

    /**
     * Add a filter.
     * @param filter the filter to add
     */
    public void addFilter(MessageFilter filter) {
	flist.add(filter);
    }

    /**
     * Test that adding a filter will not create a circular compound
     * filter - one whose filterMessage method will never terminate.
     * @param filter the message filter that would be added
     * @return true if the circularity test passes; false otherwise
     */
    public boolean circularityTest(MessageFilter filter) {
	return circularityTest(filter, this);
    }

    boolean circularityTest(MessageFilter filter,
			    CompoundMessageFilter me)
    {
	if (filter == me) return false;
	for (MessageFilter f: flist) {
	    if (f instanceof CompoundMessageFilter) {
		CompoundMessageFilter fc = (CompoundMessageFilter)f;
		if (!fc.circularityTest(filter, me)) {
		    return false;
		}
	    }
	}
	return true;
    }

    @Override 
    public Object filterMessage(Object msg) {
	if (msg == MessageFilter.DELETED) return MessageFilter.DELETED;
	for (MessageFilter filter: flist) {
	    msg = filter.filterMessage(msg);
	    if (msg == MessageFilter.DELETED) return MessageFilter.DELETED;
	}
	return msg;
    }
}

//  LocalWords:  addFilter MessageFilter GenericDomain filterMessage
//  LocalWords:  circularityTest CompoundMessageFilter
