package org.bzdev.devqsim;

/**
 * Factory for creating instances of PriorityTaskQueue.
 * <P>
 * The parameters are defined as follows:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/PriorityTQFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/PriorityTQFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.PriorityTQFactory
 */
public class PriorityTQFactory
    extends AbstrPriorityTQFactory<PriorityTaskQueue>
{
    Simulation sim;
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public PriorityTQFactory(Simulation sim) {
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
    public PriorityTQFactory() {
	this(null);
    }
    
    @Override
    protected PriorityTaskQueue newObject(String name) {
	return new PriorityTaskQueue(sim, name, willIntern());
    }
}

//  LocalWords:  PriorityTaskQueue IFRAME SRC px steelblue HREF jst
