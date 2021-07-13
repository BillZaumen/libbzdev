package org.bzdev.devqsim;

/**
 * Abstract Factory for LIFO server queues, to support factories for
 * subclasses of LIFO server queues.
 * <P>
 * AbstrLifoSrvrQFactory inherits the factory parameters
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
public abstract class AbstrLifoSrvrQFactory<OBJ extends LifoServerQueue<QS>,
					    QS extends QueueServer>

    extends LinearServerQFactory<OBJ, QS>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstrLifoSrvrQFactory(Simulation sim) {
	super(sim);
    }

}

//  LocalWords:  subclasses AbstrLifoSrvrQFactory queueServer
//  LocalWords:  deletePolicy superclass ServerQueueFactory traceSets
//  LocalWords:  traceSetMode SimObjectFactory getQueueServerClass
//  LocalWords:  getQueueServers
