package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of DelayTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link PriorityTQFactory}:
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
 * @see org.bzdev.devqsim.PriorityTaskQueue
 */
public abstract class AbstrPriorityTQFactory
    <OBJ extends PriorityTaskQueue>
    extends TaskQueueFactory<PriorityTaskQueue.PriorityParam,OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrPriorityTQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses DelayTaskQueue TaskQueueFactory IFRAME px
//  LocalWords:  PriorityTQFactory SRC steelblue HREF
