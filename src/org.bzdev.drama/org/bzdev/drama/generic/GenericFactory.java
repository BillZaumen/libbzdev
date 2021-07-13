package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Simulation factory class for implicitly created objects.
 * The minimum requirement for the simulation factory class is to
 * provide standard methods for creating instances of DomainMember.
 * Subclasses may define methods that create other objects as well.
 * As with all factories, the objective is to allow the factory to
 * provide arguments to a constructor that the user of the factory
 * would prefer not to provide explicitly.  <p> Instances of
 * GenericSimObjectFactory are used to explicitly create simulation
 * objects (e.g., for initialization via a GUI or input file).  The
 * Simulation class has methods for creating instances of subclasses
 * of GenericSimObjectFactory.
 */
abstract public class GenericFactory<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
{
    S simulation = null;

    void setSimulation(S simulation) {this.simulation = simulation;}

    /**
     * Get the simulation for this factory.
     * @return the simulation
     */
    public S getSimulation() {return simulation;}


    /**
     * Create an uninterned domain member.
     */
    abstract public DM createDomainMember();

    /*
     * Create a named domain member.
     * @param name the name of the domain member
     * @param intern true if the domain member should be interned in the
     *        simulation's name table; false otherwise
     * @exception IllegalArgumentException typically means a name is already
     *            in use
    abstract public DM createDomainMember(String name, boolean intern)
	throws IllegalArgumentException;
     */
}

//  LocalWords:  DomainMember Subclasses GenericSimObjectFactory DM
//  LocalWords:  subclasses uninterned IllegalArgumentException
//  LocalWords:  createDomainMember boolean
