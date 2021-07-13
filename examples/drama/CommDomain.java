import org.bzdev.drama.*;
import org.bzdev.drama.common.MessageFilter;

public class CommDomain extends Domain {
    public CommDomain(DramaSimulation sim, String name, int priority) {
	super(sim, name, true, priority);
	configureAsCommunicationDomain("network");
    }

    public CommDomain(DramaSimulation sim, String name, Domain parent,
			  int priority) {
	super(sim, name, true, parent, priority);
	configureAsCommunicationDomain("network");
    }
}
