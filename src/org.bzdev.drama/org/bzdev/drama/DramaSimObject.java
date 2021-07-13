package org.bzdev.drama;
import org.bzdev.drama.generic.*;

/**
 * Base class for user-defined objects that are meant for a
 * Drama simulation but are not subclasses of other Drama objects.
 * In particular, the classes Actor, Condition, Group, Domain,
 * and DomainMember do not have this class as a superclass.
 * <P>
 * Because the construct explicitly takes a DramaSimulation as an
 * argument, subclasses of this class will not be added simulations
 * using a different flavor.
 */

public abstract class DramaSimObject
    extends GenericSimObject<DramaSimulation,Actor,Condition,Domain,
	    DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object; null for
     *        an automatically generated name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; 
     *        false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     * @see org.bzdev.devqsim.Simulation#getObjectNames(Class)
     */
    protected DramaSimObject(DramaSimulation sim, String name, boolean intern) 
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }
}

//  LocalWords:  subclasses DomainMember superclass DramaSimulation
//  LocalWords:  IllegalArgumentException getObject getObjectNames
