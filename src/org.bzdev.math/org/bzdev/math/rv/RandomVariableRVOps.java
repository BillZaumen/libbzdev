package org.bzdev.math.rv;
/**
 * Interface for Random variable that generates another random variable.
 * This interface provides type constraints used by interfaces that
 * extend this interface.
 */
public interface RandomVariableRVOps<T, RV extends RandomVariableOps<T>> 
    extends RandomVariableOps<RV> 
{
}
