package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of WaitTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link WaitTaskQueueFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.WaitTaskQueue
 */
public abstract class AbstrWaitTaskQFactory<OBJ extends WaitTaskQueue>
    extends Abstr2WaitTaskQFactory<OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrWaitTaskQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses WaitTaskQueue TaskQueueFactory IFRAME SRC
//  LocalWords:  WaitTaskQueueFactory px steelblue HREF
