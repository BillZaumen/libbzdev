package org.bzdev.devqsim;

/**
 * Factory for creating WaitTaskQueues.
 * The parameters are defined as follows:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.WaitTaskQueue
 */
public class WaitTaskQueueFactory extends AbstrWaitTaskQFactory<WaitTaskQueue>
{
    Simulation sim;
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public WaitTaskQueueFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * jst calls the default constructor with a null argument.
     */
    public WaitTaskQueueFactory() {
	this(null);
    }

    @Override
    public WaitTaskQueue newObject(String name) {
	return new WaitTaskQueue(sim, name, willIntern());
    }
}

//  LocalWords:  WaitTaskQueues IFRAME SRC px steelblue HREF jst
