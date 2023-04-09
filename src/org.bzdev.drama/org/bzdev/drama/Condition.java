package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Top-level basic-simulation condition class.
 * Conditions represent quantities that are part of the global state
 * of a simulation, but that are not represented by actors or other
 * more specific simulation objects.  Domains and user-defined
 * condition observers will be notified when a condition changes.
 * <P>
 * Conditions can be used for a variety of purposes, including turning
 * on or off debugging for all the actors associated with a given
 * domain.  The simplest conditions simply provide a value.
 * <P>
 * When a condition changes its state, the object that changed the state
 * is expected to call notifyObservers() followed by completeNotification().
 * The use of two methods allows objects to queue up multiple notifications
 *  and apply them at once.
 * <P>
 * As a general rule, it is preferable to send as few notifications as
 * possible.  For example, if a condition represents a value that is
 * changing with time, one might represent the condition by a value at
 * some time and the rate of change, sending notifications only when new
 * values or rates of change are needed.
 */
abstract public class Condition 
    extends GenericCondition<DramaSimulation,Actor,Condition,
	    Domain, DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the condition; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     */
    protected Condition(DramaSimulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }
}

//  LocalWords:  notifyObservers completeNotification
//  LocalWords:  IllegalArgumentException
