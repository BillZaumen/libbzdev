import org.bzdev.devqsim.Simulation;
import org.bzdev.devqsim.TaskThread;

public class TestSim1 {
    public static void main(String argv[]) throws Exception {

	Simulation sim = new Simulation();
	sim.scheduleTask(() -> {
		for(int i = 0; i < 5; i++) {
		    System.out.println("task is at simulation time "
				       + sim.currentTicks()
				       + " [i= " + i +"]");
		    TaskThread.pause(10);
		}
	    }, 10);
	sim.run();
    }
}
