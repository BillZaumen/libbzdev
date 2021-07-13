import org.bzdev.drama.*;
import org.bzdev.drama.common.CommDomainInfo;
import org.bzdev.drama.common.CommDomainType;
import java.util.Set;

public class CommDomainTest {
    static DramaSimulation sim;
    public static void main(String argv[]) throws Exception {

	boolean traceDelays = false;
	boolean traceFilters = false;

	int ind = 0;
	while (ind < argv.length) {
	    if (argv[ind].equals("--traceDelays")) traceDelays = true;
	    else if (argv[ind].equals("--traceFilters")) traceFilters = true;
	    ind++;
	}


	sim = new DramaSimulation();

	TestCommDomain topdomain = new TestCommDomain(sim, "topdomain", 0);

	TestCommDomain domain = new TestCommDomain(sim, "domain", topdomain, 0);

	TestCommDomain subdomain1 = new TestCommDomain(sim, "subdomain1",
						       domain, 0);
	TestCommDomain subdomain2 = new TestCommDomain(sim, "subdomain2",
						       domain, 0);
	TestCommDomain subdomain3 = new TestCommDomain(sim, "subdomain3",
						       domain, 0);
	TestCommDomain subdomain4 = new TestCommDomain(sim, "subdomain4",
						       domain, 0);
	TestCommDomain subdomain5 = new TestCommDomain(sim, "subdomain5",
						       topdomain, 0);
	TestCommDomain subdomain6 = new TestCommDomain(sim, "subdomain6",
						       subdomain5, 0);
	
	TestMsgForwardingInfo mfi1 =
	    new TestMsgForwardingInfo(sim, "mfi1", true);
	TestMsgForwardingInfo mfi2 =
	    new TestMsgForwardingInfo(sim, "mfi2", true);
	TestMsgForwardingInfo mfi3 =
	    new TestMsgForwardingInfo(sim, "mfi3", true);
	TestMsgForwardingInfo mfi4 =
	    new TestMsgForwardingInfo(sim, "mfi4", true);
	TestMsgForwardingInfo mfi5 =
	    new TestMsgForwardingInfo(sim, "mfi5", true);
	TestMsgForwardingInfo mfi6 =
	    new TestMsgForwardingInfo(sim, "mfi6", true);
	TestMsgForwardingInfo mfi7 =
	    new TestMsgForwardingInfo(sim, "mfi7", true);
	TestMsgForwardingInfo mfi8 =
	    new TestMsgForwardingInfo(sim, "mfi8", true);

	topdomain.setMessageForwardingInfo(mfi1);
	domain.setMessageForwardingInfo(mfi2);
	subdomain1.setMessageForwardingInfo(mfi3);
	subdomain2.setMessageForwardingInfo(mfi4);
	subdomain3.setMessageForwardingInfo(mfi5);
	subdomain4.setMessageForwardingInfo(mfi6);
	subdomain5.setMessageForwardingInfo(mfi7);
	subdomain6.setMessageForwardingInfo(mfi8);

	TestActor adom = new TestActor(sim, "adom");

	TestActor a1 = new TestActor(sim, "a1");
	TestActor a2 = new TestActor(sim, "a2");
	TestActor a3 = new TestActor(sim, "a3");
	TestActor a4 = new TestActor(sim, "a4");
	TestActor atop = new TestActor(sim, "atop");
	TestActor a5 = new TestActor(sim, "a5");
	TestActor a6 = new TestActor(sim, "a6");

	TestGroupCDT g = new TestGroupCDT(sim, "g");

	TestGroupCDT gg = new TestGroupCDT(sim, "gg");

	adom.joinDomain(domain);
	a1.joinDomain(subdomain1);
	g.joinDomain(subdomain2);
	gg.joinDomain(subdomain4);
	a2.joinDomain(subdomain3);
	a3.joinDomain(subdomain3);
	a4.joinDomain(subdomain4);

	atop.joinDomain(topdomain);
	a5.joinDomain(subdomain5);
	a6.joinDomain(subdomain6);

	a1.joinGroup(g);
	a2.joinGroup(gg);
	a3.joinGroup(g);
	a4.joinGroup(g);
	gg.joinGroup(g);

	System.out.println("trying findCommDomain(a1, null, a3)");
	CommDomainInfo<Domain> cdinfo =
	    sim.findCommDomain(a1, null, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a1, null, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(a1, null, g)");
	cdinfo = sim.findCommDomain(a1, null, g);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a1, null, g)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(g, null, gg)");
	cdinfo = sim.findCommDomain(g, null, gg);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(g, null, gg)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(g, null, a3)");
	cdinfo = sim.findCommDomain(g, null, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(g, null, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(a2, null, a3)");
	cdinfo = sim.findCommDomain(a2, null, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a2, null, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}
	// add a number of domain types to check the search.
	Set<CommDomainType> cdset = CommDomainType.typeSet("a", "b", "network",
							   "c", "d");
	System.out.println("trying findCommDomain(a1, cdset, a3)");
	cdinfo = sim.findCommDomain(a1, cdset, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a1, cdset, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(a1, cdset, g)");
	cdinfo = sim.findCommDomain(a1, cdset, g);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a1, cdset, g)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(g, cdset, gg)");
	cdinfo = sim.findCommDomain(g, cdset, gg);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(g, cdset, gg)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(g, cdset, a3)");
	cdinfo = sim.findCommDomain(g, cdset, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(g, cdset, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}

	System.out.println("trying findCommDomain(a2, cdset, a3)");
	cdinfo = sim.findCommDomain(a2, cdset, a3);
	if (cdinfo == null) {
	    System.out.println("    cdinfo = null");
	} else {
	    System.out.println("    cdinfo: src = "
			       + cdinfo.getSourceDomain().getName()
			       + ", ancestor = "
			       + cdinfo.getAncestorDomain().getName()
			       + ", dest = "
			       + cdinfo.getDestDomain().getName());
	    System.out.print("    MFIs: ");
	    for (String name: sim.findMsgFrwdngInfo(a2, cdset, a3)) {
		System.out.print(name + " ");
	    }
	    System.out.println();
	}


	mfi1.setTraceModes(traceDelays, traceFilters);
	mfi2.setTraceModes(traceDelays, traceFilters);
	mfi3.setTraceModes(traceDelays, traceFilters);
	mfi4.setTraceModes(traceDelays, traceFilters);
	mfi5.setTraceModes(traceDelays, traceFilters);
	mfi6.setTraceModes(traceDelays, traceFilters);
	mfi7.setTraceModes(traceDelays, traceFilters);
	mfi8.setTraceModes(traceDelays, traceFilters);

	System.out.println("------------------");
	if (!a2.sendNewMsg(a3)) System.out.println("sending msg failed");
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(adom);
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(a2);
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(atop);
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(a5);
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(a6);
	sim.run();
	System.out.println("------------------");
	atop.sendNewMsg(a5);
	sim.run();
	System.out.println("------------------");
	atop.sendNewMsg(a6);
	sim.run();
	System.out.println("------------------");
	a1.sendNewMsg(a6);
	sim.run();

	System.out.println("------------------");
	if (!a1.sendNewMsg(g)) System.out.println("sending msg failed");
	sim.run();
	System.exit(0);
    }
}
