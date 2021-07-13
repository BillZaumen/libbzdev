import org.bzdev.drama.*;

public class TestDomain extends Domain {
    public TestDomain(DramaSimulation sim, String name, int priority) {
	super(sim, name, true, priority);
	configureAsCommunicationDomain("network");
    }
}
