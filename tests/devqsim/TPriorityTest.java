import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class TPriorityTest {
    static Simulation sim = new Simulation();

    static Callable createCallable(final String text) {
	return new Callable() {
	    public void call() {
		System.out.println(text + ": current time = "
				   + sim.currentTicks());
	    }
	};
    }



    static public void main(String argv[]) {
	try {
	    sim.scheduleCallWP(createCallable("hello 2"), 1.0);
	    sim.scheduleCallWP(createCallable("hello 1"), -1.0);
	    sim.scheduleCall(createCallable("goodbye 2"), 10, 1.0);
	    sim.scheduleCall(createCallable("goodbye 1"), 10, -1.0);
	    sim.run();

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("hello 4");
			TaskThread.pause(10, -1.0);
			System.out.println("goodbye 3");
		    }
		}, 10, 1.0);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("hello 3");
			TaskThread.pause(10, 1.0);
			System.out.println("goodbye 4");
		    }
		}, 10, -1.0);
	    sim.run();
	    System.out.println("------- FIFO test ----------");

	    final FifoTaskQueue cq = new FifoTaskQueue(sim, "cq", true);
	    sim.scheduleCall(createCallable("goodbye"), 10);
	    cq.add(createCallable("hello"), 10, -1.0);

	    cq.add(createCallable("goodbye 1"), 10, 1.0);
	    cq.add(createCallable("goodbye 2"), 10, 1.0);
	    sim.scheduleCall(createCallable("hello 1"), 20);
	    sim.scheduleCall(createCallable("hello 2"), 30);
	    sim.run();

	    System.out.println("------- LIFO test ----------");

	    final LifoTaskQueue lcq = new LifoTaskQueue(sim, "lcq", true);
	    lcq.freeze(true);
	    lcq.add(createCallable("goodbye 2"), 10, 1.0);
	    lcq.add(createCallable("goodbye 1"), 10, 1.0);
	    lcq.freeze(false);
	    sim.scheduleCall(createCallable("hello 1"), 10);
	    sim.scheduleCall(createCallable("hello 2"), 20);
	    sim.run();
	    lcq.preempt(true);
	    lcq.add(createCallable("goodbye 3"), 10);
	    lcq.add(createCallable("hello 3"), 5, 1.0);
	    sim.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
