package org.bzdev.devqsim;
import java.util.*;

/**
 * LIFO (Last In First Out) delay queue.
 */
public class LifoTaskQueue extends DelayTaskQueue {
    LinkedList<TaskQueueSimEvent<DelayTaskQueue.Parameter>> queue =
	new LinkedList<TaskQueueSimEvent<DelayTaskQueue.Parameter>>();
 
    private Simulation sim;

    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true if the queue should be interned, false otherwise.
     */
    public LifoTaskQueue(Simulation sim, boolean intern) {
	super(sim, intern);
	this.sim = sim;
    }

    protected int getSize() {return queue.size();}

    /**
     * Constructor with name.
     * @param sim the simulation
     * @param name the name of the queue
     * @param intern true if the queue should be interned, false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public LifoTaskQueue(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	this.sim = sim;
    }

    public boolean canPreempt() {return true;}

    protected boolean offerToQueue(TaskQueueSimEvent<DelayTaskQueue.Parameter>
				   event,
				   TaskQueueSimEvent<DelayTaskQueue.Parameter>
				   scheduled)
    {
	if (scheduled != null) {
	    long interval;
	    if (scheduled.isPending()) {
		interval = sim.currentTicks() - scheduled.offQueueTime;
	    } else {
		interval = 0;
	    }
	    scheduled.parameters.interval -= interval;
	    if (scheduled.parameters.interval < 0)
		scheduled.parameters.interval = 0;
	    replaceScheduledEvent(event);
	    queue.addFirst(scheduled);
	    return true;
	} else {
	    queue.addFirst(event);
	    return true;
	}
    }

    protected TaskQueueSimEvent<DelayTaskQueue.Parameter> pollFromQueue() {
	return queue.poll();
    }
}

//  LocalWords:  IllegalArgumentException
