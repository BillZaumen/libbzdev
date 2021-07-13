package org.bzdev.math.stats;
import org.bzdev.math.RealValuedFunctOps;
import java.util.PriorityQueue;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class for generating the Kolmogorov-Smirnov statistic.
 */
public class KSStat extends Statistic {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }

    PriorityQueue<Double> pq;
    RealValuedFunctOps cpf;

    int n;
    volatile boolean moreData = true;
    double Dn = Double.NEGATIVE_INFINITY;

    private synchronized void createDn() {
	if (moreData) {
	    n = pq.size();
	    double numb = (double)n;
	    int i = 0;
	    Double d;
	    while ((d = pq.poll()) != null) {
		double y = d;
		i++;
		double v = cpf.valueAt(y);
		double max1 = v - (i-1)/numb;
		double max2 = (i/numb) - v;
		double max = (max1>max2)? max1: max2;
		if (Dn < max) Dn = max;
	    }
	    moreData = false;
	}
    }

    /**
     * Get the size of the data set.
     * @return the number of data points
     */
    public int size() {
	if (moreData) createDn();
	return n;
    }

    /**
     * Get the Kolmogorov-Smirnov test statistic D<sub>n</sub>.
     * @return the statistic.
     */
    @Override
    public double getValue() {
	if (moreData) createDn();
	return Dn;
    }

    @Override
    public ProbDistribution getDistribution() {
	if (moreData) createDn();
	return new KDistr(n);
    }



    /**
     * Constructor.
     * @param f the cumulative distribution function that the data is to be
     *         tested against.
     */
    public KSStat(RealValuedFunctOps f) {
	cpf = f;
	pq = new PriorityQueue<Double>();
    }

    /**
     * Constructor with an estimate of the data-set size.
     * @param f the cumulative distribution function that the data is to be
     *         tested against.
     * @param n an estimate of the data-set size
     */
    public KSStat(RealValuedFunctOps f, int n) {
	cpf = f;
	pq = new PriorityQueue<Double>(n);
    }


    /**
     * Constructor with an initial data set.
     * More data may be added until size() or getStatistic() is called.
     * @param f the cumulative distribution function that the data is to be
     *         tested against.
     * @param array the initial data set
     */
    public KSStat(RealValuedFunctOps f, double[] array) {
	cpf = f;
	for (int i = 0; i < array.length; i++) {
	    pq.add(array[i]);
	}
    }

    /**
     * Add data.
     * @param d a value to add to the data set
     * @exception IllegalStateException additional data cannot be added
     */
    public synchronized void add(double d)
	throws IllegalStateException
    {
	if (moreData == false)
	    throw new IllegalStateException(errorMsg("noMoreData"));
	pq.offer(d);
    }

    /**
     * Add multiple data points.
     * @param array a value to add to the data set
     * @exception IllegalStateException additional data cannot be added
     */
    public synchronized void add(double[] array)
	throws IllegalStateException
    {
	if (moreData == false)
	    throw new IllegalStateException(errorMsg("noMoreData"));
	for (int i = 0; i < array.length; i++) {
	    pq.offer(array[i]);
	}
    }
}
//  LocalWords:  exbundle Kolmogorov Smirnov getStatistic
//  LocalWords:  IllegalStateException noMoreData
