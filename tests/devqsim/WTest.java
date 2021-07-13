import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class WTest {
    static Simulation sim = new Simulation();
    static WaitTaskQueue wq = new WaitTaskQueue(sim, "wq", true);

    static class OurCallable implements Callable {
	int i;
	OurCallable(int i) {
	    this.i = i;
	}
	public void call() {
	    System.out.println("called OurCallable " + i 
			       + " at " + sim.currentTicks());
	}
    }

    static public void main(String argv[]) {
	try {
	    System.out.println("wq.canRelease() = " + wq.canRelease());
	    for (int i = 0; i < 10; i++) {
		wq.add(new OurCallable(i));
	    }

	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("late event at time "
					   +sim.currentTicks());
		    }
		}, 4L, 1.0);

	    for (int i = 1; i < 5; i++) {
		if (i == 3) {
		    sim.scheduleCall(new Callable() {
			    public void call() {
				wq.release(2);
			    }
			}, (long)i);
		} else {
		    sim.scheduleCall(new Callable() {
			    public void call() {
				wq.release(1);
			    }
			}, (long)i);
		}
	    }

	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("early event at time "
					   +sim.currentTicks());
		    }
		}, 4L, -1.0);

	    sim.scheduleCall(new Callable() {
		    public void call() {
			wq.freeze(false);
			wq.freeze(true);
		    }
		}, 7L);


	    sim.scheduleCall(new Callable() {
		    public void call() {
			wq.freeze(false);
		    }
		}, 10L);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			wq.freeze(true);
		    }
		}, 10L, 1.0);
	    sim.run();
	    System.out.println("----");
	    for (int i = 0; i < 3; i++) {
		wq.add(new OurCallable(i));
	    }
	    wq.releaseUpTo(3);
	    sim.run();
	    System.out.println("----");
	    for (int i = 3; i < 6; i++) {
		wq.add(new OurCallable(i));
	    }
	    wq.releaseUpTo(10);
	    sim.run();
	    for (int i = 6; i < 10; i++) {
		wq.add(new OurCallable(i));
	    }
	    sim.run();
	    System.out.println("----");
	    wq.releaseUpTo(10);
	    sim.run();
	    System.out.println("--------------------");

	    sim.scheduleCall(new Callable() {
		    public void call() {
			for(int i = 0; i < 3; i++) {
			    wq.add(new OurCallable(i));
			}
		    }
		}, 1);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			wq.releaseUpTo(5);
		    }
		}, 2);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			for(int i = 3; i < 10; i++) {
			    wq.add(new OurCallable(i));
			}
		    }
		}, 2);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			wq.release(5);
		    }
		}, 3);
	    sim.run();
	    System.out.println("----");

	    System.out.println("Try CANCELS_AS_RELEASED");
	    wq.setReleasePolicy(TaskQueue.ReleasePolicy.CANCELS_AS_RELEASED);
	    for (int i = 0; i < 5; i++) {
		wq.add(new OurCallable(i));
	    }
	    wq.add(new OurCallable(5)).cancel();
	    for (int i = 6; i < 10; i++) {
		wq.add(new OurCallable(i));
	    }
	    wq.release(7);
	    sim.run();
	    System.out.println("----");
	    wq.freeze(false);
	    sim.run();
	    wq.freeze(true);
	    System.out.println("----");
	    System.out.println("Try REPLACE_CANCELS");
	    wq.setReleasePolicy(TaskQueue.ReleasePolicy.REPLACE_CANCELS);
	    SimulationEvent[] evs = new SimulationEvent[10];
	    for (int i = 0; i < 10; i++) {
		evs[i] = wq.add(new OurCallable(i));
	    }
	    wq.releaseUpTo(10);
	    for (int i = 5; i < 10; i++) {
		evs[i].cancel();
		wq.add(new OurCallable(5+i));
	    }
	    wq.add(new OurCallable(15));
	    sim.run();
	    System.out.println("----");
	    wq.freeze(false);
	    sim.run();
	    wq.freeze(true);
	    System.out.println("----------------------------");
	    System.out.println("corner case test:");
	    for (int i = 0; i < 3; i++) {
		wq.add(new OurCallable(i));
	    }
	    wq.release(5);
	    sim.run();
	    for (int i = 3; i < 5; i++) {
		wq.add(new OurCallable(i));
	    }
	    sim.run();
	    System.out.println("-----");
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task started at "
					   + sim.currentTicks());
			boolean added =
			    wq.addCurrentTask(new SimEventCallable() {
				    public void call(final SimulationEvent sev){
				    sim.scheduleCall(new Callable() {
					    public void call() {
						sev.cancel();
					    }
					}, 10);
				}
			    });
			System.out.println("task ended at "
					   + sim.currentTicks()
					   +", added = " + added);
		    }
		});
	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("wq.size() = "
					   + wq.size() + " at "
					   + sim.currentTicks());
			wq.releaseUpTo(1);
		    }
		}, 20);
	    sim.run();

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("second task started at "
					   + sim.currentTicks());
			boolean added = wq.addCurrentTask(10);
			System.out.println("second task ended at "
					   + sim.currentTicks()
					   +", added = " + added);
		    }
		});
	    System.out.println("----");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
