package org.bzdev.math;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class representing a summation.
 * Subclasses provide various algorithms.
 * <P>
 * The naive algorithm for summing a series of values is subject
 * to relatively large floating-point errors when a large number of
 * values are summed.  This class provides summation algorithms that
 * substantially reduce errors. Multiple algorithms are provided to
 * allow accuracy to be traded off for performance.
 */
public abstract class Adder {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    Adder() {}

    /**
     * Add the elements of an array to the summation.
     * @param array elements to add to the sum
     */
    public abstract void add(double[] array);

    /**
     * Add the elements of an array to the summation, specifying an offset
     * into the array and the number of elements to process.
     * The indices of the elements added are members of the interval
     * [start,end).
     * @param array the array of elements to sum
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @exception IllegalArgumentException the start and end
     *            values are not consistent with the array's size
     *            or are out of range or out of order
     */
    public abstract void add(double[] array, int start, int end)
	throws IllegalArgumentException;

    /**
     * Add a single number to the summation.
     * @param value the value to add
     */
    public abstract void add(double value);


    /**
     * Add the values produced by an Iterable to the summation.
     * This method will add the elements of any object that can
     * be used in a 'foreach' statement (the "for (TYPE value: object)"
     * syntax) when TYPE is a subclass of Number (e.g., Double, Float,
     * etc.)
     * @param iterable the Iterable whose values will be summed
     */
    public abstract void add(Iterable<? extends Number> iterable);

    /**
     * Get the sum.
     * Additional values can be subsequently added.
     * @return the sum
     */
    public abstract double getSum();

    /**
     * Reset the class to sum a new set of values.
     */
    public abstract void reset();

    /**
     * Add a series of values using the Kahan summation algorithm.
     * A description of the algorithm can be found in a
     * <A HREF="https://en.wikipedia.org/wiki/Kahan_summation_algorithm">
     * Wikipedia article</A>.
     * This class is intended for cases were additional numerical accuracy
     * better than summing the elements of a vector in the order in which they
     * appear is needed. Accuracy is higher than when the Pairwise class is
     * used, at the cost of some additional computation per element being
     * summed.
     * <P>
     * The documentation for the class {@link Adder.Kahan.State} describes
     * how to use the Kahan summation algorithm directly (e.g., to avoid
     * the overhead of some method calls).
     */
    public static final class Kahan extends Adder {

	/**
	 * Encapsulate the state of an instance of Adder.Kahan.
	 * This class is used when, for maximal efficiency, one
	 * wishes to in-line code Kahan's algorithm for some
	 * critical steps (e.g., an innermost loop).  In most
	 * cases, one should just use the adder's methods to reduce
	 * the chances of a programming error. The use of this class
	 * may be warranted when one does not want to depend on a
	 * JIT compiler in-line coding calls to
	 * {@link Adder.Kahan#add(double)}.
	 * <P>
	 * One will use {@link Adder.Kahan#getState()} to obtain
	 * the adder's state and then in-line code Kahan's algorithm
	 * to update the state of the adder. For example,
	 * <blockquote><pre>
	 *    Adder.Kahan adder = new Adder.Kahan();
	 *    ...
	 *    Adder.Kahan.State state = adder.getState();
	 *    while (...) {
	 *       double term = ...
	 *       double y = term - state.c;
	 *       double t = state.total + y;
	 *       state.c = (t - state.total) - y;
	 *       state.total = t;
	 *       ...
	 *    }
	 * </pre></blockquote>
	 * To retrieve the total when done, just read the variable
	 * <code>state.total</code>.
	 * As a reminder, use parentheses as shown above as the
	 * order of evaluation is important for Kahan's summation algorithm.
	 * Alternatively, once can create just the adder's state:
	 * <blockquote><pre>
	 *    Adder.Kahan.State state = new Adder.Kahan.State();
	 *    while (...) {
	 *       double term = ...
	 *       double y = term - state.c;
	 *       double t = state.total + y;
	 *       state.c = (t - state.total) - y;
	 *       state.total = t;
	 *       ...
	 *    }
	 * </pre></blockquote>
	 * To reset the state when a Kahan adder's state is constructed
	 * instead of the adder itself, set the <code>c</code>
	 * and <code>total</code> fields to 0.0:
	 * <blockquote><pre>
	 *    state.c = 0.0;
	 *    state.total = 0.0;
	 * </pre></blockquote>
	 *<P>
	 * One can alternatively create two variables, "c" and "total"
	 * for example, and use those directly.
	 */
	public static final class State {
	    /**
	     * The total value summed.
	     */
	    public double total = 0.0;
	    /**
	     * The correction term.
	     */
	    public double c = 0.0;

	    /**
	     * Constructor.
	     */
	    public State() {}
	}

	private final Adder.Kahan.State state = new State();
	// double total = 0.0;
	// double c = 0.0;
    
	/**
	 * Get the state of this adder.
	 * @return this adder's state
	 * @see Adder.Kahan.State
	 */
	public State getState() {
	    return state;
	}

	// Test indicated that use of volatile was not needed to prevent
	// unwanted optimizations.
	private double t = 0.0;

	/**
	 * Constructor.
	 */
	public Kahan() {super();}

	@Override
	public void add(double[] array) {
	    double y;
	    for (int i = 0; i < array.length; i++) {
		y = array[i] - state.c;
		t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	}

	@Override
	public void add(double[] array, int start, int end) {
	    if (end < start || start < 0 || end > array.length) {
		String msg = errorMsg("argsOutOfRange2", start, end);
		throw new IllegalArgumentException(msg);
	    }
	    double y;
	    for (int i = start; i < end; i++) {
		y = array[i] - state.c;
		t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	}

	@Override
	public void add(double value) {
	    double y = value - state.c;
	    t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}

	@Override
	public void add(Iterable<? extends Number> iterable) {
	    for (Number n: iterable) {
		double y = n.doubleValue() - state.c;
		t = state.total + y;
		state.c = (t - state.total) - y;
		state.total = t;
	    }
	}

	@Override
	public double getSum() {
	    return state.total;
	}

	@Override
	public void reset() {
	    state.total = 0.0;
	    state.c = 0.0;
	}
    }

    /**
     * Add a series of numbers using pairwise summation.
     * The values are recursively divided into two parts of about the
     * same size and summed.  For efficiency, a small number of
     * values (64) are summed directly, eliminating approximately the
     * last 6 levels of recursive calls.
     * <P>
     * When the values to be summed are in an array, with no values to
     * be added afterwards, this algorithm is only slightly less accurate
     * than Kahan's algorithm, and is computationally less expensive.
     * <P>
     * The static method {@link #getSum(double[])} should be used if the sum of
     * the elements in an array should be immediately calculated, as this
     * method avoids allocating an object.
     */
    public static final class Pairwise extends Adder {
	private static final int DEFAULT_SIZE = 128;

	private static final int N = 6;
	private static final int LIMIT = (1<<N);

	private double[] psums = new double[32];
	private boolean[] existing = new boolean[32];
	private int max = 0;
	private double subtotal = 0.0;
	private int limit = 0;

	@Override
	public void add(double[] array) {
	    add(array, 0, array.length);
	}

	@Override
	public void add(double[] array, int start, int end)
	    throws IllegalArgumentException
	{
	    if (end < start || start < 0 || end > array.length) {
		String msg = errorMsg("argsOutOfRange2", start, end);
		throw new IllegalArgumentException(msg);
	    }
	    int nprune = (end - start) % LIMIT;
	    while (nprune-- > 0) {
		// inline-code the add(double) method
		subtotal += array[start++];
		limit++;
		if (limit == LIMIT) {
		    int index = 0;
		    while (existing[index]) {
			subtotal += psums[index];
			existing[index] = false;
			psums[index] = 0.0;
			index++;
			if (index == psums.length) {
			    double[] tmp = new double[psums.length + 16];
			    boolean[] btmp = new boolean[existing.length + 16];
			    System.arraycopy(psums, 0, tmp, 0, psums.length);
			    System.arraycopy(existing, 0, tmp, 0,
					     existing.length);
			    psums = tmp;
			    existing = btmp;
			}
		    }
		    psums[index] = subtotal;
		    existing[index] = true;
		    if (index >= max) max++;
		    subtotal = 0.0;
		    limit = 0;
		}
	    }
	    if (start == end) return;
	    int n = end - start;
	    int m = LIMIT;
	    int index = 0;
	    while (m < n) {
		m = m << 1;
		index++;
	    }
	    if (m > n) {
		m = m >> 1;
		index--;
	    }
	    // m is now the largest integer that is less than n and
	    // that is also a power of 2, and index is the level of
	    // the tree (highest towards the root).
	    double sum = pairwise(array, start, end);
	    while (existing[index]) {
		sum += psums[index];
		existing[index] = false;
		psums[index] = 0.0;
		index++;
		if (index == psums.length) {
		    double[] tmp = new double[psums.length + 16];
		    boolean[] btmp = new boolean[existing.length + 16];
		    System.arraycopy(psums, 0, tmp, 0, psums.length);
		    System.arraycopy(existing, 0, tmp, 0,
				     existing.length);
		    psums = tmp;
		    existing = btmp;
		}
	    }
	    psums[index] = sum;
	    existing[index] = true;
	    if (index >= max) max = index+1;
	}

	@Override
	public void add(double value) {
	    subtotal += value;
	    limit++;
	    if (limit == LIMIT) {
		int index = 0;
		while (existing[index]) {
		    subtotal += psums[index];
		    existing[index] = false;
		    psums[index] = 0.0;
		    index++;
		    if (index == psums.length) {
			double[] tmp = new double[psums.length + 16];
			boolean[] btmp = new boolean[existing.length + 16];
			System.arraycopy(psums, 0, tmp, 0, psums.length);
			System.arraycopy(existing, 0, tmp, 0,
					 existing.length);
			psums = tmp;
			existing = btmp;
		    }
		}
		psums[index] = subtotal;
		existing[index] = true;
		if (index >= max) max++;
		subtotal = 0.0;
		limit = 0;
	    }
	}

	@Override
	public void add(Iterable<? extends Number> iterable) {
	    for (Number n: iterable) {
		subtotal += n.doubleValue();
		limit++;
		if (limit == LIMIT) {
		    int index = 0;
		    while (existing[index]) {
			subtotal += psums[index];
			existing[index] = false;
			psums[index] = 0.0;
			index++;
			if (index == psums.length) {
			    double[] tmp = new double[psums.length + 16];
			    boolean[] btmp = new boolean[existing.length + 16];
			    System.arraycopy(psums, 0, tmp, 0, psums.length);
			    System.arraycopy(existing, 0, tmp, 0,
					     existing.length);
			    psums = tmp;
			    existing = btmp;
			}
		    }
		    psums[index] = subtotal;
		    existing[index] = true;
		    if (index >= max) max++;
		    subtotal = 0.0;
		    limit = 0;
		}
	    }
	}

	@Override
	public double getSum() {
	    double sum = subtotal;
	    for (int i = 0; i < max; i++) {
		sum += psums[i];
	    }
	    return sum;
	}


	@Override
	public void reset() {
	    Arrays.fill(psums, 0.0);
	    Arrays.fill(existing, false);
	    max = 0;
	    subtotal = 0.0;
	    limit = 0;
	}

	/**
	 * Constructor.
	 * This assumes a default capacity of 128.
	 */
	public Pairwise () {
	    super();
	}

	/**
	 * Get the sum of the elements in an array.
	 * This is a shortcut method that avoids creating an object to
	 * handle the sum.
	 * @param array the array whose elements should be added together
	 * @return the sum of the array's elements
	 */
	public static double getSum(double[] array) {
	    return pairwise(array, 0, array.length);
	}

	static double pairwise(double[] array, int start, int end) {
	    if (end - start < LIMIT) {
		double total = 0.0;
		while (start < end) {
		    total += array[start++];
		}
		return total;
	    } else {
		int middle = (start + end)/2;
		return  pairwise(array, start, middle)
		    + pairwise(array, middle, end);
	    }
	}
    }
}

//  LocalWords:  exbundle Subclasses IllegalArgumentException foreach
//  LocalWords:  Iterable iterable Kahan HREF Kahan's JIT getState
//  LocalWords:  blockquote pre argsOutOfRange getSum
