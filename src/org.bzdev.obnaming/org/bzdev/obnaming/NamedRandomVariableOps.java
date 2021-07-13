package org.bzdev.obnaming;
import org.bzdev.math.rv.RandomVariable;
import org.bzdev.math.rv.RandomVariableOps;
import org.bzdev.math.rv.RandomVariableException;

/**
 * Interface for named objects that provide a random variable.
 *
 */
public interface NamedRandomVariableOps<T, RV extends RandomVariable<T>>
    extends RandomVariableOps<T>, NamedObjectOps
{
    /**
     * Get the random variable that this class names.
     * @return the random variable
     */
    RV getRandomVariable();

    @Override
    default void setMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	getRandomVariable().setMinimum(min, closed);
    }

    @Override
    default void tightenMinimum(T min, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException {
	getRandomVariable().tightenMinimum(min, closed);
    }

        @Override
    default void tightenMinimumS(String min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	getRandomVariable().tightenMinimumS(min, closed);
    }

    @Override
    default void setMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	getRandomVariable().setMaximum(max, closed);
    }

    @Override
   default void tightenMaximum(T max, boolean closed) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	getRandomVariable().tightenMaximum(max, closed);
    }

    @Override
    default void tightenMaximumS(String max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	getRandomVariable().tightenMaximumS(max, closed);
    }

    @Override
    default T next() throws RandomVariableException {
	return getRandomVariable().next();
    }

    @Override 
    default T getMinimum()
    {
	return getRandomVariable().getMinimum();
    }
    @Override
    default Boolean getMinimumClosed()
    {
	return getRandomVariable().getMinimumClosed();
    }

    @Override
    default T getMaximum()
    {
	return getRandomVariable().getMaximum();
    }

    @Override
    default Boolean getMaximumClosed()
    {
	return getRandomVariable().getMaximumClosed();
    }

}
