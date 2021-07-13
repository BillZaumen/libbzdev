package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bzdev.math.CholeskyDecomp;
import org.bzdev.math.stats.CovarianceMatrix;
import org.bzdev.math.StaticRandom;
import org.bzdev.lang.MathOps;
import org.bzdev.lang.UnexpectedExceptionError;

/**
 * Create a correlated set of Gaussian random variables.
 * This class provides a method {@link GaussianRVs#next(double[])}
 * that sets n elements of an array to correlated random values
 * with specified means.
 * <P>
 * The covariance matrix's rows and columns use the same order as
 * used in the array that is set.
 * Given a covariance matrix cov and a vector means containing the
 * mean values, calling {@link GaussianRVs#next(double[])} will
 * create a sequence of vectors, all with the same length, such
 * that (after a very large number of iterations)
 * <UL>
 *   <LI> the mean value of the i<sup>th</sup> element of each
 *        vector will be the  i<sup>th</sup> element in the factor
 *        passed to the constructor.
 *   <LI> the covariance matrix for the vectors produced will be
 *        equal to the covariance matrix used by the constructor.
 * </UL>
 * <P>
 * This class is useful for Monte Carlo models or simulations when
 * random numbers are not mutually independent.
 */
public class GaussianRVs {
    double[][] L;
    double[] rvs;
    double[] means;
    int n;

    /**
     * Constructor.
     * @param cov the covariance matrix.
     */
    public GaussianRVs(CovarianceMatrix cov) {
	this(cov.getMatrix(), cov.getMeans());
    }

    /**
     * Constructor, specifying the covariance matrix by a two-dimensional
     * array.
     * @param cov the covariance matrix for the set of random variables to
     *        be created
     * @param means the mean values for each of the set of random variables
     */
    public GaussianRVs(double[][]cov, double[] means) {
	if (cov == null || means == null) {
	    throw new IllegalArgumentException();
	}
	try {
	    this.means = means.clone();
	} catch (Exception e) {
	    throw new UnexpectedExceptionError(e);
	}
	n = means.length;
	L = new CholeskyDecomp(cov, n, true).getL();
	rvs = new double[means.length];
    }


    /**
     * Constructor specifying the matrix size.
     * The first n rows and columns of the covariance matrix, and the
     * first n elements of the means array will be used.
     * @param cov the covariance matrix for the set of random variables to
     *        be created.
     * @param means the mean values for each of the set of random variables
     * @param n the number of rows and columns of cov and mean that are used
     */
    public GaussianRVs(double[][]cov, double[] means, int n) {
	if (n < 0) throw new IllegalArgumentException();
	if (cov == null || means == null) {
	    throw new IllegalArgumentException();
	}
	try {
	    this.means = new double[n];
	    System.arraycopy(means, 0, this.means, 0, n);
	} catch (Exception e) {
	    throw new UnexpectedExceptionError(e);
	}
	this.n = n;
	L = new CholeskyDecomp(cov, n, false).getL();
	rvs = new double[means.length];
    }

    /**
     * Get the number of random-variables that will be set
     * when the method {@link #next(double[])} is called.
     * @return the number of values that will be set when next(double[]) is
     *         called
     */
    public int length() {
	return n;
    }

    /**
     * Get the next set of values.
     * @param values an array to hold the results
     */
    public void next(double[] values) {
	int n = means.length;
	for (int i = 0; i < n; i++) {
	    rvs[i] = StaticRandom.nextGaussian();
	    values[i] = means[i];
	}
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j <= i; j++) {
		values[i] += L[i][j]*rvs[j];
	    }
	}
    }

    /**
     * Get an array containing the next set of values.
     * @return an array holding the next values
     */
    public double[] next() {
	double[] results = new double[n];
	next(results);
	return results;
    }

    /**
     * Spliterator characteristics.
     * The characteristics returned should not include
     * {@link Spliterator#SIZED} or {@link Spliterator#SUBSIZED}.
     * The  default value is
     * {@link Spliterator#IMMUTABLE} | {@link Spliterator#NONNULL}.
     * If the characteristics include {@link Spliterator#ORDERED},
     * {@link Spliterator#trySplit()} will return null.
     * @return the characteristics
     */
    protected int getCharacteristics() {
	return Spliterator.IMMUTABLE | Spliterator.NONNULL;
    }

    private static Runtime runtime = Runtime.getRuntime();

    /**
     * Get a spliterator for a specified number of values.
     * @param size the number of random values to provide
     * @return the spliterator
     */
    public Spliterator<double[]> spliterator(long size) {
	int maxdepth = (int)
	    Math.round(MathOps.log2(runtime.availableProcessors(), 1.0));
	return spliteratorAux(size, maxdepth);
    }

    private Spliterator<double[]> spliteratorAux(long size, int maxDepth) {
	return new Spliterator<double[]>() {
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
	    public boolean tryAdvance(Consumer<? super double[]> action) {
		if (count < size) {
		    count++;
		    action.accept(next());
		    return true;
		} else {
		    return false;
		}
	    }
	    @Override
	    public Spliterator<double[]> trySplit() {
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
    public Spliterator<double[]> spliterator() {
	int maxdepth = (int)
	    Math.round(MathOps.log2(runtime.availableProcessors(), 1.0));
	return spliteratorAux(maxdepth);
    }

    public Spliterator<double[]> spliteratorAux(final int maxDepth) {
	return new Spliterator<double[]>() {
	    int  maxdepth = maxDepth;
	    int characteristics = getCharacteristics()
		& ~(Spliterator.SIZED | Spliterator.SUBSIZED);
	    @Override
	    public int characteristics() {
		return characteristics;
	    }
	    @Override
	    public long estimateSize() {return Long.MAX_VALUE;}

	    @Override
	    public boolean tryAdvance(Consumer<? super double[]> action) {
		action.accept(next());
		return true;
	    }
	    @Override
	    public Spliterator<double[]> trySplit() {
		if ((characteristics & Spliterator.ORDERED) != 0
		    || maxdepth == 0) return null;
		maxdepth--;
		return spliteratorAux(maxdepth);
	    }
	};
    }

    /**
     * Get a fixed-length stream of double-valued arrays.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<double[]> stream(long size) {
	return StreamSupport.stream(spliterator(size), false);
    }

    /*
     * Get a fixed-length parallel stream of double-valued arrays.
     * @param size the number of random values to provide
     * @return the stream
     */
    public Stream<double[]> parallelStream(long size) {
	return StreamSupport.stream(spliterator(size), true);
    }

    /*
     * Get an infinie stream of double-valued arrays.
     * @return the stream
     */
    public Stream<double[]> stream() {
	return StreamSupport.stream(spliterator(), false);
    }

    /*
     * Get an infinie parallel stream of double-valued arrays.
     * @return the stream
     */
    public Stream<double[]> parallelStream() {
	return StreamSupport.stream(spliterator(), true);
    }
}
//  LocalWords:  GaussianRVs cov th
