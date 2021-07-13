package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a binomial boolean-valued random variable.
 */
public class BinomialBooleanRVRV
    extends BooleanRandomVariableRV<BinomialBooleanRV>
{
    // static final BooleanRandomVariable value = new BinomialBooleanRV();

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }


    private DoubleRandomVariable prob = null;

    /**
     * Constructor given a random number that generates the probability
     * @param prob a random variable giving the probability that the
     *        value is 'true'
     */
    public BinomialBooleanRVRV(DoubleRandomVariable prob) {
	try {
	    this.prob = (DoubleRandomVariable) (prob.clone());
	} catch (CloneNotSupportedException e) {
	    // just in case, but random variables are supposed to be
	    // cloneable.
	    this.prob = prob;
	}
	this.prob.setRequiredMinimum(0.0, true);
	this.prob.setRequiredMaximum(1.0, true);
	determineIfOrdered(prob);
    }

    /**
     * Constructor given a probability.
     * Probabilities must be in the range [0.0, 1.0];
     * @param prob the probability that a value gemerated by a random
     *        variable created by this class is 'true'
     * @exception IllegalArgumentException the argument was out range
     */
    public BinomialBooleanRVRV(double prob) {
	if (prob < 0.0 || prob > 1.0)
	    throw new IllegalArgumentException(errorMsg("outOfRange", prob));
	this.prob = new FixedDoubleRV(prob);
    }

    public BinomialBooleanRV next() {
	return new BinomialBooleanRV(prob.next());
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	BinomialBooleanRVRV obj = (BinomialBooleanRVRV) super.clone();
	prob = (DoubleRandomVariable) prob.clone();
	return obj;
    }
}

//  LocalWords:  exbundle boolean BooleanRandomVariable cloneable
//  LocalWords:  BinomialBooleanRV gemerated IllegalArgumentException
//  LocalWords:  outOfRange
