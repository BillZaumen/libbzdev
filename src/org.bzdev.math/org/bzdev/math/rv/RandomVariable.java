package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.bzdev.lang.MathOps;
import org.bzdev.math.StaticRandom;


/**
 * Random Variable base class.
 * The type parameter T indicates the type of the values that the
 * random number generator generates. Methods allow one to set minimum
 * and maximum values when there is an ordering for the type T. This is
 * useful in cases where one might (for example) compute the square root
 * of a number returned by the random number generator.  If a minimum or
 * maximum value is set, values will be generated repeatedly until a value
 * that matches the constraints is obtained.
 * <P>
 * The interface {@link RandomVariableOps} is part of a parallel
 * hierarchy provided so for classes that implement a random variable
 * but that cannot be subclasses of {@link RandomVariable}.
 */
abstract public class  RandomVariable<T>
    implements RandomVariableOps<T>, Cloneable
{
    /**
     * Set the minimum value for a random variable.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void setMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException 
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Set a required minimum value for a random variable.
     * Normally this is called by a constructor to prevent a random
     * variable from having values outside a range that makes sense.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    protected void setRequiredMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException 
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Tighten the minimum value for a random variable.
     * If there is no minimum value, it will be set.  Otherwise the
     * minimum of the allowed range will not decrease.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void tightenMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException 
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Tighten the minimum value for a random variable given a string.
     * If there is no minimum value, it will be set.  Otherwise the
     * minimum of the allowed range will not decrease. The string
     * argument is a number in a format acceptable to the constructors
     * for Integer, Long, or Double as appropriate.
     * @param min minimum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    abstract public void tightenMinimumS(String min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;


    /**
     * Set the maximum value for a random variable.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void setMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Set the required maximum value for a random variable.
     * Normally this is called by a constructor to prevent a random
     * variable from having values outside a range that makes sense.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    protected void setRequiredMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Tighten the maximum value for a random variable.
     * If there is no maximum value, it will be set.  Otherwise the
     * maximum of the allowed range will not increase.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    public void tightenMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Tighten the maximum value for a random variable  given a string.
     * If there is no maximum value, it will be set.  Otherwise the
     * maximum of the allowed range will not increase. The string
     * argument is a number in a format acceptable to the constructors
     * for Integer, Long, or Double as appropriate.
     * @param max maximum value.
     * @param closed true if the minimum value is part of the range;
     *        false if it is a lower bound on the range
     * @throws UnsupportedOperationException the random variable's type does
     *         not have an order imposed on it or the operation is not
     *         supported for implementation reasons
     * @throws IllegalArgumentException an argument is out of range
     */
    abstract public void tightenMaximumS(String max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException;


    /**
     * Get the next value for a random variable.
     * In general, each value will be independent of the last.
     * @return the next value
     * @exception the next value could not be generated
     */
    abstract public T next() throws RandomVariableException;

    /**
     * Get the lower bound on the values that can be generated.
     * Some random numbers do not have an ordering, so that null
     * will always be returned in that case.
     * @return the lower bound; null if there is none
     */
    public T getMinimum() {
	return null;
    }

    /**
     * Determine if a random variable's lower bound can be generated.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    public Boolean getMinimumClosed() {
	return null;
    }

    /**
     * Get the upper bound on the values that can be generated.
     * Some random numbers do not have an ordering, so that null
     * will always be returned in that case.
     * @return the upper bound; null if there is none
     */
    public T getMaximum() {
	return null;
    }

    /**
     * Determine if a random variable's upper bound can be generated.
     * @return true if it can be generated; false if it cannot be generated;
     *         null if this cannot be determined
     */
    public Boolean getMaximumClosed() {
	return null;
    }

    public Object clone() throws CloneNotSupportedException {
	return super.clone();
    }

    /**
     * Spliterator characteristics.
     * The characteristics returned should not include
     * {@link Spliterator#SIZED} or {@link Spliterator#SUBSIZED}.
     * The  default value is
     * {@link Spliterator#IMMUTABLE} | {@link Spliterator#NONNULL}.
     * If the characteristics include {@link Spliterator#ORDERED},
     * {@link Spliterator#trySplit()} will return null. The
     * characteristics include {@link Spliterator#ORDERED} for
     * the random variables in this package that return a deterministic
     * sequence of values.
     * @return the characteristics
     */
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL;
    }

    /*
    // minimum size for splitting. The value depends on whether
    // the random number generator is a low quality or high quality one.
    //
    static final long INCR_HQ = (1 << 15);
    static final long INCR_LQ = (1 << 16);

    static final long getIncrement() {
	return StaticRandom.isHighQuality()? INCR_HQ: INCR_LQ;
    }
    */

    private static Runtime runtime = Runtime.getRuntime();

    /**
     * Get a spliterator for a specified number of values.
     * @param size the number of random values to provide
     * @return the spliterator
     */
    public Spliterator<T> spliterator(long size) {
	int maxdepth = (int)
	    Math.round(MathOps.log2(runtime.availableProcessors(), 1.0));
	return spliteratorAux(size, maxdepth);
    }

    private Spliterator<T> spliteratorAux(long size, final int maxDepth) {
	return new Spliterator<T>() {
	    int maxdepth = maxDepth;
	    long count = 0;
	    int characteristics = getCharacteristics()
		| Spliterator.SIZED | Spliterator.SUBSIZED;
	    @Override
	    public int characteristics() {
		return characteristics;
	    }
	    @Override
	    public long estimateSize() {return size;}

	    @Override
	    public boolean tryAdvance(Consumer<? super T> action) {
		if (count < size) {
		    count++;
		    action.accept(next());
		    return true;
		} else {
		    return false;
		}
	    }
	    @Override
	    public Spliterator<T> trySplit() {
		if ((characteristics & Spliterator.ORDERED) != 0) return null;
		long newsize = (size - count) /2;
		if (newsize <= 0 /*getIncrement()*/ || maxdepth == 0) {
		    return null;
		} else {
		    count += newsize;
		    maxdepth--;
		    return spliteratorAux(newsize, maxdepth);
		}
	    }
	};
    }

    /**
     * Get a spliterator for an infinite number of values.
     * @return the spliterator
     */
    public Spliterator<T> spliterator() {
	int maxdepth = (int)
	    Math.round(MathOps.log2(runtime.availableProcessors(), 1.0));
	return spliteratorAux(maxdepth);
    }

    Spliterator<T> spliteratorAux(final int maxDepth) {
	return new Spliterator<T>() {
	    int maxdepth = maxDepth;
	    int characteristics = getCharacteristics()
		& ~(Spliterator.SIZED | Spliterator.SUBSIZED);
	    @Override
	    public int characteristics() {
		return characteristics;
	    }
	    @Override
	    public long estimateSize() {return Long.MAX_VALUE;}

	    @Override
	    public boolean tryAdvance(Consumer<? super T> action) {
		action.accept(next());
		return true;
	    }
	    @Override
	    public Spliterator<T> trySplit() {
		if ((characteristics & Spliterator.ORDERED) != 0
		    || maxdepth == 0) return null;
		maxdepth--;
		return spliteratorAux(maxdepth);
	    }
	};
    }
}

//  LocalWords:  RandomVariableOps subclasses RandomVariable SUBSIZED
//  LocalWords:  UnsupportedOperationException Spliterator NONNULL LQ
//  LocalWords:  IllegalArgumentException trySplit INCR getIncrement
//  LocalWords:  StaticRandom isHighQuality spliterator
