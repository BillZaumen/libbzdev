package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Array;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract factory for server queues.
 * This class is the base class for factories that create subclasses
 * of {@link org.bzdev.devqsim.ServerQueue  ServerQueue}.
 * <P>
 * A server queue contains a task queue, but the 'release' mechanism
 * is not enabled by default, so only a subset of the parameters
 * used to configure task queues are provided. In addition, the queue
 * servers need to be specified. As a result, the parameters that
 * this factory provides are the following:
 * <ul>
 *    <li> "queueServer" - an instance of the subtype QS of QueueServer.
 *         For this case, one must use the "add" method instead of the
 *         "set" method as the entry will be added to a set of queue servers.
 *   <li> "deletePolicy" - an enumeration QueueDeletePolicy describing
 *        the 'delete' policy for the queue. The delete policy
 *        determines what happens when the caller deletes a queue
 *        whose length is nonzero.  The values are as follows:
 *        <ul>
 *          <li> <CODE>MUST_BE_EMPTY</CODE> - the queue must be empty
 *               and not processing any more elements before it can be
 *               deleted.
 *          <li> <CODE>WHEN_EMPTY</CODE> - the queue will not accept
 *                new entries after the delete() method is called,
 *                with the actual deletion postponed until the queue
 *                is empty.
 *          <li> <CODE>NEVER</CODE> - the queue may not be deleted.
 *        </ul>
 *        The default is <CODE>WHEN_EMPTY</CODE>.
 * </ul>
 * <P>
 * In addition, a server queue inherits the parameters
 * timeline, timeline.time, timeline.traceSetMode, timeline.traceSets,
 * and traceSets provided by {@link SimObjectFactory}.
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

public abstract class ServerQueueFactory<OBJ extends ServerQueue<T,QS>,
					 T,QS extends QueueServer>
    extends DefaultSimObjectFactory<OBJ>
{
    private QueueDeletePolicy deletePolicy = QueueDeletePolicy.WHEN_EMPTY;

    /**
     * Get the base class of the objects implementing the QueueServer
     * interface for this factory.
     */
    protected abstract Class<QS> getQueueServerClass();

    private Set<QS> qsSet = new HashSet<QS>();

    /**
     * Get the number of queue servers that have been configured.
     * @return the number of queue servers
     */
    protected int numberOfQueueServers() {
	return qsSet.size();
    }

   private static String errorMsg(String key, Object... args) {
       return Simulation.errorMsg(key, args);
    }

    /**
     * Get the queue servers.
     * @return an array of queue servers
     */
    @SuppressWarnings("unchecked")
    protected QS[] getQueueServers() {
	Object object = Array.newInstance(getQueueServerClass(), qsSet.size());
	try {
	    QS[] array = (QS[]) object;
	    return qsSet.toArray(array);
	} catch (Exception e) {
	    // should not fail.
	    throw new Error(errorMsg("arrayAllocFailed", "ServerQueueFactory"));
	    /*
	    throw new Error("ServerQueueFactory: array allocation failed", e);
	    */
	}
    }

    Parm[] parms = {
	new Parm
	("queueServer", getQueueServerClass(), null,
	 new ParmParser() {
	     public void parse(org.bzdev.obnaming.NamedObjectOps value) {
		 Class<QS> clazz = getQueueServerClass();
		 if (clazz.isAssignableFrom(value.getClass())) {
		     try {
		     QS v = clazz.cast(value);
		     qsSet.add(v);
		     } catch (Exception e) {}
		 } else throw new IllegalArgumentException
			    (errorMsg("wrongType1", getParmName()));
			    /*("wrong type");*/
	     }
	     public void clear() {
		 qsSet.clear();
	     }
	     public void clear(org.bzdev.obnaming.NamedObjectOps value) {
		 Class<QS> clazz = getQueueServerClass();
		 if (clazz.isAssignableFrom(value.getClass())) {
		     try {
			 QS v = clazz.cast(value);
			 qsSet.remove(v);
		     } catch (Exception e) {}
		 }
	     }
	 },
	 null,
	 null, true, null, true),
	new Parm("deletePolicy", null,
		 new ParmParser() {
		     public void parse(java.lang.Enum<?> value) {
			 if (value instanceof QueueDeletePolicy) {
			     deletePolicy = (QueueDeletePolicy)value;
			 } else {
			     throw new IllegalArgumentException
				 (errorMsg("wrongType1", getParmName()));
			         /*("wrong type");*/
			 }
		     }
		     public void clear() {
			 deletePolicy = QueueDeletePolicy.WHEN_EMPTY;
		     }
		 },
		 QueueDeletePolicy.class,
		 null, true, null,true)
    };

    public void clear() {
	super.clear();
	deletePolicy = QueueDeletePolicy.WHEN_EMPTY;
	qsSet.clear();
    }

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected ServerQueueFactory(Simulation sim) {
	super(sim);
	initParms(parms, ServerQueueFactory.class);
	// The same labels and tips that a TaskQueue uses
	// can be used here.
 	addLabelResourceBundle("*.lpack.ServerQueueLabels",
			       SimFunctionFactory.class);
	addTipResourceBundle("*.lpack.ServerQueueTips",
			     SimFunctionFactory.class);
	addDocResourceBundle("*.lpack.ServerQueueDocs",
			     SimFunctionFactory.class);
    }

    @Override
    protected void initObject(OBJ object) {
	super.initObject(object);
	object.setDeletePolicy(deletePolicy);
   }
}

//  LocalWords:  exbundle subclasses ServerQueue ul li queueServer QS
//  LocalWords:  subtype QueueServer deletePolicy QueueDeletePolicy
//  LocalWords:  traceSetMode traceSets SimObjectFactory wrongType
//  LocalWords:  ServerQueueFactory getQueueServerClass TaskQueue
//  LocalWords:  getQueueServers arrayAllocFailed
