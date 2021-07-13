package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;


/**
 *  Abstract factory for server queues that are subclasses of
 *  PriorityServerQueue.
 *  This factory reduces the number of type parameters that must be
 *  provided.
 * <P>
 * AbstrPrioritySQFactory inherits the factory parameters
 * "queueServer" and "deletePolicy" from its superclass
 * {@link ServerQueueFactory} and the parameters "timeline", "timeline.time",
 * "timeline.traceSetMode", "timeline.traceSets", and "traceSets" from
 * {@link SimObjectFactory}.
 * <P>
 * Subclasses that are not abstract classes must implement the method
 * {@link ServerQueueFactory#getQueueServerClass() getQueueServerClass}.
 * Unless the parameter "queueServer" is hidden and thus not used,
 * subclasses will typically use the method
 * {@link ServerQueueFactory#getQueueServers()} to obtain an array of
 * the queue servers that were configured.  This array is needed by
 * the constructors of some subclasses.
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.ServerQueue
 * @see org.bzdev.devqsim.QueueServer
*/
public abstract class AbstrPrioritySQFactory<
    OBJ extends PriorityServerQueue<QS>,
    QS extends QueueServer>
    extends ServerQueueFactory<OBJ, PriorityTaskQueue.PriorityParam, QS>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrPrioritySQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  subclasses PriorityServerQueue queueServer traceSets
//  LocalWords:  AbstrPrioritySQFactory deletePolicy superclass
//  LocalWords:  ServerQueueFactory traceSetMode SimObjectFactory
//  LocalWords:  getQueueServerClass getQueueServers
