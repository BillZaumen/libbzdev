package org.bzdev.drama;
import org.bzdev.drama.generic.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;


/**
 * Top-level class for groups of message recipients.
 * While only actors can originate messages, groups can distribute
 * those messages to multiple recipients.  Actors may join one or
 * more groups. A member of a group may be another group (there is
 * a circularity check to avoid infinite loops).

 */

abstract public class Group 
    extends GenericGroup<DramaSimulation,Actor,Condition,Domain,
	    DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the group
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected Group(DramaSimulation sim, String name, boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * React to a message.
     * This should be overridden if the group tracks the messages it
     * relays. The default implementation does nothing.
     * Subclasses should either implement this method to perform any necessary
     * processing in response to a message or use dynamic methods.
     * Dynamic Method processing is enabled for only the third argument, and an
     * implementation of a dynamic method must use
     * "org.bzdev.drama.ReactToMessage" in its {@literal @}DMethodImpl
     * annotation.
     * @param source the message source
     * @param intermediateHop the last group to relay the message; null if none
     * @param message the message
     * @param recipientCount the number of recipients receiving
     *        the message.
     * @see org.bzdev.lang.annotations.DMethodImpl
     * @see org.bzdev.lang.annotations.DMethodOrder
     * @see org.bzdev.lang.annotations.DMethodContext
     * @see org.bzdev.lang.annotations.DMethodContexts
     */
    @Override
    @DynamicMethod("org.bzdev.drama.ReactToMessage")
    @DMethodOrder({0, 0, 1, 0})
    protected void reactToMessage(Actor source, Group intermediateHop,
				  Object message, int recipientCount)
    {
	try {
	    ReactToMessage.getHelper().dispatch(this, source, intermediateHop,
						message, recipientCount);
	} catch (MethodNotPresentException e) {}
    }
}

//  LocalWords:  IllegalArgumentException Subclasses DMethodImpl
//  LocalWords:  intermediateHop recipientCount
