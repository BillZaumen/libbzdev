import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.drama.common.ConditionMode;


public class DramaExample2 {
    public static void main (String argv[])
	throws Exception
    {
	DramaSimulation sim =
	    new DramaSimulation();

	CommDomain topdomain = new
	    CommDomain(sim, "topdomain", 0);

	CommDomain domain = new
	    CommDomain(sim, "domain", topdomain, 0);

	CommDomain subdomain1 = new
	    CommDomain(sim, "subdomain1", domain, 0);
	CommDomain subdomain2 = new
	    CommDomain(sim, "subdomain2", domain, 0);
	CommDomain subdomain3 = new
	    CommDomain(sim, "subdomain3", domain, 0);
	CommDomain subdomain4 = new
	    CommDomain(sim, "subdomain4", domain, 0);
	CommDomain subdomain5 = new
	    CommDomain(sim, "subdomain5", topdomain, 0);
	CommDomain subdomain6 = new
	    CommDomain(sim, "subdomain6", subdomain5, 0);
	
	TestMsgForwardingInfo mfi = new
	    TestMsgForwardingInfo(sim, "mfi", true);

	topdomain.setMessageForwardingInfo(mfi);
	domain.setMessageForwardingInfo(mfi);
	subdomain1.setMessageForwardingInfo(mfi);
	subdomain2.setMessageForwardingInfo(mfi);
	subdomain3.setMessageForwardingInfo(mfi);
	subdomain4.setMessageForwardingInfo(mfi);
	subdomain5.setMessageForwardingInfo(mfi);
	subdomain6.setMessageForwardingInfo(mfi);

	TestActor adom = new TestActor(sim, "adom", true);

	TestActor a1 = new TestActor(sim, "a1", true);
	TestActor a2 = new TestActor(sim, "a2", true);
	TestActor a3 = new TestActor(sim, "a3", true);
	TestActor a4 = new TestActor(sim, "a4", true);
	TestActor atop = new TestActor(sim, "atop", true);
	TestActor a5 = new TestActor(sim, "a5", true);
	TestActor a6 = new TestActor(sim, "a6", true);

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

	mfi.setTraceModes(true, true);

	System.out.println("------------------");
	a2.sendNewMsg(a3);
	sim.run();
	/*
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
	*/
	System.out.println("------------------");
	a1.sendNewMsg(g);
	sim.run();
    }
}
