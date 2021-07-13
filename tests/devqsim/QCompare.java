import org.bzdev.devqsim.*;

public class QCompare {
    static java.util.Random random = new java.util.Random(1758935837430L);
    static long poissonValue(double rate) {
	long value = -1;
	while (value < 0) {
	    value = (long) (-(java.lang.Math.
			     log(random.nextDouble())/rate));
	}
	return value;
    }

    static class QS  implements QueueServer {
	public long getInterval() {return 0;}
    }


    static QS qs1 = new QS();
    static QS qs2 = new QS();


    static Simulation sim = new Simulation();
    static FifoTaskQueue cq1 = new FifoTaskQueue(sim, "cq1", false);
    static FifoTaskQueue cq2 = new FifoTaskQueue(sim, "cq2", false);

    static FifoServerQueue<QS> fsq;

    static int countCq[] = new int[10];
    static int countFsq[] = new int[10];
    static int choiceCount = 0;
    static int fsqChoiceCount = 0;

    static boolean smartCustomers = false;
    static boolean slowMode = false;

    // static double tau = 0.01;
    static double tau = 0.01;
    static int scale = 10;

    public static void main(String argv[]) throws Exception {
	int ind = 0;
	while (ind < argv.length) {
	    if (argv[ind].equals("-smart")) {
		smartCustomers = true;
	    } else if (argv[ind].equals("-slow")) {
		slowMode = true;
		tau = 0.1;
		scale = 4;
	    }
	    ind++;
	}
	fsq = new FifoServerQueue<QS>(sim, "fsq", false, qs1, qs2);

	final Runnable program = new Runnable() {
		public void run() {
		    final long ptime =  poissonValue(0.2);
		    Runnable run1 = new Runnable() {
			    public void run() {
				long start = sim.currentTicks();
				boolean choseOne;
				if (smartCustomers) {
				    if (cq1.size() != cq2.size()) {
					if (cq1.size() > cq2.size()) {
					    choseOne = true;
					} else {
					    choseOne = false;
					}
				    } else {
					if (cq1.isBusy() && !cq2.isBusy()) {
					    choseOne = false;
					} else if (cq2.isBusy() &&
						   !cq1.isBusy()) {
					    choseOne = true;
					} else {
					    choiceCount++;
					    choseOne = ((choiceCount % 2) == 1);
					}
				    }
				} else {
				    choiceCount++;
				    choseOne = ((choiceCount % 2) == 1);

				}
				if (choseOne) {
				    long pt = ptime;
				    cq1.addCurrentTask(pt);
				} else {
				    cq2.addCurrentTask(ptime);
				}
				long end = sim.currentTicks();
				long ind = (end - start)/scale;
				if (0 <= ind && ind < 10) {
				    countCq[(int)ind]++;
				}
			    }
			};
		    Runnable run2 = new Runnable() {
			    public void run() {
				long start = sim.currentTicks();
				fsq.addCurrentTask(null, ptime);
				long end = sim.currentTicks();
				long ind = (end - start)/scale;
				if (0 <= ind && ind < 10) countFsq[(int)ind]++;
			    }
			};
		    sim.scheduleTask(run1, 0);
		    sim.scheduleTask(run2, 0);
		}
	    };

	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(poissonValue(tau));
			sim.scheduleTask(program, 0);
		    }
		}
	    });

	sim.run();

	for (int i = 0; i < 10; i++) {
	    System.out.println("countCq[" +(i*scale)
			       +".." +((i+1)*(scale) - 1)
			       +"] = " +countCq[i]
			       +", countFsq[" +(i*scale) + ".."
			       +(((i+1)*scale)-1)
			       +"] = " +countFsq[i]);
	}
	System.exit(0);
    }
}
