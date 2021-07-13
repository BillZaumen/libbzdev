import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.PoissonIATimeRV;
import org.bzdev.math.StaticRandom;

public class Tandem1 {
    
    static Simulation  sim = new Simulation ();
    static FifoTaskQueue cq1 = new FifoTaskQueue(sim, "cq1", false);
    static FifoTaskQueue cq2 = new FifoTaskQueue(sim, "cq2", false);

    static int count[] = new int[10];

    public static void main(String argv[]) {

	StaticRandom.setSeed(1758935837430L);
	StaticRandom.maximizeQuality();
	
	final InterarrivalTimeRV rv1 = new PoissonIATimeRV(10.0);
	final InterarrivalTimeRV rv2 = new PoissonIATimeRV(100.0);
	

	final Runnable program = new Runnable() {
		public void run() {
		    long start = sim.currentTicks();
		    cq1.addCurrentTask(rv1.next());
		    cq2.addCurrentTask(rv1.next());
		    long end = sim.currentTicks();

		    long ind = (end - start)/10;
		    if (0 <= ind && ind < 10) count[(int)ind]++;
		}
	    };

	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(rv2.next());
			sim.scheduleTask(program, 0);
		    }
		}
	    });

	sim.run();

	for (int i = 0; i < 10; i++) {
	    System.out.println("count[" +(i*10)
			       +".." +((i+1)*10 - 1)
			       +"] = " +count[i]);
	}

    }
}
