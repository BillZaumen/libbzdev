package org.bzdev.geom;

/**
 * This interface provides several constants used
 * internally for determining when an operation can
 * be performed in parallel.
 */
interface SurfaceConstants {
    /**
     * Minimum number of surface elements for computing
     * an surface integral in parallel.
     * This parameter is set so that the speed up for
     * doing the computation in parallel is large enough
     * to cover the costs of starting multiple threads
     * and waiting for them to complete.
     */
    static final int MIN_PARALLEL_SIZE_A = 1024;
    /**
     * Minimum number of surface elements for computing
     * a volume integral in parallel.
     * This parameter is set so that the speed up for
     * doing the computation in parallel is large enough
     * to cover the costs of starting multiple threads
     * and waiting for them to complete.
     */
    static final int MIN_PARALLEL_SIZE_V = 256;
    /**
     * Minimum number of surface elements for computing
     * the center of mass of an object bounded by a surface
     * in parallel.
     * This parameter is set so that the speed up for
     * doing the computation in parallel is large enough
     * to cover the costs of starting multiple threads
     * and waiting for them to complete.
     */
    static final int MIN_PARALLEL_SIZE_CM = 192;
    /**
     * Minimum number of surface elements for computing
     * the moments of an object bounded by a surface
     * in parallel.
     * This parameter is set so that the speed up for
     * doing the computation in parallel is large enough
     * to cover the costs of starting multiple threads
     * and waiting for them to complete.
     */
    static final int MIN_PARALLEL_SIZE_M = 128;

    /**
     * Minimum size to consider parallel execution when the number of
     * elements for a surface is known.
     */
    static final int MIN_PARALLEL_SIZE = 128;

    /**
     * Maximum absolute value for principal-axes components (if
     * less than this, assume zero).
     */
    static final double EPS2 = Math.scalb(1.0, -52/2);
}
