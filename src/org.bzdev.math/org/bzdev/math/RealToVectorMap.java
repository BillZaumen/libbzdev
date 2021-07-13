package org.bzdev.math;

/**
 * A mapping from a double-precision number to a vector
 * of double-precision numbers.
 * <P>
 * This interface is used by the class {@link VectorValuedGLQ}.
 */
@FunctionalInterface
public interface RealToVectorMap {
    /**
     * Apply a mapping to an argument u in order to produce
     * multiple, integer-indexed, double-precision values.
     * @param results the results of the mapping, stored in
     *        an array whose length is at least m, starting
     *        at index 0.
     * @param m the number of values produced
     * @param u the input value
     */
    void apply(double[] results, int m, double u);
}

//  LocalWords:  VectorValuedGLQ
