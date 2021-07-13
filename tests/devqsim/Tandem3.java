import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class Tandem3 {
    
    static class Customer {
	long startTime = 0;
	int index = 0;


	FifoTaskQueue[] table;


	Customer(FifoTaskQueue table[]) {
	    this.table = table;
	}

	void start() {
	    if (table == null || table.length == 0) return;
	    startTime = sim.currentTicks();
	    nextQueueCall().call();

	}
	Callable nextQueueCall() {
	    final FifoTaskQueue queue = table[index++];
	    if (index < table.length) {
		return new Callable() {
			public void call() {
			    queue.add(nextQueueCall(), poissonValue(0.1));
			}
		    };

	    } else {
		return new Callable() {
			public void call() {
			    queue.add(end(), poissonValue(0.1));
			}
		    };
	    }
	}
	Callable end() {
	    return new Callable() {
		    public void call() {
			long endTime = sim.currentTicks();
			long ind = (endTime - startTime)/10;
			if (0 <= ind && ind < 10) 
			    count[(int)ind]++;
		    }			
		};
	}
    }

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

    public static void main(String argv[]) throws Exception {

	int tableSize = 2;
	int mi = 10000;

	if (argv.length > 0) {
	    tableSize = Integer.parseInt(argv[0]);
	}
	if (argv.length > 1) {
	    mi = Integer.parseInt(argv[1]);
	}

	final int maxIterations = mi;

	final FifoTaskQueue table[] = new FifoTaskQueue[tableSize];
	for (int j = 0; j < tableSize; j++) {
	    table[j] = new FifoTaskQueue(sim, null, false);
	}

	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < maxIterations; i++) {
			TaskThread.pause(poissonValue(0.01));
			new Customer(table).start();

		    }
		}
	    });

	sim.run();

	for (int i = 0; i < 10; i++) {
	    System.out.println("count[" +(i*10)
			       +".." +((i+1)*10 - 1)
			       +"] = " +count[i]);
	}
	System.exit(0);
    }
}
