package org.bzdev.drama;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.drama.generic.*;
import org.bzdev.devqsim.*;

/**
 * Top level class for a basic simulation.  
 * This class models the flow of time, handles scheduling of events,
 * and provides tables that allow simulation objects to be looked up by
 * name.  It also allows named objects interned in the simulation's
 * tables to be retrieved, with access methods for specific types of
 * objects (Actors, etc.)
 */
public class DramaSimulation 
    extends GenericSimulation<DramaSimulation,Actor,Condition,
	    Domain,DomainMember,DramaFactory,Group>
{
    /**
     * Constructor.
     */
    public DramaSimulation() {
	this(new DramaFactory());
    }

    /**
     * Constructor with time-unit specification.
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    public DramaSimulation(double ticksPerUnitTime) {
	this(new DramaFactory(), ticksPerUnitTime);
	// System.out.println("called tickrate = " + ticksPerUnitTime);
    }

    /**
     * Constructor providing a factory.
     * @param factory the simulation factory
     */
    public DramaSimulation(DramaFactory factory) {
	super(factory);
	// System.out.println("called with no parent (factory)");
    }

    /**
     * Constructor providing a factory and unit-time specification.
     * The factory argument is provided to allow the default factory
     * to be replaced.  The default factory is used to create DomainMember
     * instances. A subclass might provide additional methods, for use
     * in conjunction with a subclass of DramaSimulation.
     * @param factory the simulation factory
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    public DramaSimulation(DramaFactory factory, double ticksPerUnitTime) {
	super(factory, ticksPerUnitTime);
	// System.out.println("called with no parent (factory, tickrate)");
    }


    /**
     * Constructor providing a parent.
     * When a simulation has a parent, the simulation's scripting
     * context is that of its parent unless specific methods are
     * overridden.  When a simulation has a parent that is also a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them.
     * @param parent the parent simulation or scripting context
     */
    public DramaSimulation(ScriptingContext parent) {
	this(parent, new DramaFactory());
    }

    /**
     * Constructor providing a parent and time-unit specification.
     * When a simulation has a parent, the simulation's scripting
     * context is that of its parent unless specific methods are
     * overridden.  When a simulation has a parent that is also a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them.
     * @param parent the parent simulation or scripting context
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    public DramaSimulation(ScriptingContext parent, double ticksPerUnitTime) {
	this(parent, new DramaFactory(), ticksPerUnitTime);
    }


    /**
     * Constructor with a parent and factory.
     * When a simulation has a parent, the simulation's scripting
     * context is that of its parent unless specific methods are
     * overridden.  When a simulation has a parent that is also a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them.
     * @param parent the parent simulation or scripting context.
     * @param factory the simulation factory
     */
    public DramaSimulation(ScriptingContext parent, DramaFactory factory) {
	super(parent, factory);
	/*
	System.out.println("called with parent class "
			   + ((parent == null)? null :
			      parent.getClass().getName()));
	*/
    }

    /**
     * Constructor with a parent, factory, and time-unit specification.
     * When a simulation has a parent, the simulation's scripting
     * context is that of its parent unless specific methods are
     * overridden.  When a simulation has a parent that is also a
     * Simulation, the parent's event queue is used instead of the
     * simulation's event queue and time structures. This allows
     * multiple simulations, perhaps with different flavors, to be
     * combined into a single simulation. While the event queue and
     * simulation time are shared, tables of simulation objects are
     * not.  Running any of the simulations sharing a parent will run
     * all of them.
     * <P>
     * The factory argument is provided to allow the default factory
     * to be replaced.  The default factory is used to create DomainMember
     * instances. A subclass might provide additional methods, for use
     * in conjunction with a subclass of DramaSimulation.
     * @param parent the parent simulation or scripting context.
     * @param factory the simulation factory
     * @param ticksPerUnitTime the number of ticks per unit time
     */
    public DramaSimulation(ScriptingContext parent, DramaFactory factory,
			   double ticksPerUnitTime) {
	super(parent, factory, ticksPerUnitTime);
	/*
	System.out.println("called with parent class "
			   + ((parent == null)? null :
			      parent.getClass().getName()));
	*/
    }


    /**
     * Get an actor.
     * @param name the actor's name
     * @return the actor; null if none with that name
     */
    public Actor getActor(String name) {
     	return (Actor)getObject(name, Actor.class);
    }

    /**
     * Get an condition.
     * @param name the condition's name
     * @return the condition; null if none with that name
     */
    public Condition getCondition(String name) {
     	return (Condition) getObject(name, Condition.class);
    }

    /**
     * Get a domain.
     * @param name the domain's name
     * @return the domain; null if none with that name
     */
    public Domain getDomain(String name) {
     	return (Domain) getObject(name, Domain.class);
    }

    /**
     * Get a domain member.
     * @param name the domain member's name
     * @return the domain member; null if none with that name
     */
    public DomainMember getDomainMember(String name) {
     	return (DomainMember) getObject(name, DomainMember.class);
    }

    /**
     * Get a group.
     * @param name the group's name
     * @return the group; null if none with that name
     */
    public Group getGroup(String name) {
     	return (Group) getObject(name, Group.class);
    }
}

//  LocalWords:  ticksPerUnitTime tickrate DomainMember
//  LocalWords:  DramaSimulation
