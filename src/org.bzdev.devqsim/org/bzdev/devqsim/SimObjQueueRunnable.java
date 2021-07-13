package org.bzdev.devqsim;

/**
 * Bind a queue runnable to a simulation object.
 * This allows a simulation listener to attribute events to
 * specific simulation objects in cases where this cannot be
 * done directly.   To create an instance of SimObjQueueRunnable,
 * use one of the bindRunnable methods defined by the SimObject
 * class and with a QueueRunnable argument.
 */
public class SimObjQueueRunnable<Server extends QueueServer> {
    SimObject source;
    QueueRunnable<Server> runnable;
    String tag;

    SimObjQueueRunnable(SimObject source, QueueRunnable<Server> runnable) {
	this.source = source;
	this.runnable = runnable;
	this.tag = null;
    }

    SimObjQueueRunnable(SimObject source, QueueRunnable<Server> runnable,
			String tag)
    {
	this.source = source;
	this.runnable = runnable;
	this.tag = tag;
    }

    /**
     * Get the source that will be used for a simulation event.
     * @return the source simulation object
     */
    public final SimObject getSource() {return source;}

    /**
     * Get the runnable.
     * @return the runnable
     */
    public final QueueRunnable<Server> getRunnable() {return runnable;}

    /**
     * Get a descriptive tag.
     * @return the string tagging the runnable; null if none provided
     */
    public final String getTag() {return tag;}
}

//  LocalWords:  runnable SimObjQueueRunnable bindRunnable SimObject
//  LocalWords:  QueueRunnable
