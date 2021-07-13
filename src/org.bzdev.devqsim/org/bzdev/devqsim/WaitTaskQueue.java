package org.bzdev.devqsim;

/**
 * Class for wait task queues.
 * Unless a subclass specifies otherwise, instances
 * of WaitTaskQueue are initially frozen so that all entries added
 * to the queue are queued and not run until explicitly released.  The
 * methods that allow entries to be run when the queue is frozen are
 * {@link TaskQueue#release(int) release},
 * {@link TaskQueue#releaseUpTo(int) releaseUpTo}, and
 * and {@link TaskQueue#clearReleaseCount() clearReleaseCount}.  Either
 * these or the method {@link TaskQueue#freeze(boolean freeze) freeze} (
 * with an argument whose value is <code>false</code>). Otherwise the
 * queue will never decrease in size.  For subclasses
 * in which {@link TaskQueue#freeze(boolean freeze) freeze} must not be
 * used, the protected method
 * {@link TaskQueue#setCanFreeze(boolean) setCanFreeze} should be called
 * with an argument of false after the queue is properly configured.
 */
public class WaitTaskQueue extends AbstractWaitTaskQueue {

    /**
     * Constructor.
     * @param sim the simulation
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     */
    public WaitTaskQueue(Simulation sim, boolean intern) {
	super(sim, null, intern);
	setCanFreeze(true);
	setCanRelease(true);
    }

    /**
     * Constructor for named queues.
     * @param sim the simulation
     * @param name the name for the queue
     * @param intern true of the queue should be interned in the simulation's
     *               name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    public WaitTaskQueue(Simulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	setCanFreeze(true);
	setCanRelease(true);
    }
}

//  LocalWords:  WaitTaskQueue TaskQueue releaseUpTo boolean
//  LocalWords:  clearReleaseCount subclasses setCanFreeze
//  LocalWords:  IllegalArgumentException
