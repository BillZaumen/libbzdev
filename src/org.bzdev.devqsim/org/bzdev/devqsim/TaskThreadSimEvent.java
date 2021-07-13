package org.bzdev.devqsim;

/**
 * Simulation event for task threads.
 */
public final class TaskThreadSimEvent extends TaskSimulationEvent {
    Simulation sim;

    TaskThreadSimEvent(Simulation sim, TaskThread thread) {
	this.sim = sim;
	this.thread = thread;
    }

    // for case where we have a queue of simulation events.
    /**
     * Cancel a thread.
     * @return true if the task can be canceled; false otherwise.
     */
    public  boolean cancel() {
	if (thread == null) return false;
	thread.cancel();
	thread = null;
	return true;
    }

    /**
     * Get the simulation.
     * @return the simulation
     */
    public Simulation getSimulation() {
	return sim;
    }

    protected void processEvent() {
	Object src = getSource();
	if (src != null) {
	    if (src == sim) {
		sim.fireTaskResume(this);
	    } else if (src instanceof SimObject) {
		SimObject obj = (SimObject) src;
		if (obj.isDeleted()) thread.cancel();
		obj.fireTaskResume(this);
	    } 
	}
	boolean interrupted = false;
	synchronized(thread.schedMonitor) {
	    thread.schedPaused = true;
	    synchronized(thread.runnableMonitor) {
		thread.runnablePaused = false;
		// System.out.println("runnablePaused false 5 " +thread.getOurId());
		thread.runnableMonitor.notifyAll();
	    }
	    thread.schedCount++;
	    try {
		//System.out.println("sched wait 4 " +thread.getOurId());
		while (thread.schedPaused) {
		    thread.schedMonitor.wait();
		}
	    } catch (InterruptedException e) {
		//System.out.println("sched wait 4 done (interrupted)");
		interrupted = true;
	    }
	    thread.schedCount--;
	    if (thread.schedCount == 0) {
		//System.out.println("schedPaused true 4 " +thread.getOurId());
		    
		thread.schedPaused = true;
	    }
	    //System.out.println("sched wait 4 done " +thread.getOurId());
	    if (interrupted) Thread.currentThread().interrupt();
	}
	if (src != null) {
	    if (src == sim) {
		if (thread.callingPause) {
		    sim.fireTaskPause(this);
		} else {
		    sim.fireTaskEnd(this);
		}
	    } else if (src instanceof SimObject) {
		SimObject obj = (SimObject) src;
		if (thread.callingPause) {
		    obj.fireTaskPause(this);
		} else {
		    obj.fireTaskEnd(this);
		}
	    } 
	}
    }
}

//  LocalWords:  runnablePaused getOurId sched schedPaused
