package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Convenience class for creating new task object subclasses.
 * This class merely provides the appropriate values for the
 * 7 type parameters needed by org.bzdev.drama.generic.TaskObject.
 * <p>
 * It provides a superclass for those classes that need standard
 * methods for scheduling a specific task or method call. Subclasses
 * can provide implementations of the methods defaultCall() and
 * defaultTask() to provide default code to either call or run in a
 * task thread. Methods allow these to be scheduled and canceled.

 * @see org.bzdev.drama.generic.GenericTaskObject
 */
public abstract class AbstractTaskObject
    extends GenericTaskObject<DramaSimulation,Actor,Condition,Domain,
	    DomainMember,DramaFactory,Group>
{
        /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the agent
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.devqsim.Simulation#getObjectNames(Class)
     */
    protected AbstractTaskObject(DramaSimulation sim,
				 String name,
				 boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }
}

//  LocalWords:  subclasses superclass defaultCall defaultTask
//  LocalWords:  IllegalArgumentException getObject getObjectNames
