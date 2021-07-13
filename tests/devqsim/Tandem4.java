import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class Tandem4 {
    
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
		return () -> {queue.add(nextQueueCall(), poissonValue(0.1));};
	    } else {
		return () ->{queue.add(end(), poissonValue(0.1));};
	    }
	}
	Callable end() {
	    return () -> {
		long endTime = sim.currentTicks();
		long ind = (endTime - startTime)/10;
		if (0 <= ind && ind < 10) {
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


    static class Generator {
	Simulation sim;
	double rate;
	int maxIterations;
	int index = 0;
	FifoTaskQueue[] table;
	Generator(Simulation sim, double rate, int maxIterations,
		  FifoTaskQueue[] table) {
	    this.sim = sim;
	    this.rate = rate;
	    this.maxIterations = maxIterations;
	    this.table = table;
	}
	
	void start() {
	    index = 0;
	    sim.scheduleCall(nextCallable(), poissonValue(rate));
	}
	Callable nextCallable() {
	    if (index++ < maxIterations) {
		return () -> {
		    new Customer(table).start();
		    sim.scheduleCall(nextCallable(),
				     poissonValue(rate));
		};

	    } else {
		return () -> {return;};
	    }
	}
    }

    static Simulation sim = new Simulation();
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

	Generator generator = new Generator(sim, 0.01, maxIterations,
					    table);
	generator.start();

	sim.run();

	for (int i = 0; i < 10; i++) {
	    System.out.println("count[" +(i*10)
			       +".." +((i+1)*10 - 1)
			       +"] = " +count[i]);
	}
	System.exit(0);
    }
}
