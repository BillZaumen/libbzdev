package org.bzdev.math.stats;
import org.bzdev.math.*;
import java.util.Arrays;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class representing an F-test statistic for Levene's test.
 * This class defines a statistic W that allows one to determine
 * if multiple subgroups (a total of k subgroups) of a sample of size N
 * have the same variances. The number of data points in the i<sup>th</sup>
 * subgroup is denoted as N<sub>i</sub> and the j<sup>th</sup> entry in 
 * the i<sup>th</sup> is denoted as Y<sub>ij</sub>.
 * <P>
 * The statistic W is defined as
 * W = ((N-k)/(k-1) (&sum;<sub>i=0</sub><sup>k-1</sup>
 * (<span style="text-decoration: overline">Z</span><sub>i.</sub>
 *  - <span style="text-decoration: overline">Z</span><sub>..</sub>)<sup>2</sup>) /
 *  ( &sum;<sub>i=0</sub><sup>k-1</sup>
 *    &sum;<sub>j=0</sub><sup>N<sub>i</sub>-1</sup>
 *    (Z<sub>ij</sub> -
 *    <span style="text-decoration: overline">Z</span><sub>i.</sub>)<sup>2</sup>)
 * where Z<sub>ij</sub> = |Y<sub>ij</sub> - M<sub>i</sub>|, and where
 * M<sub>i</sub> is defined as either
 * <OL>
 *    <LI> the mean of the i<sup>th</sup> subgroup.
 *    <LI> the median of the i<sup>th</sup> subgroup.
 *    <LI> the 10% trimmed mean of the i<sup>th</sup> subgroup.
 * </OL>
 * The definition used for M<sub>i</sub> is determined by a
 * {@link LeveneStat.Mode} argument in the constructor. If no
 * such argument is provided, the mean is used as the default.
 * Please see
 * <A href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda35a.htm">
 * Levene Test for Equality of Variances</A> for a full description of this
 * test.
 */
public class LeveneStat extends FStat {

    double W;

    /**
     * The mode for a Levene's test.
     * Levene's original paper used the mean value of each
     * subgroup when computing the array Z<sub>ij</sub> used
     * by {@link LeveneStat}.
     * For k subgroups indexed by i, with j the index within
     * a subgroup, then for a data set Y<sub>ij</sub>,
     * Z<sub>ij</sub> = |Y<sub>ij</sub> - M<sub>i</sub>|
     * where M<sub>i</sub> is defined as either
     * <OL>
     * <IT> the mean of the i<sup>th</sup> subgroup.
     * <IT> the median of the i<sup>th</sup> subgroup.
     * <IT> the 10% trimmed mean of the i<sup>th</sup> subgroup.
     * </OL>
     * @see LeveneStat
     */
    public static enum Mode {
	/**
	 * Indicate the use of the mean for the i<sup>th</sup> subgroup.
	 */
	MEAN,
	/**
	 * Indicate the use of the median for the i<sup>th</sup> subgroup.
	 */
	MEDIAN,
	/**
	 * Indicate the use of the 10% trimmed mean for the
	 * i<sup>th</sup> subgroup.
	 * <P>
	 * Note: this option needs additional testing.
	 */
	TRIMMED
    }

    /**
     * Constructor.
     * @param data arguments providing a series of data sets
     */
    public LeveneStat(double[]... data) {
	this(Mode.MEAN, data);
    }

    /**
     * Constructor providing a mode.
     * @param mode the mode (default to LeveneStat.Mode.MEAN if this
     *        argument is null), with valid modes being
     *        LeveneStat.Mode.MEAN, LeveneStat.Mode.MEDIAN, and
     *        LeveneStat.Mode.TRIMMED
     * @param data arguments providing a series of data sets
     * @see Mode
     */
    public LeveneStat(Mode mode, double[]... data) {
	super();
	if (mode == null) mode = Mode.MEAN;
	BasicStats tstats = new BasicStats.Population();
	BasicStats[] stats = new BasicStats[data.length];
	long count = 0;
	for (int i = 0; i < data.length; i++) {
	    stats[i] = new BasicStats.Population();
	    count += data[i].length;
	    // double[] row;
	    switch (mode) {
	    case MEAN:
		for (int j = 0; j < data[i].length; j++) {
		    double value = data[i][j];
		    stats[i].add(value);
		}
		break;
	    case MEDIAN:
		stats[i].add(BasicStats.median(data[i]));
		break;
	    case TRIMMED:
		stats[i].add(BasicStats.trimmedMean(10, data[i]));
		break;
	    }
	}
	for (int i = 0; i < data.length; i++) {
	    double mean = stats[i].getMean();
	    stats[i] = new BasicStats.Population();
	    for (int j = 0; j < data[i].length; j++) {
		double value = Math.abs(data[i][j] - mean);
		stats[i].add(value);
		tstats.add(value);
	    }
	}
	int k = stats.length;
	long dof1 = k-1;
	long dof2 = count - k;
	setDegreesOfFreedom(dof1, dof2);
	setSize(k);
	double sum1 = 0.0;
	double sum2 = 0.0;
	double zmean = tstats.getMean();
	for (BasicStats stat: stats) {
	    double term1 = (stat.getMean() - zmean);
	    long n = stat.size();
	    sum1 += n * term1*term1;
	    sum2 += n * stat.getVariance();
	}
	W = (dof2 * sum1) / (dof1 * sum2);
    }

    @Override
    public double getValue() {
	return W;
    }

    @Override
    public double optimalValue() {
	long d1 = getDegreesOfFreedom1();
	long d2 = getDegreesOfFreedom2();
	if (d1 <= 2) return 0.0;
	return ((d1 - 2.0) / d1) * (d2 / (d2 + 2.0));
	
    }
}

//  LocalWords:  exbundle Levene's th ij overline OL LeveneStat href
//  LocalWords:  Levene
