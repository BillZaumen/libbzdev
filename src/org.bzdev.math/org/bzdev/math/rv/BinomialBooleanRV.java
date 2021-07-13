package org.bzdev.math.rv;
import org.bzdev.math.StaticRandom;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates 'true' or 'false' with a specified
 * probability.
 */
public class BinomialBooleanRV extends BooleanRandomVariable {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    private double prob;
    boolean atLimit = false;

    /**
     * Constructor.
     * @param prob the probability that the value is 'true'
     */
    public BinomialBooleanRV(double prob) throws IllegalArgumentException {
	if (prob < 0.0 || prob > 1.0) 
	    throw new IllegalArgumentException(errorMsg("outOfRange", prob));
	this.prob = prob;
	if (prob == 0.0 || prob == 1.0) atLimit = true;
    }

    /**
     * Get the probability that this random variable has the value
     * <code>true</code>.
     * @return the probability that this random variable has the value 'true'
     */
    public double getProb() {
	return prob;
    }

    public Boolean next() {
	if (atLimit) {
	    if (prob == 0.0) {
		return false;
	    } else if (prob == 1.0) {
		return true;
	    }
	}
	return (StaticRandom.nextDouble() <= prob);
    }
}

//  LocalWords:  exbundle outOfRange
