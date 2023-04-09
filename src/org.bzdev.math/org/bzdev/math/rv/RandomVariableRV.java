package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Random variable that generates another random variable.
 * This class provides a superclass for random variables that generate
 * a random variable of some particular type. The random variable RV that
 * an instance of this class generates is a random variable that
 * generates values of type T. The use of two type parameters, T and RV,
 * allows both to be used - some subclasses require protected methods
 * that are cognizant of the type T (for example, a protected method
 * named "cmp" used by {@link RandomVariableRVN RandomVariableRVN}.
 * The class does nothing other than providing a type constraint.
 * <P>
 * The interface {@link RandomVariableRVOps} corresponds to this class.
 */
abstract public class RandomVariableRV<T, RV extends RandomVariable<T>> 
    extends RandomVariable<RV> implements RandomVariableRVOps<T, RV>
{

    boolean ordered = false;

    /**
     * Determine if this random variable's streams must be ordered.
     * Subclasses typically use random variables to generate a sequence
     * of random variables, and If any of the random variables used are
     * ordered, this random variable must also be ordered.
     * <P>
     * A subclass's constructor should call this method if any of
     * its arguments is a random variable unless that subclass overrides
     * {@link RandomVariable#getCharacteristics()}.
     * @param args the random variables to check
     * @see RandomVariable#getCharacteristics()
     */
    protected void determineIfOrdered(RandomVariable... args) {
	for (RandomVariable rv: args) {
	    if (rv == null) continue;
	    if ((rv.getCharacteristics() | Spliterator.ORDERED) != 0) {
		ordered = true;
		return;
	    }
	}
	ordered = false;
    }

    @Override
    protected int getCharacteristics() {
	int result = super.getCharacteristics();
	return ordered? (result | Spliterator.ORDERED): result;
    }


    /**
     * Get a fixed-length stream of random variables.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<RV> stream(long size) {
	return StreamSupport.stream(spliterator(size), false);
    }

    /**
     * Get a fixed-length parallel stream of random variables.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<RV> parallelStream(long size) {
	return StreamSupport.stream(spliterator(size), true);
    }

    /**
     * Get an infinite stream of random variables.
     * @return the stream
     */
    public Stream<RV> stream() {
	return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Get an infinite parallel stream of random variables.
     * @return the stream
     */
    public Stream<RV> parallelStream() {
	return StreamSupport.stream(spliterator(), true);
    }
}

//  LocalWords:  superclass subclasses cmp RandomVariableRVN args
//  LocalWords:  RandomVariableRVOps subclass's RandomVariable
//  LocalWords:  getCharacteristics
