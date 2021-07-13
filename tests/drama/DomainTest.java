import org.bzdev.drama.*;

class OurDoubleCondition extends DoubleCondition {
    public OurDoubleCondition(DramaSimulation sim, String name,
			       boolean intern) {
	super(sim, name, intern);
    }
    public void setValue(double value) {
	super.setValue(value);
	notifyObservers();
	completeNotification();
    }
}


public class DomainTest {
    static DramaSimulation sim = new DramaSimulation();

    public static void main(String argv[]) throws Exception {

	OurDoubleCondition dc1 = new OurDoubleCondition(sim, "dc1", true);
	OurDoubleCondition dc2 = new OurDoubleCondition(sim, "dc2", true);
	OurDoubleCondition dc3 = new OurDoubleCondition(sim, "dc3", true);
	OurDoubleCondition dc4 = new OurDoubleCondition(sim, "dc4", true);
	OurDoubleCondition dc5 = new OurDoubleCondition(sim, "dc5", true);

	Domain d1 = new Domain(sim, "d1", true);
	Domain d2 = new Domain(sim, "d2", true);
	Domain d3 = new Domain(sim, "d3", true);
	Domain d4 = new Domain(sim, "d4", true);

	TestActor a1 = new TestActor(sim, "a1", true);
	Actor a2 = new TestActor(sim, "a2", true);
	Actor a3 = new TestActor(sim, "a3", true);
	Actor a4 = new TestActor(sim, "a4", true);

	System.out.println("test behavior of onConditionChange");
	a1.testConditionHandling(dc1);

	System.out.println();
	System.out.println("configure:");
	System.out.println();

	DomainMember shared = new DomainMember(sim, "shared", true);
	System.out.println("d1 adds condition dc1 ...");
	d1.addCondition(dc1);
	System.out.println("d2 adds condition dc2 ...");
	d2.addCondition(dc2);
	System.out.println("a1 uses shared domain member ...");
	a1.setSharedDomainMember(shared);
	System.out.println("shared domain member joins domain d1 ...");
	shared.joinDomain(d1, true);
	System.out.println("shared domain member joins domain d2 ...");
	shared.joinDomain(d2, true);
	System.out.println("a2 uses shared domain member ...");
	a2.setSharedDomainMember(shared);
	System.out.println("shared domain member joins domain d3 ...");
	shared.joinDomain(d3, true);
	System.out.println("d3 adds condition dc3 ...");
	d3.addCondition(dc3);
	System.out.println("d4 adds condition dc4 ...");
	d4.addCondition(dc4);
	System.out.println("a1 joins domain d4 ...");
	a1.joinDomain(d4, true);
	a3.joinDomain(d4, true);
	System.out.println("d4 adds condition dc5 ...");
	d4.addCondition(dc5);
	System.out.println();
	System.out.println("now change conditions");
	System.out.println();
	dc1.setValue(1.0);
	dc2.setValue(2.0);
	dc3.setValue(3.0);
	dc4.setValue(4.0);
	dc5.setValue(5.0);
	System.out.println();
	System.out.println("now try deletions");
	System.out.println();
	System.out.println("deleting dc1 ...");
	dc1.delete();
	System.out.println("a1 leaving domain d4 ...");
	a1.leaveDomain(d4);
	System.out.println("d2 removing condition dc2 ...");
	d2.removeCondition(dc2);
	System.out.println("shared leaving domain d3 ...");
	shared.leaveDomain(d3);
	System.out.println("d4 removing condition dc4 ...");
	d4.removeCondition(dc4);

	System.out.println("d2 adding condition dc2 ...");
	d2.addCondition(dc2);
	System.out.println("a1 removing shared domain member ... ");
	a1.setSharedDomainMember(null);
	System.out.println();
	System.out.println("now change conditions");
	System.out.println();
	dc2.setValue(2.0);
	dc3.setValue(3.0);
	dc4.setValue(4.0);
	dc5.setValue(5.0);
	System.exit(0);
    }
}
