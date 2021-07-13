package org.bzdev.drama;
import org.bzdev.drama.generic.*;
import org.bzdev.drama.common.ConditionMode;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import org.bzdev.devqsim.SimObject;

/**
 * Top-level basic-simulation class for actors.
 * Actors are task objects that can send and receive messages. They
 * can be members of domains, and when joining a domain, can request
 * to be notified when a condition changes.
 */

public abstract class Actor 
    extends GenericActor<DramaSimulation,Actor,Condition,Domain,
	    DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the actor; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; 
     *        false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.devqsim.Simulation#getObjectNames(Class)
     */
    protected Actor(DramaSimulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }


    /**
     * Respond to a message.
       * Subclasses should either implement this method to perform any
     * necessary processing in response to a message or use dynamic
     * methods. Dynamic Method processing is enabled for only the
     * first argument, and an implementation of a dynamic method must
     * use "org.bzdev.drama.DoReceive" in its {@literal @}DMethodImpl
     * annotation. Such a subclass must be annotated with a {@literal @}
     * DMethodContext annotation whose <code>helper</code> element's
     * value is the string "org.bzdev.drama.DoReceive" and whose
     * <code>localHelper</code> element is the name of the "helper"
     * class that will be generated. A static block should call the
     * local helper's <code>register()</code> method (one class loader
     * will do this automatically, but that class loader may not be
     * installed so it is better to add this code explicitly).
     * <P>
     * If multiple {@literal @}DMethodContext annotations are needed,
     * these should be place in a {@literal @}DMethodContexts annotation
     * (whose value is an array of {@literal @}DMethodContext).
     * @param message the message just received
     * @param source the actor that originated the message
     * @param wereQueued true if the message was queued; false otherwise
     * @see org.bzdev.lang.annotations.DMethodImpl
     * @see org.bzdev.lang.annotations.DMethodOrder
     * @see org.bzdev.lang.annotations.DMethodContext
     * @see org.bzdev.lang.annotations.DMethodContexts
     */
    @Override
    @DynamicMethod("org.bzdev.drama.DoReceive")
    @DMethodOrder({1, 0, 0})
    protected void doReceive(Object message, Actor source, boolean wereQueued)
    {
	try {
	    DoReceive.getHelper().dispatch(this, message, source, wereQueued);
	} catch (MethodNotPresentException e) {}
    }

    /**
     * Respond to a change in conditions
     * Subclasses should either implement this method to perform any necessary
     * processing in response to a condition change or use dynamic methods.
     * When dynamic methods are used, the implementation of a dynamic
     * method must use "org.bzdev.drama.OnConditionChange"in its
     * {@literal @}DMethodImpl annotation. A subclass using the
     * {@literal @}DMethodImpl annotation must be annotated with a
     * {@literal @}DMethodContext annotation whose <code>helper</code>
     * element's value is the string "org.bzdev.drama.OnConditionChange"
     * and whose <code>localHelper</code> element is the name of the
     * "helper" class that will be generated. A static block should
     * call the local helper's <code>register()</code> method (one
     * class loader will do this automatically, but that class loader
     * may not be installed so it is better to add this code
     * explicitly).
     * <P>
     * If multiple {@literal @}DMethodContext annotations are needed,
     * these should be place in a {@literal @}DMethodContexts annotation
     * (whose value is an array of {@literal @}DMethodContext).
     * <P>
     * Finally, dynamic-method processing is applied only to the first
     * argument. The third argument (source) depends on the condition
     * mode:
     * <UL>
     *  <LI> for a mode of <code>CONDITION_DELETED</code>, the third
     *       argument is the condition that was deleted (the same as the
     *       first argument).
     *  <LI> for a mode of <code>DOMAIN_ADDED_CONDITION</code>, the
     *       third argument is the domain that added the condition.
     *  <LI> for a mode of <code>DOMAIN_REMOVED_CONDITION</code>, the
     *       third argument is the domain that added the condition.
     *  <LI> for a mode of <code>OBSERVER_ADDED_CONDITION</code>, the third
     *       argument is the condition observer (e.g., an actor or group)
     *       that added a condition.
     *  <LI> for a mode of <code>OBSERVER_REMOVED_CONDITION</code>, the third
     *       argument is the condition observer (e.g., an actor or group)
     *       that added a condition.
     *  <LI> for a mode of <code>OBSERVER_JOINED_DOMAIN</code>, the third
     *       argument is the domain an actor left
     *  <LI> for a mode of <code>OBSERVER_LEFT_DOMAIN</code>, the third argument
     *       is the domain that an actor left.
     *  <LI> for a mode of <code>OBSERVER_NOTIFIED</code>, the third
     *       argument is the condition that was changed (the same as the
     *       first argument).
     * </UL>
     * The default behavior (unless there is a dynamic method implemented
     * for specific subclasses of the first argument) is to do nothing.
     * @param c the condition that changed
     * @param mode the mode for the condition
     * @param source the object responsible for the change
     * @see org.bzdev.lang.annotations.DMethodImpl
     * @see org.bzdev.lang.annotations.DMethodOrder
     * @see org.bzdev.lang.annotations.DMethodContext
     * @see org.bzdev.lang.annotations.DMethodContexts
     * @see org.bzdev.drama.common.ConditionMode
     */
    @Override
    @DynamicMethod("org.bzdev.drama.OnConditionChange")
    @DMethodOrder({1, 0, 0})
    protected void onConditionChange(Condition c, ConditionMode mode,
				     SimObject source) {
	try {
	    OnConditionChange.getHelper().dispatch(this, c, mode, source);
	} catch (MethodNotPresentException e) {}
    }
}

//  LocalWords:  IllegalArgumentException getObject getObjectNames
//  LocalWords:  Subclasses DMethodImpl DMethodContext localHelper
//  LocalWords:  DMethodContexts wereQueued subclasses
