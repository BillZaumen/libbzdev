package org.bzdev.devqsim;

/**
 * Bind a queue callable to a simulation object.
 * This allows a simulation listener to attribute events to
 * specific simulation objects in cases where this cannot be
 * done directly.  To create an instance of SimObjQueueCallable,
 * use one of the bindCallable methods defined by the SimObject
 * class and with a QueueCallable argument.
 */
public class SimObjQueueCallable<Server extends QueueServer> {
    SimObject source;
    QueueCallable<Server> callable;
    String tag;

    SimObjQueueCallable(SimObject source, QueueCallable<Server> callable) {
	this.source = source;
	this.callable = callable;
	this.tag = null;
    }

    SimObjQueueCallable(SimObject source, 
			QueueCallable<Server> callable,
			String tag)
    {
	this.source = source;
	this.callable = callable;
	this.tag = tag;
    }

    /**
     * Get the source that will be used for a simulation event.
     * @return the source simulation object
     */
    public final SimObject getSource() {return source;}

    /**
     * Get the callable.
     * @return the callable
     */
    public final QueueCallable<Server> getCallable() {return callable;}

    /**
     * Get a descriptive tag.
     * @return the string tagging the callable; null if none provided
     */
    public final String getTag() {return tag;}
}

//  LocalWords:  SimObjQueueCallable bindCallable SimObject
//  LocalWords:  QueueCallable
