package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of DelayTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link LifoTaskQueueFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.DelayTaskQueue
 */
public abstract class DelayTaskQueueFactory<OBJ extends DelayTaskQueue>
    extends TaskQueueFactory<DelayTaskQueue.Parameter,OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected DelayTaskQueueFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses DelayTaskQueue TaskQueueFactory IFRAME px
//  LocalWords:  LifoTaskQueueFactory SRC steelblue HREF
