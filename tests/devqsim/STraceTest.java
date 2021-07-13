import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class STraceTest {
    static Simulation sim = new Simulation();

    static void printEventStackTrace() {
	for (StackTraceElement e: sim.getEventStackTrace()) {
	    System.out.println(e.toString());
	}
    }


    static public void main(String argv[]) throws Exception {
	sim.setStackTraceMode(true);

	sim.scheduleCall(new Callable() {
		public void call() {
		    System.out.println("tested sim.scheduleCall "
				       +"(current time = "
				       +sim.currentTicks() +")");
		}
	    },
	    10);
	sim.run();
	printEventStackTrace();
	System.out.println("-----------------");

	sim.scheduleTask(new Runnable() {
		public void run() {
		    System.out.println("hello");
		    printEventStackTrace();
		    TaskThread.pause(10);
		    System.out.println("goodbye");
		    printEventStackTrace();
		}
	    }, 10);
	sim.run();
	printEventStackTrace();
	System.out.println("------------------");
	final FifoTaskQueue cq = new FifoTaskQueue(sim, "cq", true);

	cq.add(new Callable() {
		public void call() {
		    System.out.println("cq call done, time = "
				       +sim.currentTicks());
		}
	    }, 10);
	sim.run();
	printEventStackTrace();
	System.out.println("-------------------");

	cq.add(new Runnable() {
		public void run() {
		    System.out.println("2nd cq task started, "
				       + "current time = "
				       + sim.currentTicks());
		    printEventStackTrace();
		    cq.addCurrentTask(10);
		    System.out.println("2nd cq task done, current time = "
				       +sim.currentTicks());
		    printEventStackTrace();
		    }
		}, 10);
	sim.run();
	printEventStackTrace();
	System.out.println("-------------------");
	cq.add(new Runnable() {
		public void run() {
		    System.out.println("3rd cq task started, current time = "
					   +sim.currentTicks());
		    printEventStackTrace();
		    cq.pauseCurrentTask(10);
		    System.out.println("3rd cq task afer pause, "
				       +"current time = "
				       +sim.currentTicks());
		    printEventStackTrace();
		}
	    }, 10);
	sim.run();
	printEventStackTrace();
	System.out.println("-------------------");
	cq.add(new Runnable() {
		public void run() {
		    System.out.println("3rd cq task started, current time = "
					   +sim.currentTicks());
		    printEventStackTrace();
		    cq.pauseCurrentTask(10);
		    System.out.println("3rd cq task afer 1st pause, "
				       +"current time = "
				       +sim.currentTicks());
		    cq.pauseCurrentTask(10);
		    System.out.println("3rd cq task afer 2nd pause, "
				       +"current time = "
				       +sim.currentTicks());
		    printEventStackTrace();
		    TaskThread.pause(10);
		    System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());
		    printEventStackTrace();
		}
	    }, 10);
	sim.run();
	printEventStackTrace();
	System.out.println("-------------------");

	final QueueServer qs1 = new QueueServer() {
		public long getInterval() {return 15;}
	    };
	final QueueServer qs2 = new QueueServer() {
		public long getInterval() {return 15;}
	    };
	final QueueCallable<QueueServer>
	    qc1 = new QueueCallable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qc1: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	    public void call() {System.out.println("sq call 1");}
	};

	QueueCallable<QueueServer>
	    qc2 = new QueueCallable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qc2: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		printEventStackTrace();
	    }
	    public void call() {System.out.println("sq call 2");}
	};
	QueueCallable<QueueServer>
	    qc3 = new QueueCallable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qc3: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	    public void call() {System.out.println("sq call 3");}
	};
	QueueCallable<QueueServer>
	    qc4 = new QueueCallable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qc4: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		printEventStackTrace();
	    }
	    public void call() {System.out.println("sq call 4");}
	};

	final FifoServerQueue<QueueServer> sq = new
	    FifoServerQueue<QueueServer>(sim, "fifoSQ", true,
					 qs1, qs2);

	sq.add(qc1, 5);
	sq.add(qc2, 5);
	sq.add(qc3, 5);
	sq.add(qc4, 5);

	sim.run();

	printEventStackTrace();

	System.out.println("-------------------");

	final QueueRunnable<QueueServer>
	    qr1 = new QueueRunnable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qr1: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	    public void run() {
		System.out.println("sq run 1, current time = "
				   +sim.currentTicks());
		printEventStackTrace();
	    }
	};

	QueueRunnable<QueueServer>
	    qr2 = new QueueRunnable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qr2: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	    public void run() {
		System.out.println("sq run 2, current time = "
				   +sim.currentTicks());
	    }
	};
	QueueRunnable<QueueServer>
	    qr3 = new QueueRunnable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qr3: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	    public void run() {
		System.out.println("sq run 3, current time = "
				   +sim.currentTicks());
	    }
	};
	QueueRunnable<QueueServer>
	    qr4 = new QueueRunnable<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("qr4: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		printEventStackTrace();
	    }
	    public void run() {
		System.out.println("sq run 4, current time = "
				   +sim.currentTicks());
	    }
	};
	sq.add(qr1, 5);
	sq.add(qr2, 5);
	sq.add(qr3, 5);
	sq.add(qr4, 5);

	sim.run();
	printEventStackTrace();

	System.out.println("-------------------");
	final QueueServerHandler<QueueServer> h1 =
		new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h1: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		printEventStackTrace();
	    }
	};

	final QueueServerHandler<QueueServer> h2 =
	    new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h2: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	};

	final QueueServerHandler<QueueServer> h3 =
	    new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h3: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		TaskThread.pause(10);
		System.out.println("h3: current time = "
				   +sim.currentTicks()
				   +", qs = " + qsname(qs));
	    }
	};

	final QueueServerHandler<QueueServer> h4 =
	    new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h4: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		TaskThread.pause(10);
		System.out.println("h4: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	};

	final QueueServerHandler<QueueServer> h5 =
	    new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h5: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
	    }
	};

	final QueueServerHandler<QueueServer> h6 =
	    new QueueServerHandler<QueueServer>() {
	    String qsname(QueueServer qs) {
		if (qs == qs1) return "qs1";
		if (qs == qs2) return "qs2";
		return "(unknown)";
	    }
	    public void interactWith (QueueServer qs) {
		System.out.println("h6: current time = "
				   +sim.currentTicks()
				   +", qs = " +qsname(qs));
		printEventStackTrace();
	    }
	};

	Runnable r1 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h1, 5);
		    System.out.println("r1 finished, ctime = "
				       +sim.currentTicks());
		    printEventStackTrace();
		}
	    };

	Runnable r2 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h2, 5);
		    System.out.println("r2 finished, ctime = "
				       +sim.currentTicks());
		}
	    };

	Runnable r3 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h3, 5);
		    System.out.println("r3 finished, ctime = "
				       +sim.currentTicks());
		}
	    };

	Runnable r4 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h4, 5);
		    System.out.println("r4 finished, ctime = "
				       +sim.currentTicks());
		}
	    };


	Runnable r5 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h5, 5);
		    System.out.println("r5 finished, ctime = "
				       +sim.currentTicks());
		}
	    };

	Runnable r6 = new Runnable() {
		public void run() {
		    sq.addCurrentTask(h6, 5);
		    System.out.println("r6 finished, ctime = "
				       +sim.currentTicks());
		    printEventStackTrace();
		}
	    };

	sim.scheduleTask(r1, 0);
	sim.scheduleTask(r2, 0);
	sim.scheduleTask(r3, 0);
	sim.scheduleTask(r4, 0);
	sim.scheduleTask(r5, 0);
	sim.scheduleTask(r6, 0);

	sim.run();
	printEventStackTrace();
	System.exit(0);
    }
}
