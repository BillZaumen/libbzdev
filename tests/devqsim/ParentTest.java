import org.bzdev.devqsim.*;
import org.bzdev.lang.Callable;

public class ParentTest {

    static Simulation sim = new Simulation();
    static Simulation sim1 = new Simulation(sim);
    static Simulation sim2 = new Simulation(sim);

    static Callable callable1 = new Callable() {
	    public void call() {
		System.out.println("currentTime = " + sim1.currentTicks());
	    }
	};
    static Callable callable2 = new Callable() {
	    public void call() {
		System.out.println("currentTime = " + sim2.currentTicks());
	    }
	};

    public static void main(String argv[]) {
	try {
	    for (int i = 0; i < 10; i++) {
		if (i%2 == 0) {
		    sim1.scheduleCall(callable1, (long)i);
		} else {
		    sim2.scheduleCall(callable2, (long)i);
		}
	    }
	    sim.run();
	    FifoTaskQueue q1 = new FifoTaskQueue(sim, "q1", true);
	    LifoTaskQueue q11 = new LifoTaskQueue(sim1, "q1", true);
	    if (q1 != sim1.getObject("q1", FifoTaskQueue.class)
		|| q11 != sim1.getObject("q1", LifoTaskQueue.class)) {
		System.out.println("parent test failed - name lookup");
		System.exit(1);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}