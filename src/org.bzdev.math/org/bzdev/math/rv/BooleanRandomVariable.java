package org.bzdev.math.rv;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base class for Boolean-valued random numbers.
 * The setMinimum, setMaximum, setRequiredMinimum, setRequiredMaximum,
 * tightenMinimum, tightenMaximum,, tightenMinimumS, and
 * tightenMaximumS methods from the RandomVariable interface throw an
 * UnsupportedOperationException as boolean values are not ordered.
 */

abstract public class BooleanRandomVariable extends RandomVariable<Boolean> 
{
    public void tightenMinimumS(String s, boolean closed)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }

    public void tightenMaximumS(String s, boolean closed)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Get a fixed-length stream of boolean values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<Boolean> stream(long size) {
	return StreamSupport.stream(spliterator(size), false);
    }

    /**
     * Get a fixed-length parallel stream of boolean values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<Boolean> parallelStream(long size) {
	return StreamSupport.stream(spliterator(size), true);
    }

    /**
     * Get an infinite stream of boolean values.
     * @return the stream
     */
    public Stream<Boolean> stream() {
	return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Get an infinite parallel stream of boolean values.
     * @return the stream
     */
    public Stream<Boolean> parallelStream() {
	return StreamSupport.stream(spliterator(), true);
    }

}

//  LocalWords:  setMinimum setMaximum setRequiredMinimum boolean
//  LocalWords:  setRequiredMaximum tightenMinimum tightenMaximum
//  LocalWords:  tightenMinimumS tightenMaximumS RandomVariable
//  LocalWords:  UnsupportedOperationException
