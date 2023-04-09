package org.bzdev.devqsim;

/**
 * Factory for creating LifoTaskQueue instances.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.LifoTaskQueue
 */
public class LifoTaskQueueFactory extends AbstrLifoTaskQFactory<LifoTaskQueue>
{
    Simulation sim;
    /*** Constructor.
     * @param sim the simulation used to name objects.
     */
    public LifoTaskQueueFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public LifoTaskQueueFactory() {
	this(null);
    }

    @Override
    public LifoTaskQueue newObject(String name) {
	return new LifoTaskQueue(sim, name, willIntern());
    }
}

//  LocalWords:  LifoTaskQueue IFRAME SRC px steelblue HREF
