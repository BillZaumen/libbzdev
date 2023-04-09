package org.bzdev.devqsim.rv;
import java.util.stream.Stream;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.RandomVariable;
import org.bzdev.math.rv.RandomVariableRV;
import org.bzdev.math.rv.RandomVariableRVOps;

/**
 * Base class for named random variables that produce random variables
 * that in turn produce a sequence of values of type
 * {@link org.bzdev.math.rv.RandomVariable}.  The type parameters are:
 * <UL>
 *   <LI> T - the type of the values that a generated random variable
 *        generate.
 *   <LI> RV - the type for the random variables generated.
 *   <LI> RVRV - the type of the random variable used to generate a
 *        sequence of random variables.
 * </UL>
 */
public abstract class SimRandomVariableRV
    <T,RV extends RandomVariable<T>,RVRV extends RandomVariableRV<T,RV>>
    extends SimRandomVariable<RV,RVRV>
{
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    protected SimRandomVariableRV(Simulation sim, String name,
				   boolean intern) {
	super(sim, name, intern);
    }

   /**
     * Constructor given a random variable.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * @param rvrv the random variable itself
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
     protected SimRandomVariableRV(Simulation sim, String name, boolean intern,
				RVRV rvrv)
    {
	super(sim, name, intern, rvrv);
    }

    /**
     * Get a fixed-length stream of random variables.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<RV> stream(long size) {
	return getRandomVariable().stream(size);
    }

    /**
     * Get a fixed-length parallel stream of random variables.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<RV> parallelStream(long size) {
	return getRandomVariable().parallelStream(size);
    }

    /**
     * Get an infinie stream of random variables.
     * @return the stream
     */
    public Stream<RV> stream() {
	return getRandomVariable().stream();
    }

    /**
     * Get an infinie parallel stream of random variables.
     * @return the stream
     */
    public Stream<RV> parallelStream() {
	return getRandomVariable().parallelStream();
    }
}

//  LocalWords:  RVRV IllegalArgumentException getObject rvrv
