package org.bzdev.devqsim.rv;
import java.util.stream.Stream;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BooleanRandomVariable;

/**
 * Base class for named objects representing random variables that
 * generate boolean values.
 * The type parameter RV is the type of the random variable being named.
 */
public class SimBooleanRV<RV extends BooleanRandomVariable>
    extends SimRandomVariable<Boolean,RV>
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
    protected SimBooleanRV(Simulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Constructor given a random variable.
     * @param sim the simulation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * @param rv the random variable itself
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public SimBooleanRV(Simulation sim, String name, boolean intern, RV rv)
    {
	super(sim, name, intern, rv);
    }

    /**
     * Get a fixed-length stream of boolean values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<Boolean> stream(long size) {
	return getRandomVariable().stream(size);
    }

    /**
     * Get a fixed-length parallel stream of boolean values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<Boolean> parallelStream(long size) {
	return getRandomVariable().parallelStream(size);
    }

    /**
     * Get an infinie stream of boolean values.
     * @return the stream
     */
    public Stream<Boolean> stream() {
	return getRandomVariable().stream();
    }

    /**
     * Get an infinie parallel stream of boolean values.
     * @return the stream
     */
    public Stream<Boolean> parallelStream() {
	return getRandomVariable().parallelStream();
    }

}

//  LocalWords:  IllegalArgumentException getObject rv
