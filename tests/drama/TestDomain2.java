import org.bzdev.drama.*;

public class TestDomain2 extends Domain {
    public TestDomain2(DramaSimulation sim, String name, int priority) {
	super(sim, name, true, priority);
    }

    protected void onJoinedDomain(Actor a, boolean tracking) {
	System.out.println("actor " + a.getName()
			   + " joined domain, tracking = "
			   + tracking);
    }

    protected void onLeftDomain(Actor a) {
	System.out.println("actor " + a.getName()
			   + " left domain");
    }

    protected void onJoinedDomain(Group g) {
	System.out.println("group " + g.getName()
			   + " joined domain");
    }

    protected void onLeftDomain(Group g) {
	System.out.println("group " + g.getName()
			   + " left domain");
    }

}
