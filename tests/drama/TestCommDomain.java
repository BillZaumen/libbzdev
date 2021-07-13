import org.bzdev.drama.*;
import org.bzdev.drama.common.MessageFilter;

public class TestCommDomain extends Domain {
    public TestCommDomain(DramaSimulation sim, String name, int priority) {
	super(sim, name, true, priority);
	configureAsCommunicationDomain("network");
    }

    public TestCommDomain(DramaSimulation sim, String name, Domain parent,
			  int priority) {
	super(sim, name, true, parent, priority);
	configureAsCommunicationDomain("network");
    }
}
