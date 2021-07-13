package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformBooleanRV;



/**
 * Factory for creating instances of SimUniformBoolRV.
 * <P>
 * This factory has no parameters.
 * @see SimUniformBooleanRV
 */
public class SimUniformBoolRVFactory
    extends SimBooleanRVFactory<UniformBooleanRV,SimUniformBooleanRV>
{

    private Simulation sim;

    /**
     * Constructor.
     * @param sim the simulation
     */
    public SimUniformBoolRVFactory(Simulation sim) {
	super(sim);
	this.sim = sim;
    }

    /**
     * Constructor for a service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is used by
     * a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public SimUniformBoolRVFactory() {
	super(null);
    }

    // We just need one: it has nothing to configure.
    private static UniformBooleanRV rv = new UniformBooleanRV();

    /**
     * Construct a new object.
     * Please see
     * {@link org.bzdev.obnaming.NamedObjectFactory#newObject(String)}
     * for details.
     * @param name the name of the object to be created
     * @return the new object
    */
    @Override
    protected SimUniformBooleanRV newObject(String name) {
        return new SimUniformBooleanRV(sim, name, willIntern(), rv);
    }
}

//  LocalWords:  SimUniformBoolRV SimUniformBooleanRV newObject
