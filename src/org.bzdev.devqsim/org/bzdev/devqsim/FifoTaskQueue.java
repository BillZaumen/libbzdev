package org.bzdev.devqsim;
import java.util.*;

/**
 * FIFO (First In First Out) delay queue.
 * Task threads and classes that implement the Runnable or Callable
 * interfaces can be put on this queue. The delay for each entry
 * is the time between when the entry is scheduled and restarted, run,
 * or called.
 */
public class FifoTaskQueue extends DelayTaskQueue {
    LinkedList<TaskQueueSimEvent<DelayTaskQueue.Parameter>> queue =
	new LinkedList<TaskQueueSimEvent<DelayTaskQueue.Parameter>>();
 
    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public FifoTaskQueue(Simulation sim, boolean intern) {
	super(sim, intern);
    }


    /**
     * Constructor for named FIFO queues.
     * @param sim the simulation
     * @param name the name for the queue
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public FifoTaskQueue(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    protected int getSize() {return queue.size();}

    protected boolean offerToQueue(TaskQueueSimEvent<DelayTaskQueue.Parameter>
				   event,
				   TaskQueueSimEvent<DelayTaskQueue.Parameter>
				   scheduled)
    {
	return queue.offer(event);
    }

    protected TaskQueueSimEvent<DelayTaskQueue.Parameter> pollFromQueue() {
	return queue.poll();
    }
}

//  LocalWords:  Runnable IllegalArgumentException
