package org.bzdev.devqsim;

/**
 * Abstract factory class for FIFO server queues with the type of the
 * queue servers unspecified.
 * <P>
 * AbstrLifoSrvrQFactory inherits the factory parameters
 * "queueServer" and "deletePolicy" from its superclass
 * {@link ServerQueueFactory} and the parameters "timeline", "timeline.time",
 * "timeline.traceSetMode", "timeline.traceSets", and "traceSets" from
 * {@link SimObjectFactory}.
 * <P>
 * <P>
 * Subclasses that are not abstract should set the type parameter QS
 * and implement the method
 * {@link ServerQueueFactory#getQueueServerClass() getQueueServerClass}
 * This can be done using an anonymous class.  For use in a scriptable
 * application, a class should be defined and the fully qualified class
 * name listed in the file org.bzdev.obnaming.NamedObjectFactory in the
 * META-INF/services directory of the application's JAR file.
 * <P>
 * Subclasses that are not abstract classes must implement the method
 * {@link ServerQueueFactory#getQueueServerClass() getQueueServerClass}.
 * For example,
 * <BLOCKQUOTE><CODE><PRE>
 *   public class FooFifoSQFactory extends FifoServerQueueFactory&lt;Foo&gt; {
 *       Class&lt;Foo&gt; getQueueServerClass() {returns Foo.class;}
 *       public FooPrioritySQFactory(Simulation sim) {
 *         super(sim);
 *       }
 *   }
 * </PRE></CODE></BLOCKQUOTE>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.ServerQueue
 * @see org.bzdev.devqsim.QueueServer
 */
public abstract class FifoServerQueueFactory<QS extends QueueServer>
    extends AbstrFifoSrvrQFactory<FifoServerQueue<QS>, QS>
{
    private Simulation sim;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public FifoServerQueueFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
    }

    protected FifoServerQueue<QS> newObject(String name) {
	return new FifoServerQueue<QS>(sim, name, willIntern(),
				       getQueueServers());
    }
}

//  LocalWords:  AbstrLifoSrvrQFactory queueServer deletePolicy QS lt
//  LocalWords:  superclass ServerQueueFactory traceSetMode traceSets
//  LocalWords:  SimObjectFactory Subclasses getQueueServerClass PRE
//  LocalWords:  scriptable BLOCKQUOTE FooFifoSQFactory
//  LocalWords:  FifoServerQueueFactory FooPrioritySQFactory
