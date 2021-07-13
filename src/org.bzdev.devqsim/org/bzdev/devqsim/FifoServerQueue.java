package org.bzdev.devqsim;

/**
 * FIFO server queue.
 * This implements a ServerQueue in which the entries are processed in
 * FIFO (First In, First Out) order.
 * 
 * @see org.bzdev.devqsim.ServerQueue
 */

public class FifoServerQueue<QS extends QueueServer> 
    extends LinearServerQueue<QS> 
{
    FifoTaskQueue tq;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the queue
     * @param intern true if the queue name should be interned in the
     *        simulation tables; false otherwise
     * @param servers the queue's servers
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    @SuppressWarnings("unchecked")
    public FifoServerQueue(Simulation sim, String name,
			   boolean intern, QS... servers) 
	throws IllegalArgumentException
    {
	super(sim, name, intern,
	      new FifoTaskQueue(sim, false) {
		  public long getInterval(DelayTaskQueue.Parameter params)
		  {
		      return 0;
		  }
		  protected void init() {
		      setCanFreeze(true);
		  }
	      },
	      servers);
    }
}

//  LocalWords:  ServerQueue IllegalArgumentException
