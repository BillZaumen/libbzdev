 package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

/**
 * Random variable that generates a double with uniform probability
 * over a range of values.
 */
public class UniformDoubleRV extends DoubleRandomVariable {

    boolean trivial;
    boolean fixed;
    double fixedValue;
    double base;
    double scale;

    private void init() {
	double lowerLimit = getMinimum();
	boolean lowerClosed = getMinimumClosed();
	double upperLimit = getMaximum();
	boolean upperClosed = getMaximumClosed();


	if (lowerClosed && !upperClosed) {
	    if (lowerLimit == 0.0 && upperLimit == 1.0) {
		trivial = true;
		fixed = false;
	    } else {
		trivial = false;
		fixed = false;
		base = lowerLimit;
		scale = upperLimit - lowerLimit;
	    }
	} else if (lowerClosed && upperClosed && lowerLimit == upperLimit) {
	    fixedValue = lowerLimit;
	    trivial = false;
	    fixed = true;
	} else {
	    trivial = false;
	    fixed = false;
	    base = lowerLimit;
	    if (!lowerClosed) base = Math.nextAfter(base, upperLimit);
	    scale = upperLimit - base;
	    if (scale == 0.0) {
		if (upperClosed) {
		    fixed = true;
		    fixedValue = base;
		} else {
		    throw new IllegalArgumentException
			("No double-precision numbers in range specified");
		}
	    } else {
		if (upperClosed) scale = Math.nextAfter(scale, 0.0);
	    }
	}
    }

    public Double next() {
	if (fixed) return fixedValue;
	double value = StaticRandom.nextDouble();
	if (trivial) {
	    return value;
	} else {
	    return base + scale * value;
	}
    }

    /**
     * Constructor.
     * The values produced will be in the range [lowerLimit, upperLimit).
     * @param lowerLimit the lower limit of the values produced
     * @param upperLimit the upper limit of the values produced
     */
    public UniformDoubleRV(double lowerLimit, double upperLimit) {
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
    public UniformDoubleRV(double lowerLimit, boolean lowerClosed, 
			   double upperLimit, boolean upperClosed)
    {
	setRequiredMinimum(lowerLimit, lowerClosed);
	setRequiredMaximum(upperLimit, upperClosed);
	init();
    }
}

//  LocalWords:  lowerLimit upperLimit lowerClosed upperClosed
