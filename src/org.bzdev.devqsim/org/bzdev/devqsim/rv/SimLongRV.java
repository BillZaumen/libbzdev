package org.bzdev.devqsim.rv;
import java.util.Spliterator;
import java.util.stream.LongStream;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;

/**
 * Base class for named objects representing random variables that
 * generate long integer values.
 * The type parameter RV is the type of the random variable being named.
 */
public class SimLongRV<RV extends LongRandomVariable>
    extends SimRandomVariable<Long,RV>
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
    protected SimLongRV(Simulation sim, String name, boolean intern) {
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
    public SimLongRV(Simulation sim, String name, boolean intern, RV rv)
    {
	super(sim, name, intern, rv);
    }

    /**
     * Get a spliterator for a specified number of values.
     * @param size the number of random values to provide
     * @return the spliterator
     */
    public Spliterator.OfLong spliterator(long size) {
	return ((RV) rv).spliterator(size);
    }

    /**
     * Get a spliterator for an infinite number of values.
     * @return the spliterator
     */
    public Spliterator.OfLong spliterator() {
	return ((RV) rv).spliterator();
    }

    /**
     * Get a fixed-length stream of boolean values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public LongStream stream(long size) {
	return getRandomVariable().stream(size);
    }

    /*
     * Get a fixed-length parallel stream of long values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public LongStream parallelStream(long size) {
	return getRandomVariable().parallelStream(size);
    }

    /*
     * Get an infinie stream of long values.
     * @return the stream
     */
    public LongStream stream() {
	return getRandomVariable().stream();
    }

    /*
     * Get an infinie parallel stream of long values.
     * @return the stream
     */
    public LongStream parallelStream() {
	return getRandomVariable().parallelStream();
    }

}

//  LocalWords:  IllegalArgumentException getObject rv
