package org.bzdev.devqsim;

/**
 * Abstract factory for subclasses of FifoTaskQueue.
 * This class merely reduces the number of type parameters that
 * TaskQueueFactory needs.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link FifoTaskQueueFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/FifoTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/FifoTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.FifoTaskQueue
 */
public abstract class AbstrFifoTaskQFactory<OBJ extends FifoTaskQueue>
    extends DelayTaskQueueFactory<OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrFifoTaskQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses FifoTaskQueue TaskQueueFactory IFRAME SRC
//  LocalWords:  FifoTaskQueueFactory px steelblue HREF
