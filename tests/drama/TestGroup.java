import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.util.*;

import java.util.*;

public class TestGroup extends Group {

    public TestGroup(DramaSimulation sim, String name) {
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
}
