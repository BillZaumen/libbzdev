package org.bzdev.devqsim.rv;
import java.util.Spliterator;
import java.util.stream.IntStream;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.IntegerRandomVariable;

/**
 * Base class for named objects representing random variables that
 * generate integer values.
 * The type parameter RV is the type of the random variable being named.
 */
public class SimIntegerRV<RV extends IntegerRandomVariable>
    extends SimRandomVariable<Integer,RV>
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
    protected SimIntegerRV(Simulation sim, String name, boolean intern) {
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
    public SimIntegerRV(Simulation sim, String name, boolean intern, RV rv)
    {
	super(sim, name, intern, rv);
    }

    /**
     * Get a spliterator for a specified number of values.
     * @param size the number of random values to provide
     * @return the spliterator
     */
    public Spliterator.OfInt spliterator(long size) {
	return ((RV) rv).spliterator(size);
    }

    /**
     * Get a spliterator for an infinite number of values.
     * @return the spliterator
     */
    public Spliterator.OfInt spliterator() {
	return ((RV) rv).spliterator();
    }

    /**
     * Get a fixed-length stream of integer values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public IntStream stream(long size) {
	return getRandomVariable().stream(size);
    }

    /*
     * Get a fixed-length parallel stream of integer values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public IntStream parallelStream(long size) {
	return getRandomVariable().parallelStream(size);
    }

    /*
     * Get an infinie stream of integer values.
     * @return the stream
     */
    public IntStream stream() {
	return getRandomVariable().stream();
    }

    /*
     * Get an infinie parallel stream of integer values.
     * @return the stream
     */
    public IntStream parallelStream() {
	return getRandomVariable().parallelStream();
    }
}

//  LocalWords:  IllegalArgumentException getObject rv
