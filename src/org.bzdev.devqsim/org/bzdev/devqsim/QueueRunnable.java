package org.bzdev.devqsim;

/**
 * Interface for executing some code on a server queue.

 * When processed, a thread is started and the
 * <code>interactWith</code> method is called first, with a queue server
 * as its argument.  After the queue server is released, the
 * <code>run</code> method is executed to allow for any necessary
 * processing after the server is done.  In both cases, the methods
 * are called from the newly running thread.
 */
public interface QueueRunnable<Server extends QueueServer> {
    /**
     * Interact with a queue server.
     * This method is called when this object is at the head of a
     * server queue and ready to be processed by a queue server.
     * @param server the server handling this QueueCallable
     */

    public void interactWith(Server server);
    /**
     * The method to call after interactWith returns.
     * Users will implement this method so that blocks of code can be
     * executed after a queue server finishes its processing.
     */
    public void run();
}

//  LocalWords:  interactWith QueueCallable
