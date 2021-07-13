package org.bzdev.math.stats;
import org.bzdev.math.*;

//@exbundle org.bzdev.math.stats.lpack.Stats

/**
 * Class representing probability distributions.
 */
public abstract class ProbDistribution implements RealValuedDomainOps {

    static String errorMsg(String key, Object... args) {
	return StatsErrorMsg.errorMsg(key, args);
    }


    /**
     * Determine if this distribution is symmetric about 0.
     * A distribution is symmetric if its probability density f
     * satisfies f(x+y) = f(x-y) and that if x+y is in f's domain,
     * so is x-y.  Equivalently, it must satisfy Q(x+y) = P(x-y).

     * @return true if it is symmetric; false otherwise.
     */
    public abstract boolean isSymmetric(double x);


    /**
     * Get the cumulative distribution function.
     * @return the cumulative distribution function
     */
    public RealValuedFunction cdf() {
	return new RealValuedFunction() {
	    @Override
	    public double valueAt(double x) {
		return P(x);
	    }

	    @Override
	    public double derivAt(double x) {
		return pd(x);
	    }
	};
    }

    /**
     * Get the complement of the cumulative distribution function.
     * @return the complement of the cumulative distribution function
     */
    public RealValuedFunction cdfc() {
	return new RealValuedFunction() {
	    @Override
	    public double valueAt(double x) {
		return Q(x);
	    }

	    @Override
	    public double derivAt(double x) {
		return -pd(x);
	    }
	};
    }
    
    /**
     * Get the probability density
     * @param x the value for which the density is to be computed
     * @return the probability density
     * @exception UnsupportedOperationException this instance does not
     *            support this operation
     * @exception IllegalArgumentException the argument was out of range
     */
    public double pd(double x)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	throw new UnsupportedOperationException(errorMsg("notSupportedPD"));
    }

    /**
     * Get the probability that a value is no larger than the argument
     * @param x the argument
     * @return the probability
     * @exception IllegalArgumentException the argument was out of range
     */
    public abstract double P(double x)throws IllegalArgumentException;

    /**
     * Get the probability that a value is no smaller than the argument
     * @param x the argument
     * @return the probability
     */
    public double Q(double x) throws IllegalArgumentException
    {
	return 1.0 - P(x);
    }

    /**
     * Get the probability that a value is within the range [-x, x].
     * This method is meaningful for distributions that are symmetric
     * about 0.  When this is not the case, an UnsupportedOperationException
     * should be thrown.
     * @param x the argument
     * @return the probability that a value is in the range [-x, x]
     * @exception UnsupportedOperationException this operation is not
     *            supported
     * @exception IllegalArgumentException the argument was out of range
     */
    public double A(double x) 
	throws UnsupportedOperationException, IllegalArgumentException
    {
	throw new UnsupportedOperationException(errorMsg("notSupportedPD"));
    }


    /**
     * Get the inverse for the method {@link #P(double)}.
     * @param x an argument in the range [0.0, 1.0]
     * @return a value y  such that x = P(y)
     * @exception MathException an error occurred while computing the
     *            inverse
     * @exception IllegalArgumentException the argument was out of range
     */
    public double inverseP(double x) throws MathException {
	if (x < 0.0 || x > 1.0)
	    throw new IllegalArgumentException(errorMsg("invDomain", x));
	double dmin = getDomainMin();
	if (dmin == Double.NEGATIVE_INFINITY) {
	    if (x == 0.0) {
		return domainMinClosed()? Double.NEGATIVE_INFINITY:
		    -Double.MAX_VALUE;
	    }
	    dmin = -Double.MAX_VALUE;
	} else if (!domainMinClosed()) {
	    dmin = Math.nextAfter(dmin, Double.MAX_VALUE);
	}
	double dmax = getDomainMax();
	if (dmax == Double.POSITIVE_INFINITY) {
	    if (x == 1.0) {
		return domainMaxClosed()? Double.POSITIVE_INFINITY:
		    Double.MAX_VALUE;
	    }
	    dmax = Double.MAX_VALUE;
	} else if (!domainMaxClosed()) {
	    dmax = Math.nextAfter(dmax, -Double.MAX_VALUE);
	}
	if (x == 0.0) return dmin;
	if (x == 1.0) return dmax;
	double guess = 0.0;
	if (guess < dmin) {
	    guess = dmin;
	}
	if (guess > dmax) {
	    guess = dmax;
	}
	double low = guess;
	double pL = P(low);
	double incr = -1.0;
	while (pL > x) {
	    while ((low + incr) < dmin) incr /= 2.0;
	    low += incr;
	    incr *= 1.5;
	    pL = P(low);
	}
	if (low > guess) guess = low;
	incr = 1.0;
	double high = guess;
	double pH = P(high);
	while (pH < x) {
	    while ((high + incr) > dmax) incr /= 2.0;
	    high += incr;
	    incr *= 1.5;
	    pH = P(high);
	}
	RootFinder rf = new RootFinder.Brent() {
		public double function(double t) {
		    return P(t);
		}
	    };
	return rf.solve(x, low, high);
    }


    /**
     * Get the inverse for the method {@link #Q(double)}.
     * @param x an argument in the range [0.0, 1.0]
     * @return a value y such that x = Q(y)
     * @exception MathException an error occurred while computing the
     *            inverse
     * @exception IllegalArgumentException the argument was out of range
     */
    public double inverseQ(double x) {
	if (x < 0.0 || x > 1.0)
	    throw new IllegalArgumentException(errorMsg("invDomain", x));
	double dmin = getDomainMin();
	if (dmin == Double.NEGATIVE_INFINITY) {
	    if (x == 1.0) {
		return domainMinClosed()? Double.NEGATIVE_INFINITY:
		    -Double.MAX_VALUE;
	    }
	    dmin = -Double.MAX_VALUE;
	} else if (!domainMinClosed()) {
	    dmin = Math.nextAfter(dmin, Double.MAX_VALUE);
	}
	double dmax = getDomainMax();
	if (dmax == Double.POSITIVE_INFINITY) {
	    if (x == 0.0) {
		return domainMaxClosed()? Double.POSITIVE_INFINITY:
		    Double.MAX_VALUE;
	    }
	    dmax = Double.MAX_VALUE;
	} else if (!domainMaxClosed()) {
	    dmax = Math.nextAfter(dmax, -Double.MAX_VALUE);
	}
	if (x == 0.0) return dmax;
	if (x == 1.0) return dmin;

	double guess = 0.0;
	if (guess < dmin) {
	    guess = dmin;
	}
	if (guess > dmax) {
	    guess = dmax;
	}
	double low = guess;
	double qH = Q(low);
	double incr = -1.0;
	while (qH < x && low > dmin) {
	    while ((low + incr) < dmin) incr /= 2.0;
	    low += incr;
	    incr *= 1.5;
	    qH = Q(low);
	}
	incr = 1.0;
	double high = guess;
	double qL = Q(high);
	while (qL > x) {
	    if ((high + incr) > dmax) incr /= 2.0;
	    high += incr;
	    incr *= 1.5;
	    qL = Q(high);
	}
	RootFinder rf = new RootFinder.Brent() {
		public double function(double t) {
		    return Q(t);
		}
	    };
	return rf.solve(x, low, high);
    }

    /**
     * Get the inverse for the method {@link #A(double)}.
     * @param x the argument
     * @return a value y such that x = A(y)
     * @exception MathException an error occurred while computing the
     *            inverse
     * @exception IllegalArgumentException the argument was out of range
     * @exception UnsupportedOperationException the operation was not
     *            supported
     */
    public double inverseA(double x) {
	if (x < -1.0 || x > 1.0)
	    throw new IllegalArgumentException(errorMsg("invDomain", x));
	double dmin = getDomainMin();
	if (dmin == Double.NEGATIVE_INFINITY) {
	    if (x == -1.0) {
		return domainMinClosed()? Double.NEGATIVE_INFINITY:
		    -Double.MAX_VALUE;
	    }
	    dmin = -Double.MAX_VALUE;
	} else if (!domainMinClosed()) {
	    dmin = Math.nextAfter(dmin, Double.MAX_VALUE);
	}
	double dmax = getDomainMax();
	if (dmax == Double.POSITIVE_INFINITY) {
	    if (x == 1.0) {
		return domainMaxClosed()? Double.POSITIVE_INFINITY:
		    Double.MAX_VALUE;
	    }
	    dmax = Double.MAX_VALUE;
	} else if (!domainMaxClosed()) {
	    dmax = Math.nextAfter(dmax, -Double.MAX_VALUE);
	}

	double guess = 0.0;
	if (guess < dmin) {
	    guess = dmin;
	}
	if (guess > dmax) {
	    guess = dmax;
	}
	double low = guess;
	double aL = A(low);
	double incr = -1.0;
	while (aL > x) {
	    while ((low + incr) < dmin) incr /= 2.0;
	    low += incr;
	    incr *= 1.5;
	    aL = A(low);
	}
	if (low > guess) guess = low;
	incr = 1.0;
	double high = guess;
	double aH = A(high);
	while (aH < x) {
	    while ((high + incr) > dmax) incr /= 2.0;
	    high += incr;
	    incr *= 1.5;
	    aH = A(high);
	}
	RootFinder rf = new RootFinder.Brent() {
		public double function(double t) {
		    return A(t);
		}
	    };
	return rf.solve(x, low, high);
    }


    @Override
    public double getDomainMax() {
	return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean domainMaxClosed() {return false;}
	
    @Override
    public double getDomainMin() {
	return Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean domainMinClosed() {return false;}

    /**
     * Determine if an argument is within the domain of the
     * functions providing a probability density or cumulative
     * probability.
     * <P>
     * The default behavior of this method assumes the domain
     * is an interval and uses the methods
     * {@link #getDomainMin()}, {@link #getDomainMin()},
     * {@link #domainMinClosed()}, and {@link #domainMinClosed()}
     * to determine if the argument represents a point in the
     * functions domain.  If the domain is not an interval
     * with each end either open or closed, then
     * this method must be overridden.  If it is not possible
     * with a reasonable amount of computation to determine that
     * the argument is in the domain, an UnsupportedOperationException
     * may be thrown.  If this exception is thrown, it should be
     * thrown regardless of the argument.
     * @param x a value to test
     * @return true if  x is in this function's domain; false otherwise
     * @exception UnsupportedOperationException domain membership
     *            could not be determined.
     */
    @Override
    public boolean isInDomain(double x) throws UnsupportedOperationException {
	double xmin = getDomainMin();
	double xmax = getDomainMax();
	if (domainMinClosed()) {
	    if (x < xmin) {
		return false;
	    }
	} else {
	    if (x <= xmin) {
		return false;
	    }
	}
	if (domainMaxClosed()) {
	    if (x > xmax) {
		return false;
	    }
	} else {
	    if (x >= xmax) {
		return false;
	    }
	}
	return true;
    }
}
//  LocalWords:  exbundle f's UnsupportedOperationException invDomain
//  LocalWords:  IllegalArgumentException notSupportedPD getDomainMin
//  LocalWords:  MathException domainMinClosed
