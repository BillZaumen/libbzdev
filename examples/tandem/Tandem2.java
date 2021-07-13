import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.PoissonIATimeRV;
import org.bzdev.math.StaticRandom;

public class Tandem2 {
    
    static java.util.Random random = new java.util.Random(1758935837430L);
    static long poissonValue(double rate) {
	long value = -1;
	while (value < 0) {
	    value = (long) (-(java.lang.Math.
			     log(random.nextDouble())/rate));
	}
	return value;
    }

    static Simulation sim = new Simulation();
    static FifoTaskQueue cq1 = new FifoTaskQueue(sim, "cq1", false);
    static FifoTaskQueue cq2 = new FifoTaskQueue(sim, "cq2", false);

    static int count[] = new int[10];

    public static void main(String argv[]) {

	StaticRandom.setSeed(1758935837430L);
	StaticRandom.maximizeQuality();
	
	final InterarrivalTimeRV rv1 = new PoissonIATimeRV(10.0);
	final InterarrivalTimeRV rv2 = new PoissonIATimeRV(100.0);


	final Callable task = new Callable() {
		public void call() {
		    final long start = sim.currentTicks();
		    cq1.add(new Callable() {
			    public void call() {
				cq2.add(new Callable() {
					public void call() {
					    long end = sim.currentTicks();
					    long ind = (end - start)/10;
					    if (0 <= ind && ind < 10) 
						count[(int)ind]++;
					}
				    }, rv1.next());
			    }
			}, rv1.next());
		}
	    };

	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(rv2.next());
			sim.scheduleCall(task, 0);
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
