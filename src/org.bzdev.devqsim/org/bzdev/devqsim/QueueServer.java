package org.bzdev.devqsim;

/**
 * Queue server interface.
 * Queue servers are passed to server queues when a server queue is
 * constructed.
 */
public interface QueueServer {
    /**
     * Get the initial processing time for a queue server. This is the
     * the minimum time an entry consumes when it is removed from a
     * queue and processed.  If the entry is a QueueCallable, the
     * interval is the processing time for the entry - the time it takes
     * to 'service' the entry.  If the entry is a QueueRunnable, the interval
     * is the time it takes between the selection of a server and the
     * call to the QueueRunnable's <code>interactWith</code> method. If
     * the entry is a QueueServerHandler, the entry was the result of a
     * task putting itself on the queue, and the interval is the time
     * between the selection of the server and a call to the
     * QueueServerHandler's <code>interactWith</code> method.
     * <p>
     * In all cases, the value of <code>getInterval()</code> excludes
     * the interval associated with the parameters passed to the server
     * queue when the entry is queued.
     * @return the interval in units of simulation ticks
     */
    long getInterval();
}
