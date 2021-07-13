package org.bzdev.devqsim;
import java.io.PrintWriter;

/**
 * Base class for simulation objects that are part of the
 * devqsim package.
 * Each simulation package contains a subcless of
 * {@link SimObject} that is a base class for additional simulation objects
 * created in that package: for example,
 * {@link org.bzdev.anim2d.AnimationObject2D},
 * {@link org.bzdev.drama.generic.GenericSimObject} and
 * {@link org.bzdev.drama.DramaSimObject}.  This class provides such
 * a base class for the devqsim package itself.
 */
abstract public class DefaultSimObject extends SimObject {
    /**
     * Get the simulation.
     * @return the simulation
     */
    protected final Simulation getSimulation() {
	return getSimulationAsSimulation();
    }

    /**
     * Constructor.
     * These objects can be looked up by name using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    protected DefaultSimObject(Simulation sim, String name,
			       boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
    }

    /**
     * Print this simulation object's configuration.
     * Documentation for the use of this method is provided by the documentation
     * for the {@link SimObject} method
     * {@link SimObject#printConfiguration(String,String,boolean,PrintWriter)}.
     * <P>
     * When the third argument has a value of true, the object name and
     * class name will be printed in a standard format with its
     * indentation provided by the iPrefix argument.
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
    }

    /**
     * Print this simulation object's state.
     * Documentation for the use of this method is provided by the documentation
     * for the {@link SimObject} method
     * {@link SimObject#printState(String,String,boolean,PrintWriter)}.
     * <P>
     * When the second argument has a value of true, the object name and
     * class name will be printed in a standard format with its
     * indentation provided by the iPrefix argument.
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
    }
}

//  LocalWords:  DefaultSimObject sim IllegalArgumentException
//  LocalWords:  getObject SimObject printConfiguration boolean
//  LocalWords:  PrintWriter printName printState
