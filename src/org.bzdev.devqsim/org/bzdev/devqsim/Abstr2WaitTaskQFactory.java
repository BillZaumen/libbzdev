package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of AbstractWaitTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link WaitTaskQueueFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/WaitTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 */
public abstract class Abstr2WaitTaskQFactory<OBJ extends AbstractWaitTaskQueue>
    extends TaskQueueFactory<AbstractWaitTaskQueue.Parameter,OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected Abstr2WaitTaskQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses AbstractWaitTaskQueue TaskQueueFactory px
//  LocalWords:  WaitTaskQueueFactory IFRAME SRC steelblue HREF
