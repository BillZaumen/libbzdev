import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class TQTest {
    static Simulation sim = new Simulation();
    static FifoTaskQueue tq = new FifoTaskQueue(sim, "tq", true);

    public static void main(String argv[]) {
	try {
	    tq.add(new Callable() {
		    public void call() {
			System.out.println("callable-1 executed at " 
					   + sim.currentTicks());
		    }
		}, 10);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task-1 started at "
					   + sim.currentTicks());
			boolean added =
			    tq.addCurrentTask(10, new SimEventCallable() {
				    public void call(final SimulationEvent sev){
					sim.scheduleCall(new Callable() {
						public void call() {
						    boolean canceled =
							sev.cancel();
						    System.out.println
							("for task-1"
							 + ", canceled = "
							 + canceled);
						}
					    }, 15);
				    }
				});
			System.out.println("task-1 ended at "
					   + sim.currentTicks()
					   + ", added = " + added);
		    }
		});
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task-2 started at "
					   + sim.currentTicks());
			boolean added =
			    tq.addCurrentTask(10, new SimEventCallable() {
				    public void call(final SimulationEvent sev){
					sim.scheduleCall(new Callable() {
						public void call() {
						    boolean canceled =
							sev.cancel();
						    System.out.println
							("for task-2"
							 + ", canceled = "
							 + canceled);
						}
					    }, 5);
				    }
				});
			System.out.println("task-2 ended at "
					   + sim.currentTicks()
					   + ", added = " + added);
		    }
		});
	    sim.scheduleCall(new Callable() {
		    public void call() {
			final SimulationEvent sev = tq.add(new Callable() {
				public void call() {
				    System.out.println
					("callable-2 executed at " 
					 + sim.currentTicks());
				}
			    }, 10);
			sim.scheduleCall(new Callable() {
				public void call() {
				    boolean canceled = sev.cancel();
				    System.out.println("callable-2 timeout at "
						       +sim.currentTicks()
						       + ", canceled = "
						       + canceled);
				}
			    }, 5);
		    }
		}, 10);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			final SimulationEvent sev = tq.add(new Callable() {
				public void call() {
				    System.out.println
					("callable-3 executed at "
					 + sim.currentTicks());
				}
			    }, 10);
			sim.scheduleCall(new Callable() {
				public void call() {
				    boolean canceled = sev.cancel();
				    System.out.println("callable-3 timeout at "
						       +sim.currentTicks()
						       + ", canceled = "
						       + canceled);
				}
			    }, 90);
		    }
		}, 10);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			tq.add(new Callable() {
				public void call() {
				    System.out.println
					("callable-4 executed at "
					 + sim.currentTicks());
				}
			    }, 10);
		    }
		}, 10);
	    sim.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
