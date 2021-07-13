package org.bzdev.math;
import org.bzdev.lang.MathOps;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.lang.ref.WeakReference;

//@exbundle org.bzdev.math.lpack.Math

/**
* Table implementation for generating Poisson-distributed values.
* This is appropriate for small values of the parameter lambda
* (which is equal to the mean value of a Poisson distribution),
* where a large number of Poisson-distributed numbers with the
* same parameter will be generated.  If the values of lambda are
* too large, numerical accuracy issues result in an incorrect
* computation.
* <P>
* There is a bound on the integers returned. If the actual value is
* too large, the implementation tries again. This distributes errors
* evenly.
*/
public final class PoissonTable {
    Double lambda;
    double[] array;
    int n;

    public static final double MAX_LAMBDA = 745.0;

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    /**
     * Get the mean value of the Poisson distribution represented by
     * this class.
     * @return the mean value of this class' Poisson distribution
     */
    public double getLambda() {
	return lambda.doubleValue();
    }

    /**
     * Get the limit for the highest integer that the {@link #next()}
     * method will produce.  The values will be less than this limit.
     * @return the limit
     */
    public double getN() {
	return n;
    }

    private static final double MIN_ARRAY_INCR = MathOps.pow(2.0, -36);

    /**
     * Estimate the limit on the integers that can be generated.
     * @param lambda the mean value of a Poisson distribution
     */
    public static int estimateN(double lambda) {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	if (lambda == 0.0) {
	    return 1;
	}
	ArrayList<Double> alist = new ArrayList<>();
	int i = 0;
	int val = 0;
	double p = StrictMath.exp(-lambda);
	double s = p;
 	int en = 0;
	while((p/s) > MIN_ARRAY_INCR) {
	    en++;
	    alist.add(s);
	    val += 1;
	    p *= lambda/val;
	    s += p;
	}
	return en;
    }

    /** Constructor.
     * @param lambda the mean value of the Poisson distribution
     */
    private PoissonTable(double lambda)	throws IllegalArgumentException {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	if (lambda > MAX_LAMBDA) {
	    throw new IllegalArgumentException
		(errorMsg("lambdaTooLargePT", lambda));
	}
	if (lambda == 0.0) {
	    array = new double[2];
	    array[0] = 1.0;
	    array[1] = 1.0;
	    n = 0;
	    this.lambda = lambda;
	    return;
	}
	this.lambda = lambda;
	ArrayList<Double> alist = new ArrayList<>();
	int i = 0;
	int val = 0;
	double p = StrictMath.exp(-lambda);
	double s = p;
	while((p/s) > MIN_ARRAY_INCR) {
	    alist.add(s);
	    val += 1;
	    p *= lambda/val;
	    s += p;
	}
	alist.add(1.0);
	int sz = alist.size();
	this.n = sz-1;
	array = new double[sz];
	i = 0;
	for (Double x: alist) {
	    array[i++] = x;
	}
    }

    private static WeakHashMap<Double,WeakReference<PoissonTable>> map =
	new WeakHashMap<>(1024, 2.0F);
    private static HashSet<PoissonTable> set = new HashSet<PoissonTable>(256);

    public static PoissonTable getTable(double lambda)
	throws IllegalArgumentException
    {
	if (lambda < 0.0)throw new IllegalArgumentException
			     (errorMsg("lambdaWasNegative", lambda));
	if (lambda > MAX_LAMBDA) return null;
	PoissonTable table = null;
	Double key = Double.valueOf(lambda);
	synchronized(map) {
	    WeakReference<PoissonTable> entry = map.get(key);
	    if (entry != null) {
		table = entry.get();
	    }
	}
	return table;
    }

    public static PoissonTable createTable(double lambda)
	throws IllegalArgumentException
    {
	PoissonTable table = null;
	Double key = Double.valueOf(lambda);
	synchronized(map) {
	    WeakReference<PoissonTable> entry = map.get(key);
	    if (entry != null) {
		table = entry.get();
	    }
	    if (table == null) {
		table = new PoissonTable(lambda);
		map.put(table.lambda, new WeakReference<PoissonTable>(table));
	    }
	}
	return table;
    }

    public static PoissonTable add(double lambda) {
	PoissonTable table;
	synchronized(set) {
	    table = createTable(lambda);
	    set.add(table);
	}
	return table;
    }

    public static PoissonTable remove(double lambda) {
	PoissonTable table = null;
	synchronized(set) {
	    table = getTable(lambda);
	    if (table != null) {
		set.remove(table);
	    }
	}
	return table;
    }

    /**
     * Get the next Poisson-distributed random number
     * @return the number
     */
    public int next() {
	int index;
	for(;;) {
	    double u = StaticRandom.nextDouble();
	    index = Arrays.binarySearch(array, u);
	    if (index < 0) {
		index = -(index + 1);
	    }
	    if (index == n) continue;
	    return index;
	}
    }
}

//  LocalWords:  exbundle lambdaWasNegative lambdaTooLargePT
