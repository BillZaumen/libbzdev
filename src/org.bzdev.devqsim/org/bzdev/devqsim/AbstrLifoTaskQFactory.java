package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of LifoTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link LifoTaskQueueFactory}:
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
public abstract class AbstrLifoTaskQFactory<OBJ extends LifoTaskQueue>
    extends DelayTaskQueueFactory<OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrLifoTaskQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses LifoTaskQueue TaskQueueFactory IFRAME SRC
//  LocalWords:  LifoTaskQueueFactory px steelblue HREF
