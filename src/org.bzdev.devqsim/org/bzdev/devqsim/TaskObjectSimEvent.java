package org.bzdev.devqsim;
import org.bzdev.lang.Callable;

class TaskObjectSimEvent extends TaskSimulationEvent {
    Callable callable;

    public String getTag() {return tag;}
   
    boolean threadStarter = false;

    TaskObjectSimEvent(Callable callable) {
	super();
	this.callable = callable;
    }

    TaskObjectSimEvent(Callable callable, String tag) {
	super(tag);
	this.callable = callable;
    }

    TaskObjectSimEvent(Callable callable, TaskThread thread) {
	super(thread);
	this.callable = callable;
    }

    TaskObjectSimEvent(Callable callable, TaskThread thread,
			      String tag)
    {
	super(thread, tag);
	this.callable = callable;
    }


    protected void processEvent() {
	Object source = getSource();
	if (thread != null) {
	    if (source instanceof SimObject) {
		// if (((SimObject)source).isDeleted()) return;
		((SimObject)source).fireTaskStart(this);
		callable.call();
		TaskSimulationEvent tsEvent = thread.getSimulationEvent();
		if (tsEvent != null &&
			   tsEvent instanceof TaskThreadSimEvent) {
		    ((SimObject)source)
			.fireTaskPause((TaskThreadSimEvent)tsEvent);
		} else {
		    ((SimObject)source).fireTaskEnd(this);
		}
	    } else if (source instanceof Simulation) {
		((Simulation)source).fireTaskStart(this);
		callable.call();
		TaskSimulationEvent tsEvent = thread.getSimulationEvent();
		if (tsEvent != null &&
		    tsEvent instanceof TaskThreadSimEvent) {
		    ((Simulation)source)
			.fireTaskPause((TaskThreadSimEvent)tsEvent);
		}  else {
		    ((Simulation)source).fireTaskEnd(this);
		}
	    } else {
		callable.call();
	    }
	} else {
	    if (source instanceof SimObject) {
		// if (((SimObject)source).isDeleted()) return;
		((SimObject)source).fireCallStart(this);
		callable.call();
		((SimObject)source).fireCallEnd(this);
	    } else if (source instanceof Simulation) {
		((Simulation)source).fireCallStart(this);
		callable.call();
		((Simulation)source).fireCallEnd(this);
	    } else {
		callable.call();
	    }
	}
    }
}

//  LocalWords:  SimObject isDeleted
