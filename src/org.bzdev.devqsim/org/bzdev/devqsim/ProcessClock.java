package org.bzdev.devqsim;
import org.bzdev.lang.Callable;

/**
 * Process-clock service.
 * Some processes can work on only one task at a time.  A good example is
 * a CPU.
 * This class models the time consumed by such processes.
 * Requests to advance the clock are posted, given the time interval
 * to advance the clock and a priority for the request.  If resources
 * are available, the request is handled immediately, otherwise it is
 * queued.  Once dequeued, a request will not be preempted.  Until
 * a request executes, it can be canceled.  Once the request can be
 * handled, the clock will advance by the time given in the request's
 * interval argument before the requested action occurs.
 */
public class ProcessClock extends DefaultSimObject {

    private PriorityTaskQueue pq;

    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public ProcessClock(Simulation sim, boolean intern) {
	super(sim, null, intern);
	pq = new PriorityTaskQueue(sim, false);
	pq.preempt(true);
    }

    /**
     * Constructor for a named ProcessClock.
     * @param sim the simulation
     * @param name the name for the queue
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public ProcessClock(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	pq = new PriorityTaskQueue(sim, false);
	pq.preempt(true);
    }

    /**
     * Advance a clock once resources are available and then call a Callable.
     * @param callable the Callable to use
     * @param priority the priority for callable
     * @param interval the process-clock time consumed
     * @return the SimulationEvent created
     */
    public SimulationEvent
	advance(Callable callable, int priority, long interval) 
    {
	return pq.add(callable, priority, interval);
    }


    /**
     * Advance a clock once resources are available and then start 
     * a TaskThread using a Runnable.
     * @param runnable the Runnable to use
     * @param priority the priority for the Runnable
     * @param interval the process-clock time consumed
     * @return the SimulationEvent created
     */
    public SimulationEvent
	advance(Runnable runnable, int priority,  long interval) 
    {
	return pq.add(runnable, priority, interval);
    }

    /**
     * Advance a clock once resources are available with a TaskThread pausing
     * until this operation is complete.
     * @param priority the priority for callable to be added
     * @param interval the process-clock time consumed
     */
    public void advance(int priority, long interval)
	throws IllegalStateException 
    {
	pq.addCurrentTask(priority, interval);
    }

    /**
     * Advance a clock  once resources are available with a TaskThread pausing
     * until this operation is complete but allowing for cancellation.
     * @param priority the priority for callable to be added
     * @param interval the process-clock time consumed
     * @param callable this argument's call method will be run when the
     *                 event is scheduled and will be passed a simulation
     *                 event (e.g., to store the event to allow the
     *                 event to be canceled)
     */
    public void advance(int priority, long interval, 
			       SimEventCallable callable)
	throws IllegalStateException 
    {
	pq.addCurrentTask(priority, interval, callable);
    }

    /**
     * {@inheritDoc}
     * In addition, the configuration that is printed includes the following\
     * items:
     * <P>
     * Defined in {@link ProcessClock}:

     * <UL>
     *  <LI> the configuration for an internal priority task queue.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix,
				   String prefix, boolean printName,
				   java.io.PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "[internal priority task queue]:");
	pq.printConfiguration(null, prefix + "    ", false, out);
    }

    /**
     * {@inheritDoc}
     * In addition, the state that is printed includes the following
     * items.
     * <P>
     * Defined in {@link ProcessClock}:
     * <UL>
     *  <LI> the state for an internal priority task queue.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "[internal priority task queue]:");
	pq.printState(null, prefix + "    ", false, out);
    }
}

//  LocalWords:  dequeued request's sim ProcessClock SimulationEvent
//  LocalWords:  IllegalArgumentException TaskThread printName
