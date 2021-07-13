package org.bzdev.devqsim;

/**
 * Interface for interacting with a queue server. 
 * This is used when a TaskThread has been suspended and put on
 * a server queue and that entry is being processed by a queue
 * server.
 */

public interface QueueServerHandler<Server extends QueueServer> {
    /**
     * Interact with a queue server.
     * This method is called when this object is at the head of a
     * server queue and ready to be processed by a queue server.
     * @param server the server handling this QueueCallable
     */
    public void interactWith(Server server);
}

//  LocalWords:  TaskThread QueueCallable
