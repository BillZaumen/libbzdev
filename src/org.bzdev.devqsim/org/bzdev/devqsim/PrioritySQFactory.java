package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;

/**
 * Abstract Factory for priority server queues with the type of the
 * queue server unspecified.
 * <P>
 * Subclasses that are not abstract should set the type parameter QS
 * and implement the method
 * {@link ServerQueueFactory#getQueueServerClass() getQueueServerClass}
 * This can be done using an anonymous class.  For use in a scriptable
 * application, a class should be defined and the fully qualified class
 * name listed in the file org.bzdev.obnaming.NamedObjectFactory in the
 * META-INF/services directory of the application's JAR file.
 * <P>
 * Parameters that are inherited include the following:
 * <ul>
 *   <li> "timeline" - an integer-keyed set of values that define
 *         changes in the object's configuration. Subclasses may provide
 *         additional parameters.  The default parameters are:
 *        <ul>
 *           <li> "timeline.time" - the time at which timeline parameters
 *                are to change. This parameter must be provided if a
 *                timeline entry exists.  The units are those used by the
 *                double-precision time unit for the simulation (for
 *                animations, this is generally seconds).
 *           <li> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpreted. the values are
 *                enumeration constants of type
 *                {@link org.bzdev.devqsim.TraceSetMode} and are used as
 *                follows:
 *                <ul>
 *                  <li> <code>KEEP</code> - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <li> <code>REMOVE</code> - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <li> <code>REPLACE</code> - remove all existing trace sets
 *                       and replace those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </ul>
 *           <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *        </ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 *    <li> "queueServer" - an instance of a the subtype QS of QueueServer.
 *         For this case, one must use the "add" method instead of the
 *         "set" method as the entry will be added to a table.
 *   <li> "deletePolicy" - an enumeration QueueDeletePolicy describing
 *        the 'delete' policy for the queue. The delete policy
 *        determines what happens when the caller deletes a queue
 *        whose length is nonzero.  The values are as follows:
 *        <ul>
 *          <li> <CODE>MUST_BE_EMPTY</CODE> - the queue must be empty
 *               and not processing any more elements before it can be
 *               deleted.
 *          <li> <CODE>WHEN_EMPTY</CODE> - the queue will not accept
 *               new entries after the delete() method is called, with
 *               the actual deletion postponed until the queue is
 *               empty.
 *          <li> <CODE>NEVER</CODE> - the queue may not be deleted.
 *        </ul>
 *        The default is <CODE>WHEN_EMPTY</CODE>.
 * </ul>
 * <P>
 * Subclasses that are not abstract classes must implement the method
 * {@link ServerQueueFactory#getQueueServerClass() getQueueServerClass}.
 * For example,
 * <BLOCKQUOTE><PRE><CODE>
 *   public class FooPrioritySQFactory extends PrioritySQFactory&lt;Foo&gt; {
 *       Class&lt;Foo&gt; getQueueServerClass() {returns Foo.class;}
 *       public FooPrioritySQFactory(Simulation sim) {
 *         super(sim);
 *       }
 *   }
 * </CODE></PRE></BLOCKQUOTE>
 * would create a factory for priority server queues whose servers' class is Foo.
 *
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 * @see org.bzdev.devqsim.ServerQueue
 * @see org.bzdev.devqsim.QueueServer
*/
public abstract class PrioritySQFactory<QS extends QueueServer>
    extends AbstrPrioritySQFactory<PriorityServerQueue<QS>, QS>
{
    private Simulation sim;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public PrioritySQFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
    }

    protected PriorityServerQueue<QS> newObject(String name) {
	return new PriorityServerQueue<QS>(sim, name, willIntern(),
					   getQueueServers());
    }
}

//  LocalWords:  Subclasses QS ServerQueueFactory getQueueServerClass
//  LocalWords:  scriptable ul li traceSetMode traceSets
//  LocalWords:  TraceSet SimObject queueServer subtype QueueServer
//  LocalWords:  deletePolicy QueueDeletePolicy BLOCKQUOTE PRE lt
//  LocalWords:  FooPrioritySQFactory PrioritySQFactory
