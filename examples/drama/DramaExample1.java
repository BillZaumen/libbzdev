import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.drama.common.ConditionMode;


public class DramaExample1 {
    public static void main (String argv[])
	throws Exception
    {
	DramaSimulation sim =
	    new DramaSimulation();

	TestActor actor1 =
	    new TestActor(sim, "actor1", true);
	TestActor actor2 =
	    new TestActor(sim, "actor2", true);
	TestActor actor3 =
	    new TestActor(sim, "actor3", true);
	TestActor actor4
	    = new TestActor(sim, "actor4", true);

	Group g = new TestGroup(sim, "g", true);

	actor1.joinGroup(g);
	actor2.joinGroup(g);
	actor3.joinGroup(g);

	Domain d = new Domain(sim, "general", true, 0);

	actor1.joinDomain(d, true);
	actor2.joinDomain(d, true);
	actor3.joinDomain(d, false);
	g.joinDomain(d);

	TestCondition bc = 
	    new TestCondition(sim, "condition", 
			      true, false);
	d.addCondition(bc);
	actor4.addCondition(bc);

	bc.setValue(true);
	bc.notifyObservers();
	bc.completeNotification();

        System.out.println("run 1:current time = "
			   + sim.currentTicks());
	actor1.scheduleDefaultCall(100);
	sim.run();
        System.out.println("run 2:current time = "
			   + sim.currentTicks());
	actor1.scheduleDefaultTask(100);
	sim.run();
        System.out.println("run 3:current time = "
			   + sim.currentTicks());
	actor1.sendNewMsg(actor2, 100);
	sim.run();
        System.out.println("run 4:current time = "
			   + sim.currentTicks());
	actor1.sendNewMsg(g, 100);
	sim.run();
        System.out.println("end of runs: :current time = "
			   + sim.currentTicks());
    }
}
