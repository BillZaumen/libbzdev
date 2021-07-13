package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.devqsim.DefaultSimObjectFactory;

import org.bzdev.math.rv.RandomVariable;


/**
 * Base class for factories for named random variables that produce a
 * sequence of values of a specified type.
 * The type parameters are:
 * <UL>
 *   <LI> T - the type of the values that the random variable
 *        generates
 *   <LI> RV - the type for the random variables generated.
 *   <LI> NRV - the type of the named random variable corresponding
 *        to RV.
 * </UL>
 */
public abstract class SimRVFactory
    <T,RV extends RandomVariable<T>,NRV extends SimRandomVariable<T,RV>>
    extends DefaultSimObjectFactory<NRV>

{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected SimRVFactory(Simulation sim) {
	super(sim);
    }

    /**
     * Set the random variable for the created named random variable.
     * Subclasses that implement {@link org.bzdev.obnaming.NamedObjectFactory}
     * are expected to call this method after creating a random variable.
     * in its {@link org.bzdev.obnaming.NamedObjectFactory#initObject}}
     * method (which the factory must implement).
     * @param target the instance of the subclass of  SimRandomVariable
     *        being initialized
     * @param rv the random variable that will provide its behavior.
     */
    protected void setRV(NRV target, RandomVariable<?> rv) {
	target.setRV(rv);
    }
}

//  LocalWords:  NRV Subclasses initObject SimRandomVariable rv
