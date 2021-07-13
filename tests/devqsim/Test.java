import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class Test {
    static Simulation sim = new Simulation();
    static SimulationEvent savedEvent = null;

    static public void main(String argv[]) {
	try {
	    sim.addSimulationListener(new DefaultSimAdapter() {
		    public void simulationStart(Simulation sim) {
			System.out.println("----- simulation starts -----");
		    }
		    public void simulationStop(Simulation sim) {
			System.out.println("----- simulation stops -----");
		    }
		});
	    System.out.println("current time = " +sim.currentTicks());
	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("tested sim.scheduleCall "
					   +"(current time = "
					   +sim.currentTicks() +")");
		    }
		},
		10);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task is at simulation time "
					   +sim.currentTicks());
		    }
		},
		10);
	    sim.scheduleTask(new Runnable() {
		    public void run() {
			for (int i = 0; i < 5; i++) {
			    System.out.println("task is at simulation time "
					       +sim.currentTicks()
					       +" [i=" +i +"]");
			    TaskThread.pause(10);
			}
		    }
		},
		10);

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task is at simulation time "
					   +sim.currentTicks());
		    }
		},
		70);
	    sim.run();
	    System.out.println("run 1 end: current time = "
			       + sim.currentTicks());

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			sim.startImmediateTask(new Runnable() {
				public void run() {
				    for (int i = 0; i < 5; i++) {
					System.out.println
					    ("task is at simulation time "
					     +sim.currentTicks()
					     +" [i=" +i +"]");
					TaskThread.pause(10);
				    }
				}
			    });
			System.out.println("nested task started");
		    }
		});
	    sim.run();

	    System.out.println("run 2 end: current time = "
			       + sim.currentTicks());

	    TaskThread task1 = sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task is at simulation time "
					   + sim.currentTicks());
		    }
		},
		10);
	    task1.cancel();
	    sim.run();
	    System.out.println("run 3 end: current time = " 
			       +  sim.currentTicks());

	    final TaskThread task2 = sim.scheduleTask(new Runnable() {
		    public void run() {
			for (int i = 0; i < 5; i++) {
			    System.out.println("task is at simulation time "
					       +sim.currentTicks()
					       +" [i=" +i +"]");
			    TaskThread.pause(10);
			}
		    }
		},
		10);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			task2.cancel();
		    }
		}, 
		35);
	    
	    sim.run();
	    System.out.println("run 4 end: current time = "
			       + sim.currentTicks());

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			for (int i = 0; i < 5; i++) {
			    System.out.println("task is at simulation time "
					       +sim.currentTicks()
					       +" [i=" +i +"]");
			    if (i == 2) {
				TaskThread.currentThread().cancel();
			    }
			    TaskThread.pause(10);
			}
		    }
		},
		10);
	    sim.run();
	    System.out.println("run 5 end: current time = "
			       + sim.currentTicks());
	    final FifoTaskQueue cq = new FifoTaskQueue(sim, "cq", true);

	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 10);
        
	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 20);

	    sim.run();
	    System.out.println("cq.size() = " +cq.size());
	    System.out.println("run 6 end: current time = "
			       + sim.currentTicks());

	    cq.add(new Runnable() {
		    public void run() {
			System.out.println("cq task run");
		    }
		}, 10);

	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 10);

	    cq.add(new Runnable() {
		    public void run() {
			System.out.println("2nd cq task started, "
					   + "current time = "
					   + sim.currentTicks());
			cq.addCurrentTask(10);
			System.out.println("2nd cq task done, current time = "
					   +sim.currentTicks());
		    }
		}, 10);
	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 10);

	    sim.run();
	    System.out.println("cq.size() = " +cq.size());
	    System.out.println("run 7 end: current time = "
			       + sim.currentTicks());

	    cq.add(new Runnable() {
		    public void run() {
			System.out.println("2nd cq task started, "
					   + "current time = "
					   + sim.currentTicks()
					   +", qsize = " +cq.size());
			TaskThread.pause(10);
			System.out.println("2nd cq task after 1st pause, "
					   +"current time = "
					   +sim.currentTicks()
					   +", qsize = " +cq.size());
			TaskThread.pause(10);
			System.out.println("2nd cq task after 2nd pause, "
					   +"current time = "
					   +sim.currentTicks()
					   +", qsize = " +cq.size());
			cq.addCurrentTask(10);
			System.out.println("2nd cq task done, current time = "
					   +sim.currentTicks()
					   +", qsize = " +cq.size());
		    }
		}, 10);
	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks()
					   +", qsize = " +cq.size());
		    }
		}, 10);

	    sim.run();
	    System.out.println("cq.size() = " +cq.size());
	    System.out.println("run 8 end: current time = "
			       + sim.currentTicks());

	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 40);

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			TaskThread.pause(10);
			System.out.println("task paused for 10 units, "
					   +"current time = "
					   +sim.currentTicks());
			cq.addCurrentTask(10);
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			TaskThread.pause(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());
		    }
		}, 10);

	    sim.run();
	    System.out.println("cq.size() = " +cq.size());
	    System.out.println("run 9 end: current time = "
			       + sim.currentTicks());

	    final LifoTaskQueue cqt = new LifoTaskQueue(sim, "cqt", true);

	    cq.add(new Runnable() {
		    public void run() {
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			cqt.addCurrentTask(10);
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			cqt.addCurrentTask(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());

		    }
		}, 10);

	    cqt.add(new Callable() {
		    public void call() {
			System.out.println("cqt call done, time = "
					   +sim.currentTicks());
		    }
		}, 40);

	    sim.run();
	    System.out.println("cq.size() = " +cq.size());
	    System.out.println("cqt.size() = " +cqt.size());
	    System.out.println("run 10 end: current time = "
			       + sim.currentTicks());

	    cqt.add(new Runnable() {
		    public void run() {
			System.out.println("currentThread = "
					   +Thread.currentThread().getName());
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10);
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());

		    }
		}, 10);

	    cqt.add(new Callable() {
		    public void call() {
			System.out.println("cqt call done, time = "
					   +sim.currentTicks());
		    }
		}, 40);
	    System.out.println("cqt.size() = " +cqt.size());

	    sim.run();
	    System.out.println("cqt.size() = " +cqt.size());
	    System.out.println("run 11 end: current time = "
			       + sim.currentTicks());

	    cqt.add(new Runnable() {
		    public void run() {
			System.out.println("currentThread = "
					   +Thread.currentThread().getName());
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10, new SimEventCallable() {
				public void call(SimulationEvent event) {
				    savedEvent = event;
				}
			    });
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());

		    }
		}, 10);

	    cqt.add(new Callable() {
		    public void call() {
			System.out.println("cqt call done, time = "
					   +sim.currentTicks());
		    }
		}, 40);

	    sim.run(15);
	    savedEvent.cancel();
	    sim.run();
	    System.out.println("cqt.size() = " +cqt.size());
	    System.out.println("run 12 end: current time = "
			       + sim.currentTicks());

	    cqt.freeze(true);
	    cqt.add(new Runnable() {
		    public void run() {
			System.out.println("currentThread = "
					   +Thread.currentThread().getName());
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10);
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			cqt.pauseCurrentTask(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());

		    }
		}, 10);

	    cqt.add(new Callable() {
		    public void call() {
			System.out.println("cqt call done, time = "
					   +sim.currentTicks());
		    }
		}, 40);

	    cqt.freeze(false);

	    sim.run();
	    System.out.println("cqt.size() = " +cqt.size());
	    System.out.println("run 13 end: current time = "
			       + sim.currentTicks());

	    final PriorityTaskQueue ptq = new PriorityTaskQueue(sim, "ptq", true);
	    Callable ptqCallable = new Callable() {
		    public void call() {
			System.out.println("ptqCallable: current time = " +
					   sim.currentTicks());
		    }
		};
	    Runnable ptqRunnable = new Runnable() {
		    public void run() {
			System.out.println("ptqRunnable: currnett time = " +
					   sim.currentTicks());
			ptq.pauseCurrentTask(10);
			System.out.println("ptqRunnable: currnett time = " +
					   sim.currentTicks());
                    
			ptq.addCurrentTask(0, 10);
			System.out.println("ptqRunnable: currnett time = " +
					   sim.currentTicks());
		    }
		};

	    ptq.add(ptqRunnable, 1, 10);
	    ptq.add(ptqCallable, 1, 10);
        
	    sim.run();
	    System.out.println("run 14 end: current time = "
			       + sim.currentTicks());

	    final ProcessClock clock = new ProcessClock(sim, "clock", true);

	    Runnable crun1 = new Runnable() {
		    public void run() {
			System.out.println("crun1: start time = " 
					   +sim.currentTicks());
			clock.advance(1, 30);
			System.out.println("crun1: end time = " 
					   +sim.currentTicks());
		    }
		};
	    Runnable crun2 = new Runnable() {
		    public void run() {
			System.out.println("crun2: start time = " 
					   +sim.currentTicks());
			clock.advance(0, 30);
			System.out.println("crun2: end time = " 
					   +sim.currentTicks());
		    }
		};

	    clock.advance(crun1, 1, 10);
	    sim.scheduleTask(crun2, 20);

	    sim.run();
	    System.out.println("run 15 end: current time = "
			       + sim.currentTicks());

	    SimulationMonitor<Simulation> monitor =
		new SimulationMonitor<Simulation>(sim) {
		int count = 0;
		long ct  = 0;
		long interval = 200;
		public boolean simulationPauses() {
		    System.out.println("simulationPauses() called at time = "
				       +getSimulation().currentTicks());
		    if (count == 0) {
			ct = getSimulation().currentTicks();
		    }
		    count++;
		    long newInterval = getSimulation().getNextEventInterval();
		    if (newInterval > interval) {
			interval -= getSimulation().advance(interval);
		    } else {
			interval -= newInterval;
		    }
                    
		    if (interval == 0) {
			return true;
		    }
		    return false;
		}
	    };
	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("callable called at time = "
					   + sim.currentTicks());
		    }
		}, 100);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("callable called at time = "
					   + sim.currentTicks());
		    }
		}, 300);

	    sim.run(monitor);
	    System.out.println("during run 17, current time = "
			       +sim.currentTicks());
	    sim.run();
	    System.out.println("run 17 end: current time = "
			       + sim.currentTicks());

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
	    System.out.println("run 18 end: current time = "
			       + sim.currentTicks());

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
	    System.out.println("run 19 end: current time = "
			       + sim.currentTicks());

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
		}
	    };

	    Runnable r1 = new Runnable() {
		    public void run() {
			sq.addCurrentTask(h1, 5);
			System.out.println("r1 finished, ctime = "
					   +sim.currentTicks());
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
		    }
		};

	    sim.scheduleTask(r1, 0);
	    sim.scheduleTask(r2, 0);
	    sim.scheduleTask(r3, 0);
	    sim.scheduleTask(r4, 0);
	    sim.scheduleTask(r5, 0);
	    sim.scheduleTask(r6, 0);

	    sim.run();
	    System.out.println("run 20 end: current time = "
			       + sim.currentTicks());

	    DefaultSimAdapter listener = new DefaultSimAdapter() {
		    public void simulationStart(Simulation sim) {
			System.out.println("Simulation started");
		    }
		    public void simulationStop(Simulation sim) {
			System.out.println("Simulation stopped");
		    }

		    public void callStart(Simulation sim, String tag){
			System.out.println("call start: " +tag);
		    }
		    public void callEnd(Simulation sim, String tag){
			System.out.println("call end: " +tag);

		    }
		    public void callStartSimObject(Simulation sim, 
						   SimObject obj, String tag)
		    {
			System.out.println(obj.getName() +" call start: " +tag);
		    }
		    public void callEndSimObject(Simulation sim, 
						 SimObject obj, String tag){
			System.out.println(obj.getName() +" call end: " +tag);
		    }
		    public void taskStart(Simulation sim, String tag){
			System.out.println("task started: " +tag);
		    }
		    public void taskPause(Simulation sim, String tag) {
			System.out.println("task paused " +tag);
		    }
		    public void taskResume(Simulation sim,String tag){
			System.out.println("task resumed: " +tag);
		    }
		    public void taskEnd(Simulation sim, String tag) {
			System.out.println("task ended: " +tag);
		    }

		    public void taskStartSimObject(Simulation sim, 
						   SimObject owner,
						   String tag)
		    {
			System.out.println(owner.getName() +" task started: " +tag);
		    }
		    public void taskPauseSimObject(Simulation sim, SimObject owner, 
						   String tag)
		    {
			System.out.println(owner.getName() +" task paused: " +tag);
		    }
		    public void taskResumeSimObject(Simulation sim,
						    SimObject owner, 
						    String tag)
		    {
			System.out.println(owner.getName() +" task resumed: " +tag);
		    }
		    public void taskEndSimObject(Simulation sim, SimObject owner, 
						 String tag)
		    {
			System.out.println(owner.getName() +" task ended: " +tag);
		    }
		    public void taskQueueStart(Simulation sim, TaskQueue q)
		    {
			System.out.println(q.getName() +": task queue start");
		    }
		    public void taskQueuePause(Simulation sim, TaskQueue q)
		    {
			System.out.println(q.getName() +": task queue pause");
		    }
		    public void taskQueueResume(Simulation sim, TaskQueue q)
		    {
			System.out.println(q.getName() +": task queue resume");
		    }
		    public void taskQueueStop(Simulation sim, TaskQueue q)
		    {
			System.out.println(q.getName() +": task queue stop");
		    }

		    public  void serverSelected(Simulation sim, ServerQueue q) {
			System.out.println(q.getName() +": server selected");
		    }

		    public void serverInteraction(Simulation sim, ServerQueue q,
						  Object server, String tag) 
		    {
			System.out.println(q.getName() +": server handler");
		    }
		    public void serverCallable(Simulation sim, ServerQueue q,
					       Object server,
					       String tag)
		    {
			System.out.println(q.getName() +": server callable");
		    }
		    public void serverRunnable(Simulation sim, ServerQueue q, 
					       Object server,
					       String tag)
		    {
			System.out.println(q.getName() +": server runnable");
		    }
		    public void serverTask(Simulation sim, ServerQueue q,
					   Object server,
					   String tag)
		    {
			System.out.println(q.getName() +": server task "
					   +"(" +tag +")");
		    }

		    public void serverInteractionSimObject(Simulation sim,
							   ServerQueue q,
							   Object server,
							   SimObject target, 
							   String tag)
		    {
			System.out.println(q.getName() +": server handler for "
					   +target.getName() +"(" +tag +")");
		    }
		    public void serverCallableSimObject(Simulation sim, 
							ServerQueue q, 
							Object server,
							SimObject target, 
							String tag)
		    {
			System.out.println(q.getName() +": server callable for "
					   +target.getName() +"(" +tag +")");
		    }
		    public void serverRunnableSimObject(Simulation sim, 
							ServerQueue q,
							Object server,
							SimObject target, 
							String tag)
		    {
			System.out.println(q.getName() +": server runnable for "
					   +target.getName() +"(" +tag +")");
		    }
		    public void serverTaskSimObject(Simulation sim, ServerQueue q, 
						    Object server,
						    SimObject target, String tag)
		    {
			System.out.println(q.getName() +": server task for "
					   +target.getName() +"(" +tag +")");
		    }
		};
	    sim.addSimulationListener(listener);

	    System.out.println("Runs with Simulation Listener");
	    sim.run();
	    System.out.println("run 21 end: current time = "
			       + sim.currentTicks());

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			for (int i = 0; i < 5; i++) {
			    System.out.println("task is at simulation time "
					       +sim.currentTicks()
					       +" [i=" +i +"]");
			    TaskThread.pause(10);
			}
			System.out.println("task is at simulation time "
					   +sim.currentTicks());
		    }
		},
		10);

	    sim.scheduleTask(new Runnable() {
		    public void run() {
			System.out.println("next task is at simulation time "
					   +sim.currentTicks());
		    }
		},
		100);

	    sim.run();
	    System.out.println("run 22 end: current time = "
			       + sim.currentTicks());

	    cq.addSimulationListener(listener);
	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 10);
        
	    cq.add(new Callable() {
		    public void call() {
			System.out.println("cq call done, time = "
					   +sim.currentTicks());
		    }
		}, 20);

	    cq.add(new Runnable() {
		    public void run() {
			System.out.println("task started, current time = "
					   +sim.currentTicks());
			cq.pauseCurrentTask(10);
			System.out.println("task waited on cq, current time = "
					   +sim.currentTicks());
			cq.pauseCurrentTask(10);
			System.out.println("task after final pause, "
					   +"current time = "
					   +sim.currentTicks());

		    }
		}, 10);

	    sim.run();
	    System.out.println("run 23 end: current time = "
			       + sim.currentTicks());

	    sq.addSimulationListener(listener);

	    // repeat old test after listener added
	    sq.add(qc1, 5);
	    sq.add(qc2, 5);
	    sq.add(qc3, 5);
	    sq.add(qc4, 5);

	    sim.run();

	    System.out.println("run 24 end: current time = "
			       + sim.currentTicks());

	    sq.add(qr1, 5);
	    sq.add(qr2, 5);
	    sq.add(qr3, 5);
	    sq.add(qr4, 5);

	    System.out.println("run 25 end : current time = "
			       + sim.currentTicks());

	    sim.scheduleTask(r1, 0);
	    sim.scheduleTask(r2, 0);
	    sim.scheduleTask(r3, 0);
	    sim.scheduleTask(r4, 0);

	    sim.run();
	    System.out.println("run 26 end: current time = "
			       + sim.currentTicks());

	    System.out.println("runs done");

	    System.out.println("check the 'advance' method");

	    sim = new Simulation();

	    System.out.println("before advance: " + sim.currentTicks());
	    sim.advance(1000L);
	    System.out.println("after advance: " + sim.currentTicks());
	    sim.run(1000L);
	    System.out.println("after run: " + sim.currentTicks());



	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
