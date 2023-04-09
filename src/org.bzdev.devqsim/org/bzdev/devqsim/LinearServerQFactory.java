package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;


/**
 *  Abstract Factory for server queues that maintain a linear ordering.
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
public abstract class LinearServerQFactory<
    OBJ extends LinearServerQueue<QS>,
    QS extends QueueServer>
    extends ServerQueueFactory<OBJ, DelayTaskQueue.Parameter, QS>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected LinearServerQFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  AbstrPrioritySQFactory queueServer deletePolicy
//  LocalWords:  superclass ServerQueueFactory traceSetMode traceSets
//  LocalWords:  SimObjectFactory Subclasses getQueueServerClass
//  LocalWords:  subclasses getQueueServers
