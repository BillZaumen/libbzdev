import org.bzdev.drama.*;

public class TestActorFactory extends AbstractActorFactory<TestActor> {
    
    public TestActorFactory() {
	this(null);
    }

    public TestActorFactory(DramaSimulation sim) {
	super(sim);
    }

    protected TestActor newObject(String name) {
	return new TestActor(getSimulation(), name, willIntern()); 
    }
}
