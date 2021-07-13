package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;
/**
 * Random variable that generates a long with uniform probability
 * over a range of values.
 */
public class UniformLongRV extends LongRandomVariable {

    boolean fixed;
    long fixedValue;
    long base;
    long range;

    private void init() {
	long lowerLimit = getMinimum();
	boolean lowerClosed = getMinimumClosed();
	long upperLimit = getMaximum();
	boolean upperClosed = getMaximumClosed();

	if (lowerClosed) {
	    if (upperClosed) {
		if (lowerLimit == upperLimit) {
		    fixed = true;
		    fixedValue = lowerLimit;
		} else {
		    base = lowerLimit;
		    range = upperLimit - lowerLimit + 1;
		}
	    } else {
		if (lowerLimit == upperLimit - 1) {
		    fixed = true;
		    fixedValue = lowerLimit;
		} else {
		    base = lowerLimit;
		    range = upperLimit - lowerLimit;
		}
	    }
	} else {
	    base = lowerLimit + 1;
	    if (upperClosed) {
		if (base == upperLimit) {
		    fixed = true;
		    fixedValue = base;
		} else {
		    range = upperLimit - base + 1;
		}
	    } else {
		if (base == upperLimit - 1) {
		    fixed = true;
		    fixedValue = base;
		} else {
		    range = upperLimit - base;
		}
	    }
	}
    }

    public Long next() {
	if (fixed) return fixedValue;
	if (range <= 0) throw new IllegalStateException
			    ("no integers in interval");
	if (range < Integer.MAX_VALUE) {
	    return base + (long)StaticRandom.nextInt((int)range);
	} else {
	    // very large range.
	    return base + Math.abs(StaticRandom.nextLong()) % range;
	}
    }

    /**
     * Constructor.
     * The values produced are in the range [lowerLimit, upperLimit).
     * @param lowerLimit the lower limit of the values produced
     * @param upperLimit the upper limit of the values produced
     */
    public UniformLongRV(long lowerLimit, long upperLimit) {
	this(lowerLimit, true, upperLimit, false);
    }

    /**
     * Constructor specifying range type (open, closed, etc.).
     * @param lowerLimit the lower limit of the values produced
     * @param lowerClosed true if the lower limit is included in the
     *        range of values produced; false otherwise
     * @param upperLimit the upper limit of the values produced
     * @param upperClosed true if the upper limit is included in the
     *        range of values produced; false otherwise
     */
    public UniformLongRV(long lowerLimit, boolean lowerClosed, 
			 long upperLimit, boolean upperClosed)
    {
	setRequiredMinimum(lowerLimit, lowerClosed);
	setRequiredMaximum(upperLimit, upperClosed);
	init();
    }
}

//  LocalWords:  lowerLimit upperLimit lowerClosed upperClosed
