import org.bzdev.devqsim.*;
import org.bzdev.drama.*;

public class STraceTest {
    static DramaSimulation sim = new DramaSimulation();

    public static void main(String argv[]) throws Exception {
	sim.setStackTraceMode(true);
	sim.run();

        TestActor e1 = new TestActor(sim, "e1");
        TestActor e2 = new TestActor(sim, "e2");
        TestActor e3 = new TestActor(sim, "e3");

        Group g = new TestGroup(sim, "g");

	Domain d = new TestDomain(sim, "general", 0);


	e1.joinDomain(d, true);
        e2.joinDomain(d, true);
        e3.joinDomain(d);

        e1.joinGroup(g);
        e2.joinGroup(g);
        e3.joinGroup(g);


        e1.scheduleDefaultCall(100);
        e2.scheduleDefaultCall(200);
        e3.scheduleDefaultCall(300);
        
        System.out.println("Starting simulation runs");
        sim.run();

        System.out.println("run 1:current time = " +sim.currentTicks());

        e3.scheduleDefaultCall(300);
        e2.scheduleDefaultCall(200);
        e2.cancelDefaultCall();
        e1.scheduleDefaultCall(100);

        e1.sendNewMsg(e2, 400);

        sim.run();

        System.out.println("run 2: current time = " +sim.currentTicks());
        e1.sendNewMsg(g, 100);
        sim.run();
        System.out.println("run 3: current time = " +sim.currentTicks());

        e2.leaveGroup(g);
        e1.sendNewMsg(g, 100);

        sim.run();
        System.out.println("run 4: current time = " +sim.currentTicks());
	System.exit(0);
    }
}
