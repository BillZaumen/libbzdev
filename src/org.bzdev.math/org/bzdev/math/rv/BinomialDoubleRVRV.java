package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of Double-valued binomial random variables.
 */
public class BinomialDoubleRVRV
    extends DoubleRandomVariableRV<BinomialDoubleRV>
{

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable prob;
    DoubleRandomVariable n;

    /**
     * Constructor.
     * For n &lt; 61, n is rounded to the nearest integer.
     * @param prob a random variable that determines the probability of
     *        a success for a single try.
     * @param n the number of tries.
     */
    public BinomialDoubleRVRV(DoubleRandomVariable prob, double n)
    {
	this(prob, new FixedDoubleRV(n));
    }
    /**
     * Constructor.
     * @param prob a random variable that determines the probability of
     *        a success for a single try.
     * @param n a random variable that determines the number of tries.
     */
    public BinomialDoubleRVRV(DoubleRandomVariable prob,
			       DoubleRandomVariable n)
    {
	// clone in case setMinimum or similar methods called.
	Class<?> clasz = null;
	try {
	    clasz = prob.getClass();
	    this.prob = (DoubleRandomVariable)prob.clone();
	    this.prob.tightenMinimum(0.0, true);
	    this.prob.tightenMaximum(1.0, true);
	    clasz = n.getClass();
	    this.n = (DoubleRandomVariable)n.clone();
	    this.n.tightenMinimum(0.0, false);
	    determineIfOrdered(prob, n);
	}  catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", clasz.getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    protected BinomialDoubleRV doNext() throws RandomVariableException {
	return new BinomialDoubleRV(prob.next(), n.next());
    }
}

//  LocalWords:  exbundle lt setMinimum noClone
