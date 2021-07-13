import org.bzdev.devqsim.Simulation;
import org.bzdev.lang.Callable;

public class TestSim2 {

    static Callable nextCall(final Simulation sim, final int i) {
	return () -> {
	    System.out.println("task is at simulation time "
			       + sim.currentTicks()
			       + " [i= " + i +"]");
	    if (i < 4) {
		sim.scheduleCall(nextCall(sim, i+1), 10);
	    }
	};
    }

    public static void main(String argv[]) throws Exception {

	Simulation sim = new Simulation();
	sim.scheduleCall(nextCall(sim, 0), 10);
	sim.run();
    }
}
