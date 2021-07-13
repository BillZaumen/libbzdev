package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Class to generate a sequence of Integer-valued binomial random variables.
 */
public class BinomialIntegerRVRV
    extends IntegerRandomVariableRV<BinomialIntegerRV>
{

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable prob;
    IntegerRandomVariable n;

    /**
     * Constructor.
     * @param prob a random variable that determines the probability of
     *        a success for a single try.
     * @param n the number of tries.
     */
    public BinomialIntegerRVRV(DoubleRandomVariable prob, int n)
    {
	this(prob, new FixedIntegerRV(n));
    }
    /**
     * Constructor.
     * @param prob a random variable that determines the probability of
     *        a success for a single try.
     * @param n a random variable that determines the number of tries.
     */
    public BinomialIntegerRVRV(DoubleRandomVariable prob,
			       IntegerRandomVariable n)
    {
	// clone in case setMinimum or similar methods called.
	Class<?> clasz = null;
	try {
	    clasz = prob.getClass();
	    this.prob = (DoubleRandomVariable)prob.clone();
	    this.prob.tightenMinimum(0.0, true);
	    this.prob.tightenMaximum(1.0, true);
	    clasz = n.getClass();
	    this.n = (IntegerRandomVariable)n.clone();
	    this.n.tightenMinimum(0, false);
	    determineIfOrdered(prob, n);
	}  catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", clasz.getName());
	    throw new RandomVariableException(msg, e);
	}
    }

    protected BinomialIntegerRV doNext() throws RandomVariableException {
	return new BinomialIntegerRV(prob.next(), n.next());
    }
}

//  LocalWords:  exbundle setMinimum noClone
