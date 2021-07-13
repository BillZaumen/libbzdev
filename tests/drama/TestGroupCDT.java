import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;
import org.bzdev.util.*;

import java.util.*;

public class TestGroupCDT extends Group {

    public TestGroupCDT(DramaSimulation sim, String name) {
	super(sim, name, true);
    }

    public Iterator<Actor> actorRecipientIterator(Actor source) {
	Iterator<Actor> it = getActorMembers().iterator();
	FilteringIterator<Actor> fit = 
	    new FilteringIterator<Actor>(it);
	fit.addToFilter(source);
	return fit;
    }

    public Iterator<Group> groupRecipientIterator(Actor source) {
	return getGroupMembers().iterator();
    }

    public long getDelay(Actor source, Object msg, Actor dest) {
	return 1L;
	
    }
    public long getDelay(Actor source, Object msg, Group dest) {
	return 1L;
    }

    private MessageFilter messageFilter = new MessageFilter() {
	    public Object filterMessage(Object msg) {
		if (msg instanceof TestMessage) {
		    return new
			TestMessage("+" + ((TestMessage)msg).getString());
		} else {
		    return msg;
		}
	    }
	};


    public MessageFilter getMessageFilter(Actor Source, Actor dest) {
	return messageFilter;
    }
    public MessageFilter getMessageFilter(Actor Source, Group dest) {
	return messageFilter;
    }

}
