import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class SQTest {
    static Simulation sim = new Simulation();


    static QueueServer server = new QueueServer() {
	    public  long getInterval() {return 10;}
	};

    static FifoServerQueue<QueueServer> sq =
	new FifoServerQueue<>(sim, "sq", true, server);

    public static void main(String argv[]) {
	try {
	    sq.add(new QueueCallable<QueueServer>() {
		    public void interactWith(QueueServer svr) {
			System.out.println("queue callable interacts at " 
					   + sim.currentTicks());
		    }
		    public void call() {
			System.out.println("queue callable finishes at "
					   + sim.currentTicks());
		    }
		}, 0);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("runnable-1 starts at "
					   + sim.currentTicks());
			boolean added = sq.addCurrentTask
			    (
			     new QueueServerHandler<QueueServer>() {
				 public void interactWith(QueueServer svr) {
				     System.out.println("runnable-1 "
							+ "interacts with "
							+ "server at "
							+ sim.currentTicks());
				 }
			     },
			     0,
			     new SimEventCallable() {
				 public void call(final SimulationEvent sev) {
					sim.scheduleCall(new Callable() {
						public void call() {
						    boolean canceled =
							sev.cancel();
						    System.out.println
							("for runnable-1 "
							 + " canceled = "
							 + canceled);
						}
					    }, 15);
				     
				 }
			     });
			System.out.println("runnable-1 ends at "
					   + sim.currentTicks()
					   + ", added = " + added);
		    }
		}, 0);

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("runnable-2 starts at "
					   + sim.currentTicks());
			boolean added = sq.addCurrentTask
			    (
			     new QueueServerHandler<QueueServer>() {
				 public void interactWith(QueueServer svr) {
				     System.out.println("runnable-2 "
							+ "interacts with "
							+ "server at "
							+ sim.currentTicks());
				 }
			     },
			     0,
			     new SimEventCallable() {
				 public void call(final SimulationEvent sev) {
					sim.scheduleCall(new Callable() {
						public void call() {
						    boolean canceled =
							sev.cancel();
						    System.out.println
							("for runnable-2 "
							 + " canceled = "
							 + canceled);
						}
					    }, 5);
				     
				 }
			     });
			System.out.println("runnable-2 ends at "
					   + sim.currentTicks()
					   + ", added = " + added);
		    }
		}, 0);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("runnable-3 starts at "
					   + sim.currentTicks());
			sq.addCurrentTask
			    (
			     new QueueServerHandler<QueueServer>() {
				 public void interactWith(QueueServer svr) {
				     System.out.println("runnable-3 "
							+ "interacts with "
							+ "server at "
							+ sim.currentTicks());
				 }
			     }, 0);
			System.out.println("runnable-3 ends at "
					   + sim.currentTicks());
		    }
		}, 0);
	    sim.run();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
