package org.bzdev.devqsim;

/**
 * Interface for executing some code on a server queue.
 * When processed, the <code>interactWith</code> method is run first,
 * with a queue server as its argument.  After the queue server is
 * released, the <code>call</code> method is executed to allow for any
 * necessary processing after the server is done.
 */
public interface QueueCallable<Server extends QueueServer> {
    /**
     * Interact with a queue server.
     * This method is called when this object is at the head of a
     * server queue and ready to be processed by a queue server.
     * @param server the server handling this QueueCallable
     */
    void interactWith(Server server);
    /**
     * The method to call.
     * Users will implement this method so that blocks of code can
     * be posted to the event queue.
     */
    void call();

}

//  LocalWords:  interactWith QueueCallable
