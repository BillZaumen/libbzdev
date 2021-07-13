package org.bzdev.drama.generic;
import org.bzdev.drama.common.*;
import org.bzdev.devqsim.*;
import javax.script.ScriptException;
import java.io.PrintWriter;

//@exbundle org.bzdev.drama.generic.lpack.GenericSimulation

/**
 * Base class for simulation objects using generics.
 * Because of simulation flavors, each flavor is associated
 * with a different subclass of GenericSimulation. With the
 * use of generics, various methods (e.g., getSimulation())
 * will return the specific subclass of GenericSimulation that
 * a flavor uses.  This reduces the need to use type casts.
 * <p>
 * Typically, the subclasses of GenericSimulation will provide
 * methods to look up specific types of objects by name and provide
 * flavor-specific operations.
 * <p>
 * The GenericSimObject class merely provides a constructor
 * and a method for obtaining the current simulation (returning the
 * subclass of GenericSimulation appropriate for a specific simulation
 * flavor). Other operations are left to its subclasses.
 */
abstract public class GenericSimObject<
    S extends GenericSimulation<S,A,C,D,DM,F,G>,
    A extends GenericActor<S,A,C,D,DM,F,G>,
    C extends GenericCondition<S,A,C,D,DM,F,G>,
    D extends GenericDomain<S,A,C,D,DM,F,G>,
    DM extends GenericDomainMember<S,A,C,D,DM,F,G>,
    F extends GenericFactory<S,A,C,D,DM,F,G>,
    G extends GenericGroup<S,A,C,D,DM,F,G>>
    extends SimObject
{
    private static String errorMsg(String key, Object... args) {
	return GenericSimulation.errorMsg(key, args);
    }

    S simulation;

    /**
     * Get the simulation.
     * @return the simulation associated with this object
     */
    protected final S getSimulation() {
	return simulation;
    }


    /**
     * Constructor.
     * When interned, these objects can be looked up by name using the
     * methods in {@link org.bzdev.devqsim.Simulation Simulation}.  For
     * each type of simulation object that a GUI or input-file parser
     * will support, the base class for that type must provide its
     * class name, and should provide a constructor that requires the
     * sim, name, and intern arguments only. Additional initialization
     * should be done separately.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.devqsim.Simulation Simulation}; false
     *        otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    protected GenericSimObject(S sim, String name,
				boolean intern)
	throws IllegalArgumentException
    {
	super(sim, name, intern);
	simulation = sim;
    }

    /**
     * Print this simulation object's configuration.
     * Documentation for the use of this method is provided by the documentation
     * for the {@link SimObject} method
     * {@link SimObject#printConfiguration(String,String,boolean,PrintWriter)}.
     * When the thirs argument has a value of true, the object name and
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
     * When the third argument has a value of true, the object name and
     * class name will be printed in a standard format with its
     * indentation provided by the iPrefix argument.
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix,
			   boolean printName,
			   PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
    }
}
