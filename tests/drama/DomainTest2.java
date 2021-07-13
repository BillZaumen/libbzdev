import org.bzdev.drama.*;

public class DomainTest2 {
    static DramaSimulation sim = new DramaSimulation();

    public static void main(String argv[]) throws Exception {

	Domain d = new TestDomain2(sim, "d1", 1);
	Actor a = new TestActor(sim, "a1", true);
	Group g = new TestGroup(sim, "g1");
	DomainMember shared = new DomainMember(sim, "shared", true);
	
	System.out.println("actor a1 joining domain d, no tracking");
	System.out.println(a.joinDomain(d, false));
	System.out.println(a.joinDomain(d, false));
	System.out.println("group g1 joining domain d");
	g.joinDomain(d);
	g.joinDomain(d);
	System.out.println("actor a1 leaving domaim d");
	System.out.println(a.leaveDomain(d));
	System.out.println(a.leaveDomain(d));
	System.out.println("group g1 leaving domain d");
	g.leaveDomain(d);
	g.leaveDomain(d);
	a.setSharedDomainMember(shared);
	shared.printConfiguration(System.out);
	System.out.println("try printing config again with more indentation");
	shared.printConfiguration("        ", System.out);
	System.out.println("shared domain member joining d, no tracking");
	System.out.println(shared.joinDomain(d, false));
	System.out.println(shared.joinDomain(d, false));
	System.out.println("shared domain member leaving domain d");
	System.out.println(shared.leaveDomain(d));
	System.out.println(shared.leaveDomain(d));
	
	System.out.println("actor a1 joining domain d, tracking");
	System.out.println(a.joinDomain(d, true));
	System.out.println(a.joinDomain(d, true));
	System.out.println("actor a1 leaving domaim d");
	System.out.println(a.leaveDomain(d));
	System.out.println(a.leaveDomain(d));


	System.out.println("set actor a1 to use domain \"shared\"");
	a.setSharedDomainMember(shared);
	System.out.println("shared domain member joining d, tracking");
	System.out.println(shared.joinDomain(d, true));
	System.out.println(shared.joinDomain(d, true));
	Actor a2 = new TestActor(sim, "a2", true);
	System.out.println("set a2's domain member to \"shared\"");
	a2.setSharedDomainMember(shared);
	shared.printConfiguration(System.out);
	System.out.println("shared domain member leaving domain d");
	System.out.println(shared.leaveDomain(d));
	System.out.println(shared.leaveDomain(d));

	System.out.println("put \"shared\" back into domain d");
	shared.joinDomain(d);
	System.out.println("remove a1 from the shared domain member");
	a.setSharedDomainMember(null);
	shared.printConfiguration(System.out);
	System.out.println("a1 new joins domain d");
	a.joinDomain(d);
	System.out.println("add group g1 to d once more");
	g.joinDomain(d);
	System.out.println("now try deleting the domain d");
	d.delete();
	shared.printConfiguration(System.out);

	System.out.println("Try a new domain d2 & add a1 and a2 (via shared)");
	d = new TestDomain2(sim, "d2", 1);
	a.joinDomain(d);
	shared.joinDomain(d);
	System.out.println("also add the group g1");
	g.joinDomain(d);
	System.out.println("Now delete a1, a2, and g1");
	g.delete();
	a.delete();
	a2.delete();
    }
}
