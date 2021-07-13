import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class Tandem1 {
    
    static java.util.Random random = new java.util.Random(1758935837430L);
    static long poissonValue(double rate) {
	long value = -1;
	while (value < 0) {
	    value = (long) (-(java.lang.Math.
			     log(random.nextDouble())/rate));
	}
	return value;
    }

    static Simulation  sim = new Simulation ();
    static FifoTaskQueue cq1 = new FifoTaskQueue(sim, "cq1", false);
    static FifoTaskQueue cq2 = new FifoTaskQueue(sim, "cq2", false);

    static int count[] = new int[10];

    public static void main(String argv[]) throws Exception {

	final Runnable program = () -> {
	    long start = sim.currentTicks();
	    cq1.addCurrentTask(poissonValue(0.1));
	    cq2.addCurrentTask(poissonValue(0.1));
	    long end = sim.currentTicks();
	    long ind = (end - start)/10;
	    if (0 <= ind && ind < 10) count[(int)ind]++;
	};

	sim.scheduleTask(()-> {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(poissonValue(0.01));
			sim.scheduleTask(program, 0);
		    }
	    });

	/*
	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(poissonValue(0.01));
			sim.scheduleTask(program, 0);
		    }
		}
	    });
	*/
	sim.run();

	for (int i = 0; i < 10; i++) {
	    System.out.println("count[" +(i*10)
			       +".." +((i+1)*10 - 1)
			       +"] = " +count[i]);
	}
	System.exit(0);
    }
}
